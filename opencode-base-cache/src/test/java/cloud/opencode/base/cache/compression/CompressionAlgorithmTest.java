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

package cloud.opencode.base.cache.compression;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CompressionAlgorithm
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CompressionAlgorithm Tests")
class CompressionAlgorithmTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("NONE has correct properties")
        void noneHasCorrectProperties() {
            assertEquals(0, CompressionAlgorithm.NONE.id());
            assertEquals("none", CompressionAlgorithm.NONE.algorithmName());
            assertEquals(0, CompressionAlgorithm.NONE.defaultLevel());
        }

        @Test
        @DisplayName("GZIP has correct properties")
        void gzipHasCorrectProperties() {
            assertEquals(1, CompressionAlgorithm.GZIP.id());
            assertEquals("gzip", CompressionAlgorithm.GZIP.algorithmName());
            assertEquals(6, CompressionAlgorithm.GZIP.defaultLevel());
        }

        @Test
        @DisplayName("LZ4 has correct properties")
        void lz4HasCorrectProperties() {
            assertEquals(2, CompressionAlgorithm.LZ4.id());
            assertEquals("lz4", CompressionAlgorithm.LZ4.algorithmName());
            assertEquals(9, CompressionAlgorithm.LZ4.defaultLevel());
        }

        @Test
        @DisplayName("ZSTD has correct properties")
        void zstdHasCorrectProperties() {
            assertEquals(3, CompressionAlgorithm.ZSTD.id());
            assertEquals("zstd", CompressionAlgorithm.ZSTD.algorithmName());
            assertEquals(3, CompressionAlgorithm.ZSTD.defaultLevel());
        }

        @Test
        @DisplayName("SNAPPY has correct properties")
        void snappyHasCorrectProperties() {
            assertEquals(4, CompressionAlgorithm.SNAPPY.id());
            assertEquals("snappy", CompressionAlgorithm.SNAPPY.algorithmName());
            assertEquals(0, CompressionAlgorithm.SNAPPY.defaultLevel());
        }
    }

    @Nested
    @DisplayName("fromId Tests")
    class FromIdTests {

        @Test
        @DisplayName("fromId returns NONE for id 0")
        void fromIdReturnsNoneForId0() {
            assertEquals(CompressionAlgorithm.NONE, CompressionAlgorithm.fromId(0));
        }

        @Test
        @DisplayName("fromId returns GZIP for id 1")
        void fromIdReturnsGzipForId1() {
            assertEquals(CompressionAlgorithm.GZIP, CompressionAlgorithm.fromId(1));
        }

        @Test
        @DisplayName("fromId returns LZ4 for id 2")
        void fromIdReturnsLz4ForId2() {
            assertEquals(CompressionAlgorithm.LZ4, CompressionAlgorithm.fromId(2));
        }

        @Test
        @DisplayName("fromId returns ZSTD for id 3")
        void fromIdReturnsZstdForId3() {
            assertEquals(CompressionAlgorithm.ZSTD, CompressionAlgorithm.fromId(3));
        }

        @Test
        @DisplayName("fromId returns SNAPPY for id 4")
        void fromIdReturnsSnappyForId4() {
            assertEquals(CompressionAlgorithm.SNAPPY, CompressionAlgorithm.fromId(4));
        }

        @Test
        @DisplayName("fromId returns NONE for invalid id")
        void fromIdReturnsNoneForInvalidId() {
            assertEquals(CompressionAlgorithm.NONE, CompressionAlgorithm.fromId(99));
            assertEquals(CompressionAlgorithm.NONE, CompressionAlgorithm.fromId(-1));
        }
    }

    @Nested
    @DisplayName("fromName Tests")
    class FromNameTests {

        @Test
        @DisplayName("fromName returns NONE for 'none'")
        void fromNameReturnsNoneForNone() {
            assertEquals(CompressionAlgorithm.NONE, CompressionAlgorithm.fromName("none"));
        }

        @Test
        @DisplayName("fromName returns GZIP for 'gzip'")
        void fromNameReturnsGzipForGzip() {
            assertEquals(CompressionAlgorithm.GZIP, CompressionAlgorithm.fromName("gzip"));
        }

        @Test
        @DisplayName("fromName returns LZ4 for 'lz4'")
        void fromNameReturnsLz4ForLz4() {
            assertEquals(CompressionAlgorithm.LZ4, CompressionAlgorithm.fromName("lz4"));
        }

        @Test
        @DisplayName("fromName returns ZSTD for 'zstd'")
        void fromNameReturnsZstdForZstd() {
            assertEquals(CompressionAlgorithm.ZSTD, CompressionAlgorithm.fromName("zstd"));
        }

        @Test
        @DisplayName("fromName returns SNAPPY for 'snappy'")
        void fromNameReturnsSnappyForSnappy() {
            assertEquals(CompressionAlgorithm.SNAPPY, CompressionAlgorithm.fromName("snappy"));
        }

        @Test
        @DisplayName("fromName is case insensitive")
        void fromNameIsCaseInsensitive() {
            assertEquals(CompressionAlgorithm.GZIP, CompressionAlgorithm.fromName("GZIP"));
            assertEquals(CompressionAlgorithm.GZIP, CompressionAlgorithm.fromName("Gzip"));
            assertEquals(CompressionAlgorithm.LZ4, CompressionAlgorithm.fromName("LZ4"));
        }

        @Test
        @DisplayName("fromName returns NONE for unknown name")
        void fromNameReturnsNoneForUnknownName() {
            assertEquals(CompressionAlgorithm.NONE, CompressionAlgorithm.fromName("unknown"));
            assertEquals(CompressionAlgorithm.NONE, CompressionAlgorithm.fromName(""));
        }
    }

    @Nested
    @DisplayName("Enum Standard Methods Tests")
    class EnumStandardMethodsTests {

        @Test
        @DisplayName("values returns all algorithms")
        void valuesReturnsAllAlgorithms() {
            CompressionAlgorithm[] values = CompressionAlgorithm.values();
            assertEquals(5, values.length);
        }

        @ParameterizedTest
        @EnumSource(CompressionAlgorithm.class)
        @DisplayName("valueOf works for all algorithms")
        void valueOfWorksForAllAlgorithms(CompressionAlgorithm algo) {
            assertEquals(algo, CompressionAlgorithm.valueOf(algo.name()));
        }

        @Test
        @DisplayName("valueOf throws for invalid name")
        void valueOfThrowsForInvalidName() {
            assertThrows(IllegalArgumentException.class, () -> CompressionAlgorithm.valueOf("INVALID"));
        }
    }

    @Nested
    @DisplayName("ID Uniqueness Tests")
    class IdUniquenessTests {

        @Test
        @DisplayName("all algorithms have unique IDs")
        void allAlgorithmsHaveUniqueIds() {
            CompressionAlgorithm[] values = CompressionAlgorithm.values();
            for (int i = 0; i < values.length; i++) {
                for (int j = i + 1; j < values.length; j++) {
                    assertNotEquals(values[i].id(), values[j].id(),
                            "IDs should be unique: " + values[i] + " and " + values[j]);
                }
            }
        }

        @Test
        @DisplayName("all algorithms have unique names")
        void allAlgorithmsHaveUniqueNames() {
            CompressionAlgorithm[] values = CompressionAlgorithm.values();
            for (int i = 0; i < values.length; i++) {
                for (int j = i + 1; j < values.length; j++) {
                    assertNotEquals(values[i].algorithmName(), values[j].algorithmName(),
                            "Names should be unique: " + values[i] + " and " + values[j]);
                }
            }
        }
    }

    @Nested
    @DisplayName("Round Trip Tests")
    class RoundTripTests {

        @ParameterizedTest
        @EnumSource(CompressionAlgorithm.class)
        @DisplayName("fromId(algo.id()) returns original algorithm")
        void fromIdRoundTrip(CompressionAlgorithm algo) {
            assertEquals(algo, CompressionAlgorithm.fromId(algo.id()));
        }

        @ParameterizedTest
        @EnumSource(CompressionAlgorithm.class)
        @DisplayName("fromName(algo.algorithmName()) returns original algorithm")
        void fromNameRoundTrip(CompressionAlgorithm algo) {
            assertEquals(algo, CompressionAlgorithm.fromName(algo.algorithmName()));
        }
    }
}
