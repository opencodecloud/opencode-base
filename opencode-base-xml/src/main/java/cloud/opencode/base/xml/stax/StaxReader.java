package cloud.opencode.base.xml.stax;

import cloud.opencode.base.xml.exception.OpenXmlException;
import cloud.opencode.base.xml.security.SecureParserFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Closeable;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * StAX Reader - Pull-mode streaming XML reader
 * StAX 读取器 - 拉模式流式 XML 读取器
 *
 * <p>This class provides a fluent API for pull-mode XML parsing using StAX.
 * StAX is efficient for both memory and CPU when processing large XML files.</p>
 * <p>此类提供使用 StAX 进行拉模式 XML 解析的流式 API。
 * StAX 在处理大型 XML 文件时内存和 CPU 效率都很高。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple iteration
 * try (StaxReader reader = StaxReader.of(xml)) {
 *     while (reader.hasNext()) {
 *         if (reader.isStartElement("user")) {
 *             String id = reader.getAttribute("id");
 *             String name = reader.getElementText("name");
 *         }
 *         reader.next();
 *     }
 * }
 *
 * // Callback-based reading
 * StaxReader.of(xml)
 *     .onElement("user", (name, attrs) -> {
 *         System.out.println("User: " + attrs.get("id"));
 *     })
 *     .onText("name", text -> {
 *         System.out.println("Name: " + text);
 *     })
 *     .read();
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pull-mode streaming XML parsing - 拉模式流式 XML 解析</li>
 *   <li>Memory-efficient for large XML files - 大型 XML 文件的内存高效处理</li>
 *   <li>AutoCloseable for resource management - 支持 AutoCloseable 的资源管理</li>
 *   <li>Element and attribute access helpers - 元素和属性访问辅助方法</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not designed for shared use) - 线程安全: 否（不适用于共享使用）</li>
 *   <li>Null-safe: No (throws on null input) - 空值安全: 否（null 输入抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class StaxReader implements Closeable {

    private final XMLStreamReader reader;
    private final Map<String, BiConsumer<String, Map<String, String>>> elementCallbacks = new HashMap<>();
    private final Map<String, Consumer<String>> textCallbacks = new HashMap<>();
    private Consumer<String> endElementCallback;

    private StaxReader(XMLStreamReader reader) {
        this.reader = reader;
    }

    /**
     * Creates a reader from an XML string.
     * 从 XML 字符串创建读取器。
     *
     * @param xml the XML string | XML 字符串
     * @return a new reader | 新读取器
     */
    public static StaxReader of(String xml) {
        if (xml == null || xml.isBlank()) {
            throw new OpenXmlException("XML string is null or empty");
        }
        try {
            XMLInputFactory factory = SecureParserFactory.createXMLInputFactory();
            return new StaxReader(factory.createXMLStreamReader(new StringReader(xml)));
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to create StAX reader", e);
        }
    }

    /**
     * Creates a reader from an input stream.
     * 从输入流创建读取器。
     *
     * @param input the input stream | 输入流
     * @return a new reader | 新读取器
     */
    public static StaxReader of(InputStream input) {
        if (input == null) {
            throw new OpenXmlException("Input stream is null");
        }
        try {
            XMLInputFactory factory = SecureParserFactory.createXMLInputFactory();
            return new StaxReader(factory.createXMLStreamReader(input));
        } catch (XMLStreamException e) {
            try {
                input.close();
            } catch (Exception closeEx) {
                e.addSuppressed(closeEx);
            }
            throw new OpenXmlException("Failed to create StAX reader", e);
        }
    }

    /**
     * Creates a reader from a file path.
     * 从文件路径创建读取器。
     *
     * @param path the file path | 文件路径
     * @return a new reader | 新读取器
     */
    public static StaxReader of(Path path) {
        if (path == null) {
            throw new OpenXmlException("Path is null");
        }
        InputStream input = null;
        try {
            input = Files.newInputStream(path);
            return of(input);
        } catch (Exception e) {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception closeEx) {
                    e.addSuppressed(closeEx);
                }
            }
            throw new OpenXmlException("Failed to read file: " + path, e);
        }
    }

    /**
     * Creates a secure reader from an XML string.
     * 从 XML 字符串创建安全读取器。
     *
     * @param xml the XML string | XML 字符串
     * @return a new secure reader | 新的安全读取器
     */
    public static StaxReader ofSecure(String xml) {
        return of(xml);
    }

    // ==================== Navigation | 导航 ====================

    /**
     * Checks if there are more events.
     * 检查是否有更多事件。
     *
     * @return true if more events exist | 如果存在更多事件则返回 true
     */
    public boolean hasNext() {
        try {
            return reader.hasNext();
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to check next event", e);
        }
    }

    /**
     * Moves to the next event.
     * 移动到下一个事件。
     *
     * @return the event type | 事件类型
     */
    public int next() {
        try {
            return reader.next();
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to advance to next event", e);
        }
    }

    /**
     * Moves to the next tag (start or end element).
     * 移动到下一个标签（开始或结束元素）。
     *
     * @return the event type | 事件类型
     */
    public int nextTag() {
        try {
            return reader.nextTag();
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to advance to next tag", e);
        }
    }

    /**
     * Gets the current event type.
     * 获取当前事件类型。
     *
     * @return the event type | 事件类型
     */
    public int getEventType() {
        return reader.getEventType();
    }

    // ==================== Element Checks | 元素检查 ====================

    /**
     * Checks if the current event is a start element.
     * 检查当前事件是否是开始元素。
     *
     * @return true if start element | 如果是开始元素则返回 true
     */
    public boolean isStartElement() {
        return reader.getEventType() == XMLStreamConstants.START_ELEMENT;
    }

    /**
     * Checks if the current event is a start element with the given name.
     * 检查当前事件是否是具有给定名称的开始元素。
     *
     * @param localName the element name | 元素名称
     * @return true if matching start element | 如果是匹配的开始元素则返回 true
     */
    public boolean isStartElement(String localName) {
        return isStartElement() && getLocalName().equals(localName);
    }

    /**
     * Checks if the current event is an end element.
     * 检查当前事件是否是结束元素。
     *
     * @return true if end element | 如果是结束元素则返回 true
     */
    public boolean isEndElement() {
        return reader.getEventType() == XMLStreamConstants.END_ELEMENT;
    }

    /**
     * Checks if the current event is an end element with the given name.
     * 检查当前事件是否是具有给定名称的结束元素。
     *
     * @param localName the element name | 元素名称
     * @return true if matching end element | 如果是匹配的结束元素则返回 true
     */
    public boolean isEndElement(String localName) {
        return isEndElement() && getLocalName().equals(localName);
    }

    /**
     * Checks if the current event is characters.
     * 检查当前事件是否是字符。
     *
     * @return true if characters | 如果是字符则返回 true
     */
    public boolean isCharacters() {
        return reader.getEventType() == XMLStreamConstants.CHARACTERS;
    }

    /**
     * Checks if the current event is whitespace.
     * 检查当前事件是否是空白字符。
     *
     * @return true if whitespace | 如果是空白字符则返回 true
     */
    public boolean isWhiteSpace() {
        try {
            return reader.isWhiteSpace();
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Element Data | 元素数据 ====================

    /**
     * Gets the local name of the current element.
     * 获取当前元素的本地名称。
     *
     * @return the local name | 本地名称
     */
    public String getLocalName() {
        return reader.getLocalName();
    }

    /**
     * Gets the namespace URI of the current element.
     * 获取当前元素的命名空间 URI。
     *
     * @return the namespace URI | 命名空间 URI
     */
    public String getNamespaceURI() {
        return reader.getNamespaceURI();
    }

    /**
     * Gets the prefix of the current element.
     * 获取当前元素的前缀。
     *
     * @return the prefix | 前缀
     */
    public String getPrefix() {
        return reader.getPrefix();
    }

    /**
     * Gets an attribute value.
     * 获取属性值。
     *
     * @param localName the attribute name | 属性名称
     * @return the attribute value or null | 属性值或 null
     */
    public String getAttribute(String localName) {
        return reader.getAttributeValue(null, localName);
    }

    /**
     * Gets an attribute value with namespace.
     * 获取带命名空间的属性值。
     *
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @param localName    the attribute name | 属性名称
     * @return the attribute value or null | 属性值或 null
     */
    public String getAttribute(String namespaceURI, String localName) {
        return reader.getAttributeValue(namespaceURI, localName);
    }

    /**
     * Gets an attribute value as Optional.
     * 获取属性值作为 Optional。
     *
     * @param localName the attribute name | 属性名称
     * @return Optional containing the value | 包含值的 Optional
     */
    public Optional<String> getAttributeOptional(String localName) {
        return Optional.ofNullable(getAttribute(localName));
    }

    /**
     * Gets all attributes as a map.
     * 获取所有属性作为映射。
     *
     * @return map of attribute names to values | 属性名称到值的映射
     */
    public Map<String, String> getAttributes() {
        Map<String, String> attrs = new HashMap<>();
        int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            attrs.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
        }
        return attrs;
    }

    /**
     * Gets the text content of the current element.
     * 获取当前元素的文本内容。
     *
     * @return the text content | 文本内容
     */
    public String getText() {
        return reader.getText();
    }

    /**
     * Gets the element text (must be on START_ELEMENT).
     * 获取元素文本（必须在 START_ELEMENT 上）。
     *
     * @return the element text | 元素文本
     */
    public String getElementText() {
        try {
            return reader.getElementText();
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to get element text", e);
        }
    }

    /**
     * Reads text content from a child element.
     * 从子元素读取文本内容。
     *
     * @param elementName the element name | 元素名称
     * @return the text content or null | 文本内容或 null
     */
    public String getElementText(String elementName) {
        try {
            while (hasNext()) {
                next();
                if (isStartElement(elementName)) {
                    return getElementText();
                }
                if (isEndElement()) {
                    break;
                }
            }
            return null;
        } catch (Exception e) {
            throw new OpenXmlException("Failed to read element text: " + elementName, e);
        }
    }

    // ==================== Callback Registration | 回调注册 ====================

    /**
     * Registers a callback for element start.
     * 注册元素开始的回调。
     *
     * @param elementName the element name | 元素名称
     * @param callback    the callback (name, attributes) | 回调（名称，属性）
     * @return this reader for chaining | 此读取器以便链式调用
     */
    public StaxReader onElement(String elementName,
                                BiConsumer<String, Map<String, String>> callback) {
        elementCallbacks.put(elementName, callback);
        return this;
    }

    /**
     * Registers a callback for element text.
     * 注册元素文本的回调。
     *
     * @param elementName the element name | 元素名称
     * @param callback    the callback (text) | 回调（文本）
     * @return this reader for chaining | 此读取器以便链式调用
     */
    public StaxReader onText(String elementName, Consumer<String> callback) {
        textCallbacks.put(elementName, callback);
        return this;
    }

    /**
     * Registers a callback for any element end.
     * 注册任意元素结束的回调。
     *
     * @param callback the callback (element name) | 回调（元素名称）
     * @return this reader for chaining | 此读取器以便链式调用
     */
    public StaxReader onEndElement(Consumer<String> callback) {
        this.endElementCallback = callback;
        return this;
    }

    /**
     * Reads the entire document using registered callbacks.
     * 使用注册的回调读取整个文档。
     */
    public void read() {
        try {
            while (hasNext()) {
                int event = next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT -> {
                        String name = getLocalName();
                        BiConsumer<String, Map<String, String>> callback = elementCallbacks.get(name);
                        if (callback != null) {
                            callback.accept(name, getAttributes());
                        }
                        // Check for text callback
                        Consumer<String> textCallback = textCallbacks.get(name);
                        if (textCallback != null) {
                            textCallback.accept(getElementText());
                        }
                    }
                    case XMLStreamConstants.END_ELEMENT -> {
                        if (endElementCallback != null) {
                            endElementCallback.accept(getLocalName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new OpenXmlException("Failed during callback-based reading", e);
        } finally {
            close();
        }
    }

    // ==================== Utility | 工具方法 ====================

    /**
     * Skips to the next start element.
     * 跳转到下一个开始元素。
     *
     * @return true if found | 如果找到则返回 true
     */
    public boolean skipToNextStartElement() {
        while (hasNext()) {
            next();
            if (isStartElement()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Skips to a specific element.
     * 跳转到特定元素。
     *
     * @param elementName the element name | 元素名称
     * @return true if found | 如果找到则返回 true
     */
    public boolean skipTo(String elementName) {
        while (hasNext()) {
            next();
            if (isStartElement(elementName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Requires the current element to be a start element with the given name.
     * 要求当前元素是具有给定名称的开始元素。
     *
     * @param localName the expected element name | 预期的元素名称
     * @throws NoSuchElementException if not matching | 如果不匹配则抛出异常
     */
    public void require(String localName) {
        if (!isStartElement(localName)) {
            throw new NoSuchElementException("Expected element: " + localName +
                ", found: " + (isStartElement() ? getLocalName() : "non-element"));
        }
    }

    /**
     * Gets the underlying XMLStreamReader.
     * 获取底层 XMLStreamReader。
     *
     * @return the underlying reader | 底层读取器
     */
    public XMLStreamReader getUnderlyingReader() {
        return reader;
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (XMLStreamException e) {
            // Ignore close exceptions
        }
    }
}
