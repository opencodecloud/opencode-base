package cloud.opencode.base.log.spi;

import cloud.opencode.base.log.Logger;
import cloud.opencode.base.log.exception.OpenLogException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * LogProviderFactory 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("LogProviderFactory 测试")
class LogProviderFactoryTest {

    @BeforeEach
    void setUp() {
        // Reset factory state for clean tests
        LogProviderFactory.reset();
    }

    @AfterEach
    void tearDown() {
        // Restore after tests
        LogProviderFactory.reset();
    }

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(LogProviderFactory.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = LogProviderFactory.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getProvider方法测试")
    class GetProviderTests {

        @Test
        @DisplayName("获取默认提供者")
        void testGetProvider() {
            LogProvider provider = LogProviderFactory.getProvider();

            assertThat(provider).isNotNull();
            // Default should be DefaultLogProvider if no other providers
            assertThat(provider.getName()).isEqualTo("DEFAULT");
        }

        @Test
        @DisplayName("多次调用返回相同实例")
        void testGetProviderSameInstance() {
            LogProvider provider1 = LogProviderFactory.getProvider();
            LogProvider provider2 = LogProviderFactory.getProvider();

            assertThat(provider1).isSameAs(provider2);
        }
    }

    @Nested
    @DisplayName("getProvider(String)方法测试")
    class GetProviderByNameTests {

        @Test
        @DisplayName("按名称获取提供者")
        void testGetProviderByName() {
            // Initialize first
            LogProviderFactory.getProvider();

            LogProvider provider = LogProviderFactory.getProvider("DEFAULT");
            assertThat(provider).isNotNull();
            assertThat(provider.getName()).isEqualTo("DEFAULT");
        }

        @Test
        @DisplayName("名称不区分大小写")
        void testGetProviderByNameCaseInsensitive() {
            LogProviderFactory.getProvider();

            LogProvider provider1 = LogProviderFactory.getProvider("DEFAULT");
            LogProvider provider2 = LogProviderFactory.getProvider("default");

            assertThat(provider1).isSameAs(provider2);
        }

        @Test
        @DisplayName("未找到提供者抛出异常")
        void testGetProviderByNameNotFound() {
            LogProviderFactory.getProvider();

            assertThatThrownBy(() -> LogProviderFactory.getProvider("NONEXISTENT"))
                .isInstanceOf(OpenLogException.class)
                .hasMessageContaining("not found");
        }
    }

    @Nested
    @DisplayName("setProvider方法测试")
    class SetProviderTests {

        @Test
        @DisplayName("设置自定义提供者")
        void testSetProvider() {
            // Initialize first
            LogProviderFactory.getProvider();

            LogProvider custom = createTestProvider("CUSTOM");
            LogProviderFactory.setProvider(custom);

            assertThat(LogProviderFactory.getProvider().getName()).isEqualTo("CUSTOM");
        }

        @Test
        @DisplayName("设置null抛出异常")
        void testSetProviderNull() {
            assertThatThrownBy(() -> LogProviderFactory.setProvider(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("设置后可按名称获取")
        void testSetProviderByName() {
            LogProvider custom = createTestProvider("MY_PROVIDER");

            LogProviderFactory.setProvider(custom);

            assertThat(LogProviderFactory.getProvider("MY_PROVIDER")).isSameAs(custom);
        }
    }

    @Nested
    @DisplayName("registerProvider方法测试")
    class RegisterProviderTests {

        @Test
        @DisplayName("注册提供者")
        void testRegisterProvider() {
            LogProviderFactory.getProvider(); // Initialize

            LogProvider custom = createTestProvider("REGISTERED");
            LogProviderFactory.registerProvider(custom);

            assertThat(LogProviderFactory.getProvider("REGISTERED")).isSameAs(custom);
        }

        @Test
        @DisplayName("注册null抛出异常")
        void testRegisterProviderNull() {
            assertThatThrownBy(() -> LogProviderFactory.registerProvider(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getAvailableProviders方法测试")
    class GetAvailableProvidersTests {

        @Test
        @DisplayName("返回可用提供者列表")
        void testGetAvailableProviders() {
            LogProviderFactory.getProvider(); // Initialize

            List<String> providers = LogProviderFactory.getAvailableProviders();

            assertThat(providers).isNotNull();
            assertThat(providers).contains("default"); // lowercase
        }

        @Test
        @DisplayName("包含注册的提供者")
        void testGetAvailableProvidersIncludesRegistered() {
            LogProviderFactory.getProvider();
            LogProviderFactory.registerProvider(createTestProvider("EXTRA"));

            List<String> providers = LogProviderFactory.getAvailableProviders();

            assertThat(providers).contains("extra");
        }
    }

    @Nested
    @DisplayName("hasProvider方法测试")
    class HasProviderTests {

        @Test
        @DisplayName("存在的提供者返回true")
        void testHasProviderTrue() {
            LogProviderFactory.getProvider();

            assertThat(LogProviderFactory.hasProvider("DEFAULT")).isTrue();
        }

        @Test
        @DisplayName("不存在的提供者返回false")
        void testHasProviderFalse() {
            LogProviderFactory.getProvider();

            assertThat(LogProviderFactory.hasProvider("NONEXISTENT")).isFalse();
        }

        @Test
        @DisplayName("名称不区分大小写")
        void testHasProviderCaseInsensitive() {
            LogProviderFactory.getProvider();

            assertThat(LogProviderFactory.hasProvider("default")).isTrue();
            assertThat(LogProviderFactory.hasProvider("DEFAULT")).isTrue();
            assertThat(LogProviderFactory.hasProvider("Default")).isTrue();
        }
    }

    @Nested
    @DisplayName("初始化测试")
    class InitializationTests {

        @Test
        @DisplayName("懒加载初始化")
        void testLazyInitialization() {
            // After reset, factory is not initialized
            // First call to getProvider should initialize
            assertThatCode(() -> LogProviderFactory.getProvider()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("线程安全初始化")
        void testThreadSafeInitialization() throws Exception {
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            LogProvider[] providers = new LogProvider[threadCount];

            for (int i = 0; i < threadCount; i++) {
                int index = i;
                threads[i] = new Thread(() -> {
                    providers[index] = LogProviderFactory.getProvider();
                });
            }

            for (Thread t : threads) t.start();
            for (Thread t : threads) t.join();

            // All should get the same provider
            for (int i = 1; i < threadCount; i++) {
                assertThat(providers[i]).isSameAs(providers[0]);
            }
        }
    }

    @Nested
    @DisplayName("reset方法测试")
    class ResetTests {

        @Test
        @DisplayName("reset清除状态")
        void testReset() {
            LogProviderFactory.getProvider();
            LogProviderFactory.registerProvider(createTestProvider("CUSTOM"));

            LogProviderFactory.reset();

            // After reset, need to reinitialize
            LogProvider provider = LogProviderFactory.getProvider();
            assertThat(provider.getName()).isEqualTo("DEFAULT");
            assertThat(LogProviderFactory.hasProvider("CUSTOM")).isFalse();
        }
    }

    // Helper method to create test providers
    private LogProvider createTestProvider(String name) {
        return new LogProvider() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            public Logger getLogger(String loggerName) {
                return new DefaultLogProvider().getLogger(loggerName);
            }

            @Override
            public MDCAdapter getMDCAdapter() {
                return new DefaultLogProvider().getMDCAdapter();
            }
        };
    }
}
