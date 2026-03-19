package cloud.opencode.base.crypto.ssl;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.core.Preconditions;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.hash.Sha2Hash;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Certificate Pinner - SHA-256 SPKI Fingerprint Certificate Pinning
 * 证书固定器 - SHA-256 SPKI 指纹证书固定
 *
 * <p>Protects against certificate authority (CA) compromise by pinning the
 * SHA-256 fingerprint of a certificate's SubjectPublicKeyInfo (SPKI).
 * Format follows the OkHttp convention: {@code sha256/base64==}.</p>
 * <p>通过固定证书 SubjectPublicKeyInfo（SPKI）的 SHA-256 指纹，防范证书颁发机构（CA）被攻击。
 * 格式遵循 OkHttp 约定：{@code sha256/base64==}。</p>
 *
 * <p><strong>Why SPKI pinning? | 为什么使用 SPKI 固定？</strong><br>
 * Pinning the public key (SPKI) rather than the full certificate fingerprint means
 * the pin remains valid after a certificate renewal, as long as the same key pair is
 * reused. This dramatically reduces operational burden.</p>
 * <p>固定公钥（SPKI）而非完整证书指纹，意味着只要复用相同密钥对，证书续期后固定仍然有效，
 * 大幅降低运维负担。</p>
 *
 * <p><strong>How to obtain a pin | 如何获取固定值:</strong></p>
 * <pre>{@code
 * # From a live server:
 * openssl s_client -connect api.example.com:443 -servername api.example.com \
 *   | openssl x509 -pubkey -noout \
 *   | openssl pkey -pubin -outform DER \
 *   | openssl dgst -sha256 -binary \
 *   | openssl base64
 *
 * // Or from Java code:
 * String pin = CertificatePinner.computePin(cert);
 * }</pre>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * CertificatePinner pinner = CertificatePinner.builder()
 *     .add("sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
 *     .add("sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=")
 *     .build();
 *
 * HttpClient client = HttpClient.builder()
 *     .sslContext(pinner.toSslContext())
 *     .build();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SHA-256 SPKI fingerprint pinning - SHA-256 SPKI 指纹固定</li>
 *   <li>Certificate pin computation - 证书固定值计算</li>
 *   <li>SSLContext with pinning enforcement - 强制固定的 SSLContext</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CertificatePinner pinner = CertificatePinner.builder()
 *     .add("sha256/AAAA...")
 *     .build();
 * SSLContext ctx = pinner.toSslContext();
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
public final class CertificatePinner {

    private static final String PIN_PREFIX = "sha256/";
    private static final Sha2Hash SHA256 = Sha2Hash.sha256();

    private final Set<String> pinnedHashes;

    private CertificatePinner(Set<String> pinnedHashes) {
        this.pinnedHashes = Collections.unmodifiableSet(new HashSet<>(pinnedHashes));
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a builder for configuring certificate pins.
     * 创建用于配置证书固定的构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Utility ====================

    /**
     * Computes the {@code sha256/base64==} pin for the given certificate.
     * Useful for obtaining pin values during development or CI verification.
     * 计算给定证书的 {@code sha256/base64==} 固定值。
     * 适用于开发期间获取固定值或 CI 验证。
     *
     * @param cert the X.509 certificate - X.509 证书
     * @return the pin string in {@code sha256/base64==} format - sha256/base64== 格式的固定字符串
     */
    public static String computePin(X509Certificate cert) {
        Preconditions.checkNotNull(cert, "cert");
        byte[] spki = cert.getPublicKey().getEncoded();
        byte[] hash = SHA256.hash(spki);
        return PIN_PREFIX + OpenBase64.encode(hash);
    }

    // ==================== SSLContext Creation ====================

    /**
     * Creates an {@link SSLContext} that enforces certificate pinning during TLS handshake.
     * The pinning check is layered on top of the JVM's default trust store: a certificate
     * must pass both normal CA verification <em>and</em> at least one pinned hash must match.
     * 创建在 TLS 握手期间强制执行证书固定的 {@link SSLContext}。
     * 固定检查叠加在 JVM 默认信任库之上：证书必须通过正常 CA 验证，
     * 且至少有一个固定哈希匹配。
     *
     * @return the SSL context - SSL 上下文
     * @throws OpenCryptoException if the SSL context cannot be created - 如果无法创建 SSL 上下文
     */
    public SSLContext toSslContext() {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            X509TrustManager defaultTm = null;
            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager xtm) {
                    defaultTm = xtm;
                    break;
                }
            }
            if (defaultTm == null) {
                throw new OpenCryptoException("No X509TrustManager found in default trust managers");
            }
            X509TrustManager pinningTm = new PinningTrustManager(defaultTm, pinnedHashes);
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{pinningTm}, null);
            return ctx;
        } catch (OpenCryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenCryptoException("Failed to create certificate pinning SSLContext: " + e.getMessage(), e);
        }
    }

    // ==================== Pinning TrustManager ====================

    private record PinningTrustManager(
            X509TrustManager delegate,
            Set<String> pins) implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            delegate.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            // Standard CA validation first
            delegate.checkServerTrusted(chain, authType);
            // SPKI pin check: at least one cert in the chain must match a pin
            for (X509Certificate cert : chain) {
                String pin = computePin(cert);
                if (pins.contains(pin)) {
                    return; // pin matched — accept
                }
            }
            throw new CertificateException(
                    "Certificate pinning verification failed: none of " + chain.length + " certificates matched any pin");
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return delegate.getAcceptedIssuers();
        }
    }

    // ==================== Builder ====================

    /**
     * Builder for {@link CertificatePinner}.
     * {@link CertificatePinner} 的构建器。
     */
    public static final class Builder {

        private final Set<String> pins = new HashSet<>();

        private Builder() {
        }

        /**
         * Adds a pin in {@code sha256/base64==} format.
         * 添加 {@code sha256/base64==} 格式的固定值。
         *
         * @param pin the pin string - 固定字符串
         * @return this builder - 此构建器
         * @throws IllegalArgumentException if the pin format is invalid - 如果格式无效
         */
        public Builder add(String pin) {
            Preconditions.checkNotNull(pin, "pin");
            if (!pin.startsWith(PIN_PREFIX)) {
                throw new IllegalArgumentException(
                        "Pin must start with '" + PIN_PREFIX + "', got: " + pin);
            }
            String base64 = pin.substring(PIN_PREFIX.length());
            if (base64.isEmpty()) {
                throw new IllegalArgumentException("Pin hash must not be empty after prefix");
            }
            pins.add(pin);
            return this;
        }

        /**
         * Adds multiple pins.
         * 添加多个固定值。
         *
         * @param pins the pin strings - 固定字符串列表
         * @return this builder - 此构建器
         */
        public Builder addAll(String... pins) {
            for (String pin : pins) {
                add(pin);
            }
            return this;
        }

        /**
         * Builds the {@link CertificatePinner}.
         * 构建 {@link CertificatePinner}。
         *
         * @return the pinner - 固定器
         * @throws IllegalStateException if no pins have been added - 如果未添加任何固定值
         */
        public CertificatePinner build() {
            if (pins.isEmpty()) {
                throw new IllegalStateException("At least one pin must be added before building");
            }
            return new CertificatePinner(pins);
        }
    }
}
