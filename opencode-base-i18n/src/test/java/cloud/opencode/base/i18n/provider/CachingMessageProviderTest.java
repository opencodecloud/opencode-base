package cloud.opencode.base.i18n.provider;

import cloud.opencode.base.i18n.spi.MessageProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * CachingMessageProvider 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("CachingMessageProvider 测试")
class CachingMessageProviderTest {

    private MockMessageProvider mockProvider;

    @BeforeEach
    void setUp() {
        mockProvider = new MockMessageProvider();
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用默认设置创建")
        void testDefaultConstructor() {
            CachingMessageProvider provider = new CachingMessageProvider(mockProvider);

            assertThat(provider.getCacheSize()).isZero();
        }

        @Test
        @DisplayName("使用自定义设置创建")
        void testCustomConstructor() {
            CachingMessageProvider provider = new CachingMessageProvider(
                    mockProvider, 500, Duration.ofMinutes(30)
            );

            assertThat(provider.getCacheSize()).isZero();
        }
    }

    @Nested
    @DisplayName("getMessageTemplate方法测试")
    class GetMessageTemplateTests {

        @Test
        @DisplayName("从委托获取消息")
        void testGetMessageFromDelegate() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "Test Message");
            CachingMessageProvider provider = new CachingMessageProvider(mockProvider);

            Optional<String> result = provider.getMessageTemplate("test.key", Locale.ENGLISH);

