package cloud.opencode.base.captcha.renderer;

import cloud.opencode.base.captcha.Captcha;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 Captcha Renderer - Renders CAPTCHA as Base64 data URL
 * Base64 验证码渲染器 - 将验证码渲染为 Base64 数据 URL
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Base64 data URL output - Base64数据URL输出</li>
 *   <li>Embeddable in HTML - 可嵌入HTML</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaRenderer renderer = new Base64CaptchaRenderer();
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
public final class Base64CaptchaRenderer implements CaptchaRenderer {

    @Override
    public void render(Captcha captcha, OutputStream out) throws IOException {
        String dataUrl = captcha.toBase64DataUrl();
        out.write(dataUrl.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public byte[] renderToBytes(Captcha captcha) {
        return captcha.toBase64DataUrl().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String renderToBase64(Captcha captcha) {
        return captcha.toBase64DataUrl();
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }
}
