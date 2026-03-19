package cloud.opencode.base.string.template;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TemplateUtilTest Tests
 * TemplateUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("TemplateUtil Tests")
class TemplateUtilTest {

    @Nested
    @DisplayName("render Tests")
    class RenderTests {

        @Test
        @DisplayName("Should render template with values")
        void shouldRenderTemplateWithValues() {
            String result = TemplateUtil.render("Hello, ${name}!", Map.of("name", "World"));
            assertThat(result).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should render template with multiple values")
        void shouldRenderTemplateWithMultipleValues() {
            Map<String, Object> values = Map.of("greeting", "Hello", "name", "World");
            String result = TemplateUtil.render("${greeting}, ${name}!", values);
            assertThat(result).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should return null for null template")
        void shouldReturnNullForNullTemplate() {
            assertThat(TemplateUtil.render(null, Map.of("name", "World"))).isNull();
        }

        @Test
        @DisplayName("Should return original for null values")
        void shouldReturnOriginalForNullValues() {
            assertThat(TemplateUtil.render("Hello, ${name}!", null)).isEqualTo("Hello, ${name}!");
        }

        @Test
        @DisplayName("Should return original for empty values")
        void shouldReturnOriginalForEmptyValues() {
            assertThat(TemplateUtil.render("Hello, ${name}!", Map.of())).isEqualTo("Hello, ${name}!");
        }

        @Test
        @DisplayName("Should use default value when variable not provided")
        void shouldUseDefaultValueWhenVariableNotProvided() {
            // Default value is only used when a non-empty map is provided but specific variable is missing
            String result = TemplateUtil.render("Hello, ${name:Guest}!", Map.of("other", "value"));
            assertThat(result).isEqualTo("Hello, Guest!");
        }

        @Test
        @DisplayName("Should override default value when variable provided")
        void shouldOverrideDefaultValueWhenVariableProvided() {
            String result = TemplateUtil.render("Hello, ${name:Guest}!", Map.of("name", "World"));
            assertThat(result).isEqualTo("Hello, World!");
        }
    }

    @Nested
    @DisplayName("extractVariables Tests")
    class ExtractVariablesTests {

        @Test
        @DisplayName("Should extract variable names")
        void shouldExtractVariableNames() {
            Set<String> vars = TemplateUtil.extractVariables("Hello, ${name}! You are ${age} years old.");
            assertThat(vars).containsExactlyInAnyOrder("name", "age");
        }

        @Test
        @DisplayName("Should return empty set for null template")
        void shouldReturnEmptySetForNullTemplate() {
            assertThat(TemplateUtil.extractVariables(null)).isEmpty();
        }

        @Test
        @DisplayName("Should return empty set for template without variables")
        void shouldReturnEmptySetForTemplateWithoutVariables() {
            assertThat(TemplateUtil.extractVariables("Plain text")).isEmpty();
        }

        @Test
        @DisplayName("Should extract variable name without default value")
        void shouldExtractVariableNameWithoutDefaultValue() {
            Set<String> vars = TemplateUtil.extractVariables("Hello, ${name:Guest}!");
            assertThat(vars).containsExactly("name");
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = TemplateUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
