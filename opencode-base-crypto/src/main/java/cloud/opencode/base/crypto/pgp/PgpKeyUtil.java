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

package cloud.opencode.base.crypto.pgp;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;

/**
 * PGP Key Utility - Manages PGP key generation, import, and export
 * PGP 密钥工具 - 管理 PGP 密钥生成、导入和导出
 *
 * <p>Provides utility methods for:</p>
 * <p>提供以下功能的工具方法：</p>
 * <ul>
 *   <li>Key pair generation (RSA, ECDSA, EdDSA) - 密钥对生成</li>
 *   <li>Key import from armored ASCII - 从装甲 ASCII 导入密钥</li>
 *   <li>Key export to armored ASCII - 导出密钥为装甲 ASCII</li>
 *   <li>Key ring management - 密钥环管理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Generate a new key pair
 * PgpKeyPair keyPair = PgpKeyUtil.generateKeyPair("user@example.com", "passphrase");
 *
 * // Export public key
 * String armoredPublicKey = PgpKeyUtil.exportPublicKey(keyPair.publicKey());
 *
 * // Import public key
 * PGPPublicKey publicKey = PgpKeyUtil.importPublicKey(armoredPublicKey);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>See class description for details - 详见类描述</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(k^3) for key generation - 时间复杂度: O(k^3)，k为密钥大小</li>
 *   <li>Space complexity: O(k) - 空间复杂度: O(k)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.2.0
 */
public final class PgpKeyUtil {

    private static final String ALGORITHM = "PGP";
    private static final String PROVIDER = "BC";

    static {
        if (Security.getProvider(PROVIDER) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private PgpKeyUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // ==================== Key Generation ====================

    /**
     * Generates a new PGP key pair with RSA algorithm.
     * 使用 RSA 算法生成新的 PGP 密钥对。
     *
     * @param userId     the user ID (typically email address)
     * @param passphrase the passphrase to protect the secret key
     * @return the generated PGP key pair
     * @throws OpenKeyException if key generation fails
     */
    public static PgpKeyPair generateKeyPair(String userId, String passphrase) {
        return generateKeyPair(userId, passphrase, PgpAlgorithm.DEFAULT_RSA_KEY_SIZE);
    }

    /**
     * Generates a new PGP key pair with RSA algorithm and specified key size.
     * 使用 RSA 算法和指定密钥大小生成新的 PGP 密钥对。
     *
     * @param userId     the user ID (typically email address)
     * @param passphrase the passphrase to protect the secret key
     * @param keySize    the RSA key size in bits (minimum 2048)
     * @return the generated PGP key pair
     * @throws OpenKeyException if key generation fails
     */
    public static PgpKeyPair generateKeyPair(String userId, String passphrase, int keySize) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(passphrase, "passphrase must not be null");

        if (keySize < PgpAlgorithm.MIN_RSA_KEY_SIZE) {
            throw new IllegalArgumentException(
                    "Key size must be at least " + PgpAlgorithm.MIN_RSA_KEY_SIZE + " bits");
        }

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", PROVIDER);
            keyGen.initialize(keySize);
            KeyPair keyPair = keyGen.generateKeyPair();

            PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder()
                    .build()
                    .get(HashAlgorithmTags.SHA1);

            PGPKeyPair pgpKeyPair = new JcaPGPKeyPair(
                    PgpAlgorithm.PublicKey.RSA_GENERAL.tag(),
                    keyPair,
                    new Date()
            );

            PGPSecretKey secretKey = new PGPSecretKey(
                    PGPSignature.DEFAULT_CERTIFICATION,
                    pgpKeyPair,
                    userId,
                    sha1Calc,
                    null,
                    null,
                    new JcaPGPContentSignerBuilder(
                            pgpKeyPair.getPublicKey().getAlgorithm(),
                            HashAlgorithmTags.SHA256
                    ),
                    new JcePBESecretKeyEncryptorBuilder(
                            PgpAlgorithm.DEFAULT_SYMMETRIC.tag(),
                            sha1Calc
                    ).setProvider(PROVIDER).build(passphrase.toCharArray())
            );

            return PgpKeyPair.fromSecretKey(secretKey, userId);

        } catch (Exception e) {
            throw new OpenKeyException(ALGORITHM,
                    "Failed to generate PGP key pair", e);
        }
    }

