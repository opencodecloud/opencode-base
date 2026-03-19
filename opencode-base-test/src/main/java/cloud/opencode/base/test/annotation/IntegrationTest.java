package cloud.opencode.base.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Integration Test
 * 集成测试
 *
 * <p>Marks a test as an integration test.</p>
 * <p>标记测试为集成测试。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Integration test categorization annotation - 集成测试分类注解</li>
 *   <li>Resource requirement declaration - 资源需求声明</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @IntegrationTest(requires = {"database", "redis"})
 * void shouldProcessOrder() {
 *     // Integration test
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrationTest {

    /**
     * Description of the test
     * 测试描述
     *
     * @return the description | 描述
     */
    String value() default "";

    /**
     * Required resources
     * 所需资源
     *
     * @return the required resources | 所需资源
     */
    String[] requires() default {};
}
