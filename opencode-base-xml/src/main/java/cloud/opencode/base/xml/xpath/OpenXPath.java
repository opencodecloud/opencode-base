package cloud.opencode.base.xml.xpath;

import cloud.opencode.base.xml.XmlElement;
import cloud.opencode.base.xml.dom.DomParser;
import cloud.opencode.base.xml.exception.XmlXPathException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * XPath Entry Class - Static facade for XPath operations
 * XPath 入口类 - XPath 操作的静态门面
 *
 * <p>This class provides static methods for evaluating XPath expressions
 * against XML content in various forms.</p>
 * <p>此类提供静态方法，用于针对各种形式的 XML 内容求值 XPath 表达式。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple string query
 * String name = OpenXPath.selectString(xml, "//user/name/text()");
 *
 * // Query elements
 * List<XmlElement> users = OpenXPath.selectElements(xml, "//user");
 *
 * // Query with namespace
 * String value = OpenXPath.withNamespaces(Map.of("ns", "http://example.com"))
 *     .selectString("//ns:item/text()");
 *
 * // Check existence
 * boolean exists = OpenXPath.exists(xml, "//user[@id='1']");
 *
 * // Count nodes
 * int count = OpenXPath.count(xml, "//user");
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Static utilities for XPath evaluation - XPath 求值的静态工具</li>
 *   <li>Namespace-aware XPath support - 支持命名空间的 XPath</li>
 *   <li>Multiple return types (string, number, node list) - 多种返回类型（字符串、数字、节点列表）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class OpenXPath {

    /**
     * ThreadLocal XPathFactory since XPathFactory is NOT thread-safe.
     * ThreadLocal XPathFactory，因为 XPathFactory 不是线程安全的。
     */
    private static final ThreadLocal<XPathFactory> XPATH_FACTORY =
            ThreadLocal.withInitial(XPathFactory::newInstance);

    private OpenXPath() {
        // Utility class
    }

    // ==================== String Query | 字符串查询 ====================

    /**
     * Selects a string value using XPath.
     * 使用 XPath 选择字符串值。
     *
     * @param xml   the XML string | XML 字符串
     * @param xpath the XPath expression | XPath 表达式
     * @return the result string | 结果字符串
     */
    public static String selectString(String xml, String xpath) {
        return selectString(DomParser.parse(xml), xpath);
    }

    /**
     * Selects a string value from a Document.
     * 从 Document 选择字符串值。
     *
     * @param document the Document | Document
     * @param xpath    the XPath expression | XPath 表达式
     * @return the result string | 结果字符串
     */
    public static String selectString(Document document, String xpath) {
        return selectString((Node) document, xpath);
    }

    /**
     * Selects a string value from a Node.
     * 从 Node 选择字符串值。
     *
     * @param node  the Node | Node
     * @param xpath the XPath expression | XPath 表达式
     * @return the result string | 结果字符串
     */
    public static String selectString(Node node, String xpath) {
        try {
            XPath xp = XPATH_FACTORY.get().newXPath();
            return xp.evaluate(xpath, node);
        } catch (XPathExpressionException e) {
            throw new XmlXPathException(xpath, e);
        }
    }

    /**
     * Selects a string value from a Document with namespace mappings.
     * 使用命名空间映射从 Document 选择字符串值。
     *
     * @param document   the Document | Document
     * @param xpath      the XPath expression | XPath 表达式
     * @param namespaces the namespace mappings (prefix -> URI) | 命名空间映射（前缀 -> URI）
     * @return the result string | 结果字符串
     */
    public static String selectString(Document document, String xpath, Map<String, String> namespaces) {
        return selectString((Node) document, xpath, namespaces);
    }

    /**
     * Selects a string value from a Node with namespace mappings.
     * 使用命名空间映射从 Node 选择字符串值。
     *
     * @param node       the Node | Node
     * @param xpath      the XPath expression | XPath 表达式
     * @param namespaces the namespace mappings (prefix -> URI) | 命名空间映射（前缀 -> URI）
     * @return the result string | 结果字符串
     */
    public static String selectString(Node node, String xpath, Map<String, String> namespaces) {
        try {
            XPath xp = XPATH_FACTORY.get().newXPath();
            xp.setNamespaceContext(new SimpleNamespaceContext(namespaces));
            return xp.evaluate(xpath, node);
        } catch (XPathExpressionException e) {
            throw new XmlXPathException(xpath, e);
        }
    }

    /**
     * Simple NamespaceContext implementation.
     * 简单的 NamespaceContext 实现。
     */
    private static class SimpleNamespaceContext implements javax.xml.namespace.NamespaceContext {
        private final Map<String, String> prefixToUri;

        SimpleNamespaceContext(Map<String, String> namespaces) {
            this.prefixToUri = namespaces != null ? namespaces : Map.of();
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return prefixToUri.getOrDefault(prefix, javax.xml.XMLConstants.NULL_NS_URI);
        }

        @Override
        public String getPrefix(String namespaceURI) {
            for (Map.Entry<String, String> entry : prefixToUri.entrySet()) {
                if (entry.getValue().equals(namespaceURI)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        @Override
        public java.util.Iterator<String> getPrefixes(String namespaceURI) {
            List<String> prefixes = new ArrayList<>();
            for (Map.Entry<String, String> entry : prefixToUri.entrySet()) {
                if (entry.getValue().equals(namespaceURI)) {
                    prefixes.add(entry.getKey());
                }
            }
            return prefixes.iterator();
        }
    }

    // ==================== Number Query | 数字查询 ====================

    /**
     * Selects a number using XPath.
     * 使用 XPath 选择数字。
     *
     * @param xml   the XML string | XML 字符串
     * @param xpath the XPath expression | XPath 表达式
     * @return the result number | 结果数字
     */
    public static Number selectNumber(String xml, String xpath) {
        return selectNumber(DomParser.parse(xml), xpath);
    }

    /**
     * Selects a number from a Document.
     * 从 Document 选择数字。
     *
     * @param document the Document | Document
     * @param xpath    the XPath expression | XPath 表达式
     * @return the result number | 结果数字
     */
    public static Number selectNumber(Document document, String xpath) {
        try {
            XPath xp = XPATH_FACTORY.get().newXPath();
            return (Number) xp.evaluate(xpath, document, XPathConstants.NUMBER);
        } catch (XPathExpressionException e) {
            throw new XmlXPathException(xpath, e);
        }
    }

    // ==================== Boolean Query | 布尔查询 ====================

    /**
     * Selects a boolean using XPath.
     * 使用 XPath 选择布尔值。
     *
     * @param xml   the XML string | XML 字符串
     * @param xpath the XPath expression | XPath 表达式
     * @return the result boolean | 结果布尔值
     */
    public static Boolean selectBoolean(String xml, String xpath) {
        return selectBoolean(DomParser.parse(xml), xpath);
    }

    /**
     * Selects a boolean from a Document.
     * 从 Document 选择布尔值。
     *
     * @param document the Document | Document
     * @param xpath    the XPath expression | XPath 表达式
     * @return the result boolean | 结果布尔值
     */
    public static Boolean selectBoolean(Document document, String xpath) {
        try {
            XPath xp = XPATH_FACTORY.get().newXPath();
            return (Boolean) xp.evaluate(xpath, document, XPathConstants.BOOLEAN);
        } catch (XPathExpressionException e) {
            throw new XmlXPathException(xpath, e);
        }
    }

    // ==================== Node Query | 节点查询 ====================

    /**
     * Selects a single Node using XPath.
     * 使用 XPath 选择单个节点。
     *
     * @param xml   the XML string | XML 字符串
     * @param xpath the XPath expression | XPath 表达式
     * @return the result Node | 结果节点
     */
    public static Node selectNode(String xml, String xpath) {
        return selectNode(DomParser.parse(xml), xpath);
    }

    /**
     * Selects a single Node from a Document.
     * 从 Document 选择单个节点。
     *
     * @param document the Document | Document
     * @param xpath    the XPath expression | XPath 表达式
     * @return the result Node | 结果节点
     */
    public static Node selectNode(Document document, String xpath) {
        return selectNode((Node) document, xpath);
    }

    /**
     * Selects a single Node from a Node.
     * 从 Node 选择单个节点。
     *
     * @param node  the Node | Node
     * @param xpath the XPath expression | XPath 表达式
     * @return the result Node | 结果节点
     */
    public static Node selectNode(Node node, String xpath) {
        try {
            XPath xp = XPATH_FACTORY.get().newXPath();
            return (Node) xp.evaluate(xpath, node, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new XmlXPathException(xpath, e);
        }
    }

    /**
     * Selects NodeList using XPath.
     * 使用 XPath 选择节点列表。
     *
     * @param xml   the XML string | XML 字符串
     * @param xpath the XPath expression | XPath 表达式
     * @return the result NodeList | 结果节点列表
     */
    public static NodeList selectNodes(String xml, String xpath) {
        return selectNodes(DomParser.parse(xml), xpath);
    }

    /**
     * Selects NodeList from a Document.
     * 从 Document 选择节点列表。
     *
     * @param document the Document | Document
     * @param xpath    the XPath expression | XPath 表达式
     * @return the result NodeList | 结果节点列表
     */
    public static NodeList selectNodes(Document document, String xpath) {
        return selectNodes((Node) document, xpath);
    }

    /**
     * Selects NodeList from a Node.
     * 从 Node 选择节点列表。
     *
     * @param node  the Node | Node
     * @param xpath the XPath expression | XPath 表达式
     * @return the result NodeList | 结果节点列表
     */
    public static NodeList selectNodes(Node node, String xpath) {
        try {
            XPath xp = XPATH_FACTORY.get().newXPath();
            return (NodeList) xp.evaluate(xpath, node, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new XmlXPathException(xpath, e);
        }
    }

    // ==================== Element Query | 元素查询 ====================

    /**
     * Selects a single XmlElement using XPath.
     * 使用 XPath 选择单个 XmlElement。
     *
     * @param xml   the XML string | XML 字符串
     * @param xpath the XPath expression | XPath 表达式
     * @return the result element, or null if not found | 结果元素，如果未找到则返回 null
     */
    public static XmlElement selectElement(String xml, String xpath) {
        return selectElement(DomParser.parse(xml), xpath);
    }

    /**
     * Selects a single XmlElement from a Document.
     * 从 Document 选择单个 XmlElement。
     *
     * @param document the Document | Document
     * @param xpath    the XPath expression | XPath 表达式
     * @return the result element, or null if not found | 结果元素，如果未找到则返回 null
     */
    public static XmlElement selectElement(Document document, String xpath) {
        return selectElement((Node) document, xpath);
    }

    /**
     * Selects a single XmlElement from a Node.
     * 从 Node 选择单个 XmlElement。
     *
     * @param node  the Node | Node
     * @param xpath the XPath expression | XPath 表达式
     * @return the result element, or null if not found | 结果元素，如果未找到则返回 null
     */
    public static XmlElement selectElement(Node node, String xpath) {
        Node result = selectNode(node, xpath);
        if (result instanceof Element elem) {
            return XmlElement.of(elem);
        }
        return null;
    }

    /**
     * Selects XmlElements using XPath.
     * 使用 XPath 选择 XmlElement 列表。
     *
     * @param xml   the XML string | XML 字符串
     * @param xpath the XPath expression | XPath 表达式
     * @return the list of elements | 元素列表
     */
    public static List<XmlElement> selectElements(String xml, String xpath) {
        return selectElements(DomParser.parse(xml), xpath);
    }

    /**
     * Selects XmlElements from a Document.
     * 从 Document 选择 XmlElement 列表。
     *
     * @param document the Document | Document
     * @param xpath    the XPath expression | XPath 表达式
     * @return the list of elements | 元素列表
     */
    public static List<XmlElement> selectElements(Document document, String xpath) {
        return selectElements((Node) document, xpath);
    }

    /**
     * Selects XmlElements from a Node.
     * 从 Node 选择 XmlElement 列表。
     *
     * @param node  the Node | Node
     * @param xpath the XPath expression | XPath 表达式
     * @return the list of elements | 元素列表
     */
    public static List<XmlElement> selectElements(Node node, String xpath) {
        NodeList nodeList = selectNodes(node, xpath);
        List<XmlElement> result = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(i);
            if (n instanceof Element elem) {
                result.add(XmlElement.of(elem));
            }
        }
        return result;
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Checks if a node matching the XPath exists.
     * 检查是否存在匹配 XPath 的节点。
     *
     * @param xml   the XML string | XML 字符串
     * @param xpath the XPath expression | XPath 表达式
     * @return true if exists | 如果存在则返回 true
     */
    public static boolean exists(String xml, String xpath) {
        return selectNode(xml, xpath) != null;
    }

    /**
     * Counts nodes matching the XPath.
     * 统计匹配 XPath 的节点数量。
     *
     * @param xml   the XML string | XML 字符串
     * @param xpath the XPath expression | XPath 表达式
     * @return the count | 数量
     */
    public static int count(String xml, String xpath) {
        return selectNodes(xml, xpath).getLength();
    }

    /**
     * Creates an XPath query with namespaces.
     * 创建带命名空间的 XPath 查询。
     *
     * @param namespaces the namespace map (prefix -> URI) | 命名空间映射（前缀 -> URI）
     * @return the XPath query object | XPath 查询对象
     */
    public static XPathQuery withNamespaces(Map<String, String> namespaces) {
        return XPathQuery.create().namespaces(namespaces);
    }
}
