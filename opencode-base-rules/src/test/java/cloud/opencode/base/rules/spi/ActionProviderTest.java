package cloud.opencode.base.rules.spi;

import cloud.opencode.base.rules.model.Action;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ActionProvider SPI Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("ActionProvider SPI Tests")
class ActionProviderTest {

    @Nested
    @DisplayName("Abstract Method Requirements Tests")
    class AbstractMethodRequirementsTests {

        @Test
        @DisplayName("getActions() must be implemented")
        void getActionsMustBeImplemented() {
            ActionProvider provider = createProvider("test", Map.of("action1", ctx -> {}));

            Map<String, Action> actions = provider.getActions();

            assertThat(actions).hasSize(1);
            assertThat(actions).containsKey("action1");
        }

        @Test
        @DisplayName("getName() must be implemented")
        void getNameMustBeImplemented() {
            ActionProvider provider = createProvider("my-provider", Map.of());

            assertThat(provider.getName()).isEqualTo("my-provider");
        }
    }

    @Nested
    @DisplayName("getAction() Default Tests")
    class GetActionDefaultTests {

        @Test
        @DisplayName("getAction() should return action when present")
        void getActionShouldReturnActionWhenPresent() {
            Action action = ctx -> ctx.put("result", "executed");
            ActionProvider provider = createProvider("test", Map.of("myAction", action));

            Optional<Action> result = provider.getAction("myAction");

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(action);
        }

        @Test
        @DisplayName("getAction() should return empty when not found")
        void getActionShouldReturnEmptyWhenNotFound() {
            ActionProvider provider = createProvider("test", Map.of());

            Optional<Action> result = provider.getAction("nonexistent");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("getAction() should work with null value in map")
        void getActionShouldWorkWithNullValueInMap() {
            ActionProvider provider = createProvider("test", new java.util.HashMap<>() {{
                put("nullAction", null);
            }});

            Optional<Action> result = provider.getAction("nullAction");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getPriority() Default Tests")
    class GetPriorityDefaultTests {

        @Test
        @DisplayName("getPriority() default should return 1000")
        void getPriorityDefaultShouldReturn1000() {
            ActionProvider provider = createProvider("test", Map.of());

            assertThat(provider.getPriority()).isEqualTo(1000);
        }

        @Test
        @DisplayName("getPriority() can be overridden")
        void getPriorityCanBeOverridden() {
            ActionProvider provider = new ActionProvider() {
                @Override
                public Map<String, Action> getActions() { return Map.of(); }
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
            ActionProvider provider = createProvider("test", Map.of());

            assertThat(provider.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("isEnabled() can be overridden")
        void isEnabledCanBeOverridden() {
            ActionProvider provider = new ActionProvider() {
                @Override
                public Map<String, Action> getActions() { return Map.of(); }
                @Override
                public String getName() { return "disabled"; }
                @Override
                public boolean isEnabled() { return false; }
            };

            assertThat(provider.isEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("Full Implementation Tests")
    class FullImplementationTests {

        @Test
        @DisplayName("Full implementation should work")
        void fullImplementationShouldWork() {
            Action sendEmail = ctx -> ctx.put("emailSent", true);
            Action sendSms = ctx -> ctx.put("smsSent", true);
            Action logEvent = ctx -> ctx.put("logged", true);

            ActionProvider provider = new ActionProvider() {
                @Override
                public Map<String, Action> getActions() {
                    return Map.of(
                            "sendEmail", sendEmail,
                            "sendSms", sendSms,
                            "logEvent", logEvent
                    );
                }
                @Override
                public String getName() { return "notification-actions"; }
                @Override
                public int getPriority() { return 50; }
                @Override
                public boolean isEnabled() { return true; }
            };

            assertThat(provider.getName()).isEqualTo("notification-actions");
            assertThat(provider.getPriority()).isEqualTo(50);
            assertThat(provider.isEnabled()).isTrue();
            assertThat(provider.getActions()).hasSize(3);
            assertThat(provider.getAction("sendEmail")).isPresent();
            assertThat(provider.getAction("sendSms")).isPresent();
            assertThat(provider.getAction("logEvent")).isPresent();
            assertThat(provider.getAction("unknown")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Multiple Actions Tests")
    class MultipleActionsTests {

        @Test
        @DisplayName("Should handle multiple actions")
        void shouldHandleMultipleActions() {
            ActionProvider provider = createProvider("multi", Map.of(
                    "action1", ctx -> {},
                    "action2", ctx -> {},
                    "action3", ctx -> {}
            ));

            assertThat(provider.getActions()).hasSize(3);
            assertThat(provider.getAction("action1")).isPresent();
            assertThat(provider.getAction("action2")).isPresent();
            assertThat(provider.getAction("action3")).isPresent();
        }
    }

    private ActionProvider createProvider(String name, Map<String, Action> actions) {
        return new ActionProvider() {
            @Override
            public Map<String, Action> getActions() { return actions; }
            @Override
            public String getName() { return name; }
        };
    }
}
