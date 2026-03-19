package cloud.opencode.base.money.exception;

import cloud.opencode.base.money.Currency;

/**
 * Currency Mismatch Exception
 * 币种不匹配异常
 *
 * <p>Exception thrown when operations are attempted on money with different currencies.</p>
 * <p>当对不同币种的金额进行操作时抛出此异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception for currency mismatch in money operations - 金额操作中的币种不匹配异常</li>
 *   <li>Carries expected and actual currency information - 携带期望和实际的币种信息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new CurrencyMismatchException(Currency.CNY, Currency.USD);
 * // "Currency mismatch: expected CNY, got USD"
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
public class CurrencyMismatchException extends MoneyException {

    private final Currency expected;
    private final Currency actual;

    /**
     * Create currency mismatch exception
     * 创建币种不匹配异常
     *
     * @param expected the expected currency | 期望的币种
     * @param actual the actual currency | 实际的币种
     */
    public CurrencyMismatchException(Currency expected, Currency actual) {
        super(String.format("Currency mismatch: expected %s, got %s", expected, actual),
              MoneyErrorCode.CURRENCY_MISMATCH);
        this.expected = expected;
        this.actual = actual;
    }

    /**
     * Create currency mismatch exception with message
     * 创建带消息的币种不匹配异常
     *
     * @param message the error message | 错误消息
     * @param expected the expected currency | 期望的币种
     * @param actual the actual currency | 实际的币种
     */
    public CurrencyMismatchException(String message, Currency expected, Currency actual) {
        super(message, MoneyErrorCode.CURRENCY_MISMATCH);
        this.expected = expected;
        this.actual = actual;
    }

    /**
     * Get expected currency
     * 获取期望的币种
     *
     * @return the expected currency | 期望的币种
     */
    public Currency getExpected() {
        return expected;
    }

    /**
     * Get actual currency
     * 获取实际的币种
     *
     * @return the actual currency | 实际的币种
     */
    public Currency getActual() {
        return actual;
    }
}
