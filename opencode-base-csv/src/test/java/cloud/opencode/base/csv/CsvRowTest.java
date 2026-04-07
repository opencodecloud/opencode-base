package cloud.opencode.base.csv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CsvRow 测试
 */
@DisplayName("CsvRow 测试")
class CsvRowTest {

    @Nested
    @DisplayName("静态工厂方法")
    class FactoryMethods {

        @Test
        @DisplayName("of(String...) 创建行")
        void ofVarargs() {
            CsvRow row = CsvRow.of("a", "b", "c");
            assertThat(row.size()).isEqualTo(3);
            assertThat(row.rowNumber()).isEqualTo(CsvRow.UNKNOWN_ROW);
        }

        @Test
        @DisplayName("of(int, String...) 创建带行号的行")
        void ofWithRowNumber() {
            CsvRow row = CsvRow.of(5, "x", "y");
            assertThat(row.rowNumber()).isEqualTo(5);
            assertThat(row.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("of(int, List) 从列表创建行")
        void ofFromList() {
            CsvRow row = CsvRow.of(1, List.of("a", "b"));
            assertThat(row.size()).isEqualTo(2);
            assertThat(row.get(0)).isEqualTo("a");
        }

        @Test
        @DisplayName("of(int, List) null列表抛出异常")
        void ofFromNullList() {
            assertThatThrownBy(() -> CsvRow.of(1, (List<String>) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("空字段数组")
        void ofEmpty() {
            CsvRow row = CsvRow.of();
            assertThat(row.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("字段访问")
    class FieldAccess {

        @Test
        @DisplayName("通过索引获取字段")
        void getByIndex() {
            CsvRow row = CsvRow.of("alpha", "beta", "gamma");
            assertThat(row.get(0)).isEqualTo("alpha");
            assertThat(row.get(1)).isEqualTo("beta");
            assertThat(row.get(2)).isEqualTo("gamma");
        }

        @Test
        @DisplayName("索引越界抛出异常")
        void getOutOfBounds() {
            CsvRow row = CsvRow.of("a");
            assertThatThrownBy(() -> row.get(5))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("通过标题名获取字段")
        void getByHeaderName() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("Alice", "30")
                    .build();
            CsvRow row = doc.getRow(0);
            assertThat(row.get("age", doc)).isEqualTo("30");
        }

        @Test
        @DisplayName("标题名不存在抛出异常")
        void getByHeaderNameNotFound() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name")
                    .addRow("Alice")
                    .build();
            CsvRow row = doc.getRow(0);
            assertThatThrownBy(() -> row.get("missing", doc))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("headerName为null抛出异常")
        void getByNullHeaderName() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name")
                    .addRow("Alice")
                    .build();
            CsvRow row = doc.getRow(0);
            assertThatThrownBy(() -> row.get(null, doc))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("parent为null抛出异常")
        void getByNullParent() {
            CsvRow row = CsvRow.of("a");
            assertThatThrownBy(() -> row.get("name", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("isEmpty")
    class IsEmpty {

        @Test
        @DisplayName("所有字段为空时isEmpty返回true")
        void allFieldsEmpty() {
            CsvRow row = CsvRow.of("", "");
            assertThat(row.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("零字段时isEmpty返回true")
        void zeroFields() {
            CsvRow row = CsvRow.of();
            assertThat(row.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("有非空字段时isEmpty返回false")
        void nonEmptyField() {
            CsvRow row = CsvRow.of("", "data", "");
            assertThat(row.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("values和stream")
    class ValuesAndStream {

        @Test
        @DisplayName("values返回不可修改列表")
        void valuesUnmodifiable() {
            CsvRow row = CsvRow.of("a", "b");
            assertThatThrownBy(() -> row.values().add("c"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("stream返回所有字段")
        void streamAllFields() {
            CsvRow row = CsvRow.of("x", "y", "z");
            assertThat(row.stream().toList()).containsExactly("x", "y", "z");
        }
    }

    @Nested
    @DisplayName("toMap")
    class ToMap {

        @Test
        @DisplayName("转换为Map")
        void toMapNormal() {
            CsvRow row = CsvRow.of("Alice", "30");
            Map<String, String> map = row.toMap(List.of("name", "age"));
            assertThat(map).containsEntry("name", "Alice");
            assertThat(map).containsEntry("age", "30");
        }

        @Test
        @DisplayName("标题多于字段时缺失值为null")
        void toMapMoreHeaders() {
            CsvRow row = CsvRow.of("Alice");
            Map<String, String> map = row.toMap(List.of("name", "age"));
            assertThat(map).containsEntry("name", "Alice");
            assertThat(map).containsEntry("age", null);
        }

        @Test
        @DisplayName("字段多于标题时多余字段被忽略")
        void toMapMoreFields() {
            CsvRow row = CsvRow.of("Alice", "30", "Engineer");
            Map<String, String> map = row.toMap(List.of("name", "age"));
            assertThat(map).hasSize(2);
        }

        @Test
        @DisplayName("toMap返回不可修改Map")
        void toMapUnmodifiable() {
            CsvRow row = CsvRow.of("a");
            Map<String, String> map = row.toMap(List.of("k"));
            assertThatThrownBy(() -> map.put("x", "y"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("headers为null抛出异常")
        void toMapNullHeaders() {
            CsvRow row = CsvRow.of("a");
            assertThatThrownBy(() -> row.toMap(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("equals和hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("相同字段和行号相等")
        void sameFieldsAndRowNumber() {
            CsvRow a = CsvRow.of(1, "a", "b");
            CsvRow b = CsvRow.of(1, "a", "b");
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("不同字段不相等")
        void differentFields() {
            CsvRow a = CsvRow.of(1, "a");
            CsvRow b = CsvRow.of(1, "b");
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("不同行号不相等")
        void differentRowNumber() {
            CsvRow a = CsvRow.of(1, "a");
            CsvRow b = CsvRow.of(2, "a");
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("与自身相等")
        void selfEquals() {
            CsvRow row = CsvRow.of("a");
            assertThat(row).isEqualTo(row);
        }

        @Test
        @DisplayName("与null不相等")
        void notEqualsNull() {
            assertThat(CsvRow.of("a")).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("toString包含字段值")
        void toStringContainsFields() {
            CsvRow row = CsvRow.of("hello", "world");
            String str = row.toString();
            assertThat(str).contains("CsvRow");
            assertThat(str).contains("hello");
            assertThat(str).contains("world");
        }

        @Test
        @DisplayName("带行号的toString包含行号")
        void toStringWithRowNumber() {
            CsvRow row = CsvRow.of(3, "data");
            assertThat(row.toString()).contains("[3]");
        }

        @Test
        @DisplayName("未知行号的toString不含行号标记")
        void toStringUnknownRowNumber() {
            CsvRow row = CsvRow.of("data");
            assertThat(row.toString()).doesNotContain("[-1]");
        }
    }
}
