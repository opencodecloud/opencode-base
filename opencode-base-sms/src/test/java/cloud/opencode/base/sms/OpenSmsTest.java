package cloud.opencode.base.sms;

import cloud.opencode.base.sms.config.SmsConfig;
import cloud.opencode.base.sms.config.SmsProviderType;
import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;
import cloud.opencode.base.sms.provider.SmsProvider;
import cloud.opencode.base.sms.template.SmsTemplate;
import cloud.opencode.base.sms.template.SmsTemplateRegistry;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenSmsTest Tests
 * OpenSmsTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("OpenSms 测试")
class OpenSmsTest {

    @Nested
    @DisplayName("of方法测试")
    class OfTests {

        @Test
        @DisplayName("使用配置创建")
        void testOfWithConfig() {
            SmsConfig config = SmsConfig.builder()
                    .providerType(SmsProviderType.CONSOLE)
                    .accessKey("test")
                    .secretKey("test")
                    .signName("Test")
                    .build();

            OpenSms sms = OpenSms.of(config);

            assertThat(sms).isNotNull();
            assertThat(sms.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("使用提供商创建")
        void testOfWithProvider() {
            SmsProvider provider = new TestSmsProvider();

            OpenSms sms = OpenSms.of(provider);

            assertThat(sms).isNotNull();
            assertThat(sms.getProvider()).isEqualTo(provider);
        }

        @Test
        @DisplayName("null配置抛出SmsException")
        void testOfWithNullConfig() {
            assertThatThrownBy(() -> OpenSms.of((SmsConfig) null))
                    .isInstanceOf(cloud.opencode.base.sms.exception.SmsException.class);
        }
    }

    @Nested
    @DisplayName("console方法测试")
    class ConsoleTests {

        @Test
        @DisplayName("创建控制台SMS实例")
        void testConsole() {
            OpenSms sms = OpenSms.console();

            assertThat(sms).isNotNull();
            assertThat(sms.isAvailable()).isTrue();
            assertThat(sms.getProvider().getName()).isEqualToIgnoringCase("CONSOLE");
        }
    }

    @Nested
    @DisplayName("send方法测试")
    class SendTests {

        private OpenSms sms;

        @BeforeEach
        void setUp() {
            sms = OpenSms.console();
        }

        @Test
        @DisplayName("发送简单消息")
        void testSendSimple() {
            SmsResult result = sms.send("13800138000", "Test message");

            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("发送消息对象")
        void testSendMessage() {
            SmsMessage message = SmsMessage.of("13800138000", "Test message");

            SmsResult result = sms.send(message);

            assertThat(result.success()).isTrue();
        }
    }

    @Nested
    @DisplayName("sendBatch方法测试")
    class SendBatchTests {

        private OpenSms sms;

        @BeforeEach
        void setUp() {
            sms = OpenSms.console();
        }

        @Test
        @DisplayName("批量发送消息")
        void testSendBatch() {
            List<SmsMessage> messages = List.of(
                    SmsMessage.of("13800138001", "Message 1"),
                    SmsMessage.of("13800138002", "Message 2")
            );

            List<SmsResult> results = sms.sendBatch(messages);

            assertThat(results).hasSize(2);
            assertThat(results).allMatch(SmsResult::success);
        }
    }

    @Nested
    @DisplayName("sendToAll方法测试")
    class SendToAllTests {

        private OpenSms sms;

        @BeforeEach
        void setUp() {
            sms = OpenSms.console();
        }

        @Test
        @DisplayName("发送相同内容到多个号码")
        void testSendToAll() {
            List<String> phones = List.of("13800138001", "13800138002", "13800138003");

            List<SmsResult> results = sms.sendToAll(phones, "Same message");

            assertThat(results).hasSize(3);
        }
    }

    @Nested
    @DisplayName("模板功能测试")
    class TemplateTests {

        private OpenSms sms;

        @BeforeEach
        void setUp() {
            sms = OpenSms.console();
        }

        @Test
        @DisplayName("注册模板-使用ID和内容")
        void testRegisterTemplateWithIdAndContent() {
            OpenSms result = sms.registerTemplate("verify", "Your code is ${code}");

            assertThat(result).isSameAs(sms);
            assertThat(sms.getTemplateRegistry().contains("verify")).isTrue();
        }

        @Test
        @DisplayName("注册模板-使用SmsTemplate")
        void testRegisterTemplate() {
            SmsTemplate template = SmsTemplate.of("verify", "Your code is ${code}");

            sms.registerTemplate(template);

            assertThat(sms.getTemplateRegistry().contains("verify")).isTrue();
        }

        @Test
        @DisplayName("使用模板发送")
        void testSendTemplate() {
            sms.registerTemplate(SmsTemplate.of("verify", "Your code is ${code}"));

            SmsResult result = sms.sendTemplate("verify", "13800138000", Map.of("code", "123456"));

            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("使用模板批量发送")
        void testSendTemplateToAll() {
            sms.registerTemplate(SmsTemplate.of("notify", "Message: ${msg}"));

            List<SmsResult> results = sms.sendTemplateToAll(
                    "notify",
                    List.of("13800138001", "13800138002"),
                    Map.of("msg", "Hello")
            );

            assertThat(results).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getProvider方法测试")
    class GetProviderTests {

        @Test
        @DisplayName("返回提供商")
        void testGetProvider() {
            OpenSms sms = OpenSms.console();

            SmsProvider provider = sms.getProvider();

            assertThat(provider).isNotNull();
            assertThat(provider.getName()).isEqualToIgnoringCase("CONSOLE");
        }
    }

    @Nested
    @DisplayName("getTemplateRegistry方法测试")
    class GetTemplateRegistryTests {

        @Test
        @DisplayName("返回模板注册表")
        void testGetTemplateRegistry() {
            OpenSms sms = OpenSms.console();

            SmsTemplateRegistry registry = sms.getTemplateRegistry();

            assertThat(registry).isNotNull();
        }
    }

    @Nested
    @DisplayName("isAvailable方法测试")
    class IsAvailableTests {

        @Test
        @DisplayName("console始终可用")
        void testIsAvailable() {
            OpenSms sms = OpenSms.console();

            assertThat(sms.isAvailable()).isTrue();
        }
    }

    @Nested
    @DisplayName("close方法测试")
    class CloseTests {

        @Test
        @DisplayName("close不抛出异常")
        void testClose() {
            OpenSms sms = OpenSms.console();

            assertThatNoException().isThrownBy(sms::close);
        }

        @Test
        @DisplayName("close后可以继续调用")
        void testCloseAndUse() {
            OpenSms sms = OpenSms.console();
            sms.close();

            // close后仍可使用（console provider不做实际清理）
            assertThatNoException().isThrownBy(() -> sms.send("13800138000", "Test"));
        }
    }

    // Test helper provider
    private static class TestSmsProvider implements SmsProvider {
        @Override
        public SmsResult send(SmsMessage message) {
            return SmsResult.success("test-msg-id", message.phoneNumber());
        }

        @Override
        public List<SmsResult> sendBatch(List<SmsMessage> messages) {
            return messages.stream()
                    .map(m -> SmsResult.success("test-msg-id", m.phoneNumber()))
                    .toList();
        }

        @Override
        public String getName() {
            return "TestProvider";
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }
}
