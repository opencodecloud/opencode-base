package cloud.opencode.base.deepclone.annotation;

import java.lang.annotation.*;

/**
 * Annotation to mark fields that should be ignored during cloning
 * 标记克隆时忽略的字段注解
 *
 * <p>Fields marked with this annotation will be set to their default value (null/0/false)
 * in the cloned object.</p>
 * <p>标记此注解的字段在克隆对象中将被设置为默认值（null/0/false）。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class Entity {
 *     private String id;
 *
 *     @CloneIgnore(reason = "Cache data, not needed in clone")
 *     private transient Map<String, Object> cache;
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Skip annotated fields during cloning - 克隆时跳过注解字段</li>
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
public @interface CloneIgnore {

    /**
     * Reason for ignoring this field (for documentation)
     * 忽略此字段的原因（用于文档）
     *
     * @return the reason | 原因
     */
    String reason() default "";
}
