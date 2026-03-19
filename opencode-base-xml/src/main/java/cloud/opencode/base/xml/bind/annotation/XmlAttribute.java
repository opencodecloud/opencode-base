package cloud.opencode.base.xml.bind.annotation;

import java.lang.annotation.*;

/**
 * XML Attribute Annotation - Maps a field to an XML attribute
 * XML 属性注解 - 将字段映射到 XML 属性
 *
 * <p>This annotation specifies how a field should be mapped to an XML attribute.</p>
 * <p>此注解指定如何将字段映射到 XML 属性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Map fields to XML attributes - 将字段映射到 XML 属性</li>
 *   <li>Custom attribute name via value() - 通过 value() 自定义属性名</li>
 *   <li>Required attribute marking - 必需属性标记</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @XmlRoot("user")
 * public class User {
 *     @XmlAttribute("id")
 *     private Long id;
 *
 *     @XmlAttribute(required = true)
 *     private String type;
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
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface XmlAttribute {

    /**
     * The attribute name.
     * 属性名称。
     *
     * @return the attribute name, defaults to field name | 属性名称，默认为字段名
     */
    String value() default "";

    /**
     * Whether the attribute is required.
     * 属性是否必需。
     *
     * @return true if required | 如果必需则返回 true
     */
    boolean required() default false;
}
