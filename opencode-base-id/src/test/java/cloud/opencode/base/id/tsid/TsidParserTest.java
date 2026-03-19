package cloud.opencode.base.id.tsid;

import cloud.opencode.base.id.exception.OpenIdGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * TsidParser 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("TsidParser 测试")
class TsidParserTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("create工厂方法")
        void testCreate() {
            TsidParser parser = TsidParser.create();

            assertThat(parser).isNotNull();
        }

        @Test
        @DisplayName("单例模式")
        void testSingleton() {
            TsidParser parser1 = TsidParser.create();
            TsidParser parser2 = TsidParser.create();

            assertThat(parser1).isSameAs(parser2);
        }

        @Test
        @DisplayName("使用自定义epoch创建")
        void testCreateWithEpoch() {
            long epoch = 1609459200000L;
            TsidParser parser = TsidParser.create(epoch);

            assertThat(parser).isNotNull();
            assertThat(parser.getEpoch()).isEqualTo(epoch);
        }

        @Test
        @DisplayName("使用默认epoch创建返回单例")
        void testCreateWithDefaultEpoch() {
            TsidParser parser = TsidParser.create(TsidGenerator.DEFAULT_EPOCH);

            assertThat(parser).isSameAs(TsidParser.create());
        }
    }

    @Nested
    @DisplayName("解析字符串测试")
    class ParseStringTests {

        @Test
        @DisplayName("解析有效TSID字符串")
        void testParse() {
            TsidGenerator gen = TsidGenerator.create();
            TsidParser parser = TsidParser.create();
            String tsidStr = gen.generateStr();

            var parsed = parser.parse(tsidStr);

            assertThat(parsed).isNotNull();
            assertThat(parsed.tsidStr()).isEqualTo(tsidStr);
        }

        @Test
        @DisplayName("解析获取时间戳")
        void testParseTimestamp() {
            TsidGenerator gen = TsidGenerator.create();
            TsidParser parser = TsidParser.create();
            long before = System.currentTimeMillis();
            String tsidStr = gen.generateStr();
            long after = System.currentTimeMillis();

            var parsed = parser.parse(tsidStr);

            assertThat(parsed.timestampMillis()).isBetween(before, after);
        }

        @Test
        @DisplayName("解析无效字符串抛出异常")
        void testParseInvalid() {
            TsidParser parser = TsidParser.create();

            assertThatThrownBy(() -> parser.parse("invalid"))
                    .isInstanceOf(OpenIdGenerationException.class);
        }
    }

    @Nested
    @DisplayName("解析long测试")
    class ParseLongTests {

        @Test
        @DisplayName("解析有效TSID long")
        void testParseLong() {
            TsidGenerator gen = TsidGenerator.create();
            TsidParser parser = TsidParser.create();
            Long tsid = gen.generate();

            var parsed = parser.parse(tsid);

            assertThat(parsed).isNotNull();
            assertThat(parsed.tsid()).isEqualTo(tsid);
        }

        @Test
        @DisplayName("解析负数抛出异常")
        void testParseLongNegative() {
            TsidParser parser = TsidParser.create();

            assertThatThrownBy(() -> parser.parse(-1L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("提取时间戳测试")
    class ExtractTimestampTests {

        @Test
        @DisplayName("从字符串提取时间戳")
        void testExtractTimestampString() {
            TsidGenerator gen = TsidGenerator.create();
            TsidParser parser = TsidParser.create();
            long before = System.currentTimeMillis();
            String tsidStr = gen.generateStr();
            long after = System.currentTimeMillis();

            Instant timestamp = parser.extractTimestamp(tsidStr);

            assertThat(timestamp.toEpochMilli()).isBetween(before, after);
        }

        @Test
        @DisplayName("从long提取时间戳")
        void testExtractTimestampLong() {
            TsidGenerator gen = TsidGenerator.create();
            TsidParser parser = TsidParser.create();
            long before = System.currentTimeMillis();
            Long tsid = gen.generate();
            long after = System.currentTimeMillis();

            Instant timestamp = parser.extractTimestamp(tsid);

            assertThat(timestamp.toEpochMilli()).isBetween(before, after);
        }

        @Test
        @DisplayName("从无效字符串提取时间戳抛出异常")
        void testExtractTimestampInvalid() {
            TsidParser parser = TsidParser.create();

            assertThatThrownBy(() -> parser.extractTimestamp("invalid"))
                    .isInstanceOf(OpenIdGenerationException.class);
        }
    }

    @Nested
    @DisplayName("验证方法测试")
    class ValidationTests {

        @Test
        @DisplayName("验证有效字符串")
        void testIsValidStringTrue() {
            TsidGenerator gen = TsidGenerator.create();
            TsidParser parser = TsidParser.create();
            String tsidStr = gen.generateStr();

            assertThat(parser.isValid(tsidStr)).isTrue();
        }

        @Test
        @DisplayName("验证无效字符串")
        void testIsValidStringFalse() {
            TsidParser parser = TsidParser.create();

            assertThat(parser.isValid("invalid")).isFalse();
        }

        @Test
        @DisplayName("验证有效long")
        void testIsValidLongTrue() {
            TsidGenerator gen = TsidGenerator.create();
            TsidParser parser = TsidParser.create();
            Long tsid = gen.generate();

            assertThat(parser.isValid(tsid)).isTrue();
        }

        @Test
        @DisplayName("验证负数long")
        void testIsValidLongFalse() {
            TsidParser parser = TsidParser.create();

            assertThat(parser.isValid(-1L)).isFalse();
        }

        @Test
        @DisplayName("验证零")
        void testIsValidZero() {
            TsidParser parser = TsidParser.create();

            assertThat(parser.isValid(0L)).isTrue();
        }
    }

    @Nested
    @DisplayName("ParsedTsid测试")
    class ParsedTsidTests {

        @Test
        @DisplayName("获取时间戳二进制")
        void testGetTimestampBinary() {
            TsidGenerator gen = TsidGenerator.create();
            TsidParser parser = TsidParser.create();
            Long tsid = gen.generate();

            var parsed = parser.parse(tsid);

            assertThat(parsed.getTimestampBinary()).isNotNull();
            assertThat(parsed.getTimestampBinary()).hasSize(42);
        }

        @Test
        @DisplayName("获取随机二进制")
        void testGetRandomBinary() {
            TsidGenerator gen = TsidGenerator.create();
            TsidParser parser = TsidParser.create();
            Long tsid = gen.generate();

            var parsed = parser.parse(tsid);

            assertThat(parsed.getRandomBinary()).isNotNull();
            assertThat(parsed.getRandomBinary()).hasSize(22);
        }

        @Test
        @DisplayName("获取序列号")
        void testGetSequence() {
            TsidGenerator gen = TsidGenerator.create();
            TsidParser parser = TsidParser.create();
            Long tsid = gen.generate();

            var parsed = parser.parse(tsid);

            assertThat(parsed.getSequence()).isGreaterThanOrEqualTo(0);
            assertThat(parsed.getSequence()).isEqualTo(parsed.random());
        }

        @Test
        @DisplayName("toString方法")
        void testToString() {
            TsidGenerator gen = TsidGenerator.create();
            TsidParser parser = TsidParser.create();
            String tsidStr = gen.generateStr();

            var parsed = parser.parse(tsidStr);

            assertThat(parsed.toString()).contains(tsidStr);
            assertThat(parsed.toString()).contains("ParsedTsid");
        }

        @Test
        @DisplayName("时间字段")
        void testTime() {
            TsidGenerator gen = TsidGenerator.create();
            TsidParser parser = TsidParser.create();
            long before = System.currentTimeMillis();
            Long tsid = gen.generate();
            long after = System.currentTimeMillis();

            var parsed = parser.parse(tsid);

            // TSID时间精度为毫秒
            assertThat(parsed.time().toEpochMilli()).isBetween(before, after);
        }
    }
}
