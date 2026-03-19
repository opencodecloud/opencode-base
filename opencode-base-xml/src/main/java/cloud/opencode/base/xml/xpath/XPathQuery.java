package cloud.opencode.base.xml.xpath;

import cloud.opencode.base.xml.XmlElement;
import cloud.opencode.base.xml.dom.DomParser;
import cloud.opencode.base.xml.exception.XmlXPathException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.xpath.*;
import java.util.*;

/**
 * XPath Query - Builder for complex XPath queries
 * XPath 查询 - 复杂 XPath 查询的构建器
 *
 * <p>This class provides a fluent interface for building and executing
 * XPath queries with support for namespaces and variables.</p>
 * <p>此类提供流式接口，用于构建和执行支持命名空间和变量的 XPath 查询。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple query
 * String name = XPathQuery.create()
 *     .xml("<root><name>John</name></root>")
 *     .selectString("//name/text()");
 *
 * // Query with namespace
 * String value = XPathQuery.create()
 *     .xml(nsXml)
 *     .namespace("ns", "http://example.com")
 *     .selectString("//ns:item/text()");
 *
 * // Query with variables
 * String result = XPathQuery.create()
 *     .xml(xml)
 *     .variable("userId", "123")
 *     .selectString("//user[@id=$userId]/name");
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent builder for complex XPath queries - 复杂 XPath 查询的流式构建器</li>
 *   <li>Namespace and variable support - 命名空间和变量支持</li>
 *   <li>Multiple result type selection (string, number, node list) - 多种结果类型选择（字符串、数字、节点列表）</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable builder) - 线程安全: 否（可变构建器）</li>
 *   <li>Null-safe: No (throws on null XML/expression) - 空值安全: 否（null XML/表达式抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class XPathQuery {

    private static final ThreadLocal<XPathFactory> XPATH_FACTORY =
        ThreadLocal.withInitial(XPathFactory::newInstance);

    private Document document;
    private final Map<String, String> namespaces = new LinkedHashMap<>();
    private final Map<String, Object> variables = new LinkedHashMap<>();

    private XPathQuery() {
    }

    /**
     * Creates a new XPath query.
     * 创建新的 XPath 查询。
     *
     * @return the query object | 查询对象
     */
    public static XPathQuery create() {
        return new XPathQuery();
    }

    /**
     * Sets the XML to query.
     * 设置要查询的 XML。
     *
     * @param xml the XML string | XML 字符串
     * @return this query for chaining | 此查询以便链式调用
     */
    public XPathQuery xml(String xml) {
        this.document = DomParser.parseNamespaceAware(xml);
        return this;
    }

    /**
     * Sets the Document to query.
     * 设置要查询的 Document。
     *
     * @param document the Document | Document
     * @return this query for chaining | 此查询以便链式调用
     */
    public XPathQuery document(Document document) {
        this.document = document;
        return this;
    }

    /**
     * Adds a namespace binding.
     * 添加命名空间绑定。
     *
     * @param prefix       the namespace prefix | 命名空间前缀
     * @param namespaceUri the namespace URI | 命名空间 URI
     * @return this query for chaining | 此查询以便链式调用
     */
    public XPathQuery namespace(String prefix, String namespaceUri) {
        this.namespaces.put(prefix, namespaceUri);
        return this;
    }

    /**
     * Adds multiple namespace bindings.
     * 添加多个命名空间绑定。
     *
     * @param namespaces the namespace map (prefix -> URI) | 命名空间映射（前缀 -> URI）
     * @return this query for chaining | 此查询以便链式调用
     */
    public XPathQuery namespaces(Map<String, String> namespaces) {
        this.namespaces.putAll(namespaces);
        return this;
    }

    /**
     * Adds a variable binding.
     * 添加变量绑定。
     *
     * @param name  the variable name | 变量名
     * @param value the variable value | 变量值
     * @return this query for chaining | 此查询以便链式调用
     */
    public XPathQuery variable(String name, Object value) {
        this.variables.put(name, value);
        return this;
    }

    /**
     * Selects a string value.
     * 选择字符串值。
     *
     * @param xpath the XPath expression | XPath 表达式
     * @return the result string | 结果字符串
     */
    public String selectString(String xpath) {
        return evaluate(xpath, XPathConstants.STRING);
    }

    /**
     * Selects a number value.
     * 选择数字值。
     *
     * @param xpath the XPath expression | XPath 表达式
     * @return the result number | 结果数字
     */
    public Number selectNumber(String xpath) {
        return evaluate(xpath, XPathConstants.NUMBER);
    }

    /**
     * Selects a boolean value.
     * 选择布尔值。
     *
     * @param xpath the XPath expression | XPath 表达式
     * @return the result boolean | 结果布尔值
     */
    public Boolean selectBoolean(String xpath) {
        return evaluate(xpath, XPathConstants.BOOLEAN);
    }

    /**
     * Selects a single element.
     * 选择单个元素。
     *
     * @param xpath the XPath expression | XPath 表达式
     * @return the element, or null if not found | 元素，如果未找到则返回 null
     */
    public XmlElement selectElement(String xpath) {
        Node node = evaluate(xpath, XPathConstants.NODE);
        if (node instanceof Element elem) {
            return XmlElement.of(elem);
        }
        return null;
    }

    /**
     * Selects multiple elements.
     * 选择多个元素。
     *
     * @param xpath the XPath expression | XPath 表达式
     * @return the list of elements | 元素列表
     */
    public List<XmlElement> selectElements(String xpath) {
        NodeList nodeList = evaluate(xpath, XPathConstants.NODESET);
        List<XmlElement> result = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element elem) {
                result.add(XmlElement.of(elem));
            }
        }
        return result;
    }

    /**
     * Selects string values.
     * 选择字符串值列表。
     *
     * @param xpath the XPath expression | XPath 表达式
     * @return the list of strings | 字符串列表
     */
    public List<String> selectValues(String xpath) {
        NodeList nodeList = evaluate(xpath, XPathConstants.NODESET);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            result.add(node.getTextContent());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> T evaluate(String xpath, QName returnType) {
        if (document == null) {
            throw new XmlXPathException(xpath, "No XML document set");
        }

        try {
            XPath xp = XPATH_FACTORY.get().newXPath();

            // Set namespace context
            if (!namespaces.isEmpty()) {
                xp.setNamespaceContext(new SimpleNamespaceContext(namespaces));
            }

            // Set variable resolver
            if (!variables.isEmpty()) {
                xp.setXPathVariableResolver(new SimpleVariableResolver(variables));
            }

            return (T) xp.evaluate(xpath, document, returnType);
        } catch (XPathExpressionException e) {
            throw new XmlXPathException(xpath, e);
        }
    }

    /**
     * Simple NamespaceContext implementation.
     * 简单的 NamespaceContext 实现。
     */
    private static class SimpleNamespaceContext implements javax.xml.namespace.NamespaceContext {
        private final Map<String, String> namespaces;

        SimpleNamespaceContext(Map<String, String> namespaces) {
            this.namespaces = namespaces;
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return namespaces.getOrDefault(prefix, "");
        }

        @Override
        public String getPrefix(String namespaceURI) {
            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                if (entry.getValue().equals(namespaceURI)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            List<String> prefixes = new ArrayList<>();
            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                if (entry.getValue().equals(namespaceURI)) {
                    prefixes.add(entry.getKey());
                }
            }
            return prefixes.iterator();
        }
    }

    /**
     * Simple XPathVariableResolver implementation.
     * 简单的 XPathVariableResolver 实现。
     */
    private static class SimpleVariableResolver implements XPathVariableResolver {
        private final Map<String, Object> variables;

        SimpleVariableResolver(Map<String, Object> variables) {
            this.variables = variables;
        }

        @Override
        public Object resolveVariable(QName variableName) {
            return variables.get(variableName.getLocalPart());
        }
    }
}
