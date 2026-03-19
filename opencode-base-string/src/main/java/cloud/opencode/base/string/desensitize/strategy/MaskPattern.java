package cloud.opencode.base.string.desensitize.strategy;

/**
 * Mask Pattern - Record defining masking pattern configuration.
 * 掩码模式 - 定义掩码模式配置的记录。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable start/end keep lengths - 可配置首尾保留长度</li>
 *   <li>Configurable mask character - 可配置掩码字符</li>
 *   <li>Predefined patterns (MOBILE, ID_CARD, etc.) - 预定义模式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MaskPattern pattern = new MaskPattern(3, 4, '*');
 * MaskPattern mobile = MaskPattern.MOBILE; // keep first 3, last 4
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (record is immutable) - 线程安全: 是（记录不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public record MaskPattern(int startKeep, int endKeep, char maskChar) {
    public static final MaskPattern DEFAULT = new MaskPattern(3, 4, '*');
    public static final MaskPattern MOBILE = new MaskPattern(3, 4, '*');
    public static final MaskPattern ID_CARD = new MaskPattern(6, 4, '*');
    public static final MaskPattern EMAIL = new MaskPattern(1, 0, '*');
    public static final MaskPattern BANK_CARD = new MaskPattern(4, 4, '*');
}
