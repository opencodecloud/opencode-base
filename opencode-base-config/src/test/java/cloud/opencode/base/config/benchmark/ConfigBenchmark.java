package cloud.opencode.base.config.benchmark;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.ConfigDiff;
import cloud.opencode.base.config.ConfigDump;
import cloud.opencode.base.config.OpenConfig;
import cloud.opencode.base.config.RelaxedKeyResolver;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance benchmarks for opencode-base-config v1.0.3 new components.
 * nanoTime loop benchmarks (non-JMH, sufficient for order-of-magnitude detection).
 *
 * @author Leon Soo
 */
class ConfigBenchmark {

    private static final int WARMUP = 50_000;
    private static final int ITERATIONS = 500_000;

    private static double benchmark(String name, Runnable op) {
        for (int i = 0; i < WARMUP; i++) op.run();
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) op.run();
        long elapsed = System.nanoTime() - start;
        double nsPerOp = (double) elapsed / ITERATIONS;
        double opsPerMs = (double) ITERATIONS / (elapsed / 1_000_000.0);
        System.out.printf("  %-50s %10.0f ops/ms  %8.1f ns/op%n", name, opsPerMs, nsPerOp);
        return nsPerOp;
    }

    // ======================= RelaxedKeyResolver =======================

    @Test
    void relaxedKeyResolver_normalize() {
        System.out.println("\n=== RelaxedKeyResolver.normalize() ===");

        double kebabNs = benchmark("normalize(kebab-case)", () ->
                RelaxedKeyResolver.normalize("database.max-pool-size"));

        double camelNs = benchmark("normalize(camelCase)", () ->
                RelaxedKeyResolver.normalize("database.maxPoolSize"));

        double upperNs = benchmark("normalize(UPPER_SNAKE)", () ->
                RelaxedKeyResolver.normalize("DATABASE_MAX_POOL_SIZE"));

        assertThat(kebabNs).isLessThan(200); // guard: must be < 200ns
        assertThat(upperNs).isLessThan(200);
    }

    @Test
    void relaxedKeyResolver_resolve_with_cache() {
        System.out.println("\n=== RelaxedKeyResolver.resolve() ===");

        // Simulate a config with 100 keys
        Map<String, String> props = new LinkedHashMap<>();
        for (int i = 0; i < 100; i++) {
            props.put("app.section" + i + ".property-name", "value" + i);
        }
        Set<String> keys = props.keySet();

        // Resolve existing key (worst case: last in iteration)
        double resolveHitNs = benchmark("resolve(100 keys, hit)", () ->
                RelaxedKeyResolver.resolve("app.section99.propertyName", keys));

        // Resolve non-existing key (full scan miss)
        double resolveMissNs = benchmark("resolve(100 keys, miss)", () ->
                RelaxedKeyResolver.resolve("app.nonexistent.key", keys));

        System.out.printf("  → resolve hit: %.1f ns, miss: %.1f ns%n", resolveHitNs, resolveMissNs);
    }

    @Test
    void relaxedBinding_cached_config_lookup() {
        System.out.println("\n=== Config.getString() with relaxed binding (cached) ===");

        Map<String, String> props = new LinkedHashMap<>();
        props.put("DATABASE_MAX_POOL_SIZE", "10");
        props.put("app.server.host", "localhost");
        for (int i = 0; i < 98; i++) {
            props.put("app.filler.key" + i, "value" + i);
        }

        Config config = OpenConfig.builder()
                .addProperties(props)
                .enableRelaxedBinding()
                .build();

        // Warmup the cache
        config.getString("database.max-pool-size");

        double cachedNs = benchmark("getString(relaxed, cached hit)", () ->
                config.getString("database.max-pool-size"));

        // Direct key lookup for comparison
        Config directConfig = OpenConfig.builder()
                .addProperties(props)
                .build();

        double directNs = benchmark("getString(exact match, no relaxed)", () ->
                directConfig.getString("DATABASE_MAX_POOL_SIZE"));

        double ratio = cachedNs / directNs;
        System.out.printf("  → relaxed(cached) / direct ratio: %.2fx%n", ratio);
        // Cached relaxed binding should be < 10x of direct lookup
        // (absolute value ~75ns is acceptable; ratio fluctuates with JIT)
        assertThat(ratio).isLessThan(10.0);
    }

    // ======================= ConfigDump =======================

    @Test
    void configDump_performance() {
        System.out.println("\n=== ConfigDump.dump() ===");

        Map<String, String> props = new LinkedHashMap<>();
        for (int i = 0; i < 100; i++) {
            props.put("app.config.key" + i, "value" + i);
        }
        props.put("db.password", "secret123");
        props.put("api.token", "tok-abc");

        Config config = OpenConfig.builder().addProperties(props).build();

        int dumpIterations = 10_000;
        for (int i = 0; i < 1_000; i++) ConfigDump.dump(config); // warmup
        long start = System.nanoTime();
        for (int i = 0; i < dumpIterations; i++) ConfigDump.dump(config);
        double dumpNs = (double) (System.nanoTime() - start) / dumpIterations;
        System.out.printf("  %-50s %8.0f ns/call (%.2f ms)%n",
                "dump(102 keys, default patterns)", dumpNs, dumpNs / 1_000_000);

        // Should be < 1ms for 100 keys
        assertThat(dumpNs).isLessThan(1_000_000);
    }

    // ======================= ConfigDiff =======================

    @Test
    void configDiff_performance() {
        System.out.println("\n=== ConfigDiff.diff() ===");

        Map<String, String> before = new LinkedHashMap<>();
        Map<String, String> after = new LinkedHashMap<>();
        for (int i = 0; i < 100; i++) {
            before.put("key" + i, "value" + i);
            after.put("key" + i, i < 90 ? "value" + i : "changed" + i); // 10 modified
        }
        after.put("newkey", "newvalue"); // 1 added
        // key99 removed from after already handled by loop

        int diffIterations = 50_000;
        for (int i = 0; i < 5_000; i++) ConfigDiff.diff(before, after); // warmup
        long start = System.nanoTime();
        for (int i = 0; i < diffIterations; i++) ConfigDiff.diff(before, after);
        double diffNs = (double) (System.nanoTime() - start) / diffIterations;
        System.out.printf("  %-50s %8.0f ns/call%n", "diff(101 keys, 11 changes)", diffNs);

        // Should be < 500μs for 100 keys
        assertThat(diffNs).isLessThan(500_000);
    }

    // ======================= Summary =======================

    @Test
    void printSummaryHeader() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  OpenCode-Base-Config v1.0.3 Performance Benchmark");
        System.out.println("  JDK: " + Runtime.version());
        System.out.println("  OS:  " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        System.out.println("  CPU: " + Runtime.getRuntime().availableProcessors() + " cores");
        System.out.println("=".repeat(70));
    }
}
