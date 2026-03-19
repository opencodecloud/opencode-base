package cloud.opencode.base.xml.validate;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.exception.XmlValidationException;
import cloud.opencode.base.xml.security.XmlSecurity;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Schema Validator - XML Schema (XSD) validation
 * Schema 验证器 - XML Schema (XSD) 验证
 *
 * <p>This class provides XML validation against XSD schemas.</p>
 * <p>此类提供针对 XSD 模式的 XML 验证。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Validate XML against schema
 * ValidationResult result = SchemaValidator.of(schemaPath)
 *     .validate(xml);
 *
 * if (!result.isValid()) {
 *     System.err.println(result.getErrorSummary());
 * }
 *
 * // Validate and throw on error
 * SchemaValidator.of(schemaPath)
 *     .validateOrThrow(xml);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>XML Schema (XSD) validation - XML Schema（XSD）验证</li>
 *   <li>Validate from string, file, or input stream - 从字符串、文件或输入流验证</li>
 *   <li>Detailed error and warning collection - 详细的错误和警告收集</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class SchemaValidator {

    private final Schema schema;

    private SchemaValidator(Schema schema) {
        this.schema = schema;
    }

    /**
     * Creates a validator from a schema file.
     * 从模式文件创建验证器。
     *
     * @param schemaPath the schema file path | 模式文件路径
     * @return a new validator | 新验证器
     */
    public static SchemaValidator of(Path schemaPath) {
        try {
            SchemaFactory factory = createSecureSchemaFactory();
            Schema schema = factory.newSchema(schemaPath.toFile());
            return new SchemaValidator(schema);
        } catch (SAXException e) {
            throw new XmlValidationException("Failed to load schema: " + schemaPath, e);
        }
    }

    /**
     * Creates a validator from a schema string.
     * 从模式字符串创建验证器。
     *
     * @param schemaXml the XSD schema string | XSD 模式字符串
     * @return a new validator | 新验证器
     */
    public static SchemaValidator of(String schemaXml) {
        try {
            SchemaFactory factory = createSecureSchemaFactory();
            Schema schema = factory.newSchema(new StreamSource(new StringReader(schemaXml)));
            return new SchemaValidator(schema);
        } catch (SAXException e) {
            throw new XmlValidationException("Failed to parse schema", e);
        }
    }

    /**
     * Creates a validator from a schema input stream.
     * 从模式输入流创建验证器。
     *
     * @param input the schema input stream | 模式输入流
     * @return a new validator | 新验证器
     */
    public static SchemaValidator of(InputStream input) {
        try {
            SchemaFactory factory = createSecureSchemaFactory();
            Schema schema = factory.newSchema(new StreamSource(input));
            return new SchemaValidator(schema);
        } catch (SAXException e) {
            throw new XmlValidationException("Failed to parse schema from stream", e);
        }
    }

    /**
     * Creates a validator from multiple schema sources.
     * 从多个模式源创建验证器。
     *
     * @param sources the schema sources | 模式源
     * @return a new validator | 新验证器
     */
    public static SchemaValidator of(Source... sources) {
        try {
            SchemaFactory factory = createSecureSchemaFactory();
            Schema schema = factory.newSchema(sources);
            return new SchemaValidator(schema);
        } catch (SAXException e) {
            throw new XmlValidationException("Failed to parse schemas", e);
        }
    }

    // ==================== Validation | 验证 ====================

    /**
     * Validates an XML string.
     * 验证 XML 字符串。
     *
     * @param xml the XML string | XML 字符串
     * @return the validation result | 验证结果
     */
    public ValidationResult validate(String xml) {
        return validate(new StreamSource(new StringReader(xml)));
    }

    /**
     * Validates an XmlDocument.
     * 验证 XmlDocument。
     *
     * @param document the XML document | XML 文档
     * @return the validation result | 验证结果
     */
    public ValidationResult validate(XmlDocument document) {
        return validate(new DOMSource(document.getDocument()));
    }

    /**
     * Validates an XML file.
     * 验证 XML 文件。
     *
     * @param path the XML file path | XML 文件路径
     * @return the validation result | 验证结果
     */
    public ValidationResult validate(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            return validate(new StreamSource(is));
        } catch (IOException e) {
            throw new XmlValidationException("Failed to read file: " + path, e);
        }
    }

    /**
     * Validates an XML input stream.
     * 验证 XML 输入流。
     *
     * @param input the XML input stream | XML 输入流
     * @return the validation result | 验证结果
     */
    public ValidationResult validate(InputStream input) {
        return validate(new StreamSource(input));
    }

    /**
     * Validates from a Source.
     * 从 Source 验证。
     *
     * @param source the XML source | XML 源
     * @return the validation result | 验证结果
     */
    public ValidationResult validate(Source source) {
        ValidationResult result = new ValidationResult();

        try {
            Validator validator = schema.newValidator();
            validator.setErrorHandler(new CollectingErrorHandler(result));
            validator.validate(source);
        } catch (SAXException | IOException e) {
            result.addError(e.getMessage(), -1, -1);
        }

        return result;
    }

    /**
     * Validates and throws if invalid.
     * 验证，如果无效则抛出异常。
     *
     * @param xml the XML string | XML 字符串
     * @throws XmlValidationException if validation fails | 如果验证失败则抛出
     */
    public void validateOrThrow(String xml) {
        ValidationResult result = validate(xml);
        if (!result.isValid()) {
            throw new XmlValidationException(result.getErrorMessages());
        }
    }

    /**
     * Validates and throws if invalid.
     * 验证，如果无效则抛出异常。
     *
     * @param document the XML document | XML 文档
     * @throws XmlValidationException if validation fails | 如果验证失败则抛出
     */
    public void validateOrThrow(XmlDocument document) {
        ValidationResult result = validate(document);
        if (!result.isValid()) {
            throw new XmlValidationException(result.getErrorMessages());
        }
    }

    /**
     * Validates and throws if invalid.
     * 验证，如果无效则抛出异常。
     *
     * @param path the XML file path | XML 文件路径
     * @throws XmlValidationException if validation fails | 如果验证失败则抛出
     */
    public void validateOrThrow(Path path) {
        ValidationResult result = validate(path);
        if (!result.isValid()) {
            throw new XmlValidationException(result.getErrorMessages());
        }
    }

    /**
     * Checks if an XML string is valid.
     * 检查 XML 字符串是否有效。
     *
     * @param xml the XML string | XML 字符串
     * @return true if valid | 如果有效则返回 true
     */
    public boolean isValid(String xml) {
        return validate(xml).isValid();
    }

    /**
     * Checks if an XmlDocument is valid.
     * 检查 XmlDocument 是否有效。
     *
     * @param document the XML document | XML 文档
     * @return true if valid | 如果有效则返回 true
     */
    public boolean isValid(XmlDocument document) {
        return validate(document).isValid();
    }

    /**
     * Gets the underlying Schema.
     * 获取底层 Schema。
     *
     * @return the Schema | Schema 对象
     */
    public Schema getSchema() {
        return schema;
    }

    private static SchemaFactory createSecureSchemaFactory() {
        return XmlSecurity.createSecureSchemaFactory();
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
