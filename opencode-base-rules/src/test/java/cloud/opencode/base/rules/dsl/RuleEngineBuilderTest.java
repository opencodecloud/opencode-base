package cloud.opencode.base.rules.dsl;

import cloud.opencode.base.rules.*;
import cloud.opencode.base.rules.conflict.OrderConflictResolver;
import cloud.opencode.base.rules.conflict.PriorityConflictResolver;
import cloud.opencode.base.rules.listener.RuleListener;
import cloud.opencode.base.rules.model.Action;
import cloud.opencode.base.rules.model.Condition;
import cloud.opencode.base.rules.model.RuleGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * RuleEngineBuilder Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("RuleEngineBuilder Tests")
class RuleEngineBuilderTest {

    @Nested
    @DisplayName("register(Rule...) Tests")
    class RegisterRulesTests {

        @Test
        @DisplayName("register(Rule...) should add rules to engine")
        void registerShouldAddRulesToEngine() {
            Rule rule1 = createRule("rule1");
            Rule rule2 = createRule("rule2");

            RuleEngine engine = new RuleEngineBuilder()
                    .register(rule1, rule2)
                    .build();

            assertThat(engine.getRuleCount()).isEqualTo(2);
            assertThat(engine.hasRule("rule1")).isTrue();
            assertThat(engine.hasRule("rule2")).isTrue();
        }

        @Test
        @DisplayName("register(Rule...) should be chainable")
        void registerShouldBeChainable() {
            Rule rule1 = createRule("rule1");
            Rule rule2 = createRule("rule2");

            RuleEngine engine = new RuleEngineBuilder()
                    .register(rule1)
                    .register(rule2)
                    .build();

            assertThat(engine.getRuleCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("register(RuleGroup) Tests")
    class RegisterGroupTests {

        @Test
        @DisplayName("register(RuleGroup) should add group rules to engine")
        void registerGroupShouldAddGroupRulesToEngine() {
            Rule rule1 = createRule("rule1");
            Rule rule2 = createRule("rule2");
            RuleGroup group = RuleGroup.builder("group")
                    .addRules(rule1, rule2)
                    .build();

            RuleEngine engine = new RuleEngineBuilder()
                    .register(group)
                    .build();

            assertThat(engine.getRuleCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Multiple groups should be combined")
        void multipleGroupsShouldBeCombined() {
            RuleGroup group1 = RuleGroup.builder("group1")
                    .addRule(createRule("rule1"))
                    .build();
            RuleGroup group2 = RuleGroup.builder("group2")
                    .addRule(createRule("rule2"))
                    .build();

            RuleEngine engine = new RuleEngineBuilder()
                    .register(group1)
                    .register(group2)
                    .build();

            assertThat(engine.getRuleCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("setConflictResolver() Tests")
    class ConflictResolverTests {

        @Test
        @DisplayName("setConflictResolver() should configure engine")
        void setConflictResolverShouldConfigureEngine() {
            Rule highPriority = createRuleWithPriority("high", 1);
            Rule lowPriority = createRuleWithPriority("low", 100);

            List<String> executionOrder = new ArrayList<>();

            Rule trackingHigh = new Rule() {
                @Override
                public String getName() { return "high"; }
                @Override
                public String getDescription() { return null; }
                @Override
                public int getPriority() { return 1; }
                @Override
                public String getGroup() { return null; }
                @Override
                public boolean isEnabled() { return true; }
                @Override
                public boolean evaluate(RuleContext context) { return true; }
                @Override
                public void execute(RuleContext context) { executionOrder.add("high"); }
            };

            Rule trackingLow = new Rule() {
                @Override
                public String getName() { return "low"; }
                @Override
                public String getDescription() { return null; }
                @Override
                public int getPriority() { return 100; }
                @Override
                public String getGroup() { return null; }
                @Override
                public boolean isEnabled() { return true; }
                @Override
                public boolean evaluate(RuleContext context) { return true; }
                @Override
                public void execute(RuleContext context) { executionOrder.add("low"); }
            };

            RuleEngine engine = new RuleEngineBuilder()
                    .register(trackingLow, trackingHigh)
                    .setConflictResolver(PriorityConflictResolver.INSTANCE)
                    .build();

            engine.fire(RuleContext.create());

            assertThat(executionOrder.getFirst()).isEqualTo("high");
        }
    }

    @Nested
    @DisplayName("addListener() Tests")
    class AddListenerTests {

        @Test
        @DisplayName("addListener() should add listener to engine")
        void addListenerShouldAddListenerToEngine() {
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

            RuleEngine engine = new RuleEngineBuilder()
                    .addListener(listener)
                    .build();

            engine.fire(RuleContext.create());

            assertThat(events).contains("start", "finish");
        }

        @Test
        @DisplayName("Multiple listeners should all be notified")
        void multipleListenersShouldAllBeNotified() {
            List<String> events1 = new ArrayList<>();
            List<String> events2 = new ArrayList<>();

            RuleListener listener1 = new RuleListener() {
                @Override
                public void onStart(RuleContext context) { events1.add("start"); }
            };

            RuleListener listener2 = new RuleListener() {
                @Override
                public void onStart(RuleContext context) { events2.add("start"); }
            };

            RuleEngine engine = new RuleEngineBuilder()
                    .addListener(listener1)
                    .addListener(listener2)
                    .build();

            engine.fire(RuleContext.create());

            assertThat(events1).contains("start");
            assertThat(events2).contains("start");
        }
    }

    @Nested
    @DisplayName("build() Tests")
    class BuildTests {

        @Test
        @DisplayName("build() should return configured engine")
        void buildShouldReturnConfiguredEngine() {
            Rule rule = createRule("rule");

            RuleEngine engine = new RuleEngineBuilder()
                    .register(rule)
                    .build();

            assertThat(engine).isNotNull();
            assertThat(engine.getRuleCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("build() should return engine with empty rules when none registered")
        void buildShouldReturnEngineWithEmptyRulesWhenNoneRegistered() {
            RuleEngine engine = new RuleEngineBuilder().build();
            assertThat(engine.getRuleCount()).isZero();
        }
    }

    @Nested
    @DisplayName("Fluent API Tests")
    class FluentApiTests {

        @Test
        @DisplayName("All methods should be chainable")
        void allMethodsShouldBeChainable() {
            Rule rule = createRule("rule");
            RuleGroup group = RuleGroup.builder("group").build();
            RuleListener listener = new RuleListener() {};

            RuleEngine engine = new RuleEngineBuilder()
                    .register(rule)
                    .register(group)
                    .setConflictResolver(PriorityConflictResolver.INSTANCE)
                    .addListener(listener)
                    .build();

            assertThat(engine).isNotNull();
        }
    }

    private Rule createRule(String name) {
        return new Rule() {
            @Override
            public String getName() { return name; }
            @Override
            public String getDescription() { return null; }
            @Override
            public int getPriority() { return Rule.DEFAULT_PRIORITY; }
            @Override
            public String getGroup() { return null; }
            @Override
            public boolean isEnabled() { return true; }
            @Override
            public boolean evaluate(RuleContext context) { return true; }
            @Override
            public void execute(RuleContext context) {}
        };
    }

    private Rule createRuleWithPriority(String name, int priority) {
        return new Rule() {
            @Override
            public String getName() { return name; }
            @Override
            public String getDescription() { return null; }
            @Override
            public int getPriority() { return priority; }
            @Override
            public String getGroup() { return null; }
            @Override
            public boolean isEnabled() { return true; }
            @Override
            public boolean evaluate(RuleContext context) { return true; }
            @Override
            public void execute(RuleContext context) {}
        };
    }
}
