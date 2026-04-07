package cloud.opencode.base.pdf.internal.parser;

import cloud.opencode.base.pdf.exception.OpenPdfException;

import java.util.*;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * PDF Structure Parser - Parses complete PDF file structure
 * PDF 结构解析器 - 解析完整的 PDF 文件结构
 *
 * <p>Parses the high-level structure of a PDF file: header validation, xref table,
 * trailer dictionary, indirect object resolution, page tree traversal, and stream
 * decompression. Supports PDF versions 1.4 through 1.7.</p>
 * <p>解析 PDF 文件的高层结构：头部验证、xref 表、trailer 字典、间接对象解析、
 * 页面树遍历和流解压缩。支持 PDF 1.4 至 1.7 版本。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>PDF 1.4-1.7 header validation - PDF 1.4-1.7 头部验证</li>
 *   <li>Cross-reference table parsing with incremental update support - 交叉引用表解析，支持增量更新</li>
 *   <li>Lazy indirect object resolution - 延迟间接对象解析</li>
 *   <li>Page tree flattening - 页面树展平</li>
 *   <li>Stream decompression: FlateDecode, ASCII85Decode, ASCIIHexDecode - 流解压缩</li>
 *   <li>Cascaded filter support - 级联过滤器支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Recursion depth limit: 100 - 递归深度限制: 100</li>
 *   <li>Stream decompression size limit: 100 MB - 流解压大小限制: 100 MB</li>
 *   <li>Xref entry count limit - xref 条目数限制</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
public final class PdfParser {

    /** Maximum recursion depth for object resolution | 对象解析最大递归深度 */
    private static final int MAX_RECURSION = 100;

    /** Maximum decompressed stream size (100 MB) | 最大解压流大小 (100 MB) */
    private static final int MAX_STREAM_SIZE = 100 * 1024 * 1024;

    private PdfParser() {
        // Utility class
    }

    /**
     * Parses PDF data into a structured representation
     * 将 PDF 数据解析为结构化表示
     *
     * @param data raw PDF byte data | 原始 PDF 字节数据
     * @return parsed PDF structure | 解析后的 PDF 结构
     * @throws OpenPdfException if parsing fails | 解析失败时抛出异常
     */
    public static ParsedPdf parse(byte[] data) {
        Objects.requireNonNull(data, "data cannot be null");
        if (data.length < 20) {
            throw OpenPdfException.invalidFormat("File too small to be a valid PDF");
        }

        // 1. Validate header
        validateHeader(data);

        // 2. Find startxref
        PdfTokenizer tokenizer = new PdfTokenizer(data, true);
        int startxrefPos = tokenizer.searchBackward("startxref");
        if (startxrefPos < 0) {
            throw OpenPdfException.invalidFormat("Cannot find 'startxref' marker");
        }

        // 3. Read xref offset
        tokenizer.seekTo(startxrefPos);
        tokenizer.readLine(); // consume "startxref"
        String xrefOffsetStr = tokenizer.readLine().trim();
        int xrefOffset;
        try {
            xrefOffset = Integer.parseInt(xrefOffsetStr);
        } catch (NumberFormatException e) {
            throw OpenPdfException.invalidFormat("Invalid startxref offset: " + xrefOffsetStr);
        }
        if (xrefOffset < 0 || xrefOffset >= data.length) {
            throw OpenPdfException.invalidFormat(
                    "startxref offset out of range: " + xrefOffset + " (file length: " + data.length + ")");
        }

        // 4. Parse xref table
        XrefTable xref = new XrefTable();
        xref.parse(tokenizer, xrefOffset);

        return new ParsedPdf(data, xref, xref.getTrailer());
    }

    // ==================== Header Validation | 头部验证 ====================

    private static void validateHeader(byte[] data) {
        // Look for %PDF- in first 1024 bytes
        String header = new String(data, 0, Math.min(1024, data.length), java.nio.charset.StandardCharsets.US_ASCII);
        int pdfIdx = header.indexOf("%PDF-");
        if (pdfIdx < 0) {
            throw OpenPdfException.invalidFormat("Missing PDF header (%PDF-)");
        }
        // Extract version
        int versionStart = pdfIdx + 5;
        if (versionStart + 3 > data.length) {
            throw OpenPdfException.invalidFormat("Incomplete PDF header");
        }
        String version = header.substring(versionStart, Math.min(versionStart + 3, header.length()));
        // Accept 1.0 through 2.0
        if (!version.matches("\\d\\.\\d")) {
            throw OpenPdfException.invalidFormat("Invalid PDF version: " + version);
        }
    }

    // ==================== ParsedPdf Record | ParsedPdf 记录 ====================

