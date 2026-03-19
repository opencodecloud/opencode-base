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

package cloud.opencode.base.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CacheDecorators class
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheDecorators Tests")
class CacheDecoratorsTest {

    private Cache<String, String> baseCache;

    @BeforeEach
    void setUp() {
        baseCache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .build();
    }

    @Nested
    @DisplayName("ChainBuilder Tests")
    class ChainBuilderTests {

        @Test
        @DisplayName("chain() creates builder")
        void chainCreatesBuilder() {
            CacheDecorators.ChainBuilder<String, String> builder = CacheDecorators.chain(baseCache);
            assertNotNull(builder);
        }

        @Test
        @DisplayName("chain() with null throws exception")
        void chainWithNullThrows() {
            assertThrows(NullPointerException.class, () -> CacheDecorators.chain(null));
        }

        @Test
        @DisplayName("build() returns cache")
        void buildReturnsCache() {
            Cache<String, String> result = CacheDecorators.chain(baseCache).build();
            assertNotNull(result);
        }

        @Test
        @DisplayName("withTimeout adds timeout behavior")
        void withTimeoutAddsTimeout() {
            Cache<String, String> decorated = CacheDecorators.chain(baseCache)
                    .withTimeout(Duration.ofSeconds(5))
                    .build();
            assertNotNull(decorated);
            assertInstanceOf(TimeoutCache.class, decorated);
        }

        @Test
        @DisplayName("withCopyOnRead adds copy behavior")
        void withCopyOnReadAddsCopy() {
            Cache<String, String> decorated = CacheDecorators.chain(baseCache)
                    .withCopyOnRead()
                    .build();
            assertNotNull(decorated);
        }

        @Test
        @DisplayName("withCopyOnRead with copier")
        void withCopyOnReadWithCopier() {
            Cache<String, String> decorated = CacheDecorators.chain(baseCache)
                    .withCopyOnRead(s -> s + "-copy")
                    .build();
            assertNotNull(decorated);
            decorated.put("key", "value");
            String result = decorated.get("key");
            assertEquals("value-copy", result);
        }

        @Test
        @DisplayName("withProtection adds protection")
        void withProtectionAddsProtection() {
            Cache<String, String> decorated = CacheDecorators.chain(baseCache)
                    .withProtection()
                    .build();
            assertNotNull(decorated);
        }

        @Test
        @DisplayName("custom decorator with with()")
        void customDecoratorWithWith() {
            Cache<String, String> decorated = CacheDecorators.chain(baseCache)
                    .with(cache -> NullSafeCache.wrap(cache))
                    .build();
            assertNotNull(decorated);
            assertInstanceOf(NullSafeCache.class, decorated);
        }

        @Test
        @DisplayName("multiple decorators chain correctly")
        void multipleDecoratorsChain() {
            Cache<String, String> decorated = CacheDecorators.chain(baseCache)
                    .withTimeout(Duration.ofSeconds(5))
                    .withCopyOnRead(s -> s)
                    .build();
            assertNotNull(decorated);
        }
    }

    @Nested
    @DisplayName("Decorated Cache Operations")
    class DecoratedOperationsTests {

        @Test
        @DisplayName("decorated cache put and get")
        void decoratedPutAndGet() {
            Cache<String, String> decorated = CacheDecorators.chain(baseCache)
                    .withTimeout(Duration.ofSeconds(5))
                    .build();

            decorated.put("key", "value");
            assertEquals("value", decorated.get("key"));
        }

        @Test
        @DisplayName("decorated cache invalidate")
        void decoratedInvalidate() {
            Cache<String, String> decorated = CacheDecorators.chain(baseCache)
                    .withTimeout(Duration.ofSeconds(5))
                    .build();

            decorated.put("key", "value");
            decorated.invalidate("key");
            assertNull(decorated.get("key"));
        }

        @Test
        @DisplayName("decorated cache stats")
        void decoratedStats() {
            Cache<String, String> decorated = CacheDecorators.chain(baseCache)
                    .withTimeout(Duration.ofSeconds(5))
                    .build();

            CacheStats stats = decorated.stats();
            assertNotNull(stats);
        }
    }
}
