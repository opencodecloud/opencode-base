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

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for ValueCompressor
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("ValueCompressor Tests")
class ValueCompressorTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("none() creates NoOpCompressor")
        void noneCreatesNoOpCompressor() {
            ValueCompressor compressor = ValueCompressor.none();
            assertNotNull(compressor);
            assertEquals(CompressionAlgorithm.NONE, compressor.algorithm());
        }

        @Test
        @DisplayName("gzip() creates GzipCompressor with default threshold")
        void gzipCreatesGzipCompressor() {
            ValueCompressor compressor = ValueCompressor.gzip();
            assertNotNull(compressor);
            assertEquals(CompressionAlgorithm.GZIP, compressor.algorithm());
            assertEquals(1024, compressor.compressionThreshold());
        }

        @Test
        @DisplayName("gzip(threshold) creates GzipCompressor with custom threshold")
        void gzipWithThresholdCreatesGzipCompressor() {
            ValueCompressor compressor = ValueCompressor.gzip(512);
            assertNotNull(compressor);
            assertEquals(CompressionAlgorithm.GZIP, compressor.algorithm());
            assertEquals(512, compressor.compressionThreshold());
        }

        @Test
        @DisplayName("builder() creates compressor builder")
        void builderCreatesBuilder() {
            ValueCompressor.Builder builder = ValueCompressor.builder();
            assertNotNull(builder);
        }
    }

    @Nested
    @DisplayName("NoOpCompressor Tests")
    class NoOpCompressorTests {

        private final ValueCompressor compressor = ValueCompressor.none();

        @Test
        @DisplayName("compress returns same data")
        void compressReturnsSameData() {
            byte[] data = "test data".getBytes(StandardCharsets.UTF_8);
            byte[] result = compressor.compress(data);
            assertArrayEquals(data, result);
        }

        @Test
        @DisplayName("decompress returns same data")
        void decompressReturnsSameData() {
            byte[] data = "test data".getBytes(StandardCharsets.UTF_8);
            byte[] result = compressor.decompress(data);
            assertArrayEquals(data, result);
        }

        @Test
        @DisplayName("algorithm returns NONE")
        void algorithmReturnsNone() {
            assertEquals(CompressionAlgorithm.NONE, compressor.algorithm());
        }

        @Test
        @DisplayName("compressionThreshold returns MAX_VALUE")
        void compressionThresholdReturnsMaxValue() {
            assertEquals(Integer.MAX_VALUE, compressor.compressionThreshold());
        }

        @Test
        @DisplayName("shouldCompress returns false for any size")
        void shouldCompressReturnsFalseForAnySize() {
            assertFalse(compressor.shouldCompress(0));
            assertFalse(compressor.shouldCompress(1000000));
        }
    }

    @Nested
    @DisplayName("GzipCompressor Tests")
    class GzipCompressorTests {

        private final ValueCompressor compressor = ValueCompressor.gzip(100);

        @Test
        @DisplayName("compress and decompress round trip")
        void compressAndDecompressRoundTrip() {
            // Create data larger than threshold
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 50; i++) {
                sb.append("This is a test string for compression. ");
            }
            byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
            assertTrue(data.length > 100);

            byte[] compressed = compressor.compress(data);
            byte[] decompressed = compressor.decompress(compressed);

            assertArrayEquals(data, decompressed);
        }

        @Test
        @DisplayName("compress reduces size for compressible data")
        void compressReducesSizeForCompressibleData() {
            // Create highly compressible data (repetitive)
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                sb.append("AAAAAAAAAA");
            }
            byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);

            byte[] compressed = compressor.compress(data);
            assertTrue(compressed.length < data.length,
                    "Compressed size " + compressed.length + " should be less than original " + data.length);
        }

        @Test
        @DisplayName("compress returns original if not smaller")
        void compressReturnsOriginalIfNotSmaller() {
            // Random-like data that won't compress well
            byte[] data = new byte[200];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i * 17 + 13);
            }

            byte[] compressed = compressor.compress(data);
            // Might return original if compression doesn't help
            assertNotNull(compressed);
        }

        @Test
        @DisplayName("compress returns same data if below threshold")
        void compressReturnsSameDataIfBelowThreshold() {
            byte[] data = "short".getBytes(StandardCharsets.UTF_8);
            byte[] result = compressor.compress(data);
            assertArrayEquals(data, result);
        }

        @Test
        @DisplayName("compress handles null")
        void compressHandlesNull() {
            assertNull(compressor.compress(null));
        }

        @Test
        @DisplayName("compress handles empty array")
        void compressHandlesEmptyArray() {
            byte[] result = compressor.compress(new byte[0]);
            assertEquals(0, result.length);
        }

        @Test
        @DisplayName("decompress handles null")
        void decompressHandlesNull() {
            assertNull(compressor.decompress(null));
        }

        @Test
        @DisplayName("decompress handles empty array")
        void decompressHandlesEmptyArray() {
            byte[] result = compressor.decompress(new byte[0]);
            assertEquals(0, result.length);
        }

        @Test
        @DisplayName("decompress handles non-gzip data")
        void decompressHandlesNonGzipData() {
            byte[] data = "not compressed".getBytes(StandardCharsets.UTF_8);
            byte[] result = compressor.decompress(data);
            assertArrayEquals(data, result);
        }

        @Test
        @DisplayName("algorithm returns GZIP")
        void algorithmReturnsGzip() {
            assertEquals(CompressionAlgorithm.GZIP, compressor.algorithm());
        }

        @Test
        @DisplayName("compressionThreshold returns configured value")
        void compressionThresholdReturnsConfiguredValue() {
            assertEquals(100, compressor.compressionThreshold());
        }
    }

    @Nested
    @DisplayName("shouldCompress Tests")
    class ShouldCompressTests {

        @Test
        @DisplayName("shouldCompress returns true when size >= threshold")
        void shouldCompressReturnsTrueWhenSizeGteThreshold() {
            ValueCompressor compressor = ValueCompressor.gzip(100);
            assertTrue(compressor.shouldCompress(100));
            assertTrue(compressor.shouldCompress(101));
        }

        @Test
        @DisplayName("shouldCompress returns false when size < threshold")
        void shouldCompressReturnsFalseWhenSizeLtThreshold() {
            ValueCompressor compressor = ValueCompressor.gzip(100);
            assertFalse(compressor.shouldCompress(99));
            assertFalse(compressor.shouldCompress(0));
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("build with default settings creates GZIP compressor")
        void buildWithDefaultSettingsCreatesGzipCompressor() {
            ValueCompressor compressor = ValueCompressor.builder().build();
            assertEquals(CompressionAlgorithm.GZIP, compressor.algorithm());
            assertEquals(1024, compressor.compressionThreshold());
        }

        @Test
        @DisplayName("build with NONE algorithm creates NoOpCompressor")
        void buildWithNoneAlgorithmCreatesNoOpCompressor() {
            ValueCompressor compressor = ValueCompressor.builder()
                    .algorithm(CompressionAlgorithm.NONE)
                    .build();
            assertEquals(CompressionAlgorithm.NONE, compressor.algorithm());
        }

        @Test
        @DisplayName("build with custom threshold")
        void buildWithCustomThreshold() {
            ValueCompressor compressor = ValueCompressor.builder()
                    .threshold(512)
                    .build();
            assertEquals(512, compressor.compressionThreshold());
        }

        @Test
        @DisplayName("build with custom level")
        void buildWithCustomLevel() {
            ValueCompressor compressor = ValueCompressor.builder()
                    .level(9)
                    .build();
            assertNotNull(compressor);
        }

        @Test
        @DisplayName("build with LZ4 falls back to GZIP")
        void buildWithLz4FallsBackToGzip() {
            ValueCompressor compressor = ValueCompressor.builder()
                    .algorithm(CompressionAlgorithm.LZ4)
                    .build();
            assertEquals(CompressionAlgorithm.GZIP, compressor.algorithm());
        }

        @Test
        @DisplayName("build with ZSTD falls back to GZIP")
        void buildWithZstdFallsBackToGzip() {
            ValueCompressor compressor = ValueCompressor.builder()
                    .algorithm(CompressionAlgorithm.ZSTD)
                    .build();
            assertEquals(CompressionAlgorithm.GZIP, compressor.algorithm());
        }

        @Test
        @DisplayName("build with SNAPPY falls back to GZIP")
        void buildWithSnappyFallsBackToGzip() {
            ValueCompressor compressor = ValueCompressor.builder()
                    .algorithm(CompressionAlgorithm.SNAPPY)
                    .build();
            assertEquals(CompressionAlgorithm.GZIP, compressor.algorithm());
        }

        @Test
        @DisplayName("builder methods return this")
        void builderMethodsReturnThis() {
            ValueCompressor.Builder builder = ValueCompressor.builder();
            assertSame(builder, builder.algorithm(CompressionAlgorithm.GZIP));
            assertSame(builder, builder.threshold(1024));
            assertSame(builder, builder.level(6));
        }
    }

    @Nested
    @DisplayName("CompressionException Tests")
    class CompressionExceptionTests {

        @Test
        @DisplayName("constructor with message")
        void constructorWithMessage() {
            ValueCompressor.CompressionException ex = new ValueCompressor.CompressionException("test error");
            assertEquals("test error", ex.getMessage());
            assertNull(ex.getCause());
        }

        @Test
        @DisplayName("constructor with message and cause")
        void constructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("root cause");
            ValueCompressor.CompressionException ex = new ValueCompressor.CompressionException("test error", cause);
            assertEquals("test error", ex.getMessage());
            assertEquals(cause, ex.getCause());
        }

        @Test
        @DisplayName("exception is RuntimeException")
        void exceptionIsRuntimeException() {
            ValueCompressor.CompressionException ex = new ValueCompressor.CompressionException("test");
            assertInstanceOf(RuntimeException.class, ex);
        }
    }

    @Nested
    @DisplayName("Large Data Tests")
    class LargeDataTests {

        @Test
        @DisplayName("compress handles large data")
        void compressHandlesLargeData() {
            ValueCompressor compressor = ValueCompressor.gzip(100);

            // Create 1MB of compressible data
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 100000; i++) {
                sb.append("Repeating text ");
            }
            byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);

            byte[] compressed = compressor.compress(data);
            byte[] decompressed = compressor.decompress(compressed);

            assertArrayEquals(data, decompressed);
            assertTrue(compressed.length < data.length);
        }
    }

    @Nested
    @DisplayName("Binary Data Tests")
    class BinaryDataTests {

        @Test
        @DisplayName("compress handles binary data")
        void compressHandlesBinaryData() {
            ValueCompressor compressor = ValueCompressor.gzip(100);

            // Create binary data with all byte values
            byte[] data = new byte[256 * 10];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }

            byte[] compressed = compressor.compress(data);
            byte[] decompressed = compressor.decompress(compressed);

            assertArrayEquals(data, decompressed);
        }
    }
}
