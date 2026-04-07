package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;

/**
 * Neural Network Operator Interface
 * 神经网络算子接口
 *
 * <p>Base interface for all neural network operators. Each operator implements
 * stateless forward computation: receives input tensors and attributes,
 * produces output tensors.</p>
 * <p>所有神经网络算子的基础接口。每个算子实现无状态的前向计算：
 * 接收输入张量和属性，产出输出张量。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Stateless forward computation - 无状态前向计算</li>
 *   <li>Thread-safe by design (no mutable state) - 设计上线程安全（无可变状态）</li>
 *   <li>Composable via OpRegistry - 通过 OpRegistry 可组合</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Op reluOp = OpRegistry.create("ReLU");
 * List<Tensor> outputs = reluOp.forward(List.of(inputTensor), OpAttribute.empty());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@FunctionalInterface
public interface Op {

    /**
     * Execute forward computation
     * 执行前向计算
     *
     * @param inputs ordered input tensors (may include weights) | 有序输入张量（可能包含权重）
     * @param attrs  operator attributes (kernel size, stride, etc.) | 算子属性（卷积核大小、步幅等）
     * @return ordered output tensors | 有序输出张量
     * @throws cloud.opencode.base.neural.exception.OpExecutionException if computation fails | 计算失败时抛出
     */
    List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs);
}
