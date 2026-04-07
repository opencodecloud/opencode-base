package cloud.opencode.base.collections;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;
import java.util.stream.Gatherer;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

/**
 * OpenGatherers - JDK 25 Stream Gatherers Utilities
 * OpenGatherers - JDK 25 流收集器工具
 *
 * <p>Provides custom Gatherers for advanced stream processing operations
 * using JDK 25's Gatherer API (JEP 485).</p>
 * <p>使用 JDK 25 的 Gatherer API (JEP 485) 提供自定义收集器用于高级流处理操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Windowing operations (fixed, sliding, session) - 窗口操作</li>
 *   <li>Batching and chunking - 批处理和分块</li>
 *   <li>Distinct by key - 按键去重</li>
 *   <li>Scan and fold operations - 扫描和折叠操作</li>
 *   <li>Filtering with state - 带状态的过滤</li>
 *   <li>Map with previous element - 带前一个元素的映射</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Fixed window - 固定窗口
 * Stream<List<Integer>> windows = stream.gather(OpenGatherers.windowFixed(3));
 *
 * // Sliding window - 滑动窗口
 * Stream<List<Integer>> sliding = stream.gather(OpenGatherers.windowSliding(3));
 *
 * // Distinct by key - 按键去重
 * Stream<User> unique = users.gather(OpenGatherers.distinctBy(User::getEmail));
 *
 * // Take while with index - 带索引的takeWhile
 * Stream<T> limited = stream.gather(OpenGatherers.takeWhileIndexed((i, e) -> i < 10));
 *
 * // Scan (running accumulation) - 扫描（运行累积）
 * Stream<Integer> sums = numbers.gather(OpenGatherers.scan(0, Integer::sum));
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Uses JDK 25 Gatherer API - 使用 JDK 25 Gatherer API</li>
 *   <li>Supports parallel streams where applicable - 在适用时支持并行流</li>
 *   <li>Lazy evaluation - 延迟求值</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on Gatherer - 线程安全: 取决于收集器</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see java.util.stream.Gatherer
 * @see java.util.stream.Gatherers
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class OpenGatherers {

    private OpenGatherers() {
    }

    // ==================== Windowing Operations | 窗口操作 ====================

    /**
     * Creates a fixed window gatherer.
     * 创建固定窗口收集器。
     *
     * <p>Groups elements into fixed-size lists (tumbling window).</p>
     * <p>将元素分组为固定大小的列表（翻滚窗口）。</p>
     *
     * @param <T>  element type | 元素类型
     * @param size window size | 窗口大小
     * @return gatherer | 收集器
     */
    public static <T> Gatherer<T, ?, List<T>> windowFixed(int size) {
        return Gatherers.windowFixed(size);
    }

    /**
     * Creates a sliding window gatherer.
     * 创建滑动窗口收集器。
     *
     * <p>Creates overlapping windows of the specified size.</p>
     * <p>创建指定大小的重叠窗口。</p>
     *
     * @param <T>  element type | 元素类型
     * @param size window size | 窗口大小
     * @return gatherer | 收集器
     */
    public static <T> Gatherer<T, ?, List<T>> windowSliding(int size) {
        return Gatherers.windowSliding(size);
    }

    /**
     * Creates a sliding window gatherer with custom step.
     * 创建自定义步长的滑动窗口收集器。
     *
     * @param <T>  element type | 元素类型
     * @param size window size | 窗口大小
     * @param step step size | 步长
     * @return gatherer | 收集器
     */
    public static <T> Gatherer<T, ?, List<T>> windowSliding(int size, int step) {
        if (size <= 0) {
            throw new IllegalArgumentException("Window size must be positive");
        }
        if (step <= 0) {
            throw new IllegalArgumentException("Step must be positive");
        }

        return Gatherer.ofSequential(
            () -> new WindowSlidingState<T>(size, step),
            (state, element, downstream) -> {
                state.add(element);
                if (state.isReady()) {
                    List<T> window = state.getWindow();
                    if (window != null) {
                        downstream.push(window);
                    }
                }
                return true;
            },
            (state, downstream) -> {
                List<T> remaining = state.getRemaining();
                if (remaining != null && !remaining.isEmpty()) {
                    downstream.push(remaining);
                }
            }
        );
    }

    private static class WindowSlidingState<T> {
        private final int size;
        private final int step;
        private final List<T> buffer;
        private int count;

        WindowSlidingState(int size, int step) {
            this.size = size;
            this.step = step;
            this.buffer = new ArrayList<>(size);
            this.count = 0;
        }

        void add(T element) {
            buffer.add(element);
            count++;
        }

        boolean isReady() {
            return buffer.size() >= size && (count - size) % step == 0;
        }

        List<T> getWindow() {
            if (buffer.size() >= size) {
                List<T> window = new ArrayList<>(buffer.subList(buffer.size() - size, buffer.size()));
                if (buffer.size() > size) {
                    buffer.subList(0, buffer.size() - size).clear();
                }
                return window;
            }
            return null;
        }

        List<T> getRemaining() {
            return buffer.isEmpty() ? null : new ArrayList<>(buffer);
        }
    }

    // ==================== Batching | 批处理 ====================

    /**
     * Creates a batch gatherer.
     * 创建批处理收集器。
     *
     * <p>Alias for windowFixed for semantic clarity.</p>
     * <p>windowFixed 的别名，用于语义清晰。</p>
     *
     * @param <T>       element type | 元素类型
     * @param batchSize batch size | 批次大小
     * @return gatherer | 收集器
     */
    public static <T> Gatherer<T, ?, List<T>> batch(int batchSize) {
        return windowFixed(batchSize);
    }

    /**
     * Creates a batch gatherer with processing function.
     * 创建带处理函数的批处理收集器。
     *
     * @param <T>       element type | 元素类型
     * @param <R>       result type | 结果类型
     * @param batchSize batch size | 批次大小
     * @param processor batch processor | 批次处理器
     * @return gatherer | 收集器
     */
    public static <T, R> Gatherer<T, ?, R> batchProcess(int batchSize, Function<List<T>, R> processor) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be positive");
        }
        Objects.requireNonNull(processor, "Processor must not be null");

        return Gatherer.ofSequential(
            () -> new ArrayList<T>(batchSize),
            (batch, element, downstream) -> {
                batch.add(element);
                if (batch.size() >= batchSize) {
                    downstream.push(processor.apply(new ArrayList<>(batch)));
                    batch.clear();
                }
                return true;
            },
            (batch, downstream) -> {
                if (!batch.isEmpty()) {
                    downstream.push(processor.apply(batch));
                }
            }
        );
    }

    // ==================== Distinct Operations | 去重操作 ====================

    /**
     * Creates a distinct-by-key gatherer.
     * 创建按键去重收集器。
     *
     * @param <T>          element type | 元素类型
     * @param <K>          key type | 键类型
     * @param keyExtractor key extractor | 键提取器
     * @return gatherer | 收集器
     */
    public static <T, K> Gatherer<T, ?, T> distinctBy(Function<? super T, ? extends K> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "Key extractor must not be null");

        return Gatherer.ofSequential(
            HashSet<K>::new,
            (seen, element, downstream) -> {
                K key = keyExtractor.apply(element);
                if (seen.add(key)) {
                    downstream.push(element);
                }
                return true;
            }
        );
    }

    /**
     * Creates a distinct-by gatherer with custom equality.
     * 创建自定义相等性的去重收集器。
     *
     * @param <T>       element type | 元素类型
     * @param <K>       key type | 键类型
     * @param keyMapper key mapper | 键映射器
     * @param equals    equality function | 相等函数
     * @param hash      hash function | 哈希函数
     * @return gatherer | 收集器
     */
    public static <T, K> Gatherer<T, ?, T> distinctBy(
            Function<? super T, ? extends K> keyMapper,
            BiPredicate<K, K> equals,
            ToIntFunction<K> hash) {
        Objects.requireNonNull(keyMapper);
        Objects.requireNonNull(equals);
        Objects.requireNonNull(hash);

        return Gatherer.ofSequential(
            ArrayList<K>::new,
            (seen, element, downstream) -> {
                K key = keyMapper.apply(element);
                boolean exists = false;
                for (K existing : seen) {
                    if (equals.test(key, existing)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    seen.add(key);
                    downstream.push(element);
                }
                return true;
            }
        );
    }

    // ==================== Scan Operations | 扫描操作 ====================

    /**
     * Creates a scan (running fold) gatherer.
     * 创建扫描（运行折叠）收集器。
     *
     * <p>Produces intermediate accumulation results.</p>
     * <p>产生中间累积结果。</p>
     *
     * @param <T>         element type | 元素类型
     * @param <R>         result type | 结果类型
     * @param initial     initial value | 初始值
     * @param accumulator accumulator function | 累加器函数
     * @return gatherer | 收集器
     */
    public static <T, R> Gatherer<T, ?, R> scan(R initial, BiFunction<R, ? super T, R> accumulator) {
        Objects.requireNonNull(accumulator, "Accumulator must not be null");

        return Gatherer.ofSequential(
            () -> new Object[] { initial },
            (state, element, downstream) -> {
                @SuppressWarnings("unchecked")
                R current = (R) state[0];
                R next = accumulator.apply(current, element);
                state[0] = next;
                downstream.push(next);
                return true;
            }
        );
    }

    /**
     * Creates a scan gatherer with initial value and finisher.
     * 创建带初始值和完成器的扫描收集器。
     *
     * @param <T>         element type | 元素类型
     * @param <A>         accumulator type | 累加器类型
     * @param <R>         result type | 结果类型
     * @param initial     initial value supplier | 初始值提供者
     * @param accumulator accumulator function | 累加器函数
     * @param finisher    finisher function | 完成器函数
     * @return gatherer | 收集器
     */
    public static <T, A, R> Gatherer<T, ?, R> scan(
            Supplier<A> initial,
            BiFunction<A, ? super T, A> accumulator,
            Function<A, R> finisher) {
        Objects.requireNonNull(initial);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(finisher);

        return Gatherer.ofSequential(
            () -> new Object[] { initial.get() },
            (state, element, downstream) -> {
                @SuppressWarnings("unchecked")
                A current = (A) state[0];
                A next = accumulator.apply(current, element);
                state[0] = next;
                downstream.push(finisher.apply(next));
                return true;
            }
        );
    }

    // ==================== Filtering with State | 带状态的过滤 ====================

    /**
     * Creates a take-while-indexed gatherer.
     * 创建带索引的takeWhile收集器。
     *
     * @param <T>       element type | 元素类型
     * @param predicate predicate (index, element) | 谓词 (索引, 元素)
     * @return gatherer | 收集器
     */
    public static <T> Gatherer<T, ?, T> takeWhileIndexed(BiPredicate<Long, ? super T> predicate) {
        Objects.requireNonNull(predicate, "Predicate must not be null");

        return Gatherer.ofSequential(
            () -> new long[] { 0 },
            (state, element, downstream) -> {
                if (predicate.test(state[0]++, element)) {
                    downstream.push(element);
                    return true;
                }
                return false;
            }
        );
    }

    /**
     * Creates a drop-while-indexed gatherer.
     * 创建带索引的dropWhile收集器。
     *
     * @param <T>       element type | 元素类型
     * @param predicate predicate (index, element) | 谓词 (索引, 元素)
     * @return gatherer | 收集器
     */
    public static <T> Gatherer<T, ?, T> dropWhileIndexed(BiPredicate<Long, ? super T> predicate) {
        Objects.requireNonNull(predicate, "Predicate must not be null");

        return Gatherer.ofSequential(
            () -> new Object[] { 0L, true },
            (state, element, downstream) -> {
                long index = (Long) state[0];
                boolean dropping = (Boolean) state[1];
                state[0] = index + 1;

                if (dropping && predicate.test(index, element)) {
                    return true;
                }
                state[1] = false;
                downstream.push(element);
                return true;
            }
        );
    }

    /**
     * Creates a filter-indexed gatherer.
     * 创建带索引的过滤收集器。
     *
     * @param <T>       element type | 元素类型
     * @param predicate predicate (index, element) | 谓词 (索引, 元素)
     * @return gatherer | 收集器
     */
    public static <T> Gatherer<T, ?, T> filterIndexed(BiPredicate<Long, ? super T> predicate) {
        Objects.requireNonNull(predicate, "Predicate must not be null");

        return Gatherer.ofSequential(
            () -> new long[] { 0 },
            (state, element, downstream) -> {
                if (predicate.test(state[0]++, element)) {
                    downstream.push(element);
                }
                return true;
            }
        );
    }

    /**
     * Creates a changed gatherer (emit only when value changes).
     * 创建变化收集器（仅当值变化时发出）。
     *
     * @param <T> element type | 元素类型
     * @return gatherer | 收集器
     */
    public static <T> Gatherer<T, ?, T> changed() {
        return changedBy(Function.identity());
    }

    /**
     * Creates a changed-by gatherer (emit when key changes).
     * 创建按键变化收集器（当键变化时发出）。
     *
     * @param <T>          element type | 元素类型
     * @param <K>          key type | 键类型
     * @param keyExtractor key extractor | 键提取器
     * @return gatherer | 收集器
     */
    public static <T, K> Gatherer<T, ?, T> changedBy(Function<? super T, ? extends K> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "Key extractor must not be null");

        return Gatherer.ofSequential(
            () -> new Object[] { null, false },
            (state, element, downstream) -> {
                K key = keyExtractor.apply(element);
                boolean hasValue = (Boolean) state[1];
                @SuppressWarnings("unchecked")
                K previousKey = hasValue ? (K) state[0] : null;

                if (!hasValue || !Objects.equals(key, previousKey)) {
                    state[0] = key;
                    state[1] = true;
                    downstream.push(element);
                }
                return true;
            }
        );
    }

    // ==================== Mapping with Context | 带上下文的映射 ====================

    /**
     * Creates a map-indexed gatherer.
     * 创建带索引的映射收集器。
     *
     * @param <T>    element type | 元素类型
     * @param <R>    result type | 结果类型
     * @param mapper mapper (index, element) | 映射器 (索引, 元素)
     * @return gatherer | 收集器
     */
    public static <T, R> Gatherer<T, ?, R> mapIndexed(BiFunction<Long, ? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");

        return Gatherer.ofSequential(
            () -> new long[] { 0 },
            (state, element, downstream) -> {
                downstream.push(mapper.apply(state[0]++, element));
                return true;
            }
        );
    }

    /**
     * Creates a map-with-previous gatherer.
     * 创建带前一个元素的映射收集器。
     *
     * @param <T>    element type | 元素类型
     * @param <R>    result type | 结果类型
     * @param mapper mapper (previous, current) | 映射器 (前一个, 当前)
     * @return gatherer | 收集器
     */
    public static <T, R> Gatherer<T, ?, R> mapWithPrevious(BiFunction<? super T, ? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");

        return Gatherer.ofSequential(
            () -> new Object[] { null, false },
            (state, element, downstream) -> {
                if ((Boolean) state[1]) {
                    @SuppressWarnings("unchecked")
                    T previous = (T) state[0];
                    downstream.push(mapper.apply(previous, element));
                }
                state[0] = element;
                state[1] = true;
                return true;
            }
        );
    }

    /**
     * Creates a zip-with-next gatherer.
     * 创建与下一个元素配对的收集器。
     *
     * @param <T>    element type | 元素类型
     * @param <R>    result type | 结果类型
     * @param zipper zipper function | 配对函数
     * @return gatherer | 收集器
     */
    public static <T, R> Gatherer<T, ?, R> zipWithNext(BiFunction<? super T, ? super T, ? extends R> zipper) {
        Objects.requireNonNull(zipper, "Zipper must not be null");

        return Gatherer.ofSequential(
            () -> new Object[] { null, false },
            (state, element, downstream) -> {
                if ((Boolean) state[1]) {
                    @SuppressWarnings("unchecked")
                    T previous = (T) state[0];
                    downstream.push(zipper.apply(previous, element));
                }
                state[0] = element;
                state[1] = true;
                return true;
            }
        );
    }

    // ==================== Grouping | 分组 ====================

    /**
     * Creates a group-runs gatherer.
     * 创建连续分组收集器。
     *
     * <p>Groups consecutive elements with the same key.</p>
     * <p>将具有相同键的连续元素分组。</p>
     *
     * @param <T>          element type | 元素类型
     * @param <K>          key type | 键类型
     * @param keyExtractor key extractor | 键提取器
     * @return gatherer | 收集器
     */
    public static <T, K> Gatherer<T, ?, List<T>> groupRuns(Function<? super T, ? extends K> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "Key extractor must not be null");

        return Gatherer.ofSequential(
            () -> new Object[] { null, new ArrayList<T>(), false },
            (state, element, downstream) -> {
                K key = keyExtractor.apply(element);
                K currentKey = (K) state[0];
                @SuppressWarnings("unchecked")
                List<T> group = (List<T>) state[1];
                boolean hasKey = (Boolean) state[2];

                if (!hasKey) {
                    state[0] = key;
                    state[2] = true;
                    group.add(element);
                } else if (Objects.equals(key, currentKey)) {
                    group.add(element);
                } else {
                    downstream.push(new ArrayList<>(group));
                    group.clear();
                    group.add(element);
                    state[0] = key;
                }
                return true;
            },
            (state, downstream) -> {
                @SuppressWarnings("unchecked")
                List<T> group = (List<T>) state[1];
                if (!group.isEmpty()) {
                    downstream.push(group);
                }
            }
        );
    }

    // ==================== Limiting | 限制操作 ====================

    /**
     * Creates a take-last gatherer.
     * 创建获取最后n个元素的收集器。
     *
     * @param <T> element type | 元素类型
     * @param n   number of elements | 元素数量
     * @return gatherer | 收集器
     */
    public static <T> Gatherer<T, ?, T> takeLast(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive");
        }

        return Gatherer.ofSequential(
            () -> new ArrayDeque<T>(n),
            (deque, element, downstream) -> {
                if (deque.size() >= n) {
                    deque.removeFirst();
                }
                deque.addLast(element);
                return true;
            },
            (deque, downstream) -> {
                for (T element : deque) {
                    downstream.push(element);
                }
            }
        );
    }

    /**
     * Creates a drop-last gatherer.
     * 创建丢弃最后n个元素的收集器。
     *
     * @param <T> element type | 元素类型
     * @param n   number of elements | 元素数量
     * @return gatherer | 收集器
     */
    public static <T> Gatherer<T, ?, T> dropLast(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive");
        }

        return Gatherer.ofSequential(
            () -> new ArrayDeque<T>(n + 1),
            (deque, element, downstream) -> {
                deque.addLast(element);
                if (deque.size() > n) {
                    downstream.push(deque.removeFirst());
                }
                return true;
            }
        );
    }

    // ==================== Fold Operations | 折叠操作 ====================

    /**
     * Creates a fold gatherer (produces single result).
     * 创建折叠收集器（产生单个结果）。
     *
     * @param <T>         element type | 元素类型
     * @param <R>         result type | 结果类型
     * @param initial     initial value | 初始值
     * @param accumulator accumulator function | 累加器函数
     * @return gatherer | 收集器
     */
    public static <T, R> Gatherer<T, ?, R> fold(R initial, BiFunction<R, ? super T, R> accumulator) {
        Objects.requireNonNull(accumulator, "Accumulator must not be null");

        return Gatherers.fold(() -> initial, accumulator);
    }

    // ==================== Interleaving | 交错操作 ====================

    /**
     * Creates an interleave gatherer with separators.
     * 创建带分隔符的交错收集器。
     *
     * @param <T>       element type | 元素类型
     * @param separator separator element | 分隔符元素
     * @return gatherer | 收集器
     */
    public static <T> Gatherer<T, ?, T> intersperse(T separator) {
        return Gatherer.ofSequential(
            () -> new boolean[] { false },
            (state, element, downstream) -> {
                if (state[0]) {
                    downstream.push(separator);
                }
                downstream.push(element);
                state[0] = true;
                return true;
            }
        );
    }

    // ==================== Utility | 工具方法 ====================

    /**
     * Creates an indexed wrapper gatherer.
     * 创建索引包装收集器。
     *
     * @param <T> element type | 元素类型
     * @return gatherer producing indexed elements | 产生索引元素的收集器
     */
    public static <T> Gatherer<T, ?, IndexedElement<T>> indexed() {
        return Gatherer.ofSequential(
            () -> new long[] { 0 },
            (state, element, downstream) -> {
                downstream.push(new IndexedElement<>(state[0]++, element));
                return true;
            }
        );
    }

    /**
     * Indexed element record.
     * 索引元素记录。
     *
     * @param <T>   element type | 元素类型
     * @param index the index | 索引
     * @param value the value | 值
     */
    public record IndexedElement<T>(long index, T value) {}

    // ==================== Zip Operations | 配对操作 ====================

    /**
     * Creates a gatherer that pairs each element with its zero-based index.
     * 创建一个将每个元素与其从零开始的索引配对的收集器。
     *
     * <p>Unlike {@link #indexed()} which returns {@link IndexedElement}, this method
     * returns {@link Pair Pair&lt;Long, T&gt;} for better interoperability with
     * other Pair-based APIs.</p>
     * <p>与返回 {@link IndexedElement} 的 {@link #indexed()} 不同，此方法返回
     * {@link Pair Pair&lt;Long, T&gt;} 以便与其他基于 Pair 的 API 更好地互操作。</p>
     *
     * <p>Example: {@code [a, b, c] → [Pair(0,a), Pair(1,b), Pair(2,c)]}</p>
     *
     * @param <T> element type | 元素类型
     * @return gatherer producing index-element pairs | 产生索引-元素配对的收集器
     * @author Leon Soo
     * @since JDK 25, opencode-base-collections V1.0.3
     */
    public static <T> Gatherer<T, ?, Pair<Long, T>> zipWithIndex() {
        return Gatherer.ofSequential(
            AtomicLong::new,
            (state, element, downstream) -> {
                downstream.push(Pair.of(state.getAndIncrement(), element));
                return true;
            }
        );
    }

    // ==================== Conditional Take | 条件获取 ====================

    /**
     * Creates a gatherer that takes elements while the predicate is true,
     * including the first element that fails the predicate.
     * 创建一个在谓词为真时获取元素的收集器，包括第一个不满足谓词的元素。
     *
     * <p>This differs from JDK's {@code takeWhile} which excludes the boundary element.</p>
     * <p>这与 JDK 的 {@code takeWhile}（排除边界元素）不同。</p>
     *
     * <p>Example: {@code [1,2,3,4,5].takeWhileInclusive(x -> x < 3) → [1,2,3]}</p>
     *
     * @param <T>       element type | 元素类型
     * @param predicate the predicate to test elements | 用于测试元素的谓词
     * @return gatherer | 收集器
     * @throws NullPointerException if predicate is null | 如果谓词为 null
     * @author Leon Soo
     * @since JDK 25, opencode-base-collections V1.0.3
     */
    public static <T> Gatherer<T, ?, T> takeWhileInclusive(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "Predicate must not be null");

        return Gatherer.ofSequential(
            AtomicBoolean::new,
            (done, element, downstream) -> {
                if (done.get()) {
                    return false;
                }
                if (!predicate.test(element)) {
                    done.set(true);
                }
                downstream.push(element);
                return !done.get();
            }
        );
    }

    // ==================== Interleave Operations | 交错合并操作 ====================

    /**
     * Creates a gatherer that interleaves elements from the source stream
     * with elements from the provided iterator, alternating between them.
     * 创建一个将源流元素与提供的迭代器元素交替合并的收集器。
     *
     * <p>If one source is exhausted before the other, remaining elements
     * from the longer source are appended.</p>
     * <p>如果一个源在另一个之前耗尽，较长源的剩余元素将被追加。</p>
     *
     * <p>Example: {@code [1,2,3].interleave([a,b]) → [1,a,2,b,3]}</p>
     *
     * @param <T>   element type | 元素类型
     * @param other the iterator to interleave with | 要交错合并的迭代器
     * @return gatherer | 收集器
     * @throws NullPointerException if other is null | 如果 other 为 null
     * @author Leon Soo
     * @since JDK 25, opencode-base-collections V1.0.3
     */
    public static <T> Gatherer<T, ?, T> interleave(Iterator<? extends T> other) {
        Objects.requireNonNull(other, "Iterator must not be null");

        return Gatherer.ofSequential(
            () -> other,
            (iter, element, downstream) -> {
                downstream.push(element);
                if (iter.hasNext()) {
                    downstream.push(iter.next());
                }
                return true;
            },
            (iter, downstream) -> {
                while (iter.hasNext()) {
                    downstream.push(iter.next());
                }
            }
        );
    }

    /**
     * Creates a peek gatherer for debugging.
     * 创建用于调试的peek收集器。
     *
     * @param <T>    element type | 元素类型
     * @param action action to perform | 要执行的操作
     * @return gatherer | 收集器
     */
    public static <T> Gatherer<T, ?, T> peekWithIndex(BiConsumer<Long, ? super T> action) {
        Objects.requireNonNull(action, "Action must not be null");

        return Gatherer.ofSequential(
            () -> new long[] { 0 },
            (state, element, downstream) -> {
                action.accept(state[0]++, element);
                downstream.push(element);
                return true;
            }
        );
    }
}
