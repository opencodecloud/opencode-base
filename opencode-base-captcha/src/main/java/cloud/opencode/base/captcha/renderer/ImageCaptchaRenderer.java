package cloud.opencode.base.captcha.renderer;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.exception.CaptchaException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

/**
 * Image Captcha Renderer - Renders CAPTCHA as PNG image
 * 图像验证码渲染器 - 将验证码渲染为 PNG 图像
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>PNG image output - PNG图像输出</li>
 *   <li>Binary image rendering - 二进制图像渲染</li>
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
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: No (captcha must not be null) - 空值安全: 否（验证码不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class ImageCaptchaRenderer implements CaptchaRenderer {

    @Override
    public void render(Captcha captcha, OutputStream out) throws IOException {
        out.write(captcha.imageData());
    }

    @Override
    public byte[] renderToBytes(Captcha captcha) {
        return captcha.imageData();
    }

    @Override
    public String renderToBase64(Captcha captcha) {
        return Base64.getEncoder().encodeToString(captcha.imageData());
    }

    @Override
    public String getContentType() {
        return "image/png";
    }
}
