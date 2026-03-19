package cloud.opencode.base.image.watermark;

import cloud.opencode.base.image.Position;

import java.awt.*;

/**
 * Text Watermark
 * 文字水印
 *
 * <p>Immutable record for text watermark configuration.</p>
 * <p>文字水印配置的不可变记录。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple watermark
 * TextWatermark watermark = TextWatermark.of("Copyright 2024");
 *
 * // With position
 * TextWatermark watermark = TextWatermark.of("Copyright", Position.BOTTOM_RIGHT);
 *
 * // With builder
 * TextWatermark watermark = TextWatermark.builder()
 *     .text("© 2024 Company")
 *     .position(Position.BOTTOM_RIGHT)
 *     .font(new Font("Arial", Font.BOLD, 24))
 *     .color(Color.WHITE)
 *     .opacity(0.8f)
 *     .build();
 * }</pre>
 *
 * @param text the watermark text | 水印文字
 * @param position the position | 位置
 * @param font the font | 字体
 * @param color the text color | 文字颜色
 * @param opacity the opacity (0.0 to 1.0) | 透明度
 * @param margin the margin from edge | 边缘间距
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable text watermark configuration - 不可变的文字水印配置</li>
 *   <li>Builder pattern and factory methods - 构建器模式和工厂方法</li>
 *   <li>Configurable font, color, position, opacity, and margin - 可配置的字体、颜色、位置、透明度和边距</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (text must not be null) - 空值安全: 否（文字不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
public record TextWatermark(
    String text,
    Position position,
    Font font,
    Color color,
    float opacity,
    int margin
) implements Watermark {

    /**
     * Default font | 默认字体
     */
    public static final Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 20);

    /**
     * Default color | 默认颜色
     */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 255, 180);

    /**
     * Default opacity | 默认透明度
     */
    public static final float DEFAULT_OPACITY = 0.8f;

    /**
     * Default margin | 默认边距
     */
    public static final int DEFAULT_MARGIN = 10;

    /**
     * Compact constructor with validation
     * 紧凑构造函数（带验证）
     */
    public TextWatermark {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }
        if (position == null) {
            position = Position.BOTTOM_RIGHT;
        }
        if (font == null) {
            font = DEFAULT_FONT;
        }
        if (color == null) {
            color = DEFAULT_COLOR;
        }
        if (opacity < 0 || opacity > 1) {
            throw new IllegalArgumentException("Opacity must be between 0.0 and 1.0");
        }
        if (margin < 0) {
            throw new IllegalArgumentException("Margin cannot be negative");
        }
    }

    /**
     * Create simple text watermark
     * 创建简单文字水印
     *
     * @param text the watermark text | 水印文字
     * @return the watermark | 水印
     */
    public static TextWatermark of(String text) {
        return new TextWatermark(text, Position.BOTTOM_RIGHT, DEFAULT_FONT, DEFAULT_COLOR, DEFAULT_OPACITY, DEFAULT_MARGIN);
    }

    /**
     * Create text watermark with position
     * 创建带位置的文字水印
     *
     * @param text the watermark text | 水印文字
     * @param position the position | 位置
     * @return the watermark | 水印
     */
    public static TextWatermark of(String text, Position position) {
        return new TextWatermark(text, position, DEFAULT_FONT, DEFAULT_COLOR, DEFAULT_OPACITY, DEFAULT_MARGIN);
    }

    /**
     * Create text watermark with position and font
     * 创建带位置和字体的文字水印
     *
     * @param text the watermark text | 水印文字
     * @param position the position | 位置
     * @param font the font | 字体
     * @return the watermark | 水印
     */
    public static TextWatermark of(String text, Position position, Font font) {
        return new TextWatermark(text, position, font, DEFAULT_COLOR, DEFAULT_OPACITY, DEFAULT_MARGIN);
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
    public static Builder builder(TextWatermark watermark) {
        return new Builder()
            .text(watermark.text)
            .position(watermark.position)
            .font(watermark.font)
            .color(watermark.color)
            .opacity(watermark.opacity)
            .margin(watermark.margin);
    }

    /**
     * Create copy with different text
     * 创建使用不同文字的副本
     *
     * @param newText the new text | 新文字
     * @return new watermark | 新水印
     */
    public TextWatermark withText(String newText) {
        return new TextWatermark(newText, position, font, color, opacity, margin);
    }

    /**
     * Create copy with different position
     * 创建使用不同位置的副本
     *
     * @param newPosition the new position | 新位置
     * @return new watermark | 新水印
     */
    public TextWatermark withPosition(Position newPosition) {
        return new TextWatermark(text, newPosition, font, color, opacity, margin);
    }

    /**
     * Create copy with different opacity
     * 创建使用不同透明度的副本
     *
     * @param newOpacity the new opacity | 新透明度
     * @return new watermark | 新水印
     */
    public TextWatermark withOpacity(float newOpacity) {
        return new TextWatermark(text, position, font, color, newOpacity, margin);
    }

    /**
     * Text Watermark Builder
     * 文字水印构建器
     */
    public static class Builder {
        private String text;
        private Position position = Position.BOTTOM_RIGHT;
        private Font font = DEFAULT_FONT;
        private Color color = DEFAULT_COLOR;
        private float opacity = DEFAULT_OPACITY;
        private int margin = DEFAULT_MARGIN;

        /**
         * Set text
         * 设置文字
         *
         * @param text the text | 文字
         * @return this builder | 构建器
         */
        public Builder text(String text) {
            this.text = text;
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
         * Set font
         * 设置字体
         *
         * @param font the font | 字体
         * @return this builder | 构建器
         */
        public Builder font(Font font) {
            this.font = font;
            return this;
        }

        /**
         * Set font by name and size
         * 按名称和大小设置字体
         *
         * @param name the font name | 字体名称
         * @param size the font size | 字体大小
         * @return this builder | 构建器
         */
        public Builder font(String name, int size) {
            this.font = new Font(name, Font.PLAIN, size);
            return this;
        }

        /**
         * Set font by name, style and size
         * 按名称、样式和大小设置字体
         *
         * @param name the font name | 字体名称
         * @param style the font style | 字体样式
         * @param size the font size | 字体大小
         * @return this builder | 构建器
         */
        public Builder font(String name, int style, int size) {
            this.font = new Font(name, style, size);
            return this;
        }

        /**
         * Set color
         * 设置颜色
         *
         * @param color the color | 颜色
         * @return this builder | 构建器
         */
        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        /**
         * Set color by RGB
         * 按RGB设置颜色
         *
         * @param r red component | 红色分量
         * @param g green component | 绿色分量
         * @param b blue component | 蓝色分量
         * @return this builder | 构建器
         */
        public Builder color(int r, int g, int b) {
            this.color = new Color(r, g, b);
            return this;
        }

        /**
         * Set color by RGBA
         * 按RGBA设置颜色
         *
         * @param r red component | 红色分量
         * @param g green component | 绿色分量
         * @param b blue component | 蓝色分量
         * @param a alpha component | 透明度分量
         * @return this builder | 构建器
         */
        public Builder color(int r, int g, int b, int a) {
            this.color = new Color(r, g, b, a);
            return this;
        }

        /**
         * Set opacity
         * 设置透明度
         *
         * @param opacity the opacity | 透明度
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
        public TextWatermark build() {
            return new TextWatermark(text, position, font, color, opacity, margin);
        }
    }
}
