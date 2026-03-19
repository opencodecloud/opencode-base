package cloud.opencode.base.parallel.structured;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Task Result - Structured Task Result Wrapper
 * 任务结果 - 结构化任务结果包装器
 *
 * <p>Represents the result of a structured task execution,
 * containing either a success value or a failure exception.</p>
 * <p>表示结构化任务执行的结果，包含成功值或失败异常。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * TaskResult<String> result = TaskResult.success("data");
 * result.ifSuccess(data -> process(data))
 *       .ifFailure(ex -> log.error("Failed", ex));
 *
 * String value = result.getOrDefault("fallback");
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Success/Failure/Cancelled states - 成功/失败/取消状态</li>
 *   <li>Functional transformation (map, flatMap, recover) - 函数式转换</li>
 *   <li>Side effect handling (ifSuccess, ifFailure) - 副作用处理</li>
 *   <li>Optional and default value access - Optional和默认值访问</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 * </ul>
 * @param <T> the result type - 结果类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
public final class TaskResult<T> {

    private final T value;
    private final Throwable exception;
    private final State state;

    /**
     * Task state enumeration.
     * 任务状态枚举。
     */
    public enum State {
        /** Task completed successfully - 任务成功完成 */
        SUCCESS,
        /** Task failed with exception - 任务因异常失败 */
        FAILURE,
        /** Task was cancelled - 任务被取消 */
        CANCELLED
    }

