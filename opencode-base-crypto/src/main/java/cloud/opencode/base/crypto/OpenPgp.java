/*
 * Copyright 2025 OpenCode Cloud Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.crypto;

import cloud.opencode.base.crypto.pgp.PgpAlgorithm;
import cloud.opencode.base.crypto.pgp.PgpCipher;
import cloud.opencode.base.crypto.pgp.PgpKeyPair;
import cloud.opencode.base.crypto.pgp.PgpKeyUtil;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;

import java.util.Objects;

/**
 * OpenPGP Facade - Simplified API for PGP encryption and decryption
 * OpenPGP 门面类 - 简化的 PGP 加密解密 API
 *
 * <p>This facade provides easy-to-use static methods for common PGP operations,
 * commonly used in email security and file encryption scenarios.</p>
 * <p>此门面类提供易于使用的静态方法用于常见的 PGP 操作，通常用于电子邮件安全和文件加密场景。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Key pair generation - 密钥对生成</li>
 *   <li>Public key encryption - 公钥加密</li>
 *   <li>Private key decryption - 私钥解密</li>
 *   <li>Armored ASCII format support - 装甲 ASCII 格式支持</li>
 *   <li>Key import/export - 密钥导入/导出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Generate key pair
 * PgpKeyPair keyPair = OpenPgp.generateKeyPair("user@example.com", "passphrase");
 *
 * // Export public key for sharing
 * String publicKeyArmor = OpenPgp.exportPublicKey(keyPair);
 *
 * // Encrypt message
 * String encrypted = OpenPgp.encrypt("Hello, World!", keyPair.publicKey());
 *
 * // Decrypt message
 * String decrypted = OpenPgp.decrypt(encrypted, keyPair.secretKey(), "passphrase");
 *
 * // Quick encrypt/decrypt with key pair
 * String encrypted = OpenPgp.encrypt("message", keyPair);
 * String decrypted = OpenPgp.decrypt(encrypted, keyPair, "passphrase");
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
 * @since JDK 25, opencode-base-crypto V1.2.0
 */
public final class OpenPgp {

    private OpenPgp() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // ==================== Key Generation ====================

    /**
     * Generates a new PGP key pair with default 4096-bit RSA.
     * 使用默认的 4096 位 RSA 生成新的 PGP 密钥对。
     *
     * @param userId     the user ID (typically email address)
     * @param passphrase the passphrase to protect the secret key
     * @return the generated PGP key pair
     */
    public static PgpKeyPair generateKeyPair(String userId, String passphrase) {
        return PgpKeyUtil.generateKeyPair(userId, passphrase);
    }

    /**
     * Generates a new PGP key pair with specified key size.
     * 使用指定密钥大小生成新的 PGP 密钥对。
     *
     * @param userId     the user ID (typically email address)
     * @param passphrase the passphrase to protect the secret key
     * @param keySize    the RSA key size in bits (minimum 2048)
     * @return the generated PGP key pair
     */
    public static PgpKeyPair generateKeyPair(String userId, String passphrase, int keySize) {
        return PgpKeyUtil.generateKeyPair(userId, passphrase, keySize);
    }

    // ==================== Encryption ====================

    /**
     * Encrypts a message using the public key.
     * 使用公钥加密消息。
     *
     * @param plaintext the message to encrypt
     * @param publicKey the recipient's public key
     * @return armored encrypted message
     */
    public static String encrypt(String plaintext, PGPPublicKey publicKey) {
        Objects.requireNonNull(plaintext, "plaintext must not be null");
        Objects.requireNonNull(publicKey, "publicKey must not be null");

        return PgpCipher.create()
                .withPublicKey(publicKey)
                .encryptArmored(plaintext);
    }

    /**
     * Encrypts a message using the armored public key.
     * 使用装甲格式的公钥加密消息。
     *
     * @param plaintext        the message to encrypt
     * @param armoredPublicKey the recipient's armored public key
     * @return armored encrypted message
     */
    public static String encrypt(String plaintext, String armoredPublicKey) {
        Objects.requireNonNull(plaintext, "plaintext must not be null");
        Objects.requireNonNull(armoredPublicKey, "armoredPublicKey must not be null");

        return PgpCipher.create()
                .withPublicKey(armoredPublicKey)
                .encryptArmored(plaintext);
    }

