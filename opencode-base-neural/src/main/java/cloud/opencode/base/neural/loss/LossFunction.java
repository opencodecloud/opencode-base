package cloud.opencode.base.neural.loss;

import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Loss Function Interface
 * 损失函数接口
 *
 * <p>Functional interface for computing the loss (error) between predicted and target tensors.
 * All loss functions reduce to a scalar value representing the aggregate error.</p>
 * <p>用于计算预测张量和目标张量之间损失（误差）的函数式接口。
 * 所有损失函数都归约为表示聚合误差的标量值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Compute loss between predicted and target tensors - 计算预测值与目标值之间的损失</li>
 *   <li>Returns scalar tensor result - 返回标量张量结果</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LossFunction mse = new MseLoss();
 * Tensor predicted = Tensor.fromFloat(new float[]{0.5f, 0.8f}, Shape.of(2));
 * Tensor target = Tensor.fromFloat(new float[]{1.0f, 0.0f}, Shape.of(2));
 * Tensor loss = mse.compute(predicted, target);
 * float lossValue = loss.toFloatArray()[0];
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see MseLoss
 * @see MaeLoss
 * @see BinaryCrossEntropyLoss
 * @see CrossEntropyLoss
 * @see HuberLoss
 * @see CosineSimilarityLoss
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
@FunctionalInterface
public interface LossFunction {

    /**
     * Compute loss between predicted and target tensors
     * 计算预测值和目标值之间的损失
     *
     * @param predicted the predicted output tensor | 预测输出张量
     * @param target    the ground truth target tensor | 真实目标张量
     * @return a scalar tensor containing the loss value | 包含损失值的标量张量
     * @throws cloud.opencode.base.neural.exception.NeuralException
     *         if inputs are invalid or computation fails |
     *         当输入无效或计算失败时抛出
     */
    Tensor compute(Tensor predicted, Tensor target);
}
