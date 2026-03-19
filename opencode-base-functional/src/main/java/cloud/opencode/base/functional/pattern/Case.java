package cloud.opencode.base.functional.pattern;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Case - Match case definition combining pattern and action
 * Case - 组合模式和动作的匹配分支定义
 *
 * <p>Represents a single case in pattern matching, consisting of a pattern
 * to match against and an action to execute on match.</p>
 * <p>表示模式匹配中的单个分支，由要匹配的模式和匹配时执行的动作组成。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pattern + action bundling - 模式 + 动作绑定</li>
 *   <li>Type-safe case creation - 类型安全的分支创建</li>
 *   <li>Factory methods for common cases - 常见分支的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Type case
 * Case<Object, String> stringCase = Case.type(String.class, s -> "String: " + s);
 *
 * // Predicate case
 * Case<Integer, String> positive = Case.when(n -> n > 0, n -> "Positive: " + n);
 *
 * // Default case
 * Case<Object, String> defaultCase = Case.otherwise(o -> "Unknown: " + o);
 *
 * // Apply case
 * Optional<String> result = stringCase.apply("hello");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Null-safe: Handles null input - 空值安全: 处理 null 输入</li>
 * </ul>
 *
 * @param <T> input type - 输入类型
 * @param <R> result type - 结果类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public final class Case<T, R> {

    private final Pattern<T, ?> pattern;
    private final Function<? super T, ? extends R> action;

    private Case(Pattern<T, ?> pattern, Function<? super T, ? extends R> action) {
        this.pattern = pattern;
        this.action = action;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a case for type matching
     * 创建类型匹配分支
     *
     * @param type   type to match - 要匹配的类型
     * @param action action to execute - 要执行的动作
     * @param <T>    input type - 输入类型
     * @param <U>    matched type - 匹配的类型
     * @param <R>    result type - 结果类型
     * @return type case
     */
    @SuppressWarnings("unchecked")
    public static <T, U, R> Case<T, R> type(Class<U> type, Function<? super U, ? extends R> action) {
        Pattern<T, U> pattern = Pattern.type(type);
        return new Case<>(pattern, t -> action.apply((U) t));
    }

    /**
     * Create a case for predicate matching
     * 创建谓词匹配分支
     *
     * @param predicate condition to match - 匹配条件
     * @param action    action to execute - 要执行的动作
     * @param <T>       input type - 输入类型
     * @param <R>       result type - 结果类型
     * @return predicate case
     */
    public static <T, R> Case<T, R> when(Predicate<? super T> predicate,
                                          Function<? super T, ? extends R> action) {
        Pattern<T, T> pattern = value -> predicate.test(value) ? Optional.of(value) : Optional.empty();
        return new Case<>(pattern, action);
    }

    /**
     * Create a case for value equality matching
     * 创建值相等匹配分支
     *
     * @param expected expected value - 期望值
     * @param action   action to execute - 要执行的动作
     * @param <T>      input type - 输入类型
     * @param <R>      result type - 结果类型
     * @return equality case
     */
    public static <T, R> Case<T, R> equals(T expected, Function<? super T, ? extends R> action) {
        Pattern<T, T> pattern = Pattern.equalTo(expected);
        return new Case<>(pattern, action);
    }

    /**
     * Create a case for null matching
     * 创建 null 匹配分支
     *
     * @param action action to execute - 要执行的动作
     * @param <T>    input type - 输入类型
     * @param <R>    result type - 结果类型
     * @return null case
     */
    public static <T, R> Case<T, R> isNull(Function<? super T, ? extends R> action) {
        Pattern<T, T> pattern = value -> value == null ? Optional.of(value) : Optional.empty();
        return new Case<>(pattern, action);
    }

    /**
     * Create a default case that always matches
     * 创建始终匹配的默认分支
     *
     * @param action action to execute - 要执行的动作
     * @param <T>    input type - 输入类型
     * @param <R>    result type - 结果类型
     * @return default case
     */
    public static <T, R> Case<T, R> otherwise(Function<? super T, ? extends R> action) {
        Pattern<T, T> pattern = Pattern.any();
        return new Case<>(pattern, action);
    }

    /**
     * Create a case from a custom pattern
     * 从自定义模式创建分支
     *
     * @param pattern custom pattern - 自定义模式
     * @param action  action to execute - 要执行的动作
     * @param <T>     input type - 输入类型
     * @param <U>     pattern result type - 模式结果类型
     * @param <R>     result type - 结果类型
     * @return custom case
     */
    @SuppressWarnings("unchecked")
    public static <T, U, R> Case<T, R> of(Pattern<T, U> pattern,
                                           Function<? super U, ? extends R> action) {
        return new Case<>((Pattern<T, ?>) pattern, t -> {
            Optional<U> matched = pattern.match(t);
            return matched.map(action).orElse(null);
        });
    }

    // ==================== Operations | 操作 ====================

    /**
     * Try to apply this case to a value
     * 尝试将此分支应用于值
     *
     * @param value value to match - 要匹配的值
     * @return Optional containing result if matched
     */
    public Optional<R> apply(T value) {
        if (pattern.matches(value)) {
            return Optional.ofNullable(action.apply(value));
        }
        return Optional.empty();
    }

    /**
     * Check if this case matches the value
     * 检查此分支是否匹配值
     *
     * @param value value to check - 要检查的值
     * @return true if matches - 如果匹配返回 true
     */
    public boolean matches(T value) {
        return pattern.matches(value);
    }

    /**
     * Get the pattern
     * 获取模式
     *
     * @return the pattern - 模式
     */
    public Pattern<T, ?> pattern() {
        return pattern;
    }

    /**
     * Get the action
     * 获取动作
     *
     * @return the action - 动作
     */
    public Function<? super T, ? extends R> action() {
        return action;
    }
}
