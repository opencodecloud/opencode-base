package cloud.opencode.base.captcha.renderer;

import cloud.opencode.base.captcha.Captcha;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

/**
 * Audio Captcha Renderer - Renders CAPTCHA as WAV audio
 * 音频验证码渲染器 - 将验证码渲染为 WAV 音频
 *
 * <p>This renderer outputs the pre-generated WAV audio data from the CAPTCHA.
 * The audio data is stored in the {@code imageData} field of the {@link Captcha} record.</p>
 * <p>此渲染器输出验证码中预生成的 WAV 音频数据。
 * 音频数据存储在 {@link Captcha} 记录的 {@code imageData} 字段中。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>WAV audio output - WAV 音频输出</li>
 *   <li>Stream-based rendering - 基于流的渲染</li>
 *   <li>Base64 encoding support - Base64 编码支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaRenderer renderer = new AudioCaptchaRenderer();
 * renderer.render(captcha, outputStream);
 * String base64 = renderer.renderToBase64(captcha);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: No (captcha and stream must not be null) - 空值安全: 否（验证码和流不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
public final class AudioCaptchaRenderer implements CaptchaRenderer {

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
        return "audio/wav";
    }
}
