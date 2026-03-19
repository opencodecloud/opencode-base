package cloud.opencode.base.crypto.keyexchange;

import cloud.opencode.base.crypto.enums.CurveType;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import cloud.opencode.base.crypto.kdf.Hkdf;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.ECGenParameterSpec;

/**
 * ECDH (Elliptic Curve Diffie-Hellman) key exchange engine
 * ECDH 密钥协商引擎 - 椭圆曲线 Diffie-Hellman 密钥交换
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ECDH key exchange with NIST curves - ECDH 密钥交换（NIST 曲线）</li>
 *   <li>P-256, P-384, P-521 support - P-256、P-384、P-521 支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EcdhEngine engine = EcdhEngine.p256();
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
public final class EcdhEngine implements KeyExchangeEngine {

    private static final String ALGORITHM = "ECDH";
    private static final String KEY_ALGORITHM = "EC";

    private final CurveType curve;
    private PrivateKey privateKey;
    private PublicKey remotePublicKey;

    private EcdhEngine(CurveType curve) {
        if (curve == CurveType.CURVE25519 || curve == CurveType.CURVE448) {
            throw new IllegalArgumentException(
                "Use X25519Engine or X448Engine for Curve25519/Curve448. " +
                "ECDH is for NIST curves (P-256, P-384, P-521) and secp256k1.");
        }
        this.curve = curve;
    }

    private EcdhEngine(CurveType curve, KeyPair keyPair) {
        this(curve);
        this.privateKey = keyPair.getPrivate();
    }

    /**
     * Creates an ECDH engine with NIST P-256 curve
     * 创建使用 NIST P-256 曲线的 ECDH 引擎
     *
     * @return new ECDH engine instance
     */
    public static EcdhEngine p256() {
        return new EcdhEngine(CurveType.P_256);
    }

    /**
     * Creates an ECDH engine with NIST P-384 curve
     * 创建使用 NIST P-384 曲线的 ECDH 引擎
     *
     * @return new ECDH engine instance
     */
    public static EcdhEngine p384() {
        return new EcdhEngine(CurveType.P_384);
    }

    /**
     * Creates an ECDH engine with NIST P-521 curve
     * 创建使用 NIST P-521 曲线的 ECDH 引擎
     *
     * @return new ECDH engine instance
     */
    public static EcdhEngine p521() {
        return new EcdhEngine(CurveType.P_521);
    }

    /**
     * Creates an ECDH engine with the specified curve
     * 创建使用指定曲线的 ECDH 引擎
     *
     * @param curve the elliptic curve to use
     * @return new ECDH engine instance
     * @throws NullPointerException if curve is null
     * @throws IllegalArgumentException if curve is not supported for ECDH
     */
    public static EcdhEngine withCurve(CurveType curve) {
        if (curve == null) {
            throw new NullPointerException("Curve cannot be null");
        }
        return new EcdhEngine(curve);
    }

    /**
     * Creates an ECDH engine with the specified curve and generates a key pair
     * 创建使用指定曲线的 ECDH 引擎并生成密钥对
     *
     * @param curve the elliptic curve to use
     * @return new ECDH engine instance with generated key pair
     * @throws NullPointerException if curve is null
     * @throws IllegalArgumentException if curve is not supported for ECDH
     */
    public static EcdhEngine withGeneratedKeyPair(CurveType curve) {
        if (curve == null) {
            throw new NullPointerException("Curve cannot be null");
        }
        EcdhEngine engine = new EcdhEngine(curve);
        KeyPair keyPair = engine.generateKeyPair();
        return new EcdhEngine(curve, keyPair);
    }

    /**
     * Performs ECDH key agreement between two parties (static method)
     * 执行两方之间的 ECDH 密钥协商（静态方法）
     *
     * @param myPrivateKey the local private key
     * @param theirPublicKey the remote public key
     * @return the shared secret
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
     * Performs ECDH key agreement and derives key material using HKDF (static method)
     * 执行 ECDH 密钥协商并使用 HKDF 派生密钥材料（静态方法）
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
            ECGenParameterSpec ecSpec = new ECGenParameterSpec(curve.getCurveName());
            keyPairGenerator.initialize(ecSpec, new SecureRandom());
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw OpenKeyException.generationFailed(KEY_ALGORITHM, e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new OpenKeyException(KEY_ALGORITHM,
                "Key generation failed for curve: " + curve.getCurveName(), e);
        }
    }

    @Override
    public EcdhEngine setPrivateKey(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new NullPointerException("Private key cannot be null");
        }
        if (!KEY_ALGORITHM.equals(privateKey.getAlgorithm())) {
            throw OpenKeyException.typeMismatch(KEY_ALGORITHM, privateKey.getAlgorithm());
        }
        this.privateKey = privateKey;
        return this;
    }

    @Override
    public EcdhEngine setRemotePublicKey(PublicKey publicKey) {
        if (publicKey == null) {
            throw new NullPointerException("Public key cannot be null");
        }
        if (!KEY_ALGORITHM.equals(publicKey.getAlgorithm())) {
            throw OpenKeyException.typeMismatch(KEY_ALGORITHM, publicKey.getAlgorithm());
        }
        this.remotePublicKey = publicKey;
        return this;
    }

    @Override
    public byte[] computeSharedSecret() {
        if (privateKey == null) {
            throw OpenKeyException.keyNotSet("ECDH agreement (private key missing)");
        }
        if (remotePublicKey == null) {
            throw OpenKeyException.keyNotSet("ECDH agreement (remote public key missing)");
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
        return ALGORITHM + "-" + curve.name();
    }

    /**
     * Gets the curve type used by this engine
     * 获取此引擎使用的曲线类型
     *
     * @return the curve type
     */
    public CurveType getCurve() {
        return curve;
    }
}
