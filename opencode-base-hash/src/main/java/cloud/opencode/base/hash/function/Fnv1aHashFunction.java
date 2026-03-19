package cloud.opencode.base.hash.function;

import cloud.opencode.base.hash.HashCode;
import cloud.opencode.base.hash.Hasher;

/**
 * FNV-1a hash function implementation
 * FNV-1a 哈希函数实现
 *
 * <p>Fowler-Noll-Vo (FNV) hash is a non-cryptographic hash function
 * with good distribution characteristics. FNV-1a is a variant that
 * XORs the input byte before multiplying.</p>
 * <p>Fowler-Noll-Vo (FNV) 哈希是一种具有良好分布特性的非加密哈希函数。
 * FNV-1a 是一种在乘法之前对输入字节进行 XOR 的变体。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>32-bit and 64-bit variants - 32位和64位变体</li>
 *   <li>Simple and fast implementation - 简单快速的实现</li>
 *   <li>Good distribution - 良好的分布</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HashCode hash32 = Fnv1aHashFunction.fnv1a_32().hashUtf8("Hello");
 * HashCode hash64 = Fnv1aHashFunction.fnv1a_64().hashBytes(data);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Not for cryptographic use - 不用于加密用途</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = input size - O(n), n为输入大小</li>
 *   <li>Space complexity: O(1) - O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public final class Fnv1aHashFunction extends AbstractHashFunction {

    // FNV-1a 32-bit parameters
    private static final int FNV_32_PRIME = 0x01000193;
    private static final int FNV_32_OFFSET_BASIS = 0x811c9dc5;

    // FNV-1a 64-bit parameters
    private static final long FNV_64_PRIME = 0x00000100000001B3L;
    private static final long FNV_64_OFFSET_BASIS = 0xcbf29ce484222325L;

    private final boolean is64Bit;

    private Fnv1aHashFunction(int bits) {
        super(bits, "fnv1a_" + bits);
        this.is64Bit = (bits == 64);
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a 32-bit FNV-1a function
     * 创建32位FNV-1a函数
     *
     * @return hash function | 哈希函数
     */
    public static Fnv1aHashFunction fnv1a_32() {
        return new Fnv1aHashFunction(32);
    }

    /**
     * Creates a 64-bit FNV-1a function
     * 创建64位FNV-1a函数
     *
     * @return hash function | 哈希函数
     */
    public static Fnv1aHashFunction fnv1a_64() {
        return new Fnv1aHashFunction(64);
    }

    @Override
    public Hasher newHasher() {
        return is64Bit ? new Fnv1a64Hasher() : new Fnv1a32Hasher();
    }

    @Override
    public HashCode hashBytes(byte[] input, int offset, int length) {
        if (is64Bit) {
            return HashCode.fromLong(hash64(input, offset, length));
        } else {
            return HashCode.fromInt(hash32(input, offset, length));
        }
    }

    // ==================== 32-bit Implementation | 32位实现 ====================

    private static int hash32(byte[] data, int offset, int length) {
        int hash = FNV_32_OFFSET_BASIS;
        for (int i = 0; i < length; i++) {
            hash ^= (data[offset + i] & 0xff);
            hash *= FNV_32_PRIME;
        }
        return hash;
    }

    // ==================== 64-bit Implementation | 64位实现 ====================

    private static long hash64(byte[] data, int offset, int length) {
        long hash = FNV_64_OFFSET_BASIS;
        for (int i = 0; i < length; i++) {
            hash ^= (data[offset + i] & 0xffL);
            hash *= FNV_64_PRIME;
        }
        return hash;
    }

    // ==================== Hasher Implementations | Hasher实现 ====================

    private static class Fnv1a32Hasher extends AbstractHasher {
        private int hash = FNV_32_OFFSET_BASIS;

        @Override
        public Hasher putByte(byte b) {
            hash ^= (b & 0xff);
            hash *= FNV_32_PRIME;
            return this;
        }

        @Override
        public Hasher putBytes(byte[] bytes, int offset, int length) {
            for (int i = 0; i < length; i++) {
                hash ^= (bytes[offset + i] & 0xff);
                hash *= FNV_32_PRIME;
            }
            return this;
        }

        @Override
        protected HashCode doHash() {
            return HashCode.fromInt(hash);
        }
    }

    private static class Fnv1a64Hasher extends AbstractHasher {
        private long hash = FNV_64_OFFSET_BASIS;

        @Override
        public Hasher putByte(byte b) {
            hash ^= (b & 0xffL);
            hash *= FNV_64_PRIME;
            return this;
        }

        @Override
        public Hasher putBytes(byte[] bytes, int offset, int length) {
            for (int i = 0; i < length; i++) {
                hash ^= (bytes[offset + i] & 0xffL);
                hash *= FNV_64_PRIME;
            }
            return this;
        }

        @Override
        protected HashCode doHash() {
            return HashCode.fromLong(hash);
        }
    }
}
