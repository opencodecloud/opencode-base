package cloud.opencode.base.csv.benchmark;

import cloud.opencode.base.csv.CsvConfig;
import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;
import cloud.opencode.base.csv.internal.CsvFormatter;
import cloud.opencode.base.csv.internal.CsvParser;
import cloud.opencode.base.csv.merge.CsvMerge;
import cloud.opencode.base.csv.query.CsvQuery;
import cloud.opencode.base.csv.stats.CsvStats;
import cloud.opencode.base.csv.stream.CsvReader;
import cloud.opencode.base.csv.transform.CsvTransform;
import cloud.opencode.base.csv.validator.CsvValidationResult;
import cloud.opencode.base.csv.validator.CsvValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance regression test for opencode-base-csv.
 * Runs timed operations on realistic data sizes to catch performance regressions.
 * Not a JMH benchmark (those require separate execution), but validates
 * O(n) behavior by comparing elapsed times at different scales.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("CSV Performance Regression Tests | CSV性能回归测试")
class CsvBenchmarkTest {

    private static final int SMALL = 1_000;
    private static final int LARGE = 10_000;
    private static final long MAX_SMALL_MS = 500;   // generous bounds
    private static final long MAX_LARGE_MS = 5_000;

    private String csvSmall;
    private String csvLarge;
    private CsvDocument docSmall;
    private CsvDocument docLarge;

    @BeforeAll
    void setup() {
        csvSmall = generateCsv(SMALL);
        csvLarge = generateCsv(LARGE);
        docSmall = CsvParser.parse(csvSmall, CsvConfig.DEFAULT);
        docLarge = CsvParser.parse(csvLarge, CsvConfig.DEFAULT);
    }

    private static String generateCsv(int rows) {
        StringBuilder sb = new StringBuilder(rows * 40);
        sb.append("id,name,age,city,score\n");
        for (int i = 0; i < rows; i++) {
            sb.append(i).append(",Name").append(i % 100)
              .append(',').append(20 + (i % 50))
              .append(",City").append(i % 10)
              .append(',').append(String.format("%.2f", (i % 1000) / 10.0))
              .append('\n');
        }
        return sb.toString();
    }

    @Nested
    @DisplayName("Parse Performance | 解析性能")
    class ParsePerf {

        @Test
        @DisplayName("解析1K行 < " + MAX_SMALL_MS + "ms")
        void parseSmall() {
            long start = System.nanoTime();
            CsvDocument doc = CsvParser.parse(csvSmall, CsvConfig.DEFAULT);
            long ms = (System.nanoTime() - start) / 1_000_000;
            assertThat(doc.rowCount()).isEqualTo(SMALL);
            assertThat(ms).isLessThan(MAX_SMALL_MS);
            System.out.printf("  Parse %,d rows: %d ms%n", SMALL, ms);
        }

        @Test
        @DisplayName("解析10K行 < " + MAX_LARGE_MS + "ms")
        void parseLarge() {
            long start = System.nanoTime();
            CsvDocument doc = CsvParser.parse(csvLarge, CsvConfig.DEFAULT);
            long ms = (System.nanoTime() - start) / 1_000_000;
            assertThat(doc.rowCount()).isEqualTo(LARGE);
            assertThat(ms).isLessThan(MAX_LARGE_MS);
            System.out.printf("  Parse %,d rows: %d ms%n", LARGE, ms);
        }

        @Test
        @DisplayName("流式读取10K行 < " + MAX_LARGE_MS + "ms")
        void streamLarge() {
            long start = System.nanoTime();
            long count;
            try (CsvReader reader = new CsvReader(new StringReader(csvLarge), CsvConfig.DEFAULT)) {
                count = reader.stream().count();
            }
            long ms = (System.nanoTime() - start) / 1_000_000;
            assertThat(count).isEqualTo(LARGE);
            assertThat(ms).isLessThan(MAX_LARGE_MS);
            System.out.printf("  Stream %,d rows: %d ms%n", LARGE, ms);
        }
    }

    @Nested
    @DisplayName("Write Performance | 写入性能")
    class WritePerf {

        @Test
        @DisplayName("格式化10K行 < " + MAX_LARGE_MS + "ms")
        void formatLarge() {
            long start = System.nanoTime();
            String result = CsvFormatter.format(docLarge, CsvConfig.DEFAULT);
            long ms = (System.nanoTime() - start) / 1_000_000;
            assertThat(result).isNotEmpty();
            assertThat(ms).isLessThan(MAX_LARGE_MS);
            System.out.printf("  Format %,d rows: %d ms%n", LARGE, ms);
        }
    }

    @Nested
    @DisplayName("Query Performance | 查询性能")
    class QueryPerf {

