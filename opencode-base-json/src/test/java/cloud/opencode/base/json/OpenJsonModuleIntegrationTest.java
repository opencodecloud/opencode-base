package cloud.opencode.base.json;

import cloud.opencode.base.json.adapter.JsonTypeAdapter;
import cloud.opencode.base.json.adapter.JsonTypeAdapterFactory;
import cloud.opencode.base.json.spi.JsonModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OpenJson Module Integration")
class OpenJsonModuleIntegrationTest {

    private JsonModule.SimpleModule createTestModule(String name, String version) {
        return new JsonModule.SimpleModule(name, version) {};
    }

    @Nested
    @DisplayName("registerModule")
    class RegisterModuleTest {

        @Test
        @DisplayName("returns same OpenJson instance for fluent chaining")
        void fluentChaining() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);
            var module = createTestModule("test", "1.0");

            OpenJson result = json.registerModule(module);

            assertThat(result).isSameAs(json);
        }

        @Test
        @DisplayName("registered module appears in getModules")
        void appearsInGetModules() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);
            var module = createTestModule("test-mod", "2.0");

            json.registerModule(module);

            assertThat(json.getModules()).hasSize(1);
            assertThat(json.getModules().getFirst().getName()).isEqualTo("test-mod");
            assertThat(json.getModules().getFirst().getVersion()).isEqualTo("2.0");
        }

        @Test
        @DisplayName("multiple modules can be registered")
        void multipleModules() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);

            json.registerModule(createTestModule("mod1", "1.0"))
                .registerModule(createTestModule("mod2", "2.0"))
                .registerModule(createTestModule("mod3", "3.0"));

            assertThat(json.getModules()).hasSize(3);
        }

        @Test
        @DisplayName("null module throws NullPointerException")
        void nullModule() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);

            assertThatNullPointerException()
                    .isThrownBy(() -> json.registerModule(null));
        }

        @Test
        @DisplayName("getModules returns unmodifiable list")
        void getModulesUnmodifiable() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> json.getModules().add(null));
        }

        @Test
        @DisplayName("module mixins are registered in MixinSource via setupModule")
        void moduleMixinsRegistered() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);
            var module = createTestModule("mixin-mod", "1.0");
            module.addMixin(String.class, CharSequence.class);

            json.registerModule(module);

            assertThat(json.getMixinSource().hasMixin(String.class)).isTrue();
            assertThat(json.getMixinSource().getMixin(String.class)).isEqualTo(CharSequence.class);
        }

        @Test
        @DisplayName("setupModule is called immediately on registration")
        void setupCalledImmediately() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);
            boolean[] setupCalled = {false};

            JsonModule immediateModule = new JsonModule() {
                @Override
                public String getName() { return "immediate"; }
                @Override
                public String getVersion() { return "1.0"; }
                @Override
                public void setupModule(SetupContext context) {
                    setupCalled[0] = true;
                    assertThat(context.getConfig()).isNotNull();
                }
            };

            json.registerModule(immediateModule);

            assertThat(setupCalled[0]).isTrue();
        }
    }

    @Nested
    @DisplayName("addMixin")
    class AddMixinTest {

        @Test
        @DisplayName("registers mixin in MixinSource")
        void registersInMixinSource() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);

            json.addMixin(Integer.class, Number.class);

            MixinSource source = json.getMixinSource();
            assertThat(source.hasMixin(Integer.class)).isTrue();
            assertThat(source.getMixin(Integer.class)).isEqualTo(Number.class);
        }

        @Test
        @DisplayName("returns same OpenJson instance for fluent chaining")
        void fluentChaining() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);

            OpenJson result = json.addMixin(String.class, CharSequence.class);

            assertThat(result).isSameAs(json);
        }

        @Test
        @DisplayName("multiple addMixin calls accumulate")
        void multipleAdds() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);

            json.addMixin(String.class, CharSequence.class)
                .addMixin(Integer.class, Number.class);

            assertThat(json.getMixinSource().size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("setPropertyFilter and getPropertyFilter")
    class PropertyFilterTest {

        @Test
        @DisplayName("round-trip set and get")
        void roundTrip() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);
            PropertyFilter filter = PropertyFilter.include("name");

            json.setPropertyFilter("myFilter", filter);

            assertThat(json.getPropertyFilter("myFilter")).isSameAs(filter);
        }

        @Test
        @DisplayName("returns same OpenJson instance for fluent chaining")
        void fluentChaining() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);

            OpenJson result = json.setPropertyFilter("f", PropertyFilter.includeAll());

            assertThat(result).isSameAs(json);
        }

        @Test
        @DisplayName("getPropertyFilter returns null for unknown id")
        void unknownFilterReturnsNull() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);

            assertThat(json.getPropertyFilter("nonexistent")).isNull();
        }

        @Test
        @DisplayName("multiple filters with different ids")
        void multipleFilters() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);
            PropertyFilter f1 = PropertyFilter.include("name");
            PropertyFilter f2 = PropertyFilter.exclude("password");
            PropertyFilter f3 = PropertyFilter.includeNonNull();

            json.setPropertyFilter("include-name", f1)
                .setPropertyFilter("exclude-password", f2)
                .setPropertyFilter("non-null", f3);

            assertThat(json.getPropertyFilter("include-name")).isSameAs(f1);
            assertThat(json.getPropertyFilter("exclude-password")).isSameAs(f2);
            assertThat(json.getPropertyFilter("non-null")).isSameAs(f3);
        }

        @Test
        @DisplayName("overwrite filter with same id")
        void overwriteFilter() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);
            PropertyFilter original = PropertyFilter.includeAll();
            PropertyFilter replacement = PropertyFilter.excludeAll();

            json.setPropertyFilter("myFilter", original);
            json.setPropertyFilter("myFilter", replacement);

            assertThat(json.getPropertyFilter("myFilter")).isSameAs(replacement);
        }

        @Test
        @DisplayName("null filter id throws NullPointerException")
        void nullFilterId() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);

            assertThatNullPointerException()
                    .isThrownBy(() -> json.setPropertyFilter(null, PropertyFilter.includeAll()));
        }

        @Test
        @DisplayName("null filter throws NullPointerException")
        void nullFilter() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);

            assertThatNullPointerException()
                    .isThrownBy(() -> json.setPropertyFilter("id", null));
        }
    }

    @Nested
    @DisplayName("getMixinSource")
    class GetMixinSourceTest {

        @Test
        @DisplayName("returns non-null MixinSource")
        void returnsNonNull() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);
            assertThat(json.getMixinSource()).isNotNull();
        }

        @Test
        @DisplayName("returns same MixinSource instance on repeated calls")
        void sameInstance() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);
            MixinSource source1 = json.getMixinSource();
            MixinSource source2 = json.getMixinSource();
            assertThat(source1).isSameAs(source2);
        }

        @Test
        @DisplayName("MixinSource reflects addMixin calls")
        void reflectsAddMixin() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);
            json.addMixin(String.class, CharSequence.class);

            MixinSource source = json.getMixinSource();

            assertThat(source.hasMixin(String.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Combined module and direct registration")
    class CombinedTest {

        @Test
        @DisplayName("module mixin and direct addMixin both appear in MixinSource")
        void modulePlusDirect() {
            OpenJson json = OpenJson.withConfig(JsonConfig.DEFAULT);
            var module = createTestModule("mod", "1.0");
            module.addMixin(String.class, CharSequence.class);

            json.registerModule(module);
            json.addMixin(Integer.class, Number.class);

            MixinSource source = json.getMixinSource();
            assertThat(source.hasMixin(String.class)).isTrue();
            assertThat(source.hasMixin(Integer.class)).isTrue();
            assertThat(source.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("each OpenJson instance has independent state")
        void independentInstances() {
            OpenJson json1 = OpenJson.withConfig(JsonConfig.DEFAULT);
            OpenJson json2 = OpenJson.withConfig(JsonConfig.DEFAULT);

            json1.addMixin(String.class, CharSequence.class);
            json1.setPropertyFilter("f", PropertyFilter.includeAll());

            assertThat(json2.getMixinSource().size()).isZero();
            assertThat(json2.getPropertyFilter("f")).isNull();
            assertThat(json2.getModules()).isEmpty();
        }
    }
}
