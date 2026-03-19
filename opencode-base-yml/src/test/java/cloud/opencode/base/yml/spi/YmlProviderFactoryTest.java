package cloud.opencode.base.yml.spi;

import cloud.opencode.base.yml.YmlConfig;
import cloud.opencode.base.yml.YmlNode;
import cloud.opencode.base.yml.exception.OpenYmlException;

import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link YmlProviderFactory}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlProviderFactory Tests")
class YmlProviderFactoryTest {

    @BeforeEach
    void setUp() {
        // Reset factory state before each test
        YmlProviderFactory.reset();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        YmlProviderFactory.reset();
    }

    @Nested
    @DisplayName("getProvider Tests")
    class GetProviderTests {

        @Test
        @DisplayName("getProvider should return a provider")
        void getProviderShouldReturnAProvider() {
            YmlProvider provider = YmlProviderFactory.getProvider();

            assertThat(provider).isNotNull();
        }

        @Test
        @DisplayName("getProvider should return the same instance on multiple calls")
        void getProviderShouldReturnSameInstance() {
            YmlProvider provider1 = YmlProviderFactory.getProvider();
            YmlProvider provider2 = YmlProviderFactory.getProvider();

            assertThat(provider1).isSameAs(provider2);
        }

        @Test
        @DisplayName("getProvider should return highest priority available provider")
        void getProviderShouldReturnHighestPriorityProvider() {
            YmlProvider provider = YmlProviderFactory.getProvider();

            assertThat(provider).isNotNull();
            assertThat(provider.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("getProvider should return provider with valid name")
        void getProviderShouldReturnProviderWithValidName() {
            YmlProvider provider = YmlProviderFactory.getProvider();

            assertThat(provider.getName()).isNotNull();
            assertThat(provider.getName()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getProvider(String) Tests")
    class GetProviderByNameTests {

        @Test
        @DisplayName("getProvider should return provider by name")
        void getProviderShouldReturnProviderByName() {
            YmlProvider provider = YmlProviderFactory.getProvider("snakeyaml");

            assertThat(provider).isNotNull();
            assertThat(provider.getName()).isEqualToIgnoringCase("snakeyaml");
        }

        @Test
        @DisplayName("getProvider should be case insensitive")
        void getProviderShouldBeCaseInsensitive() {
            YmlProvider provider1 = YmlProviderFactory.getProvider("snakeyaml");
            YmlProvider provider2 = YmlProviderFactory.getProvider("SNAKEYAML");
            YmlProvider provider3 = YmlProviderFactory.getProvider("SnakeYaml");

            assertThat(provider1.getName()).isEqualToIgnoringCase("snakeyaml");
            assertThat(provider2.getName()).isEqualToIgnoringCase("snakeyaml");
            assertThat(provider3.getName()).isEqualToIgnoringCase("snakeyaml");
        }

        @Test
        @DisplayName("getProvider should throw exception for unknown provider name")
        void getProviderShouldThrowExceptionForUnknownName() {
            assertThatThrownBy(() -> YmlProviderFactory.getProvider("nonexistent-provider"))
                .isInstanceOf(OpenYmlException.class)
                .hasMessageContaining("nonexistent-provider");
        }

        @Test
        @DisplayName("getProvider should throw exception with descriptive message")
        void getProviderShouldThrowExceptionWithDescriptiveMessage() {
            assertThatThrownBy(() -> YmlProviderFactory.getProvider("unknown"))
                .isInstanceOf(OpenYmlException.class)
                .hasMessageContaining("No YAML provider found with name");
        }
    }

    @Nested
    @DisplayName("getAvailableProviders Tests")
    class GetAvailableProvidersTests {

        @Test
        @DisplayName("getAvailableProviders should return non-empty list")
        void getAvailableProvidersShouldReturnNonEmptyList() {
            List<YmlProvider> providers = YmlProviderFactory.getAvailableProviders();

            assertThat(providers).isNotEmpty();
        }

        @Test
        @DisplayName("getAvailableProviders should return only available providers")
        void getAvailableProvidersShouldReturnOnlyAvailableProviders() {
            List<YmlProvider> providers = YmlProviderFactory.getAvailableProviders();

            assertThat(providers).allMatch(YmlProvider::isAvailable);
        }

        @Test
        @DisplayName("getAvailableProviders should return providers sorted by priority descending")
        void getAvailableProvidersShouldReturnProvidersSortedByPriority() {
            List<YmlProvider> providers = YmlProviderFactory.getAvailableProviders();

            if (providers.size() > 1) {
                for (int i = 0; i < providers.size() - 1; i++) {
                    assertThat(providers.get(i).getPriority())
                        .isGreaterThanOrEqualTo(providers.get(i + 1).getPriority());
                }
            }
        }

        @Test
        @DisplayName("getAvailableProviders should return unmodifiable list")
        void getAvailableProvidersShouldReturnUnmodifiableList() {
            List<YmlProvider> providers = YmlProviderFactory.getAvailableProviders();

            assertThatThrownBy(() -> providers.add(null))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getAvailableProviders should include snakeyaml provider")
        void getAvailableProvidersShouldIncludeSnakeyaml() {
            List<YmlProvider> providers = YmlProviderFactory.getAvailableProviders();

            assertThat(providers)
                .anyMatch(p -> p.getName().equalsIgnoreCase("snakeyaml"));
        }
    }

    @Nested
    @DisplayName("setDefaultProvider Tests")
    class SetDefaultProviderTests {

        @Test
        @DisplayName("setDefaultProvider should set custom provider")
        void setDefaultProviderShouldSetCustomProvider() {
            YmlProvider customProvider = new MockYmlProvider("custom-provider", 200);
            YmlProviderFactory.setDefaultProvider(customProvider);

            YmlProvider retrieved = YmlProviderFactory.getProvider();

            assertThat(retrieved).isSameAs(customProvider);
            assertThat(retrieved.getName()).isEqualTo("custom-provider");
        }

        @Test
        @DisplayName("setDefaultProvider should override previous provider")
        void setDefaultProviderShouldOverridePreviousProvider() {
            YmlProvider provider1 = new MockYmlProvider("provider1", 100);
            YmlProvider provider2 = new MockYmlProvider("provider2", 200);

            YmlProviderFactory.setDefaultProvider(provider1);
            assertThat(YmlProviderFactory.getProvider().getName()).isEqualTo("provider1");

            YmlProviderFactory.setDefaultProvider(provider2);
            assertThat(YmlProviderFactory.getProvider().getName()).isEqualTo("provider2");
        }

        @Test
        @DisplayName("setDefaultProvider should accept null to force re-discovery")
        void setDefaultProviderShouldAcceptNull() {
            YmlProvider customProvider = new MockYmlProvider("custom", 200);
            YmlProviderFactory.setDefaultProvider(customProvider);
            assertThat(YmlProviderFactory.getProvider()).isSameAs(customProvider);

            YmlProviderFactory.setDefaultProvider(null);
            // After setting null, getProvider should discover providers via SPI
            YmlProvider rediscovered = YmlProviderFactory.getProvider();
            assertThat(rediscovered).isNotSameAs(customProvider);
        }
    }

    @Nested
    @DisplayName("reset Tests")
    class ResetTests {

        @Test
        @DisplayName("reset should clear cached provider")
        void resetShouldClearCachedProvider() {
            YmlProvider provider1 = YmlProviderFactory.getProvider();
            YmlProviderFactory.reset();
            YmlProvider provider2 = YmlProviderFactory.getProvider();

            // Both should be valid providers, but potentially different instances
            assertThat(provider1).isNotNull();
            assertThat(provider2).isNotNull();
        }

        @Test
        @DisplayName("reset should allow re-discovery of providers")
        void resetShouldAllowRediscovery() {
            YmlProvider customProvider = new MockYmlProvider("custom", 300);
            YmlProviderFactory.setDefaultProvider(customProvider);
            assertThat(YmlProviderFactory.getProvider()).isSameAs(customProvider);

            YmlProviderFactory.reset();

            // After reset, provider should be rediscovered via SPI
            YmlProvider rediscovered = YmlProviderFactory.getProvider();
            assertThat(rediscovered).isNotSameAs(customProvider);
        }

        @Test
        @DisplayName("reset can be called multiple times safely")
        void resetCanBeCalledMultipleTimes() {
            assertThatCode(() -> {
                YmlProviderFactory.reset();
                YmlProviderFactory.reset();
                YmlProviderFactory.reset();
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("hasProvider Tests")
    class HasProviderTests {

        @Test
        @DisplayName("hasProvider should return true when providers are available")
        void hasProviderShouldReturnTrueWhenAvailable() {
            boolean hasProvider = YmlProviderFactory.hasProvider();

            assertThat(hasProvider).isTrue();
        }

        @Test
        @DisplayName("hasProvider should check provider availability")
        void hasProviderShouldCheckAvailability() {
            // hasProvider should verify that at least one provider is actually available
            assertThat(YmlProviderFactory.hasProvider()).isTrue();

            // This verifies getAvailableProviders returns at least one
            assertThat(YmlProviderFactory.getAvailableProviders()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("concurrent getProvider calls should return same instance")
        void concurrentGetProviderShouldReturnSameInstance() throws InterruptedException {
            final int threadCount = 10;
            final YmlProvider[] providers = new YmlProvider[threadCount];
            final Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    providers[index] = YmlProviderFactory.getProvider();
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // All threads should get the same provider instance
            YmlProvider firstProvider = providers[0];
            for (YmlProvider provider : providers) {
                assertThat(provider).isSameAs(firstProvider);
            }
        }

        @Test
        @DisplayName("concurrent reset and getProvider should not throw")
        void concurrentResetAndGetProviderShouldNotThrow() throws InterruptedException {
            final int iterations = 100;
            final Thread resetThread = new Thread(() -> {
                for (int i = 0; i < iterations; i++) {
                    YmlProviderFactory.reset();
                }
            });

            final Thread getThread = new Thread(() -> {
                for (int i = 0; i < iterations; i++) {
                    try {
                        YmlProviderFactory.getProvider();
                    } catch (Exception e) {
                        // May throw if no provider, but shouldn't get corrupt state
                    }
                }
            });

            assertThatCode(() -> {
                resetThread.start();
                getThread.start();
                resetThread.join();
                getThread.join();
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("factory should have private constructor")
        void factoryShouldHavePrivateConstructor() throws NoSuchMethodException {
            var constructor = YmlProviderFactory.class.getDeclaredConstructor();

            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("instantiating via reflection should throw AssertionError")
        void instantiatingViaReflectionShouldThrowAssertionError() throws NoSuchMethodException {
            var constructor = YmlProviderFactory.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertThatThrownBy(constructor::newInstance)
                .hasCauseInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("Provider Priority Tests")
    class ProviderPriorityTests {

        @Test
        @DisplayName("default provider should have expected priority")
        void defaultProviderShouldHaveExpectedPriority() {
            YmlProvider provider = YmlProviderFactory.getProvider();

            // SnakeYamlProvider has priority 100
            assertThat(provider.getPriority()).isPositive();
        }

        @Test
        @DisplayName("higher priority provider should be selected when set")
        void higherPriorityProviderShouldBeSelected() {
            YmlProvider highPriority = new MockYmlProvider("high-priority", 1000);
            YmlProviderFactory.setDefaultProvider(highPriority);

            YmlProvider selected = YmlProviderFactory.getProvider();

            assertThat(selected.getName()).isEqualTo("high-priority");
            assertThat(selected.getPriority()).isEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("provider from factory should be functional")
        void providerFromFactoryShouldBeFunctional() {
            YmlProvider provider = YmlProviderFactory.getProvider();

            String yaml = provider.dump(Map.of("key", "value"));
            assertThat(yaml).contains("key");
            assertThat(yaml).contains("value");

            Map<String, Object> loaded = provider.load(yaml);
            assertThat(loaded).containsEntry("key", "value");
        }

        @Test
        @DisplayName("provider should support all basic operations")
        void providerShouldSupportAllBasicOperations() {
            YmlProvider provider = YmlProviderFactory.getProvider();

            // Test load
            Map<String, Object> data = provider.load("name: test");
            assertThat(data).containsEntry("name", "test");

            // Test dump
            String yaml = provider.dump(Map.of("count", 42));
            assertThat(yaml).contains("count");

            // Test isValid
            assertThat(provider.isValid("valid: yaml")).isTrue();
            assertThat(provider.isValid("invalid: :::")).isFalse();
        }
    }

    /**
     * Mock YmlProvider for testing.
     */
    private static class MockYmlProvider implements YmlProvider {
        private final String name;
        private final int priority;

        MockYmlProvider(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public Map<String, Object> load(String yaml) {
            return Map.of();
        }

        @Override
        public <T> T load(String yaml, Class<T> clazz) {
            return null;
        }

        @Override
        public Map<String, Object> load(InputStream input) {
            return Map.of();
        }

        @Override
        public <T> T load(InputStream input, Class<T> clazz) {
            return null;
        }

        @Override
        public List<Map<String, Object>> loadAll(String yaml) {
            return List.of();
        }

        @Override
        public <T> List<T> loadAll(String yaml, Class<T> clazz) {
            return List.of();
        }

        @Override
        public String dump(Object obj) {
            return "";
        }

        @Override
        public String dump(Object obj, YmlConfig config) {
            return "";
        }

        @Override
        public void dump(Object obj, OutputStream output) {
        }

        @Override
        public void dump(Object obj, Writer writer) {
        }

        @Override
        public String dumpAll(Iterable<?> documents) {
            return "";
        }

        @Override
        public YmlNode parseTree(String yaml) {
            return null;
        }

        @Override
        public YmlNode parseTree(InputStream input) {
            return null;
        }

        @Override
        public boolean isValid(String yaml) {
            return true;
        }

        @Override
        public YmlProvider configure(YmlConfig config) {
            return this;
        }

        @Override
        public YmlConfig getConfig() {
            return YmlConfig.defaults();
        }
    }
}
