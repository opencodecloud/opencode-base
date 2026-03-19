package cloud.opencode.base.xml.stax;

import org.junit.jupiter.api.*;

import javax.xml.stream.*;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * StaxUtilTest Tests
 * StaxUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("StaxUtil Tests")
class StaxUtilTest {

    private static final String TEST_XML = """
        <root attr="value">
            <item id="1">first</item>
            <item id="2">second</item>
            <item id="3">third</item>
        </root>
        """;

    @Nested
    @DisplayName("Event Type Name Tests")
    class EventTypeNameTests {

        @Test
        @DisplayName("getEventTypeName should return START_ELEMENT for start element")
        void getEventTypeNameShouldReturnStartElementForStartElement() {
            assertThat(StaxUtil.getEventTypeName(XMLStreamConstants.START_ELEMENT))
                .isEqualTo("START_ELEMENT");
        }

        @Test
        @DisplayName("getEventTypeName should return END_ELEMENT for end element")
        void getEventTypeNameShouldReturnEndElementForEndElement() {
            assertThat(StaxUtil.getEventTypeName(XMLStreamConstants.END_ELEMENT))
                .isEqualTo("END_ELEMENT");
        }

        @Test
        @DisplayName("getEventTypeName should return CHARACTERS for characters")
        void getEventTypeNameShouldReturnCharactersForCharacters() {
            assertThat(StaxUtil.getEventTypeName(XMLStreamConstants.CHARACTERS))
                .isEqualTo("CHARACTERS");
        }

        @Test
        @DisplayName("getEventTypeName should return START_DOCUMENT for start document")
        void getEventTypeNameShouldReturnStartDocumentForStartDocument() {
            assertThat(StaxUtil.getEventTypeName(XMLStreamConstants.START_DOCUMENT))
                .isEqualTo("START_DOCUMENT");
        }

        @Test
        @DisplayName("getEventTypeName should return END_DOCUMENT for end document")
        void getEventTypeNameShouldReturnEndDocumentForEndDocument() {
            assertThat(StaxUtil.getEventTypeName(XMLStreamConstants.END_DOCUMENT))
                .isEqualTo("END_DOCUMENT");
        }
    }

    @Nested
    @DisplayName("Factory Tests")
    class FactoryTests {

        @Test
        @DisplayName("createSecureInputFactory should create secure factory")
        void createSecureInputFactoryShouldCreateSecureFactory() {
            XMLInputFactory factory = StaxUtil.createSecureInputFactory();

            assertThat(factory).isNotNull();
        }

        @Test
        @DisplayName("createOutputFactory should create factory")
        void createOutputFactoryShouldCreateFactory() {
            XMLOutputFactory factory = StaxUtil.createOutputFactory();

            assertThat(factory).isNotNull();
        }

        @Test
        @DisplayName("createReader should create XMLStreamReader")
        void createReaderShouldCreateXmlStreamReader() {
            XMLStreamReader reader = StaxUtil.createReader(TEST_XML);

            assertThat(reader).isNotNull();
        }

        @Test
        @DisplayName("createWriter should create XMLStreamWriter")
        void createWriterShouldCreateXmlStreamWriter() {
            StringWriter sw = new StringWriter();
            XMLStreamWriter writer = StaxUtil.createWriter(sw);

            assertThat(writer).isNotNull();
        }
    }

    @Nested
    @DisplayName("ForEach Tests")
    class ForEachTests {

        @Test
        @DisplayName("forEachEvent should iterate all events")
        void forEachEventShouldIterateAllEvents() {
            int[] count = {0};

            StaxUtil.forEachEvent(TEST_XML, r -> count[0]++);

            assertThat(count[0]).isGreaterThan(0);
        }

        @Test
        @DisplayName("forEachStartElement should iterate start elements")
        void forEachStartElementShouldIterateStartElements() {
            int[] count = {0};

            StaxUtil.forEachStartElement(TEST_XML, r -> count[0]++);

            assertThat(count[0]).isEqualTo(4); // root + 3 items
        }
    }

    @Nested
    @DisplayName("Collect Tests")
    class CollectTests {

        @Test
        @DisplayName("collectElementNames should return all element names")
        void collectElementNamesShouldReturnAllElementNames() {
            List<String> names = StaxUtil.collectElementNames(TEST_XML);

            assertThat(names).contains("root", "item");
        }

