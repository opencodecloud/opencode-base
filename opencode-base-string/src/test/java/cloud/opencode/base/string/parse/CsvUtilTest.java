package cloud.opencode.base.string.parse;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CsvUtilTest Tests
 * CsvUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("CsvUtil Tests")
class CsvUtilTest {

    @Nested
    @DisplayName("parse Tests")
    class ParseTests {

        @Test
        @DisplayName("Should parse simple CSV")
        void shouldParseSimpleCsv() {
            String csv = "a,b,c\n1,2,3";
            List<List<String>> rows = CsvUtil.parse(csv);
            assertThat(rows).hasSize(2);
            assertThat(rows.get(0)).containsExactly("a", "b", "c");
            assertThat(rows.get(1)).containsExactly("1", "2", "3");
        }

        @Test
        @DisplayName("Should handle quoted fields")
        void shouldHandleQuotedFields() {
            String csv = "\"a,b\",c";
            List<List<String>> rows = CsvUtil.parse(csv);
            assertThat(rows.get(0)).containsExactly("a,b", "c");
        }

        @Test
        @DisplayName("Should return empty list for null")
        void shouldReturnEmptyListForNull() {
            assertThat(CsvUtil.parse(null)).isEmpty();
        }

        @Test
        @DisplayName("Should skip empty lines")
        void shouldSkipEmptyLines() {
            String csv = "a,b\n\nc,d";
            List<List<String>> rows = CsvUtil.parse(csv);
            assertThat(rows).hasSize(2);
        }

        @Test
        @DisplayName("Should trim whitespace from fields")
        void shouldTrimWhitespaceFromFields() {
            String csv = " a , b , c ";
            List<List<String>> rows = CsvUtil.parse(csv);
            assertThat(rows.get(0)).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("parseWithHeader Tests")
    class ParseWithHeaderTests {

        @Test
        @DisplayName("Should parse CSV with header")
        void shouldParseCsvWithHeader() {
            String csv = "name,age,city\nAlice,30,New York\nBob,25,Boston";
            List<Map<String, String>> rows = CsvUtil.parseWithHeader(csv);
            assertThat(rows).hasSize(2);
            assertThat(rows.get(0))
                .containsEntry("name", "Alice")
                .containsEntry("age", "30")
                .containsEntry("city", "New York");
        }

        @Test
        @DisplayName("Should return empty list for empty CSV")
        void shouldReturnEmptyListForEmptyCsv() {
            assertThat(CsvUtil.parseWithHeader(null)).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for header only")
        void shouldReturnEmptyListForHeaderOnly() {
            String csv = "name,age";
            List<Map<String, String>> rows = CsvUtil.parseWithHeader(csv);
            assertThat(rows).isEmpty();
        }

        @Test
        @DisplayName("Should handle mismatched columns")
        void shouldHandleMismatchedColumns() {
            String csv = "name,age,city\nAlice,30";  // missing city
            List<Map<String, String>> rows = CsvUtil.parseWithHeader(csv);
            assertThat(rows.get(0)).containsKeys("name", "age");
        }
    }

    @Nested
    @DisplayName("toCsv Tests")
    class ToCsvTests {

        @Test
        @DisplayName("Should convert to CSV")
        void shouldConvertToCsv() {
            List<List<String>> rows = List.of(
                List.of("a", "b", "c"),
                List.of("1", "2", "3")
            );
            String csv = CsvUtil.toCsv(rows);
            assertThat(csv).isEqualTo("a,b,c\n1,2,3\n");
        }

        @Test
        @DisplayName("Should escape fields with commas")
        void shouldEscapeFieldsWithCommas() {
            List<List<String>> rows = List.of(List.of("a,b", "c"));
            String csv = CsvUtil.toCsv(rows);
            assertThat(csv).isEqualTo("\"a,b\",c\n");
        }

        @Test
        @DisplayName("Should escape fields with quotes")
        void shouldEscapeFieldsWithQuotes() {
            List<List<String>> rows = List.of(List.of("say \"hello\"", "world"));
            String csv = CsvUtil.toCsv(rows);
            assertThat(csv).isEqualTo("\"say \"\"hello\"\"\",world\n");
        }

        @Test
        @DisplayName("Should escape fields with newlines")
        void shouldEscapeFieldsWithNewlines() {
            List<List<String>> rows = List.of(List.of("line1\nline2", "b"));
            String csv = CsvUtil.toCsv(rows);
            assertThat(csv).isEqualTo("\"line1\nline2\",b\n");
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = CsvUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
