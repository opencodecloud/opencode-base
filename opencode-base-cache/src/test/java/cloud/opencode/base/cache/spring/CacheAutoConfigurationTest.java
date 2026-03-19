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

package cloud.opencode.base.cache.spring;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CacheAutoConfiguration
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheAutoConfiguration Tests")
class CacheAutoConfigurationTest {

    @Nested
    @DisplayName("createCacheManager Tests")
    class CreateCacheManagerTests {

        @Test
        @DisplayName("createCacheManager creates manager from properties")
        void createCacheManagerCreatesManagerFromProperties() {
            CacheProperties properties = new CacheProperties();
            properties.setEnabled(true);

            OpenCodeCacheManager manager = CacheAutoConfiguration.createCacheManager(properties);

            assertNotNull(manager);
        }

        @Test
        @DisplayName("createCacheManager creates manager when disabled")
        void createCacheManagerCreatesManagerWhenDisabled() {
            CacheProperties properties = new CacheProperties();
            properties.setEnabled(false);

            OpenCodeCacheManager manager = CacheAutoConfiguration.createCacheManager(properties);

            // When disabled, creates a basic manager via OpenCodeCacheManager.create()
            assertNotNull(manager);
        }

        @Test
        @DisplayName("createCacheManager with validation validates properties")
        void createCacheManagerWithValidationValidatesProperties() {
            CacheProperties properties = new CacheProperties();
            properties.getDefaultSpec().setMaximumSize(-1);

            assertThrows(IllegalArgumentException.class, () ->
                    CacheAutoConfiguration.createCacheManager(properties, true));
        }

        @Test
        @DisplayName("createCacheManager without validation skips validation")
        void createCacheManagerWithoutValidationSkipsValidation() {
            CacheProperties properties = new CacheProperties();
            properties.setEnabled(false);
            // Even if invalid, should not throw when validation is disabled
            properties.getDefaultSpec().setMaximumSize(-1);

            OpenCodeCacheManager manager = CacheAutoConfiguration.createCacheManager(properties, false);
            assertNotNull(manager);
        }

        @Test
        @DisplayName("createCacheManager applies default configuration")
        void createCacheManagerAppliesDefaultConfiguration() {
            CacheProperties properties = new CacheProperties();
            properties.setEnabled(true);
            properties.getDefaultSpec().setMaximumSize(5000);
            properties.getDefaultSpec().setExpireAfterWrite(Duration.ofMinutes(30));
            properties.getDefaultSpec().setRecordStats(true);

            OpenCodeCacheManager manager = CacheAutoConfiguration.createCacheManager(properties);

            assertNotNull(manager);
        }

        @Test
        @DisplayName("createCacheManager applies named cache configurations")
        void createCacheManagerAppliesNamedCacheConfigurations() {
            CacheProperties properties = new CacheProperties();
            properties.setEnabled(true);

            CacheProperties.CacheSpec usersSpec = new CacheProperties.CacheSpec();
            usersSpec.setMaximumSize(1000);
            usersSpec.setExpireAfterWrite(Duration.ofHours(1));
            properties.getCaches().put("users", usersSpec);

            CacheProperties.CacheSpec productsSpec = new CacheProperties.CacheSpec();
            productsSpec.setMaximumSize(5000);
            productsSpec.setExpireAfterAccess(Duration.ofMinutes(10));
            properties.getCaches().put("products", productsSpec);

            OpenCodeCacheManager manager = CacheAutoConfiguration.createCacheManager(properties);

            assertNotNull(manager);
            assertTrue(manager.getCacheNames().contains("users"));
            assertTrue(manager.getCacheNames().contains("products"));
        }

        @Test
        @DisplayName("createCacheManager applies allowNullValues")
        void createCacheManagerAppliesAllowNullValues() {
            CacheProperties properties = new CacheProperties();
            properties.setEnabled(true);
            properties.setAllowNullValues(false);

            OpenCodeCacheManager manager = CacheAutoConfiguration.createCacheManager(properties);

            assertNotNull(manager);
        }

