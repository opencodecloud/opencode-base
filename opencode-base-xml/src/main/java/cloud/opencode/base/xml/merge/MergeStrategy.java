package cloud.opencode.base.xml.merge;

/**
 * Merge Strategy - Strategies for merging XML documents
 * 合并策略 - XML 文档的合并策略
 *
 * <p>Defines how conflicts are resolved when merging an overlay document
 * onto a base document.</p>
 * <p>定义将覆盖文档合并到基础文档时如何解决冲突。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>OVERRIDE - overlay values replace base values - 覆盖值替换基础值</li>
 *   <li>APPEND - overlay elements are appended - 覆盖元素追加到末尾</li>
 *   <li>SKIP_EXISTING - only add elements not in base - 仅添加基础文档中不存在的元素</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
public enum MergeStrategy {

    /**
     * Overlay values replace base values for matching elements.
     * 覆盖值替换匹配元素的基础值。
     */
    OVERRIDE,

    /**
     * Overlay elements are appended to the base document.
     * 覆盖元素追加到基础文档。
     */
    APPEND,

    /**
     * Only add elements from overlay that do not exist in base.
     * 仅从覆盖文档中添加基础文档中不存在的元素。
     */
    SKIP_EXISTING
}
