package cloud.opencode.base.rules.benchmark;

import cloud.opencode.base.rules.*;
import cloud.opencode.base.rules.engine.DefaultRuleEngine;
import cloud.opencode.base.rules.model.Action;
import cloud.opencode.base.rules.model.Condition;
import cloud.opencode.base.rules.key.TypedKey;
import cloud.opencode.base.rules.metric.MetricsListener;
import cloud.opencode.base.rules.metric.RuleMetrics;
import cloud.opencode.base.rules.score.AggregationStrategy;
import cloud.opencode.base.rules.score.ScoreResult;
import cloud.opencode.base.rules.score.ScoringEngine;
import cloud.opencode.base.rules.score.ScoringRule;
import cloud.opencode.base.rules.trace.ExecutionTrace;
import cloud.opencode.base.rules.validation.RuleValidator;
import cloud.opencode.base.rules.validation.ValidationReport;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance Benchmarks for OpenCode Rules Engine
 * OpenCode 规则引擎性能基准测试
 *
 * <p>Measures throughput and latency of core rule engine operations including
 * fire, fireFirst, fireAndTrace, scoring, validation and TypedKey access.</p>
 * <p>测量核心规则引擎操作的吞吐量和延迟，包括 fire、fireFirst、fireAndTrace、
 * 评分、校验和 TypedKey 访问。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.3
 */
@DisplayName("Rules Engine 性能基准测试")
@Tag("benchmark")
class RuleEngineBenchmark {

    private static final int WARMUP_OPS = 50_000;
    private static final int MEASURE_OPS = 200_000;

    private record BenchResult(String name, long ops, long totalNanos) {
        double opsPerMs() { return ops * 1_000_000.0 / totalNanos; }
        double nsPerOp() { return (double) totalNanos / ops; }

        @Override
        public String toString() {
            return String.format("%-55s %,10d ops  %,.0f ops/ms  %,.0f ns/op",
                    name, ops, opsPerMs(), nsPerOp());
        }
    }

    private final List<BenchResult> results = new ArrayList<>();

    @AfterEach
    void printResult() {
        results.forEach(r -> System.out.println("  BENCH │ " + r));
        results.clear();
    }

    private BenchResult bench(String name, int warmup, int measure, Runnable op) {
        for (int i = 0; i < warmup; i++) op.run();
        long start = System.nanoTime();
        for (int i = 0; i < measure; i++) op.run();
        long elapsed = System.nanoTime() - start;
        BenchResult result = new BenchResult(name, measure, elapsed);
        results.add(result);
        return result;
    }

    // ==================== Engine Setup ====================

    private static Rule createRule(String name, int priority, boolean fires) {
        return OpenRules.rule(name)
                .priority(priority)
                .when((Condition) ctx -> fires)
                .then((Action) ctx -> ctx.setResult(name, true))
                .build();
    }

    private static Rule createTerminalRule(String name, int priority) {
        return OpenRules.rule(name)
                .priority(priority)
                .when((Condition) ctx -> true)
                .then((Action) ctx -> ctx.setResult(name, true))
                .terminal()
                .build();
    }

    // ==================== Core fire() Benchmarks ====================

    @Nested
    @DisplayName("Core fire() Benchmarks | 核心 fire() 基准测试")
    class CoreFireBenchmarks {

        @Test
        @DisplayName("fire() with 10 rules, all match | 10条规则全部匹配")
        void fire10RulesAllMatch() {
            RuleEngine engine = new DefaultRuleEngine();
            for (int i = 0; i < 10; i++) {
                engine.register(createRule("rule-" + i, i, true));
            }
            BenchResult r = bench("fire(10 rules, all match)", WARMUP_OPS, MEASURE_OPS, () -> {
                RuleContext ctx = RuleContext.create();
                engine.fire(ctx);
            });
            assertThat(r.opsPerMs()).isGreaterThan(10); // sanity: >10k ops/sec
        }

        @Test
        @DisplayName("fire() with 100 rules, all match | 100条规则全部匹配")
        void fire100RulesAllMatch() {
            RuleEngine engine = new DefaultRuleEngine();
            for (int i = 0; i < 100; i++) {
                engine.register(createRule("rule-" + i, i, true));
            }
            bench("fire(100 rules, all match)", WARMUP_OPS, MEASURE_OPS, () -> {
                RuleContext ctx = RuleContext.create();
                engine.fire(ctx);
            });
        }

