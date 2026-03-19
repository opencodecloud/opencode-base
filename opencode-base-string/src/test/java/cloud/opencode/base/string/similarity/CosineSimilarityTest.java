package cloud.opencode.base.string.similarity;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.within;

/**
 * CosineSimilarityTest Tests
 * CosineSimilarityTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("CosineSimilarity Tests")
class CosineSimilarityTest {

    @Nested
    @DisplayName("calculate Tests")
    class CalculateTests {

        @Test
        @DisplayName("Should return 1.0 for identical strings")
        void shouldReturnOneForIdenticalStrings() {
            assertThat(CosineSimilarity.calculate("hello world", "hello world")).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should return 0.0 for null strings")
        void shouldReturnZeroForNullStrings() {
            assertThat(CosineSimilarity.calculate(null, "hello")).isEqualTo(0.0);
            assertThat(CosineSimilarity.calculate("hello", null)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should return 0.0 for empty string vs non-empty")
        void shouldReturnZeroForEmptyVsNonEmpty() {
            assertThat(CosineSimilarity.calculate("", "hello")).isEqualTo(0.0);
            assertThat(CosineSimilarity.calculate("hello", "")).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should calculate similarity for word-based comparison")
        void shouldCalculateSimilarityForWordBasedComparison() {
            double similarity = CosineSimilarity.calculate("the quick brown", "quick brown fox");
            assertThat(similarity).isBetween(0.0, 1.0);
        }

        @Test
        @DisplayName("Should handle case insensitivity")
        void shouldHandleCaseInsensitivity() {
            double similarity = CosineSimilarity.calculate("Hello World", "hello world");
            assertThat(similarity).isCloseTo(1.0, within(0.0001));
        }

        @Test
        @DisplayName("Should return 0.0 for completely different strings")
        void shouldReturnZeroForCompletelyDifferentStrings() {
            double similarity = CosineSimilarity.calculate("abc def", "xyz uvw");
            assertThat(similarity).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should handle repeated words")
        void shouldHandleRepeatedWords() {
            double similarity = CosineSimilarity.calculate("test test test", "test");
            assertThat(similarity).isGreaterThan(0.0);
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = CosineSimilarity.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
