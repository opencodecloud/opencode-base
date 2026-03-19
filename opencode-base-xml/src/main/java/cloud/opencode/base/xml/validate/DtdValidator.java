package cloud.opencode.base.xml.validate;

import cloud.opencode.base.xml.exception.XmlValidationException;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * DTD Validator - Document Type Definition validation
 * DTD 验证器 - 文档类型定义验证
 *
 * <p>This class provides XML validation against DTD (Document Type Definition).</p>
 * <p>此类提供针对 DTD（文档类型定义）的 XML 验证。</p>
 *
 * <p><strong>Security Note | 安全注意:</strong></p>
 * <p>DTD validation requires enabling DTD processing, which can be a security risk.
 * Use with caution and only with trusted XML sources.</p>
 * <p>DTD 验证需要启用 DTD 处理，这可能是安全风险。请谨慎使用，仅与受信任的 XML 源一起使用。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Validate XML with embedded DTD
 * ValidationResult result = DtdValidator.create()
 *     .validate(xmlWithDtd);
 *
 * // Validate XML against external DTD
 * ValidationResult result = DtdValidator.create()
 *     .withExternalDtd(dtdPath)
 *     .validate(xml);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>DTD (Document Type Definition) validation - DTD（文档类型定义）验证</li>
 *   <li>Support for embedded and external DTDs - 支持嵌入式和外部 DTD</li>
 *   <li>Fluent builder API - 流式构建器 API</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class DtdValidator {

    private String externalDtd;
    private boolean allowExternalEntities = false;

    private DtdValidator() {
    }

    /**
     * Creates a new DTD validator.
     * 创建新的 DTD 验证器。
     *
     * @return a new validator | 新验证器
     */
    public static DtdValidator create() {
        return new DtdValidator();
    }

    /**
     * Sets an external DTD for validation.
     * 设置用于验证的外部 DTD。
     *
     * @param dtdPath the DTD file path | DTD 文件路径
     * @return this validator for chaining | 此验证器以便链式调用
     */
    public DtdValidator withExternalDtd(Path dtdPath) {
        this.externalDtd = dtdPath.toUri().toString();
        return this;
    }

    /**
     * Sets an external DTD for validation by URL.
     * 通过 URL 设置用于验证的外部 DTD。
     *
     * @param dtdUrl the DTD URL | DTD URL
     * @return this validator for chaining | 此验证器以便链式调用
     */
    public DtdValidator withExternalDtd(String dtdUrl) {
        this.externalDtd = dtdUrl;
        return this;
    }

    /**
     * Allows external entity resolution (security risk).
     * 允许外部实体解析（安全风险）。
     *
     * @param allow whether to allow | 是否允许
     * @return this validator for chaining | 此验证器以便链式调用
     */
    public DtdValidator allowExternalEntities(boolean allow) {
        this.allowExternalEntities = allow;
        return this;
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
        return validate(new InputSource(new StringReader(xml)));
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
            InputSource source = new InputSource(is);
            source.setSystemId(path.toUri().toString());
            return validate(source);
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
        return validate(new InputSource(input));
    }

    /**
     * Validates from an InputSource.
     * 从 InputSource 验证。
     *
     * @param source the XML source | XML 源
     * @return the validation result | 验证结果
     */
    public ValidationResult validate(InputSource source) {
        ValidationResult result = new ValidationResult();

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            factory.setNamespaceAware(true);

            // Configure security features
            if (!allowExternalEntities) {
                try {
                    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                } catch (Exception ignored) {
                    // Some parsers may not support these features
                }
            }

            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();

            // Set error handler
            reader.setErrorHandler(new CollectingErrorHandler(result));

            // Set entity resolver for external DTD
            if (externalDtd != null) {
                reader.setEntityResolver(new ExternalDtdResolver(externalDtd));
            }

            reader.parse(source);

        } catch (SAXParseException e) {
            result.addError(e.getMessage(), e.getLineNumber(), e.getColumnNumber());
        } catch (Exception e) {
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
     * Error handler that collects errors into a ValidationResult.
     * 将错误收集到 ValidationResult 中的错误处理器。
     */
    private static class CollectingErrorHandler extends DefaultHandler {
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

    /**
     * Entity resolver for external DTD.
     * 外部 DTD 的实体解析器。
     */
    private static class ExternalDtdResolver implements EntityResolver {
        private final String dtdUrl;

        ExternalDtdResolver(String dtdUrl) {
            this.dtdUrl = dtdUrl;
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) {
            // Always use the configured external DTD
            return new InputSource(dtdUrl);
        }
    }
}
