package cloud.opencode.base.string.match;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * AhoCorasickTest Tests
 * AhoCorasickTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("AhoCorasick Tests")
class AhoCorasickTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create matcher with patterns")
        void shouldCreateMatcherWithPatterns() {
            AhoCorasick matcher = AhoCorasick.builder()
                .addPattern("bad")
                .addPattern("word")
                .build();
            assertThat(matcher.patternCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should add patterns as collection")
        void shouldAddPatternsAsCollection() {
            AhoCorasick matcher = AhoCorasick.builder()
                .addPatterns(List.of("one", "two", "three"))
                .build();
            assertThat(matcher.patternCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should add patterns as varargs")
        void shouldAddPatternsAsVarargs() {
            AhoCorasick matcher = AhoCorasick.builder()
                .addPatterns("a", "b", "c")
                .build();
            assertThat(matcher.patternCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should ignore null and empty patterns")
        void shouldIgnoreNullAndEmptyPatterns() {
            AhoCorasick matcher = AhoCorasick.builder()
                .addPattern(null)
                .addPattern("")
                .addPattern("valid")
                .build();
            assertThat(matcher.patternCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw when no patterns provided")
        void shouldThrowWhenNoPatternsProvided() {
            assertThatThrownBy(() -> AhoCorasick.builder().build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("At least one pattern is required");
        }

        @Test
        @DisplayName("Should set ignore case")
        void shouldSetIgnoreCase() {
            AhoCorasick matcher = AhoCorasick.builder()
                .addPattern("test")
                .ignoreCase(true)
                .build();
            assertThat(matcher.containsAny("TEST")).isTrue();
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of should create matcher from varargs")
        void ofShouldCreateMatcherFromVarargs() {
            AhoCorasick matcher = AhoCorasick.of("one", "two");
            assertThat(matcher.patternCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("of should create matcher from collection")
        void ofShouldCreateMatcherFromCollection() {
            AhoCorasick matcher = AhoCorasick.of(List.of("one", "two", "three"));
            assertThat(matcher.patternCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("ofIgnoreCase should create case insensitive matcher from varargs")
        void ofIgnoreCaseShouldCreateCaseInsensitiveMatcher() {
            AhoCorasick matcher = AhoCorasick.ofIgnoreCase("test");
            assertThat(matcher.containsAny("TEST")).isTrue();
        }

        @Test
        @DisplayName("ofIgnoreCase should create case insensitive matcher from collection")
        void ofIgnoreCaseShouldCreateCaseInsensitiveMatcherFromCollection() {
            AhoCorasick matcher = AhoCorasick.ofIgnoreCase(List.of("test"));
            assertThat(matcher.containsAny("TEST")).isTrue();
        }
    }

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should find all matches")
        void shouldFindAllMatches() {
            AhoCorasick matcher = AhoCorasick.of("bad", "word");
            List<PatternMatch> matches = matcher.findAll("This is a bad word");
            assertThat(matches).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list for null text")
        void shouldReturnEmptyListForNullText() {
            AhoCorasick matcher = AhoCorasick.of("test");
            assertThat(matcher.findAll(null)).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for empty text")
        void shouldReturnEmptyListForEmptyText() {
            AhoCorasick matcher = AhoCorasick.of("test");
            assertThat(matcher.findAll("")).isEmpty();
        }

        @Test
        @DisplayName("Should find overlapping patterns")
        void shouldFindOverlappingPatterns() {
            AhoCorasick matcher = AhoCorasick.of("abc", "bc");
            List<PatternMatch> matches = matcher.findAll("xabcy");
            assertThat(matches).hasSizeGreaterThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findFirst Tests")
    class FindFirstTests {

        @Test
        @DisplayName("Should find first match")
        void shouldFindFirstMatch() {
            AhoCorasick matcher = AhoCorasick.of("bad", "word");
            Optional<PatternMatch> match = matcher.findFirst("This bad word");
            assertThat(match).isPresent();
            assertThat(match.get().pattern()).isEqualTo("bad");
        }

        @Test
        @DisplayName("Should return empty for no match")
        void shouldReturnEmptyForNoMatch() {
            AhoCorasick matcher = AhoCorasick.of("xyz");
            assertThat(matcher.findFirst("abc")).isEmpty();
        }
    }

    @Nested
    @DisplayName("containsAny Tests")
    class ContainsAnyTests {

        @Test
        @DisplayName("Should return true when match exists")
        void shouldReturnTrueWhenMatchExists() {
            AhoCorasick matcher = AhoCorasick.of("bad");
            assertThat(matcher.containsAny("This is bad")).isTrue();
        }

        @Test
        @DisplayName("Should return false when no match")
        void shouldReturnFalseWhenNoMatch() {
            AhoCorasick matcher = AhoCorasick.of("bad");
            assertThat(matcher.containsAny("This is good")).isFalse();
        }
    }

    @Nested
    @DisplayName("countMatches Tests")
    class CountMatchesTests {

        @Test
        @DisplayName("Should count all matches")
        void shouldCountAllMatches() {
            AhoCorasick matcher = AhoCorasick.of("a");
            assertThat(matcher.countMatches("banana")).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("getMatchedPatterns Tests")
    class GetMatchedPatternsTests {

        @Test
        @DisplayName("Should return unique matched patterns")
        void shouldReturnUniqueMatchedPatterns() {
            AhoCorasick matcher = AhoCorasick.of("bad", "good", "word");
            Set<String> matched = matcher.getMatchedPatterns("bad and good and bad");
            assertThat(matched).containsExactlyInAnyOrder("bad", "good");
        }
    }

    @Nested
    @DisplayName("replaceAll Tests")
    class ReplaceAllTests {

        @Test
        @DisplayName("Should replace all matches")
        void shouldReplaceAllMatches() {
            AhoCorasick matcher = AhoCorasick.of("bad");
            String result = matcher.replaceAll("This is bad and bad", "***");
            assertThat(result).isEqualTo("This is *** and ***");
        }

        @Test
        @DisplayName("Should return original for null text")
        void shouldReturnOriginalForNullText() {
            AhoCorasick matcher = AhoCorasick.of("bad");
            assertThat(matcher.replaceAll(null, "***")).isNull();
        }

        @Test
        @DisplayName("Should return original for no match")
        void shouldReturnOriginalForNoMatch() {
            AhoCorasick matcher = AhoCorasick.of("bad");
            assertThat(matcher.replaceAll("good text", "***")).isEqualTo("good text");
        }
    }

    @Nested
    @DisplayName("filter Tests")
    class FilterTests {

        @Test
        @DisplayName("Should filter with mask character")
        void shouldFilterWithMaskCharacter() {
            AhoCorasick matcher = AhoCorasick.of("bad");
            String result = matcher.filter("This is bad", '*');
            assertThat(result).isEqualTo("This is ***");
        }

        @Test
        @DisplayName("Should filter with default asterisk")
        void shouldFilterWithDefaultAsterisk() {
            AhoCorasick matcher = AhoCorasick.of("bad");
            String result = matcher.filter("This is bad");
            assertThat(result).isEqualTo("This is ***");
        }

        @Test
        @DisplayName("Should return original for null text")
        void shouldReturnOriginalForNullText() {
            AhoCorasick matcher = AhoCorasick.of("bad");
            assertThat(matcher.filter(null, '*')).isNull();
        }
    }

    @Nested
    @DisplayName("highlight Tests")
    class HighlightTests {

        @Test
        @DisplayName("Should highlight matches")
        void shouldHighlightMatches() {
            AhoCorasick matcher = AhoCorasick.of("bad");
            String result = matcher.highlight("This is bad", "<b>", "</b>");
            assertThat(result).isEqualTo("This is <b>bad</b>");
        }

        @Test
        @DisplayName("Should return original for null text")
        void shouldReturnOriginalForNullText() {
            AhoCorasick matcher = AhoCorasick.of("bad");
            assertThat(matcher.highlight(null, "<b>", "</b>")).isNull();
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("patternCount should return pattern count")
        void patternCountShouldReturnPatternCount() {
            AhoCorasick matcher = AhoCorasick.of("a", "b", "c");
            assertThat(matcher.patternCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("getPatterns should return all patterns")
        void getPatternsShouldReturnAllPatterns() {
            AhoCorasick matcher = AhoCorasick.of("a", "b", "c");
            assertThat(matcher.getPatterns()).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("hasPattern should check if pattern exists")
        void hasPatternShouldCheckIfPatternExists() {
            AhoCorasick matcher = AhoCorasick.of("test");
            assertThat(matcher.hasPattern("test")).isTrue();
            assertThat(matcher.hasPattern("other")).isFalse();
            assertThat(matcher.hasPattern(null)).isFalse();
        }
    }
}
