package cloud.opencode.base.sms.batch;

import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;
import cloud.opencode.base.sms.provider.SmsProvider;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * BatchSenderTest Tests
 * BatchSenderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("BatchSender 测试")
class BatchSenderTest {

    private TestSmsProvider provider;

    @BeforeEach
    void setUp() {
        provider = new TestSmsProvider();
    }

    @Nested
    @DisplayName("send方法测试")
    class SendTests {

        @Test
        @DisplayName("发送批量消息")
        void testSend() {
            BatchSender sender = BatchSender.builder(provider)
                    .batchSize(10)
                    .build();

            List<SmsMessage> messages = List.of(
                    SmsMessage.of("13800138001", "Message 1"),
                    SmsMessage.of("13800138002", "Message 2"),
                    SmsMessage.of("13800138003", "Message 3")
            );

            BatchResult result = sender.send(messages);

            assertThat(result.totalCount()).isEqualTo(3);
            assertThat(result.isAllSuccess()).isTrue();
        }

        @Test
        @DisplayName("空消息列表返回空结果")
        void testSendEmpty() {
            BatchSender sender = BatchSender.builder(provider).build();

            BatchResult result = sender.send(List.of());

            assertThat(result.totalCount()).isZero();
        }

        @Test
        @DisplayName("null消息列表返回空结果")
        void testSendNull() {
            BatchSender sender = BatchSender.builder(provider).build();

            BatchResult result = sender.send(null);

            assertThat(result.totalCount()).isZero();
        }
    }

    @Nested
    @DisplayName("sendAsync方法测试")
    class SendAsyncTests {

        @Test
        @DisplayName("异步发送批量消息")
        void testSendAsync() throws Exception {
            BatchSender sender = BatchSender.builder(provider).build();

            List<SmsMessage> messages = List.of(
                    SmsMessage.of("13800138001", "Message 1"),
                    SmsMessage.of("13800138002", "Message 2")
            );

            CompletableFuture<BatchResult> future = sender.sendAsync(messages);
            BatchResult result = future.get();

            assertThat(result.totalCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("sendToPhones方法测试")
    class SendToPhonesTests {

        @Test
        @DisplayName("发送相同内容到多个号码")
        void testSendToPhones() {
            BatchSender sender = BatchSender.builder(provider).build();

            List<String> phones = List.of("13800138001", "13800138002", "13800138003");
            SmsMessage templateMessage = SmsMessage.of("template", "Same message for all");

            BatchResult result = sender.sendToPhones(phones, templateMessage);

            assertThat(result.totalCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("设置batchSize")
        void testBuilderBatchSize() {
            BatchSender sender = BatchSender.builder(provider)
                    .batchSize(50)
                    .build();

            assertThat(sender).isNotNull();
        }

        @Test
        @DisplayName("设置concurrency")
        void testBuilderConcurrency() {
            BatchSender sender = BatchSender.builder(provider)
                    .concurrency(4)
                    .build();

            assertThat(sender).isNotNull();
        }

        @Test
        @DisplayName("设置timeout")
        void testBuilderTimeout() {
            BatchSender sender = BatchSender.builder(provider)
                    .timeout(Duration.ofMinutes(5))
                    .build();

            assertThat(sender).isNotNull();
        }

        @Test
        @DisplayName("设置validatePhones")
        void testBuilderValidatePhones() {
            BatchSender sender = BatchSender.builder(provider)
                    .validatePhones(true)
                    .build();

            assertThat(sender).isNotNull();
        }

        @Test
        @DisplayName("设置onProgress回调")
        void testBuilderOnProgress() {
            AtomicInteger progressCalls = new AtomicInteger(0);

            BatchSender sender = BatchSender.builder(provider)
                    .batchSize(1)
                    .onProgress(progress -> progressCalls.incrementAndGet())
                    .build();

            List<SmsMessage> messages = List.of(
                    SmsMessage.of("13800138001", "Message 1"),
                    SmsMessage.of("13800138002", "Message 2")
            );

            sender.send(messages);

            assertThat(progressCalls.get()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("BatchProgress测试")
    class BatchProgressTests {

        @Test
        @DisplayName("进度记录包含正确信息")
        void testBatchProgressRecord() {
            BatchSender.BatchProgress progress = new BatchSender.BatchProgress(
                    5, 10, 4, 1
            );

            assertThat(progress.completed()).isEqualTo(5);
            assertThat(progress.total()).isEqualTo(10);
            assertThat(progress.success()).isEqualTo(4);
            assertThat(progress.failure()).isEqualTo(1);
        }

        @Test
        @DisplayName("getPercentage计算正确")
        void testGetPercentage() {
            BatchSender.BatchProgress progress = new BatchSender.BatchProgress(
                    5, 10, 4, 1
            );

            assertThat(progress.getPercentage()).isEqualTo(50.0);
        }

        @Test
        @DisplayName("isComplete返回是否完成")
        void testIsComplete() {
            BatchSender.BatchProgress incomplete = new BatchSender.BatchProgress(5, 10, 4, 1);
            BatchSender.BatchProgress complete = new BatchSender.BatchProgress(10, 10, 9, 1);

            assertThat(incomplete.isComplete()).isFalse();
            assertThat(complete.isComplete()).isTrue();
        }

        @Test
        @DisplayName("total为0时getPercentage返回0")
        void testGetPercentageZeroTotal() {
            BatchSender.BatchProgress progress = new BatchSender.BatchProgress(0, 0, 0, 0);

            assertThat(progress.getPercentage()).isEqualTo(0.0);
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
