package cloud.opencode.base.feature;

import java.util.Set;

/**
 * Feature Group
 * 功能组
 *
 * <p>Immutable record representing a group of related features.</p>
 * <p>表示一组相关功能的不可变记录。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Group features by name - 按名称分组功能</li>
 *   <li>Check membership - 检查成员资格</li>
 *   <li>Immutable feature key set - 不可变功能键集合</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FeatureGroup group = new FeatureGroup("payment", "Payment features",
 *     Set.of("pay-v2", "pay-refund"));
 * boolean contains = group.contains("pay-v2"); // true
 * int size = group.size(); // 2
 * }</pre>
 *
 * @param name        the group name | 组名称
 * @param description the group description | 组描述
 * @param featureKeys the set of feature keys in this group | 此组中的功能键集合
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
public record FeatureGroup(String name, String description, Set<String> featureKeys) {

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造函数
     */
    public FeatureGroup {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Group name cannot be null or blank");
        }
        featureKeys = featureKeys != null ? Set.copyOf(featureKeys) : Set.of();
    }

    /**
     * Check if this group contains the given feature key
     * 检查此组是否包含给定的功能键
     *
     * @param key the feature key | 功能键
     * @return true if contains | 如果包含返回true
     */
    public boolean contains(String key) {
        return key != null && featureKeys.contains(key);
    }

    /**
     * Get the number of features in this group
     * 获取此组中的功能数量
     *
     * @return the size | 大小
     */
    public int size() {
        return featureKeys.size();
    }
}
