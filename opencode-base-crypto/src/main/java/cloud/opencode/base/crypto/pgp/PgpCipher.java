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
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.*;
import org.bouncycastle.util.io.Streams;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;

/**
 * PGP Cipher - Provides PGP encryption and decryption operations
 * PGP 加密器 - 提供 PGP 加密和解密操作
 *
 * <p>This class provides fluent API for PGP encryption and decryption,
 * commonly used in email security scenarios.</p>
 * <p>此类提供 PGP 加密和解密的流式 API，常用于电子邮件安全场景。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Public key encryption - 公钥加密</li>
 *   <li>Private key decryption - 私钥解密</li>
 *   <li>Armored ASCII output - 装甲 ASCII 输出</li>
 *   <li>Compression support - 压缩支持</li>
 *   <li>Integrity check - 完整性检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create cipher instance
 * PgpCipher cipher = PgpCipher.create();
 *
 * // Encrypt with public key
 * String encrypted = cipher
 *     .withPublicKey(publicKey)
 *     .encryptArmored("Hello, World!");
 *
 * // Decrypt with private key
 * String decrypted = cipher
 *     .withSecretKey(secretKey, "passphrase")
 *     .decryptArmored(encrypted);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for data, O(k^3) for key operations - 时间复杂度: O(n) 数据处理，O(k^3) 密钥操作</li>
 *   <li>Space complexity: O(n) - 空间复杂度: O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.2.0
 */
public final class PgpCipher {

    private static final String ALGORITHM = "PGP";
    private static final String PROVIDER = "BC";
    private static final int BUFFER_SIZE = 65536;

