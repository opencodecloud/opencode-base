package cloud.opencode.base.neural.model;

import cloud.opencode.base.neural.exception.ModelFormatException;
import cloud.opencode.base.neural.exception.ModelLoadException;
import cloud.opencode.base.neural.op.LinearOp;
import cloud.opencode.base.neural.op.OpAttribute;
import cloud.opencode.base.neural.op.OpRegistry;
import cloud.opencode.base.neural.op.ReluOp;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import cloud.opencode.base.neural.tensor.TensorType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link OcmWriter} and {@link OcmLoader}
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("OcmWriter / OcmLoader")
class OcmWriterLoaderTest {

    @BeforeAll
    static void registerOps() {
        try { OpRegistry.register("Linear", LinearOp::new); } catch (Exception e) { /* already registered */ }
        try { OpRegistry.register("ReLU", ReluOp::new); } catch (Exception e) { /* already registered */ }
    }

    /**
     * Builds a simple Linear -> ReLU model for round-trip testing.
     */
    private OcmModel buildTestModel() {
        Tensor weight = Tensor.fromFloat(
                new float[]{1, 0, 0, 1, 1, 1}, Shape.of(3, 2));
        Tensor bias = Tensor.fromFloat(
                new float[]{0.5f, -0.5f, 0.0f}, Shape.of(3));

        GraphNode linearNode = new GraphNode(
                "linear1", "Linear",
                new LinearOp(), OpAttribute.empty(),
                new int[][]{{-1, 0}},
                new Tensor[]{weight, bias}
        );

        GraphNode reluNode = new GraphNode(
                "relu1", "ReLU",
                new ReluOp(), OpAttribute.empty(),
                new int[][]{{0, 0}},
                new Tensor[0]
        );

        ModelMetadata metadata = new ModelMetadata(
                "test-mlp", "Leon Soo", "A simple MLP for testing", 1700000000000L);

        List<GraphInput> graphInputs = List.of(
                new GraphInput("input", TensorType.FLOAT32, Shape.of(1, 2)));
        List<GraphOutput> graphOutputs = List.of(
                new GraphOutput("output", 1, 0));

        Graph graph = new Graph(
                List.of(linearNode, reluNode), graphInputs, graphOutputs, metadata);

        List<TensorInfo> inputInfos = List.of(
                new TensorInfo("input", TensorType.FLOAT32, Shape.of(1, 2)));
        List<TensorInfo> outputInfos = List.of(
                new TensorInfo("output", TensorType.FLOAT32, Shape.of(1, 3)));

        return new OcmModel(graph, metadata, inputInfos, outputInfos);
    }

    @Nested
    @DisplayName("Round-trip: write then load from byte array")
    class ByteArrayRoundTrip {

        @Test
        @DisplayName("should preserve metadata after round-trip")
        void preserveMetadata() {
            OcmModel original = buildTestModel();
            byte[] data = OcmWriter.write(original);
            OcmModel loaded = OcmLoader.load(data);

            assertThat(loaded.metadata().name()).isEqualTo("test-mlp");
            assertThat(loaded.metadata().author()).isEqualTo("Leon Soo");
            assertThat(loaded.metadata().description()).isEqualTo("A simple MLP for testing");
            assertThat(loaded.metadata().createdAt()).isEqualTo(1700000000000L);
        }

        @Test
        @DisplayName("should preserve graph structure after round-trip")
        void preserveGraphStructure() {
            OcmModel original = buildTestModel();
            byte[] data = OcmWriter.write(original);
            OcmModel loaded = OcmLoader.load(data);

            Graph graph = loaded.graph();
            assertThat(graph.nodes()).hasSize(2);
            assertThat(graph.inputs()).hasSize(1);
            assertThat(graph.outputs()).hasSize(1);

            assertThat(graph.nodes().get(0).name()).isEqualTo("linear1");
            assertThat(graph.nodes().get(0).opType()).isEqualTo("Linear");
            assertThat(graph.nodes().get(1).name()).isEqualTo("relu1");
            assertThat(graph.nodes().get(1).opType()).isEqualTo("ReLU");

            assertThat(graph.inputs().get(0).name()).isEqualTo("input");
            assertThat(graph.outputs().get(0).name()).isEqualTo("output");
        }

        @Test
        @DisplayName("should produce correct output from loaded model")
        void executeLoadedModel() {
            OcmModel original = buildTestModel();
            byte[] data = OcmWriter.write(original);
            OcmModel loaded = OcmLoader.load(data);

            Tensor input = Tensor.fromFloat(new float[]{1.0f, -1.0f}, Shape.of(1, 2));
            Map<String, Tensor> result = loaded.graph().execute(Map.of("input", input));

            assertThat(result).containsKey("output");
            float[] output = result.get("output").toFloatArray();
            assertThat(output[0]).isCloseTo(1.5f, within(1e-5f));
            assertThat(output[1]).isCloseTo(0.0f, within(1e-5f));
            assertThat(output[2]).isCloseTo(0.0f, within(1e-5f));
        }

