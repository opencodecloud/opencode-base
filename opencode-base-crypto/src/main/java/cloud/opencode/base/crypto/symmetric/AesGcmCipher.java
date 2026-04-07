package cloud.opencode.base.crypto.symmetric;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import cloud.opencode.base.crypto.key.KeyGenerator;
import cloud.opencode.base.crypto.util.SecureEraser;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

/**
 * AES-GCM cipher implementation - Authenticated Encryption with Associated Data
 * AES-GCM 加密实现 - 关联数据认证加密
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>AES-128-GCM and AES-256-GCM - AES-128-GCM 和 AES-256-GCM</li>
 *   <li>Authenticated encryption with associated data - 带关联数据的认证加密</li>
 *   <li>Automatic nonce generation - 自动随机数生成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AeadCipher gcm = AesGcmCipher.aes256Gcm();
 * byte[] encrypted = gcm.encrypt(plaintext, key);
 * byte[] decrypted = gcm.decrypt(encrypted, key);
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
public final class AesGcmCipher implements AeadCipher {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int DEFAULT_IV_LENGTH = 12; // 96 bits recommended for GCM
    private static final int DEFAULT_TAG_LENGTH = 128; // 128 bits (16 bytes)
    private static final int DEFAULT_KEY_SIZE = 256;

    private SecretKey key;
    private byte[] iv;
    private byte[] aad;
    private int tagLength = DEFAULT_TAG_LENGTH;
    private final int keySize;
    private static final SecureRandom RANDOM = new SecureRandom();

    private AesGcmCipher(int keySize) {
        this.keySize = keySize;
    }

    /**
     * Create AES-128-GCM cipher.
     * 创建 AES-128-GCM 加密器。
     *
     * @return AES-128-GCM cipher instance / AES-128-GCM 加密实例
     */
    public static AesGcmCipher aes128Gcm() {
        return new AesGcmCipher(128);
    }

    /**
     * Create AES-256-GCM cipher (recommended).
     * 创建 AES-256-GCM 加密器（推荐）。
     *
     * @return AES-256-GCM cipher instance / AES-256-GCM 加密实例
     */
    public static AesGcmCipher aes256Gcm() {
        return new AesGcmCipher(256);
    }

    /**
     * Create default AES-GCM cipher (256-bit).
     * 创建默认 AES-GCM 加密器（256 位）。
     *
     * @return AES-GCM cipher instance / AES-GCM 加密实例
     */
    public static AesGcmCipher create() {
        return new AesGcmCipher(DEFAULT_KEY_SIZE);
    }

    /**
     * Create a builder for AES-GCM cipher.
     * 创建 AES-GCM 加密器构建器。
     *
     * @return builder instance / 构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public AesGcmCipher setKey(SecretKey key) {
        this.key = key;
        return this;
    }

    @Override
    public AesGcmCipher setKey(byte[] key) {
        AesKeyValidator.validateKeyBytes(key, "AES-GCM");
        this.key = new SecretKeySpec(key, "AES");
        return this;
    }

    @Override
    public AesGcmCipher setIv(byte[] iv) {
        if (iv != null && iv.length != DEFAULT_IV_LENGTH) {
            throw new OpenCryptoException("AES-GCM IV should be " + DEFAULT_IV_LENGTH + " bytes (96 bits)");
        }
        this.iv = iv != null ? iv.clone() : null;
        return this;
    }

    @Override
    public AesGcmCipher setNonce(byte[] nonce) {
        return setIv(nonce);
    }

    @Override
    public AesGcmCipher setAad(byte[] aad) {
        this.aad = aad != null ? aad.clone() : null;
        return this;
    }

    @Override
    public AesGcmCipher setTagLength(int tagBits) {
        if (tagBits != 96 && tagBits != 104 && tagBits != 112 && tagBits != 120 && tagBits != 128) {
            throw new OpenCryptoException("AES-GCM tag length must be 96, 104, 112, 120, or 128 bits");
        }
        this.tagLength = tagBits;
        return this;
    }

    @Override
    public byte[] encrypt(byte[] plaintext) {
        try {
            if (key == null) {
                key = KeyGenerator.generateAesKey(keySize);
            }
            // Always generate a fresh IV for each encryption to prevent nonce reuse
            byte[] encIv = generateIv();
            this.iv = encIv;

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(tagLength, encIv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            if (aad != null) {
                cipher.updateAAD(aad);
            }

            byte[] ciphertext = cipher.doFinal(plaintext);
            // Prepend IV to ciphertext so decrypt() can extract it
            byte[] result = new byte[encIv.length + ciphertext.length];
            System.arraycopy(encIv, 0, result, 0, encIv.length);
            System.arraycopy(ciphertext, 0, result, encIv.length, ciphertext.length);
            return result;
        } catch (Exception e) {
            throw new OpenCryptoException("AES-GCM encryption failed", e);
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
    public String encryptBase64(String plaintext) {
        return OpenBase64.encode(encrypt(plaintext));
    }

    @Override
    public String encryptHex(byte[] plaintext) {
        return HexCodec.encode(encrypt(plaintext));
    }

    @Override
    public void encryptFile(Path source, Path target) {
        try {
            byte[] data = Files.readAllBytes(source);
            byte[] encrypted = encrypt(data);
            Files.write(target, encrypted);
        } catch (IOException e) {
            throw new OpenCryptoException("AES-GCM file encryption failed", e);
        }
    }

    @Override
    public OutputStream encryptStream(OutputStream output) {
        throw new UnsupportedOperationException(
                "Streaming AEAD encryption is not supported for AES-GCM - use byte array methods (encrypt/encryptBase64/encryptHex) instead");
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) {
        try {
            if (key == null) {
                throw new OpenKeyException("Key not set");
            }
            if (ciphertext == null || ciphertext.length < DEFAULT_IV_LENGTH) {
                throw new OpenCryptoException("Ciphertext too short - must contain prepended IV");
            }

            // Extract IV from the first 12 bytes of the ciphertext
            byte[] decIv = new byte[DEFAULT_IV_LENGTH];
            System.arraycopy(ciphertext, 0, decIv, 0, DEFAULT_IV_LENGTH);
            byte[] actualCiphertext = new byte[ciphertext.length - DEFAULT_IV_LENGTH];
            System.arraycopy(ciphertext, DEFAULT_IV_LENGTH, actualCiphertext, 0, actualCiphertext.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(tagLength, decIv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            if (aad != null) {
                cipher.updateAAD(aad);
            }

            return cipher.doFinal(actualCiphertext);
        } catch (OpenCryptoException | OpenKeyException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenCryptoException("AES-GCM decryption failed - authentication may have failed", e);
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
    public String decryptBase64ToString(String base64Ciphertext) {
        return decryptToString(decryptBase64(base64Ciphertext));
    }

    @Override
    public byte[] decryptHex(String hexCiphertext) {
        return decrypt(HexCodec.decode(hexCiphertext));
    }

    @Override
    public void decryptFile(Path source, Path target) {
        try {
            byte[] data = Files.readAllBytes(source);
            byte[] decrypted = decrypt(data);
            Files.write(target, decrypted);
        } catch (IOException e) {
            throw new OpenCryptoException("AES-GCM file decryption failed", e);
        }
    }

    @Override
    public InputStream decryptStream(InputStream input) {
        throw new UnsupportedOperationException(
                "Streaming AEAD decryption is not supported for AES-GCM - use byte array methods (decrypt/decryptBase64/decryptHex) instead");
    }

    @Override
    public byte[] generateIv() {
        byte[] newIv = new byte[DEFAULT_IV_LENGTH];
        RANDOM.nextBytes(newIv);
        return newIv;
    }

    @Override
    public byte[] generateNonce() {
        return generateIv();
    }

    @Override
    public int getIvLength() {
        return DEFAULT_IV_LENGTH;
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }

    /**
     * Get current IV (defensive copy).
     * 获取当前初始化向量（防御性拷贝）。
     *
     * @return copy of IV bytes / 初始化向量字节的拷贝
     */
    public byte[] getIv() {
        return iv != null ? iv.clone() : null;
    }

    /**
     * Get current key (defensive copy).
     * 获取当前密钥（防御性拷贝）。
     *
     * <p>Note: Returns a new SecretKeySpec with a copy of the key bytes to prevent
     * external modification of the internal key state.</p>
     * <p>注意：返回一个新的SecretKeySpec，包含密钥字节的拷贝，以防止外部修改内部密钥状态。</p>
     *
     * @return copy of secret key / 密钥的拷贝
     */
    public SecretKey getKey() {
        if (key == null) {
            return null;
        }
        byte[] keyBytes = key.getEncoded();
        try {
            return new javax.crypto.spec.SecretKeySpec(keyBytes.clone(), key.getAlgorithm());
        } finally {
            SecureEraser.erase(keyBytes);
        }
    }

    /**
     * Get authentication tag length in bits.
     * 获取认证标签长度（比特）。
     *
     * @return tag length / 标签长度
     */
    public int getTagLength() {
        return tagLength;
    }

    /**
     * Builder class for AES-GCM cipher.
     * AES-GCM 加密器构建器。
     */
    public static class Builder {
        private int keySize = DEFAULT_KEY_SIZE;
        private int tagLength = DEFAULT_TAG_LENGTH;

        /**
         * Set key size in bits.
         * 设置密钥大小（比特）。
         *
         * @param bits key size (128, 192, or 256) / 密钥大小（128、192 或 256）
         * @return this builder / 当前构建器
         */
        public Builder keySize(int bits) {
            if (bits != 128 && bits != 192 && bits != 256) {
                throw new OpenKeyException("AES-GCM key size must be 128, 192, or 256 bits");
            }
            this.keySize = bits;
            return this;
        }

        /**
         * Set authentication tag length in bits.
         * 设置认证标签长度（比特）。
         *
         * @param bits tag length (96, 104, 112, 120, or 128) / 标签长度（96、104、112、120 或 128）
         * @return this builder / 当前构建器
         */
        public Builder tagLength(int bits) {
            if (bits != 96 && bits != 104 && bits != 112 && bits != 120 && bits != 128) {
                throw new OpenCryptoException("AES-GCM tag length must be 96, 104, 112, 120, or 128 bits");
            }
            this.tagLength = bits;
            return this;
        }

        /**
         * Build the AES-GCM cipher instance.
         * 构建 AES-GCM 加密实例。
         *
         * @return AES-GCM cipher instance / AES-GCM 加密实例
         */
        public AesGcmCipher build() {
            AesGcmCipher cipher = new AesGcmCipher(keySize);
            cipher.tagLength = this.tagLength;
            return cipher;
        }
    }
}
