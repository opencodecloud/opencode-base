package cloud.opencode.base.event.handler;

import cloud.opencode.base.event.Event;


/**
 * Logging Exception Handler
 * 日志异常处理器
 *
 * <p>Default exception handler that logs errors without interrupting processing.</p>
 * <p>默认的异常处理器，记录错误但不中断处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Error logging - 错误日志记录</li>
 *   <li>Non-interrupting - 不中断处理</li>
 *   <li>Configurable log level - 可配置的日志级别</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EventExceptionHandler handler = new LoggingExceptionHandler();
 * // or with custom level
 * EventExceptionHandler handler = new LoggingExceptionHandler(System.Logger.Level.WARNING);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public class LoggingExceptionHandler implements EventExceptionHandler {

    private static final System.Logger logger = System.getLogger(LoggingExceptionHandler.class.getName());

    private final System.Logger.Level logLevel;

    /**
     * Create logging handler with default ERROR level
     * 使用默认ERROR级别创建日志处理器
     */
    public LoggingExceptionHandler() {
        this(System.Logger.Level.ERROR);
    }

    /**
     * Create logging handler with specified level
     * 使用指定级别创建日志处理器
     *
     * @param logLevel the log level | 日志级别
     */
    public LoggingExceptionHandler(System.Logger.Level logLevel) {
        this.logLevel = logLevel != null ? logLevel : System.Logger.Level.ERROR;
    }

    /**
     * Handle exception by logging it
     * 通过记录日志处理异常
     *
     * @param event        the event being processed | 正在处理的事件
     * @param exception    the exception that occurred | 发生的异常
     * @param listenerName the name of the listener that threw | 抛出异常的监听器名称
     */
    @Override
    public void handleException(Event event, Throwable exception, String listenerName) {
        String message = String.format(
            "Event listener error: event=%s, eventId=%s, listener=%s",
            event.getClass().getSimpleName(),
            event.getId(),
            listenerName
        );

        logger.log(logLevel, message, exception);
    }
}
