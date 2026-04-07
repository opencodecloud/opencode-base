package cloud.opencode.base.csv.query;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;
import cloud.opencode.base.csv.exception.OpenCsvException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CsvQuery unit tests
 * CsvQuery 单元测试
 */
@DisplayName("CsvQuery - CSV query builder | CSV查询构建器")
class CsvQueryTest {

    private CsvDocument sampleDoc() {
        return CsvDocument.builder()
                .header("name", "age", "role")
                .addRow("Alice", "30", "Engineer")
                .addRow("Bob", "25", "Designer")
                .addRow("Charlie", "35", "Engineer")
                .addRow("Diana", "28", "Manager")
                .addRow("Eve", "25", "Designer")
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
        @DisplayName("Creates query from document | 从文档创建查询")
        void createsQueryFromDocument() {
            CsvDocument doc = sampleDoc();
            CsvQuery query = CsvQuery.from(doc);
            assertThat(query.execute()).isEqualTo(doc);
        }

        @Test
        @DisplayName("Throws on null document | null文档抛出异常")
        void throwsOnNullDocument() {
            assertThatThrownBy(() -> CsvQuery.from(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== select() ====================

    @Nested
    @DisplayName("select() - column projection | 列投影")
    class SelectTest {

        @Test
        @DisplayName("Selects specific columns | 选择特定列")
        void selectsSpecificColumns() {
            CsvDocument result = CsvQuery.from(sampleDoc())
                    .select("name", "role")
                    .execute();
            assertThat(result.headers()).containsExactly("name", "role");
            assertThat(result.rowCount()).isEqualTo(5);
            assertThat(result.getRow(0).get(0)).isEqualTo("Alice");
            assertThat(result.getRow(0).get(1)).isEqualTo("Engineer");
        }

        @Test
        @DisplayName("Select single column | 选择单列")
        void selectSingleColumn() {
            CsvDocument result = CsvQuery.from(sampleDoc())
                    .select("age")
                    .execute();
            assertThat(result.headers()).containsExactly("age");
            assertThat(result.getRow(0).get(0)).isEqualTo("30");
        }

        @Test
        @DisplayName("Throws on non-existent column | 不存在的列抛出异常")
        void throwsOnNonExistentColumn() {
            assertThatThrownBy(() -> CsvQuery.from(sampleDoc())
                    .select("nonexistent")
                    .execute())
                    .isInstanceOf(OpenCsvException.class)
                    .hasMessageContaining("nonexistent");
        }

        @Test
        @DisplayName("Throws on null columns | null列抛出异常")
        void throwsOnNullColumns() {
            assertThatThrownBy(() -> CsvQuery.from(sampleDoc()).select((String[]) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== where() ====================

    @Nested
    @DisplayName("where() - row filtering | 行过滤")
    class WhereTest {

        @Test
        @DisplayName("Filters rows by predicate | 按谓词过滤行")
        void filtersRowsByPredicate() {
            CsvDocument result = CsvQuery.from(sampleDoc())
                    .where(row -> "Engineer".equals(row.get(2)))
                    .execute();
            assertThat(result.rowCount()).isEqualTo(2);
            assertThat(result.getRow(0).get(0)).isEqualTo("Alice");
            assertThat(result.getRow(1).get(0)).isEqualTo("Charlie");
        }

        @Test
        @DisplayName("Multiple where clauses combine with AND | 多个where子句AND组合")
        void multipleWhereCombineWithAnd() {
            CsvDocument result = CsvQuery.from(sampleDoc())
                    .where(row -> "Engineer".equals(row.get(2)))
                    .where(row -> Integer.parseInt(row.get(1)) > 30)
                    .execute();
            assertThat(result.rowCount()).isEqualTo(1);
            assertThat(result.getRow(0).get(0)).isEqualTo("Charlie");
        }

        @Test
        @DisplayName("Filter that matches nothing returns empty | 无匹配过滤器返回空")
        void filterMatchingNothingReturnsEmpty() {
            CsvDocument result = CsvQuery.from(sampleDoc())
                    .where(_ -> false)
                    .execute();
            assertThat(result.isEmpty()).isTrue();
            assertThat(result.headers()).containsExactly("name", "age", "role");
        }

        @Test
        @DisplayName("Throws on null predicate | null谓词抛出异常")
        void throwsOnNullPredicate() {
            assertThatThrownBy(() -> CsvQuery.from(sampleDoc()).where(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== orderBy() ====================

    @Nested
    @DisplayName("orderBy() - sorting | 排序")
    class OrderByTest {

        @Test
        @DisplayName("Sorts ascending by string | 按字符串升序排序")
        void sortsAscendingByString() {
            CsvDocument result = CsvQuery.from(sampleDoc())
                    .orderBy("name", true)
                    .execute();
            List<String> names = result.getColumn("name");
            assertThat(names).containsExactly("Alice", "Bob", "Charlie", "Diana", "Eve");
        }

        @Test
        @DisplayName("Sorts descending by string | 按字符串降序排序")
        void sortsDescendingByString() {
            CsvDocument result = CsvQuery.from(sampleDoc())
                    .orderBy("name", false)
                    .execute();
            List<String> names = result.getColumn("name");
            assertThat(names).containsExactly("Eve", "Diana", "Charlie", "Bob", "Alice");
        }

        @Test
        @DisplayName("Sorts with custom comparator | 使用自定义比较器排序")
        void sortsWithCustomComparator() {
            CsvDocument result = CsvQuery.from(sampleDoc())
                    .orderBy("age", Comparator.comparingInt(Integer::parseInt))
                    .execute();
            List<String> ages = result.getColumn("age");
            assertThat(ages).containsExactly("25", "25", "28", "30", "35");
        }

        @Test
        @DisplayName("Throws on non-existent sort column | 不存在的排序列抛出异常")
        void throwsOnNonExistentSortColumn() {
            assertThatThrownBy(() -> CsvQuery.from(sampleDoc())
                    .orderBy("nonexistent", true)
                    .execute())
                    .isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("Throws on null column | null列抛出异常")
        void throwsOnNullColumn() {
            assertThatThrownBy(() -> CsvQuery.from(sampleDoc()).orderBy(null, true))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Throws on null comparator | null比较器抛出异常")
        void throwsOnNullComparator() {
            assertThatThrownBy(() -> CsvQuery.from(sampleDoc()).orderBy("name", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== limit() / offset() ====================

    @Nested
    @DisplayName("limit() and offset() - pagination | 分页")
    class PaginationTest {

        @Test
        @DisplayName("Limits result rows | 限制结果行数")
        void limitsResultRows() {
            CsvDocument result = CsvQuery.from(sampleDoc())
                    .limit(2)
                    .execute();
            assertThat(result.rowCount()).isEqualTo(2);
            assertThat(result.getRow(0).get(0)).isEqualTo("Alice");
            assertThat(result.getRow(1).get(0)).isEqualTo("Bob");
        }

        @Test
        @DisplayName("Offset skips rows | 偏移跳过行")
        void offsetSkipsRows() {
            CsvDocument result = CsvQuery.from(sampleDoc())
                    .offset(3)
                    .execute();
            assertThat(result.rowCount()).isEqualTo(2);
            assertThat(result.getRow(0).get(0)).isEqualTo("Diana");
        }

        @Test
        @DisplayName("Limit and offset combined | 限制和偏移组合")
        void limitAndOffsetCombined() {
            CsvDocument result = CsvQuery.from(sampleDoc())
                    .offset(1)
                    .limit(2)
                    .execute();
            assertThat(result.rowCount()).isEqualTo(2);
            assertThat(result.getRow(0).get(0)).isEqualTo("Bob");
            assertThat(result.getRow(1).get(0)).isEqualTo("Charlie");
        }

        @Test
        @DisplayName("Limit zero returns empty | 限制为零返回空")
        void limitZeroReturnsEmpty() {
            CsvDocument result = CsvQuery.from(sampleDoc())
                    .limit(0)
                    .execute();
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Limit exceeding row count returns all | 限制超过行数返回全部")
        void limitExceedingRowCountReturnsAll() {
            CsvDocument result = CsvQuery.from(sampleDoc())
                    .limit(100)
                    .execute();
            assertThat(result.rowCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("Offset exceeding row count returns empty | 偏移超过行数返回空")
        void offsetExceedingRowCountReturnsEmpty() {
            CsvDocument result = CsvQuery.from(sampleDoc())
                    .offset(100)
                    .execute();
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Negative limit throws | 负限制抛出异常")
        void negativeLimitThrows() {
            assertThatThrownBy(() -> CsvQuery.from(sampleDoc()).limit(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Negative offset throws | 负偏移抛出异常")
        void negativeOffsetThrows() {
            assertThatThrownBy(() -> CsvQuery.from(sampleDoc()).offset(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== distinct() ====================

    @Nested
    @DisplayName("distinct() - deduplication | 去重")
    class DistinctTest {

        @Test
        @DisplayName("Removes duplicate rows (all fields) | 移除重复行（所有字段）")
        void removesAllFieldDuplicates() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("Alice", "30")
                    .addRow("Bob", "25")
                    .addRow("Alice", "30")
                    .addRow("Bob", "25")
                    .build();
            CsvDocument result = CsvQuery.from(doc).distinct().execute();
            assertThat(result.rowCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Removes duplicates by specific columns | 按特定列去重")
        void removesColumnDuplicates() {
            CsvDocument result = CsvQuery.from(sampleDoc())
                    .distinct("role")
                    .execute();
            // Engineer, Designer, Manager -> 3 unique roles
            assertThat(result.rowCount()).isEqualTo(3);
            assertThat(result.getRow(0).get(0)).isEqualTo("Alice");   // first Engineer
            assertThat(result.getRow(1).get(0)).isEqualTo("Bob");     // first Designer
            assertThat(result.getRow(2).get(0)).isEqualTo("Diana");   // first Manager
        }

        @Test
        @DisplayName("Distinct on non-existent column throws | 不存在列的去重抛出异常")
        void throwsOnNonExistentDistinctColumn() {
            assertThatThrownBy(() -> CsvQuery.from(sampleDoc())
                    .distinct("nonexistent")
                    .execute())
                    .isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("Distinct on empty document | 空文档去重")
        void distinctOnEmptyDocument() {
            CsvDocument result = CsvQuery.from(emptyDoc()).distinct().execute();
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Throws on null columns | null列抛出异常")
        void throwsOnNullColumns() {
            assertThatThrownBy(() -> CsvQuery.from(sampleDoc()).distinct((String[]) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== count() ====================

    @Nested
    @DisplayName("count() - row counting | 行计数")
    class CountTest {

        @Test
        @DisplayName("Counts all rows | 计数所有行")
        void countsAllRows() {
            long count = CsvQuery.from(sampleDoc()).count();
            assertThat(count).isEqualTo(5);
        }

        @Test
        @DisplayName("Counts filtered rows | 计数过滤后的行")
        void countsFilteredRows() {
            long count = CsvQuery.from(sampleDoc())
                    .where(row -> "Engineer".equals(row.get(2)))
                    .count();
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Count of empty document is zero | 空文档计数为零")
        void countOfEmptyIsZero() {
            assertThat(CsvQuery.from(emptyDoc()).count()).isEqualTo(0);
        }
    }

    // ==================== column() ====================

    @Nested
    @DisplayName("column() - extract column values | 提取列值")
    class ColumnTest {

        @Test
        @DisplayName("Extracts column values | 提取列值")
        void extractsColumnValues() {
            List<String> ages = CsvQuery.from(sampleDoc()).column("age");
            assertThat(ages).containsExactly("30", "25", "35", "28", "25");
        }

        @Test
        @DisplayName("Extracts filtered column values | 提取过滤后的列值")
        void extractsFilteredColumnValues() {
            List<String> names = CsvQuery.from(sampleDoc())
                    .where(row -> "Designer".equals(row.get(2)))
                    .column("name");
            assertThat(names).containsExactly("Bob", "Eve");
        }

        @Test
        @DisplayName("Throws on non-existent column | 不存在的列抛出异常")
        void throwsOnNonExistentColumn() {
            assertThatThrownBy(() -> CsvQuery.from(sampleDoc()).column("nonexistent"))
                    .isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("Throws on null column name | null列名抛出异常")
        void throwsOnNullColumnName() {
            assertThatThrownBy(() -> CsvQuery.from(sampleDoc()).column(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== groupBy() / countBy() ====================

    @Nested
    @DisplayName("groupBy() / countBy() - grouping | 分组")
    class GroupByTest {

        @Test
        @DisplayName("Groups rows by column | 按列分组行")
        void groupsRowsByColumn() {
            Map<String, CsvDocument> groups = CsvQuery.from(sampleDoc()).groupBy("role");
            assertThat(groups).hasSize(3);
            assertThat(groups.get("Engineer").rowCount()).isEqualTo(2);
            assertThat(groups.get("Designer").rowCount()).isEqualTo(2);
            assertThat(groups.get("Manager").rowCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Group documents retain headers | 分组文档保留标题")
        void groupDocumentsRetainHeaders() {
            Map<String, CsvDocument> groups = CsvQuery.from(sampleDoc()).groupBy("role");
            for (CsvDocument groupDoc : groups.values()) {
                assertThat(groupDoc.headers()).containsExactly("name", "age", "role");
            }
        }

        @Test
        @DisplayName("countBy returns correct counts | countBy返回正确计数")
        void countByReturnsCorrectCounts() {
            Map<String, Long> counts = CsvQuery.from(sampleDoc()).countBy("role");
            assertThat(counts).containsEntry("Engineer", 2L)
                    .containsEntry("Designer", 2L)
                    .containsEntry("Manager", 1L);
        }

        @Test
        @DisplayName("GroupBy on empty document | 空文档分组")
        void groupByOnEmptyDocument() {
            Map<String, CsvDocument> groups = CsvQuery.from(emptyDoc()).groupBy("name");
            assertThat(groups).isEmpty();
        }

        @Test
        @DisplayName("Throws on non-existent group column | 不存在的分组列抛出异常")
        void throwsOnNonExistentGroupColumn() {
            assertThatThrownBy(() -> CsvQuery.from(sampleDoc()).groupBy("nonexistent"))
                    .isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("Throws on null groupBy column | null分组列抛出异常")
        void throwsOnNullGroupByColumn() {
            assertThatThrownBy(() -> CsvQuery.from(sampleDoc()).groupBy(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Throws on null countBy column | null计数列抛出异常")
        void throwsOnNullCountByColumn() {
            assertThatThrownBy(() -> CsvQuery.from(sampleDoc()).countBy(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== Immutability | 不可变性 ====================

    @Nested
    @DisplayName("Immutability verification | 不可变性验证")
    class ImmutabilityTest {

        @Test
        @DisplayName("Intermediate operations do not mutate original | 中间操作不改变原始对象")
        void intermediateOpsDoNotMutate() {
            CsvDocument doc = sampleDoc();
            CsvQuery original = CsvQuery.from(doc);
            CsvQuery filtered = original.where(row -> "Engineer".equals(row.get(2)));
            CsvQuery limited = original.limit(1);

            // Original should still return all rows
            assertThat(original.execute().rowCount()).isEqualTo(5);
            // Filtered should return 2
            assertThat(filtered.execute().rowCount()).isEqualTo(2);
            // Limited should return 1
            assertThat(limited.execute().rowCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Multiple executions return same result | 多次执行返回相同结果")
        void multipleExecutionsReturnSameResult() {
            CsvQuery query = CsvQuery.from(sampleDoc())
                    .where(row -> "Designer".equals(row.get(2)))
                    .select("name");
            CsvDocument first = query.execute();
            CsvDocument second = query.execute();
            assertThat(first).isEqualTo(second);
        }
    }

    // ==================== Combined Operations | 组合操作 ====================

    @Nested
    @DisplayName("Combined operations | 组合操作")
    class CombinedTest {

        @Test
        @DisplayName("Filter + sort + limit + select | 过滤+排序+限制+选择")
        void filterSortLimitSelect() {
            CsvDocument result = CsvQuery.from(sampleDoc())
                    .where(row -> Integer.parseInt(row.get(1)) >= 28)
                    .orderBy("age", Comparator.comparingInt(Integer::parseInt))
                    .limit(2)
                    .select("name", "age")
                    .execute();
            assertThat(result.headers()).containsExactly("name", "age");
            assertThat(result.rowCount()).isEqualTo(2);
            assertThat(result.getRow(0).get(0)).isEqualTo("Diana");  // age 28
            assertThat(result.getRow(1).get(0)).isEqualTo("Alice");  // age 30
        }

        @Test
        @DisplayName("Empty document with all operations | 空文档执行所有操作")
        void emptyDocumentWithAllOperations() {
            CsvDocument result = CsvQuery.from(emptyDoc())
                    .where(_ -> true)
                    .orderBy("name", true)
                    .limit(10)
                    .offset(0)
                    .distinct()
                    .execute();
            assertThat(result.isEmpty()).isTrue();
            assertThat(result.headers()).containsExactly("name", "age");
        }

        @Test
        @DisplayName("GroupBy with filter | 带过滤的分组")
        void groupByWithFilter() {
            Map<String, Long> counts = CsvQuery.from(sampleDoc())
                    .where(row -> Integer.parseInt(row.get(1)) < 30)
                    .countBy("role");
            assertThat(counts).containsEntry("Designer", 2L)
                    .containsEntry("Manager", 1L)
                    .doesNotContainKey("Engineer");
        }
    }
}
