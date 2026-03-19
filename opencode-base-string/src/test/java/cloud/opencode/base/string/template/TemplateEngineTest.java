package cloud.opencode.base.string.template;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TemplateEngineTest Tests
 * TemplateEngineTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("TemplateEngine Tests")
class TemplateEngineTest {

    @Nested
    @DisplayName("create Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create engine instance")
        void shouldCreateEngineInstance() {
            TemplateEngine engine = TemplateEngine.create();
            assertThat(engine).isNotNull();
        }
    }

    @Nested
    @DisplayName("render with Map Tests")
    class RenderWithMapTests {

        @Test
        @DisplayName("Should render simple variable")
        void shouldRenderSimpleVariable() {
            TemplateEngine engine = TemplateEngine.create();
            String result = engine.render("Hello ${name}!", Map.of("name", "World"));
            assertThat(result).isEqualTo("Hello World!");
        }

        @Test
        @DisplayName("Should render multiple variables")
        void shouldRenderMultipleVariables() {
            TemplateEngine engine = TemplateEngine.create();
            String result = engine.render("${greeting} ${name}!", Map.of("greeting", "Hi", "name", "John"));
            assertThat(result).isEqualTo("Hi John!");
        }

        @Test
        @DisplayName("Should return null for null template")
        void shouldReturnNullForNullTemplate() {
            TemplateEngine engine = TemplateEngine.create();
            assertThat(engine.render(null, Map.of("name", "World"))).isNull();
        }

        @Test
        @DisplayName("Should return original for empty template")
        void shouldReturnOriginalForEmptyTemplate() {
            TemplateEngine engine = TemplateEngine.create();
            assertThat(engine.render("", Map.of("name", "World"))).isEmpty();
        }

        @Test
        @DisplayName("Should return original for null context")
        void shouldReturnOriginalForNullContext() {
            TemplateEngine engine = TemplateEngine.create();
            Map<String, Object> nullContext = null;
            assertThat(engine.render("Hello ${name}!", nullContext)).isEqualTo("Hello ${name}!");
        }

        @Test
        @DisplayName("Should return original for empty context")
        void shouldReturnOriginalForEmptyContext() {
            TemplateEngine engine = TemplateEngine.create();
            assertThat(engine.render("Hello ${name}!", Map.of())).isEqualTo("Hello ${name}!");
        }

        @Test
        @DisplayName("Should render empty string for missing variable")
        void shouldRenderEmptyStringForMissingVariable() {
            TemplateEngine engine = TemplateEngine.create();
            String result = engine.render("Hello ${name}!", Map.of("other", "value"));
            assertThat(result).isEqualTo("Hello !");
        }

        @Test
        @DisplayName("Should handle unclosed variable")
        void shouldHandleUnclosedVariable() {
            TemplateEngine engine = TemplateEngine.create();
            String result = engine.render("Hello ${name", Map.of("name", "World"));
            assertThat(result).isEqualTo("Hello ${name");
        }
    }

    @Nested
    @DisplayName("render with TemplateContext Tests")
    class RenderWithContextTests {

        @Test
        @DisplayName("Should render with TemplateContext")
        void shouldRenderWithTemplateContext() {
            TemplateEngine engine = TemplateEngine.create();
            TemplateContext context = new TemplateContext(Map.of("name", "World"));
            String result = engine.render("Hello ${name}!", context);
            assertThat(result).isEqualTo("Hello World!");
        }

        @Test
        @DisplayName("Should handle null TemplateContext")
        void shouldHandleNullTemplateContext() {
            TemplateEngine engine = TemplateEngine.create();
            TemplateContext nullContext = null;
            String result = engine.render("Hello ${name}!", nullContext);
            assertThat(result).isEqualTo("Hello ${name}!");
        }
    }

    @Nested
    @DisplayName("registerFunction Tests")
    class RegisterFunctionTests {

        @Test
        @DisplayName("Should register and use custom function")
        void shouldRegisterAndUseCustomFunction() {
            TemplateEngine engine = TemplateEngine.create()
                .registerFunction("upper", args -> args[0].toString().toUpperCase());

            String result = engine.render("${upper(name)}", Map.of("name", "world"));
            assertThat(result).isEqualTo("WORLD");
        }

        @Test
        @DisplayName("Should ignore null function name")
        void shouldIgnoreNullFunctionName() {
            TemplateEngine engine = TemplateEngine.create()
                .registerFunction(null, args -> "test");

            assertThat(engine).isNotNull();
        }

        @Test
        @DisplayName("Should ignore null function")
        void shouldIgnoreNullFunction() {
            TemplateEngine engine = TemplateEngine.create()
                .registerFunction("test", null);

            assertThat(engine).isNotNull();
        }

        @Test
        @DisplayName("Should return this for chaining")
        void shouldReturnThisForChaining() {
            TemplateEngine engine = TemplateEngine.create();
            TemplateEngine result = engine.registerFunction("test", args -> "test");
            assertThat(result).isSameAs(engine);
        }
    }

    @Nested
    @DisplayName("variablePrefix Tests")
    class VariablePrefixTests {

        @Test
        @DisplayName("Should use custom variable prefix")
        void shouldUseCustomVariablePrefix() {
            TemplateEngine engine = TemplateEngine.create()
                .variablePrefix("{{")
                .variableSuffix("}}");

            String result = engine.render("Hello {{name}}!", Map.of("name", "World"));
            assertThat(result).isEqualTo("Hello World!");
        }

        @Test
        @DisplayName("Should ignore null prefix")
        void shouldIgnoreNullPrefix() {
            TemplateEngine engine = TemplateEngine.create()
                .variablePrefix(null);

            String result = engine.render("Hello ${name}!", Map.of("name", "World"));
            assertThat(result).isEqualTo("Hello World!");
        }

        @Test
        @DisplayName("Should ignore empty prefix")
        void shouldIgnoreEmptyPrefix() {
            TemplateEngine engine = TemplateEngine.create()
                .variablePrefix("");

            String result = engine.render("Hello ${name}!", Map.of("name", "World"));
            assertThat(result).isEqualTo("Hello World!");
        }
    }

    @Nested
    @DisplayName("variableSuffix Tests")
    class VariableSuffixTests {

        @Test
        @DisplayName("Should use custom variable suffix")
        void shouldUseCustomVariableSuffix() {
            TemplateEngine engine = TemplateEngine.create()
                .variablePrefix("%")
                .variableSuffix("%");

            String result = engine.render("Hello %name%!", Map.of("name", "World"));
            assertThat(result).isEqualTo("Hello World!");
        }

        @Test
        @DisplayName("Should ignore null suffix")
        void shouldIgnoreNullSuffix() {
            TemplateEngine engine = TemplateEngine.create()
                .variableSuffix(null);

            String result = engine.render("Hello ${name}!", Map.of("name", "World"));
            assertThat(result).isEqualTo("Hello World!");
        }
    }
}
