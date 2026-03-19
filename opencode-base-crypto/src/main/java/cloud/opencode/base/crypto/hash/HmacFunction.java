package cloud.opencode.base.crypto.hash;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.exception.OpenCryptoException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * HMAC (Hash-based Message Authentication Code) function implementation - Keyed hash for message authentication
 * HMAC（基于哈希的消息认证码）函数实现 - 用于消息认证的密钥哈希
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>HMAC-SHA256, HMAC-SHA384, HMAC-SHA512 - HMAC-SHA256、HMAC-SHA384、HMAC-SHA512</li>
 *   <li>Constant-time verification - 常量时间验证</li>
 *   <li>Hex and Base64 output encoding - 十六进制和 Base64 输出编码</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HmacFunction hmac = HmacFunction.hmacSha256(keyBytes);
 * String hex = hmac.computeHex("message");
 * boolean valid = hmac.verify("message".getBytes(), expectedMac);
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
public final class HmacFunction {

    private final String algorithm;
    private final byte[] key;

    private HmacFunction(String algorithm, byte[] key) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }
        if (key.length == 0) {
            throw new IllegalArgumentException("Key cannot be empty");
        }
        this.algorithm = algorithm;
        this.key = key.clone(); // Defensive copy
    }

    /**
     * Create HMAC-SHA256 function
     * 创建 HMAC-SHA256 函数
     *
     * @param key secret key for HMAC
     * @return HMAC-SHA256 function instance
     * @throws NullPointerException if key is null
     * @throws IllegalArgumentException if key is empty
     */
    public static HmacFunction hmacSha256(byte[] key) {
        return of("HmacSHA256", key);
    }

    /**
     * Create HMAC-SHA384 function
     * 创建 HMAC-SHA384 函数
     *
     * @param key secret key for HMAC
     * @return HMAC-SHA384 function instance
     * @throws NullPointerException if key is null
     * @throws IllegalArgumentException if key is empty
     */
    public static HmacFunction hmacSha384(byte[] key) {
        return of("HmacSHA384", key);
    }

    /**
     * Create HMAC-SHA512 function
     * 创建 HMAC-SHA512 函数
     *
     * @param key secret key for HMAC
     * @return HMAC-SHA512 function instance
     * @throws NullPointerException if key is null
     * @throws IllegalArgumentException if key is empty
     */
    public static HmacFunction hmacSha512(byte[] key) {
        return of("HmacSHA512", key);
    }

    /**
     * Create HMAC function with custom algorithm
     * 创建指定算法的 HMAC 函数
     *
     * @param algorithm HMAC algorithm name (e.g., "HmacSHA256", "HmacSHA512")
     * @param key secret key for HMAC
     * @return HMAC function instance
     * @throws NullPointerException if algorithm or key is null
     * @throws IllegalArgumentException if key is empty
     */
    public static HmacFunction of(String algorithm, byte[] key) {
        if (algorithm == null) {
            throw new NullPointerException("Algorithm cannot be null");
        }
        return new HmacFunction(algorithm, key);
    }

    /**
     * Compute MAC (Message Authentication Code) of byte array
     * 计算字节数组的消息认证码
     *
     * @param data input data to authenticate
     * @return MAC as byte array
     * @throws NullPointerException if data is null
     * @throws OpenCryptoException if MAC computation fails
     */
    public byte[] mac(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        try {
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec keySpec = new SecretKeySpec(key, algorithm);
            mac.init(keySpec);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            throw OpenCryptoException.algorithmNotAvailable(algorithm);
        } catch (InvalidKeyException e) {
            throw new OpenCryptoException(algorithm, "mac", "Invalid key", e);
        }
    }

    /**
     * Compute MAC of string (UTF-8 encoded)
     * 计算字符串的消息认证码（使用 UTF-8 编码）
     *
     * @param data input string to authenticate
     * @return MAC as byte array
     * @throws NullPointerException if data is null
     * @throws OpenCryptoException if MAC computation fails
     */
    public byte[] mac(String data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return mac(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Compute MAC and return as hexadecimal string
     * 计算消息认证码并返回十六进制字符串
     *
     * @param data input data to authenticate
     * @return MAC as lowercase hex string
     * @throws NullPointerException if data is null
     * @throws OpenCryptoException if MAC computation fails
     */
    public String macHex(byte[] data) {
        return HexCodec.encode(mac(data));
    }

    /**
     * Compute MAC and return as Base64 string
     * 计算消息认证码并返回 Base64 字符串
     *
     * @param data input data to authenticate
     * @return MAC as Base64 string
     * @throws NullPointerException if data is null
     * @throws OpenCryptoException if MAC computation fails
     */
    public String macBase64(byte[] data) {
        return OpenBase64.encode(mac(data));
    }

    /**
     * Verify MAC for given data using constant-time comparison
     * 使用恒定时间比较验证给定数据的消息认证码
     *
     * @param data original data
     * @param mac MAC to verify
     * @return true if MAC is valid, false otherwise
     * @throws NullPointerException if data or mac is null
     * @throws OpenCryptoException if MAC computation fails
     */
    public boolean verify(byte[] data, byte[] mac) {
        if (mac == null) {
            throw new NullPointerException("MAC cannot be null");
        }
        byte[] computed = mac(data);
        return MessageDigest.isEqual(computed, mac);
    }

    /**
     * Get the HMAC algorithm name
     * 获取 HMAC 算法名称
     *
     * @return algorithm name
     */
    public String getAlgorithm() {
        return algorithm;
    }
}
