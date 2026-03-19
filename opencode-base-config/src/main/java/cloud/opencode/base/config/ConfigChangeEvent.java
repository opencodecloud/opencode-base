package cloud.opencode.base.config;

import java.time.Instant;

/**
 * Configuration Change Event
 * 配置变更事件
 *
 * <p>Represents a configuration change event, including the key, old value, new value,
 * change type, and timestamp.</p>
 * <p>表示配置变更事件,包括键、旧值、新值、变更类型和时间戳。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configuration change tracking - 配置变更跟踪</li>
 *   <li>Change type identification (ADDED/MODIFIED/REMOVED) - 变更类型识别</li>
 *   <li>Timestamp recording - 时间戳记录</li>
 *   <li>Factory methods for creating events - 创建事件的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create events
 * ConfigChangeEvent added = ConfigChangeEvent.added("new.key", "value");
 * ConfigChangeEvent modified = ConfigChangeEvent.modified("key", "old", "new");
 * ConfigChangeEvent removed = ConfigChangeEvent.removed("old.key", "value");
 *
 * // Use in listener
 * config.addListener(event -> {
 *     if (event.isModified()) {
 *         System.out.println(event.key() + " changed: " +
 *                          event.oldValue() + " -> " + event.newValue());
 *     }
 * });
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for all operations - 时间复杂度: 所有操作为O(1)</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 *   <li>Immutable and lightweight - 不可变且轻量级</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable: Yes - 不可变: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 *
 * @param key configuration key | 配置键
 * @param oldValue old value | 旧值
 * @param newValue new value | 新值
 * @param changeType change type | 变更类型
 * @param timestamp event timestamp | 事件时间戳
 */
public record ConfigChangeEvent(
        String key,
        String oldValue,
        String newValue,
        ChangeType changeType,
        Instant timestamp
) {

    /**
     * Configuration Change Type
     * 配置变更类型
     */
    public enum ChangeType {
        /** Configuration added | 配置添加 */
        ADDED,
        /** Configuration modified | 配置修改 */
        MODIFIED,
        /** Configuration removed | 配置删除 */
        REMOVED
    }

    // ==================== Convenience Methods | 便捷方法 ====================

    /**
     * Check if configuration was added
     * 检查是否为配置添加
     *
     * @return true if added | 如果是添加返回true
     */
    public boolean isAdded() {
        return changeType == ChangeType.ADDED;
    }

    /**
     * Check if configuration was modified
     * 检查是否为配置修改
     *
     * @return true if modified | 如果是修改返回true
     */
    public boolean isModified() {
        return changeType == ChangeType.MODIFIED;
    }

    /**
     * Check if configuration was removed
     * 检查是否为配置删除
     *
     * @return true if removed | 如果是删除返回true
     */
    public boolean isRemoved() {
        return changeType == ChangeType.REMOVED;
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Create configuration added event
     * 创建配置添加事件
     *
     * @param key configuration key | 配置键
     * @param newValue new value | 新值
     * @return change event | 变更事件
     */
    public static ConfigChangeEvent added(String key, String newValue) {
        return new ConfigChangeEvent(key, null, newValue, ChangeType.ADDED, Instant.now());
    }

    /**
     * Create configuration modified event
     * 创建配置修改事件
     *
     * @param key configuration key | 配置键
     * @param oldValue old value | 旧值
     * @param newValue new value | 新值
     * @return change event | 变更事件
     */
    public static ConfigChangeEvent modified(String key, String oldValue, String newValue) {
        return new ConfigChangeEvent(key, oldValue, newValue, ChangeType.MODIFIED, Instant.now());
    }

    /**
     * Create configuration removed event
     * 创建配置删除事件
     *
     * @param key configuration key | 配置键
     * @param oldValue old value | 旧值
     * @return change event | 变更事件
     */
    public static ConfigChangeEvent removed(String key, String oldValue) {
        return new ConfigChangeEvent(key, oldValue, null, ChangeType.REMOVED, Instant.now());
    }

    // ==================== Object Methods | 对象方法 ====================

    @Override
    public String toString() {
        return "ConfigChangeEvent{" +
                "key='" + key + '\'' +
                ", oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
                ", changeType=" + changeType +
                ", timestamp=" + timestamp +
                '}';
    }
}