        @Test
        @DisplayName("fire() with 10 rules, none match | 10条规则全不匹配")
        void fire10RulesNoneMatch() {
            RuleEngine engine = new DefaultRuleEngine();
            for (int i = 0; i < 10; i++) {
                engine.register(createRule("rule-" + i, i, false));
            }
            bench("fire(10 rules, none match)", WARMUP_OPS, MEASURE_OPS, () -> {
                RuleContext ctx = RuleContext.create();
                engine.fire(ctx);
            });
        }

        @Test
        @DisplayName("fireFirst() with 10 rules | 首条匹配模式")
        void fireFirst10Rules() {
            RuleEngine engine = new DefaultRuleEngine();
            for (int i = 0; i < 10; i++) {
                engine.register(createRule("rule-" + i, i, true));
            }
            bench("fireFirst(10 rules)", WARMUP_OPS, MEASURE_OPS, () -> {
                RuleContext ctx = RuleContext.create();
                engine.fireFirst(ctx);
            });
        }
    }

    // ==================== Terminal/Halt Benchmarks ====================

    @Nested
    @DisplayName("Terminal/Halt Benchmarks | 终止/停止基准测试")
    class TerminalHaltBenchmarks {

        @Test
        @DisplayName("terminal rule stops at rule 3 of 10 | 终止规则在第3条停止")
        void terminalStopsEarly() {
            RuleEngine engine = new DefaultRuleEngine();
            for (int i = 0; i < 10; i++) {
                if (i == 2) {
                    engine.register(createTerminalRule("rule-" + i, i));
                } else {
                    engine.register(createRule("rule-" + i, i, true));
                }
            }
            bench("fire(10 rules, terminal@3)", WARMUP_OPS, MEASURE_OPS, () -> {
                RuleContext ctx = RuleContext.create();
                engine.fire(ctx);
            });
        }

        @Test
        @DisplayName("haltWhen stops at rule 3 of 10 | 停止条件在第3条生效")
        void haltWhenStopsEarly() {
            DefaultRuleEngine engine = new DefaultRuleEngine();
            for (int i = 0; i < 10; i++) {
                engine.register(createRule("rule-" + i, i, true));
            }
            engine.setHaltCondition(ctx -> ctx.getResult("rule-2") != null);
            bench("fire(10 rules, haltWhen@3)", WARMUP_OPS, MEASURE_OPS, () -> {
                RuleContext ctx = RuleContext.create();
                engine.fire(ctx);
            });
        }
    }

    // ==================== TypedKey Benchmarks ====================

    @Nested
    @DisplayName("TypedKey Benchmarks | TypedKey 基准测试")
    class TypedKeyBenchmarks {

        private static final TypedKey<String> TYPE_KEY = TypedKey.of("type", String.class);
        private static final TypedKey<Double> AMOUNT_KEY = TypedKey.of("amount", Double.class);

        @Test
        @DisplayName("TypedKey get/put vs String get/put | TypedKey vs String 性能对比")
        void typedKeyVsString() {
            RuleContext ctx = RuleContext.create();
            ctx.put("type", "VIP");
            ctx.put("amount", 100.0);
            ctx.put(TYPE_KEY, "VIP");
            ctx.put(AMOUNT_KEY, 100.0);

            bench("String get() × 2", WARMUP_OPS, MEASURE_OPS, () -> {
                String t = ctx.get("type");
                Double a = ctx.get("amount");
            });

            bench("TypedKey get() × 2", WARMUP_OPS, MEASURE_OPS, () -> {
                String t = ctx.get(TYPE_KEY);
                Double a = ctx.get(AMOUNT_KEY);
            });
        }
    }

    // ==================== Trace Benchmarks ====================

    @Nested
    @DisplayName("Trace Benchmarks | 追踪基准测试")
    class TraceBenchmarks {

        @Test
        @DisplayName("fireAndTrace() vs fire() overhead | 追踪开销对比")
        void fireAndTraceOverhead() {
            RuleEngine engine = new DefaultRuleEngine();
            for (int i = 0; i < 10; i++) {
                engine.register(createRule("rule-" + i, i, i % 2 == 0));
            }

            BenchResult plain = bench("fire(10 rules, 5 match)", WARMUP_OPS, MEASURE_OPS, () -> {
                RuleContext ctx = RuleContext.create();
                engine.fire(ctx);
            });

            BenchResult traced = bench("fireAndTrace(10 rules, 5 match)", WARMUP_OPS, MEASURE_OPS, () -> {
                RuleContext ctx = RuleContext.create();
                ExecutionTrace trace = engine.fireAndTrace(ctx);
            });

            double overhead = traced.nsPerOp() / plain.nsPerOp();
            System.out.printf("  BENCH │ Trace overhead ratio: %.2fx%n", overhead);
        }
    }

