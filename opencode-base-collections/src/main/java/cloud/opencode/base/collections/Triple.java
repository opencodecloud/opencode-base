package cloud.opencode.base.collections;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

/**
 * Triple - Immutable generic triple of three values
 * Triple - 不可变的泛型三元组
 *
 * <p>A simple container holding three related values. Provides functional
 * transformation methods and conversion to {@link Pair}.</p>
 * <p>一个持有三个相关值的简单容器。提供函数式变换方法以及到 {@link Pair} 的转换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable record-based implementation - 基于 record 的不可变实现</li>
 *   <li>Functional transformations (mapFirst, mapSecond, mapThird) - 函数式变换</li>
 *   <li>Projection to Pair (dropFirst, dropSecond, dropThird) - 投影到 Pair</li>
 *   <li>Null-safe: allows null values - 空值安全：允许 null 值</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a triple - 创建三元组
 * Triple<String, Integer, Boolean> triple = Triple.of("name", 25, true);
 *
 * // Map individual elements - 映射单个元素
 * Triple<String, String, Boolean> mapped = triple.mapSecond(String::valueOf);
 *
 * // Drop to pair - 投影到二元组
 * Pair<String, Integer> pair = triple.dropThird(); // ("name", 25)
 * }</pre>
 *
 * @param <A> the type of the first element - 第一个元素的类型
 * @param <B> the type of the second element - 第二个元素的类型
 * @param <C> the type of the third element - 第三个元素的类型
 * @param first  the first element - 第一个元素
 * @param second the second element - 第二个元素
 * @param third  the third element - 第三个元素
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Pair
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public record Triple<A, B, C>(A first, B second, C third) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new Triple with the given values.
     * 使用给定的值创建新的三元组。
     *
     * @param first  the first element (nullable) - 第一个元素（可为 null）
     * @param second the second element (nullable) - 第二个元素（可为 null）
     * @param third  the third element (nullable) - 第三个元素（可为 null）
     * @param <A>    the type of the first element - 第一个元素的类型
     * @param <B>    the type of the second element - 第二个元素的类型
     * @param <C>    the type of the third element - 第三个元素的类型
     * @return a new Triple - 新的三元组
     */
    public static <A, B, C> Triple<A, B, C> of(A first, B second, C third) {
        return new Triple<>(first, second, third);
    }

    // ---- Transformation methods | 变换方法 ----

    /**
     * Transforms the first element using the given function.
     * 使用给定函数变换第一个元素。
     *
     * @param fn  the mapping function - 映射函数
     * @param <D> the type of the new first element - 新的第一个元素的类型
     * @return a new Triple with the transformed first element - 变换了第一个元素的新三元组
     * @throws NullPointerException if fn is null - 如果 fn 为 null
     */
    public <D> Triple<D, B, C> mapFirst(Function<? super A, ? extends D> fn) {
        Objects.requireNonNull(fn, "fn must not be null");
        return new Triple<>(fn.apply(first), second, third);
    }

    /**
     * Transforms the second element using the given function.
     * 使用给定函数变换第二个元素。
     *
     * @param fn  the mapping function - 映射函数
     * @param <D> the type of the new second element - 新的第二个元素的类型
     * @return a new Triple with the transformed second element - 变换了第二个元素的新三元组
     * @throws NullPointerException if fn is null - 如果 fn 为 null
     */
    public <D> Triple<A, D, C> mapSecond(Function<? super B, ? extends D> fn) {
        Objects.requireNonNull(fn, "fn must not be null");
        return new Triple<>(first, fn.apply(second), third);
    }

    /**
     * Transforms the third element using the given function.
     * 使用给定函数变换第三个元素。
     *
     * @param fn  the mapping function - 映射函数
     * @param <D> the type of the new third element - 新的第三个元素的类型
     * @return a new Triple with the transformed third element - 变换了第三个元素的新三元组
     * @throws NullPointerException if fn is null - 如果 fn 为 null
     */
    public <D> Triple<A, B, D> mapThird(Function<? super C, ? extends D> fn) {
        Objects.requireNonNull(fn, "fn must not be null");
        return new Triple<>(first, second, fn.apply(third));
    }

    // ---- Projection methods | 投影方法 ----

    /**
     * Returns a Pair containing the first and second elements, dropping the third.
     * 返回包含第一和第二个元素的二元组，丢弃第三个。
     *
     * @return a Pair of (first, second) - (first, second) 二元组
     */
    public Pair<A, B> dropThird() {
        return Pair.of(first, second);
    }

    /**
     * Returns a Pair containing the second and third elements, dropping the first.
     * 返回包含第二和第三个元素的二元组，丢弃第一个。
     *
     * @return a Pair of (second, third) - (second, third) 二元组
     */
    public Pair<B, C> dropFirst() {
        return Pair.of(second, third);
    }

    /**
     * Returns a Pair containing the first and third elements, dropping the second.
     * 返回包含第一和第三个元素的二元组，丢弃第二个。
     *
     * @return a Pair of (first, third) - (first, third) 二元组
     */
    public Pair<A, C> dropSecond() {
        return Pair.of(first, third);
    }
}
