package cloud.opencode.base.csv.split;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;
import cloud.opencode.base.csv.exception.OpenCsvException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CsvSplit 测试
 */
@DisplayName("CsvSplit 测试")
class CsvSplitTest {

    private CsvDocument sampleDoc() {
        return CsvDocument.builder()
                .header("name", "age", "dept")
                .addRow("Alice", "30", "Engineering")
                .addRow("Bob", "25", "Design")
                .addRow("Charlie", "35", "Engineering")
                .addRow("Diana", "28", "Design")
                .addRow("Eve", "32", "Marketing")
                .build();
    }

    @Nested
    @DisplayName("bySize - 按大小拆分")
    class BySizeTest {

        @Test
        @DisplayName("拆分为均匀块")
        void evenChunks() {
            CsvDocument doc = sampleDoc();
            List<CsvDocument> chunks = CsvSplit.bySize(doc, 2);
            assertThat(chunks).hasSize(3);
            assertThat(chunks.get(0).rowCount()).isEqualTo(2);
            assertThat(chunks.get(1).rowCount()).isEqualTo(2);
            assertThat(chunks.get(2).rowCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("maxRows大于行数 - 返回单个文档")
        void maxRowsLargerThanDoc() {
            List<CsvDocument> chunks = CsvSplit.bySize(sampleDoc(), 100);
            assertThat(chunks).hasSize(1);
            assertThat(chunks.getFirst().rowCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("maxRows等于行数 - 返回单个文档")
        void maxRowsEqualToDoc() {
            List<CsvDocument> chunks = CsvSplit.bySize(sampleDoc(), 5);
            assertThat(chunks).hasSize(1);
        }

        @Test
        @DisplayName("maxRows为1 - 每行一个文档")
        void maxRowsOne() {
            List<CsvDocument> chunks = CsvSplit.bySize(sampleDoc(), 1);
            assertThat(chunks).hasSize(5);
            for (CsvDocument chunk : chunks) {
                assertThat(chunk.rowCount()).isEqualTo(1);
                assertThat(chunk.headers()).containsExactly("name", "age", "dept");
            }
        }

        @Test
        @DisplayName("标题保留在每个块中")
        void headersPreserved() {
            List<CsvDocument> chunks = CsvSplit.bySize(sampleDoc(), 2);
            for (CsvDocument chunk : chunks) {
                assertThat(chunk.headers()).containsExactly("name", "age", "dept");
            }
        }

        @Test
        @DisplayName("maxRows <= 0 时抛出OpenCsvException")
        void invalidMaxRows() {
            assertThatThrownBy(() -> CsvSplit.bySize(sampleDoc(), 0))
                    .isInstanceOf(OpenCsvException.class);
            assertThatThrownBy(() -> CsvSplit.bySize(sampleDoc(), -1))
                    .isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("null文档时抛出异常")
        void nullDoc() {
            assertThatThrownBy(() -> CsvSplit.bySize(null, 10))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("空文档 - 返回单个空文档")
        void emptyDoc() {
            CsvDocument empty = CsvDocument.builder().header("name").build();
            List<CsvDocument> chunks = CsvSplit.bySize(empty, 10);
            assertThat(chunks).hasSize(1);
            assertThat(chunks.getFirst().isEmpty()).isTrue();
            assertThat(chunks.getFirst().headers()).containsExactly("name");
        }
    }

    @Nested
    @DisplayName("byCondition - 按条件拆分")
    class ByConditionTest {

        @Test
        @DisplayName("正常分区")
        void normalPartition() {
            List<CsvDocument> parts = CsvSplit.byCondition(
                    sampleDoc(), row -> row.get(2).equals("Engineering"));
            assertThat(parts).hasSize(2);
            assertThat(parts.get(0).rowCount()).isEqualTo(2); // matching
            assertThat(parts.get(1).rowCount()).isEqualTo(3); // non-matching
        }

        @Test
        @DisplayName("全部匹配")
        void allMatch() {
            List<CsvDocument> parts = CsvSplit.byCondition(sampleDoc(), row -> true);
            assertThat(parts.get(0).rowCount()).isEqualTo(5);
            assertThat(parts.get(1).rowCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("全不匹配")
        void noneMatch() {
            List<CsvDocument> parts = CsvSplit.byCondition(sampleDoc(), row -> false);
            assertThat(parts.get(0).rowCount()).isEqualTo(0);
            assertThat(parts.get(1).rowCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("标题保留")
        void headersPreserved() {
            List<CsvDocument> parts = CsvSplit.byCondition(sampleDoc(), row -> true);
            assertThat(parts.get(0).headers()).containsExactly("name", "age", "dept");
            assertThat(parts.get(1).headers()).containsExactly("name", "age", "dept");
        }

        @Test
        @DisplayName("空文档 - 返回两个空文档")
        void emptyDoc() {
            CsvDocument empty = CsvDocument.builder().header("x").build();
            List<CsvDocument> parts = CsvSplit.byCondition(empty, row -> true);
            assertThat(parts).hasSize(2);
            assertThat(parts.get(0).isEmpty()).isTrue();
            assertThat(parts.get(1).isEmpty()).isTrue();
        }

        @Test
        @DisplayName("null参数时抛出异常")
        void nullParams() {
            assertThatThrownBy(() -> CsvSplit.byCondition(null, row -> true))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CsvSplit.byCondition(sampleDoc(), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("byColumn - 按列值拆分")
    class ByColumnTest {

        @Test
        @DisplayName("按部门分组")
        void groupByDept() {
            Map<String, CsvDocument> groups = CsvSplit.byColumn(sampleDoc(), "dept");
            assertThat(groups).hasSize(3);
            assertThat(groups.get("Engineering").rowCount()).isEqualTo(2);
            assertThat(groups.get("Design").rowCount()).isEqualTo(2);
            assertThat(groups.get("Marketing").rowCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("每个分组保留标题")
        void headersPreserved() {
            Map<String, CsvDocument> groups = CsvSplit.byColumn(sampleDoc(), "dept");
            for (CsvDocument doc : groups.values()) {
                assertThat(doc.headers()).containsExactly("name", "age", "dept");
            }
        }

        @Test
        @DisplayName("单一值 - 返回一个分组")
        void singleGroup() {
            CsvDocument doc = CsvDocument.builder()
                    .header("val")
                    .addRow("A")
                    .addRow("A")
                    .build();
            Map<String, CsvDocument> groups = CsvSplit.byColumn(doc, "val");
            assertThat(groups).hasSize(1);
            assertThat(groups.get("A").rowCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("列不存在时抛出OpenCsvException")
        void missingColumn() {
            assertThatThrownBy(() -> CsvSplit.byColumn(sampleDoc(), "nonexistent"))
                    .isInstanceOf(OpenCsvException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("空文档 - 返回空Map")
        void emptyDoc() {
            CsvDocument empty = CsvDocument.builder().header("dept").build();
            Map<String, CsvDocument> groups = CsvSplit.byColumn(empty, "dept");
            assertThat(groups).isEmpty();
        }

        @Test
        @DisplayName("null参数时抛出异常")
        void nullParams() {
            assertThatThrownBy(() -> CsvSplit.byColumn(null, "dept"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CsvSplit.byColumn(sampleDoc(), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
