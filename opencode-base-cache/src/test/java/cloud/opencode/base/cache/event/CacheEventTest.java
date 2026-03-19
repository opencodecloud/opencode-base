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

package cloud.opencode.base.cache.event;

import cloud.opencode.base.cache.model.RemovalCause;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CacheEvent
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheEvent Tests")
class CacheEventTest {

    @Nested
    @DisplayName("EventType Enum Tests")
    class EventTypeEnumTests {

        @Test
        @DisplayName("all event types exist")
        void allEventTypesExist() {
            CacheEvent.EventType[] types = CacheEvent.EventType.values();
            assertEquals(7, types.length);

            assertNotNull(CacheEvent.EventType.PUT);
            assertNotNull(CacheEvent.EventType.GET);
            assertNotNull(CacheEvent.EventType.REMOVE);
            assertNotNull(CacheEvent.EventType.EXPIRE);
            assertNotNull(CacheEvent.EventType.EVICT);
            assertNotNull(CacheEvent.EventType.LOAD);
            assertNotNull(CacheEvent.EventType.CLEAR);
        }

        @Test
        @DisplayName("valueOf works")
        void valueOfWorks() {
            assertEquals(CacheEvent.EventType.PUT, CacheEvent.EventType.valueOf("PUT"));
            assertEquals(CacheEvent.EventType.GET, CacheEvent.EventType.valueOf("GET"));
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("put creates PUT event")
        void putCreatesPutEvent() {
            CacheEvent<String, String> event = CacheEvent.put("cache", "key", "value");

            assertEquals(CacheEvent.EventType.PUT, event.type());
            assertEquals("cache", event.cacheName());
            assertEquals("key", event.key());
            assertEquals("value", event.value());
            assertNull(event.oldValue());
            assertNull(event.removalCause());
            assertFalse(event.isHit());
            assertNotNull(event.timestamp());
        }

        @Test
        @DisplayName("put with oldValue creates PUT event")
        void putWithOldValueCreatesPutEvent() {
            CacheEvent<String, String> event = CacheEvent.put("cache", "key", "newValue", "oldValue");

            assertEquals(CacheEvent.EventType.PUT, event.type());
            assertEquals("newValue", event.value());
            assertEquals("oldValue", event.oldValue());
        }

        @Test
        @DisplayName("getHit creates GET event with hit")
        void getHitCreatesGetEventWithHit() {
            CacheEvent<String, String> event = CacheEvent.getHit("cache", "key", "value");

            assertEquals(CacheEvent.EventType.GET, event.type());
            assertEquals("cache", event.cacheName());
            assertEquals("key", event.key());
            assertEquals("value", event.value());
            assertTrue(event.isHit());
        }

        @Test
        @DisplayName("getMiss creates GET event without hit")
        void getMissCreatesGetEventWithoutHit() {
            CacheEvent<String, String> event = CacheEvent.getMiss("cache", "key");

            assertEquals(CacheEvent.EventType.GET, event.type());
            assertEquals("key", event.key());
            assertNull(event.value());
            assertFalse(event.isHit());
        }

        @Test
        @DisplayName("remove creates REMOVE event")
        void removeCreatesRemoveEvent() {
            CacheEvent<String, String> event = CacheEvent.remove("cache", "key", "oldValue");

            assertEquals(CacheEvent.EventType.REMOVE, event.type());
            assertEquals("key", event.key());
            assertNull(event.value());
            assertEquals("oldValue", event.oldValue());
            assertEquals(RemovalCause.EXPLICIT, event.removalCause());
        }

        @Test
        @DisplayName("expire creates EXPIRE event")
        void expireCreatesExpireEvent() {
            CacheEvent<String, String> event = CacheEvent.expire("cache", "key", "oldValue");

            assertEquals(CacheEvent.EventType.EXPIRE, event.type());
            assertEquals("key", event.key());
            assertEquals("oldValue", event.oldValue());
            assertEquals(RemovalCause.EXPIRED, event.removalCause());
        }

        @Test
        @DisplayName("evict creates EVICT event")
        void evictCreatesEvictEvent() {
            CacheEvent<String, String> event = CacheEvent.evict("cache", "key", "oldValue", RemovalCause.SIZE);

            assertEquals(CacheEvent.EventType.EVICT, event.type());
            assertEquals("key", event.key());
            assertEquals("oldValue", event.oldValue());
            assertEquals(RemovalCause.SIZE, event.removalCause());
        }

        @Test
        @DisplayName("load creates LOAD event")
        void loadCreatesLoadEvent() {
            CacheEvent<String, String> event = CacheEvent.load("cache", "key", "loadedValue");

            assertEquals(CacheEvent.EventType.LOAD, event.type());
            assertEquals("key", event.key());
            assertEquals("loadedValue", event.value());
            assertNull(event.oldValue());
        }

        @Test
        @DisplayName("clear creates CLEAR event")
        void clearCreatesClearEvent() {
            CacheEvent<String, String> event = CacheEvent.clear("cache");

            assertEquals(CacheEvent.EventType.CLEAR, event.type());
            assertEquals("cache", event.cacheName());
            assertNull(event.key());
            assertNull(event.value());
        }
    }

