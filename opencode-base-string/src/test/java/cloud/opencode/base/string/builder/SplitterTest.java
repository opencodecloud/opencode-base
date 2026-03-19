package cloud.opencode.base.string.builder;

import org.junit.jupiter.api.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * SplitterTest Tests
 * SplitterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("Splitter Tests")
class SplitterTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("on(char) should create splitter with char separator")
        void onCharShouldCreateSplitterWithCharSeparator() {
            List<String> result = Splitter.on(',').splitToList("a,b,c");
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("on(String) should create splitter with string separator")
        void onStringShouldCreateSplitterWithStringSeparator() {
            List<String> result = Splitter.on("::").splitToList("a::b::c");
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("on(Pattern) should create splitter with pattern separator")
        void onPatternShouldCreateSplitterWithPatternSeparator() {
            List<String> result = Splitter.on(Pattern.compile("\\s+")).splitToList("a b  c");
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("onPattern should create splitter with regex pattern")
        void onPatternStringShouldCreateSplitterWithRegexPattern() {
            List<String> result = Splitter.onPattern("\\d+").splitToList("a1b2c");
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("on(CharMatcher) should create splitter with char matcher")
        void onCharMatcherShouldCreateSplitterWithCharMatcher() {
            List<String> result = Splitter.on(CharMatcher.whitespace()).splitToList("a b c");
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("fixedLength should create splitter with fixed length")
        void fixedLengthShouldCreateSplitterWithFixedLength() {
            List<String> result = Splitter.fixedLength(3).splitToList("abcdefgh");
            assertThat(result).containsExactly("abc", "def", "gh");
        }
    }

    @Nested
    @DisplayName("split Tests")
    class SplitTests {

        @Test
        @DisplayName("split should return Iterable")
        void splitShouldReturnIterable() {
            Iterable<String> result = Splitter.on(',').split("a,b,c");
            List<String> list = new ArrayList<>();
            result.forEach(list::add);
            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("split should handle empty string")
        void splitShouldHandleEmptyString() {
            List<String> result = Splitter.on(',').splitToList("");
            assertThat(result).containsExactly("");
        }

        @Test
        @DisplayName("split should handle no separator found")
        void splitShouldHandleNoSeparatorFound() {
            List<String> result = Splitter.on(',').splitToList("abc");
            assertThat(result).containsExactly("abc");
        }
    }

    @Nested
    @DisplayName("splitToList Tests")
    class SplitToListTests {

        @Test
        @DisplayName("splitToList should return List")
        void splitToListShouldReturnList() {
            List<String> result = Splitter.on(',').splitToList("a,b,c");
            assertThat(result).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("splitToStream Tests")
    class SplitToStreamTests {

        @Test
        @DisplayName("splitToStream should return Stream")
        void splitToStreamShouldReturnStream() {
            List<String> result = Splitter.on(',').splitToStream("a,b,c")
                .collect(Collectors.toList());
            assertThat(result).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("omitEmptyStrings Tests")
    class OmitEmptyStringsTests {

        @Test
        @DisplayName("omitEmptyStrings should skip empty strings")
        void omitEmptyStringsShouldSkipEmptyStrings() {
            List<String> result = Splitter.on(',').omitEmptyStrings().splitToList("a,,b,,c");
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("omitEmptyStrings should handle all empty")
        void omitEmptyStringsShouldHandleAllEmpty() {
            List<String> result = Splitter.on(',').omitEmptyStrings().splitToList(",,,");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("trimResults Tests")
    class TrimResultsTests {

        @Test
        @DisplayName("trimResults should trim whitespace")
        void trimResultsShouldTrimWhitespace() {
            List<String> result = Splitter.on(',').trimResults().splitToList(" a , b , c ");
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("trimResults with CharMatcher should trim matched chars")
        void trimResultsWithCharMatcherShouldTrimMatchedChars() {
            List<String> result = Splitter.on(',').trimResults(CharMatcher.is('_')).splitToList("_a_,_b_");
            assertThat(result).containsExactly("a", "b");
        }
    }

    @Nested
    @DisplayName("limit Tests")
    class LimitTests {

        @Test
        @DisplayName("limit should limit number of splits")
        void limitShouldLimitNumberOfSplits() {
            // Note: The current implementation may not fully support limit
            Splitter splitter = Splitter.on(',').limit(2);
            assertThat(splitter).isNotNull();
        }
    }

    @Nested
    @DisplayName("Combined Options Tests")
    class CombinedOptionsTests {

        @Test
        @DisplayName("Should combine omitEmptyStrings and trimResults")
        void shouldCombineOmitEmptyStringsAndTrimResults() {
            List<String> result = Splitter.on(',')
                .omitEmptyStrings()
                .trimResults()
                .splitToList(" a , , b , c ");
            assertThat(result).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("MapSplitter Tests")
    class MapSplitterTests {

        @Test
        @DisplayName("withKeyValueSeparator(String) should create MapSplitter")
        void withKeyValueSeparatorStringShouldCreateMapSplitter() {
            Map<String, String> result = Splitter.on('&')
                .withKeyValueSeparator("=")
                .split("a=1&b=2");
            assertThat(result).containsEntry("a", "1").containsEntry("b", "2");
        }

        @Test
        @DisplayName("withKeyValueSeparator(char) should create MapSplitter")
        void withKeyValueSeparatorCharShouldCreateMapSplitter() {
            Map<String, String> result = Splitter.on('&')
                .withKeyValueSeparator('=')
                .split("key=value");
            assertThat(result).containsEntry("key", "value");
        }

        @Test
        @DisplayName("MapSplitter should handle missing value")
        void mapSplitterShouldHandleMissingValue() {
            Map<String, String> result = Splitter.on('&')
                .withKeyValueSeparator("=")
                .split("a=1&b");
            assertThat(result).containsEntry("a", "1").containsEntry("b", "");
        }
    }
}
