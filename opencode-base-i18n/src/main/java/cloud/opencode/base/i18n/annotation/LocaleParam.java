package cloud.opencode.base.i18n.annotation;

import java.lang.annotation.*;

/**
 * Annotation for marking method parameters as locale source
 * 用于标记方法参数为Locale来源的注解
 *
 * <p>This annotation indicates that a method parameter should be used
 * as the locale for i18n message resolution.</p>
 * <p>此注解表示方法参数应被用作国际化消息解析的Locale。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Parameter locale binding - 参数Locale绑定</li>
 *   <li>Type support (Locale, String, HttpServletRequest) - 类型支持</li>
 *   <li>Fallback support - 回退支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Direct Locale parameter
 * public String getMessage(String key, @LocaleParam Locale locale) { ... }
 *
 * // Language tag string
 * @LocaleParam(type = LocaleParam.Type.LANGUAGE_TAG)
 * public String getMessage(String key, String languageTag) { ... }
 *
 * // From request
 * public String getMessage(String key, @LocaleParam(type = LocaleParam.Type.REQUEST) HttpServletRequest request) { ... }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (annotation) - 线程安全: 是（注解）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LocaleParam {

    /**
     * The type of locale source
     * Locale来源的类型
     *
     * @return source type | 来源类型
     */
    Type type() default Type.LOCALE;

    /**
     * Whether to use default locale if the parameter is null
     * 如果参数为null是否使用默认Locale
     *
     * @return true to use default | 如果使用默认值返回true
     */
    boolean useDefault() default true;

    /**
     * The fallback locale language tag if parameter is null
     * 参数为null时的回退Locale语言标签
     *
     * @return fallback language tag | 回退语言标签
     */
    String fallback() default "";

    /**
     * Locale source types
     * Locale来源类型
     */
    enum Type {
        /**
         * Direct Locale object
         * 直接的Locale对象
         */
        LOCALE,

        /**
         * IETF BCP 47 language tag string (e.g., "zh-CN")
         * IETF BCP 47语言标签字符串（如"zh-CN"）
         */
        LANGUAGE_TAG,

        /**
         * Locale string (e.g., "zh_CN")
         * Locale字符串（如"zh_CN"）
         */
        LOCALE_STRING,

        /**
         * From HTTP request (Accept-Language header)
         * 从HTTP请求获取（Accept-Language头）
         */
        REQUEST,

        /**
         * From thread context
         * 从线程上下文获取
         */
        CONTEXT
    }
}
