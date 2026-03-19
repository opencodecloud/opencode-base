package cloud.opencode.base.string.parse;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenParseTest Tests
 * OpenParseTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenParse Tests")
class OpenParseTest {

    @Nested
    @DisplayName("tokenize Tests")
    class TokenizeTests {

        @Test
        @DisplayName("Should tokenize with custom delimiters")
        void shouldTokenizeWithCustomDelimiters() {
            List<String> tokens = OpenParse.tokenize("a,b;c", ",;");
            assertThat(tokens).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should tokenize with space delimiter")
        void shouldTokenizeWithSpaceDelimiter() {
            List<String> tokens = OpenParse.tokenize("Hello World Test", " ");
            assertThat(tokens).containsExactly("Hello", "World", "Test");
        }
    }

    @Nested
    @DisplayName("parseCsv Tests")
    class ParseCsvTests {

        @Test
        @DisplayName("Should parse simple CSV")
        void shouldParseSimpleCsv() {
            String csv = "a,b,c\n1,2,3";
            List<List<String>> rows = OpenParse.parseCsv(csv);
            assertThat(rows).hasSize(2);
            assertThat(rows.get(0)).containsExactly("a", "b", "c");
            assertThat(rows.get(1)).containsExactly("1", "2", "3");
        }
    }

    @Nested
    @DisplayName("parseCsvWithHeader Tests")
    class ParseCsvWithHeaderTests {

        @Test
        @DisplayName("Should parse CSV with header")
        void shouldParseCsvWithHeader() {
            String csv = "name,age\nAlice,30\nBob,25";
            List<Map<String, String>> rows = OpenParse.parseCsvWithHeader(csv);
            assertThat(rows).hasSize(2);
            assertThat(rows.get(0)).containsEntry("name", "Alice").containsEntry("age", "30");
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenParse.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
