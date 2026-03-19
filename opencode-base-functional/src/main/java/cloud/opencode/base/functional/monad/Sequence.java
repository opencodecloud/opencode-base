package cloud.opencode.base.functional.monad;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Sequence - Lazy evaluated sequence
 * Sequence - 惰性求值序列
 *
 * <p>A lazy sequence that computes elements only when needed. Unlike Stream,
 * Sequence can be traversed multiple times. Similar to Kotlin's Sequence or
 * Scala's LazyList.</p>
 * <p>一个只在需要时计算元素的惰性序列。与 Stream 不同，Sequence 可以多次遍历。
 * 类似于 Kotlin 的 Sequence 或 Scala 的 LazyList。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lazy evaluation - 惰性求值</li>
 *   <li>Reusable (can traverse multiple times) - 可重用（可多次遍历）</li>
 *   <li>Memory efficient (no intermediate collections) - 内存高效（无中间集合）</li>
 *   <li>Short-circuit operations - 短路操作</li>
 *   <li>Infinite sequences support - 支持无限序列</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create from elements
 * Sequence<Integer> seq = Sequence.of(1, 2, 3, 4, 5);
 *
 * // Lazy operations - nothing computed yet
 * Sequence<Integer> result = seq
 *     .filter(x -> x > 2)
 *     .map(x -> x * 2)
 *     .take(2);
 *
 * // Computed only when consuming
 * List<Integer> list = result.toList();  // [6, 8]
 *
 * // Can be traversed again
 * int sum = result.fold(0, Integer::sum);  // 14
 *
 * // Infinite sequence
 * Sequence<Integer> naturals = Sequence.iterate(1, n -> n + 1);
 * List<Integer> first10 = naturals.take(10).toList();
 *
 * // Generate sequence
 * Sequence<Double> randoms = Sequence.generate(Math::random);
 * }</pre>
 *
 * <p><strong>Comparison with Stream | 与 Stream 对比:</strong></p>
 * <ul>
 *   <li>Stream: single-use, parallel support - 一次性使用，支持并行</li>
 *   <li>Sequence: reusable, sequential only - 可重用，仅顺序执行</li>
 * </ul>
 *
 * @param <T> element type - 元素类型
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public final class Sequence<T> implements Iterable<T> {

    private final Supplier<Iterator<T>> iteratorSupplier;

    private Sequence(Supplier<Iterator<T>> iteratorSupplier) {
        this.iteratorSupplier = iteratorSupplier;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create an empty sequence.
     * 创建空序列。
     *
     * @param <T> element type - 元素类型
     * @return empty sequence - 空序列
     */
    public static <T> Sequence<T> empty() {
        return new Sequence<>(Collections::emptyIterator);
    }

    /**
     * Create a sequence from elements.
     * 从元素创建序列。
     *
     * @param <T>      element type - 元素类型
     * @param elements the elements - 元素
     * @return new sequence - 新序列
     */
    @SafeVarargs
    public static <T> Sequence<T> of(T... elements) {
        return new Sequence<>(() -> Arrays.asList(elements).iterator());
    }

    /**
     * Create a sequence from iterable.
     * 从可迭代对象创建序列。
     *
     * @param <T>      element type - 元素类型
     * @param iterable the iterable - 可迭代对象
     * @return new sequence - 新序列
     */
    public static <T> Sequence<T> from(Iterable<T> iterable) {
        Objects.requireNonNull(iterable, "iterable must not be null");
        return new Sequence<>(iterable::iterator);
    }

    /**
     * Create a sequence from stream (single-use).
     * 从流创建序列（一次性使用）。
     *
     * @param <T>    element type - 元素类型
     * @param stream the stream - 流
     * @return new sequence - 新序列
     */
    public static <T> Sequence<T> fromStream(Stream<T> stream) {
        Objects.requireNonNull(stream, "stream must not be null");
        List<T> list = stream.toList();
        return new Sequence<>(list::iterator);
    }

    /**
     * Create an infinite sequence by repeatedly applying a function.
     * 通过重复应用函数创建无限序列。
     *
     * @param <T>  element type - 元素类型
     * @param seed initial value - 初始值
     * @param f    function to generate next - 生成下一个的函数
     * @return infinite sequence - 无限序列
     */
    public static <T> Sequence<T> iterate(T seed, UnaryOperator<T> f) {
        Objects.requireNonNull(f, "function must not be null");
        return new Sequence<>(() -> new Iterator<>() {
            private T current = seed;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                T result = current;
                current = f.apply(current);
                return result;
            }
        });
    }

    /**
     * Create an infinite sequence from a supplier.
     * 从供应商创建无限序列。
     *
     * @param <T>      element type - 元素类型
     * @param supplier value supplier - 值供应商
     * @return infinite sequence - 无限序列
     */
    public static <T> Sequence<T> generate(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        return new Sequence<>(() -> new Iterator<>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                return supplier.get();
            }
        });
    }

    /**
     * Create a sequence of integers from start (inclusive) to end (exclusive).
     * 创建从 start（包含）到 end（不包含）的整数序列。
     *
     * @param start start value (inclusive) - 起始值（包含）
     * @param end   end value (exclusive) - 结束值（不包含）
     * @return integer sequence - 整数序列
     */
    public static Sequence<Integer> range(int start, int end) {
        return new Sequence<>(() -> new Iterator<>() {
            private int current = start;

            @Override
            public boolean hasNext() {
                return current < end;
            }

            @Override
            public Integer next() {
                if (!hasNext()) throw new NoSuchElementException();
                return current++;
            }
        });
    }

    /**
     * Create a sequence of integers from start (inclusive) to end (inclusive).
     * 创建从 start（包含）到 end（包含）的整数序列。
     *
     * @param start start value (inclusive) - 起始值（包含）
     * @param end   end value (inclusive) - 结束值（包含）
     * @return integer sequence - 整数序列
     */
    public static Sequence<Integer> rangeClosed(int start, int end) {
        return range(start, end + 1);
    }

    // ==================== Transformation | 转换操作 ====================

    /**
     * Map each element to a new value.
     * 将每个元素映射为新值。
     *
     * @param <R>    result type - 结果类型
     * @param mapper mapping function - 映射函数
     * @return mapped sequence - 映射后的序列
     */
    public <R> Sequence<R> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return new Sequence<>(() -> new Iterator<>() {
            private final Iterator<T> source = iterator();

            @Override
            public boolean hasNext() {
                return source.hasNext();
            }

            @Override
            public R next() {
                return mapper.apply(source.next());
            }
        });
    }

    /**
     * Map each element to a sequence and flatten.
     * 将每个元素映射为序列并展平。
     *
     * @param <R>    result type - 结果类型
     * @param mapper mapping function - 映射函数
     * @return flattened sequence - 展平后的序列
     */
    public <R> Sequence<R> flatMap(Function<? super T, ? extends Sequence<R>> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return new Sequence<>(() -> new Iterator<>() {
            private final Iterator<T> source = iterator();
            private Iterator<R> current = Collections.emptyIterator();

            @Override
            public boolean hasNext() {
                while (!current.hasNext()) {
                    if (!source.hasNext()) {
                        return false;
                    }
                    current = mapper.apply(source.next()).iterator();
                }
                return true;
            }

            @Override
            public R next() {
                if (!hasNext()) throw new NoSuchElementException();
                return current.next();
            }
        });
    }

    /**
     * Filter elements matching the predicate.
     * 过滤匹配谓词的元素。
     *
     * @param predicate filter predicate - 过滤谓词
     * @return filtered sequence - 过滤后的序列
     */
    public Sequence<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        return new Sequence<>(() -> new Iterator<>() {
            private final Iterator<T> source = iterator();
            private T nextItem;
            private boolean hasNextItem = false;

            @Override
            public boolean hasNext() {
                if (hasNextItem) return true;
                while (source.hasNext()) {
                    T item = source.next();
                    if (predicate.test(item)) {
                        nextItem = item;
                        hasNextItem = true;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                hasNextItem = false;
                return nextItem;
            }
        });
    }

    /**
     * Filter elements not matching the predicate.
     * 过滤不匹配谓词的元素。
     *
     * @param predicate filter predicate - 过滤谓词
     * @return filtered sequence - 过滤后的序列
     */
    public Sequence<T> filterNot(Predicate<? super T> predicate) {
        return filter(predicate.negate());
    }

    /**
     * Take only the first n elements.
     * 只取前 n 个元素。
     *
     * @param n number to take - 要取的数量
     * @return truncated sequence - 截断后的序列
     */
    public Sequence<T> take(int n) {
        if (n <= 0) return empty();
        return new Sequence<>(() -> new Iterator<>() {
            private final Iterator<T> source = iterator();
            private int remaining = n;

            @Override
            public boolean hasNext() {
                return remaining > 0 && source.hasNext();
            }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                remaining--;
                return source.next();
            }
        });
    }

    /**
     * Take elements while predicate is true.
     * 取元素直到谓词为假。
     *
     * @param predicate the predicate - 谓词
     * @return truncated sequence - 截断后的序列
     */
    public Sequence<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        return new Sequence<>(() -> new Iterator<>() {
            private final Iterator<T> source = iterator();
            private T nextItem;
            private boolean hasNextItem = false;
            private boolean done = false;

            @Override
            public boolean hasNext() {
                if (done) return false;
                if (hasNextItem) return true;
                if (!source.hasNext()) {
                    done = true;
                    return false;
                }
                T item = source.next();
                if (!predicate.test(item)) {
                    done = true;
                    return false;
                }
                nextItem = item;
                hasNextItem = true;
                return true;
            }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                hasNextItem = false;
                return nextItem;
            }
        });
    }

    /**
     * Drop the first n elements.
     * 丢弃前 n 个元素。
     *
     * @param n number to drop - 要丢弃的数量
     * @return remaining sequence - 剩余序列
     */
    public Sequence<T> drop(int n) {
        if (n <= 0) return this;
        return new Sequence<>(() -> {
            Iterator<T> source = iterator();
            int remaining = n;
            while (remaining > 0 && source.hasNext()) {
                source.next();
                remaining--;
            }
            return source;
        });
    }

    /**
     * Drop elements while predicate is true.
     * 丢弃元素直到谓词为假。
     *
     * @param predicate the predicate - 谓词
     * @return remaining sequence - 剩余序列
     */
    public Sequence<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        return new Sequence<>(() -> {
            Iterator<T> source = iterator();
            while (source.hasNext()) {
                T item = source.next();
                if (!predicate.test(item)) {
                    // Found first non-matching, need to include it
                    return new Iterator<>() {
                        private T first = item;
                        private boolean hasFirst = true;

                        @Override
                        public boolean hasNext() {
                            return hasFirst || source.hasNext();
                        }

                        @Override
                        public T next() {
                            if (hasFirst) {
                                hasFirst = false;
                                return first;
                            }
                            return source.next();
                        }
                    };
                }
            }
            return Collections.emptyIterator();
        });
    }

    /**
     * Remove duplicate elements.
     * 移除重复元素。
     *
     * @return distinct sequence - 去重后的序列
     */
    public Sequence<T> distinct() {
        return new Sequence<>(() -> {
            Iterator<T> source = iterator();
            Set<T> seen = new HashSet<>();
            return new Iterator<>() {
                private T nextItem;
                private boolean hasNextItem = false;

                @Override
                public boolean hasNext() {
                    if (hasNextItem) return true;
                    while (source.hasNext()) {
                        T item = source.next();
                        if (seen.add(item)) {
                            nextItem = item;
                            hasNextItem = true;
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public T next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    hasNextItem = false;
                    return nextItem;
                }
            };
        });
    }

    /**
     * Sort the sequence by natural order.
     * 按自然顺序排序序列。
     *
     * @return sorted sequence - 排序后的序列
     */
    @SuppressWarnings("unchecked")
    public Sequence<T> sorted() {
        return sorted((Comparator<T>) Comparator.naturalOrder());
    }

    /**
     * Sort the sequence by comparator.
     * 按比较器排序序列。
     *
     * @param comparator the comparator - 比较器
     * @return sorted sequence - 排序后的序列
     */
    public Sequence<T> sorted(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator must not be null");
        return new Sequence<>(() -> {
            List<T> list = toList();
            list.sort(comparator);
            return list.iterator();
        });
    }

    /**
     * Zip with another sequence.
     * 与另一个序列合并。
     *
     * @param <U>   other element type - 另一个元素类型
     * @param <R>   result type - 结果类型
     * @param other other sequence - 另一个序列
     * @param zipper combining function - 组合函数
     * @return zipped sequence - 合并后的序列
     */
    public <U, R> Sequence<R> zip(Sequence<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        Objects.requireNonNull(other, "other must not be null");
        Objects.requireNonNull(zipper, "zipper must not be null");
        return new Sequence<>(() -> new Iterator<>() {
            private final Iterator<T> iterA = iterator();
            private final Iterator<U> iterB = other.iterator();

            @Override
            public boolean hasNext() {
                return iterA.hasNext() && iterB.hasNext();
            }

            @Override
            public R next() {
                return zipper.apply(iterA.next(), iterB.next());
            }
        });
    }

    /**
     * Zip with index.
     * 与索引合并。
     *
     * @return sequence of indexed elements - 带索引元素的序列
     */
    public Sequence<IndexedValue<T>> zipWithIndex() {
        return new Sequence<>(() -> new Iterator<>() {
            private final Iterator<T> source = iterator();
            private int index = 0;

            @Override
            public boolean hasNext() {
                return source.hasNext();
            }

            @Override
            public IndexedValue<T> next() {
                return new IndexedValue<>(index++, source.next());
            }
        });
    }

    // ==================== Terminal Operations | 终端操作 ====================

    /**
     * Fold elements from left with initial value.
     * 从左侧使用初始值折叠元素。
     *
     * @param <R>     result type - 结果类型
     * @param initial initial value - 初始值
     * @param folder  folding function - 折叠函数
     * @return folded result - 折叠结果
     */
    public <R> R fold(R initial, BiFunction<? super R, ? super T, ? extends R> folder) {
        Objects.requireNonNull(folder, "folder must not be null");
        R result = initial;
        for (T item : this) {
            result = folder.apply(result, item);
        }
        return result;
    }

    /**
     * Reduce elements (no initial value).
     * 归约元素（无初始值）。
     *
     * @param reducer reducing function - 归约函数
     * @return optional result - 可选结果
     */
    public Optional<T> reduce(BinaryOperator<T> reducer) {
        Objects.requireNonNull(reducer, "reducer must not be null");
        Iterator<T> iter = iterator();
        if (!iter.hasNext()) return Optional.empty();
        T result = iter.next();
        while (iter.hasNext()) {
            result = reducer.apply(result, iter.next());
        }
        return Optional.of(result);
    }

    /**
     * Collect to a list.
     * 收集为列表。
     *
     * @return list of elements - 元素列表
     */
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        for (T item : this) {
            list.add(item);
        }
        return list;
    }

    /**
     * Collect to a set.
     * 收集为集合。
     *
     * @return set of elements - 元素集合
     */
    public Set<T> toSet() {
        Set<T> set = new HashSet<>();
        for (T item : this) {
            set.add(item);
        }
        return set;
    }

    /**
     * Collect using a collector.
     * 使用收集器收集。
     *
     * @param <R>       result type - 结果类型
     * @param <A>       accumulator type - 累加器类型
     * @param collector the collector - 收集器
     * @return collected result - 收集结果
     */
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        A container = collector.supplier().get();
        var accumulator = collector.accumulator();
        for (T item : this) {
            accumulator.accept(container, item);
        }
        return collector.finisher().apply(container);
    }

    /**
     * Find first element matching predicate.
     * 查找第一个匹配谓词的元素。
     *
     * @param predicate the predicate - 谓词
     * @return optional first match - 可选的第一个匹配
     */
    public Optional<T> find(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        for (T item : this) {
            if (predicate.test(item)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    /**
     * Find first element.
     * 查找第一个元素。
     *
     * @return optional first element - 可选的第一个元素
     */
    public Optional<T> first() {
        Iterator<T> iter = iterator();
        return iter.hasNext() ? Optional.of(iter.next()) : Optional.empty();
    }

    /**
     * Find last element.
     * 查找最后一个元素。
     *
     * @return optional last element - 可选的最后一个元素
     */
    public Optional<T> last() {
        T last = null;
        boolean hasAny = false;
        for (T item : this) {
            last = item;
            hasAny = true;
        }
        return hasAny ? Optional.of(last) : Optional.empty();
    }

    /**
     * Check if any element matches predicate.
     * 检查是否有任何元素匹配谓词。
     *
     * @param predicate the predicate - 谓词
     * @return true if any match - 如果有匹配返回 true
     */
    public boolean any(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        for (T item : this) {
            if (predicate.test(item)) return true;
        }
        return false;
    }

    /**
     * Check if all elements match predicate.
     * 检查是否所有元素都匹配谓词。
     *
     * @param predicate the predicate - 谓词
     * @return true if all match - 如果全部匹配返回 true
     */
    public boolean all(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        for (T item : this) {
            if (!predicate.test(item)) return false;
        }
        return true;
    }

    /**
     * Check if no elements match predicate.
     * 检查是否没有元素匹配谓词。
     *
     * @param predicate the predicate - 谓词
     * @return true if none match - 如果全不匹配返回 true
     */
    public boolean none(Predicate<? super T> predicate) {
        return !any(predicate);
    }

    /**
     * Count elements.
     * 计数元素。
     *
     * @return element count - 元素数量
     */
    public long count() {
        long count = 0;
        for (T ignored : this) {
            count++;
        }
        return count;
    }

    /**
     * Perform action on each element.
     * 对每个元素执行操作。
     *
     * @param action the action - 操作
     */
    @Override
    public void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action must not be null");
        for (T item : this) {
            action.accept(item);
        }
    }

    /**
     * Convert to stream.
     * 转换为流。
     *
     * @return stream of elements - 元素流
     */
    public Stream<T> toStream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public Iterator<T> iterator() {
        return iteratorSupplier.get();
    }

    // ==================== Helper Classes | 辅助类 ====================

    /**
     * Value with its index.
     * 带索引的值。
     *
     * @param <T>   value type - 值类型
     * @param index the index - 索引
     * @param value the value - 值
     */
    public record IndexedValue<T>(int index, T value) {}
}
