package cloud.opencode.base.web.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * HTTP Media Type (MIME Type) representation and Accept header parsing
 * HTTP 媒体类型（MIME 类型）表示和 Accept 头部解析
 *
 * <p>Provides RFC 7231 compliant media type handling including quality factor
 * parsing, content negotiation, and wildcard matching.</p>
 * <p>提供符合 RFC 7231 的媒体类型处理，包括质量因子解析、内容协商和通配符匹配。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Media type parsing with parameters - 带参数的媒体类型解析</li>
 *   <li>Accept header parsing with quality factor sorting - Accept 头部解析与质量因子排序</li>
 *   <li>Content negotiation (bestMatch) - 内容协商（最佳匹配）</li>
 *   <li>Wildcard support (* / *) - 通配符支持</li>
 *   <li>Common media type constants - 常见媒体类型常量</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Parse a single media type
 * MediaType json = MediaType.parse("application/json; charset=utf-8");
 *
 * // Parse Accept header
 * List<MediaType> accepted = MediaType.parseAccept("text/html, application/json;q=0.9");
 *
 * // Content negotiation
 * Optional<MediaType> best = MediaType.bestMatch(accepted,
 *     List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (factory methods reject null) - 空值安全: 是（工厂方法拒绝 null）</li>
 * </ul>
 *
 * @param type       the primary type (e.g., "application") | 主类型
 * @param subtype    the sub-type (e.g., "json") | 子类型
 * @param parameters the media type parameters (e.g., charset, q) | 媒体类型参数
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.3
 */
public record MediaType(
        String type,
        String subtype,
        Map<String, String> parameters
) {

    // ==================== Constants ====================

    /**
     * application/json
     */
    public static final MediaType APPLICATION_JSON = new MediaType("application", "json", Map.of());

    /**
     * application/xml
     */
    public static final MediaType APPLICATION_XML = new MediaType("application", "xml", Map.of());

    /**
     * text/plain
     */
    public static final MediaType TEXT_PLAIN = new MediaType("text", "plain", Map.of());

    /**
     * text/html
     */
    public static final MediaType TEXT_HTML = new MediaType("text", "html", Map.of());

    /**
     * application/octet-stream
     */
    public static final MediaType APPLICATION_OCTET_STREAM = new MediaType("application", "octet-stream", Map.of());

    /**
     * Wildcard media type: *&#47;*
     */
    public static final MediaType ALL = new MediaType("*", "*", Map.of());

    // ==================== Compact Constructor ====================

    /**
     * Compact constructor: normalizes type/subtype to lowercase, defensively copies parameters.
     * 紧凑构造器：将类型/子类型规范化为小写，防御性复制参数。
     */
    public MediaType {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(subtype, "subtype must not be null");
        type = type.strip().toLowerCase(Locale.ROOT);
        subtype = subtype.strip().toLowerCase(Locale.ROOT);
        if (type.isEmpty()) {
            throw new IllegalArgumentException("type must not be empty");
        }
        if (subtype.isEmpty()) {
            throw new IllegalArgumentException("subtype must not be empty");
        }
        parameters = (parameters == null || parameters.isEmpty())
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(parameters));
    }

    // ==================== Factory Methods ====================

    /**
     * Create a MediaType with type and subtype only.
     * 仅使用主类型和子类型创建 MediaType。
     *
     * @param type    the primary type | 主类型
     * @param subtype the sub-type | 子类型
     * @return the media type | 媒体类型
     */
    public static MediaType of(String type, String subtype) {
        return new MediaType(type, subtype, Map.of());
    }

    /**
     * Create a MediaType with type, subtype, and parameters.
     * 使用主类型、子类型和参数创建 MediaType。
     *
     * @param type    the primary type | 主类型
     * @param subtype the sub-type | 子类型
     * @param params  the parameters | 参数
     * @return the media type | 媒体类型
     */
    public static MediaType of(String type, String subtype, Map<String, String> params) {
        return new MediaType(type, subtype, params);
    }

    /**
     * Parse a single media type string (e.g., "application/json; charset=utf-8").
     * 解析单个媒体类型字符串。
     *
     * @param value the media type string | 媒体类型字符串
     * @return the parsed media type | 解析后的媒体类型
     * @throws IllegalArgumentException if value is null, blank, or malformed | 如果值为 null、空白或格式错误
     */
    public static MediaType parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("media type string must not be null or blank");
        }
        return parseOne(value.strip());
    }

    /**
     * Parse an HTTP Accept header into a list of media types sorted by quality factor (descending).
     * 将 HTTP Accept 头部解析为按质量因子降序排列的媒体类型列表。
     *
     * @param acceptHeader the Accept header value | Accept 头部值
     * @return the sorted list of media types | 排序后的媒体类型列表
     * @throws IllegalArgumentException if acceptHeader is null or blank | 如果头部值为 null 或空白
     */
    public static List<MediaType> parseAccept(String acceptHeader) {
        if (acceptHeader == null || acceptHeader.isBlank()) {
            throw new IllegalArgumentException("Accept header must not be null or blank");
        }
        String[] segments = acceptHeader.split(",");
        List<MediaType> result = new ArrayList<>(segments.length);
        for (String segment : segments) {
            String trimmed = segment.strip();
            if (!trimmed.isEmpty()) {
                result.add(parseOne(trimmed));
            }
        }
        result.sort(Comparator.comparingDouble(MediaType::getQuality).reversed());
        return Collections.unmodifiableList(result);
    }

    /**
     * Find the best match from available media types given acceptable types.
     * 根据可接受的类型从可用媒体类型中找到最佳匹配。
     *
     * <p>Iterates acceptable types in quality-factor order and returns the first
     * available type that is included by an acceptable type.</p>
     * <p>按质量因子顺序遍历可接受类型，返回第一个被可接受类型包含的可用类型。</p>
     *
     * @param acceptable the acceptable media types (from Accept header) | 可接受的媒体类型
     * @param available  the available media types (server can produce) | 可用的媒体类型
     * @return the best match, or empty if none | 最佳匹配，如果没有则为空
     */
    public static Optional<MediaType> bestMatch(List<MediaType> acceptable, List<MediaType> available) {
        Objects.requireNonNull(acceptable, "acceptable must not be null");
        Objects.requireNonNull(available, "available must not be null");
        for (MediaType accept : acceptable) {
            for (MediaType avail : available) {
                if (accept.includes(avail)) {
                    return Optional.of(avail);
                }
            }
        }
        return Optional.empty();
    }

    // ==================== Instance Methods ====================

    /**
     * Get the quality factor (q parameter), defaulting to 1.0.
     * 获取质量因子（q 参数），默认 1.0。
     *
     * @return the quality factor [0.0, 1.0] | 质量因子
     */
    public double getQuality() {
        String q = parameters.get("q");
        if (q == null) {
            return 1.0;
        }
        try {
            double value = Double.parseDouble(q);
            return (value < 0.0) ? 0.0 : Math.min(value, 1.0);
        } catch (NumberFormatException e) {
            // Malformed q-value: deprioritize rather than promote
            return 0.0;
        }
    }

    /**
     * Check if this media type includes the other (wildcard matching).
     * 检查此媒体类型是否包含另一个（通配符匹配）。
     *
     * <p>A wildcard type includes all types. A wildcard subtype includes all subtypes
     * of the same primary type.</p>
     *
     * @param other the other media type | 另一个媒体类型
     * @return true if this type includes the other | 如果包含返回 true
     */
    public boolean includes(MediaType other) {
        if (other == null) {
            return false;
        }
        if (isWildcard()) {
            return true;
        }
        if (!this.type.equals(other.type)) {
            return false;
        }
        return isWildcardSubtype() || this.subtype.equals(other.subtype);
    }

    /**
     * Check if this is the wildcard type (*&#47;*).
     * 检查是否为通配符类型。
     *
     * @return true if wildcard | 如果为通配符返回 true
     */
    public boolean isWildcard() {
        return "*".equals(type) && "*".equals(subtype);
    }

    /**
     * Check if the subtype is a wildcard.
     * 检查子类型是否为通配符。
     *
     * @return true if subtype is wildcard | 如果子类型为通配符返回 true
     */
    public boolean isWildcardSubtype() {
        return "*".equals(subtype);
    }

    /**
     * Get the MIME type string ("type/subtype").
     * 获取 MIME 类型字符串（"type/subtype"）。
     *
     * @return the MIME type | MIME 类型
     */
    public String mimeType() {
        return type + "/" + subtype;
    }

    /**
     * Get the charset parameter value.
     * 获取 charset 参数值。
     *
     * @return the charset, or empty if not present | 字符集，如果不存在则为空
     */
    public Optional<String> getCharset() {
        return Optional.ofNullable(parameters.get("charset"));
    }

    // ==================== Internal Parsing ====================

    /**
     * Parse a single media type segment.
     * 解析单个媒体类型片段。
     */
    private static MediaType parseOne(String segment) {
        String[] parts = segment.split(";");
        String mimeTypePart = parts[0].strip();
        int slash = mimeTypePart.indexOf('/');
        if (slash < 0) {
            throw new IllegalArgumentException("Invalid media type: " + segment);
        }
        String typePart = mimeTypePart.substring(0, slash).strip();
        String subtypePart = mimeTypePart.substring(slash + 1).strip();
        if (typePart.isEmpty() || subtypePart.isEmpty()) {
            throw new IllegalArgumentException("Invalid media type: " + segment);
        }

        Map<String, String> params = Map.of();
        if (parts.length > 1) {
            params = new LinkedHashMap<>();
            for (int i = 1; i < parts.length; i++) {
                String param = parts[i].strip();
                int eq = param.indexOf('=');
                if (eq > 0) {
                    String key = param.substring(0, eq).strip().toLowerCase(Locale.ROOT);
                    String val = param.substring(eq + 1).strip();
                    // Remove surrounding quotes
                    if (val.length() >= 2 && val.startsWith("\"") && val.endsWith("\"")) {
                        val = val.substring(1, val.length() - 1);
                    }
                    params.put(key, val);
                }
            }
        }
        return new MediaType(typePart, subtypePart, params);
    }

    // ==================== toString ====================

    /**
     * Return the full media type string including parameters.
     * 返回包含参数的完整媒体类型字符串。
     *
     * @return the string representation | 字符串表示
     */
    @Override
    public String toString() {
        if (parameters.isEmpty()) {
            return type + "/" + subtype;
        }
        StringBuilder sb = new StringBuilder(type.length() + 1 + subtype.length() + 32);
        sb.append(type).append('/').append(subtype);
        parameters.forEach((k, v) -> sb.append("; ").append(k).append('=').append(v));
        return sb.toString();
    }
}
