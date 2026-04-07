package cloud.opencode.base.neural.model;

import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.TensorType;

import java.util.Objects;

/**
 * Graph Input Descriptor
 * 计算图输入描述符
 *
 * <p>Immutable record describing an input to the computation graph,
 * including its name, expected data type, and shape.</p>
 * <p>描述计算图输入的不可变记录，
 * 包括名称、期望的数据类型和形状。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param name  input name | 输入名称
 * @param type  expected tensor type | 期望的张量类型
 * @param shape expected tensor shape | 期望的张量形状
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Graph
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public record GraphInput(String name, TensorType type, Shape shape) {

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造器
     */
    public GraphInput {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(shape, "shape must not be null");
    }
}
