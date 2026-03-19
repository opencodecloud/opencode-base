package cloud.opencode.base.string.diff;

/**
 * Diff Line - Record representing a single line in a diff result.
 * 差异行 - 表示差异结果中单行的记录。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Diff type (EQUAL, INSERT, DELETE, MODIFY) - 差异类型</li>
 *   <li>Line number tracking for original and revised - 原始和修改行号跟踪</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DiffLine line = new DiffLine(DiffLine.Type.INSERT, -1, 5, "new content");
 * DiffLine.Type type = line.type(); // INSERT
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
public record DiffLine(Type type, int originalLine, int revisedLine, String content) {
    public enum Type {
        EQUAL, INSERT, DELETE, MODIFY
    }
}
