package cloud.opencode.base.pdf.document;

import cloud.opencode.base.pdf.content.*;
import cloud.opencode.base.pdf.font.PdfFont;
import cloud.opencode.base.pdf.font.StandardFont;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.DeflaterOutputStream;

/**
 * PDF 1.4 Renderer — generates valid PDF binary from DocumentBuilder state.
 * PDF 1.4 渲染器 — 从 DocumentBuilder 状态生成有效的 PDF 二进制文件。
 *
 * <p>Renders text (PdfText), lines (PdfLine), rectangles (PdfRectangle),
 * ellipses (PdfEllipse), paragraphs (PdfParagraph), images (PdfImage),
 * tables (PdfTable), watermarks, and headers/footers into a
 * standards-compliant PDF 1.4 byte stream.</p>
 * <p>将文本、线条、矩形、椭圆、段落、图像、表格、水印和页眉页脚
 * 渲染为符合标准的 PDF 1.4 字节流。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>PDF 1.4 compliant output with catalog, pages tree, font resources - 符合 PDF 1.4 规范</li>
 *   <li>Standard 14 fonts support (Helvetica, Times, Courier families) - 支持标准 14 种字体</li>
 *   <li>Text, line, rectangle, filled rectangle, table rendering - 文本、线条、矩形、表格渲染</li>
 *   <li>Ellipse/circle rendering with Bezier curve approximation - 椭圆/圆形渲染（贝塞尔曲线逼近）</li>
 *   <li>Paragraph rendering with word-wrap, alignment, and first-line indent - 段落渲染（自动换行、对齐、首行缩进）</li>
 *   <li>JPEG and PNG image embedding as XObject streams - JPEG 和 PNG 图片嵌入为 XObject 流</li>
 *   <li>Text rotation, underline, character/word spacing - 文本旋转、下划线、字符/词间距</li>
 *   <li>Watermark rendering with transparency (ExtGState) - 水印渲染（带透明度 ExtGState）</li>
 *   <li>Header and footer rendering with page number substitution - 页眉页脚渲染（支持页码替换）</li>
 *   <li>Document metadata (Title, Author, Subject, Keywords, Creator) - 文档元数据</li>
 *   <li>Multi-page documents with per-page size/orientation - 多页文档，每页可独立设置尺寸和方向</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (single-use renderer per build call) - 线程安全: 否（每次构建调用单独使用）</li>
 *   <li>Null-safe: Yes (null text treated as empty) - 空值安全: 是（null 文本视为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
final class PdfRenderer {

    /**
     * Bezier control point coefficient for approximating a quarter-circle arc.
     * 贝塞尔控制点系数，用于逼近四分之一圆弧。
     */
    private static final float KAPPA = 0.5522847498f;

    /**
     * Approximate character width factor for Type1 fonts (characters * fontSize * factor).
     * Type1 字体的近似字符宽度因子。
     */
    private static final float CHAR_WIDTH_FACTOR = 0.5f;

    private final DocumentBuilder builder;

    /** Counter for dynamic font keys (starts at 20 to avoid collisions with F1-F7) */
    private int nextDynamicFontKey = 20;
    /** Cache for dynamic font keys to ensure stability across calls | 动态字体键缓存 */
    private final Map<String, String> dynamicFontKeyCache = new LinkedHashMap<>();

    PdfRenderer(DocumentBuilder builder) {
        this.builder = Objects.requireNonNull(builder);
    }

    /**
     * Render PDF to byte array.
     * 渲染 PDF 为字节数组。
     *
     * @return PDF bytes | PDF 字节数组
     */
    byte[] toBytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
        List<Integer> offsets = new ArrayList<>();
        List<PageBuilder> pages = builder.getPages();
        int totalPages = pages.size();
        if (pages.isEmpty()) {
            pages = List.of(new PageBuilder(builder, builder.getPageSize(), builder.getOrientation()));
            totalPages = 1;
        }

        // Collect all unique fonts used across pages
        Map<String, String> fontMap = collectFonts(pages);
        int fontCount = fontMap.size();

        // --- Phase 1: Collect images from all pages and assign object numbers ---
        List<ImageData> allImages = new ArrayList<>();
        // Map: pageIndex -> list of image indices in allImages
        Map<Integer, List<Integer>> pageImageIndices = new LinkedHashMap<>();
        for (int i = 0; i < pages.size(); i++) {
            List<Integer> imageIndices = new ArrayList<>();
            for (PdfElement element : pages.get(i).getElements()) {
                if (element instanceof PdfImage img) {
                    ImageData data = loadImageData(img);
                    if (data != null) {
                        imageIndices.add(allImages.size());
                        allImages.add(data);
                    }
                }
            }
            pageImageIndices.put(i, imageIndices);
        }

        // Determine if we need watermark ExtGState
        boolean hasWatermark = builder.getWatermark() != null;
        boolean hasHeader = builder.getHeader() != null;
        boolean hasFooter = builder.getFooter() != null;

        // --- Object number layout ---
        // Obj 1: Catalog
        // Obj 2: Pages
        // Obj 3..3+fontCount-1: Fonts
        // Then: ExtGState (if watermark), Image XObjects, then Page+Content pairs, then Info
        int objNum = 3;

        // Font objects
        Map<String, Integer> fontObjNums = new LinkedHashMap<>();
        for (var entry : fontMap.entrySet()) {
            fontObjNums.put(entry.getKey(), objNum);
            objNum++;
        }

        // ExtGState object (for watermark transparency)
        int extGStateObjNum = -1;
        if (hasWatermark) {
            extGStateObjNum = objNum;
            objNum++;
        }

        // Image XObject object numbers
        int[] imageObjNums = new int[allImages.size()];
        for (int i = 0; i < allImages.size(); i++) {
            imageObjNums[i] = objNum;
            objNum++;
            // If image has SMask, it needs an extra object
            if (allImages.get(i).smaskData != null) {
                allImages.get(i).smaskObjNum = objNum;
                objNum++;
            }
        }

        // Page + Content stream objects
        int firstPageObj = objNum;
        // Each page uses 2 objects (page dict + content stream)
        // objNum after pages: firstPageObj + pages.size() * 2

        // --- PDF Header ---
        write(out, "%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n");

        // --- Object 1: Catalog ---
        offsets.add(out.size());
        write(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        // --- Object 2: Pages ---
        offsets.add(out.size());
        StringBuilder pagesKids = new StringBuilder("2 0 obj\n<< /Type /Pages /Kids [");
        for (int i = 0; i < pages.size(); i++) {
            if (i > 0) pagesKids.append(" ");
            pagesKids.append(firstPageObj + i * 2).append(" 0 R");
        }
        pagesKids.append("] /Count ").append(pages.size()).append(" >>\nendobj\n");
        write(out, pagesKids.toString());

        // --- Font objects ---
        for (var entry : fontMap.entrySet()) {
            offsets.add(out.size());
            int fObjNum = fontObjNums.get(entry.getKey());
            write(out, fObjNum + " 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /"
                    + entry.getValue() + " >>\nendobj\n");
        }

        // --- ExtGState object for watermark ---
        if (hasWatermark) {
            offsets.add(out.size());
            float opacity = builder.getWatermark().getOpacity();
            write(out, extGStateObjNum + " 0 obj\n<< /Type /ExtGState /ca "
                    + fmt(opacity) + " >>\nendobj\n");
        }

        // --- Image XObject objects ---
        // Object numbers are assigned as: imageObjNums[i]=N, smaskObjNum=N+1 (if present).
        // Objects must be written in ascending object-number order so the offsets[] list
        // aligns correctly with the xref table (offsets[k] = file position of object k+1).
        for (int i = 0; i < allImages.size(); i++) {
            ImageData imgData = allImages.get(i);

            // Write image XObject first (object imageObjNums[i])
            offsets.add(out.size());
            StringBuilder imgObj = new StringBuilder();
            imgObj.append(imageObjNums[i]).append(" 0 obj\n<< /Type /XObject /Subtype /Image")
                    .append(" /Width ").append(imgData.pixelWidth)
                    .append(" /Height ").append(imgData.pixelHeight)
                    .append(" /ColorSpace /DeviceRGB /BitsPerComponent 8");
            if (imgData.isJpeg) {
                imgObj.append(" /Filter /DCTDecode");
            } else {
                imgObj.append(" /Filter /FlateDecode");
            }
            imgObj.append(" /Length ").append(imgData.streamData.length);
            if (imgData.smaskData != null) {
                imgObj.append(" /SMask ").append(imgData.smaskObjNum).append(" 0 R");
            }
            imgObj.append(" >>\nstream\n");
            write(out, imgObj.toString());
            writeBytes(out, imgData.streamData);
            write(out, "\nendstream\nendobj\n");

            // Write SMask object second (object smaskObjNum = imageObjNums[i]+1)
            if (imgData.smaskData != null) {
                offsets.add(out.size());
                write(out, imgData.smaskObjNum + " 0 obj\n<< /Type /XObject /Subtype /Image"
                        + " /Width " + imgData.pixelWidth + " /Height " + imgData.pixelHeight
                        + " /ColorSpace /DeviceGray /BitsPerComponent 8"
                        + " /Filter /FlateDecode /Length " + imgData.smaskData.length
                        + " >>\nstream\n");
                writeBytes(out, imgData.smaskData);
                write(out, "\nendstream\nendobj\n");
            }
        }

        // --- Font resources dict ---
        StringBuilder fontResources = new StringBuilder("<< ");
        for (var entry : fontObjNums.entrySet()) {
            fontResources.append("/").append(entry.getKey()).append(" ")
                    .append(entry.getValue()).append(" 0 R ");
        }
        fontResources.append(">>");
        String fontResStr = fontResources.toString();

        // --- Page objects + content streams ---
        int imageCounter = 0; // global image counter for naming /ImN
        for (int i = 0; i < pages.size(); i++) {
            PageBuilder page = pages.get(i);
            float w = page.getWidth();
            float h = page.getHeight();
            int pageObjNum = firstPageObj + i * 2;
            int contentObjNum = pageObjNum + 1;

            // Build image name map for this page: /ImN -> objNum
            List<Integer> pageImgIdx = pageImageIndices.getOrDefault(i, List.of());
            Map<Integer, String> imgElementToName = new LinkedHashMap<>();
            StringBuilder xobjRes = new StringBuilder();
            if (!pageImgIdx.isEmpty()) {
                xobjRes.append(" /XObject << ");
                for (int idx : pageImgIdx) {
                    String name = "Im" + idx;
                    xobjRes.append("/").append(name).append(" ")
                            .append(imageObjNums[idx]).append(" 0 R ");
                }
                xobjRes.append(">>");
            }

            // Build ExtGState resources
            String extGStateRes = "";
            if (hasWatermark) {
                extGStateRes = " /ExtGState << /GS1 " + extGStateObjNum + " 0 R >>";
            }

            // Render content stream
            String stream = renderPageStream(page, fontMap, pageImgIdx, allImages,
                    imageObjNums, i, totalPages, w, h);
            byte[] streamBytes = stream.getBytes(StandardCharsets.ISO_8859_1);

            // Page object
            offsets.add(out.size());
            write(out, pageObjNum + " 0 obj\n<< /Type /Page /Parent 2 0 R"
                    + " /MediaBox [0 0 " + fmt(w) + " " + fmt(h) + "]"
                    + " /Contents " + contentObjNum + " 0 R"
                    + " /Resources << /Font " + fontResStr
                    + extGStateRes
                    + xobjRes
                    + " >> >>\nendobj\n");

            // Content stream
            offsets.add(out.size());
            write(out, contentObjNum + " 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n");
            writeBytes(out, streamBytes);
            write(out, "\nendstream\nendobj\n");
        }

        // --- Info dictionary ---
        int infoObjNum = offsets.size() + 1;
        offsets.add(out.size());
        StringBuilder info = new StringBuilder(infoObjNum + " 0 obj\n<< ");
        if (builder.getTitle() != null) info.append("/Title (").append(escapePdf(builder.getTitle())).append(") ");
        if (builder.getAuthor() != null) info.append("/Author (").append(escapePdf(builder.getAuthor())).append(") ");
        if (builder.getSubject() != null) info.append("/Subject (").append(escapePdf(builder.getSubject())).append(") ");
        if (builder.getCreator() != null) info.append("/Creator (").append(escapePdf(builder.getCreator())).append(") ");
        info.append("/Producer (OpenCode-Base-PDF) ");
        info.append("/CreationDate (D:").append(OffsetDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))).append(") ");
        info.append(">>\nendobj\n");
        write(out, info.toString());

        // --- Cross-reference table ---
        int xrefOffset = out.size();
        int totalObjects = offsets.size();
        write(out, "xref\n0 " + (totalObjects + 1) + "\n");
        write(out, "0000000000 65535 f \n");
        for (int offset : offsets) {
            writeXrefEntry(out, offset);
        }

        // --- Trailer ---
        write(out, "trailer\n<< /Size " + (totalObjects + 1)
                + " /Root 1 0 R /Info " + infoObjNum + " 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n");

        return out.toByteArray();
    }

    /**
     * Render and save to file.
     * 渲染并保存到文件。
     *
     * @param path target file | 目标文件
     */
    void save(Path path) {
        try {
            Files.write(path, toBytes());
        } catch (IOException e) {
            throw new cloud.opencode.base.pdf.exception.OpenPdfException("Failed to save PDF to " + path, e);
        }
    }

    /**
     * Render and write to stream.
     * 渲染并写入流。
     *
     * @param outputStream target stream | 目标流
     */
    void save(OutputStream outputStream) {
        try {
            outputStream.write(toBytes());
            outputStream.flush();
        } catch (IOException e) {
            throw new cloud.opencode.base.pdf.exception.OpenPdfException("Failed to write PDF to stream", e);
        }
    }

    // ==================== Page Rendering | 页面渲染 ====================

    /**
     * Renders a page content stream including header, footer, elements, and watermark.
     * 渲染页面内容流，包括页眉、页脚、元素和水印。
     *
     * @param page          page builder | 页面构建器
     * @param fontMap       font name map | 字体名称映射
     * @param pageImgIdx    image indices for this page | 本页图片索引
     * @param allImages     all image data | 所有图片数据
     * @param imageObjNums  image object numbers | 图片对象编号
     * @param pageIndex     current page index (0-based) | 当前页码（0起始）
     * @param totalPages    total page count | 总页数
     * @param pageWidth     page width in points | 页面宽度（点）
     * @param pageHeight    page height in points | 页面高度（点）
     * @return content stream string | 内容流字符串
     */
    private String renderPageStream(PageBuilder page, Map<String, String> fontMap,
                                     List<Integer> pageImgIdx, List<ImageData> allImages,
                                     int[] imageObjNums, int pageIndex, int totalPages,
                                     float pageWidth, float pageHeight) {
        StringBuilder sb = new StringBuilder();

        // --- Header ---
        if (builder.getHeader() != null) {
            renderHeader(sb, fontMap, pageIndex + 1, totalPages, pageWidth, pageHeight);
        }

        // --- Page elements ---
        int imgIdxCounter = 0;
        for (PdfElement element : page.getElements()) {
            switch (element) {
                case PdfText text -> renderText(sb, text, fontMap);
                case PdfLine line -> renderLine(sb, line);
                case PdfRectangle rect -> renderRectangle(sb, rect);
                case PdfTable table -> renderTable(sb, table, fontMap);
                case PdfEllipse ellipse -> renderEllipse(sb, ellipse);
                case PdfParagraph paragraph -> renderParagraph(sb, paragraph, fontMap);
                case PdfImage img -> {
                    if (imgIdxCounter < pageImgIdx.size()) {
                        int globalIdx = pageImgIdx.get(imgIdxCounter);
                        ImageData data = allImages.get(globalIdx);
                        renderImage(sb, img, "Im" + globalIdx, data);
                        imgIdxCounter++;
                    }
                }
            }
        }

        // --- Footer ---
        if (builder.getFooter() != null) {
            renderFooter(sb, fontMap, pageIndex + 1, totalPages, pageWidth, pageHeight);
        }

        // --- Watermark (rendered last, on top) ---
        if (builder.getWatermark() != null) {
            renderWatermark(sb, fontMap, pageWidth, pageHeight);
        }

        return sb.toString();
    }

    // ==================== Text Rendering | 文本渲染 ====================

    /**
     * Renders a text element with rotation, underline, character spacing, and word spacing support.
     * 渲染文本元素，支持旋转、下划线、字符间距和词间距。
     *
     * @param sb      string builder for content stream | 内容流字符串构建器
     * @param text    text element | 文本元素
     * @param fontMap font name map | 字体名称映射
     */
    private void renderText(StringBuilder sb, PdfText text, Map<String, String> fontMap) {
        String fontKey = fontKeyFor(text.getFont());
        float fontSize = text.getFontSize() > 0 ? text.getFontSize() : 12;

        sb.append("BT\n");

        // Color
        PdfColor color = text.getColor();
        if (color != null) {
            sb.append(fmt(color.getRed())).append(" ").append(fmt(color.getGreen()))
              .append(" ").append(fmt(color.getBlue())).append(" rg\n");
        }

        // Character spacing
        if (text.getCharacterSpacing() > 0) {
            sb.append(fmt(text.getCharacterSpacing())).append(" Tc\n");
        }

        // Word spacing
        if (text.getWordSpacing() > 0) {
            sb.append(fmt(text.getWordSpacing())).append(" Tw\n");
        }

        sb.append("/").append(fontKey).append(" ").append(fmt(fontSize)).append(" Tf\n");

        // Text matrix with rotation support
        float rotation = text.getRotation();
        if (rotation != 0) {
            double rad = Math.toRadians(rotation);
            float cos = (float) Math.cos(rad);
            float sin = (float) Math.sin(rad);
            sb.append(fmt(cos)).append(" ").append(fmt(sin)).append(" ")
              .append(fmt(-sin)).append(" ").append(fmt(cos)).append(" ")
              .append(fmt(text.getX())).append(" ").append(fmt(text.getY())).append(" Tm\n");
        } else {
            sb.append("1 0 0 1 ").append(fmt(text.getX())).append(" ")
              .append(fmt(text.getY())).append(" Tm\n");
        }

        sb.append("(").append(escapePdf(text.getContent())).append(") Tj\n");
        sb.append("ET\n");

        // Underline: draw a line below the text
        if (text.isUnderline()) {
            float underlineY = text.getY() + fontSize * -0.15f;
            float lineWidth = fontSize * 0.05f;
            float textWidth = estimateTextWidth(text.getContent(), fontSize);

            if (color != null) {
                sb.append(fmt(color.getRed())).append(" ").append(fmt(color.getGreen()))
                  .append(" ").append(fmt(color.getBlue())).append(" RG\n");
            }
            sb.append(fmt(lineWidth)).append(" w\n");

            if (rotation != 0) {
                // For rotated text, rotate the underline endpoints too
                double rad = Math.toRadians(rotation);
                float cos = (float) Math.cos(rad);
                float sin = (float) Math.sin(rad);
                float dx = 0;
                float dy = fontSize * -0.15f;
                float startX = text.getX() + dx * cos - dy * sin;
                float startY = text.getY() + dx * sin + dy * cos;
                float endX = startX + textWidth * cos;
                float endY = startY + textWidth * sin;
                sb.append(fmt(startX)).append(" ").append(fmt(startY)).append(" m\n");
                sb.append(fmt(endX)).append(" ").append(fmt(endY)).append(" l\nS\n");
            } else {
                sb.append(fmt(text.getX())).append(" ").append(fmt(underlineY)).append(" m\n");
                sb.append(fmt(text.getX() + textWidth)).append(" ")
                  .append(fmt(underlineY)).append(" l\nS\n");
            }
        }
    }

    // ==================== Line Rendering | 线条渲染 ====================

    /**
     * Renders a line element.
     * 渲染线条元素。
     *
     * @param sb   string builder for content stream | 内容流字符串构建器
     * @param line line element | 线条元素
     */
    private void renderLine(StringBuilder sb, PdfLine line) {
        PdfColor color = line.getColor();
        if (color != null) {
            sb.append(fmt(color.getRed())).append(" ").append(fmt(color.getGreen()))
              .append(" ").append(fmt(color.getBlue())).append(" RG\n");
        }
        float w = line.getLineWidth();
        if (w > 0) sb.append(fmt(w)).append(" w\n");

        sb.append(fmt(line.getX())).append(" ").append(fmt(line.getY())).append(" m\n");
        sb.append(fmt(line.getX2())).append(" ").append(fmt(line.getY2())).append(" l\nS\n");
    }

    // ==================== Rectangle Rendering | 矩形渲染 ====================

    /**
     * Renders a rectangle element (filled and/or stroked).
     * 渲染矩形元素（填充和/或描边）。
     *
     * @param sb   string builder for content stream | 内容流字符串构建器
     * @param rect rectangle element | 矩形元素
     */
    private void renderRectangle(StringBuilder sb, PdfRectangle rect) {
        // Fill
        if (rect.isFilled() && rect.getFillColor() != null) {
            PdfColor fc = rect.getFillColor();
            sb.append(fmt(fc.getRed())).append(" ").append(fmt(fc.getGreen()))
              .append(" ").append(fmt(fc.getBlue())).append(" rg\n");
            sb.append(fmt(rect.getX())).append(" ").append(fmt(rect.getY())).append(" ")
              .append(fmt(rect.getWidth())).append(" ").append(fmt(rect.getHeight())).append(" re\nf\n");
        }
        // Stroke
        PdfColor sc = rect.getStrokeColor();
        if (sc != null) {
            sb.append(fmt(sc.getRed())).append(" ").append(fmt(sc.getGreen()))
              .append(" ").append(fmt(sc.getBlue())).append(" RG\n");
            float sw = rect.getStrokeWidth();
            if (sw > 0) sb.append(fmt(sw)).append(" w\n");
            sb.append(fmt(rect.getX())).append(" ").append(fmt(rect.getY())).append(" ")
              .append(fmt(rect.getWidth())).append(" ").append(fmt(rect.getHeight())).append(" re\nS\n");
        }
    }

    // ==================== Ellipse Rendering | 椭圆渲染 ====================

    /**
     * Renders an ellipse using four cubic Bezier curves to approximate the shape.
     * 使用四段三次贝塞尔曲线渲染椭圆。
     *
     * <p>The Bezier approximation uses the kappa constant (4/3 * (sqrt(2) - 1) ≈ 0.5523)
     * to compute control points that closely approximate a quarter-circle arc.</p>
     * <p>贝塞尔逼近使用 kappa 常数计算控制点，紧密逼近四分之一圆弧。</p>
     *
     * @param sb      string builder for content stream | 内容流字符串构建器
     * @param ellipse ellipse element | 椭圆元素
     */
    private void renderEllipse(StringBuilder sb, PdfEllipse ellipse) {
        float cx = ellipse.getCenterX();
        float cy = ellipse.getCenterY();
        float rx = ellipse.getRadiusX();
        float ry = ellipse.getRadiusY();
        float kx = rx * KAPPA;
        float ky = ry * KAPPA;

        // Stroke color and width
        PdfColor strokeColor = ellipse.getStrokeColor();
        if (strokeColor != null) {
            sb.append(fmt(strokeColor.getRed())).append(" ").append(fmt(strokeColor.getGreen()))
              .append(" ").append(fmt(strokeColor.getBlue())).append(" RG\n");
        }
        float sw = ellipse.getStrokeWidth();
        if (sw > 0) {
            sb.append(fmt(sw)).append(" w\n");
        }

        // Fill color
        PdfColor fillColor = ellipse.getFillColor();
        if (fillColor != null) {
            sb.append(fmt(fillColor.getRed())).append(" ").append(fmt(fillColor.getGreen()))
              .append(" ").append(fmt(fillColor.getBlue())).append(" rg\n");
        }

        // Start at the rightmost point (cx + rx, cy)
        sb.append(fmt(cx + rx)).append(" ").append(fmt(cy)).append(" m\n");

        // Top-right quadrant: (cx+rx, cy) -> (cx, cy+ry)
        sb.append(fmt(cx + rx)).append(" ").append(fmt(cy + ky)).append(" ")
          .append(fmt(cx + kx)).append(" ").append(fmt(cy + ry)).append(" ")
          .append(fmt(cx)).append(" ").append(fmt(cy + ry)).append(" c\n");

        // Top-left quadrant: (cx, cy+ry) -> (cx-rx, cy)
        sb.append(fmt(cx - kx)).append(" ").append(fmt(cy + ry)).append(" ")
          .append(fmt(cx - rx)).append(" ").append(fmt(cy + ky)).append(" ")
          .append(fmt(cx - rx)).append(" ").append(fmt(cy)).append(" c\n");

        // Bottom-left quadrant: (cx-rx, cy) -> (cx, cy-ry)
        sb.append(fmt(cx - rx)).append(" ").append(fmt(cy - ky)).append(" ")
          .append(fmt(cx - kx)).append(" ").append(fmt(cy - ry)).append(" ")
          .append(fmt(cx)).append(" ").append(fmt(cy - ry)).append(" c\n");

        // Bottom-right quadrant: (cx, cy-ry) -> (cx+rx, cy)
        sb.append(fmt(cx + kx)).append(" ").append(fmt(cy - ry)).append(" ")
          .append(fmt(cx + rx)).append(" ").append(fmt(cy - ky)).append(" ")
          .append(fmt(cx + rx)).append(" ").append(fmt(cy)).append(" c\n");

        // Choose paint operator
        if (fillColor != null && strokeColor != null) {
            sb.append("B\n"); // fill + stroke
        } else if (fillColor != null) {
            sb.append("f\n"); // fill only
        } else {
            sb.append("S\n"); // stroke only
        }
    }

    // ==================== Paragraph Rendering | 段落渲染 ====================

    /**
     * Renders a paragraph with automatic word wrapping, alignment, and first-line indent.
     * 渲染带自动换行、对齐和首行缩进的段落。
     *
     * @param sb        string builder for content stream | 内容流字符串构建器
     * @param paragraph paragraph element | 段落元素
     * @param fontMap   font name map | 字体名称映射
     */
    private void renderParagraph(StringBuilder sb, PdfParagraph paragraph, Map<String, String> fontMap) {
        String content = paragraph.getContent();
        if (content == null || content.isEmpty()) return;

        String fontKey = fontKeyFor(paragraph.getFont());
        float fontSize = paragraph.getFontSize() > 0 ? paragraph.getFontSize() : 12;
        float maxWidth = paragraph.getWidth() > 0 ? paragraph.getWidth() : 500;
        float lineSpacing = paragraph.getLineSpacing() > 0 ? paragraph.getLineSpacing() : 1.2f;
        float lineHeight = fontSize * lineSpacing;
        float firstIndent = paragraph.getFirstLineIndent();
        PdfParagraph.Alignment alignment = paragraph.getAlignment();
        PdfColor color = paragraph.getColor();

        // Split content into words
        String[] words = content.split("\\s+");
        if (words.length == 0) return;

        // Build lines by accumulating words
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        float currentWidth = 0;
        boolean isFirstLine = true;

        for (String word : words) {
            float wordWidth = estimateTextWidth(word, fontSize);
            float spaceWidth = estimateTextWidth(" ", fontSize);
            float availableWidth = isFirstLine ? maxWidth - firstIndent : maxWidth;

            if (currentLine.isEmpty()) {
                currentLine.append(word);
                currentWidth = wordWidth;
            } else if (currentWidth + spaceWidth + wordWidth <= availableWidth) {
                currentLine.append(" ").append(word);
                currentWidth += spaceWidth + wordWidth;
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
                currentWidth = wordWidth;
                isFirstLine = false;
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        // Render each line
        float baseX = paragraph.getX();
        float currentY = paragraph.getY();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            float indent = (i == 0) ? firstIndent : 0;
            float lineWidth = estimateTextWidth(line, fontSize);
            float availableWidth = (i == 0) ? maxWidth - firstIndent : maxWidth;

            // Calculate x position based on alignment
            float x = baseX + indent;
            float wordSpacingAdj = 0;

            switch (alignment) {
                case CENTER -> x = baseX + indent + (availableWidth - lineWidth) / 2;
                case RIGHT -> x = baseX + indent + availableWidth - lineWidth;
                case JUSTIFY -> {
                    // Only justify if not the last line
                    if (i < lines.size() - 1) {
                        String[] lineWords = line.split("\\s+");
                        if (lineWords.length > 1) {
                            float totalWordWidth = 0;
                            for (String w : lineWords) {
                                totalWordWidth += estimateTextWidth(w, fontSize);
                            }
                            wordSpacingAdj = (availableWidth - totalWordWidth) / (lineWords.length - 1);
                        }
                    }
                }
                default -> {} // LEFT - use x as is
            }

            sb.append("BT\n");

            if (color != null) {
                sb.append(fmt(color.getRed())).append(" ").append(fmt(color.getGreen()))
                  .append(" ").append(fmt(color.getBlue())).append(" rg\n");
            }

            if (wordSpacingAdj > 0) {
                sb.append(fmt(wordSpacingAdj)).append(" Tw\n");
            }

            sb.append("/").append(fontKey).append(" ").append(fmt(fontSize)).append(" Tf\n");
            sb.append("1 0 0 1 ").append(fmt(x)).append(" ").append(fmt(currentY)).append(" Tm\n");
            sb.append("(").append(escapePdf(line)).append(") Tj\n");

            // Reset word spacing if it was set
            if (wordSpacingAdj > 0) {
                sb.append("0 Tw\n");
            }

            sb.append("ET\n");

            currentY -= lineHeight;
        }
    }

    // ==================== Image Rendering | 图像渲染 ====================

    /**
     * Renders an image reference in the content stream using the Do operator.
     * 在内容流中使用 Do 操作符渲染图像引用。
     *
     * @param sb      string builder for content stream | 内容流字符串构建器
     * @param img     image element | 图像元素
     * @param imgName image resource name (e.g., "Im0") | 图像资源名称
     * @param data    loaded image data | 已加载的图像数据
     */
    private void renderImage(StringBuilder sb, PdfImage img, String imgName, ImageData data) {
        float w = img.getWidth() > 0 ? img.getWidth() : data.pixelWidth;
        float h = img.getHeight() > 0 ? img.getHeight() : data.pixelHeight;

        // Scale to width/height maintaining aspect ratio if only one dimension set
        if (img.getWidth() > 0 && img.getHeight() <= 0) {
            h = data.pixelHeight * (img.getWidth() / data.pixelWidth);
        } else if (img.getHeight() > 0 && img.getWidth() <= 0) {
            w = data.pixelWidth * (img.getHeight() / data.pixelHeight);
        }

        sb.append("q\n"); // save graphics state

        // If opacity is set and < 1, we would need ExtGState per image
        // For simplicity, images with opacity < 1 are rendered without alpha here
        // (full alpha support requires additional ExtGState objects per unique opacity)

        // Transformation matrix: scale and position
        // [w 0 0 h x y] cm
        sb.append(fmt(w)).append(" 0 0 ").append(fmt(h)).append(" ")
          .append(fmt(img.getX())).append(" ").append(fmt(img.getY())).append(" cm\n");

        sb.append("/").append(imgName).append(" Do\n");
        sb.append("Q\n"); // restore graphics state
    }

    // ==================== Table Rendering | 表格渲染 ====================

    /**
     * Renders a table element with headers, data rows, borders, and backgrounds.
     * 渲染表格元素，包括表头、数据行、边框和背景。
     *
     * @param sb      string builder for content stream | 内容流字符串构建器
     * @param table   table element | 表格元素
     * @param fontMap font name map | 字体名称映射
     */
    private void renderTable(StringBuilder sb, PdfTable table, Map<String, String> fontMap) {
        float x = table.getX();
        float y = table.getY();
        float totalWidth = table.getWidth() > 0 ? table.getWidth() : 500;
        float cellPadding = table.getCellPadding();
        float rowHeight = 16 + cellPadding * 2;
        float borderWidth = table.getBorderWidth();

        int columns = table.getColumns();
        float[] colWidths = computeColumnWidths(table, totalWidth);

        // Default font
        String fontKey = fontKeyFor(null);

        // Border color
        PdfColor borderColor = table.getBorderColor();
        if (borderColor != null) {
            sb.append(fmt(borderColor.getRed())).append(" ").append(fmt(borderColor.getGreen()))
              .append(" ").append(fmt(borderColor.getBlue())).append(" RG\n");
        }
        if (borderWidth > 0) sb.append(fmt(borderWidth)).append(" w\n");

        float currentY = y;

        // Header rows
        List<PdfCell[]> headerRows = table.getHeaderRows();
        for (PdfCell[] headerRow : headerRows) {
            PdfColor headerBg = table.getHeaderBackground();
            if (headerBg != null) {
                sb.append(fmt(headerBg.getRed())).append(" ").append(fmt(headerBg.getGreen()))
                  .append(" ").append(fmt(headerBg.getBlue())).append(" rg\n");
                sb.append(fmt(x)).append(" ").append(fmt(currentY - rowHeight)).append(" ")
                  .append(fmt(totalWidth)).append(" ").append(fmt(rowHeight)).append(" re\nf\n");
            }
            renderTableCellRow(sb, headerRow, x, currentY, colWidths, cellPadding, rowHeight, fontKey, borderWidth > 0);
            currentY -= rowHeight;
        }

        // Data rows
        List<PdfCell[]> dataRows = table.getDataRows();
        for (int r = 0; r < dataRows.size(); r++) {
            PdfColor rowBg = table.getOddRowColor();
            if (r % 2 == 1 && table.getEvenRowColor() != null) {
                rowBg = table.getEvenRowColor();
            }
            if (rowBg != null) {
                sb.append(fmt(rowBg.getRed())).append(" ").append(fmt(rowBg.getGreen()))
                  .append(" ").append(fmt(rowBg.getBlue())).append(" rg\n");
                sb.append(fmt(x)).append(" ").append(fmt(currentY - rowHeight)).append(" ")
                  .append(fmt(totalWidth)).append(" ").append(fmt(rowHeight)).append(" re\nf\n");
            }
            renderTableCellRow(sb, dataRows.get(r), x, currentY, colWidths, cellPadding, rowHeight, fontKey, borderWidth > 0);
            currentY -= rowHeight;
        }

        // Table border
        if (borderWidth > 0) {
            float tableHeight = y - currentY;
            sb.append(fmt(x)).append(" ").append(fmt(currentY)).append(" ")
              .append(fmt(totalWidth)).append(" ").append(fmt(tableHeight)).append(" re\nS\n");
        }
    }

    private void renderTableCellRow(StringBuilder sb, PdfCell[] cells, float x, float y,
                                     float[] colWidths, float padding, float rowHeight,
                                     String fontKey, boolean drawBorder) {
        float cx = x;
        for (int i = 0; i < colWidths.length; i++) {
            // Cell border
            if (drawBorder) {
                sb.append(fmt(cx)).append(" ").append(fmt(y - rowHeight)).append(" ")
                  .append(fmt(colWidths[i])).append(" ").append(fmt(rowHeight)).append(" re\nS\n");
            }
            // Cell text
            if (i < cells.length && cells[i] != null) {
                String text = cells[i].getContent();
                if (text != null && !text.isEmpty()) {
                    sb.append("BT\n");
                    PdfColor tc = cells[i].getTextColor();
                    if (tc != null) {
                        sb.append(fmt(tc.getRed())).append(" ").append(fmt(tc.getGreen()))
                          .append(" ").append(fmt(tc.getBlue())).append(" rg\n");
                    } else {
                        sb.append("0 0 0 rg\n");
                    }
                    float fs = cells[i].getFontSize() > 0 ? cells[i].getFontSize() : 9;
                    sb.append("/").append(fontKey).append(" ").append(fmt(fs)).append(" Tf\n");
                    sb.append("1 0 0 1 ").append(fmt(cx + padding)).append(" ")
                      .append(fmt(y - rowHeight + padding + 2)).append(" Tm\n");
                    sb.append("(").append(escapePdf(text)).append(") Tj\n");
                    sb.append("ET\n");
                }
            }
            cx += colWidths[i];
        }
    }

    // ==================== Watermark Rendering | 水印渲染 ====================

    /**
     * Renders a watermark on the page with transparency and rotation.
     * 在页面上渲染带透明度和旋转的水印。
     *
     * @param sb         string builder for content stream | 内容流字符串构建器
     * @param fontMap    font name map | 字体名称映射
     * @param pageWidth  page width | 页面宽度
     * @param pageHeight page height | 页面高度
     */
    private void renderWatermark(StringBuilder sb, Map<String, String> fontMap,
                                  float pageWidth, float pageHeight) {
        PdfWatermark watermark = builder.getWatermark();
        if (watermark == null) return;

        String text = watermark.getText();
        float fontSize = watermark.getFontSize();
        float rotation = watermark.getRotation();

        PdfColor color = watermark.getColor();
        float r = 0.75f, g = 0.75f, b = 0.75f;
        if (color != null) {
            r = color.getRed();
            g = color.getGreen();
            b = color.getBlue();
        }

        String fontKey = fontKeyFor(watermark.getFont());

        if (text == null || text.isEmpty()) return;
        if (fontSize <= 0) fontSize = 60;

        // Calculate center position
        float cx = pageWidth / 2;
        float cy = pageHeight / 2;

        sb.append("q\n"); // save graphics state
        sb.append("/GS1 gs\n"); // apply transparency

        // Rotation transform around page center
        double rad = Math.toRadians(rotation);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        // Translate to center, rotate, then position text
        sb.append("BT\n");
        sb.append(fmt(r)).append(" ").append(fmt(g)).append(" ").append(fmt(b)).append(" rg\n");
        sb.append("/").append(fontKey).append(" ").append(fmt(fontSize)).append(" Tf\n");

        // Estimate text width to center it
        float textWidth = estimateTextWidth(text, fontSize);
        float tx = cx - (textWidth * cos + fontSize * sin) / 2;
        float ty = cy - (-textWidth * sin + fontSize * cos) / 2;

        sb.append(fmt(cos)).append(" ").append(fmt(sin)).append(" ")
          .append(fmt(-sin)).append(" ").append(fmt(cos)).append(" ")
          .append(fmt(tx)).append(" ").append(fmt(ty)).append(" Tm\n");

        sb.append("(").append(escapePdf(text)).append(") Tj\n");
        sb.append("ET\n");
        sb.append("Q\n"); // restore graphics state
    }

    // ==================== Header/Footer Rendering | 页眉页脚渲染 ====================

    /**
     * Renders a header at the top of the page.
     * 在页面顶部渲染页眉。
     *
     * @param sb         string builder for content stream | 内容流字符串构建器
     * @param fontMap    font name map | 字体名称映射
     * @param page       current page number (1-based) | 当前页码（1起始）
     * @param total      total page count | 总页数
     * @param pageWidth  page width | 页面宽度
     * @param pageHeight page height | 页面高度
     */
    private void renderHeader(StringBuilder sb, Map<String, String> fontMap,
                               int page, int total, float pageWidth, float pageHeight) {
        PdfHeader header = builder.getHeader();
        if (header == null) return;

        float fontSize = header.getFontSize();
        boolean showLine = header.isShowLine();

        PdfColor color = header.getColor();
        float r = 0, g = 0, b = 0;
        if (color != null) {
            r = color.getRed();
            g = color.getGreen();
            b = color.getBlue();
        }

        String fontKey = fontKeyFor(header.getFont());

        float marginTop = builder.getMarginTop();
        float y = pageHeight - marginTop / 2;
        float marginLeft = builder.getMarginLeft();
        float marginRight = builder.getMarginRight();
        float contentWidth = pageWidth - marginLeft - marginRight;

        String leftText = header.resolveLeft(page, total);
        String centerText = header.resolveCenter(page, total);
        String rightText = header.resolveRight(page, total);

        if (leftText != null && !leftText.isEmpty()) {
            sb.append("BT\n");
            sb.append(fmt(r)).append(" ").append(fmt(g)).append(" ").append(fmt(b)).append(" rg\n");
            sb.append("/").append(fontKey).append(" ").append(fmt(fontSize)).append(" Tf\n");
            sb.append("1 0 0 1 ").append(fmt(marginLeft)).append(" ").append(fmt(y)).append(" Tm\n");
            sb.append("(").append(escapePdf(leftText)).append(") Tj\n");
            sb.append("ET\n");
        }

        if (centerText != null && !centerText.isEmpty()) {
            float tw = estimateTextWidth(centerText, fontSize);
            float cx = marginLeft + (contentWidth - tw) / 2;
            sb.append("BT\n");
            sb.append(fmt(r)).append(" ").append(fmt(g)).append(" ").append(fmt(b)).append(" rg\n");
            sb.append("/").append(fontKey).append(" ").append(fmt(fontSize)).append(" Tf\n");
            sb.append("1 0 0 1 ").append(fmt(cx)).append(" ").append(fmt(y)).append(" Tm\n");
            sb.append("(").append(escapePdf(centerText)).append(") Tj\n");
            sb.append("ET\n");
        }

        if (rightText != null && !rightText.isEmpty()) {
            float tw = estimateTextWidth(rightText, fontSize);
            float rx = pageWidth - marginRight - tw;
            sb.append("BT\n");
            sb.append(fmt(r)).append(" ").append(fmt(g)).append(" ").append(fmt(b)).append(" rg\n");
            sb.append("/").append(fontKey).append(" ").append(fmt(fontSize)).append(" Tf\n");
            sb.append("1 0 0 1 ").append(fmt(rx)).append(" ").append(fmt(y)).append(" Tm\n");
            sb.append("(").append(escapePdf(rightText)).append(") Tj\n");
            sb.append("ET\n");
        }

        // Separator line
        if (showLine) {
            float lineY = y - fontSize;
            sb.append(fmt(r)).append(" ").append(fmt(g)).append(" ").append(fmt(b)).append(" RG\n");
            sb.append("0.50 w\n");
            sb.append(fmt(marginLeft)).append(" ").append(fmt(lineY)).append(" m\n");
            sb.append(fmt(pageWidth - marginRight)).append(" ").append(fmt(lineY)).append(" l\nS\n");
        }
    }

    /**
     * Renders a footer at the bottom of the page.
     * 在页面底部渲染页脚。
     *
     * @param sb         string builder for content stream | 内容流字符串构建器
     * @param fontMap    font name map | 字体名称映射
     * @param page       current page number (1-based) | 当前页码（1起始）
     * @param total      total page count | 总页数
     * @param pageWidth  page width | 页面宽度
     * @param pageHeight page height | 页面高度
     */
    private void renderFooter(StringBuilder sb, Map<String, String> fontMap,
                               int page, int total, float pageWidth, float pageHeight) {
        PdfFooter footer = builder.getFooter();
        if (footer == null) return;

        float fontSize = footer.getFontSize();
        boolean showLine = footer.isShowLine();

        PdfColor color = footer.getColor();
        float r = 0, g = 0, b = 0;
        if (color != null) {
            r = color.getRed();
            g = color.getGreen();
            b = color.getBlue();
        }

        String fontKey = fontKeyFor(footer.getFont());

        float marginBottom = builder.getMarginBottom();
        float y = marginBottom / 2;
        float marginLeft = builder.getMarginLeft();
        float marginRight = builder.getMarginRight();
        float contentWidth = pageWidth - marginLeft - marginRight;

        String leftText = footer.resolveLeft(page, total);
        String centerText = footer.resolveCenter(page, total);
        String rightText = footer.resolveRight(page, total);

        // Separator line (above footer text)
        if (showLine) {
            float lineY = y + fontSize;
            sb.append(fmt(r)).append(" ").append(fmt(g)).append(" ").append(fmt(b)).append(" RG\n");
            sb.append("0.50 w\n");
            sb.append(fmt(marginLeft)).append(" ").append(fmt(lineY)).append(" m\n");
            sb.append(fmt(pageWidth - marginRight)).append(" ").append(fmt(lineY)).append(" l\nS\n");
        }

        if (leftText != null && !leftText.isEmpty()) {
            sb.append("BT\n");
            sb.append(fmt(r)).append(" ").append(fmt(g)).append(" ").append(fmt(b)).append(" rg\n");
            sb.append("/").append(fontKey).append(" ").append(fmt(fontSize)).append(" Tf\n");
            sb.append("1 0 0 1 ").append(fmt(marginLeft)).append(" ").append(fmt(y)).append(" Tm\n");
            sb.append("(").append(escapePdf(leftText)).append(") Tj\n");
            sb.append("ET\n");
        }

        if (centerText != null && !centerText.isEmpty()) {
            float tw = estimateTextWidth(centerText, fontSize);
            float cx = marginLeft + (contentWidth - tw) / 2;
            sb.append("BT\n");
            sb.append(fmt(r)).append(" ").append(fmt(g)).append(" ").append(fmt(b)).append(" rg\n");
            sb.append("/").append(fontKey).append(" ").append(fmt(fontSize)).append(" Tf\n");
            sb.append("1 0 0 1 ").append(fmt(cx)).append(" ").append(fmt(y)).append(" Tm\n");
            sb.append("(").append(escapePdf(centerText)).append(") Tj\n");
            sb.append("ET\n");
        }

        if (rightText != null && !rightText.isEmpty()) {
            float tw = estimateTextWidth(rightText, fontSize);
            float rx = pageWidth - marginRight - tw;
            sb.append("BT\n");
            sb.append(fmt(r)).append(" ").append(fmt(g)).append(" ").append(fmt(b)).append(" rg\n");
            sb.append("/").append(fontKey).append(" ").append(fmt(fontSize)).append(" Tf\n");
            sb.append("1 0 0 1 ").append(fmt(rx)).append(" ").append(fmt(y)).append(" Tm\n");
            sb.append("(").append(escapePdf(rightText)).append(") Tj\n");
            sb.append("ET\n");
        }
    }

    // ==================== Image Loading | 图像加载 ====================

    /**
     * Loads image data from a PdfImage element, supporting JPEG and PNG formats.
     * 从 PdfImage 元素加载图像数据，支持 JPEG 和 PNG 格式。
     *
     * <p>JPEG images are embedded directly (DCTDecode filter). PNG images are
     * decoded via ImageIO, then RGB pixel data is deflate-compressed (FlateDecode).
     * If the PNG has an alpha channel, a separate SMask stream is generated.</p>
     * <p>JPEG 图像直接嵌入（DCTDecode 过滤器）。PNG 图像通过 ImageIO 解码，
     * 然后 RGB 像素数据经 deflate 压缩（FlateDecode）。若 PNG 含 alpha 通道，
     * 则生成单独的 SMask 流。</p>
     *
     * @param img image element | 图像元素
     * @return loaded image data, or null if loading fails | 已加载的图像数据，加载失败则返回 null
     */
    private ImageData loadImageData(PdfImage img) {
        try {
            byte[] rawBytes;
            if (img.getSourceBytes() != null) {
                rawBytes = img.getSourceBytes();
            } else if (img.getSourcePath() != null) {
                rawBytes = Files.readAllBytes(img.getSourcePath());
            } else {
                return null;
            }

            PdfImage.ImageFormat format = img.getFormat();

            // Detect JPEG by magic bytes if format not explicitly set
            if (format == null && rawBytes.length >= 2) {
                if ((rawBytes[0] & 0xFF) == 0xFF && (rawBytes[1] & 0xFF) == 0xD8) {
                    format = PdfImage.ImageFormat.JPEG;
                } else {
                    format = PdfImage.ImageFormat.PNG;
                }
            }

            if (format == PdfImage.ImageFormat.JPEG) {
                return loadJpegData(rawBytes);
            } else {
                return loadPngData(rawBytes);
            }
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Loads JPEG image data, extracting dimensions from SOF0 marker.
     * 加载 JPEG 图像数据，从 SOF0 标记提取尺寸。
     *
     * @param data raw JPEG bytes | 原始 JPEG 字节
     * @return image data | 图像数据
     */
    private ImageData loadJpegData(byte[] data) {
        int width = 0;
        int height = 0;

        // Parse SOF0/SOF2 marker to get dimensions
        for (int i = 0; i < data.length - 9; i++) {
            if ((data[i] & 0xFF) == 0xFF) {
                int marker = data[i + 1] & 0xFF;
                // SOF0 (0xC0), SOF1 (0xC1), SOF2 (0xC2)
                if (marker >= 0xC0 && marker <= 0xC2) {
                    height = ((data[i + 5] & 0xFF) << 8) | (data[i + 6] & 0xFF);
                    width = ((data[i + 7] & 0xFF) << 8) | (data[i + 8] & 0xFF);
                    break;
                }
            }
        }

        if (width == 0 || height == 0) {
            // Fallback: try using ImageIO
            try {
                var bis = new java.io.ByteArrayInputStream(data);
                var image = javax.imageio.ImageIO.read(bis);
                if (image != null) {
                    width = image.getWidth();
                    height = image.getHeight();
                }
            } catch (IOException e) {
                width = 100;
                height = 100;
            }
        }

        ImageData result = new ImageData();
        result.streamData = data;
        result.pixelWidth = width;
        result.pixelHeight = height;
        result.isJpeg = true;
        return result;
    }

    /**
     * Loads PNG image data by decoding to BufferedImage, extracting RGB pixels,
     * and deflate-compressing. Generates SMask for alpha channel if present.
     * 通过解码为 BufferedImage 加载 PNG 图像数据，提取 RGB 像素并 deflate 压缩。
     * 如有 alpha 通道则生成 SMask。
     *
     * @param data raw PNG bytes | 原始 PNG 字节
     * @return image data | 图像数据
     */
    private ImageData loadPngData(byte[] data) throws IOException {
        var bis = new java.io.ByteArrayInputStream(data);
        BufferedImage image = javax.imageio.ImageIO.read(bis);
        if (image == null) return null;

        int w = image.getWidth();
        int h = image.getHeight();
        boolean hasAlpha = image.getColorModel().hasAlpha();

        // Extract RGB data
        long rgbSize = (long) w * h * 3;
        if (rgbSize > 100 * 1024 * 1024L) {
            return null; // image too large to embed
        }
        byte[] rgb = new byte[(int) rgbSize];
        byte[] alpha;
        if (hasAlpha) {
            long alphaSize = (long) w * h;
            if (alphaSize > 100 * 1024 * 1024L) {
                return null; // too large
            }
            alpha = new byte[(int) alphaSize];
        } else {
            alpha = null;
        }

        // Bulk pixel access: 5-10x faster than per-pixel getRGB(x,y)
        int[] pixels = image.getRGB(0, 0, w, h, null, 0, w);
        for (int p = 0; p < pixels.length; p++) {
            int pixel = pixels[p];
            int idx = p * 3;
            rgb[idx] = (byte) ((pixel >> 16) & 0xFF);
            rgb[idx + 1] = (byte) ((pixel >> 8) & 0xFF);
            rgb[idx + 2] = (byte) (pixel & 0xFF);
            if (hasAlpha) {
                alpha[p] = (byte) ((pixel >> 24) & 0xFF);
            }
        }

        ImageData result = new ImageData();
        result.streamData = deflate(rgb);
        result.pixelWidth = w;
        result.pixelHeight = h;
        result.isJpeg = false;

        if (hasAlpha) {
            result.smaskData = deflate(alpha);
        }

        return result;
    }

    // ==================== Utilities | 工具方法 ====================

    /**
     * Deflate-compresses byte data.
     * 对字节数据进行 Deflate 压缩。
     *
     * @param data raw bytes | 原始字节
     * @return compressed bytes | 压缩后的字节
     */
    private static byte[] deflate(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
        try (DeflaterOutputStream dos = new DeflaterOutputStream(baos)) {
            dos.write(data);
        }
        return baos.toByteArray();
    }

    /**
     * Estimates text width using a rough character-count approximation for Type1 fonts.
     * 使用粗略的字符计数近似估算 Type1 字体的文本宽度。
     *
     * @param text     text content | 文本内容
     * @param fontSize font size | 字体大小
     * @return estimated width in points | 估算宽度（点）
     */
    private static float estimateTextWidth(String text, float fontSize) {
        if (text == null || text.isEmpty()) return 0;
        return text.length() * fontSize * CHAR_WIDTH_FACTOR;
    }

    private Map<String, String> collectFonts(List<PageBuilder> pages) {
        Map<String, String> fonts = new LinkedHashMap<>();
        fonts.put("F1", "Helvetica"); // always include default

        for (PageBuilder page : pages) {
            for (PdfElement element : page.getElements()) {
                if (element instanceof PdfText text && text.getFont() != null) {
                    String key = fontKeyFor(text.getFont());
                    fonts.putIfAbsent(key, text.getFont().getPdfName());
                }
                if (element instanceof PdfParagraph para && para.getFont() != null) {
                    String key = fontKeyFor(para.getFont());
                    fonts.putIfAbsent(key, para.getFont().getPdfName());
                }
            }
        }

        // Collect fonts from watermark, header, footer
        PdfWatermark wm = builder.getWatermark();
        if (wm != null && wm.getFont() != null) {
            String key = fontKeyFor(wm.getFont());
            fonts.putIfAbsent(key, wm.getFont().getPdfName());
        }
        PdfHeader hdr = builder.getHeader();
        if (hdr != null && hdr.getFont() != null) {
            String key = fontKeyFor(hdr.getFont());
            fonts.putIfAbsent(key, hdr.getFont().getPdfName());
        }
        PdfFooter ftr = builder.getFooter();
        if (ftr != null && ftr.getFont() != null) {
            String key = fontKeyFor(ftr.getFont());
            fonts.putIfAbsent(key, ftr.getFont().getPdfName());
        }

        // Add bold variant for tables
        fonts.putIfAbsent("F2", "Helvetica-Bold");
        return fonts;
    }

    private String fontKeyFor(PdfFont font) {
        if (font == null) return "F1";
        if (font == StandardFont.HELVETICA) return "F1";
        if (font == StandardFont.HELVETICA_BOLD) return "F2";
        if (font == StandardFont.HELVETICA_OBLIQUE) return "F3";
        if (font == StandardFont.TIMES_ROMAN) return "F4";
        if (font == StandardFont.TIMES_BOLD) return "F5";
        if (font == StandardFont.COURIER) return "F6";
        if (font == StandardFont.COURIER_BOLD) return "F7";
        // Dynamic fonts: use monotonic counter to avoid hash collisions
        String pdfName = font.getPdfName();
        return dynamicFontKeyCache.computeIfAbsent(pdfName,
                k -> "F" + nextDynamicFontKey++);
    }

    private float[] computeColumnWidths(PdfTable table, float totalWidth) {
        float[] configured = table.getColumnWidths();
        int columns = table.getColumns();
        if (columns <= 0) {
            return new float[0];
        }
        float[] widths = new float[columns];
        if (configured != null && configured.length == columns) {
            // Check if values are ratios (all <= 1) or absolute
            boolean areRatios = true;
            for (float v : configured) { if (v > 1f) { areRatios = false; break; } }
            if (areRatios) {
                for (int i = 0; i < columns; i++) widths[i] = totalWidth * configured[i];
            } else {
                System.arraycopy(configured, 0, widths, 0, columns);
            }
        } else {
            float each = totalWidth / columns;
            Arrays.fill(widths, each);
        }
        return widths;
    }

    private static String escapePdf(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '(' -> sb.append("\\(");
                case ')' -> sb.append("\\)");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 32 || c > 126) {
                        int val = c & 0xFF;
                        sb.append('\\');
                        sb.append((char) ('0' + (val / 64)));
                        sb.append((char) ('0' + ((val / 8) % 8)));
                        sb.append((char) ('0' + (val % 8)));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static String fmt(float v) {
        int i = (int) v;
        if (v == i) return Integer.toString(i);
        // Manual 2-decimal formatting: avoids String.format/Formatter allocation
        long scaled = Math.round(v * 100.0);
        long integer = scaled / 100;
        long frac = Math.abs(scaled % 100);
        // Preserve sign for values in (-1.0, 0.0) where integer division yields 0
        String prefix = (scaled < 0 && integer == 0) ? "-" : "";
        return prefix + integer + "." + (frac < 10 ? "0" : "") + frac;
    }

    private static final byte[] XREF_SUFFIX = " 00000 n \n".getBytes(StandardCharsets.US_ASCII);

    private static void writeXrefEntry(ByteArrayOutputStream out, int offset) {
        byte[] buf = new byte[10];
        int val = offset;
        for (int i = 9; i >= 0; i--) {
            buf[i] = (byte) ('0' + (val % 10));
            val /= 10;
        }
        out.write(buf, 0, 10);
        out.write(XREF_SUFFIX, 0, XREF_SUFFIX.length);
    }

    private static void write(ByteArrayOutputStream out, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.US_ASCII);
        out.write(bytes, 0, bytes.length);
    }

    private static void writeBytes(ByteArrayOutputStream out, byte[] bytes) {
        out.write(bytes, 0, bytes.length);
    }

    // ==================== Inner Classes | 内部类 ====================

    /**
     * Holds loaded image data for embedding into the PDF.
     * 保存已加载的图像数据用于嵌入 PDF。
     */
    private static final class ImageData {
        byte[] streamData;
        byte[] smaskData;
        int smaskObjNum;
        int pixelWidth;
        int pixelHeight;
        boolean isJpeg;
    }
}