    /**
     * Parsed PDF - Holds parsed PDF structure with lazy object resolution
     * 解析后的 PDF - 持有解析后的 PDF 结构，支持延迟对象解析
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-pdf V1.0.3
     */
    public static final class ParsedPdf {

        private final byte[] data;
        private final XrefTable xref;
        private final PdfObject.PdfDictionary trailer;
        private final Map<Integer, PdfObject> objectCache = new HashMap<>();

        ParsedPdf(byte[] data, XrefTable xref, PdfObject.PdfDictionary trailer) {
            this.data = data;
            this.xref = xref;
            this.trailer = trailer;
        }

        /**
         * Gets the trailer dictionary
         * 获取 trailer 字典
         *
         * @return trailer dictionary | trailer 字典
         */
        public PdfObject.PdfDictionary getTrailer() {
            return trailer;
        }

        /**
         * Resolves an indirect object by object number
         * 按对象号解析间接对象
         *
         * @param objNum object number | 对象号
         * @return resolved object | 解析后的对象
         * @throws OpenPdfException if object cannot be resolved | 无法解析对象时抛出异常
         */
        public PdfObject resolveObject(int objNum) {
            return resolveObject(objNum, 0);
        }

        private PdfObject resolveObject(int objNum, int depth) {
            if (depth > MAX_RECURSION) {
                throw OpenPdfException.invalidFormat("Object resolution exceeded maximum recursion depth");
            }

            PdfObject cached = objectCache.get(objNum);
            if (cached != null) return cached;

            long offset = xref.getOffset(objNum);
            if (offset < 0 || offset >= data.length) {
                return new PdfObject.PdfNull();
            }
            if (offset > Integer.MAX_VALUE) {
                throw new OpenPdfException("PDF_PARSE", "Object offset out of range: " + offset);
            }

            PdfTokenizer tokenizer = new PdfTokenizer(data, true);
            tokenizer.seekTo((int) offset);

            // Read: N G obj VALUE endobj
            PdfObject objNumToken = tokenizer.readToken();
            PdfObject genToken = tokenizer.readToken();
            String objKeyword = tokenizer.readKeywordString();

            if (!"obj".equals(objKeyword)) {
                throw OpenPdfException.invalidFormat(
                        "Expected 'obj' keyword at offset " + offset + ", got: " + objKeyword);
            }

            PdfObject value = tokenizer.readToken();

            // Check if this is a stream object
            if (value instanceof PdfObject.PdfDictionary dict) {
                tokenizer.skipWhitespaceAndComments();
                int pos = tokenizer.getPosition();
                String nextWord = tokenizer.readKeywordString();
                if ("stream".equals(nextWord)) {
                    // Skip the EOL after "stream"
                    if (tokenizer.getPosition() < data.length && data[tokenizer.getPosition()] == '\r') {
                        tokenizer.seekTo(tokenizer.getPosition() + 1);
                    }
                    if (tokenizer.getPosition() < data.length && data[tokenizer.getPosition()] == '\n') {
                        tokenizer.seekTo(tokenizer.getPosition() + 1);
                    }

                    int streamStart = tokenizer.getPosition();
                    int length = dict.getInt("Length", 0);

                    // Resolve Length if it's an indirect reference
                    PdfObject lengthObj = dict.get("Length");
                    if (lengthObj instanceof PdfObject.PdfReference ref) {
                        PdfObject resolved = resolveObject(ref.objectNumber(), depth + 1);
                        if (resolved instanceof PdfObject.PdfNumber num) {
                            length = num.intValue();
                        }
                    }

                    if (length <= 0 || (long) streamStart + length > data.length) {
                        // Try to find endstream marker
                        length = findEndstream(data, streamStart);
                    }

                    if (length <= 0 || (long) streamStart + length > data.length) {
                        // Cannot extract stream, return dictionary only
                        objectCache.put(objNum, dict);
                        return dict;
                    }

                    byte[] rawStreamData = new byte[length];
                    System.arraycopy(data, streamStart, rawStreamData, 0, length);

                    // Decompress
                    byte[] decodedData = decodeStream(dict, rawStreamData);
                    value = new PdfObject.PdfStream(dict, decodedData);
                } else {
                    tokenizer.seekTo(pos); // restore position
                }
            }

            objectCache.put(objNum, value);
            return value;
        }

        /**
         * Resolves a PdfObject: if it's a PdfReference, resolves the referenced object
         * 解析 PdfObject：如果是 PdfReference，解析引用的对象
         *
         * @param obj object to resolve | 要解析的对象
         * @return resolved object | 解析后的对象
         */
        public PdfObject resolve(PdfObject obj) {
            return resolve(obj, 0);
        }

