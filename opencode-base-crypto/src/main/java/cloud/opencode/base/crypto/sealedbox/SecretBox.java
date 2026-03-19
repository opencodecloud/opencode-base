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
import cloud.opencode.base.crypto.key.KeyGenerator;
import cloud.opencode.base.crypto.random.SecureRandoms;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Secret Box - Simplified symmetric encryption (NaCl/Libsodium style)
 * 秘密盒 - 简化的对称加密（NaCl/Libsodium 风格）
 *
 * <p>Provides a simple interface for authenticated symmetric encryption
 * using AES-GCM. The nonce is automatically generated and prepended
 * to the ciphertext.</p>
 * <p>使用 AES-GCM 提供简单的认证对称加密接口。
 * 随机数自动生成并添加到密文前面。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Authenticated encryption - 认证加密</li>
 *   <li>Automatic nonce generation - 自动随机数生成</li>
 *   <li>Simple API - 简单 API</li>
 *   <li>256-bit AES-GCM - 256位 AES-GCM</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Generate a key
 * SecretKey key = SecretBox.generateKey();
 *
 * // Encrypt
 * byte[] message = "Secret message".getBytes();
 * byte[] encrypted = SecretBox.encrypt(message, key);
 *
 * // Decrypt
 * byte[] decrypted = SecretBox.decrypt(encrypted, key);
 *
 * // With string convenience methods
 * String encrypted = SecretBox.encryptString("Hello", key);
 * String decrypted = SecretBox.decryptString(encrypted, key);
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
 * @see SealedBox
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class SecretBox {

    /** Nonce/IV size for AES-GCM in bytes */
    private static final int GCM_NONCE_LENGTH = 12;

    /** Authentication tag size for AES-GCM in bits */
    private static final int GCM_TAG_LENGTH = 128;

    /** Key size in bits */
    private static final int KEY_SIZE_BITS = 256;

    private SecretBox() {}

    // ==================== Key Generation | 密钥生成 ====================

    /**
     * Generates a new 256-bit secret key.
     * 生成新的 256 位密钥。
     *
     * @return the secret key - 密钥
     */
    public static SecretKey generateKey() {
        return KeyGenerator.generateAesKey(KEY_SIZE_BITS);
    }

    /**
     * Creates a secret key from raw bytes.
     * 从原始字节创建密钥。
     *
     * @param keyBytes the key bytes (must be 32 bytes) - 密钥字节（必须是 32 字节）
     * @return the secret key - 密钥
     */
    public static SecretKey keyFromBytes(byte[] keyBytes) {
        if (keyBytes == null || keyBytes.length != 32) {
            throw new OpenCryptoException("Key must be 32 bytes");
        }
        return new SecretKeySpec(keyBytes, "AES");
    }

    // ==================== Encryption | 加密 ====================

    /**
     * Encrypts a message.
     * 加密消息。
     *
     * @param plaintext the message to encrypt - 要加密的消息
     * @param key the secret key - 密钥
     * @return the encrypted message (nonce + ciphertext) - 加密的消息（随机数 + 密文）
     */
    public static byte[] encrypt(byte[] plaintext, SecretKey key) {
        Objects.requireNonNull(plaintext, "plaintext must not be null");
        Objects.requireNonNull(key, "key must not be null");

        try {
            // Generate random nonce
            byte[] nonce = SecureRandoms.nextBytes(GCM_NONCE_LENGTH);

            // Encrypt
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, nonce));
            byte[] ciphertext = cipher.doFinal(plaintext);

            // Combine: nonce || ciphertext
            ByteBuffer result = ByteBuffer.allocate(nonce.length + ciphertext.length);
            result.put(nonce);
            result.put(ciphertext);

            return result.array();

        } catch (Exception e) {
            throw new OpenCryptoException("Failed to encrypt message", e);
        }
    }

    /**
     * Encrypts a string message.
     * 加密字符串消息。
     *
     * @param plaintext the message to encrypt - 要加密的消息
     * @param key the secret key - 密钥
     * @return the encrypted message - 加密的消息
     */
    public static byte[] encrypt(String plaintext, SecretKey key) {
        return encrypt(plaintext.getBytes(StandardCharsets.UTF_8), key);
    }

    /**
     * Encrypts with additional authenticated data (AAD).
     * 使用附加认证数据（AAD）加密。
     *
     * @param plaintext the message to encrypt - 要加密的消息
     * @param key the secret key - 密钥
     * @param aad additional authenticated data - 附加认证数据
     * @return the encrypted message - 加密的消息
     */
    public static byte[] encryptWithAad(byte[] plaintext, SecretKey key, byte[] aad) {
        Objects.requireNonNull(plaintext, "plaintext must not be null");
        Objects.requireNonNull(key, "key must not be null");

        try {
            byte[] nonce = SecureRandoms.nextBytes(GCM_NONCE_LENGTH);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, nonce));

            if (aad != null && aad.length > 0) {
                cipher.updateAAD(aad);
            }

            byte[] ciphertext = cipher.doFinal(plaintext);

            ByteBuffer result = ByteBuffer.allocate(nonce.length + ciphertext.length);
            result.put(nonce);
            result.put(ciphertext);

            return result.array();

        } catch (Exception e) {
            throw new OpenCryptoException("Failed to encrypt message with AAD", e);
        }
    }

    // ==================== Decryption | 解密 ====================

    /**
     * Decrypts a message.
     * 解密消息。
     *
     * @param encrypted the encrypted message (nonce + ciphertext) - 加密的消息
     * @param key the secret key - 密钥
     * @return the decrypted message - 解密的消息
     */
    public static byte[] decrypt(byte[] encrypted, SecretKey key) {
        Objects.requireNonNull(encrypted, "encrypted must not be null");
        Objects.requireNonNull(key, "key must not be null");

        if (encrypted.length < GCM_NONCE_LENGTH + 16) {
            throw new OpenCryptoException("Encrypted message too short");
        }

        try {
            ByteBuffer buffer = ByteBuffer.wrap(encrypted);

            // Extract nonce
            byte[] nonce = new byte[GCM_NONCE_LENGTH];
            buffer.get(nonce);

            // Extract ciphertext
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            // Decrypt
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, nonce));

            return cipher.doFinal(ciphertext);

        } catch (javax.crypto.AEADBadTagException e) {
            throw new OpenCryptoException("Authentication failed - message tampered or wrong key", e);
        } catch (Exception e) {
            throw new OpenCryptoException("Failed to decrypt message", e);
        }
    }

    /**
     * Decrypts a message and returns as string.
     * 解密消息并返回字符串。
     *
     * @param encrypted the encrypted message - 加密的消息
     * @param key the secret key - 密钥
     * @return the decrypted message as string - 解密的消息字符串
     */
    public static String decryptAsString(byte[] encrypted, SecretKey key) {
        return new String(decrypt(encrypted, key), StandardCharsets.UTF_8);
    }

    /**
     * Decrypts with additional authenticated data (AAD).
     * 使用附加认证数据（AAD）解密。
     *
     * @param encrypted the encrypted message - 加密的消息
     * @param key the secret key - 密钥
     * @param aad additional authenticated data - 附加认证数据
     * @return the decrypted message - 解密的消息
     */
    public static byte[] decryptWithAad(byte[] encrypted, SecretKey key, byte[] aad) {
        Objects.requireNonNull(encrypted, "encrypted must not be null");
        Objects.requireNonNull(key, "key must not be null");

        if (encrypted.length < GCM_NONCE_LENGTH + 16) {
            throw new OpenCryptoException("Encrypted message too short");
        }

        try {
            ByteBuffer buffer = ByteBuffer.wrap(encrypted);

            byte[] nonce = new byte[GCM_NONCE_LENGTH];
            buffer.get(nonce);

            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, nonce));

            if (aad != null && aad.length > 0) {
                cipher.updateAAD(aad);
            }

            return cipher.doFinal(ciphertext);

        } catch (javax.crypto.AEADBadTagException e) {
            throw new OpenCryptoException("Authentication failed - message tampered, wrong key, or AAD mismatch", e);
        } catch (Exception e) {
            throw new OpenCryptoException("Failed to decrypt message with AAD", e);
        }
    }
}
