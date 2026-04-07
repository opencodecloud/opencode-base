package cloud.opencode.base.email;

import cloud.opencode.base.email.exception.EmailException;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Validated Email Address Value Type
 * 经过验证的邮箱地址值类型
 *
 * <p>Immutable value object representing a validated email address.</p>
 * <p>表示经过验证的邮箱地址的不可变值对象。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RFC 5321 email address validation - RFC 5321 邮箱地址验证</li>
 *   <li>Case-insensitive domain comparison - 域名部分大小写不敏感比较</li>
 *   <li>Fail-fast validation at creation - 创建时快速失败验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EmailAddress addr = EmailAddress.of("user@example.com");
 * String local = addr.localPart();   // "user"
 * String domain = addr.domain();     // "example.com"
 * String full = addr.address();      // "user@example.com"
 *
 * // Validation at creation
 * EmailAddress.of("invalid");  // throws EmailException
 *
 * // Case-insensitive domain
 * EmailAddress.of("user@EXAMPLE.COM").equals(EmailAddress.of("user@example.com")); // true
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: of() rejects null - 空值安全: of() 拒绝null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
public final class EmailAddress implements Comparable<EmailAddress> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
    );

    private static final int MAX_LENGTH = 254;

    private final String address;
    private final String localPart;
    private final String domain;

    private EmailAddress(String address, String localPart, String domain) {
        this.address = address;
        this.localPart = localPart;
        this.domain = domain;
    }

    /**
     * Create a validated EmailAddress from a string
     * 从字符串创建经过验证的EmailAddress
     *
     * @param address the email address string | 邮箱地址字符串
     * @return the validated EmailAddress | 经过验证的EmailAddress
     * @throws EmailException if the address is invalid | 地址无效时抛出
     */
    public static EmailAddress of(String address) {
        if (address == null || address.isBlank()) {
            throw new EmailException("Email address cannot be null or blank");
        }

        String trimmed = address.strip();
        if (trimmed.length() > MAX_LENGTH) {
            throw new EmailException("Email address exceeds maximum length of " + MAX_LENGTH + ": " + trimmed);
        }

        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new EmailException("Invalid email address format: " + trimmed);
        }

        int atIndex = trimmed.lastIndexOf('@');
        String localPart = trimmed.substring(0, atIndex);
        String domain = trimmed.substring(atIndex + 1).toLowerCase(Locale.ROOT);

        if (localPart.length() > 64) {
            throw new EmailException("Email local part exceeds maximum length of 64: " + localPart);
        }

        // Normalize: keep local part as-is (case-sensitive per RFC), lowercase domain
        return new EmailAddress(localPart + "@" + domain, localPart, domain);
    }

    /**
     * Check if a string is a valid email address
     * 检查字符串是否是有效的邮箱地址
     *
     * @param address the email address string | 邮箱地址字符串
     * @return true if valid | 有效返回true
     */
    public static boolean isValid(String address) {
        if (address == null || address.isBlank()) {
            return false;
        }
        String trimmed = address.strip();
        if (trimmed.length() > MAX_LENGTH) {
            return false;
        }
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            return false;
        }
        int atIndex = trimmed.lastIndexOf('@');
        return trimmed.substring(0, atIndex).length() <= 64;
    }

    /**
     * Get the full email address
     * 获取完整邮箱地址
     *
     * @return the email address | 邮箱地址
     */
    public String address() {
        return address;
    }

    /**
     * Get the local part (before @)
     * 获取本地部分（@之前）
     *
     * @return the local part | 本地部分
     */
    public String localPart() {
        return localPart;
    }

    /**
     * Get the domain part (after @, lowercased)
     * 获取域名部分（@之后，已小写化）
     *
     * @return the domain | 域名
     */
    public String domain() {
        return domain;
    }

    @Override
    public int compareTo(EmailAddress other) {
        return this.address.compareTo(other.address);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailAddress that)) return false;
        return address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    @Override
    public String toString() {
        return address;
    }
}
