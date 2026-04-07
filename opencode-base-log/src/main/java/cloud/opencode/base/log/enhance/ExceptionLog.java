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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Exception Logging Enhancement - Smart Exception Logging
 * 异常日志增强 - 智能异常日志
 *
 * <p>Provides enhanced exception logging capabilities including:</p>
 * <p>提供增强的异常日志能力，包括：</p>
 * <ul>
 *   <li>Root cause analysis - 根因分析</li>
 *   <li>Exception chain formatting - 异常链格式化</li>
 *   <li>Deduplication (same exception logged only once) - 去重（相同异常仅记录一次）</li>
 *   <li>Rate limiting - 限速</li>
 *   <li>Exception summarization - 异常摘要</li>
 * </ul>
 *
 * <p><strong>Usage Example | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     processOrder(order);
 * } catch (Exception e) {
 *     // Smart logging with root cause analysis
 *     ExceptionLog.error("Order processing failed: orderId=" + orderId, e);
 *     // Output:
 *     // ERROR Order processing failed: orderId=123
 *     // Root Cause: SQLException: Connection refused
 *     // Exception Chain: OrderProcessException -> ServiceException -> SQLException
 *
 *     // Log once (avoid log flooding)
 *     ExceptionLog.errorOnce("Service unavailable", e);
 *
 *     // Rate-limited logging
 *     ExceptionLog.errorRateLimited("DB connection failed", e, Duration.ofMinutes(1));
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Root cause analysis and exception chain formatting - 根因分析和异常链格式化</li>
 *   <li>Exception deduplication (log once per unique signature) - 异常去重（每个唯一签名仅记录一次）</li>
 *   <li>Rate-limited exception logging - 限速异常日志</li>
 *   <li>Compact stack trace with configurable depth - 可配置深度的紧凑堆栈跟踪</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap + AtomicLong) - 线程安全: 是（ConcurrentHashMap + AtomicLong）</li>
 *   <li>Null-safe: Yes (handles null throwable) - 空值安全: 是（处理 null 异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class ExceptionLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionLog.class);

    // Maximum tracked exceptions to prevent unbounded memory growth
    private static final int MAX_TRACKED_EXCEPTIONS = 10_000;

    // Deduplication tracking (by exception signature)
    private static final Set<String> LOGGED_EXCEPTIONS = ConcurrentHashMap.newKeySet();

    // Rate limiting tracking
    private static final ConcurrentHashMap<String, AtomicLong> LAST_LOG_TIMES = new ConcurrentHashMap<>();

    private ExceptionLog() {
    }

    // ==================== Enhanced Error Logging | 增强错误日志 ====================

    /**
     * Logs an error with root cause analysis
     * 带根因分析记录错误
     *
     * @param message   the log message | 日志消息
     * @param throwable the exception | 异常
     */
    public static void error(String message, Throwable throwable) {
        Throwable rootCause = getRootCause(throwable);
        String chain = formatExceptionChain(throwable);

        LOGGER.error("{}\n  Root Cause: {} - {}\n  Exception Chain: {}",
                message,
                rootCause.getClass().getSimpleName(),
                rootCause.getMessage(),
                chain,
                throwable);
    }

    /**
     * Logs an error only once per unique exception signature
     * 每个唯一异常签名仅记录一次错误
     *
     * <p>Note: Tracks at most 10,000 unique signatures to prevent memory leaks.
     * When the limit is reached, the tracking set is cleared and logging resumes.</p>
     *
     * @param message   the log message | 日志消息
     * @param throwable the exception | 异常
     */
    public static void errorOnce(String message, Throwable throwable) {
        String signature = getExceptionSignature(throwable);
        // Prevent unbounded growth: evict oldest batch instead of clearing all
        if (LOGGED_EXCEPTIONS.size() > MAX_TRACKED_EXCEPTIONS) {
            evictOldest(LOGGED_EXCEPTIONS, MAX_TRACKED_EXCEPTIONS / 10);
        }
        if (LOGGED_EXCEPTIONS.add(signature)) {
            error(message, throwable);
        }
    }

    /**
     * Logs an error with rate limiting
     * 带限速记录错误
     *
     * @param message   the log message | 日志消息
     * @param throwable the exception | 异常
     * @param interval  minimum interval between logs | 日志之间的最小间隔
     */
    public static void errorRateLimited(String message, Throwable throwable, Duration interval) {
        String signature = getExceptionSignature(throwable);
        long now = System.currentTimeMillis();
        // Prevent unbounded growth: evict oldest batch instead of clearing all
        if (LAST_LOG_TIMES.size() > MAX_TRACKED_EXCEPTIONS) {
            evictOldestTimes(LAST_LOG_TIMES, MAX_TRACKED_EXCEPTIONS / 10);
        }
        AtomicLong lastTime = LAST_LOG_TIMES.computeIfAbsent(signature, _ -> new AtomicLong(0));
        long last = lastTime.get();

        if (now - last >= interval.toMillis()) {
            if (lastTime.compareAndSet(last, now)) {
                error(message, throwable);
            }
        }
    }

    /**
     * Logs a warning with root cause analysis
     * 带根因分析记录警告
     *
     * @param message   the log message | 日志消息
     * @param throwable the exception | 异常
     */
    public static void warn(String message, Throwable throwable) {
        Throwable rootCause = getRootCause(throwable);
        LOGGER.warn("{} (Root Cause: {} - {})",
                message,
                rootCause.getClass().getSimpleName(),
                rootCause.getMessage());
    }

    // ==================== Exception Analysis | 异常分析 ====================

    /**
     * Gets the root cause of an exception
     * 获取异常的根因
     *
     * @param throwable the exception | 异常
     * @return the root cause | 根因
     */
    public static Throwable getRootCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        // Use visited set to detect arbitrary cycles (not just self-reference)
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable cause = throwable;
        while (cause.getCause() != null && visited.add(cause)) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * Gets the causal chain of an exception
     * 获取异常的因果链
     *
     * @param throwable the exception | 异常
     * @return list of exceptions in the chain | 链中的异常列表
     */
    public static List<Throwable> getCausalChain(Throwable throwable) {
        List<Throwable> chain = new ArrayList<>();
        Throwable current = throwable;
        while (current != null && !chain.contains(current)) {
            chain.add(current);
            current = current.getCause();
        }
        return chain;
    }

    /**
     * Formats the exception chain as a string
     * 将异常链格式化为字符串
     *
     * @param throwable the exception | 异常
     * @return formatted chain string | 格式化的链字符串
     */
    public static String formatExceptionChain(Throwable throwable) {
        List<Throwable> chain = getCausalChain(throwable);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chain.size(); i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(chain.get(i).getClass().getSimpleName());
        }
        return sb.toString();
    }

    /**
     * Creates a summary of an exception
     * 创建异常摘要
     *
     * @param throwable the exception | 异常
     * @return summary string | 摘要字符串
     */
    public static String summarize(Throwable throwable) {
        if (throwable == null) {
            return "null";
        }
        Throwable root = getRootCause(throwable);
        return String.format("%s: %s (root: %s - %s)",
                throwable.getClass().getSimpleName(),
                throwable.getMessage() != null ? throwable.getMessage() : "no message",
                root.getClass().getSimpleName(),
                root.getMessage() != null ? root.getMessage() : "no message");
    }

    /**
     * Gets the stack trace as a string
     * 获取堆栈跟踪字符串
     *
     * @param throwable the exception | 异常
     * @return stack trace string | 堆栈跟踪字符串
     */
    public static String getStackTraceString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Gets a compact stack trace (limited depth)
     * 获取紧凑的堆栈跟踪（有限深度）
     *
     * @param throwable the exception | 异常
     * @param maxDepth  maximum stack trace depth | 最大堆栈跟踪深度
     * @return compact stack trace | 紧凑的堆栈跟踪
     */
    public static String getCompactStackTrace(Throwable throwable, int maxDepth) {
        if (throwable == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName())
                .append(": ")
                .append(throwable.getMessage())
                .append("\n");

        StackTraceElement[] elements = throwable.getStackTrace();
        int depth = Math.min(elements.length, maxDepth);
        for (int i = 0; i < depth; i++) {
            sb.append("  at ").append(elements[i]).append("\n");
        }
        if (elements.length > maxDepth) {
            sb.append("  ... ").append(elements.length - maxDepth).append(" more\n");
        }
        return sb.toString();
    }

    // ==================== Exception Signature | 异常签名 ====================

    /**
     * Gets a unique signature for an exception (for deduplication)
     * 获取异常的唯一签名（用于去重）
     *
     * @param throwable the exception | 异常
     * @return signature string | 签名字符串
     */
    public static String getExceptionSignature(Throwable throwable) {
        if (throwable == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName());

        // Add first few stack trace elements
        StackTraceElement[] elements = throwable.getStackTrace();
        int depth = Math.min(elements.length, 3);
        for (int i = 0; i < depth; i++) {
            sb.append("|")
                    .append(elements[i].getClassName())
                    .append(".")
                    .append(elements[i].getMethodName())
                    .append(":")
                    .append(elements[i].getLineNumber());
        }

        // Include root cause type
        Throwable root = getRootCause(throwable);
        if (root != throwable) {
            sb.append("|root:").append(root.getClass().getName());
        }

        return sb.toString();
    }

    // ==================== Eviction Helpers | 淘汰帮助方法 ====================

    /**
     * Evicts a batch of entries from a Set to prevent unbounded growth.
     * 从 Set 中批量淘汰条目以防止无限增长。
     */
    private static void evictOldest(Set<String> set, int count) {
        var iterator = set.iterator();
        for (int i = 0; i < count && iterator.hasNext(); i++) {
            iterator.next();
            iterator.remove();
        }
    }

    /**
     * Evicts entries with the oldest timestamps from a rate-limit map.
     * 从限速映射中淘汰最旧时间戳的条目。
     */
    private static void evictOldestTimes(ConcurrentHashMap<String, AtomicLong> map, int count) {
        map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(
                        java.util.Comparator.comparingLong(AtomicLong::get)))
                .limit(count)
                .map(Map.Entry::getKey)
                .toList()
                .forEach(map::remove);
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Checks if an exception contains a specific exception type in its chain
     * 检查异常链中是否包含特定异常类型
     *
     * @param throwable  the exception | 异常
     * @param targetType the type to check for | 要检查的类型
     * @return true if found | 如果找到返回 true
     */
    public static boolean containsExceptionType(Throwable throwable, Class<? extends Throwable> targetType) {
        for (Throwable t : getCausalChain(throwable)) {
            if (targetType.isInstance(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds an exception of specific type in the causal chain
     * 在因果链中查找特定类型的异常
     *
     * @param throwable  the exception | 异常
     * @param targetType the type to find | 要查找的类型
     * @param <T>        the exception type | 异常类型
     * @return the found exception or null | 找到的异常或 null
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T findExceptionOfType(Throwable throwable, Class<T> targetType) {
        for (Throwable t : getCausalChain(throwable)) {
            if (targetType.isInstance(t)) {
                return (T) t;
            }
        }
        return null;
    }

    /**
     * Clears the deduplication tracking (useful for testing)
     * 清除去重追踪（用于测试）
     */
    public static void clearDeduplicationTracking() {
        LOGGED_EXCEPTIONS.clear();
    }

    /**
     * Clears the rate limit tracking (useful for testing)
     * 清除限速追踪（用于测试）
     */
    public static void clearRateLimitTracking() {
        LAST_LOG_TIMES.clear();
    }
}
