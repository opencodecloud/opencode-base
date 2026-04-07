package cloud.opencode.base.feature.audit;

import java.time.Instant;

/**
 * Feature Audit Event
 * 功能审计事件
 *
 * <p>Immutable record representing a feature audit event.</p>
 * <p>表示功能审计事件的不可变记录。</p>
 *
 * <p><strong>Actions | 操作类型:</strong></p>
 * <ul>
 *   <li>REGISTER - Feature registration | 功能注册</li>
 *   <li>ENABLE - Feature enabled | 功能启用</li>
 *   <li>DISABLE - Feature disabled | 功能禁用</li>
 *   <li>UPDATE_STRATEGY - Strategy updated | 策略更新</li>
 *   <li>DELETE - Feature deleted | 功能删除</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FeatureAuditEvent event = new FeatureAuditEvent(
 *     "dark-mode",
 *     "admin@example.com",
 *     "ENABLE",
 *     false,
 *     true,
 *     Instant.now()
 * );
 *
 * auditLogger.log(event);
 * }</pre>
 *
 * @param featureKey the feature key | 功能键
 * @param operatorId the operator ID | 操作者ID
 * @param action     the action performed | 执行的操作
 * @param oldValue   the old enabled state | 旧的启用状态
 * @param newValue   the new enabled state | 新的启用状态
 * @param timestamp  the event timestamp | 事件时间戳
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable audit event record - 不可变的审计事件记录</li>
 *   <li>Captures feature name, action, user, and timestamp - 捕获功能名称、操作、用户和时间戳</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public record FeatureAuditEvent(
    String featureKey,
    String operatorId,
    String action,
    boolean oldValue,
    boolean newValue,
    Instant timestamp
) {

    /**
     * Check if this event represents a state change
     * 检查此事件是否表示状态更改
     *
     * @return true if state changed | 如果状态更改返回true
     */
    public boolean isStateChanged() {
        return oldValue != newValue;
    }

    /**
     * Format as log string
     * 格式化为日志字符串
     *
     * @return formatted log string | 格式化的日志字符串
     */
    public String toLogString() {
        return String.format("[%s] %s: feature=%s, operator=%s, %s -> %s",
            timestamp, sanitize(action), sanitize(featureKey), sanitize(operatorId), oldValue, newValue);
    }

    private static String sanitize(String value) {
        if (value == null) {
            return "null";
        }
        return value.replace("\r", "").replace("\n", " ");
    }
}
