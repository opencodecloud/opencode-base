package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;

/**
 * Always On Strategy
 * 始终启用策略
 *
 * <p>Strategy that always returns true (feature enabled).</p>
 * <p>始终返回true（功能启用）的策略。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Feature feature = Feature.builder("always-on")
 *     .strategy(AlwaysOnStrategy.INSTANCE)
 *     .build();
 *
 * // or using builder shortcut
 * Feature feature = Feature.builder("always-on")
 *     .alwaysOn()
 *     .build();
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unconditionally enables features - 无条件启用功能</li>
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
public class AlwaysOnStrategy implements EnableStrategy {

    /**
     * Singleton instance
     * 单例实例
     */
    public static final AlwaysOnStrategy INSTANCE = new AlwaysOnStrategy();

    /**
     * Private constructor for singleton
     * 单例私有构造函数
     */
    private AlwaysOnStrategy() {}

    /**
     * Always returns true
     * 始终返回true
     *
     * @param feature the feature | 功能
     * @param context the context | 上下文
     * @return always true | 始终为true
     */
    @Override
    public boolean isEnabled(Feature feature, FeatureContext context) {
        return true;
    }

    @Override
    public String toString() {
        return "AlwaysOnStrategy";
    }
}