        @Test
        @DisplayName("createCacheManager applies eviction policies")
        void createCacheManagerAppliesEvictionPolicies() {
            // Test LRU policy
            CacheProperties lruProps = new CacheProperties();
            lruProps.setEnabled(true);
            CacheProperties.CacheSpec lruSpec = new CacheProperties.CacheSpec();
            lruSpec.setEvictionPolicy("lru");
            lruProps.getCaches().put("lru-cache", lruSpec);
            assertNotNull(CacheAutoConfiguration.createCacheManager(lruProps));

            // Test LFU policy
            CacheProperties lfuProps = new CacheProperties();
            lfuProps.setEnabled(true);
            CacheProperties.CacheSpec lfuSpec = new CacheProperties.CacheSpec();
            lfuSpec.setEvictionPolicy("lfu");
            lfuProps.getCaches().put("lfu-cache", lfuSpec);
            assertNotNull(CacheAutoConfiguration.createCacheManager(lfuProps));

            // Test FIFO policy
            CacheProperties fifoProps = new CacheProperties();
            fifoProps.setEnabled(true);
            CacheProperties.CacheSpec fifoSpec = new CacheProperties.CacheSpec();
            fifoSpec.setEvictionPolicy("fifo");
            fifoProps.getCaches().put("fifo-cache", fifoSpec);
            assertNotNull(CacheAutoConfiguration.createCacheManager(fifoProps));

            // Test W-TinyLFU policy
            CacheProperties wtinylfuProps = new CacheProperties();
            wtinylfuProps.setEnabled(true);
            CacheProperties.CacheSpec wtinylfuSpec = new CacheProperties.CacheSpec();
            wtinylfuSpec.setEvictionPolicy("wtinylfu");
            wtinylfuProps.getCaches().put("wtinylfu-cache", wtinylfuSpec);
            assertNotNull(CacheAutoConfiguration.createCacheManager(wtinylfuProps));

            // Test W-TinyLFU with dash
            CacheProperties wtinylfuDashProps = new CacheProperties();
            wtinylfuDashProps.setEnabled(true);
            CacheProperties.CacheSpec wtinylfuDashSpec = new CacheProperties.CacheSpec();
            wtinylfuDashSpec.setEvictionPolicy("w-tinylfu");
            wtinylfuDashProps.getCaches().put("wtinylfu-dash-cache", wtinylfuDashSpec);
            assertNotNull(CacheAutoConfiguration.createCacheManager(wtinylfuDashProps));
        }

        @Test
        @DisplayName("createCacheManager applies initialCapacity")
        void createCacheManagerAppliesInitialCapacity() {
            CacheProperties properties = new CacheProperties();
            properties.setEnabled(true);

            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();
            spec.setInitialCapacity(64);
            properties.getCaches().put("capacity-cache", spec);

            OpenCodeCacheManager manager = CacheAutoConfiguration.createCacheManager(properties);

            assertNotNull(manager);
            assertTrue(manager.getCacheNames().contains("capacity-cache"));
        }

        @Test
        @DisplayName("createCacheManager applies refreshAfterWrite with expireAfterWrite")
        void createCacheManagerAppliesRefreshAfterWrite() {
            CacheProperties properties = new CacheProperties();
            properties.setEnabled(true);

            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();
            spec.setExpireAfterWrite(Duration.ofMinutes(30)); // Required for refresh
            spec.setRefreshAfterWrite(Duration.ofMinutes(5));
            properties.getCaches().put("refresh-cache", spec);

            OpenCodeCacheManager manager = CacheAutoConfiguration.createCacheManager(properties);

            assertNotNull(manager);
            assertTrue(manager.getCacheNames().contains("refresh-cache"));
        }

        @Test
        @DisplayName("createCacheManager handles unknown eviction policy")
        void createCacheManagerHandlesUnknownEvictionPolicy() {
            CacheProperties properties = new CacheProperties();
            properties.setEnabled(true);

            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();
            spec.setEvictionPolicy("unknown-policy");
            properties.getCaches().put("cache", spec);

            // Should not throw, defaults to LRU
            OpenCodeCacheManager manager = CacheAutoConfiguration.createCacheManager(properties, false);
            assertNotNull(manager);
        }
    }

    @Nested
    @DisplayName("ConfigurationFactory Tests")
    class ConfigurationFactoryTests {

