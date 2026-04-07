package cloud.opencode.base.neural.model;

import cloud.opencode.base.neural.op.Op;
import cloud.opencode.base.neural.op.OpAttribute;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Computation Graph Node
 * 计算图节点
 *
 * <p>Represents a single node in the computation graph. Each node holds a reference
 * to an {@link Op} operator, its attributes, input source mappings, and optional
 * weight tensors. Nodes are executed in topological order by the {@link Graph}.</p>
 * <p>表示计算图中的单个节点。每个节点持有对 {@link Op} 算子的引用、
 * 算子属性、输入源映射和可选的权重张量。节点由 {@link Graph} 按拓扑顺序执行。</p>
 *
 * <p><strong>Input Source Format | 输入源格式:</strong></p>
 * <ul>
 *   <li>{@code inputSources[i][0]}: source node index (-1 means graph input) -
 *       源节点索引（-1 表示计算图输入）</li>
 *   <li>{@code inputSources[i][1]}: output slot from the source node or graph input index -
 *       源节点的输出槽位或计算图输入索引</li>
 * </ul>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (holds mutable Tensor references) -
 *       线程安全: 否（持有可变 Tensor 引用）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Graph
 * @see Op
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class GraphNode {

    private final String name;
    private final String opType;
    private final Op op;
    private final OpAttribute attrs;
    private final int[][] inputSources;
    private final Tensor[] weights;

    /**
     * Create a new graph node
     * 创建新的计算图节点
     *
     * @param name         node name | 节点名称
     * @param opType       operator type name (e.g. "Linear", "ReLU") | 算子类型名称
     * @param op           operator instance | 算子实例
     * @param attrs        operator attributes | 算子属性
     * @param inputSources input source mappings [inputIdx][0=nodeIdx, 1=slot] | 输入源映射
     * @param weights      weight tensors (may be empty) | 权重张量（可为空数组）
     */
    public GraphNode(String name, String opType, Op op, OpAttribute attrs,
                     int[][] inputSources, Tensor[] weights) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.opType = Objects.requireNonNull(opType, "opType must not be null");
        this.op = Objects.requireNonNull(op, "op must not be null");
        this.attrs = Objects.requireNonNull(attrs, "attrs must not be null");
        Objects.requireNonNull(inputSources, "inputSources must not be null");
        Objects.requireNonNull(weights, "weights must not be null");
        // Defensive copies
        this.inputSources = new int[inputSources.length][];
        for (int i = 0; i < inputSources.length; i++) {
            if (inputSources[i] == null || inputSources[i].length != 2) {
                throw new IllegalArgumentException(
                        "inputSources[" + i + "] must be a 2-element array [nodeIdx, slot]");
            }
            this.inputSources[i] = inputSources[i].clone();
        }
        this.weights = weights.clone();
    }

    /**
     * Get the node name
     * 获取节点名称
     *
     * @return node name | 节点名称
     */
    public String name() {
        return name;
    }

    /**
     * Get the operator type name
     * 获取算子类型名称
     *
     * @return operator type | 算子类型
     */
    public String opType() {
        return opType;
    }

    /**
     * Get the operator instance
     * 获取算子实例
     *
     * @return op instance | 算子实例
     */
    public Op op() {
        return op;
    }

    /**
     * Get the operator attributes
     * 获取算子属性
     *
     * @return operator attributes | 算子属性
     */
    public OpAttribute attrs() {
        return attrs;
    }

    /**
     * Get defensive copy of input source mappings
     * 获取输入源映射的防御性拷贝
     *
     * @return input sources array | 输入源数组
     */
    public int[][] inputSources() {
        int[][] copy = new int[inputSources.length][];
        for (int i = 0; i < inputSources.length; i++) {
            copy[i] = inputSources[i].clone();
        }
        return copy;
    }

    /**
     * Get defensive copy of weight tensors
     * 获取权重张量的防御性拷贝
     *
     * @return weight tensors array | 权重张量数组
     */
    public Tensor[] weights() {
        return weights.clone();
    }

    /**
     * Execute this node's operator with the given inputs
     * 使用给定输入执行此节点的算子
     *
     * @param inputs ordered input tensors | 有序输入张量
     * @return ordered output tensors | 有序输出张量
     */
    public List<Tensor> execute(List<Tensor> inputs) {
        return op.forward(inputs, attrs);
    }

    @Override
    public String toString() {
        return "GraphNode[name=" + name + ", opType=" + opType
                + ", inputs=" + inputSources.length
                + ", weights=" + weights.length + "]";
    }
}
