package cloud.opencode.base.email.internal;

import org.junit.jupiter.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailTemplateTest Tests
 * EmailTemplateTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailTemplate 接口测试")
class EmailTemplateTest {

    @Nested
    @DisplayName("render方法测试")
    class RenderTests {

        @Test
        @DisplayName("Lambda实现render")
        void testLambdaRender() {
            EmailTemplate template = (tmpl, vars) -> {
                String result = tmpl;
                for (var entry : vars.entrySet()) {
                    result = result.replace("${" + entry.getKey() + "}", entry.getValue().toString());
                }
                return result;
            };

            String result = template.render("Hello ${name}!", Map.of("name", "World"));
            assertThat(result).isEqualTo("Hello World!");
        }

        @Test
        @DisplayName("空变量Map")
        void testEmptyVariables() {
            EmailTemplate template = (tmpl, vars) -> tmpl;
            String result = template.render("Hello!", Map.of());
            assertThat(result).isEqualTo("Hello!");
        }

        @Test
        @DisplayName("多变量替换")
        void testMultipleVariables() {
            EmailTemplate template = (tmpl, vars) -> {
                String result = tmpl;
                for (var entry : vars.entrySet()) {
                    result = result.replace("{" + entry.getKey() + "}", entry.getValue().toString());
                }
                return result;
            };

            String result = template.render("Dear {name}, your order #{orderId} is ready.",
                    Map.of("name", "Alice", "orderId", 42));
            assertThat(result).isEqualTo("Dear Alice, your order #42 is ready.");
        }
    }

    @Nested
    @DisplayName("接口特性测试")
    class InterfaceTests {

        @Test
        @DisplayName("EmailTemplate是接口")
        void testIsInterface() {
            assertThat(EmailTemplate.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("只有一个抽象方法")
        void testSingleMethod() {
            long abstractMethodCount = java.util.Arrays.stream(EmailTemplate.class.getDeclaredMethods())
                    .filter(m -> java.lang.reflect.Modifier.isAbstract(m.getModifiers()))
                    .count();
            assertThat(abstractMethodCount).isEqualTo(1);
        }
    }
}
