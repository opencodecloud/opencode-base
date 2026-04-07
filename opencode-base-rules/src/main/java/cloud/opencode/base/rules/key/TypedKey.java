package cloud.opencode.base.rules.key;

import java.util.Objects;

/**
 * Typed Key - Type-Safe Key for Fact and Variable Access
 * 类型化键 - 用于事实和变量访问的类型安全键
 *
 * <p>Provides compile-time type safety when accessing values from {@code FactStore}
 * and {@code RuleContext}, eliminating the need for manual casting.</p>
 * <p>在从 {@code FactStore} 和 {@code RuleContext} 访问值时提供编译时类型安全，
 * 消除手动转换的需要。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TypedKey<String> CUSTOMER_TYPE = TypedKey.of("customerType", String.class);
 * TypedKey<Double> ORDER_AMOUNT = TypedKey.of("orderAmount", Double.class);
 *
 * // Type-safe access
 * context.put(CUSTOMER_TYPE, "VIP");
 * String type = context.get(CUSTOMER_TYPE);
 * }</pre>
 *
 * @param name the key name | 键名称
 * @param type the value type | 值类型
 * @param <T>  the value type | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.3
 */
public record TypedKey<T>(String name, Class<T> type) {

    /**
     * Canonical constructor with validation
     * 带验证的规范构造函数
     *
     * @param name the key name | 键名称
     * @param type the value type | 值类型
     * @throws NullPointerException if name or type is null | 如果name或type为null则抛出
     */
    public TypedKey {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
    }

    /**
     * Creates a typed key with the given name and type
     * 使用给定名称和类型创建类型化键
     *
     * @param name the key name | 键名称
     * @param type the value type | 值类型
     * @param <T>  the value type | 值类型
     * @return the typed key | 类型化键
     * @throws NullPointerException if name or type is null | 如果name或type为null则抛出
     */
    public static <T> TypedKey<T> of(String name, Class<T> type) {
        return new TypedKey<>(name, type);
    }

    @Override
    public String toString() {
        return "TypedKey{name='" + name + "',type=" + type.getName() + "}";
    }
}
