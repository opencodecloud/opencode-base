package cloud.opencode.base.crypto.keyexchange;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import cloud.opencode.base.crypto.kdf.Hkdf;

import javax.crypto.KeyAgreement;
import java.security.*;

/**
 * X25519 key exchange engine (Curve25519 ECDH) - Recommended for most applications
 * X25519 密钥协商引擎 - 推荐用于大多数应用场景
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>X25519 Diffie-Hellman key exchange - X25519 Diffie-Hellman 密钥交换</li>
 *   <li>256-bit security level - 256 位安全级别</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * X25519Engine engine = X25519Engine.create();
 * KeyPair kp = engine.generateKeyPair();
 * byte[] shared = engine.computeSharedSecret(kp.getPrivate(), peerPubKey);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class X25519Engine implements KeyExchangeEngine {

    private static final String ALGORITHM = "X25519";
    private static final String KEY_ALGORITHM = "X25519";

    private PrivateKey privateKey;
    private PublicKey remotePublicKey;

    private X25519Engine() {
    }

    private X25519Engine(KeyPair keyPair) {
        this.privateKey = keyPair.getPrivate();
    }

    /**
     * Creates a new X25519 engine instance
     * 创建新的 X25519 引擎实例
     *
     * @return new X25519 engine instance
     */
    public static X25519Engine create() {
        return new X25519Engine();
    }

    /**
     * Creates a new X25519 engine instance with a generated key pair
     * 创建新的 X25519 引擎实例并生成密钥对
     *
     * @return new X25519 engine instance with generated key pair
     */
    public static X25519Engine withGeneratedKeyPair() {
        X25519Engine engine = new X25519Engine();
        KeyPair keyPair = engine.generateKeyPair();
        return new X25519Engine(keyPair);
    }

    /**
     * Performs X25519 key agreement between two parties (static method)
     * 执行两方之间的 X25519 密钥协商（静态方法）
     *
     * @param myPrivateKey the local private key
     * @param theirPublicKey the remote public key
     * @return the shared secret (32 bytes)
     * @throws NullPointerException if any key is null
     */
    public static byte[] agree(PrivateKey myPrivateKey, PublicKey theirPublicKey) {
        if (myPrivateKey == null || theirPublicKey == null) {
            throw new NullPointerException("Keys cannot be null");
        }

        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance(ALGORITHM);
            keyAgreement.init(myPrivateKey);
            keyAgreement.doPhase(theirPublicKey, true);
            return keyAgreement.generateSecret();
        } catch (NoSuchAlgorithmException e) {
            throw OpenCryptoException.algorithmNotAvailable(ALGORITHM);
        } catch (InvalidKeyException e) {
            throw new OpenCryptoException(ALGORITHM, "agreement", "Invalid key for agreement", e);
        } catch (Exception e) {
            throw new OpenCryptoException(ALGORITHM, "agreement", "Key agreement failed", e);
        }
    }

    /**
     * Performs X25519 key agreement and derives key material using HKDF (static method)
     * 执行 X25519 密钥协商并使用 HKDF 派生密钥材料（静态方法）
     *
     * @param myPrivateKey the local private key
     * @param theirPublicKey the remote public key
     * @param info optional context information for key derivation
     * @param keyLength desired key length in bytes
     * @return derived key material
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if keyLength is invalid
     */
    public static byte[] agreeAndDerive(PrivateKey myPrivateKey, PublicKey theirPublicKey,
                                        byte[] info, int keyLength) {
        byte[] sharedSecret = agree(myPrivateKey, theirPublicKey);
        try {
            return Hkdf.sha256().deriveKey(sharedSecret, info, keyLength);
        } finally {
            // Clear sensitive data
            java.util.Arrays.fill(sharedSecret, (byte) 0);
        }
    }

    @Override
    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGenerator.initialize(255, new SecureRandom());
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw OpenKeyException.generationFailed(KEY_ALGORITHM, e);
        } catch (InvalidParameterException e) {
            throw new OpenKeyException(KEY_ALGORITHM, "Key generation failed", e);
        }
    }

    @Override
    public X25519Engine setPrivateKey(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new NullPointerException("Private key cannot be null");
        }
        if (!KEY_ALGORITHM.equals(privateKey.getAlgorithm()) &&
            !"XDH".equals(privateKey.getAlgorithm())) {
            throw OpenKeyException.typeMismatch(KEY_ALGORITHM, privateKey.getAlgorithm());
        }
        this.privateKey = privateKey;
        return this;
    }

    @Override
    public X25519Engine setRemotePublicKey(PublicKey publicKey) {
        if (publicKey == null) {
            throw new NullPointerException("Public key cannot be null");
        }
        if (!KEY_ALGORITHM.equals(publicKey.getAlgorithm()) &&
            !"XDH".equals(publicKey.getAlgorithm())) {
            throw OpenKeyException.typeMismatch(KEY_ALGORITHM, publicKey.getAlgorithm());
        }
        this.remotePublicKey = publicKey;
        return this;
    }

    @Override
    public byte[] computeSharedSecret() {
        if (privateKey == null) {
            throw OpenKeyException.keyNotSet("X25519 agreement (private key missing)");
        }
        if (remotePublicKey == null) {
            throw OpenKeyException.keyNotSet("X25519 agreement (remote public key missing)");
        }

        return agree(privateKey, remotePublicKey);
    }

    @Override
    public byte[] deriveKey(byte[] info, int length) {
        byte[] sharedSecret = computeSharedSecret();
        try {
            return Hkdf.sha256().deriveKey(sharedSecret, info, length);
        } finally {
            // Clear sensitive data
            java.util.Arrays.fill(sharedSecret, (byte) 0);
        }
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }

    /**
     * Gets the private key if set
     * 获取已设置的私钥
     *
     * @return the private key, or null if not set
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Gets the public key from the private key (if set).
     * 从私钥获取公钥（如果已设置）。
     *
     * <p>X25519 does not support deriving a public key from an existing private key
     * through the standard JCA API. Use {@link #generateKeyPair()} to obtain both
     * the public and private keys together.</p>
     *
     * @return never returns normally
     * @throws UnsupportedOperationException always; use {@link #generateKeyPair()} to obtain keys
     */
    public PublicKey getPublicKey() {
        throw new UnsupportedOperationException(
                "Key retrieval not supported; use generateKeyPair() to obtain keys");
    }
}
