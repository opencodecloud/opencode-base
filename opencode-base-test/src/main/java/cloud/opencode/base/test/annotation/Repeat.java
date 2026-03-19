package cloud.opencode.base.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Repeat
 * 重复
 *
 * <p>Marks a test to be repeated multiple times.</p>
 * <p>标记测试重复多次执行。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Repeat test execution annotation - 重复测试执行注解</li>
 *   <li>Configurable iteration count - 可配置的迭代次数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @Repeat(100)
 * void shouldBeConsistent() {
 *     // Repeated test
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
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Repeat {

    /**
     * Number of times to repeat
     * 重复次数
     *
     * @return the count | 次数
     */
    int value() default 1;

    /**
     * Fail fast on first failure
     * 首次失败时快速失败
     *
     * @return true to fail fast | 如果快速失败返回true
     */
    boolean failFast() default true;
}
