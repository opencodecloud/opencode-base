package cloud.opencode.base.i18n.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for MissingKeyHandler
 */
@DisplayName("MissingKeyHandler")
class MissingKeyHandlerTest {

    @Nested
    @DisplayName("noOp()")
    class NoOp {
        @Test void doesNotThrow() {
            MissingKeyHandler handler = MissingKeyHandler.noOp();
            assertThatCode(() -> handler.onMissingKey("test.key", Locale.ENGLISH))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("logging()")
    class LoggingHandler {
        @Test void logsWithoutException() {
            MissingKeyHandler handler = MissingKeyHandler.logging();
            assertThatCode(() -> handler.onMissingKey("missing.key", Locale.FRENCH))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("collecting()")
    class CollectingFactory {
        @Test void returnsCollectingHandler() {
            CollectingMissingKeyHandler collector = MissingKeyHandler.collecting();
            assertThat(collector).isNotNull();
            assertThat(collector.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("andThen()")
    class AndThen {
        @Test void callsBothHandlers() {
            CollectingMissingKeyHandler first  = new CollectingMissingKeyHandler();
            CollectingMissingKeyHandler second = new CollectingMissingKeyHandler();

            MissingKeyHandler composed = first.andThen(second);
            composed.onMissingKey("key.one", Locale.ENGLISH);

            assertThat(first.contains("key.one")).isTrue();
            assertThat(second.contains("key.one")).isTrue();
        }

        @Test void secondCalledEvenIfFirstThrows() {
            CollectingMissingKeyHandler second = new CollectingMissingKeyHandler();
            MissingKeyHandler throwing = (k, l) -> { throw new RuntimeException("oops"); };
            MissingKeyHandler composed = throwing.andThen(second);
            // andThen propagates the exception from the first handler
            assertThatRuntimeException().isThrownBy(() -> composed.onMissingKey("k", Locale.ENGLISH));
        }
    }
}
