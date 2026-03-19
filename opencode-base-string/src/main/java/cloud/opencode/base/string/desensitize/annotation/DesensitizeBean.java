package cloud.opencode.base.string.desensitize.annotation;

import java.lang.annotation.*;

/**
 * Desensitize Bean Annotation - Marks beans containing desensitizable fields.
 * 脱敏Bean注解 - 标记包含可脱敏字段的Bean。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-level marker for desensitization processing - 类型级脱敏处理标记</li>
 *   <li>Enable/disable toggle - 启用/禁用开关</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @DesensitizeBean
 * public class UserVO {
 *     @Desensitize(DesensitizeType.MOBILE_PHONE)
 *     private String phone;
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (annotation is immutable) - 线程安全: 是（注解不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DesensitizeBean {
    boolean enabled() default true;
}
