package cloud.opencode.base.xml.dom;

import cloud.opencode.base.xml.exception.OpenXmlException;
import cloud.opencode.base.xml.security.SecureParserFactory;
import org.w3c.dom.*;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * DOM Utility Class - Utility methods for DOM manipulation
 * DOM 工具类 - DOM 操作的工具方法
 *
 * <p>This class provides static utility methods for common DOM operations
 * including serialization, conversion, and node manipulation.</p>
 * <p>此类提供用于常见 DOM 操作的静态工具方法，包括序列化、转换和节点操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Document/Node to String conversion - 文档/节点转字符串</li>
 *   <li>Element to Map conversion - 元素转 Map</li>
 *   <li>Text type conversion - 文本类型转换</li>
 *   <li>Node cloning and manipulation - 节点克隆和操作</li>
 * </ul>
 *
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Convert document to string
 * String xml = DomUtil.toString(document, 4);
 * 
 * // Convert element to map
 * Map<String, String> map = DomUtil.toMap(element);
 * 
 * // Get text content with type conversion
 * int value = DomUtil.getInt(element, "count", 0);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: No (throws on null document/node) - 空值安全: 否（null 文档/节点抛异常）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for elementToMap/toMap (recursive traversal of all nodes, depth-limited to 1000); O(c) for getChildElements where c=child count; O(1) for scalar getters - 时间复杂度: elementToMap/toMap 为 O(n)（递归遍历所有节点，深度限制为 1000）；getChildElements 为 O(c)，c 为子元素数；标量获取器为 O(1)</li>
 *   <li>Space complexity: O(n) for map conversions; O(1) for scalar operations - 空间复杂度: map 转换为 O(n)；标量操作为 O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class DomUtil {

    private DomUtil() {
        // Utility class
    }

    // ==================== Document Creation | 文档创建 ====================

    /**
     * Creates a new empty Document.
     * 创建新的空 Document。
     *
     * @return a new empty Document | 新的空 Document
     */
    public static Document createDocument() {
        try {
            return SecureParserFactory.createDocumentBuilder().newDocument();
        } catch (Exception e) {
            throw new OpenXmlException("Failed to create Document", e);
        }
    }

    /**
     * Creates a new Document with a root element.
     * 创建带根元素的新 Document。
     *
     * @param rootElementName the root element name | 根元素名称
     * @return the new Document | 新 Document
     */
    public static Document createDocument(String rootElementName) {
        Document doc = createDocument();
        doc.appendChild(doc.createElement(rootElementName));
        return doc;
    }

    // ==================== Serialization | 序列化 ====================

    /**
     * Converts a Document to XML string.
     * 将 Document 转换为 XML 字符串。
     *
     * @param document the Document | Document
     * @return the XML string | XML 字符串
     */
    public static String toString(Document document) {
        return toString(document, 0);
    }

    /**
     * Converts a Document to formatted XML string.
     * 将 Document 转换为格式化的 XML 字符串。
     *
     * @param document the Document | Document
     * @param indent   the number of spaces for indentation | 缩进空格数
     * @return the formatted XML string | 格式化的 XML 字符串
     */
    public static String toString(Document document, int indent) {
        try {
            Transformer transformer = SecureParserFactory.createTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            if (indent > 0) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                    String.valueOf(indent));
            }

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            throw new OpenXmlException("Failed to serialize Document", e);
        }
    }

    /**
     * Converts a Node to XML string.
     * 将 Node 转换为 XML 字符串。
     *
     * @param node the Node | Node
     * @return the XML string | XML 字符串
     */
    public static String toString(Node node) {
        return toString(node, 0);
    }

    /**
     * Converts a Node to formatted XML string.
     * 将 Node 转换为格式化的 XML 字符串。
     *
     * @param node   the Node | Node
     * @param indent the number of spaces for indentation | 缩进空格数
     * @return the formatted XML string | 格式化的 XML 字符串
     */
    public static String toString(Node node, int indent) {
        try {
            Transformer transformer = SecureParserFactory.createTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            if (indent > 0) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                    String.valueOf(indent));
            }

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            throw new OpenXmlException("Failed to serialize Node", e);
        }
    }

    // ==================== Conversion | 转换 ====================

    /**
     * Maximum recursion depth for DOM traversal to prevent stack overflow.
     * DOM 遍历的最大递归深度以防止栈溢出。
     */
    private static final int MAX_DEPTH = 1000;

    /**
     * Converts an Element to a Map.
     * 将 Element 转换为 Map。
     *
     * @param element the Element | Element
     * @return the Map representation | Map 表示
     */
    public static Map<String, Object> elementToMap(Element element) {
        return elementToMap(element, 0);
    }

    private static Map<String, Object> elementToMap(Element element, int depth) {
        if (depth >= MAX_DEPTH) {
            throw new OpenXmlException(
                "DOM traversal exceeded maximum depth of " + MAX_DEPTH +
                " - possible circular reference or excessively nested XML");
        }

        Map<String, Object> map = new LinkedHashMap<>();

        // Add attributes
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            map.put("@" + attr.getNodeName(), attr.getNodeValue());
        }

        // Process child nodes
        Map<String, List<Object>> childMap = new LinkedHashMap<>();
        NodeList children = element.getChildNodes();
        StringBuilder textContent = new StringBuilder();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child instanceof Element childElem) {
                String tagName = childElem.getTagName();
                childMap.computeIfAbsent(tagName, _ -> new ArrayList<>())
                    .add(elementToMap(childElem, depth + 1));
            } else if (child instanceof Text textNode) {
                String text = textNode.getNodeValue().trim();
                if (!text.isEmpty()) {
                    textContent.append(text);
                }
            }
        }

        // Flatten single-element lists
        for (Map.Entry<String, List<Object>> entry : childMap.entrySet()) {
            if (entry.getValue().size() == 1) {
                map.put(entry.getKey(), entry.getValue().getFirst());
            } else {
                map.put(entry.getKey(), entry.getValue());
            }
        }

        // Add text content if present and no child elements
        if (!textContent.isEmpty() && childMap.isEmpty()) {
            map.put("#text", textContent.toString());
        }

        return map;
    }

    /**
     * Converts text to the specified type.
     * 将文本转换为指定类型。
     *
     * @param <T>   the type parameter | 类型参数
     * @param text  the text to convert | 要转换的文本
     * @param clazz the target type | 目标类型
     * @return the converted value | 转换后的值
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertText(String text, Class<T> clazz) {
        if (text == null || text.isBlank()) {
            return null;
        }

        text = text.trim();

        if (clazz == String.class) {
            return (T) text;
        }
        if (clazz == Integer.class || clazz == int.class) {
            return (T) Integer.valueOf(text);
        }
        if (clazz == Long.class || clazz == long.class) {
            return (T) Long.valueOf(text);
        }
        if (clazz == Double.class || clazz == double.class) {
            return (T) Double.valueOf(text);
        }
        if (clazz == Float.class || clazz == float.class) {
            return (T) Float.valueOf(text);
        }
        if (clazz == Boolean.class || clazz == boolean.class) {
            return (T) Boolean.valueOf(text);
        }
        if (clazz == Short.class || clazz == short.class) {
            return (T) Short.valueOf(text);
        }
        if (clazz == Byte.class || clazz == byte.class) {
            return (T) Byte.valueOf(text);
        }
        if (clazz == BigDecimal.class) {
            return (T) new BigDecimal(text);
        }
        if (clazz == BigInteger.class) {
            return (T) new BigInteger(text);
        }

        throw new OpenXmlException("Cannot convert text to type: " + clazz.getName());
    }

    // ==================== Node Operations | 节点操作 ====================

    /**
     * Deep clones a node into another document.
     * 将节点深度克隆到另一个文档。
     *
     * @param node     the node to clone | 要克隆的节点
     * @param document the target document | 目标文档
     * @return the cloned node | 克隆的节点
     */
    public static Node cloneInto(Node node, Document document) {
        return document.importNode(node, true);
    }

    /**
     * Gets all child elements of a node.
     * 获取节点的所有子元素。
     *
     * @param parent the parent node | 父节点
     * @return list of child elements | 子元素列表
     */
    public static List<Element> getChildElements(Node parent) {
        List<Element> elements = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element elem) {
                elements.add(elem);
            }
        }
        return elements;
    }

    /**
     * Gets child elements with the specified tag name.
     * 获取具有指定标签名的子元素。
     *
     * @param parent  the parent node | 父节点
     * @param tagName the tag name | 标签名
     * @return list of matching elements | 匹配元素列表
     */
    public static List<Element> getChildElements(Node parent, String tagName) {
        List<Element> elements = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element elem && tagName.equals(elem.getTagName())) {
                elements.add(elem);
            }
        }
        return elements;
    }

    /**
     * Gets the first child element with the specified tag name.
     * 获取具有指定标签名的第一个子元素。
     *
     * @param parent  the parent node | 父节点
     * @param tagName the tag name | 标签名
     * @return the first matching element, or null | 第一个匹配的元素，或 null
     */
    public static Element getFirstChildElement(Node parent, String tagName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element elem && tagName.equals(elem.getTagName())) {
                return elem;
            }
        }
        return null;
    }

    /**
     * Gets the text content of the first child element with the specified tag name.
     * 获取具有指定标签名的第一个子元素的文本内容。
     *
     * @param parent  the parent node | 父节点
     * @param tagName the tag name | 标签名
     * @return the text content, or null | 文本内容，或 null
     */
    public static String getChildText(Node parent, String tagName) {
        Element child = getFirstChildElement(parent, tagName);
        return child != null ? child.getTextContent() : null;
    }

    /**
     * Removes all child nodes from a node.
     * 从节点移除所有子节点。
     *
     * @param node the node to clear | 要清空的节点
     */
    public static void removeAllChildren(Node node) {
        while (node.hasChildNodes()) {
            node.removeChild(node.getFirstChild());
        }
    }

    /**
     * Adds a child element with text content.
     * 添加带文本内容的子元素。
     *
     * @param parent  the parent element | 父元素
     * @param tagName the tag name | 标签名
     * @param text    the text content | 文本内容
     * @return the new element | 新元素
     */
    public static Element addChildElement(Element parent, String tagName, String text) {
        Document doc = parent.getOwnerDocument();
        Element child = doc.createElement(tagName);
        if (text != null) {
            child.setTextContent(text);
        }
        parent.appendChild(child);
        return child;
    }
}
