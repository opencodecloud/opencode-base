package cloud.opencode.base.i18n;

import cloud.opencode.base.i18n.exception.OpenNoSuchMessageException;
import cloud.opencode.base.i18n.formatter.DefaultMessageFormatter;
import cloud.opencode.base.i18n.resolver.ThreadLocalLocaleResolver;
import cloud.opencode.base.i18n.spi.LocaleResolver;
import cloud.opencode.base.i18n.spi.MessageFormatter;
import cloud.opencode.base.i18n.spi.MessageProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenI18n 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("OpenI18n 测试")
class OpenI18nTest {

    private MockMessageProvider mockProvider;

    @BeforeEach
    void setUp() {
        mockProvider = new MockMessageProvider();
        OpenI18n.setMessageProvider(mockProvider);
        OpenI18n.setLocaleResolver(new ThreadLocalLocaleResolver());
        OpenI18n.setMessageFormatter(new DefaultMessageFormatter());
        OpenI18n.setDefaultLocale(Locale.ENGLISH);
        OpenI18n.setThrowOnMissingMessage(false);
        OpenI18n.resetLocale();
    }

    @AfterEach
    void tearDown() {
        OpenI18n.resetLocale();
    }

    @Nested
    @DisplayName("get方法测试")
    class GetTests {

        @Test
        @DisplayName("获取消息")
        void testGet() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "Test Message");

            String result = OpenI18n.get("test.key");

