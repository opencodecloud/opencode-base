package cloud.opencode.base.pdf.internal;

import cloud.opencode.base.pdf.PdfDocument;
import cloud.opencode.base.pdf.PdfPage;
import cloud.opencode.base.pdf.document.Metadata;
import cloud.opencode.base.pdf.document.PageSize;
import cloud.opencode.base.pdf.exception.OpenPdfException;
import cloud.opencode.base.pdf.form.PdfForm;
import cloud.opencode.base.pdf.internal.parser.PdfObject;
import cloud.opencode.base.pdf.internal.parser.PdfParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Default PDF Document Implementation - Read-only PDF document backed by parsed PDF data
 * 默认 PDF 文档实现 - 由解析后的 PDF 数据支持的只读 PDF 文档
 *
 * <p>Implements {@link PdfDocument} for reading existing PDF files. Provides full
 * support for page access, metadata extraction, and text extraction. Write operations
 * (addPage, insertPage, removePage, etc.) are not supported and will throw
 * {@link UnsupportedOperationException}.</p>
 * <p>实现 {@link PdfDocument} 以读取现有 PDF 文件。完全支持页面访问、元数据提取和文本提取。
 * 写操作（addPage、insertPage、removePage 等）不受支持，将抛出 {@link UnsupportedOperationException}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Open PDF from Path, byte[], or InputStream - 从 Path、byte[] 或 InputStream 打开 PDF</li>
 *   <li>Page count and page access - 页数和页面访问</li>
 *   <li>Metadata extraction from Info dictionary - 从 Info 字典提取元数据</li>
 *   <li>Text extraction per page - 每页文本提取</li>
 *   <li>Encryption detection via trailer /Encrypt - 通过 trailer /Encrypt 检测加密</li>
 *   <li>File size limit: 500 MB - 文件大小限制: 500 MB</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No — not designed for concurrent access - 线程安全: 否 — 非并发访问设计</li>
 *   <li>File size checked before reading (500 MB max) - 读取前检查文件大小（最大 500 MB）</li>
 *   <li>Defensive byte array copy on toBytes() - toBytes() 上的防御性字节数组复制</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
public final class DefaultPdfDocument implements PdfDocument {

    /** Maximum file size: 500 MB | 最大文件大小: 500 MB */
    private static final long MAX_FILE_SIZE = 500L * 1024 * 1024;

    private final byte[] rawData;
    private final PdfParser.ParsedPdf parsed;
    private final List<DefaultPdfPage> pages;
    private Metadata metadata;
    private boolean closed;

    private DefaultPdfDocument(byte[] rawData, PdfParser.ParsedPdf parsed) {
        this.rawData = rawData;
        this.parsed = parsed;

        List<PdfObject.PdfDictionary> pageDicts = parsed.getPages();
        List<DefaultPdfPage> pageList = new ArrayList<>(pageDicts.size());
        for (int i = 0; i < pageDicts.size(); i++) {
            pageList.add(new DefaultPdfPage(parsed, pageDicts.get(i), i + 1));
        }
        this.pages = List.copyOf(pageList);
    }

    /**
     * Opens a PDF document from file path
     * 从文件路径打开 PDF 文档
     *
     * @param path file path | 文件路径
     * @return PDF document | PDF 文档
     * @throws OpenPdfException if reading or parsing fails | 读取或解析失败时抛出异常
     */
    public static DefaultPdfDocument open(Path path) {
        Objects.requireNonNull(path, "path cannot be null");
        try {
            long fileSize = Files.size(path);
            if (fileSize > MAX_FILE_SIZE) {
                throw new OpenPdfException("read",
                        "File size " + fileSize + " bytes exceeds maximum " + MAX_FILE_SIZE + " bytes");
            }
            byte[] data = Files.readAllBytes(path);
            return open(data);
        } catch (IOException e) {
            throw OpenPdfException.readFailed(path.toString(), e);
        }
    }

    /**
     * Opens a PDF document from byte array
     * 从字节数组打开 PDF 文档
     *
     * @param data PDF byte data | PDF 字节数据
     * @return PDF document | PDF 文档
     * @throws OpenPdfException if parsing fails | 解析失败时抛出异常
     */
    public static DefaultPdfDocument open(byte[] data) {
        Objects.requireNonNull(data, "data cannot be null");
        if (data.length > MAX_FILE_SIZE) {
            throw new OpenPdfException("read",
                    "Data size " + data.length + " bytes exceeds maximum " + MAX_FILE_SIZE + " bytes");
        }
        PdfParser.ParsedPdf parsed = PdfParser.parse(data);
        return new DefaultPdfDocument(data.clone(), parsed);
    }

    /**
     * Opens a PDF document from input stream
     * 从输入流打开 PDF 文档
     *
     * @param inputStream input stream | 输入流
     * @return PDF document | PDF 文档
     * @throws OpenPdfException if reading or parsing fails | 读取或解析失败时抛出异常
     */
    public static DefaultPdfDocument open(InputStream inputStream) {
        Objects.requireNonNull(inputStream, "inputStream cannot be null");
        try {
            byte[] data = inputStream.readNBytes((int) MAX_FILE_SIZE + 1);
            if (data.length > MAX_FILE_SIZE) {
                throw new OpenPdfException("read",
                        "Input stream exceeds maximum size: " + MAX_FILE_SIZE + " bytes");
            }
            return open(data);
        } catch (IOException e) {
            throw OpenPdfException.readFailed("InputStream", e);
        }
    }

