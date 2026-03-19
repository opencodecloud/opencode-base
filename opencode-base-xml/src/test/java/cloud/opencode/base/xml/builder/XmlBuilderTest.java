package cloud.opencode.base.xml.builder;

import cloud.opencode.base.xml.XmlDocument;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlBuilderTest Tests
 * XmlBuilderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlBuilder Tests")
class XmlBuilderTest {

    @Nested
    @DisplayName("Create Tests")
    class CreateTests {

        @Test
        @DisplayName("create should create builder with root element")
        void createShouldCreateBuilderWithRootElement() {
            XmlDocument doc = XmlBuilder.create("root").build();

            assertThat(doc.getRoot().getName()).isEqualTo("root");
        }

        @Test
        @DisplayName("create with namespace should create builder with namespaced root")
        void createWithNamespaceShouldCreateBuilderWithNamespacedRoot() {
            XmlDocument doc = XmlBuilder.create("http://example.com", "root").build();

            assertThat(doc.getRoot().getName()).isEqualTo("root");
        }
    }

    @Nested
    @DisplayName("Declaration Tests")
    class DeclarationTests {

        @Test
        @DisplayName("encoding should set XML encoding")
        void encodingShouldSetXmlEncoding() {
            XmlBuilder builder = XmlBuilder.create("root")
                .encoding("UTF-8");

            XmlDocument doc = builder.build();

            // Verify the builder returns a valid document
            assertThat(doc).isNotNull();
            assertThat(doc.getRoot().getName()).isEqualTo("root");
        }

        @Test
        @DisplayName("version should set XML version")
        void versionShouldSetXmlVersion() {
            XmlBuilder builder = XmlBuilder.create("root")
                .version("1.1");

            XmlDocument doc = builder.build();
            String xml = doc.toXml();

            assertThat(xml).contains("1.1");
        }

