package cloud.opencode.base.event.saga;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * SagaResult 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("SagaResult 测试")
class SagaResultTest {

    @Nested
    @DisplayName("记录构造测试")
    class RecordConstructorTests {

        @Test
        @DisplayName("成功结果构造")
        void testSuccessResult() {
            Instant start = Instant.now();
            Instant end = start.plusMillis(100);

            SagaResult<String> result = new SagaResult<>(
                    "saga-123",
                    "order-saga",
                    SagaStatus.COMPLETED,
                    "context",
                    start,
                    end,
                    null,
                    null
            );

            assertThat(result.sagaId()).isEqualTo("saga-123");
            assertThat(result.sagaName()).isEqualTo("order-saga");
            assertThat(result.status()).isEqualTo(SagaStatus.COMPLETED);
            assertThat(result.context()).isEqualTo("context");
            assertThat(result.startTime()).isEqualTo(start);
            assertThat(result.endTime()).isEqualTo(end);
            assertThat(result.failedStep()).isNull();
            assertThat(result.error()).isNull();
        }

        @Test
        @DisplayName("失败结果构造")
        void testFailureResult() {
            RuntimeException error = new RuntimeException("Failed");

            SagaResult<String> result = new SagaResult<>(
                    "saga-456",
                    "payment-saga",
                    SagaStatus.COMPENSATED,
                    "context",
                    Instant.now(),
                    Instant.now(),
                    "process-payment",
                    error
            );

            assertThat(result.status()).isEqualTo(SagaStatus.COMPENSATED);
            assertThat(result.failedStep()).isEqualTo("process-payment");
            assertThat(result.error()).isEqualTo(error);
        }
    }

    @Nested
    @DisplayName("isSuccess() 测试")
    class IsSuccessTests {

        @Test
        @DisplayName("COMPLETED返回true")
        void testCompletedIsSuccess() {
            SagaResult<String> result = new SagaResult<>(
                    "id", "name", SagaStatus.COMPLETED, "ctx",
                    Instant.now(), Instant.now(), null, null);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("其他状态返回false")
        void testOtherStatusesNotSuccess() {
            for (SagaStatus status : new SagaStatus[]{
                    SagaStatus.FAILED, SagaStatus.COMPENSATED, SagaStatus.RUNNING, SagaStatus.PENDING}) {
                SagaResult<String> result = new SagaResult<>(
                        "id", "name", status, "ctx",
                        Instant.now(), Instant.now(), null, null);

                assertThat(result.isSuccess()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("isCompensated() 测试")
    class IsCompensatedTests {

        @Test
        @DisplayName("COMPENSATED返回true")
        void testCompensatedReturnsTrue() {
            SagaResult<String> result = new SagaResult<>(
                    "id", "name", SagaStatus.COMPENSATED, "ctx",
                    Instant.now(), Instant.now(), "step1", new RuntimeException());

            assertThat(result.isCompensated()).isTrue();
        }

        @Test
        @DisplayName("其他状态返回false")
        void testOtherStatusesNotCompensated() {
            SagaResult<String> result = new SagaResult<>(
                    "id", "name", SagaStatus.COMPLETED, "ctx",
                    Instant.now(), Instant.now(), null, null);

            assertThat(result.isCompensated()).isFalse();
        }
    }

    @Nested
    @DisplayName("isFailed() 测试")
    class IsFailedTests {

        @Test
        @DisplayName("FAILED返回true")
        void testFailedReturnsTrue() {
            SagaResult<String> result = new SagaResult<>(
                    "id", "name", SagaStatus.FAILED, "ctx",
                    Instant.now(), Instant.now(), null, new RuntimeException());

            assertThat(result.isFailed()).isTrue();
        }

        @Test
        @DisplayName("COMPENSATED也返回true")
        void testCompensatedAlsoFailed() {
            SagaResult<String> result = new SagaResult<>(
                    "id", "name", SagaStatus.COMPENSATED, "ctx",
                    Instant.now(), Instant.now(), "step1", new RuntimeException());

            assertThat(result.isFailed()).isTrue();
        }

        @Test
        @DisplayName("COMPLETED返回false")
        void testCompletedNotFailed() {
            SagaResult<String> result = new SagaResult<>(
                    "id", "name", SagaStatus.COMPLETED, "ctx",
                    Instant.now(), Instant.now(), null, null);

            assertThat(result.isFailed()).isFalse();
        }
    }

    @Nested
    @DisplayName("getDuration() 测试")
    class GetDurationTests {

        @Test
        @DisplayName("返回正确的执行时长")
        void testReturnsDuration() {
            Instant start = Instant.now();
            Instant end = start.plusMillis(500);

            SagaResult<String> result = new SagaResult<>(
                    "id", "name", SagaStatus.COMPLETED, "ctx",
                    start, end, null, null);

            assertThat(result.getDuration()).isEqualTo(Duration.ofMillis(500));
        }
    }

    @Nested
    @DisplayName("getErrorMessage() 测试")
    class GetErrorMessageTests {

        @Test
        @DisplayName("有错误返回消息")
        void testReturnsErrorMessage() {
            RuntimeException error = new RuntimeException("Something went wrong");

            SagaResult<String> result = new SagaResult<>(
                    "id", "name", SagaStatus.FAILED, "ctx",
                    Instant.now(), Instant.now(), "step1", error);

            assertThat(result.getErrorMessage()).isEqualTo("Something went wrong");
        }

        @Test
        @DisplayName("无错误返回null")
        void testReturnsNullWhenNoError() {
            SagaResult<String> result = new SagaResult<>(
                    "id", "name", SagaStatus.COMPLETED, "ctx",
                    Instant.now(), Instant.now(), null, null);

            assertThat(result.getErrorMessage()).isNull();
        }
    }
}
