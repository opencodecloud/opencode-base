package cloud.opencode.base.sms.message;

import org.junit.jupiter.api.*;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * SmsResultTest Tests
 * SmsResultTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SmsResult 测试")
class SmsResultTest {

    @Nested
    @DisplayName("success方法测试")
    class SuccessTests {

        @Test
        @DisplayName("创建成功结果-带messageId")
        void testSuccessWithMessageId() {
            SmsResult result = SmsResult.success("MSG123");

            assertThat(result.success()).isTrue();
            assertThat(result.messageId()).isEqualTo("MSG123");
            assertThat(result.errorCode()).isNull();
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("创建成功结果-带messageId和phoneNumber")
        void testSuccessWithPhoneNumber() {
            SmsResult result = SmsResult.success("MSG123", "13800138000");

            assertThat(result.success()).isTrue();
            assertThat(result.messageId()).isEqualTo("MSG123");
            assertThat(result.phoneNumber()).isEqualTo("13800138000");
        }

        @Test
        @DisplayName("成功结果timestamp不为null")
        void testSuccessTimestamp() {
            Instant before = Instant.now();
            SmsResult result = SmsResult.success("MSG123");
            Instant after = Instant.now();

            assertThat(result.timestamp()).isNotNull();
            assertThat(result.timestamp()).isBetween(before, after.plusMillis(1));
        }
    }

    @Nested
    @DisplayName("failure方法测试")
    class FailureTests {

        @Test
        @DisplayName("创建失败结果-带errorCode和message")
        void testFailureSimple() {
            SmsResult result = SmsResult.failure("ERR001", "Send failed");

            assertThat(result.success()).isFalse();
            assertThat(result.errorCode()).isEqualTo("ERR001");
            assertThat(result.errorMessage()).isEqualTo("Send failed");
            assertThat(result.messageId()).isNull();
        }

        @Test
        @DisplayName("创建失败结果-带phoneNumber")
        void testFailureWithPhoneNumber() {
            SmsResult result = SmsResult.failure("13800138000", "ERR001", "Send failed");

            assertThat(result.success()).isFalse();
            assertThat(result.phoneNumber()).isEqualTo("13800138000");
            assertThat(result.errorCode()).isEqualTo("ERR001");
        }
    }

    @Nested
    @DisplayName("isFailed方法测试")
    class IsFailedTests {

        @Test
        @DisplayName("成功结果返回false")
        void testIsFailedOnSuccess() {
            SmsResult result = SmsResult.success("MSG123");

            assertThat(result.isFailed()).isFalse();
        }

        @Test
        @DisplayName("失败结果返回true")
        void testIsFailedOnFailure() {
            SmsResult result = SmsResult.failure("ERR001", "Failed");

            assertThat(result.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordTests {

        @Test
        @DisplayName("equals和hashCode")
        void testEqualsAndHashCode() {
            SmsResult r1 = new SmsResult(true, "MSG1", "13800138000", null, null, Instant.EPOCH);
            SmsResult r2 = new SmsResult(true, "MSG1", "13800138000", null, null, Instant.EPOCH);

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("不同结果不相等")
        void testNotEquals() {
            SmsResult r1 = SmsResult.success("MSG1");
            SmsResult r2 = SmsResult.success("MSG2");

            assertThat(r1).isNotEqualTo(r2);
        }

        @Test
        @DisplayName("toString包含关键信息")
        void testToString() {
            SmsResult result = SmsResult.success("MSG123", "13800138000");
            String str = result.toString();

            assertThat(str).contains("MSG123");
            assertThat(str).contains("true");
        }
    }
}
