package cloud.opencode.base.xml;

import cloud.opencode.base.xml.exception.XmlParseException;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlDocumentTest Tests
 * XmlDocumentTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlDocument Tests")
class XmlDocumentTest {

    private static final String SIMPLE_XML = "<root><child>text</child></root>";
    private static final String XML_WITH_ATTRS = "<root id=\"1\" name=\"test\"><child>text</child></root>";
    private static final String XML_WITH_NS = "<root xmlns=\"http://example.com\"><child>text</child></root>";

    @Nested
    @DisplayName("Parse Tests")
    class ParseTests {

        @Test
        @DisplayName("parse should parse valid XML string")
        void parseShouldParseValidXmlString() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            assertThat(doc).isNotNull();
            assertThat(doc.getRoot()).isNotNull();
            assertThat(doc.getRoot().getName()).isEqualTo("root");
        }

        @Test
        @DisplayName("parse should handle XML with attributes")
        void parseShouldHandleXmlWithAttributes() {
            XmlDocument doc = XmlDocument.parse(XML_WITH_ATTRS);

            assertThat(doc.getRoot().getAttribute("id")).isEqualTo("1");
            assertThat(doc.getRoot().getAttribute("name")).isEqualTo("test");
        }

        @Test
        @DisplayName("parse should handle XML with namespaces")
        void parseShouldHandleXmlWithNamespaces() {
            XmlDocument doc = XmlDocument.parse(XML_WITH_NS);

            assertThat(doc.getRoot()).isNotNull();
            assertThat(doc.getRoot().getName()).isEqualTo("root");
        }

        @Test
        @DisplayName("parse should throw exception for invalid XML")
        void parseShouldThrowExceptionForInvalidXml() {
            assertThatThrownBy(() -> XmlDocument.parse("<invalid"))
                .isInstanceOf(XmlParseException.class);
        }

        @Test
        @DisplayName("parse should throw exception for null input")
        void parseShouldThrowExceptionForNullInput() {
            assertThatThrownBy(() -> XmlDocument.parse((String) null))
                .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Load Tests")
    class LoadTests {

        @Test
        @DisplayName("load should load from path")
        void loadShouldLoadFromPath() throws IOException {
            Path tempFile = Files.createTempFile("test", ".xml");
            try {
                Files.writeString(tempFile, SIMPLE_XML);

                XmlDocument doc = XmlDocument.load(tempFile);

                assertThat(doc).isNotNull();
                assertThat(doc.getRoot().getName()).isEqualTo("root");
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("load should load from input stream")
        void loadShouldLoadFromInputStream() {
            InputStream is = new ByteArrayInputStream(SIMPLE_XML.getBytes());

            XmlDocument doc = XmlDocument.load(is);

            assertThat(doc).isNotNull();
            assertThat(doc.getRoot().getName()).isEqualTo("root");
        }
    }

    @Nested
    @DisplayName("Create Tests")
    class CreateTests {

        @Test
        @DisplayName("create should create document with root element")
        void createShouldCreateDocumentWithRootElement() {
            XmlDocument doc = XmlDocument.create("root");

            assertThat(doc).isNotNull();
            assertThat(doc.getRoot()).isNotNull();
            assertThat(doc.getRoot().getName()).isEqualTo("root");
        }

        @Test
        @DisplayName("of should create from DOM document")
        void ofShouldCreateFromDomDocument() {
            XmlDocument original = XmlDocument.parse(SIMPLE_XML);

            XmlDocument wrapped = XmlDocument.of(original.getDocument());

            assertThat(wrapped).isNotNull();
            assertThat(wrapped.getRoot().getName()).isEqualTo("root");
        }
    }

    @Nested
    @DisplayName("Root Element Tests")
    class RootElementTests {

        @Test
        @DisplayName("getRoot should return root element")
        void getRootShouldReturnRootElement() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            XmlElement root = doc.getRoot();

            assertThat(root).isNotNull();
            assertThat(root.getName()).isEqualTo("root");
        }
    }

    @Nested
    @DisplayName("XPath Tests")
    class XPathTests {

        @Test
        @DisplayName("xpath should evaluate XPath expression")
        void xpathShouldEvaluateXPathExpression() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            String result = doc.xpath("/root/child");

            assertThat(result).isEqualTo("text");
        }

        @Test
        @DisplayName("xpathList should return multiple elements")
        void xpathListShouldReturnMultipleElements() {
            XmlDocument doc = XmlDocument.parse("<root><item>1</item><item>2</item></root>");

            List<XmlElement> elements = doc.xpathList("/root/item");

            assertThat(elements).hasSize(2);
        }

        @Test
        @DisplayName("xpathOne should return single element")
        void xpathOneShouldReturnSingleElement() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            XmlElement element = doc.xpathOne("/root/child");

            assertThat(element).isNotNull();
            assertThat(element.getName()).isEqualTo("child");
        }

        @Test
        @DisplayName("xpath with type should return typed result")
        void xpathWithTypeShouldReturnTypedResult() {
            XmlDocument doc = XmlDocument.parse("<root><num>42</num></root>");

            Integer result = doc.xpath("/root/num", Integer.class);

            assertThat(result).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("Element Access Tests")
    class ElementAccessTests {

        @Test
        @DisplayName("getElement should return child element")
        void getElementShouldReturnChildElement() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            XmlElement child = doc.getElement("child");

            assertThat(child).isNotNull();
            assertThat(child.getName()).isEqualTo("child");
        }

        @Test
        @DisplayName("getElements should return all matching elements")
        void getElementsShouldReturnAllMatchingElements() {
            XmlDocument doc = XmlDocument.parse("<root><item>1</item><item>2</item></root>");

            List<XmlElement> items = doc.getElements("item");

            assertThat(items).hasSize(2);
        }

        @Test
        @DisplayName("getElementText should return text content")
        void getElementTextShouldReturnTextContent() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            String text = doc.getElementText("child");

            assertThat(text).isEqualTo("text");
        }

        @Test
        @DisplayName("getElementText with default should return default for missing")
        void getElementTextWithDefaultShouldReturnDefaultForMissing() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            String text = doc.getElementText("missing", "default");

            assertThat(text).isEqualTo("default");
        }

