package cloud.opencode.base.image.transform;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Perspective (Homography) Transform Operations for Images
 * 图像透视（单应性）变换操作
 *
 * <p>Applies perspective (projective) transformations to images using a homography
 * matrix computed from 4 point correspondences via the Direct Linear Transform (DLT)
 * algorithm. Unlike affine transforms, perspective transforms can model foreshortening
 * and vanishing-point effects.</p>
 * <p>使用通过直接线性变换（DLT）算法从 4 个点对应关系计算的单应性矩阵，对图像进行透视（射影）变换。
 * 与仿射变换不同，透视变换可以模拟透视缩短和消失点效果。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>4-point perspective transform via DLT homography - 通过 DLT 单应性的 4 点透视变换</li>
 *   <li>Bilinear interpolation for sub-pixel accuracy - 双线性插值实现亚像素精度</li>
 *   <li>Document rectification (trapezoid to rectangle) - 文档矫正（梯形转矩形）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Rectify a trapezoid region to a rectangle
 * double[] src = {50,50, 350,30, 380,280, 20,300}; // trapezoid corners
 * double[] dst = {0,0, 400,0, 400,300, 0,300};     // rectangle corners
 * BufferedImage rectified = PerspectiveTransformOp.apply(image, src, dst, 400, 300);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: No (validates input, throws on null) - 空值安全: 否（验证输入，null 时抛出异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
public final class PerspectiveTransformOp {

    private PerspectiveTransformOp() {
        throw new AssertionError("No PerspectiveTransformOp instances");
    }

