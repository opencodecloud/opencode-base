package cloud.opencode.base.xml.diff;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.dom.DomParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * XML Diff - Compares two XML documents and produces a list of differences
 * XML 差异比较 - 比较两个 XML 文档并生成差异列表
 *
 * <p>This utility class provides methods for comparing XML documents structurally,
 * detecting added, removed, and modified elements, attributes, and text content.</p>
 * <p>此工具类提供结构化比较 XML 文档的方法，检测新增、删除和修改的元素、属性和文本内容。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Compare XML strings or XmlDocument objects - 比较 XML 字符串或 XmlDocument 对象</li>
 *   <li>Detect element, attribute, and text changes - 检测元素、属性和文本变更</li>
 *   <li>XPath-like path output for each difference - 为每个差异生成类 XPath 路径</li>
 *   <li>Equality check for quick comparison - 快速相等性检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Compare two XML strings
 * List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);
 * for (DiffEntry entry : diffs) {
 *     System.out.println(entry.path() + " " + entry.type());
 * }
 *
 * // Check equality
 * boolean equal = XmlDiff.isEqual(xml1, xml2);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n is the total number of nodes - 时间复杂度: O(n)，n 为节点总数</li>
 *   <li>Space complexity: O(n) for the diff result list - 空间复杂度: O(n)，用于差异结果列表</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: No (throws on null input) - 空值安全: 否（null 输入抛异常）</li>
 *   <li>Secure parsing via DomParser (XXE protection) - 通过 DomParser 安全解析（XXE 防护）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
public final class XmlDiff {

    /**
     * Maximum recursion depth to prevent stack overflow on malicious input.
     * 最大递归深度，防止恶意输入导致栈溢出。
     */
    private static final int MAX_DEPTH = 512;

    private XmlDiff() {
        // Utility class
    }

    /**
     * Compares two XML strings and returns a list of differences.
     * 比较两个 XML 字符串并返回差异列表。
     *
     * @param xml1 the first XML string | 第一个 XML 字符串
     * @param xml2 the second XML string | 第二个 XML 字符串
     * @return the list of differences | 差异列表
     * @throws cloud.opencode.base.xml.exception.OpenXmlException if parsing fails | 如果解析失败则抛出异常
     */
    public static List<DiffEntry> diff(String xml1, String xml2) {
        Objects.requireNonNull(xml1, "xml1 must not be null");
        Objects.requireNonNull(xml2, "xml2 must not be null");
        Document doc1 = DomParser.parse(xml1);
        Document doc2 = DomParser.parse(xml2);
        return diffDocuments(doc1, doc2);
    }

    /**
     * Compares two XmlDocument objects and returns a list of differences.
     * 比较两个 XmlDocument 对象并返回差异列表。
     *
     * @param doc1 the first document | 第一个文档
     * @param doc2 the second document | 第二个文档
     * @return the list of differences | 差异列表
     */
    public static List<DiffEntry> diff(XmlDocument doc1, XmlDocument doc2) {
        Objects.requireNonNull(doc1, "doc1 must not be null");
        Objects.requireNonNull(doc2, "doc2 must not be null");
        return diffDocuments(doc1.getDocument(), doc2.getDocument());
    }

    /**
     * Checks whether two XML strings are structurally equal.
     * 检查两个 XML 字符串是否结构相等。
     *
     * @param xml1 the first XML string | 第一个 XML 字符串
     * @param xml2 the second XML string | 第二个 XML 字符串
     * @return true if equal | 如果相等则返回 true
     * @throws cloud.opencode.base.xml.exception.OpenXmlException if parsing fails | 如果解析失败则抛出异常
     */
    public static boolean isEqual(String xml1, String xml2) {
        return diff(xml1, xml2).isEmpty();
    }

    /**
     * Checks whether two XmlDocument objects are structurally equal.
     * 检查两个 XmlDocument 对象是否结构相等。
     *
     * @param doc1 the first document | 第一个文档
     * @param doc2 the second document | 第二个文档
     * @return true if equal | 如果相等则返回 true
     */
    public static boolean isEqual(XmlDocument doc1, XmlDocument doc2) {
        return diff(doc1, doc2).isEmpty();
    }

    // ==================== Internal Implementation | 内部实现 ====================

