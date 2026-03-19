package cloud.opencode.base.sms.provider;

import cloud.opencode.base.sms.config.SmsConfig;
import cloud.opencode.base.sms.config.SmsProviderType;
import cloud.opencode.base.sms.exception.SmsSendException;
import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * AbstractSmsProviderTest Tests
 * AbstractSmsProviderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("AbstractSmsProvider 测试")
class AbstractSmsProviderTest {

    private TestAbstractProvider provider;

    @BeforeEach
    void setUp() {
        SmsConfig config = SmsConfig.builder()
                .providerType(SmsProviderType.CUSTOM)
                .accessKey("test")
                .secretKey("test")
                .signName("TestSign")
                .build();
        provider = new TestAbstractProvider(config);
    }

    @Nested
    @DisplayName("send方法测试")
    class SendTests {

        @Test
        @DisplayName("发送有效消息成功")
        void testSendValidMessage() {
            SmsMessage message = SmsMessage.of("+8613800138000", "Test content");

            SmsResult result = provider.send(message);

            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("null消息抛出异常")
        void testSendNullMessage() {
            assertThatThrownBy(() -> provider.send(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("validatePhoneNumber方法测试")
    class ValidatePhoneNumberTests {

        @Test
        @DisplayName("有效国际号码验证通过")
        void testValidInternationalNumber() {
            SmsMessage message = SmsMessage.of("+8613800138000", "Test");

            assertThatNoException().isThrownBy(() -> provider.send(message));
        }

        @Test
        @DisplayName("有效本地号码验证通过")
        void testValidLocalNumber() {
            SmsMessage message = SmsMessage.of("13800138000", "Test");

            assertThatNoException().isThrownBy(() -> provider.send(message));
        }

        @Test
        @DisplayName("null手机号抛出异常")
        void testNullPhoneNumber() {
            SmsMessage message = SmsMessage.builder()
                    .phoneNumber(null)
                    .content("Test")
                    .build();

            // The builder should handle null, but if it gets through:
            assertThatThrownBy(() -> provider.send(message))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("空白手机号抛出异常")
        void testBlankPhoneNumber() {
            SmsMessage message = SmsMessage.of("   ", "Test");

            assertThatThrownBy(() -> provider.send(message))
                    .isInstanceOf(SmsSendException.class);
        }

        @Test
        @DisplayName("无效手机号抛出异常")
        void testInvalidPhoneNumber() {
            SmsMessage message = SmsMessage.of("123", "Test");

            assertThatThrownBy(() -> provider.send(message))
                    .isInstanceOf(SmsSendException.class);
        }
    }

    @Nested
    @DisplayName("validateContent方法测试")
    class ValidateContentTests {

        @Test
        @DisplayName("有效内容验证通过")
        void testValidContent() {
            SmsMessage message = SmsMessage.of("+8613800138000", "Valid content");

            assertThatNoException().isThrownBy(() -> provider.send(message));
        }

        @Test
        @DisplayName("空白内容抛出异常")
        void testBlankContent() {
            SmsMessage message = SmsMessage.of("+8613800138000", "   ");

            assertThatThrownBy(() -> provider.send(message))
                    .isInstanceOf(SmsSendException.class);
        }

        @Test
        @DisplayName("过长内容抛出异常")
        void testTooLongContent() {
            String longContent = "x".repeat(501);
            SmsMessage message = SmsMessage.of("+8613800138000", longContent);

            assertThatThrownBy(() -> provider.send(message))
                    .isInstanceOf(SmsSendException.class);
        }

        @Test
        @DisplayName("正好500字符通过")
        void testExactly500Characters() {
            String content = "x".repeat(500);
            SmsMessage message = SmsMessage.of("+8613800138000", content);

            assertThatNoException().isThrownBy(() -> provider.send(message));
        }
    }

    @Nested
    @DisplayName("getSignName方法测试")
    class GetSignNameTests {

        @Test
        @DisplayName("返回配置的签名")
        void testGetSignName() {
            assertThat(provider.getSignName()).isEqualTo("TestSign");
        }
    }

    @Nested
    @DisplayName("getConfig方法测试")
    class GetConfigTests {

        @Test
        @DisplayName("返回配置对象")
        void testGetConfig() {
            SmsConfig config = provider.getConfig();

            assertThat(config).isNotNull();
            assertThat(config.signName()).isEqualTo("TestSign");
        }
    }

    // Test implementation
    private static class TestAbstractProvider extends AbstractSmsProvider {
        TestAbstractProvider(SmsConfig config) {
            super(config);
        }

        @Override
        protected SmsResult doSend(SmsMessage message) {
            return SmsResult.success("test-msg-id", message.phoneNumber());
        }

        @Override
        public List<SmsResult> sendBatch(List<SmsMessage> messages) {
            return messages.stream().map(this::send).toList();
        }

        @Override
        public String getName() {
            return "TestProvider";
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        // Expose protected methods for testing
        @Override
        public String getSignName() {
            return super.getSignName();
        }

        @Override
        public SmsConfig getConfig() {
            return super.getConfig();
        }
    }
}
