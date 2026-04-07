package cloud.opencode.base.xml.bind;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.bind.annotation.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Record Binder Tests - Tests for record binding support in XmlBinder
 * 记录绑定器测试 - XmlBinder 中记录绑定支持的测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
@DisplayName("Record Binder Tests")
class RecordBinderTest {

    // ==================== Test Records | 测试记录 ====================

    @XmlRoot("person")
    record SimplePerson(String name, int age) {
    }

    @XmlRoot("user")
    record UserWithAttribute(
        @XmlAttribute("id") int id,
        String name,
        String email
    ) {
    }

    @XmlRoot("item")
    record ItemWithElementMapping(
        @XmlElement("item-name") String name,
        @XmlElement("item-price") String price
    ) {
    }

    record Address(String city, String zip) {
    }

    @XmlRoot("employee")
    record EmployeeWithNested(String name, Address address) {
    }

    @XmlRoot("team")
    record TeamWithList(
        String name,
        @XmlElementList(value = "members", itemName = "member") List<String> members
    ) {
    }

    @XmlRoot("entry")
    record EntryWithValue(
        @XmlAttribute("key") String key,
        @XmlValue String content
    ) {
    }

    @XmlRoot("config")
    record ConfigRecord(String host, int port, boolean enabled) {
    }

    @XmlRoot("data")
    record RecordWithIgnore(
        String name,
        @XmlIgnore String secret
    ) {
    }

    record NestedAddress(String street, String city) {
    }

    record NestedPerson(String name, NestedAddress address) {
    }

    @XmlRoot("company")
    record CompanyWithNestedRecords(
        String name,
        @XmlElementList(value = "employees", itemName = "emp") List<SimplePerson> employees
    ) {
    }

    // ==================== Tests | 测试 ====================

    @Nested
    @DisplayName("Simple Record Tests")
    class SimpleRecordTests {

        @Test
        @DisplayName("should unmarshal simple record with string and int fields")
        void shouldUnmarshalSimpleRecord() {
            String xml = "<person><name>Alice</name><age>30</age></person>";

            SimplePerson person = XmlBinder.create().unmarshal(xml, SimplePerson.class);

            assertThat(person.name()).isEqualTo("Alice");
            assertThat(person.age()).isEqualTo(30);
        }

