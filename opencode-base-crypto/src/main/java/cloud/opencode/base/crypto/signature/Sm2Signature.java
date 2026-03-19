package cloud.opencode.base.crypto.signature;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.codec.PemCodec;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
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
 * SM2 signature implementation (Chinese national cryptographic standard).
 * SM2 签名实现（中国国家密码标准）。
 *
 * <p>Requires Bouncy Castle provider for SM2 support.
 * 需要 Bouncy Castle 提供商支持 SM2。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SM2 signatures (Chinese national standard) - SM2 签名（中国国密标准）</li>
 *   <li>Requires Bouncy Castle provider - 需要 Bouncy Castle 提供者</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Sm2Signature sm2 = Sm2Signature.create();
 * sm2.setPrivateKey(privateKey);
 * byte[] sig = sm2.sign(data);
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
public final class Sm2Signature implements SignatureEngine {

    private static final String ALGORITHM = "SM3withSM2";
    private static final String KEY_ALGORITHM = "SM2";
    private static final String PROVIDER = "BC";
    private static final int BUFFER_SIZE = 8192;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private Signature signatureInstance;

    private Sm2Signature() {
        ensureBouncyCastleAvailable();
    }

    /**
     * Create SM2 signature instance.
     * 创建 SM2 签名实例。
     *
     * @return SM2 signature instance / SM2 签名实例
     */
    public static Sm2Signature create() {
        return new Sm2Signature();
    }

    /**
     * Check if Bouncy Castle provider is available.
     * 检查 Bouncy Castle 提供商是否可用。
     *
     * @return true if available / 如果可用则返回 true
     */
    public static boolean isBouncyCastleAvailable() {
        try {
            Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void ensureBouncyCastleAvailable() {
        if (!isBouncyCastleAvailable()) {
            throw new OpenCryptoException(
                "SM2 requires Bouncy Castle provider. Add dependency: org.bouncycastle:bcprov-jdk18on"
            );
        }
        // Ensure BC provider is registered
        if (Security.getProvider(PROVIDER) == null) {
            try {
                Class<?> bcClass = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
                Security.addProvider((Provider) bcClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                throw new OpenCryptoException("Failed to register Bouncy Castle provider", e);
            }
        }
    }

    @Override
    public SignatureEngine setPrivateKey(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new NullPointerException("Private key cannot be null");
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
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM, PROVIDER);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
            this.privateKey = keyFactory.generatePrivate(keySpec);
            return this;
        } catch (Exception e) {
            throw new OpenKeyException("Failed to decode SM2 private key", e);
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
        this.publicKey = publicKey;
        return this;
    }

    @Override
    public SignatureEngine setPublicKey(byte[] encodedKey) {
        if (encodedKey == null) {
            throw new NullPointerException("Encoded key cannot be null");
        }
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM, PROVIDER);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
            this.publicKey = keyFactory.generatePublic(keySpec);
            return this;
        } catch (Exception e) {
            throw new OpenKeyException("Failed to decode SM2 public key", e);
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
            Signature signature = Signature.getInstance(ALGORITHM, PROVIDER);
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw OpenSignatureException.signFailed(ALGORITHM, e);
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
            Signature signature = Signature.getInstance(ALGORITHM, PROVIDER);
            signature.initSign(privateKey);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                signature.update(buffer, 0, bytesRead);
            }
            return signature.sign();
        } catch (Exception e) {
            throw OpenSignatureException.signFailed(ALGORITHM, e);
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
            Signature sig = Signature.getInstance(ALGORITHM, PROVIDER);
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(signature);
        } catch (Exception e) {
            throw OpenSignatureException.verifyFailed(ALGORITHM, e);
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
            Signature sig = Signature.getInstance(ALGORITHM, PROVIDER);
            sig.initVerify(publicKey);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                sig.update(buffer, 0, bytesRead);
            }
            return sig.verify(signature);
        } catch (Exception e) {
            throw OpenSignatureException.verifyFailed(ALGORITHM, e);
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
            signatureInstance = null;
            return result;
        } catch (SignatureException e) {
            signatureInstance = null;
            throw OpenSignatureException.signFailed(ALGORITHM, e);
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
            signatureInstance = null;
            return result;
        } catch (SignatureException e) {
            signatureInstance = null;
            throw OpenSignatureException.verifyFailed(ALGORITHM, e);
        }
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }

    private void ensureSignatureInstance() {
        if (signatureInstance == null) {
            try {
                signatureInstance = Signature.getInstance(ALGORITHM, PROVIDER);
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
