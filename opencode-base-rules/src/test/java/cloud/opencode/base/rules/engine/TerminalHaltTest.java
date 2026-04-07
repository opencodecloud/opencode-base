package cloud.opencode.base.rules.engine;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.RuleEngine;
import cloud.opencode.base.rules.RuleResult;
import cloud.opencode.base.rules.dsl.RuleBuilder;
import cloud.opencode.base.rules.dsl.RuleEngineBuilder;
import cloud.opencode.base.rules.model.Action;
import cloud.opencode.base.rules.model.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for terminal rule and halt condition features
 * 终止规则和停止条件功能测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.3
 */
@DisplayName("Terminal & Halt - Rule Execution Control | 终止与停止 - 规则执行控制")
class TerminalHaltTest {

    @Nested
    @DisplayName("Terminal Rule | 终止规则")
    class TerminalRuleTest {

        @Test
        @DisplayName("Terminal rule stops subsequent rule execution | 终止规则停止后续规则执行")
        void terminalRuleStopsExecution() {
            AtomicBoolean rule1Fired = new AtomicBoolean(false);
            AtomicBoolean rule2Fired = new AtomicBoolean(false);
            AtomicBoolean rule3Fired = new AtomicBoolean(false);

            Rule rule1 = RuleBuilder.rule("rule1")
                    .priority(1)
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> rule1Fired.set(true))
                    .build();

            Rule rule2 = RuleBuilder.rule("rule2")
                    .priority(2)
                    .terminal()
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> rule2Fired.set(true))
                    .build();