    @Nested
    @DisplayName("Optional Methods Tests")
    class OptionalMethodsTests {

        @Test
        @DisplayName("optionalOldValue returns present when has value")
        void optionalOldValueReturnsPresentWhenHasValue() {
            CacheEvent<String, String> event = CacheEvent.put("cache", "key", "new", "old");
            Optional<String> optional = event.optionalOldValue();
            assertTrue(optional.isPresent());
            assertEquals("old", optional.get());
        }

        @Test
        @DisplayName("optionalOldValue returns empty when no value")
        void optionalOldValueReturnsEmptyWhenNoValue() {
            CacheEvent<String, String> event = CacheEvent.put("cache", "key", "value");
            Optional<String> optional = event.optionalOldValue();
            assertTrue(optional.isEmpty());
        }

        @Test
        @DisplayName("optionalValue returns present when has value")
        void optionalValueReturnsPresentWhenHasValue() {
            CacheEvent<String, String> event = CacheEvent.put("cache", "key", "value");
            Optional<String> optional = event.optionalValue();
            assertTrue(optional.isPresent());
            assertEquals("value", optional.get());
        }

        @Test
        @DisplayName("optionalValue returns empty when no value")
        void optionalValueReturnsEmptyWhenNoValue() {
            CacheEvent<String, String> event = CacheEvent.getMiss("cache", "key");
            Optional<String> optional = event.optionalValue();
            assertTrue(optional.isEmpty());
        }

        @Test
        @DisplayName("optionalRemovalCause returns present when has cause")
        void optionalRemovalCauseReturnsPresentWhenHasCause() {
            CacheEvent<String, String> event = CacheEvent.remove("cache", "key", "value");
            Optional<RemovalCause> optional = event.optionalRemovalCause();
            assertTrue(optional.isPresent());
            assertEquals(RemovalCause.EXPLICIT, optional.get());
        }

        @Test
        @DisplayName("optionalRemovalCause returns empty when no cause")
        void optionalRemovalCauseReturnsEmptyWhenNoCause() {
            CacheEvent<String, String> event = CacheEvent.put("cache", "key", "value");
            Optional<RemovalCause> optional = event.optionalRemovalCause();
            assertTrue(optional.isEmpty());
        }
    }

    @Nested
    @DisplayName("Event Type Check Tests")
    class EventTypeCheckTests {

        @Test
        @DisplayName("isWriteEvent returns true for PUT")
        void isWriteEventReturnsTrueForPut() {
            CacheEvent<String, String> event = CacheEvent.put("cache", "key", "value");
            assertTrue(event.isWriteEvent());
        }

        @Test
        @DisplayName("isWriteEvent returns true for REMOVE")
        void isWriteEventReturnsTrueForRemove() {
            CacheEvent<String, String> event = CacheEvent.remove("cache", "key", "value");
            assertTrue(event.isWriteEvent());
        }

        @Test
        @DisplayName("isWriteEvent returns true for CLEAR")
        void isWriteEventReturnsTrueForClear() {
            CacheEvent<String, String> event = CacheEvent.clear("cache");
            assertTrue(event.isWriteEvent());
        }

        @Test
        @DisplayName("isWriteEvent returns false for GET")
        void isWriteEventReturnsFalseForGet() {
            CacheEvent<String, String> event = CacheEvent.getHit("cache", "key", "value");
            assertFalse(event.isWriteEvent());
        }

        @Test
        @DisplayName("isWriteEvent returns false for LOAD")
        void isWriteEventReturnsFalseForLoad() {
            CacheEvent<String, String> event = CacheEvent.load("cache", "key", "value");
            assertFalse(event.isWriteEvent());
        }

        @Test
        @DisplayName("isRemovalEvent returns true for REMOVE")
        void isRemovalEventReturnsTrueForRemove() {
            CacheEvent<String, String> event = CacheEvent.remove("cache", "key", "value");
            assertTrue(event.isRemovalEvent());
        }

