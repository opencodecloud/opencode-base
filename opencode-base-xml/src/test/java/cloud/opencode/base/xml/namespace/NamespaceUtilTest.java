package cloud.opencode.base.xml.namespace;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.XmlElement;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * NamespaceUtilTest Tests
 * NamespaceUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("NamespaceUtil Tests")
class NamespaceUtilTest {

    @Nested
    @DisplayName("Constant Tests")
    class ConstantTests {

        @Test
        @DisplayName("XMLNS_URI should be correct")
        void xmlnsUriShouldBeCorrect() {
            assertThat(NamespaceUtil.XMLNS_URI)
                .isEqualTo("http://www.w3.org/2000/xmlns/");
        }

        @Test
        @DisplayName("XSI_URI should be correct")
        void xsiUriShouldBeCorrect() {
            assertThat(NamespaceUtil.XSI_URI)
                .isEqualTo("http://www.w3.org/2001/XMLSchema-instance");
        }

        @Test
        @DisplayName("XSD_URI should be correct")
        void xsdUriShouldBeCorrect() {
            assertThat(NamespaceUtil.XSD_URI)
                .isEqualTo("http://www.w3.org/2001/XMLSchema");
        }

        @Test
        @DisplayName("SOAP_ENV_URI should be correct")
        void soapEnvUriShouldBeCorrect() {
            assertThat(NamespaceUtil.SOAP_ENV_URI)
                .isEqualTo("http://schemas.xmlsoap.org/soap/envelope/");
        }

        @Test
        @DisplayName("SOAP12_ENV_URI should be correct")
        void soap12EnvUriShouldBeCorrect() {
            assertThat(NamespaceUtil.SOAP12_ENV_URI)
                .isEqualTo("http://www.w3.org/2003/05/soap-envelope");
        }
    }

    @Nested
    @DisplayName("Extract Namespaces Tests")
    class ExtractNamespacesTests {

        @Test
        @DisplayName("extractNamespaces should extract default namespace")
        void extractNamespacesShouldExtractDefaultNamespace() {
            String xml = "<root xmlns=\"http://example.com\"/>";
            XmlDocument doc = XmlDocument.parse(xml);

            Map<String, String> namespaces = NamespaceUtil.extractNamespaces(doc);

            assertThat(namespaces).containsValue("http://example.com");
        }

        @Test
        @DisplayName("extractNamespaces should extract prefixed namespaces")
        void extractNamespacesShouldExtractPrefixedNamespaces() {
            String xml = "<root xmlns:ns=\"http://example.com\"/>";
            XmlDocument doc = XmlDocument.parse(xml);

            Map<String, String> namespaces = NamespaceUtil.extractNamespaces(doc);

            assertThat(namespaces).containsEntry("ns", "http://example.com");
        }

        @Test
        @DisplayName("extractNamespaces from element should extract namespaces")
        void extractNamespacesFromElementShouldExtractNamespaces() {
            String xml = "<root xmlns:ns=\"http://example.com\"><ns:child/></root>";
            XmlDocument doc = XmlDocument.parse(xml);

            Map<String, String> namespaces = NamespaceUtil.extractNamespaces(doc.getRoot());

            assertThat(namespaces).containsEntry("ns", "http://example.com");
        }
    }

    @Nested
    @DisplayName("Create Context Tests")
    class CreateContextTests {

        @Test
        @DisplayName("createContext from document should create context")
        void createContextFromDocumentShouldCreateContext() {
            String xml = "<root xmlns:ns=\"http://example.com\"/>";
            XmlDocument doc = XmlDocument.parse(xml);

            OpenNamespaceContext context = NamespaceUtil.createContext(doc);

            assertThat(context).isNotNull();
            assertThat(context.getNamespaceURI("ns")).isEqualTo("http://example.com");
        }

        @Test
        @DisplayName("createContext from element should create context")
        void createContextFromElementShouldCreateContext() {
            String xml = "<root xmlns:ns=\"http://example.com\"/>";
            XmlDocument doc = XmlDocument.parse(xml);

            OpenNamespaceContext context = NamespaceUtil.createContext(doc.getRoot());

            assertThat(context).isNotNull();
            assertThat(context.getNamespaceURI("ns")).isEqualTo("http://example.com");
        }

        @Test
        @DisplayName("createContextFromMap should create context from map")
        void createContextFromMapShouldCreateContextFromMap() {
            Map<String, String> bindings = Map.of("ns", "http://example.com");

            OpenNamespaceContext context = NamespaceUtil.createContextFromMap(bindings);

            assertThat(context.getNamespaceURI("ns")).isEqualTo("http://example.com");
        }
    }

    @Nested
    @DisplayName("QName Tests")
    class QNameTests {

        @Test
        @DisplayName("getLocalPart should return local part")
        void getLocalPartShouldReturnLocalPart() {
            assertThat(NamespaceUtil.getLocalPart("ns:element")).isEqualTo("element");
            assertThat(NamespaceUtil.getLocalPart("element")).isEqualTo("element");
        }

        @Test
        @DisplayName("getLocalPart should handle null")
        void getLocalPartShouldHandleNull() {
            assertThat(NamespaceUtil.getLocalPart(null)).isNull();
        }

        @Test
        @DisplayName("getPrefix should return prefix")
        void getPrefixShouldReturnPrefix() {
            assertThat(NamespaceUtil.getPrefix("ns:element")).isEqualTo("ns");
            assertThat(NamespaceUtil.getPrefix("element")).isEmpty();
        }

        @Test
        @DisplayName("createQName should create qualified name")
        void createQNameShouldCreateQualifiedName() {
            String qname = NamespaceUtil.createQName("ns", "element");

            assertThat(qname).isEqualTo("ns:element");
        }

