package cloud.opencode.base.neural.session;

import java.util.List;

/**
 * Profiling Result for Inference Session
 * 推理会话性能分析结果
 *
 * <p>Immutable record containing per-operator timing information collected
 * during a profiled inference run. Use {@link #totalTimeMillis()} for a
 * quick overview or {@link #summary()} for a human-readable report.</p>
 * <p>不可变记录，包含在性能分析推理运行期间收集的每个算子的时间信息。
 * 使用 {@link #totalTimeMillis()} 进行快速概览，
 * 或使用 {@link #summary()} 获取人类可读的报告。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param opTimings     per-operator timing entries | 每个算子的计时条目
 * @param totalTimeNanos total inference time in nanoseconds | 总推理时间（纳秒）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see InferenceSession
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public record ProfilingResult(List<OpTiming> opTimings, long totalTimeNanos) {

    /**
     * Creates a ProfilingResult with defensive copy of the timing list
     * 创建 ProfilingResult，对时间列表进行防御性拷贝
     *
     * @param opTimings      per-operator timing entries | 每个算子的计时条目
     * @param totalTimeNanos total inference time in nanoseconds | 总推理时间（纳秒）
     */
    public ProfilingResult {
        opTimings = List.copyOf(opTimings);
        if (totalTimeNanos < 0) {
            throw new IllegalArgumentException("totalTimeNanos must be >= 0, got: " + totalTimeNanos);
        }
    }

    /**
     * Per-operator Timing Entry
     * 单个算子的计时条目
     *
     * @param nodeName name of the graph node | 计算图节点名称
     * @param opType   operator type (e.g. "Conv2D", "MatMul") | 算子类型
     * @param timeNanos execution time in nanoseconds | 执行时间（纳秒）
     * @author Leon Soo
     * @since JDK 25, opencode-base-neural V1.0.0
     */
    public record OpTiming(String nodeName, String opType, long timeNanos) {

        /**
         * Get execution time in milliseconds
         * 获取执行时间（毫秒）
         *
         * @return time in milliseconds | 时间（毫秒）
         */
        public double timeMillis() {
            return timeNanos / 1_000_000.0;
        }
    }

    /**
     * Get total inference time in milliseconds
     * 获取总推理时间（毫秒）
     *
     * @return total time in milliseconds | 总时间（毫秒）
     */
    public double totalTimeMillis() {
        return totalTimeNanos / 1_000_000.0;
    }

    /**
     * Get a human-readable profiling summary
     * 获取人类可读的性能分析摘要
     *
     * <p>The summary includes total time, operator count, and per-operator breakdown
     * sorted by execution time descending.</p>
     * <p>摘要包括总时间、算子数量以及按执行时间降序排列的各算子明细。</p>
     *
     * @return formatted summary string | 格式化的摘要字符串
     */
    public String summary() {
        var sb = new StringBuilder();
        sb.append("Profiling Summary\n");
        sb.append("=================\n");
        sb.append(String.format("Total time: %.3f ms%n", totalTimeMillis()));
        sb.append(String.format("Operators:  %d%n", opTimings.size()));
        sb.append("-----------------\n");

        // Sort by time descending
        opTimings.stream()
                .sorted((a, b) -> Long.compare(b.timeNanos(), a.timeNanos()))
                .forEach(op -> sb.append(String.format(
                        "  %-30s %-12s %8.3f ms%n",
                        op.nodeName(), op.opType(), op.timeMillis())));

        return sb.toString();
    }
}
