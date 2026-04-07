package cloud.opencode.base.log.benchmark;

import cloud.opencode.base.log.CallerInfo;
import cloud.opencode.base.log.LogEvent;
import cloud.opencode.base.log.LogLevel;
import cloud.opencode.base.log.Logger;
import cloud.opencode.base.log.LoggerFactory;
import cloud.opencode.base.log.async.AsyncLogger;
import cloud.opencode.base.log.context.MDC;
import cloud.opencode.base.log.enhance.LogMasking;
import cloud.opencode.base.log.enhance.StructuredLog;
import cloud.opencode.base.log.filter.FilterAction;
import cloud.opencode.base.log.filter.LevelFilter;
import cloud.opencode.base.log.filter.LogFilterChain;
import cloud.opencode.base.log.filter.ThrottleFilter;
import cloud.opencode.base.log.level.DynamicLevelManager;
import cloud.opencode.base.log.spi.ConsoleFormatter;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance Benchmarks for opencode-base-log V1.0.3 Components
 * opencode-base-log V1.0.3 组件的性能基准测试
 *
 * <p>Lightweight nanoTime-based benchmarks that measure throughput and latency
 * of critical hot paths in the logging framework. Not JMH, but sufficient to
 * detect order-of-magnitude regressions.</p>
 * <p>基于 nanoTime 的轻量基准测试，测量日志框架中关键热路径的吞吐量和延迟。
 * 非 JMH，但足以检测数量级回归。</p>
 *
 * <p>Run with: {@code mvn test -pl opencode-base-log -Dtest="LogBenchmark"}</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
class LogBenchmark {

    private static final int WARMUP = 20_000;
    private static final int ITERATIONS = 200_000;

    /**
     * Benchmarks a named operation, printing ops/ms and ns/op.
     * 基准测试一个命名操作，打印 ops/ms 和 ns/op。
     */
    private static double benchmark(String name, Runnable op) {
        for (int i = 0; i < WARMUP; i++) op.run();

        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) op.run();
        long elapsed = System.nanoTime() - start;

