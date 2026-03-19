package cloud.opencode.base.i18n.support;

import cloud.opencode.base.i18n.MessageSource;
import cloud.opencode.base.i18n.exception.OpenNoSuchMessageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * MessageSourceAccessor 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("MessageSourceAccessor 测试")
class MessageSourceAccessorTest {

    private MockMessageSource mockSource;

    @BeforeEach
    void setUp() {
        mockSource = new MockMessageSource();
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用MessageSource创建")
        void testConstructorWithSource() {
            MessageSourceAccessor accessor = new MessageSourceAccessor(mockSource);

            assertThat(accessor.getMessageSource()).isEqualTo(mockSource);
            assertThat(accessor.getDefaultLocale()).isEqualTo(Locale.getDefault());
        }

        @Test
        @DisplayName("使用MessageSource和默认Locale创建")
        void testConstructorWithLocale() {
            MessageSourceAccessor accessor = new MessageSourceAccessor(mockSource, Locale.FRENCH);

            assertThat(accessor.getDefaultLocale()).isEqualTo(Locale.FRENCH);
        }

        @Test
        @DisplayName("null默认Locale使用系统默认")
        void testConstructorWithNullLocale() {
            MessageSourceAccessor accessor = new MessageSourceAccessor(mockSource, null);

            assertThat(accessor.getDefaultLocale()).isEqualTo(Locale.getDefault());
        }
    }

    @Nested
    @DisplayName("getMessage方法测试")
    class GetMessageTests {

        @Test
        @DisplayName("使用默认Locale获取消息")
        void testGetMessageWithDefaultLocale() {
            mockSource.addMessage("test.key", Locale.getDefault(), "Default Message");
            MessageSourceAccessor accessor = new MessageSourceAccessor(mockSource);

            String result = accessor.getMessage("test.key");

            assertThat(result).isEqualTo("Default Message");
        }

        @Test
        @DisplayName("使用参数获取消息")
        void testGetMessageWithArgs() {
            mockSource.addMessage("test.key", Locale.getDefault(), "Hello World");
            MessageSourceAccessor accessor = new MessageSourceAccessor(mockSource);

            String result = accessor.getMessage("test.key", "arg1", "arg2");

            assertThat(result).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("使用指定Locale获取消息")
        void testGetMessageWithLocale() {
            mockSource.addMessage("test.key", Locale.FRENCH, "French Message");
            MessageSourceAccessor accessor = new MessageSourceAccessor(mockSource);

            String result = accessor.getMessage("test.key", Locale.FRENCH);

            assertThat(result).isEqualTo("French Message");
        }

        @Test
        @DisplayName("使用指定Locale和参数获取消息")
        void testGetMessageWithLocaleAndArgs() {
            mockSource.addMessage("test.key", Locale.GERMAN, "German Message");
            MessageSourceAccessor accessor = new MessageSourceAccessor(mockSource);

            String result = accessor.getMessage("test.key", Locale.GERMAN, "arg");

            assertThat(result).isEqualTo("German Message");
        }
    }

    @Nested
    @DisplayName("getMessageOrDefault方法测试")
    class GetMessageOrDefaultTests {

        @Test
        @DisplayName("消息存在返回消息")
        void testGetMessageOrDefaultExists() {
            mockSource.addMessage("test.key", Locale.getDefault(), "Test Message");
            MessageSourceAccessor accessor = new MessageSourceAccessor(mockSource);

            String result = accessor.getMessageOrDefault("test.key", "Default");

            assertThat(result).isEqualTo("Test Message");
        }

        @Test
        @DisplayName("消息不存在返回默认值")
        void testGetMessageOrDefaultNotExists() {
            MessageSourceAccessor accessor = new MessageSourceAccessor(mockSource);

            String result = accessor.getMessageOrDefault("nonexistent", "Default Value");

            assertThat(result).isEqualTo("Default Value");
        }

