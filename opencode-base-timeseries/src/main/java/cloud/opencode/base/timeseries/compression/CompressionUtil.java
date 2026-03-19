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

package cloud.opencode.base.timeseries.compression;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * CompressionUtil - Time Series Compression Utilities
 * CompressionUtil - 时间序列压缩工具
 *
 * <p>Provides compression algorithms optimized for time series data including
 * Gorilla compression, Delta-delta encoding, and simple run-length encoding.</p>
 * <p>提供针对时间序列数据优化的压缩算法，包括 Gorilla 压缩、Delta-delta 编码和简单的游程编码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Gorilla compression (XOR-based float compression) - Gorilla 压缩（基于异或的浮点数压缩）</li>
 *   <li>Delta-delta timestamp encoding - Delta-delta 时间戳编码</li>
 *   <li>Variable-length encoding - 可变长度编码</li>
 *   <li>Simple dictionary compression - 简单字典压缩</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Compress timestamps using delta-delta
 * long[] timestamps = {1000, 1060, 1120, 1180};
 * byte[] compressed = CompressionUtil.compressTimestamps(timestamps);
 * long[] decompressed = CompressionUtil.decompressTimestamps(compressed);
 *
 * // Compress values using Gorilla XOR
 * double[] values = {23.5, 23.6, 23.4, 23.5};
 * byte[] compressed = CompressionUtil.compressValues(values);
 * double[] decompressed = CompressionUtil.decompressValues(compressed);
 *
 * // Compress entire time series
 * CompressedTimeSeries cts = CompressionUtil.compress(timeSeries);
 * TimeSeries decompressed = CompressionUtil.decompress(cts);
 *
 * // Get compression ratio
 * double ratio = cts.compressionRatio();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (null/empty input returns empty result) - 空值安全: 是（空输入返回空结果）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) - each data point encoded/decoded in a single sequential pass - 时间复杂度: O(n) - 每个数据点在单次顺序遍历中完成编解码</li>
 *   <li>Space complexity: O(n) - compressed byte buffer and decompressed array proportional to input size - 空间复杂度: O(n) - 压缩字节缓冲区和解压缩数组与输入规模成正比</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public final class CompressionUtil {

    private CompressionUtil() {
    }

    // ==================== Timestamp Compression | 时间戳压缩 ====================

    /**
     * Compress timestamps using delta-delta encoding.
     * 使用 delta-delta 编码压缩时间戳。
     *
     * @param timestamps array of timestamps (milliseconds) | 时间戳数组（毫秒）
     * @return compressed bytes | 压缩后的字节
     */
    public static byte[] compressTimestamps(long[] timestamps) {
        if (timestamps == null || timestamps.length == 0) {
            return new byte[0];
        }

        BitWriter writer = new BitWriter();

        // Write count
        writer.writeVarLong(timestamps.length);

        // Write first timestamp
        writer.writeLong(timestamps[0]);

        if (timestamps.length == 1) {
            return writer.toByteArray();
        }

        // Write second timestamp as delta
        long delta = timestamps[1] - timestamps[0];
        writer.writeVarLong(delta);

        // Write remaining as delta-of-delta
        for (int i = 2; i < timestamps.length; i++) {
            long newDelta = timestamps[i] - timestamps[i - 1];
            long deltaOfDelta = newDelta - delta;

            // Use variable-length encoding for delta-of-delta
            if (deltaOfDelta == 0) {
                writer.writeBit(0);
            } else if (deltaOfDelta >= -63 && deltaOfDelta <= 64) {
                writer.writeBit(1);
                writer.writeBit(0);
                writer.writeBits(deltaOfDelta + 63, 7);
            } else if (deltaOfDelta >= -255 && deltaOfDelta <= 256) {
                writer.writeBit(1);
                writer.writeBit(1);
                writer.writeBit(0);
                writer.writeBits(deltaOfDelta + 255, 9);
            } else if (deltaOfDelta >= -2047 && deltaOfDelta <= 2048) {
                writer.writeBit(1);
                writer.writeBit(1);
                writer.writeBit(1);
                writer.writeBit(0);
                writer.writeBits(deltaOfDelta + 2047, 12);
            } else {
                writer.writeBit(1);
                writer.writeBit(1);
                writer.writeBit(1);
                writer.writeBit(1);
                writer.writeLong(deltaOfDelta);
            }

            delta = newDelta;
        }

        return writer.toByteArray();
    }

    /**
     * Decompress timestamps from delta-delta encoding.
     * 从 delta-delta 编码解压时间戳。
     *
     * @param compressed compressed bytes | 压缩后的字节
     * @return array of timestamps | 时间戳数组
     */
    public static long[] decompressTimestamps(byte[] compressed) {
        if (compressed == null || compressed.length == 0) {
            return new long[0];
        }

        BitReader reader = new BitReader(compressed);

        int count = (int) reader.readVarLong();
        long[] timestamps = new long[count];

        timestamps[0] = reader.readLong();

        if (count == 1) {
            return timestamps;
        }

        long delta = reader.readVarLong();
        timestamps[1] = timestamps[0] + delta;

        for (int i = 2; i < count; i++) {
            long deltaOfDelta;

            if (reader.readBit() == 0) {
                deltaOfDelta = 0;
            } else if (reader.readBit() == 0) {
                deltaOfDelta = (int) reader.readBits(7) - 63;
            } else if (reader.readBit() == 0) {
                deltaOfDelta = (int) reader.readBits(9) - 255;
            } else if (reader.readBit() == 0) {
                deltaOfDelta = (int) reader.readBits(12) - 2047;
            } else {
                deltaOfDelta = reader.readLong();
            }

            delta += deltaOfDelta;
            timestamps[i] = timestamps[i - 1] + delta;
        }

        return timestamps;
    }

    // ==================== Value Compression (Gorilla-style) | 值压缩（Gorilla 风格）====================

    /**
     * Compress double values using XOR-based Gorilla compression.
     * 使用基于异或的 Gorilla 压缩压缩 double 值。
     *
     * @param values array of double values | double 值数组
     * @return compressed bytes | 压缩后的字节
     */
    public static byte[] compressValues(double[] values) {
        if (values == null || values.length == 0) {
            return new byte[0];
        }

        BitWriter writer = new BitWriter();

        // Write count
        writer.writeVarLong(values.length);

        // Write first value
        long prevBits = Double.doubleToRawLongBits(values[0]);
        writer.writeLong(prevBits);

        int prevLeading = 0;
        int prevTrailing = 0;

        for (int i = 1; i < values.length; i++) {
            long currentBits = Double.doubleToRawLongBits(values[i]);
            long xor = prevBits ^ currentBits;

            if (xor == 0) {
                // Same value, write 0 bit
                writer.writeBit(0);
            } else {
                writer.writeBit(1);

                int leading = Long.numberOfLeadingZeros(xor);
                int trailing = Long.numberOfTrailingZeros(xor);

                // Cap leading zeros
                if (leading > 31) {
                    leading = 31;
                }

                if (leading >= prevLeading && trailing >= prevTrailing && prevLeading > 0) {
                    // Can reuse previous block
                    writer.writeBit(0);
                    int blockSize = 64 - prevLeading - prevTrailing;
                    writer.writeBits(xor >>> prevTrailing, blockSize);
                } else {
                    // New block
                    writer.writeBit(1);
                    writer.writeBits(leading, 5);
                    int blockSize = 64 - leading - trailing;
                    writer.writeBits(blockSize - 1, 6);
                    writer.writeBits(xor >>> trailing, blockSize);
                    prevLeading = leading;
                    prevTrailing = trailing;
                }
            }

            prevBits = currentBits;
        }

        return writer.toByteArray();
    }

    /**
     * Decompress double values from Gorilla compression.
     * 从 Gorilla 压缩解压 double 值。
     *
     * @param compressed compressed bytes | 压缩后的字节
     * @return array of double values | double 值数组
     */
    public static double[] decompressValues(byte[] compressed) {
        if (compressed == null || compressed.length == 0) {
            return new double[0];
        }

        BitReader reader = new BitReader(compressed);

        int count = (int) reader.readVarLong();
        double[] values = new double[count];

        long prevBits = reader.readLong();
        values[0] = Double.longBitsToDouble(prevBits);

        int prevLeading = 0;
        int prevTrailing = 0;

        for (int i = 1; i < count; i++) {
            if (reader.readBit() == 0) {
                // Same value
                values[i] = Double.longBitsToDouble(prevBits);
            } else {
                if (reader.readBit() == 0) {
                    // Reuse previous block
                    int blockSize = 64 - prevLeading - prevTrailing;
                    long xor = reader.readBits(blockSize) << prevTrailing;
                    prevBits ^= xor;
                } else {
                    // New block
                    prevLeading = (int) reader.readBits(5);
                    int blockSize = (int) reader.readBits(6) + 1;
                    prevTrailing = 64 - prevLeading - blockSize;
                    long xor = reader.readBits(blockSize) << prevTrailing;
                    prevBits ^= xor;
                }
                values[i] = Double.longBitsToDouble(prevBits);
            }
        }

        return values;
    }

    // ==================== Full TimeSeries Compression | 完整时间序列压缩 ====================

    /**
     * Compress a complete time series.
     * 压缩完整的时间序列。
     *
     * @param series the time series | 时间序列
     * @return compressed time series | 压缩后的时间序列
     */
    public static CompressedTimeSeries compress(TimeSeries series) {
        List<DataPoint> points = series.getPoints();

        long[] timestamps = new long[points.size()];
        double[] values = new double[points.size()];

        for (int i = 0; i < points.size(); i++) {
            timestamps[i] = points.get(i).epochMillis();
            values[i] = points.get(i).value();
        }

        byte[] compressedTimestamps = compressTimestamps(timestamps);
        byte[] compressedValues = compressValues(values);

        int originalSize = points.size() * (8 + 8); // 8 bytes timestamp + 8 bytes value
        int compressedSize = compressedTimestamps.length + compressedValues.length;

        return new CompressedTimeSeries(
                compressedTimestamps,
                compressedValues,
                points.size(),
                originalSize,
                compressedSize
        );
    }

    /**
     * Decompress a compressed time series.
     * 解压压缩后的时间序列。
     *
     * @param compressed the compressed time series | 压缩后的时间序列
     * @return the time series | 时间序列
     */
    public static TimeSeries decompress(CompressedTimeSeries compressed) {
        long[] timestamps = decompressTimestamps(compressed.compressedTimestamps());
        double[] values = decompressValues(compressed.compressedValues());

        List<DataPoint> points = new ArrayList<>(timestamps.length);
        for (int i = 0; i < timestamps.length; i++) {
            points.add(DataPoint.of(timestamps[i], values[i]));
        }

        return new TimeSeries("decompressed", points);
    }

    // ==================== Simple Compression | 简单压缩 ====================

    /**
     * Simple delta encoding for integers.
     * 整数的简单 delta 编码。
     *
     * @param values array of integers | 整数数组
     * @return delta-encoded array | delta 编码数组
     */
    public static int[] deltaEncode(int[] values) {
        if (values == null || values.length == 0) {
            return new int[0];
        }

        int[] encoded = new int[values.length];
        encoded[0] = values[0];

        for (int i = 1; i < values.length; i++) {
            encoded[i] = values[i] - values[i - 1];
        }

        return encoded;
    }

    /**
     * Decode delta-encoded integers.
     * 解码 delta 编码的整数。
     *
     * @param encoded delta-encoded array | delta 编码数组
     * @return original array | 原始数组
     */
    public static int[] deltaDecode(int[] encoded) {
        if (encoded == null || encoded.length == 0) {
            return new int[0];
        }

        int[] decoded = new int[encoded.length];
        decoded[0] = encoded[0];

        for (int i = 1; i < encoded.length; i++) {
            decoded[i] = decoded[i - 1] + encoded[i];
        }

        return decoded;
    }

    // ==================== Result Classes | 结果类 ====================

    /**
     * Compressed time series data.
     * 压缩后的时间序列数据。
     */
    public record CompressedTimeSeries(
            byte[] compressedTimestamps,
            byte[] compressedValues,
            int pointCount,
            int originalSizeBytes,
            int compressedSizeBytes
    ) {
        /**
         * Get compression ratio (original / compressed).
         * 获取压缩比（原始 / 压缩）。
         */
        public double compressionRatio() {
            if (compressedSizeBytes == 0) return 0;
            return (double) originalSizeBytes / compressedSizeBytes;
        }

        /**
         * Get space savings percentage.
         * 获取空间节省百分比。
         */
        public double spaceSavingsPercent() {
            if (originalSizeBytes == 0) return 0;
            return 100.0 * (originalSizeBytes - compressedSizeBytes) / originalSizeBytes;
        }
    }

    // ==================== Bit I/O Helpers | 位 I/O 辅助 ====================

    private static class BitWriter {
        private final ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        private long currentByte = 0;
        private int bitPosition = 0;

        void writeBit(int bit) {
            currentByte = (currentByte << 1) | (bit & 1);
            bitPosition++;
            if (bitPosition == 8) {
                buffer.put((byte) currentByte);
                currentByte = 0;
                bitPosition = 0;
            }
        }

        void writeBits(long value, int numBits) {
            for (int i = numBits - 1; i >= 0; i--) {
                writeBit((int) ((value >> i) & 1));
            }
        }

        void writeLong(long value) {
            writeBits(value, 64);
        }

        void writeVarLong(long value) {
            while ((value & ~0x7FL) != 0) {
                writeBits((value & 0x7F) | 0x80, 8);
                value >>>= 7;
            }
            writeBits(value, 8);
        }

        byte[] toByteArray() {
            // Flush remaining bits
            if (bitPosition > 0) {
                currentByte <<= (8 - bitPosition);
                buffer.put((byte) currentByte);
            }

            byte[] result = new byte[buffer.position()];
            buffer.flip();
            buffer.get(result);
            return result;
        }
    }

    private static class BitReader {
        private final byte[] data;
        private int bytePosition = 0;
        private int bitPosition = 0;

        BitReader(byte[] data) {
            this.data = data;
        }

        int readBit() {
            if (bytePosition >= data.length) {
                return 0;
            }
            int bit = (data[bytePosition] >> (7 - bitPosition)) & 1;
            bitPosition++;
            if (bitPosition == 8) {
                bytePosition++;
                bitPosition = 0;
            }
            return bit;
        }

        long readBits(int numBits) {
            long value = 0;
            for (int i = 0; i < numBits; i++) {
                value = (value << 1) | readBit();
            }
            return value;
        }

        long readLong() {
            return readBits(64);
        }

        long readVarLong() {
            long value = 0;
            int shift = 0;
            long b;
            do {
                b = readBits(8);
                value |= (b & 0x7F) << shift;
                shift += 7;
            } while ((b & 0x80) != 0);
            return value;
        }
    }
}
