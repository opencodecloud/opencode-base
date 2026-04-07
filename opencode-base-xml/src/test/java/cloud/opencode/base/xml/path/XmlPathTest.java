package cloud.opencode.base.xml.path;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.XmlElement;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlPath Tests
 * XmlPath 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
@DisplayName("XmlPath Tests")
class XmlPathTest {

    private static final String SAMPLE_XML = """
        <config>
            <db>
                <host>localhost</host>
                <port>5432</port>
                <enabled>true</enabled>
                <name attr1="val1">mydb</name>
            </db>
            <users>
                <user id="1">
                    <name>Alice</name>
                    <email>alice@example.com</email>
                </user>
                <user id="2">
                    <name>Bob</name>
                    <email>bob@example.com</email>
                </user>
                <user id="3">
                    <name>Charlie</name>
                    <email>charlie@example.com</email>
                </user>
            </users>
            <items>
                <item>apple</item>
                <item>banana</item>
                <item>cherry</item>
            </items>
        </config>
        """;

    private XmlDocument doc;

    @BeforeEach
    void setUp() {
        doc = XmlDocument.parse(SAMPLE_XML);
    }

    @Nested
    @DisplayName("getString Tests")
    class GetStringTests {

        @Test
        @DisplayName("should get simple path value")
        void shouldGetSimplePathValue() {
            String value = XmlPath.getString(doc, "config.db.host");

            assertThat(value).isEqualTo("localhost");
        }

        @Test
        @DisplayName("should get nested path value")
        void shouldGetNestedPathValue() {
            String value = XmlPath.getString(doc, "config.db.port");

            assertThat(value).isEqualTo("5432");
        }

        @Test
        @DisplayName("should return null for non-existent path")
        void shouldReturnNullForNonExistentPath() {
            String value = XmlPath.getString(doc, "config.db.password");

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("should get value from element")
        void shouldGetValueFromElement() {
            XmlElement root = doc.getRoot();
            String value = XmlPath.getString(root, "db.host");

            assertThat(value).isEqualTo("localhost");
        }

        @Test
        @DisplayName("should get value with default")
        void shouldGetValueWithDefault() {
            String value = XmlPath.getString(doc, "config.db.password", "secret");

            assertThat(value).isEqualTo("secret");
        }

        @Test
        @DisplayName("should return actual value when not null even with default")
        void shouldReturnActualValueWithDefault() {
            String value = XmlPath.getString(doc, "config.db.host", "fallback");

            assertThat(value).isEqualTo("localhost");
        }
    }

    @Nested
    @DisplayName("getOptional Tests")
    class GetOptionalTests {

        @Test
        @DisplayName("should return present optional for existing path")
        void shouldReturnPresentOptional() {
            Optional<String> value = XmlPath.getOptional(doc, "config.db.host");

            assertThat(value).isPresent().hasValue("localhost");
        }

        @Test
        @DisplayName("should return empty optional for non-existent path")
        void shouldReturnEmptyOptional() {
            Optional<String> value = XmlPath.getOptional(doc, "config.db.missing");

            assertThat(value).isEmpty();
        }
    }

    @Nested
    @DisplayName("Typed Getter Tests")
    class TypedGetterTests {

        @Test
        @DisplayName("getInt should return parsed integer")
        void getIntShouldReturnParsedInteger() {
            int value = XmlPath.getInt(doc, "config.db.port", 0);

            assertThat(value).isEqualTo(5432);
        }

        @Test
        @DisplayName("getInt should return default for non-existent path")
        void getIntShouldReturnDefaultForMissing() {
            int value = XmlPath.getInt(doc, "config.db.timeout", 30);

            assertThat(value).isEqualTo(30);
        }

        @Test
        @DisplayName("getInt should return default for non-numeric value")
        void getIntShouldReturnDefaultForNonNumeric() {
            int value = XmlPath.getInt(doc, "config.db.host", 0);

            assertThat(value).isEqualTo(0);
        }

