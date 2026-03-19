package cloud.opencode.base.reflect.lambda;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Serializable Supplier Interface
 * 可序列化Supplier接口
 *
 * <p>A Supplier that is also Serializable, enabling lambda metadata extraction.</p>
 * <p>一个同时也是Serializable的Supplier，可以提取lambda元数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Serializable Supplier for lambda metadata extraction - 可序列化Supplier用于lambda元数据提取</li>
 *   <li>Constant and null factory methods - 常量和null工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SerializableSupplier<String> supplier = () -> "hello";
 * String value = supplier.get(); // "hello"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @param <T> the output type | 输出类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@FunctionalInterface
public interface SerializableSupplier<T> extends Supplier<T>, Serializable {

    /**
     * Creates a SerializableSupplier
     * 创建SerializableSupplier
     *
     * @param supplier the supplier | 提供者
     * @param <T>      the output type | 输出类型
     * @return the serializable supplier | 可序列化提供者
     */
    static <T> SerializableSupplier<T> of(SerializableSupplier<T> supplier) {
        return supplier;
    }

    /**
     * Creates a constant supplier
     * 创建常量提供者
     *
     * @param value the value to supply | 要提供的值
     * @param <T>   the output type | 输出类型
     * @return the constant supplier | 常量提供者
     */
    static <T> SerializableSupplier<T> constant(T value) {
        return () -> value;
    }

    /**
     * Creates a null supplier
     * 创建null提供者
     *
     * @param <T> the output type | 输出类型
     * @return the null supplier | null提供者
     */
    static <T> SerializableSupplier<T> nullSupplier() {
        return () -> null;
    }
}
