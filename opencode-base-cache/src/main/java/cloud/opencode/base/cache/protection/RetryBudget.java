package cloud.opencode.base.cache.protection;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Retry Budget — Limits the fraction of requests that can be retried.
 * 重试预算 — 限制可重试请求的比例。
 *
 * <p>Prevents retry amplification in microservices by ensuring that retries
 * never exceed a configurable fraction of total requests (default: 20%).</p>
 * <p>通过确保重试不超过总请求的可配置比例（默认：20%），防止微服务中的重试放大。</p>
 *
 * <p>Uses a simple ratio gate: each original request increments a total counter;
 * each retry is allowed only when {@code retries/requests < retryRatio}.</p>
 * <p>使用简单的比例门控：每个原始请求递增总计数器；
 * 仅当 {@code retries/requests < retryRatio} 时才允许重试。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * RetryBudget budget = RetryBudget.ofRatio(0.20); // max 20% retries
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Ratio-based retry limiting - 基于比率的重试限制</li>
 *   <li>Atomic counter tracking - 原子计数器跟踪</li>
 *   <li>Retry amplification prevention - 重试放大防护</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RetryBudget budget = RetryBudget.ofRatio(0.20);
 * budget.recordRequest();
 * if (budget.tryAcquireRetry()) {
 *     // retry allowed
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses AtomicLong) - 线程安全: 是（使用 AtomicLong）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class RetryBudget {

    /** Maximum fraction of requests that can be retries (e.g. 0.20 = 20%). */
    private final double retryRatio;

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalRetries = new AtomicLong(0);

    private RetryBudget(double retryRatio) {
        if (retryRatio <= 0 || retryRatio >= 1) {
            throw new IllegalArgumentException(
                    "Retry ratio must be between 0 (exclusive) and 1 (exclusive), got: " + retryRatio);
        }
        this.retryRatio = retryRatio;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a retry budget with the given ratio.
     * 创建具有给定比例的重试预算。
     *
     * @param retryRatio the maximum fraction of retries (0 exclusive, 1 exclusive),
     *                   e.g. {@code 0.20} = at most 20% of requests may be retried
     *                   - 最大重试比例（不含 0 和 1），例如 {@code 0.20} = 最多 20% 的请求可重试
     * @return the retry budget - 重试预算
     */
    public static RetryBudget ofRatio(double retryRatio) {
        return new RetryBudget(retryRatio);
    }

    /**
     * Creates a retry budget with the given percentage (1–99).
     * 创建具有给定百分比的重试预算（1–99）。
     *
     * @param percent the percentage of requests that may be retried (1–99)
     *                - 可重试请求的百分比（1–99）
     * @return the retry budget - 重试预算
     */
    public static RetryBudget ofPercent(int percent) {
        if (percent <= 0 || percent >= 100) {
            throw new IllegalArgumentException(
                    "Retry percent must be between 1 and 99 (inclusive), got: " + percent);
        }
        return new RetryBudget(percent / 100.0);
    }

    /**
     * Creates a retry budget that effectively allows unlimited retries (99.99% ratio).
     * Useful for testing or scenarios where budgeting is not desired.
     * 创建实际上允许无限重试的重试预算（99.99% 比例）。
     * 适用于测试或不需要预算控制的场景。
     *
     * @return an unlimited retry budget - 无限重试预算
     */
    public static RetryBudget unlimited() {
        return new RetryBudget(0.9999);
    }

    // ==================== Budget Operations | 预算操作 ====================

    /**
     * Records an original (non-retry) request.
     * Should be called before each original request attempt.
     * 记录一个原始（非重试）请求。
     * 应在每个原始请求尝试之前调用。
     */
    public void recordRequest() {
        totalRequests.incrementAndGet();
    }

    /**
     * Returns true if a retry is allowed given the current budget state.
     * 如果当前预算状态允许重试，则返回 true。
     *
     * @return true if a retry is allowed - 如果允许重试返回 true
     */
    public boolean canRetry() {
        long requests = totalRequests.get();
        if (requests == 0) {
            return true;
        }
        long retries = totalRetries.get();
        return (double) retries / requests < retryRatio;
    }

    /**
     * Records that a retry is about to be attempted.
     * Should be called just before making a retry attempt.
     * 记录即将尝试重试。
     * 应在进行重试尝试之前调用。
     */
    public void recordRetry() {
        totalRetries.incrementAndGet();
    }

    // ==================== Metrics | 指标 ====================

    /**
     * Returns the current retry ratio (retries / total requests).
     * Returns 0.0 if no requests have been recorded.
     * 返回当前重试比率（重试次数 / 总请求数）。
     * 如果没有记录请求，则返回 0.0。
     *
     * @return the current retry ratio - 当前重试比率
     */
    public double currentRetryRatio() {
        long requests = totalRequests.get();
        return requests == 0 ? 0.0 : (double) totalRetries.get() / requests;
    }

    /**
     * Returns the configured maximum retry ratio.
     * 返回配置的最大重试比率。
     *
     * @return the max retry ratio - 最大重试比率
     */
    public double getRetryRatio() {
        return retryRatio;
    }

    /**
     * Returns the total number of original requests recorded.
     * 返回已记录的原始请求总数。
     *
     * @return the total request count - 总请求数
     */
    public long getTotalRequests() {
        return totalRequests.get();
    }

    /**
     * Returns the total number of retries recorded.
     * 返回已记录的重试总数。
     *
     * @return the total retry count - 总重试数
     */
    public long getTotalRetries() {
        return totalRetries.get();
    }

    @Override
    public String toString() {
        return "RetryBudget{ratio=" + retryRatio
                + ", current=" + String.format("%.4f", currentRetryRatio())
                + ", requests=" + totalRequests.get()
                + ", retries=" + totalRetries.get() + "}";
    }
}
