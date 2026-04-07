package cloud.opencode.base.email;

/**
 * Email Flags Record
 * 邮件标记记录
 *
 * <p>Immutable record representing email status flags.</p>
 * <p>表示邮件状态标记的不可变记录。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Read/unread status - 已读/未读状态</li>
 *   <li>Answered status - 已回复状态</li>
 *   <li>Flagged/starred status - 标记/星标状态</li>
 *   <li>Deleted status - 已删除状态</li>
 *   <li>Draft status - 草稿状态</li>
 *   <li>Recent status - 最近状态</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check if email is unread
 * if (!email.flags().seen()) {
 *     processNewEmail(email);
 * }
 *
 * // Check if email is flagged/starred
 * if (email.flags().flagged()) {
 *     highlightEmail(email);
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public record EmailFlags(
        boolean seen,
        boolean answered,
        boolean flagged,
        boolean deleted,
        boolean draft,
        boolean recent
) {

    /**
     * Default flags for a new unread email
     * 新未读邮件的默认标记
     */
    public static final EmailFlags UNREAD = new EmailFlags(false, false, false, false, false, true);

    /**
     * Default flags for a read email
     * 已读邮件的默认标记
     */
    public static final EmailFlags READ = new EmailFlags(true, false, false, false, false, false);

    /**
     * Create flags from IMAP flags string
     * 从IMAP标记字符串创建标记
     *
     * <p>Parses an IMAP FLAGS response string like "(\\Seen \\Flagged \\Answered)"</p>
     * <p>解析IMAP FLAGS响应字符串，如 "(\\Seen \\Flagged \\Answered)"</p>
     *
     * @param imapFlags the IMAP flags string (e.g., "(\\Seen \\Flagged)") | IMAP标记字符串
     * @return the email flags | 邮件标记
     */
    public static EmailFlags fromImapFlags(String imapFlags) {
        if (imapFlags == null || imapFlags.isBlank()) {
            return UNREAD;
        }
        String upper = imapFlags.toUpperCase();
        return new EmailFlags(
                upper.contains("\\SEEN"),
                upper.contains("\\ANSWERED"),
                upper.contains("\\FLAGGED"),
                upper.contains("\\DELETED"),
                upper.contains("\\DRAFT"),
                upper.contains("\\RECENT")
        );
    }

    /**
     * Convert to IMAP flags string
     * 转换为IMAP标记字符串
     *
     * @return the IMAP flags string (e.g., "(\\Seen \\Flagged)") | IMAP标记字符串
     */
    public String toImapFlags() {
        StringBuilder sb = new StringBuilder("(");
        if (seen) sb.append("\\Seen ");
        if (answered) sb.append("\\Answered ");
        if (flagged) sb.append("\\Flagged ");
        if (deleted) sb.append("\\Deleted ");
        if (draft) sb.append("\\Draft ");
        if (recent) sb.append("\\Recent ");
        return sb.toString().trim() + ")";
    }

    /**
     * Check if email is unread
     * 检查邮件是否未读
     *
     * @return true if unread | 未读返回true
     */
    public boolean isUnread() {
        return !seen;
    }

    /**
     * Create a copy with seen flag set
     * 创建设置已读标记的副本
     *
     * @param seen the seen flag | 已读标记
     * @return new flags with updated seen status | 更新已读状态后的新标记
     */
    public EmailFlags withSeen(boolean seen) {
        return new EmailFlags(seen, answered, flagged, deleted, draft, recent);
    }

    /**
     * Create a copy with flagged status set
     * 创建设置标记状态的副本
     *
     * @param flagged the flagged status | 标记状态
     * @return new flags with updated flagged status | 更新标记状态后的新标记
     */
    public EmailFlags withFlagged(boolean flagged) {
        return new EmailFlags(seen, answered, flagged, deleted, draft, recent);
    }

    /**
     * Create a copy with deleted flag set
     * 创建设置删除标记的副本
     *
     * @param deleted the deleted flag | 删除标记
     * @return new flags with updated deleted status | 更新删除状态后的新标记
     */
    public EmailFlags withDeleted(boolean deleted) {
        return new EmailFlags(seen, answered, flagged, deleted, draft, recent);
    }

    /**
     * Create a copy with answered flag set
     * 创建设置已回复标记的副本
     *
     * @param answered the answered flag | 已回复标记
     * @return new flags with updated answered status | 更新已回复状态后的新标记
     */
    public EmailFlags withAnswered(boolean answered) {
        return new EmailFlags(seen, answered, flagged, deleted, draft, recent);
    }
}
