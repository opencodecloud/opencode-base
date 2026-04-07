package cloud.opencode.base.xml.splitter;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.exception.OpenXmlException;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlSplitter Tests
 * XmlSplitter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
@DisplayName("XmlSplitter Tests")
class XmlSplitterTest {

    private static final String MULTI_ITEM_XML = """
            <root>
                <item><name>Alice</name><age>30</age></item>
                <item><name>Bob</name><age>25</age></item>
                <item><name>Charlie</name><age>35</age></item>
            </root>
            """;

    private static final String NESTED_XML = """
            <root>
                <item>
                    <name>Parent</name>
                    <item>
                        <name>NestedChild</name>
                    </item>
                </item>
                <item><name>Second</name></item>
            </root>
            """;

    private static final String EMPTY_XML = "<root></root>";

    private static final String SINGLE_ITEM_XML = "<root><item>only</item></root>";

    @Nested
    @DisplayName("split(String) Tests")
    class SplitStringTests {

        @Test
        @DisplayName("should split XML with multiple matching elements")
        void shouldSplitMultipleElements() {
            List<XmlDocument> results = new ArrayList<>();
            XmlSplitter.split(MULTI_ITEM_XML, "item", results::add);

            assertThat(results).hasSize(3);
            assertThat(results.get(0).xpath("//name/text()")).isEqualTo("Alice");
            assertThat(results.get(1).xpath("//name/text()")).isEqualTo("Bob");
            assertThat(results.get(2).xpath("//name/text()")).isEqualTo("Charlie");
        }

        @Test
        @DisplayName("should preserve child elements in fragments")
        void shouldPreserveChildElements() {
            List<XmlDocument> results = new ArrayList<>();
            XmlSplitter.split(MULTI_ITEM_XML, "item", results::add);

            XmlDocument first = results.get(0);
            assertThat(first.xpath("//age/text()")).isEqualTo("30");
        }

        @Test
        @DisplayName("should handle no matching elements")
        void shouldHandleNoMatchingElements() {
            List<XmlDocument> results = new ArrayList<>();
            XmlSplitter.split(EMPTY_XML, "item", results::add);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("should handle non-existent element name")
        void shouldHandleNonExistentElementName() {
            List<XmlDocument> results = new ArrayList<>();
            XmlSplitter.split(MULTI_ITEM_XML, "nonexistent", results::add);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("should handle single element")
        void shouldHandleSingleElement() {
            List<XmlDocument> results = new ArrayList<>();
            XmlSplitter.split(SINGLE_ITEM_XML, "item", results::add);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getRoot().getName()).isEqualTo("item");
        }
    }

    @Nested
    @DisplayName("split(InputStream) Tests")
    class SplitInputStreamTests {

        @Test
        @DisplayName("should split from input stream")
        void shouldSplitFromInputStream() {
            InputStream is = new ByteArrayInputStream(MULTI_ITEM_XML.getBytes(StandardCharsets.UTF_8));
            List<XmlDocument> results = new ArrayList<>();
            XmlSplitter.split(is, "item", results::add);

            assertThat(results).hasSize(3);
        }

