package cloud.opencode.base.crypto.symmetric;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import cloud.opencode.base.crypto.key.KeyGenerator;
import cloud.opencode.base.crypto.util.SecureEraser;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.ChaCha20ParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

/**
 * ChaCha20-Poly1305 cipher implementation - Modern AEAD cipher
 * ChaCha20-Poly1305 加密实现 - 现代 AEAD 加密
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ChaCha20-Poly1305 authenticated encryption - ChaCha20-Poly1305 认证加密</li>
 *   <li>256-bit key, 96-bit nonce - 256 位密钥，96 位随机数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AeadCipher chacha = ChaChaCipher.create();
 * byte[] encrypted = chacha.encrypt(plaintext, key);
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
public final class ChaChaCipher implements AeadCipher {

    private static final String ALGORITHM = "ChaCha20-Poly1305";
    private static final int NONCE_LENGTH = 12; // 96 bits
    private static final int KEY_LENGTH = 32; // 256 bits
    private static final int TAG_LENGTH = 128; // 128 bits (16 bytes)

    private SecretKey key;
    private byte[] nonce;
    private byte[] aad;
    private final SecureRandom random = new SecureRandom();

    private ChaChaCipher() {
    }

    /**
     * Create ChaCha20-Poly1305 cipher.
     * 创建 ChaCha20-Poly1305 加密器。
     *
     * @return ChaCha20-Poly1305 cipher instance / ChaCha20-Poly1305 加密实例
     */
    public static ChaChaCipher create() {
        return new ChaChaCipher();
    }

    /**
     * Create a builder for ChaCha20-Poly1305 cipher.
     * 创建 ChaCha20-Poly1305 加密器构建器。
     *
     * @return builder instance / 构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public ChaChaCipher setKey(SecretKey key) {
        this.key = key;
        return this;
    }

    @Override
    public ChaChaCipher setKey(byte[] key) {
        if (key == null || key.length != KEY_LENGTH) {
            throw new OpenKeyException("ChaCha20 key must be 256 bits (32 bytes)");
        }
        this.key = new SecretKeySpec(key, "ChaCha20");
        return this;
    }

    @Override
    public ChaChaCipher setIv(byte[] iv) {
        return setNonce(iv);
    }

    @Override
    public ChaChaCipher setNonce(byte[] nonce) {
        if (nonce != null && nonce.length != NONCE_LENGTH) {
            throw new OpenCryptoException("ChaCha20-Poly1305 nonce must be " + NONCE_LENGTH + " bytes (96 bits)");
        }
        this.nonce = nonce != null ? nonce.clone() : null;
        return this;
    }

    @Override
    public ChaChaCipher setAad(byte[] aad) {
        this.aad = aad != null ? aad.clone() : null;
        return this;
    }

    @Override
    public ChaChaCipher setTagLength(int tagBits) {
        if (tagBits != TAG_LENGTH) {
            throw new OpenCryptoException("ChaCha20-Poly1305 tag length is fixed at 128 bits");
        }
        // Tag length is fixed for ChaCha20-Poly1305
        return this;
    }

    @Override
    public byte[] encrypt(byte[] plaintext) {
        try {
            if (key == null) {
                key = KeyGenerator.generateChacha20Key();
            }
            // Always generate a fresh nonce for each encryption to prevent nonce reuse
            byte[] encNonce = generateNonce();
            this.nonce = encNonce;

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(encNonce);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

            if (aad != null) {
                cipher.updateAAD(aad);
            }

            byte[] ciphertext = cipher.doFinal(plaintext);
            // Prepend nonce to ciphertext so decrypt() can extract it
            byte[] result = new byte[encNonce.length + ciphertext.length];
            System.arraycopy(encNonce, 0, result, 0, encNonce.length);
            System.arraycopy(ciphertext, 0, result, encNonce.length, ciphertext.length);
            return result;
        } catch (Exception e) {
            throw new OpenCryptoException("ChaCha20-Poly1305 encryption failed", e);
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
            throw new OpenCryptoException("ChaCha20-Poly1305 file encryption failed", e);
        }
    }

    @Override
    public OutputStream encryptStream(OutputStream output) {
        throw new UnsupportedOperationException(
                "Streaming AEAD encryption is not supported for ChaCha20-Poly1305 - use byte array methods (encrypt/encryptBase64/encryptHex) instead");
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) {
        try {
            if (key == null) {
                throw new OpenKeyException("Key not set");
            }
            if (ciphertext == null || ciphertext.length < NONCE_LENGTH) {
                throw new OpenCryptoException("Ciphertext too short - must contain prepended nonce");
            }

            // Extract nonce from the first 12 bytes of the ciphertext
            byte[] decNonce = new byte[NONCE_LENGTH];
            System.arraycopy(ciphertext, 0, decNonce, 0, NONCE_LENGTH);
            byte[] actualCiphertext = new byte[ciphertext.length - NONCE_LENGTH];
            System.arraycopy(ciphertext, NONCE_LENGTH, actualCiphertext, 0, actualCiphertext.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(decNonce);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

            if (aad != null) {
                cipher.updateAAD(aad);
            }

            return cipher.doFinal(actualCiphertext);
        } catch (OpenCryptoException | OpenKeyException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenCryptoException("ChaCha20-Poly1305 decryption failed - authentication may have failed", e);
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
            throw new OpenCryptoException("ChaCha20-Poly1305 file decryption failed", e);
        }
    }

    @Override
    public InputStream decryptStream(InputStream input) {
        throw new UnsupportedOperationException(
                "Streaming AEAD decryption is not supported for ChaCha20-Poly1305 - use byte array methods (decrypt/decryptBase64/decryptHex) instead");
    }

    @Override
    public byte[] generateIv() {
        return generateNonce();
    }

    @Override
    public byte[] generateNonce() {
        byte[] newNonce = new byte[NONCE_LENGTH];
        random.nextBytes(newNonce);
        return newNonce;
    }

    @Override
    public int getIvLength() {
        return NONCE_LENGTH;
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }

    /**
     * Get current nonce (returns a defensive copy).
     * 获取当前随机数（返回防御性副本）。
     *
     * @return nonce bytes / 随机数字节
     */
    public byte[] getNonce() {
        return nonce == null ? null : nonce.clone();
    }

    /**
     * Get current key.
     * 获取当前密钥。
     *
     * @return secret key / 密钥
     */
    public SecretKey getKey() {
        if (key == null) return null;
        byte[] keyBytes = key.getEncoded();
        try {
            return new SecretKeySpec(keyBytes.clone(), key.getAlgorithm());
        } finally {
            SecureEraser.erase(keyBytes);
        }
    }

    /**
     * Get nonce length in bytes.
     * 获取随机数长度（字节）。
     *
     * @return nonce length / 随机数长度
     */
    public int getNonceLength() {
        return NONCE_LENGTH;
    }

    /**
     * Get authentication tag length in bits.
     * 获取认证标签长度（比特）。
     *
     * @return tag length / 标签长度
     */
    public int getTagLength() {
        return TAG_LENGTH;
    }

    /**
     * Builder class for ChaCha20-Poly1305 cipher.
     * ChaCha20-Poly1305 加密器构建器。
     */
    public static class Builder {

        /**
         * Build the ChaCha20-Poly1305 cipher instance.
         * 构建 ChaCha20-Poly1305 加密实例。
         *
         * @return ChaCha20-Poly1305 cipher instance / ChaCha20-Poly1305 加密实例
         */
        public ChaChaCipher build() {
            return new ChaChaCipher();
        }
    }
}
