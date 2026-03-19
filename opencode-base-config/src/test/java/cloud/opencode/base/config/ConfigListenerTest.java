package cloud.opencode.base.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the ConfigListener functional interface.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ConfigListener Tests")
class ConfigListenerTest {

    @Nested
    @DisplayName("Functional Interface Tests")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("ConfigListener is a functional interface")
        void testIsFunctionalInterface() {
            assertThat(ConfigListener.class.isAnnotationPresent(FunctionalInterface.class))
                    .isTrue();
        }

        @Test
        @DisplayName("ConfigListener can be implemented as lambda")
        void testLambdaImplementation() {
            AtomicReference<String> captured = new AtomicReference<>();
            ConfigListener listener = event -> captured.set(event.key());

            ConfigChangeEvent event = ConfigChangeEvent.added("my.key", "my.value");
            listener.onConfigChange(event);

            assertThat(captured.get()).isEqualTo("my.key");
        }

        @Test
        @DisplayName("ConfigListener can be implemented as method reference")
        void testMethodReference() {
            AtomicReference<ConfigChangeEvent> captured = new AtomicReference<>();
            ConfigListener listener = captured::set;

            ConfigChangeEvent event = ConfigChangeEvent.modified("key", "old", "new");
            listener.onConfigChange(event);

            assertThat(captured.get()).isNotNull();
            assertThat(captured.get().key()).isEqualTo("key");
            assertThat(captured.get().oldValue()).isEqualTo("old");
            assertThat(captured.get().newValue()).isEqualTo("new");
        }

        @Test
        @DisplayName("ConfigListener can handle different event types")
        void testDifferentEventTypes() {
            AtomicReference<ConfigChangeEvent.ChangeType> typeRef = new AtomicReference<>();
            ConfigListener listener = event -> typeRef.set(event.changeType());

            listener.onConfigChange(ConfigChangeEvent.added("k", "v"));
            assertThat(typeRef.get()).isEqualTo(ConfigChangeEvent.ChangeType.ADDED);

            listener.onConfigChange(ConfigChangeEvent.modified("k", "v1", "v2"));
            assertThat(typeRef.get()).isEqualTo(ConfigChangeEvent.ChangeType.MODIFIED);

            listener.onConfigChange(ConfigChangeEvent.removed("k", "v"));
            assertThat(typeRef.get()).isEqualTo(ConfigChangeEvent.ChangeType.REMOVED);
        }
    }

    @Nested
    @DisplayName("Anonymous Class Implementation Tests")
    class AnonymousClassTests {

        @Test
        @DisplayName("ConfigListener can be implemented as anonymous class")
        void testAnonymousClass() {
            AtomicReference<String> captured = new AtomicReference<>();

            ConfigListener listener = new ConfigListener() {
                @Override
                public void onConfigChange(ConfigChangeEvent event) {
                    captured.set(event.newValue());
                }
            };

            listener.onConfigChange(ConfigChangeEvent.added("key", "value123"));
            assertThat(captured.get()).isEqualTo("value123");
        }
    }
}
