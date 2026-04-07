package cloud.opencode.base.xml.bind.annotation;

import java.lang.annotation.*;

/**
 * XML Element Annotation - Maps a field to an XML element
 * XML 元素注解 - 将字段映射到 XML 元素
 *
 * <p>This annotation specifies how a field should be mapped to an XML element.</p>
 * <p>此注解指定如何将字段映射到 XML 元素。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Map fields to XML elements - 将字段映射到 XML 元素</li>
 *   <li>Custom element name and namespace - 自定义元素名称和命名空间</li>
 *   <li>CDATA section support - CDATA 节支持</li>
 *   <li>Default value and required field marking - 默认值和必需字段标记</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @XmlRoot("user")
 * public class User {
 *     @XmlElement("name")
 *     private String name;
 *
 *     @XmlElement(value = "description", cdata = true)
 *     private String description;
 *
 *     @XmlElement(required = true)
 *     private String email;
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
public @interface XmlElement {

    /**
     * The element name.
     * 元素名称。
     *
     * @return the element name, defaults to field name | 元素名称，默认为字段名
     */
    String value() default "";

    /**
     * The namespace URI.
     * 命名空间 URI。
     *
     * @return the namespace URI | 命名空间 URI
     */
    String namespace() default "";

    /**
     * Whether the element is required.
     * 元素是否必需。
     *
     * @return true if required | 如果必需则返回 true
     */
    boolean required() default false;

    /**
     * The default value.
     * 默认值。
     *
     * @return the default value | 默认值
     */
    String defaultValue() default "";

    /**
     * Whether to use CDATA section.
     * 是否使用 CDATA 节。
     *
     * @return true if CDATA | 如果使用 CDATA 则返回 true
     */
    boolean cdata() default false;
}
