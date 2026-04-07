package cloud.opencode.base.neural.model;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.internal.TensorPool;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Computation Graph
 * 计算图
 *
 * <p>Represents a directed acyclic computation graph consisting of topologically
 * sorted {@link GraphNode} nodes. Inputs are fed by name, outputs are collected
 * by name after execution. All nodes are executed sequentially in topological order.</p>
 * <p>表示由拓扑排序的 {@link GraphNode} 节点组成的有向无环计算图。
 * 输入按名称提供，输出在执行后按名称收集。所有节点按拓扑顺序顺序执行。</p>
 *
 * <p><strong>Execution Model | 执行模型:</strong></p>
 * <ol>
 *   <li>Map input names to graph input indices - 将输入名称映射到计算图输入索引</li>
 *   <li>Execute each node in topological order, collecting inputs from graph
 *       inputs or prior nodes' results - 按拓扑顺序执行每个节点</li>
 *   <li>Collect output tensors by name from specified nodes - 按名称从指定节点收集输出张量</li>
 * </ol>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (execute is not reentrant) -
 *       线程安全: 否（execute 不可重入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see GraphNode
 * @see GraphInput
 * @see GraphOutput
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class Graph {

    private final List<GraphNode> nodes;
    private final List<GraphInput> inputs;
    private final List<GraphOutput> outputs;
    private final ModelMetadata metadata;

    /**
     * Create a new computation graph
     * 创建新的计算图
     *
     * @param nodes    topologically sorted graph nodes | 拓扑排序的计算图节点
     * @param inputs   graph input descriptors | 计算图输入描述符
     * @param outputs  graph output descriptors | 计算图输出描述符
     * @param metadata model metadata | 模型元数据
     */
    public Graph(List<GraphNode> nodes, List<GraphInput> inputs,
                 List<GraphOutput> outputs, ModelMetadata metadata) {
        Objects.requireNonNull(nodes, "nodes must not be null");
        Objects.requireNonNull(inputs, "inputs must not be null");
        Objects.requireNonNull(outputs, "outputs must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        this.nodes = List.copyOf(nodes);
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.metadata = metadata;
    }

    /**
     * Get the graph nodes (topologically sorted)
     * 获取计算图节点（拓扑排序）
     *
     * @return unmodifiable list of nodes | 不可修改的节点列表
     */
    public List<GraphNode> nodes() {
        return nodes;
    }

    /**
     * Get the graph input descriptors
     * 获取计算图输入描述符
     *
     * @return unmodifiable list of inputs | 不可修改的输入列表
     */
    public List<GraphInput> inputs() {
        return inputs;
    }

    /**
     * Get the graph output descriptors
     * 获取计算图输出描述符
     *
     * @return unmodifiable list of outputs | 不可修改的输出列表
     */
    public List<GraphOutput> outputs() {
        return outputs;
    }

    /**
     * Get the model metadata
     * 获取模型元数据
     *
     * @return model metadata | 模型元数据
     */
    public ModelMetadata metadata() {
        return metadata;
    }

    /**
     * Execute the computation graph with the given inputs
     * 使用给定输入执行计算图
     *
     * <p>Nodes are executed in topological order. Each node collects its inputs
     * from either graph inputs (when source nodeIdx == -1) or from the results
     * of previously executed nodes. Weight tensors are appended after dynamic inputs.</p>
     * <p>节点按拓扑顺序执行。每个节点从计算图输入（当源节点索引为 -1 时）
     * 或从先前执行的节点的结果中收集其输入。权重张量附加在动态输入之后。</p>
     *
     * @param inputMap map of input name to tensor | 输入名称到张量的映射
     * @return map of output name to tensor | 输出名称到张量的映射
     * @throws NeuralException if execution fails | 执行失败时抛出
     */
    public Map<String, Tensor> execute(Map<String, Tensor> inputMap) {
        Objects.requireNonNull(inputMap, "inputMap must not be null");

        // 1. Map input names to indices
        Map<String, Integer> inputNameToIndex = new HashMap<>();
        for (int i = 0; i < inputs.size(); i++) {
            inputNameToIndex.put(inputs.get(i).name(), i);
        }

        // Validate all required inputs are present
        Tensor[] graphInputTensors = new Tensor[inputs.size()];
        for (int i = 0; i < inputs.size(); i++) {
            String name = inputs.get(i).name();
            Tensor t = inputMap.get(name);
            if (t == null) {
                throw new NeuralException(
                        "Missing graph input: " + name,
                        NeuralErrorCode.GRAPH_EXECUTION_FAILED);
            }
            graphInputTensors[i] = t;
        }

        // 2. Execute nodes in topological order
        @SuppressWarnings("unchecked")
        List<Tensor>[] nodeResults = new List[nodes.size()];

        for (int i = 0; i < nodes.size(); i++) {
            GraphNode node = nodes.get(i);
            int[][] sources = node.inputSources();

            // Collect dynamic inputs from graph inputs or prior node results
            List<Tensor> nodeInputs = new ArrayList<>();
            for (int[] source : sources) {
                int srcNodeIdx = source[0];
                int srcSlot = source[1];

                if (srcNodeIdx == -1) {
                    // Source is a graph input
                    if (srcSlot < 0 || srcSlot >= graphInputTensors.length) {
                        throw new NeuralException(
                                "Invalid graph input slot " + srcSlot + " for node " + node.name(),
                                NeuralErrorCode.GRAPH_EXECUTION_FAILED);
                    }
                    nodeInputs.add(graphInputTensors[srcSlot]);
                } else {
                    // Source is a prior node's output
                    if (srcNodeIdx < 0 || srcNodeIdx >= i) {
                        throw new NeuralException(
                                "Invalid source node index " + srcNodeIdx + " for node " + node.name(),
                                NeuralErrorCode.GRAPH_EXECUTION_FAILED);
                    }
                    List<Tensor> srcResults = nodeResults[srcNodeIdx];
                    if (srcSlot < 0 || srcSlot >= srcResults.size()) {
                        throw new NeuralException(
                                "Invalid output slot " + srcSlot + " from node " + srcNodeIdx
                                        + " for node " + node.name(),
                                NeuralErrorCode.GRAPH_EXECUTION_FAILED);
                    }
                    nodeInputs.add(srcResults.get(srcSlot));
                }
            }

            // Append weight tensors
            for (Tensor w : node.weights()) {
                nodeInputs.add(w);
            }

            // Execute the node
            try {
                nodeResults[i] = node.execute(nodeInputs);
            } catch (NeuralException e) {
                throw e;
            } catch (Exception e) {
                throw new NeuralException(
                        "Failed to execute node " + node.name() + ": " + e.getMessage(),
                        e,
                        NeuralErrorCode.GRAPH_EXECUTION_FAILED);
            }
        }

        // 3. Collect outputs
        Map<String, Tensor> outputMap = new HashMap<>();
        for (GraphOutput output : outputs) {
            int nodeIdx = output.nodeIndex();
            int slot = output.outputSlot();
            if (nodeIdx < 0 || nodeIdx >= nodes.size()) {
                throw new NeuralException(
                        "Invalid output node index " + nodeIdx,
                        NeuralErrorCode.GRAPH_EXECUTION_FAILED);
            }
            List<Tensor> results = nodeResults[nodeIdx];
            if (slot < 0 || slot >= results.size()) {
                throw new NeuralException(
                        "Invalid output slot " + slot + " from node " + nodeIdx,
                        NeuralErrorCode.GRAPH_EXECUTION_FAILED);
            }
            outputMap.put(output.name(), results.get(slot));
        }

        return Collections.unmodifiableMap(outputMap);
    }

    /**
     * Execute the computation graph with the given inputs, using a TensorPool for
     * intermediate buffer management
     * 使用给定输入和 TensorPool 执行计算图，用于中间缓冲区管理
     *
     * <p>Tracks reference counts of intermediate node outputs. When all downstream
     * consumers of a node's output have executed, the backing float array is released
     * back to the pool for reuse in subsequent executions.</p>
     * <p>跟踪中间节点输出的引用计数。当某节点输出的所有下游消费者都已执行完毕后，
     * 底层浮点数组将被归还到池中，供后续执行复用。</p>
     *
     * @param inputMap map of input name to tensor | 输入名称到张量的映射
     * @param pool     tensor buffer pool for intermediate data reuse | 用于中间数据复用的张量缓冲区池
     * @return map of output name to tensor | 输出名称到张量的映射
     * @throws NeuralException if execution fails | 执行失败时抛出
     */
    public Map<String, Tensor> execute(Map<String, Tensor> inputMap, TensorPool pool) {
        Objects.requireNonNull(inputMap, "inputMap must not be null");
        Objects.requireNonNull(pool, "pool must not be null");

        // 1. Map input names to indices
        Map<String, Integer> inputNameToIndex = new HashMap<>();
        for (int i = 0; i < inputs.size(); i++) {
            inputNameToIndex.put(inputs.get(i).name(), i);
        }

        // Validate all required inputs are present
        Tensor[] graphInputTensors = new Tensor[inputs.size()];
        for (int i = 0; i < inputs.size(); i++) {
            String name = inputs.get(i).name();
            Tensor t = inputMap.get(name);
            if (t == null) {
                throw new NeuralException(
                        "Missing graph input: " + name,
                        NeuralErrorCode.GRAPH_EXECUTION_FAILED);
            }
            graphInputTensors[i] = t;
        }

        // 2. Compute reference counts for each node's output
        // A node's output is referenced by each downstream node that reads from it,
        // plus by each graph output that references it.
        int[] refCounts = new int[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            int[][] sources = nodes.get(i).inputSources();
            for (int[] source : sources) {
                int srcNodeIdx = source[0];
                if (srcNodeIdx >= 0) {
                    refCounts[srcNodeIdx]++;
                }
            }
        }

        // Collect the set of node indices that are graph outputs (should not be released)
        Set<Integer> outputNodeIndices = new HashSet<>();
        for (GraphOutput output : outputs) {
            outputNodeIndices.add(output.nodeIndex());
        }

        // Add graph output references to ref counts (so they never reach zero during execution)
        for (GraphOutput output : outputs) {
            refCounts[output.nodeIndex()]++;
        }

        // 3. Execute nodes in topological order
        @SuppressWarnings("unchecked")
        List<Tensor>[] nodeResults = new List[nodes.size()];

        for (int i = 0; i < nodes.size(); i++) {
            GraphNode node = nodes.get(i);
            int[][] sources = node.inputSources();

            // Collect dynamic inputs from graph inputs or prior node results
            List<Tensor> nodeInputs = new ArrayList<>();
            for (int[] source : sources) {
                int srcNodeIdx = source[0];
                int srcSlot = source[1];

                if (srcNodeIdx == -1) {
                    // Source is a graph input
                    if (srcSlot < 0 || srcSlot >= graphInputTensors.length) {
                        throw new NeuralException(
                                "Invalid graph input slot " + srcSlot + " for node " + node.name(),
                                NeuralErrorCode.GRAPH_EXECUTION_FAILED);
                    }
                    nodeInputs.add(graphInputTensors[srcSlot]);
                } else {
                    // Source is a prior node's output
                    if (srcNodeIdx < 0 || srcNodeIdx >= i) {
                        throw new NeuralException(
                                "Invalid source node index " + srcNodeIdx + " for node " + node.name(),
                                NeuralErrorCode.GRAPH_EXECUTION_FAILED);
                    }
                    List<Tensor> srcResults = nodeResults[srcNodeIdx];
                    if (srcSlot < 0 || srcSlot >= srcResults.size()) {
                        throw new NeuralException(
                                "Invalid output slot " + srcSlot + " from node " + srcNodeIdx
                                        + " for node " + node.name(),
                                NeuralErrorCode.GRAPH_EXECUTION_FAILED);
                    }
                    nodeInputs.add(srcResults.get(srcSlot));
                }
            }

            // Append weight tensors
            for (Tensor w : node.weights()) {
                nodeInputs.add(w);
            }

            // Execute the node
            try {
                nodeResults[i] = node.execute(nodeInputs);
            } catch (NeuralException e) {
                throw e;
            } catch (Exception e) {
                throw new NeuralException(
                        "Failed to execute node " + node.name() + ": " + e.getMessage(),
                        e,
                        NeuralErrorCode.GRAPH_EXECUTION_FAILED);
            }

            // Decrement ref counts for consumed inputs and release if zero
            for (int[] source : sources) {
                int srcNodeIdx = source[0];
                if (srcNodeIdx >= 0) {
                    refCounts[srcNodeIdx]--;
                    if (refCounts[srcNodeIdx] == 0 && !outputNodeIndices.contains(srcNodeIdx)) {
                        // Release all output tensors of the source node to the pool
                        List<Tensor> srcResults = nodeResults[srcNodeIdx];
                        if (srcResults != null) {
                            for (Tensor t : srcResults) {
                                // Return a copy to pool for reuse, then close to prevent use-after-release
                                pool.release(t.toFloatArray());
                                t.close();
                            }
                            nodeResults[srcNodeIdx] = null;
                        }
                    }
                }
            }
        }

        // 4. Collect outputs
        Map<String, Tensor> outputMap = new HashMap<>();
        for (GraphOutput output : outputs) {
            int nodeIdx = output.nodeIndex();
            int slot = output.outputSlot();
            if (nodeIdx < 0 || nodeIdx >= nodes.size()) {
                throw new NeuralException(
                        "Invalid output node index " + nodeIdx,
                        NeuralErrorCode.GRAPH_EXECUTION_FAILED);
            }
            List<Tensor> results = nodeResults[nodeIdx];
            if (results == null || slot < 0 || slot >= results.size()) {
                throw new NeuralException(
                        "Invalid output slot " + slot + " from node " + nodeIdx,
                        NeuralErrorCode.GRAPH_EXECUTION_FAILED);
            }
            outputMap.put(output.name(), results.get(slot));
        }

        return Collections.unmodifiableMap(outputMap);
    }

    @Override
    public String toString() {
        return "Graph[nodes=" + nodes.size()
                + ", inputs=" + inputs.size()
                + ", outputs=" + outputs.size() + "]";
    }
}
