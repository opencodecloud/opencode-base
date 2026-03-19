package cloud.opencode.base.image;

/**
 * Image Info
 * 图片信息
 *
 * <p>Immutable record containing image metadata.</p>
 * <p>包含图片元数据的不可变记录。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ImageInfo info = OpenImage.getInfo(Path.of("photo.jpg"));
 * System.out.println(info.width() + "x" + info.height());
 * System.out.println("Format: " + info.format());
 * System.out.println("Size: " + info.fileSize() + " bytes");
 * }</pre>
 *
 * @param width the image width in pixels | 图片宽度（像素）
 * @param height the image height in pixels | 图片高度（像素）
 * @param format the image format | 图片格式
 * @param fileSize the file size in bytes | 文件大小（字节）
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable record containing image metadata - 包含图片元数据的不可变记录</li>
 *   <li>Width, height, format, and file size information - 宽度、高度、格式和文件大小信息</li>
 *   <li>Convenience constructors for common use cases - 常见用例的便捷构造函数</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (format and fileSize can be null/0) - 空值安全: 是（格式和文件大小可为 null/0）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
public record ImageInfo(
    int width,
    int height,
    ImageFormat format,
    long fileSize
) {

    /**
     * Create image info with dimensions only
     * 仅使用尺寸创建图片信息
     *
     * @param width the image width | 图片宽度
     * @param height the image height | 图片高度
     */
    public ImageInfo(int width, int height) {
        this(width, height, null, 0);
    }

    /**
     * Create image info with dimensions and format
     * 使用尺寸和格式创建图片信息
     *
     * @param width the image width | 图片宽度
     * @param height the image height | 图片高度
     * @param format the image format | 图片格式
     */
    public ImageInfo(int width, int height, ImageFormat format) {
        this(width, height, format, 0);
    }

    /**
     * Get aspect ratio
     * 获取宽高比
     *
     * @return the aspect ratio (width / height) | 宽高比
     */
    public double aspectRatio() {
        return height == 0 ? 0 : (double) width / height;
    }

    /**
     * Get total pixels
     * 获取总像素数
     *
     * @return the total number of pixels | 总像素数
     */
    public long pixels() {
        return (long) width * height;
    }

    /**
     * Get estimated memory size in bytes (assuming ARGB)
     * 获取估算的内存大小（字节，假设ARGB）
     *
     * @return estimated memory size | 估算的内存大小
     */
    public long estimatedMemorySize() {
        return pixels() * 4; // 4 bytes per pixel for ARGB
    }

    /**
     * Check if image is landscape orientation
     * 检查图片是否为横向
     *
     * @return true if landscape | 如果是横向返回true
     */
    public boolean isLandscape() {
        return width > height;
    }

    /**
     * Check if image is portrait orientation
     * 检查图片是否为纵向
     *
     * @return true if portrait | 如果是纵向返回true
     */
    public boolean isPortrait() {
        return height > width;
    }

    /**
     * Check if image is square
     * 检查图片是否为正方形
     *
     * @return true if square | 如果是正方形返回true
     */
    public boolean isSquare() {
        return width == height;
    }

    /**
     * Get file size in human-readable format
     * 获取人类可读的文件大小
     *
     * @return formatted file size | 格式化的文件大小
     */
    public String fileSizeFormatted() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }

    @Override
    public String toString() {
        return String.format("ImageInfo[%dx%d, format=%s, size=%s]",
            width, height,
            format != null ? format.name() : "unknown",
            fileSizeFormatted());
    }
}
