package cloud.opencode.base.yml.benchmark;

import cloud.opencode.base.yml.OpenYml;
import cloud.opencode.base.yml.YmlDocument;
import cloud.opencode.base.yml.bind.YmlBinder;
import cloud.opencode.base.yml.diff.DiffEntry;
import cloud.opencode.base.yml.diff.YmlDiff;
import cloud.opencode.base.yml.merge.MergeStrategy;
import cloud.opencode.base.yml.merge.YmlMerger;
import cloud.opencode.base.yml.placeholder.PlaceholderResolver;
import cloud.opencode.base.yml.schema.ValidationResult;
import cloud.opencode.base.yml.schema.YmlSchema;
import cloud.opencode.base.yml.transform.YmlFlattener;
import cloud.opencode.base.yml.transform.YmlJson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance Benchmarks for opencode-base-yml
 * opencode-base-yml 性能基准测试
 *
 * <p>Measures throughput and latency of core YAML operations using warmup + measurement
 * iterations with nanoTime-based timing. Covers hot paths: parse, dump, bind, merge,
 * diff, flatten, json-convert, placeholder resolve, and schema validate.</p>
 * <p>使用预热+测量迭代和 nanoTime 计时测量核心 YAML 操作的吞吐量和延迟。
 * 覆盖热路径：解析、转储、绑定、合并、差异、扁平化、JSON 转换、占位符解析、模式校验。</p>
 *
 * <p><strong>Run:</strong> {@code mvn test -pl opencode-base-yml -Dgroups=benchmark}</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
@DisplayName("opencode-base-yml 性能基准测试")
@Tag("benchmark")
class YmlBenchmark {

    // ==================== Fixtures ====================

    /** Small YAML: ~80 chars, 4 keys. */
    private static final String SMALL_YAML =
            "server:\n  port: 8080\n  host: localhost\n  debug: false\n";

    /** Medium YAML: ~400 chars, nested structure. */
    private static final String MEDIUM_YAML = """
            server:
              port: 8080
              host: localhost
              ssl: true
            database:
              url: jdbc:mysql://localhost:3306/mydb
              username: admin
              password: secret
              pool:
                min: 5
                max: 20
                timeout: 30000
            logging:
              level: INFO
              file: /var/log/app.log
              console: true
            cache:
              enabled: true
              ttl: 3600
              maxSize: 1000
            features:
              - auth
              - monitoring
              - rate-limiting
              - caching
            """;

    /** Large YAML: ~4KB, array of 50 items. */
    private static final String LARGE_YAML = buildLargeYaml();

    private static String buildLargeYaml() {
        StringBuilder sb = new StringBuilder("users:\n");
        for (int i = 0; i < 50; i++) {
            sb.append("  - id: ").append(i).append("\n");
            sb.append("    name: User").append(i).append("\n");
            sb.append("    email: user").append(i).append("@example.com\n");
            sb.append("    score: ").append(i * 1.5).append("\n");
            sb.append("    active: ").append(i % 2 == 0).append("\n");
            sb.append("    tags:\n");
            sb.append("      - tag").append(i % 5).append("\n");
            sb.append("      - tag").append(i % 3).append("\n");
        }
        return sb.toString();
    }

    /** Pre-parsed data for non-parse benchmarks. */
    private static final Map<String, Object> SMALL_MAP = OpenYml.load(SMALL_YAML);
    private static final Map<String, Object> MEDIUM_MAP = OpenYml.load(MEDIUM_YAML);
    private static final Map<String, Object> LARGE_MAP = OpenYml.load(LARGE_YAML);
    private static final YmlDocument MEDIUM_DOC = OpenYml.parse(MEDIUM_YAML);

    /** Overlay for merge benchmarks. */
    private static final Map<String, Object> OVERLAY_MAP = OpenYml.load(
            "server:\n  port: 9090\n  debug: true\nnewKey: newValue\n");

    /** Flat map for unflatten benchmarks. */
    private static final Map<String, Object> FLAT_MAP = YmlFlattener.flatten(MEDIUM_MAP);

    /** Modified map for diff benchmarks. */
    private static final Map<String, Object> MODIFIED_MAP;
    static {
        var m = new java.util.LinkedHashMap<>(MEDIUM_MAP);
        var server = new java.util.LinkedHashMap<>((Map<String, Object>) m.get("server"));
        server.put("port", 9090);
        server.put("newField", "added");
        m.put("server", server);
        m.remove("cache");
        MODIFIED_MAP = m;
    }

    /** Placeholder YAML. */
    private static final String PLACEHOLDER_YAML =
            "server:\n  port: ${APP_PORT:8080}\n  host: ${APP_HOST:localhost}\n  env: ${APP_ENV:dev}\n";

