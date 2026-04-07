package cloud.opencode.base.hash.function;

import cloud.opencode.base.hash.HashCode;
import cloud.opencode.base.hash.Hasher;
import cloud.opencode.base.hash.exception.OpenHashException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * HMAC hash function implementation
 * HMAC 哈希函数实现
 *
 * <p>Wraps {@link javax.crypto.Mac} to provide HMAC-based hash functions
 * supporting MD5, SHA-1, SHA-256, SHA-384, and SHA-512 algorithms.</p>
 * <p>封装 {@link javax.crypto.Mac}，提供基于 HMAC 的哈希函数，
 * 支持 MD5、SHA-1、SHA-256、SHA-384 和 SHA-512 算法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>HMAC-MD5, HMAC-SHA1, HMAC-SHA256, HMAC-SHA384, HMAC-SHA512 - 支持多种HMAC算法</li>
 *   <li>Key cloned internally, never exposed - 密钥内部克隆，不对外暴露</li>
 *   <li>Each newHasher() creates a new Mac instance - 每次newHasher()创建新的Mac实例</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * byte[] key = "secret".getBytes(StandardCharsets.UTF_8);
 * HashFunction hmac = HmacHashFunction.hmacSha256(key);
 * HashCode hash = hmac.hashUtf8("Hello World");
 * String hex = hash.toHex();
 *
 * // Streaming via Hasher
 * Hasher hasher = hmac.newHasher();
 * hasher.putUtf8("Hello").putInt(42);
 * HashCode streamHash = hasher.hash();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Key never exposed via toString() - 密钥不会通过toString()暴露</li>
 *   <li>Suitable for message authentication - 适用于消息认证</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.3
 */
public final class HmacHashFunction extends AbstractHashFunction {

    private final String algorithm;
    private final byte[] key;

    private HmacHashFunction(String algorithm, int bits, byte[] key) {
        super(bits, algorithm);
        this.algorithm = algorithm;
        this.key = key.clone();
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates an HMAC-MD5 hash function
     * 创建 HMAC-MD5 哈希函数
     *
     * @param key secret key bytes | 密钥字节
     * @return HMAC-MD5 hash function | HMAC-MD5 哈希函数
     * @throws OpenHashException if key is null or empty | 如果密钥为null或为空
     */
    public static HmacHashFunction hmacMd5(byte[] key) {
        validateKey(key);
        return new HmacHashFunction("HmacMD5", 128, key);
    }

    /**
     * Creates an HMAC-SHA1 hash function
     * 创建 HMAC-SHA1 哈希函数
     *
     * @param key secret key bytes | 密钥字节
     * @return HMAC-SHA1 hash function | HMAC-SHA1 哈希函数
     * @throws OpenHashException if key is null or empty | 如果密钥为null或为空
     */
    public static HmacHashFunction hmacSha1(byte[] key) {
        validateKey(key);
        return new HmacHashFunction("HmacSHA1", 160, key);
    }

    /**
     * Creates an HMAC-SHA256 hash function
     * 创建 HMAC-SHA256 哈希函数
     *
     * @param key secret key bytes | 密钥字节
     * @return HMAC-SHA256 hash function | HMAC-SHA256 哈希函数
     * @throws OpenHashException if key is null or empty | 如果密钥为null或为空
     */
    public static HmacHashFunction hmacSha256(byte[] key) {
        validateKey(key);
        return new HmacHashFunction("HmacSHA256", 256, key);
    }

    /**
     * Creates an HMAC-SHA384 hash function
     * 创建 HMAC-SHA384 哈希函数
     *
     * @param key secret key bytes | 密钥字节
     * @return HMAC-SHA384 hash function | HMAC-SHA384 哈希函数
     * @throws OpenHashException if key is null or empty | 如果密钥为null或为空
     */
    public static HmacHashFunction hmacSha384(byte[] key) {
        validateKey(key);
        return new HmacHashFunction("HmacSHA384", 384, key);
    }

    /**
     * Creates an HMAC-SHA512 hash function
     * 创建 HMAC-SHA512 哈希函数
     *
     * @param key secret key bytes | 密钥字节
     * @return HMAC-SHA512 hash function | HMAC-SHA512 哈希函数
     * @throws OpenHashException if key is null or empty | 如果密钥为null或为空
     */
    public static HmacHashFunction hmacSha512(byte[] key) {
        validateKey(key);
        return new HmacHashFunction("HmacSHA512", 512, key);
    }

    // ==================== HashFunction Implementation | HashFunction实现 ====================

    @Override
    public Hasher newHasher() {
        return new HmacHasher(createMac());
    }

    @Override
    public HashCode hashBytes(byte[] input, int offset, int length) {
        java.util.Objects.requireNonNull(input, "input");
        if (offset < 0 || length < 0 || length > input.length - offset) {
            throw OpenHashException.invalidInput(
                    "offset=" + offset + ", length=" + length + ", array.length=" + input.length);
        }
        Mac mac = createMac();
        mac.update(input, offset, length);
        return HashCode.fromBytes(mac.doFinal());
    }

    @Override
    public String toString() {
        return name + "[" + bits + "]";
    }

    // ==================== Internal Methods | 内部方法 ====================

    /**
     * Creates and initializes a new Mac instance
     * 创建并初始化新的Mac实例
     */
    private Mac createMac() {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key, algorithm));
            return mac;
        } catch (NoSuchAlgorithmException e) {
            throw OpenHashException.algorithmNotSupported(algorithm);
        } catch (InvalidKeyException e) {
            throw OpenHashException.hmacFailed(algorithm, e);
        }
    }

    /**
     * Validates the key
     * 验证密钥
     */
    private static void validateKey(byte[] key) {
        if (key == null || key.length == 0) {
            throw OpenHashException.invalidInput("HMAC key must not be null or empty");
        }
    }

    // ==================== Hasher Implementation | Hasher实现 ====================

    /**
     * HMAC-based Hasher implementation
     * 基于HMAC的Hasher实现
     */
    private static final class HmacHasher extends AbstractHasher {

        private final Mac mac;

        HmacHasher(Mac mac) {
            this.mac = mac;
        }

        @Override
        public Hasher putByte(byte b) {
            checkNotUsed();
            mac.update(b);
            return this;
        }

        @Override
        public Hasher putBytes(byte[] bytes, int offset, int length) {
            checkNotUsed();
            mac.update(bytes, offset, length);
            return this;
        }

        @Override
        protected HashCode doHash() {
            return HashCode.fromBytes(mac.doFinal());
        }
    }
}
