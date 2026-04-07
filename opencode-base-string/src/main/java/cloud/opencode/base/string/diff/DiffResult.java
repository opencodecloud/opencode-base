package cloud.opencode.base.string.diff;

import java.util.List;

/**
 * Diff Result - Record holding the result of a string diff operation.
 * 差异结果 - 持有字符串差异操作结果的记录。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified diff output - 统一差异输出</li>
 *   <li>HTML diff output - HTML差异输出</li>
 *   <li>Addition/deletion/modification counts - 增/删/改计数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DiffResult result = OpenDiff.diff("hello", "world");
 * boolean changed = result.hasDiff();
 * String unified = result.toUnifiedDiff();
 * String html = result.toHtml();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (record is immutable) - 线程安全: 是（记录不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public record DiffResult(List<DiffLine> lines, int additions, int deletions, int modifications) {
    
    public boolean hasDiff() {
        return additions > 0 || deletions > 0 || modifications > 0;
    }

    public String toUnifiedDiff() {
        StringBuilder sb = new StringBuilder();
        for (DiffLine line : lines) {
            switch (line.type()) {
                case INSERT -> sb.append("+ ").append(line.content()).append("\n");
                case DELETE -> sb.append("- ").append(line.content()).append("\n");
                case MODIFY -> sb.append("! ").append(line.content()).append("\n");
                case EQUAL -> sb.append("  ").append(line.content()).append("\n");
            }
        }
        return sb.toString();
    }

    public String toHtml() {
        StringBuilder sb = new StringBuilder("<div class=\"diff\">\n");
        for (DiffLine line : lines) {
            String cssClass = switch (line.type()) {
                case INSERT -> "insert";
                case DELETE -> "delete";
                case MODIFY -> "modify";
                case EQUAL -> "equal";
            };
            sb.append("  <div class=\"").append(cssClass).append("\">")
              .append(escapeHtml(line.content()))
              .append("</div>\n");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private static String escapeHtml(String str) {
        // Delegate to the optimized single-pass HtmlUtil.escape().
        // 委托给已优化的单遍历 HtmlUtil.escape()。
        return cloud.opencode.base.string.escape.HtmlUtil.escape(str);
    }
}
