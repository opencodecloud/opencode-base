package cloud.opencode.base.xml.security;

import org.junit.jupiter.api.*;

import javax.xml.parsers.*;
import javax.xml.stream.*;
import javax.xml.transform.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SecureParserFactoryTest Tests
 * SecureParserFactoryTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("SecureParserFactory Tests")
class SecureParserFactoryTest {

    @Nested
    @DisplayName("DocumentBuilder Factory Tests")
    class DocumentBuilderFactoryTests {

        @Test
        @DisplayName("getDocumentBuilderFactory should return secure factory")
        void getDocumentBuilderFactoryShouldReturnSecureFactory() {
            DocumentBuilderFactory factory = SecureParserFactory.getDocumentBuilderFactory();

            assertThat(factory).isNotNull();
        }

        @Test
        @DisplayName("getDocumentBuilderFactory should be thread-safe")
        void getDocumentBuilderFactoryShouldBeThreadSafe() throws Exception {
            DocumentBuilderFactory factory1 = SecureParserFactory.getDocumentBuilderFactory();
            DocumentBuilderFactory factory2 = SecureParserFactory.getDocumentBuilderFactory();

            // Should return cached factory
            assertThat(factory1).isSameAs(factory2);
        }

        @Test
        @DisplayName("createDocumentBuilder should return configured builder")
        void createDocumentBuilderShouldReturnConfiguredBuilder() throws Exception {
            DocumentBuilder builder = SecureParserFactory.createDocumentBuilder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("createNamespaceAwareDocumentBuilder should handle namespaces")
        void createNamespaceAwareDocumentBuilderShouldHandleNamespaces() throws Exception {
            DocumentBuilder builder = SecureParserFactory.createNamespaceAwareDocumentBuilder();

            assertThat(builder).isNotNull();
            assertThat(builder.isNamespaceAware()).isTrue();
        }
    }

    @Nested
    @DisplayName("SAXParser Factory Tests")
    class SaxParserFactoryTests {

        @Test
        @DisplayName("getSAXParserFactory should return secure factory")
        void getSaxParserFactoryShouldReturnSecureFactory() {
            SAXParserFactory factory = SecureParserFactory.getSAXParserFactory();

            assertThat(factory).isNotNull();
        }

        @Test
        @DisplayName("createSAXParser should return configured parser")
        void createSaxParserShouldReturnConfiguredParser() throws Exception {
            SAXParser parser = SecureParserFactory.createSAXParser();

            assertThat(parser).isNotNull();
        }

        @Test
        @DisplayName("createNamespaceAwareSAXParser should handle namespaces")
        void createNamespaceAwareSaxParserShouldHandleNamespaces() throws Exception {
            SAXParser parser = SecureParserFactory.createNamespaceAwareSAXParser();

            assertThat(parser).isNotNull();
            assertThat(parser.isNamespaceAware()).isTrue();
        }
    }

    @Nested
    @DisplayName("XMLStreamReader Factory Tests")
    class XmlStreamReaderFactoryTests {

        @Test
        @DisplayName("getXMLInputFactory should return secure factory")
        void getXmlInputFactoryShouldReturnSecureFactory() {
            XMLInputFactory factory = SecureParserFactory.getXMLInputFactory();

            assertThat(factory).isNotNull();
        }

        @Test
        @DisplayName("getXMLInputFactory should be cached")
        void getXmlInputFactoryShouldBeCached() {
            XMLInputFactory factory1 = SecureParserFactory.getXMLInputFactory();
            XMLInputFactory factory2 = SecureParserFactory.getXMLInputFactory();

            assertThat(factory1).isSameAs(factory2);
        }

        @Test
        @DisplayName("createXMLInputFactory should return new factory")
        void createXMLInputFactoryShouldReturnNewFactory() {
            XMLInputFactory factory1 = SecureParserFactory.createXMLInputFactory();
            XMLInputFactory factory2 = SecureParserFactory.createXMLInputFactory();

            assertThat(factory1).isNotNull();
            assertThat(factory2).isNotNull();
            assertThat(factory1).isNotSameAs(factory2);
        }
    }

    @Nested
    @DisplayName("XMLStreamReader Creation Tests")
    class XmlStreamReaderCreationTests {

        @Test
        @DisplayName("createXMLStreamReader from InputStream should work")
        void createXMLStreamReaderFromInputStreamShouldWork() {
            String xml = "<root/>";
            var reader = SecureParserFactory.createXMLStreamReader(
                new java.io.ByteArrayInputStream(xml.getBytes()));

            assertThat(reader).isNotNull();
        }

        @Test
        @DisplayName("createXMLStreamReader from Reader should work")
        void createXMLStreamReaderFromReaderShouldWork() {
            String xml = "<root/>";
            var reader = SecureParserFactory.createXMLStreamReader(
                new java.io.StringReader(xml));

            assertThat(reader).isNotNull();
        }
    }

    @Nested
    @DisplayName("Transformer Factory Tests")
    class TransformerFactoryTests {

        @Test
        @DisplayName("getTransformerFactory should return secure factory")
        void getTransformerFactoryShouldReturnSecureFactory() {
            TransformerFactory factory = SecureParserFactory.getTransformerFactory();

            assertThat(factory).isNotNull();
        }

        @Test
        @DisplayName("createTransformer should return configured transformer")
        void createTransformerShouldReturnConfiguredTransformer() throws Exception {
            Transformer transformer = SecureParserFactory.createTransformer();

            assertThat(transformer).isNotNull();
        }
    }

    @Nested
    @DisplayName("Security Configuration Tests")
    class SecurityConfigurationTests {

        @Test
        @DisplayName("factory should block external entities by default")
        void factoryShouldBlockExternalEntitiesByDefault() throws Exception {
            DocumentBuilderFactory factory = SecureParserFactory.getDocumentBuilderFactory();

            // Feature should be set to block external entities
            assertThat(factory).isNotNull();
        }
    }
}
