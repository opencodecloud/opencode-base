package cloud.opencode.base.feature.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Feature Toggle Annotation
 * 功能开关注解
 *
 * <p>Annotation for marking methods or types as feature-gated.</p>
 * <p>用于标记方法或类型为功能门控的注解。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Method-level toggle - 方法级别开关</li>
 *   <li>Type-level toggle - 类型级别开关</li>
 *   <li>Default value support - 默认值支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @FeatureToggle("dark-mode")
 * public void renderDarkTheme() {
 *     // Only executes if dark-mode feature is enabled
 * }
 *
 * @FeatureToggle(value = "beta-feature", defaultEnabled = false)
 * public class BetaService {
 *     // Class gated by beta-feature flag
 * }
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeatureToggle {

    /**
     * Feature key
     * 功能键
     *
     * @return the feature key | 功能键
     */
    String value();

    /**
     * Default enabled state when feature is not found
     * 功能未找到时的默认启用状态
     *
     * @return default enabled state | 默认启用状态
     */
    boolean defaultEnabled() default false;

    /**
     * Description of the feature
     * 功能描述
     *
     * @return feature description | 功能描述
     */
    String description() default "";
}
