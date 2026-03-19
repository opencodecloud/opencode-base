package cloud.opencode.base.collections;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * IteratorUtil - Iterator Utility Class
 * IteratorUtil - 迭代器工具类
 *
 * <p>Provides comprehensive operations for Iterator including creation, concatenation,
 * filtering, transformation, and conversion.</p>
 * <p>提供全面的迭代器操作，包括创建、连接、过滤、转换和转换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Factory methods for iterators - 迭代器工厂方法</li>
 *   <li>Concatenation and partitioning - 连接和分区</li>
 *   <li>Filtering and transformation - 过滤和转换</li>
 *   <li>PeekingIterator support - PeekingIterator 支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create empty iterator - 创建空迭代器
 * Iterator<String> empty = IteratorUtil.emptyIterator();
 *
 * // Create singleton iterator - 创建单元素迭代器
 * Iterator<String> single = IteratorUtil.singletonIterator("hello");
 *
 * // Create peeking iterator - 创建可查看迭代器
 * PeekingIterator<String> peeking = IteratorUtil.peekingIterator(list.iterator());
 *
 * // Filter iterator - 过滤迭代器
 * Iterator<String> filtered = IteratorUtil.filter(iterator, s -> s.length() > 5);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Most operations are lazy - 大多数操作是惰性的</li>
 *   <li>No additional memory allocation for transformations - 转换不需要额外内存分配</li>
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
public final class IteratorUtil {