    private static final PlaceholderResolver RESOLVER = PlaceholderResolver.builder()
            .addPropertySource("test", Map.of("APP_PORT", "9090", "APP_HOST", "prod.example.com"))
            .build();

    /** Schema for validation benchmark. */
    private static final YmlSchema SCHEMA = YmlSchema.builder()
            .required("server", "database")
            .type("server.port", Integer.class)
            .range("server.port", 1, 65535)
            .pattern("database.url", "^jdbc:.+")
            .build();

    /** Record for bind benchmark. */
    record ServerConfig(int port, String host, boolean ssl) {}

    // ==================== Benchmark Infrastructure ====================

    private record BenchResult(String name, long ops, long totalNanos) {
        double opsPerMs() { return ops * 1_000_000.0 / totalNanos; }
        double nsPerOp()  { return (double) totalNanos / ops; }

        @Override
        public String toString() {
            return String.format("  %-52s %,8d ops  %,9.0f ops/ms  %,8.1f ns/op",
                    name, ops, opsPerMs(), nsPerOp());
        }
    }

    @FunctionalInterface
    interface BenchAction { void run(); }

    private BenchResult bench(String name, int warmup, int measure, BenchAction action) {
        for (int i = 0; i < warmup; i++) action.run();
        long start = System.nanoTime();
        for (int i = 0; i < measure; i++) action.run();
        return new BenchResult(name, measure, System.nanoTime() - start);
    }

    private static final int WARMUP  = 20_000;
    private static final int MEASURE = 100_000;

    // ==================== Benchmarks ====================

    @Nested
    @DisplayName("解析基准 (Parse)")
    class ParseBenchmarks {

