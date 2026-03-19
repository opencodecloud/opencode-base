package cloud.opencode.base.xml.transform;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.XmlElement;
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

/**
 * XML Transformer - XML serialization and formatting utilities
 * XML 转换器 - XML 序列化和格式化工具
 *
 * <p>This class provides utilities for XML serialization, formatting, and identity transformation.</p>
 * <p>此类提供 XML 序列化、格式化和恒等转换的工具。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Format XML
 * String formatted = XmlTransformer.format(xml, 4);
 *
 * // Minify XML
 * String minified = XmlTransformer.minify(xml);
 *
 * // Serialize document to file
 * XmlTransformer.serialize(document, Path.of("output.xml"), 4);
 *
 * // Copy/clone an element
 * XmlElement clone = XmlTransformer.clone(element);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>XML serialization and formatting - XML 序列化和格式化</li>
 *   <li>Minification and pretty-printing - 压缩和美化输出</li>
 *   <li>Element cloning and identity transformation - 元素克隆和恒等转换</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility, secure factory) - 线程安全: 是（无状态工具，安全工厂）</li>
 *   <li>Null-safe: No (throws on null input) - 空值安全: 否（null 输入抛异常）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for all format/minify/serialize operations where n=document or XML string size (full Transformer traversal) - 时间复杂度: 所有 format/minify/serialize 操作为 O(n)，n 为文档或 XML 字符串大小（完整 Transformer 遍历）</li>
 *   <li>Space complexity: O(n) for output string or stream buffer - 空间复杂度: 输出字符串或流缓冲区为 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class XmlTransformer {

    private XmlTransformer() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    // ==================== Format | 格式化 ====================

    /**
     * Formats an XML string with indentation.
     * 使用缩进格式化 XML 字符串。
     *
     * @param xml    the XML string | XML 字符串
     * @param indent the indent spaces | 缩进空格数
     * @return the formatted XML | 格式化的 XML
     */
    public static String format(String xml, int indent) {
        return transform(xml, true, indent, "UTF-8", false);
    }

    /**
     * Formats an XML string with default indentation (4 spaces).
     * 使用默认缩进（4 个空格）格式化 XML 字符串。
     *
     * @param xml the XML string | XML 字符串
     * @return the formatted XML | 格式化的 XML
     */
    public static String format(String xml) {
        return format(xml, 4);
    }

    /**
     * Formats an XmlDocument with indentation.
     * 使用缩进格式化 XmlDocument。
     *
     * @param document the document | 文档
     * @param indent   the indent spaces | 缩进空格数
     * @return the formatted XML | 格式化的 XML
     */
    public static String format(XmlDocument document, int indent) {
        return transformDocument(document, true, indent, "UTF-8", false);
    }

    /**
     * Minifies an XML string by removing whitespace.
     * 通过删除空白字符来压缩 XML 字符串。
     *
     * @param xml the XML string | XML 字符串
     * @return the minified XML | 压缩的 XML
     */
    public static String minify(String xml) {
        return transform(xml, false, 0, "UTF-8", false);
    }

    /**
     * Minifies an XmlDocument by removing whitespace.
     * 通过删除空白字符来压缩 XmlDocument。
     *
     * @param document the document | 文档
     * @return the minified XML | 压缩的 XML
     */
    public static String minify(XmlDocument document) {
        return transformDocument(document, false, 0, "UTF-8", false);
    }

    // ==================== Serialize | 序列化 ====================

    /**
     * Serializes an XmlDocument to a string.
     * 将 XmlDocument 序列化为字符串。
     *
     * @param document the document | 文档
     * @return the XML string | XML 字符串
     */
    public static String serialize(XmlDocument document) {
        return transformDocument(document, false, 0, "UTF-8", false);
    }

    /**
     * Serializes an XmlDocument to a formatted string.
     * 将 XmlDocument 序列化为格式化的字符串。
     *
     * @param document the document | 文档
     * @param indent   the indent spaces | 缩进空格数
     * @return the XML string | XML 字符串
     */
    public static String serialize(XmlDocument document, int indent) {
        return transformDocument(document, true, indent, "UTF-8", false);
    }

    /**
     * Serializes an XmlDocument to a file.
     * 将 XmlDocument 序列化到文件。
     *
     * @param document the document | 文档
     * @param path     the output path | 输出路径
     */
    public static void serialize(XmlDocument document, Path path) {
        serialize(document, path, 0, "UTF-8");
    }

    /**
     * Serializes an XmlDocument to a file with formatting.
     * 将 XmlDocument 序列化到文件并格式化。
     *
     * @param document the document | 文档
     * @param path     the output path | 输出路径
     * @param indent   the indent spaces | 缩进空格数
     */
    public static void serialize(XmlDocument document, Path path, int indent) {
        serialize(document, path, indent, "UTF-8");
    }

    /**
     * Serializes an XmlDocument to a file with options.
     * 使用选项将 XmlDocument 序列化到文件。
     *
     * @param document the document | 文档
     * @param path     the output path | 输出路径
     * @param indent   the indent spaces | 缩进空格数
     * @param encoding the encoding | 编码
     */
    public static void serialize(XmlDocument document, Path path, int indent, String encoding) {
        try (OutputStream os = Files.newOutputStream(path)) {
            serializeToStream(document, os, indent > 0, indent, encoding);
        } catch (IOException e) {
            throw new XmlTransformException("Failed to serialize to file: " + path, e);
        }
    }

    /**
     * Serializes an XmlDocument to an output stream.
     * 将 XmlDocument 序列化到输出流。
     *
     * @param document the document | 文档
     * @param output   the output stream | 输出流
     */
    public static void serialize(XmlDocument document, OutputStream output) {
        serializeToStream(document, output, false, 0, "UTF-8");
    }

    /**
     * Serializes an XmlDocument to an output stream with formatting.
     * 将 XmlDocument 序列化到输出流并格式化。
     *
     * @param document the document | 文档
     * @param output   the output stream | 输出流
     * @param indent   the indent spaces | 缩进空格数
     */
    public static void serialize(XmlDocument document, OutputStream output, int indent) {
        serializeToStream(document, output, indent > 0, indent, "UTF-8");
    }

    /**
     * Serializes an XmlElement to a string.
     * 将 XmlElement 序列化为字符串。
     *
     * @param element the element | 元素
     * @return the XML string | XML 字符串
     */
    public static String serialize(XmlElement element) {
        return transformElement(element, false, 0, "UTF-8", true);
    }

    /**
     * Serializes an XmlElement to a formatted string.
     * 将 XmlElement 序列化为格式化的字符串。
     *
     * @param element the element | 元素
     * @param indent  the indent spaces | 缩进空格数
     * @return the XML string | XML 字符串
     */
    public static String serialize(XmlElement element, int indent) {
        return transformElement(element, indent > 0, indent, "UTF-8", true);
    }

    // ==================== Clone | 克隆 ====================

    /**
     * Clones an XmlDocument.
     * 克隆 XmlDocument。
     *
     * @param document the document to clone | 要克隆的文档
     * @return the cloned document | 克隆的文档
     */
    public static XmlDocument clone(XmlDocument document) {
        try {
            Transformer transformer = createTransformer();
            DOMSource source = new DOMSource(document.getDocument());
            DOMResult result = new DOMResult();
            transformer.transform(source, result);
            return XmlDocument.of((org.w3c.dom.Document) result.getNode());
        } catch (TransformerException e) {
            throw new XmlTransformException("Failed to clone document", e);
        }
    }

    /**
     * Clones an XmlElement.
     * 克隆 XmlElement。
     *
     * @param element the element to clone | 要克隆的元素
     * @return the cloned element | 克隆的元素
     */
    public static XmlElement clone(XmlElement element) {
        org.w3c.dom.Element domElement = element.getElement();
        org.w3c.dom.Element cloned = (org.w3c.dom.Element) domElement.cloneNode(true);
        return XmlElement.of(cloned);
    }

    // ==================== Parse and Transform | 解析并转换 ====================

    /**
     * Parses and formats an XML string.
     * 解析并格式化 XML 字符串。
     *
     * @param xml    the XML string | XML 字符串
     * @param indent the indent spaces | 缩进空格数
     * @return the formatted XML | 格式化的 XML
     */
    public static String parseAndFormat(String xml, int indent) {
        XmlDocument doc = XmlDocument.parse(xml);
        return format(doc, indent);
    }

    /**
     * Converts an XML string to a canonical form.
     * 将 XML 字符串转换为规范形式。
     *
     * @param xml the XML string | XML 字符串
     * @return the canonical XML | 规范化的 XML
     */
    public static String canonicalize(String xml) {
        // Parse and re-serialize for basic canonicalization
        XmlDocument doc = XmlDocument.parse(xml);
        return serialize(doc);
    }

    // ==================== Internal | 内部方法 ====================

    private static String transform(String xml, boolean indent, int indentAmount,
                                    String encoding, boolean omitDeclaration) {
        try {
            Transformer transformer = createTransformer();
            configureTransformer(transformer, indent, indentAmount, encoding, omitDeclaration);

            StringWriter writer = new StringWriter();
            transformer.transform(
                new StreamSource(new StringReader(xml)),
                new StreamResult(writer)
            );
            return writer.toString();
        } catch (TransformerException e) {
            throw new XmlTransformException("Failed to transform XML", e);
        }
    }

    private static String transformDocument(XmlDocument document, boolean indent, int indentAmount,
                                            String encoding, boolean omitDeclaration) {
        try {
            Transformer transformer = createTransformer();
            configureTransformer(transformer, indent, indentAmount, encoding, omitDeclaration);

            StringWriter writer = new StringWriter();
            transformer.transform(
                new DOMSource(document.getDocument()),
                new StreamResult(writer)
            );
            return writer.toString();
        } catch (TransformerException e) {
            throw new XmlTransformException("Failed to transform document", e);
        }
    }

    private static String transformElement(XmlElement element, boolean indent, int indentAmount,
                                           String encoding, boolean omitDeclaration) {
        try {
            Transformer transformer = createTransformer();
            configureTransformer(transformer, indent, indentAmount, encoding, omitDeclaration);

            StringWriter writer = new StringWriter();
            transformer.transform(
                new DOMSource(element.getElement()),
                new StreamResult(writer)
            );
            return writer.toString();
        } catch (TransformerException e) {
            throw new XmlTransformException("Failed to transform element", e);
        }
    }

    private static void serializeToStream(XmlDocument document, OutputStream output,
                                          boolean indent, int indentAmount, String encoding) {
        try {
            Transformer transformer = createTransformer();
            configureTransformer(transformer, indent, indentAmount, encoding, false);

            transformer.transform(
                new DOMSource(document.getDocument()),
                new StreamResult(output)
            );
        } catch (TransformerException e) {
            throw new XmlTransformException("Failed to serialize to stream", e);
        }
    }

    private static Transformer createTransformer() {
        return SecureParserFactory.createTransformer();
    }

    private static void configureTransformer(Transformer transformer, boolean indent, int indentAmount,
                                             String encoding, boolean omitDeclaration) {
        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
        if (indent && indentAmount > 0) {
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                String.valueOf(indentAmount));
        }
        if (omitDeclaration) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
    }
}
