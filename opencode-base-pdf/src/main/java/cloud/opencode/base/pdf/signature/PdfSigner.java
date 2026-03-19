package cloud.opencode.base.pdf.signature;

import cloud.opencode.base.pdf.PdfDocument;
import cloud.opencode.base.pdf.exception.OpenPdfException;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

/**
 * PDF Digital Signer
 * PDF 数字签名器
 *
 * <p>Signs PDF documents with digital signatures using PKCS#7.</p>
 * <p>使用 PKCS#7 对 PDF 文档进行数字签名。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Digital signature with certificates - 使用证书进行数字签名</li>
 *   <li>Visible signature appearance - 可见的签名外观</li>
 *   <li>Timestamp support - 时间戳支持</li>
 *   <li>Multiple signature fields - 多签名字段</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Sign a PDF
 * PdfSigner.create()
 *     .keyStore(Path.of("keystore.p12"), "password".toCharArray(), "PKCS12")
 *     .alias("mycert")
 *     .reason("Contract Approval")
 *     .location("New York")
 *     .sign(Path.of("document.pdf"), Path.of("signed.pdf"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No — not designed for concurrent use - 线程安全: 否 — 非并发设计</li>
 *   <li>Null-safe: Yes — parameters are validated - 空值安全: 是 — 参数已验证</li>
 *   <li>Sensitive data: Key store passwords are cloned defensively - 敏感数据: 密钥库密码进行了防御性克隆</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public final class PdfSigner {

    private Path keyStorePath;
    private InputStream keyStoreStream;
    private char[] keyStorePassword;
    private String keyStoreType = "PKCS12";
    private String alias;
    private char[] keyPassword;
    private SignatureAppearance appearance;
    private String reason;
    private String location;
    private String contact;
    private String fieldName;
    private int pageNumber;
    private float x;
    private float y;
    private float width;
    private float height;
    private String tsaUrl;
    private String tsaUsername;
    private String tsaPassword;

    private PdfSigner() {}

    // ==================== 签名配置 Signature Configuration ====================

    /**
     * Sets the key store for signing.
     * 设置签名用的密钥库。
     *
     * @param keyStorePath key store file path | 密钥库文件路径
     * @param password     key store password | 密钥库密码
     * @param type         key store type (e.g., "PKCS12", "JKS") | 密钥库类型
     * @return this signer | 当前签名器
     */
    public PdfSigner keyStore(Path keyStorePath, char[] password, String type) {
        this.keyStorePath = Objects.requireNonNull(keyStorePath, "keyStorePath cannot be null");
        this.keyStorePassword = Objects.requireNonNull(password, "password cannot be null").clone();
        this.keyStoreType = Objects.requireNonNull(type, "type cannot be null");
        this.keyStoreStream = null;
        return this;
    }

    /**
     * Sets the key store from input stream.
     * 从输入流设置密钥库。
     *
     * @param keyStoreStream key store stream | 密钥库流
     * @param password       key store password | 密钥库密码
     * @param type           key store type | 密钥库类型
     * @return this signer | 当前签名器
     */
    public PdfSigner keyStore(InputStream keyStoreStream, char[] password, String type) {
        this.keyStoreStream = Objects.requireNonNull(keyStoreStream, "keyStoreStream cannot be null");
        this.keyStorePassword = Objects.requireNonNull(password, "password cannot be null").clone();
        this.keyStoreType = Objects.requireNonNull(type, "type cannot be null");
        this.keyStorePath = null;
        return this;
    }

    /**
     * Sets the certificate alias to use.
     * 设置要使用的证书别名。
     *
     * @param alias certificate alias | 证书别名
     * @return this signer | 当前签名器
     */
    public PdfSigner alias(String alias) {
        this.alias = Objects.requireNonNull(alias, "alias cannot be null");
        return this;
    }

    /**
     * Sets private key password (if different from keystore).
     * 设置私钥密码（如果与密钥库不同）。
     *
     * @param password private key password | 私钥密码
     * @return this signer | 当前签名器
     */
    public PdfSigner keyPassword(char[] password) {
        this.keyPassword = password != null ? password.clone() : null;
        return this;
    }

    // ==================== 签名外观 Signature Appearance ====================

    /**
     * Sets signature appearance.
     * 设置签名外观。
     *
     * @param appearance signature appearance | 签名外观
     * @return this signer | 当前签名器
     */
    public PdfSigner appearance(SignatureAppearance appearance) {
        this.appearance = appearance;
        return this;
    }

    /**
     * Sets signature reason.
     * 设置签名原因。
     *
     * @param reason signing reason | 签名原因
     * @return this signer | 当前签名器
     */
    public PdfSigner reason(String reason) {
        this.reason = reason;
        return this;
    }

    /**
     * Sets signature location.
     * 设置签名位置。
     *
     * @param location signing location | 签名位置
     * @return this signer | 当前签名器
     */
    public PdfSigner location(String location) {
        this.location = location;
        return this;
    }

    /**
     * Sets signature contact info.
     * 设置签名联系信息。
     *
     * @param contact contact information | 联系信息
     * @return this signer | 当前签名器
     */
    public PdfSigner contact(String contact) {
        this.contact = contact;
        return this;
    }

    /**
     * Sets signature field name.
     * 设置签名字段名。
     *
     * @param fieldName signature field name | 签名字段名
     * @return this signer | 当前签名器
     */
    public PdfSigner fieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    /**
     * Sets signature rectangle position.
     * 设置签名矩形位置。
     *
     * @param pageNumber page number | 页码
     * @param x          x coordinate | x 坐标
     * @param y          y coordinate | y 坐标
     * @param width      width | 宽度
     * @param height     height | 高度
     * @return this signer | 当前签名器
     */
    public PdfSigner rectangle(int pageNumber, float x, float y, float width, float height) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be positive");
        }
        this.pageNumber = pageNumber;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }

    // ==================== 时间戳 Timestamp ====================

    /**
     * Enables timestamp.
     * 启用时间戳。
     *
     * @param tsaUrl TSA server URL | TSA 服务器 URL
     * @return this signer | 当前签名器
     */
    public PdfSigner timestamp(String tsaUrl) {
        this.tsaUrl = Objects.requireNonNull(tsaUrl, "tsaUrl cannot be null");
        return this;
    }

    /**
     * Enables timestamp with authentication.
     * 启用带认证的时间戳。
     *
     * @param tsaUrl   TSA server URL | TSA 服务器 URL
     * @param username TSA username | TSA 用户名
     * @param password TSA password | TSA 密码
     * @return this signer | 当前签名器
     */
    public PdfSigner timestamp(String tsaUrl, String username, String password) {
        this.tsaUrl = Objects.requireNonNull(tsaUrl, "tsaUrl cannot be null");
        this.tsaUsername = username;
        this.tsaPassword = password;
        return this;
    }

    // ==================== 签名执行 Signature Execution ====================

    /**
     * Signs a PDF document.
     * 对 PDF 文档签名。
     *
     * @param source source PDF | 源 PDF
     * @return signed PDF document | 签名后的 PDF 文档
     * @throws OpenPdfException if signing fails | 签名失败时抛出异常
     */
    public PdfDocument sign(Path source) {
        Objects.requireNonNull(source, "source cannot be null");
        validateConfiguration();
        // Implementation will be provided by internal classes
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Signs and saves to file.
     * 签名并保存到文件。
     *
     * @param source source PDF | 源 PDF
     * @param target target file | 目标文件
     * @throws OpenPdfException if signing fails | 签名失败时抛出异常
     */
    public void sign(Path source, Path target) {
        Objects.requireNonNull(source, "source cannot be null");
        Objects.requireNonNull(target, "target cannot be null");
        validateConfiguration();
        // Implementation will be provided by internal classes
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Signs a PDF document object.
     * 对 PDF 文档对象签名。
     *
     * @param document PDF document | PDF 文档
     * @return signed PDF document | 签名后的 PDF 文档
     * @throws OpenPdfException if signing fails | 签名失败时抛出异常
     */
    public PdfDocument sign(PdfDocument document) {
        Objects.requireNonNull(document, "document cannot be null");
        validateConfiguration();
        // Implementation will be provided by internal classes
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void validateConfiguration() {
        if (keyStorePath == null && keyStoreStream == null) {
            throw new IllegalStateException("Key store must be configured");
        }
        if (alias == null) {
            throw new IllegalStateException("Certificate alias must be configured");
        }
    }

    // ==================== 访问方法 Accessors ====================

    public Path getKeyStorePath() {
        return keyStorePath;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public String getAlias() {
        return alias;
    }

    public SignatureAppearance getAppearance() {
        return appearance;
    }

    public String getReason() {
        return reason;
    }

    public String getLocation() {
        return location;
    }

    public String getContact() {
        return contact;
    }

    public String getFieldName() {
        return fieldName;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public String getTsaUrl() {
        return tsaUrl;
    }

    // ==================== 静态工厂 Static Factory ====================

    /**
     * Creates a new signer.
     * 创建新的签名器。
     *
     * @return PDF signer | PDF 签名器
     */
    public static PdfSigner create() {
        return new PdfSigner();
    }
}
