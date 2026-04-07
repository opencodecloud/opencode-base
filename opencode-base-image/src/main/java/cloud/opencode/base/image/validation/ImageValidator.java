package cloud.opencode.base.image.validation;

import cloud.opencode.base.image.ImageFormat;
import cloud.opencode.base.image.exception.ImageTooLargeException;
import cloud.opencode.base.image.exception.ImageValidationException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Image Validator
 * 图片验证器
 *
 * <p>Validates images for format, size, and security.</p>
 * <p>验证图片的格式、大小和安全性。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Validate file
 * ImageValidator.validate(path);
 *
 * // Validate with limits
 * ImageValidator.validate(path, 10_000_000, 4000, 4000);
 *
 * // Check magic numbers
 * boolean valid = ImageValidator.checkMagicNumber(bytes);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>File size and dimension validation - 文件大小和尺寸验证</li>
 *   <li>Magic number verification for format detection - 用于格式检测的魔数验证</li>
 *   <li>Configurable maximum file size and dimensions - 可配置的最大文件大小和尺寸</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
public final class ImageValidator {

    /**
     * Default maximum file size (10 MB)
     * 默认最大文件大小（10 MB）
     */
    public static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Default maximum width (8000 pixels)
     * 默认最大宽度（8000 像素）
     */
    public static final int DEFAULT_MAX_WIDTH = 8000;

    /**
     * Default maximum height (8000 pixels)
     * 默认最大高度（8000 像素）
     */
    public static final int DEFAULT_MAX_HEIGHT = 8000;

    /**
     * Magic number signatures for image formats
     * 图片格式的魔数签名
     */
    private static final Map<String, byte[][]> MAGIC_NUMBERS = Map.of(
        "JPEG", new byte[][]{
            {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        },
        "PNG", new byte[][]{
            {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
        },
        "GIF", new byte[][]{
            {0x47, 0x49, 0x46, 0x38, 0x37, 0x61}, // GIF87a
            {0x47, 0x49, 0x46, 0x38, 0x39, 0x61}  // GIF89a
        },
        "BMP", new byte[][]{
            {0x42, 0x4D}
        }
    );

    /**
     * WebP magic: RIFF....WEBP (bytes 0-3 = "RIFF", bytes 8-11 = "WEBP")
     * WebP 魔数：RIFF....WEBP（字节 0-3 = "RIFF"，字节 8-11 = "WEBP"）
     */
    private static final byte[] WEBP_RIFF = {0x52, 0x49, 0x46, 0x46}; // "RIFF"
    private static final byte[] WEBP_WEBP = {0x57, 0x45, 0x42, 0x50}; // "WEBP"

    private ImageValidator() {
        // Utility class
    }

    /**
     * Validate image file
     * 验证图片文件
     *
     * @param path the file path | 文件路径
     * @throws ImageValidationException if validation fails | 如果验证失败
     */
    public static void validate(Path path) throws ImageValidationException {
        validate(path, DEFAULT_MAX_FILE_SIZE, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT);
    }

    /**
     * Validate image file with limits
     * 验证图片文件（带限制）
     *
     * @param path the file path | 文件路径
     * @param maxFileSize maximum file size in bytes | 最大文件大小（字节）
     * @param maxWidth maximum width in pixels | 最大宽度（像素）
     * @param maxHeight maximum height in pixels | 最大高度（像素）
     * @throws ImageValidationException if validation fails | 如果验证失败
     */
    public static void validate(Path path, long maxFileSize, int maxWidth, int maxHeight)
            throws ImageValidationException {
        // Check file exists
        if (!Files.exists(path)) {
            throw new ImageValidationException("File not found");
        }

        // Check file is readable
        if (!Files.isReadable(path)) {
            throw new ImageValidationException("File is not readable");
        }

        try {
            // Check file size
            long size = Files.size(path);
            if (size > maxFileSize) {
                throw new ImageTooLargeException(size, maxFileSize);
            }

            // Check magic number
            byte[] header = new byte[8];
            try (InputStream is = Files.newInputStream(path)) {
                int read = is.read(header);
                if (read < 2) {
                    throw new ImageValidationException("File too small to be a valid image");
                }
            }

            if (!checkMagicNumber(header)) {
                throw new ImageValidationException("Invalid image format (magic number mismatch)");
            }

            // Check dimensions
            int[] dimensions = getDimensions(path);
            if (dimensions[0] > maxWidth || dimensions[1] > maxHeight) {
                throw new ImageTooLargeException(dimensions[0], dimensions[1], maxWidth, maxHeight);
            }

        } catch (IOException e) {
            throw new ImageValidationException("Failed to validate image: " + e.getMessage(), e);
        }
    }

    /**
     * Validate image bytes
     * 验证图片字节数组
     *
     * @param bytes the image bytes | 图片字节数组
     * @throws ImageValidationException if validation fails | 如果验证失败
     */
    public static void validate(byte[] bytes) throws ImageValidationException {
        validate(bytes, DEFAULT_MAX_FILE_SIZE, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT);
    }

    /**
     * Validate image bytes with limits
     * 验证图片字节数组（带限制）
     *
     * @param bytes the image bytes | 图片字节数组
     * @param maxFileSize maximum file size in bytes | 最大文件大小（字节）
     * @param maxWidth maximum width in pixels | 最大宽度（像素）
     * @param maxHeight maximum height in pixels | 最大高度（像素）
     * @throws ImageValidationException if validation fails | 如果验证失败
     */
    public static void validate(byte[] bytes, long maxFileSize, int maxWidth, int maxHeight)
            throws ImageValidationException {
        if (bytes == null || bytes.length == 0) {
            throw new ImageValidationException("Image bytes cannot be null or empty");
        }

        // Check size
        if (bytes.length > maxFileSize) {
            throw new ImageTooLargeException(bytes.length, maxFileSize);
        }

        // Check magic number
        if (!checkMagicNumber(bytes)) {
            throw new ImageValidationException("Invalid image format (magic number mismatch)");
        }

        // Check dimensions
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ImageInputStream iis = ImageIO.createImageInputStream(bais)) {

            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                throw new ImageValidationException("No image reader found");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(iis);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width > maxWidth || height > maxHeight) {
                    throw new ImageTooLargeException(width, height, maxWidth, maxHeight);
                }
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new ImageValidationException("Failed to validate image: " + e.getMessage(), e);
        }
    }

