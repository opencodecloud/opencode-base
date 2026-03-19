package cloud.opencode.base.image.internal;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Compress Operation
 * 压缩操作
 *
 * <p>Internal utility for image compression operations.</p>
 * <p>图片压缩操作的内部工具类。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Image compression with quality control - 带质量控制的图片压缩</li>
 *   <li>Internal utility, not part of public API - 内部工具，非公共 API</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Internal usage
 * BufferedImage compressed = CompressOp.compress(image, 0.8f, "jpg");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: No (throws on null image) - 空值安全: 否（null 图片抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
public final class CompressOp {

    private CompressOp() {
        // Utility class
    }

    /**
     * Compress image with quality
     * 按质量压缩图片
     *
     * @param image the source image | 源图片
     * @param quality quality from 0.0 to 1.0 | 质量（0.0到1.0）
     * @return the compressed image | 压缩后的图片
     */
    public static BufferedImage compress(BufferedImage image, float quality) {
        if (quality < 0 || quality > 1) {
            throw new IllegalArgumentException("Quality must be between 0.0 and 1.0");
        }

        try {
            // Compress via JPEG encoding/decoding
            byte[] compressed = compressToBytes(image, quality, "jpg");
            return ImageIO.read(new ByteArrayInputStream(compressed));
        } catch (IOException e) {
            // If compression fails, return original
            return image;
        }
    }

    /**
     * Compress to byte array with quality
     * 按质量压缩到字节数组
     *
     * @param image the source image | 源图片
     * @param quality quality from 0.0 to 1.0 | 质量（0.0到1.0）
     * @param format the format (jpg, png, etc.) | 格式
     * @return the compressed bytes | 压缩后的字节数组
     * @throws IOException if compression fails | 如果压缩失败
     */
    public static byte[] compressToBytes(BufferedImage image, float quality, String format) throws IOException {
        // Ensure image has no alpha for JPEG
        BufferedImage toCompress = image;
        if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
            toCompress = removeAlpha(image);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        if (!writers.hasNext()) {
            // Fallback to simple write
            ImageIO.write(toCompress, format, baos);
            return baos.toByteArray();
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }

            writer.write(null, new IIOImage(toCompress, null, null), param);
        } finally {
            writer.dispose();
        }

        return baos.toByteArray();
    }

    /**
     * Compress to target file size
     * 压缩到目标文件大小
     *
     * @param image the source image | 源图片
     * @param targetSizeBytes target size in bytes | 目标大小（字节）
     * @return the compressed image | 压缩后的图片
     */
    public static BufferedImage compressToSize(BufferedImage image, long targetSizeBytes) {
        float quality = 0.9f;
        float step = 0.1f;

        BufferedImage result = image;

        try {
            byte[] compressed = compressToBytes(image, quality, "jpg");

            // Binary search for optimal quality
            while (compressed.length > targetSizeBytes && quality > 0.1f) {
                quality -= step;
                compressed = compressToBytes(image, quality, "jpg");
            }

            result = ImageIO.read(new ByteArrayInputStream(compressed));
        } catch (IOException e) {
            // Return original on failure
        }

        return result;
    }

    /**
     * Optimize image (reduce colors, etc.)
     * 优化图片（减少颜色等）
     *
     * @param image the source image | 源图片
     * @return the optimized image | 优化后的图片
     */
    public static BufferedImage optimize(BufferedImage image) {
        // Convert to RGB if necessary
        if (image.getType() != BufferedImage.TYPE_INT_RGB) {
            return removeAlpha(image);
        }
        return image;
    }

    /**
     * Remove alpha channel (convert to RGB)
     * 移除透明通道（转换为RGB）
     *
     * @param image the source image | 源图片
     * @return the image without alpha | 无透明通道的图片
     */
    public static BufferedImage removeAlpha(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        }

        BufferedImage result = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        try {
            // Fill with white background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            // Draw image on top
            g.drawImage(image, 0, 0, null);
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Reduce color depth
     * 减少颜色深度
     *
     * @param image the source image | 源图片
     * @param bits bits per channel (1-8) | 每通道位数（1-8）
     * @return the reduced image | 减少颜色后的图片
     */
    public static BufferedImage reduceColors(BufferedImage image, int bits) {
        if (bits < 1 || bits > 8) {
            throw new IllegalArgumentException("Bits must be between 1 and 8");
        }

        int factor = 256 / (1 << bits);
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = ((rgb >> 16) & 0xFF) / factor * factor;
                int g = ((rgb >> 8) & 0xFF) / factor * factor;
                int b = (rgb & 0xFF) / factor * factor;
                result.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }

        return result;
    }

    /**
     * Estimate compressed size
     * 估算压缩后大小
     *
     * @param image the source image | 源图片
     * @param quality the quality | 质量
     * @return estimated size in bytes | 估算大小（字节）
     */
    public static long estimateCompressedSize(BufferedImage image, float quality) {
        try {
            byte[] compressed = compressToBytes(image, quality, "jpg");
            return compressed.length;
        } catch (IOException e) {
            // Rough estimate: width * height * quality factor
            return (long) (image.getWidth() * image.getHeight() * quality * 0.5);
        }
    }
}
