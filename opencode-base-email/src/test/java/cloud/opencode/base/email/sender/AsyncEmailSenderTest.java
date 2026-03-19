package cloud.opencode.base.email.sender;

import cloud.opencode.base.email.Email;
import cloud.opencode.base.email.EmailConfig;
import cloud.opencode.base.email.SendResult;
import cloud.opencode.base.email.exception.EmailException;
import cloud.opencode.base.email.internal.EmailSender;
import cloud.opencode.base.email.retry.EmailRetryExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * AsyncEmailSender 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("AsyncEmailSender 测试")
class AsyncEmailSenderTest {

    private MockEmailSender mockSender;

    @BeforeEach
    void setUp() {
        mockSender = new MockEmailSender();
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用代理发送器创建")
        void testConstructorWithDelegate() {
            try (AsyncEmailSender sender = new AsyncEmailSender(mockSender)) {
                assertThat(sender.getDelegate()).isEqualTo(mockSender);
                assertThat(sender.getExecutor()).isNotNull();
            }
        }

        @Test
        @DisplayName("使用代理和重试执行器创建")
        void testConstructorWithRetryExecutor() {
            EmailRetryExecutor retryExecutor = EmailRetryExecutor.builder()
                    .maxRetries(3)
                    .build();

            try (AsyncEmailSender sender = new AsyncEmailSender(mockSender, retryExecutor)) {
                assertThat(sender.getDelegate()).isEqualTo(mockSender);
            }
        }
    }

    @Nested
    @DisplayName("send() 同步发送测试")
    class SendTests {

