package cloud.opencode.base.feature.lifecycle;

/**
 * Feature Lifecycle State
 * 功能生命周期状态
 *
 * <p>Represents the lifecycle state of a feature toggle.</p>
 * <p>表示功能开关的生命周期状态。</p>
 *
 * <p><strong>Lifecycle States | 生命周期状态:</strong></p>
 * <ul>
 *   <li>{@link #CREATED} - Newly created, not yet active | 新创建，尚未激活</li>
 *   <li>{@link #ACTIVE} - Active and in use | 活跃且在使用中</li>
 *   <li>{@link #DEPRECATED} - Deprecated, should not be used | 已弃用，不应使用</li>
 *   <li>{@link #ARCHIVED} - Archived, no longer available | 已归档，不再可用</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FeatureLifecycle state = FeatureLifecycle.ACTIVE;
 * if (state.isUsable()) {
 *     // Feature can be used
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
public enum FeatureLifecycle {

    /**
     * Newly created, not yet active
     * 新创建，尚未激活
     */
    CREATED("Newly created", "新创建"),

    /**
     * Active and in use
     * 活跃且在使用中
     */
    ACTIVE("Active", "活跃"),

    /**
     * Deprecated, should not be used
     * 已弃用，不应使用
     */
    DEPRECATED("Deprecated", "已弃用"),

    /**
     * Archived, no longer available
     * 已归档，不再可用
     */
    ARCHIVED("Archived", "已归档");

    private final String description;
    private final String descriptionZh;

    FeatureLifecycle(String description, String descriptionZh) {
        this.description = description;
        this.descriptionZh = descriptionZh;
    }

    /**
     * Get the English description
     * 获取英文描述
     *
     * @return description | 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the Chinese description
     * 获取中文描述
     *
     * @return Chinese description | 中文描述
     */
    public String getDescriptionZh() {
        return descriptionZh;
    }

    /**
     * Check if this lifecycle state allows feature usage
     * 检查此生命周期状态是否允许使用功能
     *
     * <p>Only {@link #CREATED} and {@link #ACTIVE} are considered usable.</p>
     * <p>仅 {@link #CREATED} 和 {@link #ACTIVE} 被视为可用。</p>
     *
     * @return true if usable | 如果可用返回true
     */
    public boolean isUsable() {
        return this == CREATED || this == ACTIVE;
    }
}
