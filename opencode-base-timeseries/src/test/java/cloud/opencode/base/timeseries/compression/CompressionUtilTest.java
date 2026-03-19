package cloud.opencode.base.timeseries.compression;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CompressionUtilTest Tests
 * CompressionUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("CompressionUtil Tests")
class CompressionUtilTest {

    private TimeSeries series;
    private Instant baseTime;

    @BeforeEach
    void setUp() {
        baseTime = Instant.parse("2024-01-01T00:00:00Z");
        series = new TimeSeries("test-series");

        // Create test data
        for (int i = 0; i < 100; i++) {
            DataPoint point = DataPoint.of(baseTime.plusSeconds(i), 50.0 + Math.sin(i * 0.1) * 10);
            series.add(point);
        }
    }

    @Nested
    @DisplayName("Timestamp Compression Tests")
    class TimestampCompressionTests {

        @Test
        @DisplayName("compressTimestamps should reduce data size")
        void compressTimestampsShouldReduceDataSize() {
            long[] timestamps = new long[100];
            for (int i = 0; i < 100; i++) {
                timestamps[i] = baseTime.plusSeconds(i).toEpochMilli();
            }

            byte[] compressed = CompressionUtil.compressTimestamps(timestamps);

            // Compressed should be smaller than raw longs (8 bytes each)
            assertThat(compressed.length).isLessThan(timestamps.length * 8);
        }

        @Test
        @DisplayName("decompressTimestamps should restore original timestamps")
        void decompressTimestampsShouldRestoreOriginalTimestamps() {
            long[] original = new long[100];
            for (int i = 0; i < 100; i++) {
                original[i] = baseTime.plusSeconds(i).toEpochMilli();
            }

            byte[] compressed = CompressionUtil.compressTimestamps(original);
            long[] decompressed = CompressionUtil.decompressTimestamps(compressed);

            assertThat(decompressed).isEqualTo(original);
        }

        @Test
        @DisplayName("compressTimestamps should handle empty array")
        void compressTimestampsShouldHandleEmptyArray() {
            byte[] compressed = CompressionUtil.compressTimestamps(new long[0]);

            assertThat(compressed).isNotNull();
            assertThat(compressed).isEmpty();
        }

        @Test
        @DisplayName("compressTimestamps should handle single timestamp")
        void compressTimestampsShouldHandleSingleTimestamp() {
            long[] single = new long[]{baseTime.toEpochMilli()};

            byte[] compressed = CompressionUtil.compressTimestamps(single);
            long[] decompressed = CompressionUtil.decompressTimestamps(compressed);

            assertThat(decompressed).isEqualTo(single);
        }

        @Test
        @DisplayName("compressTimestamps should handle null array")
        void compressTimestampsShouldHandleNullArray() {
            byte[] compressed = CompressionUtil.compressTimestamps(null);

            assertThat(compressed).isEmpty();
        }
    }

    @Nested
    @DisplayName("Value Compression Tests")
    class ValueCompressionTests {

