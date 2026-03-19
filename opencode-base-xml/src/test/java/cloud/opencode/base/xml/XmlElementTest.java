package cloud.opencode.base.xml;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlElementTest Tests
 * XmlElementTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlElement Tests")
class XmlElementTest {

    private XmlDocument testDocument;
    private XmlElement root;

    @BeforeEach
    void setUp() {
        testDocument = XmlDocument.parse("""
            <root id="1" name="test">
                <child attr="value">text content</child>
                <items>
                    <item>one</item>
                    <item>two</item>
                    <item>three</item>
                </items>
                <empty/>
                <nested>
                    <deep>deep value</deep>
                </nested>
            </root>
            """);
        root = testDocument.getRoot();
    }

    @Nested
    @DisplayName("Basic Properties Tests")
    class BasicPropertiesTests {

        @Test
        @DisplayName("getName should return element name")
        void getNameShouldReturnElementName() {
            assertThat(root.getName()).isEqualTo("root");
        }

        @Test
        @DisplayName("getLocalName should return local name")
        void getLocalNameShouldReturnLocalName() {
            assertThat(root.getLocalName()).isEqualTo("root");
        }

        @Test
        @DisplayName("getNode should return the underlying DOM node")
        void getNodeShouldReturnUnderlyingDomNode() {
            assertThat(root.getNode()).isNotNull();
            assertThat(root.getNode().getNodeName()).isEqualTo("root");
        }
    }

    @Nested
    @DisplayName("Attribute Tests")
    class AttributeTests {

        @Test
        @DisplayName("getAttribute should return attribute value")
        void getAttributeShouldReturnAttributeValue() {
            assertThat(root.getAttribute("id")).isEqualTo("1");
            assertThat(root.getAttribute("name")).isEqualTo("test");
        }

        @Test
        @DisplayName("getAttribute should return null for missing attribute")
        void getAttributeShouldReturnNullForMissingAttribute() {
            assertThat(root.getAttribute("nonexistent")).isNull();
        }

        @Test
        @DisplayName("getAttribute with default should return default for missing")
        void getAttributeWithDefaultShouldReturnDefaultForMissing() {
            String value = root.getAttribute("nonexistent", "default");

            assertThat(value).isEqualTo("default");
        }

        @Test
        @DisplayName("hasAttribute should return true for existing attribute")
        void hasAttributeShouldReturnTrueForExistingAttribute() {
            assertThat(root.hasAttribute("id")).isTrue();
        }

