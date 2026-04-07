package cloud.opencode.base.json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MixinSource")
class MixinSourceTest {

    @Nested
    @DisplayName("addMixin and getMixin")
    class AddAndGetTest {

        @Test
        @DisplayName("add and get round-trip")
        void roundTrip() {
            var source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);

            assertThat(source.getMixin(String.class)).isEqualTo(CharSequence.class);
        }

        @Test
        @DisplayName("add multiple mixins independently")
        void multipleIndependent() {
            var source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);
            source.addMixin(Integer.class, Number.class);
            source.addMixin(Double.class, Comparable.class);

            assertThat(source.getMixin(String.class)).isEqualTo(CharSequence.class);
            assertThat(source.getMixin(Integer.class)).isEqualTo(Number.class);
            assertThat(source.getMixin(Double.class)).isEqualTo(Comparable.class);
        }

        @Test
        @DisplayName("overwrite existing mixin for same target")
        void overwrite() {
            var source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);
            source.addMixin(String.class, Comparable.class);

            assertThat(source.getMixin(String.class)).isEqualTo(Comparable.class);
            assertThat(source.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("getMixin returns null for unregistered target")
        void getUnregistered() {
            var source = new MixinSource();
            assertThat(source.getMixin(String.class)).isNull();
        }
    }

    @Nested
    @DisplayName("hasMixin")
    class HasMixinTest {

        @Test
        @DisplayName("returns true for registered target")
        void registeredTarget() {
            var source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);

            assertThat(source.hasMixin(String.class)).isTrue();
        }

        @Test
        @DisplayName("returns false for unregistered target")
        void unregisteredTarget() {
            var source = new MixinSource();
            assertThat(source.hasMixin(String.class)).isFalse();
        }

        @Test
        @DisplayName("returns false after removal")
        void afterRemoval() {
            var source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);
            source.removeMixin(String.class);

            assertThat(source.hasMixin(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("removeMixin")
    class RemoveMixinTest {

        @Test
        @DisplayName("removes existing mixin")
        void removesExisting() {
            var source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);
            source.addMixin(Integer.class, Number.class);

            source.removeMixin(String.class);

            assertThat(source.hasMixin(String.class)).isFalse();
            assertThat(source.hasMixin(Integer.class)).isTrue();
            assertThat(source.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("removing non-existent mixin does not throw")
        void removeNonExistent() {
            var source = new MixinSource();
            assertThatCode(() -> source.removeMixin(String.class)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("getMixins")
    class GetMixinsTest {

        @Test
        @DisplayName("returns unmodifiable map")
        void unmodifiable() {
            var source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);

            Map<Class<?>, Class<?>> mixins = source.getMixins();

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> mixins.put(Integer.class, Number.class));
        }

        @Test
        @DisplayName("reflects current state")
        void reflectsCurrentState() {
            var source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);
            source.addMixin(Integer.class, Number.class);

            Map<Class<?>, Class<?>> mixins = source.getMixins();

            assertThat(mixins).hasSize(2)
                    .containsEntry(String.class, CharSequence.class)
                    .containsEntry(Integer.class, Number.class);
        }

        @Test
        @DisplayName("empty source returns empty map")
        void emptySource() {
            var source = new MixinSource();
            assertThat(source.getMixins()).isEmpty();
        }
    }

    @Nested
    @DisplayName("clear")
    class ClearTest {

        @Test
        @DisplayName("removes all mixins")
        void removesAll() {
            var source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);
            source.addMixin(Integer.class, Number.class);
            source.addMixin(Double.class, Comparable.class);

            source.clear();

            assertThat(source.size()).isZero();
            assertThat(source.hasMixin(String.class)).isFalse();
            assertThat(source.hasMixin(Integer.class)).isFalse();
            assertThat(source.hasMixin(Double.class)).isFalse();
        }

        @Test
        @DisplayName("clearing empty source does not throw")
        void clearEmpty() {
            var source = new MixinSource();
            assertThatCode(source::clear).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("can add mixins after clear")
        void addAfterClear() {
            var source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);
            source.clear();

            source.addMixin(Integer.class, Number.class);

            assertThat(source.size()).isEqualTo(1);
            assertThat(source.hasMixin(Integer.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("size")
    class SizeTest {

        @Test
        @DisplayName("tracks additions correctly")
        void tracksAdditions() {
            var source = new MixinSource();

            assertThat(source.size()).isZero();
            source.addMixin(String.class, CharSequence.class);
            assertThat(source.size()).isEqualTo(1);
            source.addMixin(Integer.class, Number.class);
            assertThat(source.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("tracks removals correctly")
        void tracksRemovals() {
            var source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);
            source.addMixin(Integer.class, Number.class);

            source.removeMixin(String.class);

            assertThat(source.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("overwrite does not change size")
        void overwriteKeepsSize() {
            var source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);
            source.addMixin(String.class, Comparable.class);

            assertThat(source.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Null handling")
    class NullHandlingTest {

        @Test
        @DisplayName("addMixin with null target throws NullPointerException")
        void nullTarget() {
            var source = new MixinSource();
            assertThatNullPointerException()
                    .isThrownBy(() -> source.addMixin(null, CharSequence.class));
        }

        @Test
        @DisplayName("addMixin with null mixin throws NullPointerException")
        void nullMixin() {
            var source = new MixinSource();
            assertThatNullPointerException()
                    .isThrownBy(() -> source.addMixin(String.class, null));
        }

        @Test
        @DisplayName("getMixin with null target throws NullPointerException")
        void getNullTarget() {
            var source = new MixinSource();
            assertThatNullPointerException()
                    .isThrownBy(() -> source.getMixin(null));
        }

        @Test
        @DisplayName("hasMixin with null target throws NullPointerException")
        void hasNullTarget() {
            var source = new MixinSource();
            assertThatNullPointerException()
                    .isThrownBy(() -> source.hasMixin(null));
        }

        @Test
        @DisplayName("removeMixin with null target throws NullPointerException")
        void removeNullTarget() {
            var source = new MixinSource();
            assertThatNullPointerException()
                    .isThrownBy(() -> source.removeMixin(null));
        }
    }

    @Nested
    @DisplayName("Thread safety")
    class ThreadSafetyTest {

        @Test
        @DisplayName("concurrent add and get from multiple threads")
        void concurrentAddAndGet() throws InterruptedException {
            var source = new MixinSource();
            int threadCount = 8;
            int operationsPerThread = 200;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger errorCount = new AtomicInteger(0);

            // Pre-create distinct classes to use as keys - use array types for uniqueness
            Class<?>[] targets = {
                    String.class, Integer.class, Long.class, Double.class,
                    Float.class, Short.class, Byte.class, Boolean.class
            };
            Class<?>[] mixins = {
                    CharSequence.class, Number.class, Serializable.class, Comparable.class,
                    Runnable.class, Cloneable.class, AutoCloseable.class, Iterable.class
            };

            try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
                for (int t = 0; t < threadCount; t++) {
                    int threadIdx = t;
                    executor.submit(() -> {
                        try {
                            startLatch.await();
                            for (int i = 0; i < operationsPerThread; i++) {
                                source.addMixin(targets[threadIdx], mixins[threadIdx]);
                                Class<?> result = source.getMixin(targets[threadIdx]);
                                if (result == null) {
                                    errorCount.incrementAndGet();
                                }
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        } finally {
                            doneLatch.countDown();
                        }
                    });
                }
                startLatch.countDown();
                doneLatch.await();
            }

            assertThat(errorCount.get()).isZero();
            assertThat(source.size()).isEqualTo(threadCount);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("contains size information")
        void containsSize() {
            var source = new MixinSource();
            source.addMixin(String.class, CharSequence.class);
            source.addMixin(Integer.class, Number.class);

            assertThat(source.toString()).contains("2");
        }

        @Test
        @DisplayName("contains class name")
        void containsClassName() {
            var source = new MixinSource();
            assertThat(source.toString()).contains("MixinSource");
        }
    }
}
