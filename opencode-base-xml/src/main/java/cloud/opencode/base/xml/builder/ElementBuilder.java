package cloud.opencode.base.xml.builder;

import cloud.opencode.base.xml.XmlElement;
import cloud.opencode.base.xml.dom.DomUtil;
import cloud.opencode.base.xml.exception.OpenXmlException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.function.Consumer;

/**
 * Element Builder - Fluent builder for XML elements
 * 元素构建器 - XML 元素的流式构建器
 *
 * <p>This class provides a fluent API for building standalone XML elements.</p>
 * <p>此类提供流式 API，用于构建独立的 XML 元素。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent builder pattern for XML elements - XML 元素的流式构建器模式</li>
 *   <li>Attribute, child element, text, CDATA, comment support - 属性、子元素、文本、CDATA、注释支持</li>
 *   <li>Nested element building with startChild/endChild - 通过 startChild/endChild 构建嵌套元素</li>
 *   <li>Namespace-aware element creation - 支持命名空间的元素创建</li>
 *   <li>Consumer-based child configuration - 基于 Consumer 的子元素配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Build a simple element
 * XmlElement element = ElementBuilder.create("user")
 *     .attribute("id", "1")
 *     .child("name", "John Doe")
 *     .child("email", "john@example.com")
 *     .build();
 *
 * // Build nested elements
 * XmlElement element = ElementBuilder.create("user")
 *     .attribute("id", "1")
 *     .startChild("address")
 *         .child("city", "New York")
 *         .child("country", "USA")
 *     .endChild()
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable builder state) - 线程安全: 否（可变构建器状态）</li>
 *   <li>Null-safe: Partial (null values are skipped for attributes and text) - 空值安全: 部分（属性和文本的空值被跳过）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) overall where n=number of children/attributes added; each individual operation is O(1) - 时间复杂度: 总体 O(n)，n 为添加的子元素/属性数量；每次单独操作为 O(1)</li>
 *   <li>Space complexity: O(n) for the DOM subtree built; no auxiliary data structures beyond the DOM itself - 空间复杂度: 构建的 DOM 子树为 O(n)；除 DOM 自身外无额外数据结构</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class ElementBuilder {

    private final Document document;
    private final Element element;
    private final ElementBuilder parent;

    private ElementBuilder(String name) {
        this.document = DomUtil.createDocument();
        this.element = document.createElement(name);
        this.parent = null;
    }

    private ElementBuilder(String namespaceURI, String name) {
        this.document = DomUtil.createDocument();
        this.element = document.createElementNS(namespaceURI, name);
        this.parent = null;
    }

    private ElementBuilder(ElementBuilder parent, Element element) {
        this.document = parent.document;
        this.element = element;
        this.parent = parent;
    }

    /**
     * Creates a new element builder with the given name.
     * 使用给定名称创建新的元素构建器。
     *
     * @param name the element name | 元素名称
     * @return a new builder | 新构建器
     */
    public static ElementBuilder create(String name) {
        if (name == null || name.isBlank()) {
            throw new OpenXmlException("Element name cannot be null or empty");
        }
        return new ElementBuilder(name);
    }

    /**
     * Creates a new element builder with namespace.
     * 使用命名空间创建新的元素构建器。
     *
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @param name         the element name | 元素名称
     * @return a new builder | 新构建器
     */
    public static ElementBuilder create(String namespaceURI, String name) {
        if (name == null || name.isBlank()) {
            throw new OpenXmlException("Element name cannot be null or empty");
        }
        return new ElementBuilder(namespaceURI, name);
    }

    // ==================== Attributes | 属性 ====================

    /**
     * Adds an attribute to the element.
     * 向元素添加属性。
     *
     * @param name  the attribute name | 属性名称
     * @param value the attribute value | 属性值
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public ElementBuilder attribute(String name, String value) {
        if (value != null) {
            element.setAttribute(name, value);
        }
        return this;
    }

    /**
     * Adds an attribute with namespace.
     * 添加带命名空间的属性。
     *
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @param name         the attribute name | 属性名称
     * @param value        the attribute value | 属性值
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public ElementBuilder attribute(String namespaceURI, String name, String value) {
        if (value != null) {
            element.setAttributeNS(namespaceURI, name, value);
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
    public ElementBuilder attribute(String name, Number value) {
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
    public ElementBuilder attribute(String name, boolean value) {
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
    public ElementBuilder attributeIfNotNull(String name, String value) {
        if (value != null) {
            return attribute(name, value);
        }
        return this;
    }

    // ==================== Namespace | 命名空间 ====================

    /**
     * Adds a namespace declaration.
     * 添加命名空间声明。
     *
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @param prefix       the prefix | 前缀
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public ElementBuilder namespace(String namespaceURI, String prefix) {
        element.setAttributeNS("http://www.w3.org/2000/xmlns/",
            "xmlns:" + prefix, namespaceURI);
        return this;
    }

    /**
     * Sets the default namespace.
     * 设置默认命名空间。
     *
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public ElementBuilder defaultNamespace(String namespaceURI) {
        element.setAttribute("xmlns", namespaceURI);
        return this;
    }

    // ==================== Child Elements | 子元素 ====================

    /**
     * Adds a child element with text content.
     * 添加带文本内容的子元素。
     *
     * @param name the element name | 元素名称
     * @param text the text content | 文本内容
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public ElementBuilder child(String name, String text) {
        Element child = document.createElement(name);
        if (text != null) {
            child.setTextContent(text);
        }
        element.appendChild(child);
        return this;
    }

    /**
     * Adds a child element with number content.
     * 添加带数字内容的子元素。
     *
     * @param name  the element name | 元素名称
     * @param value the number value | 数字值
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public ElementBuilder child(String name, Number value) {
        return child(name, value != null ? value.toString() : null);
    }

    /**
     * Adds a child element with boolean content.
     * 添加带布尔内容的子元素。
     *
     * @param name  the element name | 元素名称
     * @param value the boolean value | 布尔值
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public ElementBuilder child(String name, boolean value) {
        return child(name, String.valueOf(value));
    }

    /**
     * Adds a child element if value is not null.
     * 如果值不为 null 则添加子元素。
     *
     * @param name the element name | 元素名称
     * @param text the text content | 文本内容
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public ElementBuilder childIfNotNull(String name, String text) {
        if (text != null) {
            return child(name, text);
        }
        return this;
    }

    /**
     * Adds an empty child element.
     * 添加空子元素。
     *
     * @param name the element name | 元素名称
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public ElementBuilder emptyChild(String name) {
        Element child = document.createElement(name);
        element.appendChild(child);
        return this;
    }

    /**
     * Starts building a child element.
     * 开始构建子元素。
     *
     * @param name the element name | 元素名称
     * @return a new builder for the child | 子元素的新构建器
     */
    public ElementBuilder startChild(String name) {
        Element child = document.createElement(name);
        element.appendChild(child);
        return new ElementBuilder(this, child);
    }

    /**
     * Starts building a child element with namespace.
     * 开始构建带命名空间的子元素。
     *
     * @param namespaceURI the namespace URI | 命名空间 URI
     * @param name         the element name | 元素名称
     * @return a new builder for the child | 子元素的新构建器
     */
    public ElementBuilder startChild(String namespaceURI, String name) {
        Element child = document.createElementNS(namespaceURI, name);
        element.appendChild(child);
        return new ElementBuilder(this, child);
    }

    /**
     * Ends building the current child and returns to the parent.
     * 结束构建当前子元素并返回到父元素。
     *
     * @return the parent builder | 父构建器
     */
    public ElementBuilder endChild() {
        if (parent != null) {
            return parent;
        }
        return this;
    }

    /**
     * Adds a built XmlElement as a child.
     * 添加已构建的 XmlElement 作为子元素。
     *
     * @param xmlElement the element to add | 要添加的元素
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public ElementBuilder child(XmlElement xmlElement) {
        Element imported = (Element) document.importNode(xmlElement.getElement(), true);
        element.appendChild(imported);
        return this;
    }

    /**
     * Configures a child element using a consumer.
     * 使用消费者配置子元素。
     *
     * @param name       the element name | 元素名称
     * @param configurer the element configurer | 元素配置器
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public ElementBuilder child(String name, Consumer<ElementBuilder> configurer) {
        ElementBuilder childBuilder = startChild(name);
        configurer.accept(childBuilder);
        return childBuilder.endChild();
    }

    // ==================== Content | 内容 ====================

    /**
     * Sets the text content of the element.
     * 设置元素的文本内容。
     *
     * @param text the text content | 文本内容
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public ElementBuilder text(String text) {
        if (text != null) {
            element.appendChild(document.createTextNode(text));
        }
        return this;
    }

    /**
     * Adds CDATA content.
     * 添加 CDATA 内容。
     *
     * @param data the CDATA content | CDATA 内容
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public ElementBuilder cdata(String data) {
        if (data != null) {
            // Split on "]]>" to produce valid CDATA sections
            // "]]>" is illegal inside CDATA; split into multiple CDATA sections
            String[] parts = data.split("\\]\\]>", -1);
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    // Re-insert the "]]>" split across two CDATA sections:
                    // end previous with "]]" and start next with ">"
                    element.appendChild(document.createCDATASection("]]"));
                    element.appendChild(document.createCDATASection(">" + parts[i]));
                } else {
                    element.appendChild(document.createCDATASection(parts[i]));
                }
            }
        }
        return this;
    }

    /**
     * Adds a comment.
     * 添加注释。
     *
     * @param comment the comment text | 注释文本
     * @return this builder for chaining | 此构建器以便链式调用
     */
    public ElementBuilder comment(String comment) {
        if (comment != null) {
            element.appendChild(document.createComment(comment));
        }
        return this;
    }

    // ==================== Build | 构建 ====================

    /**
     * Builds and returns the XmlElement.
     * 构建并返回 XmlElement。
     *
     * @return the built element | 构建的元素
     */
    public XmlElement build() {
        return XmlElement.of(element);
    }

    /**
     * Gets the underlying DOM Element.
     * 获取底层 DOM 元素。
     *
     * @return the DOM Element | DOM 元素
     */
    public Element unwrap() {
        return element;
    }

    /**
     * Converts the element to an XML string.
     * 将元素转换为 XML 字符串。
     *
     * @return the XML string | XML 字符串
     */
    public String toXml() {
        return DomUtil.toString(element);
    }

    /**
     * Converts the element to a formatted XML string.
     * 将元素转换为格式化的 XML 字符串。
     *
     * @param indent the indent spaces | 缩进空格数
     * @return the XML string | XML 字符串
     */
    public String toXml(int indent) {
        return DomUtil.toString(element, indent);
    }
}
