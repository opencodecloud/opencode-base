package cloud.opencode.base.xml.builder;

import cloud.opencode.base.xml.dom.DomBuilder;
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
        @DisplayName("create should create builder with root element")
        void createShouldCreateBuilderWithRootElement() {
            DomBuilder builder = DomBuilder.create("root");

            Document doc = builder.build();

            assertThat(doc.getDocumentElement().getTagName()).isEqualTo("root");
        }

        @Test
        @DisplayName("create with namespace should create builder with namespace")
        void createWithNamespaceShouldCreateBuilderWithNamespace() {
            DomBuilder builder = DomBuilder.create("root", "http://example.com");

            Document doc = builder.build();

            assertThat(doc.getDocumentElement().getNamespaceURI()).isEqualTo("http://example.com");
        }
    }

    @Nested
    @DisplayName("Element Building Tests")
    class ElementBuildingTests {

        @Test
        @DisplayName("startElement and endElement should create nested elements")
        void startElementAndEndElementShouldCreateNestedElements() {
            Document doc = DomBuilder.create("root")
                .startElement("child")
                .endElement()
                .build();

            Element root = doc.getDocumentElement();
            assertThat(root.getElementsByTagName("child").getLength()).isEqualTo(1);
        }

        @Test
        @DisplayName("multiple nested elements should work correctly")
        void multipleNestedElementsShouldWorkCorrectly() {
            Document doc = DomBuilder.create("root")
                .startElement("level1")
                    .startElement("level2")
                        .startElement("level3")
                        .endElement()
                    .endElement()
                .endElement()
                .build();

            Element root = doc.getDocumentElement();
            assertThat(root.getElementsByTagName("level3").getLength()).isEqualTo(1);
        }

        @Test
        @DisplayName("endElement should return to parent element")
        void endElementShouldReturnToParentElement() {
            Document doc = DomBuilder.create("root")
                .startElement("child1")
                .endElement()
                .startElement("child2")
                .endElement()
                .build();

            Element root = doc.getDocumentElement();
            assertThat(root.getChildNodes().getLength()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Attribute Tests")
    class AttributeTests {

        @Test
        @DisplayName("attribute should add attribute to current element")
        void attributeShouldAddAttributeToCurrentElement() {
            Document doc = DomBuilder.create("root")
                .attribute("id", "123")
                .build();

            assertThat(doc.getDocumentElement().getAttribute("id")).isEqualTo("123");
        }

        @Test
        @DisplayName("multiple attributes should work")
        void multipleAttributesShouldWork() {
            Document doc = DomBuilder.create("root")
                .attribute("id", "123")
                .attribute("name", "test")
                .build();

            Element root = doc.getDocumentElement();
            assertThat(root.getAttribute("id")).isEqualTo("123");
            assertThat(root.getAttribute("name")).isEqualTo("test");
        }

        @Test
        @DisplayName("attribute on nested element should work")
        void attributeOnNestedElementShouldWork() {
            Document doc = DomBuilder.create("root")
                .startElement("child")
                    .attribute("childAttr", "value")
                .endElement()
                .build();

            Element child = (Element) doc.getElementsByTagName("child").item(0);
            assertThat(child.getAttribute("childAttr")).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("Add Element Tests")
    class AddElementTests {

        @Test
        @DisplayName("addElement with text should add element with content")
        void addElementWithTextShouldAddElementWithContent() {
            Document doc = DomBuilder.create("root")
                .addElement("child", "text content")
                .build();

            Element child = (Element) doc.getElementsByTagName("child").item(0);
            assertThat(child.getTextContent()).isEqualTo("text content");
        }

        @Test
        @DisplayName("addElement with attribute should add element with attr")
        void addElementWithAttributeShouldAddElementWithAttr() {
            Document doc = DomBuilder.create("root")
                .addElement("child", "text", "id", "123")
                .build();

            Element child = (Element) doc.getElementsByTagName("child").item(0);
            assertThat(child.getTextContent()).isEqualTo("text");
            assertThat(child.getAttribute("id")).isEqualTo("123");
        }
    }

    @Nested
    @DisplayName("Text Content Tests")
    class TextContentTests {

        @Test
        @DisplayName("text should set text content")
        void textShouldSetTextContent() {
            Document doc = DomBuilder.create("root")
                .startElement("child")
                    .text("hello world")
                .endElement()
                .build();

            Element child = (Element) doc.getElementsByTagName("child").item(0);
            assertThat(child.getTextContent()).isEqualTo("hello world");
        }

        @Test
        @DisplayName("cdata should add CDATA section")
        void cdataShouldAddCdataSection() {
            Document doc = DomBuilder.create("root")
                .startElement("child")
                    .cdata("<special>content</special>")
                .endElement()
                .build();

            Element child = (Element) doc.getElementsByTagName("child").item(0);
            assertThat(child.getTextContent()).isEqualTo("<special>content</special>");
        }

        @Test
        @DisplayName("comment should add comment node")
        void commentShouldAddCommentNode() {
            Document doc = DomBuilder.create("root")
                .comment("This is a comment")
                .build();

            assertThat(doc).isNotNull();
        }
    }

    @Nested
    @DisplayName("Processing Instruction Tests")
    class ProcessingInstructionTests {

        @Test
        @DisplayName("processingInstruction should add PI")
        void processingInstructionShouldAddPi() {
            Document doc = DomBuilder.create("root")
                .processingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"")
                .build();

            assertThat(doc).isNotNull();
        }
    }

    @Nested
    @DisplayName("Element Access Tests")
    class ElementAccessTests {

        @Test
        @DisplayName("getRootElement should return root element")
        void getRootElementShouldReturnRootElement() {
            DomBuilder builder = DomBuilder.create("root")
                .startElement("child");

            Element root = builder.getRootElement();

            assertThat(root.getTagName()).isEqualTo("root");
        }

        @Test
        @DisplayName("getDocument should return document")
        void getDocumentShouldReturnDocument() {
            DomBuilder builder = DomBuilder.create("root");

            Document doc = builder.getDocument();

            assertThat(doc).isNotNull();
            assertThat(doc.getDocumentElement().getTagName()).isEqualTo("root");
        }

        @Test
        @DisplayName("returnToRoot should return to root")
        void returnToRootShouldReturnToRoot() {
            DomBuilder builder = DomBuilder.create("root")
                .startElement("child")
                    .startElement("grandchild")
                    .returnToRoot()
                .addElement("sibling", "text");

            Document doc = builder.build();
            Element root = doc.getDocumentElement();
            assertThat(root.getElementsByTagName("sibling").getLength()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Build Tests")
    class BuildTests {

        @Test
        @DisplayName("build should return document")
        void buildShouldReturnDocument() {
            Document doc = DomBuilder.create("root").build();

            assertThat(doc).isNotNull();
            assertThat(doc.getDocumentElement()).isNotNull();
        }

        @Test
        @DisplayName("toXml should return XML string")
        void toXmlShouldReturnXmlString() {
            String xml = DomBuilder.create("root")
                .addElement("child", "text")
                .toXml();

            assertThat(xml).contains("<root>");
            assertThat(xml).contains("<child>text</child>");
        }

        @Test
        @DisplayName("toXml with indent should format output")
        void toXmlWithIndentShouldFormatOutput() {
            String xml = DomBuilder.create("root")
                .addElement("child", "text")
                .toXml(4);

            assertThat(xml).contains("<root>");
        }
    }
}
