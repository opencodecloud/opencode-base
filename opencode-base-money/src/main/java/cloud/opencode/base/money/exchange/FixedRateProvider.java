package cloud.opencode.base.money.exchange;

import cloud.opencode.base.money.Currency;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fixed Rate Provider
 * 固定汇率提供者
 *
 * <p>Exchange rate provider using fixed, pre-configured rates.</p>
 * <p>使用固定预配置汇率的汇率提供者。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FixedRateProvider provider = FixedRateProvider.builder()
 *     .rate(Currency.CNY, Currency.USD, "0.14")
 *     .rate(Currency.CNY, Currency.EUR, "0.13")
 *     .build();
 *
 * Money usd = provider.convert(Money.of("100"), Currency.USD);  // $14.00
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fixed pre-configured exchange rates - 固定预配置汇率</li>
 *   <li>Builder pattern for rate configuration - 构建器模式配置汇率</li>
 *   <li>Thread-safe with ConcurrentHashMap - 使用ConcurrentHashMap实现线程安全</li>
 *   <li>Factory methods for common CNY rates - 常用人民币汇率的工厂方法</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap for rate storage) - 线程安全: 是（使用ConcurrentHashMap存储汇率）</li>
 *   <li>Null-safe: No, currencies must not be null - 空值安全: 否，货币不可为null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
public final class FixedRateProvider implements ExchangeRateProvider {

    private final Map<String, ExchangeRate> rates;

    /**
     * Create empty fixed rate provider
     * 创建空的固定汇率提供者
     */
    public FixedRateProvider() {
        this.rates = new ConcurrentHashMap<>();
    }

    /**
     * Create fixed rate provider with rates
     * 创建带汇率的固定汇率提供者
     *
     * @param rates the rates map | 汇率映射
     */
    private FixedRateProvider(Map<String, ExchangeRate> rates) {
        this.rates = new ConcurrentHashMap<>(rates);
    }

    /**
     * Create builder
     * 创建构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create with common CNY rates
     * 创建带常用人民币汇率的提供者
     *
     * @return the provider | 提供者
     */
    public static FixedRateProvider withCommonCnyRates() {
        return builder()
            .rate(Currency.CNY, Currency.USD, "0.14")
            .rate(Currency.CNY, Currency.EUR, "0.13")
            .rate(Currency.CNY, Currency.GBP, "0.11")
            .rate(Currency.CNY, Currency.JPY, "21.5")
            .rate(Currency.CNY, Currency.HKD, "1.09")
            .build();
    }

    @Override
    public Optional<ExchangeRate> getRate(Currency source, Currency target) {
        if (source.equals(target)) {
            return Optional.of(ExchangeRate.identity(source));
        }
        String key = makeKey(source, target);
        ExchangeRate rate = rates.get(key);
        if (rate != null) {
            return Optional.of(rate);
        }
        // Try inverse
        String inverseKey = makeKey(target, source);
        ExchangeRate inverseRate = rates.get(inverseKey);
        if (inverseRate != null) {
            return Optional.of(inverseRate.inverse());
        }
        return Optional.empty();
    }

    /**
     * Add rate
     * 添加汇率
     *
     * @param source the source currency | 源货币
     * @param target the target currency | 目标货币
     * @param rate the rate | 汇率
     * @return this provider | 此提供者
     */
    public FixedRateProvider addRate(Currency source, Currency target, BigDecimal rate) {
        String key = makeKey(source, target);
        rates.put(key, ExchangeRate.of(source, target, rate));
        return this;
    }

    /**
     * Add rate from string
     * 从字符串添加汇率
     *
     * @param source the source currency | 源货币
     * @param target the target currency | 目标货币
     * @param rate the rate string | 汇率字符串
     * @return this provider | 此提供者
     */
    public FixedRateProvider addRate(Currency source, Currency target, String rate) {
        return addRate(source, target, new BigDecimal(rate));
    }

    /**
     * Remove rate
     * 移除汇率
     *
     * @param source the source currency | 源货币
     * @param target the target currency | 目标货币
     * @return this provider | 此提供者
     */
    public FixedRateProvider removeRate(Currency source, Currency target) {
        String key = makeKey(source, target);
        rates.remove(key);
        return this;
    }

    /**
     * Clear all rates
     * 清除所有汇率
     *
     * @return this provider | 此提供者
     */
    public FixedRateProvider clearRates() {
        rates.clear();
        return this;
    }

    /**
     * Get all rates
     * 获取所有汇率
     *
     * @return the rates map | 汇率映射
     */
    public Map<String, ExchangeRate> getAllRates() {
        return Map.copyOf(rates);
    }

    /**
     * Get rate count
     * 获取汇率数量
     *
     * @return the count | 数量
     */
    public int getRateCount() {
        return rates.size();
    }

    /**
     * Make key for rate map
     * 生成汇率映射的键
     */
    private static String makeKey(Currency source, Currency target) {
        return source.name() + "_" + target.name();
    }

    /**
     * Builder for FixedRateProvider
     * FixedRateProvider 构建器
     */
    public static final class Builder {

        private final Map<String, ExchangeRate> rates = new ConcurrentHashMap<>();

        private Builder() {
        }

        /**
         * Add rate
         * 添加汇率
         *
         * @param source the source currency | 源货币
         * @param target the target currency | 目标货币
         * @param rate the rate | 汇率
         * @return this builder | 此构建器
         */
        public Builder rate(Currency source, Currency target, BigDecimal rate) {
            String key = makeKey(source, target);
            rates.put(key, ExchangeRate.of(source, target, rate));
            return this;
        }

        /**
         * Add rate from string
         * 从字符串添加汇率
         *
         * @param source the source currency | 源货币
         * @param target the target currency | 目标货币
         * @param rate the rate string | 汇率字符串
         * @return this builder | 此构建器
         */
        public Builder rate(Currency source, Currency target, String rate) {
            return rate(source, target, new BigDecimal(rate));
        }

        /**
         * Build the provider
         * 构建提供者
         *
         * @return the provider | 提供者
         */
        public FixedRateProvider build() {
            return new FixedRateProvider(rates);
        }
    }
}
