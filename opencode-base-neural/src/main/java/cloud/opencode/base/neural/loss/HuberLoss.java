package cloud.opencode.base.neural.loss;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Huber Loss Function (Smooth L1 Loss)
 * Huber损失函数（平滑L1损失）
 *
 * <p>Computes the Huber loss, a combination of MSE and MAE that is less sensitive
 * to outliers than MSE while still being differentiable at zero:</p>
 * <p>计算Huber损失，它是MSE和MAE的组合，比MSE对异常值更不敏感，
 * 同时在零点处仍然可微：</p>
 *
 * <ul>
 *   <li>If {@code |predicted - target| <= delta}: {@code 0.5 * (predicted - target)²}</li>
 *   <li>Otherwise: {@code delta * (|predicted - target| - 0.5 * delta)}</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable delta threshold - 可配置的delta阈值</li>
 *   <li>Smooth transition between quadratic and linear regions - 二次和线性区域之间的平滑过渡</li>
 *   <li>Robust to outliers - 对异常值鲁棒</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Default delta = 1.0
 * LossFunction huber = new HuberLoss();
 * // Custom delta
 * LossFunction huberCustom = new HuberLoss(0.5f);
 *
 * Tensor predicted = Tensor.fromFloat(new float[]{1.0f, 5.0f}, Shape.of(2));
 * Tensor target = Tensor.fromFloat(new float[]{1.5f, 2.0f}, Shape.of(2));
 * Tensor loss = huber.compute(predicted, target);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see LossFunction
 * @see MseLoss
 * @see MaeLoss
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class HuberLoss implements LossFunction {

    private final float delta;

    /**
     * Create a Huber loss function with the default delta of 1.0
     * 创建默认delta为1.0的Huber损失函数
     */
    public HuberLoss() {
        this(1.0f);
    }

    /**
     * Create a Huber loss function with a custom delta threshold
     * 创建自定义delta阈值的Huber损失函数
     *
     * @param delta the threshold at which to switch from quadratic to linear loss (must be positive) |
     *              从二次损失切换到线性损失的阈值（必须为正）
     * @throws NeuralException if delta is not positive | 当delta不为正时抛出
     */
    public HuberLoss(float delta) {
        if (delta <= 0.0f || Float.isNaN(delta) || Float.isInfinite(delta)) {
            throw new NeuralException("Delta must be a positive finite value, got " + delta,
                    NeuralErrorCode.INVALID_LOSS_INPUT);
        }
        this.delta = delta;
    }

    /**
     * Get the delta threshold
     * 获取delta阈值
     *
     * @return the delta value | delta值
     */
    public float delta() {
        return delta;
    }

    /**
     * Compute Huber loss
     * 计算Huber损失
     *
     * @param predicted the predicted output tensor | 预测输出张量
     * @param target    the ground truth target tensor | 真实目标张量
     * @return a scalar tensor containing the Huber loss value | 包含Huber损失值的标量张量
     * @throws NeuralException if inputs are null or shapes mismatch |
     *                         当输入为空或形状不匹配时抛出
     */
    @Override
    public Tensor compute(Tensor predicted, Tensor target) {
        LossUtil.validateInputs(predicted, target);

        float[] predData = predicted.toFloatArray();
        float[] targetData = target.toFloatArray();
        int n = predData.length;

        double sum = 0.0;
        double d = delta;
        for (int i = 0; i < n; i++) {
            double diff = Math.abs((double) predData[i] - (double) targetData[i]);
            if (diff <= d) {
                sum += 0.5 * diff * diff;
            } else {
                sum += d * (diff - 0.5 * d);
            }
        }

        float huber = (float) (sum / n);
        return Tensor.scalar(huber);
    }
}
