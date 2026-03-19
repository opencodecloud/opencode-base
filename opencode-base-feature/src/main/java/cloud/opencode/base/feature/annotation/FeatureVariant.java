package cloud.opencode.base.feature.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Feature Variant Annotation
 * 功能变体注解
 *
 * <p>Annotation for marking methods as feature variants for A/B testing.</p>
 * <p>用于标记方法为A/B测试功能变体的注解。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>A/B testing support - A/B测试支持</li>
 *   <li>Multiple variants - 多变体支持</li>
 *   <li>Variant identification - 变体标识</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @FeatureVariant(feature = "checkout-flow", variant = "A")
 * public void checkoutFlowA() {
 *     // Original checkout flow
 * }
 *
 * @FeatureVariant(feature = "checkout-flow", variant = "B")
 * public void checkoutFlowB() {
 *     // New checkout flow for testing
 * }
 *
 * @FeatureVariant(feature = "button-color", variant = "blue", percentage = 50)
 * public void renderBlueButton() {
 *     // 50% of users see blue button
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
public @interface FeatureVariant {

    /**
     * Feature key
     * 功能键
     *
     * @return the feature key | 功能键
     */
    String feature();

    /**
     * Variant identifier (e.g., "A", "B", "control", "experiment")
     * 变体标识符（例如："A"、"B"、"control"、"experiment"）
     *
     * @return variant identifier | 变体标识符
     */
    String variant();

    /**
     * Percentage of traffic for this variant (0-100)
     * 此变体的流量百分比（0-100）
     *
     * @return traffic percentage | 流量百分比
     */
    int percentage() default 0;

    /**
     * Description of the variant
     * 变体描述
     *
     * @return variant description | 变体描述
     */
    String description() default "";
}
