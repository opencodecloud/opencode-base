package cloud.opencode.base.string.desensitize.strategy;

/**
 * Desensitize Type - Enum defining built-in desensitization types.
 * 脱敏类型 - 定义内置脱敏类型的枚举。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>MOBILE_PHONE - 手机号脱敏</li>
 *   <li>ID_CARD - 身份证号脱敏</li>
 *   <li>EMAIL - 邮箱脱敏</li>
 *   <li>BANK_CARD - 银行卡号脱敏</li>
 *   <li>CHINESE_NAME - 中文姓名脱敏</li>
 *   <li>ADDRESS, PASSWORD, CUSTOM - 地址、密码、自定义脱敏</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @Desensitize(DesensitizeType.MOBILE_PHONE)
 * private String phone;
 *
 * String masked = OpenMask.desensitize("13812345678", DesensitizeType.MOBILE_PHONE);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum is immutable) - 线程安全: 是（枚举不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public enum DesensitizeType {
    MOBILE_PHONE,
    ID_CARD,
    EMAIL,
    BANK_CARD,
    CHINESE_NAME,
    ADDRESS,
    PASSWORD,
    CUSTOM
}
