package cloud.opencode.base.sms.template;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;

/**
 * TemplateParserTest Tests
 * TemplateParserTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("TemplateParser 测试")
class TemplateParserTest {

    @Nested
    @DisplayName("create方法测试")
    class CreateTests {

        @Test
        @DisplayName("创建默认解析器")
        void testCreate() {
            TemplateParser parser = TemplateParser.create();

            assertThat(parser).isNotNull();
        }
    }

    @Nested
    @DisplayName("extractVariables方法测试")
    class ExtractVariablesTests {

        @Test
        @DisplayName("提取${var}格式变量")
        void testExtractDollarBraceVariables() {
            TemplateParser parser = TemplateParser.create();

            List<String> vars = parser.extractVariables("Hello ${name}, code: ${code}");

            assertThat(vars).containsExactlyInAnyOrder("name", "code");
        }

        @Test
        @DisplayName("提取#{var}格式变量")
        void testExtractHashVariables() {
            TemplateParser parser = TemplateParser.hashPattern();

            List<String> vars = parser.extractVariables("Hello #{name}, code: #{code}");

            assertThat(vars).containsExactlyInAnyOrder("name", "code");
        }

        @Test
        @DisplayName("提取{{var}}格式变量")
        void testExtractDoubleBraceVariables() {
            TemplateParser parser = TemplateParser.bracePattern();

            List<String> vars = parser.extractVariables("Hello {{name}}, code: {{code}}");

            assertThat(vars).containsExactlyInAnyOrder("name", "code");
        }

        @Test
        @DisplayName("无变量返回空列表")
        void testExtractNoVariables() {
            TemplateParser parser = TemplateParser.create();

            List<String> vars = parser.extractVariables("Static content without variables");

            assertThat(vars).isEmpty();
        }

        @Test
        @DisplayName("重复变量只返回一次")
        void testExtractDuplicateVariables() {
            TemplateParser parser = TemplateParser.create();

            List<String> vars = parser.extractVariables("${code} and ${code} again");

            assertThat(vars).containsExactly("code");
        }

        @Test
        @DisplayName("null模板返回空列表")
        void testExtractNullTemplate() {
            TemplateParser parser = TemplateParser.create();

            List<String> vars = parser.extractVariables(null);

            assertThat(vars).isEmpty();
        }
    }

    @Nested
    @DisplayName("render方法测试")
    class RenderTests {

        @Test
        @DisplayName("渲染${var}格式")
        void testRenderDollarBrace() {
            TemplateParser parser = TemplateParser.create();

            String result = parser.render("Hello ${name}", Map.of("name", "World"));

            assertThat(result).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("渲染#{var}格式")
        void testRenderHash() {
            TemplateParser parser = TemplateParser.hashPattern();

            String result = parser.render("Hello #{name}", Map.of("name", "World"));

            assertThat(result).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("渲染{{var}}格式")
        void testRenderDoubleBrace() {
            TemplateParser parser = TemplateParser.bracePattern();

            String result = parser.render("Hello {{name}}", Map.of("name", "World"));

            assertThat(result).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("渲染多个变量")
        void testRenderMultipleVariables() {
            TemplateParser parser = TemplateParser.create();

            String result = parser.render("${greeting} ${name}!", Map.of("greeting", "Hello", "name", "World"));

            assertThat(result).isEqualTo("Hello World!");
        }

        @Test
        @DisplayName("null模板返回null")
        void testRenderNullTemplate() {
            TemplateParser parser = TemplateParser.create();

            String result = parser.render(null, Map.of("name", "World"));

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("validate方法测试")
    class ValidateTests {

        @Test
        @DisplayName("有效模板验证通过")
        void testValidateValid() {
            TemplateParser parser = TemplateParser.create();

            TemplateParser.ValidationResult result = parser.validate("Hello ${name}", Map.of("name", "World"));

            assertThat(result.valid()).isTrue();
        }

        @Test
        @DisplayName("返回必需的变量")
        void testValidateReturnsRequiredVariables() {
            TemplateParser parser = TemplateParser.create();

            TemplateParser.ValidationResult result = parser.validate("${a} ${b} ${c}", Map.of("a", "1", "b", "2", "c", "3"));

            assertThat(result.requiredVariables()).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("返回缺失的变量")
        void testValidateReturnsMissingVariables() {
            TemplateParser parser = TemplateParser.create();

            TemplateParser.ValidationResult result = parser.validate("${a} ${b} ${c}", Map.of("a", "1"));

            assertThat(result.valid()).isFalse();
            assertThat(result.missingVariables()).containsExactlyInAnyOrder("b", "c");
        }

        @Test
        @DisplayName("返回多余的变量")
        void testValidateReturnsExtraVariables() {
            TemplateParser parser = TemplateParser.create();

            TemplateParser.ValidationResult result = parser.validate("${a}", Map.of("a", "1", "b", "2", "c", "3"));

            assertThat(result.valid()).isTrue();
            assertThat(result.extraVariables()).containsExactlyInAnyOrder("b", "c");
        }

        @Test
        @DisplayName("hasMissing返回是否有缺失变量")
        void testValidationResultHasMissing() {
            TemplateParser parser = TemplateParser.create();

            TemplateParser.ValidationResult result = parser.validate("${a} ${b}", Map.of("a", "1"));

            assertThat(result.hasMissing()).isTrue();
        }

        @Test
        @DisplayName("hasExtra返回是否有多余变量")
        void testValidationResultHasExtra() {
            TemplateParser parser = TemplateParser.create();

            TemplateParser.ValidationResult result = parser.validate("${a}", Map.of("a", "1", "b", "2"));

            assertThat(result.hasExtra()).isTrue();
        }

        @Test
        @DisplayName("getErrorMessage返回错误信息")
        void testValidationResultGetErrorMessage() {
            TemplateParser parser = TemplateParser.create();

            TemplateParser.ValidationResult result = parser.validate("${a} ${b}", Map.of("a", "1"));

            assertThat(result.getErrorMessage()).contains("Missing");
        }
    }

    @Nested
    @DisplayName("countVariables方法测试")
    class CountVariablesTests {

        @Test
        @DisplayName("返回正确数量")
        void testCountVariables() {
            TemplateParser parser = TemplateParser.create();

            int count = parser.countVariables("${a} ${b} ${c}");

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("无变量返回0")
        void testCountVariablesNone() {
            TemplateParser parser = TemplateParser.create();

            int count = parser.countVariables("Static content");

            assertThat(count).isZero();
        }

        @Test
        @DisplayName("重复变量计算一次")
        void testCountVariablesDuplicates() {
            TemplateParser parser = TemplateParser.create();

            int count = parser.countVariables("${a} ${a} ${b}");

            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("hasVariables方法测试")
    class HasVariablesTests {

        @Test
        @DisplayName("有变量返回true")
        void testHasVariablesTrue() {
            TemplateParser parser = TemplateParser.create();

            boolean result = parser.hasVariables("Hello ${name}");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("无变量返回false")
        void testHasVariablesFalse() {
            TemplateParser parser = TemplateParser.create();

            boolean result = parser.hasVariables("Static content");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null模板返回false")
        void testHasVariablesNull() {
            TemplateParser parser = TemplateParser.create();

            boolean result = parser.hasVariables(null);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("escapeValue方法测试")
    class EscapeValueTests {

        @Test
        @DisplayName("转义$符号")
        void testEscapeDollar() {
            String result = TemplateParser.escapeValue("Test $value");

            assertThat(result).contains("\\$");
        }

        @Test
        @DisplayName("转义大括号")
        void testEscapeBraces() {
            String result = TemplateParser.escapeValue("Test {value}");

            assertThat(result).contains("\\{").contains("\\}");
        }

        @Test
        @DisplayName("null返回null")
        void testEscapeNull() {
            String result = TemplateParser.escapeValue(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("lenient模式测试")
    class LenientTests {

        @Test
        @DisplayName("lenient模式创建")
        void testLenientMode() {
            TemplateParser parser = TemplateParser.lenient("default");

            assertThat(parser).isNotNull();
        }

        @Test
        @DisplayName("lenient模式空变量Map返回原模板")
        void testLenientModeEmptyVariables() {
            // When variables map is empty, template is returned unchanged
            TemplateParser parser = TemplateParser.lenient("N/A");

            String result = parser.render("Hello ${name}", Map.of());

            assertThat(result).isEqualTo("Hello ${name}");
        }

        @Test
        @DisplayName("lenient模式缺失变量使用默认值")
        void testLenientModeDefaultValue() {
            // Default value is used when variables map is non-empty but specific variable is missing
            TemplateParser parser = TemplateParser.lenient("N/A");

            String result = parser.render("Hello ${name}, code is ${code}", Map.of("code", "123"));

            assertThat(result).isEqualTo("Hello N/A, code is 123");
        }
    }

    @Nested
    @DisplayName("withPattern方法测试")
    class WithPatternTests {

        @Test
        @DisplayName("自定义模式")
        void testWithPattern() {
            Pattern customPattern = Pattern.compile("\\[\\[([^\\]]+)\\]\\]");
            TemplateParser parser = TemplateParser.withPattern(customPattern);

            List<String> vars = parser.extractVariables("Hello [[name]]!");

            assertThat(vars).contains("name");
        }
    }

    @Nested
    @DisplayName("hashPattern方法测试")
    class HashPatternTests {

        @Test
        @DisplayName("创建#{}模式解析器")
        void testHashPattern() {
            TemplateParser parser = TemplateParser.hashPattern();

            assertThat(parser).isNotNull();
        }
    }

    @Nested
    @DisplayName("bracePattern方法测试")
    class BracePatternTests {

        @Test
        @DisplayName("创建{{}}模式解析器")
        void testBracePattern() {
            TemplateParser parser = TemplateParser.bracePattern();

            assertThat(parser).isNotNull();
        }
    }
}
