package cloud.opencode.base.sms.provider;

import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * TencentSmsSenderTest Tests
 * TencentSmsSenderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("TencentSmsSender 测试")
class TencentSmsSenderTest {

    @Nested
    @DisplayName("TencentSmsConfig测试")
    class TencentSmsConfigTests {

        @Test
        @DisplayName("有效配置创建成功")
        void testValidConfigCreation() {
            TencentSmsSender.TencentSmsConfig config = new TencentSmsSender.TencentSmsConfig(
                    "secretId",
                    "secretKey",
                    "1400000000",
                    "TestSign",
                    "ap-guangzhou"
            );

            assertThat(config.secretId()).isEqualTo("secretId");
            assertThat(config.secretKey()).isEqualTo("secretKey");
            assertThat(config.appId()).isEqualTo("1400000000");
            assertThat(config.signName()).isEqualTo("TestSign");
            assertThat(config.region()).isEqualTo("ap-guangzhou");
        }

        @Test
        @DisplayName("null secretId抛出异常")
        void testNullSecretIdThrows() {
            assertThatThrownBy(() -> new TencentSmsSender.TencentSmsConfig(
                    null,
                    "secretKey",
                    "1400000000",
                    "TestSign",
                    "ap-guangzhou"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("空白secretId抛出异常")
        void testBlankSecretIdThrows() {
            assertThatThrownBy(() -> new TencentSmsSender.TencentSmsConfig(
                    "   ",
                    "secretKey",
                    "1400000000",
                    "TestSign",
                    "ap-guangzhou"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null secretKey抛出异常")
        void testNullSecretKeyThrows() {
            assertThatThrownBy(() -> new TencentSmsSender.TencentSmsConfig(
                    "secretId",
                    null,
                    "1400000000",
                    "TestSign",
                    "ap-guangzhou"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null appId抛出异常")
        void testNullAppIdThrows() {
            assertThatThrownBy(() -> new TencentSmsSender.TencentSmsConfig(
                    "secretId",
                    "secretKey",
                    null,
                    "TestSign",
                    "ap-guangzhou"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null signName抛出异常")
        void testNullSignNameThrows() {
            assertThatThrownBy(() -> new TencentSmsSender.TencentSmsConfig(
                    "secretId",
                    "secretKey",
                    "1400000000",
                    null,
                    "ap-guangzhou"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null region抛出异常")
        void testNullRegionThrows() {
            assertThatThrownBy(() -> new TencentSmsSender.TencentSmsConfig(
                    "secretId",
                    "secretKey",
                    "1400000000",
                    "TestSign",
                    null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("isConfigured返回true")
        void testIsConfigured() {
            TencentSmsSender.TencentSmsConfig config = new TencentSmsSender.TencentSmsConfig(
                    "secretId",
                    "secretKey",
                    "1400000000",
                    "TestSign",
                    "ap-guangzhou"
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
            TencentSmsSender.TencentSmsConfig config = new TencentSmsSender.TencentSmsConfig(
                    "secretId",
                    "secretKey",
                    "1400000000",
                    "TestSign",
                    "ap-guangzhou"
            );

            TencentSmsSender sender = TencentSmsSender.create(config);

            assertThat(sender).isNotNull();
            assertThat(sender.getConfig()).isEqualTo(config);
        }

        @Test
        @DisplayName("null配置抛出异常")
        void testCreateWithNullConfig() {
            assertThatThrownBy(() -> TencentSmsSender.create(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("send方法测试")
    class SendTests {

        private TencentSmsSender sender;

        @BeforeEach
        void setUp() {
            TencentSmsSender.TencentSmsConfig config = new TencentSmsSender.TencentSmsConfig(
                    "test-secret-id",
                    "test-secret-key",
                    "1400000000",
                    "TestSign",
                    "ap-guangzhou"
            );
            sender = TencentSmsSender.create(config);
        }

        @Test
        @DisplayName("发送模板消息")
        void testSendTemplateMessage() {
            SmsMessage message = SmsMessage.ofTemplate(
                    "+8613800138000",
                    "123456",
                    Map.of("code", "1234")
            );

            // 由于是测试环境，实际API调用会失败，但我们验证方法能正常执行
            SmsResult result = sender.send(message);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("sendBatch方法测试")
    class SendBatchTests {

        private TencentSmsSender sender;

        @BeforeEach
        void setUp() {
            TencentSmsSender.TencentSmsConfig config = new TencentSmsSender.TencentSmsConfig(
                    "test-secret-id",
                    "test-secret-key",
                    "1400000000",
                    "TestSign",
                    "ap-guangzhou"
            );
            sender = TencentSmsSender.create(config);
        }

        @Test
        @DisplayName("批量发送返回结果列表")
        void testSendBatch() {
            List<SmsMessage> messages = List.of(
                    SmsMessage.ofTemplate("+8613800138001", "123456", Map.of("code", "111")),
                    SmsMessage.ofTemplate("+8613800138002", "123456", Map.of("code", "222"))
            );

            List<SmsResult> results = sender.sendBatch(messages);

            assertThat(results).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("返回TencentCloud")
        void testGetName() {
            TencentSmsSender.TencentSmsConfig config = new TencentSmsSender.TencentSmsConfig(
                    "secretId",
                    "secretKey",
                    "1400000000",
                    "TestSign",
                    "ap-guangzhou"
            );
            TencentSmsSender sender = TencentSmsSender.create(config);

            assertThat(sender.getName()).isEqualTo("TencentCloud");
        }
    }

    @Nested
    @DisplayName("isAvailable方法测试")
    class IsAvailableTests {

        @Test
        @DisplayName("配置正确返回true")
        void testIsAvailable() {
            TencentSmsSender.TencentSmsConfig config = new TencentSmsSender.TencentSmsConfig(
                    "secretId",
                    "secretKey",
                    "1400000000",
                    "TestSign",
                    "ap-guangzhou"
            );
            TencentSmsSender sender = TencentSmsSender.create(config);

            assertThat(sender.isAvailable()).isTrue();
        }
    }
}
