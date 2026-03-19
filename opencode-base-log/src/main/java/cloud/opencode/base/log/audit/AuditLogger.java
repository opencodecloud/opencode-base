package cloud.opencode.base.log.audit;

/**
 * Audit Logger Interface - Audit Event Recording
 * 审计记录器接口 - 审计事件记录
 *
 * <p>This interface defines the contract for audit logging implementations.
 * Custom implementations can persist audit events to databases, files, or
 * external systems.</p>
 * <p>此接口定义审计日志实现的契约。自定义实现可以将审计事件持久化到数据库、文件或外部系统。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class DatabaseAuditLogger implements AuditLogger {
 *     @Override
 *     public void log(AuditEvent event) {
 *         auditRepository.save(event);
 *     }
 * }
 *
 * AuditLog.setLogger(new DatabaseAuditLogger());
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pluggable audit event persistence - 可插拔的审计事件持久化</li>
 *   <li>Enable/disable toggle - 启用/禁用开关</li>
 *   <li>Lifecycle management (initialize/shutdown) - 生命周期管理（初始化/关闭）</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (event must not be null) - 空值安全: 否（事件不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public interface AuditLogger {

    /**
     * Logs an audit event.
     * 记录审计事件。
     *
     * @param event the audit event - 审计事件
     */
    void log(AuditEvent event);

    /**
     * Checks if audit logging is enabled.
     * 检查审计日志是否启用。
     *
     * @return true if enabled - 如果启用返回 true
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Initializes the audit logger.
     * 初始化审计记录器。
     */
    default void initialize() {
        // Default no-op
    }

    /**
     * Shuts down the audit logger.
     * 关闭审计记录器。
     */
    default void shutdown() {
        // Default no-op
    }
}