    /**
     * Encrypts a message using the key pair's public key.
     * 使用密钥对的公钥加密消息。
     *
     * @param plaintext the message to encrypt
     * @param keyPair   the key pair (uses public key)
     * @return armored encrypted message
     */
    public static String encrypt(String plaintext, PgpKeyPair keyPair) {
        Objects.requireNonNull(plaintext, "plaintext must not be null");
        Objects.requireNonNull(keyPair, "keyPair must not be null");

        return encrypt(plaintext, keyPair.publicKey());
    }

    /**
     * Encrypts binary data using the public key.
     * 使用公钥加密二进制数据。
     *
     * @param data      the data to encrypt
     * @param publicKey the recipient's public key
     * @return encrypted bytes
     */
    public static byte[] encrypt(byte[] data, PGPPublicKey publicKey) {
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(publicKey, "publicKey must not be null");

        return PgpCipher.create()
                .withPublicKey(publicKey)
                .encrypt(data);
    }

    /**
     * Encrypts binary data and returns armored ASCII.
     * 加密二进制数据并返回装甲 ASCII。
     *
     * @param data      the data to encrypt
     * @param publicKey the recipient's public key
     * @return armored encrypted data
     */
    public static String encryptArmored(byte[] data, PGPPublicKey publicKey) {
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(publicKey, "publicKey must not be null");

        return PgpCipher.create()
                .withPublicKey(publicKey)
                .encryptArmored(data);
    }

    // ==================== Decryption ====================

    /**
     * Decrypts an armored message using the secret key.
     * 使用私钥解密装甲消息。
     *
     * @param armoredMessage the armored encrypted message
     * @param secretKey      the recipient's secret key
     * @param passphrase     the passphrase for the secret key
     * @return decrypted message
     */
    public static String decrypt(String armoredMessage, PGPSecretKey secretKey, String passphrase) {
        Objects.requireNonNull(armoredMessage, "armoredMessage must not be null");
        Objects.requireNonNull(secretKey, "secretKey must not be null");
        Objects.requireNonNull(passphrase, "passphrase must not be null");

        return PgpCipher.create()
                .withSecretKey(secretKey, passphrase)
                .decryptArmored(armoredMessage);
    }

    /**
     * Decrypts an armored message using the armored secret key.
     * 使用装甲格式的私钥解密消息。
     *
     * @param armoredMessage   the armored encrypted message
     * @param armoredSecretKey the recipient's armored secret key
     * @param passphrase       the passphrase for the secret key
     * @return decrypted message
     */
    public static String decrypt(String armoredMessage, String armoredSecretKey, String passphrase) {
        Objects.requireNonNull(armoredMessage, "armoredMessage must not be null");
        Objects.requireNonNull(armoredSecretKey, "armoredSecretKey must not be null");
        Objects.requireNonNull(passphrase, "passphrase must not be null");

        return PgpCipher.create()
                .withSecretKey(armoredSecretKey, passphrase)
                .decryptArmored(armoredMessage);
    }

    /**
     * Decrypts an armored message using the key pair.
     * 使用密钥对解密装甲消息。
     *
     * @param armoredMessage the armored encrypted message
     * @param keyPair        the key pair
     * @param passphrase     the passphrase for the secret key
     * @return decrypted message
     */
    public static String decrypt(String armoredMessage, PgpKeyPair keyPair, String passphrase) {
        Objects.requireNonNull(armoredMessage, "armoredMessage must not be null");
        Objects.requireNonNull(keyPair, "keyPair must not be null");
        Objects.requireNonNull(passphrase, "passphrase must not be null");

        return PgpCipher.create()
                .withKeyPair(keyPair, passphrase)
                .decryptArmored(armoredMessage);
    }

