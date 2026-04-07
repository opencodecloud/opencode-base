/*
 * Copyright 2025 Leon Soo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cloud.opencode.base.json.benchmark;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.OpenJson;
import cloud.opencode.base.json.util.JsonCanonicalizer;
import cloud.opencode.base.json.util.JsonEquals;
import cloud.opencode.base.json.util.JsonFlattener;
import cloud.opencode.base.json.util.JsonStrings;
import cloud.opencode.base.json.util.JsonTruncator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance Benchmarks for opencode-base-json
 * opencode-base-json 性能基准测试
 *
 * <p>Measures throughput and latency of core JSON operations using warmup + measurement
 * iterations with nanoTime-based timing. Covers hot paths identified during code review:
 * parse, serialize, validate, minify, pretty-print, canonicalize, flatten, and deep-equals.</p>
 * <p>使用预热+测量迭代和 nanoTime 计时测量核心 JSON 操作的吞吐量和延迟。
 * 覆盖代码审查中识别的热路径：解析、序列化、验证、压缩、格式化、规范化、扁平化和深度比较。</p>
 *
 * <p><strong>Run:</strong> {@code mvn test -pl opencode-base-json -Dgroups=benchmark}</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.3
 */
@DisplayName("opencode-base-json 性能基准测试")
@Tag("benchmark")
class JsonBenchmark {

    // ==================== Fixtures ====================

    /** Small JSON: 7 keys, ~120 chars. 小 JSON：7 个键，约 120 字符。 */
    private static final String SMALL_JSON =
            "{\"id\":1,\"name\":\"Alice\",\"age\":30,\"active\":true," +
            "\"score\":9.5,\"rank\":null,\"tags\":[\"admin\",\"user\"]}";

    /** Medium JSON: user profile with nested objects and arrays, ~600 chars. 中等 JSON：约 600 字符。 */
    private static final String MEDIUM_JSON =
            "{\"user\":{\"id\":12345,\"name\":\"Alice Smith\",\"email\":\"alice@example.com\"," +
            "\"address\":{\"street\":\"123 Main St\",\"city\":\"Springfield\",\"zip\":\"12345\"}," +
            "\"roles\":[\"admin\",\"editor\",\"viewer\"]," +
            "\"metadata\":{\"created\":\"2024-01-01\",\"updated\":\"2024-06-15\",\"version\":3}}," +
            "\"permissions\":{\"read\":true,\"write\":true,\"delete\":false}," +
            "\"scores\":[{\"subject\":\"math\",\"grade\":95},{\"subject\":\"science\",\"grade\":88}," +
            "{\"subject\":\"history\",\"grade\":76}]," +
            "\"config\":{\"theme\":\"dark\",\"language\":\"en\",\"timezone\":\"UTC\"}}";

    /** Large JSON: array of 50 objects, ~4KB. 大型 JSON：50 个对象数组，约 4KB。 */
    private static final String LARGE_JSON = buildLargeJson();

