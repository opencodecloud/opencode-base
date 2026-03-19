package cloud.opencode.base.collections;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Streams - Enhanced Stream Utilities
 * Streams - 增强的流工具类
 *
 * <p>This class provides additional utility methods for working with Java Streams
 * that are not available in the standard library.</p>
 * <p>该类提供处理 Java 流的额外工具方法，这些方法在标准库中不可用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>zip - Combine two streams element-by-element - 逐元素组合两个流</li>
 *   <li>findLast - Find the last element of a stream - 查找流的最后一个元素</li>
 *   <li>mapWithIndex - Map with element index - 带索引的映射</li>
 *   <li>forEachPair - Process consecutive pairs - 处理连续对</li>
 *   <li>concat - Concatenate multiple streams - 连接多个流</li>
 *   <li>interleave - Interleave two streams - 交错两个流</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Zip two streams | 合并两个流
 * Stream<String> names = Stream.of("Alice", "Bob");
 * Stream<Integer> ages = Stream.of(30, 25);
 * Stream<String> result = Streams.zip(names, ages,
 *     (name, age) -> name + " is " + age);
 * // ["Alice is 30", "Bob is 25"]
 *
 * // Find last element | 查找最后一个元素
 * Optional<String> last = Streams.findLast(Stream.of("a", "b", "c"));
 * // Optional["c"]
 *
 * // Map with index | 带索引映射
 * Stream<String> indexed = Streams.mapWithIndex(
 *     Stream.of("a", "b", "c"),
 *     (element, index) -> index + ": " + element);
 * // ["0: a", "1: b", "2: c"]
 *
 * // Process pairs | 处理连续对
 * Streams.forEachPair(Stream.of(1, 2, 3, 4),
 *     (a, b) -> System.out.println(a + " + " + b + " = " + (a + b)));
 * // Prints: "1 + 2 = 3", "2 + 3 = 5", "3 + 4 = 7"
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>All methods in this class are stateless and thread-safe.</p>
 * <p>此类中的所有方法都是无状态和线程安全的。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class Streams {

    private Streams() {
        // Utility class, not instantiable
    }

    // ==================== Zip 操作 | Zip Operations ====================

    /**
     * Zips two streams together using the provided combiner function.
     * The resulting stream length is the minimum of the two input stream lengths.
     * 使用提供的组合函数将两个流合并在一起。结果流的长度是两个输入流长度的最小值。
     *
     * @param <A>      type of first stream | 第一个流的类型
     * @param <B>      type of second stream | 第二个流的类型
     * @param <R>      type of result | 结果类型
     * @param streamA  first stream | 第一个流
     * @param streamB  second stream | 第二个流
     * @param combiner function to combine elements | 组合元素的函数
     * @return zipped stream | 合并后的流
     * @throws NullPointerException if any argument is null | 如果任何参数为 null
     */
    public static <A, B, R> Stream<R> zip(
            Stream<A> streamA,
            Stream<B> streamB,
            BiFunction<? super A, ? super B, ? extends R> combiner) {
        Objects.requireNonNull(streamA, "streamA must not be null");
        Objects.requireNonNull(streamB, "streamB must not be null");
        Objects.requireNonNull(combiner, "combiner must not be null");

        Spliterator<A> splitA = streamA.spliterator();
        Spliterator<B> splitB = streamB.spliterator();

        // Calculate characteristics
        int characteristics = splitA.characteristics() & splitB.characteristics()
                & ~(Spliterator.DISTINCT | Spliterator.SORTED);

        // Calculate size
        long size = Math.min(splitA.estimateSize(), splitB.estimateSize());

        Iterator<A> iterA = Spliterators.iterator(splitA);
        Iterator<B> iterB = Spliterators.iterator(splitB);

        return StreamSupport.stream(new Spliterators.AbstractSpliterator<R>(size, characteristics) {
            @Override
            public boolean tryAdvance(Consumer<? super R> action) {
                if (iterA.hasNext() && iterB.hasNext()) {
                    action.accept(combiner.apply(iterA.next(), iterB.next()));
                    return true;
                }
                return false;
            }
        }, streamA.isParallel() || streamB.isParallel());
    }

    /**
     * Zips two collections together using the provided combiner function.
     * 使用提供的组合函数将两个集合合并在一起。
     *
     * @param <A>        type of first collection | 第一个集合的类型
     * @param <B>        type of second collection | 第二个集合的类型
     * @param <R>        type of result | 结果类型
     * @param collectionA first collection | 第一个集合
     * @param collectionB second collection | 第二个集合
     * @param combiner   function to combine elements | 组合元素的函数
     * @return zipped stream | 合并后的流
     */
    public static <A, B, R> Stream<R> zip(
            Collection<A> collectionA,
            Collection<B> collectionB,
            BiFunction<? super A, ? super B, ? extends R> combiner) {
        return zip(collectionA.stream(), collectionB.stream(), combiner);
    }

    /**
     * Zips a stream with its indices, producing pairs of (index, element).
     * 将流与其索引合并，生成（索引，元素）对。
     *
     * @param <T>    element type | 元素类型
     * @param stream the stream | 流
     * @return stream of indexed elements | 带索引元素的流
     */
    public static <T> Stream<IndexedElement<T>> zipWithIndex(Stream<T> stream) {
        Objects.requireNonNull(stream, "stream must not be null");

        Spliterator<T> split = stream.spliterator();
        Iterator<T> iter = Spliterators.iterator(split);

        return StreamSupport.stream(new Spliterators.AbstractSpliterator<IndexedElement<T>>(
                split.estimateSize(), split.characteristics()) {
            private long index = 0;

            @Override
            public boolean tryAdvance(Consumer<? super IndexedElement<T>> action) {
                if (iter.hasNext()) {
                    action.accept(new IndexedElement<>(index++, iter.next()));
                    return true;
                }
                return false;
            }
        }, stream.isParallel());
    }

    // ==================== FindLast 操作 | FindLast Operations ====================

    /**
     * Returns the last element of a stream, if present.
     * 返回流的最后一个元素（如果存在）。
     *
     * <p>This is a terminal operation that consumes the entire stream.</p>
     * <p>这是一个消耗整个流的终端操作。</p>
     *
     * @param <T>    element type | 元素类型
     * @param stream the stream | 流
     * @return the last element, or empty if stream is empty | 最后一个元素，如果流为空则返回空
     * @throws NullPointerException if stream is null | 如果流为 null
     */
    public static <T> Optional<T> findLast(Stream<T> stream) {
        Objects.requireNonNull(stream, "stream must not be null");

        // For sized streams, try to use spliterator directly
        Spliterator<T> spliterator = stream.spliterator();

        // If ordered and sized, we can potentially optimize
        if (spliterator.hasCharacteristics(Spliterator.SIZED)) {
            long size = spliterator.estimateSize();
            if (size == 0) {
                return Optional.empty();
            }
        }

        // Fall back to iteration
        T[] last = (T[]) new Object[1];
        spliterator.forEachRemaining(e -> last[0] = e);
        return Optional.ofNullable(last[0]);
    }

    /**
     * Returns the last element of an iterable, if present.
     * 返回可迭代对象的最后一个元素（如果存在）。
     *
     * @param <T>      element type | 元素类型
     * @param iterable the iterable | 可迭代对象
     * @return the last element, or empty if empty | 最后一个元素，如果为空则返回空
     */
    public static <T> Optional<T> findLast(Iterable<T> iterable) {
        Objects.requireNonNull(iterable, "iterable must not be null");

        if (iterable instanceof SequencedCollection<T> seq) {
            return seq.isEmpty() ? Optional.empty() : Optional.ofNullable(seq.getLast());
        }

        T last = null;
        for (T element : iterable) {
            last = element;
        }
        return Optional.ofNullable(last);
    }

    // ==================== MapWithIndex 操作 | MapWithIndex Operations ====================

    /**
     * Maps each element of a stream along with its index.
     * 将流的每个元素与其索引一起映射。
     *
     * @param <T>    source element type | 源元素类型
     * @param <R>    result element type | 结果元素类型
     * @param stream the stream | 流
     * @param mapper function that takes element and index | 接受元素和索引的函数
     * @return mapped stream | 映射后的流
     */
    public static <T, R> Stream<R> mapWithIndex(
            Stream<T> stream,
            BiFunction<? super T, Long, ? extends R> mapper) {
        Objects.requireNonNull(stream, "stream must not be null");
        Objects.requireNonNull(mapper, "mapper must not be null");

        Spliterator<T> split = stream.spliterator();
        Iterator<T> iter = Spliterators.iterator(split);

        return StreamSupport.stream(new Spliterators.AbstractSpliterator<R>(
                split.estimateSize(), split.characteristics() & ~Spliterator.SORTED) {
            private long index = 0;

            @Override
            public boolean tryAdvance(Consumer<? super R> action) {
                if (iter.hasNext()) {
                    action.accept(mapper.apply(iter.next(), index++));
                    return true;
                }
                return false;
            }
        }, stream.isParallel());
    }

    /**
     * Filters elements of a stream based on their index.
     * 根据索引过滤流的元素。
     *
     * @param <T>       element type | 元素类型
     * @param stream    the stream | 流
     * @param predicate predicate that takes element and index | 接受元素和索引的谓词
     * @return filtered stream | 过滤后的流
     */
    public static <T> Stream<T> filterWithIndex(
            Stream<T> stream,
            BiPredicate<? super T, Long> predicate) {
        Objects.requireNonNull(stream, "stream must not be null");
        Objects.requireNonNull(predicate, "predicate must not be null");

        Spliterator<T> split = stream.spliterator();
        Iterator<T> iter = Spliterators.iterator(split);

        return StreamSupport.stream(new Spliterators.AbstractSpliterator<T>(
                split.estimateSize(), split.characteristics() & ~(Spliterator.SORTED | Spliterator.SIZED)) {
            private long index = 0;

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                while (iter.hasNext()) {
                    T element = iter.next();
                    if (predicate.test(element, index++)) {
                        action.accept(element);
                        return true;
                    }
                }
                return false;
            }
        }, stream.isParallel());
    }

    // ==================== Pair 操作 | Pair Operations ====================

    /**
     * Processes consecutive pairs of elements in a stream.
     * 处理流中的连续元素对。
     *
     * <p>For a stream [a, b, c, d], this processes (a,b), (b,c), (c,d).</p>
     * <p>对于流 [a, b, c, d]，这会处理 (a,b), (b,c), (c,d)。</p>
     *
     * @param <T>      element type | 元素类型
     * @param stream   the stream | 流
     * @param consumer consumer for pairs | 对的消费者
     */
    public static <T> void forEachPair(Stream<T> stream, BiConsumer<? super T, ? super T> consumer) {
        Objects.requireNonNull(stream, "stream must not be null");
        Objects.requireNonNull(consumer, "consumer must not be null");

        Iterator<T> iter = stream.iterator();
        if (!iter.hasNext()) {
            return;
        }

        T previous = iter.next();
        while (iter.hasNext()) {
            T current = iter.next();
            consumer.accept(previous, current);
            previous = current;
        }
    }

    /**
     * Maps consecutive pairs of elements.
     * 映射连续元素对。
     *
     * @param <T>    source element type | 源元素类型
     * @param <R>    result element type | 结果元素类型
     * @param stream the stream | 流
     * @param mapper function to combine pairs | 组合对的函数
     * @return stream of mapped pairs | 映射后的对流
     */
    public static <T, R> Stream<R> mapPairs(
            Stream<T> stream,
            BiFunction<? super T, ? super T, ? extends R> mapper) {
        Objects.requireNonNull(stream, "stream must not be null");
        Objects.requireNonNull(mapper, "mapper must not be null");

        Spliterator<T> split = stream.spliterator();
        Iterator<T> iter = Spliterators.iterator(split);

        if (!iter.hasNext()) {
            return Stream.empty();
        }

        return StreamSupport.stream(new Spliterators.AbstractSpliterator<R>(
                Math.max(0, split.estimateSize() - 1), split.characteristics() & ~Spliterator.SIZED) {
            private T previous = iter.next();

            @Override
            public boolean tryAdvance(Consumer<? super R> action) {
                if (iter.hasNext()) {
                    T current = iter.next();
                    action.accept(mapper.apply(previous, current));
                    previous = current;
                    return true;
                }
                return false;
            }
        }, stream.isParallel());
    }

    // ==================== Concat 操作 | Concat Operations ====================

    /**
     * Concatenates multiple streams into one.
     * 将多个流连接成一个。
     *
     * @param <T>     element type | 元素类型
     * @param streams the streams to concatenate | 要连接的流
     * @return concatenated stream | 连接后的流
     */
    @SafeVarargs
    public static <T> Stream<T> concat(Stream<T>... streams) {
        if (streams == null || streams.length == 0) {
            return Stream.empty();
        }
        if (streams.length == 1) {
            return streams[0];
        }

        return Arrays.stream(streams).flatMap(Function.identity());
    }

    /**
     * Concatenates multiple iterables into one stream.
     * 将多个可迭代对象连接成一个流。
     *
     * @param <T>       element type | 元素类型
     * @param iterables the iterables to concatenate | 要连接的可迭代对象
     * @return concatenated stream | 连接后的流
     */
    @SafeVarargs
    public static <T> Stream<T> concat(Iterable<T>... iterables) {
        if (iterables == null || iterables.length == 0) {
            return Stream.empty();
        }

        return Arrays.stream(iterables)
                .flatMap(iterable -> StreamSupport.stream(iterable.spliterator(), false));
    }

    // ==================== Interleave 操作 | Interleave Operations ====================

    /**
     * Interleaves two streams, alternating between elements from each.
     * 交错两个流，交替取每个流的元素。
     *
     * <p>For streams [1, 2, 3] and [a, b, c], produces [1, a, 2, b, 3, c].</p>
     * <p>对于流 [1, 2, 3] 和 [a, b, c]，产生 [1, a, 2, b, 3, c]。</p>
     *
     * @param <T>     element type | 元素类型
     * @param streamA first stream | 第一个流
     * @param streamB second stream | 第二个流
     * @return interleaved stream | 交错后的流
     */
    public static <T> Stream<T> interleave(Stream<T> streamA, Stream<T> streamB) {
        Objects.requireNonNull(streamA, "streamA must not be null");
        Objects.requireNonNull(streamB, "streamB must not be null");

        Iterator<T> iterA = streamA.iterator();
        Iterator<T> iterB = streamB.iterator();

        return StreamSupport.stream(new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, 0) {
            private boolean useA = true;

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                if (useA) {
                    if (iterA.hasNext()) {
                        action.accept(iterA.next());
                        useA = false;
                        return true;
                    } else if (iterB.hasNext()) {
                        action.accept(iterB.next());
                        return true;
                    }
                } else {
                    if (iterB.hasNext()) {
                        action.accept(iterB.next());
                        useA = true;
                        return true;
                    } else if (iterA.hasNext()) {
                        action.accept(iterA.next());
                        return true;
                    }
                }
                return false;
            }
        }, false);
    }

    // ==================== Stream Creation | 流创建 ====================

    /**
     * Creates a stream from an optional value.
     * 从可选值创建流。
     *
     * @param <T>      element type | 元素类型
     * @param optional the optional | 可选值
     * @return stream with 0 or 1 element | 包含 0 或 1 个元素的流
     */
    public static <T> Stream<T> stream(Optional<T> optional) {
        return optional.stream();
    }

    /**
     * Creates a stream from an iterator.
     * 从迭代器创建流。
     *
     * @param <T>      element type | 元素类型
     * @param iterator the iterator | 迭代器
     * @return stream of iterator elements | 迭代器元素的流
     */
    public static <T> Stream<T> stream(Iterator<T> iterator) {
        Objects.requireNonNull(iterator, "iterator must not be null");
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, 0), false);
    }

    /**
     * Creates a stream from an iterable.
     * 从可迭代对象创建流。
     *
     * @param <T>      element type | 元素类型
     * @param iterable the iterable | 可迭代对象
     * @return stream of iterable elements | 可迭代对象元素的流
     */
    public static <T> Stream<T> stream(Iterable<T> iterable) {
        Objects.requireNonNull(iterable, "iterable must not be null");
        if (iterable instanceof Collection<T> collection) {
            return collection.stream();
        }
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    /**
     * Creates a stream from an enumeration.
     * 从枚举创建流。
     *
     * @param <T>         element type | 元素类型
     * @param enumeration the enumeration | 枚举
     * @return stream of enumeration elements | 枚举元素的流
     */
    public static <T> Stream<T> stream(Enumeration<T> enumeration) {
        Objects.requireNonNull(enumeration, "enumeration must not be null");
        return stream(enumeration.asIterator());
    }

    // ==================== 辅助类 | Helper Classes ====================

    /**
     * An element paired with its index.
     * 与其索引配对的元素。
     *
     * @param <T>   element type | 元素类型
     * @param index the index (0-based) | 索引（从 0 开始）
     * @param value the element value | 元素值
     */
    public record IndexedElement<T>(long index, T value) {

        /**
         * Returns the index as an int. May throw if index exceeds Integer.MAX_VALUE.
         * 将索引作为 int 返回。如果索引超过 Integer.MAX_VALUE 可能会抛出异常。
         *
         * @return index as int | int 形式的索引
         */
        public int indexAsInt() {
            return Math.toIntExact(index);
        }
    }
}
