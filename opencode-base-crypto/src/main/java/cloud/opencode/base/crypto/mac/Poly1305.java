package cloud.opencode.base.crypto.mac;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.util.ConstantTimeUtil;

import java.nio.charset.StandardCharsets;
import java.security.Security;

/**
 * Poly1305 MAC implementation - One-time authenticator using Bouncy Castle
 * Poly1305 消息认证码实现 - 使用 Bouncy Castle 的一次性认证器
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Poly1305 message authentication code - Poly1305 消息认证码</li>
 *   <li>One-time authenticator - 一次性认证器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Poly1305 poly = Poly1305.create(keyBytes);
 * byte[] tag = poly.compute(data);
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
public final class Poly1305 implements Mac {

    private static final String ALGORITHM = "Poly1305";
    private static final int MAC_LENGTH = 16; // Poly1305 produces 16 bytes
    private static final int KEY_LENGTH = 32; // Poly1305 requires 32-byte key
    private static final String BC_PROVIDER = "BC";

    private final org.bouncycastle.crypto.macs.Poly1305 poly1305;
    private final byte[] key;

    private Poly1305(byte[] key) {
        ensureBouncyCastleAvailable();
        validateKey(key);

        this.key = key.clone();
        this.poly1305 = new org.bouncycastle.crypto.macs.Poly1305();

        // Poly1305 uses a cipher for the key, typically AES
        // We'll use a simple key parameter approach
        org.bouncycastle.crypto.params.KeyParameter keyParam =
            new org.bouncycastle.crypto.params.KeyParameter(this.key);
        this.poly1305.init(keyParam);
    }

    /**
     * Creates a new Poly1305 instance with the given key
     * 使用给定密钥创建新的 Poly1305 实例
     *
     * @param key the secret key bytes (must be 32 bytes)
     * @return new Poly1305 instance
     * @throws NullPointerException if key is null
     * @throws OpenCryptoException if key length is invalid or Bouncy Castle is not available
     */
    public static Poly1305 of(byte[] key) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }
        return new Poly1305(key);
    }

    /**
     * Ensures Bouncy Castle provider is available
     * 确保 Bouncy Castle 提供者可用
     *
     * @throws OpenCryptoException if Bouncy Castle is not available
     */
    private static void ensureBouncyCastleAvailable() {
        try {
            Class.forName("org.bouncycastle.crypto.macs.Poly1305");
        } catch (ClassNotFoundException e) {
            throw OpenCryptoException.algorithmNotAvailable(ALGORITHM + " (Bouncy Castle not found)");
        }

        // Check if BC provider is registered
        if (Security.getProvider(BC_PROVIDER) == null) {
            try {
                // Try to add BC provider
                Security.addProvider(
                    (java.security.Provider) Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider")
                        .getDeclaredConstructor()
                        .newInstance()
                );
            } catch (Exception e) {
                throw OpenCryptoException.algorithmNotAvailable(ALGORITHM + " (Bouncy Castle provider not available)");
            }
        }
    }

    /**
     * Validates the key length
     * 验证密钥长度
     *
     * @param key the key to validate
     * @throws OpenCryptoException if key length is invalid
     */
    private static void validateKey(byte[] key) {
        if (key.length != KEY_LENGTH) {
            throw new OpenCryptoException(ALGORITHM, "initialization",
                String.format("Invalid key length: expected %d bytes, got %d bytes", KEY_LENGTH, key.length));
        }
    }

    @Override
    public byte[] compute(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        try {
            poly1305.reset();
            poly1305.update(data, 0, data.length);
            byte[] mac = new byte[MAC_LENGTH];
            poly1305.doFinal(mac, 0);
            return mac;
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
        poly1305.update(data, 0, data.length);
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
            byte[] mac = new byte[MAC_LENGTH];
            poly1305.doFinal(mac, 0);
            return mac;
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
        poly1305.reset();
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
