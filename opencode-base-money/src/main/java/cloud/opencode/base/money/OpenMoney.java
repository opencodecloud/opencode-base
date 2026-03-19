package cloud.opencode.base.money;

import cloud.opencode.base.money.calc.AllocationUtil;
import cloud.opencode.base.money.calc.MoneyCalcUtil;
import cloud.opencode.base.money.exchange.ExchangeRate;
import cloud.opencode.base.money.exchange.ExchangeRateProvider;
import cloud.opencode.base.money.format.ChineseUtil;
import cloud.opencode.base.money.format.MoneyFormatUtil;
import cloud.opencode.base.money.validation.MoneyValidator;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * OpenMoney
 * 金额工具门面类
 *
 * <p>Facade class for money operations. Provides convenient static methods
 * for common money operations.</p>
 * <p>金额操作的门面类。提供常用金额操作的便捷静态方法。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Convert to Chinese uppercase
 * String chinese = OpenMoney.toChineseUpperCase(new BigDecimal("1234.56"));
 * // 壹仟贰佰叁拾肆元伍角陆分
 *
 * // Allocate by ratios
 * List<Money> parts = OpenMoney.allocate(Money.of("100"), 1, 2, 3);
 * // [¥16.67, ¥33.33, ¥50.00]
 *
 * // Calculate sum
 * Money total = OpenMoney.sum(List.of(Money.of("100"), Money.of("200")));
 * // ¥300.00
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Facade for money creation, allocation, and calculation - 金额创建、分摊和计算的门面</li>
 *   <li>Convenient static methods for common operations - 常用操作的便捷静态方法</li>
 *   <li>Chinese uppercase conversion - 中文大写转换</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No, arguments must not be null - 空值安全: 否，参数不可为null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
public final class OpenMoney {

    private OpenMoney() {
        // Utility class
    }

    // ============ Factory Methods | 工厂方法 ============

    /**
     * Create money from string (default CNY)
     * 从字符串创建金额（默认人民币）
     *
     * @param amount the amount string | 金额字符串
     * @return the money | 金额
     */
    public static Money of(String amount) {
        return Money.of(amount);
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
        return Money.of(amount, currency);
    }

    /**
     * Create money from cents
     * 从分创建金额
     *
     * @param cents the cents | 分
     * @return the money | 金额
     */
    public static Money ofCents(long cents) {
        return Money.ofCents(cents);
    }

    /**
     * Create zero money
     * 创建零金额
     *
     * @return the zero money | 零金额
     */
    public static Money zero() {
        return Money.zero();
    }

    /**
     * Create zero money with currency
     * 创建指定货币的零金额
     *
     * @param currency the currency | 货币
     * @return the zero money | 零金额
     */
    public static Money zero(Currency currency) {
        return Money.zero(currency);
    }

    // ============ Chinese Uppercase | 中文大写 ============

    /**
     * Convert amount to Chinese uppercase
     * 将金额转换为中文大写
     *
     * @param amount the amount | 金额
     * @return the Chinese uppercase string | 中文大写字符串
     */
    public static String toChineseUpperCase(BigDecimal amount) {
        return ChineseUtil.toUpperCase(amount);
    }

    /**
     * Convert money to Chinese uppercase
     * 将金额转换为中文大写
     *
     * @param money the money | 金额
     * @return the Chinese uppercase string | 中文大写字符串
     */
    public static String toChineseUpperCase(Money money) {
        return money.toChineseUpperCase();
    }

    // ============ Allocation | 分摊 ============

    /**
     * Allocate money by ratios
     * 按比例分摊金额
     *
     * @param total the total money | 总金额
     * @param ratios the ratios | 比例
     * @return the allocated parts | 分摊后的各部分
     */
    public static List<Money> allocate(Money total, int... ratios) {
        return AllocationUtil.allocate(total, ratios);
    }

    /**
     * Split money evenly
     * 平均分摊金额
     *
     * @param total the total money | 总金额
     * @param parts the number of parts | 份数
     * @return the split parts | 分摊后的各部分
     */
    public static List<Money> split(Money total, int parts) {
        return AllocationUtil.split(total, parts);
    }

    /**
     * Allocate by percentages
     * 按百分比分摊
     *
     * @param total the total money | 总金额
     * @param percentages the percentages (must sum to 100) | 百分比（必须加起来等于100）
     * @return the allocated parts | 分摊后的各部分
     */
    public static List<Money> allocateByPercent(Money total, int... percentages) {
        return AllocationUtil.allocateByPercent(total, percentages);
    }

    // ============ Aggregation | 聚合运算 ============

    /**
     * Calculate sum of money collection
     * 计算金额集合的和
     *
     * @param moneys the money collection | 金额集合
     * @return the sum | 和
     */
    public static Money sum(Collection<Money> moneys) {
        return MoneyCalcUtil.sum(moneys);
    }

    /**
     * Calculate average of money collection
     * 计算金额集合的平均值
     *
     * @param moneys the money collection | 金额集合
     * @return the average | 平均值
     */
    public static Money average(Collection<Money> moneys) {
        return MoneyCalcUtil.average(moneys);
    }