        @Test
        @DisplayName("should throw on null input stream")
        void shouldThrowOnNullInputStream() {
            assertThatThrownBy(() -> XmlSplitter.split((InputStream) null, "item", doc -> {}))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw on null element name")
        void shouldThrowOnNullElementName() {
            InputStream is = new ByteArrayInputStream("<r/>".getBytes(StandardCharsets.UTF_8));
            assertThatThrownBy(() -> XmlSplitter.split(is, null, doc -> {}))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw on blank element name")
        void shouldThrowOnBlankElementName() {
            InputStream is = new ByteArrayInputStream("<r/>".getBytes(StandardCharsets.UTF_8));
            assertThatThrownBy(() -> XmlSplitter.split(is, "  ", doc -> {}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw on null handler")
        void shouldThrowOnNullHandler() {
            InputStream is = new ByteArrayInputStream("<r/>".getBytes(StandardCharsets.UTF_8));
            assertThatThrownBy(() -> XmlSplitter.split(is, "item", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Nested Element Tests")
    class NestedElementTests {

        @Test
        @DisplayName("should handle nested elements with correct depth tracking")
        void shouldHandleNestedElements() {
            List<XmlDocument> results = new ArrayList<>();
            XmlSplitter.split(NESTED_XML, "item", results::add);

            // Should find the top-level <item> elements only (2 at the top level)
            assertThat(results).hasSize(2);

            // First item should contain the nested <item> as a child
            XmlDocument first = results.get(0);
            assertThat(first.xpath("//name/text()")).isEqualTo("Parent");

            // Second item
            XmlDocument second = results.get(1);
            assertThat(second.xpath("//name/text()")).isEqualTo("Second");
        }
    }

    @Nested
    @DisplayName("splitAll Tests")
    class SplitAllTests {

        @Test
        @DisplayName("should collect all fragments")
        void shouldCollectAllFragments() {
            List<XmlDocument> results = XmlSplitter.splitAll(MULTI_ITEM_XML, "item");

            assertThat(results).hasSize(3);
        }

        @Test
        @DisplayName("should return empty list for no matches")
        void shouldReturnEmptyListForNoMatches() {
            List<XmlDocument> results = XmlSplitter.splitAll(EMPTY_XML, "item");

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("should return immutable list")
        void shouldReturnImmutableList() {
            List<XmlDocument> results = XmlSplitter.splitAll(MULTI_ITEM_XML, "item");

            assertThatThrownBy(() -> results.add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("splitIndexed Tests")
    class SplitIndexedTests {

        @Test
        @DisplayName("should provide correct indices")
        void shouldProvideCorrectIndices() {
            InputStream is = new ByteArrayInputStream(MULTI_ITEM_XML.getBytes(StandardCharsets.UTF_8));
            List<SplitResult> results = new ArrayList<>();
            XmlSplitter.splitIndexed(is, "item", results::add);

            assertThat(results).hasSize(3);
            assertThat(results.get(0).index()).isEqualTo(0);
            assertThat(results.get(1).index()).isEqualTo(1);
            assertThat(results.get(2).index()).isEqualTo(2);
        }

        @Test
        @DisplayName("should provide fragments with correct content")
        void shouldProvideFragmentsWithCorrectContent() {
            InputStream is = new ByteArrayInputStream(MULTI_ITEM_XML.getBytes(StandardCharsets.UTF_8));
            List<SplitResult> results = new ArrayList<>();
            XmlSplitter.splitIndexed(is, "item", results::add);

            assertThat(results.get(0).fragment().xpath("//name/text()")).isEqualTo("Alice");
            assertThat(results.get(2).fragment().xpath("//name/text()")).isEqualTo("Charlie");
        }
    }

    @Nested
    @DisplayName("count Tests")
    class CountTests {

        @Test
        @DisplayName("should count matching elements from string")
        void shouldCountMatchingElementsFromString() {
            int count = XmlSplitter.count(MULTI_ITEM_XML, "item");

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("should count matching elements from input stream")
        void shouldCountMatchingElementsFromInputStream() {
            InputStream is = new ByteArrayInputStream(MULTI_ITEM_XML.getBytes(StandardCharsets.UTF_8));
            int count = XmlSplitter.count(is, "item");

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("should return zero for no matches")
        void shouldReturnZeroForNoMatches() {
            int count = XmlSplitter.count(EMPTY_XML, "item");

            assertThat(count).isZero();
        }

        @Test
        @DisplayName("should return zero for non-existent element name")
        void shouldReturnZeroForNonExistentElementName() {
            int count = XmlSplitter.count(MULTI_ITEM_XML, "nonexistent");

            assertThat(count).isZero();
        }

        @Test
        @DisplayName("should count top-level matches only (not nested)")
        void shouldCountTopLevelMatchesOnly() {
            int count = XmlSplitter.count(NESTED_XML, "item");

            // 2 top-level <item> elements (the nested one is inside the first)
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("should count single element")
        void shouldCountSingleElement() {
            int count = XmlSplitter.count(SINGLE_ITEM_XML, "item");

            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Attribute and Content Tests")
    class AttributeAndContentTests {

        @Test
        @DisplayName("should preserve attributes in split fragments")
        void shouldPreserveAttributes() {
            String xml = """
                    <root>
                        <item id="1" type="A"><value>first</value></item>
                        <item id="2" type="B"><value>second</value></item>
                    </root>
                    """;

            List<XmlDocument> results = XmlSplitter.splitAll(xml, "item");

            assertThat(results).hasSize(2);
            assertThat(results.get(0).getRoot().getAttribute("id")).isEqualTo("1");
            assertThat(results.get(0).getRoot().getAttribute("type")).isEqualTo("A");
            assertThat(results.get(1).getRoot().getAttribute("id")).isEqualTo("2");
        }

        @Test
        @DisplayName("should preserve CDATA sections")
        void shouldPreserveCdataSections() {
            String xml = "<root><item><![CDATA[<html>content</html>]]></item></root>";
            List<XmlDocument> results = XmlSplitter.splitAll(xml, "item");

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("should handle mixed content")
        void shouldHandleMixedContent() {
            String xml = """
                    <root>
                        <item>text <b>bold</b> more text</item>
                    </root>
                    """;

            List<XmlDocument> results = XmlSplitter.splitAll(xml, "item");

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("should handle self-closing elements")
        void shouldHandleSelfClosingElements() {
            String xml = "<root><item/><item/><item/></root>";

            List<XmlDocument> results = XmlSplitter.splitAll(xml, "item");

            assertThat(results).hasSize(3);
        }
    }
}