        @Test
        @DisplayName("getBoolean should return parsed boolean")
        void getBooleanShouldReturnParsedBoolean() {
            boolean value = XmlPath.getBoolean(doc, "config.db.enabled", false);

            assertThat(value).isTrue();
        }

        @Test
        @DisplayName("getBoolean should return default for non-existent path")
        void getBooleanShouldReturnDefaultForMissing() {
            boolean value = XmlPath.getBoolean(doc, "config.db.debug", true);

            assertThat(value).isTrue();
        }
    }

    @Nested
    @DisplayName("Index Access Tests")
    class IndexAccessTests {

        @Test
        @DisplayName("should access element by index")
        void shouldAccessElementByIndex() {
            String value = XmlPath.getString(doc, "config.users.user[1].name");

            assertThat(value).isEqualTo("Bob");
        }

        @Test
        @DisplayName("should access first element by index 0")
        void shouldAccessFirstElement() {
            String value = XmlPath.getString(doc, "config.users.user[0].name");

            assertThat(value).isEqualTo("Alice");
        }

        @Test
        @DisplayName("should access last element by index")
        void shouldAccessLastElement() {
            String value = XmlPath.getString(doc, "config.users.user[2].name");

            assertThat(value).isEqualTo("Charlie");
        }

        @Test
        @DisplayName("should return null for out-of-range index")
        void shouldReturnNullForOutOfRange() {
            String value = XmlPath.getString(doc, "config.users.user[99].name");

            assertThat(value).isNull();
        }
    }

    @Nested
    @DisplayName("Attribute Access Tests")
    class AttributeAccessTests {

        @Test
        @DisplayName("should access attribute via @ notation")
        void shouldAccessAttributeViaAtNotation() {
            String value = XmlPath.getAttribute(doc, "config.users.user[0].@id");

            assertThat(value).isEqualTo("1");
        }

        @Test
        @DisplayName("should access attribute on nested element")
        void shouldAccessAttributeOnNestedElement() {
            String value = XmlPath.getString(doc, "config.db.name.@attr1");

            assertThat(value).isEqualTo("val1");
        }

        @Test
        @DisplayName("should return null for non-existent attribute")
        void shouldReturnNullForNonExistentAttribute() {
            String value = XmlPath.getAttribute(doc, "config.db.host.@missing");

            assertThat(value).isNull();
        }
    }

    @Nested
    @DisplayName("getElements Tests")
    class GetElementsTests {

        @Test
        @DisplayName("should get list of matching elements")
        void shouldGetListOfMatchingElements() {
            List<XmlElement> elements = XmlPath.getElements(doc, "config.users.user");

            assertThat(elements).hasSize(3);
        }

        @Test
        @DisplayName("should get list of items")
        void shouldGetListOfItems() {
            List<XmlElement> elements = XmlPath.getElements(doc, "config.items.item");

            assertThat(elements).hasSize(3);
        }

        @Test
        @DisplayName("should return empty list for non-existent path")
        void shouldReturnEmptyListForNonExistent() {
            List<XmlElement> elements = XmlPath.getElements(doc, "config.missing.item");

            assertThat(elements).isEmpty();
        }
    }

    @Nested
    @DisplayName("getStrings Tests")
    class GetStringsTests {

        @Test
        @DisplayName("should get list of text values")
        void shouldGetListOfTextValues() {
            List<String> values = XmlPath.getStrings(doc, "config.items.item");

            assertThat(values).containsExactly("apple", "banana", "cherry");
        }

        @Test
        @DisplayName("should return empty list for non-existent path")
        void shouldReturnEmptyListForNonExistent() {
            List<String> values = XmlPath.getStrings(doc, "config.missing.item");

            assertThat(values).isEmpty();
        }
    }

    @Nested
    @DisplayName("getElement Tests")
    class GetElementTests {

