package cloud.opencode.base.feature.security;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;
import cloud.opencode.base.feature.OpenFeature;
import cloud.opencode.base.feature.audit.FeatureAuditEvent;
import cloud.opencode.base.feature.exception.FeatureSecurityException;
import cloud.opencode.base.feature.strategy.EnableStrategy;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/**
 * Secure Feature Manager
 * 安全功能管理器
 *
 * <p>Feature manager with permission control and audit logging.</p>
 * <p>带有权限控制和审计日志的功能管理器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Permission control - 权限控制</li>
 *   <li>Admin whitelist - 管理员白名单</li>
 *   <li>Audit logging - 审计日志</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SecureFeatureManager manager = new SecureFeatureManager(
 *     Set.of("admin1", "admin2"),
 *     new FileAuditLogger(logPath)
 * );
 *
 * manager.enable("new-feature", "admin1"); // OK
 * manager.enable("new-feature", "user1");  // throws FeatureSecurityException
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
public class SecureFeatureManager {

    private final OpenFeature features;
    private final Set<String> adminUsers;
    private final AuditLogger auditLogger;

    /**
     * Create secure feature manager
     * 创建安全功能管理器
     *
     * @param adminUsers  the set of admin user IDs | 管理员用户ID集合
     * @param auditLogger the audit logger | 审计日志记录器
     */
    public SecureFeatureManager(Set<String> adminUsers, AuditLogger auditLogger) {
        this.features = OpenFeature.getInstance();
        this.adminUsers = adminUsers != null ? Set.copyOf(adminUsers) : Set.of();
        this.auditLogger = auditLogger;
    }

    /**
     * Create with custom OpenFeature instance
     * 使用自定义OpenFeature实例创建
     *
     * @param features    the OpenFeature instance | OpenFeature实例
     * @param adminUsers  the set of admin user IDs | 管理员用户ID集合
     * @param auditLogger the audit logger | 审计日志记录器
     */
    public SecureFeatureManager(OpenFeature features, Set<String> adminUsers, AuditLogger auditLogger) {
        this.features = features;
        this.adminUsers = adminUsers != null ? Set.copyOf(adminUsers) : Set.of();
        this.auditLogger = auditLogger;
    }

    /**
     * Register a feature (requires admin)
     * 注册功能（需要管理员）
     *
     * @param feature    the feature to register | 要注册的功能
     * @param operatorId the operator ID | 操作者ID
     * @throws FeatureSecurityException if not authorized | 如果未授权
     */
    public void register(Feature feature, String operatorId) {
        checkPermission(operatorId);

        features.register(feature);

        logAudit(feature.key(), operatorId, "REGISTER", false, feature.isEnabled());
    }

    /**
     * Enable a feature (requires admin)
     * 启用功能（需要管理员）
     *
     * @param featureKey the feature key | 功能键
     * @param operatorId the operator ID | 操作者ID
     * @throws FeatureSecurityException if not authorized | 如果未授权
     */
    public void enable(String featureKey, String operatorId) {
        checkPermission(operatorId);

        boolean oldValue = features.isEnabled(featureKey);
        features.enable(featureKey);
        boolean newValue = features.isEnabled(featureKey);

        logAudit(featureKey, operatorId, "ENABLE", oldValue, newValue);
    }

    /**
     * Disable a feature (requires admin)
     * 禁用功能（需要管理员）
     *
     * @param featureKey the feature key | 功能键
     * @param operatorId the operator ID | 操作者ID
     * @throws FeatureSecurityException if not authorized | 如果未授权
     */
    public void disable(String featureKey, String operatorId) {
        checkPermission(operatorId);

        boolean oldValue = features.isEnabled(featureKey);
        features.disable(featureKey);

        logAudit(featureKey, operatorId, "DISABLE", oldValue, false);
    }

    /**
     * Update feature strategy (requires admin)
     * 更新功能策略（需要管理员）
     *
     * @param featureKey the feature key | 功能键
     * @param strategy   the new strategy | 新策略
     * @param operatorId the operator ID | 操作者ID
     * @throws FeatureSecurityException if not authorized | 如果未授权
     */
    public void updateStrategy(String featureKey, EnableStrategy strategy, String operatorId) {
        checkPermission(operatorId);

        boolean oldValue = features.isEnabled(featureKey);
        features.updateStrategy(featureKey, strategy);
        boolean newValue = features.isEnabled(featureKey);

        logAudit(featureKey, operatorId, "UPDATE_STRATEGY", oldValue, newValue);
    }

    /**
     * Check if feature is enabled (no permission required)
     * 检查功能是否启用（无需权限）
     *
     * @param featureKey the feature key | 功能键
     * @return true if enabled | 如果启用返回true
     */
    public boolean isEnabled(String featureKey) {
        return features.isEnabled(featureKey);
    }

    /**
     * Check if feature is enabled for context (no permission required)
     * 检查功能对上下文是否启用（无需权限）
     *
     * @param featureKey the feature key | 功能键
     * @param context    the context | 上下文
     * @return true if enabled | 如果启用返回true
     */
    public boolean isEnabled(String featureKey, FeatureContext context) {
        return features.isEnabled(featureKey, context);
    }

    /**
     * Get a feature (no permission required)
     * 获取功能（无需权限）
     *
     * @param featureKey the feature key | 功能键
     * @return optional containing feature | 包含功能的Optional
     */
    public Optional<Feature> get(String featureKey) {
        return features.get(featureKey);
    }

    /**
     * Check if operator is admin
     * 检查操作者是否为管理员
     *
     * @param operatorId the operator ID | 操作者ID
     * @return true if admin | 如果是管理员返回true
     */
    public boolean isAdmin(String operatorId) {
        return operatorId != null && adminUsers.contains(operatorId);
    }

    /**
     * Check permission and throw if not authorized
     * 检查权限，如果未授权则抛出异常
     *
     * @param operatorId the operator ID | 操作者ID
     * @throws FeatureSecurityException if not authorized | 如果未授权
     */
    private void checkPermission(String operatorId) {
        if (!isAdmin(operatorId)) {
            throw new FeatureSecurityException("Unauthorized: " + operatorId);
        }
    }

    /**
     * Log audit event
     * 记录审计事件
     */
    private void logAudit(String featureKey, String operatorId, String action,
                          boolean oldValue, boolean newValue) {
        if (auditLogger != null) {
            auditLogger.log(new FeatureAuditEvent(
                featureKey, operatorId, action, oldValue, newValue, Instant.now()
            ));
        }
    }
}
