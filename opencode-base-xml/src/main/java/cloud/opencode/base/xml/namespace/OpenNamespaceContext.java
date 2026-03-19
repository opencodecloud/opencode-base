package cloud.opencode.base.xml.namespace;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.*;

/**
 * Open Namespace Context - NamespaceContext implementation for XPath
 * 开放命名空间上下文 - 用于 XPath 的 NamespaceContext 实现
 *
 * <p>This class provides a simple implementation of NamespaceContext for use with XPath.</p>
 * <p>此类提供用于 XPath 的 NamespaceContext 简单实现。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create namespace context
 * OpenNamespaceContext ctx = OpenNamespaceContext.create()
 *     .bind("soap", "http://schemas.xmlsoap.org/soap/envelope/")
 *     .bind("xsd", "http://www.w3.org/2001/XMLSchema");
 *
 * // Use with XPath
 * XPath xpath = XPathFactory.newInstance().newXPath();
 * xpath.setNamespaceContext(ctx);
 * String result = xpath.evaluate("//soap:Body", document);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>NamespaceContext implementation for XPath evaluation - 用于 XPath 求值的 NamespaceContext 实现</li>
 *   <li>Fluent API for prefix-URI binding - 用于前缀-URI 绑定的流式 API</li>
 *   <li>Pre-bound standard XML namespaces - 预绑定的标准 XML 命名空间</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable bindings) - 线程安全: 否（可变绑定）</li>
 *   <li>Null-safe: No (throws on null prefix/URI) - 空值安全: 否（null 前缀/URI 抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class OpenNamespaceContext implements NamespaceContext {

    private final Map<String, String> prefixToUri = new HashMap<>();
    private final Map<String, List<String>> uriToPrefixes = new HashMap<>();
    private String defaultNamespaceUri = XMLConstants.NULL_NS_URI;

    private OpenNamespaceContext() {
        // Pre-bind standard namespaces
        bindInternal(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
        bindInternal(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
    }

    /**
     * Creates a new namespace context.
     * 创建新的命名空间上下文。
     *
     * @return a new context | 新上下文
     */
    public static OpenNamespaceContext create() {
        return new OpenNamespaceContext();
    }

    /**
     * Creates a namespace context from a map.
     * 从映射创建命名空间上下文。
     *
     * @param prefixToUri the prefix to URI map | 前缀到 URI 映射
     * @return a new context | 新上下文
     */
    public static OpenNamespaceContext of(Map<String, String> prefixToUri) {
        OpenNamespaceContext ctx = new OpenNamespaceContext();
        prefixToUri.forEach(ctx::bind);
        return ctx;
    }

    /**
     * Creates a namespace context with a single binding.
     * 创建具有单个绑定的命名空间上下文。
     *
     * @param prefix       the prefix | 前缀
     * @param namespaceUri the namespace URI | 命名空间 URI
     * @return a new context | 新上下文
     */
    public static OpenNamespaceContext of(String prefix, String namespaceUri) {
        return create().bind(prefix, namespaceUri);
    }

    /**
     * Binds a prefix to a namespace URI.
     * 将前缀绑定到命名空间 URI。
     *
     * @param prefix       the prefix | 前缀
     * @param namespaceUri the namespace URI | 命名空间 URI
     * @return this context for chaining | 此上下文以便链式调用
     */
    public OpenNamespaceContext bind(String prefix, String namespaceUri) {
        if (prefix == null || namespaceUri == null) {
            throw new IllegalArgumentException("Prefix and namespace URI cannot be null");
        }

        // Don't allow rebinding of standard prefixes
        if (XMLConstants.XML_NS_PREFIX.equals(prefix) ||
            XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
            return this;
        }

        bindInternal(prefix, namespaceUri);
        return this;
    }

    private void bindInternal(String prefix, String namespaceUri) {
        prefixToUri.put(prefix, namespaceUri);
        uriToPrefixes.computeIfAbsent(namespaceUri, k -> new ArrayList<>()).add(prefix);
    }

    /**
     * Sets the default namespace URI.
     * 设置默认命名空间 URI。
     *
     * @param namespaceUri the namespace URI | 命名空间 URI
     * @return this context for chaining | 此上下文以便链式调用
     */
    public OpenNamespaceContext setDefaultNamespace(String namespaceUri) {
        this.defaultNamespaceUri = namespaceUri != null ? namespaceUri : XMLConstants.NULL_NS_URI;
        prefixToUri.put(XMLConstants.DEFAULT_NS_PREFIX, this.defaultNamespaceUri);
        uriToPrefixes.computeIfAbsent(this.defaultNamespaceUri, k -> new ArrayList<>())
            .add(XMLConstants.DEFAULT_NS_PREFIX);
        return this;
    }

    /**
     * Removes a prefix binding.
     * 移除前缀绑定。
     *
     * @param prefix the prefix to remove | 要移除的前缀
     * @return this context for chaining | 此上下文以便链式调用
     */
    public OpenNamespaceContext unbind(String prefix) {
        if (XMLConstants.XML_NS_PREFIX.equals(prefix) ||
            XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
            return this;
        }

        String uri = prefixToUri.remove(prefix);
        if (uri != null) {
            List<String> prefixes = uriToPrefixes.get(uri);
            if (prefixes != null) {
                prefixes.remove(prefix);
                if (prefixes.isEmpty()) {
                    uriToPrefixes.remove(uri);
                }
            }
        }
        return this;
    }

    /**
     * Checks if a prefix is bound.
     * 检查前缀是否已绑定。
     *
     * @param prefix the prefix | 前缀
     * @return true if bound | 如果已绑定则返回 true
     */
    public boolean isBound(String prefix) {
        return prefixToUri.containsKey(prefix);
    }

    /**
     * Gets all bound prefixes.
     * 获取所有已绑定的前缀。
     *
     * @return set of prefixes | 前缀集合
     */
    public Set<String> getPrefixes() {
        return Collections.unmodifiableSet(prefixToUri.keySet());
    }

    /**
     * Gets all bindings.
     * 获取所有绑定。
     *
     * @return map of prefix to URI | 前缀到 URI 的映射
     */
    public Map<String, String> getBindings() {
        return Collections.unmodifiableMap(prefixToUri);
    }

    // ==================== NamespaceContext Implementation | NamespaceContext 实现 ====================

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Prefix cannot be null");
        }

        String uri = prefixToUri.get(prefix);
        return uri != null ? uri : XMLConstants.NULL_NS_URI;
    }

    @Override
    public String getPrefix(String namespaceUri) {
        if (namespaceUri == null) {
            throw new IllegalArgumentException("Namespace URI cannot be null");
        }

        List<String> prefixes = uriToPrefixes.get(namespaceUri);
        return (prefixes != null && !prefixes.isEmpty()) ? prefixes.getFirst() : null;
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceUri) {
        if (namespaceUri == null) {
            throw new IllegalArgumentException("Namespace URI cannot be null");
        }

        List<String> prefixes = uriToPrefixes.get(namespaceUri);
        if (prefixes == null || prefixes.isEmpty()) {
            return Collections.emptyIterator();
        }
        return Collections.unmodifiableList(prefixes).iterator();
    }

    @Override
    public String toString() {
        return "OpenNamespaceContext{bindings=" + prefixToUri + "}";
    }
}
