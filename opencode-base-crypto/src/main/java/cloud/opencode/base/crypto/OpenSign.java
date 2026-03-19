package cloud.opencode.base.crypto;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.enums.SignatureAlgorithm;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenSignatureException;
import cloud.opencode.base.crypto.signature.*;

import java.nio.charset.StandardCharsets;
import java.security.*;

/**
 * Digital signature facade for signing and verification - Provides convenient API for various signature algorithms
 * 数字签名门面类 - 为各种签名算法提供便捷的 API
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>EdDSA signing (Ed25519, Ed448) - EdDSA 签名（Ed25519、Ed448）</li>
 *   <li>ECDSA signing (P-256, P-384, P-521) - ECDSA 签名（P-256、P-384、P-521）</li>
 *   <li>RSA and RSA-PSS signing - RSA 和 RSA-PSS 签名</li>
 *   <li>SM2 signing (Chinese national standard) - SM2 签名（中国国密标准）</li>
 *   <li>Hex and Base64 output encoding - 十六进制和 Base64 输出编码</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * OpenSign signer = OpenSign.ed25519();
 * KeyPair keyPair = signer.generateKeyPair();
 * signer.setKeyPair(keyPair);
 * byte[] signature = signer.sign("message");
 * boolean valid = signer.verify("message", signature);
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
public final class OpenSign {

    private final SignatureEngine engine;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    private OpenSign(SignatureEngine engine) {
        this.engine = engine;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Create Ed25519 signer (recommended)
     * 创建 Ed25519 签名器（推荐）
     *
     * @return OpenSign instance
     */
    public static OpenSign ed25519() {
        return new OpenSign(EddsaSignature.ed25519());
    }

    /**
     * Create Ed448 signer
     * 创建 Ed448 签名器
     *
     * @return OpenSign instance
     */
    public static OpenSign ed448() {
        return new OpenSign(EddsaSignature.ed448());
    }

    /**
     * Create ECDSA P-256 signer
     * 创建 ECDSA P-256 签名器
     *
     * @return OpenSign instance
     */
    public static OpenSign ecdsaP256() {
        return new OpenSign(EcdsaSignature.p256());
    }

    /**
     * Create ECDSA P-384 signer
     * 创建 ECDSA P-384 签名器
     *
     * @return OpenSign instance
     */
    public static OpenSign ecdsaP384() {
        return new OpenSign(EcdsaSignature.p384());
    }

    /**
     * Create ECDSA P-521 signer
     * 创建 ECDSA P-521 签名器
     *
     * @return OpenSign instance
     */
    public static OpenSign ecdsaP521() {
        return new OpenSign(EcdsaSignature.p521());
    }

    /**
     * Create RSA-SHA256 signer
     * 创建 RSA-SHA256 签名器
     *
     * @return OpenSign instance
     */
    public static OpenSign sha256WithRsa() {
        return new OpenSign(RsaSignature.sha256());
    }

    /**
     * Create RSA-SHA384 signer
     * 创建 RSA-SHA384 签名器
     *
     * @return OpenSign instance
     */
    public static OpenSign sha384WithRsa() {
        return new OpenSign(RsaSignature.sha384());
    }

    /**
     * Create RSA-SHA512 signer
     * 创建 RSA-SHA512 签名器
     *
     * @return OpenSign instance
     */
    public static OpenSign sha512WithRsa() {
        return new OpenSign(RsaSignature.sha512());
    }

    /**
     * Create RSA-PSS signer
     * 创建 RSA-PSS 签名器
     *
     * @return OpenSign instance
     */
    public static OpenSign rsaPss() {
        return new OpenSign(RsaPssSignature.sha256());
    }

    /**
     * Create RSA-PSS-SHA384 signer
     * 创建 RSA-PSS-SHA384 签名器
     *
     * @return OpenSign instance
     */
    public static OpenSign rsaPssSha384() {
        return new OpenSign(RsaPssSignature.sha384());
    }

    /**
     * Create RSA-PSS-SHA512 signer
     * 创建 RSA-PSS-SHA512 签名器
     *
     * @return OpenSign instance
     */
    public static OpenSign rsaPssSha512() {
        return new OpenSign(RsaPssSignature.sha512());
    }

    /**
     * Create SM2 signer (requires Bouncy Castle)
     * 创建 SM2 签名器（需要 Bouncy Castle）
     *
     * @return OpenSign instance
     */
    public static OpenSign sm2() {
        return new OpenSign(Sm2Signature.create());
    }

    /**
     * Create signer by algorithm enum
     * 根据算法枚举创建签名器
     *
     * @param algorithm signature algorithm
     * @return OpenSign instance
     */
    public static OpenSign of(SignatureAlgorithm algorithm) {
        if (algorithm == null) {
            throw new NullPointerException("Algorithm cannot be null");
        }
        return switch (algorithm) {
            case ED25519 -> ed25519();
            case ED448 -> ed448();
            case ECDSA_P256_SHA256 -> ecdsaP256();
            case ECDSA_P384_SHA384 -> ecdsaP384();
            case ECDSA_P521_SHA512 -> ecdsaP521();
            case RSA_SHA256 -> sha256WithRsa();
            case RSA_SHA384 -> sha384WithRsa();
            case RSA_SHA512 -> sha512WithRsa();
            case RSA_PSS_SHA256 -> rsaPss();
            case RSA_PSS_SHA384 -> rsaPssSha384();
            case RSA_PSS_SHA512 -> rsaPssSha512();
            case SM2 -> sm2();
        };
    }

    // ==================== Key Configuration ====================

    /**
     * Set private key for signing
     * 设置签名私钥
     *
     * @param privateKey private key
     * @return this instance for chaining
     */
    public OpenSign setPrivateKey(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new NullPointerException("Private key cannot be null");
        }
        this.privateKey = privateKey;
        engine.setPrivateKey(privateKey);
        return this;
    }

    /**
     * Set public key for verification
     * 设置验签公钥
     *
     * @param publicKey public key
     * @return this instance for chaining
     */
    public OpenSign setPublicKey(PublicKey publicKey) {
        if (publicKey == null) {
            throw new NullPointerException("Public key cannot be null");
        }
        this.publicKey = publicKey;
        engine.setPublicKey(publicKey);
        return this;
    }

    /**
     * Set key pair for signing and verification
     * 设置签名和验签的密钥对
     *
     * @param keyPair key pair
     * @return this instance for chaining
     */
    public OpenSign setKeyPair(KeyPair keyPair) {
        if (keyPair == null) {
            throw new NullPointerException("Key pair cannot be null");
        }
        setPrivateKey(keyPair.getPrivate());
        setPublicKey(keyPair.getPublic());
        return this;
    }

    // ==================== Signing ====================

    /**
     * Sign data
     * 签名数据
     *
     * @param data data to sign
     * @return signature bytes
     */
    public byte[] sign(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        if (privateKey == null) {
            throw new OpenSignatureException("sign", "Private key not set");
        }
        return engine.sign(data);
    }

    /**
     * Sign string (UTF-8)
     * 签名字符串（UTF-8）
     *
     * @param data string to sign
     * @return signature bytes
     */
    public byte[] sign(String data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return sign(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Sign data and return as hex string
     * 签名并返回十六进制字符串
     *
     * @param data data to sign
     * @return hex signature
     */
    public String signHex(byte[] data) {
        return HexCodec.encode(sign(data));
    }

    /**
     * Sign string and return as hex string
     * 签名字符串并返回十六进制字符串
     *
     * @param data string to sign
     * @return hex signature
     */
    public String signHex(String data) {
        return HexCodec.encode(sign(data));
    }

    /**
     * Sign data and return as Base64 string
     * 签名并返回 Base64 字符串
     *
     * @param data data to sign
     * @return Base64 signature
     */
    public String signBase64(byte[] data) {
        return OpenBase64.encode(sign(data));
    }

    /**
     * Sign string and return as Base64 string
     * 签名字符串并返回 Base64 字符串
     *
     * @param data string to sign
     * @return Base64 signature
     */
    public String signBase64(String data) {
        return OpenBase64.encode(sign(data));
    }

    // ==================== Verification ====================

    /**
     * Verify signature
     * 验证签名
     *
     * @param data original data
     * @param signature signature bytes
     * @return true if valid
     */
    public boolean verify(byte[] data, byte[] signature) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        if (signature == null) {
            throw new NullPointerException("Signature cannot be null");
        }
        if (publicKey == null) {
            throw new OpenSignatureException("verify", "Public key not set");
        }
        return engine.verify(data, signature);
    }

    /**
     * Verify signature of string
     * 验证字符串签名
     *
     * @param data original string
     * @param signature signature bytes
     * @return true if valid
     */
    public boolean verify(String data, byte[] signature) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return verify(data.getBytes(StandardCharsets.UTF_8), signature);
    }

    /**
     * Verify hex-encoded signature
     * 验证十六进制编码的签名
     *
     * @param data original data
     * @param signatureHex hex-encoded signature
     * @return true if valid
     */
    public boolean verifyHex(byte[] data, String signatureHex) {
        if (signatureHex == null) {
            throw new NullPointerException("Signature cannot be null");
        }
        return verify(data, HexCodec.decode(signatureHex));
    }

    /**
     * Verify hex-encoded signature of string
     * 验证字符串的十六进制编码签名
     *
     * @param data original string
     * @param signatureHex hex-encoded signature
     * @return true if valid
     */
    public boolean verifyHex(String data, String signatureHex) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return verifyHex(data.getBytes(StandardCharsets.UTF_8), signatureHex);
    }

    /**
     * Verify Base64-encoded signature
     * 验证 Base64 编码的签名
     *
     * @param data original data
     * @param signatureBase64 Base64-encoded signature
     * @return true if valid
     */
    public boolean verifyBase64(byte[] data, String signatureBase64) {
        if (signatureBase64 == null) {
            throw new NullPointerException("Signature cannot be null");
        }
        return verify(data, OpenBase64.decode(signatureBase64));
    }

    /**
     * Verify Base64-encoded signature of string
     * 验证字符串的 Base64 编码签名
     *
     * @param data original string
     * @param signatureBase64 Base64-encoded signature
     * @return true if valid
     */
    public boolean verifyBase64(String data, String signatureBase64) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return verifyBase64(data.getBytes(StandardCharsets.UTF_8), signatureBase64);
    }

    // ==================== Key Generation ====================

    /**
     * Generate key pair for this algorithm
     * 生成此算法的密钥对
     *
     * @return generated key pair
     */
    public KeyPair generateKeyPair() {
        try {
            String algorithm = engine.getAlgorithm();
            String keyAlgorithm = switch (algorithm) {
                case "Ed25519", "Ed448" -> algorithm;
                case "SHA256withECDSA", "SHA384withECDSA", "SHA512withECDSA" -> "EC";
                case "SHA256withRSA", "SHA384withRSA", "SHA512withRSA", "SHA256withRSA/PSS" -> "RSA";
                case "SM3withSM2" -> "SM2";
                default -> "RSA";
            };
            KeyPairGenerator generator = KeyPairGenerator.getInstance(keyAlgorithm);
            if (keyAlgorithm.equals("RSA")) {
                generator.initialize(2048);
            }
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new OpenCryptoException(engine.getAlgorithm(), "generateKeyPair", "Failed to generate key pair", e);
        }
    }

    /**
     * Generate key pair and set it
     * 生成密钥对并设置
     *
     * @return this instance for chaining
     */
    public OpenSign withGeneratedKeyPair() {
        KeyPair keyPair = generateKeyPair();
        setKeyPair(keyPair);
        return this;
    }

    // ==================== Info Methods ====================

    /**
     * Get algorithm name
     * 获取算法名称
     *
     * @return algorithm name
     */
    public String getAlgorithm() {
        return engine.getAlgorithm();
    }
}