    static {
        if (Security.getProvider(PROVIDER) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private PGPPublicKey publicKey;
    private PGPSecretKey secretKey;
    private PGPPrivateKey privateKey;
    private PgpAlgorithm.Symmetric symmetricAlgorithm = PgpAlgorithm.DEFAULT_SYMMETRIC;
    private boolean withIntegrityCheck = true;
    private boolean withCompression = true;
    private int compressionAlgorithm = PGPCompressedData.ZIP;

    private PgpCipher() {
    }

    /**
     * Creates a new PGP cipher instance.
     * 创建新的 PGP 加密器实例。
     *
     * @return a new PgpCipher instance
     */
    public static PgpCipher create() {
        return new PgpCipher();
    }

    // ==================== Configuration ====================

    /**
     * Sets the public key for encryption.
     * 设置用于加密的公钥。
     *
     * @param publicKey the PGP public key
     * @return this cipher instance
     */
    public PgpCipher withPublicKey(PGPPublicKey publicKey) {
        this.publicKey = Objects.requireNonNull(publicKey, "publicKey must not be null");
        return this;
    }

    /**
     * Sets the public key from armored ASCII string.
     * 从装甲 ASCII 字符串设置公钥。
     *
     * @param armoredKey the armored public key
     * @return this cipher instance
     */
    public PgpCipher withPublicKey(String armoredKey) {
        this.publicKey = PgpKeyUtil.importPublicKey(armoredKey);
        return this;
    }

    /**
     * Sets the secret key for decryption.
     * 设置用于解密的私钥。
     *
     * @param secretKey  the PGP secret key
     * @param passphrase the passphrase to decrypt the key
     * @return this cipher instance
     */
    public PgpCipher withSecretKey(PGPSecretKey secretKey, String passphrase) {
        this.secretKey = Objects.requireNonNull(secretKey, "secretKey must not be null");
        this.privateKey = PgpKeyUtil.extractPrivateKey(secretKey, passphrase);
        return this;
    }

    /**
     * Sets the secret key from armored ASCII string.
     * 从装甲 ASCII 字符串设置私钥。
     *
     * @param armoredKey the armored secret key
     * @param passphrase the passphrase
     * @return this cipher instance
     */
    public PgpCipher withSecretKey(String armoredKey, String passphrase) {
        this.secretKey = PgpKeyUtil.importSecretKey(armoredKey, passphrase);
        this.privateKey = PgpKeyUtil.extractPrivateKey(secretKey, passphrase);
        return this;
    }

    /**
     * Sets the key pair for both encryption and decryption.
     * 设置用于加密和解密的密钥对。
     *
     * @param keyPair    the PGP key pair
     * @param passphrase the passphrase
     * @return this cipher instance
     */
    public PgpCipher withKeyPair(PgpKeyPair keyPair, String passphrase) {
        Objects.requireNonNull(keyPair, "keyPair must not be null");
        this.publicKey = keyPair.publicKey();
        this.secretKey = keyPair.secretKey();
        this.privateKey = PgpKeyUtil.extractPrivateKey(secretKey, passphrase);
        return this;
    }

    /**
     * Sets the symmetric algorithm for encryption.
     * 设置用于加密的对称算法。
     *
     * @param algorithm the symmetric algorithm
     * @return this cipher instance
     */
    public PgpCipher withSymmetricAlgorithm(PgpAlgorithm.Symmetric algorithm) {
        this.symmetricAlgorithm = Objects.requireNonNull(algorithm, "algorithm must not be null");
        return this;
    }

    /**
     * Enables or disables integrity check.
     * 启用或禁用完整性检查。
     *
     * @param enabled true to enable integrity check
     * @return this cipher instance
     */
    public PgpCipher withIntegrityCheck(boolean enabled) {
        this.withIntegrityCheck = enabled;
        return this;
    }

    /**
     * Enables or disables compression.
     * 启用或禁用压缩。
     *
     * @param enabled true to enable compression
     * @return this cipher instance
     */
    public PgpCipher withCompression(boolean enabled) {
        this.withCompression = enabled;
        return this;
    }

    // ==================== Encryption ====================

    /**
     * Encrypts data and returns armored ASCII string.
     * 加密数据并返回装甲 ASCII 字符串。
     *
     * @param plaintext the data to encrypt
     * @return armored encrypted data
     * @throws OpenCryptoException if encryption fails
     */
    public String encryptArmored(String plaintext) {
        Objects.requireNonNull(plaintext, "plaintext must not be null");
        return encryptArmored(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encrypts data and returns armored ASCII string.
     * 加密数据并返回装甲 ASCII 字符串。
     *
     * @param data the data to encrypt
     * @return armored encrypted data
     * @throws OpenCryptoException if encryption fails
     */
    public String encryptArmored(byte[] data) {
        Objects.requireNonNull(data, "data must not be null");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (ArmoredOutputStream armoredOut = new ArmoredOutputStream(out)) {
                encrypt(data, armoredOut);
            }
            return out.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new OpenCryptoException(ALGORITHM, "encryption",
                    "Failed to encrypt data", e);
        }
    }

    /**
     * Encrypts data and returns raw bytes.
     * 加密数据并返回原始字节。
     *
     * @param data the data to encrypt
     * @return encrypted bytes
     * @throws OpenCryptoException if encryption fails
     */
    public byte[] encrypt(byte[] data) {
        Objects.requireNonNull(data, "data must not be null");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            encrypt(data, out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new OpenCryptoException(ALGORITHM, "encryption",
                    "Failed to encrypt data", e);
        }
    }

    /**
     * Encrypts data and returns Base64 encoded string.
     * 加密数据并返回 Base64 编码字符串。
     *
     * @param data the data to encrypt
     * @return Base64 encoded encrypted data
     * @throws OpenCryptoException if encryption fails
     */
    public String encryptBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(encrypt(data));
    }

    private void encrypt(byte[] data, OutputStream out) throws Exception {
        if (publicKey == null) {
            throw new IllegalStateException("Public key must be set for encryption");
        }

        PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
                new JcePGPDataEncryptorBuilder(symmetricAlgorithm.tag())
                        .setWithIntegrityPacket(withIntegrityCheck)
                        .setSecureRandom(new SecureRandom())
                        .setProvider(PROVIDER)
        );

        encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(publicKey)
                .setProvider(PROVIDER));

        try (OutputStream encOut = encGen.open(out, new byte[BUFFER_SIZE])) {
            if (withCompression) {
                PGPCompressedDataGenerator compGen =
                        new PGPCompressedDataGenerator(compressionAlgorithm);
                try (OutputStream compOut = compGen.open(encOut, new byte[BUFFER_SIZE])) {
                    writeLiteralData(compOut, data);
                }
            } else {
                writeLiteralData(encOut, data);
            }
        }
    }

    private void writeLiteralData(OutputStream out, byte[] data) throws Exception {
        PGPLiteralDataGenerator litGen = new PGPLiteralDataGenerator();
        try (OutputStream litOut = litGen.open(out, PGPLiteralData.BINARY,
                PGPLiteralData.CONSOLE, data.length, new Date())) {
            litOut.write(data);
        }
    }

    // ==================== Decryption ====================

    /**
     * Decrypts armored ASCII data.
     * 解密装甲 ASCII 数据。
     *
     * @param armoredData the armored encrypted data
     * @return decrypted string
     * @throws OpenCryptoException if decryption fails
     */
    public String decryptArmored(String armoredData) {
        Objects.requireNonNull(armoredData, "armoredData must not be null");
        return new String(decryptArmoredToBytes(armoredData), StandardCharsets.UTF_8);
    }

