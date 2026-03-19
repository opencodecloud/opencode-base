package cloud.opencode.base.crypto.key;

import cloud.opencode.base.crypto.codec.PemCodec;
import cloud.opencode.base.crypto.enums.CurveType;
import cloud.opencode.base.crypto.exception.OpenKeyException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.*;

/**
 * Key generator utility for symmetric and asymmetric keys - Generate cryptographic keys
 * 密钥生成器工具类 - 生成对称和非对称密钥
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>AES, RSA, EC, Ed25519 key generation - AES、RSA、EC、Ed25519 密钥生成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SecretKey aesKey = KeyGenerator.generateAesKey(256);
 * KeyPair rsaKeyPair = KeyGenerator.generateRsaKeyPair(2048);
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
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class KeyGenerator {

    private KeyGenerator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== Symmetric Key Generation ====================

    /**
     * Generate AES key with specified key size
     * 生成指定大小的 AES 密钥
     *
     * @param keyBits key size in bits (128, 192, or 256)
     * @return generated AES secret key
     * @throws OpenKeyException if key generation fails
     */
    public static SecretKey generateAesKey(int keyBits) {
        try {
            javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance("AES");
            keyGen.init(keyBits);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw OpenKeyException.generationFailed("AES", e);
        }
    }

    /**
     * Generate AES-128 key
     * 生成 AES-128 密钥
     *
     * @return generated AES-128 secret key
     * @throws OpenKeyException if key generation fails
     */
    public static SecretKey generateAes128Key() {
        return generateAesKey(128);
    }

    /**
     * Generate AES-256 key
     * 生成 AES-256 密钥
     *
     * @return generated AES-256 secret key
     * @throws OpenKeyException if key generation fails
     */
    public static SecretKey generateAes256Key() {
        return generateAesKey(256);
    }

    /**
     * Generate ChaCha20 key
     * 生成 ChaCha20 密钥
     *
     * @return generated ChaCha20 secret key
     * @throws OpenKeyException if key generation fails
     */
    public static SecretKey generateChacha20Key() {
        try {
            javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance("ChaCha20");
            keyGen.init(256);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw OpenKeyException.generationFailed("ChaCha20", e);
        }
    }

    /**
     * Generate SM4 key (Chinese standard)
     * 生成 SM4 密钥（中国标准）
     *
     * @return generated SM4 secret key
     * @throws OpenKeyException if key generation fails
     */
    public static SecretKey generateSm4Key() {
        try {
            SecureRandom random = new SecureRandom();
            byte[] keyBytes = new byte[16]; // SM4 uses 128-bit keys
            random.nextBytes(keyBytes);
            return new SecretKeySpec(keyBytes, "SM4");
        } catch (Exception e) {
            throw OpenKeyException.generationFailed("SM4", e);
        }
    }

    /**
     * Create secret key from byte array
     * 从字节数组创建对称密钥
     *
     * @param keyBytes  key bytes
     * @param algorithm algorithm name (e.g., "AES", "ChaCha20")
     * @return secret key
     * @throws OpenKeyException if key creation fails
     */
    public static SecretKey secretKey(byte[] keyBytes, String algorithm) {
        if (keyBytes == null || keyBytes.length == 0) {
            throw new OpenKeyException("Key bytes cannot be null or empty");
        }
        if (algorithm == null || algorithm.isEmpty()) {
            throw new OpenKeyException("Algorithm cannot be null or empty");
        }
        try {
            return new SecretKeySpec(keyBytes, algorithm);
        } catch (Exception e) {
            throw OpenKeyException.generationFailed(algorithm, e);
        }
    }

    // ==================== Asymmetric Key Pair Generation ====================

    /**
     * Generate RSA key pair with specified key size
     * 生成指定大小的 RSA 密钥对
     *
     * @param keyBits key size in bits (minimum 2048 recommended)
     * @return generated RSA key pair
     * @throws OpenKeyException if key generation fails
     */
    public static KeyPair generateRsaKeyPair(int keyBits) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(keyBits);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw OpenKeyException.generationFailed("RSA", e);
        }
    }

    /**
     * Generate RSA-2048 key pair
     * 生成 RSA-2048 密钥对
     *
     * @return generated RSA-2048 key pair
     * @throws OpenKeyException if key generation fails
     */
    public static KeyPair generateRsa2048KeyPair() {
        return generateRsaKeyPair(2048);
    }

    /**
     * Generate RSA-4096 key pair
     * 生成 RSA-4096 密钥对
     *
     * @return generated RSA-4096 key pair
     * @throws OpenKeyException if key generation fails
     */
    public static KeyPair generateRsa4096KeyPair() {
        return generateRsaKeyPair(4096);
    }

    /**
     * Generate elliptic curve key pair
     * 生成椭圆曲线密钥对
     *
     * @param curve elliptic curve type
     * @return generated EC key pair
     * @throws OpenKeyException if key generation fails
     */
    public static KeyPair generateEcKeyPair(CurveType curve) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec spec = new ECGenParameterSpec(curve.getCurveName());
            keyGen.initialize(spec);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw OpenKeyException.generationFailed("EC-" + curve.name(), e);
        }
    }

    /**
     * Generate P-256 elliptic curve key pair
     * 生成 P-256 椭圆曲线密钥对
     *
     * @return generated P-256 key pair
     * @throws OpenKeyException if key generation fails
     */
    public static KeyPair generateP256KeyPair() {
        return generateEcKeyPair(CurveType.P_256);
    }

    /**
     * Generate P-384 elliptic curve key pair
     * 生成 P-384 椭圆曲线密钥对
     *
     * @return generated P-384 key pair
     * @throws OpenKeyException if key generation fails
     */
    public static KeyPair generateP384KeyPair() {
        return generateEcKeyPair(CurveType.P_384);
    }

    /**
     * Generate Ed25519 key pair for EdDSA signatures
     * 生成用于 EdDSA 签名的 Ed25519 密钥对
     *
     * @return generated Ed25519 key pair
     * @throws OpenKeyException if key generation fails
     */
    public static KeyPair generateEd25519KeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("Ed25519");
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw OpenKeyException.generationFailed("Ed25519", e);
        }
    }

    /**
     * Generate Ed448 key pair for EdDSA signatures
     * 生成用于 EdDSA 签名的 Ed448 密钥对
     *
     * @return generated Ed448 key pair
     * @throws OpenKeyException if key generation fails
     */
    public static KeyPair generateEd448KeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("Ed448");
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw OpenKeyException.generationFailed("Ed448", e);
        }
    }

    /**
     * Generate X25519 key pair for key exchange
     * 生成用于密钥交换的 X25519 密钥对
     *
     * @return generated X25519 key pair
     * @throws OpenKeyException if key generation fails
     */
    public static KeyPair generateX25519KeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("X25519");
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw OpenKeyException.generationFailed("X25519", e);
        }
    }

    /**
     * Generate SM2 key pair (Chinese standard)
     * 生成 SM2 密钥对（中国标准）
     *
     * @return generated SM2 key pair
     * @throws OpenKeyException if key generation fails
     */
    public static KeyPair generateSm2KeyPair() {
        return generateEcKeyPair(CurveType.SM2);
    }

    // ==================== Key Export ====================

    /**
     * Export public key to byte array
     * 导出公钥为字节数组
     *
     * @param publicKey public key to export
     * @return encoded public key bytes
     * @throws OpenKeyException if export fails
     */
    public static byte[] exportPublicKey(PublicKey publicKey) {
        if (publicKey == null) {
            throw new OpenKeyException("Public key cannot be null");
        }
        return publicKey.getEncoded();
    }

    /**
     * Export private key to byte array
     * 导出私钥为字节数组
     *
     * @param privateKey private key to export
     * @return encoded private key bytes
     * @throws OpenKeyException if export fails
     */
    public static byte[] exportPrivateKey(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new OpenKeyException("Private key cannot be null");
        }
        return privateKey.getEncoded();
    }

    /**
     * Export public key to PEM format
     * 导出公钥为 PEM 格式
     *
     * @param publicKey public key to export
     * @return PEM formatted public key
     * @throws OpenKeyException if export fails
     */
    public static String exportPublicKeyPem(PublicKey publicKey) {
        byte[] encoded = exportPublicKey(publicKey);
        return PemCodec.encodePublicKey(encoded);
    }

    /**
     * Export private key to PEM format
     * 导出私钥为 PEM 格式
     *
     * @param privateKey private key to export
     * @return PEM formatted private key
     * @throws OpenKeyException if export fails
     */
    public static String exportPrivateKeyPem(PrivateKey privateKey) {
        byte[] encoded = exportPrivateKey(privateKey);
        return PemCodec.encodePrivateKey(encoded);
    }

    /**
     * Export key pair to PEM format
     * 导出密钥对为 PEM 格式
     *
     * @param keyPair key pair to export
     * @return PEM formatted key pair (public key + private key)
     * @throws OpenKeyException if export fails
     */
    public static String exportKeyPairPem(KeyPair keyPair) {
        if (keyPair == null) {
            throw new OpenKeyException("Key pair cannot be null");
        }
        String publicPem = exportPublicKeyPem(keyPair.getPublic());
        String privatePem = exportPrivateKeyPem(keyPair.getPrivate());
        return publicPem + System.lineSeparator() + privatePem;
    }

    // ==================== Key Import ====================

    /**
     * Import public key from byte array
     * 从字节数组导入公钥
     *
     * @param encoded   encoded public key bytes
     * @param algorithm key algorithm (e.g., "RSA", "EC")
     * @return imported public key
     * @throws OpenKeyException if import fails
     */
    public static PublicKey importPublicKey(byte[] encoded, String algorithm) {
        if (encoded == null || encoded.length == 0) {
            throw new OpenKeyException("Encoded key bytes cannot be null or empty");
        }
        if (algorithm == null || algorithm.isEmpty()) {
            throw new OpenKeyException("Algorithm cannot be null or empty");
        }
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw OpenKeyException.parseFailed(algorithm, e);
        }
    }

    /**
     * Import private key from byte array
     * 从字节数组导入私钥
     *
     * @param encoded   encoded private key bytes
     * @param algorithm key algorithm (e.g., "RSA", "EC")
     * @return imported private key
     * @throws OpenKeyException if import fails
     */
    public static PrivateKey importPrivateKey(byte[] encoded, String algorithm) {
        if (encoded == null || encoded.length == 0) {
            throw new OpenKeyException("Encoded key bytes cannot be null or empty");
        }
        if (algorithm == null || algorithm.isEmpty()) {
            throw new OpenKeyException("Algorithm cannot be null or empty");
        }
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw OpenKeyException.parseFailed(algorithm, e);
        }
    }

    /**
     * Import public key from PEM format
     * 从 PEM 格式导入公钥
     *
     * @param pem PEM formatted public key
     * @return imported public key
     * @throws OpenKeyException if import fails
     */
    public static PublicKey importPublicKeyPem(String pem) {
        try {
            byte[] encoded = PemCodec.decodePublicKey(pem);
            String algorithm = detectAlgorithm(encoded, true);
            return importPublicKey(encoded, algorithm);
        } catch (Exception e) {
            throw OpenKeyException.parseFailed("PEM-PublicKey", e);
        }
    }

    /**
     * Import private key from PEM format
     * 从 PEM 格式导入私钥
     *
     * @param pem PEM formatted private key
     * @return imported private key
     * @throws OpenKeyException if import fails
     */
    public static PrivateKey importPrivateKeyPem(String pem) {
        try {
            byte[] encoded = PemCodec.decodePrivateKey(pem);
            String algorithm = detectAlgorithm(encoded, false);
            return importPrivateKey(encoded, algorithm);
        } catch (Exception e) {
            throw OpenKeyException.parseFailed("PEM-PrivateKey", e);
        }
    }

    /**
     * Import key pair from PEM format
     * 从 PEM 格式导入密钥对
     *
     * @param publicKeyPem  PEM formatted public key
     * @param privateKeyPem PEM formatted private key
     * @return imported key pair
     * @throws OpenKeyException if import fails
     */
    public static KeyPair importKeyPairPem(String publicKeyPem, String privateKeyPem) {
        PublicKey publicKey = importPublicKeyPem(publicKeyPem);
        PrivateKey privateKey = importPrivateKeyPem(privateKeyPem);
        return new KeyPair(publicKey, privateKey);
    }

    // ==================== Helper Methods ====================

    /**
     * Detect key algorithm from encoded key bytes
     * 从编码的密钥字节检测算法
     *
     * @param encoded  encoded key bytes
     * @param isPublic whether this is a public key
     * @return detected algorithm name
     */
    private static String detectAlgorithm(byte[] encoded, boolean isPublic) {
        // Try common algorithms
        String[] algorithms = {"RSA", "EC", "Ed25519", "Ed448", "X25519", "X448"};
        for (String algorithm : algorithms) {
            try {
                KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
                if (isPublic) {
                    keyFactory.generatePublic(new X509EncodedKeySpec(encoded));
                } else {
                    keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encoded));
                }
                return algorithm;
            } catch (Exception e) {
                // Try next algorithm
            }
        }
        throw new OpenKeyException("Unable to detect key algorithm");
    }
}