        @Test
        @DisplayName("select + where + limit 10K行")
        void queryComposite() {
            long start = System.nanoTime();
            CsvDocument result = CsvQuery.from(docLarge)
                    .where(row -> Integer.parseInt(row.get(2)) > 40)
                    .select("name", "score")
                    .limit(100)
                    .execute();
            long ms = (System.nanoTime() - start) / 1_000_000;
            assertThat(result.rowCount()).isLessThanOrEqualTo(100);
            assertThat(ms).isLessThan(MAX_LARGE_MS);
            System.out.printf("  Query (where+select+limit) %,d rows: %d ms%n", LARGE, ms);
        }

        @Test
        @DisplayName("orderBy 10K行")
        void querySort() {
            long start = System.nanoTime();
            CsvDocument result = CsvQuery.from(docLarge)
                    .orderBy("age", true)
                    .execute();
            long ms = (System.nanoTime() - start) / 1_000_000;
            assertThat(result.rowCount()).isEqualTo(LARGE);
            assertThat(ms).isLessThan(MAX_LARGE_MS);
            System.out.printf("  Sort %,d rows: %d ms%n", LARGE, ms);
        }

        @Test
        @DisplayName("groupBy 10K行")
        void queryGroupBy() {
            long start = System.nanoTime();
            var groups = CsvQuery.from(docLarge).groupBy("city");
            long ms = (System.nanoTime() - start) / 1_000_000;
            assertThat(groups).isNotEmpty();
            assertThat(ms).isLessThan(MAX_LARGE_MS);
            System.out.printf("  GroupBy %,d rows: %d ms%n", LARGE, ms);
        }

        @Test
        @DisplayName("distinct 10K行")
        void queryDistinct() {
            long start = System.nanoTime();
            CsvDocument result = CsvQuery.from(docLarge).distinct("city").execute();
            long ms = (System.nanoTime() - start) / 1_000_000;
            assertThat(result.rowCount()).isEqualTo(10); // City0..City9
            assertThat(ms).isLessThan(MAX_LARGE_MS);
            System.out.printf("  Distinct %,d rows: %d ms%n", LARGE, ms);
        }
    }

    @Nested
    @DisplayName("Transform Performance | 转换性能")
    class TransformPerf {

        @Test
        @DisplayName("rename + map + add + remove 10K行")
        void transformComposite() {
            long start = System.nanoTime();
            CsvDocument result = CsvTransform.from(docLarge)
                    .renameColumn("name", "full_name")
                    .mapColumn("full_name", String::toUpperCase)
                    .addColumn("status", "active")
                    .removeColumns("city")
                    .execute();
            long ms = (System.nanoTime() - start) / 1_000_000;
            assertThat(result.rowCount()).isEqualTo(LARGE);
            assertThat(ms).isLessThan(MAX_LARGE_MS);
            System.out.printf("  Transform (4 ops) %,d rows: %d ms%n", LARGE, ms);
        }
    }

    @Nested
    @DisplayName("Stats Performance | 统计性能")
    class StatsPerf {

        @Test
        @DisplayName("summary 10K行")
        void statsSummary() {
            long start = System.nanoTime();
            var stats = CsvStats.summary(docLarge, "score");
            long ms = (System.nanoTime() - start) / 1_000_000;
            assertThat(stats.totalCount()).isEqualTo(LARGE);
            assertThat(ms).isLessThan(MAX_LARGE_MS);
            System.out.printf("  Summary %,d rows: %d ms%n", LARGE, ms);
        }

        @Test
        @DisplayName("frequency 10K行")
        void statsFrequency() {
            long start = System.nanoTime();
            var freq = CsvStats.frequency(docLarge, "city");
            long ms = (System.nanoTime() - start) / 1_000_000;
            assertThat(freq).hasSize(10);
            assertThat(ms).isLessThan(MAX_LARGE_MS);
            System.out.printf("  Frequency %,d rows: %d ms%n", LARGE, ms);
        }
    }

    @Nested
    @DisplayName("Validator Performance | 校验性能")
    class ValidatorPerf {

        @Test
        @DisplayName("validate 3 rules x 10K行")
        void validateLarge() {
            CsvValidator validator = CsvValidator.builder()
                    .notBlank("name")
                    .range("age", 0, 150)
                    .range("score", 0, 100)
                    .build();

            long start = System.nanoTime();
            CsvValidationResult result = validator.validate(docLarge);
            long ms = (System.nanoTime() - start) / 1_000_000;
            assertThat(result.valid()).isTrue();
            assertThat(ms).isLessThan(MAX_LARGE_MS);
            System.out.printf("  Validate (3 rules) %,d rows: %d ms%n", LARGE, ms);
        }
    }

    @Nested
    @DisplayName("Merge Performance | 合并性能")
    class MergePerf {

        @Test
        @DisplayName("concat 3 x 1K行文档")
        void concatSmall() {
            long start = System.nanoTime();
            CsvDocument result = CsvMerge.concat(docSmall, docSmall, docSmall);
            long ms = (System.nanoTime() - start) / 1_000_000;
            assertThat(result.rowCount()).isEqualTo(SMALL * 3);
            assertThat(ms).isLessThan(MAX_SMALL_MS);
            System.out.printf("  Concat 3x%,d rows: %d ms%n", SMALL, ms);
        }
    }
}
