package cloud.opencode.base.xml.bind.annotation;

import java.lang.annotation.*;

/**
 * XML Root Element Annotation - Marks a class as an XML root element
 * XML 根元素注解 - 标记类为 XML 根元素
 *
 * <p>This annotation specifies the root element name for XML binding.</p>
 * <p>此注解指定 XML 绑定的根元素名称。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Define root element name for XML binding - 定义 XML 绑定的根元素名称</li>
 *   <li>Namespace URI support - 命名空间 URI 支持</li>
 *   <li>Defaults to lowercase class name if not specified - 未指定时默认使用小写类名</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @XmlRoot("user")
 * public class User {
 *     private String name;
 *     private String email;
 * }
 *
 * @XmlRoot(value = "item", namespace = "http://example.com")
 * public class Item {
 *     private String id;
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
@Target(ElementType.TYPE)
@Documented
public @interface XmlRoot {

    /**
     * The element name.
     * 元素名称。
     *
     * @return the element name, defaults to class name | 元素名称，默认为类名
     */
    String value() default "";

    /**
     * The namespace URI.
     * 命名空间 URI。
     *
     * @return the namespace URI | 命名空间 URI
     */
    String namespace() default "";
}
