package cloud.opencode.base.csv.stats;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.exception.OpenCsvException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CsvStats tests
 * CsvStats 测试
 */
@DisplayName("CsvStats - CSV statistical operations | CSV统计操作")
class CsvStatsTest {

    // ==================== Helper Methods | 辅助方法 ====================

    private CsvDocument numericDoc() {
        return CsvDocument.builder()
                .header("name", "score", "grade")
                .addRow("Alice", "95", "A")
                .addRow("Bob", "87", "B")
                .addRow("Charlie", "92", "A")
                .addRow("Diana", "78", "C")
                .build();
    }

    private CsvDocument mixedDoc() {
        return CsvDocument.builder()
                .header("name", "value")
                .addRow("Alice", "10")
                .addRow("Bob", "abc")
                .addRow("Charlie", "")
                .addRow("Diana", "20.5")
                .build();
    }

    private CsvDocument emptyDoc() {
        return CsvDocument.builder()
                .header("name", "score")
                .build();
    }

    // ==================== Count | 计数 ====================

    @Nested
    @DisplayName("count | 计数")
    class CountTest {

        @Test
        @DisplayName("Count non-blank values | 计算非空白值数量")
        void countNonBlank() {
            assertThat(CsvStats.count(numericDoc(), "name")).isEqualTo(4);
        }

        @Test
        @DisplayName("Count with blank values | 包含空白值的计数")
        void countWithBlanks() {
            assertThat(CsvStats.count(mixedDoc(), "value")).isEqualTo(3);
        }

        @Test
        @DisplayName("Count on empty document | 空文档的计数")
        void countEmpty() {
            assertThat(CsvStats.count(emptyDoc(), "name")).isEqualTo(0);
        }

