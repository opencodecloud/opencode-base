package cloud.opencode.base.xml.xpath;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.XmlElement;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * XPathResultTest Tests
 * XPathResultTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XPathResult Tests")
class XPathResultTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of with value and xpath should create result")
        void ofWithValueAndXpathShouldCreateResult() {
            XPathResult result = XPathResult.of("test value", "/path");

            assertThat(result.value()).isEqualTo("test value");
            assertThat(result.xpath()).isEqualTo("/path");
        }

        @Test
        @DisplayName("of with elements and xpath should create result")
        void ofWithElementsAndXpathShouldCreateResult() {
            XmlDocument doc = XmlDocument.parse("<root><item/><item/></root>");
            List<XmlElement> elements = doc.getRoot().getChildren("item");

            XPathResult result = XPathResult.of(elements, "/root/item");

            assertThat(result.elements()).hasSize(2);
            assertThat(result.xpath()).isEqualTo("/root/item");
        }
    }

    @Nested
    @DisplayName("AsString Tests")
    class AsStringTests {

        @Test
        @DisplayName("asString should return value as string")
        void asStringShouldReturnValueAsString() {
            XPathResult result = XPathResult.of("test", "/path");

            assertThat(result.asString()).isEqualTo("test");
        }

        @Test
        @DisplayName("asString should return first element text")
        void asStringShouldReturnFirstElementText() {
            XmlDocument doc = XmlDocument.parse("<root><item>content</item></root>");
            List<XmlElement> elements = doc.getRoot().getChildren("item");

            XPathResult result = XPathResult.of(elements, "/path");

            assertThat(result.asString()).isEqualTo("content");
        }

        @Test
        @DisplayName("asString should return null for empty result")
        void asStringShouldReturnNullForEmptyResult() {
            XPathResult result = XPathResult.of(List.of(), "/path");

            assertThat(result.asString()).isNull();
        }

        @Test
        @DisplayName("asOptionalString should return optional")
        void asOptionalStringShouldReturnOptional() {
            XPathResult result = XPathResult.of("value", "/path");

            assertThat(result.asOptionalString()).isPresent().contains("value");
        }
    }

    @Nested
    @DisplayName("AsInt Tests")
    class AsIntTests {

        @Test
        @DisplayName("asInt should parse integer value")
        void asIntShouldParseIntegerValue() {
            XPathResult result = XPathResult.of("42", "/path");

            assertThat(result.asInt()).isEqualTo(42);
        }

        @Test
        @DisplayName("asInt should return null for null value")
        void asIntShouldReturnNullForNullValue() {
            XPathResult result = XPathResult.of(List.of(), "/path");

            assertThat(result.asInt()).isNull();
        }

        @Test
        @DisplayName("asOptionalInt should return optional")
        void asOptionalIntShouldReturnOptional() {
            XPathResult result = XPathResult.of("42", "/path");

            assertThat(result.asOptionalInt()).isPresent().contains(42);
        }
    }

    @Nested
    @DisplayName("AsLong Tests")
    class AsLongTests {

        @Test
        @DisplayName("asLong should parse long value")
        void asLongShouldParseLongValue() {
            XPathResult result = XPathResult.of("9999999999", "/path");

            assertThat(result.asLong()).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("asLong should return null for null value")
        void asLongShouldReturnNullForNullValue() {
            XPathResult result = XPathResult.of(List.of(), "/path");

            assertThat(result.asLong()).isNull();
        }
    }

    @Nested
    @DisplayName("AsDouble Tests")
    class AsDoubleTests {

        @Test
        @DisplayName("asDouble should parse double value")
        void asDoubleShouldParseDoubleValue() {
            XPathResult result = XPathResult.of("3.14", "/path");

            assertThat(result.asDouble()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("asDouble should return number value")
        void asDoubleShouldReturnNumberValue() {
            XPathResult result = XPathResult.of(3.14159, "/path");

            assertThat(result.asDouble()).isEqualTo(3.14159);
        }

        @Test
        @DisplayName("asDouble should return null for null value")
        void asDoubleShouldReturnNullForNullValue() {
            XPathResult result = XPathResult.of(List.of(), "/path");

            assertThat(result.asDouble()).isNull();
        }
    }

    @Nested
    @DisplayName("AsBoolean Tests")
    class AsBooleanTests {

        @Test
        @DisplayName("asBoolean should return boolean value")
        void asBooleanShouldReturnBooleanValue() {
            XPathResult result = XPathResult.of(true, "/path");

            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("asBoolean should parse string true")
        void asBooleanShouldParseStringTrue() {
            XPathResult result = XPathResult.of("true", "/path");

            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("asBoolean should return null for null value")
        void asBooleanShouldReturnNullForNullValue() {
            XPathResult result = XPathResult.of(List.of(), "/path");

            assertThat(result.asBoolean()).isNull();
        }
    }

    @Nested
    @DisplayName("AsElement Tests")
    class AsElementTests {

        @Test
        @DisplayName("asElement should return first element")
        void asElementShouldReturnFirstElement() {
            XmlDocument doc = XmlDocument.parse("<root><item/></root>");
            List<XmlElement> elements = doc.getRoot().getChildren("item");

            XPathResult result = XPathResult.of(elements, "/path");

            assertThat(result.asElement()).isPresent();
            assertThat(result.asElement().get().getName()).isEqualTo("item");
        }

        @Test
        @DisplayName("asElement should return empty for no elements")
        void asElementShouldReturnEmptyForNoElements() {
            XPathResult result = XPathResult.of(List.of(), "/path");

            assertThat(result.asElement()).isEmpty();
        }
    }

    @Nested
    @DisplayName("AsElements Tests")
    class AsElementsTests {

        @Test
        @DisplayName("asElements should return all elements")
        void asElementsShouldReturnAllElements() {
            XmlDocument doc = XmlDocument.parse("<root><item/><item/></root>");
            List<XmlElement> elements = doc.getRoot().getChildren("item");

            XPathResult result = XPathResult.of(elements, "/path");

            assertThat(result.asElements()).hasSize(2);
        }

        @Test
        @DisplayName("asElements should return empty list for no elements")
        void asElementsShouldReturnEmptyListForNoElements() {
            XPathResult result = XPathResult.of("value", "/path");

            assertThat(result.asElements()).isEmpty();
        }
    }

    @Nested
    @DisplayName("IsEmpty Tests")
    class IsEmptyTests {

        @Test
        @DisplayName("isEmpty should return true for empty result")
        void isEmptyShouldReturnTrueForEmptyResult() {
            XPathResult result = XPathResult.of(List.of(), "/path");

            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty should return false for result with value")
        void isEmptyShouldReturnFalseForResultWithValue() {
            XPathResult result = XPathResult.of("value", "/path");

            assertThat(result.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("isEmpty should return false for result with elements")
        void isEmptyShouldReturnFalseForResultWithElements() {
            XmlDocument doc = XmlDocument.parse("<root><item/></root>");
            List<XmlElement> elements = doc.getRoot().getChildren("item");

            XPathResult result = XPathResult.of(elements, "/path");

            assertThat(result.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("Count Tests")
    class CountTests {

        @Test
        @DisplayName("count should return number of elements")
        void countShouldReturnNumberOfElements() {
            XmlDocument doc = XmlDocument.parse("<root><item/><item/><item/></root>");
            List<XmlElement> elements = doc.getRoot().getChildren("item");

            XPathResult result = XPathResult.of(elements, "/path");

            assertThat(result.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("count should return 0 for empty elements")
        void countShouldReturn0ForEmptyElements() {
            XPathResult result = XPathResult.of(List.of(), "/path");

            assertThat(result.count()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Record Accessors Tests")
    class RecordAccessorsTests {

        @Test
        @DisplayName("value should return stored value")
        void valueShouldReturnStoredValue() {
            XPathResult result = XPathResult.of("test", "/path");

            assertThat(result.value()).isEqualTo("test");
        }

        @Test
        @DisplayName("elements should return stored elements")
        void elementsShouldReturnStoredElements() {
            XmlDocument doc = XmlDocument.parse("<root><item/></root>");
            List<XmlElement> elements = doc.getRoot().getChildren("item");

            XPathResult result = XPathResult.of(elements, "/path");

            assertThat(result.elements()).isEqualTo(elements);
        }

        @Test
        @DisplayName("xpath should return stored xpath")
        void xpathShouldReturnStoredXpath() {
            XPathResult result = XPathResult.of("value", "/root/child");

            assertThat(result.xpath()).isEqualTo("/root/child");
        }
    }
}
