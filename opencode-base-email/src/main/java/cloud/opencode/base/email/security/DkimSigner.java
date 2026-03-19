package cloud.opencode.base.email.security;

import cloud.opencode.base.email.exception.EmailSecurityException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;
import java.util.Enumeration;
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
 * DkimSigner.sign(mimeMessage, dkim);
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
     * Sign a message with DKIM
     * 使用DKIM签名消息
     *
     * @param message the message to sign | 要签名的消息
     * @param config  the DKIM configuration | DKIM配置
     * @throws EmailSecurityException if signing fails | 签名失败时抛出
     */
    public static void sign(MimeMessage message, DkimConfig config) {
        if (config == null) {
            return;
        }

        try {
            // Save changes to ensure headers are finalized
            message.saveChanges();

            // Build the DKIM signature
            String signature = buildSignature(message, config);

            // Add the signature header
            message.setHeader(DKIM_SIGNATURE_HEADER, signature);

        } catch (Exception e) {
            throw new EmailSecurityException("Failed to sign message with DKIM", e);
        }
    }

    /**
     * Build DKIM signature value
     */
    private static String buildSignature(MimeMessage message, DkimConfig config) throws Exception {
        String domain = config.domain();
        String selector = config.selector();
        PrivateKey privateKey = config.privateKey();
        Set<String> headersToSign = config.headersToSign();

        // Get body hash (bh)
        String bodyHash = computeBodyHash(message);

        // Build header list (h)
        String headerList = buildHeaderList(message, headersToSign);

        // Current timestamp
        long timestamp = Instant.now().getEpochSecond();

        // Build signature data template (without b= value)
        String signatureTemplate = buildSignatureTemplate(
                domain, selector, headerList, bodyHash, timestamp
        );

        // Canonicalize headers for signing
        String canonicalizedHeaders = canonicalizeHeaders(message, headersToSign);

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
        return "v=1; a=" + ALGORITHM + "; c=" + CANONICALIZATION + "; " +
                "d=" + domain + "; s=" + selector + "; " +
                "t=" + timestamp + "; " +
                "h=" + headerList + "; " +
                "bh=" + bodyHash + "; " +
                "b=";
    }

    /**
     * Compute body hash (bh tag)
     */
    private static String computeBodyHash(MimeMessage message) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        message.writeTo(baos);
        String fullMessage = baos.toString(StandardCharsets.UTF_8);

        // Extract body (after first blank line)
        int bodyStart = fullMessage.indexOf("\r\n\r\n");
        if (bodyStart == -1) {
            bodyStart = fullMessage.indexOf("\n\n");
        }
        String body = bodyStart >= 0 ? fullMessage.substring(bodyStart + 4) : "";

        // Apply relaxed body canonicalization
        body = relaxedBodyCanonicalization(body);

        // Compute SHA-256 hash
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(body.getBytes(StandardCharsets.UTF_8));

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
     * Build header list for h= tag
     */
    private static String buildHeaderList(MimeMessage message, Set<String> headersToSign)
            throws MessagingException {
        StringJoiner joiner = new StringJoiner(":");
        for (String header : headersToSign) {
            if (message.getHeader(header) != null) {
                joiner.add(header.toLowerCase());
            }
        }
        return joiner.toString();
    }

    /**
     * Canonicalize headers for signing
     */
    private static String canonicalizeHeaders(MimeMessage message, Set<String> headersToSign)
            throws MessagingException {
        StringBuilder sb = new StringBuilder();
        for (String headerName : headersToSign) {
            String[] values = message.getHeader(headerName);
            if (values != null && values.length > 0) {
                sb.append(relaxedHeaderCanonicalization(headerName, values[0]));
                sb.append("\r\n");
            }
        }
        return sb.toString();
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
        StringBuilder sb = new StringBuilder();
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
