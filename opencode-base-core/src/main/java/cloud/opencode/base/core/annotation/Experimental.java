package cloud.opencode.base.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an API as experimental.
 * 标记一个 API 为实验性的。
 *
 * <p>Experimental APIs may change or be removed in future releases without notice.
 * They are available for early feedback but should not be relied upon in production.</p>
 * <p>实验性 API 可能在未来版本中更改或删除，恕不另行通知。
 * 它们可供早期反馈使用，但不应在生产环境中依赖。</p>
 *
 * <p><strong>Example / 示例:</strong></p>
 * <pre>{@code
 * @Experimental(since = "1.0.0", reason = "API shape may change based on community feedback")
 * public class ReactiveHttpClient { ... }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Mark APIs as experimental at type, method, constructor, or field level - 在类型、方法、构造器或字段级别标记API为实验性</li>
 *   <li>Configurable since version and reason - 可配置起始版本和原因</li>
 *   <li>Runtime retention for reflective access - 运行时保留以支持反射访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @Experimental(since = "1.0.0", reason = "API shape may change")
 * public class NewFeature { }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (annotation is immutable) - 线程安全: 是（注解不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
public @interface Experimental {

    /**
     * The version since which this API is experimental.
     * 此 API 从哪个版本开始成为实验性的。
     *
     * @return the version string - 版本字符串
     */
    String since() default "";

    /**
     * The reason this API is experimental.
     * 此 API 成为实验性的原因。
     *
     * @return the reason - 原因
     */
    String reason() default "API shape may change based on community feedback";
}
