package cloud.opencode.base.string.match;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * FuzzyMatcherTest Tests
 * FuzzyMatcherTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("FuzzyMatcher Tests")
class FuzzyMatcherTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create builder for strings")
        void shouldCreateBuilderForStrings() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .addAll(List.of("apple", "banana"))
                .build();
            assertThat(matcher.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should create builder with key extractor")
        void shouldCreateBuilderWithKeyExtractor() {
            record Item(String name) {}
            FuzzyMatcher<Item> matcher = FuzzyMatcher.builder(Item::name)
                .addAll(List.of(new Item("apple"), new Item("banana")))
                .build();
            assertThat(matcher.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should throw for null key extractor")
        void shouldThrowForNullKeyExtractor() {
            assertThatThrownBy(() -> FuzzyMatcher.builder(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should set threshold")
        void shouldSetThreshold() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .addAll(List.of("apple"))
                .threshold(0.8)
                .build();
            assertThat(matcher).isNotNull();
        }

        @Test
        @DisplayName("Should throw for invalid threshold")
        void shouldThrowForInvalidThreshold() {
            assertThatThrownBy(() -> FuzzyMatcher.<String>builder().threshold(-0.1))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> FuzzyMatcher.<String>builder().threshold(1.1))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should set maxResults")
        void shouldSetMaxResults() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .maxResults(5)
                .build();
            assertThat(matcher).isNotNull();
        }

        @Test
        @DisplayName("Should throw for invalid maxResults")
        void shouldThrowForInvalidMaxResults() {
            assertThatThrownBy(() -> FuzzyMatcher.<String>builder().maxResults(0))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should add single item")
        void shouldAddSingleItem() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .add("apple")
                .add("banana")
                .build();
            assertThat(matcher.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should ignore null items")
        void shouldIgnoreNullItems() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .add(null)
                .addAll(Arrays.asList("apple", null, "banana"))
                .build();
            assertThat(matcher.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should set algorithm")
        void shouldSetAlgorithm() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .algorithm(FuzzyMatcher.MatchAlgorithm.LEVENSHTEIN)
                .build();
            assertThat(matcher).isNotNull();
        }

        @Test
        @DisplayName("Should set ignoreCase")
        void shouldSetIgnoreCase() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .ignoreCase(false)
                .build();
            assertThat(matcher).isNotNull();
        }
    }

    @Nested
    @DisplayName("match Tests")
    class MatchTests {

        @Test
        @DisplayName("Should find matching items")
        void shouldFindMatchingItems() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .addAll(List.of("apple", "apply", "banana"))
                .threshold(0.5)
                .build();
            List<FuzzyMatch<String>> matches = matcher.match("apple");
            assertThat(matches).isNotEmpty();
            assertThat(matches).extracting(FuzzyMatch::item).contains("apple");
        }

        @Test
        @DisplayName("Should return empty list for null query")
        void shouldReturnEmptyListForNullQuery() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .addAll(List.of("apple"))
                .build();
            assertThat(matcher.match(null)).isEmpty();
            assertThat(matcher.match("")).isEmpty();
        }

        @Test
        @DisplayName("Should sort by score descending")
        void shouldSortByScoreDescending() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .addAll(List.of("apple", "apply", "application"))
                .threshold(0.5)
                .build();
            List<FuzzyMatch<String>> matches = matcher.match("apple");
            assertThat(matches.get(0).item()).isEqualTo("apple");
        }

        @Test
        @DisplayName("Should limit results")
        void shouldLimitResults() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .addAll(List.of("a", "ab", "abc", "abcd", "abcde"))
                .maxResults(2)
                .threshold(0.1)
                .build();
            List<FuzzyMatch<String>> matches = matcher.match("abc");
            assertThat(matches).hasSizeLessThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("matchBest Tests")
    class MatchBestTests {

        @Test
        @DisplayName("Should find best match")
        void shouldFindBestMatch() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .addAll(List.of("apple", "banana", "cherry"))
                .build();
            Optional<FuzzyMatch<String>> best = matcher.matchBest("apple");
            assertThat(best).isPresent();
            assertThat(best.get().item()).isEqualTo("apple");
        }

        @Test
        @DisplayName("Should return empty for no match")
        void shouldReturnEmptyForNoMatch() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .addAll(List.of("apple", "banana"))
                .threshold(0.9)
                .build();
            Optional<FuzzyMatch<String>> best = matcher.matchBest("xyz");
            assertThat(best).isEmpty();
        }
    }

    @Nested
    @DisplayName("suggest Tests")
    class SuggestTests {

        @Test
        @DisplayName("Should return suggestions")
        void shouldReturnSuggestions() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .addAll(List.of("apple", "apply", "banana"))
                .build();
            List<String> suggestions = matcher.suggest("app");
            assertThat(suggestions).isNotEmpty();
        }

        @Test
        @DisplayName("suggestStrings should return string keys")
        void suggestStringsShouldReturnStringKeys() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .addAll(List.of("apple", "apply"))
                .build();
            List<String> suggestions = matcher.suggestStrings("apple");
            assertThat(suggestions).contains("apple");
        }
    }

    @Nested
    @DisplayName("hasMatch Tests")
    class HasMatchTests {

        @Test
        @DisplayName("Should return true when match exists")
        void shouldReturnTrueWhenMatchExists() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .addAll(List.of("apple"))
                .build();
            assertThat(matcher.hasMatch("apple")).isTrue();
        }

        @Test
        @DisplayName("Should return false when no match exists")
        void shouldReturnFalseWhenNoMatchExists() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .addAll(List.of("apple"))
                .threshold(0.9)
                .build();
            assertThat(matcher.hasMatch("xyz")).isFalse();
        }
    }

    @Nested
    @DisplayName("Algorithm Tests")
    class AlgorithmTests {

        @Test
        @DisplayName("LEVENSHTEIN algorithm should work")
        void levenshteinAlgorithmShouldWork() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .addAll(List.of("apple", "banana"))
                .algorithm(FuzzyMatcher.MatchAlgorithm.LEVENSHTEIN)
                .build();
            assertThat(matcher.match("apple")).isNotEmpty();
        }

        @Test
        @DisplayName("JARO_WINKLER algorithm should work")
        void jaroWinklerAlgorithmShouldWork() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .addAll(List.of("apple", "banana"))
                .algorithm(FuzzyMatcher.MatchAlgorithm.JARO_WINKLER)
                .build();
            assertThat(matcher.match("apple")).isNotEmpty();
        }

        @Test
        @DisplayName("CONTAINS algorithm should work")
        void containsAlgorithmShouldWork() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .addAll(List.of("application", "banana"))
                .algorithm(FuzzyMatcher.MatchAlgorithm.CONTAINS)
                .threshold(0.1)
                .build();
            // The contains algorithm checks if target contains query
            // "application".contains("app") is true, score = 1 - (11-3)/11 = 0.27
            List<FuzzyMatch<String>> matches = matcher.match("app");
            assertThat(matches).isNotEmpty();
        }

        @Test
        @DisplayName("PREFIX algorithm should work")
        void prefixAlgorithmShouldWork() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .addAll(List.of("apple", "banana"))
                .algorithm(FuzzyMatcher.MatchAlgorithm.PREFIX)
                .build();
            assertThat(matcher.match("app")).isNotEmpty();
        }

        @Test
        @DisplayName("COMBINED algorithm should work")
        void combinedAlgorithmShouldWork() {
            FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
                .addAll(List.of("apple", "banana"))
                .algorithm(FuzzyMatcher.MatchAlgorithm.COMBINED)
                .build();
            assertThat(matcher.match("apple")).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("MatchAlgorithm Enum Tests")
    class MatchAlgorithmEnumTests {

        @Test
        @DisplayName("Should have all expected values")
        void shouldHaveAllExpectedValues() {
            assertThat(FuzzyMatcher.MatchAlgorithm.values()).contains(
                FuzzyMatcher.MatchAlgorithm.LEVENSHTEIN,
                FuzzyMatcher.MatchAlgorithm.JARO_WINKLER,
                FuzzyMatcher.MatchAlgorithm.CONTAINS,
                FuzzyMatcher.MatchAlgorithm.PREFIX,
                FuzzyMatcher.MatchAlgorithm.COMBINED
            );
        }
    }
}
