package cloud.opencode.base.serialization;

import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * SerializationResultTest Tests
 * SerializationResultTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.3
 */
@DisplayName("SerializationResult Tests")
class SerializationResultTest {

    private static final byte[] SAMPLE_DATA = "hello world".getBytes(StandardCharsets.UTF_8);
    private static final String JSON_FORMAT = "json";

    @Nested
    @DisplayName("of() Factory Method Tests")
    class OfTests {

        @Test
        @DisplayName("of(byte[], String) should create result with zero duration")
        void ofShouldCreateResultWithZeroDuration() {
            SerializationResult result = SerializationResult.of(SAMPLE_DATA, JSON_FORMAT);

            assertThat(result).isNotNull();
            assertThat(result.data()).isEqualTo(SAMPLE_DATA);
            assertThat(result.format()).isEqualTo(JSON_FORMAT);
            assertThat(result.durationNanos()).isZero();
            assertThat(result.compressed()).isFalse();
        }

        @Test
        @DisplayName("of(byte[], String, long) should create result with specified duration")
        void ofWithDurationShouldCreateResultWithSpecifiedDuration() {
            long duration = 12345L;

            SerializationResult result = SerializationResult.of(SAMPLE_DATA, JSON_FORMAT, duration);

            assertThat(result.data()).isEqualTo(SAMPLE_DATA);
            assertThat(result.format()).isEqualTo(JSON_FORMAT);
            assertThat(result.durationNanos()).isEqualTo(duration);
            assertThat(result.compressed()).isFalse();
        }

        @Test
        @DisplayName("of should preserve data reference")
        void ofShouldPreserveDataReference() {
            byte[] data = {1, 2, 3};
            SerializationResult result = SerializationResult.of(data, "binary");

            assertThat(result.data()).isEqualTo(data);
        }
    }

    @Nested
    @DisplayName("timed() Auto-Timing Tests")
    class TimedTests {

        @Test
        @DisplayName("timed should measure execution time")
        void timedShouldMeasureExecutionTime() {
            SerializationResult result = SerializationResult.timed(
                    () -> SAMPLE_DATA, JSON_FORMAT);

            assertThat(result.data()).isEqualTo(SAMPLE_DATA);
            assertThat(result.format()).isEqualTo(JSON_FORMAT);
            assertThat(result.durationNanos()).isGreaterThanOrEqualTo(0L);
            assertThat(result.compressed()).isFalse();
        }

        @Test
        @DisplayName("timed should capture non-trivial duration for slow supplier")
        void timedShouldCaptureNonTrivialDuration() {
            SerializationResult result = SerializationResult.timed(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return SAMPLE_DATA;
            }, JSON_FORMAT);

            // At least a few milliseconds (in nanos)
            assertThat(result.durationNanos()).isGreaterThan(1_000_000L);
        }

