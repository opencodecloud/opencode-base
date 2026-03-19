package cloud.opencode.base.string.similarity;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * JaccardSimilarityTest Tests
 * JaccardSimilarityTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("JaccardSimilarity Tests")
class JaccardSimilarityTest {

    @Nested
    @DisplayName("calculate with default n-gram Tests")
    class CalculateDefaultTests {

        @Test
        @DisplayName("Should return 1.0 for identical strings")
        void shouldReturnOneForIdenticalStrings() {
            assertThat(JaccardSimilarity.calculate("hello", "hello")).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should return 0.0 for null strings")
        void shouldReturnZeroForNullStrings() {
            assertThat(JaccardSimilarity.calculate(null, "hello")).isEqualTo(0.0);
            assertThat(JaccardSimilarity.calculate("hello", null)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should return 1.0 for both empty strings")
        void shouldReturnOneForBothEmptyStrings() {
            assertThat(JaccardSimilarity.calculate("", "")).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should calculate similarity for similar strings")
        void shouldCalculateSimilarityForSimilarStrings() {
            double similarity = JaccardSimilarity.calculate("hello", "hallo");
            assertThat(similarity).isBetween(0.0, 1.0);
        }

        @Test
        @DisplayName("Should return lower similarity for different strings")
        void shouldReturnLowerSimilarityForDifferentStrings() {
            double similar = JaccardSimilarity.calculate("hello", "hallo");
            double different = JaccardSimilarity.calculate("hello", "world");
            assertThat(similar).isGreaterThan(different);
        }
    }

    @Nested
    @DisplayName("calculate with custom n-gram Tests")
    class CalculateCustomNgramTests {

        @Test
        @DisplayName("Should work with n-gram of 1")
        void shouldWorkWithNgramOfOne() {
            double similarity = JaccardSimilarity.calculate("abc", "bcd", 1);
            // Common chars: b, c. Union: a, b, c, d. Jaccard = 2/4 = 0.5
            assertThat(similarity).isCloseTo(0.5, within(0.001));
        }

        @Test
        @DisplayName("Should work with n-gram of 3")
        void shouldWorkWithNgramOfThree() {
            double similarity = JaccardSimilarity.calculate("hello", "hello", 3);
            assertThat(similarity).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should return 1.0 for identical strings with any n-gram")
        void shouldReturnOneForIdenticalStrings() {
            assertThat(JaccardSimilarity.calculate("test", "test", 1)).isEqualTo(1.0);
            assertThat(JaccardSimilarity.calculate("test", "test", 2)).isEqualTo(1.0);
            assertThat(JaccardSimilarity.calculate("test", "test", 3)).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = JaccardSimilarity.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
