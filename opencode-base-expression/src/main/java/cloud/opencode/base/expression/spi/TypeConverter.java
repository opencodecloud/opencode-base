package cloud.opencode.base.expression.spi;

/**
 * Type Converter SPI
 * 类型转换器SPI
 *
 * <p>Provides a service provider interface for type conversion.</p>
 * <p>为类型转换提供服务提供者接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SPI for custom type conversion logic - 用于自定义类型转换逻辑的SPI</li>
 *   <li>Convertibility check before conversion - 转换前的可转换性检查</li>
 *   <li>Generic type-safe conversion method - 泛型类型安全转换方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class MoneyTypeConverter implements TypeConverter {
 *     @Override
 *     public boolean canConvert(Class<?> source, Class<?> target) {
 *         return target == Money.class && source == String.class;
 *     }
 *
 *     @Override
 *     public <T> T convert(Object value, Class<T> targetType) {
 *         return targetType.cast(Money.parse((String) value));
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for canConvert and convert in typical implementations - 时间复杂度: 典型实现中 canConvert 和 convert 均为 O(1)</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public interface TypeConverter {

    /**
     * Check if conversion is possible
     * 检查是否可以转换
     *
     * @param sourceType the source type | 源类型
     * @param targetType the target type | 目标类型
     * @return true if convertible | 如果可转换返回true
     */
    boolean canConvert(Class<?> sourceType, Class<?> targetType);

    /**
     * Convert value to target type
     * 将值转换为目标类型
     *
     * @param value the value to convert | 要转换的值
     * @param targetType the target type | 目标类型
     * @param <T> the target type | 目标类型
     * @return the converted value | 转换后的值
     */
    <T> T convert(Object value, Class<T> targetType);
}
