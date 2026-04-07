package cloud.opencode.base.neural.loss;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Loss Utility Methods
 * 损失函数工具方法
 *
 * <p>Internal utility class providing common validation and helper methods
 * shared across loss function implementations.</p>
 * <p>内部工具类，提供损失函数实现之间共享的通用验证和辅助方法。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
final class LossUtil {

    private LossUtil() {
        // utility class
    }

    /**
     * Validate that predicted and target tensors are non-null, have matching shapes,
     * and have positive element count
     * 验证预测和目标张量非空、形状匹配且元素数为正
     *
     * @param predicted the predicted tensor | 预测张量
     * @param target    the target tensor | 目标张量
     * @throws NeuralException if validation fails | 验证失败时抛出
     */
    static void validateInputs(Tensor predicted, Tensor target) {
        if (predicted == null) {
            throw new NeuralException("Predicted tensor must not be null",
                    NeuralErrorCode.INVALID_LOSS_INPUT);
        }
        if (target == null) {
            throw new NeuralException("Target tensor must not be null",
                    NeuralErrorCode.INVALID_LOSS_INPUT);
        }
        if (!predicted.shape().equals(target.shape())) {
            throw new NeuralException(
                    "Shape mismatch: predicted " + predicted.shape() + " vs target " + target.shape(),
                    NeuralErrorCode.INVALID_LOSS_INPUT);
        }
        if (predicted.shape().size() <= 0) {
            throw new NeuralException("Tensor must have positive element count",
                    NeuralErrorCode.INVALID_LOSS_INPUT);
        }
    }
}
