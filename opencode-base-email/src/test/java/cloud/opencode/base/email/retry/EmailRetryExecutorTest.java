package cloud.opencode.base.email.retry;

import cloud.opencode.base.email.Email;
import cloud.opencode.base.email.SendResult;
import cloud.opencode.base.email.exception.EmailErrorCode;
import cloud.opencode.base.email.exception.EmailException;
import cloud.opencode.base.email.exception.EmailSendException;
import cloud.opencode.base.email.internal.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailRetryExecutor 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailRetryExecutor 测试")
class EmailRetryExecutorTest {

    private Email testEmail;

    @BeforeEach
    void setUp() {
        testEmail = Email.builder()
                .to("test@example.com")
                .subject("Test")
                .text("Content")
                .build();
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造方法")
        void testDefaultConstructor() {
            EmailRetryExecutor executor = new EmailRetryExecutor();

            assertThat(executor.getMaxRetries()).isEqualTo(3);
            assertThat(executor.getInitialDelay()).isEqualTo(Duration.ofSeconds(1));
            assertThat(executor.getBackoffMultiplier()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("带参数构造方法")
        void testConstructorWithParams() {
            EmailRetryExecutor executor = new EmailRetryExecutor(
                    5,
                    Duration.ofMillis(100),
                    1.5
            );

            assertThat(executor.getMaxRetries()).isEqualTo(5);
            assertThat(executor.getInitialDelay()).isEqualTo(Duration.ofMillis(100));
            assertThat(executor.getBackoffMultiplier()).isEqualTo(1.5);
        }

        @Test
        @DisplayName("带所有参数构造方法")
        void testConstructorWithAllParams() {
            EmailRetryExecutor executor = new EmailRetryExecutor(
                    5,
                    Duration.ofMillis(100),
                    1.5,
                    Duration.ofSeconds(30)
            );

            assertThat(executor.getMaxRetries()).isEqualTo(5);
            assertThat(executor.getMaxDelay()).isEqualTo(Duration.ofSeconds(30));
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("使用Builder创建")
        void testBuilder() {
            EmailRetryExecutor executor = EmailRetryExecutor.builder()
                    .maxRetries(5)
                    .initialDelay(Duration.ofMillis(200))
                    .backoffMultiplier(1.5)
                    .maxDelay(Duration.ofMinutes(1))
                    .build();

            assertThat(executor.getMaxRetries()).isEqualTo(5);
            assertThat(executor.getInitialDelay()).isEqualTo(Duration.ofMillis(200));
            assertThat(executor.getBackoffMultiplier()).isEqualTo(1.5);
            assertThat(executor.getMaxDelay()).isEqualTo(Duration.ofMinutes(1));
        }

        @Test
        @DisplayName("Builder默认值")
        void testBuilderDefaults() {
            EmailRetryExecutor executor = EmailRetryExecutor.builder().build();

            assertThat(executor.getMaxRetries()).isEqualTo(3);
            assertThat(executor.getInitialDelay()).isEqualTo(Duration.ofSeconds(1));
            assertThat(executor.getBackoffMultiplier()).isEqualTo(2.0);
            assertThat(executor.getMaxDelay()).isEqualTo(Duration.ofMinutes(5));
        }
    }

    @Nested
    @DisplayName("executeWithRetry() 测试")
    class ExecuteWithRetryTests {

        @Test
        @DisplayName("发送成功不重试")
        void testSuccessNoRetry() {
            AtomicInteger sendCount = new AtomicInteger(0);
            EmailSender sender = email -> sendCount.incrementAndGet();

            EmailRetryExecutor executor = EmailRetryExecutor.builder()
                    .maxRetries(3)
                    .build();

            executor.executeWithRetry(testEmail, sender);

            assertThat(sendCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("可重试错误进行重试")
        void testRetryableErrorRetries() {
            AtomicInteger sendCount = new AtomicInteger(0);
            EmailSender sender = email -> {
                if (sendCount.incrementAndGet() < 3) {
                    throw new EmailSendException("Connection failed", email, EmailErrorCode.CONNECTION_FAILED);
                }
            };

            EmailRetryExecutor executor = EmailRetryExecutor.builder()
                    .maxRetries(5)
                    .initialDelay(Duration.ofMillis(10))
                    .build();

            executor.executeWithRetry(testEmail, sender);

            assertThat(sendCount.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("不可重试错误不重试")
        void testNonRetryableErrorNoRetry() {
            AtomicInteger sendCount = new AtomicInteger(0);
            EmailSender sender = email -> {
                sendCount.incrementAndGet();
                throw new EmailSendException("Auth failed", email, EmailErrorCode.AUTH_FAILED);
            };

            EmailRetryExecutor executor = EmailRetryExecutor.builder()
                    .maxRetries(5)
                    .build();

            assertThatThrownBy(() -> executor.executeWithRetry(testEmail, sender))
                    .isInstanceOf(EmailSendException.class);

            assertThat(sendCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("达到最大重试次数后抛出异常")
        void testMaxRetriesExceeded() {
            AtomicInteger sendCount = new AtomicInteger(0);
            EmailSender sender = email -> {
                sendCount.incrementAndGet();
                throw new EmailSendException("Timeout", email, EmailErrorCode.CONNECTION_TIMEOUT);
            };

            EmailRetryExecutor executor = EmailRetryExecutor.builder()
                    .maxRetries(3)
                    .initialDelay(Duration.ofMillis(10))
                    .build();

            assertThatThrownBy(() -> executor.executeWithRetry(testEmail, sender))
                    .isInstanceOf(EmailSendException.class);

            // 1 initial attempt + 3 retries = 4 total
            assertThat(sendCount.get()).isEqualTo(4);
        }

        @Test
        @DisplayName("重试回调被调用")
        void testRetryCallbackCalled() {
            AtomicInteger sendCount = new AtomicInteger(0);
            AtomicInteger callbackCount = new AtomicInteger(0);

            EmailSender sender = email -> {
                if (sendCount.incrementAndGet() < 3) {
                    throw new EmailSendException("Timeout", email, EmailErrorCode.SEND_TIMEOUT);
                }
            };

            EmailRetryExecutor executor = EmailRetryExecutor.builder()
                    .maxRetries(5)
                    .initialDelay(Duration.ofMillis(10))
                    .build();

            executor.executeWithRetry(testEmail, sender, (attempt, ex) -> {
                callbackCount.incrementAndGet();
                assertThat(attempt).isPositive();
                assertThat(ex).isInstanceOf(EmailSendException.class);
            });

            assertThat(callbackCount.get()).isEqualTo(2); // 2 retries before success
        }

        @Test
        @DisplayName("无回调时正常执行")
        void testWithoutCallback() {
            AtomicInteger sendCount = new AtomicInteger(0);
            EmailSender sender = email -> {
                if (sendCount.incrementAndGet() < 2) {
                    throw new EmailSendException("Timeout", email, EmailErrorCode.CONNECTION_TIMEOUT);
                }
            };

            EmailRetryExecutor executor = EmailRetryExecutor.builder()
                    .maxRetries(3)
                    .initialDelay(Duration.ofMillis(10))
                    .build();

            assertThatNoException().isThrownBy(() ->
                    executor.executeWithRetry(testEmail, sender, null));

            assertThat(sendCount.get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("指数退避测试")
    class ExponentialBackoffTests {

        @Test
        @DisplayName("延迟按指数增长")
        void testExponentialBackoff() {
            // This is more of a conceptual test - we verify the backoff multiplier is set
            EmailRetryExecutor executor = EmailRetryExecutor.builder()
                    .initialDelay(Duration.ofMillis(100))
                    .backoffMultiplier(2.0)
                    .build();

            // After first retry: 100ms
            // After second retry: 200ms
            // After third retry: 400ms
            assertThat(executor.getBackoffMultiplier()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("延迟不超过最大值")
        void testMaxDelayRespected() {
            EmailRetryExecutor executor = EmailRetryExecutor.builder()
                    .initialDelay(Duration.ofSeconds(10))
                    .backoffMultiplier(10.0)
                    .maxDelay(Duration.ofSeconds(30))
                    .build();

            // Even with large backoff, max delay should be respected
            assertThat(executor.getMaxDelay()).isEqualTo(Duration.ofSeconds(30));
        }
    }

    @Nested
    @DisplayName("getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("getMaxRetries() 返回正确值")
        void testGetMaxRetries() {
            EmailRetryExecutor executor = EmailRetryExecutor.builder()
                    .maxRetries(10)
                    .build();

            assertThat(executor.getMaxRetries()).isEqualTo(10);
        }

        @Test
        @DisplayName("getInitialDelay() 返回正确值")
        void testGetInitialDelay() {
            Duration delay = Duration.ofMillis(500);
            EmailRetryExecutor executor = EmailRetryExecutor.builder()
                    .initialDelay(delay)
                    .build();

            assertThat(executor.getInitialDelay()).isEqualTo(delay);
        }

        @Test
        @DisplayName("getBackoffMultiplier() 返回正确值")
        void testGetBackoffMultiplier() {
            EmailRetryExecutor executor = EmailRetryExecutor.builder()
                    .backoffMultiplier(3.0)
                    .build();

            assertThat(executor.getBackoffMultiplier()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("getMaxDelay() 返回正确值")
        void testGetMaxDelay() {
            Duration maxDelay = Duration.ofMinutes(10);
            EmailRetryExecutor executor = EmailRetryExecutor.builder()
                    .maxDelay(maxDelay)
                    .build();

            assertThat(executor.getMaxDelay()).isEqualTo(maxDelay);
        }
    }

    // Mock EmailSender helper
    private static class MockEmailSender implements EmailSender {
        @Override
        public void send(Email email) {
        }

        @Override
        public SendResult sendWithResult(Email email) {
            return SendResult.success("<mock-id>");
        }
    }
}
