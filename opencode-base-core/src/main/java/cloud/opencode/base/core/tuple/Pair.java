package cloud.opencode.base.core.tuple;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Pair - Immutable two-element tuple (Record implementation)
 * 二元组 - 不可变的两元素元组（Record 实现）
 *
 * <p>Immutable key-value pair container for returning two related values.</p>
 * <p>不可变的键值对容器，可用于返回两个相关值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable storage for two values - 不可变的双值存储</li>
 *   <li>Multiple aliases (left/right, first/second, key/value) - 多种别名访问</li>
 *   <li>Element mapping and transformation - 元素映射和转换</li>
 *   <li>Map.Entry interoperability - 与 Map.Entry 互操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Pair<String, Integer> pair = Pair.of("name", 25);
 * String key = pair.left();
 * Pair<String, String> mapped = pair.mapRight(String::valueOf);
 * Map.Entry<String, Integer> entry = pair.toEntry();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是 (不可变 Record)</li>
 *   <li>Null-safe: Allows null values - 空值安全: 允许 null 值</li>
 * </ul>
 *
 * @param left  left value (first element) - 左值（第一个元素）
 * @param right right value (second element) - 右值（第二个元素）
 * @param <L>   left value type - 左值类型
 * @param <R>   right value type - 右值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public record Pair<L, R>(L left, R right) implements Serializable {

    /**
     * Creates a Pair
     * 创建二元组
     *
     * @param left  left value | 左值
     * @param right right value | 右值
     * @param <L>   left value type | 左值类型
     * @param <R>   right value type | 右值类型
     * @return the Pair | 二元组
     */
    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    /**
     * Creates a Pair from Map.Entry
     * 从 Map.Entry 创建二元组
     *
     * @param entry Map.Entry
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return the Pair | 二元组
     */
    public static <K, V> Pair<K, V> fromEntry(Map.Entry<K, V> entry) {
        return new Pair<>(entry.getKey(), entry.getValue());
    }

    /**
     * Creates an empty Pair
     * 创建空二元组
     *
     * @param <L> left value type | 左值类型
     * @param <R> right value type | 右值类型
     * @return an empty Pair | 空二元组
     */
    public static <L, R> Pair<L, R> empty() {
        return new Pair<>(null, null);
    }

    /**
     * Gets the first element (alias for left)
     * 获取第一个元素（左值的别名）
     *
     * @return left value | 左值
     */
    public L first() {
        return left;
    }

    /**
     * Gets the second element (alias for right)
     * 获取第二个元素（右值的别名）
     *
     * @return right value | 右值
     */
    public R second() {
        return right;
    }

    /**
     * Gets the key (alias for left, for Map scenarios)
     * 获取键（左值的别名，用于 Map 场景）
     *
     * @return left value | 左值
     */
    public L key() {
        return left;
    }

    /**
     * Gets the value (alias for right, for Map scenarios)
     * 获取值（右值的别名，用于 Map 场景）
     *
     * @return right value | 右值
     */
    public R value() {
        return right;
    }

    /**
     * Swaps left and right values
     * 交换左右值
     *
     * @return the swapped Pair | 交换后的二元组
     */
    public Pair<R, L> swap() {
        return new Pair<>(right, left);
    }

    /**
     * Maps the left value
     * 映射左值
     *
     * @param mapper the mapper function | 映射函数
     * @param <T>    new left type | 新左值类型
     * @return a new Pair | 新二元组
     */
    public <T> Pair<T, R> mapLeft(Function<? super L, ? extends T> mapper) {
        return new Pair<>(mapper.apply(left), right);
    }

    /**
     * Maps the right value
     * 映射右值
     *
     * @param mapper the mapper function | 映射函数
     * @param <T>    new right type | 新右值类型
     * @return a new Pair | 新二元组
     */
    public <T> Pair<L, T> mapRight(Function<? super R, ? extends T> mapper) {
        return new Pair<>(left, mapper.apply(right));
    }

    /**
     * Maps both left and right values
     * 同时映射左右值
     *
     * @param leftMapper  left mapper function | 左值映射函数
     * @param rightMapper right mapper function | 右值映射函数
     * @param <T>         new left type | 新左值类型
     * @param <U>         new right type | 新右值类型
     * @return a new Pair | 新二元组
     */
    public <T, U> Pair<T, U> map(Function<? super L, ? extends T> leftMapper,
                                  Function<? super R, ? extends U> rightMapper) {
        return new Pair<>(leftMapper.apply(left), rightMapper.apply(right));
    }

    /**
     * Applies a bi-function
     * 应用二元函数
     *
     * @param function the bi-function | 二元函数
     * @param <T>      result type | 结果类型
     * @return the function result | 函数结果
     */
    public <T> T apply(BiFunction<? super L, ? super R, ? extends T> function) {
        return function.apply(left, right);
    }

    /**
     * Checks if it contains a null value
     * 检查是否包含 null 值
     *
     * @return true if any value is null | 如果任一值为 null 返回 true
     */
    public boolean hasNull() {
        return left == null || right == null;
    }

    /**
     * Checks if all values are non-null
     * 检查是否都非 null
     *
     * @return true if all values are non-null | 如果都非 null 返回 true
     */
    public boolean allNonNull() {
        return left != null && right != null;
    }

    /**
     * Converts to Map.Entry
     * 转换为 Map.Entry
     *
     * @return Map.Entry
     */
    public Map.Entry<L, R> toEntry() {
        return new AbstractMap.SimpleImmutableEntry<>(left, right);
    }

    /**
     * Converts to an array
     * 转换为数组
     *
     * @return an array containing two elements | 包含两个元素的数组
     */
    public Object[] toArray() {
        return new Object[]{left, right};
    }

    @Override
    public String toString() {
        return "(" + left + ", " + right + ")";
    }
}