            assertThat(result).isEqualTo("Test Message");
        }

        @Test
        @DisplayName("获取带参数的消息")
        void testGetWithArgs() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "Hello, {0}!");

            String result = OpenI18n.get("test.key", "World");

            assertThat(result).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("使用指定Locale获取消息")
        void testGetWithLocale() {
            mockProvider.addMessage("test.key", Locale.CHINESE, "Chinese Message");

            String result = OpenI18n.get("test.key", Locale.CHINESE);

            assertThat(result).isEqualTo("Chinese Message");
        }

        @Test
        @DisplayName("消息不存在返回key")
        void testGetNotFound() {
            String result = OpenI18n.get("nonexistent.key");

            assertThat(result).isEqualTo("nonexistent.key");
        }

        @Test
        @DisplayName("消息不存在抛出异常")
        void testGetNotFoundThrows() {
            OpenI18n.setThrowOnMissingMessage(true);

            assertThatThrownBy(() -> OpenI18n.get("nonexistent.key"))
                    .isInstanceOf(OpenNoSuchMessageException.class);
        }

        @Test
        @DisplayName("回退到默认Locale")
        void testGetFallbackToDefault() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "English Message");

            String result = OpenI18n.get("test.key", Locale.FRENCH);

            assertThat(result).isEqualTo("English Message");
        }
    }

    @Nested
    @DisplayName("getOrDefault方法测试")
    class GetOrDefaultTests {

        @Test
        @DisplayName("消息存在返回消息")
        void testGetOrDefaultExists() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "Test Message");

            String result = OpenI18n.getOrDefault("test.key", "Default");

            assertThat(result).isEqualTo("Test Message");
        }

        @Test
        @DisplayName("消息不存在返回默认值")
        void testGetOrDefaultNotExists() {
            String result = OpenI18n.getOrDefault("nonexistent", "Default Value");

            assertThat(result).isEqualTo("Default Value");
        }

        @Test
        @DisplayName("使用指定Locale获取或默认值")
        void testGetOrDefaultWithLocale() {
            mockProvider.addMessage("test.key", Locale.CHINESE, "Chinese");

            String result = OpenI18n.getOrDefault("test.key", Locale.CHINESE, "Default");

            assertThat(result).isEqualTo("Chinese");
        }

        @Test
        @DisplayName("带参数获取或默认值")
        void testGetOrDefaultWithArgs() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "Hello {0}");

            String result = OpenI18n.getOrDefault("test.key", "Default", "World");

            assertThat(result).isEqualTo("Hello World");
        }
    }

    @Nested
    @DisplayName("get方法测试（命名参数）")
    class GetWithMapTests {

        @Test
        @DisplayName("使用Map参数获取消息")
        void testGetWithMap() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "Hello, {0}!");

            String result = OpenI18n.get("test.key", Map.of("name", "Alice"));

            assertThat(result).isEqualTo("Hello, Alice!");
        }

        @Test
        @DisplayName("使用指定Locale和Map获取消息")
        void testGetWithLocaleAndMap() {
            mockProvider.addMessage("test.key", Locale.FRENCH, "Bonjour, {0}!");

            String result = OpenI18n.get("test.key", Locale.FRENCH, Map.of("name", "Bob"));

            assertThat(result).isEqualTo("Bonjour, Bob!");
        }

        @Test
        @DisplayName("消息不存在抛出异常")
        void testGetWithMapThrows() {
            OpenI18n.setThrowOnMissingMessage(true);

            assertThatThrownBy(() -> OpenI18n.get("nonexistent", Map.of("name", "test")))
                    .isInstanceOf(OpenNoSuchMessageException.class);
        }
    }

    @Nested
    @DisplayName("contains方法测试")
    class ContainsTests {

        @Test
        @DisplayName("消息存在返回true")
        void testContainsTrue() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "Test");
            OpenI18n.setCurrentLocale(Locale.ENGLISH);

            assertThat(OpenI18n.contains("test.key")).isTrue();
        }

        @Test
        @DisplayName("消息不存在返回false")
        void testContainsFalse() {
            assertThat(OpenI18n.contains("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("使用指定Locale检查")
        void testContainsWithLocale() {
            mockProvider.addMessage("test.key", Locale.CHINESE, "Chinese");

            assertThat(OpenI18n.contains("test.key", Locale.CHINESE)).isTrue();
            assertThat(OpenI18n.contains("test.key", Locale.FRENCH)).isFalse();
        }
    }

    @Nested
    @DisplayName("Locale管理测试")
    class LocaleManagementTests {

        @Test
        @DisplayName("获取当前Locale")
        void testGetCurrentLocale() {
            Locale locale = OpenI18n.getCurrentLocale();

            assertThat(locale).isNotNull();
        }

        @Test
        @DisplayName("设置当前Locale")
        void testSetCurrentLocale() {
            OpenI18n.setCurrentLocale(Locale.JAPANESE);

            assertThat(OpenI18n.getCurrentLocale()).isEqualTo(Locale.JAPANESE);
        }

        @Test
        @DisplayName("重置当前Locale")
        void testResetLocale() {
            OpenI18n.setCurrentLocale(Locale.KOREAN);

            OpenI18n.resetLocale();

            assertThat(OpenI18n.getCurrentLocale()).isEqualTo(Locale.getDefault());
        }
    }

    @Nested
    @DisplayName("withLocale方法测试")
    class WithLocaleTests {

        @Test
        @DisplayName("在指定Locale下执行Runnable")
        void testWithLocaleRunnable() {
            mockProvider.addMessage("test.key", Locale.FRENCH, "French");
            AtomicReference<String> result = new AtomicReference<>();

            OpenI18n.withLocale(Locale.FRENCH, () -> {
                result.set(OpenI18n.get("test.key"));
            });

            assertThat(result.get()).isEqualTo("French");
            assertThat(OpenI18n.getCurrentLocale()).isNotEqualTo(Locale.FRENCH);
        }

        @Test
        @DisplayName("在指定Locale下执行Supplier")
        void testWithLocaleSupplier() {
            mockProvider.addMessage("test.key", Locale.GERMAN, "German");

            String result = OpenI18n.withLocale(Locale.GERMAN, () -> OpenI18n.get("test.key"));

            assertThat(result).isEqualTo("German");
        }

        @Test
        @DisplayName("异常后恢复Locale")
        void testWithLocaleRestoresOnException() {
            Locale original = OpenI18n.getCurrentLocale();

            assertThatThrownBy(() ->
                    OpenI18n.withLocale(Locale.ITALIAN, () -> {
                        throw new RuntimeException("test");
                    })
            ).isInstanceOf(RuntimeException.class);

            assertThat(OpenI18n.getCurrentLocale()).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("全局配置测试")
    class GlobalConfigTests {

        @Test
        @DisplayName("设置LocaleResolver")
        void testSetLocaleResolver() {
            LocaleResolver resolver = () -> Locale.ITALIAN;

            OpenI18n.setLocaleResolver(resolver);

            assertThat(OpenI18n.getCurrentLocale()).isEqualTo(Locale.ITALIAN);
        }

        @Test
        @DisplayName("设置MessageProvider")
        void testSetMessageProvider() {
            MessageProvider provider = (key, locale) -> Optional.of("Custom: " + key);

            OpenI18n.setMessageProvider(provider);

            assertThat(OpenI18n.get("any.key")).isEqualTo("Custom: any.key");
        }

        @Test
        @DisplayName("设置MessageFormatter")
        void testSetMessageFormatter() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "Template {0}");
            MessageFormatter formatter = new MessageFormatter() {
                @Override
                public String format(String template, Locale locale, Object... args) {
                    return "Custom: " + template;
                }

                @Override
                public String format(String template, Locale locale, Map<String, Object> params) {
                    return "Custom: " + template;
                }
            };

            OpenI18n.setMessageFormatter(formatter);

            assertThat(OpenI18n.get("test.key", "arg")).isEqualTo("Custom: Template {0}");
        }

        @Test
        @DisplayName("设置默认Locale")
        void testSetDefaultLocale() {
            OpenI18n.setDefaultLocale(Locale.CHINESE);
            mockProvider.addMessage("test.key", Locale.CHINESE, "Chinese Default");

            String result = OpenI18n.get("test.key", Locale.FRENCH);

            assertThat(result).isEqualTo("Chinese Default");
        }
    }

    @Nested
    @DisplayName("getMessageSource方法测试")
    class GetMessageSourceTests {

        @Test
        @DisplayName("获取MessageSource")
        void testGetMessageSource() {
            MessageSource source = OpenI18n.getMessageSource();

            assertThat(source).isNotNull();
        }

        @Test
        @DisplayName("MessageSource可以获取消息")
        void testMessageSourceGetMessage() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "Test");
            MessageSource source = OpenI18n.getMessageSource();

            String result = source.getMessage("test.key", Locale.ENGLISH);

            assertThat(result).isEqualTo("Test");
        }
    }

    @Nested
    @DisplayName("refresh方法测试")
    class RefreshTests {

        @Test
        @DisplayName("刷新缓存")
        void testRefresh() {
            assertThatCode(() -> OpenI18n.refresh()).doesNotThrowAnyException();
        }
    }

    /**
     * Mock MessageProvider for testing
     */
    private static class MockMessageProvider implements MessageProvider {
        private final java.util.Map<String, String> messages = new java.util.HashMap<>();

        void addMessage(String key, Locale locale, String value) {
            messages.put(key + "_" + locale, value);
        }

        @Override
        public Optional<String> getMessageTemplate(String key, Locale locale) {
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
    }
}
