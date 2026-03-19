package cloud.opencode.base.id.segment;

/**
 * Segment Allocator Interface
 * 号段分配器接口
 *
 * <p>Interface for allocating ID segments from a central source.
 * Implementations should handle concurrent access and persistence.</p>
 * <p>从中心源分配ID号段的接口。实现应处理并发访问和持久化。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Segment allocation - 号段分配</li>
 *   <li>Business tag support - 业务标识支持</li>
 *   <li>Configurable step size - 可配置步长</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SegmentAllocator allocator = JdbcSegmentAllocator.create(dataSource);
 * Segment segment = allocator.allocate("order");
 *
 * for (long i = segment.start(); i < segment.end(); i++) {
 *     // Use i as ID
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public interface SegmentAllocator {

    /**
     * Default step size
     * 默认步长
     */
    int DEFAULT_STEP = 1000;

    /**
     * Allocates a segment for the given business tag
     * 为给定业务标识分配号段
     *
     * @param bizTag the business tag | 业务标识
     * @return allocated segment | 分配的号段
     */
    Segment allocate(String bizTag);

    /**
     * Gets the step size
     * 获取步长
     *
     * @return step size | 步长
     */
    int getStep();

    /**
     * Segment definition
     * 号段定义
     */
    final class Segment {

        private final long start;
        private final long end;
        private final int step;

        /**
         * Creates a segment
         * 创建号段
         *
         * @param start the start value (inclusive) | 起始值（包含）
         * @param end   the end value (exclusive) | 结束值（不包含）
         * @param step  the step size | 步长
         * @throws IllegalArgumentException if start is negative, end &lt; start, or step &lt;= 0
         */
        public Segment(long start, long end, int step) {
            if (start < 0) {
                throw new IllegalArgumentException("Segment start must be non-negative: " + start);
            }
            if (end < start) {
                throw new IllegalArgumentException("Segment end must be >= start: end=" + end + ", start=" + start);
            }
            if (step <= 0) {
                throw new IllegalArgumentException("Segment step must be positive: " + step);
            }
            this.start = start;
            this.end = end;
            this.step = step;
        }

        /**
         * Gets the start value
         * 获取起始值
         *
         * @return start value | 起始值
         */
        public long start() {
            return start;
        }

        /**
         * Gets the end value
         * 获取结束值
         *
         * @return end value | 结束值
         */
        public long end() {
            return end;
        }

        /**
         * Gets the step size
         * 获取步长
         *
         * @return step size | 步长
         */
        public int step() {
            return step;
        }

        /**
         * Gets the maximum value (same as end)
         * 获取最大值（与end相同）
         *
         * @return max value | 最大值
         */
        public long getMaxValue() {
            return end;
        }

        /**
         * Gets the current value (same as start)
         * 获取当前值（与start相同）
         *
         * @return current value | 当前值
         */
        public long getCurrentValue() {
            return start;
        }

        /**
         * Gets the remaining count
         * 获取剩余数量
         *
         * @return remaining count | 剩余数量
         */
        public long remaining() {
            return end - start;
        }

        @Override
        public String toString() {
            return String.format("Segment{start=%d, end=%d, step=%d}", start, end, step);
        }
    }
}
