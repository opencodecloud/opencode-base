package cloud.opencode.base.xml.diff;

/**
 * Diff Type - Types of differences between XML documents
 * 差异类型 - XML 文档之间的差异类型
 *
 * <p>This enum represents the different types of changes that can be detected
 * when comparing two XML documents.</p>
 * <p>此枚举表示比较两个 XML 文档时可以检测到的不同变更类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Element-level changes (added, removed, modified) - 元素级变更（新增、删除、修改）</li>
 *   <li>Attribute-level changes (added, removed, modified) - 属性级变更（新增、删除、修改）</li>
 *   <li>Text content changes - 文本内容变更</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
public enum DiffType {

    /**
     * Element added in the second document.
     * 第二个文档中新增了元素。
     */
    ADDED,

    /**
     * Element removed from the first document.
     * 第一个文档中删除了元素。
     */
    REMOVED,

    /**
     * Element modified between documents (structural change).
     * 文档之间的元素已修改（结构变更）。
     */
    MODIFIED,

    /**
     * Attribute added in the second document.
     * 第二个文档中新增了属性。
     */
    ATTRIBUTE_ADDED,

    /**
     * Attribute removed from the first document.
     * 第一个文档中删除了属性。
     */
    ATTRIBUTE_REMOVED,

    /**
     * Attribute value modified between documents.
     * 文档之间的属性值已修改。
     */
    ATTRIBUTE_MODIFIED,

    /**
     * Text content modified between documents.
     * 文档之间的文本内容已修改。
     */
    TEXT_MODIFIED
}
