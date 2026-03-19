package cloud.opencode.base.hash.function;

import cloud.opencode.base.hash.Funnel;
import cloud.opencode.base.hash.HashCode;
import cloud.opencode.base.hash.HashFunction;
import cloud.opencode.base.hash.Hasher;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Abstract base class for hash functions
 * 哈希函数抽象基类
 *
 * <p>Provides common implementations for hash function methods,
 * reducing code duplication in concrete implementations.</p>
 * <p>提供哈希函数方法的通用实现，减少具体实现中的代码重复。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Default method implementations - 默认方法实现</li>
 *   <li>AbstractHasher base class - AbstractHasher基类</li>
 *   <li>Common utility methods - 通用工具方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Extend to create custom hash functions
 * // 扩展以创建自定义哈希函数
 * public class MyHash extends AbstractHashFunction {
 *     @Override
 *     public HashCode hash(byte[] data) {
 *         // custom hashing logic
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = input size - O(n), n为输入大小</li>
 *   <li>Space complexity: O(1) for hash state - 哈希状态 O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public abstract class AbstractHashFunction implements HashFunction {

    /**
     * Hash function bits
     */
    protected final int bits;

    /**
     * Algorithm name
     */
    protected final String name;

    /**
     * Creates an abstract hash function
     * 创建抽象哈希函数
     *
     * @param bits hash output bits | 哈希输出位数
     * @param name algorithm name | 算法名称
     */
    protected AbstractHashFunction(int bits, String name) {
        this.bits = bits;
        this.name = name;
    }

    @Override
    public int bits() {
        return bits;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Hasher newHasher(int expectedInputSize) {
        return newHasher();
    }

    @Override
    public HashCode hashBytes(byte[] input) {
        return hashBytes(input, 0, input.length);
    }

    @Override
    public HashCode hashInt(int input) {
        return newHasher(4).putInt(input).hash();
    }

    @Override
    public HashCode hashLong(long input) {
        return newHasher(8).putLong(input).hash();
    }

    @Override
    public <T> HashCode hashObject(T instance, Funnel<? super T> funnel) {
        Hasher hasher = newHasher();
        funnel.funnel(instance, hasher);
        return hasher.hash();
    }

    @Override
    public String toString() {
        return name + "[" + bits + "]";
    }

    // ==================== Abstract Hasher | 抽象Hasher ====================

    /**
     * Abstract base class for Hasher implementations
     * Hasher实现的抽象基类
     */
    protected abstract static class AbstractHasher implements Hasher {

        private boolean used = false;

        @Override
        public Hasher putBytes(byte[] bytes) {
            return putBytes(bytes, 0, bytes.length);
        }

        @Override
        public Hasher putBytes(ByteBuffer buffer) {
            if (buffer.hasArray()) {
                return putBytes(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
            }
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return putBytes(bytes);
        }

        @Override
        public Hasher putShort(short s) {
            return putByte((byte) s).putByte((byte) (s >> 8));
        }

        @Override
        public Hasher putInt(int i) {
            return putByte((byte) i)
                    .putByte((byte) (i >> 8))
                    .putByte((byte) (i >> 16))
                    .putByte((byte) (i >> 24));
        }

        @Override
        public Hasher putLong(long l) {
            for (int i = 0; i < 8; i++) {
                putByte((byte) (l >> (i * 8)));
            }
            return this;
        }

        @Override
        public Hasher putFloat(float f) {
            return putInt(Float.floatToRawIntBits(f));
        }

        @Override
        public Hasher putDouble(double d) {
            return putLong(Double.doubleToRawLongBits(d));
        }

        @Override
        public Hasher putBoolean(boolean b) {
            return putByte(b ? (byte) 1 : (byte) 0);
        }

        @Override
        public Hasher putChar(char c) {
            return putByte((byte) c).putByte((byte) (c >> 8));
        }

        @Override
        public Hasher putString(CharSequence charSequence, Charset charset) {
            return putBytes(charSequence.toString().getBytes(charset));
        }

        @Override
        public <T> Hasher putObject(T instance, Funnel<? super T> funnel) {
            funnel.funnel(instance, this);
            return this;
        }

        @Override
        public final HashCode hash() {
            checkNotUsed();
            used = true;
            return doHash();
        }

        /**
         * Performs the actual hash computation
         * 执行实际的哈希计算
         *
         * @return computed hash code | 计算的哈希码
         */
        protected abstract HashCode doHash();

        /**
         * Checks if this hasher has been used
         * 检查此hasher是否已被使用
         */
        protected void checkNotUsed() {
            if (used) {
                throw new IllegalStateException("Hasher has already been used");
            }
        }
    }

    /**
     * Buffer-based hasher for accumulating bytes
     * 基于缓冲区的hasher用于累积字节
     */
    protected abstract static class BufferedHasher extends AbstractHasher {

        protected ByteBuffer buffer;

        protected BufferedHasher(int initialCapacity) {
            this.buffer = ByteBuffer.allocate(initialCapacity).order(ByteOrder.LITTLE_ENDIAN);
        }

        @Override
        public Hasher putByte(byte b) {
            ensureCapacity(1);
            buffer.put(b);
            return this;
        }

        @Override
        public Hasher putBytes(byte[] bytes, int offset, int length) {
            ensureCapacity(length);
            buffer.put(bytes, offset, length);
            return this;
        }

        /**
         * Ensures buffer has enough capacity
         * 确保缓冲区有足够容量
         */
        protected void ensureCapacity(int additional) {
            if (buffer.remaining() < additional) {
                long doubledCapacity = (long) buffer.capacity() * 2;
                long requiredCapacity = (long) buffer.position() + additional;
                int newCapacity = (int) Math.min(Integer.MAX_VALUE, Math.max(doubledCapacity, requiredCapacity));
                ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity).order(ByteOrder.LITTLE_ENDIAN);
                buffer.flip();
                newBuffer.put(buffer);
                buffer = newBuffer;
            }
        }

        /**
         * Gets accumulated bytes
         * 获取累积的字节
         */
        protected byte[] getBytes() {
            byte[] bytes = new byte[buffer.position()];
            buffer.flip();
            buffer.get(bytes);
            return bytes;
        }
    }
}
