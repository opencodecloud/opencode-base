package cloud.opencode.base.neural.metric;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Confusion Matrix Utility
 * 混淆矩阵工具类
 *
 * <p>Computes a multi-class confusion matrix and derives per-class metrics
 * (accuracy, precision, recall, F1 score) from the matrix. The matrix layout is
 * {@code matrix[actual][predicted] = count}.</p>
 * <p>计算多分类混淆矩阵，并从矩阵中导出每个类别的指标（准确率、精确率、召回率、F1 分数）。
 * 矩阵布局为 {@code matrix[actual][predicted] = count}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multi-class confusion matrix computation | 多分类混淆矩阵计算</li>
 *   <li>Per-class accuracy, precision, recall, F1 | 按类别的准确率、精确率、召回率、F1</li>
 *   <li>Safe handling of zero denominators | 安全处理零分母</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Accuracy
 * @see Precision
 * @see Recall
 * @see F1Score
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class ConfusionMatrix {

    private ConfusionMatrix() {
        // utility class
    }

    /**
     * Compute confusion matrix from predicted and target class labels
     * 从预测类别标签和目标类别标签计算混淆矩阵
     *
     * @param predicted  predicted tensor of shape [N] with integer class labels | 形状为 [N] 的整数类别标签预测张量
     * @param target     target tensor of shape [N] with integer class labels | 形状为 [N] 的整数类别标签目标张量
     * @param numClasses number of classes (matrix dimension) | 类别数（矩阵维度）
     * @return confusion matrix where {@code matrix[actual][predicted] = count} |
     *         混淆矩阵，{@code matrix[actual][predicted] = count}
     * @throws NeuralException if inputs are null, empty, shapes don't match,
     *                         or labels are out of range | 如果输入为空、形状不匹配或标签超出范围
     */
    public static int[][] compute(Tensor predicted, Tensor target, int numClasses) {
        MetricValidation.requireNonNull(predicted, "predicted");
        MetricValidation.requireNonNull(target, "target");
        MetricValidation.requireRank(predicted, 1, "predicted");
        MetricValidation.requireRank(target, 1, "target");
        MetricValidation.requireSameShape(predicted, target);

        int n = predicted.shape().dim(0);
        MetricValidation.requirePositiveSize(n);

        if (numClasses <= 0) {
            throw new NeuralException(
                    "numClasses must be positive, got " + numClasses,
                    NeuralErrorCode.INVALID_METRIC_INPUT);
        }

        float[] pred = predicted.toFloatArray();
        float[] tgt = target.toFloatArray();

        int[][] matrix = new int[numClasses][numClasses];
        for (int i = 0; i < n; i++) {
            int predLabel = Math.round(pred[i]);
            int targetLabel = Math.round(tgt[i]);
            if (predLabel < 0 || predLabel >= numClasses) {
                throw new NeuralException(
                        "Predicted label " + predLabel + " out of range [0, " + numClasses + ")",
                        NeuralErrorCode.INVALID_METRIC_INPUT);
            }
            if (targetLabel < 0 || targetLabel >= numClasses) {
                throw new NeuralException(
                        "Target label " + targetLabel + " out of range [0, " + numClasses + ")",
                        NeuralErrorCode.INVALID_METRIC_INPUT);
            }
            matrix[targetLabel][predLabel]++;
        }
        return matrix;
    }

    /**
     * Compute overall accuracy from confusion matrix: sum(diagonal) / sum(all)
     * 从混淆矩阵计算总准确率：sum(对角线) / sum(全部)
     *
     * @param matrix confusion matrix | 混淆矩阵
     * @return accuracy in [0, 1] | [0, 1] 范围的准确率
     * @throws NeuralException if matrix is null or empty | 如果矩阵为空
     */
    public static float accuracy(int[][] matrix) {
        requireValidMatrix(matrix);
        int diagonal = 0;
        int total = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                total += matrix[i][j];
                if (i == j) {
                    diagonal += matrix[i][j];
                }
            }
        }
        if (total == 0) {
            return 0.0f;
        }
        return (float) diagonal / total;
    }

    /**
     * Compute per-class precision: TP / (TP + FP)
     * 计算指定类别的精确率：TP / (TP + FP)
     *
     * @param matrix   confusion matrix | 混淆矩阵
     * @param classIdx class index | 类别索引
     * @return precision in [0, 1] | [0, 1] 范围的精确率
     * @throws NeuralException if matrix is null, empty, or classIdx is out of range |
     *                         如果矩阵为空或类别索引超出范围
     */
    public static float precision(int[][] matrix, int classIdx) {
        requireValidMatrix(matrix);
        requireValidClassIdx(matrix, classIdx);

        int tp = matrix[classIdx][classIdx];
        int columnSum = 0;
        for (int[] row : matrix) {
            columnSum += row[classIdx];
        }
        if (columnSum == 0) {
            return 0.0f;
        }
        return (float) tp / columnSum;
    }

    /**
     * Compute per-class recall: TP / (TP + FN)
     * 计算指定类别的召回率：TP / (TP + FN)
     *
     * @param matrix   confusion matrix | 混淆矩阵
     * @param classIdx class index | 类别索引
     * @return recall in [0, 1] | [0, 1] 范围的召回率
     * @throws NeuralException if matrix is null, empty, or classIdx is out of range |
     *                         如果矩阵为空或类别索引超出范围
     */
    public static float recall(int[][] matrix, int classIdx) {
        requireValidMatrix(matrix);
        requireValidClassIdx(matrix, classIdx);

        int tp = matrix[classIdx][classIdx];
        int rowSum = 0;
        for (int val : matrix[classIdx]) {
            rowSum += val;
        }
        if (rowSum == 0) {
            return 0.0f;
        }
        return (float) tp / rowSum;
    }

    /**
     * Compute per-class F1 score: 2 * P * R / (P + R)
     * 计算指定类别的 F1 分数：2 * P * R / (P + R)
     *
     * @param matrix   confusion matrix | 混淆矩阵
     * @param classIdx class index | 类别索引
     * @return F1 score in [0, 1] | [0, 1] 范围的 F1 分数
     * @throws NeuralException if matrix is null, empty, or classIdx is out of range |
     *                         如果矩阵为空或类别索引超出范围
     */
    public static float f1Score(int[][] matrix, int classIdx) {
        float p = precision(matrix, classIdx);
        float r = recall(matrix, classIdx);
        float denominator = p + r;
        if (denominator == 0.0f) {
            return 0.0f;
        }
        return 2.0f * p * r / denominator;
    }

    private static void requireValidMatrix(int[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            throw new NeuralException(
                    "Confusion matrix must not be null or empty",
                    NeuralErrorCode.INVALID_METRIC_INPUT);
        }
    }

    private static void requireValidClassIdx(int[][] matrix, int classIdx) {
        if (classIdx < 0 || classIdx >= matrix.length) {
            throw new NeuralException(
                    "Class index " + classIdx + " out of range [0, " + matrix.length + ")",
                    NeuralErrorCode.INVALID_METRIC_INPUT);
        }
    }
}
