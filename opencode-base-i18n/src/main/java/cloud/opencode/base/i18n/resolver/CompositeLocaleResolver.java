package cloud.opencode.base.i18n.resolver;

import cloud.opencode.base.i18n.spi.LocaleResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Composite locale resolver that combines multiple resolvers
 * 组合多个解析器的复合Locale解析器
 *
 * <p>Tries multiple resolvers in order until one returns a valid locale.
 * Supports mutable locale through the first mutable resolver.</p>
 * <p>按顺序尝试多个解析器，直到返回有效的Locale。通过第一个可变解析器支持可变Locale。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple resolver chaining - 多解析器链接</li>
 *   <li>Fallback support - 回退支持</li>
 *   <li>Builder pattern - 构建器模式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CompositeLocaleResolver resolver = CompositeLocaleResolver.builder()
 *     .add(new ThreadLocalLocaleResolver())
 *     .add(new AcceptHeaderLocaleResolver(headerSupplier))
 *     .add(new FixedLocaleResolver(Locale.ENGLISH))
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on underlying resolvers - 线程安全: 取决于底层解析器</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
public class CompositeLocaleResolver implements LocaleResolver {

    private final List<LocaleResolver> resolvers;
    private final Locale defaultLocale;

    /**
     * Creates a composite resolver with a list of resolvers
     * 使用解析器列表创建复合解析器
     *
     * @param resolvers the resolvers | 解析器列表
     */
    public CompositeLocaleResolver(List<LocaleResolver> resolvers) {
        this(resolvers, Locale.getDefault());
    }

    /**
     * Creates a composite resolver with resolvers and default locale
     * 使用解析器列表和默认Locale创建复合解析器
     *
     * @param resolvers     the resolvers | 解析器列表
     * @param defaultLocale the default locale | 默认地区
     */
    public CompositeLocaleResolver(List<LocaleResolver> resolvers, Locale defaultLocale) {
        this.resolvers = new ArrayList<>(resolvers);
        this.defaultLocale = defaultLocale != null ? defaultLocale : Locale.getDefault();
    }

    @Override
    public Locale resolve() {
        for (LocaleResolver resolver : resolvers) {
            try {
                Locale locale = resolver.resolve();
                if (locale != null) {
                    return locale;
                }
            } catch (Exception e) {
                // Continue to next resolver
            }
        }
        return defaultLocale;
    }

    @Override
    public void setLocale(Locale locale) {
        // Delegate to the first resolver that supports it
        for (LocaleResolver resolver : resolvers) {
            try {
                resolver.setLocale(locale);
                return;
            } catch (UnsupportedOperationException e) {
                // Continue to next resolver
            }
        }
        throw new UnsupportedOperationException("No resolver supports setting locale");
    }

    @Override
    public void reset() {
        for (LocaleResolver resolver : resolvers) {
            try {
                resolver.reset();
            } catch (Exception e) {
                // Ignore reset failures
            }
        }
    }

    /**
     * Adds a resolver to the composite
     * 向复合解析器添加解析器
     *
     * @param resolver the resolver | 解析器
     * @return this composite | 此复合解析器
     */
    public CompositeLocaleResolver addResolver(LocaleResolver resolver) {
        resolvers.add(resolver);
        return this;
    }

    /**
     * Gets the number of resolvers
     * 获取解析器数量
     *
     * @return resolver count | 解析器数量
     */
    public int size() {
        return resolvers.size();
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
     * Creates a builder
     * 创建构建器
     *
     * @return builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for CompositeLocaleResolver
     * CompositeLocaleResolver的构建器
     */
    public static class Builder {
        private final List<LocaleResolver> resolvers = new ArrayList<>();
        private Locale defaultLocale = Locale.getDefault();

        /**
         * Adds a resolver
         * 添加解析器
         *
         * @param resolver the resolver | 解析器
         * @return this builder | 此构建器
         */
        public Builder add(LocaleResolver resolver) {
            resolvers.add(resolver);
            return this;
        }

        /**
         * Adds a resolver at the beginning
         * 在开头添加解析器
         *
         * @param resolver the resolver | 解析器
         * @return this builder | 此构建器
         */
        public Builder addFirst(LocaleResolver resolver) {
            resolvers.addFirst(resolver);
            return this;
        }

        /**
         * Sets the default locale
         * 设置默认Locale
         *
         * @param defaultLocale the default locale | 默认地区
         * @return this builder | 此构建器
         */
        public Builder defaultLocale(Locale defaultLocale) {
            this.defaultLocale = defaultLocale;
            return this;
        }

        /**
         * Builds the composite resolver
         * 构建复合解析器
         *
         * @return composite resolver | 复合解析器
         */
        public CompositeLocaleResolver build() {
            return new CompositeLocaleResolver(resolvers, defaultLocale);
        }
    }
}