    /**
     * Gets the underlying parsed PDF for internal use
     * 获取底层解析的 PDF 供内部使用
     *
     * @return parsed PDF | 解析后的 PDF
     */
    public PdfParser.ParsedPdf getParsed() {
        return parsed;
    }

    // ==================== PdfDocument Interface | PdfDocument 接口 ====================

    @Override
    public int getPageCount() {
        ensureOpen();
        return pages.size();
    }

    @Override
    public PdfPage getPage(int pageNumber) {
        ensureOpen();
        if (pageNumber < 1 || pageNumber > pages.size()) {
            throw OpenPdfException.invalidPageNumber(pageNumber, pages.size());
        }
        return pages.get(pageNumber - 1);
    }

    @Override
    public List<PdfPage> getPages() {
        ensureOpen();
        return List.copyOf(pages);
    }

    @Override
    public PdfPage addPage() {
        throw new UnsupportedOperationException("Read-only document: addPage not supported");
    }

    @Override
    public PdfPage addPage(PageSize pageSize) {
        throw new UnsupportedOperationException("Read-only document: addPage not supported");
    }

    @Override
    public void insertPage(int pageNumber, PdfPage page) {
        throw new UnsupportedOperationException("Read-only document: insertPage not supported");
    }

    @Override
    public void removePage(int pageNumber) {
        throw new UnsupportedOperationException("Read-only document: removePage not supported");
    }

    @Override
    public Metadata getMetadata() {
        ensureOpen();
        if (metadata == null) {
            metadata = extractMetadata();
        }
        return metadata;
    }

    @Override
    public void setMetadata(Metadata metadata) {
        throw new UnsupportedOperationException("Read-only document: setMetadata not supported");
    }

    @Override
    public boolean hasForm() {
        ensureOpen();
        // Check if Root catalog has /AcroForm
        PdfObject rootRef = parsed.getTrailer().get("Root");
        PdfObject root = parsed.resolve(rootRef);
        if (root instanceof PdfObject.PdfDictionary catalog) {
            return catalog.containsKey("AcroForm");
        }
        return false;
    }

    @Override
    public PdfForm getForm() {
        ensureOpen();
        return null; // Form parsing not implemented
    }

    @Override
    public void save(Path path) {
        ensureOpen();
        Objects.requireNonNull(path, "path cannot be null");
        try {
            Files.write(path, rawData);
        } catch (IOException e) {
            throw OpenPdfException.writeFailed(path.toString(), e);
        }
    }

    @Override
    public void save(OutputStream outputStream) {
        ensureOpen();
        Objects.requireNonNull(outputStream, "outputStream cannot be null");
        try {
            outputStream.write(rawData);
            outputStream.flush();
        } catch (IOException e) {
            throw OpenPdfException.writeFailed("OutputStream", e);
        }
    }

    @Override
    public byte[] toBytes() {
        ensureOpen();
        return rawData.clone();
    }

    @Override
    public boolean isEncrypted() {
        ensureOpen();
        return parsed.isEncrypted();
    }

    @Override
    public void setPassword(String userPassword, String ownerPassword) {
        throw new UnsupportedOperationException("Read-only document: setPassword not supported");
    }

    @Override
    public void close() {
        closed = true;
    }

    // ==================== Internal Methods | 内部方法 ====================

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Document is closed");
        }
    }

    private Metadata extractMetadata() {
        PdfObject.PdfDictionary info = parsed.getInfo();
        if (info == null) {
            return Metadata.empty();
        }

        String title = info.getString("Title");
        String author = info.getString("Author");
        String subject = info.getString("Subject");
        String creator = info.getString("Creator");
        String producer = info.getString("Producer");

        // Parse keywords (stored as single string, may be comma-separated)
        String keywordsStr = info.getString("Keywords");
        List<String> keywords = List.of();
        if (keywordsStr != null && !keywordsStr.isBlank()) {
            keywords = List.of(keywordsStr.split(",\\s*"));
        }

        // Parse dates
        Instant creationDate = parsePdfDate(info.getString("CreationDate"));
        Instant modDate = parsePdfDate(info.getString("ModDate"));

        return new Metadata(title, author, subject, keywords, creator, producer, creationDate, modDate);
    }

    /**
     * Parses PDF date format: D:YYYYMMDDHHmmSS[+/-HH'mm']
     * 解析 PDF 日期格式
     */
    private static Instant parsePdfDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;

        // Remove "D:" prefix
        String s = dateStr;
        if (s.startsWith("D:")) {
            s = s.substring(2);
        }

        try {
            // Extract components
            int year = s.length() >= 4 ? Integer.parseInt(s.substring(0, 4)) : 2000;
            int month = s.length() >= 6 ? Integer.parseInt(s.substring(4, 6)) : 1;
            int day = s.length() >= 8 ? Integer.parseInt(s.substring(6, 8)) : 1;
            int hour = s.length() >= 10 ? Integer.parseInt(s.substring(8, 10)) : 0;
            int min = s.length() >= 12 ? Integer.parseInt(s.substring(10, 12)) : 0;
            int sec = s.length() >= 14 ? Integer.parseInt(s.substring(12, 14)) : 0;

            java.time.LocalDateTime ldt = java.time.LocalDateTime.of(year, month, day, hour, min, sec);
            return ldt.toInstant(java.time.ZoneOffset.UTC);
        } catch (Exception e) {
            return null;
        }
    }
}
