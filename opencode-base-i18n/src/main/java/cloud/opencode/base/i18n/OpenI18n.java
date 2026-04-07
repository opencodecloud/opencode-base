package cloud.opencode.base.i18n;

import cloud.opencode.base.i18n.exception.OpenNoSuchMessageException;
import cloud.opencode.base.i18n.fallback.LocaleFallbackStrategy;
import cloud.opencode.base.i18n.formatter.DefaultMessageFormatter;
import cloud.opencode.base.i18n.handler.MissingKeyHandler;
import cloud.opencode.base.i18n.key.I18nKey;
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
 * <p>Main entry point for all i18n operations. Provides message retrieval, locale
 * management, global configuration, and integration with custom fallback strategies
 * and missing-key handlers.</p>
 * <p>所有国际化操作的主入口。提供消息获取、区域管理、全局配置以及与自定义回退策略
 * 和缺失键处理器的集成。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Simple message retrieval with positional/named parameters - 位置/命名参数简单消息获取</li>
 *   <li>Type-safe I18nKey support - 类型安全的 I18nKey 支持</li>
 *   <li>Custom locale fallback strategies - 自定义区域回退策略</li>
 *   <li>Missing key notification callbacks - 缺失键通知回调</li>
 *   <li>ThreadLocal locale management with scoped execution - 带作用域执行的 ThreadLocal 区域管理</li>
 *   <li>Thread-safe global configuration - 线程安全的全局配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic setup
 * OpenI18n.setMessageProvider(new ResourceBundleProvider("i18n/messages"));
 * OpenI18n.setDefaultLocale(Locale.ENGLISH);
 *
 * // Message retrieval
 * String msg = OpenI18n.get("user.welcome", "John");
 * String msg = OpenI18n.get("greeting", Locale.CHINESE, "张三");
 *
 * // With named parameters (IcuLikeFormatter)
 * OpenI18n.setMessageFormatter(new IcuLikeFormatter());
 * String msg = OpenI18n.get("files.count", Map.of("count", 5));
 *
 * // Type-safe key
 * String msg = AppMessage.WELCOME.get("Alice");
 *
 * // Scoped execution
 * OpenI18n.withLocale(Locale.JAPANESE, () -> {
 *     System.out.println(OpenI18n.get("greeting"));
 * });
 *
 * // Custom fallback chain
 * OpenI18n.setFallbackStrategy(ChainedLocaleFallback.builder()
 *     .chain(Locale.of("pt","BR"), Locale.of("pt","PT"), Locale.ENGLISH)
 *     .build());
 *
 * // Missing key collection (dev/test)
 * CollectingMissingKeyHandler collector = new CollectingMissingKeyHandler();
 * OpenI18n.setMissingKeyHandler(collector);
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
 * @since JDK 25, opencode-base-i18n V1.0.3
 */
public final class OpenI18n {

    private static volatile MessageProvider          provider;
    private static volatile LocaleResolver           resolver;
    private static volatile MessageFormatter         formatter;
    private static volatile Locale                   defaultLocale  = Locale.getDefault();
    private static volatile boolean                  throwOnMissing = false;
    private static volatile MissingKeyHandler        missingKeyHandler;
    private static volatile LocaleFallbackStrategy   fallbackStrategy;

    static {
        resolver  = new ThreadLocalLocaleResolver();
        formatter = new DefaultMessageFormatter();
    }

    private OpenI18n() {}

    // ==================== Message Retrieval | 消息获取 ====================

    /**
     * Gets a message for the current locale with positional arguments
     * 使用位置参数获取当前区域的消息
     *
     * @param key  the message key | 消息键
     * @param args positional format arguments | 位置格式化参数
     * @return formatted message | 格式化消息
     */
    public static String get(String key, Object... args) {
        return get(key, getCurrentLocale(), args);
    }

    /**
     * Gets a message for the specified locale with positional arguments
     * 使用位置参数获取指定区域的消息
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 区域
     * @param args   positional format arguments | 位置格式化参数
     * @return formatted message | 格式化消息
     */
    public static String get(String key, Locale locale, Object... args) {
        Optional<String> template = resolve(key, locale);
        if (template.isEmpty()) {
            handleMissing(key, locale);
            if (throwOnMissing) throw OpenNoSuchMessageException.messageNotFound(key, locale);
            return key;
        }
        return args.length == 0 ? template.get() : formatter.format(template.get(), locale, args);
    }

    /**
     * Gets a message with named parameters for the current locale
     * 使用命名参数获取当前区域的消息
     *
     * @param key    the message key | 消息键
     * @param params named parameters | 命名参数
     * @return formatted message | 格式化消息
     */
    public static String get(String key, Map<String, Object> params) {
        return get(key, getCurrentLocale(), params);
    }

    /**
     * Gets a message with named parameters for the specified locale
     * 使用命名参数获取指定区域的消息
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 区域
     * @param params named parameters | 命名参数
     * @return formatted message | 格式化消息
     */
    public static String get(String key, Locale locale, Map<String, Object> params) {
        Optional<String> template = resolve(key, locale);
        if (template.isEmpty()) {
            handleMissing(key, locale);
            if (throwOnMissing) throw OpenNoSuchMessageException.messageNotFound(key, locale);
            return key;
        }
        return formatter.format(template.get(), locale, params);
    }

