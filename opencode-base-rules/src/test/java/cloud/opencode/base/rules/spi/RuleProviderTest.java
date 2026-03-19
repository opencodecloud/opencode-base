package cloud.opencode.base.rules.spi;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * RuleProvider SPI Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("RuleProvider SPI Tests")
class RuleProviderTest {

    @Nested
    @DisplayName("Abstract Method Requirements Tests")
    class AbstractMethodRequirementsTests {

        @Test
        @DisplayName("getRules() must be implemented")
        void getRulesMustBeImplemented() {
            RuleProvider provider = createProvider("test", List.of(createRule("rule1")));

            Collection<Rule> rules = provider.getRules();

            assertThat(rules).hasSize(1);
        }

        @Test
        @DisplayName("getName() must be implemented")
        void getNameMustBeImplemented() {
            RuleProvider provider = createProvider("my-provider", List.of());

            assertThat(provider.getName()).isEqualTo("my-provider");
        }
    }

    @Nested
    @DisplayName("getPriority() Default Tests")
    class GetPriorityDefaultTests {

        @Test
        @DisplayName("getPriority() default should return 1000")
        void getPriorityDefaultShouldReturn1000() {
            RuleProvider provider = createProvider("test", List.of());

            assertThat(provider.getPriority()).isEqualTo(1000);
        }

        @Test
        @DisplayName("getPriority() can be overridden")
        void getPriorityCanBeOverridden() {
            RuleProvider provider = new RuleProvider() {
                @Override
                public Collection<Rule> getRules() { return List.of(); }
                @Override
                public String getName() { return "custom"; }
                @Override
                public int getPriority() { return 100; }
            };

            assertThat(provider.getPriority()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("isEnabled() Default Tests")
    class IsEnabledDefaultTests {

        @Test
        @DisplayName("isEnabled() default should return true")
        void isEnabledDefaultShouldReturnTrue() {
            RuleProvider provider = createProvider("test", List.of());

            assertThat(provider.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("isEnabled() can be overridden")
        void isEnabledCanBeOverridden() {
            RuleProvider provider = new RuleProvider() {
                @Override
                public Collection<Rule> getRules() { return List.of(); }
                @Override
                public String getName() { return "disabled"; }
                @Override
                public boolean isEnabled() { return false; }
            };

            assertThat(provider.isEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("refresh() Default Tests")
    class RefreshDefaultTests {

        @Test
        @DisplayName("refresh() default should do nothing")
        void refreshDefaultShouldDoNothing() {
            RuleProvider provider = createProvider("test", List.of());

            assertThatCode(provider::refresh).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("refresh() can be overridden")
        void refreshCanBeOverridden() {
            final boolean[] refreshed = {false};
            RuleProvider provider = new RuleProvider() {
                @Override
                public Collection<Rule> getRules() { return List.of(); }
                @Override
                public String getName() { return "refreshable"; }
                @Override
                public void refresh() { refreshed[0] = true; }
            };

            provider.refresh();
            assertThat(refreshed[0]).isTrue();
        }
    }

    @Nested
    @DisplayName("close() Default Tests")
    class CloseDefaultTests {

        @Test
        @DisplayName("close() default should do nothing")
        void closeDefaultShouldDoNothing() {
            RuleProvider provider = createProvider("test", List.of());

            assertThatCode(provider::close).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("close() can be overridden")
        void closeCanBeOverridden() throws Exception {
            final boolean[] closed = {false};
            RuleProvider provider = new RuleProvider() {
                @Override
                public Collection<Rule> getRules() { return List.of(); }
                @Override
                public String getName() { return "closeable"; }
                @Override
                public void close() { closed[0] = true; }
            };

            provider.close();
            assertThat(closed[0]).isTrue();
        }

        @Test
        @DisplayName("close() can throw exception")
        void closeCanThrowException() {
            RuleProvider provider = new RuleProvider() {
                @Override
                public Collection<Rule> getRules() { return List.of(); }
                @Override
                public String getName() { return "failing"; }
                @Override
                public void close() throws Exception {
                    throw new Exception("Close failed");
                }
            };

            assertThatThrownBy(provider::close)
                    .isInstanceOf(Exception.class)
                    .hasMessage("Close failed");
        }
    }

    @Nested
    @DisplayName("Full Implementation Tests")
    class FullImplementationTests {

        @Test
        @DisplayName("Full implementation should work")
        void fullImplementationShouldWork() {
            Rule rule1 = createRule("rule1");
            Rule rule2 = createRule("rule2");

            RuleProvider provider = new RuleProvider() {
                @Override
                public Collection<Rule> getRules() {
                    return List.of(rule1, rule2);
                }
                @Override
                public String getName() { return "full-provider"; }
                @Override
                public int getPriority() { return 50; }
                @Override
                public boolean isEnabled() { return true; }
                @Override
                public void refresh() { /* reload rules */ }
                @Override
                public void close() { /* cleanup */ }
            };

            assertThat(provider.getName()).isEqualTo("full-provider");
            assertThat(provider.getPriority()).isEqualTo(50);
            assertThat(provider.isEnabled()).isTrue();
            assertThat(provider.getRules()).containsExactly(rule1, rule2);
        }
    }

    private RuleProvider createProvider(String name, Collection<Rule> rules) {
        return new RuleProvider() {
            @Override
            public Collection<Rule> getRules() { return rules; }
            @Override
            public String getName() { return name; }
        };
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
            public boolean evaluate(RuleContext context) { return true; }
            @Override
            public void execute(RuleContext context) {}
        };
    }
}
