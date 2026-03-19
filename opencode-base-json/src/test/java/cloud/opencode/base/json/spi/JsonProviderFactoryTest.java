package cloud.opencode.base.json.spi;

import cloud.opencode.base.json.JsonConfig;
import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.TypeReference;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;
import cloud.opencode.base.json.stream.JsonReader;
import cloud.opencode.base.json.stream.JsonWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonProviderFactory 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonProviderFactory 测试")
class JsonProviderFactoryTest {

    private JsonProvider mockProvider;

    @BeforeEach
    void setUp() {
        mockProvider = createMockProvider("test-provider", "1.0.0", 100);
    }

    @AfterEach
    void tearDown() {
        // Clean up registered test provider
        JsonProviderFactory.unregisterProvider("test-provider");
    }

    @Nested
    @DisplayName("registerProvider方法测试")
    class RegisterProviderTests {

        @Test
        @DisplayName("注册提供者")
        void testRegisterProvider() {
            JsonProviderFactory.registerProvider(mockProvider);

            assertThat(JsonProviderFactory.hasProvider("test-provider")).isTrue();
        }

        @Test
        @DisplayName("null提供者抛出异常")
        void testRegisterNullProvider() {
            assertThatThrownBy(() -> JsonProviderFactory.registerProvider(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("高优先级提供者成为默认")
        void testHighPriorityBecomesDefault() {
            JsonProvider highPriority = createMockProvider("high-priority", "1.0", 1000);
            JsonProviderFactory.registerProvider(highPriority);

            try {
                assertThat(JsonProviderFactory.getProvider().getName()).isEqualTo("high-priority");
            } finally {
                JsonProviderFactory.unregisterProvider("high-priority");
            }
        }
    }

    @Nested
    @DisplayName("unregisterProvider方法测试")
    class UnregisterProviderTests {

        @Test
        @DisplayName("注销已注册提供者")
        void testUnregisterProvider() {
            JsonProviderFactory.registerProvider(mockProvider);
            assertThat(JsonProviderFactory.hasProvider("test-provider")).isTrue();

            JsonProvider removed = JsonProviderFactory.unregisterProvider("test-provider");

            assertThat(removed).isNotNull();
            assertThat(JsonProviderFactory.hasProvider("test-provider")).isFalse();
        }

        @Test
        @DisplayName("注销不存在的提供者返回null")
        void testUnregisterNonExistent() {
            JsonProvider removed = JsonProviderFactory.unregisterProvider("non-existent");

            assertThat(removed).isNull();
        }

        @Test
        @DisplayName("null名称抛出异常")
        void testUnregisterNullName() {
            assertThatThrownBy(() -> JsonProviderFactory.unregisterProvider(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("hasProvider方法测试")
    class HasProviderTests {

        @Test
        @DisplayName("检查已注册提供者")
        void testHasRegisteredProvider() {
            JsonProviderFactory.registerProvider(mockProvider);

            assertThat(JsonProviderFactory.hasProvider("test-provider")).isTrue();
        }

        @Test
        @DisplayName("检查未注册提供者")
        void testHasUnregisteredProvider() {
            assertThat(JsonProviderFactory.hasProvider("non-existent")).isFalse();
        }

        @Test
        @DisplayName("名称不区分大小写")
        void testCaseInsensitive() {
            JsonProviderFactory.registerProvider(mockProvider);

            assertThat(JsonProviderFactory.hasProvider("TEST-PROVIDER")).isTrue();
            assertThat(JsonProviderFactory.hasProvider("Test-Provider")).isTrue();
        }
    }

    @Nested
    @DisplayName("hasAnyProvider方法测试")
    class HasAnyProviderTests {

        @Test
        @DisplayName("有提供者返回true")
        void testHasAnyProvider() {
            JsonProviderFactory.registerProvider(mockProvider);

            assertThat(JsonProviderFactory.hasAnyProvider()).isTrue();
        }
    }

    @Nested
    @DisplayName("getAvailableProviders方法测试")
    class GetAvailableProvidersTests {

        @Test
        @DisplayName("返回提供者列表")
        void testGetAvailableProviders() {
            JsonProviderFactory.registerProvider(mockProvider);

            List<String> providers = JsonProviderFactory.getAvailableProviders();

            assertThat(providers).contains("test-provider");
        }
    }

    @Nested
    @DisplayName("getAllProviders方法测试")
    class GetAllProvidersTests {

        @Test
        @DisplayName("返回所有提供者")
        void testGetAllProviders() {
            JsonProviderFactory.registerProvider(mockProvider);

            var providers = JsonProviderFactory.getAllProviders();

            assertThat(providers).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("setDefaultProvider方法测试")
    class SetDefaultProviderTests {

        @Test
        @DisplayName("设置默认提供者")
        void testSetDefaultProvider() {
            JsonProviderFactory.registerProvider(mockProvider);
            JsonProviderFactory.setDefaultProvider(mockProvider);

            assertThat(JsonProviderFactory.getProvider().getName()).isEqualTo("test-provider");
        }

        @Test
        @DisplayName("按名称设置默认提供者")
        void testSetDefaultProviderByName() {
            JsonProviderFactory.registerProvider(mockProvider);
            JsonProviderFactory.setDefaultProvider("test-provider");

            assertThat(JsonProviderFactory.getProvider().getName()).isEqualTo("test-provider");
        }

        @Test
        @DisplayName("null提供者抛出异常")
        void testSetNullDefaultProvider() {
            assertThatThrownBy(() -> JsonProviderFactory.setDefaultProvider((JsonProvider) null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getProvider方法测试")
    class GetProviderTests {

        @Test
        @DisplayName("按名称获取提供者")
        void testGetProviderByName() {
            JsonProviderFactory.registerProvider(mockProvider);

            JsonProvider provider = JsonProviderFactory.getProvider("test-provider");

            assertThat(provider).isNotNull();
            assertThat(provider.getName()).isEqualTo("test-provider");
        }

        @Test
        @DisplayName("不存在的名称抛出异常")
        void testGetNonExistentProvider() {
            assertThatThrownBy(() -> JsonProviderFactory.getProvider("non-existent"))
                .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("null名称抛出异常")
        void testGetProviderNullName() {
            assertThatThrownBy(() -> JsonProviderFactory.getProvider((String) null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getProviderInfo方法测试")
    class GetProviderInfoTests {

        @Test
        @DisplayName("返回提供者信息")
        void testGetProviderInfo() {
            JsonProviderFactory.registerProvider(mockProvider);

            List<JsonProviderFactory.ProviderInfo> infos = JsonProviderFactory.getProviderInfo();

            assertThat(infos).isNotEmpty();
            assertThat(infos.stream().anyMatch(i -> i.name().equals("test-provider"))).isTrue();
        }
    }

    @Nested
    @DisplayName("ProviderInfo记录测试")
    class ProviderInfoTests {

        @Test
        @DisplayName("创建ProviderInfo")
        void testProviderInfo() {
            JsonProviderFactory.ProviderInfo info = new JsonProviderFactory.ProviderInfo(
                "test", "1.0.0", 10, true);

            assertThat(info.name()).isEqualTo("test");
            assertThat(info.version()).isEqualTo("1.0.0");
            assertThat(info.priority()).isEqualTo(10);
            assertThat(info.isDefault()).isTrue();
        }
    }

    // Helper method to create a mock provider
    private JsonProvider createMockProvider(String name, String version, int priority) {
        return new JsonProvider() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getVersion() {
                return version;
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
            public void configure(JsonConfig config) {}

            @Override
            public boolean supportsFeature(JsonFeature feature) {
                return true;
            }

            @Override
            public String toJson(Object obj) {
                return "{}";
            }

            @Override
            public byte[] toJsonBytes(Object obj) {
                return "{}".getBytes();
            }

            @Override
            public void toJson(Object obj, OutputStream output) {}

            @Override
            public void toJson(Object obj, Writer writer) {}

            @Override
            public <T> T fromJson(String json, Class<T> clazz) {
                return null;
            }

            @Override
            public <T> T fromJson(String json, Type type) {
                return null;
            }

            @Override
            public <T> T fromJson(String json, TypeReference<T> typeReference) {
                return null;
            }

            @Override
            public <T> T fromJson(byte[] json, Class<T> clazz) {
                return null;
            }

            @Override
            public <T> T fromJson(InputStream input, Class<T> clazz) {
                return null;
            }

            @Override
            public <T> T fromJson(Reader reader, Class<T> clazz) {
                return null;
            }

            @Override
            public <T> List<T> fromJsonArray(String json, Class<T> elementType) {
                return List.of();
            }

            @Override
            public <K, V> Map<K, V> fromJsonMap(String json, Class<K> keyType, Class<V> valueType) {
                return Map.of();
            }

            @Override
            public JsonNode parseTree(String json) {
                return JsonNode.nullNode();
            }

            @Override
            public JsonNode parseTree(byte[] json) {
                return JsonNode.nullNode();
            }

            @Override
            public <T> T treeToValue(JsonNode node, Class<T> clazz) {
                return null;
            }

            @Override
            public JsonNode valueToTree(Object obj) {
                return JsonNode.nullNode();
            }

            @Override
            public JsonReader createReader(InputStream input) {
                return null;
            }

            @Override
            public JsonReader createReader(Reader reader) {
                return null;
            }

            @Override
            public JsonWriter createWriter(OutputStream output) {
                return null;
            }

            @Override
            public JsonWriter createWriter(Writer writer) {
                return null;
            }

            @Override
            public <T> T convertValue(Object obj, Class<T> clazz) {
                return null;
            }

            @Override
            public <T> T convertValue(Object obj, TypeReference<T> typeReference) {
                return null;
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T getUnderlyingProvider() {
                return (T) this;
            }

            @Override
            public JsonProvider copy() {
                return this;
            }
        };
    }
}
