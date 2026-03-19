package cloud.opencode.base.email.query;

/**
 * Email Folder Enumeration
 * 邮件文件夹枚举
 *
 * <p>Standard email folder names used by IMAP/POP3.</p>
 * <p>IMAP/POP3使用的标准邮件文件夹名称。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Standard folder names - 标准文件夹名称</li>
 *   <li>Common name aliases - 通用名称别名</li>
 *   <li>Provider-specific name mapping - 供应商特定名称映射</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use standard folder
 * EmailQuery query = EmailQuery.builder()
 *     .folder(EmailFolder.INBOX)
 *     .unreadOnly()
 *     .build();
 *
 * // Use custom folder name
 * EmailQuery query = EmailQuery.builder()
 *     .folder("Custom/MyFolder")
 *     .build();
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum is immutable) - 线程安全: 是（枚举不可变）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public enum EmailFolder {

    /**
     * Inbox folder (default)
     * 收件箱文件夹（默认）
     */
    INBOX("INBOX", "Inbox"),

    /**
     * Sent items folder
     * 已发送邮件文件夹
     */
    SENT("Sent", "Sent Items", "Sent Messages", "[Gmail]/Sent Mail"),

    /**
     * Drafts folder
     * 草稿箱文件夹
     */
    DRAFTS("Drafts", "Draft", "[Gmail]/Drafts"),

    /**
     * Trash/Deleted items folder
     * 已删除/垃圾箱文件夹
     */
    TRASH("Trash", "Deleted Items", "Deleted Messages", "[Gmail]/Trash"),

    /**
     * Spam/Junk folder
     * 垃圾邮件文件夹
     */
    SPAM("Spam", "Junk", "Junk E-mail", "[Gmail]/Spam"),

    /**
     * Archive folder
     * 归档文件夹
     */
    ARCHIVE("Archive", "All Mail", "[Gmail]/All Mail"),

    /**
     * Starred/Flagged items folder
     * 星标/标记邮件文件夹
     */
    STARRED("Starred", "Flagged", "[Gmail]/Starred"),

    /**
     * Important items folder
     * 重要邮件文件夹
     */
    IMPORTANT("Important", "[Gmail]/Important");

    private final String primaryName;
    private final String[] alternativeNames;

    EmailFolder(String primaryName, String... alternativeNames) {
        this.primaryName = primaryName;
        this.alternativeNames = alternativeNames;
    }

    /**
     * Get the primary folder name
     * 获取主要文件夹名称
     *
     * @return the primary folder name | 主要文件夹名称
     */
    public String getName() {
        return primaryName;
    }

    /**
     * Get alternative folder names
     * 获取可选文件夹名称
     *
     * @return array of alternative names | 可选名称数组
     */
    public String[] getAlternativeNames() {
        return alternativeNames.clone();
    }

    /**
     * Get all possible folder names (primary + alternatives)
     * 获取所有可能的文件夹名称（主要名称 + 可选名称）
     *
     * @return array of all possible names | 所有可能的名称数组
     */
    public String[] getAllNames() {
        String[] all = new String[alternativeNames.length + 1];
        all[0] = primaryName;
        System.arraycopy(alternativeNames, 0, all, 1, alternativeNames.length);
        return all;
    }

    /**
     * Find folder enum by name (case-insensitive)
     * 根据名称查找文件夹枚举（不区分大小写）
     *
     * @param name the folder name | 文件夹名称
     * @return the matching folder or null | 匹配的文件夹或null
     */
    public static EmailFolder fromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (EmailFolder folder : values()) {
            if (folder.primaryName.equalsIgnoreCase(name)) {
                return folder;
            }
            for (String alt : folder.alternativeNames) {
                if (alt.equalsIgnoreCase(name)) {
                    return folder;
                }
            }
        }
        return null;
    }

    /**
     * Check if this folder matches the given name
     * 检查此文件夹是否匹配给定名称
     *
     * @param name the folder name to check | 要检查的文件夹名称
     * @return true if matches | 匹配返回true
     */
    public boolean matches(String name) {
        if (name == null) return false;
        if (primaryName.equalsIgnoreCase(name)) return true;
        for (String alt : alternativeNames) {
            if (alt.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return primaryName;
    }
}
