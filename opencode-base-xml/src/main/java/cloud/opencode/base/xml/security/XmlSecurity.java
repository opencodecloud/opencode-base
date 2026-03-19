package cloud.opencode.base.xml.security;

import cloud.opencode.base.xml.exception.OpenXmlException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;

/**
 * XML Security Configuration - Provides secure XML parser configuration
 * XML 安全配置 - 提供安全的 XML 解析器配置
 *
 * <p>This class provides methods to configure XML parsers securely to prevent
 * XXE (XML External Entity) attacks and other XML-related security threats.</p>
 * <p>此类提供配置安全 XML 解析器的方法，以防止 XXE（XML 外部实体）攻击和其他 XML 相关安全威胁。</p>
 *
 * <p><strong>Security Features | 安全特性:</strong></p>
 * <ul>
 *   <li>Disables external entities (XXE protection) - 禁用外部实体（XXE 防护）</li>
 *   <li>Disables DTD processing - 禁用 DTD 处理</li>
 *   <li>Limits entity expansion - 限制实体扩展</li>
 *   <li>Disables external parameter entities - 禁用外部参数实体</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create secure DocumentBuilderFactory
 * DocumentBuilderFactory factory = XmlSecurity.createSecureDocumentBuilderFactory();
 *
 * // Or secure an existing factory
 * DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 * XmlSecurity.secure(factory);
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class XmlSecurity {

    /**
     * Default entity expansion limit.
     * 默认实体扩展限制。
     */
    public static final int DEFAULT_ENTITY_EXPANSION_LIMIT = 64000;

    /**
     * Feature to disable external general entities.
     * 禁用外部通用实体的特性。
     */
    private static final String FEATURE_EXTERNAL_GENERAL_ENTITIES =
        "http://xml.org/sax/features/external-general-entities";

    /**
     * Feature to disable external parameter entities.
     * 禁用外部参数实体的特性。
     */
    private static final String FEATURE_EXTERNAL_PARAMETER_ENTITIES =
        "http://xml.org/sax/features/external-parameter-entities";

    /**
     * Feature to disable DTD loading.
     * 禁用 DTD 加载的特性。
     */
    private static final String FEATURE_LOAD_EXTERNAL_DTD =
        "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    /**
     * Feature to disallow DOCTYPE declarations.
     * 禁止 DOCTYPE 声明的特性。
     */
    private static final String FEATURE_DISALLOW_DOCTYPE =
        "http://apache.org/xml/features/disallow-doctype-decl";

    private XmlSecurity() {
        // Utility class
    }

    /**
     * Configures a DocumentBuilderFactory with secure settings.
     * 使用安全设置配置 DocumentBuilderFactory。
     *
     * @param factory the factory to configure | 要配置的工厂
     * @return the configured factory | 配置后的工厂
     */
    public static DocumentBuilderFactory secure(DocumentBuilderFactory factory) {
        try {
            // Disable external entities
            factory.setFeature(FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
            factory.setFeature(FEATURE_EXTERNAL_PARAMETER_ENTITIES, false);
            factory.setFeature(FEATURE_LOAD_EXTERNAL_DTD, false);
            factory.setFeature(FEATURE_DISALLOW_DOCTYPE, true);

            // Set secure processing
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // Disable XInclude
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            // Set access restrictions
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (ParserConfigurationException e) {
            throw new OpenXmlException("Failed to configure secure DocumentBuilderFactory", e);
        }
        return factory;
    }

    /**
     * Configures a SAXParserFactory with secure settings.
     * 使用安全设置配置 SAXParserFactory。
     *
     * @param factory the factory to configure | 要配置的工厂
     * @return the configured factory | 配置后的工厂
     */
    public static SAXParserFactory secure(SAXParserFactory factory) {
        try {
            // Disable external entities
            factory.setFeature(FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
            factory.setFeature(FEATURE_EXTERNAL_PARAMETER_ENTITIES, false);
            factory.setFeature(FEATURE_LOAD_EXTERNAL_DTD, false);
            factory.setFeature(FEATURE_DISALLOW_DOCTYPE, true);

            // Set secure processing
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // Disable XInclude
            factory.setXIncludeAware(false);
        } catch (Exception e) {
            throw new OpenXmlException("Failed to configure secure SAXParserFactory", e);
        }
        return factory;
    }

    /**
     * Configures an XMLInputFactory with secure settings.
     * 使用安全设置配置 XMLInputFactory。
     *
     * @param factory the factory to configure | 要配置的工厂
     * @return the configured factory | 配置后的工厂
     */
    public static XMLInputFactory secure(XMLInputFactory factory) {
        // Disable DTD support
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

        // Set entity resolver to null
        factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);

        return factory;
    }

    /**
     * Configures a TransformerFactory with secure settings.
     * 使用安全设置配置 TransformerFactory。
     *
     * @param factory the factory to configure | 要配置的工厂
     * @return the configured factory | 配置后的工厂
     */
    public static TransformerFactory secure(TransformerFactory factory) {
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        } catch (Exception e) {
            throw new OpenXmlException("Failed to configure secure TransformerFactory", e);
        }
        return factory;
    }

    /**
     * Configures a SchemaFactory with secure settings.
     * 使用安全设置配置 SchemaFactory。
     *
     * @param factory the factory to configure | 要配置的工厂
     * @return the configured factory | 配置后的工厂
     */
    public static SchemaFactory secure(SchemaFactory factory) {
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (Exception e) {
            throw new OpenXmlException("Failed to configure secure SchemaFactory", e);
        }
        return factory;
    }

    /**
     * Creates a secure DocumentBuilderFactory.
     * 创建安全的 DocumentBuilderFactory。
     *
     * @return a secure factory | 安全的工厂
     */
    public static DocumentBuilderFactory createSecureDocumentBuilderFactory() {
        return secure(DocumentBuilderFactory.newInstance());
    }

    /**
     * Creates a secure SAXParserFactory.
     * 创建安全的 SAXParserFactory。
     *
     * @return a secure factory | 安全的工厂
     */
    public static SAXParserFactory createSecureSAXParserFactory() {
        return secure(SAXParserFactory.newInstance());
    }

    /**
     * Creates a secure XMLInputFactory.
     * 创建安全的 XMLInputFactory。
     *
     * @return a secure factory | 安全的工厂
     */
    public static XMLInputFactory createSecureXMLInputFactory() {
        return secure(XMLInputFactory.newInstance());
    }

    /**
     * Creates a secure TransformerFactory.
     * 创建安全的 TransformerFactory。
     *
     * @return a secure factory | 安全的工厂
     */
    public static TransformerFactory createSecureTransformerFactory() {
        return secure(TransformerFactory.newInstance());
    }

    /**
     * Creates a secure SchemaFactory for XSD validation.
     * 创建用于 XSD 验证的安全 SchemaFactory。
     *
     * @return a secure factory | 安全的工厂
     */
    public static SchemaFactory createSecureSchemaFactory() {
        return secure(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI));
    }

    /**
     * Disables external entities on a DocumentBuilderFactory.
     * 在 DocumentBuilderFactory 上禁用外部实体。
     *
     * @param factory the factory to configure | 要配置的工厂
     */
    public static void disableExternalEntities(DocumentBuilderFactory factory) {
        try {
            factory.setFeature(FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
            factory.setFeature(FEATURE_EXTERNAL_PARAMETER_ENTITIES, false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (ParserConfigurationException e) {
            throw new OpenXmlException("Failed to disable external entities", e);
        }
    }

    /**
     * Disables DTD processing on a DocumentBuilderFactory.
     * 在 DocumentBuilderFactory 上禁用 DTD 处理。
     *
     * @param factory the factory to configure | 要配置的工厂
     */
    public static void disableDtd(DocumentBuilderFactory factory) {
        try {
            factory.setFeature(FEATURE_DISALLOW_DOCTYPE, true);
            factory.setFeature(FEATURE_LOAD_EXTERNAL_DTD, false);
        } catch (ParserConfigurationException e) {
            throw new OpenXmlException("Failed to disable DTD", e);
        }
    }

    /**
     * Sets the entity expansion limit on a DocumentBuilderFactory.
     * 在 DocumentBuilderFactory 上设置实体扩展限制。
     *
     * @param factory the factory to configure | 要配置的工厂
     * @param limit   the maximum number of entity expansions | 最大实体扩展数
     */
    public static void setEntityExpansionLimit(DocumentBuilderFactory factory, int limit) {
        try {
            factory.setAttribute("http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit",
                String.valueOf(limit));
        } catch (IllegalArgumentException e) {
            // Some implementations may not support this attribute
        }
    }
}
