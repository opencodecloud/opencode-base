package cloud.opencode.base.xml;

import cloud.opencode.base.xml.bind.XmlBinder;
import cloud.opencode.base.xml.dom.DomUtil;
import cloud.opencode.base.xml.exception.OpenXmlException;
import cloud.opencode.base.xml.xpath.OpenXPath;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * XML Element - Wrapper for DOM Element with fluent API
 * XML 元素 - 提供流式 API 的 DOM Element 封装
 *
 * <p>This class wraps a DOM Element and provides convenient methods for
 * accessing and manipulating XML content.</p>
 * <p>此类封装 DOM Element，并提供便捷的方法来访问和操作 XML 内容。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent API for element access and modification - 元素访问和修改的流式 API</li>
 *   <li>Attribute read/write operations - 属性读写操作</li>
 *   <li>Child element navigation and manipulation - 子元素导航和操作</li>
 *   <li>XPath queries relative to this element - 相对于此元素的 XPath 查询</li>
 *   <li>Type-safe text conversion - 类型安全的文本转换</li>
 *   <li>Object binding support - 对象绑定支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * XmlElement element = XmlElement.of(domElement);
 *
 * // Get element info
 * String name = element.getName();
 * String text = element.getText();
 *
 * // Access attributes
 * String id = element.getAttribute("id");
 * Map<String, String> attrs = element.getAttributes();
 *
 * // Access children
 * XmlElement child = element.getChild("name");
 * List<XmlElement> items = element.getChildren("item");
 *
 * // XPath queries
 * String value = element.xpath("./name/text()");
 * List<XmlElement> results = element.xpathList(".//item");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (wraps mutable DOM Element) - 线程安全: 否（封装可变的 DOM Element）</li>
 *   <li>Null-safe: No (null inputs throw exceptions) - 空值安全: 否（空值输入抛出异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class XmlElement implements XmlNode {

    private final Element element;

    private XmlElement(Element element) {
        this.element = Objects.requireNonNull(element, "Element must not be null");
    }

    /**
     * Creates an XmlElement from a DOM Element.
     * 从 DOM Element 创建 XmlElement。
     *
     * @param element the DOM Element | DOM Element
     * @return the XmlElement | XmlElement
     */
    public static XmlElement of(Element element) {
        return new XmlElement(element);
    }

    /**
     * Creates an XmlElement from a DOM Node (if it's an Element).
     * 从 DOM Node 创建 XmlElement（如果是 Element）。
     *
     * @param node the DOM Node | DOM Node
     * @return the XmlElement | XmlElement
     * @throws OpenXmlException if node is not an Element | 如果节点不是 Element 则抛出异常
     */
    public static XmlElement of(Node node) {
        if (node instanceof Element elem) {
            return new XmlElement(elem);
        }
        throw new OpenXmlException("Node is not an Element: " + node.getNodeType());
    }

    /**
     * Returns the underlying DOM Element.
     * 返回底层的 DOM Element。
     *
     * @return the DOM Element | DOM Element
     */
    public Element getElement() {
        return element;
    }

    @Override
    public Node getNode() {
        return element;
    }

    // ==================== Basic Information | 基本信息 ====================

    @Override
    public String getName() {
        return element.getTagName();
    }

    @Override
    public String getLocalName() {
        String localName = element.getLocalName();
        return localName != null ? localName : element.getTagName();
    }

    @Override
    public String getNamespaceUri() {
        return element.getNamespaceURI();
    }

    @Override
    public String getPrefix() {
        return element.getPrefix();
    }

    // ==================== Text Content | 文本内容 ====================

    @Override
    public String getText() {
        return element.getTextContent();
    }

    @Override
    public String getTextTrim() {
        String text = getText();
        return text != null ? text.trim() : "";
    }

    @Override
    public boolean hasText() {
        String text = getTextTrim();
        return text != null && !text.isEmpty();
    }

    /**
     * Sets the text content.
     * 设置文本内容。
     *
     * @param text the text content | 文本内容
     * @return this element for chaining | 此元素以便链式调用
     */
    public XmlElement setText(String text) {
        element.setTextContent(text);
        return this;
    }

    /**
     * Returns the text content converted to the specified type.
     * 返回转换为指定类型的文本内容。
     *
     * @param <T>   the type parameter | 类型参数
     * @param clazz the target type | 目标类型
     * @return the converted value | 转换后的值
     */
    public <T> T getTextAs(Class<T> clazz) {
        return DomUtil.convertText(getText(), clazz);
    }

    /**
     * Returns the text content or default value if null/empty.
     * 返回文本内容，如果为 null/空则返回默认值。
     *
     * @param defaultValue the default value | 默认值
     * @return the text content or default | 文本内容或默认值
     */
    public String getText(String defaultValue) {
        String text = getTextTrim();
        return (text != null && !text.isEmpty()) ? text : defaultValue;
    }

    // ==================== Attribute Operations | 属性操作 ====================

    /**
     * Returns the attribute value.
     * 返回属性值。
     *
     * @param name the attribute name | 属性名
     * @return the attribute value, or null if not present | 属性值，如果不存在则返回 null
     */
    public String getAttribute(String name) {
        if (element.hasAttribute(name)) {
            return element.getAttribute(name);
        }
        return null;
    }

    /**
     * Returns the attribute value or default.
     * 返回属性值或默认值。
     *
     * @param name         the attribute name | 属性名
     * @param defaultValue the default value | 默认值
     * @return the attribute value or default | 属性值或默认值
     */
    public String getAttribute(String name, String defaultValue) {
        String value = getAttribute(name);
        return value != null ? value : defaultValue;
    }

    /**
     * Returns the attribute value converted to the specified type.
     * 返回转换为指定类型的属性值。
     *
     * @param <T>   the type parameter | 类型参数
     * @param name  the attribute name | 属性名
     * @param clazz the target type | 目标类型
     * @return the converted value | 转换后的值
     */
    public <T> T getAttribute(String name, Class<T> clazz) {
        return DomUtil.convertText(getAttribute(name), clazz);
    }

    /**
     * Returns all attributes as a Map.
     * 返回所有属性为 Map。
     *
     * @return the attributes Map | 属性 Map
     */
    public Map<String, String> getAttributes() {
        Map<String, String> attrs = new LinkedHashMap<>();
        var namedNodeMap = element.getAttributes();
        for (int i = 0; i < namedNodeMap.getLength(); i++) {
            Node attr = namedNodeMap.item(i);
            attrs.put(attr.getNodeName(), attr.getNodeValue());
        }
        return attrs;
    }

    /**
     * Sets an attribute value.
     * 设置属性值。
     *
     * @param name  the attribute name | 属性名
     * @param value the attribute value | 属性值
     * @return this element for chaining | 此元素以便链式调用
     */
    public XmlElement setAttribute(String name, String value) {
        element.setAttribute(name, value);
        return this;
    }

    /**
     * Removes an attribute.
     * 移除属性。
     *
     * @param name the attribute name | 属性名
     * @return this element for chaining | 此元素以便链式调用
     */
    public XmlElement removeAttribute(String name) {
        element.removeAttribute(name);
        return this;
    }

    /**
     * Returns whether this element has the specified attribute.
     * 返回此元素是否具有指定的属性。
     *
     * @param name the attribute name | 属性名
     * @return true if attribute exists | 如果属性存在则返回 true
     */
    public boolean hasAttribute(String name) {
        return element.hasAttribute(name);
    }

    // ==================== Child Element Operations | 子元素操作 ====================

    /**
     * Returns the first child element with the specified name.
     * 返回具有指定名称的第一个子元素。
     *
     * @param name the child element name | 子元素名称
     * @return the child element, or null if not found | 子元素，如果未找到则返回 null
     */
    public XmlElement getChild(String name) {
        NodeList children = element.getElementsByTagName(name);
        if (children.getLength() > 0) {
            Node firstChild = children.item(0);
            // Make sure it's a direct child
            if (firstChild.getParentNode() == element && firstChild instanceof Element elem) {
                return XmlElement.of(elem);
            }
        }
        // Search direct children only
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child instanceof Element elem && name.equals(elem.getTagName())) {
                return XmlElement.of(elem);
            }
        }
        return null;
    }

    /**
     * Returns all child elements with the specified name.
     * 返回具有指定名称的所有子元素。
     *
     * @param name the child element name | 子元素名称
     * @return the list of child elements | 子元素列表
     */
    public List<XmlElement> getChildren(String name) {
        List<XmlElement> result = new ArrayList<>();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element elem && name.equals(elem.getTagName())) {
                result.add(XmlElement.of(elem));
            }
        }
        return result;
    }

    /**
     * Returns all child elements.
     * 返回所有子元素。
     *
     * @return the list of all child elements | 所有子元素列表
     */
    public List<XmlElement> getChildren() {
        List<XmlElement> result = new ArrayList<>();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element elem) {
                result.add(XmlElement.of(elem));
            }
        }
        return result;
    }

    /**
     * Returns the text content of a child element.
     * 返回子元素的文本内容。
     *
     * @param name the child element name | 子元素名称
     * @return the text content, or null if not found | 文本内容，如果未找到则返回 null
     */
    public String getChildText(String name) {
        XmlElement child = getChild(name);
        return child != null ? child.getText() : null;
    }

    /**
     * Returns the text content of a child element or default.
     * 返回子元素的文本内容或默认值。
     *
     * @param name         the child element name | 子元素名称
     * @param defaultValue the default value | 默认值
     * @return the text content or default | 文本内容或默认值
     */
    public String getChildText(String name, String defaultValue) {
        String text = getChildText(name);
        return text != null ? text : defaultValue;
    }

    /**
     * Returns whether this element has a child with the specified name.
     * 返回此元素是否具有指定名称的子元素。
     *
     * @param name the child element name | 子元素名称
     * @return true if child exists | 如果子元素存在则返回 true
     */
    public boolean hasChild(String name) {
        return getChild(name) != null;
    }

    /**
     * Returns the number of child elements.
     * 返回子元素数量。
     *
     * @return the child count | 子元素数量
     */
    public int getChildCount() {
        int count = 0;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                count++;
            }
        }
        return count;
    }

    /**
     * Adds a new child element.
     * 添加新的子元素。
     *
     * @param name the element name | 元素名称
     * @return the new child element | 新子元素
     */
    public XmlElement addChild(String name) {
        Element child = element.getOwnerDocument().createElement(name);
        element.appendChild(child);
        return XmlElement.of(child);
    }

    /**
     * Adds a new child element with text content.
     * 添加带文本内容的新子元素。
     *
     * @param name the element name | 元素名称
     * @param text the text content | 文本内容
     * @return the new child element | 新子元素
     */
    public XmlElement addChild(String name, String text) {
        XmlElement child = addChild(name);
        if (text != null) {
            child.setText(text);
        }
        return child;
    }

    /**
     * Adds an existing element as a child.
     * 将现有元素添加为子元素。
     *
     * @param child the child element to add | 要添加的子元素
     * @return this element for chaining | 此元素以便链式调用
     */
    public XmlElement addChild(XmlElement child) {
        Node imported = element.getOwnerDocument().importNode(child.getElement(), true);
        element.appendChild(imported);
        return this;
    }

    /**
     * Removes child elements with the specified name.
     * 移除具有指定名称的子元素。
     *
     * @param name the child element name | 子元素名称
     * @return this element for chaining | 此元素以便链式调用
     */
    public XmlElement removeChild(String name) {
        List<XmlElement> children = getChildren(name);
        for (XmlElement child : children) {
            element.removeChild(child.getElement());
        }
        return this;
    }

    /**
     * Removes all child elements.
     * 移除所有子元素。
     *
     * @return this element for chaining | 此元素以便链式调用
     */
    public XmlElement removeChildren() {
        while (element.hasChildNodes()) {
            element.removeChild(element.getFirstChild());
        }
        return this;
    }

    // ==================== Parent Element | 父元素 ====================

    /**
     * Returns the parent element.
     * 返回父元素。
     *
     * @return the parent element, or null if this is the root | 父元素，如果是根元素则返回 null
     */
    public XmlElement getParent() {
        Node parent = element.getParentNode();
        if (parent instanceof Element elem) {
            return XmlElement.of(elem);
        }
        return null;
    }

    /**
     * Returns whether this element has a parent element.
     * 返回此元素是否有父元素。
     *
     * @return true if has parent | 如果有父元素则返回 true
     */
    public boolean hasParent() {
        return element.getParentNode() instanceof Element;
    }

    // ==================== XPath | XPath ====================

    /**
     * Evaluates an XPath expression relative to this element.
     * 相对于此元素求值 XPath 表达式。
     *
     * @param xpath the XPath expression | XPath 表达式
     * @return the result string | 结果字符串
     */
    public String xpath(String xpath) {
        return OpenXPath.selectString(element, xpath);
    }

    /**
     * Evaluates an XPath expression and returns element list.
     * 求值 XPath 表达式并返回元素列表。
     *
     * @param xpath the XPath expression | XPath 表达式
     * @return the list of matching elements | 匹配元素列表
     */
    public List<XmlElement> xpathList(String xpath) {
        return OpenXPath.selectElements(element, xpath);
    }

    // ==================== Conversion | 转换 ====================

    /**
     * Binds this element to an object.
     * 将此元素绑定到对象。
     *
     * @param <T>   the type parameter | 类型参数
     * @param clazz the target type | 目标类型
     * @return the bound object | 绑定的对象
     */
    public <T> T bind(Class<T> clazz) {
        return XmlBinder.create().unmarshal(this, clazz);
    }

    @Override
    public Map<String, Object> toMap() {
        return DomUtil.elementToMap(element);
    }

    @Override
    public String toXml() {
        return DomUtil.toString(element);
    }

    @Override
    public String toXml(int indent) {
        return DomUtil.toString(element, indent);
    }

    @Override
    public String toString() {
        return toXml();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof XmlElement other) {
            return element.isEqualNode(other.element);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return element.hashCode();
    }
}