    private TaskResult(T value, Throwable exception, State state) {
        this.value = value;
        this.exception = exception;
        this.state = state;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a successful result.
     * 创建成功结果。
     *
     * @param value the value - 值
     * @param <T>   the result type - 结果类型
     * @return the result - 结果
     */
    public static <T> TaskResult<T> success(T value) {
        return new TaskResult<>(value, null, State.SUCCESS);
    }

    /**
     * Creates a failed result.
     * 创建失败结果。
     *
     * @param exception the exception - 异常
     * @param <T>       the result type - 结果类型
     * @return the result - 结果
     */
    public static <T> TaskResult<T> failure(Throwable exception) {
        Objects.requireNonNull(exception, "Exception cannot be null");
        return new TaskResult<>(null, exception, State.FAILURE);
    }

    /**
     * Creates a cancelled result.
     * 创建取消结果。
     *
     * @param <T> the result type - 结果类型
     * @return the result - 结果
     */
    public static <T> TaskResult<T> cancelled() {
        return new TaskResult<>(null, null, State.CANCELLED);
    }

    /**
     * Creates a result from a callable.
     * 从 Callable 创建结果。
     *
     * @param callable the callable - Callable
     * @param <T>      the result type - 结果类型
     * @return the result - 结果
     */
    public static <T> TaskResult<T> of(java.util.concurrent.Callable<T> callable) {
        try {
            return success(callable.call());
        } catch (Exception e) {
            return failure(e);
        }
    }

    // ==================== State Checks ====================

    /**
     * Checks if the task succeeded.
     * 检查任务是否成功。
     *
     * @return true if success - 如果成功返回 true
     */
    public boolean isSuccess() {
        return state == State.SUCCESS;
    }

    /**
     * Checks if the task failed.
     * 检查任务是否失败。
     *
     * @return true if failure - 如果失败返回 true
     */
    public boolean isFailure() {
        return state == State.FAILURE;
    }

    /**
     * Checks if the task was cancelled.
     * 检查任务是否被取消。
     *
     * @return true if cancelled - 如果取消返回 true
     */
    public boolean isCancelled() {
        return state == State.CANCELLED;
    }

    /**
     * Gets the state.
     * 获取状态。
     *
     * @return the state - 状态
     */
    public State getState() {
        return state;
    }

    // ==================== Value Access ====================

    /**
     * Gets the value, throwing if failed.
     * 获取值，如果失败则抛出异常。
     *
     * @return the value - 值
     * @throws IllegalStateException if not success - 如果不是成功状态
     */
    public T get() {
        if (state != State.SUCCESS) {
            throw new IllegalStateException("Task did not succeed: " + state);
        }
        return value;
    }

    /**
     * Gets the value or null.
     * 获取值或 null。
     *
     * @return the value or null - 值或 null
     */
    public T getOrNull() {
        return value;
    }

    /**
     * Gets the value or default.
     * 获取值或默认值。
     *
     * @param defaultValue the default value - 默认值
     * @return the value or default - 值或默认值
     */
    public T getOrDefault(T defaultValue) {
        return isSuccess() ? value : defaultValue;
    }

    /**
     * Gets the value or computes from exception.
     * 获取值或从异常计算。
     *
     * @param mapper the exception mapper - 异常映射器
     * @return the value or mapped - 值或映射结果
     */
    public T getOrElse(Function<Throwable, T> mapper) {
        return isSuccess() ? value : mapper.apply(exception);
    }

    /**
     * Gets the value as Optional.
     * 获取值为 Optional。
     *
     * @return the optional value - Optional 值
     */
    public Optional<T> toOptional() {
        return isSuccess() ? Optional.ofNullable(value) : Optional.empty();
    }

    /**
     * Gets the exception.
     * 获取异常。
     *
     * @return the exception or null - 异常或 null
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * Gets the exception as Optional.
     * 获取异常为 Optional。
     *
     * @return the optional exception - Optional 异常
     */
    public Optional<Throwable> getExceptionOptional() {
        return Optional.ofNullable(exception);
    }

    // ==================== Transformation ====================

    /**
     * Maps the success value.
     * 映射成功值。
     *
     * @param mapper the mapper function - 映射函数
     * @param <R>    the result type - 结果类型
     * @return the mapped result - 映射结果
     */
    public <R> TaskResult<R> map(Function<T, R> mapper) {
        if (isSuccess()) {
            try {
                return success(mapper.apply(value));
            } catch (Exception e) {
                return failure(e);
            }
        }
        return new TaskResult<>(null, exception, state);
    }

    /**
     * Flat maps the success value.
     * 扁平映射成功值。
     *
     * @param mapper the mapper function - 映射函数
     * @param <R>    the result type - 结果类型
     * @return the flat mapped result - 扁平映射结果
     */
    public <R> TaskResult<R> flatMap(Function<T, TaskResult<R>> mapper) {
        if (isSuccess()) {
            try {
                return mapper.apply(value);
            } catch (Exception e) {
                return failure(e);
            }
        }
        return new TaskResult<>(null, exception, state);
    }

    /**
     * Recovers from failure.
     * 从失败中恢复。
     *
     * @param recovery the recovery function - 恢复函数
     * @return the recovered result - 恢复结果
     */
    public TaskResult<T> recover(Function<Throwable, T> recovery) {
        if (isFailure()) {
            try {
                return success(recovery.apply(exception));
            } catch (Exception e) {
                return failure(e);
            }
        }
        return this;
    }

    // ==================== Side Effects ====================

    /**
     * Executes action if success.
     * 如果成功执行动作。
     *
     * @param action the action - 动作
     * @return this result - 此结果
     */
    public TaskResult<T> ifSuccess(Consumer<T> action) {
        if (isSuccess()) {
            action.accept(value);
        }
        return this;
    }

    /**
     * Executes action if failure.
     * 如果失败执行动作。
     *
     * @param action the action - 动作
     * @return this result - 此结果
     */
    public TaskResult<T> ifFailure(Consumer<Throwable> action) {
        if (isFailure()) {
            action.accept(exception);
        }
        return this;
    }

    /**
     * Executes action if cancelled.
     * 如果取消执行动作。
     *
     * @param action the action - 动作
     * @return this result - 此结果
     */
    public TaskResult<T> ifCancelled(Runnable action) {
        if (isCancelled()) {
            action.run();
        }
        return this;
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskResult<?> that)) return false;
        return state == that.state &&
                Objects.equals(value, that.value) &&
                Objects.equals(exception, that.exception);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, exception, state);
    }

    @Override
    public String toString() {
        return switch (state) {
            case SUCCESS -> "TaskResult.Success[" + value + "]";
            case FAILURE -> "TaskResult.Failure[" + exception.getClass().getSimpleName() + ": " + exception.getMessage() + "]";
            case CANCELLED -> "TaskResult.Cancelled";
        };
    }
}
