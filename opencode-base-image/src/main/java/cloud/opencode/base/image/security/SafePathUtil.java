package cloud.opencode.base.image.security;

import cloud.opencode.base.image.ImageFormat;
import cloud.opencode.base.image.exception.ImageValidationException;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Safe Path Util
 * 安全路径工具类
 *
 * <p>Utilities for validating and sanitizing file paths for image operations.</p>
 * <p>用于验证和清理图片操作文件路径的工具类。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Validate path
 * SafePathUtil.validatePath(path);
 *
 * // Check for path traversal
 * boolean safe = SafePathUtil.isSafePath(path, baseDir);
 *
 * // Sanitize filename
 * String safe = SafePathUtil.sanitizeFilename("../evil.jpg");
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Path traversal attack prevention - 路径遍历攻击防护</li>
 *   <li>Filename sanitization - 文件名清理</li>
 *   <li>Image file extension validation - 图片文件扩展名验证</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: No (throws on null path) - 空值安全: 否（null 路径抛异常）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(m) where m is the filename length - regex matching and string replacement scan the filename once - 时间复杂度: O(m)，m 为文件名长度 - 正则匹配和字符串替换对文件名各扫描一次</li>
 *   <li>Space complexity: O(m) - sanitized string proportional to filename length - 空间复杂度: O(m) - 清理后的字符串与文件名长度成正比</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
public final class SafePathUtil {

    private static final Pattern NON_SAFE_FILENAME_PATTERN = Pattern.compile("[^a-zA-Z0-9._-]");

    /**
     * Allowed extensions for image files
     * 允许的图片文件扩展名
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    /**
     * Dangerous filename patterns
     * 危险的文件名模式
     */
    private static final Set<String> DANGEROUS_PATTERNS = Set.of(
        "..", "..\\", "../", "~", "con", "prn", "aux", "nul",
        "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9",
        "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9"
    );

    private SafePathUtil() {
        // Utility class
    }

    /**
     * Validate path for image operations
     * 验证图片操作的路径
     *
     * @param path the path to validate | 要验证的路径
     * @throws ImageValidationException if path is invalid | 如果路径无效
     */
    public static void validatePath(Path path) throws ImageValidationException {
        if (path == null) {
            throw new ImageValidationException("Path cannot be null");
        }

        String fileName = path.getFileName().toString();

        // Check for dangerous patterns
        String lowerName = fileName.toLowerCase();
        for (String pattern : DANGEROUS_PATTERNS) {
            if (lowerName.contains(pattern)) {
                throw new ImageValidationException("Invalid path: contains dangerous pattern");
            }
        }

        // Check extension
        String extension = getExtension(fileName);
        if (extension.isEmpty() || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new ImageValidationException("Invalid image extension: " + extension);
        }
    }

    /**
     * Validate path within base directory
     * 在基础目录内验证路径
     *
     * @param path the path to validate | 要验证的路径
     * @param baseDir the base directory | 基础目录
     * @throws ImageValidationException if path is outside base dir | 如果路径在基础目录之外
     */
    public static void validatePath(Path path, Path baseDir) throws ImageValidationException {
        validatePath(path);

        if (!isSafePath(path, baseDir)) {
            throw new ImageValidationException("Path is outside allowed directory");
        }
    }

    /**
     * Check if path is safe (within base directory)
     * 检查路径是否安全（在基础目录内）
     *
     * @param path the path to check | 要检查的路径
     * @param baseDir the base directory | 基础目录
     * @return true if path is safe | 如果路径安全返回true
     */
    public static boolean isSafePath(Path path, Path baseDir) {
        try {
            Path normalizedPath = path.toAbsolutePath().normalize();
            Path normalizedBase = baseDir.toAbsolutePath().normalize();
            return normalizedPath.startsWith(normalizedBase);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sanitize filename for safe storage
     * 清理文件名以安全存储
     *
     * @param filename the original filename | 原始文件名
     * @return the sanitized filename | 清理后的文件名
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "image";
        }

        // Remove path separators
        String sanitized = filename
            .replace("/", "_")
            .replace("\\", "_")
            .replace("..", "_");

        // Remove special characters
        sanitized = NON_SAFE_FILENAME_PATTERN.matcher(sanitized).replaceAll("_");

        // Limit length
        if (sanitized.length() > 255) {
            String ext = getExtension(sanitized);
            String name = sanitized.substring(0, sanitized.length() - ext.length() - 1);
            name = name.substring(0, Math.min(name.length(), 250 - ext.length()));
            sanitized = name + "." + ext;
        }

        return sanitized;
    }

    /**
     * Get file extension
     * 获取文件扩展名
     *
     * @param filename the filename | 文件名
     * @return the extension (without dot) | 扩展名（不含点）
     */
    public static String getExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }

    /**
     * Check if extension is allowed
     * 检查扩展名是否允许
     *
     * @param extension the extension to check | 要检查的扩展名
     * @return true if allowed | 如果允许返回true
     */
    public static boolean isAllowedExtension(String extension) {
        return extension != null && ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * Get format from path
     * 从路径获取格式
     *
     * @param path the file path | 文件路径
     * @return the image format or null | 图片格式或null
     */
    public static ImageFormat getFormat(Path path) {
        if (path == null) {
            return null;
        }
        String extension = getExtension(path.getFileName().toString());
        if (extension.isEmpty()) {
            return null;
        }
        try {
            return ImageFormat.fromExtension(extension);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Generate safe output path
     * 生成安全的输出路径
     *
     * @param inputPath the input path | 输入路径
     * @param suffix the suffix to add | 要添加的后缀
     * @return the output path | 输出路径
     */
    public static Path generateOutputPath(Path inputPath, String suffix) {
        String filename = inputPath.getFileName().toString();
        String extension = getExtension(filename);
        String basename = filename.substring(0, filename.length() - extension.length() - 1);

        String newFilename = sanitizeFilename(basename + suffix + "." + extension);
        Path parent = inputPath.getParent();
        return parent != null ? parent.resolve(newFilename) : Path.of(newFilename);
    }

    /**
     * Generate safe output path with new format
     * 生成带新格式的安全输出路径
     *
     * @param inputPath the input path | 输入路径
     * @param format the new format | 新格式
     * @return the output path | 输出路径
     */
    public static Path generateOutputPath(Path inputPath, ImageFormat format) {
        String filename = inputPath.getFileName().toString();
        String extension = getExtension(filename);
        String basename = filename.substring(0, filename.length() - extension.length() - 1);

        String newFilename = sanitizeFilename(basename + "." + format.getExtension());
        Path parent = inputPath.getParent();
        return parent != null ? parent.resolve(newFilename) : Path.of(newFilename);
    }

    /**
     * Ensure parent directory exists
     * 确保父目录存在
     *
     * @param path the file path | 文件路径
     * @return true if directory exists or was created | 如果目录存在或已创建返回true
     */
    public static boolean ensureParentExists(Path path) {
        try {
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
