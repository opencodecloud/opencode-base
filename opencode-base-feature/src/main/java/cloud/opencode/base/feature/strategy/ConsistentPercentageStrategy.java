package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Consistent Percentage Strategy
 * 一致性哈希百分比策略
 *
 * <p>Strategy using consistent hashing to prevent users from bypassing by ID manipulation.</p>
 * <p>使用一致性哈希防止用户通过修改ID绕过的策略。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SHA-256 consistent hashing - SHA-256一致性哈希</li>
 *   <li>Salt for security - 加盐安全</li>
 *   <li>Feature-specific distribution - 按功能分布</li>
 *   <li>Strict user requirement - 严格要求用户ID</li>
 * </ul>
 *
 * <p><strong>Comparison with PercentageStrategy | 与PercentageStrategy对比:</strong></p>
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
 *   <li>Use when you need cryptographic security against user ID manipulation</li>
 *   <li>当需要防止用户ID操纵的加密安全时使用</li>
 *   <li>Use when different features should have different user distributions</li>
 *   <li>当不同功能需要不同的用户分布时使用</li>
 *   <li>Use when anonymous users should NOT be allowed</li>
 *   <li>当不允许匿名用户时使用</li>
 *   <li>Use {@link PercentageStrategy} for simpler rollouts with anonymous support</li>
 *   <li>对于支持匿名用户的简单灰度发布，使用{@link PercentageStrategy}</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // 10% with salt
 * Feature feature = Feature.builder("new-feature")
 *     .strategy(new ConsistentPercentageStrategy(10, "secret-salt"))
 *     .build();
 *
 * // Same user always gets same result
 * boolean result1 = feature.isEnabled(FeatureContext.ofUser("user1"));
 * boolean result2 = feature.isEnabled(FeatureContext.ofUser("user1"));
 * assert result1 == result2;
 *
 * // Anonymous users are NOT allowed
 * boolean anon = feature.isEnabled(FeatureContext.empty()); // Always false
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
 * @see PercentageStrategy
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public class ConsistentPercentageStrategy implements EnableStrategy {

    private static final ThreadLocal<MessageDigest> SHA256 = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    });

    private final int percentage;
    private final String salt;

    /**
     * Create consistent percentage strategy
     * 创建一致性百分比策略
     *
     * @param percentage the percentage (0-100) | 百分比 (0-100)
     * @param salt       the salt for hashing | 用于哈希的盐
     */
    public ConsistentPercentageStrategy(int percentage, String salt) {
        this.percentage = Math.max(0, Math.min(100, percentage));
        this.salt = salt != null ? salt : "";
    }

    /**
     * Create with default empty salt
     * 使用默认空盐创建
     *
     * @param percentage the percentage | 百分比
     */
    public ConsistentPercentageStrategy(int percentage) {
        this(percentage, "");
    }

    /**
     * Check if enabled using consistent hash
     * 使用一致性哈希检查是否启用
     *
     * @param feature the feature | 功能
     * @param context the context | 上下文
     * @return true if enabled | 如果启用返回true
     */
    @Override
    public boolean isEnabled(Feature feature, FeatureContext context) {
        if (context.userId() == null) {
            return false; // Require user ID for consistency
        }

        // Combine feature key + user ID + salt for unique hash
        String input = feature.key() + ":" + context.userId() + ":" + salt;
        int hash = computeHash(input);
        return hash < percentage;
    }

    /**
     * Compute consistent hash value (0-99)
     * 计算一致性哈希值 (0-99)
     *
     * @param input the input string | 输入字符串
     * @return hash value 0-99 | 哈希值 0-99
     */
    private int computeHash(String input) {
        MessageDigest md = SHA256.get();
        if (md != null) {
            md.reset();
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            // Use first 4 bytes for integer, mask sign bit to avoid Math.abs(MIN_VALUE) bug
            int value = ((hash[0] & 0xFF) << 24) |
                        ((hash[1] & 0xFF) << 16) |
                        ((hash[2] & 0xFF) << 8) |
                        (hash[3] & 0xFF);
            return (int) (Integer.toUnsignedLong(value) % 100);
        }
        // Fallback to simple hashCode
        return (int) (Integer.toUnsignedLong(input.hashCode()) % 100);
    }

    /**
     * Get the percentage
     * 获取百分比
     *
     * @return percentage | 百分比
     */
    public int getPercentage() {
        return percentage;
    }

    /**
     * Check if salt is configured
     * 检查是否配置了盐
     *
     * @return true if salt is set | 如果设置了盐返回true
     */
    public boolean hasSalt() {
        return salt != null && !salt.isEmpty();
    }

    @Override
    public String toString() {
        return "ConsistentPercentageStrategy{percentage=" + percentage + ", hasSalt=" + hasSalt() + "}";
    }
}