    // ==================== Metrics Benchmarks ====================

    @Nested
    @DisplayName("Metrics Benchmarks | 指标基准测试")
    class MetricsBenchmarks {

        @Test
        @DisplayName("fire() with MetricsListener overhead | MetricsListener 开销")
        void metricsListenerOverhead() {
            RuleEngine enginePlain = new DefaultRuleEngine();
            RuleEngine engineMetrics = new DefaultRuleEngine();
            RuleMetrics metrics = new RuleMetrics();
            engineMetrics.addListener(new MetricsListener(metrics));

            for (int i = 0; i < 10; i++) {
                Rule rule = createRule("rule-" + i, i, true);
                enginePlain.register(rule);
                engineMetrics.register(rule);
            }

            BenchResult plain = bench("fire(10 rules, no listener)", WARMUP_OPS, MEASURE_OPS, () -> {
                RuleContext ctx = RuleContext.create();
                enginePlain.fire(ctx);
            });

            BenchResult withMetrics = bench("fire(10 rules, MetricsListener)", WARMUP_OPS, MEASURE_OPS, () -> {
                RuleContext ctx = RuleContext.create();
                engineMetrics.fire(ctx);
            });

            double overhead = withMetrics.nsPerOp() / plain.nsPerOp();
            System.out.printf("  BENCH │ MetricsListener overhead ratio: %.2fx%n", overhead);
        }
    }

    // ==================== Scoring Benchmarks ====================

    @Nested
    @DisplayName("Scoring Benchmarks | 评分基准测试")
    class ScoringBenchmarks {

        @Test
        @DisplayName("ScoringEngine with 10 rules | 10条评分规则")
        void score10Rules() {
            List<ScoringRule> scoringRules = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                final int idx = i;
                scoringRules.add(new ScoringRule() {
                    public String getName() { return "score-" + idx; }
                    public String getDescription() { return null; }
                    public int getPriority() { return idx; }
                    public boolean evaluate(RuleContext ctx) { return true; }
                    public void execute(RuleContext ctx) {}
                    public double score(RuleContext ctx) { return idx * 0.1; }
                    public double weight() { return 1.0; }
                });
            }

            bench("ScoringEngine.score(10 rules, SUM)", WARMUP_OPS, MEASURE_OPS, () -> {
                RuleContext ctx = RuleContext.create();
                ScoreResult r = ScoringEngine.score(ctx, scoringRules, AggregationStrategy.SUM);
            });

            bench("ScoringEngine.score(10 rules, WEIGHTED_SUM)", WARMUP_OPS, MEASURE_OPS, () -> {
                RuleContext ctx = RuleContext.create();
                ScoreResult r = ScoringEngine.score(ctx, scoringRules, AggregationStrategy.WEIGHTED_SUM);
            });
        }
    }

    // ==================== Validation Benchmarks ====================

    @Nested
    @DisplayName("Validation Benchmarks | 校验基准测试")
    class ValidationBenchmarks {

        @Test
        @DisplayName("RuleValidator.validate() with 100 rules | 校验100条规则")
        void validate100Rules() {
            List<Rule> ruleList = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                ruleList.add(createRule("rule-" + i, i, true));
            }

            bench("RuleValidator.validate(100 rules)", WARMUP_OPS, MEASURE_OPS, () -> {
                ValidationReport report = RuleValidator.validate(ruleList);
            });
        }
    }

    // ==================== Cache Hit Benchmark ====================

    @Nested
    @DisplayName("Cache Benchmarks | 缓存基准测试")
    class CacheBenchmarks {

        @Test
        @DisplayName("fire() repeated calls benefit from sorted-rules cache | 重复fire()受益于排序缓存")
        void repeatedFireCacheHit() {
            RuleEngine engine = new DefaultRuleEngine();
            for (int i = 0; i < 50; i++) {
                engine.register(createRule("rule-" + i, 50 - i, true));
            }

            bench("fire(50 rules, cached sort)", WARMUP_OPS, MEASURE_OPS, () -> {
                RuleContext ctx = RuleContext.create();
                engine.fire(ctx);
            });
        }
    }
}