        @Test
        @DisplayName("countElements should return element count")
        void countElementsShouldReturnElementCount() {
            int count = StaxUtil.countElements(TEST_XML, "item");

            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Attribute Tests")
    class AttributeTests {

        @Test
        @DisplayName("getAttributes should return all attributes")
        void getAttributesShouldReturnAllAttributes() throws Exception {
            XMLStreamReader reader = StaxUtil.createReader("<root a=\"1\" b=\"2\"/>");
            reader.nextTag();

            Map<String, String> attrs = StaxUtil.getAttributes(reader);

            assertThat(attrs).containsEntry("a", "1");
            assertThat(attrs).containsEntry("b", "2");
        }

        @Test
        @DisplayName("getAttribute should return attribute value as Optional")
        void getAttributeShouldReturnAttributeValueAsOptional() throws Exception {
            XMLStreamReader reader = StaxUtil.createReader("<root id=\"123\"/>");
            reader.nextTag();

            Optional<String> value = StaxUtil.getAttribute(reader, "id");

            assertThat(value).isPresent();
            assertThat(value.get()).isEqualTo("123");
        }

        @Test
        @DisplayName("getAttribute should return empty for missing attribute")
        void getAttributeShouldReturnEmptyForMissing() throws Exception {
            XMLStreamReader reader = StaxUtil.createReader("<root/>");
            reader.nextTag();

            Optional<String> value = StaxUtil.getAttribute(reader, "nonexistent");

            assertThat(value).isEmpty();
        }
    }

    @Nested
    @DisplayName("Text Content Tests")
    class TextContentTests {

        @Test
        @DisplayName("getElementTextSafe should return element text")
        void getElementTextSafeShouldReturnElementText() throws Exception {
            XMLStreamReader reader = StaxUtil.createReader("<root>content</root>");
            reader.nextTag();

            String text = StaxUtil.getElementTextSafe(reader);

            assertThat(text).isEqualTo("content");
        }

        @Test
        @DisplayName("getElementTextSafe should return empty for empty element")
        void getElementTextSafeShouldReturnEmptyForEmptyElement() throws Exception {
            XMLStreamReader reader = StaxUtil.createReader("<root/>");
            reader.nextTag();

            String text = StaxUtil.getElementTextSafe(reader);

            assertThat(text).isEmpty();
        }
    }

    @Nested
    @DisplayName("Skip Tests")
    class SkipTests {

        @Test
        @DisplayName("skipTo should skip to named element")
        void skipToShouldSkipToNamedElement() {
            XMLStreamReader reader = StaxUtil.createReader(TEST_XML);

            boolean found = StaxUtil.skipTo(reader, "item");

            assertThat(found).isTrue();
            assertThat(reader.getLocalName()).isEqualTo("item");
        }

        @Test
        @DisplayName("skipTo should return false if not found")
        void skipToShouldReturnFalseIfNotFound() {
            XMLStreamReader reader = StaxUtil.createReader("<root/>");

            boolean found = StaxUtil.skipTo(reader, "nonexistent");

            assertThat(found).isFalse();
        }
    }

    @Nested
    @DisplayName("Copy Element Tests")
    class CopyElementTests {

        @Test
        @DisplayName("elementToXml should convert element to XML string")
        void elementToXmlShouldConvertElementToXmlString() throws Exception {
            XMLStreamReader reader = StaxUtil.createReader("<root><child>text</child></root>");
            reader.nextTag(); // root
            reader.nextTag(); // child

            String xml = StaxUtil.elementToXml(reader);

            assertThat(xml).contains("<child>");
            assertThat(xml).contains("text");
        }
    }

    @Nested
    @DisplayName("Close Tests")
    class CloseTests {

        @Test
        @DisplayName("closeQuietly should close reader without exception")
        void closeQuietlyShouldCloseReaderWithoutException() {
            XMLStreamReader reader = StaxUtil.createReader("<root/>");

            assertThatCode(() -> StaxUtil.closeQuietly(reader))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("closeQuietly should handle null reader")
        void closeQuietlyShouldHandleNullReader() {
            assertThatCode(() -> StaxUtil.closeQuietly((XMLStreamReader) null))
                .doesNotThrowAnyException();
        }
    }
}
