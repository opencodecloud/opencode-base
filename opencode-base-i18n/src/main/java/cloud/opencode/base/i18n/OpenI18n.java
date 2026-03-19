package cloud.opencode.base.i18n;

import cloud.opencode.base.i18n.exception.OpenNoSuchMessageException;
import cloud.opencode.base.i18n.formatter.DefaultMessageFormatter;
import cloud.opencode.base.i18n.provider.ChainMessageProvider;
import cloud.opencode.base.i18n.provider.ResourceBundleProvider;
import cloud.opencode.base.i18n.resolver.ThreadLocalLocaleResolver;
import cloud.opencode.base.i18n.spi.LocaleResolver;
import cloud.opencode.base.i18n.spi.MessageBundleProvider;
import cloud.opencode.base.i18n.spi.MessageFormatter;
import cloud.opencode.base.i18n.spi.MessageProvider;

import java.util.*;
import java.util.function.Supplier;

/**
 * Internationalization utility facade class
 * 国际化工具门面类
 *
 * <p>Provides the simplest API for retrieving internationalized messages.
 * This is the main entry point for all i18n operations.</p>
 * <p>提供最简洁的API来获取国际化消息。这是所有国际化操作的主入口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Simple message retrieval - 简单消息获取</li>
 *   <li>Named parameter support - 命名参数支持</li>
 *   <li>ThreadLocal locale management - ThreadLocal地区管理</li>
 *   <li>Global configuration - 全局配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get message with current locale
 * String msg = OpenI18n.get("user.welcome", "John");
 *
 * // Get message with specific locale
 * String chineseMsg = OpenI18n.get("user.welcome", Locale.CHINESE, "张三");
 *
 * // Execute with specific locale
 * OpenI18n.withLocale(Locale.JAPANESE, () -> {
 *     System.out.println(OpenI18n.get("greeting"));
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
public final class OpenI18n {

    private static volatile MessageProvider provider;
    private static volatile LocaleResolver resolver;
    private static volatile MessageFormatter formatter;
    private static volatile Locale defaultLocale = Locale.getDefault();
    private static volatile boolean throwOnMissing = false;

    static {
        // Default implementations
        resolver = new ThreadLocalLocaleResolver();
        formatter = new DefaultMessageFormatter();
    }

    private OpenI18n() {
        // Utility class
    }

    // ==================== Message Retrieval | 消息获取 ====================

    /**
     * Gets a message using current locale
     * 使用当前Locale获取消息
     *
     * @param key  the message key | 消息键
     * @param args format arguments | 格式化参数
     * @return formatted message | 格式化后的消息
     */
    public static String get(String key, Object... args) {
        return get(key, getCurrentLocale(), args);
    }

    /**
     * Gets a message using specified locale
     * 使用指定Locale获取消息
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 地区
     * @param args   format arguments | 格式化参数
     * @return formatted message | 格式化后的消息
     */
    public static String get(String key, Locale locale, Object... args) {
        Optional<String> template = getProvider().getMessageTemplate(key, locale);

        if (template.isEmpty()) {
            // Try default locale fallback
            if (!locale.equals(defaultLocale)) {
                template = getProvider().getMessageTemplate(key, defaultLocale);
            }
        }

        if (template.isEmpty()) {
            if (throwOnMissing) {
                throw OpenNoSuchMessageException.messageNotFound(key, locale);
            }
            return key;
        }

        return args.length == 0 ? template.get() : formatter.format(template.get(), locale, args);
    }

    /**
     * Gets a message with default value
     * 获取消息，不存在时返回默认值
     *
     * @param key          the message key | 消息键
     * @param defaultValue default value | 默认值
     * @param args         format arguments | 格式化参数
     * @return formatted message or default | 格式化后的消息或默认值
     */
    public static String getOrDefault(String key, String defaultValue, Object... args) {
        return getOrDefault(key, getCurrentLocale(), defaultValue, args);
    }

    /**
     * Gets a message with default value
     * 获取消息，不存在时返回默认值
     *
     * @param key          the message key | 消息键
     * @param locale       the locale | 地区
     * @param defaultValue default value | 默认值
     * @param args         format arguments | 格式化参数
     * @return formatted message or default | 格式化后的消息或默认值
     */
    public static String getOrDefault(String key, Locale locale, String defaultValue, Object... args) {
        Optional<String> template = getProvider().getMessageTemplate(key, locale);

        if (template.isEmpty() && !locale.equals(defaultLocale)) {
            template = getProvider().getMessageTemplate(key, defaultLocale);
        }

        if (template.isEmpty()) {
            return defaultValue;
        }

        return args.length == 0 ? template.get() : formatter.format(template.get(), locale, args);
    }

    /**
     * Gets a message with named parameters
     * 使用命名参数获取消息
     *
     * @param key    the message key | 消息键
     * @param params named parameters | 命名参数
     * @return formatted message | 格式化后的消息
     */
    public static String get(String key, Map<String, Object> params) {
        return get(key, getCurrentLocale(), params);
    }

    /**
     * Gets a message with named parameters
     * 使用命名参数获取消息
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 地区
     * @param params named parameters | 命名参数
     * @return formatted message | 格式化后的消息
     */
    public static String get(String key, Locale locale, Map<String, Object> params) {
        Optional<String> template = getProvider().getMessageTemplate(key, locale);

        if (template.isEmpty() && !locale.equals(defaultLocale)) {
            template = getProvider().getMessageTemplate(key, defaultLocale);
        }

        if (template.isEmpty()) {
            if (throwOnMissing) {
                throw OpenNoSuchMessageException.messageNotFound(key, locale);
            }
            return key;
        }

        return formatter.format(template.get(), locale, params);
    }

    /**
     * Checks if a message exists
     * 判断消息是否存在
     *
     * @param key the message key | 消息键
     * @return true if exists | 如果存在返回true
     */
    public static boolean contains(String key) {
        return contains(key, getCurrentLocale());
    }

    /**
     * Checks if a message exists
     * 判断消息是否存在
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 地区
     * @return true if exists | 如果存在返回true
     */
    public static boolean contains(String key, Locale locale) {
        return getProvider().containsMessage(key, locale);
    }

    // ==================== Locale Management | Locale管理 ====================

    /**
     * Gets the current locale
     * 获取当前Locale
     *
     * @return current locale | 当前Locale
     */
    public static Locale getCurrentLocale() {
        return resolver.resolve();
    }

    /**
     * Sets the current thread locale
     * 设置当前线程Locale
     *
     * @param locale the locale | 地区
     */
    public static void setCurrentLocale(Locale locale) {
        resolver.setLocale(locale);
    }

    /**
     * Resets the current thread locale
     * 重置当前线程Locale
     */
    public static void resetLocale() {
        resolver.reset();
    }

    /**
     * Executes with specified locale
     * 在指定Locale下执行
     *
     * @param locale   the locale | 地区
     * @param runnable the runnable | 执行逻辑
     */
    public static void withLocale(Locale locale, Runnable runnable) {
        Locale previous = getCurrentLocale();
        try {
            setCurrentLocale(locale);
            runnable.run();
        } finally {
            if (previous != null) {
                setCurrentLocale(previous);
            } else {
                resetLocale();
            }
        }
    }

    /**
     * Executes with specified locale and returns result
     * 在指定Locale下执行并返回结果
     *
     * @param locale   the locale | 地区
     * @param supplier the supplier | 执行逻辑
     * @param <T>      result type | 返回类型
     * @return result | 结果
     */
    public static <T> T withLocale(Locale locale, Supplier<T> supplier) {
        Locale previous = getCurrentLocale();
        try {
            setCurrentLocale(locale);
            return supplier.get();
        } finally {
            if (previous != null) {
                setCurrentLocale(previous);
            } else {
                resetLocale();
            }
        }
    }

    // ==================== Global Configuration | 全局配置 ====================

    /**
     * Sets the global locale resolver
     * 设置全局Locale解析器
     *
     * @param localeResolver the resolver | 解析器
     */
    public static void setLocaleResolver(LocaleResolver localeResolver) {
        resolver = localeResolver;
    }

    /**
     * Sets the global message provider
     * 设置全局消息提供者
     *
     * @param messageProvider the provider | 提供者
     */
    public static void setMessageProvider(MessageProvider messageProvider) {
        provider = messageProvider;
    }

    /**
     * Sets the global message formatter
     * 设置全局消息格式化器
     *
     * @param messageFormatter the formatter | 格式化器
     */
    public static void setMessageFormatter(MessageFormatter messageFormatter) {
        formatter = messageFormatter;
    }

    /**
     * Sets the default locale
     * 设置默认Locale
     *
     * @param locale the default locale | 默认地区
     */
    public static void setDefaultLocale(Locale locale) {
        defaultLocale = locale;
    }

    /**
     * Sets whether to throw exception on missing message
     * 设置是否在消息未找到时抛出异常
     *
     * @param throwOnMissingMessage whether to throw | 是否抛出
     */
    public static void setThrowOnMissingMessage(boolean throwOnMissingMessage) {
        throwOnMissing = throwOnMissingMessage;
    }

    /**
     * Gets the message source
     * 获取消息源
     *
     * @return message source | 消息源
     */
    public static MessageSource getMessageSource() {
        return new DefaultMessageSource(getProvider(), formatter);
    }

    /**
     * Refreshes message cache
     * 刷新消息缓存
     */
    public static void refresh() {
        if (provider != null) {
            provider.refresh();
        }
        if (formatter != null) {
            formatter.clearCache();
        }
    }

    // ==================== Internal Methods | 内部方法 ====================

    private static MessageProvider getProvider() {
        if (provider == null) {
            synchronized (OpenI18n.class) {
                if (provider == null) {
                    provider = discoverProviders();
                }
            }
        }
        return provider;
    }

    /**
     * Discovers message bundles via ServiceLoader and builds a ChainMessageProvider
     * 通过ServiceLoader发现消息包并构建ChainMessageProvider
     */
    private static MessageProvider discoverProviders() {
        ServiceLoader<MessageBundleProvider> loader = ServiceLoader.load(MessageBundleProvider.class);
        List<MessageBundleProvider> bundles = new ArrayList<>();
        loader.forEach(bundles::add);

        if (bundles.isEmpty()) {
            // Fallback to default bundle if no SPI providers found
            return new ResourceBundleProvider("i18n/messages");
        }

        // Sort by priority (lower value = higher priority)
        bundles.sort(Comparator.comparingInt(MessageBundleProvider::priority));

        ChainMessageProvider.Builder builder = ChainMessageProvider.builder();
        for (MessageBundleProvider bundle : bundles) {
            builder.add(new ResourceBundleProvider(bundle.baseName()));
        }
        return builder.build();
    }

    /**
     * Default MessageSource implementation
     */
    private static class DefaultMessageSource implements MessageSource {
        private final MessageProvider provider;
        private final MessageFormatter formatter;

        DefaultMessageSource(MessageProvider provider, MessageFormatter formatter) {
            this.provider = provider;
            this.formatter = formatter;
        }

        @Override
        public String getMessage(String key, Locale locale, Object... args) {
            return OpenI18n.get(key, locale, args);
        }

        @Override
        public Optional<String> getMessageOptional(String key, Locale locale, Object... args) {
            Optional<String> template = provider.getMessageTemplate(key, locale);
            if (template.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(args.length == 0 ? template.get() : formatter.format(template.get(), locale, args));
        }

        @Override
        public Optional<String> getMessageTemplate(String key, Locale locale) {
            return provider.getMessageTemplate(key, locale);
        }

        @Override
        public boolean containsMessage(String key, Locale locale) {
            return provider.containsMessage(key, locale);
        }

        @Override
        public java.util.Set<String> getKeys(Locale locale) {
            return provider.getKeys(locale);
        }

        @Override
        public java.util.Set<Locale> getSupportedLocales() {
            return provider.getSupportedLocales();
        }
    }
}
