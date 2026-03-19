package cloud.opencode.base.id;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * IdParser 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("IdParser 接口测试")
class IdParserTest {

    @Nested
    @DisplayName("接口方法测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("parse方法存在")
        void testParseExists() throws NoSuchMethodException {
            var method = IdParser.class.getMethod("parse", Object.class);

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Object.class);
        }

        @Test
        @DisplayName("extractTimestamp方法存在")
        void testExtractTimestampExists() throws NoSuchMethodException {
            var method = IdParser.class.getMethod("extractTimestamp", Object.class);

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Instant.class);
        }

        @Test
        @DisplayName("isValid方法存在")
        void testIsValidExists() throws NoSuchMethodException {
            var method = IdParser.class.getMethod("isValid", Object.class);

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(boolean.class);
        }
    }

    @Nested
    @DisplayName("实现测试")
    class ImplementationTests {

        record ParsedId(String id, Instant timestamp) {}

        private IdParser<String, ParsedId> createMockParser() {
            return new IdParser<>() {
                @Override
                public ParsedId parse(String id) {
                    if (!isValid(id)) {
                        throw new IllegalArgumentException("Invalid ID");
                    }
                    return new ParsedId(id, Instant.now());
                }

                @Override
                public Instant extractTimestamp(String id) {
                    return Instant.now();
                }

                @Override
                public boolean isValid(String id) {
                    return id != null && id.startsWith("ID-");
                }
            };
        }

        @Test
        @DisplayName("parse有效ID")
        void testParseValid() {
            var parser = createMockParser();

            ParsedId result = parser.parse("ID-123");

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo("ID-123");
        }

        @Test
        @DisplayName("parse无效ID抛出异常")
        void testParseInvalid() {
            var parser = createMockParser();

            assertThatThrownBy(() -> parser.parse("invalid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("isValid验证有效ID")
        void testIsValidTrue() {
            var parser = createMockParser();

            assertThat(parser.isValid("ID-123")).isTrue();
        }

        @Test
        @DisplayName("isValid验证无效ID")
        void testIsValidFalse() {
            var parser = createMockParser();

            assertThat(parser.isValid("invalid")).isFalse();
            assertThat(parser.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("extractTimestamp返回时间戳")
        void testExtractTimestamp() {
            var parser = createMockParser();

            Instant timestamp = parser.extractTimestamp("ID-123");

            assertThat(timestamp).isNotNull();
        }
    }
}
