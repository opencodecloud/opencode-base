package cloud.opencode.base.xml.xpath;

import cloud.opencode.base.xml.XmlElement;

import java.util.List;
import java.util.Optional;

/**
 * XPath Result - Container for XPath query results
 * XPath 结果 - XPath 查询结果的容器
 *
 * <p>This record holds the result of an XPath query with convenience methods
 * for accessing the result in different forms.</p>
 * <p>此记录保存 XPath 查询的结果，并提供便捷方法以不同形式访问结果。</p>
 *
 * @param value    the raw result value | 原始结果值
 * @param elements the list of element results | 元素结果列表
 * @param xpath    the XPath expression used | 使用的 XPath 表达式
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Container for XPath query results - XPath 查询结果的容器</li>
 *   <li>Access results as string, number, boolean, or element list - 以字符串、数字、布尔值或元素列表访问结果</li>
 *   <li>Immutable record type - 不可变记录类型</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Access XPath result
 * XPathResult result = OpenXPath.evaluate(doc, "//user/name");
 * String name = result.asString();
 * List<XmlElement> elements = result.elements();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (value can be null) - 空值安全: 是（值可以为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public record XPathResult(
    Object value,
    List<XmlElement> elements,
    String xpath
) {

    /**
     * Creates an XPath result from a single value.
     * 从单个值创建 XPath 结果。
     *
     * @param value the value | 值
     * @param xpath the XPath expression | XPath 表达式
     * @return the result | 结果
     */
    public static XPathResult of(Object value, String xpath) {
        return new XPathResult(value, List.of(), xpath);
    }

    /**
     * Creates an XPath result from elements.
     * 从元素创建 XPath 结果。
     *
     * @param elements the elements | 元素列表
     * @param xpath    the XPath expression | XPath 表达式
     * @return the result | 结果
     */
    public static XPathResult of(List<XmlElement> elements, String xpath) {
        return new XPathResult(null, elements, xpath);
    }

    /**
     * Returns the result as a string.
     * 返回结果为字符串。
     *
     * @return the string value | 字符串值
     */
    public String asString() {
        if (value != null) {
            return value.toString();
        }
        if (!elements.isEmpty()) {
            return elements.getFirst().getText();
        }
        return null;
    }

    /**
     * Returns the result as an optional string.
     * 返回结果为可选字符串。
     *
     * @return the optional string | 可选字符串
     */
    public Optional<String> asOptionalString() {
        return Optional.ofNullable(asString());
    }

    /**
     * Returns the result as an integer.
     * 返回结果为整数。
     *
     * @return the integer value | 整数值
     */
    public Integer asInt() {
        String str = asString();
        return str != null ? Integer.parseInt(str.trim()) : null;
    }

    /**
     * Returns the result as an optional integer.
     * 返回结果为可选整数。
     *
     * @return the optional integer | 可选整数
     */
    public Optional<Integer> asOptionalInt() {
        return Optional.ofNullable(asInt());
    }

    /**
     * Returns the result as a long.
     * 返回结果为长整数。
     *
     * @return the long value | 长整数值
     */
    public Long asLong() {
        String str = asString();
        return str != null ? Long.parseLong(str.trim()) : null;
    }

    /**
     * Returns the result as a double.
     * 返回结果为双精度浮点数。
     *
     * @return the double value | 双精度浮点数值
     */
    public Double asDouble() {
        if (value instanceof Number num) {
            return num.doubleValue();
        }
        String str = asString();
        return str != null ? Double.parseDouble(str.trim()) : null;
    }

    /**
     * Returns the result as a boolean.
     * 返回结果为布尔值。
     *
     * @return the boolean value | 布尔值
     */
    public Boolean asBoolean() {
        if (value instanceof Boolean bool) {
            return bool;
        }
        String str = asString();
        return str != null ? Boolean.parseBoolean(str.trim()) : null;
    }

    /**
     * Returns the first element if available.
     * 返回第一个元素（如果可用）。
     *
     * @return the first element | 第一个元素
     */
    public Optional<XmlElement> asElement() {
        return elements.isEmpty() ? Optional.empty() : Optional.of(elements.getFirst());
    }

    /**
     * Returns all elements.
     * 返回所有元素。
     *
     * @return the elements | 元素列表
     */
    public List<XmlElement> asElements() {
        return elements;
    }

    /**
     * Returns whether the result is empty.
     * 返回结果是否为空。
     *
     * @return true if empty | 如果为空则返回 true
     */
    public boolean isEmpty() {
        return value == null && elements.isEmpty();
    }

    /**
     * Returns the count of matched elements.
     * 返回匹配元素的数量。
     *
     * @return the count | 数量
     */
    public int count() {
        return elements.size();
    }
}
