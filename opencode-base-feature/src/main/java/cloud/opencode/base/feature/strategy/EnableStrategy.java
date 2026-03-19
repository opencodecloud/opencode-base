package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;

/**
 * Enable Strategy Interface
 * 启用策略接口
 *
 * <p>Interface for feature enablement strategies.</p>
 * <p>功能启用策略的接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Feature evaluation - 功能评估</li>
 *   <li>Context-aware decision - 上下文感知决策</li>
 *   <li>Composable strategies - 可组合策略</li>
 * </ul>
 *
 * <p><strong>Built-in Strategies | 内置策略:</strong></p>
 * <ul>
 *   <li>{@link AlwaysOnStrategy} - Always enabled | 始终启用</li>
 *   <li>{@link AlwaysOffStrategy} - Always disabled | 始终禁用</li>
 *   <li>{@link PercentageStrategy} - Percentage rollout | 百分比灰度</li>
 *   <li>{@link UserListStrategy} - User whitelist | 用户白名单</li>
 *   <li>{@link DateRangeStrategy} - Time-based | 基于时间</li>
 *   <li>{@link CompositeStrategy} - Combined strategies | 组合策略</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Custom strategy
 * EnableStrategy strategy = (feature, context) -> {
 *     return context.getAttribute("role", "").equals("admin");
 * };
 *
 * Feature feature = Feature.builder("admin-feature")
 *     .strategy(strategy)
 *     .build();
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@FunctionalInterface
public interface EnableStrategy {

    /**
     * Determine if the feature is enabled for the given context
     * 确定功能对于给定上下文是否启用
     *
     * @param feature the feature being evaluated | 正在评估的功能
     * @param context the evaluation context | 评估上下文
     * @return true if enabled | 如果启用返回true
     */
    boolean isEnabled(Feature feature, FeatureContext context);
}
