package cloud.opencode.base.string.template;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TemplateContextTest Tests
 * TemplateContextTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("TemplateContext Tests")
class TemplateContextTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create empty context")
        void shouldCreateEmptyContext() {
            TemplateContext context = new TemplateContext();
            assertThat(context.getVariables()).isEmpty();
        }

        @Test
        @DisplayName("Should create context with initial values")
        void shouldCreateContextWithInitialValues() {
            TemplateContext context = new TemplateContext(Map.of("name", "World"));
            assertThat(context.get("name")).isEqualTo("World");
        }
    }

    @Nested
    @DisplayName("get and set Tests")
    class GetSetTests {

        @Test
        @DisplayName("Should get set value")
        void shouldGetSetValue() {
            TemplateContext context = new TemplateContext();
            context.set("name", "World");
            assertThat(context.get("name")).isEqualTo("World");
        }

        @Test
        @DisplayName("Should return null for missing key")
        void shouldReturnNullForMissingKey() {
            TemplateContext context = new TemplateContext();
            assertThat(context.get("missing")).isNull();
        }
    }

    @Nested
    @DisplayName("getVariables Tests")
    class GetVariablesTests {

        @Test
        @DisplayName("Should return unmodifiable map")
        void shouldReturnUnmodifiableMap() {
            TemplateContext context = new TemplateContext(Map.of("name", "World"));
            Map<String, Object> variables = context.getVariables();

            assertThatThrownBy(() -> variables.put("key", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Default Filter Tests")
    class DefaultFilterTests {

        @Test
        @DisplayName("Should have upper filter")
        void shouldHaveUpperFilter() {
            TemplateContext context = new TemplateContext();
            TemplateFilter upper = context.getFilter("upper");
            assertThat(upper).isNotNull();
            assertThat(upper.apply("hello", new String[]{})).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("Should have lower filter")
        void shouldHaveLowerFilter() {
            TemplateContext context = new TemplateContext();
            TemplateFilter lower = context.getFilter("lower");
            assertThat(lower).isNotNull();
            assertThat(lower.apply("HELLO", new String[]{})).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should have truncate filter")
        void shouldHaveTruncateFilter() {
            TemplateContext context = new TemplateContext();
            TemplateFilter truncate = context.getFilter("truncate");
            assertThat(truncate).isNotNull();
            assertThat(truncate.apply("Hello World", new String[]{"5"})).isEqualTo("Hello...");
        }

        @Test
        @DisplayName("Should have default filter")
        void shouldHaveDefaultFilter() {
            TemplateContext context = new TemplateContext();
            TemplateFilter defaultFilter = context.getFilter("default");
            assertThat(defaultFilter).isNotNull();
            assertThat(defaultFilter.apply("", new String[]{"fallback"})).isEqualTo("fallback");
            assertThat(defaultFilter.apply("value", new String[]{"fallback"})).isEqualTo("value");
        }

        @Test
        @DisplayName("Upper filter should handle null")
        void upperFilterShouldHandleNull() {
            TemplateContext context = new TemplateContext();
            TemplateFilter upper = context.getFilter("upper");
            assertThat(upper.apply(null, new String[]{})).isEmpty();
        }

        @Test
        @DisplayName("Truncate filter should handle null")
        void truncateFilterShouldHandleNull() {
            TemplateContext context = new TemplateContext();
            TemplateFilter truncate = context.getFilter("truncate");
            assertThat(truncate.apply(null, new String[]{})).isEmpty();
        }

        @Test
        @DisplayName("Truncate filter should use default length")
        void truncateFilterShouldUseDefaultLength() {
            TemplateContext context = new TemplateContext();
            TemplateFilter truncate = context.getFilter("truncate");
            String shortText = "Short";
            assertThat(truncate.apply(shortText, new String[]{})).isEqualTo(shortText);
        }
    }

    @Nested
    @DisplayName("registerFilter Tests")
    class RegisterFilterTests {

        @Test
        @DisplayName("Should register custom filter")
        void shouldRegisterCustomFilter() {
            TemplateContext context = new TemplateContext();
            context.registerFilter("reverse", (v, args) -> new StringBuilder(v).reverse().toString());

            TemplateFilter reverse = context.getFilter("reverse");
            assertThat(reverse.apply("hello", new String[]{})).isEqualTo("olleh");
        }

        @Test
        @DisplayName("Should return null for missing filter")
        void shouldReturnNullForMissingFilter() {
            TemplateContext context = new TemplateContext();
            assertThat(context.getFilter("nonexistent")).isNull();
        }
    }
}
