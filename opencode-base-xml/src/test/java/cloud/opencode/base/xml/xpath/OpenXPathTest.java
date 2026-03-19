package cloud.opencode.base.xml.xpath;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.XmlElement;
import org.junit.jupiter.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenXPathTest Tests
 * OpenXPathTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("OpenXPath Tests")
class OpenXPathTest {

    private static final String TEST_XML = """
        <catalog>
            <book id="1" category="fiction">
                <title>Java Programming</title>
                <author>John Doe</author>
                <price>29.99</price>
            </book>
            <book id="2" category="non-fiction">
                <title>XML Guide</title>
                <author>Jane Smith</author>
                <price>24.99</price>
            </book>
            <book id="3" category="fiction">
                <title>Python Basics</title>
                <author>Bob Wilson</author>
                <price>19.99</price>
            </book>
        </catalog>
        """;

    private XmlDocument xmlDocument;
    private Document document;

    @BeforeEach
    void setUp() {
        xmlDocument = XmlDocument.parse(TEST_XML);
        document = xmlDocument.getDocument();
    }

    @Nested
    @DisplayName("SelectString Tests")
    class SelectStringTests {

        @Test
        @DisplayName("selectString should return text content")
        void selectStringShouldReturnTextContent() {
            String title = OpenXPath.selectString(document, "/catalog/book[1]/title");

            assertThat(title).isEqualTo("Java Programming");
        }

        @Test
        @DisplayName("selectString should return attribute value")
        void selectStringShouldReturnAttributeValue() {
            String id = OpenXPath.selectString(document, "/catalog/book[1]/@id");

            assertThat(id).isEqualTo("1");
        }

        @Test
        @DisplayName("selectString should return empty for non-existing path")
        void selectStringShouldReturnEmptyForNonExistingPath() {
            String result = OpenXPath.selectString(document, "/catalog/nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("SelectNumber Tests")
    class SelectNumberTests {

        @Test
        @DisplayName("selectNumber should return numeric value")
        void selectNumberShouldReturnNumericValue() {
            Number price = OpenXPath.selectNumber(document, "/catalog/book[1]/price");

            assertThat(price.doubleValue()).isEqualTo(29.99);
        }

        @Test
        @DisplayName("selectNumber should return count")
        void selectNumberShouldReturnCount() {
            Number count = OpenXPath.selectNumber(document, "count(/catalog/book)");

            assertThat(count.intValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("selectNumber should return sum")
        void selectNumberShouldReturnSum() {
            Number sum = OpenXPath.selectNumber(document, "sum(/catalog/book/price)");

            assertThat(sum.doubleValue()).isCloseTo(74.97, within(0.01));
        }
    }

    @Nested
    @DisplayName("SelectBoolean Tests")
    class SelectBooleanTests {

        @Test
        @DisplayName("selectBoolean should return true for existing element")
        void selectBooleanShouldReturnTrueForExistingElement() {
            boolean exists = OpenXPath.selectBoolean(document, "boolean(/catalog/book)");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("selectBoolean should return false for non-existing element")
        void selectBooleanShouldReturnFalseForNonExistingElement() {
            boolean exists = OpenXPath.selectBoolean(document, "boolean(/catalog/nonexistent)");

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("selectBoolean should evaluate comparison")
        void selectBooleanShouldEvaluateComparison() {
            boolean result = OpenXPath.selectBoolean(document, "count(/catalog/book) > 2");

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("SelectNode Tests")
    class SelectNodeTests {

        @Test
        @DisplayName("selectNode should return single node")
        void selectNodeShouldReturnSingleNode() {
            Node node = OpenXPath.selectNode(document, "/catalog/book[1]");

            assertThat(node).isNotNull();
            assertThat(node.getNodeName()).isEqualTo("book");
        }

        @Test
        @DisplayName("selectNode should return null for non-existing path")
        void selectNodeShouldReturnNullForNonExistingPath() {
            Node node = OpenXPath.selectNode(document, "/catalog/nonexistent");

            assertThat(node).isNull();
        }
    }

    @Nested
    @DisplayName("SelectNodes Tests")
    class SelectNodesTests {

        @Test
        @DisplayName("selectNodes should return all matching nodes")
        void selectNodesShouldReturnAllMatchingNodes() {
            NodeList nodes = OpenXPath.selectNodes(document, "/catalog/book");

            assertThat(nodes.getLength()).isEqualTo(3);
        }

        @Test
        @DisplayName("selectNodes should return empty list for non-existing path")
        void selectNodesShouldReturnEmptyListForNonExistingPath() {
            NodeList nodes = OpenXPath.selectNodes(document, "/catalog/nonexistent");

            assertThat(nodes.getLength()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("SelectElement Tests")
    class SelectElementTests {

        @Test
        @DisplayName("selectElement should return XmlElement")
        void selectElementShouldReturnXmlElement() {
            XmlElement element = OpenXPath.selectElement(document, "/catalog/book[1]");

            assertThat(element).isNotNull();
            assertThat(element.getName()).isEqualTo("book");
            assertThat(element.getAttribute("id")).isEqualTo("1");
        }

        @Test
        @DisplayName("selectElement should return null for non-existing path")
        void selectElementShouldReturnNullForNonExistingPath() {
            XmlElement element = OpenXPath.selectElement(document, "/catalog/nonexistent");

            assertThat(element).isNull();
        }
    }

    @Nested
    @DisplayName("SelectElements Tests")
    class SelectElementsTests {

        @Test
        @DisplayName("selectElements should return list of XmlElements")
        void selectElementsShouldReturnListOfXmlElements() {
            List<XmlElement> elements = OpenXPath.selectElements(document, "/catalog/book");

            assertThat(elements).hasSize(3);
        }

        @Test
        @DisplayName("selectElements with predicate should filter")
        void selectElementsWithPredicateShouldFilter() {
            List<XmlElement> elements = OpenXPath.selectElements(document,
                "/catalog/book[@category='fiction']");

            assertThat(elements).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Exists Tests")
    class ExistsTests {

        @Test
        @DisplayName("exists should return true for existing path")
        void existsShouldReturnTrueForExistingPath() {
            assertThat(OpenXPath.exists(TEST_XML, "/catalog/book")).isTrue();
        }

        @Test
        @DisplayName("exists should return false for non-existing path")
        void existsShouldReturnFalseForNonExistingPath() {
            assertThat(OpenXPath.exists(TEST_XML, "/catalog/nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("Count Tests")
    class CountTests {

        @Test
        @DisplayName("count should return number of matching nodes")
        void countShouldReturnNumberOfMatchingNodes() {
            int count = OpenXPath.count(TEST_XML, "/catalog/book");

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("count should return zero for non-existing path")
        void countShouldReturnZeroForNonExistingPath() {
            int count = OpenXPath.count(TEST_XML, "/catalog/nonexistent");

            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Namespace Tests")
    class NamespaceTests {

        @Test
        @DisplayName("withNamespaces should support namespaced queries")
        void withNamespacesShouldSupportNamespacedQueries() {
            String nsXml = """
                <root xmlns:ex="http://example.com">
                    <ex:item>value</ex:item>
                </root>
                """;
            Map<String, String> namespaces = Map.of("e", "http://example.com");

            String result = OpenXPath.withNamespaces(namespaces)
                .xml(nsXml)
                .selectString("/root/e:item");

            assertThat(result).isEqualTo("value");
        }
    }
}
