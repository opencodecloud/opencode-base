package cloud.opencode.base.xml.dom;

import org.junit.jupiter.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.assertj.core.api.Assertions.*;

/**
 * DomBuilderTest Tests
 * DomBuilderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("DomBuilder Tests")
class DomBuilderTest {

    @Nested
    @DisplayName("Create Tests")
    class CreateTests {

        @Test
        @DisplayName("should create builder with root element name")
        void shouldCreateWithRootName() {
            Document doc = DomBuilder.create("root").build();
            assertThat(doc).isNotNull();
            assertThat(doc.getDocumentElement().getTagName()).isEqualTo("root");
        }

        @Test
        @DisplayName("should create builder with namespace")
        void shouldCreateWithNamespace() {
            Document doc = DomBuilder.create("ns:root", "http://example.com").build();
            assertThat(doc).isNotNull();
            assertThat(doc.getDocumentElement().getNamespaceURI()).isEqualTo("http://example.com");
        }
    }

    @Nested
    @DisplayName("Add Element Tests")
    class AddElementTests {

        @Test
        @DisplayName("should add child element with text")
        void shouldAddElementWithText() {
            Document doc = DomBuilder.create("root")
                .addElement("name", "John")
                .build();

            Element name = (Element) doc.getDocumentElement().getElementsByTagName("name").item(0);
            assertThat(name).isNotNull();
            assertThat(name.getTextContent()).isEqualTo("John");
        }

        @Test
        @DisplayName("should add element with null text")
        void shouldAddElementWithNullText() {
            Document doc = DomBuilder.create("root")
                .addElement("empty", null)
                .build();

            Element empty = (Element) doc.getDocumentElement().getElementsByTagName("empty").item(0);
            assertThat(empty).isNotNull();
            assertThat(empty.getTextContent()).isEmpty();
        }

        @Test
        @DisplayName("should add element with text and attribute")
        void shouldAddElementWithTextAndAttribute() {
            Document doc = DomBuilder.create("root")
                .addElement("item", "value", "type", "special")
                .build();

            Element item = (Element) doc.getDocumentElement().getElementsByTagName("item").item(0);
            assertThat(item).isNotNull();
            assertThat(item.getTextContent()).isEqualTo("value");
            assertThat(item.getAttribute("type")).isEqualTo("special");
        }
    }

    @Nested
    @DisplayName("Attribute Tests")
    class AttributeTests {

        @Test
        @DisplayName("should add attribute to current element")
        void shouldAddAttribute() {
            Document doc = DomBuilder.create("root")
                .attribute("id", "123")
                .build();

            assertThat(doc.getDocumentElement().getAttribute("id")).isEqualTo("123");
        }
    }

    @Nested
    @DisplayName("Nested Element Tests")
    class NestedElementTests {

        @Test
        @DisplayName("should support nested elements via startElement/endElement")
        void shouldSupportNestedElements() {
            Document doc = DomBuilder.create("root")
                .startElement("address")
                    .addElement("city", "Beijing")
                    .addElement("country", "China")
                .endElement()
                .build();

            Element address = (Element) doc.getDocumentElement().getElementsByTagName("address").item(0);
            assertThat(address).isNotNull();

            Element city = (Element) address.getElementsByTagName("city").item(0);
            assertThat(city.getTextContent()).isEqualTo("Beijing");
        }

        @Test
        @DisplayName("should support namespace on nested elements")
        void shouldSupportNamespaceOnNested() {
            Document doc = DomBuilder.create("root")
                .startElement("ns:child", "http://example.com")
                .endElement()
                .build();

            Element child = (Element) doc.getDocumentElement().getElementsByTagNameNS("http://example.com", "child").item(0);
            assertThat(child).isNotNull();
        }

        @Test
        @DisplayName("endElement on root should stay at root")
        void endElementOnRootShouldStayAtRoot() {
            Document doc = DomBuilder.create("root")
                .endElement() // Should not fail, stays at root
                .addElement("test", "value")
                .build();

            assertThat(doc.getDocumentElement().getElementsByTagName("test").getLength()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Text Content Tests")
    class TextContentTests {

        @Test
        @DisplayName("should set text content on current element")
        void shouldSetTextContent() {
            Document doc = DomBuilder.create("root")
                .text("Hello World")
                .build();

            assertThat(doc.getDocumentElement().getTextContent()).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("should add CDATA section")
        void shouldAddCdataSection() {
            Document doc = DomBuilder.create("root")
                .cdata("<script>alert('xss')</script>")
                .build();

            assertThat(doc.getDocumentElement().getTextContent())
                .isEqualTo("<script>alert('xss')</script>");
        }
    }

    @Nested
    @DisplayName("Comment and PI Tests")
    class CommentAndPiTests {

        @Test
        @DisplayName("should add comment")
        void shouldAddComment() {
            DomBuilder builder = DomBuilder.create("root")
                .comment("This is a comment");

            assertThat(builder.build()).isNotNull();
        }

        @Test
        @DisplayName("should add processing instruction")
        void shouldAddProcessingInstruction() {
            DomBuilder builder = DomBuilder.create("root")
                .processingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"");

            assertThat(builder.build()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Navigation Tests")
    class NavigationTests {

        @Test
        @DisplayName("returnToRoot should reset current to root")
        void returnToRootShouldReset() {
            Document doc = DomBuilder.create("root")
                .startElement("child")
                    .startElement("grandchild")
                    .returnToRoot()
                .addElement("sibling", "value")
                .build();

            // sibling should be a direct child of root
            Element sibling = (Element) doc.getDocumentElement().getElementsByTagName("sibling").item(0);
            assertThat(sibling).isNotNull();
            assertThat(sibling.getParentNode()).isEqualTo(doc.getDocumentElement());
        }
    }

    @Nested
    @DisplayName("Accessor Tests")
    class AccessorTests {

        @Test
        @DisplayName("getDocument should return same as build")
        void getDocumentShouldReturnSameAsBuild() {
            DomBuilder builder = DomBuilder.create("root");
            assertThat(builder.getDocument()).isSameAs(builder.build());
        }

        @Test
        @DisplayName("getRootElement should return root element")
        void getRootElementShouldReturnRoot() {
            DomBuilder builder = DomBuilder.create("myRoot");
            assertThat(builder.getRootElement().getTagName()).isEqualTo("myRoot");
        }
    }

    @Nested
    @DisplayName("XML Output Tests")
    class XmlOutputTests {

        @Test
        @DisplayName("toXml should return non-empty XML string")
        void toXmlShouldReturnNonEmptyString() {
            String xml = DomBuilder.create("root")
                .addElement("name", "test")
                .toXml();

            assertThat(xml).isNotEmpty();
            assertThat(xml).contains("root");
            assertThat(xml).contains("name");
        }

        @Test
        @DisplayName("toXml with indent should return formatted string")
        void toXmlWithIndentShouldReturnFormatted() {
            String xml = DomBuilder.create("root")
                .addElement("name", "test")
                .toXml(2);

            assertThat(xml).isNotEmpty();
            assertThat(xml).contains("root");
        }
    }

    @Nested
    @DisplayName("Import Node Tests")
    class ImportNodeTests {

        @Test
        @DisplayName("should import node from another document")
        void shouldImportNode() {
            Document source = DomBuilder.create("source")
                .addElement("data", "imported")
                .build();

            Element sourceData = (Element) source.getDocumentElement()
                .getElementsByTagName("data").item(0);

            Document doc = DomBuilder.create("target")
                .importNode(sourceData, true)
                .build();

            Element importedData = (Element) doc.getDocumentElement()
                .getElementsByTagName("data").item(0);
            assertThat(importedData).isNotNull();
            assertThat(importedData.getTextContent()).isEqualTo("imported");
        }
    }
}
