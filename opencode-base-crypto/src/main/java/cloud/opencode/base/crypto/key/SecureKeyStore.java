package cloud.opencode.base.crypto.key;

import cloud.opencode.base.crypto.exception.OpenKeyException;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Secure key store for managing cryptographic keys - Store and retrieve keys securely using PKCS12 format
 * 安全密钥存储 - 使用 PKCS12 格式安全地存储和检索密钥
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>In-memory secure key storage - 内存安全密钥存储</li>
 *   <li>Key lifecycle management - 密钥生命周期管理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SecureKeyStore store = SecureKeyStore.create();
 * store.store("myKey", secretKey);
 * SecretKey key = store.load("myKey");
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
public final class SecureKeyStore implements AutoCloseable {

    private static final String KEYSTORE_TYPE = "PKCS12";
    private final KeyStore keyStore;

    /**
     * Private constructor - use factory methods to create instances
     * 私有构造函数 - 使用工厂方法创建实例
     *
     * @param keyStore the underlying KeyStore
     */
    private SecureKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    /**
     * Create a new empty key store
     * 创建新的空密钥存储
     *
     * @return new SecureKeyStore instance
     * @throws OpenKeyException if creation fails
     */
    public static SecureKeyStore create() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            keyStore.load(null, null);
            return new SecureKeyStore(keyStore);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new OpenKeyException("Failed to create key store", e);
        }
    }

    /**
     * Load key store from file
     * 从文件加载密钥存储
     *
     * @param path     path to key store file
     * @param password password to unlock key store
     * @return loaded SecureKeyStore instance
     * @throws OpenKeyException if loading fails
     */
    public static SecureKeyStore load(Path path, char[] password) {
        if (path == null) {
            throw new OpenKeyException("Path cannot be null");
        }
        if (password == null) {
            throw new OpenKeyException("Password cannot be null");
        }
        if (!Files.exists(path)) {
            throw new OpenKeyException("Key store file does not exist: " + path);
        }

        char[] pwCopy = password.clone();
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            try (InputStream input = Files.newInputStream(path)) {
                keyStore.load(input, pwCopy);
            }
            return new SecureKeyStore(keyStore);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new OpenKeyException("Failed to load key store from: " + path, e);
        } finally {
            Arrays.fill(pwCopy, '\0');
        }
    }

    /**
     * Store secret key in key store
     * 在密钥存储中存储对称密钥
     *
     * @param alias    alias for the key
     * @param key      secret key to store
     * @param password password to protect the key
     * @throws OpenKeyException if storing fails
     */
    public void store(String alias, SecretKey key, char[] password) {
        if (alias == null || alias.isEmpty()) {
            throw new OpenKeyException("Alias cannot be null or empty");
        }
        if (key == null) {
            throw new OpenKeyException("Secret key cannot be null");
        }
        if (password == null) {
            throw new OpenKeyException("Password cannot be null");
        }

        char[] pwCopy = password.clone();
        KeyStore.PasswordProtection protection = null;
        try {
            KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(key);
            protection = new KeyStore.PasswordProtection(pwCopy);
            keyStore.setEntry(alias, entry, protection);
        } catch (KeyStoreException e) {
            throw new OpenKeyException("Failed to store secret key with alias: " + alias, e);
        } finally {
            Arrays.fill(pwCopy, '\0');
            if (protection != null) {
                // DestroyFailedException is expected for some JCE providers
                try { protection.destroy(); } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Store key pair in key store
     * 在密钥存储中存储密钥对
     *
     * @param alias    alias for the key pair
     * @param keyPair  key pair to store
     * @param password password to protect the private key
     * @throws OpenKeyException if storing fails
     */
    public void store(String alias, KeyPair keyPair, char[] password) {
        if (keyPair == null) {
            throw new OpenKeyException("Key pair cannot be null");
        }

        // Create a self-signed certificate for the public key
        Certificate[] chain = createSelfSignedCertificate(keyPair);
        store(alias, keyPair.getPrivate(), password, chain);
    }

    /**
     * Store private key with certificate chain in key store
     * 在密钥存储中存储带证书链的私钥
     *
     * @param alias      alias for the key
     * @param privateKey private key to store
     * @param password   password to protect the private key
     * @param chain      certificate chain
     * @throws OpenKeyException if storing fails
     */
    public void store(String alias, PrivateKey privateKey, char[] password, Certificate[] chain) {
        if (alias == null || alias.isEmpty()) {
            throw new OpenKeyException("Alias cannot be null or empty");
        }
        if (privateKey == null) {
            throw new OpenKeyException("Private key cannot be null");
        }
        if (password == null) {
            throw new OpenKeyException("Password cannot be null");
        }
        if (chain == null || chain.length == 0) {
            throw new OpenKeyException("Certificate chain cannot be null or empty");
        }

        char[] pwCopy = password.clone();
        try {
            keyStore.setKeyEntry(alias, privateKey, pwCopy, chain);
        } catch (KeyStoreException e) {
            throw new OpenKeyException("Failed to store private key with alias: " + alias, e);
        } finally {
            Arrays.fill(pwCopy, '\0');
        }
    }

    /**
     * Get secret key from key store
     * 从密钥存储中获取对称密钥
     *
     * @param alias    alias of the key
     * @param password password to unlock the key
     * @return secret key
     * @throws OpenKeyException if retrieval fails or key not found
     */
    public SecretKey getSecretKey(String alias, char[] password) {
        if (alias == null || alias.isEmpty()) {
            throw new OpenKeyException("Alias cannot be null or empty");
        }
        if (password == null) {
            throw new OpenKeyException("Password cannot be null");
        }

        char[] pwCopy = password.clone();
        KeyStore.PasswordProtection protection = null;
        try {
            protection = new KeyStore.PasswordProtection(pwCopy);
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, protection);
            if (entry == null) {
                throw new OpenKeyException("Secret key not found for alias: " + alias);
            }
            return entry.getSecretKey();
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
            throw new OpenKeyException("Failed to retrieve secret key with alias: " + alias, e);
        } finally {
            Arrays.fill(pwCopy, '\0');
            if (protection != null) {
                // DestroyFailedException is expected for some JCE providers
                try { protection.destroy(); } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Get key pair from key store
     * 从密钥存储中获取密钥对
     *
     * @param alias    alias of the key pair
     * @param password password to unlock the private key
     * @return key pair
     * @throws OpenKeyException if retrieval fails or key not found
     */
    public KeyPair getKeyPair(String alias, char[] password) {
        PrivateKey privateKey = getPrivateKey(alias, password);
        PublicKey publicKey = getPublicKey(alias);
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Get private key from key store
     * 从密钥存储中获取私钥
     *
     * @param alias    alias of the key
     * @param password password to unlock the private key
     * @return private key
     * @throws OpenKeyException if retrieval fails or key not found
     */
    public PrivateKey getPrivateKey(String alias, char[] password) {
        if (alias == null || alias.isEmpty()) {
            throw new OpenKeyException("Alias cannot be null or empty");
        }
        if (password == null) {
            throw new OpenKeyException("Password cannot be null");
        }

        char[] pwCopy = password.clone();
        try {
            Key key = keyStore.getKey(alias, pwCopy);
            if (key == null) {
                throw new OpenKeyException("Private key not found for alias: " + alias);
            }
            if (!(key instanceof PrivateKey)) {
                throw new OpenKeyException("Entry is not a private key for alias: " + alias);
            }
            return (PrivateKey) key;
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new OpenKeyException("Failed to retrieve private key with alias: " + alias, e);
        } finally {
            Arrays.fill(pwCopy, '\0');
        }
    }

    /**
     * Get public key from key store
     * 从密钥存储中获取公钥
     *
     * @param alias alias of the key
     * @return public key
     * @throws OpenKeyException if retrieval fails or key not found
     */
    public PublicKey getPublicKey(String alias) {
        if (alias == null || alias.isEmpty()) {
            throw new OpenKeyException("Alias cannot be null or empty");
        }

        try {
            Certificate cert = keyStore.getCertificate(alias);
            if (cert == null) {
                throw new OpenKeyException("Certificate not found for alias: " + alias);
            }
            return cert.getPublicKey();
        } catch (KeyStoreException e) {
            throw new OpenKeyException("Failed to retrieve public key with alias: " + alias, e);
        }
    }

    /**
     * Check if key store contains entry with given alias
     * 检查密钥存储是否包含给定别名的条目
     *
     * @param alias alias to check
     * @return true if alias exists
     * @throws OpenKeyException if check fails
     */
    public boolean containsAlias(String alias) {
        if (alias == null || alias.isEmpty()) {
            throw new OpenKeyException("Alias cannot be null or empty");
        }

        try {
            return keyStore.containsAlias(alias);
        } catch (KeyStoreException e) {
            throw new OpenKeyException("Failed to check alias: " + alias, e);
        }
    }

    /**
     * Delete entry from key store
     * 从密钥存储中删除条目
     *
     * @param alias alias of the entry to delete
     * @throws OpenKeyException if deletion fails
     */
    public void deleteEntry(String alias) {
        if (alias == null || alias.isEmpty()) {
            throw new OpenKeyException("Alias cannot be null or empty");
        }

        try {
            keyStore.deleteEntry(alias);
        } catch (KeyStoreException e) {
            throw new OpenKeyException("Failed to delete entry with alias: " + alias, e);
        }
    }

    /**
     * Get all aliases in key store
     * 获取密钥存储中的所有别名
     *
     * @return set of aliases
     * @throws OpenKeyException if retrieval fails
     */
    public Set<String> aliases() {
        try {
            Set<String> result = new HashSet<>();
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                result.add(aliases.nextElement());
            }
            return result;
        } catch (KeyStoreException e) {
            throw new OpenKeyException("Failed to retrieve aliases", e);
        }
    }

    /**
     * Save key store to file
     * 保存密钥存储到文件
     *
     * @param path     path to save the key store
     * @param password password to protect the key store
     * @throws OpenKeyException if saving fails
     */
    public void save(Path path, char[] password) {
        if (path == null) {
            throw new OpenKeyException("Path cannot be null");
        }
        if (password == null) {
            throw new OpenKeyException("Password cannot be null");
        }

        char[] pwCopy = password.clone();
        try {
            // Create parent directories if they don't exist
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            try (OutputStream output = Files.newOutputStream(path)) {
                keyStore.store(output, pwCopy);
            }
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new OpenKeyException("Failed to save key store to: " + path, e);
        } finally {
            Arrays.fill(pwCopy, '\0');
        }
    }

    /**
     * Close the key store and clear sensitive data
     * 关闭密钥存储并清除敏感数据
     */
    @Override
    public void close() {
        // KeyStore doesn't require explicit cleanup
        // This method is provided for future-proofing and consistency with AutoCloseable
    }

    /**
     * Create a self-signed certificate for a key pair
     * 为密钥对创建自签名证书
     *
     * @param keyPair key pair
     * @return certificate chain containing the self-signed certificate
     */
    private Certificate[] createSelfSignedCertificate(KeyPair keyPair) {
        // For simplicity, we're creating a dummy certificate
        // In production, you should use proper certificate generation
        try {
            // Use the public key's encoding as a simple certificate placeholder
            // This is a workaround since creating proper X.509 certificates requires
            // external libraries like BouncyCastle
            java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");

            // Create a minimal certificate structure
            // Note: This is a simplified approach and may not work for all use cases
            // For production use, consider using BouncyCastle or similar library
            throw new OpenKeyException("Self-signed certificate creation requires BouncyCastle library. " +
                "Please use store(alias, privateKey, password, chain) with a proper certificate chain.");
        } catch (CertificateException e) {
            throw new OpenKeyException("Failed to create self-signed certificate", e);
        }
    }
}
