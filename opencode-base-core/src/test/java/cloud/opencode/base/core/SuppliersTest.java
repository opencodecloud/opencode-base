/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Suppliers class
 * Suppliers 类的全面测试
 *
 * @author Test
 * @since JDK 25, opencode-base-core V1.0.0
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Suppliers Tests")
class SuppliersTest {

    // ==================== memoize() Tests ====================

    @Nested
    @DisplayName("memoize() Method")
    class MemoizeTests {

        @Test
        @DisplayName("Memoize calls delegate only once")
        void memoizeCallsOnce() {
            AtomicInteger callCount = new AtomicInteger(0);
            Supplier<String> memoized = Suppliers.memoize(() -> {
                callCount.incrementAndGet();
                return "value";
            });

            assertEquals("value", memoized.get());
            assertEquals("value", memoized.get());
            assertEquals("value", memoized.get());

            assertEquals(1, callCount.get());
        }

        @Test
        @DisplayName("Memoize returns same instance")
        void memoizeReturnsSameInstance() {
            Supplier<Object> memoized = Suppliers.memoize(Object::new);

            Object first = memoized.get();
            Object second = memoized.get();

            assertSame(first, second);
        }

        @Test
        @DisplayName("Memoize handles null value")
        void memoizeHandlesNull() {
            AtomicInteger callCount = new AtomicInteger(0);
            Supplier<String> memoized = Suppliers.memoize(() -> {
                callCount.incrementAndGet();
                return null;
            });

            assertNull(memoized.get());
            assertNull(memoized.get());

            assertEquals(1, callCount.get());
        }

        @Test
        @DisplayName("Memoize null delegate throws exception")
        void memoizeNullDelegate() {
            assertThrows(NullPointerException.class, () -> Suppliers.memoize(null));
        }

        @Test
        @DisplayName("Double memoize returns same supplier")
        void doubleMemoize() {
            Supplier<String> original = Suppliers.memoize(() -> "value");
            Supplier<String> doubleMemoized = Suppliers.memoize(original);

            assertSame(original, doubleMemoized);
        }

        @Test
        @DisplayName("Memoize is thread-safe")
        void memoizeThreadSafe() throws InterruptedException {
            AtomicInteger callCount = new AtomicInteger(0);
            Supplier<String> memoized = Suppliers.memoize(() -> {
                callCount.incrementAndGet();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "value";
            });

            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        memoized.get();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            assertEquals(1, callCount.get());
        }

        @Test
        @DisplayName("Memoize toString before computation")
        void memoizeToStringBeforeComputation() {
            Supplier<String> delegate = () -> "value";
            Supplier<String> memoized = Suppliers.memoize(delegate);

            String str = memoized.toString();
            assertTrue(str.contains("memoize"));
        }

        @Test
        @DisplayName("Memoize toString after computation")
        void memoizeToStringAfterComputation() {
            Supplier<String> memoized = Suppliers.memoize(() -> "computed");
            memoized.get();

            String str = memoized.toString();
            assertTrue(str.contains("computed"));
        }
    }

    // ==================== memoizeWithExpiration(TimeUnit) Tests ====================

    @Nested
    @DisplayName("memoizeWithExpiration(TimeUnit) Method")
    class MemoizeWithExpirationTimeUnitTests {

        @Test
        @DisplayName("Memoize with expiration caches value")
        void memoizeWithExpirationCaches() {
            AtomicInteger callCount = new AtomicInteger(0);
            Supplier<String> memoized = Suppliers.memoizeWithExpiration(() -> {
                callCount.incrementAndGet();
                return "value";
            }, 1, TimeUnit.SECONDS);

            assertEquals("value", memoized.get());
            assertEquals("value", memoized.get());

            assertEquals(1, callCount.get());
        }

        @Test
        @DisplayName("Memoize with expiration expires after duration")
        void memoizeWithExpirationExpires() throws InterruptedException {
            AtomicInteger callCount = new AtomicInteger(0);
            Supplier<Integer> memoized = Suppliers.memoizeWithExpiration(() -> {
                return callCount.incrementAndGet();
            }, 50, TimeUnit.MILLISECONDS);

            assertEquals(1, memoized.get());
            assertEquals(1, memoized.get());

            Thread.sleep(100);

            assertEquals(2, memoized.get());
            assertEquals(2, memoized.get());
        }

