package cloud.opencode.base.money.validation;

import cloud.opencode.base.money.Money;
import cloud.opencode.base.money.exception.InvalidAmountException;
import cloud.opencode.base.money.exception.MoneyErrorCode;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Money Validator
 * 金额验证器
 *
 * <p>Utility for validating money amounts and related values.</p>
 * <p>验证金额及相关值的工具类。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BigDecimal amount = MoneyValidator.validateAndParse("100.50");
 * MoneyValidator.validatePositive(Money.of("100"));
 * MoneyValidator.validateRange(Money.of("100"), Money.of("0"), Money.of("1000"));
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Validate and parse monetary amount strings - 验证和解析金额字符串</li>
 *   <li>Range validation (min/max) - 范围验证（最小/最大）</li>
 *   <li>Positive/non-negative amount validation - 正数/非负数金额验证</li>
 *   <li>Configurable decimal scale limits - 可配置的小数位数限制</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility with compiled patterns) - 线程安全: 是（使用编译模式的无状态工具类）</li>
 *   <li>Null-safe: No, null amount throws InvalidAmountException - 空值安全: 否，null金额抛出InvalidAmountException</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
public final class MoneyValidator {

    /**
     * Maximum amount (100 billion) | 最大金额（1000亿）
     */
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000000000");

    /**
     * Minimum amount (negative 100 billion) | 最小金额（负1000亿）
     */
    private static final BigDecimal MIN_AMOUNT = MAX_AMOUNT.negate();

    /**
     * Amount pattern | 金额格式
     */
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    /**
     * Default max decimal places | 默认最大小数位数
     */
    private static final int DEFAULT_MAX_SCALE = 2;

    private MoneyValidator() {
        // Utility class
    }

    /**
     * Validate and parse amount string
     * 验证并解析金额字符串
     *
     * @param amountStr the amount string | 金额字符串
     * @return the parsed amount | 解析后的金额
     * @throws InvalidAmountException if invalid | 如果无效
     */
    public static BigDecimal validateAndParse(String amountStr) {
        return validateAndParse(amountStr, DEFAULT_MAX_SCALE);
    }

    /**
     * Validate and parse amount string with max scale
     * 按最大精度验证并解析金额字符串
     *
     * @param amountStr the amount string | 金额字符串
     * @param maxScale the maximum decimal places | 最大小数位数
     * @return the parsed amount | 解析后的金额
     * @throws InvalidAmountException if invalid | 如果无效
     */
    public static BigDecimal validateAndParse(String amountStr, int maxScale) {
        if (amountStr == null || amountStr.isBlank()) {
            throw new InvalidAmountException("Amount cannot be empty");
        }

        // Remove whitespace
        amountStr = amountStr.strip();

        // Validate format
        if (!AMOUNT_PATTERN.matcher(amountStr).matches()) {
            throw InvalidAmountException.formatError(amountStr);
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            throw new InvalidAmountException("Cannot parse amount: " + amountStr, e);
        }

        // Validate range
        validateRange(amount);

        // Validate precision
        if (amount.scale() > maxScale) {
            throw InvalidAmountException.precisionError(amountStr, maxScale);
        }

        return amount;
    }

    /**
     * Validate amount range
     * 验证金额范围
     *
     * @param amount the amount | 金额
     * @throws InvalidAmountException if out of range | 如果超出范围
     */
    public static void validateRange(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw InvalidAmountException.overflow(amount.toString());
        }
        if (amount.compareTo(MIN_AMOUNT) < 0) {
            throw InvalidAmountException.overflow(amount.toString());
        }
    }

    /**
     * Validate money is positive
     * 验证金额为正
     *
     * @param money the money | 金额
     * @throws InvalidAmountException if not positive | 如果不为正
     */
    public static void validatePositive(Money money) {
        Objects.requireNonNull(money, "Money cannot be null");
        if (!money.isPositive()) {
            throw new InvalidAmountException(
                "Amount must be positive: " + money,
                MoneyErrorCode.AMOUNT_NEGATIVE
            );
        }
    }

    /**
     * Validate money is non-negative
     * 验证金额非负
     *
     * @param money the money | 金额
     * @throws InvalidAmountException if negative | 如果为负
     */
    public static void validateNonNegative(Money money) {
        Objects.requireNonNull(money, "Money cannot be null");
        if (money.isNegative()) {
            throw new InvalidAmountException(
                "Amount cannot be negative: " + money,
                MoneyErrorCode.AMOUNT_NEGATIVE
            );
        }
    }

    /**
     * Validate money is not zero
     * 验证金额不为零
     *
     * @param money the money | 金额
     * @throws InvalidAmountException if zero | 如果为零
     */
    public static void validateNotZero(Money money) {
        Objects.requireNonNull(money, "Money cannot be null");
        if (money.isZero()) {
            throw new InvalidAmountException("Amount cannot be zero: " + money);
        }
    }

    /**
     * Validate money is within range
     * 验证金额在范围内
     *
     * @param money the money | 金额
     * @param min the minimum (inclusive) | 最小值（包含）
     * @param max the maximum (inclusive) | 最大值（包含）
     * @throws InvalidAmountException if out of range | 如果超出范围
     */
    public static void validateRange(Money money, Money min, Money max) {
        Objects.requireNonNull(money, "Money cannot be null");
        Objects.requireNonNull(min, "Min cannot be null");
        Objects.requireNonNull(max, "Max cannot be null");

        if (money.isLessThan(min) || money.isGreaterThan(max)) {
            throw new InvalidAmountException(
                String.format("Amount %s is out of range [%s, %s]", money, min, max)
            );
        }
    }

    /**
     * Check if amount string is valid
     * 检查金额字符串是否有效
     *
     * @param amountStr the amount string | 金额字符串
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(String amountStr) {
        return isValid(amountStr, DEFAULT_MAX_SCALE);
    }

    /**
     * Check if amount string is valid with max scale
     * 按最大精度检查金额字符串是否有效
     *
     * @param amountStr the amount string | 金额字符串
     * @param maxScale the maximum decimal places | 最大小数位数
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(String amountStr, int maxScale) {
        try {
            validateAndParse(amountStr, maxScale);
            return true;
        } catch (InvalidAmountException e) {
            return false;
        }
    }

    /**
     * Get maximum allowed amount
     * 获取允许的最大金额
     *
     * @return the maximum amount | 最大金额
     */
    public static BigDecimal getMaxAmount() {
        return MAX_AMOUNT;
    }

    /**
     * Get minimum allowed amount
     * 获取允许的最小金额
     *
     * @return the minimum amount | 最小金额
     */
    public static BigDecimal getMinAmount() {
        return MIN_AMOUNT;
    }
}
