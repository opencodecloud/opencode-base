package cloud.opencode.base.captcha.generator;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.interactive.ClickCaptchaGenerator;
import cloud.opencode.base.captcha.interactive.ImageSelectCaptchaGenerator;
import cloud.opencode.base.captcha.interactive.JigsawCaptchaGenerator;
import cloud.opencode.base.captcha.interactive.RotateCaptchaGenerator;
import cloud.opencode.base.captcha.interactive.SliderCaptchaGenerator;

/**
 * Captcha Generator - Sealed interface for CAPTCHA generation
 * 验证码生成器 - 验证码生成的密封接口
 *
 * <p>This sealed interface defines the contract for CAPTCHA generators.
 * All generators must implement this interface.</p>
 * <p>此密封接口定义了验证码生成器的契约。
 * 所有生成器都必须实现此接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sealed interface ensuring all generators are known at compile time - 密封接口确保编译时已知所有生成器</li>
 *   <li>Factory method for type-based generator creation - 基于类型的生成器创建工厂方法</li>
 *   <li>Convenience factory methods for common types - 常用类型的便捷工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaGenerator generator = CaptchaGenerator.forType(CaptchaType.ALPHANUMERIC);
 * Captcha captcha = generator.generate(CaptchaConfig.defaults());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (config must be non-null) - 空值安全: 否（config 不能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public sealed interface CaptchaGenerator
    permits ImageCaptchaGenerator, ArithmeticCaptchaGenerator, ChineseCaptchaGenerator,
            GifCaptchaGenerator, SpecCaptchaGenerator, PowCaptchaGenerator,
            AudioCaptchaGenerator, TestCaptchaGenerator,
            SliderCaptchaGenerator, ClickCaptchaGenerator, RotateCaptchaGenerator,
            ImageSelectCaptchaGenerator, JigsawCaptchaGenerator {

    /**
     * Generates a CAPTCHA with default configuration.
     * 使用默认配置生成验证码。
     *
     * @return the generated CAPTCHA | 生成的验证码
     */
    default Captcha generate() {
        return generate(CaptchaConfig.defaults());
    }

    /**
     * Generates a CAPTCHA with the specified configuration.
     * 使用指定配置生成验证码。
     *
     * @param config the configuration | 配置
     * @return the generated CAPTCHA | 生成的验证码
     */
    Captcha generate(CaptchaConfig config);

    /**
     * Gets the supported CAPTCHA type.
     * 获取支持的验证码类型。
     *
     * @return the CAPTCHA type | 验证码类型
     */
    CaptchaType getType();

    /**
     * Creates a generator for the specified type.
     * 为指定类型创建生成器。
     *
     * @param type the CAPTCHA type | 验证码类型
     * @return the generator | 生成器
     */
    static CaptchaGenerator forType(CaptchaType type) {
        return switch (type) {
            case NUMERIC, ALPHA, ALPHANUMERIC -> new ImageCaptchaGenerator(type);
            case ARITHMETIC -> new ArithmeticCaptchaGenerator();
            case CHINESE -> new ChineseCaptchaGenerator();
            case GIF -> new GifCaptchaGenerator();
            case SLIDER -> new SliderCaptchaGenerator();
            case CLICK -> new ClickCaptchaGenerator();
            case ROTATE -> new RotateCaptchaGenerator();
            case IMAGE_SELECT -> new ImageSelectCaptchaGenerator();
            case POW -> new PowCaptchaGenerator();
            case AUDIO -> new AudioCaptchaGenerator();
            case JIGSAW -> new JigsawCaptchaGenerator();
        };
    }

    /**
     * Creates a numeric CAPTCHA generator.
     * 创建数字验证码生成器。
     *
     * @return the generator | 生成器
     */
    static CaptchaGenerator numeric() {
        return new ImageCaptchaGenerator(CaptchaType.NUMERIC);
    }

    /**
     * Creates an alphabetic CAPTCHA generator.
     * 创建字母验证码生成器。
     *
     * @return the generator | 生成器
     */
    static CaptchaGenerator alpha() {
        return new ImageCaptchaGenerator(CaptchaType.ALPHA);
    }

    /**
     * Creates an alphanumeric CAPTCHA generator.
     * 创建字母数字验证码生成器。
     *
     * @return the generator | 生成器
     */
    static CaptchaGenerator alphanumeric() {
        return new ImageCaptchaGenerator(CaptchaType.ALPHANUMERIC);
    }

    /**
     * Creates an arithmetic CAPTCHA generator.
     * 创建算术验证码生成器。
     *
     * @return the generator | 生成器
     */
    static CaptchaGenerator arithmetic() {
        return new ArithmeticCaptchaGenerator();
    }

    /**
     * Creates a Chinese CAPTCHA generator.
     * 创建中文验证码生成器。
     *
     * @return the generator | 生成器
     */
    static CaptchaGenerator chinese() {
        return new ChineseCaptchaGenerator();
    }

    /**
     * Creates a GIF CAPTCHA generator.
     * 创建 GIF 验证码生成器。
     *
     * @return the generator | 生成器
     */
    static CaptchaGenerator gif() {
        return new GifCaptchaGenerator();
    }

    /**
     * Creates an audio CAPTCHA generator.
     * 创建音频验证码生成器。
     *
     * @return the generator | 生成器
     */
    static CaptchaGenerator audio() {
        return new AudioCaptchaGenerator();
    }

    /**
     * Creates a Proof-of-Work CAPTCHA generator.
     * 创建工作量证明验证码生成器。
     *
     * @return the generator | 生成器
     */
    static CaptchaGenerator pow() {
        return new PowCaptchaGenerator();
    }
}
