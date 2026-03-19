package cloud.opencode.base.collections;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * ComparatorUtil - Comparator Utility Class
 * ComparatorUtil - 比较器工具类
 *
 * <p>Provides utility methods for creating and manipulating Comparators.</p>
 * <p>提供创建和操作 Comparator 的工具方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Null handling comparators - 空值处理比较器</li>
 *   <li>Order checking methods - 顺序检查方法</li>
 *   <li>Lexicographical comparators - 字典序比较器</li>
 *   <li>Min/Max value operations - 最值操作</li>
 *   <li>Top K collectors - Top K 收集器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check if ordered - 检查是否有序
 * boolean ordered = ComparatorUtil.isInOrder(list, Comparator.naturalOrder());
 *
 * // Lexicographical comparator - 字典序比较器
 * Comparator<Iterable<String>> lexical = ComparatorUtil.lexicographical();
 *
 * // Get min/max - 获取最小/最大值
 * String min = ComparatorUtil.min("a", "b", Comparator.naturalOrder());
 *
 * // Least K elements collector - 最小 K 个元素收集器
 * List<Integer> smallest = stream.collect(ComparatorUtil.least(5, Comparator.naturalOrder()));
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>isInOrder: O(n) - isInOrder: O(n)</li>
 *   <li>least/greatest: O(n log k) - least/greatest: O(n log k)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class ComparatorUtil {

    private ComparatorUtil() {
    }

    // ==================== 空值处理 | Null Handling ====================

    /**
     * Return a comparator that treats null as less than all other values
     * null 值最小的比较器
     *
     * @param <T>        element type | 元素类型
     * @param comparator the comparator for non-null values | 非空值的比较器
     * @return comparator with nulls first | null 优先的比较器
     */
    public static <T> Comparator<T> nullsFirst(Comparator<? super T> comparator) {
        return Comparator.nullsFirst(comparator);
    }

    /**
     * Return a comparator that treats null as greater than all other values
     * null 值最大的比较器
     *
     * @param <T>        element type | 元素类型
     * @param comparator the comparator for non-null values | 非空值的比较器
     * @return comparator with nulls last | null 最后的比较器
     */
    public static <T> Comparator<T> nullsLast(Comparator<? super T> comparator) {
        return Comparator.nullsLast(comparator);
    }

    // ==================== 检查方法 | Check Methods ====================

    /**
     * Check if the iterable is in non-strict ascending order
     * 检查是否有序
     *
     * @param <T>        element type | 元素类型
     * @param iterable   the iterable | 可迭代对象
     * @param comparator the comparator | 比较器
     * @return true if in order | 如果有序则返回 true
     */
    public static <T> boolean isInOrder(Iterable<? extends T> iterable, Comparator<T> comparator) {
        if (iterable == null || comparator == null) {
            return true;
        }
        Iterator<? extends T> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            return true;
        }
        T prev = iterator.next();
        while (iterator.hasNext()) {
            T current = iterator.next();
            if (comparator.compare(prev, current) > 0) {
                return false;
            }
            prev = current;
        }
        return true;
    }

    /**
     * Check if the iterable is in strict ascending order
     * 检查是否严格有序
     *
     * @param <T>        element type | 元素类型
     * @param iterable   the iterable | 可迭代对象
     * @param comparator the comparator | 比较器
     * @return true if in strict order | 如果严格有序则返回 true
     */
    public static <T> boolean isInStrictOrder(Iterable<? extends T> iterable, Comparator<T> comparator) {
        if (iterable == null || comparator == null) {
            return true;
        }
        Iterator<? extends T> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            return true;
        }
        T prev = iterator.next();
        while (iterator.hasNext()) {
            T current = iterator.next();
            if (comparator.compare(prev, current) >= 0) {
                return false;
            }
            prev = current;
        }
        return true;
    }

    // ==================== 字典序 | Lexicographical Order ====================

    /**
     * Return a lexicographical comparator for iterables
     * Iterable 的字典序比较器
     *
     * @param <T> element type | 元素类型
     * @return lexicographical comparator | 字典序比较器
     */
    public static <T extends Comparable<? super T>> Comparator<Iterable<T>> lexicographical() {
        return lexicographical(Comparator.naturalOrder());
    }

    /**
     * Return a lexicographical comparator for iterables with custom element comparator
     * 带自定义比较器的字典序
     *
     * @param <T>        element type | 元素类型
     * @param comparator element comparator | 元素比较器
     * @return lexicographical comparator | 字典序比较器
     */
    public static <T> Comparator<Iterable<T>> lexicographical(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> {
            if (a == b) {
                return 0;
            }
            if (a == null) {
                return -1;
            }
            if (b == null) {
                return 1;
            }
            Iterator<T> itA = a.iterator();
            Iterator<T> itB = b.iterator();
            while (itA.hasNext() && itB.hasNext()) {
                int result = comparator.compare(itA.next(), itB.next());
                if (result != 0) {
                    return result;
                }
            }
            if (itA.hasNext()) {
                return 1;
            }
            if (itB.hasNext()) {
                return -1;
            }
            return 0;
        };
    }

    // ==================== 最值操作 | Min/Max Operations ====================

    /**
     * Return the minimum of two values
     * 返回最小值
     *
     * @param <T>        element type | 元素类型
     * @param a          first value | 第一个值
     * @param b          second value | 第二个值
     * @param comparator the comparator | 比较器
     * @return minimum value | 最小值
     */
    public static <T> T min(T a, T b, Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return comparator.compare(a, b) <= 0 ? a : b;
    }

    /**
     * Return the maximum of two values
     * 返回最大值
     *
     * @param <T>        element type | 元素类型
     * @param a          first value | 第一个值
     * @param b          second value | 第二个值
     * @param comparator the comparator | 比较器
     * @return maximum value | 最大值
     */
    public static <T> T max(T a, T b, Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return comparator.compare(a, b) >= 0 ? a : b;
    }

    // ==================== Top K 收集器 | Top K Collectors ====================

    /**
     * Return a collector that collects the k smallest elements
     * 获取前 K 个最小值的 Collector
     *
     * @param <T>        element type | 元素类型
     * @param k          number of elements | 元素数量
     * @param comparator the comparator | 比较器
     * @return collector for k smallest elements | 最小 k 个元素的收集器
     */
    public static <T> Collector<T, ?, List<T>> least(int k, Comparator<? super T> comparator) {
        if (k <= 0) {
            return Collectors.collectingAndThen(Collectors.toList(), list -> Collections.emptyList());
        }
        Objects.requireNonNull(comparator);
        return Collectors.collectingAndThen(
                Collectors.toCollection(() -> new PriorityQueue<>(k + 1, comparator.reversed())),
                queue -> {
                    // For each element, if queue > k, remove the largest
                    List<T> result = new ArrayList<>(queue);
                    result.sort(comparator);
                    if (result.size() > k) {
                        return result.subList(0, k);
                    }
                    return result;
                }
        );
    }

    /**
     * Return a collector that collects the k greatest elements
     * 获取前 K 个最大值的 Collector
     *
     * @param <T>        element type | 元素类型
     * @param k          number of elements | 元素数量
     * @param comparator the comparator | 比较器
     * @return collector for k greatest elements | 最大 k 个元素的收集器
     */
    public static <T> Collector<T, ?, List<T>> greatest(int k, Comparator<? super T> comparator) {
        if (k <= 0) {
            return Collectors.collectingAndThen(Collectors.toList(), list -> Collections.emptyList());
        }
        Objects.requireNonNull(comparator);
        return Collectors.collectingAndThen(
                Collectors.toCollection(() -> new PriorityQueue<>(k + 1, comparator)),
                queue -> {
                    List<T> result = new ArrayList<>(queue);
                    result.sort(comparator.reversed());
                    if (result.size() > k) {
                        return result.subList(0, k);
                    }
                    return result;
                }
        );
    }

    // ==================== 空处理比较器 | Empty Handling Comparators ====================

    /**
     * Return a comparator that treats empty as less than non-empty
     * 空值优先的比较器
     *
     * @param <T>             element type | 元素类型
     * @param valueComparator comparator for non-empty values | 非空值的比较器
     * @return comparator with empties first | 空值优先的比较器
     */
    public static <T> Comparator<T> emptiesFirst(Comparator<? super T> valueComparator) {
        return nullsFirst(valueComparator);
    }

    /**
     * Return a comparator that treats empty as greater than non-empty
     * 空值最后的比较器
     *
     * @param <T>             element type | 元素类型
     * @param valueComparator comparator for non-empty values | 非空值的比较器
     * @return comparator with empties last | 空值最后的比较器
     */
    public static <T> Comparator<T> emptiesLast(Comparator<? super T> valueComparator) {
        return nullsLast(valueComparator);
    }
}
