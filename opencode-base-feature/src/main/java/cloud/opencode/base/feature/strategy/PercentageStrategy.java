package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;

import java.security.SecureRandom;

/**
 * Percentage Strategy
 * 百分比策略
 *
 * <p>Strategy that enables feature for a percentage of requests.</p>
 * <p>为一定百分比的请求启用功能的策略。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Percentage-based rollout - 基于百分比的灰度发布</li>
 *   <li>Consistent results for same user (via hashCode) - 同一用户结果一致（通过hashCode）</li>
 *   <li>Random for anonymous requests - 匿名请求随机</li>
 * </ul>
 *
 * <p><strong>Comparison with ConsistentPercentageStrategy | 与ConsistentPercentageStrategy对比:</strong></p>
 * <table border="1">
 *   <tr><th>Aspect</th><th>PercentageStrategy</th><th>ConsistentPercentageStrategy</th></tr>
 *   <tr><td>Hash algorithm</td><td>hashCode()</td><td>SHA-256</td></tr>
 *   <tr><td>No userId</td><td>Random</td><td>Returns false</td></tr>
 *   <tr><td>Feature-specific</td><td>No</td><td>Yes (includes feature key)</td></tr>
 *   <tr><td>Salt support</td><td>No</td><td>Yes</td></tr>
 * </table>
 *
 * <p><strong>When to use | 何时使用:</strong></p>
 * <ul>
 *   <li>Use this strategy for simple rollouts where anonymous users should be included</li>
 *   <li>使用此策略进行简单的灰度发布，允许匿名用户参与</li>
 *   <li>Use {@link ConsistentPercentageStrategy} when you need strict user tracking or cryptographic security</li>
 *   <li>当需要严格的用户跟踪或加密安全时，使用{@link ConsistentPercentageStrategy}</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // 10% rollout
 * Feature feature = Feature.builder("new-feature")
 *     .percentage(10)
 *     .build();
 *
 * // or directly
 * Feature feature = Feature.builder("new-feature")
 *     .strategy(new PercentageStrategy(10))
 *     .build();
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see ConsistentPercentageStrategy
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public class PercentageStrategy implements EnableStrategy {

    private final int percentage;
    private final SecureRandom random;

    /**
     * Create percentage strategy
     * 创建百分比策略
     *
     * @param percentage the percentage (0-100) | 百分比 (0-100)
     */
    public PercentageStrategy(int percentage) {
        this.percentage = Math.max(0, Math.min(100, percentage));
        this.random = new SecureRandom();
    }

    /**
     * Check if enabled based on percentage
     * 基于百分比检查是否启用
     *
     * <p>If userId is present, uses consistent hashing to ensure
     * the same user always gets the same result.</p>
     * <p>如果存在userId，使用一致性哈希确保同一用户始终得到相同结果。</p>
     *
     * @param feature the feature | 功能
     * @param context the context | 上下文
     * @return true if enabled | 如果启用返回true
     */
    @Override
    public boolean isEnabled(Feature feature, FeatureContext context) {
        // If user ID is present, use consistent hashing
        if (context.userId() != null) {
            int hash = (context.userId().hashCode() & Integer.MAX_VALUE) % 100;
            return hash < percentage;
        }
        // Otherwise use random
        return random.nextInt(100) < percentage;
    }

    /**
     * Get the percentage value
     * 获取百分比值
     *
     * @return percentage | 百分比
     */
    public int getPercentage() {
        return percentage;
    }

    @Override
    public String toString() {
        return "PercentageStrategy{percentage=" + percentage + "}";
    }
}
