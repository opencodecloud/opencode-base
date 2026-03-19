package cloud.opencode.base.string.desensitize;

import cloud.opencode.base.string.desensitize.annotation.*;
import cloud.opencode.base.string.desensitize.strategy.*;
import java.lang.reflect.Field;

/**
 * Desensitize Processor - Processes fields annotated with desensitization rules.
 * 脱敏处理器 - 处理带有脱敏规则注解的字段。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Annotation-driven field desensitization - 注解驱动的字段脱敏</li>
 *   <li>Support for {@link DesensitizeBean} marked classes - 支持标记类</li>
 *   <li>Automatic type-based masking - 自动类型脱敏</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @DesensitizeBean
 * public class User {
 *     @Desensitize(DesensitizeType.MOBILE_PHONE)
 *     private String phone;
 * }
 *
 * User user = new User();
 * user.setPhone("13812345678");
 * DesensitizeProcessor.process(user); // phone -> "138****5678"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(f) where f is the number of annotated fields on the class - 时间复杂度: O(f)，f为类上带注解的字段数量</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class DesensitizeProcessor {

    private static final System.Logger LOG = System.getLogger(DesensitizeProcessor.class.getName());
    private DesensitizeProcessor() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static <T> T process(T obj) {
        if (obj == null) return null;
        
        Class<?> clazz = obj.getClass();
        if (!clazz.isAnnotationPresent(DesensitizeBean.class)) {
            return obj;
        }
        
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Desensitize.class)) {
                processField(obj, field);
            }
        }
        
        return obj;
    }

    private static void processField(Object obj, Field field) {
        try {
            field.setAccessible(true);
            Object value = field.get(obj);
            
            if (value instanceof String str) {
                Desensitize anno = field.getAnnotation(Desensitize.class);
                String desensitized = OpenMask.desensitize(str, anno.value());
                field.set(obj, desensitized);
            }
        } catch (Exception e) {
            // Log warning - sensitive data may not have been properly masked
            // 记录警告 - 敏感数据可能未被正确脱敏
            LOG.log(System.Logger.Level.WARNING,
                    () -> "Desensitization failed for field: " + field.getName()
                            + " - sensitive data may not be masked", e);
        }
    }
}
