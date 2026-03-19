package cloud.opencode.base.string.template;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ContextBuilderTest Tests
 * ContextBuilderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("ContextBuilder Tests")
class ContextBuilderTest {

    @Nested
    @DisplayName("create Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create builder")
        void shouldCreateBuilder() {
            ContextBuilder builder = ContextBuilder.create();
            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("set Tests")
    class SetTests {

        @Test
        @DisplayName("Should set single value")
        void shouldSetSingleValue() {
            TemplateContext context = ContextBuilder.create()
                .set("name", "World")
                .build();

            assertThat(context.get("name")).isEqualTo("World");
        }

        @Test
        @DisplayName("Should chain multiple sets")
        void shouldChainMultipleSets() {
            TemplateContext context = ContextBuilder.create()
                .set("greeting", "Hello")
                .set("name", "World")
                .build();

            assertThat(context.get("greeting")).isEqualTo("Hello");
            assertThat(context.get("name")).isEqualTo("World");
        }

        @Test
        @DisplayName("Should return builder for chaining")
        void shouldReturnBuilderForChaining() {
            ContextBuilder builder = ContextBuilder.create();
            ContextBuilder result = builder.set("key", "value");
            assertThat(result).isSameAs(builder);
        }
    }

    @Nested
    @DisplayName("setAll Tests")
    class SetAllTests {

        @Test
        @DisplayName("Should set all values from map")
        void shouldSetAllValuesFromMap() {
            Map<String, Object> values = Map.of("a", 1, "b", 2);
            TemplateContext context = ContextBuilder.create()
                .setAll(values)
                .build();

            assertThat(context.get("a")).isEqualTo(1);
            assertThat(context.get("b")).isEqualTo(2);
        }

        @Test
        @DisplayName("Should merge with existing values")
        void shouldMergeWithExistingValues() {
            TemplateContext context = ContextBuilder.create()
                .set("existing", "value")
                .setAll(Map.of("new", "added"))
                .build();

            assertThat(context.get("existing")).isEqualTo("value");
            assertThat(context.get("new")).isEqualTo("added");
        }
    }

    @Nested
    @DisplayName("build Tests")
    class BuildTests {

        @Test
        @DisplayName("Should build TemplateContext")
        void shouldBuildTemplateContext() {
            TemplateContext context = ContextBuilder.create()
                .set("name", "World")
                .build();

            assertThat(context).isInstanceOf(TemplateContext.class);
        }

        @Test
        @DisplayName("Built context should have default filters")
        void builtContextShouldHaveDefaultFilters() {
            TemplateContext context = ContextBuilder.create().build();
            assertThat(context.getFilter("upper")).isNotNull();
        }
    }
}