        private PdfObject resolve(PdfObject obj, int depth) {
            if (depth > MAX_RECURSION) {
                throw OpenPdfException.invalidFormat("Reference resolution exceeded maximum recursion depth");
            }
            if (obj instanceof PdfObject.PdfReference ref) {
                PdfObject resolved = resolveObject(ref.objectNumber(), depth + 1);
                // Resolved object might itself be a reference (unlikely but handle)
                if (resolved instanceof PdfObject.PdfReference) {
                    return resolve(resolved, depth + 1);
                }
                return resolved;
            }
            return obj;
        }

        /**
         * Gets all pages by traversing the page tree
         * 通过遍历页面树获取所有页面
         *
         * @return list of page dictionaries | 页面字典列表
         */
        public List<PdfObject.PdfDictionary> getPages() {
            // Get Root catalog
            PdfObject rootRef = trailer.get("Root");
            PdfObject root = resolve(rootRef);
            if (!(root instanceof PdfObject.PdfDictionary catalog)) {
                throw OpenPdfException.invalidFormat("Cannot resolve document catalog");
            }

            // Get Pages tree root
            PdfObject pagesRef = catalog.get("Pages");
            PdfObject pagesObj = resolve(pagesRef);
            if (!(pagesObj instanceof PdfObject.PdfDictionary pagesDict)) {
                throw OpenPdfException.invalidFormat("Cannot resolve pages tree root");
            }

            // Flatten page tree using iterative approach with stack
            // Track visited object numbers to prevent circular references
            List<PdfObject.PdfDictionary> pages = new ArrayList<>();
            Deque<PdfObject> stack = new ArrayDeque<>();
            Set<Integer> visited = new HashSet<>();
            stack.push(pagesDict);

            int maxPages = 100_000; // Safety limit
            while (!stack.isEmpty() && pages.size() < maxPages) {
                PdfObject current = stack.pop();
                // Skip circular references
                if (current instanceof PdfObject.PdfReference ref) {
                    if (!visited.add(ref.objectNumber())) continue;
                }
                PdfObject resolved = resolve(current);
                if (!(resolved instanceof PdfObject.PdfDictionary dict)) continue;

                String type = dict.getString("Type");
                if ("Page".equals(type)) {
                    pages.add(dict);
                } else if ("Pages".equals(type)) {
                    PdfObject.PdfArray kids = dict.getArray("Kids");
                    if (kids != null) {
                        // Push in reverse order so first child is processed first
                        for (int i = kids.size() - 1; i >= 0; i--) {
                            stack.push(kids.get(i));
                        }
                    }
                }
            }

            return List.copyOf(pages);
        }

        /**
         * Gets document info dictionary
         * 获取文档信息字典
         *
         * @return info dictionary, or null if not present | 信息字典，不存在返回 null
         */
        public PdfObject.PdfDictionary getInfo() {
            PdfObject infoRef = trailer.get("Info");
            if (infoRef == null) return null;
            PdfObject info = resolve(infoRef);
            if (info instanceof PdfObject.PdfDictionary dict) return dict;
            return null;
        }

        /**
         * Checks if the document has an Encrypt dictionary (is encrypted)
         * 检查文档是否有 Encrypt 字典（是否加密）
         *
         * @return true if encrypted | 加密返回 true
         */
        public boolean isEncrypted() {
            return trailer.containsKey("Encrypt");
        }

        // ==================== Stream Decoding | 流解码 ====================

        private static byte[] decodeStream(PdfObject.PdfDictionary dict, byte[] rawData) {
            PdfObject filterObj = dict.get("Filter");
            if (filterObj == null) {
                return rawData;
            }

            List<String> filters = new ArrayList<>();
            if (filterObj instanceof PdfObject.PdfName name) {
                filters.add(name.value());
            } else if (filterObj instanceof PdfObject.PdfArray arr) {
                for (PdfObject element : arr.elements()) {
                    if (element instanceof PdfObject.PdfName name) {
                        filters.add(name.value());
                    }
                }
            }

            byte[] current = rawData;
            for (String filter : filters) {
                current = applyFilter(filter, current);
            }
            return current;
        }

        private static byte[] applyFilter(String filter, byte[] data) {
            return switch (filter) {
                case "FlateDecode", "Fl" -> decodeFlateDecode(data);
                case "ASCIIHexDecode", "AHx" -> decodeASCIIHex(data);
                case "ASCII85Decode", "A85" -> decodeASCII85(data);
                default -> throw OpenPdfException.unsupportedFeature("PDF filter: " + filter);
            };
        }

