package cloud.opencode.base.log.spi;

import cloud.opencode.base.log.Logger;

/**
 * Log Provider SPI Interface - Pluggable Logging Engine
 * 日志提供者 SPI 接口 - 可插拔日志引擎
 *
 * <p>This interface defines the SPI contract for logging providers.
 * Implementations adapt to specific logging frameworks like SLF4J, Log4j2, or JUL.</p>
 * <p>此接口定义日志提供者的 SPI 契约。实现适配特定的日志框架，如 SLF4J、Log4j2 或 JUL。</p>
 *
 * <p><strong>Implementation Guidelines | 实现指南:</strong></p>
 * <ul>
 *   <li>Implementations must be thread-safe - 实现必须线程安全</li>
 *   <li>isAvailable() should check for framework presence - isAvailable() 应检查框架是否存在</li>
 *   <li>Lower priority values are preferred - 较低的优先级值优先</li>
 *   <li>Provider should be registered via ServiceLoader - 提供者应通过 ServiceLoader 注册</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SPI contract for pluggable logging engines - 可插拔日志引擎的 SPI 契约</li>
 *   <li>Priority-based provider selection - 基于优先级的提供者选择</li>
 *   <li>Lifecycle management (initialize/shutdown) - 生命周期管理（初始化/关闭）</li>
 *   <li>MDC and NDC adapter provisioning - MDC 和 NDC 适配器提供</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Implement custom provider
 * public class MyLogProvider implements LogProvider {
 *     @Override
 *     public String getName() { return "MY_LOG"; }
 *     @Override
 *     public boolean isAvailable() { return true; }
 *     @Override
 *     public Logger getLogger(String name) { ... }
 *     @Override
 *     public MDCAdapter getMDCAdapter() { ... }
 * }
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public interface LogProvider {

    /**
     * Returns the name of this provider.
     * 返回此提供者的名称。
     *
     * @return the provider name (e.g., "SLF4J", "Log4j2", "JUL") - 提供者名称
     */
    String getName();

    /**
     * Returns the priority of this provider.
     * 返回此提供者的优先级。
     *
     * <p>Lower values indicate higher priority. Default is 100.</p>
     * <p>较低的值表示较高的优先级。默认为 100。</p>
     *
     * @return the priority (lower = higher priority) - 优先级（越低越优先）
     */
    default int getPriority() {
        return 100;
    }

    /**
     * Checks if this provider is available.
     * 检查此提供者是否可用。
     *
     * <p>Should check if the underlying logging framework is present on the classpath.</p>
     * <p>应检查底层日志框架是否存在于类路径中。</p>
     *
     * @return true if available - 如果可用返回 true
     */
    boolean isAvailable();

    /**
     * Gets a logger for the specified name.
     * 获取指定名称的日志记录器。
     *
     * @param name the logger name - 日志记录器名称
     * @return the logger instance - 日志记录器实例
     */
    Logger getLogger(String name);

    /**
     * Gets a logger for the specified class.
     * 获取指定类的日志记录器。
     *
     * @param clazz the class - 类
     * @return the logger instance - 日志记录器实例
     */
    default Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    /**
     * Returns the MDC adapter for this provider.
     * 返回此提供者的 MDC 适配器。
     *
     * @return the MDC adapter - MDC 适配器
     */
    MDCAdapter getMDCAdapter();

    /**
     * Returns the NDC adapter for this provider.
     * 返回此提供者的 NDC 适配器。
     *
     * @return the NDC adapter, or null if not supported - NDC 适配器，如果不支持则返回 null
     */
    default NDCAdapter getNDCAdapter() {
        return null;
    }

    /**
     * Returns the log adapter for this provider.
     * 返回此提供者的日志适配器。
     *
     * @return the log adapter - 日志适配器
     */
    default LogAdapter getLogAdapter() {
        return null;
    }

    /**
     * Initializes the provider.
     * 初始化提供者。
     *
     * <p>Called once when the provider is first loaded.</p>
     * <p>当提供者首次加载时调用一次。</p>
     */
    default void initialize() {
        // Default no-op
    }

    /**
     * Shuts down the provider.
     * 关闭提供者。
     *
     * <p>Called when the logging system is shutting down.</p>
     * <p>当日志系统关闭时调用。</p>
     */
    default void shutdown() {
        // Default no-op
    }
}
