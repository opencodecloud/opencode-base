package cloud.opencode.base.functional.pattern;

import cloud.opencode.base.functional.exception.OpenMatchException;

import java.lang.reflect.RecordComponent;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * OpenMatch - Pattern matching entry point
 * OpenMatch - 模式匹配入口
 *
 * <p>Provides a fluent API for pattern matching that complements JDK 25's
 * native pattern matching with additional features.</p>
 * <p>提供流式 API 用于模式匹配，补充 JDK 25 原生模式匹配的附加功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type matching (caseOf) - 类型匹配</li>
 *   <li>Predicate matching (when) - 谓词匹配</li>
 *   <li>Value equality matching (whenEquals) - 值相等匹配</li>
 *   <li>Record deconstruction (caseRecord) - Record 解构</li>
 *   <li>Sealed type matching (caseSealed) - 密封类型匹配</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic type matching
 * String result = OpenMatch.of(value)
 *     .caseOf(String.class, s -> "String: " + s)
 *     .caseOf(Integer.class, n -> "Number: " + n)
 *     .caseOf(List.class, l -> "List: " + l.size())
 *     .orElse(o -> "Unknown");
 *
 * // Predicate matching
 * String desc = OpenMatch.of(number)
 *     .when(n -> n < 0, n -> "Negative")
 *     .when(n -> n == 0, n -> "Zero")
 *     .when(n -> n > 0, n -> "Positive")
 *     .orElseThrow();
 *
 * // Value matching
 * String day = OpenMatch.of(dayOfWeek)
 *     .whenEquals(1, d -> "Monday")
 *     .whenEquals(2, d -> "Tuesday")
 *     .orElse(d -> "Other");
 *
 * // Use JDK 25 native pattern matching for sealed types
 * double area = switch (shape) {
 *     case Circle(var r) -> Math.PI * r * r;
 *     case Rectangle(var w, var h) -> w * h;
 * };
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (matcher is mutable but typically used locally)</li>
 *   <li>Null-safe: Handles null values - 空值安全: 处理 null 值</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(k) where k is the number of cases evaluated before first match - 时间复杂度: O(k)，k 为首次匹配前评估的分支数</li>
 *   <li>Space complexity: O(1) per Matcher instance - 空间复杂度: O(1) 每个 Matcher 实例</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public final class OpenMatch {

    private OpenMatch() {
        // Entry point only
    }

    /**
     * Start pattern matching on a value
     * 开始对值进行模式匹配
     *
     * @param value value to match - 要匹配的值
     * @param <T>   value type - 值类型
     * @return Matcher for fluent API
     */
    public static <T> Matcher<T> of(T value) {
        return new Matcher<>(value);
    }

    /**
     * Matcher - Fluent pattern matching builder
     * Matcher - 流式模式匹配构建器
     *
     * @param <T> value type - 值类型
     */
    public static final class Matcher<T> {
        private final T value;
        private boolean matched = false;
        private Object result;

        Matcher(T value) {
            this.value = value;
        }

        /**
         * Match by type
         * 按类型匹配
         *
         * @param type   type to match - 要匹配的类型
         * @param action action to execute - 要执行的动作
         * @param <U>    matched type - 匹配的类型
         * @param <R>    result type - 结果类型
         * @return this Matcher for chaining
         */
        @SuppressWarnings("unchecked")
        public <U, R> Matcher<T> caseOf(Class<U> type, Function<? super U, R> action) {
            if (!matched && type.isInstance(value)) {
                result = action.apply((U) value);
                matched = true;
            }
            return this;
        }

        /**
         * Match by predicate
         * 按谓词匹配
         *
         * @param predicate condition to match - 匹配条件
         * @param action    action to execute - 要执行的动作
         * @param <R>       result type - 结果类型
         * @return this Matcher for chaining
         */
        public <R> Matcher<T> when(Predicate<? super T> predicate, Function<? super T, R> action) {
            if (!matched && value != null && predicate.test(value)) {
                result = action.apply(value);
                matched = true;
            }
            return this;
        }

        /**
         * Match by value equality
         * 按值相等匹配
         *
         * @param expected expected value - 期望值
         * @param action   action to execute - 要执行的动作
         * @param <R>      result type - 结果类型
         * @return this Matcher for chaining
         */
        public <R> Matcher<T> whenEquals(T expected, Function<? super T, R> action) {
            if (!matched && Objects.equals(value, expected)) {
                result = action.apply(value);
                matched = true;
            }
            return this;
        }

        /**
         * Match null value
         * 匹配 null 值
         *
         * @param action action to execute - 要执行的动作
         * @param <R>    result type - 结果类型
         * @return this Matcher for chaining
         */
        public <R> Matcher<T> whenNull(Function<? super T, R> action) {
            if (!matched && value == null) {
                result = action.apply(value);
                matched = true;
            }
            return this;
        }

        /**
         * Match record and deconstruct (2 components)
         * 匹配 Record 并解构（2 个组件）
         *
         * @param recordType record type to match - 要匹配的 Record 类型
         * @param action     action receiving deconstructed components - 接收解构组件的动作
         * @param <R1>       first component type - 第一个组件类型
         * @param <R2>       second component type - 第二个组件类型
         * @param <Out>      result type - 结果类型
         * @return this Matcher for chaining
         */
        @SuppressWarnings("unchecked")
        public <R1, R2, Out> Matcher<T> caseRecord(
                Class<? extends Record> recordType,
                BiFunction<R1, R2, Out> action) {
            if (!matched && recordType.isInstance(value)) {
                RecordComponent[] components = recordType.getRecordComponents();
                if (components.length >= 2) {
                    try {
                        R1 r1 = (R1) components[0].getAccessor().invoke(value);
                        R2 r2 = (R2) components[1].getAccessor().invoke(value);
                        result = action.apply(r1, r2);
                        matched = true;
                    } catch (Exception e) {
                        // Deconstruction failed, don't match
                    }
                }
            }
            return this;
        }

        /**
         * Match sealed type
         * 匹配密封类型
         *
         * @param sealedType sealed type to match - 要匹配的密封类型
         * @param action     action to execute - 要执行的动作
         * @param <S>        sealed type - 密封类型
         * @param <R>        result type - 结果类型
         * @return this Matcher for chaining
         */
        @SuppressWarnings("unchecked")
        public <S, R> Matcher<T> caseSealed(Class<S> sealedType, Function<S, R> action) {
            if (!matched && sealedType.isInstance(value)) {
                result = action.apply((S) value);
                matched = true;
            }
            return this;
        }

        /**
         * Apply a Case
         * 应用一个 Case
         *
         * @param matchCase case to apply - 要应用的分支
         * @param <R>       result type - 结果类型
         * @return this Matcher for chaining
         */
        public <R> Matcher<T> match(Case<T, R> matchCase) {
            if (!matched && matchCase.matches(value)) {
                result = matchCase.apply(value).orElse(null);
                matched = true;
            }
            return this;
        }

        /**
         * Match using a Pattern with action
         * 使用 Pattern 进行匹配并执行动作
         *
         * <p><strong>Example | 示例:</strong></p>
         * <pre>
         * Pattern&lt;Object, String&gt; stringPattern = Pattern.type(String.class);
         * String result = OpenMatch.of(value)
         *     .match(stringPattern, s -&gt; "String: " + s)
         *     .orElse(o -&gt; "Unknown");
         * </pre>
         *
         * @param pattern pattern to match - 要匹配的模式
         * @param action  action to execute on match - 匹配时执行的动作
         * @param <U>     pattern result type - 模式结果类型
         * @param <R>     result type - 结果类型
         * @return this Matcher for chaining
         */
        public <U, R> Matcher<T> match(Pattern<T, U> pattern, Function<? super U, R> action) {
            if (!matched) {
                var matchResult = pattern.match(value);
                if (matchResult.isPresent()) {
                    result = action.apply(matchResult.get());
                    matched = true;
                }
            }
            return this;
        }

        /**
         * Get result or default if no match
         * 获取结果或默认值（如果没有匹配）
         *
         * @param defaultAction default action - 默认动作
         * @param <R>           result type - 结果类型
         * @return result or default
         */
        @SuppressWarnings("unchecked")
        public <R> R orElse(Function<? super T, R> defaultAction) {
            if (!matched) {
                return defaultAction.apply(value);
            }
            return (R) result;
        }

        /**
         * Get result or default value if no match
         * 获取结果或默认值（如果没有匹配）
         *
         * @param defaultValue default value - 默认值
         * @param <R>          result type - 结果类型
         * @return result or default
         */
        @SuppressWarnings("unchecked")
        public <R> R orElseGet(R defaultValue) {
            if (!matched) {
                return defaultValue;
            }
            return (R) result;
        }

        /**
         * Get result or throw if no match
         * 获取结果或抛出异常（如果没有匹配）
         *
         * @param <R> result type - 结果类型
         * @return result
         * @throws OpenMatchException if no match
         */
        @SuppressWarnings("unchecked")
        public <R> R orElseThrow() {
            if (!matched) {
                throw OpenMatchException.noMatch(value);
            }
            return (R) result;
        }

        /**
         * Get result with type checking
         * 获取结果并进行类型检查
         *
         * <p>This is the type-safe version of orElseGet/orElse. Use this when
         * you want to ensure the result is of the expected type at runtime.</p>
         * <p>这是 orElseGet/orElse 的类型安全版本。当您想在运行时确保结果是预期类型时使用。</p>
         *
         * @param resultType expected result type - 期望的结果类型
         * @param defaultValue default value if no match or wrong type - 无匹配或类型错误时的默认值
         * @param <R>        result type - 结果类型
         * @return result or default value
         */
        public <R> R getAs(Class<R> resultType, R defaultValue) {
            if (!matched) {
                return defaultValue;
            }
            if (result == null) {
                return defaultValue;
            }
            if (resultType.isInstance(result)) {
                return resultType.cast(result);
            }
            return defaultValue;
        }

        /**
         * Get result with type checking, throwing if wrong type
         * 获取结果并进行类型检查，类型错误时抛出异常
         *
         * @param resultType expected result type - 期望的结果类型
         * @param <R>        result type - 结果类型
         * @return result
         * @throws OpenMatchException if no match or wrong type
         */
        public <R> R getAs(Class<R> resultType) {
            if (!matched) {
                throw OpenMatchException.noMatch(value);
            }
            if (result == null) {
                return null;
            }
            if (resultType.isInstance(result)) {
                return resultType.cast(result);
            }
            throw new OpenMatchException(
                "Result type mismatch: expected " + resultType.getName() +
                ", got " + result.getClass().getName());
        }

        /**
         * Check if a match was found
         * 检查是否找到匹配
         *
         * @return true if matched - 如果匹配返回 true
         */
        public boolean isMatched() {
            return matched;
        }

        /**
         * Get the original value
         * 获取原始值
         *
         * @return the value being matched - 被匹配的值
         */
        public T value() {
            return value;
        }
    }
}
