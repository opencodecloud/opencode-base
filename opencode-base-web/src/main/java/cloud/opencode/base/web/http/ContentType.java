package cloud.opencode.base.web.http;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Content Type - HTTP Content-Type Header Values
 * 内容类型 - HTTP Content-Type 头部值
 *
 * <p>This class provides common MIME types and utilities for Content-Type handling.</p>
 * <p>此类提供常见的 MIME 类型和 Content-Type 处理工具。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * String json = ContentType.APPLICATION_JSON;
 * ContentType type = ContentType.parse("text/html; charset=utf-8");
 * String mimeType = type.getMimeType();
 * Charset charset = type.getCharset();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Common MIME type constants - 常见MIME类型常量</li>
 *   <li>Content-Type header parsing - Content-Type头部解析</li>
 *   <li>Charset and boundary support - 字符集和边界支持</li>
 *   <li>Type checking methods (isJson, isXml, etc.) - 类型检查方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ContentType json = ContentType.json();
 * ContentType parsed = ContentType.parse("text/html; charset=utf-8");
 * boolean isJson = parsed.isJson();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 是（不可变）</li>
 *   <li>Null-safe: Partial (parse returns null for null input) - 部分（parse对null输入返回null）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public final class ContentType {

    // ==================== Common MIME Types ====================

    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_XML = "application/xml";
    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String APPLICATION_PDF = "application/pdf";
    public static final String APPLICATION_ZIP = "application/zip";

    public static final String TEXT_PLAIN = "text/plain";
    public static final String TEXT_HTML = "text/html";
    public static final String TEXT_XML = "text/xml";
    public static final String TEXT_CSS = "text/css";
    public static final String TEXT_JAVASCRIPT = "text/javascript";

    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    public static final String MULTIPART_MIXED = "multipart/mixed";

    public static final String IMAGE_PNG = "image/png";
    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String IMAGE_GIF = "image/gif";
    public static final String IMAGE_SVG = "image/svg+xml";
    public static final String IMAGE_WEBP = "image/webp";

    public static final String AUDIO_MPEG = "audio/mpeg";
    public static final String VIDEO_MP4 = "video/mp4";

    // ==================== Instance Fields ====================

    private final String mimeType;
    private final Charset charset;
    private final String boundary;

    private ContentType(String mimeType, Charset charset, String boundary) {
        this.mimeType = mimeType;
        this.charset = charset;
        this.boundary = boundary;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a ContentType with MIME type only.
     * 仅使用 MIME 类型创建 ContentType。
     *
     * @param mimeType the MIME type - MIME 类型
     * @return the ContentType - ContentType
     */
    public static ContentType of(String mimeType) {
        return new ContentType(mimeType, null, null);
    }

    /**
     * Creates a ContentType with MIME type and charset.
     * 使用 MIME 类型和字符集创建 ContentType。
     *
     * @param mimeType the MIME type - MIME 类型
     * @param charset  the charset - 字符集
     * @return the ContentType - ContentType
     */
    public static ContentType of(String mimeType, Charset charset) {
        return new ContentType(mimeType, charset, null);
    }

    /**
     * Creates a ContentType for JSON with UTF-8.
     * 创建 UTF-8 编码的 JSON ContentType。
     *
     * @return the ContentType - ContentType
     */
    public static ContentType json() {
        return new ContentType(APPLICATION_JSON, StandardCharsets.UTF_8, null);
    }

    /**
     * Creates a ContentType for XML with UTF-8.
     * 创建 UTF-8 编码的 XML ContentType。
     *
     * @return the ContentType - ContentType
     */
    public static ContentType xml() {
        return new ContentType(APPLICATION_XML, StandardCharsets.UTF_8, null);
    }

    /**
     * Creates a ContentType for form data.
     * 创建表单数据的 ContentType。
     *
     * @return the ContentType - ContentType
     */
    public static ContentType form() {
        return new ContentType(APPLICATION_FORM_URLENCODED, StandardCharsets.UTF_8, null);
    }

    /**
     * Creates a ContentType for multipart with boundary.
     * 使用边界创建 multipart 的 ContentType。
     *
     * @param boundary the boundary - 边界
     * @return the ContentType - ContentType
     */
    public static ContentType multipart(String boundary) {
        return new ContentType(MULTIPART_FORM_DATA, null, boundary);
    }

    /**
     * Creates a ContentType for plain text with UTF-8.
     * 创建 UTF-8 编码的纯文本 ContentType。
     *
     * @return the ContentType - ContentType
     */
    public static ContentType text() {
        return new ContentType(TEXT_PLAIN, StandardCharsets.UTF_8, null);
    }

    /**
     * Creates a ContentType for binary data.
     * 创建二进制数据的 ContentType。
     *
     * @return the ContentType - ContentType
     */
    public static ContentType binary() {
        return new ContentType(APPLICATION_OCTET_STREAM, null, null);
    }

    /**
     * Parses a Content-Type header value.
     * 解析 Content-Type 头部值。
     *
     * @param value the header value - 头部值
     * @return the ContentType - ContentType
     */
    public static ContentType parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String[] parts = value.split(";");
        String mimeType = parts[0].trim();
        Charset charset = null;
        String boundary = null;

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i].trim();
            if (startsWithIgnoreCase(part, "charset=")) {
                String charsetName = part.substring(8).trim();
                if (charsetName.startsWith("\"") && charsetName.endsWith("\"")) {
                    charsetName = charsetName.substring(1, charsetName.length() - 1);
                }
                try {
                    charset = Charset.forName(charsetName);
                } catch (Exception ignored) {
                    // Use default
                }
            } else if (startsWithIgnoreCase(part, "boundary=")) {
                boundary = part.substring(9).trim();
                if (boundary.startsWith("\"") && boundary.endsWith("\"")) {
                    boundary = boundary.substring(1, boundary.length() - 1);
                }
            }
        }

        return new ContentType(mimeType, charset, boundary);
    }

    // ==================== Getters ====================

    /**
     * Gets the MIME type.
     * 获取 MIME 类型。
     *
     * @return the MIME type - MIME 类型
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Gets the charset.
     * 获取字符集。
     *
     * @return the charset or null - 字符集或 null
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Gets the charset or default.
     * 获取字符集或默认值。
     *
     * @param defaultCharset the default charset - 默认字符集
     * @return the charset - 字符集
     */
    public Charset getCharsetOrDefault(Charset defaultCharset) {
        return charset != null ? charset : defaultCharset;
    }

    /**
     * Gets the boundary for multipart.
     * 获取 multipart 的边界。
     *
     * @return the boundary or null - 边界或 null
     */
    public String getBoundary() {
        return boundary;
    }

    // ==================== Type Checks ====================

    /**
     * Checks if this is a JSON type.
     * 检查是否是 JSON 类型。
     *
     * @return true if JSON - 如果是 JSON 返回 true
     */
    public boolean isJson() {
        return mimeType != null && (mimeType.equals(APPLICATION_JSON) ||
                mimeType.endsWith("+json"));
    }

    /**
     * Checks if this is an XML type.
     * 检查是否是 XML 类型。
     *
     * @return true if XML - 如果是 XML 返回 true
     */
    public boolean isXml() {
        return mimeType != null && (mimeType.equals(APPLICATION_XML) ||
                mimeType.equals(TEXT_XML) || mimeType.endsWith("+xml"));
    }

    /**
     * Checks if this is a text type.
     * 检查是否是文本类型。
     *
     * @return true if text - 如果是文本返回 true
     */
    public boolean isText() {
        return mimeType != null && mimeType.startsWith("text/");
    }

    /**
     * Checks if this is a multipart type.
     * 检查是否是 multipart 类型。
     *
     * @return true if multipart - 如果是 multipart 返回 true
     */
    public boolean isMultipart() {
        return mimeType != null && mimeType.startsWith("multipart/");
    }

    /**
     * Checks if this is a form type.
     * 检查是否是表单类型。
     *
     * @return true if form - 如果是表单返回 true
     */
    public boolean isForm() {
        return APPLICATION_FORM_URLENCODED.equals(mimeType);
    }

    // ==================== Internal ====================

    private static boolean startsWithIgnoreCase(String s, String prefix) {
        return s.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    // ==================== Object Methods ====================

    /**
     * Returns the Content-Type header value.
     * 返回 Content-Type 头部值。
     *
     * @return the header value - 头部值
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(mimeType);
        if (charset != null) {
            sb.append("; charset=").append(charset.name().toLowerCase());
        }
        if (boundary != null) {
            sb.append("; boundary=").append(boundary);
        }
        return sb.toString();
    }
}
