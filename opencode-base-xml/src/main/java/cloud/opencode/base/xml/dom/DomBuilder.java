package cloud.opencode.base.xml.dom;

import cloud.opencode.base.xml.exception.OpenXmlException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * DOM Builder - Builder for creating DOM documents programmatically
 * DOM 构建器 - 程序化创建 DOM 文档的构建器
 *
 * <p>This class provides a fluent API for building DOM documents without
 * parsing XML strings.</p>
 * <p>此类提供流式 API，无需解析 XML 字符串即可构建 DOM 文档。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Programmatic DOM document construction without parsing - 无需解析即可程序化构建 DOM 文档</li>
 *   <li>Fluent chaining API for elements, attributes, text, CDATA, comments - 元素、属性、文本、CDATA、注释的流式链式 API</li>
 *   <li>Namespace-aware element creation - 支持命名空间的元素创建</li>
 *   <li>Node import from other documents - 从其他文档导入节点</li>
 *   <li>Processing instruction support - 处理指令支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Document doc = DomBuilder.create("root")
 *     .addElement("name", "John")
 *     .addElement("age", "30")
 *     .startElement("address")
 *         .addElement("city", "Beijing")
 *         .addElement("country", "China")
 *     .endElement()
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable builder state) - 线程安全: 否（可变构建器状态）</li>
 *   <li>Null-safe: Partial (null text values are allowed and skipped) - 空值安全: 部分（空文本值被允许并跳过）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for complete document construction where n=total elements; each addElement/startElement call is O(1) - 时间复杂度: 完整文档构建为 O(n)，n 为元素总数；每次 addElement/startElement 调用为 O(1)</li>
 *   <li>Space complexity: O(n) for the DOM tree in memory - 空间复杂度: 内存中 DOM 树为 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class DomBuilder {

    private final Document document;
    private final Element root;
    private Element current;

    private DomBuilder(String rootName) {
        this.document = DomParser.createDocument();
        this.root = document.createElement(rootName);
        document.appendChild(root);
        this.current = root;
    }

    private DomBuilder(String rootName, String namespaceUri) {
        this.document = DomParser.createDocument();
        this.root = document.createElementNS(namespaceUri, rootName);
        document.appendChild(root);
        this.current = root;
    }

    /**
     * Creates a new DOM builder with the specified root element name.
     * 使用指定的根元素名称创建新的 DOM 构建器。
     *
     * @param rootName the root element name | 根元素名称
     * @return a new builder | 新构建器
     */
    public static DomBuilder create(String rootName) {
        return new DomBuilder(rootName);
    }

    /**
     * Creates a new DOM builder with namespace.
     * 创建带命名空间的新 DOM 构建器。
     *
     * @param rootName     the root element name | 根元素名称
     * @param namespaceUri the namespace URI | 命名空间 URI
     * @return a new builder | 新构建器
     */
    public static DomBuilder create(String rootName, String namespaceUri) {
        return new DomBuilder(rootName, namespaceUri);
    }

    /**
     * Adds an attribute to the current element.
     * 向当前元素添加属性。
     *
     * @param name  the attribute name | 属性名
     * @param value the attribute value | 属性值
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public DomBuilder attribute(String name, String value) {
        current.setAttribute(name, value);
        return this;
    }

    /**
     * Adds a child element with text content to the current element.
     * 向当前元素添加带文本内容的子元素。
     *
     * @param name the element name | 元素名称
     * @param text the text content | 文本内容
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public DomBuilder addElement(String name, String text) {
        Element elem = document.createElement(name);
        if (text != null) {
            elem.setTextContent(text);
        }
        current.appendChild(elem);
        return this;
    }

    /**
     * Adds a child element with text content and attribute.
     * 添加带文本内容和属性的子元素。
     *
     * @param name      the element name | 元素名称
     * @param text      the text content | 文本内容
     * @param attrName  the attribute name | 属性名
     * @param attrValue the attribute value | 属性值
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public DomBuilder addElement(String name, String text, String attrName, String attrValue) {
        Element elem = document.createElement(name);
        if (text != null) {
            elem.setTextContent(text);
        }
        elem.setAttribute(attrName, attrValue);
        current.appendChild(elem);
        return this;
    }

    /**
     * Starts a new child element and makes it the current element.
     * 开始新的子元素并将其设为当前元素。
     *
     * @param name the element name | 元素名称
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public DomBuilder startElement(String name) {
        Element elem = document.createElement(name);
        current.appendChild(elem);
        current = elem;
        return this;
    }

    /**
     * Starts a new child element with namespace.
     * 开始带命名空间的新子元素。
     *
     * @param name         the element name | 元素名称
     * @param namespaceUri the namespace URI | 命名空间 URI
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public DomBuilder startElement(String name, String namespaceUri) {
        Element elem = document.createElementNS(namespaceUri, name);
        current.appendChild(elem);
        current = elem;
        return this;
    }

    /**
     * Ends the current element and returns to its parent.
     * 结束当前元素并返回其父元素。
     *
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public DomBuilder endElement() {
        Node parent = current.getParentNode();
        if (parent instanceof Element parentElem) {
            current = parentElem;
        } else {
            current = root;
        }
        return this;
    }

    /**
     * Sets the text content of the current element.
     * 设置当前元素的文本内容。
     *
     * @param text the text content | 文本内容
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public DomBuilder text(String text) {
        current.setTextContent(text);
        return this;
    }

    /**
     * Adds a CDATA section to the current element.
     * 向当前元素添加 CDATA 节。
     *
     * @param text the CDATA content | CDATA 内容
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public DomBuilder cdata(String text) {
        current.appendChild(document.createCDATASection(text));
        return this;
    }

    /**
     * Adds a comment to the current element.
     * 向当前元素添加注释。
     *
     * @param comment the comment text | 注释文本
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public DomBuilder comment(String comment) {
        current.appendChild(document.createComment(comment));
        return this;
    }

    /**
     * Adds a processing instruction to the document.
     * 向文档添加处理指令。
     *
     * @param target the target | 目标
     * @param data   the data | 数据
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public DomBuilder processingInstruction(String target, String data) {
        document.insertBefore(
            document.createProcessingInstruction(target, data),
            root
        );
        return this;
    }

    /**
     * Imports and appends a node from another document.
     * 从另一个文档导入并附加节点。
     *
     * @param node the node to import | 要导入的节点
     * @param deep whether to import children | 是否导入子节点
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public DomBuilder importNode(Node node, boolean deep) {
        Node imported = document.importNode(node, deep);
        current.appendChild(imported);
        return this;
    }

    /**
     * Returns to the root element.
     * 返回根元素。
     *
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public DomBuilder returnToRoot() {
        current = root;
        return this;
    }

    /**
     * Gets the underlying Document (alias for build()).
     * 获取底层 Document（build() 的别名）。
     *
     * @return the Document | Document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Gets the root element.
     * 获取根元素。
     *
     * @return the root element | 根元素
     */
    public Element getRootElement() {
        return root;
    }

    /**
     * Builds and returns the Document.
     * 构建并返回 Document。
     *
     * @return the built Document | 构建的 Document
     */
    public Document build() {
        return document;
    }

    /**
     * Builds and returns the Document as XML string.
     * 构建并返回 Document 为 XML 字符串。
     *
     * @return the XML string | XML 字符串
     */
    public String toXml() {
        return DomUtil.toString(document);
    }

    /**
     * Builds and returns the Document as formatted XML string.
     * 构建并返回 Document 为格式化的 XML 字符串。
     *
     * @param indent the number of spaces for indentation | 缩进空格数
     * @return the formatted XML string | 格式化的 XML 字符串
     */
    public String toXml(int indent) {
        return DomUtil.toString(document, indent);
    }
}
