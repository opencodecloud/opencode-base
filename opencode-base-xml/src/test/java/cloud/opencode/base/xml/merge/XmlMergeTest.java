package cloud.opencode.base.xml.merge;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.exception.OpenXmlException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlMerge Tests
 * XmlMerge 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
@DisplayName("XmlMerge Tests")
class XmlMergeTest {

    @Nested
    @DisplayName("Override Strategy Tests")
    class OverrideStrategyTests {

        @Test
        @DisplayName("should override existing text content")
        void shouldOverrideExistingTextContent() {
            XmlDocument base = XmlDocument.parse("<root><item>old</item></root>");
            XmlDocument overlay = XmlDocument.parse("<root><item>new</item></root>");

            XmlDocument merged = XmlMerge.merge(base, overlay);
            String xml = merged.toXml();

            assertThat(xml).contains("new");
            assertThat(xml).doesNotContain(">old<");
        }

        @Test
        @DisplayName("should add new elements from overlay")
        void shouldAddNewElementsFromOverlay() {
            XmlDocument base = XmlDocument.parse("<root><a>1</a></root>");
            XmlDocument overlay = XmlDocument.parse("<root><b>2</b></root>");

            XmlDocument merged = XmlMerge.merge(base, overlay);
            String xml = merged.toXml();

            assertThat(xml).contains("<a>1</a>");
            assertThat(xml).contains("<b>2</b>");
        }

        @Test
        @DisplayName("should override attributes")
        void shouldOverrideAttributes() {
            XmlDocument base = XmlDocument.parse("<root attr=\"old\"/>");
            XmlDocument overlay = XmlDocument.parse("<root attr=\"new\"/>");

            XmlDocument merged = XmlMerge.merge(base, overlay);
            String xml = merged.toXml();

            assertThat(xml).contains("attr=\"new\"");
        }

        @Test
        @DisplayName("should add new attributes from overlay")
        void shouldAddNewAttributesFromOverlay() {
            XmlDocument base = XmlDocument.parse("<root a=\"1\"/>");
            XmlDocument overlay = XmlDocument.parse("<root b=\"2\"/>");

            XmlDocument merged = XmlMerge.merge(base, overlay);
            String xml = merged.toXml();

            assertThat(xml).contains("a=\"1\"");
            assertThat(xml).contains("b=\"2\"");
        }

        @Test
        @DisplayName("should deep merge nested elements")
        void shouldDeepMergeNestedElements() {
            XmlDocument base = XmlDocument.parse(
                "<root><parent><child1>a</child1></parent></root>");
            XmlDocument overlay = XmlDocument.parse(
                "<root><parent><child2>b</child2></parent></root>");

            XmlDocument merged = XmlMerge.merge(base, overlay);
            String xml = merged.toXml();

            assertThat(xml).contains("<child1>a</child1>");
            assertThat(xml).contains("<child2>b</child2>");
        }

        @Test
        @DisplayName("default merge should use OVERRIDE strategy")
        void defaultMergeShouldUseOverrideStrategy() {
            XmlDocument base = XmlDocument.parse("<root><item>old</item></root>");
            XmlDocument overlay = XmlDocument.parse("<root><item>new</item></root>");

            XmlDocument merged = XmlMerge.merge(base, overlay);
            String xml = merged.toXml();

            assertThat(xml).contains("new");
        }
    }

    @Nested
    @DisplayName("Append Strategy Tests")
    class AppendStrategyTests {

        @Test
        @DisplayName("should append overlay elements")
        void shouldAppendOverlayElements() {
            XmlDocument base = XmlDocument.parse("<root><item>1</item></root>");
            XmlDocument overlay = XmlDocument.parse("<root><item>2</item></root>");

            XmlDocument merged = XmlMerge.merge(base, overlay, MergeStrategy.APPEND);
            String xml = merged.toXml();

            // Should have both items
            assertThat(xml).contains(">1<");
            assertThat(xml).contains(">2<");
        }

