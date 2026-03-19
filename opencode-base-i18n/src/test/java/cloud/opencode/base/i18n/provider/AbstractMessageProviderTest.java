package cloud.opencode.base.i18n.provider;

import cloud.opencode.base.i18n.spi.MessageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * AbstractMessageProvider 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("AbstractMessageProvider 测试")
class AbstractMessageProviderTest {

    @Nested
    @DisplayName("getMessageTemplate方法测试 - Locale回退")
    class LocaleFallbackTests {

        @Test
        @DisplayName("精确Locale匹配")
        void testExactLocaleMatch() {
            TestMessageProvider provider = new TestMessageProvider();
            provider.addMessage("test.key", Locale.CHINESE, "Chinese");

            Optional<String> result = provider.getMessageTemplate("test.key", Locale.CHINESE);

            assertThat(result).hasValue("Chinese");
        }

        @Test
        @DisplayName("去除variant回退")
        void testFallbackWithoutVariant() {
            TestMessageProvider provider = new TestMessageProvider();
            Locale zhTW = Locale.of("zh", "TW");
            provider.addMessage("test.key", zhTW, "Traditional Chinese");

            Locale zhTWHK = Locale.of("zh", "TW", "HK");
            Optional<String> result = provider.getMessageTemplate("test.key", zhTWHK);

            assertThat(result).hasValue("Traditional Chinese");
        }

        @Test
        @DisplayName("只用语言回退")
        void testFallbackToLanguageOnly() {
            TestMessageProvider provider = new TestMessageProvider();
            provider.addMessage("test.key", Locale.CHINESE, "Chinese");

            Locale zhCN = Locale.of("zh", "CN");
            Optional<String> result = provider.getMessageTemplate("test.key", zhCN);

            assertThat(result).hasValue("Chinese");
        }

        @Test
        @DisplayName("回退到默认Locale")
        void testFallbackToDefaultLocale() {
            TestMessageProvider provider = new TestMessageProvider();
            provider.setDefaultLocale(Locale.ENGLISH);
            provider.addMessage("test.key", Locale.ENGLISH, "English");

            Optional<String> result = provider.getMessageTemplate("test.key", Locale.FRENCH);

            assertThat(result).hasValue("English");
        }

        @Test
        @DisplayName("回退到ROOT Locale")
        void testFallbackToRoot() {
            TestMessageProvider provider = new TestMessageProvider();
            provider.setDefaultLocale(Locale.GERMAN); // No German message
            provider.addMessage("test.key", Locale.ROOT, "Root");

            Optional<String> result = provider.getMessageTemplate("test.key", Locale.FRENCH);

            assertThat(result).hasValue("Root");
        }

        @Test
        @DisplayName("所有回退都失败返回空")
        void testAllFallbacksFail() {
            TestMessageProvider provider = new TestMessageProvider();

            Optional<String> result = provider.getMessageTemplate("nonexistent", Locale.ENGLISH);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("使用key作为默认值")
        void testUseKeyAsDefault() {
            TestMessageProvider provider = new TestMessageProvider();
            provider.setUseCodeAsDefaultMessage(true);

            Optional<String> result = provider.getMessageTemplate("fallback.key", Locale.ENGLISH);

            assertThat(result).hasValue("fallback.key");
        }
    }

    @Nested
    @DisplayName("setDefaultLocale方法测试")
    class SetDefaultLocaleTests {

        @Test
        @DisplayName("设置默认Locale")
        void testSetDefaultLocale() {
            TestMessageProvider provider = new TestMessageProvider();

            provider.setDefaultLocale(Locale.FRENCH);

            assertThat(provider.getDefaultLocale()).isEqualTo(Locale.FRENCH);
        }
    }

    @Nested
    @DisplayName("getDefaultLocale方法测试")
    class GetDefaultLocaleTests {

        @Test
        @DisplayName("获取默认Locale")
        void testGetDefaultLocale() {
            TestMessageProvider provider = new TestMessageProvider();

            Locale defaultLocale = provider.getDefaultLocale();

            assertThat(defaultLocale).isEqualTo(Locale.getDefault());
        }
    }

    @Nested
    @DisplayName("createCacheKey方法测试")
    class CreateCacheKeyTests {

        @Test
        @DisplayName("创建缓存键")
        void testCreateCacheKey() {
            TestMessageProvider provider = new TestMessageProvider();

            String cacheKey = provider.testCreateCacheKey("message.key", Locale.CHINESE);

            assertThat(cacheKey).isEqualTo("message.key_zh");
        }

        @Test
        @DisplayName("创建带地区的缓存键")
        void testCreateCacheKeyWithCountry() {
            TestMessageProvider provider = new TestMessageProvider();

            String cacheKey = provider.testCreateCacheKey("message.key", Locale.US);

            assertThat(cacheKey).isEqualTo("message.key_en_US");
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现MessageProvider接口")
        void testImplementsInterface() {
            TestMessageProvider provider = new TestMessageProvider();

            assertThat(provider).isInstanceOf(MessageProvider.class);
        }
    }

    /**
     * Test implementation of AbstractMessageProvider
     */
    private static class TestMessageProvider extends AbstractMessageProvider {
        private final Map<String, String> messages = new HashMap<>();

        void addMessage(String key, Locale locale, String value) {
            messages.put(key + "_" + locale, value);
        }

        @Override
        protected Optional<String> doGetMessage(String key, Locale locale) {
            return Optional.ofNullable(messages.get(key + "_" + locale));
        }

        @Override
        public boolean containsMessage(String key, Locale locale) {
            return messages.containsKey(key + "_" + locale);
        }

        @Override
        public Set<String> getKeys(Locale locale) {
            return Set.of();
        }

        @Override
        public Set<Locale> getSupportedLocales() {
            return Set.of();
        }

        String testCreateCacheKey(String key, Locale locale) {
            return createCacheKey(key, locale);
        }
    }
}
