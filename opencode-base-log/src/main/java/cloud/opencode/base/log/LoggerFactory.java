package cloud.opencode.base.log;

import cloud.opencode.base.log.spi.LogProvider;
import cloud.opencode.base.log.spi.LogProviderFactory;

/**
 * Logger Factory - Factory for Creating Logger Instances
 * 日志工厂 - 创建日志记录器实例的工厂
 *
 * <p>This factory class provides static methods to obtain Logger instances.
 * It delegates to the underlying LogProvider for actual logger creation.</p>
 * <p>此工厂类提供获取 Logger 实例的静态方法。它委托底层 LogProvider 进行实际的日志记录器创建。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Get logger by class
 * Logger log = LoggerFactory.getLogger(MyService.class);
 *
 * // Get logger by name
 * Logger log = LoggerFactory.getLogger("com.example.MyService");
 *
 * // Auto-detect caller class
 * Logger log = LoggerFactory.getLogger();
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Create loggers by class or name - 按类或名称创建日志记录器</li>
 *   <li>Auto-detect caller class via StackWalker - 通过 StackWalker 自动检测调用类</li>
 *   <li>Pluggable LogProvider backend - 可插拔的 LogProvider 后端</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: No (throws on null class/name) - 空值安全: 否（null 类/名称抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class LoggerFactory {

    private LoggerFactory() {
        // Utility class
    }

    /**
     * Gets a logger for the specified class.
     * 获取指定类的日志记录器。
     *
     * @param clazz the class - 类
     * @return the logger instance - 日志记录器实例
     */
    public static Logger getLogger(Class<?> clazz) {
        return getProvider().getLogger(clazz);
    }

    /**
     * Gets a logger for the specified name.
     * 获取指定名称的日志记录器。
     *
     * @param name the logger name - 日志记录器名称
     * @return the logger instance - 日志记录器实例
     */
    public static Logger getLogger(String name) {
        return getProvider().getLogger(name);
    }

    /**
     * Gets a logger for the calling class.
     * 获取调用类的日志记录器。
     *
     * <p>This method automatically detects the calling class using stack walking.</p>
     * <p>此方法使用栈遍历自动检测调用类。</p>
     *
     * @return the logger instance - 日志记录器实例
     */
    public static Logger getLogger() {
        String callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(frames -> frames
                        .skip(1)
                        .map(StackWalker.StackFrame::getClassName)
                        .findFirst()
                        .orElse("UNKNOWN"));
        return getLogger(callerClass);
    }

    /**
     * Gets the current log provider.
     * 获取当前日志提供者。
     *
     * @return the log provider - 日志提供者
     */
    public static LogProvider getProvider() {
        return LogProviderFactory.getProvider();
    }

    /**
     * Sets the log provider.
     * 设置日志提供者。
     *
     * @param provider the provider to set - 要设置的提供者
     */
    public static void setProvider(LogProvider provider) {
        LogProviderFactory.setProvider(provider);
    }
}
