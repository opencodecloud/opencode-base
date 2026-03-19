package cloud.opencode.base.xml.bind.annotation;

import java.lang.annotation.*;

/**
 * XML Element List Annotation - Maps a collection to wrapped XML elements
 * XML 元素列表注解 - 将集合映射到包装的 XML 元素
 *
 * <p>This annotation specifies how a collection should be mapped to XML
 * with an optional wrapper element.</p>
 * <p>此注解指定如何将集合映射到带有可选包装元素的 XML。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Map collections to XML element lists - 将集合映射到 XML 元素列表</li>
 *   <li>Optional wrapper element support - 可选包装元素支持</li>
 *   <li>Custom item element naming - 自定义项目元素命名</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @XmlRoot("user")
 * public class User {
 *     @XmlElementList(value = "roles", itemName = "role")
 *     private List<String> roles;
 *     // Produces: <roles><role>admin</role><role>user</role></roles>
 *
 *     @XmlElementList(itemName = "tag")
 *     private List<String> tags;
 *     // Produces: <tag>java</tag><tag>xml</tag>
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
public @interface XmlElementList {

    /**
     * The wrapper element name.
     * 包装元素名称。
     *
     * @return the wrapper name, empty means no wrapper | 包装器名称，空表示无包装器
     */
    String value() default "";

    /**
     * The item element name.
     * 项目元素名称。
     *
     * @return the item element name | 项目元素名称
     */
    String itemName() default "";
}
