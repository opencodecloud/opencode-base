package cloud.opencode.base.image.transform;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Affine Transform Operations for Images
 * 图像仿射变换操作
 *
 * <p>Provides affine transformations on images including translation, rotation,
 * scaling, shearing and arbitrary 6-parameter affine mappings. Supports both
 * direct matrix specification and 3-point correspondence mapping.</p>
 * <p>提供图像仿射变换操作，包括平移、旋转、缩放、错切以及任意 6 参数仿射映射。
 * 同时支持直接矩阵指定和 3 点对应映射。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Apply 6-parameter affine transform matrix [a,b,c,d,e,f] - 应用 6 参数仿射变换矩阵 [a,b,c,d,e,f]</li>
 *   <li>Map 3 source points to 3 destination points - 将 3 个源点映射到 3 个目标点</li>
 *   <li>Bilinear interpolation for high-quality output - 双线性插值保证高质量输出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Identity transform (no change)
 * BufferedImage result = AffineTransformOp.apply(image, new double[]{1,0,0, 0,1,0});
 *
 * // Scale 2x
 * BufferedImage scaled = AffineTransformOp.apply(image, new double[]{2,0,0, 0,2,0});
 *
 * // 3-point mapping
 * double[] src = {0,0, 100,0, 0,100};
 * double[] dst = {10,10, 210,10, 10,210};
 * BufferedImage mapped = AffineTransformOp.apply(image, src, dst, 220, 220);
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
public final class AffineTransformOp {

    private AffineTransformOp() {
        throw new AssertionError("No AffineTransformOp instances");
    }

