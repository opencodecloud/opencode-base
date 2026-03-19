package cloud.opencode.base.image.thumbnail;

import cloud.opencode.base.image.Image;
import cloud.opencode.base.image.ImageFormat;
import cloud.opencode.base.image.OpenImage;
import cloud.opencode.base.image.exception.ImageIOException;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.exception.ImageWriteException;
import cloud.opencode.base.image.internal.ResizeOp;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

/**
 * Thumbnail Builder
 * 缩略图构建器
 *
 * <p>Builder for creating thumbnails with various options.</p>
 * <p>用于创建各种选项缩略图的构建器。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple thumbnail
 * ThumbnailBuilder.of(imagePath)
 *     .size(200, 200)
 *     .save(thumbnailPath);
 *
 * // With crop
 * ThumbnailBuilder.of(image)
 *     .size(150, 150)
 *     .crop(true)
 *     .quality(0.85f)
 *     .format(ImageFormat.JPEG)
 *     .save(outputPath);
 *
 * // Get as bytes
 * byte[] bytes = ThumbnailBuilder.of(inputPath)
 *     .size(100, 100)
 *     .toBytes();
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent builder API for thumbnail generation - 用于缩略图生成的流式构建器 API</li>
 *   <li>Configurable size, crop mode, quality, and format - 可配置的大小、裁剪模式、质量和格式</li>
 *   <li>Save to file or convert to byte array - 保存到文件或转换为字节数组</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable builder) - 线程安全: 否（可变构建器）</li>
 *   <li>Null-safe: No (throws on null source) - 空值安全: 否（null 源抛异常）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(W*H) for build() where W*H is the source pixel count - pixel resampling visits every source pixel - 时间复杂度: O(W*H)，W*H 为源图片像素数 - 像素重采样需访问每个源像素</li>
 *   <li>Space complexity: O(w*h) where w*h is the target thumbnail dimensions - output image buffer proportional to target size - 空间复杂度: O(w*h)，w*h 为目标缩略图尺寸 - 输出图像缓冲区与目标大小成正比</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
public class ThumbnailBuilder {

    private BufferedImage source;
    private Path outputPath;
    private int width = 100;
    private int height = 100;
    private boolean crop = false;
    private float quality = 0.85f;
    private ImageFormat format = ImageFormat.JPEG;

    /**
     * Create empty thumbnail builder
     * 创建空的缩略图构建器
     *
     * <p>Use {@link #source(Path)} or {@link #source(Image)} to set the source image.</p>
     * <p>使用 {@link #source(Path)} 或 {@link #source(Image)} 设置源图片。</p>
     */
    public ThumbnailBuilder() {
    }

    /**
     * Create thumbnail builder from BufferedImage
     * 从BufferedImage创建缩略图构建器
     *
     * @param source the source image | 源图片
     */
    public ThumbnailBuilder(BufferedImage source) {
        if (source == null) {
            throw new IllegalArgumentException("Source image cannot be null");
        }
        this.source = source;
    }

    /**
     * Create thumbnail builder from path
     * 从路径创建缩略图构建器
     *
     * @param path the image path | 图片路径
     * @return the builder | 构建器
     * @throws ImageIOException if reading fails | 如果读取失败
     */
    public static ThumbnailBuilder of(Path path) throws ImageIOException {
        return new ThumbnailBuilder(OpenImage.readBufferedImage(path));
    }

    /**
     * Create thumbnail builder from Image
     * 从Image创建缩略图构建器
     *
     * @param image the image | 图片
     * @return the builder | 构建器
     */
    public static ThumbnailBuilder of(Image image) {
        return new ThumbnailBuilder(image.getBufferedImage());
    }

    /**
     * Create thumbnail builder from BufferedImage
     * 从BufferedImage创建缩略图构建器
     *
     * @param image the buffered image | 缓冲图片
     * @return the builder | 构建器
     */
    public static ThumbnailBuilder of(BufferedImage image) {
        return new ThumbnailBuilder(image);
    }

    /**
     * Create thumbnail builder from bytes
     * 从字节数组创建缩略图构建器
     *
     * @param bytes the image bytes | 图片字节数组
     * @return the builder | 构建器
     * @throws ImageIOException if reading fails | 如果读取失败
     */
    public static ThumbnailBuilder of(byte[] bytes) throws ImageIOException {
        return new ThumbnailBuilder(OpenImage.read(bytes).getBufferedImage());
    }

    /**
     * Set thumbnail size
     * 设置缩略图尺寸
     *
     * @param width the width | 宽度
     * @param height the height | 高度
     * @return this builder | 构建器
     */
    public ThumbnailBuilder size(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * Set thumbnail width (height auto-calculated)
     * 设置缩略图宽度（高度自动计算）
     *
     * @param width the width | 宽度
     * @return this builder | 构建器
     */
    public ThumbnailBuilder width(int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("Width must be positive");
        }
        this.width = width;
        double ratio = (double) source.getWidth() / source.getHeight();
        this.height = (int) (width / ratio);
        return this;
    }

    /**
     * Set thumbnail height (width auto-calculated)
     * 设置缩略图高度（宽度自动计算）
     *
     * @param height the height | 高度
     * @return this builder | 构建器
     */
    public ThumbnailBuilder height(int height) {
        if (height <= 0) {
            throw new IllegalArgumentException("Height must be positive");
        }
        this.height = height;
        double ratio = (double) source.getWidth() / source.getHeight();
        this.width = (int) (height * ratio);
        return this;
    }

    /**
     * Enable/disable cropping to fit exact size
     * 启用/禁用裁剪以适应精确尺寸
     *
     * @param crop true to crop | 是否裁剪
     * @return this builder | 构建器
     */
    public ThumbnailBuilder crop(boolean crop) {
        this.crop = crop;
        return this;
    }

    /**
     * Set output quality (for JPEG)
     * 设置输出质量（用于JPEG）
     *
     * @param quality quality from 0.0 to 1.0 | 质量（0.0到1.0）
     * @return this builder | 构建器
     */
    public ThumbnailBuilder quality(float quality) {
        if (quality < 0 || quality > 1) {
            throw new IllegalArgumentException("Quality must be between 0.0 and 1.0");
        }
        this.quality = quality;
        return this;
    }

    /**
     * Set output format
     * 设置输出格式
     *
     * @param format the format | 格式
     * @return this builder | 构建器
     */
    public ThumbnailBuilder format(ImageFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("Format cannot be null");
        }
        this.format = format;
        return this;
    }

    /**
     * Set source image from path
     * 从路径设置源图片
     *
     * @param path the source image path | 源图片路径
     * @return this builder | 构建器
     * @throws ImageIOException if reading fails | 如果读取失败
     */
    public ThumbnailBuilder source(Path path) throws ImageIOException {
        this.source = OpenImage.readBufferedImage(path);
        return this;
    }

    /**
     * Set source image from Image
     * 从Image设置源图片
     *
     * @param image the source image | 源图片
     * @return this builder | 构建器
     */
    public ThumbnailBuilder source(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("Source image cannot be null");
        }
        this.source = image.getBufferedImage();
        return this;
    }

    /**
     * Set output path
     * 设置输出路径
     *
     * @param path the output path | 输出路径
     * @return this builder | 构建器
     */
    public ThumbnailBuilder output(Path path) {
        this.outputPath = path;
        return this;
    }

    /**
     * Create the thumbnail and save to output path
     * 创建缩略图并保存到输出路径
     *
     * <p>This method combines {@link #build()} and {@link #save(Path)}.</p>
     * <p>此方法组合了 {@link #build()} 和 {@link #save(Path)}。</p>
     *
     * @throws ImageWriteException if writing fails | 如果写入失败
     * @throws IllegalStateException if source or output is not set | 如果源或输出未设置
     */
    public void create() throws ImageWriteException {
        if (source == null) {
            throw new IllegalStateException("Source image must be set");
        }
        if (outputPath == null) {
            throw new IllegalStateException("Output path must be set");
        }
        save(outputPath);
    }

    /**
     * Build thumbnail
     * 构建缩略图
     *
     * @return the thumbnail image | 缩略图
     * @throws IllegalStateException if source is not set | 如果源未设置
     */
    public BufferedImage build() {
        if (source == null) {
            throw new IllegalStateException("Source image must be set");
        }
        BufferedImage result;

        if (crop) {
            // Crop to fit exact size
            result = cropAndResize();
        } else {
            // Resize maintaining aspect ratio
            result = ResizeOp.resizeToFit(source, width, height);
        }

        return result;
    }

    /**
     * Build and return as Image wrapper
     * 构建并返回Image包装器
     *
     * @return the Image wrapper | 图片包装器
     */
    public Image toImage() {
        return new Image(build(), format);
    }

    /**
     * Build and save to path
     * 构建并保存到路径
     *
     * @param path the output path | 输出路径
     * @throws ImageWriteException if writing fails | 如果写入失败
     */
    public void save(Path path) throws ImageWriteException {
        BufferedImage thumbnail = build();
        OpenImage.write(thumbnail, path, format);
    }

    /**
     * Build and return as bytes
     * 构建并返回字节数组
     *
     * @return the thumbnail bytes | 缩略图字节数组
     * @throws ImageOperationException if conversion fails | 如果转换失败
     */
    public byte[] toBytes() throws ImageOperationException {
        return toImage().toBytes(format);
    }

    /**
     * Crop and resize to exact dimensions
     * 裁剪并调整到精确尺寸
     */
    private BufferedImage cropAndResize() {
        int srcWidth = source.getWidth();
        int srcHeight = source.getHeight();

        double srcRatio = (double) srcWidth / srcHeight;
        double targetRatio = (double) width / height;

        int cropWidth, cropHeight, cropX, cropY;

        if (srcRatio > targetRatio) {
            // Source is wider, crop horizontally
            cropHeight = srcHeight;
            cropWidth = (int) (srcHeight * targetRatio);
            cropX = (srcWidth - cropWidth) / 2;
            cropY = 0;
        } else {
            // Source is taller, crop vertically
            cropWidth = srcWidth;
            cropHeight = (int) (srcWidth / targetRatio);
            cropX = 0;
            cropY = (srcHeight - cropHeight) / 2;
        }

        BufferedImage cropped = source.getSubimage(cropX, cropY, cropWidth, cropHeight);
        return ResizeOp.resize(cropped, width, height);
    }
}
