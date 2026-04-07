package cloud.opencode.base.money.calc;

import cloud.opencode.base.money.Currency;
import cloud.opencode.base.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Money Rounding Utility - Multiple rounding strategies for monetary amounts
 * 金额舍入工具 - 多种货币金额舍入策略
 *
 * <p>Provides various rounding strategies commonly used in financial calculations,
 * including Swedish rounding, banker's rounding, step-based rounding, and more.</p>
 * <p>提供金融计算中常用的多种舍入策略，包括瑞典舍入、银行家舍入、步进舍入等。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Money m = Money.of("10.23");
 *
 * // Swedish rounding (nearest 0.05)
 * MoneyRounding.swedish(m);           // ¥10.25
 *
 * // Banker's rounding (HALF_EVEN)
 * MoneyRounding.bankers(m);           // ¥10.23
 *
 * // Round to step (nearest 0.5)
 * MoneyRounding.roundToStep(m, new BigDecimal("0.5"));  // ¥10.00
 *
 * // Ceiling (round up)
 * MoneyRounding.ceil(m);              // ¥10.23 (already at scale)
 * MoneyRounding.ceilToStep(m, BigDecimal.ONE);  // ¥11
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Swedish rounding (0.05 step) - 瑞典舍入（0.05步进）</li>
 *   <li>Banker's rounding (HALF_EVEN) - 银行家舍入</li>
 *   <li>Custom step rounding (round/ceil/floor to any step) - 自定义步进舍入</li>
 *   <li>Ceiling and floor rounding - 向上和向下舍入</li>
 *   <li>Round to significant figures - 按有效位数舍入</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No, arguments validated non-null - 空值安全: 否，参数验证非null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.3
 */
public final class MoneyRounding {

    private static final BigDecimal SWEDISH_STEP = new BigDecimal("0.05");

    private MoneyRounding() {
        // Utility class
    }

    /**
     * Swedish rounding — round to nearest 0.05
     * 瑞典舍入 — 四舍五入到最近的 0.05
     *
     * <p>Used in Sweden, Switzerland, and other countries where 1 and 2 cent coins
     * are not in circulation. Cash transactions are rounded to nearest 5 cents.</p>
     * <p>用于瑞典、瑞士等不流通1分和2分硬币的国家。现金交易四舍五入到最近的5分。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * swedish(Money.of("10.22")) = ¥10.20
     * swedish(Money.of("10.23")) = ¥10.25
     * swedish(Money.of("10.25")) = ¥10.25
     * swedish(Money.of("10.27")) = ¥10.25
     * swedish(Money.of("10.28")) = ¥10.30
     * </pre>
     *
     * @param money the money to round | 要舍入的金额
     * @return the rounded money | 舍入后的金额
     */
    public static Money swedish(Money money) {
        return roundToStep(money, SWEDISH_STEP);
    }

    /**
     * Banker's rounding — round half to even (HALF_EVEN)
     * 银行家舍入 — 四舍六入五取偶
     *
     * <p>IEEE 754 standard rounding. When the value is exactly halfway,
     * it rounds to the nearest even digit. Minimizes cumulative rounding bias.</p>
     * <p>IEEE 754 标准舍入。当值恰好在中间时，舍入到最近的偶数。最小化累积舍入偏差。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * bankers(Money.of("10.225")) = ¥10.22 (round to even)
     * bankers(Money.of("10.235")) = ¥10.24 (round to even)
     * bankers(Money.of("10.245")) = ¥10.24 (round to even)
     * </pre>
     *
     * @param money the money to round | 要舍入的金额
     * @return the rounded money | 舍入后的金额
     */
    public static Money bankers(Money money) {
        Objects.requireNonNull(money, "Money must not be null");
        BigDecimal rounded = money.amount().setScale(money.currency().getScale(), RoundingMode.HALF_EVEN);
        return Money.of(rounded, money.currency());
    }

    /**
     * Round to currency scale using HALF_UP (standard rounding)
     * 按货币精度标准四舍五入
     *
     * @param money the money to round | 要舍入的金额
     * @return the rounded money | 舍入后的金额
     */
    public static Money standard(Money money) {
        Objects.requireNonNull(money, "Money must not be null");
        BigDecimal rounded = money.amount().setScale(money.currency().getScale(), RoundingMode.HALF_UP);
        return Money.of(rounded, money.currency());
    }

    /**
     * Ceiling — round up to currency scale
     * 向上取整到货币精度
     *
     * @param money the money to round | 要舍入的金额
     * @return the rounded money | 舍入后的金额
     */
    public static Money ceil(Money money) {
        Objects.requireNonNull(money, "Money must not be null");
        BigDecimal rounded = money.amount().setScale(money.currency().getScale(), RoundingMode.CEILING);
        return Money.of(rounded, money.currency());
    }

