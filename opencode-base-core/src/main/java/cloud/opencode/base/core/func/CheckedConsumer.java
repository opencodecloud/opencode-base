package cloud.opencode.base.core.func;

import cloud.opencode.base.core.exception.OpenException;

import java.util.function.Consumer;

/**
 * Checked Consumer - Consumer that can throw checked exceptions
 * 可抛出受检异常的 Consumer - 扩展 JDK Consumer 支持受检异常
 *
 * <p>Extends JDK {@link Consumer} to allow throwing checked exceptions in lambdas.</p>
 * <p>扩展 JDK {@link Consumer}，支持在 lambda 中抛出受检异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Throw checked exceptions in lambda - 在 lambda 中抛出受检异常</li>
 *   <li>Convert to standard Consumer (unchecked) - 转换为标准 Consumer</li>
 *   <li>Silent execution (acceptQuietly) - 静默执行</li>
 *   <li>Composition with andThen - 组合操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CheckedConsumer<Path> consumer = path -> Files.delete(path);
 * Consumer<Path> wrapped = consumer.unchecked();
 * consumer.acceptQuietly(path);  // ignores exceptions
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @param <T> input type - 输入类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@FunctionalInterface
public interface CheckedConsumer<T> {

    /**
     * Executes an operation that may throw a checked exception
     * 执行操作，可能抛出受检异常
     *
     * @param t the value | 输入值
     * @throws Exception if the condition is not met | 如果操作失败
     */
    void accept(T t) throws Exception;

    /**
     * Converts
     * 转换为标准 Consumer，受检异常包装为 RuntimeException
     *
     * @return Consumer
     */
    default Consumer<T> unchecked() {
        return t -> {
            try {
                accept(t);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new OpenException("Checked consumer failed", e);
            }
        };
    }

    /**
     * Silently executes, ignoring exceptions
     * 静默执行，忽略异常
     *
     * @param t the value | 输入值
     */
    default void acceptQuietly(T t) {
        try {
            accept(t);
        } catch (Exception ignored) {
        }
    }

    /**
     * Composes two CheckedConsumers
     * 组合两个 CheckedConsumer
     *
     * @param after the value | 后续操作
     * @return the result | 组合后的 CheckedConsumer
     */
    default CheckedConsumer<T> andThen(CheckedConsumer<? super T> after) {
        return t -> {
            accept(t);
            after.accept(t);
        };
    }

    /**
     * Wraps a standard Consumer as a CheckedConsumer
     * 将普通 Consumer 包装为 CheckedConsumer
     *
     * @param consumer the value | 普通 Consumer
     * @param <T> the value | 输入类型
     * @return CheckedConsumer
     */
    static <T> CheckedConsumer<T> of(Consumer<T> consumer) {
        return consumer::accept;
    }
}
