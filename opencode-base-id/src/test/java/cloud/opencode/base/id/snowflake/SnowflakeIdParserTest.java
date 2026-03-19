package cloud.opencode.base.id.snowflake;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * SnowflakeIdParser 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("SnowflakeIdParser 测试")
class SnowflakeIdParserTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("createDefault工厂方法")
        void testCreateDefault() {
            SnowflakeIdParser parser = SnowflakeIdParser.createDefault();

            assertThat(parser).isNotNull();
        }

        @Test
        @DisplayName("create使用配置")
        void testCreateWithConfig() {
            SnowflakeConfig config = SnowflakeConfig.defaultConfig();
            SnowflakeIdParser parser = SnowflakeIdParser.create(config);

            assertThat(parser).isNotNull();
        }

        @Test
        @DisplayName("create使用自定义配置")
        void testCreateWithCustomConfig() {
            SnowflakeConfig config = SnowflakeConfig.of(5, 10);
            SnowflakeIdParser parser = SnowflakeIdParser.create(config);

            assertThat(parser).isNotNull();
        }
    }

    @Nested
    @DisplayName("解析测试")
    class ParseTests {

        @Test
        @DisplayName("解析有效ID")
        void testParseValid() {
            SnowflakeGenerator gen = SnowflakeGenerator.create(1, 2);
            SnowflakeIdParser parser = SnowflakeIdParser.create(gen.getConfig());
            long id = gen.generate();

            var parsed = parser.parse(id);

            assertThat(parsed).isNotNull();
            assertThat(parsed.id()).isEqualTo(id);
            assertThat(parsed.workerId()).isEqualTo(1);
            assertThat(parsed.datacenterId()).isEqualTo(2);
        }

        @Test
        @DisplayName("解析默认配置ID")
        void testParseDefaultConfig() {
            SnowflakeGenerator gen = SnowflakeGenerator.create();
            SnowflakeIdParser parser = SnowflakeIdParser.createDefault();
            long id = gen.generate();

            var parsed = parser.parse(id);

            assertThat(parsed).isNotNull();
            assertThat(parsed.id()).isEqualTo(id);
        }

        @Test
        @DisplayName("解析获取时间戳")
        void testParseTimestamp() {
            SnowflakeGenerator gen = SnowflakeGenerator.create();
            SnowflakeIdParser parser = SnowflakeIdParser.createDefault();
            long before = System.currentTimeMillis();
            long id = gen.generate();
            long after = System.currentTimeMillis();

            var parsed = parser.parse(id);

            assertThat(parsed.timestamp()).isBetween(before, after);
        }

        @Test
        @DisplayName("解析获取时间Instant")
        void testParseTime() {
            SnowflakeGenerator gen = SnowflakeGenerator.create();
            SnowflakeIdParser parser = SnowflakeIdParser.createDefault();
            long id = gen.generate();

            var parsed = parser.parse(id);

            assertThat(parsed.time()).isNotNull();
            assertThat(parsed.time()).isBeforeOrEqualTo(Instant.now());
        }

        @Test
        @DisplayName("解析获取序列号")
        void testParseSequence() {
            SnowflakeGenerator gen = SnowflakeGenerator.create();
            SnowflakeIdParser parser = SnowflakeIdParser.createDefault();
            long id = gen.generate();

            var parsed = parser.parse(id);

            assertThat(parsed.sequence()).isGreaterThanOrEqualTo(0);
            assertThat(parsed.sequence()).isLessThanOrEqualTo(4095);
        }

        @Test
        @DisplayName("解析null抛出异常")
        void testParseNull() {
            SnowflakeIdParser parser = SnowflakeIdParser.createDefault();

            assertThatThrownBy(() -> parser.parse(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("解析负数抛出异常")
        void testParseNegative() {
            SnowflakeIdParser parser = SnowflakeIdParser.createDefault();

            assertThatThrownBy(() -> parser.parse(-1L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("提取时间戳测试")
    class ExtractTimestampTests {

        @Test
        @DisplayName("提取时间戳")
        void testExtractTimestamp() {
            SnowflakeGenerator gen = SnowflakeGenerator.create();
            SnowflakeIdParser parser = SnowflakeIdParser.createDefault();
            long before = System.currentTimeMillis();
            long id = gen.generate();
            long after = System.currentTimeMillis();

            Instant timestamp = parser.extractTimestamp(id);

            assertThat(timestamp.toEpochMilli()).isBetween(before, after);
        }

        @Test
        @DisplayName("提取时间戳null抛出异常")
        void testExtractTimestampNull() {
            SnowflakeIdParser parser = SnowflakeIdParser.createDefault();

            assertThatThrownBy(() -> parser.extractTimestamp(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("提取时间戳负数抛出异常")
        void testExtractTimestampNegative() {
            SnowflakeIdParser parser = SnowflakeIdParser.createDefault();

            assertThatThrownBy(() -> parser.extractTimestamp(-1L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("验证测试")
    class ValidationTests {

        @Test
        @DisplayName("验证有效ID")
        void testIsValidTrue() {
            SnowflakeGenerator gen = SnowflakeGenerator.create();
            SnowflakeIdParser parser = SnowflakeIdParser.createDefault();
            long id = gen.generate();

            assertThat(parser.isValid(id)).isTrue();
        }

        @Test
        @DisplayName("验证零ID")
        void testIsValidZero() {
            SnowflakeIdParser parser = SnowflakeIdParser.createDefault();

            assertThat(parser.isValid(0L)).isTrue();
        }

        @Test
        @DisplayName("验证负数ID")
        void testIsValidNegative() {
            SnowflakeIdParser parser = SnowflakeIdParser.createDefault();

            assertThat(parser.isValid(-1L)).isFalse();
        }

        @Test
        @DisplayName("验证null")
        void testIsValidNull() {
            SnowflakeIdParser parser = SnowflakeIdParser.createDefault();

            assertThat(parser.isValid(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("ParsedId测试")
    class ParsedIdTests {

        @Test
        @DisplayName("ParsedId toString")
        void testParsedIdToString() {
            SnowflakeGenerator gen = SnowflakeGenerator.create(1, 2);
            SnowflakeIdParser parser = SnowflakeIdParser.create(gen.getConfig());
            long id = gen.generate();

            var parsed = parser.parse(id);
            String str = parsed.toString();

            assertThat(str).isNotNull();
            assertThat(str).contains("ParsedId");
            assertThat(str).contains("id=" + id);
            assertThat(str).contains("datacenter=2");
            assertThat(str).contains("worker=1");
        }

        @Test
        @DisplayName("ParsedId record方法")
        void testParsedIdRecordMethods() {
            SnowflakeGenerator gen = SnowflakeGenerator.create(3, 4);
            SnowflakeIdParser parser = SnowflakeIdParser.create(gen.getConfig());
            long id = gen.generate();

            var parsed = parser.parse(id);

            assertThat(parsed.id()).isEqualTo(id);
            assertThat(parsed.workerId()).isEqualTo(3);
            assertThat(parsed.datacenterId()).isEqualTo(4);
            assertThat(parsed.timestamp()).isGreaterThan(0);
            assertThat(parsed.time()).isNotNull();
            assertThat(parsed.sequence()).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("使用生成器内置解析器测试")
    class GeneratorParserTests {

        @Test
        @DisplayName("生成器parse方法")
        void testGeneratorParse() {
            SnowflakeGenerator gen = SnowflakeGenerator.create(5, 6);
            long id = gen.generate();

            var parsed = gen.parse(id);

            assertThat(parsed).isNotNull();
            assertThat(parsed.id()).isEqualTo(id);
            assertThat(parsed.workerId()).isEqualTo(5);
            assertThat(parsed.datacenterId()).isEqualTo(6);
        }
    }
}
