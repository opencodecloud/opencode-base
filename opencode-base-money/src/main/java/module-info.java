/**
 * OpenCode Base Money Module
 * OpenCode 基础金额模块
 *
 * <p>Provides precise monetary amount handling with multi-currency support.</p>
 * <p>提供精确的货币金额处理，支持多币种。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Precise calculation (BigDecimal) - 精确计算</li>
 *   <li>Multi-currency support - 多币种支持</li>
 *   <li>Chinese uppercase conversion - 中文大写转换</li>
 *   <li>Amount allocation/splitting - 金额分摊</li>
 *   <li>Exchange rate conversion - 汇率转换</li>
 *   <li>Amount formatting - 金额格式化</li>
 *   <li>Validation utilities - 验证工具</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-money V1.0.0
 */
module cloud.opencode.base.money {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.money;
    exports cloud.opencode.base.money.calc;
    exports cloud.opencode.base.money.exchange;
    exports cloud.opencode.base.money.exception;
    exports cloud.opencode.base.money.format;
    exports cloud.opencode.base.money.validation;

    // SPI: Allow ServiceLoader to find ExchangeRateProvider implementations
    uses cloud.opencode.base.money.exchange.ExchangeRateProvider;
}
