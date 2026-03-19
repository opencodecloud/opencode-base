package cloud.opencode.base.xml.dom;

import cloud.opencode.base.xml.exception.XmlParseException;
import org.junit.jupiter.api.*;
import org.w3c.dom.Document;

import java.io.*;
import java.nio.file.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DomParserTest Tests
 * DomParserTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("DomParser Tests")
class DomParserTest {

    private static final String SIMPLE_XML = "<root><child>text</child></root>";
    private static final String NS_XML = "<root xmlns=\"http://example.com\"><child>text</child></root>";
    private static final String INVALID_XML = "<root><child></root>";

    @Nested
    @DisplayName("Parse String Tests")
    class ParseStringTests {

        @Test
        @DisplayName("parse should parse valid XML string")
        void parseShouldParseValidXmlString() {
            Document doc = DomParser.parse(SIMPLE_XML);

            assertThat(doc).isNotNull();
            assertThat(doc.getDocumentElement().getTagName()).isEqualTo("root");
        }

        @Test
        @DisplayName("parse should throw exception for invalid XML")
        void parseShouldThrowExceptionForInvalidXml() {
            assertThatThrownBy(() -> DomParser.parse(INVALID_XML))
                .isInstanceOf(XmlParseException.class);
        }

        @Test
        @DisplayName("parse should throw exception for null input")
        void parseShouldThrowExceptionForNullInput() {
            assertThatThrownBy(() -> DomParser.parse((String) null))
                .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("parse should throw exception for empty string")
        void parseShouldThrowExceptionForEmptyString() {
            assertThatThrownBy(() -> DomParser.parse(""))
                .isInstanceOf(XmlParseException.class);
        }
    }

    @Nested
    @DisplayName("Parse Path Tests")
    class ParsePathTests {

        @Test
        @DisplayName("parse should parse XML from path")
        void parseShouldParseXmlFromPath() throws IOException {
            Path tempFile = Files.createTempFile("test", ".xml");
            try {
                Files.writeString(tempFile, SIMPLE_XML);

                Document doc = DomParser.parse(tempFile);

                assertThat(doc).isNotNull();
                assertThat(doc.getDocumentElement().getTagName()).isEqualTo("root");
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("parse should throw exception for non-existent file")
        void parseShouldThrowExceptionForNonExistentFile() {
            Path nonExistent = Path.of("/nonexistent/file.xml");

            assertThatThrownBy(() -> DomParser.parse(nonExistent))
                .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Parse InputStream Tests")
    class ParseInputStreamTests {

        @Test
        @DisplayName("parse should parse XML from input stream")
        void parseShouldParseXmlFromInputStream() {
            InputStream is = new ByteArrayInputStream(SIMPLE_XML.getBytes());

            Document doc = DomParser.parse(is);

            assertThat(doc).isNotNull();
            assertThat(doc.getDocumentElement().getTagName()).isEqualTo("root");
        }

        @Test
        @DisplayName("parse should throw exception for null stream")
        void parseShouldThrowExceptionForNullStream() {
            assertThatThrownBy(() -> DomParser.parse((InputStream) null))
                .isInstanceOf(XmlParseException.class);
        }
    }

    @Nested
    @DisplayName("Namespace Aware Parse Tests")
    class NamespaceAwareParseTests {

        @Test
        @DisplayName("parseNamespaceAware should parse with namespace support")
        void parseNamespaceAwareShouldParseWithNamespaceSupport() {
            Document doc = DomParser.parseNamespaceAware(NS_XML);

            assertThat(doc).isNotNull();
            assertThat(doc.getDocumentElement().getNamespaceURI()).isEqualTo("http://example.com");
        }

        @Test
        @DisplayName("parseNamespaceAware from path should parse with namespace support")
        void parseNamespaceAwareFromPathShouldParseWithNamespaceSupport() throws IOException {
            Path tempFile = Files.createTempFile("test", ".xml");
            try {
                Files.writeString(tempFile, NS_XML);

                Document doc = DomParser.parseNamespaceAware(tempFile);

                assertThat(doc).isNotNull();
                assertThat(doc.getDocumentElement().getNamespaceURI()).isEqualTo("http://example.com");
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("parseNamespaceAware from stream should parse with namespace support")
        void parseNamespaceAwareFromStreamShouldParseWithNamespaceSupport() {
            InputStream is = new ByteArrayInputStream(NS_XML.getBytes());

            Document doc = DomParser.parseNamespaceAware(is);

            assertThat(doc).isNotNull();
            assertThat(doc.getDocumentElement().getNamespaceURI()).isEqualTo("http://example.com");
        }
    }

    @Nested
    @DisplayName("Create Document Tests")
    class CreateDocumentTests {

        @Test
        @DisplayName("createDocument should create empty document")
        void createDocumentShouldCreateEmptyDocument() {
            Document doc = DomParser.createDocument();

            assertThat(doc).isNotNull();
            assertThat(doc.getDocumentElement()).isNull();
        }

        @Test
        @DisplayName("createDocument with root should create document with root")
        void createDocumentWithRootShouldCreateDocumentWithRoot() {
            Document doc = DomParser.createDocument("root");

            assertThat(doc).isNotNull();
            assertThat(doc.getDocumentElement().getTagName()).isEqualTo("root");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("isValidXml should return true for valid XML")
        void isValidXmlShouldReturnTrueForValidXml() {
            assertThat(DomParser.isValidXml(SIMPLE_XML)).isTrue();
        }

        @Test
        @DisplayName("isValidXml should return false for invalid XML")
        void isValidXmlShouldReturnFalseForInvalidXml() {
            assertThat(DomParser.isValidXml(INVALID_XML)).isFalse();
        }

        @Test
        @DisplayName("isValidXml should return false for null")
        void isValidXmlShouldReturnFalseForNull() {
            assertThat(DomParser.isValidXml(null)).isFalse();
        }

        @Test
        @DisplayName("isValidXml should return false for empty string")
        void isValidXmlShouldReturnFalseForEmptyString() {
            assertThat(DomParser.isValidXml("")).isFalse();
        }
    }
}
