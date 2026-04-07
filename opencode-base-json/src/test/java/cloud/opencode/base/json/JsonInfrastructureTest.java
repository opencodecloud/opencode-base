package cloud.opencode.base.json;

import cloud.opencode.base.json.adapter.JsonTypeAdapter;
import cloud.opencode.base.json.adapter.JsonTypeAdapterFactory;
import cloud.opencode.base.json.spi.JsonFeature;
import cloud.opencode.base.json.spi.JsonModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * JSON Infrastructure Test - Tests for Module, Mixin, Filter, and Feature infrastructure
 * JSON 基础设施测试 - 模块、混入、过滤器和特性基础设施的测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JSON Infrastructure 测试")
class JsonInfrastructureTest {

    // ==================== JsonModule Tests ====================

    @Nested
    @DisplayName("JsonModule.SimpleModule 测试")
    class SimpleModuleTests {

        @Test
        @DisplayName("创建SimpleModule并获取名称和版本")
        void testSimpleModuleNameAndVersion() {
            JsonModule.SimpleModule module = createTestModule("test-module", "1.0.0");

            assertThat(module.getName()).isEqualTo("test-module");
            assertThat(module.getVersion()).isEqualTo("1.0.0");
        }

        @Test
        @DisplayName("添加适配器")
        void testAddAdapter() {
            JsonModule.SimpleModule module = createTestModule("test", "1.0");
            JsonTypeAdapter<String> adapter = JsonTypeAdapter.ofString(
                    String.class, s -> s, s -> s);

            module.addAdapter(adapter);

            assertThat(module.getAdapters()).hasSize(1);
            assertThat(module.getAdapters().getFirst()).isSameAs(adapter);
        }

        @Test
        @DisplayName("添加工厂")
        void testAddFactory() {
            JsonModule.SimpleModule module = createTestModule("test", "1.0");
            JsonTypeAdapterFactory factory = new JsonTypeAdapterFactory() {
                @Override
                public <T> JsonTypeAdapter<T> create(Class<T> type) {
                    return null;
                }
            };

            module.addFactory(factory);

            assertThat(module.getFactories()).hasSize(1);
        }

        @Test
        @DisplayName("添加混入")
        void testAddMixin() {
            JsonModule.SimpleModule module = createTestModule("test", "1.0");

            module.addMixin(String.class, CharSequence.class);

            assertThat(module.getMixins()).hasSize(1);
            assertThat(module.getMixins().get(String.class)).isEqualTo(CharSequence.class);
        }

        @Test
        @DisplayName("setupModule注册所有组件")
        void testSetupModuleRegistersComponents() {
            JsonModule.SimpleModule module = createTestModule("test", "1.0");
            JsonTypeAdapter<String> adapter = JsonTypeAdapter.ofString(
                    String.class, s -> s, s -> s);
            module.addAdapter(adapter);
            module.addMixin(String.class, CharSequence.class);

            boolean[] adapterAdded = {false};
            boolean[] mixinAdded = {false};

            module.setupModule(new JsonModule.SetupContext() {
                @Override
                public void addTypeAdapter(JsonTypeAdapter<?> a) {
                    adapterAdded[0] = true;
                }

                @Override
                public void addTypeAdapterFactory(JsonTypeAdapterFactory factory) {
                }

                @Override
                public void addMixin(Class<?> target, Class<?> mixin) {
                    mixinAdded[0] = true;
                }

                @Override
                public void addKeyAdapter(Class<?> type, JsonTypeAdapter<?> a) {
                }

                @Override
                public JsonConfig getConfig() {
                    return JsonConfig.DEFAULT;
                }
            });

            assertThat(adapterAdded[0]).isTrue();
            assertThat(mixinAdded[0]).isTrue();
        }

