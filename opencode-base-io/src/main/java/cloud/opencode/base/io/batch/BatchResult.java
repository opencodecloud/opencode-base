package cloud.opencode.base.io.batch;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Batch Operation Result
 * 批量操作结果
 *
 * <p>Immutable record containing the results of a batch file operation.</p>
 * <p>包含批量文件操作结果的不可变记录。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable result of batch file operations - 批量文件操作的不可变结果</li>
 *   <li>Success/failure/skipped counting - 成功/失败/跳过计数</li>
 *   <li>Duration and throughput calculation - 持续时间和吞吐量计算</li>
 *   <li>Failure details with path-to-exception mapping - 失败详情（路径到异常映射）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BatchResult result = OpenBatch.copyAll(files, targetDir);
 * if (result.isAllSuccess()) {
 *     System.out.println("All " + result.totalCount() + " files copied");
 * } else {
 *     result.failures().forEach((path, ex) -> System.err.println(path + ": " + ex));
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No, constructor parameters must not be null - 空值安全: 否，构造器参数不可为null</li>
 * </ul>
 *
 * @param operation the operation type | 操作类型
 * @param totalCount total number of items | 总项目数
 * @param successCount successful items | 成功项目数
 * @param failureCount failed items | 失败项目数
 * @param skippedCount skipped items | 跳过项目数
 * @param failures map of failed paths to exceptions | 失败路径到异常的映射
 * @param startTime operation start time | 操作开始时间
 * @param endTime operation end time | 操作结束时间
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public record BatchResult(
    String operation,
    int totalCount,
    int successCount,
    int failureCount,
    int skippedCount,
    Map<Path, Throwable> failures,
    Instant startTime,
    Instant endTime
) {

    /**
     * Check if all operations succeeded
     * 检查是否全部成功
     *
     * @return true if no failures | 如果没有失败返回 true
     */
    public boolean isAllSuccess() {
        return failureCount == 0;
    }

    /**
     * Check if any operation failed
     * 检查是否有失败
     *
     * @return true if any failure | 如果有失败返回 true
     */
    public boolean hasFailures() {
        return failureCount > 0;
    }

    /**
     * Check if operation was partially successful
     * 检查是否部分成功
     *
     * @return true if some succeeded and some failed | 如果部分成功部分失败返回 true
     */
    public boolean isPartialSuccess() {
        return successCount > 0 && failureCount > 0;
    }

    /**
     * Get operation duration
     * 获取操作耗时
     *
     * @return duration | 耗时
     */
    public Duration duration() {
        return Duration.between(startTime, endTime);
    }

    /**
     * Get success rate
     * 获取成功率
     *
     * @return success rate (0.0 to 1.0) | 成功率（0.0 到 1.0）
     */
    public double successRate() {
        if (totalCount == 0) {
            return 1.0;
        }
        return (double) successCount / totalCount;
    }

    /**
     * Get failed paths
     * 获取失败的路径
     *
     * @return list of failed paths | 失败路径列表
     */
    public List<Path> failedPaths() {
        return List.copyOf(failures.keySet());
    }

    /**
     * Get exception for a specific path
     * 获取特定路径的异常
     *
     * @param path the path | 路径
     * @return exception or empty | 异常，不存在时返回空
     */
    public Optional<Throwable> getFailure(Path path) {
        return Optional.ofNullable(failures.get(path));
    }

    /**
     * Get summary string
     * 获取摘要字符串
     *
     * @return summary | 摘要
     */
    public String summary() {
        return String.format("%s: %d total, %d success, %d failed, %d skipped in %dms",
            operation, totalCount, successCount, failureCount, skippedCount, duration().toMillis());
    }

    @Override
    public String toString() {
        return summary();
    }

    /**
     * Create a builder
     * 创建构建器
     *
     * @param operation the operation type | 操作类型
     * @return builder | 构建器
     */
    public static Builder builder(String operation) {
        return new Builder(operation);
    }

    /**
     * BatchResult Builder
     * 批量结果构建器
     */
    public static class Builder {
        private final String operation;
        private final Instant startTime;
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicInteger skippedCount = new AtomicInteger(0);
        private final java.util.concurrent.ConcurrentHashMap<Path, Throwable> failures =
            new java.util.concurrent.ConcurrentHashMap<>();

        public Builder(String operation) {
            this.operation = operation;
            this.startTime = Instant.now();
        }

        /**
         * Record a success
         * 记录成功
         *
         * @return this builder | 构建器
         */
        public Builder success() {
            successCount.incrementAndGet();
            return this;
        }

        /**
         * Record a failure
         * 记录失败
         *
         * @param path the failed path | 失败路径
         * @param error the exception | 异常
         * @return this builder | 构建器
         */
        public Builder failure(Path path, Throwable error) {
            failureCount.incrementAndGet();
            failures.put(path, error);
            return this;
        }

        /**
         * Record a skip
         * 记录跳过
         *
         * @return this builder | 构建器
         */
        public Builder skipped() {
            skippedCount.incrementAndGet();
            return this;
        }

        /**
         * Increment success count atomically
         * 原子递增成功计数
         */
        public void incrementSuccess() {
            successCount.incrementAndGet();
        }

        /**
         * Record failure atomically
         * 原子记录失败
         *
         * @param path the failed path | 失败路径
         * @param error the exception | 异常
         */
        public void recordFailure(Path path, Throwable error) {
            failureCount.incrementAndGet();
            failures.put(path, error);
        }

        /**
         * Increment skip count atomically
         * 原子递增跳过计数
         */
        public void incrementSkipped() {
            skippedCount.incrementAndGet();
        }

        /**
         * Build the result
         * 构建结果
         *
         * @return batch result | 批量结果
         */
        public BatchResult build() {
            int sc = successCount.get();
            int fc = failureCount.get();
            int skc = skippedCount.get();
            int total = sc + fc + skc;
            return new BatchResult(
                operation,
                total,
                sc,
                fc,
                skc,
                Map.copyOf(failures),
                startTime,
                Instant.now()
            );
        }
    }
}
