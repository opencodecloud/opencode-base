package cloud.opencode.base.log.audit;

import cloud.opencode.base.log.Logger;
import cloud.opencode.base.log.LoggerFactory;
import cloud.opencode.base.log.marker.Markers;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AuditLog - Audit Logging Utility
 * AuditLog - 审计日志工具
 *
 * <p>AuditLog provides convenient static methods for recording audit events
 * such as user actions, login/logout, and data changes.</p>
 * <p>AuditLog 提供方便的静态方法来记录审计事件，如用户操作、登录/注销和数据更改。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Simple logging
 * AuditLog.log("user123", "LOGIN", "system", "SUCCESS");
 *
 * // Using builder
 * AuditLog.event("UPDATE_USER")
 *     .userId("admin")
 *     .target("User")
 *     .targetId("user456")
 *     .success()
 *     .detail("field", "email")
 *     .build();  // Automatically logged
 *
 * // Record login
 * AuditLog.logLogin("user123", true, "192.168.1.1");
 *
 * // Record data change
 * AuditLog.logDataChange("admin", "Order", "ORD-001", "UPDATE", oldOrder, newOrder);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Static facade for audit logging - 审计日志的静态门面</li>
 *   <li>Pluggable AuditLogger backend - 可插拔的 AuditLogger 后端</li>
 *   <li>Convenience methods for login/logout/data change - 登录/注销/数据变更的便捷方法</li>
 *   <li>Auto-logging builder pattern - 自动记录的构建器模式</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (AtomicReference for logger swap) - 线程安全: 是（AtomicReference 用于记录器切换）</li>
 *   <li>Null-safe: Yes (null logger falls back to default) - 空值安全: 是（null 记录器回退到默认）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class AuditLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLog.class);
    private static final AtomicReference<AuditLogger> AUDIT_LOGGER =
            new AtomicReference<>(new DefaultAuditLogger());

    private AuditLog() {
        // Utility class
    }

    // ==================== Basic Logging ====================

    /**
     * Logs a simple audit event.
     * 记录简单的审计事件。
     *
     * @param userId the user ID - 用户 ID
     * @param action the action - 操作
     * @param target the target - 目标
     * @param result the result - 结果
     */
    public static void log(String userId, String action, String target, String result) {
        log(AuditEvent.builder(action)
                .userId(userId)
                .target(target)
                .result(result)
                .build());
    }

    /**
     * Logs an audit event with details.
     * 记录带详情的审计事件。
     *
     * @param userId  the user ID - 用户 ID
     * @param action  the action - 操作
     * @param target  the target - 目标
     * @param result  the result - 结果
     * @param details the details - 详情
     */
    public static void log(String userId, String action, String target, String result,
                           Map<String, Object> details) {
        log(AuditEvent.builder(action)
                .userId(userId)
                .target(target)
                .result(result)
                .details(details)
                .build());
    }

    /**
     * Logs an audit event.
     * 记录审计事件。
     *
     * @param event the audit event - 审计事件
     */
    public static void log(AuditEvent event) {
        AuditLogger logger = AUDIT_LOGGER.get();
        if (logger.isEnabled()) {
            logger.log(event);
        }
    }

    // ==================== Builder Access ====================

    /**
     * Creates an audit event builder.
     * 创建审计事件构建器。
     *
     * @param action the action - 操作
     * @return the builder - 构建器
     */
    public static LoggingBuilder event(String action) {
        return new LoggingBuilder(action);
    }

    // ==================== Convenience Methods ====================

    /**
     * Logs a login event.
     * 记录登录事件。
     *
     * @param userId  the user ID - 用户 ID
     * @param success whether login was successful - 登录是否成功
     * @param ip      the IP address - IP 地址
     */
    public static void logLogin(String userId, boolean success, String ip) {
        AuditEvent.Builder builder = AuditEvent.builder("LOGIN")
                .userId(userId)
                .target("system")
                .ip(ip);
        if (success) {
            builder.success();
        } else {
            builder.failure();
        }
        log(builder.build());
    }

    /**
     * Logs a logout event.
     * 记录注销事件。
     *
     * @param userId the user ID - 用户 ID
     */
    public static void logLogout(String userId) {
        log(AuditEvent.builder("LOGOUT")
                .userId(userId)
                .target("system")
                .success()
                .build());
    }

    /**
     * Logs a data change event.
     * 记录数据更改事件。
     *
     * @param userId   the user ID - 用户 ID
     * @param entity   the entity name - 实体名称
     * @param entityId the entity ID - 实体 ID
     * @param action   the action (CREATE/UPDATE/DELETE) - 操作
     * @param before   the value before change - 更改前的值
     * @param after    the value after change - 更改后的值
     */
    public static void logDataChange(String userId, String entity, String entityId,
                                     String action, Object before, Object after) {
        AuditEvent.Builder builder = AuditEvent.builder(action)
                .userId(userId)
                .target(entity)
                .targetId(entityId)
                .success();

        if (before != null) {
            builder.detail("before", before.toString());
        }
        if (after != null) {
            builder.detail("after", after.toString());
        }

        log(builder.build());
    }

    // ==================== Logger Configuration ====================

    /**
     * Gets the audit logger.
     * 获取审计记录器。
     *
     * @return the audit logger - 审计记录器
     */
    public static AuditLogger getLogger() {
        return AUDIT_LOGGER.get();
    }

    /**
     * Sets the audit logger.
     * 设置审计记录器。
     *
     * @param logger the audit logger - 审计记录器
     */
    public static void setLogger(AuditLogger logger) {
        AUDIT_LOGGER.set(logger != null ? logger : new DefaultAuditLogger());
    }

    // ==================== Logging Builder ====================

    /**
     * Builder that automatically logs when build() is called.
     * 调用 build() 时自动记录的构建器。
     */
    public static final class LoggingBuilder {
        private final AuditEvent.Builder delegate;

        private LoggingBuilder(String action) {
            this.delegate = AuditEvent.builder(action);
        }

        public LoggingBuilder userId(String userId) {
            delegate.userId(userId);
            return this;
        }

        public LoggingBuilder target(String target) {
            delegate.target(target);
            return this;
        }

        public LoggingBuilder targetId(String targetId) {
            delegate.targetId(targetId);
            return this;
        }

        public LoggingBuilder result(String result) {
            delegate.result(result);
            return this;
        }

        public LoggingBuilder success() {
            delegate.success();
            return this;
        }

        public LoggingBuilder failure() {
            delegate.failure();
            return this;
        }

        public LoggingBuilder ip(String ip) {
            delegate.ip(ip);
            return this;
        }

        public LoggingBuilder userAgent(String userAgent) {
            delegate.userAgent(userAgent);
            return this;
        }

        public LoggingBuilder detail(String key, Object value) {
            delegate.detail(key, value);
            return this;
        }

        public LoggingBuilder details(Map<String, Object> details) {
            delegate.details(details);
            return this;
        }

        /**
         * Builds and logs the audit event.
         * 构建并记录审计事件。
         *
         * @return the audit event - 审计事件
         */
        public AuditEvent build() {
            AuditEvent event = delegate.build();
            log(event);
            return event;
        }
    }

    // ==================== Default Logger ====================

    private static final class DefaultAuditLogger implements AuditLogger {
        @Override
        public void log(AuditEvent event) {
            LOGGER.info(Markers.AUDIT,
                    "[AUDIT] action={}, userId={}, target={}, targetId={}, result={}, ip={}, details={}",
                    event.action(),
                    event.userId(),
                    event.target(),
                    event.targetId(),
                    event.result(),
                    event.ip(),
                    event.details()
            );
        }
    }
}
