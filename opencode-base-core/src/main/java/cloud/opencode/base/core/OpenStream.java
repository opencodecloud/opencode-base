package cloud.opencode.base.core;

import cloud.opencode.base.core.exception.OpenException;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Stream Utility Class - Enhanced Stream operations with JDK 25 Gatherers support
 * Stream工具类 - 增强的Stream操作，支持JDK 25 Gatherers
 *
 * <p>Provides utility methods for working with Java Streams, including
 * creation, transformation, collection, and parallel processing.</p>
 * <p>提供Java Stream的实用方法，包括创建、转换、收集和并行处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Stream creation (of, from, range, generate) - Stream创建</li>
 *   <li>Stream transformation (batch, window, sliding) - Stream转换</li>
 *   <li>Parallel processing utilities - 并行处理工具</li>
 *   <li>Collectors extensions - Collectors扩展</li>
 *   <li>Stream combination (zip, merge, concat) - Stream组合</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Batch processing - 批量处理
 * List<List<User>> batches = OpenStream.batch(users.stream(), 100);
 *
 * // Sliding window - 滑动窗口
 * Stream<List<Integer>> windows = OpenStream.slidingWindow(numbers, 3);
 *
 * // Zip streams - 合并流
 * Stream<Pair<A, B>> zipped = OpenStream.zip(streamA, streamB, Pair::of);
 *
 * // Parallel with concurrency limit - 限制并发的并行处理
 * results = OpenStream.parallelWithLimit(items, 10, item -> process(item));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenStream {

    private OpenStream() {
        // Utility class
    }

    // ==================== Stream Creation | Stream创建 ====================

    /**
     * Creates a stream from varargs
     * 从可变参数创建Stream
     *
     * @param elements elements | 元素
     * @param <T> element type | 元素类型
     * @return stream | 流
     */
    @SafeVarargs
    public static <T> Stream<T> of(T... elements) {
        if (elements == null || elements.length == 0) {
            return Stream.empty();
        }
        return Arrays.stream(elements);
    }

    /**
     * Creates a stream from an iterable
     * 从Iterable创建Stream
     *
     * @param iterable the iterable | 可迭代对象
     * @param <T> element type | 元素类型
     * @return stream | 流
     */
    public static <T> Stream<T> from(Iterable<T> iterable) {
        if (iterable == null) {
            return Stream.empty();
        }
        if (iterable instanceof Collection<T> c) {
            return c.stream();
        }
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    /**
     * Creates a stream from an iterator
     * 从Iterator创建Stream
     *
     * @param iterator the iterator | 迭代器
     * @param <T> element type | 元素类型
     * @return stream | 流
     */
    public static <T> Stream<T> from(Iterator<T> iterator) {
        if (iterator == null) {
            return Stream.empty();
        }
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(
            iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * Creates a stream from an optional
     * 从Optional创建Stream
     *
     * @param optional the optional | 可选值
     * @param <T> element type | 元素类型
     * @return stream with 0 or 1 element | 包含0或1个元素的流
     */
    public static <T> Stream<T> from(Optional<T> optional) {
        return optional == null ? Stream.empty() : optional.stream();
    }

    /**
     * Creates an int range stream
     * 创建整数范围Stream
     *
     * @param startInclusive start (inclusive) | 起始值(包含)
     * @param endExclusive end (exclusive) | 结束值(不包含)
     * @return int stream | 整数流
     */
    public static IntStream range(int startInclusive, int endExclusive) {
        return IntStream.range(startInclusive, endExclusive);
    }

    /**
     * Creates an int range stream (closed)
     * 创建闭区间整数范围Stream
     *
     * @param startInclusive start (inclusive) | 起始值(包含)
     * @param endInclusive end (inclusive) | 结束值(包含)
     * @return int stream | 整数流
     */
    public static IntStream rangeClosed(int startInclusive, int endInclusive) {
        return IntStream.rangeClosed(startInclusive, endInclusive);
    }

    /**
     * Creates a long range stream
     * 创建长整数范围Stream
     *
     * @param startInclusive start (inclusive) | 起始值(包含)
     * @param endExclusive end (exclusive) | 结束值(不包含)
     * @return long stream | 长整数流
     */
    public static LongStream range(long startInclusive, long endExclusive) {
        return LongStream.range(startInclusive, endExclusive);
    }

    /**
     * Generates an infinite stream
     * 生成无限Stream
     *
     * @param supplier element supplier | 元素提供者
     * @param <T> element type | 元素类型
     * @return infinite stream | 无限流
     */
    public static <T> Stream<T> generate(Supplier<T> supplier) {
        return Stream.generate(supplier);
    }

    /**
     * Generates an infinite stream with seed
     * 使用种子生成无限Stream
     *
     * @param seed initial value | 初始值
     * @param next next value function | 下一个值函数
     * @param <T> element type | 元素类型
     * @return infinite stream | 无限流
     */
    public static <T> Stream<T> iterate(T seed, UnaryOperator<T> next) {
        return Stream.iterate(seed, next);
    }

    /**
     * Generates a finite stream with seed and predicate
     * 使用种子和谓词生成有限Stream
     *
     * @param seed initial value | 初始值
     * @param hasNext continue predicate | 继续条件
     * @param next next value function | 下一个值函数
     * @param <T> element type | 元素类型
     * @return finite stream | 有限流
     */
    public static <T> Stream<T> iterate(T seed, Predicate<T> hasNext, UnaryOperator<T> next) {
        return Stream.iterate(seed, hasNext, next);
    }

    // ==================== Batch Processing | 批量处理 ====================

    /**
     * Splits stream into batches of fixed size
     * 将Stream分割成固定大小的批次
     *
     * @param stream source stream | 源流
     * @param batchSize batch size | 批次大小
     * @param <T> element type | 元素类型
     * @return list of batches | 批次列表
     */
    public static <T> List<List<T>> batch(Stream<T> stream, int batchSize) {
        if (stream == null || batchSize <= 0) {
            return Collections.emptyList();
        }

        List<List<T>> batches = new ArrayList<>();
        List<T> currentBatch = new ArrayList<>(batchSize);

        stream.forEach(item -> {
            currentBatch.add(item);
            if (currentBatch.size() >= batchSize) {
                batches.add(new ArrayList<>(currentBatch));
                currentBatch.clear();
            }
        });

        if (!currentBatch.isEmpty()) {
            batches.add(currentBatch);
        }

        return batches;
    }

    /**
     * Creates a stream of batches
     * 创建批次流
     *
     * @param source source collection | 源集合
     * @param batchSize batch size | 批次大小
     * @param <T> element type | 元素类型
     * @return stream of batches | 批次流
     */
    public static <T> Stream<List<T>> batchStream(Collection<T> source, int batchSize) {
        if (source == null || source.isEmpty() || batchSize <= 0) {
            return Stream.empty();
        }

        List<T> list = source instanceof List<T> l ? l : new ArrayList<>(source);
        int size = list.size();
        int numBatches = (size + batchSize - 1) / batchSize;

        return IntStream.range(0, numBatches)
            .mapToObj(i -> list.subList(
                i * batchSize,
                Math.min((i + 1) * batchSize, size)));
    }

    // ==================== Windowing | 窗口操作 ====================

    /**
     * Creates a sliding window stream
     * 创建滑动窗口流
     *
     * @param source source collection | 源集合
     * @param windowSize window size | 窗口大小
     * @param <T> element type | 元素类型
     * @return stream of windows | 窗口流
     */
    public static <T> Stream<List<T>> slidingWindow(Collection<T> source, int windowSize) {
        return slidingWindow(source, windowSize, 1);
    }

    /**
     * Creates a sliding window stream with custom step
     * 创建自定义步长的滑动窗口流
     *
     * @param source source collection | 源集合
     * @param windowSize window size | 窗口大小
     * @param step step size | 步长
     * @param <T> element type | 元素类型
     * @return stream of windows | 窗口流
     */
    public static <T> Stream<List<T>> slidingWindow(Collection<T> source, int windowSize, int step) {
        if (source == null || source.isEmpty() || windowSize <= 0 || step <= 0) {
            return Stream.empty();
        }

        List<T> list = source instanceof List<T> l ? l : new ArrayList<>(source);
        int size = list.size();

        if (size < windowSize) {
            return Stream.of(new ArrayList<>(list));
        }

        int numWindows = (size - windowSize) / step + 1;
        return IntStream.range(0, numWindows)
            .mapToObj(i -> new ArrayList<>(list.subList(i * step, i * step + windowSize)));
    }

    /**
     * Creates a tumbling window stream (non-overlapping)
     * 创建翻滚窗口流(不重叠)
     *
     * @param source source collection | 源集合
     * @param windowSize window size | 窗口大小
     * @param <T> element type | 元素类型
     * @return stream of windows | 窗口流
     */
    public static <T> Stream<List<T>> tumblingWindow(Collection<T> source, int windowSize) {
        return batchStream(source, windowSize);
    }

    // ==================== Stream Combination | Stream组合 ====================

    /**
     * Zips two streams into pairs
     * 将两个流合并成对
     *
     * @param streamA first stream | 第一个流
     * @param streamB second stream | 第二个流
     * @param zipper zip function | 合并函数
     * @param <A> first element type | 第一个元素类型
     * @param <B> second element type | 第二个元素类型
     * @param <R> result type | 结果类型
     * @return zipped stream | 合并后的流
     */
    public static <A, B, R> Stream<R> zip(
            Stream<A> streamA,
            Stream<B> streamB,
            BiFunction<A, B, R> zipper) {
        if (streamA == null || streamB == null || zipper == null) {
            return Stream.empty();
        }

        Iterator<A> iterA = streamA.iterator();
        Iterator<B> iterB = streamB.iterator();

        return from(new Iterator<R>() {
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
     * Zips stream with indices
     * 将流与索引合并
     *
     * @param stream source stream | 源流
     * @param <T> element type | 元素类型
     * @return stream of indexed elements | 带索引的元素流
     */
    public static <T> Stream<IndexedValue<T>> zipWithIndex(Stream<T> stream) {
        if (stream == null) {
            return Stream.empty();
        }

        long[] index = {0};
        return stream.map(value -> new IndexedValue<>(index[0]++, value));
    }

    /**
     * Indexed value record
     * 带索引的值记录
     *
     * @param index the index | 索引
     * @param value the value | 值
     * @param <T> value type | 值类型
     */
    public record IndexedValue<T>(long index, T value) {}

    /**
     * Merges multiple streams
     * 合并多个流
     *
     * @param streams streams to merge | 要合并的流
     * @param <T> element type | 元素类型
     * @return merged stream | 合并后的流
     */
    @SafeVarargs
    public static <T> Stream<T> merge(Stream<T>... streams) {
        if (streams == null || streams.length == 0) {
            return Stream.empty();
        }
        return Arrays.stream(streams)
            .filter(Objects::nonNull)
            .flatMap(Function.identity());
    }

    /**
     * Interleaves two streams
     * 交错合并两个流
     *
     * @param streamA first stream | 第一个流
     * @param streamB second stream | 第二个流
     * @param <T> element type | 元素类型
     * @return interleaved stream | 交错后的流
     */
    public static <T> Stream<T> interleave(Stream<T> streamA, Stream<T> streamB) {
        if (streamA == null && streamB == null) {
            return Stream.empty();
        }
        if (streamA == null) return streamB;
        if (streamB == null) return streamA;

        Iterator<T> iterA = streamA.iterator();
        Iterator<T> iterB = streamB.iterator();

        return from(new Iterator<T>() {
            private boolean takeFromA = true;

            @Override
            public boolean hasNext() {
                return iterA.hasNext() || iterB.hasNext();
            }

            @Override
            public T next() {
                if (takeFromA && iterA.hasNext()) {
                    takeFromA = false;
                    return iterA.next();
                } else if (iterB.hasNext()) {
                    takeFromA = true;
                    return iterB.next();
                } else {
                    takeFromA = false;
                    return iterA.next();
                }
            }
        });
    }

    // ==================== Filtering | 过滤操作 ====================

    /**
     * Filters nulls from stream
     * 从流中过滤空值
     *
     * @param stream source stream | 源流
     * @param <T> element type | 元素类型
     * @return stream without nulls | 不含空值的流
     */
    public static <T> Stream<T> filterNulls(Stream<T> stream) {
        return stream == null ? Stream.empty() : stream.filter(Objects::nonNull);
    }

    /**
     * Distinct by key
     * 按键去重
     *
     * @param stream source stream | 源流
     * @param keyExtractor key extractor | 键提取器
     * @param <T> element type | 元素类型
     * @param <K> key type | 键类型
     * @return distinct stream | 去重后的流
     */
    public static <T, K> Stream<T> distinctBy(Stream<T> stream, Function<T, K> keyExtractor) {
        if (stream == null || keyExtractor == null) {
            return Stream.empty();
        }

        Set<K> seen = Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());
        return stream.filter(item -> seen.add(keyExtractor.apply(item)));
    }

    /**
     * Takes elements while predicate is true
     * 在谓词为真时获取元素
     *
     * @param stream source stream | 源流
     * @param predicate condition | 条件
     * @param <T> element type | 元素类型
     * @return filtered stream | 过滤后的流
     */
    public static <T> Stream<T> takeWhile(Stream<T> stream, Predicate<T> predicate) {
        return stream == null ? Stream.empty() : stream.takeWhile(predicate);
    }

    /**
     * Drops elements while predicate is true
     * 在谓词为真时跳过元素
     *
     * @param stream source stream | 源流
     * @param predicate condition | 条件
     * @param <T> element type | 元素类型
     * @return filtered stream | 过滤后的流
     */
    public static <T> Stream<T> dropWhile(Stream<T> stream, Predicate<T> predicate) {
        return stream == null ? Stream.empty() : stream.dropWhile(predicate);
    }

    // ==================== Reduction | 归约操作 ====================

    /**
     * Finds first matching element
     * 查找第一个匹配元素
     *
     * @param stream source stream | 源流
     * @param predicate condition | 条件
     * @param <T> element type | 元素类型
     * @return optional result | 可选结果
     */
    public static <T> Optional<T> findFirst(Stream<T> stream, Predicate<T> predicate) {
        if (stream == null || predicate == null) {
            return Optional.empty();
        }
        return stream.filter(predicate).findFirst();
    }

    /**
     * Checks if any element matches
     * 检查是否有元素匹配
     *
     * @param stream source stream | 源流
     * @param predicate condition | 条件
     * @param <T> element type | 元素类型
     * @return true if any matches | 如果有匹配则为true
     */
    public static <T> boolean anyMatch(Stream<T> stream, Predicate<T> predicate) {
        return stream != null && predicate != null && stream.anyMatch(predicate);
    }

    /**
     * Checks if all elements match
     * 检查是否所有元素都匹配
     *
     * @param stream source stream | 源流
     * @param predicate condition | 条件
     * @param <T> element type | 元素类型
     * @return true if all match | 如果全部匹配则为true
     */
    public static <T> boolean allMatch(Stream<T> stream, Predicate<T> predicate) {
        return stream == null || predicate == null || stream.allMatch(predicate);
    }

    /**
     * Checks if no elements match
     * 检查是否没有元素匹配
     *
     * @param stream source stream | 源流
     * @param predicate condition | 条件
     * @param <T> element type | 元素类型
     * @return true if none match | 如果没有匹配则为true
     */
    public static <T> boolean noneMatch(Stream<T> stream, Predicate<T> predicate) {
        return stream == null || predicate == null || stream.noneMatch(predicate);
    }

    // ==================== Collectors | 收集器 ====================

    /**
     * Collects to unmodifiable list
     * 收集为不可变列表
     *
     * @param stream source stream | 源流
     * @param <T> element type | 元素类型
     * @return unmodifiable list | 不可变列表
     */
    public static <T> List<T> toUnmodifiableList(Stream<T> stream) {
        if (stream == null) {
            return List.of();
        }
        return stream.toList();
    }

    /**
     * Collects to unmodifiable set
     * 收集为不可变集合
     *
     * @param stream source stream | 源流
     * @param <T> element type | 元素类型
     * @return unmodifiable set | 不可变集合
     */
    public static <T> Set<T> toUnmodifiableSet(Stream<T> stream) {
        if (stream == null) {
            return Set.of();
        }
        return stream.collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Collects to map with key extractor
     * 使用键提取器收集为Map
     *
     * @param stream source stream | 源流
     * @param keyMapper key extractor | 键提取器
     * @param <T> element type | 元素类型
     * @param <K> key type | 键类型
     * @return map | Map
     */
    public static <T, K> Map<K, T> toMap(Stream<T> stream, Function<T, K> keyMapper) {
        if (stream == null || keyMapper == null) {
            return Map.of();
        }
        return stream.collect(Collectors.toMap(keyMapper, Function.identity()));
    }

    /**
     * Collects to map with key and value extractors
     * 使用键和值提取器收集为Map
     *
     * @param stream source stream | 源流
     * @param keyMapper key extractor | 键提取器
     * @param valueMapper value extractor | 值提取器
     * @param <T> element type | 元素类型
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return map | Map
     */
    public static <T, K, V> Map<K, V> toMap(
            Stream<T> stream,
            Function<T, K> keyMapper,
            Function<T, V> valueMapper) {
        if (stream == null || keyMapper == null || valueMapper == null) {
            return Map.of();
        }
        return stream.collect(Collectors.toMap(keyMapper, valueMapper));
    }

    /**
     * Groups by key
     * 按键分组
     *
     * @param stream source stream | 源流
     * @param keyMapper key extractor | 键提取器
     * @param <T> element type | 元素类型
     * @param <K> key type | 键类型
     * @return grouped map | 分组Map
     */
    public static <T, K> Map<K, List<T>> groupBy(Stream<T> stream, Function<T, K> keyMapper) {
        if (stream == null || keyMapper == null) {
            return Map.of();
        }
        return stream.collect(Collectors.groupingBy(keyMapper));
    }

    /**
     * Partitions by predicate
     * 按谓词分区
     *
     * @param stream source stream | 源流
     * @param predicate partition condition | 分区条件
     * @param <T> element type | 元素类型
     * @return partitioned map | 分区Map
     */
    public static <T> Map<Boolean, List<T>> partitionBy(Stream<T> stream, Predicate<T> predicate) {
        if (stream == null || predicate == null) {
            return Map.of(true, List.of(), false, List.of());
        }
        return stream.collect(Collectors.partitioningBy(predicate));
    }

    /**
     * Joins to string
     * 连接为字符串
     *
     * @param stream source stream | 源流
     * @param delimiter delimiter | 分隔符
     * @param <T> element type | 元素类型
     * @return joined string | 连接后的字符串
     */
    public static <T> String joining(Stream<T> stream, CharSequence delimiter) {
        if (stream == null) {
            return "";
        }
        return stream.map(Object::toString).collect(Collectors.joining(delimiter));
    }

    /**
     * Joins to string with prefix and suffix
     * 使用前缀和后缀连接为字符串
     *
     * @param stream source stream | 源流
     * @param delimiter delimiter | 分隔符
     * @param prefix prefix | 前缀
     * @param suffix suffix | 后缀
     * @param <T> element type | 元素类型
     * @return joined string | 连接后的字符串
     */
    public static <T> String joining(
            Stream<T> stream,
            CharSequence delimiter,
            CharSequence prefix,
            CharSequence suffix) {
        if (stream == null) {
            return prefix.toString() + suffix.toString();
        }
        return stream.map(Object::toString)
            .collect(Collectors.joining(delimiter, prefix, suffix));
    }

    // ==================== Parallel Processing | 并行处理 ====================

    /**
     * Processes in parallel with limited concurrency
     * 使用有限并发进行并行处理
     *
     * @param source source collection | 源集合
     * @param parallelism parallelism level | 并行度
     * @param mapper transformation function | 转换函数
     * @param <T> input type | 输入类型
     * @param <R> result type | 结果类型
     * @return result list | 结果列表
     */
    public static <T, R> List<R> parallelMap(
            Collection<T> source,
            int parallelism,
            Function<T, R> mapper) {
        if (source == null || source.isEmpty() || mapper == null) {
            return List.of();
        }

        if (parallelism <= 1 || source.size() <= parallelism) {
            return source.stream().map(mapper).toList();
        }

        // Use ForkJoinPool with custom parallelism
        java.util.concurrent.ForkJoinPool pool = new java.util.concurrent.ForkJoinPool(parallelism);
        try {
            return pool.submit(() ->
                source.parallelStream().map(mapper).toList()
            ).get();
        } catch (Exception e) {
            throw new OpenException("Parallel processing failed", e);
        } finally {
            pool.shutdown();
        }
    }

    /**
     * Processes in parallel and collects results
     * 并行处理并收集结果
     *
     * @param source source collection | 源集合
     * @param mapper transformation function | 转换函数
     * @param <T> input type | 输入类型
     * @param <R> result type | 结果类型
     * @return result list | 结果列表
     */
    public static <T, R> List<R> parallelMap(Collection<T> source, Function<T, R> mapper) {
        if (source == null || source.isEmpty() || mapper == null) {
            return List.of();
        }
        return source.parallelStream().map(mapper).toList();
    }

    /**
     * Filters in parallel
     * 并行过滤
     *
     * @param source source collection | 源集合
     * @param predicate filter condition | 过滤条件
     * @param <T> element type | 元素类型
     * @return filtered list | 过滤后的列表
     */
    public static <T> List<T> parallelFilter(Collection<T> source, Predicate<T> predicate) {
        if (source == null || source.isEmpty() || predicate == null) {
            return List.of();
        }
        return source.parallelStream().filter(predicate).toList();
    }

    // ==================== Statistics | 统计操作 ====================

    /**
     * Counts elements
     * 统计元素数量
     *
     * @param stream source stream | 源流
     * @param <T> element type | 元素类型
     * @return count | 数量
     */
    public static <T> long count(Stream<T> stream) {
        return stream == null ? 0 : stream.count();
    }

    /**
     * Gets sum of integers
     * 获取整数和
     *
     * @param stream source stream | 源流
     * @return sum | 和
     */
    public static int sumInt(IntStream stream) {
        return stream == null ? 0 : stream.sum();
    }

    /**
     * Gets sum of longs
     * 获取长整数和
     *
     * @param stream source stream | 源流
     * @return sum | 和
     */
    public static long sumLong(LongStream stream) {
        return stream == null ? 0L : stream.sum();
    }

    /**
     * Gets sum of doubles
     * 获取双精度数和
     *
     * @param stream source stream | 源流
     * @return sum | 和
     */
    public static double sumDouble(DoubleStream stream) {
        return stream == null ? 0.0 : stream.sum();
    }

    /**
     * Gets average of integers
     * 获取整数平均值
     *
     * @param stream source stream | 源流
     * @return optional average | 可选平均值
     */
    public static OptionalDouble averageInt(IntStream stream) {
        return stream == null ? OptionalDouble.empty() : stream.average();
    }

    /**
     * Gets max element
     * 获取最大元素
     *
     * @param stream source stream | 源流
     * @param comparator comparator | 比较器
     * @param <T> element type | 元素类型
     * @return optional max | 可选最大值
     */
    public static <T> Optional<T> max(Stream<T> stream, Comparator<T> comparator) {
        if (stream == null || comparator == null) {
            return Optional.empty();
        }
        return stream.max(comparator);
    }

    /**
     * Gets min element
     * 获取最小元素
     *
     * @param stream source stream | 源流
     * @param comparator comparator | 比较器
     * @param <T> element type | 元素类型
     * @return optional min | 可选最小值
     */
    public static <T> Optional<T> min(Stream<T> stream, Comparator<T> comparator) {
        if (stream == null || comparator == null) {
            return Optional.empty();
        }
        return stream.min(comparator);
    }

    // ==================== Utility | 工具方法 ====================

    /**
     * Peeks at each element for debugging
     * 查看每个元素用于调试
     *
     * @param stream source stream | 源流
     * @param action action to perform | 要执行的操作
     * @param <T> element type | 元素类型
     * @return same stream with peek | 带peek的同一个流
     */
    public static <T> Stream<T> peek(Stream<T> stream, Consumer<T> action) {
        if (stream == null || action == null) {
            return stream == null ? Stream.empty() : stream;
        }
        return stream.peek(action);
    }

    /**
     * Limits stream size
     * 限制流大小
     *
     * @param stream source stream | 源流
     * @param maxSize max size | 最大大小
     * @param <T> element type | 元素类型
     * @return limited stream | 限制后的流
     */
    public static <T> Stream<T> limit(Stream<T> stream, long maxSize) {
        return stream == null ? Stream.empty() : stream.limit(maxSize);
    }

    /**
     * Skips first n elements
     * 跳过前n个元素
     *
     * @param stream source stream | 源流
     * @param n number to skip | 跳过数量
     * @param <T> element type | 元素类型
     * @return stream after skip | 跳过后的流
     */
    public static <T> Stream<T> skip(Stream<T> stream, long n) {
        return stream == null ? Stream.empty() : stream.skip(n);
    }

    /**
     * Sorts stream
     * 排序流
     *
     * @param stream source stream | 源流
     * @param comparator comparator | 比较器
     * @param <T> element type | 元素类型
     * @return sorted stream | 排序后的流
     */
    public static <T> Stream<T> sorted(Stream<T> stream, Comparator<T> comparator) {
        if (stream == null) {
            return Stream.empty();
        }
        return comparator == null ? stream.sorted() : stream.sorted(comparator);
    }

    /**
     * Flattens nested streams
     * 展平嵌套流
     *
     * @param stream source stream of streams | 流的源流
     * @param <T> element type | 元素类型
     * @return flattened stream | 展平后的流
     */
    public static <T> Stream<T> flatten(Stream<Stream<T>> stream) {
        return stream == null ? Stream.empty() : stream.flatMap(Function.identity());
    }

    /**
     * Flattens nested collections
     * 展平嵌套集合
     *
     * @param stream source stream of collections | 集合的源流
     * @param <T> element type | 元素类型
     * @return flattened stream | 展平后的流
     */
    public static <T> Stream<T> flattenCollections(Stream<? extends Collection<T>> stream) {
        return stream == null ? Stream.empty() : stream.flatMap(Collection::stream);
    }
}
