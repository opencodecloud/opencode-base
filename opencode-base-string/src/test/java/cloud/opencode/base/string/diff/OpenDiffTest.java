package cloud.opencode.base.string.diff;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenDiffTest Tests
 * OpenDiffTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenDiff Tests")
class OpenDiffTest {

    @Nested
    @DisplayName("diff Tests")
    class DiffTests {

        @Test
        @DisplayName("Should delegate to diffLines")
        void shouldDelegateToDiffLines() {
            DiffResult result = OpenDiff.diff("line1", "line1");
            assertThat(result.hasDiff()).isFalse();
        }
    }

    @Nested
    @DisplayName("diffLines Tests")
    class DiffLinesTests {

        @Test
        @DisplayName("Should return no diff for identical strings")
        void shouldReturnNoDiffForIdenticalStrings() {
            DiffResult result = OpenDiff.diffLines("hello\nworld", "hello\nworld");
            assertThat(result.hasDiff()).isFalse();
        }

        @Test
        @DisplayName("Should handle null input")
        void shouldHandleNullInput() {
            DiffResult result = OpenDiff.diffLines(null, "hello");
            assertThat(result).isNotNull();

            result = OpenDiff.diffLines("hello", null);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should detect additions")
        void shouldDetectAdditions() {
            DiffResult result = OpenDiff.diffLines("line1", "line1\nline2");
            assertThat(result.additions()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should detect deletions")
        void shouldDetectDeletions() {
            DiffResult result = OpenDiff.diffLines("line1\nline2", "line1");
            assertThat(result.deletions()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should detect modifications")
        void shouldDetectModifications() {
            DiffResult result = OpenDiff.diffLines("line1", "line2");
            assertThat(result.modifications()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("diffWords Tests")
    class DiffWordsTests {

        @Test
        @DisplayName("Should compare word by word")
        void shouldCompareWordByWord() {
            DiffResult result = OpenDiff.diffWords("hello world", "hello there");
            assertThat(result.hasDiff()).isTrue();
        }

        @Test
        @DisplayName("Should handle null input")
        void shouldHandleNullInput() {
            DiffResult result = OpenDiff.diffWords(null, "hello");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should return no diff for identical content")
        void shouldReturnNoDiffForIdenticalContent() {
            DiffResult result = OpenDiff.diffWords("hello world", "hello world");
            assertThat(result.additions()).isZero();
            assertThat(result.deletions()).isZero();
        }
    }

    @Nested
    @DisplayName("diffChars Tests")
    class DiffCharsTests {

        @Test
        @DisplayName("Should compare character by character")
        void shouldCompareCharacterByCharacter() {
            DiffResult result = OpenDiff.diffChars("abc", "adc");
            assertThat(result.hasDiff()).isTrue();
        }

        @Test
        @DisplayName("Should handle null input")
        void shouldHandleNullInput() {
            DiffResult result = OpenDiff.diffChars(null, "hello");
            assertThat(result).isNotNull();
            assertThat(result.additions()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should return no diff for identical strings")
        void shouldReturnNoDiffForIdenticalStrings() {
            DiffResult result = OpenDiff.diffChars("hello", "hello");
            assertThat(result.additions()).isZero();
            assertThat(result.deletions()).isZero();
        }
    }

    @Nested
    @DisplayName("unifiedDiff Tests")
    class UnifiedDiffTests {

        @Test
        @DisplayName("Should return unified diff format")
        void shouldReturnUnifiedDiffFormat() {
            String diff = OpenDiff.unifiedDiff("line1", "line2");
            assertThat(diff).contains("!");
        }

        @Test
        @DisplayName("Should include file names when provided")
        void shouldIncludeFileNamesWhenProvided() {
            String diff = OpenDiff.unifiedDiff("a", "b", "original.txt", "revised.txt", 3);
            assertThat(diff).contains("--- original.txt");
            assertThat(diff).contains("+++ revised.txt");
        }
    }

    @Nested
    @DisplayName("htmlDiff Tests")
    class HtmlDiffTests {

        @Test
        @DisplayName("Should return HTML formatted diff")
        void shouldReturnHtmlFormattedDiff() {
            String html = OpenDiff.htmlDiff("line1", "line2");
            assertThat(html).contains("<div class=\"diff\">");
            assertThat(html).contains("</div>");
        }
    }

    @Nested
    @DisplayName("applyPatch Tests")
    class ApplyPatchTests {

        @Test
        @DisplayName("Should throw UnsupportedOperationException")
        void shouldThrowUnsupportedOperationException() {
            assertThatThrownBy(() -> OpenDiff.applyPatch("original", "+ added line"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("not yet fully implemented");
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenDiff.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
