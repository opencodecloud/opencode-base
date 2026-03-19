package cloud.opencode.base.crypto.kdf;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.util.CryptoUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * HMAC-based Key Derivation Function (HKDF) implementation - RFC 5869 compliant KDF using HMAC
 * 基于 HMAC 的密钥派生函数实现 - 符合 RFC 5869 标准的 KDF，使用 HMAC
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>HKDF-SHA256 and HKDF-SHA512 - HKDF-SHA256 和 HKDF-SHA512</li>
 *   <li>Extract-then-expand key derivation - 提取-扩展密钥派生</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Hkdf hkdf = Hkdf.sha256();
 * byte[] key = hkdf.deriveKey(ikm, salt, info, 32);
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
public final class Hkdf implements KdfEngine {

    private static final String DEFAULT_ALGORITHM = "HmacSHA256";
    private static final int SHA256_LENGTH = 32;
    private static final int SHA384_LENGTH = 48;
    private static final int SHA512_LENGTH = 64;

    private final String algorithm;
    private final int hashLength;

    private Hkdf(String algorithm) {
        this.algorithm = algorithm;
        this.hashLength = getHashLength(algorithm);
    }

    /**
     * Creates an HKDF instance with SHA-256
     * 使用 SHA-256 创建 HKDF 实例
     *
     * @return new HKDF instance using HmacSHA256
     */
    public static Hkdf sha256() {
        return new Hkdf("HmacSHA256");
    }

    /**
     * Creates an HKDF instance with SHA-384
     * 使用 SHA-384 创建 HKDF 实例
     *
     * @return new HKDF instance using HmacSHA384
     */
    public static Hkdf sha384() {
        return new Hkdf("HmacSHA384");
    }

    /**
     * Creates an HKDF instance with SHA-512
     * 使用 SHA-512 创建 HKDF 实例
     *
     * @return new HKDF instance using HmacSHA512
     */
    public static Hkdf sha512() {
        return new Hkdf("HmacSHA512");
    }

    /**
     * Derives key material from input keying material (IKM)
     * 从输入密钥材料派生密钥
     *
     * @param ikm input keying material
     * @param info optional context and application specific information (can be null)
     * @param length desired output length in bytes (max 255 * hashLength)
     * @return derived key material
     * @throws NullPointerException if ikm is null
     * @throws IllegalArgumentException if length is invalid
     */
    public byte[] deriveKey(byte[] ikm, byte[] info, int length) {
        return deriveKey(ikm, null, info, length);
    }

    /**
     * Derives key material from input keying material (IKM) with optional salt
     * 从输入密钥材料和可选盐值派生密钥
     *
     * @param ikm input keying material
     * @param salt optional salt value (can be null, defaults to hashLength zeros)
     * @param info optional context and application specific information (can be null)
     * @param length desired output length in bytes (max 255 * hashLength)
     * @return derived key material
     * @throws NullPointerException if ikm is null
     * @throws IllegalArgumentException if length is invalid
     */
    public byte[] deriveKey(byte[] ikm, byte[] salt, byte[] info, int length) {
        if (ikm == null) {
            throw new NullPointerException("Input keying material cannot be null");
        }
        if (length <= 0 || length > 255 * hashLength) {
            throw new IllegalArgumentException("Invalid output length: " + length
                + " (must be between 1 and " + (255 * hashLength) + ")");
        }

        // Extract: PRK = HMAC-Hash(salt, IKM)
        byte[] prk = extract(salt, ikm);

        // Expand: OKM = HKDF-Expand(PRK, info, L)
        byte[] okm = expand(prk, info, length);

        // Clear sensitive data
        CryptoUtil.secureErase(prk);

        return okm;
    }

