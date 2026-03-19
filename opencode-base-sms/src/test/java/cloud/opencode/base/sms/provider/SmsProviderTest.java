package cloud.opencode.base.sms.provider;

import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * SmsProviderTest Tests
 * SmsProviderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SmsProvider 测试")
class SmsProviderTest {

    private SmsProvider provider;

    @BeforeEach
    void setUp() {
        provider = new TestSmsProvider();
    }

    @Nested
    @DisplayName("sendBatch默认方法测试")
    class SendBatchTests {

        @Test
        @DisplayName("批量发送调用单条发送")
        void testSendBatchDefault() {
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
    @DisplayName("isAvailable默认方法测试")
    class IsAvailableTests {

        @Test
        @DisplayName("默认返回true")
        void testIsAvailableDefault() {
            assertThat(provider.isAvailable()).isTrue();
        }
    }

    @Nested
    @DisplayName("close默认方法测试")
    class CloseTests {

        @Test
        @DisplayName("默认不抛出异常")
        void testCloseDefault() {
            assertThatNoException().isThrownBy(() -> provider.close());
        }
    }

    // Simple test provider implementation
    private static class TestSmsProvider implements SmsProvider {
        @Override
        public SmsResult send(SmsMessage message) {
            return SmsResult.success("test-msg-id", message.phoneNumber());
        }

        @Override
        public String getName() {
            return "test";
        }
    }
}
