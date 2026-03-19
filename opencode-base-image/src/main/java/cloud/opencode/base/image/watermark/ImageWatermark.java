package cloud.opencode.base.image.watermark;

import cloud.opencode.base.image.Position;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

/**
 * Image Watermark
 * 图片水印
 *
 * <p>Immutable record for image watermark configuration.</p>
 * <p>图片水印配置的不可变记录。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple watermark
 * ImageWatermark watermark = ImageWatermark.of(logoImage);
 *
 * // With position
 * ImageWatermark watermark = ImageWatermark.of(logoImage, Position.BOTTOM_RIGHT);
 *
 * // With builder
 * ImageWatermark watermark = ImageWatermark.builder()
 *     .image(logoImage)
 *     .position(Position.BOTTOM_RIGHT)
 *     .opacity(0.8f)
 *     .margin(20)
 *     .build();
 *
 * // With all options
 * ImageWatermark watermark = new ImageWatermark(logoImage, Position.BOTTOM_RIGHT, 0.5f, 20);
 * }</pre>
 *
 * @param image the watermark image | 水印图片
 * @param position the position | 位置
 * @param opacity the opacity (0.0 to 1.0) | 透明度
 * @param margin the margin from edge | 边缘间距
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable image watermark configuration - 不可变的图片水印配置</li>
 *   <li>Builder pattern and factory methods - 构建器模式和工厂方法</li>
 *   <li>Configurable position, opacity, and margin - 可配置的位置、透明度和边距</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (image must not be null) - 空值安全: 否（图片不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
public record ImageWatermark(
    BufferedImage image,
    Position position,
    float opacity,
    int margin
) implements Watermark {

    /**
     * Default opacity | 默认透明度
     */
    public static final float DEFAULT_OPACITY = 1.0f;

    /**
     * Default margin | 默认边距
     */
    public static final int DEFAULT_MARGIN = 10;

    /**
     * Compact constructor with validation
     * 紧凑构造函数（带验证）
     */
    public ImageWatermark {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        if (position == null) {
            position = Position.BOTTOM_RIGHT;
        }
        if (opacity < 0 || opacity > 1) {
            throw new IllegalArgumentException("Opacity must be between 0.0 and 1.0");
        }
        if (margin < 0) {
            throw new IllegalArgumentException("Margin cannot be negative");
        }
    }

    /**
     * Create simple image watermark
     * 创建简单图片水印
     *
     * @param image the watermark image | 水印图片
     * @return the watermark | 水印
     */
    public static ImageWatermark of(BufferedImage image) {
        return new ImageWatermark(image, Position.BOTTOM_RIGHT, DEFAULT_OPACITY, DEFAULT_MARGIN);
    }

    /**
     * Create image watermark with position
     * 创建带位置的图片水印
     *
     * @param image the watermark image | 水印图片
     * @param position the position | 位置
     * @return the watermark | 水印
     */
    public static ImageWatermark of(BufferedImage image, Position position) {
        return new ImageWatermark(image, position, DEFAULT_OPACITY, DEFAULT_MARGIN);
    }

    /**
     * Create image watermark with position and opacity
     * 创建带位置和透明度的图片水印
     *
     * @param image the watermark image | 水印图片
     * @param position the position | 位置
     * @param opacity the opacity | 透明度
     * @return the watermark | 水印
     */
    public static ImageWatermark of(BufferedImage image, Position position, float opacity) {
        return new ImageWatermark(image, position, opacity, DEFAULT_MARGIN);
    }

    /**
     * Get watermark width
     * 获取水印宽度
     *
     * @return the width | 宽度
     */
    public int getWidth() {
        return image.getWidth();
    }

    /**
     * Get watermark height
     * 获取水印高度
     *
     * @return the height | 高度
     */
    public int getHeight() {
        return image.getHeight();
    }

    /**
     * Create copy with different position
     * 创建使用不同位置的副本
     *
     * @param newPosition the new position | 新位置
     * @return new watermark | 新水印
     */
    public ImageWatermark withPosition(Position newPosition) {
        return new ImageWatermark(image, newPosition, opacity, margin);
    }

    /**
     * Create copy with different opacity
     * 创建使用不同透明度的副本
     *
     * @param newOpacity the new opacity | 新透明度
     * @return new watermark | 新水印
     */
    public ImageWatermark withOpacity(float newOpacity) {
        return new ImageWatermark(image, position, newOpacity, margin);
    }

    /**
     * Create copy with different margin
     * 创建使用不同边距的副本
     *
     * @param newMargin the new margin | 新边距
     * @return new watermark | 新水印
     */
    public ImageWatermark withMargin(int newMargin) {
        return new ImageWatermark(image, position, opacity, newMargin);
    }

    /**
     * Create copy with different image
     * 创建使用不同图片的副本
     *
     * @param newImage the new image | 新图片
     * @return new watermark | 新水印
     */
    public ImageWatermark withImage(BufferedImage newImage) {
        return new ImageWatermark(newImage, position, opacity, margin);
    }

    /**
     * Create a builder
     * 创建构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a builder from existing watermark
     * 从现有水印创建构建器
     *
     * @param watermark the existing watermark | 现有水印
     * @return the builder | 构建器
     */
    public static Builder builder(ImageWatermark watermark) {
        return new Builder()
            .image(watermark.image)
            .position(watermark.position)
            .opacity(watermark.opacity)
            .margin(watermark.margin);
    }

    /**
     * Image Watermark Builder
     * 图片水印构建器
     */
    public static class Builder {
        private BufferedImage image;
        private Position position = Position.BOTTOM_RIGHT;
        private float opacity = DEFAULT_OPACITY;
        private int margin = DEFAULT_MARGIN;

        /**
         * Set image
         * 设置图片
         *
         * @param image the image | 图片
         * @return this builder | 构建器
         */
        public Builder image(BufferedImage image) {
            this.image = image;
            return this;
        }

        /**
         * Set position
         * 设置位置
         *
         * @param position the position | 位置
         * @return this builder | 构建器
         */
        public Builder position(Position position) {
            this.position = position;
            return this;
        }

        /**
         * Set opacity
         * 设置透明度
         *
         * @param opacity the opacity (0.0 to 1.0) | 透明度
         * @return this builder | 构建器
         */
        public Builder opacity(float opacity) {
            this.opacity = opacity;
            return this;
        }

        /**
         * Set margin
         * 设置边距
         *
         * @param margin the margin | 边距
         * @return this builder | 构建器
         */
        public Builder margin(int margin) {
            this.margin = margin;
            return this;
        }

        /**
         * Build the watermark
         * 构建水印
         *
         * @return the watermark | 水印
         */
        public ImageWatermark build() {
            return new ImageWatermark(image, position, opacity, margin);
        }
    }
}