        @Test
        @DisplayName("hasElement should return true for existing element")
        void hasElementShouldReturnTrueForExistingElement() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            assertThat(doc.hasElement("child")).isTrue();
        }

        @Test
        @DisplayName("hasElement should return false for missing element")
        void hasElementShouldReturnFalseForMissingElement() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            assertThat(doc.hasElement("missing")).isFalse();
        }
    }

    @Nested
    @DisplayName("Modification Tests")
    class ModificationTests {

        @Test
        @DisplayName("addElement with text should add element with content")
        void addElementWithTextShouldAddElementWithContent() {
            XmlDocument doc = XmlDocument.create("root");

            doc.addElement("child", "content");

            XmlElement child = doc.getRoot().getChild("child");
            assertThat(child).isNotNull();
            assertThat(child.getText()).isEqualTo("content");
        }

        @Test
        @DisplayName("addElement should add XmlElement")
        void addElementShouldAddXmlElement() {
            XmlDocument doc = XmlDocument.create("root");
            XmlDocument other = XmlDocument.parse("<other>value</other>");

            doc.addElement(other.getRoot());

            assertThat(doc.getRoot().getChild("other")).isNotNull();
        }

        @Test
        @DisplayName("removeElement should remove child element")
        void removeElementShouldRemoveChildElement() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            doc.removeElement("child");

            assertThat(doc.getRoot().getChild("child")).isNull();
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("toXml should serialize to XML string")
        void toXmlShouldSerializeToXmlString() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            String xml = doc.toXml();

            assertThat(xml).contains("<root>");
            assertThat(xml).contains("<child>text</child>");
        }

        @Test
        @DisplayName("toXml with indent should format output")
        void toXmlWithIndentShouldFormatOutput() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            String xml = doc.toXml(4);

            assertThat(xml).contains("\n");
        }

        @Test
        @DisplayName("toString should return XML string")
        void toStringShouldReturnXmlString() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            String str = doc.toString();

            assertThat(str).contains("<root>");
        }

        @Test
        @DisplayName("toMap should convert to Map")
        void toMapShouldConvertToMap() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            Map<String, Object> map = doc.toMap();

            assertThat(map).isNotNull();
        }
    }

    @Nested
    @DisplayName("Save Tests")
    class SaveTests {

        @Test
        @DisplayName("save should write to path")
        void saveShouldWriteToPath() throws IOException {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);
            Path tempFile = Files.createTempFile("test", ".xml");

            try {
                doc.save(tempFile);

                String content = Files.readString(tempFile);
                assertThat(content).contains("<root>");
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }
    }

    @Nested
    @DisplayName("Binding Tests")
    class BindingTests {

        @Test
        @DisplayName("bind should bind to object")
        void bindShouldBindToObject() {
            XmlDocument doc = XmlDocument.parse("<testObj><value>test</value></testObj>");

            TestObj obj = doc.bind(TestObj.class);

            assertThat(obj).isNotNull();
            assertThat(obj.value).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("Document Access Tests")
    class DocumentAccessTests {

        @Test
        @DisplayName("getDocument should return underlying DOM document")
        void getDocumentShouldReturnUnderlyingDomDocument() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            org.w3c.dom.Document domDoc = doc.getDocument();

            assertThat(domDoc).isNotNull();
            assertThat(domDoc.getDocumentElement().getTagName()).isEqualTo("root");
        }
    }

    public static class TestObj {
        public String value;
    }
}
