package cloud.opencode.base.csv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CsvDocument 测试
 */
@DisplayName("CsvDocument 测试")
class CsvDocumentTest {

    private CsvDocument sampleDoc() {
        return CsvDocument.builder()
                .header("name", "age", "role")
                .addRow("Alice", "30", "Engineer")
                .addRow("Bob", "25", "Designer")
                .addRow("Charlie", "35", "Manager")
                .build();
    }

    @Nested
    @DisplayName("构建器")
    class BuilderTest {

        @Test
        @DisplayName("构建带标题和行的文档")
        void buildWithHeaderAndRows() {
            CsvDocument doc = sampleDoc();
            assertThat(doc.headers()).containsExactly("name", "age", "role");
            assertThat(doc.rowCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("构建空文档")
        void buildEmpty() {
            CsvDocument doc = CsvDocument.builder().build();
            assertThat(doc.isEmpty()).isTrue();
            assertThat(doc.headers()).isEmpty();
            assertThat(doc.rowCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("构建无标题文档")
        void buildNoHeader() {
            CsvDocument doc = CsvDocument.builder()
                    .addRow("a", "b")
                    .build();
            assertThat(doc.headers()).isEmpty();
            assertThat(doc.rowCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("使用列表设置标题")
        void headerFromList() {
            CsvDocument doc = CsvDocument.builder()
                    .header(List.of("x", "y"))
                    .build();
            assertThat(doc.headers()).containsExactly("x", "y");
        }

        @Test
        @DisplayName("添加CsvRow对象")
        void addCsvRow() {
            CsvRow row = CsvRow.of(1, "val1", "val2");
            CsvDocument doc = CsvDocument.builder()
                    .header("a", "b")
                    .addRow(row)
                    .build();
            assertThat(doc.getRow(0)).isEqualTo(row);
        }

        @Test
        @DisplayName("添加null行抛出异常")
        void addNullRow() {
            assertThatThrownBy(() -> CsvDocument.builder().addRow((CsvRow) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("行访问")
    class RowAccess {

        @Test
        @DisplayName("通过索引获取行")
        void getRow() {
            CsvDocument doc = sampleDoc();
            CsvRow row = doc.getRow(1);
            assertThat(row.get(0)).isEqualTo("Bob");
        }

        @Test
        @DisplayName("索引越界抛出异常")
        void getRowOutOfBounds() {
            CsvDocument doc = sampleDoc();
            assertThatThrownBy(() -> doc.getRow(5))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("负数索引抛出异常")
        void getRowNegative() {
            CsvDocument doc = sampleDoc();
            assertThatThrownBy(() -> doc.getRow(-1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("列访问")
    class ColumnAccess {

        @Test
        @DisplayName("通过名称获取列")
        void getColumnByName() {
            CsvDocument doc = sampleDoc();
            List<String> ages = doc.getColumn("age");
            assertThat(ages).containsExactly("30", "25", "35");
        }

        @Test
        @DisplayName("通过索引获取列")
        void getColumnByIndex() {
            CsvDocument doc = sampleDoc();
            List<String> names = doc.getColumn(0);
            assertThat(names).containsExactly("Alice", "Bob", "Charlie");
        }

        @Test
        @DisplayName("列名不存在抛出异常")
        void getColumnByNameNotFound() {
            CsvDocument doc = sampleDoc();
            assertThatThrownBy(() -> doc.getColumn("nonexistent"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nonexistent");
        }

        @Test
        @DisplayName("负数列索引抛出异常")
        void getColumnNegativeIndex() {
            CsvDocument doc = sampleDoc();
            assertThatThrownBy(() -> doc.getColumn(-1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("列索引超出行字段数时返回null")
        void getColumnIndexBeyondRowFields() {
            CsvDocument doc = CsvDocument.builder()
                    .header("a", "b", "c")
                    .addRow("1")
                    .build();
            List<String> col = doc.getColumn(2);
            assertThat(col).containsExactly((String) null);
        }

        @Test
        @DisplayName("列名为null抛出异常")
        void getColumnNullName() {
            CsvDocument doc = sampleDoc();
            assertThatThrownBy(() -> doc.getColumn(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("文档属性")
    class DocumentProperties {

        @Test
        @DisplayName("行数正确")
        void rowCount() {
            assertThat(sampleDoc().rowCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("列数基于标题")
        void columnCountFromHeaders() {
            assertThat(sampleDoc().columnCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("无标题时列数基于第一行")
        void columnCountFromFirstRow() {
            CsvDocument doc = CsvDocument.builder()
                    .addRow("a", "b", "c", "d")
                    .build();
            assertThat(doc.columnCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("空文档列数为0")
        void columnCountEmpty() {
            CsvDocument doc = CsvDocument.builder().build();
            assertThat(doc.columnCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("非空文档isEmpty为false")
        void isNotEmpty() {
            assertThat(sampleDoc().isEmpty()).isFalse();
        }

        @Test
        @DisplayName("空文档isEmpty为true")
        void isEmptyTrue() {
            assertThat(CsvDocument.builder().build().isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("流操作")
    class StreamOps {

        @Test
        @DisplayName("stream返回所有行")
        void streamAllRows() {
            CsvDocument doc = sampleDoc();
            assertThat(doc.stream().count()).isEqualTo(3);
        }

        @Test
        @DisplayName("stream支持过滤")
        void streamFilter() {
            CsvDocument doc = sampleDoc();
            long count = doc.stream()
                    .filter(row -> row.get(0).startsWith("A"))
                    .count();
            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("子文档")
    class SubDocument {

        @Test
        @DisplayName("提取子文档")
        void subDocumentRange() {
            CsvDocument doc = sampleDoc();
            CsvDocument sub = doc.subDocument(0, 2);
            assertThat(sub.rowCount()).isEqualTo(2);
            assertThat(sub.getRow(0).get(0)).isEqualTo("Alice");
            assertThat(sub.getRow(1).get(0)).isEqualTo("Bob");
        }

        @Test
        @DisplayName("子文档保留标题")
        void subDocumentKeepsHeaders() {
            CsvDocument doc = sampleDoc();
            CsvDocument sub = doc.subDocument(1, 2);
            assertThat(sub.headers()).containsExactly("name", "age", "role");
        }

        @Test
        @DisplayName("空范围返回空子文档")
        void subDocumentEmptyRange() {
            CsvDocument doc = sampleDoc();
            CsvDocument sub = doc.subDocument(1, 1);
            assertThat(sub.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("无效范围抛出异常")
        void subDocumentInvalidRange() {
            CsvDocument doc = sampleDoc();
            assertThatThrownBy(() -> doc.subDocument(-1, 2))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("起始大于结束抛出异常")
        void subDocumentStartAfterEnd() {
            CsvDocument doc = sampleDoc();
            assertThatThrownBy(() -> doc.subDocument(2, 1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("结束超出范围抛出异常")
        void subDocumentEndBeyondSize() {
            CsvDocument doc = sampleDoc();
            assertThatThrownBy(() -> doc.subDocument(0, 10))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("equals和hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("相同文档相等")
        void sameDocEquals() {
            CsvDocument a = sampleDoc();
            CsvDocument b = sampleDoc();
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("不同行数不相等")
        void differentRowCount() {
            CsvDocument a = sampleDoc();
            CsvDocument b = CsvDocument.builder()
                    .header("name", "age", "role")
                    .addRow("Alice", "30", "Engineer")
                    .build();
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("不同标题不相等")
        void differentHeaders() {
            CsvDocument a = CsvDocument.builder().header("a").build();
            CsvDocument b = CsvDocument.builder().header("b").build();
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("与自身相等")
        void selfEquals() {
            CsvDocument doc = sampleDoc();
            assertThat(doc).isEqualTo(doc);
        }

        @Test
        @DisplayName("与null不相等")
        void notEqualsNull() {
            assertThat(sampleDoc()).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("toString包含标题和行数")
        void toStringContainsInfo() {
            String str = sampleDoc().toString();
            assertThat(str).contains("CsvDocument");
            assertThat(str).contains("name");
            assertThat(str).contains("rowCount=3");
        }

        @Test
        @DisplayName("超过5行时显示省略")
        void toStringTruncates() {
            CsvDocument.Builder builder = CsvDocument.builder().header("x");
            for (int i = 0; i < 10; i++) {
                builder.addRow(String.valueOf(i));
            }
            String str = builder.build().toString();
            assertThat(str).contains("more");
        }
    }

    @Nested
    @DisplayName("不可变性")
    class Immutability {

        @Test
        @DisplayName("headers列表不可修改")
        void headersUnmodifiable() {
            CsvDocument doc = sampleDoc();
            assertThatThrownBy(() -> doc.headers().add("extra"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("rows列表不可修改")
        void rowsUnmodifiable() {
            CsvDocument doc = sampleDoc();
            assertThatThrownBy(() -> doc.rows().add(CsvRow.of("x")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
