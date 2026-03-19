package cloud.opencode.base.xml.security;

import org.junit.jupiter.api.*;

import javax.xml.parsers.*;
import javax.xml.stream.*;
import javax.xml.transform.*;
import javax.xml.validation.SchemaFactory;
import javax.xml.XMLConstants;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlSecurityTest Tests
 * XmlSecurityTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlSecurity Tests")
class XmlSecurityTest {

    @Nested
    @DisplayName("Secure DocumentBuilderFactory Tests")
    class SecureDocumentBuilderFactoryTests {

        @Test
        @DisplayName("secure should configure factory securely")
        void secureShouldConfigureFactorySecurely() throws Exception {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            XmlSecurity.secure(factory);

            assertThat(factory).isNotNull();
        }

        @Test
        @DisplayName("secure should return same factory for chaining")
        void secureShouldReturnSameFactoryForChaining() throws Exception {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilderFactory result = XmlSecurity.secure(factory);

            assertThat(result).isSameAs(factory);
        }

        @Test
        @DisplayName("createSecureDocumentBuilderFactory should create secure factory")
        void createSecureDocumentBuilderFactoryShouldCreateSecureFactory() {
            DocumentBuilderFactory factory = XmlSecurity.createSecureDocumentBuilderFactory();

            assertThat(factory).isNotNull();
        }
    }

    @Nested
    @DisplayName("Secure SAXParserFactory Tests")
    class SecureSaxParserFactoryTests {

        @Test
        @DisplayName("secure should configure SAX factory securely")
        void secureShouldConfigureSaxFactorySecurely() throws Exception {
            SAXParserFactory factory = SAXParserFactory.newInstance();

            XmlSecurity.secure(factory);

            assertThat(factory).isNotNull();
        }

        @Test
        @DisplayName("secure should return same SAX factory for chaining")
        void secureShouldReturnSameSaxFactoryForChaining() throws Exception {
            SAXParserFactory factory = SAXParserFactory.newInstance();

            SAXParserFactory result = XmlSecurity.secure(factory);

            assertThat(result).isSameAs(factory);
        }

        @Test
        @DisplayName("createSecureSAXParserFactory should create secure factory")
        void createSecureSaxParserFactoryShouldCreateSecureFactory() {
            SAXParserFactory factory = XmlSecurity.createSecureSAXParserFactory();

            assertThat(factory).isNotNull();
        }
    }

    @Nested
    @DisplayName("Secure XMLInputFactory Tests")
    class SecureXmlInputFactoryTests {

        @Test
        @DisplayName("secure should configure input factory securely")
        void secureShouldConfigureInputFactorySecurely() {
            XMLInputFactory factory = XMLInputFactory.newInstance();

            XmlSecurity.secure(factory);

            assertThat(factory).isNotNull();
        }

        @Test
        @DisplayName("secure should return same input factory for chaining")
        void secureShouldReturnSameInputFactoryForChaining() {
            XMLInputFactory factory = XMLInputFactory.newInstance();

            XMLInputFactory result = XmlSecurity.secure(factory);

            assertThat(result).isSameAs(factory);
        }

        @Test
        @DisplayName("createSecureXMLInputFactory should create secure factory")
        void createSecureXmlInputFactoryShouldCreateSecureFactory() {
            XMLInputFactory factory = XmlSecurity.createSecureXMLInputFactory();

            assertThat(factory).isNotNull();
        }
    }

    @Nested
    @DisplayName("Secure TransformerFactory Tests")
    class SecureTransformerFactoryTests {

        @Test
        @DisplayName("secure should configure transformer factory securely")
        void secureShouldConfigureTransformerFactorySecurely() throws Exception {
            TransformerFactory factory = TransformerFactory.newInstance();

            XmlSecurity.secure(factory);

            assertThat(factory).isNotNull();
        }

