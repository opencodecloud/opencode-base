package cloud.opencode.base.xml.transform;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.exception.XmlTransformException;
import cloud.opencode.base.xml.security.SecureParserFactory;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * XSLT Transformer - XSLT stylesheet transformation
 * XSLT 转换器 - XSLT 样式表转换
 *
 * <p>This class provides XSLT transformation capabilities with a fluent API.</p>
 * <p>此类提供具有流式 API 的 XSLT 转换功能。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Transform XML using XSLT stylesheet
 * String result = XsltTransformer.of(xslt)
 *     .parameter("title", "My Document")
 *     .transform(xml);
 *
 * // Transform to file
 * XsltTransformer.of(xsltPath)
 *     .transform(xmlPath, outputPath);
 *
 * // Transform to XmlDocument
 * XmlDocument doc = XsltTransformer.of(xslt)
 *     .transformToDocument(xml);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>XSLT stylesheet transformation - XSLT 样式表转换</li>
 *   <li>Fluent API with parameter support - 带参数支持的流式 API</li>
 *   <li>Transform to string, file, or XmlDocument - 转换到字符串、文件或 XmlDocument</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable parameters) - 线程安全: 否（可变参数）</li>
 *   <li>Null-safe: No (throws on null XSLT/XML) - 空值安全: 否（null XSLT/XML 抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class XsltTransformer {

    private final Templates templates;
    private final Map<String, Object> parameters = new HashMap<>();
    private final Map<String, String> outputProperties = new HashMap<>();

    private XsltTransformer(Templates templates) {
        this.templates = templates;
    }

    /**
     * Creates a transformer from an XSLT string.
     * 从 XSLT 字符串创建转换器。
     *
     * @param xslt the XSLT stylesheet | XSLT 样式表
     * @return a new transformer | 新转换器
     */
    public static XsltTransformer of(String xslt) {
        try {
            TransformerFactory factory = SecureParserFactory.getTransformerFactory();
            Templates templates = factory.newTemplates(
                new StreamSource(new StringReader(xslt)));
            return new XsltTransformer(templates);
        } catch (TransformerException e) {
            throw new XmlTransformException("Failed to compile XSLT stylesheet", e);
        }
    }

    /**
     * Creates a transformer from an XSLT file.
     * 从 XSLT 文件创建转换器。
     *
     * @param path the XSLT file path | XSLT 文件路径
     * @return a new transformer | 新转换器
     */
    public static XsltTransformer of(Path path) {
        try {
            TransformerFactory factory = SecureParserFactory.getTransformerFactory();
            Templates templates = factory.newTemplates(new StreamSource(path.toFile()));
            return new XsltTransformer(templates);
        } catch (TransformerException e) {
            throw new XmlTransformException("Failed to compile XSLT from file: " + path, e);
        }
    }

    /**
     * Creates a transformer from an XSLT input stream.
     * 从 XSLT 输入流创建转换器。
     *
     * @param input the XSLT input stream | XSLT 输入流
     * @return a new transformer | 新转换器
     */
    public static XsltTransformer of(InputStream input) {
        try {
            TransformerFactory factory = SecureParserFactory.getTransformerFactory();
            Templates templates = factory.newTemplates(new StreamSource(input));
            return new XsltTransformer(templates);
        } catch (TransformerException e) {
            throw new XmlTransformException("Failed to compile XSLT from stream", e);
        }
    }

    // ==================== Parameters | 参数 ====================

    /**
     * Sets a transformation parameter.
     * 设置转换参数。
     *
     * @param name  the parameter name | 参数名称
     * @param value the parameter value | 参数值
     * @return this transformer for chaining | 此转换器以便链式调用
     */
    public XsltTransformer parameter(String name, Object value) {
        parameters.put(name, value);
        return this;
    }

    /**
     * Sets multiple transformation parameters.
     * 设置多个转换参数。
     *
     * @param params the parameters map | 参数映射
     * @return this transformer for chaining | 此转换器以便链式调用
     */
    public XsltTransformer parameters(Map<String, Object> params) {
        parameters.putAll(params);
        return this;
    }

    /**
     * Clears all parameters.
     * 清除所有参数。
     *
     * @return this transformer for chaining | 此转换器以便链式调用
     */
    public XsltTransformer clearParameters() {
        parameters.clear();
        return this;
    }

    // ==================== Output Properties | 输出属性 ====================

    /**
     * Sets an output property.
     * 设置输出属性。
     *
     * @param name  the property name | 属性名称
     * @param value the property value | 属性值
     * @return this transformer for chaining | 此转换器以便链式调用
     */
    public XsltTransformer outputProperty(String name, String value) {
        outputProperties.put(name, value);
        return this;
    }

    /**
     * Sets the output method (xml, html, text).
     * 设置输出方法（xml、html、text）。
     *
     * @param method the output method | 输出方法
     * @return this transformer for chaining | 此转换器以便链式调用
     */
    public XsltTransformer method(String method) {
        return outputProperty(OutputKeys.METHOD, method);
    }

    /**
     * Sets the output encoding.
     * 设置输出编码。
     *
     * @param encoding the encoding | 编码
     * @return this transformer for chaining | 此转换器以便链式调用
     */
    public XsltTransformer encoding(String encoding) {
        return outputProperty(OutputKeys.ENCODING, encoding);
    }

    /**
     * Sets whether to indent output.
     * 设置是否缩进输出。
     *
     * @param indent whether to indent | 是否缩进
     * @return this transformer for chaining | 此转换器以便链式调用
     */
    public XsltTransformer indent(boolean indent) {
        return outputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
    }

    /**
     * Sets whether to omit XML declaration.
     * 设置是否省略 XML 声明。
     *
     * @param omit whether to omit | 是否省略
     * @return this transformer for chaining | 此转换器以便链式调用
     */
    public XsltTransformer omitXmlDeclaration(boolean omit) {
        return outputProperty(OutputKeys.OMIT_XML_DECLARATION, omit ? "yes" : "no");
    }

    // ==================== Transform | 转换 ====================

    /**
     * Transforms an XML string.
     * 转换 XML 字符串。
     *
     * @param xml the XML string | XML 字符串
     * @return the transformed result | 转换结果
     */
    public String transform(String xml) {
        StringWriter writer = new StringWriter();
        transform(new StreamSource(new StringReader(xml)), new StreamResult(writer));
        return writer.toString();
    }

    /**
     * Transforms an XmlDocument.
     * 转换 XmlDocument。
     *
     * @param document the XML document | XML 文档
     * @return the transformed result | 转换结果
     */
    public String transform(XmlDocument document) {
        StringWriter writer = new StringWriter();
        transform(new DOMSource(document.getDocument()), new StreamResult(writer));
        return writer.toString();
    }

    /**
     * Transforms from input stream to string.
     * 从输入流转换为字符串。
     *
     * @param input the input stream | 输入流
     * @return the transformed result | 转换结果
     */
    public String transform(InputStream input) {
        StringWriter writer = new StringWriter();
        transform(new StreamSource(input), new StreamResult(writer));
        return writer.toString();
    }

    /**
     * Transforms from file to string.
     * 从文件转换为字符串。
     *
     * @param path the input file path | 输入文件路径
     * @return the transformed result | 转换结果
     */
    public String transformFile(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            return transform(is);
        } catch (IOException e) {
            throw new XmlTransformException("Failed to read file: " + path, e);
        }
    }

    /**
     * Transforms from file to file.
     * 从文件转换到文件。
     *
     * @param input  the input file path | 输入文件路径
     * @param output the output file path | 输出文件路径
     */
    public void transform(Path input, Path output) {
        try (InputStream is = Files.newInputStream(input);
             OutputStream os = Files.newOutputStream(output)) {
            transform(new StreamSource(is), new StreamResult(os));
        } catch (IOException e) {
            throw new XmlTransformException("Failed to transform file: " + input + " -> " + output, e);
        }
    }

    /**
     * Transforms to an output stream.
     * 转换到输出流。
     *
     * @param xml    the XML string | XML 字符串
     * @param output the output stream | 输出流
     */
    public void transform(String xml, OutputStream output) {
        transform(new StreamSource(new StringReader(xml)), new StreamResult(output));
    }

    /**
     * Transforms an XML string to XmlDocument.
     * 将 XML 字符串转换为 XmlDocument。
     *
     * @param xml the XML string | XML 字符串
     * @return the transformed document | 转换后的文档
     */
    public XmlDocument transformToDocument(String xml) {
        DOMResult result = new DOMResult();
        transform(new StreamSource(new StringReader(xml)), result);
        return XmlDocument.of((org.w3c.dom.Document) result.getNode());
    }

    /**
     * Transforms an XmlDocument to XmlDocument.
     * 将 XmlDocument 转换为 XmlDocument。
     *
     * @param document the XML document | XML 文档
     * @return the transformed document | 转换后的文档
     */
    public XmlDocument transformToDocument(XmlDocument document) {
        DOMResult result = new DOMResult();
        transform(new DOMSource(document.getDocument()), result);
        return XmlDocument.of((org.w3c.dom.Document) result.getNode());
    }

    /**
     * Performs the transformation.
     * 执行转换。
     *
     * @param source the source | 源
     * @param result the result | 结果
     */
    private void transform(Source source, Result result) {
        try {
            Transformer transformer = templates.newTransformer();

            // Set parameters
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                transformer.setParameter(entry.getKey(), entry.getValue());
            }

            // Set output properties
            for (Map.Entry<String, String> entry : outputProperties.entrySet()) {
                transformer.setOutputProperty(entry.getKey(), entry.getValue());
            }

            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new XmlTransformException("XSLT transformation failed", e);
        }
    }

    /**
     * Gets the underlying Templates object.
     * 获取底层 Templates 对象。
     *
     * @return the Templates | Templates 对象
     */
    public Templates getTemplates() {
        return templates;
    }
}
