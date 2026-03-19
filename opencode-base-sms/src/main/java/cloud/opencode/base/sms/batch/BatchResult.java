package cloud.opencode.base.sms.batch;

import cloud.opencode.base.sms.message.SmsResult;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Batch Result
 * 批量发送结果
 *
 * <p>Aggregated result of batch SMS sending.</p>
 * <p>批量短信发送的聚合结果。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Success/failure counts and timing - 成功/失败计数和耗时</li>
 *   <li>Individual result access - 单条结果访问</li>
 *   <li>Immutable result record - 不可变结果记录</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BatchResult result = BatchResult.of(resultList, startTime);
 * boolean allOk = result.isAllSuccess();
 * int failures = result.failureCount();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param results individual results | 单条结果列表
 * @param totalCount total message count | 总消息数
 * @param successCount success count | 成功数
 * @param failureCount failure count | 失败数
 * @param startTime batch start time | 批次开始时间
 * @param endTime batch end time | 批次结束时间
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public record BatchResult(
    List<SmsResult> results,
    int totalCount,
    int successCount,
    int failureCount,
    Instant startTime,
    Instant endTime
) {
    public BatchResult {
        results = results != null ? List.copyOf(results) : List.of();
    }

    /**
     * Create batch result from results
     * 从结果列表创建批量结果
     *
     * @param results the results | 结果列表
     * @param startTime the start time | 开始时间
     * @return the batch result | 批量结果
     */
    public static BatchResult of(List<SmsResult> results, Instant startTime) {
        int total = results.size();
        int success = (int) results.stream().filter(SmsResult::success).count();
        int failure = total - success;
        return new BatchResult(results, total, success, failure, startTime, Instant.now());
    }

    /**
     * Create empty batch result
     * 创建空批量结果
     *
     * @return the empty result | 空结果
     */
    public static BatchResult empty() {
        Instant now = Instant.now();
        return new BatchResult(List.of(), 0, 0, 0, now, now);
    }

    /**
     * Check if all succeeded
     * 检查是否全部成功
     *
     * @return true if all succeeded | 如果全部成功返回true
     */
    public boolean isAllSuccess() {
        return failureCount == 0 && totalCount > 0;
    }

    /**
     * Check if all failed
     * 检查是否全部失败
     *
     * @return true if all failed | 如果全部失败返回true
     */
    public boolean isAllFailed() {
        return successCount == 0 && totalCount > 0;
    }

    /**
     * Check if partial success
     * 检查是否部分成功
     *
     * @return true if partial | 如果部分成功返回true
     */
    public boolean isPartialSuccess() {
        return successCount > 0 && failureCount > 0;
    }

    /**
     * Get success rate
     * 获取成功率
     *
     * @return the success rate (0.0 - 1.0) | 成功率
     */
    public double getSuccessRate() {
        if (totalCount == 0) {
            return 0.0;
        }
        return (double) successCount / totalCount;
    }

    /**
     * Get duration
     * 获取耗时
     *
     * @return the duration | 耗时
     */
    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }

    /**
     * Get success results
     * 获取成功结果
     *
     * @return the success results | 成功结果
     */
    public List<SmsResult> getSuccessResults() {
        return results.stream()
            .filter(SmsResult::success)
            .toList();
    }

    /**
     * Get failure results
     * 获取失败结果
     *
     * @return the failure results | 失败结果
     */
    public List<SmsResult> getFailureResults() {
        return results.stream()
            .filter(r -> !r.success())
            .toList();
    }

    /**
     * Get failure counts by error code
     * 按错误码获取失败统计
     *
     * @return the error code counts | 错误码统计
     */
    public Map<String, Long> getFailureCountsByCode() {
        return results.stream()
            .filter(r -> !r.success())
            .filter(r -> r.errorCode() != null)
            .collect(Collectors.groupingBy(
                SmsResult::errorCode,
                Collectors.counting()
            ));
    }

    /**
     * Get throughput (messages per second)
     * 获取吞吐量（每秒消息数）
     *
     * @return the throughput | 吞吐量
     */
    public double getThroughput() {
        long durationMs = getDuration().toMillis();
        if (durationMs == 0) {
            return 0.0;
        }
        return (double) totalCount / durationMs * 1000;
    }

    @Override
    public String toString() {
        return String.format(
            "BatchResult{total=%d, success=%d, failure=%d, rate=%.1f%%, duration=%dms}",
            totalCount, successCount, failureCount,
            getSuccessRate() * 100, getDuration().toMillis()
        );
    }
}