            Rule rule3 = RuleBuilder.rule("rule3")
                    .priority(3)
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> rule3Fired.set(true))
                    .build();

            RuleEngine engine = new RuleEngineBuilder()
                    .register(rule1, rule2, rule3)
                    .build();

            RuleResult result = engine.fire(RuleContext.create());

            assertThat(rule1Fired.get()).isTrue();
            assertThat(rule2Fired.get()).isTrue();
            assertThat(rule3Fired.get()).isFalse();
        }

        @Test
        @DisplayName("Non-terminal rules execute all | 非终止规则全部执行")
        void nonTerminalRulesExecuteAll() {
            AtomicInteger firedCount = new AtomicInteger(0);

            Rule rule1 = RuleBuilder.rule("rule1")
                    .priority(1)
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> firedCount.incrementAndGet())
                    .build();

            Rule rule2 = RuleBuilder.rule("rule2")
                    .priority(2)
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> firedCount.incrementAndGet())
                    .build();

            Rule rule3 = RuleBuilder.rule("rule3")
                    .priority(3)
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> firedCount.incrementAndGet())
                    .build();

            RuleEngine engine = new RuleEngineBuilder()
                    .register(rule1, rule2, rule3)
                    .build();

            engine.fire(RuleContext.create());

            assertThat(firedCount.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("Terminal rule via builder with boolean | 通过builder设置终止规则（布尔值）")
        void terminalWithBoolean() {
            Rule rule = RuleBuilder.rule("test")
                    .terminal(true)
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("Non-terminal by default | 默认非终止")
        void nonTerminalByDefault() {
            Rule rule = RuleBuilder.rule("test")
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("DefaultRule toString includes terminal when true | DefaultRule toString在true时包含terminal")
        void toStringIncludesTerminal() {
            Rule rule = RuleBuilder.rule("stop-rule")
                    .terminal()
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.toString()).contains("terminal=true");
        }

        @Test
        @DisplayName("DefaultRule toString excludes terminal when false | DefaultRule toString在false时不包含terminal")
        void toStringExcludesTerminalWhenFalse() {
            Rule rule = RuleBuilder.rule("normal-rule")
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.toString()).doesNotContain("terminal");
        }
    }

    @Nested
    @DisplayName("Halt Condition | 停止条件")
    class HaltConditionTest {

        @Test
        @DisplayName("haltWhen stops execution when condition met | haltWhen在条件满足时停止执行")
        void haltWhenStopsExecution() {
            AtomicInteger firedCount = new AtomicInteger(0);

            Rule rule1 = RuleBuilder.rule("rule1")
                    .priority(1)
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {
                        firedCount.incrementAndGet();
                        ctx.put("stop", true);
                    })
                    .build();

            Rule rule2 = RuleBuilder.rule("rule2")
                    .priority(2)
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> firedCount.incrementAndGet())
                    .build();

            RuleEngine engine = new RuleEngineBuilder()
                    .register(rule1, rule2)
                    .haltWhen(ctx -> Boolean.TRUE.equals(ctx.get("stop")))
                    .build();

            engine.fire(RuleContext.create());

            assertThat(firedCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("haltWhen does not stop when condition not met | haltWhen在条件不满足时不停止")
        void haltWhenDoesNotStopIfNotMet() {
            AtomicInteger firedCount = new AtomicInteger(0);

            Rule rule1 = RuleBuilder.rule("rule1")
                    .priority(1)
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> firedCount.incrementAndGet())
                    .build();

            Rule rule2 = RuleBuilder.rule("rule2")
                    .priority(2)
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> firedCount.incrementAndGet())
                    .build();

            RuleEngine engine = new RuleEngineBuilder()
                    .register(rule1, rule2)
                    .haltWhen(ctx -> false)
                    .build();

            engine.fire(RuleContext.create());

            assertThat(firedCount.get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Combined terminal + halt | 终止+停止组合")
    class CombinedTest {

        @Test
        @DisplayName("Terminal rule takes priority over halt condition | 终止规则优先于停止条件")
        void terminalTakesPriority() {
            AtomicInteger firedCount = new AtomicInteger(0);

            Rule rule1 = RuleBuilder.rule("rule1")
                    .priority(1)
                    .terminal()
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> firedCount.incrementAndGet())
                    .build();

            Rule rule2 = RuleBuilder.rule("rule2")
                    .priority(2)
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> firedCount.incrementAndGet())
                    .build();

            RuleEngine engine = new RuleEngineBuilder()
                    .register(rule1, rule2)
                    .haltWhen(ctx -> false) // halt condition never met
                    .build();

            engine.fire(RuleContext.create());

            // Only rule1 fires because it is terminal
            assertThat(firedCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Halt condition stops before terminal rule | 停止条件在终止规则前停止")
        void haltBeforeTerminal() {
            AtomicInteger firedCount = new AtomicInteger(0);

            Rule rule1 = RuleBuilder.rule("rule1")
                    .priority(1)
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {
                        firedCount.incrementAndGet();
                        ctx.put("done", true);
                    })
                    .build();

            Rule rule2 = RuleBuilder.rule("rule2")
                    .priority(2)
                    .terminal()
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> firedCount.incrementAndGet())
                    .build();

            RuleEngine engine = new RuleEngineBuilder()
                    .register(rule1, rule2)
                    .haltWhen(ctx -> Boolean.TRUE.equals(ctx.get("done")))
                    .build();

            engine.fire(RuleContext.create());

            // rule1 fires and sets "done", halt condition triggers before rule2
            assertThat(firedCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Terminal rule stops fireUntilHalt loop | 终止规则停止fireUntilHalt循环")
        void terminalStopsFireUntilHaltLoop() {
            AtomicInteger firedCount = new AtomicInteger(0);

            Rule rule = RuleBuilder.rule("counter")
                    .priority(1)
                    .terminal()
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> firedCount.incrementAndGet())
                    .build();

            RuleEngine engine = new RuleEngineBuilder()
                    .register(rule)
                    .build();

            engine.fireUntilHalt(RuleContext.create());

            // Terminal rule should stop after first fire, not loop
            assertThat(firedCount.get()).isEqualTo(1);
        }
    }
}
