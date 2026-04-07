package cloud.opencode.base.functional.function;

import cloud.opencode.base.functional.exception.OpenFunctionalException;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * CheckedConsumer - Consumer that may throw checked exceptions
 * CheckedConsumer - 可能抛出受检异常的消费者
 *
 * <p>Extends the concept of JDK {@link Consumer} to allow throwing checked exceptions.
 * Provides safe conversion to standard Consumer via {@link #unchecked()} and
 * silent execution via {@link #acceptQuietly(Object)}.</p>
 * <p>扩展 JDK {@link Consumer} 的概念，支持抛出受检异常。
 * 通过 {@link #unchecked()} 安全转换为标准 Consumer，
 * 通过 {@link #acceptQuietly(Object)} 静默执行。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Throw checked exceptions in lambda - 在 lambda 中抛出受检异常</li>
 *   <li>Convert to standard Consumer (unchecked) - 转换为标准 Consumer</li>
 *   <li>Silent execution - 静默执行</li>
 *   <li>Chaining support (andThen) - 支持链式调用</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CheckedConsumer<Path> deletePath = path -> Files.delete(path);
 *
 * Consumer<Path> wrapped = deletePath.unchecked();
 * deletePath.acceptQuietly(path);
 *
 * // Chaining
 * deletePath.andThen(p -> log.info("Deleted " + p))
 *     .accept(path);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @param <T> input type - 输入类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.3
 */
@FunctionalInterface
public interface CheckedConsumer<T> {

    /**
     * Accept input, may throw checked exception
     * 接受输入，可能抛出受检异常
     *
     * @param t input - 输入
     * @throws Exception if operation fails - 如果操作失败
     */
    void accept(T t) throws Exception;

    /**
     * Convert to standard Consumer, wrapping checked exceptions in OpenFunctionalException
     * 转换为标准 Consumer，将受检异常包装为 OpenFunctionalException
     *
     * <p>Runtime exceptions are rethrown as-is; checked exceptions are wrapped
     * in {@link OpenFunctionalException}.</p>
     * <p>运行时异常原样抛出；受检异常被包装为 {@link OpenFunctionalException}。</p>
     *
     * @return Consumer that wraps checked exceptions - 包装受检异常的 Consumer
     */
    default Consumer<T> unchecked() {
        return t -> {
            try {
                accept(t);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new OpenFunctionalException("Checked consumer failed", e);
            }
        };
    }

    /**
     * Accept silently, ignoring any checked or unchecked exception (but not {@link Error})
     * 静默接受，忽略任何受检或非受检异常（不包括 {@link Error}）
     *
     * @param t input - 输入
     */
    default void acceptQuietly(T t) {
        try {
            accept(t);
        } catch (Exception ignored) {
            // Silently ignore
        }
    }

    /**
     * Chain with another consumer (execute this, then after)
     * 与另一个消费者链接（先执行本函数，再执行 after）
     *
     * @param after consumer to execute after this - 在本函数之后执行的消费者
     * @return chained consumer - 链式消费者
     * @throws NullPointerException if after is null - 如果 after 为 null
     */
    default CheckedConsumer<T> andThen(CheckedConsumer<? super T> after) {
        Objects.requireNonNull(after, "after must not be null");
        return t -> {
            accept(t);
            after.accept(t);
        };
    }

    /**
     * Wrap a standard Consumer as CheckedConsumer
     * 将标准 Consumer 包装为 CheckedConsumer
     *
     * @param consumer standard Consumer - 标准 Consumer
     * @param <T>      input type - 输入类型
     * @return CheckedConsumer wrapper - CheckedConsumer 包装器
     */
    static <T> CheckedConsumer<T> of(Consumer<T> consumer) {
        java.util.Objects.requireNonNull(consumer, "consumer must not be null");
        return consumer::accept;
    }
}