    private static String buildLargeJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 50; i++) {
            if (i > 0) sb.append(',');
            sb.append("{\"id\":").append(i)
              .append(",\"name\":\"User").append(i).append("\"")
              .append(",\"email\":\"user").append(i).append("@example.com\"")
              .append(",\"score\":").append(i * 1.5)
              .append(",\"active\":").append(i % 2 == 0)
              .append(",\"tags\":[\"tag").append(i % 5).append("\",\"tag").append(i % 3).append("\"]")
              .append(",\"meta\":{\"created\":\"2024-0").append(i % 9 + 1).append("-01\"")
              .append(",\"version\":").append(i % 10).append("}}");
        }
        sb.append("]");
        return sb.toString();
    }

    /** Pre-parsed nodes for serialize-only benchmarks. 预解析节点，用于仅序列化基准。 */
    private static final JsonNode SMALL_NODE = OpenJson.parse(SMALL_JSON);
    private static final JsonNode MEDIUM_NODE = OpenJson.parse(MEDIUM_JSON);
    private static final JsonNode LARGE_NODE = OpenJson.parse(LARGE_JSON);

    /** Nested node for canonicalize / flatten benchmarks. 用于规范化/扁平化基准的嵌套节点。 */
    private static final JsonNode NESTED_NODE = buildNestedNode();

    private static JsonNode buildNestedNode() {
        return JsonNode.object()
                .put("z", JsonNode.object().put("b", 2).put("a", 1))
                .put("y", JsonNode.array()
                        .add(JsonNode.object().put("d", 4).put("c", 3))
                        .add(JsonNode.object().put("f", 6).put("e", 5)))
                .put("x", "hello world")
                .put("w", 3.14159)
                .put("v", true);
    }

    /** Pre-flattened map for unflatten benchmarks. 预扁平化映射，用于反扁平化基准。 */
    private static final Map<String, JsonNode> FLAT_MAP = JsonFlattener.flatten(MEDIUM_NODE);

    // ==================== Benchmark Infrastructure ====================

    private record BenchResult(String name, long ops, long totalNanos) {
        double opsPerMs() { return ops * 1_000_000.0 / totalNanos; }
        double nsPerOp()  { return (double) totalNanos / ops; }

        @Override
        public String toString() {
            return String.format("  %-52s %,8d ops  %,9.0f ops/ms  %,6.1f ns/op",
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
    @DisplayName("解析基准测试 (Parse)")
    class ParseBenchmarks {

        @Test
        @DisplayName("解析性能: 小/中/大 JSON")
        void parseThroughput() {
            System.out.println("\n══════════ Parse Benchmarks ══════════");
            BenchResult r1 = bench("parse  small (~120 chars)",   WARMUP, MEASURE, () -> OpenJson.parse(SMALL_JSON));
            BenchResult r2 = bench("parse  medium (~600 chars)",  WARMUP, MEASURE, () -> OpenJson.parse(MEDIUM_JSON));
            BenchResult r3 = bench("parse  large  (~4KB, 50 obj)",WARMUP, MEASURE / 5, () -> OpenJson.parse(LARGE_JSON));
            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);
            System.out.println();
            assertThat(r1.opsPerMs()).isGreaterThan(0);
            assertThat(r2.opsPerMs()).isGreaterThan(0);
            assertThat(r3.opsPerMs()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("序列化基准测试 (Serialize)")
    class SerializeBenchmarks {

        @Test
        @DisplayName("序列化性能: 小/中/大 JsonNode")
        void serializeThroughput() {
            System.out.println("\n══════════ Serialize Benchmarks ══════════");
            BenchResult r1 = bench("serialize small  node",       WARMUP, MEASURE,     () -> OpenJson.toJson(SMALL_NODE));
            BenchResult r2 = bench("serialize medium node",       WARMUP, MEASURE,     () -> OpenJson.toJson(MEDIUM_NODE));
            BenchResult r3 = bench("serialize large  node",       WARMUP, MEASURE / 5, () -> OpenJson.toJson(LARGE_NODE));
            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);
            System.out.println();
            assertThat(r1.opsPerMs()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("JsonStrings 基准测试")
    class JsonStringsBenchmarks {

        @Test
        @DisplayName("isValid / minify / prettyPrint 性能")
        void stringsOperations() {
            System.out.println("\n══════════ JsonStrings Benchmarks ══════════");

            // isValid — state machine, no tree
            BenchResult r1 = bench("isValid small",               WARMUP, MEASURE,     () -> JsonStrings.isValid(SMALL_JSON));
            BenchResult r2 = bench("isValid medium",              WARMUP, MEASURE,     () -> JsonStrings.isValid(MEDIUM_JSON));

            // minify — single pass, strip whitespace
            String prettySmall  = JsonStrings.prettyPrint(SMALL_JSON);
            String prettyMedium = JsonStrings.prettyPrint(MEDIUM_JSON);
            BenchResult r3 = bench("minify small  (from pretty)", WARMUP, MEASURE,     () -> JsonStrings.minify(prettySmall));
            BenchResult r4 = bench("minify medium (from pretty)", WARMUP, MEASURE,     () -> JsonStrings.minify(prettyMedium));

            // prettyPrint — single pass, indentation
            BenchResult r5 = bench("prettyPrint small",           WARMUP, MEASURE,     () -> JsonStrings.prettyPrint(SMALL_JSON));
            BenchResult r6 = bench("prettyPrint medium",          WARMUP, MEASURE,     () -> JsonStrings.prettyPrint(MEDIUM_JSON));

            // escape / unescape — char-by-char
            String withSpecials = "Hello\\nWorld\\t\"test\\\\path\"\u0001\u001f";
            BenchResult r7 = bench("escape   (special chars)",    WARMUP, MEASURE,     () -> JsonStrings.escape(withSpecials));
            BenchResult r8 = bench("unescape (escape sequences)", WARMUP, MEASURE,     () -> JsonStrings.unescape(JsonStrings.escape(withSpecials)));

            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);
            System.out.println(r4);
            System.out.println(r5);
            System.out.println(r6);
            System.out.println(r7);
            System.out.println(r8);
            System.out.println();

            assertThat(r1.opsPerMs()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("JsonCanonicalizer 基准测试")
    class CanonicalizerBenchmarks {

        @Test
        @DisplayName("规范化性能: 对象键排序 + ES6 数字")
        void canonicalizeThroughput() {
            System.out.println("\n══════════ JsonCanonicalizer Benchmarks ══════════");

            // Key sorting is the main cost for objects
            BenchResult r1 = bench("canonicalize small  object",  WARMUP, MEASURE,     () -> JsonCanonicalizer.canonicalize(SMALL_NODE));
            BenchResult r2 = bench("canonicalize medium object",  WARMUP, MEASURE,     () -> JsonCanonicalizer.canonicalize(MEDIUM_NODE));
            BenchResult r3 = bench("canonicalize nested (5 keys)",WARMUP, MEASURE,     () -> JsonCanonicalizer.canonicalize(NESTED_NODE));

            // Number serialization: integer, double, BigDecimal
            JsonNode intNode    = JsonNode.of(42);
            JsonNode doubleNode = JsonNode.of(1.23456789);
            JsonNode largeInt   = JsonNode.of(1e20);   // expands to BigInteger via BigDecimal
            BenchResult r4 = bench("canonicalize int    42",      WARMUP, MEASURE,     () -> JsonCanonicalizer.canonicalize(intNode));
            BenchResult r5 = bench("canonicalize double 1.2345",  WARMUP, MEASURE,     () -> JsonCanonicalizer.canonicalize(doubleNode));
            BenchResult r6 = bench("canonicalize 1e20   (BigInt)",WARMUP, MEASURE,     () -> JsonCanonicalizer.canonicalize(largeInt));

            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);
            System.out.println(r4);
            System.out.println(r5);
            System.out.println(r6);
            System.out.println();

            assertThat(r1.opsPerMs()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("JsonFlattener 基准测试")
    class FlattenerBenchmarks {

        @Test
        @DisplayName("flatten / unflatten 性能")
        void flattenUnflattenThroughput() {
            System.out.println("\n══════════ JsonFlattener Benchmarks ══════════");

            BenchResult r1 = bench("flatten   small  node",       WARMUP, MEASURE,     () -> JsonFlattener.flatten(SMALL_NODE));
            BenchResult r2 = bench("flatten   medium node",       WARMUP, MEASURE,     () -> JsonFlattener.flatten(MEDIUM_NODE));
            BenchResult r3 = bench("flatten   nested (5 keys)",   WARMUP, MEASURE,     () -> JsonFlattener.flatten(NESTED_NODE));
            BenchResult r4 = bench("unflatten medium flat-map",   WARMUP, MEASURE,     () -> JsonFlattener.unflatten(FLAT_MAP));

            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);
            System.out.println(r4);
            System.out.println();

            assertThat(r1.opsPerMs()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("JsonEquals 基准测试")
    class EqualsBenchmarks {

        @Test
        @DisplayName("结构相等性比较性能")
        void equalsThroughput() {
            System.out.println("\n══════════ JsonEquals Benchmarks ══════════");

            // Equal nodes (most common case — full traversal)
            JsonNode nodeA = OpenJson.parse(MEDIUM_JSON);
            JsonNode nodeB = OpenJson.parse(MEDIUM_JSON);

            // Unequal nodes (should short-circuit quickly)
            JsonNode nodeC = JsonNode.object().put("x", 1);
            JsonNode nodeD = JsonNode.object().put("x", 2);

            BenchResult r1 = bench("equals medium node (equal)",     WARMUP, MEASURE, () -> JsonEquals.equals(nodeA, nodeB));
            BenchResult r2 = bench("equals small node  (not equal)", WARMUP, MEASURE, () -> JsonEquals.equals(nodeC, nodeD));
            BenchResult r3 = bench("equals via JSON string (medium)",WARMUP, MEASURE, () -> JsonEquals.equals(MEDIUM_JSON, MEDIUM_JSON));

            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);
            System.out.println();

            assertThat(r1.opsPerMs()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("JsonTruncator 基准测试")
    class TruncatorBenchmarks {

        @Test
        @DisplayName("截断性能: 字符串级 + 树级")
        void truncateThroughput() {
            System.out.println("\n══════════ JsonTruncator Benchmarks ══════════");

            // String-level truncation: no parse, just substring
            BenchResult r1 = bench("truncate(String) short  (no-op)", WARMUP, MEASURE, () -> JsonTruncator.truncate(SMALL_JSON, 10_000));
            BenchResult r2 = bench("truncate(String) long   (cut)",   WARMUP, MEASURE, () -> JsonTruncator.truncate(LARGE_JSON, 100));

            // Tree-level truncation: traverse + depth limit
            JsonTruncator.TruncateConfig cfg = new JsonTruncator.TruncateConfig(4096, 5, 50, 5, "...");
            BenchResult r3 = bench("truncate(JsonNode) medium",       WARMUP, MEASURE, () -> JsonTruncator.truncate(MEDIUM_NODE, cfg));
            BenchResult r4 = bench("truncate(JsonNode) large",        WARMUP, MEASURE / 5, () -> JsonTruncator.truncate(LARGE_NODE, cfg));

            // Summary: O(1) for primitive, O(1) for container (just count)
            BenchResult r5 = bench("summary object (medium)",         WARMUP, MEASURE, () -> JsonTruncator.summary(MEDIUM_NODE));

            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);
            System.out.println(r4);
            System.out.println(r5);
            System.out.println();

            assertThat(r1.opsPerMs()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("BeanMapper POJO 序列化/反序列化基准测试")
    class BeanMapperBenchmarks {

        record UserRecord(String name, int age, boolean active, String email) {}

        static final UserRecord POJO = new UserRecord("Alice Smith", 30, true, "alice@example.com");
        static final String POJO_JSON = "{\"name\":\"Alice Smith\",\"age\":30,\"active\":true,\"email\":\"alice@example.com\"}";

        @Test
        @DisplayName("BeanMapper POJO 序列化/反序列化吞吐量")
        void pojoThroughput() {
            System.out.println("\n══════════ BeanMapper Benchmarks ══════════");

            // Serialize: POJO → JSON (includes IdentityHashMap circular reference check)
            BenchResult r1 = bench("toJson POJO (record, 4 fields)", WARMUP, MEASURE,
                    () -> OpenJson.toJson(POJO));

            // Deserialize: JSON → POJO
            BenchResult r2 = bench("fromJson → record (4 fields)",   WARMUP, MEASURE,
                    () -> OpenJson.fromJson(POJO_JSON, UserRecord.class));

            // Map serialization (JsonSerializer path, not BeanMapper)
            java.util.Map<String, Object> map = java.util.Map.of(
                    "name", "Alice", "age", 30, "active", true, "email", "alice@example.com");
            BenchResult r3 = bench("toJson Map (4 entries)",          WARMUP, MEASURE,
                    () -> OpenJson.toJson(map));

            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);
            System.out.println();

            assertThat(r1.opsPerMs()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("综合吞吐量报告")
    class SummaryBenchmarks {

        @Test
        @DisplayName("全量热路径吞吐量汇总")
        void fullSummary() {
            System.out.println("\n╔══════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║        opencode-base-json  V1.0.3 — Performance Benchmark Report        ║");
            System.out.println("╚══════════════════════════════════════════════════════════════════════════╝\n");

            System.out.println("── Parse ─────────────────────────────────────────────────────────────────");
            print(bench("parse  small  (~120 chars)",    WARMUP, MEASURE,     () -> OpenJson.parse(SMALL_JSON)));
            print(bench("parse  medium (~600 chars)",    WARMUP, MEASURE,     () -> OpenJson.parse(MEDIUM_JSON)));

            System.out.println("── Serialize ─────────────────────────────────────────────────────────────");
            print(bench("toJson small  node",            WARMUP, MEASURE,     () -> OpenJson.toJson(SMALL_NODE)));
            print(bench("toJson medium node",            WARMUP, MEASURE,     () -> OpenJson.toJson(MEDIUM_NODE)));

            System.out.println("── JsonStrings ───────────────────────────────────────────────────────────");
            print(bench("isValid     small",             WARMUP, MEASURE,     () -> JsonStrings.isValid(SMALL_JSON)));
            print(bench("isValid     medium",            WARMUP, MEASURE,     () -> JsonStrings.isValid(MEDIUM_JSON)));
            print(bench("minify      small",             WARMUP, MEASURE,     () -> JsonStrings.minify(SMALL_JSON)));
            print(bench("prettyPrint small",             WARMUP, MEASURE,     () -> JsonStrings.prettyPrint(SMALL_JSON)));

            System.out.println("── JsonCanonicalizer ─────────────────────────────────────────────────────");
            print(bench("canonicalize small  object",    WARMUP, MEASURE,     () -> JsonCanonicalizer.canonicalize(SMALL_NODE)));
            print(bench("canonicalize medium object",    WARMUP, MEASURE,     () -> JsonCanonicalizer.canonicalize(MEDIUM_NODE)));

            System.out.println("── JsonFlattener ─────────────────────────────────────────────────────────");
            print(bench("flatten   medium",              WARMUP, MEASURE,     () -> JsonFlattener.flatten(MEDIUM_NODE)));
            print(bench("unflatten medium flat-map",     WARMUP, MEASURE,     () -> JsonFlattener.unflatten(FLAT_MAP)));

            System.out.println("── JsonEquals ────────────────────────────────────────────────────────────");
            JsonNode a = OpenJson.parse(MEDIUM_JSON);
            JsonNode b = OpenJson.parse(MEDIUM_JSON);
            print(bench("equals    medium (equal)",      WARMUP, MEASURE,     () -> JsonEquals.equals(a, b)));

            System.out.println("── JsonTruncator ─────────────────────────────────────────────────────────");
            JsonTruncator.TruncateConfig cfg = new JsonTruncator.TruncateConfig(4096, 5, 50, 5, "...");
            print(bench("truncate(JsonNode) medium",     WARMUP, MEASURE,     () -> JsonTruncator.truncate(MEDIUM_NODE, cfg)));

            System.out.println("\n──────────────────────────────────────────────────────────────────────────");
            System.out.println("  JVM: " + System.getProperty("java.vm.version") +
                               "  OS: " + System.getProperty("os.name"));
            System.out.println();
        }

        private void print(BenchResult r) { System.out.println(r); }
    }
}
