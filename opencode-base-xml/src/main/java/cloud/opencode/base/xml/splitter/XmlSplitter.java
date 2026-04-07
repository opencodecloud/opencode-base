package cloud.opencode.base.xml.splitter;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.exception.OpenXmlException;
import cloud.opencode.base.xml.security.SecureParserFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * XML Splitter - Splits large XML streams by element name
 * XML 拆分器 - 按元素名称拆分大型 XML 流
 *
 * <p>This utility class provides stream-based XML splitting using StAX parsing.
 * It scans the XML input for elements matching a given name, extracts each matching
 * element (including its full subtree) into a standalone {@link XmlDocument}, and
 * passes it to a callback handler.</p>
 * <p>此工具类使用 StAX 解析提供基于流的 XML 拆分。它扫描 XML 输入以查找与给定名称匹配的元素，
 * 将每个匹配元素（包括其完整子树）提取为独立的 {@link XmlDocument}，并传递给回调处理器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Stream-based splitting with O(1) memory per fragment - 基于流的拆分，每个片段 O(1) 内存</li>
 *   <li>Correct depth tracking for nested elements - 嵌套元素的正确深度跟踪</li>
 *   <li>Multiple input sources: InputStream, Path, String - 多种输入源：输入流、路径、字符串</li>
 *   <li>Indexed splitting with {@link SplitResult} - 带索引的拆分，使用 {@link SplitResult}</li>
 *   <li>Collect-all and count modes - 全部收集和计数模式</li>
 *   <li>Secure parsing via {@link SecureParserFactory} - 通过 {@link SecureParserFactory} 进行安全解析</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Split and process each <item> element
 * XmlSplitter.split(inputStream, "item", doc -> {
 *     String name = doc.xpath("//name/text()");
 *     System.out.println(name);
 * });
 *
 * // Collect all <record> fragments
 * List<XmlDocument> records = XmlSplitter.splitAll(xml, "record");
 *
 * // Count elements without loading into memory
 * int count = XmlSplitter.count(inputStream, "item");
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = input size - 时间复杂度: O(n)，n 为输入大小</li>
 *   <li>Space complexity: O(m) where m = largest matching fragment - 空间复杂度: O(m)，m 为最大匹配片段</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: No (null inputs throw exceptions) - 空值安全: 否（空值输入抛出异常）</li>
 *   <li>XXE protection enabled via SecureParserFactory - 通过 SecureParserFactory 启用 XXE 防护</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
public final class XmlSplitter {

    /**
     * Maximum nesting depth for captured subtrees to prevent stack overflow.
     * 捕获子树的最大嵌套深度，防止栈溢出。
     */
    private static final int MAX_DEPTH = 512;

    private XmlSplitter() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Splits an XML input stream and processes each matching element via callback.
     * 拆分 XML 输入流并通过回调处理每个匹配元素。
     *
     * @param input       the input stream | 输入流
     * @param elementName the element name to match | 要匹配的元素名称
     * @param handler     the callback handler | 回调处理器
     * @throws OpenXmlException if splitting fails | 如果拆分失败则抛出异常
     */
    public static void split(InputStream input, String elementName, Consumer<XmlDocument> handler) {
        Objects.requireNonNull(input, "InputStream must not be null");
        Objects.requireNonNull(elementName, "Element name must not be null");
        Objects.requireNonNull(handler, "Handler must not be null");
        if (elementName.isBlank()) {
            throw new IllegalArgumentException("Element name must not be blank");
        }

        XMLStreamReader reader = null;
        try {
            reader = SecureParserFactory.createXMLStreamReader(input);
            splitInternal(reader, elementName, (index, doc) -> handler.accept(doc));
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to split XML stream: " + e.getMessage(), e);
        } finally {
            closeReader(reader);
        }
    }

    /**
     * Splits an XML file and processes each matching element via callback.
     * 拆分 XML 文件并通过回调处理每个匹配元素。
     *
     * @param path        the file path | 文件路径
     * @param elementName the element name to match | 要匹配的元素名称
     * @param handler     the callback handler | 回调处理器
     * @throws OpenXmlException if splitting fails | 如果拆分失败则抛出异常
     */
    public static void split(Path path, String elementName, Consumer<XmlDocument> handler) {
        Objects.requireNonNull(path, "Path must not be null");
        try (InputStream is = Files.newInputStream(path)) {
            split(is, elementName, handler);
        } catch (IOException e) {
            throw new OpenXmlException("Failed to read file: " + path, e);
        }
    }

