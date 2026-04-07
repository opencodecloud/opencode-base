package cloud.opencode.base.image.color;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;

import java.util.Objects;

/**
 * Color Space Conversion Utilities
 * 颜色空间转换工具类
 *
 * <p>Provides conversions between RGB and various color spaces including HSV, HSL, CIELAB, and YCbCr.
 * All conversions operate on packed ARGB pixel arrays for maximum interoperability with PixelOp.</p>
 * <p>提供 RGB 与多种颜色空间（HSV、HSL、CIELAB、YCbCr）之间的转换。
 * 所有转换操作在 ARGB 像素数组上进行，与 PixelOp 保持最大互操作性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RGB to/from HSV conversion - RGB 与 HSV 颜色空间互转</li>
 *   <li>RGB to/from HSL conversion - RGB 与 HSL 颜色空间互转</li>
 *   <li>RGB to/from CIELAB conversion (D65 illuminant, sRGB) - RGB 与 CIELAB 颜色空间互转（D65 光源，sRGB）</li>
 *   <li>RGB to/from YCbCr conversion (ITU-R BT.601) - RGB 与 YCbCr 颜色空间互转（ITU-R BT.601）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * int[] pixels = PixelOp.getPixels(image);
 * float[][] hsv = ColorSpaceOp.toHsv(pixels);
 * // Manipulate hsv[0] (H), hsv[1] (S), hsv[2] (V)
 * int[] result = ColorSpaceOp.fromHsv(hsv);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time: O(n) where n = pixel count - 时间: O(n)，n 为像素数量</li>
 *   <li>Space: O(n) for output arrays - 空间: O(n) 用于输出数组</li>
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
public final class ColorSpaceOp {

    // D65 reference white point
    private static final double XN = 0.95047;
    private static final double YN = 1.00000;
    private static final double ZN = 1.08883;

    // LAB threshold constants
    private static final double LAB_EPSILON = 216.0 / 24389.0;  // 0.008856
    private static final double LAB_KAPPA = 24389.0 / 27.0;     // 903.3

    private ColorSpaceOp() {
        throw new AssertionError("No ColorSpaceOp instances");
    }

    // ==================== HSV ====================

    /**
     * Convert ARGB pixels to HSV color space.
     * 将 ARGB 像素转换为 HSV 颜色空间。
     *
     * @param argbPixels the ARGB pixel array | ARGB 像素数组
     * @return float[3][pixelCount] for H(0-360), S(0-1), V(0-1) | float[3][像素数] 分别对应 H(0-360), S(0-1), V(0-1)
     * @throws ImageOperationException if argbPixels is null or empty | 当 argbPixels 为 null 或空时抛出
     */
    public static float[][] toHsv(int[] argbPixels) {
        validatePixels(argbPixels);
        int len = argbPixels.length;
        float[] h = new float[len];
        float[] s = new float[len];
        float[] v = new float[len];

        for (int i = 0; i < len; i++) {
            int px = argbPixels[i];
            float r = ((px >> 16) & 0xFF) / 255.0f;
            float g = ((px >> 8) & 0xFF) / 255.0f;
            float b = (px & 0xFF) / 255.0f;

            float max = Math.max(r, Math.max(g, b));
            float min = Math.min(r, Math.min(g, b));
            float delta = max - min;

            // Value
            v[i] = max;

            // Saturation
            if (max == 0.0f) {
                s[i] = 0.0f;
            } else {
                s[i] = delta / max;
            }

            // Hue
            if (delta == 0.0f) {
                h[i] = 0.0f;
            } else if (max == r) {
                h[i] = 60.0f * (((g - b) / delta) % 6.0f);
            } else if (max == g) {
                h[i] = 60.0f * (((b - r) / delta) + 2.0f);
            } else {
                h[i] = 60.0f * (((r - g) / delta) + 4.0f);
            }
            if (h[i] < 0.0f) {
                h[i] += 360.0f;
            }
        }
        return new float[][]{h, s, v};
    }

    /**
     * Convert HSV color space to ARGB pixels (A=255).
     * 将 HSV 颜色空间转换为 ARGB 像素（A=255）。
     *
     * @param hsv float[3][pixelCount] for H(0-360), S(0-1), V(0-1) | float[3][像素数] 分别对应 H(0-360), S(0-1), V(0-1)
     * @return ARGB pixel array | ARGB 像素数组
     * @throws ImageOperationException if hsv is null or invalid | 当 hsv 为 null 或无效时抛出
     */
    public static int[] fromHsv(float[][] hsv) {
        validateColorArray(hsv, 3, "hsv");
        int len = hsv[0].length;
        int[] result = new int[len];
        float[] hArr = hsv[0];
        float[] sArr = hsv[1];
        float[] vArr = hsv[2];

        for (int i = 0; i < len; i++) {
            float hue = hArr[i] % 360.0f;
            if (hue < 0.0f) {
                hue += 360.0f;
            }
            float sat = Math.clamp(sArr[i], 0.0f, 1.0f);
            float val = Math.clamp(vArr[i], 0.0f, 1.0f);

            float c = val * sat;
            float x = c * (1.0f - Math.abs((hue / 60.0f) % 2.0f - 1.0f));
            float m = val - c;

            float r, g, b;
            if (hue < 60.0f) {
                r = c; g = x; b = 0;
            } else if (hue < 120.0f) {
                r = x; g = c; b = 0;
            } else if (hue < 180.0f) {
                r = 0; g = c; b = x;
            } else if (hue < 240.0f) {
                r = 0; g = x; b = c;
            } else if (hue < 300.0f) {
                r = x; g = 0; b = c;
            } else {
                r = c; g = 0; b = x;
            }

            int ri = PixelOp.clamp(Math.round((r + m) * 255.0f));
            int gi = PixelOp.clamp(Math.round((g + m) * 255.0f));
            int bi = PixelOp.clamp(Math.round((b + m) * 255.0f));
            result[i] = PixelOp.argb(255, ri, gi, bi);
        }
        return result;
    }

    // ==================== HSL ====================

    /**
     * Convert ARGB pixels to HSL color space.
     * 将 ARGB 像素转换为 HSL 颜色空间。
     *
     * @param argbPixels the ARGB pixel array | ARGB 像素数组
     * @return float[3][pixelCount] for H(0-360), S(0-1), L(0-1) | float[3][像素数] 分别对应 H(0-360), S(0-1), L(0-1)
     * @throws ImageOperationException if argbPixels is null or empty | 当 argbPixels 为 null 或空时抛出
     */
    public static float[][] toHsl(int[] argbPixels) {
        validatePixels(argbPixels);
        int len = argbPixels.length;
        float[] h = new float[len];
        float[] s = new float[len];
        float[] l = new float[len];

        for (int i = 0; i < len; i++) {
            int px = argbPixels[i];
            float r = ((px >> 16) & 0xFF) / 255.0f;
            float g = ((px >> 8) & 0xFF) / 255.0f;
            float b = (px & 0xFF) / 255.0f;

            float max = Math.max(r, Math.max(g, b));
            float min = Math.min(r, Math.min(g, b));
            float delta = max - min;

            // Lightness
            l[i] = (max + min) / 2.0f;

            // Saturation
            if (delta == 0.0f) {
                s[i] = 0.0f;
            } else {
                s[i] = delta / (1.0f - Math.abs(2.0f * l[i] - 1.0f));
            }

            // Hue
            if (delta == 0.0f) {
                h[i] = 0.0f;
            } else if (max == r) {
                h[i] = 60.0f * (((g - b) / delta) % 6.0f);
            } else if (max == g) {
                h[i] = 60.0f * (((b - r) / delta) + 2.0f);
            } else {
                h[i] = 60.0f * (((r - g) / delta) + 4.0f);
            }
            if (h[i] < 0.0f) {
                h[i] += 360.0f;
            }
        }
        return new float[][]{h, s, l};
    }

    /**
     * Convert HSL color space to ARGB pixels (A=255).
     * 将 HSL 颜色空间转换为 ARGB 像素（A=255）。
     *
     * @param hsl float[3][pixelCount] for H(0-360), S(0-1), L(0-1) | float[3][像素数] 分别对应 H(0-360), S(0-1), L(0-1)
     * @return ARGB pixel array | ARGB 像素数组
     * @throws ImageOperationException if hsl is null or invalid | 当 hsl 为 null 或无效时抛出
     */
    public static int[] fromHsl(float[][] hsl) {
        validateColorArray(hsl, 3, "hsl");
        int len = hsl[0].length;
        int[] result = new int[len];
        float[] hArr = hsl[0];
        float[] sArr = hsl[1];
        float[] lArr = hsl[2];

        for (int i = 0; i < len; i++) {
            float hue = hArr[i] % 360.0f;
            if (hue < 0.0f) {
                hue += 360.0f;
            }
            float sat = Math.clamp(sArr[i], 0.0f, 1.0f);
            float lit = Math.clamp(lArr[i], 0.0f, 1.0f);

            float c = (1.0f - Math.abs(2.0f * lit - 1.0f)) * sat;
            float x = c * (1.0f - Math.abs((hue / 60.0f) % 2.0f - 1.0f));
            float m = lit - c / 2.0f;

            float r, g, b;
            if (hue < 60.0f) {
                r = c; g = x; b = 0;
            } else if (hue < 120.0f) {
                r = x; g = c; b = 0;
            } else if (hue < 180.0f) {
                r = 0; g = c; b = x;
            } else if (hue < 240.0f) {
                r = 0; g = x; b = c;
            } else if (hue < 300.0f) {
                r = x; g = 0; b = c;
            } else {
                r = c; g = 0; b = x;
            }

            int ri = PixelOp.clamp(Math.round((r + m) * 255.0f));
            int gi = PixelOp.clamp(Math.round((g + m) * 255.0f));
            int bi = PixelOp.clamp(Math.round((b + m) * 255.0f));
            result[i] = PixelOp.argb(255, ri, gi, bi);
        }
        return result;
    }

    // ==================== CIELAB ====================

    /**
     * Convert ARGB pixels to CIELAB color space using D65 illuminant and sRGB.
     * 使用 D65 光源和 sRGB 将 ARGB 像素转换为 CIELAB 颜色空间。
     *
     * @param argbPixels the ARGB pixel array | ARGB 像素数组
     * @return float[3][pixelCount] for L(0-100), a(-128 to 127), b(-128 to 127) | float[3][像素数]
     * @throws ImageOperationException if argbPixels is null or empty | 当 argbPixels 为 null 或空时抛出
     */
    public static float[][] toLab(int[] argbPixels) {
        validatePixels(argbPixels);
        int len = argbPixels.length;
        float[] lArr = new float[len];
        float[] aArr = new float[len];
        float[] bArr = new float[len];

        for (int i = 0; i < len; i++) {
            int px = argbPixels[i];
            double r = srgbToLinear(((px >> 16) & 0xFF) / 255.0);
            double g = srgbToLinear(((px >> 8) & 0xFF) / 255.0);
            double b = srgbToLinear((px & 0xFF) / 255.0);

            // Linear RGB to XYZ (sRGB D65 matrix)
            double x = 0.4124564 * r + 0.3575761 * g + 0.1804375 * b;
            double y = 0.2126729 * r + 0.7151522 * g + 0.0721750 * b;
            double z = 0.0193339 * r + 0.1191920 * g + 0.9503041 * b;

            // XYZ to LAB
            double fx = labF(x / XN);
            double fy = labF(y / YN);
            double fz = labF(z / ZN);

            lArr[i] = (float) (116.0 * fy - 16.0);
            aArr[i] = (float) (500.0 * (fx - fy));
            bArr[i] = (float) (200.0 * (fy - fz));
        }
        return new float[][]{lArr, aArr, bArr};
    }

    /**
     * Convert CIELAB color space to ARGB pixels (A=255).
     * 将 CIELAB 颜色空间转换为 ARGB 像素（A=255）。
     *
     * @param lab float[3][pixelCount] for L, a, b | float[3][像素数] 分别对应 L, a, b
     * @return ARGB pixel array | ARGB 像素数组
     * @throws ImageOperationException if lab is null or invalid | 当 lab 为 null 或无效时抛出
     */
    public static int[] fromLab(float[][] lab) {
        validateColorArray(lab, 3, "lab");
        int len = lab[0].length;
        int[] result = new int[len];
        float[] lArr = lab[0];
        float[] aArr = lab[1];
        float[] bArr = lab[2];

        for (int i = 0; i < len; i++) {
            double l = lArr[i];
            double a = aArr[i];
            double b = bArr[i];

            // LAB to XYZ
            double fy = (l + 16.0) / 116.0;
            double fx = a / 500.0 + fy;
            double fz = fy - b / 200.0;

            double x = XN * labFInv(fx);
            double y = YN * labFInv(fy);
            double z = ZN * labFInv(fz);

            // XYZ to linear RGB (inverse sRGB D65 matrix)
            double lr = 3.2404542 * x - 1.5371385 * y - 0.4985314 * z;
            double lg = -0.9692660 * x + 1.8760108 * y + 0.0415560 * z;
            double lb = 0.0556434 * x - 0.2040259 * y + 1.0572252 * z;

            int ri = PixelOp.clamp((int) Math.round(linearToSrgb(lr) * 255.0));
            int gi = PixelOp.clamp((int) Math.round(linearToSrgb(lg) * 255.0));
            int bi = PixelOp.clamp((int) Math.round(linearToSrgb(lb) * 255.0));
            result[i] = PixelOp.argb(255, ri, gi, bi);
        }
        return result;
    }

    // ==================== YCbCr ====================

    /**
     * Convert ARGB pixels to YCbCr color space (ITU-R BT.601).
     * 使用 ITU-R BT.601 将 ARGB 像素转换为 YCbCr 颜色空间。
     *
     * @param argbPixels the ARGB pixel array | ARGB 像素数组
     * @return float[3][pixelCount] for Y, Cb, Cr | float[3][像素数] 分别对应 Y, Cb, Cr
     * @throws ImageOperationException if argbPixels is null or empty | 当 argbPixels 为 null 或空时抛出
     */
    public static float[][] toYCbCr(int[] argbPixels) {
        validatePixels(argbPixels);
        int len = argbPixels.length;
        float[] yArr = new float[len];
        float[] cbArr = new float[len];
        float[] crArr = new float[len];

        for (int i = 0; i < len; i++) {
            int px = argbPixels[i];
            float r = (px >> 16) & 0xFF;
            float g = (px >> 8) & 0xFF;
            float b = px & 0xFF;

            yArr[i] = 0.299f * r + 0.587f * g + 0.114f * b;
            cbArr[i] = 128.0f - 0.169f * r - 0.331f * g + 0.500f * b;
            crArr[i] = 128.0f + 0.500f * r - 0.419f * g - 0.081f * b;
        }
        return new float[][]{yArr, cbArr, crArr};
    }

    /**
     * Convert YCbCr color space to ARGB pixels (A=255, ITU-R BT.601).
     * 使用 ITU-R BT.601 将 YCbCr 颜色空间转换为 ARGB 像素（A=255）。
     *
     * @param ycbcr float[3][pixelCount] for Y, Cb, Cr | float[3][像素数] 分别对应 Y, Cb, Cr
     * @return ARGB pixel array | ARGB 像素数组
     * @throws ImageOperationException if ycbcr is null or invalid | 当 ycbcr 为 null 或无效时抛出
     */
    public static int[] fromYCbCr(float[][] ycbcr) {
        validateColorArray(ycbcr, 3, "ycbcr");
        int len = ycbcr[0].length;
        int[] result = new int[len];
        float[] yArr = ycbcr[0];
        float[] cbArr = ycbcr[1];
        float[] crArr = ycbcr[2];

        for (int i = 0; i < len; i++) {
            float y = yArr[i];
            float cb = cbArr[i] - 128.0f;
            float cr = crArr[i] - 128.0f;

            int r = PixelOp.clamp(Math.round(y + 1.402f * cr));
            int g = PixelOp.clamp(Math.round(y - 0.344f * cb - 0.714f * cr));
            int b = PixelOp.clamp(Math.round(y + 1.772f * cb));
            result[i] = PixelOp.argb(255, r, g, b);
        }
        return result;
    }

    // ==================== Internal helpers ====================

    /**
     * sRGB gamma decode: convert sRGB component [0,1] to linear [0,1].
     */
    private static double srgbToLinear(double c) {
        return (c <= 0.04045) ? (c / 12.92) : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    /**
     * sRGB gamma encode: convert linear [0,1] to sRGB [0,1].
     */
    private static double linearToSrgb(double c) {
        if (c <= 0.0) {
            return 0.0;
        }
        return (c <= 0.0031308) ? (12.92 * c) : (1.055 * Math.pow(c, 1.0 / 2.4) - 0.055);
    }

    /**
     * LAB f function.
     */
    private static double labF(double t) {
        return (t > LAB_EPSILON) ? Math.cbrt(t) : (LAB_KAPPA * t + 16.0) / 116.0;
    }

    /**
     * LAB inverse f function.
     */
    private static double labFInv(double t) {
        double t3 = t * t * t;
        return (t3 > LAB_EPSILON) ? t3 : (116.0 * t - 16.0) / LAB_KAPPA;
    }

    private static void validatePixels(int[] argbPixels) {
        Objects.requireNonNull(argbPixels, "argbPixels must not be null");
        if (argbPixels.length == 0) {
            throw new ImageOperationException("argbPixels must not be empty",
                    ImageErrorCode.INVALID_PARAMETERS);
        }
    }

    private static void validateColorArray(float[][] arr, int channels, String name) {
        Objects.requireNonNull(arr, name + " must not be null");
        if (arr.length != channels) {
            throw new ImageOperationException(
                    name + " must have " + channels + " channels, got: " + arr.length,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        for (int c = 0; c < channels; c++) {
            Objects.requireNonNull(arr[c], name + "[" + c + "] must not be null");
        }
        int len = arr[0].length;
        if (len == 0) {
            throw new ImageOperationException(name + " arrays must not be empty",
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        for (int c = 1; c < channels; c++) {
            if (arr[c].length != len) {
                throw new ImageOperationException(
                        name + " channel arrays must have equal length",
                        ImageErrorCode.INVALID_PARAMETERS);
            }
        }
    }
}
