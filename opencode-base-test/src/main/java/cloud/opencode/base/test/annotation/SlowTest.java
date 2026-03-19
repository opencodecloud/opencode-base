package cloud.opencode.base.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Slow Test
 * 慢速测试
 *
 * <p>Marks a test as slow (typically &gt; 1s).</p>
 * <p>标记测试为慢速测试（通常大于1秒）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Slow test categorization annotation - 慢测试分类注解</li>
 *   <li>Test filtering support - 测试过滤支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @SlowTest
 * void shouldProcessLargeDataset() {
 *     // Slow test implementation
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
public @interface SlowTest {

    /**
     * Description of the test
     * 测试描述
     *
     * @return the description | 描述
     */
    String value() default "";

    /**
     * Expected duration in milliseconds
     * 期望时长（毫秒）
     *
     * @return the expected duration | 期望时长
     */
    long expectedMillis() default 1000;
}
