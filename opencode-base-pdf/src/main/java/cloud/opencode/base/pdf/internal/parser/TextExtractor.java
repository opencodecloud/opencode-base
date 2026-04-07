package cloud.opencode.base.pdf.internal.parser;

import java.util.*;

/**
 * PDF Text Extractor - Extracts text content from parsed PDF pages
 * PDF 文本提取器 - 从解析后的 PDF 页面提取文本内容
 *
 * <p>Processes content stream operators to reconstruct text content from PDF pages.
 * Tracks text state (font, position, line spacing) and inserts appropriate whitespace
 * and line breaks based on text positioning operators.</p>
 * <p>处理内容流操作符以从 PDF 页面重建文本内容。跟踪文本状态（字体、位置、行间距），
 * 并根据文本定位操作符插入适当的空白和换行符。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Text extraction from Tj, TJ, ', " operators - 从 Tj、TJ、'、" 操作符提取文本</li>
 *   <li>Line break detection via Td/TD/Tm y-coordinate changes - 通过 Td/TD/Tm 的 y 坐标变化检测换行</li>
 *   <li>Space insertion for TJ array negative spacing - TJ 数组负间距的空格插入</li>
 *   <li>ToUnicode CMap parsing for character mapping - ToUnicode CMap 解析用于字符映射</li>
 *   <li>Standard 14 font encoding fallback - 标准 14 字体编码回退</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes — stateless static methods - 线程安全: 是 — 无状态静态方法</li>
 *   <li>CMap parsing bounded by stream data size - CMap 解析受流数据大小限制</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
public final class TextExtractor {

    /** Threshold for TJ spacing that triggers a space insertion | TJ 间距触发空格插入的阈值 */
    private static final double TJ_SPACE_THRESHOLD = -100;

    private TextExtractor() {
        // Utility class
    }

    /**
     * Extracts text from a single page
     * 从单个页面提取文本
     *
     * @param pdf       parsed PDF | 解析后的 PDF
     * @param pageIndex 0-based page index | 基于0的页面索引
     * @return extracted text | 提取的文本
     */
    public static String extractText(PdfParser.ParsedPdf pdf, int pageIndex) {
        Objects.requireNonNull(pdf, "pdf cannot be null");
        List<PdfObject.PdfDictionary> pages = pdf.getPages();
        if (pageIndex < 0 || pageIndex >= pages.size()) {
            return "";
        }

        PdfObject.PdfDictionary pageDict = pages.get(pageIndex);
        byte[] contentData = getPageContentData(pdf, pageDict);
        if (contentData == null || contentData.length == 0) {
            return "";
        }

        // Build font-to-CMap mapping for this page
        Map<String, Map<Integer, String>> fontCMaps = buildFontCMaps(pdf, pageDict);

        List<ContentStreamParser.Operator> operators = ContentStreamParser.parse(contentData);
        return processOperators(operators, fontCMaps);
    }

    /**
     * Extracts text from all pages
     * 从所有页面提取文本
     *
     * @param pdf parsed PDF | 解析后的 PDF
     * @return extracted text from all pages | 所有页面提取的文本
     */
    public static String extractAllText(PdfParser.ParsedPdf pdf) {
        Objects.requireNonNull(pdf, "pdf cannot be null");
        // Fetch pages once to avoid N+1 page tree traversals
        List<PdfObject.PdfDictionary> pages = pdf.getPages();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pages.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(extractTextFromPage(pdf, pages.get(i)));
        }
        return sb.toString();
    }

    /**
     * Extracts text from a pre-fetched page dictionary (avoids repeated getPages() calls)
     * 从预取的页面字典中提取文本（避免重复 getPages() 调用）
     */
    private static String extractTextFromPage(PdfParser.ParsedPdf pdf,
                                               PdfObject.PdfDictionary pageDict) {
        byte[] contentData = getPageContentData(pdf, pageDict);
        if (contentData == null || contentData.length == 0) return "";
        Map<String, Map<Integer, String>> fontCMaps = buildFontCMaps(pdf, pageDict);
        List<ContentStreamParser.Operator> operators = ContentStreamParser.parse(contentData);
        return processOperators(operators, fontCMaps);
    }

    // ==================== Content Data Extraction | 内容数据提取 ====================

    private static byte[] getPageContentData(PdfParser.ParsedPdf pdf, PdfObject.PdfDictionary pageDict) {
        PdfObject contentsObj = pageDict.get("Contents");
        if (contentsObj == null) return null;

        PdfObject resolved = pdf.resolve(contentsObj);

        if (resolved instanceof PdfObject.PdfStream stream) {
            return stream.data();
        }

        if (resolved instanceof PdfObject.PdfArray array) {
            // Multiple content streams - concatenate with size limit
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            long totalSize = 0;
            for (PdfObject element : array.elements()) {
                PdfObject streamObj = pdf.resolve(element);
                if (streamObj instanceof PdfObject.PdfStream stream) {
                    byte[] data = stream.data();
                    totalSize += data.length;
                    if (totalSize > 100_000_000L) {
                        break; // combined content exceeds 100 MB limit
                    }
                    out.write(data, 0, data.length);
                    out.write(' '); // Separator
                }
            }
            return out.toByteArray();
        }

        return null;
    }

    // ==================== Font CMap Building | 字体 CMap 构建 ====================

    private static Map<String, Map<Integer, String>> buildFontCMaps(
            PdfParser.ParsedPdf pdf, PdfObject.PdfDictionary pageDict) {
        Map<String, Map<Integer, String>> cmaps = new HashMap<>();

        // Get Resources dictionary
        PdfObject resourcesObj = pageDict.get("Resources");
        if (resourcesObj == null) return cmaps;
        PdfObject resolved = pdf.resolve(resourcesObj);
        if (!(resolved instanceof PdfObject.PdfDictionary resources)) return cmaps;

        // Get Font dictionary from Resources
        PdfObject fontsObj = resources.get("Font");
        if (fontsObj == null) return cmaps;
        PdfObject fontsResolved = pdf.resolve(fontsObj);
        if (!(fontsResolved instanceof PdfObject.PdfDictionary fonts)) return cmaps;

        for (var entry : fonts.entries().entrySet()) {
            String fontName = entry.getKey();
            PdfObject fontObj = pdf.resolve(entry.getValue());
            if (fontObj instanceof PdfObject.PdfDictionary fontDict) {
                Map<Integer, String> cmap = extractToUnicodeCMap(pdf, fontDict);
                if (cmap != null && !cmap.isEmpty()) {
                    cmaps.put(fontName, cmap);
                }
            }
        }

        return cmaps;
    }

    private static Map<Integer, String> extractToUnicodeCMap(
            PdfParser.ParsedPdf pdf, PdfObject.PdfDictionary fontDict) {
        PdfObject toUnicodeObj = fontDict.get("ToUnicode");
        if (toUnicodeObj == null) return null;

        PdfObject resolved = pdf.resolve(toUnicodeObj);
        if (!(resolved instanceof PdfObject.PdfStream cmapStream)) return null;

        return parseCMap(new String(cmapStream.data(), java.nio.charset.StandardCharsets.US_ASCII));
    }

    /**
     * Parses a ToUnicode CMap to extract character code to Unicode mappings
     * 解析 ToUnicode CMap 以提取字符编码到 Unicode 的映射
     */
    static Map<Integer, String> parseCMap(String cmapData) {
        Map<Integer, String> map = new HashMap<>();

        // Parse beginbfchar sections
        int idx = 0;
        while ((idx = cmapData.indexOf("beginbfchar", idx)) >= 0) {
            idx += "beginbfchar".length();
            int endIdx = cmapData.indexOf("endbfchar", idx);
            if (endIdx < 0) break;
            String section = cmapData.substring(idx, endIdx);
            parseBfCharSection(section, map);
            idx = endIdx;
        }

        // Parse beginbfrange sections
        idx = 0;
        while ((idx = cmapData.indexOf("beginbfrange", idx)) >= 0) {
            idx += "beginbfrange".length();
            int endIdx = cmapData.indexOf("endbfrange", idx);
            if (endIdx < 0) break;
            String section = cmapData.substring(idx, endIdx);
            parseBfRangeSection(section, map);
            idx = endIdx;
        }

        return map;
    }

    private static void parseBfCharSection(String section, Map<Integer, String> map) {
        List<String> hexStrings = extractHexStrings(section);
        for (int i = 0; i + 1 < hexStrings.size(); i += 2) {
            int code = parseHexInt(hexStrings.get(i));
            String unicode = hexToUnicode(hexStrings.get(i + 1));
            map.put(code, unicode);
        }
    }

    /** Maximum entries per bfrange to prevent DoS | 每个bfrange最大条目数（防DoS） */
    private static final int MAX_RANGE_SIZE = 65536;
    /** Maximum total CMap entries | CMap最大总条目数 */
    private static final int MAX_CMAP_ENTRIES = 100_000;

    private static void parseBfRangeSection(String section, Map<Integer, String> map) {
        List<String> hexStrings = extractHexStrings(section);
        for (int i = 0; i + 2 < hexStrings.size(); i += 3) {
            int startCode = parseHexInt(hexStrings.get(i));
            int endCode = parseHexInt(hexStrings.get(i + 1));
            // Guard against malicious ranges (e.g., <0000> <7FFFFFFF>)
            if (endCode < startCode || endCode - startCode > MAX_RANGE_SIZE) {
                continue;
            }
            String startUnicode = hexStrings.get(i + 2);
            int unicodeStart = parseHexInt(startUnicode);
            for (int code = startCode; code <= endCode; code++) {
                int unicodeVal = unicodeStart + (code - startCode);
                map.put(code, new String(Character.toChars(unicodeVal)));
                if (map.size() > MAX_CMAP_ENTRIES) {
                    return;
                }
            }
        }
    }

    private static List<String> extractHexStrings(String section) {
        List<String> result = new ArrayList<>();
        int pos = 0;
        while (pos < section.length()) {
            int start = section.indexOf('<', pos);
            if (start < 0) break;
            int end = section.indexOf('>', start);
            if (end < 0) break;
            result.add(section.substring(start + 1, end));
            pos = end + 1;
        }
        return result;
    }

    private static int parseHexInt(String hex) {
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String hexToUnicode(String hex) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i + 3 < hex.length(); i += 4) {
            try {
                int codePoint = Integer.parseInt(hex.substring(i, i + 4), 16);
                sb.appendCodePoint(codePoint);
            } catch (NumberFormatException e) {
                sb.append('\uFFFD'); // replacement character
            }
        }
        if (hex.length() == 2) {
            try {
                sb.appendCodePoint(Integer.parseInt(hex, 16));
            } catch (NumberFormatException e) {
                sb.append('\uFFFD');
            }
        }
        return sb.toString();
    }

    // ==================== Operator Processing | 操作符处理 ====================

    private static String processOperators(List<ContentStreamParser.Operator> operators,
                                           Map<String, Map<Integer, String>> fontCMaps) {
        StringBuilder result = new StringBuilder();
        String currentFont = null;
        double currentY = 0;
        double lastY = Double.NaN;
        double lastX = 0;
        double leading = 0;
        boolean inText = false;

        for (ContentStreamParser.Operator op : operators) {
            switch (op.name()) {
                case "BT" -> {
                    inText = true;
                    lastX = 0;
                    // Don't reset lastY - we want line breaks between BT/ET blocks
                }
                case "ET" -> inText = false;

                case "Tf" -> {
                    // Set font: /FontName size Tf
                    if (!op.operands().isEmpty() && op.operands().getFirst() instanceof PdfObject.PdfName name) {
                        currentFont = name.value();
                    }
                }

                case "Td" -> {
                    // Move text position: tx ty Td
                    if (op.operands().size() >= 2) {
                        double tx = getNumber(op.operands().get(0));
                        double ty = getNumber(op.operands().get(1));
                        lastX += tx;
                        currentY += ty;
                        if (!Double.isNaN(lastY) && Math.abs(currentY - lastY) > 0.5) {
                            if (!result.isEmpty() && result.charAt(result.length() - 1) != '\n') {
                                result.append('\n');
                            }
                        } else if (tx > 5 && !result.isEmpty() &&
                                   result.charAt(result.length() - 1) != ' ' &&
                                   result.charAt(result.length() - 1) != '\n') {
                            result.append(' ');
                        }
                        lastY = currentY;
                    }
                }

                case "TD" -> {
                    // Move text position and set leading: tx ty TD
                    if (op.operands().size() >= 2) {
                        double tx = getNumber(op.operands().get(0));
                        double ty = getNumber(op.operands().get(1));
                        leading = -ty;
                        lastX += tx;
                        currentY += ty;
                        if (!Double.isNaN(lastY) && Math.abs(currentY - lastY) > 0.5) {
                            if (!result.isEmpty() && result.charAt(result.length() - 1) != '\n') {
                                result.append('\n');
                            }
                        }
                        lastY = currentY;
                    }
                }

                case "Tm" -> {
                    // Set text matrix: a b c d e f Tm
                    if (op.operands().size() >= 6) {
                        double newX = getNumber(op.operands().get(4));
                        double newY = getNumber(op.operands().get(5));
                        if (!Double.isNaN(lastY) && Math.abs(newY - lastY) > 0.5) {
                            if (!result.isEmpty() && result.charAt(result.length() - 1) != '\n') {
                                result.append('\n');
                            }
                        } else if (!Double.isNaN(lastY) && Math.abs(newY - lastY) <= 0.5
                                   && newX - lastX > 5) {
                            if (!result.isEmpty() && result.charAt(result.length() - 1) != ' ') {
                                result.append(' ');
                            }
                        }
                        lastX = newX;
                        currentY = newY;
                        lastY = newY;
                    }
                }

                case "T*" -> {
                    // Move to start of next line
                    currentY -= leading;
                    if (!result.isEmpty() && result.charAt(result.length() - 1) != '\n') {
                        result.append('\n');
                    }
                    lastY = currentY;
                }

                case "TL" -> {
                    // Set text leading
                    if (!op.operands().isEmpty()) {
                        leading = getNumber(op.operands().getFirst());
                    }
                }

                case "Tj" -> {
                    // Show text: (string) Tj
                    if (!op.operands().isEmpty()) {
                        PdfObject textObj = op.operands().getFirst();
                        String text = decodeTextObject(textObj, currentFont, fontCMaps);
                        result.append(text);
                    }
                }

                case "TJ" -> {
                    // Show text with positioning: [(string) number (string) ...] TJ
                    if (!op.operands().isEmpty() && op.operands().getFirst() instanceof PdfObject.PdfArray arr) {
                        for (PdfObject element : arr.elements()) {
                            if (element instanceof PdfObject.PdfString || element instanceof PdfObject.PdfName) {
                                result.append(decodeTextObject(element, currentFont, fontCMaps));
                            } else if (element instanceof PdfObject.PdfNumber num) {
                                // Negative value = move right (gap between chars)
                                if (num.value() < TJ_SPACE_THRESHOLD) {
                                    result.append(' ');
                                }
                            }
                        }
                    }
                }

                case "'" -> {
                    // Move to next line and show text: (string) '
                    currentY -= leading;
                    if (!result.isEmpty() && result.charAt(result.length() - 1) != '\n') {
                        result.append('\n');
                    }
                    lastY = currentY;
                    if (!op.operands().isEmpty()) {
                        result.append(decodeTextObject(op.operands().getFirst(), currentFont, fontCMaps));
                    }
                }

                case "\"" -> {
                    // Set word/char spacing, move to next line, show text: aw ac (string) "
                    currentY -= leading;
                    if (!result.isEmpty() && result.charAt(result.length() - 1) != '\n') {
                        result.append('\n');
                    }
                    lastY = currentY;
                    if (op.operands().size() >= 3) {
                        result.append(decodeTextObject(op.operands().get(2), currentFont, fontCMaps));
                    }
                }

                default -> {
                    // Other operators are ignored for text extraction
                }
            }
        }

        return result.toString().stripTrailing();
    }

    private static String decodeTextObject(PdfObject obj, String fontName,
                                           Map<String, Map<Integer, String>> fontCMaps) {
        if (obj instanceof PdfObject.PdfString s) {
            // Try CMap lookup first
            if (fontName != null) {
                Map<Integer, String> cmap = fontCMaps.get(fontName);
                if (cmap != null && !cmap.isEmpty()) {
                    return decodeByCMap(s.value(), cmap);
                }
            }
            // Fallback: use string value directly
            return s.value();
        }
        return "";
    }

    private static String decodeByCMap(String raw, Map<Integer, String> cmap) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            int code = raw.charAt(i) & 0xFFFF;
            // Try 2-byte lookup first
            if (i + 1 < raw.length()) {
                int twoByteCode = ((raw.charAt(i) & 0xFF) << 8) | (raw.charAt(i + 1) & 0xFF);
                String mapped = cmap.get(twoByteCode);
                if (mapped != null) {
                    sb.append(mapped);
                    i++; // consume second byte
                    continue;
                }
            }
            // Single byte lookup
            String mapped = cmap.get(code);
            if (mapped != null) {
                sb.append(mapped);
            } else {
                sb.append((char) code);
            }
        }
        return sb.toString();
    }

    private static double getNumber(PdfObject obj) {
        if (obj instanceof PdfObject.PdfNumber n) return n.value();
        return 0;
    }
}
