package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * Dropout Operator (Inference Mode)
 * Dropout 算子（推理模式）
 *
 * <p>During inference, dropout is a no-op: the input tensor is passed through
 * unchanged. This operator simply returns the first input tensor as-is.</p>
 * <p>在推理阶段，Dropout 是无操作：输入张量原样传递。
 * 此算子直接返回第一个输入张量。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Inference passthrough: returns input unchanged - 推理透传：返回不变的输入</li>
 *   <li>Zero overhead: no data copy or computation - 零开销：无数据复制或计算</li>
 *   <li>Training dropout is not implemented (inference-only engine) - 未实现训练 Dropout（仅推理引擎）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Op dropout = new DropoutOp();
 * List<Tensor> outputs = dropout.forward(List.of(input), OpAttribute.empty());
 * // outputs.get(0) == input (same reference)
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Input validation: null and empty input checks - 输入验证：null 和空输入检查</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class DropoutOp implements Op {

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("DropoutOp requires at least 1 input", "Dropout");
        }
        return List.of(inputs.get(0));
    }
}