        @Test
        @DisplayName("hasAttribute should return false for missing attribute")
        void hasAttributeShouldReturnFalseForMissingAttribute() {
            assertThat(root.hasAttribute("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("getAttributes should return all attributes")
        void getAttributesShouldReturnAllAttributes() {
            Map<String, String> attrs = root.getAttributes();

            assertThat(attrs).containsEntry("id", "1");
            assertThat(attrs).containsEntry("name", "test");
        }

        @Test
        @DisplayName("getAttributes keySet should return all attribute names")
        void getAttributesKeySetShouldReturnAllAttributeNames() {
            Set<String> names = root.getAttributes().keySet();

            assertThat(names).contains("id", "name");
        }

        @Test
        @DisplayName("getAttribute with Integer class should parse integer attribute")
        void getAttributeWithIntegerClassShouldParseIntegerAttribute() {
            Integer value = root.getAttribute("id", Integer.class);

            assertThat(value).isEqualTo(1);
        }

        @Test
        @DisplayName("getAttribute with Long class should parse long attribute")
        void getAttributeWithLongClassShouldParseLongAttribute() {
            Long value = root.getAttribute("id", Long.class);

            assertThat(value).isEqualTo(1L);
        }

        @Test
        @DisplayName("getAttribute with Boolean class should parse boolean attribute")
        void getAttributeWithBooleanClassShouldParseBooleanAttribute() {
            XmlDocument doc = XmlDocument.parse("<root enabled=\"true\"/>");

            Boolean value = doc.getRoot().getAttribute("enabled", Boolean.class);

            assertThat(value).isTrue();
        }
    }

    @Nested
    @DisplayName("Child Element Tests")
    class ChildElementTests {

        @Test
        @DisplayName("getChild should return first matching child")
        void getChildShouldReturnFirstMatchingChild() {
            XmlElement child = root.getChild("child");

            assertThat(child).isNotNull();
            assertThat(child.getName()).isEqualTo("child");
        }

        @Test
        @DisplayName("getChild should return null for missing child")
        void getChildShouldReturnNullForMissingChild() {
            XmlElement child = root.getChild("nonexistent");

            assertThat(child).isNull();
        }

        @Test
        @DisplayName("getChildren should return all matching children")
        void getChildrenShouldReturnAllMatchingChildren() {
            XmlElement items = root.getChild("items");
            List<XmlElement> itemList = items.getChildren("item");

            assertThat(itemList).hasSize(3);
        }

        @Test
        @DisplayName("getChildren without name should return all children")
        void getChildrenWithoutNameShouldReturnAllChildren() {
            List<XmlElement> children = root.getChildren();

            assertThat(children).isNotEmpty();
        }

        @Test
        @DisplayName("getChildCount greater than zero indicates element has children")
        void getChildCountGreaterThanZeroIndicatesElementHasChildren() {
            assertThat(root.getChildCount()).isGreaterThan(0);
        }

        @Test
        @DisplayName("getChildCount should be zero for empty element")
        void getChildCountShouldBeZeroForEmptyElement() {
            XmlElement empty = root.getChild("empty");

            assertThat(empty.getChildCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("getChildren first element should return first child element")
        void getChildrenFirstElementShouldReturnFirstChildElement() {
            List<XmlElement> children = root.getChildren();

            assertThat(children).isNotEmpty();
            XmlElement first = children.get(0);
            assertThat(first).isNotNull();
        }

        @Test
        @DisplayName("getChildCount should return number of children")
        void getChildCountShouldReturnNumberOfChildren() {
            int count = root.getChildCount();

            assertThat(count).isGreaterThan(0);
        }

        @Test
        @DisplayName("hasChild should return true for existing child")
        void hasChildShouldReturnTrueForExistingChild() {
            assertThat(root.hasChild("child")).isTrue();
        }

        @Test
        @DisplayName("hasChild should return false for missing child")
        void hasChildShouldReturnFalseForMissingChild() {
            assertThat(root.hasChild("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("getChildText should return child text content")
        void getChildTextShouldReturnChildTextContent() {
            String text = root.getChildText("child");

            assertThat(text).isEqualTo("text content");
        }

        @Test
        @DisplayName("getChildText should return null for missing child")
        void getChildTextShouldReturnNullForMissingChild() {
            String text = root.getChildText("nonexistent");

            assertThat(text).isNull();
        }

        @Test
        @DisplayName("getChildText with default should return default for missing child")
        void getChildTextWithDefaultShouldReturnDefaultForMissingChild() {
            String text = root.getChildText("nonexistent", "default");

            assertThat(text).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("Text Content Tests")
    class TextContentTests {

        @Test
        @DisplayName("getText should return text content")
        void getTextShouldReturnTextContent() {
            XmlElement child = root.getChild("child");

            assertThat(child.getText()).isEqualTo("text content");
        }

        @Test
        @DisplayName("getText should return empty for element without text")
        void getTextShouldReturnEmptyForElementWithoutText() {
            XmlElement empty = root.getChild("empty");

            assertThat(empty.getText()).isEmpty();
        }

        @Test
        @DisplayName("hasText should return true for element with text")
        void hasTextShouldReturnTrueForElementWithText() {
            XmlElement child = root.getChild("child");

            assertThat(child.hasText()).isTrue();
        }

        @Test
        @DisplayName("getTextTrim should return trimmed text")
        void getTextTrimShouldReturnTrimmedText() {
            XmlElement child = root.getChild("child");

            assertThat(child.getTextTrim()).isEqualTo("text content");
        }

        @Test
        @DisplayName("getTextAs Integer class should parse integer text")
        void getTextAsIntegerClassShouldParseIntegerText() {
            XmlDocument doc = XmlDocument.parse("<root><num>42</num></root>");
            Integer value = doc.getRoot().getChild("num").getTextAs(Integer.class);

            assertThat(value).isEqualTo(42);
        }

        @Test
        @DisplayName("getTextAs Long class should parse long text")
        void getTextAsLongClassShouldParseLongText() {
            XmlDocument doc = XmlDocument.parse("<root><num>9999999999</num></root>");

            Long value = doc.getRoot().getChild("num").getTextAs(Long.class);

            assertThat(value).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("getTextAs Boolean class should parse boolean text")
        void getTextAsBooleanClassShouldParseBooleanText() {
            XmlDocument doc = XmlDocument.parse("<root><flag>true</flag></root>");

            Boolean value = doc.getRoot().getChild("flag").getTextAs(Boolean.class);

            assertThat(value).isTrue();
        }

        @Test
        @DisplayName("getText with default should return default for empty text")
        void getTextWithDefaultShouldReturnDefaultForEmptyText() {
            XmlElement empty = root.getChild("empty");

            String value = empty.getText("default");

            assertThat(value).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("XPath Tests")
    class XPathTests {

        @Test
        @DisplayName("xpath should evaluate XPath expression from element")
        void xpathShouldEvaluateXPathExpressionFromElement() {
            String value = root.xpath("child");

            assertThat(value).isEqualTo("text content");
        }

        @Test
        @DisplayName("xpathList should return matching elements")
        void xpathListShouldReturnMatchingElements() {
            List<XmlElement> items = root.xpathList("items/item");

            assertThat(items).hasSize(3);
        }

        @Test
        @DisplayName("xpathList first element should return nested element")
        void xpathListFirstElementShouldReturnNestedElement() {
            List<XmlElement> result = root.xpathList("nested/deep");

            assertThat(result).isNotEmpty();
            assertThat(result.get(0).getText()).isEqualTo("deep value");
        }
    }

    @Nested
    @DisplayName("Modification Tests")
    class ModificationTests {

        @Test
        @DisplayName("setAttribute should set attribute value")
        void setAttributeShouldSetAttributeValue() {
            XmlDocument doc = XmlDocument.create("root");

            doc.getRoot().setAttribute("newAttr", "newValue");

            assertThat(doc.getRoot().getAttribute("newAttr")).isEqualTo("newValue");
        }

        @Test
        @DisplayName("setText should set text content")
        void setTextShouldSetTextContent() {
            XmlDocument doc = XmlDocument.create("root");

            doc.getRoot().setText("new text");

            assertThat(doc.getRoot().getText()).isEqualTo("new text");
        }

        @Test
        @DisplayName("addChild should add child element")
        void addChildShouldAddChildElement() {
            XmlDocument doc = XmlDocument.create("root");

            XmlElement child = doc.getRoot().addChild("child");

            assertThat(child).isNotNull();
            assertThat(doc.getRoot().getChild("child")).isNotNull();
        }

        @Test
        @DisplayName("addChild with text should add element with content")
        void addChildWithTextShouldAddElementWithContent() {
            XmlDocument doc = XmlDocument.create("root");

            XmlElement child = doc.getRoot().addChild("child", "content");

            assertThat(child.getText()).isEqualTo("content");
        }

        @Test
        @DisplayName("removeAttribute should remove attribute")
        void removeAttributeShouldRemoveAttribute() {
            XmlDocument doc = XmlDocument.parse("<root attr=\"value\"/>");

            doc.getRoot().removeAttribute("attr");

            assertThat(doc.getRoot().hasAttribute("attr")).isFalse();
        }

        @Test
        @DisplayName("removeChild should remove child element")
        void removeChildShouldRemoveChildElement() {
            XmlDocument doc = XmlDocument.parse("<root><child/></root>");

            doc.getRoot().removeChild("child");

            assertThat(doc.getRoot().hasChild("child")).isFalse();
        }

        @Test
        @DisplayName("removeChildren should remove all children")
        void removeChildrenShouldRemoveAllChildren() {
            XmlDocument doc = XmlDocument.parse("<root><a/><b/><c/></root>");

            doc.getRoot().removeChildren();

            assertThat(doc.getRoot().getChildCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("addChild with XmlElement should add existing element")
        void addChildWithXmlElementShouldAddExistingElement() {
            XmlDocument doc1 = XmlDocument.create("root");
            XmlDocument doc2 = XmlDocument.parse("<other><child>content</child></other>");
            XmlElement childToAdd = doc2.getRoot().getChild("child");

            doc1.getRoot().addChild(childToAdd);

            assertThat(doc1.getRoot().hasChild("child")).isTrue();
            assertThat(doc1.getRoot().getChild("child").getText()).isEqualTo("content");
        }
    }

    @Nested
    @DisplayName("Namespace Tests")
    class NamespaceTests {

        @Test
        @DisplayName("getNamespaceUri should return null when parser is not namespace aware")
        void getNamespaceUriShouldReturnNullWhenParserIsNotNamespaceAware() {
            // Note: XmlDocument.parse uses a non-namespace-aware parser by default
            // so getNamespaceUri() returns null even for elements with xmlns declarations
            XmlDocument doc = XmlDocument.parse("<root xmlns=\"http://example.com\"/>");

            String ns = doc.getRoot().getNamespaceUri();

            // Without namespace-aware parsing, getNamespaceURI() returns null
            assertThat(ns).isNull();
        }

        @Test
        @DisplayName("getPrefix should return null when parser is not namespace aware")
        void getPrefixShouldReturnNullWhenParserIsNotNamespaceAware() {
            // Note: XmlDocument.parse uses a non-namespace-aware parser by default
            // so getPrefix() returns null even for elements with prefixes
            XmlDocument doc = XmlDocument.parse("<ns:root xmlns:ns=\"http://example.com\"/>");

            String prefix = doc.getRoot().getPrefix();

            // Without namespace-aware parsing, getPrefix() returns null
            assertThat(prefix).isNull();
        }

        @Test
        @DisplayName("getNamespaceUri should return null for element without namespace")
        void getNamespaceUriShouldReturnNullForElementWithoutNamespace() {
            XmlDocument doc = XmlDocument.parse("<root/>");

            String ns = doc.getRoot().getNamespaceUri();

            assertThat(ns).isNull();
        }
    }

    @Nested
    @DisplayName("Conversion Tests")
    class ConversionTests {

        @Test
        @DisplayName("toMap should convert element to map")
        void toMapShouldConvertElementToMap() {
            XmlDocument doc = XmlDocument.parse("<root><name>test</name><value>123</value></root>");

            Map<String, Object> map = doc.getRoot().toMap();

            assertThat(map).containsKey("name");
            assertThat(map).containsKey("value");
        }

        @Test
        @DisplayName("toXml should serialize element to XML")
        void toXmlShouldSerializeElementToXml() {
            XmlElement child = root.getChild("child");

            String xml = child.toXml();

            assertThat(xml).contains("<child");
            assertThat(xml).contains("text content");
        }

        @Test
        @DisplayName("toXml with indent should serialize element with formatting")
        void toXmlWithIndentShouldSerializeElementWithFormatting() {
            String xml = root.toXml(2);

            assertThat(xml).contains("<root");
        }

        @Test
        @DisplayName("toString should return XML representation")
        void toStringShouldReturnXmlRepresentation() {
            XmlElement child = root.getChild("child");

            String str = child.toString();

            assertThat(str).contains("<child");
        }
    }

    @Nested
    @DisplayName("Element Access Tests")
    class ElementAccessTests {

        @Test
        @DisplayName("getElement should return underlying DOM element")
        void getElementShouldReturnUnderlyingDomElement() {
            org.w3c.dom.Element domElement = root.getElement();

            assertThat(domElement).isNotNull();
            assertThat(domElement.getTagName()).isEqualTo("root");
        }
    }

    @Nested
    @DisplayName("Parent Element Tests")
    class ParentElementTests {

        @Test
        @DisplayName("getParent should return parent element")
        void getParentShouldReturnParentElement() {
            XmlElement child = root.getChild("child");

            XmlElement parent = child.getParent();

            assertThat(parent).isNotNull();
            assertThat(parent.getName()).isEqualTo("root");
        }

        @Test
        @DisplayName("getParent should return null for root element")
        void getParentShouldReturnNullForRootElement() {
            XmlElement parent = root.getParent();

            assertThat(parent).isNull();
        }

        @Test
        @DisplayName("hasParent should return true for child element")
        void hasParentShouldReturnTrueForChildElement() {
            XmlElement child = root.getChild("child");

            assertThat(child.hasParent()).isTrue();
        }

        @Test
        @DisplayName("hasParent should return false for root element")
        void hasParentShouldReturnFalseForRootElement() {
            assertThat(root.hasParent()).isFalse();
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("equals should return true for same element")
        void equalsShouldReturnTrueForSameElement() {
            assertThat(root.equals(root)).isTrue();
        }

        @Test
        @DisplayName("equals should return true for equal elements")
        void equalsShouldReturnTrueForEqualElements() {
            XmlDocument doc1 = XmlDocument.parse("<root/>");
            XmlDocument doc2 = XmlDocument.parse("<root/>");

            assertThat(doc1.getRoot().equals(doc2.getRoot())).isTrue();
        }

        @Test
        @DisplayName("equals should return false for different elements")
        void equalsShouldReturnFalseForDifferentElements() {
            XmlDocument doc1 = XmlDocument.parse("<root/>");
            XmlDocument doc2 = XmlDocument.parse("<other/>");

            assertThat(doc1.getRoot().equals(doc2.getRoot())).isFalse();
        }

        @Test
        @DisplayName("hashCode should be consistent")
        void hashCodeShouldBeConsistent() {
            int hash1 = root.hashCode();
            int hash2 = root.hashCode();

            assertThat(hash1).isEqualTo(hash2);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of with Element should create XmlElement")
        void ofWithElementShouldCreateXmlElement() {
            org.w3c.dom.Element domElement = root.getElement();

            XmlElement xmlElement = XmlElement.of(domElement);

            assertThat(xmlElement).isNotNull();
            assertThat(xmlElement.getName()).isEqualTo("root");
        }

        @Test
        @DisplayName("of with Node should create XmlElement if node is Element")
        void ofWithNodeShouldCreateXmlElementIfNodeIsElement() {
            org.w3c.dom.Node node = root.getNode();

            XmlElement xmlElement = XmlElement.of(node);

            assertThat(xmlElement).isNotNull();
            assertThat(xmlElement.getName()).isEqualTo("root");
        }

        @Test
        @DisplayName("of with non-Element Node should throw exception")
        void ofWithNonElementNodeShouldThrowException() {
            org.w3c.dom.Node textNode = root.getElement().getOwnerDocument().createTextNode("text");

            assertThatThrownBy(() -> XmlElement.of(textNode))
                    .isInstanceOf(cloud.opencode.base.xml.exception.OpenXmlException.class);
        }
    }
}
