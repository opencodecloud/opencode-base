package cloud.opencode.base.config.converter.impl;

import cloud.opencode.base.config.converter.ConfigConverter;

/**
 * String Converter (Passthrough)
 * 字符串转换器(直通)
 *
 * <p>Identity converter that passes through string values unchanged.</p>
 * <p>恒等转换器，直接返回原始字符串值。</p>
 *
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core StringConverter functionality - StringConverter核心功能</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // See class-level documentation for usage
 * // 参见类级文档了解用法
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
  * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - identity passthrough - 时间复杂度: O(1) 恒等直通</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class StringConverter implements ConfigConverter<String> {
    
    @Override
    public String convert(String value) {
        return value;
    }
    
    @Override
    public Class<String> getType() {
        return String.class;
    }
}
