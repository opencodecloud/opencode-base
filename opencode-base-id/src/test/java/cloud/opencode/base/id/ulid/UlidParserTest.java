package cloud.opencode.base.id.ulid;

import cloud.opencode.base.id.exception.OpenIdGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * UlidParser 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("UlidParser 测试")
class UlidParserTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("create工厂方法")
        void testCreate() {
            UlidParser parser = UlidParser.create();

            assertThat(parser).isNotNull();
        }

        @Test
        @DisplayName("单例模式")
        void testSingleton() {
            UlidParser parser1 = UlidParser.create();
            UlidParser parser2 = UlidParser.create();

            assertThat(parser1).isSameAs(parser2);
        }
    }

    @Nested
    @DisplayName("解析方法测试")
    class ParseTests {

        @Test
        @DisplayName("解析有效ULID")
        void testParse() {
            UlidGenerator gen = UlidGenerator.create();
            UlidParser parser = UlidParser.create();
            String ulid = gen.generate();

            var parsed = parser.parse(ulid);

            assertThat(parsed).isNotNull();
            assertThat(parsed.ulid()).isEqualTo(ulid);
        }

        @Test
        @DisplayName("解析获取时间戳")
        void testParseTimestamp() {
            UlidGenerator gen = UlidGenerator.create();
            UlidParser parser = UlidParser.create();
            long before = System.currentTimeMillis();
            String ulid = gen.generate();
            long after = System.currentTimeMillis();

            var parsed = parser.parse(ulid);

            assertThat(parsed.timestamp()).isBetween(before, after);
        }

        @Test
        @DisplayName("解析获取时间")
        void testParseTime() {
            UlidGenerator gen = UlidGenerator.create();
            UlidParser parser = UlidParser.create();
            long before = System.currentTimeMillis();
            String ulid = gen.generate();
            long after = System.currentTimeMillis();

            var parsed = parser.parse(ulid);

            // ULID时间精度为毫秒
            assertThat(parsed.time().toEpochMilli()).isBetween(before, after);
        }

        @Test
        @DisplayName("解析获取随机性")
        void testParseRandomness() {
            UlidGenerator gen = UlidGenerator.create();
            UlidParser parser = UlidParser.create();
            String ulid = gen.generate();

            var parsed = parser.parse(ulid);

            assertThat(parsed.randomness()).isNotNull();
            assertThat(parsed.randomness()).hasSize(10);
        }

        @Test
        @DisplayName("解析无效ULID抛出异常")
        void testParseInvalid() {
            UlidParser parser = UlidParser.create();

            assertThatThrownBy(() -> parser.parse("invalid"))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        @DisplayName("解析null抛出异常")
        void testParseNull() {
            UlidParser parser = UlidParser.create();

            assertThatThrownBy(() -> parser.parse(null))
                    .isInstanceOf(OpenIdGenerationException.class);
        }
    }

    @Nested
    @DisplayName("提取时间戳测试")
    class ExtractTimestampTests {

        @Test
        @DisplayName("提取有效ULID时间戳")
        void testExtractTimestamp() {
            UlidGenerator gen = UlidGenerator.create();
            UlidParser parser = UlidParser.create();
            long before = System.currentTimeMillis();
            String ulid = gen.generate();
            long after = System.currentTimeMillis();

            Instant timestamp = parser.extractTimestamp(ulid);

            assertThat(timestamp.toEpochMilli()).isBetween(before, after);
        }

        @Test
        @DisplayName("提取无效ULID时间戳抛出异常")
        void testExtractTimestampInvalid() {
            UlidParser parser = UlidParser.create();

            assertThatThrownBy(() -> parser.extractTimestamp("invalid"))
                    .isInstanceOf(OpenIdGenerationException.class);
        }
    }

    @Nested
    @DisplayName("验证方法测试")
    class ValidationTests {

        @Test
        @DisplayName("验证有效ULID")
        void testIsValidTrue() {
            UlidGenerator gen = UlidGenerator.create();
            UlidParser parser = UlidParser.create();
            String ulid = gen.generate();

            assertThat(parser.isValid(ulid)).isTrue();
        }

        @Test
        @DisplayName("验证无效ULID")
        void testIsValidFalse() {
            UlidParser parser = UlidParser.create();

            assertThat(parser.isValid("invalid")).isFalse();
        }
    }

    @Nested
    @DisplayName("ParsedUlid测试")
    class ParsedUlidTests {

        @Test
        @DisplayName("获取时间戳部分")
        void testGetTimestampPart() {
            UlidGenerator gen = UlidGenerator.create();
            UlidParser parser = UlidParser.create();
            String ulid = gen.generate();

            var parsed = parser.parse(ulid);

            assertThat(parsed.getTimestampPart()).isEqualTo(ulid.substring(0, 10));
            assertThat(parsed.getTimestampPart()).hasSize(10);
        }

        @Test
        @DisplayName("获取随机性部分")
        void testGetRandomnessPart() {
            UlidGenerator gen = UlidGenerator.create();
            UlidParser parser = UlidParser.create();
            String ulid = gen.generate();

            var parsed = parser.parse(ulid);

            assertThat(parsed.getRandomnessPart()).isEqualTo(ulid.substring(10));
            assertThat(parsed.getRandomnessPart()).hasSize(16);
        }

        @Test
        @DisplayName("toString方法")
        void testToString() {
            UlidGenerator gen = UlidGenerator.create();
            UlidParser parser = UlidParser.create();
            String ulid = gen.generate();

            var parsed = parser.parse(ulid);

            assertThat(parsed.toString()).contains(ulid);
            assertThat(parsed.toString()).contains("ParsedUlid");
        }

        @Test
        @DisplayName("randomness返回副本")
        void testRandomnessReturnsCopy() {
            UlidGenerator gen = UlidGenerator.create();
            UlidParser parser = UlidParser.create();
            String ulid = gen.generate();

            var parsed = parser.parse(ulid);
            byte[] randomness1 = parsed.randomness();
            byte[] randomness2 = parsed.randomness();

            assertThat(randomness1).isNotSameAs(randomness2);
            assertThat(randomness1).isEqualTo(randomness2);
        }
    }
}
