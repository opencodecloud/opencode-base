package cloud.opencode.base.i18n.resolver;

import cloud.opencode.base.i18n.spi.LocaleResolver;

import java.util.Locale;

/**
 * ThreadLocal-based locale resolver
 * 基于ThreadLocal的Locale解析器
 *
 * <p>Stores the locale in a ThreadLocal for per-thread isolation.</p>
 * <p>将Locale存储在ThreadLocal中实现线程隔离。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe locale storage - 线程安全的Locale存储</li>
 *   <li>Per-thread isolation - 线程隔离</li>
 *   <li>InheritableThreadLocal support - 可继承ThreadLocal支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ThreadLocalLocaleResolver resolver = new ThreadLocalLocaleResolver();
 * resolver.setLocale(Locale.CHINESE);
 * Locale current = resolver.resolve(); // zh
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
public class ThreadLocalLocaleResolver implements LocaleResolver {

    private final ThreadLocal<Locale> localeHolder;
    private final Locale defaultLocale;

    /**
     * Creates a resolver with system default locale
     * 使用系统默认Locale创建解析器
     */
    public ThreadLocalLocaleResolver() {
        this(Locale.getDefault(), false);
    }

    /**
     * Creates a resolver with specified default locale
     * 使用指定的默认Locale创建解析器
     *
     * @param defaultLocale the default locale | 默认地区
     */
    public ThreadLocalLocaleResolver(Locale defaultLocale) {
        this(defaultLocale, false);
    }

    /**
     * Creates a resolver with inheritance option
     * 使用继承选项创建解析器
     *
     * @param defaultLocale the default locale | 默认地区
     * @param inheritable   whether to use InheritableThreadLocal | 是否使用可继承ThreadLocal
     */
    public ThreadLocalLocaleResolver(Locale defaultLocale, boolean inheritable) {
        this.defaultLocale = defaultLocale != null ? defaultLocale : Locale.getDefault();
        this.localeHolder = inheritable ? new InheritableThreadLocal<>() : new ThreadLocal<>();
    }

    @Override
    public Locale resolve() {
        Locale locale = localeHolder.get();
        return locale != null ? locale : defaultLocale;
    }

    @Override
    public void setLocale(Locale locale) {
        if (locale != null) {
            localeHolder.set(locale);
        } else {
            localeHolder.remove();
        }
    }

    @Override
    public void reset() {
        localeHolder.remove();
    }

    /**
     * Gets the default locale
     * 获取默认Locale
     *
     * @return default locale | 默认地区
     */
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Checks if a locale is explicitly set for current thread
     * 检查当前线程是否显式设置了Locale
     *
     * @return true if locale is set | 如果设置了返回true
     */
    public boolean isLocaleSet() {
        return localeHolder.get() != null;
    }
}
