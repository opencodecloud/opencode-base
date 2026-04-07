package cloud.opencode.base.neural.model;

import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.TensorType;

import java.util.Objects;

/**
 * Tensor Information Descriptor
 * 张量信息描述符
 *
 * <p>Immutable record describing a tensor's metadata: name, data type, and shape.
 * Used to describe model inputs and outputs without carrying actual data.</p>
 * <p>描述张量元数据的不可变记录：名称、数据类型和形状。
 * 用于描述模型输入和输出，不携带实际数据。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param name  tensor name | 张量名称
 * @param type  tensor data type | 张量数据类型
 * @param shape tensor shape | 张量形状
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see OcmModel
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public record TensorInfo(String name, TensorType type, Shape shape) {

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造器
     */
    public TensorInfo {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(shape, "shape must not be null");
    }
}
