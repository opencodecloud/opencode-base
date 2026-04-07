
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Root Name - Specifies Root Wrapping Name
 * JSON 根名称 - 指定根包装名称
 *
 * <p>This annotation specifies the root element name used to wrap the
 * serialized JSON object. This is useful for APIs that expect a root
 * wrapper element, or for XML interoperability.</p>
 * <p>此注解指定用于包装序列化 JSON 对象的根元素名称。对于期望根包装元素的 API
 * 或 XML 互操作性非常有用。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * @JsonRootName("user")
 * public class User {
 *     private String name;
 *     private int age;
 * }
 * // Serializes as: {"user":{"name":"Alice","age":30}}
 *
 * @JsonRootName(value = "item", namespace = "http://example.com/ns")
 * public class Item {
 *     private String title;
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Root wrapping for serialized output - 序列化输出的根包装</li>
 *   <li>Optional namespace for XML interop - 可选的 XML 互操作命名空间</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonRootName {

    /**
     * The root wrapping name.
     * 根包装名称。
     *
     * @return the root name, or empty for default - 根名称，空则使用默认值
     */
    String value() default "";

    /**
     * Optional namespace for XML interoperability.
     * 可选的 XML 互操作命名空间。
     *
     * @return the namespace URI, or empty for none - 命名空间 URI，空则无命名空间
     */
    String namespace() default "";
}
