package cloud.opencode.base.collections.immutable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * PersistentList - Persistent immutable linked list with structural sharing
 * PersistentList - 基于结构共享的持久化不可变链表
 *
 * <p>A persistent list based on cons-cell linked list. All mutation operations
 * return a new list while sharing structure with the original, making it ideal
 * for functional programming and concurrent access.</p>
 * <p>基于 cons-cell 链表的持久化列表。所有变更操作都返回一个新列表，同时与原列表
 * 共享结构，非常适合函数式编程和并发访问。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Structural sharing - 结构共享</li>
 *   <li>Immutable - 不可变</li>
 *   <li>O(1) prepend and tail - O(1) 前插和取尾</li>
 *   <li>Functional operations (map, filter) - 函数式操作（映射、过滤）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create list - 创建列表
 * PersistentList<String> list = PersistentList.of("a", "b", "c");
 *
 * // Prepend element (O(1)) - 前插元素 (O(1))
 * PersistentList<String> list2 = list.prepend("z"); // ["z", "a", "b", "c"]
 * // Original list is unchanged - 原列表不变
 * // list is still ["a", "b", "c"]
 *
 * // Tail (O(1)) - 获取尾部 (O(1))
 * PersistentList<String> tail = list.tail(); // ["b", "c"]
 *
 * // Map and filter - 映射和过滤
 * PersistentList<Integer> lengths = list.map(String::length);
 * PersistentList<String> filtered = list.filter(s -> s.compareTo("b") >= 0);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>prepend: O(1) - prepend: O(1)</li>
 *   <li>head / tail: O(1) - head / tail: O(1)</li>
 *   <li>append / reversed / map / filter: O(n) - append / reversed / map / filter: O(n)</li>
 *   <li>contains / size: O(n) / O(1) - contains / size: O(n) / O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @param <E> element type | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public sealed interface PersistentList<E> permits PersistentList.Nil, PersistentList.Cons {

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Return an empty persistent list.
     * 返回一个空的持久化列表。
     *
     * @param <E> element type | 元素类型
     * @return empty persistent list | 空的持久化列表
     */
    @SuppressWarnings("unchecked")
    static <E> PersistentList<E> empty() {
        return (PersistentList<E>) Nil.INSTANCE;
    }

    /**
     * Create a persistent list from the given elements.
     * 从给定的元素创建持久化列表。
     *
     * <p>Elements are stored in the order provided.</p>
     * <p>元素按提供的顺序存储。</p>
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return persistent list containing the elements | 包含元素的持久化列表
     */
    @SafeVarargs
    static <E> PersistentList<E> of(E... elements) {
        Objects.requireNonNull(elements, "Elements array must not be null");
        PersistentList<E> result = empty();
        for (int i = elements.length - 1; i >= 0; i--) {
            result = result.prepend(elements[i]);
        }
        return result;
    }

    /**
     * Create a persistent list from an iterable.
     * 从可迭代对象创建持久化列表。
     *
     * @param <E>      element type | 元素类型
     * @param iterable the iterable | 可迭代对象
     * @return persistent list containing the elements | 包含元素的持久化列表
     */
    static <E> PersistentList<E> from(Iterable<E> iterable) {
        Objects.requireNonNull(iterable, "Iterable must not be null");
        List<E> buffer = new ArrayList<>();
        iterable.forEach(buffer::add);
        PersistentList<E> result = empty();
        for (int i = buffer.size() - 1; i >= 0; i--) {
            result = result.prepend(buffer.get(i));
        }
        return result;
    }

    // ==================== 核心操作 | Core Operations ====================

    /**
     * Prepend an element to the front of this list (O(1)).
     * 在列表前端插入一个元素 (O(1))。
     *
     * @param element the element to prepend | 要前插的元素
     * @return a new list with the element prepended | 前插元素后的新列表
     */
    PersistentList<E> prepend(E element);

    /**
     * Append an element to the end of this list (O(n)).
     * 在列表末尾追加一个元素 (O(n))。
     *
     * @param element the element to append | 要追加的元素
     * @return a new list with the element appended | 追加元素后的新列表
     */
    PersistentList<E> append(E element);

    /**
     * Return the tail of this list (all elements except the first) (O(1)).
     * 返回列表的尾部（除第一个元素外的所有元素）(O(1))。
     *
     * @return the tail of the list | 列表的尾部
     * @throws NoSuchElementException if the list is empty | 如果列表为空
     */
    PersistentList<E> tail();

    /**
     * Return the head (first element) of this list (O(1)).
     * 返回列表的头部（第一个元素）(O(1))。
     *
     * @return the head element | 头部元素
     * @throws NoSuchElementException if the list is empty | 如果列表为空
     */
    E head();

    /**
     * Return the number of elements in this list.
     * 返回列表中的元素数量。
     *
     * @return the size | 大小
     */
    int size();

    /**
     * Check if this list is empty.
     * 检查列表是否为空。
     *
     * @return true if the list is empty | 如果列表为空则返回 true
     */
    boolean isEmpty();

    /**
     * Check if this list contains the given element.
     * 检查列表是否包含给定元素。
     *
     * @param element the element to search for | 要搜索的元素
     * @return true if the element is found | 如果找到元素则返回 true
     */
    boolean contains(Object element);

    // ==================== 函数式操作 | Functional Operations ====================

    /**
     * Return a reversed copy of this list (O(n)).
     * 返回此列表的反转副本 (O(n))。
     *
     * @return reversed list | 反转后的列表
     */
    PersistentList<E> reversed();

    /**
     * Apply a function to each element and return a new list (O(n)).
     * 对每个元素应用函数并返回新列表 (O(n))。
     *
     * @param <R> result element type | 结果元素类型
     * @param fn  the mapping function | 映射函数
     * @return a new list with mapped elements | 包含映射元素的新列表
     */
    <R> PersistentList<R> map(Function<? super E, ? extends R> fn);

    /**
     * Filter elements by a predicate and return a new list (O(n)).
     * 按谓词过滤元素并返回新列表 (O(n))。
     *
     * @param predicate the filter predicate | 过滤谓词
     * @return a new list with only matching elements | 仅包含匹配元素的新列表
     */
    PersistentList<E> filter(Predicate<? super E> predicate);

    // ==================== 转换操作 | Conversion Operations ====================

    /**
     * Convert this persistent list to a JDK {@link List}.
     * 将此持久化列表转换为 JDK {@link List}。
     *
     * @return an unmodifiable list containing all elements | 包含所有元素的不可修改列表
     */
    List<E> toList();

    /**
     * Return a sequential stream over the elements.
     * 返回元素上的顺序流。
     *
     * @return a stream | 流
     */
    Stream<E> stream();

    /**
     * Return an iterator over the elements.
     * 返回元素上的迭代器。
     *
     * @return an iterator | 迭代器
     */
    Iterator<E> iterator();

    // ==================== Nil 实现 | Nil Implementation ====================

    /**
     * Nil - The empty persistent list.
     * Nil - 空的持久化列表。
     *
     * @param <E> element type | 元素类型
     */
    record Nil<E>() implements PersistentList<E> {

        /** Singleton instance. | 单例实例。 */
        @SuppressWarnings("rawtypes")
        static final Nil INSTANCE = new Nil();

        @Override
        public PersistentList<E> prepend(E element) {
            return new Cons<>(element, this, 1);
        }

        @Override
        public PersistentList<E> append(E element) {
            return new Cons<>(element, this, 1);
        }

        @Override
        public PersistentList<E> tail() {
            throw new NoSuchElementException("tail() called on empty PersistentList");
        }

        @Override
        public E head() {
            throw new NoSuchElementException("head() called on empty PersistentList");
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean contains(Object element) {
            return false;
        }

        @Override
        public PersistentList<E> reversed() {
            return this;
        }

        @Override
        public <R> PersistentList<R> map(Function<? super E, ? extends R> fn) {
            Objects.requireNonNull(fn, "Mapping function must not be null");
            return empty();
        }

        @Override
        public PersistentList<E> filter(Predicate<? super E> predicate) {
            Objects.requireNonNull(predicate, "Predicate must not be null");
            return this;
        }

        @Override
        public List<E> toList() {
            return List.of();
        }

        @Override
        public Stream<E> stream() {
            return Stream.empty();
        }

        @Override
        public Iterator<E> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public String toString() {
            return "PersistentList[]";
        }
    }

    // ==================== Cons 实现 | Cons Implementation ====================

    /**
     * Cons - A non-empty persistent list node (head + tail).
     * Cons - 非空持久化列表节点（头部 + 尾部）。
     *
     * @param <E>  element type | 元素类型
     * @param head the first element | 第一个元素
     * @param tail the rest of the list | 列表的其余部分
     * @param size the number of elements | 元素数量
     */
    record Cons<E>(E head, PersistentList<E> tail, int size) implements PersistentList<E> {

        /**
         * Canonical constructor with validation.
         * 带验证的规范构造方法。
         */
        public Cons {
            Objects.requireNonNull(tail, "Tail must not be null");
            if (size <= 0) {
                throw new IllegalArgumentException("Size must be positive");
            }
        }

        @Override
        public PersistentList<E> prepend(E element) {
            if (size == Integer.MAX_VALUE) {
                throw new IllegalStateException("PersistentList size would exceed Integer.MAX_VALUE");
            }
            return new Cons<>(element, this, size + 1);
        }

        @Override
        public PersistentList<E> append(E element) {
            // Rebuild the list with the new element at the end
            // 在末尾添加新元素，重新构建列表
            return appendRecursive(this, element);
        }

        private static <E> PersistentList<E> appendRecursive(PersistentList<E> list, E element) {
            // Use iterative approach to avoid stack overflow on large lists
            // 使用迭代方式避免大列表上的栈溢出
            List<E> buffer = new ArrayList<>();
            PersistentList<E> current = list;
            while (!current.isEmpty()) {
                buffer.add(current.head());
                current = current.tail();
            }
            PersistentList<E> result = new Cons<>(element, empty(), 1);
            for (int i = buffer.size() - 1; i >= 0; i--) {
                result = result.prepend(buffer.get(i));
            }
            return result;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object element) {
            PersistentList<E> current = this;
            while (!current.isEmpty()) {
                E h = current.head();
                if (Objects.equals(h, element)) {
                    return true;
                }
                current = current.tail();
            }
            return false;
        }

        @Override
        public PersistentList<E> reversed() {
            PersistentList<E> result = empty();
            PersistentList<E> current = this;
            while (!current.isEmpty()) {
                result = result.prepend(current.head());
                current = current.tail();
            }
            return result;
        }

        @Override
        public <R> PersistentList<R> map(Function<? super E, ? extends R> fn) {
            Objects.requireNonNull(fn, "Mapping function must not be null");
            // Collect mapped elements, then build in reverse to preserve order
            // 收集映射后的元素，然后反向构建以保持顺序
            List<R> buffer = new ArrayList<>(size);
            PersistentList<E> current = this;
            while (!current.isEmpty()) {
                buffer.add(fn.apply(current.head()));
                current = current.tail();
            }
            PersistentList<R> result = empty();
            for (int i = buffer.size() - 1; i >= 0; i--) {
                result = result.prepend(buffer.get(i));
            }
            return result;
        }

        @Override
        public PersistentList<E> filter(Predicate<? super E> predicate) {
            Objects.requireNonNull(predicate, "Predicate must not be null");
            List<E> buffer = new ArrayList<>();
            PersistentList<E> current = this;
            while (!current.isEmpty()) {
                E h = current.head();
                if (predicate.test(h)) {
                    buffer.add(h);
                }
                current = current.tail();
            }
            PersistentList<E> result = empty();
            for (int i = buffer.size() - 1; i >= 0; i--) {
                result = result.prepend(buffer.get(i));
            }
            return result;
        }

        @Override
        public List<E> toList() {
            List<E> result = new ArrayList<>(size);
            PersistentList<E> current = this;
            while (!current.isEmpty()) {
                result.add(current.head());
                current = current.tail();
            }
            return Collections.unmodifiableList(result);
        }

        @Override
        public Stream<E> stream() {
            Spliterator<E> spliterator = Spliterators.spliterator(
                    iterator(), size, Spliterator.ORDERED | Spliterator.SIZED | Spliterator.IMMUTABLE
            );
            return StreamSupport.stream(spliterator, false);
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<>() {
                private PersistentList<E> current = Cons.this;

                @Override
                public boolean hasNext() {
                    return !current.isEmpty();
                }

                @Override
                public E next() {
                    if (current.isEmpty()) {
                        throw new NoSuchElementException();
                    }
                    E value = current.head();
                    current = current.tail();
                    return value;
                }
            };
        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner(", ", "PersistentList[", "]");
            PersistentList<E> current = this;
            while (!current.isEmpty()) {
                joiner.add(String.valueOf(current.head()));
                current = current.tail();
            }
            return joiner.toString();
        }
    }
}
