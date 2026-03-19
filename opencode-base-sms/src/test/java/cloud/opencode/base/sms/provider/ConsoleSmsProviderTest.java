package cloud.opencode.base.sms.provider;

import cloud.opencode.base.sms.config.SmsConfig;
import cloud.opencode.base.sms.config.SmsProviderType;
import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * ConsoleSmsProviderTest Tests
 * ConsoleSmsProviderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("ConsoleSmsProvider 测试")
class ConsoleSmsProviderTest {

    private ConsoleSmsProvider provider;

    @BeforeEach
    void setUp() {
        SmsConfig config = SmsConfig.builder()
                .providerType(SmsProviderType.CONSOLE)
                .accessKey("test")
                .secretKey("test")
                .signName("Test")
                .build();
        provider = new ConsoleSmsProvider(config);
    }

    @Nested
    @DisplayName("send方法测试")
    class SendTests {

        @Test
        @DisplayName("发送简单消息返回成功")
        void testSendSimple() {
            SmsMessage message = SmsMessage.of("13800138000", "Test message");

            SmsResult result = provider.send(message);

            assertThat(result.success()).isTrue();
            assertThat(result.phoneNumber()).isEqualTo("13800138000");
        }

        @Test
        @DisplayName("发送模板消息返回成功")
        void testSendTemplate() {
            // ConsoleSmsProvider requires content to be non-null
            SmsMessage message = SmsMessage.builder()
                    .phoneNumber("13800138000")
                    .content("Your code is 123456")
                    .templateId("SMS_001")
                    .variables(Map.of("code", "123456"))
                    .build();

            SmsResult result = provider.send(message);

            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("输出到控制台")
        void testSendOutputsToConsole() {
            SmsMessage message = SmsMessage.of("13800138000", "Hello World");
            SmsResult result = provider.send(message);

            assertThat(result.success()).isTrue();
            assertThat(result.phoneNumber()).isEqualTo("13800138000");
            assertThat(result.messageId()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("null消息抛出异常")
        void testSendNullMessage() {
            assertThatThrownBy(() -> provider.send(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null内容抛出异常")
        void testSendNullContent() {
            SmsMessage message = SmsMessage.ofTemplate("13800138000", "SMS_001", Map.of("code", "123456"));

            assertThatThrownBy(() -> provider.send(message))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Content");
        }
    }

    @Nested
    @DisplayName("sendBatch方法测试")
    class SendBatchTests {

        @Test
        @DisplayName("批量发送返回所有成功")
        void testSendBatch() {
            List<SmsMessage> messages = List.of(
                    SmsMessage.of("13800138001", "Message 1"),
                    SmsMessage.of("13800138002", "Message 2"),
                    SmsMessage.of("13800138003", "Message 3")
            );

            List<SmsResult> results = provider.sendBatch(messages);

            assertThat(results).hasSize(3);
            assertThat(results).allMatch(SmsResult::success);
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
        @DisplayName("返回console")
        void testGetName() {
            assertThat(provider.getName()).isEqualTo("console");
        }
    }

    @Nested
    @DisplayName("isAvailable方法测试")
    class IsAvailableTests {

        @Test
        @DisplayName("始终返回true")
        void testIsAvailable() {
            assertThat(provider.isAvailable()).isTrue();
        }
    }

    @Nested
    @DisplayName("close方法测试")
    class CloseTests {

        @Test
        @DisplayName("close不抛出异常")
        void testClose() {
            assertThatNoException().isThrownBy(() -> provider.close());
        }
    }

    @Nested
    @DisplayName("无参构造函数测试")
    class NoArgConstructorTests {

        @Test
        @DisplayName("无参构造函数创建provider")
        void testNoArgConstructor() {
            ConsoleSmsProvider noArgProvider = new ConsoleSmsProvider();

            assertThat(noArgProvider).isNotNull();
            assertThat(noArgProvider.getName()).isEqualTo("console");
        }
    }
}
