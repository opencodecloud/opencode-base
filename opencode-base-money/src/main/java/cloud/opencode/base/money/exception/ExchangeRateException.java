package cloud.opencode.base.money.exception;

import cloud.opencode.base.money.Currency;

/**
 * Exchange Rate Exception
 * 汇率异常
 *
 * <p>Exception thrown when exchange rate related errors occur.</p>
 * <p>当发生汇率相关错误时抛出此异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception for exchange rate errors - 汇率错误异常</li>
 *   <li>Carries source and target currency information - 携带源和目标币种信息</li>
 *   <li>Factory method for rate-not-found scenario - 汇率未找到场景的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw ExchangeRateException.notFound(Currency.CNY, Currency.BTC);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
public class ExchangeRateException extends MoneyException {

    private final Currency source;
    private final Currency target;

    /**
     * Create exchange rate exception
     * 创建汇率异常
     *
     * @param message the error message | 错误消息
     */
    public ExchangeRateException(String message) {
        super(message, MoneyErrorCode.RATE_NOT_FOUND);
        this.source = null;
        this.target = null;
    }

    /**
     * Create exchange rate exception with error code
     * 创建带错误码的汇率异常
     *
     * @param message the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public ExchangeRateException(String message, MoneyErrorCode errorCode) {
        super(message, errorCode);
        this.source = null;
        this.target = null;
    }

    /**
     * Create exchange rate exception with currencies
     * 创建带币种的汇率异常
     *
     * @param message the error message | 错误消息
     * @param source the source currency | 源币种
     * @param target the target currency | 目标币种
     */
    public ExchangeRateException(String message, Currency source, Currency target) {
        super(message, MoneyErrorCode.RATE_NOT_FOUND);
        this.source = source;
        this.target = target;
    }

    /**
     * Create exception for rate not found
     * 创建汇率未找到的异常
     *
     * @param source the source currency | 源币种
     * @param target the target currency | 目标币种
     * @return the exception | 异常
     */
    public static ExchangeRateException notFound(Currency source, Currency target) {
        return new ExchangeRateException(
            String.format("Exchange rate not found: %s -> %s", source, target),
            source,
            target
        );
    }

    /**
     * Create exception for invalid rate
     * 创建无效汇率的异常
     *
     * @param rate the invalid rate | 无效的汇率
     * @return the exception | 异常
     */
    public static ExchangeRateException invalidRate(String rate) {
        return new ExchangeRateException(
            "Invalid exchange rate: " + rate,
            MoneyErrorCode.RATE_INVALID
        );
    }

    /**
     * Get source currency
     * 获取源币种
     *
     * @return the source currency | 源币种
     */
    public Currency getSource() {
        return source;
    }

    /**
     * Get target currency
     * 获取目标币种
     *
     * @return the target currency | 目标币种
     */
    public Currency getTarget() {
        return target;
    }
}