        @Test
        @DisplayName("should handle missing fields with defaults")
        void shouldHandleMissingFieldsWithDefaults() {
            String xml = "<person><name>Bob</name></person>";

            SimplePerson person = XmlBinder.create().unmarshal(xml, SimplePerson.class);

            assertThat(person.name()).isEqualTo("Bob");
            assertThat(person.age()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Attribute Record Tests")
    class AttributeRecordTests {

        @Test
        @DisplayName("should unmarshal record with @XmlAttribute")
        void shouldUnmarshalRecordWithAttribute() {
            String xml = "<user id=\"42\"><name>Charlie</name><email>c@test.com</email></user>";

            UserWithAttribute user = XmlBinder.create().unmarshal(xml, UserWithAttribute.class);

            assertThat(user.id()).isEqualTo(42);
            assertThat(user.name()).isEqualTo("Charlie");
            assertThat(user.email()).isEqualTo("c@test.com");
        }

        @Test
        @DisplayName("should handle missing attribute with default")
        void shouldHandleMissingAttribute() {
            String xml = "<user><name>Dave</name><email>d@test.com</email></user>";

            UserWithAttribute user = XmlBinder.create().unmarshal(xml, UserWithAttribute.class);

            assertThat(user.id()).isEqualTo(0);
            assertThat(user.name()).isEqualTo("Dave");
        }
    }

    @Nested
    @DisplayName("Element Mapping Tests")
    class ElementMappingTests {

        @Test
        @DisplayName("should unmarshal record with @XmlElement name mapping")
        void shouldUnmarshalWithElementMapping() {
            String xml = "<item><item-name>Widget</item-name><item-price>9.99</item-price></item>";

            ItemWithElementMapping item = XmlBinder.create().unmarshal(xml, ItemWithElementMapping.class);

            assertThat(item.name()).isEqualTo("Widget");
            assertThat(item.price()).isEqualTo("9.99");
        }
    }

    @Nested
    @DisplayName("Nested Record Tests")
    class NestedRecordTests {

        @Test
        @DisplayName("should unmarshal record with nested record")
        void shouldUnmarshalNestedRecord() {
            String xml = """
                <employee>
                    <name>Eve</name>
                    <address>
                        <city>Portland</city>
                        <zip>97201</zip>
                    </address>
                </employee>
                """;

            EmployeeWithNested emp = XmlBinder.create().unmarshal(xml, EmployeeWithNested.class);

            assertThat(emp.name()).isEqualTo("Eve");
            assertThat(emp.address()).isNotNull();
            assertThat(emp.address().city()).isEqualTo("Portland");
            assertThat(emp.address().zip()).isEqualTo("97201");
        }

        @Test
        @DisplayName("should handle missing nested record")
        void shouldHandleMissingNestedRecord() {
            String xml = "<employee><name>Frank</name></employee>";

            EmployeeWithNested emp = XmlBinder.create().unmarshal(xml, EmployeeWithNested.class);

            assertThat(emp.name()).isEqualTo("Frank");
            assertThat(emp.address()).isNull();
        }
    }

    @Nested
    @DisplayName("List Record Tests")
    class ListRecordTests {

        @Test
        @DisplayName("should unmarshal record with List field")
        void shouldUnmarshalRecordWithList() {
            String xml = """
                <team>
                    <name>Alpha</name>
                    <members>
                        <member>Alice</member>
                        <member>Bob</member>
                        <member>Charlie</member>
                    </members>
                </team>
                """;

            TeamWithList team = XmlBinder.create().unmarshal(xml, TeamWithList.class);

            assertThat(team.name()).isEqualTo("Alpha");
            assertThat(team.members()).containsExactly("Alice", "Bob", "Charlie");
        }

        @Test
        @DisplayName("should handle empty list")
        void shouldHandleEmptyList() {
            String xml = "<team><name>Beta</name><members/></team>";

            TeamWithList team = XmlBinder.create().unmarshal(xml, TeamWithList.class);

            assertThat(team.name()).isEqualTo("Beta");
            assertThat(team.members()).isEmpty();
        }

        @Test
        @DisplayName("should unmarshal record with list of nested records")
        void shouldUnmarshalListOfNestedRecords() {
            String xml = """
                <company>
                    <name>Acme</name>
                    <employees>
                        <emp><name>Alice</name><age>30</age></emp>
                        <emp><name>Bob</name><age>25</age></emp>
                    </employees>
                </company>
                """;

            CompanyWithNestedRecords company = XmlBinder.create()
                .unmarshal(xml, CompanyWithNestedRecords.class);

            assertThat(company.name()).isEqualTo("Acme");
            assertThat(company.employees()).hasSize(2);
            assertThat(company.employees().get(0).name()).isEqualTo("Alice");
            assertThat(company.employees().get(0).age()).isEqualTo(30);
            assertThat(company.employees().get(1).name()).isEqualTo("Bob");
        }
    }

    @Nested
    @DisplayName("XmlValue Record Tests")
    class XmlValueRecordTests {

        @Test
        @DisplayName("should unmarshal record with @XmlValue")
        void shouldUnmarshalRecordWithXmlValue() {
            String xml = "<entry key=\"name\">John Doe</entry>";

            EntryWithValue entry = XmlBinder.create().unmarshal(xml, EntryWithValue.class);

            assertThat(entry.key()).isEqualTo("name");
            assertThat(entry.content()).isEqualTo("John Doe");
        }
    }

    @Nested
    @DisplayName("Default Value Tests")
    class DefaultValueTests {

        @Test
        @DisplayName("should use component name for non-annotated fields")
        void shouldUseComponentNameByDefault() {
            String xml = "<config><host>localhost</host><port>8080</port><enabled>true</enabled></config>";

            ConfigRecord config = XmlBinder.create().unmarshal(xml, ConfigRecord.class);

            assertThat(config.host()).isEqualTo("localhost");
            assertThat(config.port()).isEqualTo(8080);
            assertThat(config.enabled()).isTrue();
        }

        @Test
        @DisplayName("should use primitive defaults for missing elements")
        void shouldUsePrimitiveDefaults() {
            String xml = "<config><host>localhost</host></config>";

            ConfigRecord config = XmlBinder.create().unmarshal(xml, ConfigRecord.class);

            assertThat(config.host()).isEqualTo("localhost");
            assertThat(config.port()).isEqualTo(0);
            assertThat(config.enabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("XmlIgnore Record Tests")
    class XmlIgnoreRecordTests {

        @Test
        @DisplayName("should ignore field marked with @XmlIgnore")
        void shouldIgnoreFieldWithXmlIgnore() {
            String xml = "<data><name>visible</name><secret>hidden</secret></data>";

            RecordWithIgnore data = XmlBinder.create().unmarshal(xml, RecordWithIgnore.class);

            assertThat(data.name()).isEqualTo("visible");
            assertThat(data.secret()).isNull();
        }
    }

    @Nested
    @DisplayName("Marshal Record Tests")
    class MarshalRecordTests {

        @Test
        @DisplayName("should marshal simple record to XML")
        void shouldMarshalSimpleRecord() {
            SimplePerson person = new SimplePerson("Alice", 30);

            String xml = XmlBinder.create().marshal(person);

            XmlDocument doc = XmlDocument.parse(xml);
            assertThat(doc.getRoot().getName()).isEqualTo("person");
            assertThat(doc.getRoot().getChildText("name")).isEqualTo("Alice");
            assertThat(doc.getRoot().getChildText("age")).isEqualTo("30");
        }

        @Test
        @DisplayName("should marshal record with attribute")
        void shouldMarshalRecordWithAttribute() {
            UserWithAttribute user = new UserWithAttribute(42, "Charlie", "c@test.com");

            String xml = XmlBinder.create().marshal(user);

            XmlDocument doc = XmlDocument.parse(xml);
            assertThat(doc.getRoot().getAttribute("id")).isEqualTo("42");
            assertThat(doc.getRoot().getChildText("name")).isEqualTo("Charlie");
        }
    }

    @Nested
    @DisplayName("Round-Trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("should survive unmarshal-marshal round trip")
        void shouldSurviveRoundTrip() {
            String originalXml = "<person><name>Alice</name><age>30</age></person>";

            XmlBinder binder = XmlBinder.create();
            SimplePerson person = binder.unmarshal(originalXml, SimplePerson.class);
            String marshaledXml = binder.marshal(person);
            SimplePerson roundTripped = binder.unmarshal(marshaledXml, SimplePerson.class);

            assertThat(roundTripped.name()).isEqualTo(person.name());
            assertThat(roundTripped.age()).isEqualTo(person.age());
        }
    }
}
