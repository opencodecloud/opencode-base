/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.crypto.sealedbox;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.kdf.Hkdf;
import cloud.opencode.base.crypto.random.SecureRandoms;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;
import java.util.Objects;

/**
 * Sealed Box - Anonymous public-key encryption (NaCl/Libsodium style)
 * 密封盒 - 匿名公钥加密（NaCl/Libsodium 风格）
 *
 * <p>Provides anonymous public-key encryption where the sender's identity
 * is not revealed to the recipient. This is achieved by generating an
 * ephemeral key pair for each encryption operation.</p>
 * <p>提供匿名公钥加密，发送者的身份不会向接收者透露。
 * 这是通过为每次加密操作生成临时密钥对来实现的。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Anonymous encryption - sender identity not revealed - 匿名加密 - 不透露发送者身份</li>
 *   <li>Ephemeral key generation - 临时密钥生成</li>
 *   <li>X25519 key exchange + AES-GCM encryption - X25519 密钥交换 + AES-GCM 加密</li>
 *   <li>Authenticated encryption - 认证加密</li>
 *   <li>Simple API - 简单 API</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Generate recipient's key pair
 * KeyPair recipientKeyPair = SealedBox.generateKeyPair();
 *
 * // Sender encrypts (only needs recipient's public key)
 * byte[] message = "Secret message".getBytes();
 * byte[] sealed = SealedBox.seal(message, recipientKeyPair.getPublic());
 *
 * // Recipient decrypts (needs their private key)
 * byte[] opened = SealedBox.open(sealed, recipientKeyPair);
 *
 * // With builder for custom configuration
 * SealedBox box = SealedBox.builder()
 *     .algorithm(SealedBox.Algorithm.X25519_AES_GCM)
 *     .build();
 * byte[] sealed = box.encrypt(message, recipientPublicKey);
 * byte[] opened = box.decrypt(sealed, recipientKeyPair);
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
 * @see SecretBox
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class SealedBox {

    /** Nonce/IV size for AES-GCM in bytes */
    private static final int GCM_NONCE_LENGTH = 12;

    /** Authentication tag size for AES-GCM in bits */
    private static final int GCM_TAG_LENGTH = 128;

    /** X25519 public key size in bytes */
    private static final int X25519_PUBLIC_KEY_LENGTH = 32;

    /** Info string for HKDF key derivation */
    private static final byte[] HKDF_INFO = "SealedBox".getBytes(StandardCharsets.UTF_8);

    private final Algorithm algorithm;

    private SealedBox(Builder builder) {
        this.algorithm = builder.algorithm;
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Seals (encrypts) a message for a recipient.
     * 为接收者密封（加密）消息。
     *
     * @param plaintext the message to encrypt - 要加密的消息
     * @param recipientPublicKey the recipient's public key - 接收者的公钥
     * @return the sealed message - 密封的消息
     */
    public static byte[] seal(byte[] plaintext, PublicKey recipientPublicKey) {
        return builder().build().encrypt(plaintext, recipientPublicKey);
    }

    /**
     * Seals (encrypts) a string message for a recipient.
     * 为接收者密封（加密）字符串消息。
     *
     * @param plaintext the message to encrypt - 要加密的消息
     * @param recipientPublicKey the recipient's public key - 接收者的公钥
     * @return the sealed message - 密封的消息
     */
    public static byte[] seal(String plaintext, PublicKey recipientPublicKey) {
        return seal(plaintext.getBytes(StandardCharsets.UTF_8), recipientPublicKey);
    }

    /**
     * Opens (decrypts) a sealed message.
     * 打开（解密）密封的消息。
     *
     * @param sealed the sealed message - 密封的消息
     * @param recipientKeyPair the recipient's key pair - 接收者的密钥对
     * @return the decrypted message - 解密的消息
     */
    public static byte[] open(byte[] sealed, KeyPair recipientKeyPair) {
        return builder().build().decrypt(sealed, recipientKeyPair);
    }

    /**
     * Opens (decrypts) a sealed message and returns as string.
     * 打开（解密）密封的消息并返回字符串。
     *
     * @param sealed the sealed message - 密封的消息
     * @param recipientKeyPair the recipient's key pair - 接收者的密钥对
     * @return the decrypted message as string - 解密的消息字符串
     */
    public static String openAsString(byte[] sealed, KeyPair recipientKeyPair) {
        return new String(open(sealed, recipientKeyPair), StandardCharsets.UTF_8);
    }

    /**
     * Generates a key pair suitable for SealedBox.
     * 生成适用于 SealedBox 的密钥对。
     *
     * @return the key pair - 密钥对
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("X25519");
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new OpenCryptoException("X25519 not available", e);
        }
    }

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Instance Methods | 实例方法 ====================

    /**
     * Encrypts a message for a recipient.
     * 为接收者加密消息。
     *
     * @param plaintext the message to encrypt - 要加密的消息
     * @param recipientPublicKey the recipient's public key - 接收者的公钥
     * @return the encrypted message with ephemeral public key - 带有临时公钥的加密消息
     */
    public byte[] encrypt(byte[] plaintext, PublicKey recipientPublicKey) {
        Objects.requireNonNull(plaintext, "plaintext must not be null");
        Objects.requireNonNull(recipientPublicKey, "recipientPublicKey must not be null");

        try {
            // Generate ephemeral key pair
            KeyPair ephemeralKeyPair = generateKeyPair();

            // Perform key agreement
            byte[] sharedSecret = performKeyAgreement(
                    ephemeralKeyPair.getPrivate(),
                    recipientPublicKey
            );

            // Derive encryption key using HKDF
            SecretKey encryptionKey = deriveKey(sharedSecret,
                    getPublicKeyBytes(ephemeralKeyPair.getPublic()),
                    getPublicKeyBytes(recipientPublicKey));

            // Generate nonce
            byte[] nonce = SecureRandoms.nextBytes(GCM_NONCE_LENGTH);

            // Encrypt with AES-GCM
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_LENGTH, nonce));
            byte[] ciphertext = cipher.doFinal(plaintext);

            // Assemble: ephemeral_public_key || nonce || ciphertext
            byte[] ephemeralPublicKeyBytes = getPublicKeyBytes(ephemeralKeyPair.getPublic());
            ByteBuffer result = ByteBuffer.allocate(
                    ephemeralPublicKeyBytes.length + nonce.length + ciphertext.length
            );
            result.put(ephemeralPublicKeyBytes);
            result.put(nonce);
            result.put(ciphertext);

            // Clear sensitive data
            Arrays.fill(sharedSecret, (byte) 0);

            return result.array();

        } catch (Exception e) {
            throw new OpenCryptoException("Failed to seal message", e);
        }
    }

    /**
     * Decrypts a sealed message.
     * 解密密封的消息。
     *
     * @param sealed the sealed message - 密封的消息
     * @param recipientKeyPair the recipient's key pair - 接收者的密钥对
     * @return the decrypted message - 解密的消息
     */
    public byte[] decrypt(byte[] sealed, KeyPair recipientKeyPair) {
        Objects.requireNonNull(sealed, "sealed must not be null");
        Objects.requireNonNull(recipientKeyPair, "recipientKeyPair must not be null");

        if (sealed.length < X25519_PUBLIC_KEY_LENGTH + GCM_NONCE_LENGTH + 16) {
            throw new OpenCryptoException("Sealed message too short");
        }

        try {
            ByteBuffer buffer = ByteBuffer.wrap(sealed);

            // Extract ephemeral public key
            byte[] ephemeralPublicKeyBytes = new byte[X25519_PUBLIC_KEY_LENGTH];
            buffer.get(ephemeralPublicKeyBytes);
            PublicKey ephemeralPublicKey = bytesToPublicKey(ephemeralPublicKeyBytes);

            // Extract nonce
            byte[] nonce = new byte[GCM_NONCE_LENGTH];
            buffer.get(nonce);

            // Extract ciphertext
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            // Perform key agreement
            byte[] sharedSecret = performKeyAgreement(
                    recipientKeyPair.getPrivate(),
                    ephemeralPublicKey
            );

            // Derive decryption key using HKDF
            SecretKey decryptionKey = deriveKey(sharedSecret,
                    ephemeralPublicKeyBytes,
                    getPublicKeyBytes(recipientKeyPair.getPublic()));

            // Decrypt with AES-GCM
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, decryptionKey, new GCMParameterSpec(GCM_TAG_LENGTH, nonce));
            byte[] plaintext = cipher.doFinal(ciphertext);

            // Clear sensitive data
            Arrays.fill(sharedSecret, (byte) 0);

            return plaintext;

        } catch (Exception e) {
            throw new OpenCryptoException("Failed to open sealed message", e);
        }
    }

    // ==================== Private Methods | 私有方法 ====================

    private byte[] performKeyAgreement(PrivateKey privateKey, PublicKey publicKey) throws Exception {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("X25519");
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(publicKey, true);
        return keyAgreement.generateSecret();
    }

    private SecretKey deriveKey(byte[] sharedSecret, byte[] ephemeralPubKey, byte[] recipientPubKey) {
        // Combine inputs for HKDF salt
        byte[] salt = new byte[ephemeralPubKey.length + recipientPubKey.length];
        System.arraycopy(ephemeralPubKey, 0, salt, 0, ephemeralPubKey.length);
        System.arraycopy(recipientPubKey, 0, salt, ephemeralPubKey.length, recipientPubKey.length);

        // Derive 256-bit key using HKDF
        byte[] derived = Hkdf.sha256().deriveKey(sharedSecret, salt, HKDF_INFO, 32);
        return new SecretKeySpec(derived, "AES");
    }

    private byte[] getPublicKeyBytes(PublicKey publicKey) {
        // X25519 public keys are stored in X.509 format
        // The actual key bytes are the last 32 bytes
        byte[] encoded = publicKey.getEncoded();
        if (encoded.length == X25519_PUBLIC_KEY_LENGTH) {
            return encoded;
        }
        // For X.509 encoded keys, extract the raw bytes
        return Arrays.copyOfRange(encoded, encoded.length - X25519_PUBLIC_KEY_LENGTH, encoded.length);
    }

    private PublicKey bytesToPublicKey(byte[] bytes) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("X25519");
        // Create X25519 public key from raw bytes
        java.security.spec.X509EncodedKeySpec keySpec = createX25519PublicKeySpec(bytes);
        return keyFactory.generatePublic(keySpec);
    }

    private java.security.spec.X509EncodedKeySpec createX25519PublicKeySpec(byte[] publicKeyBytes) {
        // X.509 SubjectPublicKeyInfo structure for X25519
        // See RFC 8410
        byte[] x509Prefix = new byte[] {
                0x30, 0x2a,                         // SEQUENCE, 42 bytes
                0x30, 0x05,                         // SEQUENCE, 5 bytes (AlgorithmIdentifier)
                0x06, 0x03, 0x2b, 0x65, 0x6e,       // OID 1.3.101.110 (X25519)
                0x03, 0x21, 0x00                    // BIT STRING, 33 bytes (with leading 0x00)
        };

        byte[] encoded = new byte[x509Prefix.length + publicKeyBytes.length];
        System.arraycopy(x509Prefix, 0, encoded, 0, x509Prefix.length);
        System.arraycopy(publicKeyBytes, 0, encoded, x509Prefix.length, publicKeyBytes.length);

        return new java.security.spec.X509EncodedKeySpec(encoded);
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for SealedBox.
     * SealedBox 构建器。
     */
    public static final class Builder {
        private Algorithm algorithm = Algorithm.X25519_AES_GCM;

        private Builder() {}

        /**
         * Sets the algorithm.
         * 设置算法。
         */
        public Builder algorithm(Algorithm algorithm) {
            this.algorithm = Objects.requireNonNull(algorithm);
            return this;
        }

        /**
         * Builds the SealedBox.
         * 构建 SealedBox。
         */
        public SealedBox build() {
            return new SealedBox(this);
        }
    }

    /**
     * Supported algorithms for SealedBox.
     * SealedBox 支持的算法。
     */
    public enum Algorithm {
        /** X25519 key exchange with AES-256-GCM */
        X25519_AES_GCM
    }
}
