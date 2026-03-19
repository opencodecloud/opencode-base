package cloud.opencode.base.i18n.support;

import cloud.opencode.base.i18n.LocaleContext;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Holder for thread-bound LocaleContext
 * 线程绑定的LocaleContext持有者
 *
 * <p>Provides static methods to get/set the LocaleContext for the current thread.
 * Supports both regular ThreadLocal and InheritableThreadLocal modes.</p>
 * <p>提供静态方法来获取/设置当前线程的LocaleContext。
 * 支持普通ThreadLocal和InheritableThreadLocal模式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-bound locale context - 线程绑定的Locale上下文</li>
 *   <li>Inheritable mode support - 可继承模式支持</li>
 *   <li>Convenient access methods - 便捷的访问方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Set locale context
 * LocaleContextHolder.setLocale(Locale.CHINESE);
 *
 * // Get current locale
 * Locale locale = LocaleContextHolder.getLocale();
 *
 * // Reset to default
 * LocaleContextHolder.reset();
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
public final class LocaleContextHolder {

    private static final ThreadLocal<LocaleContext> localeContextHolder = new ThreadLocal<>();
    private static final ThreadLocal<LocaleContext> inheritableLocaleContextHolder = new InheritableThreadLocal<>();
    private static volatile Locale defaultLocale = Locale.getDefault();
    private static volatile TimeZone defaultTimeZone = TimeZone.getDefault();

    private LocaleContextHolder() {
    }

    /**
     * Sets the locale context for the current thread
     * 为当前线程设置Locale上下文
     *
     * @param context the locale context | Locale上下文
     */
    public static void setLocaleContext(LocaleContext context) {
        setLocaleContext(context, false);
    }

    /**
     * Sets the locale context with inheritance option
     * 使用继承选项设置Locale上下文
     *
     * @param context     the locale context | Locale上下文
     * @param inheritable whether to use inheritable thread local | 是否使用可继承的线程本地变量
     */
    public static void setLocaleContext(LocaleContext context, boolean inheritable) {
        if (context == null) {
            localeContextHolder.remove();
            inheritableLocaleContextHolder.remove();
        } else if (inheritable) {
            inheritableLocaleContextHolder.set(context);
            localeContextHolder.remove();
        } else {
            localeContextHolder.set(context);
            inheritableLocaleContextHolder.remove();
        }
    }

    /**
     * Gets the locale context for the current thread
     * 获取当前线程的Locale上下文
     *
     * @return locale context or null | Locale上下文或null
     */
    public static LocaleContext getLocaleContext() {
        LocaleContext context = localeContextHolder.get();
        if (context == null) {
            context = inheritableLocaleContextHolder.get();
        }
        return context;
    }

    /**
     * Sets the locale for the current thread
     * 为当前线程设置Locale
     *
     * @param locale the locale | 地区
     */
    public static void setLocale(Locale locale) {
        setLocale(locale, false);
    }

    /**
     * Sets the locale with inheritance option
     * 使用继承选项设置Locale
     *
     * @param locale      the locale | 地区
     * @param inheritable whether to use inheritable thread local | 是否使用可继承的线程本地变量
     */
    public static void setLocale(Locale locale, boolean inheritable) {
        LocaleContext context = getLocaleContext();
        TimeZone timeZone = context != null ? context.timeZone() : defaultTimeZone;
        setLocaleContext(LocaleContext.of(locale, timeZone), inheritable);
    }

    /**
     * Gets the locale for the current thread
     * 获取当前线程的Locale
     *
     * @return locale | 地区
     */
    public static Locale getLocale() {
        LocaleContext context = getLocaleContext();
        return context != null ? context.locale() : defaultLocale;
    }

    /**
     * Sets the time zone for the current thread
     * 为当前线程设置时区
     *
     * @param timeZone the time zone | 时区
     */
    public static void setTimeZone(TimeZone timeZone) {
        setTimeZone(timeZone, false);
    }

    /**
     * Sets the time zone with inheritance option
     * 使用继承选项设置时区
     *
     * @param timeZone    the time zone | 时区
     * @param inheritable whether to use inheritable thread local | 是否使用可继承的线程本地变量
     */
    public static void setTimeZone(TimeZone timeZone, boolean inheritable) {
        LocaleContext context = getLocaleContext();
        Locale locale = context != null ? context.locale() : defaultLocale;
        setLocaleContext(LocaleContext.of(locale, timeZone), inheritable);
    }

    /**
     * Gets the time zone for the current thread
     * 获取当前线程的时区
     *
     * @return time zone | 时区
     */
    public static TimeZone getTimeZone() {
        LocaleContext context = getLocaleContext();
        return context != null && context.timeZone() != null ? context.timeZone() : defaultTimeZone;
    }

    /**
     * Resets the locale context for the current thread
     * 重置当前线程的Locale上下文
     */
    public static void reset() {
        localeContextHolder.remove();
        inheritableLocaleContextHolder.remove();
    }

    /**
     * Sets the default locale
     * 设置默认Locale
     *
     * @param locale the default locale | 默认地区
     */
    public static void setDefaultLocale(Locale locale) {
        defaultLocale = locale != null ? locale : Locale.getDefault();
    }

    /**
     * Gets the default locale
     * 获取默认Locale
     *
     * @return default locale | 默认地区
     */
    public static Locale getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Sets the default time zone
     * 设置默认时区
     *
     * @param timeZone the default time zone | 默认时区
     */
    public static void setDefaultTimeZone(TimeZone timeZone) {
        defaultTimeZone = timeZone != null ? timeZone : TimeZone.getDefault();
    }

    /**
     * Gets the default time zone
     * 获取默认时区
     *
     * @return default time zone | 默认时区
     */
    public static TimeZone getDefaultTimeZone() {
        return defaultTimeZone;
    }

    /**
     * Sets whether the locale context should be inheritable
     * 设置Locale上下文是否可被子线程继承
     *
     * <p>When set to true, child threads will inherit the locale context
     * from parent thread. When set to false, only the current thread
     * can access the locale context.</p>
     * <p>设置为true时，子线程将继承父线程的Locale上下文。
     * 设置为false时，只有当前线程可以访问Locale上下文。</p>
     *
     * @param inheritable true for inheritable mode | true表示可继承模式
     */
    public static void setInheritable(boolean inheritable) {
        LocaleContext context = getLocaleContext();
        if (context != null) {
            setLocaleContext(context, inheritable);
        }
    }
}
