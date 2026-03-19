package cloud.opencode.base.collections.transform;

import java.util.*;
import java.util.function.Predicate;

/**
 * PartitionUtil - Collection Partitioning Utilities
 * PartitionUtil - 集合分区工具
 *
 * <p>Provides utilities for partitioning collections into groups.</p>
 * <p>提供将集合分区为组的工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Partition by predicate - 按谓词分区</li>
 *   <li>Partition by size - 按大小分区</li>
 *   <li>Partition into n parts - 分区为 n 部分</li>
 *   <li>Sliding window partitions - 滑动窗口分区</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
 *
 * // Partition by predicate - 按谓词分区
 * Map<Boolean, List<Integer>> evenOdd = PartitionUtil.partition(numbers, n -> n % 2 == 0);
 *
 * // Partition by size - 按大小分区
 * List<List<Integer>> chunks = PartitionUtil.partitionBySize(numbers, 3);
 * // [[1,2,3], [4,5,6], [7,8,9], [10]]
 *
 * // Partition into n parts - 分区为 n 部分
 * List<List<Integer>> parts = PartitionUtil.partitionIntoN(numbers, 3);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 是（无状态工具）</li>
 *   <li>Null-safe: No (input must not be null) - 否（输入不能为null）</li>
 * </ul>
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for partition and partitionBySize; O(n*w) for slidingWindow where w is the window size - 时间复杂度: partition和partitionBySize为O(n)；slidingWindow为O(n*w)，w为窗口大小</li>
 *   <li>Space complexity: O(n) for the resulting partitions - 空间复杂度: O(n)，存储分区结果</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class PartitionUtil {

    private PartitionUtil() {
    }

    // ==================== 谓词分区 | Predicate Partition ====================

    /**
     * Partition a collection by a predicate.
     * 按谓词分区集合。
     *
     * @param <T>       element type | 元素类型
     * @param items     the items | 项目
     * @param predicate the predicate | 谓词
     * @return map with true/false keys | 带有 true/false 键的映射
     */
    public static <T> Map<Boolean, List<T>> partition(Collection<T> items, Predicate<? super T> predicate) {
        Objects.requireNonNull(items);
        Objects.requireNonNull(predicate);

        Map<Boolean, List<T>> result = new HashMap<>();
        result.put(true, new ArrayList<>());
        result.put(false, new ArrayList<>());

        for (T item : items) {
            result.get(predicate.test(item)).add(item);
        }
        return result;
    }

    /**
     * Partition a collection by multiple predicates.
     * 按多个谓词分区集合。
     *
     * @param <T>        element type | 元素类型
     * @param items      the items | 项目
     * @param predicates the predicates | 谓词
     * @return list of partitions | 分区列表
     */
    @SafeVarargs
    public static <T> List<List<T>> partitionBy(Collection<T> items, Predicate<? super T>... predicates) {
        Objects.requireNonNull(items);
        Objects.requireNonNull(predicates);

        List<List<T>> result = new ArrayList<>(predicates.length + 1);
        for (int i = 0; i <= predicates.length; i++) {
            result.add(new ArrayList<>());
        }

        outer:
        for (T item : items) {
            for (int i = 0; i < predicates.length; i++) {
                if (predicates[i].test(item)) {
                    result.get(i).add(item);
                    continue outer;
                }
            }
            result.get(predicates.length).add(item);
        }
        return result;
    }

    // ==================== 大小分区 | Size Partition ====================

    /**
     * Partition a list into chunks of specified size.
     * 将列表分区为指定大小的块。
     *
     * @param <T>  element type | 元素类型
     * @param list the list | 列表
     * @param size the chunk size | 块大小
     * @return list of chunks | 块列表
     */
    public static <T> List<List<T>> partitionBySize(List<T> list, int size) {
        Objects.requireNonNull(list);
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }

        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            result.add(new ArrayList<>(list.subList(i, Math.min(i + size, list.size()))));
        }
        return result;
    }

    /**
     * Partition an iterable into chunks of specified size.
     * 将可迭代对象分区为指定大小的块。
     *
     * @param <T>      element type | 元素类型
     * @param iterable the iterable | 可迭代对象
     * @param size     the chunk size | 块大小
     * @return list of chunks | 块列表
     */
    public static <T> List<List<T>> partitionBySize(Iterable<T> iterable, int size) {
        Objects.requireNonNull(iterable);
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }

        List<List<T>> result = new ArrayList<>();
        List<T> chunk = new ArrayList<>(size);

        for (T item : iterable) {
            chunk.add(item);
            if (chunk.size() == size) {
                result.add(chunk);
                chunk = new ArrayList<>(size);
            }
        }

        if (!chunk.isEmpty()) {
            result.add(chunk);
        }
        return result;
    }

    /**
     * Partition a list into n approximately equal parts.
     * 将列表分区为 n 个大致相等的部分。
     *
     * @param <T>  element type | 元素类型
     * @param list the list | 列表
     * @param n    the number of parts | 部分数
     * @return list of parts | 部分列表
     */
    public static <T> List<List<T>> partitionIntoN(List<T> list, int n) {
        Objects.requireNonNull(list);
        if (n <= 0) {
            throw new IllegalArgumentException("N must be positive");
        }

        List<List<T>> result = new ArrayList<>(n);
        int size = list.size();
        int baseSize = size / n;
        int remainder = size % n;

        int index = 0;
        for (int i = 0; i < n; i++) {
            int partSize = baseSize + (i < remainder ? 1 : 0);
            if (partSize > 0) {
                result.add(new ArrayList<>(list.subList(index, index + partSize)));
                index += partSize;
            } else {
                result.add(new ArrayList<>());
            }
        }
        return result;
    }

    // ==================== 滑动窗口 | Sliding Window ====================

    /**
     * Create sliding windows over a list.
     * 在列表上创建滑动窗口。
     *
     * @param <T>        element type | 元素类型
     * @param list       the list | 列表
     * @param windowSize the window size | 窗口大小
     * @return list of windows | 窗口列表
     */
    public static <T> List<List<T>> slidingWindow(List<T> list, int windowSize) {
        return slidingWindow(list, windowSize, 1);
    }

    /**
     * Create sliding windows over a list with specified step.
     * 在列表上创建指定步长的滑动窗口。
     *
     * @param <T>        element type | 元素类型
     * @param list       the list | 列表
     * @param windowSize the window size | 窗口大小
     * @param step       the step size | 步长
     * @return list of windows | 窗口列表
     */
    public static <T> List<List<T>> slidingWindow(List<T> list, int windowSize, int step) {
        Objects.requireNonNull(list);
        if (windowSize <= 0) {
            throw new IllegalArgumentException("Window size must be positive");
        }
        if (step <= 0) {
            throw new IllegalArgumentException("Step must be positive");
        }

        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i <= list.size() - windowSize; i += step) {
            result.add(new ArrayList<>(list.subList(i, i + windowSize)));
        }
        return result;
    }

    // ==================== 特殊分区 | Special Partition ====================

    /**
     * Partition into head and tail.
     * 分区为头部和尾部。
     *
     * @param <T>  element type | 元素类型
     * @param list the list | 列表
     * @return map with "head" and "tail" keys | 带有 "head" 和 "tail" 键的映射
     */
    public static <T> Map<String, Object> headTail(List<T> list) {
        Objects.requireNonNull(list);
        Map<String, Object> result = new HashMap<>();
        if (list.isEmpty()) {
            result.put("head", null);
            result.put("tail", Collections.emptyList());
        } else {
            result.put("head", list.getFirst());
            result.put("tail", list.size() > 1 ? new ArrayList<>(list.subList(1, list.size())) : Collections.emptyList());
        }
        return result;
    }

    /**
     * Partition into init and last.
     * 分区为初始部分和最后元素。
     *
     * @param <T>  element type | 元素类型
     * @param list the list | 列表
     * @return map with "init" and "last" keys | 带有 "init" 和 "last" 键的映射
     */
    public static <T> Map<String, Object> initLast(List<T> list) {
        Objects.requireNonNull(list);
        Map<String, Object> result = new HashMap<>();
        if (list.isEmpty()) {
            result.put("init", Collections.emptyList());
            result.put("last", null);
        } else {
            result.put("init", list.size() > 1 ? new ArrayList<>(list.subList(0, list.size() - 1)) : Collections.emptyList());
            result.put("last", list.getLast());
        }
        return result;
    }

    /**
     * Split at specified index.
     * 在指定索引处分割。
     *
     * @param <T>   element type | 元素类型
     * @param list  the list | 列表
     * @param index the index | 索引
     * @return list of two parts | 两部分的列表
     */
    public static <T> List<List<T>> splitAt(List<T> list, int index) {
        Objects.requireNonNull(list);
        if (index < 0 || index > list.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + list.size());
        }

        List<List<T>> result = new ArrayList<>(2);
        result.add(new ArrayList<>(list.subList(0, index)));
        result.add(new ArrayList<>(list.subList(index, list.size())));
        return result;
    }

    /**
     * Take while predicate is true.
     * 在谓词为真时获取。
     *
     * @param <T>       element type | 元素类型
     * @param list      the list | 列表
     * @param predicate the predicate | 谓词
     * @return list of taken elements | 获取的元素列表
     */
    public static <T> List<T> takeWhile(List<T> list, Predicate<? super T> predicate) {
        Objects.requireNonNull(list);
        Objects.requireNonNull(predicate);

        List<T> result = new ArrayList<>();
        for (T item : list) {
            if (predicate.test(item)) {
                result.add(item);
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * Drop while predicate is true.
     * 在谓词为真时丢弃。
     *
     * @param <T>       element type | 元素类型
     * @param list      the list | 列表
     * @param predicate the predicate | 谓词
     * @return remaining elements | 剩余元素
     */
    public static <T> List<T> dropWhile(List<T> list, Predicate<? super T> predicate) {
        Objects.requireNonNull(list);
        Objects.requireNonNull(predicate);

        int index = 0;
        for (T item : list) {
            if (!predicate.test(item)) {
                break;
            }
            index++;
        }
        return new ArrayList<>(list.subList(index, list.size()));
    }

    /**
     * Span - partition into takeWhile and dropWhile.
     * Span - 分区为 takeWhile 和 dropWhile。
     *
     * @param <T>       element type | 元素类型
     * @param list      the list | 列表
     * @param predicate the predicate | 谓词
     * @return list of two parts | 两部分的列表
     */
    public static <T> List<List<T>> span(List<T> list, Predicate<? super T> predicate) {
        Objects.requireNonNull(list);
        Objects.requireNonNull(predicate);

        List<List<T>> result = new ArrayList<>(2);
        result.add(takeWhile(list, predicate));
        result.add(dropWhile(list, predicate));
        return result;
    }
}
