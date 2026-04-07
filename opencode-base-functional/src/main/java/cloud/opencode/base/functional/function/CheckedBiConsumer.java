package cloud.opencode.base.functional.function;

import cloud.opencode.base.functional.exception.OpenFunctionalException;

import java.util.function.BiConsumer;

/**
 * CheckedBiConsumer - BiConsumer that can throw checked exceptions
 * CheckedBiConsumer - 可抛出受检异常的双参消费者
 *
 * <p>Extends the concept of JDK {@link BiConsumer} to allow throwing checked exceptions.</p>
 * <p>扩展 JDK {@link BiConsumer} 的概念，支持抛出受检异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Throw checked exceptions in lambda - 在 lambda 中抛出受检异常</li>
 *   <li>Convert to standard BiConsumer (unchecked) - 转换为标准 BiConsumer</li>
 *   <li>Silent execution - 静默执行</li>
 *   <li>Chaining support (andThen) - 支持链式调用</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CheckedBiConsumer<Path, String> writer =
 *     (path, content) -> Files.writeString(path, content);
 *
 * BiConsumer<Path, String> wrapped = writer.unchecked();
 * writer.acceptQuietly(path, content);
 *
 * // Chaining
 * writer.andThen((p, c) -> log.info("Written to " + p))
 *     .accept(path, content);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @param <T> first input type - 第一个输入类型
 * @param <U> second input type - 第二个输入类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@FunctionalInterface
public interface CheckedBiConsumer<T, U> {

    /**
     * Accept inputs, may throw checked exception
     * 接受输入，可能抛出受检异常
     *
     * @param t first input - 第一个输入
     * @param u second input - 第二个输入
     * @throws Exception if operation fails - 如果操作失败
     */
    void accept(T t, U u) throws Exception;

    /**
     * Convert to standard BiConsumer, wrapping checked exceptions
     * 转换为标准 BiConsumer，包装受检异常
     *
     * @return BiConsumer that wraps checked exceptions - 包装受检异常的 BiConsumer
     */
    default BiConsumer<T, U> unchecked() {
        return (t, u) -> {
            try {
                accept(t, u);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new OpenFunctionalException("Checked bi-consumer failed", e);
            }
        };
    }

    /**
     * Accept silently, ignoring any checked or unchecked exception (but not {@link Error})
     * 静默接受，忽略任何受检或非受检异常（不包括 {@link Error}）
     *
     * @param t first input - 第一个输入
     * @param u second input - 第二个输入
     */
    default void acceptQuietly(T t, U u) {
        try {
            accept(t, u);
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
     */
    default CheckedBiConsumer<T, U> andThen(CheckedBiConsumer<? super T, ? super U> after) {
        java.util.Objects.requireNonNull(after, "after must not be null");
        return (t, u) -> {
            accept(t, u);
            after.accept(t, u);
        };
    }

    /**
     * Wrap a standard BiConsumer as CheckedBiConsumer
     * 将标准 BiConsumer 包装为 CheckedBiConsumer
     *
     * @param consumer standard BiConsumer - 标准 BiConsumer
     * @param <T>      first input type - 第一个输入类型
     * @param <U>      second input type - 第二个输入类型
     * @return CheckedBiConsumer wrapper - CheckedBiConsumer 包装器
     */
    static <T, U> CheckedBiConsumer<T, U> of(BiConsumer<T, U> consumer) {
        java.util.Objects.requireNonNull(consumer, "consumer must not be null");
        return consumer::accept;
    }
}
