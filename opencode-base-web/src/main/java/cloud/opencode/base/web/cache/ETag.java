package cloud.opencode.base.web.cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * HTTP ETag (Entity Tag) generation and matching
 * HTTP ETag（实体标签）生成与匹配
 *
 * <p>Provides ETag creation, parsing, and comparison following
 * <a href="https://www.rfc-editor.org/rfc/rfc7232">RFC 7232</a> semantics,
 * including strong and weak comparison functions.</p>
 * <p>提供 ETag 创建、解析和比较功能，遵循
 * <a href="https://www.rfc-editor.org/rfc/rfc7232">RFC 7232</a> 语义，
 * 包括强比较和弱比较函数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Strong and weak ETag creation - 强/弱 ETag 创建</li>
 *   <li>Content-based ETag generation (SHA-256) - 基于内容的 ETag 生成（SHA-256）</li>
 *   <li>If-None-Match header matching (multi-value, wildcard) - If-None-Match 头部匹配</li>
 *   <li>Strong and weak comparison functions per RFC 7232 - RFC 7232 强/弱比较函数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create ETags
 * ETag strong = ETag.strong("abc123");
 * ETag weak = ETag.weak("abc123");
 * ETag fromContent = ETag.fromContent("Hello, World!");
 *
 * // Parse from header
 * ETag parsed = ETag.parse("W/\"abc123\"");
 *
 * // Match against If-None-Match
 * boolean match = etag.matches("\"abc123\", W/\"def456\"");
 *
 * // Comparison
 * boolean strongMatch = etag1.strongMatches(etag2);
 * boolean weakMatch = etag1.weakMatches(etag2);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (factory methods reject null) - 空值安全: 是（工厂方法拒绝 null）</li>
 * </ul>
 *
 * @param value the opaque tag value (without quotes) | 不含引号的标签值
 * @param weak  whether this is a weak ETag | 是否为弱 ETag
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.3
 */