        private static byte[] decodeFlateDecode(byte[] data) {
            Inflater inflater = new Inflater();
            try {
                inflater.setInput(data);
                int initialCapacity = (int) Math.min((long) data.length * 2, MAX_STREAM_SIZE);
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(initialCapacity);
                byte[] buffer = new byte[8192];
                long totalSize = 0;
                while (!inflater.finished()) {
                    int count;
                    try {
                        count = inflater.inflate(buffer);
                    } catch (DataFormatException e) {
                        throw OpenPdfException.invalidFormat("FlateDecode decompression failed: " + e.getMessage());
                    }
                    if (count == 0 && inflater.needsInput()) break;
                    totalSize += count;
                    if (totalSize > MAX_STREAM_SIZE) {
                        throw OpenPdfException.invalidFormat(
                                "Decompressed stream exceeds size limit (" + MAX_STREAM_SIZE + " bytes)");
                    }
                    out.write(buffer, 0, count);
                }
                return out.toByteArray();
            } finally {
                inflater.end();
            }
        }

        private static byte[] decodeASCIIHex(byte[] data) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(data.length / 2);
            int hi = -1;
            for (byte b : data) {
                int c = b & 0xFF;
                if (c == '>') break; // EOD marker
                if (isHexChar(c)) {
                    int digit = hexVal(c);
                    if (hi < 0) {
                        hi = digit;
                    } else {
                        out.write((hi << 4) | digit);
                        hi = -1;
                    }
                    if (out.size() > MAX_STREAM_SIZE) {
                        throw OpenPdfException.invalidFormat(
                                "ASCIIHex decoded stream exceeds maximum size");
                    }
                }
                // Skip whitespace
            }
            if (hi >= 0) {
                out.write(hi << 4); // Trailing nibble padded with 0
            }
            return out.toByteArray();
        }

        private static byte[] decodeASCII85(byte[] data) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            int i = 0;
            // Skip leading <~ if present
            if (i + 1 < data.length && data[i] == '<' && data[i + 1] == '~') {
                i += 2;
            }

            long[] tuple = new long[5];
            int tupleCount = 0;
            while (i < data.length) {
                int c = data[i++] & 0xFF;
                if (c == '~' || c == '>') break; // EOD
                if (c <= ' ') continue; // Skip whitespace

                if (c == 'z') {
                    // Special: z = 4 zero bytes
                    out.write(0); out.write(0); out.write(0); out.write(0);
                    if (out.size() > MAX_STREAM_SIZE) {
                        throw OpenPdfException.invalidFormat(
                                "ASCII85 decoded stream exceeds maximum size");
                    }
                    continue;
                }

                if (c < '!' || c > 'u') continue;
                tuple[tupleCount++] = c - '!';

                if (tupleCount == 5) {
                    long value = 0;
                    for (int j = 0; j < 5; j++) {
                        value = value * 85 + tuple[j];
                    }
                    out.write((int) ((value >> 24) & 0xFF));
                    out.write((int) ((value >> 16) & 0xFF));
                    out.write((int) ((value >> 8) & 0xFF));
                    out.write((int) (value & 0xFF));
                    tupleCount = 0;
                    if (out.size() > MAX_STREAM_SIZE) {
                        throw OpenPdfException.invalidFormat(
                                "ASCII85 decoded stream exceeds maximum size");
                    }
                }
            }

            // Handle remaining bytes
            if (tupleCount > 1) {
                for (int j = tupleCount; j < 5; j++) {
                    tuple[j] = 84; // Pad with 'u' (84)
                }
                long value = 0;
                for (int j = 0; j < 5; j++) {
                    value = value * 85 + tuple[j];
                }
                for (int j = 0; j < tupleCount - 1; j++) {
                    out.write((int) ((value >> (24 - j * 8)) & 0xFF));
                }
            }

            return out.toByteArray();
        }

        private static boolean isHexChar(int c) {
            return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
        }

        private static int hexVal(int c) {
            if (c >= '0' && c <= '9') return c - '0';
            if (c >= 'a' && c <= 'f') return c - 'a' + 10;
            if (c >= 'A' && c <= 'F') return c - 'A' + 10;
            return 0;
        }

        /**
         * Finds the "endstream" marker position from a given start
         * 从给定起始位置查找 "endstream" 标记
         */
        private static int findEndstream(byte[] data, int start) {
            byte[] marker = "endstream".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
            int maxSearch = Math.min(data.length, start + MAX_STREAM_SIZE);
            for (int i = start; i <= maxSearch - marker.length; i++) {
                boolean match = true;
                for (int j = 0; j < marker.length; j++) {
                    if (data[i + j] != marker[j]) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    int end = i;
                    // Trim trailing whitespace
                    while (end > start && (data[end - 1] == '\n' || data[end - 1] == '\r' || data[end - 1] == ' ')) {
                        end--;
                    }
                    return end - start;
                }
            }
            // Fallback: return 0 length
            return 0;
        }
    }
}
