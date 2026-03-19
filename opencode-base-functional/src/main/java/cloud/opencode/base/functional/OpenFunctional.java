package cloud.opencode.base.functional;

import cloud.opencode.base.functional.async.AsyncFunctionUtil;
import cloud.opencode.base.functional.async.LazyAsync;
import cloud.opencode.base.functional.function.CheckedBiConsumer;
import cloud.opencode.base.functional.function.CheckedBiFunction;
import cloud.opencode.base.functional.function.FunctionUtil;
import cloud.opencode.base.functional.monad.*;
import cloud.opencode.base.functional.optics.Lens;
import cloud.opencode.base.functional.optics.OptionalLens;
import cloud.opencode.base.functional.pattern.Case;
import cloud.opencode.base.functional.pattern.OpenMatch;
import cloud.opencode.base.functional.pattern.Pattern;
import cloud.opencode.base.functional.pipeline.Pipeline;
import cloud.opencode.base.functional.pipeline.PipeUtil;
import cloud.opencode.base.functional.record.RecordUtil;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * OpenFunctional - Unified entry point for functional programming utilities
 * OpenFunctional - 函数式编程工具的统一入口点
 *
 * <p>Provides convenient static methods to access all functional programming
 * features in the opencode-base-functional module. This is the main entry
 * point for using functional utilities.</p>
 * <p>提供便捷的静态方法来访问 opencode-base-functional 模块中的所有函数式编程功能。
 * 这是使用函数式工具的主入口点。</p>
 *
 * <p><strong>Module Overview | 模块概览:</strong></p>
 * <ul>
 *   <li>Monad types: Try, Either, Option, Validation, Lazy - Monad 类型</li>
 *   <li>Pattern matching: OpenMatch with fluent API - 模式匹配</li>
 *   <li>Function utilities: compose, curry, memoize - 函数工具</li>
 *   <li>Optics: Lens for immutable updates - 透镜用于不可变更新</li>
 *   <li>Pipeline: Data transformation chains - 管道用于数据转换链</li>
 *   <li>Async: Virtual Thread utilities - 异步虚拟线程工具</li>
 *   <li>Record: Record manipulation utilities - Record 操作工具</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Try monad for exception handling
 * Try<Integer> result = OpenFunctional.tryOf(() -> Integer.parseInt(input));
 *
 * // Either for error handling
 * Either<String, User> user = OpenFunctional.right(new User("Alice"));
 *
 * // Option for nullable values
 * Option<String> name = OpenFunctional.some("value");
 *
 * // Pattern matching
 * String type = OpenFunctional.match(value)
 *     .caseOf(String.class, s -> "String: " + s)
 *     .caseOf(Integer.class, n -> "Number: " + n)
 *     .orElse(o -> "Unknown");
 *
 * // Function composition
 * Function<String, Integer> parseAndDouble = OpenFunctional.compose(
 *     Integer::parseInt,
 *     n -> n * 2
 * );
 *
 * // Lens for immutable updates
 * Lens<Person, String> nameLens = OpenFunctional.lens(
 *     Person::name,
 *     (p, n) -> new Person(n, p.age())
 * );
 *
 * // Async with Virtual Threads
 * CompletableFuture<Data> future = OpenFunctional.async(() -> fetchData());
 *
 * // Pipeline transformations
 * String result = OpenFunctional.pipe("  hello  ")
 *     .then(String::trim)
 *     .then(String::toUpperCase)
 *     .get();
 * }</pre>
 *
 * <p><strong>Design Philosophy | 设计理念:</strong></p>
 * <ul>
 *   <li>Immutability - All transformations return new values - 不可变性 - 所有转换返回新值</li>
 *   <li>Type safety - Generic types prevent runtime errors - 类型安全 - 泛型防止运行时错误</li>
 *   <li>Null safety - Option/Try prevent NPEs - 空值安全 - Option/Try 防止 NPE</li>
 *   <li>Composability - Functions and types compose naturally - 可组合性 - 函数和类型自然组合</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: All methods are thread-safe - 线程安全: 所有方法线程安全</li>
 *   <li>No side effects: Pure functions by default - 无副作用: 默认纯函数</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Functional programming entry point with Try, Either, Option monads - 函数式编程入口，提供Try、Either、Option单子</li>
 *   <li>Pattern matching with type-safe case expressions - 类型安全的模式匹配</li>
 *   <li>Lazy evaluation and memoization support - 惰性求值与记忆化支持</li>
 *   <li>Pipeline composition for function chaining - 管道组合用于函数链式调用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public final class OpenFunctional {

    private OpenFunctional() {
        // Entry point class
    }

    // ==================== Try Monad | Try 单子 ====================

    /**
     * Create a Try from a supplier
     * 从供应商创建 Try
     *
     * @param supplier computation that may throw - 可能抛出的计算
     * @param <T>      result type - 结果类型
     * @return Try containing result or exception
     */
    public static <T> Try<T> tryOf(cloud.opencode.base.core.func.CheckedSupplier<T> supplier) {
        return Try.of(supplier);
    }

    /**
     * Create a successful Try
     * 创建成功的 Try
     *
     * @param value the value - 值
     * @param <T>   result type - 结果类型
     * @return successful Try
     */
    public static <T> Try<T> success(T value) {
        return Try.success(value);
    }

    /**
     * Create a failed Try
     * 创建失败的 Try
     *
     * @param throwable the exception - 异常
     * @param <T>       result type - 结果类型
     * @return failed Try
     */
    public static <T> Try<T> failure(Throwable throwable) {
        return Try.failure(throwable);
    }

    // ==================== Either | Either 类型 ====================

    /**
     * Create a Left Either
     * 创建 Left Either
     *
     * @param value the left value - 左值
     * @param <L>   left type - 左类型
     * @param <R>   right type - 右类型
     * @return Left Either
     */
    public static <L, R> Either<L, R> left(L value) {
        return Either.left(value);
    }

    /**
     * Create a Right Either
     * 创建 Right Either
     *
     * @param value the right value - 右值
     * @param <L>   left type - 左类型
     * @param <R>   right type - 右类型
     * @return Right Either
     */
    public static <L, R> Either<L, R> right(R value) {
        return Either.right(value);
    }

    // ==================== Option | Option 类型 ====================

    /**
     * Create a Some Option
     * 创建 Some Option
     *
     * @param value the value (non-null) - 值（非空）
     * @param <T>   value type - 值类型
     * @return Some Option
     */
    public static <T> Option<T> some(T value) {
        return Option.some(value);
    }

    /**
     * Create a None Option
     * 创建 None Option
     *
     * @param <T> value type - 值类型
     * @return None Option
     */
    public static <T> Option<T> none() {
        return Option.none();
    }

    /**
     * Create an Option from nullable value
     * 从可空值创建 Option
     *
     * @param value nullable value - 可空值
     * @param <T>   value type - 值类型
     * @return Some if non-null, None otherwise
     */
    public static <T> Option<T> option(T value) {
        return Option.of(value);
    }

    // ==================== Validation | Validation 类型 ====================

    /**
     * Create a valid Validation
     * 创建有效的 Validation
     *
     * @param value the value - 值
     * @param <E>   error type - 错误类型
     * @param <T>   value type - 值类型
     * @return valid Validation
     */
    public static <E, T> Validation<E, T> valid(T value) {
        return Validation.valid(value);
    }

    /**
     * Create an invalid Validation
     * 创建无效的 Validation
     *
     * @param error the error - 错误
     * @param <E>   error type - 错误类型
     * @param <T>   value type - 值类型
     * @return invalid Validation
     */
    public static <E, T> Validation<E, T> invalid(E error) {
        return Validation.invalid(error);
    }

    // ==================== Lazy | Lazy 类型 ====================

    /**
     * Create a Lazy computation
     * 创建惰性计算
     *
     * @param supplier deferred computation - 延迟的计算
     * @param <T>      result type - 结果类型
     * @return Lazy container
     */
    public static <T> Lazy<T> lazy(Supplier<T> supplier) {
        return Lazy.of(supplier);
    }

    // ==================== Pattern Matching | 模式匹配 ====================

    /**
     * Start pattern matching
     * 开始模式匹配
     *
     * @param value value to match - 要匹配的值
     * @param <T>   value type - 值类型
     * @return Matcher for fluent API
     */
    public static <T> OpenMatch.Matcher<T> match(T value) {
        return OpenMatch.of(value);
    }

    /**
     * Create a type pattern
     * 创建类型模式
     *
     * @param type type to match - 要匹配的类型
     * @param <T>  input type - 输入类型
     * @param <R>  matched type - 匹配的类型
     * @return type pattern
     */
    public static <T, R> Pattern<T, R> typePattern(Class<R> type) {
        return Pattern.type(type);
    }

    /**
     * Create a type case
     * 创建类型分支
     *
     * @param type   type to match - 要匹配的类型
     * @param action action on match - 匹配时的动作
     * @param <T>    input type - 输入类型
     * @param <U>    matched type - 匹配的类型
     * @param <R>    result type - 结果类型
     * @return type case
     */
    public static <T, U, R> Case<T, R> caseOf(Class<U> type, Function<? super U, ? extends R> action) {
        return Case.type(type, action);
    }

    /**
     * Create a predicate case
     * 创建谓词分支
     *
     * @param predicate condition - 条件
     * @param action    action on match - 匹配时的动作
     * @param <T>       input type - 输入类型
     * @param <R>       result type - 结果类型
     * @return predicate case
     */
    public static <T, R> Case<T, R> when(Predicate<? super T> predicate,
                                          Function<? super T, ? extends R> action) {
        return Case.when(predicate, action);
    }

    // ==================== Function Utilities | 函数工具 ====================

    /**
     * Compose two functions
     * 组合两个函数
     *
     * @param f first function - 第一个函数
     * @param g second function - 第二个函数
     * @param <A> input type - 输入类型
     * @param <B> intermediate type - 中间类型
     * @param <C> output type - 输出类型
     * @return composed function
     */
    public static <A, B, C> Function<A, C> compose(Function<A, B> f, Function<B, C> g) {
        return FunctionUtil.compose(f, g);
    }

    /**
     * Curry a BiFunction
     * 柯里化 BiFunction
     *
     * @param f the function - 函数
     * @param <A> first argument type - 第一个参数类型
     * @param <B> second argument type - 第二个参数类型
     * @param <C> result type - 结果类型
     * @return curried function
     */
    public static <A, B, C> Function<A, Function<B, C>> curry(BiFunction<A, B, C> f) {
        return FunctionUtil.curry(f);
    }

    /**
     * Memoize a function
     * 记忆化函数
     *
     * @param f the function - 函数
     * @param <T> input type - 输入类型
     * @param <R> result type - 结果类型
     * @return memoized function
     */
    public static <T, R> Function<T, R> memoize(Function<T, R> f) {
        return FunctionUtil.memoize(f);
    }

    /**
     * Memoize a function with custom cache size
     * 记忆化函数（自定义缓存大小）
     *
     * @param f       the function - 函数
     * @param maxSize maximum cache size (LRU eviction) - 最大缓存大小（LRU 驱逐）
     * @param <T>     input type - 输入类型
     * @param <R>     result type - 结果类型
     * @return memoized function with bounded cache
     */
    public static <T, R> Function<T, R> memoize(Function<T, R> f, int maxSize) {
        return FunctionUtil.memoize(f, maxSize);
    }

    // ==================== Checked Functions | 可抛异常函数 ====================

    /**
     * Create a CheckedBiFunction
     * 创建可抛异常的双参函数
     *
     * <p>Allows using lambdas that throw checked exceptions with BiFunction-like operations.</p>
     * <p>允许在 BiFunction 类操作中使用抛出受检异常的 lambda。</p>
     *
     * @param f   the checked function - 可抛异常函数
     * @param <T> first input type - 第一个输入类型
     * @param <U> second input type - 第二个输入类型
     * @param <R> result type - 结果类型
     * @return CheckedBiFunction
     */
    public static <T, U, R> CheckedBiFunction<T, U, R> checkedBiFunction(
            CheckedBiFunction<T, U, R> f) {
        return f;
    }

    /**
     * Create a CheckedBiConsumer
     * 创建可抛异常的双参消费者
     *
     * <p>Allows using lambdas that throw checked exceptions with BiConsumer-like operations.</p>
     * <p>允许在 BiConsumer 类操作中使用抛出受检异常的 lambda。</p>
     *
     * @param c   the checked consumer - 可抛异常消费者
     * @param <T> first input type - 第一个输入类型
     * @param <U> second input type - 第二个输入类型
     * @return CheckedBiConsumer
     */
    public static <T, U> CheckedBiConsumer<T, U> checkedBiConsumer(
            CheckedBiConsumer<T, U> c) {
        return c;
    }

    /**
     * Convert a CheckedBiFunction to a standard BiFunction
     * 将 CheckedBiFunction 转换为标准 BiFunction
     *
     * <p>Checked exceptions are wrapped in RuntimeException.</p>
     * <p>受检异常被包装为 RuntimeException。</p>
     *
     * @param f   the checked function - 可抛异常函数
     * @param <T> first input type - 第一个输入类型
     * @param <U> second input type - 第二个输入类型
     * @param <R> result type - 结果类型
     * @return standard BiFunction
     */
    public static <T, U, R> BiFunction<T, U, R> uncheckedBiFunction(
            CheckedBiFunction<T, U, R> f) {
        return f.unchecked();
    }

    // ==================== Lens | 透镜 ====================

    /**
     * Create a lens
     * 创建透镜
     *
     * @param getter getter function - getter 函数
     * @param setter setter function - setter 函数
     * @param <S>    source type - 源类型
     * @param <A>    target type - 目标类型
     * @return lens
     */
    public static <S, A> Lens<S, A> lens(Function<S, A> getter, BiFunction<S, A, S> setter) {
        return Lens.of(getter, setter);
    }

    /**
     * Create an optional lens
     * 创建可选透镜
     *
     * @param getter optional getter - 可选 getter
     * @param setter setter function - setter 函数
     * @param <S>    source type - 源类型
     * @param <A>    target type - 目标类型
     * @return optional lens
     */
    public static <S, A> OptionalLens<S, A> optionalLens(
            Function<S, java.util.Optional<A>> getter,
            BiFunction<S, A, S> setter) {
        return OptionalLens.of(getter, setter);
    }

    // ==================== Pipeline | 管道 ====================

    /**
     * Start a pipeline with a value
     * 以值开始管道
     *
     * @param value starting value - 起始值
     * @param <T>   value type - 值类型
     * @return pipeline builder
     */
    public static <T> Pipeline.PipelineBuilder<T> pipeline(T value) {
        return Pipeline.of(value);
    }

    /**
     * Start a pipe chain
     * 开始管道链
     *
     * @param value starting value - 起始值
     * @param <T>   value type - 值类型
     * @return pipe holder
     */
    public static <T> PipeUtil.Pipe<T> pipe(T value) {
        return PipeUtil.pipe(value);
    }

    // ==================== Async | 异步 ====================

    /**
     * Run async on Virtual Thread
     * 在虚拟线程上异步运行
     *
     * @param supplier computation - 计算
     * @param <T>      result type - 结果类型
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<T> async(Supplier<T> supplier) {
        return AsyncFunctionUtil.async(supplier);
    }

    /**
     * Run async with timeout
     * 带超时异步运行
     *
     * @param supplier computation - 计算
     * @param timeout  timeout duration - 超时时长
     * @param <T>      result type - 结果类型
     * @return Try containing result
     */
    public static <T> Try<T> asyncTimeout(Supplier<T> supplier, Duration timeout) {
        return AsyncFunctionUtil.asyncWithTimeout(supplier, timeout);
    }

    /**
     * Create a lazy async computation
     * 创建惰性异步计算
     *
     * @param supplier computation - 计算
     * @param <T>      result type - 结果类型
     * @return LazyAsync container
     */
    public static <T> LazyAsync<T> lazyAsync(Supplier<T> supplier) {
        return LazyAsync.of(supplier);
    }

    /**
     * Parallel map over a list
     * 并行映射列表
     *
     * @param items  items to process - 要处理的项目
     * @param mapper function to apply - 要应用的函数
     * @param <T>    input type - 输入类型
     * @param <R>    result type - 结果类型
     * @return list of results
     */
    public static <T, R> List<R> parallelMap(List<T> items, Function<T, R> mapper) {
        return AsyncFunctionUtil.parallelMap(items, mapper);
    }

    // ==================== Record | Record 工具 ====================

    /**
     * Create a lens for a record component
     * 为 record 组件创建透镜
     *
     * @param recordClass   record class - record 类
     * @param componentName component name - 组件名称
     * @param <R>           record type - record 类型
     * @param <T>           component type - 组件类型
     * @return lens for component
     */
    public static <R extends Record, T> Lens<R, T> recordLens(Class<R> recordClass,
                                                               String componentName) {
        return RecordUtil.lens(recordClass, componentName);
    }

    /**
     * Copy a record with modifications
     * 带修改复制 record
     *
     * @param record        the record - record
     * @param modifications modifications map - 修改映射
     * @param <R>           record type - record 类型
     * @return copied record
     */
    public static <R extends Record> R copyRecord(R record,
                                                   java.util.Map<String, Object> modifications) {
        return RecordUtil.copy(record, modifications);
    }

    /**
     * Convert record to map
     * 将 record 转换为 map
     *
     * @param record the record - record
     * @return map of components
     */
    public static java.util.Map<String, Object> recordToMap(Record record) {
        return RecordUtil.toMap(record);
    }
}
