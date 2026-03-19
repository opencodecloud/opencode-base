package cloud.opencode.base.captcha.renderer;

import cloud.opencode.base.captcha.Captcha;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Captcha Renderer - Interface for CAPTCHA rendering
 * 验证码渲染器 - 验证码渲染接口
 *
 * <p>This interface defines the contract for rendering CAPTCHAs to various outputs.</p>
 * <p>此接口定义了将验证码渲染到各种输出的契约。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple output format support - 多种输出格式支持</li>
 *   <li>Stream-based rendering - 基于流的渲染</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaRenderer renderer = new ImageCaptchaRenderer();
 * renderer.render(captcha, outputStream);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (captcha and stream must not be null) - 空值安全: 否（验证码和流不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public interface CaptchaRenderer {

    /**
     * Renders the CAPTCHA to an output stream.
     * 将验证码渲染到输出流。
     *
     * @param captcha the CAPTCHA | 验证码
     * @param out     the output stream | 输出流
     * @throws IOException if rendering fails | 如果渲染失败
     */
    void render(Captcha captcha, OutputStream out) throws IOException;

    /**
     * Renders the CAPTCHA to a byte array.
     * 将验证码渲染到字节数组。
     *
     * @param captcha the CAPTCHA | 验证码
     * @return the rendered bytes | 渲染的字节
     */
    byte[] renderToBytes(Captcha captcha);

    /**
     * Renders the CAPTCHA to a Base64 string.
     * 将验证码渲染到 Base64 字符串。
     *
     * @param captcha the CAPTCHA | 验证码
     * @return the Base64 string | Base64 字符串
     */
    String renderToBase64(Captcha captcha);

    /**
     * Gets the content type of the rendered output.
     * 获取渲染输出的内容类型。
     *
     * @return the content type | 内容类型
     */
    String getContentType();

    /**
     * Creates an image renderer.
     * 创建图像渲染器。
     *
     * @return the renderer | 渲染器
     */
    static CaptchaRenderer image() {
        return new ImageCaptchaRenderer();
    }

    /**
     * Creates a GIF renderer.
     * 创建 GIF 渲染器。
     *
     * @return the renderer | 渲染器
     */
    static CaptchaRenderer gif() {
        return new GifCaptchaRenderer();
    }

    /**
     * Creates a Base64 renderer.
     * 创建 Base64 渲染器。
     *
     * @return the renderer | 渲染器
     */
    static CaptchaRenderer base64() {
        return new Base64CaptchaRenderer();
    }
}
