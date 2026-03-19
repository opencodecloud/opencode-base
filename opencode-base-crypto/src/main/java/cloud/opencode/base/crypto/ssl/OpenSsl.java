package cloud.opencode.base.crypto.ssl;

import cloud.opencode.base.crypto.exception.OpenCryptoException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import java.io.InputStream;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * OpenSsl - SSL/TLS Utility Class
 * OpenSsl - SSL/TLS 工具类
 *
 * <p>This class provides static utilities for SSL/TLS operations including
 * creating SSL contexts, loading certificates, and configuring trust managers.</p>
 * <p>此类提供 SSL/TLS 操作的静态工具，包括创建 SSL 上下文、加载证书和配置信任管理器。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Create trust-all context (development only)
 * SSLContext devContext = OpenSsl.createTrustAllContext();
 *
 * // Create context with custom truststore
 * SSLContext prodContext = OpenSsl.createContext(
 *     Path.of("/path/to/truststore.jks"), "password");
 *
 * // Get default SSL context
 * SSLContext defaultContext = OpenSsl.getDefaultContext();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SSLContext creation utilities - SSLContext 创建工具</li>
 *   <li>Trust-all context for development - 开发用信任所有上下文</li>
 *   <li>PEM and extra CA support - PEM 和额外 CA 支持</li>
 *   <li>Mutual TLS (mTLS) support - 双向 TLS（mTLS）支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SSLContext ctx = OpenSsl.createTrustAllContext(); // dev only
 * SSLContext prod = OpenSsl.withExtraCA(Path.of("ca.pem"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial - 空值安全: 部分</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class OpenSsl {

    private static final System.Logger LOGGER = System.getLogger(OpenSsl.class.getName());

    private OpenSsl() {
        // Utility class
    }

    // ==================== Context Creation ====================

    /**
     * Creates a builder for SSLContext.
     * 创建 SSLContext 的构建器。
     *
     * @return the builder - 构建器
     */
    public static SslContextBuilder builder() {
        return SslContextBuilder.create();
    }

    /**
     * Gets the default SSLContext.
     * 获取默认的 SSLContext。
     *
     * @return the default SSLContext - 默认的 SSLContext
     */
    public static SSLContext getDefaultContext() {
        try {
            return SSLContext.getDefault();
        } catch (NoSuchAlgorithmException e) {
            throw new OpenCryptoException("Failed to get default SSLContext", e);
        }
    }

    /**
     * Creates a SSLContext that trusts all certificates.
     * 创建信任所有证书的 SSLContext。
     *
     * <p><strong>WARNING:</strong> Use only for development/testing.</p>
     * <p><strong>警告：</strong>仅用于开发/测试。</p>
     *
     * @return the SSLContext - SSLContext
     */
    public static SSLContext createTrustAllContext() {
        return SslContextBuilder.create()
                .trustAll()
                .build();
    }

    /**
     * Creates a SSLContext with custom truststore.
     * 使用自定义信任库创建 SSLContext。
     *
     * @param trustStorePath     the truststore path - 信任库路径
     * @param trustStorePassword the truststore password - 信任库密码
     * @return the SSLContext - SSLContext
     */
    public static SSLContext createContext(Path trustStorePath, String trustStorePassword) {
        return SslContextBuilder.create()
                .trustStore(trustStorePath, trustStorePassword)
                .build();
    }

    /**
     * Creates a SSLContext with keystore and truststore.
     * 使用密钥库和信任库创建 SSLContext。
     *
     * @param keyStorePath       the keystore path - 密钥库路径
     * @param keyStorePassword   the keystore password - 密钥库密码
     * @param trustStorePath     the truststore path - 信任库路径
     * @param trustStorePassword the truststore password - 信任库密码
     * @return the SSLContext - SSLContext
     */
    public static SSLContext createContext(Path keyStorePath, String keyStorePassword,
                                           Path trustStorePath, String trustStorePassword) {
        return SslContextBuilder.create()
                .keyStore(keyStorePath, keyStorePassword)
                .trustStore(trustStorePath, trustStorePassword)
                .build();
    }

    /**
     * Creates a SSLContext from streams.
     * 从流创建 SSLContext。
     *
     * @param keyStoreStream     the keystore stream - 密钥库流
     * @param keyStorePassword   the keystore password - 密钥库密码
     * @param trustStoreStream   the truststore stream - 信任库流
     * @param trustStorePassword the truststore password - 信任库密码
     * @return the SSLContext - SSLContext
     */
    public static SSLContext createContext(InputStream keyStoreStream, String keyStorePassword,
                                           InputStream trustStoreStream, String trustStorePassword) {
        return SslContextBuilder.create()
                .keyStore(keyStoreStream, keyStorePassword)
                .trustStore(trustStoreStream, trustStorePassword)
                .build();
    }

    /**
     * Creates a SSLContext that trusts only a specific PEM-encoded certificate.
     * 创建只信任指定 PEM 证书的 SSLContext。
     *
     * <p>Safer than {@link #createTrustAllContext()} — validates the certificate
     * chain but only trusts the provided CA.</p>
     * <p>比 {@link #createTrustAllContext()} 更安全——验证证书链但只信任提供的 CA。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * String pem = Files.readString(Path.of("/etc/ssl/internal-ca.pem"));
     * SSLContext ctx = OpenSsl.fromPem(pem);
     * HttpClient client = HttpClient.builder().sslContext(ctx).build();
     * }</pre>
     *
     * @param caCertPem the PEM-encoded CA certificate - PEM 格式的 CA 证书
     * @return the SSLContext - SSLContext
     */
    public static SSLContext fromPem(String caCertPem) {
        return SslContextBuilder.create()
                .pemCertificate(caCertPem)
                .build();
    }

    /**
     * Creates a SSLContext that trusts the JVM system CA store plus an extra CA certificate.
     * 创建信任 JVM 系统 CA 库和额外 CA 证书的 SSLContext。
     *
     * <p>This is the recommended approach for corporate environments where the server
     * uses an internal CA not shipped with the JVM. All public CAs remain trusted.</p>
     * <p>推荐用于企业环境（服务器使用 JVM 中未包含的内部 CA）。所有公共 CA 仍然受信任。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * SSLContext ctx = OpenSsl.withExtraCA(Path.of("/etc/corp/ca.pem"));
     * HttpClient client = HttpClient.builder().sslContext(ctx).build();
     * }</pre>
     *
     * @param caCertPath path to the extra CA PEM file - 额外 CA PEM 文件路径
     * @return the SSLContext - SSLContext
     */
    public static SSLContext withExtraCA(Path caCertPath) {
        return SslContextBuilder.create()
                .withExtraCA(caCertPath)
                .build();
    }

    /**
     * Creates a SSLContext that trusts the JVM system CA store plus a PEM-encoded CA.
     * 创建信任 JVM 系统 CA 库和 PEM 格式 CA 的 SSLContext。
     *
     * @param caCertPem the PEM-encoded extra CA certificate - PEM 格式的额外 CA 证书
     * @return the SSLContext - SSLContext
     */
    public static SSLContext withExtraCA(String caCertPem) {
        return SslContextBuilder.create()
                .withExtraCA(caCertPem)
                .build();
    }

    // ==================== Mutual TLS (mTLS) ====================

    /**
     * Creates an SSLContext for mutual TLS (mTLS) using PEM client certificate and PKCS8 private key.
     * The server's certificate is validated against the JVM default trust store.
     * 使用 PEM 客户端证书和 PKCS8 私钥创建双向 TLS（mTLS）的 SSLContext。
     * 服务器证书使用 JVM 默认信任库验证。
     *
     * <p>The private key must be in <strong>PKCS8 unencrypted</strong> format
     * ({@code -----BEGIN PRIVATE KEY-----}). Convert from PKCS1:
     * {@code openssl pkcs8 -topk8 -nocrypt -in key.pem -out key-pkcs8.pem}</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * String cert = Files.readString(Path.of("/etc/ssl/client.pem"));
     * String key  = Files.readString(Path.of("/etc/ssl/client-key.pem"));
     * HttpClient client = HttpClient.builder()
     *     .sslContext(OpenSsl.mTls(cert, key))
     *     .build();
     * }</pre>
     *
     * @param clientCertPem PEM-encoded client certificate - PEM 格式的客户端证书
     * @param clientKeyPem  PEM-encoded PKCS8 private key - PEM 格式的 PKCS8 私钥
     * @return the SSLContext configured for mTLS - 配置了 mTLS 的 SSLContext
     */
    public static SSLContext mTls(String clientCertPem, String clientKeyPem) {
        return SslContextBuilder.create()
                .pemClientCertificate(clientCertPem, clientKeyPem)
                .build();
    }

    /**
     * Creates an SSLContext for mutual TLS from PEM files on disk.
     * 从磁盘上的 PEM 文件创建双向 TLS（mTLS）的 SSLContext。
     *
     * @param clientCertPath path to the PEM client certificate - PEM 客户端证书文件路径
     * @param clientKeyPath  path to the PKCS8 private key file - PKCS8 私钥文件路径
     * @return the SSLContext configured for mTLS - 配置了 mTLS 的 SSLContext
     */
    public static SSLContext mTls(Path clientCertPath, Path clientKeyPath) {
        return SslContextBuilder.create()
                .pemClientCertificate(clientCertPath, clientKeyPath)
                .build();
    }

    /**
     * Creates an SSLContext for mutual TLS with a custom CA trust anchor.
     * Use when both the client certificate and server's CA are non-public.
     * 使用自定义 CA 信任锚创建双向 TLS（mTLS）的 SSLContext。
     * 适用于客户端证书和服务器 CA 均为非公开的场景（如服务网格、内部微服务）。
     *
     * @param clientCertPem PEM-encoded client certificate - PEM 格式的客户端证书
     * @param clientKeyPem  PEM-encoded PKCS8 private key - PEM 格式的 PKCS8 私钥
     * @param caCertPem     PEM-encoded CA certificate to trust - 要信任的 PEM 格式 CA 证书
     * @return the SSLContext configured for mTLS - 配置了 mTLS 的 SSLContext
     */
    public static SSLContext mTls(String clientCertPem, String clientKeyPem, String caCertPem) {
        return SslContextBuilder.create()
                .pemClientCertificate(clientCertPem, clientKeyPem)
                .pemCertificate(caCertPem)
                .build();
    }

    // ==================== SSL Socket Factory ====================

    /**
     * Creates a SSLSocketFactory that trusts all certificates.
     * 创建信任所有证书的 SSLSocketFactory。
     *
     * @return the SSLSocketFactory - SSLSocketFactory
     */
    public static SSLSocketFactory createTrustAllSocketFactory() {
        return createTrustAllContext().getSocketFactory();
    }

    /**
     * Gets the default SSLSocketFactory.
     * 获取默认的 SSLSocketFactory。
     *
     * @return the SSLSocketFactory - SSLSocketFactory
     */
    public static SSLSocketFactory getDefaultSocketFactory() {
        return getDefaultContext().getSocketFactory();
    }

    // ==================== Hostname Verifier ====================

    /**
     * Gets a hostname verifier that accepts all hostnames.
     * 获取接受所有主机名的主机名验证器。
     *
     * <p><strong>WARNING:</strong> Use only for development/testing.</p>
     *
     * @return the hostname verifier - 主机名验证器
     */
    public static HostnameVerifier getTrustAllHostnameVerifier() {
        return (hostname, session) -> true;
    }

    /**
     * Gets the default hostname verifier.
     * 获取默认的主机名验证器。
     *
     * @return the hostname verifier - 主机名验证器
     */
    public static HostnameVerifier getDefaultHostnameVerifier() {
        return HttpsURLConnection.getDefaultHostnameVerifier();
    }

    // ==================== Certificate Info ====================

    /**
     * Gets server certificates from a URL.
     * 从 URL 获取服务器证书。
     *
     * @param host the hostname - 主机名
     * @param port the port - 端口
     * @return the certificates - 证书数组
     */
    public static X509Certificate[] getServerCertificates(String host, int port) {
        try {
            SSLContext context = getDefaultContext();
            SSLSocketFactory factory = context.getSocketFactory();

            try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port)) {
                socket.startHandshake();
                return Arrays.stream(socket.getSession().getPeerCertificates())
                        .filter(cert -> cert instanceof X509Certificate)
                        .map(cert -> (X509Certificate) cert)
                        .toArray(X509Certificate[]::new);
            }
        } catch (Exception e) {
            throw new OpenCryptoException("Failed to get server certificates from: " + host + ":" + port, e);
        }
    }

    /**
     * Gets certificate subject DN.
     * 获取证书主题 DN。
     *
     * @param certificate the certificate - 证书
     * @return the subject DN - 主题 DN
     */
    public static String getCertificateSubject(X509Certificate certificate) {
        return certificate.getSubjectX500Principal().getName();
    }

    /**
     * Gets certificate issuer DN.
     * 获取证书签发者 DN。
     *
     * @param certificate the certificate - 证书
     * @return the issuer DN - 签发者 DN
     */
    public static String getCertificateIssuer(X509Certificate certificate) {
        return certificate.getIssuerX500Principal().getName();
    }

    // ==================== Protocol Support ====================

    /**
     * Gets supported SSL protocols.
     * 获取支持的 SSL 协议。
     *
     * @return the supported protocols - 支持的协议
     */
    public static String[] getSupportedProtocols() {
        try {
            SSLContext context = SSLContext.getDefault();
            return context.getSupportedSSLParameters().getProtocols();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(System.Logger.Level.WARNING, "Failed to get supported protocols", e);
            return new String[0];
        }
    }

    /**
     * Gets supported cipher suites.
     * 获取支持的密码套件。
     *
     * @return the supported cipher suites - 支持的密码套件
     */
    public static String[] getSupportedCipherSuites() {
        try {
            SSLContext context = SSLContext.getDefault();
            return context.getSupportedSSLParameters().getCipherSuites();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(System.Logger.Level.WARNING, "Failed to get supported cipher suites", e);
            return new String[0];
        }
    }

    /**
     * Checks if TLS 1.3 is supported.
     * 检查是否支持 TLS 1.3。
     *
     * @return true if supported - 如果支持返回 true
     */
    public static boolean isTls13Supported() {
        return Arrays.asList(getSupportedProtocols()).contains("TLSv1.3");
    }
}
