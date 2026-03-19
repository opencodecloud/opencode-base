package cloud.opencode.base.sms.provider;

import cloud.opencode.base.sms.config.HttpSmsConfig;
import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * HttpSmsProviderTest Tests
 * HttpSmsProviderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("HttpSmsProvider 测试")
class HttpSmsProviderTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("有效配置创建成功")
        void testValidConfig() {
            HttpSmsConfig config = HttpSmsConfig.builder()
                    .name("test")
                    .apiUrl("https://api.example.com/sms")
                    .appId("app123")
                    .appKey("key456")
                    .signName("TestSign")
                    .build();

            HttpSmsProvider provider = new HttpSmsProvider(config);

            assertThat(provider).isNotNull();
            assertThat(provider.getName()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("send方法测试")
    class SendTests {

        private HttpSmsProvider provider;

        @BeforeEach
        void setUp() {
            HttpSmsConfig config = HttpSmsConfig.builder()
                    .name("test")
                    .apiUrl("https://api.example.com/sms")
                    .appId("app123")
                    .appKey("key456")
                    .signName("TestSign")
                    .build();
            provider = new HttpSmsProvider(config);
        }

        @Test
        @DisplayName("发送消息")
        void testSendMessage() {
            SmsMessage message = SmsMessage.of("13800138000", "Test content");

            // 由于是测试环境，实际HTTP调用会失败
            SmsResult result = provider.send(message);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("发送模板消息")
        void testSendTemplateMessage() {
            SmsMessage message = SmsMessage.ofTemplate(
                    "13800138000",
                    "TPL001",
                    Map.of("code", "123456")
            );

            SmsResult result = provider.send(message);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("sendBatch方法测试")
    class SendBatchTests {

        private HttpSmsProvider provider;

        @BeforeEach
        void setUp() {
            HttpSmsConfig config = HttpSmsConfig.builder()
                    .name("test")
                    .apiUrl("https://api.example.com/sms")
                    .build();
            provider = new HttpSmsProvider(config);
        }

        @Test
        @DisplayName("批量发送返回结果列表")
        void testSendBatch() {
            List<SmsMessage> messages = List.of(
                    SmsMessage.of("13800138001", "Message 1"),
                    SmsMessage.of("13800138002", "Message 2")
            );

            List<SmsResult> results = provider.sendBatch(messages);

            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("空列表返回空结果")
        void testSendBatchEmpty() {
            List<SmsResult> results = provider.sendBatch(List.of());

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("返回配置的名称")
        void testGetName() {
            HttpSmsConfig config = HttpSmsConfig.builder()
                    .name("custom-provider")
                    .apiUrl("https://api.example.com/sms")
                    .build();
            HttpSmsProvider provider = new HttpSmsProvider(config);

            assertThat(provider.getName()).isEqualTo("custom-provider");
        }
    }

    @Nested
    @DisplayName("isAvailable方法测试")
    class IsAvailableTests {

        @Test
        @DisplayName("有apiUrl返回true")
        void testIsAvailableWithApiUrl() {
            HttpSmsConfig config = HttpSmsConfig.builder()
                    .name("test")
                    .apiUrl("https://api.example.com/sms")
                    .build();
            HttpSmsProvider provider = new HttpSmsProvider(config);

            assertThat(provider.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("无apiUrl返回false")
        void testIsAvailableWithoutApiUrl() {
            HttpSmsConfig config = HttpSmsConfig.builder()
                    .name("test")
                    .build();
            HttpSmsProvider provider = new HttpSmsProvider(config);

            assertThat(provider.isAvailable()).isFalse();
        }
    }
}
