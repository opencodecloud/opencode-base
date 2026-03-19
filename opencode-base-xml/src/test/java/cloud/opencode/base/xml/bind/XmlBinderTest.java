package cloud.opencode.base.xml.bind;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.bind.adapter.XmlAdapter;
import cloud.opencode.base.xml.bind.annotation.*;
import cloud.opencode.base.xml.exception.XmlBindException;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlBinderTest Tests
 * XmlBinderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlBinder Tests")
class XmlBinderTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("create should create new binder")
        void createShouldCreateNewBinder() {
            XmlBinder binder = XmlBinder.create();

            assertThat(binder).isNotNull();
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("formatted should enable formatted output")
        void formattedShouldEnableFormattedOutput() {
            XmlBinder binder = XmlBinder.create().formatted(true);

            assertThat(binder).isNotNull();
        }

        @Test
        @DisplayName("encoding should set output encoding")
        void encodingShouldSetOutputEncoding() {
            XmlBinder binder = XmlBinder.create().encoding("UTF-16");

            assertThat(binder).isNotNull();
        }

        @Test
        @DisplayName("indent should set indentation level")
        void indentShouldSetIndentationLevel() {
            XmlBinder binder = XmlBinder.create().indent(2);

            assertThat(binder).isNotNull();
        }
    }

    @Nested
    @DisplayName("Unmarshal Tests")
    class UnmarshalTests {

        @Test
        @DisplayName("unmarshal should convert XML to object")
        void unmarshalShouldConvertXmlToObject() {
            String xml = "<person><name>John</name><age>30</age></person>";

            Person person = XmlBinder.create().unmarshal(xml, Person.class);

            assertThat(person.name).isEqualTo("John");
            assertThat(person.age).isEqualTo(30);
        }

        @Test
        @DisplayName("unmarshal should handle XmlRoot annotation")
        void unmarshalShouldHandleXmlRootAnnotation() {
            String xml = "<employee><name>Jane</name></employee>";

            Employee employee = XmlBinder.create().unmarshal(xml, Employee.class);

            assertThat(employee.name).isEqualTo("Jane");
        }

        @Test
        @DisplayName("unmarshal should handle XmlAttribute annotation")
        void unmarshalShouldHandleXmlAttributeAnnotation() {
            String xml = "<item id=\"123\"><name>Widget</name></item>";

            Item item = XmlBinder.create().unmarshal(xml, Item.class);

            assertThat(item.id).isEqualTo(123L);
            assertThat(item.name).isEqualTo("Widget");
        }

        @Test
        @DisplayName("unmarshal should handle XmlValue annotation")
        void unmarshalShouldHandleXmlValueAnnotation() {
            String xml = "<tag id=\"1\">content here</tag>";

            Tag tag = XmlBinder.create().unmarshal(xml, Tag.class);

            assertThat(tag.id).isEqualTo(1L);
            assertThat(tag.content).isEqualTo("content here");
        }

        @Test
        @DisplayName("unmarshal should handle XmlIgnore annotation")
        void unmarshalShouldHandleXmlIgnoreAnnotation() {
            String xml = "<user><name>Test</name><password>secret</password></user>";

            User user = XmlBinder.create().unmarshal(xml, User.class);

            assertThat(user.name).isEqualTo("Test");
            assertThat(user.password).isNull();
        }

        @Test
        @DisplayName("unmarshal should handle XmlElementList annotation")
        void unmarshalShouldHandleXmlElementListAnnotation() {
            String xml = """
                <catalog>
                    <items>
                        <item>one</item>
                        <item>two</item>
                        <item>three</item>
                    </items>
                </catalog>
                """;

            Catalog catalog = XmlBinder.create().unmarshal(xml, Catalog.class);

            assertThat(catalog.items).containsExactly("one", "two", "three");
        }

        @Test
        @DisplayName("unmarshal from XmlDocument should work")
        void unmarshalFromXmlDocumentShouldWork() {
            XmlDocument doc = XmlDocument.parse("<person><name>Test</name><age>25</age></person>");

            Person person = XmlBinder.create().unmarshal(doc, Person.class);

            assertThat(person.name).isEqualTo("Test");
        }

        @Test
        @DisplayName("unmarshal should handle mismatched XML gracefully")
        void unmarshalShouldHandleMismatchedXmlGracefully() {
            // XML without matching fields should still unmarshal without exception
            // The object will just have default/null field values
            Person person = XmlBinder.create().unmarshal("<invalid></invalid>", Person.class);

            // Fields will be null since XML structure doesn't match
            assertThat(person.name).isNull();
        }
    }

    @Nested
    @DisplayName("Marshal Tests")
    class MarshalTests {

        @Test
        @DisplayName("marshal should convert object to XML")
        void marshalShouldConvertObjectToXml() {
            Person person = new Person();
            person.name = "John";
            person.age = 30;

            String xml = XmlBinder.create().marshal(person);

            assertThat(xml).contains("<name>John</name>");
            assertThat(xml).contains("<age>30</age>");
        }

        @Test
        @DisplayName("marshal should handle XmlRoot annotation")
        void marshalShouldHandleXmlRootAnnotation() {
            Employee employee = new Employee();
            employee.name = "Jane";

            String xml = XmlBinder.create().marshal(employee);

            assertThat(xml).contains("<employee>");
        }

        @Test
        @DisplayName("marshal should handle XmlAttribute annotation")
        void marshalShouldHandleXmlAttributeAnnotation() {
            Item item = new Item();
            item.id = 123L;
            item.name = "Widget";

            String xml = XmlBinder.create().marshal(item);

            assertThat(xml).contains("id=\"123\"");
        }

        @Test
        @DisplayName("marshal should handle XmlIgnore annotation")
        void marshalShouldHandleXmlIgnoreAnnotation() {
            User user = new User();
            user.name = "Test";
            user.password = "secret";

            String xml = XmlBinder.create().marshal(user);

            assertThat(xml).contains("<name>Test</name>");
            assertThat(xml).doesNotContain("secret");
        }

        @Test
        @DisplayName("marshal should handle XmlElementList annotation")
        void marshalShouldHandleXmlElementListAnnotation() {
            Catalog catalog = new Catalog();
            catalog.items = List.of("one", "two", "three");

            String xml = XmlBinder.create().marshal(catalog);

            assertThat(xml).contains("<items>");
            assertThat(xml).contains("<item>one</item>");
        }

        @Test
        @DisplayName("marshal with formatting should indent output")
        void marshalWithFormattingShouldIndentOutput() {
            Person person = new Person();
            person.name = "John";

            String xml = XmlBinder.create().formatted(true).marshal(person);

            assertThat(xml).contains("\n");
        }

        @Test
        @DisplayName("marshal should throw for null object")
        void marshalShouldThrowForNullObject() {
            assertThatThrownBy(() -> XmlBinder.create().marshal(null))
                .isInstanceOf(XmlBindException.class);
        }

        @Test
        @DisplayName("marshalToDocument should return XmlDocument")
        void marshalToDocumentShouldReturnXmlDocument() {
            Person person = new Person();
            person.name = "John";

            XmlDocument doc = XmlBinder.create().marshalToDocument(person);

            assertThat(doc).isNotNull();
            assertThat(doc.xpath("//name")).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("Adapter Tests")
    class AdapterTests {

        @Test
        @DisplayName("registerAdapter should register custom adapter")
        void registerAdapterShouldRegisterCustomAdapter() {
            XmlAdapter<String, CustomType> adapter = new XmlAdapter<>() {
                @Override
                public CustomType unmarshal(String value) {
                    return new CustomType(value);
                }

                @Override
                public String marshal(CustomType value) {
                    return value.value();
                }

                @Override
                public Class<CustomType> getBoundType() {
                    return CustomType.class;
                }
            };

            XmlBinder binder = XmlBinder.create().registerAdapter(adapter);

            assertThat(binder).isNotNull();
        }
    }

    @Nested
    @DisplayName("Nested Object Tests")
    class NestedObjectTests {

        @Test
        @DisplayName("should handle nested objects")
        void shouldHandleNestedObjects() {
            String xml = """
                <order>
                    <customer>
                        <name>John</name>
                    </customer>
                </order>
                """;

            Order order = XmlBinder.create().unmarshal(xml, Order.class);

            assertThat(order.customer).isNotNull();
            assertThat(order.customer.name).isEqualTo("John");
        }
    }

    // Test classes
    public static class Person {
        public String name;
        public int age;
    }

    @XmlRoot("employee")
    public static class Employee {
        public String name;
    }

    public static class Item {
        @XmlAttribute
        public Long id;
        public String name;
    }

    public static class Tag {
        @XmlAttribute
        public Long id;
        @XmlValue
        public String content;
    }

    public static class User {
        public String name;
        @XmlIgnore
        public String password;
    }

    public static class Catalog {
        @XmlElementList(value = "items", itemName = "item")
        public List<String> items;
    }

    public static class Order {
        public Customer customer;
    }

    public static class Customer {
        public String name;
    }

    public record CustomType(String value) {}
}
