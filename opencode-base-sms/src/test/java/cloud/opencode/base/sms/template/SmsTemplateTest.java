package cloud.opencode.base.sms.template;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * SmsTemplateTest Tests
 * SmsTemplateTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SmsTemplate 测试")
class SmsTemplateTest {

    @Nested
    @DisplayName("of方法测试")
    class OfTests {

        @Test
        @DisplayName("创建简单模板")
        void testOfSimple() {
            SmsTemplate template = SmsTemplate.of("verify", "Your code is ${code}");

            assertThat(template.id()).isEqualTo("verify");
            assertThat(template.content()).isEqualTo("Your code is ${code}");
        }

        @Test
        @DisplayName("null id允许")
        void testOfWithNullId() {
            SmsTemplate template = SmsTemplate.of(null, "content");

            assertThat(template.id()).isNull();
            assertThat(template.content()).isEqualTo("content");
        }

        @Test
        @DisplayName("null content允许")
        void testOfWithNullContent() {
            SmsTemplate template = SmsTemplate.of("id", null);

            assertThat(template.id()).isEqualTo("id");
            assertThat(template.content()).isNull();
        }
    }

    @Nested
    @DisplayName("render方法测试")
    class RenderTests {

        @Test
        @DisplayName("渲染单个变量")
        void testRenderSingleVariable() {
            SmsTemplate template = SmsTemplate.of("verify", "Your code is ${code}");

            String result = template.render(Map.of("code", "123456"));

            assertThat(result).isEqualTo("Your code is 123456");
        }

        @Test
        @DisplayName("渲染多个变量")
        void testRenderMultipleVariables() {
            SmsTemplate template = SmsTemplate.of("welcome", "Hello ${name}, your order ${orderId} is ready");

            String result = template.render(Map.of("name", "John", "orderId", "12345"));

            assertThat(result).isEqualTo("Hello John, your order 12345 is ready");
        }

        @Test
        @DisplayName("缺少变量保持原样")
        void testRenderMissingVariable() {
            SmsTemplate template = SmsTemplate.of("test", "Hello ${name}");

            String result = template.render(Map.of());

            assertThat(result).isEqualTo("Hello ${name}");
        }

        @Test
        @DisplayName("空变量映射")
        void testRenderEmptyVariables() {
            SmsTemplate template = SmsTemplate.of("static", "This is static content");

            String result = template.render(Map.of());

            assertThat(result).isEqualTo("This is static content");
        }
    }

    @Nested
    @DisplayName("hasAllVariables方法测试")
    class HasAllVariablesTests {

        @Test
        @DisplayName("所有变量都存在返回true")
        void testHasAllVariablesTrue() {
            SmsTemplate template = SmsTemplate.of("test", "Hello ${name}, code is ${code}");

            boolean result = template.hasAllVariables(Map.of("name", "John", "code", "123"));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("缺少变量返回false")
        void testHasAllVariablesFalse() {
            SmsTemplate template = SmsTemplate.of("test", "Hello ${name}, code is ${code}");

            boolean result = template.hasAllVariables(Map.of("name", "John"));

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("无变量模板返回true")
        void testHasAllVariablesNoVariables() {
            SmsTemplate template = SmsTemplate.of("static", "Static content");

            boolean result = template.hasAllVariables(Map.of());

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("getMissingVariables方法测试")
    class GetMissingVariablesTests {

        @Test
        @DisplayName("返回缺少的变量")
        void testGetMissingVariables() {
            SmsTemplate template = SmsTemplate.of("test", "Hello ${name}, code is ${code}");

            List<String> missing = template.getMissingVariables(Map.of("name", "John"));

            assertThat(missing).containsExactly("code");
        }

        @Test
        @DisplayName("无缺少变量返回空列表")
        void testGetMissingVariablesNone() {
            SmsTemplate template = SmsTemplate.of("test", "Hello ${name}");

            List<String> missing = template.getMissingVariables(Map.of("name", "John"));

            assertThat(missing).isEmpty();
        }

        @Test
        @DisplayName("返回多个缺少变量")
        void testGetMissingVariablesMultiple() {
            SmsTemplate template = SmsTemplate.of("test", "${a} ${b} ${c}");

            List<String> missing = template.getMissingVariables(Map.of());

            assertThat(missing).hasSize(3);
            assertThat(missing).contains("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordTests {

        @Test
        @DisplayName("equals和hashCode")
        void testEqualsAndHashCode() {
            SmsTemplate t1 = SmsTemplate.of("id", "content");
            SmsTemplate t2 = SmsTemplate.of("id", "content");

            assertThat(t1).isEqualTo(t2);
            assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
        }

        @Test
        @DisplayName("不同模板不相等")
        void testNotEquals() {
            SmsTemplate t1 = SmsTemplate.of("id1", "content");
            SmsTemplate t2 = SmsTemplate.of("id2", "content");

            assertThat(t1).isNotEqualTo(t2);
        }

        @Test
        @DisplayName("toString包含关键信息")
        void testToString() {
            SmsTemplate template = SmsTemplate.of("verify", "Your code is ${code}");

            assertThat(template.toString()).contains("verify");
        }
    }
}
