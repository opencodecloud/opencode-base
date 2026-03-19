/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.core;

import java.util.*;
import java.util.function.Function;

/**
 * Ordering - Fluent comparator builder
 * 排序器 - 流式比较器构建器
 *
 * <p>Provides a fluent API for building complex comparators with
 * null handling, reverse ordering, and chaining support.</p>
 * <p>提供用于构建复杂比较器的流式 API，支持空值处理、反向排序和链式调用。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Natural ordering
 * Ordering<String> natural = Ordering.natural();
 * natural.compare("a", "b");  // -1
 *
 * // Reverse ordering
 * Ordering<String> reversed = Ordering.<String>natural().reversed();
 * reversed.compare("a", "b");  // 1
 *
 * // Nulls first/last
 * Ordering<String> nullsFirst = Ordering.<String>natural().nullsFirst();
 * nullsFirst.compare(null, "a");  // -1
 *
 * // By key extraction
 * Ordering<Person> byAge = Ordering.from(Person::getAge);
 * Ordering<Person> byName = Ordering.from(Person::getName);
 *
 * // Compound ordering
 * Ordering<Person> compound = byAge.thenComparing(byName);
 *
 * // Min/Max operations
 * String min = Ordering.<String>natural().min("apple", "banana");  // "apple"
 * List<String> top3 = Ordering.<String>natural().leastOf(list, 3);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Natural and custom comparator ordering - 自然和自定义比较器排序</li>
 *   <li>Null handling: nullsFirst/nullsLast - 空值处理: nullsFirst/nullsLast</li>
 *   <li>Compound ordering with thenComparing - 通过thenComparing复合排序</li>
 *   <li>Min/Max and top-k element selection - 最小/最大和Top-K元素选择</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after creation) - 线程安全: 是（创建后不可变）</li>
 *   <li>Null-safe: Yes, with nullsFirst()/nullsLast() - 空值安全: 是，通过nullsFirst()/nullsLast()</li>
 * </ul>
 *
 * @param <T> the type being compared - 被比较的类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public abstract class Ordering<T> implements Comparator<T> {

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Returns an ordering that uses the natural order of the values.
     * 返回使用值的自然顺序的排序器。
     *
     * @param <T> the comparable type - 可比较类型
     * @return the natural ordering - 自然排序器
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> Ordering<T> natural() {
        return (Ordering<T>) NaturalOrdering.INSTANCE;
    }

    /**
     * Creates an ordering from an existing comparator.
     * 从现有比较器创建排序器。
     *
     * @param <T> the type being compared - 被比较的类型
     * @param comparator the comparator - 比较器
     * @return the ordering - 排序器
     */
    public static <T> Ordering<T> from(Comparator<T> comparator) {
        if (comparator instanceof Ordering<T> ordering) {
            return ordering;
        }
        return new ComparatorOrdering<>(comparator);
    }

    /**
     * Creates an ordering by extracting a comparable key.
     * 通过提取可比较的键创建排序器。
     *
     * @param <T> the type being compared - 被比较的类型
     * @param <U> the key type - 键类型
     * @param keyExtractor the key extractor - 键提取器
     * @return the ordering - 排序器
     */
    public static <T, U extends Comparable<? super U>> Ordering<T> from(
            Function<? super T, ? extends U> keyExtractor) {
        return new ByKeyOrdering<>(keyExtractor, natural());
    }

    /**
     * Creates an ordering by extracting a key and using a comparator.
     * 通过提取键并使用比较器创建排序器。
     *
     * @param <T> the type being compared - 被比较的类型
     * @param <U> the key type - 键类型
     * @param keyExtractor the key extractor - 键提取器
     * @param keyComparator the key comparator - 键比较器
     * @return the ordering - 排序器
     */
    public static <T, U> Ordering<T> from(
            Function<? super T, ? extends U> keyExtractor,
            Comparator<? super U> keyComparator) {
        return new ByKeyOrdering<>(keyExtractor, keyComparator);
    }

    /**
     * Returns an ordering that treats all values as equal.
     * 返回将所有值视为相等的排序器。
     *
     * @param <T> the type - 类型
     * @return the all-equal ordering - 全等排序器
     */
    @SuppressWarnings("unchecked")
    public static <T> Ordering<T> allEqual() {
        return (Ordering<T>) AllEqualOrdering.INSTANCE;
    }

    /**
     * Returns an ordering based on the iteration order of explicit values.
     * 返回基于显式值迭代顺序的排序器。
     *
     * @param <T> the type - 类型
     * @param valuesInOrder the values in order - 按顺序排列的值
     * @return the explicit ordering - 显式排序器
     */
    @SafeVarargs
    public static <T> Ordering<T> explicit(T... valuesInOrder) {
        return explicit(Arrays.asList(valuesInOrder));
    }

    /**
     * Returns an ordering based on the iteration order of explicit values.
     * 返回基于显式值迭代顺序的排序器。
     *
     * @param <T> the type - 类型
     * @param valuesInOrder the values in order - 按顺序排列的值
     * @return the explicit ordering - 显式排序器
     */
    public static <T> Ordering<T> explicit(List<T> valuesInOrder) {
        return new ExplicitOrdering<>(valuesInOrder);
    }

    // ==================== Transformation Methods | 转换方法 ====================

    /**
     * Returns the reverse ordering.
     * 返回反向排序器。
     *
     * @return the reversed ordering - 反向排序器
     */
    public Ordering<T> reversed() {
        return new ReverseOrdering<>(this);
    }

    /**
     * Returns an ordering that treats null as less than all other values.
     * 返回将 null 视为小于所有其他值的排序器。
     *
     * @return the ordering with nulls first - 空值优先的排序器
     */
    public Ordering<T> nullsFirst() {
        return new NullsFirstOrdering<>(this);
    }

    /**
     * Returns an ordering that treats null as greater than all other values.
     * 返回将 null 视为大于所有其他值的排序器。
     *
     * @return the ordering with nulls last - 空值最后的排序器
     */
    public Ordering<T> nullsLast() {
        return new NullsLastOrdering<>(this);
    }

    /**
     * Returns a compound ordering with a secondary comparator.
     * 返回具有次要比较器的复合排序器。
     *
     * @param secondary the secondary comparator - 次要比较器
     * @return the compound ordering - 复合排序器
     */
    public Ordering<T> thenComparing(Comparator<? super T> secondary) {
        return new CompoundOrdering<>(this, secondary);
    }

    /**
     * Returns a compound ordering using a key extractor.
     * 使用键提取器返回复合排序器。
     *
     * @param <U> the key type - 键类型
     * @param keyExtractor the key extractor - 键提取器
     * @return the compound ordering - 复合排序器
     */
    public <U extends Comparable<? super U>> Ordering<T> thenComparing(
            Function<? super T, ? extends U> keyExtractor) {
        return thenComparing(from(keyExtractor));
    }

    /**
     * Returns an ordering that applies a function before comparing.
     * 返回在比较前应用函数的排序器。
     *
     * @param <F> the input type - 输入类型
     * @param function the function to apply - 要应用的函数
     * @return the new ordering - 新排序器
     */
    public <F> Ordering<F> onResultOf(Function<F, ? extends T> function) {
        return new ByKeyOrdering<>(function, this);
    }

    // ==================== Min/Max Operations | 最小/最大操作 ====================

    /**
     * Returns the minimum of two values.
     * 返回两个值中的最小值。
     */
    public T min(T a, T b) {
        return compare(a, b) <= 0 ? a : b;
    }

    /**
     * Returns the minimum of the given values.
     * 返回给定值中的最小值。
     */
    @SafeVarargs
    public final T min(T first, T second, T... rest) {
        T result = min(first, second);
        for (T value : rest) {
            result = min(result, value);
        }
        return result;
    }

    /**
     * Returns the minimum value in an iterable.
     * 返回可迭代对象中的最小值。
     */
    public T min(Iterable<T> iterable) {
        Iterator<T> iter = iterable.iterator();
        if (!iter.hasNext()) {
            throw new NoSuchElementException("Cannot find min of empty iterable");
        }
        T result = iter.next();
        while (iter.hasNext()) {
            result = min(result, iter.next());
        }
        return result;
    }

    /**
     * Returns the maximum of two values.
     * 返回两个值中的最大值。
     */
    public T max(T a, T b) {
        return compare(a, b) >= 0 ? a : b;
    }

    /**
     * Returns the maximum of the given values.
     * 返回给定值中的最大值。
     */
    @SafeVarargs
    public final T max(T first, T second, T... rest) {
        T result = max(first, second);
        for (T value : rest) {
            result = max(result, value);
        }
        return result;
    }

    /**
     * Returns the maximum value in an iterable.
     * 返回可迭代对象中的最大值。
     */
    public T max(Iterable<T> iterable) {
        Iterator<T> iter = iterable.iterator();
        if (!iter.hasNext()) {
            throw new NoSuchElementException("Cannot find max of empty iterable");
        }
        T result = iter.next();
        while (iter.hasNext()) {
            result = max(result, iter.next());
        }
        return result;
    }

    // ==================== Top K Operations | 前 K 操作 ====================

    /**
     * Returns the k smallest elements in the given iterable.
     * 返回给定可迭代对象中最小的 k 个元素。
     *
     * @param iterable the iterable - 可迭代对象
     * @param k the number of elements - 元素数量
     * @return the k smallest elements in sorted order - 按排序顺序排列的最小 k 个元素
     */
    public List<T> leastOf(Iterable<T> iterable, int k) {
        if (k < 0) {
            throw new IllegalArgumentException("k must be non-negative");
        }
        if (k == 0) {
            return List.of();
        }

        // Use a max-heap to keep track of the k smallest elements
        PriorityQueue<T> heap = new PriorityQueue<>(k + 1, reversed());

        for (T element : iterable) {
            heap.offer(element);
            if (heap.size() > k) {
                heap.poll();
            }
        }

        List<T> result = new ArrayList<>(heap);
        result.sort(this);
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the k greatest elements in the given iterable.
     * 返回给定可迭代对象中最大的 k 个元素。
     *
     * @param iterable the iterable - 可迭代对象
     * @param k the number of elements - 元素数量
     * @return the k greatest elements in descending order - 按降序排列的最大 k 个元素
     */
    public List<T> greatestOf(Iterable<T> iterable, int k) {
        return reversed().leastOf(iterable, k);
    }

    // ==================== Sorting Operations | 排序操作 ====================

    /**
     * Returns a sorted copy of the given iterable.
     * 返回给定可迭代对象的排序副本。
     */
    public List<T> sortedCopy(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        for (T element : iterable) {
            list.add(element);
        }
        list.sort(this);
        return list;
    }

    /**
     * Returns an immutable sorted copy of the given iterable.
     * 返回给定可迭代对象的不可变排序副本。
     */
    public List<T> immutableSortedCopy(Iterable<T> iterable) {
        return Collections.unmodifiableList(sortedCopy(iterable));
    }

    /**
     * Returns true if the iterable is sorted according to this ordering.
     * 如果可迭代对象按此排序器排序返回 true。
     */
    public boolean isOrdered(Iterable<T> iterable) {
        Iterator<T> iter = iterable.iterator();
        if (!iter.hasNext()) return true;

        T prev = iter.next();
        while (iter.hasNext()) {
            T current = iter.next();
            if (compare(prev, current) > 0) {
                return false;
            }
            prev = current;
        }
        return true;
    }

    /**
     * Returns true if the iterable is strictly sorted according to this ordering.
     * 如果可迭代对象按此排序器严格排序返回 true。
     */
    public boolean isStrictlyOrdered(Iterable<T> iterable) {
        Iterator<T> iter = iterable.iterator();
        if (!iter.hasNext()) return true;

        T prev = iter.next();
        while (iter.hasNext()) {
            T current = iter.next();
            if (compare(prev, current) >= 0) {
                return false;
            }
            prev = current;
        }
        return true;
    }

    // ==================== Implementations | 实现 ====================

    private static final class NaturalOrdering extends Ordering<Comparable<Object>> {
        static final NaturalOrdering INSTANCE = new NaturalOrdering();

        @Override
        public int compare(Comparable<Object> left, Comparable<Object> right) {
            return left.compareTo(right);
        }

        @Override
        public Ordering<Comparable<Object>> reversed() {
            return ReverseNaturalOrdering.INSTANCE;
        }
    }

    private static final class ReverseNaturalOrdering extends Ordering<Comparable<Object>> {
        static final ReverseNaturalOrdering INSTANCE = new ReverseNaturalOrdering();

        @Override
        public int compare(Comparable<Object> left, Comparable<Object> right) {
            return right.compareTo(left);
        }

        @Override
        public Ordering<Comparable<Object>> reversed() {
            return NaturalOrdering.INSTANCE;
        }
    }

    private static final class ComparatorOrdering<T> extends Ordering<T> {
        private final Comparator<T> comparator;

        ComparatorOrdering(Comparator<T> comparator) {
            this.comparator = Objects.requireNonNull(comparator);
        }

        @Override
        public int compare(T left, T right) {
            return comparator.compare(left, right);
        }
    }

    private static final class ByKeyOrdering<T, U> extends Ordering<T> {
        private final Function<? super T, ? extends U> keyExtractor;
        private final Comparator<? super U> keyComparator;

        ByKeyOrdering(Function<? super T, ? extends U> keyExtractor, Comparator<? super U> keyComparator) {
            this.keyExtractor = Objects.requireNonNull(keyExtractor);
            this.keyComparator = Objects.requireNonNull(keyComparator);
        }

        @Override
        public int compare(T left, T right) {
            return keyComparator.compare(keyExtractor.apply(left), keyExtractor.apply(right));
        }
    }

    private static final class ReverseOrdering<T> extends Ordering<T> {
        private final Ordering<T> forward;

        ReverseOrdering(Ordering<T> forward) {
            this.forward = forward;
        }

        @Override
        public int compare(T left, T right) {
            return forward.compare(right, left);
        }

        @Override
        public Ordering<T> reversed() {
            return forward;
        }
    }

    private static final class NullsFirstOrdering<T> extends Ordering<T> {
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

        @Override
        public Ordering<T> nullsLast() {
            return new NullsLastOrdering<>(delegate);
        }
    }

    private static final class NullsLastOrdering<T> extends Ordering<T> {
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

        @Override
        public Ordering<T> nullsFirst() {
            return new NullsFirstOrdering<>(delegate);
        }
    }

    private static final class CompoundOrdering<T> extends Ordering<T> {
        private final Comparator<? super T> primary;
        private final Comparator<? super T> secondary;

        CompoundOrdering(Comparator<? super T> primary, Comparator<? super T> secondary) {
            this.primary = primary;
            this.secondary = secondary;
        }

        @Override
        public int compare(T left, T right) {
            int result = primary.compare(left, right);
            return result != 0 ? result : secondary.compare(left, right);
        }
    }

    private static final class AllEqualOrdering extends Ordering<Object> {
        static final AllEqualOrdering INSTANCE = new AllEqualOrdering();

        @Override
        public int compare(Object left, Object right) {
            return 0;
        }
    }

    private static final class ExplicitOrdering<T> extends Ordering<T> {
        private final Map<T, Integer> rankMap;

        ExplicitOrdering(List<T> valuesInOrder) {
            this.rankMap = new HashMap<>();
            int rank = 0;
            for (T value : valuesInOrder) {
                if (rankMap.put(value, rank++) != null) {
                    throw new IllegalArgumentException("Duplicate value: " + value);
                }
            }
        }

        @Override
        public int compare(T left, T right) {
            Integer leftRank = rankMap.get(left);
            Integer rightRank = rankMap.get(right);

            if (leftRank == null) {
                throw new IllegalArgumentException("Unknown value: " + left);
            }
            if (rightRank == null) {
                throw new IllegalArgumentException("Unknown value: " + right);
            }

            return Integer.compare(leftRank, rightRank);
        }
    }
}
