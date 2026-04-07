package cloud.opencode.base.parallel;

import cloud.opencode.base.parallel.exception.OpenParallelException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Futures - CompletableFuture Aggregation Utilities
 * Futures - CompletableFuture 聚合工具
 *
 * <p>Provides static utility methods for common CompletableFuture aggregation patterns,
 * similar to Guava's {@code Futures} class. All methods are null-safe and return
 * well-defined results for empty inputs.</p>
 * <p>提供常用的 CompletableFuture 聚合模式的静态工具方法，
 * 类似于 Guava 的 {@code Futures} 类。所有方法均为 null 安全，
 * 对空输入返回明确定义的结果。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Collect all results (fail-fast)
 * CompletableFuture<List<String>> all = Futures.allAsList(future1, future2, future3);
 *
 * // Collect only successful results
 * CompletableFuture<List<String>> successes = Futures.successfulAsList(futures);
 *
 * // Settle all - get both successes and failures
 * CompletableFuture<ParallelResult<String>> settled = Futures.settleAll(futures);
 *
 * // Race - first successful wins
 * CompletableFuture<String> first = Futures.firstSuccessful(futures);
 *
 * // Timeout
 * CompletableFuture<String> timed = Futures.withTimeout(future, Duration.ofSeconds(5));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (utility class, stateless) - 线程安全: 是（工具类，无状态）</li>
 *   <li>Null-safe: All methods validate inputs - Null 安全: 所有方法验证输入</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.3
 */
public final class Futures {

    /**
     * Private constructor to prevent instantiation.
     * 私有构造函数，防止实例化。
     */
    private Futures() {
        // Static utility class - 静态工具类
    }

    // ==================== allAsList | 全部收集 ====================

    /**
     * Collects all future results into a list. If any future fails,
     * the returned future completes exceptionally.
     * 将所有 Future 结果收集到列表中。如果任何 Future 失败，
     * 返回的 Future 将异常完成。
     *
     * @param futures the futures to collect - 要收集的 Future
     * @param <T>     the result type - 结果类型
     * @return a future completing with all results, or failing if any future fails -
     *         一个包含所有结果的 Future，如果任何 Future 失败则失败
     * @throws NullPointerException if futures is null - 如果 futures 为 null
     */
    @SafeVarargs
    public static <T> CompletableFuture<List<T>> allAsList(CompletableFuture<T>... futures) {
        Objects.requireNonNull(futures, "futures must not be null / futures 不能为 null");
        return allAsList(Arrays.asList(futures));
    }

    /**
     * Collects all future results into a list. If any future fails,
     * the returned future completes exceptionally.
     * 将所有 Future 结果收集到列表中。如果任何 Future 失败，
     * 返回的 Future 将异常完成。
     *
     * @param futures the futures to collect - 要收集的 Future
     * @param <T>     the result type - 结果类型
     * @return a future completing with all results, or failing if any future fails -
     *         一个包含所有结果的 Future，如果任何 Future 失败则失败
     * @throws NullPointerException if futures is null - 如果 futures 为 null
     */
    public static <T> CompletableFuture<List<T>> allAsList(List<CompletableFuture<T>> futures) {
        Objects.requireNonNull(futures, "futures must not be null / futures 不能为 null");

        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        // Snapshot the list to avoid mutation during execution
        List<CompletableFuture<T>> snapshot = List.copyOf(futures);

        @SuppressWarnings("unchecked")
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                snapshot.toArray(new CompletableFuture[0]));

