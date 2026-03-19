package cloud.opencode.base.string;

import cloud.opencode.base.string.template.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenTemplateTest Tests
 * OpenTemplateTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenTemplate Tests")
class OpenTemplateTest {

    @BeforeEach
    void setUp() {
        OpenTemplate.clearCache();
    }

    @Nested
    @DisplayName("render Tests")
    class RenderTests {

        @Test
        @DisplayName("Should render template with values")
        void shouldRenderTemplateWithValues() {
            Map<String, Object> values = Map.of("name", "World");
            String result = OpenTemplate.render("Hello, ${name}!", values);
            assertThat(result).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should render with default value")
        void shouldRenderWithDefaultValue() {
            Map<String, Object> values = Map.of();
            String result = OpenTemplate.render("Hello, ${name}!", values, "default");
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("format Tests")
    class FormatTests {

        @Test
        @DisplayName("Should format with map")
        void shouldFormatWithMap() {
            Map<String, Object> values = Map.of("name", "World");
            String result = OpenTemplate.format("Hello, ${name}!", values);
            assertThat(result).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should format with varargs")
        void shouldFormatWithVarargs() {
            String result = OpenTemplate.format("Hello, {0}!", "World");
            assertThat(result).isEqualTo("Hello, World!");
        }
    }

    @Nested
    @DisplayName("Template Object Tests")
    class TemplateObjectTests {

        @Test
        @DisplayName("of should create StringTemplate")
        void ofShouldCreateStringTemplate() {
            StringTemplate template = OpenTemplate.of("Hello, ${name}!");
            assertThat(template).isNotNull();
        }

        @Test
        @DisplayName("placeholder should create PlaceholderTemplate")
        void placeholderShouldCreatePlaceholderTemplate() {
            PlaceholderTemplate template = OpenTemplate.placeholder("Hello, {{name}}!", "{{", "}}");
            assertThat(template).isNotNull();
        }

        @Test
        @DisplayName("compile should create Template")
        void compileShouldCreateTemplate() {
            Template template = OpenTemplate.compile("Hello, ${name}!");
            assertThat(template).isNotNull();
        }
    }

    @Nested
    @DisplayName("renderInline Tests")
    class RenderInlineTests {

        @Test
        @DisplayName("Should render inline template")
        void shouldRenderInlineTemplate() {
            Map<String, Object> context = Map.of("name", "World");
            String result = OpenTemplate.renderInline("Hello, ${name}!", context);
            assertThat(result).isEqualTo("Hello, World!");
        }
    }

    @Nested
    @DisplayName("Filter Tests")
    class FilterTests {

        @Test
        @DisplayName("Should register and get filter")
        void shouldRegisterAndGetFilter() {
            TemplateFilter filter = (v, args) -> v != null ? v.toUpperCase() : "";
            OpenTemplate.registerFilter("myUpper", filter);
            TemplateFilter retrieved = OpenTemplate.getFilter("myUpper");
            assertThat(retrieved).isEqualTo(filter);
        }

        @Test
        @DisplayName("Should have default filters")
        void shouldHaveDefaultFilters() {
            assertThat(OpenTemplate.getFilter("upper")).isNotNull();
            assertThat(OpenTemplate.getFilter("lower")).isNotNull();
            assertThat(OpenTemplate.getFilter("capitalize")).isNotNull();
            assertThat(OpenTemplate.getFilter("truncate")).isNotNull();
            assertThat(OpenTemplate.getFilter("default")).isNotNull();
            assertThat(OpenTemplate.getFilter("escape")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Template Cache Tests")
    class TemplateCacheTests {

        @Test
        @DisplayName("Should register and render named template")
        void shouldRegisterAndRenderNamedTemplate() {
            OpenTemplate.register("greeting", "Hello, ${name}!");
            Map<String, Object> context = Map.of("name", "World");
            String result = OpenTemplate.renderNamed("greeting", context);
            assertThat(result).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should throw for unknown template")
        void shouldThrowForUnknownTemplate() {
            assertThatThrownBy(() -> OpenTemplate.renderNamed("unknown", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Template not found");
        }

        @Test
        @DisplayName("Should clear cache")
        void shouldClearCache() {
            OpenTemplate.register("test", "Test ${value}");
            OpenTemplate.clearCache();
            assertThatThrownBy(() -> OpenTemplate.renderNamed("test", Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenTemplate.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
