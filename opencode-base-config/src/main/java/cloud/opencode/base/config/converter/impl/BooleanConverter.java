package cloud.opencode.base.config.converter.impl;

import cloud.opencode.base.config.OpenConfigException;
import cloud.opencode.base.config.converter.ConfigConverter;

/**
 * Boolean Converter
 * 布尔转换器
 *
 * <p>Converts string values to Boolean with support for multiple formats.</p>
 * <p>将字符串值转换为Boolean，支持多种格式。</p>
 *
 * <p><strong>Supported Formats | 支持的格式:</strong></p>
 * <pre>
 * true:  "true", "yes", "on", "1", "enabled"
 * false: "false", "no", "off", "0", "disabled"
 * </pre>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BooleanConverter converter = new BooleanConverter();
 * boolean enabled = converter.convert("yes");   // true
 * boolean active = converter.convert("1");      // true
 * boolean flag = converter.convert("off");      // false
 * }</pre>
 *
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core BooleanConverter functionality - BooleanConverter核心功能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
  * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - switch on bounded string set - 时间复杂度: O(1) 有界字符串集合的 switch</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class BooleanConverter implements ConfigConverter<Boolean> {
    
    @Override
    public Boolean convert(String value) {
        return switch (value.toLowerCase()) {
            case "true", "yes", "on", "1", "enabled" -> true;
            case "false", "no", "off", "0", "disabled" -> false;
            default -> throw OpenConfigException.invalidBoolean(value);
        };
    }
    
    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }
}
