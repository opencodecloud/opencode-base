package cloud.opencode.base.pdf.internal;

import cloud.opencode.base.pdf.PdfPage;
import cloud.opencode.base.pdf.content.*;
import cloud.opencode.base.pdf.document.Orientation;
import cloud.opencode.base.pdf.document.PageSize;
import cloud.opencode.base.pdf.internal.parser.PdfObject;
import cloud.opencode.base.pdf.internal.parser.PdfParser;
import cloud.opencode.base.pdf.internal.parser.TextExtractor;

/**
 * Default PDF Page Implementation - Read-only page backed by parsed PDF data
 * 默认 PDF 页面实现 - 由解析后的 PDF 数据支持的只读页面
 *
 * <p>Implements {@link PdfPage} for reading existing PDF pages. Supports text extraction
 * and page dimension queries. Content-adding methods are not supported for parsed
 * (read-only) documents.</p>
 * <p>实现 {@link PdfPage} 以读取现有 PDF 页面。支持文本提取和页面尺寸查询。
 * 解析后的（只读）文档不支持添加内容的方法。</p>
 *
 * <p><strong>Features | 主���功能:</strong></p>
 * <ul>
 *   <li>Text extraction from page content streams - 从页面内容流提取文本</li>
 *   <li>Page dimensions from /MediaBox - 从 /MediaBox 获取页面尺寸</li>
 *   <li>Page rotation from /Rotate - 从 /Rotate 获取页面旋���</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No — shares parsed PDF state - 线程安全: 否 — 共享解析后的 PDF 状态</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
public final class DefaultPdfPage implements PdfPage {

    private final PdfParser.ParsedPdf pdf;
    private final PdfObject.PdfDictionary pageDict;
    private final int pageNumber;

    /**
     * Creates a default page from parsed data
     * 从解析后的数据创建默认页面
     *
     * @param pdf        parsed PDF | 解析后的 PDF
     * @param pageDict   page dictionary | 页面字典
     * @param pageNumber 1-based page number | 基于1的页码
     */
    DefaultPdfPage(PdfParser.ParsedPdf pdf, PdfObject.PdfDictionary pageDict, int pageNumber) {
        this.pdf = pdf;
        this.pageDict = pageDict;
        this.pageNumber = pageNumber;
    }

    // ==================== Page Properties | 页面属性 ====================

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public PageSize getPageSize() {
        float w = getWidth();
        float h = getHeight();
        // Try to match known page sizes
        for (PageSize size : PageSize.values()) {
            if (Math.abs(size.getWidth() - w) < 2 && Math.abs(size.getHeight() - h) < 2) {
                return size;
            }
            if (Math.abs(size.getHeight() - w) < 2 && Math.abs(size.getWidth() - h) < 2) {
                return size;
            }
        }
        return PageSize.A4; // Default fallback
    }

    @Override
    public float getWidth() {
        float[] mediaBox = getMediaBox();
        return mediaBox[2] - mediaBox[0];
    }

    @Override
    public float getHeight() {
        float[] mediaBox = getMediaBox();
        return mediaBox[3] - mediaBox[1];
    }

    @Override
    public Orientation getOrientation() {
        return getWidth() > getHeight() ? Orientation.LANDSCAPE : Orientation.PORTRAIT;
    }

    @Override
    public void setRotation(int degrees) {
        throw new UnsupportedOperationException("Read-only page: setRotation not supported");
    }

    @Override
    public int getRotation() {
        return pageDict.getInt("Rotate", 0);
    }

    // ==================== Content Addition (Unsupported) | 添加内容（不支持）====================

    @Override
    public PdfPage addText(String text, float x, float y) {
        throw new UnsupportedOperationException("Read-only page: addText not supported");
    }

    @Override
    public PdfPage addText(PdfText pdfText) {
        throw new UnsupportedOperationException("Read-only page: addText not supported");
    }

    @Override
    public PdfPage addParagraph(PdfParagraph paragraph) {
        throw new UnsupportedOperationException("Read-only page: addParagraph not supported");
    }

    @Override
    public PdfPage addImage(PdfImage image) {
        throw new UnsupportedOperationException("Read-only page: addImage not supported");
    }

    @Override
    public PdfPage addTable(PdfTable table) {
        throw new UnsupportedOperationException("Read-only page: addTable not supported");
    }

    @Override
    public PdfPage addLine(PdfLine line) {
        throw new UnsupportedOperationException("Read-only page: addLine not supported");
    }

    @Override
    public PdfPage addRectangle(PdfRectangle rectangle) {
        throw new UnsupportedOperationException("Read-only page: addRectangle not supported");
    }

    // ==================== Text Extraction | 文本提取 ====================

    @Override
    public String extractText() {
        return TextExtractor.extractText(pdf, pageNumber - 1);
    }

    // ==================== Private Methods | 私有方法 ====================

    /**
     * Gets the MediaBox [llx, lly, urx, ury] from the page dictionary,
     * inheriting from parent Pages node if not directly specified.
     * 从页面字典获取 MediaBox，如果未直接指定则从父 Pages 节点继承。
     */
    private float[] getMediaBox() {
        PdfObject.PdfArray mediaBox = pageDict.getArray("MediaBox");
        if (mediaBox == null) {
            // Try to resolve if it's a reference
            PdfObject mediaBoxObj = pageDict.get("MediaBox");
            if (mediaBoxObj != null) {
                PdfObject resolved = pdf.resolve(mediaBoxObj);
                if (resolved instanceof PdfObject.PdfArray arr) {
                    mediaBox = arr;
                }
            }
        }
        if (mediaBox != null && mediaBox.size() >= 4) {
            return new float[]{
                    getFloat(mediaBox.get(0)),
                    getFloat(mediaBox.get(1)),
                    getFloat(mediaBox.get(2)),
                    getFloat(mediaBox.get(3))
            };
        }
        // Default: A4
        return new float[]{0, 0, 595, 842};
    }

    private static float getFloat(PdfObject obj) {
        if (obj instanceof PdfObject.PdfNumber n) return n.floatValue();
        return 0;
    }
}
