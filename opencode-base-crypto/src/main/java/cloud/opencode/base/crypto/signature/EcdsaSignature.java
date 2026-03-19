package cloud.opencode.base.crypto.signature;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.codec.PemCodec;
import cloud.opencode.base.crypto.enums.CurveType;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import cloud.opencode.base.crypto.exception.OpenSignatureException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * ECDSA signature implementation - Elliptic Curve Digital Signature Algorithm
 * ECDSA 签名实现 - 椭圆曲线数字签名算法
 * <p>
 * ECDSA provides strong security with smaller key sizes compared to RSA.
 * Commonly used curves are P-256, P-384, and P-521.
 * ECDSA 提供比 RSA 更小的密钥大小和强大的安全性。常用曲线为 P-256、P-384 和 P-521。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ECDSA with P-256, P-384, P-521 - ECDSA（P-256、P-384、P-521）</li>
 *   <li>SHA-256/384/512 digest algorithms - SHA-256/384/512 摘要算法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EcdsaSignature ecdsa = EcdsaSignature.p256();
 * ecdsa.setPrivateKey(privateKey);
 * byte[] sig = ecdsa.sign(data);
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
public final class EcdsaSignature implements SignatureEngine {

    private static final String KEY_ALGORITHM = "EC";
    private static final int BUFFER_SIZE = 8192;

    private final String algorithm;
    private final CurveType curveType;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private Signature signatureInstance;

    private EcdsaSignature(String algorithm, CurveType curveType) {
        this.algorithm = algorithm;
        this.curveType = curveType;
    }

    /**
     * Create ECDSA signature with P-256 curve (SHA-256, recommended for most uses)
     * 创建使用 P-256 曲线的 ECDSA 签名（SHA-256，推荐用于大多数场景）
     *
     * @return ECDSA P-256 signature instance
     */
    public static EcdsaSignature p256() {
        return new EcdsaSignature("SHA256withECDSA", CurveType.P_256);
    }

    /**
     * Create ECDSA signature with P-384 curve (SHA-384, higher security)
     * 创建使用 P-384 曲线的 ECDSA 签名（SHA-384，更高安全性）
     *
     * @return ECDSA P-384 signature instance
     */
    public static EcdsaSignature p384() {
        return new EcdsaSignature("SHA384withECDSA", CurveType.P_384);
    }

    /**
     * Create ECDSA signature with P-521 curve (SHA-512, maximum security)
     * 创建使用 P-521 曲线的 ECDSA 签名（SHA-512，最高安全性）
     *
     * @return ECDSA P-521 signature instance
     */
    public static EcdsaSignature p521() {
        return new EcdsaSignature("SHA512withECDSA", CurveType.P_521);
    }

    /**
     * Create ECDSA signature with custom curve type
     * 创建使用自定义曲线类型的 ECDSA 签名
     *
     * @param curve the elliptic curve type
     * @return ECDSA signature instance
     */
    public static EcdsaSignature withCurve(CurveType curve) {
        if (curve == null) {
            throw new NullPointerException("Curve type cannot be null");
        }

        String algorithm = switch (curve) {
            case P_256, SECP256K1 -> "SHA256withECDSA";
            case P_384 -> "SHA384withECDSA";
            case P_521 -> "SHA512withECDSA";
            default -> throw new IllegalArgumentException("Unsupported curve for ECDSA: " + curve);
        };

        return new EcdsaSignature(algorithm, curve);
    }

    /**
     * Create ECDSA P-256 signature with generated key pair
     * 创建带有生成密钥对的 ECDSA P-256 签名
     *
     * @return ECDSA P-256 signature with generated keys
     */
    public static EcdsaSignature p256WithGeneratedKeyPair() {
        return p256().withGeneratedKeyPair();
    }

    /**
     * Create ECDSA P-384 signature with generated key pair
     * 创建带有生成密钥对的 ECDSA P-384 签名
     *
     * @return ECDSA P-384 signature with generated keys
     */
    public static EcdsaSignature p384WithGeneratedKeyPair() {
        return p384().withGeneratedKeyPair();
    }

