package cloud.opencode.base.config.converter.impl;

import cloud.opencode.base.config.converter.ConfigConverter;
import java.time.Duration;

/**
 * Duration Converter
 * Duration转换器
 *
 * <p>Converts string values to Duration objects with support for simplified format.</p>
 * <p>将字符串值转换为Duration对象，支持简化格式。</p>
 *
 * <p><strong>Supported Formats | 支持的格式:</strong></p>
 * <pre>
 * 30s  → Duration.ofSeconds(30)
 * 5m   → Duration.ofMinutes(5)
 * 2h   → Duration.ofHours(2)
 * 1d   → Duration.ofDays(1)
 * PT1H30M → ISO-8601 format
 * </pre>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DurationConverter converter = new DurationConverter();
 * Duration timeout = converter.convert("30s");  // 30 seconds
 * Duration interval = converter.convert("5m");  // 5 minutes
 * Duration iso = converter.convert("PT1H30M"); // 1 hour 30 minutes
 * }</pre>
 *
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core DurationConverter functionality - DurationConverter核心功能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n is value string length - 时间复杂度: O(n)，n 为值字符串长度</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class DurationConverter implements ConfigConverter<Duration> {
    
    @Override
    public Duration convert(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Duration value cannot be null");
        }
        if (value.startsWith("PT") || value.startsWith("P")) {
            return Duration.parse(value);
        }

        if (value.length() < 2) {
            throw new IllegalArgumentException("Invalid duration: " + value);
        }

        char unit = value.charAt(value.length() - 1);
        long amount = Long.parseLong(value.substring(0, value.length() - 1));

        return switch (Character.toLowerCase(unit)) {
            case 's' -> Duration.ofSeconds(amount);
            case 'm' -> Duration.ofMinutes(amount);
            case 'h' -> Duration.ofHours(amount);
            case 'd' -> Duration.ofDays(amount);
            default -> Duration.parse("PT" + value.toUpperCase());
        };
    }
    
    @Override
    public Class<Duration> getType() {
        return Duration.class;
    }
}
