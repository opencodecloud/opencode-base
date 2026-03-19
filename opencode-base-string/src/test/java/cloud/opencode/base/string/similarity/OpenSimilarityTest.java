package cloud.opencode.base.string.similarity;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenSimilarityTest Tests
 * OpenSimilarityTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenSimilarity Tests")
class OpenSimilarityTest {

    @Nested
    @DisplayName("levenshteinDistance Tests")
    class LevenshteinDistanceTests {

        @Test
        @DisplayName("Should delegate to LevenshteinDistance.calculate")
        void shouldDelegateToLevenshteinDistanceCalculate() {
            assertThat(OpenSimilarity.levenshteinDistance("cat", "bat")).isEqualTo(1);
            assertThat(OpenSimilarity.levenshteinDistance("kitten", "sitting")).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("levenshteinSimilarity Tests")
    class LevenshteinSimilarityTests {

        @Test
        @DisplayName("Should delegate to LevenshteinDistance.similarity")
        void shouldDelegateToLevenshteinDistanceSimilarity() {
            assertThat(OpenSimilarity.levenshteinSimilarity("hello", "hello")).isEqualTo(1.0);
            assertThat(OpenSimilarity.levenshteinSimilarity("hello", "hallo")).isCloseTo(0.8, within(0.001));
        }
    }

    @Nested
    @DisplayName("jaccardSimilarity Tests")
    class JaccardSimilarityTests {

        @Test
        @DisplayName("Should calculate with default n-gram")
        void shouldCalculateWithDefaultNgram() {
            assertThat(OpenSimilarity.jaccardSimilarity("hello", "hello")).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should calculate with custom n-gram")
        void shouldCalculateWithCustomNgram() {
            double similarity = OpenSimilarity.jaccardSimilarity("abc", "bcd", 1);
            assertThat(similarity).isCloseTo(0.5, within(0.001));
        }
    }

    @Nested
    @DisplayName("cosineSimilarity Tests")
    class CosineSimilarityTests {

        @Test
        @DisplayName("Should delegate to CosineSimilarity.calculate")
        void shouldDelegateToCosineSimilarityCalculate() {
            assertThat(OpenSimilarity.cosineSimilarity("hello world", "hello world")).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("jaroWinklerSimilarity Tests")
    class JaroWinklerSimilarityTests {

        @Test
        @DisplayName("Should return 1.0 for identical strings")
        void shouldReturnOneForIdenticalStrings() {
            assertThat(OpenSimilarity.jaroWinklerSimilarity("hello", "hello")).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should return 0.0 for null strings")
        void shouldReturnZeroForNullStrings() {
            assertThat(OpenSimilarity.jaroWinklerSimilarity(null, "hello")).isEqualTo(0.0);
            assertThat(OpenSimilarity.jaroWinklerSimilarity("hello", null)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should calculate high similarity for similar strings")
        void shouldCalculateHighSimilarityForSimilarStrings() {
            double similarity = OpenSimilarity.jaroWinklerSimilarity("hello", "hallo");
            assertThat(similarity).isGreaterThan(0.7);
        }

        @Test
        @DisplayName("Should return 0.0 for completely different strings")
        void shouldReturnZeroForCompletelyDifferentStrings() {
            double similarity = OpenSimilarity.jaroWinklerSimilarity("abc", "xyz");
            assertThat(similarity).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should boost score for common prefix")
        void shouldBoostScoreForCommonPrefix() {
            double withPrefix = OpenSimilarity.jaroWinklerSimilarity("prefix_test", "prefix_rest");
            double withoutPrefix = OpenSimilarity.jaroWinklerSimilarity("atest", "brest");
            assertThat(withPrefix).isGreaterThan(withoutPrefix);
        }
    }

    @Nested
    @DisplayName("longestCommonSubsequence Tests")
    class LongestCommonSubsequenceTests {

        @Test
        @DisplayName("Should return 0 for null strings")
        void shouldReturnZeroForNullStrings() {
            assertThat(OpenSimilarity.longestCommonSubsequence(null, "abc")).isZero();
            assertThat(OpenSimilarity.longestCommonSubsequence("abc", null)).isZero();
        }

        @Test
        @DisplayName("Should return length for identical strings")
        void shouldReturnLengthForIdenticalStrings() {
            assertThat(OpenSimilarity.longestCommonSubsequence("abc", "abc")).isEqualTo(3);
        }

        @Test
        @DisplayName("Should calculate LCS correctly")
        void shouldCalculateLcsCorrectly() {
            assertThat(OpenSimilarity.longestCommonSubsequence("ABCDGH", "AEDFHR")).isEqualTo(3);
            assertThat(OpenSimilarity.longestCommonSubsequence("AGGTAB", "GXTXAYB")).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("longestCommonSubstring Tests")
    class LongestCommonSubstringTests {

        @Test
        @DisplayName("Should return 0 for null strings")
        void shouldReturnZeroForNullStrings() {
            assertThat(OpenSimilarity.longestCommonSubstring(null, "abc")).isZero();
            assertThat(OpenSimilarity.longestCommonSubstring("abc", null)).isZero();
        }

        @Test
        @DisplayName("Should return length for identical strings")
        void shouldReturnLengthForIdenticalStrings() {
            assertThat(OpenSimilarity.longestCommonSubstring("abc", "abc")).isEqualTo(3);
        }

        @Test
        @DisplayName("Should calculate longest common substring correctly")
        void shouldCalculateLongestCommonSubstringCorrectly() {
            assertThat(OpenSimilarity.longestCommonSubstring("abcdef", "zbcdf")).isEqualTo(3);
            assertThat(OpenSimilarity.longestCommonSubstring("hello", "jello")).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("isSimilar Tests")
    class IsSimilarTests {

        @Test
        @DisplayName("Should return true for identical strings")
        void shouldReturnTrueForIdenticalStrings() {
            assertThat(OpenSimilarity.isSimilar("hello", "hello", 0.8)).isTrue();
        }

        @Test
        @DisplayName("Should return true when above threshold")
        void shouldReturnTrueWhenAboveThreshold() {
            assertThat(OpenSimilarity.isSimilar("hello", "hallo", 0.7)).isTrue();
        }

        @Test
        @DisplayName("Should return false when below threshold")
        void shouldReturnFalseWhenBelowThreshold() {
            assertThat(OpenSimilarity.isSimilar("hello", "world", 0.8)).isFalse();
        }
    }

    @Nested
    @DisplayName("findMostSimilar Tests")
    class FindMostSimilarTests {

        @Test
        @DisplayName("Should return null for null or empty candidates")
        void shouldReturnNullForNullOrEmptyCandidates() {
            assertThat(OpenSimilarity.findMostSimilar("hello", null)).isNull();
            assertThat(OpenSimilarity.findMostSimilar("hello", List.of())).isNull();
        }

        @Test
        @DisplayName("Should find most similar string")
        void shouldFindMostSimilarString() {
            List<String> candidates = List.of("world", "hallo", "test");
            assertThat(OpenSimilarity.findMostSimilar("hello", candidates)).isEqualTo("hallo");
        }

        @Test
        @DisplayName("Should return exact match when present")
        void shouldReturnExactMatchWhenPresent() {
            List<String> candidates = List.of("world", "hello", "test");
            assertThat(OpenSimilarity.findMostSimilar("hello", candidates)).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("findSimilar Tests")
    class FindSimilarTests {

        @Test
        @DisplayName("Should return empty list for null candidates")
        void shouldReturnEmptyListForNullCandidates() {
            assertThat(OpenSimilarity.findSimilar("hello", null, 0.5)).isEmpty();
        }

        @Test
        @DisplayName("Should find all similar strings above threshold")
        void shouldFindAllSimilarStringsAboveThreshold() {
            List<String> candidates = List.of("hello", "hallo", "jello", "world");
            List<String> similar = OpenSimilarity.findSimilar("hello", candidates, 0.7);
            assertThat(similar).contains("hello", "hallo", "jello");
            assertThat(similar).doesNotContain("world");
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenSimilarity.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
