package cloud.opencode.base.id;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * IdConverter 工具类测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("IdConverter 工具类测试")
class IdConverterTest {

    @Nested
    @DisplayName("Base62转换测试")
    class Base62Tests {

        @Test
        @DisplayName("toBase62转换正数")
        void testToBase62Positive() {
            String result = IdConverter.toBase62(12345L);

            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("toBase62转换零")
        void testToBase62Zero() {
            String result = IdConverter.toBase62(0L);

            assertThat(result).isEqualTo("0");
        }

        @Test
        @DisplayName("toBase62转换负数")
        void testToBase62Negative() {
            String result = IdConverter.toBase62(-12345L);

            assertThat(result).startsWith("-");
        }

        @Test
        @DisplayName("fromBase62转换")
        void testFromBase62() {
            String base62 = IdConverter.toBase62(12345L);

            long result = IdConverter.fromBase62(base62);

            assertThat(result).isEqualTo(12345L);
        }

        @Test
        @DisplayName("fromBase62空字符串抛出异常")
        void testFromBase62Empty() {
            assertThatThrownBy(() -> IdConverter.fromBase62(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("fromBase62 null抛出异常")
        void testFromBase62Null() {
            assertThatThrownBy(() -> IdConverter.fromBase62(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("fromBase62无效字符抛出异常")
        void testFromBase62InvalidChar() {
            assertThatThrownBy(() -> IdConverter.fromBase62("abc!@#"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Base62往返转换")
        void testBase62RoundTrip() {
            long original = 9876543210L;

            String base62 = IdConverter.toBase62(original);
            long result = IdConverter.fromBase62(base62);

            assertThat(result).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("Base36转换测试")
    class Base36Tests {

        @Test
        @DisplayName("toBase36转换")
        void testToBase36() {
            String result = IdConverter.toBase36(12345L);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(Long.toString(12345L, 36));
        }

        @Test
        @DisplayName("fromBase36转换")
        void testFromBase36() {
            String base36 = IdConverter.toBase36(12345L);

            long result = IdConverter.fromBase36(base36);

            assertThat(result).isEqualTo(12345L);
        }

        @Test
        @DisplayName("Base36往返转换")
        void testBase36RoundTrip() {
            long original = 9876543210L;

            String base36 = IdConverter.toBase36(original);
            long result = IdConverter.fromBase36(base36);

            assertThat(result).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("ULID/UUID转换测试")
    class UlidUuidTests {

        @Test
        @DisplayName("ulidToUuid转换")
        void testUlidToUuid() {
            String ulid = OpenId.ulid();

            UUID uuid = IdConverter.ulidToUuid(ulid);

            assertThat(uuid).isNotNull();
        }

        @Test
        @DisplayName("uuidToUlid转换")
        void testUuidToUlid() {
            UUID uuid = UUID.randomUUID();

            String ulid = IdConverter.uuidToUlid(uuid);

            assertThat(ulid).isNotNull();
            assertThat(ulid).hasSize(26);
        }

        @Test
        @DisplayName("ULID和UUID往返转换")
        void testUlidUuidRoundTrip() {
            String originalUlid = OpenId.ulid();

            UUID uuid = IdConverter.ulidToUuid(originalUlid);
            String resultUlid = IdConverter.uuidToUlid(uuid);

            assertThat(resultUlid).isEqualTo(originalUlid);
        }

        @Test
        @DisplayName("ulidToUuid无效ULID抛出异常")
        void testUlidToUuidInvalid() {
            assertThatThrownBy(() -> IdConverter.ulidToUuid("invalid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Snowflake Base62转换测试")
    class SnowflakeBase62Tests {

        @Test
        @DisplayName("snowflakeToBase62转换")
        void testSnowflakeToBase62() {
            long snowflakeId = OpenId.snowflakeId();

            String base62 = IdConverter.snowflakeToBase62(snowflakeId);

            assertThat(base62).isNotNull();
            assertThat(base62.length()).isLessThanOrEqualTo(11);
        }

        @Test
        @DisplayName("base62ToSnowflake转换")
        void testBase62ToSnowflake() {
            long originalId = OpenId.snowflakeId();
            String base62 = IdConverter.snowflakeToBase62(originalId);

            long result = IdConverter.base62ToSnowflake(base62);

            assertThat(result).isEqualTo(originalId);
        }
    }

    @Nested
    @DisplayName("UUID Base62转换测试")
    class UuidBase62Tests {

        @Test
        @DisplayName("uuidToBase62转换")
        void testUuidToBase62() {
            UUID uuid = UUID.randomUUID();

            String base62 = IdConverter.uuidToBase62(uuid);

            assertThat(base62).isNotNull();
            assertThat(base62).hasSize(22);
        }

        @Test
        @DisplayName("base62ToUuid转换")
        void testBase62ToUuid() {
            UUID original = UUID.randomUUID();
            String base62 = IdConverter.uuidToBase62(original);

            UUID result = IdConverter.base62ToUuid(base62);

            assertThat(result).isEqualTo(original);
        }

        @Test
        @DisplayName("UUID和Base62往返转换")
        void testUuidBase62RoundTrip() {
            UUID original = UUID.randomUUID();

            String base62 = IdConverter.uuidToBase62(original);
            UUID result = IdConverter.base62ToUuid(base62);

            assertThat(result).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("验证方法测试")
    class ValidationTests {

        @Test
        @DisplayName("isValidBase62有效字符串")
        void testIsValidBase62True() {
            String validBase62 = IdConverter.toBase62(12345L);

            assertThat(IdConverter.isValidBase62(validBase62)).isTrue();
        }

        @Test
        @DisplayName("isValidBase62无效字符串")
        void testIsValidBase62False() {
            assertThat(IdConverter.isValidBase62(null)).isFalse();
            assertThat(IdConverter.isValidBase62("")).isFalse();
            assertThat(IdConverter.isValidBase62("abc!@#")).isFalse();
        }

        @Test
        @DisplayName("isValidBase36有效字符串")
        void testIsValidBase36True() {
            String validBase36 = IdConverter.toBase36(12345L);

            assertThat(IdConverter.isValidBase36(validBase36)).isTrue();
        }

        @Test
        @DisplayName("isValidBase36无效字符串")
        void testIsValidBase36False() {
            assertThat(IdConverter.isValidBase36(null)).isFalse();
            assertThat(IdConverter.isValidBase36("")).isFalse();
        }
    }
}
