package cloud.opencode.base.sms.provider;

import cloud.opencode.base.sms.exception.SmsSendException;
import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * HuaweiSmsSenderTest Tests
 * HuaweiSmsSenderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("HuaweiSmsSender 测试")
class HuaweiSmsSenderTest {

    @Nested
    @DisplayName("HuaweiSmsConfig测试")
    class HuaweiSmsConfigTests {

        @Test
        @DisplayName("有效配置创建成功")
        void testValidConfigCreation() {
            HuaweiSmsSender.HuaweiSmsConfig config = new HuaweiSmsSender.HuaweiSmsConfig(
                    "appKey",
                    "appSecret",
                    "sender",
                    "channelId",
                    "cn-north-4"
            );

            assertThat(config.appKey()).isEqualTo("appKey");
            assertThat(config.appSecret()).isEqualTo("appSecret");
            assertThat(config.sender()).isEqualTo("sender");
            assertThat(config.channelId()).isEqualTo("channelId");
            assertThat(config.region()).isEqualTo("cn-north-4");
        }

        @Test
        @DisplayName("null region使用默认值")
        void testNullRegionUsesDefault() {
            HuaweiSmsSender.HuaweiSmsConfig config = new HuaweiSmsSender.HuaweiSmsConfig(
                    "appKey",
                    "appSecret",
                    "sender",
                    "channelId",
                    null
            );

            assertThat(config.region()).isEqualTo("cn-north-4");
        }

        @Test
        @DisplayName("空白region使用默认值")
        void testBlankRegionUsesDefault() {
            HuaweiSmsSender.HuaweiSmsConfig config = new HuaweiSmsSender.HuaweiSmsConfig(
                    "appKey",
                    "appSecret",
                    "sender",
                    "channelId",
                    "   "
            );

            assertThat(config.region()).isEqualTo("cn-north-4");
        }

        @Test
        @DisplayName("null appKey抛出异常")
        void testNullAppKeyThrows() {
            assertThatThrownBy(() -> new HuaweiSmsSender.HuaweiSmsConfig(
                    null,
                    "appSecret",
                    "sender",
                    "channelId",
                    "cn-north-4"
            )).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null appSecret抛出异常")
        void testNullAppSecretThrows() {
            assertThatThrownBy(() -> new HuaweiSmsSender.HuaweiSmsConfig(
                    "appKey",
                    null,
                    "sender",
                    "channelId",
                    "cn-north-4"
            )).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null sender抛出异常")
        void testNullSenderThrows() {
            assertThatThrownBy(() -> new HuaweiSmsSender.HuaweiSmsConfig(
                    "appKey",
                    "appSecret",
                    null,
                    "channelId",
                    "cn-north-4"
            )).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of工厂方法")
        void testOfFactory() {
            HuaweiSmsSender.HuaweiSmsConfig config = HuaweiSmsSender.HuaweiSmsConfig.of(
                    "appKey",
                    "appSecret",
                    "sender",
                    "channelId"
            );

            assertThat(config.region()).isEqualTo("cn-north-4");
        }

        @Test
        @DisplayName("isConfigured返回true")
        void testIsConfigured() {
            HuaweiSmsSender.HuaweiSmsConfig config = HuaweiSmsSender.HuaweiSmsConfig.of(
                    "appKey",
                    "appSecret",
                    "sender",
                    "channelId"
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
            HuaweiSmsSender.HuaweiSmsConfig config = HuaweiSmsSender.HuaweiSmsConfig.of(
                    "appKey",
                    "appSecret",
                    "sender",
                    "channelId"
            );

            HuaweiSmsSender sender = HuaweiSmsSender.create(config);

            assertThat(sender).isNotNull();
            assertThat(sender.getConfig()).isEqualTo(config);
        }

        @Test
        @DisplayName("null配置抛出异常")
        void testCreateWithNullConfig() {
            assertThatThrownBy(() -> HuaweiSmsSender.create(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("send方法测试")
    class SendTests {

        private HuaweiSmsSender sender;

        @BeforeEach
        void setUp() {
            HuaweiSmsSender.HuaweiSmsConfig config = HuaweiSmsSender.HuaweiSmsConfig.of(
                    "test-app-key",
                    "test-app-secret",
                    "test-sender",
                    "test-channel"
            );
            sender = HuaweiSmsSender.create(config);
        }

        @Test
        @DisplayName("null消息抛出异常")
        void testSendNullMessage() {
            assertThatThrownBy(() -> sender.send(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null手机号抛出异常")
        void testSendNullPhoneNumber() {
            SmsMessage message = SmsMessage.builder()
                    .phoneNumber(null)
                    .content("Test")
                    .build();

            assertThatThrownBy(() -> sender.send(message))
                    .isInstanceOf(SmsSendException.class);
        }

        @Test
        @DisplayName("空白手机号抛出异常")
        void testSendBlankPhoneNumber() {
            SmsMessage message = SmsMessage.builder()
                    .phoneNumber("   ")
                    .content("Test")
                    .build();

            assertThatThrownBy(() -> sender.send(message))
                    .isInstanceOf(SmsSendException.class);
        }
    }

    @Nested
    @DisplayName("sendBatch方法测试")
    class SendBatchTests {

        private HuaweiSmsSender sender;

        @BeforeEach
        void setUp() {
            HuaweiSmsSender.HuaweiSmsConfig config = HuaweiSmsSender.HuaweiSmsConfig.of(
                    "test-app-key",
                    "test-app-secret",
                    "test-sender",
                    "test-channel"
            );
            sender = HuaweiSmsSender.create(config);
        }

        @Test
        @DisplayName("批量发送返回结果列表")
        void testSendBatch() {
            List<SmsMessage> messages = List.of(
                    SmsMessage.ofTemplate("13800138001", "TPL001", Map.of("code", "111")),
                    SmsMessage.ofTemplate("13800138002", "TPL001", Map.of("code", "222"))
            );

            List<SmsResult> results = sender.sendBatch(messages);

            assertThat(results).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("返回HUAWEI")
        void testGetName() {
            HuaweiSmsSender.HuaweiSmsConfig config = HuaweiSmsSender.HuaweiSmsConfig.of(
                    "appKey",
                    "appSecret",
                    "sender",
                    "channelId"
            );
            HuaweiSmsSender sender = HuaweiSmsSender.create(config);

            assertThat(sender.getName()).isEqualTo("HUAWEI");
        }
    }

    @Nested
    @DisplayName("isAvailable方法测试")
    class IsAvailableTests {

        @Test
        @DisplayName("配置正确返回true")
        void testIsAvailable() {
            HuaweiSmsSender.HuaweiSmsConfig config = HuaweiSmsSender.HuaweiSmsConfig.of(
                    "appKey",
                    "appSecret",
                    "sender",
                    "channelId"
            );
            HuaweiSmsSender sender = HuaweiSmsSender.create(config);

            assertThat(sender.isAvailable()).isTrue();
        }
    }

    @Nested
    @DisplayName("getEndpoint方法测试")
    class GetEndpointTests {

        @Test
        @DisplayName("返回正确的端点URL")
        void testGetEndpoint() {
            HuaweiSmsSender.HuaweiSmsConfig config = new HuaweiSmsSender.HuaweiSmsConfig(
                    "appKey",
                    "appSecret",
                    "sender",
                    "channelId",
                    "cn-north-4"
            );
            HuaweiSmsSender sender = HuaweiSmsSender.create(config);

            assertThat(sender.getEndpoint()).contains("cn-north-4");
            assertThat(sender.getEndpoint()).contains("myhuaweicloud.com");
        }
    }
}
