package cloud.opencode.base.pdf;

import cloud.opencode.base.pdf.document.Metadata;
import cloud.opencode.base.pdf.document.PageSize;
import cloud.opencode.base.pdf.exception.OpenPdfException;
import cloud.opencode.base.pdf.form.PdfForm;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * PDF Document - Represents a PDF document with pages and content
 * PDF 文档 - 表示包含页面和内容的 PDF 文档
 *
 * <p>This interface provides access to PDF document structure including
 * pages, metadata, forms, and save operations.</p>
 * <p>此接口提供对 PDF 文档结构的访问，包括页面、元数据、表单和保存操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Page access and manipulation - 页面访问和操作</li>
 *   <li>Metadata management - 元数据管理</li>
 *   <li>Form access - 表单访问</li>
 *   <li>Document saving - 文档保存</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Open and read a document
 * try (PdfDocument doc = OpenPdf.open(Path.of("document.pdf"))) {
 *     int pages = doc.getPageCount();
 *     Metadata meta = doc.getMetadata();
 *     doc.save(Path.of("copy.pdf"));
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No — implementations are not thread-safe - 线程安全: 否 — 实现非线程安全</li>
 *   <li>Null-safe: Yes — parameters are validated - 空值安全: 是 — 参数已验证</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public interface PdfDocument extends AutoCloseable {

    // ==================== Page Access | 页面访问 ====================

    /**
     * Gets the total page count
     * 获取总页数
     *
     * @return page count | 页数
     */
    int getPageCount();

    /**
     * Gets a specific page (1-based index)
     * 获取指定页面（从1开始）
     *
     * @param pageNumber page number (1-based) | 页码（从1开始）
     * @return PDF page | PDF 页面
     * @throws OpenPdfException if page number is invalid | 页码无效时抛出异常
     */
    PdfPage getPage(int pageNumber);

    /**
     * Gets all pages
     * 获取所有页面
     *
     * @return list of pages | 页面列表
     */
    List<PdfPage> getPages();

    /**
     * Adds a new blank page
     * 添加新的空白页
     *
     * @return the new page | 新页面
     */
    PdfPage addPage();

    /**
     * Adds a new page with specified size
     * 添加指定大小的新页面
     *
     * @param pageSize page size | 页面大小
     * @return the new page | 新页面
     */
    PdfPage addPage(PageSize pageSize);

    /**
     * Inserts a page at specified position
     * 在指定位置插入页面
     *
     * @param pageNumber position (1-based) | 位置（从1开始）
     * @param page       page to insert | 要插入的页面
     * @throws OpenPdfException if position is invalid | 位置无效时抛出异常
     */
    void insertPage(int pageNumber, PdfPage page);

    /**
     * Removes a page
     * 删除页面
     *
     * @param pageNumber page number to remove (1-based) | 要删除的页码（从1开始）
     * @throws OpenPdfException if page number is invalid | 页码无效时抛出异常
     */
    void removePage(int pageNumber);

    // ==================== Metadata | 元数据 ====================

    /**
     * Gets document metadata
     * 获取文档元数据
     *
     * @return metadata | 元数据
     */
    Metadata getMetadata();

    /**
     * Sets document metadata
     * 设置文档元数据
     *
     * @param metadata metadata to set | 要设置的元数据
     */
    void setMetadata(Metadata metadata);

    // ==================== Form | 表单 ====================

    /**
     * Checks if document has interactive form
     * 检查文档是否有交互表单
     *
     * @return true if has form | 如果有表单返回 true
     */
    boolean hasForm();

    /**
     * Gets the interactive form
     * 获取交互表单
     *
     * @return PDF form, or null if none | PDF 表单，如果没有则返回 null
     */
    PdfForm getForm();

    // ==================== Save | 保存 ====================

    /**
     * Saves document to file
     * 保存文档到文件
     *
     * @param path target file path | 目标文件路径
     * @throws OpenPdfException if saving fails | 保存失败时抛出异常
     */
    void save(Path path);

    /**
     * Saves document to output stream
     * 保存文档到输出流
     *
     * @param outputStream target stream | 目标输出流
     * @throws OpenPdfException if saving fails | 保存失败时抛出异常
     */
    void save(OutputStream outputStream);

    /**
     * Saves document to byte array
     * 保存文档到字节数组
     *
     * @return PDF bytes | PDF 字节数组
     */
    byte[] toBytes();

    // ==================== Encryption | 加密 ====================

    /**
     * Checks if document is encrypted
     * 检查文档是否加密
     *
     * @return true if encrypted | 如果加密返回 true
     */
    boolean isEncrypted();

    /**
     * Sets document password protection
     * 设置文档密码保护
     *
     * @param userPassword  user password (for opening) | 用户密码（用于打开）
     * @param ownerPassword owner password (for permissions) | 所有者密码（用于权限）
     */
    void setPassword(String userPassword, String ownerPassword);

    // ==================== Resource Release | 资源释放 ====================

    /**
     * Closes the document and releases resources
     * 关闭文档并释放资源
     */
    @Override
    void close();
}
