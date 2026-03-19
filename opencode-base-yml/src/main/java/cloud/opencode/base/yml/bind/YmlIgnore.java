package cloud.opencode.base.yml.bind;

import java.lang.annotation.*;

/**
 * YML Ignore - Excludes a field from YAML binding
 * YML 忽略 - 将字段从 YAML 绑定中排除
 *
 * <p>This annotation prevents a field from being included in YAML serialization/deserialization.</p>
 * <p>此注解阻止字段包含在 YAML 序列化/反序列化中。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exclude fields from YAML serialization and deserialization - 从 YAML 序列化和反序列化中排除字段</li>
 *   <li>Applicable to fields and methods - 适用于字段和方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class UserConfig {
 *     private String name;
 *
 *     @YmlIgnore
 *     private String password;
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (annotation is inherently immutable) - 线程安全: 是（注解本身不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YmlIgnore {
}
