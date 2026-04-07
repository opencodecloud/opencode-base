package cloud.opencode.base.image.analysis;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Template Matching using Normalized Cross-Correlation (NCC)
 * 基于归一化互相关（NCC）的模板匹配
 *
 * <p>Finds occurrences of a template image within a larger source image by computing
 * the Normalized Cross-Correlation at every valid position. NCC produces a score
 * in [-1, 1] where 1.0 indicates a perfect match.</p>
 * <p>通过在每个有效位置计算归一化互相关来查找模板图像在较大源图像中的出现。
 * NCC 产生 [-1, 1] 范围内的分数，1.0 表示完美匹配。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Best-match search: find the single best matching position - 最佳匹配搜索: 查找单个最佳匹配位置</li>
 *   <li>Multi-match search: find all positions above a threshold with NMS - 多匹配搜索: 查找所有高于阈值的位置并进行非极大值抑制</li>
 *   <li>Grayscale-based comparison for illumination robustness - 基于灰度的比较，对光照具有鲁棒性</li>
 *   <li>Non-maximum suppression to eliminate overlapping detections - 非极大值抑制消除重叠检测</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BufferedImage source = ...; // large image
 * BufferedImage template = ...; // small template to find
 *
 * // Find best match
 * TemplateMatchOp.MatchResult best = TemplateMatchOp.match(source, template);
 * System.out.printf("Best match at (%d, %d) with score %.4f%n",
 *     best.x(), best.y(), best.score());
 *
 * // Find all matches above threshold 0.8
 * List<TemplateMatchOp.MatchResult> all = TemplateMatchOp.matchAll(source, template, 0.8);
 * for (TemplateMatchOp.MatchResult m : all) {
 *     System.out.printf("Match at (%d, %d), score=%.4f%n", m.x(), m.y(), m.score());
 * }
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(W * H * tw * th) where (W, H) is source size and (tw, th) is template size -
 *       时间复杂度: O(W * H * tw * th)，(W, H) 为源图像尺寸，(tw, th) 为模板尺寸</li>
 *   <li>Space complexity: O(W * H) for the grayscale source and score map -
 *       空间复杂度: O(W * H) 用于灰度源图像和分数图</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless, all methods are pure functions) - 线程安全: 是（无状态，所有方法为纯函数）</li>
 *   <li>Null-safe: No (null image throws NullPointerException) - 空值安全: 否（null 图像抛出 NullPointerException）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
public final class TemplateMatchOp {

    private TemplateMatchOp() {
        throw new AssertionError("No TemplateMatchOp instances");
    }

    /**
     * A template match result at a specific position with its NCC score.
     * 特定位置的模板匹配结果及其 NCC 分数。
     *
     * @param x     the x coordinate of the top-left corner of the match | 匹配左上角的 x 坐标
     * @param y     the y coordinate of the top-left corner of the match | 匹配左上角的 y 坐标
     * @param score the NCC score in [-1, 1], where 1.0 = perfect match | NCC 分数，范围 [-1, 1]，1.0 = 完美匹配
     */
    public record MatchResult(int x, int y, double score) {
    }

    /**
     * Find the best match of a template in the source image using NCC.
     * 使用 NCC 在源图像中查找模板的最佳匹配。
     *
     * <p>Both images are converted to grayscale before matching. The method slides the
     * template over every valid position in the source and computes the Normalized
     * Cross-Correlation score.</p>
     * <p>匹配前两幅图像均转换为灰度。方法将模板滑过源图像中每个有效位置，
     * 并计算归一化互相关分数。</p>
     *
     * @param source   the source image to search in | 要搜索的源图像
     * @param template the template image to find | 要查找的模板图像
     * @return the best match result | 最佳匹配结果
     * @throws NullPointerException    if source or template is null | 当源图像或模板为 null 时抛出
     * @throws ImageOperationException if template is larger than source | 当模板大于源图像时抛出
     */
    public static MatchResult match(BufferedImage source, BufferedImage template) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(template, "template must not be null");

