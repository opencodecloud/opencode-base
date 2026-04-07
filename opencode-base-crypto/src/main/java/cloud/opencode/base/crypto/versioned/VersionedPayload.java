package cloud.opencode.base.crypto.versioned;

import cloud.opencode.base.crypto.exception.OpenCryptoException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable record representing a versioned cipher payload with serialization support.
 * 表示带版本的加密负载的不可变记录，支持序列化。
 *
 * <p>Serialization format: {@code [1 byte version] [1 byte algorithm name length] [algorithm name UTF-8] [ciphertext]}</p>
 * <p>序列化格式: {@code [1字节版本] [1字节算法名长度] [算法名UTF8] [密文]}</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Compact binary serialization - 紧凑的二进制序列化</li>
 *   <li>Version range 0-255 - 版本号范围 0-255</li>
 *   <li>Defensive copies of ciphertext array - 密文数组的防御性拷贝</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * VersionedPayload payload = new VersionedPayload(1, "AES-256-GCM", ciphertext);
 * byte[] serialized = payload.serialize();
 * VersionedPayload restored = VersionedPayload.deserialize(serialized);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @param version the cipher version (0-255) | 加密版本号（0-255）
 * @param algorithm the algorithm name | 算法名称
 * @param ciphertext the encrypted data | 加密数据
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.3
 */
public record VersionedPayload(int version, String algorithm, byte[] ciphertext) {

    /**
     * Compact constructor with parameter validation.
     * 紧凑构造器，含参数校验。
     */
    public VersionedPayload {
        if (version < 0 || version > 255) {
            throw new IllegalArgumentException(
                    "Version must be in range [0, 255], got: " + version);
        }
        Objects.requireNonNull(algorithm, "algorithm must not be null");
        if (algorithm.isEmpty()) {
            throw new IllegalArgumentException("algorithm must not be empty");
        }
        byte[] algBytes = algorithm.getBytes(StandardCharsets.UTF_8);
        if (algBytes.length > 255) {
            throw new IllegalArgumentException(
                    "Algorithm name UTF-8 encoding must not exceed 255 bytes, got: " + algBytes.length);
        }
        Objects.requireNonNull(ciphertext, "ciphertext must not be null");
        ciphertext = ciphertext.clone(); // defensive copy
    }

    /**
     * Returns a defensive copy of the ciphertext.
     * 返回密文的防御性拷贝。
     *
     * @return copy of ciphertext bytes | 密文字节的拷贝
     */
    @Override
    public byte[] ciphertext() {
        return ciphertext.clone();
    }

    /**
     * Serializes this payload to a byte array.
     * 将此负载序列化为字节数组。
     *
     * <p>Format: {@code [1 byte version] [1 byte alg-name length] [alg-name UTF-8] [ciphertext]}</p>
     *
     * @return serialized byte array | 序列化的字节数组
     */
    public byte[] serialize() {
        byte[] algBytes = algorithm.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[1 + 1 + algBytes.length + ciphertext.length];
        result[0] = (byte) version;
        result[1] = (byte) algBytes.length;
        System.arraycopy(algBytes, 0, result, 2, algBytes.length);
        System.arraycopy(ciphertext, 0, result, 2 + algBytes.length, ciphertext.length);
        return result;
    }

    /**
     * Deserializes a byte array into a VersionedPayload.
     * 将字节数组反序列化为 VersionedPayload。
     *
     * @param data the serialized data | 序列化数据
     * @return deserialized VersionedPayload | 反序列化的 VersionedPayload
     * @throws OpenCryptoException if the data is malformed | 当数据格式错误时抛出
     */
    public static VersionedPayload deserialize(byte[] data) {
        Objects.requireNonNull(data, "data must not be null");
        if (data.length < 2) {
            throw new OpenCryptoException("VersionedPayload", "deserialize",
                    "Data too short: minimum 2 bytes required, got " + data.length);
        }
        int version = data[0] & 0xFF;
        int algLen = data[1] & 0xFF;
        if (algLen == 0) {
            throw new OpenCryptoException("VersionedPayload", "deserialize",
                    "Algorithm name length is 0");
        }
        if (data.length < 2 + algLen) {
            throw new OpenCryptoException("VersionedPayload", "deserialize",
                    "Data too short: expected at least " + (2 + algLen) + " bytes, got " + data.length);
        }
        String algorithm = new String(data, 2, algLen, StandardCharsets.UTF_8);
        int ciphertextLen = data.length - 2 - algLen;
        byte[] ciphertext = new byte[ciphertextLen];
        if (ciphertextLen > 0) {
            System.arraycopy(data, 2 + algLen, ciphertext, 0, ciphertextLen);
        }
        return new VersionedPayload(version, algorithm, ciphertext);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VersionedPayload that)) return false;
        return version == that.version
                && algorithm.equals(that.algorithm)
                && Arrays.equals(ciphertext, that.ciphertext);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(version, algorithm);
        result = 31 * result + Arrays.hashCode(ciphertext);
        return result;
    }

    @Override
    public String toString() {
        return "VersionedPayload[version=" + version + ", algorithm=" + algorithm
                + ", ciphertextLength=" + ciphertext.length + "]";
    }
}
