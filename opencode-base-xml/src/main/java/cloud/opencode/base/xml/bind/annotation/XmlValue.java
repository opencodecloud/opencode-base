package cloud.opencode.base.xml.bind.annotation;

import java.lang.annotation.*;

/**
 * XML Value Annotation - Maps a field to element text content
 * XML 值注解 - 将字段映射到元素文本内容
 *
 * <p>This annotation marks a field to receive the text content of the element.</p>
 * <p>此注解标记字段接收元素的文本内容。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Map a field to element text content - 将字段映射到元素文本内容</li>
 *   <li>Combine with @XmlAttribute for mixed content elements - 与 @XmlAttribute 组合用于混合内容元素</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @XmlRoot("item")
 * public class Item {
 *     @XmlAttribute("id")
 *     private Long id;
 *
 *     @XmlValue
 *     private String content; // receives element text
 * }
 *
 * // XML: <item id="1">Some content here</item>
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
public @interface XmlValue {
}
