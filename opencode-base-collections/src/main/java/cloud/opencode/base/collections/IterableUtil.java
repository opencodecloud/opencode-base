package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * IterableUtil - Iterable Utility Class
 * IterableUtil - 可迭代对象工具类
 *
 * <p>Provides comprehensive operations for Iterable including concatenation, partitioning,
 * filtering, transformation, and statistical operations.</p>
 * <p>提供全面的 Iterable 操作，包括连接、分区、过滤、转换和统计操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lazy concatenation of multiple iterables - 惰性连接多个可迭代对象</li>
 *   <li>Partitioning by size - 按大小分区</li>
 *   <li>Lazy filtering and transformation - 惰性过滤和转换</li>
 *   <li>Element access operations - 元素访问操作</li>
 *   <li>Statistical operations - 统计操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Concatenate iterables - 连接可迭代对象
 * Iterable<String> all = IterableUtil.concat(list1, list2, list3);
 *
 * // Partition into chunks - 分区
 * Iterable<List<String>> chunks = IterableUtil.partition(list, 10);
 *
 * // Filter elements - 过滤
 * Iterable<String> filtered = IterableUtil.filter(list, s -> s.length() > 5);
 *
 * // Transform elements - 转换
 * Iterable<Integer> lengths = IterableUtil.transform(list, String::length);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Most operations are lazy - 大多数操作是惰性的</li>
 *   <li>Memory efficient for large datasets - 对大数据集内存高效</li>
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
public final class IterableUtil {

    private IterableUtil() {
    }

    // ==================== 连接与拼接 | Concatenation ====================

