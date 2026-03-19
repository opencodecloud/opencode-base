package cloud.opencode.base.oauth2.provider;

import cloud.opencode.base.oauth2.exception.OAuth2Exception;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ProviderRegistryTest Tests
 * ProviderRegistryTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("ProviderRegistry 测试")
class ProviderRegistryTest {

    private ProviderRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ProviderRegistry();
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建空注册表")
        void testEmptyRegistry() {
            ProviderRegistry empty = new ProviderRegistry();
            assertThat(empty.isEmpty()).isTrue();
            assertThat(empty.size()).isZero();
        }
    }

    @Nested
    @DisplayName("global方法测试")
    class GlobalTests {

        @Test
        @DisplayName("global返回全局实例")
        void testGlobal() {
            ProviderRegistry global1 = ProviderRegistry.global();
            ProviderRegistry global2 = ProviderRegistry.global();

            assertThat(global1).isSameAs(global2);
        }

        @Test
        @DisplayName("全局实例包含内置providers")
        void testGlobalContainsBuiltins() {
            ProviderRegistry global = ProviderRegistry.global();

            assertThat(global.contains("google")).isTrue();
            assertThat(global.contains("microsoft")).isTrue();
            assertThat(global.contains("github")).isTrue();
            assertThat(global.contains("apple")).isTrue();
            assertThat(global.contains("facebook")).isTrue();
        }
    }

    @Nested
    @DisplayName("withBuiltins方法测试")
    class WithBuiltinsTests {

        @Test
        @DisplayName("withBuiltins创建包含内置providers的新实例")
        void testWithBuiltins() {
            ProviderRegistry registry = ProviderRegistry.withBuiltins();

            assertThat(registry.contains("google")).isTrue();
            assertThat(registry.contains("microsoft")).isTrue();
            assertThat(registry.size()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("register方法测试")
    class RegisterTests {

        @Test
        @DisplayName("register(OAuth2Provider)注册provider")
        void testRegisterProvider() {
            OAuth2Provider provider = createTestProvider("TestProvider");
            registry.register(provider);

            assertThat(registry.contains("testprovider")).isTrue();
        }

        @Test
        @DisplayName("register(String, OAuth2Provider)使用自定义名称注册")
        void testRegisterWithCustomName() {
            OAuth2Provider provider = createTestProvider("Original");
            registry.register("custom", provider);

            assertThat(registry.contains("custom")).isTrue();
            assertThat(registry.contains("original")).isFalse();
        }

        @Test
        @DisplayName("register返回this用于链式调用")
        void testRegisterReturnsThis() {
            ProviderRegistry result = registry.register(createTestProvider("Test1"));
            assertThat(result).isSameAs(registry);
        }

        @Test
        @DisplayName("register null provider抛出异常")
        void testRegisterNullProvider() {
            assertThatThrownBy(() -> registry.register((OAuth2Provider) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("get方法测试")
    class GetTests {

        @Test
        @DisplayName("get返回已注册的provider")
        void testGet() {
            OAuth2Provider provider = createTestProvider("MyProvider");
            registry.register(provider);

            OAuth2Provider found = registry.get("myprovider");
            assertThat(found).isNotNull();
            assertThat(found.name()).isEqualTo("MyProvider");
        }

        @Test
        @DisplayName("get大小写不敏感")
        void testGetCaseInsensitive() {
            registry.register(createTestProvider("TestProvider"));

            assertThat(registry.get("testprovider")).isNotNull();
            assertThat(registry.get("TESTPROVIDER")).isNotNull();
            assertThat(registry.get("TestProvider")).isNotNull();
        }

        @Test
        @DisplayName("get未找到时抛出异常")
        void testGetNotFound() {
            assertThatThrownBy(() -> registry.get("nonexistent"))
                    .isInstanceOf(OAuth2Exception.class);
        }
    }

    @Nested
    @DisplayName("find方法测试")
    class FindTests {

        @Test
        @DisplayName("find返回Optional")
        void testFind() {
            registry.register(createTestProvider("MyProvider"));

            Optional<OAuth2Provider> found = registry.find("myprovider");
            assertThat(found).isPresent();
        }

        @Test
        @DisplayName("find未找到返回empty")
        void testFindNotFound() {
            Optional<OAuth2Provider> found = registry.find("nonexistent");
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("find null返回empty")
        void testFindNull() {
            Optional<OAuth2Provider> found = registry.find(null);
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("contains方法测试")
    class ContainsTests {

        @Test
        @DisplayName("contains返回正确结果")
        void testContains() {
            registry.register(createTestProvider("MyProvider"));

            assertThat(registry.contains("myprovider")).isTrue();
            assertThat(registry.contains("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("contains null返回false")
        void testContainsNull() {
            assertThat(registry.contains(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("remove方法测试")
    class RemoveTests {

        @Test
        @DisplayName("remove移除并返回provider")
        void testRemove() {
            registry.register(createTestProvider("MyProvider"));

            Optional<OAuth2Provider> removed = registry.remove("myprovider");
            assertThat(removed).isPresent();
            assertThat(registry.contains("myprovider")).isFalse();
        }

        @Test
        @DisplayName("remove不存在的provider返回empty")
        void testRemoveNotFound() {
            Optional<OAuth2Provider> removed = registry.remove("nonexistent");
            assertThat(removed).isEmpty();
        }

        @Test
        @DisplayName("remove null返回empty")
        void testRemoveNull() {
            Optional<OAuth2Provider> removed = registry.remove(null);
            assertThat(removed).isEmpty();
        }
    }

    @Nested
    @DisplayName("集合操作测试")
    class CollectionOperationsTests {

        @Test
        @DisplayName("names返回所有名称")
        void testNames() {
            registry.register(createTestProvider("Provider1"));
            registry.register(createTestProvider("Provider2"));

            assertThat(registry.names()).containsExactlyInAnyOrder("provider1", "provider2");
        }

        @Test
        @DisplayName("all返回所有providers")
        void testAll() {
            registry.register(createTestProvider("Provider1"));
            registry.register(createTestProvider("Provider2"));

            assertThat(registry.all()).hasSize(2);
        }

        @Test
        @DisplayName("size返回正确数量")
        void testSize() {
            assertThat(registry.size()).isZero();

            registry.register(createTestProvider("Provider1"));
            assertThat(registry.size()).isEqualTo(1);

            registry.register(createTestProvider("Provider2"));
            assertThat(registry.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("isEmpty")
        void testIsEmpty() {
            assertThat(registry.isEmpty()).isTrue();

            registry.register(createTestProvider("Provider1"));
            assertThat(registry.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("clear清除所有providers")
        void testClear() {
            registry.register(createTestProvider("Provider1"));
            registry.register(createTestProvider("Provider2"));

            registry.clear();
            assertThat(registry.isEmpty()).isTrue();
        }
    }

    private OAuth2Provider createTestProvider(String name) {
        return CustomProvider.builder()
                .name(name)
                .authorizationEndpoint("https://auth.test.com/authorize")
                .tokenEndpoint("https://auth.test.com/token")
                .build();
    }
}
