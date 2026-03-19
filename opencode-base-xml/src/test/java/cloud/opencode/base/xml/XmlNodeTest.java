package cloud.opencode.base.xml;

import org.junit.jupiter.api.*;
import org.w3c.dom.Node;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlNodeTest Tests
 * XmlNodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlNode Tests")
class XmlNodeTest {

    @Nested
    @DisplayName("NodeType Enum Tests")
    class NodeTypeEnumTests {

        @Test
        @DisplayName("NodeType should have all expected values")
        void nodeTypeShouldHaveAllExpectedValues() {
            XmlNode.NodeType[] types = XmlNode.NodeType.values();

            assertThat(types).containsExactly(
                XmlNode.NodeType.ELEMENT,
                XmlNode.NodeType.ATTRIBUTE,
                XmlNode.NodeType.TEXT,
                XmlNode.NodeType.CDATA,
                XmlNode.NodeType.COMMENT,
                XmlNode.NodeType.DOCUMENT,
                XmlNode.NodeType.PROCESSING_INSTRUCTION,
                XmlNode.NodeType.UNKNOWN
            );
        }

        @Test
        @DisplayName("valueOf should return correct enum value")
        void valueOfShouldReturnCorrectEnumValue() {
            assertThat(XmlNode.NodeType.valueOf("ELEMENT")).isEqualTo(XmlNode.NodeType.ELEMENT);
            assertThat(XmlNode.NodeType.valueOf("ATTRIBUTE")).isEqualTo(XmlNode.NodeType.ATTRIBUTE);
            assertThat(XmlNode.NodeType.valueOf("TEXT")).isEqualTo(XmlNode.NodeType.TEXT);
            assertThat(XmlNode.NodeType.valueOf("CDATA")).isEqualTo(XmlNode.NodeType.CDATA);
            assertThat(XmlNode.NodeType.valueOf("COMMENT")).isEqualTo(XmlNode.NodeType.COMMENT);
            assertThat(XmlNode.NodeType.valueOf("DOCUMENT")).isEqualTo(XmlNode.NodeType.DOCUMENT);
            assertThat(XmlNode.NodeType.valueOf("PROCESSING_INSTRUCTION")).isEqualTo(XmlNode.NodeType.PROCESSING_INSTRUCTION);
            assertThat(XmlNode.NodeType.valueOf("UNKNOWN")).isEqualTo(XmlNode.NodeType.UNKNOWN);
        }

        @Test
        @DisplayName("ordinal values should be correct")
        void ordinalValuesShouldBeCorrect() {
            assertThat(XmlNode.NodeType.ELEMENT.ordinal()).isEqualTo(0);
            assertThat(XmlNode.NodeType.ATTRIBUTE.ordinal()).isEqualTo(1);
            assertThat(XmlNode.NodeType.TEXT.ordinal()).isEqualTo(2);
            assertThat(XmlNode.NodeType.CDATA.ordinal()).isEqualTo(3);
            assertThat(XmlNode.NodeType.COMMENT.ordinal()).isEqualTo(4);
            assertThat(XmlNode.NodeType.DOCUMENT.ordinal()).isEqualTo(5);
            assertThat(XmlNode.NodeType.PROCESSING_INSTRUCTION.ordinal()).isEqualTo(6);
            assertThat(XmlNode.NodeType.UNKNOWN.ordinal()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("Interface Method Tests")
    class InterfaceMethodTests {

        @Test
        @DisplayName("XmlElement should implement XmlNode interface")
        void xmlElementShouldImplementXmlNodeInterface() {
            XmlDocument doc = XmlDocument.parse("<root attr=\"value\">text</root>");
            XmlElement element = doc.getRoot();

            assertThat(element).isInstanceOf(XmlNode.class);
        }

        @Test
        @DisplayName("getName should return element name")
        void getNameShouldReturnElementName() {
            XmlDocument doc = XmlDocument.parse("<root/>");
            XmlNode node = doc.getRoot();

            assertThat(node.getName()).isEqualTo("root");
        }

        @Test
        @DisplayName("getText should return text content")
        void getTextShouldReturnTextContent() {
            XmlDocument doc = XmlDocument.parse("<root>hello</root>");
            XmlNode node = doc.getRoot();

            assertThat(node.getText()).isEqualTo("hello");
        }

        @Test
        @DisplayName("hasText should return true when element has text")
        void hasTextShouldReturnTrueWhenElementHasText() {
            XmlDocument doc = XmlDocument.parse("<root>content</root>");
            XmlNode node = doc.getRoot();

            assertThat(node.hasText()).isTrue();
        }

        @Test
        @DisplayName("hasText should return false when element has no text")
        void hasTextShouldReturnFalseWhenElementHasNoText() {
            XmlDocument doc = XmlDocument.parse("<root/>");
            XmlNode node = doc.getRoot();

            assertThat(node.hasText()).isFalse();
        }

        @Test
        @DisplayName("getTextTrim should return trimmed text")
        void getTextTrimShouldReturnTrimmedText() {
            XmlDocument doc = XmlDocument.parse("<root>  hello  </root>");
            XmlNode node = doc.getRoot();

            assertThat(node.getTextTrim()).isEqualTo("hello");
        }

        @Test
        @DisplayName("toXml should return XML string")
        void toXmlShouldReturnXmlString() {
            XmlDocument doc = XmlDocument.parse("<root><child/></root>");
            XmlNode node = doc.getRoot();

            String xml = node.toXml();

            assertThat(xml).contains("<root>");
            assertThat(xml).contains("<child");
        }

        @Test
        @DisplayName("toMap should return map representation")
        void toMapShouldReturnMapRepresentation() {
            XmlDocument doc = XmlDocument.parse("<root><child>value</child></root>");
            XmlNode node = doc.getRoot();

            Map<String, Object> map = node.toMap();

            assertThat(map).isNotNull();
        }
    }
}
