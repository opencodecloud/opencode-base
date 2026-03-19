package cloud.opencode.base.rules.decision;

import java.util.List;
import java.util.Map;

/**
 * Decision Result - Result of Decision Table Evaluation
 * 决策结果 - 决策表评估的结果
 *
 * <p>Contains the output values from matched rows in a decision table.</p>
 * <p>包含决策表中匹配行的输出值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable result record - 不可变结果记录</li>
 *   <li>Single and multiple match support - 单个和多个匹配支持</li>
 *   <li>Typed value retrieval with defaults - 类型化值检索（带默认值）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DecisionResult result = table.evaluate(context);
 * if (result.hasMatch()) {
 *     Double discount = result.get("discount");
 *     Boolean freeShipping = result.get("freeShipping");
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (outputs map must not be null) - 空值安全: 否（输出映射不能为null）</li>
 * </ul>
 *
 * @param matched     whether any row matched | 是否有行匹配
 * @param outputs     the output values from the matched row(s) | 匹配行的输出值
 * @param matchedRows the indices of matched rows | 匹配行的索引
 * @param allOutputs  all outputs when multiple rows match (for COLLECT policy) | 多行匹配时的所有输出（用于COLLECT策略）
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public record DecisionResult(
        boolean matched,
        Map<String, Object> outputs,
        List<Integer> matchedRows,
        List<Map<String, Object>> allOutputs
) {
    /**
     * Checks if any row matched
     * 检查是否有行匹配
     *
     * @return true if matched | 如果匹配返回true
     */
    public boolean hasMatch() {
        return matched;
    }

    /**
     * Gets an output value
     * 获取输出值
     *
     * @param key the output key | 输出键
     * @param <T> the value type | 值类型
     * @return the output value | 输出值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) outputs.get(key);
    }

    /**
     * Gets an output value with default
     * 获取输出值，带默认值
     *
     * @param key          the output key | 输出键
     * @param defaultValue the default value | 默认值
     * @param <T>          the value type | 值类型
     * @return the output value or default | 输出值或默认值
     */
    public <T> T get(String key, T defaultValue) {
        T value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets the count of matched rows
     * 获取匹配行的数量
     *
     * @return matched row count | 匹配行数量
     */
    public int matchCount() {
        return matchedRows.size();
    }

    /**
     * Creates a result for no match
     * 创建无匹配的结果
     *
     * @return no-match result | 无匹配结果
     */
    public static DecisionResult noMatch() {
        return new DecisionResult(false, Map.of(), List.of(), List.of());
    }

    /**
     * Creates a result for a single match
     * 创建单个匹配的结果
     *
     * @param rowIndex the matched row index | 匹配行索引
     * @param outputs  the output values | 输出值
     * @return the result | 结果
     */
    public static DecisionResult singleMatch(int rowIndex, Map<String, Object> outputs) {
        return new DecisionResult(true, outputs, List.of(rowIndex), List.of(outputs));
    }

    /**
     * Creates a result for multiple matches
     * 创建多个匹配的结果
     *
     * @param matchedRows the matched row indices | 匹配行索引
     * @param allOutputs  all output values | 所有输出值
     * @return the result | 结果
     */
    public static DecisionResult multipleMatches(List<Integer> matchedRows, List<Map<String, Object>> allOutputs) {
        Map<String, Object> firstOutput = allOutputs.isEmpty() ? Map.of() : allOutputs.getFirst();
        return new DecisionResult(true, firstOutput, matchedRows, allOutputs);
    }
}
