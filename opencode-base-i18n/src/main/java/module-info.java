/**
 * OpenCode Base I18n Module
 * OpenCode 基础国际化模块
 *
 * <p>Provides comprehensive internationalization and localization utilities based on JDK 25,
 * including CLDR plural rules, ICU-like formatting, type-safe message keys, custom locale
 * fallback chains, bundle validation, and SPI-based provider extension.</p>
 * <p>提供基于 JDK 25 的完整国际化与本地化工具，包括 CLDR 复数规则、ICU 风格格式化、
 * 类型安全消息键、自定义区域回退链、包验证和基于 SPI 的提供者扩展。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>CLDR Plural Rules (50+ languages) - CLDR 复数规则（50+ 语言）</li>
 *   <li>ICU-like Message Formatting (no single-quote escaping) - ICU 风格消息格式化（无需单引号转义）</li>
 *   <li>Select/Gender Formatting - 选择/性别格式化</li>
 *   <li>Type-safe I18nKey and I18nEnum - 类型安全的 I18nKey 和 I18nEnum</li>
 *   <li>Custom Locale Fallback Chains - 自定义区域回退链</li>
 *   <li>Bundle Validation and Coverage - 包验证和覆盖率</li>
 *   <li>Missing Key Handler Callbacks - 缺失键处理回调</li>
 *   <li>SPI Message Provider, Resolver, Formatter - SPI 消息提供者、解析器、格式化器</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.3
 */
module cloud.opencode.base.i18n {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.i18n;
    exports cloud.opencode.base.i18n.annotation;
    exports cloud.opencode.base.i18n.exception;
    exports cloud.opencode.base.i18n.fallback;
    exports cloud.opencode.base.i18n.formatter;
    exports cloud.opencode.base.i18n.handler;
    exports cloud.opencode.base.i18n.key;
    exports cloud.opencode.base.i18n.plural;
    exports cloud.opencode.base.i18n.provider;
    exports cloud.opencode.base.i18n.resolver;
    exports cloud.opencode.base.i18n.select;
    exports cloud.opencode.base.i18n.spi;
    exports cloud.opencode.base.i18n.support;
    exports cloud.opencode.base.i18n.validation;

    // SPI extension points
    uses cloud.opencode.base.i18n.spi.MessageProvider;
    uses cloud.opencode.base.i18n.spi.LocaleResolver;
    uses cloud.opencode.base.i18n.spi.MessageBundleProvider;
    uses cloud.opencode.base.i18n.spi.MessageFormatter;
    uses cloud.opencode.base.i18n.fallback.LocaleFallbackStrategy;
}