    /**
     * Apply a perspective (homography) transform from 4 source points to 4 destination points.
     * 应用从 4 个源点到 4 个目标点的透视（单应性）变换。
     *
     * <p>Points are specified as flat arrays: [x0, y0, x1, y1, x2, y2, x3, y3].
     * The homography is computed using the Direct Linear Transform (DLT) algorithm,
     * then the inverse mapping is applied with bilinear interpolation.</p>
     *
     * @param image        the source image | 源图像
     * @param srcPoints    source points [x0,y0, x1,y1, x2,y2, x3,y3] | 源点坐标
     * @param dstPoints    destination points [x0,y0, x1,y1, x2,y2, x3,y3] | 目标点坐标
     * @param outputWidth  the output image width | 输出图像宽度
     * @param outputHeight the output image height | 输出图像高度
     * @return the transformed image | 变换后的图像
     * @throws ImageOperationException if any input is null, points arrays have wrong length,
     *                                 output dimensions are not positive, or the homography is degenerate
     *                                 当输入为 null、点数组长度不对、输出尺寸非正数或单应性退化时抛出
     */
    public static BufferedImage apply(BufferedImage image,
                                      double[] srcPoints, double[] dstPoints,
                                      int outputWidth, int outputHeight) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(srcPoints, "srcPoints must not be null");
        Objects.requireNonNull(dstPoints, "dstPoints must not be null");
        if (srcPoints.length != 8) {
            throw new ImageOperationException(
                    "srcPoints must have exactly 8 elements (4 points), got: " + srcPoints.length,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (dstPoints.length != 8) {
            throw new ImageOperationException(
                    "dstPoints must have exactly 8 elements (4 points), got: " + dstPoints.length,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (outputWidth <= 0 || outputHeight <= 0) {
            throw new ImageOperationException(
                    "Output dimensions must be positive, got: " + outputWidth + "x" + outputHeight,
                    ImageErrorCode.INVALID_DIMENSIONS);
        }
        validateFinite(srcPoints, "srcPoints");
        validateFinite(dstPoints, "dstPoints");

        // Compute forward homography H: src -> dst using DLT
        // Then we need inverse H to map dst pixels back to src for sampling
        double[] h = solveHomography(srcPoints, dstPoints);
        double[] hInv = solveHomography(dstPoints, srcPoints);

        // Ensure source is ARGB for pixel access
        BufferedImage src = PixelOp.ensureArgb(image);
        int srcW = src.getWidth();
        int srcH = src.getHeight();
        int[] srcPixels = PixelOp.getPixels(src);

        BufferedImage output = PixelOp.createArgb(outputWidth, outputHeight);
        int[] outPixels = PixelOp.getPixels(output);

        // Inverse mapping: for each output pixel, find source coordinate
        for (int oy = 0; oy < outputHeight; oy++) {
            for (int ox = 0; ox < outputWidth; ox++) {
                // Apply inverse homography: (ox, oy) -> (sx, sy)
                double w = hInv[6] * ox + hInv[7] * oy + 1.0;
                if (Math.abs(w) < 1e-10) {
                    // Point at infinity, leave transparent
                    continue;
                }
                double invW = 1.0 / w;
                double sx = (hInv[0] * ox + hInv[1] * oy + hInv[2]) * invW;
                double sy = (hInv[3] * ox + hInv[4] * oy + hInv[5]) * invW;

                // Bilinear interpolation
                outPixels[oy * outputWidth + ox] = bilinearSample(srcPixels, srcW, srcH, sx, sy);
            }
        }

        return output;
    }

    /**
     * Solve homography H from 4 point correspondences using DLT.
     * Returns [h1, h2, h3, h4, h5, h6, h7, h8] where h9=1.
     *
     * For each point pair (x,y) -> (x',y'):
     *   -x*h1 - y*h2 - h3 + x'*x*h7 + x'*y*h8 = -x'
     *   -x*h4 - y*h5 - h6 + y'*x*h7 + y'*y*h8 = -y'
     */
    private static double[] solveHomography(double[] src, double[] dst) {
        // Build 8x8 matrix A and 8x1 vector b: A * h = b
        double[][] a = new double[8][8];
        double[] b = new double[8];

        for (int i = 0; i < 4; i++) {
            double x = src[2 * i];
            double y = src[2 * i + 1];
            double xp = dst[2 * i];
            double yp = dst[2 * i + 1];

            int r1 = 2 * i;
            int r2 = 2 * i + 1;

            // Row for x': -x*h1 - y*h2 - h3 + 0 + 0 + 0 + x'*x*h7 + x'*y*h8 = -x'
            a[r1][0] = -x;
            a[r1][1] = -y;
            a[r1][2] = -1;
            a[r1][3] = 0;
            a[r1][4] = 0;
            a[r1][5] = 0;
            a[r1][6] = xp * x;
            a[r1][7] = xp * y;
            b[r1] = -xp;

            // Row for y': 0 + 0 + 0 - x*h4 - y*h5 - h6 + y'*x*h7 + y'*y*h8 = -y'
            a[r2][0] = 0;
            a[r2][1] = 0;
            a[r2][2] = 0;
            a[r2][3] = -x;
            a[r2][4] = -y;
            a[r2][5] = -1;
            a[r2][6] = yp * x;
            a[r2][7] = yp * y;
            b[r2] = -yp;
        }

        // Solve via Gaussian elimination with partial pivoting
        double[] result = gaussianElimination(a, b);
        return result;
    }

    /**
     * Solve Ax = b using Gaussian elimination with partial pivoting.
     * Returns the 8-element solution vector.
     */
    private static double[] gaussianElimination(double[][] a, double[] b) {
        int n = a.length;
        // Augmented matrix
        double[][] aug = new double[n][n + 1];
        for (int i = 0; i < n; i++) {
            System.arraycopy(a[i], 0, aug[i], 0, n);
            aug[i][n] = b[i];
        }

        // Forward elimination with partial pivoting
        for (int col = 0; col < n; col++) {
            // Find pivot
            int maxRow = col;
            double maxVal = Math.abs(aug[col][col]);
            for (int row = col + 1; row < n; row++) {
                double val = Math.abs(aug[row][col]);
                if (val > maxVal) {
                    maxVal = val;
                    maxRow = row;
                }
            }

            if (maxVal < 1e-12) {
                throw new ImageOperationException(
                        "Homography matrix is singular, point mapping is degenerate",
                        ImageErrorCode.INVALID_PARAMETERS);
            }

            // Swap rows
            if (maxRow != col) {
                double[] tmp = aug[col];
                aug[col] = aug[maxRow];
                aug[maxRow] = tmp;
            }

            // Eliminate below
            double pivot = aug[col][col];
            for (int row = col + 1; row < n; row++) {
                double factor = aug[row][col] / pivot;
                for (int j = col; j <= n; j++) {
                    aug[row][j] -= factor * aug[col][j];
                }
            }
        }

        // Back substitution
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = aug[i][n];
            for (int j = i + 1; j < n; j++) {
                sum -= aug[i][j] * x[j];
            }
            x[i] = sum / aug[i][i];
        }

        return x;
    }

    /**
     * Bilinear interpolation sampling from source pixel array.
     * Returns transparent (0) for out-of-bounds coordinates.
     */
    private static int bilinearSample(int[] pixels, int w, int h, double x, double y) {
        if (x < 0 || y < 0 || x >= w - 1 || y >= h - 1) {
            // For border pixels, use nearest neighbor or transparent
            int ix = (int) Math.round(x);
            int iy = (int) Math.round(y);
            if (ix >= 0 && ix < w && iy >= 0 && iy < h) {
                return pixels[iy * w + ix];
            }
            return 0; // transparent
        }

        int x0 = (int) x;
        int y0 = (int) y;
        int x1 = x0 + 1;
        int y1 = y0 + 1;

        double fx = x - x0;
        double fy = y - y0;
        double fx1 = 1.0 - fx;
        double fy1 = 1.0 - fy;

        int p00 = pixels[y0 * w + x0];
        int p10 = pixels[y0 * w + x1];
        int p01 = pixels[y1 * w + x0];
        int p11 = pixels[y1 * w + x1];

        double w00 = fx1 * fy1;
        double w10 = fx * fy1;
        double w01 = fx1 * fy;
        double w11 = fx * fy;

        int a = PixelOp.clamp((int) Math.round(
                PixelOp.alpha(p00) * w00 + PixelOp.alpha(p10) * w10
                        + PixelOp.alpha(p01) * w01 + PixelOp.alpha(p11) * w11));
        int r = PixelOp.clamp((int) Math.round(
                PixelOp.red(p00) * w00 + PixelOp.red(p10) * w10
                        + PixelOp.red(p01) * w01 + PixelOp.red(p11) * w11));
        int g = PixelOp.clamp((int) Math.round(
                PixelOp.green(p00) * w00 + PixelOp.green(p10) * w10
                        + PixelOp.green(p01) * w01 + PixelOp.green(p11) * w11));
        int bl = PixelOp.clamp((int) Math.round(
                PixelOp.blue(p00) * w00 + PixelOp.blue(p10) * w10
                        + PixelOp.blue(p01) * w01 + PixelOp.blue(p11) * w11));

        return PixelOp.argb(a, r, g, bl);
    }

    /**
     * Validate that all values in the array are finite (not NaN or Infinity).
     */
    private static void validateFinite(double[] values, String name) {
        for (int i = 0; i < values.length; i++) {
            if (!Double.isFinite(values[i])) {
                throw new ImageOperationException(
                        name + "[" + i + "] is not finite: " + values[i],
                        ImageErrorCode.INVALID_PARAMETERS);
            }
        }
    }
}
