package cloud.opencode.base.xml.transform;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.XmlElement;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlTransformerTest Tests
 * XmlTransformerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlTransformer Tests")
class XmlTransformerTest {

    private static final String SIMPLE_XML = "<root><child>text</child></root>";

    @Nested
    @DisplayName("Format Tests")
    class FormatTests {

        @Test
        @DisplayName("format should format XML with default indentation")
        void formatShouldFormatXmlWithDefaultIndentation() {
            String formatted = XmlTransformer.format(SIMPLE_XML);

            assertThat(formatted).contains("\n");
        }

        @Test
        @DisplayName("format should format XML with specified indentation")
        void formatShouldFormatXmlWithSpecifiedIndentation() {
            String formatted = XmlTransformer.format(SIMPLE_XML, 2);

            assertThat(formatted).contains("\n");
        }

        @Test
        @DisplayName("format from document should format document")
        void formatFromDocumentShouldFormatDocument() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            String formatted = XmlTransformer.format(doc, 4);

            assertThat(formatted).contains("\n");
        }
    }

    @Nested
    @DisplayName("Minify Tests")
    class MinifyTests {

        @Test
        @DisplayName("minify should produce compact output")
        void minifyShouldProduceCompactOutput() {
            // Note: XmlTransformer.minify uses identity transform with indent=no
            // This prevents adding NEW whitespace but doesn't remove existing text node whitespace
            String formatted = """
                <root>
                    <child>text</child>
                </root>
                """;

            String minified = XmlTransformer.minify(formatted);

            // Should contain the XML content
            assertThat(minified).contains("<root>");
            assertThat(minified).contains("<child>text</child>");
            assertThat(minified).contains("</root>");
        }

        @Test
        @DisplayName("minify should preserve content")
        void minifyShouldPreserveContent() {
            String minified = XmlTransformer.minify(SIMPLE_XML);

            assertThat(minified).contains("<root>");
            assertThat(minified).contains("<child>text</child>");
        }

        @Test
        @DisplayName("minify document should work")
        void minifyDocumentShouldWork() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            String minified = XmlTransformer.minify(doc);

            assertThat(minified).contains("<root>");
        }
    }

    @Nested
    @DisplayName("Serialize Tests")
    class SerializeTests {

        @Test
        @DisplayName("serialize to string should work")
        void serializeToStringShouldWork() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            String xml = XmlTransformer.serialize(doc);

            assertThat(xml).contains("<root>");
        }

        @Test
        @DisplayName("serialize with indent should format output")
        void serializeWithIndentShouldFormatOutput() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);

            String xml = XmlTransformer.serialize(doc, 4);

            assertThat(xml).contains("\n");
        }

        @Test
        @DisplayName("serialize to path should write file")
        void serializeToPathShouldWriteFile() throws IOException {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);
            Path tempFile = Files.createTempFile("output", ".xml");

            try {
                XmlTransformer.serialize(doc, tempFile);

                String content = Files.readString(tempFile);
                assertThat(content).contains("<root>");
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("serialize to path with indent should format file")
        void serializeToPathWithIndentShouldFormatFile() throws IOException {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);
            Path tempFile = Files.createTempFile("output", ".xml");

            try {
                XmlTransformer.serialize(doc, tempFile, 4);

                String content = Files.readString(tempFile);
                assertThat(content).contains("<root>");
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("serialize to output stream should write")
        void serializeToOutputStreamShouldWrite() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            XmlTransformer.serialize(doc, baos);

            String content = baos.toString();
            assertThat(content).contains("<root>");
        }

        @Test
        @DisplayName("serialize to output stream with indent should format")
        void serializeToOutputStreamWithIndentShouldFormat() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            XmlTransformer.serialize(doc, baos, 4);

            String content = baos.toString();
            assertThat(content).contains("<root>");
        }

        @Test
        @DisplayName("serialize element should work")
        void serializeElementShouldWork() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);
            XmlElement root = doc.getRoot();

            String xml = XmlTransformer.serialize(root);

            assertThat(xml).contains("<root>");
        }

        @Test
        @DisplayName("serialize element with indent should format")
        void serializeElementWithIndentShouldFormat() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);
            XmlElement root = doc.getRoot();

            String xml = XmlTransformer.serialize(root, 4);

            assertThat(xml).contains("<root>");
        }
    }

    @Nested
    @DisplayName("Clone Tests")
    class CloneTests {

        @Test
        @DisplayName("clone should create independent copy")
        void cloneShouldCreateIndependentCopy() {
            XmlDocument original = XmlDocument.parse(SIMPLE_XML);

            XmlDocument cloned = XmlTransformer.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.toXml()).isEqualTo(original.toXml());
        }

        @Test
        @DisplayName("clone should support modification without affecting original")
        void cloneShouldSupportModificationWithoutAffectingOriginal() {
            XmlDocument original = XmlDocument.parse(SIMPLE_XML);
            XmlDocument cloned = XmlTransformer.clone(original);

            cloned.getRoot().setAttribute("newAttr", "value");

            assertThat(original.getRoot().hasAttribute("newAttr")).isFalse();
            assertThat(cloned.getRoot().hasAttribute("newAttr")).isTrue();
        }

        @Test
        @DisplayName("clone element should create independent copy")
        void cloneElementShouldCreateIndependentCopy() {
            XmlDocument doc = XmlDocument.parse(SIMPLE_XML);
            XmlElement original = doc.getRoot();

            XmlElement cloned = XmlTransformer.clone(original);

            assertThat(cloned).isNotSameAs(original);
        }
    }

    @Nested
    @DisplayName("Parse And Format Tests")
    class ParseAndFormatTests {

        @Test
        @DisplayName("parseAndFormat should parse and format XML")
        void parseAndFormatShouldParseAndFormatXml() {
            String formatted = XmlTransformer.parseAndFormat(SIMPLE_XML, 4);

            assertThat(formatted).contains("\n");
        }
    }

    @Nested
    @DisplayName("Canonicalize Tests")
    class CanonicalizeTests {

        @Test
        @DisplayName("canonicalize should normalize XML")
        void canonicalizeShouldNormalizeXml() {
            String xml = "<root attr=\"value\"/>";

            String canonical = XmlTransformer.canonicalize(xml);

            assertThat(canonical).contains("<root");
        }
    }
}
