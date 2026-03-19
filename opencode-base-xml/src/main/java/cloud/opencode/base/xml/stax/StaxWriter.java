package cloud.opencode.base.xml.stax;

import cloud.opencode.base.xml.exception.OpenXmlException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * StAX Writer - Streaming XML writer
 * StAX 写入器 - 流式 XML 写入器
 *
 * <p>This class provides a fluent API for streaming XML output using StAX.
 * StAX writing is memory-efficient for generating large XML files.</p>
 * <p>此类提供使用 StAX 进行流式 XML 输出的流式 API。
 * StAX 写入对于生成大型 XML 文件内存效率高。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Write to string
 * String xml = StaxWriter.create()
 *     .startDocument()
 *     .startElement("users")
 *         .startElement("user")
 *             .attribute("id", "1")
 *             .element("name", "John")
 *             .element("email", "john@example.com")
 *         .endElement()
 *     .endElement()
 *     .endDocument()
 *     .toString();
 *
 * // Write to file
 * StaxWriter.create(Path.of("users.xml"))
 *     .startDocument("UTF-8", "1.0")
 *     .startElement("users")
 *     // ... add content
 *     .endDocument()
 *     .close();
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent API for streaming XML output - 用于流式 XML 输出的流式 API</li>
 *   <li>Memory-efficient writing for large XML files - 大型 XML 文件的内存高效写入</li>
 *   <li>Automatic indentation and formatting - 自动缩进和格式化</li>
 *   <li>Namespace and attribute support - 命名空间和属性支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not designed for shared use) - 线程安全: 否（不适用于共享使用）</li>
 *   <li>Null-safe: No (throws on null element names) - 空值安全: 否（null 元素名称抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class StaxWriter implements Closeable {

    private final XMLStreamWriter writer;
    private final StringWriter stringWriter;
    private final OutputStream outputStream;
    private final Deque<String> elementStack = new ArrayDeque<>();
    private boolean formatted = false;
    private int indentLevel = 0;
    private String indentString = "    ";

    private StaxWriter(XMLStreamWriter writer, StringWriter stringWriter, OutputStream outputStream) {
        this.writer = writer;
        this.stringWriter = stringWriter;
        this.outputStream = outputStream;
    }

    /**
     * Creates a new writer that writes to a string.
     * 创建写入字符串的新写入器。
     *
     * @return a new writer | 新写入器
     */
    public static StaxWriter create() {
        try {
            StringWriter sw = new StringWriter();
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            return new StaxWriter(factory.createXMLStreamWriter(sw), sw, null);
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to create StAX writer", e);
        }
    }

    /**
     * Creates a new writer that writes to an output stream.
     * 创建写入输出流的新写入器。
     *
     * @param output the output stream | 输出流
     * @return a new writer | 新写入器
     */
    public static StaxWriter create(OutputStream output) {
        try {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            return new StaxWriter(factory.createXMLStreamWriter(output, "UTF-8"), null, output);
        } catch (XMLStreamException e) {
            // Close the output stream on failure to prevent resource leak
            try {
                output.close();
            } catch (IOException ioe) {
                e.addSuppressed(ioe);
            }
            throw new OpenXmlException("Failed to create StAX writer", e);
        }
    }

    /**
     * Creates a new writer that writes to a file.
     * 创建写入文件的新写入器。
     *
     * @param path the file path | 文件路径
     * @return a new writer | 新写入器
     */
    public static StaxWriter create(Path path) {
        OutputStream os = null;
        try {
            os = Files.newOutputStream(path);
            return create(os);
        } catch (IOException e) {
            // Close the output stream on failure to prevent resource leak
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ioe) {
                    e.addSuppressed(ioe);
                }
            }
            throw new OpenXmlException("Failed to create file writer: " + path, e);
        }
    }

    // ==================== Configuration | 配置 ====================

    /**
     * Enables formatted output with indentation.
     * 启用带缩进的格式化输出。
     *
     * @param formatted whether to format | 是否格式化
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter formatted(boolean formatted) {
        this.formatted = formatted;
        return this;
    }

    /**
     * Sets the indent string.
     * 设置缩进字符串。
     *
     * @param indent the indent string | 缩进字符串
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter indent(String indent) {
        this.indentString = indent;
        return this;
    }

    /**
     * Sets the indent size in spaces.
     * 设置空格缩进大小。
     *
     * @param spaces the number of spaces | 空格数
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter indent(int spaces) {
        this.indentString = " ".repeat(spaces);
        return this;
    }

    // ==================== Document | 文档 ====================

    /**
     * Writes the XML declaration.
     * 写入 XML 声明。
     *
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter startDocument() {
        return startDocument("UTF-8", "1.0");
    }

    /**
     * Writes the XML declaration with encoding and version.
     * 写入带编码和版本的 XML 声明。
     *
     * @param encoding the encoding | 编码
     * @param version  the XML version | XML 版本
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter startDocument(String encoding, String version) {
        try {
            writer.writeStartDocument(encoding, version);
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to write document start", e);
        }
    }

    /**
     * Ends the document.
     * 结束文档。
     *
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter endDocument() {
        try {
            writer.writeEndDocument();
            writer.flush();
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to write document end", e);
        }
    }

    // ==================== Elements | 元素 ====================

    /**
     * Starts an element.
     * 开始一个元素。
     *
     * @param localName the element name | 元素名称
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter startElement(String localName) {
        try {
            writeIndent();
            writer.writeStartElement(localName);
            elementStack.push(localName);
            indentLevel++;
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to write start element: " + localName, e);
        }
    }

    /**
     * Starts an element with namespace.
     * 开始带命名空间的元素。
     *
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @param localName    the element name | 元素名称
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter startElement(String namespaceURI, String localName) {
        try {
            writeIndent();
            writer.writeStartElement(namespaceURI, localName);
            elementStack.push(localName);
            indentLevel++;
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to write start element: " + localName, e);
        }
    }

    /**
     * Starts an element with prefix and namespace.
     * 开始带前缀和命名空间的元素。
     *
     * @param prefix       the prefix | 前缀
     * @param localName    the element name | 元素名称
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter startElement(String prefix, String localName, String namespaceURI) {
        try {
            writeIndent();
            writer.writeStartElement(prefix, localName, namespaceURI);
            elementStack.push(localName);
            indentLevel++;
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to write start element: " + localName, e);
        }
    }

    /**
     * Ends the current element.
     * 结束当前元素。
     *
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter endElement() {
        try {
            indentLevel--;
            writeIndent();
            writer.writeEndElement();
            elementStack.pop();
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to write end element", e);
        }
    }

    /**
     * Writes an empty element.
     * 写入空元素。
     *
     * @param localName the element name | 元素名称
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter emptyElement(String localName) {
        try {
            writeIndent();
            writer.writeEmptyElement(localName);
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to write empty element: " + localName, e);
        }
    }

    /**
     * Writes a complete element with text content.
     * 写入带文本内容的完整元素。
     *
     * @param localName the element name | 元素名称
     * @param text      the text content | 文本内容
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter element(String localName, String text) {
        try {
            writeIndent();
            writer.writeStartElement(localName);
            if (text != null) {
                writer.writeCharacters(text);
            }
            writer.writeEndElement();
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to write element: " + localName, e);
        }
    }

    /**
     * Writes a complete element with text content if not null.
     * 如果不为 null 则写入带文本内容的完整元素。
     *
     * @param localName the element name | 元素名称
     * @param text      the text content | 文本内容
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter elementIfNotNull(String localName, String text) {
        if (text != null) {
            return element(localName, text);
        }
        return this;
    }

    /**
     * Writes a complete element with number content.
     * 写入带数字内容的完整元素。
     *
     * @param localName the element name | 元素名称
     * @param value     the number value | 数字值
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter element(String localName, Number value) {
        return element(localName, value != null ? value.toString() : null);
    }

    /**
     * Writes a complete element with boolean content.
     * 写入带布尔内容的完整元素。
     *
     * @param localName the element name | 元素名称
     * @param value     the boolean value | 布尔值
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter element(String localName, boolean value) {
        return element(localName, String.valueOf(value));
    }

    // ==================== Attributes | 属性 ====================

    /**
     * Writes an attribute.
     * 写入属性。
     *
     * @param localName the attribute name | 属性名称
     * @param value     the attribute value | 属性值
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter attribute(String localName, String value) {
        try {
            writer.writeAttribute(localName, value);
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to write attribute: " + localName, e);
        }
    }

    /**
     * Writes an attribute with namespace.
     * 写入带命名空间的属性。
     *
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @param localName    the attribute name | 属性名称
     * @param value        the attribute value | 属性值
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter attribute(String namespaceURI, String localName, String value) {
        try {
            writer.writeAttribute(namespaceURI, localName, value);
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to write attribute: " + localName, e);
        }
    }

    /**
     * Writes an attribute with prefix and namespace.
     * 写入带前缀和命名空间的属性。
     *
     * @param prefix       the prefix | 前缀
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @param localName    the attribute name | 属性名称
     * @param value        the attribute value | 属性值
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter attribute(String prefix, String namespaceURI, String localName, String value) {
        try {
            writer.writeAttribute(prefix, namespaceURI, localName, value);
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to write attribute: " + localName, e);
        }
    }

    /**
     * Writes an attribute if value is not null.
     * 如果值不为 null 则写入属性。
     *
     * @param localName the attribute name | 属性名称
     * @param value     the attribute value | 属性值
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter attributeIfNotNull(String localName, String value) {
        if (value != null) {
            return attribute(localName, value);
        }
        return this;
    }

    /**
     * Writes an attribute with number value.
     * 写入带数字值的属性。
     *
     * @param localName the attribute name | 属性名称
     * @param value     the number value | 数字值
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter attribute(String localName, Number value) {
        return attribute(localName, value != null ? value.toString() : "");
    }

    /**
     * Writes an attribute with boolean value.
     * 写入带布尔值的属性。
     *
     * @param localName the attribute name | 属性名称
     * @param value     the boolean value | 布尔值
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter attribute(String localName, boolean value) {
        return attribute(localName, String.valueOf(value));
    }

    // ==================== Content | 内容 ====================

    /**
     * Writes text content.
     * 写入文本内容。
     *
     * @param text the text | 文本
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter text(String text) {
        try {
            if (text != null) {
                writer.writeCharacters(text);
            }
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to write text", e);
        }
    }

    /**
     * Writes CDATA content.
     * 写入 CDATA 内容。
     *
     * @param data the CDATA content | CDATA 内容
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter cdata(String data) {
        try {
            if (data != null) {
                writer.writeCData(data);
            }
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to write CDATA", e);
        }
    }

    /**
     * Writes a comment.
     * 写入注释。
     *
     * @param comment the comment text | 注释文本
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter comment(String comment) {
        try {
            writeIndent();
            writer.writeComment(comment);
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to write comment", e);
        }
    }

    /**
     * Writes a processing instruction.
     * 写入处理指令。
     *
     * @param target the target | 目标
     * @param data   the data | 数据
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter processingInstruction(String target, String data) {
        try {
            writeIndent();
            writer.writeProcessingInstruction(target, data);
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to write processing instruction", e);
        }
    }

    // ==================== Namespace | 命名空间 ====================

    /**
     * Writes a default namespace declaration.
     * 写入默认命名空间声明。
     *
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter defaultNamespace(String namespaceURI) {
        try {
            writer.writeDefaultNamespace(namespaceURI);
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to write default namespace", e);
        }
    }

    /**
     * Writes a namespace declaration.
     * 写入命名空间声明。
     *
     * @param prefix       the prefix | 前缀
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter namespace(String prefix, String namespaceURI) {
        try {
            writer.writeNamespace(prefix, namespaceURI);
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to write namespace", e);
        }
    }

    // ==================== Output | 输出 ====================

    /**
     * Flushes the writer.
     * 刷新写入器。
     *
     * @return this writer for chaining | 此写入器以便链式调用
     */
    public StaxWriter flush() {
        try {
            writer.flush();
            return this;
        } catch (XMLStreamException e) {
            throw new OpenXmlException("Failed to flush writer", e);
        }
    }

    /**
     * Gets the underlying XMLStreamWriter.
     * 获取底层 XMLStreamWriter。
     *
     * @return the underlying writer | 底层写入器
     */
    public XMLStreamWriter getUnderlyingWriter() {
        return writer;
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (Exception e) {
            // Log but don't throw from close
            System.getLogger(StaxWriter.class.getName())
                    .log(System.Logger.Level.WARNING, "Error closing XML writer", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    System.getLogger(StaxWriter.class.getName())
                            .log(System.Logger.Level.WARNING, "Error closing output stream", e);
                }
            }
        }
    }

    @Override
    public String toString() {
        if (stringWriter != null) {
            return stringWriter.toString();
        }
        return super.toString();
    }

    private void writeIndent() throws XMLStreamException {
        if (formatted && indentLevel > 0) {
            writer.writeCharacters("\n");
            for (int i = 0; i < indentLevel; i++) {
                writer.writeCharacters(indentString);
            }
        }
    }
}
