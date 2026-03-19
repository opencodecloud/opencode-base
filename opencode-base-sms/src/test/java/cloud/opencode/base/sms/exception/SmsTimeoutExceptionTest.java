package cloud.opencode.base.sms.exception;

import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * SmsTimeoutExceptionTest Tests
 * SmsTimeoutExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SmsTimeoutException 测试")
class SmsTimeoutExceptionTest {

    @Nested
    @DisplayName("TimeoutType枚举测试")
    class TimeoutTypeTests {

        @Test
        @DisplayName("包含所有超时类型")
        void testAllTypes() {
            SmsTimeoutException.TimeoutType[] types = SmsTimeoutException.TimeoutType.values();

            assertThat(types).contains(
                    SmsTimeoutException.TimeoutType.CONNECTION,
                    SmsTimeoutException.TimeoutType.READ,
                    SmsTimeoutException.TimeoutType.TOTAL
            );
        }

        @Test
        @DisplayName("valueOf返回正确枚举")
        void testValueOf() {
            assertThat(SmsTimeoutException.TimeoutType.valueOf("CONNECTION"))
                    .isEqualTo(SmsTimeoutException.TimeoutType.CONNECTION);
        }
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用错误码、类型和超时创建")
        void testCreateBasic() {
            SmsTimeoutException exception = new SmsTimeoutException(
                    SmsErrorCode.CONNECTION_TIMEOUT,
                    SmsTimeoutException.TimeoutType.CONNECTION,
                    Duration.ofSeconds(10)
            );

            assertThat(exception.getTimeoutType()).isEqualTo(SmsTimeoutException.TimeoutType.CONNECTION);
            assertThat(exception.getTimeout()).isEqualTo(Duration.ofSeconds(10));
        }

        @Test
        @DisplayName("使用操作描述创建")
        void testCreateWithOperation() {
            SmsTimeoutException exception = new SmsTimeoutException(
                    SmsErrorCode.SEND_TIMEOUT,
                    SmsTimeoutException.TimeoutType.TOTAL,
                    Duration.ofSeconds(30),
                    "send SMS"
            );

            assertThat(exception.getOperation()).isEqualTo("send SMS");
            assertThat(exception.getMessage()).contains("send SMS");
        }

        @Test
        @DisplayName("使用原因创建")
        void testCreateWithCause() {
            Throwable cause = new RuntimeException("Timeout");
            SmsTimeoutException exception = new SmsTimeoutException(
                    SmsErrorCode.READ_TIMEOUT,
                    SmsTimeoutException.TimeoutType.READ,
                    Duration.ofSeconds(5),
                    cause
            );

            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("connectionTimeout工厂方法")
        void testConnectionTimeout() {
            SmsTimeoutException exception = SmsTimeoutException.connectionTimeout(Duration.ofSeconds(10));

            assertThat(exception.getErrorCode()).isEqualTo(SmsErrorCode.CONNECTION_TIMEOUT);
            assertThat(exception.getTimeoutType()).isEqualTo(SmsTimeoutException.TimeoutType.CONNECTION);
            assertThat(exception.getTimeout()).isEqualTo(Duration.ofSeconds(10));
        }

        @Test
        @DisplayName("connectionTimeout带原因")
        void testConnectionTimeoutWithCause() {
            Throwable cause = new RuntimeException("Error");
            SmsTimeoutException exception = SmsTimeoutException.connectionTimeout(Duration.ofSeconds(10), cause);

            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("readTimeout工厂方法")
        void testReadTimeout() {
            SmsTimeoutException exception = SmsTimeoutException.readTimeout(Duration.ofSeconds(30));

            assertThat(exception.getErrorCode()).isEqualTo(SmsErrorCode.READ_TIMEOUT);
            assertThat(exception.getTimeoutType()).isEqualTo(SmsTimeoutException.TimeoutType.READ);
        }

        @Test
        @DisplayName("readTimeout带原因")
        void testReadTimeoutWithCause() {
            Throwable cause = new RuntimeException("Error");
            SmsTimeoutException exception = SmsTimeoutException.readTimeout(Duration.ofSeconds(30), cause);

            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("sendTimeout工厂方法")
        void testSendTimeout() {
            SmsTimeoutException exception = SmsTimeoutException.sendTimeout(Duration.ofMinutes(1), "batch send");

            assertThat(exception.getErrorCode()).isEqualTo(SmsErrorCode.SEND_TIMEOUT);
            assertThat(exception.getTimeoutType()).isEqualTo(SmsTimeoutException.TimeoutType.TOTAL);
            assertThat(exception.getOperation()).isEqualTo("batch send");
        }
    }

    @Nested
    @DisplayName("getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("getTimeoutType返回超时类型")
        void testGetTimeoutType() {
            SmsTimeoutException exception = SmsTimeoutException.connectionTimeout(Duration.ofSeconds(10));

            assertThat(exception.getTimeoutType()).isEqualTo(SmsTimeoutException.TimeoutType.CONNECTION);
        }

        @Test
        @DisplayName("getTimeout返回超时时长")
        void testGetTimeout() {
            SmsTimeoutException exception = SmsTimeoutException.readTimeout(Duration.ofSeconds(5));

            assertThat(exception.getTimeout()).isEqualTo(Duration.ofSeconds(5));
        }

        @Test
        @DisplayName("getOperation返回操作")
        void testGetOperation() {
            SmsTimeoutException exception = SmsTimeoutException.sendTimeout(Duration.ofSeconds(30), "test op");

            assertThat(exception.getOperation()).isEqualTo("test op");
        }

        @Test
        @DisplayName("无操作返回null")
        void testGetOperationNull() {
            SmsTimeoutException exception = SmsTimeoutException.connectionTimeout(Duration.ofSeconds(10));

            assertThat(exception.getOperation()).isNull();
        }
    }
}
