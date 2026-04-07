package cloud.opencode.base.neural.model;

import java.util.Objects;

/**
 * Model Metadata Record
 * 模型元数据记录
 *
 * <p>Immutable record containing descriptive metadata for a neural network model,
 * including its name, author, description, and creation timestamp.</p>
 * <p>包含神经网络模型描述性元数据的不可变记录，
 * 包括模型名称、作者、描述和创建时间戳。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param name        model name | 模型名称
 * @param author      model author | 模型作者
 * @param description model description | 模型描述
 * @param createdAt   creation timestamp in epoch milliseconds | 创建时间戳（毫秒）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see OcmModel
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public record ModelMetadata(String name, String author, String description, long createdAt) {

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造器
     */
    public ModelMetadata {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(author, "author must not be null");
        Objects.requireNonNull(description, "description must not be null");
        if (createdAt < 0) {
            throw new IllegalArgumentException("createdAt must not be negative");
        }
    }
}
