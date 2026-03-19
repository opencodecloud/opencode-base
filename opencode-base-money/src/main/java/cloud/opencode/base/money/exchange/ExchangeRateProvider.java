package cloud.opencode.base.money.exchange;

import cloud.opencode.base.money.Currency;
import cloud.opencode.base.money.Money;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Exchange Rate Provider
 * 汇率提供者接口
 *
 * <p>Interface for providing exchange rates between currencies.</p>
 * <p>提供货币之间汇率的接口。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ExchangeRateProvider provider = new FixedRateProvider();
 * Optional<ExchangeRate> rate = provider.getRate(Currency.CNY, Currency.USD);
 * Money converted = provider.convert(Money.of("100"), Currency.USD);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Interface for exchange rate lookup - 汇率查找接口</li>
 *   <li>Optional and throwing rate retrieval - 可选和抛异常的汇率获取</li>
 *   <li>Default convert() method for money conversion - 默认convert()方法用于金额转换</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No, currency arguments must not be null - 空值安全: 否，货币参数不可为null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
public interface ExchangeRateProvider {

    /**
     * Get exchange rate between two currencies
     * 获取两种货币之间的汇率
     *
     * @param source the source currency | 源货币
     * @param target the target currency | 目标货币
     * @return the exchange rate or empty | 汇率或空
     */
    Optional<ExchangeRate> getRate(Currency source, Currency target);

    /**
     * Get exchange rate, throwing if not found
     * 获取汇率，如果未找到则抛出异常
     *
     * @param source the source currency | 源货币
     * @param target the target currency | 目标货币
     * @return the exchange rate | 汇率
     * @throws cloud.opencode.base.money.exception.ExchangeRateException if rate not found | 如果未找到汇率
     */
    default ExchangeRate getRateOrThrow(Currency source, Currency target) {
        return getRate(source, target)
            .orElseThrow(() -> cloud.opencode.base.money.exception.ExchangeRateException.notFound(source, target));
    }

    /**
     * Convert money to target currency
     * 将金额转换为目标货币
     *
     * @param money the money to convert | 要转换的金额
     * @param target the target currency | 目标货币
     * @return the converted money | 转换后的金额
     * @throws cloud.opencode.base.money.exception.ExchangeRateException if rate not found | 如果未找到汇率
     */
    default Money convert(Money money, Currency target) {
        if (money.currency().equals(target)) {
            return money;
        }
        ExchangeRate rate = getRateOrThrow(money.currency(), target);
        return money.convertTo(target, rate.rate());
    }

    /**
     * Check if rate is available
     * 检查汇率是否可用
     *
     * @param source the source currency | 源货币
     * @param target the target currency | 目标货币
     * @return true if available | 如果可用返回true
     */
    default boolean hasRate(Currency source, Currency target) {
        return getRate(source, target).isPresent();
    }

    /**
     * Get rate value
     * 获取汇率值
     *
     * @param source the source currency | 源货币
     * @param target the target currency | 目标货币
     * @return the rate value or empty | 汇率值或空
     */
    default Optional<BigDecimal> getRateValue(Currency source, Currency target) {
        return getRate(source, target).map(ExchangeRate::rate);
    }
}
