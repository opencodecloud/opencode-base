package cloud.opencode.base.pdf.internal.parser;

import cloud.opencode.base.pdf.exception.OpenPdfException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * PDF Cross-Reference Table - Manages object byte offsets for random access
 * PDF 交叉引用表 - 管理对象字节偏移量以支持随机访问
 *
 * <p>Parses and stores the PDF cross-reference table (xref) which maps object
 * numbers to their byte offsets in the file. Supports incremental updates
 * via the /Prev pointer chain in trailer dictionaries.</p>
 * <p>解析并存储 PDF 交叉引用表（xref），将对象号映射到文件中的字节偏移量。
 * 通过 trailer 字典中的 /Prev 指针链支持增量更新。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Traditional xref table parsing - 传统 xref 表解析</li>
 *   <li>Incremental update support via /Prev chain - 通过 /Prev 链支持增量更新</li>
 *   <li>Trailer dictionary extraction - Trailer 字典提取</li>
 *   <li>Object offset lookup - 对象偏移量查找</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No — mutable state during parsing - 线程安全: 否 — 解析期间有可变状态</li>
 *   <li>Limits xref entries to prevent memory exhaustion - 限制 xref 条目数以防内存耗尽</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
public final class XrefTable {

    /** Maximum number of xref entries to prevent memory exhaustion | 最大 xref 条目数 */
    private static final int MAX_XREF_ENTRIES = 10_000_000;

    /** Maximum number of incremental updates to follow | 最大增量更新跟随次数 */
    private static final int MAX_PREV_CHAIN = 100;

    private final Map<Integer, Long> offsets = new HashMap<>();
    private PdfObject.PdfDictionary trailer;

    /**
     * Parses xref table starting at given offset
     * 从给定偏移量开始解析 xref 表
     *
     * @param tokenizer PDF tokenizer | PDF 分词器
     * @param xrefOffset byte offset of xref keyword | xref 关键字的字节偏移量
     * @throws OpenPdfException if xref table is malformed | xref 表格式错误时抛出异常
     */
    public void parse(PdfTokenizer tokenizer, int xrefOffset) {
        Objects.requireNonNull(tokenizer, "tokenizer cannot be null");

        int chainDepth = 0;
        int currentOffset = xrefOffset;

        while (currentOffset >= 0 && chainDepth < MAX_PREV_CHAIN) {
            chainDepth++;
            tokenizer.seekTo(currentOffset);

            // Read "xref" keyword
            String keyword = tokenizer.readKeywordString();
            if (!"xref".equals(keyword)) {
                throw OpenPdfException.invalidFormat("Expected 'xref' at offset " + currentOffset);
            }

            // Parse xref sections
            parseXrefSections(tokenizer);

            // Parse trailer
            tokenizer.skipWhitespaceAndComments();
            String trailerKeyword = tokenizer.readKeywordString();
            if (!"trailer".equals(trailerKeyword)) {
                throw OpenPdfException.invalidFormat("Expected 'trailer' after xref section");
            }

            PdfObject trailerObj = tokenizer.readToken();
            if (!(trailerObj instanceof PdfObject.PdfDictionary trailerDict)) {
                throw OpenPdfException.invalidFormat("Expected dictionary after 'trailer'");
            }

            // Keep the first (most recent) trailer as the primary one
            if (this.trailer == null) {
                this.trailer = trailerDict;
            }

            // Follow /Prev chain for incremental updates
            PdfObject prevObj = trailerDict.get("Prev");
            if (prevObj instanceof PdfObject.PdfNumber prevNum) {
                currentOffset = prevNum.intValue();
            } else {
                currentOffset = -1;
            }
        }

        if (this.trailer == null) {
            throw OpenPdfException.invalidFormat("No trailer found in PDF");
        }
    }

    /**
     * Gets the byte offset for a given object number
     * 获取给定对象号的字节偏移量
     *
     * @param objectNumber object number | 对象号
     * @return byte offset, or -1 if not found | 字节偏移量，未找到返回 -1
     */
    public long getOffset(int objectNumber) {
        Long offset = offsets.get(objectNumber);
        return offset != null ? offset : -1;
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
     * Gets the total number of tracked objects
     * 获取跟踪的对象总数
     *
     * @return number of entries | 条目数
     */
    public int size() {
        return offsets.size();
    }

    /**
     * Checks if an object number exists in the xref table
     * 检查对象号是否存在于 xref 表中
     *
     * @param objectNumber object number | 对象号
     * @return true if exists | 存在返回 true
     */
    public boolean contains(int objectNumber) {
        return offsets.containsKey(objectNumber);
    }

    // ==================== Private Methods | 私有方法 ====================

    private void parseXrefSections(PdfTokenizer tokenizer) {
        while (true) {
            tokenizer.skipWhitespaceAndComments();
            if (!tokenizer.hasMore()) break;

            int savedPos = tokenizer.getPosition();

            // Try to read first number of subsection header
            String firstWord = tokenizer.readKeywordString();
            if ("trailer".equals(firstWord)) {
                // We've hit the trailer, restore position
                tokenizer.seekTo(savedPos);
                break;
            }

            int startObj;
            try {
                startObj = Integer.parseInt(firstWord);
            } catch (NumberFormatException e) {
                // Not a subsection header, restore and break
                tokenizer.seekTo(savedPos);
                break;
            }

            // Read count
            tokenizer.skipWhitespaceAndComments();
            String countStr = tokenizer.readKeywordString();
            int count;
            try {
                count = Integer.parseInt(countStr);
            } catch (NumberFormatException e) {
                throw OpenPdfException.invalidFormat("Invalid xref subsection count: " + countStr);
            }

            if (startObj < 0 || count < 0 || (long) startObj + count > MAX_XREF_ENTRIES) {
                throw OpenPdfException.invalidFormat(
                        "Invalid xref section: startObj=" + startObj + ", count=" + count);
            }
            if (offsets.size() + count > MAX_XREF_ENTRIES) {
                throw OpenPdfException.invalidFormat("Total xref entries exceed limit");
            }

            // Parse entries
            for (int i = 0; i < count; i++) {
                tokenizer.skipWhitespaceAndComments();
                String offsetStr = tokenizer.readKeywordString();
                tokenizer.skipWhitespaceAndComments();
                String genStr = tokenizer.readKeywordString();
                tokenizer.skipWhitespaceAndComments();
                String status = tokenizer.readKeywordString();

                if (offsetStr == null || genStr == null || status == null) {
                    throw OpenPdfException.invalidFormat("Incomplete xref entry");
                }

                int objNum = startObj + i;
                if ("n".equals(status)) {
                    try {
                        long offset = Long.parseLong(offsetStr);
                        // Only add if not already present (newer entries take precedence)
                        offsets.putIfAbsent(objNum, offset);
                    } catch (NumberFormatException e) {
                        throw OpenPdfException.invalidFormat("Invalid xref offset: " + offsetStr);
                    }
                }
                // Free entries ('f') are ignored
            }
        }
    }
}