        @Test
        @DisplayName("should append new elements")
        void shouldAppendNewElements() {
            XmlDocument base = XmlDocument.parse("<root><a>1</a></root>");
            XmlDocument overlay = XmlDocument.parse("<root><b>2</b></root>");

            XmlDocument merged = XmlMerge.merge(base, overlay, MergeStrategy.APPEND);
            String xml = merged.toXml();

            assertThat(xml).contains("<a>1</a>");
            assertThat(xml).contains("<b>2</b>");
        }

        @Test
        @DisplayName("should append multiple overlay items")
        void shouldAppendMultipleOverlayItems() {
            XmlDocument base = XmlDocument.parse("<root><item>a</item></root>");
            XmlDocument overlay = XmlDocument.parse("<root><item>b</item><item>c</item></root>");

            XmlDocument merged = XmlMerge.merge(base, overlay, MergeStrategy.APPEND);
            String xml = merged.toXml();

            assertThat(xml).contains(">a<");
            assertThat(xml).contains(">b<");
            assertThat(xml).contains(">c<");
        }
    }

    @Nested
    @DisplayName("SkipExisting Strategy Tests")
    class SkipExistingStrategyTests {

        @Test
        @DisplayName("should skip existing elements")
        void shouldSkipExistingElements() {
            XmlDocument base = XmlDocument.parse("<root><item>old</item></root>");
            XmlDocument overlay = XmlDocument.parse("<root><item>new</item></root>");

            XmlDocument merged = XmlMerge.merge(base, overlay, MergeStrategy.SKIP_EXISTING);
            String xml = merged.toXml();

            assertThat(xml).contains(">old<");
            assertThat(xml).doesNotContain(">new<");
        }

        @Test
        @DisplayName("should add non-existing elements")
        void shouldAddNonExistingElements() {
            XmlDocument base = XmlDocument.parse("<root><a>1</a></root>");
            XmlDocument overlay = XmlDocument.parse("<root><b>2</b></root>");

            XmlDocument merged = XmlMerge.merge(base, overlay, MergeStrategy.SKIP_EXISTING);
            String xml = merged.toXml();

            assertThat(xml).contains("<a>1</a>");
            assertThat(xml).contains("<b>2</b>");
        }

        @Test
        @DisplayName("should skip existing attributes")
        void shouldSkipExistingAttributes() {
            XmlDocument base = XmlDocument.parse("<root attr=\"old\"/>");
            XmlDocument overlay = XmlDocument.parse("<root attr=\"new\"/>");

            XmlDocument merged = XmlMerge.merge(base, overlay, MergeStrategy.SKIP_EXISTING);
            String xml = merged.toXml();

            assertThat(xml).contains("attr=\"old\"");
        }

        @Test
        @DisplayName("should add non-existing attributes")
        void shouldAddNonExistingAttributes() {
            XmlDocument base = XmlDocument.parse("<root a=\"1\"/>");
            XmlDocument overlay = XmlDocument.parse("<root b=\"2\"/>");

            XmlDocument merged = XmlMerge.merge(base, overlay, MergeStrategy.SKIP_EXISTING);
            String xml = merged.toXml();

            assertThat(xml).contains("a=\"1\"");
            assertThat(xml).contains("b=\"2\"");
        }
    }

    @Nested
    @DisplayName("String Merge Tests")
    class StringMergeTests {

        @Test
        @DisplayName("should merge XML strings with default strategy")
        void shouldMergeXmlStringsWithDefaultStrategy() {
            String base = "<root><a>1</a></root>";
            String overlay = "<root><b>2</b></root>";

            String merged = XmlMerge.merge(base, overlay);

            assertThat(merged).contains("<a>1</a>");
            assertThat(merged).contains("<b>2</b>");
        }