    /**
     * Decrypts binary data using the secret key.
     * 使用私钥解密二进制数据。
     *
     * @param encryptedData the encrypted data
     * @param secretKey     the recipient's secret key
     * @param passphrase    the passphrase for the secret key
     * @return decrypted bytes
     */
    public static byte[] decrypt(byte[] encryptedData, PGPSecretKey secretKey, String passphrase) {
        Objects.requireNonNull(encryptedData, "encryptedData must not be null");
        Objects.requireNonNull(secretKey, "secretKey must not be null");
        Objects.requireNonNull(passphrase, "passphrase must not be null");

        return PgpCipher.create()
                .withSecretKey(secretKey, passphrase)
                .decrypt(encryptedData);
    }

    // ==================== Key Import/Export ====================

    /**
     * Exports the public key to armored ASCII format.
     * 将公钥导出为装甲 ASCII 格式。
     *
     * @param keyPair the key pair
     * @return armored public key
     */
    public static String exportPublicKey(PgpKeyPair keyPair) {
        Objects.requireNonNull(keyPair, "keyPair must not be null");
        return PgpKeyUtil.exportPublicKey(keyPair.publicKey());
    }

    /**
     * Exports the public key to armored ASCII format.
     * 将公钥导出为装甲 ASCII 格式。
     *
     * @param publicKey the public key
     * @return armored public key
     */
    public static String exportPublicKey(PGPPublicKey publicKey) {
        return PgpKeyUtil.exportPublicKey(publicKey);
    }

    /**
     * Exports the secret key to armored ASCII format.
     * 将私钥导出为装甲 ASCII 格式。
     *
     * @param keyPair the key pair
     * @return armored secret key
     */
    public static String exportSecretKey(PgpKeyPair keyPair) {
        Objects.requireNonNull(keyPair, "keyPair must not be null");
        return PgpKeyUtil.exportSecretKey(keyPair.secretKey());
    }

    /**
     * Exports the secret key to armored ASCII format.
     * 将私钥导出为装甲 ASCII 格式。
     *
     * @param secretKey the secret key
     * @return armored secret key
     */
    public static String exportSecretKey(PGPSecretKey secretKey) {
        return PgpKeyUtil.exportSecretKey(secretKey);
    }

    /**
     * Imports a public key from armored ASCII format.
     * 从装甲 ASCII 格式导入公钥。
     *
     * @param armoredKey the armored public key
     * @return the PGP public key
     */
    public static PGPPublicKey importPublicKey(String armoredKey) {
        return PgpKeyUtil.importPublicKey(armoredKey);
    }

    /**
     * Imports a key pair from armored ASCII format.
     * 从装甲 ASCII 格式导入密钥对。
     *
     * @param armoredSecretKey the armored secret key
     * @param passphrase       the passphrase
     * @return the PGP key pair
     */
    public static PgpKeyPair importKeyPair(String armoredSecretKey, String passphrase) {
        return PgpKeyUtil.importKeyPair(armoredSecretKey, passphrase);
    }

    // ==================== Key Information ====================

    /**
     * Returns the key ID in hexadecimal format.
     * 返回十六进制格式的密钥 ID。
     *
     * @param publicKey the public key
     * @return the key ID in hex format
     */
    public static String keyIdHex(PGPPublicKey publicKey) {
        return PgpKeyUtil.keyIdHex(publicKey);
    }

    /**
     * Returns the key fingerprint in hexadecimal format.
     * 返回十六进制格式的密钥指纹。
     *
     * @param publicKey the public key
     * @return the fingerprint in hex format
     */
    public static String fingerprintHex(PGPPublicKey publicKey) {
        return PgpKeyUtil.fingerprintHex(publicKey);
    }

    // ==================== Cipher Builder ====================

    /**
     * Creates a new PGP cipher builder for advanced configuration.
     * 创建新的 PGP 加密器构建器用于高级配置。
     *
     * @return a new PgpCipher instance
     */
    public static PgpCipher cipher() {
        return PgpCipher.create();
    }
}
