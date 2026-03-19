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

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CacheProperties
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheProperties Tests")
class CachePropertiesTest {

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("default enabled is true")
        void defaultEnabledIsTrue() {
            CacheProperties props = new CacheProperties();

            assertTrue(props.isEnabled());
        }

        @Test
        @DisplayName("default allowNullValues is true")
        void defaultAllowNullValuesIsTrue() {
            CacheProperties props = new CacheProperties();

            assertTrue(props.isAllowNullValues());
        }

        @Test
        @DisplayName("default spec has expected values")
        void defaultSpecHasExpectedValues() {
            CacheProperties props = new CacheProperties();
            CacheProperties.CacheSpec spec = props.getDefaultSpec();

            assertNotNull(spec);
            assertEquals(10000, spec.getMaximumSize());
            assertEquals(-1, spec.getMaximumWeight());
            assertEquals(16, spec.getInitialCapacity());
            assertEquals(16, spec.getConcurrencyLevel());
            assertFalse(spec.isRecordStats());
            assertTrue(spec.isUseVirtualThreads());
            assertEquals("lru", spec.getEvictionPolicy());
            assertFalse(spec.isMetricsEnabled());
        }

        @Test
        @DisplayName("default caches is empty")
        void defaultCachesIsEmpty() {
            CacheProperties props = new CacheProperties();

            assertTrue(props.getCaches().isEmpty());
        }

