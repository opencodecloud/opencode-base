package cloud.opencode.base.image.morphology;

import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Morphological Image Operations
 * 形态学图像操作工具类
 *
 * <p>Provides standard morphological operations on grayscale images: erosion, dilation,
 * opening, closing, gradient, top-hat, and black-hat transforms.</p>
 * <p>提供灰度图像上的标准形态学操作：腐蚀、膨胀、开运算、闭运算、梯度、顶帽和黑帽变换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Erosion — minimum filter over structuring element - 腐蚀 — 结构元素上的最小值滤波</li>
 *   <li>Dilation — maximum filter over structuring element - 膨胀 — 结构元素上的最大值滤波</li>
 *   <li>Opening — erosion followed by dilation - 开运算 — 先腐蚀后膨胀</li>
 *   <li>Closing — dilation followed by erosion - 闭运算 — 先膨胀后腐蚀</li>
 *   <li>Gradient — dilation minus erosion - 梯度 — 膨胀减去腐蚀</li>
 *   <li>Top-hat — original minus opening - 顶帽 — 原图减去开运算</li>
 *   <li>Black-hat — closing minus original - 黑帽 — 闭运算减去原图</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * StructuringElement se = StructuringElement.rect(3, 3);
 * BufferedImage eroded = MorphologyOp.erode(image, se);
 * BufferedImage dilated = MorphologyOp.dilate(image, se);
 * BufferedImage opened = MorphologyOp.open(image, se);
 * BufferedImage edges = MorphologyOp.gradient(image, se);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(w * h * kw * kh) per operation - 时间复杂度: 每次操作 O(w * h * kw * kh)</li>
 *   <li>Space complexity: O(w * h) for output image - 空间复杂度: O(w * h) 输出图像</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
public final class MorphologyOp {

    private MorphologyOp() {
        throw new AssertionError("No MorphologyOp instances");
    }

    /**
     * Erode an image using the given structuring element.
     * 使用给定的结构元素对图像进行腐蚀。
     *
     * <p>For each pixel, computes the minimum grayscale value over the structuring element mask.
     * The image is first converted to grayscale via {@link ChannelOp#toGray(int[])}.</p>
     * <p>对于每个像素，计算结构元素掩码范围内的最小灰度值。
     * 图像首先通过 {@link ChannelOp#toGray(int[])} 转换为灰度图像。</p>
     *
     * @param image   the source image | 源图像
     * @param element the structuring element | 结构元素
     * @return the eroded grayscale image | 腐蚀后的灰度图像
     * @throws NullPointerException if any argument is null | 当任一参数为 null 时抛出
     */
    public static BufferedImage erode(BufferedImage image, StructuringElement element) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(element, "element must not be null");