            assertThat(result).hasValue("Test Message");
        }

        @Test
        @DisplayName("缓存消息")
        void testCachesMessage() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "Test Message");
            CachingMessageProvider provider = new CachingMessageProvider(mockProvider);

            provider.getMessageTemplate("test.key", Locale.ENGLISH);
            provider.getMessageTemplate("test.key", Locale.ENGLISH);

            assertThat(provider.getCacheSize()).isEqualTo(1);
            assertThat(mockProvider.getCallCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("缓存未命中")
        void testCacheMiss() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "Test Message");
            CachingMessageProvider provider = new CachingMessageProvider(mockProvider);

            provider.getMessageTemplate("test.key", Locale.ENGLISH);
            provider.getMessageTemplate("other.key", Locale.ENGLISH);

            assertThat(provider.getCacheSize()).isEqualTo(2);
        }

        @Test
        @DisplayName("不同Locale分别缓存")
        void testCachesPerLocale() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "English");
            mockProvider.addMessage("test.key", Locale.CHINESE, "Chinese");
            CachingMessageProvider provider = new CachingMessageProvider(mockProvider);

            provider.getMessageTemplate("test.key", Locale.ENGLISH);
            provider.getMessageTemplate("test.key", Locale.CHINESE);

            assertThat(provider.getCacheSize()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("containsMessage方法测试")
    class ContainsMessageTests {

        @Test
        @DisplayName("消息存在返回true")
        void testContainsMessageTrue() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "Test");
            CachingMessageProvider provider = new CachingMessageProvider(mockProvider);

            assertThat(provider.containsMessage("test.key", Locale.ENGLISH)).isTrue();
        }

        @Test
        @DisplayName("消息不存在返回false")
        void testContainsMessageFalse() {
            CachingMessageProvider provider = new CachingMessageProvider(mockProvider);

            assertThat(provider.containsMessage("nonexistent", Locale.ENGLISH)).isFalse();
        }
    }

    @Nested
    @DisplayName("getKeys方法测试")
    class GetKeysTests {

        @Test
        @DisplayName("委托给底层provider")
        void testGetKeysDelegates() {
            mockProvider.addMessage("key1", Locale.ENGLISH, "Value1");
            mockProvider.addMessage("key2", Locale.ENGLISH, "Value2");
            CachingMessageProvider provider = new CachingMessageProvider(mockProvider);

            Set<String> keys = provider.getKeys(Locale.ENGLISH);

            assertThat(keys).containsExactlyInAnyOrder("key1", "key2");
        }
    }

    @Nested
    @DisplayName("getSupportedLocales方法测试")
    class GetSupportedLocalesTests {

        @Test
        @DisplayName("委托给底层provider")
        void testGetSupportedLocalesDelegates() {
            mockProvider.addSupportedLocale(Locale.ENGLISH);
            mockProvider.addSupportedLocale(Locale.CHINESE);
            CachingMessageProvider provider = new CachingMessageProvider(mockProvider);

            Set<Locale> locales = provider.getSupportedLocales();

            assertThat(locales).containsExactlyInAnyOrder(Locale.ENGLISH, Locale.CHINESE);
        }
    }

    @Nested
    @DisplayName("refresh方法测试")
    class RefreshTests {

        @Test
        @DisplayName("刷新清除缓存")
        void testRefreshClearsCache() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "Test");
            CachingMessageProvider provider = new CachingMessageProvider(mockProvider);

            provider.getMessageTemplate("test.key", Locale.ENGLISH);
            assertThat(provider.getCacheSize()).isEqualTo(1);

            provider.refresh();

            assertThat(provider.getCacheSize()).isZero();
        }

        @Test
        @DisplayName("刷新重置命中率")
        void testRefreshResetsHitRate() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "Test");
            CachingMessageProvider provider = new CachingMessageProvider(mockProvider);

            provider.getMessageTemplate("test.key", Locale.ENGLISH);
            provider.getMessageTemplate("test.key", Locale.ENGLISH);

            provider.refresh();

            assertThat(provider.getHitRate()).isZero();
        }
    }

    @Nested
    @DisplayName("getHitRate方法测试")
    class HitRateTests {

        @Test
        @DisplayName("初始命中率为0")
        void testInitialHitRate() {
            CachingMessageProvider provider = new CachingMessageProvider(mockProvider);

            assertThat(provider.getHitRate()).isZero();
        }

        @Test
        @DisplayName("计算命中率")
        void testHitRateCalculation() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "Test");
            CachingMessageProvider provider = new CachingMessageProvider(mockProvider);

            provider.getMessageTemplate("test.key", Locale.ENGLISH); // miss
            provider.getMessageTemplate("test.key", Locale.ENGLISH); // hit
            provider.getMessageTemplate("test.key", Locale.ENGLISH); // hit

            assertThat(provider.getHitRate()).isCloseTo(0.666, within(0.01));
        }
    }

    @Nested
    @DisplayName("clearCache方法测试")
    class ClearCacheTests {

        @Test
        @DisplayName("清除缓存")
        void testClearCache() {
            mockProvider.addMessage("test.key", Locale.ENGLISH, "Test");
            CachingMessageProvider provider = new CachingMessageProvider(mockProvider);

            provider.getMessageTemplate("test.key", Locale.ENGLISH);
            assertThat(provider.getCacheSize()).isEqualTo(1);

            provider.clearCache();

            assertThat(provider.getCacheSize()).isZero();
        }
    }

    @Nested
    @DisplayName("缓存大小限制测试")
    class CacheSizeLimitTests {

        @Test
        @DisplayName("超过最大大小时驱逐")
        void testEvictionOnMaxSize() {
            CachingMessageProvider provider = new CachingMessageProvider(
                    mockProvider, 2, Duration.ofHours(1)
            );

            for (int i = 0; i < 5; i++) {
                String key = "key" + i;
                mockProvider.addMessage(key, Locale.ENGLISH, "Value" + i);
                provider.getMessageTemplate(key, Locale.ENGLISH);
            }

            assertThat(provider.getCacheSize()).isLessThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现MessageProvider接口")
        void testImplementsInterface() {
            CachingMessageProvider provider = new CachingMessageProvider(mockProvider);

            assertThat(provider).isInstanceOf(MessageProvider.class);
        }
    }

    /**
     * Mock MessageProvider for testing
     */
    private static class MockMessageProvider implements MessageProvider {
        private final java.util.Map<String, String> messages = new java.util.HashMap<>();
        private final Set<Locale> supportedLocales = new java.util.HashSet<>();
        private final AtomicInteger callCount = new AtomicInteger(0);

        void addMessage(String key, Locale locale, String value) {
            messages.put(key + "_" + locale, value);
        }

        void addSupportedLocale(Locale locale) {
            supportedLocales.add(locale);
        }

        int getCallCount() {
            return callCount.get();
        }

        @Override
        public Optional<String> getMessageTemplate(String key, Locale locale) {
            callCount.incrementAndGet();
            return Optional.ofNullable(messages.get(key + "_" + locale));
        }

        @Override
        public Set<String> getKeys(Locale locale) {
            return messages.keySet().stream()
                    .filter(k -> k.endsWith("_" + locale))
                    .map(k -> k.substring(0, k.lastIndexOf("_")))
                    .collect(java.util.stream.Collectors.toSet());
        }

        @Override
        public Set<Locale> getSupportedLocales() {
            return supportedLocales;
        }
    }
}
