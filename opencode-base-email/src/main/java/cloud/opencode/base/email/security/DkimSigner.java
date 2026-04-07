package cloud.opencode.base.email.security;

import cloud.opencode.base.email.exception.EmailSecurityException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;

/**
 * DKIM Message Signer
 * DKIM消息签名器
 *
 * <p>Signs email messages with DKIM (DomainKeys Identified Mail).</p>
 * <p>使用DKIM签名邮件消息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RSA-SHA256 signing - RSA-SHA256签名</li>
 *   <li>Relaxed/relaxed canonicalization - 宽松/宽松规范化</li>
 *   <li>Configurable headers to sign - 可配置签名邮件头</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DkimConfig dkim = DkimConfig.load("example.com", "mail", keyPath);
 * String signedMessage = DkimSigner.sign(rawMessage, dkim);
 * }</pre>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://tools.ietf.org/html/rfc6376">RFC 6376 - DKIM</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public final class DkimSigner {

    private static final String DKIM_SIGNATURE_HEADER = "DKIM-Signature";
    private static final String ALGORITHM = "rsa-sha256";
    private static final String CANONICALIZATION = "relaxed/relaxed";

    private static final Pattern HEADER_UNFOLD_PATTERN = Pattern.compile("\\r\\n[ \\t]+");
    private static final Pattern WHITESPACE_REDUCE_PATTERN = Pattern.compile("[ \\t]+");
    private static final Pattern LINE_ENDING_PATTERN = Pattern.compile("\\r\\n|\\r|\\n");
    private static final Pattern TRAILING_WHITESPACE_PATTERN = Pattern.compile("[ \\t]+$");
    private static final Pattern CRLF_PATTERN = Pattern.compile("\\r\\n");

    private DkimSigner() {
        // Utility class
    }

    /**
     * Sign a raw MIME message with DKIM
     * 使用DKIM签名原始MIME消息
     *
     * @param rawMessage the raw MIME message string | 原始MIME消息字符串
     * @param config     the DKIM configuration | DKIM配置
     * @return the signed message with DKIM-Signature header prepended | 带有DKIM-Signature头的签名消息
     * @throws EmailSecurityException if signing fails | 签名失败时抛出
     */
    public static String sign(String rawMessage, DkimConfig config) {
        if (config == null) {
            return rawMessage;
        }

        try {
            // Build the DKIM signature
            String signature = buildSignature(rawMessage, config);

            // Prepend DKIM-Signature as the first header in the message
            return DKIM_SIGNATURE_HEADER + ": " + signature + "\r\n" + rawMessage;

        } catch (Exception e) {
            throw new EmailSecurityException("Failed to sign message with DKIM", e);
        }
    }

    /**
     * Build DKIM signature value
     */
    private static String buildSignature(String rawMessage, DkimConfig config) throws Exception {
        String domain = config.domain();
        String selector = config.selector();
        PrivateKey privateKey = config.privateKey();
        Set<String> headersToSign = config.headersToSign();

        // Split message into headers and body at the first blank line
        String headers = "";
        String body = "";
        int bodySeparator = rawMessage.indexOf("\r\n\r\n");
        if (bodySeparator >= 0) {
            headers = rawMessage.substring(0, bodySeparator);
            body = rawMessage.substring(bodySeparator + 4);
        } else {
            int lfSeparator = rawMessage.indexOf("\n\n");
            if (lfSeparator >= 0) {
                headers = rawMessage.substring(0, lfSeparator);
                body = rawMessage.substring(lfSeparator + 2);
            } else {
                headers = rawMessage;
            }
        }

        // Get body hash (bh)
        String bodyHash = computeBodyHash(body);

        // Normalize and split header section once (avoids redundant regex work per header)
        String normalizedHeaders = LINE_ENDING_PATTERN.matcher(headers).replaceAll("\r\n");
        String[] headerLines = CRLF_PATTERN.split(normalizedHeaders, -1);

        // Build header list (h)
        String headerList = buildHeaderList(headerLines, headersToSign);

        // Current timestamp
        long timestamp = Instant.now().getEpochSecond();

        // Build signature data template (without b= value)
        String signatureTemplate = buildSignatureTemplate(
                domain, selector, headerList, bodyHash, timestamp
        );

        // Canonicalize headers for signing
        String canonicalizedHeaders = canonicalizeHeaders(headerLines, headersToSign);

        // Add the DKIM-Signature header itself (without b= value) to the signing data
        String dkimHeaderForSigning = relaxedHeaderCanonicalization(
                DKIM_SIGNATURE_HEADER, signatureTemplate
        );
        String dataToSign = canonicalizedHeaders + dkimHeaderForSigning;

        // Sign
        String signatureValue = computeSignature(dataToSign, privateKey);

        // Return complete signature with b= value
        return signatureTemplate + signatureValue;
    }

    /**
     * Build signature template without b= value
     */
    private static String buildSignatureTemplate(String domain, String selector,
                                                  String headerList, String bodyHash, long timestamp) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("v=1; a=").append(ALGORITHM)
          .append("; c=").append(CANONICALIZATION)
          .append("; d=").append(domain)
          .append("; s=").append(selector)
          .append("; t=").append(timestamp)
          .append("; h=").append(headerList)
          .append("; bh=").append(bodyHash)
          .append("; b=");
        return sb.toString();
    }

    /**
     * Compute body hash (bh tag)
     */
    private static String computeBodyHash(String body) throws Exception {
        // Apply relaxed body canonicalization
        String canonicalBody = relaxedBodyCanonicalization(body);

        // Compute SHA-256 hash
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(canonicalBody.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Compute RSA-SHA256 signature
     */
    private static String computeSignature(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    /**
     * Build header list for h= tag using pre-split header lines.
     * 使用预分割的邮件头行构建 h= 标签的邮件头列表。
     *
     * @param headerLines  pre-normalized and pre-split header lines | 预规范化并预分割的邮件头行
     * @param headersToSign set of header names to sign | 要签名的邮件头名称集合
     * @return colon-separated list of header names present in the message | 消息中存在的邮件头名称（冒号分隔）
     */
    private static String buildHeaderList(String[] headerLines, Set<String> headersToSign) {
        StringJoiner joiner = new StringJoiner(":");
        for (String header : headersToSign) {
            if (findHeaderValueFromLines(headerLines, header) != null) {
                joiner.add(header.toLowerCase());
            }
        }
        return joiner.toString();
    }

    /**
     * Canonicalize headers for signing using pre-split header lines.
     * 使用预分割的邮件头行规范化签名邮件头。
     *
     * @param headerLines  pre-normalized and pre-split header lines | 预规范化并预分割的邮件头行
     * @param headersToSign set of header names to sign | 要签名的邮件头名称集合
     * @return canonicalized header string for signing | 用于签名的规范化邮件头字符串
     */
    private static String canonicalizeHeaders(String[] headerLines, Set<String> headersToSign) {
        StringBuilder sb = new StringBuilder();
        for (String headerName : headersToSign) {
            String value = findHeaderValueFromLines(headerLines, headerName);
            if (value != null) {
                sb.append(relaxedHeaderCanonicalization(headerName, value));
                sb.append("\r\n");
            }
        }
        return sb.toString();
    }

    /**
     * Find a header value from pre-split header lines by name (case-insensitive).
     * Handles folded headers (continuation lines starting with whitespace).
     * 从预分割的邮件头行中按名称（不区分大小写）查找邮件头值。
     * 处理折叠邮件头（以空白字符开头的续行）。
     *
     * @param lines      pre-normalized, CRLF-split header lines | 预规范化的 CRLF 分割邮件头行
     * @param headerName the header name to find | 要查找的邮件头名称
     * @return the header value, or null if not found | 邮件头值，未找到返回 null
     */
    private static String findHeaderValueFromLines(String[] lines, String headerName) {
        String lowerName = headerName.toLowerCase();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int colonIdx = line.indexOf(':');
            if (colonIdx > 0) {
                String name = line.substring(0, colonIdx).trim();
                if (name.toLowerCase().equals(lowerName)) {
                    // Found the header; collect value including any continuation lines
                    StringBuilder value = new StringBuilder(line.substring(colonIdx + 1));
                    for (int j = i + 1; j < lines.length; j++) {
                        if (!lines[j].isEmpty() && (lines[j].charAt(0) == ' ' || lines[j].charAt(0) == '\t')) {
                            value.append("\r\n").append(lines[j]);
                        } else {
                            break;
                        }
                    }
                    return value.toString().trim();
                }
            }
        }
        return null;
    }

    /**
     * Relaxed header canonicalization (RFC 6376 Section 3.4.2)
     */
    private static String relaxedHeaderCanonicalization(String name, String value) {
        // Convert header name to lowercase
        String canonName = name.toLowerCase().trim();

        // Unfold header value and reduce whitespace
        String canonValue = HEADER_UNFOLD_PATTERN.matcher(value).replaceAll(" ");  // Unfold
        canonValue = WHITESPACE_REDUCE_PATTERN.matcher(canonValue).replaceAll(" ")  // Reduce WSP
                .trim();

        return canonName + ":" + canonValue;
    }

    /**
     * Relaxed body canonicalization (RFC 6376 Section 3.4.4)
     */
    private static String relaxedBodyCanonicalization(String body) {
        if (body == null || body.isEmpty()) {
            return "\r\n";
        }

        // Normalize line endings to CRLF
        body = LINE_ENDING_PATTERN.matcher(body).replaceAll("\r\n");

        // Reduce whitespace at end of lines
        StringBuilder sb = new StringBuilder(body.length() + 64);
        for (String line : CRLF_PATTERN.split(body, -1)) {
            // Reduce WSP sequences to single SP
            String canonLine = WHITESPACE_REDUCE_PATTERN.matcher(line).replaceAll(" ");
            // Remove trailing whitespace
            canonLine = TRAILING_WHITESPACE_PATTERN.matcher(canonLine).replaceAll("");
            sb.append(canonLine).append("\r\n");
        }

        String result = sb.toString();

        // Remove trailing empty lines (but keep one CRLF)
        while (result.endsWith("\r\n\r\n")) {
            result = result.substring(0, result.length() - 2);
        }

        return result;
    }
}
