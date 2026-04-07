package cloud.opencode.base.csv.sampling;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.exception.OpenCsvException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CsvSampling 测试
 */
@DisplayName("CsvSampling 测试")
class CsvSamplingTest {

    private CsvDocument sampleDoc() {
        CsvDocument.Builder builder = CsvDocument.builder()
                .header("id", "name", "category");
        for (int i = 0; i < 20; i++) {
            String cat = i % 3 == 0 ? "A" : (i % 3 == 1 ? "B" : "C");
            builder.addRow(String.valueOf(i), "Name" + i, cat);
        }
        return builder.build();
    }

    @Nested
    @DisplayName("random - 随机采样")
    class RandomSamplingTest {

        @Test
        @DisplayName("采样数小于行数")
        void sampleLessThanRowCount() {
            CsvDocument sample = CsvSampling.random(sampleDoc(), 5, 42L);
            assertThat(sample.rowCount()).isEqualTo(5);
            assertThat(sample.headers()).containsExactly("id", "name", "category");
        }

        @Test
        @DisplayName("采样数等于行数 - 返回整个文档")
        void sampleEqualToRowCount() {
            CsvDocument doc = sampleDoc();
            CsvDocument sample = CsvSampling.random(doc, 20);
            assertThat(sample).isSameAs(doc);
        }

        @Test
        @DisplayName("采样数大于行数 - 返回整个文档")
        void sampleGreaterThanRowCount() {
            CsvDocument doc = sampleDoc();
            CsvDocument sample = CsvSampling.random(doc, 100);
            assertThat(sample).isSameAs(doc);
        }

        @Test
        @DisplayName("无放回 - 无重复行")
        void noDuplicates() {
            CsvDocument sample = CsvSampling.random(sampleDoc(), 10, 42L);
            Set<String> ids = new HashSet<>();
            for (int i = 0; i < sample.rowCount(); i++) {
                ids.add(sample.getRow(i).get(0));
            }
            assertThat(ids).hasSize(10);
        }

        @Test
        @DisplayName("种子保证可重现性")
        void seededReproducible() {
            CsvDocument doc = sampleDoc();
            CsvDocument s1 = CsvSampling.random(doc, 5, 42L);
            CsvDocument s2 = CsvSampling.random(doc, 5, 42L);
            for (int i = 0; i < s1.rowCount(); i++) {
                assertThat(s1.getRow(i).get(0)).isEqualTo(s2.getRow(i).get(0));
            }
        }

        @Test
        @DisplayName("空文档 - 返回空文档")
        void emptyDoc() {
            CsvDocument empty = CsvDocument.builder().header("id").build();
            CsvDocument sample = CsvSampling.random(empty, 5);
            assertThat(sample.isEmpty()).isTrue();
            assertThat(sample.headers()).containsExactly("id");
        }

