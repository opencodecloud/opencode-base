package cloud.opencode.base.xml.sax;

import cloud.opencode.base.xml.exception.OpenXmlException;
import cloud.opencode.base.xml.security.SecureParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SAX Parser - Event-driven streaming XML parser
 * SAX 解析器 - 事件驱动的流式 XML 解析器
 *
 * <p>This class provides SAX parsing with a simplified, fluent API.
 * SAX parsing is memory-efficient for large XML files.</p>
 * <p>此类提供简化流式 API 的 SAX 解析。SAX 解析对于大型 XML 文件内存效率高。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Using SimpleSaxHandler
 * List<User> users = new ArrayList<>();
 *
 * SaxParser.createSecure()
 *     .handler(SimpleSaxHandler.create()
 *         .onStart("user", (name, attrs) -> {
 *             User user = new User();
 *             user.setId(Long.parseLong(attrs.get("id")));
 *             users.add(user);
 *         })
 *         .onText("name", text -> users.getLast().setName(text))
 *         .onText("email", text -> users.getLast().setEmail(text))
 *     )
 *     .parse(xml);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Event-driven streaming XML parsing - 事件驱动的流式 XML 解析</li>
 *   <li>Memory-efficient for large XML files - 大型 XML 文件的内存高效处理</li>
 *   <li>Secure parsing with XXE protection - 带 XXE 防护的安全解析</li>
 *   <li>Fluent builder API - 流式构建器 API</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not designed for shared use) - 线程安全: 否（不适用于共享使用）</li>
 *   <li>Null-safe: No (throws on null XML/handler) - 空值安全: 否（null XML/处理器抛异常）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n=input size in characters/bytes - 时间复杂度: O(n)，n 为输入的字符/字节数</li>
 *   <li>Space complexity: O(1) - event-driven streaming; no document tree is built in memory - 空间复杂度: O(1) - 事件驱动的流式处理；不在内存中构建文档树</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class SaxParser {

    private static final Logger logger = System.getLogger(SaxParser.class.getName());

    private SaxHandler handler;
    private boolean namespaceAware = false;
    private boolean validating = false;
    private boolean secure = true;

    private SaxParser() {
    }

    /**
     * Creates a new SAX parser.
     * 创建新的 SAX 解析器。
     *
     * @return a new parser | 新解析器
     */
    public static SaxParser create() {
        return new SaxParser();
    }

    /**
     * Creates a secure SAX parser (XXE protection).
     * 创建安全的 SAX 解析器（XXE 防护）。
     *
     * @return a new secure parser | 新的安全解析器
     */
    public static SaxParser createSecure() {
        return new SaxParser().secure(true);
    }

    /**
     * Sets the handler.
     * 设置处理器。
     *
     * @param handler the SAX handler | SAX 处理器
     * @return this parser for chaining | 此解析器以便链式调用
     */
    public SaxParser handler(SaxHandler handler) {
        this.handler = handler;
        return this;
    }

    /**
     * Sets a simple handler.
     * 设置简化处理器。
     *
     * @param handler the simple handler | 简化处理器
     * @return this parser for chaining | 此解析器以便链式调用
     */
    public SaxParser handler(SimpleSaxHandler handler) {
        this.handler = handler;
        return this;
    }

    /**
     * Enables namespace awareness.
     * 启用命名空间感知。
     *
     * @param enabled whether enabled | 是否启用
     * @return this parser for chaining | 此解析器以便链式调用
     */
    public SaxParser namespaceAware(boolean enabled) {
        this.namespaceAware = enabled;
        return this;
    }

    /**
     * Enables validation.
     * 启用验证。
     *
     * @param enabled whether enabled | 是否启用
     * @return this parser for chaining | 此解析器以便链式调用
     */
    public SaxParser validating(boolean enabled) {
        this.validating = enabled;
        return this;
    }

    /**
     * Enables secure mode (XXE protection).
     * 启用安全模式（XXE 防护）。
     *
     * @param enabled whether enabled | 是否启用
     * @return this parser for chaining | 此解析器以便链式调用
     */
    public SaxParser secure(boolean enabled) {
        this.secure = enabled;
        return this;
    }

    /**
     * Parses an XML string.
     * 解析 XML 字符串。
     *
     * @param xml the XML string | XML 字符串
     */
    public void parse(String xml) {
        if (xml == null || xml.isBlank()) {
            throw new OpenXmlException("XML string is null or empty");
        }
        try {
            createParser().parse(new InputSource(new StringReader(xml)), createDefaultHandler());
        } catch (SAXException | IOException e) {
            throw new OpenXmlException("SAX parsing failed", e);
        }
    }

    /**
     * Parses from an input stream.
     * 从输入流解析。
     *
     * @param input the input stream | 输入流
     */
    public void parse(InputStream input) {
        if (input == null) {
            throw new OpenXmlException("Input stream is null");
        }
        try {
            createParser().parse(input, createDefaultHandler());
        } catch (SAXException | IOException e) {
            throw new OpenXmlException("SAX parsing failed", e);
        }
    }

    /**
     * Parses from a file.
     * 从文件解析。
     *
     * @param path the file path | 文件路径
     */
    public void parse(Path path) {
        if (path == null) {
            throw new OpenXmlException("Path is null");
        }
        try (InputStream is = Files.newInputStream(path)) {
            parse(is);
        } catch (IOException e) {
            throw new OpenXmlException("Failed to read file: " + path, e);
        }
    }

    private SAXParser createParser() {
        if (secure) {
            return namespaceAware
                ? SecureParserFactory.createNamespaceAwareSAXParser()
                : SecureParserFactory.createSAXParser();
        }
        try {
            var factory = javax.xml.parsers.SAXParserFactory.newInstance();
            factory.setNamespaceAware(namespaceAware);
            factory.setValidating(validating);
            // Basic XXE protections even in non-secure mode
            // 即使在非安全模式下也添加基本的 XXE 防护
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            return factory.newSAXParser();
        } catch (Exception e) {
            throw new OpenXmlException("Failed to create SAX parser", e);
        }
    }

    private DefaultHandler createDefaultHandler() {
        return new DefaultHandler() {
            @Override
            public void startDocument() throws SAXException {
                if (handler != null) {
                    handler.startDocument();
                }
            }

            @Override
            public void endDocument() throws SAXException {
                if (handler != null) {
                    handler.endDocument();
                }
            }

            @Override
            public void startElement(String uri, String localName, String qName,
                                     Attributes attributes) throws SAXException {
                if (handler != null) {
                    Map<String, String> attrs = new LinkedHashMap<>();
                    for (int i = 0; i < attributes.getLength(); i++) {
                        attrs.put(attributes.getQName(i), attributes.getValue(i));
                    }
                    handler.startElement(uri, localName, qName, attrs);
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                if (handler != null) {
                    handler.endElement(uri, localName, qName);
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                if (handler != null) {
                    handler.characters(new String(ch, start, length));
                }
            }

            @Override
            public void processingInstruction(String target, String data) throws SAXException {
                if (handler != null) {
                    handler.processingInstruction(target, data);
                }
            }

            @Override
            public void warning(org.xml.sax.SAXParseException e) throws SAXException {
                logger.log(Level.WARNING, "SAX parsing warning at line " + e.getLineNumber() +
                        ", column " + e.getColumnNumber(), e);
                if (handler != null) {
                    handler.warning(new SaxParseException(e));
                }
            }

            @Override
            public void error(org.xml.sax.SAXParseException e) throws SAXException {
                logger.log(Level.ERROR, "SAX parsing error at line " + e.getLineNumber() +
                        ", column " + e.getColumnNumber(), e);
                if (handler != null) {
                    handler.error(new SaxParseException(e));
                }
            }

            @Override
            public void fatalError(org.xml.sax.SAXParseException e) throws SAXException {
                logger.log(Level.ERROR, "SAX parsing fatal error at line " + e.getLineNumber() +
                        ", column " + e.getColumnNumber(), e);
                throw new SaxParseException(e);
            }
        };
    }
}
