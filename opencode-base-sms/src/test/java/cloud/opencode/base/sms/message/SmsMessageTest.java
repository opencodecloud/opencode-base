package cloud.opencode.base.sms.message;

import org.junit.jupiter.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * SmsMessageTest Tests
 * SmsMessageTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SmsMessage 测试")
class SmsMessageTest {

    @Nested
    @DisplayName("of方法测试")
    class OfTests {

        @Test
        @DisplayName("创建简单消息")
        void testOfSimple() {
            SmsMessage message = SmsMessage.of("13800138000", "Test content");

            assertThat(message.phoneNumber()).isEqualTo("13800138000");
            assertThat(message.content()).isEqualTo("Test content");
            assertThat(message.templateId()).isNull();
            assertThat(message.variables()).isEmpty();
        }

        @Test
        @DisplayName("null手机号允许")
        void testOfWithNullPhone() {
            SmsMessage message = SmsMessage.of(null, "content");

            assertThat(message.phoneNumber()).isNull();
            assertThat(message.content()).isEqualTo("content");
        }

        @Test
        @DisplayName("null内容允许")
        void testOfWithNullContent() {
            SmsMessage message = SmsMessage.of("13800138000", null);

            assertThat(message.phoneNumber()).isEqualTo("13800138000");
            assertThat(message.content()).isNull();
        }
    }

    @Nested
    @DisplayName("ofTemplate方法测试")
    class OfTemplateTests {

        @Test
        @DisplayName("创建模板消息")
        void testOfTemplate() {
            Map<String, String> vars = Map.of("code", "123456");
            SmsMessage message = SmsMessage.ofTemplate("13800138000", "SMS_001", vars);

            assertThat(message.phoneNumber()).isEqualTo("13800138000");
            assertThat(message.templateId()).isEqualTo("SMS_001");
            assertThat(message.variables()).containsEntry("code", "123456");
        }

        @Test
        @DisplayName("空变量创建模板消息")
        void testOfTemplateWithEmptyVars() {
            SmsMessage message = SmsMessage.ofTemplate("13800138000", "SMS_001", Map.of());

            assertThat(message.templateId()).isEqualTo("SMS_001");
            assertThat(message.variables()).isEmpty();
        }

        @Test
        @DisplayName("null变量转为空Map")
        void testOfTemplateWithNullVars() {
            SmsMessage message = SmsMessage.ofTemplate("13800138000", "SMS_001", null);

            assertThat(message.variables()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("构建完整消息")
        void testBuilderFull() {
            SmsMessage message = SmsMessage.builder()
                    .phoneNumber("13800138000")
                    .content("Hello")
                    .templateId("SMS_001")
                    .variables(Map.of("name", "John"))
                    .build();

            assertThat(message.phoneNumber()).isEqualTo("13800138000");
            assertThat(message.content()).isEqualTo("Hello");
            assertThat(message.templateId()).isEqualTo("SMS_001");
            assertThat(message.variables()).containsEntry("name", "John");
        }

        @Test
        @DisplayName("variable方法添加单个变量")
        void testBuilderVariable() {
            SmsMessage message = SmsMessage.builder()
                    .phoneNumber("13800138000")
                    .content("Test")
                    .variable("code", "123")
                    .variable("name", "Test")
                    .build();

            assertThat(message.variables())
                    .containsEntry("code", "123")
                    .containsEntry("name", "Test");
        }

        @Test
        @DisplayName("null手机号允许")
        void testBuilderWithNullPhone() {
            SmsMessage message = SmsMessage.builder()
                    .content("Test")
                    .build();

            assertThat(message.phoneNumber()).isNull();
            assertThat(message.content()).isEqualTo("Test");
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordTests {

        @Test
        @DisplayName("equals和hashCode")
        void testEqualsAndHashCode() {
            SmsMessage msg1 = SmsMessage.of("13800138000", "Test");
            SmsMessage msg2 = SmsMessage.of("13800138000", "Test");

            assertThat(msg1).isEqualTo(msg2);
            assertThat(msg1.hashCode()).isEqualTo(msg2.hashCode());
        }

        @Test
        @DisplayName("不同消息不相等")
        void testNotEquals() {
            SmsMessage msg1 = SmsMessage.of("13800138000", "Test1");
            SmsMessage msg2 = SmsMessage.of("13800138000", "Test2");

            assertThat(msg1).isNotEqualTo(msg2);
        }

        @Test
        @DisplayName("toString包含关键信息")
        void testToString() {
            SmsMessage message = SmsMessage.of("13800138000", "Test");
            String str = message.toString();

            assertThat(str).contains("13800138000");
            assertThat(str).contains("Test");
        }
    }
}
