package cloud.opencode.base.neural.internal;

/**
 * Workspace Pre-allocator
 * 工作区预分配器
 *
 * <p>A simple bump allocator that pre-allocates a contiguous float array as workspace
 * memory for neural network inference. Operators allocate regions from this workspace
 * by bumping the offset, and the entire workspace is reset between inference calls.</p>
 * <p>一个简单的碰撞分配器，预分配一个连续的浮点数组作为神经网络推理的工作区内存。
 * 算子通过推进偏移量从工作区分配区域，整个工作区在推理调用之间重置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>O(1) allocation via offset bumping - 通过偏移量推进实现 O(1) 分配</li>
 *   <li>Zero-cost reset for reuse between inference calls - 推理调用之间零开销重置</li>
 *   <li>Single contiguous backing array for cache locality - 单个连续底层数组保证缓存局部性</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (designed for single-threaded per-inference usage) -
 *       线程安全: 否（设计为每次推理单线程使用）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class WorkspaceAllocator {

    private final float[] workspace;
    private int offset;

    /**
     * Create a WorkspaceAllocator with the specified total capacity.
     * 创建指定总容量的工作区分配器。
     *
     * @param totalSize the total number of floats in the workspace | 工作区中浮点数的总数
     * @throws IllegalArgumentException if totalSize is negative | 如果 totalSize 为负数
     */
    public WorkspaceAllocator(int totalSize) {
        if (totalSize < 0) {
            throw new IllegalArgumentException("totalSize must not be negative: " + totalSize);
        }
        this.workspace = new float[totalSize];
        this.offset = 0;
    }

    /**
     * Allocate a region of the workspace and return its starting offset.
     * 分配工作区的一个区域并返回其起始偏移量。
     *
     * <p>The returned offset can be used with {@link #data()} to access the allocated region:
     * {@code float[] ws = allocator.data(); int off = allocator.allocate(100);}</p>
     * <p>返回的偏移量可与 {@link #data()} 一起使用来访问分配的区域。</p>
     *
     * @param size the number of floats to allocate | 要分配的浮点数数量
     * @return the starting offset of the allocated region | 分配区域的起始偏移量
     * @throws IllegalArgumentException if size is negative | 如果 size 为负数
     * @throws IllegalStateException    if there is insufficient capacity | 如果容量不足
     */
    public int allocate(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative: " + size);
        }
        if ((long) offset + size > workspace.length) {
            throw new IllegalStateException(
                    "Workspace overflow: requested " + size + ", available " + (workspace.length - offset));
        }
        int result = offset;
        offset += size;
        return result;
    }

    /**
     * Reset the offset to 0 for reuse between inference calls.
     * 将偏移量重置为 0 以便在推理调用之间复用。
     *
     * <p>Note: this does not zero the backing array for performance reasons.</p>
     * <p>注意：出于性能考虑，不会清零底层数组。</p>
     */
    public void reset() {
        offset = 0;
    }

    /**
     * Get the backing workspace array.
     * 获取底层工作区数组。
     *
     * @return the backing float array | 底层浮点数组
     */
    public float[] data() {
        return workspace;
    }

    /**
     * Get the total capacity of the workspace.
     * 获取工作区的总容量。
     *
     * @return the total number of floats the workspace can hold | 工作区可容纳的浮点数总数
     */
    public int capacity() {
        return workspace.length;
    }

    /**
     * Get the current used size of the workspace.
     * 获取工作区的当前已使用大小。
     *
     * @return the number of floats currently allocated | 当前已分配的浮点数数量
     */
    public int used() {
        return offset;
    }
}
