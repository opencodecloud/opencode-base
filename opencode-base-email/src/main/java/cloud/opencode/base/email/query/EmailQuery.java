package cloud.opencode.base.email.query;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Email Query Record
 * 邮件查询记录
 *
 * <p>Immutable query configuration for filtering emails.</p>
 * <p>用于过滤邮件的不可变查询配置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Folder selection - 文件夹选择</li>
 *   <li>Date range filtering - 日期范围过滤</li>
 *   <li>Sender/recipient filtering - 发件人/收件人过滤</li>
 *   <li>Subject search - 主题搜索</li>
 *   <li>Content search - 内容搜索</li>
 *   <li>Flag filtering (unread, flagged) - 标记过滤（未读、星标）</li>
 *   <li>Attachment filtering - 附件过滤</li>
 *   <li>Pagination support - 分页支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Query unread emails from last 7 days
 * EmailQuery query = EmailQuery.builder()
 *     .folder(EmailFolder.INBOX)
 *     .fromDate(LocalDateTime.now().minusDays(7))
 *     .unreadOnly()
 *     .limit(100)
 *     .build();
 *
 * // Query emails from specific sender
 * EmailQuery query = EmailQuery.builder()
 *     .from("sender@example.com")
 *     .subjectContains("Report")
 *     .build();
 *
 * // Query flagged emails with attachments
 * EmailQuery query = EmailQuery.builder()
 *     .flaggedOnly()
 *     .hasAttachments()
 *     .build();
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
public record EmailQuery(
        String folder,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        Set<String> from,
        Set<String> to,
        String subjectContains,
        String bodyContains,
        boolean unreadOnly,
        boolean flaggedOnly,
        boolean hasAttachments,
        boolean includeDeleted,
        int limit,
        int offset,
        SortOrder sortOrder
) {

    /**
     * Sort order for email queries
     * 邮件查询排序顺序
     */
    public enum SortOrder {
        /**
         * Newest first (default)
         * 最新优先（默认）
         */
        NEWEST_FIRST,

        /**
         * Oldest first
         * 最旧优先
         */
        OLDEST_FIRST,

        /**
         * By subject (A-Z)
         * 按主题（A-Z）
         */
        SUBJECT_ASC,

        /**
         * By subject (Z-A)
         * 按主题（Z-A）
         */
        SUBJECT_DESC,

        /**
         * By sender (A-Z)
         * 按发件人（A-Z）
         */
        SENDER_ASC,

        /**
         * By sender (Z-A)
         * 按发件人（Z-A）
         */
        SENDER_DESC
    }

    /**
     * Create a new builder
     * 创建新的构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a query for all unread emails
     * 创建查询所有未读邮件的查询
     *
     * @return the query | 查询
     */
    public static EmailQuery unread() {
        return builder().unreadOnly().build();
    }

    /**
     * Create a query for all emails in folder
     * 创建查询文件夹中所有邮件的查询
     *
     * @param folder the folder | 文件夹
     * @return the query | 查询
     */
    public static EmailQuery forFolder(EmailFolder folder) {
        return builder().folder(folder).build();
    }

    /**
     * Create a query for all emails in folder
     * 创建查询文件夹中所有邮件的查询
     *
     * @param folder the folder name | 文件夹名称
     * @return the query | 查询
     */
    public static EmailQuery forFolder(String folder) {
        return builder().folder(folder).build();
    }

    /**
     * Check if query has any filters
     * 检查查询是否有任何过滤条件
     *
     * @return true if has filters | 有过滤条件返回true
     */
    public boolean hasFilters() {
        return fromDate != null || toDate != null
                || (from != null && !from.isEmpty())
                || (to != null && !to.isEmpty())
                || subjectContains != null
                || bodyContains != null
                || unreadOnly || flaggedOnly || hasAttachments;
    }

    /**
     * Email Query Builder
     * 邮件查询构建器
     */
    public static class Builder {
        private String folder = "INBOX";
        private LocalDateTime fromDate;
        private LocalDateTime toDate;
        private Set<String> from;
        private Set<String> to;
        private String subjectContains;
        private String bodyContains;
        private boolean unreadOnly = false;
        private boolean flaggedOnly = false;
        private boolean hasAttachments = false;
        private boolean includeDeleted = false;
        private int limit = 100;
        private int offset = 0;
        private SortOrder sortOrder = SortOrder.NEWEST_FIRST;

        /**
         * Set the folder to query
         * 设置要查询的文件夹
         *
         * @param folder the folder | 文件夹
         * @return this builder | 构建器
         */
        public Builder folder(EmailFolder folder) {
            this.folder = folder.getName();
            return this;
        }

        /**
         * Set the folder to query by name
         * 根据名称设置要查询的文件夹
         *
         * @param folder the folder name | 文件夹名称
         * @return this builder | 构建器
         */
        public Builder folder(String folder) {
            this.folder = folder;
            return this;
        }

        /**
         * Set minimum date for emails
         * 设置邮件最小日期
         *
         * @param fromDate the minimum date | 最小日期
         * @return this builder | 构建器
         */
        public Builder fromDate(LocalDateTime fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        /**
         * Set maximum date for emails
         * 设置邮件最大日期
         *
         * @param toDate the maximum date | 最大日期
         * @return this builder | 构建器
         */
        public Builder toDate(LocalDateTime toDate) {
            this.toDate = toDate;
            return this;
        }

        /**
         * Set date range for emails
         * 设置邮件日期范围
         *
         * @param fromDate the minimum date | 最小日期
         * @param toDate   the maximum date | 最大日期
         * @return this builder | 构建器
         */
        public Builder dateRange(LocalDateTime fromDate, LocalDateTime toDate) {
            this.fromDate = fromDate;
            this.toDate = toDate;
            return this;
        }

        /**
         * Filter by sender address
         * 按发件人地址过滤
         *
         * @param from the sender address | 发件人地址
         * @return this builder | 构建器
         */
        public Builder from(String from) {
            this.from = Set.of(from);
            return this;
        }

        /**
         * Filter by multiple sender addresses
         * 按多个发件人地址过滤
         *
         * @param from the sender addresses | 发件人地址列表
         * @return this builder | 构建器
         */
        public Builder from(Set<String> from) {
            this.from = from != null ? Set.copyOf(from) : null;
            return this;
        }

        /**
         * Filter by recipient address
         * 按收件人地址过滤
         *
         * @param to the recipient address | 收件人地址
         * @return this builder | 构建器
         */
        public Builder to(String to) {
            this.to = Set.of(to);
            return this;
        }

        /**
         * Filter by multiple recipient addresses
         * 按多个收件人地址过滤
         *
         * @param to the recipient addresses | 收件人地址列表
         * @return this builder | 构建器
         */
        public Builder to(Set<String> to) {
            this.to = to != null ? Set.copyOf(to) : null;
            return this;
        }

        /**
         * Filter by subject containing text
         * 按主题包含文本过滤
         *
         * @param subject the text to search in subject | 要在主题中搜索的文本
         * @return this builder | 构建器
         */
        public Builder subjectContains(String subject) {
            this.subjectContains = subject;
            return this;
        }

        /**
         * Filter by body containing text
         * 按正文包含文本过滤
         *
         * @param body the text to search in body | 要在正文中搜索的文本
         * @return this builder | 构建器
         */
        public Builder bodyContains(String body) {
            this.bodyContains = body;
            return this;
        }

        /**
         * Filter to only unread emails
         * 仅过滤未读邮件
         *
         * @return this builder | 构建器
         */
        public Builder unreadOnly() {
            this.unreadOnly = true;
            return this;
        }

        /**
         * Set unread filter
         * 设置未读过滤
         *
         * @param unreadOnly true for unread only | true仅未读
         * @return this builder | 构建器
         */
        public Builder unreadOnly(boolean unreadOnly) {
            this.unreadOnly = unreadOnly;
            return this;
        }

        /**
         * Filter to only flagged/starred emails
         * 仅过滤标记/星标邮件
         *
         * @return this builder | 构建器
         */
        public Builder flaggedOnly() {
            this.flaggedOnly = true;
            return this;
        }

        /**
         * Set flagged filter
         * 设置标记过滤
         *
         * @param flaggedOnly true for flagged only | true仅标记
         * @return this builder | 构建器
         */
        public Builder flaggedOnly(boolean flaggedOnly) {
            this.flaggedOnly = flaggedOnly;
            return this;
        }

        /**
         * Filter to only emails with attachments
         * 仅过滤有附件的邮件
         *
         * @return this builder | 构建器
         */
        public Builder hasAttachments() {
            this.hasAttachments = true;
            return this;
        }

        /**
         * Set attachment filter
         * 设置附件过滤
         *
         * @param hasAttachments true for emails with attachments | true有附件
         * @return this builder | 构建器
         */
        public Builder hasAttachments(boolean hasAttachments) {
            this.hasAttachments = hasAttachments;
            return this;
        }

        /**
         * Include deleted emails
         * 包含已删除邮件
         *
         * @return this builder | 构建器
         */
        public Builder includeDeleted() {
            this.includeDeleted = true;
            return this;
        }

        /**
         * Set include deleted filter
         * 设置包含已删除过滤
         *
         * @param includeDeleted true to include deleted | true包含已删除
         * @return this builder | 构建器
         */
        public Builder includeDeleted(boolean includeDeleted) {
            this.includeDeleted = includeDeleted;
            return this;
        }

        /**
         * Set maximum number of emails to return
         * 设置返回的最大邮件数
         *
         * @param limit the maximum count | 最大数量
         * @return this builder | 构建器
         */
        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        /**
         * Set offset for pagination
         * 设置分页偏移量
         *
         * @param offset the offset | 偏移量
         * @return this builder | 构建器
         */
        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }

        /**
         * Set pagination
         * 设置分页
         *
         * @param limit  the maximum count | 最大数量
         * @param offset the offset | 偏移量
         * @return this builder | 构建器
         */
        public Builder page(int limit, int offset) {
            this.limit = limit;
            this.offset = offset;
            return this;
        }

        /**
         * Set sort order
         * 设置排序顺序
         *
         * @param sortOrder the sort order | 排序顺序
         * @return this builder | 构建器
         */
        public Builder sortBy(SortOrder sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        /**
         * Sort by newest first
         * 按最新优先排序
         *
         * @return this builder | 构建器
         */
        public Builder newestFirst() {
            this.sortOrder = SortOrder.NEWEST_FIRST;
            return this;
        }

        /**
         * Sort by oldest first
         * 按最旧优先排序
         *
         * @return this builder | 构建器
         */
        public Builder oldestFirst() {
            this.sortOrder = SortOrder.OLDEST_FIRST;
            return this;
        }

        /**
         * Build the query
         * 构建查询
         *
         * @return the query | 查询
         */
        public EmailQuery build() {
            return new EmailQuery(
                    folder,
                    fromDate,
                    toDate,
                    from,
                    to,
                    subjectContains,
                    bodyContains,
                    unreadOnly,
                    flaggedOnly,
                    hasAttachments,
                    includeDeleted,
                    limit,
                    offset,
                    sortOrder
            );
        }
    }
}
