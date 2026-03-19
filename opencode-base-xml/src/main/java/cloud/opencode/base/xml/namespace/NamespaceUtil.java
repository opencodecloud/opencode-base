package cloud.opencode.base.xml.namespace;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.XmlElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import java.util.HashMap;
import java.util.Map;

/**
 * Namespace Utility - Utilities for XML namespace operations
 * 命名空间工具 - XML 命名空间操作的工具类
 *
 * <p>This class provides utility methods for working with XML namespaces.</p>
 * <p>此类提供处理 XML 命名空间的工具方法。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Utility methods for XML namespace operations - XML 命名空间操作的工具方法</li>
 *   <li>Namespace extraction from DOM documents - 从 DOM 文档提取命名空间</li>
 *   <li>Common namespace URI constants - 常用命名空间 URI 常量</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Extract namespaces from document
 * Map<String, String> namespaces = NamespaceUtil.extractNamespaces(document);
 * 
 * // Get namespace URI for prefix
 * String uri = NamespaceUtil.getNamespaceURI(element, "soap");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: No (throws on null document) - 空值安全: 否（null 文档抛异常）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(d×a) for extractNamespaces where d=ancestor depth and a=attributes per element; O(n) for string operations (getLocalPart, getPrefix, createQName) where n=string length; O(1) for namespace checks - 时间复杂度: extractNamespaces 为 O(d×a)，d 为祖先深度，a 为每个元素的属性数；字符串操作 O(n)，n 为字符串长度；命名空间检查为 O(1)</li>
 *   <li>Space complexity: O(d) for namespace map entries from ancestor chain - 空间复杂度: 祖先链的命名空间 map 条目为 O(d)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class NamespaceUtil {

    /**
     * Common XML namespace URIs.
     * 常用 XML 命名空间 URI。
     */
    public static final String XMLNS_URI = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
    public static final String XML_URI = XMLConstants.XML_NS_URI;
    public static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String XSD_URI = "http://www.w3.org/2001/XMLSchema";
    public static final String SOAP_ENV_URI = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String SOAP12_ENV_URI = "http://www.w3.org/2003/05/soap-envelope";

    private NamespaceUtil() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    // ==================== Namespace Extraction | 命名空间提取 ====================

    /**
     * Extracts all namespace declarations from a document.
     * 从文档中提取所有命名空间声明。
     *
     * @param document the document | 文档
     * @return map of prefix to namespace URI | 前缀到命名空间 URI 的映射
     */
    public static Map<String, String> extractNamespaces(XmlDocument document) {
        if (document == null || document.getRoot() == null) {
            return Map.of();
        }
        return extractNamespaces(document.getRoot());
    }

    /**
     * Extracts all namespace declarations from an element and its ancestors.
     * 从元素及其祖先中提取所有命名空间声明。
     *
     * @param element the element | 元素
     * @return map of prefix to namespace URI | 前缀到命名空间 URI 的映射
     */
    public static Map<String, String> extractNamespaces(XmlElement element) {
        Map<String, String> namespaces = new HashMap<>();
        extractNamespacesRecursive(element.getElement(), namespaces);
        return namespaces;
    }

    /**
     * Extracts namespace declarations from a DOM element and its ancestors.
     * 从 DOM 元素及其祖先中提取命名空间声明。
     *
     * @param element the DOM element | DOM 元素
     * @return map of prefix to namespace URI | 前缀到命名空间 URI 的映射
     */
    public static Map<String, String> extractNamespaces(Element element) {
        Map<String, String> namespaces = new HashMap<>();
        extractNamespacesRecursive(element, namespaces);
        return namespaces;
    }

    private static void extractNamespacesRecursive(Node node, Map<String, String> namespaces) {
        if (node == null || node.getNodeType() != Node.ELEMENT_NODE) {
            return;
        }

        // Extract from parent first (child declarations override parent)
        extractNamespacesRecursive(node.getParentNode(), namespaces);

        // Extract from current element
        NamedNodeMap attrs = node.getAttributes();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attr = attrs.item(i);
                String name = attr.getNodeName();

                if ("xmlns".equals(name)) {
                    // Default namespace
                    namespaces.put("", attr.getNodeValue());
                } else if (name.startsWith("xmlns:")) {
                    // Prefixed namespace
                    String prefix = name.substring(6);
                    namespaces.put(prefix, attr.getNodeValue());
                }
            }
        }
    }

    // ==================== Namespace Context Creation | 命名空间上下文创建 ====================

    /**
     * Creates a namespace context from a document.
     * 从文档创建命名空间上下文。
     *
     * @param document the document | 文档
     * @return a namespace context | 命名空间上下文
     */
    public static OpenNamespaceContext createContext(XmlDocument document) {
        Map<String, String> namespaces = extractNamespaces(document);
        return createContextFromMap(namespaces);
    }

    /**
     * Creates a namespace context from an element.
     * 从元素创建命名空间上下文。
     *
     * @param element the element | 元素
     * @return a namespace context | 命名空间上下文
     */
    public static OpenNamespaceContext createContext(XmlElement element) {
        Map<String, String> namespaces = extractNamespaces(element);
        return createContextFromMap(namespaces);
    }

    /**
     * Creates a namespace context from a map.
     * 从映射创建命名空间上下文。
     *
     * @param namespaces the namespace map | 命名空间映射
     * @return a namespace context | 命名空间上下文
     */
    public static OpenNamespaceContext createContextFromMap(Map<String, String> namespaces) {
        OpenNamespaceContext ctx = OpenNamespaceContext.create();
        namespaces.forEach((prefix, uri) -> {
            if (prefix.isEmpty()) {
                ctx.setDefaultNamespace(uri);
            } else {
                ctx.bind(prefix, uri);
            }
        });
        return ctx;
    }

    // ==================== QName Operations | 限定名操作 ====================

    /**
     * Gets the local part of a qualified name.
     * 获取限定名的本地部分。
     *
     * @param qName the qualified name (e.g., "prefix:localName") | 限定名
     * @return the local part | 本地部分
     */
    public static String getLocalPart(String qName) {
        if (qName == null) {
            return null;
        }
        int colonIndex = qName.indexOf(':');
        return colonIndex >= 0 ? qName.substring(colonIndex + 1) : qName;
    }

    /**
     * Gets the prefix of a qualified name.
     * 获取限定名的前缀。
     *
     * @param qName the qualified name (e.g., "prefix:localName") | 限定名
     * @return the prefix, or empty string if no prefix | 前缀，如果没有前缀则返回空字符串
     */
    public static String getPrefix(String qName) {
        if (qName == null) {
            return "";
        }
        int colonIndex = qName.indexOf(':');
        return colonIndex >= 0 ? qName.substring(0, colonIndex) : "";
    }

    /**
     * Creates a qualified name from prefix and local part.
     * 从前缀和本地部分创建限定名。
     *
     * @param prefix    the prefix (can be null or empty) | 前缀（可以为 null 或空）
     * @param localPart the local part | 本地部分
     * @return the qualified name | 限定名
     */
    public static String createQName(String prefix, String localPart) {
        if (prefix == null || prefix.isEmpty()) {
            return localPart;
        }
        return prefix + ":" + localPart;
    }

    /**
     * Checks if a name is a qualified name (has a prefix).
     * 检查名称是否是限定名（具有前缀）。
     *
     * @param name the name | 名称
     * @return true if qualified | 如果是限定名则返回 true
     */
    public static boolean isQualifiedName(String name) {
        return name != null && name.contains(":");
    }

    // ==================== Namespace Declaration | 命名空间声明 ====================

    /**
     * Adds a namespace declaration to an element.
     * 向元素添加命名空间声明。
     *
     * @param element      the element | 元素
     * @param prefix       the prefix | 前缀
     * @param namespaceUri the namespace URI | 命名空间 URI
     */
    public static void declareNamespace(XmlElement element, String prefix, String namespaceUri) {
        Element domElement = element.getElement();
        if (prefix == null || prefix.isEmpty()) {
            domElement.setAttribute("xmlns", namespaceUri);
        } else {
            domElement.setAttributeNS(XMLNS_URI, "xmlns:" + prefix, namespaceUri);
        }
    }

    /**
     * Adds a default namespace declaration to an element.
     * 向元素添加默认命名空间声明。
     *
     * @param element      the element | 元素
     * @param namespaceUri the namespace URI | 命名空间 URI
     */
    public static void declareDefaultNamespace(XmlElement element, String namespaceUri) {
        declareNamespace(element, null, namespaceUri);
    }

    /**
     * Removes a namespace declaration from an element.
     * 从元素中移除命名空间声明。
     *
     * @param element the element | 元素
     * @param prefix  the prefix to remove | 要移除的前缀
     */
    public static void removeNamespaceDeclaration(XmlElement element, String prefix) {
        Element domElement = element.getElement();
        if (prefix == null || prefix.isEmpty()) {
            domElement.removeAttribute("xmlns");
        } else {
            domElement.removeAttributeNS(XMLNS_URI, "xmlns:" + prefix);
        }
    }

    // ==================== Namespace Checking | 命名空间检查 ====================

    /**
     * Gets the namespace URI of an element.
     * 获取元素的命名空间 URI。
     *
     * @param element the element | 元素
     * @return the namespace URI, or null | 命名空间 URI，或 null
     */
    public static String getNamespaceUri(XmlElement element) {
        return element.getElement().getNamespaceURI();
    }

    /**
     * Gets the namespace prefix of an element.
     * 获取元素的命名空间前缀。
     *
     * @param element the element | 元素
     * @return the prefix, or null | 前缀，或 null
     */
    public static String getNamespacePrefix(XmlElement element) {
        return element.getElement().getPrefix();
    }

    /**
     * Checks if an element is in a specific namespace.
     * 检查元素是否在特定命名空间中。
     *
     * @param element      the element | 元素
     * @param namespaceUri the namespace URI | 命名空间 URI
     * @return true if in the namespace | 如果在命名空间中则返回 true
     */
    public static boolean isInNamespace(XmlElement element, String namespaceUri) {
        String elementNs = getNamespaceUri(element);
        if (namespaceUri == null) {
            return elementNs == null || elementNs.isEmpty();
        }
        return namespaceUri.equals(elementNs);
    }

    /**
     * Checks if an element has no namespace.
     * 检查元素是否没有命名空间。
     *
     * @param element the element | 元素
     * @return true if no namespace | 如果没有命名空间则返回 true
     */
    public static boolean hasNoNamespace(XmlElement element) {
        String ns = getNamespaceUri(element);
        return ns == null || ns.isEmpty();
    }

    // ==================== Standard Namespace Contexts | 标准命名空间上下文 ====================

    /**
     * Creates a namespace context for SOAP 1.1.
     * 创建 SOAP 1.1 的命名空间上下文。
     *
     * @return a namespace context | 命名空间上下文
     */
    public static OpenNamespaceContext soapContext() {
        return OpenNamespaceContext.create()
            .bind("soap", SOAP_ENV_URI)
            .bind("xsi", XSI_URI)
            .bind("xsd", XSD_URI);
    }

    /**
     * Creates a namespace context for SOAP 1.2.
     * 创建 SOAP 1.2 的命名空间上下文。
     *
     * @return a namespace context | 命名空间上下文
     */
    public static OpenNamespaceContext soap12Context() {
        return OpenNamespaceContext.create()
            .bind("soap", SOAP12_ENV_URI)
            .bind("xsi", XSI_URI)
            .bind("xsd", XSD_URI);
    }

    /**
     * Creates a namespace context for XML Schema.
     * 创建 XML Schema 的命名空间上下文。
     *
     * @return a namespace context | 命名空间上下文
     */
    public static OpenNamespaceContext xsdContext() {
        return OpenNamespaceContext.create()
            .bind("xsd", XSD_URI)
            .bind("xsi", XSI_URI);
    }
}
