package cloud.opencode.base.captcha;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private final List<String> customFontPaths;
    private final boolean randomFontPerChar;
    private final float charOverlapRatio;
    private final boolean sineWarpEnabled;
    private final boolean outlineShadowEnabled;
    private final boolean bezierNoiseEnabled;
    private final int powDifficulty;
    private final float audioSpeedVariation;

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
        this.fontColors = builder.fontColors != null ? builder.fontColors.clone() : new Color[0];
        this.caseSensitive = builder.caseSensitive;
        this.gifFrameCount = builder.gifFrameCount;
        this.gifDelay = builder.gifDelay;
        this.customFontPaths = List.copyOf(builder.customFontPaths);
        this.randomFontPerChar = builder.randomFontPerChar;
        this.charOverlapRatio = builder.charOverlapRatio;
        this.sineWarpEnabled = builder.sineWarpEnabled;
        this.outlineShadowEnabled = builder.outlineShadowEnabled;
        this.bezierNoiseEnabled = builder.bezierNoiseEnabled;
        this.powDifficulty = builder.powDifficulty;
        this.audioSpeedVariation = builder.audioSpeedVariation;
    }

    /** Cached default configuration instance (immutable, safe to share). | 缓存的默认配置实例（不可变，可安全共享）。 */
    private static final CaptchaConfig DEFAULT_CONFIG = new Builder().build();

    /**
     * Creates a default configuration.
     * 创建默认配置。
     *
     * @return the default config | 默认配置
     */
    public static CaptchaConfig defaults() {
        return DEFAULT_CONFIG;
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
            .gifDelay(gifDelay)
            .customFontPaths(customFontPaths)
            .randomFontPerChar(randomFontPerChar)
            .charOverlapRatio(charOverlapRatio)
            .sineWarpEnabled(sineWarpEnabled)
            .outlineShadowEnabled(outlineShadowEnabled)
            .bezierNoiseEnabled(bezierNoiseEnabled)
            .powDifficulty(powDifficulty)
            .audioSpeedVariation(audioSpeedVariation);
    }

    // ==================== Getters ====================

    /**
     * Gets the image width.
     * 获取图像宽度。
     *
     * @return the width in pixels | 宽度（像素）
     */
    public int getWidth() { return width; }

    /**
     * Gets the image height.
     * 获取图像高度。
     *
     * @return the height in pixels | 高度（像素）
     */
    public int getHeight() { return height; }

    /**
     * Gets the captcha character length.
     * 获取验证码字符长度。
     *
     * @return the number of characters | 字符数
     */
    public int getLength() { return length; }

    /**
     * Gets the captcha type.
     * 获取验证码类型。
     *
     * @return the captcha type | 验证码类型
     */
    public CaptchaType getType() { return type; }

    /**
     * Gets the expiration time.
     * 获取过期时间。
     *
     * @return the expire duration | 过期时长
     */
    public Duration getExpireTime() { return expireTime; }

    /**
     * Gets the number of noise lines.
     * 获取干扰线数量。
     *
     * @return the noise line count | 干扰线数量
     */
    public int getNoiseLines() { return noiseLines; }

    /**
     * Gets the number of noise dots.
     * 获取干扰点数量。
     *
     * @return the noise dot count | 干扰点数量
     */
    public int getNoiseDots() { return noiseDots; }

    /**
     * Gets the font size.
     * 获取字体大小。
     *
     * @return the font size in points | 字体大小（磅）
     */
    public float getFontSize() { return fontSize; }

    /**
     * Gets the font name.
     * 获取字体名称。
     *
     * @return the font name | 字体名称
     */
    public String getFontName() { return fontName; }

    /**
     * Gets the background color.
     * 获取背景颜色。
     *
     * @return the background color | 背景颜色
     */
    public Color getBackgroundColor() { return backgroundColor; }

    /**
     * Gets the font color palette.
     * 获取字体颜色数组。
     *
     * @return the font colors | 字体颜色数组
     */
    public Color[] getFontColors() { return fontColors.clone(); }

    /**
     * Gets a font color by index without array cloning (internal use).
     * 按索引获取字体颜色，无需数组克隆（内部使用）。
     *
     * @param index the index (wraps around) | 索引（循环取值）
     * @return the color | 颜色
     */
    public Color getFontColorAt(int index) {
        return fontColors[index % fontColors.length];
    }

    /**
     * Gets the number of font colors available.
     * 获取可用字体颜色数量。
     *
     * @return the count | 数量
     */
    public int getFontColorCount() {
        return fontColors.length;
    }

    /**
     * Returns whether verification is case-sensitive.
     * 返回验证是否区分大小写。
     *
     * @return {@code true} if case-sensitive | 是否区分大小写
     */
    public boolean isCaseSensitive() { return caseSensitive; }

    /**
     * Gets the GIF frame count.
     * 获取 GIF 帧数。
     *
     * @return the number of GIF frames | GIF 帧数
     */
    public int getGifFrameCount() { return gifFrameCount; }

    /**
     * Gets the GIF frame delay.
     * 获取 GIF 帧延迟。
     *
     * @return the delay between frames in milliseconds | 帧间延迟（毫秒）
     */
    public int getGifDelay() { return gifDelay; }

    /**
     * Gets the custom font paths.
     * 获取自定义字体路径列表。
     *
     * @return the custom font paths (unmodifiable) | 自定义字体路径列表（不可修改）
     */
    public List<String> getCustomFontPaths() { return customFontPaths; }

    /**
     * Returns whether each character uses a random different font.
     * 返回每个字符是否使用随机不同字体。
     *
     * @return {@code true} if random font per character is enabled | 是否启用每字符随机字体
     */
    public boolean isRandomFontPerChar() { return randomFontPerChar; }

    /**
     * Gets the character overlap ratio.
     * 获取字符重叠比例。
     *
     * @return the overlap ratio (0.0-0.5) | 重叠比例（0.0-0.5）
     */
    public float getCharOverlapRatio() { return charOverlapRatio; }

    /**
     * Returns whether sine wave warp is enabled.
     * 返回是否启用正弦波变形。
     *
     * @return {@code true} if sine warp is enabled | 是否启用正弦波变形
     */
    public boolean isSineWarpEnabled() { return sineWarpEnabled; }

    /**
     * Returns whether character outline shadow is enabled.
     * 返回是否启用字符轮廓阴影。
     *
     * @return {@code true} if outline shadow is enabled | 是否启用轮廓阴影
     */
    public boolean isOutlineShadowEnabled() { return outlineShadowEnabled; }

    /**
     * Returns whether Bezier curve noise is enabled.
     * 返回是否启用贝塞尔穿字噪声。
     *
     * @return {@code true} if Bezier noise is enabled | 是否启用贝塞尔噪声
     */
    public boolean isBezierNoiseEnabled() { return bezierNoiseEnabled; }

    /**
     * Gets the PoW (Proof-of-Work) difficulty.
     * 获取 PoW（工作量证明）难度。
     *
     * @return the number of leading zero bits | 前导零位数
     */
    public int getPowDifficulty() { return powDifficulty; }

    /**
     * Gets the audio speed variation range.
     * 获取音频语速变化范围。
     *
     * @return the speed variation (0.0-0.5) | 语速变化范围（0.0-0.5）
     */
    public float getAudioSpeedVariation() { return audioSpeedVariation; }

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
        private List<String> customFontPaths = new ArrayList<>();
        private boolean randomFontPerChar = false;
        private float charOverlapRatio = 0.0f;
        private boolean sineWarpEnabled = false;
        private boolean outlineShadowEnabled = false;
        private boolean bezierNoiseEnabled = false;
        private int powDifficulty = 20;
        private float audioSpeedVariation = 0.2f;

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
         * Sets the custom font paths.
         * 设置自定义字体路径列表。
         *
         * @param paths the font paths | 字体路径列表
         * @return this builder | 此构建器
         */
        public Builder customFontPaths(List<String> paths) {
            this.customFontPaths = new ArrayList<>(paths);
            return this;
        }

        /**
         * Adds a single custom font path.
         * 追加单个自定义字体路径。
         *
         * @param path the font path | 字体路径
         * @return this builder | 此构建器
         */
        public Builder customFontPath(String path) {
            this.customFontPaths.add(path);
            return this;
        }

        /**
         * Sets whether each character uses a random different font.
         * 设置每个字符是否使用随机不同字体。
         *
         * @param randomFontPerChar true to enable | true 表示启用
         * @return this builder | 此构建器
         */
        public Builder randomFontPerChar(boolean randomFontPerChar) {
            this.randomFontPerChar = randomFontPerChar;
            return this;
        }

        /**
         * Sets the character overlap ratio.
         * 设置字符重叠比例。
         *
         * @param charOverlapRatio the ratio (0.0-0.5) | 重叠比例（0.0-0.5）
         * @return this builder | 此构建器
         */
        public Builder charOverlapRatio(float charOverlapRatio) {
            this.charOverlapRatio = charOverlapRatio;
            return this;
        }

        /**
         * Sets whether sine wave warp is enabled.
         * 设置是否启用正弦波变形。
         *
         * @param sineWarpEnabled true to enable | true 表示启用
         * @return this builder | 此构建器
         */
        public Builder sineWarpEnabled(boolean sineWarpEnabled) {
            this.sineWarpEnabled = sineWarpEnabled;
            return this;
        }

        /**
         * Sets whether character outline shadow is enabled.
         * 设置是否启用字符轮廓阴影。
         *
         * @param outlineShadowEnabled true to enable | true 表示启用
         * @return this builder | 此构建器
         */
        public Builder outlineShadowEnabled(boolean outlineShadowEnabled) {
            this.outlineShadowEnabled = outlineShadowEnabled;
            return this;
        }

        /**
         * Sets whether Bezier curve noise is enabled.
         * 设置是否启用贝塞尔穿字噪声。
         *
         * @param bezierNoiseEnabled true to enable | true 表示启用
         * @return this builder | 此构建器
         */
        public Builder bezierNoiseEnabled(boolean bezierNoiseEnabled) {
            this.bezierNoiseEnabled = bezierNoiseEnabled;
            return this;
        }

        /**
         * Sets the PoW (Proof-of-Work) difficulty.
         * 设置 PoW（工作量证明）难度。
         *
         * @param powDifficulty the number of leading zero bits (1-32) | 前导零位数（1-32）
         * @return this builder | 此构建器
         */
        public Builder powDifficulty(int powDifficulty) {
            this.powDifficulty = powDifficulty;
            return this;
        }

        /**
         * Sets the audio speed variation range.
         * 设置音频语速变化范围。
         *
         * @param audioSpeedVariation the variation (0.0-0.5) | 变化范围（0.0-0.5）
         * @return this builder | 此构建器
         */
        public Builder audioSpeedVariation(float audioSpeedVariation) {
            this.audioSpeedVariation = audioSpeedVariation;
            return this;
        }

        /**
         * Builds the configuration after validating all parameters.
         * 验证所有参数后构建配置。
         *
         * <p>Bounds are enforced to prevent resource exhaustion (e.g., OOM from
         * oversized {@code BufferedImage} allocation).</p>
         * <p>强制边界检查以防止资源耗尽（例如，过大的 {@code BufferedImage} 分配导致 OOM）。</p>
         *
         * @return the configuration | 配置
         * @throws NullPointerException if type, expireTime, or fontName is null |
         *         如果 type、expireTime 或 fontName 为 null
         * @throws IllegalArgumentException if any numeric parameter is out of bounds |
         *         如果任何数值参数超出范围
         */
        public CaptchaConfig build() {
            Objects.requireNonNull(type, "type must not be null");
            Objects.requireNonNull(expireTime, "expireTime must not be null");
            Objects.requireNonNull(fontName, "fontName must not be null");
            if (width < 1 || width > 2000) {
                throw new IllegalArgumentException("width must be between 1 and 2000");
            }
            if (height < 1 || height > 2000) {
                throw new IllegalArgumentException("height must be between 1 and 2000");
            }
            if (length < 1 || length > 20) {
                throw new IllegalArgumentException("length must be between 1 and 20");
            }
            if (gifFrameCount < 1 || gifFrameCount > 100) {
                throw new IllegalArgumentException("gifFrameCount must be between 1 and 100");
            }
            if (noiseLines < 0 || noiseLines > 100) {
                throw new IllegalArgumentException("noiseLines must be between 0 and 100");
            }
            if (noiseDots < 0 || noiseDots > 1000) {
                throw new IllegalArgumentException("noiseDots must be between 0 and 1000");
            }
            if (fontSize < 1 || fontSize > 200) {
                throw new IllegalArgumentException("fontSize must be between 1 and 200");
            }
            if (gifDelay < 1 || gifDelay > 10000) {
                throw new IllegalArgumentException("gifDelay must be between 1 and 10000");
            }
            if (fontColors == null || fontColors.length == 0) {
                throw new IllegalArgumentException("fontColors must not be null or empty");
            }
            if (charOverlapRatio < 0.0f || charOverlapRatio > 0.5f) {
                throw new IllegalArgumentException("charOverlapRatio must be between 0.0 and 0.5");
            }
            if (powDifficulty < 10 || powDifficulty > 32) {
                throw new IllegalArgumentException("powDifficulty must be between 10 and 32, recommended 20");
            }
            if (audioSpeedVariation < 0.0f || audioSpeedVariation > 0.5f) {
                throw new IllegalArgumentException("audioSpeedVariation must be between 0.0 and 0.5");
            }
            if (customFontPaths.size() > 20) {
                throw new IllegalArgumentException("customFontPaths must contain at most 20 entries");
            }
            return new CaptchaConfig(this);
        }
    }
}
