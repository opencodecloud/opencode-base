package cloud.opencode.base.string.template;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * PlaceholderTemplateTest Tests
 * PlaceholderTemplateTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("PlaceholderTemplate Tests")
class PlaceholderTemplateTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of should create template")
        void ofShouldCreateTemplate() {
            PlaceholderTemplate template = PlaceholderTemplate.of("Hello, {{name}}!", "{{", "}}");
            assertThat(template).isNotNull();
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with custom prefix and suffix")
        void shouldCreateWithCustomPrefixAndSuffix() {
            PlaceholderTemplate template = new PlaceholderTemplate("Hello, [name]!", "[", "]");
            assertThat(template.render(Map.of("name", "World"))).isEqualTo("Hello, World!");
        }
    }

    @Nested
    @DisplayName("render Tests")
    class RenderTests {

        @Test
        @DisplayName("Should render with double brace placeholders")
        void shouldRenderWithDoubleBracePlaceholders() {
            PlaceholderTemplate template = PlaceholderTemplate.of("Hello, {{name}}!", "{{", "}}");
            assertThat(template.render(Map.of("name", "World"))).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should render with multiple placeholders")
        void shouldRenderWithMultiplePlaceholders() {
            PlaceholderTemplate template = PlaceholderTemplate.of("{{greeting}}, {{name}}!", "{{", "}}");
            Map<String, Object> values = Map.of("greeting", "Hello", "name", "World");
            assertThat(template.render(values)).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should return null for null template")
        void shouldReturnNullForNullTemplate() {
            PlaceholderTemplate template = new PlaceholderTemplate(null, "{{", "}}");
            assertThat(template.render(Map.of("name", "World"))).isNull();
        }

        @Test
        @DisplayName("Should return original for null values")
        void shouldReturnOriginalForNullValues() {
            PlaceholderTemplate template = PlaceholderTemplate.of("Hello, {{name}}!", "{{", "}}");
            assertThat(template.render(null)).isEqualTo("Hello, {{name}}!");
        }

        @Test
        @DisplayName("Should return original for empty values")
        void shouldReturnOriginalForEmptyValues() {
            PlaceholderTemplate template = PlaceholderTemplate.of("Hello, {{name}}!", "{{", "}}");
            assertThat(template.render(Map.of())).isEqualTo("Hello, {{name}}!");
        }

        @Test
        @DisplayName("Should handle null value in map")
        void shouldHandleNullValueInMap() {
            PlaceholderTemplate template = PlaceholderTemplate.of("Hello, {{name}}!", "{{", "}}");
            Map<String, Object> values = new HashMap<>();
            values.put("name", null);
            assertThat(template.render(values)).isEqualTo("Hello, !");
        }

        @Test
        @DisplayName("Should render with percent placeholders")
        void shouldRenderWithPercentPlaceholders() {
            PlaceholderTemplate template = PlaceholderTemplate.of("Hello, %name%!", "%", "%");
            assertThat(template.render(Map.of("name", "World"))).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should render with hash placeholders")
        void shouldRenderWithHashPlaceholders() {
            PlaceholderTemplate template = PlaceholderTemplate.of("Hello, #name#!", "#", "#");
            assertThat(template.render(Map.of("name", "World"))).isEqualTo("Hello, World!");
        }
    }
}
