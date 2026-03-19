package cloud.opencode.base.image;

import cloud.opencode.base.image.exception.*;
import cloud.opencode.base.image.validation.ImageValidator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * OpenImage
 * 图片处理工具类
 *
 * <p>Static utility class for image operations. Entry point for the image processing API.</p>
 * <p>图片操作的静态工具类。图片处理API的入口点。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Read and get info
 * ImageInfo info = OpenImage.getInfo(Path.of("photo.jpg"));
 *
 * // Read as Image wrapper
 * Image image = OpenImage.read(path)
 *     .resize(800, 600)
 *     .save(outputPath);
 *
 * // Quick resize
 * OpenImage.resize(inputPath, outputPath, 800, 600);
 *
 * // Convert format
 * OpenImage.convert(inputPath, outputPath, ImageFormat.PNG);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Static utility entry point for image processing - 图片处理的静态工具入口点</li>
 *   <li>Read, resize, crop, rotate, convert operations - 读取、缩放、裁剪、旋转、转换操作</li>
 *   <li>Image info extraction without full loading - 无需完全加载的图片信息提取</li>
 *   <li>Format detection from file content - 从文件内容检测格式</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (throws on null path/stream) - 空值安全: 否（null 路径/流抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
public final class OpenImage {

    /**
     * Maximum number of pixels allowed when loading an image (100 megapixels).
     * Images exceeding this limit will be rejected to prevent out-of-memory errors.
     * 加载图片时允许的最大像素数（1 亿像素）。超过此限制的图片将被拒绝以防止内存溢出。
     */
    private static final long MAX_PIXELS = 100_000_000L;

    private OpenImage() {
        // Utility class
    }