        return allOf.thenApply(_ -> {
            List<T> results = new ArrayList<>(snapshot.size());
            for (CompletableFuture<T> f : snapshot) {
                results.add(f.join());
            }
            return List.copyOf(results);
        });
    }

    // ==================== successfulAsList | 成功收集 ====================

    /**
     * Collects only the successful results from the given futures.
     * Failed futures are silently ignored. The returned future always completes normally.
     * 仅收集给定 Future 中的成功结果。失败的 Future 被静默忽略。
     * 返回的 Future 始终正常完成。
     *
     * <p>The order of results in the returned list corresponds to the order
     * of successful completions, which may differ from the input order.</p>
     * <p>返回列表中结果的顺序对应成功完成的顺序，可能与输入顺序不同。</p>
     *
     * @param futures the futures to collect - 要收集的 Future
     * @param <T>     the result type - 结果类型
     * @return a future completing with only the successful results -
     *         一个仅包含成功结果的 Future
     * @throws NullPointerException if futures is null - 如果 futures 为 null
     */
    public static <T> CompletableFuture<List<T>> successfulAsList(List<CompletableFuture<T>> futures) {
        Objects.requireNonNull(futures, "futures must not be null / futures 不能为 null");

        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        List<CompletableFuture<T>> snapshot = List.copyOf(futures);
        ConcurrentLinkedQueue<T> successes = new ConcurrentLinkedQueue<>();
        CompletableFuture<List<T>> result = new CompletableFuture<>();
        AtomicInteger remaining = new AtomicInteger(snapshot.size());

        for (CompletableFuture<T> future : snapshot) {
            future.whenComplete((value, throwable) -> {
                if (throwable == null) {
                    successes.add(value);
                }
                if (remaining.decrementAndGet() == 0) {
                    result.complete(List.copyOf(successes));
                }
            });
        }

        return result;
    }

    // ==================== settleAll | 全部结算 ====================

    /**
     * Settles all futures, collecting both successes and failures into a {@link ParallelResult}.
     * The returned future always completes normally regardless of individual future outcomes.
     * 结算所有 Future，将成功和失败收集到 {@link ParallelResult} 中。
     * 无论单个 Future 的结果如何，返回的 Future 始终正常完成。
     *
     * @param futures the futures to settle - 要结算的 Future
     * @param <T>     the result type - 结果类型
     * @return a future completing with a {@link ParallelResult} containing all outcomes -
     *         一个包含所有结果的 {@link ParallelResult} 的 Future
     * @throws NullPointerException if futures is null - 如果 futures 为 null
     */
    public static <T> CompletableFuture<ParallelResult<T>> settleAll(List<CompletableFuture<T>> futures) {
        Objects.requireNonNull(futures, "futures must not be null / futures 不能为 null");

        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(ParallelResult.of(List.of(), List.of()));
        }

        List<CompletableFuture<T>> snapshot = List.copyOf(futures);
        ConcurrentLinkedQueue<T> successes = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        CompletableFuture<ParallelResult<T>> result = new CompletableFuture<>();
        AtomicInteger remaining = new AtomicInteger(snapshot.size());

        for (CompletableFuture<T> future : snapshot) {
            future.whenComplete((value, throwable) -> {
                if (throwable != null) {
                    // Unwrap CompletionException to get the actual cause
                    Throwable cause = throwable instanceof java.util.concurrent.CompletionException
                            && throwable.getCause() != null
                            ? throwable.getCause()
                            : throwable;
                    failures.add(cause);
                } else {
                    successes.add(value);
                }
                if (remaining.decrementAndGet() == 0) {
                    result.complete(ParallelResult.of(
                            List.copyOf(successes), List.copyOf(failures)));
                }
            });
        }

        return result;
    }

    // ==================== firstSuccessful | 首个成功 ====================

    /**
     * Returns the first future that completes successfully. All remaining futures
     * are cancelled once a winner is found. If all futures fail, the returned future
     * completes exceptionally with {@link OpenParallelException}.
     * 返回首个成功完成的 Future。一旦找到获胜者，所有剩余的 Future 将被取消。
     * 如果所有 Future 都失败，返回的 Future 将以 {@link OpenParallelException} 异常完成。
     *
     * @param futures the futures to race - 要竞争的 Future
     * @param <T>     the result type - 结果类型
     * @return a future completing with the first successful result -
     *         一个包含首个成功结果的 Future
     * @throws NullPointerException     if futures is null - 如果 futures 为 null
     * @throws IllegalArgumentException if futures is empty - 如果 futures 为空
     */
    public static <T> CompletableFuture<T> firstSuccessful(List<CompletableFuture<T>> futures) {
        Objects.requireNonNull(futures, "futures must not be null / futures 不能为 null");
        if (futures.isEmpty()) {
            throw new IllegalArgumentException(
                    "futures must not be empty / futures 不能为空");
        }

        List<CompletableFuture<T>> snapshot = List.copyOf(futures);
        CompletableFuture<T> result = new CompletableFuture<>();
        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        AtomicInteger remaining = new AtomicInteger(snapshot.size());

        for (CompletableFuture<T> future : snapshot) {
            future.whenComplete((value, throwable) -> {
                if (throwable != null) {
                    Throwable cause = throwable instanceof java.util.concurrent.CompletionException
                            && throwable.getCause() != null
                            ? throwable.getCause()
                            : throwable;
                    failures.add(cause);
                    if (remaining.decrementAndGet() == 0) {
                        // All failed
                        result.completeExceptionally(
                                OpenParallelException.allFailed(List.copyOf(failures)));
                    }
                } else {
                    if (result.complete(value)) {
                        // Cancel remaining futures
                        for (CompletableFuture<T> f : snapshot) {
                            if (!f.isDone()) {
                                f.cancel(true);
                            }
                        }
                    }
                }
            });
        }

        return result;
    }

    // ==================== withTimeout | 超时控制 ====================

    /**
     * Adds a timeout to the given future. If the future does not complete within
     * the specified duration, it completes exceptionally with {@link OpenParallelException}.
     * 为给定的 Future 添加超时。如果 Future 未在指定时间内完成，
     * 将以 {@link OpenParallelException} 异常完成。
     *
     * @param future  the future to add timeout to - 要添加超时的 Future
     * @param timeout the timeout duration - 超时时长
     * @param <T>     the result type - 结果类型
     * @return a new future that completes with the result or times out -
     *         一个新的 Future，包含结果或超时
     * @throws NullPointerException if future or timeout is null - 如果 future 或 timeout 为 null
     */
    public static <T> CompletableFuture<T> withTimeout(CompletableFuture<T> future, Duration timeout) {
        Objects.requireNonNull(future, "future must not be null / future 不能为 null");
        Objects.requireNonNull(timeout, "timeout must not be null / timeout 不能为 null");

        CompletableFuture<T> result = new CompletableFuture<>();

        // Create an independent copy to avoid mutating the caller's future
        CompletableFuture<T> copy = future.thenApply(java.util.function.Function.identity());
        copy.orTimeout(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);

        copy.whenComplete((value, throwable) -> {
            if (throwable != null) {
                Throwable cause = throwable instanceof java.util.concurrent.CompletionException
                        && throwable.getCause() != null
                        ? throwable.getCause()
                        : throwable;
                if (cause instanceof TimeoutException) {
                    result.completeExceptionally(OpenParallelException.timeout(timeout));
                } else {
                    result.completeExceptionally(throwable);
                }
            } else {
                result.complete(value);
            }
        });

        return result;
    }
}
