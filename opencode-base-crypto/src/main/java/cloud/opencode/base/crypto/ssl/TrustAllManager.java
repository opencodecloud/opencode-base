package cloud.opencode.base.crypto.ssl;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

/**
 * Trust All Manager - X509TrustManager that Trusts All Certificates
 * 信任所有管理器 - 信任所有证书的 X509TrustManager
 *
 * <p><strong>WARNING:</strong> This trust manager accepts ALL certificates
 * without validation. Use ONLY for development and testing purposes.</p>
 * <p><strong>警告：</strong>此信任管理器接受所有证书而不进行验证。
 * 仅用于开发和测试目的。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * SSLContext sslContext = SSLContext.getInstance("TLS");
 * sslContext.init(null, new TrustManager[]{TrustAllManager.INSTANCE}, null);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Trusts all certificates without validation - 不验证地信任所有证书</li>
 *   <li>Development and testing only - 仅用于开发和测试</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Development only
 * SSLContext ctx = SSLContext.getInstance("TLS");
 * ctx.init(null, new TrustManager[]{TrustAllManager.INSTANCE}, null);
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
public final class TrustAllManager implements X509TrustManager {

    /**
     * System property to enable TrustAllManager in production (NOT recommended)
     * 在生产环境启用TrustAllManager的系统属性（不推荐）
     */
    public static final String TRUST_ALL_ENABLED_PROPERTY = "opencode.ssl.trustAll.enabled";

    /**
     * Singleton instance.
     * 单例实例。
     */
    public static final TrustAllManager INSTANCE = createInstance();

    private static final X509Certificate[] EMPTY_CERTIFICATES = new X509Certificate[0];

    private static TrustAllManager createInstance() {
        // Check if explicitly enabled via system property
        String enabled = System.getProperty(TRUST_ALL_ENABLED_PROPERTY);
        if (!"true".equalsIgnoreCase(enabled)) {
            // Log warning that this should only be used in development
            System.getLogger(TrustAllManager.class.getName())
                    .log(System.Logger.Level.WARNING,
                            "TrustAllManager is being instantiated. This should ONLY be used in development/testing. " +
                                    "Set -D" + TRUST_ALL_ENABLED_PROPERTY + "=true to suppress this warning.");
        }
        return new TrustAllManager();
    }

    private TrustAllManager() {
        // Singleton
    }

    /**
     * Creates a new instance with explicit acknowledgment of security risks.
     * 创建一个明确承认安全风险的新实例。
     *
     * @param acknowledgeSecurityRisk must be true to create instance | 必须为true才能创建实例
     * @return new TrustAllManager instance | 新的TrustAllManager实例
     * @throws SecurityException if acknowledgeSecurityRisk is false | 如果acknowledgeSecurityRisk为false则抛出
     */
    public static TrustAllManager createUnsafe(boolean acknowledgeSecurityRisk) {
        if (!acknowledgeSecurityRisk) {
            throw new SecurityException(
                    "You must explicitly acknowledge the security risk by passing true. " +
                            "TrustAllManager disables ALL SSL certificate validation and should NEVER be used in production.");
        }
        return new TrustAllManager();
    }

    /**
     * Does not check client certificates.
     * 不检查客户端证书。
     *
     * @param chain    the certificate chain - 证书链
     * @param authType the authentication type - 认证类型
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
        // Trust all - no validation
    }

    /**
     * Does not check server certificates.
     * 不检查服务器证书。
     *
     * @param chain    the certificate chain - 证书链
     * @param authType the authentication type - 认证类型
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
        // Trust all - no validation
    }

    /**
     * Returns empty accepted issuers.
     * 返回空的接受的签发者。
     *
     * @return empty array - 空数组
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return EMPTY_CERTIFICATES;
    }
}