    /**
     * Splits an XML string and processes each matching element via callback.
     * 拆分 XML 字符串并通过回调处理每个匹配元素。
     *
     * @param xml         the XML string | XML 字符串
     * @param elementName the element name to match | 要匹配的元素名称
     * @param handler     the callback handler | 回调处理器
     * @throws OpenXmlException if splitting fails | 如果拆分失败则抛出异常
     */
    public static void split(String xml, String elementName, Consumer<XmlDocument> handler) {
        Objects.requireNonNull(xml, "XML string must not be null");
        split(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), elementName, handler);
    }

    /**
     * Splits an XML input stream with index and processes each matching element via callback.
     * 拆分带索引的 XML 输入流并通过回调处理每个匹配元素。
     *
     * @param input       the input stream | 输入流
     * @param elementName the element name to match | 要匹配的元素名称
     * @param handler     the callback handler | 回调处理器
     * @throws OpenXmlException if splitting fails | 如果拆分失败则抛出异常
     */
    public static void splitIndexed(InputStream input, String elementName, Consumer<SplitResult> handler) {
        Objects.requireNonNull(input, "InputStream must not be null");
        Objects.requireNonNull(elementName, "Element name must not be null");
        Objects.requireNonNull(handler, "Handler must not be null");
        if (elementName.isBlank()) {
            throw new IllegalArgumentException("Element name must not be blank");
        }

        XMLStreamReader reader = null;
        try {
            reader = SecureParserFactory.createXMLStreamReader(input);
            splitInternal(reader, elementName, (index, doc) -> handler.accept(new SplitResult(index, doc)));
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to split XML stream: " + e.getMessage(), e);
        } finally {
            closeReader(reader);
        }
    }

    /**
     * Collects all matching element fragments from an XML string.
     * 从 XML 字符串中收集所有匹配的元素片段。
     *
     * @param xml         the XML string | XML 字符串
     * @param elementName the element name to match | 要匹配的元素名称
     * @return list of document fragments | 文档片段列表
     * @throws OpenXmlException if splitting fails | 如果拆分失败则抛出异常
     */
    public static List<XmlDocument> splitAll(String xml, String elementName) {
        Objects.requireNonNull(xml, "XML string must not be null");
        List<XmlDocument> results = new ArrayList<>();
        split(xml, elementName, results::add);
        return List.copyOf(results);
    }

    /**
     * Counts matching elements in an XML input stream using O(1) memory.
     * 使用 O(1) 内存计算 XML 输入流中的匹配元素数量。
     *
     * @param input       the input stream | 输入流
     * @param elementName the element name to count | 要计数的元素名称
     * @return the count of matching elements | 匹配元素的数量
     * @throws OpenXmlException if counting fails | 如果计数失败则抛出异常
     */
    public static int count(InputStream input, String elementName) {
        Objects.requireNonNull(input, "InputStream must not be null");
        Objects.requireNonNull(elementName, "Element name must not be null");
        if (elementName.isBlank()) {
            throw new IllegalArgumentException("Element name must not be blank");
        }

        XMLStreamReader reader = null;
        try {
            reader = SecureParserFactory.createXMLStreamReader(input);
            int count = 0;
            int depth = 0;
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (depth == 0 && elementName.equals(reader.getLocalName())) {
                        count++;
                        // Skip entire subtree
                        depth = 1;
                    } else if (depth > 0) {
                        depth++;
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    if (depth > 0) {
                        depth--;
                    }
                }
            }
            return count;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to count XML elements: " + e.getMessage(), e);
        } finally {
            closeReader(reader);
        }
    }

    /**
     * Counts matching elements in an XML string using O(1) memory.
     * 使用 O(1) 内存计算 XML 字符串中的匹配元素数量。
     *
     * @param xml         the XML string | XML 字符串
     * @param elementName the element name to count | 要计数的元素名称
     * @return the count of matching elements | 匹配元素的数量
     * @throws OpenXmlException if counting fails | 如果计数失败则抛出异常
     */
    public static int count(String xml, String elementName) {
        Objects.requireNonNull(xml, "XML string must not be null");
        return count(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), elementName);
    }

    /**
     * Internal split implementation using a BiConsumer for index and document.
     * 使用 BiConsumer 处理索引和文档的内部拆分实现。
     */
    private static void splitInternal(XMLStreamReader reader, String elementName,
                                       IndexedHandler handler) throws XMLStreamException {
        int index = 0;
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT && elementName.equals(reader.getLocalName())) {
                XmlDocument fragment = captureSubtree(reader, elementName);
                handler.handle(index, fragment);
                index++;
            }
        }
    }

    /**
     * Captures the current element and its entire subtree as an XmlDocument.
     * 将当前元素及其完整子树捕获为 XmlDocument。
     *
     * <p>When called, the reader is positioned on a START_ELEMENT event for the
     * target element. This method reads through the subtree tracking depth, and
     * builds a DOM document containing the element and all its content.</p>
     */
    private static XmlDocument captureSubtree(XMLStreamReader reader, String elementName) throws XMLStreamException {
        Document doc = SecureParserFactory.createDocumentBuilder().newDocument();

        // Create the root element matching the current reader position
        Element root = createElementFromReader(doc, reader);
        doc.appendChild(root);

        // Track depth: starts at 1 (we've seen the opening START_ELEMENT)
        int depth = 1;
        Node current = root;

        while (depth > 0 && reader.hasNext()) {
            int event = reader.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT -> {
                    if (depth >= MAX_DEPTH) {
                        throw new OpenXmlException(
                            "XML element nesting depth exceeded " + MAX_DEPTH + " in splitter subtree capture");
                    }
                    Element child = createElementFromReader(doc, reader);
                    current.appendChild(child);
                    current = child;
                    depth++;
                }
                case XMLStreamConstants.END_ELEMENT -> {
                    depth--;
                    if (depth > 0) {
                        current = current.getParentNode();
                    }
                }
                case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA -> {
                    String text = reader.getText();
                    if (text != null && !text.isEmpty()) {
                        if (event == XMLStreamConstants.CDATA) {
                            current.appendChild(doc.createCDATASection(text));
                        } else {
                            current.appendChild(doc.createTextNode(text));
                        }
                    }
                }
                case XMLStreamConstants.COMMENT -> {
                    String comment = reader.getText();
                    if (comment != null) {
                        current.appendChild(doc.createComment(comment));
                    }
                }
                case XMLStreamConstants.PROCESSING_INSTRUCTION -> {
                    current.appendChild(doc.createProcessingInstruction(
                            reader.getPITarget(), reader.getPIData()));
                }
                default -> {
                    // Ignore other events (SPACE, ENTITY_REFERENCE, etc.)
                }
            }
        }

        return XmlDocument.of(doc);
    }

    /**
     * Creates a DOM Element from the current reader position, preserving attributes and namespaces.
     * 从当前读取器位置创建 DOM 元素，保留属性和命名空间。
     */
    private static Element createElementFromReader(Document doc, XMLStreamReader reader) {
        String localName = reader.getLocalName();
        String namespaceUri = reader.getNamespaceURI();
        String prefix = reader.getPrefix();

        Element element;
        if (namespaceUri != null && !namespaceUri.isEmpty()) {
            String qualifiedName = (prefix != null && !prefix.isEmpty())
                    ? prefix + ":" + localName
                    : localName;
            element = doc.createElementNS(namespaceUri, qualifiedName);
        } else {
            element = doc.createElement(localName);
        }

        // Copy namespace declarations
        int nsCount = reader.getNamespaceCount();
        for (int i = 0; i < nsCount; i++) {
            String nsPrefix = reader.getNamespacePrefix(i);
            String nsUri = reader.getNamespaceURI(i);
            if (nsPrefix == null || nsPrefix.isEmpty()) {
                element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", nsUri);
            } else {
                element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + nsPrefix, nsUri);
            }
        }

        // Copy attributes
        int attrCount = reader.getAttributeCount();
        for (int i = 0; i < attrCount; i++) {
            String attrNs = reader.getAttributeNamespace(i);
            String attrLocal = reader.getAttributeLocalName(i);
            String attrValue = reader.getAttributeValue(i);
            String attrPrefix = reader.getAttributePrefix(i);

            if (attrNs != null && !attrNs.isEmpty()) {
                String qualifiedAttr = (attrPrefix != null && !attrPrefix.isEmpty())
                        ? attrPrefix + ":" + attrLocal
                        : attrLocal;
                element.setAttributeNS(attrNs, qualifiedAttr, attrValue);
            } else {
                element.setAttribute(attrLocal, attrValue);
            }
        }

        return element;
    }

    /**
     * Closes the XMLStreamReader, ignoring exceptions.
     * 关闭 XMLStreamReader，忽略异常。
     */
    private static void closeReader(XMLStreamReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (XMLStreamException ignored) {
                // Best effort close
            }
        }
    }

    /**
     * Functional interface for indexed handler.
     * 带索引处理器的函数接口。
     */
    @FunctionalInterface
    private interface IndexedHandler {
        void handle(int index, XmlDocument doc);
    }
}