        @Test
        @DisplayName("compressValues should compress similar values")
        void compressValuesShouldCompressSimilarValues() {
            double[] values = new double[100];
            for (int i = 0; i < 100; i++) {
                values[i] = 50.0 + Math.sin(i * 0.1) * 10;
            }

            byte[] compressed = CompressionUtil.compressValues(values);

            assertThat(compressed).isNotNull();
            assertThat(compressed.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("decompressValues should restore original values")
        void decompressValuesShouldRestoreOriginalValues() {
            double[] original = new double[100];
            for (int i = 0; i < 100; i++) {
                original[i] = 50.0 + Math.sin(i * 0.1) * 10;
            }

            byte[] compressed = CompressionUtil.compressValues(original);
            double[] decompressed = CompressionUtil.decompressValues(compressed);

            assertThat(decompressed).hasSize(original.length);
            for (int i = 0; i < original.length; i++) {
                assertThat(decompressed[i]).isCloseTo(original[i], within(0.0001));
            }
        }

        @Test
        @DisplayName("compressValues should handle empty array")
        void compressValuesShouldHandleEmptyArray() {
            byte[] compressed = CompressionUtil.compressValues(new double[0]);

            assertThat(compressed).isNotNull();
            assertThat(compressed).isEmpty();
        }

        @Test
        @DisplayName("compressValues should handle null array")
        void compressValuesShouldHandleNullArray() {
            byte[] compressed = CompressionUtil.compressValues(null);

            assertThat(compressed).isEmpty();
        }

        @Test
        @DisplayName("compressValues should handle constant values efficiently")
        void compressValuesShouldHandleConstantValuesEfficiently() {
            double[] values = new double[100];
            for (int i = 0; i < 100; i++) {
                values[i] = 42.0;
            }

            byte[] compressed = CompressionUtil.compressValues(values);
            double[] decompressed = CompressionUtil.decompressValues(compressed);

            assertThat(decompressed).containsOnly(42.0);
        }
    }

    @Nested
    @DisplayName("Full Compression Tests")
    class FullCompressionTests {

        @Test
        @DisplayName("compress should create CompressedTimeSeries")
        void compressShouldCreateCompressedTimeSeries() {
            CompressionUtil.CompressedTimeSeries compressed = CompressionUtil.compress(series);

            assertThat(compressed).isNotNull();
            assertThat(compressed.pointCount()).isEqualTo(series.size());
        }

        @Test
        @DisplayName("decompress should restore original series")
        void decompressShouldRestoreOriginalSeries() {
            CompressionUtil.CompressedTimeSeries compressed = CompressionUtil.compress(series);
            TimeSeries restored = CompressionUtil.decompress(compressed);

            assertThat(restored.size()).isEqualTo(series.size());

            List<DataPoint> originalPoints = series.getPoints();
            List<DataPoint> restoredPoints = restored.getPoints();

            for (int i = 0; i < originalPoints.size(); i++) {
                assertThat(restoredPoints.get(i).timestamp())
                    .isEqualTo(originalPoints.get(i).timestamp());
                assertThat(restoredPoints.get(i).value())
                    .isCloseTo(originalPoints.get(i).value(), within(0.0001));
            }
        }

        @Test
        @DisplayName("compression ratio should be calculated correctly")
        void compressionRatioShouldBeCalculatedCorrectly() {
            CompressionUtil.CompressedTimeSeries compressed = CompressionUtil.compress(series);

            double ratio = compressed.compressionRatio();

            assertThat(ratio).isGreaterThan(0);
            assertThat(compressed.originalSizeBytes()).isGreaterThan(0);
            assertThat(compressed.compressedSizeBytes()).isGreaterThan(0);
        }

        @Test
        @DisplayName("space savings should be calculated correctly")
        void spaceSavingsShouldBeCalculatedCorrectly() {
            CompressionUtil.CompressedTimeSeries compressed = CompressionUtil.compress(series);

            double savings = compressed.spaceSavingsPercent();

            assertThat(savings).isBetween(-100.0, 100.0);
        }
    }

    @Nested
    @DisplayName("Delta Encoding Tests")
    class DeltaEncodingTests {

        @Test
        @DisplayName("deltaEncode should create differences")
        void deltaEncodeShouldCreateDifferences() {
            int[] values = {1000, 1010, 1020, 1030, 1040};

            int[] deltas = CompressionUtil.deltaEncode(values);

            assertThat(deltas).hasSize(values.length);
            assertThat(deltas[0]).isEqualTo(1000); // First value stays
            assertThat(deltas[1]).isEqualTo(10);
            assertThat(deltas[2]).isEqualTo(10);
        }

        @Test
        @DisplayName("deltaDecode should restore original values")
        void deltaDecodeShouldRestoreOriginalValues() {
            int[] original = {1000, 1010, 1020, 1030, 1040};

            int[] encoded = CompressionUtil.deltaEncode(original);
            int[] decoded = CompressionUtil.deltaDecode(encoded);

            assertThat(decoded).isEqualTo(original);
        }

        @Test
        @DisplayName("deltaEncode should handle empty array")
        void deltaEncodeShouldHandleEmptyArray() {
            int[] deltas = CompressionUtil.deltaEncode(new int[0]);

            assertThat(deltas).isEmpty();
        }

        @Test
        @DisplayName("deltaEncode should handle single value")
        void deltaEncodeShouldHandleSingleValue() {
            int[] original = {1000};

            int[] encoded = CompressionUtil.deltaEncode(original);
            int[] decoded = CompressionUtil.deltaDecode(encoded);

            assertThat(decoded).isEqualTo(original);
        }

        @Test
        @DisplayName("deltaEncode should handle null array")
        void deltaEncodeShouldHandleNullArray() {
            int[] deltas = CompressionUtil.deltaEncode(null);

            assertThat(deltas).isEmpty();
        }
    }

    @Nested
    @DisplayName("CompressedTimeSeries Record Tests")
    class CompressedTimeSeriesRecordTests {

        @Test
        @DisplayName("CompressedTimeSeries should store all fields")
        void compressedTimeSeriesShouldStoreAllFields() {
            byte[] timestamps = new byte[]{1, 2, 3};
            byte[] values = new byte[]{4, 5, 6};
            int pointCount = 10;
            int originalSize = 160;
            int compressedSize = 6;

            CompressionUtil.CompressedTimeSeries cts =
                new CompressionUtil.CompressedTimeSeries(timestamps, values, pointCount, originalSize, compressedSize);

            assertThat(cts.compressedTimestamps()).isEqualTo(timestamps);
            assertThat(cts.compressedValues()).isEqualTo(values);
            assertThat(cts.pointCount()).isEqualTo(pointCount);
            assertThat(cts.originalSizeBytes()).isEqualTo(originalSize);
            assertThat(cts.compressedSizeBytes()).isEqualTo(compressedSize);
        }

        @Test
        @DisplayName("compressionRatio should return correct ratio")
        void compressionRatioShouldReturnCorrectRatio() {
            CompressionUtil.CompressedTimeSeries cts =
                new CompressionUtil.CompressedTimeSeries(new byte[10], new byte[10], 10, 160, 20);

            double ratio = cts.compressionRatio();

            assertThat(ratio).isEqualTo(8.0); // 160 / 20 = 8
        }

        @Test
        @DisplayName("spaceSavingsPercent should return correct percentage")
        void spaceSavingsPercentShouldReturnCorrectPercentage() {
            CompressionUtil.CompressedTimeSeries cts =
                new CompressionUtil.CompressedTimeSeries(new byte[10], new byte[10], 10, 100, 25);

            double savings = cts.spaceSavingsPercent();

            assertThat(savings).isEqualTo(75.0); // (100 - 25) / 100 * 100 = 75%
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty series")
        void shouldHandleEmptySeries() {
            TimeSeries empty = new TimeSeries("empty");

            CompressionUtil.CompressedTimeSeries compressed = CompressionUtil.compress(empty);

            assertThat(compressed.pointCount()).isZero();
        }

        @Test
        @DisplayName("should handle series with single point")
        void shouldHandleSeriesWithSinglePoint() {
            TimeSeries single = new TimeSeries("single");
            single.add(baseTime, 42.0);

            CompressionUtil.CompressedTimeSeries compressed = CompressionUtil.compress(single);
            TimeSeries restored = CompressionUtil.decompress(compressed);

            assertThat(restored.size()).isEqualTo(1);
            assertThat(restored.getFirst().map(DataPoint::value)).contains(42.0);
        }

        @Test
        @DisplayName("should handle series with constant values")
        void shouldHandleSeriesWithConstantValues() {
            TimeSeries constant = new TimeSeries("constant");
            for (int i = 0; i < 100; i++) {
                constant.add(baseTime.plusSeconds(i), 50.0);
            }

            CompressionUtil.CompressedTimeSeries compressed = CompressionUtil.compress(constant);
            TimeSeries restored = CompressionUtil.decompress(compressed);

            assertThat(restored.size()).isEqualTo(100);
        }

        @Test
        @DisplayName("should handle series with irregular timestamps")
        void shouldHandleSeriesWithIrregularTimestamps() {
            TimeSeries irregular = new TimeSeries("irregular");
            irregular.add(baseTime, 1.0);
            irregular.add(baseTime.plusSeconds(10), 2.0);
            irregular.add(baseTime.plusSeconds(100), 3.0);
            irregular.add(baseTime.plusSeconds(1000), 4.0);

            CompressionUtil.CompressedTimeSeries compressed = CompressionUtil.compress(irregular);
            TimeSeries restored = CompressionUtil.decompress(compressed);

            assertThat(restored.size()).isEqualTo(4);
        }

        @Test
        @DisplayName("should handle negative values")
        void shouldHandleNegativeValues() {
            TimeSeries negative = new TimeSeries("negative");
            for (int i = 0; i < 10; i++) {
                negative.add(baseTime.plusSeconds(i), -100.0 + i);
            }

            CompressionUtil.CompressedTimeSeries compressed = CompressionUtil.compress(negative);
            TimeSeries restored = CompressionUtil.decompress(compressed);

            assertThat(restored.size()).isEqualTo(10);
            assertThat(restored.getFirst().map(DataPoint::value)).contains(-100.0);
        }

        @Test
        @DisplayName("should handle very large values")
        void shouldHandleVeryLargeValues() {
            TimeSeries large = new TimeSeries("large");
            for (int i = 0; i < 10; i++) {
                large.add(baseTime.plusSeconds(i), Double.MAX_VALUE / 2);
            }

            CompressionUtil.CompressedTimeSeries compressed = CompressionUtil.compress(large);
            TimeSeries restored = CompressionUtil.decompress(compressed);

            assertThat(restored.size()).isEqualTo(10);
        }
    }
}
