package cloud.opencode.base.id;

import java.util.ArrayList;
import java.util.List;

/**
 * ID Generator Interface
 * ID生成器接口
 *
 * <p>Unified interface for all ID generators in this library.
 * Implementations must be thread-safe.</p>
 * <p>本库所有ID生成器的统一接口。实现必须是线程安全的。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Single ID generation - 单个ID生成</li>
 *   <li>Batch ID generation - 批量ID生成</li>
 *   <li>Generator type identification - 生成器类型标识</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * IdGenerator<Long> snowflake = SnowflakeGenerator.create();
 * long id = snowflake.generate();
 * List<Long> ids = snowflake.generateBatch(100);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @param <T> the type of ID generated | 生成的ID类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@FunctionalInterface
public interface IdGenerator<T> {

    /**
     * Generates the next ID
     * 生成下一个ID
     *
     * @return generated ID | 生成的ID
     */
    T generate();

    /**
     * Generates a batch of IDs
     * 批量生成ID
     *
     * @param count the number of IDs to generate | 要生成的ID数量
     * @return list of generated IDs | 生成的ID列表
     */
    default List<T> generateBatch(int count) {
        List<T> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ids.add(generate());
        }
        return ids;
    }

    /**
     * Gets the generator type name
     * 获取生成器类型名称
     *
     * @return type name | 类型名称
     */
    default String getType() {
        return getClass().getSimpleName();
    }
}
