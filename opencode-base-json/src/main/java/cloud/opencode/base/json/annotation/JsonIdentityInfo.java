
package cloud.opencode.base.json.annotation;

import cloud.opencode.base.json.identity.ObjectIdGenerator;
import cloud.opencode.base.json.identity.ObjectIdResolver;
import cloud.opencode.base.json.identity.SimpleObjectIdResolver;

import java.lang.annotation.*;

/**
 * JSON Identity Info - Handles Object Identity for Circular References
 * JSON 身份信息 - 处理循环引用的对象身份
 *
 * <p>This annotation enables id-based serialization to handle circular references
 * and shared object graphs. On first encounter, the full object is serialized
 * along with an identity property. Subsequent references serialize only the ID.</p>
 * <p>此注解启用基于 ID 的序列化来处理循环引用和共享对象图。
 * 首次遇到时，完整对象与身份属性一起序列化。后续引用仅序列化 ID。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * @JsonIdentityInfo(
 *     generator = ObjectIdGenerators.IntSequenceGenerator.class,
 *     property = "@id"
 * )
 * public class Node {
 *     private String name;
 *     private List<Node> children;
 *     private Node parent;
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pluggable ID generators (integer, UUID, property, string) - 可插拔 ID 生成器（整数、UUID、属性、字符串）</li>
 *   <li>Customizable identity property name - 可自定义身份属性名</li>
 *   <li>Scoped uniqueness for ID isolation - 作用域唯一性实现 ID 隔离</li>
 *   <li>Pluggable ID resolver for custom lookup strategies - 可插拔 ID 解析器支持自定义查找策略</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ObjectIdGenerator
 * @see ObjectIdResolver
 * @since JDK 25, opencode-base-json V1.0.0
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonIdentityInfo {

    /**
     * The object ID generator class to use for producing identifiers.
     * 用于生成标识符的对象 ID 生成器类。
     *
     * @return the generator class - 生成器类
     */
    Class<? extends ObjectIdGenerator<?>> generator();

    /**
     * The name of the JSON property used to store the object identity.
     * 用于存储对象身份的 JSON 属性名称。
     *
     * @return the property name - 属性名称
     */
    String property() default "@id";

    /**
     * The scope class for determining ID uniqueness boundaries.
     * 用于确定 ID 唯一性边界的作用域类。
     *
     * <p>{@code Void.class} indicates global scope (default).</p>
     * <p>{@code Void.class} 表示全局作用域（默认值）。</p>
     *
     * @return the scope class - 作用域类
     */
    Class<?> scope() default Void.class;

    /**
     * The resolver class used to look up previously seen objects by their ID.
     * 用于按 ID 查找先前遇到的对象的解析器类。
     *
     * @return the resolver class - 解析器类
     */
    Class<? extends ObjectIdResolver> resolver() default SimpleObjectIdResolver.class;
}
