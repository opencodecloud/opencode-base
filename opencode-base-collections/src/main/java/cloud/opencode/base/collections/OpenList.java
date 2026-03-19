package cloud.opencode.base.collections;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * OpenList - List Facade Utility Class
 * OpenList - 列表门面工具类
 *
 * <p>Provides comprehensive list operations including creation, transformation,
 * filtering, and searching.</p>
 * <p>提供全面的列表操作，包括创建、转换、过滤和搜索。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Factory methods for list creation - 列表创建工厂方法</li>
 *   <li>List transformation - 列表转换</li>
 *   <li>List filtering - 列表过滤</li>
 *   <li>List searching - 列表搜索</li>
 *   <li>List partitioning - 列表分区</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create list - 创建列表
 * List<String> list = OpenList.of("a", "b", "c");
 *
 * // Reverse - 反转
 * List<String> reversed = OpenList.reverse(list);
 *
 * // Partition - 分区
 * List<List<String>> partitions = OpenList.partition(list, 2);
 *
 * // Transform - 转换
 * List<Integer> lengths = OpenList.transform(list, String::length);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Most operations: O(n) - 大多数操作: O(n)</li>
 *   <li>Random access: O(1) - 随机访问: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class OpenList {

    private OpenList() {
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty ArrayList.
     * 创建空的 ArrayList。
     *
     * @param <E> element type | 元素类型
     * @return new empty ArrayList | 新的空 ArrayList
     */
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<>();
    }

    /**
     * Create an ArrayList with the given elements.
     * 创建包含给定元素的 ArrayList。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return new ArrayList | 新的 ArrayList
     */
    @SafeVarargs
    public static <E> ArrayList<E> of(E... elements) {
        ArrayList<E> list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }

    /**
     * Create an ArrayList from an Iterable.
     * 从 Iterable 创建 ArrayList。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return new ArrayList | 新的 ArrayList
     */
    public static <E> ArrayList<E> from(Iterable<? extends E> elements) {
        if (elements instanceof Collection<?> coll) {
            return new ArrayList<>((Collection<? extends E>) coll);
        }
        ArrayList<E> list = new ArrayList<>();
        elements.forEach(list::add);
        return list;
    }

    /**
     * Create an ArrayList with initial capacity.
     * 创建具有初始容量的 ArrayList。
     *
     * @param <E>      element type | 元素类型
     * @param capacity initial capacity | 初始容量
     * @return new ArrayList | 新的 ArrayList
     */
    public static <E> ArrayList<E> withCapacity(int capacity) {
        return new ArrayList<>(capacity);
    }

    /**
     * Create a LinkedList.
     * 创建 LinkedList。
     *
     * @param <E> element type | 元素类型
     * @return new LinkedList | 新的 LinkedList
     */
    public static <E> LinkedList<E> newLinkedList() {
        return new LinkedList<>();
    }

    /**
     * Create a LinkedList from an Iterable.
     * 从 Iterable 创建 LinkedList。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return new LinkedList | 新的 LinkedList
     */
    public static <E> LinkedList<E> linkedListFrom(Iterable<? extends E> elements) {
        LinkedList<E> list = new LinkedList<>();
        elements.forEach(list::add);
        return list;
    }

    // ==================== 视图操作 | View Operations ====================

    /**
     * Return a reversed view of the list.
     * 返回列表的反转视图。
     *
     * @param <E>  element type | 元素类型
     * @param list the list | 列表
     * @return reversed view | 反转视图
     */
    public static <E> List<E> reverse(List<E> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        return list.reversed();
    }

    /**
     * Return a partition view of the list.
     * 返回列表的分区视图。
     *
     * @param <E>  element type | 元素类型
     * @param list the list | 列表
     * @param size partition size | 分区大小
     * @return list of partitions | 分区列表
     */
    public static <E> List<List<E>> partition(List<E> list, int size) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Partition size must be positive: " + size);
        }
        List<List<E>> result = new ArrayList<>((list.size() + size - 1) / size);
        for (int i = 0; i < list.size(); i += size) {
            result.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return result;
    }

    /**
     * Return a transformed view of the list.
     * 返回列表的转换视图。
     *
     * @param <F>      input element type | 输入元素类型
     * @param <T>      output element type | 输出元素类型
     * @param list     the list | 列表
     * @param function the transform function | 转换函数
     * @return transformed list | 转换后的列表
     */
    public static <F, T> List<T> transform(List<F> list, Function<? super F, ? extends T> function) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return new TransformedList<>(list, function);
    }

    /**
     * Return a view of the string as a list of characters.
     * 返回字符串作为字符列表的视图。
     *
     * @param string the string | 字符串
     * @return list of characters | 字符列表
     */
    public static List<Character> charactersOf(String string) {
        if (string == null || string.isEmpty()) {
            return Collections.emptyList();
        }
        return new CharacterList(string);
    }

    // ==================== 查询操作 | Query Operations ====================

    /**
     * Get the first element of the list.
     * 获取列表的第一个元素。
     *
     * @param <E>  element type | 元素类型
     * @param list the list | 列表
     * @return first element or null if empty | 第一个元素，空列表返回 null
     */
    public static <E> E getFirst(List<E> list) {
        return (list == null || list.isEmpty()) ? null : list.getFirst();
    }

    /**
     * Get the first element of the list with default value.
     * 获取列表的第一个元素，带默认值。
     *
     * @param <E>          element type | 元素类型
     * @param list         the list | 列表
     * @param defaultValue default value | 默认值
     * @return first element or default | 第一个元素或默认值
     */
    public static <E> E getFirst(List<E> list, E defaultValue) {
        return (list == null || list.isEmpty()) ? defaultValue : list.getFirst();
    }

    /**
     * Get the last element of the list.
     * 获取列表的最后一个元素。
     *
     * @param <E>  element type | 元素类型
     * @param list the list | 列表
     * @return last element or null if empty | 最后一个元素，空列表返回 null
     */
    public static <E> E getLast(List<E> list) {
        return (list == null || list.isEmpty()) ? null : list.getLast();
    }

    /**
     * Get the last element of the list with default value.
     * 获取列表的最后一个元素，带默认值。
     *
     * @param <E>          element type | 元素类型
     * @param list         the list | 列表
     * @param defaultValue default value | 默认值
     * @return last element or default | 最后一个元素或默认值
     */
    public static <E> E getLast(List<E> list, E defaultValue) {
        return (list == null || list.isEmpty()) ? defaultValue : list.getLast();
    }

    /**
     * Find the first element matching the predicate.
     * 查找第一个匹配谓词的元素。
     *
     * @param <E>       element type | 元素类型
     * @param list      the list | 列表
     * @param predicate the predicate | 谓词
     * @return Optional containing the element or empty | 包含元素的 Optional 或空
     */
    public static <E> Optional<E> findFirst(List<E> list, Predicate<? super E> predicate) {
        if (list == null || list.isEmpty()) {
            return Optional.empty();
        }
        for (E element : list) {
            if (predicate.test(element)) {
                return Optional.of(element);
            }
        }
        return Optional.empty();
    }

    /**
     * Find all elements matching the predicate.
     * 查找所有匹配谓词的元素。
     *
     * @param <E>       element type | 元素类型
     * @param list      the list | 列表
     * @param predicate the predicate | 谓词
     * @return list of matching elements | 匹配元素的列表
     */
    public static <E> List<E> filter(List<E> list, Predicate<? super E> predicate) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<E> result = new ArrayList<>();
        for (E element : list) {
            if (predicate.test(element)) {
                result.add(element);
            }
        }
        return result;
    }

    // ==================== 笛卡尔积 | Cartesian Product ====================

    /**
     * Compute Cartesian product of lists.
     * 计算列表的笛卡尔积。
     *
     * @param <E>   element type | 元素类型
     * @param lists the lists | 列表
     * @return Cartesian product | 笛卡尔积
     */
    @SafeVarargs
    public static <E> List<List<E>> cartesianProduct(List<? extends E>... lists) {
        return cartesianProduct(Arrays.asList(lists));
    }

    /**
     * Compute Cartesian product of lists.
     * 计算列表的笛卡尔积。
     *
     * @param <E>   element type | 元素类型
     * @param lists the lists | 列表
     * @return Cartesian product | 笛卡尔积
     */
    public static <E> List<List<E>> cartesianProduct(List<? extends List<? extends E>> lists) {
        if (lists == null || lists.isEmpty()) {
            return Collections.singletonList(Collections.emptyList());
        }
        List<List<E>> result = new ArrayList<>();
        result.add(new ArrayList<>());

        for (List<? extends E> list : lists) {
            List<List<E>> newResult = new ArrayList<>();
            for (List<E> existing : result) {
                for (E element : list) {
                    List<E> combo = new ArrayList<>(existing);
                    combo.add(element);
                    newResult.add(combo);
                }
            }
            result = newResult;
        }
        return result;
    }

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Transformed list view.
     * 转换后的列表视图。
     */
    private static class TransformedList<F, T> extends AbstractList<T> implements RandomAccess {
        private final List<F> fromList;
        private final Function<? super F, ? extends T> function;

        TransformedList(List<F> fromList, Function<? super F, ? extends T> function) {
            this.fromList = fromList;
            this.function = function;
        }

        @Override
        public T get(int index) {
            return function.apply(fromList.get(index));
        }

        @Override
        public int size() {
            return fromList.size();
        }
    }

    /**
     * Character list view of a string.
     * 字符串的字符列表视图。
     */
    private static class CharacterList extends AbstractList<Character> implements RandomAccess {
        private final String string;

        CharacterList(String string) {
            this.string = string;
        }

        @Override
        public Character get(int index) {
            return string.charAt(index);
        }

        @Override
        public int size() {
            return string.length();
        }
    }
}