        @Test
        @DisplayName("should get element at path")
        void shouldGetElementAtPath() {
            XmlElement element = XmlPath.getElement(doc, "config.db.host");

            assertThat(element).isNotNull();
            assertThat(element.getText()).isEqualTo("localhost");
        }

        @Test
        @DisplayName("should return null for non-existent element")
        void shouldReturnNullForNonExistent() {
            XmlElement element = XmlPath.getElement(doc, "config.db.missing");

            assertThat(element).isNull();
        }
    }

    @Nested
    @DisplayName("exists Tests")
    class ExistsTests {

        @Test
        @DisplayName("should return true for existing path")
        void shouldReturnTrueForExistingPath() {
            boolean result = XmlPath.exists(doc, "config.db.host");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false for non-existent path")
        void shouldReturnFalseForNonExistentPath() {
            boolean result = XmlPath.exists(doc, "config.db.missing");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return true for existing attribute")
        void shouldReturnTrueForExistingAttribute() {
            boolean result = XmlPath.exists(doc, "config.users.user[0].@id");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false for non-existent attribute")
        void shouldReturnFalseForNonExistentAttribute() {
            boolean result = XmlPath.exists(doc, "config.db.host.@nonexistent");

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("set Tests")
    class SetTests {

        @Test
        @DisplayName("should set value at existing path")
        void shouldSetValueAtExistingPath() {
            XmlPath.set(doc, "config.db.host", "192.168.1.1");

            assertThat(XmlPath.getString(doc, "config.db.host")).isEqualTo("192.168.1.1");
        }

        @Test
        @DisplayName("should create intermediate elements")
        void shouldCreateIntermediateElements() {
            XmlPath.set(doc, "config.cache.redis.host", "redis.local");

            assertThat(XmlPath.getString(doc, "config.cache.redis.host")).isEqualTo("redis.local");
        }

        @Test
        @DisplayName("should set attribute value")
        void shouldSetAttributeValue() {
            XmlPath.set(doc, "config.db.@version", "14");

            assertThat(XmlPath.getString(doc, "config.db.@version")).isEqualTo("14");
        }

        @Test
        @DisplayName("should create new element at leaf")
        void shouldCreateNewElementAtLeaf() {
            XmlPath.set(doc, "config.db.schema", "public");

            assertThat(XmlPath.getString(doc, "config.db.schema")).isEqualTo("public");
        }
    }

    @Nested
    @DisplayName("Null Safety Tests")
    class NullSafetyTests {

        @Test
        @DisplayName("getString should throw on null document")
        void getStringShouldThrowOnNullDoc() {
            assertThatNullPointerException()
                .isThrownBy(() -> XmlPath.getString((XmlDocument) null, "path"));
        }

        @Test
        @DisplayName("getString should throw on null path")
        void getStringShouldThrowOnNullPath() {
            assertThatNullPointerException()
                .isThrownBy(() -> XmlPath.getString(doc, null));
        }

        @Test
        @DisplayName("exists should throw on null document")
        void existsShouldThrowOnNullDoc() {
            assertThatNullPointerException()
                .isThrownBy(() -> XmlPath.exists(null, "path"));
        }

        @Test
        @DisplayName("set should throw on null document")
        void setShouldThrowOnNullDoc() {
            assertThatNullPointerException()
                .isThrownBy(() -> XmlPath.set(null, "path", "value"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle document with no root")
        void shouldHandleNoRoot() {
            // A document parsed from valid XML always has a root,
            // but getOptional on a missing path should return empty
            assertThat(XmlPath.getOptional(doc, "nonexistent.path")).isEmpty();
        }

        @Test
        @DisplayName("should handle single segment path")
        void shouldHandleSingleSegmentPath() {
            // Single segment "config" refers to the root itself
            XmlElement element = XmlPath.getElement(doc, "config");

            assertThat(element).isNotNull();
            assertThat(element.getName()).isEqualTo("config");
        }
    }
}
