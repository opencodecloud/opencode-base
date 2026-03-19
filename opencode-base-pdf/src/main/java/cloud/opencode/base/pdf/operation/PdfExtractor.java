package cloud.opencode.base.pdf.operation;

import cloud.opencode.base.pdf.PdfDocument;
import cloud.opencode.base.pdf.exception.OpenPdfException;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * PDF Content Extractor
 * PDF 内容提取器
 *
 * <p>Extracts text and images from PDF documents.</p>
 * <p>从 PDF 文档提取文本和图像。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Extract text from all pages - 从所有页面提取文本</li>
 *   <li>Extract text from specific pages - 从指定页面提取文本</li>
 *   <li>Extract images - 提取图像</li>
 *   <li>Extract metadata - 提取元数据</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Extract all text
 * String text = PdfExtractor.of(Path.of("document.pdf"))
 *     .extractText();
 *
 * // Extract text from specific pages
 * String text = PdfExtractor.of(Path.of("document.pdf"))
 *     .extractText(1, 2, 3);
 *
 * // Extract images
 * List<ExtractedImage> images = PdfExtractor.of(Path.of("document.pdf"))
 *     .extractImages();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No — not designed for concurrent use - 线程安全: 否 — 非并发设计</li>
 *   <li>Null-safe: Yes — parameters are validated - 空值安全: 是 — 参数已验证</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public final class PdfExtractor {

    private Path sourcePath;
    private PdfDocument sourceDocument;

    private PdfExtractor() {}

    // ==================== 源设置 Source Settings ====================

    /**
     * Sets source PDF file.
     * 设置源 PDF 文件。
     *
     * @param path PDF file path | PDF 文件路径
     * @return this extractor | 当前提取器
     */
    public PdfExtractor source(Path path) {
        this.sourcePath = Objects.requireNonNull(path, "path cannot be null");
        this.sourceDocument = null;
        return this;
    }

    /**
     * Sets source PDF document.
     * 设置源 PDF 文档。
     *
     * @param document PDF document | PDF 文档
     * @return this extractor | 当前提取器
     */
    public PdfExtractor source(PdfDocument document) {
        this.sourceDocument = Objects.requireNonNull(document, "document cannot be null");
        this.sourcePath = null;
        return this;
    }

    // ==================== 文本提取 Text Extraction ====================

    /**
     * Extracts all text from PDF.
     * 从 PDF 提取所有文本。
     *
     * @return extracted text | 提取的文本
     * @throws OpenPdfException if extraction fails | 提取失败时抛出异常
     */
    public String extractText() {
        validateSource();
        // Implementation will be provided by internal classes
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Extracts text from specific pages.
     * 从指定页面提取文本。
     *
     * @param pageNumbers page numbers (1-based) | 页码（从1开始）
     * @return extracted text | 提取的文本
     * @throws OpenPdfException if extraction fails | 提取失败时抛出异常
     */
    public String extractText(int... pageNumbers) {
        Objects.requireNonNull(pageNumbers, "pageNumbers cannot be null");
        for (int pageNumber : pageNumbers) {
            if (pageNumber < 1) {
                throw new IllegalArgumentException("Page numbers must be positive");
            }
        }
        validateSource();
        // Implementation will be provided by internal classes
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Extracts text from a page range.
     * 从页面范围提取文本。
     *
     * @param startPage start page (1-based) | 起始页码
     * @param endPage   end page (1-based) | 结束页码
     * @return extracted text | 提取的文本
     * @throws OpenPdfException if extraction fails | 提取失败时抛出异常
     */
    public String extractTextRange(int startPage, int endPage) {
        if (startPage < 1 || endPage < 1) {
            throw new IllegalArgumentException("Page numbers must be positive");
        }
        if (startPage > endPage) {
            throw new IllegalArgumentException("Start page must not exceed end page");
        }
        validateSource();
        // Implementation will be provided by internal classes
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // ==================== 图像提取 Image Extraction ====================

    /**
     * Extracts all images from PDF.
     * 从 PDF 提取所有图像。
     *
     * @return list of extracted images | 提取的图像列表
     * @throws OpenPdfException if extraction fails | 提取失败时抛出异常
     */
    public List<ExtractedImage> extractImages() {
        validateSource();
        // Implementation will be provided by internal classes
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Extracts images from specific pages.
     * 从指定页面提取图像。
     *
     * @param pageNumbers page numbers (1-based) | 页码（从1开始）
     * @return list of extracted images | 提取的图像列表
     * @throws OpenPdfException if extraction fails | 提取失败时抛出异常
     */
    public List<ExtractedImage> extractImages(int... pageNumbers) {
        Objects.requireNonNull(pageNumbers, "pageNumbers cannot be null");
        for (int pageNumber : pageNumbers) {
            if (pageNumber < 1) {
                throw new IllegalArgumentException("Page numbers must be positive");
            }
        }
        validateSource();
        // Implementation will be provided by internal classes
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Extracts images as byte arrays.
     * 将图像提取为字节数组。
     *
     * @return list of image byte arrays | 图像字节数组列表
     * @throws OpenPdfException if extraction fails | 提取失败时抛出异常
     */
    public List<byte[]> extractImageBytes() {
        validateSource();
        // Implementation will be provided by internal classes
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Saves extracted images to directory.
     * 将提取的图像保存到目录。
     *
     * @param directory  target directory | 目标目录
     * @param namePrefix file name prefix | 文件名前缀
     * @return list of saved file paths | 保存的文件路径列表
     * @throws OpenPdfException if extraction fails | 提取失败时抛出异常
     */
    public List<Path> saveImages(Path directory, String namePrefix) {
        Objects.requireNonNull(directory, "directory cannot be null");
        Objects.requireNonNull(namePrefix, "namePrefix cannot be null");
        validateSource();
        // Implementation will be provided by internal classes
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void validateSource() {
        if (sourcePath == null && sourceDocument == null) {
            throw new IllegalStateException("Source must be set before extraction");
        }
    }

    // ==================== 访问方法 Accessors ====================

    public Path getSourcePath() {
        return sourcePath;
    }

    public PdfDocument getSourceDocument() {
        return sourceDocument;
    }

    // ==================== 静态工厂 Static Factory ====================

    /**
     * Creates a new extractor.
     * 创建新的提取器。
     *
     * @return PDF extractor | PDF 提取器
     */
    public static PdfExtractor create() {
        return new PdfExtractor();
    }

    /**
     * Creates extractor for file.
     * 为文件创建提取器。
     *
     * @param path PDF file path | PDF 文件路径
     * @return PDF extractor | PDF 提取器
     */
    public static PdfExtractor of(Path path) {
        return new PdfExtractor().source(path);
    }

    /**
     * Creates extractor for document.
     * 为文档创建提取器。
     *
     * @param document PDF document | PDF 文档
     * @return PDF extractor | PDF 提取器
     */
    public static PdfExtractor of(PdfDocument document) {
        return new PdfExtractor().source(document);
    }

    // ==================== 内部类 Inner Classes ====================

    /**
     * Extracted Image
     * 提取的图像
     *
     * @param data       image data | 图像数据
     * @param format     image format | 图像格式
     * @param width      image width | 图像宽度
     * @param height     image height | 图像高度
     * @param pageNumber page number where found | 发现图像的页码
     */
    public record ExtractedImage(
        byte[] data,
        String format,
        int width,
        int height,
        int pageNumber
    ) {
        public ExtractedImage {
            Objects.requireNonNull(data, "data cannot be null");
            data = data.clone();
        }

        @Override
        public byte[] data() {
            return data.clone();
        }
    }
}
