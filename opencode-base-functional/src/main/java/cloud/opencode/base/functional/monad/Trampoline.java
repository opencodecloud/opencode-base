/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.functional.monad;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Trampoline - Stack-safe recursion using trampolining
 * Trampoline - 使用蹦床模式的栈安全递归
 *
 * <p>Converts recursive computations into iterative ones, preventing stack overflow.
 * Instead of actual recursive calls, computations return either a final result
 * or a continuation to be executed in the next iteration.</p>
 * <p>将递归计算转换为迭代计算，防止栈溢出。计算不进行实际的递归调用，
 * 而是返回最终结果或在下一次迭代中执行的延续。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Stack-safe recursion - 栈安全的递归</li>
 *   <li>Constant stack space - 常量栈空间</li>
 *   <li>Monadic operations (map, flatMap) - Monad 操作</li>
 *   <li>Mutual recursion support - 支持相互递归</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Factorial (would overflow with normal recursion for large n)
 * Trampoline<Long> factorial(long n, long acc) {
 *     if (n <= 1) {
 *         return Trampoline.done(acc);
 *     }
 *     return Trampoline.more(() -> factorial(n - 1, n * acc));
 * }
 * long result = factorial(10000, 1).get(); // No stack overflow!
 *
 * // Fibonacci (with accumulator pattern)
 * Trampoline<Long> fibonacci(int n, long a, long b) {
 *     if (n == 0) return Trampoline.done(a);
 *     if (n == 1) return Trampoline.done(b);
 *     return Trampoline.more(() -> fibonacci(n - 1, b, a + b));
 * }
 *
 * // Using map for transformations
 * Trampoline<String> result = factorial(100, 1)
 *     .map(Object::toString);
 *
 * // Using flatMap for dependent computations
 * Trampoline<Integer> sum = Trampoline.done(10)
 *     .flatMap(x -> Trampoline.done(x + 5));
 *
 * // Mutual recursion example (even/odd)
 * Trampoline<Boolean> isEven(int n) {
 *     if (n == 0) return Trampoline.done(true);
 *     return Trampoline.more(() -> isOdd(n - 1));
 * }
 * Trampoline<Boolean> isOdd(int n) {
 *     if (n == 0) return Trampoline.done(false);
 *     return Trampoline.more(() -> isEven(n - 1));
 * }
 * boolean even = isEven(1000000).get(); // Stack-safe!
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time: O(n) iterations for n recursive calls - 时间: n 次递归调用需要 O(n) 次迭代</li>
 *   <li>Stack: O(1) - uses heap instead - 栈: O(1) - 使用堆代替</li>
 *   <li>Heap: O(n) Trampoline objects - 堆: O(n) 个 Trampoline 对象</li>
 * </ul>
 *
 * <p><strong>When to Use | 何时使用:</strong></p>
 * <ul>
 *   <li>Deep recursive algorithms (thousands+ levels) - 深度递归算法（数千层以上）</li>
 *   <li>Tail-recursive functions - 尾递归函数</li>
 *   <li>Mutual recursion - 相互递归</li>
 *   <li>When stack size cannot be increased - 当无法增加栈大小时</li>
 * </ul>
 *
 * @param <T> result type - 结果类型
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public sealed interface Trampoline<T> permits Trampoline.Done, Trampoline.More, Trampoline.FlatMap {

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a completed Trampoline with a result.
     * 创建包含结果的已完成 Trampoline。
     *
     * <p>Use this to return the final result of the computation.</p>
     * <p>使用此方法返回计算的最终结果。</p>
     *
     * @param result the result value - 结果值
     * @param <T>    result type - 结果类型
     * @return completed Trampoline
     */
    static <T> Trampoline<T> done(T result) {
        return new Done<>(result);
    }

    /**
     * Create a suspended Trampoline with a continuation.
     * 创建包含延续的挂起 Trampoline。
     *
     * <p>Use this to defer the next recursive call.</p>
     * <p>使用此方法推迟下一次递归调用。</p>
     *
     * @param continuation supplier of the next Trampoline - 下一个 Trampoline 的供应商
     * @param <T>          result type - 结果类型
     * @return suspended Trampoline
     */
    static <T> Trampoline<T> more(Supplier<Trampoline<T>> continuation) {
        return new More<>(continuation);
    }

    /**
     * Create a Trampoline that suspends computation.
     * 创建挂起计算的 Trampoline。
     *
     * <p>Alias for {@link #more} for readability.</p>
     * <p>{@link #more} 的别名，增加可读性。</p>
     *
     * @param continuation supplier of the next Trampoline - 下一个 Trampoline 的供应商
     * @param <T>          result type - 结果类型
     * @return suspended Trampoline
     */
    static <T> Trampoline<T> suspend(Supplier<Trampoline<T>> continuation) {
        return more(continuation);
    }

    /**
     * Lift a supplier into a Trampoline.
     * 将供应商提升为 Trampoline。
     *
     * @param supplier value supplier - 值供应商
     * @param <T>      result type - 结果类型
     * @return Trampoline containing the computed value
     */
    static <T> Trampoline<T> delay(Supplier<T> supplier) {
        return more(() -> done(supplier.get()));
    }

    // ==================== Core Methods | 核心方法 ====================

    /**
     * Execute the trampoline and get the result.
     * 执行蹦床并获取结果。
     *
     * <p>This method iteratively executes continuations until a final result
     * is reached, using constant stack space.</p>
     * <p>此方法迭代执行延续直到得到最终结果，使用常量栈空间。</p>
     *
     * @return the computed result - 计算结果
     */
    default T get() {
        return run();
    }

    /**
     * Execute the trampoline and get the result.
     * 执行蹦床并获取结果。
     *
     * <p>Alias for {@link #get()}.</p>
     * <p>{@link #get()} 的别名。</p>
     *
     * @return the computed result - 计算结果
     */
    default T run() {
        Trampoline<T> current = this;

        while (true) {
            switch (current) {
                case Done<T> done -> {
                    return done.result;
                }
                case More<T> more -> {
                    current = more.continuation.get();
                }
                case FlatMap<?, T> flatMap -> {
                    current = flatMap.step();
                }
            }
        }
    }

    /**
     * Check if the Trampoline is complete.
     * 检查 Trampoline 是否完成。
     *
     * @return true if done - 如果完成返回 true
     */
    boolean isDone();

    // ==================== Transformations | 转换 ====================

    /**
     * Transform the result when it becomes available.
     * 结果可用时转换它。
     *
     * <p>The mapper is applied lazily when get() is called.</p>
     * <p>映射函数在调用 get() 时惰性应用。</p>
     *
     * @param mapper transformation function - 转换函数
     * @param <U>    result type - 结果类型
     * @return transformed Trampoline
     */
    default <U> Trampoline<U> map(Function<? super T, ? extends U> mapper) {
        return flatMap(t -> done(mapper.apply(t)));
    }

    /**
     * Chain another Trampoline computation.
     * 链接另一个 Trampoline 计算。
     *
     * <p>Enables composition of multiple trampolined computations.</p>
     * <p>启用多个蹦床计算的组合。</p>
     *
     * @param f function producing the next Trampoline - 产生下一个 Trampoline 的函数
     * @param <U> result type - 结果类型
     * @return chained Trampoline
     */
    <U> Trampoline<U> flatMap(Function<? super T, Trampoline<U>> f);

    /**
     * Execute a side effect when the result is available.
     * 结果可用时执行副作用。
     *
     * @param action the action to perform - 要执行的动作
     * @return this Trampoline for chaining
     */
    default Trampoline<T> peek(java.util.function.Consumer<? super T> action) {
        return map(t -> {
            action.accept(t);
            return t;
        });
    }

    // ==================== Conversion | 转换 ====================

    /**
     * Convert to a Lazy that executes the trampoline.
     * 转换为执行蹦床的 Lazy。
     *
     * @return Lazy containing the result
     */
    default Lazy<T> toLazy() {
        return Lazy.of(this::get);
    }

    /**
     * Convert to a Try that captures exceptions.
     * 转换为捕获异常的 Try。
     *
     * @return Try containing the result or exception
     */
    default Try<T> toTry() {
        return Try.of(this::get);
    }

    // ==================== Implementation Classes | 实现类 ====================

    /**
     * Completed Trampoline with a result.
     * 包含结果的已完成 Trampoline。
     *
     * @param result the computed result - 计算结果
     * @param <T>    result type - 结果类型
     */
    record Done<T>(T result) implements Trampoline<T> {

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public <U> Trampoline<U> flatMap(Function<? super T, Trampoline<U>> f) {
            return new FlatMap<>(this, f);
        }

        @Override
        public String toString() {
            return "Done[" + result + "]";
        }
    }

    /**
     * Suspended Trampoline with a continuation.
     * 包含延续的挂起 Trampoline。
     *
     * @param continuation supplier of the next computation - 下一个计算的供应商
     * @param <T>          result type - 结果类型
     */
    record More<T>(Supplier<Trampoline<T>> continuation) implements Trampoline<T> {

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public <U> Trampoline<U> flatMap(Function<? super T, Trampoline<U>> f) {
            return new FlatMap<>(this, f);
        }

        @Override
        public String toString() {
            return "More[...]";
        }
    }

    /**
     * FlatMapped Trampoline for chaining computations.
     * 用于链接计算的 FlatMapped Trampoline。
     *
     * @param previous the previous computation - 之前的计算
     * @param f        the transformation function - 转换函数
     * @param <A>      input type - 输入类型
     * @param <B>      output type - 输出类型
     */
    record FlatMap<A, B>(Trampoline<A> previous, Function<? super A, Trampoline<B>> f) implements Trampoline<B> {

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public <U> Trampoline<U> flatMap(Function<? super B, Trampoline<U>> g) {
            return new FlatMap<>(previous, a -> new FlatMap<>(f.apply(a), g));
        }

        /**
         * Execute one step of the flatMap chain.
         * 执行 flatMap 链的一步。
         *
         * @return the next Trampoline in the chain
         */
        Trampoline<B> step() {
            return switch (previous) {
                case Done<A> done -> f.apply(done.result);
                case More<A> more -> new FlatMap<>(more.continuation.get(), f);
                case FlatMap<?, ?> nested -> rebalance(nested);
            };
        }

        /**
         * Rebalance nested FlatMaps to maintain right-association.
         * 重新平衡嵌套的 FlatMaps 以保持右结合性。
         */
        @SuppressWarnings("unchecked")
        private <C> Trampoline<B> rebalance(FlatMap<?, ?> nested) {
            FlatMap<C, A> typedNested = (FlatMap<C, A>) nested;
            return new FlatMap<>(typedNested.previous, c -> new FlatMap<>(typedNested.f.apply(c), f));
        }

        @Override
        public String toString() {
            return "FlatMap[" + previous + " -> ...]";
        }
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Create a Trampoline for a recursive function.
     * 为递归函数创建 Trampoline。
     *
     * <p>Helper method for converting recursive functions to trampolined versions.</p>
     * <p>用于将递归函数转换为蹦床版本的辅助方法。</p>
     *
     * @param value     initial value - 初始值
     * @param predicate termination condition - 终止条件
     * @param next      function to compute next value - 计算下一个值的函数
     * @param <T>       value type - 值类型
     * @return Trampoline that iterates until predicate is satisfied
     */
    static <T> Trampoline<T> iterate(
            T value,
            java.util.function.Predicate<T> predicate,
            Function<T, T> next) {

        if (predicate.test(value)) {
            return done(value);
        }
        return more(() -> iterate(next.apply(value), predicate, next));
    }

    /**
     * Create a Trampoline that repeats a computation n times.
     * 创建重复计算 n 次的 Trampoline。
     *
     * @param n       number of times to repeat - 重复次数
     * @param initial initial value - 初始值
     * @param step    step function - 步进函数
     * @param <T>     value type - 值类型
     * @return Trampoline with final result
     */
    static <T> Trampoline<T> repeat(int n, T initial, Function<T, T> step) {
        return repeatHelper(n, initial, step);
    }

    private static <T> Trampoline<T> repeatHelper(int n, T value, Function<T, T> step) {
        if (n <= 0) {
            return done(value);
        }
        return more(() -> repeatHelper(n - 1, step.apply(value), step));
    }

    /**
     * Sequence multiple Trampolines, returning the last result.
     * 顺序执行多个 Trampoline，返回最后一个结果。
     *
     * @param trampolines the trampolines to sequence - 要顺序执行的蹦床
     * @param <T>         result type - 结果类型
     * @return Trampoline with the last result
     */
    @SafeVarargs
    static <T> Trampoline<T> sequence(Trampoline<T>... trampolines) {
        if (trampolines == null || trampolines.length == 0) {
            throw new IllegalArgumentException("At least one Trampoline required");
        }

        Trampoline<T> result = trampolines[0];
        for (int i = 1; i < trampolines.length; i++) {
            final Trampoline<T> next = trampolines[i];
            result = result.flatMap(ignored -> next);
        }
        return result;
    }
}
