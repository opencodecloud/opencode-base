package cloud.opencode.base.string.match;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * FuzzyMatchTest Tests
 * FuzzyMatchTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("FuzzyMatch Tests")
class FuzzyMatchTest {

    @Nested
    @DisplayName("Record Tests")
    class RecordTests {

        @Test
        @DisplayName("Should create record with all fields")
        void shouldCreateRecordWithAllFields() {
            FuzzyMatch<String> match = new FuzzyMatch<>("apple", "apple", 1.0);
            assertThat(match.item()).isEqualTo("apple");
            assertThat(match.key()).isEqualTo("apple");
            assertThat(match.score()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should support equals and hashCode")
        void shouldSupportEqualsAndHashCode() {
            FuzzyMatch<String> match1 = new FuzzyMatch<>("apple", "apple", 1.0);
            FuzzyMatch<String> match2 = new FuzzyMatch<>("apple", "apple", 1.0);
            assertThat(match1).isEqualTo(match2);
            assertThat(match1.hashCode()).isEqualTo(match2.hashCode());
        }
    }

    @Nested
    @DisplayName("scoreAsPercent Tests")
    class ScoreAsPercentTests {

        @Test
        @DisplayName("Should format score as percentage")
        void shouldFormatScoreAsPercentage() {
            FuzzyMatch<String> match = new FuzzyMatch<>("apple", "apple", 0.857);
            assertThat(match.scoreAsPercent()).isEqualTo("85.7%");
        }

        @Test
        @DisplayName("Should handle 100%")
        void shouldHandle100Percent() {
            FuzzyMatch<String> match = new FuzzyMatch<>("apple", "apple", 1.0);
            assertThat(match.scoreAsPercent()).isEqualTo("100.0%");
        }

        @Test
        @DisplayName("Should handle 0%")
        void shouldHandle0Percent() {
            FuzzyMatch<String> match = new FuzzyMatch<>("apple", "apple", 0.0);
            assertThat(match.scoreAsPercent()).isEqualTo("0.0%");
        }
    }

    @Nested
    @DisplayName("isExactMatch Tests")
    class IsExactMatchTests {

        @Test
        @DisplayName("Should return true for score 1.0")
        void shouldReturnTrueForScore1() {
            FuzzyMatch<String> match = new FuzzyMatch<>("apple", "apple", 1.0);
            assertThat(match.isExactMatch()).isTrue();
        }

        @Test
        @DisplayName("Should return true for score >= 0.9999")
        void shouldReturnTrueForScoreAbove09999() {
            FuzzyMatch<String> match = new FuzzyMatch<>("apple", "apple", 0.9999);
            assertThat(match.isExactMatch()).isTrue();
        }

        @Test
        @DisplayName("Should return false for lower scores")
        void shouldReturnFalseForLowerScores() {
            FuzzyMatch<String> match = new FuzzyMatch<>("apple", "apple", 0.9);
            assertThat(match.isExactMatch()).isFalse();
        }
    }

    @Nested
    @DisplayName("isStrongMatch Tests")
    class IsStrongMatchTests {

        @Test
        @DisplayName("Should return true for score >= 0.8")
        void shouldReturnTrueForScoreAbove08() {
            FuzzyMatch<String> match1 = new FuzzyMatch<>("apple", "apple", 0.8);
            FuzzyMatch<String> match2 = new FuzzyMatch<>("apple", "apple", 0.9);
            assertThat(match1.isStrongMatch()).isTrue();
            assertThat(match2.isStrongMatch()).isTrue();
        }

        @Test
        @DisplayName("Should return false for score < 0.8")
        void shouldReturnFalseForScoreBelow08() {
            FuzzyMatch<String> match = new FuzzyMatch<>("apple", "apple", 0.7);
            assertThat(match.isStrongMatch()).isFalse();
        }
    }

    @Nested
    @DisplayName("isWeakMatch Tests")
    class IsWeakMatchTests {

        @Test
        @DisplayName("Should return true for score < 0.6")
        void shouldReturnTrueForScoreBelow06() {
            FuzzyMatch<String> match = new FuzzyMatch<>("apple", "apple", 0.5);
            assertThat(match.isWeakMatch()).isTrue();
        }

        @Test
        @DisplayName("Should return false for score >= 0.6")
        void shouldReturnFalseForScoreAbove06() {
            FuzzyMatch<String> match1 = new FuzzyMatch<>("apple", "apple", 0.6);
            FuzzyMatch<String> match2 = new FuzzyMatch<>("apple", "apple", 0.8);
            assertThat(match1.isWeakMatch()).isFalse();
            assertThat(match2.isWeakMatch()).isFalse();
        }
    }
}
