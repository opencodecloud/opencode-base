package cloud.opencode.base.core.convert;

/**
 * Type Converter Interface - Core interface for type conversion
 * 类型转换器接口 - 类型转换的核心接口
 *
 * <p>Functional interface for converting values between types with optional default value support.</p>
 * <p>用于在类型之间转换值的函数式接口，支持默认值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Convert with default value - 带默认值转换</li>
 *   <li>Convert without default (returns null on failure) - 无默认值转换</li>
 *   <li>Functional interface for lambda usage - 支持 Lambda 表达式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Implement converter - 实现转换器
 * Converter<Integer> intConverter = (value, def) -> {
 *     try { return Integer.parseInt(value.toString()); }
 *     catch (Exception e) { return def; }
 * };
 *
 * // Use converter - 使用转换器
 * Integer result = intConverter.convert("123", 0);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Yes, null returns default value - 空值安全: 是，null返回默认值</li>
 * </ul>
 *
 * @param <T> target type - 目标类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@FunctionalInterface
public interface Converter<T> {

    /**
     * Converts
     * 转换值为目标类型
     *
     * @param value the value | 源值
     * @param defaultValue the default value | 默认值
     * @return the result | 转换后的值，转换失败时返回默认值
     */
    T convert(Object value, T defaultValue);

    /**
     * Converts
     * 转换值为目标类型
     *
     * @param value the value | 源值
     * @return the result | 转换后的值，转换失败时返回 null
     */
    default T convert(Object value) {
        return convert(value, null);
    }
}
