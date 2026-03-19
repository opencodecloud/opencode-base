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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CacheMXBean interface
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheMXBean Interface Tests")
class CacheMXBeanTest {

    @Nested
    @DisplayName("Interface Implementation Tests")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("interface can be implemented")
        void interfaceCanBeImplemented() {
            CacheMXBean mbean = new TestCacheMXBean();

            assertEquals("test-cache", mbean.getName());
            assertEquals(100, mbean.getSize());
            assertEquals(80, mbean.getHitCount());
            assertEquals(20, mbean.getMissCount());
            assertEquals(100, mbean.getRequestCount());
            assertEquals(0.8, mbean.getHitRatio(), 0.001);
            assertEquals(0.2, mbean.getMissRatio(), 0.001);
            assertEquals(5, mbean.getEvictionCount());
            assertEquals(10, mbean.getLoadSuccessCount());
            assertEquals(2, mbean.getLoadFailureCount());
            assertEquals(50.0, mbean.getAverageLoadTimeMillis());
            assertEquals(100.0, mbean.getGetLatencyP50Micros());
            assertEquals(500.0, mbean.getGetLatencyP95Micros());
            assertEquals(1000.0, mbean.getGetLatencyP99Micros());
            assertEquals(1000, mbean.getMaximumSize());
            assertTrue(mbean.isStatisticsEnabled());
            assertEquals("TTL=1h", mbean.getExpirationConfig());
        }

        @Test
        @DisplayName("clear does not throw")
        void clearDoesNotThrow() {
            CacheMXBean mbean = new TestCacheMXBean();

            assertDoesNotThrow(mbean::clear);
        }

        @Test
        @DisplayName("cleanup does not throw")
        void cleanupDoesNotThrow() {
            CacheMXBean mbean = new TestCacheMXBean();

            assertDoesNotThrow(mbean::cleanup);
        }

        @Test
        @DisplayName("resetStatistics does not throw")
        void resetStatisticsDoesNotThrow() {
            CacheMXBean mbean = new TestCacheMXBean();

            assertDoesNotThrow(mbean::resetStatistics);
        }
    }

    /**
     * Test implementation of CacheMXBean
     */
    private static class TestCacheMXBean implements CacheMXBean {

        private long hitCount = 80;
        private long missCount = 20;
        private boolean cleared = false;

        @Override
        public String getName() {
            return "test-cache";
        }

        @Override
        public long getSize() {
            return cleared ? 0 : 100;
        }

        @Override
        public long getHitCount() {
            return hitCount;
        }

        @Override
        public long getMissCount() {
            return missCount;
        }

        @Override
        public long getRequestCount() {
            return hitCount + missCount;
        }

        @Override
        public double getHitRatio() {
            long total = hitCount + missCount;
            return total == 0 ? 0.0 : (double) hitCount / total;
        }

        @Override
        public double getMissRatio() {
            return 1.0 - getHitRatio();
        }

        @Override
        public long getEvictionCount() {
            return 5;
        }

        @Override
        public long getLoadSuccessCount() {
            return 10;
        }

        @Override
        public long getLoadFailureCount() {
            return 2;
        }

        @Override
        public double getAverageLoadTimeMillis() {
            return 50.0;
        }

        @Override
        public double getGetLatencyP50Micros() {
            return 100.0;
        }

        @Override
        public double getGetLatencyP95Micros() {
            return 500.0;
        }

        @Override
        public double getGetLatencyP99Micros() {
            return 1000.0;
        }

        @Override
        public void clear() {
            cleared = true;
        }

        @Override
        public void cleanup() {
            // No-op for test
        }

        @Override
        public void resetStatistics() {
            hitCount = 0;
            missCount = 0;
        }

        @Override
        public long getMaximumSize() {
            return 1000;
        }

        @Override
        public boolean isStatisticsEnabled() {
            return true;
        }

        @Override
        public String getExpirationConfig() {
            return "TTL=1h";
        }
    }
}
