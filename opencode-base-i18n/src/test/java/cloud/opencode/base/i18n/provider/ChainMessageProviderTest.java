package cloud.opencode.base.i18n.provider;

import cloud.opencode.base.i18n.spi.MessageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * ChainMessageProvider 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("ChainMessageProvider 测试")
class ChainMessageProviderTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用列表创建")
        void testConstructorWithList() {
            List<MessageProvider> providers = List.of(
                    createMockProvider("key1", "Value1"),
                    createMockProvider("key2", "Value2")
            );
            ChainMessageProvider chain = new ChainMessageProvider(providers);

            assertThat(chain.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("使用可变参数创建")
        void testConstructorWithVarargs() {
            ChainMessageProvider chain = new ChainMessageProvider(
                    createMockProvider("key1", "Value1"),
                    createMockProvider("key2", "Value2")
            );

            assertThat(chain.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("getMessageTemplate方法测试")
    class GetMessageTemplateTests {

        @Test
        @DisplayName("从第一个provider获取消息")
        void testGetFromFirstProvider() {
            ChainMessageProvider chain = new ChainMessageProvider(
                    createMockProvider("test.key", "First"),
                    createMockProvider("test.key", "Second")
            );

            Optional<String> result = chain.getMessageTemplate("test.key", Locale.ENGLISH);

            assertThat(result).hasValue("First");
        }

        @Test
        @DisplayName("第一个provider没有时从第二个获取")
        void testFallbackToSecond() {
            ChainMessageProvider chain = new ChainMessageProvider(
                    createMockProvider("other.key", "First"),
                    createMockProvider("test.key", "Second")
            );

            Optional<String> result = chain.getMessageTemplate("test.key", Locale.ENGLISH);

            assertThat(result).hasValue("Second");
        }

        @Test
        @DisplayName("所有provider都没有时返回空")
        void testAllEmpty() {
            ChainMessageProvider chain = new ChainMessageProvider(
                    createMockProvider("key1", "Value1"),
                    createMockProvider("key2", "Value2")
            );

            Optional<String> result = chain.getMessageTemplate("nonexistent", Locale.ENGLISH);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("containsMessage方法测试")
    class ContainsMessageTests {

        @Test
        @DisplayName("任一provider包含则返回true")
        void testContainsInAny() {
            ChainMessageProvider chain = new ChainMessageProvider(
                    createMockProvider("key1", "Value1"),
                    createMockProvider("key2", "Value2")
            );

            assertThat(chain.containsMessage("key2", Locale.ENGLISH)).isTrue();
        }

        @Test
        @DisplayName("所有provider都不包含返回false")
        void testContainsNone() {
            ChainMessageProvider chain = new ChainMessageProvider(
                    createMockProvider("key1", "Value1")
            );

            assertThat(chain.containsMessage("nonexistent", Locale.ENGLISH)).isFalse();
        }
    }

    @Nested
    @DisplayName("getKeys方法测试")
    class GetKeysTests {

        @Test
        @DisplayName("合并所有provider的keys")
        void testGetKeysMerged() {
            ChainMessageProvider chain = new ChainMessageProvider(
                    createMockProviderWithKeys(Set.of("key1", "key2")),
                    createMockProviderWithKeys(Set.of("key3", "key4"))
            );

            Set<String> keys = chain.getKeys(Locale.ENGLISH);

            assertThat(keys).containsExactlyInAnyOrder("key1", "key2", "key3", "key4");
        }

        @Test
        @DisplayName("去重keys")
        void testGetKeysDeduped() {
            ChainMessageProvider chain = new ChainMessageProvider(
                    createMockProviderWithKeys(Set.of("key1", "key2")),
                    createMockProviderWithKeys(Set.of("key2", "key3"))
            );

            Set<String> keys = chain.getKeys(Locale.ENGLISH);

            assertThat(keys).containsExactlyInAnyOrder("key1", "key2", "key3");
        }
    }

    @Nested
    @DisplayName("getSupportedLocales方法测试")
    class GetSupportedLocalesTests {

        @Test
        @DisplayName("合并所有provider的Locales")
        void testGetSupportedLocalesMerged() {
            ChainMessageProvider chain = new ChainMessageProvider(
                    createMockProviderWithLocales(Set.of(Locale.ENGLISH)),
                    createMockProviderWithLocales(Set.of(Locale.CHINESE))
            );

            Set<Locale> locales = chain.getSupportedLocales();

            assertThat(locales).containsExactlyInAnyOrder(Locale.ENGLISH, Locale.CHINESE);
        }
    }

    @Nested
    @DisplayName("refresh方法测试")
    class RefreshTests {

        @Test
        @DisplayName("刷新所有provider")
        void testRefreshAll() {
            java.util.concurrent.atomic.AtomicInteger refreshCount = new java.util.concurrent.atomic.AtomicInteger(0);
            MessageProvider provider1 = new MessageProvider() {
                @Override
                public Optional<String> getMessageTemplate(String key, Locale locale) {
                    return Optional.empty();
                }

                @Override
                public void refresh() {
                    refreshCount.incrementAndGet();
                }
            };
            MessageProvider provider2 = new MessageProvider() {
                @Override
                public Optional<String> getMessageTemplate(String key, Locale locale) {
                    return Optional.empty();
                }

                @Override
                public void refresh() {
                    refreshCount.incrementAndGet();
                }
            };

            ChainMessageProvider chain = new ChainMessageProvider(provider1, provider2);
            chain.refresh();

            assertThat(refreshCount.get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("addProvider方法测试")
    class AddProviderTests {

        @Test
        @DisplayName("添加provider")
        void testAddProvider() {
            ChainMessageProvider chain = new ChainMessageProvider(List.of());

            chain.addProvider(createMockProvider("key", "Value"));

            assertThat(chain.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("链式添加")
        void testAddProviderChain() {
            ChainMessageProvider chain = new ChainMessageProvider(List.of())
                    .addProvider(createMockProvider("key1", "Value1"))
                    .addProvider(createMockProvider("key2", "Value2"));

            assertThat(chain.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("使用builder构建")
        void testBuilder() {
            ChainMessageProvider chain = ChainMessageProvider.builder()
                    .add(createMockProvider("key1", "Value1"))
                    .add(createMockProvider("key2", "Value2"))
                    .build();

            assertThat(chain.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("使用addFirst在开头添加")
        void testBuilderAddFirst() {
            ChainMessageProvider chain = ChainMessageProvider.builder()
                    .add(createMockProvider("test.key", "Second"))
                    .addFirst(createMockProvider("test.key", "First"))
                    .build();

            Optional<String> result = chain.getMessageTemplate("test.key", Locale.ENGLISH);
            assertThat(result).hasValue("First");
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现MessageProvider接口")
        void testImplementsInterface() {
            ChainMessageProvider chain = new ChainMessageProvider(List.of());

            assertThat(chain).isInstanceOf(MessageProvider.class);
        }
    }

    private MessageProvider createMockProvider(String key, String value) {
        return (k, locale) -> key.equals(k) ? Optional.of(value) : Optional.empty();
    }

    private MessageProvider createMockProviderWithKeys(Set<String> keys) {
        return new MessageProvider() {
            @Override
            public Optional<String> getMessageTemplate(String key, Locale locale) {
                return keys.contains(key) ? Optional.of("value") : Optional.empty();
            }

            @Override
            public Set<String> getKeys(Locale locale) {
                return keys;
            }
        };
    }

    private MessageProvider createMockProviderWithLocales(Set<Locale> locales) {
        return new MessageProvider() {
            @Override
            public Optional<String> getMessageTemplate(String key, Locale locale) {
                return Optional.empty();
            }

            @Override
            public Set<Locale> getSupportedLocales() {
                return locales;
            }
        };
    }
}
