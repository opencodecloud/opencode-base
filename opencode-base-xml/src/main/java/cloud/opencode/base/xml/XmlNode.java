package cloud.opencode.base.xml;

import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

/**
 * XML Node Interface - Base interface for XML nodes
 * XML 节点接口 - XML 节点的基础接口
 *
 * <p>This interface defines common operations for XML nodes including elements,
 * attributes, text, and other node types.</p>
 * <p>此接口定义了 XML 节点的通用操作，包括元素、属性、文本和其他节点类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Name and namespace access - 名称和命名空间访问</li>
 *   <li>Text content retrieval - 文本内容获取</li>
 *   <li>XML serialization with optional indentation - 带可选缩进的 XML 序列化</li>
 *   <li>Map conversion for data interchange - 用于数据交换的 Map 转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * XmlNode node = ...; // XmlElement implements XmlNode
 * String name = node.getName();
 * String text = node.getText();
 * String xml = node.toXml(4);
 * Map<String, Object> map = node.toMap();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public interface XmlNode {

    /**
     * Returns the node name.
     * 返回节点名称。
     *
     * @return the node name | 节点名称
     */
    String getName();

    /**
     * Returns the local name (without namespace prefix).
     * 返回本地名称（不含命名空间前缀）。
     *
     * @return the local name | 本地名称
     */
    String getLocalName();

    /**
     * Returns the namespace URI.
     * 返回命名空间 URI。
     *
     * @return the namespace URI, or null if not set | 命名空间 URI，如果未设置则返回 null
     */
    String getNamespaceUri();

    /**
     * Returns the namespace prefix.
     * 返回命名空间前缀。
     *
     * @return the prefix, or null if not set | 前缀，如果未设置则返回 null
     */
    String getPrefix();

    /**
     * Returns the text content.
     * 返回文本内容。
     *
     * @return the text content | 文本内容
     */
    String getText();

    /**
     * Returns the text content trimmed.
     * 返回去除空白的文本内容。
     *
     * @return the trimmed text content | 去除空白的文本内容
     */
    String getTextTrim();

    /**
     * Returns whether this node has any text content.
     * 返回此节点是否有任何文本内容。
     *
     * @return true if has text content | 如果有文本内容则返回 true
     */
    boolean hasText();

    /**
     * Returns the underlying DOM Node.
     * 返回底层的 DOM 节点。
     *
     * @return the DOM node | DOM 节点
     */
    Node getNode();

    /**
     * Converts this node to XML string.
     * 将此节点转换为 XML 字符串。
     *
     * @return the XML string | XML 字符串
     */
    String toXml();

    /**
     * Converts this node to XML string with indentation.
     * 将此节点转换为带缩进的 XML 字符串。
     *
     * @param indent the number of spaces for indentation | 缩进空格数
     * @return the formatted XML string | 格式化的 XML 字符串
     */
    String toXml(int indent);

    /**
     * Converts this node to a Map.
     * 将此节点转换为 Map。
     *
     * @return the Map representation | Map 表示
     */
    Map<String, Object> toMap();

    /**
     * Node type enumeration.
     * 节点类型枚举。
     */
    enum NodeType {
        /** Element node */
        ELEMENT,
        /** Attribute node */
        ATTRIBUTE,
        /** Text node */
        TEXT,
        /** CDATA section node */
        CDATA,
        /** Comment node */
        COMMENT,
        /** Document node */
        DOCUMENT,
        /** Processing instruction node */
        PROCESSING_INSTRUCTION,
        /** Unknown node type */
        UNKNOWN
    }
}
