
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Mask - Data Masking for Sensitive Fields
 * JSON 脱敏 - 敏感字段的数据脱敏
 *
 * <p>This annotation marks a field for data masking during serialization.
 * It supports various masking strategies for sensitive data like passwords,
 * phone numbers, ID cards, and email addresses.</p>
 * <p>此注解标记字段在序列化时进行数据脱敏。支持多种脱敏策略，
 * 用于密码、手机号、身份证号和电子邮件等敏感数据。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class User {
 *     @JsonMask(type = MaskType.PASSWORD)
 *     private String password;       // -> "******"
 *
 *     @JsonMask(type = MaskType.PHONE)
 *     private String phone;          // "13812345678" -> "138****5678"
 *
 *     @JsonMask(type = MaskType.ID_CARD)
 *     private String idCard;         // "110101199001011234" -> "110***********1234"
 *
 *     @JsonMask(type = MaskType.EMAIL)
 *     private String email;          // "test@example.com" -> "t***@example.com"
 *
 *     @JsonMask(type = MaskType.CUSTOM, pattern = "(?<=.{2}).(?=.{2})")
 *     private String customField;    // Custom regex masking
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple built-in masking strategies (phone, email, ID card, etc.) - 多种内置脱敏策略</li>
 *   <li>Custom regex-based masking support - 自定义正则表达式脱敏支持</li>
 *   <li>Configurable mask character and prefix/suffix lengths - 可配置脱敏字符和前缀/后缀长度</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonMask {

    /**
     * Mask type enumeration
     * 脱敏类型枚举
     */
    enum MaskType {
        /**
         * Password masking - all characters replaced with *.
         * 密码脱敏 - 所有字符替换为 *。
         * <p>Example: "password123" -> "******"</p>
         */
        PASSWORD,

        /**
         * Phone number masking - middle 4 digits hidden.
         * 手机号脱敏 - 中间4位隐藏。
         * <p>Example: "13812345678" -> "138****5678"</p>
         */
        PHONE,

        /**
         * ID card masking - middle digits hidden.
         * 身份证号脱敏 - 中间位数隐藏。
         * <p>Example: "110101199001011234" -> "110***********1234"</p>
         */
        ID_CARD,

        /**
         * Email masking - username partially hidden.
         * 邮箱脱敏 - 用户名部分隐藏。
         * <p>Example: "test@example.com" -> "t***@example.com"</p>
         */
        EMAIL,

        /**
         * Bank card masking - middle digits hidden.
         * 银行卡脱敏 - 中间位数隐藏。
         * <p>Example: "6222021234567890123" -> "6222****0123"</p>
         */
        BANK_CARD,

        /**
         * Name masking - surname visible, given name hidden.
         * 姓名脱敏 - 姓可见，名隐藏。
         * <p>Example: "张三丰" -> "张**"</p>
         */
        NAME,

        /**
         * Address masking - detailed address hidden.
         * 地址脱敏 - 详细地址隐藏。
         * <p>Example: "北京市朝阳区xxx街道" -> "北京市朝阳区****"</p>
         */
        ADDRESS,

        /**
         * Custom masking with regex pattern.
         * 使用正则表达式的自定义脱敏。
         */
        CUSTOM,

        /**
         * Full masking - entire value hidden.
         * 完全脱敏 - 整个值隐藏。
         * <p>Example: "anything" -> "******"</p>
         */
        FULL
    }

    /**
     * The masking type.
     * 脱敏类型。
     *
     * @return the mask type - 脱敏类型
     */
    MaskType type() default MaskType.FULL;

    /**
     * Custom regex pattern for CUSTOM mask type.
     * 用于 CUSTOM 脱敏类型的自定义正则表达式。
     *
     * @return the pattern - 模式
     */
    String pattern() default "";

    /**
     * Replacement character for masking.
     * 脱敏替换字符。
     *
     * @return the mask character - 脱敏字符
     */
    char maskChar() default '*';

    /**
     * Number of visible characters at the start.
     * 开头可见的字符数。
     *
     * @return the prefix length - 前缀长度
     */
    int prefixLength() default -1;

    /**
     * Number of visible characters at the end.
     * 结尾可见的字符数。
     *
     * @return the suffix length - 后缀长度
     */
    int suffixLength() default -1;

    /**
     * Whether masking is enabled (can be controlled at runtime).
     * 是否启用脱敏（可在运行时控制）。
     *
     * @return true if masking is enabled - 如果启用脱敏则返回 true
     */
    boolean enabled() default true;
}
