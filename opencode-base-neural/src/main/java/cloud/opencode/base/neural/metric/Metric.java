package cloud.opencode.base.neural.metric;

import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Metric Functional Interface
 * 指标函数式接口
 *
 * <p>Defines a standard contract for computing evaluation metrics between predicted
 * and target tensors. All metric implementations should return a scalar tensor
 * containing the computed metric value.</p>
 * <p>定义计算预测张量和目标张量之间评估指标的标准契约。所有指标实现应返回包含计算指标值的标量张量。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified metric computation interface | 统一的指标计算接口</li>
 *   <li>Supports lambda and method reference composition | 支持 lambda 和方法引用组合</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Accuracy
 * @see BinaryAccuracy
 * @see Precision
 * @see Recall
 * @see F1Score
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
@FunctionalInterface
public interface Metric {

    /**
     * Compute metric between predicted and target values
     * 计算预测值和目标值之间的指标
     *
     * @param predicted the predicted tensor | 预测张量
     * @param target    the target tensor | 目标张量
     * @return scalar tensor containing the metric value | 包含指标值的标量张量
     */
    Tensor compute(Tensor predicted, Tensor target);
}
