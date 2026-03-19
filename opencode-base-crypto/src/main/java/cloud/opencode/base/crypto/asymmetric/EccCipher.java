package cloud.opencode.base.crypto.asymmetric;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.codec.PemCodec;
import cloud.opencode.base.crypto.enums.CurveType;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;

/**
 * ECC cipher implementation using ECIES (Elliptic Curve Integrated Encryption Scheme)
 * ECC 密码实现（椭圆曲线集成加密方案）
 * <p>
 * This implementation uses ECDH for key agreement combined with AES-256-GCM for encryption.
 * The scheme is: ECDH(ephemeral, recipient_public) -> KDF -> AES-GCM(derived_key, plaintext)
 * 此实现使用 ECDH 密钥协商结合 AES-256-GCM 加密。
 * 方案为：ECDH(临时密钥, 接收方公钥) -> KDF -> AES-GCM(派生密钥, 明文)
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ECIES encryption using elliptic curves - 使用椭圆曲线的 ECIES 加密</li>
 *   <li>P-256, P-384, P-521 curve support - P-256、P-384、P-521 曲线支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EccCipher ecc = EccCipher.p256();
 * ecc.setPublicKey(publicKey);
 * byte[] encrypted = ecc.encrypt(data);
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
 *   <li>Time complexity: O(k^3) for key gen, O(n) for encrypt - 时间复杂度: O(k^3) 密钥生成，O(n) 加密</li>
 *   <li>Space complexity: O(k) - 空间复杂度: O(k)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class EccCipher implements AsymmetricCipher {

    private static final String KEY_ALGORITHM = "EC";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_AGREEMENT_ALGORITHM = "ECDH";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_SIZE = 12; // 96 bits
    private static final int GCM_TAG_SIZE = 128; // 128 bits

    private final CurveType curveType;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    /**
     * Private constructor
     */
    private EccCipher(CurveType curveType) {
        this.curveType = curveType;
    }

    /**
     * Create ECC cipher with P-256 curve (recommended)
     * 创建使用 P-256 曲线的 ECC 密码（推荐）
     *
     * @return new ECC cipher with P-256
     */
    public static EccCipher p256() {
        return new EccCipher(CurveType.P_256);
    }

    /**
     * Create ECC cipher with P-384 curve
     * 创建使用 P-384 曲线的 ECC 密码
     *
     * @return new ECC cipher with P-384
     */
    public static EccCipher p384() {
        return new EccCipher(CurveType.P_384);
    }

    /**
     * Create ECC cipher with P-521 curve
     * 创建使用 P-521 曲线的 ECC 密码
     *
     * @return new ECC cipher with P-521
     */
    public static EccCipher p521() {
        return new EccCipher(CurveType.P_521);
    }

    /**
     * Create ECC cipher with specified curve
     * 创建使用指定曲线的 ECC 密码
     *
     * @param curve the elliptic curve type
     * @return new ECC cipher
     */
    public static EccCipher withCurve(CurveType curve) {
        if (curve == null) {
            throw new NullPointerException("Curve type cannot be null");
        }
        return new EccCipher(curve);
    }

    /**
     * Create ECC cipher with generated key pair
     * 创建带生成密钥对的 ECC 密码
     *
     * @param curve the elliptic curve type
     * @return ECC cipher with generated keys
     */
    public static EccCipher withGeneratedKeyPair(CurveType curve) {
        if (curve == null) {
            throw new NullPointerException("Curve type cannot be null");
        }

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            ECGenParameterSpec ecSpec = new ECGenParameterSpec(curve.getCurveName());
            generator.initialize(ecSpec, new SecureRandom());
            KeyPair keyPair = generator.generateKeyPair();

            EccCipher cipher = new EccCipher(curve);
            cipher.publicKey = keyPair.getPublic();
            cipher.privateKey = keyPair.getPrivate();
            return cipher;
        } catch (NoSuchAlgorithmException e) {
            throw OpenCryptoException.algorithmNotAvailable(KEY_ALGORITHM);
        } catch (InvalidAlgorithmParameterException e) {
            throw new OpenCryptoException("Invalid curve parameter: " + curve.getCurveName(), e);
        }
    }

    @Override
    public AsymmetricCipher setPublicKey(PublicKey publicKey) {
        if (publicKey == null) {
            throw new NullPointerException("Public key cannot be null");
        }
        if (!KEY_ALGORITHM.equals(publicKey.getAlgorithm())) {
            throw new IllegalArgumentException("Key must be EC key");
        }
        this.publicKey = publicKey;
        return this;
    }

    @Override
    public AsymmetricCipher setPublicKey(byte[] encodedKey) {
        if (encodedKey == null) {
            throw new NullPointerException("Encoded key cannot be null");
        }
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
            this.publicKey = keyFactory.generatePublic(keySpec);
            return this;
        } catch (Exception e) {
            throw new OpenKeyException("Failed to decode public key", e);
        }
    }

    @Override
    public AsymmetricCipher setPublicKeyPem(String pem) {
        if (pem == null) {
            throw new NullPointerException("PEM string cannot be null");
        }
        try {
            byte[] encoded = PemCodec.decodePublicKey(pem);
            return setPublicKey(encoded);
        } catch (Exception e) {
            throw new OpenKeyException("Failed to parse PEM public key", e);
        }
    }

    @Override
    public AsymmetricCipher setPrivateKey(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new NullPointerException("Private key cannot be null");
        }
        if (!KEY_ALGORITHM.equals(privateKey.getAlgorithm())) {
            throw new IllegalArgumentException("Key must be EC key");
        }
        this.privateKey = privateKey;
        return this;
    }

    @Override
    public AsymmetricCipher setPrivateKey(byte[] encodedKey) {
        if (encodedKey == null) {
            throw new NullPointerException("Encoded key cannot be null");
        }
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
            this.privateKey = keyFactory.generatePrivate(keySpec);
            return this;
        } catch (Exception e) {
            throw new OpenKeyException("Failed to decode private key", e);
        }
    }

    @Override
    public AsymmetricCipher setPrivateKeyPem(String pem) {
        if (pem == null) {
            throw new NullPointerException("PEM string cannot be null");
        }
        try {
            byte[] encoded = PemCodec.decodePrivateKey(pem);
            return setPrivateKey(encoded);
        } catch (Exception e) {
            throw new OpenKeyException("Failed to parse PEM private key", e);
        }
    }

    @Override
    public AsymmetricCipher setKeyPair(KeyPair keyPair) {
        if (keyPair == null) {
            throw new NullPointerException("KeyPair cannot be null");
        }
        setPublicKey(keyPair.getPublic());
        setPrivateKey(keyPair.getPrivate());
        return this;
    }

    @Override
    public byte[] encrypt(byte[] plaintext) {
        if (plaintext == null) {
            throw new NullPointerException("Plaintext cannot be null");
        }
        if (publicKey == null) {
            throw new IllegalStateException("Public key not set");
        }

        byte[] sharedSecret = null;
        byte[] derivedKey = null;
        try {
            // Generate ephemeral key pair
            KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            ECGenParameterSpec ecSpec = new ECGenParameterSpec(curveType.getCurveName());
            generator.initialize(ecSpec, new SecureRandom());
            KeyPair ephemeralKeyPair = generator.generateKeyPair();

            // Perform ECDH key agreement
            KeyAgreement keyAgreement = KeyAgreement.getInstance(KEY_AGREEMENT_ALGORITHM);
            keyAgreement.init(ephemeralKeyPair.getPrivate());
            keyAgreement.doPhase(publicKey, true);
            sharedSecret = keyAgreement.generateSecret();

            // Derive AES key from shared secret using SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            derivedKey = digest.digest(sharedSecret);

            // Generate random IV for GCM
            byte[] iv = new byte[GCM_IV_SIZE];
            new SecureRandom().nextBytes(iv);

            // Encrypt with AES-GCM
            SecretKey aesKey = new SecretKeySpec(derivedKey, "AES");
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_SIZE, iv);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);
            byte[] ciphertext = cipher.doFinal(plaintext);

            // Get ephemeral public key bytes
            byte[] ephemeralPublicKey = ephemeralKeyPair.getPublic().getEncoded();

            // Format: [ephemeral_public_key_length(4)] [ephemeral_public_key] [iv] [ciphertext]
            ByteBuffer buffer = ByteBuffer.allocate(4 + ephemeralPublicKey.length + iv.length + ciphertext.length);
            buffer.putInt(ephemeralPublicKey.length);
            buffer.put(ephemeralPublicKey);
            buffer.put(iv);
            buffer.put(ciphertext);

            return buffer.array();
        } catch (Exception e) {
            throw OpenCryptoException.encryptionFailed(getAlgorithm(), e);
        } finally {
            if (sharedSecret != null) java.util.Arrays.fill(sharedSecret, (byte) 0);
            if (derivedKey != null) java.util.Arrays.fill(derivedKey, (byte) 0);
        }
    }

    @Override
    public byte[] encrypt(String plaintext) {
        if (plaintext == null) {
            throw new NullPointerException("Plaintext cannot be null");
        }
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
        if (ciphertext == null) {
            throw new NullPointerException("Ciphertext cannot be null");
        }
        if (privateKey == null) {
            throw new IllegalStateException("Private key not set");
        }

        byte[] sharedSecret = null;
        byte[] derivedKey = null;
        try {
            ByteBuffer buffer = ByteBuffer.wrap(ciphertext);

            // Extract ephemeral public key length and validate it
            if (buffer.remaining() < 4) {
                throw new OpenCryptoException(getAlgorithm(), "decryption",
                    "Ciphertext too short: missing ephemeral key length");
            }
            int ephemeralKeyLength = buffer.getInt();
            // Validate: must be positive, must not exceed remaining buffer (minus IV),
            // and reasonable max (EC public key X.509 encoded is typically < 256 bytes)
            if (ephemeralKeyLength <= 0 || ephemeralKeyLength > 1024
                    || ephemeralKeyLength > buffer.remaining() - GCM_IV_SIZE) {
                throw new OpenCryptoException(getAlgorithm(), "decryption",
                    "Invalid ephemeral key length: " + ephemeralKeyLength);
            }
            byte[] ephemeralPublicKeyBytes = new byte[ephemeralKeyLength];
            buffer.get(ephemeralPublicKeyBytes);

            // Extract IV
            byte[] iv = new byte[GCM_IV_SIZE];
            buffer.get(iv);

            // Extract encrypted data
            byte[] encryptedData = new byte[buffer.remaining()];
            buffer.get(encryptedData);

            // Reconstruct ephemeral public key
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(ephemeralPublicKeyBytes);
            PublicKey ephemeralPublicKey = keyFactory.generatePublic(keySpec);

            // Perform ECDH key agreement
            KeyAgreement keyAgreement = KeyAgreement.getInstance(KEY_AGREEMENT_ALGORITHM);
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(ephemeralPublicKey, true);
            sharedSecret = keyAgreement.generateSecret();

            // Derive AES key from shared secret
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            derivedKey = digest.digest(sharedSecret);

            // Decrypt with AES-GCM
            SecretKey aesKey = new SecretKeySpec(derivedKey, "AES");
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_SIZE, iv);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);

            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw OpenCryptoException.decryptionFailed(getAlgorithm(), e);
        } finally {
            if (sharedSecret != null) java.util.Arrays.fill(sharedSecret, (byte) 0);
            if (derivedKey != null) java.util.Arrays.fill(derivedKey, (byte) 0);
        }
    }

    @Override
    public String decryptToString(byte[] ciphertext) {
        return new String(decrypt(ciphertext), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] decryptBase64(String base64Ciphertext) {
        if (base64Ciphertext == null) {
            throw new NullPointerException("Base64 ciphertext cannot be null");
        }
        byte[] ciphertext = OpenBase64.decode(base64Ciphertext);
        return decrypt(ciphertext);
    }

    @Override
    public byte[] decryptHex(String hexCiphertext) {
        if (hexCiphertext == null) {
            throw new NullPointerException("Hex ciphertext cannot be null");
        }
        byte[] ciphertext = HexCodec.decode(hexCiphertext);
        return decrypt(ciphertext);
    }

    @Override
    public String getAlgorithm() {
        return "ECIES-" + curveType.getCurveName() + "-AES-256-GCM";
    }

    @Override
    public int getMaxEncryptSize() {
        // ECIES with AES-GCM has no practical size limit for encryption
        // The plaintext can be arbitrarily large
        return -1;
    }

    @Override
    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(curveType.getKeySize());
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new OpenCryptoException("EC", "key generation", "Failed to generate EC key pair", e);
        }
    }

    /**
     * Get the public key
     * 获取公钥
     *
     * @return the public key, or null if not set
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Get the private key
     * 获取私钥
     *
     * @return the private key, or null if not set
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Get the curve type
     * 获取曲线类型
     *
     * @return the curve type
     */
    public CurveType getCurveType() {
        return curveType;
    }
}