        @Test
        @DisplayName("YAML 解析吞吐量: 小/中/大")
        void parseThroughput() {
            System.out.println("\n══════════ Parse Benchmarks ══════════");
            BenchResult r1 = bench("parse  small (~80 chars)",    WARMUP, MEASURE,     () -> OpenYml.load(SMALL_YAML));
            BenchResult r2 = bench("parse  medium (~400 chars)",  WARMUP, MEASURE,     () -> OpenYml.load(MEDIUM_YAML));
            BenchResult r3 = bench("parse  large  (~4KB, 50 obj)",WARMUP, MEASURE / 5, () -> OpenYml.load(LARGE_YAML));
            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);
            System.out.println();
            assertThat(r1.opsPerMs()).isGreaterThan(1);
            assertThat(r2.opsPerMs()).isGreaterThan(1);
            assertThat(r3.opsPerMs()).isGreaterThan(0.1);
        }
    }

    @Nested
    @DisplayName("转储基准 (Dump)")
    class DumpBenchmarks {

        @Test
        @DisplayName("YAML 转储吞吐量: 小/中/大")
        void dumpThroughput() {
            System.out.println("\n══════════ Dump Benchmarks ══════════");
            BenchResult r1 = bench("dump   small",  WARMUP, MEASURE,     () -> OpenYml.dump(SMALL_MAP));
            BenchResult r2 = bench("dump   medium", WARMUP, MEASURE,     () -> OpenYml.dump(MEDIUM_MAP));
            BenchResult r3 = bench("dump   large",  WARMUP, MEASURE / 5, () -> OpenYml.dump(LARGE_MAP));
            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);
            System.out.println();
            assertThat(r1.opsPerMs()).isGreaterThan(1);
        }
    }

    @Nested
    @DisplayName("绑定基准 (Bind)")
    class BindBenchmarks {

        @Test
        @DisplayName("对象绑定吞吐量")
        void bindThroughput() {
            System.out.println("\n══════════ Bind Benchmarks ══════════");
            BenchResult r1 = bench("bind   record (ServerConfig)", WARMUP, MEASURE,
                    () -> YmlBinder.bind(MEDIUM_DOC, "server", ServerConfig.class));
            System.out.println(r1);
            System.out.println();
            assertThat(r1.opsPerMs()).isGreaterThan(1);
        }
    }

    @Nested
    @DisplayName("合并基准 (Merge)")
    class MergeBenchmarks {

        @Test
        @DisplayName("文档合并吞吐量")
        void mergeThroughput() {
            System.out.println("\n══════════ Merge Benchmarks ══════════");
            BenchResult r1 = bench("merge  DEEP_MERGE",  WARMUP, MEASURE,
                    () -> YmlMerger.merge(MEDIUM_MAP, OVERLAY_MAP, MergeStrategy.DEEP_MERGE));
            BenchResult r2 = bench("merge  OVERRIDE",    WARMUP, MEASURE,
                    () -> YmlMerger.merge(MEDIUM_MAP, OVERLAY_MAP, MergeStrategy.OVERRIDE));
            System.out.println(r1);
            System.out.println(r2);
            System.out.println();
            assertThat(r1.opsPerMs()).isGreaterThan(1);
        }
    }

    @Nested
    @DisplayName("差异基准 (Diff)")
    class DiffBenchmarks {

        @Test
        @DisplayName("文档差异比较吞吐量")
        void diffThroughput() {
            System.out.println("\n══════════ Diff Benchmarks ══════════");
            BenchResult r1 = bench("diff   medium (with changes)", WARMUP, MEASURE,
                    () -> YmlDiff.diff(MEDIUM_MAP, MODIFIED_MAP));
            BenchResult r2 = bench("diff   medium (identical)",    WARMUP, MEASURE,
                    () -> YmlDiff.diff(MEDIUM_MAP, MEDIUM_MAP));
            System.out.println(r1);
            System.out.println(r2);
            System.out.println();
            assertThat(r1.opsPerMs()).isGreaterThan(1);
        }
    }

    @Nested
    @DisplayName("扁平化基准 (Flatten)")
    class FlattenBenchmarks {

        @Test
        @DisplayName("扁平化/反扁平化吞吐量")
        void flattenThroughput() {
            System.out.println("\n══════════ Flatten Benchmarks ══════════");
            BenchResult r1 = bench("flatten   medium", WARMUP, MEASURE,
                    () -> YmlFlattener.flatten(MEDIUM_MAP));
            BenchResult r2 = bench("unflatten medium", WARMUP, MEASURE,
                    () -> YmlFlattener.unflatten(FLAT_MAP));
            System.out.println(r1);
            System.out.println(r2);
            System.out.println();
            assertThat(r1.opsPerMs()).isGreaterThan(1);
        }
    }

    @Nested
    @DisplayName("JSON 转换基准 (JSON)")
    class JsonBenchmarks {

        @Test
        @DisplayName("YAML↔JSON 转换吞吐量")
        void jsonThroughput() {
            System.out.println("\n══════════ JSON Convert Benchmarks ══════════");
            BenchResult r1 = bench("toJson   medium (compact)",  WARMUP, MEASURE,
                    () -> YmlJson.toJson(MEDIUM_MAP));
            BenchResult r2 = bench("toJson   medium (pretty)",   WARMUP, MEASURE,
                    () -> YmlJson.toJson(MEDIUM_MAP, true));
            String json = YmlJson.toJson(MEDIUM_MAP);
            BenchResult r3 = bench("fromJson medium",            WARMUP, MEASURE,
                    () -> YmlJson.fromJson(json));
            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);
            System.out.println();
            assertThat(r1.opsPerMs()).isGreaterThan(1);
        }
    }

    @Nested
    @DisplayName("占位符基准 (Placeholder)")
    class PlaceholderBenchmarks {

        @Test
        @DisplayName("占位符解析吞吐量")
        void placeholderThroughput() {
            System.out.println("\n══════════ Placeholder Benchmarks ══════════");
            BenchResult r1 = bench("resolve  3 placeholders",      WARMUP, MEASURE,
                    () -> RESOLVER.resolve(PLACEHOLDER_YAML));
            BenchResult r2 = bench("resolve  no placeholders",     WARMUP, MEASURE,
                    () -> RESOLVER.resolve(SMALL_YAML));
            System.out.println(r1);
            System.out.println(r2);
            System.out.println();
            assertThat(r1.opsPerMs()).isGreaterThan(1);
        }
    }

    @Nested
    @DisplayName("校验基准 (Schema)")
    class SchemaBenchmarks {

        @Test
        @DisplayName("结构校验吞吐量")
        void schemaThroughput() {
            System.out.println("\n══════════ Schema Validate Benchmarks ══════════");
            BenchResult r1 = bench("validate medium (pass)", WARMUP, MEASURE,
                    () -> SCHEMA.validate(MEDIUM_MAP));
            System.out.println(r1);
            System.out.println();
            assertThat(r1.opsPerMs()).isGreaterThan(1);
        }
    }

    @Nested
    @DisplayName("端到端基准 (E2E)")
    class E2EBenchmarks {

        @Test
        @DisplayName("端到端: 解析→绑定→校验→转储")
        void e2eThroughput() {
            System.out.println("\n══════════ E2E Benchmarks ══════════");
            BenchResult r1 = bench("e2e    parse→bind→validate→dump", WARMUP, MEASURE / 2, () -> {
                Map<String, Object> data = OpenYml.load(MEDIUM_YAML);
                YmlDocument doc = YmlDocument.of(data);
                SCHEMA.validate(data);
                ServerConfig cfg = YmlBinder.bind(doc, "server", ServerConfig.class);
                OpenYml.dump(data);
            });
            System.out.println(r1);
            System.out.println();
            assertThat(r1.opsPerMs()).isGreaterThan(0.1);
        }
    }
}
