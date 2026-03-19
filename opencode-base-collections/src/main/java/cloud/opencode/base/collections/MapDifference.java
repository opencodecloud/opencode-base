package cloud.opencode.base.collections;

import java.util.Map;

/**
 * MapDifference - Result of comparing two maps
 * MapDifference - 比较两个 Map 的结果
 *
 * <p>Represents the difference between two maps, including entries only in the left,
 * only in the right, in common, and with differing values.</p>
 * <p>表示两个 Map 之间的差异，包括仅在左边的条目、仅在右边的条目、共同的条目和值不同的条目。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Entries only on left - 仅在左边的条目</li>
 *   <li>Entries only on right - 仅在右边的条目</li>
 *   <li>Entries in common - 共同的条目</li>
 *   <li>Entries with differing values - 值不同的条目</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MapDifference<String, Integer> diff = MapUtil.difference(map1, map2);
 *
 * if (!diff.areEqual()) {
 *     Map<String, Integer> onlyLeft = diff.entriesOnlyOnLeft();
 *     Map<String, Integer> onlyRight = diff.entriesOnlyOnRight();
 *     Map<String, ValueDifference<Integer>> differing = diff.entriesDiffering();
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public interface MapDifference<K, V> {

    /**
     * Check if the two maps are equal
     * 是否相等
     *
     * @return true if equal | 如果相等则返回 true
     */
    boolean areEqual();

    /**
     * Get entries only in the left map
     * 仅在左边的条目
     *
     * @return entries only on left | 仅在左边的条目
     */
    Map<K, V> entriesOnlyOnLeft();

    /**
     * Get entries only in the right map
     * 仅在右边的条目
     *
     * @return entries only on right | 仅在右边的条目
     */
    Map<K, V> entriesOnlyOnRight();

    /**
     * Get entries common to both maps (same key and value)
     * 共同的条目
     *
     * @return common entries | 共同的条目
     */
    Map<K, V> entriesInCommon();

    /**
     * Get entries where keys are the same but values differ
     * 值不同的条目
     *
     * @return differing entries | 值不同的条目
     */
    Map<K, ValueDifference<V>> entriesDiffering();

    /**
     * ValueDifference - Represents different values for the same key
     * ValueDifference - 表示相同键的不同值
     *
     * @param <V> value type | 值类型
     */
    interface ValueDifference<V> {

        /**
         * Get the left value
         * 左值
         *
         * @return left value | 左值
         */
        V leftValue();

        /**
         * Get the right value
         * 右值
         *
         * @return right value | 右值
         */
        V rightValue();
    }
}
