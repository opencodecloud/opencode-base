package cloud.opencode.base.money.calc;

import cloud.opencode.base.money.Currency;
import cloud.opencode.base.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

/**
 * Money Calc Util
 * 金额计算工具类
 *
 * <p>Utility for money calculation operations like sum, average, min, max.</p>
 * <p>金额计算操作工具类，如求和、平均、最小、最大。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<Money> orders = List.of(Money.of("100"), Money.of("200"), Money.of("300"));
 * Money total = MoneyCalcUtil.sum(orders);     // ¥600.00
 * Money avg = MoneyCalcUtil.average(orders);   // ¥200.00
 * Money max = MoneyCalcUtil.max(orders);       // ¥300.00
 * Money min = MoneyCalcUtil.min(orders);       // ¥100.00
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sum, average, min, max calculations on money collections - 金额集合的求和、平均、最小、最大计算</li>
 *   <li>Currency-aware aggregation - 货币感知聚合</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes, null/empty collections return zero - 空值安全: 是，null/空集合返回零</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n is the size of the money collection - 时间复杂度: O(n)，n 为金额集合大小</li>
 *   <li>Space complexity: O(1) - streaming reduction with no intermediate collection - 空间复杂度: O(1) 流式归约无中间集合</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
public final class MoneyCalcUtil {

    private MoneyCalcUtil() {
        // Utility class
    }

    /**
     * Calculate sum of money collection
     * 计算金额集合的和
     *
     * @param moneys the money collection | 金额集合
     * @return the sum | 和
     */
    public static Money sum(Collection<Money> moneys) {
        if (moneys == null || moneys.isEmpty()) {
            return Money.zero();
        }
        return moneys.stream()
            .filter(Objects::nonNull)
            .reduce(Money::add)
            .orElse(Money.zero());
    }

    /**
     * Calculate sum with specified currency
     * 按指定货币计算和
     *
     * @param moneys the money collection | 金额集合
     * @param currency the currency | 货币
     * @return the sum | 和
     */
    public static Money sum(Collection<Money> moneys, Currency currency) {
        if (moneys == null || moneys.isEmpty()) {
            return Money.zero(currency);
        }
        return moneys.stream()
            .filter(Objects::nonNull)
            .reduce(Money::add)
            .orElse(Money.zero(currency));
    }

    /**
     * Calculate average of money collection
     * 计算金额集合的平均值
     *
     * @param moneys the money collection | 金额集合
     * @return the average | 平均值
     */
    public static Money average(Collection<Money> moneys) {
        if (moneys == null || moneys.isEmpty()) {
            throw new IllegalArgumentException("Cannot calculate average of null or empty collection");
        }
        // Single-pass: track both sum and count in one iteration
        Money total = null;
        long count = 0;
        for (Money m : moneys) {
            if (m != null) {
                total = (total == null) ? m : total.add(m);
                count++;
            }
        }
        if (count == 0) {
            throw new IllegalArgumentException("Cannot calculate average: all elements are null");
        }
        return total.divide(count);
    }

    /**
     * Find maximum money
     * 找最大金额
     *
     * @param moneys the money collection | 金额集合
     * @return the maximum | 最大值
     */
    public static Money max(Collection<Money> moneys) {
        return moneys.stream()
            .filter(Objects::nonNull)
            .max(Comparator.naturalOrder())
            .orElseThrow(() -> new IllegalArgumentException("Collection is empty"));
    }

    /**
     * Find minimum money
     * 找最小金额
     *
     * @param moneys the money collection | 金额集合
     * @return the minimum | 最小值
     */
    public static Money min(Collection<Money> moneys) {
        return moneys.stream()
            .filter(Objects::nonNull)
            .min(Comparator.naturalOrder())
            .orElseThrow(() -> new IllegalArgumentException("Collection is empty"));
    }

    /**
     * Calculate percentage
     * 计算百分比
     *
     * @param part the part | 部分
     * @param total the total | 总数
     * @param scale the decimal scale | 小数位数
     * @return the percentage (0-1) | 百分比（0-1）
     */
    public static BigDecimal percentage(Money part, Money total, int scale) {
        if (total.isZero()) {
            return BigDecimal.ZERO;
        }
        return part.amount().divide(total.amount(), scale + 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate percentage as integer (0-100)
     * 计算整数百分比（0-100）
     *
     * @param part the part | 部分
     * @param total the total | 总数
     * @return the percentage (0-100) | 百分比（0-100）
     */
    public static int percentageInt(Money part, Money total) {
        return percentage(part, total, 2).multiply(BigDecimal.valueOf(100)).intValue();
    }

    /**
     * Apply discount
     * 应用折扣
     *
     * @param money the money | 金额
     * @param discountRate the discount rate (0-1, e.g., 0.1 for 10% off) | 折扣率（0-1，如0.1表示9折）
     * @return the discounted money | 折后金额
     */
    public static Money applyDiscount(Money money, BigDecimal discountRate) {
        BigDecimal multiplier = BigDecimal.ONE.subtract(discountRate);
        return money.multiply(multiplier).round();
    }

    /**
     * Apply discount percentage
     * 应用折扣百分比
     *
     * @param money the money | 金额
     * @param discountPercent the discount percentage (0-100) | 折扣百分比（0-100）
     * @return the discounted money | 折后金额
     */
    public static Money applyDiscountPercent(Money money, int discountPercent) {
        return applyDiscount(money, BigDecimal.valueOf(discountPercent).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
    }

    /**
     * Calculate tax
     * 计算税额
     *
     * @param money the money (before tax) | 金额（税前）
     * @param taxRate the tax rate (0-1) | 税率（0-1）
     * @return the tax amount | 税额
     */
    public static Money calculateTax(Money money, BigDecimal taxRate) {
        return money.multiply(taxRate).round();
    }

    /**
     * Add tax
     * 加税
     *
     * @param money the money (before tax) | 金额（税前）
     * @param taxRate the tax rate (0-1) | 税率（0-1）
     * @return the money with tax | 含税金额
     */
    public static Money addTax(Money money, BigDecimal taxRate) {
        return money.add(calculateTax(money, taxRate));
    }

    /**
     * Remove tax (get pre-tax amount from tax-inclusive price)
     * 去税（从含税价获取税前金额）
     *
     * @param moneyWithTax the money with tax | 含税金额
     * @param taxRate the tax rate (0-1) | 税率（0-1）
     * @return the pre-tax money | 税前金额
     */
    public static Money removeTax(Money moneyWithTax, BigDecimal taxRate) {
        BigDecimal divisor = BigDecimal.ONE.add(taxRate);
        return moneyWithTax.divide(divisor);
    }

    /**
     * Round to nearest value
     * 四舍五入到最接近的值
     *
     * @param money the money | 金额
     * @param nearest the nearest value (e.g., 0.5, 1, 10) | 最接近的值（如0.5、1、10）
     * @return the rounded money | 四舍五入后的金额
     */
    public static Money roundToNearest(Money money, BigDecimal nearest) {
        BigDecimal rounded = money.amount()
            .divide(nearest, 0, RoundingMode.HALF_UP)
            .multiply(nearest);
        return Money.of(rounded, money.currency());
    }

    /**
     * Check if amounts are equal (ignoring scale)
     * 检查金额是否相等（忽略精度）
     *
     * @param m1 the first money | 第一个金额
     * @param m2 the second money | 第二个金额
     * @return true if equal | 如果相等返回true
     */
    public static boolean areEqual(Money m1, Money m2) {
        if (m1 == null && m2 == null) {
            return true;
        }
        if (m1 == null || m2 == null) {
            return false;
        }
        if (!m1.currency().equals(m2.currency())) {
            return false;
        }
        return m1.amount().compareTo(m2.amount()) == 0;
    }
}