    /**
     * Gets a message via a type-safe I18nKey with positional arguments
     * 通过类型安全的 I18nKey 和位置参数获取消息
     *
     * @param i18nKey the message key | I18n 键
     * @param args    positional arguments | 位置参数
     * @return formatted message | 格式化消息
     */
    public static String get(I18nKey i18nKey, Object... args) {
        return get(i18nKey.key(), args);
    }

    /**
     * Gets a message via a type-safe I18nKey for the specified locale
     * 通过类型安全的 I18nKey 获取指定区域的消息
     *
     * @param i18nKey the message key | I18n 键
     * @param locale  the locale | 区域
     * @param args    positional arguments | 位置参数
     * @return formatted message | 格式化消息
     */
    public static String get(I18nKey i18nKey, Locale locale, Object... args) {
        return get(i18nKey.key(), locale, args);
    }

    /**
     * Gets a message or the provided default value if not found
     * 获取消息，未找到时返回提供的默认值
     *
     * @param key          the message key | 消息键
     * @param defaultValue default value | 默认值
     * @param args         positional arguments | 位置参数
     * @return formatted message or default | 格式化消息或默认值
     */
    public static String getOrDefault(String key, String defaultValue, Object... args) {
        return getOrDefault(key, getCurrentLocale(), defaultValue, args);
    }

    /**
     * Gets a message or the provided default value if not found
     * 获取消息，未找到时返回提供的默认值
     *
     * @param key          the message key | 消息键
     * @param locale       the locale | 区域
     * @param defaultValue default value | 默认值
     * @param args         positional arguments | 位置参数
     * @return formatted message or default | 格式化消息或默认值
     */
    public static String getOrDefault(String key, Locale locale, String defaultValue, Object... args) {
        Optional<String> template = resolve(key, locale);
        if (template.isEmpty()) return defaultValue;
        return args.length == 0 ? template.get() : formatter.format(template.get(), locale, args);
    }

    /**
     * Checks if a message exists for the current locale
     * 检查当前区域是否存在消息
     *
     * @param key the message key | 消息键
     * @return true if message exists | 消息存在则返回 true
     */
    public static boolean contains(String key) {
        return contains(key, getCurrentLocale());
    }

    /**
     * Checks if a message exists for the specified locale
     * 检查指定区域是否存在消息
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 区域
     * @return true if message exists | 消息存在则返回 true
     */
    public static boolean contains(String key, Locale locale) {
        return getProvider().containsMessage(key, locale);
    }

    // ==================== Locale Management | 区域管理 ====================

    /**
     * Gets the current thread's locale
     * 获取当前线程的区域
     *
     * @return current locale | 当前区域
     */
    public static Locale getCurrentLocale() {
        return resolver.resolve();
    }

    /**
     * Sets the current thread's locale
     * 设置当前线程的区域
     *
     * @param locale the locale | 区域
     */
    public static void setCurrentLocale(Locale locale) {
        resolver.setLocale(locale);
    }

    /**
     * Resets the current thread's locale to the default
     * 将当前线程的区域重置为默认值
     */
    public static void resetLocale() {
        resolver.reset();
    }

    /**
     * Executes the given action in the context of the specified locale
     * 在指定区域的上下文中执行给定操作
     *
     * @param locale   the locale | 区域
     * @param runnable the action | 操作
     */
    public static void withLocale(Locale locale, Runnable runnable) {
        Locale previous = getCurrentLocale();
        try {
            setCurrentLocale(locale);
            runnable.run();
        } finally {
            if (previous != null) setCurrentLocale(previous);
            else resetLocale();
        }
    }

    /**
     * Executes the given supplier in the context of the specified locale and returns the result
     * 在指定区域的上下文中执行给定供应者并返回结果
     *
     * @param locale   the locale | 区域
     * @param supplier the supplier | 供应者
     * @param <T>      result type | 返回类型
     * @return the result | 结果
     */
    public static <T> T withLocale(Locale locale, Supplier<T> supplier) {
        Locale previous = getCurrentLocale();
        try {
            setCurrentLocale(locale);
            return supplier.get();
        } finally {
            if (previous != null) setCurrentLocale(previous);
            else resetLocale();
        }
    }

    // ==================== Global Configuration | 全局配置 ====================

    /**
     * Sets the global locale resolver
     * 设置全局区域解析器
     *
     * @param localeResolver the resolver | 解析器
     */
    public static void setLocaleResolver(LocaleResolver localeResolver) {
        resolver = Objects.requireNonNull(localeResolver, "LocaleResolver must not be null");
    }

    /**
     * Sets the global message provider
     * 设置全局消息提供者
     *
     * @param messageProvider the provider | 提供者
     */
    public static void setMessageProvider(MessageProvider messageProvider) {
        provider = Objects.requireNonNull(messageProvider, "MessageProvider must not be null");
    }

