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
    EASY(3, 3, 20, false, false, false, false, 0.0f),

    /**
     * Medium - Moderate noise and distortion
     * 中等 - 适中噪点和干扰
     */
    MEDIUM(5, 50, 32, false, false, false, false, 0.0f),

    /**
     * Hard - Heavy noise and distortion
     * 困难 - 大量噪点和干扰
     */
    HARD(8, 100, 28, true, true, false, true, 0.1f),

    /**
     * Extreme - Maximum noise and distortion
     * 极难 - 最大噪点和干扰
     */
    EXTREME(12, 200, 26, true, true, true, true, 0.2f);

    private final int noiseLines;
    private final int noiseDots;
    private final float fontSize;
    private final boolean randomFontPerChar;
    private final boolean bezierNoiseEnabled;
    private final boolean sineWarpEnabled;
    private final boolean outlineShadowEnabled;
    private final float charOverlapRatio;

    CaptchaStrength(int noiseLines, int noiseDots, float fontSize,
                    boolean randomFontPerChar, boolean bezierNoiseEnabled,
                    boolean sineWarpEnabled, boolean outlineShadowEnabled,
                    float charOverlapRatio) {
        this.noiseLines = noiseLines;
        this.noiseDots = noiseDots;
        this.fontSize = fontSize;
        this.randomFontPerChar = randomFontPerChar;
        this.bezierNoiseEnabled = bezierNoiseEnabled;
        this.sineWarpEnabled = sineWarpEnabled;
        this.outlineShadowEnabled = outlineShadowEnabled;
        this.charOverlapRatio = charOverlapRatio;
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
     * Returns whether each character uses a random different font.
     * 返回每个字符是否使用随机不同字体。
     *
     * @return {@code true} if random font per character is enabled | 是否启用每字符随机字体
     */
    public boolean isRandomFontPerChar() {
        return randomFontPerChar;
    }

    /**
     * Returns whether Bezier curve noise is enabled.
     * 返回是否启用贝塞尔穿字噪声。
     *
     * @return {@code true} if Bezier noise is enabled | 是否启用贝塞尔噪声
     */
    public boolean isBezierNoiseEnabled() {
        return bezierNoiseEnabled;
    }

    /**
     * Returns whether sine wave warp is enabled.
     * 返回是否启用正弦波变形。
     *
     * @return {@code true} if sine warp is enabled | 是否启用正弦波变形
     */
    public boolean isSineWarpEnabled() {
        return sineWarpEnabled;
    }

    /**
     * Returns whether character outline shadow is enabled.
     * 返回是否启用字符轮廓阴影。
     *
     * @return {@code true} if outline shadow is enabled | 是否启用轮廓阴影
     */
    public boolean isOutlineShadowEnabled() {
        return outlineShadowEnabled;
    }

    /**
     * Gets the character overlap ratio.
     * 获取字符重叠比例。
     *
     * @return the overlap ratio (0.0-0.5) | 重叠比例（0.0-0.5）
     */
    public float getCharOverlapRatio() {
        return charOverlapRatio;
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
            .fontSize(fontSize)
            .randomFontPerChar(randomFontPerChar)
            .bezierNoiseEnabled(bezierNoiseEnabled)
            .sineWarpEnabled(sineWarpEnabled)
            .outlineShadowEnabled(outlineShadowEnabled)
            .charOverlapRatio(charOverlapRatio);
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
