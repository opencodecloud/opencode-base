package cloud.opencode.base.pdf.signature;

import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Objects;

/**
 * Timestamp Information
 * 时间戳信息
 *
 * <p>Contains details about a timestamp in a PDF signature.</p>
 * <p>包含 PDF 签名中时间戳的详细信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Timestamp time and validity status - 时间戳时间和有效性状态</li>
 *   <li>TSA certificate and name access - TSA 证书和名称访问</li>
 *   <li>Hash algorithm and serial number - 哈希算法和序列号</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimestampInfo ts = TimestampInfo.of(Instant.now());
 * Instant time = ts.time();
 * boolean valid = ts.isValid();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes — immutable record - 线程安全: 是 — 不可变记录</li>
 *   <li>Null-safe: Partial — time is validated as non-null, other fields may be null - 空值安全: 部分 — 时间已验证非空，其他字段可能为空</li>
 * </ul>
 *
 * @param time              timestamp time | 时间戳时间
 * @param tsaCertificate    TSA certificate | TSA 证书
 * @param hashAlgorithm     hash algorithm used | 使用的哈希算法
 * @param serialNumber      timestamp serial number | 时间戳序列号
 * @param isValid           whether timestamp is valid | 时间戳是否有效
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public record TimestampInfo(
    Instant time,
    X509Certificate tsaCertificate,
    String hashAlgorithm,
    String serialNumber,
    boolean isValid
) {

    /**
     * Creates a new timestamp info.
     * 创建新的时间戳信息。
     *
     * @param time              timestamp time | 时间戳时间
     * @param tsaCertificate    TSA certificate | TSA 证书
     * @param hashAlgorithm     hash algorithm used | 使用的哈希算法
     * @param serialNumber      timestamp serial number | 时间戳序列号
     * @param isValid           whether timestamp is valid | 时间戳是否有效
     */
    public TimestampInfo {
        Objects.requireNonNull(time, "time cannot be null");
    }

    /**
     * Gets TSA name from certificate.
     * 从证书获取 TSA 名称。
     *
     * @return TSA name, or null if no certificate | TSA 名称，如果没有证书则返回 null
     */
    public String getTsaName() {
        return tsaCertificate != null
            ? tsaCertificate.getSubjectX500Principal().getName()
            : null;
    }

    /**
     * Creates a valid timestamp info.
     * 创建有效的时间戳信息。
     *
     * @param time              timestamp time | 时间戳时间
     * @param tsaCertificate    TSA certificate | TSA 证书
     * @param hashAlgorithm     hash algorithm | 哈希算法
     * @param serialNumber      serial number | 序列号
     * @return timestamp info | 时间戳信息
     */
    public static TimestampInfo of(Instant time, X509Certificate tsaCertificate,
                                   String hashAlgorithm, String serialNumber) {
        return new TimestampInfo(time, tsaCertificate, hashAlgorithm, serialNumber, true);
    }

    /**
     * Creates a timestamp info with only time.
     * 创建仅包含时间的时间戳信息。
     *
     * @param time timestamp time | 时间戳时间
     * @return timestamp info | 时间戳信息
     */
    public static TimestampInfo of(Instant time) {
        return new TimestampInfo(time, null, null, null, true);
    }
}
