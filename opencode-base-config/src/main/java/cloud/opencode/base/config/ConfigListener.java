package cloud.opencode.base.config;

/**
 * Configuration Change Listener
 * 配置变更监听器
 *
 * <p>Functional interface for listening to configuration changes. Implementations can react
 * to configuration modifications, additions, and removals.</p>
 * <p>用于监听配置变更的函数式接口。实现类可以响应配置的修改、添加和删除。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Functional interface for configuration change callbacks - 配置变更回调的函数式接口</li>
 *   <li>Supports lambda expressions - 支持Lambda表达式</li>
 *   <li>Asynchronous notification support - 支持异步通知</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Lambda listener
 * config.addListener(event -> {
 *     System.out.println("Config changed: " + event.key());
 * });
 *
 * // Method reference
 * config.addListener(this::handleConfigChange);
 *
 * // Specific key listener
 * config.addListener("log.level", event -> {
 *     updateLogLevel(event.newValue());
 * });
 *
 * // Multi-event handler
 * config.addListener(event -> {
 *     if (event.isModified() && event.key().startsWith("database.")) {
 *         refreshDataSource();
 *     }
 * });
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Listener execution may be async - 监听器执行可能是异步的</li>
 *   <li>Keep listener logic lightweight - 保持监听器逻辑轻量级</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Exception handling: Exceptions are caught and logged - 异常处理: 异常会被捕获并记录</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@FunctionalInterface
public interface ConfigListener {

    /**
     * Handle configuration change event
     * 处理配置变更事件
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * // Print change
     * event -> System.out.println(event)
     *
     * // Update component
     * event -> component.refresh()
     *
     * // Conditional handling
     * event -> {
     *     if (event.isModified()) {
     *         handleModification(event);
     *     }
     * }
     * }</pre>
     *
     * @param event configuration change event | 配置变更事件
     */
    void onConfigChange(ConfigChangeEvent event);
}
