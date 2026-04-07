package cloud.opencode.base.xml.canonical;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.dom.DomParser;
import cloud.opencode.base.xml.exception.OpenXmlException;
import cloud.opencode.base.xml.security.SecureParserFactory;
import org.w3c.dom.*;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.*;

/**
 * XML Canonicalizer - Produces canonical XML output (C14N)
 * XML 规范化器 - 生成规范化 XML 输出（C14N）
 *
 * <p>This utility class canonicalizes XML documents to ensure consistent
 * serialization regardless of input formatting. The canonicalization process
 * includes attribute sorting, whitespace normalization, and optional comment removal.</p>
 * <p>此工具类对 XML 文档进行规范化，确保无论输入格式如何都能产生一致的序列化结果。
 * 规范化过程包括属性排序、空白规范化和可选的注释移除。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Attribute alphabetical reordering - 属性字母排序</li>
 *   <li>Namespace declaration normalization - 命名空间声明规范化</li>
 *   <li>UTF-8 encoding enforcement - 强制 UTF-8 编码</li>
 *   <li>XML declaration removal - 移除 XML 声明</li>
 *   <li>Whitespace normalization between elements - 元素间空白规范化</li>
 *   <li>Optional comment removal - 可选注释移除</li>
 *   <li>Consistent output across multiple calls - 多次调用产生一致输出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Canonicalize XML string
 * String canonical = XmlCanonicalizer.canonicalize("<root b='2' a='1'/>");
 * // Result: <root a="1" b="2"/>
 *
 * // Canonicalize with comment removal
 * String canonical = XmlCanonicalizer.canonicalize(xml, true);
 *
 * // Canonicalize XmlDocument
 * XmlDocument doc = XmlDocument.parse(xml);
 * String canonical = XmlCanonicalizer.canonicalize(doc);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n log n) due to attribute sorting, where n = total nodes/attributes - 时间复杂度: O(n log n)，由于属性排序，n 为总节点/属性数</li>
 *   <li>Space complexity: O(n) for the DOM tree - 空间复杂度: O(n)，用于 DOM 树</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility, uses secure parser) - 线程安全: 是（无状态工具，使用安全解析器）</li>
 *   <li>Null-safe: No (null inputs throw NullPointerException) - 空值安全: 否（null 输入抛出 NullPointerException）</li>
 *   <li>Secure parsing with XXE protection - 安全解析，带 XXE 防护</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
public final class XmlCanonicalizer {

    /**
     * Maximum recursion depth to prevent stack overflow on malicious input.
     * 最大递归深度，防止恶意输入导致栈溢出。
     */
    private static final int MAX_DEPTH = 512;

    private XmlCanonicalizer() {
        // Utility class
    }

    /**
     * Canonicalizes the given XML string.
     * 规范化给定的 XML 字符串。
     *
     * <p>Comments are preserved by default.</p>
     * <p>默认保留注释。</p>
     *
     * @param xml the XML string | XML 字符串
     * @return the canonical XML string | 规范化的 XML 字符串
     * @throws OpenXmlException if parsing or transformation fails | 如果解析或转换失败则抛出异常
     */
    public static String canonicalize(String xml) {
        return canonicalize(xml, false);
    }

    /**
     * Canonicalizes the given XmlDocument.
     * 规范化给定的 XmlDocument。
     *
     * <p>Comments are preserved by default.</p>
     * <p>默认保留注释。</p>
     *
     * @param doc the XML document | XML 文档
     * @return the canonical XML string | 规范化的 XML 字符串
     * @throws OpenXmlException if transformation fails | 如果转换失败则抛出异常
     */
    public static String canonicalize(XmlDocument doc) {
        return canonicalize(doc, false);
    }

    /**
     * Canonicalizes the given XML string with optional comment removal.
     * 规范化给定的 XML 字符串，可选择移除注释。
     *
     * @param xml            the XML string | XML 字符串
     * @param removeComments whether to remove comments | 是否移除注释
     * @return the canonical XML string | 规范化的 XML 字符串
     * @throws OpenXmlException if parsing or transformation fails | 如果解析或转换失败则抛出异常
     */
    public static String canonicalize(String xml, boolean removeComments) {
        Objects.requireNonNull(xml, "XML string must not be null");
        Document document = DomParser.parse(xml);
        return canonicalizeDocument(document, removeComments);
    }

    /**
     * Canonicalizes the given XmlDocument with optional comment removal.
     * 规范化给定的 XmlDocument，可选择移除注释。
     *
     * @param doc            the XML document | XML 文档
     * @param removeComments whether to remove comments | 是否移除注释
     * @return the canonical XML string | 规范化的 XML 字符串
     * @throws OpenXmlException if transformation fails | 如果转换失败则抛出异常
     */
    public static String canonicalize(XmlDocument doc, boolean removeComments) {
        Objects.requireNonNull(doc, "Document must not be null");
        Document document = doc.getDocument();
        // Clone to avoid modifying the original
        Document clone = (Document) document.cloneNode(true);
        return canonicalizeDocument(clone, removeComments);
    }

    // ==================== Internal | 内部实现 ====================

    /**
     * Performs canonicalization on a DOM Document.
     * 对 DOM Document 执行规范化。
     */
    private static String canonicalizeDocument(Document document, boolean removeComments) {
        try {
            // 1. Normalize the document
            document.normalizeDocument();

            // 2. Remove comments if requested
            if (removeComments) {
                removeComments(document, 0);
            }

            // 3. Normalize whitespace between elements
            normalizeWhitespace(document, 0);

            // 4. Sort attributes alphabetically per element
            sortAttributes(document, 0);

            // 5. Serialize with OMIT_XML_DECLARATION
            return serialize(document);
        } catch (OpenXmlException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenXmlException("Failed to canonicalize XML", e);
        }
    }

    /**
     * Removes all comment nodes from the document tree.
     * 从文档树中移除所有注释节点。
     */
    private static void removeComments(Node node, int depth) {
        if (depth > MAX_DEPTH) {
            return;
        }
        List<Node> toRemove = new ArrayList<>();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.COMMENT_NODE) {
                toRemove.add(child);
            } else {
                removeComments(child, depth + 1);
            }
        }
        for (Node n : toRemove) {
            n.getParentNode().removeChild(n);
        }
    }

    /**
     * Normalizes whitespace by removing pure-whitespace text nodes between elements.
     * 通过移除元素间的纯空白文本节点来规范化空白。
     */
    private static void normalizeWhitespace(Node node, int depth) {
        if (depth > MAX_DEPTH) {
            return;
        }
        List<Node> toRemove = new ArrayList<>();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                String text = child.getTextContent();
                if (text != null && text.isBlank() && hasSiblingElements(node)) {
                    toRemove.add(child);
                }
            } else {
                normalizeWhitespace(child, depth + 1);
            }
        }
        for (Node n : toRemove) {
            n.getParentNode().removeChild(n);
        }
    }

    /**
     * Checks whether a node has any child elements (not just text).
     * 检查节点是否有子元素（不仅仅是文本）。
     */
    private static boolean hasSiblingElements(Node parent) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sorts attributes alphabetically on all elements in the tree.
     * 对树中所有元素的属性按字母顺序排序。
     */
    private static void sortAttributes(Node node, int depth) {
        if (depth > MAX_DEPTH) {
            return;
        }
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            NamedNodeMap attrs = element.getAttributes();
            if (attrs != null && attrs.getLength() > 1) {
                // Collect attributes
                List<Attr> attrList = new ArrayList<>(attrs.getLength());
                for (int i = 0; i < attrs.getLength(); i++) {
                    attrList.add((Attr) attrs.item(i));
                }

                // Sort: namespace declarations first, then alphabetical by name
                attrList.sort((a, b) -> {
                    boolean aIsNs = isNamespaceDeclaration(a);
                    boolean bIsNs = isNamespaceDeclaration(b);
                    if (aIsNs && !bIsNs) return -1;
                    if (!aIsNs && bIsNs) return 1;
                    return a.getName().compareTo(b.getName());
                });

                // Remove all and re-add in sorted order
                for (Attr attr : attrList) {
                    element.removeAttributeNode(attr);
                }
                for (Attr attr : attrList) {
                    element.setAttributeNode(attr);
                }
            }
        }

        // Recurse into children
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            sortAttributes(children.item(i), depth + 1);
        }
    }

    /**
     * Checks whether an attribute is a namespace declaration.
     * 检查属性是否为命名空间声明。
     */
    private static boolean isNamespaceDeclaration(Attr attr) {
        String name = attr.getName();
        return "xmlns".equals(name) || name.startsWith("xmlns:");
    }

    /**
     * Serializes the document to a canonical XML string.
     * 将文档序列化为规范化 XML 字符串。
     */
    private static String serialize(Document document) {
        try {
            Transformer transformer = SecureParserFactory.createTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            throw new OpenXmlException("Failed to serialize canonical XML", e);
        }
    }
}
