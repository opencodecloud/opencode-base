package cloud.opencode.base.image;

/**
 * Image Format
 * 图片格式枚举
 *
 * <p>Supported image formats for reading and writing.</p>
 * <p>支持读写的图片格式。</p>
 *
 * <p><strong>Supported Formats | 支持的格式:</strong></p>
 * <ul>
 *   <li>JPEG - Most common, no transparency | 最常用，不支持透明度</li>
 *   <li>PNG - Supports transparency | 支持透明度</li>
 *   <li>GIF - Static images only | 仅支持静态图片</li>
 *   <li>BMP - Uncompressed | 无压缩</li>
 *   <li>WEBP - Optional, requires TwelveMonkeys plugin | 可选，需要 TwelveMonkeys 插件</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Enum for supported image formats (JPEG, PNG, GIF, BMP, WEBP) - 支持的图片格式枚举（JPEG、PNG、GIF、BMP、WEBP）</li>
 *   <li>MIME type and file extension mapping - MIME 类型和文件扩展名映射</li>
 *   <li>Transparency support detection - 透明度支持检测</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get format info
 * ImageFormat format = ImageFormat.PNG;
 * String ext = format.getExtension();      // "png"
 * String mime = format.getMimeType();       // "image/png"
 * boolean alpha = format.supportsAlpha();   // true
 * 
 * // Detect from filename
 * ImageFormat detected = ImageFormat.fromExtension("jpg");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: No (throws on unknown extension) - 空值安全: 否（未知扩展名抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
public enum ImageFormat {

    /**
     * JPEG format | JPEG格式
     */
    JPEG("jpg", "image/jpeg", false),

    /**
     * JPG format (alias for JPEG) | JPG格式（JPEG别名）
     */
    JPG("jpg", "image/jpeg", false),

    /**
     * PNG format | PNG格式
     */
    PNG("png", "image/png", true),

    /**
     * GIF format | GIF格式
     */
    GIF("gif", "image/gif", true),

    /**
     * BMP format | BMP格式
     */
    BMP("bmp", "image/bmp", false),

    /**
     * WebP format | WebP格式
     *
     * <p>Optional format, requires TwelveMonkeys imageio-webp plugin.</p>
     * <p>可选格式，需要 TwelveMonkeys imageio-webp 插件。</p>
     *
     * <p>Add dependency to enable:</p>
     * <pre>{@code
     * <dependency>
     *     <groupId>com.twelvemonkeys.imageio</groupId>
     *     <artifactId>imageio-webp</artifactId>
     *     <version>3.12.0</version>
     * </dependency>
     * }</pre>
     */
    WEBP("webp", "image/webp", true);

    private final String extension;
    private final String mimeType;
    private final boolean supportsTransparency;

    ImageFormat(String extension, String mimeType, boolean supportsTransparency) {
        this.extension = extension;
        this.mimeType = mimeType;
        this.supportsTransparency = supportsTransparency;
    }

    /**
     * Get file extension
     * 获取文件扩展名
     *
     * @return the file extension | 文件扩展名
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Get MIME type
     * 获取MIME类型
     *
     * @return the MIME type | MIME类型
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Check if format supports transparency
     * 检查格式是否支持透明度
     *
     * @return true if transparency is supported | 如果支持透明度返回true
     */
    public boolean supportsTransparency() {
        return supportsTransparency;
    }

    /**
     * Get format from file extension
     * 从文件扩展名获取格式
     *
     * @param extension the file extension | 文件扩展名
     * @return the image format | 图片格式
     * @throws IllegalArgumentException if format is not supported | 如果格式不支持则抛出异常
     */
    public static ImageFormat fromExtension(String extension) {
        if (extension == null) {
            throw new IllegalArgumentException("Extension cannot be null");
        }
        String ext = extension.toLowerCase().replace(".", "");
        return switch (ext) {
            case "jpg", "jpeg" -> JPEG;
            case "png" -> PNG;
            case "gif" -> GIF;
            case "bmp" -> BMP;
            case "webp" -> WEBP;
            default -> throw new IllegalArgumentException("Unsupported format: " + extension);
        };
    }

    /**
     * Get format from MIME type
     * 从MIME类型获取格式
     *
     * @param mimeType the MIME type | MIME类型
     * @return the image format | 图片格式
     * @throws IllegalArgumentException if MIME type is not supported | 如果MIME类型不支持则抛出异常
     */
    public static ImageFormat fromMimeType(String mimeType) {
        if (mimeType == null) {
            throw new IllegalArgumentException("MIME type cannot be null");
        }
        return switch (mimeType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> JPEG;
            case "image/png" -> PNG;
            case "image/gif" -> GIF;
            case "image/bmp" -> BMP;
            case "image/webp" -> WEBP;
            default -> throw new IllegalArgumentException("Unsupported MIME type: " + mimeType);
        };
    }

    /**
     * Check if extension is supported
     * 检查扩展名是否支持
     *
     * @param extension the file extension | 文件扩展名
     * @return true if supported | 如果支持返回true
     */
    public static boolean isSupported(String extension) {
        if (extension == null) {
            return false;
        }
        String ext = extension.toLowerCase().replace(".", "");
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "bmp" -> true;
            case "webp" -> isWebPAvailable();
            default -> false;
        };
    }

    /**
     * Check if WebP support is available at runtime
     * 检查运行时是否支持 WebP
     *
     * <p>WebP support requires the TwelveMonkeys imageio-webp plugin.</p>
     * <p>WebP 支持需要 TwelveMonkeys imageio-webp 插件。</p>
     *
     * @return true if WebP plugin is available | 如果 WebP 插件可用返回 true
     */
    public static boolean isWebPAvailable() {
        return javax.imageio.ImageIO.getImageReadersByFormatName("webp").hasNext();
    }

    /**
     * Check if this format is available at runtime
     * 检查此格式在运行时是否可用
     *
     * <p>Most formats are always available, but WEBP requires an optional plugin.</p>
     * <p>大多数格式始终可用，但 WEBP 需要可选插件。</p>
     *
     * @return true if format is available | 如果格式可用返回 true
     */
    public boolean isAvailable() {
        if (this == WEBP) {
            return isWebPAvailable();
        }
        return true;
    }
}
