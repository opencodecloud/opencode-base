
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Auto-Detect - Controls Property Auto-Detection Visibility
 * JSON 自动检测 - 控制属性自动检测的可见性
 *
 * <p>This annotation controls which properties are auto-detected for
 * serialization and deserialization based on their visibility level.</p>
 * <p>此注解根据可见性级别控制哪些属性会被自动检测用于序列化和反序列化。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * @JsonAutoDetect(
 *     fieldVisibility = JsonAutoDetect.Visibility.ANY,
 *     getterVisibility = JsonAutoDetect.Visibility.NONE
 * )
 * public class User {
 *     private String name;
 *     private int age;
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Control field visibility for auto-detection - 控制字段自动检测的可见性</li>
 *   <li>Control getter/setter visibility - 控制getter/setter的可见性</li>
 *   <li>Control is-getter visibility - 控制is-getter的可见性</li>
 *   <li>Control creator (constructor/factory) visibility - 控制构造器/工厂方法的可见性</li>
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
public @interface JsonAutoDetect {

    /**
     * Visibility level for property auto-detection
     * 属性自动检测的可见性级别
     */
    enum Visibility {
        /**
         * Any visibility level (including private).
         * 任何可见性级别（包括private）。
         */
        ANY,

        /**
         * Non-private visibility (package, protected, public).
         * 非private可见性（包级别、protected、public）。
         */
        NON_PRIVATE,

        /**
         * Protected and public visibility.
         * protected和public可见性。
         */
        PROTECTED_AND_PUBLIC,

        /**
         * Public visibility only.
         * 仅public可见性。
         */
        PUBLIC_ONLY,

        /**
         * No auto-detection.
         * 不自动检测。
         */
        NONE,

        /**
         * Use default visibility rules.
         * 使用默认可见性规则。
         */
        DEFAULT
    }

    /**
     * Minimum visibility for auto-detecting fields.
     * 自动检测字段的最低可见性。
     *
     * @return the field visibility level - 字段可见性级别
     */
    Visibility fieldVisibility() default Visibility.DEFAULT;

    /**
     * Minimum visibility for auto-detecting getters.
     * 自动检测getter方法的最低可见性。
     *
     * @return the getter visibility level - getter可见性级别
     */
    Visibility getterVisibility() default Visibility.DEFAULT;

    /**
     * Minimum visibility for auto-detecting setters.
     * 自动检测setter方法的最低可见性。
     *
     * @return the setter visibility level - setter可见性级别
     */
    Visibility setterVisibility() default Visibility.DEFAULT;

    /**
     * Minimum visibility for auto-detecting is-getters (boolean getters).
     * 自动检测is-getter（布尔getter）方法的最低可见性。
     *
     * @return the is-getter visibility level - is-getter可见性级别
     */
    Visibility isGetterVisibility() default Visibility.DEFAULT;

    /**
     * Minimum visibility for auto-detecting creators (constructors, factory methods).
     * 自动检测构造器和工厂方法的最低可见性。
     *
     * @return the creator visibility level - 构造器可见性级别
     */
    Visibility creatorVisibility() default Visibility.DEFAULT;
}
