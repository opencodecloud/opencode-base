package cloud.opencode.base.id.uuid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenUuid 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("OpenUuid 测试")
class OpenUuidTest {

    @Nested
    @DisplayName("生成方法测试")
    class GenerationTests {

        @Test
        @DisplayName("randomUuid方法")
        void testRandomUuid() {
            UUID uuid = OpenUuid.randomUuid();

            assertThat(uuid).isNotNull();
            assertThat(uuid.version()).isEqualTo(4);
        }

        @Test
        @DisplayName("timeOrderedUuid方法")
        void testTimeOrderedUuid() {
            UUID uuid = OpenUuid.timeOrderedUuid();

            assertThat(uuid).isNotNull();
            assertThat(uuid.version()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("字符串转换测试")
    class StringConversionTests {

        @Test
        @DisplayName("toSimpleString方法")
        void testToSimpleString() {
            UUID uuid = UUID.randomUUID();

            String simple = OpenUuid.toSimpleString(uuid);

            assertThat(simple).isNotNull();
            assertThat(simple).hasSize(32);
            assertThat(simple).doesNotContain("-");
        }

        @Test
        @DisplayName("fromSimpleString方法")
        void testFromSimpleString() {
            UUID original = UUID.randomUUID();
            String simple = OpenUuid.toSimpleString(original);

            UUID parsed = OpenUuid.fromSimpleString(simple);

            assertThat(parsed).isEqualTo(original);
        }

        @Test
        @DisplayName("fromSimpleString无效长度抛出异常")
        void testFromSimpleStringInvalidLength() {
            assertThatThrownBy(() -> OpenUuid.fromSimpleString("short"))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> OpenUuid.fromSimpleString(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("字符串转换往返")
        void testStringRoundTrip() {
            UUID original = UUID.randomUUID();
            String simple = OpenUuid.toSimpleString(original);
            UUID parsed = OpenUuid.fromSimpleString(simple);

            assertThat(parsed).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("字节转换测试")
    class ByteConversionTests {

        @Test
        @DisplayName("toBytes方法")
        void testToBytes() {
            UUID uuid = UUID.randomUUID();

            byte[] bytes = OpenUuid.toBytes(uuid);

            assertThat(bytes).isNotNull();
            assertThat(bytes).hasSize(16);
        }

        @Test
        @DisplayName("fromBytes方法")
        void testFromBytes() {
            UUID original = UUID.randomUUID();
            byte[] bytes = OpenUuid.toBytes(original);

            UUID parsed = OpenUuid.fromBytes(bytes);

            assertThat(parsed).isEqualTo(original);
        }

        @Test
        @DisplayName("fromBytes无效长度抛出异常")
        void testFromBytesInvalidLength() {
            assertThatThrownBy(() -> OpenUuid.fromBytes(new byte[10]))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> OpenUuid.fromBytes(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("字节转换往返")
        void testBytesRoundTrip() {
            UUID original = UUID.randomUUID();
            byte[] bytes = OpenUuid.toBytes(original);
            UUID parsed = OpenUuid.fromBytes(bytes);

            assertThat(parsed).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("版本和变体测试")
    class VersionVariantTests {

        @Test
        @DisplayName("getVersion方法")
        void testGetVersion() {
            UUID uuidV4 = UUID.randomUUID();
            UUID uuidV7 = OpenUuid.timeOrderedUuid();

            assertThat(OpenUuid.getVersion(uuidV4)).isEqualTo(4);
            assertThat(OpenUuid.getVersion(uuidV7)).isEqualTo(7);
        }

        @Test
        @DisplayName("getVariant方法")
        void testGetVariant() {
            UUID uuid = UUID.randomUUID();

            assertThat(OpenUuid.getVariant(uuid)).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("验证方法测试")
    class ValidationTests {

        @Test
        @DisplayName("isValid有效UUID")
        void testIsValidTrue() {
            UUID uuid = UUID.randomUUID();

            assertThat(OpenUuid.isValid(uuid.toString())).isTrue();
        }

        @Test
        @DisplayName("isValid无效UUID")
        void testIsValidFalse() {
            assertThat(OpenUuid.isValid("invalid")).isFalse();
            assertThat(OpenUuid.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("isValidSimple有效简化UUID")
        void testIsValidSimpleTrue() {
            UUID uuid = UUID.randomUUID();
            String simple = OpenUuid.toSimpleString(uuid);

            assertThat(OpenUuid.isValidSimple(simple)).isTrue();
        }

        @Test
        @DisplayName("isValidSimple无效简化UUID")
        void testIsValidSimpleFalse() {
            assertThat(OpenUuid.isValidSimple("invalid")).isFalse();
            assertThat(OpenUuid.isValidSimple(null)).isFalse();
            assertThat(OpenUuid.isValidSimple("ghijklmnopqrstuvwxyz123456789012")).isFalse();
        }
    }

    @Nested
    @DisplayName("时间戳提取测试")
    class TimestampExtractionTests {

        @Test
        @DisplayName("extractTimestamp v7")
        void testExtractTimestampV7() {
            long before = System.currentTimeMillis();
            UUID uuid = OpenUuid.timeOrderedUuid();
            long after = System.currentTimeMillis();

            long timestamp = OpenUuid.extractTimestamp(uuid);

            assertThat(timestamp).isBetween(before, after);
        }

        @Test
        @DisplayName("extractTimestamp非v7返回-1")
        void testExtractTimestampNonV7() {
            UUID uuid = UUID.randomUUID();

            long timestamp = OpenUuid.extractTimestamp(uuid);

            assertThat(timestamp).isEqualTo(-1);
        }
    }
}
