package cloud.opencode.base.captcha;

import java.awt.*;
import java.time.Duration;

/**
 * Captcha Configuration - Configuration for CAPTCHA generation
 * 验证码配置 - 验证码生成配置
 *
 * <p>This class provides a fluent builder API for configuring CAPTCHA generation.</p>
 * <p>此类提供用于配置验证码生成的流式构建器 API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent builder API for configuration - 流式构建器 API 配置</li>
 *   <li>Default values for all settings - 所有设置的默认值</li>
 *   <li>Support for image dimensions, fonts, noise, and expiration - 支持图像尺寸、字体、噪点和过期</li>
 *   <li>GIF animation settings (frame count, delay) - GIF 动画设置（帧数、延迟）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaConfig config = CaptchaConfig.builder()
 *     .width(200)
 *     .height(80)
 *     .length(6)
 *     .type(CaptchaType.ALPHANUMERIC)
 *     .expireTime(Duration.ofMinutes(5))
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (some fields may be null if not set) - 空值安全: 否（某些字段未设置时可能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class CaptchaConfig {

    // Default values
    private static final int DEFAULT_WIDTH = 160;
    private static final int DEFAULT_HEIGHT = 60;
    private static final int DEFAULT_LENGTH = 4;
    private static final Duration DEFAULT_EXPIRE_TIME = Duration.ofMinutes(5);
    private static final int DEFAULT_NOISE_LINES = 5;
    private static final int DEFAULT_NOISE_DOTS = 50;
    private static final float DEFAULT_FONT_SIZE = 32.0f;
    private static final String DEFAULT_FONT_NAME = "Arial";

    private final int width;
    private final int height;
    private final int length;
    private final CaptchaType type;
    private final Duration expireTime;
    private final int noiseLines;
    private final int noiseDots;
    private final float fontSize;
    private final String fontName;
    private final Color backgroundColor;
    private final Color[] fontColors;
    private final boolean caseSensitive;
    private final int gifFrameCount;
    private final int gifDelay;

    private CaptchaConfig(Builder builder) {
        this.width = builder.width;
        this.height = builder.height;
        this.length = builder.length;
        this.type = builder.type;
        this.expireTime = builder.expireTime;
        this.noiseLines = builder.noiseLines;
        this.noiseDots = builder.noiseDots;
        this.fontSize = builder.fontSize;
        this.fontName = builder.fontName;
        this.backgroundColor = builder.backgroundColor;
        this.fontColors = builder.fontColors;
        this.caseSensitive = builder.caseSensitive;
        this.gifFrameCount = builder.gifFrameCount;
        this.gifDelay = builder.gifDelay;
    }

    /**
     * Creates a default configuration.
     * 创建默认配置。
     *
     * @return the default config | 默认配置
     */
    public static CaptchaConfig defaults() {
        return new Builder().build();
    }

    /**
     * Creates a builder for configuration.
     * 创建配置构建器。
     *
     * @return a new builder | 新构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder from this configuration.
     * 从此配置创建构建器。
     *
     * @return a builder with current values | 具有当前值的构建器
     */
    public Builder toBuilder() {
        return new Builder()
            .width(width)
            .height(height)
            .length(length)
            .type(type)
            .expireTime(expireTime)
            .noiseLines(noiseLines)
            .noiseDots(noiseDots)
            .fontSize(fontSize)
            .fontName(fontName)
            .backgroundColor(backgroundColor)
            .fontColors(fontColors)
            .caseSensitive(caseSensitive)
            .gifFrameCount(gifFrameCount)
            .gifDelay(gifDelay);
    }

    // ==================== Getters ====================

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getLength() { return length; }
    public CaptchaType getType() { return type; }
    public Duration getExpireTime() { return expireTime; }
    public int getNoiseLines() { return noiseLines; }
    public int getNoiseDots() { return noiseDots; }
    public float getFontSize() { return fontSize; }
    public String getFontName() { return fontName; }
    public Color getBackgroundColor() { return backgroundColor; }
    public Color[] getFontColors() { return fontColors; }
    public boolean isCaseSensitive() { return caseSensitive; }
    public int getGifFrameCount() { return gifFrameCount; }
    public int getGifDelay() { return gifDelay; }

    /**
     * Configuration Builder
     * 配置构建器
     */
    public static final class Builder {

        private int width = DEFAULT_WIDTH;
        private int height = DEFAULT_HEIGHT;
        private int length = DEFAULT_LENGTH;
        private CaptchaType type = CaptchaType.ALPHANUMERIC;
        private Duration expireTime = DEFAULT_EXPIRE_TIME;
        private int noiseLines = DEFAULT_NOISE_LINES;
        private int noiseDots = DEFAULT_NOISE_DOTS;
        private float fontSize = DEFAULT_FONT_SIZE;
        private String fontName = DEFAULT_FONT_NAME;
        private Color backgroundColor = Color.WHITE;
        private Color[] fontColors = new Color[] {
            new Color(0, 0, 0),
            new Color(0, 0, 255),
            new Color(255, 0, 0),
            new Color(0, 128, 0),
            new Color(128, 0, 128)
        };
        private boolean caseSensitive = false;
        private int gifFrameCount = 10;
        private int gifDelay = 100;

        private Builder() {}

        /**
         * Sets the image width.
         * 设置图像宽度。
         *
         * @param width the width | 宽度
         * @return this builder | 此构建器
         */
        public Builder width(int width) {
            this.width = width;
            return this;
        }

        /**
         * Sets the image height.
         * 设置图像高度。
         *
         * @param height the height | 高度
         * @return this builder | 此构建器
         */
        public Builder height(int height) {
            this.height = height;
            return this;
        }

        /**
         * Sets the CAPTCHA code length.
         * 设置验证码长度。
         *
         * @param length the length | 长度
         * @return this builder | 此构建器
         */
        public Builder length(int length) {
            this.length = length;
            return this;
        }

        /**
         * Sets the CAPTCHA type.
         * 设置验证码类型。
         *
         * @param type the type | 类型
         * @return this builder | 此构建器
         */
        public Builder type(CaptchaType type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the expiration time.
         * 设置过期时间。
         *
         * @param expireTime the expire time | 过期时间
         * @return this builder | 此构建器
         */
        public Builder expireTime(Duration expireTime) {
            this.expireTime = expireTime;
            return this;
        }

        /**
         * Sets the number of noise lines.
         * 设置干扰线数量。
         *
         * @param noiseLines the noise lines count | 干扰线数量
         * @return this builder | 此构建器
         */
        public Builder noiseLines(int noiseLines) {
            this.noiseLines = noiseLines;
            return this;
        }

        /**
         * Sets the number of noise dots.
         * 设置干扰点数量。
         *
         * @param noiseDots the noise dots count | 干扰点数量
         * @return this builder | 此构建器
         */
        public Builder noiseDots(int noiseDots) {
            this.noiseDots = noiseDots;
            return this;
        }

        /**
         * Sets the font size.
         * 设置字体大小。
         *
         * @param fontSize the font size | 字体大小
         * @return this builder | 此构建器
         */
        public Builder fontSize(float fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        /**
         * Sets the font name.
         * 设置字体名称。
         *
         * @param fontName the font name | 字体名称
         * @return this builder | 此构建器
         */
        public Builder fontName(String fontName) {
            this.fontName = fontName;
            return this;
        }

        /**
         * Sets the background color.
         * 设置背景颜色。
         *
         * @param backgroundColor the background color | 背景颜色
         * @return this builder | 此构建器
         */
        public Builder backgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        /**
         * Sets the font colors.
         * 设置字体颜色。
         *
         * @param fontColors the font colors | 字体颜色数组
         * @return this builder | 此构建器
         */
        public Builder fontColors(Color... fontColors) {
            this.fontColors = fontColors;
            return this;
        }

        /**
         * Sets case sensitivity.
         * 设置区分大小写。
         *
         * @param caseSensitive true for case sensitive | true 表示区分大小写
         * @return this builder | 此构建器
         */
        public Builder caseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
            return this;
        }

        /**
         * Sets the GIF frame count.
         * 设置 GIF 帧数。
         *
         * @param gifFrameCount the frame count | 帧数
         * @return this builder | 此构建器
         */
        public Builder gifFrameCount(int gifFrameCount) {
            this.gifFrameCount = gifFrameCount;
            return this;
        }

        /**
         * Sets the GIF frame delay.
         * 设置 GIF 帧延迟。
         *
         * @param gifDelay the delay in milliseconds | 延迟（毫秒）
         * @return this builder | 此构建器
         */
        public Builder gifDelay(int gifDelay) {
            this.gifDelay = gifDelay;
            return this;
        }

        /**
         * Builds the configuration.
         * 构建配置。
         *
         * @return the configuration | 配置
         */
        public CaptchaConfig build() {
            return new CaptchaConfig(this);
        }
    }
}
