package cloud.opencode.base.xml;

import cloud.opencode.base.xml.bind.XmlBinder;
import cloud.opencode.base.xml.builder.ElementBuilder;
import cloud.opencode.base.xml.builder.XmlBuilder;
import cloud.opencode.base.xml.canonical.XmlCanonicalizer;
import cloud.opencode.base.xml.diff.DiffEntry;
import cloud.opencode.base.xml.diff.XmlDiff;
import cloud.opencode.base.xml.merge.XmlMerge;
import cloud.opencode.base.xml.namespace.NamespaceUtil;
import cloud.opencode.base.xml.namespace.OpenNamespaceContext;
import cloud.opencode.base.xml.path.XmlPath;
import cloud.opencode.base.xml.sax.SaxParser;
import cloud.opencode.base.xml.splitter.XmlSplitter;
import cloud.opencode.base.xml.stax.StaxReader;
import cloud.opencode.base.xml.stax.StaxWriter;
import cloud.opencode.base.xml.transform.XmlTransformer;
import cloud.opencode.base.xml.transform.XsltTransformer;
import cloud.opencode.base.xml.validate.SchemaValidator;
import cloud.opencode.base.xml.validate.ValidationResult;
import cloud.opencode.base.xml.validate.XmlValidator;
import cloud.opencode.base.xml.xpath.OpenXPath;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * OpenXml - Unified facade for XML operations
 * OpenXml - XML 操作的统一门面
 *
 * <p>This class provides a unified entry point for all XML operations in the opencode-base-xml module.</p>
 * <p>此类为 opencode-base-xml 模块中的所有 XML 操作提供统一入口点。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>XML parsing from String, File, InputStream - 从字符串、文件、输入流解析 XML</li>
 *   <li>Fluent XML document building - 流式 XML 文档构建</li>
 *   <li>XPath querying with namespace support - 支持命名空间的 XPath 查询</li>
 *   <li>XML Schema and DTD validation - XML Schema 和 DTD 验证</li>
 *   <li>XSLT transformation - XSLT 转换</li>
 *   <li>XML-to-object binding and marshalling - XML 到对象绑定和编组</li>
 *   <li>SAX and StAX streaming parsers - SAX 和 StAX 流式解析器</li>
 *   <li>Namespace management utilities - 命名空间管理工具</li>
 *   <li>XML diff and comparison - XML 差异比较</li>
 *   <li>XML document merging - XML 文档合并</li>
 *   <li>Dot-notation path access - 点表示法路径访问</li>
 *   <li>Stream-based XML splitting - 基于流的 XML 拆分</li>
 *   <li>XML canonicalization - XML 规范化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Parse XML
 * XmlDocument doc = OpenXml.parse(xmlString);
 * XmlDocument doc = OpenXml.parseFile(path);
 *
 * // Build XML
 * XmlDocument doc = OpenXml.builder("root")
 *     .element("child", "value")
 *     .build();
 *
 * // XPath queries
 * String value = OpenXml.xpath(doc, "//name/text()");
 * List<XmlElement> elements = OpenXml.xpathElements(doc, "//item");
 *
 * // Validate
 * boolean valid = OpenXml.isWellFormed(xml);
 * ValidationResult result = OpenXml.validateSchema(xml, schemaPath);
 *
 * // Transform
 * String formatted = OpenXml.format(xml, 4);
 * String result = OpenXml.xslt(xml, xsltPath);
 *
 * // Bind to objects
 * User user = OpenXml.unmarshal(xml, User.class);
 * String xml = OpenXml.marshal(user);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (all static methods, no shared mutable state) - 线程安全: 是（全部静态方法，无共享可变状态）</li>
 *   <li>Null-safe: No (null inputs throw exceptions) - 空值安全: 否（空值输入抛出异常）</li>
 *   <li>XXE protection enabled by default - 默认启用 XXE 防护</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
public final class OpenXml {

    private OpenXml() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    // ==================== Parse | 解析 ====================

