package cloud.opencode.base.money.exchange;

import cloud.opencode.base.money.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Exchange Rate
 * 汇率
 *
 * <p>Immutable record representing an exchange rate between two currencies.</p>
 * <p>表示两种货币之间汇率的不可变记录。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ExchangeRate rate = ExchangeRate.of(Currency.CNY, Currency.USD, "0.14");
 * BigDecimal usd = rate.convert(new BigDecimal("100"));  // 14.00
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable exchange rate between two currencies - 两种货币之间的不可变汇率</li>
 *   <li>Amount conversion with proper scaling - 带正确精度的金额转换</li>
 *   <li>Timestamp tracking - 时间戳跟踪</li>
 *   <li>Inverse rate calculation - 反向汇率计算</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No, source/target/rate validated non-null - 空值安全: 否，源/目标/汇率验证非null</li>
 * </ul>
 *
 * @param source the source currency | 源货币
 * @param target the target currency | 目标货币
 * @param rate the exchange rate | 汇率
 * @param timestamp the timestamp | 时间戳
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
public record ExchangeRate(Currency source, Currency target, BigDecimal rate, LocalDateTime timestamp) {

    /**
     * Canonical constructor with validation
     * 规范构造器（带验证）
     */
    public ExchangeRate {
        Objects.requireNonNull(source, "Source currency must not be null");
        Objects.requireNonNull(target, "Target currency must not be null");
        Objects.requireNonNull(rate, "Rate must not be null");
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Rate must be positive");
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    // ============ Factory Methods | 工厂方法 ============

    /**
     * Create exchange rate
     * 创建汇率
     *
     * @param source the source currency | 源货币
     * @param target the target currency | 目标货币
     * @param rate the exchange rate | 汇率
     * @return the exchange rate | 汇率
     */
    public static ExchangeRate of(Currency source, Currency target, BigDecimal rate) {
        return new ExchangeRate(source, target, rate, LocalDateTime.now());
    }

    /**
     * Create exchange rate from string
     * 从字符串创建汇率
     *
     * @param source the source currency | 源货币
     * @param target the target currency | 目标货币
     * @param rate the exchange rate string | 汇率字符串
     * @return the exchange rate | 汇率
     */
    public static ExchangeRate of(Currency source, Currency target, String rate) {
        return of(source, target, new BigDecimal(rate));
    }

    /**
     * Create exchange rate with timestamp
     * 创建带时间戳的汇率
     *
     * @param source the source currency | 源货币
     * @param target the target currency | 目标货币
     * @param rate the exchange rate | 汇率
     * @param timestamp the timestamp | 时间戳
     * @return the exchange rate | 汇率
     */
    public static ExchangeRate of(Currency source, Currency target, BigDecimal rate, LocalDateTime timestamp) {
        return new ExchangeRate(source, target, rate, timestamp);
    }

    /**
     * Create identity rate (1:1)
     * 创建等值汇率（1:1）
     *
     * @param currency the currency | 货币
     * @return the exchange rate | 汇率
     */
    public static ExchangeRate identity(Currency currency) {
        return new ExchangeRate(currency, currency, BigDecimal.ONE, LocalDateTime.now());
    }

    // ============ Conversion | 转换 ============

    /**
     * Convert amount from source to target currency
     * 将金额从源货币转换为目标货币
     *
     * @param amount the amount in source currency | 源货币金额
     * @return the amount in target currency | 目标货币金额
     */
    public BigDecimal convert(BigDecimal amount) {
        return amount.multiply(rate).setScale(target.getScale(), RoundingMode.HALF_UP);
    }

    /**
     * Get inverse rate (target to source)
     * 获取逆汇率（目标到源）
     *
     * @return the inverse exchange rate | 逆汇率
     */
    public ExchangeRate inverse() {
        return new ExchangeRate(
            target,
            source,
            BigDecimal.ONE.divide(rate, 10, RoundingMode.HALF_UP),
            timestamp
        );
    }

    // ============ Utility | 工具方法 ============

    /**
     * Check if rate is expired
     * 检查汇率是否过期
     *
     * @param maxAgeHours the maximum age in hours | 最大有效时间（小时）
     * @return true if expired | 如果过期返回true
     */
    public boolean isExpired(int maxAgeHours) {
        return timestamp.plusHours(maxAgeHours).isBefore(LocalDateTime.now());
    }

    /**
     * Check if this rate can convert between given currencies
     * 检查此汇率是否可以在给定货币之间转换
     *
     * @param from the source currency | 源货币
     * @param to the target currency | 目标货币
     * @return true if can convert | 如果可以转换返回true
     */
    public boolean canConvert(Currency from, Currency to) {
        return source.equals(from) && target.equals(to);
    }

    /**
     * Format as string
     * 格式化为字符串
     *
     * @return the formatted string | 格式化字符串
     */
    public String format() {
        return String.format("1 %s = %s %s", source.getCode(), rate.stripTrailingZeros().toPlainString(), target.getCode());
    }

    @Override
    public String toString() {
        return format();
    }
}