    // ==================== Key Import ====================

    /**
     * Imports a PGP public key from armored ASCII format.
     * 从装甲 ASCII 格式导入 PGP 公钥。
     *
     * @param armoredKey the armored public key string
     * @return the PGP public key
     * @throws OpenKeyException if import fails
     */
    public static PGPPublicKey importPublicKey(String armoredKey) {
        Objects.requireNonNull(armoredKey, "armoredKey must not be null");

        try (InputStream in = PGPUtil.getDecoderStream(
                new ByteArrayInputStream(armoredKey.getBytes(StandardCharsets.UTF_8)))) {

            PGPPublicKeyRingCollection keyRingCollection =
                    new PGPPublicKeyRingCollection(in, new JcaKeyFingerprintCalculator());

            Iterator<PGPPublicKeyRing> keyRings = keyRingCollection.getKeyRings();
            while (keyRings.hasNext()) {
                PGPPublicKeyRing keyRing = keyRings.next();
                Iterator<PGPPublicKey> keys = keyRing.getPublicKeys();
                while (keys.hasNext()) {
                    PGPPublicKey key = keys.next();
                    if (key.isEncryptionKey()) {
                        return key;
                    }
                }
            }

            throw new OpenKeyException(ALGORITHM, "No encryption key found in key ring");

        } catch (OpenKeyException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenKeyException(ALGORITHM,
                    "Failed to import public key", e);
        }
    }

    /**
     * Imports a PGP public key from byte array.
     * 从字节数组导入 PGP 公钥。
     *
     * @param keyData the public key bytes
     * @return the PGP public key
     * @throws OpenKeyException if import fails
     */
    public static PGPPublicKey importPublicKey(byte[] keyData) {
        Objects.requireNonNull(keyData, "keyData must not be null");
        return importPublicKey(new String(keyData, StandardCharsets.UTF_8));
    }

    /**
     * Imports a PGP secret key from armored ASCII format.
     * 从装甲 ASCII 格式导入 PGP 私钥。
     *
     * @param armoredKey the armored secret key string
     * @param passphrase the passphrase to decrypt the key
     * @return the PGP secret key
     * @throws OpenKeyException if import fails
     */
    public static PGPSecretKey importSecretKey(String armoredKey, String passphrase) {
        Objects.requireNonNull(armoredKey, "armoredKey must not be null");
        Objects.requireNonNull(passphrase, "passphrase must not be null");

        try (InputStream in = PGPUtil.getDecoderStream(
                new ByteArrayInputStream(armoredKey.getBytes(StandardCharsets.UTF_8)))) {

            PGPSecretKeyRingCollection keyRingCollection =
                    new PGPSecretKeyRingCollection(in, new JcaKeyFingerprintCalculator());

            Iterator<PGPSecretKeyRing> keyRings = keyRingCollection.getKeyRings();
            while (keyRings.hasNext()) {
                PGPSecretKeyRing keyRing = keyRings.next();
                Iterator<PGPSecretKey> keys = keyRing.getSecretKeys();
                while (keys.hasNext()) {
                    PGPSecretKey key = keys.next();
                    if (key.isSigningKey()) {
                        // Verify passphrase by attempting to extract private key
                        verifyPassphrase(key, passphrase);
                        return key;
                    }
                }
            }

            throw new OpenKeyException(ALGORITHM, "No signing key found in key ring");

        } catch (OpenKeyException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenKeyException(ALGORITHM,
                    "Failed to import secret key", e);
        }
    }

    /**
     * Imports a PGP key pair from armored ASCII format.
     * 从装甲 ASCII 格式导入 PGP 密钥对。
     *
     * @param armoredSecretKey the armored secret key string
     * @param passphrase       the passphrase to decrypt the key
     * @return the PGP key pair
     * @throws OpenKeyException if import fails
     */
    public static PgpKeyPair importKeyPair(String armoredSecretKey, String passphrase) {
        PGPSecretKey secretKey = importSecretKey(armoredSecretKey, passphrase);
        String userId = extractUserId(secretKey);
        return PgpKeyPair.fromSecretKey(secretKey, userId);
    }

    // ==================== Key Export ====================

