package cloud.opencode.base.cron;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * OpenCron - Cron Expression Facade
 * OpenCron - Cron表达式门面类
 *
 * <p>Unified entry point for cron expression parsing, validation, scheduling,
 * and human-readable description. All cron functionality is accessible through
 * this single facade class.</p>
 * <p>Cron表达式解析、验证、调度和人类可读描述的统一入口。
 * 所有Cron功能都可通过此单一门面类访问。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Parse cron expressions (5/6-field, macros, aliases) - 解析Cron表达式</li>
 *   <li>Validate expressions with optional minimum interval check - 验证表达式</li>
 *   <li>Calculate next/previous execution times - 计算下次/上次执行时间</li>
 *   <li>Generate human-readable descriptions - 生成人类可读描述</li>
 *   <li>Fluent builder API for constructing expressions - 流式构建器API</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Parse and query next execution | 解析并查询下次执行
 * ZonedDateTime next = OpenCron.nextExecution("0 9 * * MON-FRI", ZonedDateTime.now());
 *
 * // Get next 5 executions | 获取下5次执行时间
 * List<ZonedDateTime> times = OpenCron.nextExecutions("30 10 * * *", ZonedDateTime.now(), 5);
 *
 * // Validate expression | 验证表达式
 * boolean valid = OpenCron.isValid("0 0 L * *");
 *
 * // Human-readable description | 人类可读描述
 * String desc = OpenCron.describe("0 9 * * MON-FRI"); // "At 09:00, Monday through Friday"
 *
 * // Builder API | 构建器API
 * CronExpression expr = OpenCron.builder().weekdays().at(9, 0).build();
 *
 * // Macro support | 宏支持
 * CronExpression daily = OpenCron.parse("@daily");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless static methods) - 线程安全: 是（无状态静态方法）</li>
 *   <li>Null-safe: Yes (rejects null inputs) - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see CronExpression
 * @see CronBuilder
 * @since JDK 25, opencode-base-cron V1.0.0
 */
public final class OpenCron {

    // ==================== Predefined Macros | 预定义宏 ====================

    /** {@code @yearly} — January 1st at midnight | 每年1月1日午夜 */
    public static final String YEARLY = "@yearly";
    /** {@code @monthly} — 1st of each month at midnight | 每月1号午夜 */
    public static final String MONTHLY = "@monthly";
    /** {@code @weekly} — Sunday at midnight | 每周日午夜 */
    public static final String WEEKLY = "@weekly";
    /** {@code @daily} — Every day at midnight | 每天午夜 */
    public static final String DAILY = "@daily";
    /** {@code @hourly} — Every hour | 每小时 */
    public static final String HOURLY = "@hourly";

    private OpenCron() {
    }

    // ==================== Parse | 解析 ====================

    /**
     * Parses a cron expression
     * 解析Cron表达式
     *
     * @param expression the cron expression (5/6-field or macro) | Cron表达式（5/6字段或宏）
     * @return the parsed expression | 解析后的表达式
     * @throws cloud.opencode.base.cron.exception.OpenCronException if invalid | 如果无效
     */
    public static CronExpression parse(String expression) {
        return CronExpression.parse(expression);
    }

    // ==================== Validate | 验证 ====================

    /**
     * Checks if a cron expression is valid
     * 检查Cron表达式是否有效
     *
     * @param expression the cron expression | Cron表达式
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(String expression) {
        return CronValidator.isValid(expression);
    }

    /**
     * Validates a cron expression, throwing on failure
     * 验证Cron表达式，失败时抛出异常
     *
     * @param expression the cron expression | Cron表达式
     * @throws cloud.opencode.base.cron.exception.OpenCronException if invalid | 如果无效
     */
    public static void validate(String expression) {
        CronValidator.validate(expression);
    }

    /**
     * Validates a cron expression with minimum interval check
     * 验证Cron表达式并检查最小间隔
     *
     * @param expression  the cron expression | Cron表达式
     * @param minInterval the minimum interval | 最小间隔
     * @throws cloud.opencode.base.cron.exception.OpenCronException if invalid or interval too short | 如果无效或间隔太短
     */
    public static void validate(String expression, Duration minInterval) {
        CronValidator.validate(expression, minInterval);
    }

    // ==================== Scheduling | 调度 ====================

    /**
     * Gets the next execution time
     * 获取下次执行时间
     *
     * @param expression the cron expression | Cron表达式
     * @param from       the start time | 开始时间
     * @return the next execution time, or null | 下次执行时间，或null
     */
    public static ZonedDateTime nextExecution(String expression, ZonedDateTime from) {
        return CronExpression.parse(expression).nextExecution(from);
    }