        @Test
        @DisplayName("timed should return correct data from supplier")
        void timedShouldReturnCorrectDataFromSupplier() {
            byte[] expected = {10, 20, 30};
            SerializationResult result = SerializationResult.timed(() -> expected, "binary");

            assertThat(result.data()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Getter / Accessor Tests")
    class GetterTests {

        @Test
        @DisplayName("data() should return the byte array")
        void dataShouldReturnByteArray() {
            SerializationResult result = new SerializationResult(SAMPLE_DATA, JSON_FORMAT, -1, false, 100L);

            assertThat(result.data()).isEqualTo(SAMPLE_DATA);
        }

        @Test
        @DisplayName("data() should return a defensive copy")
        void dataShouldReturnDefensiveCopy() {
            SerializationResult result = new SerializationResult(SAMPLE_DATA, JSON_FORMAT, -1, false, 100L);

            byte[] copy1 = result.data();
            byte[] copy2 = result.data();
            assertThat(copy1).isNotSameAs(copy2); // different array instances
            assertThat(copy1).isEqualTo(copy2);   // same content
        }

        @Test
        @DisplayName("dataUnsafe() should return the same internal array every time")
        void dataUnsafeShouldReturnSameArray() {
            SerializationResult result = new SerializationResult(SAMPLE_DATA, JSON_FORMAT, -1, false, 100L);

            byte[] ref1 = result.dataUnsafe();
            byte[] ref2 = result.dataUnsafe();
            assertThat(ref1).isSameAs(ref2); // same array instance
            assertThat(ref1).isEqualTo(SAMPLE_DATA);
        }

        @Test
        @DisplayName("format() should return the format string")
        void formatShouldReturnFormatString() {
            SerializationResult result = new SerializationResult(SAMPLE_DATA, "xml", -1, false, 0L);

            assertThat(result.format()).isEqualTo("xml");
        }

        @Test
        @DisplayName("durationNanos() should return the duration")
        void durationNanosShouldReturnDuration() {
            SerializationResult result = new SerializationResult(SAMPLE_DATA, JSON_FORMAT, -1, false, 999L);

            assertThat(result.durationNanos()).isEqualTo(999L);
        }

        @Test
        @DisplayName("compressed() should return compression flag")
        void compressedShouldReturnCompressionFlag() {
            SerializationResult compressed = new SerializationResult(SAMPLE_DATA, JSON_FORMAT, -1, true, 0L);
            SerializationResult uncompressed = new SerializationResult(SAMPLE_DATA, JSON_FORMAT, -1, false, 0L);

            assertThat(compressed.compressed()).isTrue();
            assertThat(uncompressed.compressed()).isFalse();
        }

        @Test
        @DisplayName("size() should return data length")
        void sizeShouldReturnDataLength() {
            SerializationResult result = SerializationResult.of(SAMPLE_DATA, JSON_FORMAT);

            assertThat(result.size()).isEqualTo(SAMPLE_DATA.length);
        }

        @Test
        @DisplayName("asString() should convert data to UTF-8 string")
        void asStringShouldConvertDataToUtf8String() {
            byte[] data = "Hello, World!".getBytes(StandardCharsets.UTF_8);
            SerializationResult result = SerializationResult.of(data, JSON_FORMAT);

            assertThat(result.asString()).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("asString() should handle UTF-8 multibyte characters")
        void asStringShouldHandleUtf8MultibyteCharacters() {
            String original = "Hello, \u4e16\u754c!";
            byte[] data = original.getBytes(StandardCharsets.UTF_8);
            SerializationResult result = SerializationResult.of(data, JSON_FORMAT);

            assertThat(result.asString()).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty byte array")
        void shouldHandleEmptyByteArray() {
            byte[] empty = new byte[0];
            SerializationResult result = SerializationResult.of(empty, JSON_FORMAT);

            assertThat(result.data()).isEmpty();
            assertThat(result.size()).isZero();
            assertThat(result.asString()).isEmpty();
        }

        @Test
        @DisplayName("should handle single byte data")
        void shouldHandleSingleByteData() {
            byte[] single = {42};
            SerializationResult result = SerializationResult.of(single, JSON_FORMAT);

            assertThat(result.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("should handle large data")
        void shouldHandleLargeData() {
            byte[] large = new byte[1024 * 1024]; // 1MB
            SerializationResult result = SerializationResult.of(large, "binary");

            assertThat(result.size()).isEqualTo(1024 * 1024);
        }

        @Test
        @DisplayName("record equals should work for identical results")
        void recordEqualsShouldWorkForIdenticalResults() {
            byte[] data = {1, 2, 3};
            SerializationResult result1 = new SerializationResult(data, JSON_FORMAT, -1, false, 100L);
            SerializationResult result2 = new SerializationResult(data, JSON_FORMAT, -1, false, 100L);

            assertThat(result1).isEqualTo(result2);
            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }

        @Test
        @DisplayName("record equals should differ when format differs")
        void recordEqualsShouldDifferWhenFormatDiffers() {
            SerializationResult result1 = SerializationResult.of(SAMPLE_DATA, "json");
            SerializationResult result2 = SerializationResult.of(SAMPLE_DATA, "xml");

            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("record toString should contain field information")
        void recordToStringShouldContainFieldInformation() {
            SerializationResult result = SerializationResult.of(SAMPLE_DATA, JSON_FORMAT);

            String str = result.toString();
            assertThat(str).contains("SerializationResult");
            assertThat(str).contains(JSON_FORMAT);
        }

        @Test
        @DisplayName("zero duration nanos should be valid")
        void zeroDurationNanosShouldBeValid() {
            SerializationResult result = new SerializationResult(SAMPLE_DATA, JSON_FORMAT, -1, false, 0L);

            assertThat(result.durationNanos()).isZero();
        }
    }
}
