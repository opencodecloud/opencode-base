package cloud.opencode.base.email.attachment;

import cloud.opencode.base.email.Attachment;
import cloud.opencode.base.email.exception.EmailException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * File-based Email Attachment
 * 基于文件的邮件附件
 *
 * <p>Attachment implementation for local file system files.</p>
 * <p>本地文件系统文件的附件实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Load from Path - 从Path加载</li>
 *   <li>Auto-detect MIME type - 自动检测MIME类型</li>
 *   <li>Lazy loading - 延迟加载</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Attachment attachment = new FileAttachment(Path.of("report.pdf"));
 * Email email = Email.builder()
 *     .attach(attachment)
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
public final class FileAttachment implements Attachment {

    private final Path path;
    private final String fileName;
    private final String contentType;

    /**
     * Create file attachment from path
     * 从路径创建文件附件
     *
     * @param path the file path | 文件路径
     */
    public FileAttachment(Path path) {
        this(path, null, null);
    }

    /**
     * Create file attachment with custom file name
     * 创建带自定义文件名的文件附件
     *
     * @param path     the file path | 文件路径
     * @param fileName the display file name | 显示的文件名
     */
    public FileAttachment(Path path, String fileName) {
        this(path, fileName, null);
    }

    /**
     * Create file attachment with custom file name and content type
     * 创建带自定义文件名和内容类型的文件附件
     *
     * @param path        the file path | 文件路径
     * @param fileName    the display file name | 显示的文件名
     * @param contentType the MIME type | MIME类型
     */
    public FileAttachment(Path path, String fileName, String contentType) {
        if (path == null) {
            throw new EmailException("File path cannot be null");
        }
        if (!Files.exists(path)) {
            throw new EmailException("File not found: " + path);
        }
        if (!Files.isRegularFile(path)) {
            throw new EmailException("Path is not a regular file: " + path);
        }

        this.path = path;
        this.fileName = fileName != null ? fileName : path.getFileName().toString();
        this.contentType = contentType != null ? contentType : detectContentType(path);
    }

    /**
     * Create file attachment from path
     * 从路径创建文件附件
     *
     * @param path the file path | 文件路径
     * @return the file attachment | 文件附件
     */
    public static FileAttachment of(Path path) {
        return new FileAttachment(path);
    }

    /**
     * Create file attachment from path string
     * 从路径字符串创建文件附件
     *
     * @param path the file path string | 文件路径字符串
     * @return the file attachment | 文件附件
     */
    public static FileAttachment of(String path) {
        return new FileAttachment(Path.of(path));
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
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new EmailException("Failed to read attachment file: " + path, e);
        }
    }

    @Override
    public long getSize() {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new EmailException("Failed to get file size: " + path, e);
        }
    }

    /**
     * Get the file path
     * 获取文件路径
     *
     * @return the path | 路径
     */
    public Path getPath() {
        return path;
    }

    private static String detectContentType(Path path) {
        try {
            String type = Files.probeContentType(path);
            return type != null ? type : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    @Override
    public String toString() {
        return "FileAttachment{" +
                "fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", path=" + path +
                '}';
    }
}
