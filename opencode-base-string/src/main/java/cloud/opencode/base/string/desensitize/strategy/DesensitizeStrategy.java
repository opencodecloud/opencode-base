package cloud.opencode.base.string.desensitize.strategy;

/**
 * Desensitize Strategy - Functional interface for custom desensitization logic.
 * 脱敏策略 - 用于自定义脱敏逻辑的函数式接口。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Functional interface for lambda-based strategies - 函数式接口支持Lambda策略</li>
 *   <li>Custom desensitization logic - 自定义脱敏逻辑</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DesensitizeStrategy strategy = original -> original.substring(0, 1) + "***";
 * String masked = strategy.desensitize("secret"); // "s***"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@FunctionalInterface
public interface DesensitizeStrategy {
    String desensitize(String original);
}
