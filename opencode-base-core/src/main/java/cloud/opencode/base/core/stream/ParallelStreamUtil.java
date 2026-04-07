package cloud.opencode.base.core.stream;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Parallel Stream Utility - Smart parallel/sequential stream selection
 * 并行流工具类 - 智能选择并行/顺序流
 *
 * <p>Provides utilities for intelligent parallel stream usage based on collection size.</p>
 * <p>提供基于集合大小智能选择并行流使用的工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Smart parallel/sequential selection by threshold - 按阈值智能选择并行/顺序流</li>
 *   <li>CPU-aware threshold calculation - CPU 感知的阈值计算</li>
 *   <li>Force parallel/sequential methods - 强制并行/顺序流方法</li>
 *   <li>Parallelism recommendations - 并行推荐</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Smart stream selection - 智能流选择
 * Stream<String> stream = ParallelStreamUtil.stream(list);
 *
 * // Check if parallel recommended - 检查是否推荐并行
 * boolean parallel = ParallelStreamUtil.isParallelRecommended(list.size());
 *
 * // Force parallel - 强制并行
 * Stream<String> parallel = ParallelStreamUtil.parallelStream(list);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Yes (returns empty stream on null) - 空值安全: 是</li>
 * </ul>
 *
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n/p) where n = elements, p = parallelism - O(n/p), n为元素数, p为并行度</li>
 *   <li>Space complexity: O(n) for stream pipeline - 流管道 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class ParallelStreamUtil {

    /**
     * Default parallel threshold, configurable via system property opencode.parallel.stream.threshold
     * 默认并行阈值，可通过系统属性 opencode.parallel.stream.threshold 配置
     */
    private static final int DEFAULT_THRESHOLD = Integer.getInteger("opencode.parallel.stream.threshold", 1000);

    /**
     * Available processor count
     * 可用处理器数量
     */
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private ParallelStreamUtil() {
    }

    /**
     * Gets the recommended parallel threshold
     * 获取推荐的并行阈值
     * <p>
     * 基于 CPU 核心数计算，通常为每核心 1000 个元素
     * </p>
     */
    public static int getRecommendedThreshold() {
        return (int) Math.min((long) DEFAULT_THRESHOLD * AVAILABLE_PROCESSORS, Integer.MAX_VALUE);
    }

    /**
     * Selects sequential or parallel stream based on collection size
     * 根据集合大小智能选择顺序/并行流
     */
    public static <T> Stream<T> stream(Collection<T> collection) {
        return stream(collection, DEFAULT_THRESHOLD);
    }

    /**
     * Selects sequential or parallel stream based on collection size (custom threshold)
     * 根据集合大小智能选择顺序/并行流（自定义阈值）
     */
    public static <T> Stream<T> stream(Collection<T> collection, int threshold) {
        if (collection == null) {
            return Stream.empty();
        }
        if (collection.size() >= threshold && AVAILABLE_PROCESSORS > 1) {
            return collection.parallelStream();
        }
        return collection.stream();
    }

    /**
     * Checks if parallel stream is recommended
     * 检查是否推荐使用并行流
     */
    public static boolean isParallelRecommended(int size) {
        return isParallelRecommended(size, DEFAULT_THRESHOLD);
    }

    /**
     * Checks if parallel stream is recommended (custom threshold)
     * 检查是否推荐使用并行流（自定义阈值）
     */
    public static boolean isParallelRecommended(int size, int threshold) {
        return size >= threshold && AVAILABLE_PROCESSORS > 1;
    }

    /**
     * Gets the available processor count
     * 获取可用处理器数量
     */
    public static int getAvailableProcessors() {
        return AVAILABLE_PROCESSORS;
    }

    /**
     * Gets the ForkJoinPool parallelism
     * 获取 ForkJoinPool 并行度
     */
    public static int getParallelism() {
        return java.util.concurrent.ForkJoinPool.commonPool().getParallelism();
    }

    /**
     * Forces a sequential stream
     * 强制使用顺序流
     */
    public static <T> Stream<T> sequentialStream(Collection<T> collection) {
        return collection != null ? collection.stream() : Stream.empty();
    }

    /**
     * Forces a parallel stream
     * 强制使用并行流
     */
    public static <T> Stream<T> parallelStream(Collection<T> collection) {
        return collection != null ? collection.parallelStream() : Stream.empty();
    }

    /**
     * Converts the stream to parallel if the condition is met
     * 将流转为并行流（如果条件满足）
     */
    public static <T> Stream<T> toParallelIf(Stream<T> stream, boolean condition) {
        return condition && AVAILABLE_PROCESSORS > 1 ? stream.parallel() : stream.sequential();
    }

    /**
     * Converts the stream to sequential
     * 将流转为顺序流
     */
    public static <T> Stream<T> toSequential(Stream<T> stream) {
        return stream.sequential();
    }
}
