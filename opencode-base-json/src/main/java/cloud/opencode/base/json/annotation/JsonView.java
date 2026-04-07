
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON View - Selective Property Serialization
 * JSON 视图 - 选择性属性序列化
 *
 * <p>This annotation defines one or more views for selective property
 * serialization. By assigning properties to different view classes,
 * you can control which properties are included when serializing
 * with a specific view active.</p>
 * <p>此注解定义一个或多个视图以进行选择性属性序列化。
 * 通过将属性分配到不同的视图类，可以控制在特定视图激活时
 * 序列化时包含哪些属性。</p>
 *
 * <p>View classes are typically empty interfaces or classes used purely
 * as markers. View inheritance is supported: if a property is annotated
 * with a parent view, it will also be included when serializing with
 * any child view.</p>
 * <p>视图类通常是空的接口或类，纯粹用作标记。
 * 支持视图继承：如果属性标注了父视图，在使用任何子视图序列化时
 * 也会被包含。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Define view marker interfaces
 * public class Views {
 *     public interface Summary {}
 *     public interface Detail extends Summary {}
 *     public interface Admin extends Detail {}
 * }
 *
 * public class User {
 *     @JsonView(Views.Summary.class)
 *     private String name;
 *
 *     @JsonView(Views.Detail.class)
 *     private String email;
 *
 *     @JsonView(Views.Admin.class)
 *     private String role;
 *
 *     private String internalId; // no view = included in all or none
 * }
 *
 * // Serialize with Summary view: only "name" is included
 * // Serialize with Detail view: "name" and "email" are included
 * // Serialize with Admin view: "name", "email", and "role" are included
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Selective property inclusion based on active view - 基于活动视图的选择性属性包含</li>
 *   <li>Multiple views per property - 每个属性可有多个视图</li>
 *   <li>View inheritance support - 视图继承支持</li>
 *   <li>Applicable to fields, methods, and types - 可应用于字段、方法和类型</li>
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
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonView {

    /**
     * The view classes that this property belongs to.
     * 此属性所属的视图类。
     *
     * <p>View classes are typically empty interfaces used as markers.
     * A property annotated with a view will only be serialized when
     * that view (or a child view) is active.</p>
     * <p>视图类通常是空的接口，用作标记。
     * 标注了视图的属性仅在该视图（或子视图）激活时才会被序列化。</p>
     *
     * @return the view classes - 视图类
     */
    Class<?>[] value();
}
