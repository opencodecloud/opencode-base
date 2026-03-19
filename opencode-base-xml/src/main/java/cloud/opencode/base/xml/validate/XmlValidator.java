package cloud.opencode.base.xml.validate;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.exception.XmlValidationException;
import cloud.opencode.base.xml.security.SecureParserFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * XML Validator - XML well-formedness and validation utilities
 * XML 验证器 - XML 格式良好性和验证工具
 *
 * <p>This class provides static utilities for XML validation.</p>
 * <p>此类提供 XML 验证的静态工具。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check if XML is well-formed
 * boolean isWellFormed = XmlValidator.isWellFormed(xml);
 *
 * // Validate well-formedness and get errors
 * ValidationResult result = XmlValidator.validateWellFormedness(xml);
 *
 * // Validate against XSD schema
 * ValidationResult result = XmlValidator.validateSchema(xml, schemaPath);
 *
 * // Validate against DTD
 * ValidationResult result = XmlValidator.validateDtd(xml, dtdPath);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>XML well-formedness validation - XML 格式良好性验证</li>
 *   <li>Schema and DTD validation delegation - Schema 和 DTD 验证委托</li>
 *   <li>Static utility methods for quick validation - 用于快速验证的静态工具方法</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility, secure parsing) - 线程安全: 是（无状态工具，安全解析）</li>
 *   <li>Null-safe: No (throws on null XML) - 空值安全: 否（null XML 抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class XmlValidator {

    private XmlValidator() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    // ==================== Well-formedness | 格式良好性 ====================

    /**
     * Checks if an XML string is well-formed.
     * 检查 XML 字符串是否格式良好。
     *
     * @param xml the XML string | XML 字符串
     * @return true if well-formed | 如果格式良好则返回 true
     */
    public static boolean isWellFormed(String xml) {
        return validateWellFormedness(xml).isValid();
    }

    /**
     * Checks if an XML file is well-formed.
     * 检查 XML 文件是否格式良好。
     *
     * @param path the XML file path | XML 文件路径
     * @return true if well-formed | 如果格式良好则返回 true
     */
    public static boolean isWellFormed(Path path) {
        return validateWellFormedness(path).isValid();
    }

    /**
     * Validates XML well-formedness.
     * 验证 XML 格式良好性。
     *
     * @param xml the XML string | XML 字符串
     * @return the validation result | 验证结果
     */
    public static ValidationResult validateWellFormedness(String xml) {
        ValidationResult result = new ValidationResult();

        try {
            DocumentBuilder builder = SecureParserFactory.createDocumentBuilder();
            builder.setErrorHandler(new CollectingErrorHandler(result));
            builder.parse(new InputSource(new StringReader(xml)));
        } catch (SAXParseException e) {
            result.addError(e.getMessage(), e.getLineNumber(), e.getColumnNumber());
        } catch (SAXException | IOException e) {
            result.addError(e.getMessage(), -1, -1);
        }

        return result;
    }

    /**
     * Validates XML file well-formedness.
     * 验证 XML 文件格式良好性。
     *
     * @param path the XML file path | XML 文件路径
     * @return the validation result | 验证结果
     */
    public static ValidationResult validateWellFormedness(Path path) {
        ValidationResult result = new ValidationResult();

        try (InputStream is = Files.newInputStream(path)) {
            DocumentBuilder builder = SecureParserFactory.createDocumentBuilder();
            builder.setErrorHandler(new CollectingErrorHandler(result));
            builder.parse(is);
        } catch (SAXParseException e) {
            result.addError(e.getMessage(), e.getLineNumber(), e.getColumnNumber());
        } catch (SAXException | IOException e) {
            result.addError(e.getMessage(), -1, -1);
        }

        return result;
    }

    /**
     * Validates well-formedness and throws if invalid.
     * 验证格式良好性，如果无效则抛出异常。
     *
     * @param xml the XML string | XML 字符串
     * @throws XmlValidationException if not well-formed | 如果格式不良好则抛出
     */
    public static void requireWellFormed(String xml) {
        ValidationResult result = validateWellFormedness(xml);
        if (!result.isValid()) {
            throw new XmlValidationException("XML is not well-formed", result.getErrorMessages());
        }
    }

    /**
     * Validates file well-formedness and throws if invalid.
     * 验证文件格式良好性，如果无效则抛出异常。
     *
     * @param path the XML file path | XML 文件路径
     * @throws XmlValidationException if not well-formed | 如果格式不良好则抛出
     */
    public static void requireWellFormed(Path path) {
        ValidationResult result = validateWellFormedness(path);
        if (!result.isValid()) {
            throw new XmlValidationException("XML is not well-formed: " + path, result.getErrorMessages());
        }
    }

    // ==================== Schema Validation | Schema 验证 ====================

    /**
     * Validates XML against an XSD schema.
     * 针对 XSD 模式验证 XML。
     *
     * @param xml        the XML string | XML 字符串
     * @param schemaPath the schema file path | 模式文件路径
     * @return the validation result | 验证结果
     */
    public static ValidationResult validateSchema(String xml, Path schemaPath) {
        return SchemaValidator.of(schemaPath).validate(xml);
    }

    /**
     * Validates XML against an XSD schema string.
     * 针对 XSD 模式字符串验证 XML。
     *
     * @param xml       the XML string | XML 字符串
     * @param schemaXml the XSD schema string | XSD 模式字符串
     * @return the validation result | 验证结果
     */
    public static ValidationResult validateSchema(String xml, String schemaXml) {
        return SchemaValidator.of(schemaXml).validate(xml);
    }

    /**
     * Validates XML file against an XSD schema.
     * 针对 XSD 模式验证 XML 文件。
     *
     * @param xmlPath    the XML file path | XML 文件路径
     * @param schemaPath the schema file path | 模式文件路径
     * @return the validation result | 验证结果
     */
    public static ValidationResult validateSchema(Path xmlPath, Path schemaPath) {
        return SchemaValidator.of(schemaPath).validate(xmlPath);
    }

    /**
     * Validates XmlDocument against an XSD schema.
     * 针对 XSD 模式验证 XmlDocument。
     *
     * @param document   the XML document | XML 文档
     * @param schemaPath the schema file path | 模式文件路径
     * @return the validation result | 验证结果
     */
    public static ValidationResult validateSchema(XmlDocument document, Path schemaPath) {
        return SchemaValidator.of(schemaPath).validate(document);
    }

    /**
     * Checks if XML is valid against a schema.
     * 检查 XML 是否对模式有效。
     *
     * @param xml        the XML string | XML 字符串
     * @param schemaPath the schema file path | 模式文件路径
     * @return true if valid | 如果有效则返回 true
     */
    public static boolean isValidAgainstSchema(String xml, Path schemaPath) {
        return validateSchema(xml, schemaPath).isValid();
    }

    // ==================== DTD Validation | DTD 验证 ====================

    /**
     * Validates XML with embedded DTD.
     * 验证带有嵌入式 DTD 的 XML。
     *
     * @param xml the XML string with DTD | 带 DTD 的 XML 字符串
     * @return the validation result | 验证结果
     */
    public static ValidationResult validateDtd(String xml) {
        return DtdValidator.create().validate(xml);
    }

    /**
     * Validates XML against an external DTD.
     * 针对外部 DTD 验证 XML。
     *
     * @param xml     the XML string | XML 字符串
     * @param dtdPath the DTD file path | DTD 文件路径
     * @return the validation result | 验证结果
     */
    public static ValidationResult validateDtd(String xml, Path dtdPath) {
        return DtdValidator.create().withExternalDtd(dtdPath).validate(xml);
    }

    /**
     * Validates XML file against an external DTD.
     * 针对外部 DTD 验证 XML 文件。
     *
     * @param xmlPath the XML file path | XML 文件路径
     * @param dtdPath the DTD file path | DTD 文件路径
     * @return the validation result | 验证结果
     */
    public static ValidationResult validateDtd(Path xmlPath, Path dtdPath) {
        return DtdValidator.create().withExternalDtd(dtdPath).validate(xmlPath);
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a SchemaValidator.
     * 创建 SchemaValidator。
     *
     * @param schemaPath the schema file path | 模式文件路径
     * @return a new SchemaValidator | 新的 SchemaValidator
     */
    public static SchemaValidator schemaValidator(Path schemaPath) {
        return SchemaValidator.of(schemaPath);
    }

    /**
     * Creates a SchemaValidator from schema string.
     * 从模式字符串创建 SchemaValidator。
     *
     * @param schemaXml the XSD schema string | XSD 模式字符串
     * @return a new SchemaValidator | 新的 SchemaValidator
     */
    public static SchemaValidator schemaValidator(String schemaXml) {
        return SchemaValidator.of(schemaXml);
    }

    /**
     * Creates a DtdValidator.
     * 创建 DtdValidator。
     *
     * @return a new DtdValidator | 新的 DtdValidator
     */
    public static DtdValidator dtdValidator() {
        return DtdValidator.create();
    }

    /**
     * Error handler that collects errors into a ValidationResult.
     * 将错误收集到 ValidationResult 中的错误处理器。
     */
    private static class CollectingErrorHandler implements ErrorHandler {
        private final ValidationResult result;

        CollectingErrorHandler(ValidationResult result) {
            this.result = result;
        }

        @Override
        public void warning(SAXParseException e) {
            result.addWarning(e.getMessage(), e.getLineNumber(), e.getColumnNumber());
        }

        @Override
        public void error(SAXParseException e) {
            result.addError(e.getMessage(), e.getLineNumber(), e.getColumnNumber());
        }

        @Override
        public void fatalError(SAXParseException e) {
            result.addError(e.getMessage(), e.getLineNumber(), e.getColumnNumber());
        }
    }
}
