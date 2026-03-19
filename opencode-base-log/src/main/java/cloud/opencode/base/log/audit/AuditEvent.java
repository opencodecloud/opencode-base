package cloud.opencode.base.log.audit;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Audit Event - Immutable Audit Log Entry
 * 审计事件 - 不可变的审计日志条目
 *
 * <p>This record represents an audit event containing information about
 * user actions, including who, what, when, where, and result.</p>
 * <p>此记录表示包含用户操作信息的审计事件，包括谁、什么、何时、何地和结果。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * AuditEvent event = AuditEvent.builder("USER_LOGIN")
 *     .userId("user123")
 *     .target("system")
 *     .success()
 *     .ip("192.168.1.1")
 *     .detail("browser", "Chrome")
 *     .build();
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable audit event record - 不可变的审计事件记录</li>
 *   <li>Builder pattern for flexible construction - 构建器模式，灵活构造</li>
 *   <li>Auto-generated event ID and timestamp - 自动生成事件 ID 和时间戳</li>
 *   <li>Defensive copy of details map - 详情映射的防御性拷贝</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (action must not be null) - 空值安全: 否（操作不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public record AuditEvent(
        String eventId,
        Instant timestamp,
        String userId,
        String action,
        String target,
        String targetId,
        String result,
        String ip,
        String userAgent,
        Map<String, Object> details
) {

    /**
     * Result constant for success.
     * 成功结果常量。
     */
    public static final String RESULT_SUCCESS = "SUCCESS";

    /**
     * Result constant for failure.
     * 失败结果常量。
     */
    public static final String RESULT_FAILURE = "FAILURE";

    /**
     * Canonical constructor with validation.
     * 带验证的规范构造函数。
     */
    public AuditEvent {
        Objects.requireNonNull(action, "Action must not be null");
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        if (details == null) {
            details = Map.of();
        } else {
            details = Map.copyOf(details);
        }
    }

    /**
     * Creates a builder for the specified action.
     * 为指定操作创建构建器。
     *
     * @param action the action - 操作
     * @return the builder - 构建器
     */
    public static Builder builder(String action) {
        return new Builder(action);
    }

    /**
     * Checks if the event was successful.
     * 检查事件是否成功。
     *
     * @return true if successful - 如果成功返回 true
     */
    public boolean isSuccess() {
        return RESULT_SUCCESS.equals(result);
    }

    /**
     * Builder for AuditEvent.
     * AuditEvent 的构建器。
     */
    public static final class Builder {
        private String eventId;
        private Instant timestamp;
        private String userId;
        private final String action;
        private String target;
        private String targetId;
        private String result;
        private String ip;
        private String userAgent;
        private final Map<String, Object> details = new HashMap<>();

        private Builder(String action) {
            this.action = Objects.requireNonNull(action);
        }

        /**
         * Sets the event ID.
         * 设置事件 ID。
         *
         * @param eventId the event ID - 事件 ID
         * @return this builder - 此构建器
         */
        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        /**
         * Sets the timestamp.
         * 设置时间戳。
         *
         * @param timestamp the timestamp - 时间戳
         * @return this builder - 此构建器
         */
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * Sets the user ID.
         * 设置用户 ID。
         *
         * @param userId the user ID - 用户 ID
         * @return this builder - 此构建器
         */
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Sets the target.
         * 设置目标。
         *
         * @param target the target - 目标
         * @return this builder - 此构建器
         */
        public Builder target(String target) {
            this.target = target;
            return this;
        }

        /**
         * Sets the target ID.
         * 设置目标 ID。
         *
         * @param targetId the target ID - 目标 ID
         * @return this builder - 此构建器
         */
        public Builder targetId(String targetId) {
            this.targetId = targetId;
            return this;
        }

        /**
         * Sets the result.
         * 设置结果。
         *
         * @param result the result - 结果
         * @return this builder - 此构建器
         */
        public Builder result(String result) {
            this.result = result;
            return this;
        }

        /**
         * Sets the result as success.
         * 设置结果为成功。
         *
         * @return this builder - 此构建器
         */
        public Builder success() {
            this.result = RESULT_SUCCESS;
            return this;
        }

        /**
         * Sets the result as failure.
         * 设置结果为失败。
         *
         * @return this builder - 此构建器
         */
        public Builder failure() {
            this.result = RESULT_FAILURE;
            return this;
        }

        /**
         * Sets the IP address.
         * 设置 IP 地址。
         *
         * @param ip the IP address - IP 地址
         * @return this builder - 此构建器
         */
        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        /**
         * Sets the user agent.
         * 设置用户代理。
         *
         * @param userAgent the user agent - 用户代理
         * @return this builder - 此构建器
         */
        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        /**
         * Adds a detail.
         * 添加详情。
         *
         * @param key   the detail key - 详情键
         * @param value the detail value - 详情值
         * @return this builder - 此构建器
         */
        public Builder detail(String key, Object value) {
            this.details.put(key, value);
            return this;
        }

        /**
         * Adds multiple details.
         * 添加多个详情。
         *
         * @param details the details - 详情
         * @return this builder - 此构建器
         */
        public Builder details(Map<String, Object> details) {
            this.details.putAll(details);
            return this;
        }

        /**
         * Builds the audit event.
         * 构建审计事件。
         *
         * @return the audit event - 审计事件
         */
        public AuditEvent build() {
            return new AuditEvent(eventId, timestamp, userId, action, target,
                    targetId, result, ip, userAgent, details);
        }
    }
}
