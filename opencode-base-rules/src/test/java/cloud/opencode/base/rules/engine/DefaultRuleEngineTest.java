package cloud.opencode.base.rules.engine;

import cloud.opencode.base.rules.*;
import cloud.opencode.base.rules.conflict.OrderConflictResolver;
import cloud.opencode.base.rules.conflict.PriorityConflictResolver;
import cloud.opencode.base.rules.listener.RuleListener;
import cloud.opencode.base.rules.model.Condition;
import cloud.opencode.base.rules.model.RuleGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultRuleEngine Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("DefaultRuleEngine Tests")
class DefaultRuleEngineTest {

    private DefaultRuleEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DefaultRuleEngine();
    }

    @Nested
    @DisplayName("register() Tests")
    class RegisterTests {

        @Test
        @DisplayName("register(Rule...) should register rules")
        void registerShouldRegisterRules() {
            Rule rule1 = createRule("rule1", 100, true, ctx -> true);
            Rule rule2 = createRule("rule2", 100, true, ctx -> true);

            engine.register(rule1, rule2);

            assertThat(engine.getRuleCount()).isEqualTo(2);
            assertThat(engine.hasRule("rule1")).isTrue();
            assertThat(engine.hasRule("rule2")).isTrue();
        }

        @Test
        @DisplayName("register(Rule...) should return engine for chaining")
        void registerShouldReturnEngineForChaining() {
            Rule rule = createRule("rule", 100, true, ctx -> true);
            RuleEngine result = engine.register(rule);

            assertThat(result).isSameAs(engine);
        }

        @Test
        @DisplayName("register(RuleGroup) should register group rules")
        void registerGroupShouldRegisterGroupRules() {
            Rule rule1 = createRule("rule1", 100, true, ctx -> true);
            Rule rule2 = createRule("rule2", 100, true, ctx -> true);
            RuleGroup group = RuleGroup.builder("group")
                    .addRules(rule1, rule2)
                    .build();

            engine.register(group);

            assertThat(engine.getRuleCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("register() should overwrite rule with same name")
        void registerShouldOverwriteRuleWithSameName() {
            Rule rule1 = createRule("rule", 100, true, ctx -> true);
            Rule rule2 = createRule("rule", 50, true, ctx -> false);

            engine.register(rule1);
            engine.register(rule2);

            assertThat(engine.getRuleCount()).isEqualTo(1);
            assertThat(engine.getRule("rule").getPriority()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("unregister() Tests")
    class UnregisterTests {

        @Test
        @DisplayName("unregister() should remove rule by name")
        void unregisterShouldRemoveRuleByName() {
            Rule rule = createRule("rule", 100, true, ctx -> true);
            engine.register(rule);
            engine.unregister("rule");

            assertThat(engine.hasRule("rule")).isFalse();
        }

        @Test
        @DisplayName("unregister() should return engine for chaining")
        void unregisterShouldReturnEngineForChaining() {
            RuleEngine result = engine.unregister("nonexistent");
            assertThat(result).isSameAs(engine);
        }
    }

    @Nested
    @DisplayName("fire() Tests")
    class FireTests {

        @Test
        @DisplayName("fire() should execute matching rules")
        void fireShouldExecuteMatchingRules() {
            Rule rule = createRule("rule", 100, true, ctx -> true);
            engine.register(rule);

            RuleContext context = RuleContext.create();
            RuleResult result = engine.fire(context);

            assertThat(result.success()).isTrue();
            assertThat(result.wasFired("rule")).isTrue();
        }

        @Test
        @DisplayName("fire() should skip non-matching rules")
        void fireShouldSkipNonMatchingRules() {
            Rule rule = createRule("rule", 100, true, ctx -> false);
            engine.register(rule);

            RuleContext context = RuleContext.create();
            RuleResult result = engine.fire(context);

            assertThat(result.wasSkipped("rule")).isTrue();
        }

        @Test
        @DisplayName("fire() should skip disabled rules")
        void fireShouldSkipDisabledRules() {
            Rule rule = createRule("rule", 100, false, ctx -> true);
            engine.register(rule);

            RuleContext context = RuleContext.create();
            RuleResult result = engine.fire(context);

            assertThat(result.wasSkipped("rule")).isTrue();
        }

        @Test
        @DisplayName("fire(context, group) should only fire group rules")
        void fireWithGroupShouldOnlyFireGroupRules() {
            Rule groupRule = new DefaultRule("group-rule", null, 100, "group1", true,
                    ctx -> true, ctx -> ctx.setResult("group-executed", true));
            Rule otherRule = new DefaultRule("other-rule", null, 100, "group2", true,
                    ctx -> true, ctx -> ctx.setResult("other-executed", true));

            engine.register(groupRule, otherRule);
            RuleContext context = RuleContext.create();
            RuleResult result = engine.fire(context, "group1");

            assertThat(result.wasFired("group-rule")).isTrue();
            assertThat(result.firedRules()).doesNotContain("other-rule");
        }

        @Test
        @DisplayName("fire() should collect results from context")
        void fireShouldCollectResultsFromContext() {
            Rule rule = new DefaultRule("rule", null, 100, null, true,
                    ctx -> true, ctx -> ctx.setResult("discount", 0.15));

            engine.register(rule);
            RuleContext context = RuleContext.create();
            RuleResult result = engine.fire(context);

            assertThat(result.results()).containsEntry("discount", 0.15);
        }

        @Test
        @DisplayName("fire() should handle rule execution errors")
        void fireShouldHandleRuleExecutionErrors() {
            Rule rule = new DefaultRule("failing-rule", null, 100, null, true,
                    ctx -> true, ctx -> { throw new RuntimeException("Test error"); });

            engine.register(rule);
            RuleContext context = RuleContext.create();
            RuleResult result = engine.fire(context);

            assertThat(result.hasFailed("failing-rule")).isTrue();
            assertThat(result.errors()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("fireFirst() Tests")
    class FireFirstTests {

        @Test
        @DisplayName("fireFirst() should only fire first matching rule")
        void fireFirstShouldOnlyFireFirstMatchingRule() {
            Rule rule1 = new DefaultRule("rule1", null, 1, null, true,
                    ctx -> true, ctx -> ctx.setResult("rule1", true));
            Rule rule2 = new DefaultRule("rule2", null, 2, null, true,
                    ctx -> true, ctx -> ctx.setResult("rule2", true));

            engine.register(rule1, rule2);
            RuleContext context = RuleContext.create();
            RuleResult result = engine.fireFirst(context);

            assertThat(result.firedCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("fireUntilHalt() Tests")
    class FireUntilHaltTests {

        @Test
        @DisplayName("fireUntilHalt() should re-evaluate rules")
        void fireUntilHaltShouldReEvaluateRules() {
            Rule rule = new DefaultRule("rule", null, 100, null, true,
                    ctx -> ctx.<Integer>get("counter", 0) < 3,
                    ctx -> ctx.put("counter", ctx.<Integer>get("counter", 0) + 1));

            engine.register(rule);
            RuleContext context = RuleContext.create();
            engine.fireUntilHalt(context);

            assertThat(context.<Integer>get("counter")).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Query Methods Tests")
    class QueryMethodsTests {

        @Test
        @DisplayName("getRules() should return all rules")
        void getRulesShouldReturnAllRules() {
            engine.register(createRule("r1", 100, true, ctx -> true));
            engine.register(createRule("r2", 100, true, ctx -> true));

            assertThat(engine.getRules()).hasSize(2);
        }

        @Test
        @DisplayName("getRules(group) should return group rules only")
        void getRulesWithGroupShouldReturnGroupRulesOnly() {
            Rule groupRule = new DefaultRule("group-rule", null, 100, "group1", true,
                    ctx -> true, ctx -> {});
            Rule otherRule = new DefaultRule("other-rule", null, 100, "group2", true,
                    ctx -> true, ctx -> {});

            engine.register(groupRule, otherRule);
            List<Rule> groupRules = engine.getRules("group1");

            assertThat(groupRules).hasSize(1);
            assertThat(groupRules.getFirst().getName()).isEqualTo("group-rule");
        }

        @Test
        @DisplayName("getRule() should return rule by name")
        void getRuleShouldReturnRuleByName() {
            Rule rule = createRule("my-rule", 100, true, ctx -> true);
            engine.register(rule);

            assertThat(engine.getRule("my-rule")).isSameAs(rule);
        }

        @Test
        @DisplayName("getRule() should return null for missing rule")
        void getRuleShouldReturnNullForMissingRule() {
            assertThat(engine.getRule("nonexistent")).isNull();
        }

        @Test
        @DisplayName("hasRule() should check if rule exists")
        void hasRuleShouldCheckIfRuleExists() {
            engine.register(createRule("exists", 100, true, ctx -> true));

            assertThat(engine.hasRule("exists")).isTrue();
            assertThat(engine.hasRule("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("getRuleCount() should return count")
        void getRuleCountShouldReturnCount() {
            assertThat(engine.getRuleCount()).isZero();

            engine.register(createRule("r1", 100, true, ctx -> true));
            assertThat(engine.getRuleCount()).isEqualTo(1);

            engine.register(createRule("r2", 100, true, ctx -> true));
            assertThat(engine.getRuleCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Listener Tests")
    class ListenerTests {

        @Test
        @DisplayName("addListener() should add listener")
        void addListenerShouldAddListener() {
            List<String> events = new ArrayList<>();
            RuleListener listener = new RuleListener() {
                @Override
                public void onStart(RuleContext context) {
                    events.add("start");
                }
                @Override
                public void onFinish(RuleContext context, int firedCount, long elapsedMillis) {
                    events.add("finish");
                }
            };

            engine.addListener(listener);
            engine.fire(RuleContext.create());

            assertThat(events).contains("start", "finish");
        }

        @Test
        @DisplayName("removeListener() should remove listener")
        void removeListenerShouldRemoveListener() {
            List<String> events = new ArrayList<>();
            RuleListener listener = new RuleListener() {
                @Override
                public void onStart(RuleContext context) {
                    events.add("start");
                }
            };

            engine.addListener(listener);
            engine.removeListener(listener);
            engine.fire(RuleContext.create());

            assertThat(events).isEmpty();
        }

        @Test
        @DisplayName("Listeners should receive all events")
        void listenersShouldReceiveAllEvents() {
            List<String> events = new ArrayList<>();
            Rule rule = createRule("rule", 100, true, ctx -> true);
            engine.register(rule);

            RuleListener listener = new RuleListener() {
                @Override
                public void beforeEvaluate(Rule r, RuleContext context) {
                    events.add("beforeEvaluate:" + r.getName());
                }
                @Override
                public void afterEvaluate(Rule r, RuleContext context, boolean matched) {
                    events.add("afterEvaluate:" + r.getName() + ":" + matched);
                }
                @Override
                public void beforeExecute(Rule r, RuleContext context) {
                    events.add("beforeExecute:" + r.getName());
                }
                @Override
                public void afterExecute(Rule r, RuleContext context) {
                    events.add("afterExecute:" + r.getName());
                }
            };

            engine.addListener(listener);
            engine.fire(RuleContext.create());

            assertThat(events).containsExactly(
                    "beforeEvaluate:rule",
                    "afterEvaluate:rule:true",
                    "beforeExecute:rule",
                    "afterExecute:rule"
            );
        }
    }

    @Nested
    @DisplayName("Conflict Resolver Tests")
    class ConflictResolverTests {

        @Test
        @DisplayName("setConflictResolver() should use custom resolver")
        void setConflictResolverShouldUseCustomResolver() {
            Rule highPriority = new DefaultRule("high", null, 1, null, true,
                    ctx -> true, ctx -> ctx.setResult("order", ctx.<String>getResult("order", "") + "high"));
            Rule lowPriority = new DefaultRule("low", null, 100, null, true,
                    ctx -> true, ctx -> ctx.setResult("order", ctx.<String>getResult("order", "") + "low"));

            engine.register(lowPriority, highPriority);
            engine.setConflictResolver(PriorityConflictResolver.INSTANCE);

            RuleContext context = RuleContext.create();
            engine.fire(context);

            // With priority resolver, high priority should execute first
            assertThat(context.<String>getResult("order")).startsWith("high");
        }

        @Test
        @DisplayName("Default resolver should be priority-based")
        void defaultResolverShouldBePriorityBased() {
            Rule highPriority = new DefaultRule("high", null, 1, null, true,
                    ctx -> true, ctx -> ctx.setResult("first", ctx.getResult("first") == null ? "high" : ctx.getResult("first")));
            Rule lowPriority = new DefaultRule("low", null, 100, null, true,
                    ctx -> true, ctx -> ctx.setResult("first", ctx.getResult("first") == null ? "low" : ctx.getResult("first")));

            engine.register(lowPriority, highPriority);
            RuleContext context = RuleContext.create();
            engine.fire(context);

            assertThat(context.<String>getResult("first")).isEqualTo("high");
        }
    }

    @Nested
    @DisplayName("clear() Tests")
    class ClearTests {

        @Test
        @DisplayName("clear() should remove all rules")
        void clearShouldRemoveAllRules() {
            engine.register(createRule("r1", 100, true, ctx -> true));
            engine.register(createRule("r2", 100, true, ctx -> true));
            engine.clear();

            assertThat(engine.getRuleCount()).isZero();
        }
    }

    private Rule createRule(String name, int priority, boolean enabled, Condition condition) {
        return new DefaultRule(name, null, priority, null, enabled, condition, ctx -> {});
    }
}
