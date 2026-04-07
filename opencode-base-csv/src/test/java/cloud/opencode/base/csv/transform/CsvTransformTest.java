package cloud.opencode.base.csv.transform;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;
import cloud.opencode.base.csv.exception.OpenCsvException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CsvTransform unit tests
 * CsvTransform 单元测试
 */
@DisplayName("CsvTransform - CSV transformation pipeline | CSV转换管道")
class CsvTransformTest {

    private CsvDocument sampleDoc() {
        return CsvDocument.builder()
                .header("name", "age", "role")
                .addRow("Alice", "30", "Engineer")
                .addRow("Bob", "25", "Designer")
                .addRow("Charlie", "35", "Manager")
                .build();
    }

    private CsvDocument emptyDoc() {
        return CsvDocument.builder()
                .header("name", "age")
                .build();
    }

    // ==================== Factory | 工厂方法 ====================

    @Nested
    @DisplayName("from() - factory method | 工厂方法")
    class FromTest {

        @Test
        @DisplayName("Creates transform from document | 从文档创建转换")
        void createsTransformFromDocument() {
            CsvDocument doc = sampleDoc();
            CsvDocument result = CsvTransform.from(doc).execute();
            assertThat(result).isEqualTo(doc);
        }

        @Test
        @DisplayName("Throws on null document | null文档抛出异常")
        void throwsOnNullDocument() {
            assertThatThrownBy(() -> CsvTransform.from(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== renameColumn() ====================

    @Nested
    @DisplayName("renameColumn() - single column rename | 单列重命名")
    class RenameColumnTest {

        @Test
        @DisplayName("Renames a column | 重命名列")
        void renamesColumn() {
            CsvDocument result = CsvTransform.from(sampleDoc())
                    .renameColumn("name", "fullName")
                    .execute();
            assertThat(result.headers()).containsExactly("fullName", "age", "role");
            assertThat(result.getRow(0).get(0)).isEqualTo("Alice");
        }

        @Test
        @DisplayName("Throws on non-existent column | 不存在的列抛出异常")
        void throwsOnNonExistentColumn() {
            assertThatThrownBy(() -> CsvTransform.from(sampleDoc())
                    .renameColumn("nonexistent", "newName")
                    .execute())
                    .isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("Throws on blank new name | 空白新名称抛出异常")
        void throwsOnBlankNewName() {
            assertThatThrownBy(() -> CsvTransform.from(sampleDoc())
                    .renameColumn("name", "  ")
                    .execute())
                    .isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("Throws on null old name | null旧名称抛出异常")
        void throwsOnNullOldName() {
            assertThatThrownBy(() -> CsvTransform.from(sampleDoc())
                    .renameColumn(null, "newName"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Throws on null new name | null新名称抛出异常")
        void throwsOnNullNewName() {
            assertThatThrownBy(() -> CsvTransform.from(sampleDoc())
                    .renameColumn("name", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== renameColumns() ====================

    @Nested
    @DisplayName("renameColumns() - batch rename | 批量重命名")
    class RenameColumnsTest {

        @Test
        @DisplayName("Batch renames columns | 批量重命名列")
        void batchRenamesColumns() {
            CsvDocument result = CsvTransform.from(sampleDoc())
                    .renameColumns(Map.of("name", "fullName", "age", "years"))
                    .execute();
            assertThat(result.headers()).contains("fullName", "years", "role");
            assertThat(result.headers()).doesNotContain("name", "age");
        }

        @Test
        @DisplayName("Throws on non-existent column in mapping | 映射中不存在的列抛出异常")
        void throwsOnNonExistentInMapping() {
            assertThatThrownBy(() -> CsvTransform.from(sampleDoc())
                    .renameColumns(Map.of("nonexistent", "newName"))
                    .execute())
                    .isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("Throws on null mapping | null映射抛出异常")
        void throwsOnNullMapping() {
            assertThatThrownBy(() -> CsvTransform.from(sampleDoc())
                    .renameColumns(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== reorderColumns() ====================

    @Nested
    @DisplayName("reorderColumns() - column reordering | 列重新排序")
    class ReorderColumnsTest {

        @Test
        @DisplayName("Reorders columns | 重新排序列")
        void reordersColumns() {
            CsvDocument result = CsvTransform.from(sampleDoc())
                    .reorderColumns("role", "name", "age")
                    .execute();
            assertThat(result.headers()).containsExactly("role", "name", "age");
            assertThat(result.getRow(0).get(0)).isEqualTo("Engineer");
            assertThat(result.getRow(0).get(1)).isEqualTo("Alice");
            assertThat(result.getRow(0).get(2)).isEqualTo("30");
        }

        @Test
        @DisplayName("Reorder with subset of columns | 使用列子集重新排序")
        void reorderWithSubset() {
            CsvDocument result = CsvTransform.from(sampleDoc())
                    .reorderColumns("role", "name")
                    .execute();
            assertThat(result.headers()).containsExactly("role", "name");
            assertThat(result.columnCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Throws on non-existent column | 不存在的列抛出异常")
        void throwsOnNonExistentColumn() {
            assertThatThrownBy(() -> CsvTransform.from(sampleDoc())
                    .reorderColumns("name", "nonexistent")
                    .execute())
                    .isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("Throws on null columns | null列抛出异常")
        void throwsOnNullColumns() {
            assertThatThrownBy(() -> CsvTransform.from(sampleDoc())
                    .reorderColumns((String[]) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== addColumn() ====================

    @Nested
    @DisplayName("addColumn() - add new column | 添加新列")
    class AddColumnTest {

        @Test
        @DisplayName("Adds column with default value | 添加具有默认值的列")
        void addsColumnWithDefault() {
            CsvDocument result = CsvTransform.from(sampleDoc())
                    .addColumn("status", "active")
                    .execute();
            assertThat(result.headers()).containsExactly("name", "age", "role", "status");
            assertThat(result.getRow(0).get(3)).isEqualTo("active");
            assertThat(result.getRow(2).get(3)).isEqualTo("active");
        }

        @Test
        @DisplayName("Adds column with empty default | 添加具有空默认值的列")
        void addsColumnWithEmptyDefault() {
            CsvDocument result = CsvTransform.from(sampleDoc())
                    .addColumn("status", "")
                    .execute();
            assertThat(result.headers()).contains("status");
            assertThat(result.getRow(0).get(3)).isEmpty();
        }

        @Test
        @DisplayName("Adds computed column | 添加计算列")
        void addsComputedColumn() {
            CsvDocument result = CsvTransform.from(sampleDoc())
                    .addColumn("greeting", row -> "Hello, " + row.get(0))
                    .execute();
            assertThat(result.headers()).containsExactly("name", "age", "role", "greeting");
            assertThat(result.getRow(0).get(3)).isEqualTo("Hello, Alice");
            assertThat(result.getRow(1).get(3)).isEqualTo("Hello, Bob");
        }

        @Test
        @DisplayName("Adds column to empty document | 向空文档添加列")
        void addsColumnToEmptyDocument() {
            CsvDocument result = CsvTransform.from(emptyDoc())
                    .addColumn("status", "active")
                    .execute();
            assertThat(result.headers()).containsExactly("name", "age", "status");
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Throws on null name | null名称抛出异常")
        void throwsOnNullName() {
            assertThatThrownBy(() -> CsvTransform.from(sampleDoc())
                    .addColumn(null, "value"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Throws on null mapper | null映射器抛出异常")
        void throwsOnNullMapper() {
            assertThatThrownBy(() -> CsvTransform.from(sampleDoc())
                    .addColumn("col", (java.util.function.Function<CsvRow, String>) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== removeColumns() ====================

    @Nested
    @DisplayName("removeColumns() - column removal | 列移除")
    class RemoveColumnsTest {

        @Test
        @DisplayName("Removes specified columns | 移除指定列")
        void removesColumns() {
            CsvDocument result = CsvTransform.from(sampleDoc())
                    .removeColumns("age")
                    .execute();
            assertThat(result.headers()).containsExactly("name", "role");
            assertThat(result.getRow(0).size()).isEqualTo(2);
            assertThat(result.getRow(0).get(0)).isEqualTo("Alice");
            assertThat(result.getRow(0).get(1)).isEqualTo("Engineer");
        }

        @Test
        @DisplayName("Removes multiple columns | 移除多个列")
        void removesMultipleColumns() {
            CsvDocument result = CsvTransform.from(sampleDoc())
                    .removeColumns("age", "role")
                    .execute();
            assertThat(result.headers()).containsExactly("name");
            assertThat(result.getRow(0).size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Removing non-existent column is no-op | 移除不存在的列无操作")
        void removingNonExistentIsNoOp() {
            CsvDocument result = CsvTransform.from(sampleDoc())
                    .removeColumns("nonexistent")
                    .execute();
            assertThat(result.headers()).containsExactly("name", "age", "role");
        }

        @Test
        @DisplayName("Throws on null columns | null列抛出异常")
        void throwsOnNullColumns() {
            assertThatThrownBy(() -> CsvTransform.from(sampleDoc())
                    .removeColumns((String[]) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== mapColumn() ====================

    @Nested
    @DisplayName("mapColumn() - column value mapping | 列值映射")
    class MapColumnTest {

        @Test
        @DisplayName("Maps column values | 映射列值")
        void mapsColumnValues() {
            CsvDocument result = CsvTransform.from(sampleDoc())
                    .mapColumn("name", String::toUpperCase)
                    .execute();
            assertThat(result.getRow(0).get(0)).isEqualTo("ALICE");
            assertThat(result.getRow(1).get(0)).isEqualTo("BOB");
            assertThat(result.getRow(2).get(0)).isEqualTo("CHARLIE");
        }

        @Test
        @DisplayName("Map does not affect other columns | 映射不影响其他列")
        void mapDoesNotAffectOtherColumns() {
            CsvDocument result = CsvTransform.from(sampleDoc())
                    .mapColumn("name", String::toUpperCase)
                    .execute();
            assertThat(result.getRow(0).get(1)).isEqualTo("30");
            assertThat(result.getRow(0).get(2)).isEqualTo("Engineer");
        }

        @Test
        @DisplayName("Throws on non-existent column | 不存在的列抛出异常")
        void throwsOnNonExistentColumn() {
            assertThatThrownBy(() -> CsvTransform.from(sampleDoc())
                    .mapColumn("nonexistent", String::toUpperCase)
                    .execute())
                    .isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("Throws on null column | null列抛出异常")
        void throwsOnNullColumn() {
            assertThatThrownBy(() -> CsvTransform.from(sampleDoc())
                    .mapColumn(null, String::toUpperCase))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Throws on null mapper | null映射器抛出异常")
        void throwsOnNullMapper() {
            assertThatThrownBy(() -> CsvTransform.from(sampleDoc())
                    .mapColumn("name", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== mapRows() ====================

    @Nested
    @DisplayName("mapRows() - row transformation | 行转换")
    class MapRowsTest {

        @Test
        @DisplayName("Transforms rows | 转换行")
        void transformsRows() {
            CsvDocument result = CsvTransform.from(sampleDoc())
                    .mapRows(row -> CsvRow.of(
                            row.get(0).toUpperCase(),
                            row.get(1),
                            row.get(2).toLowerCase()))
                    .execute();
            assertThat(result.getRow(0).get(0)).isEqualTo("ALICE");
            assertThat(result.getRow(0).get(2)).isEqualTo("engineer");
        }

        @Test
        @DisplayName("Throws on null mapper | null映射器抛出异常")
        void throwsOnNullMapper() {
            assertThatThrownBy(() -> CsvTransform.from(sampleDoc())
                    .mapRows(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Throws when mapper returns null | 映射器返回null时抛出异常")
        void throwsWhenMapperReturnsNull() {
            assertThatThrownBy(() -> CsvTransform.from(sampleDoc())
                    .mapRows(_ -> null)
                    .execute())
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== filterColumns() ====================

    @Nested
    @DisplayName("filterColumns() - column filtering | 列过滤")
    class FilterColumnsTest {

        @Test
        @DisplayName("Keeps matching columns | 保留匹配的列")
        void keepsMatchingColumns() {
            CsvDocument result = CsvTransform.from(sampleDoc())
                    .filterColumns(header -> !header.equals("age"))
                    .execute();
            assertThat(result.headers()).containsExactly("name", "role");
        }

        @Test
        @DisplayName("Filter all columns leaves empty headers | 过滤所有列留下空标题")
        void filterAllColumnsLeavesEmpty() {
            CsvDocument result = CsvTransform.from(sampleDoc())
                    .filterColumns(_ -> false)
                    .execute();
            assertThat(result.headers()).isEmpty();
        }

        @Test
        @DisplayName("Throws on null predicate | null谓词抛出异常")
        void throwsOnNullPredicate() {
            assertThatThrownBy(() -> CsvTransform.from(sampleDoc())
                    .filterColumns(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== Immutability | 不可变性 ====================

    @Nested
    @DisplayName("Immutability verification | 不可变性验证")
    class ImmutabilityTest {

        @Test
        @DisplayName("Steps do not mutate original | 步骤不改变原始对象")
        void stepsDoNotMutateOriginal() {
            CsvDocument doc = sampleDoc();
            CsvTransform original = CsvTransform.from(doc);
            CsvTransform renamed = original.renameColumn("name", "fullName");
            CsvTransform removed = original.removeColumns("age");

            CsvDocument originalResult = original.execute();
            CsvDocument renamedResult = renamed.execute();
            CsvDocument removedResult = removed.execute();

            assertThat(originalResult.headers()).containsExactly("name", "age", "role");
            assertThat(renamedResult.headers()).containsExactly("fullName", "age", "role");
            assertThat(removedResult.headers()).containsExactly("name", "role");
        }

        @Test
        @DisplayName("Multiple executions return same result | 多次执行返回相同结果")
        void multipleExecutionsReturnSameResult() {
            CsvTransform transform = CsvTransform.from(sampleDoc())
                    .renameColumn("name", "fullName")
                    .mapColumn("fullName", String::toUpperCase);
            CsvDocument first = transform.execute();
            CsvDocument second = transform.execute();
            assertThat(first).isEqualTo(second);
        }
    }

    // ==================== Combined Operations | 组合操作 ====================

    @Nested
    @DisplayName("Combined operations | 组合操作")
    class CombinedTest {

        @Test
        @DisplayName("Rename + add + map + remove | 重命名+添加+映射+移除")
        void renameAddMapRemove() {
            CsvDocument result = CsvTransform.from(sampleDoc())
                    .renameColumn("name", "fullName")
                    .addColumn("status", "active")
                    .mapColumn("fullName", String::toUpperCase)
                    .removeColumns("age")
                    .execute();
            assertThat(result.headers()).containsExactly("fullName", "role", "status");
            assertThat(result.getRow(0).get(0)).isEqualTo("ALICE");
            assertThat(result.getRow(0).get(1)).isEqualTo("Engineer");
            assertThat(result.getRow(0).get(2)).isEqualTo("active");
        }

        @Test
        @DisplayName("Reorder + filter columns | 重排+过滤列")
        void reorderAndFilter() {
            CsvDocument result = CsvTransform.from(sampleDoc())
                    .reorderColumns("role", "name", "age")
                    .filterColumns(h -> !h.equals("age"))
                    .execute();
            assertThat(result.headers()).containsExactly("role", "name");
        }

        @Test
        @DisplayName("Empty document through pipeline | 空文档通过管道")
        void emptyDocumentThroughPipeline() {
            CsvDocument result = CsvTransform.from(emptyDoc())
                    .renameColumn("name", "fullName")
                    .addColumn("status", "active")
                    .removeColumns("age")
                    .execute();
            assertThat(result.headers()).containsExactly("fullName", "status");
            assertThat(result.isEmpty()).isTrue();
        }
    }
}
