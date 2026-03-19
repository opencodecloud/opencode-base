package cloud.opencode.base.i18n.provider;

import cloud.opencode.base.i18n.spi.MessageProvider;

import java.util.*;

/**
 * Chain message provider that combines multiple providers
 * 组合多个提供者的链式消息提供者
 *
 * <p>Searches through a chain of providers in order until a message is found.</p>
 * <p>按顺序在提供者链中搜索，直到找到消息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple provider chaining - 多提供者链接</li>
 *   <li>Order-based search - 基于顺序的搜索</li>
 *   <li>Builder pattern support - 构建器模式支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ChainMessageProvider chain = ChainMessageProvider.builder()
 *     .add(new ResourceBundleProvider("i18n/errors"))
 *     .add(new ResourceBundleProvider("i18n/messages"))
 *     .build();
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
public class ChainMessageProvider implements MessageProvider {

    private final List<MessageProvider> providers;

    /**
     * Creates a chain provider with a list of providers
     * 使用提供者列表创建链式提供者
     *
     * @param providers the providers | 提供者列表
     */
    public ChainMessageProvider(List<MessageProvider> providers) {
        this.providers = new ArrayList<>(providers);
    }

    /**
     * Creates a chain provider with providers array
     * 使用提供者数组创建链式提供者
     *
     * @param providers the providers | 提供者数组
     */
    public ChainMessageProvider(MessageProvider... providers) {
        this.providers = new ArrayList<>(Arrays.asList(providers));
    }

    @Override
    public Optional<String> getMessageTemplate(String key, Locale locale) {
        for (MessageProvider provider : providers) {
            Optional<String> message = provider.getMessageTemplate(key, locale);
            if (message.isPresent()) {
                return message;
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean containsMessage(String key, Locale locale) {
        for (MessageProvider provider : providers) {
            if (provider.containsMessage(key, locale)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getKeys(Locale locale) {
        Set<String> allKeys = new HashSet<>();
        for (MessageProvider provider : providers) {
            allKeys.addAll(provider.getKeys(locale));
        }
        return allKeys;
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        Set<Locale> allLocales = new HashSet<>();
        for (MessageProvider provider : providers) {
            allLocales.addAll(provider.getSupportedLocales());
        }
        return allLocales;
    }

    @Override
    public void refresh() {
        for (MessageProvider provider : providers) {
            provider.refresh();
        }
    }

    /**
     * Adds a provider to the chain
     * 向链中添加提供者
     *
     * @param provider the provider | 提供者
     * @return this chain | 此链
     */
    public ChainMessageProvider addProvider(MessageProvider provider) {
        providers.add(provider);
        return this;
    }

    /**
     * Gets the number of providers in the chain
     * 获取链中的提供者数量
     *
     * @return provider count | 提供者数量
     */
    public int size() {
        return providers.size();
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
     * Builder for ChainMessageProvider
     * ChainMessageProvider的构建器
     */
    public static class Builder {
        private final List<MessageProvider> providers = new ArrayList<>();

        /**
         * Adds a provider to the chain
         * 向链中添加提供者
         *
         * @param provider the provider | 提供者
         * @return this builder | 此构建器
         */
        public Builder add(MessageProvider provider) {
            providers.add(provider);
            return this;
        }

        /**
         * Adds a provider at the beginning
         * 在开头添加提供者
         *
         * @param provider the provider | 提供者
         * @return this builder | 此构建器
         */
        public Builder addFirst(MessageProvider provider) {
            providers.addFirst(provider);
            return this;
        }

        /**
         * Builds the chain provider
         * 构建链式提供者
         *
         * @return chain provider | 链式提供者
         */
        public ChainMessageProvider build() {
            return new ChainMessageProvider(providers);
        }
    }
}
