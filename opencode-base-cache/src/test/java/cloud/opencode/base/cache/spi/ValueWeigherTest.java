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

package cloud.opencode.base.cache.spi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for ValueWeigher
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("ValueWeigher Tests")
class ValueWeigherTest {

    @Nested
    @DisplayName("fixed Tests")
    class FixedTests {

        @Test
        @DisplayName("fixed creates weigher with constant weight")
        void fixedCreatesWeigherWithConstantWeight() {
            ValueWeigher<String> weigher = ValueWeigher.fixed(10);

            assertEquals(10, weigher.weigh("short"));
            assertEquals(10, weigher.weigh("a longer string"));
            assertEquals(10, weigher.weigh(""));
        }

        @Test
        @DisplayName("fixed accepts zero weight")
        void fixedAcceptsZeroWeight() {
            ValueWeigher<String> weigher = ValueWeigher.fixed(0);
            assertEquals(0, weigher.weigh("any"));
        }

        @Test
        @DisplayName("fixed throws on negative weight")
        void fixedThrowsOnNegativeWeight() {
            assertThrows(IllegalArgumentException.class, () -> ValueWeigher.fixed(-1));
        }
    }

    @Nested
    @DisplayName("stringLength Tests")
    class StringLengthTests {

        @Test
        @DisplayName("stringLength returns string length")
        void stringLengthReturnsStringLength() {
            ValueWeigher<String> weigher = ValueWeigher.stringLength();

            assertEquals(0, weigher.weigh(""));
            assertEquals(5, weigher.weigh("hello"));
            assertEquals(11, weigher.weigh("hello world"));
        }

        @Test
        @DisplayName("stringLength handles null")
        void stringLengthHandlesNull() {
            ValueWeigher<String> weigher = ValueWeigher.stringLength();
            assertEquals(0, weigher.weigh(null));
        }
    }

    @Nested
    @DisplayName("byteArrayLength Tests")
    class ByteArrayLengthTests {

        @Test
        @DisplayName("byteArrayLength returns array length")
        void byteArrayLengthReturnsArrayLength() {
            ValueWeigher<byte[]> weigher = ValueWeigher.byteArrayLength();

            assertEquals(0, weigher.weigh(new byte[0]));
            assertEquals(5, weigher.weigh(new byte[5]));
            assertEquals(100, weigher.weigh(new byte[100]));
        }

        @Test
        @DisplayName("byteArrayLength handles null")
        void byteArrayLengthHandlesNull() {
            ValueWeigher<byte[]> weigher = ValueWeigher.byteArrayLength();
            assertEquals(0, weigher.weigh(null));
        }
    }

    @Nested
    @DisplayName("collectionSize Tests")
    class CollectionSizeTests {

        @Test
        @DisplayName("collectionSize returns collection size")
        void collectionSizeReturnsCollectionSize() {
            ValueWeigher<List<String>> weigher = ValueWeigher.collectionSize();

            assertEquals(0, weigher.weigh(List.of()));
            assertEquals(3, weigher.weigh(List.of("a", "b", "c")));
            assertEquals(5, weigher.weigh(Arrays.asList("1", "2", "3", "4", "5")));
        }

        @Test
        @DisplayName("collectionSize works with Set")
        void collectionSizeWorksWithSet() {
            ValueWeigher<Set<Integer>> weigher = ValueWeigher.collectionSize();

            assertEquals(0, weigher.weigh(Set.of()));
            assertEquals(3, weigher.weigh(Set.of(1, 2, 3)));
        }

        @Test
        @DisplayName("collectionSize handles null")
        void collectionSizeHandlesNull() {
            ValueWeigher<List<String>> weigher = ValueWeigher.collectionSize();
            assertEquals(0, weigher.weigh(null));
        }
    }

    @Nested
    @DisplayName("mapSize Tests")
    class MapSizeTests {

        @Test
        @DisplayName("mapSize returns map size")
        void mapSizeReturnsMapSize() {
            ValueWeigher<Map<String, String>> weigher = ValueWeigher.mapSize();

            assertEquals(0, weigher.weigh(Map.of()));
            assertEquals(2, weigher.weigh(Map.of("a", "1", "b", "2")));
        }

        @Test
        @DisplayName("mapSize handles null")
        void mapSizeHandlesNull() {
            ValueWeigher<Map<String, String>> weigher = ValueWeigher.mapSize();
            assertEquals(0, weigher.weigh(null));
        }
    }

