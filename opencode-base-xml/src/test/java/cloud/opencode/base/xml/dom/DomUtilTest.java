package cloud.opencode.base.xml.dom;

import org.junit.jupiter.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DomUtilTest Tests
 * DomUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("DomUtil Tests")
class DomUtilTest {

    private Document document;
    private Element root;

    @BeforeEach
    void setUp() {
        document = DomParser.parse("<root id=\"1\"><child>text</child><item>value</item></root>");
        root = document.getDocumentElement();
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should serialize document to XML")
        void toStringShouldSerializeDocumentToXml() {
            String xml = DomUtil.toString(document);

            assertThat(xml).contains("<root");
            assertThat(xml).contains("<child>text</child>");
        }

        @Test
        @DisplayName("toString should serialize element to XML")
        void toStringShouldSerializeElementToXml() {
            Element child = (Element) root.getElementsByTagName("child").item(0);

            String xml = DomUtil.toString(child);

            assertThat(xml).contains("<child>text</child>");
        }

        @Test
        @DisplayName("toString with indent should format output")
        void toStringWithIndentShouldFormatOutput() {
            String xml = DomUtil.toString(document, 4);

            assertThat(xml).contains("\n");
        }

        @Test
        @DisplayName("toString with zero indent should not add extra formatting")
        void toStringWithZeroIndentShouldNotAddExtraFormatting() {
            String xml = DomUtil.toString(document, 0);

            assertThat(xml).contains("<root");
            assertThat(xml).contains("<?xml");
        }
    }

    @Nested
    @DisplayName("Element To Map Tests")
    class ElementToMapTests {

        @Test
        @DisplayName("elementToMap should convert element to map")
        void elementToMapShouldConvertElementToMap() {
            Map<String, Object> map = DomUtil.elementToMap(root);

            // Child elements are converted to nested maps with #text key
            assertThat(map).containsKey("child");
            assertThat(map).containsKey("item");
        }

        @Test
        @DisplayName("elementToMap should handle attributes with @ prefix")
        void elementToMapShouldHandleAttributes() {
            Map<String, Object> map = DomUtil.elementToMap(root);

            assertThat(map).containsEntry("@id", "1");
        }

        @Test
        @DisplayName("elementToMap should return empty map for empty element")
        void elementToMapShouldReturnEmptyMapForEmptyElement() {
            Document emptyDoc = DomParser.parse("<root/>");
            Element emptyRoot = emptyDoc.getDocumentElement();

            Map<String, Object> map = DomUtil.elementToMap(emptyRoot);

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("elementToMap should handle nested elements")
        void elementToMapShouldHandleNestedElements() {
            Document nestedDoc = DomParser.parse("<root><parent><child>nested</child></parent></root>");
            Element nestedRoot = nestedDoc.getDocumentElement();

            Map<String, Object> map = DomUtil.elementToMap(nestedRoot);

            assertThat(map).containsKey("parent");
            @SuppressWarnings("unchecked")
            Map<String, Object> parentMap = (Map<String, Object>) map.get("parent");
            assertThat(parentMap).containsKey("child");
        }
    }

    @Nested
    @DisplayName("Convert Text Tests")
    class ConvertTextTests {

