package cloud.opencode.base.reflect.lambda;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Serializable Consumer Interface
 * 可序列化Consumer接口
 *
 * <p>A Consumer that is also Serializable, enabling lambda metadata extraction.</p>
 * <p>一个同时也是Serializable的Consumer，可以提取lambda元数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Serializable Consumer for lambda metadata extraction - 可序列化Consumer用于lambda元数据提取</li>
 *   <li>Chainable with andThen - 可通过andThen链接</li>
 *   <li>No-op factory method - 空操作工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SerializableConsumer<String> printer = System.out::println;
 * printer.accept("Hello");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @param <T> the input type | 输入类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@FunctionalInterface
public interface SerializableConsumer<T> extends Consumer<T>, Serializable {

    /**
     * Creates a SerializableConsumer
     * 创建SerializableConsumer
     *
     * @param consumer the consumer | 消费者
     * @param <T>      the input type | 输入类型
     * @return the serializable consumer | 可序列化消费者
     */
    static <T> SerializableConsumer<T> of(SerializableConsumer<T> consumer) {
        return consumer;
    }

    /**
     * Creates a no-op consumer
     * 创建空操作消费者
     *
     * @param <T> the input type | 输入类型
     * @return the no-op consumer | 空操作消费者
     */
    static <T> SerializableConsumer<T> noOp() {
        return t -> {};
    }

    /**
     * Chains this consumer with another
     * 将此消费者与另一个链接
     *
     * @param after the consumer to run after | 之后运行的消费者
     * @return the chained consumer | 链接后的消费者
     */
    default SerializableConsumer<T> andThen(SerializableConsumer<? super T> after) {
        return (T t) -> {
            accept(t);
            after.accept(t);
        };
    }
}
