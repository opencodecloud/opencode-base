package cloud.opencode.base.parallel;

import cloud.opencode.base.parallel.exception.OpenParallelException;

import java.util.List;
import java.util.Objects;

/**
 * Parallel Result - Encapsulates both success results and failure exceptions from parallel execution.
 * 并行结果 - 封装并行执行的成功结果和失败异常。
 *
 * <p>This is an immutable container that holds the outcome of parallel task execution,
 * separating successes from failures. It provides query methods to inspect the result
 * and terminal operations to enforce success requirements.</p>
 * <p>这是一个不可变容器，保存并行任务执行的结果，将成功与失败分离。
 * 提供查询方法检查结果，以及终端操作来强制要求成功。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * ParallelResult<String> result = Futures.settleAll(futures).join();
 *
 * if (result.hasFailures()) {
 *     log.warn("Failed: {}/{}", result.failureCount(), result.totalCount());
 * }
 *
 * // Or throw if any failed
 * List<String> values = result.getOrThrow();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Defensive copying: Uses List.copyOf() - 防御性复制: 使用 List.copyOf()</li>
 * </ul>
 *
 * @param <T> the type of success results - 成功结果的类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.3
 */
public final class ParallelResult<T> {

    private final List<T> successes;
    private final List<Throwable> failures;

    /**
     * Private constructor.
     * 私有构造函数。
     *
     * @param successes the success results - 成功结果
     * @param failures  the failure exceptions - 失败异常
     */
    private ParallelResult(List<T> successes, List<Throwable> failures) {
        this.successes = successes;
        this.failures = failures;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a parallel result with both successes and failures.
     * 创建包含成功和失败的并行结果。
     *
     * @param successes the success results (must not be null) - 成功结果（不能为 null）
     * @param failures  the failure exceptions (must not be null) - 失败异常（不能为 null）
     * @param <T>       the type of success results - 成功结果的类型
     * @return the parallel result - 并行结果
     * @throws NullPointerException if successes or failures is null - 如果 successes 或 failures 为 null
     */
    public static <T> ParallelResult<T> of(List<T> successes, List<Throwable> failures) {
        Objects.requireNonNull(successes, "successes must not be null / successes 不能为 null");
        Objects.requireNonNull(failures, "failures must not be null / failures 不能为 null");
        return new ParallelResult<>(List.copyOf(successes), List.copyOf(failures));
    }

    /**
     * Creates a parallel result where all tasks succeeded.
     * 创建所有任务都成功的并行结果。
     *
     * @param results the success results (must not be null) - 成功结果（不能为 null）
     * @param <T>     the type of success results - 成功结果的类型
     * @return the parallel result with no failures - 没有失败的并行结果
     * @throws NullPointerException if results is null - 如果 results 为 null
     */
    public static <T> ParallelResult<T> allSucceeded(List<T> results) {
        Objects.requireNonNull(results, "results must not be null / results 不能为 null");
        return new ParallelResult<>(List.copyOf(results), List.of());
    }

    /**
     * Creates a parallel result where all tasks failed.
     * 创建所有任务都失败的并行结果。
     *
     * @param failures the failure exceptions (must not be null) - 失败异常（不能为 null）
     * @param <T>      the type of success results - 成功结果的类型
     * @return the parallel result with no successes - 没有成功的并行结果
     * @throws NullPointerException if failures is null - 如果 failures 为 null
     */
    public static <T> ParallelResult<T> allFailed(List<Throwable> failures) {
        Objects.requireNonNull(failures, "failures must not be null / failures 不能为 null");
        return new ParallelResult<>(List.of(), List.copyOf(failures));
    }

    // ==================== Query Methods | 查询方法 ====================

    /**
     * Gets the unmodifiable list of success results.
     * 获取不可修改的成功结果列表。
     *
     * @return the success results - 成功结果
     */
    public List<T> successes() {
        return successes;
    }

    /**
     * Gets the unmodifiable list of failure exceptions.
     * 获取不可修改的失败异常列表。
     *
     * @return the failure exceptions - 失败异常
     */
    public List<Throwable> failures() {
        return failures;
    }

    /**
     * Returns {@code true} if there are any failures.
     * 如果存在任何失败，返回 {@code true}。
     *
     * @return true if at least one task failed - 如果至少一个任务失败则为 true
     */
    public boolean hasFailures() {
        return !failures.isEmpty();
    }

    /**
     * Returns {@code true} if all tasks succeeded (no failures).
     * 如果所有任务都成功（无失败），返回 {@code true}。
     *
     * @return true if all tasks succeeded - 如果所有任务都成功则为 true
     */
    public boolean isAllSuccessful() {
        return failures.isEmpty();
    }

    /**
     * Returns {@code true} if all tasks failed (no successes).
     * 如果所有任务都失败（无成功），返回 {@code true}。
     *
     * @return true if all tasks failed - 如果所有任务都失败则为 true
     */
    public boolean isAllFailed() {
        return successes.isEmpty() && !failures.isEmpty();
    }

    /**
     * Gets the number of successful tasks.
     * 获取成功的任务数。
     *
     * @return the success count - 成功数
     */
    public int successCount() {
        return successes.size();
    }

    /**
     * Gets the number of failed tasks.
     * 获取失败的任务数。
     *
     * @return the failure count - 失败数
     */
    public int failureCount() {
        return failures.size();
    }

    /**
     * Gets the total number of tasks (successes + failures).
     * 获取总任务数（成功数 + 失败数）。
     *
     * @return the total count - 总数
     */
    public int totalCount() {
        return successes.size() + failures.size();
    }

    // ==================== Terminal Operations | 终端操作 ====================

    /**
     * Throws {@link OpenParallelException} if any task failed.
     * 如果任何任务失败，抛出 {@link OpenParallelException}。
     *
     * <p>If there are no failures, this method does nothing.</p>
     * <p>如果没有失败，此方法不执行任何操作。</p>
     *
     * @throws OpenParallelException if at least one task failed, with aggregated failures -
     *                                如果至少一个任务失败，包含聚合的失败信息
     */
    public void throwIfAnyFailed() {
        if (!failures.isEmpty()) {
            throw OpenParallelException.partialFailure(failures, totalCount());
        }
    }

    /**
     * Throws {@link OpenParallelException} only if ALL tasks failed.
     * 仅当所有任务都失败时抛出 {@link OpenParallelException}。
     *
     * <p>If at least one task succeeded, this method does nothing.</p>
     * <p>如果至少一个任务成功，此方法不执行任何操作。</p>
     *
     * @throws OpenParallelException if all tasks failed - 如果所有任务都失败
     */
    public void throwIfAllFailed() {
        if (isAllFailed()) {
            throw OpenParallelException.allFailed(failures);
        }
    }

    /**
     * Returns the success results if no failures occurred, otherwise throws.
     * 如果没有失败则返回成功结果，否则抛出异常。
     *
     * @return the success results - 成功结果
     * @throws OpenParallelException if any task failed - 如果任何任务失败
     */
    public List<T> getOrThrow() {
        throwIfAnyFailed();
        return successes;
    }

    // ==================== Object Methods | 对象方法 ====================

    /**
     * Returns a string representation of this parallel result.
     * 返回此并行结果的字符串表示。
     *
     * @return the string representation - 字符串表示
     */
    @Override
    public String toString() {
        return "ParallelResult{successes=" + successes.size()
                + ", failures=" + failures.size()
                + ", total=" + totalCount() + "}";
    }
}