        @Test
        @DisplayName("isRemovalEvent returns true for EXPIRE")
        void isRemovalEventReturnsTrueForExpire() {
            CacheEvent<String, String> event = CacheEvent.expire("cache", "key", "value");
            assertTrue(event.isRemovalEvent());
        }

        @Test
        @DisplayName("isRemovalEvent returns true for EVICT")
        void isRemovalEventReturnsTrueForEvict() {
            CacheEvent<String, String> event = CacheEvent.evict("cache", "key", "value", RemovalCause.SIZE);
            assertTrue(event.isRemovalEvent());
        }

        @Test
        @DisplayName("isRemovalEvent returns false for PUT")
        void isRemovalEventReturnsFalseForPut() {
            CacheEvent<String, String> event = CacheEvent.put("cache", "key", "value");
            assertFalse(event.isRemovalEvent());
        }

        @Test
        @DisplayName("isRemovalEvent returns false for GET")
        void isRemovalEventReturnsFalseForGet() {
            CacheEvent<String, String> event = CacheEvent.getHit("cache", "key", "value");
            assertFalse(event.isRemovalEvent());
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("timestamp is set to current time")
        void timestampIsSetToCurrentTime() {
            Instant before = Instant.now();
            CacheEvent<String, String> event = CacheEvent.put("cache", "key", "value");
            Instant after = Instant.now();

            assertNotNull(event.timestamp());
            assertFalse(event.timestamp().isBefore(before));
            assertFalse(event.timestamp().isAfter(after));
        }
    }

    @Nested
    @DisplayName("Record Methods Tests")
    class RecordMethodsTests {

        @Test
        @DisplayName("record accessors work")
        void recordAccessorsWork() {
            Instant now = Instant.now();
            CacheEvent<String, String> event = new CacheEvent<>(
                    CacheEvent.EventType.PUT,
                    "cache",
                    "key",
                    "value",
                    "oldValue",
                    RemovalCause.EXPLICIT,
                    true,
                    now
            );

            assertEquals(CacheEvent.EventType.PUT, event.type());
            assertEquals("cache", event.cacheName());
            assertEquals("key", event.key());
            assertEquals("value", event.value());
            assertEquals("oldValue", event.oldValue());
            assertEquals(RemovalCause.EXPLICIT, event.removalCause());
            assertTrue(event.isHit());
            assertEquals(now, event.timestamp());
        }

        @Test
        @DisplayName("equals works")
        void equalsWorks() {
            Instant now = Instant.now();
            CacheEvent<String, String> event1 = new CacheEvent<>(
                    CacheEvent.EventType.PUT, "cache", "key", "value", null, null, false, now
            );
            CacheEvent<String, String> event2 = new CacheEvent<>(
                    CacheEvent.EventType.PUT, "cache", "key", "value", null, null, false, now
            );

            assertEquals(event1, event2);
        }

        @Test
        @DisplayName("hashCode works")
        void hashCodeWorks() {
            Instant now = Instant.now();
            CacheEvent<String, String> event1 = new CacheEvent<>(
                    CacheEvent.EventType.PUT, "cache", "key", "value", null, null, false, now
            );
            CacheEvent<String, String> event2 = new CacheEvent<>(
                    CacheEvent.EventType.PUT, "cache", "key", "value", null, null, false, now
            );

            assertEquals(event1.hashCode(), event2.hashCode());
        }

        @Test
        @DisplayName("toString works")
        void toStringWorks() {
            CacheEvent<String, String> event = CacheEvent.put("cache", "key", "value");
            String str = event.toString();

            assertTrue(str.contains("PUT"));
            assertTrue(str.contains("cache"));
            assertTrue(str.contains("key"));
            assertTrue(str.contains("value"));
        }
    }

    @Nested
    @DisplayName("Null Value Tests")
    class NullValueTests {

        @Test
        @DisplayName("handles null key in clear event")
        void handlesNullKeyInClearEvent() {
            CacheEvent<String, String> event = CacheEvent.clear("cache");
            assertNull(event.key());
        }

        @Test
        @DisplayName("handles null value in miss event")
        void handlesNullValueInMissEvent() {
            CacheEvent<String, String> event = CacheEvent.getMiss("cache", "key");
            assertNull(event.value());
        }

        @Test
        @DisplayName("handles null oldValue in put event")
        void handlesNullOldValueInPutEvent() {
            CacheEvent<String, String> event = CacheEvent.put("cache", "key", "value");
            assertNull(event.oldValue());
        }
    }
}