    /**
     * Parses an XML string to XmlDocument.
     * 将 XML 字符串解析为 XmlDocument。
     *
     * @param xml the XML string | XML 字符串
     * @return the parsed document | 解析后的文档
     */
    public static XmlDocument parse(String xml) {
        return XmlDocument.parse(xml);
    }

    /**
     * Parses an XML file to XmlDocument.
     * 将 XML 文件解析为 XmlDocument。
     *
     * @param path the file path | 文件路径
     * @return the parsed document | 解析后的文档
     */
    public static XmlDocument parseFile(Path path) {
        return XmlDocument.load(path);
    }

    /**
     * Parses an XML input stream to XmlDocument.
     * 将 XML 输入流解析为 XmlDocument。
     *
     * @param input the input stream | 输入流
     * @return the parsed document | 解析后的文档
     */
    public static XmlDocument parse(InputStream input) {
        return XmlDocument.load(input);
    }

    // ==================== Build | 构建 ====================

    /**
     * Creates an XML builder with the given root element name.
     * 使用给定的根元素名称创建 XML 构建器。
     *
     * @param rootName the root element name | 根元素名称
     * @return a new builder | 新构建器
     */
    public static XmlBuilder builder(String rootName) {
        return XmlBuilder.create(rootName);
    }

    /**
     * Creates an XML builder with namespace.
     * 使用命名空间创建 XML 构建器。
     *
     * @param namespaceUri the namespace URI | 命名空间 URI
     * @param rootName     the root element name | 根元素名称
     * @return a new builder | 新构建器
     */
    public static XmlBuilder builder(String namespaceUri, String rootName) {
        return XmlBuilder.create(namespaceUri, rootName);
    }

    /**
     * Creates an element builder.
     * 创建元素构建器。
     *
     * @param name the element name | 元素名称
     * @return a new element builder | 新元素构建器
     */
    public static ElementBuilder element(String name) {
        return ElementBuilder.create(name);
    }

    // ==================== XPath | XPath 查询 ====================

    /**
     * Evaluates an XPath expression and returns a string.
     * 计算 XPath 表达式并返回字符串。
     *
     * @param doc   the document | 文档
     * @param xpath the XPath expression | XPath 表达式
     * @return the result string | 结果字符串
     */
    public static String xpath(XmlDocument doc, String xpath) {
        return OpenXPath.selectString(doc.getDocument(), xpath);
    }

    /**
     * Evaluates an XPath expression with namespaces.
     * 使用命名空间计算 XPath 表达式。
     *
     * @param doc        the document | 文档
     * @param xpath      the XPath expression | XPath 表达式
     * @param namespaces the namespace map | 命名空间映射
     * @return the result string | 结果字符串
     */
    public static String xpath(XmlDocument doc, String xpath, Map<String, String> namespaces) {
        return OpenXPath.selectString(doc.getDocument(), xpath, namespaces);
    }

    /**
     * Evaluates an XPath expression and returns elements.
     * 计算 XPath 表达式并返回元素列表。
     *
     * @param doc   the document | 文档
     * @param xpath the XPath expression | XPath 表达式
     * @return list of matching elements | 匹配元素列表
     */
    public static java.util.List<XmlElement> xpathElements(XmlDocument doc, String xpath) {
        return OpenXPath.selectElements(doc.getDocument(), xpath);
    }

    /**
     * Evaluates an XPath expression on an element.
     * 在元素上计算 XPath 表达式。
     *
     * @param element the element | 元素
     * @param xpath   the XPath expression | XPath 表达式
     * @return the result string | 结果字符串
     */
    public static String xpath(XmlElement element, String xpath) {
        return OpenXPath.selectString(element.getElement(), xpath);
    }

    // ==================== Validate | 验证 ====================

    /**
     * Checks if XML is well-formed.
     * 检查 XML 是否格式良好。
     *
     * @param xml the XML string | XML 字符串
     * @return true if well-formed | 如果格式良好则返回 true
     */
    public static boolean isWellFormed(String xml) {
        return XmlValidator.isWellFormed(xml);
    }