    /**
     * Find maximum money
     * 找最大金额
     *
     * @param moneys the money collection | 金额集合
     * @return the maximum | 最大值
     */
    public static Money max(Collection<Money> moneys) {
        return MoneyCalcUtil.max(moneys);
    }

    /**
     * Find minimum money
     * 找最小金额
     *
     * @param moneys the money collection | 金额集合
     * @return the minimum | 最小值
     */
    public static Money min(Collection<Money> moneys) {
        return MoneyCalcUtil.min(moneys);
    }

    // ============ Discount & Tax | 折扣与税 ============

    /**
     * Apply discount
     * 应用折扣
     *
     * @param money the money | 金额
     * @param discountRate the discount rate (0-1) | 折扣率（0-1）
     * @return the discounted money | 折后金额
     */
    public static Money applyDiscount(Money money, BigDecimal discountRate) {
        return MoneyCalcUtil.applyDiscount(money, discountRate);
    }

    /**
     * Calculate tax
     * 计算税额
     *
     * @param money the money | 金额
     * @param taxRate the tax rate (0-1) | 税率（0-1）
     * @return the tax amount | 税额
     */
    public static Money calculateTax(Money money, BigDecimal taxRate) {
        return MoneyCalcUtil.calculateTax(money, taxRate);
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
        return MoneyCalcUtil.addTax(money, taxRate);
    }

    // ============ Exchange Rate | 汇率转换 ============

    /**
     * Convert money to target currency
     * 将金额转换为目标货币
     *
     * @param money the money | 金额
     * @param target the target currency | 目标货币
     * @param rate the exchange rate | 汇率
     * @return the converted money | 转换后的金额
     */
    public static Money convert(Money money, Currency target, BigDecimal rate) {
        return money.convertTo(target, rate);
    }

    /**
     * Convert money using exchange rate provider
     * 使用汇率提供者转换金额
     *
     * @param money the money | 金额
     * @param target the target currency | 目标货币
     * @param provider the exchange rate provider | 汇率提供者
     * @return the converted money | 转换后的金额
     */
    public static Money convert(Money money, Currency target, ExchangeRateProvider provider) {
        return provider.convert(money, target);
    }

    /**
     * Create exchange rate
     * 创建汇率
     *
     * @param source the source currency | 源货币
     * @param target the target currency | 目标货币
     * @param rate the rate | 汇率
     * @return the exchange rate | 汇率
     */
    public static ExchangeRate rate(Currency source, Currency target, BigDecimal rate) {
        return ExchangeRate.of(source, target, rate);
    }

    // ============ Formatting | 格式化 ============

    /**
     * Format money
     * 格式化金额
     *
     * @param money the money | 金额
     * @return the formatted string | 格式化字符串
     */
    public static String format(Money money) {
        return MoneyFormatUtil.format(money);
    }

    /**
     * Format money with currency code
     * 带货币代码格式化金额
     *
     * @param money the money | 金额
     * @return the formatted string | 格式化字符串
     */
    public static String formatWithCode(Money money) {
        return MoneyFormatUtil.formatWithCode(money);
    }

    /**
     * Format money in accounting style
     * 会计格式化金额
     *
     * @param money the money | 金额
     * @return the formatted string | 格式化字符串
     */
    public static String formatAccounting(Money money) {
        return MoneyFormatUtil.formatAccounting(money);
    }

    /**
     * Format money in compact style
     * 紧凑格式化金额
     *
     * @param money the money | 金额
     * @return the formatted string | 格式化字符串
     */
    public static String formatCompact(Money money) {
        return MoneyFormatUtil.formatCompact(money);
    }

    // ============ Validation | 验证 ============

    /**
     * Validate and parse amount string
     * 验证并解析金额字符串
     *
     * @param amountStr the amount string | 金额字符串
     * @return the parsed amount | 解析后的金额
     */
    public static BigDecimal validateAndParse(String amountStr) {
        return MoneyValidator.validateAndParse(amountStr);
    }

    /**
     * Validate money is positive
     * 验证金额为正
     *
     * @param money the money | 金额
     */
    public static void validatePositive(Money money) {
        MoneyValidator.validatePositive(money);
    }

    /**
     * Validate money is non-negative
     * 验证金额非负
     *
     * @param money the money | 金额
     */
    public static void validateNonNegative(Money money) {
        MoneyValidator.validateNonNegative(money);
    }

    /**
     * Check if amount string is valid
     * 检查金额字符串是否有效
     *
     * @param amountStr the amount string | 金额字符串
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(String amountStr) {
        return MoneyValidator.isValid(amountStr);
    }

    // ============ Comparison | 比较 ============

    /**
     * Check if amounts are equal
     * 检查金额是否相等
     *
     * @param m1 the first money | 第一个金额
     * @param m2 the second money | 第二个金额
     * @return true if equal | 如果相等返回true
     */
    public static boolean areEqual(Money m1, Money m2) {
        return MoneyCalcUtil.areEqual(m1, m2);
    }
}
