package cloud.opencode.base.functional.async;

import cloud.opencode.base.functional.exception.OpenFunctionalException;
import cloud.opencode.base.functional.monad.Try;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * LazyAsync - Lazy asynchronous computation
 * LazyAsync - 惰性异步计算
 *
 * <p>Combines lazy evaluation with asynchronous execution. The computation
 * is not started until explicitly triggered, then runs on a Virtual Thread.
 * Results are cached for subsequent access.</p>
 * <p>将惰性求值与异步执行结合。计算在显式触发之前不会开始，然后在虚拟线程上运行。
 * 结果会被缓存以供后续访问。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Deferred async execution - 延迟异步执行</li>
 *   <li>Virtual Thread based - 基于虚拟线程</li>
 *   <li>Result caching - 结果缓存</li>
 *   <li>Timeout support - 超时支持</li>
 *   <li>Monadic operations - Monad 操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create lazy async computation
 * LazyAsync<Data> lazy = LazyAsync.of(() -> fetchDataFromNetwork());
 *
 * // Computation not started yet
 * assertFalse(lazy.isStarted());
 *
 * // Start and get result
 * CompletableFuture<Data> future = lazy.start();
 * Data data = future.join();
 *
 * // Or get blocking with timeout
 * Try<Data> result = lazy.get(Duration.ofSeconds(5));
 *
 * // Chain transformations (also lazy)
 * LazyAsync<String> processed = lazy
 *     .map(Data::getName)
 *     .map(String::toUpperCase);
 *
 * // Force evaluation
 * String name = processed.force();
 *
 * // Multiple lazy operations
 * LazyAsync<Combined> combined = LazyAsync.combine(
 *     LazyAsync.of(() -> fetchA()),
 *     LazyAsync.of(() -> fetchB()),
 *     Combined::new
 * );
 * }</pre>
 *
 * <p><strong>States | 状态:</strong></p>
 * <ul>
 *   <li>NOT_STARTED - Computation not yet triggered - 未启动 - 计算尚未触发</li>
 *   <li>RUNNING - Computation in progress - 运行中 - 计算进行中</li>
 *   <li>COMPLETED - Result available - 已完成 - 结果可用</li>
 *   <li>FAILED - Computation failed - 失败 - 计算失败</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Start: O(1) (just triggers async) - 启动: O(1) (仅触发异步)</li>
 *   <li>Subsequent get: O(1) (cached) - 后续获取: O(1) (已缓存)</li>
 *   <li>Memory: Holds supplier until started - 内存: 保持 supplier 直到启动</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Exception handling: Wrapped in Try/Future - 异常处理: 包装在 Try/Future 中</li>
 * </ul>
 *
 * @param <T> result type - 结果类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public final class LazyAsync<T> {

    /**
     * Computation states
     * 计算状态
     */
    public enum State {
        NOT_STARTED,
        RUNNING,
        COMPLETED,
        FAILED
    }

    private final Supplier<T> supplier;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile CompletableFuture<T> future;
    private volatile State state = State.NOT_STARTED;

    private LazyAsync(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a lazy async computation
     * 创建惰性异步计算
     *
     * @param supplier the computation - 计算
     * @param <T>      result type - 结果类型
     * @return lazy async container
     */
    public static <T> LazyAsync<T> of(Supplier<T> supplier) {
        return new LazyAsync<>(supplier);
    }

    /**
     * Create an already completed lazy async
     * 创建已完成的惰性异步
     *
     * @param value the value - 值
     * @param <T>   result type - 结果类型
     * @return completed lazy async
     */
    public static <T> LazyAsync<T> completed(T value) {
        LazyAsync<T> lazy = new LazyAsync<>(() -> value);
        lazy.future = CompletableFuture.completedFuture(value);
        lazy.state = State.COMPLETED;
        return lazy;
    }

    /**
     * Create a failed lazy async
     * 创建失败的惰性异步
     *
     * @param throwable the exception - 异常
     * @param <T>       result type - 结果类型
     * @return failed lazy async
     */
    public static <T> LazyAsync<T> failed(Throwable throwable) {
        LazyAsync<T> lazy = new LazyAsync<>(() -> { throw new OpenFunctionalException("Lazy async failed", throwable); });
        lazy.future = CompletableFuture.failedFuture(throwable);
        lazy.state = State.FAILED;
        return lazy;
    }

    // ==================== Execution | 执行 ====================

    /**
     * Start the computation if not already started
     * 如果尚未启动则启动计算
     *
     * @return CompletableFuture with result
     */
    public CompletableFuture<T> start() {
        lock.lock();
        try {
            if (future == null) {
                state = State.RUNNING;
                future = AsyncFunctionUtil.async(supplier);
                future.whenComplete((result, error) -> {
                    state = error != null ? State.FAILED : State.COMPLETED;
                });
            }
            return future;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the result, starting computation if needed
     * 获取结果，必要时启动计算
     *
     * @return the result (blocking)
     * @throws RuntimeException if computation fails
     */
    public T force() {
        try {
            T result = start().get();
            // Ensure state is updated after get() completes (don't rely only on async whenComplete)
            state = State.COMPLETED;
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenFunctionalException("Interrupted while waiting for result", e);
        } catch (ExecutionException e) {
            state = State.FAILED;
            throw new OpenFunctionalException("Computation failed", e.getCause());
        }
    }

    /**
     * Get the result with timeout
     * 带超时获取结果
     *
     * @param timeout maximum wait time - 最大等待时间
     * @return Try containing result or exception
     */
    public Try<T> get(Duration timeout) {
        try {
            T result = start().get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            return Try.success(result);
        } catch (TimeoutException e) {
            return Try.failure(new TimeoutException("Computation timed out after " + timeout));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Try.failure(e);
        } catch (ExecutionException e) {
            return Try.failure(e.getCause() != null ? e.getCause() : e);
        }
    }

    /**
     * Get the result as Try (blocking)
     * 获取结果为 Try（阻塞）
     *
     * @return Try containing result
     */
    public Try<T> toTry() {
        try {
            return Try.success(force());
        } catch (Exception e) {
            return Try.failure(e);
        }
    }

    // ==================== State Queries | 状态查询 ====================

    /**
     * Check if computation has been started
     * 检查计算是否已启动
     *
     * @return true if started
     */
    public boolean isStarted() {
        return state != State.NOT_STARTED;
    }

    /**
     * Check if computation is running
     * 检查计算是否正在运行
     *
     * @return true if running
     */
    public boolean isRunning() {
        return state == State.RUNNING;
    }

    /**
     * Check if computation is complete
     * 检查计算是否完成
     *
     * @return true if completed successfully
     */
    public boolean isCompleted() {
        return state == State.COMPLETED;
    }

    /**
     * Check if computation failed
     * 检查计算是否失败
     *
     * @return true if failed
     */
    public boolean isFailed() {
        return state == State.FAILED;
    }

    /**
     * Get the current state
     * 获取当前状态
     *
     * @return the state
     */
    public State state() {
        return state;
    }

    // ==================== Transformations | 转换 ====================

    /**
     * Transform the result lazily
     * 惰性转换结果
     *
     * @param mapper transformation function - 转换函数
     * @param <U>    result type - 结果类型
     * @return new lazy async with transformation
     */
    public <U> LazyAsync<U> map(Function<? super T, ? extends U> mapper) {
        return LazyAsync.of(() -> mapper.apply(this.force()));
    }

    /**
     * Flat-map to another lazy async
     * 扁平映射到另一个惰性异步
     *
     * @param mapper function returning LazyAsync - 返回 LazyAsync 的函数
     * @param <U>    result type - 结果类型
     * @return flattened lazy async
     */
    public <U> LazyAsync<U> flatMap(Function<? super T, LazyAsync<U>> mapper) {
        return LazyAsync.of(() -> mapper.apply(this.force()).force());
    }

    /**
     * Recover from failure
     * 从失败恢复
     *
     * @param recovery recovery function - 恢复函数
     * @return lazy async with recovery
     */
    public LazyAsync<T> recover(Function<Throwable, T> recovery) {
        return LazyAsync.of(() -> {
            try {
                return this.force();
            } catch (Exception e) {
                return recovery.apply(e);
            }
        });
    }

    /**
     * Provide fallback on failure
     * 失败时提供回退
     *
     * @param fallback fallback lazy async - 回退惰性异步
     * @return lazy async with fallback
     */
    public LazyAsync<T> orElse(LazyAsync<T> fallback) {
        return LazyAsync.of(() -> {
            try {
                return this.force();
            } catch (Exception e) {
                return fallback.force();
            }
        });
    }

    // ==================== Combinators | 组合器 ====================

    /**
     * Combine two lazy async computations
     * 组合两个惰性异步计算
     *
     * @param la1      first lazy async - 第一个惰性异步
     * @param la2      second lazy async - 第二个惰性异步
     * @param combiner combiner function - 组合函数
     * @param <T1>     first type - 第一个类型
     * @param <T2>     second type - 第二个类型
     * @param <R>      result type - 结果类型
     * @return combined lazy async
     */
    public static <T1, T2, R> LazyAsync<R> combine(
            LazyAsync<T1> la1,
            LazyAsync<T2> la2,
            java.util.function.BiFunction<T1, T2, R> combiner) {
        return LazyAsync.of(() -> {
            // Start both in parallel
            CompletableFuture<T1> f1 = la1.start();
            CompletableFuture<T2> f2 = la2.start();

            // Wait for both
            try {
                T1 r1 = f1.get();
                T2 r2 = f2.get();
                return combiner.apply(r1, r2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new OpenFunctionalException("Interrupted while combining async results", e);
            } catch (ExecutionException e) {
                throw new OpenFunctionalException("Combined async computation failed", e.getCause());
            }
        });
    }

    /**
     * Race two lazy async computations
     * 竞争两个惰性异步计算
     *
     * <p>Returns the result of whichever completes first.</p>
     * <p>返回先完成的那个的结果。</p>
     *
     * @param la1 first lazy async - 第一个惰性异步
     * @param la2 second lazy async - 第二个惰性异步
     * @param <T> result type - 结果类型
     * @return lazy async completing with first result
     */
    public static <T> LazyAsync<T> race(LazyAsync<T> la1, LazyAsync<T> la2) {
        return LazyAsync.of(() -> {
            CompletableFuture<T> f1 = la1.start();
            CompletableFuture<T> f2 = la2.start();

            try {
                return CompletableFuture.anyOf(f1, f2)
                        .thenApply(result -> {
                            @SuppressWarnings("unchecked")
                            T typed = (T) result;
                            return typed;
                        })
                        .get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new OpenFunctionalException("Interrupted while racing async computations", e);
            } catch (ExecutionException e) {
                throw new OpenFunctionalException("Raced async computation failed", e.getCause());
            }
        });
    }

    /**
     * Get the underlying future if started
     * 获取底层 Future（如果已启动）
     *
     * @return CompletableFuture or null if not started
     */
    public CompletableFuture<T> getFuture() {
        return future;
    }

    @Override
    public String toString() {
        return "LazyAsync[" + state + "]";
    }
}