    @Nested
    @DisplayName("arrayLength Tests")
    class ArrayLengthTests {

        @Test
        @DisplayName("arrayLength returns array length")
        void arrayLengthReturnsArrayLength() {
            ValueWeigher<String[]> weigher = ValueWeigher.arrayLength();

            assertEquals(0, weigher.weigh(new String[0]));
            assertEquals(3, weigher.weigh(new String[]{"a", "b", "c"}));
        }

        @Test
        @DisplayName("arrayLength works with primitive arrays")
        void arrayLengthWorksWithPrimitiveArrays() {
            ValueWeigher<int[]> weigher = ValueWeigher.arrayLength();
            assertEquals(5, weigher.weigh(new int[5]));
        }

        @Test
        @DisplayName("arrayLength returns 1 for non-arrays")
        void arrayLengthReturnsOneForNonArrays() {
            ValueWeigher<String> weigher = ValueWeigher.arrayLength();
            assertEquals(1, weigher.weigh("not an array"));
        }

        @Test
        @DisplayName("arrayLength handles null")
        void arrayLengthHandlesNull() {
            ValueWeigher<String[]> weigher = ValueWeigher.arrayLength();
            assertEquals(0, weigher.weigh(null));
        }
    }

    @Nested
    @DisplayName("estimatedMemory Tests")
    class EstimatedMemoryTests {

        @Test
        @DisplayName("estimatedMemory for String")
        void estimatedMemoryForString() {
            ValueWeigher<Object> weigher = ValueWeigher.estimatedMemory();

            // String: length * 2 + 40
            long weight = weigher.weigh("hello"); // 5 * 2 + 40 = 50
            assertEquals(50, weight);
        }

        @Test
        @DisplayName("estimatedMemory for byte array")
        void estimatedMemoryForByteArray() {
            ValueWeigher<Object> weigher = ValueWeigher.estimatedMemory();

            // byte[]: length + 16
            long weight = weigher.weigh(new byte[100]); // 100 + 16 = 116
            assertEquals(116, weight);
        }

        @Test
        @DisplayName("estimatedMemory for Collection")
        void estimatedMemoryForCollection() {
            ValueWeigher<Object> weigher = ValueWeigher.estimatedMemory();

            // Collection: size * 8 + 40
            long weight = weigher.weigh(List.of(1, 2, 3)); // 3 * 8 + 40 = 64
            assertEquals(64, weight);
        }

        @Test
        @DisplayName("estimatedMemory for Map")
        void estimatedMemoryForMap() {
            ValueWeigher<Object> weigher = ValueWeigher.estimatedMemory();

            // Map: size * 16 + 40
            long weight = weigher.weigh(Map.of("a", 1, "b", 2)); // 2 * 16 + 40 = 72
            assertEquals(72, weight);
        }

        @Test
        @DisplayName("estimatedMemory for array")
        void estimatedMemoryForArray() {
            ValueWeigher<Object> weigher = ValueWeigher.estimatedMemory();

            // Array: length * 8 + 16
            long weight = weigher.weigh(new Object[10]); // 10 * 8 + 16 = 96
            assertEquals(96, weight);
        }

        @Test
        @DisplayName("estimatedMemory for other objects")
        void estimatedMemoryForOtherObjects() {
            ValueWeigher<Object> weigher = ValueWeigher.estimatedMemory();

            // Other: 40 (minimum overhead)
            long weight = weigher.weigh(Integer.valueOf(42));
            assertEquals(40, weight);
        }

        @Test
        @DisplayName("estimatedMemory handles null")
        void estimatedMemoryHandlesNull() {
            ValueWeigher<Object> weigher = ValueWeigher.estimatedMemory();
            assertEquals(0, weigher.weigh(null));
        }
    }

    @Nested
    @DisplayName("times Tests")
    class TimesTests {

        @Test
        @DisplayName("times multiplies weight")
        void timesMultipliesWeight() {
            ValueWeigher<String> weigher = ValueWeigher.stringLength().times(3);

            assertEquals(0, weigher.weigh(""));
            assertEquals(15, weigher.weigh("hello")); // 5 * 3
        }

        @Test
        @DisplayName("times accepts zero multiplier")
        void timesAcceptsZeroMultiplier() {
            ValueWeigher<String> weigher = ValueWeigher.stringLength().times(0);
            assertEquals(0, weigher.weigh("hello"));
        }