    /**
     * Validates XML well-formedness.
     * 验证 XML 格式良好性。
     *
     * @param xml the XML string | XML 字符串
     * @return the validation result | 验证结果
     */
    public static ValidationResult validateWellFormedness(String xml) {
        return XmlValidator.validateWellFormedness(xml);
    }

    /**
     * Validates XML against a schema.
     * 针对模式验证 XML。
     *
     * @param xml        the XML string | XML 字符串
     * @param schemaPath the schema path | 模式路径
     * @return the validation result | 验证结果
     */
    public static ValidationResult validateSchema(String xml, Path schemaPath) {
        return SchemaValidator.of(schemaPath).validate(xml);
    }

    /**
     * Creates a schema validator.
     * 创建模式验证器。
     *
     * @param schemaPath the schema path | 模式路径
     * @return a new validator | 新验证器
     */
    public static SchemaValidator schemaValidator(Path schemaPath) {
        return SchemaValidator.of(schemaPath);
    }

    // ==================== Transform | 转换 ====================

    /**
     * Formats XML with indentation.
     * 使用缩进格式化 XML。
     *
     * @param xml    the XML string | XML 字符串
     * @param indent the indent spaces | 缩进空格数
     * @return the formatted XML | 格式化的 XML
     */
    public static String format(String xml, int indent) {
        return XmlTransformer.format(xml, indent);
    }

    /**
     * Formats XML with default indentation (4 spaces).
     * 使用默认缩进（4 空格）格式化 XML。
     *
     * @param xml the XML string | XML 字符串
     * @return the formatted XML | 格式化的 XML
     */
    public static String format(String xml) {
        return XmlTransformer.format(xml);
    }

    /**
     * Minifies XML by removing whitespace.
     * 通过删除空白来压缩 XML。
     *
     * @param xml the XML string | XML 字符串
     * @return the minified XML | 压缩的 XML
     */
    public static String minify(String xml) {
        return XmlTransformer.minify(xml);
    }

    /**
     * Transforms XML using XSLT.
     * 使用 XSLT 转换 XML。
     *
     * @param xml      the XML string | XML 字符串
     * @param xsltPath the XSLT path | XSLT 路径
     * @return the transformed result | 转换结果
     */
    public static String xslt(String xml, Path xsltPath) {
        return XsltTransformer.of(xsltPath).transform(xml);
    }

    /**
     * Creates an XSLT transformer.
     * 创建 XSLT 转换器。
     *
     * @param xsltPath the XSLT path | XSLT 路径
     * @return a new transformer | 新转换器
     */
    public static XsltTransformer xsltTransformer(Path xsltPath) {
        return XsltTransformer.of(xsltPath);
    }

    /**
     * Creates an XSLT transformer from string.
     * 从字符串创建 XSLT 转换器。
     *
     * @param xslt the XSLT string | XSLT 字符串
     * @return a new transformer | 新转换器
     */
    public static XsltTransformer xsltTransformer(String xslt) {
        return XsltTransformer.of(xslt);
    }

    // ==================== Binding | 绑定 ====================

    /**
     * Unmarshals XML to an object.
     * 将 XML 解组为对象。
     *
     * @param <T>   the type parameter | 类型参数
     * @param xml   the XML string | XML 字符串
     * @param clazz the target class | 目标类
     * @return the unmarshalled object | 解组的对象
     */
    public static <T> T unmarshal(String xml, Class<T> clazz) {
        return XmlBinder.create().unmarshal(xml, clazz);
    }

    /**
     * Unmarshals XmlDocument to an object.
     * 将 XmlDocument 解组为对象。
     *
     * @param <T>      the type parameter | 类型参数
     * @param document the document | 文档
     * @param clazz    the target class | 目标类
     * @return the unmarshalled object | 解组的对象
     */
    public static <T> T unmarshal(XmlDocument document, Class<T> clazz) {
        return XmlBinder.create().unmarshal(document, clazz);
    }