        @Test
        @DisplayName("sampleSize <= 0 时抛出异常")
        void invalidSampleSize() {
            assertThatThrownBy(() -> CsvSampling.random(sampleDoc(), 0))
                    .isInstanceOf(OpenCsvException.class);
            assertThatThrownBy(() -> CsvSampling.random(sampleDoc(), -1))
                    .isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("null文档时抛出异常")
        void nullDoc() {
            assertThatThrownBy(() -> CsvSampling.random(null, 5))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("采样1行")
        void sampleOne() {
            CsvDocument sample = CsvSampling.random(sampleDoc(), 1, 99L);
            assertThat(sample.rowCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("systematic - 系统采样")
    class SystematicSamplingTest {

        @Test
        @DisplayName("每隔5行采样")
        void everyFifthRow() {
            CsvDocument sample = CsvSampling.systematic(sampleDoc(), 5, 0);
            // rows: 0, 5, 10, 15 → 4 rows
            assertThat(sample.rowCount()).isEqualTo(4);
            assertThat(sample.getRow(0).get(0)).isEqualTo("0");
            assertThat(sample.getRow(1).get(0)).isEqualTo("5");
            assertThat(sample.getRow(2).get(0)).isEqualTo("10");
            assertThat(sample.getRow(3).get(0)).isEqualTo("15");
        }

        @Test
        @DisplayName("带偏移采样")
        void withOffset() {
            CsvDocument sample = CsvSampling.systematic(sampleDoc(), 5, 2);
            // rows: 2, 7, 12, 17 → 4 rows
            assertThat(sample.rowCount()).isEqualTo(4);
            assertThat(sample.getRow(0).get(0)).isEqualTo("2");
            assertThat(sample.getRow(1).get(0)).isEqualTo("7");
        }

        @Test
        @DisplayName("间隔为1 - 返回所有行")
        void intervalOne() {
            CsvDocument sample = CsvSampling.systematic(sampleDoc(), 1, 0);
            assertThat(sample.rowCount()).isEqualTo(20);
        }

        @Test
        @DisplayName("间隔大于行数 - 返回一行")
        void intervalLargerThanDoc() {
            CsvDocument sample = CsvSampling.systematic(sampleDoc(), 100, 0);
            assertThat(sample.rowCount()).isEqualTo(1);
            assertThat(sample.getRow(0).get(0)).isEqualTo("0");
        }

        @Test
        @DisplayName("标题保留")
        void headersPreserved() {
            CsvDocument sample = CsvSampling.systematic(sampleDoc(), 5, 0);
            assertThat(sample.headers()).containsExactly("id", "name", "category");
        }

        @Test
        @DisplayName("interval <= 0 时抛出异常")
        void invalidInterval() {
            assertThatThrownBy(() -> CsvSampling.systematic(sampleDoc(), 0))
                    .isInstanceOf(OpenCsvException.class);
            assertThatThrownBy(() -> CsvSampling.systematic(sampleDoc(), -1))
                    .isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("startOffset无效时抛出异常")
        void invalidStartOffset() {
            assertThatThrownBy(() -> CsvSampling.systematic(sampleDoc(), 5, -1))
                    .isInstanceOf(OpenCsvException.class);
            assertThatThrownBy(() -> CsvSampling.systematic(sampleDoc(), 5, 5))
                    .isInstanceOf(OpenCsvException.class);
            assertThatThrownBy(() -> CsvSampling.systematic(sampleDoc(), 5, 6))
                    .isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("空文档 - 返回空文档")
        void emptyDoc() {
            CsvDocument empty = CsvDocument.builder().header("id").build();
            CsvDocument sample = CsvSampling.systematic(empty, 5);
            assertThat(sample.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("null文档时抛出异常")
        void nullDoc() {
            assertThatThrownBy(() -> CsvSampling.systematic(null, 5))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("stratified - 分层采样")
    class StratifiedSamplingTest {

        @Test
        @DisplayName("按比例采样")
        void proportionalSampling() {
            // 20 rows: A=7, B=7, C=6
            // sampleSize=10 → A~3.5, B~3.5, C~3
            CsvDocument sample = CsvSampling.stratified(sampleDoc(), "category", 10, 42L);
            assertThat(sample.rowCount()).isGreaterThanOrEqualTo(8);
            assertThat(sample.rowCount()).isLessThanOrEqualTo(12);
        }

        @Test
        @DisplayName("每组至少1行（如果可能）")
        void atLeastOnePerGroup() {
            // Small sample from 3 groups → each should get at least 1
            CsvDocument sample = CsvSampling.stratified(sampleDoc(), "category", 3, 42L);
            // Count distinct categories in sample
            Set<String> cats = new HashSet<>();
            for (int i = 0; i < sample.rowCount(); i++) {
                cats.add(sample.getRow(i).get(2));
            }
            assertThat(cats).hasSize(3);
        }

        @Test
        @DisplayName("sampleSize大于行数时不超过总行数")
        void sampleSizeLargerThanTotal() {
            CsvDocument sample = CsvSampling.stratified(sampleDoc(), "category", 100, 42L);
            assertThat(sample.rowCount()).isLessThanOrEqualTo(20);
        }

        @Test
        @DisplayName("种子保证可重现性")
        void seededReproducible() {
            CsvDocument doc = sampleDoc();
            CsvDocument s1 = CsvSampling.stratified(doc, "category", 10, 42L);
            CsvDocument s2 = CsvSampling.stratified(doc, "category", 10, 42L);
            assertThat(s1.rowCount()).isEqualTo(s2.rowCount());
            for (int i = 0; i < s1.rowCount(); i++) {
                assertThat(s1.getRow(i).get(0)).isEqualTo(s2.getRow(i).get(0));
            }
        }

        @Test
        @DisplayName("标题保留")
        void headersPreserved() {
            CsvDocument sample = CsvSampling.stratified(sampleDoc(), "category", 5, 42L);
            assertThat(sample.headers()).containsExactly("id", "name", "category");
        }

        @Test
        @DisplayName("列不存在时抛出异常")
        void missingColumn() {
            assertThatThrownBy(() -> CsvSampling.stratified(sampleDoc(), "nonexistent", 5))
                    .isInstanceOf(OpenCsvException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("sampleSize <= 0 时抛出异常")
        void invalidSampleSize() {
            assertThatThrownBy(() -> CsvSampling.stratified(sampleDoc(), "category", 0))
                    .isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("空文档 - 返回空文档")
        void emptyDoc() {
            CsvDocument empty = CsvDocument.builder().header("category").build();
            CsvDocument sample = CsvSampling.stratified(empty, "category", 5);
            assertThat(sample.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("null参数时抛出异常")
        void nullParams() {
            assertThatThrownBy(() -> CsvSampling.stratified(null, "cat", 5))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CsvSampling.stratified(sampleDoc(), null, 5))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("单组分层采样")
        void singleGroup() {
            CsvDocument doc = CsvDocument.builder()
                    .header("id", "cat")
                    .addRow("1", "X")
                    .addRow("2", "X")
                    .addRow("3", "X")
                    .build();
            CsvDocument sample = CsvSampling.stratified(doc, "cat", 2, 42L);
            assertThat(sample.rowCount()).isEqualTo(2);
        }
    }
}
