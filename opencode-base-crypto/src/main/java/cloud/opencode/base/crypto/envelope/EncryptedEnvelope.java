package cloud.opencode.base.crypto.envelope;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.exception.OpenCryptoException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Encrypted envelope data structure - Contains encrypted key, IV, ciphertext and authentication tag
 * 加密信封数据结构 - 包含加密密钥、初始化向量、密文和认证标签
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Encrypted data container with metadata - 带元数据的加密数据容器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EncryptedEnvelope envelope = new EncryptedEnvelope(wrappedKey, ciphertext, iv);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class EncryptedEnvelope {

    private final byte[] encryptedKey;
    private final byte[] iv;
    private final byte[] ciphertext;
    private final byte[] tag;

    /**
     * Constructs an encrypted envelope with the specified components.
     * 使用指定组件构造加密信封
     *
     * @param encryptedKey the encrypted data encryption key
     * @param iv the initialization vector
     * @param ciphertext the encrypted data
     * @param tag the authentication tag (can be null for non-AEAD algorithms)
     * @throws NullPointerException if encryptedKey, iv, or ciphertext is null
     */
    public EncryptedEnvelope(byte[] encryptedKey, byte[] iv, byte[] ciphertext, byte[] tag) {
        Objects.requireNonNull(encryptedKey, "Encrypted key cannot be null");
        Objects.requireNonNull(iv, "IV cannot be null");
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null");

        this.encryptedKey = encryptedKey.clone();
        this.iv = iv.clone();
        this.ciphertext = ciphertext.clone();
        this.tag = tag != null ? tag.clone() : null;
    }

    /**
     * Returns the encrypted data encryption key.
     * 返回加密的数据加密密钥
     *
     * @return copy of the encrypted key
     */
    public byte[] encryptedKey() {
        return encryptedKey.clone();
    }

    /**
     * Returns the initialization vector.
     * 返回初始化向量
     *
     * @return copy of the IV
     */
    public byte[] iv() {
        return iv.clone();
    }

    /**
     * Returns the ciphertext.
     * 返回密文
     *
     * @return copy of the ciphertext
     */
    public byte[] ciphertext() {
        return ciphertext.clone();
    }

    /**
     * Returns the authentication tag.
     * 返回认证标签
     *
     * @return copy of the tag, or null if not present
     */
    public byte[] tag() {
        return tag != null ? tag.clone() : null;
    }

    /**
     * Serializes this envelope to a Base64 encoded string.
     * 将此信封序列化为 Base64 编码字符串
     * <p>
     * Format: Base64(bytes format)
     *
     * @return Base64 encoded envelope
     */
    public String toBase64() {
        return OpenBase64.encode(toBytes());
    }

    /**
     * Deserializes an encrypted envelope from a Base64 encoded string.
     * 从 Base64 编码字符串反序列化加密信封
     *
     * @param base64 the Base64 encoded envelope
     * @return the deserialized envelope
     * @throws NullPointerException if base64 is null
     * @throws OpenCryptoException if deserialization fails
     */
    public static EncryptedEnvelope fromBase64(String base64) {
        Objects.requireNonNull(base64, "Base64 string cannot be null");
        try {
            byte[] bytes = OpenBase64.decode(base64);
            return fromBytes(bytes);
        } catch (Exception e) {
            throw new OpenCryptoException("Failed to deserialize envelope from Base64", e);
        }
    }

    /**
     * Serializes this envelope to a byte array.
     * 将此信封序列化为字节数组
     * <p>
     * Format: [4-byte encryptedKey length][encryptedKey][4-byte iv length][iv]
     *         [4-byte ciphertext length][ciphertext][4-byte tag length][tag or empty]
     *
     * @return serialized envelope bytes
     */
    public byte[] toBytes() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            // Write encrypted key
            dos.writeInt(encryptedKey.length);
            dos.write(encryptedKey);

            // Write IV
            dos.writeInt(iv.length);
            dos.write(iv);

            // Write ciphertext
            dos.writeInt(ciphertext.length);
            dos.write(ciphertext);

            // Write tag (or 0 if null)
            if (tag != null) {
                dos.writeInt(tag.length);
                dos.write(tag);
            } else {
                dos.writeInt(0);
            }

            dos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new OpenCryptoException("Failed to serialize envelope", e);
        }
    }

    /**
     * Deserializes an encrypted envelope from a byte array.
     * 从字节数组反序列化加密信封
     *
     * @param bytes the serialized envelope bytes
     * @return the deserialized envelope
     * @throws NullPointerException if bytes is null
     * @throws OpenCryptoException if deserialization fails
     */
    public static EncryptedEnvelope fromBytes(byte[] bytes) {
        Objects.requireNonNull(bytes, "Bytes cannot be null");

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             DataInputStream dis = new DataInputStream(bais)) {

            // Read encrypted key
            int encryptedKeyLength = dis.readInt();
            if (encryptedKeyLength <= 0 || encryptedKeyLength > 65536) {
                throw new OpenCryptoException("Invalid encrypted key length: " + encryptedKeyLength);
            }
            byte[] encryptedKey = new byte[encryptedKeyLength];
            dis.readFully(encryptedKey);

            // Read IV
            int ivLength = dis.readInt();
            if (ivLength <= 0 || ivLength > 1024) {
                throw new OpenCryptoException("Invalid IV length: " + ivLength);
            }
            byte[] iv = new byte[ivLength];
            dis.readFully(iv);

            // Read ciphertext
            int ciphertextLength = dis.readInt();
            if (ciphertextLength < 0 || ciphertextLength > 100_000_000) {
                throw new OpenCryptoException("Invalid ciphertext length: " + ciphertextLength);
            }
            byte[] ciphertext = new byte[ciphertextLength];
            dis.readFully(ciphertext);

            // Read tag (may be 0 for non-AEAD algorithms)
            int tagLength = dis.readInt();
            byte[] tag = null;
            if (tagLength > 0) {
                if (tagLength > 1024) {
                    throw new OpenCryptoException("Invalid tag length: " + tagLength);
                }
                tag = new byte[tagLength];
                dis.readFully(tag);
            }

            return new EncryptedEnvelope(encryptedKey, iv, ciphertext, tag);
        } catch (IOException e) {
            throw new OpenCryptoException("Failed to deserialize envelope", e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        EncryptedEnvelope that = (EncryptedEnvelope) obj;
        return Arrays.equals(encryptedKey, that.encryptedKey) &&
               Arrays.equals(iv, that.iv) &&
               Arrays.equals(ciphertext, that.ciphertext) &&
               Arrays.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(encryptedKey);
        result = 31 * result + Arrays.hashCode(iv);
        result = 31 * result + Arrays.hashCode(ciphertext);
        result = 31 * result + Arrays.hashCode(tag);
        return result;
    }

    @Override
    public String toString() {
        return "EncryptedEnvelope{" +
               "encryptedKeyLength=" + encryptedKey.length +
               ", ivLength=" + iv.length +
               ", ciphertextLength=" + ciphertext.length +
               ", tagLength=" + (tag != null ? tag.length : 0) +
               '}';
    }
}