    /**
     * HKDF-Extract: Extracts a pseudorandom key from input key material
     * HKDF-提取：从输入密钥材料中提取伪随机密钥
     *
     * @param salt the salt value (if null or empty, a string of zeros is used)
     * @param ikm  the input key material
     * @return the pseudorandom key (PRK)
     * @throws NullPointerException if ikm is null
     * @throws OpenCryptoException  if extraction fails
     */
    public byte[] extract(byte[] salt, byte[] ikm) {
        Objects.requireNonNull(ikm, "Input key material cannot be null");
        try {
            // If salt is not provided, use a string of HashLen zeros
            byte[] actualSalt = (salt == null || salt.length == 0)
                ? new byte[hashLength]
                : salt;

            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(actualSalt, algorithm));
            return mac.doFinal(ikm);
        } catch (NoSuchAlgorithmException e) {
            throw OpenCryptoException.algorithmNotAvailable(algorithm);
        } catch (InvalidKeyException e) {
            throw new OpenCryptoException(algorithm, "extract", "Invalid salt", e);
        }
    }

    /**
     * HKDF-Expand: Expands a pseudorandom key to desired length
     * HKDF-扩展：将伪随机密钥扩展到所需长度
     *
     * @param prk    the pseudorandom key from extract phase
     * @param info   the context and application specific information (can be null)
     * @param length the desired output key length in bytes
     * @return the output key material (OKM)
     * @throws NullPointerException     if prk is null
     * @throws IllegalArgumentException if length is invalid
     * @throws OpenCryptoException      if expansion fails
     */
    public byte[] expand(byte[] prk, byte[] info, int length) {
        Objects.requireNonNull(prk, "Pseudorandom key cannot be null");

        if (length <= 0) {
            throw new IllegalArgumentException("Output length must be positive");
        }

        // RFC 5869: output length must not exceed 255 * hashLength
        if (length > 255 * hashLength) {
            throw new IllegalArgumentException(
                    String.format("Requested length %d exceeds maximum %d bytes for %s",
                            length, 255 * hashLength, algorithm));
        }
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(prk, algorithm));

            int iterations = (int) Math.ceil((double) length / hashLength);
            byte[] okm = new byte[length];
            byte[] t = new byte[0];
            int pos = 0;

            for (int i = 1; i <= iterations; i++) {
                mac.reset();
                mac.update(t);
                if (info != null && info.length > 0) {
                    mac.update(info);
                }
                mac.update((byte) i);
                t = mac.doFinal();

                int bytesToCopy = Math.min(hashLength, length - pos);
                System.arraycopy(t, 0, okm, pos, bytesToCopy);
                pos += bytesToCopy;
            }

            // Securely erase temporary key material
            CryptoUtil.secureErase(t);

            return okm;
        } catch (NoSuchAlgorithmException e) {
            throw OpenCryptoException.algorithmNotAvailable(algorithm);
        } catch (InvalidKeyException e) {
            throw new OpenCryptoException(algorithm, "expand", "Invalid key for HKDF-Expand", e);
        }
    }

    /**
     * Gets the hash length for the given HMAC algorithm
     * 获取给定 HMAC 算法的哈希长度
     *
     * @param algorithm the HMAC algorithm name
     * @return hash length in bytes
     */
    private static int getHashLength(String algorithm) {
        return switch (algorithm) {
            case "HmacSHA256" -> SHA256_LENGTH;
            case "HmacSHA384" -> SHA384_LENGTH;
            case "HmacSHA512" -> SHA512_LENGTH;
            default -> throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        };
    }

    /**
     * Gets the algorithm name
     * 获取算法名称
     *
     * @return the HMAC algorithm name
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Gets the hash output length
     * 获取哈希输出长度
     *
     * @return hash length in bytes
     */
    public int getHashLength() {
        return hashLength;
    }

    /**
     * HKDF-Extract-and-Expand: Combines extract and expand in one call
     * HKDF-提取并扩展：在一次调用中结合提取和扩展
     *
     * @param salt   the salt value (can be null)
     * @param ikm    the input key material
     * @param info   the context and application specific information (can be null)
     * @param length the desired output key length in bytes
     * @return the output key material (OKM)
     * @throws NullPointerException     if ikm is null
     * @throws IllegalArgumentException if length is invalid
     * @throws OpenCryptoException      if derivation fails
     */
    public byte[] extractAndExpand(byte[] salt, byte[] ikm, byte[] info, int length) {
        return deriveKey(ikm, salt, info, length);
    }

    /**
     * Derives multiple keys from the same input key material with different info contexts
     * 使用不同的信息上下文从相同的输入密钥材料派生多个密钥
     *
     * @param salt    the salt value (can be null)
     * @param ikm     the input key material
     * @param infos   array of context information for each derived key
     * @param lengths array of desired lengths for each derived key
     * @return array of derived keys
     * @throws NullPointerException     if ikm, infos, or lengths is null
     * @throws IllegalArgumentException if infos and lengths arrays have different lengths
     * @throws OpenCryptoException      if derivation fails
     */
    public byte[][] deriveKeys(byte[] salt, byte[] ikm, byte[][] infos, int[] lengths) {
        Objects.requireNonNull(ikm, "Input key material cannot be null");
        Objects.requireNonNull(infos, "Info array cannot be null");
        Objects.requireNonNull(lengths, "Lengths array cannot be null");

        if (infos.length != lengths.length) {
            throw new IllegalArgumentException("Info and lengths arrays must have the same length");
        }

        byte[] prk = extract(salt, ikm);
        try {
            byte[][] derivedKeys = new byte[infos.length][];
            for (int i = 0; i < infos.length; i++) {
                derivedKeys[i] = expand(prk, infos[i], lengths[i]);
            }
            return derivedKeys;
        } finally {
            // Securely erase the PRK after use
            CryptoUtil.secureErase(prk);
        }
    }

    @Override
    public byte[] derive(byte[] inputKeyMaterial, byte[] salt, byte[] info, int length) {
        return deriveKey(inputKeyMaterial, salt, info, length);
    }

    @Override
    public byte[] derive(byte[] inputKeyMaterial, int length) {
        return deriveKey(inputKeyMaterial, null, null, length);
    }
}
