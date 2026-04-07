package cloud.opencode.base.xml.bind.annotation;

import java.lang.annotation.*;

/**
 * XML Ignore Annotation - Excludes a field from XML binding
 * XML 忽略注解 - 从 XML 绑定中排除字段
 *
 * <p>This annotation marks a field to be ignored during XML marshalling/unmarshalling.</p>
 * <p>此注解标记字段在 XML 编组/解组期间被忽略。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exclude fields from XML marshalling/unmarshalling - 从 XML 编组/解组中排除字段</li>
 *   <li>Applicable to fields and methods - 适用于字段和方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @XmlRoot("user")
 * public class User {
 *     private String name;
 *
 *     @XmlIgnore
 *     private String password; // not included in XML
 *
 *     @XmlIgnore
 *     private transient Object cache;
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (annotation is immutable) - 线程安全: 是（注解不可变）</li>
 *   <li>Null-safe: N/A (annotation type) - 空值安全: 不适用（注解类型）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
@Documented
public @interface XmlIgnore {
}