        @Test
        @DisplayName("should merge XML strings with specified strategy")
        void shouldMergeXmlStringsWithSpecifiedStrategy() {
            String base = "<root><item>old</item></root>";
            String overlay = "<root><item>new</item></root>";

            String merged = XmlMerge.merge(base, overlay, MergeStrategy.SKIP_EXISTING);

            assertThat(merged).contains(">old<");
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("mismatched root elements should throw exception")
        void mismatchedRootElementsShouldThrowException() {
            XmlDocument base = XmlDocument.parse("<root1/>");
            XmlDocument overlay = XmlDocument.parse("<root2/>");

            assertThatThrownBy(() -> XmlMerge.merge(base, overlay))
                .isInstanceOf(OpenXmlException.class)
                .hasMessageContaining("Root element mismatch");
        }

        @Test
        @DisplayName("null base should throw NullPointerException")
        void nullBaseShouldThrowNullPointerException() {
            assertThatThrownBy(() -> XmlMerge.merge((XmlDocument) null, XmlDocument.parse("<root/>")))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null overlay should throw NullPointerException")
        void nullOverlayShouldThrowNullPointerException() {
            assertThatThrownBy(() -> XmlMerge.merge(XmlDocument.parse("<root/>"), (XmlDocument) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null strategy should throw NullPointerException")
        void nullStrategyShouldThrowNullPointerException() {
            XmlDocument doc = XmlDocument.parse("<root/>");
            assertThatThrownBy(() -> XmlMerge.merge(doc, doc, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null baseXml string should throw NullPointerException")
        void nullBaseXmlStringShouldThrowNullPointerException() {
            assertThatThrownBy(() -> XmlMerge.merge((String) null, "<root/>"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null overlayXml string should throw NullPointerException")
        void nullOverlayXmlStringShouldThrowNullPointerException() {
            assertThatThrownBy(() -> XmlMerge.merge("<root/>", (String) null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("merge should not modify base document")
        void mergeShouldNotModifyBaseDocument() {
            XmlDocument base = XmlDocument.parse("<root><item>original</item></root>");
            XmlDocument overlay = XmlDocument.parse("<root><item>modified</item></root>");

            XmlMerge.merge(base, overlay);

            assertThat(base.toXml()).contains("original");
        }

        @Test
        @DisplayName("merge should not modify overlay document")
        void mergeShouldNotModifyOverlayDocument() {
            XmlDocument base = XmlDocument.parse("<root><item>base</item></root>");
            XmlDocument overlay = XmlDocument.parse("<root><item>overlay</item></root>");

            XmlMerge.merge(base, overlay);

            assertThat(overlay.toXml()).contains("overlay");
        }
    }

    @Nested
    @DisplayName("Complex Merge Tests")
    class ComplexMergeTests {

        @Test
        @DisplayName("should merge deeply nested structures")
        void shouldMergeDeeplyNestedStructures() {
            XmlDocument base = XmlDocument.parse(
                "<config><db><host>localhost</host></db></config>");
            XmlDocument overlay = XmlDocument.parse(
                "<config><db><port>5432</port></db></config>");

            XmlDocument merged = XmlMerge.merge(base, overlay);
            String xml = merged.toXml();

            assertThat(xml).contains("<host>localhost</host>");
            assertThat(xml).contains("<port>5432</port>");
        }

        @Test
        @DisplayName("should handle empty overlay")
        void shouldHandleEmptyOverlay() {
            XmlDocument base = XmlDocument.parse("<root><item>value</item></root>");
            XmlDocument overlay = XmlDocument.parse("<root/>");

            XmlDocument merged = XmlMerge.merge(base, overlay);
            String xml = merged.toXml();

            assertThat(xml).contains("<item>value</item>");
        }

        @Test
        @DisplayName("should handle empty base")
        void shouldHandleEmptyBase() {
            XmlDocument base = XmlDocument.parse("<root/>");
            XmlDocument overlay = XmlDocument.parse("<root><item>value</item></root>");

            XmlDocument merged = XmlMerge.merge(base, overlay);
            String xml = merged.toXml();

            assertThat(xml).contains("<item>value</item>");
        }
    }
}
