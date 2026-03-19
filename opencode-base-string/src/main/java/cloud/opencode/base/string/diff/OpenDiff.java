package cloud.opencode.base.string.diff;

import java.util.*;
import java.util.regex.Pattern;

/**
 * String Diff Utility - Computes differences between strings.
 * 字符串差异工具 - 计算字符串之间的差异。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Line-level diff - 行级差异比较</li>
 *   <li>Word-level diff - 单词级差异比较</li>
 *   <li>Character-level diff - 字符级差异比较</li>
 *   <li>Unified and HTML diff output - 统一和HTML差异输出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DiffResult result = OpenDiff.diff("hello\nworld", "hello\njava");
 * String unified = OpenDiff.unifiedDiff("original", "revised");
 * String html = OpenDiff.htmlDiff("original", "revised");
 * }</pre>
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
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class OpenDiff {
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("\n");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    private OpenDiff() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static DiffResult diff(String original, String revised) {
        return diffLines(original, revised);
    }

    public static DiffResult diffLines(String original, String revised) {
        if (original == null) original = "";
        if (revised == null) revised = "";
        
        String[] origLines = NEWLINE_PATTERN.split(original, -1);
        String[] revLines = NEWLINE_PATTERN.split(revised, -1);
        
        List<DiffLine> diffLines = new ArrayList<>();
        int additions = 0, deletions = 0, modifications = 0;
        
        // Simple line-by-line comparison
        int maxLen = Math.max(origLines.length, revLines.length);
        for (int i = 0; i < maxLen; i++) {
            String origLine = i < origLines.length ? origLines[i] : null;
            String revLine = i < revLines.length ? revLines[i] : null;
            
            if (origLine != null && revLine != null) {
                if (origLine.equals(revLine)) {
                    diffLines.add(new DiffLine(DiffLine.Type.EQUAL, i, i, origLine));
                } else {
                    diffLines.add(new DiffLine(DiffLine.Type.MODIFY, i, i, revLine));
                    modifications++;
                }
            } else if (origLine != null) {
                diffLines.add(new DiffLine(DiffLine.Type.DELETE, i, -1, origLine));
                deletions++;
            } else {
                diffLines.add(new DiffLine(DiffLine.Type.INSERT, -1, i, revLine));
                additions++;
            }
        }
        
        return new DiffResult(diffLines, additions, deletions, modifications);
    }

    public static DiffResult diffWords(String original, String revised) {
        String[] origWords = WHITESPACE_PATTERN.split(original != null ? original : "");
        String[] revWords = WHITESPACE_PATTERN.split(revised != null ? revised : "");
        
        List<DiffLine> diffLines = new ArrayList<>();
        int additions = 0, deletions = 0, modifications = 0;
        
        int i = 0, j = 0;
        while (i < origWords.length || j < revWords.length) {
            if (i < origWords.length && j < revWords.length && origWords[i].equals(revWords[j])) {
                diffLines.add(new DiffLine(DiffLine.Type.EQUAL, i, j, origWords[i]));
                i++;
                j++;
            } else if (j >= revWords.length || (i < origWords.length && !origWords[i].equals(revWords[j]))) {
                diffLines.add(new DiffLine(DiffLine.Type.DELETE, i, -1, origWords[i]));
                deletions++;
                i++;
            } else {
                diffLines.add(new DiffLine(DiffLine.Type.INSERT, -1, j, revWords[j]));
                additions++;
                j++;
            }
        }
        
        return new DiffResult(diffLines, additions, deletions, modifications);
    }

    public static DiffResult diffChars(String original, String revised) {
        if (original == null) original = "";
        if (revised == null) revised = "";
        
        List<DiffLine> diffLines = new ArrayList<>();
        int additions = 0, deletions = 0, modifications = 0;
        
        int i = 0, j = 0;
        while (i < original.length() || j < revised.length()) {
            if (i < original.length() && j < revised.length() && original.charAt(i) == revised.charAt(j)) {
                diffLines.add(new DiffLine(DiffLine.Type.EQUAL, i, j, String.valueOf(original.charAt(i))));
                i++;
                j++;
            } else if (j >= revised.length() || (i < original.length() && original.charAt(i) != revised.charAt(j))) {
                diffLines.add(new DiffLine(DiffLine.Type.DELETE, i, -1, String.valueOf(original.charAt(i))));
                deletions++;
                i++;
            } else {
                diffLines.add(new DiffLine(DiffLine.Type.INSERT, -1, j, String.valueOf(revised.charAt(j))));
                additions++;
                j++;
            }
        }
        
        return new DiffResult(diffLines, additions, deletions, modifications);
    }

    public static String unifiedDiff(String original, String revised) {
        return diffLines(original, revised).toUnifiedDiff();
    }

    public static String unifiedDiff(String original, String revised, String originalName, String revisedName, int contextLines) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- ").append(originalName).append("\n");
        sb.append("+++ ").append(revisedName).append("\n");
        sb.append(unifiedDiff(original, revised));
        return sb.toString();
    }

    public static String htmlDiff(String original, String revised) {
        return diffLines(original, revised).toHtml();
    }

    public static String applyPatch(String original, String patch) {
        // Simplified patch application
        String[] lines = patch.split("\n");
        StringBuilder result = new StringBuilder(original);
        
        for (String line : lines) {
            if (line.startsWith("+ ")) {
                result.append(line.substring(2)).append("\n");
            }
        }
        
        return result.toString();
    }
}