    /**
     * Apply an affine transform with a 6-parameter matrix.
     * 应用 6 参数仿射变换矩阵。
     *
     * <p>The matrix parameters [a, b, c, d, e, f] define the transform:</p>
     * <pre>
     * x' = a*x + b*y + c
     * y' = d*x + e*y + f
     * </pre>
     *
     * <p>The output image has the same dimensions as the input.</p>
     *
     * @param image  the source image | 源图像
     * @param matrix the 6-parameter affine matrix [a, b, c, d, e, f] | 6 参数仿射矩阵 [a, b, c, d, e, f]
     * @return the transformed image | 变换后的图像
     * @throws ImageOperationException if image is null, matrix is null or length != 6 | 当 image 为 null、matrix 为 null 或长度不为 6 时抛出
     */
    public static BufferedImage apply(BufferedImage image, double[] matrix) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(matrix, "matrix must not be null");
        if (matrix.length != 6) {
            throw new ImageOperationException(
                    "Affine matrix must have exactly 6 elements, got: " + matrix.length,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        validateFinite(matrix, "matrix");

        // AffineTransform constructor: (m00, m10, m01, m11, m02, m12)
        // Our convention: x' = a*x + b*y + c, y' = d*x + e*y + f
        // AffineTransform: x' = m00*x + m01*y + m02, y' = m10*x + m11*y + m12
        // So: m00=a, m01=b, m02=c, m10=d, m11=e, m12=f
        double a = matrix[0], b = matrix[1], c = matrix[2];
        double d = matrix[3], e = matrix[4], f = matrix[5];
        AffineTransform at = new AffineTransform(a, d, b, e, c, f);

        return applyTransform(image, at, image.getWidth(), image.getHeight());
    }

    /**
     * Apply an affine transform derived from 3 source points mapped to 3 destination points.
     * 应用从 3 个源点映射到 3 个目标点的仿射变换。
     *
     * <p>Solves the 6-parameter affine system from the 3 point correspondences.
     * Points are specified as flat arrays: [x0, y0, x1, y1, x2, y2].</p>
     *
     * @param image        the source image | 源图像
     * @param srcPoints    source points [x0, y0, x1, y1, x2, y2] | 源点坐标 [x0, y0, x1, y1, x2, y2]
     * @param dstPoints    destination points [x0, y0, x1, y1, x2, y2] | 目标点坐标 [x0, y0, x1, y1, x2, y2]
     * @param outputWidth  the output image width | 输出图像宽度
     * @param outputHeight the output image height | 输出图像高度
     * @return the transformed image | 变换后的图像
     * @throws ImageOperationException if any input is null, points arrays have wrong length,
     *                                 output dimensions are not positive, or the point mapping is degenerate
     *                                 当输入为 null、点数组长度不对、输出尺寸非正数或点映射退化时抛出
     */
    public static BufferedImage apply(BufferedImage image,
                                      double[] srcPoints, double[] dstPoints,
                                      int outputWidth, int outputHeight) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(srcPoints, "srcPoints must not be null");
        Objects.requireNonNull(dstPoints, "dstPoints must not be null");
        if (srcPoints.length != 6) {
            throw new ImageOperationException(
                    "srcPoints must have exactly 6 elements (3 points), got: " + srcPoints.length,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (dstPoints.length != 6) {
            throw new ImageOperationException(
                    "dstPoints must have exactly 6 elements (3 points), got: " + dstPoints.length,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (outputWidth <= 0 || outputHeight <= 0) {
            throw new ImageOperationException(
                    "Output dimensions must be positive, got: " + outputWidth + "x" + outputHeight,
                    ImageErrorCode.INVALID_DIMENSIONS);
        }
        validateFinite(srcPoints, "srcPoints");
        validateFinite(dstPoints, "dstPoints");

        // Solve affine matrix from 3-point correspondence:
        // dst = A * src where A = [a b c; d e f; 0 0 1]
        // For each point: x' = a*x + b*y + c, y' = d*x + e*y + f
        // 3 points give 6 equations for 6 unknowns
        double sx0 = srcPoints[0], sy0 = srcPoints[1];
        double sx1 = srcPoints[2], sy1 = srcPoints[3];
        double sx2 = srcPoints[4], sy2 = srcPoints[5];

        double dx0 = dstPoints[0], dy0 = dstPoints[1];
        double dx1 = dstPoints[2], dy1 = dstPoints[3];
        double dx2 = dstPoints[4], dy2 = dstPoints[5];

        // Determinant of source matrix
        double det = sx0 * (sy1 - sy2) - sy0 * (sx1 - sx2) + (sx1 * sy2 - sx2 * sy1);
        if (Math.abs(det) < 1e-10) {
            throw new ImageOperationException(
                    "Source points are collinear, cannot compute affine transform",
                    ImageErrorCode.INVALID_PARAMETERS);
        }

        double invDet = 1.0 / det;

        // Solve for a, b, c (x' row)
        double a = ((sy1 - sy2) * dx0 + (sy2 - sy0) * dx1 + (sy0 - sy1) * dx2) * invDet;
        double b = ((sx2 - sx1) * dx0 + (sx0 - sx2) * dx1 + (sx1 - sx0) * dx2) * invDet;
        double cVal = ((sx1 * sy2 - sx2 * sy1) * dx0 + (sx2 * sy0 - sx0 * sy2) * dx1
                + (sx0 * sy1 - sx1 * sy0) * dx2) * invDet;

        // Solve for d, e, f (y' row)
        double d = ((sy1 - sy2) * dy0 + (sy2 - sy0) * dy1 + (sy0 - sy1) * dy2) * invDet;
        double e = ((sx2 - sx1) * dy0 + (sx0 - sx2) * dy1 + (sx1 - sx0) * dy2) * invDet;
        double fVal = ((sx1 * sy2 - sx2 * sy1) * dy0 + (sx2 * sy0 - sx0 * sy2) * dy1
                + (sx0 * sy1 - sx1 * sy0) * dy2) * invDet;

        // AffineTransform constructor: (m00, m10, m01, m11, m02, m12)
        AffineTransform at = new AffineTransform(a, d, b, e, cVal, fVal);

        return applyTransform(image, at, outputWidth, outputHeight);
    }

    /**
     * Apply an AffineTransform to the image with bilinear interpolation.
     */
    private static BufferedImage applyTransform(BufferedImage image, AffineTransform at,
                                                int width, int height) {
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = output.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(image, at, null);
        } finally {
            g.dispose();
        }
        return output;
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