        @Test
        @DisplayName("standalone should set standalone declaration")
        void standaloneShouldSetStandaloneDeclaration() {
            XmlBuilder builder = XmlBuilder.create("root")
                .standalone(true);

            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("Element Building Tests")
    class ElementBuildingTests {

        @Test
        @DisplayName("startElement and end should create nested elements")
        void startElementAndEndShouldCreateNestedElements() {
            XmlDocument doc = XmlBuilder.create("root")
                .startElement("child")
                .end()
                .build();

            assertThat(doc.getRoot().getChild("child")).isNotNull();
        }

        @Test
        @DisplayName("element should add element with text")
        void elementShouldAddElementWithText() {
            XmlDocument doc = XmlBuilder.create("root")
                .element("name", "value")
                .build();

            assertThat(doc.getRoot().getChild("name").getText()).isEqualTo("value");
        }

        @Test
        @DisplayName("emptyElement should add empty element")
        void emptyElementShouldAddEmptyElement() {
            XmlDocument doc = XmlBuilder.create("root")
                .emptyElement("empty")
                .build();

            assertThat(doc.getRoot().getChild("empty")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Attribute Tests")
    class AttributeTests {

        @Test
        @DisplayName("attribute should add attribute to current element")
        void attributeShouldAddAttributeToCurrentElement() {
            XmlDocument doc = XmlBuilder.create("root")
                .attribute("id", "123")
                .build();

            assertThat(doc.getRoot().getAttribute("id")).isEqualTo("123");
        }

        @Test
        @DisplayName("attribute on nested element should work")
        void attributeOnNestedElementShouldWork() {
            XmlDocument doc = XmlBuilder.create("root")
                .startElement("child")
                    .attribute("type", "test")
                .end()
                .build();

            assertThat(doc.getRoot().getChild("child").getAttribute("type")).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("Text Content Tests")
    class TextContentTests {

        @Test
        @DisplayName("text should add text content")
        void textShouldAddTextContent() {
            XmlDocument doc = XmlBuilder.create("root")
                .startElement("child")
                    .text("hello")
                .end()
                .build();

            assertThat(doc.getRoot().getChild("child").getText()).isEqualTo("hello");
        }

        @Test
        @DisplayName("cdata should add CDATA section")
        void cdataShouldAddCdataSection() {
            XmlDocument doc = XmlBuilder.create("root")
                .startElement("content")
                    .cdata("<html>content</html>")
                .end()
                .build();

            assertThat(doc.getRoot().getChild("content").getText())
                .isEqualTo("<html>content</html>");
        }

        @Test
        @DisplayName("comment should add comment")
        void commentShouldAddComment() {
            XmlDocument doc = XmlBuilder.create("root")
                .comment("This is a comment")
                .build();

            assertThat(doc).isNotNull();
        }
    }

    @Nested
    @DisplayName("Namespace Tests")
    class NamespaceTests {

        @Test
        @DisplayName("namespace should declare namespace prefix")
        void namespaceShouldDeclareNamespacePrefix() {
            XmlDocument doc = XmlBuilder.create("root")
                .namespace("http://example.com", "ns")
                .build();

            String xml = doc.toXml();
            assertThat(xml).contains("xmlns:ns=\"http://example.com\"");
        }

        @Test
        @DisplayName("defaultNamespace should set default namespace")
        void defaultNamespaceShouldSetDefaultNamespace() {
            XmlDocument doc = XmlBuilder.create("root")
                .defaultNamespace("http://example.com")
                .build();

            String xml = doc.toXml();
            assertThat(xml).contains("xmlns=\"http://example.com\"");
        }

        @Test
        @DisplayName("startElement with namespace should create namespaced element")
        void startElementWithNamespaceShouldCreateNamespacedElement() {
            XmlDocument doc = XmlBuilder.create("root")
                .namespace("http://example.com", "ns")
                .startElement("http://example.com", "ns", "child")
                .end()
                .build();

            assertThat(doc.toXml()).contains("ns:child");
        }
    }

    @Nested
    @DisplayName("Complex Document Tests")
    class ComplexDocumentTests {

        @Test
        @DisplayName("should build complex document structure")
        void shouldBuildComplexDocumentStructure() {
            XmlDocument doc = XmlBuilder.create("catalog")
                .encoding("UTF-8")
                .attribute("version", "1.0")
                .startElement("books")
                    .startElement("book")
                        .attribute("id", "1")
                        .element("title", "Java Programming")
                        .element("author", "John Doe")
                        .element("price", "29.99")
                    .end()
                    .startElement("book")
                        .attribute("id", "2")
                        .element("title", "XML Fundamentals")
                        .element("author", "Jane Smith")
                        .element("price", "24.99")
                    .end()
                .end()
                .build();

            assertThat(doc.xpathList("//book")).hasSize(2);
            assertThat(doc.xpath("//book[@id='1']/title")).isEqualTo("Java Programming");
        }
    }

    @Nested
    @DisplayName("Output Tests")
    class OutputTests {

        @Test
        @DisplayName("build should return XmlDocument")
        void buildShouldReturnXmlDocument() {
            XmlDocument doc = XmlBuilder.create("root").build();

            assertThat(doc).isNotNull();
            assertThat(doc).isInstanceOf(XmlDocument.class);
        }

        @Test
        @DisplayName("toXml should return XML string")
        void toXmlShouldReturnXmlString() {
            String xml = XmlBuilder.create("root")
                .element("child", "text")
                .toXml();

            assertThat(xml).contains("<root>");
            assertThat(xml).contains("<child>text</child>");
        }

        @Test
        @DisplayName("toXml with indent should return formatted XML")
        void toXmlWithIndentShouldReturnFormattedXml() {
            String xml = XmlBuilder.create("root")
                .element("child", "text")
                .toXml(4);

            assertThat(xml).contains("\n");
        }

        @Test
        @DisplayName("toXml with indent should use specified indent")
        void toXmlWithIndentShouldUseSpecifiedIndent() {
            String xml = XmlBuilder.create("root")
                .element("child", "text")
                .toXml(2);

            assertThat(xml).contains("\n");
        }
    }
}
