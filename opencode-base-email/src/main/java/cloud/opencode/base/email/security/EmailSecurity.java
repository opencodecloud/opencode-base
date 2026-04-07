package cloud.opencode.base.email.security;

import cloud.opencode.base.email.Attachment;
import cloud.opencode.base.email.exception.EmailErrorCode;
import cloud.opencode.base.email.exception.EmailSecurityException;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Email Security Utility Class
 * 邮件安全工具类
 *
 * <p>Provides security utilities for email operations.</p>
 * <p>提供邮件操作的安全工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Header injection prevention - 邮件头注入防护</li>
 *   <li>Email address validation - 邮箱地址验证</li>
 *   <li>Attachment security validation - 附件安全验证</li>
 *   <li>Content sanitization - 内容清理</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public final class EmailSecurity {

    private EmailSecurity() {
        // Utility class
    }

    /**
     * Email address validation pattern
     * 邮箱地址验证正则
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern CR_LF_PATTERN = Pattern.compile("[\\r\\n]");
    private static final Pattern CONTROL_CHAR_PATTERN = Pattern.compile("[\\u0000-\\u001f]");

    /**
     * Default allowed attachment extensions
     * 默认允许的附件扩展名
     */
    private static final Set<String> DEFAULT_ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "rtf",
            "png", "jpg", "jpeg", "gif", "bmp", "svg", "webp",
            "zip", "7z", "tar", "gz"
    );

    /**
     * Dangerous attachment extensions
     * 危险的附件扩展名
     */
    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "com", "msi", "scr", "pif",
            "vbs", "vbe", "js", "jse", "ws", "wsf", "wsc", "wsh",
            "ps1", "psm1", "psd1",
            "dll", "sys", "drv",
            "sh", "bash", "csh", "ksh", "zsh"
    );

    /**
     * Default max attachment size (10MB)
     * 默认最大附件大小（10MB）
     */
    private static final long DEFAULT_MAX_ATTACHMENT_SIZE = 10L * 1024 * 1024;

    /**
     * Sanitize email header content to prevent injection attacks
     * 清理邮件头内容以防止注入攻击
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * sanitizeHeader("Test\r\nBcc: hacker@evil.com") = "TestBcc: hacker@evil.com"
     * sanitizeHeader("Normal Subject")               = "Normal Subject"
     * </pre>
     *
     * @param value the header value | 邮件头值
     * @return the sanitized value | 清理后的值
     */
    public static String sanitizeHeader(String value) {
        if (value == null) {
            return null;
        }
        // Remove carriage returns and line feeds (prevent header injection)
        // Remove control characters
        return CONTROL_CHAR_PATTERN.matcher(CR_LF_PATTERN.matcher(value).replaceAll("")).replaceAll("");
    }

    /**
     * Validate email address format
     * 验证邮箱地址格式
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * isValidEmail("user@example.com")           = true
     * isValidEmail("user.name+tag@example.co.uk") = true
     * isValidEmail("invalid")                    = false
     * isValidEmail("user@")                      = false
     * </pre>
     *
     * @param email the email address | 邮箱地址
     * @return true if valid | 有效返回true
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate attachment security
     * 验证附件安全性
     *
     * @param attachment the attachment | 附件
     * @throws EmailSecurityException if validation fails | 验证失败时抛出
     */
    public static void validateAttachment(Attachment attachment) {
        validateAttachment(attachment, DEFAULT_ALLOWED_EXTENSIONS, DEFAULT_MAX_ATTACHMENT_SIZE);
    }

    /**
     * Validate attachment security with custom settings
     * 使用自定义设置验证附件安全性
     *
     * @param attachment        the attachment | 附件
     * @param allowedExtensions allowed file extensions | 允许的文件扩展名
     * @param maxSize           max file size in bytes | 最大文件大小（字节）
     * @throws EmailSecurityException if validation fails | 验证失败时抛出
     */
    public static void validateAttachment(Attachment attachment,
                                          Set<String> allowedExtensions,
                                          long maxSize) {
        if (attachment == null) {
            throw new EmailSecurityException("Attachment cannot be null",
                    EmailErrorCode.INVALID_ATTACHMENT);
        }

        String fileName = attachment.getFileName();
        if (fileName == null || fileName.isBlank()) {
            throw new EmailSecurityException("Attachment file name cannot be blank",
                    EmailErrorCode.INVALID_ATTACHMENT);
        }

        // Check for path traversal
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new EmailSecurityException("Invalid attachment file name: " + fileName,
                    EmailErrorCode.INVALID_ATTACHMENT);
        }

        // Check extension
        String ext = getExtension(fileName).toLowerCase();
        if (DANGEROUS_EXTENSIONS.contains(ext)) {
            throw new EmailSecurityException("Dangerous attachment type not allowed: " + ext,
                    EmailErrorCode.INVALID_ATTACHMENT);
        }

        if (!allowedExtensions.contains(ext)) {
            throw new EmailSecurityException("Attachment type not allowed: " + ext,
                    EmailErrorCode.INVALID_ATTACHMENT);
        }

        // Check size (reject negative sizes from corrupted files)
        long size = attachment.getSize();
        if (size < 0 || size > maxSize) {
            throw new EmailSecurityException(
                    String.format("Attachment size invalid or exceeds limit: %d (max %d bytes)", size, maxSize),
                    EmailErrorCode.INVALID_ATTACHMENT);
        }
    }

    /**
     * Check if attachment extension is allowed
     * 检查附件扩展名是否允许
     *
     * @param fileName the file name | 文件名
     * @return true if allowed | 允许返回true
     */
    public static boolean isAllowedExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        String ext = getExtension(fileName).toLowerCase();
        return DEFAULT_ALLOWED_EXTENSIONS.contains(ext) && !DANGEROUS_EXTENSIONS.contains(ext);
    }

    /**
     * Check if attachment extension is dangerous
     * 检查附件扩展名是否危险
     *
     * @param fileName the file name | 文件名
     * @return true if dangerous | 危险返回true
     */
    public static boolean isDangerousExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        String ext = getExtension(fileName).toLowerCase();
        return DANGEROUS_EXTENSIONS.contains(ext);
    }

    /**
     * Get file extension
     * 获取文件扩展名
     *
     * @param fileName the file name | 文件名
     * @return the extension without dot | 不带点的扩展名
     */
    public static String getExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot >= fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1);
    }

    /**
     * Get default allowed extensions
     * 获取默认允许的扩展名
     *
     * @return the allowed extensions | 允许的扩展名
     */
    public static Set<String> getDefaultAllowedExtensions() {
        return DEFAULT_ALLOWED_EXTENSIONS;
    }

    /**
     * Get dangerous extensions
     * 获取危险扩展名
     *
     * @return the dangerous extensions | 危险扩展名
     */
    public static Set<String> getDangerousExtensions() {
        return DANGEROUS_EXTENSIONS;
    }

    /**
     * Get default max attachment size
     * 获取默认最大附件大小
     *
     * @return the max size in bytes | 最大字节大小
     */
    public static long getDefaultMaxAttachmentSize() {
        return DEFAULT_MAX_ATTACHMENT_SIZE;
    }
}