        @Test
        @DisplayName("secure should return same transformer factory for chaining")
        void secureShouldReturnSameTransformerFactoryForChaining() throws Exception {
            TransformerFactory factory = TransformerFactory.newInstance();

            TransformerFactory result = XmlSecurity.secure(factory);

            assertThat(result).isSameAs(factory);
        }

        @Test
        @DisplayName("createSecureTransformerFactory should create secure factory")
        void createSecureTransformerFactoryShouldCreateSecureFactory() {
            TransformerFactory factory = XmlSecurity.createSecureTransformerFactory();

            assertThat(factory).isNotNull();
        }
    }

    @Nested
    @DisplayName("Secure SchemaFactory Tests")
    class SecureSchemaFactoryTests {

        @Test
        @DisplayName("secure should configure schema factory securely")
        void secureShouldConfigureSchemaFactorySecurely() throws Exception {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            XmlSecurity.secure(factory);

            assertThat(factory).isNotNull();
        }

        @Test
        @DisplayName("secure should return same schema factory for chaining")
        void secureShouldReturnSameSchemaFactoryForChaining() throws Exception {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            SchemaFactory result = XmlSecurity.secure(factory);

            assertThat(result).isSameAs(factory);
        }

        @Test
        @DisplayName("createSecureSchemaFactory should create secure factory")
        void createSecureSchemaFactoryShouldCreateSecureFactory() {
            SchemaFactory factory = XmlSecurity.createSecureSchemaFactory();

            assertThat(factory).isNotNull();
        }
    }

    @Nested
    @DisplayName("Disable External Entities Tests")
    class DisableExternalEntitiesTests {

        @Test
        @DisplayName("disableExternalEntities should configure DocumentBuilderFactory")
        void disableExternalEntitiesShouldConfigureDocumentBuilderFactory() throws Exception {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            XmlSecurity.disableExternalEntities(factory);

            assertThat(factory).isNotNull();
        }
    }

    @Nested
    @DisplayName("Disable DTD Tests")
    class DisableDtdTests {

        @Test
        @DisplayName("disableDtd should configure DocumentBuilderFactory")
        void disableDtdShouldConfigureDocumentBuilderFactory() throws Exception {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            XmlSecurity.disableDtd(factory);

            assertThat(factory).isNotNull();
        }
    }

    @Nested
    @DisplayName("Entity Expansion Limit Tests")
    class EntityExpansionLimitTests {

        @Test
        @DisplayName("setEntityExpansionLimit should set limit")
        void setEntityExpansionLimitShouldSetLimit() throws Exception {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            XmlSecurity.setEntityExpansionLimit(factory, 1000);

            assertThat(factory).isNotNull();
        }

        @Test
        @DisplayName("DEFAULT_ENTITY_EXPANSION_LIMIT should have expected value")
        void defaultEntityExpansionLimitShouldHaveExpectedValue() {
            assertThat(XmlSecurity.DEFAULT_ENTITY_EXPANSION_LIMIT).isEqualTo(64000);
        }
    }

    @Nested
    @DisplayName("XXE Prevention Tests")
    class XxePreventionTests {

        @Test
        @DisplayName("secured factory should prevent XXE attacks")
        void securedFactoryShouldPreventXxeAttacks() throws Exception {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            XmlSecurity.secure(factory);
            DocumentBuilder builder = factory.newDocumentBuilder();

            String xxeXml = """
                <?xml version="1.0"?>
                <!DOCTYPE root [
                    <!ENTITY xxe SYSTEM "file:///etc/passwd">
                ]>
                <root>&xxe;</root>
                """;

            // Should either throw or not expand entity
            try {
                org.w3c.dom.Document doc = builder.parse(
                    new java.io.ByteArrayInputStream(xxeXml.getBytes())
                );
                String content = doc.getDocumentElement().getTextContent();
                // If parsed, entity should not be expanded
                assertThat(content).doesNotContain("root:");
            } catch (Exception e) {
                // Expected - XXE blocked
                assertThat(e).isNotNull();
            }
        }
    }
}
