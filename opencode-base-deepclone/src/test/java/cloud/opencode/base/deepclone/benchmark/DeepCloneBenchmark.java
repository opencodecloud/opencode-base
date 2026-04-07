package cloud.opencode.base.deepclone.benchmark;

import cloud.opencode.base.deepclone.*;
import cloud.opencode.base.deepclone.cloner.ReflectiveCloner;
import cloud.opencode.base.deepclone.cloner.UnsafeCloner;

import org.junit.jupiter.api.*;

import java.io.Serializable;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance benchmarks for opencode-base-deepclone.
 * 深度克隆模块性能基准测试。
 *
 * <p>Uses nanoTime loop with warmup/measure phases (lightweight, non-JMH).
 * Sufficient to detect order-of-magnitude regressions.</p>
 *
 * <p>Run with: {@code mvn test -pl opencode-base-deepclone -Dtest="DeepCloneBenchmark"}</p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeepCloneBenchmark {

    private static final int WARMUP = 10_000;
    private static final int ITERATIONS = 100_000;

    // ==================== Test Data ====================

    static class SimpleObject implements Serializable {
        String name = "Alice";
        int age = 30;
        double score = 95.5;
        boolean active = true;
    }

    static class NestedObject implements Serializable {
        String label = "root";
        SimpleObject child = new SimpleObject();
        List<String> tags = new ArrayList<>(List.of("java", "clone", "test"));
        Map<String, Integer> scores = new HashMap<>(Map.of("math", 95, "eng", 88));
    }

    static class DeepObject implements Serializable {
        String id = "deep-1";
        NestedObject nested = new NestedObject();
        Optional<String> nickname = Optional.of("Bob");
        List<String> immutableTags = List.of("a", "b", "c");

        enum Status { ACTIVE, INACTIVE }
        Status status = Status.ACTIVE;
    }

    static class WideObject implements Serializable {
        String f1 = "a", f2 = "b", f3 = "c", f4 = "d", f5 = "e";
        String f6 = "f", f7 = "g", f8 = "h", f9 = "i", f10 = "j";
        int i1 = 1, i2 = 2, i3 = 3, i4 = 4, i5 = 5;
        double d1 = 1.0, d2 = 2.0, d3 = 3.0;
    }

    // ==================== Benchmark Helper ====================

    private static double benchmark(String name, Runnable op) {
        for (int i = 0; i < WARMUP; i++) op.run();

        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) op.run();
        long elapsed = System.nanoTime() - start;

        double opsPerMs = (double) ITERATIONS / (elapsed / 1_000_000.0);
        double nsPerOp = (double) elapsed / ITERATIONS;
        System.out.printf("  %-50s %10.0f ops/ms  %8.1f ns/op%n", name, opsPerMs, nsPerOp);
        return nsPerOp;
    }

    // ==================== 1. Strategy Comparison ====================

    @Test
    @Order(1)
    void printHeader() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("  OpenCode-Base-DeepClone Performance Benchmark");
        System.out.println("  JDK: " + Runtime.version());
        System.out.println("  OS:  " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        System.out.println("  CPU: " + Runtime.getRuntime().availableProcessors() + " cores");
        System.out.println("  Warmup: " + WARMUP + ", Measure: " + ITERATIONS);
        System.out.println("=".repeat(80));
    }

    @Test
    @Order(2)
    void strategyComparison_simpleObject() {
        System.out.println("\n=== Strategy Comparison: SimpleObject (4 fields) ===");
        var obj = new SimpleObject();

        double reflective = benchmark("Reflective (default)", () -> OpenClone.clone(obj));
        double unsafe = benchmark("Unsafe", () -> OpenClone.cloneByUnsafe(obj));
        double serializing = benchmark("Serialization", () -> OpenClone.cloneBySerialization(obj));

        System.out.printf("  → Reflective/Unsafe ratio:       %.2fx%n", reflective / unsafe);
        System.out.printf("  → Serialization/Reflective ratio: %.2fx%n", serializing / reflective);

        // Serialization should be significantly slower
        assertThat(serializing).isGreaterThan(reflective);
    }

    @Test
    @Order(3)
    void strategyComparison_nestedObject() {
        System.out.println("\n=== Strategy Comparison: NestedObject (4 fields + child + list + map) ===");
        var obj = new NestedObject();

        double reflective = benchmark("Reflective (default)", () -> OpenClone.clone(obj));
        double unsafe = benchmark("Unsafe", () -> OpenClone.cloneByUnsafe(obj));
        double serializing = benchmark("Serialization", () -> OpenClone.cloneBySerialization(obj));

        System.out.printf("  → Reflective/Unsafe ratio:       %.2fx%n", reflective / unsafe);
        System.out.printf("  → Serialization/Reflective ratio: %.2fx%n", serializing / reflective);
    }

    // ==================== 2. Clone vs Shallow Clone ====================

    @Test
    @Order(4)
    void deepClone_vs_shallowClone() {
        System.out.println("\n=== Deep Clone vs Shallow Clone: NestedObject ===");
        var obj = new NestedObject();

        double deep = benchmark("OpenClone.clone()", () -> OpenClone.clone(obj));
        double shallow = benchmark("OpenClone.shallowClone()", () -> OpenClone.shallowClone(obj));

        double ratio = deep / shallow;
        System.out.printf("  → Deep/Shallow ratio: %.2fx%n", ratio);

        // Shallow should be faster than deep
        assertThat(shallow).isLessThan(deep);
    }

    // ==================== 3. Immutable Optimization ====================

    @Test
    @Order(5)
    void immutableOptimization() {
        System.out.println("\n=== Immutable Detection: List.of() vs ArrayList ===");
        var objWithImmutable = new DeepObject();

        // DeepObject has immutableTags=List.of("a","b","c") which should be skipped
        double withImmutable = benchmark("DeepObject (has List.of field)", () -> OpenClone.clone(objWithImmutable));

        // Compare with nested which has mutable ArrayList
        var objWithMutable = new NestedObject();
        double withMutable = benchmark("NestedObject (has ArrayList field)", () -> OpenClone.clone(objWithMutable));

        System.out.printf("  → DeepObject includes enum + Optional + List.of — should benefit from skips%n");
    }

    // ==================== 4. Enum Identity ====================

    @Test
    @Order(6)
    void enumIdentityPreservation() {
        System.out.println("\n=== Enum Identity Preservation ===");
        var obj = new DeepObject();

        benchmark("Clone with enum field (identity preserved)", () -> {
            DeepObject cloned = OpenClone.clone(obj);
            assert cloned.status == DeepObject.Status.ACTIVE;
        });
    }

    // ==================== 5. FieldFilter Overhead ====================

    @Test
    @Order(7)
    void fieldFilter_overhead() {
        System.out.println("\n=== FieldFilter Overhead ===");
        var obj = new WideObject();

        Cloner noFilter = OpenClone.builder().reflective().build();
        Cloner withFilter = OpenClone.builder()
                .reflective()
                .filter(FieldFilter.excludeNames("f1", "f2", "f3"))
                .build();

        double noFilterNs = benchmark("No filter (18 fields)", () -> noFilter.clone(obj));
        double withFilterNs = benchmark("With filter (exclude 3 fields)", () -> withFilter.clone(obj));

        double overhead = (withFilterNs - noFilterNs) / noFilterNs * 100;
        System.out.printf("  → Filter overhead: %.1f%% (should be < 20%%)%n", overhead);

        // Filter should not add more than 50% overhead
        assertThat(withFilterNs).isLessThan(noFilterNs * 1.5);
    }

    // ==================== 6. CloneListener Overhead ====================

    @Test
    @Order(8)
    void cloneListener_overhead() {
        System.out.println("\n=== CloneListener Overhead ===");
        var obj = new NestedObject();

        Cloner noListener = OpenClone.builder().reflective().build();
        Cloner withListener = OpenClone.builder()
                .reflective()
                .listener(new CloneListener() {
                    @Override
                    public void afterClone(Object original, Object cloned, CloneContext ctx) {
                        // no-op listener
                    }
                })
                .build();

        double noListenerNs = benchmark("No listener", () -> noListener.clone(obj));
        double withListenerNs = benchmark("With no-op listener", () -> withListener.clone(obj));

        double overhead = (withListenerNs - noListenerNs) / noListenerNs * 100;
        System.out.printf("  → Listener overhead: %.1f%% (per-object invocation; acceptable < 200%%)%n", overhead);

        // Listener is called per-object (including nested children), so overhead
        // scales with object graph depth. For NestedObject (~8 sub-objects),
        // 14-16 extra method calls add ~400-500ns absolute overhead.
        assertThat(withListenerNs).isLessThan(noListenerNs * 3.0);
    }

    // ==================== 7. ClonePolicy Comparison ====================

    @Test
    @Order(9)
    void clonePolicy_comparison() {
        System.out.println("\n=== ClonePolicy: STANDARD vs LENIENT ===");
        var obj = new SimpleObject();

        Cloner standard = OpenClone.builder().policy(ClonePolicy.STANDARD).build();
        Cloner lenient = OpenClone.builder().policy(ClonePolicy.LENIENT).build();

        double standardNs = benchmark("STANDARD policy", () -> standard.clone(obj));
        double lenientNs = benchmark("LENIENT policy", () -> lenient.clone(obj));

        double ratio = lenientNs / standardNs;
        System.out.printf("  → LENIENT/STANDARD ratio: %.2fx (should be ~1.0x on happy path)%n", ratio);

        // On happy path (no errors), LENIENT should be nearly same speed
        assertThat(ratio).isBetween(0.5, 2.0);
    }

    // ==================== 8. Batch Clone ====================

    @Test
    @Order(10)
    void batchClone_performance() {
        System.out.println("\n=== Batch Clone: 100 SimpleObjects ===");
        List<SimpleObject> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) list.add(new SimpleObject());

        int batchWarmup = 1_000;
        int batchIters = 10_000;

        // Sequential
        for (int i = 0; i < batchWarmup; i++) OpenClone.cloneBatch(list);
        long start = System.nanoTime();
        for (int i = 0; i < batchIters; i++) OpenClone.cloneBatch(list);
        double seqNs = (double) (System.nanoTime() - start) / batchIters;
        System.out.printf("  %-50s %10.0f ns/batch  (%.1f ns/obj)%n",
                "cloneBatch(100)", seqNs, seqNs / 100);

        // Parallel
        for (int i = 0; i < batchWarmup; i++) OpenClone.cloneBatchParallel(list, 4);
        start = System.nanoTime();
        for (int i = 0; i < batchIters; i++) OpenClone.cloneBatchParallel(list, 4);
        double parNs = (double) (System.nanoTime() - start) / batchIters;
        System.out.printf("  %-50s %10.0f ns/batch  (%.1f ns/obj)%n",
                "cloneBatchParallel(100, 4)", parNs, parNs / 100);

        System.out.printf("  → Parallel/Sequential ratio: %.2fx%n", parNs / seqNs);
        // Parallel has thread creation overhead, may be slower for small batches
    }

    // ==================== 9. CopyTo Performance ====================

    @Test
    @Order(11)
    void copyTo_performance() {
        System.out.println("\n=== CopyTo vs Clone ===");
        var source = new SimpleObject();
        var target = new SimpleObject();

        double cloneNs = benchmark("OpenClone.clone()", () -> OpenClone.clone(source));
        double copyToNs = benchmark("OpenClone.copyTo(source, target)", () -> OpenClone.copyTo(source, target));

        System.out.printf("  → CopyTo/Clone ratio: %.2fx%n", copyToNs / cloneNs);
    }

    // ==================== 10. Cache Warmup Effect ====================

    @Test
    @Order(12)
    void cacheWarmupEffect() {
        System.out.println("\n=== Cache Warmup Effect (ReflectiveCloner) ===");

        // Clear caches
        ReflectiveCloner.clearCaches();
        var obj = new NestedObject();

        // First clone (cold cache)
        long start = System.nanoTime();
        OpenClone.clone(obj);
        long coldNs = System.nanoTime() - start;

        // Subsequent clones (warm cache)
        for (int i = 0; i < WARMUP; i++) OpenClone.clone(obj);
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) OpenClone.clone(obj);
        double warmNs = (double) (System.nanoTime() - start) / ITERATIONS;

        System.out.printf("  %-50s %10d ns%n", "First clone (cold cache)", coldNs);
        System.out.printf("  %-50s %10.0f ns%n", "Subsequent clones (warm cache, avg)", warmNs);
        System.out.printf("  → Cold/Warm ratio: %.0fx%n", coldNs / warmNs);

        // Cold cache should be significantly slower due to reflection introspection
        assertThat((double) coldNs).isGreaterThan(warmNs);
    }
}
