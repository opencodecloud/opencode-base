package cloud.opencode.base.deepclone.annotation;

import java.lang.annotation.*;

/**
 * Annotation to force deep cloning of a field
 * 标记强制深度克隆的字段注解
 *
 * <p>Fields marked with this annotation will always be deeply cloned,
 * even if the type would normally be treated as immutable.</p>
 * <p>标记此注解的字段将始终被深度克隆，即使该类型通常被视为不可变类型。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class Entity {
 *     @CloneDeep(reason = "Mutable wrapper that needs deep copy")
 *     private LocalDateTime modifiedTime;
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Force deep cloning of annotated fields - 强制深度克隆注解字段</li>
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
public @interface CloneDeep {

    /**
     * Reason for forcing deep clone (for documentation)
     * 强制深拷贝的原因（用于文档）
     *
     * @return the reason | 原因
     */
    String reason() default "";
}
