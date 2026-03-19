package cloud.opencode.base.string.diff;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * DiffResultTest Tests
 * DiffResultTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("DiffResult Tests")
class DiffResultTest {

    @Nested
    @DisplayName("Record Tests")
    class RecordTests {

        @Test
        @DisplayName("Should create DiffResult with all properties")
        void shouldCreateDiffResultWithAllProperties() {
            List<DiffLine> lines = List.of(
                new DiffLine(DiffLine.Type.EQUAL, 0, 0, "line1"),
                new DiffLine(DiffLine.Type.INSERT, -1, 1, "line2")
            );

            DiffResult result = new DiffResult(lines, 1, 0, 0);

            assertThat(result.lines()).hasSize(2);
            assertThat(result.additions()).isEqualTo(1);
            assertThat(result.deletions()).isZero();
            assertThat(result.modifications()).isZero();
        }
    }

    @Nested
    @DisplayName("hasDiff Tests")
    class HasDiffTests {

        @Test
        @DisplayName("Should return false when no changes")
        void shouldReturnFalseWhenNoChanges() {
            DiffResult result = new DiffResult(List.of(), 0, 0, 0);
            assertThat(result.hasDiff()).isFalse();
        }

        @Test
        @DisplayName("Should return true when there are additions")
        void shouldReturnTrueWhenThereAreAdditions() {
            DiffResult result = new DiffResult(List.of(), 1, 0, 0);
            assertThat(result.hasDiff()).isTrue();
        }

        @Test
        @DisplayName("Should return true when there are deletions")
        void shouldReturnTrueWhenThereAreDeletions() {
            DiffResult result = new DiffResult(List.of(), 0, 1, 0);
            assertThat(result.hasDiff()).isTrue();
        }

        @Test
        @DisplayName("Should return true when there are modifications")
        void shouldReturnTrueWhenThereAreModifications() {
            DiffResult result = new DiffResult(List.of(), 0, 0, 1);
            assertThat(result.hasDiff()).isTrue();
        }
    }

    @Nested
    @DisplayName("toUnifiedDiff Tests")
    class ToUnifiedDiffTests {

        @Test
        @DisplayName("Should format unified diff correctly")
        void shouldFormatUnifiedDiffCorrectly() {
            List<DiffLine> lines = List.of(
                new DiffLine(DiffLine.Type.EQUAL, 0, 0, "unchanged"),
                new DiffLine(DiffLine.Type.INSERT, -1, 1, "added"),
                new DiffLine(DiffLine.Type.DELETE, 1, -1, "removed"),
                new DiffLine(DiffLine.Type.MODIFY, 2, 2, "modified")
            );

            DiffResult result = new DiffResult(lines, 1, 1, 1);
            String unified = result.toUnifiedDiff();

            assertThat(unified).contains("  unchanged");
            assertThat(unified).contains("+ added");
            assertThat(unified).contains("- removed");
            assertThat(unified).contains("! modified");
        }
    }

    @Nested
    @DisplayName("toHtml Tests")
    class ToHtmlTests {

        @Test
        @DisplayName("Should format HTML diff correctly")
        void shouldFormatHtmlDiffCorrectly() {
            List<DiffLine> lines = List.of(
                new DiffLine(DiffLine.Type.EQUAL, 0, 0, "unchanged"),
                new DiffLine(DiffLine.Type.INSERT, -1, 1, "added"),
                new DiffLine(DiffLine.Type.DELETE, 1, -1, "removed"),
                new DiffLine(DiffLine.Type.MODIFY, 2, 2, "modified")
            );

            DiffResult result = new DiffResult(lines, 1, 1, 1);
            String html = result.toHtml();

            assertThat(html).startsWith("<div class=\"diff\">");
            assertThat(html).endsWith("</div>");
            assertThat(html).contains("class=\"equal\"");
            assertThat(html).contains("class=\"insert\"");
            assertThat(html).contains("class=\"delete\"");
            assertThat(html).contains("class=\"modify\"");
        }

        @Test
        @DisplayName("Should escape HTML special characters")
        void shouldEscapeHtmlSpecialCharacters() {
            List<DiffLine> lines = List.of(
                new DiffLine(DiffLine.Type.EQUAL, 0, 0, "<div>&test</div>")
            );

            DiffResult result = new DiffResult(lines, 0, 0, 0);
            String html = result.toHtml();

            assertThat(html).contains("&lt;div&gt;");
            assertThat(html).contains("&amp;test");
        }
    }
}
