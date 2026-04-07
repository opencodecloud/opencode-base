package cloud.opencode.base.json.spi;

import cloud.opencode.base.json.JsonConfig;
import cloud.opencode.base.json.adapter.JsonTypeAdapter;
import cloud.opencode.base.json.adapter.JsonTypeAdapterFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JsonModule")
class JsonModuleTest {

    private JsonModule.SimpleModule createModule(String name, String version) {
        return new JsonModule.SimpleModule(name, version) {};
    }

    @Nested
    @DisplayName("SimpleModule basics")
    class SimpleModuleBasicsTest {

        @Test
        @DisplayName("getName returns configured name")
        void getName() {
            var module = createModule("my-module", "1.0.0");
            assertThat(module.getName()).isEqualTo("my-module");
        }

        @Test
        @DisplayName("getVersion returns configured version")
        void getVersion() {
            var module = createModule("my-module", "2.5.1");
            assertThat(module.getVersion()).isEqualTo("2.5.1");
        }

        @Test
        @DisplayName("null name throws NullPointerException")
        void nullName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> createModule(null, "1.0"))
                    .withMessageContaining("name");
        }

        @Test
        @DisplayName("null version throws NullPointerException")
        void nullVersion() {
            assertThatNullPointerException()
                    .isThrownBy(() -> createModule("test", null))
                    .withMessageContaining("version");
        }

        @Test
        @DisplayName("toString contains name and version")
        void toStringContainsInfo() {
            var module = createModule("cool-module", "3.0.0");
            assertThat(module.toString())
                    .contains("cool-module")
                    .contains("3.0.0");
        }
    }

    @Nested
    @DisplayName("Adapter registration")
    class AdapterRegistrationTest {

        @Test
        @DisplayName("addAdapter registers adapter")
        void addAdapter() {
            var module = createModule("test", "1.0");
            JsonTypeAdapter<String> adapter = JsonTypeAdapter.ofString(
                    String.class, s -> s, s -> s);

            module.addAdapter(adapter);

            assertThat(module.getAdapters()).hasSize(1);
            assertThat(module.getAdapters().getFirst()).isSameAs(adapter);
        }

        @Test
        @DisplayName("addAdapter supports multiple adapters")
        void addMultipleAdapters() {
            var module = createModule("test", "1.0");
            JsonTypeAdapter<String> adapter1 = JsonTypeAdapter.ofString(
                    String.class, s -> s, s -> s);
            JsonTypeAdapter<Integer> adapter2 = JsonTypeAdapter.ofString(
                    Integer.class, String::valueOf, Integer::parseInt);

            module.addAdapter(adapter1).addAdapter(adapter2);

            assertThat(module.getAdapters()).hasSize(2);
        }

        @Test
        @DisplayName("addAdapter null throws NullPointerException")
        void addNullAdapter() {
            var module = createModule("test", "1.0");
            assertThatNullPointerException()
                    .isThrownBy(() -> module.addAdapter(null));
        }

        @Test
        @DisplayName("getAdapters returns unmodifiable list")
        void adaptersUnmodifiable() {
            var module = createModule("test", "1.0");
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> module.getAdapters().add(null));
        }

        @Test
        @DisplayName("addAdapter returns this for chaining")
        void chainingAdapter() {
            var module = createModule("test", "1.0");
            JsonTypeAdapter<String> adapter = JsonTypeAdapter.ofString(
                    String.class, s -> s, s -> s);

            JsonModule.SimpleModule result = module.addAdapter(adapter);

            assertThat(result).isSameAs(module);
        }
    }

    @Nested
    @DisplayName("Factory registration")
    class FactoryRegistrationTest {

        @Test
        @DisplayName("addFactory registers factory")
        void addFactory() {
            var module = createModule("test", "1.0");
            JsonTypeAdapterFactory factory = new JsonTypeAdapterFactory() {
                @Override
                public <T> JsonTypeAdapter<T> create(Class<T> type) { return null; }
            };

            module.addFactory(factory);

            assertThat(module.getFactories()).hasSize(1);
        }

        @Test
        @DisplayName("addFactory null throws NullPointerException")
        void addNullFactory() {
            var module = createModule("test", "1.0");
            assertThatNullPointerException()
                    .isThrownBy(() -> module.addFactory(null));
        }

        @Test
        @DisplayName("getFactories returns unmodifiable list")
        void factoriesUnmodifiable() {
            var module = createModule("test", "1.0");
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> module.getFactories().add(null));
        }

        @Test
        @DisplayName("addFactory returns this for chaining")
        void chainingFactory() {
            var module = createModule("test", "1.0");
            JsonTypeAdapterFactory factory = new JsonTypeAdapterFactory() {
                @Override
                public <T> JsonTypeAdapter<T> create(Class<T> type) { return null; }
            };

            assertThat(module.addFactory(factory)).isSameAs(module);
        }
    }

    @Nested
    @DisplayName("Mixin registration")
    class MixinRegistrationTest {

        @Test
        @DisplayName("addMixin registers target-to-mixin mapping")
        void addMixin() {
            var module = createModule("test", "1.0");

            module.addMixin(String.class, CharSequence.class);

            assertThat(module.getMixins()).hasSize(1);
            assertThat(module.getMixins().get(String.class)).isEqualTo(CharSequence.class);
        }

        @Test
        @DisplayName("addMixin overwrites previous mapping for same target")
        void overwriteMixin() {
            var module = createModule("test", "1.0");
            module.addMixin(String.class, CharSequence.class);
            module.addMixin(String.class, Comparable.class);

            assertThat(module.getMixins()).hasSize(1);
            assertThat(module.getMixins().get(String.class)).isEqualTo(Comparable.class);
        }

        @Test
        @DisplayName("addMixin null target throws NullPointerException")
        void nullTarget() {
            var module = createModule("test", "1.0");
            assertThatNullPointerException()
                    .isThrownBy(() -> module.addMixin(null, CharSequence.class));
        }

        @Test
        @DisplayName("addMixin null mixin throws NullPointerException")
        void nullMixin() {
            var module = createModule("test", "1.0");
            assertThatNullPointerException()
                    .isThrownBy(() -> module.addMixin(String.class, null));
        }

        @Test
        @DisplayName("getMixins returns unmodifiable map")
        void mixinsUnmodifiable() {
            var module = createModule("test", "1.0");
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> module.getMixins().put(String.class, Object.class));
        }

        @Test
        @DisplayName("addMixin returns this for chaining")
        void chainingMixin() {
            var module = createModule("test", "1.0");
            assertThat(module.addMixin(String.class, CharSequence.class)).isSameAs(module);
        }
    }

    @Nested
    @DisplayName("Key adapter registration")
    class KeyAdapterRegistrationTest {

        @Test
        @DisplayName("addKeyAdapter registers key adapter")
        void addKeyAdapter() {
            var module = createModule("test", "1.0");
            JsonTypeAdapter<String> adapter = JsonTypeAdapter.ofString(
                    String.class, s -> s, s -> s);

            module.addKeyAdapter(String.class, adapter);

            assertThat(module.getKeyAdapters()).hasSize(1);
            assertThat(module.getKeyAdapters().get(String.class)).isSameAs(adapter);
        }

        @Test
        @DisplayName("addKeyAdapter null type throws NullPointerException")
        void nullType() {
            var module = createModule("test", "1.0");
            JsonTypeAdapter<String> adapter = JsonTypeAdapter.ofString(
                    String.class, s -> s, s -> s);
            assertThatNullPointerException()
                    .isThrownBy(() -> module.addKeyAdapter(null, adapter));
        }

        @Test
        @DisplayName("addKeyAdapter null adapter throws NullPointerException")
        void nullAdapter() {
            var module = createModule("test", "1.0");
            assertThatNullPointerException()
                    .isThrownBy(() -> module.addKeyAdapter(String.class, null));
        }

        @Test
        @DisplayName("getKeyAdapters returns unmodifiable map")
        void keyAdaptersUnmodifiable() {
            var module = createModule("test", "1.0");
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> module.getKeyAdapters().put(String.class, null));
        }

        @Test
        @DisplayName("addKeyAdapter returns this for chaining")
        void chaining() {
            var module = createModule("test", "1.0");
            JsonTypeAdapter<String> adapter = JsonTypeAdapter.ofString(
                    String.class, s -> s, s -> s);
            assertThat(module.addKeyAdapter(String.class, adapter)).isSameAs(module);
        }
    }

    @Nested
    @DisplayName("setupModule")
    class SetupModuleTest {

        @Test
        @DisplayName("calls context methods for all registered components")
        void callsContextMethods() {
            var module = createModule("test", "1.0");
            JsonTypeAdapter<String> adapter = JsonTypeAdapter.ofString(
                    String.class, s -> s, s -> s);
            JsonTypeAdapterFactory factory = new JsonTypeAdapterFactory() {
                @Override
                public <T> JsonTypeAdapter<T> create(Class<T> type) { return null; }
            };
            JsonTypeAdapter<Integer> keyAdapter = JsonTypeAdapter.ofString(
                    Integer.class, String::valueOf, Integer::parseInt);

            module.addAdapter(adapter);
            module.addFactory(factory);
            module.addMixin(String.class, CharSequence.class);
            module.addKeyAdapter(Integer.class, keyAdapter);

            List<JsonTypeAdapter<?>> addedAdapters = new ArrayList<>();
            List<JsonTypeAdapterFactory> addedFactories = new ArrayList<>();
            Map<Class<?>, Class<?>> addedMixins = new HashMap<>();
            Map<Class<?>, JsonTypeAdapter<?>> addedKeyAdapters = new HashMap<>();

            module.setupModule(new JsonModule.SetupContext() {
                @Override
                public void addTypeAdapter(JsonTypeAdapter<?> a) {
                    addedAdapters.add(a);
                }

                @Override
                public void addTypeAdapterFactory(JsonTypeAdapterFactory f) {
                    addedFactories.add(f);
                }

                @Override
                public void addMixin(Class<?> target, Class<?> mixin) {
                    addedMixins.put(target, mixin);
                }

                @Override
                public void addKeyAdapter(Class<?> type, JsonTypeAdapter<?> a) {
                    addedKeyAdapters.put(type, a);
                }

                @Override
                public JsonConfig getConfig() {
                    return JsonConfig.DEFAULT;
                }
            });

            assertThat(addedAdapters).hasSize(1).containsExactly(adapter);
            assertThat(addedFactories).hasSize(1).containsExactly(factory);
            assertThat(addedMixins).hasSize(1).containsEntry(String.class, CharSequence.class);
            assertThat(addedKeyAdapters).hasSize(1).containsKey(Integer.class);
        }

        @Test
        @DisplayName("empty module calls nothing on context")
        void emptyModuleCallsNothing() {
            var module = createModule("empty", "1.0");
            boolean[] anyCalled = {false};

            module.setupModule(new JsonModule.SetupContext() {
                @Override
                public void addTypeAdapter(JsonTypeAdapter<?> a) { anyCalled[0] = true; }
                @Override
                public void addTypeAdapterFactory(JsonTypeAdapterFactory f) { anyCalled[0] = true; }
                @Override
                public void addMixin(Class<?> target, Class<?> mixin) { anyCalled[0] = true; }
                @Override
                public void addKeyAdapter(Class<?> type, JsonTypeAdapter<?> a) { anyCalled[0] = true; }
                @Override
                public JsonConfig getConfig() { return JsonConfig.DEFAULT; }
            });

            assertThat(anyCalled[0]).isFalse();
        }

        @Test
        @DisplayName("setupModule preserves order of adapters")
        void preservesAdapterOrder() {
            var module = createModule("test", "1.0");
            JsonTypeAdapter<String> adapter1 = JsonTypeAdapter.ofString(
                    String.class, s -> s, s -> s);
            JsonTypeAdapter<Integer> adapter2 = JsonTypeAdapter.ofString(
                    Integer.class, String::valueOf, Integer::parseInt);

            module.addAdapter(adapter1).addAdapter(adapter2);

            List<JsonTypeAdapter<?>> addedAdapters = new ArrayList<>();
            module.setupModule(new JsonModule.SetupContext() {
                @Override
                public void addTypeAdapter(JsonTypeAdapter<?> a) { addedAdapters.add(a); }
                @Override
                public void addTypeAdapterFactory(JsonTypeAdapterFactory f) {}
                @Override
                public void addMixin(Class<?> target, Class<?> mixin) {}
                @Override
                public void addKeyAdapter(Class<?> type, JsonTypeAdapter<?> a) {}
                @Override
                public JsonConfig getConfig() { return JsonConfig.DEFAULT; }
            });

            assertThat(addedAdapters).containsExactly(adapter1, adapter2);
        }
    }

    @Nested
    @DisplayName("Full chaining")
    class FullChainingTest {

        @Test
        @DisplayName("all add methods can be chained fluently")
        void fullChain() {
            JsonTypeAdapter<String> adapter = JsonTypeAdapter.ofString(
                    String.class, s -> s, s -> s);
            JsonTypeAdapterFactory factory = new JsonTypeAdapterFactory() {
                @Override
                public <T> JsonTypeAdapter<T> create(Class<T> type) { return null; }
            };
            JsonTypeAdapter<Integer> keyAdapter = JsonTypeAdapter.ofString(
                    Integer.class, String::valueOf, Integer::parseInt);

            var module = createModule("chain-test", "1.0")
                    .addAdapter(adapter)
                    .addFactory(factory)
                    .addMixin(String.class, CharSequence.class)
                    .addKeyAdapter(Integer.class, keyAdapter);

            assertThat(module.getAdapters()).hasSize(1);
            assertThat(module.getFactories()).hasSize(1);
            assertThat(module.getMixins()).hasSize(1);
            assertThat(module.getKeyAdapters()).hasSize(1);
        }
    }
}
