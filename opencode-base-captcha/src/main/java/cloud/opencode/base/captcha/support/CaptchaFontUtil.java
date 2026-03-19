package cloud.opencode.base.captcha.support;

import cloud.opencode.base.captcha.CaptchaConfig;

import java.awt.*;

/**
 * Captcha Font Utility - Font handling utilities
 * 验证码字体工具 - 字体处理工具
 *
 * <p>This class provides font-related utilities for CAPTCHA rendering.</p>
 * <p>此类提供验证码渲染的字体相关工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Font style variation - 字体样式变化</li>
 *   <li>Configurable font size - 可配置字体大小</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Font font = CaptchaFontUtil.getFont(config);
 * Font[] fonts = CaptchaFontUtil.getFonts(config, 4);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: No (config must not be null) - 空值安全: 否（配置不能为null）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - bounded by small constant font/color lists - 时间复杂度: O(1)，由常量字体/颜色列表限制</li>
 *   <li>Space complexity: O(1) - stateless utility - 空间复杂度: O(1) 无状态工具类</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class CaptchaFontUtil {

    /** Default font styles for variation | 默认字体样式变化 */
    private static final int[] FONT_STYLES = {
        Font.PLAIN,
        Font.BOLD,
        Font.ITALIC,
        Font.BOLD | Font.ITALIC
    };

    /** Fallback font families | 备用字体族 */
    private static final String[] FALLBACK_FONTS = {
        "Arial",
        "Helvetica",
        "Verdana",
        "Tahoma",
        "Dialog"
    };

    private CaptchaFontUtil() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Gets a font based on configuration.
     * 根据配置获取字体。
     *
     * @param config the configuration | 配置
     * @return the font | 字体
     */
    public static Font getFont(CaptchaConfig config) {
        return getFont(config.getFontName(), config.getFontSize());
    }

    /**
     * Gets a font with specified name and size.
     * 获取指定名称和大小的字体。
     *
     * @param fontName the font name | 字体名称
     * @param fontSize the font size | 字体大小
     * @return the font | 字体
     */
    public static Font getFont(String fontName, float fontSize) {
        Font font = new Font(fontName, Font.PLAIN, (int) fontSize);
        if (font.getFamily().equals(Font.DIALOG)) {
            // Try fallback fonts
            for (String fallback : FALLBACK_FONTS) {
                font = new Font(fallback, Font.PLAIN, (int) fontSize);
                if (!font.getFamily().equals(Font.DIALOG)) {
                    break;
                }
            }
        }
        return font;
    }

    /**
     * Gets a random font style.
     * 获取随机字体样式。
     *
     * @param font the base font | 基础字体
     * @return the styled font | 样式化字体
     */
    public static Font getRandomStyleFont(Font font) {
        int style = FONT_STYLES[CaptchaChars.randomInt(FONT_STYLES.length)];
        return font.deriveFont(style);
    }

    /**
     * Gets a rotated font.
     * 获取旋转字体。
     *
     * @param font  the base font | 基础字体
     * @param angle the rotation angle in radians | 旋转角度（弧度）
     * @return the rotated font | 旋转后的字体
     */
    public static Font getRotatedFont(Font font, double angle) {
        return font.deriveFont(
            java.awt.geom.AffineTransform.getRotateInstance(angle)
        );
    }

    /**
     * Gets a font suitable for Chinese characters.
     * 获取适合中文字符的字体。
     *
     * @param fontSize the font size | 字体大小
     * @return the font | 字体
     */
    public static Font getChineseFont(float fontSize) {
        String[] chineseFonts = {
            "SimHei", "SimSun", "Microsoft YaHei", "NSimSun",
            "WenQuanYi Micro Hei", "Noto Sans CJK SC", "Source Han Sans SC"
        };

        for (String fontName : chineseFonts) {
            Font font = new Font(fontName, Font.PLAIN, (int) fontSize);
            if (font.canDisplay('\u4e00')) {
                return font;
            }
        }

        // Fallback to system default
        return new Font(Font.SANS_SERIF, Font.PLAIN, (int) fontSize);
    }

    /**
     * Gets a random color from the config colors.
     * 从配置颜色中获取随机颜色。
     *
     * @param config the configuration | 配置
     * @return the random color | 随机颜色
     */
    public static Color getRandomColor(CaptchaConfig config) {
        Color[] colors = config.getFontColors();
        return colors[CaptchaChars.randomInt(colors.length)];
    }

    /**
     * Generates a random color.
     * 生成随机颜色。
     *
     * @return the random color | 随机颜色
     */
    public static Color randomColor() {
        return new Color(
            CaptchaChars.randomInt(200),
            CaptchaChars.randomInt(200),
            CaptchaChars.randomInt(200)
        );
    }

    /**
     * Generates a light random color.
     * 生成浅随机颜色。
     *
     * @return the light color | 浅颜色
     */
    public static Color randomLightColor() {
        return new Color(
            CaptchaChars.randomInt(155, 255),
            CaptchaChars.randomInt(155, 255),
            CaptchaChars.randomInt(155, 255)
        );
    }

    /**
     * Generates a dark random color.
     * 生成深随机颜色。
     *
     * @return the dark color | 深颜色
     */
    public static Color randomDarkColor() {
        return new Color(
            CaptchaChars.randomInt(100),
            CaptchaChars.randomInt(100),
            CaptchaChars.randomInt(100)
        );
    }
}
