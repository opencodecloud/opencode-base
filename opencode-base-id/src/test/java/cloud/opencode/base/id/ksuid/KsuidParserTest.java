package cloud.opencode.base.id.ksuid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * KsuidParser 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("KsuidParser 测试")
class KsuidParserTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("create工厂方法")
        void testCreate() {
            KsuidParser parser = KsuidParser.create();

            assertThat(parser).isNotNull();
        }

        @Test
        @DisplayName("单例模式")
        void testSingleton() {
            KsuidParser parser1 = KsuidParser.create();
            KsuidParser parser2 = KsuidParser.create();

            assertThat(parser1).isSameAs(parser2);
        }
    }

    @Nested
    @DisplayName("解析方法测试")
    class ParseTests {

        @Test
        @DisplayName("解析有效KSUID")
        void testParse() {
            KsuidGenerator gen = KsuidGenerator.create();
            KsuidParser parser = KsuidParser.create();
            String ksuid = gen.generate();

            var parsed = parser.parse(ksuid);

            assertThat(parsed).isNotNull();
            assertThat(parsed.ksuid()).isEqualTo(ksuid);
        }

        @Test
        @DisplayName("解析获取时间戳")
        void testParseTime() {
            KsuidGenerator gen = KsuidGenerator.create();
            KsuidParser parser = KsuidParser.create();
            long beforeSeconds = System.currentTimeMillis() / 1000;
            String ksuid = gen.generate();
            long afterSeconds = System.currentTimeMillis() / 1000;

            var parsed = parser.parse(ksuid);

            assertThat(parsed.time().getEpochSecond()).isBetween(beforeSeconds, afterSeconds);
        }

        @Test
        @DisplayName("解析获取字节数组")
        void testParseBytes() {
            KsuidGenerator gen = KsuidGenerator.create();
            KsuidParser parser = KsuidParser.create();
            String ksuid = gen.generate();

            var parsed = parser.parse(ksuid);

            assertThat(parsed.bytes()).isNotNull();
            assertThat(parsed.bytes()).hasSize(20);
        }

        @Test
        @DisplayName("解析获取负载")
        void testParsePayload() {
            KsuidGenerator gen = KsuidGenerator.create();
            KsuidParser parser = KsuidParser.create();
            String ksuid = gen.generate();

            var parsed = parser.parse(ksuid);

            assertThat(parsed.payload()).isNotNull();
            assertThat(parsed.payload()).hasSize(16);
        }

        @Test
        @DisplayName("解析无效KSUID抛出异常")
        void testParseInvalid() {
            KsuidParser parser = KsuidParser.create();

            assertThatThrownBy(() -> parser.parse("invalid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("解析null抛出异常")
        void testParseNull() {
            KsuidParser parser = KsuidParser.create();

            assertThatThrownBy(() -> parser.parse(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("提取时间戳测试")
    class ExtractTimestampTests {

        @Test
        @DisplayName("提取有效KSUID时间戳")
        void testExtractTimestamp() {
            KsuidGenerator gen = KsuidGenerator.create();
            KsuidParser parser = KsuidParser.create();
            long beforeSeconds = System.currentTimeMillis() / 1000;
            String ksuid = gen.generate();
            long afterSeconds = System.currentTimeMillis() / 1000;

            Instant timestamp = parser.extractTimestamp(ksuid);

            assertThat(timestamp.getEpochSecond()).isBetween(beforeSeconds, afterSeconds);
        }
    }

    @Nested
    @DisplayName("验证方法测试")
    class ValidationTests {

        @Test
        @DisplayName("验证有效KSUID")
        void testIsValidTrue() {
            KsuidGenerator gen = KsuidGenerator.create();
            KsuidParser parser = KsuidParser.create();
            String ksuid = gen.generate();

            assertThat(parser.isValid(ksuid)).isTrue();
        }

        @Test
        @DisplayName("验证无效KSUID")
        void testIsValidFalse() {
            KsuidParser parser = KsuidParser.create();

            assertThat(parser.isValid("invalid")).isFalse();
        }
    }

    @Nested
    @DisplayName("ParsedKsuid测试")
    class ParsedKsuidTests {

        @Test
        @DisplayName("获取时间戳十六进制")
        void testGetTimestampHex() {
            KsuidGenerator gen = KsuidGenerator.create();
            KsuidParser parser = KsuidParser.create();
            String ksuid = gen.generate();

            var parsed = parser.parse(ksuid);

            assertThat(parsed.getTimestampHex()).isNotNull();
            assertThat(parsed.getTimestampHex()).hasSize(8);
        }

        @Test
        @DisplayName("获取负载十六进制")
        void testGetPayloadHex() {
            KsuidGenerator gen = KsuidGenerator.create();
            KsuidParser parser = KsuidParser.create();
            String ksuid = gen.generate();

            var parsed = parser.parse(ksuid);

            assertThat(parsed.getPayloadHex()).isNotNull();
            assertThat(parsed.getPayloadHex()).hasSize(32);
        }

        @Test
        @DisplayName("toString方法")
        void testToString() {
            KsuidGenerator gen = KsuidGenerator.create();
            KsuidParser parser = KsuidParser.create();
            String ksuid = gen.generate();

            var parsed = parser.parse(ksuid);

            assertThat(parsed.toString()).contains(ksuid);
            assertThat(parsed.toString()).contains("ParsedKsuid");
        }
    }
}
