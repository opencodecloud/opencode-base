package cloud.opencode.base.money;

import cloud.opencode.base.money.exception.CurrencyMismatchException;

import java.util.Objects;

/**
 * MoneyRange - Immutable closed monetary range [min, max]
 * 金额区间 - 不可变闭区间 [最小值, 最大值]
 *
 * <p>Represents a closed range of monetary amounts with the same currency.
 * Useful for price ranges, budget limits, salary bands, and validation.</p>
 * <p>表示相同币种的闭区间金额范围。适用于价格区间、预算限制、薪资范围和验证。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create range
 * MoneyRange range = MoneyRange.of(Money.of("10"), Money.of("100"));
 *
 * // Contains check
 * range.contains(Money.of("50"));   // true
 * range.contains(Money.of("150"));  // false
 *
 * // Clamp to range
 * range.clamp(Money.of("150"));     // ¥100.00
 *
 * // Range operations
 * MoneyRange other = MoneyRange.of(Money.of("80"), Money.of("200"));
 * range.overlaps(other);            // true
 * range.intersection(other);        // [¥80.00, ¥100.00]
 * range.span(other);                // [¥10.00, ¥200.00]
 * range.gap(other);                 // empty (overlapping)
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Closed range [min, max] with same-currency enforcement - 同币种闭区间</li>
 *   <li>Contains, clamp, overlaps, intersection, span, gap - 包含、夹紧、重叠、交集、跨度、间隙</li>
 *   <li>Immutable and thread-safe - 不可变且线程安全</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No, min and max must not be null - 空值安全: 否，最小值和最大值不可为null</li>
 * </ul>
 *
 * @param min the lower bound (inclusive) | 下界（包含）
 * @param max the upper bound (inclusive) | 上界（包含）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.3
 */
public record MoneyRange(Money min, Money max) {

    /**
     * Canonical constructor with validation
     * 规范构造器（带验证）
     */
    public MoneyRange {
        Objects.requireNonNull(min, "Min must not be null");
        Objects.requireNonNull(max, "Max must not be null");
        if (!min.currency().equals(max.currency())) {
            throw new CurrencyMismatchException(min.currency(), max.currency());
        }
        if (min.amount().compareTo(max.amount()) > 0) {
            throw new IllegalArgumentException(
                    String.format("Min (%s) must not be greater than max (%s)", min, max));
        }
    }

    /**
     * Create a money range
     * 创建金额区间
     *
     * @param min the lower bound | 下界
     * @param max the upper bound | 上界
     * @return the money range | 金额区间
     */
    public static MoneyRange of(Money min, Money max) {
        return new MoneyRange(min, max);
    }

    /**
     * Create a single-point range where min == max
     * 创建单点区间（min == max）
     *
     * @param value the single value | 单一值
     * @return the money range | 金额区间
     */
    public static MoneyRange singleton(Money value) {
        Objects.requireNonNull(value, "Value must not be null");
        return new MoneyRange(value, value);
    }

    /**
     * Get the currency of this range
     * 获取此区间的货币
     *
     * @return the currency | 货币
     */
    public Currency currency() {
        return min.currency();
    }

    /**
     * Check if this range contains the given money
     * 检查此区间是否包含给定金额
     *
     * @param money the money to check | 要检查的金额
     * @return true if contained | 如果包含返回true
     * @throws CurrencyMismatchException if currencies differ | 如果币种不同
     */
    public boolean contains(Money money) {
        Objects.requireNonNull(money, "Money must not be null");
        assertSameCurrency(money);
        return money.amount().compareTo(min.amount()) >= 0
                && money.amount().compareTo(max.amount()) <= 0;
    }

    /**
     * Check if this range fully contains another range
     * 检查此区间是否完全包含另一个区间
     *
     * @param other the other range | 另一个区间
     * @return true if this range contains the other entirely | 如果完全包含返回true
     * @throws CurrencyMismatchException if currencies differ | 如果币种不同
     */
    public boolean contains(MoneyRange other) {
        Objects.requireNonNull(other, "Other range must not be null");
        assertSameCurrency(other.min);
        return this.min.amount().compareTo(other.min.amount()) <= 0
                && this.max.amount().compareTo(other.max.amount()) >= 0;
    }