    /**
     * Check image dimensions before full load to prevent OOM on very large images.
     * 在完全加载之前检查图片尺寸，以防止非常大的图片导致内存溢出。
     *
     * @param input the input source (File, InputStream, etc.) compatible with
     *              {@link ImageIO#createImageInputStream(Object)}
     * @throws ImageIOException if the image is too large or dimensions cannot be read
     */
    private static void checkDimensions(Object input) throws ImageIOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(input)) {
            if (iis == null) {
                return; // Cannot check; let ImageIO.read() handle the error
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(iis);
                    int width = reader.getWidth(0);
                    int height = reader.getHeight(0);
                    long pixels = (long) width * height;
                    if (pixels > MAX_PIXELS) {
                        throw new ImageIOException(
                                "Image too large: " + width + "x" + height +
                                " (" + pixels + " pixels exceeds limit of " + MAX_PIXELS + ")");
                    }
                } finally {
                    reader.dispose();
                }
            }
        } catch (ImageIOException e) {
            throw e;
        } catch (IOException e) {
            // Cannot check dimensions; let ImageIO.read() handle the error downstream
        }
    }

    // ==================== Read Operations ====================

    /**
     * Read image from path
     * 从路径读取图片
     *
     * @param path the image path | 图片路径
     * @return the image wrapper | 图片包装器
     * @throws ImageReadException if reading fails | 如果读取失败
     */
    public static Image read(Path path) throws ImageReadException {
        try {
            checkDimensions(path.toFile());
            BufferedImage image = ImageIO.read(path.toFile());
            if (image == null) {
                throw new ImageReadException(path, new IOException("Cannot read image"));
            }
            ImageFormat format = detectFormat(path);
            return new Image(image, format);
        } catch (ImageIOException e) {
            throw new ImageReadException(path, e);
        } catch (IOException e) {
            throw new ImageReadException(path, e);
        }
    }

    /**
     * Read image from input stream
     * 从输入流读取图片
     *
     * @param in the input stream | 输入流
     * @return the image wrapper | 图片包装器
     * @throws ImageIOException if reading fails | 如果读取失败
     */
    public static Image read(InputStream in) throws ImageIOException {
        try {
            // Buffer the stream so we can check dimensions before full load
            byte[] bytes = in.readAllBytes();
            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                checkDimensions(bais);
            }
            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                BufferedImage image = ImageIO.read(bais);
                if (image == null) {
                    throw new ImageIOException("Cannot read image from stream");
                }
                return new Image(image);
            }
        } catch (ImageIOException e) {
            throw e;
        } catch (IOException e) {
            throw new ImageIOException("Failed to read image from stream", e);
        }
    }

    /**
     * Read image from byte array
     * 从字节数组读取图片
     *
     * @param bytes the image bytes | 图片字节数组
     * @return the image wrapper | 图片包装器
     * @throws ImageIOException if reading fails | 如果读取失败
     */
    public static Image read(byte[] bytes) throws ImageIOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            return read(bais);
        } catch (IOException e) {
            throw new ImageIOException("Failed to read image from bytes", e);
        }
    }

    /**
     * Read image from Base64 string
     * 从Base64字符串读取图片
     *
     * @param base64 the Base64 encoded image | Base64编码的图片
     * @return the image wrapper | 图片包装器
     * @throws ImageIOException if reading fails | 如果读取失败
     */
    public static Image fromBase64(String base64) throws ImageIOException {
        if (base64 == null || base64.isBlank()) {
            throw new ImageIOException("Base64 string cannot be null or blank");
        }
        // Remove data URI prefix if present (e.g., "data:image/png;base64,")
        String cleanBase64 = base64;
        if (base64.contains(",")) {
            cleanBase64 = base64.substring(base64.indexOf(",") + 1);
        }
        byte[] bytes = java.util.Base64.getDecoder().decode(cleanBase64);
        return read(bytes);
    }

    /**
     * Read BufferedImage directly from path
     * 直接从路径读取BufferedImage
     *
     * @param path the image path | 图片路径
     * @return the buffered image | 缓冲图片
     * @throws ImageReadException if reading fails | 如果读取失败
     */
    public static BufferedImage readBufferedImage(Path path) throws ImageReadException {
        return read(path).getBufferedImage();
    }

    // ==================== Write Operations ====================

    /**
     * Write image to path
     * 写入图片到路径
     *
     * @param image the buffered image | 缓冲图片
     * @param path the output path | 输出路径
     * @throws ImageWriteException if writing fails | 如果写入失败
     */
    public static void write(BufferedImage image, Path path) throws ImageWriteException {
        ImageFormat format = detectFormat(path);
        write(image, path, format);
    }

    /**
     * Write image to path with format
     * 写入图片到路径（指定格式）
     *
     * @param image the buffered image | 缓冲图片
     * @param path the output path | 输出路径
     * @param format the image format | 图片格式
     * @throws ImageWriteException if writing fails | 如果写入失败
     */
    public static void write(BufferedImage image, Path path, ImageFormat format) throws ImageWriteException {
        try {
            // Ensure parent directory exists
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            boolean success = ImageIO.write(image, format.getExtension(), path.toFile());
            if (!success) {
                throw new ImageWriteException(path, new IOException("No writer found for format: " + format));
            }
        } catch (IOException e) {
            throw new ImageWriteException(path, e);
        }
    }

    /**
     * Write image to output stream
     * 写入图片到输出流
     *
     * @param image the buffered image | 缓冲图片
     * @param out the output stream | 输出流
     * @param format the image format | 图片格式
     * @throws ImageIOException if writing fails | 如果写入失败
     */
    public static void write(BufferedImage image, OutputStream out, ImageFormat format) throws ImageIOException {
        try {
            boolean success = ImageIO.write(image, format.getExtension(), out);
            if (!success) {
                throw new ImageIOException("No writer found for format: " + format);
            }
        } catch (IOException e) {
            throw new ImageIOException("Failed to write image", e);
        }
    }

    // ==================== Info Operations ====================

    /**
     * Get image info from path
     * 从路径获取图片信息
     *
     * @param path the image path | 图片路径
     * @return the image info | 图片信息
     * @throws ImageReadException if reading fails | 如果读取失败
     */
    public static ImageInfo getInfo(Path path) throws ImageReadException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(path.toFile())) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                throw new ImageReadException(path, new IOException("No reader found"));
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(iis);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                ImageFormat format = detectFormat(path);
                long fileSize = Files.size(path);
                return new ImageInfo(width, height, format, fileSize);
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new ImageReadException(path, e);
        }
    }

    /**
     * Get image dimensions without fully loading
     * 获取图片尺寸（不完全加载）
     *
     * @param path the image path | 图片路径
     * @return array of [width, height] | [宽度, 高度]数组
     * @throws ImageReadException if reading fails | 如果读取失败
     */
    public static int[] getDimensions(Path path) throws ImageReadException {
        ImageInfo info = getInfo(path);
        return new int[]{info.width(), info.height()};
    }

    // ==================== Quick Operations ====================

    /**
     * Quick resize operation
     * 快速调整尺寸操作
     *
     * @param input the input path | 输入路径
     * @param output the output path | 输出路径
     * @param width the target width | 目标宽度
     * @param height the target height | 目标高度
     * @throws ImageIOException if operation fails | 如果操作失败
     */
    public static void resize(Path input, Path output, int width, int height) throws ImageIOException {
        read(input).resize(width, height).save(output);
    }

    /**
     * Quick crop operation
     * 快速裁剪操作
     *
     * @param input the input path | 输入路径
     * @param output the output path | 输出路径
     * @param x the x coordinate | X坐标
     * @param y the y coordinate | Y坐标
     * @param width the crop width | 裁剪宽度
     * @param height the crop height | 裁剪高度
     * @throws ImageIOException if operation fails | 如果操作失败
     */
    public static void crop(Path input, Path output, int x, int y, int width, int height) throws ImageIOException {
        read(input).crop(x, y, width, height).save(output);
    }

    /**
     * Quick rotate operation
     * 快速旋转操作
     *
     * @param input the input path | 输入路径
     * @param output the output path | 输出路径
     * @param degrees the rotation degrees | 旋转角度
     * @throws ImageIOException if operation fails | 如果操作失败
     */
    public static void rotate(Path input, Path output, double degrees) throws ImageIOException {
        read(input).rotate(degrees).save(output);
    }

    /**
     * Quick format conversion
     * 快速格式转换
     *
     * @param input the input path | 输入路径
     * @param output the output path | 输出路径
     * @param format the target format | 目标格式
     * @throws ImageIOException if operation fails | 如果操作失败
     */
    public static void convert(Path input, Path output, ImageFormat format) throws ImageIOException {
        read(input).convert(format).save(output, format);
    }

    /**
     * Quick compress operation
     * 快速压缩操作
     *
     * @param input the input path | 输入路径
     * @param output the output path | 输出路径
     * @param quality the quality (0.0 to 1.0) | 质量（0.0到1.0）
     * @throws ImageIOException if operation fails | 如果操作失败
     */
    public static void compress(Path input, Path output, float quality) throws ImageIOException {
        read(input).compress(quality).save(output);
    }

    /**
     * Quick thumbnail creation
     * 快速创建缩略图
     *
     * @param input  the input path | 输入路径
     * @param output the output path | 输出路径
     * @param size   the maximum dimension | 最大尺寸
     * @throws ImageIOException if operation fails | 如果操作失败
     */
    public static void thumbnail(Path input, Path output, int size) throws ImageIOException {
        Image img = read(input);
        int w = img.getWidth();
        int h = img.getHeight();
        double scale = (double) size / Math.max(w, h);
        int newW = (int) (w * scale);
        int newH = (int) (h * scale);
        img.resize(newW, newH).save(output);
    }

    /**
     * Create a thumbnail builder
     * 创建缩略图构建器
     *
     * <p>Returns a builder for creating thumbnails with various options.</p>
     * <p>返回用于创建各种选项缩略图的构建器。</p>
     *
     * <p><strong>Usage Example | 使用示例:</strong></p>
     * <pre>{@code
     * OpenImage.thumbnail()
     *     .source(Path.of("photo.jpg"))
     *     .size(200, 200)
     *     .format(ImageFormat.PNG)
     *     .output(Path.of("thumb.png"))
     *     .create();
     * }</pre>
     *
     * @return the thumbnail builder | 缩略图构建器
     */
    public static cloud.opencode.base.image.thumbnail.ThumbnailBuilder thumbnail() {
        return new cloud.opencode.base.image.thumbnail.ThumbnailBuilder();
    }

    /**
     * Quick horizontal flip
     * 快速水平翻转
     *
     * @param input  the input path | 输入路径
     * @param output the output path | 输出路径
     * @throws ImageIOException if operation fails | 如果操作失败
     */
    public static void flipHorizontal(Path input, Path output) throws ImageIOException {
        read(input).flipHorizontal().save(output);
    }

    /**
     * Quick vertical flip
     * 快速垂直翻转
     *
     * @param input  the input path | 输入路径
     * @param output the output path | 输出路径
     * @throws ImageIOException if operation fails | 如果操作失败
     */
    public static void flipVertical(Path input, Path output) throws ImageIOException {
        read(input).flipVertical().save(output);
    }

    /**
     * Quick grayscale conversion
     * 快速灰度转换
     *
     * @param input  the input path | 输入路径
     * @param output the output path | 输出路径
     * @throws ImageIOException if operation fails | 如果操作失败
     */
    public static void grayscale(Path input, Path output) throws ImageIOException {
        read(input).grayscale().save(output);
    }

    /**
     * Convert image to byte array
     * 将图片转换为字节数组
     *
     * @param image  the image | 图片
     * @param format the format | 格式
     * @return byte array | 字节数组
     * @throws ImageIOException if conversion fails | 如果转换失败
     */
    public static byte[] toBytes(BufferedImage image, ImageFormat format) throws ImageIOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            write(image, baos, format);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ImageIOException("Failed to convert image to bytes", e);
        }
    }

    /**
     * Convert image from path to byte array
     * 将路径中的图片转换为字节数组
     *
     * @param path the image path | 图片路径
     * @return byte array | 字节数组
     * @throws ImageIOException if conversion fails | 如果转换失败
     */
    public static byte[] toBytes(Path path) throws ImageIOException {
        Image img = read(path);
        return toBytes(img.getBufferedImage(), img.getFormat());
    }

    // ==================== Utility Methods ====================

    /**
     * Detect format from file path
     * 从文件路径检测格式
     *
     * @param path the file path | 文件路径
     * @return the detected format | 检测到的格式
     */
    public static ImageFormat detectFormat(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            String extension = fileName.substring(dotIndex + 1);
            try {
                return ImageFormat.fromExtension(extension);
            } catch (IllegalArgumentException e) {
                return ImageFormat.PNG;
            }
        }
        return ImageFormat.PNG;
    }

    /**
     * Check if file is a valid image
     * 检查文件是否为有效图片
     *
     * @param path the file path | 文件路径
     * @return true if valid image | 如果是有效图片返回true
     */
    public static boolean isValidImage(Path path) {
        return ImageValidator.isValidImage(path);
    }

    /**
     * Check if format is supported
     * 检查格式是否支持
     *
     * @param extension the file extension | 文件扩展名
     * @return true if supported | 如果支持返回true
     */
    public static boolean isSupported(String extension) {
        return ImageFormat.isSupported(extension);
    }

    /**
     * Create a blank image
     * 创建空白图片
     *
     * @param width the width | 宽度
     * @param height the height | 高度
     * @return the image wrapper | 图片包装器
     */
    public static Image createBlank(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        return new Image(image);
    }

    /**
     * Create a blank image with color
     * 创建带颜色的空白图片
     *
     * @param width the width | 宽度
     * @param height the height | 高度
     * @param color the background color (ARGB) | 背景颜色（ARGB）
     * @return the image wrapper | 图片包装器
     */
    public static Image createBlank(int width, int height, int color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, color);
            }
        }
        return new Image(image);
    }
}