        @Test
        @DisplayName("null名称抛出异常")
        void testNullNameThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> createTestModule(null, "1.0"))
                    .withMessageContaining("name");
        }

        @Test
        @DisplayName("null版本抛出异常")
        void testNullVersionThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> createTestModule("test", null))
                    .withMessageContaining("version");
        }

        @Test
        @DisplayName("toString包含名称和版本")
        void testToString() {
            JsonModule.SimpleModule module = createTestModule("my-module", "2.0.0");

            assertThat(module.toString()).contains("my-module").contains("2.0.0");
        }

        @Test
        @DisplayName("适配器列表不可修改")
        void testAdaptersUnmodifiable() {
            JsonModule.SimpleModule module = createTestModule("test", "1.0");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> module.getAdapters().add(null));
        }

        @Test
        @DisplayName("链式调用")
        void testChaining() {
            JsonTypeAdapter<String> adapter = JsonTypeAdapter.ofString(
                    String.class, s -> s, s -> s);

            JsonModule.SimpleModule module = createTestModule("test", "1.0")
                    .addAdapter(adapter)
                    .addMixin(String.class, CharSequence.class);

            assertThat(module.getAdapters()).hasSize(1);
            assertThat(module.getMixins()).hasSize(1);
        }

        private JsonModule.SimpleModule createTestModule(String name, String version) {
            return new JsonModule.SimpleModule(name, version) {
                // Concrete subclass for testing
            };
        }
    }

    // ==================== MixinSource Tests ====================

    @Nested
    @DisplayName("MixinSource 测试")
    class MixinSourceTests {

        @Test
        @DisplayName("添加混入")
        void testAddMixin() {
            MixinSource source = new MixinSource();

            source.addMixin(String.class, CharSequence.class);

            assertThat(source.hasMixin(String.class)).isTrue();
            assertThat(source.getMixin(String.class)).isEqualTo(CharSequence.class);
        }

        @Test
        @DisplayName("移除混入")
        void testRemoveMixin() {
            MixinSource source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);

            source.removeMixin(String.class);

            assertThat(source.hasMixin(String.class)).isFalse();
            assertThat(source.getMixin(String.class)).isNull();
        }

        @Test
        @DisplayName("获取不存在的混入返回null")
        void testGetNonExistentMixin() {
            MixinSource source = new MixinSource();

            assertThat(source.getMixin(String.class)).isNull();
            assertThat(source.hasMixin(String.class)).isFalse();
        }

        @Test
        @DisplayName("获取所有混入")
        void testGetAllMixins() {
            MixinSource source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);
            source.addMixin(Integer.class, Number.class);

            Map<Class<?>, Class<?>> mixins = source.getMixins();

            assertThat(mixins).hasSize(2);
            assertThat(mixins.get(String.class)).isEqualTo(CharSequence.class);
            assertThat(mixins.get(Integer.class)).isEqualTo(Number.class);
        }

        @Test
        @DisplayName("返回的Map不可修改")
        void testGetMixinsUnmodifiable() {
            MixinSource source = new MixinSource();

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> source.getMixins().put(String.class, Object.class));
        }

        @Test
        @DisplayName("清除所有混入")
        void testClear() {
            MixinSource source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);
            source.addMixin(Integer.class, Number.class);

            source.clear();

            assertThat(source.size()).isZero();
            assertThat(source.hasMixin(String.class)).isFalse();
        }

        @Test
        @DisplayName("null目标类抛出异常")
        void testNullTargetThrows() {
            MixinSource source = new MixinSource();

            assertThatNullPointerException()
                    .isThrownBy(() -> source.addMixin(null, CharSequence.class));
        }

        @Test
        @DisplayName("null混入类抛出异常")
        void testNullMixinThrows() {
            MixinSource source = new MixinSource();

            assertThatNullPointerException()
                    .isThrownBy(() -> source.addMixin(String.class, null));
        }

        @Test
        @DisplayName("覆盖已存在的混入")
        void testOverwriteMixin() {
            MixinSource source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);

            source.addMixin(String.class, Comparable.class);

            assertThat(source.getMixin(String.class)).isEqualTo(Comparable.class);
            assertThat(source.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("size返回正确数量")
        void testSize() {
            MixinSource source = new MixinSource();

            assertThat(source.size()).isZero();

            source.addMixin(String.class, CharSequence.class);
            assertThat(source.size()).isEqualTo(1);

            source.addMixin(Integer.class, Number.class);
            assertThat(source.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("toString包含数量")
        void testToString() {
            MixinSource source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);

            assertThat(source.toString()).contains("1");
        }
    }

    // ==================== PropertyFilter Tests ====================

    @Nested
    @DisplayName("PropertyFilter 测试")
    class PropertyFilterTests {

        @Test
        @DisplayName("includeAll包含所有属性")
        void testIncludeAll() {
            PropertyFilter filter = PropertyFilter.includeAll();

            assertThat(filter.includeProperty("name", "value", Object.class)).isTrue();
            assertThat(filter.includeProperty("password", null, Object.class)).isTrue();
        }

        @Test
        @DisplayName("excludeAll排除所有属性")
        void testExcludeAll() {
            PropertyFilter filter = PropertyFilter.excludeAll();

            assertThat(filter.includeProperty("name", "value", Object.class)).isFalse();
            assertThat(filter.includeProperty("password", null, Object.class)).isFalse();
        }

        @Test
        @DisplayName("include白名单过滤")
        void testInclude() {
            PropertyFilter filter = PropertyFilter.include("name", "email");

            assertThat(filter.includeProperty("name", "John", Object.class)).isTrue();
            assertThat(filter.includeProperty("email", "j@j.com", Object.class)).isTrue();
            assertThat(filter.includeProperty("password", "secret", Object.class)).isFalse();
        }

        @Test
        @DisplayName("exclude黑名单过滤")
        void testExclude() {
            PropertyFilter filter = PropertyFilter.exclude("password", "secret");

            assertThat(filter.includeProperty("name", "John", Object.class)).isTrue();
            assertThat(filter.includeProperty("password", "123", Object.class)).isFalse();
            assertThat(filter.includeProperty("secret", "abc", Object.class)).isFalse();
        }

        @Test
        @DisplayName("includeNonNull排除null值")
        void testIncludeNonNull() {
            PropertyFilter filter = PropertyFilter.includeNonNull();

            assertThat(filter.includeProperty("name", "John", Object.class)).isTrue();
            assertThat(filter.includeProperty("name", null, Object.class)).isFalse();
        }

        @Test
        @DisplayName("include空数组匹配无属性")
        void testIncludeEmpty() {
            PropertyFilter filter = PropertyFilter.include();

            assertThat(filter.includeProperty("anything", "value", Object.class)).isFalse();
        }

        @Test
        @DisplayName("exclude空数组匹配所有属性")
        void testExcludeEmpty() {
            PropertyFilter filter = PropertyFilter.exclude();

            assertThat(filter.includeProperty("anything", "value", Object.class)).isTrue();
        }

        @Test
        @DisplayName("null参数抛出异常")
        void testNullPropertiesThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> PropertyFilter.include((String[]) null));
            assertThatNullPointerException()
                    .isThrownBy(() -> PropertyFilter.exclude((String[]) null));
        }
    }

    // ==================== JsonFeature Tests ====================

    @Nested
    @DisplayName("JsonFeature新特性 测试")
    class JsonFeatureTests {

        @Test
        @DisplayName("序列化新特性存在")
        void testSerializationFeatures() {
            assertThat(JsonFeature.WRAP_ROOT_VALUE).isNotNull();
            assertThat(JsonFeature.WRAP_ROOT_VALUE.getCategory())
                    .isEqualTo(JsonFeature.Category.SERIALIZATION);
            assertThat(JsonFeature.WRAP_ROOT_VALUE.isEnabledByDefault()).isFalse();

            assertThat(JsonFeature.WRITE_ENUMS_USING_INDEX).isNotNull();
            assertThat(JsonFeature.WRITE_ENUMS_USING_INDEX.getCategory())
                    .isEqualTo(JsonFeature.Category.SERIALIZATION);
            assertThat(JsonFeature.WRITE_ENUMS_USING_INDEX.isEnabledByDefault()).isFalse();

            assertThat(JsonFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS).isNotNull();
            assertThat(JsonFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS.getCategory())
                    .isEqualTo(JsonFeature.Category.SERIALIZATION);
            assertThat(JsonFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS.isEnabledByDefault()).isFalse();
        }

        @Test
        @DisplayName("反序列化新特性存在")
        void testDeserializationFeatures() {
            assertThat(JsonFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL).isNotNull();
            assertThat(JsonFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL.getCategory())
                    .isEqualTo(JsonFeature.Category.DESERIALIZATION);
            assertThat(JsonFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL.isEnabledByDefault()).isFalse();

            assertThat(JsonFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE).isNotNull();
            assertThat(JsonFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE.getCategory())
                    .isEqualTo(JsonFeature.Category.DESERIALIZATION);

            assertThat(JsonFeature.UNWRAP_ROOT_VALUE).isNotNull();
            assertThat(JsonFeature.UNWRAP_ROOT_VALUE.getCategory())
                    .isEqualTo(JsonFeature.Category.DESERIALIZATION);
            assertThat(JsonFeature.UNWRAP_ROOT_VALUE.isEnabledByDefault()).isFalse();

            assertThat(JsonFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS).isNotNull();
            assertThat(JsonFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS.getCategory())
                    .isEqualTo(JsonFeature.Category.DESERIALIZATION);
            assertThat(JsonFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS.isEnabledByDefault()).isFalse();
        }

        @Test
        @DisplayName("新序列化特性是序列化特性")
        void testNewSerializationFeaturesAreSerializationFeatures() {
            assertThat(JsonFeature.WRAP_ROOT_VALUE.isSerializationFeature()).isTrue();
            assertThat(JsonFeature.WRITE_ENUMS_USING_INDEX.isSerializationFeature()).isTrue();
            assertThat(JsonFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS.isSerializationFeature()).isTrue();
        }

        @Test
        @DisplayName("新反序列化特性是反序列化特性")
        void testNewDeserializationFeaturesAreDeserializationFeatures() {
            assertThat(JsonFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL.isDeserializationFeature()).isTrue();
            assertThat(JsonFeature.UNWRAP_ROOT_VALUE.isDeserializationFeature()).isTrue();
            assertThat(JsonFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS.isDeserializationFeature()).isTrue();
        }

        @Test
        @DisplayName("在JsonConfig中启用新特性")
        void testEnableNewFeaturesInConfig() {
            JsonConfig config = JsonConfig.builder()
                    .enable(JsonFeature.WRAP_ROOT_VALUE)
                    .enable(JsonFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
                    .build();

            assertThat(config.isEnabled(JsonFeature.WRAP_ROOT_VALUE)).isTrue();
            assertThat(config.isEnabled(JsonFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)).isTrue();
            assertThat(config.isEnabled(JsonFeature.UNWRAP_ROOT_VALUE)).isFalse();
        }
    }

    // ==================== OpenJson Integration Tests ====================

    @Nested
    @DisplayName("OpenJson模块和混入集成 测试")
    class OpenJsonIntegrationTests {

        @Test
        @DisplayName("注册模块")
        void testRegisterModule() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);
            JsonModule.SimpleModule module = new JsonModule.SimpleModule("test", "1.0") {};

            OpenJson result = json.registerModule(module);

            assertThat(result).isSameAs(json);
            assertThat(json.getModules()).hasSize(1);
            assertThat(json.getModules().getFirst().getName()).isEqualTo("test");
        }

        @Test
        @DisplayName("添加混入")
        void testAddMixin() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);

            OpenJson result = json.addMixin(String.class, CharSequence.class);

            assertThat(result).isSameAs(json);
            assertThat(json.getMixinSource().hasMixin(String.class)).isTrue();
            assertThat(json.getMixinSource().getMixin(String.class)).isEqualTo(CharSequence.class);
        }

        @Test
        @DisplayName("设置属性过滤器")
        void testSetPropertyFilter() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);
            PropertyFilter filter = PropertyFilter.include("name");

            OpenJson result = json.setPropertyFilter("myFilter", filter);

            assertThat(result).isSameAs(json);
            assertThat(json.getPropertyFilter("myFilter")).isSameAs(filter);
        }

        @Test
        @DisplayName("获取不存在的过滤器返回null")
        void testGetNonExistentFilter() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);

            assertThat(json.getPropertyFilter("nonexistent")).isNull();
        }

        @Test
        @DisplayName("null模块抛出异常")
        void testNullModuleThrows() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);

            assertThatNullPointerException()
                    .isThrownBy(() -> json.registerModule(null));
        }

        @Test
        @DisplayName("null过滤器ID抛出异常")
        void testNullFilterIdThrows() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);

            assertThatNullPointerException()
                    .isThrownBy(() -> json.setPropertyFilter(null, PropertyFilter.includeAll()));
        }

        @Test
        @DisplayName("null过滤器抛出异常")
        void testNullFilterThrows() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);

            assertThatNullPointerException()
                    .isThrownBy(() -> json.setPropertyFilter("id", null));
        }
    }
}
