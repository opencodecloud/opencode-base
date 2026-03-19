package cloud.opencode.base.functional.pipeline;

import cloud.opencode.base.functional.exception.OpenFunctionalException;
import cloud.opencode.base.functional.monad.Try;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.*;

/**
 * Pipeline - Composable data transformation pipeline
 * Pipeline - 可组合的数据转换管道
 *
 * <p>A fluent API for building transformation pipelines that process data
 * through a series of stages. Supports filtering, mapping, flat-mapping,
 * and error handling.</p>
 * <p>用于构建转换管道的流式 API，通过一系列阶段处理数据。支持过滤、映射、
 * 扁平映射和错误处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent chainable operations - 流式可链接操作</li>
 *   <li>Type-safe transformations - 类型安全转换</li>
 *   <li>Error handling integration - 错误处理集成</li>
 *   <li>Lazy evaluation option - 惰性求值选项</li>
 *   <li>Collection processing - 集合处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple transformation pipeline
 * String result = Pipeline.of("  hello world  ")
 *     .map(String::trim)
 *     .map(String::toUpperCase)
 *     .filter(s -> s.length() > 0)
 *     .execute();
 *
 * // Pipeline with error handling
 * Try<Integer> parsed = Pipeline.of(userInput)
 *     .map(String::trim)
 *     .filter(s -> !s.isEmpty())
 *     .mapTry(Integer::parseInt)
 *     .executeTry();
 *
 * // Collection pipeline
 * List<String> names = Pipeline.ofCollection(users)
 *     .filter(User::isActive)
 *     .map(User::getName)
 *     .map(String::toUpperCase)
 *     .toList();
 *
 * // Pipeline composition
 * Pipeline<String, Integer> parseAndValidate = Pipeline.<String>identity()
 *     .map(String::trim)
 *     .mapTry(Integer::parseInt);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Each stage is O(1) to add - 每个阶段添加为 O(1)</li>
 *   <li>Execution is O(n) for n stages - 执行为 O(n) n 个阶段</li>
 *   <li>Memory: proportional to pipeline length - 内存: 与管道长度成正比</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not designed for concurrent modification) - 线程安全: 否</li>
 *   <li>Null-safe: Configurable null handling - 空值安全: 可配置空值处理</li>
 * </ul>
 *
 * @param <T> input type - 输入类型
 * @param <R> output type - 输出类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public final class Pipeline<T, R> {

    private final Function<T, R> function;

    private Pipeline(Function<T, R> function) {
        this.function = function;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a pipeline starting with a value
     * 创建以值开始的管道
     *
     * @param value starting value - 起始值
     * @param <T>   value type - 值类型
     * @return pipeline builder
     */
    public static <T> PipelineBuilder<T> of(T value) {
        return new PipelineBuilder<>(value);
    }

    /**
     * Create an identity pipeline
     * 创建恒等管道
     *
     * @param <T> type - 类型
     * @return identity pipeline
     */
    public static <T> Pipeline<T, T> identity() {
        return new Pipeline<>(Function.identity());
    }

    /**
     * Create a pipeline from a function
     * 从函数创建管道
     *
     * @param function the function - 函数
     * @param <T>      input type - 输入类型
     * @param <R>      output type - 输出类型
     * @return pipeline wrapping the function
     */
    public static <T, R> Pipeline<T, R> from(Function<T, R> function) {
        return new Pipeline<>(function);
    }

    /**
     * Create a collection pipeline
     * 创建集合管道
     *
     * @param collection the collection - 集合
     * @param <T>        element type - 元素类型
     * @return collection pipeline builder
     */
    public static <T> CollectionPipeline<T, T> ofCollection(Collection<T> collection) {
        return new CollectionPipeline<>(collection, Function.identity());
    }

    // ==================== Pipeline Operations | 管道操作 ====================

    /**
     * Apply the pipeline to a value
     * 将管道应用于值
     *
     * @param input the input value - 输入值
     * @return transformed value
     */
    public R apply(T input) {
        return function.apply(input);
    }

    /**
     * Apply the pipeline safely, returning Try
     * 安全地应用管道，返回 Try
     *
     * @param input the input value - 输入值
     * @return Try containing result or exception
     */
    public Try<R> applyTry(T input) {
        return Try.of(() -> function.apply(input));
    }

    /**
     * Chain another transformation
     * 链接另一个转换
     *
     * @param mapper transformation function - 转换函数
     * @param <U>    result type - 结果类型
     * @return extended pipeline
     */
    public <U> Pipeline<T, U> andThen(Function<? super R, ? extends U> mapper) {
        return new Pipeline<>(function.andThen(mapper));
    }

    /**
     * Compose with another pipeline
     * 与另一个管道组合
     *
     * @param before pipeline to apply first - 先应用的管道
     * @param <V>    input type - 输入类型
     * @return composed pipeline
     */
    public <V> Pipeline<V, R> compose(Pipeline<V, T> before) {
        return new Pipeline<>(v -> function.apply(before.apply(v)));
    }

    /**
     * Get the underlying function
     * 获取底层函数
     *
     * @return the function - 函数
     */
    public Function<T, R> toFunction() {
        return function;
    }

    // ==================== Pipeline Builder | 管道构建器 ====================

    /**
     * PipelineBuilder - Fluent builder for single-value pipelines
     * PipelineBuilder - 单值管道的流式构建器
     *
     * @param <T> current value type - 当前值类型
     */
    public static final class PipelineBuilder<T> {
        private final T value;
        private boolean filtered = false;

        PipelineBuilder(T value) {
            this.value = value;
        }

        /**
         * Transform the value
         * 转换值
         *
         * @param mapper transformation function - 转换函数
         * @param <R>    result type - 结果类型
         * @return builder with transformed value
         */
        public <R> PipelineBuilder<R> map(Function<? super T, ? extends R> mapper) {
            if (filtered || value == null) {
                return new PipelineBuilder<>(null);
            }
            return new PipelineBuilder<>(mapper.apply(value));
        }

        /**
         * Transform with a function that may fail
         * 使用可能失败的函数转换
         *
         * @param mapper transformation function - 转换函数
         * @param <R>    result type - 结果类型
         * @return builder wrapping Try result
         */
        public <R> TryPipelineBuilder<R> mapTry(Function<? super T, ? extends R> mapper) {
            if (filtered || value == null) {
                return new TryPipelineBuilder<>(Try.failure(new NullPointerException("Pipeline value is null")));
            }
            return new TryPipelineBuilder<>(Try.of(() -> mapper.apply(value)));
        }

        /**
         * Flat-map the value
         * 扁平映射值
         *
         * @param mapper function returning PipelineBuilder - 返回 PipelineBuilder 的函数
         * @param <R>    result type - 结果类型
         * @return flattened builder
         */
        public <R> PipelineBuilder<R> flatMap(Function<? super T, PipelineBuilder<R>> mapper) {
            if (filtered || value == null) {
                return new PipelineBuilder<>(null);
            }
            return mapper.apply(value);
        }

        /**
         * Filter the value
         * 过滤值
         *
         * @param predicate filter condition - 过滤条件
         * @return builder (value becomes null if filtered)
         */
        public PipelineBuilder<T> filter(Predicate<? super T> predicate) {
            if (filtered || value == null || !predicate.test(value)) {
                PipelineBuilder<T> builder = new PipelineBuilder<>(null);
                builder.filtered = true;
                return builder;
            }
            return this;
        }

        /**
         * Peek at the current value
         * 查看当前值
         *
         * @param consumer action to perform - 要执行的动作
         * @return this builder
         */
        public PipelineBuilder<T> peek(Consumer<? super T> consumer) {
            if (!filtered && value != null) {
                consumer.accept(value);
            }
            return this;
        }

        /**
         * Execute the pipeline and get the result
         * 执行管道并获取结果
         *
         * @return the result (may be null)
         */
        public T execute() {
            return value;
        }

        /**
         * Execute and wrap result in Optional
         * 执行并将结果包装为 Optional
         *
         * @return Optional containing result
         */
        public Optional<T> executeOptional() {
            return Optional.ofNullable(value);
        }

        /**
         * Execute or return default if null/filtered
         * 执行或返回默认值（如果为 null/已过滤）
         *
         * @param defaultValue default value - 默认值
         * @return result or default
         */
        public T executeOrElse(T defaultValue) {
            return value != null ? value : defaultValue;
        }

        /**
         * Execute or compute default if null/filtered
         * 执行或计算默认值（如果为 null/已过滤）
         *
         * @param supplier default value supplier - 默认值供应商
         * @return result or computed default
         */
        public T executeOrElseGet(Supplier<? extends T> supplier) {
            return value != null ? value : supplier.get();
        }
    }

    // ==================== Try Pipeline Builder | Try 管道构建器 ====================

    /**
     * TryPipelineBuilder - Builder for pipelines that may fail
     * TryPipelineBuilder - 可能失败的管道构建器
     *
     * @param <T> value type - 值类型
     */
    public static final class TryPipelineBuilder<T> {
        private final Try<T> value;

        TryPipelineBuilder(Try<T> value) {
            this.value = value;
        }

        /**
         * Transform the value if successful
         * 如果成功则转换值
         *
         * @param mapper transformation function - 转换函数
         * @param <R>    result type - 结果类型
         * @return builder with transformed Try
         */
        public <R> TryPipelineBuilder<R> map(Function<? super T, ? extends R> mapper) {
            return new TryPipelineBuilder<>(value.map(mapper));
        }

        /**
         * Transform with another Try-returning function
         * 使用另一个返回 Try 的函数转换
         *
         * @param mapper function returning Try - 返回 Try 的函数
         * @param <R>    result type - 结果类型
         * @return builder with flat-mapped Try
         */
        public <R> TryPipelineBuilder<R> flatMap(Function<? super T, Try<R>> mapper) {
            return new TryPipelineBuilder<>(value.flatMap(mapper));
        }

        /**
         * Filter with a predicate
         * 使用谓词过滤
         *
         * @param predicate filter condition - 过滤条件
         * @return filtered builder
         */
        public TryPipelineBuilder<T> filter(Predicate<? super T> predicate) {
            return new TryPipelineBuilder<>(value.filter(predicate));
        }

        /**
         * Recover from failure
         * 从失败恢复
         *
         * @param recovery recovery function - 恢复函数
         * @return builder with recovery
         */
        public TryPipelineBuilder<T> recover(Function<Throwable, T> recovery) {
            return new TryPipelineBuilder<>(value.recover(recovery));
        }

        /**
         * Execute and get the Try result
         * 执行并获取 Try 结果
         *
         * @return the Try result
         */
        public Try<T> executeTry() {
            return value;
        }

        /**
         * Execute and get the value, throwing on failure
         * 执行并获取值，失败时抛出异常
         *
         * @return the value
         * @throws RuntimeException if failed
         */
        public T execute() {
            if (value.isSuccess()) {
                return value.get();
            }
            Throwable cause = value.getCause().orElse(null);
            throw new OpenFunctionalException("Pipeline execution failed", cause);
        }

        /**
         * Execute or return default on failure
         * 执行或在失败时返回默认值
         *
         * @param defaultValue default value - 默认值
         * @return result or default
         */
        public T executeOrElse(T defaultValue) {
            return value.getOrElse(defaultValue);
        }
    }

    // ==================== Collection Pipeline | 集合管道 ====================

    /**
     * CollectionPipeline - Pipeline for processing collections
     * CollectionPipeline - 用于处理集合的管道
     *
     * @param <T> input element type - 输入元素类型
     * @param <R> output element type - 输出元素类型
     */
    public static final class CollectionPipeline<T, R> {
        private final Collection<T> source;
        private final Function<T, R> transformer;
        private final Predicate<T> filter;

        CollectionPipeline(Collection<T> source, Function<T, R> transformer) {
            this(source, transformer, t -> true);
        }

        private CollectionPipeline(Collection<T> source, Function<T, R> transformer, Predicate<T> filter) {
            this.source = source;
            this.transformer = transformer;
            this.filter = filter;
        }

        /**
         * Transform each element
         * 转换每个元素
         *
         * @param mapper transformation function - 转换函数
         * @param <U>    result type - 结果类型
         * @return pipeline with transformation
         */
        @SuppressWarnings("unchecked")
        public <U> CollectionPipeline<T, U> map(Function<? super R, ? extends U> mapper) {
            Function<T, U> newTransformer = transformer.andThen(r -> mapper.apply(r));
            return new CollectionPipeline<>(source, newTransformer, filter);
        }

        /**
         * Filter elements
         * 过滤元素
         *
         * @param predicate filter condition - 过滤条件
         * @return filtered pipeline
         */
        @SuppressWarnings("unchecked")
        public CollectionPipeline<T, R> filter(Predicate<? super T> predicate) {
            Predicate<T> newFilter = filter.and((Predicate<T>) predicate);
            return new CollectionPipeline<>(source, transformer, newFilter);
        }

        /**
         * Execute and collect to List
         * 执行并收集为 List
         *
         * @return list of results
         */
        public List<R> toList() {
            return source.stream()
                    .filter(filter)
                    .map(transformer)
                    .toList();
        }

        /**
         * Execute and perform action on each
         * 执行并对每个元素执行动作
         *
         * @param action action to perform - 要执行的动作
         */
        public void forEach(Consumer<? super R> action) {
            source.stream()
                    .filter(filter)
                    .map(transformer)
                    .forEach(action);
        }

        /**
         * Execute and count results
         * 执行并计数结果
         *
         * @return count of elements
         */
        public long count() {
            return source.stream()
                    .filter(filter)
                    .count();
        }

        /**
         * Find first matching element
         * 查找第一个匹配元素
         *
         * @return Optional containing first element
         */
        public Optional<R> findFirst() {
            return source.stream()
                    .filter(filter)
                    .map(transformer)
                    .findFirst();
        }

        /**
         * Check if any element matches
         * 检查是否有任何元素匹配
         *
         * @param predicate match condition - 匹配条件
         * @return true if any match
         */
        public boolean anyMatch(Predicate<? super R> predicate) {
            return source.stream()
                    .filter(filter)
                    .map(transformer)
                    .anyMatch(predicate);
        }

        /**
         * Check if all elements match
         * 检查是否所有元素都匹配
         *
         * @param predicate match condition - 匹配条件
         * @return true if all match
         */
        public boolean allMatch(Predicate<? super R> predicate) {
            return source.stream()
                    .filter(filter)
                    .map(transformer)
                    .allMatch(predicate);
        }

        /**
         * Reduce elements to single value
         * 将元素规约为单个值
         *
         * @param identity    identity value - 恒等值
         * @param accumulator accumulator function - 累加器函数
         * @return reduced value
         */
        public R reduce(R identity, BinaryOperator<R> accumulator) {
            return source.stream()
                    .filter(filter)
                    .map(transformer)
                    .reduce(identity, accumulator);
        }
    }
}
