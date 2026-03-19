package cloud.opencode.base.xml.builder;

import cloud.opencode.base.xml.XmlElement;
import org.junit.jupiter.api.*;
import org.w3c.dom.Element;

import static org.assertj.core.api.Assertions.*;

/**
 * ElementBuilderTest Tests
 * ElementBuilderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("ElementBuilder Tests")
class ElementBuilderTest {

    @Nested
    @DisplayName("Create Tests")
    class CreateTests {

        @Test
        @DisplayName("create should create builder with element name")
        void createShouldCreateBuilderWithElementName() {
            XmlElement element = ElementBuilder.create("test").build();

            assertThat(element.getName()).isEqualTo("test");
        }

        @Test
        @DisplayName("create with namespace should create namespaced element")
        void createWithNamespaceShouldCreateNamespacedElement() {
            XmlElement element = ElementBuilder.create("http://example.com", "test").build();

            assertThat(element.getName()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("Attribute Tests")
    class AttributeTests {

        @Test
        @DisplayName("attribute should add attribute")
        void attributeShouldAddAttribute() {
            XmlElement element = ElementBuilder.create("test")
                .attribute("id", "123")
                .build();

            assertThat(element.getAttribute("id")).isEqualTo("123");
        }

        @Test
        @DisplayName("multiple attributes should work")
        void multipleAttributesShouldWork() {
            XmlElement element = ElementBuilder.create("test")
                .attribute("id", "123")
                .attribute("name", "test")
                .attribute("type", "example")
                .build();

            assertThat(element.getAttribute("id")).isEqualTo("123");
            assertThat(element.getAttribute("name")).isEqualTo("test");
            assertThat(element.getAttribute("type")).isEqualTo("example");
        }

        @Test
        @DisplayName("attribute with number should work")
        void attributeWithNumberShouldWork() {
            XmlElement element = ElementBuilder.create("test")
                .attribute("count", 42)
                .build();

            assertThat(element.getAttribute("count")).isEqualTo("42");
        }
    }

    @Nested
    @DisplayName("Child Element Tests")
    class ChildElementTests {

        @Test
        @DisplayName("emptyChild should add empty child element")
        void emptyChildShouldAddEmptyChildElement() {
            XmlElement element = ElementBuilder.create("parent")
                .emptyChild("child")
                .build();

            assertThat(element.getChild("child")).isNotNull();
        }

        @Test
        @DisplayName("child with text should add child with content")
        void childWithTextShouldAddChildWithContent() {
            XmlElement element = ElementBuilder.create("parent")
                .child("child", "text content")
                .build();

            assertThat(element.getChild("child").getText()).isEqualTo("text content");
        }

        @Test
        @DisplayName("child with XmlElement should add nested structure")
        void childWithXmlElementShouldAddNestedStructure() {
            XmlElement childElement = ElementBuilder.create("child")
                .attribute("id", "1")
                .text("content")
                .build();

            XmlElement element = ElementBuilder.create("parent")
                .child(childElement)
                .build();

            XmlElement child = element.getChild("child");
            assertThat(child.getAttribute("id")).isEqualTo("1");
            assertThat(child.getText()).isEqualTo("content");
        }

        @Test
        @DisplayName("children should add multiple children")
        void childrenShouldAddMultipleChildren() {
            XmlElement element = ElementBuilder.create("parent")
                .child("item", "one")
                .child("item", "two")
                .child("item", "three")
                .build();

            assertThat(element.getChildren("item")).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Text Content Tests")
    class TextContentTests {

        @Test
        @DisplayName("text should set text content")
        void textShouldSetTextContent() {
            XmlElement element = ElementBuilder.create("test")
                .text("hello world")
                .build();

            assertThat(element.getText()).isEqualTo("hello world");
        }

        @Test
        @DisplayName("cdata should add CDATA section")
        void cdataShouldAddCdataSection() {
            XmlElement element = ElementBuilder.create("test")
                .cdata("<html>content</html>")
                .build();

            assertThat(element.getText()).isEqualTo("<html>content</html>");
        }

        @Test
        @DisplayName("comment should add comment")
        void commentShouldAddComment() {
            XmlElement element = ElementBuilder.create("test")
                .comment("A comment")
                .build();

            assertThat(element).isNotNull();
        }
    }

    @Nested
    @DisplayName("Namespace Tests")
    class NamespaceTests {

        @Test
        @DisplayName("namespace should declare namespace")
        void namespaceShouldDeclareNamespace() {
            XmlElement element = ElementBuilder.create("test")
                .namespace("http://example.com", "ns")
                .build();

            String xml = element.toXml();
            assertThat(xml).contains("xmlns:ns=\"http://example.com\"");
        }

        @Test
        @DisplayName("defaultNamespace should set default namespace")
        void defaultNamespaceShouldSetDefaultNamespace() {
            XmlElement element = ElementBuilder.create("test")
                .defaultNamespace("http://example.com")
                .build();

            String xml = element.toXml();
            assertThat(xml).contains("xmlns=\"http://example.com\"");
        }
    }

    @Nested
    @DisplayName("Complex Structure Tests")
    class ComplexStructureTests {

        @Test
        @DisplayName("should build complex nested structure")
        void shouldBuildComplexNestedStructure() {
            XmlElement addressElement = ElementBuilder.create("address")
                .child("street", "123 Main St")
                .child("city", "Springfield")
                .child("zip", "12345")
                .build();

            XmlElement contactsElement = ElementBuilder.create("contacts")
                .child("email", "john@example.com")
                .child("phone", "555-1234")
                .build();

            XmlElement element = ElementBuilder.create("person")
                .attribute("id", "1")
                .child("name", "John Doe")
                .child(addressElement)
                .child(contactsElement)
                .build();

            assertThat(element.getAttribute("id")).isEqualTo("1");
            assertThat(element.getChild("name").getText()).isEqualTo("John Doe");
            assertThat(element.xpath("address/city")).isEqualTo("Springfield");
            assertThat(element.xpath("contacts/email")).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("should build with startChild and endChild")
        void shouldBuildWithStartChildAndEndChild() {
            XmlElement element = ElementBuilder.create("parent")
                .startChild("address")
                    .child("city", "New York")
                    .child("country", "USA")
                .endChild()
                .build();

            assertThat(element.getChild("address")).isNotNull();
            assertThat(element.xpath("address/city")).isEqualTo("New York");
        }
    }

    @Nested
    @DisplayName("Build Tests")
    class BuildTests {

        @Test
        @DisplayName("build should return XmlElement")
        void buildShouldReturnXmlElement() {
            XmlElement element = ElementBuilder.create("test").build();

            assertThat(element).isNotNull();
            assertThat(element).isInstanceOf(XmlElement.class);
        }

        @Test
        @DisplayName("unwrap should return DOM Element")
        void unwrapShouldReturnDomElement() {
            Element element = ElementBuilder.create("test").unwrap();

            assertThat(element).isNotNull();
            assertThat(element.getTagName()).isEqualTo("test");
        }

        @Test
        @DisplayName("toXml should return XML string")
        void toXmlShouldReturnXmlString() {
            String xml = ElementBuilder.create("test")
                .attribute("id", "1")
                .child("child", "text")
                .toXml();

            assertThat(xml).contains("<test");
            assertThat(xml).contains("id=\"1\"");
            assertThat(xml).contains("<child>text</child>");
        }
    }
}
