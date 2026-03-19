package cloud.opencode.base.crypto.envelope;


import cloud.opencode.base.crypto.enums.AsymmetricAlgorithm;
import cloud.opencode.base.crypto.enums.SymmetricAlgorithm;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import cloud.opencode.base.crypto.key.KeyGenerator;
import cloud.opencode.base.crypto.random.NonceGenerator;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

/**
 * Envelope encryption implementation - Combines asymmetric and symmetric encryption for secure data encryption
 * 信封加密实现 - 结合非对称和对称加密实现安全的数据加密
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Envelope encryption (RSA + AES-GCM) - 信封加密（RSA + AES-GCM）</li>
 *   <li>Data encryption key wrapping - 数据加密密钥包装</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm();
 * EncryptedEnvelope envelope = crypto.encrypt(data, publicKey);
 * byte[] decrypted = crypto.decrypt(envelope, privateKey);
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
public final class EnvelopeCrypto {

    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AsymmetricAlgorithm asymmetricAlgorithm;
    private final SymmetricAlgorithm symmetricAlgorithm;
    private PublicKey recipientPublicKey;
    private PrivateKey recipientPrivateKey;

    /**
     * Package-private constructor for use by HybridCrypto
     *
     * @param asymmetricAlgorithm asymmetric algorithm for key encryption
     * @param symmetricAlgorithm  symmetric algorithm for data encryption
     */
    EnvelopeCrypto(AsymmetricAlgorithm asymmetricAlgorithm, SymmetricAlgorithm symmetricAlgorithm) {
        this.asymmetricAlgorithm = asymmetricAlgorithm;
        this.symmetricAlgorithm = symmetricAlgorithm;
    }

    /**
     * Create envelope crypto with RSA-OAEP and AES-GCM (Recommended)
     * 创建使用 RSA-OAEP 和 AES-GCM 的信封加密（推荐）
     *
     * @return new EnvelopeCrypto instance
     */
    public static EnvelopeCrypto rsaAesGcm() {
        return new EnvelopeCrypto(AsymmetricAlgorithm.RSA_OAEP_SHA256, SymmetricAlgorithm.AES_GCM_256);
    }

    /**
     * Create envelope crypto with ECDH key agreement and AES-GCM.
     * 创建使用 ECDH 密钥协商和 AES-GCM 的信封加密。
     * <p>
     * <b>Not yet implemented.</b> ECDH key agreement requires a different key wrapping
     * mechanism than RSA OAEP. Use {@link #rsaAesGcm()} instead.
     *
     * @return never returns normally
     * @throws UnsupportedOperationException always — ECDH key agreement is not yet implemented
     */
    public static EnvelopeCrypto ecdhAesGcm() {
        throw new UnsupportedOperationException(
                "ECDH key agreement not yet implemented, use rsaAesGcm()");
    }

    /**
     * Create envelope crypto with X25519 key agreement and ChaCha20-Poly1305.
     * 创建使用 X25519 密钥协商和 ChaCha20-Poly1305 的信封加密。
     * <p>
     * <b>Not yet implemented.</b> X25519 key agreement requires a different key wrapping
     * mechanism than RSA OAEP. Use {@link #rsaAesGcm()} instead.
     *
     * @return never returns normally
     * @throws UnsupportedOperationException always — X25519 key agreement is not yet implemented
     */
    public static EnvelopeCrypto x25519ChaCha20() {
        throw new UnsupportedOperationException(
                "X25519 key agreement not yet implemented, use rsaAesGcm()");
    }

    /**
     * Set recipient public key for encryption
     * 设置接收者公钥用于加密
     *
     * @param publicKey recipient's public key
     * @return this instance for method chaining
     * @throws NullPointerException if publicKey is null
     */
    public EnvelopeCrypto setRecipientPublicKey(PublicKey publicKey) {
        this.recipientPublicKey = Objects.requireNonNull(publicKey, "Public key cannot be null");
        return this;
    }

    /**
     * Set recipient private key for decryption
     * 设置接收者私钥用于解密
     *
     * @param privateKey recipient's private key
     * @return this instance for method chaining
     * @throws NullPointerException if privateKey is null
     */
    public EnvelopeCrypto setRecipientPrivateKey(PrivateKey privateKey) {
        this.recipientPrivateKey = Objects.requireNonNull(privateKey, "Private key cannot be null");
        return this;
    }

    /**
     * Encrypt plaintext using envelope encryption
     * 使用信封加密加密明文
     * <p>
     * Process:
     * 1. Generate random DEK (Data Encryption Key)
     * 2. Encrypt plaintext with DEK using symmetric algorithm
     * 3. Encrypt DEK with recipient's public key
     * 4. Return EncryptedEnvelope containing all components
     *
     * @param plaintext data to encrypt
     * @return encrypted envelope
     * @throws NullPointerException     if plaintext is null
     * @throws IllegalStateException    if public key is not set
     * @throws OpenCryptoException      if encryption fails
     */
    public EncryptedEnvelope encrypt(byte[] plaintext) {
        return encrypt(plaintext, null);
    }

    /**
     * Encrypt plaintext using envelope encryption with additional authenticated data
     * 使用信封加密加密明文，支持附加认证数据
     *
     * @param plaintext data to encrypt
     * @param aad       additional authenticated data (can be null)
     * @return encrypted envelope
     * @throws NullPointerException     if plaintext is null
     * @throws IllegalStateException    if public key is not set
     * @throws OpenCryptoException      if encryption fails
     */
    public EncryptedEnvelope encrypt(byte[] plaintext, byte[] aad) {
        Objects.requireNonNull(plaintext, "Plaintext cannot be null");
        if (recipientPublicKey == null) {
            throw new IllegalStateException("Recipient public key not set");
        }

        byte[] dekBytes = null;
        try {
            // Step 1: Generate random DEK
            SecretKey dek = generateDataEncryptionKey();
            dekBytes = dek.getEncoded();

            // Step 2: Generate IV
            byte[] iv = generateIv();

            // Step 3: Encrypt plaintext with DEK
            byte[] ciphertext;
            byte[] tag = null;

            if (symmetricAlgorithm.isAead()) {
                // AEAD mode (GCM, ChaCha20-Poly1305)
                Cipher cipher = Cipher.getInstance(symmetricAlgorithm.getTransformation());
                GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
                cipher.init(Cipher.ENCRYPT_MODE, dek, spec);

                if (aad != null && aad.length > 0) {
                    cipher.updateAAD(aad);
                }

                byte[] encrypted = cipher.doFinal(plaintext);
                // For GCM, tag is appended to ciphertext
                int tagLength = GCM_TAG_LENGTH / 8;
                ciphertext = new byte[encrypted.length - tagLength];
                tag = new byte[tagLength];
                System.arraycopy(encrypted, 0, ciphertext, 0, ciphertext.length);
                System.arraycopy(encrypted, ciphertext.length, tag, 0, tag.length);
            } else {
                // Non-AEAD mode (CBC, CTR)
                Cipher cipher = Cipher.getInstance(symmetricAlgorithm.getTransformation());
                IvParameterSpec spec = new IvParameterSpec(iv);
                cipher.init(Cipher.ENCRYPT_MODE, dek, spec);
                ciphertext = cipher.doFinal(plaintext);
            }

            // Step 4: Encrypt DEK with recipient's public key
            byte[] encryptedKey = encryptKey(dekBytes);

            return new EncryptedEnvelope(encryptedKey, iv, ciphertext, tag);

        } catch (Exception e) {
            throw OpenCryptoException.encryptionFailed("EnvelopeCrypto", e);
        } finally {
            // Zero DEK plaintext bytes after use
            if (dekBytes != null) {
                Arrays.fill(dekBytes, (byte) 0);
            }
        }
    }

    /**
     * Encrypt plaintext and return Base64 encoded result
     * 加密明文并返回 Base64 编码结果
     *
     * @param plaintext data to encrypt
     * @return Base64 encoded encrypted envelope
     * @throws NullPointerException     if plaintext is null
     * @throws IllegalStateException    if public key is not set
     * @throws OpenCryptoException      if encryption fails
     */
    public String encryptBase64(byte[] plaintext) {
        return encrypt(plaintext).toBase64();
    }

    /**
     * Decrypt encrypted envelope
     * 解密加密信封
     * <p>
     * Process:
     * 1. Decrypt DEK using recipient's private key
     * 2. Decrypt ciphertext using DEK
     *
     * @param envelope encrypted envelope
     * @return decrypted plaintext
     * @throws NullPointerException     if envelope is null
     * @throws IllegalStateException    if private key is not set
     * @throws OpenCryptoException      if decryption fails
     */
    public byte[] decrypt(EncryptedEnvelope envelope) {
        return decrypt(envelope, null);
    }

    /**
     * Decrypt encrypted envelope with additional authenticated data
     * 解密加密信封，支持附加认证数据
     *
     * @param envelope encrypted envelope
     * @param aad      additional authenticated data (must match encryption AAD)
     * @return decrypted plaintext
     * @throws NullPointerException     if envelope is null
     * @throws IllegalStateException    if private key is not set
     * @throws OpenCryptoException      if decryption fails or authentication fails
     */
    public byte[] decrypt(EncryptedEnvelope envelope, byte[] aad) {
        Objects.requireNonNull(envelope, "Envelope cannot be null");
        if (recipientPrivateKey == null) {
            throw new IllegalStateException("Recipient private key not set");
        }

        byte[] dekBytes = null;
        try {
            // Step 1: Decrypt DEK with recipient's private key
            dekBytes = decryptKey(envelope.encryptedKey());
            String algorithm = symmetricAlgorithm.getTransformation().split("/")[0];
            SecretKey dek = new SecretKeySpec(dekBytes, algorithm);

            // Step 2: Decrypt ciphertext with DEK
            byte[] plaintext;

            if (symmetricAlgorithm.isAead()) {
                // AEAD mode (GCM, ChaCha20-Poly1305)
                Cipher cipher = Cipher.getInstance(symmetricAlgorithm.getTransformation());
                GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, envelope.iv());
                cipher.init(Cipher.DECRYPT_MODE, dek, spec);

                if (aad != null && aad.length > 0) {
                    cipher.updateAAD(aad);
                }

                // For GCM, combine ciphertext and tag
                byte[] ciphertext = envelope.ciphertext();
                byte[] tag = envelope.tag();
                byte[] combined = new byte[ciphertext.length + (tag != null ? tag.length : 0)];
                System.arraycopy(ciphertext, 0, combined, 0, ciphertext.length);
                if (tag != null) {
                    System.arraycopy(tag, 0, combined, ciphertext.length, tag.length);
                }

                plaintext = cipher.doFinal(combined);
            } else {
                // Non-AEAD mode (CBC, CTR)
                Cipher cipher = Cipher.getInstance(symmetricAlgorithm.getTransformation());
                IvParameterSpec spec = new IvParameterSpec(envelope.iv());
                cipher.init(Cipher.DECRYPT_MODE, dek, spec);
                plaintext = cipher.doFinal(envelope.ciphertext());
            }

            return plaintext;

        } catch (Exception e) {
            throw OpenCryptoException.decryptionFailed("EnvelopeCrypto", e);
        } finally {
            // Zero DEK plaintext bytes after use
            if (dekBytes != null) {
                Arrays.fill(dekBytes, (byte) 0);
            }
        }
    }

    /**
     * Decrypt Base64 encoded encrypted envelope
     * 解密 Base64 编码的加密信封
     *
     * @param base64Envelope Base64 encoded encrypted envelope
     * @return decrypted plaintext
     * @throws NullPointerException     if base64Envelope is null
     * @throws IllegalStateException    if private key is not set
     * @throws OpenCryptoException      if decryption fails
     */
    public byte[] decryptBase64(String base64Envelope) {
        Objects.requireNonNull(base64Envelope, "Base64 envelope cannot be null");
        EncryptedEnvelope envelope = EncryptedEnvelope.fromBase64(base64Envelope);
        return decrypt(envelope);
    }

    /**
     * Generate data encryption key
     *
     * @return generated secret key
     */
    private SecretKey generateDataEncryptionKey() {
        return KeyGenerator.generateAesKey(symmetricAlgorithm.getKeySize());
    }

    /**
     * Generate initialization vector
     *
     * @return IV bytes
     */
    private byte[] generateIv() {
        if (symmetricAlgorithm.isAead()) {
            return NonceGenerator.forAesGcm(SECURE_RANDOM);
        } else {
            // For CBC/CTR, use 16 bytes (128 bits)
            byte[] iv = new byte[16];
            SECURE_RANDOM.nextBytes(iv);
            return iv;
        }
    }

    /**
     * Encrypt key using recipient's public key
     *
     * @param keyBytes key bytes to encrypt
     * @return encrypted key bytes
     */
    private byte[] encryptKey(byte[] keyBytes) {
        try {
            Cipher cipher = Cipher.getInstance(asymmetricAlgorithm.getTransformation());
            cipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey);
            return cipher.doFinal(keyBytes);
        } catch (Exception e) {
            throw OpenCryptoException.encryptionFailed(asymmetricAlgorithm.name(), e);
        }
    }

    /**
     * Decrypt key using recipient's private key
     *
     * @param encryptedKeyBytes encrypted key bytes
     * @return decrypted key bytes
     */
    private byte[] decryptKey(byte[] encryptedKeyBytes) {
        try {
            Cipher cipher = Cipher.getInstance(asymmetricAlgorithm.getTransformation());
            cipher.init(Cipher.DECRYPT_MODE, recipientPrivateKey);
            return cipher.doFinal(encryptedKeyBytes);
        } catch (Exception e) {
            throw OpenCryptoException.decryptionFailed(asymmetricAlgorithm.name(), e);
        }
    }

    /**
     * Get the asymmetric algorithm
     *
     * @return asymmetric algorithm
     */
    public AsymmetricAlgorithm getAsymmetricAlgorithm() {
        return asymmetricAlgorithm;
    }

    /**
     * Get the symmetric algorithm
     *
     * @return symmetric algorithm
     */
    public SymmetricAlgorithm getSymmetricAlgorithm() {
        return symmetricAlgorithm;
    }
}
