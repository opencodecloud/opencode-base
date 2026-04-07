package cloud.opencode.base.neural.internal;

/**
 * Reference Counter for Computation Graph Nodes
 * 计算图节点引用计数器
 *
 * <p>Tracks reference counts for intermediate tensors in a computation graph.
 * When a node's reference count reaches zero, its backing float array is
 * automatically released back to the {@link TensorPool} for reuse.</p>
 * <p>跟踪计算图中中间张量的引用计数。当节点的引用计数达到零时，
 * 其底层浮点数组自动释放回 {@link TensorPool} 以供复用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Per-node reference counting for automatic memory reclamation - 按节点引用计数实现自动内存回收</li>
 *   <li>Integration with TensorPool for buffer reuse - 与 TensorPool 集成实现缓冲区复用</li>
 *   <li>Bounds-checked node index access - 有边界检查的节点索引访问</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (designed for single-threaded graph execution) -
 *       线程安全: 否（设计为单线程图执行）</li>
 *   <li>Prevents double-free via underflow detection - 通过下溢检测防止双重释放</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see TensorPool
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class RefCounter {

    private final int[] counts;
    private final TensorPool pool;

    /**
     * Create a RefCounter for the given number of graph nodes.
     * 为指定数量的图节点创建引用计数器。
     *
     * @param nodeCount the number of nodes in the computation graph | 计算图中的节点数量
     * @param pool      the tensor pool for releasing buffers | 用于释放缓冲区的张量池
     * @throws IllegalArgumentException if nodeCount is negative or pool is null | 如果 nodeCount 为负数或 pool 为 null
     */
    public RefCounter(int nodeCount, TensorPool pool) {
        if (nodeCount < 0) {
            throw new IllegalArgumentException("nodeCount must not be negative: " + nodeCount);
        }
        if (pool == null) {
            throw new IllegalArgumentException("pool must not be null");
        }
        this.counts = new int[nodeCount];
        this.pool = pool;
    }

    /**
     * Initialize the reference count for a node.
     * 初始化节点的引用计数。
     *
     * @param nodeIndex the index of the node | 节点索引
     * @param refCount  the initial reference count | 初始引用计数
     * @throws IndexOutOfBoundsException if nodeIndex is out of range | 如果 nodeIndex 超出范围
     * @throws IllegalArgumentException  if refCount is negative | 如果 refCount 为负数
     */
    public void init(int nodeIndex, int refCount) {
        checkIndex(nodeIndex);
        if (refCount < 0) {
            throw new IllegalArgumentException("refCount must not be negative: " + refCount);
        }
        counts[nodeIndex] = refCount;
    }

    /**
     * Decrement the reference count for a node.
     * 递减节点的引用计数。
     *
     * <p>If the count reaches zero, the provided data array is released back to the
     * {@link TensorPool}. If the count is already zero, an {@link IllegalStateException}
     * is thrown to detect double-free bugs.</p>
     * <p>如果计数达到零，提供的数据数组将释放回 {@link TensorPool}。
     * 如果计数已经为零，则抛出 {@link IllegalStateException} 以检测双重释放错误。</p>
     *
     * @param nodeIndex the index of the node | 节点索引
     * @param data      the tensor data to release if count reaches 0 | 计数达到 0 时要释放的张量数据
     * @throws IndexOutOfBoundsException if nodeIndex is out of range | 如果 nodeIndex 超出范围
     * @throws IllegalStateException     if reference count is already zero | 如果引用计数已经为零
     */
    public void decrement(int nodeIndex, float[] data) {
        checkIndex(nodeIndex);
        if (counts[nodeIndex] <= 0) {
            throw new IllegalStateException(
                    "Reference count already zero for node " + nodeIndex);
        }
        counts[nodeIndex]--;
        if (counts[nodeIndex] == 0 && data != null) {
            pool.release(data);
        }
    }

    /**
     * Get the current reference count for a node.
     * 获取节点的当前引用计数。
     *
     * @param nodeIndex the index of the node | 节点索引
     * @return the current reference count | 当前引用计数
     * @throws IndexOutOfBoundsException if nodeIndex is out of range | 如果 nodeIndex 超出范围
     */
    public int get(int nodeIndex) {
        checkIndex(nodeIndex);
        return counts[nodeIndex];
    }

    /**
     * Check that the node index is within valid range.
     * 检查节点索引是否在有效范围内。
     */
    private void checkIndex(int nodeIndex) {
        if (nodeIndex < 0 || nodeIndex >= counts.length) {
            throw new IndexOutOfBoundsException(
                    "Node index " + nodeIndex + " out of range [0, " + counts.length + ")");
        }
    }
}
