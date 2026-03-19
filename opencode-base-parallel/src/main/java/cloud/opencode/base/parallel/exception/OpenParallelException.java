package cloud.opencode.base.parallel.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.time.Duration;
import java.util.List;

/**
 * Open Parallel Exception - Parallel Execution Exception
 * Open 并行异常 - 并行执行异常
 *
 * <p>This exception is thrown when parallel task execution fails,
 * including timeout, interruption, and partial failures.</p>
 * <p>当并行任务执行失败时抛出此异常，包括超时、中断和部分失败。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * try {
 *     OpenParallel.invokeAll(tasks, Duration.ofSeconds(10));
 * } catch (OpenParallelException e) {
 *     log.error("Failed: {}/{}", e.getFailedCount(), e.getTotalCount());
 *     e.getSuppressedExceptions().forEach(ex -> log.warn("Suppressed: ", ex));
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Suppressed exception tracking - 被抑制异常跟踪</li>
 *   <li>Failed/total task count - 失败/总任务计数</li>
 *   <li>Factory methods for common errors - 常见错误的工厂方法</li>
 *   <li>Extends OpenException for unified handling - 继承OpenException统一处理</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
public class OpenParallelException extends OpenException {

    private static final String COMPONENT = "PARALLEL";

    private final List<Throwable> suppressedExceptions;
    private final int failedCount;
    private final int totalCount;

    /**
     * Constructs a new parallel exception with message.
     * 使用消息构造新的并行异常。
     *
     * @param message the detail message - 详细消息
     */
    public OpenParallelException(String message) {
        super(COMPONENT, null, message);
        this.suppressedExceptions = List.of();
        this.failedCount = 0;
        this.totalCount = 0;
    }

    /**
     * Constructs a new parallel exception with message and cause.
     * 使用消息和原因构造新的并行异常。
     *
     * @param message the detail message - 详细消息
     * @param cause   the cause - 原因
     */
    public OpenParallelException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.suppressedExceptions = List.of();
        this.failedCount = 1;
        this.totalCount = 1;
    }

    /**
     * Constructs a new parallel exception with full details.
     * 使用完整详情构造新的并行异常。
     *
     * @param message     the detail message - 详细消息
     * @param suppressed  the suppressed exceptions - 被抑制的异常
     * @param failedCount the number of failed tasks - 失败的任务数
     * @param totalCount  the total number of tasks - 总任务数
     */
    public OpenParallelException(String message, List<Throwable> suppressed,
                                  int failedCount, int totalCount) {
        super(COMPONENT, null, message);
        this.suppressedExceptions = suppressed != null ? List.copyOf(suppressed) : List.of();
        this.failedCount = failedCount;
        this.totalCount = totalCount;
        this.suppressedExceptions.forEach(this::addSuppressed);
    }

    // ==================== Getters ====================

    /**
     * Gets the suppressed exceptions.
     * 获取被抑制的异常。
     *
     * @return the suppressed exceptions - 被抑制的异常
     */
    public List<Throwable> getSuppressedExceptions() {
        return suppressedExceptions;
    }

    /**
     * Gets the number of failed tasks.
     * 获取失败的任务数。
     *
     * @return the failed count - 失败数
     */
    public int getFailedCount() {
        return failedCount;
    }

    /**
     * Gets the total number of tasks.
     * 获取总任务数。
     *
     * @return the total count - 总数
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Gets the number of successful tasks.
     * 获取成功的任务数。
     *
     * @return the success count - 成功数
     */
    public int getSuccessCount() {
        return totalCount - failedCount;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a timeout exception.
     * 创建超时异常。
     *
     * @param timeout the timeout duration - 超时时长
     * @return the exception - 异常
     */
    public static OpenParallelException timeout(Duration timeout) {
        return new OpenParallelException(
                "Parallel execution timeout after " + timeout.toMillis() + "ms");
    }

    /**
     * Creates an interrupted exception.
     * 创建中断异常。
     *
     * @param cause the cause - 原因
     * @return the exception - 异常
     */
    public static OpenParallelException interrupted(InterruptedException cause) {
        return new OpenParallelException("Parallel execution interrupted", cause);
    }

    /**
     * Creates a partial failure exception.
     * 创建部分失败异常。
     *
     * @param failures   the failed exceptions - 失败的异常列表
     * @param totalCount the total task count - 总任务数
     * @return the exception - 异常
     */
    public static OpenParallelException partialFailure(List<Throwable> failures, int totalCount) {
        return new OpenParallelException(
                "Parallel execution partially failed: " + failures.size() + "/" + totalCount + " tasks failed",
                failures, failures.size(), totalCount);
    }

    /**
     * Creates an all-failed exception.
     * 创建全部失败异常。
     *
     * @param failures the failed exceptions - 失败的异常列表
     * @return the exception - 异常
     */
    public static OpenParallelException allFailed(List<Throwable> failures) {
        return new OpenParallelException(
                "All " + failures.size() + " parallel tasks failed",
                failures, failures.size(), failures.size());
    }

    /**
     * Creates an execution failed exception.
     * 创建执行失败异常。
     *
     * @param message the message - 消息
     * @param cause   the cause - 原因
     * @return the exception - 异常
     */
    public static OpenParallelException executionFailed(String message, Throwable cause) {
        return new OpenParallelException(message, cause);
    }
}
