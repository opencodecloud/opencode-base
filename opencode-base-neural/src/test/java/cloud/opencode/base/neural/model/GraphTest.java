package cloud.opencode.base.neural.model;

import cloud.opencode.base.neural.internal.TensorPool;
import cloud.opencode.base.neural.op.LinearOp;
import cloud.opencode.base.neural.op.Op;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link Graph}
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("Graph")
class GraphTest {

    @BeforeAll
    static void registerOps() {
        try { OpRegistry.register("Linear", LinearOp::new); } catch (Exception e) { /* already registered */ }
        try { OpRegistry.register("ReLU", ReluOp::new); } catch (Exception e) { /* already registered */ }
    }

    /**
     * Build a simple Linear -> ReLU graph:
     *   input (1, 2) -> Linear(2->3, with bias) -> ReLU -> output
     *
     * Weight matrix (3x2): [[1, 0], [0, 1], [1, 1]]
     * Bias (3): [0.5, -0.5, 0.0]
     *
     * For input [[1, -1]]:
     *   Linear: [1*1+(-1)*0+0.5, 1*0+(-1)*1+(-0.5), 1*1+(-1)*1+0.0] = [1.5, -1.5, 0.0]
     *   ReLU:   [1.5, 0.0, 0.0]
     */
    @Nested
    @DisplayName("Simple Linear -> ReLU Graph")
    class SimpleLinearReluGraph {

        @Test
        @DisplayName("should execute Linear -> ReLU and produce correct output")
        void executeLinearRelu() {
            // Weights: 3x2 matrix
            Tensor weight = Tensor.fromFloat(
                    new float[]{1, 0, 0, 1, 1, 1}, Shape.of(3, 2));
            // Bias: 3 elements
            Tensor bias = Tensor.fromFloat(
                    new float[]{0.5f, -0.5f, 0.0f}, Shape.of(3));

            // Node 0: Linear, input from graph input 0, weights: weight + bias
            GraphNode linearNode = new GraphNode(
                    "linear1", "Linear",
                    new LinearOp(), OpAttribute.empty(),
                    new int[][]{{-1, 0}},  // input from graph input slot 0
                    new Tensor[]{weight, bias}
            );

            // Node 1: ReLU, input from node 0 output slot 0
            GraphNode reluNode = new GraphNode(
                    "relu1", "ReLU",
                    new ReluOp(), OpAttribute.empty(),
                    new int[][]{{0, 0}},   // input from node 0, slot 0
                    new Tensor[0]
            );

            ModelMetadata metadata = new ModelMetadata("test-model", "test", "test model", 0);

            List<GraphInput> inputs = List.of(
                    new GraphInput("input", TensorType.FLOAT32, Shape.of(1, 2)));
            List<GraphOutput> outputs = List.of(
                    new GraphOutput("output", 1, 0));  // from relu node, slot 0

            Graph graph = new Graph(List.of(linearNode, reluNode), inputs, outputs, metadata);

            // Execute
            Tensor inputTensor = Tensor.fromFloat(new float[]{1.0f, -1.0f}, Shape.of(1, 2));
            Map<String, Tensor> result = graph.execute(Map.of("input", inputTensor));

            assertThat(result).containsKey("output");
            Tensor output = result.get("output");
            assertThat(output.shape().rank()).isEqualTo(2);
            assertThat(output.shape().dim(0)).isEqualTo(1);
            assertThat(output.shape().dim(1)).isEqualTo(3);

            float[] data = output.toFloatArray();
            assertThat(data[0]).isCloseTo(1.5f, within(1e-5f));
            assertThat(data[1]).isCloseTo(0.0f, within(1e-5f));
            assertThat(data[2]).isCloseTo(0.0f, within(1e-5f));
        }

        @Test
        @DisplayName("should provide graph metadata")
        void graphMetadata() {
            ModelMetadata metadata = new ModelMetadata("test", "author", "desc", 12345);
            Graph graph = new Graph(List.of(), List.of(), List.of(), metadata);
            assertThat(graph.metadata().name()).isEqualTo("test");
            assertThat(graph.metadata().author()).isEqualTo("author");
            assertThat(graph.nodes()).isEmpty();
            assertThat(graph.inputs()).isEmpty();
            assertThat(graph.outputs()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("should throw when required input is missing")
        void missingInput() {
            ModelMetadata metadata = new ModelMetadata("test", "author", "desc", 0);
            List<GraphInput> inputs = List.of(
                    new GraphInput("x", TensorType.FLOAT32, Shape.of(1, 2)));
            Graph graph = new Graph(List.of(), inputs, List.of(), metadata);

            assertThatThrownBy(() -> graph.execute(Map.of()))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("Missing graph input");
        }
    }

    @Nested
    @DisplayName("TensorPool Integration")
    class TensorPoolIntegration {

        @Test
        @DisplayName("should execute with TensorPool and produce correct output")
        void executeWithPool() {
            // Same Linear -> ReLU graph as above
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

            ModelMetadata metadata = new ModelMetadata("test-model", "test", "test model", 0);
            List<GraphInput> inputs = List.of(
                    new GraphInput("input", TensorType.FLOAT32, Shape.of(1, 2)));
            List<GraphOutput> outputs = List.of(
                    new GraphOutput("output", 1, 0));

            Graph graph = new Graph(List.of(linearNode, reluNode), inputs, outputs, metadata);
            TensorPool pool = new TensorPool();

            // Pool starts empty
            assertThat(pool.pooledCount()).isEqualTo(0);

            // Execute with pool
            Tensor inputTensor = Tensor.fromFloat(new float[]{1.0f, -1.0f}, Shape.of(1, 2));
            Map<String, Tensor> result = graph.execute(Map.of("input", inputTensor), pool);

            // Output should be correct
            assertThat(result).containsKey("output");
            Tensor output = result.get("output");
            float[] data = output.toFloatArray();
            assertThat(data[0]).isCloseTo(1.5f, within(1e-5f));
            assertThat(data[1]).isCloseTo(0.0f, within(1e-5f));
            assertThat(data[2]).isCloseTo(0.0f, within(1e-5f));

            // Pool should have released buffers for intermediate node (linear1 output)
            // since the ReLU node consumed it and it's not a graph output
            assertThat(pool.pooledCount()).isGreaterThan(0);
        }

        @Test
        @DisplayName("should not release graph output tensors to pool")
        void shouldNotReleaseGraphOutputs() {
            // Single node graph: Linear is both the only node and the output
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

            ModelMetadata metadata = new ModelMetadata("test-model", "test", "test model", 0);
            List<GraphInput> inputs = List.of(
                    new GraphInput("input", TensorType.FLOAT32, Shape.of(1, 2)));
            List<GraphOutput> outputs = List.of(
                    new GraphOutput("output", 0, 0)); // output from node 0 directly

            Graph graph = new Graph(List.of(linearNode), inputs, outputs, metadata);
            TensorPool pool = new TensorPool();

            Tensor inputTensor = Tensor.fromFloat(new float[]{1.0f, -1.0f}, Shape.of(1, 2));
            Map<String, Tensor> result = graph.execute(Map.of("input", inputTensor), pool);

            // Output should still be valid
            assertThat(result).containsKey("output");
            assertThat(result.get("output").toFloatArray()).isNotEmpty();

            // Pool should be empty: the only node is a graph output, so nothing was released
            assertThat(pool.pooledCount()).isEqualTo(0);
        }
    }
}
