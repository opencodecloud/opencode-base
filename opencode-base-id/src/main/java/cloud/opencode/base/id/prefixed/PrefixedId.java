package cloud.opencode.base.id.prefixed;

import cloud.opencode.base.id.exception.OpenIdGenerationException;

import java.util.regex.Pattern;

/**
 * Prefixed ID - Type-safe ID with entity-type prefix (Stripe/TypeID style)
 * 带前缀的ID - 带实体类型前缀的类型安全ID（Stripe/TypeID风格）
 *
 * <p>Represents an immutable prefixed identifier in the style popularized by Stripe
 * (e.g., {@code cus_abc123}, {@code inv_xyz456}) and TypeID. The prefix encodes the
 * entity type, making IDs self-documenting in logs, APIs, and debugging sessions.</p>
 * <p>表示不可变的带前缀标识符，风格参考Stripe（如{@code cus_abc123}、{@code inv_xyz456}）
 * 和TypeID。前缀编码实体类型，使ID在日志、API和调试中具有自描述性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Entity-type prefix for self-documenting IDs - 实体类型前缀，ID自描述</li>
 *   <li>Prefix validation (lowercase letters/digits/underscores) - 前缀验证（小写字母/数字/下划线）</li>
 *   <li>Parse from string with {@link #fromString(String)} - 通过fromString解析字符串</li>
 *   <li>Works with any underlying ID format - 兼容任何底层ID格式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a prefixed ID
 * PrefixedId userId = PrefixedId.of("usr", "01ARZ3NDEKTSV4RRFFQ69G5FAV");
 * System.out.println(userId);  // "usr_01ARZ3NDEKTSV4RRFFQ69G5FAV"
 *
 * // Parse from string
 * PrefixedId parsed = PrefixedId.fromString("order_7ZYQP4T89A");
 * System.out.println(parsed.prefix());  // "order"
 * System.out.println(parsed.rawId());   // "7ZYQP4T89A"
 *
 * // Validate
 * boolean valid = PrefixedId.isValid("usr_01ARZ3NDEK");  // true
 * boolean bad   = PrefixedId.isValid("User_123");         // false (uppercase prefix)
 * }</pre>
 *
 * <p><strong>Prefix Rules | 前缀规则:</strong></p>
 * <ul>
 *   <li>Must match {@code [a-z][a-z0-9_]{0,30}} — lowercase start, max 31 chars total</li>
 *   <li>Must start with a lowercase letter (not digit or underscore)</li>
 *   <li>May contain lowercase letters, digits, and underscores</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (throws on null) - 空值安全: 否（空值抛异常）</li>
 * </ul>
 *
 * @param prefix the entity-type prefix (e.g., "usr", "order") | 实体类型前缀（如"usr"、"order"）
 * @param rawId  the underlying ID string | 底层ID字符串
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.3
 */
public record PrefixedId(String prefix, String rawId) {

    /** Valid prefix pattern: lowercase letter, then lowercase letters/digits/underscores, max 31 chars total
     *  有效前缀模式：小写字母开头，后接小写字母/数字/下划线，最多31字符 */
    static final Pattern PREFIX_PATTERN = Pattern.compile("[a-z][a-z0-9_]{0,30}");

    /**
     * Compact canonical constructor with validation
     * 带验证的紧凑规范构造方法
     */
    public PrefixedId {
        if (prefix == null || prefix.isEmpty()) {
            throw OpenIdGenerationException.invalidPrefix(prefix == null ? "null" : "");
        }
        if (!PREFIX_PATTERN.matcher(prefix).matches()) {
            throw OpenIdGenerationException.invalidPrefix(prefix);
        }
        if (rawId == null || rawId.isEmpty()) {
            throw OpenIdGenerationException.invalidIdFormat("PrefixedId",
                    "rawId must not be null or empty");
        }
    }

    /**
     * Creates a PrefixedId from a prefix and raw ID
     * 从前缀和原始ID创建PrefixedId
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * PrefixedId.of("usr", "01ARZ3NDEK")   = PrefixedId[prefix=usr, rawId=01ARZ3NDEK]
     * PrefixedId.of("order_item", "abc123") = PrefixedId[prefix=order_item, rawId=abc123]
     * </pre>
     *
     * @param prefix the entity-type prefix | 实体类型前缀
     * @param rawId  the underlying ID | 底层ID
     * @return PrefixedId instance | PrefixedId实例
     * @throws OpenIdGenerationException if prefix is invalid or rawId is null/empty | 前缀无效或rawId为空时抛出
     */
    public static PrefixedId of(String prefix, String rawId) {
        return new PrefixedId(prefix, rawId);
    }

    /**
     * Parses a full prefixed ID string (e.g., "usr_01ARZ3NDEK")
     * 解析完整的带前缀ID字符串（如"usr_01ARZ3NDEK"）
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * PrefixedId.fromString("usr_01ARZ3NDEK")   → prefix="usr",  rawId="01ARZ3NDEK"
     * PrefixedId.fromString("order_abc_123")    → prefix="order", rawId="abc_123"
     * </pre>
     *
     * @param prefixedId the full prefixed ID string | 完整的带前缀ID字符串
     * @return parsed PrefixedId | 解析后的PrefixedId
     * @throws OpenIdGenerationException if the format is invalid | 格式无效时抛出
     */
    public static PrefixedId fromString(String prefixedId) {
        if (prefixedId == null || prefixedId.isEmpty()) {
            throw OpenIdGenerationException.invalidIdFormat("PrefixedId",
                    "prefixedId must not be null or empty");
        }
        int idx = prefixedId.indexOf('_');
        if (idx <= 0) {
            throw OpenIdGenerationException.invalidIdFormat("PrefixedId",
                    prefixedId + " (expected format: '<prefix>_<id>')");
        }
        String prefix = prefixedId.substring(0, idx);
        String rawId = prefixedId.substring(idx + 1);
        return new PrefixedId(prefix, rawId);
    }

    /**
     * Validates whether a string is a valid prefixed ID
     * 验证字符串是否是有效的带前缀ID
     *
     * @param prefixedId the string to validate | 要验证的字符串
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(String prefixedId) {
        if (prefixedId == null || prefixedId.isEmpty()) {
            return false;
        }
        int idx = prefixedId.indexOf('_');
        if (idx <= 0 || idx >= prefixedId.length() - 1) {
            return false;
        }
        String prefix = prefixedId.substring(0, idx);
        return PREFIX_PATTERN.matcher(prefix).matches();
    }

    /**
     * Returns the full prefixed ID string (prefix + "_" + rawId)
     * 返回完整的带前缀ID字符串（前缀 + "_" + 原始ID）
     *
     * @return full ID string | 完整ID字符串
     */
    public String fullId() {
        return prefix + "_" + rawId;
    }

    /**
     * Returns the full prefixed ID string
     * 返回完整的带前缀ID字符串
     *
     * @return full ID string | 完整ID字符串
     */
    @Override
    public String toString() {
        return fullId();
    }
}
