package cloud.opencode.base.email.attachment;

import cloud.opencode.base.email.Attachment;
import cloud.opencode.base.email.exception.EmailException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Inline Email Attachment for HTML Embedding
 * 用于HTML嵌入的内嵌邮件附件
 *
 * <p>Attachment implementation for inline content in HTML emails.</p>
 * <p>HTML邮件中内嵌内容的附件实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Content-ID for HTML reference - 用于HTML引用的Content-ID</li>
 *   <li>Image embedding support - 图片嵌入支持</li>
 *   <li>Use with cid: URLs in HTML - 在HTML中使用cid:URL</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create inline image
 * InlineAttachment logo = InlineAttachment.of("logo", Path.of("logo.png"), "image/png");
 *
 * // Reference in HTML
 * String html = "<img src=\"cid:logo\" alt=\"Logo\"/>";
 *
 * Email email = Email.builder()
 *     .html(html)
 *     .attach(logo)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable: Yes - 不可变: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public final class InlineAttachment implements Attachment {

    private final String contentId;
    private final String fileName;
    private final byte[] data;
    private final String contentType;

    /**
     * Create inline attachment from byte data
     * 从字节数据创建内嵌附件
     *
     * @param contentId   the Content-ID for HTML reference | 用于HTML引用的Content-ID
     * @param fileName    the file name | 文件名
     * @param data        the byte data | 字节数据
     * @param contentType the MIME type | MIME类型
     */
    public InlineAttachment(String contentId, String fileName, byte[] data, String contentType) {
        if (contentId == null || contentId.isBlank()) {
            throw new EmailException("Content-ID cannot be null or blank");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new EmailException("File name cannot be null or blank");
        }
        if (data == null) {
            throw new EmailException("Data cannot be null");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new EmailException("Content type cannot be null or blank");
        }

        this.contentId = contentId;
        this.fileName = fileName;
        this.data = Arrays.copyOf(data, data.length);
        this.contentType = contentType;
    }

    /**
     * Create inline attachment from file
     * 从文件创建内嵌附件
     *
     * @param contentId   the Content-ID for HTML reference | 用于HTML引用的Content-ID
     * @param path        the file path | 文件路径
     * @param contentType the MIME type | MIME类型
     * @return the inline attachment | 内嵌附件
     */
    public static InlineAttachment of(String contentId, Path path, String contentType) {
        try {
            byte[] data = Files.readAllBytes(path);
            return new InlineAttachment(contentId, path.getFileName().toString(), data, contentType);
        } catch (IOException e) {
            throw new EmailException("Failed to read file: " + path, e);
        }
    }

    /**
     * Create inline attachment from byte data
     * 从字节数据创建内嵌附件
     *
     * @param contentId   the Content-ID for HTML reference | 用于HTML引用的Content-ID
     * @param fileName    the file name | 文件名
     * @param data        the byte data | 字节数据
     * @param contentType the MIME type | MIME类型
     * @return the inline attachment | 内嵌附件
     */
    public static InlineAttachment of(String contentId, String fileName, byte[] data, String contentType) {
        return new InlineAttachment(contentId, fileName, data, contentType);
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(data);
    }

    @Override
    public long getSize() {
        return data.length;
    }

    @Override
    public boolean isInline() {
        return true;
    }

    @Override
    public String getContentId() {
        return contentId;
    }

    /**
     * Get a copy of the byte data
     * 获取字节数据的拷贝
     *
     * @return copy of the data | 数据拷贝
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public String toString() {
        return "InlineAttachment{" +
                "contentId='" + contentId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", size=" + data.length +
                '}';
    }
}
