package cloud.opencode.base.crypto.ssl;

import cloud.opencode.base.crypto.exception.OpenCryptoException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * SSL Context Builder - Fluent SSL Context Configuration
 * SSL 上下文构建器 - 流式 SSL 上下文配置
 *
 * <p>This builder provides a fluent API for creating SSLContext instances
 * with various configurations including keystores, truststores, and protocols.</p>
 * <p>此构建器提供流式 API 来创建带有各种配置的 SSLContext 实例，
 * 包括密钥库、信任库和协议。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * SSLContext sslContext = SslContextBuilder.create()
 *     .keyStore(Path.of("/path/to/keystore.p12"), "password")
 *     .trustStore(Path.of("/path/to/truststore.jks"), "password")
 *     .protocol("TLSv1.3")
 *     .build();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent SSLContext configuration - 流式 SSLContext 配置</li>
 *   <li>Keystore and truststore loading - 密钥库和信任库加载</li>
 *   <li>PEM certificate support - PEM 证书支持</li>
 *   <li>Mutual TLS (mTLS) configuration - 双向 TLS（mTLS）配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SSLContext ctx = SslContextBuilder.create()
 *     .keyStore(Path.of("keystore.p12"), "password")
 *     .trustStore(Path.of("truststore.jks"), "password")
 *     .protocol("TLSv1.3")
 *     .build();
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
 *   <li>Time complexity: O(1) - 时间复杂度: O(1)</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class SslContextBuilder {

    private static final String DEFAULT_PROTOCOL = "TLS";
    private static final String DEFAULT_KEYSTORE_TYPE = "PKCS12";

    private String protocol = DEFAULT_PROTOCOL;
    private KeyManager[] keyManagers;
    private TrustManager[] trustManagers;
    private SecureRandom secureRandom;

    private SslContextBuilder() {
    }

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @return the builder - 构建器
     */
    public static SslContextBuilder create() {
        return new SslContextBuilder();
    }

    /**
     * Sets the SSL protocol.
     * 设置 SSL 协议。
     *
     * @param protocol the protocol (e.g., "TLS", "TLSv1.2", "TLSv1.3") - 协议
     * @return this builder - 此构建器
     */
    public SslContextBuilder protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Sets TLS 1.2 protocol.
     * 设置 TLS 1.2 协议。
     *
     * @return this builder - 此构建器
     */
    public SslContextBuilder tlsV12() {
        this.protocol = "TLSv1.2";
        return this;
    }

    /**
     * Sets TLS 1.3 protocol.
     * 设置 TLS 1.3 协议。
     *
     * @return this builder - 此构建器
     */
    public SslContextBuilder tlsV13() {
        this.protocol = "TLSv1.3";
        return this;
    }

    /**
     * Sets key managers.
     * 设置密钥管理器。
     *
     * @param keyManagers the key managers - 密钥管理器
     * @return this builder - 此构建器
     */
    public SslContextBuilder keyManagers(KeyManager... keyManagers) {
        this.keyManagers = keyManagers;
        return this;
    }

    /**
     * Sets trust managers.
     * 设置信任管理器。
     *
     * @param trustManagers the trust managers - 信任管理器
     * @return this builder - 此构建器
     */
    public SslContextBuilder trustManagers(TrustManager... trustManagers) {
        this.trustManagers = trustManagers;
        return this;
    }

    /**
     * Sets secure random.
     * 设置安全随机数。
     *
     * @param secureRandom the secure random - 安全随机数
     * @return this builder - 此构建器
     */
    public SslContextBuilder secureRandom(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
        return this;
    }

    /**
     * Configures to trust all certificates (DANGEROUS - development only).
     * 配置为信任所有证书（危险 - 仅用于开发）。
     *
     * @return this builder - 此构建器
     */
    public SslContextBuilder trustAll() {
        this.trustManagers = new TrustManager[]{TrustAllManager.INSTANCE};
        return this;
    }

    /**
     * Loads keystore from file.
     * 从文件加载密钥库。
     *
     * @param path     the keystore path - 密钥库路径
     * @param password the keystore password - 密钥库密码
     * @return this builder - 此构建器
     * @throws OpenCryptoException if loading fails - 如果加载失败
     */
    public SslContextBuilder keyStore(Path path, String password) {
        return keyStore(path, password, DEFAULT_KEYSTORE_TYPE);
    }

    /**
     * Loads keystore from file with type.
     * 从文件加载指定类型的密钥库。
     *
     * @param path     the keystore path - 密钥库路径
     * @param password the keystore password - 密钥库密码
     * @param type     the keystore type - 密钥库类型
     * @return this builder - 此构建器
     * @throws OpenCryptoException if loading fails - 如果加载失败
     */
    public SslContextBuilder keyStore(Path path, String password, String type) {
        try (InputStream is = Files.newInputStream(path)) {
            KeyStore keyStore = KeyStore.getInstance(type);
            keyStore.load(is, password.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, password.toCharArray());
            this.keyManagers = kmf.getKeyManagers();
            return this;
        } catch (IOException | GeneralSecurityException e) {
            throw new OpenCryptoException("Failed to load keystore from: " + path, e);
        }
    }

    /**
     * Loads keystore from input stream.
     * 从输入流加载密钥库。
     *
     * @param inputStream the input stream - 输入流
     * @param password    the keystore password - 密钥库密码
     * @return this builder - 此构建器
     * @throws OpenCryptoException if loading fails - 如果加载失败
     */
    public SslContextBuilder keyStore(InputStream inputStream, String password) {
        return keyStore(inputStream, password, DEFAULT_KEYSTORE_TYPE);
    }

    /**
     * Loads keystore from input stream with type.
     * 从输入流加载指定类型的密钥库。
     *
     * @param inputStream the input stream - 输入流
     * @param password    the keystore password - 密钥库密码
     * @param type        the keystore type - 密钥库类型
     * @return this builder - 此构建器
     * @throws OpenCryptoException if loading fails - 如果加载失败
     */
    public SslContextBuilder keyStore(InputStream inputStream, String password, String type) {
        try {
            KeyStore keyStore = KeyStore.getInstance(type);
            keyStore.load(inputStream, password.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, password.toCharArray());
            this.keyManagers = kmf.getKeyManagers();
            return this;
        } catch (GeneralSecurityException | IOException e) {
            throw new OpenCryptoException("Failed to load keystore from stream", e);
        }
    }

    /**
     * Loads truststore from file.
     * 从文件加载信任库。
     *
     * @param path     the truststore path - 信任库路径
     * @param password the truststore password - 信任库密码
     * @return this builder - 此构建器
     * @throws OpenCryptoException if loading fails - 如果加载失败
     */
    public SslContextBuilder trustStore(Path path, String password) {
        return trustStore(path, password, KeyStore.getDefaultType());
    }

    /**
     * Loads truststore from file with type.
     * 从文件加载指定类型的信任库。
     *
     * @param path     the truststore path - 信任库路径
     * @param password the truststore password - 信任库密码
     * @param type     the truststore type - 信任库类型
     * @return this builder - 此构建器
     * @throws OpenCryptoException if loading fails - 如果加载失败
     */
    public SslContextBuilder trustStore(Path path, String password, String type) {
        try (InputStream is = Files.newInputStream(path)) {
            KeyStore trustStore = KeyStore.getInstance(type);
            trustStore.load(is, password.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            this.trustManagers = tmf.getTrustManagers();
            return this;
        } catch (IOException | GeneralSecurityException e) {
            throw new OpenCryptoException("Failed to load truststore from: " + path, e);
        }
    }

    /**
     * Loads truststore from input stream.
     * 从输入流加载信任库。
     *
     * @param inputStream the input stream - 输入流
     * @param password    the truststore password - 信任库密码
     * @return this builder - 此构建器
     * @throws OpenCryptoException if loading fails - 如果加载失败
     */
    public SslContextBuilder trustStore(InputStream inputStream, String password) {
        return trustStore(inputStream, password, KeyStore.getDefaultType());
    }

    /**
     * Loads truststore from input stream with type.
     * 从输入流加载指定类型的信任库。
     *
     * @param inputStream the input stream - 输入流
     * @param password    the truststore password - 信任库密码
     * @param type        the truststore type - 信任库类型
     * @return this builder - 此构建器
     * @throws OpenCryptoException if loading fails - 如果加载失败
     */
    public SslContextBuilder trustStore(InputStream inputStream, String password, String type) {
        try {
            KeyStore trustStore = KeyStore.getInstance(type);
            trustStore.load(inputStream, password.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            this.trustManagers = tmf.getTrustManagers();
            return this;
        } catch (GeneralSecurityException | IOException e) {
            throw new OpenCryptoException("Failed to load truststore from stream", e);
        }
    }

    /**
     * Loads a PEM-encoded certificate and trusts it (as a CA or leaf certificate).
     * 加载 PEM 格式的证书并将其作为受信任证书（CA 或叶证书）。
     *
     * <p>Useful when the server uses a self-signed certificate or a private CA,
     * and you want to trust only that specific certificate without disabling
     * all certificate validation.</p>
     * <p>适用于服务器使用自签名证书或私有 CA 的场景，
     * 只信任该特定证书而不禁用所有证书验证。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * String pem = Files.readString(Path.of("/etc/ssl/server.pem"));
     * SSLContext ctx = SslContextBuilder.create().pemCertificate(pem).build();
     * }</pre>
     *
     * @param pemContent the PEM-encoded certificate string - PEM 格式的证书字符串
     * @return this builder - 此构建器
     * @throws OpenCryptoException if the PEM is invalid - 如果 PEM 无效
     */
    public SslContextBuilder pemCertificate(String pemContent) {
        try {
            X509Certificate cert = parsePem(pemContent);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setCertificateEntry("pem-ca", cert);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            this.trustManagers = tmf.getTrustManagers();
            return this;
        } catch (GeneralSecurityException | IOException e) {
            throw new OpenCryptoException("Failed to parse PEM certificate", e);
        }
    }

    /**
     * Loads a PEM-encoded certificate from a file.
     * 从文件加载 PEM 格式的证书。
     *
     * @param pemPath the path to the PEM file - PEM 文件路径
     * @return this builder - 此构建器
     * @throws OpenCryptoException if the file cannot be read - 如果文件无法读取
     */
    public SslContextBuilder pemCertificate(Path pemPath) {
        try {
            return pemCertificate(Files.readString(pemPath));
        } catch (IOException e) {
            throw new OpenCryptoException("Failed to read PEM file: " + pemPath, e);
        }
    }

    /**
     * Merges the JVM default system trust store with one or more extra PEM CA certificates.
     * 将 JVM 默认系统信任库与额外的 PEM 格式 CA 证书合并。
     *
     * <p>This is the recommended approach for corporate environments where the server
     * is signed by an internal CA not included in the JVM default trust store.
     * Unlike {@link #trustAll()}, this method preserves full certificate validation
     * for all other hosts.</p>
     * <p>推荐用于企业环境，服务器由不在 JVM 默认信任库中的内部 CA 签署。
     * 与 {@link #trustAll()} 不同，此方法对所有其他主机保留完整的证书验证。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * SSLContext ctx = SslContextBuilder.create()
     *     .withExtraCA(Path.of("/etc/corp/ca.pem"))
     *     .build();
     * }</pre>
     *
     * @param caCertPemPath the path to the extra CA PEM file - 额外 CA PEM 文件路径
     * @return this builder - 此构建器
     * @throws OpenCryptoException if the file cannot be read or the cert is invalid - 如果文件无法读取或证书无效
     */
    public SslContextBuilder withExtraCA(Path caCertPemPath) {
        try {
            return withExtraCA(Files.readString(caCertPemPath));
        } catch (IOException e) {
            throw new OpenCryptoException("Failed to read CA certificate file: " + caCertPemPath, e);
        }
    }

    /**
     * Merges the JVM default system trust store with a PEM-encoded CA certificate string.
     * 将 JVM 默认系统信任库与 PEM 格式的 CA 证书字符串合并。
     *
     * @param caCertPem the PEM-encoded CA certificate - PEM 格式的 CA 证书字符串
     * @return this builder - 此构建器
     * @throws OpenCryptoException if the certificate is invalid - 如果证书无效
     */
    public SslContextBuilder withExtraCA(String caCertPem) {
        try {
            // Load the JVM default trust store
            TrustManagerFactory defaultTmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            defaultTmf.init((KeyStore) null); // null = use JVM default

            // Build a combined KeyStore: system trusted CAs + extra CA
            KeyStore combinedKs = KeyStore.getInstance(KeyStore.getDefaultType());
            combinedKs.load(null, null);

            int index = 0;
            for (TrustManager tm : defaultTmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager x509tm) {
                    for (X509Certificate cert : x509tm.getAcceptedIssuers()) {
                        combinedKs.setCertificateEntry("system-ca-" + index++, cert);
                    }
                }
            }
            combinedKs.setCertificateEntry("extra-ca", parsePem(caCertPem));

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(combinedKs);
            this.trustManagers = tmf.getTrustManagers();
            return this;
        } catch (GeneralSecurityException | IOException e) {
            throw new OpenCryptoException("Failed to merge extra CA with system trust store", e);
        }
    }

    // ==================== mTLS PEM Client Certificate ====================

    /**
     * Configures mutual TLS (mTLS) using PEM-encoded client certificate and PKCS8 private key.
     * This is the standard format used in Kubernetes / cloud-native environments.
     * 使用 PEM 格式的客户端证书和 PKCS8 私钥配置双向 TLS（mTLS）。
     * 这是 Kubernetes / 云原生环境中的标准格式。
     *
     * <p>The private key must be in <strong>PKCS8 unencrypted</strong> format
     * ({@code -----BEGIN PRIVATE KEY-----}). To convert from PKCS1 ({@code BEGIN RSA PRIVATE KEY}):
     * {@code openssl pkcs8 -topk8 -nocrypt -in key.pem -out key-pkcs8.pem}</p>
     * <p>私钥必须是 <strong>PKCS8 未加密</strong>格式（{@code -----BEGIN PRIVATE KEY-----}）。
     * 从 PKCS1 转换：{@code openssl pkcs8 -topk8 -nocrypt -in key.pem -out key-pkcs8.pem}</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * String cert = Files.readString(Path.of("/etc/ssl/client.pem"));
     * String key  = Files.readString(Path.of("/etc/ssl/client-key.pem"));
     * SSLContext ctx = SslContextBuilder.create()
     *     .pemClientCertificate(cert, key)
     *     .withExtraCA("/etc/ssl/ca.pem")          // optional: custom CA
     *     .build();
     * HttpClient client = HttpClient.builder().sslContext(ctx).build();
     * }</pre>
     *
     * @param certPem PEM-encoded client certificate - PEM 格式的客户端证书
     * @param keyPem  PEM-encoded PKCS8 private key - PEM 格式的 PKCS8 私钥
     * @return this builder - 此构建器
     * @throws OpenCryptoException if the certificate or key is invalid - 如果证书或密钥无效
     */
    public SslContextBuilder pemClientCertificate(String certPem, String keyPem) {
        try {
            X509Certificate cert = parsePem(certPem);
            PrivateKey key = parsePrivateKeyPem(keyPem);

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setKeyEntry("client-cert", key, new char[0], new X509Certificate[]{cert});

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, new char[0]);
            this.keyManagers = kmf.getKeyManagers();
            return this;
        } catch (GeneralSecurityException | IOException e) {
            throw new OpenCryptoException("Failed to configure mTLS with PEM client certificate", e);
        }
    }

    /**
     * Configures mutual TLS (mTLS) from PEM files on disk.
     * 从磁盘上的 PEM 文件配置双向 TLS（mTLS）。
     *
     * @param certPath path to the PEM client certificate file - PEM 客户端证书文件路径
     * @param keyPath  path to the PKCS8 private key file - PKCS8 私钥文件路径
     * @return this builder - 此构建器
     * @throws OpenCryptoException if the files cannot be read - 如果文件无法读取
     */
    public SslContextBuilder pemClientCertificate(Path certPath, Path keyPath) {
        try {
            return pemClientCertificate(Files.readString(certPath), Files.readString(keyPath));
        } catch (IOException e) {
            throw new OpenCryptoException(
                    "Failed to read mTLS PEM files: " + certPath + ", " + keyPath, e);
        }
    }

    // ==================== PEM Parsing ====================

    private static X509Certificate parsePem(String pem) throws GeneralSecurityException {
        String cleaned = pem
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(cleaned);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(decoded));
    }

    /**
     * Parses a PKCS8 unencrypted PEM private key (BEGIN PRIVATE KEY).
     * Tries RSA, EC, Ed25519, Ed448, DSA in order — the DER contains the algorithm OID.
     * 解析 PKCS8 未加密 PEM 私钥（BEGIN PRIVATE KEY）。
     * 按顺序尝试 RSA、EC、Ed25519、Ed448、DSA——DER 中包含算法 OID。
     */
    private static PrivateKey parsePrivateKeyPem(String pem) throws GeneralSecurityException {
        String cleaned = pem
                .replaceAll("-----BEGIN.*?-----", "")
                .replaceAll("-----END.*?-----", "")
                .replaceAll("\\s+", "");
        byte[] der = Base64.getDecoder().decode(cleaned);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        for (String alg : new String[]{"RSA", "EC", "Ed25519", "Ed448", "DSA"}) {
            try {
                return KeyFactory.getInstance(alg).generatePrivate(spec);
            } catch (InvalidKeySpecException | NoSuchAlgorithmException ignored) {
                // Try next algorithm
            }
        }
        throw new InvalidKeySpecException("Unsupported private key format (tried RSA, EC, Ed25519, Ed448, DSA)");
    }

    /**
     * Builds the SSLContext.
     * 构建 SSLContext。
     *
     * @return the SSLContext - SSLContext
     * @throws OpenCryptoException if building fails - 如果构建失败
     */
    public SSLContext build() {
        try {
            SSLContext sslContext = SSLContext.getInstance(protocol);
            sslContext.init(keyManagers, trustManagers, secureRandom);
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new OpenCryptoException("Failed to build SSLContext", e);
        }
    }
}
