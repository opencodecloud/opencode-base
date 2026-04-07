package cloud.opencode.base.web.crypto;

import org.junit.jupiter.api.*;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * EncryptedResultTest Tests
 * EncryptedResultTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("EncryptedResult Tests")
class EncryptedResultTest {

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("of should create encrypted result")
        void ofShouldCreateEncryptedResult() {
            EncryptedResult result = EncryptedResult.of("00000", "Success", "encryptedData", "AES-GCM");

            assertThat(result.code()).isEqualTo("00000");
            assertThat(result.message()).isEqualTo("Success");
            assertThat(result.encryptedData()).isEqualTo("encryptedData");
            assertThat(result.algorithm()).isEqualTo("AES-GCM");
            assertThat(result.timestamp()).isNotNull();
            assertThat(result.traceId()).isNull();
            assertThat(result.sign()).isNull();
        }

        @Test
        @DisplayName("of with trace ID should create encrypted result with trace ID")
        void ofWithTraceIdShouldCreateEncryptedResultWithTraceId() {
            EncryptedResult result = EncryptedResult.of("00000", "Success", "encryptedData", "AES-GCM", "trace-123");

            assertThat(result.code()).isEqualTo("00000");
            assertThat(result.message()).isEqualTo("Success");
            assertThat(result.traceId()).isEqualTo("trace-123");
            assertThat(result.sign()).isNull();
        }

        @Test
        @DisplayName("withSign should create signed copy")
        void withSignShouldCreateSignedCopy() {
            EncryptedResult unsigned = EncryptedResult.of("00000", "Success", "data", "AES-GCM");
            EncryptedResult signed = unsigned.withSign("hmac-signature");

            assertThat(signed.sign()).isEqualTo("hmac-signature");
            assertThat(signed.code()).isEqualTo(unsigned.code());
            assertThat(signed.message()).isEqualTo(unsigned.message());
            assertThat(signed.encryptedData()).isEqualTo(unsigned.encryptedData());
            assertThat(signed.algorithm()).isEqualTo(unsigned.algorithm());
            assertThat(signed.timestamp()).isEqualTo(unsigned.timestamp());
        }
    }

    @Nested
    @DisplayName("Sign Payload Tests")
    class SignPayloadTests {

        @Test
        @DisplayName("signPayload should include all fields except sign")
        void signPayloadShouldIncludeAllFields() {
            Instant now = Instant.parse("2026-03-25T08:00:00Z");
            EncryptedResult result = new EncryptedResult("00000", "Success", "data", "AES", now, "trace-1", "sig");

            String payload = result.signPayload();
            assertThat(payload).contains("00000", "Success", "data", "AES", "2026-03-25T08:00:00Z", "trace-1");
            assertThat(payload).doesNotContain("sig");
        }

        @Test
        @DisplayName("signPayload should handle null traceId and message")
        void signPayloadShouldHandleNulls() {
            Instant now = Instant.parse("2026-03-25T08:00:00Z");
            EncryptedResult result = new EncryptedResult("00000", null, "data", "AES", now, null, null);

            String payload = result.signPayload();
            assertThat(payload).isNotNull();
            assertThat(payload).contains("00000", "data", "AES");
        }
    }

    @Nested
    @DisplayName("Success Check Tests")
    class SuccessCheckTests {

        @Test
        @DisplayName("isSuccess should return true for success code")
        void isSuccessShouldReturnTrueForSuccessCode() {
            EncryptedResult result = EncryptedResult.of("00000", "Success", "data", "AES");

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("isSuccess should return false for non-success code")
        void isSuccessShouldReturnFalseForNonSuccessCode() {
            EncryptedResult result = EncryptedResult.of("A0400", "Bad Request", "data", "AES");

            assertThat(result.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("Record Methods Tests")
    class RecordMethodsTests {

        @Test
        @DisplayName("equals should compare correctly")
        void equalsShouldCompareCorrectly() {
            Instant now = Instant.now();
            EncryptedResult result1 = new EncryptedResult("00000", "Success", "data", "AES", now, "trace", "sig");
            EncryptedResult result2 = new EncryptedResult("00000", "Success", "data", "AES", now, "trace", "sig");

            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("hashCode should be consistent")
        void hashCodeShouldBeConsistent() {
            Instant now = Instant.now();
            EncryptedResult result1 = new EncryptedResult("00000", "Success", "data", "AES", now, "trace", "sig");
            EncryptedResult result2 = new EncryptedResult("00000", "Success", "data", "AES", now, "trace", "sig");

            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }

        @Test
        @DisplayName("toString should return string representation")
        void toStringShouldReturnStringRepresentation() {
            EncryptedResult result = EncryptedResult.of("00000", "Success", "data", "AES");

            assertThat(result.toString()).contains("00000", "Success", "data", "AES");
        }
    }
}
