package cloud.opencode.base.reflect.lambda;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * Serializable Predicate Interface
 * 可序列化Predicate接口
 *
 * <p>A Predicate that is also Serializable, enabling lambda metadata extraction.</p>
 * <p>一个同时也是Serializable的Predicate，可以提取lambda元数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Serializable Predicate for lambda metadata extraction - 可序列化Predicate用于lambda元数据提取</li>
 *   <li>Composable with and, or, negate - 可通过and、or、negate组合</li>
 *   <li>Factory methods: alwaysTrue, alwaysFalse, isNull, nonNull, isEqual - 工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SerializablePredicate<String> notEmpty = s -> !s.isEmpty();
 * boolean result = notEmpty.test("hello"); // true
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @param <T> the input type | 输入类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@FunctionalInterface
public interface SerializablePredicate<T> extends Predicate<T>, Serializable {

    /**
     * Creates a SerializablePredicate
     * 创建SerializablePredicate
     *
     * @param predicate the predicate | 谓词
     * @param <T>       the input type | 输入类型
     * @return the serializable predicate | 可序列化谓词
     */
    static <T> SerializablePredicate<T> of(SerializablePredicate<T> predicate) {
        return predicate;
    }

    /**
     * Creates a predicate that always returns true
     * 创建总是返回true的谓词
     *
     * @param <T> the input type | 输入类型
     * @return the always-true predicate | 总是true的谓词
     */
    static <T> SerializablePredicate<T> alwaysTrue() {
        return t -> true;
    }

    /**
     * Creates a predicate that always returns false
     * 创建总是返回false的谓词
     *
     * @param <T> the input type | 输入类型
     * @return the always-false predicate | 总是false的谓词
     */
    static <T> SerializablePredicate<T> alwaysFalse() {
        return t -> false;
    }

    /**
     * Creates a predicate that checks for null
     * 创建检查null的谓词
     *
     * @param <T> the input type | 输入类型
     * @return the null-checking predicate | 检查null的谓词
     */
    static <T> SerializablePredicate<T> isNull() {
        return t -> t == null;
    }

    /**
     * Creates a predicate that checks for non-null
     * 创建检查非null的谓词
     *
     * @param <T> the input type | 输入类型
     * @return the non-null-checking predicate | 检查非null的谓词
     */
    static <T> SerializablePredicate<T> nonNull() {
        return t -> t != null;
    }

    /**
     * Creates a predicate that checks equality with a value
     * 创建检查与值相等的谓词
     *
     * @param targetRef the target reference | 目标引用
     * @param <T>       the input type | 输入类型
     * @return the equality predicate | 相等谓词
     */
    static <T> SerializablePredicate<T> isEqual(Object targetRef) {
        return (null == targetRef) ? isNull() : t -> targetRef.equals(t);
    }

    /**
     * Returns a composed predicate (AND)
     * 返回组合谓词（AND）
     *
     * @param other the other predicate | 另一个谓词
     * @return the composed predicate | 组合后的谓词
     */
    default SerializablePredicate<T> and(SerializablePredicate<? super T> other) {
        return (t) -> test(t) && other.test(t);
    }

    /**
     * Returns a negated predicate
     * 返回否定的谓词
     *
     * @return the negated predicate | 否定的谓词
     */
    @Override
    default SerializablePredicate<T> negate() {
        return (t) -> !test(t);
    }

    /**
     * Returns a composed predicate (OR)
     * 返回组合谓词（OR）
     *
     * @param other the other predicate | 另一个谓词
     * @return the composed predicate | 组合后的谓词
     */
    default SerializablePredicate<T> or(SerializablePredicate<? super T> other) {
        return (t) -> test(t) || other.test(t);
    }
}
