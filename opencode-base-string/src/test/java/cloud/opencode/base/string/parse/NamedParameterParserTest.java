package cloud.opencode.base.string.parse;

import cloud.opencode.base.string.parse.NamedParameterParser.ParsedSql;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("NamedParameterParser")
class NamedParameterParserTest {

    @Nested
    @DisplayName("parse")
    class Parse {

        @Test
        @DisplayName("should replace named params with positional placeholders")
        void shouldReplaceNamedParams() {
            ParsedSql result = NamedParameterParser.parse("SELECT * FROM t WHERE id = :id AND name = :name");
            assertThat(result.sql()).isEqualTo("SELECT * FROM t WHERE id = ? AND name = ?");
            assertThat(result.paramNames()).containsExactly("id", "name");
        }

        @Test
        @DisplayName("should handle SQL with no parameters")
        void shouldHandleNoParams() {
            ParsedSql result = NamedParameterParser.parse("SELECT * FROM t");
            assertThat(result.sql()).isEqualTo("SELECT * FROM t");
            assertThat(result.paramNames()).isEmpty();
        }

        @Test
        @DisplayName("should return original for null input")
        void shouldHandleNull() {
            ParsedSql result = NamedParameterParser.parse(null);
            assertThat(result.sql()).isNull();
            assertThat(result.paramNames()).isEmpty();
        }

        @Test
        @DisplayName("should return original for blank input")
        void shouldHandleBlank() {
            ParsedSql result = NamedParameterParser.parse("   ");
            assertThat(result.sql()).isEqualTo("   ");
            assertThat(result.paramNames()).isEmpty();
        }

        @Test
        @DisplayName("should skip PostgreSQL cast operator ::")
        void shouldSkipPostgresCast() {
            ParsedSql result = NamedParameterParser.parse("SELECT id::text FROM t WHERE name = :name");
            assertThat(result.sql()).isEqualTo("SELECT id::text FROM t WHERE name = ?");
            assertThat(result.paramNames()).containsExactly("name");
        }

        @Test
        @DisplayName("should skip params inside single-quoted strings")
        void shouldSkipStringLiterals() {
            ParsedSql result = NamedParameterParser.parse("SELECT * FROM t WHERE name = ':notparam' AND id = :id");
            assertThat(result.sql()).isEqualTo("SELECT * FROM t WHERE name = ':notparam' AND id = ?");
            assertThat(result.paramNames()).containsExactly("id");
        }

        @Test
        @DisplayName("should handle escaped single quotes inside strings")
        void shouldHandleEscapedQuotes() {
            ParsedSql result = NamedParameterParser.parse("SELECT * FROM t WHERE name = 'it''s :notparam' AND id = :id");
            assertThat(result.paramNames()).containsExactly("id");
        }

        @Test
        @DisplayName("should handle duplicate parameter names")
        void shouldHandleDuplicateParams() {
            ParsedSql result = NamedParameterParser.parse("SELECT * FROM t WHERE a = :id OR b = :id");
            assertThat(result.sql()).isEqualTo("SELECT * FROM t WHERE a = ? OR b = ?");
            assertThat(result.paramNames()).containsExactly("id", "id");
        }

        @Test
        @DisplayName("should handle underscore in param names")
        void shouldHandleUnderscoreParams() {
            ParsedSql result = NamedParameterParser.parse("WHERE col = :my_param_1");
            assertThat(result.paramNames()).containsExactly("my_param_1");
        }

        @Test
        @DisplayName("should return immutable param names list")
        void shouldReturnImmutableList() {
            ParsedSql result = NamedParameterParser.parse("WHERE id = :id");
            assertThatThrownBy(() -> result.paramNames().add("x"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("extractValues")
    class ExtractValues {

        @Test
        @DisplayName("should extract values in correct order")
        void shouldExtractInOrder() {
            ParsedSql parsed = NamedParameterParser.parse("WHERE id = :id AND name = :name");
            Object[] values = NamedParameterParser.extractValues(parsed, Map.of("id", 42, "name", "Alice"));
            assertThat(values).containsExactly(42, "Alice");
        }

        @Test
        @DisplayName("should throw for missing parameter")
        void shouldThrowForMissingParam() {
            ParsedSql parsed = NamedParameterParser.parse("WHERE id = :id");
            assertThatThrownBy(() -> NamedParameterParser.extractValues(parsed, Map.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("id");
        }

        @Test
        @DisplayName("should handle null params map")
        void shouldHandleNullParamsMap() {
            ParsedSql parsed = NamedParameterParser.parse("SELECT 1");
            Object[] values = NamedParameterParser.extractValues(parsed, null);
            assertThat(values).isEmpty();
        }

        @Test
        @DisplayName("should handle null values in map")
        void shouldHandleNullValues() {
            ParsedSql parsed = NamedParameterParser.parse("WHERE id = :id");
            Map<String, Object> params = new java.util.HashMap<>();
            params.put("id", null);
            Object[] values = NamedParameterParser.extractValues(parsed, params);
            assertThat(values).containsExactly((Object) null);
        }

        @Test
        @DisplayName("should extract duplicate param values")
        void shouldExtractDuplicateParams() {
            ParsedSql parsed = NamedParameterParser.parse("WHERE a = :id OR b = :id");
            Object[] values = NamedParameterParser.extractValues(parsed, Map.of("id", 99));
            assertThat(values).containsExactly(99, 99);
        }
    }
}
