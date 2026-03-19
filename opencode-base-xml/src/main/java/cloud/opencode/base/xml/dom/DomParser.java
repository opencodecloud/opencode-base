package cloud.opencode.base.xml.dom;

import cloud.opencode.base.xml.exception.OpenXmlException;
import cloud.opencode.base.xml.exception.XmlParseException;
import cloud.opencode.base.xml.security.SecureParserFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * DOM Parser - Parses XML to DOM Document
 * DOM 解析器 - 将 XML 解析为 DOM Document
 *
 * <p>This class provides methods for parsing XML content into DOM Document objects.
 * All parsing is done securely with XXE protection enabled by default.</p>
 * <p>此类提供将 XML 内容解析为 DOM Document 对象的方法。所有解析默认启用 XXE 防护安全进行。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Parse from String, File, InputStream - 从字符串、文件、输入流解析</li>
 *   <li>Secure parsing (XXE protection) - 安全解析（XXE 防护）</li>
 *   <li>Namespace-aware parsing - 支持命名空间的解析</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Parse XML string
 * Document doc = DomParser.parse("<root><item>value</item></root>");
 *
 * // Parse from file
 * Document doc = DomParser.parse(Path.of("config.xml"));
 *
 * // Parse with namespace awareness
 * Document doc = DomParser.parseNamespaceAware(xmlString);
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility, secure parsing) - 线程安全: 是（无状态工具，安全解析）</li>
 *   <li>Null-safe: No (throws on null input) - 空值安全: 否（null 输入抛异常）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n=input size in characters/bytes - 时间复杂度: O(n)，n 为输入的字符/字节数</li>
 *   <li>Space complexity: O(n) as the entire DOM tree is held in memory - 空间复杂度: O(n)，整个 DOM 树保存在内存中</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class DomParser {

    private DomParser() {
        // Utility class
    }

    /**
     * Parses XML string to Document.
     * 解析 XML 字符串为 Document。
     *
     * @param xml the XML string | XML 字符串
     * @return the Document | Document
     * @throws XmlParseException if parsing fails | 如果解析失败则抛出异常
     */
    public static Document parse(String xml) {
        if (xml == null || xml.isBlank()) {
            throw new XmlParseException("XML string is null or empty");
        }
        try {
            DocumentBuilder builder = SecureParserFactory.createDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xml)));
        } catch (SAXParseException e) {
            throw new XmlParseException(e.getMessage(), e.getLineNumber(), e.getColumnNumber(), e);
        } catch (Exception e) {
            throw new XmlParseException("Failed to parse XML: " + e.getMessage(), e);
        }
    }

    /**
     * Parses XML file to Document.
     * 解析 XML 文件为 Document。
     *
     * @param path the file path | 文件路径
     * @return the Document | Document
     * @throws XmlParseException if parsing fails | 如果解析失败则抛出异常
     */
    public static Document parse(Path path) {
        if (path == null) {
            throw new XmlParseException("Path is null");
        }
        if (!Files.exists(path)) {
            throw new XmlParseException("File not found: " + path);
        }
        try (InputStream is = Files.newInputStream(path)) {
            return parse(is);
        } catch (IOException e) {
            throw new XmlParseException("Failed to read file: " + path, e);
        }
    }

    /**
     * Parses XML from input stream to Document.
     * 从输入流解析 XML 为 Document。
     *
     * @param input the input stream | 输入流
     * @return the Document | Document
     * @throws XmlParseException if parsing fails | 如果解析失败则抛出异常
     */
    public static Document parse(InputStream input) {
        if (input == null) {
            throw new XmlParseException("Input stream is null");
        }
        try {
            DocumentBuilder builder = SecureParserFactory.createDocumentBuilder();
            return builder.parse(input);
        } catch (SAXParseException e) {
            throw new XmlParseException(e.getMessage(), e.getLineNumber(), e.getColumnNumber(), e);
        } catch (Exception e) {
            throw new XmlParseException("Failed to parse XML: " + e.getMessage(), e);
        }
    }

    /**
     * Parses XML string with namespace awareness.
     * 解析 XML 字符串（支持命名空间）。
     *
     * @param xml the XML string | XML 字符串
     * @return the Document | Document
     * @throws XmlParseException if parsing fails | 如果解析失败则抛出异常
     */
    public static Document parseNamespaceAware(String xml) {
        if (xml == null || xml.isBlank()) {
            throw new XmlParseException("XML string is null or empty");
        }
        try {
            DocumentBuilder builder = SecureParserFactory.createNamespaceAwareDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xml)));
        } catch (SAXParseException e) {
            throw new XmlParseException(e.getMessage(), e.getLineNumber(), e.getColumnNumber(), e);
        } catch (Exception e) {
            throw new XmlParseException("Failed to parse XML: " + e.getMessage(), e);
        }
    }

    /**
     * Parses XML file with namespace awareness.
     * 解析 XML 文件（支持命名空间）。
     *
     * @param path the file path | 文件路径
     * @return the Document | Document
     * @throws XmlParseException if parsing fails | 如果解析失败则抛出异常
     */
    public static Document parseNamespaceAware(Path path) {
        if (path == null) {
            throw new XmlParseException("Path is null");
        }
        if (!Files.exists(path)) {
            throw new XmlParseException("File not found: " + path);
        }
        try (InputStream is = Files.newInputStream(path)) {
            return parseNamespaceAware(is);
        } catch (IOException e) {
            throw new XmlParseException("Failed to read file: " + path, e);
        }
    }

    /**
     * Parses XML from input stream with namespace awareness.
     * 从输入流解析 XML（支持命名空间）。
     *
     * @param input the input stream | 输入流
     * @return the Document | Document
     * @throws XmlParseException if parsing fails | 如果解析失败则抛出异常
     */
    public static Document parseNamespaceAware(InputStream input) {
        if (input == null) {
            throw new XmlParseException("Input stream is null");
        }
        try {
            DocumentBuilder builder = SecureParserFactory.createNamespaceAwareDocumentBuilder();
            return builder.parse(input);
        } catch (SAXParseException e) {
            throw new XmlParseException(e.getMessage(), e.getLineNumber(), e.getColumnNumber(), e);
        } catch (Exception e) {
            throw new XmlParseException("Failed to parse XML: " + e.getMessage(), e);
        }
    }

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

    /**
     * Validates whether the given string is well-formed XML.
     * 验证给定字符串是否是格式正确的 XML。
     *
     * @param xml the XML string | XML 字符串
     * @return true if valid XML | 如果是有效 XML 则返回 true
     */
    public static boolean isValidXml(String xml) {
        if (xml == null || xml.isBlank()) {
            return false;
        }
        try {
            parse(xml);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
