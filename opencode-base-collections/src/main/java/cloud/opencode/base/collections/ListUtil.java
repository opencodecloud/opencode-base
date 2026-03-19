package cloud.opencode.base.collections;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * ListUtil - List Utility Class
 * ListUtil - 列表工具类
 *
 * <p>Provides factory methods and operations for List implementations including
 * ArrayList, LinkedList, and CopyOnWriteArrayList.</p>
 * <p>提供 List 实现的工厂方法和操作，包括 ArrayList、LinkedList 和 CopyOnWriteArrayList。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Factory methods for list creation - 列表创建工厂方法</li>
 *   <li>View operations (reverse, partition, transform) - 视图操作（反转、分区、转换）</li>
 *   <li>Cartesian product - 笛卡尔积</li>
 *   <li>Character list from strings - 字符列表</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create ArrayList - 创建 ArrayList
 * List<String> list = ListUtil.newArrayList("a", "b", "c");
 *
 * // Reverse view - 反转视图
 * List<String> reversed = ListUtil.reverse(list);
 *
 * // Partition - 分区
 * List<List<String>> partitions = ListUtil.partition(list, 2);
 *
 * // Transform view - 转换视图
 * List<Integer> lengths = ListUtil.transform(list, String::length);
 *
 * // Cartesian product - 笛卡尔积
 * List<List<String>> product = ListUtil.cartesianProduct(list1, list2);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Factory methods: O(n) - 工厂方法: O(n)</li>
 *   <li>View operations: O(1) creation, O(n) iteration - 视图操作: O(1) 创建, O(n) 遍历</li>
 *   <li>Cartesian product: O(n^k) for k lists - 笛卡尔积: O(n^k) 对于 k 个列表</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (except CopyOnWriteArrayList) - 线程安全: 否（除了 CopyOnWriteArrayList）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class ListUtil {

    private ListUtil() {
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create a new ArrayList
     * 创建 ArrayList
     *
     * @param <E> element type | 元素类型
     * @return new ArrayList | 新的 ArrayList
     */
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<>();
    }

    /**
     * Create a new ArrayList with initial elements
     * 创建带初始元素的 ArrayList
     *
     * @param <E>      element type | 元素类型
     * @param elements initial elements | 初始元素
     * @return new ArrayList | 新的 ArrayList
     */
    @SafeVarargs
    public static <E> ArrayList<E> newArrayList(E... elements) {
        if (elements == null || elements.length == 0) {
            return new ArrayList<>();
        }
        ArrayList<E> list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }

    /**
     * Create a new ArrayList from an Iterable
     * 从 Iterable 创建 ArrayList
     *
     * @param <E>      element type | 元素类型
     * @param elements elements | 元素
     * @return new ArrayList | 新的 ArrayList
     */
    public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements) {
        if (elements == null) {
            return new ArrayList<>();
        }
        if (elements instanceof Collection<? extends E> collection) {
            return new ArrayList<>(collection);
        }
        ArrayList<E> list = new ArrayList<>();
        for (E e : elements) {
            list.add(e);
        }
        return list;
    }

    /**
     * Create a new ArrayList with initial capacity
     * 创建指定容量的 ArrayList
     *
     * @param <E>             element type | 元素类型
     * @param initialCapacity initial capacity | 初始容量
     * @return new ArrayList | 新的 ArrayList
     */
    public static <E> ArrayList<E> newArrayListWithCapacity(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Initial capacity cannot be negative: " + initialCapacity);
        }
        return new ArrayList<>(initialCapacity);
    }

    /**
     * Create a new ArrayList with expected size
     * 创建预期大小的 ArrayList
     *
     * @param <E>          element type | 元素类型
     * @param expectedSize expected size | 预期大小
     * @return new ArrayList | 新的 ArrayList
     */
    public static <E> ArrayList<E> newArrayListWithExpectedSize(int expectedSize) {
        if (expectedSize < 0) {
            throw new IllegalArgumentException("Expected size cannot be negative: " + expectedSize);
        }
        // Add 10% buffer for potential growth, guarding against overflow
        long capacity = (long) expectedSize + expectedSize / 10 + 1;
        return new ArrayList<>((int) Math.min(capacity, Integer.MAX_VALUE));
    }

    /**
     * Create a new LinkedList
     * 创建 LinkedList
     *
     * @param <E> element type | 元素类型
     * @return new LinkedList | 新的 LinkedList
     */
    public static <E> LinkedList<E> newLinkedList() {
        return new LinkedList<>();
    }

    /**
     * Create a new LinkedList from an Iterable
     * 从 Iterable 创建 LinkedList
     *
     * @param <E>      element type | 元素类型
     * @param elements elements | 元素
     * @return new LinkedList | 新的 LinkedList
     */
    public static <E> LinkedList<E> newLinkedList(Iterable<? extends E> elements) {
        LinkedList<E> list = new LinkedList<>();
        if (elements != null) {
            for (E e : elements) {
                list.add(e);
            }
        }
        return list;
    }

    /**
     * Create a new CopyOnWriteArrayList
     * 创建 CopyOnWriteArrayList
     *
     * @param <E> element type | 元素类型
     * @return new CopyOnWriteArrayList | 新的 CopyOnWriteArrayList
     */
    public static <E> CopyOnWriteArrayList<E> newCopyOnWriteArrayList() {
        return new CopyOnWriteArrayList<>();
    }

    /**
     * Create a new CopyOnWriteArrayList from an Iterable
     * 从 Iterable 创建 CopyOnWriteArrayList
     *
     * @param <E>      element type | 元素类型
     * @param elements elements | 元素
     * @return new CopyOnWriteArrayList | 新的 CopyOnWriteArrayList
     */
    public static <E> CopyOnWriteArrayList<E> newCopyOnWriteArrayList(Iterable<? extends E> elements) {
        CopyOnWriteArrayList<E> list = new CopyOnWriteArrayList<>();
        if (elements != null) {
            for (E e : elements) {
                list.add(e);
            }
        }
        return list;
    }

    // ==================== 视图操作 | View Operations ====================

    /**
     * Return a reversed view of the list
     * 反转视图
     *
     * @param <E>  element type | 元素类型
     * @param list the list | 列表
     * @return reversed view | 反转视图
     */
    public static <E> List<E> reverse(List<E> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.reversed();
    }

    /**
     * Return a partitioned view of the list
     * 分区视图
     *
     * @param <E>  element type | 元素类型
     * @param list the list | 列表
     * @param size partition size | 分区大小
     * @return list of partitions | 分区列表
     */
    public static <E> List<List<E>> partition(List<E> list, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive: " + size);
        }
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return new PartitionedList<>(list, size);
    }

    /**
     * Return a transformed view of the list
     * 转换视图
     *
     * @param <F>      from type | 源类型
     * @param <T>      to type | 目标类型
     * @param fromList the source list | 源列表
     * @param function the transform function | 转换函数
     * @return transformed view | 转换视图
     */
    public static <F, T> List<T> transform(List<F> fromList, Function<? super F, ? extends T> function) {
        if (fromList == null || function == null) {
            return Collections.emptyList();
        }
        return new TransformedList<>(fromList, function);
    }

    /**
     * Return a character list view of a string
     * 字符列表视图
     *
     * @param string the string | 字符串
     * @return character list | 字符列表
     */
    public static List<Character> charactersOf(String string) {
        if (string == null || string.isEmpty()) {
            return Collections.emptyList();
        }
        return new CharacterList(string);
    }

    /**
     * Return a character list view of a CharSequence
     * 字符列表视图
     *
     * @param sequence the CharSequence | 字符序列
     * @return character list | 字符列表
     */
    public static List<Character> charactersOf(CharSequence sequence) {
        if (sequence == null || sequence.isEmpty()) {
            return Collections.emptyList();
        }
        return new CharSequenceList(sequence);
    }

    // ==================== 笛卡尔积 | Cartesian Product ====================

    /**
     * Compute the Cartesian product of lists
     * 计算笛卡尔积
     *
     * @param <E>   element type | 元素类型
     * @param lists lists to compute product | 要计算积的列表
     * @return Cartesian product | 笛卡尔积
     */
    public static <E> List<List<E>> cartesianProduct(List<? extends List<? extends E>> lists) {
        if (lists == null || lists.isEmpty()) {
            List<List<E>> result = new ArrayList<>();
            result.add(Collections.emptyList());
            return result;
        }
        for (List<? extends E> list : lists) {
            if (list == null || list.isEmpty()) {
                return Collections.emptyList();
            }
        }
        return new CartesianProductList<>(lists);
    }

    /**
     * Compute the Cartesian product of lists
     * 计算笛卡尔积
     *
     * @param <E>   element type | 元素类型
     * @param lists lists to compute product | 要计算积的列表
     * @return Cartesian product | 笛卡尔积
     */
    @SafeVarargs
    public static <E> List<List<E>> cartesianProduct(List<? extends E>... lists) {
        if (lists == null || lists.length == 0) {
            List<List<E>> result = new ArrayList<>();
            result.add(Collections.emptyList());
            return result;
        }
        return cartesianProduct(Arrays.asList(lists));
    }

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Partitioned list view
     */
    private static class PartitionedList<E> extends AbstractList<List<E>> {
        private final List<E> list;
        private final int size;

        PartitionedList(List<E> list, int size) {
            this.list = list;
            this.size = size;
        }

        @Override
        public List<E> get(int index) {
            int start = index * size;
            int end = Math.min(start + size, list.size());
            if (start >= list.size()) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
            }
            return list.subList(start, end);
        }

        @Override
        public int size() {
            return (list.size() + size - 1) / size;
        }
    }

    /**
     * Transformed list view
     */
    private static class TransformedList<F, T> extends AbstractList<T> {
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
     * Character list from String
     */
    private static class CharacterList extends AbstractList<Character> {
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

    /**
     * Character list from CharSequence
     */
    private static class CharSequenceList extends AbstractList<Character> {
        private final CharSequence sequence;

        CharSequenceList(CharSequence sequence) {
            this.sequence = sequence;
        }

        @Override
        public Character get(int index) {
            return sequence.charAt(index);
        }

        @Override
        public int size() {
            return sequence.length();
        }
    }

    /**
     * Cartesian product list
     */
    private static class CartesianProductList<E> extends AbstractList<List<E>> {
        private final List<? extends List<? extends E>> lists;
        private final int size;

        CartesianProductList(List<? extends List<? extends E>> lists) {
            this.lists = lists;
            long size = 1;
            for (List<? extends E> list : lists) {
                size *= list.size();
                if (size > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException("Cartesian product too large");
                }
            }
            this.size = (int) size;
        }

        @Override
        public List<E> get(int index) {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
            }
            List<E> result = new ArrayList<>(lists.size());
            for (int i = lists.size() - 1; i >= 0; i--) {
                List<? extends E> list = lists.get(i);
                result.addFirst(list.get(index % list.size()));
                index /= list.size();
            }
            return result;
        }

        @Override
        public int size() {
            return size;
        }
    }
}
