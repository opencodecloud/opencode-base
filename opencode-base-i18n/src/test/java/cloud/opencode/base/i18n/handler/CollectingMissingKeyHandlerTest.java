package cloud.opencode.base.i18n.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for CollectingMissingKeyHandler
 */
@DisplayName("CollectingMissingKeyHandler")
class CollectingMissingKeyHandlerTest {

    @Nested
    @DisplayName("Collection behavior")
    class Collection {
        @Test void collectsKeys() {
            CollectingMissingKeyHandler handler = new CollectingMissingKeyHandler();
            handler.onMissingKey("key.one", Locale.ENGLISH);
            handler.onMissingKey("key.two", Locale.FRENCH);

            assertThat(handler.getMissingKeys()).containsExactlyInAnyOrder("key.one", "key.two");
            assertThat(handler.size()).isEqualTo(2);
            assertThat(handler.isEmpty()).isFalse();
        }

        @Test void deduplicatesKeys() {
            CollectingMissingKeyHandler handler = new CollectingMissingKeyHandler();
            handler.onMissingKey("key.one", Locale.ENGLISH);
            handler.onMissingKey("key.one", Locale.FRENCH);  // same key, different locale

            assertThat(handler.size()).isEqualTo(1);
            assertThat(handler.getMissingKeys()).containsExactly("key.one");
        }

        @Test void containsWorks() {
            CollectingMissingKeyHandler handler = new CollectingMissingKeyHandler();
            handler.onMissingKey("found.key", Locale.ENGLISH);

            assertThat(handler.contains("found.key")).isTrue();
            assertThat(handler.contains("not.there")).isFalse();
        }
    }

    @Nested
    @DisplayName("clear()")
    class Clear {
        @Test void clearsAllKeys() {
            CollectingMissingKeyHandler handler = new CollectingMissingKeyHandler();
            handler.onMissingKey("k1", Locale.ENGLISH);
            handler.onMissingKey("k2", Locale.ENGLISH);

            handler.clear();

            assertThat(handler.isEmpty()).isTrue();
            assertThat(handler.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Immutable view")
    class ImmutableView {
        @Test void returnedSetIsUnmodifiable() {
            CollectingMissingKeyHandler handler = new CollectingMissingKeyHandler();
            handler.onMissingKey("k", Locale.ENGLISH);

            assertThatThrownBy(() -> handler.getMissingKeys().add("x"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Thread safety")
    class ThreadSafety {
        @Test void concurrentWrites() throws InterruptedException {
            CollectingMissingKeyHandler handler = new CollectingMissingKeyHandler();
            int threads = 10;
            int perThread = 100;

            Thread[] ts = new Thread[threads];
            for (int i = 0; i < threads; i++) {
                final int idx = i;
                ts[i] = new Thread(() -> {
                    for (int j = 0; j < perThread; j++) {
                        handler.onMissingKey("key." + idx + "." + j, Locale.ENGLISH);
                    }
                });
            }
            for (Thread t : ts) t.start();
            for (Thread t : ts) t.join();

            assertThat(handler.size()).isEqualTo(threads * perThread);
        }
    }
}
