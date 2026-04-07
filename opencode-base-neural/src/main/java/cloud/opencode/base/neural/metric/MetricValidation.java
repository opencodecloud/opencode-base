package cloud.opencode.base.neural.metric;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Metric Input Validation Utility
 * 指标输入验证工具类
 *
 * <p>Provides common validation methods shared across all metric implementations.
 * Throws {@link NeuralException} with {@link NeuralErrorCode#INVALID_METRIC_INPUT}
 * for all validation failures.</p>
 * <p>提供所有指标实现共享的通用验证方法。所有验证失败均抛出
 * 带有 {@link NeuralErrorCode#INVALID_METRIC_INPUT} 的 {@link NeuralException}。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
final class MetricValidation {

    private MetricValidation() {
        // utility class
    }

    /**
     * Require tensor is not null
     * 要求张量不为空
     *
     * @param tensor the tensor to check | 要检查的张量
     * @param name   parameter name for error message | 用于错误消息的参数名
     * @throws NeuralException if tensor is null | 如果张量为空
     */
    static void requireNonNull(Tensor tensor, String name) {
        if (tensor == null) {
            throw new NeuralException(
                    name + " tensor must not be null",
                    NeuralErrorCode.INVALID_METRIC_INPUT);
        }
    }

    /**
     * Require tensor has specific rank
     * 要求张量具有指定阶数
     *
     * @param tensor       the tensor to check | 要检查的张量
     * @param expectedRank expected rank | 期望的阶数
     * @param name         parameter name for error message | 用于错误消息的参数名
     * @throws NeuralException if rank doesn't match | 如果阶数不匹配
     */
    static void requireRank(Tensor tensor, int expectedRank, String name) {
        if (tensor.shape().rank() != expectedRank) {
            throw new NeuralException(
                    name + " tensor must be " + expectedRank + "D, got rank " + tensor.shape().rank(),
                    NeuralErrorCode.INVALID_METRIC_INPUT);
        }
    }

    /**
     * Require two tensors have the same shape
     * 要求两个张量具有相同形状
     *
     * @param a first tensor | 第一个张量
     * @param b second tensor | 第二个张量
     * @throws NeuralException if shapes don't match | 如果形状不匹配
     */
    static void requireSameShape(Tensor a, Tensor b) {
        if (!a.shape().equals(b.shape())) {
            throw new NeuralException(
                    "Shape mismatch: predicted " + a.shape() + " vs target " + b.shape(),
                    NeuralErrorCode.INVALID_METRIC_INPUT);
        }
    }

    /**
     * Require two dimension sizes are equal
     * 要求两个维度大小相等
     *
     * @param sizeA first size | 第一个大小
     * @param sizeB second size | 第二个大小
     * @param nameA first name | 第一个名称
     * @param nameB second name | 第二个名称
     * @throws NeuralException if sizes don't match | 如果大小不匹配
     */
    static void requireSameSize(int sizeA, int sizeB, String nameA, String nameB) {
        if (sizeA != sizeB) {
            throw new NeuralException(
                    nameA + " (" + sizeA + ") must equal " + nameB + " (" + sizeB + ")",
                    NeuralErrorCode.INVALID_METRIC_INPUT);
        }
    }

    /**
     * Require size is positive (greater than 0)
     * 要求大小为正数（大于 0）
     *
     * @param size the size to check | 要检查的大小
     * @throws NeuralException if size is not positive | 如果大小不为正
     */
    static void requirePositiveSize(int size) {
        if (size <= 0) {
            throw new NeuralException(
                    "Input size must be positive, got " + size,
                    NeuralErrorCode.INVALID_METRIC_INPUT);
        }
    }
}
