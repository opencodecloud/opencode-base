package cloud.opencode.base.collections;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Pair - Immutable generic pair of two values
 * Pair - 不可变的泛型二元组
 *
 * <p>A simple container holding two related values. Implements {@link Map.Entry}
 * for seamless interoperability with Java collections.</p>
 * <p>一个持有两个相关值的简单容器。实现 {@link Map.Entry} 以便与 Java 集合无缝互操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable record-based implementation - 基于 record 的不可变实现</li>
 *   <li>Map.Entry compatibility - Map.Entry 兼容</li>
 *   <li>Functional transformations (map, swap) - 函数式变换（map、swap）</li>
 *   <li>Null-safe: allows null values - 空值安全：允许 null 值</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a pair - 创建二元组
 * Pair<String, Integer> pair = Pair.of("age", 25);
 *
 * // Swap elements - 交换元素
 * Pair<Integer, String> swapped = pair.swap(); // (25, "age")
 *
 * // Map values - 映射值
 * Pair<String, String> mapped = pair.mapSecond(String::valueOf); // ("age", "25")
 *
 * // Use as Map.Entry - 作为 Map.Entry 使用
 * Map<String, Integer> map = Map.ofEntries(Pair.of("a", 1), Pair.of("b", 2));
 * }</pre>
 *
 * @param <A> the type of the first element - 第一个元素的类型
 * @param <B> the type of the second element - 第二个元素的类型
 * @param first  the first element - 第一个元素
 * @param second the second element - 第二个元素
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Triple
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public record Pair<A, B>(A first, B second) implements Map.Entry<A, B>, Serializable {

    // ---- Map.Entry contract: hashCode and equals | Map.Entry 契约：hashCode 和 equals ----

    /**
     * Returns a hash code consistent with the {@link Map.Entry} contract:
     * {@code Objects.hashCode(getKey()) ^ Objects.hashCode(getValue())}.
     * 返回与 {@link Map.Entry} 契约一致的哈希码。
     *
     * @return the hash code | 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(first) ^ Objects.hashCode(second);
    }

    /**
     * Compares this Pair with another object for equality, compatible with
     * the {@link Map.Entry} contract. Returns {@code true} if the other object
     * is a {@link Map.Entry} with equal key and value.
     * 将此 Pair 与另一个对象进行相等性比较，兼容 {@link Map.Entry} 契约。
     *
     * @param o the object to compare | 要比较的对象
     * @return true if equal | 如果相等返回 true
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Map.Entry<?, ?> e) {
            return Objects.equals(first, e.getKey()) && Objects.equals(second, e.getValue());
        }
        return false;
    }

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new Pair with the given values.
     * 使用给定的值创建新的二元组。
     *
     * @param first  the first element (nullable) - 第一个元素（可为 null）
     * @param second the second element (nullable) - 第二个元素（可为 null）
     * @param <A>    the type of the first element - 第一个元素的类型
     * @param <B>    the type of the second element - 第二个元素的类型
     * @return a new Pair - 新的二元组
     */
    public static <A, B> Pair<A, B> of(A first, B second) {
        return new Pair<>(first, second);
    }

    /**
     * Creates a Pair from a {@link Map.Entry}.
     * 从 {@link Map.Entry} 创建二元组。
     *
     * @param entry the map entry - Map 条目
     * @param <A>   the type of the key - 键的类型
     * @param <B>   the type of the value - 值的类型
     * @return a new Pair containing the entry's key and value - 包含条目键值的新二元组
     * @throws NullPointerException if entry is null - 如果 entry 为 null
     */
    public static <A, B> Pair<A, B> fromEntry(Map.Entry<A, B> entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        return new Pair<>(entry.getKey(), entry.getValue());
    }

    // ---- Map.Entry implementation | Map.Entry 实现 ----

    /**
     * Returns the first element as the key.
     * 返回第一个元素作为键。
     *
     * @return the first element - 第一个元素
     */
    @Override
    public A getKey() {
        return first;
    }

    /**
     * Returns the second element as the value.
     * 返回第二个元素作为值。
     *
     * @return the second element - 第二个元素
     */
    @Override
    public B getValue() {
        return second;
    }

    /**
     * Always throws {@link UnsupportedOperationException} since Pair is immutable.
     * 始终抛出 {@link UnsupportedOperationException}，因为 Pair 是不可变的。
     *
     * @param value ignored - 忽略
     * @return never returns - 永远不返回
     * @throws UnsupportedOperationException always - 始终抛出
     */
    @Override
    public B setValue(B value) {
        throw new UnsupportedOperationException("Pair is immutable");
    }

    // ---- Transformation methods | 变换方法 ----

    /**
     * Returns a new Pair with the elements swapped.
     * 返回一个交换了元素的新二元组。
     *
     * @return a new Pair with first and second swapped - 交换了第一和第二个元素的新二元组
     */
    public Pair<B, A> swap() {
        return new Pair<>(second, first);
    }

    /**
     * Transforms the first element using the given function.
     * 使用给定函数变换第一个元素。
     *
     * @param fn  the mapping function - 映射函数
     * @param <C> the type of the new first element - 新的第一个元素的类型
     * @return a new Pair with the transformed first element - 变换了第一个元素的新二元组
     * @throws NullPointerException if fn is null - 如果 fn 为 null
     */
    public <C> Pair<C, B> mapFirst(Function<? super A, ? extends C> fn) {
        Objects.requireNonNull(fn, "fn must not be null");
        return new Pair<>(fn.apply(first), second);
    }

    /**
     * Transforms the second element using the given function.
     * 使用给定函数变换第二个元素。
     *
     * @param fn  the mapping function - 映射函数
     * @param <C> the type of the new second element - 新的第二个元素的类型
     * @return a new Pair with the transformed second element - 变换了第二个元素的新二元组
     * @throws NullPointerException if fn is null - 如果 fn 为 null
     */
    public <C> Pair<A, C> mapSecond(Function<? super B, ? extends C> fn) {
        Objects.requireNonNull(fn, "fn must not be null");
        return new Pair<>(first, fn.apply(second));
    }

    /**
     * Applies a bi-function to both elements and returns the result.
     * 对两个元素应用双参数函数并返回结果。
     *
     * @param fn  the mapping function - 映射函数
     * @param <C> the result type - 结果类型
     * @return the result of applying the function - 函数应用的结果
     * @throws NullPointerException if fn is null - 如果 fn 为 null
     */
    public <C> C map(BiFunction<? super A, ? super B, ? extends C> fn) {
        Objects.requireNonNull(fn, "fn must not be null");
        return fn.apply(first, second);
    }
}
