package cloud.opencode.base.csv.merge;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.exception.OpenCsvException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CsvMerge tests
 * CsvMerge 测试
 */
@DisplayName("CsvMerge - CSV merge utility | CSV合并工具")
class CsvMergeTest {

    // ==================== Helper Methods | 辅助方法 ====================

    private CsvDocument docAB() {
        return CsvDocument.builder()
                .header("id", "name")
                .addRow("1", "Alice")
                .addRow("2", "Bob")
                .build();
    }

    private CsvDocument docCD() {
        return CsvDocument.builder()
                .header("id", "name")
                .addRow("3", "Charlie")
                .addRow("4", "Diana")
                .build();
    }

    private CsvDocument docWithExtra() {
        return CsvDocument.builder()
                .header("id", "name", "extra")
                .addRow("5", "Eve", "x")
                .build();
    }

    private CsvDocument docMissingCol() {
        return CsvDocument.builder()
                .header("id")
                .addRow("6")
                .build();
    }

    private CsvDocument emptyDoc() {
        return CsvDocument.builder()
                .header("id", "name")
                .build();
    }

    // ==================== Concat Varargs | 连接（可变参数） ====================

    @Nested
    @DisplayName("concat(CsvDocument...) | 连接（可变参数）")
    class ConcatVarargs {

        @Test
        @DisplayName("Concat two documents | 连接两个文档")
        void concatTwo() {
            CsvDocument result = CsvMerge.concat(docAB(), docCD());
            assertThat(result.headers()).containsExactly("id", "name");
            assertThat(result.rowCount()).isEqualTo(4);
            assertThat(result.getRow(0).get(1)).isEqualTo("Alice");
            assertThat(result.getRow(2).get(1)).isEqualTo("Charlie");
        }

        @Test
        @DisplayName("Concat single document | 连接单个文档")
        void concatSingle() {
            CsvDocument result = CsvMerge.concat(docAB());
            assertThat(result.rowCount()).isEqualTo(2);
            assertThat(result.headers()).containsExactly("id", "name");
        }

        @Test
        @DisplayName("Concat with extra column ignored | 连接时忽略多余列")
        void concatExtraColumnIgnored() {
            CsvDocument result = CsvMerge.concat(docAB(), docWithExtra());
            assertThat(result.headers()).containsExactly("id", "name");
            assertThat(result.rowCount()).isEqualTo(3);
            // Extra column "extra" is ignored
            assertThat(result.getRow(2).get(0)).isEqualTo("5");
            assertThat(result.getRow(2).get(1)).isEqualTo("Eve");
        }

        @Test
        @DisplayName("Concat with missing column fills empty | 连接时缺少列填充空字符串")
        void concatMissingColumnFilled() {
            CsvDocument result = CsvMerge.concat(docAB(), docMissingCol());
            assertThat(result.headers()).containsExactly("id", "name");
            assertThat(result.rowCount()).isEqualTo(3);
            assertThat(result.getRow(2).get(0)).isEqualTo("6");
            assertThat(result.getRow(2).get(1)).isEqualTo("");
        }

