package cloud.opencode.base.image;

import cloud.opencode.base.image.exception.ImageIOException;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.exception.ImageWriteException;
import cloud.opencode.base.image.internal.*;
import cloud.opencode.base.image.watermark.ImageWatermark;
import cloud.opencode.base.image.watermark.TextWatermark;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Image
 * 图片处理包装类
 *
 * <p>Chainable image processing wrapper with fluent API.</p>
 * <p>支持链式调用的图片处理包装类。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Chain operations
 * Image.from(path)
 *     .resize(800, 600)
 *     .rotate(90)
 *     .watermark(TextWatermark.of("Copyright"))
 *     .save(outputPath);
 *
 * // Get bytes
 * byte[] bytes = Image.from(inputStream)
 *     .crop(100, 100, 400, 300)
 *     .toBytes(ImageFormat.PNG);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Chainable fluent API for image operations - 可链式调用的流式图片操作 API</li>
 *   <li>Resize, crop, rotate, watermark operations - 缩放、裁剪、旋转、水印操作</li>
 *   <li>Save to file or convert to byte array - 保存到文件或转换为字节数组</li>
 *   <li>Multiple format support (JPEG, PNG, GIF, BMP) - 多格式支持（JPEG、PNG、GIF、BMP）</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable internal state) - 线程安全: 否（可变内部状态）</li>
 *   <li>Null-safe: No (throws on null image) - 空值安全: 否（null 图片抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
public class Image {

    private BufferedImage image;
    private ImageFormat format;

    /**
     * Create image wrapper
     * 创建图片包装器
     *
     * @param image the buffered image | 缓冲图片
     */
    public Image(BufferedImage image) {
        this.image = image;
        this.format = ImageFormat.PNG;
    }

    /**
     * Create image wrapper with format
     * 创建带格式的图片包装器
     *
     * @param image the buffered image | 缓冲图片
     * @param format the image format | 图片格式
     */
    public Image(BufferedImage image, ImageFormat format) {
        this.image = image;
        this.format = format;
    }

    /**
     * Create image from path
     * 从路径创建图片
     *
     * @param path the image path | 图片路径
     * @return the image wrapper | 图片包装器
     * @throws ImageIOException if reading fails | 如果读取失败
     */
    public static Image from(Path path) throws ImageIOException {
        return OpenImage.read(path);
    }

    /**
     * Create image from bytes
     * 从字节数组创建图片
     *
     * @param bytes the image bytes | 图片字节数组
     * @return the image wrapper | 图片包装器
     * @throws ImageIOException if reading fails | 如果读取失败
     */
    public static Image from(byte[] bytes) throws ImageIOException {
        return OpenImage.read(bytes);
    }

    /**
     * Create image from BufferedImage
     * 从BufferedImage创建图片
     *
     * @param image the buffered image | 缓冲图片
     * @return the image wrapper | 图片包装器
     */
    public static Image from(BufferedImage image) {
        return new Image(image);
    }

    /**
     * Get the underlying BufferedImage
     * 获取底层BufferedImage
     *
     * <p><strong>Warning:</strong> Returns the internal mutable reference. Modifications
     * to the returned BufferedImage will affect this Image instance. Use
     * {@link #copyBufferedImage()} if you need an independent copy.</p>
     * <p><strong>警告：</strong>返回内部可变引用。对返回的BufferedImage的修改将影响此Image实例。
     * 如果需要独立副本，请使用 {@link #copyBufferedImage()}。</p>
     *
     * @return the buffered image (mutable reference) | 缓冲图片（可变引用）
     */
    public BufferedImage getBufferedImage() {
        return image;
    }