    /**
     * Exports a PGP public key to armored ASCII format.
     * 将 PGP 公钥导出为装甲 ASCII 格式。
     *
     * @param publicKey the public key to export
     * @return the armored public key string
     * @throws OpenKeyException if export fails
     */
    public static String exportPublicKey(PGPPublicKey publicKey) {
        Objects.requireNonNull(publicKey, "publicKey must not be null");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ArmoredOutputStream armoredOut = new ArmoredOutputStream(out)) {

            publicKey.encode(armoredOut);
            armoredOut.close();
            return out.toString(StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new OpenKeyException(ALGORITHM,
                    "Failed to export public key", e);
        }
    }

    /**
     * Exports a PGP secret key to armored ASCII format.
     * 将 PGP 私钥导出为装甲 ASCII 格式。
     *
     * @param secretKey the secret key to export
     * @return the armored secret key string
     * @throws OpenKeyException if export fails
     */
    public static String exportSecretKey(PGPSecretKey secretKey) {
        Objects.requireNonNull(secretKey, "secretKey must not be null");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ArmoredOutputStream armoredOut = new ArmoredOutputStream(out)) {

            secretKey.encode(armoredOut);
            armoredOut.close();
            return out.toString(StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new OpenKeyException(ALGORITHM,
                    "Failed to export secret key", e);
        }
    }

    /**
     * Exports a PGP public key to raw bytes.
     * 将 PGP 公钥导出为原始字节。
     *
     * @param publicKey the public key to export
     * @return the public key bytes
     * @throws OpenKeyException if export fails
     */
    public static byte[] exportPublicKeyBytes(PGPPublicKey publicKey) {
        Objects.requireNonNull(publicKey, "publicKey must not be null");

        try {
            return publicKey.getEncoded();
        } catch (Exception e) {
            throw new OpenKeyException(ALGORITHM,
                    "Failed to export public key bytes", e);
        }
    }

    // ==================== Key Information ====================

    /**
     * Extracts the private key from a PGP secret key.
     * 从 PGP 私钥中提取私钥。
     *
     * @param secretKey  the PGP secret key
     * @param passphrase the passphrase
     * @return the extracted private key
     * @throws OpenKeyException if extraction fails
     */
    public static PGPPrivateKey extractPrivateKey(PGPSecretKey secretKey, String passphrase) {
        Objects.requireNonNull(secretKey, "secretKey must not be null");
        Objects.requireNonNull(passphrase, "passphrase must not be null");

        try {
            PBESecretKeyDecryptor decryptor = new JcePBESecretKeyDecryptorBuilder()
                    .setProvider(PROVIDER)
                    .build(passphrase.toCharArray());
            return secretKey.extractPrivateKey(decryptor);
        } catch (PGPException e) {
            throw new OpenKeyException(ALGORITHM,
                    "Failed to extract private key - incorrect passphrase?", e);
        }
    }

    /**
     * Returns the key ID as hexadecimal string.
     * 返回十六进制格式的密钥 ID。
     *
     * @param key the PGP public key
     * @return the key ID in hex format
     */
    public static String keyIdHex(PGPPublicKey key) {
        Objects.requireNonNull(key, "key must not be null");
        return Long.toHexString(key.getKeyID()).toUpperCase();
    }

    /**
     * Returns the key fingerprint as hexadecimal string.
     * 返回十六进制格式的密钥指纹。
     *
     * @param key the PGP public key
     * @return the fingerprint in hex format
     */
    public static String fingerprintHex(PGPPublicKey key) {
        Objects.requireNonNull(key, "key must not be null");
        byte[] fingerprint = key.getFingerprint();
        StringBuilder hex = new StringBuilder();
        for (byte b : fingerprint) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    // ==================== Private Helpers ====================

    private static void verifyPassphrase(PGPSecretKey secretKey, String passphrase) {
        try {
            PBESecretKeyDecryptor decryptor = new JcePBESecretKeyDecryptorBuilder()
                    .setProvider(PROVIDER)
                    .build(passphrase.toCharArray());
            secretKey.extractPrivateKey(decryptor);
        } catch (PGPException e) {
            throw new OpenKeyException(ALGORITHM,
                    "Invalid passphrase for secret key", e);
        }
    }

    private static String extractUserId(PGPSecretKey secretKey) {
        Iterator<String> userIds = secretKey.getUserIDs();
        if (userIds.hasNext()) {
            return userIds.next();
        }
        return "unknown";
    }
}
