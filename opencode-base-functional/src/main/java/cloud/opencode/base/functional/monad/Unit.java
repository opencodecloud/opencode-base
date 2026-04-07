package cloud.opencode.base.functional.monad;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Unit - Represents a valueless result (void equivalent as a type)
 * Unit - 表示无值结果（void 的类型等价物）
 *
 * <p>Use when a generic API requires a type parameter but the actual value is irrelevant.
 * Unlike {@link Void} (which cannot be instantiated), Unit has exactly one value: {@link #INSTANCE}.</p>
 * <p>当泛型 API 需要类型参数但实际值无关时使用。
 * 与 {@link Void}（不可实例化）不同，Unit 恰好有一个值：{@link #INSTANCE}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Singleton enum value - 单例枚举值</li>
 *   <li>Type-safe void replacement - 类型安全的 void 替代</li>
 *   <li>Utility methods for functional pipelines - 函数式管道的实用方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // As a type parameter for generic APIs
 * // 作为泛型 API 的类型参数
 * CompletableFuture<Unit> future = runAsync(() -> doWork())
 *     .thenApply(Unit.ignore());
 *
 * // As a supplier
 * // 作为供应者
 * Supplier<Unit> supplier = Unit.supplier();
 *
 * // Discard function result
 * // 丢弃函数结果
 * Function<String, Unit> discard = Unit.ignore();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum singleton) - 线程安全: 是 (枚举单例)</li>
 *   <li>Serializable: Yes (enum) - 可序列化: 是 (枚举)</li>
 *   <li>Null-safe: Yes (always returns INSTANCE) - 空值安全: 是 (始终返回 INSTANCE)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.3
 */
public enum Unit {

    /**
     * The single Unit value
     * 唯一的 Unit 值
     */
    INSTANCE;

    /**
     * Return a function that discards its input and returns Unit
     * 返回一个丢弃输入并返回 Unit 的函数
     *
     * <p>Useful for converting any value-producing operation into a Unit-producing one.</p>
     * <p>用于将任何产生值的操作转换为产生 Unit 的操作。</p>
     *
     * @param <T> input type (ignored) - 输入类型（被忽略）
     * @return function that always returns INSTANCE - 始终返回 INSTANCE 的函数
     */
    public static <T> Function<T, Unit> ignore() {
        return t -> INSTANCE;
    }

    /**
     * Return a supplier that always supplies Unit
     * 返回一个始终提供 Unit 的供应者
     *
     * @return supplier of Unit - Unit 的供应者
     */
    public static Supplier<Unit> supplier() {
        return () -> INSTANCE;
    }

    /**
     * Returns "()" as the string representation of Unit
     * 返回 "()" 作为 Unit 的字符串表示
     *
     * @return "()" - "()"
     */
    @Override
    public String toString() {
        return "()";
    }
}
