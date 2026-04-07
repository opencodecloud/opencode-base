
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Back Reference - Marks the Back Part of a Bidirectional Relationship
 * JSON 反向引用 - 标记双向关系的反向部分
 *
 * <p>This annotation marks the "child" (back) side of a bidirectional relationship.
 * The annotated property is <strong>not serialized</strong>, but is restored
 * during deserialization by linking back to the parent object marked with
 * {@link JsonManagedReference}.</p>
 * <p>此注解标记双向关系的"子"（反向）一侧。被注解的属性<strong>不会被序列化</strong>，
 * 但在反序列化时会通过链接回被 {@link JsonManagedReference} 标记的父对象来恢复。</p>
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
 *   <li>Omitted from serialization output - 从序列化输出中省略</li>
 *   <li>Restored during deserialization via the forward reference - 反序列化时通过正向引用恢复</li>
 *   <li>Value must match the paired @JsonManagedReference - value 必须与配对的 @JsonManagedReference 匹配</li>
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
 * @see JsonManagedReference
 * @since JDK 25, opencode-base-json V1.0.0
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonBackReference {

    /**
     * The logical name of the reference, must match the corresponding {@link JsonManagedReference}.
     * 引用的逻辑名称，必须与对应的 {@link JsonManagedReference} 匹配。
     *
     * @return the reference name - 引用名称
     */
    String value() default "defaultReference";
}
