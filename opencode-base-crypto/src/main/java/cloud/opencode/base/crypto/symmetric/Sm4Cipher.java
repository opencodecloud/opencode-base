package cloud.opencode.base.crypto.symmetric;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import cloud.opencode.base.crypto.random.NonceGenerator;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

/**
 * SM4 cipher implementation (Chinese national cryptographic standard).
 * SM4 加密实现（中国国家密码标准）。
 *
 * <p>Requires Bouncy Castle provider for SM4 support.
 * 需要 Bouncy Castle 提供商支持 SM4。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SM4-GCM and SM4-CBC modes - SM4-GCM 和 SM4-CBC 模式</li>
 *   <li>Chinese national standard (GB/T 32907-2016) - 中国国密标准（GB/T 32907-2016）</li>
 *   <li>Requires Bouncy Castle provider - 需要 Bouncy Castle 提供者</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AeadCipher sm4 = Sm4Cipher.gcm();
 * byte[] encrypted = sm4.encrypt(plaintext, key);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Partial - 空值安全: 部分</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) - 时间复杂度: O(n)，n为明文长度</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class Sm4Cipher implements SymmetricCipher, AeadCipher {

    private static final String ALGORITHM = "SM4";
    private static final int KEY_SIZE = 128; // SM4 uses 128-bit keys
    private static final int BLOCK_SIZE = 16; // 128 bits
    private static final int GCM_IV_LENGTH = 12; // 96 bits for GCM
    private static final int CBC_IV_LENGTH = 16; // 128 bits for CBC
    private static final int DEFAULT_TAG_LENGTH = 128; // 128 bits

    private SecretKey key;
    private byte[] iv;
    private byte[] aad;
    private CipherMode mode = CipherMode.CBC;
    private Padding padding = Padding.PKCS7;
    private int tagLengthBits = DEFAULT_TAG_LENGTH;
    private final SecureRandom random = new SecureRandom();

    private Sm4Cipher(CipherMode mode) {
        this.mode = mode;
        ensureBouncyCastleAvailable();
    }

    /**
     * Create SM4 cipher in CBC mode.
     * 创建 CBC 模式的 SM4 加密器。
     *
     * @return SM4-CBC cipher instance / SM4-CBC 加密实例
     */
    public static Sm4Cipher cbc() {
        return new Sm4Cipher(CipherMode.CBC);
    }

    /**
     * Create SM4 cipher in GCM mode (AEAD).
     * 创建 GCM 模式的 SM4 加密器（AEAD）。
     *
     * @return SM4-GCM cipher instance / SM4-GCM 加密实例
     */
    public static Sm4Cipher gcm() {
        return new Sm4Cipher(CipherMode.GCM);
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
                "SM4 requires Bouncy Castle provider. Add dependency: org.bouncycastle:bcprov-jdk18on"
            );
        }
        // Ensure BC provider is registered
        if (Security.getProvider("BC") == null) {
            try {
                Class<?> bcClass = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
                Security.addProvider((Provider) bcClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                throw new OpenCryptoException("Failed to register Bouncy Castle provider", e);
            }
        }
    }

    @Override
    public Sm4Cipher setKey(SecretKey key) {
        this.key = key;
        return this;
    }

    @Override
    public Sm4Cipher setKey(byte[] key) {
        if (key == null || key.length != 16) {
            throw new OpenKeyException("SM4 key must be 128 bits (16 bytes)");
        }
        this.key = new SecretKeySpec(key, ALGORITHM);
        return this;
    }

    @Override
    public Sm4Cipher setIv(byte[] iv) {
        int expectedLength = (mode == CipherMode.GCM) ? GCM_IV_LENGTH : CBC_IV_LENGTH;
        if (iv != null && iv.length != expectedLength) {
            throw new OpenCryptoException("SM4-" + mode + " IV must be " + expectedLength + " bytes");
        }
        this.iv = iv;
        return this;
    }

    @Override
    public Sm4Cipher setNonce(byte[] nonce) {
        return setIv(nonce);
    }

    @Override
    public Sm4Cipher setAad(byte[] aad) {
        if (mode != CipherMode.GCM) {
            throw new OpenCryptoException("AAD only supported in GCM mode");
        }
        this.aad = aad;
        return this;
    }

    @Override
    public Sm4Cipher setMode(CipherMode mode) {
        if (mode != CipherMode.CBC && mode != CipherMode.GCM && mode != CipherMode.ECB) {
            throw new OpenCryptoException("SM4 only supports CBC, GCM, and ECB modes");
        }
        this.mode = mode;
        return this;
    }

    @Override
    public Sm4Cipher setPadding(Padding padding) {
        if (mode == CipherMode.GCM) {
            throw new OpenCryptoException("GCM mode does not use padding");
        }
        this.padding = padding;
        return this;
    }

    @Override
    public Sm4Cipher setTagLength(int tagBits) {
        if (mode != CipherMode.GCM) {
            throw new OpenCryptoException("Tag length only applicable in GCM mode");
        }
        if (tagBits != 96 && tagBits != 104 && tagBits != 112 && tagBits != 120 && tagBits != 128) {
            throw new OpenCryptoException("SM4-GCM tag length must be 96, 104, 112, 120, or 128 bits");
        }
        this.tagLengthBits = tagBits;
        return this;
    }

    @Override
    public byte[] encrypt(byte[] plaintext) {
        try {
            if (key == null) {
                key = generateKey();
            }

            Cipher cipher = Cipher.getInstance(getTransformation(), "BC");

            if (mode == CipherMode.ECB) {
                cipher.init(Cipher.ENCRYPT_MODE, key);
                return cipher.doFinal(plaintext);
            }

            // Always generate a fresh IV for each encryption to prevent nonce reuse
            byte[] encIv = generateIv();
            this.iv = encIv;

            if (mode == CipherMode.GCM) {
                GCMParameterSpec spec = new GCMParameterSpec(tagLengthBits, encIv);
                cipher.init(Cipher.ENCRYPT_MODE, key, spec);
                if (aad != null) {
                    cipher.updateAAD(aad);
                }
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(encIv));
            }

            byte[] ciphertext = cipher.doFinal(plaintext);
            // Prepend IV to ciphertext so decrypt() can extract it
            byte[] result = new byte[encIv.length + ciphertext.length];
            System.arraycopy(encIv, 0, result, 0, encIv.length);
            System.arraycopy(ciphertext, 0, result, encIv.length, ciphertext.length);
            return result;
        } catch (Exception e) {
            throw new OpenCryptoException("SM4 encryption failed", e);
        }
    }

    @Override
    public byte[] encrypt(String plaintext) {
        return encrypt(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String encryptBase64(byte[] plaintext) {
        return OpenBase64.encode(encrypt(plaintext));
    }

    @Override
    public String encryptBase64(String plaintext) {
        return OpenBase64.encode(encrypt(plaintext));
    }

    @Override
    public String encryptHex(byte[] plaintext) {
        return HexCodec.encode(encrypt(plaintext));
    }

    @Override
    public void encryptFile(Path source, Path target) {
        try {
            if (key == null) {
                key = generateKey();
            }

            Cipher cipher = Cipher.getInstance(getTransformation(), "BC");

            if (mode == CipherMode.ECB) {
                cipher.init(Cipher.ENCRYPT_MODE, key);
                try (var in = Files.newInputStream(source);
                     var out = Files.newOutputStream(target);
                     var cout = new CipherOutputStream(out, cipher)) {
                    in.transferTo(cout);
                }
                return;
            }

            // Always generate a fresh IV for each encryption
            byte[] encIv = generateIv();
            this.iv = encIv;

            if (mode == CipherMode.GCM) {
                GCMParameterSpec spec = new GCMParameterSpec(tagLengthBits, encIv);
                cipher.init(Cipher.ENCRYPT_MODE, key, spec);
                if (aad != null) {
                    cipher.updateAAD(aad);
                }
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(encIv));
            }

            try (var in = Files.newInputStream(source);
                 var out = Files.newOutputStream(target);
                 var cout = new CipherOutputStream(out, cipher)) {
                // Prepend IV to output
                out.write(encIv);
                in.transferTo(cout);
            }
        } catch (Exception e) {
            throw new OpenCryptoException("SM4 file encryption failed", e);
        }
    }

    @Override
    public OutputStream encryptStream(OutputStream output) {
        try {
            if (key == null) {
                key = generateKey();
            }

            Cipher cipher = Cipher.getInstance(getTransformation(), "BC");

            if (mode == CipherMode.ECB) {
                cipher.init(Cipher.ENCRYPT_MODE, key);
                return new CipherOutputStream(output, cipher);
            }

            // Always generate a fresh IV for each encryption
            byte[] encIv = generateIv();
            this.iv = encIv;

            if (mode == CipherMode.GCM) {
                GCMParameterSpec spec = new GCMParameterSpec(tagLengthBits, encIv);
                cipher.init(Cipher.ENCRYPT_MODE, key, spec);
                if (aad != null) {
                    cipher.updateAAD(aad);
                }
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(encIv));
            }

            // Prepend IV to output
            output.write(encIv);
            return new CipherOutputStream(output, cipher);
        } catch (Exception e) {
            throw new OpenCryptoException("SM4 stream encryption failed", e);
        }
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) {
        try {
            if (key == null) {
                throw new OpenKeyException("Key not set");
            }

            Cipher cipher = Cipher.getInstance(getTransformation(), "BC");

            if (mode == CipherMode.ECB) {
                cipher.init(Cipher.DECRYPT_MODE, key);
                return cipher.doFinal(ciphertext);
            }

            // Extract IV from the beginning of ciphertext
            int ivLen = getIvLength();
            if (ciphertext.length < ivLen) {
                throw new OpenCryptoException("Ciphertext too short: missing IV");
            }
            byte[] decIv = new byte[ivLen];
            System.arraycopy(ciphertext, 0, decIv, 0, ivLen);
            this.iv = decIv;

            byte[] encryptedData = new byte[ciphertext.length - ivLen];
            System.arraycopy(ciphertext, ivLen, encryptedData, 0, encryptedData.length);

            if (mode == CipherMode.GCM) {
                GCMParameterSpec spec = new GCMParameterSpec(tagLengthBits, decIv);
                cipher.init(Cipher.DECRYPT_MODE, key, spec);
                if (aad != null) {
                    cipher.updateAAD(aad);
                }
            } else {
                cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(decIv));
            }

            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw new OpenCryptoException("SM4 decryption failed", e);
        }
    }

    @Override
    public String decryptToString(byte[] ciphertext) {
        return new String(decrypt(ciphertext), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] decryptBase64(String base64Ciphertext) {
        return decrypt(OpenBase64.decode(base64Ciphertext));
    }

    @Override
    public String decryptBase64ToString(String base64Ciphertext) {
        return decryptToString(OpenBase64.decode(base64Ciphertext));
    }

    @Override
    public byte[] decryptHex(String hexCiphertext) {
        return decrypt(HexCodec.decode(hexCiphertext));
    }

    @Override
    public void decryptFile(Path source, Path target) {
        try {
            if (key == null) {
                throw new OpenKeyException("Key not set");
            }

            Cipher cipher = Cipher.getInstance(getTransformation(), "BC");

            if (mode == CipherMode.ECB) {
                cipher.init(Cipher.DECRYPT_MODE, key);
                try (var in = Files.newInputStream(source);
                     var cin = new CipherInputStream(in, cipher);
                     var out = Files.newOutputStream(target)) {
                    cin.transferTo(out);
                }
                return;
            }

            try (var in = Files.newInputStream(source);
                 var out = Files.newOutputStream(target)) {
                // Extract IV from the beginning of the file
                int ivLen = getIvLength();
                byte[] decIv = new byte[ivLen];
                int read = in.readNBytes(decIv, 0, ivLen);
                if (read < ivLen) {
                    throw new OpenCryptoException("File too short: missing IV");
                }
                this.iv = decIv;

                if (mode == CipherMode.GCM) {
                    GCMParameterSpec spec = new GCMParameterSpec(tagLengthBits, decIv);
                    cipher.init(Cipher.DECRYPT_MODE, key, spec);
                    if (aad != null) {
                        cipher.updateAAD(aad);
                    }
                } else {
                    cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(decIv));
                }

                try (var cin = new CipherInputStream(in, cipher)) {
                    cin.transferTo(out);
                }
            }
        } catch (Exception e) {
            throw new OpenCryptoException("SM4 file decryption failed", e);
        }
    }

    @Override
    public InputStream decryptStream(InputStream input) {
        try {
            if (key == null) {
                throw new OpenKeyException("Key not set");
            }

            Cipher cipher = Cipher.getInstance(getTransformation(), "BC");

            if (mode == CipherMode.ECB) {
                cipher.init(Cipher.DECRYPT_MODE, key);
                return new CipherInputStream(input, cipher);
            }

            // Extract IV from the beginning of the stream
            int ivLen = getIvLength();
            byte[] decIv = new byte[ivLen];
            int read = input.readNBytes(decIv, 0, ivLen);
            if (read < ivLen) {
                throw new OpenCryptoException("Stream too short: missing IV");
            }
            this.iv = decIv;

            if (mode == CipherMode.GCM) {
                GCMParameterSpec spec = new GCMParameterSpec(tagLengthBits, decIv);
                cipher.init(Cipher.DECRYPT_MODE, key, spec);
                if (aad != null) {
                    cipher.updateAAD(aad);
                }
            } else {
                cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(decIv));
            }

            return new CipherInputStream(input, cipher);
        } catch (Exception e) {
            throw new OpenCryptoException("SM4 stream decryption failed", e);
        }
    }

    @Override
    public byte[] generateIv() {
        int length = (mode == CipherMode.GCM) ? GCM_IV_LENGTH : CBC_IV_LENGTH;
        return NonceGenerator.random(length);
    }

    @Override
    public byte[] generateNonce() {
        return generateIv();
    }

    @Override
    public int getBlockSize() {
        return BLOCK_SIZE;
    }

    @Override
    public int getIvLength() {
        return (mode == CipherMode.GCM) ? GCM_IV_LENGTH : CBC_IV_LENGTH;
    }

    @Override
    public String getAlgorithm() {
        return "SM4-" + mode.name();
    }

    @Override
    public SecretKey generateKey(int keySize) {
        if (keySize != KEY_SIZE) {
            throw new OpenKeyException("SM4 only supports 128-bit keys");
        }
        return generateKey();
    }

    /**
     * Get current IV.
     * 获取当前初始化向量。
     *
     * @return IV bytes / 初始化向量字节
     */
    public byte[] getIv() {
        return iv == null ? null : iv.clone();
    }

    /**
     * Get current key.
     * 获取当前密钥。
     *
     * @return secret key / 密钥
     */
    public SecretKey getKey() {
        if (key == null) return null;
        return new SecretKeySpec(key.getEncoded().clone(), key.getAlgorithm());
    }

    private String getTransformation() {
        if (mode == CipherMode.GCM) {
            return ALGORITHM + "/GCM/NoPadding";
        }
        return ALGORITHM + "/" + mode.name() + "/" + padding.getValue();
    }

    private SecretKey generateKey() {
        byte[] keyBytes = new byte[KEY_SIZE / 8];
        random.nextBytes(keyBytes);
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
}
