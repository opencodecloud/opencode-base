package cloud.opencode.base.captcha.support;

import cloud.opencode.base.captcha.CaptchaConfig;

/**
 * Captcha Strength - CAPTCHA difficulty levels
 * 验证码强度 - 验证码难度级别
 *
 * <p>This enum defines difficulty levels for CAPTCHA generation.</p>
 * <p>此枚举定义了验证码生成的难度级别。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Four difficulty levels (EASY, MEDIUM, HARD, EXTREME) - 四个难度级别</li>
 *   <li>Configurable noise, line, and length parameters - 可配置噪点、线条和长度参数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaStrength strength = CaptchaStrength.MEDIUM;
 * CaptchaConfig config = strength.toConfig();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public enum CaptchaStrength {

    /**
     * Easy - Minimal noise, clear text
     * 简单 - 最少噪点，清晰文本
     */
    EASY(3, 3, 20),

    /**
     * Medium - Moderate noise and distortion
     * 中等 - 适中噪点和干扰
     */
    MEDIUM(5, 50, 32),

    /**
     * Hard - Heavy noise and distortion
     * 困难 - 大量噪点和干扰
     */
    HARD(8, 100, 28),

    /**
     * Extreme - Maximum noise and distortion
     * 极难 - 最大噪点和干扰
     */
    EXTREME(12, 200, 26);

    private final int noiseLines;
    private final int noiseDots;
    private final float fontSize;

    CaptchaStrength(int noiseLines, int noiseDots, float fontSize) {
        this.noiseLines = noiseLines;
        this.noiseDots = noiseDots;
        this.fontSize = fontSize;
    }

    /**
     * Gets the number of noise lines.
     * 获取干扰线数量。
     *
     * @return the noise lines count | 干扰线数量
     */
    public int getNoiseLines() {
        return noiseLines;
    }

    /**
     * Gets the number of noise dots.
     * 获取干扰点数量。
     *
     * @return the noise dots count | 干扰点数量
     */
    public int getNoiseDots() {
        return noiseDots;
    }

    /**
     * Gets the font size.
     * 获取字体大小。
     *
     * @return the font size | 字体大小
     */
    public float getFontSize() {
        return fontSize;
    }

    /**
     * Applies this strength to a configuration builder.
     * 将此强度应用到配置构建器。
     *
     * @param builder the configuration builder | 配置构建器
     * @return the builder | 构建器
     */
    public CaptchaConfig.Builder applyTo(CaptchaConfig.Builder builder) {
        return builder
            .noiseLines(noiseLines)
            .noiseDots(noiseDots)
            .fontSize(fontSize);
    }

    /**
     * Creates a configuration with this strength.
     * 使用此强度创建配置。
     *
     * @return the configuration | 配置
     */
    public CaptchaConfig toConfig() {
        return applyTo(CaptchaConfig.builder()).build();
    }
}
