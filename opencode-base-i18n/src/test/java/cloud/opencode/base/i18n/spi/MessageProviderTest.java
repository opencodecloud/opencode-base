package cloud.opencode.base.i18n.spi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * MessageProvider 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("MessageProvider 接口测试")
class MessageProviderTest {

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("是函数式接口")
        void testIsFunctionalInterface() {
            assertThat(MessageProvider.class.isAnnotationPresent(FunctionalInterface.class)).isTrue();
        }

        @Test
        @DisplayName("可以用lambda实现")
        void testLambdaImplementation() {
            MessageProvider provider = (key, locale) -> Optional.of("Value for " + key);

            assertThat(provider.getMessageTemplate("test", Locale.ENGLISH))
                    .hasValue("Value for test");
        }
    }

    @Nested
    @DisplayName("getMessageTemplate方法测试")
    class GetMessageTemplateTests {

        @Test
        @DisplayName("getMessageTemplate是抽象方法")
        void testGetMessageTemplateIsAbstract() throws NoSuchMethodException {
            var method = MessageProvider.class.getMethod("getMessageTemplate", String.class, Locale.class);

            assertThat(method.isDefault()).isFalse();
        }

        @Test
        @DisplayName("返回Optional")
        void testReturnsOptional() {
            MessageProvider provider = (key, locale) -> Optional.of("test");

            assertThat(provider.getMessageTemplate("key", Locale.ENGLISH)).isInstanceOf(Optional.class);
        }

        @Test
        @DisplayName("可以返回空Optional")
        void testReturnsEmptyOptional() {
            MessageProvider provider = (key, locale) -> Optional.empty();

            assertThat(provider.getMessageTemplate("key", Locale.ENGLISH)).isEmpty();
        }
    }

    @Nested
    @DisplayName("containsMessage默认方法测试")
    class ContainsMessageTests {

        @Test
        @DisplayName("containsMessage是默认方法")
        void testContainsMessageIsDefault() throws NoSuchMethodException {
            var method = MessageProvider.class.getMethod("containsMessage", String.class, Locale.class);

            assertThat(method.isDefault()).isTrue();
        }

        @Test
        @DisplayName("消息存在返回true")
        void testContainsMessageTrue() {
            MessageProvider provider = (key, locale) ->
                    "exists".equals(key) ? Optional.of("value") : Optional.empty();

            assertThat(provider.containsMessage("exists", Locale.ENGLISH)).isTrue();
        }

        @Test
        @DisplayName("消息不存在返回false")
        void testContainsMessageFalse() {
            MessageProvider provider = (key, locale) -> Optional.empty();

            assertThat(provider.containsMessage("nonexistent", Locale.ENGLISH)).isFalse();
        }
    }

    @Nested
    @DisplayName("getKeys默认方法测试")
    class GetKeysTests {

        @Test
        @DisplayName("getKeys是默认方法")
        void testGetKeysIsDefault() throws NoSuchMethodException {
            var method = MessageProvider.class.getMethod("getKeys", Locale.class);

            assertThat(method.isDefault()).isTrue();
        }

        @Test
        @DisplayName("默认返回空集合")
        void testGetKeysDefaultEmpty() {
            MessageProvider provider = (key, locale) -> Optional.empty();

            assertThat(provider.getKeys(Locale.ENGLISH)).isEmpty();
        }

        @Test
        @DisplayName("可以覆盖getKeys方法")
        void testGetKeysOverride() {
            MessageProvider provider = new MessageProvider() {
                @Override
                public Optional<String> getMessageTemplate(String key, Locale locale) {
                    return Optional.empty();
                }

                @Override
                public Set<String> getKeys(Locale locale) {
                    return Set.of("key1", "key2");
                }
            };

            assertThat(provider.getKeys(Locale.ENGLISH)).containsExactlyInAnyOrder("key1", "key2");
        }
    }

    @Nested
    @DisplayName("getSupportedLocales默认方法测试")
    class GetSupportedLocalesTests {

        @Test
        @DisplayName("getSupportedLocales是默认方法")
        void testGetSupportedLocalesIsDefault() throws NoSuchMethodException {
            var method = MessageProvider.class.getMethod("getSupportedLocales");

            assertThat(method.isDefault()).isTrue();
        }

        @Test
        @DisplayName("默认返回空集合")
        void testGetSupportedLocalesDefaultEmpty() {
            MessageProvider provider = (key, locale) -> Optional.empty();

            assertThat(provider.getSupportedLocales()).isEmpty();
        }

        @Test
        @DisplayName("可以覆盖getSupportedLocales方法")
        void testGetSupportedLocalesOverride() {
            MessageProvider provider = new MessageProvider() {
                @Override
                public Optional<String> getMessageTemplate(String key, Locale locale) {
                    return Optional.empty();
                }

                @Override
                public Set<Locale> getSupportedLocales() {
                    return Set.of(Locale.ENGLISH, Locale.CHINESE);
                }
            };

            assertThat(provider.getSupportedLocales())
                    .containsExactlyInAnyOrder(Locale.ENGLISH, Locale.CHINESE);
        }
    }

    @Nested
    @DisplayName("refresh默认方法测试")
    class RefreshTests {

        @Test
        @DisplayName("refresh是默认方法")
        void testRefreshIsDefault() throws NoSuchMethodException {
            var method = MessageProvider.class.getMethod("refresh");

            assertThat(method.isDefault()).isTrue();
        }

        @Test
        @DisplayName("默认实现不做任何操作")
        void testRefreshNoOp() {
            MessageProvider provider = (key, locale) -> Optional.empty();

            assertThatCode(() -> provider.refresh()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("可以覆盖refresh方法")
        void testRefreshOverride() {
            var refreshCalled = new boolean[]{false};
            MessageProvider provider = new MessageProvider() {
                @Override
                public Optional<String> getMessageTemplate(String key, Locale locale) {
                    return Optional.empty();
                }

                @Override
                public void refresh() {
                    refreshCalled[0] = true;
                }
            };

            provider.refresh();

            assertThat(refreshCalled[0]).isTrue();
        }
    }
}
