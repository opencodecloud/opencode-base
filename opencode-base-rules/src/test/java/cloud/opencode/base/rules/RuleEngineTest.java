package cloud.opencode.base.rules;

import cloud.opencode.base.rules.listener.RuleListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

/**
 * RuleEngine Interface Tests
 * RuleEngine 接口测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("RuleEngine Interface Tests | RuleEngine 接口测试")
class RuleEngineTest {

    private RuleEngine engine;

    // Helper typed lambdas to avoid ambiguity between Predicate/Condition and Consumer/Action
    private static final Predicate<RuleContext> ALWAYS_TRUE = ctx -> true;
    private static final Predicate<RuleContext> ALWAYS_FALSE = ctx -> false;
    private static final Consumer<RuleContext> NO_OP = ctx -> {};

    @BeforeEach
    void setUp() {
        engine = OpenRules.defaultEngine();
    }

    @Nested
    @DisplayName("Registration Tests | 注册测试")
    class RegistrationTests {

        @Test
        @DisplayName("register single rule | 注册单个规则")
        void testRegisterSingleRule() {
            Rule rule = OpenRules.rule("test-rule")
                    .when(ALWAYS_TRUE)
                    .then(NO_OP)
                    .build();

            engine.register(rule);

            assertThat(engine.hasRule("test-rule")).isTrue();
            assertThat(engine.getRuleCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("register multiple rules | 注册多个规则")
        void testRegisterMultipleRules() {
            Rule rule1 = OpenRules.rule("rule1").when(ALWAYS_TRUE).then(NO_OP).build();
            Rule rule2 = OpenRules.rule("rule2").when(ALWAYS_TRUE).then(NO_OP).build();
            Rule rule3 = OpenRules.rule("rule3").when(ALWAYS_TRUE).then(NO_OP).build();

            engine.register(rule1, rule2, rule3);

            assertThat(engine.getRuleCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("unregister rule | 注销规则")
        void testUnregisterRule() {
            Rule rule = OpenRules.rule("to-remove").when(ALWAYS_TRUE).then(NO_OP).build();
            engine.register(rule);

            assertThat(engine.hasRule("to-remove")).isTrue();

            engine.unregister("to-remove");

            assertThat(engine.hasRule("to-remove")).isFalse();
        }

        @Test
        @DisplayName("clear removes all rules | clear 移除所有规则")
        void testClear() {
            Rule rule1 = OpenRules.rule("rule1").when(ALWAYS_TRUE).then(NO_OP).build();
            Rule rule2 = OpenRules.rule("rule2").when(ALWAYS_TRUE).then(NO_OP).build();
            engine.register(rule1, rule2);

            engine.clear();

            assertThat(engine.getRuleCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Query Tests | 查询测试")
    class QueryTests {

        @Test
        @DisplayName("getRules returns all rules | getRules 返回所有规则")
        void testGetRules() {
            Rule rule1 = OpenRules.rule("rule1").when(ALWAYS_TRUE).then(NO_OP).build();
            Rule rule2 = OpenRules.rule("rule2").when(ALWAYS_TRUE).then(NO_OP).build();
            engine.register(rule1, rule2);

            List<Rule> rules = engine.getRules();

            assertThat(rules).hasSize(2);
        }

        @Test
        @DisplayName("getRule by name | 按名称获取规则")
        void testGetRule() {
            Rule rule = OpenRules.rule("my-rule").when(ALWAYS_TRUE).then(NO_OP).build();
            engine.register(rule);

            Rule found = engine.getRule("my-rule");

            assertThat(found).isNotNull();
            assertThat(found.getName()).isEqualTo("my-rule");
        }

        @Test
        @DisplayName("getRule returns null for unknown | getRule 对未知规则返回 null")
        void testGetRuleUnknown() {
            Rule found = engine.getRule("nonexistent");
            assertThat(found).isNull();
        }

        @Test
        @DisplayName("hasRule returns correct value | hasRule 返回正确值")
        void testHasRule() {
            Rule rule = OpenRules.rule("exists").when(ALWAYS_TRUE).then(NO_OP).build();
            engine.register(rule);

            assertThat(engine.hasRule("exists")).isTrue();
            assertThat(engine.hasRule("not-exists")).isFalse();
        }
    }

    @Nested
    @DisplayName("Fire Tests | 触发测试")
    class FireTests {

        @Test
        @DisplayName("fire executes matching rules | fire 执行匹配的规则")
        void testFire() {
            AtomicInteger count = new AtomicInteger(0);

            Rule rule1 = OpenRules.rule("rule1")
                    .when(ALWAYS_TRUE)
                    .then((Consumer<RuleContext>) ctx -> count.incrementAndGet())
                    .build();
            Rule rule2 = OpenRules.rule("rule2")
                    .when(ALWAYS_TRUE)
                    .then((Consumer<RuleContext>) ctx -> count.incrementAndGet())
                    .build();

            engine.register(rule1, rule2);
            RuleContext context = OpenRules.context();

            RuleResult result = engine.fire(context);

            assertThat(count.get()).isEqualTo(2);
            assertThat(result.firedCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("fire skips rules that don't match | fire 跳过不匹配的规则")
        void testFireSkipsNonMatching() {
            AtomicInteger count = new AtomicInteger(0);

            Rule matching = OpenRules.rule("matching")
                    .when(ALWAYS_TRUE)
                    .then((Consumer<RuleContext>) ctx -> count.incrementAndGet())
                    .build();
            Rule nonMatching = OpenRules.rule("non-matching")
                    .when(ALWAYS_FALSE)
                    .then((Consumer<RuleContext>) ctx -> count.incrementAndGet())
                    .build();

            engine.register(matching, nonMatching);
            RuleContext context = OpenRules.context();

            engine.fire(context);

            assertThat(count.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("fireFirst executes only first matching rule | fireFirst 只执行第一个匹配的规则")
        void testFireFirst() {
            AtomicInteger count = new AtomicInteger(0);

            Rule rule1 = OpenRules.rule("rule1")
                    .priority(10)
                    .when(ALWAYS_TRUE)
                    .then((Consumer<RuleContext>) ctx -> count.incrementAndGet())
                    .build();
            Rule rule2 = OpenRules.rule("rule2")
                    .priority(20)
                    .when(ALWAYS_TRUE)
                    .then((Consumer<RuleContext>) ctx -> count.incrementAndGet())
                    .build();

            engine.register(rule1, rule2);
            RuleContext context = OpenRules.context();

            engine.fireFirst(context);

            assertThat(count.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("fire with group | 按分组触发")
        void testFireWithGroup() {
            AtomicInteger count = new AtomicInteger(0);

            Rule grouped = OpenRules.rule("grouped")
                    .group("my-group")
                    .when(ALWAYS_TRUE)
                    .then((Consumer<RuleContext>) ctx -> count.incrementAndGet())
                    .build();
            Rule ungrouped = OpenRules.rule("ungrouped")
                    .when(ALWAYS_TRUE)
                    .then((Consumer<RuleContext>) ctx -> count.incrementAndGet())
                    .build();

            engine.register(grouped, ungrouped);
            RuleContext context = OpenRules.context();

            engine.fire(context, "my-group");

            assertThat(count.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Listener Tests | 监听器测试")
    class ListenerTests {

        @Test
        @DisplayName("addListener registers listener | addListener 注册监听器")
        void testAddListener() {
            AtomicInteger beforeCount = new AtomicInteger(0);

            RuleListener listener = new RuleListener() {
                @Override
                public void beforeExecute(Rule rule, RuleContext context) {
                    beforeCount.incrementAndGet();
                }
            };

            engine.addListener(listener);

            Rule rule = OpenRules.rule("test").when(ALWAYS_TRUE).then(NO_OP).build();
            engine.register(rule);
            engine.fire(OpenRules.context());

            assertThat(beforeCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("removeListener unregisters listener | removeListener 注销监听器")
        void testRemoveListener() {
            AtomicInteger count = new AtomicInteger(0);

            RuleListener listener = new RuleListener() {
                @Override
                public void beforeExecute(Rule rule, RuleContext context) {
                    count.incrementAndGet();
                }
            };

            engine.addListener(listener);
            engine.removeListener(listener);

            Rule rule = OpenRules.rule("test").when(ALWAYS_TRUE).then(NO_OP).build();
            engine.register(rule);
            engine.fire(OpenRules.context());

            assertThat(count.get()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Conflict Resolver Tests | 冲突解决器测试")
    class ConflictResolverTests {

        @Test
        @DisplayName("setConflictResolver configures resolver | setConflictResolver 配置解决器")
        void testSetConflictResolver() {
            engine.setConflictResolver(OpenRules.priorityResolver());

            // Just verify it doesn't throw
            assertThat(engine).isNotNull();
        }
    }

    @Nested
    @DisplayName("Group Tests | 分组测试")
    class GroupTests {

        @Test
        @DisplayName("getRules with group | 获取分组中的规则")
        void testGetRulesWithGroup() {
            Rule g1 = OpenRules.rule("g1").group("group-a").when(ALWAYS_TRUE).then(NO_OP).build();
            Rule g2 = OpenRules.rule("g2").group("group-a").when(ALWAYS_TRUE).then(NO_OP).build();
            Rule g3 = OpenRules.rule("g3").group("group-b").when(ALWAYS_TRUE).then(NO_OP).build();

            engine.register(g1, g2, g3);

            List<Rule> groupA = engine.getRules("group-a");
            assertThat(groupA).hasSize(2);

            List<Rule> groupB = engine.getRules("group-b");
            assertThat(groupB).hasSize(1);
        }
    }
}
