package cloud.opencode.base.core.benchmark;

import cloud.opencode.base.core.Lazy;
import cloud.opencode.base.core.Joiner;
import cloud.opencode.base.core.Suppliers;
import cloud.opencode.base.core.bean.OpenBean;
import cloud.opencode.base.core.collect.OpenCollections;
import cloud.opencode.base.core.convert.Convert;
import cloud.opencode.base.core.concurrent.VirtualTasks;
import cloud.opencode.base.core.primitives.Ints;
import cloud.opencode.base.core.reflect.ReflectUtil;
import cloud.opencode.base.core.result.Result;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance benchmarks for opencode-base-core v1.0.3 new components.
 * 使用 nanoTime 循环的轻量基准测试（非 JMH，但足以发现量级问题）。
 *
 * <p>Run with: mvn test -pl opencode-base-core -Dtest="CoreBenchmark"</p>
 *
 * <p>Each benchmark uses warmup + measurement phases to reduce JIT noise.</p>
 *
 * @author Leon Soo
 */
class CoreBenchmark {

    private static final int WARMUP = 50_000;
    private static final int ITERATIONS = 500_000;

    /** Measure ops/ms for a given operation */
    private static double benchmark(String name, Runnable op) {
        // Warmup
        for (int i = 0; i < WARMUP; i++) op.run();

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) op.run();
        long elapsed = System.nanoTime() - start;

