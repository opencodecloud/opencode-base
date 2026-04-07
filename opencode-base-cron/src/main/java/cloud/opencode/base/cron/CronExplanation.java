package cloud.opencode.base.cron;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Cron Explanation - One-Stop Debugging Info Container for Cron Expressions
 * Cron解释 - Cron表达式的一站式调试信息容器
 *
 * <p>An immutable record that bundles together all the information needed
 * to understand and debug a cron expression: the original expression string,
 * a human-readable description, upcoming execution times, and the estimated
 * interval between executions.</p>
 * <p>一个不可变记录，将理解和调试Cron表达式所需的所有信息捆绑在一起：
 * 原始表达式字符串、人类可读描述、即将到来的执行时间以及执行之间的预估间隔。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable and thread-safe (Java record) - 不可变且线程安全（Java记录）</li>
 *   <li>Unmodifiable next-executions list - 不可修改的下次执行列表</li>
 *   <li>Formatted multi-line toString() for debugging - 格式化的多行toString()用于调试</li>
 *   <li>Serializable for transport/storage - 可序列化用于传输/存储</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CronExpression expr = CronExpression.parse("0 9 * * MON-FRI");
 * ZonedDateTime now = ZonedDateTime.now();
 * CronExplanation explanation = new CronExplanation(
 *     expr.getExpression(),
 *     expr.describe(),
 *     expr.nextExecutions(now, 5),
 *     Duration.ofHours(24)
 * );
 * System.out.println(explanation);
 * // Expression : 0 9 * * MON-FRI
 * // Description: At 09:00, Monday through Friday
 * // Interval   : PT24H
 * // Next executions:
 * //   1. 2026-04-03T09:00:00+08:00[Asia/Shanghai]
 * //   2. 2026-04-06T09:00:00+08:00[Asia/Shanghai]
 * //   ...
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record with defensive copy) - 线程安全: 是（不可变记录，防御性拷贝）</li>
 *   <li>Null-safe: Yes (rejects null inputs) - 空值安全: 是</li>
 * </ul>
 *
 * @param expression        the original cron expression string | 原始Cron表达式字符串
 * @param description       a human-readable description of the expression | 表达式的人类可读描述
 * @param nextExecutions    the upcoming execution times (unmodifiable) | 即将到来的执行时间（不可修改）
 * @param estimatedInterval the estimated interval between consecutive executions | 连续执行之间的预估间隔
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see CronExpression
 * @see OpenCron
 * @since JDK 25, opencode-base-cron V1.0.3
 */
public record CronExplanation(
        String expression,
        String description,
        List<ZonedDateTime> nextExecutions,
        Duration estimatedInterval
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ISO_ZONED_DATE_TIME;

    /**
     * Compact constructor — validates inputs and creates a defensive copy of the list.
     * 紧凑构造器 — 验证输入并创建列表的防御性拷贝。
     */
    public CronExplanation {
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(nextExecutions, "nextExecutions must not be null");
        Objects.requireNonNull(estimatedInterval, "estimatedInterval must not be null");
        nextExecutions = List.copyOf(nextExecutions);
    }

    /**
     * Returns a nicely formatted multi-line debug output.
     * 返回格式良好的多行调试输出。
     *
     * @return the formatted debug string | 格式化的调试字符串
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Expression : ").append(expression).append('\n');
        sb.append("Description: ").append(description).append('\n');
        sb.append("Interval   : ").append(estimatedInterval).append('\n');
        sb.append("Next executions:");
        if (nextExecutions.isEmpty()) {
            sb.append(" (none)");
        } else {
            sb.append('\n');
            for (int i = 0; i < nextExecutions.size(); i++) {
                sb.append("  ").append(i + 1).append(". ")
                        .append(nextExecutions.get(i).format(FORMATTER));
                if (i < nextExecutions.size() - 1) {
                    sb.append('\n');
                }
            }
        }
        return sb.toString();
    }
}
