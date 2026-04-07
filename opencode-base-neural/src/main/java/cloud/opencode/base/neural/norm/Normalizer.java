package cloud.opencode.base.neural.norm;

import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Data Normalizer Interface
 * 数据归一化器接口
 *
 * <p>Defines the contract for data normalization operations on tensors.
 * Implementations learn parameters from data via {@link #fit(Tensor)},
 * then apply the transformation via {@link #normalize(Tensor)} and
 * reverse it via {@link #denormalize(Tensor)}.</p>
 * <p>定义对张量进行数据归一化操作的契约。
 * 实现通过 {@link #fit(Tensor)} 从数据中学习参数，
 * 然后通过 {@link #normalize(Tensor)} 应用变换，
 * 通过 {@link #denormalize(Tensor)} 反转变换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>fit: learn normalization parameters from training data — 从训练数据中学习归一化参数</li>
 *   <li>normalize: transform data using learned parameters — 使用学习的参数变换数据</li>
 *   <li>denormalize: inverse transform to recover original scale — 逆变换恢复原始尺度</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see MinMaxNormalizer
 * @see ZScoreNormalizer
 * @see L2Normalizer
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public interface Normalizer {

    /**
     * Fit normalizer to data (learn parameters)
     * 拟合归一化器到数据（学习参数）
     *
     * @param data the training data tensor | 训练数据张量
     * @throws cloud.opencode.base.neural.exception.NeuralException if data is null or invalid |
     *         如果数据为 null 或无效
     */
    void fit(Tensor data);

    /**
     * Normalize data using learned parameters
     * 使用学习的参数归一化数据
     *
     * @param data the data tensor to normalize | 要归一化的数据张量
     * @return normalized tensor | 归一化后的张量
     * @throws cloud.opencode.base.neural.exception.NeuralException if not fitted or data is invalid |
     *         如果未拟合或数据无效
     */
    Tensor normalize(Tensor data);

    /**
     * Inverse transform — denormalize data to recover original scale
     * 逆变换 — 反归一化数据以恢复原始尺度
     *
     * @param data the normalized data tensor | 已归一化的数据张量
     * @return denormalized tensor | 反归一化后的张量
     * @throws cloud.opencode.base.neural.exception.NeuralException if not fitted or data is invalid |
     *         如果未拟合或数据无效
     * @throws UnsupportedOperationException if the normalization is not invertible |
     *         如果归一化不可逆
     */
    Tensor denormalize(Tensor data);
}
