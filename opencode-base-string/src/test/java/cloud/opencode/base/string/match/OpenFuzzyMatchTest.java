package cloud.opencode.base.string.match;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenFuzzyMatchTest Tests
 * OpenFuzzyMatchTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenFuzzyMatch Tests")
class OpenFuzzyMatchTest {

    @Nested
    @DisplayName("search Tests")
    class SearchTests {

        @Test
        @DisplayName("Should find fuzzy matches")
        void shouldFindFuzzyMatches() {
            List<String> candidates = List.of("apple", "banana", "apply", "grape");
            List<String> results = OpenFuzzyMatch.search("apple", candidates);
            assertThat(results).contains("apple");
        }

        @Test
        @DisplayName("Should return empty list for null query")
        void shouldReturnEmptyListForNullQuery() {
            assertThat(OpenFuzzyMatch.search(null, List.of("a", "b"))).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for null candidates")
        void shouldReturnEmptyListForNullCandidates() {
            assertThat(OpenFuzzyMatch.search("test", null)).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for empty candidates")
        void shouldReturnEmptyListForEmptyCandidates() {
            assertThat(OpenFuzzyMatch.search("test", List.of())).isEmpty();
        }

        @Test
        @DisplayName("Should search with custom threshold")
        void shouldSearchWithCustomThreshold() {
            List<String> candidates = List.of("apple", "banana", "cherry");
            List<String> results = OpenFuzzyMatch.search("apple", candidates, 0.9);
            assertThat(results).contains("apple");
        }

        @Test
        @DisplayName("Should search with key extractor")
        void shouldSearchWithKeyExtractor() {
            record Item(String name) {}
            List<Item> items = List.of(new Item("apple"), new Item("banana"));
            List<Item> results = OpenFuzzyMatch.search("apple", items, Item::name);
            assertThat(results).extracting(Item::name).contains("apple");
        }
    }

    @Nested
    @DisplayName("suggest Tests")
    class SuggestTests {

        @Test
        @DisplayName("Should return suggestions with scores")
        void shouldReturnSuggestionsWithScores() {
            List<String> candidates = List.of("apple", "banana");
            List<FuzzyMatch<String>> results = OpenFuzzyMatch.suggest("apple", candidates, 0.5);
            assertThat(results).isNotEmpty();
            assertThat(results.get(0).item()).isEqualTo("apple");
            assertThat(results.get(0).score()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should return empty list for null inputs")
        void shouldReturnEmptyListForNullInputs() {
            assertThat(OpenFuzzyMatch.suggest(null, List.of("a"))).isEmpty();
            assertThat(OpenFuzzyMatch.suggest("a", null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findBest Tests")
    class FindBestTests {

        @Test
        @DisplayName("Should find best match")
        void shouldFindBestMatch() {
            List<String> candidates = List.of("Java", "JavaScript", "Python");
            String best = OpenFuzzyMatch.findBest("JavaScript", candidates);
            assertThat(best).isEqualTo("JavaScript");
        }

        @Test
        @DisplayName("Should return null for no match above threshold")
        void shouldReturnNullForNoMatchAboveThreshold() {
            List<String> candidates = List.of("apple", "banana");
            String best = OpenFuzzyMatch.findBest("xyz", candidates, 0.9);
            assertThat(best).isNull();
        }

        @Test
        @DisplayName("Should return null for null inputs")
        void shouldReturnNullForNullInputs() {
            assertThat(OpenFuzzyMatch.findBest(null, List.of("a"))).isNull();
            assertThat(OpenFuzzyMatch.findBest("a", null)).isNull();
        }

        @Test
        @DisplayName("Should find best with key extractor")
        void shouldFindBestWithKeyExtractor() {
            record Item(String name) {}
            List<Item> items = List.of(new Item("apple"), new Item("banana"));
            Item best = OpenFuzzyMatch.findBest("apple", items, Item::name);
            assertThat(best).isNotNull();
            assertThat(best.name()).isEqualTo("apple");
        }
    }

    @Nested
    @DisplayName("didYouMean Tests")
    class DidYouMeanTests {

        @Test
        @DisplayName("Should suggest correction")
        void shouldSuggestCorrection() {
            List<String> dictionary = List.of("receive", "believe", "achieve");
            String suggestion = OpenFuzzyMatch.didYouMean("recieve", dictionary);
            assertThat(suggestion).isEqualTo("receive");
        }

        @Test
        @DisplayName("Should return null if already in dictionary")
        void shouldReturnNullIfAlreadyInDictionary() {
            List<String> dictionary = List.of("apple", "banana");
            assertThat(OpenFuzzyMatch.didYouMean("apple", dictionary)).isNull();
        }

        @Test
        @DisplayName("Should return null for null inputs")
        void shouldReturnNullForNullInputs() {
            assertThat(OpenFuzzyMatch.didYouMean(null, List.of("a"))).isNull();
            assertThat(OpenFuzzyMatch.didYouMean("a", null)).isNull();
        }
    }

    @Nested
    @DisplayName("similarity Tests")
    class SimilarityTests {

        @Test
        @DisplayName("Should return 1.0 for identical strings")
        void shouldReturnOneForIdenticalStrings() {
            assertThat(OpenFuzzyMatch.similarity("hello", "hello")).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should return 0.0 for null strings")
        void shouldReturnZeroForNullStrings() {
            assertThat(OpenFuzzyMatch.similarity(null, "hello")).isEqualTo(0.0);
            assertThat(OpenFuzzyMatch.similarity("hello", null)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should return value between 0 and 1")
        void shouldReturnValueBetweenZeroAndOne() {
            double similarity = OpenFuzzyMatch.similarity("hello", "hallo");
            assertThat(similarity).isBetween(0.0, 1.0);
        }
    }

    @Nested
    @DisplayName("isSimilar Tests")
    class IsSimilarTests {

        @Test
        @DisplayName("Should return true for similar strings")
        void shouldReturnTrueForSimilarStrings() {
            assertThat(OpenFuzzyMatch.isSimilar("hello", "hello", 0.9)).isTrue();
            assertThat(OpenFuzzyMatch.isSimilar("hello", "hallo", 0.7)).isTrue();
        }

        @Test
        @DisplayName("Should return false for dissimilar strings")
        void shouldReturnFalseForDissimilarStrings() {
            assertThat(OpenFuzzyMatch.isSimilar("hello", "world", 0.9)).isFalse();
        }
    }

    @Nested
    @DisplayName("rankBySimilarity Tests")
    class RankBySimilarityTests {

        @Test
        @DisplayName("Should rank by similarity descending")
        void shouldRankBySimilarityDescending() {
            List<String> candidates = List.of("cat", "car", "card", "dog");
            List<String> ranked = OpenFuzzyMatch.rankBySimilarity("car", candidates);
            assertThat(ranked.get(0)).isEqualTo("car");
        }

        @Test
        @DisplayName("Should return empty list for null inputs")
        void shouldReturnEmptyListForNullInputs() {
            assertThat(OpenFuzzyMatch.rankBySimilarity(null, List.of("a"))).isEmpty();
            assertThat(OpenFuzzyMatch.rankBySimilarity("a", null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("rankWithScores Tests")
    class RankWithScoresTests {

        @Test
        @DisplayName("Should return scores map")
        void shouldReturnScoresMap() {
            List<String> candidates = List.of("apple", "apply");
            Map<String, Double> scores = OpenFuzzyMatch.rankWithScores("apple", candidates);
            assertThat(scores).containsKey("apple");
            assertThat(scores.get("apple")).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should return empty map for null inputs")
        void shouldReturnEmptyMapForNullInputs() {
            assertThat(OpenFuzzyMatch.rankWithScores(null, List.of("a"))).isEmpty();
            assertThat(OpenFuzzyMatch.rankWithScores("a", null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenFuzzyMatch.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
