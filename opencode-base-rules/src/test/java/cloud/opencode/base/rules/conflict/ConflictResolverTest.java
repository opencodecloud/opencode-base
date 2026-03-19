package cloud.opencode.base.rules.conflict;

import cloud.opencode.base.rules.OpenRules;
import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

/**
 * ConflictResolver Interface Tests
 * ConflictResolver 接口测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("ConflictResolver Interface Tests | ConflictResolver 接口测试")
class ConflictResolverTest {

    // Helper typed lambdas to avoid ambiguity between Predicate/Condition and Consumer/Action
    private static final Predicate<RuleContext> ALWAYS_TRUE = ctx -> true;
    private static final Consumer<RuleContext> NO_OP = ctx -> {};

    @Nested
    @DisplayName("Functional Interface Tests | 函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("ConflictResolver is functional interface | ConflictResolver 是函数式接口")
        void testFunctionalInterface() {
            assertThat(ConflictResolver.class.isAnnotationPresent(FunctionalInterface.class)).isTrue();
        }

        @Test
        @DisplayName("can use lambda | 可以使用 lambda")
        void testLambda() {
            ConflictResolver resolver = rules -> rules;

            List<Rule> rules = createTestRules();
            List<Rule> result = resolver.resolve(rules);

            assertThat(result).hasSameElementsAs(rules);
        }
    }

    @Nested
    @DisplayName("Resolve Tests | 解决测试")
    class ResolveTests {

        @Test
        @DisplayName("resolve returns ordered list | resolve 返回排序后的列表")
        void testResolve() {
            ConflictResolver resolver = rules -> {
                List<Rule> sorted = new ArrayList<>(rules);
                sorted.sort((r1, r2) -> Integer.compare(r1.getPriority(), r2.getPriority()));
                return sorted;
            };

            List<Rule> rules = createTestRules();
            List<Rule> result = resolver.resolve(rules);

            assertThat(result.get(0).getName()).isEqualTo("high-priority");
            assertThat(result.get(1).getName()).isEqualTo("medium-priority");
            assertThat(result.get(2).getName()).isEqualTo("low-priority");
        }

        @Test
        @DisplayName("resolve can filter rules | resolve 可以过滤规则")
        void testResolveFilter() {
            ConflictResolver resolver = rules -> rules.stream()
                    .filter(r -> r.getPriority() < 100)
                    .toList();

            List<Rule> rules = createTestRules();
            List<Rule> result = resolver.resolve(rules);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("resolve with empty list | resolve 处理空列表")
        void testResolveEmpty() {
            ConflictResolver resolver = rules -> rules;

            List<Rule> result = resolver.resolve(List.of());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Priority Resolver Tests | 优先级解决器测试")
    class PriorityResolverTests {

        @Test
        @DisplayName("priority resolver orders by priority | 优先级解决器按优先级排序")
        void testPriorityResolver() {
            ConflictResolver resolver = OpenRules.priorityResolver();

            List<Rule> rules = createTestRules();
            List<Rule> result = resolver.resolve(rules);

            assertThat(result.get(0).getPriority()).isLessThanOrEqualTo(result.get(1).getPriority());
            assertThat(result.get(1).getPriority()).isLessThanOrEqualTo(result.get(2).getPriority());
        }
    }

    @Nested
    @DisplayName("Custom Resolver Tests | 自定义解决器测试")
    class CustomResolverTests {

        @Test
        @DisplayName("reverse priority resolver | 反向优先级解决器")
        void testReversePriorityResolver() {
            ConflictResolver resolver = rules -> {
                List<Rule> sorted = new ArrayList<>(rules);
                sorted.sort((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()));
                return sorted;
            };

            List<Rule> rules = createTestRules();
            List<Rule> result = resolver.resolve(rules);

            assertThat(result.get(0).getName()).isEqualTo("low-priority");
        }

        @Test
        @DisplayName("name-based resolver | 基于名称的解决器")
        void testNameBasedResolver() {
            ConflictResolver resolver = rules -> {
                List<Rule> sorted = new ArrayList<>(rules);
                sorted.sort((r1, r2) -> r1.getName().compareTo(r2.getName()));
                return sorted;
            };

            List<Rule> rules = createTestRules();
            List<Rule> result = resolver.resolve(rules);

            // Alphabetical order: high-priority, low-priority, medium-priority
            assertThat(result.get(0).getName()).isEqualTo("high-priority");
            assertThat(result.get(1).getName()).isEqualTo("low-priority");
            assertThat(result.get(2).getName()).isEqualTo("medium-priority");
        }

        @Test
        @DisplayName("first-only resolver | 仅第一个解决器")
        void testFirstOnlyResolver() {
            ConflictResolver resolver = rules -> {
                if (rules.isEmpty()) return rules;
                List<Rule> sorted = new ArrayList<>(rules);
                sorted.sort((r1, r2) -> Integer.compare(r1.getPriority(), r2.getPriority()));
                return List.of(sorted.get(0));
            };

            List<Rule> rules = createTestRules();
            List<Rule> result = resolver.resolve(rules);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("high-priority");
        }
    }

    // Helper method
    private List<Rule> createTestRules() {
        return List.of(
                OpenRules.rule("low-priority")
                        .priority(100)
                        .when(ALWAYS_TRUE)
                        .then(NO_OP)
                        .build(),
                OpenRules.rule("high-priority")
                        .priority(10)
                        .when(ALWAYS_TRUE)
                        .then(NO_OP)
                        .build(),
                OpenRules.rule("medium-priority")
                        .priority(50)
                        .when(ALWAYS_TRUE)
                        .then(NO_OP)
                        .build()
        );
    }
}
