package cloud.opencode.base.functional.pattern;

import java.util.Optional;

/**
 * Pattern - Pattern interface for matching values
 * Pattern - 用于匹配值的模式接口
 *
 * <p>Defines the contract for patterns that can match against values
 * and extract results.</p>
 * <p>定义可以匹配值并提取结果的模式契约。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe pattern matching - 类型安全的模式匹配</li>
 *   <li>Composable patterns - 可组合的模式</li>
 *   <li>Works with OpenMatch - 与 OpenMatch 配合使用</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Type pattern
 * Pattern<Object, String> stringPattern = Pattern.type(String.class);
 *
 * // Predicate pattern
 * Pattern<Integer, Integer> positive = Pattern.when(n -> n > 0);
 *
 * // Value pattern
 * Pattern<String, String> hello = Pattern.equals("hello");
 *
 * // Using with OpenMatch
 * OpenMatch.of(value)
 *     .match(stringPattern, s -> "String: " + s)
 *     .match(positive, n -> "Positive: " + n);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Patterns handle null - 空值安全: 模式处理 null</li>
 * </ul>
 *
 * @param <T> input type to match - 要匹配的输入类型
 * @param <R> extracted result type - 提取的结果类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@FunctionalInterface
public interface Pattern<T, R> {

    /**
     * Try to match the value
     * 尝试匹配值
     *
     * @param value value to match - 要匹配的值
     * @return Optional containing extracted value if matched, empty otherwise
     */
    Optional<R> match(T value);

    /**
     * Check if pattern matches without extracting
     * 检查是否匹配而不提取
     *
     * @param value value to check - 要检查的值
     * @return true if matches - 如果匹配返回 true
     */
    default boolean matches(T value) {
        return match(value).isPresent();
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a type matching pattern
     * 创建类型匹配模式
     *
     * @param type type to match - 要匹配的类型
     * @param <T>  input type - 输入类型
     * @param <R>  target type - 目标类型
     * @return type pattern
     */
    @SuppressWarnings("unchecked")
    static <T, R> Pattern<T, R> type(Class<R> type) {
        return value -> {
            if (value != null && type.isInstance(value)) {
                return Optional.of((R) value);
            }
            return Optional.empty();
        };
    }

    /**
     * Create an equality matching pattern
     * 创建相等匹配模式
     *
     * @param expected expected value - 期望值
     * @param <T>      value type - 值类型
     * @return equality pattern
     */
    static <T> Pattern<T, T> equalTo(T expected) {
        return value -> {
            if (java.util.Objects.equals(value, expected)) {
                return Optional.ofNullable(value);
            }
            return Optional.empty();
        };
    }

    /**
     * Create a predicate matching pattern
     * 创建谓词匹配模式
     *
     * @param predicate condition to match - 匹配条件
     * @param <T>       value type - 值类型
     * @return predicate pattern
     */
    static <T> Pattern<T, T> when(java.util.function.Predicate<T> predicate) {
        return value -> {
            if (value != null && predicate.test(value)) {
                return Optional.of(value);
            }
            return Optional.empty();
        };
    }

    /**
     * Create a null matching pattern
     * 创建 null 匹配模式
     *
     * @param <T> value type - 值类型
     * @return null pattern
     */
    static <T> Pattern<T, T> isNull() {
        return new Pattern<>() {
            @Override
            public Optional<T> match(T value) {
                return Optional.empty();
            }
            @Override
            public boolean matches(T value) {
                return value == null;
            }
        };
    }

    /**
     * Create a pattern that always matches
     * 创建始终匹配的模式
     *
     * @param <T> value type - 值类型
     * @return wildcard pattern
     */
    static <T> Pattern<T, T> any() {
        return Optional::ofNullable;
    }

    // ==================== Combinators | 组合器 ====================

    /**
     * Combine with another pattern (AND)
     * 与另一个模式组合（AND）
     *
     * @param other other pattern - 其他模式
     * @return combined pattern
     */
    default Pattern<T, R> and(Pattern<? super R, R> other) {
        return value -> match(value).flatMap(other::match);
    }

    /**
     * Combine with another pattern (OR)
     * 与另一个模式组合（OR）
     *
     * @param other alternative pattern - 备选模式
     * @return combined pattern
     */
    default Pattern<T, R> or(Pattern<T, R> other) {
        return value -> {
            Optional<R> result = match(value);
            return result.isPresent() ? result : other.match(value);
        };
    }

    /**
     * Transform the matched result
     * 转换匹配的结果
     *
     * @param mapper transformation function - 转换函数
     * @param <U>    result type - 结果类型
     * @return transformed pattern
     */
    default <U> Pattern<T, U> map(java.util.function.Function<? super R, ? extends U> mapper) {
        return value -> match(value).map(mapper);
    }

    /**
     * Add a guard condition
     * 添加守卫条件
     *
     * @param predicate guard condition - 守卫条件
     * @return guarded pattern
     */
    default Pattern<T, R> filter(java.util.function.Predicate<? super R> predicate) {
        return value -> match(value).filter(predicate);
    }
}
