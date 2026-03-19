package cloud.opencode.base.crypto.mac;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.util.ConstantTimeUtil;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * HMAC-SHA512 implementation - Hash-based Message Authentication Code using SHA-512
 * HMAC-SHA512 实现 - 基于 SHA-512 的哈希消息认证码
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>HMAC-SHA512 message authentication - HMAC-SHA512 消息认证</li>
 *   <li>Constant-time verification - 常量时间验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Mac mac = HmacSha512.of(keyBytes);
 * byte[] tag = mac.compute(data);
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
public final class HmacSha512 implements Mac {

    private static final String ALGORITHM = "HmacSHA512";
    private static final int MAC_LENGTH = 64; // SHA-512 produces 64 bytes

    private final javax.crypto.Mac mac;

    private HmacSha512(SecretKey key) {
        try {
            this.mac = javax.crypto.Mac.getInstance(ALGORITHM);
            this.mac.init(key);
        } catch (NoSuchAlgorithmException e) {
            throw OpenCryptoException.algorithmNotAvailable(ALGORITHM);
        } catch (InvalidKeyException e) {
            throw new OpenCryptoException(ALGORITHM, "initialization", "Invalid key", e);
        }
    }

    /**
     * Creates a new HMAC-SHA512 instance with the given key
     * 使用给定密钥创建新的 HMAC-SHA512 实例
     *
     * @param key the secret key bytes
     * @return new HmacSha512 instance
     * @throws NullPointerException if key is null
     */
    public static HmacSha512 of(byte[] key) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }
        return new HmacSha512(new SecretKeySpec(key, ALGORITHM));
    }

    /**
     * Creates a new HMAC-SHA512 instance with the given SecretKey
     * 使用给定的 SecretKey 创建新的 HMAC-SHA512 实例
     *
     * @param key the secret key
     * @return new HmacSha512 instance
     * @throws NullPointerException if key is null
     */
    public static HmacSha512 of(SecretKey key) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }
        return new HmacSha512(key);
    }

    @Override
    public byte[] compute(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        try {
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new OpenCryptoException(ALGORITHM, "compute", "Failed to compute MAC", e);
        }
    }

    @Override
    public byte[] compute(String data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return compute(data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String computeHex(byte[] data) {
        return HexCodec.encode(compute(data));
    }

    @Override
    public String computeBase64(byte[] data) {
        return OpenBase64.encode(compute(data));
    }

    @Override
    public boolean verify(byte[] data, byte[] macValue) {
        if (data == null || macValue == null) {
            throw new NullPointerException("Data and MAC cannot be null");
        }
        byte[] computed = compute(data);
        return ConstantTimeUtil.equals(computed, macValue);
    }

    @Override
    public boolean verifyHex(byte[] data, String macHex) {
        if (macHex == null) {
            throw new NullPointerException("MAC hex string cannot be null");
        }
        try {
            byte[] macBytes = HexCodec.decode(macHex);
            return verify(data, macBytes);
        } catch (IllegalArgumentException e) {
            return false; // Invalid hex format
        }
    }

    @Override
    public boolean verifyBase64(byte[] data, String macBase64) {
        if (macBase64 == null) {
            throw new NullPointerException("MAC Base64 string cannot be null");
        }
        try {
            byte[] macBytes = OpenBase64.decode(macBase64);
            return verify(data, macBytes);
        } catch (IllegalArgumentException e) {
            return false; // Invalid Base64 format
        }
    }

    @Override
    public Mac update(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        mac.update(data);
        return this;
    }

    @Override
    public Mac update(String data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return update(data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public byte[] doFinal() {
        try {
            return mac.doFinal();
        } catch (Exception e) {
            throw new OpenCryptoException(ALGORITHM, "doFinal", "Failed to finalize MAC", e);
        }
    }

    @Override
    public String doFinalHex() {
        return HexCodec.encode(doFinal());
    }

    @Override
    public String doFinalBase64() {
        return OpenBase64.encode(doFinal());
    }

    @Override
    public Mac reset() {
        mac.reset();
        return this;
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }

    @Override
    public int getMacLength() {
        return MAC_LENGTH;
    }
}
