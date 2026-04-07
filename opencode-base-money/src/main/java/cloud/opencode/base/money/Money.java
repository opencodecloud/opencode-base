package cloud.opencode.base.money;

import cloud.opencode.base.money.exception.CurrencyMismatchException;
import cloud.opencode.base.money.format.ChineseUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Money - Immutable monetary amount with currency
 * 金额 - 带货币的不可变金额
 *
 * <p>Immutable record representing a monetary amount with currency.
 * All arithmetic operations return new Money instances.</p>
 * <p>表示带货币的金额的不可变记录。所有算术运算返回新的 Money 实例。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create money
 * Money m1 = Money.of("100.50");
 * Money m2 = Money.ofCents(10050);
 * Money m3 = Money.of("50.25", Currency.USD);
 *
 * // Arithmetic
 * Money sum = m1.add(Money.of("50.00"));
 * Money diff = m1.subtract(Money.of("25.25"));
 * Money product = m1.multiply(2);
 *
 * // Percentage operations
 * Money tax = m1.percent(13);           // 13% of m1
 * Money withTax = m1.addPercent(13);    // m1 + 13%
 * Money sale = m1.subtractPercent(20);  // m1 - 20%
 *
 * // Comparison
 * Money bigger = Money.max(m1, m3);
 * Money clamped = m1.clamp(Money.of("10"), Money.of("80"));
 *
 * // Format
 * System.out.println(m1.format());  // ¥100.50
 * System.out.println(m1.toChineseUpperCase());  // 壹佰元伍角
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable monetary amount with currency - 带货币的不可变金额</li>
 *   <li>Arithmetic: add, subtract, multiply, divide - 算术运算: 加减乘除</li>
 *   <li>Percentage: percent, addPercent, subtractPercent - 百分比运算</li>
 *   <li>Comparison: min, max, clamp - 比较: 最小值、最大值、夹紧</li>
 *   <li>Factory methods: of(), ofCents(), ofMinorUnits(), zero() - 工厂方法</li>
 *   <li>Formatting and Chinese uppercase conversion - 格式化和中文大写转换</li>
 *   <li>Comparable for ordering - 支持排序的Comparable</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No, amount and currency validated non-null - 空值安全: 否，金额和货币验证非null</li>
 * </ul>
 *
 * @param amount the amount | 金额
 * @param currency the currency | 货币
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * Canonical constructor with validation
     * 规范构造器（带验证）
     */
    public Money {
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");
    }

    // ============ Factory Methods | 工厂方法 ============

    /**
     * Create money from BigDecimal and currency
     * 从 BigDecimal 和货币创建金额
     *
     * @param amount the amount | 金额
     * @param currency the currency | 货币
     * @return the money | 金额
     */
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    /**
     * Create money from BigDecimal (default CNY)
     * 从 BigDecimal 创建金额（默认人民币）
     *
     * @param amount the amount | 金额
     * @return the money | 金额
     */
    public static Money of(BigDecimal amount) {
        return new Money(amount, Currency.CNY);
    }

    /**
     * Create money from string and currency
     * 从字符串和货币创建金额
     *
     * @param amount the amount string | 金额字符串
     * @param currency the currency | 货币
     * @return the money | 金额
     */
    public static Money of(String amount, Currency currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    /**
     * Create money from string (default CNY)
     * 从字符串创建金额（默认人民币）
     *
     * @param amount the amount string | 金额字符串
     * @return the money | 金额
     */
    public static Money of(String amount) {
        return of(amount, Currency.CNY);
    }

    /**
     * Create money from long and currency
     * 从长整型和货币创建金额
     *
     * @param amount the amount | 金额
     * @param currency the currency | 货币
     * @return the money | 金额
     */
    public static Money of(long amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    /**
     * Create money from long (default CNY)
     * 从长整型创建金额（默认人民币）
     *
     * @param amount the amount | 金额
     * @return the money | 金额
     */
    public static Money of(long amount) {
        return of(amount, Currency.CNY);
    }

    /**
     * Create money from yuan (CNY)
     * 从元创建金额（人民币）
     *
     * @param yuan the yuan amount | 元
     * @return the money | 金额
     */
    public static Money ofYuan(long yuan) {
        return new Money(BigDecimal.valueOf(yuan), Currency.CNY);
    }

    /**
     * Create money from cents (CNY, 1 yuan = 100 cents)
     * 从分创建金额（人民币，1元 = 100分）
     *
     * @param cents the cents amount | 分
     * @return the money | 金额
     */
    public static Money ofCents(long cents) {
        return new Money(BigDecimal.valueOf(cents, 2), Currency.CNY);
    }

    /**
     * Create money from minor units (smallest currency unit) for any currency
     * 从最小货币单位创建金额（支持任意币种）
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * Money.ofMinorUnits(10050, Currency.CNY)  // ¥100.50
     * Money.ofMinorUnits(10050, Currency.USD)  // $100.50
     * Money.ofMinorUnits(1000, Currency.JPY)   // ¥1000 (JPY has scale 0)
     * }</pre>
     *
     * @param minorUnits the amount in minor units | 最小单位金额
     * @param currency the currency | 货币
     * @return the money | 金额
     * @since V1.0.3
     */
    public static Money ofMinorUnits(long minorUnits, Currency currency) {
        Objects.requireNonNull(currency, "Currency must not be null");
        return new Money(BigDecimal.valueOf(minorUnits, currency.getScale()), currency);
    }

    /**
     * Create zero money with currency
     * 创建零金额
     *
     * @param currency the currency | 货币
     * @return the money | 金额
     */
    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    /**
     * Create zero money (default CNY)
     * 创建零金额（默认人民币）
     *
     * @return the money | 金额
     */
    public static Money zero() {
        return zero(Currency.CNY);
    }

    // ============ Arithmetic Operations | 算术运算 ============

    /**
     * Add money
     * 加法
     *
     * @param other the other money | 另一个金额
     * @return the sum | 和
     * @throws CurrencyMismatchException if currencies differ | 如果币种不同
     */
    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    /**
     * Subtract money
     * 减法
     *
     * @param other the other money | 另一个金额
     * @return the difference | 差
     * @throws CurrencyMismatchException if currencies differ | 如果币种不同
     */
    public Money subtract(Money other) {
        assertSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    /**
     * Multiply by BigDecimal
     * 乘以 BigDecimal
     *
     * @param multiplier the multiplier | 乘数
     * @return the product | 积
     */
    public Money multiply(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "Multiplier must not be null");
        BigDecimal result = amount.multiply(multiplier)
                .setScale(currency.getScale(), RoundingMode.HALF_UP);
        return new Money(result, currency);
    }

    /**
     * Multiply by long
     * 乘以长整型
     *
     * @param multiplier the multiplier | 乘数
     * @return the product | 积
     */
    public Money multiply(long multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    /**
     * Multiply by double
     * 乘以双精度浮点数
     *
     * @param multiplier the multiplier | 乘数
     * @return the product | 积
     */
    public Money multiply(double multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    /**
     * Divide by BigDecimal
     * 除以 BigDecimal
     *
     * @param divisor the divisor | 除数
     * @return the quotient | 商
     * @throws ArithmeticException if divisor is zero | 如果除数为零
     */
    public Money divide(BigDecimal divisor) {
        Objects.requireNonNull(divisor, "Divisor must not be null");
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return new Money(amount.divide(divisor, currency.getScale(), RoundingMode.HALF_UP), currency);
    }

    /**
     * Divide by long
     * 除以长整型
     *
     * @param divisor the divisor | 除数
     * @return the quotient | 商
     */
    public Money divide(long divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }

    /**
     * Divide by double
     * 除以双精度浮点数
     *
     * @param divisor the divisor | 除数
     * @return the quotient | 商
     */
    public Money divide(double divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }

    /**
     * Negate
     * 取反
     *
     * @return the negated money | 取反后的金额
     */
    public Money negate() {
        return new Money(amount.negate(), currency);
    }

    /**
     * Absolute value
     * 绝对值
     *
     * @return the absolute value | 绝对值
     */
    public Money abs() {
        return new Money(amount.abs(), currency);
    }

    /**
     * Round to currency scale
     * 按货币精度四舍五入
     *
     * @return the rounded money | 四舍五入后的金额
     */
    public Money round() {
        return new Money(amount.setScale(currency.getScale(), RoundingMode.HALF_UP), currency);
    }

    // ============ Percentage Operations | 百分比运算 ============

    /**
     * Calculate a percentage of this money
     * 计算此金额的百分比
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * Money.of("100").percent(13)           // ¥13.00 (13% of 100)
     * Money.of("200").percent(new BigDecimal("7.5")) // ¥15.00 (7.5% of 200)
     * }</pre>
     *
     * @param percentRate the percentage rate (e.g. 13 for 13%) | 百分比率（如 13 表示 13%）
     * @return the percentage amount | 百分比金额
     * @since V1.0.3
     */
    public Money percent(BigDecimal percentRate) {
        Objects.requireNonNull(percentRate, "Percent rate must not be null");
        BigDecimal result = amount.multiply(percentRate)
                .divide(HUNDRED, currency.getScale(), RoundingMode.HALF_UP);
        return new Money(result, currency);
    }

    /**
     * Calculate a percentage of this money
     * 计算此金额的百分比
     *
     * @param percentRate the percentage rate (e.g. 13 for 13%) | 百分比率
     * @return the percentage amount | 百分比金额
     * @since V1.0.3
     */
    public Money percent(int percentRate) {
        return percent(BigDecimal.valueOf(percentRate));
    }

    /**
     * Add a percentage to this money (markup)
     * 在此金额上加百分比（加价）
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * Money.of("100").addPercent(13)  // ¥113.00 (100 + 13%)
     * Money.of("200").addPercent(10)  // ¥220.00 (200 + 10%)
     * }</pre>
     *
     * @param percentRate the percentage rate (e.g. 13 for 13%) | 百分比率
     * @return the money with percentage added | 加上百分比后的金额
     * @since V1.0.3
     */
    public Money addPercent(BigDecimal percentRate) {
        return add(percent(percentRate));
    }

    /**
     * Add a percentage to this money (markup)
     * 在此金额上加百分比（加价）
     *
     * @param percentRate the percentage rate (e.g. 13 for 13%) | 百分比率
     * @return the money with percentage added | 加上百分比后的金额
     * @since V1.0.3
     */
    public Money addPercent(int percentRate) {
        return addPercent(BigDecimal.valueOf(percentRate));
    }

    /**
     * Subtract a percentage from this money (discount)
     * 从此金额减去百分比（折扣）
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * Money.of("100").subtractPercent(20)  // ¥80.00 (100 - 20%)
     * Money.of("200").subtractPercent(15)  // ¥170.00 (200 - 15%)
     * }</pre>
     *
     * @param percentRate the percentage rate (e.g. 20 for 20% off) | 百分比率（如 20 表示打八折）
     * @return the money with percentage subtracted | 减去百分比后的金额
     * @since V1.0.3
     */
    public Money subtractPercent(BigDecimal percentRate) {
        return subtract(percent(percentRate));
    }

    /**
     * Subtract a percentage from this money (discount)
     * 从此金额减去百分比（折扣）
     *
     * @param percentRate the percentage rate (e.g. 20 for 20% off) | 百分比率
     * @return the money with percentage subtracted | 减去百分比后的金额
     * @since V1.0.3
     */
    public Money subtractPercent(int percentRate) {
        return subtractPercent(BigDecimal.valueOf(percentRate));
    }

    // ============ Comparison | 比较 ============

    @Override
    public int compareTo(Money other) {
        assertSameCurrency(other);
        return amount.compareTo(other.amount);
    }

    /**
     * Return the greater of two money values
     * 返回两个金额中较大的
     *
     * @param a the first money | 第一个金额
     * @param b the second money | 第二个金额
     * @return the greater money | 较大的金额
     * @throws CurrencyMismatchException if currencies differ | 如果币种不同
     * @since V1.0.3
     */
    public static Money max(Money a, Money b) {
        Objects.requireNonNull(a, "First money must not be null");
        Objects.requireNonNull(b, "Second money must not be null");
        return a.isGreaterOrEqual(b) ? a : b;
    }

    /**
     * Return the lesser of two money values
     * 返回两个金额中较小的
     *
     * @param a the first money | 第一个金额
     * @param b the second money | 第二个金额
     * @return the lesser money | 较小的金额
     * @throws CurrencyMismatchException if currencies differ | 如果币种不同
     * @since V1.0.3
     */
    public static Money min(Money a, Money b) {
        Objects.requireNonNull(a, "First money must not be null");
        Objects.requireNonNull(b, "Second money must not be null");
        return a.isLessOrEqual(b) ? a : b;
    }

    /**
     * Clamp this money to the given range [min, max]
     * 将此金额夹紧到给定范围 [min, max]
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * Money.of("150").clamp(Money.of("10"), Money.of("100"))  // ¥100.00
     * Money.of("5").clamp(Money.of("10"), Money.of("100"))    // ¥10.00
     * Money.of("50").clamp(Money.of("10"), Money.of("100"))   // ¥50.00
     * }</pre>
     *
     * @param min the minimum bound | 最小边界
     * @param max the maximum bound | 最大边界
     * @return the clamped money | 夹紧后的金额
     * @throws CurrencyMismatchException if currencies differ | 如果币种不同
     * @throws IllegalArgumentException if min > max | 如果最小值大于最大值
     * @since V1.0.3
     */
    public Money clamp(Money min, Money max) {
        Objects.requireNonNull(min, "Min must not be null");
        Objects.requireNonNull(max, "Max must not be null");
        assertSameCurrency(min);
        assertSameCurrency(max);
        if (min.amount.compareTo(max.amount) > 0) {
            throw new IllegalArgumentException("Min must not be greater than max");
        }
        if (this.amount.compareTo(min.amount) < 0) {
            return min;
        }
        if (this.amount.compareTo(max.amount) > 0) {
            return max;
        }
        return this;
    }

    /**
     * Check if greater than
     * 是否大于
     *
     * @param other the other money | 另一个金额
     * @return true if greater | 如果大于返回true
     */
    public boolean isGreaterThan(Money other) {
        return compareTo(other) > 0;
    }

    /**
     * Check if less than
     * 是否小于
     *
     * @param other the other money | 另一个金额
     * @return true if less | 如果小于返回true
     */
    public boolean isLessThan(Money other) {
        return compareTo(other) < 0;
    }

    /**
     * Check if greater than or equal
     * 是否大于等于
     *
     * @param other the other money | 另一个金额
     * @return true if greater or equal | 如果大于等于返回true
     */
    public boolean isGreaterOrEqual(Money other) {
        return compareTo(other) >= 0;
    }

    /**
     * Check if less than or equal
     * 是否小于等于
     *
     * @param other the other money | 另一个金额
     * @return true if less or equal | 如果小于等于返回true
     */
    public boolean isLessOrEqual(Money other) {
        return compareTo(other) <= 0;
    }

    /**
     * Check if positive
     * 是否为正
     *
     * @return true if positive | 如果为正返回true
     */
    public boolean isPositive() {
        return amount.signum() > 0;
    }

    /**
     * Check if negative
     * 是否为负
     *
     * @return true if negative | 如果为负返回true
     */
    public boolean isNegative() {
        return amount.signum() < 0;
    }

    /**
     * Check if zero
     * 是否为零
     *
     * @return true if zero | 如果为零返回true
     */
    public boolean isZero() {
        return amount.signum() == 0;
    }

    /**
     * Check if non-negative (zero or positive)
     * 是否非负（零或正数）
     *
     * @return true if non-negative | 如果非负返回true
     * @since V1.0.3
     */
    public boolean isNonNegative() {
        return amount.signum() >= 0;
    }

    /**
     * Check if non-positive (zero or negative)
     * 是否非正（零或负数）
     *
     * @return true if non-positive | 如果非正返回true
     * @since V1.0.3
     */
    public boolean isNonPositive() {
        return amount.signum() <= 0;
    }

    // ============ Conversion | 转换 ============

    /**
     * Convert to cents (smallest unit)
     * 转换为分（最小单位）
     *
     * @return the cents | 分
     * @throws ArithmeticException if amount too large | 如果金额过大
     */
    public long toCents() {
        BigDecimal cents = amount.movePointRight(currency.getScale()).setScale(0, RoundingMode.HALF_UP);
        try {
            return cents.longValueExact();
        } catch (ArithmeticException e) {
            throw new ArithmeticException("Amount too large to convert to cents: " + amount);
        }
    }

    /**
     * Convert to minor units (smallest unit) — alias for toCents()
     * 转换为最小货币单位 — toCents() 的别名
     *
     * @return the minor units | 最小单位
     * @throws ArithmeticException if amount too large | 如果金额过大
     * @since V1.0.3
     */
    public long toMinorUnits() {
        return toCents();
    }

    /**
     * Convert to target currency
     * 转换为目标货币
     *
     * @param target the target currency | 目标货币
     * @param rate the exchange rate | 汇率
     * @return the converted money | 转换后的金额
     */
    public Money convertTo(Currency target, BigDecimal rate) {
        Objects.requireNonNull(target, "Target currency must not be null");
        Objects.requireNonNull(rate, "Exchange rate must not be null");
        return new Money(
            amount.multiply(rate).setScale(target.getScale(), RoundingMode.HALF_UP),
            target
        );
    }

    // ============ Formatting | 格式化 ============

    /**
     * Format with currency symbol
     * 带货币符号格式化
     *
     * @return the formatted string | 格式化字符串
     */
    public String format() {
        return currency.getSymbol() + formatNumber();
    }

    /**
     * Format number only
     * 仅格式化数字
     *
     * @return the formatted number | 格式化的数字
     */
    public String formatNumber() {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ROOT);
        nf.setMinimumFractionDigits(currency.getScale());
        nf.setMaximumFractionDigits(currency.getScale());
        return nf.format(amount);
    }

    /**
     * Convert to Chinese uppercase
     * 转换为中文大写
     *
     * @return the Chinese uppercase string | 中文大写字符串
     */
    public String toChineseUpperCase() {
        return ChineseUtil.toUpperCase(amount);
    }

    // ============ Helper Methods | 辅助方法 ============

    /**
     * Assert same currency
     * 断言相同货币
     *
     * @param other the other money | 另一个金额
     */
    private void assertSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new CurrencyMismatchException(currency, other.currency);
        }
    }

    @Override
    public String toString() {
        return format();
    }
}
