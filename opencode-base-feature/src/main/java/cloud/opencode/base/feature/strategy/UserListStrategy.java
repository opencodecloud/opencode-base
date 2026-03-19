package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;

import java.util.Set;

/**
 * User List Strategy
 * 用户列表策略
 *
 * <p>Strategy that enables feature for specific users.</p>
 * <p>为特定用户启用功能的策略。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>User whitelist - 用户白名单</li>
 *   <li>Beta user targeting - Beta用户定向</li>
 *   <li>VIP user features - VIP用户功能</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Using builder shortcut
 * Feature feature = Feature.builder("beta-feature")
 *     .forUsers("user1", "user2", "user3")
 *     .build();
 *
 * // Using strategy directly
 * Feature feature = Feature.builder("vip-feature")
 *     .strategy(new UserListStrategy(Set.of("vip1", "vip2")))
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
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public class UserListStrategy implements EnableStrategy {

    private final Set<String> allowedUsers;

    /**
     * Create user list strategy
     * 创建用户列表策略
     *
     * @param allowedUsers the set of allowed user IDs | 允许的用户ID集合
     */
    public UserListStrategy(Set<String> allowedUsers) {
        this.allowedUsers = allowedUsers != null ? Set.copyOf(allowedUsers) : Set.of();
    }

    /**
     * Check if user is in the allowed list
     * 检查用户是否在允许列表中
     *
     * @param feature the feature | 功能
     * @param context the context | 上下文
     * @return true if user is allowed | 如果用户被允许返回true
     */
    @Override
    public boolean isEnabled(Feature feature, FeatureContext context) {
        return context.userId() != null && allowedUsers.contains(context.userId());
    }

    /**
     * Get the set of allowed users
     * 获取允许的用户集合
     *
     * @return allowed users | 允许的用户
     */
    public Set<String> getAllowedUsers() {
        return allowedUsers;
    }

    /**
     * Check if a specific user is allowed
     * 检查特定用户是否被允许
     *
     * @param userId the user ID | 用户ID
     * @return true if allowed | 如果允许返回true
     */
    public boolean isUserAllowed(String userId) {
        return userId != null && allowedUsers.contains(userId);
    }

    @Override
    public String toString() {
        return "UserListStrategy{allowedUsers=" + allowedUsers.size() + " users}";
    }
}
