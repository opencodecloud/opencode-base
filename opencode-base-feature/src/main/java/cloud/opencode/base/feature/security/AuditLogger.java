package cloud.opencode.base.feature.security;

import cloud.opencode.base.feature.audit.FeatureAuditEvent;

/**
 * Audit Logger Interface
 * 审计日志接口
 *
 * <p>Interface for logging feature audit events.</p>
 * <p>用于记录功能审计事件的接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Audit event logging - 审计事件记录</li>
 *   <li>Change tracking - 变更跟踪</li>
 *   <li>Compliance support - 合规支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AuditLogger logger = new FileAuditLogger(logPath);
 * logger.log(new FeatureAuditEvent(
 *     "feature-key", "admin", "ENABLE", false, true, Instant.now()
 * ));
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public interface AuditLogger {

    /**
     * Log an audit event
     * 记录审计事件
     *
     * @param event the audit event | 审计事件
     */
    void log(FeatureAuditEvent event);
}
