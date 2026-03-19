package cloud.opencode.base.core.func;

import cloud.opencode.base.core.exception.OpenException;

import java.util.function.Predicate;

/**
 * Checked Predicate - Predicate that can throw checked exceptions
 * 可抛出受检异常的 Predicate - 扩展 JDK Predicate 支持受检异常
 *
 * <p>Extends JDK {@link Predicate} to allow throwing checked exceptions in lambdas.</p>
 * <p>扩展 JDK {@link Predicate}，支持在 lambda 中抛出受检异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Throw checked exceptions in lambda - 在 lambda 中抛出受检异常</li>
 *   <li>Convert to standard Predicate (unchecked) - 转换为标准 Predicate</li>
 *   <li>Silent execution (testQuietly/testOrDefault) - 静默执行</li>
 *   <li>Logical operations (and/or/negate) - 逻辑操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CheckedPredicate<Path> isLarge = path -> Files.size(path) > 1024;
 * paths.stream().filter(isLarge.unchecked()).toList();
 * boolean result = isLarge.testOrDefault(path, false);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @param <T> input type - 输入类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@FunctionalInterface
public interface CheckedPredicate<T> {

    /**
     * Tests a condition that may throw a checked exception
     * 测试条件，可能抛出受检异常
     *
     * @param t the value | 输入值
     * @return the result | 测试结果
     * @throws Exception if the condition is not met | 如果操作失败
     */
    boolean test(T t) throws Exception;

    /**
     * Converts
     * 转换为标准 Predicate，受检异常包装为 RuntimeException
     *
     * @return Predicate
     */
    default Predicate<T> unchecked() {
        return t -> {
            try {
                return test(t);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new OpenException("Checked predicate failed", e);
            }
        };
    }

    /**
     * Silently tests, returning false on exception
     * 静默测试，异常时返回 false
     *
     * @param t the value | 输入值
     * @return the result | 测试结果，异常时返回 false
     */
    default boolean testQuietly(T t) {
        try {
            return test(t);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Silently tests, returning the specified default on exception
     * 静默测试，异常时返回指定默认值
     *
     * @param t the value | 输入值
     * @param defaultValue the default value | 默认值
     * @return the result | 测试结果或默认值
     */
    default boolean testOrDefault(T t, boolean defaultValue) {
        try {
            return test(t);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Negates
     * 取反
     *
     * @return the result | 取反后的 Predicate
     */
    default CheckedPredicate<T> negate() {
        return t -> !test(t);
    }

    /**
     * Logical AND
     * 逻辑与
     *
     * @param other the value | 另一个 Predicate
     * @return the result | 组合后的 Predicate
     */
    default CheckedPredicate<T> and(CheckedPredicate<? super T> other) {
        return t -> test(t) && other.test(t);
    }

    /**
     * Logical OR
     * 逻辑或
     *
     * @param other the value | 另一个 Predicate
     * @return the result | 组合后的 Predicate
     */
    default CheckedPredicate<T> or(CheckedPredicate<? super T> other) {
        return t -> test(t) || other.test(t);
    }

    /**
     * Wraps a standard Predicate as a CheckedPredicate
     * 将普通 Predicate 包装为 CheckedPredicate
     *
     * @param predicate the value | 普通 Predicate
     * @param <T> the value | 输入类型
     * @return CheckedPredicate
     */
    static <T> CheckedPredicate<T> of(Predicate<T> predicate) {
        return predicate::test;
    }
}
