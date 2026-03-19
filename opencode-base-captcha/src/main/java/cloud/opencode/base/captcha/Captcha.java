package cloud.opencode.base.captcha;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;

/**
 * Captcha - CAPTCHA data container
 * 验证码 - 验证码数据容器
 *
 * <p>This record holds the generated CAPTCHA data including the image,
 * answer, and metadata.</p>
 * <p>此记录保存生成的验证码数据，包括图像、答案和元数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable CAPTCHA data record - 不可变验证码数据记录</li>
 *   <li>Base64 and data URL encoding - Base64 和数据 URL 编码</li>
 *   <li>Expiration tracking - 过期跟踪</li>
 *   <li>Metadata support for extended attributes - 元数据支持扩展属性</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Captcha captcha = OpenCaptcha.create();
 * String id = captcha.id();
 * String base64Image = captcha.toBase64();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (metadata may be null) - 空值安全: 否（元数据可能为空）</li>
 * </ul>
 *
 * @param id        the unique identifier | 唯一标识符
 * @param type      the CAPTCHA type | 验证码类型
 * @param imageData the image data as bytes | 图像数据（字节数组）
 * @param answer    the correct answer | 正确答案
 * @param metadata  additional metadata | 附加元数据
 * @param createdAt the creation timestamp | 创建时间戳
 * @param expiresAt the expiration timestamp | 过期时间戳
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public record Captcha(
    String id,
    CaptchaType type,
    byte[] imageData,
    String answer,
    Map<String, Object> metadata,
    Instant createdAt,
    Instant expiresAt
) {

    /**
     * Converts the image data to Base64 string.
     * 将图像数据转换为 Base64 字符串。
     *
     * @return the Base64 encoded image | Base64 编码的图像
     */
    public String toBase64() {
        return Base64.getEncoder().encodeToString(imageData);
    }

    /**
     * Converts the image data to Base64 data URL.
     * 将图像数据转换为 Base64 数据 URL。
     *
     * @return the Base64 data URL | Base64 数据 URL
     */
    public String toBase64DataUrl() {
        String mimeType = getMimeType();
        return "data:" + mimeType + ";base64," + toBase64();
    }

    /**
     * Gets the MIME type based on CAPTCHA type.
     * 根据验证码类型获取 MIME 类型。
     *
     * @return the MIME type | MIME 类型
     */
    public String getMimeType() {
        return type == CaptchaType.GIF ? "image/gif" : "image/png";
    }

    /**
     * Checks if this CAPTCHA has expired.
     * 检查此验证码是否已过期。
     *
     * @return true if expired | 如果已过期则返回 true
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Gets a metadata value.
     * 获取元数据值。
     *
     * @param key the metadata key | 元数据键
     * @param <T> the value type | 值类型
     * @return the metadata value or null | 元数据值或 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key) {
        return metadata != null ? (T) metadata.get(key) : null;
    }

    /**
     * Gets the image width from metadata.
     * 从元数据获取图像宽度。
     *
     * @return the width | 宽度
     */
    public int getWidth() {
        Integer width = getMetadata("width");
        return width != null ? width : 0;
    }

    /**
     * Gets the image height from metadata.
     * 从元数据获取图像高度。
     *
     * @return the height | 高度
     */
    public int getHeight() {
        Integer height = getMetadata("height");
        return height != null ? height : 0;
    }
}