    private static List<DiffEntry> diffDocuments(Document doc1, Document doc2) {
        List<DiffEntry> diffs = new ArrayList<>();
        Element root1 = doc1.getDocumentElement();
        Element root2 = doc2.getDocumentElement();

        if (root1 == null && root2 == null) {
            return diffs;
        }
        if (root1 == null) {
            diffs.add(new DiffEntry("/" + root2.getTagName(), DiffType.ADDED, null, root2.getTagName()));
            return diffs;
        }
        if (root2 == null) {
            diffs.add(new DiffEntry("/" + root1.getTagName(), DiffType.REMOVED, root1.getTagName(), null));
            return diffs;
        }

        if (!root1.getTagName().equals(root2.getTagName())) {
            diffs.add(new DiffEntry("/", DiffType.MODIFIED, root1.getTagName(), root2.getTagName()));
            return diffs;
        }

        String rootPath = "/" + root1.getTagName();
        diffElements(root1, root2, rootPath, diffs, 0);
        return diffs;
    }

    private static void diffElements(Element e1, Element e2, String path, List<DiffEntry> diffs, int depth) {
        if (depth > MAX_DEPTH) {
            throw new cloud.opencode.base.xml.exception.OpenXmlException(
                "XML diff exceeded maximum depth " + MAX_DEPTH + " at path: " + path);
        }
        // Compare attributes
        diffAttributes(e1, e2, path, diffs);

        // Compare text content (direct text, not from children)
        String text1 = getDirectTextContent(e1);
        String text2 = getDirectTextContent(e2);
        if (!Objects.equals(text1, text2)) {
            diffs.add(new DiffEntry(path, DiffType.TEXT_MODIFIED, text1, text2));
        }

        // Compare child elements
        List<Element> children1 = getChildElements(e1);
        List<Element> children2 = getChildElements(e2);

        // Group children by tag name for positional matching
        Map<String, List<Element>> grouped1 = groupByTagName(children1);
        Map<String, List<Element>> grouped2 = groupByTagName(children2);

        // All tag names from both
        Set<String> allTags = new LinkedHashSet<>(grouped1.keySet());
        allTags.addAll(grouped2.keySet());

        for (String tag : allTags) {
            List<Element> list1 = grouped1.getOrDefault(tag, List.of());
            List<Element> list2 = grouped2.getOrDefault(tag, List.of());

            int maxSize = Math.max(list1.size(), list2.size());
            for (int i = 0; i < maxSize; i++) {
                String childPath = path + "/" + tag + "[" + i + "]";
                if (i >= list1.size()) {
                    // Added in doc2
                    diffs.add(new DiffEntry(childPath, DiffType.ADDED, null, tag));
                } else if (i >= list2.size()) {
                    // Removed from doc1
                    diffs.add(new DiffEntry(childPath, DiffType.REMOVED, tag, null));
                } else {
                    // Both exist, compare recursively
                    diffElements(list1.get(i), list2.get(i), childPath, diffs, depth + 1);
                }
            }
        }
    }

    private static void diffAttributes(Element e1, Element e2, String path, List<DiffEntry> diffs) {
        NamedNodeMap attrs1 = e1.getAttributes();
        NamedNodeMap attrs2 = e2.getAttributes();

        // Check attributes in e1
        for (int i = 0; i < attrs1.getLength(); i++) {
            Node attr1 = attrs1.item(i);
            String name = attr1.getNodeName();
            String value1 = attr1.getNodeValue();
            String attrPath = path + "/@" + name;

            Node attr2 = attrs2.getNamedItem(name);
            if (attr2 == null) {
                diffs.add(new DiffEntry(attrPath, DiffType.ATTRIBUTE_REMOVED, value1, null));
            } else {
                String value2 = attr2.getNodeValue();
                if (!Objects.equals(value1, value2)) {
                    diffs.add(new DiffEntry(attrPath, DiffType.ATTRIBUTE_MODIFIED, value1, value2));
                }
            }
        }

        // Check attributes only in e2 (added)
        for (int i = 0; i < attrs2.getLength(); i++) {
            Node attr2 = attrs2.item(i);
            String name = attr2.getNodeName();
            if (attrs1.getNamedItem(name) == null) {
                String attrPath = path + "/@" + name;
                diffs.add(new DiffEntry(attrPath, DiffType.ATTRIBUTE_ADDED, null, attr2.getNodeValue()));
            }
        }
    }

    private static String getDirectTextContent(Element element) {
        StringBuilder sb = new StringBuilder();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
                sb.append(child.getNodeValue());
            }
        }
        String text = sb.toString().trim();
        return text.isEmpty() ? null : text;
    }

    private static List<Element> getChildElements(Element parent) {
        List<Element> children = new ArrayList<>();
        NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                children.add((Element) child);
            }
        }
        return children;
    }

    private static Map<String, List<Element>> groupByTagName(List<Element> elements) {
        Map<String, List<Element>> grouped = new LinkedHashMap<>();
        for (Element element : elements) {
            grouped.computeIfAbsent(element.getTagName(), k -> new ArrayList<>()).add(element);
        }
        return grouped;
    }
}
