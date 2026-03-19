package cloud.opencode.base.string.template;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TemplateTest Tests
 * TemplateTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("Template Tests")
class TemplateTest {

    @Nested
    @DisplayName("compile Tests")
    class CompileTests {

        @Test
        @DisplayName("Should compile template")
        void shouldCompileTemplate() {
            Template template = Template.compile("Hello, ${name}!");
            assertThat(template).isNotNull();
        }

        @Test
        @DisplayName("Should compile template with multiple variables")
        void shouldCompileTemplateWithMultipleVariables() {
            Template template = Template.compile("${greeting}, ${name}!");
            assertThat(template).isNotNull();
        }
    }

    @Nested
    @DisplayName("render with Map Tests")
    class RenderWithMapTests {

        @Test
        @DisplayName("Should render template with values")
        void shouldRenderTemplateWithValues() {
            Template template = Template.compile("Hello, ${name}!");
            String result = template.render(Map.of("name", "World"));
            assertThat(result).contains("World");
        }

        @Test
        @DisplayName("Should render template with default value")
        void shouldRenderTemplateWithDefaultValue() {
            Template template = Template.compile("Hello, ${name:Guest}!");
            String result = template.render(Map.of());
            assertThat(result).contains("Guest");
        }
    }

    @Nested
    @DisplayName("render with TemplateContext Tests")
    class RenderWithContextTests {

        @Test
        @DisplayName("Should render template with context")
        void shouldRenderTemplateWithContext() {
            Template template = Template.compile("Hello, ${name}!");
            TemplateContext context = new TemplateContext(Map.of("name", "World"));
            String result = template.render(context);
            assertThat(result).contains("World");
        }
    }

    @Nested
    @DisplayName("getSource Tests")
    class GetSourceTests {

        @Test
        @DisplayName("Should return source template")
        void shouldReturnSourceTemplate() {
            String source = "Hello, ${name}!";
            Template template = Template.compile(source);
            assertThat(template.getSource()).isEqualTo(source);
        }
    }
}
