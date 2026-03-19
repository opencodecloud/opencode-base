package cloud.opencode.base.config.converter.impl;

import cloud.opencode.base.config.converter.ConfigConverter;

/**
 * Enum Converter
 * 枚举转换器
 *
 * <p>Converts string values to enum constants (case-insensitive).</p>
 * <p>将字符串值转换为枚举常量(不区分大小写)。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * enum Status { ACTIVE, INACTIVE, PENDING }
 *
 * EnumConverter<Status> converter = new EnumConverter<>(Status.class);
 * Status status = converter.convert("active");  // ACTIVE
 * Status pending = converter.convert("PENDING"); // PENDING
 * }</pre>
 *
 * @param <E> enum type | 枚举类型
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core EnumConverter functionality - EnumConverter核心功能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
  * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) amortized - enum constant lookup via HashMap - 时间复杂度: O(1) 均摊，通过 HashMap 查找枚举常量</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class EnumConverter<E extends Enum<E>> implements ConfigConverter<E> {
    
    private final Class<E> enumType;
    
    public EnumConverter(Class<E> enumType) {
        this.enumType = enumType;
    }
    
    @Override
    public E convert(String value) {
        return Enum.valueOf(enumType, value.toUpperCase());
    }
    
    @Override
    public Class<E> getType() {
        return enumType;
    }
}
