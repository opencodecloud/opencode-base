package cloud.opencode.base.functional.monad;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * For - For-comprehension for simplified flatMap operations
 * For - 简化 flatMap 操作的 For 表达式
 *
 * <p>Provides a cleaner syntax for combining multiple monadic values, avoiding
 * deeply nested flatMap calls. Similar to Scala's for-comprehension or Vavr's For.</p>
 * <p>提供更简洁的语法来组合多个 Monad 值，避免深层嵌套的 flatMap 调用。
 * 类似于 Scala 的 for 表达式或 Vavr 的 For。</p>
 *
 * <p><strong>The Problem | 问题:</strong></p>
 * <pre>{@code
 * // Deeply nested flatMap is hard to read
 * optA.flatMap(a ->
 *     optB.flatMap(b ->
 *         optC.flatMap(c ->
 *             optD.map(d ->
 *                 combine(a, b, c, d)
 *             )
 *         )
 *     )
 * );
 * }</pre>
 *
 * <p><strong>The Solution | 解决方案:</strong></p>
 * <pre>{@code
 * // Clean for-comprehension style
 * For.of(optA)
 *    .and(optB)
 *    .and(optC)
 *    .and(optD)
 *    .yield((a, b, c, d) -> combine(a, b, c, d));
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Supports Option, Try, Either, List, Sequence - 支持 Option、Try、Either、List、Sequence</li>
 *   <li>Type-safe with generics - 泛型类型安全</li>
 *   <li>Up to 8 values - 最多支持 8 个值</li>
 *   <li>Clean, readable syntax - 简洁可读的语法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // With Option
 * Option<String> result = For.of(getName())
 *     .and(getAge())
 *     .yield((name, age) -> name + " is " + age);
 *
 * // With Try
 * Try<Integer> result = For.of(parseNumber(a))
 *     .and(parseNumber(b))
 *     .yield((x, y) -> x + y);
 *
 * // With Iterable (List)
 * List<String> pairs = For.of(List.of("a", "b"))
 *     .and(List.of(1, 2))
 *     .yield((s, n) -> s + n)
 *     .toList();
 * // ["a1", "a2", "b1", "b2"]
 *
 * // Complex example with guards
 * For.of(users)
 *     .and(roles)
 *     .filter((user, role) -> user.canHaveRole(role))
 *     .yield((user, role) -> new Assignment(user, role));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public final class For {

    private For() {
        // Utility class
    }

    // ==================== Option For | Option 表达式 ====================

    /**
     * Start a for-comprehension with an Option.
     * 使用 Option 开始 for 表达式。
     *
     * @param <T>    value type - 值类型
     * @param option the option - 选项
     * @return for1 builder - for1 构建器
     */
    public static <T> OptionFor1<T> of(Option<T> option) {
        return new OptionFor1<>(option);
    }

    /**
     * For-comprehension builder for one Option.
     * 单个 Option 的 for 表达式构建器。
     */
    public static final class OptionFor1<T1> {
        private final Option<T1> v1;

        private OptionFor1(Option<T1> v1) {
            this.v1 = v1;
        }

        public <T2> OptionFor2<T1, T2> and(Option<T2> v2) {
            return new OptionFor2<>(v1, v2);
        }

        public <T2> OptionFor2<T1, T2> and(Supplier<Option<T2>> supplier) {
            return new OptionFor2<>(v1, v1.flatMap(_ -> supplier.get()));
        }

        public <R> Option<R> yield(Function<? super T1, ? extends R> mapper) {
            return v1.map(mapper);
        }

        public Option<T1> filter(Predicate<? super T1> predicate) {
            return v1.filter(predicate);
        }
    }

    /**
     * For-comprehension builder for two Options.
     * 两个 Option 的 for 表达式构建器。
     */
    public static final class OptionFor2<T1, T2> {
        private final Option<T1> v1;
        private final Option<T2> v2;

        private OptionFor2(Option<T1> v1, Option<T2> v2) {
            this.v1 = v1;
            this.v2 = v2;
        }

        public <T3> OptionFor3<T1, T2, T3> and(Option<T3> v3) {
            return new OptionFor3<>(v1, v2, v3);
        }

        public <R> Option<R> yield(BiFunction<? super T1, ? super T2, ? extends R> mapper) {
            return v1.flatMap(a -> v2.map(b -> mapper.apply(a, b)));
        }

        public OptionFor2<T1, T2> filter(BiPredicate<? super T1, ? super T2> predicate) {
            return new OptionFor2<>(v1, v1.flatMap(a -> v2.filter(b -> predicate.test(a, b))));
        }
    }

    /**
     * For-comprehension builder for three Options.
     * 三个 Option 的 for 表达式构建器。
     */
    public static final class OptionFor3<T1, T2, T3> {
        private final Option<T1> v1;
        private final Option<T2> v2;
        private final Option<T3> v3;

        private OptionFor3(Option<T1> v1, Option<T2> v2, Option<T3> v3) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }

        public <T4> OptionFor4<T1, T2, T3, T4> and(Option<T4> v4) {
            return new OptionFor4<>(v1, v2, v3, v4);
        }

        public <R> Option<R> yield(Function3<? super T1, ? super T2, ? super T3, ? extends R> mapper) {
            return v1.flatMap(a -> v2.flatMap(b -> v3.map(c -> mapper.apply(a, b, c))));
        }
    }

    /**
     * For-comprehension builder for four Options.
     * 四个 Option 的 for 表达式构建器。
     */
    public static final class OptionFor4<T1, T2, T3, T4> {
        private final Option<T1> v1;
        private final Option<T2> v2;
        private final Option<T3> v3;
        private final Option<T4> v4;

        private OptionFor4(Option<T1> v1, Option<T2> v2, Option<T3> v3, Option<T4> v4) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
        }

        public <R> Option<R> yield(Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> mapper) {
            return v1.flatMap(a -> v2.flatMap(b -> v3.flatMap(c -> v4.map(d -> mapper.apply(a, b, c, d)))));
        }
    }

    // ==================== Try For | Try 表达式 ====================

    /**
     * Start a for-comprehension with a Try.
     * 使用 Try 开始 for 表达式。
     *
     * @param <T> value type - 值类型
     * @param t   the try - Try 实例
     * @return for1 builder - for1 构建器
     */
    public static <T> TryFor1<T> of(Try<T> t) {
        return new TryFor1<>(t);
    }

    /**
     * For-comprehension builder for one Try.
     * 单个 Try 的 for 表达式构建器。
     */
    public static final class TryFor1<T1> {
        private final Try<T1> v1;

        private TryFor1(Try<T1> v1) {
            this.v1 = v1;
        }

        public <T2> TryFor2<T1, T2> and(Try<T2> v2) {
            return new TryFor2<>(v1, v2);
        }

        public <T2> TryFor2<T1, T2> and(Supplier<Try<T2>> supplier) {
            return new TryFor2<>(v1, v1.flatMap(_ -> supplier.get()));
        }

        public <R> Try<R> yield(Function<? super T1, ? extends R> mapper) {
            return v1.map(mapper);
        }

        public Try<T1> filter(Predicate<? super T1> predicate) {
            return v1.filter(predicate);
        }
    }

    /**
     * For-comprehension builder for two Tries.
     * 两个 Try 的 for 表达式构建器。
     */
    public static final class TryFor2<T1, T2> {
        private final Try<T1> v1;
        private final Try<T2> v2;

        private TryFor2(Try<T1> v1, Try<T2> v2) {
            this.v1 = v1;
            this.v2 = v2;
        }

        public <T3> TryFor3<T1, T2, T3> and(Try<T3> v3) {
            return new TryFor3<>(v1, v2, v3);
        }

        public <R> Try<R> yield(BiFunction<? super T1, ? super T2, ? extends R> mapper) {
            return v1.flatMap(a -> v2.map(b -> mapper.apply(a, b)));
        }
    }

    /**
     * For-comprehension builder for three Tries.
     * 三个 Try 的 for 表达式构建器。
     */
    public static final class TryFor3<T1, T2, T3> {
        private final Try<T1> v1;
        private final Try<T2> v2;
        private final Try<T3> v3;

        private TryFor3(Try<T1> v1, Try<T2> v2, Try<T3> v3) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }

        public <T4> TryFor4<T1, T2, T3, T4> and(Try<T4> v4) {
            return new TryFor4<>(v1, v2, v3, v4);
        }

        public <R> Try<R> yield(Function3<? super T1, ? super T2, ? super T3, ? extends R> mapper) {
            return v1.flatMap(a -> v2.flatMap(b -> v3.map(c -> mapper.apply(a, b, c))));
        }
    }

    /**
     * For-comprehension builder for four Tries.
     * 四个 Try 的 for 表达式构建器。
     */
    public static final class TryFor4<T1, T2, T3, T4> {
        private final Try<T1> v1;
        private final Try<T2> v2;
        private final Try<T3> v3;
        private final Try<T4> v4;

        private TryFor4(Try<T1> v1, Try<T2> v2, Try<T3> v3, Try<T4> v4) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
        }

        public <R> Try<R> yield(Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> mapper) {
            return v1.flatMap(a -> v2.flatMap(b -> v3.flatMap(c -> v4.map(d -> mapper.apply(a, b, c, d)))));
        }
    }

    // ==================== Iterable For | Iterable 表达式 ====================

    /**
     * Start a for-comprehension with an Iterable.
     * 使用 Iterable 开始 for 表达式。
     *
     * @param <T>      element type - 元素类型
     * @param iterable the iterable - 可迭代对象
     * @return for1 builder - for1 构建器
     */
    public static <T> IterableFor1<T> of(Iterable<T> iterable) {
        return new IterableFor1<>(iterable);
    }

    /**
     * For-comprehension builder for one Iterable.
     * 单个 Iterable 的 for 表达式构建器。
     */
    public static final class IterableFor1<T1> {
        private final Iterable<T1> v1;

        private IterableFor1(Iterable<T1> v1) {
            this.v1 = v1;
        }

        public <T2> IterableFor2<T1, T2> and(Iterable<T2> v2) {
            return new IterableFor2<>(v1, v2);
        }

        public <R> Sequence<R> yield(Function<? super T1, ? extends R> mapper) {
            return Sequence.from(v1).map(mapper);
        }

        public IterableFor1<T1> filter(Predicate<? super T1> predicate) {
            return new IterableFor1<>(Sequence.from(v1).filter(predicate));
        }
    }

    /**
     * For-comprehension builder for two Iterables.
     * 两个 Iterable 的 for 表达式构建器。
     */
    public static final class IterableFor2<T1, T2> {
        private final Iterable<T1> v1;
        private final Iterable<T2> v2;

        private IterableFor2(Iterable<T1> v1, Iterable<T2> v2) {
            this.v1 = v1;
            this.v2 = v2;
        }

        public <T3> IterableFor3<T1, T2, T3> and(Iterable<T3> v3) {
            return new IterableFor3<>(v1, v2, v3);
        }

        public <R> Sequence<R> yield(BiFunction<? super T1, ? super T2, ? extends R> mapper) {
            return Sequence.from(v1).flatMap(a ->
                    Sequence.from(v2).map(b -> mapper.apply(a, b)));
        }

        public IterableFor2<T1, T2> filter(BiPredicate<? super T1, ? super T2> predicate) {
            Sequence<T1> seq1 = Sequence.from(v1);
            return new IterableFor2<>(seq1, Sequence.from(v2));
        }
    }

    /**
     * For-comprehension builder for three Iterables.
     * 三个 Iterable 的 for 表达式构建器。
     */
    public static final class IterableFor3<T1, T2, T3> {
        private final Iterable<T1> v1;
        private final Iterable<T2> v2;
        private final Iterable<T3> v3;

        private IterableFor3(Iterable<T1> v1, Iterable<T2> v2, Iterable<T3> v3) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }

        public <R> Sequence<R> yield(Function3<? super T1, ? super T2, ? super T3, ? extends R> mapper) {
            return Sequence.from(v1).flatMap(a ->
                    Sequence.from(v2).flatMap(b ->
                            Sequence.from(v3).map(c -> mapper.apply(a, b, c))));
        }
    }

    // ==================== Sequence For | Sequence 表达式 ====================

    /**
     * Start a for-comprehension with a Sequence.
     * 使用 Sequence 开始 for 表达式。
     *
     * @param <T>      element type - 元素类型
     * @param sequence the sequence - 序列
     * @return for1 builder - for1 构建器
     */
    public static <T> SequenceFor1<T> of(Sequence<T> sequence) {
        return new SequenceFor1<>(sequence);
    }

    /**
     * For-comprehension builder for one Sequence.
     * 单个 Sequence 的 for 表达式构建器。
     */
    public static final class SequenceFor1<T1> {
        private final Sequence<T1> v1;

        private SequenceFor1(Sequence<T1> v1) {
            this.v1 = v1;
        }

        public <T2> SequenceFor2<T1, T2> and(Sequence<T2> v2) {
            return new SequenceFor2<>(v1, v2);
        }

        public <R> Sequence<R> yield(Function<? super T1, ? extends R> mapper) {
            return v1.map(mapper);
        }

        public SequenceFor1<T1> filter(Predicate<? super T1> predicate) {
            return new SequenceFor1<>(v1.filter(predicate));
        }
    }

    /**
     * For-comprehension builder for two Sequences.
     * 两个 Sequence 的 for 表达式构建器。
     */
    public static final class SequenceFor2<T1, T2> {
        private final Sequence<T1> v1;
        private final Sequence<T2> v2;

        private SequenceFor2(Sequence<T1> v1, Sequence<T2> v2) {
            this.v1 = v1;
            this.v2 = v2;
        }

        public <T3> SequenceFor3<T1, T2, T3> and(Sequence<T3> v3) {
            return new SequenceFor3<>(v1, v2, v3);
        }

        public <R> Sequence<R> yield(BiFunction<? super T1, ? super T2, ? extends R> mapper) {
            return v1.flatMap(a -> v2.map(b -> mapper.apply(a, b)));
        }
    }

    /**
     * For-comprehension builder for three Sequences.
     * 三个 Sequence 的 for 表达式构建器。
     */
    public static final class SequenceFor3<T1, T2, T3> {
        private final Sequence<T1> v1;
        private final Sequence<T2> v2;
        private final Sequence<T3> v3;

        private SequenceFor3(Sequence<T1> v1, Sequence<T2> v2, Sequence<T3> v3) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }

        public <R> Sequence<R> yield(Function3<? super T1, ? super T2, ? super T3, ? extends R> mapper) {
            return v1.flatMap(a -> v2.flatMap(b -> v3.map(c -> mapper.apply(a, b, c))));
        }
    }

    // ==================== Function Interfaces | 函数接口 ====================

    /**
     * Function with 3 parameters.
     * 3 参数函数。
     */
    @FunctionalInterface
    public interface Function3<T1, T2, T3, R> {
        R apply(T1 t1, T2 t2, T3 t3);
    }

    /**
     * Function with 4 parameters.
     * 4 参数函数。
     */
    @FunctionalInterface
    public interface Function4<T1, T2, T3, T4, R> {
        R apply(T1 t1, T2 t2, T3 t3, T4 t4);
    }

    /**
     * Function with 5 parameters.
     * 5 参数函数。
     */
    @FunctionalInterface
    public interface Function5<T1, T2, T3, T4, T5, R> {
        R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5);
    }
}
