package cloud.opencode.base.core;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

class LazyTest {

    /** Helper to disambiguate Lazy.of(Supplier) vs Lazy.of(CheckedSupplier). */
    private static <T> Lazy<T> lazy(Supplier<T> supplier) {
        return Lazy.of(supplier);
    }

    // ==================== Factory Methods ====================

    @Nested
    class FactoryMethods {

        @Test
        void ofCreatesUnevaluatedLazy() {
            Lazy<String> lazy = lazy(() -> "hello");
            assertThat(lazy.isEvaluated()).isFalse();
        }

        @Test
        void ofRejectsNullSupplier() {
            assertThatThrownBy(() -> Lazy.of((Supplier<String>) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void ofCheckedSupplier() {
            Lazy<String> lazy = Lazy.of((cloud.opencode.base.core.func.CheckedSupplier<String>) () -> "hello");
            assertThat(lazy.get()).isEqualTo("hello");
        }

        @Test
        void ofCheckedSupplierWrapsCheckedException() {
            Lazy<String> lazy = Lazy.of((cloud.opencode.base.core.func.CheckedSupplier<String>) () -> {
                throw new Exception("checked");
            });
            assertThatThrownBy(lazy::get)
                    .isInstanceOf(OpenException.class)
                    .hasCauseInstanceOf(Exception.class);
        }

        @Test
        void valueCreatesEvaluatedLazy() {
            Lazy<String> lazy = Lazy.value("hello");
            assertThat(lazy.isEvaluated()).isTrue();
            assertThat(lazy.get()).isEqualTo("hello");
        }

        @Test
        void valueAllowsNull() {
            Lazy<String> lazy = Lazy.value(null);
            assertThat(lazy.isEvaluated()).isTrue();
            assertThat(lazy.get()).isNull();
        }
    }

    // ==================== Get ====================

    @Nested
    class Get {

        @Test
        void getComputesValueOnFirstCall() {
            AtomicInteger counter = new AtomicInteger(0);
            Lazy<Integer> lazy = lazy(() -> counter.incrementAndGet());
            assertThat(lazy.get()).isEqualTo(1);
        }

        @Test
        void getCachesValue() {
            AtomicInteger counter = new AtomicInteger(0);
            Lazy<Integer> lazy = lazy(() -> counter.incrementAndGet());
            lazy.get();
            lazy.get();
            lazy.get();
            assertThat(counter.get()).isEqualTo(1);
            assertThat(lazy.get()).isEqualTo(1);
        }

        @Test
        void getAllowsNullResult() {
            Lazy<String> lazy = lazy(() -> null);
            assertThat(lazy.get()).isNull();
            assertThat(lazy.isEvaluated()).isTrue();
        }

        @Test
        void isEvaluatedReturnsTrueAfterGet() {
            Lazy<String> lazy = lazy(() -> "hello");
            assertThat(lazy.isEvaluated()).isFalse();
            lazy.get();
            assertThat(lazy.isEvaluated()).isTrue();
        }

        @Test
        void implementsSupplierInterface() {
            Supplier<String> supplier = lazy(() -> "hello");
            assertThat(supplier.get()).isEqualTo("hello");
        }
    }

    // ==================== Map ====================

    @Nested
    class Map {

        @Test
        void mapTransformsLazily() {
            AtomicInteger counter = new AtomicInteger(0);
            Lazy<Integer> lazy = lazy(() -> counter.incrementAndGet());
            Lazy<String> mapped = lazy.map(i -> "val:" + i);
            assertThat(counter.get()).isEqualTo(0); // not evaluated yet
            assertThat(mapped.get()).isEqualTo("val:1");
        }

        @Test
        void mapRejectsNullMapper() {
            Lazy<String> lazy = lazy(() -> "hello");
            assertThatThrownBy(() -> lazy.map(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== FlatMap ====================

    @Nested
    class FlatMap {

        @Test
        void flatMapTransformsLazily() {
            Lazy<Integer> lazy = lazy(() -> 5);
            Lazy<String> flat = lazy.flatMap(i -> lazy(() -> "val:" + i));
            assertThat(flat.get()).isEqualTo("val:5");
        }

        @Test
        void flatMapRejectsNullMapper() {
            Lazy<String> lazy = lazy(() -> "hello");
            assertThatThrownBy(() -> lazy.flatMap(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== Filter ====================

    @Nested
    class Filter {

        @Test
        void filterPassesWhenPredicateSatisfied() {
            Lazy<Integer> lazy = lazy(() -> 42);
            Lazy<Integer> filtered = lazy.filter(i -> i > 0);
            assertThat(filtered.get()).isEqualTo(42);
        }

        @Test
        void filterThrowsWhenPredicateNotSatisfied() {
            Lazy<Integer> lazy = lazy(() -> -1);
            Lazy<Integer> filtered = lazy.filter(i -> i > 0);
            assertThatThrownBy(filtered::get)
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("predicate not satisfied");
        }

        @Test
        void filterRejectsNullPredicate() {
            Lazy<String> lazy = lazy(() -> "hello");
            assertThatThrownBy(() -> lazy.filter(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== GetOrElse ====================

    @Nested
    class GetOrElse {

        @Test
        void getOrElseReturnsValueOnSuccess() {
            Lazy<String> lazy = lazy(() -> "hello");
            assertThat(lazy.getOrElse("default")).isEqualTo("hello");
        }

        @Test
        void getOrElseReturnsDefaultOnException() {
            Lazy<String> lazy = lazy(() -> {
                throw new RuntimeException("boom");
            });
            assertThat(lazy.getOrElse("default")).isEqualTo("default");
        }

        @Test
        void getOrElseWithSupplierReturnsValueOnSuccess() {
            Lazy<String> lazy = lazy(() -> "hello");
            assertThat(lazy.getOrElse(() -> "default")).isEqualTo("hello");
        }

        @Test
        void getOrElseWithSupplierReturnsDefaultOnException() {
            Lazy<String> lazy = lazy(() -> {
                throw new RuntimeException("boom");
            });
            assertThat(lazy.getOrElse(() -> "computed default")).isEqualTo("computed default");
        }
    }

    // ==================== ToOptional ====================

    @Nested
    class ToOptional {

        @Test
        void toOptionalReturnsPresentForNonNull() {
            Lazy<String> lazy = lazy(() -> "hello");
            Optional<String> opt = lazy.toOptional();
            assertThat(opt).contains("hello");
        }

        @Test
        void toOptionalReturnsEmptyForNull() {
            Lazy<String> lazy = lazy(() -> null);
            assertThat(lazy.toOptional()).isEmpty();
        }

        @Test
        void toOptionalReturnsEmptyOnException() {
            Lazy<String> lazy = lazy(() -> {
                throw new RuntimeException("boom");
            });
            assertThat(lazy.toOptional()).isEmpty();
        }
    }

    // ==================== Reset ====================

    @Nested
    class Reset {

        @Test
        void resetAllowsRecomputation() {
            AtomicInteger counter = new AtomicInteger(0);
            Lazy<Integer> lazy = lazy(() -> counter.incrementAndGet());
            assertThat(lazy.get()).isEqualTo(1);
            assertThat(lazy.isEvaluated()).isTrue();

            lazy.reset();
            assertThat(lazy.isEvaluated()).isFalse();
            assertThat(lazy.get()).isEqualTo(2);
        }

        @Test
        void resetThrowsForPreEvaluatedLazy() {
            Lazy<String> lazy = Lazy.value("hello");
            assertThatThrownBy(lazy::reset)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("pre-evaluated");
        }
    }

    // ==================== Exception Memoization ====================

    @Nested
    class ExceptionMemoization {

        @Test
        void supplierExceptionIsMemoized_secondGetRethrowsSameException() {
            AtomicInteger callCount = new AtomicInteger(0);
            RuntimeException original = new RuntimeException("boom");
            Lazy<String> lazy = lazy(() -> {
                callCount.incrementAndGet();
                throw original;
            });

            // First call — supplier invoked, exception thrown
            assertThatThrownBy(lazy::get).isSameAs(original);
            assertThat(callCount.get()).isEqualTo(1);

            // Second call — supplier NOT re-invoked, same exception re-thrown
            assertThatThrownBy(lazy::get).isSameAs(original);
            assertThat(callCount.get()).isEqualTo(1); // still 1
        }

        @Test
        void failedLazy_isNotEvaluated() {
            Lazy<String> lazy = lazy(() -> { throw new RuntimeException("fail"); });
            assertThatThrownBy(lazy::get);
            assertThat(lazy.isEvaluated()).isFalse();
        }

        @Test
        void failedLazy_getOrElseReturnsDefault() {
            Lazy<String> lazy = lazy(() -> { throw new RuntimeException("fail"); });
            assertThat(lazy.getOrElse("default")).isEqualTo("default");
        }

        @Test
        void failedLazy_toOptionalReturnsEmpty() {
            Lazy<String> lazy = lazy(() -> { throw new RuntimeException("fail"); });
            assertThat(lazy.toOptional()).isEmpty();
        }

        @Test
        void failedLazy_toStringShowsFailure() {
            Lazy<String> lazy = lazy(() -> { throw new IllegalStateException("x"); });
            assertThatThrownBy(lazy::get);
            assertThat(lazy.toString()).contains("FAILED").contains("IllegalStateException");
        }

        @Test
        void resetAfterFailure_allowsRetry() {
            AtomicInteger callCount = new AtomicInteger(0);
            Lazy<String> lazy = lazy(() -> {
                if (callCount.incrementAndGet() == 1) {
                    throw new RuntimeException("transient");
                }
                return "recovered";
            });

            // First call fails
            assertThatThrownBy(lazy::get).hasMessage("transient");
            assertThat(callCount.get()).isEqualTo(1);

            // Reset clears the cached failure
            lazy.reset();
            assertThat(lazy.isEvaluated()).isFalse();

            // Second call succeeds
            assertThat(lazy.get()).isEqualTo("recovered");
            assertThat(callCount.get()).isEqualTo(2);
        }
    }

    // ==================== ToString ====================

    @Nested
    class ToString {

        @Test
        void toStringUnevaluated() {
            Lazy<String> lazy = lazy(() -> "hello");
            assertThat(lazy.toString()).isEqualTo("Lazy[?]");
        }

        @Test
        void toStringEvaluated() {
            Lazy<String> lazy = lazy(() -> "hello");
            lazy.get();
            assertThat(lazy.toString()).isEqualTo("Lazy[hello]");
        }

        @Test
        void toStringPreEvaluated() {
            Lazy<String> lazy = Lazy.value("hello");
            assertThat(lazy.toString()).isEqualTo("Lazy[hello]");
        }
    }

    // ==================== Concurrency ====================

    @Nested
    class Concurrency {

        @Test
        void concurrentGetWithVirtualThreadsComputesOnce() throws InterruptedException {
            int threadCount = 100;
            AtomicInteger computeCounter = new AtomicInteger(0);
            ConcurrentHashMap<Integer, Boolean> results = new ConcurrentHashMap<>();

            Supplier<Integer> concurrentSupplier = () -> {
                computeCounter.incrementAndGet();
                // Simulate some work
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return 42;
            };
            Lazy<Integer> lazy = Lazy.of(concurrentSupplier);

            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        startLatch.await();
                        int value = lazy.get();
                        results.put(value, true);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            // Release all threads simultaneously
            startLatch.countDown();
            doneLatch.await();

            // Supplier should have been called exactly once
            assertThat(computeCounter.get()).isEqualTo(1);
            // All threads should have gotten the same value
            assertThat(results).containsOnlyKeys(42);
            assertThat(lazy.isEvaluated()).isTrue();
        }
    }
}
