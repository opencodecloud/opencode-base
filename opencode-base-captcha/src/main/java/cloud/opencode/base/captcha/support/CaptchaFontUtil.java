package cloud.opencode.base.captcha.support;

import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.exception.CaptchaException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
 *   <li>Thread-safe: Yes (ConcurrentHashMap + volatile caching) - 线程安全: 是（ConcurrentHashMap + volatile 缓存）</li>
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

    /**
     * Font cache keyed by "fontName:fontSize" to avoid repeated Font allocation.
     * 字体缓存，键为 "fontName:fontSize"，避免重复创建 Font 对象。
     *
     * <p>The number of distinct font name + size combinations is naturally bounded
     * in captcha usage, so no explicit eviction is needed.</p>
     * <p>验证码使用中字体名称+大小的组合数量天然有限，无需显式淘汰。</p>
     */
    private static final Map<String, Font> FONT_CACHE = new ConcurrentHashMap<>();

    /** Maximum cache size to prevent unbounded growth | 最大缓存大小，防止无限增长 */
    private static final int MAX_CACHE_SIZE = 100;

    /**
     * Cached Chinese font name (null = not yet resolved, empty string = no CJK font found).
     * 缓存的中文字体名称（null = 尚未解析，空字符串 = 未找到中文字体）。
     */
    private static volatile String cachedChineseFontName;

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
        String key = fontName + ":" + (int) fontSize;
        Font cached = FONT_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        // Evict before computeIfAbsent — never modify map inside its own mapping function
        // 在 computeIfAbsent 之前淘汰 — 禁止在映射函数内部修改 map
        if (FONT_CACHE.size() >= MAX_CACHE_SIZE) {
            FONT_CACHE.clear();
        }
        return FONT_CACHE.computeIfAbsent(key, k -> {
            Font font = new Font(fontName, Font.PLAIN, (int) fontSize);
            if (font.getFamily().equals(Font.DIALOG)) {
                // Try fallback fonts | 尝试备用字体
                for (String fallback : FALLBACK_FONTS) {
                    font = new Font(fallback, Font.PLAIN, (int) fontSize);
                    if (!font.getFamily().equals(Font.DIALOG)) {
                        break;
                    }
                }
            }
            return font;
        });
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
        String resolvedName = cachedChineseFontName;
        if (resolvedName == null) {
            // Resolve the best Chinese font name once and cache it
            // 解析最佳中文字体名称并缓存
            resolvedName = resolveBestChineseFontName();
            cachedChineseFontName = resolvedName;
        }
        String fontName = resolvedName.isEmpty() ? Font.SANS_SERIF : resolvedName;
        return getFont(fontName, fontSize);
    }

    /**
     * Resolves the best available Chinese font name on this system.
     * 解析当前系统上可用的最佳中文字体名称。
     *
     * @return the font name, or empty string if none found | 字体名称，未找到则返回空字符串
     */
    private static String resolveBestChineseFontName() {
        String[] chineseFonts = {
            "SimHei", "SimSun", "Microsoft YaHei", "NSimSun",
            "WenQuanYi Micro Hei", "Noto Sans CJK SC", "Source Han Sans SC"
        };
        for (String fontName : chineseFonts) {
            Font font = new Font(fontName, Font.PLAIN, 12);
            if (font.canDisplay('\u4e00')) {
                return fontName;
            }
        }
        return "";
    }

    /**
     * Gets a random color from the config colors.
     * 从配置颜色中获取随机颜色。
     *
     * @param config the configuration | 配置
     * @return the random color | 随机颜色
     */
    public static Color getRandomColor(CaptchaConfig config) {
        return config.getFontColorAt(CaptchaChars.randomInt(config.getFontColorCount()));
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

    /**
     * Loads a custom font from a file path (TTF or OTF).
     * 从文件路径加载自定义字体（TTF 或 OTF）。
     *
     * <p>The loaded font is registered with the local {@link GraphicsEnvironment}
     * and cached for subsequent calls with the same path and size.</p>
     * <p>加载的字体会注册到本地 {@link GraphicsEnvironment} 并缓存，以供后续相同路径和大小的调用使用。</p>
     *
     * <p><strong>Security note:</strong> This method only validates the file extension (.ttf/.otf)
     * and canonical path. The caller is responsible for ensuring the path is safe and not
     * controlled by untrusted input (e.g., user-supplied paths could lead to arbitrary file reads).</p>
     * <p><strong>安全说明:</strong> 本方法仅验证文件扩展名(.ttf/.otf)和规范路径。
     * 调用者负责确保路径安全，不受不可信输入控制（例如，用户提供的路径可能导致任意文件读取）。</p>
     *
     * @param path     the font file path | 字体文件路径
     * @param fontSize the desired font size | 期望字体大小
     * @return the loaded font | 加载的字体
     * @throws CaptchaException if the font file cannot be loaded | 如果字体文件无法加载
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    public static Font loadCustomFont(String path, float fontSize) {
        if (path == null || path.isBlank()) {
            throw new CaptchaException("Font file path must not be null or blank | 字体文件路径不能为 null 或空白");
        }

        String lowerPath = path.toLowerCase();
        if (!lowerPath.endsWith(".ttf") && !lowerPath.endsWith(".otf")) {
            throw new CaptchaException("Font file must be TTF or OTF: " + path
                + " | 字体文件必须是 TTF 或 OTF 格式: " + path);
        }

        String cacheKey = path + ":" + fontSize;
        Font cached = FONT_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        File fontFile = new File(path);

        // Validate canonical path to prevent path traversal and symlink attacks
        // 验证规范路径以防止路径遍历和符号链接攻击
        try {
            String canonicalPath = fontFile.getCanonicalPath();
            String absolutePath = fontFile.getAbsolutePath();

            // Canonical path must end with .ttf/.otf (prevents null-byte or double-extension tricks)
            // 规范路径必须以 .ttf/.otf 结尾（防止空字节或双扩展名欺骗）
            if (!canonicalPath.toLowerCase().endsWith(".ttf") && !canonicalPath.toLowerCase().endsWith(".otf")) {
                throw new CaptchaException("Font file canonical path must end with .ttf or .otf: " + canonicalPath
                    + " | 字体文件规范路径必须以 .ttf 或 .otf 结尾: " + canonicalPath);
            }

            // Detect path traversal: canonical path should match absolute path (no .. resolution)
            // 检测路径遍历：规范路径应与绝对路径匹配（无 .. 解析）
            if (!canonicalPath.equals(new File(absolutePath).getCanonicalPath())) {
                throw new CaptchaException("Suspicious font file path (possible traversal): " + path
                    + " | 可疑的字体文件路径（可能存在路径遍历）: " + path);
            }

            // Reject symbolic links to prevent symlink-based attacks
            // 拒绝符号链接以防止基于符号链接的攻击
            if (java.nio.file.Files.isSymbolicLink(fontFile.toPath())) {
                throw new CaptchaException("Symbolic link font files are not allowed: " + path
                    + " | 不允许使用符号链接字体文件: " + path);
            }
        } catch (IOException e) {
            throw new CaptchaException("Cannot resolve canonical path for font file: " + path
                + " | 无法解析字体文件的规范路径: " + path, e);
        }

        if (!fontFile.exists()) {
            throw new CaptchaException("Font file does not exist: " + path
                + " | 字体文件不存在: " + path);
        }

        try {
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            Font derivedFont = baseFont.deriveFont(fontSize);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(baseFont);

            // Evict before caching — prevent unbounded growth
            // 缓存前淘汰 — 防止无限增长
            if (FONT_CACHE.size() >= MAX_CACHE_SIZE) {
                FONT_CACHE.clear();
            }
            FONT_CACHE.put(cacheKey, derivedFont);

            return derivedFont;
        } catch (FontFormatException | IOException e) {
            throw new CaptchaException("Failed to load font file: " + path
                + " | 加载字体文件失败: " + path, e);
        }
    }

    /**
     * Generates an array of random fonts, one per character, for anti-OCR.
     * 为每个字符生成一个随机字体数组，用于抗 OCR。
     *
     * <p>Builds a candidate font pool from the base font, fallback fonts, and
     * any valid custom font paths. Each character gets a randomly selected font
     * with a randomly applied style (PLAIN/BOLD/ITALIC/BOLD+ITALIC).</p>
     * <p>从基础字体、备用字体和有效的自定义字体路径构建候选字体池。每个字符随机选择
     * 一个字体并随机应用样式（普通/粗体/斜体/粗斜体）。</p>
     *
     * @param baseFontName the base font name | 基础字体名称
     * @param customPaths  the custom font paths (may be empty) | 自定义字体路径（可为空）
     * @param fontSize     the font size | 字体大小
     * @param charCount    the number of characters | 字符数
     * @return array of fonts, one per character | 字体数组，每字符一个
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    public static Font[] getRandomFontsPerChar(String baseFontName, List<String> customPaths,
                                                float fontSize, int charCount) {
        // Build candidate font pool | 构建候选字体池
        List<Font> pool = new ArrayList<>();

        // Add base font | 添加基础字体
        pool.add(getFont(baseFontName, fontSize));

        // Add fallback fonts | 添加备用字体
        for (String fallback : FALLBACK_FONTS) {
            pool.add(new Font(fallback, Font.PLAIN, (int) fontSize));
        }

        // Add custom fonts (skip on failure) | 添加自定义字体（失败时跳过）
        if (customPaths != null) {
            for (String customPath : customPaths) {
                try {
                    pool.add(loadCustomFont(customPath, fontSize));
                } catch (CaptchaException ignored) {
                    // Skip invalid custom fonts silently | 静默跳过无效的自定义字体
                }
            }
        }

        // Assign a random font with random style to each character
        // 为每个字符分配随机字体和随机样式
        Font[] fonts = new Font[charCount];
        for (int i = 0; i < charCount; i++) {
            Font selected = pool.get(CaptchaChars.randomInt(pool.size()));
            int style = FONT_STYLES[CaptchaChars.randomInt(FONT_STYLES.length)];
            fonts[i] = selected.deriveFont(style);
        }

        return fonts;
    }
}
