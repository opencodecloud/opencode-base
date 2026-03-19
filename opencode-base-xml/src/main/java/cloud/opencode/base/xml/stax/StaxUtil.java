package cloud.opencode.base.xml.stax;

import cloud.opencode.base.xml.exception.OpenXmlException;
import cloud.opencode.base.xml.security.SecureParserFactory;

import javax.xml.stream.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

/**
 * StAX Utility - Utility methods for StAX operations
 * StAX 工具类 - StAX 操作的工具方法
 *
 * <p>This class provides utility methods for common StAX operations.</p>
 * <p>此类提供常见 StAX 操作的工具方法。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Utility methods for common StAX operations - 常见 StAX 操作的工具方法</li>
 *   <li>XML content extraction and transformation helpers - XML 内容提取和转换辅助方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Extract text from XML element
 * String text = StaxUtil.getElementText(reader, "name");
 * 
 * // Read attributes into map
 * Map<String, String> attrs = StaxUtil.getAttributes(reader);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: No (throws on null reader) - 空值安全: 否（null 读取器抛异常）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for forEachEvent/forEachStartElement/collectElementNames/copyElement where n=number of XML events or subtree size; O(1) for individual reader/writer creation and attribute access - 时间复杂度: forEachEvent/forEachStartElement/collectElementNames/copyElement 为 O(n)，n 为 XML 事件数或子树大小；读写器创建和属性访问为 O(1)</li>
 *   <li>Space complexity: O(1) for streaming iteration operations; O(n) for collectElementNames result list - 空间复杂度: 流式迭代操作为 O(1)；collectElementNames 结果列表为 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class StaxUtil {

    private StaxUtil() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    // ==================== Event Type Names | 事件类型名称 ====================

    /**
     * Gets the event type name.
     * 获取事件类型名称。
     *
     * @param eventType the event type constant | 事件类型常量
     * @return the event type name | 事件类型名称
     */
    public static String getEventTypeName(int eventType) {
        return switch (eventType) {
            case XMLStreamConstants.START_ELEMENT -> "START_ELEMENT";
            case XMLStreamConstants.END_ELEMENT -> "END_ELEMENT";
            case XMLStreamConstants.PROCESSING_INSTRUCTION -> "PROCESSING_INSTRUCTION";
            case XMLStreamConstants.CHARACTERS -> "CHARACTERS";
            case XMLStreamConstants.COMMENT -> "COMMENT";
            case XMLStreamConstants.SPACE -> "SPACE";
            case XMLStreamConstants.START_DOCUMENT -> "START_DOCUMENT";
            case XMLStreamConstants.END_DOCUMENT -> "END_DOCUMENT";
            case XMLStreamConstants.ENTITY_REFERENCE -> "ENTITY_REFERENCE";
            case XMLStreamConstants.ATTRIBUTE -> "ATTRIBUTE";
            case XMLStreamConstants.DTD -> "DTD";
            case XMLStreamConstants.CDATA -> "CDATA";
            case XMLStreamConstants.NAMESPACE -> "NAMESPACE";
            case XMLStreamConstants.NOTATION_DECLARATION -> "NOTATION_DECLARATION";
            case XMLStreamConstants.ENTITY_DECLARATION -> "ENTITY_DECLARATION";
            default -> "UNKNOWN(" + eventType + ")";
        };
    }

    // ==================== Factory Creation | 工厂创建 ====================

    /**
     * Creates a secure XMLInputFactory.
     * 创建安全的 XMLInputFactory。
     *
     * @return a secure factory | 安全的工厂
     */
    public static XMLInputFactory createSecureInputFactory() {
        return SecureParserFactory.createXMLInputFactory();
    }

    /**
     * Creates an XMLOutputFactory.
     * 创建 XMLOutputFactory。
     *
     * @return a new factory | 新工厂
     */
    public static XMLOutputFactory createOutputFactory() {
        return XMLOutputFactory.newInstance();
    }

    // ==================== Reader Creation | 读取器创建 ====================

    /**
     * Creates a secure XMLStreamReader from a string.
     * 从字符串创建安全的 XMLStreamReader。
     *
     * @param xml the XML string | XML 字符串
     * @return a new reader | 新读取器
     */
    public static XMLStreamReader createReader(String xml) {
        try {
            return createSecureInputFactory().createXMLStreamReader(new StringReader(xml));
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to create XMLStreamReader", e);
        }
    }

    /**
     * Creates a secure XMLStreamReader from an input stream.
     * 从输入流创建安全的 XMLStreamReader。
     *
     * @param input the input stream | 输入流
     * @return a new reader | 新读取器
     */
    public static XMLStreamReader createReader(InputStream input) {
        try {
            return createSecureInputFactory().createXMLStreamReader(input);
        } catch (XMLStreamException e) {
            // Close the input stream on failure to prevent resource leak
            try {
                input.close();
            } catch (IOException ioe) {
                e.addSuppressed(ioe);
            }
            throw new OpenXmlException("Failed to create XMLStreamReader", e);
        }
    }

    /**
     * Creates a secure XMLStreamReader from a file.
     * 从文件创建安全的 XMLStreamReader。
     *
     * @param path the file path | 文件路径
     * @return a new reader | 新读取器
     */
    public static XMLStreamReader createReader(Path path) {
        InputStream input = null;
        try {
            input = Files.newInputStream(path);
            return createReader(input);
        } catch (IOException e) {
            // Close the input stream on failure to prevent resource leak
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ioe) {
                    e.addSuppressed(ioe);
                }
            }
            throw new OpenXmlException("Failed to read file: " + path, e);
        }
    }

    // ==================== Writer Creation | 写入器创建 ====================

    /**
     * Creates an XMLStreamWriter to a StringWriter.
     * 创建写入 StringWriter 的 XMLStreamWriter。
     *
     * @param writer the string writer | 字符串写入器
     * @return a new writer | 新写入器
     */
    public static XMLStreamWriter createWriter(StringWriter writer) {
        try {
            return createOutputFactory().createXMLStreamWriter(writer);
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to create XMLStreamWriter", e);
        }
    }

    /**
     * Creates an XMLStreamWriter to an output stream.
     * 创建写入输出流的 XMLStreamWriter。
     *
     * @param output the output stream | 输出流
     * @return a new writer | 新写入器
     */
    public static XMLStreamWriter createWriter(OutputStream output) {
        try {
            return createOutputFactory().createXMLStreamWriter(output, "UTF-8");
        } catch (XMLStreamException e) {
            // Close the output stream on failure to prevent resource leak
            try {
                output.close();
            } catch (IOException ioe) {
                e.addSuppressed(ioe);
            }
            throw new OpenXmlException("Failed to create XMLStreamWriter", e);
        }
    }

    /**
     * Creates an XMLStreamWriter to a file.
     * 创建写入文件的 XMLStreamWriter。
     *
     * @param path the file path | 文件路径
     * @return a new writer | 新写入器
     */
    public static XMLStreamWriter createWriter(Path path) {
        OutputStream output = null;
        try {
            output = Files.newOutputStream(path);
            return createWriter(output);
        } catch (IOException e) {
            // Close the output stream on failure to prevent resource leak
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ioe) {
                    e.addSuppressed(ioe);
                }
            }
            throw new OpenXmlException("Failed to write file: " + path, e);
        }
    }

    // ==================== Iteration Utilities | 迭代工具 ====================

    /**
     * Iterates over all events in XML.
     * 迭代 XML 中的所有事件。
     *
     * @param xml      the XML string | XML 字符串
     * @param consumer the event consumer | 事件消费者
     */
    public static void forEachEvent(String xml, Consumer<XMLStreamReader> consumer) {
        XMLStreamReader reader = createReader(xml);
        try {
            while (reader.hasNext()) {
                reader.next();
                consumer.accept(reader);
            }
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to iterate events", e);
        } finally {
            closeQuietly(reader);
        }
    }

    /**
     * Iterates over start elements in XML.
     * 迭代 XML 中的开始元素。
     *
     * @param xml      the XML string | XML 字符串
     * @param consumer the element consumer | 元素消费者
     */
    public static void forEachStartElement(String xml, Consumer<XMLStreamReader> consumer) {
        forEachEvent(xml, reader -> {
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                consumer.accept(reader);
            }
        });
    }

    /**
     * Collects all element names from XML.
     * 从 XML 收集所有元素名称。
     *
     * @param xml the XML string | XML 字符串
     * @return list of element names | 元素名称列表
     */
    public static List<String> collectElementNames(String xml) {
        List<String> names = new ArrayList<>();
        forEachStartElement(xml, reader -> names.add(reader.getLocalName()));
        return names;
    }

    /**
     * Counts elements with a specific name.
     * 计算具有特定名称的元素数量。
     *
     * @param xml         the XML string | XML 字符串
     * @param elementName the element name | 元素名称
     * @return the count | 计数
     */
    public static int countElements(String xml, String elementName) {
        int[] count = {0};
        forEachStartElement(xml, reader -> {
            if (reader.getLocalName().equals(elementName)) {
                count[0]++;
            }
        });
        return count[0];
    }

    // ==================== Element Utilities | 元素工具 ====================

    /**
     * Gets attributes from a reader as a map.
     * 从读取器获取属性作为映射。
     *
     * @param reader the reader | 读取器
     * @return map of attributes | 属性映射
     */
    public static Map<String, String> getAttributes(XMLStreamReader reader) {
        Map<String, String> attrs = new LinkedHashMap<>();
        int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            attrs.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
        }
        return attrs;
    }

    /**
     * Gets an optional attribute value.
     * 获取可选的属性值。
     *
     * @param reader    the reader | 读取器
     * @param localName the attribute name | 属性名称
     * @return Optional containing the value | 包含值的 Optional
     */
    public static Optional<String> getAttribute(XMLStreamReader reader, String localName) {
        return Optional.ofNullable(reader.getAttributeValue(null, localName));
    }

    /**
     * Gets element text safely.
     * 安全获取元素文本。
     *
     * @param reader the reader (must be at START_ELEMENT) | 读取器（必须在 START_ELEMENT）
     * @return the element text | 元素文本
     */
    public static String getElementTextSafe(XMLStreamReader reader) {
        try {
            return reader.getElementText();
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to get element text", e);
        }
    }

    /**
     * Skips to an element with the given name.
     * 跳转到具有给定名称的元素。
     *
     * @param reader      the reader | 读取器
     * @param elementName the element name | 元素名称
     * @return true if found | 如果找到则返回 true
     */
    public static boolean skipTo(XMLStreamReader reader, String elementName) {
        try {
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT &&
                    reader.getLocalName().equals(elementName)) {
                    return true;
                }
            }
            return false;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to skip to element: " + elementName, e);
        }
    }

    // ==================== Copy Utilities | 复制工具 ====================

    /**
     * Copies an element and its content to a writer.
     * 将元素及其内容复制到写入器。
     *
     * @param reader the reader (must be at START_ELEMENT) | 读取器（必须在 START_ELEMENT）
     * @param writer the writer | 写入器
     */
    public static void copyElement(XMLStreamReader reader, XMLStreamWriter writer) {
        try {
            if (reader.getEventType() != XMLStreamConstants.START_ELEMENT) {
                throw new OpenXmlException("Reader must be at START_ELEMENT");
            }

            int depth = 0;
            do {
                int event = reader.getEventType();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT -> {
                        depth++;
                        String prefix = reader.getPrefix();
                        String namespaceURI = reader.getNamespaceURI();
                        if (prefix != null && !prefix.isEmpty() && namespaceURI != null) {
                            writer.writeStartElement(prefix, reader.getLocalName(), namespaceURI);
                        } else if (namespaceURI != null) {
                            writer.writeStartElement(namespaceURI, reader.getLocalName());
                        } else {
                            writer.writeStartElement(reader.getLocalName());
                        }
                        // Copy namespaces
                        for (int i = 0; i < reader.getNamespaceCount(); i++) {
                            String nsPrefix = reader.getNamespacePrefix(i);
                            String nsUri = reader.getNamespaceURI(i);
                            if (nsPrefix == null || nsPrefix.isEmpty()) {
                                writer.writeDefaultNamespace(nsUri);
                            } else {
                                writer.writeNamespace(nsPrefix, nsUri);
                            }
                        }
                        // Copy attributes
                        for (int i = 0; i < reader.getAttributeCount(); i++) {
                            String attrPrefix = reader.getAttributePrefix(i);
                            String attrNs = reader.getAttributeNamespace(i);
                            String attrName = reader.getAttributeLocalName(i);
                            String attrValue = reader.getAttributeValue(i);
                            if (attrPrefix != null && !attrPrefix.isEmpty() && attrNs != null) {
                                writer.writeAttribute(attrPrefix, attrNs, attrName, attrValue);
                            } else {
                                writer.writeAttribute(attrName, attrValue);
                            }
                        }
                    }
                    case XMLStreamConstants.END_ELEMENT -> {
                        depth--;
                        writer.writeEndElement();
                    }
                    case XMLStreamConstants.CHARACTERS -> writer.writeCharacters(reader.getText());
                    case XMLStreamConstants.CDATA -> writer.writeCData(reader.getText());
                    case XMLStreamConstants.COMMENT -> writer.writeComment(reader.getText());
                    case XMLStreamConstants.PROCESSING_INSTRUCTION ->
                        writer.writeProcessingInstruction(reader.getPITarget(), reader.getPIData());
                }
                if (depth > 0 && reader.hasNext()) {
                    reader.next();
                }
            } while (depth > 0);
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to copy element", e);
        }
    }

    // ==================== Conversion Utilities | 转换工具 ====================

    /**
     * Converts an element to XML string.
     * 将元素转换为 XML 字符串。
     *
     * @param reader the reader (must be at START_ELEMENT) | 读取器（必须在 START_ELEMENT）
     * @return the XML string | XML 字符串
     */
    public static String elementToXml(XMLStreamReader reader) {
        StringWriter sw = new StringWriter();
        XMLStreamWriter writer = createWriter(sw);
        try {
            copyElement(reader, writer);
            writer.flush();
            return sw.toString();
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to convert element to XML", e);
        } finally {
            closeQuietly(writer);
        }
    }

    // ==================== Resource Management | 资源管理 ====================

    /**
     * Closes a reader quietly.
     * 安静地关闭读取器。
     *
     * @param reader the reader | 读取器
     */
    public static void closeQuietly(XMLStreamReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (XMLStreamException e) {
                // Ignore
            }
        }
    }

    /**
     * Closes a writer quietly.
     * 安静地关闭写入器。
     *
     * @param writer the writer | 写入器
     */
    public static void closeQuietly(XMLStreamWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (XMLStreamException e) {
                // Ignore
            }
        }
    }
}
