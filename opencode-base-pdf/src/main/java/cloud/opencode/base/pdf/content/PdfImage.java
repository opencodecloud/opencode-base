package cloud.opencode.base.pdf.content;

import java.nio.file.Path;

/**
 * PDF Image Element - Embedded image content
 * PDF 图像元素 - 嵌入的图像内容
 *
 * <p>Supports PNG, JPEG, and other common image formats.</p>
 * <p>支持 PNG、JPEG 和其他常见图像格式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Load images from file path or byte array - 从文件路径或字节数组加载图像</li>
 *   <li>Automatic format detection - 自动格式检测</li>
 *   <li>Position, size, rotation, and opacity control - 位置、大小、旋转和不透明度控制</li>
 *   <li>Aspect-ratio-preserving scaling - 保持纵横比的缩放</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PdfImage logo = PdfImage.from(Path.of("logo.png"))
 *     .position(50, 750)
 *     .size(100, 50);
 *
 * PdfImage photo = PdfImage.from(bytes, ImageFormat.JPEG)
 *     .position(100, 400)
 *     .scaleToWidth(200)
 *     .opacity(0.8f);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No — mutable builder pattern - 线程安全: 否 — 可变构建器模式</li>
 *   <li>Null-safe: No — callers must ensure non-null values - 空值安全: 否 — 调用方需确保非空值</li>
 *   <li>Defensive copies: Byte arrays are cloned on input and output - 防御性拷贝: 字节数组在输入和输出时进行克隆</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public final class PdfImage implements PdfElement {

    private Path sourcePath;
    private byte[] sourceBytes;
    private ImageFormat format;
    private float x;
    private float y;
    private float width = -1;
    private float height = -1;
    private float rotation;
    private float opacity = 1f;

    private PdfImage() {}

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates image from file path
     * 从文件路径创建图像
     *
     * @param path image file path | 图像文件路径
     * @return PdfImage instance | PdfImage 实例
     */
    public static PdfImage from(Path path) {
        PdfImage image = new PdfImage();
        image.sourcePath = path;
        image.format = detectFormat(path.toString());
        return image;
    }

    /**
     * Creates image from byte array
     * 从字节数组创建图像
     *
     * @param bytes  image bytes | 图像字节
     * @param format image format | 图像格式
     * @return PdfImage instance | PdfImage 实例
     */
    public static PdfImage from(byte[] bytes, ImageFormat format) {
        PdfImage image = new PdfImage();
        image.sourceBytes = bytes.clone();
        image.format = format;
        return image;
    }

    /**
     * Creates an image builder
     * 创建图像构建器
     *
     * @return new PdfImage instance | 新 PdfImage 实例
     */
    public static PdfImage builder() {
        return new PdfImage();
    }

    // ==================== Builder Methods | 构建方法 ====================

    /**
     * Sets image from file
     * 从文件设置图像
     *
     * @param imagePath image file path | 图像文件路径
     * @return this image for chaining | 当前图像用于链式调用
     */
    public PdfImage source(Path imagePath) {
        this.sourcePath = imagePath;
        this.format = detectFormat(imagePath.toString());
        return this;
    }

    /**
     * Sets image from bytes
     * 从字节设置图像
     *
     * @param imageBytes image bytes | 图像字节
     * @param format     image format | 图像格式
     * @return this image for chaining | 当前图像用于链式调用
     */
    public PdfImage source(byte[] imageBytes, ImageFormat format) {
        this.sourceBytes = imageBytes.clone();
        this.format = format;
        return this;
    }

    /**
     * Sets position
     * 设置位置
     *
     * @param x x coordinate | x 坐标
     * @param y y coordinate | y 坐标
     * @return this image for chaining | 当前图像用于链式调用
     */
    public PdfImage position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Sets size
     * 设置大小
     *
     * @param width  image width | 图像宽度
     * @param height image height | 图像高度
     * @return this image for chaining | 当前图像用于链式调用
     */
    public PdfImage size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * Scales to fit width (maintains aspect ratio)
     * 缩放以适应宽度（保持纵横比）
     *
     * @param width target width | 目标宽度
     * @return this image for chaining | 当前图像用于链式调用
     */
    public PdfImage scaleToWidth(float width) {
        this.width = width;
        this.height = -1;
        return this;
    }

    /**
     * Scales to fit height (maintains aspect ratio)
     * 缩放以适应高度（保持纵横比）
     *
     * @param height target height | 目标高度
     * @return this image for chaining | 当前图像用于链式调用
     */
    public PdfImage scaleToHeight(float height) {
        this.width = -1;
        this.height = height;
        return this;
    }

    /**
     * Sets rotation
     * 设置旋转
     *
     * @param degrees rotation in degrees | 旋转角度
     * @return this image for chaining | 当前图像用于链式调用
     */
    public PdfImage rotation(float degrees) {
        this.rotation = degrees;
        return this;
    }

    /**
     * Sets opacity
     * 设置不透明度
     *
     * @param opacity opacity (0.0 - 1.0) | 不透明度
     * @return this image for chaining | 当前图像用于链式调用
     */
    public PdfImage opacity(float opacity) {
        this.opacity = opacity;
        return this;
    }

    // ==================== Accessors | 访问方法 ====================

    public Path getSourcePath() {
        return sourcePath;
    }

    public byte[] getSourceBytes() {
        return sourceBytes != null ? sourceBytes.clone() : null;
    }

    public ImageFormat getFormat() {
        return format;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getRotation() {
        return rotation;
    }

    public float getOpacity() {
        return opacity;
    }

    private static ImageFormat detectFormat(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png")) return ImageFormat.PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return ImageFormat.JPEG;
        if (lower.endsWith(".gif")) return ImageFormat.GIF;
        if (lower.endsWith(".bmp")) return ImageFormat.BMP;
        return ImageFormat.PNG;
    }

    /**
     * Supported image formats
     * 支持的图像格式
     */
    public enum ImageFormat {
        /** PNG format | PNG 格式 */
        PNG,
        /** JPEG format | JPEG 格式 */
        JPEG,
        /** GIF format | GIF 格式 */
        GIF,
        /** BMP format | BMP 格式 */
        BMP
    }
}