    /**
     * Check if file is a valid image
     * 检查文件是否为有效图片
     *
     * @param path the file path | 文件路径
     * @return true if valid image | 如果是有效图片返回true
     */
    public static boolean isValidImage(Path path) {
        try {
            validate(path);
            return true;
        } catch (ImageValidationException e) {
            return false;
        }
    }

    /**
     * Check if bytes are a valid image
     * 检查字节数组是否为有效图片
     *
     * @param bytes the image bytes | 图片字节数组
     * @return true if valid image | 如果是有效图片返回true
     */
    public static boolean isValidImage(byte[] bytes) {
        try {
            validate(bytes);
            return true;
        } catch (ImageValidationException e) {
            return false;
        }
    }

    /**
     * Check magic number
     * 检查魔数
     *
     * @param bytes the image bytes | 图片字节数组
     * @return true if magic number matches | 如果魔数匹配返回true
     */
    public static boolean checkMagicNumber(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            return false;
        }

        // Check standard formats
        for (byte[][] signatures : MAGIC_NUMBERS.values()) {
            for (byte[] signature : signatures) {
                if (bytes.length >= signature.length) {
                    byte[] header = Arrays.copyOf(bytes, signature.length);
                    if (Arrays.equals(header, signature)) {
                        return true;
                    }
                }
            }
        }

        // Check WebP (RIFF....WEBP)
        if (isWebPFormat(bytes)) {
            return true;
        }

