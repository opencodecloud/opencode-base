package cloud.opencode.base.neural.session;

import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.model.Graph;
import cloud.opencode.base.neural.model.GraphInput;
import cloud.opencode.base.neural.model.GraphNode;
import cloud.opencode.base.neural.model.GraphOutput;
import cloud.opencode.base.neural.model.ModelMetadata;
import cloud.opencode.base.neural.model.OcmModel;
import cloud.opencode.base.neural.model.OcmWriter;
import cloud.opencode.base.neural.model.TensorInfo;
import cloud.opencode.base.neural.op.LinearOp;
import cloud.opencode.base.neural.op.OpAttribute;
import cloud.opencode.base.neural.op.OpRegistry;
import cloud.opencode.base.neural.op.ReluOp;
import cloud.opencode.base.neural.op.SoftmaxOp;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import cloud.opencode.base.neural.tensor.TensorType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * End-to-end integration tests for {@link InferenceSession}.
 * 端到端推理会话集成测试。
 *
 * <p>Builds a simple model programmatically (Linear(4->3) -> ReLU -> Linear(3->2) -> Softmax),
 * serializes it via {@link OcmWriter}, loads via {@link InferenceSession#load(byte[])},
 * and verifies correct inference results.</p>
 * <p>通过编程方式构建简单模型（Linear(4→3) → ReLU → Linear(3→2) → Softmax），
 * 通过 OcmWriter 序列化，通过 InferenceSession.load(byte[]) 加载，
 * 并验证正确的推理结果。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see InferenceSession
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("InferenceSession 端到端集成测试")
class InferenceSessionIntegrationTest {

    private static final float EPSILON = 1e-4f;

    /**
     * Ensure OpRegistry is initialized by referencing op classes.
     * 通过引用算子类确保 OpRegistry 已初始化。
     */
    @BeforeAll
    static void ensureOpsRegistered() {
        // Touching OpRegistry triggers static initializer which registers all built-in ops
        assertThat(OpRegistry.isSupported("Linear")).isTrue();
        assertThat(OpRegistry.isSupported("ReLU")).isTrue();
        assertThat(OpRegistry.isSupported("Softmax")).isTrue();
    }

    /**
     * Build a test model: Linear(4→3) → ReLU → Linear(3→2) → Softmax
     * with known weights for predictable output.
     *
     * <p>Weight design:</p>
     * <ul>
     *   <li>Linear1 weight [3,4]: identity-like mapping, bias [3] = 0</li>
     *   <li>Linear2 weight [2,3]: sums first two vs third, bias [2] = 0</li>
     * </ul>
     */
    private static byte[] buildTestModelBytes() {
        // Model: input("x", [1,4]) → Linear1(4→3) → ReLU → Linear2(3→2) → Softmax → output("y")

        // === Weights ===
        // Linear1: weight [3,4], bias [3]
        // W1 = [[1, 0, 0, 0],   → takes x[0]
        //        [0, 1, 0, 0],   → takes x[1]
        //        [0, 0, 1, 1]]   → takes x[2]+x[3]
        Tensor w1 = Tensor.fromFloat(new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 1
        }, Shape.of(3, 4));
        Tensor b1 = Tensor.fromFloat(new float[]{0, 0, 0}, Shape.of(3));

        // Linear2: weight [2,3], bias [2]
        // W2 = [[1, 1, 0],    → sums first two hidden units
        //        [0, 0, 1]]    → takes third hidden unit
        Tensor w2 = Tensor.fromFloat(new float[]{
                1, 1, 0,
                0, 0, 1
        }, Shape.of(2, 3));
        Tensor b2 = Tensor.fromFloat(new float[]{0, 0}, Shape.of(2));

        // === Graph Nodes ===
        // Node 0: Linear1 — input from graph input 0 (slot 0), weights: w1, b1
        GraphNode linear1Node = new GraphNode("linear1", "Linear", new LinearOp(),
                OpAttribute.empty(),
                new int[][]{{-1, 0}},       // input source: graph input index 0
                new Tensor[]{w1, b1});       // weight, bias

        // Node 1: ReLU — input from node 0 (slot 0), no weights
        GraphNode reluNode = new GraphNode("relu1", "ReLU", new ReluOp(),
                OpAttribute.empty(),
                new int[][]{{0, 0}},         // input from node 0, output slot 0
                new Tensor[]{});

        // Node 2: Linear2 — input from node 1 (slot 0), weights: w2, b2
        GraphNode linear2Node = new GraphNode("linear2", "Linear", new LinearOp(),
                OpAttribute.empty(),
                new int[][]{{1, 0}},         // input from node 1, output slot 0
                new Tensor[]{w2, b2});       // weight, bias

        // Node 3: Softmax — input from node 2 (slot 0), no weights
        GraphNode softmaxNode = new GraphNode("softmax1", "Softmax", new SoftmaxOp(),
                OpAttribute.empty(),
                new int[][]{{2, 0}},         // input from node 2, output slot 0
                new Tensor[]{});

        // === Graph Structure ===
        List<GraphInput> graphInputs = List.of(
                new GraphInput("x", TensorType.FLOAT32, Shape.of(1, 4)));
        List<GraphOutput> graphOutputs = List.of(
                new GraphOutput("y", 3, 0));  // output from node 3 (softmax), slot 0

        ModelMetadata metadata = new ModelMetadata(
                "test-model", "test-author", "Integration test model", System.currentTimeMillis());

        Graph graph = new Graph(
                List.of(linear1Node, reluNode, linear2Node, softmaxNode),
                graphInputs, graphOutputs, metadata);

        OcmModel model = new OcmModel(graph, metadata,
                List.of(new TensorInfo("x", TensorType.FLOAT32, Shape.of(1, 4))),
                List.of(new TensorInfo("y", TensorType.FLOAT32, Shape.of(1, 2))));

        return OcmWriter.write(model);
    }

    /**
     * Compute expected output manually for input [1, 2, 0, 3].
     *
     * <pre>
     * Linear1: [1,2,0,3] @ W1^T = [1*1+2*0+0*0+3*0, 1*0+2*1+0*0+3*0, 1*0+2*0+0*1+3*1] = [1, 2, 3]
     * ReLU: [1, 2, 3] (all positive, no change)
     * Linear2: [1,2,3] @ W2^T = [1*1+2*1+3*0, 1*0+2*0+3*1] = [3, 3]
     * Softmax: [exp(3)/(exp(3)+exp(3)), exp(3)/(exp(3)+exp(3))] = [0.5, 0.5]
     * </pre>
     */
    private static float[] computeExpectedOutput(float[] input) {
        // Linear1: weight [3,4] = [[1,0,0,0],[0,1,0,0],[0,0,1,1]]
        float h0 = input[0];                     // 1*x[0]
        float h1 = input[1];                     // 1*x[1]
        float h2 = input[2] + input[3];          // 1*x[2] + 1*x[3]

        // ReLU
        h0 = Math.max(0, h0);
        h1 = Math.max(0, h1);
        h2 = Math.max(0, h2);

        // Linear2: weight [2,3] = [[1,1,0],[0,0,1]]
        float o0 = h0 + h1;
        float o1 = h2;

        // Softmax
        float maxVal = Math.max(o0, o1);
        float exp0 = (float) Math.exp(o0 - maxVal);
        float exp1 = (float) Math.exp(o1 - maxVal);
        float sumExp = exp0 + exp1;

        return new float[]{exp0 / sumExp, exp1 / sumExp};
    }

    // ==================== Test Classes ====================

    @Nested
    @DisplayName("端到端推理测试")
    class EndToEndInferenceTest {

        @Test
        @DisplayName("已知输入产生正确输出 — 形状 [1,2] 且概率和为 1.0")
        void knownInputProducesCorrectOutput() {
            byte[] modelBytes = buildTestModelBytes();

            try (InferenceSession session = InferenceSession.load(modelBytes)) {
                Tensor inputTensor = Tensor.fromFloat(
                        new float[]{1.0f, 2.0f, 0.0f, 3.0f}, Shape.of(1, 4));
                Map<String, Tensor> outputs = session.run(Map.of("x", inputTensor));

                assertThat(outputs).containsKey("y");
                Tensor output = outputs.get("y");

                // Verify shape
                assertThat(output.shape()).isEqualTo(Shape.of(1, 2));

                // Verify values match manual calculation
                float[] expected = computeExpectedOutput(new float[]{1.0f, 2.0f, 0.0f, 3.0f});
                assertThat(output.getFloat(0, 0)).isCloseTo(expected[0], within(EPSILON));
                assertThat(output.getFloat(0, 1)).isCloseTo(expected[1], within(EPSILON));

                // Verify softmax probabilities sum to 1.0
                float sum = output.getFloat(0, 0) + output.getFloat(0, 1);
                assertThat(sum).isCloseTo(1.0f, within(EPSILON));
            }
        }

        @Test
        @DisplayName("全零输入 — ReLU 输出为零，Softmax 输出均匀分布")
        void zeroInputProducesUniformSoftmax() {
            byte[] modelBytes = buildTestModelBytes();

            try (InferenceSession session = InferenceSession.load(modelBytes)) {
                Tensor inputTensor = Tensor.fromFloat(
                        new float[]{0.0f, 0.0f, 0.0f, 0.0f}, Shape.of(1, 4));
                Map<String, Tensor> outputs = session.run(Map.of("x", inputTensor));

                Tensor output = outputs.get("y");

                // All zeros through Linear→ReLU→Linear→Softmax = [0,0] → softmax = [0.5, 0.5]
                assertThat(output.getFloat(0, 0)).isCloseTo(0.5f, within(EPSILON));
                assertThat(output.getFloat(0, 1)).isCloseTo(0.5f, within(EPSILON));
            }
        }

        @Test
        @DisplayName("负输入触发 ReLU 裁剪 — 输出仍正确")
        void negativeInputClippedByRelu() {
            byte[] modelBytes = buildTestModelBytes();

            try (InferenceSession session = InferenceSession.load(modelBytes)) {
                // input = [-1, -2, 3, 4]
                // Linear1: [-1, -2, 7]
                // ReLU: [0, 0, 7]
                // Linear2: [0, 7]
                // Softmax: [exp(0)/(exp(0)+exp(7)), exp(7)/(exp(0)+exp(7))]
                Tensor inputTensor = Tensor.fromFloat(
                        new float[]{-1.0f, -2.0f, 3.0f, 4.0f}, Shape.of(1, 4));
                Map<String, Tensor> outputs = session.run(Map.of("x", inputTensor));

                Tensor output = outputs.get("y");
                float[] expected = computeExpectedOutput(new float[]{-1.0f, -2.0f, 3.0f, 4.0f});

                assertThat(output.getFloat(0, 0)).isCloseTo(expected[0], within(EPSILON));
                assertThat(output.getFloat(0, 1)).isCloseTo(expected[1], within(EPSILON));

                // Second class should dominate (7 >> 0)
                assertThat(output.getFloat(0, 1)).isGreaterThan(0.99f);
            }
        }
    }

    @Nested
    @DisplayName("会话生命周期测试")
    class SessionLifecycleTest {

        @Test
        @DisplayName("warmup() 正常执行不抛异常")
        void warmupRunsSuccessfully() {
            byte[] modelBytes = buildTestModelBytes();

            try (InferenceSession session = InferenceSession.load(modelBytes)) {
                assertThatCode(() -> session.warmup(3))
                        .doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("metadata/inputInfo/outputInfo 返回正确信息")
        void metadataAndInfoCorrect() {
            byte[] modelBytes = buildTestModelBytes();

            try (InferenceSession session = InferenceSession.load(modelBytes)) {
                // Metadata
                assertThat(session.metadata().name()).isEqualTo("test-model");
                assertThat(session.metadata().author()).isEqualTo("test-author");
                assertThat(session.metadata().description()).isEqualTo("Integration test model");

                // Input info
                List<TensorInfo> inputInfos = session.inputInfo();
                assertThat(inputInfos).hasSize(1);
                assertThat(inputInfos.get(0).name()).isEqualTo("x");
                assertThat(inputInfos.get(0).type()).isEqualTo(TensorType.FLOAT32);

                // Output info
                List<TensorInfo> outputInfos = session.outputInfo();
                assertThat(outputInfos).hasSize(1);
                assertThat(outputInfos.get(0).name()).isEqualTo("y");
            }
        }

        @Test
        @DisplayName("close() 后 run() 抛出 NeuralException")
        void runAfterCloseThrows() {
            byte[] modelBytes = buildTestModelBytes();
            InferenceSession session = InferenceSession.load(modelBytes);
            session.close();

            Tensor inputTensor = Tensor.fromFloat(
                    new float[]{1.0f, 2.0f, 3.0f, 4.0f}, Shape.of(1, 4));

            assertThatThrownBy(() -> session.run(Map.of("x", inputTensor)))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("close() 后 metadata() 抛出 NeuralException")
        void metadataAfterCloseThrows() {
            byte[] modelBytes = buildTestModelBytes();
            InferenceSession session = InferenceSession.load(modelBytes);
            session.close();

            assertThatThrownBy(session::metadata)
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("并发推理测试")
    class ConcurrentInferenceTest {

        @Test
        @DisplayName("两个线程并发 run() 均产生正确结果")
        void concurrentRunProducesCorrectResults() throws Exception {
            byte[] modelBytes = buildTestModelBytes();

            try (InferenceSession session = InferenceSession.load(modelBytes)) {
                float[] input1Data = {1.0f, 2.0f, 0.0f, 3.0f};
                float[] input2Data = {-1.0f, -2.0f, 3.0f, 4.0f};

                float[] expected1 = computeExpectedOutput(input1Data);
                float[] expected2 = computeExpectedOutput(input2Data);

                ExecutorService executor = Executors.newFixedThreadPool(2);
                CountDownLatch startLatch = new CountDownLatch(1);

                Future<float[]> future1 = executor.submit(() -> {
                    startLatch.await();
                    Tensor input = Tensor.fromFloat(input1Data, Shape.of(1, 4));
                    Map<String, Tensor> result = session.run(Map.of("x", input));
                    Tensor out = result.get("y");
                    return new float[]{out.getFloat(0, 0), out.getFloat(0, 1)};
                });

                Future<float[]> future2 = executor.submit(() -> {
                    startLatch.await();
                    Tensor input = Tensor.fromFloat(input2Data, Shape.of(1, 4));
                    Map<String, Tensor> result = session.run(Map.of("x", input));
                    Tensor out = result.get("y");
                    return new float[]{out.getFloat(0, 0), out.getFloat(0, 1)};
                });

                // Start both threads at the same time
                startLatch.countDown();

                float[] result1 = future1.get(5, TimeUnit.SECONDS);
                float[] result2 = future2.get(5, TimeUnit.SECONDS);

                executor.shutdown();

                // Verify both results are correct
                assertThat(result1[0]).isCloseTo(expected1[0], within(EPSILON));
                assertThat(result1[1]).isCloseTo(expected1[1], within(EPSILON));

                assertThat(result2[0]).isCloseTo(expected2[0], within(EPSILON));
                assertThat(result2[1]).isCloseTo(expected2[1], within(EPSILON));
            }
        }
    }

    @Nested
    @DisplayName("序列化往返测试")
    class SerializationRoundtripTest {

        @Test
        @DisplayName("模型序列化后反序列化推理结果不变")
        void serializeDeserializeProducesSameResult() {
            byte[] modelBytes = buildTestModelBytes();

            // Run inference on the loaded model
            float[] inputData = {2.0f, 3.0f, 1.0f, 1.0f};
            float[] expected = computeExpectedOutput(inputData);

            try (InferenceSession session = InferenceSession.load(modelBytes)) {
                Tensor inputTensor = Tensor.fromFloat(inputData, Shape.of(1, 4));
                Map<String, Tensor> outputs = session.run(Map.of("x", inputTensor));
                Tensor output = outputs.get("y");

                assertThat(output.getFloat(0, 0)).isCloseTo(expected[0], within(EPSILON));
                assertThat(output.getFloat(0, 1)).isCloseTo(expected[1], within(EPSILON));

                // Verify sum = 1.0
                float sum = output.getFloat(0, 0) + output.getFloat(0, 1);
                assertThat(sum).isCloseTo(1.0f, within(EPSILON));
            }
        }
    }
}
