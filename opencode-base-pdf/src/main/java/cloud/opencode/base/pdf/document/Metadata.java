package cloud.opencode.base.pdf.document;

import java.time.Instant;
import java.util.List;

/**
 * PDF Document Metadata - Document information dictionary
 * PDF 文档元数据 - 文档信息字典
 *
 * <p>Contains standard PDF metadata fields as defined in PDF specification.</p>
 * <p>包含 PDF 规范中定义的标准元数据字段。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Standard PDF metadata fields (title, author, subject, keywords) - 标准 PDF 元数据字段（标题、作者、主题、关键词）</li>
 *   <li>Immutable record with builder support - 不可变记录，支持构建器</li>
 *   <li>Copy-with methods for updating fields - 用于更新字段的 with 方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Metadata meta = Metadata.builder()
 *     .title("My Document")
 *     .author("John Doe")
 *     .keywords("pdf", "document")
 *     .build();
 *
 * Metadata updated = meta.withTitle("New Title");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes — immutable record - 线程安全: 是 — 不可变记录</li>
 *   <li>Null-safe: Partial — fields may be null - 空值安全: 部分 — 字段可能为空</li>
 * </ul>
 *
 * @param title        document title | 文档标题
 * @param author       document author | 文档作者
 * @param subject      document subject | 文档主题
 * @param keywords     document keywords | 文档关键词
 * @param creator      creating application | 创建应用程序
 * @param producer     PDF producer | PDF 生成器
 * @param creationDate creation date | 创建日期
 * @param modDate      modification date | 修改日期
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public record Metadata(
    String title,
    String author,
    String subject,
    List<String> keywords,
    String creator,
    String producer,
    Instant creationDate,
    Instant modDate
) {

    /** Default producer name | 默认生成器名称 */
    public static final String DEFAULT_PRODUCER = "OpenCode PDF";

    /**
     * Creates empty metadata
     * 创建空的元数据
     *
     * @return empty metadata | 空的元数据
     */
    public static Metadata empty() {
        return new Metadata(null, null, null, List.of(), null, DEFAULT_PRODUCER, null, null);
    }

    /**
     * Creates metadata with title only
     * 仅创建带标题的元数据
     *
     * @param title document title | 文档标题
     * @return metadata with title | 带标题的元数据
     */
    public static Metadata ofTitle(String title) {
        return new Metadata(title, null, null, List.of(), null, DEFAULT_PRODUCER, Instant.now(), null);
    }

    /**
     * Creates a builder for metadata
     * 创建元数据构建器
     *
     * @return metadata builder | 元数据构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a copy with updated title
     * 创建更新标题后的副本
     *
     * @param newTitle new title | 新标题
     * @return updated metadata | 更新后的元数据
     */
    public Metadata withTitle(String newTitle) {
        return new Metadata(newTitle, author, subject, keywords, creator, producer, creationDate, modDate);
    }

    /**
     * Creates a copy with updated author
     * 创建更新作者后的副本
     *
     * @param newAuthor new author | 新作者
     * @return updated metadata | 更新后的元数据
     */
    public Metadata withAuthor(String newAuthor) {
        return new Metadata(title, newAuthor, subject, keywords, creator, producer, creationDate, modDate);
    }

    /**
     * Metadata Builder
     * 元数据构建器
     */
    public static final class Builder {
        private String title;
        private String author;
        private String subject;
        private List<String> keywords = List.of();
        private String creator;
        private String producer = DEFAULT_PRODUCER;
        private Instant creationDate;
        private Instant modDate;

        private Builder() {}

        /**
         * Sets document title
         * 设置文档标题
         *
         * @param title document title | 文档标题
         * @return this builder | 当前构建器
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Sets document author
         * 设置文档作者
         *
         * @param author document author | 文档作者
         * @return this builder | 当前构建器
         */
        public Builder author(String author) {
            this.author = author;
            return this;
        }

        /**
         * Sets document subject
         * 设置文档主题
         *
         * @param subject document subject | 文档主题
         * @return this builder | 当前构建器
         */
        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * Sets document keywords
         * 设置文档关键词
         *
         * @param keywords document keywords | 文档关键词
         * @return this builder | 当前构建器
         */
        public Builder keywords(String... keywords) {
            this.keywords = List.of(keywords);
            return this;
        }

        /**
         * Sets document keywords from list
         * 从列表设置文档关键词
         *
         * @param keywords document keywords | 文档关键词
         * @return this builder | 当前构建器
         */
        public Builder keywords(List<String> keywords) {
            this.keywords = List.copyOf(keywords);
            return this;
        }

        /**
         * Sets creator application
         * 设置创建应用程序
         *
         * @param creator creator application | 创建应用程序
         * @return this builder | 当前构建器
         */
        public Builder creator(String creator) {
            this.creator = creator;
            return this;
        }

        /**
         * Sets PDF producer
         * 设置 PDF 生成器
         *
         * @param producer PDF producer | PDF 生成器
         * @return this builder | 当前构建器
         */
        public Builder producer(String producer) {
            this.producer = producer;
            return this;
        }

        /**
         * Sets creation date
         * 设置创建日期
         *
         * @param creationDate creation date | 创建日期
         * @return this builder | 当前构建器
         */
        public Builder creationDate(Instant creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        /**
         * Sets modification date
         * 设置修改日期
         *
         * @param modDate modification date | 修改日期
         * @return this builder | 当前构建器
         */
        public Builder modDate(Instant modDate) {
            this.modDate = modDate;
            return this;
        }

        /**
         * Builds the metadata
         * 构建元数据
         *
         * @return metadata instance | 元数据实例
         */
        public Metadata build() {
            if (creationDate == null) {
                creationDate = Instant.now();
            }
            return new Metadata(title, author, subject, keywords, creator, producer, creationDate, modDate);
        }
    }
}