        return false;
    }

    /**
     * Check if bytes represent WebP format
     * 检查字节是否为 WebP 格式
     *
     * @param bytes the image bytes | 图片字节数组
     * @return true if WebP format | 如果是 WebP 格式返回 true
     */
    private static boolean isWebPFormat(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return false;
        }
        // Check "RIFF" at offset 0
        boolean hasRiff = bytes[0] == WEBP_RIFF[0] && bytes[1] == WEBP_RIFF[1]
                       && bytes[2] == WEBP_RIFF[2] && bytes[3] == WEBP_RIFF[3];
        // Check "WEBP" at offset 8
        boolean hasWebp = bytes[8] == WEBP_WEBP[0] && bytes[9] == WEBP_WEBP[1]
                       && bytes[10] == WEBP_WEBP[2] && bytes[11] == WEBP_WEBP[3];
        return hasRiff && hasWebp;
    }

    /**
     * Detect format from magic number
     * 从魔数检测格式
     *
     * @param bytes the image bytes | 图片字节数组
     * @return the detected format or null | 检测到的格式或null
     * @deprecated use {@link #detectFormatOptional(byte[])} for null-safe detection
     */
    @Deprecated(since = "1.0.1")
    public static ImageFormat detectFormat(byte[] bytes) {
        return detectFormatOptional(bytes).orElse(null);
    }

    /**
     * Detect format from magic number (null-safe)
     * 从魔数检测格式（空值安全）
     *
     * @param bytes the image bytes | 图片字节数组
     * @return an Optional containing the detected format, or empty if unrecognized
     *         包含检测到格式的 Optional，如果无法识别则为空
     * @since V1.0.1
     */
    public static Optional<ImageFormat> detectFormatOptional(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            return Optional.empty();
        }

        // Check standard formats
        for (Map.Entry<String, byte[][]> entry : MAGIC_NUMBERS.entrySet()) {
            for (byte[] signature : entry.getValue()) {
                if (bytes.length >= signature.length) {
                    byte[] header = Arrays.copyOf(bytes, signature.length);
                    if (Arrays.equals(header, signature)) {
                        Optional<ImageFormat> format = switch (entry.getKey()) {
                            case "JPEG" -> Optional.of(ImageFormat.JPEG);
                            case "PNG" -> Optional.of(ImageFormat.PNG);
                            case "GIF" -> Optional.of(ImageFormat.GIF);
                            case "BMP" -> Optional.of(ImageFormat.BMP);
                            default -> Optional.empty();
                        };
                        if (format.isPresent()) {
                            return format;
                        }
                    }
                }
            }
        }

        // Check WebP
        if (isWebPFormat(bytes)) {
            return Optional.of(ImageFormat.WEBP);
        }

        return Optional.empty();
    }

    /**
     * Get image dimensions without fully loading
     * 获取图片尺寸（不完全加载）
     *
     * @param path the file path | 文件路径
     * @return array of [width, height] | [宽度, 高度]数组
     * @throws IOException if reading fails | 如果读取失败
     */
    private static int[] getDimensions(Path path) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(path.toFile())) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                throw new IOException("No reader found for image");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(iis);
                return new int[]{reader.getWidth(0), reader.getHeight(0)};
            } finally {
                reader.dispose();
            }
        }
    }

    /**
     * Validate file extension matches content
     * 验证文件扩展名是否与内容匹配
     *
     * @param path the file path | 文件路径
     * @return true if extension matches content | 如果扩展名与内容匹配返回true
     */
    public static boolean validateExtensionMatchesContent(Path path) {
        try {
            byte[] header = new byte[8];
            try (InputStream is = Files.newInputStream(path)) {
                int read = is.read(header);
                if (read < 2) {
                    return false;
                }
            }

            ImageFormat detectedFormat = detectFormat(header);
            if (detectedFormat == null) {
                return false;
            }

            String fileName = path.getFileName().toString();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex <= 0) {
                return false;
            }

            String extension = fileName.substring(dotIndex + 1).toLowerCase();
            return detectedFormat.getExtension().equalsIgnoreCase(extension) ||
                   (detectedFormat == ImageFormat.JPEG && extension.equals("jpeg"));

        } catch (IOException e) {
            return false;
        }
    }
}
