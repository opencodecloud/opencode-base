/**
 * OpenCode Base I18n Module
 * OpenCode 基础国际化模块
 *
 * <p>Provides internationalization and localization utilities based on JDK 25,
 * including message resolution, locale negotiation, formatting, and SPI-based provider extension.</p>
 * <p>提供基于 JDK 25 的国际化与本地化工具，包括消息解析、区域协商、格式化和基于 SPI 的提供者扩展。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Message Bundle Resolution - 消息包解析</li>
 *   <li>Locale Negotiation - 区域协商</li>
 *   <li>Parameterized Message Formatting - 参数化消息格式化</li>
 *   <li>Annotation-Driven I18n - 注解驱动国际化</li>
 *   <li>SPI Message Provider - SPI 消息提供者</li>
 *   <li>Multiple Bundle Support - 多消息包支持</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
module cloud.opencode.base.i18n {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.i18n;
    exports cloud.opencode.base.i18n.annotation;
    exports cloud.opencode.base.i18n.exception;
    exports cloud.opencode.base.i18n.formatter;
    exports cloud.opencode.base.i18n.provider;
    exports cloud.opencode.base.i18n.resolver;
    exports cloud.opencode.base.i18n.spi;
    exports cloud.opencode.base.i18n.support;

    // SPI: I18n extension points
    uses cloud.opencode.base.i18n.spi.MessageProvider;
    uses cloud.opencode.base.i18n.spi.LocaleResolver;
    uses cloud.opencode.base.i18n.spi.MessageBundleProvider;
    uses cloud.opencode.base.i18n.spi.MessageFormatter;
}
