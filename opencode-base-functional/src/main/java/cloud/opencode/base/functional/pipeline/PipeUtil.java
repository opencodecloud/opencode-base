package cloud.opencode.base.functional.pipeline;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * PipeUtil - Utility methods for pipeline operations
 * PipeUtil - 管道操作的工具方法
 *
 * <p>Provides static utility methods for common pipeline operations,
 * including pipe operator simulation, conditional execution, and
 * collection transformations.</p>
 * <p>提供常见管道操作的静态工具方法，包括管道操作符模拟、条件执行
 * 和集合转换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pipe operator (|>) simulation - 管道操作符模拟</li>
 *   <li>Conditional execution - 条件执行</li>
 *   <li>Tap/peek operations - 窥视操作</li>
 *   <li>Collection utilities - 集合工具</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Pipe operator style
 * String result = PipeUtil.pipe("  hello  ")
 *     .then(String::trim)
 *     .then(String::toUpperCase)
 *     .then(s -> s + "!")
 *     .get();
 *
 * // Conditional execution
 * String cleaned = PipeUtil.when(input != null, input, String::trim);
 *
 * // Tap for side effects
 * String value = PipeUtil.tap(computeValue(), System.out::println);
 *
 * // Transform collection
 * List<String> names = PipeUtil.transform(users, User::getName);
 *
 * // Chain transformations
 * Function<String, Integer> parseLength = PipeUtil.chain(
 *     String::trim,
 *     String::length
 * );
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless methods) - 线程安全: 是 (无状态方法)</li>
 *   <li>Null-safe: Handles null with when/whenNonNull - 空值安全: 使用 when/whenNonNull 处理</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for pipe/tap/when; O(n) for transform/filterAndTransform where n is collection size - 时间复杂度: pipe/tap/when 为 O(1)；transform/filterAndTransform 为 O(n)，n 为集合大小</li>
 *   <li>Space complexity: O(n) for collection operations, O(1) for scalar operations - 空间复杂度: 集合操作 O(n)，标量操作 O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public final class PipeUtil {

    private PipeUtil() {
        // Utility class
    }

    // ==================== Pipe Operator | 管道操作符 ====================

    /**
     * Start a pipe chain with a value
     * 以值开始管道链
     *
     * <p>Simulates the pipe operator (|>) found in functional languages.</p>
     * <p>模拟函数式语言中的管道操作符 (|>)。</p>
     *
     * @param value starting value - 起始值
     * @param <T>   value type - 值类型
     * @return pipe holder for chaining
     */
    public static <T> Pipe<T> pipe(T value) {
        return new Pipe<>(value);
    }

    /**
     * Pipe - Value holder for pipe operations
     * Pipe - 管道操作的值持有者
     *
     * @param <T> value type - 值类型
     */
    public static final class Pipe<T> {
        private final T value;

        Pipe(T value) {
            this.value = value;
        }

        /**
         * Apply a transformation
         * 应用转换
         *
         * @param function transformation - 转换
         * @param <R>      result type - 结果类型
         * @return pipe with transformed value
         */
        public <R> Pipe<R> then(Function<? super T, ? extends R> function) {
            return new Pipe<>(function.apply(value));
        }

        /**
         * Apply transformation if value is non-null
         * 如果值非空则应用转换
         *
         * @param function transformation - 转换
         * @param <R>      result type - 结果类型
         * @return pipe with transformed value or null
         */
        public <R> Pipe<R> thenIfPresent(Function<? super T, ? extends R> function) {
            if (value == null) {
                return new Pipe<>(null);
            }
            return new Pipe<>(function.apply(value));
        }

        /**
         * Apply transformation if condition is true
         * 如果条件为真则应用转换
         *
         * @param condition condition to check - 要检查的条件
         * @param function  transformation - 转换
         * @return pipe with potentially transformed value
         */
        public Pipe<T> thenIf(boolean condition, UnaryOperator<T> function) {
            if (condition) {
                return new Pipe<>(function.apply(value));
            }
            return this;
        }

        /**
         * Perform side effect without changing value
         * 执行副作用而不改变值
         *
         * @param consumer side effect - 副作用
         * @return this pipe
         */
        public Pipe<T> tap(Consumer<? super T> consumer) {
            if (value != null) {
                consumer.accept(value);
            }
            return this;
        }

        /**
         * Get the final value
         * 获取最终值
         *
         * @return the value
         */
        public T get() {
            return value;
        }

        /**
         * Get the value or default if null
         * 获取值或默认值（如果为 null）
         *
         * @param defaultValue default value - 默认值
         * @return value or default
         */
        public T getOrElse(T defaultValue) {
            return value != null ? value : defaultValue;
        }
    }

    // ==================== Conditional Operations | 条件操作 ====================

    /**
     * Execute transformation conditionally
     * 条件执行转换
     *
     * @param condition condition to check - 要检查的条件
     * @param value     value to transform - 要转换的值
     * @param function  transformation function - 转换函数
     * @param <T>       value type - 值类型
     * @return transformed value if condition true, original otherwise
     */
    public static <T> T when(boolean condition, T value, UnaryOperator<T> function) {
        return condition ? function.apply(value) : value;
    }

    /**
     * Execute transformation if value is non-null
     * 如果值非空则执行转换
     *
     * @param value    value to transform - 要转换的值
     * @param function transformation function - 转换函数
     * @param <T>      input type - 输入类型
     * @param <R>      result type - 结果类型
     * @return transformed value or null
     */
    public static <T, R> R whenNonNull(T value, Function<? super T, ? extends R> function) {
        return value != null ? function.apply(value) : null;
    }

    /**
     * Execute transformation if value is non-null, with default
     * 如果值非空则执行转换，带默认值
     *
     * @param value        value to transform - 要转换的值
     * @param function     transformation function - 转换函数
     * @param defaultValue default if null - null 时的默认值
     * @param <T>          input type - 输入类型
     * @param <R>          result type - 结果类型
     * @return transformed value or default
     */
    public static <T, R> R whenNonNullOrElse(T value, Function<? super T, ? extends R> function, R defaultValue) {
        return value != null ? function.apply(value) : defaultValue;
    }

    /**
     * Execute transformation if predicate matches
     * 如果谓词匹配则执行转换
     *
     * @param value     value to test and transform - 要测试和转换的值
     * @param predicate condition to test - 要测试的条件
     * @param function  transformation function - 转换函数
     * @param <T>       value type - 值类型
     * @return transformed value if matches, original otherwise
     */
    public static <T> T whenMatches(T value, Predicate<? super T> predicate, UnaryOperator<T> function) {
        return predicate.test(value) ? function.apply(value) : value;
    }

    // ==================== Tap Operations | 窥视操作 ====================

    /**
     * Execute side effect and return value
     * 执行副作用并返回值
     *
     * <p>Useful for logging or debugging in the middle of a chain.</p>
     * <p>在链中间进行日志记录或调试时很有用。</p>
     *
     * @param value    the value - 值
     * @param consumer side effect - 副作用
     * @param <T>      value type - 值类型
     * @return the original value
     */
    public static <T> T tap(T value, Consumer<? super T> consumer) {
        consumer.accept(value);
        return value;
    }

    /**
     * Execute side effect if value is non-null
     * 如果值非空则执行副作用
     *
     * @param value    the value - 值
     * @param consumer side effect - 副作用
     * @param <T>      value type - 值类型
     * @return the original value
     */
    public static <T> T tapIfPresent(T value, Consumer<? super T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
        return value;
    }

    // ==================== Collection Operations | 集合操作 ====================

    /**
     * Transform a collection
     * 转换集合
     *
     * @param collection source collection - 源集合
     * @param mapper     transformation - 转换
     * @param <T>        input type - 输入类型
     * @param <R>        result type - 结果类型
     * @return list of transformed elements
     */
    public static <T, R> List<R> transform(Collection<T> collection, Function<? super T, ? extends R> mapper) {
        return collection.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * Filter and transform a collection
     * 过滤并转换集合
     *
     * @param collection source collection - 源集合
     * @param filter     filter predicate - 过滤谓词
     * @param mapper     transformation - 转换
     * @param <T>        input type - 输入类型
     * @param <R>        result type - 结果类型
     * @return list of filtered and transformed elements
     */
    public static <T, R> List<R> filterAndTransform(Collection<T> collection,
                                                     Predicate<? super T> filter,
                                                     Function<? super T, ? extends R> mapper) {
        return collection.stream()
                .filter(filter)
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * Filter a collection removing nulls
     * 过滤集合移除 null
     *
     * @param collection source collection - 源集合
     * @param <T>        element type - 元素类型
     * @return list without nulls
     */
    public static <T> List<T> filterNonNull(Collection<T> collection) {
        return collection.stream()
                .filter(Objects::nonNull)
                .toList();
    }

    // ==================== Function Chaining | 函数链接 ====================

    /**
     * Chain two functions
     * 链接两个函数
     *
     * @param first  first function - 第一个函数
     * @param second second function - 第二个函数
     * @param <T>    input type - 输入类型
     * @param <U>    intermediate type - 中间类型
     * @param <R>    result type - 结果类型
     * @return composed function
     */
    public static <T, U, R> Function<T, R> chain(Function<? super T, ? extends U> first,
                                                  Function<? super U, ? extends R> second) {
        return t -> second.apply(first.apply(t));
    }

    /**
     * Chain three functions
     * 链接三个函数
     *
     * @param first  first function - 第一个函数
     * @param second second function - 第二个函数
     * @param third  third function - 第三个函数
     * @param <T>    input type - 输入类型
     * @param <U>    first intermediate type - 第一个中间类型
     * @param <V>    second intermediate type - 第二个中间类型
     * @param <R>    result type - 结果类型
     * @return composed function
     */
    public static <T, U, V, R> Function<T, R> chain(Function<? super T, ? extends U> first,
                                                     Function<? super U, ? extends V> second,
                                                     Function<? super V, ? extends R> third) {
        return t -> third.apply(second.apply(first.apply(t)));
    }

    /**
     * Create a transformation that applies multiple transformations in order
     * 创建按顺序应用多个转换的转换
     *
     * @param transformations transformations to apply - 要应用的转换
     * @param <T>             value type - 值类型
     * @return composed transformation
     */
    @SafeVarargs
    public static <T> UnaryOperator<T> sequence(UnaryOperator<T>... transformations) {
        return value -> {
            T result = value;
            for (UnaryOperator<T> transform : transformations) {
                result = transform.apply(result);
            }
            return result;
        };
    }

    /**
     * Apply a value to a function (reverse application)
     * 将值应用于函数（反向应用）
     *
     * <p>Allows writing value |> function style code.</p>
     * <p>允许编写 value |> function 风格的代码。</p>
     *
     * @param value    the value - 值
     * @param function the function - 函数
     * @param <T>      input type - 输入类型
     * @param <R>      result type - 结果类型
     * @return function result
     */
    public static <T, R> R apply(T value, Function<? super T, ? extends R> function) {
        return function.apply(value);
    }
}
