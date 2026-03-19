package cloud.opencode.base.collections;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Equivalence - Strategy for determining equivalence between objects
 * Equivalence - 确定对象等价性的策略
 *
 * <p>An equivalence determines whether two objects are considered equivalent.
 * This is a generalization of {@link Object#equals}, allowing custom equivalence relations.</p>
 * <p>等价关系确定两个对象是否被视为等价。这是 {@link Object#equals} 的泛化，允许自定义等价关系。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Natural equivalence (using equals)
 * Equivalence<String> natural = Equivalence.equals();
 * natural.equivalent("a", "a"); // true
 *
 * // Identity equivalence (using ==)
 * Equivalence<String> identity = Equivalence.identity();
 *
 * // Custom equivalence
 * Equivalence<String> caseInsensitive = Equivalence.from(
 *     String::equalsIgnoreCase,
 *     s -> s.toLowerCase().hashCode()
 * );
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Custom equivalence relations - 自定义等价关系</li>
 *   <li>Natural equals and identity equivalence - 自然equals和引用相等</li>
 *   <li>Custom hash function support - 自定义哈希函数支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 是（不可变）</li>
 *   <li>Null-safe: Yes (handles null elements) - 是（处理null元素）</li>
 * </ul>
 * @param <T> the type of objects compared by this equivalence | 此等价关系比较的对象类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public abstract class Equivalence<T> {

    /**
     * Returns the default equivalence, using {@link Object#equals}.
     * 返回默认等价关系，使用 {@link Object#equals}。
     *
     * @param <T> the type | 类型
     * @return the equals-based equivalence | 基于 equals 的等价关系
     */
    public static <T> Equivalence<T> equals() {
        @SuppressWarnings("unchecked")
        Equivalence<T> result = (Equivalence<T>) EqualsEquivalence.INSTANCE;
        return result;
    }

    /**
     * Returns the identity equivalence, using {@code ==}.
     * 返回身份等价关系，使用 {@code ==}。
     *
     * @param <T> the type | 类型
     * @return the identity equivalence | 身份等价关系
     */
    public static <T> Equivalence<T> identity() {
        @SuppressWarnings("unchecked")
        Equivalence<T> result = (Equivalence<T>) IdentityEquivalence.INSTANCE;
        return result;
    }

    /**
     * Creates an equivalence from a predicate and hash function.
     * 从谓词和哈希函数创建等价关系。
     *
     * @param <T>          the type | 类型
     * @param predicate    the equivalence predicate | 等价谓词
     * @param hashFunction the hash function | 哈希函数
     * @return a new equivalence | 新的等价关系
     */
    public static <T> Equivalence<T> from(BiPredicate<? super T, ? super T> predicate,
                                           java.util.function.ToIntFunction<? super T> hashFunction) {
        return new FunctionalEquivalence<>(predicate, hashFunction);
    }

    /**
     * Determines whether the two given objects are equivalent.
     * 确定两个给定对象是否等价。
     *
     * @param a first object (may be null) | 第一个对象（可为 null）
     * @param b second object (may be null) | 第二个对象（可为 null）
     * @return true if equivalent | 如果等价则返回 true
     */
    public final boolean equivalent(T a, T b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return doEquivalent(a, b);
    }

    /**
     * Implementation of equivalence check for non-null values.
     * 非空值等价检查的实现。
     *
     * @param a first non-null object | 第一个非空对象
     * @param b second non-null object | 第二个非空对象
     * @return true if equivalent | 如果等价则返回 true
     */
    protected abstract boolean doEquivalent(T a, T b);

    /**
     * Returns a hash code for the given object.
     * 返回给定对象的哈希码。
     *
     * @param t the object (may be null) | 对象（可为 null）
     * @return the hash code | 哈希码
     */
    public final int hash(T t) {
        if (t == null) {
            return 0;
        }
        return doHash(t);
    }

    /**
     * Implementation of hash code for non-null values.
     * 非空值哈希码的实现。
     *
     * @param t the non-null object | 非空对象
     * @return the hash code | 哈希码
     */
    protected abstract int doHash(T t);

    // ==================== 内部实现 | Internal Implementations ====================

    private static final class EqualsEquivalence extends Equivalence<Object> {
        static final EqualsEquivalence INSTANCE = new EqualsEquivalence();

        @Override
        protected boolean doEquivalent(Object a, Object b) {
            return a.equals(b);
        }

        @Override
        protected int doHash(Object t) {
            return t.hashCode();
        }
    }

    private static final class IdentityEquivalence extends Equivalence<Object> {
        static final IdentityEquivalence INSTANCE = new IdentityEquivalence();

        @Override
        protected boolean doEquivalent(Object a, Object b) {
            return false; // a == b already handled in equivalent()
        }

        @Override
        protected int doHash(Object t) {
            return System.identityHashCode(t);
        }
    }

    private static final class FunctionalEquivalence<T> extends Equivalence<T> {
        private final BiPredicate<? super T, ? super T> predicate;
        private final java.util.function.ToIntFunction<? super T> hashFunction;

        FunctionalEquivalence(BiPredicate<? super T, ? super T> predicate,
                              java.util.function.ToIntFunction<? super T> hashFunction) {
            this.predicate = Objects.requireNonNull(predicate);
            this.hashFunction = Objects.requireNonNull(hashFunction);
        }

        @Override
        protected boolean doEquivalent(T a, T b) {
            return predicate.test(a, b);
        }

        @Override
        protected int doHash(T t) {
            return hashFunction.applyAsInt(t);
        }
    }
}
