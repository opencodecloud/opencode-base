package cloud.opencode.base.string.desensitize.handler;

/**
 * Number Desensitize Handler - Handles desensitization of Number fields.
 * 数字脱敏处理器 - 处理数字字段的脱敏操作。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Numeric value masking - 数值掩码</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String masked = NumberHandler.handle(12345); // "***"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class NumberHandler {
    private NumberHandler() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String handle(Number value) {
        return value != null ? "***" : null;
    }
}
