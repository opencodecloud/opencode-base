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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CacheEventListener
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheEventListener Tests")
class CacheEventListenerTest {

    @Nested
    @DisplayName("Default Methods Tests")
    class DefaultMethodsTests {

        @Test
        @DisplayName("interestedEventTypes returns all types by default")
        void interestedEventTypesReturnsAllTypesByDefault() {
            CacheEventListener<String, String> listener = event -> {};

            Set<CacheEvent.EventType> types = listener.interestedEventTypes();
            assertEquals(EnumSet.allOf(CacheEvent.EventType.class), types);
        }

        @Test
        @DisplayName("isInterestedIn returns true for all types by default")
        void isInterestedInReturnsTrueForAllTypesByDefault() {
            CacheEventListener<String, String> listener = event -> {};

            for (CacheEvent.EventType type : CacheEvent.EventType.values()) {
                assertTrue(listener.isInterestedIn(type));
            }
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("forTypes creates listener for specific types")
        void forTypesCreatesListenerForSpecificTypes() {
            List<CacheEvent<String, String>> received = new ArrayList<>();
            CacheEventListener<String, String> listener = CacheEventListener.forTypes(
                    EnumSet.of(CacheEvent.EventType.PUT, CacheEvent.EventType.REMOVE),
                    received::add
            );

            assertEquals(EnumSet.of(CacheEvent.EventType.PUT, CacheEvent.EventType.REMOVE),
                    listener.interestedEventTypes());
            assertTrue(listener.isInterestedIn(CacheEvent.EventType.PUT));
            assertTrue(listener.isInterestedIn(CacheEvent.EventType.REMOVE));
            assertFalse(listener.isInterestedIn(CacheEvent.EventType.GET));
        }

        @Test
        @DisplayName("forTypes invokes consumer")
        void forTypesInvokesConsumer() {
            List<CacheEvent<String, String>> received = new ArrayList<>();
            CacheEventListener<String, String> listener = CacheEventListener.forTypes(
                    EnumSet.of(CacheEvent.EventType.PUT),
                    received::add
            );

            CacheEvent<String, String> event = CacheEvent.put("cache", "key", "value");
            listener.onEvent(event);

            assertEquals(1, received.size());
            assertEquals(event, received.get(0));
        }

        @Test
        @DisplayName("onPut creates PUT listener")
        void onPutCreatesPutListener() {
            List<CacheEvent<String, String>> received = new ArrayList<>();
            CacheEventListener<String, String> listener = CacheEventListener.onPut(received::add);

            assertEquals(EnumSet.of(CacheEvent.EventType.PUT), listener.interestedEventTypes());
            assertTrue(listener.isInterestedIn(CacheEvent.EventType.PUT));
            assertFalse(listener.isInterestedIn(CacheEvent.EventType.GET));
        }

        @Test
        @DisplayName("onGet creates GET listener")
        void onGetCreatesGetListener() {
            List<CacheEvent<String, String>> received = new ArrayList<>();
            CacheEventListener<String, String> listener = CacheEventListener.onGet(received::add);

            assertEquals(EnumSet.of(CacheEvent.EventType.GET), listener.interestedEventTypes());
            assertTrue(listener.isInterestedIn(CacheEvent.EventType.GET));
            assertFalse(listener.isInterestedIn(CacheEvent.EventType.PUT));
        }

        @Test
        @DisplayName("onRemove creates REMOVE listener")
        void onRemoveCreatesRemoveListener() {
            List<CacheEvent<String, String>> received = new ArrayList<>();
            CacheEventListener<String, String> listener = CacheEventListener.onRemove(received::add);

            assertEquals(EnumSet.of(CacheEvent.EventType.REMOVE), listener.interestedEventTypes());
            assertTrue(listener.isInterestedIn(CacheEvent.EventType.REMOVE));
        }

        @Test
        @DisplayName("onEvict creates EVICT listener")
        void onEvictCreatesEvictListener() {
            List<CacheEvent<String, String>> received = new ArrayList<>();
            CacheEventListener<String, String> listener = CacheEventListener.onEvict(received::add);

            assertEquals(EnumSet.of(CacheEvent.EventType.EVICT), listener.interestedEventTypes());
            assertTrue(listener.isInterestedIn(CacheEvent.EventType.EVICT));
        }

        @Test
        @DisplayName("onExpire creates EXPIRE listener")
        void onExpireCreatesExpireListener() {
            List<CacheEvent<String, String>> received = new ArrayList<>();
            CacheEventListener<String, String> listener = CacheEventListener.onExpire(received::add);

            assertEquals(EnumSet.of(CacheEvent.EventType.EXPIRE), listener.interestedEventTypes());
            assertTrue(listener.isInterestedIn(CacheEvent.EventType.EXPIRE));
        }

        @Test
        @DisplayName("onLoad creates LOAD listener")
        void onLoadCreatesLoadListener() {
            List<CacheEvent<String, String>> received = new ArrayList<>();
            CacheEventListener<String, String> listener = CacheEventListener.onLoad(received::add);

            assertEquals(EnumSet.of(CacheEvent.EventType.LOAD), listener.interestedEventTypes());
            assertTrue(listener.isInterestedIn(CacheEvent.EventType.LOAD));
        }

        @Test
        @DisplayName("onRemoval creates listener for removal events")
        void onRemovalCreatesListenerForRemovalEvents() {
            List<CacheEvent<String, String>> received = new ArrayList<>();
            CacheEventListener<String, String> listener = CacheEventListener.onRemoval(received::add);

            Set<CacheEvent.EventType> expected = EnumSet.of(
                    CacheEvent.EventType.REMOVE,
                    CacheEvent.EventType.EXPIRE,
                    CacheEvent.EventType.EVICT
            );
            assertEquals(expected, listener.interestedEventTypes());
            assertTrue(listener.isInterestedIn(CacheEvent.EventType.REMOVE));
            assertTrue(listener.isInterestedIn(CacheEvent.EventType.EXPIRE));
            assertTrue(listener.isInterestedIn(CacheEvent.EventType.EVICT));
            assertFalse(listener.isInterestedIn(CacheEvent.EventType.PUT));
        }

        @Test
        @DisplayName("onWrite creates listener for write events")
        void onWriteCreatesListenerForWriteEvents() {
            List<CacheEvent<String, String>> received = new ArrayList<>();
            CacheEventListener<String, String> listener = CacheEventListener.onWrite(received::add);

            Set<CacheEvent.EventType> expected = EnumSet.of(
                    CacheEvent.EventType.PUT,
                    CacheEvent.EventType.REMOVE,
                    CacheEvent.EventType.CLEAR
            );
            assertEquals(expected, listener.interestedEventTypes());
            assertTrue(listener.isInterestedIn(CacheEvent.EventType.PUT));
            assertTrue(listener.isInterestedIn(CacheEvent.EventType.REMOVE));
            assertTrue(listener.isInterestedIn(CacheEvent.EventType.CLEAR));
            assertFalse(listener.isInterestedIn(CacheEvent.EventType.GET));
        }
    }