    /**
     * Marshals an object to XML string.
     * 将对象编组为 XML 字符串。
     *
     * @param obj the object | 对象
     * @return the XML string | XML 字符串
     */
    public static String marshal(Object obj) {
        return XmlBinder.create().marshal(obj);
    }

    /**
     * Marshals an object to formatted XML string.
     * 将对象编组为格式化的 XML 字符串。
     *
     * @param obj    the object | 对象
     * @param indent the indent spaces | 缩进空格数
     * @return the XML string | XML 字符串
     */
    public static String marshal(Object obj, int indent) {
        return XmlBinder.create().formatted(true).indent(indent).marshal(obj);
    }

    /**
     * Creates an XML binder.
     * 创建 XML 绑定器。
     *
     * @return a new binder | 新绑定器
     */
    public static XmlBinder binder() {
        return XmlBinder.create();
    }

    // ==================== SAX | SAX 解析 ====================

    /**
     * Creates a SAX parser.
     * 创建 SAX 解析器。
     *
     * @return a new SAX parser | 新 SAX 解析器
     */
    public static SaxParser saxParser() {
        return SaxParser.createSecure();
    }

    // ==================== StAX | StAX 解析 ====================

    /**
     * Creates a StAX reader.
     * 创建 StAX 读取器。
     *
     * @param xml the XML string | XML 字符串
     * @return a new StAX reader | 新 StAX 读取器
     */
    public static StaxReader staxReader(String xml) {
        return StaxReader.of(xml);
    }

    /**
     * Creates a StAX reader from file.
     * 从文件创建 StAX 读取器。
     *
     * @param path the file path | 文件路径
     * @return a new StAX reader | 新 StAX 读取器
     */
    public static StaxReader staxReader(Path path) {
        return StaxReader.of(path);
    }

    /**
     * Creates a StAX writer.
     * 创建 StAX 写入器。
     *
     * @return a new StAX writer | 新 StAX 写入器
     */
    public static StaxWriter staxWriter() {
        return StaxWriter.create();
    }

    /**
     * Creates a StAX writer to file.
     * 创建写入文件的 StAX 写入器。
     *
     * @param path the file path | 文件路径
     * @return a new StAX writer | 新 StAX 写入器
     */
    public static StaxWriter staxWriter(Path path) {
        return StaxWriter.create(path);
    }

    // ==================== Namespace | 命名空间 ====================

    /**
     * Creates a namespace context.
     * 创建命名空间上下文。
     *
     * @return a new namespace context | 新命名空间上下文
     */
    public static OpenNamespaceContext namespaceContext() {
        return OpenNamespaceContext.create();
    }

    /**
     * Creates a namespace context from a document.
     * 从文档创建命名空间上下文。
     *
     * @param document the document | 文档
     * @return a namespace context | 命名空间上下文
     */
    public static OpenNamespaceContext namespaceContext(XmlDocument document) {
        return NamespaceUtil.createContext(document);
    }

    /**
     * Extracts namespaces from a document.
     * 从文档提取命名空间。
     *
     * @param document the document | 文档
     * @return map of prefix to URI | 前缀到 URI 的映射
     */
    public static Map<String, String> extractNamespaces(XmlDocument document) {
        return NamespaceUtil.extractNamespaces(document);
    }

    // ==================== Diff | 比较 ====================

    /**
     * Computes the differences between two XML strings.
     * 计算两个 XML 字符串之间的差异。
     *
     * @param xml1 the first XML string | 第一个 XML 字符串
     * @param xml2 the second XML string | 第二个 XML 字符串
     * @return the list of differences | 差异列表
     */
    public static List<DiffEntry> diff(String xml1, String xml2) {
        return XmlDiff.diff(xml1, xml2);
    }

