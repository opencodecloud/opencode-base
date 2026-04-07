package cloud.opencode.base.xml.merge;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.exception.OpenXmlException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * XML Merge - Merges two XML documents by overlaying one onto another
 * XML 合并 - 通过将一个 XML 文档覆盖到另一个上来合并两个 XML 文档
 *
 * <p>This utility class provides methods for merging XML documents. The overlay document's
 * elements are merged onto the base document. Matching elements (by tag name) have their
 * text content and attributes replaced; non-matching elements from the overlay are appended.</p>
 * <p>此工具类提供合并 XML 文档的方法。覆盖文档的元素合并到基础文档上。
 * 匹配的元素（按标签名）的文本内容和属性将被替换；覆盖文档中不匹配的元素将被追加。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Merge XmlDocument objects or XML strings - 合并 XmlDocument 对象或 XML 字符串</li>
 *   <li>Recursive element merging by tag name - 按标签名递归合并元素</li>
 *   <li>Attribute overlay - 属性覆盖</li>
 *   <li>Configurable merge strategy - 可配置的合并策略</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Merge two XML documents
 * XmlDocument merged = XmlMerge.merge(base, overlay);
 *
 * // Merge XML strings
 * String merged = XmlMerge.merge(baseXml, overlayXml);
 *
 * // Merge with strategy
 * XmlDocument merged = XmlMerge.merge(base, overlay, MergeStrategy.APPEND);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n * m) where n and m are node counts - 时间复杂度: O(n * m)，n 和 m 为节点数</li>
 *   <li>Space complexity: O(n + m) for the merged document - 空间复杂度: O(n + m)，用于合并后的文档</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: No (null inputs throw NullPointerException) - 空值安全: 否（null 输入抛出 NullPointerException）</li>
 *   <li>Secure parsing via SecureParserFactory - 通过 SecureParserFactory 安全解析</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
public final class XmlMerge {

    /**
     * Maximum recursion depth to prevent stack overflow on malicious input.
     * 最大递归深度，防止恶意输入导致栈溢出。
     */
    private static final int MAX_DEPTH = 512;

    private XmlMerge() {
        // Utility class
    }

    /**
     * Merges an overlay document onto a base document using OVERRIDE strategy.
     * 使用 OVERRIDE 策略将覆盖文档合并到基础文档上。
     *
     * @param base    the base document | 基础文档
     * @param overlay the overlay document | 覆盖文档
     * @return a new merged document | 新的合并文档
     */
    public static XmlDocument merge(XmlDocument base, XmlDocument overlay) {
        return merge(base, overlay, MergeStrategy.OVERRIDE);
    }

    /**
     * Merges an overlay document onto a base document with the specified strategy.
     * 使用指定策略将覆盖文档合并到基础文档上。
     *
     * @param base     the base document | 基础文档
     * @param overlay  the overlay document | 覆盖文档
     * @param strategy the merge strategy | 合并策略
     * @return a new merged document | 新的合并文档
     */
    public static XmlDocument merge(XmlDocument base, XmlDocument overlay, MergeStrategy strategy) {
        Objects.requireNonNull(base, "Base document must not be null");
        Objects.requireNonNull(overlay, "Overlay document must not be null");
        Objects.requireNonNull(strategy, "Merge strategy must not be null");

        try {
            // Clone the base document to avoid modifying it
            Document result = (Document) base.getDocument().cloneNode(true);
            Document overlayDoc = overlay.getDocument();

            Element baseRoot = result.getDocumentElement();
            Element overlayRoot = overlayDoc.getDocumentElement();

            if (baseRoot == null || overlayRoot == null) {
                return XmlDocument.of(result);
            }

            if (!baseRoot.getTagName().equals(overlayRoot.getTagName())) {
                throw new OpenXmlException("Root element mismatch: base='" + baseRoot.getTagName()
                    + "', overlay='" + overlayRoot.getTagName() + "'");
            }

            mergeElements(result, baseRoot, overlayRoot, strategy, 0);
            return XmlDocument.of(result);
        } catch (OpenXmlException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenXmlException("Failed to merge XML documents", e);
        }
    }

    /**
     * Merges two XML strings using OVERRIDE strategy and returns the merged XML string.
     * 使用 OVERRIDE 策略合并两个 XML 字符串并返回合并后的 XML 字符串。
     *
     * @param baseXml    the base XML string | 基础 XML 字符串
     * @param overlayXml the overlay XML string | 覆盖 XML 字符串
     * @return the merged XML string | 合并后的 XML 字符串
     */
    public static String merge(String baseXml, String overlayXml) {
        return merge(baseXml, overlayXml, MergeStrategy.OVERRIDE);
    }

