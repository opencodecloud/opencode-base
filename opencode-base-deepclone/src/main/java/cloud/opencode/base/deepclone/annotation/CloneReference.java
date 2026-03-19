package cloud.opencode.base.deepclone.annotation;

import java.lang.annotation.*;

/**
 * Annotation to mark fields that should be shallow copied (reference only)
 * 标记克隆时仅复制引用的字段注解
 *
 * <p>Fields marked with this annotation will share the same reference in the cloned object.
 * This is useful for shared resources like connection pools, thread pools, etc.</p>
 * <p>标记此注解的字段在克隆对象中将共享相同的引用。
 * 适用于共享资源如连接池、线程池等。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class Service {
 *     @CloneReference(reason = "Shared database connection pool")
 *     private DataSource dataSource;
 *
 *     @CloneReference(reason = "Shared executor service")
 *     private ExecutorService executor;
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Shallow copy annotated fields (reference only) - 浅拷贝注解字段（仅引用）</li>
 *   <li>Runtime retention for reflection - 运行时保留用于反射</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (annotation is immutable) - 线程安全: 是（注解不可变）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface CloneReference {

    /**
     * Reason for using shallow copy (for documentation)
     * 使用浅拷贝的原因（用于文档）
     *
     * @return the reason | 原因
     */
    String reason() default "";
}
