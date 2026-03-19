package cloud.opencode.base.xml.builder;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.XmlElement;
import cloud.opencode.base.xml.dom.DomBuilder;
import cloud.opencode.base.xml.exception.OpenXmlException;
import org.w3c.dom.Document;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

/**
 * XML Builder - Fluent builder for XML documents
 * XML 构建器 - XML 文档的流式构建器
 *
 * <p>This class provides a fluent API for building XML documents programmatically.</p>
 * <p>此类提供流式 API，用于以编程方式构建 XML 文档。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent document building with element stack - 带元素栈的流式文档构建</li>
 *   <li>Namespace, encoding, version, standalone configuration - 命名空间、编码、版本、独立声明配置</li>
 *   <li>Element, attribute, text, CDATA, comment, PI support - 元素、属性、文本、CDATA、注释、处理指令支持</li>
 *   <li>Conditional element/attribute insertion - 条件性元素/属性插入</li>
 *   <li>Consumer-based element configuration - 基于 Consumer 的元素配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Build a simple XML document
 * XmlDocument doc = XmlBuilder.create("users")
 *     .encoding("UTF-8")
 *     .startElement("user")
 *         .attribute("id", "1")
 *         .element("name", "John Doe")
 *         .element("email", "john@example.com")
 *     .end()
 *     .build();
 *
 * // With namespace
 * XmlDocument doc = XmlBuilder.create("http://example.com", "users")
 *     .namespace("http://example.com", "ex")
 *     .startElement("user")
 *         .attribute("id", "1")
 *     .end()
 *     .build();
 *
 * // With formatted output
 * String xml = XmlBuilder.create("root")
 *     .element("child", "value")
 *     .build()
 *     .toXml(4);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable builder state with element stack) - 线程安全: 否（带元素栈的可变构建器状态）</li>
 *   <li>Null-safe: Partial (null values are skipped for attributes and text) - 空值安全: 部分（属性和文本的空值被跳过）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) overall for building a document with n elements; each startElement/element/attribute call is O(1) amortized - 时间复杂度: 构建含 n 个元素的文档总体为 O(n)；每次 startElement/element/attribute 调用摊销为 O(1)</li>
 *   <li>Space complexity: O(d) for the element stack where d=current nesting depth, plus O(n) for the DOM tree - 空间复杂度: 元素栈 O(d)，d 为当前嵌套深度，加上 DOM 树 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class XmlBuilder {

    private final DomBuilder domBuilder;
    private final Deque<org.w3c.dom.Element> elementStack = new ArrayDeque<>();
    private String encoding = "UTF-8";
    private String version = "1.0";
    private boolean standalone = false;
    private boolean hasStandalone = false;

    private XmlBuilder(String rootName) {
        this.domBuilder = DomBuilder.create(rootName);
        this.elementStack.push(domBuilder.getRootElement());
    }

    private XmlBuilder(String namespaceURI, String rootName) {
        this.domBuilder = DomBuilder.create(rootName);
        Document doc = domBuilder.getDocument();
        org.w3c.dom.Element root = doc.createElementNS(namespaceURI, rootName);
        doc.removeChild(doc.getDocumentElement());
        doc.appendChild(root);
        this.elementStack.push(root);
    }

    /**
     * Creates a new XML builder with the given root element name.
     * 使用给定的根元素名称创建新的 XML 构建器。
     *
     * @param rootName the root element name | 根元素名称
     * @return a new builder | 新构建器
     */
    public static XmlBuilder create(String rootName) {
        if (rootName == null || rootName.isBlank()) {
            throw new OpenXmlException("Root element name cannot be null or empty");
        }
        return new XmlBuilder(rootName);
    }

    /**
     * Creates a new XML builder with namespace.
     * 使用命名空间创建新的 XML 构建器。
     *
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @param rootName     the root element name | 根元素名称
     * @return a new builder | 新构建器
     */
    public static XmlBuilder create(String namespaceURI, String rootName) {
        if (rootName == null || rootName.isBlank()) {
            throw new OpenXmlException("Root element name cannot be null or empty");
        }
        return new XmlBuilder(namespaceURI, rootName);
    }

    // ==================== Configuration | 配置 ====================

    /**
     * Sets the XML encoding.
     * 设置 XML 编码。
     *
     * @param encoding the encoding | 编码
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder encoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    /**
     * Sets the XML version.
     * 设置 XML 版本。
     *
     * @param version the version | 版本
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder version(String version) {
        this.version = version;
        return this;
    }

    /**
     * Sets the standalone declaration.
     * 设置独立声明。
     *
     * @param standalone whether standalone | 是否独立
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder standalone(boolean standalone) {
        this.standalone = standalone;
        this.hasStandalone = true;
        return this;
    }

    // ==================== Namespace | 命名空间 ====================

    /**
     * Adds a namespace declaration to the current element.
     * 向当前元素添加命名空间声明。
     *
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @param prefix       the prefix | 前缀
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder namespace(String namespaceURI, String prefix) {
        org.w3c.dom.Element current = elementStack.peek();
        if (current != null) {
            current.setAttributeNS("http://www.w3.org/2000/xmlns/",
                "xmlns:" + prefix, namespaceURI);
        }
        return this;
    }

    /**
     * Sets the default namespace on the current element.
     * 在当前元素上设置默认命名空间。
     *
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder defaultNamespace(String namespaceURI) {
        org.w3c.dom.Element current = elementStack.peek();
        if (current != null) {
            current.setAttribute("xmlns", namespaceURI);
        }
        return this;
    }

    // ==================== Elements | 元素 ====================

    /**
     * Starts a new child element.
     * 开始新的子元素。
     *
     * @param name the element name | 元素名称
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder startElement(String name) {
        Document doc = domBuilder.getDocument();
        org.w3c.dom.Element newElement = doc.createElement(name);
        org.w3c.dom.Element current = elementStack.peek();
        if (current != null) {
            current.appendChild(newElement);
        }
        elementStack.push(newElement);
        return this;
    }

    /**
     * Starts a new child element with namespace.
     * 开始带命名空间的新子元素。
     *
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @param name         the element name | 元素名称
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder startElement(String namespaceURI, String name) {
        Document doc = domBuilder.getDocument();
        org.w3c.dom.Element newElement = doc.createElementNS(namespaceURI, name);
        org.w3c.dom.Element current = elementStack.peek();
        if (current != null) {
            current.appendChild(newElement);
        }
        elementStack.push(newElement);
        return this;
    }

    /**
     * Starts a new child element with prefix and namespace.
     * 开始带前缀和命名空间的新子元素。
     *
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @param prefix       the prefix | 前缀
     * @param localName    the local name | 本地名称
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder startElement(String namespaceURI, String prefix, String localName) {
        Document doc = domBuilder.getDocument();
        String qName = prefix.isEmpty() ? localName : prefix + ":" + localName;
        org.w3c.dom.Element newElement = doc.createElementNS(namespaceURI, qName);
        org.w3c.dom.Element current = elementStack.peek();
        if (current != null) {
            current.appendChild(newElement);
        }
        elementStack.push(newElement);
        return this;
    }

    /**
     * Ends the current element and returns to the parent.
     * 结束当前元素并返回到父元素。
     *
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder end() {
        if (elementStack.size() > 1) {
            elementStack.pop();
        }
        return this;
    }

    /**
     * Adds a complete element with text content.
     * 添加带文本内容的完整元素。
     *
     * @param name the element name | 元素名称
     * @param text the text content | 文本内容
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder element(String name, String text) {
        startElement(name);
        text(text);
        return end();
    }

    /**
     * Adds a complete element with number content.
     * 添加带数字内容的完整元素。
     *
     * @param name  the element name | 元素名称
     * @param value the number value | 数字值
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder element(String name, Number value) {
        return element(name, value != null ? value.toString() : null);
    }

    /**
     * Adds a complete element with boolean content.
     * 添加带布尔内容的完整元素。
     *
     * @param name  the element name | 元素名称
     * @param value the boolean value | 布尔值
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder element(String name, boolean value) {
        return element(name, String.valueOf(value));
    }

    /**
     * Adds a complete element if value is not null.
     * 如果值不为 null 则添加完整元素。
     *
     * @param name the element name | 元素名称
     * @param text the text content | 文本内容
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder elementIfNotNull(String name, String text) {
        if (text != null) {
            return element(name, text);
        }
        return this;
    }

    /**
     * Adds an empty element.
     * 添加空元素。
     *
     * @param name the element name | 元素名称
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder emptyElement(String name) {
        startElement(name);
        return end();
    }

    /**
     * Configures the current element using a consumer.
     * 使用消费者配置当前元素。
     *
     * @param configurer the element configurer | 元素配置器
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder configure(Consumer<XmlBuilder> configurer) {
        configurer.accept(this);
        return this;
    }

    // ==================== Attributes | 属性 ====================

    /**
     * Adds an attribute to the current element.
     * 向当前元素添加属性。
     *
     * @param name  the attribute name | 属性名称
     * @param value the attribute value | 属性值
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder attribute(String name, String value) {
        org.w3c.dom.Element current = elementStack.peek();
        if (current != null && value != null) {
            current.setAttribute(name, value);
        }
        return this;
    }

    /**
     * Adds an attribute with namespace to the current element.
     * 向当前元素添加带命名空间的属性。
     *
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @param name         the attribute name | 属性名称
     * @param value        the attribute value | 属性值
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder attribute(String namespaceURI, String name, String value) {
        org.w3c.dom.Element current = elementStack.peek();
        if (current != null && value != null) {
            current.setAttributeNS(namespaceURI, name, value);
        }
        return this;
    }

    /**
     * Adds an attribute with number value.
     * 添加带数字值的属性。
     *
     * @param name  the attribute name | 属性名称
     * @param value the number value | 数字值
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder attribute(String name, Number value) {
        return attribute(name, value != null ? value.toString() : null);
    }

    /**
     * Adds an attribute with boolean value.
     * 添加带布尔值的属性。
     *
     * @param name  the attribute name | 属性名称
     * @param value the boolean value | 布尔值
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder attribute(String name, boolean value) {
        return attribute(name, String.valueOf(value));
    }

    /**
     * Adds an attribute if value is not null.
     * 如果值不为 null 则添加属性。
     *
     * @param name  the attribute name | 属性名称
     * @param value the attribute value | 属性值
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder attributeIfNotNull(String name, String value) {
        if (value != null) {
            return attribute(name, value);
        }
        return this;
    }

    // ==================== Content | 内容 ====================

    /**
     * Adds text content to the current element.
     * 向当前元素添加文本内容。
     *
     * @param text the text content | 文本内容
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder text(String text) {
        if (text != null) {
            Document doc = domBuilder.getDocument();
            org.w3c.dom.Element current = elementStack.peek();
            if (current != null) {
                current.appendChild(doc.createTextNode(text));
            }
        }
        return this;
    }

    /**
     * Adds CDATA content to the current element.
     * 向当前元素添加 CDATA 内容。
     *
     * @param data the CDATA content | CDATA 内容
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder cdata(String data) {
        if (data != null) {
            Document doc = domBuilder.getDocument();
            org.w3c.dom.Element current = elementStack.peek();
            if (current != null) {
                // Split on "]]>" to produce valid CDATA sections
                // "]]>" is illegal inside CDATA; split into multiple CDATA sections
                String[] parts = data.split("\\]\\]>", -1);
                for (int i = 0; i < parts.length; i++) {
                    if (i > 0) {
                        // Re-insert the "]]>" split across two CDATA sections:
                        // end previous with "]]" and start next with ">"
                        current.appendChild(doc.createCDATASection("]]"));
                        current.appendChild(doc.createCDATASection(">" + parts[i]));
                    } else {
                        current.appendChild(doc.createCDATASection(parts[i]));
                    }
                }
            }
        }
        return this;
    }

    /**
     * Adds a comment to the current element.
     * 向当前元素添加注释。
     *
     * @param comment the comment text | 注释文本
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder comment(String comment) {
        if (comment != null) {
            Document doc = domBuilder.getDocument();
            org.w3c.dom.Element current = elementStack.peek();
            if (current != null) {
                current.appendChild(doc.createComment(comment));
            }
        }
        return this;
    }

    /**
     * Adds a processing instruction.
     * 添加处理指令。
     *
     * @param target the target | 目标
     * @param data   the data | 数据
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public XmlBuilder processingInstruction(String target, String data) {
        Document doc = domBuilder.getDocument();
        doc.appendChild(doc.createProcessingInstruction(target, data));
        return this;
    }

    // ==================== Build | 构建 ====================

    /**
     * Builds and returns the XML document.
     * 构建并返回 XML 文档。
     *
     * @return the built document | 构建的文档
     */
    public XmlDocument build() {
        Document doc = domBuilder.getDocument();
        doc.setXmlVersion(version);
        if (hasStandalone) {
            doc.setXmlStandalone(standalone);
        }
        return XmlDocument.of(doc);
    }

    /**
     * Builds and returns the root element.
     * 构建并返回根元素。
     *
     * @return the root element | 根元素
     */
    public XmlElement buildElement() {
        return build().getRoot();
    }

    /**
     * Builds and returns the XML string.
     * 构建并返回 XML 字符串。
     *
     * @return the XML string | XML 字符串
     */
    public String toXml() {
        return build().toXml();
    }

    /**
     * Builds and returns the formatted XML string.
     * 构建并返回格式化的 XML 字符串。
     *
     * @param indent the indent spaces | 缩进空格数
     * @return the XML string | XML 字符串
     */
    public String toXml(int indent) {
        return build().toXml(indent);
    }

    /**
     * Gets the underlying DOM Document.
     * 获取底层 DOM Document。
     *
     * @return the DOM Document | DOM 文档
     */
    public Document getDocument() {
        return domBuilder.getDocument();
    }

    /**
     * Gets the current element being built.
     * 获取正在构建的当前元素。
     *
     * @return the current element | 当前元素
     */
    public org.w3c.dom.Element getCurrentElement() {
        return elementStack.peek();
    }
}