        @Test
        @DisplayName("Null delegate throws exception")
        void nullDelegateThrows() {
            assertThrows(NullPointerException.class,
                () -> Suppliers.memoizeWithExpiration(null, 1, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("Null unit throws exception")
        void nullUnitThrows() {
            assertThrows(NullPointerException.class,
                () -> Suppliers.memoizeWithExpiration(() -> "value", 1, null));
        }

        @Test
        @DisplayName("Zero duration throws exception")
        void zeroDurationThrows() {
            assertThrows(IllegalArgumentException.class,
                () -> Suppliers.memoizeWithExpiration(() -> "value", 0, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("Negative duration throws exception")
        void negativeDurationThrows() {
            assertThrows(IllegalArgumentException.class,
                () -> Suppliers.memoizeWithExpiration(() -> "value", -1, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("MemoizeWithExpiration toString")
        void memoizeWithExpirationToString() {
            Supplier<String> memoized = Suppliers.memoizeWithExpiration(
                () -> "value", 1, TimeUnit.SECONDS);

            String str = memoized.toString();
            assertTrue(str.contains("memoizeWithExpiration"));
            assertTrue(str.contains("ns"));
        }
    }

    // ==================== memoizeWithExpiration(Duration) Tests ====================

    @Nested
    @DisplayName("memoizeWithExpiration(Duration) Method")
    class MemoizeWithExpirationDurationTests {

        @Test
        @DisplayName("Memoize with Duration caches value")
        void memoizeWithDurationCaches() {
            AtomicInteger callCount = new AtomicInteger(0);
            Supplier<String> memoized = Suppliers.memoizeWithExpiration(() -> {
                callCount.incrementAndGet();
                return "value";
            }, Duration.ofSeconds(1));

            assertEquals("value", memoized.get());
            assertEquals("value", memoized.get());

            assertEquals(1, callCount.get());
        }

        @Test
        @DisplayName("Memoize with Duration expires")
        void memoizeWithDurationExpires() throws InterruptedException {
            AtomicInteger callCount = new AtomicInteger(0);
            Supplier<Integer> memoized = Suppliers.memoizeWithExpiration(() -> {
                return callCount.incrementAndGet();
            }, Duration.ofMillis(50));

            assertEquals(1, memoized.get());

            Thread.sleep(100);

            assertEquals(2, memoized.get());
        }

        @Test
        @DisplayName("Null duration throws exception")
        void nullDurationThrows() {
            assertThrows(NullPointerException.class,
                () -> Suppliers.memoizeWithExpiration(() -> "value", (Duration) null));
        }

        @Test
        @DisplayName("Zero duration throws exception")
        void zeroDurationThrows() {
            assertThrows(IllegalArgumentException.class,
                () -> Suppliers.memoizeWithExpiration(() -> "value", Duration.ZERO));
        }

        @Test
        @DisplayName("Negative duration throws exception")
        void negativeDurationThrows() {
            assertThrows(IllegalArgumentException.class,
                () -> Suppliers.memoizeWithExpiration(() -> "value", Duration.ofMillis(-100)));
        }
    }

    // ==================== compose() Tests ====================

    @Nested
    @DisplayName("compose() Method")
    class ComposeTests {

        @Test
        @DisplayName("Compose applies function to delegate")
        void composeAppliesFunction() {
            Supplier<String> composed = Suppliers.compose(
                Object::toString,
                () -> 42
            );

            assertEquals("42", composed.get());
        }

        @Test
        @DisplayName("Compose with uppercase function")
        void composeUppercase() {
            Supplier<String> composed = Suppliers.compose(
                String::toUpperCase,
                () -> "hello"
            );

            assertEquals("HELLO", composed.get());
        }

        @Test
        @DisplayName("Compose evaluates lazily each time")
        void composeLazyEvaluation() {
            AtomicInteger counter = new AtomicInteger(0);
            Supplier<Integer> composed = Suppliers.compose(
                x -> x + 1,
                counter::incrementAndGet
            );

            assertEquals(2, composed.get());
            assertEquals(3, composed.get());
            assertEquals(4, composed.get());
        }

        @Test
        @DisplayName("Null function throws exception")
        void nullFunctionThrows() {
            assertThrows(NullPointerException.class,
                () -> Suppliers.compose(null, () -> "value"));
        }

        @Test
        @DisplayName("Null delegate throws exception")
        void nullDelegateThrows() {
            assertThrows(NullPointerException.class,
                () -> Suppliers.compose(Object::toString, null));
        }

        @Test
        @DisplayName("Compose with memoize for caching")
        void composeWithMemoize() {
            AtomicInteger counter = new AtomicInteger(0);
            Supplier<Integer> composed = Suppliers.memoize(
                Suppliers.compose(
                    x -> x * 2,
                    counter::incrementAndGet
                )
            );

            assertEquals(2, composed.get());
            assertEquals(2, composed.get());
            assertEquals(2, composed.get());

            assertEquals(1, counter.get());
        }
    }

    // ==================== synchronizedSupplier() Tests ====================

    @Nested
    @DisplayName("synchronizedSupplier() Method")
    class SynchronizedSupplierTests {

        @Test
        @DisplayName("Synchronized supplier returns value")
        void synchronizedReturnsValue() {
            Supplier<String> sync = Suppliers.synchronizedSupplier(() -> "value");
            assertEquals("value", sync.get());
        }

        @Test
        @DisplayName("Null delegate throws exception")
        void nullDelegateThrows() {
            assertThrows(NullPointerException.class,
                () -> Suppliers.synchronizedSupplier(null));
        }

        @Test
        @DisplayName("Synchronized is thread-safe")
        void synchronizedIsThreadSafe() throws InterruptedException {
            AtomicInteger sharedCounter = new AtomicInteger(0);
            Supplier<Integer> sync = Suppliers.synchronizedSupplier(() -> {
                int current = sharedCounter.get();
                // Simulate some work
                Thread.yield();
                return sharedCounter.incrementAndGet();
            });

            int threadCount = 100;
            ExecutorService executor = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        sync.get();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            assertEquals(threadCount, sharedCounter.get());
        }

        @Test
        @DisplayName("Synchronized toString")
        void synchronizedToString() {
            Supplier<String> sync = Suppliers.synchronizedSupplier(() -> "value");
            String str = sync.toString();
            assertTrue(str.contains("synchronizedSupplier"));
        }
    }

    // ==================== ofInstance() Tests ====================

    @Nested
    @DisplayName("ofInstance() Method")
    class OfInstanceTests {

        @Test
        @DisplayName("ofInstance returns the instance")
        void ofInstanceReturnsInstance() {
            String instance = "test";
            Supplier<String> supplier = Suppliers.ofInstance(instance);

            assertSame(instance, supplier.get());
            assertSame(instance, supplier.get());
        }

        @Test
        @DisplayName("ofInstance with null")
        void ofInstanceWithNull() {
            Supplier<String> supplier = Suppliers.ofInstance(null);

            assertNull(supplier.get());
            assertNull(supplier.get());
        }

        @Test
        @DisplayName("ofInstance returns same instance every time")
        void ofInstanceSameInstance() {
            Object obj = new Object();
            Supplier<Object> supplier = Suppliers.ofInstance(obj);

            for (int i = 0; i < 10; i++) {
                assertSame(obj, supplier.get());
            }
        }
    }

    // ==================== Integration Tests ====================

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Chain memoize and compose")
        void chainMemoizeAndCompose() {
            AtomicInteger callCount = new AtomicInteger(0);

            Supplier<String> composed = Suppliers.compose(
                n -> "Value-" + n,
                callCount::incrementAndGet
            );
            Supplier<String> memoized = Suppliers.memoize(composed);

            assertEquals("Value-1", memoized.get());
            assertEquals("Value-1", memoized.get());
            assertEquals("Value-1", memoized.get());

            assertEquals(1, callCount.get());
        }

        @Test
        @DisplayName("Synchronized memoized supplier")
        void synchronizedMemoized() {
            AtomicInteger callCount = new AtomicInteger(0);

            Supplier<Integer> supplier = Suppliers.synchronizedSupplier(
                Suppliers.memoize(callCount::incrementAndGet)
            );

            assertEquals(1, supplier.get());
            assertEquals(1, supplier.get());

            assertEquals(1, callCount.get());
        }

        @Test
        @DisplayName("Complex chain")
        void complexChain() throws InterruptedException {
            AtomicInteger callCount = new AtomicInteger(0);

            // Create a supplier that:
            // 1. Increments counter
            // 2. Multiplies by 10
            // 3. Memoizes with short expiration
            Supplier<Integer> supplier = Suppliers.memoizeWithExpiration(
                Suppliers.compose(
                    n -> n * 10,
                    callCount::incrementAndGet
                ),
                Duration.ofMillis(50)
            );

            assertEquals(10, supplier.get());
            assertEquals(10, supplier.get());
            assertEquals(1, callCount.get());

            Thread.sleep(100);

            assertEquals(20, supplier.get());
            assertEquals(2, callCount.get());
        }
    }

    // ==================== Edge Cases ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Memoize with exception in delegate")
        void memoizeWithException() {
            AtomicInteger callCount = new AtomicInteger(0);
            Supplier<String> memoized = Suppliers.memoize(() -> {
                callCount.incrementAndGet();
                throw new RuntimeException("Test exception");
            });

            assertThrows(RuntimeException.class, memoized::get);
            assertThrows(RuntimeException.class, memoized::get);

            // Exception is not cached, delegate is called each time
            assertEquals(2, callCount.get());
        }

        @Test
        @DisplayName("MemoizeWithExpiration with very short duration")
        void memoizeWithVeryShortDuration() throws InterruptedException {
            AtomicInteger callCount = new AtomicInteger(0);
            Supplier<Integer> memoized = Suppliers.memoizeWithExpiration(
                callCount::incrementAndGet,
                1, TimeUnit.NANOSECONDS
            );

            memoized.get();
            Thread.sleep(1);
            memoized.get();

            assertTrue(callCount.get() >= 1);
        }

        @Test
        @DisplayName("MemoizeWithExpiration with long duration")
        void memoizeWithLongDuration() {
            AtomicInteger callCount = new AtomicInteger(0);
            Supplier<Integer> memoized = Suppliers.memoizeWithExpiration(
                callCount::incrementAndGet,
                1, TimeUnit.HOURS
            );

            assertEquals(1, memoized.get());
            assertEquals(1, memoized.get());
            assertEquals(1, callCount.get());
        }
    }
}
