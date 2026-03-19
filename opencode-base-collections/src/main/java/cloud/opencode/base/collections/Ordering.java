package cloud.opencode.base.collections;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

/**
 * Ordering - Fluent Comparator Builder
 * Ordering - 流式比较器构建器
 *
 * <p>A fluent API for building complex comparators with method chaining.
 * Provides natural ordering, null handling, reverse, compound, and transform operations.</p>
 * <p>用于通过方法链构建复杂比较器的流式 API。提供自然排序、空值处理、反转、复合和转换操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Natural ordering - 自然排序</li>
 *   <li>Null handling (nullsFirst, nullsLast) - 空值处理</li>
 *   <li>Reverse ordering - 反转排序</li>
 *   <li>Compound ordering - 复合排序</li>
 *   <li>Transform-based ordering - 基于转换的排序</li>
 *   <li>Min/max operations - 最小/最大操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Natural ordering - 自然排序
 * Ordering<String> natural = Ordering.natural();
 *
 * // Reverse ordering - 反转排序
 * Ordering<String> reversed = Ordering.natural().reverse();
 *
 * // Null-safe ordering - 空值安全排序
 * Ordering<String> nullsFirst = Ordering.natural().nullsFirst();
 *
 * // Compound ordering - 复合排序
 * Ordering<Person> byAge = Ordering.natural().onResultOf(Person::getAge);
 * Ordering<Person> byName = Ordering.natural().onResultOf(Person::getName);
 * Ordering<Person> compound = byAge.compound(byName);
 *
 * // Sort collection - 排序集合
 * List<String> sorted = Ordering.natural().sortedCopy(strings);
 *
 * // Min/max - 最小/最大
 * String min = Ordering.natural().min(strings);
 * String max = Ordering.natural().max(strings);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>compare: O(1) to O(k) where k is compound depth - compare: O(1) 到 O(k)，k 是复合深度</li>
 *   <li>sortedCopy: O(n log n) - sortedCopy: O(n log n)</li>
 *   <li>min/max: O(n) - min/max: O(n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Configurable - 空值安全: 可配置</li>
 * </ul>
 *
 * @param <T> the type to compare | 要比较的类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public abstract class Ordering<T> implements Comparator<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Return natural ordering for Comparable types.
     * 返回 Comparable 类型的自然排序。
     *
     * @param <C> the Comparable type | Comparable 类型
     * @return natural ordering | 自然排序
     */
    public static <C extends Comparable<? super C>> Ordering<C> natural() {
        return new NaturalOrdering<>();
    }

    /**
     * Return ordering based on a Comparator.
     * 返回基于 Comparator 的排序。
     *
     * @param <T>        the type | 类型
     * @param comparator the comparator | 比较器
     * @return ordering | 排序
     */
    public static <T> Ordering<T> from(Comparator<T> comparator) {
        Objects.requireNonNull(comparator);
        if (comparator instanceof Ordering) {
            @SuppressWarnings("unchecked")
            Ordering<T> ordering = (Ordering<T>) comparator;
            return ordering;
        }
        return new ComparatorOrdering<>(comparator);
    }

    /**
     * Return explicit ordering based on provided order of elements.
     * 返回基于提供的元素顺序的显式排序。
     *
     * @param <T>           the type | 类型
     * @param valuesInOrder values in desired order | 期望顺序的值
     * @return explicit ordering | 显式排序
     */
    @SafeVarargs
    public static <T> Ordering<T> explicit(T... valuesInOrder) {
        return explicit(Arrays.asList(valuesInOrder));
    }

    /**
     * Return explicit ordering based on provided order of elements.
     * 返回基于提供的元素顺序的显式排序。
     *
     * @param <T>           the type | 类型
     * @param valuesInOrder values in desired order | 期望顺序的值
     * @return explicit ordering | 显式排序
     */
    public static <T> Ordering<T> explicit(List<T> valuesInOrder) {
        return new ExplicitOrdering<>(valuesInOrder);
    }

    /**
     * Return ordering that considers all values equal.
     * 返回将所有值视为相等的排序。
     *
     * @param <T> the type | 类型
     * @return all-equal ordering | 全相等排序
     */
    public static <T> Ordering<T> allEqual() {
        return new AllEqualOrdering<>();
    }

    /**
     * Return arbitrary but consistent ordering based on identity hash code.
     * 返回基于身份哈希码的任意但一致的排序。
     *
     * @return arbitrary ordering | 任意排序
     */
    public static Ordering<Object> arbitrary() {
        return new ArbitraryOrdering();
    }

    /**
     * Return ordering that compares using toString().
     * 返回使用 toString() 比较的排序。
     *
     * @return toString ordering | toString 排序
     */
    public static Ordering<Object> usingToString() {
        return new UsingToStringOrdering();
    }

    // ==================== 修饰方法 | Modifier Methods ====================

    /**
     * Return reverse ordering.
     * 返回反转排序。
     *
     * @return reversed ordering | 反转排序
     */
    public Ordering<T> reverse() {
        return new ReverseOrdering<>(this);
    }

    /**
     * Return ordering that places nulls first.
     * 返回将空值放在前面的排序。
     *
     * @return nulls-first ordering | 空值优先排序
     */
    public Ordering<T> nullsFirst() {
        return new NullsFirstOrdering<>(this);
    }

    /**
     * Return ordering that places nulls last.
     * 返回将空值放在后面的排序。
     *
     * @return nulls-last ordering | 空值最后排序
     */
    public Ordering<T> nullsLast() {
        return new NullsLastOrdering<>(this);
    }

    /**
     * Return compound ordering.
     * 返回复合排序。
     *
     * @param secondary secondary ordering | 次要排序
     * @return compound ordering | 复合排序
     */
    public Ordering<T> compound(Comparator<? super T> secondary) {
        return new CompoundOrdering<>(this, secondary);
    }

    /**
     * Return ordering based on a function result.
     * 返回基于函数结果的排序。
     *
     * @param <F>      the type to order | 要排序的类型
     * @param function the function | 函数
     * @return transformed ordering | 转换排序
     */
    public <F> Ordering<F> onResultOf(Function<F, ? extends T> function) {
        return new ByFunctionOrdering<>(function, this);
    }

    // ==================== 操作方法 | Operation Methods ====================

    /**
     * Return a sorted copy of the elements.
     * 返回元素的排序副本。
     *
     * @param elements elements to sort | 要排序的元素
     * @return sorted list | 排序列表
     */
    public List<T> sortedCopy(Iterable<T> elements) {
        List<T> list = new ArrayList<>();
        for (T e : elements) {
            list.add(e);
        }
        list.sort(this);
        return list;
    }

    /**
     * Return an immutable sorted copy of the elements.
     * 返回元素的不可变排序副本。
     *
     * @param elements elements to sort | 要排序的元素
     * @return immutable sorted list | 不可变排序列表
     */
    public ImmutableList<T> immutableSortedCopy(Iterable<T> elements) {
        return ImmutableList.copyOf(sortedCopy(elements));
    }

    /**
     * Check if elements are in sorted order.
     * 检查元素是否有序。
     *
     * @param iterable elements to check | 要检查的元素
     * @return true if ordered | 如果有序则返回 true
     */
    public boolean isOrdered(Iterable<? extends T> iterable) {
        Iterator<? extends T> it = iterable.iterator();
        if (!it.hasNext()) {
            return true;
        }
        T prev = it.next();
        while (it.hasNext()) {
            T current = it.next();
            if (compare(prev, current) > 0) {
                return false;
            }
            prev = current;
        }
        return true;
    }

    /**
     * Check if elements are in strict sorted order.
     * 检查元素是否严格有序。
     *
     * @param iterable elements to check | 要检查的元素
     * @return true if strictly ordered | 如果严格有序则返回 true
     */
    public boolean isStrictlyOrdered(Iterable<? extends T> iterable) {
        Iterator<? extends T> it = iterable.iterator();
        if (!it.hasNext()) {
            return true;
        }
        T prev = it.next();
        while (it.hasNext()) {
            T current = it.next();
            if (compare(prev, current) >= 0) {
                return false;
            }
            prev = current;
        }
        return true;
    }

    /**
     * Return the minimum element from iterator.
     * 从迭代器返回最小元素。
     *
     * @param iterator elements | 元素
     * @return minimum element | 最小元素
     * @throws NoSuchElementException if empty | 如果为空
     */
    public T min(Iterator<T> iterator) {
        if (!iterator.hasNext()) {
            throw new NoSuchElementException();
        }
        T min = iterator.next();
        while (iterator.hasNext()) {
            T current = iterator.next();
            if (compare(current, min) < 0) {
                min = current;
            }
        }
        return min;
    }

    /**
     * Return the minimum element.
     * 返回最小元素。
     *
     * @param iterable elements | 元素
     * @return minimum element | 最小元素
     * @throws NoSuchElementException if empty | 如果为空
     */
    public T min(Iterable<T> iterable) {
        return min(iterable.iterator());
    }

    /**
     * Return the minimum of two values.
     * 返回两个值中的最小值。
     *
     * @param a first value | 第一个值
     * @param b second value | 第二个值
     * @return minimum value | 最小值
     */
    public T min(T a, T b) {
        return compare(a, b) <= 0 ? a : b;
    }

    /**
     * Return the minimum of multiple values.
     * 返回多个值中的最小值。
     *
     * @param a    first value | 第一个值
     * @param b    second value | 第二个值
     * @param c    third value | 第三个值
     * @param rest remaining values | 其余值
     * @return minimum value | 最小值
     */
    @SafeVarargs
    public final T min(T a, T b, T c, T... rest) {
        T min = min(min(a, b), c);
        for (T value : rest) {
            min = min(min, value);
        }
        return min;
    }

    /**
     * Return the maximum element from iterator.
     * 从迭代器返回最大元素。
     *
     * @param iterator elements | 元素
     * @return maximum element | 最大元素
     * @throws NoSuchElementException if empty | 如果为空
     */
    public T max(Iterator<T> iterator) {
        if (!iterator.hasNext()) {
            throw new NoSuchElementException();
        }
        T max = iterator.next();
        while (iterator.hasNext()) {
            T current = iterator.next();
            if (compare(current, max) > 0) {
                max = current;
            }
        }
        return max;
    }

    /**
     * Return the maximum element.
     * 返回最大元素。
     *
     * @param iterable elements | 元素
     * @return maximum element | 最大元素
     * @throws NoSuchElementException if empty | 如果为空
     */
    public T max(Iterable<T> iterable) {
        return max(iterable.iterator());
    }

    /**
     * Return the maximum of two values.
     * 返回两个值中的最大值。
     *
     * @param a first value | 第一个值
     * @param b second value | 第二个值
     * @return maximum value | 最大值
     */
    public T max(T a, T b) {
        return compare(a, b) >= 0 ? a : b;
    }

    /**
     * Return the maximum of multiple values.
     * 返回多个值中的最大值。
     *
     * @param a    first value | 第一个值
     * @param b    second value | 第二个值
     * @param c    third value | 第三个值
     * @param rest remaining values | 其余值
     * @return maximum value | 最大值
     */
    @SafeVarargs
    public final T max(T a, T b, T c, T... rest) {
        T max = max(max(a, b), c);
        for (T value : rest) {
            max = max(max, value);
        }
        return max;
    }

    /**
     * Return the least k elements from iterator.
     * 从迭代器返回最小的 k 个元素。
     *
     * @param iterator elements | 元素
     * @param k        number of elements | 元素数量
     * @return least k elements | 最小的 k 个元素
     */
    public List<T> leastOf(Iterator<T> iterator, int k) {
        List<T> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return leastOf(list, k);
    }

    /**
     * Return the least k elements.
     * 返回最小的 k 个元素。
     *
     * @param iterable elements | 元素
     * @param k        number of elements | 元素数量
     * @return least k elements | 最小的 k 个元素
     */
    public List<T> leastOf(Iterable<T> iterable, int k) {
        if (k <= 0) {
            return new ArrayList<>();
        }
        List<T> result = sortedCopy(iterable);
        if (result.size() > k) {
            return result.subList(0, k);
        }
        return result;
    }

    /**
     * Return the greatest k elements from iterator.
     * 从迭代器返回最大的 k 个元素。
     *
     * @param iterator elements | 元素
     * @param k        number of elements | 元素数量
     * @return greatest k elements | 最大的 k 个元素
     */
    public List<T> greatestOf(Iterator<T> iterator, int k) {
        return reverse().leastOf(iterator, k);
    }

    /**
     * Return the greatest k elements.
     * 返回最大的 k 个元素。
     *
     * @param iterable elements | 元素
     * @param k        number of elements | 元素数量
     * @return greatest k elements | 最大的 k 个元素
     */
    public List<T> greatestOf(Iterable<T> iterable, int k) {
        return reverse().leastOf(iterable, k);
    }

    /**
     * Perform binary search on a sorted list.
     * 在排序列表上执行二分查找。
     *
     * @param sortedList the sorted list | 排序列表
     * @param key        the key to search | 要搜索的键
     * @return index of the key, or -(insertion point + 1) | 键的索引，或 -(插入点 + 1)
     */
    public int binarySearch(List<? extends T> sortedList, T key) {
        return Collections.binarySearch(sortedList, key, this);
    }

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Natural ordering
     */
    private static class NaturalOrdering<C extends Comparable<? super C>> extends Ordering<C> {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(C left, C right) {
            return left.compareTo(right);
        }
    }

    /**
     * Comparator-based ordering
     */
    private static class ComparatorOrdering<T> extends Ordering<T> {
        @Serial
        private static final long serialVersionUID = 1L;
        private final Comparator<T> comparator;

        ComparatorOrdering(Comparator<T> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(T left, T right) {
            return comparator.compare(left, right);
        }
    }

    /**
     * Explicit ordering
     */
    private static class ExplicitOrdering<T> extends Ordering<T> {
        @Serial
        private static final long serialVersionUID = 1L;
        private final Map<T, Integer> rankMap;

        ExplicitOrdering(List<T> valuesInOrder) {
            this.rankMap = new HashMap<>();
            int rank = 0;
            for (T value : valuesInOrder) {
                rankMap.put(value, rank++);
            }
        }

        @Override
        public int compare(T left, T right) {
            Integer leftRank = rankMap.get(left);
            Integer rightRank = rankMap.get(right);
            if (leftRank == null || rightRank == null) {
                throw new IllegalArgumentException("Element not in explicit order");
            }
            return Integer.compare(leftRank, rightRank);
        }
    }

    /**
     * All-equal ordering
     */
    private static class AllEqualOrdering<T> extends Ordering<T> {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(T left, T right) {
            return 0;
        }
    }

    /**
     * Reverse ordering
     */
    private static class ReverseOrdering<T> extends Ordering<T> {
        @Serial
        private static final long serialVersionUID = 1L;
        private final Ordering<T> delegate;

        ReverseOrdering(Ordering<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int compare(T left, T right) {
            return delegate.compare(right, left);
        }

        @Override
        public Ordering<T> reverse() {
            return delegate;
        }
    }

    /**
     * Nulls-first ordering
     */
    private static class NullsFirstOrdering<T> extends Ordering<T> {
        @Serial
        private static final long serialVersionUID = 1L;
        private final Ordering<T> delegate;

        NullsFirstOrdering(Ordering<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int compare(T left, T right) {
            if (left == right) return 0;
            if (left == null) return -1;
            if (right == null) return 1;
            return delegate.compare(left, right);
        }
    }

    /**
     * Nulls-last ordering
     */
    private static class NullsLastOrdering<T> extends Ordering<T> {
        @Serial
        private static final long serialVersionUID = 1L;
        private final Ordering<T> delegate;

        NullsLastOrdering(Ordering<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int compare(T left, T right) {
            if (left == right) return 0;
            if (left == null) return 1;
            if (right == null) return -1;
            return delegate.compare(left, right);
        }
    }

    /**
     * Compound ordering
     */
    private static class CompoundOrdering<T> extends Ordering<T> {
        @Serial
        private static final long serialVersionUID = 1L;
        private final Ordering<T> primary;
        private final Comparator<? super T> secondary;

        CompoundOrdering(Ordering<T> primary, Comparator<? super T> secondary) {
            this.primary = primary;
            this.secondary = secondary;
        }

        @Override
        public int compare(T left, T right) {
            int result = primary.compare(left, right);
            return result != 0 ? result : secondary.compare(left, right);
        }
    }

    /**
     * By-function ordering
     */
    private static class ByFunctionOrdering<F, T> extends Ordering<F> {
        @Serial
        private static final long serialVersionUID = 1L;
        private final Function<F, ? extends T> function;
        private final Ordering<T> ordering;

        ByFunctionOrdering(Function<F, ? extends T> function, Ordering<T> ordering) {
            this.function = function;
            this.ordering = ordering;
        }

        @Override
        public int compare(F left, F right) {
            return ordering.compare(function.apply(left), function.apply(right));
        }
    }

    /**
     * Arbitrary ordering based on identity hash code
     */
    private static class ArbitraryOrdering extends Ordering<Object> {
        @Serial
        private static final long serialVersionUID = 1L;
        private final Map<Object, Integer> uids = Collections.synchronizedMap(new WeakHashMap<>());
        private int counter = 0;

        @Override
        public int compare(Object left, Object right) {
            if (left == right) {
                return 0;
            }
            int leftHash = getUid(left);
            int rightHash = getUid(right);
            return Integer.compare(leftHash, rightHash);
        }

        private synchronized int getUid(Object obj) {
            Integer uid = uids.get(obj);
            if (uid == null) {
                uid = counter++;
                uids.put(obj, uid);
            }
            return uid;
        }
    }

    /**
     * Ordering using toString()
     */
    private static class UsingToStringOrdering extends Ordering<Object> {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(Object left, Object right) {
            return left.toString().compareTo(right.toString());
        }
    }
}