        double opsPerMs = (double) ITERATIONS / (elapsed / 1_000_000.0);
        double nsPerOp = (double) elapsed / ITERATIONS;
        System.out.printf("  %-45s %10.0f ops/ms  %8.1f ns/op%n", name, opsPerMs, nsPerOp);
        return nsPerOp;
    }

    // ======================= Lazy vs Suppliers.memoize =======================

    @Nested
    class LazyBenchmarks {

        @Test
        void lazyGet_afterInit_vs_volatileRead() {
            System.out.println("\n=== Lazy.get() (post-init) vs Suppliers.memoize() ===");

            // Baseline: direct volatile read
            var lazy = Lazy.of((Supplier<String>) () -> "hello");
            lazy.get(); // init

            @SuppressWarnings("deprecation")
            Supplier<String> memo = Suppliers.memoize((Supplier<String>) () -> "hello");
            memo.get(); // init

            double lazyNs = benchmark("Lazy.get() [post-init, VarHandle]", () -> lazy.get());
            double memoNs = benchmark("Suppliers.memoize().get() [post-init, sync]", () -> memo.get());

            System.out.printf("  → Lazy / memoize ratio: %.2fx%n", lazyNs / memoNs);
            // Both should be < 10ns (volatile read fast path)
            assertThat(lazyNs).isLessThan(50); // guard: not absurdly slow
        }

        @Test
        void lazyGet_concurrent_virtualThreads() throws Exception {
            System.out.println("\n=== Lazy.get() concurrent (1000 virtual threads) ===");

            var counter = new java.util.concurrent.atomic.AtomicInteger();
            var lazy = Lazy.of((Supplier<Integer>) counter::incrementAndGet);

            long start = System.nanoTime();
            try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
                var futures = IntStream.range(0, 1000)
                        .mapToObj(i -> exec.submit(lazy::get))
                        .toList();
                for (var f : futures) f.get();
            }
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            assertThat(counter.get()).isEqualTo(1);
            System.out.printf("  1000 virtual threads concurrent get(): %d ms, computed once%n", elapsedMs);
            assertThat(elapsedMs).isLessThan(5000); // should be < 1s typically
        }
    }

    // ======================= Result chain =======================

    @Nested
    class ResultBenchmarks {

        @Test
        void resultChain_vs_tryCatch() {
            System.out.println("\n=== Result chain vs try-catch ===");

            double resultNs = benchmark("Result.of → map → map → getOrElse", () -> {
                String r = Result.of(() -> "hello")
                        .map(s -> s + " world")
                        .map(String::toUpperCase)
                        .getOrElse("fallback");
            });

            double tryCatchNs = benchmark("try-catch equivalent", () -> {
                String r;
                try {
                    String s = "hello";
                    s = s + " world";
                    r = s.toUpperCase();
                } catch (Exception e) {
                    r = "fallback";
                }
            });

            double ratio = resultNs / tryCatchNs;
            System.out.printf("  → Result / try-catch ratio: %.2fx%n", ratio);
            // Result chain has object allocation overhead, expect 2-5x
            assertThat(ratio).isLessThan(10); // guard: not more than 10x
        }

        @Test
        void failureShortCircuit() {
            System.out.println("\n=== Result.Failure short-circuit (no allocation on map) ===");
            var failure = Result.failure(new RuntimeException("test"));

            double ns = benchmark("Failure.map().map().map().getOrElse()", () -> {
                String r = failure
                        .<String>map(x -> x + "a")
                        .map(x -> x + "b")
                        .map(x -> x + "c")
                        .getOrElse("default");
            });

            // Failure short-circuits — should be near-zero cost (just casts)
            assertThat(ns).isLessThan(50);
        }
    }

    // ======================= Convert =======================

    @Nested
    class ConvertBenchmarks {

        @Test
        void convertToInt_vs_parseInt() {
            System.out.println("\n=== Convert.toInt() vs Integer.parseInt() ===");

            double convertNs = benchmark("Convert.toInt(\"12345\")", () -> Convert.toInt("12345"));
            double parseNs = benchmark("Integer.parseInt(\"12345\")", () -> Integer.parseInt("12345"));

            double ratio = convertNs / parseNs;
            System.out.printf("  → Convert / parseInt ratio: %.2fx%n", ratio);
            assertThat(ratio).isLessThan(20); // Convert has registry lookup overhead; see perf report
        }
    }

    // ======================= Joiner =======================

    @Nested
    class JoinerBenchmarks {

        @Test
        void joiner_vs_stringJoin() {
            System.out.println("\n=== Joiner.join() vs String.join() ===");
            var items = List.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j");

            double joinerNs = benchmark("Joiner.on(\",\").join(10 items)", () ->
                    Joiner.on(",").join(items));

            double jdkNs = benchmark("String.join(\",\", 10 items)", () ->
                    String.join(",", items));

            double ratio = joinerNs / jdkNs;
            System.out.printf("  → Joiner / String.join ratio: %.2fx%n", ratio);
            assertThat(ratio).isLessThan(3); // target ≤ 1.2x per spec
        }
    }

    // ======================= Ints.indexOf =======================

    @Nested
    class PrimitivesBenchmarks {

        @Test
        void intsIndexOf_vs_loop() {
            System.out.println("\n=== Ints.indexOf() vs manual loop ===");
            int[] array = IntStream.range(0, 1000).toArray();
            int target = 999; // worst case: last element

            double intsNs = benchmark("Ints.indexOf(1000 elements, last)", () ->
                    Ints.indexOf(array, target));

            double loopNs = benchmark("Manual for-loop indexOf", () -> {
                int idx = -1;
                for (int i = 0; i < array.length; i++) {
                    if (array[i] == target) { idx = i; break; }
                }
            });

            double ratio = intsNs / loopNs;
            System.out.printf("  → Ints / loop ratio: %.2fx%n", ratio);
            assertThat(ratio).isLessThan(3); // target ≤ 1.1x per spec
        }
    }

    // ======================= OpenCollections =======================

    @Nested
    class CollectionsBenchmarks {

        @Test
        void appendPerformance() {
            System.out.println("\n=== OpenCollections.append() cost ===");
            var base = List.of("a", "b", "c", "d", "e");

            double appendNs = benchmark("OpenCollections.append(5-elem list)", () ->
                    OpenCollections.append(base, "f"));

            double manualNs = benchmark("ArrayList copy + add + unmodifiable", () -> {
                var copy = new ArrayList<>(base);
                copy.add("f");
                java.util.Collections.unmodifiableList(copy);
            });

            double ratio = appendNs / manualNs;
            System.out.printf("  → OpenCollections / manual ratio: %.2fx%n", ratio);
            assertThat(ratio).isLessThan(3); // should be ~1x (same implementation)
        }

        @Test
        void listBuilderPerformance() {
            System.out.println("\n=== ListBuilder vs ArrayList + List.copyOf() ===");

            double builderNs = benchmark("ListBuilder.add() x10 + build()", () ->
                    OpenCollections.<String>listBuilder(10)
                            .add("a").add("b").add("c").add("d").add("e")
                            .add("f").add("g").add("h").add("i").add("j")
                            .build());

            double manualNs = benchmark("new ArrayList + add x10 + List.copyOf()", () -> {
                var list = new ArrayList<String>(10);
                list.add("a"); list.add("b"); list.add("c"); list.add("d"); list.add("e");
                list.add("f"); list.add("g"); list.add("h"); list.add("i"); list.add("j");
                List.copyOf(list);
            });

            double ratio = builderNs / manualNs;
            System.out.printf("  → ListBuilder / manual ratio: %.2fx%n", ratio);
            assertThat(ratio).isLessThan(3);
        }
    }

    // ======================= VirtualTasks =======================

    @Nested
    class VirtualTasksBenchmarks {

        @Test
        void invokeAll_overhead() throws Exception {
            System.out.println("\n=== VirtualTasks.invokeAll() overhead vs raw executor ===");
            List<Callable<Integer>> tasks = IntStream.range(0, 10)
                    .mapToObj(i -> (Callable<Integer>) () -> i * 2)
                    .toList();

            // Warmup
            for (int i = 0; i < 100; i++) VirtualTasks.invokeAll(tasks);

            int iters = 1000;
            long start = System.nanoTime();
            for (int i = 0; i < iters; i++) VirtualTasks.invokeAll(tasks);
            double vtNs = (double) (System.nanoTime() - start) / iters;

            start = System.nanoTime();
            for (int i = 0; i < iters; i++) {
                try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
                    var futures = tasks.stream().map(exec::submit).toList();
                    for (var f : futures) f.get();
                }
            }
            double rawNs = (double) (System.nanoTime() - start) / iters;

            double ratio = vtNs / rawNs;
            System.out.printf("  VirtualTasks.invokeAll(10):    %10.0f ns/call%n", vtNs);
            System.out.printf("  Raw executor (10 tasks):       %10.0f ns/call%n", rawNs);
            System.out.printf("  → VirtualTasks / raw ratio:    %.2fx%n", ratio);
            assertThat(ratio).isLessThan(3); // target ≤ 1.2x
        }
    }

    // ======================= Summary =======================

    @Test
    void printSummaryHeader() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  OpenCode-Base-Core v1.0.3 Performance Benchmark");
        System.out.println("  JDK: " + Runtime.version());
        System.out.println("  OS:  " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        System.out.println("  CPU: " + Runtime.getRuntime().availableProcessors() + " cores");
        System.out.println("  Warmup: " + WARMUP + " iterations, Measure: " + ITERATIONS + " iterations");
        System.out.println("=".repeat(70));
    }
}
