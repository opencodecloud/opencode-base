package cloud.opencode.base.config;

/**
 * Configuration Change Type Enumeration
 * 配置变更类型枚举
 *
 * <p>Defines the types of configuration changes that can occur during runtime.
 * Used in conjunction with {@link ConfigChangeEvent} to track configuration modifications.</p>
 * <p>定义运行时可能发生的配置变更类型。与{@link ConfigChangeEvent}结合使用以跟踪配置修改。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ADDED - New configuration key added - 新增配置键</li>
 *   <li>MODIFIED - Existing configuration value changed - 现有配置值修改</li>
 *   <li>REMOVED - Configuration key deleted - 配置键删除</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check change type
 * ConfigChangeEvent event = ...;
 * if (event.changeType() == ConfigChangeType.ADDED) {
 *     // Handle new configuration
 * }
 *
 * // Use convenience methods in ConfigChangeEvent
 * if (event.isModified()) {
 *     String oldValue = event.oldValue();
 *     String newValue = event.newValue();
 *     // Handle modification
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum) - 线程安全: 是（枚举）</li>
 *   <li>Immutable: Yes - 不可变: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ConfigChangeEvent
 * @see ConfigListener
 * @since JDK 25, opencode-base-config V1.0.0
 */
public enum ConfigChangeType {
    /**
     * New configuration key added
     * 新增配置键
     */
    ADDED,

    /**
     * Existing configuration value changed
     * 现有配置值修改
     */
    MODIFIED,

    /**
     * Configuration key removed
     * 配置键删除
     */
    REMOVED
}
