
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Managed Reference - Marks the Forward Part of a Bidirectional Relationship
 * JSON 管理引用 - 标记双向关系的正向部分
 *
 * <p>This annotation marks the "parent" (forward) side of a bidirectional relationship.
 * The annotated property is serialized normally. It works in pair with
 * {@link JsonBackReference} to handle parent-child relationships without
 * causing infinite recursion during serialization.</p>
 * <p>此注解标记双向关系的"父"（正向）一侧。被注解的属性正常序列化。
 * 它与 {@link JsonBackReference} 配对使用，以处理父子关系，
 * 避免序列化时出现无限递归。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class Parent {
 *     @JsonManagedReference
 *     private List<Child> children;
 * }
 *
 * public class Child {
 *     @JsonBackReference
 *     private Parent parent;
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Serialized normally as the forward reference - 作为正向引用正常序列化</li>
 *   <li>Paired with @JsonBackReference by matching value - 通过匹配 value 与 @JsonBackReference 配对</li>
 *   <li>Prevents infinite recursion in bidirectional relationships - 防止双向关系中的无限递归</li>
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
 * @see JsonBackReference
 * @since JDK 25, opencode-base-json V1.0.0
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonManagedReference {

    /**
     * The logical name of the reference, used to match with {@link JsonBackReference}.
     * 引用的逻辑名称，用于与 {@link JsonBackReference} 匹配。
     *
     * @return the reference name - 引用名称
     */
    String value() default "defaultReference";
}
