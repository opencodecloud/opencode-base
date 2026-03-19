package cloud.opencode.base.string.desensitize.annotation;

import cloud.opencode.base.string.desensitize.strategy.DesensitizeType;
import java.lang.annotation.*;

/**
 * Desensitize Annotation - Marks fields for automatic desensitization.
 * 脱敏注解 - 标记需要自动脱敏的字段。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Field-level desensitization marking - 字段级脱敏标记</li>
 *   <li>Configurable mask character and keep lengths - 可配置掩码字符和保留长度</li>
 *   <li>Built-in and custom strategy support - 内置和自定义策略支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @Desensitize(DesensitizeType.MOBILE_PHONE)
 * private String phone;
 *
 * @Desensitize(value = DesensitizeType.CUSTOM, startKeep = 2, endKeep = 2, maskChar = '#')
 * private String custom;
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
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Desensitize {
    DesensitizeType value();
    int startKeep() default 0;
    int endKeep() default 0;
    char maskChar() default '*';
    String customStrategy() default "";
}