    /**
     * Decrypts armored ASCII data to bytes.
     * 解密装甲 ASCII 数据为字节。
     *
     * @param armoredData the armored encrypted data
     * @return decrypted bytes
     * @throws OpenCryptoException if decryption fails
     */
    public byte[] decryptArmoredToBytes(String armoredData) {
        Objects.requireNonNull(armoredData, "armoredData must not be null");

        try (InputStream in = PGPUtil.getDecoderStream(
                new ByteArrayInputStream(armoredData.getBytes(StandardCharsets.UTF_8)))) {
            return decrypt(in);
        } catch (OpenCryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenCryptoException(ALGORITHM, "decryption",
                    "Failed to decrypt data", e);
        }
    }

    /**
     * Decrypts raw encrypted bytes.
     * 解密原始加密字节。
     *
     * @param encryptedData the encrypted data
     * @return decrypted bytes
     * @throws OpenCryptoException if decryption fails
     */
    public byte[] decrypt(byte[] encryptedData) {
        Objects.requireNonNull(encryptedData, "encryptedData must not be null");

        try (InputStream in = new ByteArrayInputStream(encryptedData)) {
            return decrypt(in);
        } catch (OpenCryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenCryptoException(ALGORITHM, "decryption",
                    "Failed to decrypt data", e);
        }
    }

    /**
     * Decrypts Base64 encoded data.
     * 解密 Base64 编码数据。
     *
     * @param base64Data the Base64 encoded encrypted data
     * @return decrypted bytes
     * @throws OpenCryptoException if decryption fails
     */
    public byte[] decryptBase64(String base64Data) {
        Objects.requireNonNull(base64Data, "base64Data must not be null");
        return decrypt(Base64.getDecoder().decode(base64Data));
    }

    /**
     * Decrypts Base64 encoded data to string.
     * 解密 Base64 编码数据为字符串。
     *
     * @param base64Data the Base64 encoded encrypted data
     * @return decrypted string
     * @throws OpenCryptoException if decryption fails
     */
    public String decryptBase64ToString(String base64Data) {
        return new String(decryptBase64(base64Data), StandardCharsets.UTF_8);
    }

    private byte[] decrypt(InputStream in) throws Exception {
        if (privateKey == null) {
            throw new IllegalStateException("Secret key must be set for decryption");
        }

        PGPObjectFactory pgpF = new PGPObjectFactory(in, new JcaKeyFingerprintCalculator());
        Object o = pgpF.nextObject();

        PGPEncryptedDataList encList;
        if (o instanceof PGPEncryptedDataList list) {
            encList = list;
        } else {
            encList = (PGPEncryptedDataList) pgpF.nextObject();
        }

        Iterator<PGPEncryptedData> it = encList.getEncryptedDataObjects();
        PGPPublicKeyEncryptedData pbe = null;

        while (it.hasNext()) {
            PGPEncryptedData encData = it.next();
            if (encData instanceof PGPPublicKeyEncryptedData pkEncData) {
                if (pkEncData.getKeyID() == secretKey.getKeyID() ||
                        pkEncData.getKeyID() == publicKey.getKeyID()) {
                    pbe = pkEncData;
                    break;
                }
            }
        }

        if (pbe == null) {
            throw new OpenCryptoException(ALGORITHM, "decryption",
                    "No matching key found for decryption");
        }

        try (InputStream clear = pbe.getDataStream(
                new JcePublicKeyDataDecryptorFactoryBuilder()
                        .setProvider(PROVIDER)
                        .build(privateKey))) {

            PGPObjectFactory plainFact = new PGPObjectFactory(clear, new JcaKeyFingerprintCalculator());
            Object message = plainFact.nextObject();

            if (message instanceof PGPCompressedData compressedData) {
                PGPObjectFactory pgpFact = new PGPObjectFactory(
                        compressedData.getDataStream(), new JcaKeyFingerprintCalculator());
                message = pgpFact.nextObject();
            }

            if (message instanceof PGPLiteralData ld) {
                try (InputStream unc = ld.getInputStream();
                     ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    Streams.pipeAll(unc, out);

                    if (pbe.isIntegrityProtected() && !pbe.verify()) {
                        throw new OpenCryptoException(ALGORITHM, "decryption",
                                "Integrity check failed");
                    }

                    return out.toByteArray();
                }
            }

            throw new OpenCryptoException(ALGORITHM, "decryption",
                    "Unknown message type: " + message.getClass().getName());
        }
    }

    // ==================== Getters ====================

    /**
     * Returns the configured public key.
     * 返回配置的公钥。
     *
     * @return the public key, or null if not set
     */
    public PGPPublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Returns the configured secret key.
     * 返回配置的私钥。
     *
     * @return the secret key, or null if not set
     */
    public PGPSecretKey getSecretKey() {
        return secretKey;
    }

    /**
     * Returns the symmetric algorithm.
     * 返回对称算法。
     *
     * @return the symmetric algorithm
     */
    public PgpAlgorithm.Symmetric getSymmetricAlgorithm() {
        return symmetricAlgorithm;
    }
}
