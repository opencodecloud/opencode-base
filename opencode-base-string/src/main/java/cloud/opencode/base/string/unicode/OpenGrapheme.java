package cloud.opencode.base.string.unicode;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// NOTE: Internal methods are split into two categories:
//   - graphemeClusters()       : allocates List<String>, only used by substring() and reverse()
//   - countGraphemes()         : count-only, zero allocation beyond BreakIterator
//   - computeDisplayWidth()    : width-only, zero String allocation beyond BreakIterator
// Hot-path methods (length, displayWidth, truncateToWidth) use the zero-allocation variants.
//
// BreakIterator is NOT thread-safe (it has mutable internal state), but reusing one per thread
// via ThreadLocal avoids the ICU initialization cost (~100-200 ns) on every call.
// BreakIterator 非线程安全（内部状态可变），通过 ThreadLocal 每线程复用一个实例，
// 避免每次调用时的 ICU 初始化开销（~100-200 ns）。

/**
 * Grapheme Cluster Utility - Correctly handles visual characters including emoji and combining marks.
 * 字素簇工具类 - 正确处理包括emoji和组合标记在内的可视字符。
 *
 * <p>Standard {@code String.length()} counts UTF-16 code units, which gives misleading results
 * for emoji, combining characters, and other multi-code-unit graphemes. This utility uses
 * {@link BreakIterator} to correctly identify grapheme cluster boundaries.</p>
 * <p>标准的 {@code String.length()} 计算UTF-16代码单元，对emoji、组合字符等多代码单元字素
 * 会给出误导性结果。本工具使用 {@link BreakIterator} 正确识别字素簇边界。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Grapheme-aware length calculation - 字素感知的长度计算</li>
 *   <li>Grapheme-safe substring extraction - 字素安全的子串提取</li>
 *   <li>Grapheme-safe string reversal - 字素安全的字符串反转</li>
 *   <li>Display width calculation for East Asian characters - 东亚字符显示宽度计算</li>
 *   <li>Width-aware string truncation - 宽度感知的字符串截断</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * OpenGrapheme.length("a\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66b"); // 3
 * OpenGrapheme.reverse("a\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66b"); // "b\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66a"
 * OpenGrapheme.displayWidth("Hello\u4F60\u597D"); // 9
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for all operations - 时间复杂度: 所有操作均为O(n)</li>
 *   <li>Space complexity: O(n) - 空间复杂度: O(n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.3
 */
public final class OpenGrapheme {

    /**
     * Thread-local BreakIterator to amortize ICU initialization cost across calls.
     * Each call to {@code bi.setText(str)} resets internal state, so the instance is safe to reuse.
     * 线程本地 BreakIterator，摊销 ICU 初始化开销。每次 setText 重置内部状态，可安全复用。
     */
    private static final ThreadLocal<BreakIterator> TL_BREAK_ITERATOR =
            ThreadLocal.withInitial(() -> BreakIterator.getCharacterInstance(Locale.ROOT));

    private OpenGrapheme() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Count grapheme clusters (visual characters).
     * 计算字素簇（可视字符）数量。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * length(null)                        = 0
     * length("")                          = 0
     * length("hello")                     = 5
     * length("a\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66b") = 3
     * </pre>
     *
     * @param str the string to measure | 要测量的字符串
     * @return the number of grapheme clusters | 字素簇数量
     */
    public static int length(String str) {
        if (str == null || str.isEmpty()) {
            return 0;
        }
        return countGraphemes(str);
    }

    /**
     * Grapheme-safe substring by grapheme index.
     * 基于字素索引的安全子串。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * substring("hello", 1, 3)  = "el"
     * substring(null, 0, 1)     = null
     * </pre>
     *
     * @param str        the source string | 源字符串
     * @param beginIndex the beginning grapheme index (inclusive) | 起始字素索引（包含）
     * @param endIndex   the ending grapheme index (exclusive) | 结束字素索引（不包含）
     * @return the substring based on grapheme indices | 基于字素索引的子串
     * @throws IndexOutOfBoundsException if indices are out of range | 如果索引越界
     */
    public static String substring(String str, int beginIndex, int endIndex) {
        if (str == null) {
            return null;
        }
        List<String> clusters = graphemeClusters(str);
        int size = clusters.size();
        if (beginIndex < 0 || endIndex > size || beginIndex > endIndex) {
            throw new IndexOutOfBoundsException(
                    "beginIndex: " + beginIndex + ", endIndex: " + endIndex + ", grapheme length: " + size);
        }
        var sb = new StringBuilder();
        for (int i = beginIndex; i < endIndex; i++) {
            sb.append(clusters.get(i));
        }
        return sb.toString();
    }