        double opsPerMs = (double) ITERATIONS / (elapsed / 1_000_000.0);
        double nsPerOp = (double) elapsed / ITERATIONS;
        System.out.printf("  %-55s %10.0f ops/ms  %8.1f ns/op%n", name, opsPerMs, nsPerOp);
        return nsPerOp;
    }

    // ======================= LogEvent Construction =======================

    @Nested
    class LogEventBenchmarks {

        @Test
        void logEvent_builder_minimal() {
            System.out.println("\n=== LogEvent Builder Performance ===");

            double minimalNs = benchmark("LogEvent.builder(INFO, msg).build() [minimal]", () ->
                    LogEvent.builder(LogLevel.INFO, "test message").build());

            // Minimal event should be fast — no MDC copy, no caller info
            assertThat(minimalNs).as("Minimal LogEvent build should be < 500ns").isLessThan(500);
        }

        @Test
        void logEvent_builder_withMdc() {
            System.out.println("\n=== LogEvent Builder with MDC ===");

            Map<String, String> mdc = Map.of("requestId", "req-123", "userId", "user-456");

            double mdcNs = benchmark("LogEvent.builder().mdc(2 entries).build()", () ->
                    LogEvent.builder(LogLevel.INFO, "test")
                            .mdc(mdc)
                            .loggerName("com.example.Service")
                            .build());

            // MDC defensive copy adds allocation cost
            assertThat(mdcNs).as("LogEvent with 2 MDC entries should be < 1000ns").isLessThan(1000);
        }

        @Test
        void logEvent_builder_full() {
            System.out.println("\n=== LogEvent Builder Full ===");

            Map<String, String> mdc = Map.of("requestId", "req-123", "userId", "user-456",
                    "traceId", "trace-789");

            double fullNs = benchmark("LogEvent.builder() [full: logger+mdc+thread+ts]", () ->
                    LogEvent.builder(LogLevel.WARN, "Something happened")
                            .loggerName("com.example.MyService")
                            .mdc(mdc)
                            .threadName("main")
                            .timestamp(System.currentTimeMillis())
                            .build());

            assertThat(fullNs).as("Full LogEvent build should be < 1500ns").isLessThan(1500);
        }

        @Test
        void logEvent_toFormattedString() {
            System.out.println("\n=== LogEvent.toFormattedString() ===");

            LogEvent event = LogEvent.builder(LogLevel.INFO, "User logged in")
                    .loggerName("com.example.AuthService")
                    .build();

            double fmtNs = benchmark("LogEvent.toFormattedString() [DateTimeFormatter]", event::toFormattedString);

            // DateTimeFormatter + Instant creation is the cost center
            assertThat(fmtNs).as("Formatting should be < 2000ns").isLessThan(2000);
        }
    }

    // ======================= CallerInfo =======================

    @Nested
    class CallerInfoBenchmarks {

        @Test
        void callerInfo_capture() {
            System.out.println("\n=== CallerInfo.capture() Performance ===");

            double captureNs = benchmark("CallerInfo.capture() [StackWalker]", CallerInfo::capture);

            // StackWalker is inherently expensive — Log4j2 reports ~1-5μs
            System.out.printf("  → CallerInfo.capture() is %.1fμs per call%n", captureNs / 1000.0);
            assertThat(captureNs).as("StackWalker capture should be < 10μs").isLessThan(10_000);
        }

        @Test
        void callerInfo_toShortString() {
            System.out.println("\n=== CallerInfo.toShortString() ===");

            CallerInfo info = new CallerInfo("com.example.MyService", "process", "MyService.java", 42);

            double shortNs = benchmark("CallerInfo.toShortString()", info::toShortString);

            assertThat(shortNs).as("toShortString should be < 100ns").isLessThan(100);
        }
    }

    // ======================= Filter Chain =======================

    @Nested
    class FilterChainBenchmarks {

        @Test
        void filterChain_singleFilter() {
            System.out.println("\n=== LogFilterChain Performance ===");

            LogFilterChain chain = new LogFilterChain();
            chain.addFilter(new LevelFilter(LogLevel.INFO));

            LogEvent infoEvent = LogEvent.builder(LogLevel.INFO, "test").build();
            LogEvent debugEvent = LogEvent.builder(LogLevel.DEBUG, "test").build();

            double passNs = benchmark("FilterChain.apply(INFO) [1 filter, NEUTRAL]", () -> chain.apply(infoEvent));
            double denyNs = benchmark("FilterChain.apply(DEBUG) [1 filter, DENY]", () -> chain.apply(debugEvent));

            // Filter chain with 1 filter should be very fast — volatile read + int compare
            assertThat(passNs).as("Single filter NEUTRAL should be < 50ns").isLessThan(50);
            assertThat(denyNs).as("Single filter DENY should be < 50ns").isLessThan(50);
        }

        @Test
        void filterChain_multipleFilters() {
            System.out.println("\n=== LogFilterChain with Multiple Filters ===");

            LogFilterChain chain = new LogFilterChain();
            chain.addFilter(new LevelFilter(LogLevel.DEBUG));
            chain.addFilter(event -> FilterAction.NEUTRAL); // pass-through
            chain.addFilter(event -> FilterAction.NEUTRAL); // pass-through

            LogEvent event = LogEvent.builder(LogLevel.INFO, "test").build();

            double multiNs = benchmark("FilterChain.apply() [3 filters, all NEUTRAL]", () -> chain.apply(event));

            assertThat(multiNs).as("3-filter chain should be < 100ns").isLessThan(100);
        }
    }

    // ======================= ThrottleFilter =======================

    @Nested
    class ThrottleFilterBenchmarks {

        @Test
        void throttleFilter_distinctMessages() {
            System.out.println("\n=== ThrottleFilter Performance ===");

            ThrottleFilter filter = new ThrottleFilter(Duration.ofMillis(100));

            // Distinct messages — all pass, ConcurrentHashMap.compute
            int[] counter = {0};
            double distinctNs = benchmark("ThrottleFilter [distinct messages, NEUTRAL]", () -> {
                LogEvent event = LogEvent.builder(LogLevel.INFO, "msg-" + (counter[0]++ % 1000)).build();
                filter.filter(event);
            });

            // compute() on ConcurrentHashMap for distinct keys
            assertThat(distinctNs).as("Distinct message throttle should be < 500ns").isLessThan(500);

            filter.clearCache();
        }

        @Test
        void throttleFilter_duplicateMessages() {
            System.out.println("\n=== ThrottleFilter Duplicate Messages ===");

            ThrottleFilter filter = new ThrottleFilter(Duration.ofSeconds(60));
            LogEvent event = LogEvent.builder(LogLevel.INFO, "repeated-message").build();

            // Warm up the filter with the first call (NEUTRAL)
            filter.filter(event);

            double dupeNs = benchmark("ThrottleFilter [duplicate, DENY]", () -> filter.filter(event));

            // Duplicate detection — ConcurrentHashMap.compute + nanoTime check
            assertThat(dupeNs).as("Duplicate message DENY should be < 200ns").isLessThan(200);
        }
    }

    // ======================= DynamicLevelManager =======================

    @Nested
    class DynamicLevelBenchmarks {

        @AfterEach
        void cleanup() {
            DynamicLevelManager.getInstance().resetAll();
        }

        @Test
        void dynamicLevel_getEffective() {
            System.out.println("\n=== DynamicLevelManager Performance ===");

            DynamicLevelManager manager = DynamicLevelManager.getInstance();
            manager.setLevel("com.example.MyService", LogLevel.DEBUG);

            double overrideNs = benchmark("getEffectiveLevel() [override exists]", () ->
                    manager.getEffectiveLevel("com.example.MyService", LogLevel.INFO));

            double defaultNs = benchmark("getEffectiveLevel() [no override, default]", () ->
                    manager.getEffectiveLevel("com.example.Other", LogLevel.INFO));

            // ConcurrentHashMap.getOrDefault — should be very fast
            assertThat(overrideNs).as("Override lookup should be < 50ns").isLessThan(50);
            assertThat(defaultNs).as("Default lookup should be < 50ns").isLessThan(50);
        }
    }

    // ======================= ConsoleFormatter =======================

    @Nested
    class ConsoleFormatterBenchmarks {

        @Test
        void consoleFormatter_format() {
            System.out.println("\n=== ConsoleFormatter Performance ===");

            ConsoleFormatter plain = new ConsoleFormatter(false);
            ConsoleFormatter colored = new ConsoleFormatter(true);

            double plainNs = benchmark("ConsoleFormatter.format() [plain]", () ->
                    plain.format(LogLevel.INFO, "2026-04-03 10:00:00.123", "main",
                            "com.example.MyService", "User logged in"));

            double colorNs = benchmark("ConsoleFormatter.format() [ANSI color]", () ->
                    colored.format(LogLevel.INFO, "2026-04-03 10:00:00.123", "main",
                            "com.example.MyService", "User logged in"));

            // String concatenation cost
            assertThat(plainNs).as("Plain format should be < 200ns").isLessThan(200);
            assertThat(colorNs).as("Color format should be < 300ns").isLessThan(300);
        }
    }

    // ======================= MDC =======================

    @Nested
    class MDCBenchmarks {

        @AfterEach
        void cleanup() {
            MDC.clear();
        }

        @Test
        void mdc_putGetRemove() {
            System.out.println("\n=== MDC Performance ===");

            double putNs = benchmark("MDC.put(key, value)", () -> MDC.put("requestId", "req-123"));

            MDC.put("requestId", "req-123");
            double getNs = benchmark("MDC.get(key)", () -> MDC.get("requestId"));

            double removeNs = benchmark("MDC.remove(key)", () -> {
                MDC.put("temp", "val");
                MDC.remove("temp");
            });

            // ThreadLocal access + HashMap operation
            assertThat(putNs).as("MDC.put should be < 100ns").isLessThan(100);
            assertThat(getNs).as("MDC.get should be < 100ns").isLessThan(100);
        }

        @Test
        void mdc_getCopyOfContextMap() {
            System.out.println("\n=== MDC.getCopyOfContextMap() ===");

            MDC.put("requestId", "req-123");
            MDC.put("userId", "user-456");
            MDC.put("traceId", "trace-789");

            double copyNs = benchmark("MDC.getCopyOfContextMap() [3 entries]", MDC::getCopyOfContextMap);

            // HashMap copy — allocation cost
            assertThat(copyNs).as("MDC copy with 3 entries should be < 300ns").isLessThan(300);
        }

        @Test
        void mdc_scope() {
            System.out.println("\n=== MDC.scope() (try-with-resources) ===");

            double scopeNs = benchmark("MDC.scope(key, value) + close()", () -> {
                try (MDC.MDCScope ignored = MDC.scope("requestId", "req-123")) {
                    // scoped operation
                }
            });

            // put + get(previous) + close(remove) — 3 operations
            assertThat(scopeNs).as("MDC scope lifecycle should be < 300ns").isLessThan(300);
        }
    }

    // ======================= LogMasking =======================

    @Nested
    class LogMaskingBenchmarks {

        @Test
        void logMasking_strategies() {
            System.out.println("\n=== LogMasking Performance ===");

            double phoneNs = benchmark("LogMasking.mask(PHONE)", () ->
                    LogMasking.mask("13812345678", LogMasking.MaskingStrategy.PHONE));

            double emailNs = benchmark("LogMasking.mask(EMAIL)", () ->
                    LogMasking.mask("user@example.com", LogMasking.MaskingStrategy.EMAIL));

            double idCardNs = benchmark("LogMasking.mask(ID_CARD)", () ->
                    LogMasking.mask("110101199001011234", LogMasking.MaskingStrategy.ID_CARD));

            double nameNs = benchmark("LogMasking.mask(NAME)", () ->
                    LogMasking.mask("张三丰", LogMasking.MaskingStrategy.NAME));

            // String substring + concatenation
            assertThat(phoneNs).as("Phone masking should be < 200ns").isLessThan(200);
            assertThat(emailNs).as("Email masking should be < 200ns").isLessThan(200);
        }

        @Test
        void logMasking_fieldLookup() {
            System.out.println("\n=== LogMasking Field Lookup ===");

            LogMasking.registerRule("password", LogMasking.MaskingStrategy.PASSWORD);
            LogMasking.registerRule("phone", LogMasking.MaskingStrategy.PHONE);

            double lookupNs = benchmark("LogMasking.maskByField(registered)", () ->
                    LogMasking.maskByField("password", "secret123"));

            double missNs = benchmark("LogMasking.maskByField(unregistered)", () ->
                    LogMasking.maskByField("username", "john"));

            assertThat(lookupNs).as("Field lookup should be < 100ns").isLessThan(100);

            LogMasking.clearRules();
        }
    }

    // ======================= AsyncLogger =======================

    @Nested
    class AsyncLoggerBenchmarks {

        @Test
        void asyncLogger_enqueue_throughput() throws Exception {
            System.out.println("\n=== AsyncLogger Enqueue Throughput ===");

            Logger delegate = LoggerFactory.getLogger("benchmark.async");
            try (AsyncLogger async = AsyncLogger.wrap(delegate, 16384)) {

                // Measure enqueue throughput (not including actual log output)
                double enqueueNs = benchmark("AsyncLogger.info() [enqueue path]", () ->
                        async.info("Benchmark message id={}", 12345));

                async.flush();

                // Enqueue = MDC copy + LogTask creation + queue.offer
                System.out.printf("  → Enqueue throughput: ~%.0f ops/ms%n",
                        (double) 1_000_000 / enqueueNs);
            }
        }
    }

    // ======================= LogLevel.fromName =======================

    @Nested
    class LogLevelBenchmarks {

        @Test
        void logLevel_fromName() {
            System.out.println("\n=== LogLevel.fromName() Performance ===");

            double fromNameNs = benchmark("LogLevel.fromName(\"INFO\") [Map lookup]", () ->
                    LogLevel.fromName("INFO"));

            double fromNameLowerNs = benchmark("LogLevel.fromName(\"warn\") [toLowerCase+Map]", () ->
                    LogLevel.fromName("warn"));

            // Map lookup O(1) + toUpperCase() string allocation
            assertThat(fromNameNs).as("fromName should be < 300ns").isLessThan(300);
        }
    }

    // ======================= StructuredLog.escapeJson =======================

    @Nested
    class StructuredLogBenchmarks {

        @Test
        void structuredLog_escapeJson() {
            System.out.println("\n=== StructuredLog JSON Escape Performance ===");

            // Normal text (no escaping needed — fast path)
            double normalNs = benchmark("StructuredLog.info().field().log(logger) [normal]", () ->
                    StructuredLog.info()
                            .message("User logged in successfully")
                            .field("userId", "user-123")
                            .field("duration", 150)
                            .log(LoggerFactory.getLogger("benchmark.structured")));

            // Includes StackWalker call in log() + full message formatting + console output
            assertThat(normalNs).as("Structured log normal should be < 10000ns").isLessThan(10_000);
        }
    }

    // ======================= Comparison Summary =======================

    @Nested
    class ComparisonSummary {

        @AfterEach
        void cleanup() {
            MDC.clear();
            DynamicLevelManager.getInstance().resetAll();
        }

        @Test
        void hotPath_fullLoggingPipeline() {
            System.out.println("\n=== Full Logging Pipeline (Hot Path Simulation) ===");

            LogFilterChain chain = new LogFilterChain();
            chain.addFilter(new LevelFilter(LogLevel.DEBUG));

            DynamicLevelManager manager = DynamicLevelManager.getInstance();
            manager.setLevel("benchmark.pipeline", LogLevel.DEBUG);

            // Simulate: level check → filter chain → LogEvent build → format
            double pipelineNs = benchmark("Full pipeline [level→filter→event→format]", () -> {
                // 1. Dynamic level check
                LogLevel effective = manager.getEffectiveLevel("benchmark.pipeline", LogLevel.INFO);

                // 2. Build event
                LogEvent event = LogEvent.builder(effective, "Pipeline benchmark message")
                        .loggerName("benchmark.pipeline")
                        .build();

                // 3. Filter chain
                FilterAction action = chain.apply(event);

                // 4. Format output
                if (action != FilterAction.DENY) {
                    event.toFormattedString();
                }
            });

            System.out.printf("  → Full pipeline: %.1fμs per log call%n", pipelineNs / 1000.0);
            // Full pipeline should be dominated by LogEvent allocation + DateTimeFormatter
            assertThat(pipelineNs).as("Full pipeline should be < 5μs").isLessThan(5000);
        }
    }

    // ======================= Concurrent Performance =======================

    private static final int THREAD_COUNT = 8;
    private static final int OPS_PER_THREAD = 100_000;

    /**
     * Runs a concurrent benchmark with N threads, each performing opsPerThread operations.
     * Returns total ops/ms across all threads.
     */
    private static double concurrentBenchmark(String name, Runnable op) throws InterruptedException {
        // Warmup (single-threaded)
        for (int i = 0; i < 10_000; i++) op.run();

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(THREAD_COUNT);
        LongAdder totalOps = new LongAdder();

        for (int t = 0; t < THREAD_COUNT; t++) {
            Thread.ofVirtual().name("bench-" + t).start(() -> {
                try {
                    startGate.await();
                    for (int i = 0; i < OPS_PER_THREAD; i++) {
                        op.run();
                    }
                    totalOps.add(OPS_PER_THREAD);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endGate.countDown();
                }
            });
        }

        long start = System.nanoTime();
        startGate.countDown();
        endGate.await();
        long elapsed = System.nanoTime() - start;

        long total = totalOps.sum();
        double opsPerMs = (double) total / (elapsed / 1_000_000.0);
        double nsPerOp = (double) elapsed / total;
        System.out.printf("  %-55s %10.0f ops/ms  %8.1f ns/op  [%d threads × %dk ops]%n",
                name, opsPerMs, nsPerOp, THREAD_COUNT, OPS_PER_THREAD / 1000);
        return opsPerMs;
    }

    @Nested
    class ConcurrentBenchmarks {

        @AfterEach
        void cleanup() {
            MDC.clear();
            DynamicLevelManager.getInstance().resetAll();
        }

        @Test
        void concurrent_filterChain() throws InterruptedException {
            System.out.println("\n=== Concurrent FilterChain (COW volatile read) ===");

            LogFilterChain chain = new LogFilterChain();
            chain.addFilter(new LevelFilter(LogLevel.DEBUG));
            LogEvent event = LogEvent.builder(LogLevel.INFO, "concurrent test").build();

            double singleOps = 1_000_000.0 / benchmark("  [baseline] single-thread FilterChain", () -> chain.apply(event));
            double concOps = concurrentBenchmark("[concurrent] FilterChain.apply() × " + THREAD_COUNT, () -> chain.apply(event));

            double scalability = concOps / singleOps;
            System.out.printf("  → Scalability: %.1fx on %d threads (ideal: %dx)%n", scalability, THREAD_COUNT, THREAD_COUNT);
            // Virtual threads share carrier threads; concurrency test validates correctness
            // and absence of lock contention, not CPU parallelism
            assertThat(concOps).as("Concurrent FilterChain should achieve > 5000 ops/ms").isGreaterThan(5000);
        }

        @Test
        void concurrent_throttleFilter() throws InterruptedException {
            System.out.println("\n=== Concurrent ThrottleFilter (ConcurrentHashMap.compute) ===");

            ThrottleFilter filter = new ThrottleFilter(Duration.ofMillis(100));
            AtomicLong counter = new AtomicLong(0);

            double opsPerMs = concurrentBenchmark("[concurrent] ThrottleFilter distinct msgs × " + THREAD_COUNT, () -> {
                LogEvent event = LogEvent.builder(LogLevel.INFO, "msg-" + counter.incrementAndGet()).build();
                filter.filter(event);
            });

            System.out.printf("  → Concurrent throttle throughput: %.0f ops/ms%n", opsPerMs);
            // ConcurrentHashMap.compute should handle concurrent access well
            assertThat(opsPerMs).as("Concurrent throttle should achieve > 500 ops/ms").isGreaterThan(500);

            filter.clearCache();
        }

        @Test
        void concurrent_dynamicLevelManager() throws InterruptedException {
            System.out.println("\n=== Concurrent DynamicLevelManager (ConcurrentHashMap.getOrDefault) ===");

            DynamicLevelManager manager = DynamicLevelManager.getInstance();
            manager.setLevel("bench.concurrent", LogLevel.DEBUG);

            double singleOps = 1_000_000.0 / benchmark("  [baseline] single-thread getEffectiveLevel", () ->
                    manager.getEffectiveLevel("bench.concurrent", LogLevel.INFO));

            double concOps = concurrentBenchmark("[concurrent] getEffectiveLevel × " + THREAD_COUNT, () ->
                    manager.getEffectiveLevel("bench.concurrent", LogLevel.INFO));

            double scalability = concOps / singleOps;
            System.out.printf("  → Scalability: %.1fx on %d threads (ideal: %dx)%n", scalability, THREAD_COUNT, THREAD_COUNT);
            // Virtual threads share carriers; validates no lock contention
            assertThat(concOps).as("Concurrent getEffectiveLevel should achieve > 10000 ops/ms").isGreaterThan(10000);
        }

        @Test
        void concurrent_mdcIsolation() throws InterruptedException {
            System.out.println("\n=== Concurrent MDC (ThreadLocal isolation) ===");

            double concOps = concurrentBenchmark("[concurrent] MDC.put+get+remove × " + THREAD_COUNT, () -> {
                MDC.put("requestId", "req-" + Thread.currentThread().getName());
                MDC.get("requestId");
                MDC.remove("requestId");
            });

            System.out.printf("  → Concurrent MDC throughput: %.0f ops/ms%n", concOps);
            // ThreadLocal should have zero contention — scales linearly
            assertThat(concOps).as("Concurrent MDC should achieve > 5000 ops/ms").isGreaterThan(5000);
        }

        @Test
        void concurrent_logEventBuild() throws InterruptedException {
            System.out.println("\n=== Concurrent LogEvent.build() (allocation pressure) ===");

            Map<String, String> mdc = Map.of("requestId", "req-123", "userId", "user-456");

            double concOps = concurrentBenchmark("[concurrent] LogEvent.builder().mdc().build() × " + THREAD_COUNT, () ->
                    LogEvent.builder(LogLevel.INFO, "concurrent event")
                            .loggerName("bench.concurrent")
                            .mdc(mdc)
                            .build());

            System.out.printf("  → Concurrent LogEvent build throughput: %.0f ops/ms%n", concOps);
            // Record allocation + HashMap copy — measures GC pressure under contention
            assertThat(concOps).as("Concurrent LogEvent build should achieve > 2000 ops/ms").isGreaterThan(2000);
        }

        @Test
        void concurrent_asyncLogger() throws Exception {
            System.out.println("\n=== Concurrent AsyncLogger Enqueue (LinkedBlockingQueue contention) ===");

            Logger delegate = LoggerFactory.getLogger("benchmark.concurrent.async");
            try (AsyncLogger async = AsyncLogger.wrap(delegate, 65536)) {

                double concOps = concurrentBenchmark("[concurrent] AsyncLogger.info() × " + THREAD_COUNT, () ->
                        async.info("Concurrent async message id={}", 12345));

                async.flush();
                System.out.printf("  → Concurrent async enqueue: %.0f ops/ms%n", concOps);
                // LinkedBlockingQueue has lock contention — measure how bad
                assertThat(concOps).as("Concurrent async enqueue should achieve > 100 ops/ms").isGreaterThan(100);
            }
        }
    }
}
