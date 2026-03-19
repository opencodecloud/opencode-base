

/**
 * OpenCode Base Captcha - Zero-dependency CAPTCHA generation library
 * OpenCode Base 验证码 - 零依赖验证码生成库
 *
 * <p>This package provides comprehensive CAPTCHA generation and validation capabilities
 * with support for multiple types including text, arithmetic, Chinese, GIF animation,
 * and interactive (slider, click, rotate) CAPTCHAs.</p>
 *
 * <p>此包提供全面的验证码生成和验证功能，支持多种类型包括文本、算术、中文、
 * GIF 动画和交互式（滑块、点击、旋转）验证码。</p>
 *
 * <h2>Main Entry Point | 主入口点</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.captcha.OpenCaptcha} - Main facade for CAPTCHA operations</li>
 * </ul>
 *
 * <h2>Core Classes | 核心类</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.captcha.Captcha} - CAPTCHA data container</li>
 *   <li>{@link cloud.opencode.base.captcha.CaptchaConfig} - Configuration for CAPTCHA generation</li>
 *   <li>{@link cloud.opencode.base.captcha.CaptchaType} - Enumeration of CAPTCHA types</li>
 *   <li>{@link cloud.opencode.base.captcha.ValidationResult} - Result of CAPTCHA validation</li>
 * </ul>
 *
 * <h2>Usage Example | 使用示例</h2>
 * <pre>{@code
 * // Simple usage
 * Captcha captcha = OpenCaptcha.create();
 * String base64Image = captcha.toBase64DataUrl();
 *
 * // With configuration
 * Captcha captcha = OpenCaptcha.create(CaptchaConfig.builder()
 *     .type(CaptchaType.ARITHMETIC)
 *     .width(200)
 *     .height(80)
 *     .build());
 *
 * // Using builder for storage and validation
 * OpenCaptcha openCaptcha = OpenCaptcha.builder()
 *     .store(CaptchaStore.memory())
 *     .build();
 *
 * Captcha captcha = openCaptcha.generate();
 * ValidationResult result = openCaptcha.validate(captcha.id(), userAnswer);
 * }</pre>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
package cloud.opencode.base.captcha;
