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
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA-PSS signature implementation - RSA probabilistic signature scheme (recommended)
 * RSA-PSS 签名实现 - RSA 概率签名方案（推荐）
 * <p>
 * RSA-PSS is a more secure RSA signature scheme than PKCS#1 v1.5. It provides
 * provable security and is recommended for new applications.
 * RSA-PSS 是比 PKCS#1 v1.5 更安全的 RSA 签名方案。它提供可证明的安全性，推荐用于新应用。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RSA-PSS signatures with SHA-256/384/512 - RSA-PSS 签名（SHA-256/384/512）</li>
 *   <li>Probabilistic signature scheme - 概率签名方案</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RsaPssSignature pss = RsaPssSignature.sha256();
 * pss.setPrivateKey(privateKey);
 * byte[] sig = pss.sign(data);
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
public final class RsaPssSignature implements SignatureEngine {

    private static final String KEY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "RSASSA-PSS";
    private static final int DEFAULT_KEY_SIZE = 2048;
    private static final int BUFFER_SIZE = 8192;

    private final String hashAlgorithm;
    private final PSSParameterSpec pssSpec;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private Signature signatureInstance;

    private RsaPssSignature(String hashAlgorithm, PSSParameterSpec pssSpec) {
        this.hashAlgorithm = hashAlgorithm;
        this.pssSpec = pssSpec;
    }

    /**
     * Create RSA-PSS signature with SHA-256 (2048-bit key recommended)
     * 创建使用 SHA-256 的 RSA-PSS 签名（推荐 2048 位密钥）
     *
     * @return RSA-PSS with SHA-256 instance
     */
    public static RsaPssSignature sha256() {
        PSSParameterSpec spec = new PSSParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1
        );
        return new RsaPssSignature("SHA-256", spec);
    }

    /**
     * Create RSA-PSS signature with SHA-384 (3072-bit key recommended)
     * 创建使用 SHA-384 的 RSA-PSS 签名（推荐 3072 位密钥）
     *
     * @return RSA-PSS with SHA-384 instance
     */
    public static RsaPssSignature sha384() {
        PSSParameterSpec spec = new PSSParameterSpec(
            "SHA-384", "MGF1", MGF1ParameterSpec.SHA384, 48, 1
        );
        return new RsaPssSignature("SHA-384", spec);
    }

    /**
     * Create RSA-PSS signature with SHA-512 (4096-bit key recommended)
     * 创建使用 SHA-512 的 RSA-PSS 签名（推荐 4096 位密钥）
     *
     * @return RSA-PSS with SHA-512 instance
     */
    public static RsaPssSignature sha512() {
        PSSParameterSpec spec = new PSSParameterSpec(
            "SHA-512", "MGF1", MGF1ParameterSpec.SHA512, 64, 1
        );
        return new RsaPssSignature("SHA-512", spec);
    }

    /**
     * Create RSA-PSS signature with SHA-256 and generated 2048-bit key pair
     * 创建使用 SHA-256 和生成的 2048 位密钥对的 RSA-PSS 签名
     *
     * @return RSA-PSS with SHA-256 and generated keys
     */
    public static RsaPssSignature sha256WithKeyPair() {
        return sha256().withGeneratedKeyPair(DEFAULT_KEY_SIZE);
    }

    /**
     * Create RSA-PSS signature with SHA-512 and generated 4096-bit key pair
     * 创建使用 SHA-512 和生成的 4096 位密钥对的 RSA-PSS 签名
     *
     * @return RSA-PSS with SHA-512 and generated keys
     */
    public static RsaPssSignature sha512WithKeyPair() {
        return sha512().withGeneratedKeyPair(4096);
    }

    /**
     * Generate a new RSA key pair of specified size
     * 生成指定大小的新 RSA 密钥对
     *
     * @param keySize the key size in bits (minimum 2048 recommended)
     * @return this signature instance with generated keys
     * @throws IllegalArgumentException if keySize is invalid
     */
    public RsaPssSignature withGeneratedKeyPair(int keySize) {
        if (keySize < 2048) {
            throw new IllegalArgumentException("RSA key size should be at least 2048 bits");
        }
        if (keySize % 1024 != 0) {
            throw new IllegalArgumentException("RSA key size must be a multiple of 1024");
        }

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            generator.initialize(keySize);
            KeyPair keyPair = generator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
            return this;
        } catch (NoSuchAlgorithmException e) {
            throw new OpenKeyException("RSA algorithm not available", e);
        }
    }

    @Override
    public SignatureEngine setPrivateKey(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new NullPointerException("Private key cannot be null");
        }
        if (!KEY_ALGORITHM.equals(privateKey.getAlgorithm())) {
            throw new IllegalArgumentException("Key must be RSA key");
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
            throw new OpenKeyException("Failed to decode RSA private key", e);
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
            throw new IllegalArgumentException("Key must be RSA key");
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
            throw new OpenKeyException("Failed to decode RSA public key", e);
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
            Signature signature = createSignatureInstance();
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw OpenSignatureException.signFailed(SIGNATURE_ALGORITHM, e);
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
            Signature signature = createSignatureInstance();
            signature.initSign(privateKey);

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                signature.update(buffer, 0, bytesRead);
            }

            return signature.sign();
        } catch (Exception e) {
            throw OpenSignatureException.signFailed(SIGNATURE_ALGORITHM, e);
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
            Signature sig = createSignatureInstance();
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(signature);
        } catch (Exception e) {
            throw OpenSignatureException.verifyFailed(SIGNATURE_ALGORITHM, e);
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
            Signature sig = createSignatureInstance();
            sig.initVerify(publicKey);

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                sig.update(buffer, 0, bytesRead);
            }

            return sig.verify(signature);
        } catch (Exception e) {
            throw OpenSignatureException.verifyFailed(SIGNATURE_ALGORITHM, e);
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
            throw OpenSignatureException.signFailed(SIGNATURE_ALGORITHM, e);
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
            throw OpenSignatureException.verifyFailed(SIGNATURE_ALGORITHM, e);
        }
    }

    @Override
    public String getAlgorithm() {
        return SIGNATURE_ALGORITHM + " (" + hashAlgorithm + ")";
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
     * Create a new Signature instance with PSS parameters
     * 创建带有 PSS 参数的新签名实例
     */
    private Signature createSignatureInstance() {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.setParameter(pssSpec);
            return signature;
        } catch (Exception e) {
            throw new OpenSignatureException("Failed to create signature instance", e);
        }
    }

    /**
     * Ensure signature instance is initialized for multi-part operations
     * 确保签名实例已初始化用于多部分操作
     */
    private void ensureSignatureInstance() {
        if (signatureInstance == null) {
            try {
                signatureInstance = createSignatureInstance();
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