    /**
     * Merges two XML strings using the specified strategy and returns the merged XML string.
     * 使用指定策略合并两个 XML 字符串并返回合并后的 XML 字符串。
     *
     * @param baseXml    the base XML string | 基础 XML 字符串
     * @param overlayXml the overlay XML string | 覆盖 XML 字符串
     * @param strategy   the merge strategy | 合并策略
     * @return the merged XML string | 合并后的 XML 字符串
     */
    public static String merge(String baseXml, String overlayXml, MergeStrategy strategy) {
        Objects.requireNonNull(baseXml, "Base XML must not be null");
        Objects.requireNonNull(overlayXml, "Overlay XML must not be null");
        Objects.requireNonNull(strategy, "Merge strategy must not be null");

        XmlDocument base = XmlDocument.parse(baseXml);
        XmlDocument overlay = XmlDocument.parse(overlayXml);
        XmlDocument merged = merge(base, overlay, strategy);
        return merged.toXml();
    }

    // ==================== Internal | 内部实现 ====================

    /**
     * Recursively merges overlay elements onto base elements.
     * 递归地将覆盖元素合并到基础元素上。
     */
    private static void mergeElements(Document doc, Element base, Element overlay, MergeStrategy strategy, int depth) {
        if (depth > MAX_DEPTH) {
            throw new OpenXmlException("XML merge exceeded maximum depth " + MAX_DEPTH);
        }
        // Merge attributes from overlay to base
        mergeAttributes(base, overlay, strategy);

        // Index base children by tag name for O(1) lookup instead of O(n) per overlay child
        Map<String, Element> baseChildIndex = indexChildElements(base);

        // Process overlay children
        NodeList overlayChildren = overlay.getChildNodes();
        for (int i = 0; i < overlayChildren.getLength(); i++) {
            Node overlayChild = overlayChildren.item(i);
            if (overlayChild.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element overlayElement = (Element) overlayChild;
            String tagName = overlayElement.getTagName();

            // O(1) lookup instead of O(n) linear search
            Element baseMatch = baseChildIndex.get(tagName);

            switch (strategy) {
                case OVERRIDE -> {
                    if (baseMatch != null) {
                        // Recursively merge
                        mergeElements(doc, baseMatch, overlayElement, strategy, depth + 1);
                        // Update direct text content
                        mergeTextContent(baseMatch, overlayElement);
                    } else {
                        // Append new element
                        Node imported = doc.importNode(overlayElement, true);
                        base.appendChild(imported);
                    }
                }
                case APPEND -> {
                    // Always append
                    Node imported = doc.importNode(overlayElement, true);
                    base.appendChild(imported);
                }
                case SKIP_EXISTING -> {
                    if (baseMatch == null) {
                        // Only add if not present
                        Node imported = doc.importNode(overlayElement, true);
                        base.appendChild(imported);
                    }
                }
            }
        }
    }

    /**
     * Merges attributes from the overlay element onto the base element.
     * 将覆盖元素的属性合并到基础元素上。
     */
    private static void mergeAttributes(Element base, Element overlay, MergeStrategy strategy) {
        NamedNodeMap attrs = overlay.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            String name = attr.getNodeName();
            if (strategy == MergeStrategy.SKIP_EXISTING && base.hasAttribute(name)) {
                continue;
            }
            base.setAttribute(name, attr.getNodeValue());
        }
    }

    /**
     * Merges direct text content from overlay to base (if overlay has direct text).
     * 将覆盖元素的直接文本内容合并到基础元素（如果覆盖元素有直接文本）。
     */
    private static void mergeTextContent(Element base, Element overlay) {
        String overlayText = getDirectText(overlay);
        if (overlayText != null) {
            // Remove existing text nodes from base
            NodeList children = base.getChildNodes();
            for (int i = children.getLength() - 1; i >= 0; i--) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.TEXT_NODE) {
                    base.removeChild(child);
                }
            }
            // Add overlay text
            if (!overlayText.isEmpty()) {
                base.insertBefore(base.getOwnerDocument().createTextNode(overlayText), base.getFirstChild());
            }
        }
    }

    /**
     * Gets the direct text content (not from child elements) of an element.
     * 获取元素的直接文本内容（不包括子元素的文本）。
     */
    private static String getDirectText(Element element) {
        StringBuilder sb = new StringBuilder();
        boolean hasText = false;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                sb.append(child.getNodeValue());
                hasText = true;
            }
        }
        if (!hasText) {
            return null;
        }
        String text = sb.toString().trim();
        return text.isEmpty() ? null : text;
    }

    /**
     * Indexes direct child elements by tag name (first occurrence wins).
     * 按标签名索引直接子元素（首次出现的优先）。
     */
    private static Map<String, Element> indexChildElements(Element parent) {
        Map<String, Element> index = new HashMap<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                index.putIfAbsent(child.getNodeName(), (Element) child);
            }
        }
        return index;
    }
}
