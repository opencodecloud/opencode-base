package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;

/**
 * Always Off Strategy
 * 始终禁用策略
 *
 * <p>Strategy that always returns false (feature disabled).</p>
 * <p>始终返回false（功能禁用）的策略。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Feature feature = Feature.builder("always-off")
 *     .strategy(AlwaysOffStrategy.INSTANCE)
 *     .build();
 *
 * // or using builder shortcut
 * Feature feature = Feature.builder("always-off")
 *     .alwaysOff()
 *     .build();
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unconditionally disables features - 无条件禁用功能</li>
 *   <li>Zero-overhead evaluation - 零开销评估</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public class AlwaysOffStrategy implements EnableStrategy {

    /**
     * Singleton instance
     * 单例实例
     */
    public static final AlwaysOffStrategy INSTANCE = new AlwaysOffStrategy();

    /**
     * Private constructor for singleton
     * 单例私有构造函数
     */
    private AlwaysOffStrategy() {}

    /**
     * Always returns false
     * 始终返回false
     *
     * @param feature the feature | 功能
     * @param context the context | 上下文
     * @return always false | 始终为false
     */
    @Override
    public boolean isEnabled(Feature feature, FeatureContext context) {
        return false;
    }

    @Override
    public String toString() {
        return "AlwaysOffStrategy";
    }
}