        @Test
        @DisplayName("Null doc throws NPE | null文档抛出NPE")
        void nullDoc() {
            assertThatThrownBy(() -> CsvStats.count(null, "name"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Null column throws NPE | null列名抛出NPE")
        void nullColumn() {
            assertThatThrownBy(() -> CsvStats.count(numericDoc(), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Missing column throws OpenCsvException | 缺少列抛出OpenCsvException")
        void missingColumn() {
            assertThatThrownBy(() -> CsvStats.count(numericDoc(), "nonexistent"))
                    .isInstanceOf(OpenCsvException.class);
        }
    }

    // ==================== CountAll | 总行数 ====================

    @Nested
    @DisplayName("countAll | 总行数")
    class CountAllTest {

        @Test
        @DisplayName("Count all rows | 计算所有行数")
        void countAllRows() {
            assertThat(CsvStats.countAll(numericDoc())).isEqualTo(4);
        }

        @Test
        @DisplayName("Count all on empty | 空文档的总行数")
        void countAllEmpty() {
            assertThat(CsvStats.countAll(emptyDoc())).isEqualTo(0);
        }

        @Test
        @DisplayName("Null doc throws NPE | null文档抛出NPE")
        void nullDoc() {
            assertThatThrownBy(() -> CsvStats.countAll(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== Sum | 求和 ====================

    @Nested
    @DisplayName("sum | 求和")
    class SumTest {

        @Test
        @DisplayName("Sum of numeric column | 数值列求和")
        void sumNumeric() {
            BigDecimal result = CsvStats.sum(numericDoc(), "score");
            assertThat(result).isEqualByComparingTo("352");
        }

        @Test
        @DisplayName("Sum with mixed values skips non-numeric | 混合值求和跳过非数值")
        void sumMixed() {
            BigDecimal result = CsvStats.sum(mixedDoc(), "value");
            assertThat(result).isEqualByComparingTo("30.5");
        }

        @Test
        @DisplayName("Sum of non-numeric column returns ZERO | 非数值列求和返回ZERO")
        void sumNonNumeric() {
            BigDecimal result = CsvStats.sum(numericDoc(), "name");
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Sum of empty document returns ZERO | 空文档求和返回ZERO")
        void sumEmpty() {
            assertThat(CsvStats.sum(emptyDoc(), "score")).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Missing column throws | 缺少列抛出异常")
        void missingColumn() {
            assertThatThrownBy(() -> CsvStats.sum(numericDoc(), "missing"))
                    .isInstanceOf(OpenCsvException.class);
        }
    }

    // ==================== Avg | 平均值 ====================

    @Nested
    @DisplayName("avg | 平均值")
    class AvgTest {

        @Test
        @DisplayName("Average of numeric column | 数值列平均值")
        void avgNumeric() {
            BigDecimal result = CsvStats.avg(numericDoc(), "score");
            // (95 + 87 + 92 + 78) / 4 = 88.0
            assertThat(result).isEqualByComparingTo("88.000000");
        }

        @Test
        @DisplayName("Average with mixed values | 混合值的平均值")
        void avgMixed() {
            BigDecimal result = CsvStats.avg(mixedDoc(), "value");
            // (10 + 20.5) / 2 = 15.25
            assertThat(result).isEqualByComparingTo("15.250000");
        }

        @Test
        @DisplayName("Average of non-numeric column returns null | 非数值列平均值返回null")
        void avgNonNumeric() {
            assertThat(CsvStats.avg(numericDoc(), "name")).isNull();
        }

        @Test
        @DisplayName("Average of empty document returns null | 空文档平均值返回null")
        void avgEmpty() {
            assertThat(CsvStats.avg(emptyDoc(), "score")).isNull();
        }
    }

    // ==================== Min | 最小值 ====================

    @Nested
    @DisplayName("min | 最小值")
    class MinTest {

        @Test
        @DisplayName("Min of numeric column | 数值列最小值")
        void minNumeric() {
            assertThat(CsvStats.min(numericDoc(), "score")).isEqualByComparingTo("78");
        }

        @Test
        @DisplayName("Min with mixed values | 混合值最小值")
        void minMixed() {
            assertThat(CsvStats.min(mixedDoc(), "value")).isEqualByComparingTo("10");
        }

        @Test
        @DisplayName("Min of non-numeric column returns null | 非数值列最小值返回null")
        void minNonNumeric() {
            assertThat(CsvStats.min(numericDoc(), "name")).isNull();
        }

        @Test
        @DisplayName("Min of empty document returns null | 空文档最小值返回null")
        void minEmpty() {
            assertThat(CsvStats.min(emptyDoc(), "score")).isNull();
        }
    }

    // ==================== Max | 最大值 ====================

    @Nested
    @DisplayName("max | 最大值")
    class MaxTest {

        @Test
        @DisplayName("Max of numeric column | 数值列最大值")
        void maxNumeric() {
            assertThat(CsvStats.max(numericDoc(), "score")).isEqualByComparingTo("95");
        }

        @Test
        @DisplayName("Max with mixed values | 混合值最大值")
        void maxMixed() {
            assertThat(CsvStats.max(mixedDoc(), "value")).isEqualByComparingTo("20.5");
        }

        @Test
        @DisplayName("Max of non-numeric column returns null | 非数值列最大值返回null")
        void maxNonNumeric() {
            assertThat(CsvStats.max(numericDoc(), "name")).isNull();
        }

        @Test
        @DisplayName("Max of empty document returns null | 空文档最大值返回null")
        void maxEmpty() {
            assertThat(CsvStats.max(emptyDoc(), "score")).isNull();
        }
    }

    // ==================== Distinct | 去重 ====================

    @Nested
    @DisplayName("distinct | 去重")
    class DistinctTest {

        @Test
        @DisplayName("Distinct values preserving order | 去重值保留顺序")
        void distinctPreservesOrder() {
            List<String> result = CsvStats.distinct(numericDoc(), "grade");
            assertThat(result).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("Distinct with duplicates | 有重复值的去重")
        void distinctWithDuplicates() {
            CsvDocument doc = CsvDocument.builder()
                    .header("color")
                    .addRow("red")
                    .addRow("blue")
                    .addRow("red")
                    .addRow("green")
                    .addRow("blue")
                    .build();
            List<String> result = CsvStats.distinct(doc, "color");
            assertThat(result).containsExactly("red", "blue", "green");
        }

        @Test
        @DisplayName("Distinct on empty document | 空文档去重")
        void distinctEmpty() {
            assertThat(CsvStats.distinct(emptyDoc(), "name")).isEmpty();
        }

        @Test
        @DisplayName("Distinct includes blank strings | 去重包含空字符串")
        void distinctIncludesBlanks() {
            List<String> result = CsvStats.distinct(mixedDoc(), "value");
            assertThat(result).contains("");
        }

        @Test
        @DisplayName("Missing column throws | 缺少列抛出异常")
        void missingColumn() {
            assertThatThrownBy(() -> CsvStats.distinct(numericDoc(), "missing"))
                    .isInstanceOf(OpenCsvException.class);
        }
    }

    // ==================== Frequency | 频率 ====================

    @Nested
    @DisplayName("frequency | 频率")
    class FrequencyTest {

        @Test
        @DisplayName("Frequency ordered by count desc | 频率按计数降序")
        void frequencyOrdered() {
            Map<String, Long> result = CsvStats.frequency(numericDoc(), "grade");
            // A appears 2 times, B and C once each
            assertThat(result.get("A")).isEqualTo(2);
            assertThat(result.get("B")).isEqualTo(1);
            assertThat(result.get("C")).isEqualTo(1);
            // A should come first (highest count)
            assertThat(result.keySet().iterator().next()).isEqualTo("A");
        }

        @Test
        @DisplayName("Frequency on empty document | 空文档频率")
        void frequencyEmpty() {
            assertThat(CsvStats.frequency(emptyDoc(), "name")).isEmpty();
        }

        @Test
        @DisplayName("Frequency includes blank strings | 频率包含空字符串")
        void frequencyIncludesBlanks() {
            Map<String, Long> result = CsvStats.frequency(mixedDoc(), "value");
            assertThat(result).containsKey("");
        }

        @Test
        @DisplayName("Missing column throws | 缺少列抛出异常")
        void missingColumn() {
            assertThatThrownBy(() -> CsvStats.frequency(numericDoc(), "missing"))
                    .isInstanceOf(OpenCsvException.class);
        }
    }

    // ==================== Summary | 摘要 ====================

    @Nested
    @DisplayName("summary | 摘要")
    class SummaryTest {

        @Test
        @DisplayName("Summary of numeric column | 数值列摘要")
        void summaryNumeric() {
            CsvColumnStats stats = CsvStats.summary(numericDoc(), "score");
            assertThat(stats.column()).isEqualTo("score");
            assertThat(stats.totalCount()).isEqualTo(4);
            assertThat(stats.nonBlankCount()).isEqualTo(4);
            assertThat(stats.distinctCount()).isEqualTo(4);
            assertThat(stats.sum()).isEqualByComparingTo("352");
            assertThat(stats.avg()).isEqualByComparingTo("88.000000");
            assertThat(stats.min()).isEqualByComparingTo("78");
            assertThat(stats.max()).isEqualByComparingTo("95");
        }

        @Test
        @DisplayName("Summary of non-numeric column | 非数值列摘要")
        void summaryNonNumeric() {
            CsvColumnStats stats = CsvStats.summary(numericDoc(), "name");
            assertThat(stats.totalCount()).isEqualTo(4);
            assertThat(stats.nonBlankCount()).isEqualTo(4);
            assertThat(stats.sum()).isNull();
            assertThat(stats.avg()).isNull();
            assertThat(stats.min()).isNull();
            assertThat(stats.max()).isNull();
        }

        @Test
        @DisplayName("Summary of mixed column | 混合列摘要")
        void summaryMixed() {
            CsvColumnStats stats = CsvStats.summary(mixedDoc(), "value");
            assertThat(stats.totalCount()).isEqualTo(4);
            assertThat(stats.nonBlankCount()).isEqualTo(3); // "abc", "10", "20.5" (blank skipped)
            assertThat(stats.sum()).isEqualByComparingTo("30.5");
            assertThat(stats.min()).isEqualByComparingTo("10");
            assertThat(stats.max()).isEqualByComparingTo("20.5");
        }

        @Test
        @DisplayName("Summary of empty document | 空文档摘要")
        void summaryEmpty() {
            CsvColumnStats stats = CsvStats.summary(emptyDoc(), "score");
            assertThat(stats.totalCount()).isEqualTo(0);
            assertThat(stats.nonBlankCount()).isEqualTo(0);
            assertThat(stats.distinctCount()).isEqualTo(0);
            assertThat(stats.sum()).isNull();
            assertThat(stats.avg()).isNull();
            assertThat(stats.min()).isNull();
            assertThat(stats.max()).isNull();
        }

        @Test
        @DisplayName("Null params throw NPE | null参数抛出NPE")
        void nullParams() {
            assertThatThrownBy(() -> CsvStats.summary(null, "score"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CsvStats.summary(numericDoc(), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Missing column throws | 缺少列抛出异常")
        void missingColumn() {
            assertThatThrownBy(() -> CsvStats.summary(numericDoc(), "missing"))
                    .isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("Summary with all blank column | 全空白列摘要")
        void summaryAllBlank() {
            CsvDocument doc = CsvDocument.builder()
                    .header("col")
                    .addRow("")
                    .addRow("")
                    .addRow("")
                    .build();
            CsvColumnStats stats = CsvStats.summary(doc, "col");
            assertThat(stats.totalCount()).isEqualTo(3);
            assertThat(stats.nonBlankCount()).isEqualTo(0);
            assertThat(stats.sum()).isNull();
            assertThat(stats.avg()).isNull();
        }

        @Test
        @DisplayName("Summary with negative and decimal numbers | 负数和小数的摘要")
        void summaryNegativeAndDecimal() {
            CsvDocument doc = CsvDocument.builder()
                    .header("val")
                    .addRow("-5.5")
                    .addRow("10.3")
                    .addRow("0")
                    .build();
            CsvColumnStats stats = CsvStats.summary(doc, "val");
            assertThat(stats.sum()).isEqualByComparingTo("4.8");
            assertThat(stats.min()).isEqualByComparingTo("-5.5");
            assertThat(stats.max()).isEqualByComparingTo("10.3");
        }
    }
}