    /**
     * Lazily concatenate multiple iterables
     * 懒连接多个 Iterable
     *
     * @param <E>    element type | 元素类型
     * @param inputs iterables to concatenate | 要连接的可迭代对象
     * @return concatenated iterable | 连接后的可迭代对象
     */
    @SafeVarargs
    public static <E> Iterable<E> concat(Iterable<? extends E>... inputs) {
        if (inputs == null || inputs.length == 0) {
            return Collections.emptyList();
        }
        return () -> new Iterator<>() {
            private int index = 0;
            private Iterator<? extends E> current = Collections.emptyIterator();

            @Override
            public boolean hasNext() {
                while (!current.hasNext() && index < inputs.length) {
                    Iterable<? extends E> next = inputs[index++];
                    if (next != null) {
                        current = next.iterator();
                    }
                }
                return current.hasNext();
            }

            @Override
            public E next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return current.next();
            }
        };
    }

    /**
     * Lazily concatenate an iterable of iterables
     * 懒连接 Iterable 的 Iterable
     *
     * @param <E>    element type | 元素类型
     * @param inputs iterable of iterables | 可迭代对象的可迭代对象
     * @return concatenated iterable | 连接后的可迭代对象
     */
    public static <E> Iterable<E> concat(Iterable<? extends Iterable<? extends E>> inputs) {
        if (inputs == null) {
            return Collections.emptyList();
        }
        return () -> new Iterator<>() {
            private final Iterator<? extends Iterable<? extends E>> outer = inputs.iterator();
            private Iterator<? extends E> current = Collections.emptyIterator();

            @Override
            public boolean hasNext() {
                while (!current.hasNext() && outer.hasNext()) {
                    Iterable<? extends E> next = outer.next();
                    if (next != null) {
                        current = next.iterator();
                    }
                }
                return current.hasNext();
            }

            @Override
            public E next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return current.next();
            }
        };
    }

    // ==================== 分区 | Partitioning ====================

    /**
     * Partition iterable into fixed-size chunks (view)
     * 按固定大小分区（返回视图）
     *
     * @param <E>      element type | 元素类型
     * @param iterable the iterable | 可迭代对象
     * @param size     chunk size | 分区大小
     * @return iterable of chunks | 分区的可迭代对象
     */
    public static <E> Iterable<List<E>> partition(Iterable<E> iterable, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive: " + size);
        }
        if (iterable == null) {
            return Collections.emptyList();
        }
        return () -> new Iterator<>() {
            private final Iterator<E> iterator = iterable.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public List<E> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                List<E> chunk = new ArrayList<>(size);
                for (int i = 0; i < size && iterator.hasNext(); i++) {
                    chunk.add(iterator.next());
                }
                return chunk;
            }
        };
    }

    /**
     * Partition iterable into fixed-size chunks, padding the last chunk with null
     * 按固定大小分区（填充最后一组）
     *
     * @param <E>      element type | 元素类型
     * @param iterable the iterable | 可迭代对象
     * @param size     chunk size | 分区大小
     * @return iterable of chunks | 分区的可迭代对象
     */
    public static <E> Iterable<List<E>> paddedPartition(Iterable<E> iterable, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive: " + size);
        }
        if (iterable == null) {
            return Collections.emptyList();
        }
        return () -> new Iterator<>() {
            private final Iterator<E> iterator = iterable.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public List<E> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                List<E> chunk = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    if (iterator.hasNext()) {
                        chunk.add(iterator.next());
                    } else {
                        chunk.add(null);
                    }
                }
                return chunk;
            }
        };
    }

    // ==================== 过滤 | Filtering ====================

    /**
     * Lazily filter elements
     * 懒过滤
     *
     * @param <E>        element type | 元素类型
     * @param unfiltered the unfiltered iterable | 未过滤的可迭代对象
     * @param predicate  the predicate | 谓词
     * @return filtered iterable | 过滤后的可迭代对象
     */
    public static <E> Iterable<E> filter(Iterable<E> unfiltered, Predicate<? super E> predicate) {
        if (unfiltered == null || predicate == null) {
            return Collections.emptyList();
        }
        return () -> new Iterator<>() {
            private final Iterator<E> iterator = unfiltered.iterator();
            private E next;
            private boolean hasNext;

            private void advance() {
                hasNext = false;
                while (iterator.hasNext()) {
                    E candidate = iterator.next();
                    if (predicate.test(candidate)) {
                        next = candidate;
                        hasNext = true;
                        return;
                    }
                }
            }

            @Override
            public boolean hasNext() {
                if (!hasNext) {
                    advance();
                }
                return hasNext;
            }

            @Override
            public E next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                E result = next;
                hasNext = false;
                return result;
            }
        };
    }

    /**
     * Filter elements by type
     * 按类型过滤
     *
     * @param <E>         desired element type | 目标元素类型
     * @param unfiltered  the unfiltered iterable | 未过滤的可迭代对象
     * @param desiredType the desired type | 目标类型
     * @return filtered iterable | 过滤后的可迭代对象
     */
    @SuppressWarnings("unchecked")
    public static <E> Iterable<E> filter(Iterable<?> unfiltered, Class<E> desiredType) {
        if (unfiltered == null || desiredType == null) {
            return Collections.emptyList();
        }
        return (Iterable<E>) filter(unfiltered, desiredType::isInstance);
    }

    /**
     * Check if any element matches the predicate
     * 检查是否任意匹配
     *
     * @param <E>       element type | 元素类型
     * @param iterable  the iterable | 可迭代对象
     * @param predicate the predicate | 谓词
     * @return true if any element matches | 如果有任何元素匹配则返回 true
     */
    public static <E> boolean any(Iterable<E> iterable, Predicate<? super E> predicate) {
        if (iterable == null || predicate == null) {
            return false;
        }
        for (E e : iterable) {
            if (predicate.test(e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if all elements match the predicate
     * 检查是否全部匹配
     *
     * @param <E>       element type | 元素类型
     * @param iterable  the iterable | 可迭代对象
     * @param predicate the predicate | 谓词
     * @return true if all elements match | 如果所有元素都匹配则返回 true
     */
    public static <E> boolean all(Iterable<E> iterable, Predicate<? super E> predicate) {
        if (iterable == null) {
            return true;
        }
        if (predicate == null) {
            return false;
        }
        for (E e : iterable) {
            if (!predicate.test(e)) {
                return false;
            }
        }
        return true;
    }

    // ==================== 查找 | Searching ====================

    /**
     * Try to find the first matching element
     * 查找第一个匹配元素
     *
     * @param <E>       element type | 元素类型
     * @param iterable  the iterable | 可迭代对象
     * @param predicate the predicate | 谓词
     * @return optional containing the first match | 包含第一个匹配的 Optional
     */
    public static <E> Optional<E> tryFind(Iterable<E> iterable, Predicate<? super E> predicate) {
        if (iterable == null || predicate == null) {
            return Optional.empty();
        }
        for (E e : iterable) {
            if (predicate.test(e)) {
                return Optional.ofNullable(e);
            }
        }
        return Optional.empty();
    }

    /**
     * Get the only element from an iterable
     * 查找唯一匹配元素（多于一个抛异常）
     *
     * @param <E>      element type | 元素类型
     * @param iterable the iterable | 可迭代对象
     * @return the only element | 唯一元素
     * @throws NoSuchElementException   if empty | 如果为空
     * @throws OpenCollectionException if more than one element | 如果超过一个元素
     */
    public static <E> E getOnlyElement(Iterable<E> iterable) {
        if (iterable == null) {
            throw new NoSuchElementException("Iterable is null");
        }
        Iterator<E> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            throw new NoSuchElementException("Iterable is empty");
        }
        E first = iterator.next();
        if (iterator.hasNext()) {
            throw new OpenCollectionException("Expected one element but found multiple");
        }
        return first;
    }

    /**
     * Get the only element from an iterable, or default
     * 查找唯一匹配元素（带默认值）
     *
     * @param <E>          element type | 元素类型
     * @param iterable     the iterable | 可迭代对象
     * @param defaultValue default value | 默认值
     * @return the only element or default | 唯一元素或默认值
     * @throws OpenCollectionException if more than one element | 如果超过一个元素
     */
    public static <E> E getOnlyElement(Iterable<? extends E> iterable, E defaultValue) {
        if (iterable == null) {
            return defaultValue;
        }
        Iterator<? extends E> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            return defaultValue;
        }
        E first = iterator.next();
        if (iterator.hasNext()) {
            throw new OpenCollectionException("Expected one element but found multiple");
        }
        return first;
    }

    // ==================== 访问 | Access ====================

    /**
     * Get the first element or default
     * 获取第一个元素
     *
     * @param <E>          element type | 元素类型
     * @param iterable     the iterable | 可迭代对象
     * @param defaultValue default value | 默认值
     * @return first element or default | 第一个元素或默认值
     */
    public static <E> E getFirst(Iterable<? extends E> iterable, E defaultValue) {
        if (iterable == null) {
            return defaultValue;
        }
        Iterator<? extends E> iterator = iterable.iterator();
        return iterator.hasNext() ? iterator.next() : defaultValue;
    }

    /**
     * Get the last element
     * 获取最后一个元素
     *
     * @param <E>      element type | 元素类型
     * @param iterable the iterable | 可迭代对象
     * @return last element | 最后一个元素
     * @throws NoSuchElementException if empty | 如果为空
     */
    public static <E> E getLast(Iterable<E> iterable) {
        if (iterable == null) {
            throw new NoSuchElementException("Iterable is null");
        }
        if (iterable instanceof SequencedCollection<E> seq) {
            if (seq.isEmpty()) {
                throw new NoSuchElementException("Collection is empty");
            }
            return seq.getLast();
        }
        Iterator<E> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            throw new NoSuchElementException("Iterable is empty");
        }
        E last = iterator.next();
        while (iterator.hasNext()) {
            last = iterator.next();
        }
        return last;
    }

    /**
     * Get the last element or default
     * 获取最后一个元素（带默认值）
     *
     * @param <E>          element type | 元素类型
     * @param iterable     the iterable | 可迭代对象
     * @param defaultValue default value | 默认值
     * @return last element or default | 最后一个元素或默认值
     */
    public static <E> E getLast(Iterable<? extends E> iterable, E defaultValue) {
        if (iterable == null) {
            return defaultValue;
        }
        if (iterable instanceof SequencedCollection<? extends E> seq) {
            return seq.isEmpty() ? defaultValue : seq.getLast();
        }
        Iterator<? extends E> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            return defaultValue;
        }
        E last = iterator.next();
        while (iterator.hasNext()) {
            last = iterator.next();
        }
        return last;
    }

    /**
     * Get element at position
     * 按索引获取元素
     *
     * @param <E>      element type | 元素类型
     * @param iterable the iterable | 可迭代对象
     * @param position the position | 位置
     * @return element at position | 指定位置的元素
     * @throws IndexOutOfBoundsException if position is out of bounds | 如果位置越界
     */
    public static <E> E get(Iterable<E> iterable, int position) {
        if (iterable == null) {
            throw new IndexOutOfBoundsException("Iterable is null");
        }
        if (position < 0) {
            throw new IndexOutOfBoundsException("Position cannot be negative: " + position);
        }
        if (iterable instanceof List<E> list) {
            return list.get(position);
        }
        int index = 0;
        for (E e : iterable) {
            if (index == position) {
                return e;
            }
            index++;
        }
        throw new IndexOutOfBoundsException("Position: " + position + ", Size: " + index);
    }

    /**
     * Get element at position or default
     * 按索引获取元素（带默认值）
     *
     * @param <E>          element type | 元素类型
     * @param iterable     the iterable | 可迭代对象
     * @param position     the position | 位置
     * @param defaultValue default value | 默认值
     * @return element at position or default | 指定位置的元素或默认值
     */
    public static <E> E get(Iterable<? extends E> iterable, int position, E defaultValue) {
        if (iterable == null || position < 0) {
            return defaultValue;
        }
        if (iterable instanceof List<? extends E> list) {
            return position < list.size() ? list.get(position) : defaultValue;
        }
        int index = 0;
        for (E e : iterable) {
            if (index == position) {
                return e;
            }
            index++;
        }
        return defaultValue;
    }

    // ==================== 转换 | Transformation ====================

    /**
     * Lazily transform elements
     * 懒转换
     *
     * @param <F>          from type | 源类型
     * @param <T>          to type | 目标类型
     * @param fromIterable the source iterable | 源可迭代对象
     * @param function     the transform function | 转换函数
     * @return transformed iterable | 转换后的可迭代对象
     */
    public static <F, T> Iterable<T> transform(Iterable<F> fromIterable,
                                                Function<? super F, ? extends T> function) {
        if (fromIterable == null || function == null) {
            return Collections.emptyList();
        }
        return () -> new Iterator<>() {
            private final Iterator<F> iterator = fromIterable.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return function.apply(iterator.next());
            }
        };
    }

    /**
     * Limit the number of elements
     * 限制数量
     *
     * @param <E>       element type | 元素类型
     * @param iterable  the iterable | 可迭代对象
     * @param limitSize the limit | 限制数量
     * @return limited iterable | 限制后的可迭代对象
     */
    public static <E> Iterable<E> limit(Iterable<E> iterable, int limitSize) {
        if (limitSize < 0) {
            throw new IllegalArgumentException("Limit cannot be negative: " + limitSize);
        }
        if (iterable == null || limitSize == 0) {
            return Collections.emptyList();
        }
        return () -> new Iterator<>() {
            private final Iterator<E> iterator = iterable.iterator();
            private int remaining = limitSize;

            @Override
            public boolean hasNext() {
                return remaining > 0 && iterator.hasNext();
            }

            @Override
            public E next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                remaining--;
                return iterator.next();
            }
        };
    }

    /**
     * Skip the first n elements
     * 跳过前 N 个
     *
     * @param <E>          element type | 元素类型
     * @param iterable     the iterable | 可迭代对象
     * @param numberToSkip the number to skip | 跳过数量
     * @return iterable skipping first n elements | 跳过前 n 个元素的可迭代对象
     */
    public static <E> Iterable<E> skip(Iterable<E> iterable, int numberToSkip) {
        if (numberToSkip < 0) {
            throw new IllegalArgumentException("Number to skip cannot be negative: " + numberToSkip);
        }
        if (iterable == null) {
            return Collections.emptyList();
        }
        return () -> {
            Iterator<E> iterator = iterable.iterator();
            for (int i = 0; i < numberToSkip && iterator.hasNext(); i++) {
                iterator.next();
            }
            return iterator;
        };
    }

    /**
     * Cycle through the iterable infinitely
     * 循环迭代
     *
     * @param <E>      element type | 元素类型
     * @param iterable the iterable | 可迭代对象
     * @return cycling iterable | 循环的可迭代对象
     */
    public static <E> Iterable<E> cycle(Iterable<E> iterable) {
        if (iterable == null) {
            return Collections.emptyList();
        }
        return () -> new Iterator<>() {
            private Iterator<E> current = iterable.iterator();
            private boolean hasElements = current.hasNext();

            @Override
            public boolean hasNext() {
                return hasElements;
            }

            @Override
            public E next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                if (!current.hasNext()) {
                    current = iterable.iterator();
                }
                return current.next();
            }
        };
    }

    // ==================== 统计 | Statistics ====================

    /**
     * Get the size of an iterable
     * 计算大小
     *
     * @param iterable the iterable | 可迭代对象
     * @return size | 大小
     */
    public static int size(Iterable<?> iterable) {
        if (iterable == null) {
            return 0;
        }
        if (iterable instanceof Collection<?> collection) {
            return collection.size();
        }
        int count = 0;
        for (Object ignored : iterable) {
            count++;
        }
        return count;
    }

    /**
     * Check if iterable contains an element
     * 检查是否包含
     *
     * @param iterable the iterable | 可迭代对象
     * @param element  the element | 元素
     * @return true if contains | 如果包含则返回 true
     */
    public static boolean contains(Iterable<?> iterable, Object element) {
        if (iterable == null) {
            return false;
        }
        if (iterable instanceof Collection<?> collection) {
            return collection.contains(element);
        }
        for (Object e : iterable) {
            if (Objects.equals(e, element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Count the frequency of an element
     * 统计出现频率
     *
     * @param iterable the iterable | 可迭代对象
     * @param element  the element | 元素
     * @return frequency | 频率
     */
    public static int frequency(Iterable<?> iterable, Object element) {
        if (iterable == null) {
            return 0;
        }
        int count = 0;
        for (Object e : iterable) {
            if (Objects.equals(e, element)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Check if iterable is empty
     * 检查是否为空
     *
     * @param iterable the iterable | 可迭代对象
     * @return true if empty | 如果为空则返回 true
     */
    public static boolean isEmpty(Iterable<?> iterable) {
        if (iterable == null) {
            return true;
        }
        if (iterable instanceof Collection<?> collection) {
            return collection.isEmpty();
        }
        return !iterable.iterator().hasNext();
    }

    // ==================== 转换为集合 | Collection Conversion ====================

    /**
     * Convert to array
     * 转为数组
     *
     * @param <E>      element type | 元素类型
     * @param iterable the iterable | 可迭代对象
     * @param type     the element type class | 元素类型类
     * @return array | 数组
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] toArray(Iterable<? extends E> iterable, Class<E> type) {
        if (iterable == null) {
            return (E[]) Array.newInstance(type, 0);
        }
        List<E> list = new ArrayList<>();
        for (E e : iterable) {
            list.add(e);
        }
        E[] array = (E[]) Array.newInstance(type, list.size());
        return list.toArray(array);
    }

    /**
     * Add all elements to a collection
     * 添加到集合
     *
     * @param <E>             element type | 元素类型
     * @param addTo           the target collection | 目标集合
     * @param elementsToAdd   the elements to add | 要添加的元素
     * @return true if collection was modified | 如果集合被修改则返回 true
     */
    public static <E> boolean addAll(Collection<E> addTo, Iterable<? extends E> elementsToAdd) {
        if (addTo == null || elementsToAdd == null) {
            return false;
        }
        if (elementsToAdd instanceof Collection<? extends E> collection) {
            return addTo.addAll(collection);
        }
        boolean modified = false;
        for (E e : elementsToAdd) {
            if (addTo.add(e)) {
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Remove all matching elements
     * 移除所有匹配元素
     *
     * @param <E>              element type | 元素类型
     * @param removeFrom       the source iterable | 源可迭代对象
     * @param elementsToRemove the elements to remove | 要移除的元素
     * @return true if any elements were removed | 如果有元素被移除则返回 true
     */
    public static <E> boolean removeAll(Iterable<E> removeFrom, Collection<?> elementsToRemove) {
        if (removeFrom == null || elementsToRemove == null || elementsToRemove.isEmpty()) {
            return false;
        }
        boolean modified = false;
        Iterator<E> iterator = removeFrom.iterator();
        while (iterator.hasNext()) {
            if (elementsToRemove.contains(iterator.next())) {
                iterator.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Retain all matching elements
     * 保留所有匹配元素
     *
     * @param <E>              element type | 元素类型
     * @param removeFrom       the source iterable | 源可迭代对象
     * @param elementsToRetain the elements to retain | 要保留的元素
     * @return true if any elements were removed | 如果有元素被移除则返回 true
     */
    public static <E> boolean retainAll(Iterable<E> removeFrom, Collection<?> elementsToRetain) {
        if (removeFrom == null) {
            return false;
        }
        if (elementsToRetain == null || elementsToRetain.isEmpty()) {
            boolean wasEmpty = !removeFrom.iterator().hasNext();
            removeFrom.iterator().forEachRemaining(e -> {});
            Iterator<E> iterator = removeFrom.iterator();
            boolean modified = false;
            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
                modified = true;
            }
            return modified;
        }
        boolean modified = false;
        Iterator<E> iterator = removeFrom.iterator();
        while (iterator.hasNext()) {
            if (!elementsToRetain.contains(iterator.next())) {
                iterator.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Remove elements matching the predicate
     * 移除满足条件的元素
     *
     * @param <E>        element type | 元素类型
     * @param removeFrom the source iterable | 源可迭代对象
     * @param predicate  the predicate | 谓词
     * @return true if any elements were removed | 如果有元素被移除则返回 true
     */
    public static <E> boolean removeIf(Iterable<E> removeFrom, Predicate<? super E> predicate) {
        if (removeFrom == null || predicate == null) {
            return false;
        }
        boolean modified = false;
        Iterator<E> iterator = removeFrom.iterator();
        while (iterator.hasNext()) {
            if (predicate.test(iterator.next())) {
                iterator.remove();
                modified = true;
            }
        }
        return modified;
    }

    // ==================== 相等性检查 | Equality Check ====================

    /**
     * Check if two iterables have equal elements in order
     * 检查元素相等（顺序敏感）
     *
     * @param iterable1 first iterable | 第一个可迭代对象
     * @param iterable2 second iterable | 第二个可迭代对象
     * @return true if equal | 如果相等则返回 true
     */
    public static boolean elementsEqual(Iterable<?> iterable1, Iterable<?> iterable2) {
        if (iterable1 == iterable2) {
            return true;
        }
        if (iterable1 == null || iterable2 == null) {
            return false;
        }
        Iterator<?> it1 = iterable1.iterator();
        Iterator<?> it2 = iterable2.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            if (!Objects.equals(it1.next(), it2.next())) {
                return false;
            }
        }
        return !it1.hasNext() && !it2.hasNext();
    }

    // ==================== 字符串表示 | String Representation ====================

    /**
     * Convert to string representation
     * 转为字符串
     *
     * @param iterable the iterable | 可迭代对象
     * @return string representation | 字符串表示
     */
    public static String toString(Iterable<?> iterable) {
        if (iterable == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object e : iterable) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(e);
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}
