package cloud.opencode.base.core.builder;

/**
 * Builder Interface - Functional interface for object building
 * 构建器接口 - 对象构建的函数式接口
 *
 * <p>Base functional interface for implementing builder pattern.</p>
 * <p>用于实现构建器模式的基础函数式接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Build target object (build) - 构建目标对象</li>
 *   <li>Lambda-compatible functional interface - Lambda 兼容的函数式接口</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Builder<User> builder = () -> new User("name", 18);
 * User user = builder.build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @param <T> Build target type - 构建目标类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@FunctionalInterface
public interface Builder<T> {

    /**
     * Builds the target object.
     * 构建目标对象。
     *
     * @return the built object | 构建的对象
     */
    T build();
}