    /**
     * Get a copy of the underlying BufferedImage
     * 获取底层BufferedImage的副本
     *
     * <p>Returns an independent copy that can be modified without affecting
     * this Image instance.</p>
     * <p>返回一个可以修改而不影响此Image实例的独立副本。</p>
     *
     * @return a copy of the buffered image | 缓冲图片的副本
     */
    public BufferedImage copyBufferedImage() {
        BufferedImage copy = new BufferedImage(
            image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g = copy.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return copy;
    }

    /**
     * Get the image format
     * 获取图片格式
     *
     * @return the format | 格式
     */
    public ImageFormat getFormat() {
        return format;
    }

    /**
     * Set the image format
     * 设置图片格式
     *
     * @param format the format | 格式
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image format(ImageFormat format) {
        this.format = format;
        return this;
    }

    /**
     * Get image width
     * 获取图片宽度
     *
     * @return the width in pixels | 宽度（像素）
     */
    public int getWidth() {
        return image.getWidth();
    }

    /**
     * Get image height
     * 获取图片高度
     *
     * @return the height in pixels | 高度（像素）
     */
    public int getHeight() {
        return image.getHeight();
    }

    /**
     * Get image info
     * 获取图片信息
     *
     * @return the image info | 图片信息
     */
    public ImageInfo getInfo() {
        return new ImageInfo(getWidth(), getHeight(), format);
    }

    // ==================== Resize Operations ====================

    /**
     * Resize to exact dimensions
     * 调整到精确尺寸
     *
     * @param width target width | 目标宽度
     * @param height target height | 目标高度
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image resize(int width, int height) {
        this.image = ResizeOp.resize(image, width, height);
        return this;
    }

    /**
     * Resize maintaining aspect ratio
     * 保持宽高比调整尺寸
     *
     * @param maxWidth maximum width | 最大宽度
     * @param maxHeight maximum height | 最大高度
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image resizeToFit(int maxWidth, int maxHeight) {
        this.image = ResizeOp.resizeToFit(image, maxWidth, maxHeight);
        return this;
    }

    /**
     * Scale by percentage
     * 按百分比缩放
     *
     * @param scale scale factor (e.g., 0.5 for 50%) | 缩放因子（如0.5表示50%）
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image scale(double scale) {
        this.image = ResizeOp.scale(image, scale);
        return this;
    }

    /**
     * Scale to fit width
     * 缩放以适应宽度
     *
     * @param width target width | 目标宽度
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image scaleToWidth(int width) {
        this.image = ResizeOp.scaleToWidth(image, width);
        return this;
    }

    /**
     * Scale to fit height
     * 缩放以适应高度
     *
     * @param height target height | 目标高度
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image scaleToHeight(int height) {
        this.image = ResizeOp.scaleToHeight(image, height);
        return this;
    }

    // ==================== Crop Operations ====================

    /**
     * Crop to specified region
     * 裁剪到指定区域
     *
     * @param x the x coordinate | X坐标
     * @param y the y coordinate | Y坐标
     * @param width the crop width | 裁剪宽度
     * @param height the crop height | 裁剪高度
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image crop(int x, int y, int width, int height) {
        this.image = CropOp.crop(image, x, y, width, height);
        return this;
    }

    /**
     * Crop from center
     * 从中心裁剪
     *
     * @param width the crop width | 裁剪宽度
     * @param height the crop height | 裁剪高度
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image cropCenter(int width, int height) {
        this.image = CropOp.cropCenter(image, width, height);
        return this;
    }

    /**
     * Crop to square from center
     * 从中心裁剪为正方形
     *
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image cropSquare() {
        this.image = CropOp.cropSquare(image);
        return this;
    }

    // ==================== Rotate Operations ====================

    /**
     * Rotate by degrees
     * 按角度旋转
     *
     * @param degrees rotation degrees | 旋转角度
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image rotate(double degrees) {
        this.image = RotateOp.rotate(image, degrees);
        return this;
    }

    /**
     * Rotate 90 degrees clockwise
     * 顺时针旋转90度
     *
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image rotate90() {
        this.image = RotateOp.rotate90(image);
        return this;
    }

    /**
     * Rotate 180 degrees
     * 旋转180度
     *
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image rotate180() {
        this.image = RotateOp.rotate180(image);
        return this;
    }

    /**
     * Rotate 270 degrees (90 counter-clockwise)
     * 旋转270度（逆时针90度）
     *
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image rotate270() {
        this.image = RotateOp.rotate270(image);
        return this;
    }

    /**
     * Flip horizontally
     * 水平翻转
     *
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image flipHorizontal() {
        this.image = RotateOp.flipHorizontal(image);
        return this;
    }

    /**
     * Flip vertically
     * 垂直翻转
     *
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image flipVertical() {
        this.image = RotateOp.flipVertical(image);
        return this;
    }

    // ==================== Watermark Operations ====================

    /**
     * Add text watermark
     * 添加文字水印
     *
     * @param watermark the text watermark | 文字水印
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image watermark(TextWatermark watermark) {
        this.image = WatermarkOp.apply(image, watermark);
        return this;
    }

    /**
     * Add text watermark at position
     * 在指定位置添加文字水印
     *
     * @param text the watermark text | 水印文字
     * @param position the position | 位置
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image watermark(String text, Position position) {
        this.image = WatermarkOp.apply(image, TextWatermark.of(text, position));
        return this;
    }

    /**
     * Add image watermark
     * 添加图片水印
     *
     * @param watermark the image watermark | 图片水印
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image watermark(ImageWatermark watermark) {
        this.image = WatermarkOp.apply(image, watermark);
        return this;
    }

    /**
     * Add image watermark at position
     * 在指定位置添加图片水印
     *
     * @param watermarkImage the watermark image | 水印图片
     * @param position the position | 位置
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image watermark(BufferedImage watermarkImage, Position position) {
        this.image = WatermarkOp.apply(image, new ImageWatermark(watermarkImage, position, 1.0f, 10));
        return this;
    }

    // ==================== Compression Operations ====================

    /**
     * Compress image with quality
     * 按质量压缩图片
     *
     * @param quality quality from 0.0 to 1.0 | 质量（0.0到1.0）
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image compress(float quality) {
        this.image = CompressOp.compress(image, quality);
        return this;
    }

    // ==================== Convert Operations ====================

    /**
     * Convert to specified format
     * 转换到指定格式
     *
     * @param targetFormat the target format | 目标格式
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image convert(ImageFormat targetFormat) {
        this.image = ConvertOp.convert(image, this.format, targetFormat);
        this.format = targetFormat;
        return this;
    }

    /**
     * Convert to grayscale
     * 转换为灰度图
     *
     * @return this image for chaining | 用于链式调用的图片
     */
    public Image grayscale() {
        this.image = ConvertOp.grayscale(image);
        return this;
    }

    // ==================== Output Operations ====================

    /**
     * Save to path
     * 保存到路径
     *
     * @param path the output path | 输出路径
     * @throws ImageWriteException if writing fails | 如果写入失败
     */
    public void save(Path path) throws ImageWriteException {
        OpenImage.write(image, path, format);
    }

    /**
     * Save to path with format
     * 保存到路径（指定格式）
     *
     * @param path the output path | 输出路径
     * @param format the image format | 图片格式
     * @throws ImageWriteException if writing fails | 如果写入失败
     */
    public void save(Path path, ImageFormat format) throws ImageWriteException {
        OpenImage.write(image, path, format);
    }

    /**
     * Write to output stream
     * 写入输出流
     *
     * @param out the output stream | 输出流
     * @throws ImageWriteException if writing fails | 如果写入失败
     */
    public void writeTo(OutputStream out) throws ImageWriteException {
        writeTo(out, format);
    }

    /**
     * Write to output stream with format
     * 写入输出流（指定格式）
     *
     * @param out the output stream | 输出流
     * @param format the image format | 图片格式
     * @throws ImageWriteException if writing fails | 如果写入失败
     */
    public void writeTo(OutputStream out, ImageFormat format) throws ImageWriteException {
        try {
            ImageIO.write(image, format.getExtension(), out);
        } catch (IOException e) {
            throw new ImageWriteException(Path.of("stream"), e);
        }
    }

    /**
     * Convert to byte array
     * 转换为字节数组
     *
     * @return the byte array | 字节数组
     * @throws ImageOperationException if conversion fails | 如果转换失败
     */
    public byte[] toBytes() throws ImageOperationException {
        return toBytes(format);
    }

    /**
     * Convert to byte array with format
     * 转换为字节数组（指定格式）
     *
     * @param format the image format | 图片格式
     * @return the byte array | 字节数组
     * @throws ImageOperationException if conversion fails | 如果转换失败
     */
    public byte[] toBytes(ImageFormat format) throws ImageOperationException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, format.getExtension(), baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ImageOperationException("toBytes", e);
        }
    }

    /**
     * Convert to Base64 string
     * 转换为Base64字符串
     *
     * @return the Base64 encoded string | Base64编码字符串
     * @throws ImageOperationException if conversion fails | 如果转换失败
     */
    public String toBase64() throws ImageOperationException {
        return toBase64(format);
    }

    /**
     * Convert to Base64 string with format
     * 转换为Base64字符串（指定格式）
     *
     * @param format the image format | 图片格式
     * @return the Base64 encoded string | Base64编码字符串
     * @throws ImageOperationException if conversion fails | 如果转换失败
     */
    public String toBase64(ImageFormat format) throws ImageOperationException {
        return java.util.Base64.getEncoder().encodeToString(toBytes(format));
    }

    /**
     * Create a copy of this image
     * 创建此图片的副本
     *
     * @return a new Image instance | 新的Image实例
     */
    public Image copy() {
        BufferedImage copy = new BufferedImage(
            image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g = copy.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return new Image(copy, format);
    }
}