public record ETag(
        String value,
        boolean weak
) {

    private static final HexFormat HEX = HexFormat.of();

    // Cache MessageDigest per thread — getInstance() involves Provider lookup + allocation
    private static final ThreadLocal<MessageDigest> SHA256_TL = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    });

    // ==================== Compact Constructor ====================

    /**
     * Compact constructor: validates that value is not null or empty.
     * 紧凑构造器：验证 value 不为 null 且不为空。
     */
    public ETag {
        Objects.requireNonNull(value, "ETag value must not be null");
        if (value.isEmpty()) {
            throw new IllegalArgumentException("ETag value must not be empty");
        }
        if (value.indexOf('"') >= 0) {
            throw new IllegalArgumentException("ETag value must not contain double-quote characters");
        }
    }

    // ==================== Factory Methods ====================

    /**
     * Create a strong ETag.
     * 创建强 ETag。
     *
     * @param value the tag value | 标签值
     * @return the strong ETag | 强 ETag
     */
    public static ETag strong(String value) {
        return new ETag(value, false);
    }

    /**
     * Create a weak ETag.
     * 创建弱 ETag。
     *
     * @param value the tag value | 标签值
     * @return the weak ETag | 弱 ETag
     */
    public static ETag weak(String value) {
        return new ETag(value, true);
    }

    /**
     * Create a strong ETag from content bytes using SHA-256 digest.
     * 使用 SHA-256 摘要从内容字节创建强 ETag。
     *
     * @param content the content bytes | 内容字节
     * @return the strong ETag with hex digest value | 使用十六进制摘要值的强 ETag
     * @throws NullPointerException if content is null | 如果内容为 null
     */
    public static ETag fromContent(byte[] content) {
        Objects.requireNonNull(content, "content must not be null");
        return new ETag(sha256Hex(content), false);
    }

    /**
     * Create a strong ETag from content string using SHA-256 digest (UTF-8).
     * 使用 SHA-256 摘要从内容字符串创建强 ETag（UTF-8）。
     *
     * @param content the content string | 内容字符串
     * @return the strong ETag with hex digest value | 使用十六进制摘要值的强 ETag
     * @throws NullPointerException if content is null | 如果内容为 null
     */
    public static ETag fromContent(String content) {
        Objects.requireNonNull(content, "content must not be null");
        return fromContent(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Parse an ETag header value (e.g., {@code "abc"} or {@code W/"abc"}).
     * 解析 ETag 头部值。
     *
     * @param headerValue the header value | 头部值
     * @return the parsed ETag | 解析后的 ETag
     * @throws IllegalArgumentException if the header value is malformed | 如果头部值格式错误
     */
    public static ETag parse(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            throw new IllegalArgumentException("ETag header value must not be null or blank");
        }
        String trimmed = headerValue.strip();
        boolean isWeak = false;
        if (trimmed.startsWith("W/")) {
            isWeak = true;
            trimmed = trimmed.substring(2);
        }
        if (trimmed.length() < 2 || trimmed.charAt(0) != '"' || trimmed.charAt(trimmed.length() - 1) != '"') {
            throw new IllegalArgumentException("Invalid ETag header value: " + headerValue);
        }
        String val = trimmed.substring(1, trimmed.length() - 1);
        return new ETag(val, isWeak);
    }

    // ==================== Instance Methods ====================

    /**
     * Return the ETag header value (e.g., {@code "abc"} or {@code W/"abc"}).
     * 返回 ETag 头部值。
     *
     * @return the header value | 头部值
     */
    public String headerValue() {
        return weak ? "W/\"" + value + "\"" : "\"" + value + "\"";
    }

    /**
     * Check if this ETag matches an If-None-Match header value.
     * 检查此 ETag 是否匹配 If-None-Match 头部值。
     *
     * <p>Supports wildcard ("*") and comma-separated multi-value headers.
     * Uses weak comparison as defined in RFC 7232 Section 3.2.</p>
     *
     * @param ifNoneMatch the If-None-Match header value | If-None-Match 头部值
     * @return true if this ETag matches | 如果匹配返回 true
     */
    public boolean matches(String ifNoneMatch) {
        if (ifNoneMatch == null || ifNoneMatch.isBlank()) {
            return false;
        }
        String trimmed = ifNoneMatch.strip();
        if ("*".equals(trimmed)) {
            return true;
        }
        String[] parts = trimmed.split(",");
        for (String part : parts) {
            String candidate = part.strip();
            if (candidate.isEmpty()) {
                continue;
            }
            try {
                ETag other = parse(candidate);
                if (weakMatches(other)) {
                    return true;
                }
            } catch (IllegalArgumentException ignored) {
                // Skip malformed entries
            }
        }
        return false;
    }

    /**
     * Strong comparison function (RFC 7232 Section 2.3.2).
     * 强比较函数（RFC 7232 第 2.3.2 节）。
     *
     * <p>Both ETags must be strong (not weak) and have the same opaque-tag value.</p>
     *
     * @param other the other ETag | 另一个 ETag
     * @return true if both are strong and equal | 如果两者都是强且相等返回 true
     */
    public boolean strongMatches(ETag other) {
        if (other == null) {
            return false;
        }
        return !this.weak && !other.weak && this.value.equals(other.value);
    }

    /**
     * Weak comparison function (RFC 7232 Section 2.3.2).
     * 弱比较函数（RFC 7232 第 2.3.2 节）。
     *
     * <p>Only the opaque-tag values need to match; weakness is ignored.</p>
     *
     * @param other the other ETag | 另一个 ETag
     * @return true if values are equal (ignoring weakness) | 如果值相等（忽略弱标记）返回 true
     */
    public boolean weakMatches(ETag other) {
        if (other == null) {
            return false;
        }
        return this.value.equals(other.value);
    }

    // ==================== toString ====================

    /**
     * Returns the header value representation.
     * 返回头部值表示。
     *
     * @return the header value | 头部值
     */
    @Override
    public String toString() {
        return headerValue();
    }

    // ==================== Internal ====================

    /**
     * Compute SHA-256 hex digest.
     */
    private static String sha256Hex(byte[] data) {
        // ThreadLocal avoids repeated Provider lookup and allocation per call
        MessageDigest md = SHA256_TL.get();
        md.reset();
        return HEX.formatHex(md.digest(data));
    }
}
