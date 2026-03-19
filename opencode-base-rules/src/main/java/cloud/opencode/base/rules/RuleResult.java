package cloud.opencode.base.rules;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rule Result - Immutable Record of Rule Execution Outcome
 * 规则结果 - 规则执行结果的不可变记录
 *
 * <p>Contains information about which rules were fired, skipped, or failed,
 * along with execution results and timing information.</p>
 * <p>包含哪些规则被触发、跳过或失败的信息，以及执行结果和时间信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fired rules tracking - 已触发规则跟踪</li>
 *   <li>Skipped rules tracking - 已跳过规则跟踪</li>
 *   <li>Failed rules with errors - 失败规则及错误</li>
 *   <li>Execution results collection - 执行结果收集</li>
 *   <li>Execution time measurement - 执行时间测量</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RuleResult result = engine.fire(context);
 *
 * if (result.success()) {
 *     System.out.println("Fired: " + result.firedCount());
 *     Double discount = result.getResult("discount");
 * }
 *
 * if (result.wasFired("VIP-discount")) {
 *     // handle VIP discount
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param success       whether execution was successful | 执行是否成功
 * @param firedRules    list of fired rule names | 已触发的规则名称列表
 * @param skippedRules  list of skipped rule names | 已跳过的规则名称列表
 * @param failedRules   list of failed rule names | 已失败的规则名称列表
 * @param results       map of execution results | 执行结果Map
 * @param executionTime total execution time | 总执行时间
 * @param errors        list of errors | 错误列表
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Rule
 * @see RuleEngine
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public record RuleResult(
        boolean success,
        List<String> firedRules,
        List<String> skippedRules,
        List<String> failedRules,
        Map<String, Object> results,
        Duration executionTime,
        List<RuleError> errors
) {

    /**
     * Gets the count of fired rules
     * 获取已触发规则的数量
     *
     * @return fired rule count | 已触发规则数量
     */
    public int firedCount() {
        return firedRules.size();
    }

    /**
     * Gets the count of skipped rules
     * 获取已跳过规则的数量
     *
     * @return skipped rule count | 已跳过规则数量
     */
    public int skippedCount() {
        return skippedRules.size();
    }

    /**
     * Gets the count of failed rules
     * 获取已失败规则的数量
     *
     * @return failed rule count | 已失败规则数量
     */
    public int failedCount() {
        return failedRules.size();
    }

    /**
     * Checks if any rules were fired
     * 检查是否有规则被触发
     *
     * @return true if at least one rule was fired | 如果至少有一个规则被触发返回true
     */
    public boolean hasFired() {
        return !firedRules.isEmpty();
    }

    /**
     * Checks if a specific rule was fired
     * 检查特定规则是否被触发
     *
     * @param ruleName the rule name | 规则名称
     * @return true if the rule was fired | 如果规则被触发返回true
     */
    public boolean wasFired(String ruleName) {
        return firedRules.contains(ruleName);
    }

    /**
     * Checks if a specific rule was skipped
     * 检查特定规则是否被跳过
     *
     * @param ruleName the rule name | 规则名称
     * @return true if the rule was skipped | 如果规则被跳过返回true
     */
    public boolean wasSkipped(String ruleName) {
        return skippedRules.contains(ruleName);
    }

    /**
     * Checks if a specific rule failed
     * 检查特定规则是否失败
     *
     * @param ruleName the rule name | 规则名称
     * @return true if the rule failed | 如果规则失败返回true
     */
    public boolean hasFailed(String ruleName) {
        return failedRules.contains(ruleName);
    }

    /**
     * Gets a result value by key
     * 按键获取结果值
     *
     * @param key the result key | 结果键
     * @param <T> the result type | 结果类型
     * @return the result value, or null if not found | 结果值，如果未找到则为null
     */
    @SuppressWarnings("unchecked")
    public <T> T getResult(String key) {
        return (T) results.get(key);
    }

    /**
     * Gets a result value with default
     * 获取结果值，带默认值
     *
     * @param key          the result key | 结果键
     * @param defaultValue the default value | 默认值
     * @param <T>          the result type | 结果类型
     * @return the result value or default | 结果值或默认值
     */
    public <T> T getResult(String key, T defaultValue) {
        T value = getResult(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Checks if a result exists
     * 检查结果是否存在
     *
     * @param key the result key | 结果键
     * @return true if exists | 如果存在返回true
     */
    public boolean hasResult(String key) {
        return results.containsKey(key);
    }

    /**
     * Creates a success result builder
     * 创建成功结果构建器
     *
     * @return builder instance | 构建器实例
     */
    public static Builder successBuilder() {
        return new Builder(true);
    }

    /**
     * Creates a failure result builder
     * 创建失败结果构建器
     *
     * @return builder instance | 构建器实例
     */
    public static Builder failure() {
        return new Builder(false);
    }

    /**
     * Rule Error - Details about a rule execution failure
     * 规则错误 - 规则执行失败的详细信息
     *
     * @param ruleName the rule that failed | 失败的规则
     * @param message  the error message | 错误消息
     * @param cause    the cause, may be null | 原因，可能为null
     */
    public record RuleError(String ruleName, String message, Throwable cause) {
        /**
         * Creates a rule error without cause
         * 创建没有原因的规则错误
         *
         * @param ruleName the rule name | 规则名称
         * @param message  the error message | 错误消息
         * @return rule error instance | 规则错误实例
         */
        public static RuleError of(String ruleName, String message) {
            return new RuleError(ruleName, message, null);
        }

        /**
         * Creates a rule error with cause
         * 创建带原因的规则错误
         *
         * @param ruleName the rule name | 规则名称
         * @param message  the error message | 错误消息
         * @param cause    the cause | 原因
         * @return rule error instance | 规则错误实例
         */
        public static RuleError of(String ruleName, String message, Throwable cause) {
            return new RuleError(ruleName, message, cause);
        }
    }

    /**
     * Builder for RuleResult
     * RuleResult的构建器
     */
    public static final class Builder {
        private final boolean success;
        private final List<String> firedRules = new ArrayList<>();
        private final List<String> skippedRules = new ArrayList<>();
        private final List<String> failedRules = new ArrayList<>();
        private final Map<String, Object> results = new HashMap<>();
        private final List<RuleError> errors = new ArrayList<>();
        private Duration executionTime = Duration.ZERO;

        private Builder(boolean success) {
            this.success = success;
        }

        /**
         * Adds a fired rule
         * 添加已触发的规则
         *
         * @param ruleName the rule name | 规则名称
         * @return this builder | 此构建器
         */
        public Builder fired(String ruleName) {
            firedRules.add(ruleName);
            return this;
        }

        /**
         * Adds a skipped rule
         * 添加已跳过的规则
         *
         * @param ruleName the rule name | 规则名称
         * @return this builder | 此构建器
         */
        public Builder skipped(String ruleName) {
            skippedRules.add(ruleName);
            return this;
        }

        /**
         * Adds a failed rule
         * 添加已失败的规则
         *
         * @param ruleName the rule name | 规则名称
         * @param message  the error message | 错误消息
         * @param cause    the cause | 原因
         * @return this builder | 此构建器
         */
        public Builder failed(String ruleName, String message, Throwable cause) {
            failedRules.add(ruleName);
            errors.add(new RuleError(ruleName, message, cause));
            return this;
        }

        /**
         * Adds a result
         * 添加结果
         *
         * @param key   the result key | 结果键
         * @param value the result value | 结果值
         * @return this builder | 此构建器
         */
        public Builder result(String key, Object value) {
            results.put(key, value);
            return this;
        }

        /**
         * Adds multiple results
         * 添加多个结果
         *
         * @param results the results map | 结果Map
         * @return this builder | 此构建器
         */
        public Builder results(Map<String, Object> results) {
            this.results.putAll(results);
            return this;
        }

        /**
         * Sets the execution time
         * 设置执行时间
         *
         * @param duration the execution duration | 执行时长
         * @return this builder | 此构建器
         */
        public Builder executionTime(Duration duration) {
            this.executionTime = duration;
            return this;
        }

        /**
         * Builds the RuleResult
         * 构建RuleResult
         *
         * @return the rule result | 规则结果
         */
        public RuleResult build() {
            return new RuleResult(
                    success,
                    List.copyOf(firedRules),
                    List.copyOf(skippedRules),
                    List.copyOf(failedRules),
                    Map.copyOf(results),
                    executionTime,
                    List.copyOf(errors)
            );
        }
    }
}
