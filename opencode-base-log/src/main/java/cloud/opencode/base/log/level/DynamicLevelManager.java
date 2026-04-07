package cloud.opencode.base.log.level;

import cloud.opencode.base.log.LogLevel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dynamic Level Manager - Runtime log level override management
 * 动态级别管理器 - 运行时日志级别覆盖管理
 *
 * <p>Singleton that manages runtime log level overrides per logger name,
 * enabling dynamic adjustment of logging verbosity without application restart.</p>
 * <p>单例模式，按日志记录器名称管理运行时日志级别覆盖，
 * 支持在不重启应用的情况下动态调整日志详细程度。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Per-logger level overrides - 按日志记录器覆盖级别</li>
 *   <li>Effective level resolution with default fallback - 有效级别解析，带默认回退</li>
 *   <li>Thread-safe concurrent access - 线程安全的并发访问</li>
 *   <li>Bulk reset capability - 批量重置功能</li>
 *   <li>Lazy singleton initialization - 延迟单例初始化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DynamicLevelManager manager = DynamicLevelManager.getInstance();
 *
 * // Set a level override for a specific logger
 * manager.setLevel("com.example.MyService", LogLevel.DEBUG);
 *
 * // Get effective level (override or default)
 * LogLevel effective = manager.getEffectiveLevel("com.example.MyService", LogLevel.INFO);
 * // Returns DEBUG (override exists)
 *
 * // Reset a specific override
 * manager.resetLevel("com.example.MyService");
 *
 * // Reset all overrides
 * manager.resetAll();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap-based) - 线程安全: 是（基于 ConcurrentHashMap）</li>
 *   <li>Null-safe: No (logger name and level must not be null) - 空值安全: 否（日志记录器名称和级别不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
public final class DynamicLevelManager {

    private final ConcurrentHashMap<String, LogLevel> levelOverrides;

    /**
     * Private constructor for singleton pattern.
     * 单例模式的私有构造函数。
     */
    private DynamicLevelManager() {
        this.levelOverrides = new ConcurrentHashMap<>();
    }

    /**
     * Returns the singleton instance of DynamicLevelManager.
     * 返回 DynamicLevelManager 的单例实例。
     *
     * <p>Uses the initialization-on-demand holder idiom for lazy, thread-safe
     * singleton initialization without synchronization.</p>
     * <p>使用按需初始化持有者惯用法实现延迟、线程安全的单例初始化，无需同步。</p>
     *
     * @return the singleton instance - 单例实例
     */
    public static DynamicLevelManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Sets a log level override for the specified logger name.
     * 为指定的日志记录器名称设置日志级别覆盖。
     *
     * @param loggerName the logger name - 日志记录器名称
     * @param level      the log level override - 日志级别覆盖
     * @throws NullPointerException if loggerName or level is null - 如果日志记录器名称或级别为 null
     */
    public void setLevel(String loggerName, LogLevel level) {
        Objects.requireNonNull(loggerName, "Logger name must not be null");
        Objects.requireNonNull(level, "Log level must not be null");
        levelOverrides.put(loggerName, level);
    }

    /**
     * Gets the log level override for the specified logger name.
     * 获取指定日志记录器名称的日志级别覆盖。
     *
     * @param loggerName the logger name - 日志记录器名称
     * @return the log level override, or null if not set - 日志级别覆盖，如果未设置则为 null
     * @throws NullPointerException if loggerName is null - 如果日志记录器名称为 null
     */
    public LogLevel getLevel(String loggerName) {
        Objects.requireNonNull(loggerName, "Logger name must not be null");
        return levelOverrides.get(loggerName);
    }

    /**
     * Gets the effective log level for the specified logger name.
     * 获取指定日志记录器名称的有效日志级别。
     *
     * <p>Returns the override level if set, otherwise returns the default level.</p>
     * <p>如果设置了覆盖级别则返回覆盖级别，否则返回默认级别。</p>
     *
     * @param loggerName   the logger name - 日志记录器名称
     * @param defaultLevel the default level to use if no override exists - 如果没有覆盖则使用的默认级别
     * @return the effective log level - 有效日志级别
     * @throws NullPointerException if loggerName or defaultLevel is null - 如果日志记录器名称或默认级别为 null
     */
    public LogLevel getEffectiveLevel(String loggerName, LogLevel defaultLevel) {
        Objects.requireNonNull(loggerName, "Logger name must not be null");
        Objects.requireNonNull(defaultLevel, "Default level must not be null");
        return levelOverrides.getOrDefault(loggerName, defaultLevel);
    }

    /**
     * Removes the log level override for the specified logger name.
     * 移除指定日志记录器名称的日志级别覆盖。
     *
     * @param loggerName the logger name - 日志记录器名称
     * @throws NullPointerException if loggerName is null - 如果日志记录器名称为 null
     */
    public void resetLevel(String loggerName) {
        Objects.requireNonNull(loggerName, "Logger name must not be null");
        levelOverrides.remove(loggerName);
    }

    /**
     * Clears all log level overrides.
     * 清除所有日志级别覆盖。
     */
    public void resetAll() {
        levelOverrides.clear();
    }

    /**
     * Returns an unmodifiable copy of all current log level overrides.
     * 返回所有当前日志级别覆盖的不可修改副本。
     *
     * @return unmodifiable map of logger names to log levels - 日志记录器名称到日志级别的不可修改映射
     */
    public Map<String, LogLevel> getAllLevels() {
        return Collections.unmodifiableMap(new HashMap<>(levelOverrides));
    }

    /**
     * Checks if a log level override exists for the specified logger name.
     * 检查指定日志记录器名称是否存在日志级别覆盖。
     *
     * @param loggerName the logger name - 日志记录器名称
     * @return true if an override exists - 如果存在覆盖则返回 true
     * @throws NullPointerException if loggerName is null - 如果日志记录器名称为 null
     */
    public boolean hasOverride(String loggerName) {
        Objects.requireNonNull(loggerName, "Logger name must not be null");
        return levelOverrides.containsKey(loggerName);
    }

    /**
     * Returns the number of active log level overrides.
     * 返回活跃的日志级别覆盖数量。
     *
     * @return the number of overrides - 覆盖数量
     */
    public int getOverrideCount() {
        return levelOverrides.size();
    }

    /**
     * Holder class for lazy singleton initialization.
     * 用于延迟单例初始化的持有者类。
     */
    private static final class Holder {
        private static final DynamicLevelManager INSTANCE = new DynamicLevelManager();
    }
}
