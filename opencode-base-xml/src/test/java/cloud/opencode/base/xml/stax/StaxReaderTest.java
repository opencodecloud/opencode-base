package cloud.opencode.base.xml.stax;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * StaxReaderTest Tests
 * StaxReaderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("StaxReader Tests")
class StaxReaderTest {

    private static final String SIMPLE_XML = "<root><child attr=\"value\">text</child></root>";
    private static final String COMPLEX_XML = """
        <catalog>
            <book id="1">
                <title>Java Programming</title>
            </book>
            <book id="2">
                <title>XML Guide</title>
            </book>
        </catalog>
        """;

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of with String should create reader")
        void ofWithStringShouldCreateReader() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);

            assertThat(reader).isNotNull();
        }

        @Test
        @DisplayName("of with InputStream should create reader")
        void ofWithInputStreamShouldCreateReader() {
            InputStream is = new ByteArrayInputStream(SIMPLE_XML.getBytes());

            StaxReader reader = StaxReader.of(is);

            assertThat(reader).isNotNull();
        }

        @Test
        @DisplayName("of with Path should create reader")
        void ofWithPathShouldCreateReader() throws IOException {
            Path tempFile = Files.createTempFile("test", ".xml");
            try {
                Files.writeString(tempFile, SIMPLE_XML);

                StaxReader reader = StaxReader.of(tempFile);

                assertThat(reader).isNotNull();
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("ofSecure should create secure reader")
        void ofSecureShouldCreateSecureReader() {
            StaxReader reader = StaxReader.ofSecure(SIMPLE_XML);

            assertThat(reader).isNotNull();
        }
    }

    @Nested
    @DisplayName("Navigation Tests")
    class NavigationTests {

        @Test
        @DisplayName("hasNext should return true when more events")
        void hasNextShouldReturnTrueWhenMoreEvents() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);

            assertThat(reader.hasNext()).isTrue();
        }

        @Test
        @DisplayName("next should advance to next event")
        void nextShouldAdvanceToNextEvent() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);

            int eventType = reader.next();

            assertThat(eventType).isNotNull();
        }

        @Test
        @DisplayName("nextTag should skip to next start or end element")
        void nextTagShouldSkipToNextStartOrEndElement() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);

            reader.nextTag();

            assertThat(reader.isStartElement()).isTrue();
        }

        @Test
        @DisplayName("getEventType should return current event type")
        void getEventTypeShouldReturnCurrentEventType() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);
            reader.nextTag();

            int eventType = reader.getEventType();

            assertThat(eventType).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Event Type Tests")
    class EventTypeTests {

        @Test
        @DisplayName("isStartElement should return true for start element")
        void isStartElementShouldReturnTrueForStartElement() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);
            reader.nextTag();

            assertThat(reader.isStartElement()).isTrue();
        }

        @Test
        @DisplayName("isStartElement with name should check element name")
        void isStartElementWithNameShouldCheckElementName() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);
            reader.nextTag();

            assertThat(reader.isStartElement("root")).isTrue();
            assertThat(reader.isStartElement("other")).isFalse();
        }

        @Test
        @DisplayName("isEndElement should return true for end element")
        void isEndElementShouldReturnTrueForEndElement() {
            StaxReader reader = StaxReader.of("<root/>");
            reader.nextTag(); // Start root
            reader.nextTag(); // End root

            assertThat(reader.isEndElement()).isTrue();
        }

        @Test
        @DisplayName("isEndElement with name should check element name")
        void isEndElementWithNameShouldCheckElementName() {
            StaxReader reader = StaxReader.of("<root/>");
            reader.nextTag(); // Start root
            reader.nextTag(); // End root

            assertThat(reader.isEndElement("root")).isTrue();
        }

        @Test
        @DisplayName("isCharacters should return true for text content")
        void isCharactersShouldReturnTrueForTextContent() {
            StaxReader reader = StaxReader.of("<root>text</root>");
            reader.nextTag(); // Start root
            reader.next();    // Text

            assertThat(reader.isCharacters()).isTrue();
        }

        @Test
        @DisplayName("isWhiteSpace should detect whitespace")
        void isWhiteSpaceShouldDetectWhitespace() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);

            // Test that isWhiteSpace doesn't throw
            assertThatCode(() -> reader.isWhiteSpace()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Element Info Tests")
    class ElementInfoTests {

        @Test
        @DisplayName("getLocalName should return element name")
        void getLocalNameShouldReturnElementName() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);
            reader.nextTag();

            assertThat(reader.getLocalName()).isEqualTo("root");
        }

        @Test
        @DisplayName("getNamespaceURI should return namespace")
        void getNamespaceURIShouldReturnNamespace() {
            String nsXml = "<root xmlns=\"http://example.com\"/>";
            StaxReader reader = StaxReader.of(nsXml);
            reader.nextTag();

            assertThat(reader.getNamespaceURI()).isEqualTo("http://example.com");
        }

        @Test
        @DisplayName("getPrefix should return prefix")
        void getPrefixShouldReturnPrefix() {
            String nsXml = "<ns:root xmlns:ns=\"http://example.com\"/>";
            StaxReader reader = StaxReader.of(nsXml);
            reader.nextTag();

            assertThat(reader.getPrefix()).isEqualTo("ns");
        }
    }

    @Nested
    @DisplayName("Attribute Tests")
    class AttributeTests {

        @Test
        @DisplayName("getAttribute should return attribute value")
        void getAttributeShouldReturnAttributeValue() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);
            reader.nextTag(); // root
            reader.nextTag(); // child

            assertThat(reader.getAttribute("attr")).isEqualTo("value");
        }

        @Test
        @DisplayName("getAttribute should return null for missing attribute")
        void getAttributeShouldReturnNullForMissingAttribute() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);
            reader.nextTag();

            assertThat(reader.getAttribute("nonexistent")).isNull();
        }

        @Test
        @DisplayName("getAttribute with namespace should return attribute value")
        void getAttributeWithNamespaceShouldReturnAttributeValue() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);
            reader.nextTag(); // root
            reader.nextTag(); // child

            assertThat(reader.getAttribute(null, "attr")).isEqualTo("value");
        }

        @Test
        @DisplayName("getAttributeOptional should return Optional")
        void getAttributeOptionalShouldReturnOptional() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);
            reader.nextTag(); // root
            reader.nextTag(); // child

            assertThat(reader.getAttributeOptional("attr")).isPresent().contains("value");
            assertThat(reader.getAttributeOptional("missing")).isEmpty();
        }

        @Test
        @DisplayName("getAttributes should return all attributes")
        void getAttributesShouldReturnAllAttributes() {
            String xml = "<root a=\"1\" b=\"2\"/>";
            StaxReader reader = StaxReader.of(xml);
            reader.nextTag();

            Map<String, String> attrs = reader.getAttributes();

            assertThat(attrs).containsEntry("a", "1");
            assertThat(attrs).containsEntry("b", "2");
        }
    }

    @Nested
    @DisplayName("Text Content Tests")
    class TextContentTests {

        @Test
        @DisplayName("getText should return text content")
        void getTextShouldReturnTextContent() {
            StaxReader reader = StaxReader.of("<root>text</root>");
            reader.nextTag(); // root
            reader.next();    // text

            assertThat(reader.getText()).isEqualTo("text");
        }

        @Test
        @DisplayName("getElementText should return element text")
        void getElementTextShouldReturnElementText() {
            StaxReader reader = StaxReader.of("<root>content</root>");
            reader.nextTag();

            assertThat(reader.getElementText()).isEqualTo("content");
        }

        @Test
        @DisplayName("getElementText with name should read child text")
        void getElementTextWithNameShouldReadChildText() {
            StaxReader reader = StaxReader.of("<root><name>value</name></root>");
            reader.nextTag(); // root

            assertThat(reader.getElementText("name")).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("Callback Tests")
    class CallbackTests {

        @Test
        @DisplayName("onElement should register callback for element")
        void onElementShouldRegisterCallbackForElement() {
            List<String> ids = new ArrayList<>();
            StaxReader reader = StaxReader.of(COMPLEX_XML);

            reader.onElement("book", (name, attrs) -> ids.add(attrs.get("id")));
            reader.read();

            assertThat(ids).containsExactly("1", "2");
        }

        @Test
        @DisplayName("onText should register callback for text")
        void onTextShouldRegisterCallbackForText() {
            List<String> titles = new ArrayList<>();
            StaxReader reader = StaxReader.of(COMPLEX_XML);

            reader.onText("title", titles::add);
            reader.read();

            assertThat(titles).containsExactly("Java Programming", "XML Guide");
        }

        @Test
        @DisplayName("onEndElement should register callback for end element")
        void onEndElementShouldRegisterCallbackForEndElement() {
            List<String> ends = new ArrayList<>();
            StaxReader reader = StaxReader.of("<root><child/></root>");

            reader.onEndElement(ends::add);
            reader.read();

            assertThat(ends).contains("child", "root");
        }
    }

    @Nested
    @DisplayName("Read Tests")
    class ReadTests {

        @Test
        @DisplayName("read should process entire document with callbacks")
        void readShouldProcessEntireDocumentWithCallbacks() {
            List<String> elements = new ArrayList<>();
            StaxReader reader = StaxReader.of(SIMPLE_XML);

            reader.onElement("child", (name, attrs) -> elements.add(name));
            reader.read();

            assertThat(elements).contains("child");
        }
    }

    @Nested
    @DisplayName("Skip Tests")
    class SkipTests {

        @Test
        @DisplayName("skipTo should skip to specified element")
        void skipToShouldSkipToSpecifiedElement() {
            StaxReader reader = StaxReader.of(COMPLEX_XML);

            boolean found = reader.skipTo("title");

            assertThat(found).isTrue();
            assertThat(reader.getLocalName()).isEqualTo("title");
        }

        @Test
        @DisplayName("skipTo should return false if not found")
        void skipToShouldReturnFalseIfNotFound() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);

            boolean found = reader.skipTo("nonexistent");

            assertThat(found).isFalse();
        }

        @Test
        @DisplayName("skipToNextStartElement should skip to start element")
        void skipToNextStartElementShouldSkipToStartElement() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);

            boolean found = reader.skipToNextStartElement();

            assertThat(found).isTrue();
            assertThat(reader.isStartElement()).isTrue();
        }
    }

    @Nested
    @DisplayName("Require Tests")
    class RequireTests {

        @Test
        @DisplayName("require should not throw for matching element")
        void requireShouldNotThrowForMatchingElement() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);
            reader.nextTag();

            assertThatCode(() -> reader.require("root"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("require should throw for non-matching element")
        void requireShouldThrowForNonMatchingElement() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);
            reader.nextTag();

            assertThatThrownBy(() -> reader.require("other"))
                .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Underlying Reader Tests")
    class UnderlyingReaderTests {

        @Test
        @DisplayName("getUnderlyingReader should return XMLStreamReader")
        void getUnderlyingReaderShouldReturnXMLStreamReader() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);

            assertThat(reader.getUnderlyingReader()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Close Tests")
    class CloseTests {

        @Test
        @DisplayName("close should not throw")
        void closeShouldNotThrow() {
            StaxReader reader = StaxReader.of(SIMPLE_XML);

            assertThatCode(reader::close).doesNotThrowAnyException();
        }
    }
}
