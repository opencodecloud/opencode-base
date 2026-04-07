package cloud.opencode.base.collections;

import java.util.Comparator;

/**
 * ComparisonChain - Fluent comparator chain for multi-field comparisons
 * ComparisonChain - 用于多字段比较的流式比较器链
 *
 * <p>A utility for performing chained comparisons within {@code compareTo()} methods.
 * Once a non-zero comparison result is found, all subsequent comparisons are short-circuited.</p>
 * <p>用于在 {@code compareTo()} 方法中执行链式比较的工具类。
 * 一旦找到非零比较结果，所有后续比较将被短路跳过。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Short-circuit evaluation on first difference - 第一个差异处短路求值</li>
 *   <li>Supports all primitives (int, long, float, double, boolean) - 支持所有基本类型</li>
 *   <li>Custom Comparator support - 支持自定义 Comparator</li>
 *   <li>Boolean ordering: true-first or false-first - 布尔排序: true 优先或 false 优先</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Multi-field comparison in compareTo() - compareTo() 中的多字段比较
 * public int compareTo(Person other) {
 *     return ComparisonChain.start()
 *         .compare(this.lastName, other.lastName)
 *         .compare(this.firstName, other.firstName)
 *         .compare(this.age, other.age)
 *         .result();
 * }
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per chained call after short-circuit - 短路后每次链式调用 O(1)</li>
 *   <li>No heap allocations after short-circuit (singleton reuse) - 短路后无堆分配（单例复用）</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable singletons) - 线程安全: 是（不可变单例）</li>
 *   <li>Null-safe: No (null arguments cause NullPointerException) - 空值安全: 否（null 参数导致 NullPointerException）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public abstract class ComparisonChain {

    private ComparisonChain() {}

    /**
     * Starts a new comparison chain.
     * 启动一个新的比较链。
     *
     * @return the active comparison chain - 活动的比较链
     */
    public static ComparisonChain start() {
        return ACTIVE;
    }

    /**
     * Compares two {@link Comparable} values using their natural ordering.
     * 使用自然排序比较两个 {@link Comparable} 值。
     *
     * @param left  the left value - 左值
     * @param right the right value - 右值
     * @return this chain for fluent chaining - 用于流式链接的此链
     */
    public abstract ComparisonChain compare(Comparable<?> left, Comparable<?> right);

    /**
     * Compares two values using the specified {@link Comparator}.
     * 使用指定的 {@link Comparator} 比较两个值。
     *
     * @param <T>        the type of values being compared - 被比较值的类型
     * @param left       the left value - 左值
     * @param right      the right value - 右值
     * @param comparator the comparator to use - 使用的比较器
     * @return this chain for fluent chaining - 用于流式链接的此链
     */
    public abstract <T> ComparisonChain compare(T left, T right, Comparator<T> comparator);

    /**
     * Compares two {@code int} values.
     * 比较两个 {@code int} 值。
     *
     * @param left  the left value - 左值
     * @param right the right value - 右值
     * @return this chain for fluent chaining - 用于流式链接的此链
     */
    public abstract ComparisonChain compare(int left, int right);

    /**
     * Compares two {@code long} values.
     * 比较两个 {@code long} 值。
     *
     * @param left  the left value - 左值
     * @param right the right value - 右值
     * @return this chain for fluent chaining - 用于流式链接的此链
     */
    public abstract ComparisonChain compare(long left, long right);

    /**
     * Compares two {@code double} values.
     * 比较两个 {@code double} 值。
     *
     * @param left  the left value - 左值
     * @param right the right value - 右值
     * @return this chain for fluent chaining - 用于流式链接的此链
     */
    public abstract ComparisonChain compare(double left, double right);

    /**
     * Compares two {@code float} values.
     * 比较两个 {@code float} 值。
     *
     * @param left  the left value - 左值
     * @param right the right value - 右值
     * @return this chain for fluent chaining - 用于流式链接的此链
     */
    public abstract ComparisonChain compare(float left, float right);

    /**
     * Compares two booleans, sorting {@code true} before {@code false}.
     * 比较两个布尔值，{@code true} 排在 {@code false} 之前。
     *
     * @param left  the left value - 左值
     * @param right the right value - 右值
     * @return this chain for fluent chaining - 用于流式链接的此链
     */
    public abstract ComparisonChain compareTrueFirst(boolean left, boolean right);

    /**
     * Compares two booleans, sorting {@code false} before {@code true}.
     * 比较两个布尔值，{@code false} 排在 {@code true} 之前。
     *
     * @param left  the left value - 左值
     * @param right the right value - 右值
     * @return this chain for fluent chaining - 用于流式链接的此链
     */
    public abstract ComparisonChain compareFalseFirst(boolean left, boolean right);

    /**
     * Returns the final comparison result.
     * 返回最终比较结果。
     *
     * @return a negative integer, zero, or a positive integer as the first
     *         differing comparison was less than, equal to, or greater than
     *         负整数、零或正整数，取决于第一个不同比较的结果
     */
    public abstract int result();

    private static final ComparisonChain ACTIVE = new ComparisonChain() {

        @SuppressWarnings("unchecked")
        @Override
        public ComparisonChain compare(Comparable<?> left, Comparable<?> right) {
            return classify(((Comparable<Object>) left).compareTo(right));
        }

        @Override
        public <T> ComparisonChain compare(T left, T right, Comparator<T> comparator) {
            return classify(comparator.compare(left, right));
        }

        @Override
        public ComparisonChain compare(int left, int right) {
            return classify(Integer.compare(left, right));
        }

        @Override
        public ComparisonChain compare(long left, long right) {
            return classify(Long.compare(left, right));
        }

        @Override
        public ComparisonChain compare(double left, double right) {
            return classify(Double.compare(left, right));
        }

        @Override
        public ComparisonChain compare(float left, float right) {
            return classify(Float.compare(left, right));
        }

        @Override
        public ComparisonChain compareTrueFirst(boolean left, boolean right) {
            return classify(Boolean.compare(right, left)); // true before false
        }

        @Override
        public ComparisonChain compareFalseFirst(boolean left, boolean right) {
            return classify(Boolean.compare(left, right)); // false before true
        }

        @Override
        public int result() {
            return 0;
        }

        private ComparisonChain classify(int result) {
            return result < 0 ? LESS : result > 0 ? GREATER : ACTIVE;
        }
    };

    private static final ComparisonChain LESS = new InactiveChain(-1);
    private static final ComparisonChain GREATER = new InactiveChain(1);

    /**
     * A chain that has already determined the comparison result.
     * All further comparisons are no-ops.
     * 已确定比较结果的链。所有后续比较都是空操作。
     */
    private static final class InactiveChain extends ComparisonChain {

        private final int result;

        InactiveChain(int result) {
            this.result = result;
        }

        @Override
        public ComparisonChain compare(Comparable<?> left, Comparable<?> right) {
            return this;
        }

        @Override
        public <T> ComparisonChain compare(T left, T right, Comparator<T> comparator) {
            return this;
        }

        @Override
        public ComparisonChain compare(int left, int right) {
            return this;
        }

        @Override
        public ComparisonChain compare(long left, long right) {
            return this;
        }

        @Override
        public ComparisonChain compare(double left, double right) {
            return this;
        }

        @Override
        public ComparisonChain compare(float left, float right) {
            return this;
        }

        @Override
        public ComparisonChain compareTrueFirst(boolean left, boolean right) {
            return this;
        }

        @Override
        public ComparisonChain compareFalseFirst(boolean left, boolean right) {
            return this;
        }

        @Override
        public int result() {
            return result;
        }
    }
}
