package cloud.opencode.base.pdf.operation;

import cloud.opencode.base.pdf.PdfDocument;
import cloud.opencode.base.pdf.exception.OpenPdfException;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * PDF Merger
 * PDF 合并器
 *
 * <p>Merges multiple PDF documents into one.</p>
 * <p>将多个 PDF 文档合并为一个。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Merge multiple PDF files - 合并多个 PDF 文件</li>
 *   <li>Merge specific pages - 合并指定页面</li>
 *   <li>Keep bookmarks and annotations - 保留书签和注释</li>
 *   <li>Add outline entries - 添加大纲条目</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Merge multiple PDFs
 * PdfMerger.create()
 *     .add(Path.of("doc1.pdf"))
 *     .add(Path.of("doc2.pdf"))
 *     .keepBookmarks(true)
 *     .mergeTo(Path.of("merged.pdf"));
 *
 * // Merge specific pages
 * PdfMerger.create()
 *     .addPages(Path.of("document.pdf"), "1-3", "5", "7-10")
 *     .mergeTo(Path.of("selected.pdf"));
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
public final class PdfMerger {

    private final List<MergeSource> sources = new ArrayList<>();
    private boolean keepBookmarks = true;
    private boolean keepAnnotations = true;
    private boolean addOutlines = false;

    private PdfMerger() {}

    // ==================== 添加文档 Add Documents ====================

    /**
     * Adds a PDF file to merge.
     * 添加要合并的 PDF 文件。
     *
     * @param path PDF file path | PDF 文件路径
     * @return this merger | 当前合并器
     */
    public PdfMerger add(Path path) {
        Objects.requireNonNull(path, "path cannot be null");
        sources.add(new MergeSource(path, null, null, null));
        return this;
    }

    /**
     * Adds a PDF document to merge.
     * 添加要合并的 PDF 文档。
     *
     * @param document PDF document | PDF 文档
     * @return this merger | 当前合并器
     */
    public PdfMerger add(PdfDocument document) {
        Objects.requireNonNull(document, "document cannot be null");
        sources.add(new MergeSource(null, document, null, null));
        return this;
    }

    /**
     * Adds PDF from input stream.
     * 从输入流添加 PDF。
     *
     * @param inputStream PDF input stream | PDF 输入流
     * @return this merger | 当前合并器
     */
    public PdfMerger add(InputStream inputStream) {
        Objects.requireNonNull(inputStream, "inputStream cannot be null");
        sources.add(new MergeSource(null, null, inputStream, null));
        return this;
    }

    /**
     * Adds specific pages from a PDF.
     * 从 PDF 添加指定页面。
     *
     * @param path       PDF file path | PDF 文件路径
     * @param pageRanges page ranges (e.g., "1-3", "5", "7-10") | 页面范围
     * @return this merger | 当前合并器
     */
    public PdfMerger addPages(Path path, String... pageRanges) {
        Objects.requireNonNull(path, "path cannot be null");
        Objects.requireNonNull(pageRanges, "pageRanges cannot be null");
        sources.add(new MergeSource(path, null, null, List.of(pageRanges)));
        return this;
    }

    /**
     * Adds multiple PDF files.
     * 添加多个 PDF 文件。
     *
     * @param paths PDF file paths | PDF 文件路径
     * @return this merger | 当前合并器
     */
    public PdfMerger addAll(List<Path> paths) {
        Objects.requireNonNull(paths, "paths cannot be null");
        for (Path path : paths) {
            add(path);
        }
        return this;
    }

    // ==================== 合并选项 Merge Options ====================

    /**
     * Keeps bookmarks from source documents.
     * 保留源文档的书签。
     *
     * @param keep whether to keep | 是否保留
     * @return this merger | 当前合并器
     */
    public PdfMerger keepBookmarks(boolean keep) {
        this.keepBookmarks = keep;
        return this;
    }

    /**
     * Keeps annotations from source documents.
     * 保留源文档的注释。
     *
     * @param keep whether to keep | 是否保留
     * @return this merger | 当前合并器
     */
    public PdfMerger keepAnnotations(boolean keep) {
        this.keepAnnotations = keep;
        return this;
    }

    /**
     * Adds outline entries for each document.
     * 为每个文档添加大纲条目。
     *
     * @param add whether to add | 是否添加
     * @return this merger | 当前合并器
     */
    public PdfMerger addOutlines(boolean add) {
        this.addOutlines = add;
        return this;
    }

    // ==================== 执行合并 Execute Merge ====================

    /**
     * Merges and returns document.
     * 合并并返回文档。
     *
     * @return merged document | 合并后的文档
     * @throws OpenPdfException if merging fails | 合并失败时抛出异常
     */
    public PdfDocument merge() {
        if (sources.isEmpty()) {
            throw new IllegalStateException("No sources added for merging");
        }
        // Implementation will be provided by internal classes
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Merges and saves to file.
     * 合并并保存到文件。
     *
     * @param target target file path | 目标文件路径
     * @throws OpenPdfException if merging fails | 合并失败时抛出异常
     */
    public void mergeTo(Path target) {
        Objects.requireNonNull(target, "target cannot be null");
        if (sources.isEmpty()) {
            throw new IllegalStateException("No sources added for merging");
        }
        // Implementation will be provided by internal classes
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Merges and writes to stream.
     * 合并并写入流。
     *
     * @param outputStream target stream | 目标流
     * @throws OpenPdfException if merging fails | 合并失败时抛出异常
     */
    public void mergeTo(OutputStream outputStream) {
        Objects.requireNonNull(outputStream, "outputStream cannot be null");
        if (sources.isEmpty()) {
            throw new IllegalStateException("No sources added for merging");
        }
        // Implementation will be provided by internal classes
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // ==================== 访问方法 Accessors ====================

    public List<MergeSource> getSources() {
        return List.copyOf(sources);
    }

    public boolean isKeepBookmarks() {
        return keepBookmarks;
    }

    public boolean isKeepAnnotations() {
        return keepAnnotations;
    }

    public boolean isAddOutlines() {
        return addOutlines;
    }

    // ==================== 静态工厂 Static Factory ====================

    /**
     * Creates a new merger.
     * 创建新的合并器。
     *
     * @return PDF merger | PDF 合并器
     */
    public static PdfMerger create() {
        return new PdfMerger();
    }

    // ==================== 内部类 Inner Classes ====================

    /**
     * Merge Source
     * 合并源
     *
     * @param path       file path | 文件路径
     * @param document   PDF document | PDF 文档
     * @param stream     input stream | 输入流
     * @param pageRanges page ranges | 页面范围
     */
    public record MergeSource(
        Path path,
        PdfDocument document,
        InputStream stream,
        List<String> pageRanges
    ) {
        /**
         * Checks if this source has page ranges.
         * 检查此源是否有页面范围。
         *
         * @return true if has page ranges | 如果有页面范围返回 true
         */
        public boolean hasPageRanges() {
            return pageRanges != null && !pageRanges.isEmpty();
        }
    }
}
