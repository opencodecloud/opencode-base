package cloud.opencode.base.csv.benchmark;

import cloud.opencode.base.csv.CsvConfig;
import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;
import cloud.opencode.base.csv.internal.CsvFormatter;
import cloud.opencode.base.csv.internal.CsvParser;
import cloud.opencode.base.csv.merge.CsvMerge;
import cloud.opencode.base.csv.query.CsvQuery;
import cloud.opencode.base.csv.stats.CsvColumnStats;
import cloud.opencode.base.csv.stats.CsvStats;
import cloud.opencode.base.csv.stream.CsvReader;
import cloud.opencode.base.csv.transform.CsvTransform;
import cloud.opencode.base.csv.validator.CsvValidationResult;
import cloud.opencode.base.csv.validator.CsvValidator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.StringReader;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark for opencode-base-csv
 *
 * <p>Run: mvn test-compile exec:java -pl opencode-base-csv
 * -Dexec.mainClass="cloud.opencode.base.csv.benchmark.CsvBenchmark"</p>
 *
 * <p>Or run the main() method directly.</p>
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class CsvBenchmark {

    @Param({"100", "1000", "10000"})
    private int rowCount;

    private String csvString;
    private CsvDocument document;
    private CsvDocument documentSmall;
    private CsvValidator validator;

    @Setup(Level.Trial)
    public void setup() {
        // Generate CSV data
        StringBuilder sb = new StringBuilder();
        sb.append("id,name,age,city,score\n");
        for (int i = 0; i < rowCount; i++) {
            sb.append(i).append(',')
              .append("Name").append(i % 100).append(',')
              .append(20 + (i % 50)).append(',')
              .append("City").append(i % 10).append(',')
              .append(String.format("%.2f", (i % 1000) / 10.0)).append('\n');
        }
        csvString = sb.toString();
        document = CsvParser.parse(csvString, CsvConfig.DEFAULT);

        // Small document for merge benchmarks
        CsvDocument.Builder smallBuilder = CsvDocument.builder()
                .header("id", "name", "value");
        for (int i = 0; i < Math.min(100, rowCount); i++) {
            smallBuilder.addRow(String.valueOf(i), "N" + i, String.valueOf(i * 10));
        }
        documentSmall = smallBuilder.build();

        // Validator
        validator = CsvValidator.builder()
                .notBlank("name")
                .range("age", 0, 150)
                .range("score", 0, 100)
                .build();
    }

    // ==================== Parse Benchmarks ====================

    @Benchmark
    public void parseCsvString(Blackhole bh) {
        bh.consume(CsvParser.parse(csvString, CsvConfig.DEFAULT));
    }

    @Benchmark
    public void parseCsvReader(Blackhole bh) {
        bh.consume(CsvParser.parse(new StringReader(csvString), CsvConfig.DEFAULT));
    }

    @Benchmark
    public void streamingRead(Blackhole bh) {
        try (CsvReader reader = new CsvReader(new StringReader(csvString), CsvConfig.DEFAULT)) {
            reader.stream().forEach(bh::consume);
        }
    }

    // ==================== Write Benchmarks ====================

    @Benchmark
    public void formatToString(Blackhole bh) {
        bh.consume(CsvFormatter.format(document, CsvConfig.DEFAULT));
    }

    // ==================== Query Benchmarks ====================

    @Benchmark
    public void querySelect(Blackhole bh) {
        bh.consume(CsvQuery.from(document)
                .select("name", "age")
                .execute());
    }

    @Benchmark
    public void queryWhereFilter(Blackhole bh) {
        bh.consume(CsvQuery.from(document)
                .where(row -> Integer.parseInt(row.get(2)) > 40)
                .execute());
    }

    @Benchmark
    public void queryOrderBy(Blackhole bh) {
        bh.consume(CsvQuery.from(document)
                .orderBy("age", true)
                .execute());
    }

    @Benchmark
    public void queryFilterSelectLimit(Blackhole bh) {
        bh.consume(CsvQuery.from(document)
                .where(row -> Integer.parseInt(row.get(2)) > 30)
                .select("name", "score")
                .limit(10)
                .execute());
    }

    @Benchmark
    public void queryGroupBy(Blackhole bh) {
        bh.consume(CsvQuery.from(document).groupBy("city"));
    }

    @Benchmark
    public void queryDistinct(Blackhole bh) {
        bh.consume(CsvQuery.from(document).distinct("city").execute());
    }

    // ==================== Transform Benchmarks ====================

    @Benchmark
    public void transformRenameAndMap(Blackhole bh) {
        bh.consume(CsvTransform.from(document)
                .renameColumn("name", "full_name")
                .mapColumn("full_name", String::toUpperCase)
                .execute());
    }

    @Benchmark
    public void transformAddRemoveColumns(Blackhole bh) {
        bh.consume(CsvTransform.from(document)
                .addColumn("status", "active")
                .removeColumns("city")
                .execute());
    }

    // ==================== Stats Benchmarks ====================

    @Benchmark
    public void statsSummary(Blackhole bh) {
        bh.consume(CsvStats.summary(document, "score"));
    }

    @Benchmark
    public void statsFrequency(Blackhole bh) {
        bh.consume(CsvStats.frequency(document, "city"));
    }

    // ==================== Validator Benchmarks ====================

    @Benchmark
    public void validateDocument(Blackhole bh) {
        bh.consume(validator.validate(document));
    }

    // ==================== Merge Benchmarks ====================

    @Benchmark
    public void mergeConcat(Blackhole bh) {
        bh.consume(CsvMerge.concat(documentSmall, documentSmall, documentSmall));
    }

    // ==================== Runner ====================

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CsvBenchmark.class.getSimpleName())
                .param("rowCount", "1000", "10000")
                .build();
        new Runner(opt).run();
    }
}