        @Test
        @DisplayName("createQName without prefix should return local name")
        void createQNameWithoutPrefixShouldReturnLocalName() {
            String qname = NamespaceUtil.createQName(null, "element");

            assertThat(qname).isEqualTo("element");
        }

        @Test
        @DisplayName("createQName with empty prefix should return local name")
        void createQNameWithEmptyPrefixShouldReturnLocalName() {
            String qname = NamespaceUtil.createQName("", "element");

            assertThat(qname).isEqualTo("element");
        }
    }

    @Nested
    @DisplayName("Qualified Name Tests")
    class QualifiedNameTests {

        @Test
        @DisplayName("isQualifiedName should return true for qualified name")
        void isQualifiedNameShouldReturnTrueForQualifiedName() {
            assertThat(NamespaceUtil.isQualifiedName("ns:element")).isTrue();
        }

        @Test
        @DisplayName("isQualifiedName should return false for unqualified name")
        void isQualifiedNameShouldReturnFalseForUnqualifiedName() {
            assertThat(NamespaceUtil.isQualifiedName("element")).isFalse();
        }

        @Test
        @DisplayName("isQualifiedName should return false for null")
        void isQualifiedNameShouldReturnFalseForNull() {
            assertThat(NamespaceUtil.isQualifiedName(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Namespace Check Tests")
    class NamespaceCheckTests {

        @Test
        @DisplayName("isInNamespace with null namespace should match elements without namespace")
        void isInNamespaceWithNullNamespaceShouldMatchElementsWithoutNamespace() {
            // Note: XmlDocument.parse uses non-namespace-aware parser
            // so element.getNamespaceURI() returns null even with xmlns declarations
            XmlDocument doc = XmlDocument.parse("<root xmlns=\"http://example.com\"/>");

            // Since parser is not namespace-aware, element has no namespace
            boolean result = NamespaceUtil.isInNamespace(doc.getRoot(), null);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("isInNamespace should return false when namespace URI doesn't match")
        void isInNamespaceShouldReturnFalseWhenNamespaceUriDoesntMatch() {
            XmlDocument doc = XmlDocument.parse("<root xmlns=\"http://other.com\"/>");

            boolean result = NamespaceUtil.isInNamespace(doc.getRoot(), "http://example.com");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("hasNoNamespace should return true for element without namespace")
        void hasNoNamespaceShouldReturnTrueForElementWithoutNamespace() {
            XmlDocument doc = XmlDocument.parse("<root/>");

            boolean result = NamespaceUtil.hasNoNamespace(doc.getRoot());

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("getNamespaceUri should return null for non-namespace-aware parsed element")
        void getNamespaceUriShouldReturnNullForNonNamespaceAwareParsedElement() {
            // Note: XmlDocument.parse uses non-namespace-aware parser by default
            XmlDocument doc = XmlDocument.parse("<root xmlns=\"http://example.com\"/>");

            String uri = NamespaceUtil.getNamespaceUri(doc.getRoot());

            // Without namespace-aware parsing, returns null
            assertThat(uri).isNull();
        }

        @Test
        @DisplayName("getNamespacePrefix should return null for non-namespace-aware parsed element")
        void getNamespacePrefixShouldReturnNullForNonNamespaceAwareParsedElement() {
            // Note: XmlDocument.parse uses non-namespace-aware parser by default
            XmlDocument doc = XmlDocument.parse("<ns:root xmlns:ns=\"http://example.com\"/>");

            String prefix = NamespaceUtil.getNamespacePrefix(doc.getRoot());

            // Without namespace-aware parsing, returns null
            assertThat(prefix).isNull();
        }
    }

    @Nested
    @DisplayName("Declare Namespace Tests")
    class DeclareNamespaceTests {

        @Test
        @DisplayName("declareNamespace should add namespace declaration")
        void declareNamespaceShouldAddNamespaceDeclaration() {
            XmlDocument doc = XmlDocument.create("root");
            XmlElement root = doc.getRoot();

            NamespaceUtil.declareNamespace(root, "ns", "http://example.com");

            String xml = doc.toXml();
            assertThat(xml).contains("xmlns:ns=\"http://example.com\"");
        }

        @Test
        @DisplayName("declareDefaultNamespace should add default namespace")
        void declareDefaultNamespaceShouldAddDefaultNamespace() {
            XmlDocument doc = XmlDocument.create("root");
            XmlElement root = doc.getRoot();

            NamespaceUtil.declareDefaultNamespace(root, "http://example.com");

            String xml = doc.toXml();
            assertThat(xml).contains("xmlns=\"http://example.com\"");
        }
    }

    @Nested
    @DisplayName("Predefined Context Tests")
    class PredefinedContextTests {

        @Test
        @DisplayName("soapContext should have SOAP namespace")
        void soapContextShouldHaveSoapNamespace() {
            OpenNamespaceContext context = NamespaceUtil.soapContext();

            assertThat(context.getNamespaceURI("soap"))
                .isEqualTo(NamespaceUtil.SOAP_ENV_URI);
        }

        @Test
        @DisplayName("soap12Context should have SOAP 1.2 namespace")
        void soap12ContextShouldHaveSoap12Namespace() {
            OpenNamespaceContext context = NamespaceUtil.soap12Context();

            assertThat(context.getNamespaceURI("soap"))
                .isEqualTo(NamespaceUtil.SOAP12_ENV_URI);
        }

        @Test
        @DisplayName("xsdContext should have XSD namespace")
        void xsdContextShouldHaveXsdNamespace() {
            OpenNamespaceContext context = NamespaceUtil.xsdContext();

            assertThat(context.getNamespaceURI("xsd"))
                .isEqualTo(NamespaceUtil.XSD_URI);
        }
    }
}
