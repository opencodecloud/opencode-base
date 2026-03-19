package cloud.opencode.base.string.template;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * StringTemplateTest Tests
 * StringTemplateTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("StringTemplate Tests")
class StringTemplateTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of should create template")
        void ofShouldCreateTemplate() {
            StringTemplate template = StringTemplate.of("Hello, ${name}!");
            assertThat(template).isNotNull();
        }
    }

    @Nested
    @DisplayName("set Tests")
    class SetTests {

        @Test
        @DisplayName("Should set single value")
        void shouldSetSingleValue() {
            StringTemplate template = StringTemplate.of("Hello, ${name}!")
                .set("name", "World");
            assertThat(template.render()).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should chain multiple sets")
        void shouldChainMultipleSets() {
            StringTemplate template = StringTemplate.of("${greeting}, ${name}!")
                .set("greeting", "Hello")
                .set("name", "World");
            assertThat(template.render()).isEqualTo("Hello, World!");
        }
    }

    @Nested
    @DisplayName("setAll Tests")
    class SetAllTests {

        @Test
        @DisplayName("Should set all values from map")
        void shouldSetAllValuesFromMap() {
            Map<String, Object> values = Map.of("name", "World", "greeting", "Hello");
            StringTemplate template = StringTemplate.of("${greeting}, ${name}!")
                .setAll(values);
            assertThat(template.render()).isEqualTo("Hello, World!");
        }
    }

    @Nested
    @DisplayName("render Tests")
    class RenderTests {

        @Test
        @DisplayName("Should render without additional values")
        void shouldRenderWithoutAdditionalValues() {
            StringTemplate template = StringTemplate.of("Hello, ${name}!")
                .set("name", "World");
            assertThat(template.render()).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should render with additional values")
        void shouldRenderWithAdditionalValues() {
            StringTemplate template = StringTemplate.of("${greeting}, ${name}!")
                .set("greeting", "Hello");
            assertThat(template.render(Map.of("name", "World"))).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Additional values should override set values")
        void additionalValuesShouldOverrideSetValues() {
            StringTemplate template = StringTemplate.of("Hello, ${name}!")
                .set("name", "World");
            assertThat(template.render(Map.of("name", "Java"))).isEqualTo("Hello, Java!");
        }
    }

    @Nested
    @DisplayName("getVariables Tests")
    class GetVariablesTests {

        @Test
        @DisplayName("Should return all variable names")
        void shouldReturnAllVariableNames() {
            StringTemplate template = StringTemplate.of("${a} and ${b} and ${c}");
            assertThat(template.getVariables()).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("Should return empty set for no variables")
        void shouldReturnEmptySetForNoVariables() {
            StringTemplate template = StringTemplate.of("Plain text");
            assertThat(template.getVariables()).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasVariable Tests")
    class HasVariableTests {

        @Test
        @DisplayName("Should return true for existing variable")
        void shouldReturnTrueForExistingVariable() {
            StringTemplate template = StringTemplate.of("Hello, ${name}!");
            assertThat(template.hasVariable("name")).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-existing variable")
        void shouldReturnFalseForNonExistingVariable() {
            StringTemplate template = StringTemplate.of("Hello, ${name}!");
            assertThat(template.hasVariable("other")).isFalse();
        }
    }

    @Nested
    @DisplayName("defaultValue Tests")
    class DefaultValueTests {

        @Test
        @DisplayName("Should set default value")
        void shouldSetDefaultValue() {
            StringTemplate template = StringTemplate.of("Hello, ${name}!")
                .defaultValue("Unknown");
            assertThat(template).isNotNull();
        }
    }

    @Nested
    @DisplayName("strict Tests")
    class StrictTests {

        @Test
        @DisplayName("Should set strict mode")
        void shouldSetStrictMode() {
            StringTemplate template = StringTemplate.of("Hello, ${name}!")
                .strict(true);
            assertThat(template).isNotNull();
        }
    }
}
