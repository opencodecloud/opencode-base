package cloud.opencode.base.crypto.asymmetric;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.codec.PemCodec;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * SM2 cipher implementation - Chinese national cryptographic standard
 * SM2 密码实现 - 中国国密标准
 * <p>
 * SM2 is an elliptic curve cryptography algorithm specified in the Chinese
 * National Standard GM/T 0003-2012. This implementation requires Bouncy Castle
 * cryptographic provider to be available on the classpath.
 * SM2 是中国国家标准 GM/T 0003-2012 中规定的椭圆曲线密码算法。
 * 此实现需要 Bouncy Castle 加密提供程序在类路径中可用。
 * <p>
 * <strong>Note:</strong> This class requires the optional Bouncy Castle dependency.
 * If Bouncy Castle is not available, instantiation will fail with an exception.
 * <strong>注意：</strong>此类需要可选的 Bouncy Castle 依赖。
 * 如果 Bouncy Castle 不可用，实例化将失败并抛出异常。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SM2 encryption (Chinese national standard) - SM2 加密（中国国密标准）</li>
 *   <li>Requires Bouncy Castle provider - 需要 Bouncy Castle 提供者</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Sm2Cipher sm2 = Sm2Cipher.create();
 * sm2.setPublicKey(publicKey);
 * byte[] encrypted = sm2.encrypt(data);
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
 *   <li>Time complexity: O(k^3) for key operations - 时间复杂度: O(k^3)，k为密钥参数</li>
 *   <li>Space complexity: O(k) - 空间复杂度: O(k)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class Sm2Cipher implements AsymmetricCipher {

    private static final String ALGORITHM = "SM2";
    private static final String KEY_ALGORITHM = "EC";
    private static final String CURVE_NAME = "sm2p256v1";
    private static final String BC_PROVIDER = "BC";
    private static final int KEY_SIZE = 256;

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private final boolean bcAvailable;

    /**
     * Private constructor
     */
    private Sm2Cipher() {
        this.bcAvailable = checkBouncyCastleAvailable();
        if (!bcAvailable) {
            throw new OpenCryptoException(
                "SM2 requires Bouncy Castle provider. " +
                "Add org.bouncycastle:bcprov-jdk18on to your dependencies."
            );
        }
        ensureBouncyCastleProvider();
    }

    /**
     * Create a new SM2 cipher instance
     * 创建新的 SM2 密码实例
     *
     * @return new SM2 cipher instance
     * @throws OpenCryptoException if Bouncy Castle is not available
     */
    public static Sm2Cipher create() {
        return new Sm2Cipher();
    }

    /**
     * Create SM2 cipher with generated key pair
     * 创建带生成密钥对的 SM2 密码
     *
     * @return SM2 cipher with generated keys
     * @throws OpenCryptoException if Bouncy Castle is not available or key generation fails
     */
    public static Sm2Cipher withGeneratedKeyPair() {
        Sm2Cipher cipher = new Sm2Cipher();

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM, BC_PROVIDER);

            // Use reflection to avoid compile-time dependency on Bouncy Castle
            Class<?> ecGenParamSpecClass = Class.forName("java.security.spec.ECGenParameterSpec");
            Object ecSpec = ecGenParamSpecClass.getConstructor(String.class).newInstance(CURVE_NAME);

            generator.initialize((AlgorithmParameterSpec) ecSpec, new SecureRandom());
            KeyPair keyPair = generator.generateKeyPair();

            cipher.publicKey = keyPair.getPublic();
            cipher.privateKey = keyPair.getPrivate();
            return cipher;
        } catch (ClassNotFoundException e) {
            throw new OpenCryptoException("Failed to load EC parameter classes", e);
        } catch (NoSuchAlgorithmException e) {
            throw OpenCryptoException.algorithmNotAvailable(ALGORITHM);
        } catch (NoSuchProviderException e) {
            throw new OpenCryptoException("Bouncy Castle provider not available", e);
        } catch (Exception e) {
            throw new OpenCryptoException("Failed to generate SM2 key pair", e);
        }
    }

    @Override
    public AsymmetricCipher setPublicKey(PublicKey publicKey) {
        if (publicKey == null) {
            throw new NullPointerException("Public key cannot be null");
        }
        if (!KEY_ALGORITHM.equals(publicKey.getAlgorithm())) {
            throw new IllegalArgumentException("Key must be EC key for SM2");
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
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM, BC_PROVIDER);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
            this.publicKey = keyFactory.generatePublic(keySpec);
            return this;
        } catch (Exception e) {
            throw new OpenKeyException("Failed to decode SM2 public key", e);
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
            throw new IllegalArgumentException("Key must be EC key for SM2");
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
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM, BC_PROVIDER);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
            this.privateKey = keyFactory.generatePrivate(keySpec);
            return this;
        } catch (Exception e) {
            throw new OpenKeyException("Failed to decode SM2 private key", e);
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

        try {
            // Use reflection to avoid compile-time dependency on Bouncy Castle
            Class<?> cipherClass = Class.forName("javax.crypto.Cipher");
            Object cipher = cipherClass.getMethod("getInstance", String.class, String.class)
                .invoke(null, ALGORITHM, BC_PROVIDER);

            cipherClass.getMethod("init", int.class, Key.class)
                .invoke(cipher, javax.crypto.Cipher.ENCRYPT_MODE, publicKey);

            return (byte[]) cipherClass.getMethod("doFinal", byte[].class)
                .invoke(cipher, plaintext);
        } catch (ClassNotFoundException e) {
            throw new OpenCryptoException("Cipher class not found", e);
        } catch (Exception e) {
            throw OpenCryptoException.encryptionFailed(ALGORITHM, e);
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

        try {
            // Use reflection to avoid compile-time dependency on Bouncy Castle
            Class<?> cipherClass = Class.forName("javax.crypto.Cipher");
            Object cipher = cipherClass.getMethod("getInstance", String.class, String.class)
                .invoke(null, ALGORITHM, BC_PROVIDER);

            cipherClass.getMethod("init", int.class, Key.class)
                .invoke(cipher, javax.crypto.Cipher.DECRYPT_MODE, privateKey);

            return (byte[]) cipherClass.getMethod("doFinal", byte[].class)
                .invoke(cipher, ciphertext);
        } catch (ClassNotFoundException e) {
            throw new OpenCryptoException("Cipher class not found", e);
        } catch (Exception e) {
            throw OpenCryptoException.decryptionFailed(ALGORITHM, e);
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
        return ALGORITHM;
    }

    @Override
    public int getMaxEncryptSize() {
        // SM2 encryption has a practical limit, but it varies by implementation
        // Return -1 to indicate no fixed limit
        return -1;
    }

    @Override
    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM, "BC");
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new OpenCryptoException(KEY_ALGORITHM, "key generation", "Failed to generate SM2 key pair", e);
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
     * Check if Bouncy Castle provider is available
     * 检查 Bouncy Castle 提供程序是否可用
     *
     * @return true if available, false otherwise
     */
    private static boolean checkBouncyCastleAvailable() {
        try {
            Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Ensure Bouncy Castle provider is registered
     * 确保注册了 Bouncy Castle 提供程序
     */
    private static void ensureBouncyCastleProvider() {
        try {
            // Check if BC provider is already registered
            if (Security.getProvider(BC_PROVIDER) == null) {
                // Register BC provider using reflection
                Class<?> providerClass = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
                Provider provider = (Provider) providerClass.getDeclaredConstructor().newInstance();
                Security.addProvider(provider);
            }
        } catch (Exception e) {
            throw new OpenCryptoException("Failed to register Bouncy Castle provider", e);
        }
    }

    /**
     * Check if SM2 is available (i.e., Bouncy Castle is present)
     * 检查 SM2 是否可用（即 Bouncy Castle 是否存在）
     *
     * @return true if SM2 is available, false otherwise
     */
    public static boolean isAvailable() {
        return checkBouncyCastleAvailable();
    }
}
