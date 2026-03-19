package cloud.opencode.base.log.context;

import cloud.opencode.base.log.spi.LogProviderFactory;
import cloud.opencode.base.log.spi.MDCAdapter;

import java.util.Map;
import java.util.function.Supplier;

/**
 * MDC - Mapped Diagnostic Context
 * MDC - 映射诊断上下文
 *
 * <p>MDC provides a way to enrich log messages with contextual information
 * that is specific to the current thread of execution.</p>
 * <p>MDC 提供一种方式，用当前执行线程特定的上下文信息丰富日志消息。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Set context values
 * MDC.put("requestId", "req-12345");
 * MDC.put("userId", "user-001");
 *
 * // Use scope for automatic cleanup
 * try (MDCScope scope = MDC.scope("orderId", "ORD-001")) {
 *     OpenLog.info("Processing order");  // Includes orderId
 * }
 * // orderId automatically removed
 *
 * // Run with context
 * MDC.runWith("taskId", "task-001", () -> {
 *     OpenLog.info("Executing task");
 * });
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-local mapped diagnostic context - 线程本地映射诊断上下文</li>
 *   <li>Scope-based auto-cleanup (try-with-resources) - 基于作用域的自动清理（try-with-resources）</li>
 *   <li>Temporary value binding via runWith/callWith - 通过 runWith/callWith 临时值绑定</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ThreadLocal-based) - 线程安全: 是（基于 ThreadLocal）</li>
 *   <li>Null-safe: Yes (returns null for missing keys) - 空值安全: 是（缺少的键返回 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class MDC {

    private MDC() {
        // Utility class
    }

    /**
     * Puts a key-value pair into the MDC.
     * 将键值对放入 MDC。
     *
     * @param key   the key - 键
     * @param value the value - 值
     */
    public static void put(String key, String value) {
        getAdapter().put(key, value);
    }

    /**
     * Gets a value from the MDC.
     * 从 MDC 获取值。
     *
     * @param key the key - 键
     * @return the value, or null if not found - 值，如果未找到则返回 null
     */
    public static String get(String key) {
        return getAdapter().get(key);
    }

    /**
     * Removes a key from the MDC.
     * 从 MDC 移除键。
     *
     * @param key the key to remove - 要移除的键
     */
    public static void remove(String key) {
        getAdapter().remove(key);
    }

    /**
     * Clears all entries from the MDC.
     * 清空 MDC 中的所有条目。
     */
    public static void clear() {
        getAdapter().clear();
    }

    /**
     * Returns a copy of the current context map.
     * 返回当前上下文映射的副本。
     *
     * @return a copy of the context map - 上下文映射的副本
     */
    public static Map<String, String> getCopyOfContextMap() {
        return getAdapter().getCopyOfContextMap();
    }

    /**
     * Sets the context map.
     * 设置上下文映射。
     *
     * @param contextMap the context map - 上下文映射
     */
    public static void setContextMap(Map<String, String> contextMap) {
        getAdapter().setContextMap(contextMap);
    }

    /**
     * Executes a task with a temporary MDC value.
     * 使用临时 MDC 值执行任务。
     *
     * @param key      the key - 键
     * @param value    the value - 值
     * @param runnable the task - 任务
     */
    public static void runWith(String key, String value, Runnable runnable) {
        String previous = get(key);
        try {
            put(key, value);
            runnable.run();
        } finally {
            if (previous == null) {
                remove(key);
            } else {
                put(key, previous);
            }
        }
    }

    /**
     * Executes a task with a temporary MDC value and returns the result.
     * 使用临时 MDC 值执行任务并返回结果。
     *
     * @param key      the key - 键
     * @param value    the value - 值
     * @param supplier the task - 任务
     * @param <T>      the result type - 结果类型
     * @return the result - 结果
     */
    public static <T> T callWith(String key, String value, Supplier<T> supplier) {
        String previous = get(key);
        try {
            put(key, value);
            return supplier.get();
        } finally {
            if (previous == null) {
                remove(key);
            } else {
                put(key, previous);
            }
        }
    }

    /**
     * Creates an MDC scope for automatic cleanup.
     * 创建用于自动清理的 MDC 作用域。
     *
     * @param key   the key - 键
     * @param value the value - 值
     * @return the scope - 作用域
     */
    public static MDCScope scope(String key, String value) {
        String previous = get(key);
        put(key, value);
        return new MDCScope(key, previous);
    }

    /**
     * Creates an MDC scope with multiple values.
     * 创建具有多个值的 MDC 作用域。
     *
     * @param values the key-value pairs - 键值对
     * @return the scope - 作用域
     */
    public static MDCScope scope(Map<String, String> values) {
        Map<String, String> previous = getCopyOfContextMap();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
        return new MDCScope(previous);
    }

    private static MDCAdapter getAdapter() {
        return LogProviderFactory.getProvider().getMDCAdapter();
    }

    /**
     * MDC Scope - AutoCloseable for automatic cleanup.
     * MDC 作用域 - 用于自动清理的 AutoCloseable。
     */
    public static final class MDCScope implements AutoCloseable {
        private final String key;
        private final String previousValue;
        private final Map<String, String> previousMap;

        MDCScope(String key, String previousValue) {
            this.key = key;
            this.previousValue = previousValue;
            this.previousMap = null;
        }

        MDCScope(Map<String, String> previousMap) {
            this.key = null;
            this.previousValue = null;
            this.previousMap = previousMap;
        }

        @Override
        public void close() {
            if (previousMap != null) {
                setContextMap(previousMap);
            } else if (key != null) {
                if (previousValue == null) {
                    remove(key);
                } else {
                    put(key, previousValue);
                }
            }
        }
    }
}
