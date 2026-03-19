package cloud.opencode.base.core.bean;

import cloud.opencode.base.core.convert.Convert;

/**
 * Property Converter Interface - Custom property conversion strategy
 * 属性转换器接口 - 自定义属性转换策略
 *
 * <p>Functional interface for custom property conversion during bean copy operations.</p>
 * <p>函数式接口，用于 Bean 复制操作中的自定义属性转换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Custom conversion logic - 自定义转换逻辑</li>
 *   <li>Access to source/target types - 访问源/目标类型</li>
 *   <li>Property name aware - 属性名感知</li>
 *   <li>Chainable converters (andThen) - 可链式转换器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Custom converter - 自定义转换器
 * PropertyConverter converter = (value, srcType, tgtType, name) -> {
 *     if ("date".equals(name)) return formatDate(value);
 *     return Convert.convert(value, tgtType);
 * };
 * OpenBean.copyProperties(source, target, converter);
 *
 * // Chain converters - 链式转换器
 * PropertyConverter chained = converter.andThen(anotherConverter);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Implementation dependent - 空值安全: 取决于实现</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@FunctionalInterface
public interface PropertyConverter {

    /**
     * Converts property value
     * 转换属性值
     *
     * @param sourceValue  source value - 源值
     * @param sourceType   source type - 源类型
     * @param targetType   target type - 目标类型
     * @param propertyName property name - 属性名
     * @return the converted value - 转换后的值
     */
    Object convert(Object sourceValue, Class<?> sourceType, Class<?> targetType, String propertyName);

    /**
     * Default converter (uses Convert utility)
     * 默认转换器（使用 Convert 工具）
     */
    static PropertyConverter defaultConverter() {
        return (value, sourceType, targetType, name) -> Convert.convert(value, targetType);
    }

    /**
     * Identity converter that returns the original value
     * 直接返回原值的转换器
     */
    static PropertyConverter identity() {
        return (value, sourceType, targetType, name) -> value;
    }

    /**
     * Chained converter
     * 链式转换器
     */
    default PropertyConverter andThen(PropertyConverter after) {
        return (value, sourceType, targetType, name) -> {
            Object result = this.convert(value, sourceType, targetType, name);
            if (result == null) return null;
            return after.convert(result, result.getClass(), targetType, name);
        };
    }
}
