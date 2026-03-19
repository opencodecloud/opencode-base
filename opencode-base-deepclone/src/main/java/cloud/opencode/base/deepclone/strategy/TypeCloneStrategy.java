package cloud.opencode.base.deepclone.strategy;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Type-specific clone strategy configuration
 * 特定类型的克隆策略配置
 *
 * <p>Defines how a specific type should be cloned, allowing custom cloning
 * functions for types that require special handling.</p>
 * <p>定义特定类型应如何克隆，允许为需要特殊处理的类型提供自定义克隆函数。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Register a custom cloner for Money type
 * TypeCloneStrategy<Money> strategy = TypeCloneStrategy.deep(
 *     Money.class,
 *     m -> new Money(m.getAmount(), m.getCurrency())
 * );
 *
 * // Mark a type as immutable (no cloning needed)
 * TypeCloneStrategy<LocalDate> immutable = TypeCloneStrategy.immutable(LocalDate.class);
 *
 * // Use shallow copy for a type
 * TypeCloneStrategy<SharedResource> shallow = TypeCloneStrategy.shallow(SharedResource.class);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-specific clone functions - 特定类型的克隆函数</li>
 *   <li>Immutable type support - 不可变类型支持</li>
 *   <li>Deep and shallow clone modes - 深拷贝和浅拷贝模式</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 * @param <T> the type this strategy applies to | 此策略适用的类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public record TypeCloneStrategy<T>(
        Class<T> type,
        UnaryOperator<T> cloner,
        boolean deepClone
) {

    /**
     * Creates a TypeCloneStrategy
     * 创建类型克隆策略
     *
     * @param type      the type | 类型
     * @param cloner    the cloning function | 克隆函数
     * @param deepClone whether this is a deep clone | 是否为深度克隆
     */
    public TypeCloneStrategy {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(cloner, "cloner must not be null");
    }

    /**
     * Creates a deep clone strategy with a custom cloner
     * 使用自定义克隆器创建深度克隆策略
     *
     * @param type   the type | 类型
     * @param cloner the cloning function | 克隆函数
     * @param <T>    the type parameter | 类型参数
     * @return the strategy | 策略
     */
    public static <T> TypeCloneStrategy<T> deep(Class<T> type, UnaryOperator<T> cloner) {
        return new TypeCloneStrategy<>(type, cloner, true);
    }

    /**
     * Creates a shallow copy strategy (returns same reference)
     * 创建浅拷贝策略（返回相同引用）
     *
     * @param type the type | 类型
     * @param <T>  the type parameter | 类型参数
     * @return the strategy | 策略
     */
    public static <T> TypeCloneStrategy<T> shallow(Class<T> type) {
        return new TypeCloneStrategy<>(type, UnaryOperator.identity(), false);
    }

    /**
     * Creates an immutable strategy (returns same reference)
     * 创建不可变策略（返回相同引用）
     *
     * <p>Same as shallow(), used for semantic clarity when dealing with immutable types.</p>
     * <p>与shallow()相同，用于处理不可变类型时语义更清晰。</p>
     *
     * @param type the type | 类型
     * @param <T>  the type parameter | 类型参数
     * @return the strategy | 策略
     */
    public static <T> TypeCloneStrategy<T> immutable(Class<T> type) {
        return shallow(type);
    }

    /**
     * Clones an object using this strategy
     * 使用此策略克隆对象
     *
     * @param original the original object | 原始对象
     * @return the cloned object | 克隆的对象
     */
    public T apply(T original) {
        if (original == null) {
            return null;
        }
        return cloner.apply(original);
    }
}
