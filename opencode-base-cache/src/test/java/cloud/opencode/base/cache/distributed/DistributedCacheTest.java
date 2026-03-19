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

package cloud.opencode.base.cache.distributed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for DistributedCache interface
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("DistributedCache Interface Tests")
class DistributedCacheTest {

    @Nested
    @DisplayName("ScanResult Record Tests")
    class ScanResultTests {

        @Test
        @DisplayName("ScanResult record accessors work")
        void scanResultRecordAccessorsWork() {
            Set<String> keys = Set.of("key1", "key2");
            DistributedCache.ScanResult<String> result = new DistributedCache.ScanResult<>(keys, "cursor123", false);

            assertEquals(keys, result.keys());
            assertEquals("cursor123", result.nextCursor());
            assertFalse(result.finished());
        }

        @Test
        @DisplayName("ScanResult finished flag works")
        void scanResultFinishedFlagWorks() {
            DistributedCache.ScanResult<String> result = new DistributedCache.ScanResult<>(Set.of(), "", true);

            assertTrue(result.finished());
        }

        @Test
        @DisplayName("ScanResult equality works")
        void scanResultEqualityWorks() {
            Set<String> keys = Set.of("key1");
            DistributedCache.ScanResult<String> result1 = new DistributedCache.ScanResult<>(keys, "cursor", true);
            DistributedCache.ScanResult<String> result2 = new DistributedCache.ScanResult<>(keys, "cursor", true);

            assertEquals(result1, result2);
            assertEquals(result1.hashCode(), result2.hashCode());
        }
    }

    @Nested
    @DisplayName("DistributedLock Interface Tests")
    class DistributedLockTests {

        @Test
        @DisplayName("DistributedLock interface can be implemented")
        void distributedLockInterfaceCanBeImplemented() {
            DistributedCache.DistributedLock lock = new DistributedCache.DistributedLock() {
                @Override
                public Object key() {
                    return "lock-key";
                }

                @Override
                public String token() {
                    return "token-123";
                }

                @Override
                public boolean extend(Duration ttl) {
                    return true;
                }

                @Override
                public void close() {
                    // Release lock
                }
            };

            assertEquals("lock-key", lock.key());
            assertEquals("token-123", lock.token());
            assertTrue(lock.extend(Duration.ofSeconds(30)));
            assertDoesNotThrow(lock::close);
        }
    }

    @Nested
    @DisplayName("Subscription Interface Tests")
    class SubscriptionTests {

        @Test
        @DisplayName("Subscription interface can be implemented")
        void subscriptionInterfaceCanBeImplemented() {
            DistributedCache.Subscription subscription = new DistributedCache.Subscription() {
                private boolean active = true;

                @Override
                public String channel() {
                    return "cache:invalidate";
                }

                @Override
                public boolean isActive() {
                    return active;
                }

                @Override
                public void close() {
                    active = false;
                }
            };

            assertEquals("cache:invalidate", subscription.channel());
            assertTrue(subscription.isActive());
            subscription.close();
            assertFalse(subscription.isActive());
        }
    }

    @Nested
    @DisplayName("Default Method Tests")
    class DefaultMethodTests {

        @Test
        @DisplayName("decrement uses increment with negative delta")
        void decrementUsesIncrementWithNegativeDelta() {
            TestDistributedCache cache = new TestDistributedCache();

            long result = cache.decrement("counter", 5);

            assertEquals(-5, result);
            assertEquals(-5, cache.lastIncrementDelta);
        }
    }

    @Nested
    @DisplayName("Mock Implementation Tests")
    class MockImplementationTests {

        @Test
        @DisplayName("mock implementation works for basic operations")
        void mockImplementationWorksForBasicOperations() {
            TestDistributedCache cache = new TestDistributedCache();

            cache.put("key1", "value1");
            Optional<String> result = cache.get("key1");

            assertTrue(result.isPresent());
            assertEquals("value1", result.get());
        }

        @Test
        @DisplayName("mock implementation works for getOrLoad")
        void mockImplementationWorksForGetOrLoad() {
            TestDistributedCache cache = new TestDistributedCache();

            String result = cache.getOrLoad("key1", k -> "loaded-" + k, Duration.ofMinutes(5));

            assertEquals("loaded-key1", result);
            assertTrue(cache.get("key1").isPresent());
        }

