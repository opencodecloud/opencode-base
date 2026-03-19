package cloud.opencode.base.core.stream;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Optional Utility - Enhanced Optional operations
 * Optional 工具类 - 增强的 Optional 操作
 *
 * <p>Provides extended operations for Java Optional beyond the standard API.</p>
 * <p>提供超越标准 API 的 Java Optional 扩展操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>First present selection (firstPresent, firstPresentLazy) - 第一个存在值选择</li>
 *   <li>Multiple Optional combination (combine, combine3) - 多个 Optional 组合</li>
 *   <li>Presence checks (allPresent, anyPresent) - 存在性检查</li>
 *   <li>Flatten nested Optional - 扁平化嵌套 Optional</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // First present - 第一个存在值
 * Optional<String> first = OptionalUtil.firstPresent(opt1, opt2, opt3);
 *
 * // Combine two - 组合两个
 * Optional<String> combined = OptionalUtil.combine(opt1, opt2, (a, b) -> a + b);
 *
 * // Check all present - 检查全部存在
 * boolean allOk = OptionalUtil.allPresent(opt1, opt2, opt3);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OptionalUtil {

    private OptionalUtil() {
    }

    /**
     * Returns the first present Optional
     * 返回第一个存在值的 Optional
     */
    @SafeVarargs
    public static <T> Optional<T> firstPresent(Optional<T>... optionals) {
        for (Optional<T> opt : optionals) {
            if (opt != null && opt.isPresent()) {
                return opt;
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the first present Optional (using Supplier for lazy evaluation)
     * 返回第一个存在值的 Optional（使用 Supplier 延迟计算）
     */
    @SafeVarargs
    public static <T> Optional<T> firstPresentLazy(Supplier<Optional<T>>... suppliers) {
        for (Supplier<Optional<T>> supplier : suppliers) {
            Optional<T> opt = supplier.get();
            if (opt != null && opt.isPresent()) {
                return opt;
            }
        }
        return Optional.empty();
    }

    /**
     * Maps if present
     * 存在时映射
     */
    public static <T, R> Optional<R> mapIfPresent(Optional<T> optional, Function<T, R> mapper) {
        return optional.map(mapper);
    }

    /**
     * Converts to Stream
     * 转为 Stream
     */
    public static <T> Stream<T> stream(Optional<T> optional) {
        return optional.stream();
    }

    /**
     * Combines two Optionals
     * 组合两个 Optional
     */
    public static <T, U, R> Optional<R> combine(Optional<T> opt1, Optional<U> opt2, BiFunction<T, U, R> combiner) {
        if (opt1.isPresent() && opt2.isPresent()) {
            return Optional.ofNullable(combiner.apply(opt1.get(), opt2.get()));
        }
        return Optional.empty();
    }

    /**
     * Combines three Optionals
     * 组合三个 Optional
     */
    public static <T, U, V, R> Optional<R> combine3(Optional<T> opt1, Optional<U> opt2, Optional<V> opt3,
                                                     TriFunction<T, U, V, R> combiner) {
        if (opt1.isPresent() && opt2.isPresent() && opt3.isPresent()) {
            return Optional.ofNullable(combiner.apply(opt1.get(), opt2.get(), opt3.get()));
        }
        return Optional.empty();
    }

    /**
     * Gets the value or computes a default
     * 获取值或计算默认值
     */
    public static <T> T orElseGet(Optional<T> optional, Supplier<T> supplier) {
        return optional.orElseGet(supplier);
    }

    /**
     * Gets the value or returns null
     * 获取值或返回 null
     */
    public static <T> T orNull(Optional<T> optional) {
        return optional.orElse(null);
    }

    /**
     * Throws an exception if empty
     * 如果为空则抛出异常
     */
    public static <T, X extends Throwable> T orElseThrow(Optional<T> optional, Supplier<X> exceptionSupplier) throws X {
        return optional.orElseThrow(exceptionSupplier);
    }

    /**
     * Transforms the value inside Optional, handling null
     * 转换 Optional 内的值，处理 null
     */
    public static <T, R> Optional<R> flatMapNullable(Optional<T> optional, Function<T, R> mapper) {
        return optional.flatMap(value -> Optional.ofNullable(mapper.apply(value)));
    }

    /**
     * Filters and maps
     * 过滤并映射
     */
    public static <T, R> Optional<R> filterAndMap(Optional<T> optional, java.util.function.Predicate<T> predicate, Function<T, R> mapper) {
        return optional.filter(predicate).map(mapper);
    }

    /**
     * Executes an action if the value is present
     * 执行操作，如果存在值
     */
    public static <T> void ifPresentOrElse(Optional<T> optional, java.util.function.Consumer<T> action, Runnable emptyAction) {
        optional.ifPresentOrElse(action, emptyAction);
    }

    /**
     * Flattens Optional<Optional<T>> to Optional<T>
     * 将 Optional<Optional<T>> 扁平化为 Optional<T>
     */
    public static <T> Optional<T> flatten(Optional<Optional<T>> optional) {
        return optional.flatMap(Function.identity());
    }

    /**
     * Checks if all Optionals have a value present
     * 检查所有 Optional 是否都存在值
     */
    @SafeVarargs
    public static boolean allPresent(Optional<?>... optionals) {
        for (Optional<?> opt : optionals) {
            if (opt == null || opt.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if any Optional has a value present
     * 检查是否有任意 Optional 存在值
     */
    @SafeVarargs
    public static boolean anyPresent(Optional<?>... optionals) {
        for (Optional<?> opt : optionals) {
            if (opt != null && opt.isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Three-argument function interface
     * 三参数函数接口
     */
    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }
}
