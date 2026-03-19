package cloud.opencode.base.xml.stax;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;

import static org.assertj.core.api.Assertions.*;

/**
 * StaxWriterTest Tests
 * StaxWriterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("StaxWriter Tests")
class StaxWriterTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("create should create writer to StringWriter")
        void createShouldCreateWriterToStringWriter() {
            StaxWriter writer = StaxWriter.create();

            assertThat(writer).isNotNull();
        }

        @Test
        @DisplayName("create with OutputStream should create writer")
        void createWithOutputStreamShouldCreateWriter() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            StaxWriter writer = StaxWriter.create(baos);

            assertThat(writer).isNotNull();
        }

        @Test
        @DisplayName("create with Path should create writer")
        void createWithPathShouldCreateWriter() throws IOException {
            Path tempFile = Files.createTempFile("test", ".xml");
            try {
                StaxWriter writer = StaxWriter.create(tempFile);

                assertThat(writer).isNotNull();
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }
    }

    @Nested
    @DisplayName("Document Tests")
    class DocumentTests {

        @Test
        @DisplayName("startDocument should write XML declaration")
        void startDocumentShouldWriteXmlDeclaration() {
            StaxWriter writer = StaxWriter.create();

            writer.startDocument()
                .startElement("root")
                .endElement()
                .endDocument();

            String xml = writer.toString();
            assertThat(xml).contains("<?xml");
        }

        @Test
        @DisplayName("startDocument with encoding should set encoding")
        void startDocumentWithEncodingShouldSetEncoding() {
            StaxWriter writer = StaxWriter.create();

            writer.startDocument("UTF-8", "1.0")
                .startElement("root")
                .endElement()
                .endDocument();

            String xml = writer.toString();
            assertThat(xml).contains("UTF-8");
        }
    }

    @Nested
    @DisplayName("Element Tests")
    class ElementTests {

        @Test
        @DisplayName("startElement and endElement should create element")
        void startElementAndEndElementShouldCreateElement() {
            StaxWriter writer = StaxWriter.create();

            writer.startDocument()
                .startElement("root")
                .endElement()
                .endDocument();

            String xml = writer.toString();
            assertThat(xml).contains("<root");
            assertThat(xml).contains("</root>");
        }

        @Test
        @DisplayName("emptyElement should create self-closing element")
        void emptyElementShouldCreateSelfClosingElement() {
            StaxWriter writer = StaxWriter.create();

            writer.startDocument()
                .emptyElement("empty")
                .endDocument();

            String xml = writer.toString();
            assertThat(xml).contains("<empty");
        }

        @Test
        @DisplayName("element should create element with text")
        void elementShouldCreateElementWithText() {
            StaxWriter writer = StaxWriter.create();

            writer.startDocument()
                .element("item", "content")
                .endDocument();

            String xml = writer.toString();
            assertThat(xml).contains("<item>content</item>");
        }

        @Test
        @DisplayName("nested elements should work")
        void nestedElementsShouldWork() {
            StaxWriter writer = StaxWriter.create();

            writer.startDocument()
                .startElement("root")
                    .startElement("child")
                        .text("nested text")
                    .endElement()
                .endElement()
                .endDocument();

            String xml = writer.toString();
            assertThat(xml).contains("<root>");
            assertThat(xml).contains("<child>nested text</child>");
        }
    }

    @Nested
    @DisplayName("Attribute Tests")
    class AttributeTests {

        @Test
        @DisplayName("attribute should add attribute to element")
        void attributeShouldAddAttributeToElement() {
            StaxWriter writer = StaxWriter.create();

            writer.startDocument()
                .startElement("root")
                .attribute("id", "123")
                .endElement()
                .endDocument();

            String xml = writer.toString();
            assertThat(xml).contains("id=\"123\"");
        }

        @Test
        @DisplayName("multiple attributes should work")
        void multipleAttributesShouldWork() {
            StaxWriter writer = StaxWriter.create();

            writer.startDocument()
                .startElement("root")
                .attribute("a", "1")
                .attribute("b", "2")
                .endElement()
                .endDocument();

            String xml = writer.toString();
            assertThat(xml).contains("a=\"1\"");
            assertThat(xml).contains("b=\"2\"");
        }
    }

    @Nested
    @DisplayName("Text Content Tests")
    class TextContentTests {

        @Test
        @DisplayName("text should write text content")
        void textShouldWriteTextContent() {
            StaxWriter writer = StaxWriter.create();

            writer.startDocument()
                .startElement("root")
                .text("hello world")
                .endElement()
                .endDocument();

            String xml = writer.toString();
            assertThat(xml).contains("hello world");
        }

        @Test
        @DisplayName("cdata should write CDATA section")
        void cdataShouldWriteCdataSection() {
            StaxWriter writer = StaxWriter.create();

            writer.startDocument()
                .startElement("root")
                .cdata("<html>content</html>")
                .endElement()
                .endDocument();

            String xml = writer.toString();
            assertThat(xml).contains("<![CDATA[<html>content</html>]]>");
        }

        @Test
        @DisplayName("comment should write comment")
        void commentShouldWriteComment() {
            StaxWriter writer = StaxWriter.create();

            writer.startDocument()
                .startElement("root")
                .comment("This is a comment")
                .endElement()
                .endDocument();

            String xml = writer.toString();
            assertThat(xml).contains("<!--This is a comment-->");
        }
    }

    @Nested
    @DisplayName("Namespace Tests")
    class NamespaceTests {

        @Test
        @DisplayName("namespace should declare namespace with prefix")
        void namespaceShouldDeclareNamespaceWithPrefix() {
            StaxWriter writer = StaxWriter.create();

            writer.startDocument()
                .startElement("root")
                .namespace("ns", "http://example.com")
                .endElement()
                .endDocument();

            String xml = writer.toString();
            assertThat(xml).contains("xmlns:ns=\"http://example.com\"");
        }

        @Test
        @DisplayName("defaultNamespace should declare default namespace")
        void defaultNamespaceShouldDeclareDefaultNamespace() {
            StaxWriter writer = StaxWriter.create();

            writer.startDocument()
                .startElement("root")
                .defaultNamespace("http://example.com")
                .endElement()
                .endDocument();

            String xml = writer.toString();
            assertThat(xml).contains("xmlns=\"http://example.com\"");
        }
    }

    @Nested
    @DisplayName("Formatting Tests")
    class FormattingTests {

        @Test
        @DisplayName("formatted should enable indentation")
        void formattedShouldEnableIndentation() {
            StaxWriter writer = StaxWriter.create()
                .formatted(true);

            writer.startDocument()
                .startElement("root")
                .element("child", "text")
                .endElement()
                .endDocument();

            String xml = writer.toString();
            assertThat(xml).contains("\n");
        }

        @Test
        @DisplayName("indent should set indentation string")
        void indentShouldSetIndentationString() {
            StaxWriter writer = StaxWriter.create()
                .formatted(true)
                .indent("  ");

            writer.startDocument()
                .startElement("root")
                .element("child", "text")
                .endElement()
                .endDocument();

            String xml = writer.toString();
            assertThat(xml).contains("\n");
        }
    }

    @Nested
    @DisplayName("Complex Document Tests")
    class ComplexDocumentTests {

        @Test
        @DisplayName("should build complex document")
        void shouldBuildComplexDocument() {
            StaxWriter writer = StaxWriter.create();

            writer.startDocument("UTF-8", "1.0")
                .startElement("catalog")
                    .startElement("book")
                        .attribute("id", "1")
                        .element("title", "Java Programming")
                        .element("author", "John Doe")
                        .element("price", "29.99")
                    .endElement()
                    .startElement("book")
                        .attribute("id", "2")
                        .element("title", "XML Guide")
                        .element("author", "Jane Smith")
                        .element("price", "24.99")
                    .endElement()
                .endElement()
                .endDocument();

            String xml = writer.toString();
            assertThat(xml).contains("<catalog>");
            assertThat(xml).contains("<book id=\"1\">");
            assertThat(xml).contains("<title>Java Programming</title>");
            assertThat(xml).contains("<book id=\"2\">");
        }
    }

    @Nested
    @DisplayName("Output Tests")
    class OutputTests {

        @Test
        @DisplayName("toString should return XML string")
        void toStringShouldReturnXmlString() {
            StaxWriter writer = StaxWriter.create();
            writer.startDocument().element("root", "text").endDocument();

            String xml = writer.toString();

            assertThat(xml).contains("<root>text</root>");
        }

        @Test
        @DisplayName("flush should flush output")
        void flushShouldFlushOutput() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StaxWriter writer = StaxWriter.create(baos);
            writer.startDocument().element("root", "text").endDocument();

            writer.flush();

            assertThat(baos.toString()).contains("<root>");
        }

        @Test
        @DisplayName("close should close writer")
        void closeShouldCloseWriter() {
            StaxWriter writer = StaxWriter.create();
            writer.startDocument().element("root", "text").endDocument();

            assertThatCode(writer::close).doesNotThrowAnyException();
        }
    }
}
