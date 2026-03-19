/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.log.enhance;

import cloud.opencode.base.log.Logger;
import cloud.opencode.base.log.LoggerFactory;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Conditional Logging - Environment and Rate-based Logging
 * 条件日志 - 基于环境和速率的日志
 *
 * <p>Provides conditional logging capabilities including:</p>
 * <p>提供条件日志能力，包括：</p>
 * <ul>
 *   <li>Environment-based logging (dev/test/prod) - 基于环境的日志</li>
 *   <li>Condition-based logging - 基于条件的日志</li>
 *   <li>Once-only logging (per call site) - 仅一次日志（每个调用点）</li>
 *   <li>Rate-limited logging - 限速日志</li>
 * </ul>
 *
 * <p><strong>Usage Example | 使用示例:</strong></p>
 * <pre>{@code
 * // Development-only logging
 * ConditionalLog.devOnly().info("Debug info: {}", debugData);
 *
 * // Conditional logging
 * ConditionalLog.when(config.isVerbose())
 *     .info("Verbose output: {}", detail);
 *
 * // Log once per call site
 * ConditionalLog.once().warn("Deprecated: {}", deprecatedKey);
 *
 * // Rate-limited logging
 * ConditionalLog.atMostEvery(Duration.ofMinutes(1))
 *     .warn("System load high: {}%", load);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Environment-based logging (dev/test/prod) - 基于环境的日志（开发/测试/生产）</li>
 *   <li>Condition-based and lambda-condition logging - 基于条件和 Lambda 条件的日志</li>
 *   <li>Once-only logging per call site - 每个调用点仅一次日志</li>
 *   <li>Rate-limited logging with configurable interval - 可配置间隔的限速日志</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap + AtomicLong) - 线程安全: 是（ConcurrentHashMap + AtomicLong）</li>
 *   <li>Null-safe: Yes (no-op when condition false) - 空值安全: 是（条件为 false 时无操作）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class ConditionalLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionalLog.class);

    // Environment detection
    private static final String ENV = System.getProperty("app.env",
            System.getenv().getOrDefault("APP_ENV", "dev")).toLowerCase();

    // Once-only tracking (by stack trace element)
    private static final Set<String> LOGGED_ONCE = ConcurrentHashMap.newKeySet();

    // Rate limiting tracking
    private static final ConcurrentHashMap<String, AtomicLong> LAST_LOG_TIMES = new ConcurrentHashMap<>();

    private ConditionalLog() {
    }

    // ==================== Environment-based Logging | 基于环境的日志 ====================

    /**
     * Returns a logger that only logs in development environment
     * 返回仅在开发环境记录日志的日志器
     *
     * @return conditional logger | 条件日志器
     */
    public static ConditionalLogger devOnly() {
        return when(isDev());
    }

    /**
     * Returns a logger that only logs in test environment
     * 返回仅在测试环境记录日志的日志器
     *
     * @return conditional logger | 条件日志器
     */
    public static ConditionalLogger testOnly() {
        return when(isTest());
    }

    /**
     * Returns a logger that only logs in production environment
     * 返回仅在生产环境记录日志的日志器
     *
     * @return conditional logger | 条件日志器
     */
    public static ConditionalLogger prodOnly() {
        return when(isProd());
    }

    /**
     * Checks if current environment is development
     * 检查当前环境是否为开发环境
     *
     * @return true if development | 如果是开发环境返回 true
     */
    public static boolean isDev() {
        return "dev".equals(ENV) || "development".equals(ENV) || "local".equals(ENV);
    }

    /**
     * Checks if current environment is test
     * 检查当前环境是否为测试环境
     *
     * @return true if test | 如果是测试环境返回 true
     */
    public static boolean isTest() {
        return "test".equals(ENV) || "testing".equals(ENV) || "stage".equals(ENV) || "staging".equals(ENV);
    }

    /**
     * Checks if current environment is production
     * 检查当前环境是否为生产环境
     *
     * @return true if production | 如果是生产环境返回 true
     */
    public static boolean isProd() {
        return "prod".equals(ENV) || "production".equals(ENV);
    }

    // ==================== Conditional Logging | 条件日志 ====================

    /**
     * Returns a logger that only logs when condition is true
     * 返回仅在条件为真时记录日志的日志器
     *
     * @param condition the condition | 条件
     * @return conditional logger | 条件日志器
     */
    public static ConditionalLogger when(boolean condition) {
        return new ConditionalLogger(condition);
    }

    /**
     * Returns a logger that only logs when condition supplier returns true
     * 返回仅在条件供应者返回真时记录日志的日志器
     *
     * @param condition the condition supplier | 条件供应者
     * @return conditional logger | 条件日志器
     */
    public static ConditionalLogger when(Supplier<Boolean> condition) {
        return new ConditionalLogger(Boolean.TRUE.equals(condition.get()));
    }

    // ==================== Once-only Logging | 仅一次日志 ====================

    /**
     * Returns a logger that logs only once per call site
     * 返回每个调用点只记录一次的日志器
     *
     * @return once-only logger | 仅一次日志器
     */
    public static OnceLogger once() {
        return new OnceLogger();
    }

    // ==================== Rate-limited Logging | 限速日志 ====================

    /**
     * Returns a logger that logs at most once per specified duration
     * 返回每个指定时间段最多记录一次的日志器
     *
     * @param duration the minimum interval between logs | 日志之间的最小间隔
     * @return rate-limited logger | 限速日志器
     */
    public static RateLimitedLogger atMostEvery(Duration duration) {
        return new RateLimitedLogger(duration);
    }

    // ==================== Conditional Logger | 条件日志器 ====================

    /**
     * Logger that only logs when a condition is true
     * 仅在条件为真时记录日志的日志器
     */
    public static class ConditionalLogger {
        private final boolean enabled;

        ConditionalLogger(boolean enabled) {
            this.enabled = enabled;
        }

        public void trace(String message) {
            if (enabled) LOGGER.trace(message);
        }

        public void trace(String format, Object... args) {
            if (enabled) LOGGER.trace(format, args);
        }

        public void debug(String message) {
            if (enabled) LOGGER.debug(message);
        }

        public void debug(String format, Object... args) {
            if (enabled) LOGGER.debug(format, args);
        }

        public void info(String message) {
            if (enabled) LOGGER.info(message);
        }

        public void info(String format, Object... args) {
            if (enabled) LOGGER.info(format, args);
        }

        public void warn(String message) {
            if (enabled) LOGGER.warn(message);
        }

        public void warn(String format, Object... args) {
            if (enabled) LOGGER.warn(format, args);
        }

        public void warn(String message, Throwable t) {
            if (enabled) LOGGER.warn(message, t);
        }

        public void error(String message) {
            if (enabled) LOGGER.error(message);
        }

        public void error(String format, Object... args) {
            if (enabled) LOGGER.error(format, args);
        }

        public void error(String message, Throwable t) {
            if (enabled) LOGGER.error(message, t);
        }
    }

    // ==================== Once Logger | 仅一次日志器 ====================

    /**
     * Logger that logs only once per call site
     * 每个调用点只记录一次的日志器
     */
    public static class OnceLogger {

        OnceLogger() {
        }

        private boolean shouldLog() {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
            String callSite = caller.getClassName() + ":" + caller.getMethodName() + ":" + caller.getLineNumber();
            return LOGGED_ONCE.add(callSite);
        }

        public void trace(String message) {
            if (shouldLog()) LOGGER.trace(message);
        }

        public void trace(String format, Object... args) {
            if (shouldLog()) LOGGER.trace(format, args);
        }

        public void debug(String message) {
            if (shouldLog()) LOGGER.debug(message);
        }

        public void debug(String format, Object... args) {
            if (shouldLog()) LOGGER.debug(format, args);
        }

        public void info(String message) {
            if (shouldLog()) LOGGER.info(message);
        }

        public void info(String format, Object... args) {
            if (shouldLog()) LOGGER.info(format, args);
        }

        public void warn(String message) {
            if (shouldLog()) LOGGER.warn(message);
        }

        public void warn(String format, Object... args) {
            if (shouldLog()) LOGGER.warn(format, args);
        }

        public void warn(String message, Throwable t) {
            if (shouldLog()) LOGGER.warn(message, t);
        }

        public void error(String message) {
            if (shouldLog()) LOGGER.error(message);
        }

        public void error(String format, Object... args) {
            if (shouldLog()) LOGGER.error(format, args);
        }

        public void error(String message, Throwable t) {
            if (shouldLog()) LOGGER.error(message, t);
        }
    }

    // ==================== Rate-limited Logger | 限速日志器 ====================

    /**
     * Logger that logs at most once per specified duration
     * 每个指定时间段最多记录一次的日志器
     */
    public static class RateLimitedLogger {
        private final long intervalMillis;

        RateLimitedLogger(Duration interval) {
            this.intervalMillis = interval.toMillis();
        }

        private boolean shouldLog() {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
            String callSite = caller.getClassName() + ":" + caller.getMethodName() + ":" + caller.getLineNumber();

            long now = System.currentTimeMillis();
            AtomicLong lastTime = LAST_LOG_TIMES.computeIfAbsent(callSite, _ -> new AtomicLong(0));
            long last = lastTime.get();

            if (now - last >= intervalMillis) {
                return lastTime.compareAndSet(last, now);
            }
            return false;
        }

        public void trace(String message) {
            if (shouldLog()) LOGGER.trace(message);
        }

        public void trace(String format, Object... args) {
            if (shouldLog()) LOGGER.trace(format, args);
        }

        public void debug(String message) {
            if (shouldLog()) LOGGER.debug(message);
        }

        public void debug(String format, Object... args) {
            if (shouldLog()) LOGGER.debug(format, args);
        }

        public void info(String message) {
            if (shouldLog()) LOGGER.info(message);
        }

        public void info(String format, Object... args) {
            if (shouldLog()) LOGGER.info(format, args);
        }

        public void warn(String message) {
            if (shouldLog()) LOGGER.warn(message);
        }

        public void warn(String format, Object... args) {
            if (shouldLog()) LOGGER.warn(format, args);
        }

        public void warn(String message, Throwable t) {
            if (shouldLog()) LOGGER.warn(message, t);
        }

        public void error(String message) {
            if (shouldLog()) LOGGER.error(message);
        }

        public void error(String format, Object... args) {
            if (shouldLog()) LOGGER.error(format, args);
        }

        public void error(String message, Throwable t) {
            if (shouldLog()) LOGGER.error(message, t);
        }
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Clears the once-only log tracking (useful for testing)
     * 清除仅一次日志追踪（用于测试）
     */
    public static void clearOnceTracking() {
        LOGGED_ONCE.clear();
    }

    /**
     * Clears the rate limit tracking (useful for testing)
     * 清除限速追踪（用于测试）
     */
    public static void clearRateLimitTracking() {
        LAST_LOG_TIMES.clear();
    }
}