        @Test
        @DisplayName("times throws on negative multiplier")
        void timesThrowsOnNegativeMultiplier() {
            ValueWeigher<String> weigher = ValueWeigher.stringLength();
            assertThrows(IllegalArgumentException.class, () -> weigher.times(-1));
        }
    }

    @Nested
    @DisplayName("withMinimum Tests")
    class WithMinimumTests {

        @Test
        @DisplayName("withMinimum enforces minimum")
        void withMinimumEnforcesMinimum() {
            ValueWeigher<String> weigher = ValueWeigher.stringLength().withMinimum(10);

            assertEquals(10, weigher.weigh("")); // 0 -> 10
            assertEquals(10, weigher.weigh("hi")); // 2 -> 10
            assertEquals(11, weigher.weigh("hello world")); // 11 stays 11
        }
    }

    @Nested
    @DisplayName("withMaximum Tests")
    class WithMaximumTests {

        @Test
        @DisplayName("withMaximum enforces maximum")
        void withMaximumEnforcesMaximum() {
            ValueWeigher<String> weigher = ValueWeigher.stringLength().withMaximum(5);

            assertEquals(0, weigher.weigh("")); // 0 stays 0
            assertEquals(3, weigher.weigh("abc")); // 3 stays 3
            assertEquals(5, weigher.weigh("hello world")); // 11 -> 5
        }
    }

    @Nested
    @DisplayName("bounded Tests")
    class BoundedTests {

        @Test
        @DisplayName("bounded enforces min and max")
        void boundedEnforcesMinAndMax() {
            ValueWeigher<String> weigher = ValueWeigher.stringLength().bounded(2, 8);

            assertEquals(2, weigher.weigh("")); // 0 -> 2
            assertEquals(2, weigher.weigh("a")); // 1 -> 2
            assertEquals(5, weigher.weigh("hello")); // 5 stays 5
            assertEquals(8, weigher.weigh("hello world")); // 11 -> 8
        }
    }

    @Nested
    @DisplayName("combined Tests")
    class CombinedTests {

        @Test
        @DisplayName("combined adds key and value weights")
        void combinedAddsKeyAndValueWeights() {
            ValueWeigher.EntryWeigher<String, String> weigher = ValueWeigher.combined(
                    ValueWeigher.stringLength(),
                    ValueWeigher.stringLength()
            );

            assertEquals(8, weigher.weigh("key", "value")); // 3 + 5
            assertEquals(0, weigher.weigh("", "")); // 0 + 0
        }

        @Test
        @DisplayName("combined with different weigher types")
        void combinedWithDifferentWeigherTypes() {
            ValueWeigher.EntryWeigher<String, List<Integer>> weigher = ValueWeigher.combined(
                    ValueWeigher.stringLength(),
                    ValueWeigher.collectionSize()
            );

            assertEquals(7, weigher.weigh("key", List.of(1, 2, 3, 4))); // 3 + 4
        }
    }

    @Nested
    @DisplayName("EntryWeigher Tests")
    class EntryWeigherTests {

        @Test
        @DisplayName("EntryWeigher is functional interface")
        void entryWeigherIsFunctionalInterface() {
            ValueWeigher.EntryWeigher<String, Integer> weigher = (key, value) -> key.length() + value;

            assertEquals(8, weigher.weigh("key", 5)); // 3 + 5
        }
    }

    @Nested
    @DisplayName("Custom Weigher Tests")
    class CustomWeigherTests {

        @Test
        @DisplayName("custom weigher implementation")
        void customWeigherImplementation() {
            // Custom weigher that returns double the string length
            ValueWeigher<String> weigher = str -> str == null ? 0 : str.length() * 2L;

            assertEquals(0, weigher.weigh(""));
            assertEquals(10, weigher.weigh("hello"));
        }

        @Test
        @DisplayName("chaining multiple operations")
        void chainingMultipleOperations() {
            ValueWeigher<String> weigher = ValueWeigher.stringLength()
                    .times(2)
                    .withMinimum(5)
                    .withMaximum(20);

            assertEquals(5, weigher.weigh("")); // 0 * 2 = 0 -> min 5
            assertEquals(6, weigher.weigh("abc")); // 3 * 2 = 6
            assertEquals(20, weigher.weigh("hello world and more")); // 20 * 2 = 40 -> max 20
        }
    }
}