    /**
     * Create ECDSA P-521 signature with generated key pair
     * 创建带有生成密钥对的 ECDSA P-521 签名
     *
     * @return ECDSA P-521 signature with generated keys
     */
    public static EcdsaSignature p521WithGeneratedKeyPair() {
        return p521().withGeneratedKeyPair();
    }

    /**
     * Generate a new EC key pair for the configured curve
     * 为配置的曲线生成新的 EC 密钥对
     *
     * @return this signature instance with generated keys
     */
    public EcdsaSignature withGeneratedKeyPair() {
        if (curveType == null) {
            throw new IllegalStateException("Curve type not set");
        }

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            ECGenParameterSpec ecSpec = new ECGenParameterSpec(curveType.getCurveName());
            generator.initialize(ecSpec);
            KeyPair keyPair = generator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
            return this;
        } catch (Exception e) {
            throw new OpenKeyException("Failed to generate EC key pair", e);
        }
    }

    @Override
    public SignatureEngine setPrivateKey(PrivateKey privateKey) {
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
    public SignatureEngine setPrivateKey(byte[] encodedKey) {
        if (encodedKey == null) {
            throw new NullPointerException("Encoded key cannot be null");
        }
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
            this.privateKey = keyFactory.generatePrivate(keySpec);
            return this;
        } catch (Exception e) {
            throw new OpenKeyException("Failed to decode EC private key", e);
        }
    }

    @Override
    public SignatureEngine setPrivateKeyPem(String pem) {
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
    public SignatureEngine setPublicKey(PublicKey publicKey) {
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
    public SignatureEngine setPublicKey(byte[] encodedKey) {
        if (encodedKey == null) {
            throw new NullPointerException("Encoded key cannot be null");
        }
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
            this.publicKey = keyFactory.generatePublic(keySpec);
            return this;
        } catch (Exception e) {
            throw new OpenKeyException("Failed to decode EC public key", e);
        }
    }

    @Override
    public SignatureEngine setPublicKeyPem(String pem) {
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
    public SignatureEngine setKeyPair(KeyPair keyPair) {
        if (keyPair == null) {
            throw new NullPointerException("KeyPair cannot be null");
        }
        setPublicKey(keyPair.getPublic());
        setPrivateKey(keyPair.getPrivate());
        return this;
    }

    @Override
    public byte[] sign(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        if (privateKey == null) {
            throw new IllegalStateException("Private key not set");
        }

        try {
            Signature signature = Signature.getInstance(algorithm);
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw OpenSignatureException.signFailed(algorithm, e);
        }
    }

    @Override
    public byte[] sign(String data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return sign(data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String signBase64(byte[] data) {
        return OpenBase64.encode(sign(data));
    }

    @Override
    public String signBase64(String data) {
        return OpenBase64.encode(sign(data));
    }

    @Override
    public String signHex(byte[] data) {
        return HexCodec.encode(sign(data));
    }

    @Override
    public byte[] signFile(Path file) {
        if (file == null) {
            throw new NullPointerException("File path cannot be null");
        }
        if (!Files.exists(file)) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }

        try (InputStream input = Files.newInputStream(file)) {
            return sign(input);
        } catch (IOException e) {
            throw new OpenSignatureException("Failed to read file for signing", e);
        }
    }

    @Override
    public byte[] sign(InputStream input) {
        if (input == null) {
            throw new NullPointerException("InputStream cannot be null");
        }
        if (privateKey == null) {
            throw new IllegalStateException("Private key not set");
        }

        try {
            Signature signature = Signature.getInstance(algorithm);
            signature.initSign(privateKey);

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                signature.update(buffer, 0, bytesRead);
            }

            return signature.sign();
        } catch (Exception e) {
            throw OpenSignatureException.signFailed(algorithm, e);
        }
    }

    @Override
    public boolean verify(byte[] data, byte[] signature) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        if (signature == null) {
            throw new NullPointerException("Signature cannot be null");
        }
        if (publicKey == null) {
            throw new IllegalStateException("Public key not set");
        }

        try {
            Signature sig = Signature.getInstance(algorithm);
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(signature);
        } catch (Exception e) {
            throw OpenSignatureException.verifyFailed(algorithm, e);
        }
    }

    @Override
    public boolean verify(String data, byte[] signature) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return verify(data.getBytes(StandardCharsets.UTF_8), signature);
    }

    @Override
    public boolean verifyBase64(byte[] data, String base64Signature) {
        if (base64Signature == null) {
            throw new NullPointerException("Base64 signature cannot be null");
        }
        byte[] signature = OpenBase64.decode(base64Signature);
        return verify(data, signature);
    }

    @Override
    public boolean verifyBase64(String data, String base64Signature) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return verifyBase64(data.getBytes(StandardCharsets.UTF_8), base64Signature);
    }

    @Override
    public boolean verifyHex(byte[] data, String hexSignature) {
        if (hexSignature == null) {
            throw new NullPointerException("Hex signature cannot be null");
        }
        byte[] signature = HexCodec.decode(hexSignature);
        return verify(data, signature);
    }

    @Override
    public boolean verifyFile(Path file, byte[] signature) {
        if (file == null) {
            throw new NullPointerException("File path cannot be null");
        }
        if (signature == null) {
            throw new NullPointerException("Signature cannot be null");
        }
        if (!Files.exists(file)) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }
        if (publicKey == null) {
            throw new IllegalStateException("Public key not set");
        }

        try (InputStream input = Files.newInputStream(file)) {
            Signature sig = Signature.getInstance(algorithm);
            sig.initVerify(publicKey);

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                sig.update(buffer, 0, bytesRead);
            }

            return sig.verify(signature);
        } catch (Exception e) {
            throw OpenSignatureException.verifyFailed(algorithm, e);
        }
    }

    @Override
    public SignatureEngine update(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }

        try {
            ensureSignatureInstance();
            signatureInstance.update(data);
            return this;
        } catch (SignatureException e) {
            throw new OpenSignatureException("Failed to update signature", e);
        }
    }

    @Override
    public SignatureEngine update(String data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return update(data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public byte[] doSign() {
        if (signatureInstance == null) {
            throw new IllegalStateException("No data has been updated for signing");
        }

        try {
            byte[] result = signatureInstance.sign();
            signatureInstance = null; // Reset for next operation
            return result;
        } catch (SignatureException e) {
            signatureInstance = null;
            throw OpenSignatureException.signFailed(algorithm, e);
        }
    }

    @Override
    public String doSignBase64() {
        return OpenBase64.encode(doSign());
    }

    @Override
    public boolean doVerify(byte[] signature) {
        if (signature == null) {
            throw new NullPointerException("Signature cannot be null");
        }
        if (signatureInstance == null) {
            throw new IllegalStateException("No data has been updated for verification");
        }

        try {
            boolean result = signatureInstance.verify(signature);
            signatureInstance = null; // Reset for next operation
            return result;
        } catch (SignatureException e) {
            signatureInstance = null;
            throw OpenSignatureException.verifyFailed(algorithm, e);
        }
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
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
     * Ensure signature instance is initialized for multi-part operations
     * 确保签名实例已初始化用于多部分操作
     */
    private void ensureSignatureInstance() {
        if (signatureInstance == null) {
            try {
                signatureInstance = Signature.getInstance(algorithm);
                if (privateKey != null) {
                    signatureInstance.initSign(privateKey);
                } else if (publicKey != null) {
                    signatureInstance.initVerify(publicKey);
                } else {
                    throw new IllegalStateException("Neither private nor public key is set");
                }
            } catch (Exception e) {
                throw new OpenSignatureException("Failed to initialize signature", e);
            }
        }
    }
}