        @Test
        @DisplayName("使用指定Locale获取或返回默认值")
        void testGetMessageOrDefaultWithLocale() {
            mockSource.addMessage("test.key", Locale.FRENCH, "French Message");
            MessageSourceAccessor accessor = new MessageSourceAccessor(mockSource);

            String result = accessor.getMessageOrDefault("test.key", "Default", Locale.FRENCH);

            assertThat(result).isEqualTo("French Message");
        }

        @Test
        @DisplayName("使用参数获取或返回默认值")
        void testGetMessageOrDefaultWithArgs() {
            mockSource.addMessage("test.key", Locale.getDefault(), "Message");
            MessageSourceAccessor accessor = new MessageSourceAccessor(mockSource);

            String result = accessor.getMessageOrDefault("test.key", "Default", "arg1");

            assertThat(result).isEqualTo("Message");
        }

        @Test
        @DisplayName("异常时返回默认值")
        void testGetMessageOrDefaultOnException() {
            mockSource.setThrowOnMissing(true);
            MessageSourceAccessor accessor = new MessageSourceAccessor(mockSource);

            String result = accessor.getMessageOrDefault("nonexistent", "Default", Locale.ENGLISH, "arg");

            assertThat(result).isEqualTo("Default");
        }
    }

    @Nested
    @DisplayName("getMessageOptional方法测试")
    class GetMessageOptionalTests {

        @Test
        @DisplayName("消息存在返回Optional")
        void testGetMessageOptionalExists() {
            mockSource.addMessage("test.key", Locale.getDefault(), "Test Message");
            MessageSourceAccessor accessor = new MessageSourceAccessor(mockSource);

            Optional<String> result = accessor.getMessageOptional("test.key");

            assertThat(result).hasValue("Test Message");
        }

        @Test
        @DisplayName("消息不存在返回空Optional")
        void testGetMessageOptionalNotExists() {
            MessageSourceAccessor accessor = new MessageSourceAccessor(mockSource);

            Optional<String> result = accessor.getMessageOptional("nonexistent");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("使用指定Locale获取Optional")
        void testGetMessageOptionalWithLocale() {
            mockSource.addMessage("test.key", Locale.FRENCH, "French Message");
            MessageSourceAccessor accessor = new MessageSourceAccessor(mockSource);

            Optional<String> result = accessor.getMessageOptional("test.key", Locale.FRENCH);

            assertThat(result).hasValue("French Message");
        }
    }

    @Nested
    @DisplayName("访问器方法测试")
    class AccessorMethodTests {

        @Test
        @DisplayName("获取MessageSource")
        void testGetMessageSource() {
            MessageSourceAccessor accessor = new MessageSourceAccessor(mockSource);

            assertThat(accessor.getMessageSource()).isEqualTo(mockSource);
        }

        @Test
        @DisplayName("获取默认Locale")
        void testGetDefaultLocale() {
            MessageSourceAccessor accessor = new MessageSourceAccessor(mockSource, Locale.GERMAN);

            assertThat(accessor.getDefaultLocale()).isEqualTo(Locale.GERMAN);
        }
    }

    /**
     * Mock MessageSource for testing
     */
    private static class MockMessageSource implements MessageSource {
        private final java.util.Map<String, String> messages = new java.util.HashMap<>();
        private boolean throwOnMissing = false;

        void addMessage(String key, Locale locale, String value) {
            messages.put(key + "_" + locale, value);
        }

        void setThrowOnMissing(boolean throwOnMissing) {
            this.throwOnMissing = throwOnMissing;
        }

        @Override
        public String getMessage(String key, Locale locale, Object... args) {
            String msg = messages.get(key + "_" + locale);
            if (msg == null && throwOnMissing) {
                throw new OpenNoSuchMessageException(key, locale);
            }
            return msg != null ? msg : key;
        }

        @Override
        public Optional<String> getMessageOptional(String key, Locale locale, Object... args) {
            return Optional.ofNullable(messages.get(key + "_" + locale));
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