        @Test
        @DisplayName("mock implementation works for putIfAbsent")
        void mockImplementationWorksForPutIfAbsent() {
            TestDistributedCache cache = new TestDistributedCache();

            boolean first = cache.putIfAbsent("key1", "value1", Duration.ofMinutes(5));
            boolean second = cache.putIfAbsent("key1", "value2", Duration.ofMinutes(5));

            assertTrue(first);
            assertFalse(second);
            assertEquals("value1", cache.get("key1").orElse(null));
        }
    }

    /**
     * Test implementation of DistributedCache for testing purposes
     */
    private static class TestDistributedCache implements DistributedCache<String, String> {

        private final Map<String, String> store = new HashMap<>();
        long lastIncrementDelta;

        @Override
        public Optional<String> get(String key) {
            return Optional.ofNullable(store.get(key));
        }

        @Override
        public String getOrLoad(String key, Function<String, String> loader, Duration ttl) {
            return store.computeIfAbsent(key, loader);
        }

        @Override
        public Map<String, String> getAll(Collection<String> keys) {
            Map<String, String> result = new HashMap<>();
            for (String key : keys) {
                String value = store.get(key);
                if (value != null) {
                    result.put(key, value);
                }
            }
            return result;
        }

        @Override
        public void put(String key, String value) {
            store.put(key, value);
        }

        @Override
        public void put(String key, String value, Duration ttl) {
            store.put(key, value);
        }

        @Override
        public void putAll(Map<String, String> entries) {
            store.putAll(entries);
        }

        @Override
        public void putAll(Map<String, String> entries, Duration ttl) {
            store.putAll(entries);
        }

        @Override
        public boolean putIfAbsent(String key, String value, Duration ttl) {
            if (store.containsKey(key)) {
                return false;
            }
            store.put(key, value);
            return true;
        }

        @Override
        public boolean remove(String key) {
            return store.remove(key) != null;
        }

        @Override
        public long removeAll(Collection<String> keys) {
            long count = 0;
            for (String key : keys) {
                if (store.remove(key) != null) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public boolean exists(String key) {
            return store.containsKey(key);
        }

        @Override
        public Optional<Duration> getTtl(String key) {
            return store.containsKey(key) ? Optional.of(Duration.ofHours(1)) : Optional.empty();
        }

        @Override
        public boolean setTtl(String key, Duration ttl) {
            return store.containsKey(key);
        }

        @Override
        public CompletableFuture<Optional<String>> getAsync(String key) {
            return CompletableFuture.completedFuture(get(key));
        }

        @Override
        public CompletableFuture<Map<String, String>> getAllAsync(Collection<String> keys) {
            return CompletableFuture.completedFuture(getAll(keys));
        }

        @Override
        public CompletableFuture<Void> putAsync(String key, String value, Duration ttl) {
            put(key, value, ttl);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Boolean> removeAsync(String key) {
            return CompletableFuture.completedFuture(remove(key));
        }

        @Override
        public long increment(String key, long delta) {
            lastIncrementDelta = delta;
            long current = Long.parseLong(store.getOrDefault(key, "0"));
            long newValue = current + delta;
            store.put(key, String.valueOf(newValue));
            return newValue;
        }

        @Override
        public boolean compareAndSwap(String key, String expectedValue, String newValue, Duration ttl) {
            String current = store.get(key);
            if (Objects.equals(current, expectedValue)) {
                store.put(key, newValue);
                return true;
            }
            return false;
        }

        @Override
        public Set<String> keys(String pattern) {
            return store.keySet();
        }

        @Override
        public ScanResult<String> scan(String pattern, String cursor, int count) {
            return new ScanResult<>(store.keySet(), "", true);
        }

        @Override
        public long removeByPattern(String pattern) {
            long count = store.size();
            store.clear();
            return count;
        }

        @Override
        public Optional<DistributedLock> tryLock(String lockKey, Duration ttl) {
            return Optional.empty();
        }

        @Override
        public Optional<DistributedLock> lock(String lockKey, Duration ttl, Duration waitTime) {
            return Optional.empty();
        }

        @Override
        public void publish(String channel, String message) {
            // No-op
        }

        @Override
        public Subscription subscribe(String channel, Consumer<String> handler) {
            return new Subscription() {
                @Override
                public String channel() {
                    return channel;
                }

                @Override
                public boolean isActive() {
                    return false;
                }

                @Override
                public void close() {
                    // No-op
                }
            };
        }

        @Override
        public DistributedCacheStats stats() {
            return DistributedCacheStats.empty();
        }

        @Override
        public boolean isHealthy() {
            return true;
        }

        @Override
        public String name() {
            return "test-distributed-cache";
        }

        @Override
        public void close() {
            store.clear();
        }
    }
}