    /**
     * Clamp the given money to this range
     * 将给定金额夹紧到此区间
     *
     * @param money the money to clamp | 要夹紧的金额
     * @return the clamped money | 夹紧后的金额
     * @throws CurrencyMismatchException if currencies differ | 如果币种不同
     */
    public Money clamp(Money money) {
        Objects.requireNonNull(money, "Money must not be null");
        assertSameCurrency(money);
        return money.clamp(min, max);
    }

    /**
     * Check if this range overlaps with another range
     * 检查此区间是否与另一个区间重叠
     *
     * @param other the other range | 另一个区间
     * @return true if overlapping | 如果重叠返回true
     */
    public boolean overlaps(MoneyRange other) {
        Objects.requireNonNull(other, "Other range must not be null");
        assertSameCurrency(other.min);
        return this.min.amount().compareTo(other.max.amount()) <= 0
                && other.min.amount().compareTo(this.max.amount()) <= 0;
    }

    /**
     * Get the intersection of this range with another
     * 获取此区间与另一个区间的交集
     *
     * @param other the other range | 另一个区间
     * @return the intersection range, or null if no overlap | 交集区间，无重叠时返回null
     * @throws CurrencyMismatchException if currencies differ | 如果币种不同
     */
    public MoneyRange intersection(MoneyRange other) {
        Objects.requireNonNull(other, "Other range must not be null");
        if (!overlaps(other)) {
            return null;
        }
        Money lo = this.min.amount().compareTo(other.min.amount()) >= 0 ? this.min : other.min;
        Money hi = this.max.amount().compareTo(other.max.amount()) <= 0 ? this.max : other.max;
        return new MoneyRange(lo, hi);
    }

    /**
     * Get the span (union bounding range) of this range with another
     * 获取此区间与另一个区间的跨度（并集外包区间）
     *
     * @param other the other range | 另一个区间
     * @return the spanning range | 跨度区间
     */
    public MoneyRange span(MoneyRange other) {
        Objects.requireNonNull(other, "Other range must not be null");
        assertSameCurrency(other.min);
        Money lo = this.min.amount().compareTo(other.min.amount()) <= 0 ? this.min : other.min;
        Money hi = this.max.amount().compareTo(other.max.amount()) >= 0 ? this.max : other.max;
        return new MoneyRange(lo, hi);
    }

    /**
     * Get the gap between this range and another (non-overlapping space)
     * 获取此区间与另一个区间之间的间隙
     *
     * @param other the other range | 另一个区间
     * @return the gap range, or null if ranges overlap or are adjacent | 间隙区间，重叠或相邻时返回null
     */
    public MoneyRange gap(MoneyRange other) {
        Objects.requireNonNull(other, "Other range must not be null");
        assertSameCurrency(other.min);
        if (overlaps(other)) {
            return null;
        }
        MoneyRange lower = this.min.amount().compareTo(other.min.amount()) < 0 ? this : other;
        MoneyRange upper = lower == this ? other : this;
        // gap is between lower.max and upper.min
        if (lower.max.amount().compareTo(upper.min.amount()) >= 0) {
            return null; // adjacent or overlapping
        }
        return new MoneyRange(lower.max, upper.min);
    }

    /**
     * Check if this range is a single point (min equals max)
     * 检查此区间是否为单点（min 等于 max）
     *
     * @return true if singleton | 如果为单点返回true
     */
    public boolean isSingleton() {
        return min.amount().compareTo(max.amount()) == 0;
    }

    /**
     * Get the width (difference) of this range
     * 获取此区间的宽度（差值）
     *
     * @return the width as Money | 宽度金额
     */
    public Money width() {
        return max.subtract(min);
    }

    /**
     * Get the midpoint of this range
     * 获取此区间的中点
     *
     * @return the midpoint | 中点金额
     */
    public Money midpoint() {
        return min.add(max).divide(2);
    }

    private void assertSameCurrency(Money money) {
        if (!min.currency().equals(money.currency())) {
            throw new CurrencyMismatchException(min.currency(), money.currency());
        }
    }

    @Override
    public String toString() {
        return "[" + min.format() + ", " + max.format() + "]";
    }
}