    private IteratorUtil() {
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Returns an empty iterator
     * 空迭代器
     *
     * @param <E> element type | 元素类型
     * @return empty iterator | 空迭代器
     */
    public static <E> Iterator<E> emptyIterator() {
        return Collections.emptyIterator();
    }

    /**
     * Returns a singleton iterator
     * 单元素迭代器
     *
     * @param <E>   element type | 元素类型
     * @param value the single value | 单个值
     * @return singleton iterator | 单元素迭代器
     */
    public static <E> Iterator<E> singletonIterator(E value) {
        return new Iterator<>() {
            private boolean hasNext = true;

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public E next() {
                if (!hasNext) {
                    throw new NoSuchElementException();
                }
                hasNext = false;
                return value;
            }
        };
    }

    /**
     * Returns an unmodifiable iterator
     * 不可变迭代器
     *
     * @param <E>      element type | 元素类型
     * @param iterator the iterator | 迭代器
     * @return unmodifiable iterator | 不可变迭代器
     */
    public static <E> Iterator<E> unmodifiableIterator(Iterator<? extends E> iterator) {
        if (iterator == null) {
            return emptyIterator();
        }
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public E next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Iterator is unmodifiable");
            }
        };
    }

    // ==================== 连接 | Concatenation ====================

    /**
     * Concatenate multiple iterators
     * 连接多个迭代器
     *
     * @param <E>    element type | 元素类型
     * @param inputs iterators to concatenate | 要连接的迭代器
     * @return concatenated iterator | 连接后的迭代器
     */
    @SafeVarargs
    public static <E> Iterator<E> concat(Iterator<? extends E>... inputs) {
        if (inputs == null || inputs.length == 0) {
            return emptyIterator();
        }
        return new Iterator<>() {
            private int index = 0;
            private Iterator<? extends E> current = emptyIterator();

            @Override
            public boolean hasNext() {
                while (!current.hasNext() && index < inputs.length) {
                    current = inputs[index++];
                    if (current == null) {
                        current = emptyIterator();
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
     * Partition iterator into chunks
     * 分区
     *
     * @param <E>      element type | 元素类型
     * @param iterator the iterator | 迭代器
     * @param size     chunk size | 分区大小
     * @return iterator of chunks | 分区迭代器
     */
    public static <E> Iterator<List<E>> partition(Iterator<E> iterator, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive: " + size);
        }
        if (iterator == null || !iterator.hasNext()) {
            return emptyIterator();
        }
        return new Iterator<>() {
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

    // ==================== 过滤 | Filtering ====================

    /**
     * Filter iterator
     * 过滤
     *
     * @param <E>        element type | 元素类型
     * @param unfiltered the unfiltered iterator | 未过滤的迭代器
     * @param predicate  the predicate | 谓词
     * @return filtered iterator | 过滤后的迭代器
     */
    public static <E> Iterator<E> filter(Iterator<E> unfiltered, Predicate<? super E> predicate) {
        if (unfiltered == null || predicate == null) {
            return emptyIterator();
        }
        return new Iterator<>() {
            private E next;
            private boolean hasNext;

            private void advance() {
                hasNext = false;
                while (unfiltered.hasNext()) {
                    E candidate = unfiltered.next();
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

    // ==================== 转换 | Transformation ====================

    /**
     * Transform iterator
     * 转换
     *
     * @param <F>          from type | 源类型
     * @param <T>          to type | 目标类型
     * @param fromIterator the source iterator | 源迭代器
     * @param function     the transform function | 转换函数
     * @return transformed iterator | 转换后的迭代器
     */
    public static <F, T> Iterator<T> transform(Iterator<F> fromIterator,
                                                Function<? super F, ? extends T> function) {
        if (fromIterator == null || function == null) {
            return emptyIterator();
        }
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return fromIterator.hasNext();
            }

            @Override
            public T next() {
                return function.apply(fromIterator.next());
            }
        };
    }

    // ==================== 查找 | Searching ====================

    /**
     * Try to find the first matching element
     * 查找
     *
     * @param <E>       element type | 元素类型
     * @param iterator  the iterator | 迭代器
     * @param predicate the predicate | 谓词
     * @return optional containing the first match | 包含第一个匹配的 Optional
     */
    public static <E> Optional<E> tryFind(Iterator<E> iterator, Predicate<? super E> predicate) {
        if (iterator == null || predicate == null) {
            return Optional.empty();
        }
        while (iterator.hasNext()) {
            E element = iterator.next();
            if (predicate.test(element)) {
                return Optional.ofNullable(element);
            }
        }
        return Optional.empty();
    }

    // ==================== 访问 | Access ====================

    /**
     * Get element at position
     * 按索引获取
     *
     * @param <E>      element type | 元素类型
     * @param iterator the iterator | 迭代器
     * @param position the position | 位置
     * @return element at position | 指定位置的元素
     * @throws IndexOutOfBoundsException if position is out of bounds | 如果位置越界
     */
    public static <E> E get(Iterator<E> iterator, int position) {
        if (iterator == null) {
            throw new IndexOutOfBoundsException("Iterator is null");
        }
        if (position < 0) {
            throw new IndexOutOfBoundsException("Position cannot be negative: " + position);
        }
        int index = 0;
        while (iterator.hasNext()) {
            E element = iterator.next();
            if (index == position) {
                return element;
            }
            index++;
        }
        throw new IndexOutOfBoundsException("Position: " + position + ", Size: " + index);
    }

    /**
     * Get next element or default
     * 获取下一个元素（带默认值）
     *
     * @param <E>          element type | 元素类型
     * @param iterator     the iterator | 迭代器
     * @param defaultValue default value | 默认值
     * @return next element or default | 下一个元素或默认值
     */
    public static <E> E getNext(Iterator<? extends E> iterator, E defaultValue) {
        if (iterator == null || !iterator.hasNext()) {
            return defaultValue;
        }
        return iterator.next();
    }

    // ==================== 转换为集合 | Collection Conversion ====================

    /**
     * Convert to array
     * 转为数组
     *
     * @param <E>      element type | 元素类型
     * @param iterator the iterator | 迭代器
     * @param type     the element type class | 元素类型类
     * @return array | 数组
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] toArray(Iterator<? extends E> iterator, Class<E> type) {
        if (iterator == null) {
            return (E[]) Array.newInstance(type, 0);
        }
        List<E> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        E[] array = (E[]) Array.newInstance(type, list.size());
        return list.toArray(array);
    }

    /**
     * Add all elements to a collection
     * 添加到集合
     *
     * @param <E>      element type | 元素类型
     * @param addTo    the target collection | 目标集合
     * @param iterator the iterator | 迭代器
     * @return true if collection was modified | 如果集合被修改则返回 true
     */
    public static <E> boolean addAll(Collection<E> addTo, Iterator<? extends E> iterator) {
        if (addTo == null || iterator == null) {
            return false;
        }
        boolean modified = false;
        while (iterator.hasNext()) {
            if (addTo.add(iterator.next())) {
                modified = true;
            }
        }
        return modified;
    }

    // ==================== 统计 | Statistics ====================

    /**
     * Get the size (consumes iterator)
     * 计算大小（消耗迭代器）
     *
     * @param iterator the iterator | 迭代器
     * @return size | 大小
     */
    public static int size(Iterator<?> iterator) {
        if (iterator == null) {
            return 0;
        }
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }

    /**
     * Check if iterator contains an element
     * 检查是否包含
     *
     * @param iterator the iterator | 迭代器
     * @param element  the element | 元素
     * @return true if contains | 如果包含则返回 true
     */
    public static boolean contains(Iterator<?> iterator, Object element) {
        if (iterator == null) {
            return false;
        }
        while (iterator.hasNext()) {
            if (Objects.equals(iterator.next(), element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove all matching elements
     * 移除所有匹配元素
     *
     * @param <E>              element type | 元素类型
     * @param removeFrom       the source iterator | 源迭代器
     * @param elementsToRemove the elements to remove | 要移除的元素
     * @return true if any elements were removed | 如果有元素被移除则返回 true
     */
    public static <E> boolean removeAll(Iterator<E> removeFrom, Collection<?> elementsToRemove) {
        if (removeFrom == null || elementsToRemove == null || elementsToRemove.isEmpty()) {
            return false;
        }
        boolean modified = false;
        while (removeFrom.hasNext()) {
            if (elementsToRemove.contains(removeFrom.next())) {
                removeFrom.remove();
                modified = true;
            }
        }
        return modified;
    }

    // ==================== 相等性检查 | Equality Check ====================

    /**
     * Check if two iterators have equal elements
     * 检查元素相等
     *
     * @param iterator1 first iterator | 第一个迭代器
     * @param iterator2 second iterator | 第二个迭代器
     * @return true if equal | 如果相等则返回 true
     */
    public static boolean elementsEqual(Iterator<?> iterator1, Iterator<?> iterator2) {
        if (iterator1 == iterator2) {
            return true;
        }
        if (iterator1 == null || iterator2 == null) {
            return false;
        }
        while (iterator1.hasNext() && iterator2.hasNext()) {
            if (!Objects.equals(iterator1.next(), iterator2.next())) {
                return false;
            }
        }
        return !iterator1.hasNext() && !iterator2.hasNext();
    }

    // ==================== 字符串表示 | String Representation ====================

    /**
     * Convert to string representation
     * 转为字符串
     *
     * @param iterator the iterator | 迭代器
     * @return string representation | 字符串表示
     */
    public static String toString(Iterator<?> iterator) {
        if (iterator == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        while (iterator.hasNext()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(iterator.next());
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    // ==================== PeekingIterator | 可查看迭代器 ====================

    /**
     * Create a peeking iterator
     * Peek 迭代器（可查看下一个元素）
     *
     * @param <E>      element type | 元素类型
     * @param iterator the iterator | 迭代器
     * @return peeking iterator | 可查看迭代器
     */
    public static <E> PeekingIterator<E> peekingIterator(Iterator<? extends E> iterator) {
        if (iterator == null) {
            return new PeekingIterator<>() {
                @Override
                public E peek() {
                    throw new NoSuchElementException();
                }

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public E next() {
                    throw new NoSuchElementException();
                }
            };
        }
        if (iterator instanceof PeekingIterator) {
            @SuppressWarnings("unchecked")
            PeekingIterator<E> peeking = (PeekingIterator<E>) iterator;
            return peeking;
        }
        return new PeekingIteratorImpl<>(iterator);
    }

    /**
     * Advance iterator by n positions
     * 消耗并丢弃 N 个元素
     *
     * @param iterator        the iterator | 迭代器
     * @param numberToAdvance the number to advance | 前进数量
     * @return actual number advanced | 实际前进数量
     */
    public static int advance(Iterator<?> iterator, int numberToAdvance) {
        if (iterator == null || numberToAdvance <= 0) {
            return 0;
        }
        int i = 0;
        while (i < numberToAdvance && iterator.hasNext()) {
            iterator.next();
            i++;
        }
        return i;
    }

    /**
     * Limit the number of elements
     * 限制数量
     *
     * @param <E>       element type | 元素类型
     * @param iterator  the iterator | 迭代器
     * @param limitSize the limit | 限制数量
     * @return limited iterator | 限制后的迭代器
     */
    public static <E> Iterator<E> limit(Iterator<E> iterator, int limitSize) {
        if (limitSize < 0) {
            throw new IllegalArgumentException("Limit cannot be negative: " + limitSize);
        }
        if (iterator == null || limitSize == 0) {
            return emptyIterator();
        }
        return new Iterator<>() {
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
     * Cycle through the iterable
     * 循环迭代
     *
     * @param <E>      element type | 元素类型
     * @param iterable the iterable | 可迭代对象
     * @return cycling iterator | 循环的迭代器
     */
    public static <E> Iterator<E> cycle(Iterable<E> iterable) {
        if (iterable == null) {
            return emptyIterator();
        }
        return new Iterator<>() {
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

    // ==================== 内部类 | Internal Classes ====================

    /**
     * PeekingIterator implementation
     */
    private static class PeekingIteratorImpl<E> implements PeekingIterator<E> {
        private final Iterator<? extends E> iterator;
        private boolean hasPeeked;
        private E peekedElement;

        PeekingIteratorImpl(Iterator<? extends E> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return hasPeeked || iterator.hasNext();
        }

        @Override
        public E next() {
            if (!hasPeeked) {
                return iterator.next();
            }
            E result = peekedElement;
            hasPeeked = false;
            peekedElement = null;
            return result;
        }

        @Override
        public E peek() {
            if (!hasPeeked) {
                peekedElement = iterator.next();
                hasPeeked = true;
            }
            return peekedElement;
        }

        @Override
        public void remove() {
            if (hasPeeked) {
                throw new IllegalStateException("Cannot remove after peek()");
            }
            iterator.remove();
        }
    }
}