    /**
     * Grapheme-safe reverse that preserves emoji and combining characters.
     * 保留emoji和组合字符的安全反转。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * reverse("abc")   = "cba"
     * reverse(null)     = null
     * reverse("")       = ""
     * reverse("a\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66b") = "b\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66a"
     * </pre>
     *
     * @param str the string to reverse | 要反转的字符串
     * @return the reversed string | 反转后的字符串
     */
    public static String reverse(String str) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return "";
        }
        List<String> clusters = graphemeClusters(str);
        var sb = new StringBuilder();
        for (int i = clusters.size() - 1; i >= 0; i--) {
            sb.append(clusters.get(i));
        }
        return sb.toString();
    }

    /**
     * Calculate display width accounting for East Asian double-width characters.
     * 计算考虑东亚双宽度字符的显示宽度。
     *
     * <p>CJK characters count as width 2, others as width 1.</p>
     * <p>CJK字符宽度为2，其他字符宽度为1。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * displayWidth("hello")     = 5
     * displayWidth("\u4F60\u597D")       = 4
     * displayWidth("Hi\u4F60\u597D")     = 6
     * displayWidth(null)        = 0
     * </pre>
     *
     * @param str the string to measure | 要测量的字符串
     * @return the display width in columns | 以列为单位的显示宽度
     */
    public static int displayWidth(String str) {
        if (str == null || str.isEmpty()) {
            return 0;
        }
        return computeDisplayWidth(str);
    }

    /**
     * Truncate string to fit within maxWidth display columns.
     * 截断字符串以适应最大显示列宽。
     *
     * <p>Uses "..." as the default ellipsis.</p>
     * <p>使用"..."作为默认省略号。</p>
     *
     * @param str      the string to truncate | 要截断的字符串
     * @param maxWidth the maximum display width | 最大显示宽度
     * @return the truncated string | 截断后的字符串
     */
    public static String truncateToWidth(String str, int maxWidth) {
        return truncateToWidth(str, maxWidth, "...");
    }

    /**
     * Truncate string to fit within maxWidth display columns with custom ellipsis.
     * 截断字符串以适应最大显示列宽，使用自定义省略号。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * truncateToWidth("hello", 10, "...")         = "hello"
     * truncateToWidth("hello world", 8, "...")     = "hello..."
     * truncateToWidth("\u4F60\u597D\u4E16\u754C", 5, "...")   = "\u4F60..."
     * truncateToWidth(null, 10, "...")             = ""
     * </pre>
     *
     * @param str      the string to truncate | 要截断的字符串
     * @param maxWidth the maximum display width | 最大显示宽度
     * @param ellipsis the ellipsis string to append | 要追加的省略号字符串
     * @return the truncated string | 截断后的字符串
     * @throws IllegalArgumentException if maxWidth is negative | 如果maxWidth为负数
     */
    public static String truncateToWidth(String str, int maxWidth, String ellipsis) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        if (maxWidth < 0) {
            throw new IllegalArgumentException("maxWidth must not be negative: " + maxWidth);
        }
        if (ellipsis == null) {
            ellipsis = "";
        }

        // Single pass: check total width and find cutoff in one BreakIterator scan.
        // 单次遍历：在一次 BreakIterator 扫描中同时检查总宽度并定位截断点。
        int totalWidth = computeDisplayWidth(str);
        if (totalWidth <= maxWidth) {
            return str;
        }

        int ellipsisWidth = computeDisplayWidth(ellipsis);
        int availableWidth = maxWidth - ellipsisWidth;
        if (availableWidth <= 0) {
            return truncateByWidth(ellipsis, maxWidth);
        }

        // Iterate grapheme boundaries directly — no List<String> allocation.
        // 直接迭代字素边界，无需分配 List<String>。
        BreakIterator bi = TL_BREAK_ITERATOR.get();
        bi.setText(str);
        var sb = new StringBuilder();
        int currentWidth = 0;
        int start = bi.first();
        int end = bi.next();
        while (end != BreakIterator.DONE) {
            int w = isDoubleWidth(str.codePointAt(start)) ? 2 : 1;
            if (currentWidth + w > availableWidth) {
                break;
            }
            sb.append(str, start, end);
            currentWidth += w;
            start = end;
            end = bi.next();
        }
        sb.append(ellipsis);
        return sb.toString();
    }

    // ==================== Internal helpers ====================

    /**
     * Extract grapheme clusters from a string using BreakIterator.
     * Used only by {@link #substring} and {@link #reverse} which need the actual cluster strings.
     * 使用BreakIterator从字符串中提取字素簇。
     * 仅由需要实际簇字符串的 {@link #substring} 和 {@link #reverse} 使用。
     */
    private static List<String> graphemeClusters(String str) {
        List<String> clusters = new ArrayList<>();
        BreakIterator bi = TL_BREAK_ITERATOR.get();
        bi.setText(str);
        int start = bi.first();
        int end = bi.next();
        while (end != BreakIterator.DONE) {
            clusters.add(str.substring(start, end));
            start = end;
            end = bi.next();
        }
        return clusters;
    }

    /**
     * Count grapheme clusters without allocating any String objects.
     * 计算字素簇数量，不分配任何 String 对象（零分配）。
     */
    private static int countGraphemes(String str) {
        BreakIterator bi = TL_BREAK_ITERATOR.get();
        bi.setText(str);
        int count = 0;
        while (bi.next() != BreakIterator.DONE) {
            count++;
        }
        return count;
    }

    /**
     * Compute display width without allocating intermediate String clusters.
     * 计算显示宽度，不分配中间字素簇字符串（零分配）。
     */
    private static int computeDisplayWidth(String str) {
        BreakIterator bi = TL_BREAK_ITERATOR.get();
        bi.setText(str);
        int width = 0;
        int start = bi.first();
        int end = bi.next();
        while (end != BreakIterator.DONE) {
            width += isDoubleWidth(str.codePointAt(start)) ? 2 : 1;
            start = end;
            end = bi.next();
        }
        return width;
    }

    /**
     * Calculate the display width of a single grapheme cluster.
     * 计算单个字素簇的显示宽度。
     */
    private static int graphemeDisplayWidth(String cluster) {
        // Use the first code point to determine width
        int codePoint = cluster.codePointAt(0);
        return isDoubleWidth(codePoint) ? 2 : 1;
    }

    /**
     * Check if a code point is a double-width (East Asian) character.
     * 检查代码点是否为双宽度（东亚）字符。
     */
    private static boolean isDoubleWidth(int codePoint) {
        // CJK Unified Ideographs
        if (codePoint >= 0x4E00 && codePoint <= 0x9FFF) return true;
        // CJK Unified Ideographs Extension A
        if (codePoint >= 0x3400 && codePoint <= 0x4DBF) return true;
        // CJK Unified Ideographs Extension B
        if (codePoint >= 0x20000 && codePoint <= 0x2A6DF) return true;
        // CJK Compatibility Ideographs
        if (codePoint >= 0xF900 && codePoint <= 0xFAFF) return true;
        // Fullwidth Forms
        if (codePoint >= 0xFF01 && codePoint <= 0xFF60) return true;
        if (codePoint >= 0xFFE0 && codePoint <= 0xFFE6) return true;
        // Hangul Syllables
        if (codePoint >= 0xAC00 && codePoint <= 0xD7AF) return true;
        // CJK Radicals Supplement
        if (codePoint >= 0x2E80 && codePoint <= 0x2EFF) return true;
        // Kangxi Radicals
        if (codePoint >= 0x2F00 && codePoint <= 0x2FDF) return true;
        // CJK Symbols and Punctuation
        if (codePoint >= 0x3000 && codePoint <= 0x303F) return true;
        // Hiragana
        if (codePoint >= 0x3040 && codePoint <= 0x309F) return true;
        // Katakana
        if (codePoint >= 0x30A0 && codePoint <= 0x30FF) return true;
        // Bopomofo
        if (codePoint >= 0x3100 && codePoint <= 0x312F) return true;
        // Enclosed CJK Letters and Months
        if (codePoint >= 0x3200 && codePoint <= 0x32FF) return true;
        // CJK Compatibility
        if (codePoint >= 0x3300 && codePoint <= 0x33FF) return true;
        return false;
    }

    /**
     * Truncate a string to fit within a given display width (no ellipsis).
     * Uses direct BreakIterator iteration to avoid List<String> allocation.
     * 截断字符串以适应给定的显示宽度（不添加省略号）。直接迭代，零分配。
     */
    private static String truncateByWidth(String str, int maxWidth) {
        BreakIterator bi = TL_BREAK_ITERATOR.get();
        bi.setText(str);
        var sb = new StringBuilder();
        int currentWidth = 0;
        int start = bi.first();
        int end = bi.next();
        while (end != BreakIterator.DONE) {
            int w = isDoubleWidth(str.codePointAt(start)) ? 2 : 1;
            if (currentWidth + w > maxWidth) {
                break;
            }
            sb.append(str, start, end);
            currentWidth += w;
            start = end;
            end = bi.next();
        }
        return sb.toString();
    }
}
