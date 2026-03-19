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

package cloud.opencode.base.cache.jmx;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.OpenCache;
import org.junit.jupiter.api.*;

import javax.management.ObjectName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CacheJmxRegistration
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheJmxRegistration Tests")
class CacheJmxRegistrationTest {

    private Cache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .recordStats()
                .build("jmx-test-" + System.nanoTime());
    }

    @AfterEach
    void tearDown() {
        CacheJmxRegistration.unregisterAll();
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("register with default domain")
        void registerWithDefaultDomain() {
            ObjectName name = CacheJmxRegistration.register(cache);

            assertNotNull(name);
            assertTrue(name.toString().contains("cloud.opencode.base.cache"));
            assertTrue(CacheJmxRegistration.isRegistered(cache.name()));
        }

        @Test
        @DisplayName("register with custom domain")
        void registerWithCustomDomain() {
            ObjectName name = CacheJmxRegistration.register(cache, "com.example.cache");

            assertNotNull(name);
            assertTrue(name.toString().contains("com.example.cache"));
        }

        @Test
        @DisplayName("register throws on null cache")
        void registerThrowsOnNullCache() {
            assertThrows(NullPointerException.class, () ->
                    CacheJmxRegistration.register(null));
        }

        @Test
        @DisplayName("register throws on null domain")
        void registerThrowsOnNullDomain() {
            assertThrows(NullPointerException.class, () ->
                    CacheJmxRegistration.register(cache, null));
        }

        @Test
        @DisplayName("register replaces existing registration")
        void registerReplacesExistingRegistration() {
            ObjectName name1 = CacheJmxRegistration.register(cache);
            ObjectName name2 = CacheJmxRegistration.register(cache);

            assertEquals(name1, name2);
        }
    }

    @Nested
    @DisplayName("Unregistration Tests")
    class UnregistrationTests {

        @Test
        @DisplayName("unregister by cache removes registration")
        void unregisterByCacheRemovesRegistration() {
            CacheJmxRegistration.register(cache);

            CacheJmxRegistration.unregister(cache);

            assertFalse(CacheJmxRegistration.isRegistered(cache.name()));
        }

        @Test
        @DisplayName("unregister by name removes registration")
        void unregisterByNameRemovesRegistration() {
            CacheJmxRegistration.register(cache);

            CacheJmxRegistration.unregister(cache.name());

            assertFalse(CacheJmxRegistration.isRegistered(cache.name()));
        }

        @Test
        @DisplayName("unregister non-existent does not throw")
        void unregisterNonExistentDoesNotThrow() {
            assertDoesNotThrow(() -> CacheJmxRegistration.unregister("non-existent"));
        }

        @Test
        @DisplayName("unregisterAll removes all registrations")
        void unregisterAllRemovesAllRegistrations() {
            Cache<String, String> cache2 = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .build("jmx-test-2-" + System.nanoTime());

            CacheJmxRegistration.register(cache);
            CacheJmxRegistration.register(cache2);

            CacheJmxRegistration.unregisterAll();

            assertFalse(CacheJmxRegistration.isRegistered(cache.name()));
            assertFalse(CacheJmxRegistration.isRegistered(cache2.name()));
        }
    }

    @Nested
    @DisplayName("Query Tests")
    class QueryTests {

        @Test
        @DisplayName("isRegistered returns true for registered cache")
        void isRegisteredReturnsTrueForRegisteredCache() {
            CacheJmxRegistration.register(cache);

            assertTrue(CacheJmxRegistration.isRegistered(cache.name()));
        }

        @Test
        @DisplayName("isRegistered returns false for non-registered cache")
        void isRegisteredReturnsFalseForNonRegisteredCache() {
            assertFalse(CacheJmxRegistration.isRegistered("non-existent"));
        }
    }
}