        int sw = source.getWidth();
        int sh = source.getHeight();
        int tw = template.getWidth();
        int th = template.getHeight();

        validateDimensions(sw, sh, tw, th);

        int[] sourceGray = toGrayArray(source, sw, sh);
        int[] templateGray = toGrayArray(template, tw, th);

        // Precompute template statistics
        double templateMean = computeMean(templateGray);
        double templateStd = computeStd(templateGray, templateMean);

        int searchW = sw - tw + 1;
        int searchH = sh - th + 1;

        double bestScore = -2.0;
        int bestX = 0;
        int bestY = 0;

        for (int y = 0; y < searchH; y++) {
            for (int x = 0; x < searchW; x++) {
                double score = computeNcc(sourceGray, sw, templateGray, tw, th,
                        x, y, templateMean, templateStd);
                if (score > bestScore) {
                    bestScore = score;
                    bestX = x;
                    bestY = y;
                }
            }
        }

        return new MatchResult(bestX, bestY, bestScore);
    }

    /**
     * Find all matches of a template in the source image above a threshold.
     * 查找源图像中高于阈值的所有模板匹配。
     *
     * <p>After computing NCC at every valid position, local maxima above the given
     * threshold are selected. Non-maximum suppression is applied to eliminate overlapping
     * detections within a template-sized neighborhood.</p>
     * <p>在每个有效位置计算 NCC 后，选择高于给定阈值的局部极大值。
     * 应用非极大值抑制来消除模板大小邻域内的重叠检测。</p>
     *
     * @param source    the source image to search in | 要搜索的源图像
     * @param template  the template image to find | 要查找的模板图像
     * @param threshold the minimum NCC score threshold in [-1, 1] | 最小 NCC 分数阈值，范围 [-1, 1]
     * @return the list of match results above the threshold, sorted by score descending |
     *         高于阈值的匹配结果列表，按分数降序排序
     * @throws NullPointerException    if source or template is null | 当源图像或模板为 null 时抛出
     * @throws ImageOperationException if template is larger than source or threshold is invalid |
     *                                  当模板大于源图像或阈值无效时抛出
     */
    public static List<MatchResult> matchAll(BufferedImage source, BufferedImage template,
                                             double threshold) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(template, "template must not be null");

        if (threshold < -1.0 || threshold > 1.0) {
            throw new ImageOperationException(
                    "Threshold must be in [-1, 1], got: " + threshold,
                    ImageErrorCode.INVALID_PARAMETERS);
        }

        int sw = source.getWidth();
        int sh = source.getHeight();
        int tw = template.getWidth();
        int th = template.getHeight();

        validateDimensions(sw, sh, tw, th);

        int[] sourceGray = toGrayArray(source, sw, sh);
        int[] templateGray = toGrayArray(template, tw, th);

        double templateMean = computeMean(templateGray);
        double templateStd = computeStd(templateGray, templateMean);

        int searchW = sw - tw + 1;
        int searchH = sh - th + 1;

        // Compute full score map
        double[][] scoreMap = new double[searchH][searchW];
        for (int y = 0; y < searchH; y++) {
            for (int x = 0; x < searchW; x++) {
                scoreMap[y][x] = computeNcc(sourceGray, sw, templateGray, tw, th,
                        x, y, templateMean, templateStd);
            }
        }

        // Collect candidates above threshold
        List<MatchResult> candidates = new ArrayList<>();
        for (int y = 0; y < searchH; y++) {
            for (int x = 0; x < searchW; x++) {
                if (scoreMap[y][x] >= threshold) {
                    candidates.add(new MatchResult(x, y, scoreMap[y][x]));
                }
            }
        }

        // Sort by score descending for NMS (greedy)
        candidates.sort(Comparator.comparingDouble(MatchResult::score).reversed());

        // Non-maximum suppression: suppress matches within template-sized neighborhood of better matches
        List<MatchResult> results = new ArrayList<>();
        boolean[] suppressed = new boolean[candidates.size()];

        for (int i = 0; i < candidates.size(); i++) {
            if (suppressed[i]) {
                continue;
            }
            MatchResult current = candidates.get(i);
            results.add(current);

            // Suppress all lower-scoring candidates within template neighborhood
            for (int j = i + 1; j < candidates.size(); j++) {
                if (suppressed[j]) {
                    continue;
                }
                MatchResult other = candidates.get(j);
                if (Math.abs(current.x() - other.x()) < tw
                        && Math.abs(current.y() - other.y()) < th) {
                    suppressed[j] = true;
                }
            }
        }

        return Collections.unmodifiableList(results);
    }

    /**
     * Validate that the template fits within the source.
     */
    private static void validateDimensions(int sw, int sh, int tw, int th) {
        if (tw <= 0 || th <= 0 || sw <= 0 || sh <= 0) {
            throw new ImageOperationException(
                    "Image dimensions must be positive",
                    ImageErrorCode.INVALID_DIMENSIONS);
        }
        if (tw > sw || th > sh) {
            throw new ImageOperationException(
                    "Template (" + tw + "x" + th + ") is larger than source (" + sw + "x" + sh + ")",
                    ImageErrorCode.INVALID_DIMENSIONS);
        }
    }

    /**
     * Convert a BufferedImage to a flat grayscale int array.
     */
    private static int[] toGrayArray(BufferedImage image, int width, int height) {
        BufferedImage argb = PixelOp.ensureArgb(image);
        int[] pixels = PixelOp.getPixels(argb);
        return ChannelOp.toGray(pixels);
    }

    /**
     * Compute the mean of pixel values in a grayscale array.
     */
    private static double computeMean(int[] gray) {
        long sum = 0;
        for (int v : gray) {
            sum += v;
        }
        return (double) sum / gray.length;
    }

    /**
     * Compute the standard deviation of pixel values given the mean.
     */
    private static double computeStd(int[] gray, double mean) {
        double sumSq = 0.0;
        for (int v : gray) {
            double diff = v - mean;
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq / gray.length);
    }

    /**
     * Compute NCC score for a specific position in the source image.
     *
     * <p>NCC = sum((I(x+i, y+j) - meanI) * (T(i, j) - meanT)) / (N * stdI * stdT)</p>
     */
    private static double computeNcc(int[] sourceGray, int sw,
                                     int[] templateGray, int tw, int th,
                                     int offsetX, int offsetY,
                                     double templateMean, double templateStd) {
        int n = tw * th;

        // Compute source patch mean
        long patchSum = 0;
        for (int ty = 0; ty < th; ty++) {
            int srcRowOffset = (offsetY + ty) * sw + offsetX;
            int tplRowOffset = ty * tw;
            for (int tx = 0; tx < tw; tx++) {
                patchSum += sourceGray[srcRowOffset + tx];
            }
        }
        double patchMean = (double) patchSum / n;

        // Compute source patch standard deviation and cross-correlation
        double patchSumSq = 0.0;
        double crossSum = 0.0;

        for (int ty = 0; ty < th; ty++) {
            int srcRowOffset = (offsetY + ty) * sw + offsetX;
            int tplRowOffset = ty * tw;
            for (int tx = 0; tx < tw; tx++) {
                double srcDiff = sourceGray[srcRowOffset + tx] - patchMean;
                double tplDiff = templateGray[tplRowOffset + tx] - templateMean;
                patchSumSq += srcDiff * srcDiff;
                crossSum += srcDiff * tplDiff;
            }
        }

        double patchStd = Math.sqrt(patchSumSq / n);

        // Avoid division by zero: if either std is zero, patches are constant
        double denominator = (double) n * patchStd * templateStd;
        if (denominator < 1e-10) {
            // Both constant and equal means => perfect match; otherwise no correlation
            return Math.abs(patchMean - templateMean) < 1e-10 ? 1.0 : 0.0;
        }

        return crossSum / denominator;
    }
}