    /**
     * Floor — round down to currency scale
     * 向下取整到货币精度
     *
     * @param money the money to round | 要舍入的金额
     * @return the rounded money | 舍入后的金额
     */
    public static Money floor(Money money) {
        Objects.requireNonNull(money, "Money must not be null");
        BigDecimal rounded = money.amount().setScale(money.currency().getScale(), RoundingMode.FLOOR);
        return Money.of(rounded, money.currency());
    }

    /**
     * Round to nearest step (e.g., 0.05, 0.5, 1, 10)
     * 四舍五入到最近的步进值
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * roundToStep(Money.of("10.23"), new BigDecimal("0.05"))  = ¥10.25
     * roundToStep(Money.of("10.23"), new BigDecimal("0.5"))   = ¥10.00
     * roundToStep(Money.of("10.23"), BigDecimal.ONE)          = ¥10
     * roundToStep(Money.of("13.50"), BigDecimal.TEN)          = ¥10
     * </pre>
     *
     * @param money the money to round | 要舍入的金额
     * @param step the rounding step (must be positive) | 舍入步进（必须为正）
     * @return the rounded money | 舍入后的金额
     */
    public static Money roundToStep(Money money, BigDecimal step) {
        Objects.requireNonNull(money, "Money must not be null");
        Objects.requireNonNull(step, "Step must not be null");
        if (step.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Step must be positive: " + step);
        }
        BigDecimal divided = money.amount().divide(step, 0, RoundingMode.HALF_UP);
        BigDecimal rounded = divided.multiply(step)
                .setScale(money.currency().getScale(), RoundingMode.HALF_UP);
        return Money.of(rounded, money.currency());
    }

    /**
     * Round up (ceiling) to nearest step
     * 向上取整到最近的步进值
     *
     * @param money the money to round | 要舍入的金额
     * @param step the rounding step (must be positive) | 舍入步进（必须为正）
     * @return the rounded money | 舍入后的金额
     */
    public static Money ceilToStep(Money money, BigDecimal step) {
        Objects.requireNonNull(money, "Money must not be null");
        Objects.requireNonNull(step, "Step must not be null");
        if (step.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Step must be positive: " + step);
        }
        BigDecimal divided = money.amount().divide(step, 0, RoundingMode.CEILING);
        BigDecimal rounded = divided.multiply(step)
                .setScale(money.currency().getScale(), RoundingMode.HALF_UP);
        return Money.of(rounded, money.currency());
    }

    /**
     * Round down (floor) to nearest step
     * 向下取整到最近的步进值
     *
     * @param money the money to round | 要舍入的金额
     * @param step the rounding step (must be positive) | 舍入步进（必须为正）
     * @return the rounded money | 舍入后的金额
     */
    public static Money floorToStep(Money money, BigDecimal step) {
        Objects.requireNonNull(money, "Money must not be null");
        Objects.requireNonNull(step, "Step must not be null");
        if (step.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Step must be positive: " + step);
        }
        BigDecimal divided = money.amount().divide(step, 0, RoundingMode.FLOOR);
        BigDecimal rounded = divided.multiply(step)
                .setScale(money.currency().getScale(), RoundingMode.HALF_UP);
        return Money.of(rounded, money.currency());
    }

    /**
     * Round with a specific RoundingMode to currency scale
     * 使用指定舍入模式按货币精度舍入
     *
     * @param money the money to round | 要舍入的金额
     * @param mode the rounding mode | 舍入模式
     * @return the rounded money | 舍入后的金额
     */
    public static Money round(Money money, RoundingMode mode) {
        Objects.requireNonNull(money, "Money must not be null");
        Objects.requireNonNull(mode, "RoundingMode must not be null");
        BigDecimal rounded = money.amount().setScale(money.currency().getScale(), mode);
        return Money.of(rounded, money.currency());
    }

    /**
     * Round with a specific RoundingMode and scale
     * 使用指定舍入模式和精度舍入
     *
     * @param money the money to round | 要舍入的金额
     * @param scale the target scale | 目标精度
     * @param mode the rounding mode | 舍入模式
     * @return the rounded money | 舍入后的金额
     */
    public static Money round(Money money, int scale, RoundingMode mode) {
        Objects.requireNonNull(money, "Money must not be null");
        Objects.requireNonNull(mode, "RoundingMode must not be null");
        if (scale < 0) {
            throw new IllegalArgumentException("Scale must not be negative: " + scale);
        }
        BigDecimal rounded = money.amount().setScale(scale, mode);
        return Money.of(rounded, money.currency());
    }
}