    @Nested
    @DisplayName("Composition Tests")
    class CompositionTests {

        @Test
        @DisplayName("andThen composes two listeners")
        void andThenComposesTwoListeners() {
            List<String> results = new ArrayList<>();

            CacheEventListener<String, String> first = event -> results.add("first");
            CacheEventListener<String, String> second = event -> results.add("second");
            CacheEventListener<String, String> composed = first.andThen(second);

            composed.onEvent(CacheEvent.put("cache", "key", "value"));

            assertEquals(2, results.size());
            assertEquals("first", results.get(0));
            assertEquals("second", results.get(1));
        }

        @Test
        @DisplayName("andThen chains multiple listeners")
        void andThenChainsMultipleListeners() {
            List<Integer> order = new ArrayList<>();

            CacheEventListener<String, String> first = event -> order.add(1);
            CacheEventListener<String, String> second = event -> order.add(2);
            CacheEventListener<String, String> third = event -> order.add(3);

            CacheEventListener<String, String> composed = first.andThen(second).andThen(third);
            composed.onEvent(CacheEvent.put("cache", "key", "value"));

            assertEquals(List.of(1, 2, 3), order);
        }
    }

    @Nested
    @DisplayName("Lambda Implementation Tests")
    class LambdaImplementationTests {

        @Test
        @DisplayName("lambda listener receives events")
        void lambdaListenerReceivesEvents() {
            List<CacheEvent<String, String>> received = new ArrayList<>();
            CacheEventListener<String, String> listener = received::add;

            CacheEvent<String, String> event = CacheEvent.put("cache", "key", "value");
            listener.onEvent(event);

            assertEquals(1, received.size());
            assertEquals(event, received.get(0));
        }

        @Test
        @DisplayName("lambda listener is functional interface")
        void lambdaListenerIsFunctionalInterface() {
            // This should compile - proving it's a functional interface
            CacheEventListener<String, String> listener = event -> {
                System.out.println(event.type());
            };
            assertNotNull(listener);
        }
    }

    @Nested
    @DisplayName("Event Filtering Tests")
    class EventFilteringTests {

        @Test
        @DisplayName("typed listener filters by interest")
        void typedListenerFiltersByInterest() {
            List<CacheEvent<String, String>> received = new ArrayList<>();
            CacheEventListener<String, String> listener = CacheEventListener.onPut(received::add);

            // Only interested in PUT, but we can still call onEvent with any type
            CacheEvent<String, String> putEvent = CacheEvent.put("cache", "key", "value");
            CacheEvent<String, String> getEvent = CacheEvent.getHit("cache", "key", "value");

            // Listener should handle the event (filtering is done by dispatcher)
            if (listener.isInterestedIn(putEvent.type())) {
                listener.onEvent(putEvent);
            }
            if (listener.isInterestedIn(getEvent.type())) {
                listener.onEvent(getEvent);
            }

            assertEquals(1, received.size());
            assertEquals(putEvent, received.get(0));
        }
    }
}
