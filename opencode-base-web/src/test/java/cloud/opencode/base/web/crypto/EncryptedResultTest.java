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
            EncryptedResult result = EncryptedResult.of("00000", "encryptedData", "AES-GCM");

            assertThat(result.code()).isEqualTo("00000");
            assertThat(result.encryptedData()).isEqualTo("encryptedData");
            assertThat(result.algorithm()).isEqualTo("AES-GCM");
            assertThat(result.timestamp()).isNotNull();
            assertThat(result.traceId()).isNull();
        }

        @Test
        @DisplayName("of with trace ID should create encrypted result with trace ID")
        void ofWithTraceIdShouldCreateEncryptedResultWithTraceId() {
            EncryptedResult result = EncryptedResult.of("00000", "encryptedData", "AES-GCM", "trace-123");

            assertThat(result.code()).isEqualTo("00000");
            assertThat(result.traceId()).isEqualTo("trace-123");
        }
    }

    @Nested
    @DisplayName("Success Check Tests")
    class SuccessCheckTests {

        @Test
        @DisplayName("isSuccess should return true for success code")
        void isSuccessShouldReturnTrueForSuccessCode() {
            EncryptedResult result = EncryptedResult.of("00000", "data", "AES");

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("isSuccess should return false for non-success code")
        void isSuccessShouldReturnFalseForNonSuccessCode() {
            EncryptedResult result = EncryptedResult.of("A0400", "data", "AES");

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
            EncryptedResult result1 = new EncryptedResult("00000", "data", "AES", now, "trace");
            EncryptedResult result2 = new EncryptedResult("00000", "data", "AES", now, "trace");

            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("hashCode should be consistent")
        void hashCodeShouldBeConsistent() {
            Instant now = Instant.now();
            EncryptedResult result1 = new EncryptedResult("00000", "data", "AES", now, "trace");
            EncryptedResult result2 = new EncryptedResult("00000", "data", "AES", now, "trace");

            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }

        @Test
        @DisplayName("toString should return string representation")
        void toStringShouldReturnStringRepresentation() {
            EncryptedResult result = EncryptedResult.of("00000", "data", "AES");

            assertThat(result.toString()).contains("00000", "data", "AES");
        }
    }
}
