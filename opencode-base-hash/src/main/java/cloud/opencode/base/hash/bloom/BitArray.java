package cloud.opencode.base.hash.bloom;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Bit array implementation for bloom filter
 * 布隆过滤器的位数组实现
 *
 * <p>Provides an efficient bit array implementation with optional
 * thread-safe operations using CAS.</p>
 * <p>提供高效的位数组实现，可选择使用CAS进行线程安全操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Compact storage using long[] - 使用long[]的紧凑存储</li>
 *   <li>Optional thread-safe mode - 可选的线程安全模式</li>
 *   <li>Serialization support - 序列化支持</li>
 *   <li>Merge operations - 合并操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // BitArray is used internally by BloomFilter
 * // BitArray由BloomFilter内部使用
 * BitArray bits = new BitArray(1024);
 * bits.set(42);
 * boolean isSet = bits.get(42); // true
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public final class BitArray implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final long bitSize;
    private final long[] data;
    private final transient AtomicLongArray atomicData;
    private final boolean threadSafe;

    /**
     * Creates a non-thread-safe bit array
     * 创建非线程安全的位数组
     *
     * @param bitSize number of bits | 位数
     */
    public BitArray(long bitSize) {
        this(bitSize, false);
    }

    /**
     * Creates a bit array with optional thread safety
     * 创建可选线程安全的位数组
     *
     * @param bitSize    number of bits | 位数
     * @param threadSafe whether to be thread-safe | 是否线程安全
     */
    public BitArray(long bitSize, boolean threadSafe) {
        if (bitSize <= 0) {
            throw new IllegalArgumentException("Bit size must be positive");
        }
        this.bitSize = bitSize;
        this.threadSafe = threadSafe;
        int longCount = (int) ((bitSize + 63) / 64);
        this.data = threadSafe ? null : new long[longCount];
        this.atomicData = threadSafe ? new AtomicLongArray(longCount) : null;
    }

    /**
     * Creates a bit array from existing data
     * 从现有数据创建位数组
     *
     * @param bitSize number of bits | 位数
     * @param data    existing data | 现有数据
     */
    public BitArray(long bitSize, long[] data) {
        this.bitSize = bitSize;
        this.threadSafe = false;
        this.data = Arrays.copyOf(data, data.length);
        this.atomicData = null;
    }

    /**
     * Sets a bit to true
     * 将位设置为true
     *
     * @param index bit index | 位索引
     * @return true if the bit was changed | 如果位被更改返回true
     */
    public boolean set(long index) {
        checkIndex(index);
        int longIndex = (int) (index >>> 6);
        long mask = 1L << index;

        if (threadSafe) {
            long oldValue, newValue;
            do {
                oldValue = atomicData.get(longIndex);
                if ((oldValue & mask) != 0) {
                    return false;
                }
                newValue = oldValue | mask;
            } while (!atomicData.compareAndSet(longIndex, oldValue, newValue));
            return true;
        } else {
            long oldValue = data[longIndex];
            data[longIndex] |= mask;
            return (oldValue & mask) == 0;
        }
    }

    /**
     * Gets a bit value
     * 获取位值
     *
     * @param index bit index | 位索引
     * @return true if bit is set | 如果位已设置返回true
     */
    public boolean get(long index) {
        checkIndex(index);
        int longIndex = (int) (index >>> 6);
        long mask = 1L << index;

        if (threadSafe) {
            return (atomicData.get(longIndex) & mask) != 0;
        } else {
            return (data[longIndex] & mask) != 0;
        }
    }

    /**
     * Clears a bit
     * 清除位
     *
     * @param index bit index | 位索引
     * @return true if the bit was changed | 如果位被更改返回true
     */
    public boolean clear(long index) {
        checkIndex(index);
        int longIndex = (int) (index >>> 6);
        long mask = 1L << index;

        if (threadSafe) {
            long oldValue, newValue;
            do {
                oldValue = atomicData.get(longIndex);
                if ((oldValue & mask) == 0) {
                    return false;
                }
                newValue = oldValue & ~mask;
            } while (!atomicData.compareAndSet(longIndex, oldValue, newValue));
            return true;
        } else {
            long oldValue = data[longIndex];
            data[longIndex] &= ~mask;
            return (oldValue & mask) != 0;
        }
    }

    /**
     * Gets the number of bits
     * 获取位数
     *
     * @return bit count | 位数
     */
    public long bitSize() {
        return bitSize;
    }

    /**
     * Gets the number of set bits (population count)
     * 获取已设置的位数（人口计数）
     *
     * @return number of set bits | 已设置的位数
     */
    public long bitCount() {
        long count = 0;
        int length = (int) ((bitSize + 63) / 64);
        for (int i = 0; i < length; i++) {
            long value = threadSafe ? atomicData.get(i) : data[i];
            count += Long.bitCount(value);
        }
        return count;
    }

    /**
     * Clears all bits
     * 清除所有位
     */
    public void clearAll() {
        int length = (int) ((bitSize + 63) / 64);
        if (threadSafe) {
            for (int i = 0; i < length; i++) {
                atomicData.set(i, 0);
            }
        } else {
            Arrays.fill(data, 0);
        }
    }

    /**
     * Performs OR operation with another bit array
     * 与另一个位数组执行OR操作
     *
     * @param other the other bit array | 另一个位数组
     * @return this bit array | 此位数组
     */
    public BitArray or(BitArray other) {
        if (this.bitSize != other.bitSize) {
            throw new IllegalArgumentException("Bit arrays must have the same size");
        }
        int length = (int) ((bitSize + 63) / 64);
        if (threadSafe) {
            for (int i = 0; i < length; i++) {
                long otherValue = other.threadSafe ? other.atomicData.get(i) : other.data[i];
                long oldValue, newValue;
                do {
                    oldValue = atomicData.get(i);
                    newValue = oldValue | otherValue;
                } while (!atomicData.compareAndSet(i, oldValue, newValue));
            }
        } else {
            for (int i = 0; i < length; i++) {
                long otherValue = other.threadSafe ? other.atomicData.get(i) : other.data[i];
                data[i] |= otherValue;
            }
        }
        return this;
    }

    /**
     * Converts to byte array for serialization
     * 转换为字节数组用于序列化
     *
     * @return byte array | 字节数组
     */
    public byte[] toBytes() {
        int length = (int) ((bitSize + 63) / 64);
        byte[] bytes = new byte[8 + length * 8];

        // Write bit size
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (bitSize >> (i * 8));
        }

        // Write data
        for (int i = 0; i < length; i++) {
            long value = threadSafe ? atomicData.get(i) : data[i];
            for (int j = 0; j < 8; j++) {
                bytes[8 + i * 8 + j] = (byte) (value >> (j * 8));
            }
        }

        return bytes;
    }

    /**
     * Creates a bit array from byte array
     * 从字节数组创建位数组
     *
     * @param bytes byte array | 字节数组
     * @return bit array | 位数组
     * @throws IllegalArgumentException if bytes are invalid | 如果字节无效则抛出异常
     */
    public static BitArray fromBytes(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Byte array cannot be null");
        }
        if (bytes.length < 8) {
            throw new IllegalArgumentException("Invalid byte array: length must be at least 8");
        }

        // Read bit size
        long bitSize = 0;
        for (int i = 0; i < 8; i++) {
            bitSize |= (bytes[i] & 0xFFL) << (i * 8);
        }

        // Validate bit size
        if (bitSize <= 0) {
            throw new IllegalArgumentException("Invalid bit size: " + bitSize);
        }
        // Cap at 2^31 bits (~256MB) to prevent OOM from malicious byte arrays
        if (bitSize > (long) Integer.MAX_VALUE * 64L) {
            throw new IllegalArgumentException("Bit size too large: " + bitSize);
        }

        int length = (int) ((bitSize + 63) / 64);
        int expectedLength = 8 + length * 8;

        // Validate byte array length matches expected size
        if (bytes.length < expectedLength) {
            throw new IllegalArgumentException(
                "Invalid byte array length: expected at least " + expectedLength + ", got " + bytes.length);
        }

        long[] data = new long[length];

        // Read data
        for (int i = 0; i < length; i++) {
            long value = 0;
            for (int j = 0; j < 8; j++) {
                value |= (bytes[8 + i * 8 + j] & 0xFFL) << (j * 8);
            }
            data[i] = value;
        }

        return new BitArray(bitSize, data);
    }

    private void checkIndex(long index) {
        if (index < 0 || index >= bitSize) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + bitSize);
        }
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        int length = (int) ((bitSize + 63) / 64);
        for (int i = 0; i < length; i++) {
            out.writeLong(threadSafe ? atomicData.get(i) : data[i]);
        }
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (bitSize <= 0) {
            throw new java.io.InvalidObjectException("Invalid bitSize: " + bitSize);
        }
        // Cap at 2^31 bits (~256MB) to prevent OOM from malicious serialized data
        if (bitSize > (long) Integer.MAX_VALUE * 64L) {
            throw new java.io.InvalidObjectException(
                    "bitSize too large for deserialization: " + bitSize);
        }
        int length = (int) ((bitSize + 63) / 64);
        if (threadSafe) {
            // Reconstruct transient atomicData from the extra longs written by writeObject.
            // For thread-safe BitArrays, data is null and atomicData is transient (not serialized),
            // so the actual bit data is only in the extra longs written after defaultWriteObject().
            try {
                java.lang.reflect.Field atomicField = BitArray.class.getDeclaredField("atomicData");
                atomicField.setAccessible(true);
                AtomicLongArray restored = new AtomicLongArray(length);
                for (int i = 0; i < length; i++) {
                    restored.set(i, in.readLong());
                }
                atomicField.set(this, restored);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IOException("Failed to restore thread-safe BitArray", e);
            }
        } else {
            // For non-thread-safe BitArrays, data was already restored by defaultReadObject().
            // We must still read the extra longs to keep the stream position correct,
            // since writeObject always writes them.
            for (int i = 0; i < length; i++) {
                in.readLong();
            }
        }
    }
}