        int w = image.getWidth();
        int h = image.getHeight();
        int[] gray = toGray(image);
        int[] result = applyMinMax(gray, w, h, element, true);
        return toBufferedImage(result, w, h);
    }

    /**
     * Dilate an image using the given structuring element.
     * 使用给定的结构元素对图像进行膨胀。
     *
     * <p>For each pixel, computes the maximum grayscale value over the structuring element mask.
     * The image is first converted to grayscale via {@link ChannelOp#toGray(int[])}.</p>
     * <p>对于每个像素，计算结构元素掩码范围内的最大灰度值。
     * 图像首先通过 {@link ChannelOp#toGray(int[])} 转换为灰度图像。</p>
     *
     * @param image   the source image | 源图像
     * @param element the structuring element | 结构元素
     * @return the dilated grayscale image | 膨胀后的灰度图像
     * @throws NullPointerException if any argument is null | 当任一参数为 null 时抛出
     */
    public static BufferedImage dilate(BufferedImage image, StructuringElement element) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(element, "element must not be null");

        int w = image.getWidth();
        int h = image.getHeight();
        int[] gray = toGray(image);
        int[] result = applyMinMax(gray, w, h, element, false);
        return toBufferedImage(result, w, h);
    }

    /**
     * Open an image (erode then dilate).
     * 对图像进行开运算（先腐蚀后膨胀）。
     *
     * <p>Opening removes small bright regions (noise) while preserving larger structures.</p>
     * <p>开运算去除小的亮区域（噪声），同时保留较大结构。</p>
     *
     * @param image   the source image | 源图像
     * @param element the structuring element | 结构元素
     * @return the opened grayscale image | 开运算后的灰度图像
     * @throws NullPointerException if any argument is null | 当任一参数为 null 时抛出
     */
    public static BufferedImage open(BufferedImage image, StructuringElement element) {
        return dilate(erode(image, element), element);
    }

    /**
     * Close an image (dilate then erode).
     * 对图像进行闭运算（先膨胀后腐蚀）。
     *
     * <p>Closing fills small dark regions (gaps) while preserving larger structures.</p>
     * <p>闭运算填充小的暗区域（空隙），同时保留较大结构。</p>
     *
     * @param image   the source image | 源图像
     * @param element the structuring element | 结构元素
     * @return the closed grayscale image | 闭运算后的灰度图像
     * @throws NullPointerException if any argument is null | 当任一参数为 null 时抛出
     */
    public static BufferedImage close(BufferedImage image, StructuringElement element) {
        return erode(dilate(image, element), element);
    }

    /**
     * Compute the morphological gradient (dilate minus erode).
     * 计算形态学梯度（膨胀减去腐蚀）。
     *
     * <p>The gradient highlights edges and boundaries in the image.</p>
     * <p>梯度突出图像中的边缘和边界。</p>
     *
     * @param image   the source image | 源图像
     * @param element the structuring element | 结构元素
     * @return the gradient grayscale image | 梯度灰度图像
     * @throws NullPointerException if any argument is null | 当任一参数为 null 时抛出
     */
    public static BufferedImage gradient(BufferedImage image, StructuringElement element) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(element, "element must not be null");

        int w = image.getWidth();
        int h = image.getHeight();
        int[] gray = toGray(image);
        int[] dilated = applyMinMax(gray, w, h, element, false);
        int[] eroded = applyMinMax(gray, w, h, element, true);

        int[] result = new int[gray.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = PixelOp.clamp(dilated[i] - eroded[i]);
        }
        return toBufferedImage(result, w, h);
    }

    /**
     * Compute the top-hat transform (original minus opening).
     * 计算顶帽变换（原图减去开运算）。
     *
     * <p>Top-hat extracts small bright elements from a dark background.</p>
     * <p>顶帽变换从暗背景中提取小的亮元素。</p>
     *
     * @param image   the source image | 源图像
     * @param element the structuring element | 结构元素
     * @return the top-hat grayscale image | 顶帽变换灰度图像
     * @throws NullPointerException if any argument is null | 当任一参数为 null 时抛出
     */
    public static BufferedImage topHat(BufferedImage image, StructuringElement element) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(element, "element must not be null");

        int w = image.getWidth();
        int h = image.getHeight();
        int[] gray = toGray(image);

        // open = dilate(erode(gray))
        int[] eroded = applyMinMax(gray, w, h, element, true);
        int[] opened = applyMinMax(eroded, w, h, element, false);

        int[] result = new int[gray.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = PixelOp.clamp(gray[i] - opened[i]);
        }
        return toBufferedImage(result, w, h);
    }

    /**
     * Compute the black-hat transform (closing minus original).
     * 计算黑帽变换（闭运算减去原图）。
     *
     * <p>Black-hat extracts small dark elements from a bright background.</p>
     * <p>黑帽变换从亮背景中提取小的暗元素。</p>
     *
     * @param image   the source image | 源图像
     * @param element the structuring element | 结构元素
     * @return the black-hat grayscale image | 黑帽变换灰度图像
     * @throws NullPointerException if any argument is null | 当任一参数为 null 时抛出
     */
    public static BufferedImage blackHat(BufferedImage image, StructuringElement element) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(element, "element must not be null");

        int w = image.getWidth();
        int h = image.getHeight();
        int[] gray = toGray(image);

        // close = erode(dilate(gray))
        int[] dilated = applyMinMax(gray, w, h, element, false);
        int[] closed = applyMinMax(dilated, w, h, element, true);

        int[] result = new int[gray.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = PixelOp.clamp(closed[i] - gray[i]);
        }
        return toBufferedImage(result, w, h);
    }

    // ==================== Internal helpers ====================

    /**
     * Convert image to grayscale int array.
     */
    private static int[] toGray(BufferedImage image) {
        BufferedImage argb = PixelOp.ensureArgb(image);
        return ChannelOp.toGray(PixelOp.getPixels(argb));
    }

    /**
     * Apply min (erosion) or max (dilation) filter using the structuring element.
     *
     * @param gray    grayscale pixel array
     * @param w       image width
     * @param h       image height
     * @param element structuring element
     * @param isMin   true for erosion (min), false for dilation (max)
     * @return filtered grayscale array
     */
    private static int[] applyMinMax(int[] gray, int w, int h,
                                     StructuringElement element, boolean isMin) {
        int kw = element.width();
        int kh = element.height();
        int ax = element.anchorX();
        int ay = element.anchorY();
        int[] result = new int[gray.length];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int val = isMin ? 255 : 0;
                for (int ky = 0; ky < kh; ky++) {
                    for (int kx = 0; kx < kw; kx++) {
                        if (!element.maskAt(kx, ky)) {
                            continue;
                        }
                        int ix = x + kx - ax;
                        int iy = y + ky - ay;
                        // Border handling: clamp to edge
                        ix = Math.clamp(ix, 0, w - 1);
                        iy = Math.clamp(iy, 0, h - 1);
                        int pv = gray[iy * w + ix];
                        val = isMin ? Math.min(val, pv) : Math.max(val, pv);
                    }
                }
                result[y * w + x] = val;
            }
        }
        return result;
    }

    /**
     * Convert grayscale int array to BufferedImage.
     */
    private static BufferedImage toBufferedImage(int[] gray, int w, int h) {
        BufferedImage out = PixelOp.createArgb(w, h);
        int[] dst = PixelOp.getPixels(out);
        int[] argb = ChannelOp.grayToArgb(gray);
        System.arraycopy(argb, 0, dst, 0, argb.length);
        return out;
    }
}
