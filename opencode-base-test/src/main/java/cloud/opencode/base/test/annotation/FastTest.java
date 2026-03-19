package cloud.opencode.base.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fast Test
 * 快速测试
 *
 * <p>Marks a test as fast (typically &lt; 100ms).</p>
 * <p>标记测试为快速测试（通常小于100毫秒）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fast test categorization annotation - 快速测试分类注解</li>
 *   <li>Test filtering support - 测试过滤支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @FastTest
 * void shouldReturnQuickly() {
 *     // Fast test implementation
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
public @interface FastTest {

    /**
     * Description of the test
     * 测试描述
     *
     * @return the description | 描述
     */
    String value() default "";
}
