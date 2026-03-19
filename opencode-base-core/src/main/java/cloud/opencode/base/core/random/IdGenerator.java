package cloud.opencode.base.core.random;

/**
 * ID Generator Interface - Contract for unique ID generation
 * ID 生成器接口 - 唯一 ID 生成契约
 *
 * <p>Functional interface for implementing custom ID generation strategies.</p>
 * <p>用于实现自定义 ID 生成策略的函数式接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Generate single ID (nextId) - 生成单个 ID</li>
 *   <li>Generate multiple IDs (nextIds) - 批量生成 ID</li>
 *   <li>Lambda-compatible functional interface - Lambda 兼容的函数式接口</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Custom implementation - 自定义实现
 * IdGenerator generator = () -> UUID.randomUUID().toString();
 * String id = generator.nextId();
 * String[] ids = generator.nextIds(10);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Implementation dependent - 空值安全: 取决于实现</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@FunctionalInterface
public interface IdGenerator {

    /**
     * Generates the next ID
     * 生成下一个 ID
     */
    String nextId();

    /**
     * Generates the specified number of IDs
     * 生成指定数量的 ID
     */
    default String[] nextIds(int count) {
        String[] ids = new String[count];
        for (int i = 0; i < count; i++) {
            ids[i] = nextId();
        }
        return ids;
    }
}
