package cloud.opencode.base.sms.provider;

import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * AliSmsSenderTest Tests
 * AliSmsSenderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("AliSmsSender 测试")
class AliSmsSenderTest {

    @Nested
    @DisplayName("AliSmsConfig测试")
    class AliSmsConfigTests {

        @Test
        @DisplayName("有效配置创建成功")
        void testValidConfigCreation() {
            AliSmsSender.AliSmsConfig config = new AliSmsSender.AliSmsConfig(
                    "accessKeyId",
                    "accessKeySecret",
                    "TestSign",
                    "cn-hangzhou"
            );

            assertThat(config.accessKeyId()).isEqualTo("accessKeyId");
            assertThat(config.accessKeySecret()).isEqualTo("accessKeySecret");
            assertThat(config.signName()).isEqualTo("TestSign");
            assertThat(config.regionId()).isEqualTo("cn-hangzhou");
        }

        @Test
        @DisplayName("null regionId使用默认值")
        void testNullRegionUsesDefault() {
            AliSmsSender.AliSmsConfig config = new AliSmsSender.AliSmsConfig(
                    "accessKeyId",
                    "accessKeySecret",
                    "TestSign",
                    null
            );

            assertThat(config.regionId()).isEqualTo("cn-hangzhou");
        }

        @Test
        @DisplayName("空白regionId使用默认值")
        void testBlankRegionUsesDefault() {
            AliSmsSender.AliSmsConfig config = new AliSmsSender.AliSmsConfig(
                    "accessKeyId",
                    "accessKeySecret",
                    "TestSign",
                    "   "
            );

            assertThat(config.regionId()).isEqualTo("cn-hangzhou");
        }

        @Test
        @DisplayName("null accessKeyId抛出异常")
        void testNullAccessKeyIdThrows() {
            assertThatThrownBy(() -> new AliSmsSender.AliSmsConfig(
                    null,
                    "accessKeySecret",
                    "TestSign",
                    "cn-hangzhou"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null accessKeySecret抛出异常")
        void testNullAccessKeySecretThrows() {
            assertThatThrownBy(() -> new AliSmsSender.AliSmsConfig(
                    "accessKeyId",
                    null,
                    "TestSign",
                    "cn-hangzhou"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null signName抛出异常")
        void testNullSignNameThrows() {
            assertThatThrownBy(() -> new AliSmsSender.AliSmsConfig(
                    "accessKeyId",
                    "accessKeySecret",
                    null,
                    "cn-hangzhou"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("of工厂方法")
        void testOfFactory() {
            AliSmsSender.AliSmsConfig config = AliSmsSender.AliSmsConfig.of(
                    "accessKeyId",
                    "accessKeySecret",
                    "TestSign"
            );

            assertThat(config.regionId()).isEqualTo("cn-hangzhou");
        }

        @Test
        @DisplayName("isConfigured返回true")
        void testIsConfigured() {
            AliSmsSender.AliSmsConfig config = AliSmsSender.AliSmsConfig.of(
                    "accessKeyId",
                    "accessKeySecret",
                    "TestSign"
            );

            assertThat(config.isConfigured()).isTrue();
        }
    }

    @Nested
    @DisplayName("create方法测试")
    class CreateTests {

        @Test
        @DisplayName("有效配置创建成功")
        void testCreateWithValidConfig() {
            AliSmsSender.AliSmsConfig config = AliSmsSender.AliSmsConfig.of(
                    "accessKeyId",
                    "accessKeySecret",
                    "TestSign"
            );

            AliSmsSender sender = AliSmsSender.create(config);

            assertThat(sender).isNotNull();
            assertThat(sender.getConfig()).isEqualTo(config);
        }

        @Test
        @DisplayName("null配置抛出异常")
        void testCreateWithNullConfig() {
            assertThatThrownBy(() -> AliSmsSender.create(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("send方法测试")
    class SendTests {

        private AliSmsSender sender;

        @BeforeEach
        void setUp() {
            AliSmsSender.AliSmsConfig config = AliSmsSender.AliSmsConfig.of(
                    "test-access-key",
                    "test-secret-key",
                    "TestSign"
            );
            sender = AliSmsSender.create(config);
        }

        @Test
        @DisplayName("null消息返回失败")
        void testSendNullMessage() {
            SmsResult result = sender.send(null);

            assertThat(result.success()).isFalse();
            assertThat(result.errorCode()).isEqualTo("INVALID_PARAM");
        }

        @Test
        @DisplayName("null手机号返回失败")
        void testSendNullPhoneNumber() {
            SmsMessage message = SmsMessage.builder()
                    .phoneNumber(null)
                    .templateId("SMS_001")
                    .build();

            SmsResult result = sender.send(message);

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("空白手机号返回失败")
        void testSendBlankPhoneNumber() {
            SmsMessage message = SmsMessage.builder()
                    .phoneNumber("   ")
                    .templateId("SMS_001")
                    .build();

            SmsResult result = sender.send(message);

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("无templateId返回失败")
        void testSendWithoutTemplateId() {
            SmsMessage message = SmsMessage.of("13800138000", "Test content");

            SmsResult result = sender.send(message);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).contains("Template ID");
        }
    }

    @Nested
    @DisplayName("sendBatch方法测试")
    class SendBatchTests {

        private AliSmsSender sender;

        @BeforeEach
        void setUp() {
            AliSmsSender.AliSmsConfig config = AliSmsSender.AliSmsConfig.of(
                    "test-access-key",
                    "test-secret-key",
                    "TestSign"
            );
            sender = AliSmsSender.create(config);
        }

        @Test
        @DisplayName("批量发送返回结果列表")
        void testSendBatch() {
            List<SmsMessage> messages = List.of(
                    SmsMessage.ofTemplate("13800138001", "SMS_001", Map.of("code", "111")),
                    SmsMessage.ofTemplate("13800138002", "SMS_001", Map.of("code", "222"))
            );

            List<SmsResult> results = sender.sendBatch(messages);

            assertThat(results).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("返回aliyun")
        void testGetName() {
            AliSmsSender.AliSmsConfig config = AliSmsSender.AliSmsConfig.of(
                    "accessKeyId",
                    "accessKeySecret",
                    "TestSign"
            );
            AliSmsSender sender = AliSmsSender.create(config);

            assertThat(sender.getName()).isEqualTo("aliyun");
        }
    }

    @Nested
    @DisplayName("isAvailable方法测试")
    class IsAvailableTests {

        @Test
        @DisplayName("配置正确返回true")
        void testIsAvailable() {
            AliSmsSender.AliSmsConfig config = AliSmsSender.AliSmsConfig.of(
                    "accessKeyId",
                    "accessKeySecret",
                    "TestSign"
            );
            AliSmsSender sender = AliSmsSender.create(config);

            assertThat(sender.isAvailable()).isTrue();
        }
    }
}
