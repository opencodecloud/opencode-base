package cloud.opencode.base.neural.model;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * OCM Model
 * OCM 模型
 *
 * <p>Represents a loaded OpenCode Model (.ocm) containing a computation graph,
 * model metadata, and input/output tensor information. Implements {@link AutoCloseable}
 * to support explicit resource management of weight tensors.</p>
 * <p>表示已加载的 OpenCode 模型（.ocm），包含计算图、
 * 模型元数据和输入/输出张量信息。实现 {@link AutoCloseable}
 * 以支持权重张量的显式资源管理。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (Graph execution is not thread-safe) -
 *       线程安全: 否（计算图执行非线程安全）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Graph
 * @see OcmLoader
 * @see OcmWriter
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class OcmModel implements AutoCloseable {

    private final Graph graph;
    private final ModelMetadata metadata;
    private final List<TensorInfo> inputInfos;
    private final List<TensorInfo> outputInfos;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Create a new OCM model
     * 创建新的 OCM 模型
     *
     * @param graph       the computation graph | 计算图
     * @param metadata    model metadata | 模型元数据
     * @param inputInfos  input tensor information | 输入张量信息
     * @param outputInfos output tensor information | 输出张量信息
     */
    public OcmModel(Graph graph, ModelMetadata metadata,
                    List<TensorInfo> inputInfos, List<TensorInfo> outputInfos) {
        this.graph = Objects.requireNonNull(graph, "graph must not be null");
        this.metadata = Objects.requireNonNull(metadata, "metadata must not be null");
        this.inputInfos = List.copyOf(Objects.requireNonNull(inputInfos, "inputInfos must not be null"));
        this.outputInfos = List.copyOf(Objects.requireNonNull(outputInfos, "outputInfos must not be null"));
    }

    /**
     * Get the computation graph
     * 获取计算图
     *
     * @return the graph | 计算图
     */
    public Graph graph() {
        return graph;
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
     * Get the input tensor information
     * 获取输入张量信息
     *
     * @return unmodifiable list of input tensor info | 不可修改的输入张量信息列表
     */
    public List<TensorInfo> inputInfo() {
        return inputInfos;
    }

    /**
     * Get the output tensor information
     * 获取输出张量信息
     *
     * @return unmodifiable list of output tensor info | 不可修改的输出张量信息列表
     */
    public List<TensorInfo> outputInfo() {
        return outputInfos;
    }

    /**
     * Close this model, releasing weight tensor resources
     * 关闭此模型，释放权重张量资源
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        // Close all weight tensors in graph nodes
        for (GraphNode node : graph.nodes()) {
            for (var w : node.weights()) {
                if (w != null) {
                    w.close();
                }
            }
        }
    }

    /**
     * Check if this model has been closed
     * 检查此模型是否已关闭
     *
     * @return true if closed | 如果已关闭则返回 true
     */
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public String toString() {
        return "OcmModel[name=" + metadata.name()
                + ", inputs=" + inputInfos.size()
                + ", outputs=" + outputInfos.size()
                + ", nodes=" + graph.nodes().size() + "]";
    }
}