        @Test
        @DisplayName("should preserve weight data after round-trip")
        void preserveWeights() {
            OcmModel original = buildTestModel();
            byte[] data = OcmWriter.write(original);
            OcmModel loaded = OcmLoader.load(data);

            GraphNode linearNode = loaded.graph().nodes().get(0);
            Tensor[] weights = linearNode.weights();
            assertThat(weights).hasSize(2);

            // Weight matrix: 3x2
            assertThat(weights[0].shape().dim(0)).isEqualTo(3);
            assertThat(weights[0].shape().dim(1)).isEqualTo(2);
            float[] weightData = weights[0].toFloatArray();
            assertThat(weightData).containsExactly(1, 0, 0, 1, 1, 1);

            // Bias: 3
            assertThat(weights[1].shape().dim(0)).isEqualTo(3);
            float[] biasData = weights[1].toFloatArray();
            assertThat(biasData[0]).isCloseTo(0.5f, within(1e-6f));
            assertThat(biasData[1]).isCloseTo(-0.5f, within(1e-6f));
            assertThat(biasData[2]).isCloseTo(0.0f, within(1e-6f));
        }
    }

    @Nested
    @DisplayName("Round-trip: write then load from file")
    class FileRoundTrip {

        @Test
        @DisplayName("should round-trip through a file correctly")
        void fileRoundTrip(@TempDir Path tempDir) {
            OcmModel original = buildTestModel();
            Path modelPath = tempDir.resolve("test.ocm");
            OcmWriter.write(original, modelPath);

            OcmModel loaded = OcmLoader.load(modelPath);
            assertThat(loaded.metadata().name()).isEqualTo("test-mlp");

            // Verify execution produces same result
            Tensor input = Tensor.fromFloat(new float[]{2.0f, 3.0f}, Shape.of(1, 2));
            Map<String, Tensor> result = loaded.graph().execute(Map.of("input", input));
            float[] output = result.get("output").toFloatArray();

            // Linear: [2*1+3*0+0.5, 2*0+3*1-0.5, 2*1+3*1+0.0] = [2.5, 2.5, 5.0]
            // ReLU: [2.5, 2.5, 5.0] (all positive)
            assertThat(output[0]).isCloseTo(2.5f, within(1e-5f));
            assertThat(output[1]).isCloseTo(2.5f, within(1e-5f));
            assertThat(output[2]).isCloseTo(5.0f, within(1e-5f));
        }
    }

    @Nested
    @DisplayName("Format Validation")
    class FormatValidation {

        @Test
        @DisplayName("should reject data with invalid magic number")
        void invalidMagic() {
            byte[] data = new byte[64];
            // Write wrong magic
            ByteBuffer.wrap(data).putInt(0, 0xDEADBEEF);

            assertThatThrownBy(() -> OcmLoader.load(data))
                    .isInstanceOf(ModelFormatException.class)
                    .hasMessageContaining("Invalid magic number");
        }

        @Test
        @DisplayName("should reject data with unsupported major version")
        void unsupportedVersion() {
            byte[] data = new byte[128];
            ByteBuffer buf = ByteBuffer.wrap(data);
            buf.putInt(OcmFormat.MAGIC);
            buf.putShort((short) 99);  // unsupported major version
            buf.putShort((short) 0);

            assertThatThrownBy(() -> OcmLoader.load(data))
                    .isInstanceOf(ModelLoadException.class)
                    .hasMessageContaining("Unsupported model version");
        }

        @Test
        @DisplayName("written data should start with correct header")
        void headerFormat() {
            OcmModel model = buildTestModel();
            byte[] data = OcmWriter.write(model);

            assertThat(data.length).isGreaterThan(OcmFormat.HEADER_SIZE);

            ByteBuffer buf = ByteBuffer.wrap(data);
            assertThat(buf.getInt()).isEqualTo(OcmFormat.MAGIC);
            assertThat(buf.getShort()).isEqualTo(OcmFormat.VERSION_MAJOR);
            assertThat(buf.getShort()).isEqualTo(OcmFormat.VERSION_MINOR);
        }
    }

    @Nested
    @DisplayName("OcmModel lifecycle")
    class ModelLifecycle {

        @Test
        @DisplayName("close should release weight tensors")
        void closeReleasesWeights() {
            OcmModel model = buildTestModel();
            assertThat(model.isClosed()).isFalse();

            model.close();
            assertThat(model.isClosed()).isTrue();

            // Double close should be safe
            model.close();
            assertThat(model.isClosed()).isTrue();
        }

        @Test
        @DisplayName("should report input and output info")
        void inputOutputInfo() {
            OcmModel model = buildTestModel();
            assertThat(model.inputInfo()).hasSize(1);
            assertThat(model.inputInfo().get(0).name()).isEqualTo("input");
            assertThat(model.outputInfo()).hasSize(1);
            assertThat(model.outputInfo().get(0).name()).isEqualTo("output");
        }
    }
}
