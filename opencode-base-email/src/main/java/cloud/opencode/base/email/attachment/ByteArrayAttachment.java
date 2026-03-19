package cloud.opencode.base.email.attachment;

import cloud.opencode.base.email.Attachment;
import cloud.opencode.base.email.exception.EmailException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Byte Array Email Attachment
 * 字节数组邮件附件
 *
 * <p>Attachment implementation for in-memory byte data.</p>
 * <p>内存字节数据的附件实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>In-memory data - 内存数据</li>
 *   <li>No file system dependency - 无文件系统依赖</li>
 *   <li>Defensive copy for security - 防御性拷贝确保安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * byte[] data = generatePdfReport();
 * Attachment attachment = new ByteArrayAttachment("report.pdf", data, "application/pdf");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable: Yes (defensive copy) - 不可变: 是（防御性拷贝）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public final class ByteArrayAttachment implements Attachment {

    private final String fileName;
    private final byte[] data;
    private final String contentType;

    /**
     * Create byte array attachment
     * 创建字节数组附件
     *
     * @param fileName the file name | 文件名
     * @param data     the byte data | 字节数据
     */
    public ByteArrayAttachment(String fileName, byte[] data) {
        this(fileName, data, "application/octet-stream");
    }

    /**
     * Create byte array attachment with content type
     * 创建带内容类型的字节数组附件
     *
     * @param fileName    the file name | 文件名
     * @param data        the byte data | 字节数据
     * @param contentType the MIME type | MIME类型
     */
    public ByteArrayAttachment(String fileName, byte[] data, String contentType) {
        if (fileName == null || fileName.isBlank()) {
            throw new EmailException("File name cannot be null or blank");
        }
        if (data == null) {
            throw new EmailException("Data cannot be null");
        }

        this.fileName = fileName;
        this.data = Arrays.copyOf(data, data.length); // Defensive copy
        this.contentType = contentType != null ? contentType : "application/octet-stream";
    }

    /**
     * Create byte array attachment
     * 创建字节数组附件
     *
     * @param fileName    the file name | 文件名
     * @param data        the byte data | 字节数据
     * @param contentType the MIME type | MIME类型
     * @return the byte array attachment | 字节数组附件
     */
    public static ByteArrayAttachment of(String fileName, byte[] data, String contentType) {
        return new ByteArrayAttachment(fileName, data, contentType);
    }

    /**
     * Create byte array attachment
     * 创建字节数组附件
     *
     * @param fileName the file name | 文件名
     * @param data     the byte data | 字节数据
     * @return the byte array attachment | 字节数组附件
     */
    public static ByteArrayAttachment of(String fileName, byte[] data) {
        return new ByteArrayAttachment(fileName, data);
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
        return "ByteArrayAttachment{" +
                "fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", size=" + data.length +
                '}';
    }
}
