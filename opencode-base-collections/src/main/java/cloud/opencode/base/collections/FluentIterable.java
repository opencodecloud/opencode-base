package cloud.opencode.base.collections;

import cloud.opencode.base.collections.immutable.ImmutableSortedSet;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * FluentIterable - Fluent API for Iterable Operations
 * FluentIterable - 可迭代操作的流式 API
 *
 * <p>A fluent wrapper around Iterable that provides chainable operations
 * for filtering, transforming, and collecting elements.</p>
 * <p>围绕 Iterable 的流式包装器，提供可链式操作用于过滤、转换和收集元素。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent chaining - 流式链接</li>
 *   <li>Lazy evaluation - 惰性求值</li>
 *   <li>Filter and transform - 过滤和转换</li>
 *   <li>Collection conversion - 集合转换</li>
 *   <li>Terminal operations - 终端操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create from iterable - 从可迭代对象创建
 * FluentIterable<String> fluent = FluentIterable.from(strings);
 *
 * // Chain operations - 链式操作
 * List<Integer> lengths = FluentIterable.from(strings)
 *     .filter(s -> s.length() > 3)
 *     .transform(String::length)
 *     .toList();
 *
 * // Get first element - 获取第一个元素
 * Optional<String> first = FluentIterable.from(strings)
 *     .filter(s -> s.startsWith("A"))
 *     .first();
 *
 * // Limit and skip - 限制和跳过
 * List<String> page = FluentIterable.from(strings)
 *     .skip(10)
 *     .limit(5)
 *     .toList();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Operations are lazy - 操作是惰性的</li>
 *   <li>Single iteration on terminal - 终端操作单次迭代</li>
 *   <li>Memory efficient for large collections - 对大集合内存高效</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @param <E> element type | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public abstract class FluentIterable<E> implements Iterable<E> {

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create a FluentIterable from an Iterable.
     * 从 Iterable 创建 FluentIterable。
     *
     * @param <E>      element type | 元素类型
     * @param iterable the iterable | 可迭代对象
     * @return fluent iterable | 流式可迭代对象
     */
    public static <E> FluentIterable<E> from(Iterable<E> iterable) {
        if (iterable instanceof FluentIterable) {
            return (FluentIterable<E>) iterable;
        }
        return new FromIterable<>(iterable);
    }

    /**
     * Create a FluentIterable from varargs.
     * 从可变参数创建 FluentIterable。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return fluent iterable | 流式可迭代对象
     */
    @SafeVarargs
    public static <E> FluentIterable<E> of(E... elements) {
        return from(Arrays.asList(elements));
    }

    /**
     * Create an empty FluentIterable.
     * 创建空 FluentIterable。
     *
     * @param <E> element type | 元素类型
     * @return empty fluent iterable | 空流式可迭代对象
     */
    public static <E> FluentIterable<E> empty() {
        return from(Collections.emptyList());
    }

    /**
     * Concatenate multiple iterables.
     * 连接多个可迭代对象。
     *
     * @param <E>       element type | 元素类型
     * @param iterables iterables to concatenate | 要连接的可迭代对象
     * @return concatenated fluent iterable | 连接的流式可迭代对象
     */
    @SafeVarargs
    public static <E> FluentIterable<E> concat(Iterable<? extends E>... iterables) {
        return concat(Arrays.asList(iterables));
    }

    /**
     * Concatenate iterables.
     * 连接可迭代对象。
     *
     * @param <E>       element type | 元素类型
     * @param iterables iterables to concatenate | 要连接的可迭代对象
     * @return concatenated fluent iterable | 连接的流式可迭代对象
     */
    public static <E> FluentIterable<E> concat(Iterable<? extends Iterable<? extends E>> iterables) {
        return new ConcatIterable<>(iterables);
    }

    // ==================== 中间操作 | Intermediate Operations ====================

    /**
     * Filter elements.
     * 过滤元素。
     *
     * @param predicate the predicate | 谓词
     * @return filtered fluent iterable | 过滤后的流式可迭代对象
     */
    public FluentIterable<E> filter(Predicate<? super E> predicate) {
        Objects.requireNonNull(predicate);
        return new FilteredIterable<>(this, predicate);
    }

    /**
     * Filter elements by type.
     * 按类型过滤元素。
     *
     * @param <T>  target type | 目标类型
     * @param type the type | 类型
     * @return filtered fluent iterable | 过滤后的流式可迭代对象
     */
    public <T> FluentIterable<T> filter(Class<T> type) {
        Objects.requireNonNull(type);
        return new TypeFilteredIterable<>(this, type);
    }

    /**
     * Transform elements.
     * 转换元素。
     *
     * @param <T>      target type | 目标类型
     * @param function the transform function | 转换函数
     * @return transformed fluent iterable | 转换后的流式可迭代对象
     */
    public <T> FluentIterable<T> transform(Function<? super E, T> function) {
        Objects.requireNonNull(function);
        return new TransformedIterable<>(this, function);
    }

    /**
     * Flat map elements.
     * 平面映射元素。
     *
     * @param <T>      target type | 目标类型
     * @param function the function | 函数
     * @return flat mapped fluent iterable | 平面映射后的流式可迭代对象
     */
    public <T> FluentIterable<T> flatMap(Function<? super E, ? extends Iterable<T>> function) {
        Objects.requireNonNull(function);
        return new FlatMappedIterable<>(this, function);
    }

    /**
     * Transform and concatenate elements (alias for flatMap).
     * 转换并连接元素（flatMap的别名）。
     *
     * @param <T>      target type | 目标类型
     * @param function the function | 函数
     * @return transformed and concatenated fluent iterable | 转换并连接后的流式可迭代对象
     */
    public <T> FluentIterable<T> transformAndConcat(Function<? super E, ? extends Iterable<T>> function) {
        return flatMap(function);
    }

    /**
     * Limit elements.
     * 限制元素数量。
     *
     * @param maxSize maximum size | 最大数量
     * @return limited fluent iterable | 限制后的流式可迭代对象
     */
    public FluentIterable<E> limit(int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize cannot be negative");
        }
        return new LimitedIterable<>(this, maxSize);
    }

    /**
     * Skip elements.
     * 跳过元素。
     *
     * @param count number to skip | 要跳过的数量
     * @return skipped fluent iterable | 跳过后的流式可迭代对象
     */
    public FluentIterable<E> skip(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
        return new SkippedIterable<>(this, count);
    }

    /**
     * Cycle elements infinitely.
     * 无限循环元素。
     *
     * @return cycling fluent iterable | 循环的流式可迭代对象
     */
    public FluentIterable<E> cycle() {
        return new CyclingIterable<>(this);
    }

    /**
     * Append elements.
     * 追加元素。
     *
     * @param other elements to append | 要追加的元素
     * @return appended fluent iterable | 追加后的流式可迭代对象
     */
    public FluentIterable<E> append(Iterable<? extends E> other) {
        return concat(Arrays.asList(this, other));
    }

    /**
     * Append varargs elements.
     * 追加可变参数元素。
     *
     * @param elements elements to append | 要追加的元素
     * @return appended fluent iterable | 追加后的流式可迭代对象
     */
    @SafeVarargs
    public final FluentIterable<E> append(E... elements) {
        return append(Arrays.asList(elements));
    }

    /**
     * Remove duplicate elements.
     * 移除重复元素。
     *
     * @return distinct fluent iterable | 去重后的流式可迭代对象
     */
    public FluentIterable<E> distinct() {
        return new DistinctIterable<>(this);
    }

    // ==================== 终端操作 | Terminal Operations ====================

    /**
     * Return the first element.
     * 返回第一个元素。
     *
     * @return first element, or empty | 第一个元素，或空
     */
    public Optional<E> first() {
        Iterator<E> it = iterator();
        return it.hasNext() ? Optional.of(it.next()) : Optional.empty();
    }

    /**
     * Return the first element, or default.
     * 返回第一个元素，或默认值。
     *
     * @param defaultValue default value | 默认值
     * @return first element or default | 第一个元素或默认值
     */
    public E firstOr(E defaultValue) {
        return first().orElse(defaultValue);
    }

    /**
     * Return the last element.
     * 返回最后一个元素。
     *
     * @return last element, or empty | 最后一个元素，或空
     */
    public Optional<E> last() {
        E last = null;
        boolean found = false;
        for (E e : this) {
            last = e;
            found = true;
        }
        return found ? Optional.of(last) : Optional.empty();
    }

    /**
     * Return the last element, or default.
     * 返回最后一个元素，或默认值。
     *
     * @param defaultValue default value | 默认值
     * @return last element or default | 最后一个元素或默认值
     */
    public E lastOr(E defaultValue) {
        return last().orElse(defaultValue);
    }

    /**
     * Get element at index.
     * 获取指定索引的元素。
     *
     * @param index the index | 索引
     * @return element at index, or empty | 索引处的元素，或空
     */
    public Optional<E> get(int index) {
        if (index < 0) {
            return Optional.empty();
        }
        int i = 0;
        for (E e : this) {
            if (i == index) {
                return Optional.of(e);
            }
            i++;
        }
        return Optional.empty();
    }

    /**
     * Check if any element matches.
     * 检查是否有元素匹配。
     *
     * @param predicate the predicate | 谓词
     * @return true if any match | 如果有匹配则返回 true
     */
    public boolean anyMatch(Predicate<? super E> predicate) {
        for (E e : this) {
            if (predicate.test(e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if all elements match.
     * 检查是否所有元素匹配。
     *
     * @param predicate the predicate | 谓词
     * @return true if all match | 如果全部匹配则返回 true
     */
    public boolean allMatch(Predicate<? super E> predicate) {
        for (E e : this) {
            if (!predicate.test(e)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if no element matches.
     * 检查是否没有元素匹配。
     *
     * @param predicate the predicate | 谓词
     * @return true if none match | 如果没有匹配则返回 true
     */
    public boolean noneMatch(Predicate<? super E> predicate) {
        return !anyMatch(predicate);
    }

    /**
     * Find the first matching element.
     * 找到第一个匹配的元素。
     *
     * @param predicate the predicate | 谓词
     * @return first matching element, or empty | 第一个匹配的元素，或空
     */
    public Optional<E> firstMatch(Predicate<? super E> predicate) {
        for (E e : this) {
            if (predicate.test(e)) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    /**
     * Check if contains element.
     * 检查是否包含元素。
     *
     * @param element the element | 元素
     * @return true if contains | 如果包含则返回 true
     */
    public boolean contains(Object element) {
        for (E e : this) {
            if (Objects.equals(e, element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Count elements.
     * 计算元素数量。
     *
     * @return count | 数量
     */
    public int size() {
        int count = 0;
        for (E ignored : this) {
            count++;
        }
        return count;
    }

    /**
     * Check if empty.
     * 检查是否为空。
     *
     * @return true if empty | 如果为空则返回 true
     */
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    // ==================== 收集操作 | Collect Operations ====================

    /**
     * Collect to list.
     * 收集为列表。
     *
     * @return list | 列表
     */
    public List<E> toList() {
        List<E> list = new ArrayList<>();
        for (E e : this) {
            list.add(e);
        }
        return list;
    }

    /**
     * Collect to immutable list.
     * 收集为不可变列表。
     *
     * @return immutable list | 不可变列表
     */
    public ImmutableList<E> toImmutableList() {
        return ImmutableList.copyOf(this);
    }

    /**
     * Collect to set.
     * 收集为集合。
     *
     * @return set | 集合
     */
    public Set<E> toSet() {
        Set<E> set = new LinkedHashSet<>();
        for (E e : this) {
            set.add(e);
        }
        return set;
    }

    /**
     * Collect to immutable set.
     * 收集为不可变集合。
     *
     * @return immutable set | 不可变集合
     */
    public ImmutableSet<E> toImmutableSet() {
        return ImmutableSet.copyOf(this);
    }

    /**
     * Collect to sorted immutable list.
     * 收集为排序的不可变列表。
     *
     * @param comparator the comparator | 比较器
     * @return sorted immutable list | 排序的不可变列表
     */
    public ImmutableList<E> toSortedList(Comparator<? super E> comparator) {
        List<E> list = toList();
        list.sort(comparator);
        return ImmutableList.copyOf(list);
    }

    /**
     * Collect to sorted immutable set.
     * 收集为排序的不可变集合。
     *
     * @param comparator the comparator | 比较器
     * @return sorted immutable set | 排序的不可变集合
     */
    public ImmutableSortedSet<E> toSortedSet(Comparator<? super E> comparator) {
        TreeSet<E> set = new TreeSet<>(comparator);
        for (E e : this) {
            set.add(e);
        }
        return ImmutableSortedSet.copyOf(set, comparator);
    }

    /**
     * Collect to map.
     * 收集为映射。
     *
     * @param <K>           key type | 键类型
     * @param <V>           value type | 值类型
     * @param keyFunction   key function | 键函数
     * @param valueFunction value function | 值函数
     * @return map | 映射
     */
    public <K, V> Map<K, V> toMap(Function<? super E, K> keyFunction, Function<? super E, V> valueFunction) {
        Map<K, V> map = new LinkedHashMap<>();
        for (E e : this) {
            map.put(keyFunction.apply(e), valueFunction.apply(e));
        }
        return map;
    }

    /**
     * Index elements by unique key.
     * 按唯一键索引元素。
     *
     * @param <K>         key type | 键类型
     * @param keyFunction key function | 键函数
     * @return map with unique keys | 具有唯一键的映射
     */
    public <K> Map<K, E> uniqueIndex(Function<? super E, K> keyFunction) {
        Map<K, E> map = new LinkedHashMap<>();
        for (E e : this) {
            K key = keyFunction.apply(e);
            if (map.containsKey(key)) {
                throw new IllegalArgumentException("Duplicate key: " + key);
            }
            map.put(key, e);
        }
        return map;
    }

    /**
     * Convert to array.
     * 转换为数组。
     *
     * @param type the array element type | 数组元素类型
     * @return array | 数组
     */
    @SuppressWarnings("unchecked")
    public E[] toArray(Class<E> type) {
        List<E> list = toList();
        E[] array = (E[]) java.lang.reflect.Array.newInstance(type, list.size());
        return list.toArray(array);
    }

    /**
     * Copy elements into collection.
     * 将元素复制到集合中。
     *
     * @param <C>        collection type | 集合类型
     * @param collection the target collection | 目标集合
     * @return the collection | 集合
     */
    public <C extends Collection<? super E>> C copyInto(C collection) {
        for (E e : this) {
            collection.add(e);
        }
        return collection;
    }

    /**
     * Collect using a collector.
     * 使用收集器收集。
     *
     * @param <R>       result type | 结果类型
     * @param <A>       accumulator type | 累加器类型
     * @param collector the collector | 收集器
     * @return result | 结果
     */
    public <R, A> R collect(Collector<? super E, A, R> collector) {
        return stream().collect(collector);
    }

    /**
     * Convert to stream.
     * 转换为流。
     *
     * @return stream | 流
     */
    public Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Join to string.
     * 连接为字符串。
     *
     * @param separator separator | 分隔符
     * @return joined string | 连接的字符串
     */
    public String join(String separator) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (E e : this) {
            if (!first) {
                sb.append(separator);
            }
            sb.append(e);
            first = false;
        }
        return sb.toString();
    }

    // ==================== 内部类 | Internal Classes ====================

    private static class FromIterable<E> extends FluentIterable<E> {
        private final Iterable<E> iterable;

        FromIterable(Iterable<E> iterable) {
            this.iterable = Objects.requireNonNull(iterable);
        }

        @Override
        public Iterator<E> iterator() {
            return iterable.iterator();
        }
    }

    private static class FilteredIterable<E> extends FluentIterable<E> {
        private final Iterable<E> source;
        private final Predicate<? super E> predicate;

        FilteredIterable(Iterable<E> source, Predicate<? super E> predicate) {
            this.source = source;
            this.predicate = predicate;
        }

        @Override
        public Iterator<E> iterator() {
            return new FilteredIterator<>(source.iterator(), predicate);
        }
    }

    private static class FilteredIterator<E> implements Iterator<E> {
        private final Iterator<E> delegate;
        private final Predicate<? super E> predicate;
        private E next;
        private boolean hasNext;

        FilteredIterator(Iterator<E> delegate, Predicate<? super E> predicate) {
            this.delegate = delegate;
            this.predicate = predicate;
            advance();
        }

        private void advance() {
            while (delegate.hasNext()) {
                E e = delegate.next();
                if (predicate.test(e)) {
                    next = e;
                    hasNext = true;
                    return;
                }
            }
            hasNext = false;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public E next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            E result = next;
            advance();
            return result;
        }
    }

    private static class TypeFilteredIterable<E, T> extends FluentIterable<T> {
        private final Iterable<E> source;
        private final Class<T> type;

        TypeFilteredIterable(Iterable<E> source, Class<T> type) {
            this.source = source;
            this.type = type;
        }

        @Override
        public Iterator<T> iterator() {
            return new TypeFilteredIterator<>(source.iterator(), type);
        }
    }

    private static class TypeFilteredIterator<E, T> implements Iterator<T> {
        private final Iterator<E> delegate;
        private final Class<T> type;
        private T next;
        private boolean hasNext;

        TypeFilteredIterator(Iterator<E> delegate, Class<T> type) {
            this.delegate = delegate;
            this.type = type;
            advance();
        }

        private void advance() {
            while (delegate.hasNext()) {
                E e = delegate.next();
                if (type.isInstance(e)) {
                    next = type.cast(e);
                    hasNext = true;
                    return;
                }
            }
            hasNext = false;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public T next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            T result = next;
            advance();
            return result;
        }
    }

    private static class TransformedIterable<E, T> extends FluentIterable<T> {
        private final Iterable<E> source;
        private final Function<? super E, T> function;

        TransformedIterable(Iterable<E> source, Function<? super E, T> function) {
            this.source = source;
            this.function = function;
        }

        @Override
        public Iterator<T> iterator() {
            return new TransformedIterator<>(source.iterator(), function);
        }
    }

    private static class TransformedIterator<E, T> implements Iterator<T> {
        private final Iterator<E> delegate;
        private final Function<? super E, T> function;

        TransformedIterator(Iterator<E> delegate, Function<? super E, T> function) {
            this.delegate = delegate;
            this.function = function;
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public T next() {
            return function.apply(delegate.next());
        }
    }

    private static class FlatMappedIterable<E, T> extends FluentIterable<T> {
        private final Iterable<E> source;
        private final Function<? super E, ? extends Iterable<T>> function;

        FlatMappedIterable(Iterable<E> source, Function<? super E, ? extends Iterable<T>> function) {
            this.source = source;
            this.function = function;
        }

        @Override
        public Iterator<T> iterator() {
            return new FlatMappedIterator<>(source.iterator(), function);
        }
    }

    private static class FlatMappedIterator<E, T> implements Iterator<T> {
        private final Iterator<E> outer;
        private final Function<? super E, ? extends Iterable<T>> function;
        private Iterator<T> inner = Collections.emptyIterator();

        FlatMappedIterator(Iterator<E> outer, Function<? super E, ? extends Iterable<T>> function) {
            this.outer = outer;
            this.function = function;
        }

        @Override
        public boolean hasNext() {
            while (!inner.hasNext() && outer.hasNext()) {
                inner = function.apply(outer.next()).iterator();
            }
            return inner.hasNext();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return inner.next();
        }
    }

    private static class LimitedIterable<E> extends FluentIterable<E> {
        private final Iterable<E> source;
        private final int maxSize;

        LimitedIterable(Iterable<E> source, int maxSize) {
            this.source = source;
            this.maxSize = maxSize;
        }

        @Override
        public Iterator<E> iterator() {
            return new LimitedIterator<>(source.iterator(), maxSize);
        }
    }

    private static class LimitedIterator<E> implements Iterator<E> {
        private final Iterator<E> delegate;
        private int remaining;

        LimitedIterator(Iterator<E> delegate, int maxSize) {
            this.delegate = delegate;
            this.remaining = maxSize;
        }

        @Override
        public boolean hasNext() {
            return remaining > 0 && delegate.hasNext();
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            remaining--;
            return delegate.next();
        }
    }

    private static class SkippedIterable<E> extends FluentIterable<E> {
        private final Iterable<E> source;
        private final int count;

        SkippedIterable(Iterable<E> source, int count) {
            this.source = source;
            this.count = count;
        }

        @Override
        public Iterator<E> iterator() {
            Iterator<E> it = source.iterator();
            for (int i = 0; i < count && it.hasNext(); i++) {
                it.next();
            }
            return it;
        }
    }

    private static class CyclingIterable<E> extends FluentIterable<E> {
        private final Iterable<E> source;

        CyclingIterable(Iterable<E> source) {
            this.source = source;
        }

        @Override
        public Iterator<E> iterator() {
            return new CyclingIterator<>(source);
        }
    }

    private static class CyclingIterator<E> implements Iterator<E> {
        private final Iterable<E> source;
        private Iterator<E> current;

        CyclingIterator(Iterable<E> source) {
            this.source = source;
            this.current = source.iterator();
        }

        @Override
        public boolean hasNext() {
            return current.hasNext() || source.iterator().hasNext();
        }

        @Override
        public E next() {
            if (!current.hasNext()) {
                current = source.iterator();
            }
            if (!current.hasNext()) {
                throw new NoSuchElementException();
            }
            return current.next();
        }
    }

    private static class ConcatIterable<E> extends FluentIterable<E> {
        private final Iterable<? extends Iterable<? extends E>> iterables;

        ConcatIterable(Iterable<? extends Iterable<? extends E>> iterables) {
            this.iterables = iterables;
        }

        @Override
        public Iterator<E> iterator() {
            return new ConcatIterator<>(iterables.iterator());
        }
    }

    private static class ConcatIterator<E> implements Iterator<E> {
        private final Iterator<? extends Iterable<? extends E>> outer;
        private Iterator<? extends E> inner = Collections.emptyIterator();

        ConcatIterator(Iterator<? extends Iterable<? extends E>> outer) {
            this.outer = outer;
        }

        @Override
        public boolean hasNext() {
            while (!inner.hasNext() && outer.hasNext()) {
                inner = outer.next().iterator();
            }
            return inner.hasNext();
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return inner.next();
        }
    }

    private static class DistinctIterable<E> extends FluentIterable<E> {
        private final Iterable<E> source;

        DistinctIterable(Iterable<E> source) {
            this.source = source;
        }

        @Override
        public Iterator<E> iterator() {
            return new DistinctIterator<>(source.iterator());
        }
    }

    private static class DistinctIterator<E> implements Iterator<E> {
        private final Iterator<E> delegate;
        private final Set<E> seen = new LinkedHashSet<>();
        private E next;
        private boolean hasNext;

        DistinctIterator(Iterator<E> delegate) {
            this.delegate = delegate;
            advance();
        }

        private void advance() {
            while (delegate.hasNext()) {
                E e = delegate.next();
                if (seen.add(e)) {
                    next = e;
                    hasNext = true;
                    return;
                }
            }
            hasNext = false;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public E next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            E result = next;
            advance();
            return result;
        }
    }
}
