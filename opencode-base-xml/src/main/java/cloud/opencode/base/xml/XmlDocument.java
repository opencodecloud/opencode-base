package cloud.opencode.base.xml;

import cloud.opencode.base.xml.bind.XmlBinder;
import cloud.opencode.base.xml.dom.DomParser;
import cloud.opencode.base.xml.dom.DomUtil;
import cloud.opencode.base.xml.exception.OpenXmlException;
import cloud.opencode.base.xml.security.SecureParserFactory;
import cloud.opencode.base.xml.xpath.OpenXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * XML Document - Wrapper for DOM Document with fluent API
 * XML 文档 - 提供流式 API 的 DOM Document 封装
 *
 * <p>This class wraps a DOM Document and provides convenient methods for
 * parsing, querying, modifying, and serializing XML content.</p>
 * <p>此类封装 DOM Document，并提供便捷的方法来解析、查询、修改和序列化 XML 内容。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Parse XML from String, File, InputStream, classpath resource - 从字符串、文件、输入流、类路径资源解析 XML</li>
 *   <li>XPath query support - XPath 查询支持</li>
 *   <li>Element access and modification - 元素访问和修改</li>
 *   <li>Object binding via annotations - 通过注解进行对象绑定</li>
 *   <li>Serialization to XML string or file - 序列化为 XML 字符串或文件</li>
 *   <li>Conversion to Map representation - 转换为 Map 表示</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Parse XML string
 * XmlDocument doc = XmlDocument.parse("<root><item>value</item></root>");
 *
 * // Load from file
 * XmlDocument doc = XmlDocument.load(Path.of("config.xml"));
 *
 * // Create new document
 * XmlDocument doc = XmlDocument.create("root");
 *
 * // Access content
 * XmlElement root = doc.getRoot();
 * String text = doc.getElementText("item");
 *
 * // XPath queries
 * String value = doc.xpath("//item/text()");
 * List<XmlElement> items = doc.xpathList("//item");
 *
 * // Bind to object
 * Config config = doc.bind(Config.class);
 *
 * // Serialize
 * String xml = doc.toXml(4); // with 4-space indent
 * doc.save(Path.of("output.xml"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (wraps mutable DOM Document) - 线程安全: 否（封装可变的 DOM Document）</li>
 *   <li>Null-safe: No (null inputs throw exceptions) - 空值安全: 否（空值输入抛出异常）</li>
 *   <li>Secure parsing with XXE protection - 安全解析，带 XXE 防护</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class XmlDocument {

    private final Document document;

    private XmlDocument(Document document) {
        this.document = Objects.requireNonNull(document, "Document must not be null");
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Parses XML string to document.
     * 解析 XML 字符串为文档。
     *
     * @param xml the XML string | XML 字符串
     * @return the document | 文档
     */
    public static XmlDocument parse(String xml) {
        return new XmlDocument(DomParser.parse(xml));
    }

    /**
     * Loads XML from file.
     * 从文件加载 XML。
     *
     * @param path the file path | 文件路径
     * @return the document | 文档
     */
    public static XmlDocument load(Path path) {
        return new XmlDocument(DomParser.parse(path));
    }

    /**
     * Loads XML from input stream.
     * 从输入流加载 XML。
     *
     * @param input the input stream | 输入流
     * @return the document | 文档
     */
    public static XmlDocument load(InputStream input) {
        return new XmlDocument(DomParser.parse(input));
    }

    /**
     * Loads XML from classpath resource.
     * 从类路径资源加载 XML。
     *
     * @param resourceName the resource name | 资源名
     * @return the document | 文档
     */
    public static XmlDocument loadResource(String resourceName) {
        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new OpenXmlException("Resource not found: " + resourceName);
            }
            return load(is);
        } catch (IOException e) {
            throw new OpenXmlException("Failed to load resource: " + resourceName, e);
        }
    }

    /**
     * Creates an empty document with the specified root element.
     * 创建具有指定根元素的空文档。
     *
     * @param rootName the root element name | 根元素名称
     * @return the document | 文档
     */
    public static XmlDocument create(String rootName) {
        try {
            Document doc = SecureParserFactory.createDocumentBuilder().newDocument();
            Element root = doc.createElement(rootName);
            doc.appendChild(root);
            return new XmlDocument(doc);
        } catch (Exception e) {
            throw new OpenXmlException("Failed to create document", e);
        }
    }

    /**
     * Wraps an existing DOM Document.
     * 封装现有的 DOM Document。
     *
     * @param document the DOM Document | DOM Document
     * @return the wrapped document | 封装的文档
     */
    public static XmlDocument of(Document document) {
        return new XmlDocument(document);
    }

    // ==================== Basic Accessors | 基本访问器 ====================

    /**
     * Returns the root element.
     * 返回根元素。
     *
     * @return the root element | 根元素
     */
    public XmlElement getRoot() {
        Element root = document.getDocumentElement();
        return root != null ? XmlElement.of(root) : null;
    }

    /**
     * Returns the underlying DOM Document.
     * 返回底层的 DOM Document。
     *
     * @return the DOM Document | DOM Document
     */
    public Document getDocument() {
        return document;
    }

    // ==================== XPath Queries | XPath 查询 ====================

    /**
     * Evaluates an XPath expression and returns string result.
     * 求值 XPath 表达式并返回字符串结果。
     *
     * @param xpath the XPath expression | XPath 表达式
     * @return the result string | 结果字符串
     */
    public String xpath(String xpath) {
        return OpenXPath.selectString(document, xpath);
    }

    /**
     * Evaluates an XPath expression and returns element list.
     * 求值 XPath 表达式并返回元素列表。
     *
     * @param xpath the XPath expression | XPath 表达式
     * @return the list of matching elements | 匹配元素列表
     */
    public List<XmlElement> xpathList(String xpath) {
        return OpenXPath.selectElements(document, xpath);
    }

    /**
     * Evaluates an XPath expression and returns single element.
     * 求值 XPath 表达式并返回单个元素。
     *
     * @param xpath the XPath expression | XPath 表达式
     * @return the element, or null if not found | 元素，如果未找到则返回 null
     */
    public XmlElement xpathOne(String xpath) {
        return OpenXPath.selectElement(document, xpath);
    }

    /**
     * Evaluates an XPath expression and returns typed result.
     * 求值 XPath 表达式并返回类型化结果。
     *
     * @param <T>   the type parameter | 类型参数
     * @param xpath the XPath expression | XPath 表达式
     * @param clazz the target type | 目标类型
     * @return the typed result | 类型化结果
     */
    public <T> T xpath(String xpath, Class<T> clazz) {
        String value = xpath(xpath);
        return DomUtil.convertText(value, clazz);
    }

    // ==================== Element Access | 元素访问 ====================

    /**
     * Returns the first element with the specified name.
     * 返回具有指定名称的第一个元素。
     *
     * @param name the element name | 元素名称
     * @return the element, or null if not found | 元素，如果未找到则返回 null
     */
    public XmlElement getElement(String name) {
        XmlElement root = getRoot();
        return root != null ? root.getChild(name) : null;
    }

    /**
     * Returns all elements with the specified name.
     * 返回具有指定名称的所有元素。
     *
     * @param name the element name | 元素名称
     * @return the list of elements | 元素列表
     */
    public List<XmlElement> getElements(String name) {
        return xpathList("//" + name);
    }

    /**
     * Returns the text content of the first element with the specified name.
     * 返回具有指定名称的第一个元素的文本内容。
     *
     * @param name the element name | 元素名称
     * @return the text content, or null if not found | 文本内容，如果未找到则返回 null
     */
    public String getElementText(String name) {
        XmlElement elem = getElement(name);
        return elem != null ? elem.getText() : null;
    }

    /**
     * Returns the text content of an element or default value.
     * 返回元素的文本内容或默认值。
     *
     * @param name         the element name | 元素名称
     * @param defaultValue the default value | 默认值
     * @return the text content or default | 文本内容或默认值
     */
    public String getElementText(String name, String defaultValue) {
        String text = getElementText(name);
        return text != null ? text : defaultValue;
    }

    /**
     * Returns whether an element with the specified name exists.
     * 返回是否存在具有指定名称的元素。
     *
     * @param name the element name | 元素名称
     * @return true if element exists | 如果元素存在则返回 true
     */
    public boolean hasElement(String name) {
        return getElement(name) != null;
    }

    // ==================== Modification | 修改操作 ====================

    /**
     * Adds an element to the root.
     * 向根元素添加元素。
     *
     * @param name the element name | 元素名称
     * @param text the text content | 文本内容
     * @return the new element | 新元素
     */
    public XmlElement addElement(String name, String text) {
        XmlElement root = getRoot();
        if (root == null) {
            throw new OpenXmlException("Document has no root element");
        }
        return root.addChild(name, text);
    }

    /**
     * Adds an existing element to the root.
     * 向根元素添加现有元素。
     *
     * @param element the element to add | 要添加的元素
     * @return this document for chaining | 此文档以便链式调用
     */
    public XmlDocument addElement(XmlElement element) {
        XmlElement root = getRoot();
        if (root == null) {
            throw new OpenXmlException("Document has no root element");
        }
        root.addChild(element);
        return this;
    }

    /**
     * Removes elements with the specified name from the root.
     * 从根元素移除具有指定名称的元素。
     *
     * @param name the element name | 元素名称
     * @return this document for chaining | 此文档以便链式调用
     */
    public XmlDocument removeElement(String name) {
        XmlElement root = getRoot();
        if (root != null) {
            root.removeChild(name);
        }
        return this;
    }

    // ==================== Binding | 绑定 ====================

    /**
     * Binds this document to an object.
     * 将此文档绑定到对象。
     *
     * @param <T>   the type parameter | 类型参数
     * @param clazz the target type | 目标类型
     * @return the bound object | 绑定的对象
     */
    public <T> T bind(Class<T> clazz) {
        return XmlBinder.create().unmarshal(this, clazz);
    }

    // ==================== Output | 输出 ====================

    /**
     * Converts this document to XML string.
     * 将此文档转换为 XML 字符串。
     *
     * @return the XML string | XML 字符串
     */
    public String toXml() {
        return DomUtil.toString(document);
    }

    /**
     * Converts this document to formatted XML string.
     * 将此文档转换为格式化的 XML 字符串。
     *
     * @param indent the number of spaces for indentation | 缩进空格数
     * @return the formatted XML string | 格式化的 XML 字符串
     */
    public String toXml(int indent) {
        return DomUtil.toString(document, indent);
    }

    /**
     * Saves this document to file.
     * 将此文档保存到文件。
     *
     * @param path the file path | 文件路径
     */
    public void save(Path path) {
        try {
            Files.writeString(path, toXml(4));
        } catch (IOException e) {
            throw new OpenXmlException("Failed to save document to: " + path, e);
        }
    }

    /**
     * Converts this document to a Map.
     * 将此文档转换为 Map。
     *
     * @return the Map representation | Map 表示
     */
    public Map<String, Object> toMap() {
        XmlElement root = getRoot();
        return root != null ? root.toMap() : Map.of();
    }

    @Override
    public String toString() {
        return toXml();
    }
}
