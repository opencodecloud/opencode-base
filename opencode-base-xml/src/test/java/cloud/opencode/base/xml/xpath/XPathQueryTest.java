package cloud.opencode.base.xml.xpath;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.XmlElement;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * XPathQueryTest Tests
 * XPathQueryTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XPathQuery Tests")
class XPathQueryTest {

    private static final String TEST_XML = """
        <catalog>
            <book id="1">
                <title>Java Programming</title>
                <price>29.99</price>
            </book>
            <book id="2">
                <title>XML Guide</title>
                <price>24.99</price>
            </book>
        </catalog>
        """;

    private XmlDocument document;

    @BeforeEach
    void setUp() {
        document = XmlDocument.parse(TEST_XML);
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("create should create empty query")
        void createShouldCreateEmptyQuery() {
            XPathQuery query = XPathQuery.create();

            assertThat(query).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("xml should set XML source from string")
        void xmlShouldSetXmlSourceFromString() {
            String result = XPathQuery.create()
                .xml(TEST_XML)
                .selectString("/catalog/book[1]/title");

            assertThat(result).isEqualTo("Java Programming");
        }

        @Test
        @DisplayName("document should set XML source from Document")
        void documentShouldSetXmlSourceFromDocument() {
            String result = XPathQuery.create()
                .document(document.getDocument())
                .selectString("/catalog/book[1]/title");

            assertThat(result).isEqualTo("Java Programming");
        }
    }

    @Nested
    @DisplayName("Namespace Tests")
    class NamespaceTests {

        @Test
        @DisplayName("namespace should add single namespace")
        void namespaceShouldAddSingleNamespace() {
            String nsXml = """
                <root xmlns:ex="http://example.com">
                    <ex:item>value</ex:item>
                </root>
                """;

            String result = XPathQuery.create()
                .xml(nsXml)
                .namespace("e", "http://example.com")
                .selectString("/root/e:item");

            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("namespaces should add multiple namespaces")
        void namespacesShouldAddMultipleNamespaces() {
            String nsXml = """
                <root xmlns:a="http://a.com" xmlns:b="http://b.com">
                    <a:item>A value</a:item>
                    <b:item>B value</b:item>
                </root>
                """;
            Map<String, String> ns = Map.of(
                "x", "http://a.com",
                "y", "http://b.com"
            );

            String result = XPathQuery.create()
                .xml(nsXml)
                .namespaces(ns)
                .selectString("/root/x:item");

            assertThat(result).isEqualTo("A value");
        }
    }

    @Nested
    @DisplayName("Variable Tests")
    class VariableTests {

        @Test
        @DisplayName("variable should support XPath variables")
        void variableShouldSupportXPathVariables() {
            String result = XPathQuery.create()
                .document(document.getDocument())
                .variable("bookId", "2")
                .selectString("/catalog/book[@id=$bookId]/title");

            assertThat(result).isEqualTo("XML Guide");
        }
    }

    @Nested
    @DisplayName("SelectString Tests")
    class SelectStringTests {

        @Test
        @DisplayName("selectString should return text content")
        void selectStringShouldReturnTextContent() {
            String result = XPathQuery.create()
                .document(document.getDocument())
                .selectString("/catalog/book[1]/title");

            assertThat(result).isEqualTo("Java Programming");
        }

        @Test
        @DisplayName("selectString should return empty for missing element")
        void selectStringShouldReturnEmptyForMissingElement() {
            String result = XPathQuery.create()
                .document(document.getDocument())
                .selectString("/catalog/nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("SelectNumber Tests")
    class SelectNumberTests {

        @Test
        @DisplayName("selectNumber should return numeric value")
        void selectNumberShouldReturnNumericValue() {
            Number result = XPathQuery.create()
                .document(document.getDocument())
                .selectNumber("/catalog/book[1]/price");

            assertThat(result.doubleValue()).isEqualTo(29.99);
        }

        @Test
        @DisplayName("selectNumber should return count")
        void selectNumberShouldReturnCount() {
            Number result = XPathQuery.create()
                .document(document.getDocument())
                .selectNumber("count(/catalog/book)");

            assertThat(result.intValue()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("SelectBoolean Tests")
    class SelectBooleanTests {

        @Test
        @DisplayName("selectBoolean should return boolean result")
        void selectBooleanShouldReturnBooleanResult() {
            Boolean result = XPathQuery.create()
                .document(document.getDocument())
                .selectBoolean("count(/catalog/book) > 1");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("selectBoolean should return false for condition not met")
        void selectBooleanShouldReturnFalseForConditionNotMet() {
            Boolean result = XPathQuery.create()
                .document(document.getDocument())
                .selectBoolean("count(/catalog/book) > 10");

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("SelectElement Tests")
    class SelectElementTests {

        @Test
        @DisplayName("selectElement should return XmlElement")
        void selectElementShouldReturnXmlElement() {
            XmlElement result = XPathQuery.create()
                .document(document.getDocument())
                .selectElement("/catalog/book[1]");

            assertThat(result).isNotNull();
            assertThat(result.getAttribute("id")).isEqualTo("1");
        }

        @Test
        @DisplayName("selectElement should return null for missing")
        void selectElementShouldReturnNullForMissing() {
            XmlElement result = XPathQuery.create()
                .document(document.getDocument())
                .selectElement("/catalog/nonexistent");

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("SelectElements Tests")
    class SelectElementsTests {

        @Test
        @DisplayName("selectElements should return list of elements")
        void selectElementsShouldReturnListOfElements() {
            List<XmlElement> result = XPathQuery.create()
                .document(document.getDocument())
                .selectElements("/catalog/book");

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("selectElements should return empty list for missing")
        void selectElementsShouldReturnEmptyListForMissing() {
            List<XmlElement> result = XPathQuery.create()
                .document(document.getDocument())
                .selectElements("/catalog/nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("SelectValues Tests")
    class SelectValuesTests {

        @Test
        @DisplayName("selectValues should return list of string values")
        void selectValuesShouldReturnListOfStringValues() {
            List<String> result = XPathQuery.create()
                .document(document.getDocument())
                .selectValues("/catalog/book/title");

            assertThat(result).containsExactly("Java Programming", "XML Guide");
        }
    }
}
