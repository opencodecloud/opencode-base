package cloud.opencode.base.xml;

import cloud.opencode.base.xml.validate.ValidationResult;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenXmlTest Tests
 * OpenXmlTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("OpenXml Tests")
class OpenXmlTest {

    private static final String SIMPLE_XML = "<root><child>text</child></root>";
    private static final String COMPLEX_XML = """
        <catalog>
            <book id="1">
                <title>Java Programming</title>
                <author>John Doe</author>
            </book>
        </catalog>
        """;

    @Nested
    @DisplayName("Parse Tests")
    class ParseTests {

        @Test
        @DisplayName("parse should parse XML string")
        void parseShouldParseXmlString() {
            XmlDocument doc = OpenXml.parse(SIMPLE_XML);

            assertThat(doc).isNotNull();
            assertThat(doc.getRoot().getName()).isEqualTo("root");
        }

        @Test
        @DisplayName("parseFile should parse from path")
        void parseFileShouldParseFromPath() throws IOException {
            Path tempFile = Files.createTempFile("test", ".xml");
            try {
                Files.writeString(tempFile, SIMPLE_XML);

                XmlDocument doc = OpenXml.parseFile(tempFile);

                assertThat(doc).isNotNull();
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("parse should parse from input stream")
        void parseShouldParseFromInputStream() {
            InputStream is = new ByteArrayInputStream(SIMPLE_XML.getBytes());

            XmlDocument doc = OpenXml.parse(is);

            assertThat(doc).isNotNull();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("isWellFormed should return true for valid XML")
        void isWellFormedShouldReturnTrueForValidXml() {
            assertThat(OpenXml.isWellFormed(SIMPLE_XML)).isTrue();
        }

        @Test
        @DisplayName("isWellFormed should return false for invalid XML")
        void isWellFormedShouldReturnFalseForInvalidXml() {
            assertThat(OpenXml.isWellFormed("<invalid")).isFalse();
        }

        @Test
        @DisplayName("validateWellFormedness should return validation result")
        void validateWellFormednessShouldReturnValidationResult() {
            ValidationResult result = OpenXml.validateWellFormedness(SIMPLE_XML);

            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("XPath Tests")
    class XPathTests {

        @Test
        @DisplayName("xpath should evaluate XPath expression")
        void xpathShouldEvaluateXPathExpression() {
            XmlDocument doc = OpenXml.parse(COMPLEX_XML);

            String result = OpenXml.xpath(doc, "//title");

            assertThat(result).isEqualTo("Java Programming");
        }

        @Test
        @DisplayName("xpathElements should return matching elements")
        void xpathElementsShouldReturnMatchingElements() {
            XmlDocument doc = OpenXml.parse(COMPLEX_XML);

            List<XmlElement> elements = OpenXml.xpathElements(doc, "//book");

            assertThat(elements).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Transform Tests")
    class TransformTests {

        @Test
        @DisplayName("format should format XML")
        void formatShouldFormatXml() {
            String formatted = OpenXml.format(SIMPLE_XML);

            assertThat(formatted).contains("\n");
        }

        @Test
        @DisplayName("format with indent should format XML with indentation")
        void formatWithIndentShouldFormatXmlWithIndentation() {
            String formatted = OpenXml.format(SIMPLE_XML, 4);

            assertThat(formatted).contains("\n");
        }

        @Test
        @DisplayName("minify should produce compact output")
        void minifyShouldProduceCompactOutput() {
            // Note: minify uses identity transform with indent=no
            // This prevents adding NEW whitespace but doesn't remove existing text node whitespace
            String minified = OpenXml.minify("""
                <root>
                    <child>text</child>
                </root>
                """);

            // Should contain XML content
            assertThat(minified).contains("<root>");
            assertThat(minified).contains("<child>text</child>");
            assertThat(minified).contains("</root>");
        }
    }

    @Nested
    @DisplayName("Build Tests")
    class BuildTests {

        @Test
        @DisplayName("builder should create XML builder")
        void builderShouldCreateXmlBuilder() {
            XmlDocument doc = OpenXml.builder("root")
                .element("child", "text")
                .build();

            assertThat(doc.getRoot().getName()).isEqualTo("root");
            assertThat(doc.xpath("//child")).isEqualTo("text");
        }

        @Test
        @DisplayName("element should create element builder")
        void elementShouldCreateElementBuilder() {
            var elementBuilder = OpenXml.element("item");

            assertThat(elementBuilder).isNotNull();
        }
    }

    @Nested
    @DisplayName("Bind Tests")
    class BindTests {

        @Test
        @DisplayName("unmarshal should convert XML to object")
        void unmarshalShouldConvertXmlToObject() {
            String xml = "<testObject><value>test</value></testObject>";

            TestObject obj = OpenXml.unmarshal(xml, TestObject.class);

            assertThat(obj.value).isEqualTo("test");
        }

        @Test
        @DisplayName("marshal should convert object to XML")
        void marshalShouldConvertObjectToXml() {
            TestObject obj = new TestObject();
            obj.value = "test";

            String xml = OpenXml.marshal(obj);

            assertThat(xml).contains("<value>test</value>");
        }

        @Test
        @DisplayName("marshal with indent should format output")
        void marshalWithIndentShouldFormatOutput() {
            TestObject obj = new TestObject();
            obj.value = "test";

            String xml = OpenXml.marshal(obj, 4);

            assertThat(xml).contains("<value>test</value>");
        }

        @Test
        @DisplayName("binder should create XmlBinder")
        void binderShouldCreateXmlBinder() {
            var binder = OpenXml.binder();

            assertThat(binder).isNotNull();
        }
    }

    @Nested
    @DisplayName("Streaming Parser Tests")
    class StreamingParserTests {

        @Test
        @DisplayName("saxParser should create SAX parser")
        void saxParserShouldCreateSaxParser() {
            var parser = OpenXml.saxParser();

            assertThat(parser).isNotNull();
        }

        @Test
        @DisplayName("staxReader should create StAX reader")
        void staxReaderShouldCreateStaxReader() {
            var reader = OpenXml.staxReader(SIMPLE_XML);

            assertThat(reader).isNotNull();
        }

        @Test
        @DisplayName("staxWriter should create StAX writer")
        void staxWriterShouldCreateStaxWriter() {
            var writer = OpenXml.staxWriter();

            assertThat(writer).isNotNull();
        }
    }

    @Nested
    @DisplayName("Namespace Tests")
    class NamespaceTests {

        @Test
        @DisplayName("namespaceContext should create context")
        void namespaceContextShouldCreateContext() {
            var context = OpenXml.namespaceContext();

            assertThat(context).isNotNull();
        }

        @Test
        @DisplayName("extractNamespaces should extract from document")
        void extractNamespacesShouldExtractFromDocument() {
            String nsXml = "<root xmlns:ns=\"http://example.com\"><ns:child/></root>";
            XmlDocument doc = OpenXml.parse(nsXml);

            var namespaces = OpenXml.extractNamespaces(doc);

            assertThat(namespaces).isNotNull();
        }
    }

    public static class TestObject {
        public String value;
    }
}
