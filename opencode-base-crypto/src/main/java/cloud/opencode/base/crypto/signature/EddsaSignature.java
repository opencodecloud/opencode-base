package cloud.opencode.base.crypto.signature;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.codec.PemCodec;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import cloud.opencode.base.crypto.exception.OpenSignatureException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * EdDSA signature implementation - Edwards-curve Digital Signature Algorithm (recommended)
 * EdDSA 签名实现 - 爱德华兹曲线数字签名算法（推荐）
 * <p>
 * EdDSA using Ed25519 and Ed448 curves provides excellent security and performance.
 * It is deterministic and resistant to side-channel attacks. Native support in JDK 15+.
 * EdDSA 使用 Ed25519 和 Ed448 曲线提供出色的安全性和性能。
 * 它是确定性的且能抵抗侧信道攻击。JDK 15+ 原生支持。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Ed25519 and Ed448 signatures - Ed25519 和 Ed448 签名</li>
 *   <li>High performance Edwards-curve signatures - 高性能 Edwards 曲线签名</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EddsaSignature ed = EddsaSignature.ed25519();
 * ed.setPrivateKey(privateKey);
 * byte[] sig = ed.sign(data);
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
public final class EddsaSignature implements SignatureEngine {

    private static final String ALGORITHM_ED25519 = "Ed25519";
    private static final String ALGORITHM_ED448 = "Ed448";
    private static final int BUFFER_SIZE = 8192;

    private final String algorithm;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private Signature signatureInstance;

    private EddsaSignature(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Create EdDSA signature with Ed25519 curve (recommended for most uses)
     * 创建使用 Ed25519 曲线的 EdDSA 签名（推荐用于大多数场景）
     * <p>
     * Ed25519 provides 128-bit security with 256-bit keys and 512-bit signatures.
     * It is fast, secure, and widely supported.
     * Ed25519 提供 128 位安全性，密钥为 256 位，签名为 512 位。
     * 它快速、安全且广泛支持。
     *
     * @return EdDSA Ed25519 signature instance
     */
    public static EddsaSignature ed25519() {
        return new EddsaSignature(ALGORITHM_ED25519);
    }

    /**
     * Create EdDSA signature with Ed448 curve (higher security)
     * 创建使用 Ed448 曲线的 EdDSA 签名（更高安全性）
     * <p>
     * Ed448 provides 224-bit security with 456-bit keys and 912-bit signatures.
     * Use when higher security margins are required.
     * Ed448 提供 224 位安全性，密钥为 456 位，签名为 912 位。
     * 当需要更高安全边际时使用。
     *
     * @return EdDSA Ed448 signature instance
     */
    public static EddsaSignature ed448() {
        return new EddsaSignature(ALGORITHM_ED448);
    }

    /**
     * Create EdDSA Ed25519 signature with generated key pair
     * 创建带有生成密钥对的 EdDSA Ed25519 签名
     *
     * @return EdDSA Ed25519 signature with generated keys
     */
    public static EddsaSignature ed25519WithGeneratedKeyPair() {
        return ed25519().withGeneratedKeyPair();
    }

    /**
     * Create EdDSA Ed448 signature with generated key pair
     * 创建带有生成密钥对的 EdDSA Ed448 签名
     *
     * @return EdDSA Ed448 signature with generated keys
     */
    public static EddsaSignature ed448WithGeneratedKeyPair() {
        return ed448().withGeneratedKeyPair();
    }

    /**
     * Generate a new EdDSA key pair for the configured algorithm
     * 为配置的算法生成新的 EdDSA 密钥对
     *
     * @return this signature instance with generated keys
     */
    public EddsaSignature withGeneratedKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
            KeyPair keyPair = generator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
            return this;
        } catch (NoSuchAlgorithmException e) {
            throw new OpenKeyException("EdDSA algorithm not available (requires JDK 15+)", e);
        }
    }

    @Override
    public SignatureEngine setPrivateKey(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new NullPointerException("Private key cannot be null");
        }
        if (!isValidEdDsaKey(privateKey.getAlgorithm())) {
            throw new IllegalArgumentException(
                String.format("Key must be %s key, but got %s", algorithm, privateKey.getAlgorithm())
            );
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
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
            this.privateKey = keyFactory.generatePrivate(keySpec);
            return this;
        } catch (Exception e) {
            throw new OpenKeyException("Failed to decode EdDSA private key", e);
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
        if (!isValidEdDsaKey(publicKey.getAlgorithm())) {
            throw new IllegalArgumentException(
                String.format("Key must be %s key, but got %s", algorithm, publicKey.getAlgorithm())
            );
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
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
            this.publicKey = keyFactory.generatePublic(keySpec);
            return this;
        } catch (Exception e) {
            throw new OpenKeyException("Failed to decode EdDSA public key", e);
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
     * Check if key algorithm is valid for this EdDSA signature
     * 检查密钥算法是否对此 EdDSA 签名有效
     * <p>
     * JDK versions may report the algorithm as either "Ed25519"/"Ed448" or generic "EdDSA".
     * JDK 版本可能将算法报告为 "Ed25519"/"Ed448" 或通用的 "EdDSA"。
     *
     * @param keyAlgorithm the key's reported algorithm
     * @return true if the key algorithm is compatible
     */
    private boolean isValidEdDsaKey(String keyAlgorithm) {
        // Accept exact match
        if (algorithm.equals(keyAlgorithm)) {
            return true;
        }
        // JDK 25+ may report EdDSA keys with generic "EdDSA" algorithm name
        if ("EdDSA".equals(keyAlgorithm)) {
            return true;
        }
        return false;
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
