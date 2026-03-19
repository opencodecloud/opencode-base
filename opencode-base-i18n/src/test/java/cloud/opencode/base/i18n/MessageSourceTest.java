package cloud.opencode.base.i18n;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * MessageSource 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("MessageSource 接口测试")
class MessageSourceTest {

    @Nested
    @DisplayName("接口方法测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("getMessage方法存在")
        void testGetMessageExists() throws NoSuchMethodException {
            var method = MessageSource.class.getMethod("getMessage", String.class, Locale.class, Object[].class);

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("getMessageOptional方法存在")
        void testGetMessageOptionalExists() throws NoSuchMethodException {
            var method = MessageSource.class.getMethod("getMessageOptional", String.class, Locale.class, Object[].class);

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Optional.class);
        }

        @Test
        @DisplayName("getMessageTemplate方法存在")
        void testGetMessageTemplateExists() throws NoSuchMethodException {
            var method = MessageSource.class.getMethod("getMessageTemplate", String.class, Locale.class);

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Optional.class);
        }

        @Test
        @DisplayName("containsMessage方法存在")
        void testContainsMessageExists() throws NoSuchMethodException {
            var method = MessageSource.class.getMethod("containsMessage", String.class, Locale.class);

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("getKeys方法存在")
        void testGetKeysExists() throws NoSuchMethodException {
            var method = MessageSource.class.getMethod("getKeys", Locale.class);

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Set.class);
        }

        @Test
        @DisplayName("getSupportedLocales方法存在")
        void testGetSupportedLocalesExists() throws NoSuchMethodException {
            var method = MessageSource.class.getMethod("getSupportedLocales");

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Set.class);
        }
    }

    @Nested
    @DisplayName("实现测试")
    class ImplementationTests {

        private MessageSource createMockSource() {
            return new MessageSource() {
                private final java.util.Map<String, String> messages = new java.util.HashMap<>();

                {
                    messages.put("greeting_en", "Hello");
                    messages.put("greeting_zh", "你好");
                }

                @Override
                public String getMessage(String key, Locale locale, Object... args) {
                    return messages.getOrDefault(key + "_" + locale.getLanguage(), key);
                }

                @Override
                public Optional<String> getMessageOptional(String key, Locale locale, Object... args) {
                    return Optional.ofNullable(messages.get(key + "_" + locale.getLanguage()));
                }

                @Override
                public Optional<String> getMessageTemplate(String key, Locale locale) {
                    return Optional.ofNullable(messages.get(key + "_" + locale.getLanguage()));
                }

                @Override
                public boolean containsMessage(String key, Locale locale) {
                    return messages.containsKey(key + "_" + locale.getLanguage());
                }

                @Override
                public Set<String> getKeys(Locale locale) {
                    return Set.of("greeting");
                }

                @Override
                public Set<Locale> getSupportedLocales() {
                    return Set.of(Locale.ENGLISH, Locale.CHINESE);
                }
            };
        }

        @Test
        @DisplayName("getMessage返回消息")
        void testGetMessage() {
            MessageSource source = createMockSource();

            String result = source.getMessage("greeting", Locale.ENGLISH);

            assertThat(result).isEqualTo("Hello");
        }

        @Test
        @DisplayName("getMessageOptional消息存在")
        void testGetMessageOptionalExists() {
            MessageSource source = createMockSource();

            Optional<String> result = source.getMessageOptional("greeting", Locale.ENGLISH);

            assertThat(result).hasValue("Hello");
        }

        @Test
        @DisplayName("getMessageOptional消息不存在")
        void testGetMessageOptionalNotExists() {
            MessageSource source = createMockSource();

            Optional<String> result = source.getMessageOptional("nonexistent", Locale.ENGLISH);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("getMessageTemplate返回模板")
        void testGetMessageTemplate() {
            MessageSource source = createMockSource();

            Optional<String> result = source.getMessageTemplate("greeting", Locale.CHINESE);

            assertThat(result).hasValue("你好");
        }

        @Test
        @DisplayName("containsMessage消息存在")
        void testContainsMessageTrue() {
            MessageSource source = createMockSource();

            assertThat(source.containsMessage("greeting", Locale.ENGLISH)).isTrue();
        }

        @Test
        @DisplayName("containsMessage消息不存在")
        void testContainsMessageFalse() {
            MessageSource source = createMockSource();

            assertThat(source.containsMessage("nonexistent", Locale.ENGLISH)).isFalse();
        }

        @Test
        @DisplayName("getKeys返回键集合")
        void testGetKeys() {
            MessageSource source = createMockSource();

            Set<String> keys = source.getKeys(Locale.ENGLISH);

            assertThat(keys).contains("greeting");
        }

        @Test
        @DisplayName("getSupportedLocales返回支持的Locale")
        void testGetSupportedLocales() {
            MessageSource source = createMockSource();

            Set<Locale> locales = source.getSupportedLocales();

            assertThat(locales).containsExactlyInAnyOrder(Locale.ENGLISH, Locale.CHINESE);
        }
    }
}
