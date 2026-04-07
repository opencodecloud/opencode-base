package cloud.opencode.base.crypto.symmetric;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import cloud.opencode.base.crypto.util.CryptoUtil;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * AES cipher implementation supporting CBC and CTR modes.
 * AES 加密实现，支持 CBC 和 CTR 模式。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>AES-CBC and AES-CTR modes - AES-CBC 和 AES-CTR 模式</li>
 *   <li>128/192/256-bit key support - 128/192/256 位密钥支持</li>
 *   <li>Automatic IV generation and prepending - 自动 IV 生成和前置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AesCipher cipher = AesCipher.cbc();
 * cipher.setKey(secretKey);
 * byte[] encrypted = cipher.encrypt(plaintext);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Partial - 空值安全: 部分</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) - 时间复杂度: O(n)，n为明文长度</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class AesCipher implements SymmetricCipher {

    private static final System.Logger LOGGER = System.getLogger(AesCipher.class.getName());
    private static final String ALGORITHM = "AES";
    private static final int BLOCK_SIZE = 16;
    private static final int DEFAULT_KEY_SIZE = 256;

    private SecretKey key;
    private byte[] iv;
    private CipherMode mode = CipherMode.CBC;
    private Padding padding = Padding.PKCS7;
    private final int keySize;
    private final SecureRandom random = new SecureRandom();

    private AesCipher(int keySize) {
        this.keySize = keySize;
    }

    /**
     * Create AES-128 cipher.
     * 创建 AES-128 加密器。
     *
     * @return AES-128 cipher instance / AES-128 加密实例
     */
    public static AesCipher aes128() {
        return new AesCipher(128);
    }

    /**
     * Create AES-256 cipher (recommended).
     * 创建 AES-256 加密器（推荐）。
     *
     * @return AES-256 cipher instance / AES-256 加密实例
     */
    public static AesCipher aes256() {
        return new AesCipher(256);
    }

    /**
     * Create AES cipher in CBC mode.
     * 创建 CBC 模式的 AES 加密器。
     *
     * @return AES-CBC cipher instance / AES-CBC 加密实例
     */
    public static AesCipher cbc() {
        return new AesCipher(DEFAULT_KEY_SIZE).setMode(CipherMode.CBC);
    }

    /**
     * Create AES cipher in CTR mode.
     * 创建 CTR 模式的 AES 加密器。
     *
     * @return AES-CTR cipher instance / AES-CTR 加密实例
     */
    public static AesCipher ctr() {
        return new AesCipher(DEFAULT_KEY_SIZE).setMode(CipherMode.CTR);
    }

    /**
     * Create a builder for AES cipher.
     * 创建 AES 加密器构建器。
     *
     * @return builder instance / 构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public AesCipher setKey(SecretKey key) {
        this.key = key;
        return this;
    }

    @Override
    public AesCipher setKey(byte[] key) {
        AesKeyValidator.validateKeyBytes(key, "AES");
        this.key = new SecretKeySpec(key, ALGORITHM);
        return this;
    }

    @Override
    public AesCipher setIv(byte[] iv) {
        if (iv != null && iv.length != BLOCK_SIZE) {
            throw new OpenCryptoException("AES IV must be " + BLOCK_SIZE + " bytes");
        }
        this.iv = iv != null ? iv.clone() : null;
        return this;
    }

    @Override
    public AesCipher setMode(CipherMode mode) {
        if (mode == CipherMode.GCM || mode == CipherMode.CCM) {
            throw new OpenCryptoException("Use AesGcmCipher for AEAD modes");
        }
        this.mode = mode;
        return this;
    }

    @Override
    public AesCipher setPadding(Padding padding) {
        this.padding = padding;
        return this;
    }

    @Override
    public byte[] encrypt(byte[] plaintext) {
        try {
            if (key == null) {
                key = generateAesKey(keySize);
            }

            Cipher cipher = Cipher.getInstance(getTransformation());
            if (mode == CipherMode.ECB) {
                LOGGER.log(System.Logger.Level.WARNING, "AES-ECB mode is insecure and should not be used for production data. Use GCM or CBC instead.");
                cipher.init(Cipher.ENCRYPT_MODE, key);
                return cipher.doFinal(plaintext);
            } else {
                // Always generate a fresh IV for each encryption to prevent IV reuse
                byte[] encIv = generateIv();
                this.iv = encIv;
                cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(encIv));
                byte[] ciphertext = cipher.doFinal(plaintext);
                // Prepend IV to ciphertext so decrypt() can extract it
                byte[] result = new byte[encIv.length + ciphertext.length];
                System.arraycopy(encIv, 0, result, 0, encIv.length);
                System.arraycopy(ciphertext, 0, result, encIv.length, ciphertext.length);
                return result;
            }
        } catch (Exception e) {
            throw new OpenCryptoException("AES encryption failed", e);
        }
    }

    @Override
    public byte[] encrypt(String plaintext) {
        return encrypt(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String encryptBase64(byte[] plaintext) {
        return OpenBase64.encode(encrypt(plaintext));
    }

    @Override
    public String encryptHex(byte[] plaintext) {
        return HexCodec.encode(encrypt(plaintext));
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) {
        try {
            if (key == null) {
                throw new OpenKeyException("Key not set");
            }

            Cipher cipher = Cipher.getInstance(getTransformation());
            if (mode == CipherMode.ECB) {
                LOGGER.log(System.Logger.Level.WARNING, "AES-ECB mode is insecure and should not be used for production data. Use GCM or CBC instead.");
                cipher.init(Cipher.DECRYPT_MODE, key);
                return cipher.doFinal(ciphertext);
            } else {
                if (ciphertext == null || ciphertext.length < BLOCK_SIZE) {
                    throw new OpenCryptoException("Ciphertext too short - must contain prepended IV");
                }
                // Extract IV from the first 16 bytes of the ciphertext
                byte[] decIv = new byte[BLOCK_SIZE];
                System.arraycopy(ciphertext, 0, decIv, 0, BLOCK_SIZE);
                byte[] actualCiphertext = new byte[ciphertext.length - BLOCK_SIZE];
                System.arraycopy(ciphertext, BLOCK_SIZE, actualCiphertext, 0, actualCiphertext.length);

                cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(decIv));
                return cipher.doFinal(actualCiphertext);
            }
        } catch (OpenCryptoException | OpenKeyException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenCryptoException("AES decryption failed", e);
        }
    }

    @Override
    public String decryptToString(byte[] ciphertext) {
        return new String(decrypt(ciphertext), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] decryptBase64(String base64Ciphertext) {
        return decrypt(OpenBase64.decode(base64Ciphertext));
    }

    @Override
    public byte[] decryptHex(String hexCiphertext) {
        return decrypt(HexCodec.decode(hexCiphertext));
    }

    @Override
    public byte[] generateIv() {
        byte[] newIv = new byte[BLOCK_SIZE];
        random.nextBytes(newIv);
        return newIv;
    }

    @Override
    public int getBlockSize() {
        return BLOCK_SIZE;
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM + "-" + mode.name();
    }

    @Override
    public int getIvLength() {
        return BLOCK_SIZE;
    }

    @Override
    public SecretKey generateKey(int keySize) {
        return generateAesKey(keySize);
    }

    /**
     * Get current IV (returns a defensive copy).
     * 获取当前初始化向量（返回防御性副本）。
     *
     * @return IV bytes / 初始化向量字节
     */
    public byte[] getIv() {
        return iv == null ? null : iv.clone();
    }

    /**
     * Get current key.
     * 获取当前密钥。
     *
     * @return secret key / 密钥
     */
    public SecretKey getKey() {
        return key;
    }

    /**
     * Generate AES key of specified size.
     * 生成指定大小的 AES 密钥。
     *
     * @param keySize key size in bits / 密钥大小（位）
     * @return generated secret key / 生成的密钥
     */
    private SecretKey generateAesKey(int keySize) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(keySize, random);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new OpenCryptoException("Failed to generate AES key", e);
        }
    }

    private String getTransformation() {
        // CTR mode is a stream cipher mode and must use NoPadding
        String paddingValue = (mode == CipherMode.CTR) ? "NoPadding" : padding.getValue();
        return ALGORITHM + "/" + mode.name() + "/" + paddingValue;
    }

    /**
     * Builder class for AES cipher.
     * AES 加密器构建器。
     */
    public static class Builder {
        private int keySize = DEFAULT_KEY_SIZE;
        private CipherMode mode = CipherMode.CBC;
        private Padding padding = Padding.PKCS7;

        /**
         * Set key size in bits.
         * 设置密钥大小（比特）。
         *
         * @param bits key size (128, 192, or 256) / 密钥大小（128、192 或 256）
         * @return this builder / 当前构建器
         */
        public Builder keySize(int bits) {
            if (bits != 128 && bits != 192 && bits != 256) {
                throw new OpenKeyException("AES key size must be 128, 192, or 256 bits");
            }
            this.keySize = bits;
            return this;
        }

        /**
         * Set cipher mode.
         * 设置加密模式。
         *
         * @param mode cipher mode / 加密模式
         * @return this builder / 当前构建器
         */
        public Builder mode(CipherMode mode) {
            if (mode == CipherMode.GCM || mode == CipherMode.CCM) {
                throw new OpenCryptoException("Use AesGcmCipher for AEAD modes");
            }
            this.mode = mode;
            return this;
        }

        /**
         * Set padding scheme.
         * 设置填充方案。
         *
         * @param padding padding scheme / 填充方案
         * @return this builder / 当前构建器
         */
        public Builder padding(Padding padding) {
            this.padding = padding;
            return this;
        }

        /**
         * Build the AES cipher instance.
         * 构建 AES 加密实例。
         *
         * @return AES cipher instance / AES 加密实例
         */
        public AesCipher build() {
            AesCipher cipher = new AesCipher(keySize);
            cipher.mode = this.mode;
            cipher.padding = this.padding;
            return cipher;
        }
    }
}
