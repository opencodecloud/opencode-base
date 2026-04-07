package cloud.opencode.base.csv.diff;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.exception.OpenCsvException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for CsvDiff
 * CsvDiff测试
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-csv V1.0.3
 */
@DisplayName("CsvDiff - CSV差异比较")
class CsvDiffTest {

    // ==================== Positional Diff | 位置差异 ====================

    @Nested
    @DisplayName("位置差异比较")
    class PositionalDiff {

        @Test
        @DisplayName("相同文档无变更")
        void sameDocumentsNoChanges() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("Alice", "30")
                    .addRow("Bob", "25")
                    .build();

            List<CsvChange> changes = CsvDiff.diff(doc, doc);

            assertThat(changes).isEmpty();
        }

        @Test
        @DisplayName("检测新增行")
        void detectAddedRows() {
            CsvDocument original = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("Alice", "30")
                    .build();

            CsvDocument modified = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("Alice", "30")
                    .addRow("Bob", "25")
                    .addRow("Carol", "28")
                    .build();

            List<CsvChange> changes = CsvDiff.diff(original, modified);

            assertThat(changes).hasSize(2);
            assertThat(changes).allMatch(c -> c.type() == CsvChange.ChangeType.ADDED);
            assertThat(changes.get(0).rowIndex()).isEqualTo(1);
            assertThat(changes.get(0).newRow().get(0)).isEqualTo("Bob");
            assertThat(changes.get(1).rowIndex()).isEqualTo(2);
            assertThat(changes.get(1).newRow().get(0)).isEqualTo("Carol");
        }

        @Test
        @DisplayName("检测删除行")
        void detectRemovedRows() {
            CsvDocument original = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("Alice", "30")
                    .addRow("Bob", "25")
                    .addRow("Carol", "28")
                    .build();

            CsvDocument modified = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("Alice", "30")
                    .build();

            List<CsvChange> changes = CsvDiff.diff(original, modified);

            assertThat(changes).hasSize(2);
            assertThat(changes).allMatch(c -> c.type() == CsvChange.ChangeType.REMOVED);
            assertThat(changes.get(0).oldRow().get(0)).isEqualTo("Bob");
            assertThat(changes.get(1).oldRow().get(0)).isEqualTo("Carol");
        }

        @Test
        @DisplayName("检测修改行")
        void detectModifiedRows() {
            CsvDocument original = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("Alice", "30")
                    .addRow("Bob", "25")
                    .build();

            CsvDocument modified = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("Alice", "31")
                    .addRow("Bob", "25")
                    .build();

            List<CsvChange> changes = CsvDiff.diff(original, modified);

            assertThat(changes).hasSize(1);
            assertThat(changes.get(0).type()).isEqualTo(CsvChange.ChangeType.MODIFIED);
            assertThat(changes.get(0).rowIndex()).isEqualTo(0);
            assertThat(changes.get(0).oldRow().get(1)).isEqualTo("30");
            assertThat(changes.get(0).newRow().get(1)).isEqualTo("31");
        }

        @Test
        @DisplayName("混合变更检测")
        void detectMixedChanges() {
            CsvDocument original = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("Alice", "30")
                    .addRow("Bob", "25")
                    .addRow("Carol", "28")
                    .build();

            CsvDocument modified = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("Alice", "31")
                    .addRow("Dave", "35")
                    .build();

            List<CsvChange> changes = CsvDiff.diff(original, modified);

            // Row 0: modified (30->31), Row 1: modified (Bob->Dave), Row 2: removed
            assertThat(changes).hasSize(3);

            long modifiedCount = changes.stream()
                    .filter(c -> c.type() == CsvChange.ChangeType.MODIFIED)
                    .count();
            long removedCount = changes.stream()
                    .filter(c -> c.type() == CsvChange.ChangeType.REMOVED)
                    .count();

            assertThat(modifiedCount).isEqualTo(2);
            assertThat(removedCount).isEqualTo(1);
        }

        @Test
        @DisplayName("空文档差异比较")
        void emptyDocuments() {
            CsvDocument empty1 = CsvDocument.builder().header("a").build();
            CsvDocument empty2 = CsvDocument.builder().header("a").build();

            List<CsvChange> changes = CsvDiff.diff(empty1, empty2);

            assertThat(changes).isEmpty();
        }

        @Test
        @DisplayName("null参数校验")
        void nullParameterValidation() {
            CsvDocument doc = CsvDocument.builder().header("a").build();

            assertThatThrownBy(() -> CsvDiff.diff(null, doc))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CsvDiff.diff(doc, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== Key-Based Diff | 基于键的差异 ====================

    @Nested
    @DisplayName("基于键列的差异比较")
    class KeyBasedDiff {

        @Test
        @DisplayName("使用键列匹配行")
        void diffByKeyColumn() {
            CsvDocument original = CsvDocument.builder()
                    .header("id", "name", "age")
                    .addRow("1", "Alice", "30")
                    .addRow("2", "Bob", "25")
                    .addRow("3", "Carol", "28")
                    .build();

            CsvDocument modified = CsvDocument.builder()
                    .header("id", "name", "age")
                    .addRow("1", "Alice", "31")
                    .addRow("3", "Carol", "28")
                    .addRow("4", "Dave", "35")
                    .build();

            List<CsvChange> changes = CsvDiff.diffByKey(original, modified, "id");

            assertThat(changes).hasSize(3);

            // ID 1: MODIFIED (age 30->31)
            CsvChange modified1 = changes.stream()
                    .filter(c -> c.type() == CsvChange.ChangeType.MODIFIED)
                    .findFirst().orElseThrow();
            assertThat(modified1.oldRow().get(2)).isEqualTo("30");
            assertThat(modified1.newRow().get(2)).isEqualTo("31");

            // ID 2: REMOVED
            CsvChange removed = changes.stream()
                    .filter(c -> c.type() == CsvChange.ChangeType.REMOVED)
                    .findFirst().orElseThrow();
            assertThat(removed.oldRow().get(1)).isEqualTo("Bob");

            // ID 4: ADDED
            CsvChange added = changes.stream()
                    .filter(c -> c.type() == CsvChange.ChangeType.ADDED)
                    .findFirst().orElseThrow();
            assertThat(added.newRow().get(1)).isEqualTo("Dave");
        }

        @Test
        @DisplayName("键列不存在时抛出异常")
        void keyColumnNotFound() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("Alice", "30")
                    .build();

            assertThatThrownBy(() -> CsvDiff.diffByKey(doc, doc, "id"))
                    .isInstanceOf(OpenCsvException.class)
                    .hasMessageContaining("id")
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("键列比较空文档")
        void diffByKeyEmptyDocuments() {
            CsvDocument empty1 = CsvDocument.builder().header("id", "name").build();
            CsvDocument empty2 = CsvDocument.builder().header("id", "name").build();

            List<CsvChange> changes = CsvDiff.diffByKey(empty1, empty2, "id");

            assertThat(changes).isEmpty();
        }

        @Test
        @DisplayName("null参数校验")
        void nullParameterValidation() {
            CsvDocument doc = CsvDocument.builder().header("id").build();

            assertThatThrownBy(() -> CsvDiff.diffByKey(null, doc, "id"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CsvDiff.diffByKey(doc, null, "id"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CsvDiff.diffByKey(doc, doc, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