    /**
     * Sets the global message formatter
     * 设置全局消息格式化器
     *
     * @param messageFormatter the formatter | 格式化器
     */
    public static void setMessageFormatter(MessageFormatter messageFormatter) {
        formatter = Objects.requireNonNull(messageFormatter, "MessageFormatter must not be null");
    }

    /**
     * Sets the global default locale
     * 设置全局默认区域
     *
     * @param locale the default locale | 默认区域
     */
    public static void setDefaultLocale(Locale locale) {
        defaultLocale = Objects.requireNonNull(locale, "Default locale must not be null");
    }

    /**
     * Sets whether to throw an exception when a message key is not found
     * 设置消息键未找到时是否抛出异常
     *
     * @param throwOnMissingMessage true to throw | 为 true 时抛出异常
     */
    public static void setThrowOnMissingMessage(boolean throwOnMissingMessage) {
        throwOnMissing = throwOnMissingMessage;
    }

    /**
     * Sets the handler to be called when a message key is not found
     * 设置消息键未找到时调用的处理器
     *
     * @param handler the missing key handler (null to disable) | 缺失键处理器（null 禁用）
     */
    public static void setMissingKeyHandler(MissingKeyHandler handler) {
        missingKeyHandler = handler;
    }

    /**
     * Sets a custom locale fallback strategy for message resolution
     * 设置消息解析的自定义区域回退策略
     *
     * @param strategy the fallback strategy (null to use default two-level fallback) | 回退策略
     */
    public static void setFallbackStrategy(LocaleFallbackStrategy strategy) {
        fallbackStrategy = strategy;
    }

    /**
     * Returns the current MessageSource view of the configured provider and formatter
     * 返回当前配置的提供者和格式化器的 MessageSource 视图
     *
     * @return message source | 消息源
     */
    public static MessageSource getMessageSource() {
        return new DefaultMessageSource(getProvider(), formatter);
    }

    /**
     * Refreshes the message provider cache and formatter caches
     * 刷新消息提供者缓存和格式化器缓存
     */
    public static void refresh() {
        MessageProvider p = provider;
        if (p != null) p.refresh();
        MessageFormatter f = formatter;
        if (f != null) f.clearCache();
    }

    // ==================== Internal Methods | 内部方法 ====================

    /**
     * Resolves a message template using the configured fallback strategy
     * 使用配置的回退策略解析消息模板
     */
    private static Optional<String> resolve(String key, Locale locale) {
        MessageProvider mp = getProvider();
        LocaleFallbackStrategy strategy = fallbackStrategy;

        if (strategy != null) {
            List<Locale> chain = strategy.getFallbackChain(locale);
            for (Locale candidate : chain) {
                Optional<String> tmpl = mp.getMessageTemplate(key, candidate);
                if (tmpl.isPresent()) return tmpl;
            }
        } else {
            // Default two-level fallback: requested locale → default locale
            Optional<String> tmpl = mp.getMessageTemplate(key, locale);
            if (tmpl.isPresent()) return tmpl;
            if (!locale.equals(defaultLocale)) {
                return mp.getMessageTemplate(key, defaultLocale);
            }
        }
        return Optional.empty();
    }

    /** Notifies the missing key handler if configured | 通知缺失键处理器（如果已配置） */
    private static void handleMissing(String key, Locale locale) {
        MissingKeyHandler handler = missingKeyHandler;
        if (handler != null) {
            try {
                handler.onMissingKey(key, locale);
            } catch (Exception ignored) {
                // Never let handler exceptions propagate
            }
        }
    }

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

    private static MessageProvider discoverProviders() {
        ServiceLoader<MessageBundleProvider> loader = ServiceLoader.load(MessageBundleProvider.class);
        List<MessageBundleProvider> bundles = new ArrayList<>();
        loader.forEach(bundles::add);

        if (bundles.isEmpty()) {
            return new ResourceBundleProvider("i18n/messages");
        }
        bundles.sort(Comparator.comparingInt(MessageBundleProvider::priority));
        ChainMessageProvider.Builder builder = ChainMessageProvider.builder();
        for (MessageBundleProvider bundle : bundles) {
            builder.add(new ResourceBundleProvider(bundle.baseName()));
        }
        return builder.build();
    }

    // ==================== DefaultMessageSource | 内部消息源实现 ====================

    private static final class DefaultMessageSource implements MessageSource {

        private final MessageProvider  provider;
        private final MessageFormatter formatter;

        DefaultMessageSource(MessageProvider provider, MessageFormatter formatter) {
            this.provider  = provider;
            this.formatter = formatter;
        }

        @Override
        public String getMessage(String key, Locale locale, Object... args) {
            return OpenI18n.get(key, locale, args);
        }

        @Override
        public Optional<String> getMessageOptional(String key, Locale locale, Object... args) {
            Optional<String> tmpl = provider.getMessageTemplate(key, locale);
            if (tmpl.isEmpty()) return Optional.empty();
            return Optional.of(args.length == 0 ? tmpl.get() : formatter.format(tmpl.get(), locale, args));
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
        public Set<String> getKeys(Locale locale) {
            return provider.getKeys(locale);
        }

        @Override
        public Set<Locale> getSupportedLocales() {
            return provider.getSupportedLocales();
        }
    }
}