        @Test
        @DisplayName("convertText should convert to String")
        void convertTextShouldConvertToString() {
            String result = DomUtil.convertText("hello", String.class);

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("convertText should convert to Integer")
        void convertTextShouldConvertToInteger() {
            Integer result = DomUtil.convertText("42", Integer.class);

            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("convertText should convert to int primitive")
        void convertTextShouldConvertToIntPrimitive() {
            int result = DomUtil.convertText("42", int.class);

            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("convertText should convert to Long")
        void convertTextShouldConvertToLong() {
            Long result = DomUtil.convertText("9999999999", Long.class);

            assertThat(result).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("convertText should convert to Double")
        void convertTextShouldConvertToDouble() {
            Double result = DomUtil.convertText("3.14", Double.class);

            assertThat(result).isEqualTo(3.14);
        }

        @Test
        @DisplayName("convertText should convert to Boolean")
        void convertTextShouldConvertToBoolean() {
            Boolean result = DomUtil.convertText("true", Boolean.class);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("convertText should return null for null input")
        void convertTextShouldReturnNullForNullInput() {
            String result = DomUtil.convertText(null, String.class);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Clone Tests")
    class CloneTests {

        @Test
        @DisplayName("cloneInto should clone element into another document")
        void cloneIntoShouldCloneElementIntoAnotherDocument() {
            Document targetDoc = DomParser.createDocument("target");
            Element targetRoot = targetDoc.getDocumentElement();

            Node cloned = DomUtil.cloneInto(root, targetDoc);
            targetRoot.appendChild(cloned);

            assertThat(((Element) cloned).getTagName()).isEqualTo("root");
            assertThat(targetRoot.getElementsByTagName("root").getLength()).isEqualTo(1);
        }

        @Test
        @DisplayName("cloneInto should create deep copy")
        void cloneIntoShouldCreateDeepCopy() {
            Document targetDoc = DomUtil.createDocument();

            Node cloned = DomUtil.cloneInto(root, targetDoc);
            targetDoc.appendChild(cloned);

            assertThat(cloned).isNotSameAs(root);
            assertThat(DomUtil.toString(targetDoc)).contains("<child>text</child>");
        }
    }

    @Nested
    @DisplayName("Child Element Tests")
    class ChildElementTests {

        @Test
        @DisplayName("getChildElements should return child elements")
        void getChildElementsShouldReturnChildElements() {
            List<Element> children = DomUtil.getChildElements(root);

            assertThat(children).hasSize(2);
        }

        @Test
        @DisplayName("getChildElements with name should filter by name")
        void getChildElementsWithNameShouldFilterByName() {
            List<Element> children = DomUtil.getChildElements(root, "child");

            assertThat(children).hasSize(1);
            assertThat(children.get(0).getTagName()).isEqualTo("child");
        }

        @Test
        @DisplayName("getFirstChildElement should return first child with name")
        void getFirstChildElementShouldReturnFirstChildWithName() {
            Element first = DomUtil.getFirstChildElement(root, "child");

            assertThat(first).isNotNull();
            assertThat(first.getTagName()).isEqualTo("child");
        }

        @Test
        @DisplayName("getFirstChildElement with name should return named child")
        void getFirstChildElementWithNameShouldReturnNamedChild() {
            Element child = DomUtil.getFirstChildElement(root, "item");

            assertThat(child).isNotNull();
            assertThat(child.getTagName()).isEqualTo("item");
        }

        @Test
        @DisplayName("getFirstChildElement should return null for missing")
        void getFirstChildElementShouldReturnNullForMissing() {
            Element child = DomUtil.getFirstChildElement(root, "nonexistent");

            assertThat(child).isNull();
        }
    }

    @Nested
    @DisplayName("Add Child Element Tests")
    class AddChildElementTests {

        @Test
        @DisplayName("addChildElement should add element with text")
        void addChildElementShouldAddElementWithText() {
            Element newChild = DomUtil.addChildElement(root, "newChild", "content");

            assertThat(newChild).isNotNull();
            assertThat(newChild.getTextContent()).isEqualTo("content");
            assertThat(root.getElementsByTagName("newChild").getLength()).isEqualTo(1);
        }

        @Test
        @DisplayName("addChildElement with null text should add element without content")
        void addChildElementWithNullTextShouldAddElementWithoutContent() {
            Element newChild = DomUtil.addChildElement(root, "emptyChild", null);

            assertThat(newChild).isNotNull();
            assertThat(newChild.getTextContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Child Text Tests")
    class GetChildTextTests {

        @Test
        @DisplayName("getChildText should return text content of named child")
        void getChildTextShouldReturnTextContentOfNamedChild() {
            String text = DomUtil.getChildText(root, "child");

            assertThat(text).isEqualTo("text");
        }

        @Test
        @DisplayName("getChildText should return null for missing child")
        void getChildTextShouldReturnNullForMissingChild() {
            String text = DomUtil.getChildText(root, "nonexistent");

            assertThat(text).isNull();
        }
    }

    @Nested
    @DisplayName("Remove All Children Tests")
    class RemoveAllChildrenTests {

        @Test
        @DisplayName("removeAllChildren should remove all child nodes")
        void removeAllChildrenShouldRemoveAllChildNodes() {
            assertThat(root.hasChildNodes()).isTrue();

            DomUtil.removeAllChildren(root);

            assertThat(root.hasChildNodes()).isFalse();
        }
    }

    @Nested
    @DisplayName("Create Document Tests")
    class CreateDocumentTests {

        @Test
        @DisplayName("createDocument should create empty document")
        void createDocumentShouldCreateEmptyDocument() {
            Document doc = DomUtil.createDocument();

            assertThat(doc).isNotNull();
            assertThat(doc.getDocumentElement()).isNull();
        }

        @Test
        @DisplayName("createDocument with root name should create document with root element")
        void createDocumentWithRootNameShouldCreateDocumentWithRootElement() {
            Document doc = DomUtil.createDocument("myRoot");

            assertThat(doc).isNotNull();
            assertThat(doc.getDocumentElement()).isNotNull();
            assertThat(doc.getDocumentElement().getTagName()).isEqualTo("myRoot");
        }
    }

    @Nested
    @DisplayName("Namespace Tests")
    class NamespaceTests {

        @Test
        @DisplayName("namespace aware parsing should preserve namespace URI")
        void namespaceAwareParsingGetNamespaceURI() {
            Document nsDoc = DomParser.parseNamespaceAware(
                "<root xmlns=\"http://example.com\"/>"
            );

            String ns = nsDoc.getDocumentElement().getNamespaceURI();

            assertThat(ns).isEqualTo("http://example.com");
        }

        @Test
        @DisplayName("namespace aware parsing should preserve local name")
        void namespaceAwareParsingGetLocalName() {
            Document nsDoc = DomParser.parseNamespaceAware(
                "<ns:root xmlns:ns=\"http://example.com\"/>"
            );

            String localName = nsDoc.getDocumentElement().getLocalName();

            assertThat(localName).isEqualTo("root");
        }
    }
}
