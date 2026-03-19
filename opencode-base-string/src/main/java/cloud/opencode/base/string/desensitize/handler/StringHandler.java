package cloud.opencode.base.string.desensitize.handler;

import cloud.opencode.base.string.desensitize.strategy.DesensitizeStrategy;

/**
 * String Desensitize Handler - Handles desensitization of String fields.
 * 字符串脱敏处理器 - 处理String字段的脱敏操作。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Strategy-based string masking - 基于策略的字符串脱敏</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DesensitizeStrategy strategy = s -> s.substring(0, 1) + "***";
 * String masked = StringHandler.handle("secret", strategy); // "s***"
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
public final class StringHandler {
    private StringHandler() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String handle(String value, DesensitizeStrategy strategy) {
        return value != null ? strategy.desensitize(value) : null;
    }
}