    /**
     * Gets the next N execution times
     * 获取下N次执行时间
     *
     * @param expression the cron expression | Cron表达式
     * @param from       the start time | 开始时间
     * @param count      the number of executions | 执行次数
     * @return the list of execution times | 执行时间列表
     */
    public static List<ZonedDateTime> nextExecutions(String expression, ZonedDateTime from, int count) {
        return CronExpression.parse(expression).nextExecutions(from, count);
    }

    /**
     * Gets the previous execution time
     * 获取上次执行时间
     *
     * @param expression the cron expression | Cron表达式
     * @param from       the reference time | 参考时间
     * @return the previous execution time, or null | 上次执行时间，或null
     */
    public static ZonedDateTime previousExecution(String expression, ZonedDateTime from) {
        return CronExpression.parse(expression).previousExecution(from);
    }

    /**
     * Gets the previous N execution times
     * 获取前N次执行时间
     *
     * @param expression the cron expression | Cron表达式
     * @param from       the reference time | 参考时间
     * @param count      the number of executions | 执行次数
     * @return the list of execution times (newest first) | 执行时间列表（最新在前）
     */
    public static List<ZonedDateTime> previousExecutions(String expression, ZonedDateTime from, int count) {
        return CronExpression.parse(expression).previousExecutions(from, count);
    }

    /**
     * Gets the estimated interval between executions
     * 获取执行之间的预估间隔
     *
     * @param expression the cron expression | Cron表达式
     * @return the estimated interval | 预估间隔
     */
    public static Duration getEstimatedInterval(String expression) {
        return CronValidator.getEstimatedInterval(expression);
    }

    // ==================== Duration Convenience | 时间间隔便利 ====================

    /**
     * Gets the duration until the next execution
     * 获取距下次执行的时间间隔
     *
     * @param expression the cron expression | Cron表达式
     * @param from       the reference time | 参考时间
     * @return the duration to next execution, or null | 距下次执行的Duration，或null
     */
    public static Duration timeToNextExecution(String expression, ZonedDateTime from) {
        return CronExpression.parse(expression).timeToNextExecution(from);
    }

    /**
     * Gets the duration since the last execution
     * 获取距上次执行的时间间隔
     *
     * @param expression the cron expression | Cron表达式
     * @param from       the reference time | 参考时间
     * @return the duration from last execution, or null | 距上次执行的Duration，或null
     */
    public static Duration timeFromLastExecution(String expression, ZonedDateTime from) {
        return CronExpression.parse(expression).timeFromLastExecution(from);
    }

    // ==================== Executions Between | 区间执行 ====================

    /**
     * Counts executions between two times
     * 计算两个时间点之间的执行次数
     *
     * @param expression the cron expression | Cron表达式
     * @param from       the start time (exclusive) | 开始时间（不包含）
     * @param to         the end time (inclusive) | 结束时间（包含）
     * @return the count of executions | 执行次数
     */
    public static long countExecutionsBetween(String expression, ZonedDateTime from, ZonedDateTime to) {
        return CronExpression.parse(expression).countExecutionsBetween(from, to);
    }

    /**
     * Lists all executions between two times
     * 列出两个时间点之间的所有执行时间
     *
     * @param expression the cron expression | Cron表达式
     * @param from       the start time (exclusive) | 开始时间（不包含）
     * @param to         the end time (inclusive) | 结束时间（包含）
     * @return unmodifiable list of execution times | 不可修改的执行时间列表
     */
    public static List<ZonedDateTime> executionsBetween(String expression, ZonedDateTime from, ZonedDateTime to) {
        return CronExpression.parse(expression).executionsBetween(from, to);
    }

    // ==================== Equivalence | 等价性 ====================

    /**
     * Checks if two cron expressions produce the same schedule
     * 检查两个Cron表达式是否产生相同的调度
     *
     * @param expr1 the first cron expression | 第一个Cron表达式
     * @param expr2 the second cron expression | 第二个Cron表达式
     * @return true if equivalent schedules | 如果调度等价返回true
     */
    public static boolean isEquivalent(String expr1, String expr2) {
        return CronExpression.parse(expr1).isEquivalentTo(CronExpression.parse(expr2));
    }

    // ==================== Explain | 解释 ====================

    /**
     * Gets a comprehensive explanation for debugging
     * 获取用于调试的综合解释信息
     *
     * @param expression the cron expression | Cron表达式
     * @param from       the reference time | 参考时间
     * @return the explanation | 解释信息
     */
    public static CronExplanation explain(String expression, ZonedDateTime from) {
        return CronExpression.parse(expression).explain(from);
    }

