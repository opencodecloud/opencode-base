package cloud.opencode.base.log.spi;

import cloud.opencode.base.log.Logger;
import cloud.opencode.base.log.LogLevel;
import cloud.opencode.base.log.context.MDC;
import cloud.opencode.base.log.marker.Marker;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Default Log Provider - Console-based Fallback Provider
 * 默认日志提供者 - 基于控制台的回退提供者
 *
 * <p>This provider is used when no other logging framework is available.
 * It outputs logs to the console with basic formatting.</p>
 * <p>当没有其他日志框架可用时使用此提供者。它将日志输出到控制台，带有基本格式。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Console-based fallback logging provider - 基于控制台的回退日志提供者</li>
 *   <li>Automatic message formatting with {} placeholders - 使用 {} 占位符的自动消息格式化</li>
 *   <li>Built-in MDC and NDC adapter implementations - 内置 MDC 和 NDC 适配器实现</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap + ThreadLocal) - 线程安全: 是（ConcurrentHashMap + ThreadLocal）</li>
 *   <li>Null-safe: Yes (handles null args) - 空值安全: 是（处理 null 参数）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public class DefaultLogProvider implements LogProvider {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Map<String, Logger> LOGGERS = new ConcurrentHashMap<>();
    private final DefaultMDCAdapter mdcAdapter = new DefaultMDCAdapter();
    private final DefaultNDCAdapter ndcAdapter = new DefaultNDCAdapter();

    @Override
    public String getName() {
        return "DEFAULT";
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE; // Lowest priority
    }

    @Override
    public boolean isAvailable() {
        return true; // Always available
    }

    @Override
    public Logger getLogger(String name) {
        return LOGGERS.computeIfAbsent(name, DefaultLogger::new);
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public NDCAdapter getNDCAdapter() {
        return ndcAdapter;
    }

    // ==================== Default Logger Implementation ====================

    private static class DefaultLogger implements Logger {
        private final String name;
        private LogLevel level = LogLevel.INFO;

        DefaultLogger(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        private void doLog(LogLevel level, String message, Throwable throwable) {
            if (!isEnabled(level)) return;

            PrintStream out = level.getLevel() >= LogLevel.WARN.getLevel() ? System.err : System.out;
            String timestamp = LocalDateTime.now().format(FORMATTER);
            String threadName = Thread.currentThread().getName();
            String shortName = getShortName();

            out.printf("%s [%s] %s %s - %s%n", timestamp, threadName, level, shortName, message);
            if (throwable != null) {
                throwable.printStackTrace(out);
            }
        }

        private String getShortName() {
            int lastDot = name.lastIndexOf('.');
            return lastDot >= 0 ? name.substring(lastDot + 1) : name;
        }

        private static final int MAX_ARG_LENGTH = 1024;

        private String formatMessage(String format, Object... args) {
            if (args == null || args.length == 0) return format;
            StringBuilder sb = new StringBuilder();
            int argIndex = 0;
            int i = 0;
            while (i < format.length()) {
                if (i < format.length() - 1 && format.charAt(i) == '{' && format.charAt(i + 1) == '}') {
                    if (argIndex < args.length) {
                        sb.append(safeToString(args[argIndex++]));
                    } else {
                        sb.append("{}");
                    }
                    i += 2;
                } else {
                    sb.append(format.charAt(i));
                    i++;
                }
            }
            return sb.toString();
        }

        private static String safeToString(Object arg) {
            if (arg == null) {
                return "null";
            }
            try {
                String s = arg.toString();
                if (s.length() > MAX_ARG_LENGTH) {
                    return s.substring(0, MAX_ARG_LENGTH) + "...(truncated)";
                }
                return s;
            } catch (RuntimeException e) {
                return "[toString() error: " + e.getClass().getSimpleName() + "]";
            }
        }

        // TRACE
        @Override public boolean isTraceEnabled() { return isEnabled(LogLevel.TRACE); }
        @Override public boolean isTraceEnabled(Marker marker) { return isTraceEnabled(); }
        @Override public void trace(String message) { doLog(LogLevel.TRACE, message, null); }
        @Override public void trace(String format, Object... args) { doLog(LogLevel.TRACE, formatMessage(format, args), null); }
        @Override public void trace(String message, Throwable t) { doLog(LogLevel.TRACE, message, t); }
        @Override public void trace(Supplier<String> s) { if (isTraceEnabled()) doLog(LogLevel.TRACE, s.get(), null); }
        @Override public void trace(Marker m, String message) { doLog(LogLevel.TRACE, message, null); }
        @Override public void trace(Marker m, String format, Object... args) { doLog(LogLevel.TRACE, formatMessage(format, args), null); }

        // DEBUG
        @Override public boolean isDebugEnabled() { return isEnabled(LogLevel.DEBUG); }
        @Override public boolean isDebugEnabled(Marker marker) { return isDebugEnabled(); }
        @Override public void debug(String message) { doLog(LogLevel.DEBUG, message, null); }
        @Override public void debug(String format, Object... args) { doLog(LogLevel.DEBUG, formatMessage(format, args), null); }
        @Override public void debug(String message, Throwable t) { doLog(LogLevel.DEBUG, message, t); }
        @Override public void debug(Supplier<String> s) { if (isDebugEnabled()) doLog(LogLevel.DEBUG, s.get(), null); }
        @Override public void debug(Marker m, String message) { doLog(LogLevel.DEBUG, message, null); }
        @Override public void debug(Marker m, String format, Object... args) { doLog(LogLevel.DEBUG, formatMessage(format, args), null); }

        // INFO
        @Override public boolean isInfoEnabled() { return isEnabled(LogLevel.INFO); }
        @Override public boolean isInfoEnabled(Marker marker) { return isInfoEnabled(); }
        @Override public void info(String message) { doLog(LogLevel.INFO, message, null); }
        @Override public void info(String format, Object... args) { doLog(LogLevel.INFO, formatMessage(format, args), null); }
        @Override public void info(String message, Throwable t) { doLog(LogLevel.INFO, message, t); }
        @Override public void info(Supplier<String> s) { if (isInfoEnabled()) doLog(LogLevel.INFO, s.get(), null); }
        @Override public void info(Marker m, String message) { doLog(LogLevel.INFO, message, null); }
        @Override public void info(Marker m, String format, Object... args) { doLog(LogLevel.INFO, formatMessage(format, args), null); }

        // WARN
        @Override public boolean isWarnEnabled() { return isEnabled(LogLevel.WARN); }
        @Override public boolean isWarnEnabled(Marker marker) { return isWarnEnabled(); }
        @Override public void warn(String message) { doLog(LogLevel.WARN, message, null); }
        @Override public void warn(String format, Object... args) { doLog(LogLevel.WARN, formatMessage(format, args), null); }
        @Override public void warn(String message, Throwable t) { doLog(LogLevel.WARN, message, t); }
        @Override public void warn(Supplier<String> s) { if (isWarnEnabled()) doLog(LogLevel.WARN, s.get(), null); }
        @Override public void warn(Marker m, String message) { doLog(LogLevel.WARN, message, null); }
        @Override public void warn(Marker m, String format, Object... args) { doLog(LogLevel.WARN, formatMessage(format, args), null); }
        @Override public void warn(Marker m, String message, Throwable t) { doLog(LogLevel.WARN, message, t); }

        // ERROR
        @Override public boolean isErrorEnabled() { return isEnabled(LogLevel.ERROR); }
        @Override public boolean isErrorEnabled(Marker marker) { return isErrorEnabled(); }
        @Override public void error(String message) { doLog(LogLevel.ERROR, message, null); }
        @Override public void error(String format, Object... args) { doLog(LogLevel.ERROR, formatMessage(format, args), null); }
        @Override public void error(String message, Throwable t) { doLog(LogLevel.ERROR, message, t); }
        @Override public void error(Throwable t) { doLog(LogLevel.ERROR, t != null ? (t.getMessage() != null ? t.getMessage() : t.getClass().getName()) : "null", t); }
        @Override public void error(Supplier<String> s) { if (isErrorEnabled()) doLog(LogLevel.ERROR, s.get(), null); }
        @Override public void error(Supplier<String> s, Throwable t) { if (isErrorEnabled()) doLog(LogLevel.ERROR, s.get(), t); }
        @Override public void error(Marker m, String message) { doLog(LogLevel.ERROR, message, null); }
        @Override public void error(Marker m, String format, Object... args) { doLog(LogLevel.ERROR, formatMessage(format, args), null); }
        @Override public void error(Marker m, String message, Throwable t) { doLog(LogLevel.ERROR, message, t); }

        // Generic
        @Override public boolean isEnabled(LogLevel level) { return level.isGreaterOrEqual(this.level); }
        @Override public void log(LogLevel level, String message) { doLog(level, message, null); }
        @Override public void log(LogLevel level, String format, Object... args) { doLog(level, formatMessage(format, args), null); }
        @Override public void log(LogLevel level, String message, Throwable t) { doLog(level, message, t); }
    }

    // ==================== Default MDC Adapter ====================

    /**
     * Default MDC (Mapped Diagnostic Context) adapter using ThreadLocal storage.
     * 使用 ThreadLocal 存储的默认 MDC（映射诊断上下文）适配器。
     *
     * <p><strong>WARNING | 警告:</strong> In thread pool or virtual thread environments,
     * MDC entries are not automatically cleared when a thread is reused. Callers MUST invoke
     * {@link #clear()} at task boundaries (e.g., at the end of request processing) to prevent
     * memory leaks and context leakage between tasks. Use {@link #wrapRunnable(Runnable)} to
     * automatically clear MDC context after task execution.</p>
     * <p>在线程池或虚拟线程环境中，当线程被复用时 MDC 条目不会自动清理。调用者必须在任务边界
     * （如请求处理结束时）调用 {@link #clear()} 以防止内存泄漏和上下文泄漏。
     * 使用 {@link #wrapRunnable(Runnable)} 可在任务执行后自动清理 MDC 上下文。</p>
     */
    private static class DefaultMDCAdapter implements MDCAdapter {
        private final ThreadLocal<Map<String, String>> context = ThreadLocal.withInitial(HashMap::new);

        @Override
        public void put(String key, String value) {
            context.get().put(key, value);
        }

        @Override
        public String get(String key) {
            return context.get().get(key);
        }

        @Override
        public void remove(String key) {
            context.get().remove(key);
        }

        /**
         * Clears all entries from the MDC for the current thread.
         * 清空当前线程 MDC 中的所有条目。
         *
         * <p><strong>Important | 重要:</strong> This method MUST be called at task boundaries
         * in thread pool or virtual thread environments to prevent memory leaks and context
         * leakage between tasks.</p>
         * <p>在线程池或虚拟线程环境中，必须在任务边界调用此方法以防止内存泄漏和上下文泄漏。</p>
         */
        @Override
        public void clear() {
            context.get().clear();
        }

        @Override
        public Map<String, String> getCopyOfContextMap() {
            return new HashMap<>(context.get());
        }

        @Override
        public void setContextMap(Map<String, String> contextMap) {
            context.get().clear();
            if (contextMap != null) {
                context.get().putAll(contextMap);
            }
        }
    }

    /**
     * Wraps a {@link Runnable} to automatically clear MDC context after execution.
     * 包装 {@link Runnable} 以在执行后自动清理 MDC 上下文。
     *
     * <p>Use this when submitting tasks to thread pools or virtual thread executors
     * to prevent MDC context leakage between tasks.</p>
     * <p>在向线程池或虚拟线程执行器提交任务时使用此方法，以防止任务之间的 MDC 上下文泄漏。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * executor.submit(DefaultLogProvider.wrapRunnable(() -> {
     *     MDC.put("requestId", id);
     *     // ... task logic ...
     * }));
     * }</pre>
     *
     * @param task the original runnable - 原始 Runnable
     * @return a wrapped runnable that clears MDC in a finally block - 在 finally 块中清理 MDC 的包装 Runnable
     */
    public static Runnable wrapRunnable(Runnable task) {
        return () -> {
            try {
                task.run();
            } finally {
                MDC.clear();
            }
        };
    }

    // ==================== Default NDC Adapter ====================

    private static class DefaultNDCAdapter implements NDCAdapter {
        private final ThreadLocal<Deque<String>> stack = ThreadLocal.withInitial(ArrayDeque::new);
        private volatile int maxDepth = 100;

        @Override
        public void push(String message) {
            Deque<String> s = stack.get();
            if (s.size() < maxDepth) {
                s.push(message);
            }
        }

        @Override
        public String pop() {
            return stack.get().poll();
        }

        @Override
        public String peek() {
            return stack.get().peek();
        }

        @Override
        public void clear() {
            stack.get().clear();
        }

        @Override
        public int getDepth() {
            return stack.get().size();
        }

        @Override
        public void setMaxDepth(int maxDepth) {
            if (maxDepth < 1) {
                throw new IllegalArgumentException("maxDepth must be >= 1, got: " + maxDepth);
            }
            this.maxDepth = maxDepth;
        }

        @Override
        public Deque<String> getCopyOfStack() {
            return new ArrayDeque<>(stack.get());
        }

        @Override
        public void setStack(Deque<String> newStack) {
            Deque<String> s = stack.get();
            s.clear();
            if (newStack != null) {
                s.addAll(newStack);
            }
        }
    }
}