        @Test
        @DisplayName("PREFIX constant is correct")
        void prefixConstantIsCorrect() {
            assertEquals("opencode.cache", CacheProperties.PREFIX);
        }
    }

    @Nested
    @DisplayName("Setters Tests")
    class SettersTests {

        @Test
        @DisplayName("setEnabled works")
        void setEnabledWorks() {
            CacheProperties props = new CacheProperties();

            props.setEnabled(false);

            assertFalse(props.isEnabled());
        }

        @Test
        @DisplayName("setAllowNullValues works")
        void setAllowNullValuesWorks() {
            CacheProperties props = new CacheProperties();

            props.setAllowNullValues(false);

            assertFalse(props.isAllowNullValues());
        }

        @Test
        @DisplayName("setDefaultSpec works")
        void setDefaultSpecWorks() {
            CacheProperties props = new CacheProperties();
            CacheProperties.CacheSpec newSpec = new CacheProperties.CacheSpec();
            newSpec.setMaximumSize(5000);

            props.setDefaultSpec(newSpec);

            assertEquals(5000, props.getDefaultSpec().getMaximumSize());
        }

        @Test
        @DisplayName("setCaches works")
        void setCachesWorks() {
            CacheProperties props = new CacheProperties();
            Map<String, CacheProperties.CacheSpec> caches = new LinkedHashMap<>();
            caches.put("users", new CacheProperties.CacheSpec());

            props.setCaches(caches);

            assertEquals(1, props.getCaches().size());
            assertTrue(props.getCaches().containsKey("users"));
        }
    }

    @Nested
    @DisplayName("CacheSpec Setters Tests")
    class CacheSpecSettersTests {

        @Test
        @DisplayName("setMaximumSize works")
        void setMaximumSizeWorks() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();

            spec.setMaximumSize(5000);

            assertEquals(5000, spec.getMaximumSize());
        }

        @Test
        @DisplayName("setMaximumWeight works")
        void setMaximumWeightWorks() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();

            spec.setMaximumWeight(1000000);

            assertEquals(1000000, spec.getMaximumWeight());
        }

        @Test
        @DisplayName("setExpireAfterWrite works")
        void setExpireAfterWriteWorks() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();

            spec.setExpireAfterWrite(Duration.ofMinutes(30));

            assertEquals(Duration.ofMinutes(30), spec.getExpireAfterWrite());
        }

        @Test
        @DisplayName("setExpireAfterAccess works")
        void setExpireAfterAccessWorks() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();

            spec.setExpireAfterAccess(Duration.ofMinutes(10));

            assertEquals(Duration.ofMinutes(10), spec.getExpireAfterAccess());
        }

        @Test
        @DisplayName("setRefreshAfterWrite works")
        void setRefreshAfterWriteWorks() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();

            spec.setRefreshAfterWrite(Duration.ofMinutes(5));

            assertEquals(Duration.ofMinutes(5), spec.getRefreshAfterWrite());
        }

        @Test
        @DisplayName("setInitialCapacity works")
        void setInitialCapacityWorks() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();

            spec.setInitialCapacity(32);

            assertEquals(32, spec.getInitialCapacity());
        }

        @Test
        @DisplayName("setConcurrencyLevel works")
        void setConcurrencyLevelWorks() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();

            spec.setConcurrencyLevel(8);

            assertEquals(8, spec.getConcurrencyLevel());
        }

        @Test
        @DisplayName("setRecordStats works")
        void setRecordStatsWorks() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();

            spec.setRecordStats(true);

            assertTrue(spec.isRecordStats());
        }

        @Test
        @DisplayName("setUseVirtualThreads works")
        void setUseVirtualThreadsWorks() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();

            spec.setUseVirtualThreads(false);

            assertFalse(spec.isUseVirtualThreads());
        }

        @Test
        @DisplayName("setEvictionPolicy works")
        void setEvictionPolicyWorks() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();

            spec.setEvictionPolicy("lfu");

            assertEquals("lfu", spec.getEvictionPolicy());
        }

        @Test
        @DisplayName("setMetricsEnabled works")
        void setMetricsEnabledWorks() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();

            spec.setMetricsEnabled(true);

            assertTrue(spec.isMetricsEnabled());
        }
    }

    @Nested
    @DisplayName("CacheSpec Validation Tests")
    class CacheSpecValidationTests {

        @Test
        @DisplayName("valid spec passes validation")
        void validSpecPassesValidation() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();
            spec.setMaximumSize(1000);
            spec.setInitialCapacity(16);
            spec.setConcurrencyLevel(4);
            spec.setEvictionPolicy("lru");

            assertDoesNotThrow(spec::validate);
        }

        @Test
        @DisplayName("negative maximumSize fails validation")
        void negativeMaximumSizeFailsValidation() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();
            spec.setMaximumSize(-1);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, spec::validate);
            assertTrue(ex.getMessage().contains("maximumSize"));
        }

        @Test
        @DisplayName("maximumWeight less than -1 fails validation")
        void maximumWeightLessThanMinusOneFailsValidation() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();
            spec.setMaximumWeight(-2);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, spec::validate);
            assertTrue(ex.getMessage().contains("maximumWeight"));
        }

        @Test
        @DisplayName("negative initialCapacity fails validation")
        void negativeInitialCapacityFailsValidation() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();
            spec.setInitialCapacity(-1);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, spec::validate);
            assertTrue(ex.getMessage().contains("initialCapacity"));
        }

        @Test
        @DisplayName("zero concurrencyLevel fails validation")
        void zeroConcurrencyLevelFailsValidation() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();
            spec.setConcurrencyLevel(0);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, spec::validate);
            assertTrue(ex.getMessage().contains("concurrencyLevel"));
        }

        @Test
        @DisplayName("negative expireAfterWrite fails validation")
        void negativeExpireAfterWriteFailsValidation() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();
            spec.setExpireAfterWrite(Duration.ofMinutes(-1));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, spec::validate);
            assertTrue(ex.getMessage().contains("expireAfterWrite"));
        }

        @Test
        @DisplayName("negative expireAfterAccess fails validation")
        void negativeExpireAfterAccessFailsValidation() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();
            spec.setExpireAfterAccess(Duration.ofMinutes(-1));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, spec::validate);
            assertTrue(ex.getMessage().contains("expireAfterAccess"));
        }

        @Test
        @DisplayName("negative refreshAfterWrite fails validation")
        void negativeRefreshAfterWriteFailsValidation() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();
            spec.setRefreshAfterWrite(Duration.ofMinutes(-1));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, spec::validate);
            assertTrue(ex.getMessage().contains("refreshAfterWrite"));
        }

        @Test
        @DisplayName("invalid evictionPolicy fails validation")
        void invalidEvictionPolicyFailsValidation() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();
            spec.setEvictionPolicy("invalid");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, spec::validate);
            assertTrue(ex.getMessage().contains("eviction policy"));
        }

        @Test
        @DisplayName("all valid eviction policies pass validation")
        void allValidEvictionPoliciesPassValidation() {
            for (String policy : new String[]{"lru", "lfu", "fifo", "wtinylfu", "w-tinylfu"}) {
                CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();
                spec.setEvictionPolicy(policy);

                assertDoesNotThrow(spec::validate, "Policy " + policy + " should be valid");
            }
        }

        @Test
        @DisplayName("eviction policy validation is case insensitive")
        void evictionPolicyValidationIsCaseInsensitive() {
            CacheProperties.CacheSpec spec = new CacheProperties.CacheSpec();
            spec.setEvictionPolicy("LRU");

            assertDoesNotThrow(spec::validate);
        }
    }

    @Nested
    @DisplayName("CacheProperties Validation Tests")
    class CachePropertiesValidationTests {

        @Test
        @DisplayName("validate passes for valid properties")
        void validatePassesForValidProperties() {
            CacheProperties props = new CacheProperties();

            assertDoesNotThrow(props::validate);
        }

        @Test
        @DisplayName("validate checks defaultSpec")
        void validateChecksDefaultSpec() {
            CacheProperties props = new CacheProperties();
            props.getDefaultSpec().setMaximumSize(-1);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, props::validate);
            assertTrue(ex.getMessage().contains("Default cache config"));
        }

        @Test
        @DisplayName("validate checks named caches")
        void validateChecksNamedCaches() {
            CacheProperties props = new CacheProperties();
            CacheProperties.CacheSpec invalidSpec = new CacheProperties.CacheSpec();
            invalidSpec.setConcurrencyLevel(0);
            props.getCaches().put("users", invalidSpec);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, props::validate);
            assertTrue(ex.getMessage().contains("users"));
        }

        @Test
        @DisplayName("validate handles null defaultSpec")
        void validateHandlesNullDefaultSpec() {
            CacheProperties props = new CacheProperties();
            props.setDefaultSpec(null);

            assertDoesNotThrow(props::validate);
        }
    }
}