    // ==================== Stream | 流式调度 ====================

    /**
     * Returns a lazy stream of future execution times
     * 返回未来执行时间的惰性流
     *
     * @param expression the cron expression | Cron表达式
     * @param from       the start time (exclusive) | 开始时间（不包含）
     * @return an ordered stream of execution times | 有序的执行时间流
     */
    public static Stream<ZonedDateTime> stream(String expression, ZonedDateTime from) {
        return CronExpression.parse(expression).stream(from);
    }

    /**
     * Returns a lazy stream of past execution times (newest first)
     * 返回过去执行时间的惰性流（最新在前）
     *
     * @param expression the cron expression | Cron表达式
     * @param from       the reference time (exclusive) | 参考时间（不包含）
     * @return an ordered stream of past execution times | 有序的过去执行时间流
     */
    public static Stream<ZonedDateTime> reverseStream(String expression, ZonedDateTime from) {
        return CronExpression.parse(expression).reverseStream(from);
    }

    // ==================== Filtered Scheduling | 过滤调度 ====================

    /**
     * Gets the next execution time that satisfies a filter
     * 获取满足过滤条件的下次执行时间
     *
     * @param expression the cron expression | Cron表达式
     * @param from       the start time (exclusive) | 开始时间（不包含）
     * @param filter     the filter predicate | 过滤谓词
     * @return the next matching execution time, or null | 下次匹配的执行时间
     */
    public static ZonedDateTime nextExecution(String expression, ZonedDateTime from,
                                              Predicate<ZonedDateTime> filter) {
        return CronExpression.parse(expression).nextExecution(from, filter);
    }

    /**
     * Gets the previous execution time that satisfies a filter
     * 获取满足过滤条件的上次执行时间
     *
     * @param expression the cron expression | Cron表达式
     * @param from       the reference time (exclusive) | 参考时间（不包含）
     * @param filter     the filter predicate | 过滤谓词
     * @return the previous matching execution time, or null | 上次匹配的执行时间
     */
    public static ZonedDateTime previousExecution(String expression, ZonedDateTime from,
                                                  Predicate<ZonedDateTime> filter) {
        return CronExpression.parse(expression).previousExecution(from, filter);
    }

    // ==================== Overlap Detection | 重叠检测 ====================

    /**
     * Finds the next time both expressions fire simultaneously
     * 查找两个表达式同时触发的下一个时间
     *
     * @param expr1 the first cron expression | 第一个Cron表达式
     * @param expr2 the second cron expression | 第二个Cron表达式
     * @param from  the start time (exclusive) | 开始时间（不包含）
     * @return the next overlapping time, or null | 下一个重叠时间
     */
    public static ZonedDateTime nextOverlap(String expr1, String expr2, ZonedDateTime from) {
        return CronExpression.parse(expr1).nextOverlap(CronExpression.parse(expr2), from);
    }

    /**
     * Checks if two expressions overlap in a time range
     * 检查两个表达式在时间范围内是否有重叠
     *
     * @param expr1 the first cron expression | 第一个Cron表达式
     * @param expr2 the second cron expression | 第二个Cron表达式
     * @param from  the start time (exclusive) | 开始时间（不包含）
     * @param to    the end time (inclusive) | 结束时间（包含）
     * @return true if overlapping | 如果重叠返回true
     */
    public static boolean hasOverlap(String expr1, String expr2, ZonedDateTime from, ZonedDateTime to) {
        return CronExpression.parse(expr1).hasOverlapBetween(CronExpression.parse(expr2), from, to);
    }

    // ==================== Describe | 描述 ====================

    /**
     * Gets a human-readable description of a cron expression
     * 获取Cron表达式的人类可读描述
     *
     * @param expression the cron expression | Cron表达式
     * @return the description | 描述
     */
    public static String describe(String expression) {
        return CronExpression.parse(expression).describe();
    }

    /**
     * Gets a human-readable description in the specified locale
     * 获取指定语言的人类可读描述
     *
     * @param expression the cron expression | Cron表达式
     * @param locale     the locale | 语言
     * @return the localized description | 本地化描述
     */
    public static String describe(String expression, Locale locale) {
        return CronExpression.parse(expression).describe(locale);
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Creates a new cron expression builder
     * 创建新的Cron表达式构建器
     *
     * @return the builder | 构建器
     */
    public static CronBuilder builder() {
        return CronBuilder.create();
    }
}
