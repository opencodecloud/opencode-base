
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Naming - Specifies Property Naming Strategy
 * JSON 命名 - 指定属性命名策略
 *
 * <p>This annotation specifies the naming strategy for converting
 * Java field names to JSON property names.</p>
 * <p>此注解指定将 Java 字段名转换为 JSON 属性名的命名策略。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * @JsonNaming(Strategy.SNAKE_CASE)
 * public class UserProfile {
 *     private String userName;      // -> "user_name"
 *     private String emailAddress;  // -> "email_address"
 * }
 *
 * @JsonNaming(Strategy.KEBAB_CASE)
 * public class Config {
 *     private String maxRetryCount; // -> "max-retry-count"
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple naming strategies (snake_case, kebab-case, PascalCase, etc.) - 多种命名策略</li>
 *   <li>Class-level naming strategy annotation - 类级命名策略注解</li>
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
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonNaming {

    /**
     * Naming strategy enumeration
     * 命名策略枚举
     */
    enum Strategy {
        /**
         * Use field names as-is (identity).
         * 按原样使用字段名（恒等映射）。
         * <p>Example: userName -> userName</p>
         */
        IDENTITY,

        /**
         * Convert camelCase to snake_case.
         * 将驼峰式转换为蛇形命名。
         * <p>Example: userName -> user_name</p>
         */
        SNAKE_CASE,

        /**
         * Convert camelCase to UPPER_SNAKE_CASE.
         * 将驼峰式转换为大写蛇形命名。
         * <p>Example: userName -> USER_NAME</p>
         */
        UPPER_SNAKE_CASE,

        /**
         * Convert camelCase to kebab-case.
         * 将驼峰式转换为短横线命名。
         * <p>Example: userName -> user-name</p>
         */
        KEBAB_CASE,

        /**
         * Convert camelCase to PascalCase.
         * 将驼峰式转换为帕斯卡命名。
         * <p>Example: userName -> UserName</p>
         */
        PASCAL_CASE,

        /**
         * Convert to lowercase.
         * 转换为小写。
         * <p>Example: userName -> username</p>
         */
        LOWER_CASE,

        /**
         * Convert to dot.case notation.
         * 转换为点号分隔命名。
         * <p>Example: userName -> user.name</p>
         */
        DOT_CASE
    }

    /**
     * The naming strategy to use.
     * 要使用的命名策略。
     *
     * @return the strategy - 策略
     */
    Strategy value() default Strategy.IDENTITY;
}