    /**
     * Computes the differences between two XML documents.
     * 计算两个 XML 文档之间的差异。
     *
     * @param doc1 the first document | 第一个文档
     * @param doc2 the second document | 第二个文档
     * @return the list of differences | 差异列表
     */
    public static List<DiffEntry> diff(XmlDocument doc1, XmlDocument doc2) {
        return XmlDiff.diff(doc1, doc2);
    }

    /**
     * Checks whether two XML strings are structurally equal.
     * 检查两个 XML 字符串是否结构相等。
     *
     * @param xml1 the first XML string | 第一个 XML 字符串
     * @param xml2 the second XML string | 第二个 XML 字符串
     * @return true if structurally equal | 如果结构相等则返回 true
     */
    public static boolean xmlEquals(String xml1, String xml2) {
        return XmlDiff.isEqual(xml1, xml2);
    }

    // ==================== Merge | 合并 ====================

    /**
     * Merges an overlay document onto a base document.
     * 将覆盖文档合并到基础文档上。
     *
     * @param base    the base document | 基础文档
     * @param overlay the overlay document | 覆盖文档
     * @return the merged document | 合并后的文档
     */
    public static XmlDocument merge(XmlDocument base, XmlDocument overlay) {
        return XmlMerge.merge(base, overlay);
    }

    /**
     * Merges an overlay XML string onto a base XML string.
     * 将覆盖 XML 字符串合并到基础 XML 字符串上。
     *
     * @param baseXml    the base XML string | 基础 XML 字符串
     * @param overlayXml the overlay XML string | 覆盖 XML 字符串
     * @return the merged XML string | 合并后的 XML 字符串
     */
    public static String merge(String baseXml, String overlayXml) {
        return XmlMerge.merge(baseXml, overlayXml);
    }

    // ==================== Path | 路径访问 ====================

    /**
     * Accesses an element value using dot-notation path.
     * 使用点表示法路径访问元素值。
     *
     * @param doc  the document | 文档
     * @param path the dot-notation path (e.g. "root.child.name") | 点表示法路径（例如 "root.child.name"）
     * @return the element text value | 元素文本值
     */
    public static String path(XmlDocument doc, String path) {
        return XmlPath.getString(doc, path);
    }

    /**
     * Accesses an element value using dot-notation path, returning Optional.
     * 使用点表示法路径访问元素值，返回 Optional。
     *
     * @param doc  the document | 文档
     * @param path the dot-notation path | 点表示法路径
     * @return the element text value, or empty if not found | 元素文本值，如果未找到则为空
     */
    public static Optional<String> pathOptional(XmlDocument doc, String path) {
        return XmlPath.getOptional(doc, path);
    }

    // ==================== Split | 拆分 ====================

    /**
     * Splits an XML input stream by element name and processes each fragment.
     * 按元素名称拆分 XML 输入流并处理每个片段。
     *
     * @param input       the input stream | 输入流
     * @param elementName the element name to split on | 要拆分的元素名称
     * @param handler     the fragment handler | 片段处理器
     */
    public static void split(InputStream input, String elementName, Consumer<XmlDocument> handler) {
        XmlSplitter.split(input, elementName, handler);
    }

    /**
     * Splits an XML string by element name and collects all fragments.
     * 按元素名称拆分 XML 字符串并收集所有片段。
     *
     * @param xml         the XML string | XML 字符串
     * @param elementName the element name to split on | 要拆分的元素名称
     * @return the list of fragments | 片段列表
     */
    public static List<XmlDocument> splitAll(String xml, String elementName) {
        return XmlSplitter.splitAll(xml, elementName);
    }

    // ==================== Canonicalize | 规范化 ====================

    /**
     * Produces a canonical XML representation of the given XML string.
     * 生成给定 XML 字符串的规范化 XML 表示。
     *
     * @param xml the XML string | XML 字符串
     * @return the canonical XML | 规范化的 XML
     */
    public static String canonicalize(String xml) {
        return XmlCanonicalizer.canonicalize(xml);
    }
}
