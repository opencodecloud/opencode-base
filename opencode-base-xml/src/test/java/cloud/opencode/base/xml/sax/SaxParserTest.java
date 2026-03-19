package cloud.opencode.base.xml.sax;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * SaxParserTest Tests
 * SaxParserTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("SaxParser Tests")
class SaxParserTest {

    private static final String SIMPLE_XML = "<root><child>text</child></root>";
    private static final String COMPLEX_XML = """
        <catalog>
            <book id="1">
                <title>Java Programming</title>
                <author>John Doe</author>
            </book>
            <book id="2">
                <title>XML Guide</title>
                <author>Jane Smith</author>
            </book>
        </catalog>
        """;

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("create should create parser")
        void createShouldCreateParser() {
            SaxParser parser = SaxParser.create();

            assertThat(parser).isNotNull();
        }

        @Test
        @DisplayName("createSecure should create secure parser")
        void createSecureShouldCreateSecureParser() {
            SaxParser parser = SaxParser.createSecure();

            assertThat(parser).isNotNull();
        }
    }

    @Nested
    @DisplayName("Handler Configuration Tests")
    class HandlerConfigurationTests {

        @Test
        @DisplayName("handler should set SAX handler")
        void handlerShouldSetSaxHandler() {
            List<String> elements = new ArrayList<>();
            SaxHandler handler = new SaxHandler() {
                @Override
                public void startElement(String uri, String localName, String qName,
                                         Map<String, String> attributes) {
                    elements.add(qName);
                }
            };

            SaxParser.create()
                .handler(handler)
                .parse(SIMPLE_XML);

            assertThat(elements).contains("root", "child");
        }
    }

    @Nested
    @DisplayName("Parse String Tests")
    class ParseStringTests {

        @Test
        @DisplayName("parse should parse XML string")
        void parseShouldParseXmlString() {
            List<String> elements = new ArrayList<>();

            SaxParser.create()
                .handler(SimpleSaxHandler.create()
                    .onAnyStart((name, attrs) -> elements.add(name)))
                .parse(SIMPLE_XML);

            assertThat(elements).contains("root", "child");
        }

        @Test
        @DisplayName("parse should capture text content")
        void parseShouldCaptureTextContent() {
            List<String> texts = new ArrayList<>();

            SaxParser.create()
                .handler(SimpleSaxHandler.create()
                    .onText("child", texts::add))
                .parse(SIMPLE_XML);

            assertThat(texts).contains("text");
        }
    }

    @Nested
    @DisplayName("Parse Path Tests")
    class ParsePathTests {

        @Test
        @DisplayName("parse should parse from path")
        void parseShouldParseFromPath() throws IOException {
            Path tempFile = Files.createTempFile("test", ".xml");
            try {
                Files.writeString(tempFile, SIMPLE_XML);
                List<String> elements = new ArrayList<>();

                SaxParser.create()
                    .handler(SimpleSaxHandler.create()
                        .onAnyStart((name, attrs) -> elements.add(name)))
                    .parse(tempFile);

                assertThat(elements).contains("root", "child");
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }
    }

    @Nested
    @DisplayName("Parse InputStream Tests")
    class ParseInputStreamTests {

        @Test
        @DisplayName("parse should parse from input stream")
        void parseShouldParseFromInputStream() {
            InputStream is = new ByteArrayInputStream(SIMPLE_XML.getBytes());
            List<String> elements = new ArrayList<>();

            SaxParser.create()
                .handler(SimpleSaxHandler.create()
                    .onAnyStart((name, attrs) -> elements.add(name)))
                .parse(is);

            assertThat(elements).contains("root", "child");
        }
    }

    @Nested
    @DisplayName("Namespace Aware Tests")
    class NamespaceAwareTests {

        @Test
        @DisplayName("namespaceAware should enable namespace processing")
        void namespaceAwareShouldEnableNamespaceProcessing() {
            String nsXml = "<root xmlns=\"http://example.com\"><child/></root>";
            List<String> uris = new ArrayList<>();

            SaxParser.create()
                .namespaceAware(true)
                .handler(new SaxHandler() {
                    @Override
                    public void startElement(String uri, String localName, String qName,
                                             Map<String, String> attributes) {
                        if (!uri.isEmpty()) {
                            uris.add(uri);
                        }
                    }
                })
                .parse(nsXml);

            assertThat(uris).contains("http://example.com");
        }
    }

    @Nested
    @DisplayName("Validating Tests")
    class ValidatingTests {

        @Test
        @DisplayName("validating should enable DTD validation")
        void validatingShouldEnableDtdValidation() {
            SaxParser parser = SaxParser.create()
                .validating(true);

            assertThat(parser).isNotNull();
        }
    }

    @Nested
    @DisplayName("Secure Parsing Tests")
    class SecureParsingTests {

        @Test
        @DisplayName("secure should create secure parser")
        void secureShouldCreateSecureParser() {
            SaxParser parser = SaxParser.create()
                .secure(true);

            assertThat(parser).isNotNull();
        }
    }

    @Nested
    @DisplayName("Complex XML Tests")
    class ComplexXmlTests {

        @Test
        @DisplayName("should parse complex XML with attributes")
        void shouldParseComplexXmlWithAttributes() {
            List<String> titles = new ArrayList<>();
            List<String> ids = new ArrayList<>();

            SaxParser.create()
                .handler(SimpleSaxHandler.create()
                    .onStart("book", (name, attrs) -> ids.add(attrs.get("id")))
                    .onText("title", titles::add))
                .parse(COMPLEX_XML);

            assertThat(ids).containsExactly("1", "2");
            assertThat(titles).containsExactly("Java Programming", "XML Guide");
        }
    }
}
