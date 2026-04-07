package cloud.opencode.base.classloader.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ResourceWatchHandle
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("ResourceWatchHandle Tests")
class ResourceWatchHandleTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create handle with cancel action")
        void shouldCreateHandleWithCancelAction() {
            ResourceWatchHandle handle = new ResourceWatchHandle(() -> {});

            assertThat(handle).isNotNull();
            assertThat(handle.isClosed()).isFalse();
        }

        @Test
        @DisplayName("Should throw on null cancel action")
        void shouldThrowOnNullCancelAction() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ResourceWatchHandle(null))
                    .withMessageContaining("Cancel action");
        }
    }

    @Nested
    @DisplayName("Close Tests")
    class CloseTests {

        @Test
        @DisplayName("Should run cancel action on close")
        void shouldRunCancelActionOnClose() {
            AtomicInteger counter = new AtomicInteger(0);
            ResourceWatchHandle handle = new ResourceWatchHandle(counter::incrementAndGet);

            handle.close();

            assertThat(counter.get()).isEqualTo(1);
            assertThat(handle.isClosed()).isTrue();
        }

        @Test
        @DisplayName("Should be idempotent on multiple closes")
        void shouldBeIdempotentOnMultipleCloses() {
            AtomicInteger counter = new AtomicInteger(0);
            ResourceWatchHandle handle = new ResourceWatchHandle(counter::incrementAndGet);

            handle.close();
            handle.close();
            handle.close();

            assertThat(counter.get()).isEqualTo(1);
            assertThat(handle.isClosed()).isTrue();
        }

        @Test
        @DisplayName("Should work with try-with-resources")
        void shouldWorkWithTryWithResources() {
            AtomicInteger counter = new AtomicInteger(0);

            try (ResourceWatchHandle handle = new ResourceWatchHandle(counter::incrementAndGet)) {
                assertThat(handle.isClosed()).isFalse();
            }

            assertThat(counter.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("State Tests")
    class StateTests {

        @Test
        @DisplayName("Should report not closed initially")
        void shouldReportNotClosedInitially() {
            ResourceWatchHandle handle = new ResourceWatchHandle(() -> {});

            assertThat(handle.isClosed()).isFalse();
        }

        @Test
        @DisplayName("Should report closed after close")
        void shouldReportClosedAfterClose() {
            ResourceWatchHandle handle = new ResourceWatchHandle(() -> {});

            handle.close();

            assertThat(handle.isClosed()).isTrue();
        }
    }
}