        @Test
        @DisplayName("同步发送邮件")
        void testSend() {
            try (AsyncEmailSender sender = new AsyncEmailSender(mockSender)) {
                Email email = createTestEmail();
                sender.send(email);

                assertThat(mockSender.getSentCount()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("使用重试执行器同步发送")
        void testSendWithRetry() {
            EmailRetryExecutor retryExecutor = EmailRetryExecutor.builder()
                    .maxRetries(3)
                    .build();

            try (AsyncEmailSender sender = new AsyncEmailSender(mockSender, retryExecutor)) {
                Email email = createTestEmail();
                sender.send(email);

                assertThat(mockSender.getSentCount()).isEqualTo(1);
            }
        }
    }

    @Nested
    @DisplayName("sendAsync() 异步发送测试")
    class SendAsyncTests {

        @Test
        @DisplayName("异步发送单个邮件")
        void testSendAsync() throws Exception {
            try (AsyncEmailSender sender = new AsyncEmailSender(mockSender)) {
                Email email = createTestEmail();

                CompletableFuture<Void> future = sender.sendAsync(email);
                future.get(5, TimeUnit.SECONDS);

                assertThat(mockSender.getSentCount()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("异步发送带回调")
        void testSendAsyncWithCallback() throws Exception {
            try (AsyncEmailSender sender = new AsyncEmailSender(mockSender)) {
                Email email = createTestEmail();
                AtomicBoolean callbackCalled = new AtomicBoolean(false);

                CompletableFuture<Void> future = sender.sendAsync(email, (e, ex) -> {
                    callbackCalled.set(true);
                    assertThat(e).isEqualTo(email);
                    assertThat(ex).isNull();
                });

                future.get(5, TimeUnit.SECONDS);

                assertThat(callbackCalled.get()).isTrue();
            }
        }

        @Test
        @DisplayName("异步发送失败回调收到异常")
        void testSendAsyncFailureCallback() throws Exception {
            MockEmailSender failingSender = new MockEmailSender() {
                @Override
                public void send(Email email) {
                    throw new EmailException("Send failed");
                }
            };

            try (AsyncEmailSender sender = new AsyncEmailSender(failingSender)) {
                Email email = createTestEmail();
                AtomicBoolean exceptionReceived = new AtomicBoolean(false);

                CompletableFuture<Void> future = sender.sendAsync(email, (e, ex) -> {
                    if (ex != null) {
                        exceptionReceived.set(true);
                    }
                });

                assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS))
                        .isInstanceOf(ExecutionException.class);

                assertThat(exceptionReceived.get()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("sendAllAsync() 批量异步发送测试")
    class SendAllAsyncTests {

        @Test
        @DisplayName("批量异步发送")
        void testSendAllAsync() throws Exception {
            try (AsyncEmailSender sender = new AsyncEmailSender(mockSender)) {
                List<Email> emails = List.of(
                        createTestEmail(),
                        createTestEmail(),
                        createTestEmail()
                );

                List<CompletableFuture<Void>> futures = sender.sendAllAsync(emails);

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .get(10, TimeUnit.SECONDS);

                assertThat(mockSender.getSentCount()).isEqualTo(3);
            }
        }

        @Test
        @DisplayName("批量发送并等待全部完成")
        void testSendAllAndWait() throws Exception {
            try (AsyncEmailSender sender = new AsyncEmailSender(mockSender)) {
                List<Email> emails = List.of(
                        createTestEmail(),
                        createTestEmail()
                );

                sender.sendAllAndWait(emails).get(10, TimeUnit.SECONDS);

                assertThat(mockSender.getSentCount()).isEqualTo(2);
            }
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("使用sender构建")
        void testBuilderWithSender() {
            try (AsyncEmailSender sender = AsyncEmailSender.builder()
                    .sender(mockSender)
                    .build()) {
                assertThat(sender.getDelegate()).isEqualTo(mockSender);
            }
        }

        @Test
        @DisplayName("使用config构建")
        void testBuilderWithConfig() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            try (AsyncEmailSender sender = AsyncEmailSender.builder()
                    .config(config)
                    .build()) {
                assertThat(sender.getDelegate()).isInstanceOf(SmtpEmailSender.class);
            }
        }

        @Test
        @DisplayName("没有sender抛出异常")
        void testBuilderWithoutSender() {
            assertThatThrownBy(() -> AsyncEmailSender.builder().build())
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("Sender is required");
        }

        @Test
        @DisplayName("配置线程池参数")
        void testBuilderWithThreadPoolConfig() {
            try (AsyncEmailSender sender = AsyncEmailSender.builder()
                    .sender(mockSender)
                    .useVirtualThreads(false)
                    .corePoolSize(5)
                    .maxPoolSize(10)
                    .queueCapacity(100)
                    .threadNamePrefix("email-test-")
                    .build()) {
                assertThat(sender.getExecutor()).isNotNull();
            }
        }

        @Test
        @DisplayName("使用虚拟线程")
        void testBuilderWithVirtualThreads() {
            try (AsyncEmailSender sender = AsyncEmailSender.builder()
                    .sender(mockSender)
                    .useVirtualThreads(true)
                    .build()) {
                assertThat(sender.getExecutor()).isNotNull();
            }
        }

        @Test
        @DisplayName("使用自定义执行器")
        void testBuilderWithCustomExecutor() {
            ExecutorService customExecutor = Executors.newFixedThreadPool(2);

            try (AsyncEmailSender sender = AsyncEmailSender.builder()
                    .sender(mockSender)
                    .executor(customExecutor)
                    .build()) {
                assertThat(sender.getExecutor()).isEqualTo(customExecutor);
            } finally {
                customExecutor.shutdown();
            }
        }

        @Test
        @DisplayName("配置重试执行器")
        void testBuilderWithRetryExecutor() {
            EmailRetryExecutor retryExecutor = EmailRetryExecutor.builder()
                    .maxRetries(5)
                    .build();

            try (AsyncEmailSender sender = AsyncEmailSender.builder()
                    .sender(mockSender)
                    .retryExecutor(retryExecutor)
                    .build()) {
                assertThat(sender).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("close() 测试")
    class CloseTests {

        @Test
        @DisplayName("关闭时关闭自有执行器")
        void testCloseShutdownsOwnedExecutor() throws Exception {
            AsyncEmailSender sender = new AsyncEmailSender(mockSender);
            ExecutorService executor = sender.getExecutor();

            sender.close();

            // Give some time for shutdown
            Thread.sleep(100);
            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("关闭时调用代理的close")
        void testCloseCallsDelegateClose() {
            AsyncEmailSender sender = new AsyncEmailSender(mockSender);

            sender.close();

            assertThat(mockSender.isClosed()).isTrue();
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("并发发送多封邮件")
        void testConcurrentSend() throws Exception {
            try (AsyncEmailSender sender = new AsyncEmailSender(mockSender)) {
                int emailCount = 100;
                List<CompletableFuture<Void>> futures = new ArrayList<>();

                for (int i = 0; i < emailCount; i++) {
                    futures.add(sender.sendAsync(createTestEmail()));
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .get(30, TimeUnit.SECONDS);

                assertThat(mockSender.getSentCount()).isEqualTo(emailCount);
            }
        }
    }

    private Email createTestEmail() {
        return Email.builder()
                .to("recipient@example.com")
                .subject("Test")
                .text("Content")
                .build();
    }

    private static class MockEmailSender implements EmailSender {
        private final AtomicInteger sentCount = new AtomicInteger(0);
        private boolean closed = false;

        @Override
        public void send(Email email) {
            sentCount.incrementAndGet();
        }

        @Override
        public SendResult sendWithResult(Email email) {
            send(email);
            return SendResult.success("<mock-id>");
        }

        public int getSentCount() {
            return sentCount.get();
        }

        @Override
        public void close() {
            closed = true;
        }

        public boolean isClosed() {
            return closed;
        }
    }
}
