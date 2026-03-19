package cloud.opencode.base.captcha.generator;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.exception.CaptchaGenerationException;
import cloud.opencode.base.captcha.support.CaptchaNoiseUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract Captcha Generator - Base class for CAPTCHA generators
 * 抽象验证码生成器 - 验证码生成器的基类
 *
 * <p>This abstract class provides common functionality for CAPTCHA generation.</p>
 * <p>此抽象类提供验证码生成的通用功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>BufferedImage creation and graphics setup - BufferedImage 创建和图形设置</li>
 *   <li>Noise drawing utilities - 噪点绘制工具</li>
 *   <li>Image-to-bytes conversion - 图像到字节转换</li>
 *   <li>ID generation and metadata creation - ID 生成和元数据创建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class CustomGenerator extends AbstractCaptchaGenerator implements CaptchaGenerator {
 *     public Captcha generate(CaptchaConfig config) {
 *         BufferedImage image = createImage(config);
 *         Graphics2D g = createGraphics(image, config);
 *         // custom rendering...
 *         byte[] data = toBytes(image);
 *         return buildCaptcha(CaptchaType.ALPHANUMERIC, data, code, config);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless, no shared mutable state) - 线程安全: 是（无状态，无共享可变状态）</li>
 *   <li>Null-safe: No (parameters must be non-null) - 空值安全: 否（参数不能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public abstract class AbstractCaptchaGenerator {

    /**
     * Creates a buffered image with the specified configuration.
     * 使用指定配置创建缓冲图像。
     *
     * @param config the configuration | 配置
     * @return the buffered image | 缓冲图像
     */
    protected BufferedImage createImage(CaptchaConfig config) {
        return new BufferedImage(
            config.getWidth(),
            config.getHeight(),
            BufferedImage.TYPE_INT_RGB
        );
    }

    /**
     * Creates graphics for the image.
     * 为图像创建图形。
     *
     * @param image  the image | 图像
     * @param config the configuration | 配置
     * @return the graphics | 图形
     */
    protected Graphics2D createGraphics(BufferedImage image, CaptchaConfig config) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fill background
        g.setColor(config.getBackgroundColor());
        g.fillRect(0, 0, config.getWidth(), config.getHeight());

        return g;
    }

    /**
     * Draws noise on the image.
     * 在图像上绘制噪点。
     *
     * @param g      the graphics | 图形
     * @param config the configuration | 配置
     */
    protected void drawNoise(Graphics2D g, CaptchaConfig config) {
        CaptchaNoiseUtil.drawNoiseLines(g, config);
        CaptchaNoiseUtil.drawNoiseDots(g, config);
    }

    /**
     * Converts image to PNG bytes.
     * 将图像转换为 PNG 字节。
     *
     * @param image the image | 图像
     * @return the PNG bytes | PNG 字节
     */
    protected byte[] toBytes(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new CaptchaGenerationException("Failed to convert image to bytes", e);
        }
    }

    /**
     * Generates a unique CAPTCHA ID.
     * 生成唯一的验证码 ID。
     *
     * @return the unique ID | 唯一 ID
     */
    protected String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Creates metadata for the CAPTCHA.
     * 为验证码创建元数据。
     *
     * @param config the configuration | 配置
     * @return the metadata | 元数据
     */
    protected Map<String, Object> createMetadata(CaptchaConfig config) {
        return Map.of(
            "width", config.getWidth(),
            "height", config.getHeight(),
            "length", config.getLength()
        );
    }

    /**
     * Builds a CAPTCHA from generated data.
     * 从生成的数据构建验证码。
     *
     * @param type      the CAPTCHA type | 验证码类型
     * @param imageData the image data | 图像数据
     * @param answer    the answer | 答案
     * @param config    the configuration | 配置
     * @return the CAPTCHA | 验证码
     */
    protected Captcha buildCaptcha(CaptchaType type, byte[] imageData, String answer, CaptchaConfig config) {
        Instant now = Instant.now();
        return new Captcha(
            generateId(),
            type,
            imageData,
            answer,
            createMetadata(config),
            now,
            now.plus(config.getExpireTime())
        );
    }
}
