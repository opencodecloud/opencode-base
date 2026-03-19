package cloud.opencode.base.pdf.signature;

import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Signature Information
 * 签名信息
 *
 * <p>Contains details about a PDF digital signature.</p>
 * <p>包含 PDF 数字签名的详细信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Signature metadata (name, reason, location, contact) - 签名元数据（名称、原因、位置、联系方式）</li>
 *   <li>Certificate chain access - 证书链访问</li>
 *   <li>Timestamp information - 时间戳信息</li>
 *   <li>Validity and coverage status - 有效性和覆盖状态</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SignatureInfo info = SignatureInfo.builder()
 *     .name("Contract")
 *     .reason("Approval")
 *     .valid(true)
 *     .build();
 *
 * String signer = info.getSignerName();
 * boolean hasTs = info.hasTimestamp();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes — immutable record - 线程安全: 是 — 不可变记录</li>
 *   <li>Null-safe: Partial — fields may be null - 空值安全: 部分 — 字段可能为空</li>
 *   <li>Defensive copies: Certificate list is copied on construction - 防御性拷贝: 证书列表在构造时进行拷贝</li>
 * </ul>
 *
 * @param name              signature name | 签名名称
 * @param reason            signing reason | 签名原因
 * @param location          signing location | 签名位置
 * @param contactInfo       contact information | 联系信息
 * @param signDate          signing date | 签名日期
 * @param certificates      certificate chain | 证书链
 * @param timestampInfo     timestamp info (if present) | 时间戳信息
 * @param isValid           whether signature is valid | 签名是否有效
 * @param coversWholeDoc    whether signature covers entire document | 签名是否覆盖整个文档
 * @param revisionNumber    revision number | 修订号
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public record SignatureInfo(
    String name,
    String reason,
    String location,
    String contactInfo,
    Instant signDate,
    List<X509Certificate> certificates,
    TimestampInfo timestampInfo,
    boolean isValid,
    boolean coversWholeDoc,
    int revisionNumber
) {

    /**
     * Creates a new signature info.
     * 创建新的签名信息。
     */
    public SignatureInfo {
        certificates = certificates != null ? List.copyOf(certificates) : List.of();
    }

    /**
     * Gets the signer certificate.
     * 获取签名者证书。
     *
     * @return signer certificate, or null if none | 签名者证书，如果没有则返回 null
     */
    public X509Certificate getSignerCertificate() {
        return certificates.isEmpty() ? null : certificates.getFirst();
    }

    /**
     * Gets signer name from certificate.
     * 从证书获取签名者名称。
     *
     * @return signer name | 签名者名称
     */
    public String getSignerName() {
        var cert = getSignerCertificate();
        return cert != null ? cert.getSubjectX500Principal().getName() : null;
    }

    /**
     * Checks if signature has timestamp.
     * 检查签名是否有时间戳。
     *
     * @return true if has timestamp | 如果有时间戳返回 true
     */
    public boolean hasTimestamp() {
        return timestampInfo != null;
    }

    /**
     * Creates a builder for signature info.
     * 创建签名信息构建器。
     *
     * @return signature info builder | 签名信息构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Signature Info Builder
     * 签名信息构建器
     */
    public static final class Builder {
        private String name;
        private String reason;
        private String location;
        private String contactInfo;
        private Instant signDate;
        private List<X509Certificate> certificates;
        private TimestampInfo timestampInfo;
        private boolean isValid;
        private boolean coversWholeDoc;
        private int revisionNumber;

        private Builder() {}

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder contactInfo(String contactInfo) {
            this.contactInfo = contactInfo;
            return this;
        }

        public Builder signDate(Instant signDate) {
            this.signDate = signDate;
            return this;
        }

        public Builder certificates(List<X509Certificate> certificates) {
            this.certificates = certificates;
            return this;
        }

        public Builder timestampInfo(TimestampInfo timestampInfo) {
            this.timestampInfo = timestampInfo;
            return this;
        }

        public Builder valid(boolean isValid) {
            this.isValid = isValid;
            return this;
        }

        public Builder coversWholeDoc(boolean coversWholeDoc) {
            this.coversWholeDoc = coversWholeDoc;
            return this;
        }

        public Builder revisionNumber(int revisionNumber) {
            this.revisionNumber = revisionNumber;
            return this;
        }

        public SignatureInfo build() {
            return new SignatureInfo(
                name, reason, location, contactInfo, signDate,
                certificates, timestampInfo, isValid, coversWholeDoc, revisionNumber
            );
        }
    }
}