        @Test
        @DisplayName("Concat with empty document | 连接空文档")
        void concatWithEmpty() {
            CsvDocument result = CsvMerge.concat(docAB(), emptyDoc());
            assertThat(result.rowCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Null varargs throws NPE | null可变参数抛出NPE")
        void nullVarargs() {
            assertThatThrownBy(() -> CsvMerge.concat((CsvDocument[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Null element throws NPE | null元素抛出NPE")
        void nullElement() {
            assertThatThrownBy(() -> CsvMerge.concat(docAB(), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Empty varargs throws OpenCsvException | 空可变参数抛出OpenCsvException")
        void emptyVarargs() {
            assertThatThrownBy(() -> CsvMerge.concat(new CsvDocument[0]))
                    .isInstanceOf(OpenCsvException.class);
        }
    }

    // ==================== Concat List | 连接（列表） ====================

    @Nested
    @DisplayName("concat(List<CsvDocument>) | 连接（列表）")
    class ConcatList {

        @Test
        @DisplayName("Concat from list | 从列表连接")
        void concatFromList() {
            CsvDocument result = CsvMerge.concat(List.of(docAB(), docCD()));
            assertThat(result.rowCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("Null list throws NPE | null列表抛出NPE")
        void nullList() {
            assertThatThrownBy(() -> CsvMerge.concat((List<CsvDocument>) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Empty list throws OpenCsvException | 空列表抛出OpenCsvException")
        void emptyList() {
            assertThatThrownBy(() -> CsvMerge.concat(List.of()))
                    .isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("Concat documents without headers | 连接无标题的文档")
        void concatNoHeaders() {
            CsvDocument a = CsvDocument.builder().addRow("1", "a").build();
            CsvDocument b = CsvDocument.builder().addRow("2", "b").build();
            CsvDocument result = CsvMerge.concat(List.of(a, b));
            assertThat(result.headers()).isEmpty();
            assertThat(result.rowCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Headers from first non-empty doc | 使用第一个非空文档的标题")
        void headersFromFirstNonEmpty() {
            CsvDocument noHeader = CsvDocument.builder().addRow("x").build();
            CsvDocument withHeader = CsvDocument.builder()
                    .header("id", "name")
                    .addRow("1", "Alice")
                    .build();
            CsvDocument result = CsvMerge.concat(List.of(noHeader, withHeader));
            assertThat(result.headers()).containsExactly("id", "name");
        }
    }

    // ==================== Inner Join | 内连接 ====================

    @Nested
    @DisplayName("innerJoin | 内连接")
    class InnerJoinTest {

        @Test
        @DisplayName("Basic inner join | 基本内连接")
        void basicInnerJoin() {
            CsvDocument left = CsvDocument.builder()
                    .header("id", "name")
                    .addRow("1", "Alice")
                    .addRow("2", "Bob")
                    .addRow("3", "Charlie")
                    .build();
            CsvDocument right = CsvDocument.builder()
                    .header("id", "score")
                    .addRow("1", "95")
                    .addRow("3", "88")
                    .build();

            CsvDocument result = CsvMerge.innerJoin(left, right, "id");
            assertThat(result.headers()).containsExactly("id", "name", "score");
            assertThat(result.rowCount()).isEqualTo(2);
            assertThat(result.getRow(0).get(0)).isEqualTo("1");
            assertThat(result.getRow(0).get(1)).isEqualTo("Alice");
            assertThat(result.getRow(0).get(2)).isEqualTo("95");
            assertThat(result.getRow(1).get(0)).isEqualTo("3");
            assertThat(result.getRow(1).get(2)).isEqualTo("88");
        }

        @Test
        @DisplayName("Inner join with no matches | 无匹配的内连接")
        void noMatches() {
            CsvDocument left = CsvDocument.builder()
                    .header("id", "name")
                    .addRow("1", "Alice")
                    .build();
            CsvDocument right = CsvDocument.builder()
                    .header("id", "score")
                    .addRow("99", "100")
                    .build();

            CsvDocument result = CsvMerge.innerJoin(left, right, "id");
            assertThat(result.rowCount()).isEqualTo(0);
            assertThat(result.headers()).containsExactly("id", "name", "score");
        }

        @Test
        @DisplayName("Inner join with duplicate keys in right | 右表有重复键的内连接")
        void duplicateKeysRight() {
            CsvDocument left = CsvDocument.builder()
                    .header("id", "name")
                    .addRow("1", "Alice")
                    .build();
            CsvDocument right = CsvDocument.builder()
                    .header("id", "item")
                    .addRow("1", "book")
                    .addRow("1", "pen")
                    .build();

            CsvDocument result = CsvMerge.innerJoin(left, right, "id");
            assertThat(result.rowCount()).isEqualTo(2);
            assertThat(result.getRow(0).get(2)).isEqualTo("book");
            assertThat(result.getRow(1).get(2)).isEqualTo("pen");
        }

        @Test
        @DisplayName("Missing key column in left throws | 左表缺少键列抛出异常")
        void missingKeyLeft() {
            CsvDocument left = CsvDocument.builder()
                    .header("name")
                    .addRow("Alice")
                    .build();
            CsvDocument right = CsvDocument.builder()
                    .header("id", "score")
                    .addRow("1", "95")
                    .build();

            assertThatThrownBy(() -> CsvMerge.innerJoin(left, right, "id"))
                    .isInstanceOf(OpenCsvException.class)
                    .hasMessageContaining("left");
        }

        @Test
        @DisplayName("Missing key column in right throws | 右表缺少键列抛出异常")
        void missingKeyRight() {
            CsvDocument left = CsvDocument.builder()
                    .header("id", "name")
                    .addRow("1", "Alice")
                    .build();
            CsvDocument right = CsvDocument.builder()
                    .header("score")
                    .addRow("95")
                    .build();

            assertThatThrownBy(() -> CsvMerge.innerJoin(left, right, "id"))
                    .isInstanceOf(OpenCsvException.class)
                    .hasMessageContaining("right");
        }

        @Test
        @DisplayName("Null params throw NPE | null参数抛出NPE")
        void nullParams() {
            CsvDocument doc = docAB();
            assertThatThrownBy(() -> CsvMerge.innerJoin(null, doc, "id"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CsvMerge.innerJoin(doc, null, "id"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CsvMerge.innerJoin(doc, doc, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Inner join with empty documents | 空文档的内连接")
        void emptyDocuments() {
            CsvDocument left = CsvDocument.builder()
                    .header("id", "name")
                    .build();
            CsvDocument right = CsvDocument.builder()
                    .header("id", "score")
                    .addRow("1", "95")
                    .build();

            CsvDocument result = CsvMerge.innerJoin(left, right, "id");
            assertThat(result.rowCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Inner join with overlapping non-key column names | 非键列名重叠的内连接")
        void overlappingColumnNames() {
            CsvDocument left = CsvDocument.builder()
                    .header("id", "value")
                    .addRow("1", "left_val")
                    .build();
            CsvDocument right = CsvDocument.builder()
                    .header("id", "value")
                    .addRow("1", "right_val")
                    .build();

            CsvDocument result = CsvMerge.innerJoin(left, right, "id");
            assertThat(result.headers()).containsExactly("id", "value", "value_right");
            assertThat(result.getRow(0).get(1)).isEqualTo("left_val");
            assertThat(result.getRow(0).get(2)).isEqualTo("right_val");
        }
    }

    // ==================== Left Join | 左连接 ====================

    @Nested
    @DisplayName("leftJoin | 左连接")
    class LeftJoinTest {

        @Test
        @DisplayName("Basic left join | 基本左连接")
        void basicLeftJoin() {
            CsvDocument left = CsvDocument.builder()
                    .header("id", "name")
                    .addRow("1", "Alice")
                    .addRow("2", "Bob")
                    .addRow("3", "Charlie")
                    .build();
            CsvDocument right = CsvDocument.builder()
                    .header("id", "score")
                    .addRow("1", "95")
                    .addRow("3", "88")
                    .build();

            CsvDocument result = CsvMerge.leftJoin(left, right, "id");
            assertThat(result.headers()).containsExactly("id", "name", "score");
            assertThat(result.rowCount()).isEqualTo(3);
            // Alice matched
            assertThat(result.getRow(0).get(2)).isEqualTo("95");
            // Bob not matched -> empty
            assertThat(result.getRow(1).get(2)).isEqualTo("");
            // Charlie matched
            assertThat(result.getRow(2).get(2)).isEqualTo("88");
        }

        @Test
        @DisplayName("Left join with no right matches | 右表完全无匹配的左连接")
        void noRightMatches() {
            CsvDocument left = CsvDocument.builder()
                    .header("id", "name")
                    .addRow("1", "Alice")
                    .addRow("2", "Bob")
                    .build();
            CsvDocument right = CsvDocument.builder()
                    .header("id", "score")
                    .addRow("99", "100")
                    .build();

            CsvDocument result = CsvMerge.leftJoin(left, right, "id");
            assertThat(result.rowCount()).isEqualTo(2);
            assertThat(result.getRow(0).get(2)).isEqualTo("");
            assertThat(result.getRow(1).get(2)).isEqualTo("");
        }

        @Test
        @DisplayName("Left join preserves all left rows | 左连接保留所有左表行")
        void preservesAllLeft() {
            CsvDocument left = CsvDocument.builder()
                    .header("id", "name")
                    .addRow("1", "Alice")
                    .addRow("2", "Bob")
                    .build();
            CsvDocument right = CsvDocument.builder()
                    .header("id", "score")
                    .build();

            CsvDocument result = CsvMerge.leftJoin(left, right, "id");
            assertThat(result.rowCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Null params throw NPE | null参数抛出NPE")
        void nullParams() {
            CsvDocument doc = docAB();
            assertThatThrownBy(() -> CsvMerge.leftJoin(null, doc, "id"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CsvMerge.leftJoin(doc, null, "id"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CsvMerge.leftJoin(doc, doc, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Missing key column throws | 缺少键列抛出异常")
        void missingKeyColumn() {
            CsvDocument left = CsvDocument.builder()
                    .header("name")
                    .addRow("Alice")
                    .build();
            CsvDocument right = CsvDocument.builder()
                    .header("id", "score")
                    .addRow("1", "95")
                    .build();

            assertThatThrownBy(() -> CsvMerge.leftJoin(left, right, "id"))
                    .isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("Left join with multiple right columns | 右表多列的左连接")
        void multipleRightColumns() {
            CsvDocument left = CsvDocument.builder()
                    .header("id", "name")
                    .addRow("1", "Alice")
                    .build();
            CsvDocument right = CsvDocument.builder()
                    .header("id", "score", "grade")
                    .addRow("1", "95", "A")
                    .build();

            CsvDocument result = CsvMerge.leftJoin(left, right, "id");
            assertThat(result.headers()).containsExactly("id", "name", "score", "grade");
            assertThat(result.getRow(0).get(2)).isEqualTo("95");
            assertThat(result.getRow(0).get(3)).isEqualTo("A");
        }
    }
}
