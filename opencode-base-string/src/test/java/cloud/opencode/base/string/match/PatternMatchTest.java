package cloud.opencode.base.string.match;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * PatternMatchTest Tests
 * PatternMatchTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("PatternMatch Tests")
class PatternMatchTest {

    @Nested
    @DisplayName("Record Tests")
    class RecordTests {

        @Test
        @DisplayName("Should create pattern match record")
        void shouldCreatePatternMatchRecord() {
            PatternMatch match = new PatternMatch("test", 0, 4, "test");
            assertThat(match.pattern()).isEqualTo("test");
            assertThat(match.start()).isEqualTo(0);
            assertThat(match.end()).isEqualTo(4);
            assertThat(match.matchedText()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("length Tests")
    class LengthTests {

        @Test
        @DisplayName("Should return correct length")
        void shouldReturnCorrectLength() {
            PatternMatch match = new PatternMatch("test", 5, 9, "test");
            assertThat(match.length()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("overlaps Tests")
    class OverlapsTests {

        @Test
        @DisplayName("Should detect overlapping matches")
        void shouldDetectOverlappingMatches() {
            PatternMatch match1 = new PatternMatch("ab", 0, 2, "ab");
            PatternMatch match2 = new PatternMatch("bc", 1, 3, "bc");
            assertThat(match1.overlaps(match2)).isTrue();
        }

        @Test
        @DisplayName("Should detect non-overlapping matches")
        void shouldDetectNonOverlappingMatches() {
            PatternMatch match1 = new PatternMatch("ab", 0, 2, "ab");
            PatternMatch match2 = new PatternMatch("cd", 3, 5, "cd");
            assertThat(match1.overlaps(match2)).isFalse();
        }

        @Test
        @DisplayName("Should detect adjacent matches as non-overlapping")
        void shouldDetectAdjacentMatchesAsNonOverlapping() {
            PatternMatch match1 = new PatternMatch("ab", 0, 2, "ab");
            PatternMatch match2 = new PatternMatch("cd", 2, 4, "cd");
            assertThat(match1.overlaps(match2)).isFalse();
        }
    }

    @Nested
    @DisplayName("contains Tests")
    class ContainsTests {

        @Test
        @DisplayName("Should detect contained match")
        void shouldDetectContainedMatch() {
            PatternMatch outer = new PatternMatch("abcd", 0, 4, "abcd");
            PatternMatch inner = new PatternMatch("bc", 1, 3, "bc");
            assertThat(outer.contains(inner)).isTrue();
        }

        @Test
        @DisplayName("Should detect non-contained match")
        void shouldDetectNonContainedMatch() {
            PatternMatch match1 = new PatternMatch("ab", 0, 2, "ab");
            PatternMatch match2 = new PatternMatch("cd", 3, 5, "cd");
            assertThat(match1.contains(match2)).isFalse();
        }

        @Test
        @DisplayName("Should contain itself")
        void shouldContainItself() {
            PatternMatch match = new PatternMatch("test", 0, 4, "test");
            assertThat(match.contains(match)).isTrue();
        }
    }

    @Nested
    @DisplayName("extractFrom Tests")
    class ExtractFromTests {

        @Test
        @DisplayName("Should extract from original text")
        void shouldExtractFromOriginalText() {
            PatternMatch match = new PatternMatch("test", 5, 9, "test");
            assertThat(match.extractFrom("Hellotest!")).isEqualTo("test");
        }

        @Test
        @DisplayName("Should return matched text for null original")
        void shouldReturnMatchedTextForNullOriginal() {
            PatternMatch match = new PatternMatch("test", 0, 4, "test");
            assertThat(match.extractFrom(null)).isEqualTo("test");
        }

        @Test
        @DisplayName("Should return matched text for invalid indices")
        void shouldReturnMatchedTextForInvalidIndices() {
            PatternMatch match = new PatternMatch("test", 100, 104, "test");
            assertThat(match.extractFrom("short")).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("toDisplayString Tests")
    class ToDisplayStringTests {

        @Test
        @DisplayName("Should return formatted display string")
        void shouldReturnFormattedDisplayString() {
            PatternMatch match = new PatternMatch("test", 5, 9, "test");
            assertThat(match.toDisplayString()).isEqualTo("'test' at [5-9]");
        }
    }
}
