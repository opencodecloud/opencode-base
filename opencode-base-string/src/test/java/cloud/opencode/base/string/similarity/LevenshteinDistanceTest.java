package cloud.opencode.base.string.similarity;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * LevenshteinDistanceTest Tests
 * LevenshteinDistanceTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("LevenshteinDistance Tests")
class LevenshteinDistanceTest {

    @Nested
    @DisplayName("calculate Tests")
    class CalculateTests {

        @Test
        @DisplayName("Should return 0 for identical strings")
        void shouldReturnZeroForIdenticalStrings() {
            assertThat(LevenshteinDistance.calculate("hello", "hello")).isZero();
        }

        @Test
        @DisplayName("Should throw for null strings")
        void shouldThrowForNullStrings() {
            assertThatThrownBy(() -> LevenshteinDistance.calculate(null, "hello"))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> LevenshteinDistance.calculate("hello", null))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> LevenshteinDistance.calculate(null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should return string length for empty comparison")
        void shouldReturnStringLengthForEmptyComparison() {
            assertThat(LevenshteinDistance.calculate("", "hello")).isEqualTo(5);
            assertThat(LevenshteinDistance.calculate("hello", "")).isEqualTo(5);
        }

        @Test
        @DisplayName("Should return 0 for two empty strings")
        void shouldReturnZeroForTwoEmptyStrings() {
            assertThat(LevenshteinDistance.calculate("", "")).isZero();
        }

        @Test
        @DisplayName("Should calculate single character difference")
        void shouldCalculateSingleCharacterDifference() {
            assertThat(LevenshteinDistance.calculate("cat", "bat")).isEqualTo(1);
            assertThat(LevenshteinDistance.calculate("cat", "cart")).isEqualTo(1);
            assertThat(LevenshteinDistance.calculate("cat", "ca")).isEqualTo(1);
        }

        @Test
        @DisplayName("Should calculate multiple character differences")
        void shouldCalculateMultipleCharacterDifferences() {
            assertThat(LevenshteinDistance.calculate("kitten", "sitting")).isEqualTo(3);
            assertThat(LevenshteinDistance.calculate("flaw", "lawn")).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle completely different strings")
        void shouldHandleCompletelyDifferentStrings() {
            assertThat(LevenshteinDistance.calculate("abc", "xyz")).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("similarity Tests")
    class SimilarityTests {

        @Test
        @DisplayName("Should return 1.0 for identical strings")
        void shouldReturnOneForIdenticalStrings() {
            assertThat(LevenshteinDistance.similarity("hello", "hello")).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should throw for null strings")
        void shouldThrowForNullStrings() {
            assertThatThrownBy(() -> LevenshteinDistance.similarity(null, "hello"))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> LevenshteinDistance.similarity("hello", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should return 1.0 for two empty strings")
        void shouldReturnOneForTwoEmptyStrings() {
            assertThat(LevenshteinDistance.similarity("", "")).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should calculate similarity correctly")
        void shouldCalculateSimilarityCorrectly() {
            // "hello" and "hallo" have distance 1, max length 5, similarity = 1 - 1/5 = 0.8
            assertThat(LevenshteinDistance.similarity("hello", "hallo")).isCloseTo(0.8, within(0.001));
        }

        @Test
        @DisplayName("Should return 0.0 for completely different strings")
        void shouldReturnZeroForCompletelyDifferentStrings() {
            assertThat(LevenshteinDistance.similarity("abc", "xyz")).isCloseTo(0.0, within(0.001));
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = LevenshteinDistance.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
