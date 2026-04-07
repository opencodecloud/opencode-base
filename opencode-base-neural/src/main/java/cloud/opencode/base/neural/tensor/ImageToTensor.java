package cloud.opencode.base.neural.tensor;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.TensorException;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Image to Tensor Bridge Utility
 * 图像到张量桥接工具
 *
 * <p>Converts {@link BufferedImage} instances into neural {@link Tensor} objects
 * with NCHW layout (batch, channels, height, width). Uses only standard
 * {@code java.awt} APIs — no dependency on the opencode-base-image module.</p>
 * <p>将 {@link BufferedImage} 实例转换为 NCHW 布局（批次、通道、高度、宽度）的
 * 神经 {@link Tensor} 对象。仅使用标准 {@code java.awt} API，不依赖 opencode-base-image 模块。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Convert any BufferedImage to [1, 3, H, W] tensor normalized to [0, 1] -
 *       将任意 BufferedImage 转换为归一化到 [0, 1] 的 [1, 3, H, W] 张量</li>
 *   <li>Custom per-channel mean/std normalization (e.g. ImageNet) -
 *       自定义逐通道均值/标准差归一化（如 ImageNet）</li>
 *   <li>Optional BGR channel ordering - 可选 BGR 通道顺序</li>
 *   <li>Optional resize before conversion - 转换前可选缩放</li>
 * </ul>
 *
 * <p><strong>Algorithm | 算法:</strong></p>
 * <ol>
 *   <li>If target size specified, resize image using bicubic interpolation -
 *       如果指定了目标大小，使用双三次插值缩放图像</li>
 *   <li>Extract ARGB pixels via {@code getRGB()} - 通过 {@code getRGB()} 提取 ARGB 像素</li>
 *   <li>Decompose each pixel: R=(argb&gt;&gt;16)&amp;0xFF, G=(argb&gt;&gt;8)&amp;0xFF, B=argb&amp;0xFF -
 *       分解每个像素</li>
 *   <li>Build float[] in NCHW layout: data[c*H*W + y*W + x] -
 *       按 NCHW 布局构建 float 数组</li>
 *   <li>Normalize: value = pixel / 255.0f - 归一化</li>
 *   <li>If mean/std provided: value = (value - mean[c]) / std[c] - 如果提供均值/标准差则进一步归一化</li>
 *   <li>If BGR: swap R and B channels - 如果 BGR 模式则交换 R 和 B 通道</li>
 * </ol>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Tensor
 * @see Shape
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class ImageToTensor {

    private static final int CHANNELS = 3;
    private static final float PIXEL_SCALE = 255.0f;

    private ImageToTensor() {
        throw new AssertionError("No ImageToTensor instances");
    }

    /**
     * Convert a BufferedImage to a Tensor with shape [1, 3, H, W] normalized to [0, 1]
     * 将 BufferedImage 转换为形状为 [1, 3, H, W] 的归一化到 [0, 1] 的张量
     *
     * @param image the source image (must not be null) | 源图像（不能为 null）
     * @return tensor with shape [1, 3, H, W] and values in [0, 1] |
     *         形状为 [1, 3, H, W] 且值在 [0, 1] 范围内的张量
     * @throws TensorException if image is null | 如果图像为 null
     */
    public static Tensor convert(BufferedImage image) {
        return convert(image, ConvertOptions.defaults());
    }

    /**
     * Convert a BufferedImage to a Tensor with custom per-channel normalization
     * 使用自定义逐通道归一化将 BufferedImage 转换为张量
     *
     * <p>Applies: (pixel/255 - mean[c]) / std[c] per channel.</p>
     * <p>逐通道应用: (pixel/255 - mean[c]) / std[c]。</p>
     *
     * @param image the source image (must not be null) | 源图像（不能为 null）
     * @param mean  per-channel mean values [R, G, B] (nullable, if null no mean subtraction) |
     *              逐通道均值 [R, G, B]（可为 null，null 时不减均值）
     * @param std   per-channel std values [R, G, B] (nullable, if null no std division) |
     *              逐通道标准差 [R, G, B]（可为 null，null 时不除标准差）
     * @return normalized tensor with shape [1, 3, H, W] | 归一化后形状为 [1, 3, H, W] 的张量
     * @throws TensorException if image is null or mean/std arrays have wrong length |
     *                         如果图像为 null 或 mean/std 数组长度不正确
     */
    public static Tensor convert(BufferedImage image, float[] mean, float[] std) {
        return convert(image, ConvertOptions.builder()
                .mean(mean)
                .std(std)
                .build());
    }

    /**
     * Convert a BufferedImage to a Tensor with full conversion options
     * 使用完整转换选项将 BufferedImage 转换为张量
     *
     * @param image   the source image (must not be null) | 源图像（不能为 null）
     * @param options conversion options (must not be null) | 转换选项（不能为 null）
     * @return tensor with shape [1, 3, H, W] | 形状为 [1, 3, H, W] 的张量
     * @throws TensorException if image or options is null, or options are invalid |
     *                         如果图像或选项为 null，或选项无效
     */
    public static Tensor convert(BufferedImage image, ConvertOptions options) {
        if (image == null) {
            throw new TensorException("Image must not be null", NeuralErrorCode.INVALID_PARAMETERS);
        }
        if (options == null) {
            throw new TensorException("ConvertOptions must not be null", NeuralErrorCode.INVALID_PARAMETERS);
        }

        // Validate mean/std arrays
        if (options.mean() != null && options.mean().length != CHANNELS) {
            throw new TensorException(
                    "Mean array must have " + CHANNELS + " elements, got " + options.mean().length,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        if (options.std() != null && options.std().length != CHANNELS) {
            throw new TensorException(
                    "Std array must have " + CHANNELS + " elements, got " + options.std().length,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        if (options.std() != null) {
            for (int c = 0; c < CHANNELS; c++) {
                if (options.std()[c] == 0.0f) {
                    throw new TensorException(
                            "Std values must not be zero (channel " + c + ")",
                            NeuralErrorCode.INVALID_PARAMETERS);
                }
            }
        }

        // Resize if needed
        BufferedImage src = image;
        int targetW = options.targetWidth();
        int targetH = options.targetHeight();
        if (targetW > 0 && targetH > 0
                && (targetW != image.getWidth() || targetH != image.getHeight())) {
            src = resize(image, targetW, targetH);
        }

        int h = src.getHeight();
        int w = src.getWidth();

        // Extract ARGB pixels
        int[] argbPixels = src.getRGB(0, 0, w, h, null, 0, w);

        // Build NCHW float array: data[c * H * W + y * W + x]
        long totalSize = (long) CHANNELS * h * w;
        if (totalSize > Integer.MAX_VALUE) {
            throw new TensorException("Image too large: " + w + "x" + h, NeuralErrorCode.INVALID_PARAMETERS);
        }
        float[] data = new float[(int) totalSize];

        int planeSize = h * w;
        float[] mean = options.mean();
        float[] std = options.std();
        boolean bgr = options.bgr();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = argbPixels[y * w + x];
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                float rf = r / PIXEL_SCALE;
                float gf = g / PIXEL_SCALE;
                float bf = b / PIXEL_SCALE;

                // Apply mean/std normalization
                if (mean != null) {
                    rf -= mean[0];
                    gf -= mean[1];
                    bf -= mean[2];
                }
                if (std != null) {
                    rf /= std[0];
                    gf /= std[1];
                    bf /= std[2];
                }

                int pixelIdx = y * w + x;
                if (bgr) {
                    // Channel 0 = B, Channel 1 = G, Channel 2 = R
                    data[pixelIdx] = bf;
                    data[planeSize + pixelIdx] = gf;
                    data[2 * planeSize + pixelIdx] = rf;
                } else {
                    // Channel 0 = R, Channel 1 = G, Channel 2 = B
                    data[pixelIdx] = rf;
                    data[planeSize + pixelIdx] = gf;
                    data[2 * planeSize + pixelIdx] = bf;
                }
            }
        }

        return Tensor.fromFloat(data, Shape.of(1, CHANNELS, h, w));
    }

    /**
     * Resize a BufferedImage using bicubic interpolation
     * 使用双三次插值缩放 BufferedImage
     */
    private static BufferedImage resize(BufferedImage src, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2d.drawImage(src, 0, 0, targetWidth, targetHeight, null);
        } finally {
            g2d.dispose();
        }
        return resized;
    }

    /**
     * Options for image-to-tensor conversion
     * 图像到张量转换的选项
     *
     * <p>Configures channel ordering, normalization, and optional resize.</p>
     * <p>配置通道顺序、归一化和可选缩放。</p>
     *
     * @param bgr          false=RGB (default), true=BGR channel ordering |
     *                     false=RGB（默认），true=BGR 通道顺序
     * @param mean         per-channel mean for normalization, nullable |
     *                     逐通道归一化均值，可为 null
     * @param std          per-channel standard deviation for normalization, nullable |
     *                     逐通道归一化标准差，可为 null
     * @param targetWidth  resize target width, 0=keep original |
     *                     缩放目标宽度，0=保持原始
     * @param targetHeight resize target height, 0=keep original |
     *                     缩放目标高度，0=保持原始
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-neural V1.0.0
     */
    public record ConvertOptions(
            boolean bgr,
            float[] mean,
            float[] std,
            int targetWidth,
            int targetHeight
    ) {

        /**
         * Compact constructor — defensive copy of mutable arrays
         * 紧凑构造函数 — 防御性拷贝可变数组
         */
        public ConvertOptions {
            mean = mean != null ? mean.clone() : null;
            std = std != null ? std.clone() : null;
        }

        /**
         * Create default options (RGB, no normalization, no resize)
         * 创建默认选项（RGB，无归一化，无缩放）
         *
         * @return default options | 默认选项
         */
        public static ConvertOptions defaults() {
            return new ConvertOptions(false, null, null, 0, 0);
        }

        /**
         * Create a new builder
         * 创建新的构建器
         *
         * @return new builder | 新构建器
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Builder for ConvertOptions
         * ConvertOptions 构建器
         *
         * @author Leon Soo
         * <a href="https://leonsoo.com">www.LeonSoo.com</a>
         * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
         * @since JDK 25, opencode-base-neural V1.0.0
         */
        public static final class Builder {
            private boolean bgr;
            private float[] mean;
            private float[] std;
            private int targetWidth;
            private int targetHeight;

            private Builder() {
            }

            /**
             * Set BGR channel ordering
             * 设置 BGR 通道顺序
             *
             * @param bgr true for BGR, false for RGB | true 为 BGR，false 为 RGB
             * @return this builder | 此构建器
             */
            public Builder bgr(boolean bgr) {
                this.bgr = bgr;
                return this;
            }

            /**
             * Set per-channel mean values for normalization
             * 设置逐通道归一化均值
             *
             * @param mean per-channel mean [R, G, B] or null | 逐通道均值 [R, G, B] 或 null
             * @return this builder | 此构建器
             */
            public Builder mean(float[] mean) {
                this.mean = mean != null ? mean.clone() : null;
                return this;
            }

            /**
             * Set per-channel standard deviation values for normalization
             * 设置逐通道归一化标准差
             *
             * @param std per-channel std [R, G, B] or null | 逐通道标准差 [R, G, B] 或 null
             * @return this builder | 此构建器
             */
            public Builder std(float[] std) {
                this.std = std != null ? std.clone() : null;
                return this;
            }

            /**
             * Set target width for resize (0 = keep original)
             * 设置缩放目标宽度（0 = 保持原始）
             *
             * @param targetWidth target width | 目标宽度
             * @return this builder | 此构建器
             */
            public Builder targetWidth(int targetWidth) {
                this.targetWidth = targetWidth;
                return this;
            }

            /**
             * Set target height for resize (0 = keep original)
             * 设置缩放目标高度（0 = 保持原始）
             *
             * @param targetHeight target height | 目标高度
             * @return this builder | 此构建器
             */
            public Builder targetHeight(int targetHeight) {
                this.targetHeight = targetHeight;
                return this;
            }

            /**
             * Build the ConvertOptions
             * 构建 ConvertOptions
             *
             * @return the built ConvertOptions | 构建的 ConvertOptions
             */
            public ConvertOptions build() {
                return new ConvertOptions(bgr, mean, std, targetWidth, targetHeight);
            }
        }
    }
}