        @Test
        @DisplayName("create creates manager with consumer")
        void createCreatesManagerWithConsumer() {
            OpenCodeCacheManager manager = CacheAutoConfiguration.ConfigurationFactory.create(props -> {
                props.setEnabled(true);
                props.getDefaultSpec().setMaximumSize(5000);
            });

            assertNotNull(manager);
        }

        @Test
        @DisplayName("createSimple creates manager with basic settings")
        void createSimpleCreatesManagerWithBasicSettings() {
            OpenCodeCacheManager manager = CacheAutoConfiguration.ConfigurationFactory.createSimple(
                    1000, Duration.ofMinutes(10));

            assertNotNull(manager);
        }

        @Test
        @DisplayName("createWithCaches creates manager with named caches")
        void createWithCachesCreatesManagerWithNamedCaches() {
            OpenCodeCacheManager manager = CacheAutoConfiguration.ConfigurationFactory.createWithCaches(
                    "users", "products", "sessions");

            assertNotNull(manager);
            assertTrue(manager.getCacheNames().contains("users"));
            assertTrue(manager.getCacheNames().contains("products"));
            assertTrue(manager.getCacheNames().contains("sessions"));
        }

        @Test
        @DisplayName("createWithCaches with no caches creates manager")
        void createWithCachesWithNoCachesCreatesManager() {
            OpenCodeCacheManager manager = CacheAutoConfiguration.ConfigurationFactory.createWithCaches();

            // Creates a manager even with no cache names
            assertNotNull(manager);
        }
    }

    @Nested
    @DisplayName("META-INF Configuration Tests")
    class MetaInfConfigurationTests {

        @Test
        @DisplayName("getAutoConfigurationClassName returns correct class name")
        void getAutoConfigurationClassNameReturnsCorrectClassName() {
            String className = CacheAutoConfiguration.getAutoConfigurationClassName();

            assertEquals("cloud.opencode.base.cache.spring.CacheAutoConfiguration", className);
        }

        @Test
        @DisplayName("getPropertiesClassName returns correct class name")
        void getPropertiesClassNameReturnsCorrectClassName() {
            String className = CacheAutoConfiguration.getPropertiesClassName();

            assertEquals("cloud.opencode.base.cache.spring.CacheProperties", className);
        }

        @Test
        @DisplayName("getCacheManagerClassName returns correct class name")
        void getCacheManagerClassNameReturnsCorrectClassName() {
            String className = CacheAutoConfiguration.getCacheManagerClassName();

            assertEquals("cloud.opencode.base.cache.spring.OpenCodeCacheManager", className);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("createCacheManager with null defaultSpec")
        void createCacheManagerWithNullDefaultSpec() {
            CacheProperties properties = new CacheProperties();
            properties.setEnabled(true);
            properties.setDefaultSpec(null);

            OpenCodeCacheManager manager = CacheAutoConfiguration.createCacheManager(properties, false);

            assertNotNull(manager);
        }

        @Test
        @DisplayName("createCacheManager with empty caches map")
        void createCacheManagerWithEmptyCachesMap() {
            CacheProperties properties = new CacheProperties();
            properties.setEnabled(true);
            properties.getCaches().clear();

            OpenCodeCacheManager manager = CacheAutoConfiguration.createCacheManager(properties);

            assertNotNull(manager);
        }

        @Test
        @DisplayName("createCacheManager applies multiple spec properties")
        void createCacheManagerAppliesMultipleSpecProperties() {
            CacheProperties properties = new CacheProperties();
            properties.setEnabled(true);

            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();
            spec.setMaximumSize(1000);
            spec.setExpireAfterWrite(Duration.ofMinutes(30));
            spec.setExpireAfterAccess(Duration.ofMinutes(10));
            spec.setRefreshAfterWrite(Duration.ofMinutes(5)); // Valid since expireAfterWrite is set
            spec.setInitialCapacity(32);
            spec.setRecordStats(true);
            spec.setEvictionPolicy("lfu");
            properties.getCaches().put("full-spec-cache", spec);

            OpenCodeCacheManager manager = CacheAutoConfiguration.createCacheManager(properties);

            assertNotNull(manager);
            assertTrue(manager.getCacheNames().contains("full-spec-cache"));
        }
    }
}
