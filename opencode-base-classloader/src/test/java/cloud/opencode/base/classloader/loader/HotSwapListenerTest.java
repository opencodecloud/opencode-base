package cloud.opencode.base.classloader.loader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for HotSwapListener
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
@DisplayName("HotSwapListener Tests")
class HotSwapListenerTest {

    @Nested
    @DisplayName("Functional Interface Tests")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("Should implement as lambda")
        void shouldImplementAsLambda() {
            AtomicReference<String> capturedName = new AtomicReference<>();
            AtomicInteger capturedOld = new AtomicInteger(-1);
            AtomicInteger capturedNew = new AtomicInteger(-1);

            HotSwapListener listener = (className, oldVersion, newVersion) -> {
                capturedName.set(className);
                capturedOld.set(oldVersion);
                capturedNew.set(newVersion);
            };

            listener.onSwap("com.example.Test", 1, 2);

            assertThat(capturedName.get()).isEqualTo("com.example.Test");
            assertThat(capturedOld.get()).isEqualTo(1);
            assertThat(capturedNew.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should implement as anonymous class")
        void shouldImplementAsAnonymousClass() {
            AtomicInteger callCount = new AtomicInteger(0);

            HotSwapListener listener = new HotSwapListener() {
                @Override
                public void onSwap(String className, int oldVersion, int newVersion) {
                    callCount.incrementAndGet();
                }
            };

            listener.onSwap("test.Class", 0, 1);

            assertThat(callCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should accept method reference")
        void shouldAcceptMethodReference() {
            var holder = new Object() {
                String name;
                int oldV;
                int newV;

                void handle(String className, int oldVersion, int newVersion) {
                    this.name = className;
                    this.oldV = oldVersion;
                    this.newV = newVersion;
                }
            };

            HotSwapListener listener = holder::handle;
            listener.onSwap("my.Class", 3, 4);

            assertThat(holder.name).isEqualTo("my.Class");
            assertThat(holder.oldV).isEqualTo(3);
            assertThat(holder.newV).isEqualTo(4);
        }

        @Test
        @DisplayName("Should handle zero versions")
        void shouldHandleZeroVersions() {
            AtomicInteger capturedOld = new AtomicInteger(-1);
            AtomicInteger capturedNew = new AtomicInteger(-1);

            HotSwapListener listener = (className, oldVersion, newVersion) -> {
                capturedOld.set(oldVersion);
                capturedNew.set(newVersion);
            };

            listener.onSwap("test.Class", 0, 1);

            assertThat(capturedOld.get()).isEqualTo(0);
            assertThat(capturedNew.get()).isEqualTo(1);
        }
    }
}
