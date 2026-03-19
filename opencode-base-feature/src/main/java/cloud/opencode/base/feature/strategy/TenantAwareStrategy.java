package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;

import java.util.Map;

/**
 * Tenant Aware Strategy
 * 租户感知策略
 *
 * <p>Strategy that enables features based on tenant configuration.</p>
 * <p>基于租户配置启用功能的策略。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Per-tenant overrides - 按租户覆盖</li>
 *   <li>Multi-tenant support - 多租户支持</li>
 *   <li>Fallback strategy - 回退策略</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Enterprise tenant enabled, trial disabled
 * Feature feature = Feature.builder("premium-feature")
 *     .strategy(new TenantAwareStrategy(
 *         Map.of("enterprise", true, "trial", false),
 *         AlwaysOffStrategy.INSTANCE
 *     ))
 *     .build();
 *
 * // Check with tenant context
 * boolean enabled = feature.isEnabled(
 *     FeatureContext.builder()
 *         .tenantId("enterprise")
 *         .build()
 * );
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
public class TenantAwareStrategy implements EnableStrategy {

    private final Map<String, Boolean> tenantOverrides;
    private final EnableStrategy fallbackStrategy;

    /**
     * Create tenant aware strategy
     * 创建租户感知策略
     *
     * @param tenantOverrides  tenant-specific overrides | 租户特定覆盖
     * @param fallbackStrategy fallback when tenant not in map | 租户不在映射中时的回退
     */
    public TenantAwareStrategy(Map<String, Boolean> tenantOverrides, EnableStrategy fallbackStrategy) {
        this.tenantOverrides = tenantOverrides != null ? Map.copyOf(tenantOverrides) : Map.of();
        this.fallbackStrategy = fallbackStrategy != null ? fallbackStrategy : AlwaysOffStrategy.INSTANCE;
    }

    /**
     * Create with default fallback (always off)
     * 使用默认回退创建（始终禁用）
     *
     * @param tenantOverrides tenant-specific overrides | 租户特定覆盖
     */
    public TenantAwareStrategy(Map<String, Boolean> tenantOverrides) {
        this(tenantOverrides, AlwaysOffStrategy.INSTANCE);
    }

    /**
     * Check if enabled for tenant
     * 检查是否对租户启用
     *
     * @param feature the feature | 功能
     * @param context the context | 上下文
     * @return true if enabled for tenant | 如果对租户启用返回true
     */
    @Override
    public boolean isEnabled(Feature feature, FeatureContext context) {
        if (context.tenantId() != null && tenantOverrides.containsKey(context.tenantId())) {
            return tenantOverrides.get(context.tenantId());
        }
        return fallbackStrategy.isEnabled(feature, context);
    }

    /**
     * Get tenant overrides
     * 获取租户覆盖
     *
     * @return tenant overrides map | 租户覆盖映射
     */
    public Map<String, Boolean> getTenantOverrides() {
        return tenantOverrides;
    }

    /**
     * Get fallback strategy
     * 获取回退策略
     *
     * @return fallback strategy | 回退策略
     */
    public EnableStrategy getFallbackStrategy() {
        return fallbackStrategy;
    }

    /**
     * Check if tenant has explicit override
     * 检查租户是否有显式覆盖
     *
     * @param tenantId the tenant ID | 租户ID
     * @return true if has override | 如果有覆盖返回true
     */
    public boolean hasTenantOverride(String tenantId) {
        return tenantOverrides.containsKey(tenantId);
    }

    @Override
    public String toString() {
        return "TenantAwareStrategy{tenants=" + tenantOverrides.size() + "}";
    }
}
