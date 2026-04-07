package cloud.opencode.base.xml.diff;

import cloud.opencode.base.xml.XmlDocument;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlDiff Tests
 * XmlDiff 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
@DisplayName("XmlDiff Tests")
class XmlDiffTest {

    @Nested
    @DisplayName("Equal Documents Tests")
    class EqualDocumentsTests {

        @Test
        @DisplayName("identical XML strings should be equal")
        void identicalXmlStringsShouldBeEqual() {
            String xml = "<root><item>value</item></root>";

            assertThat(XmlDiff.isEqual(xml, xml)).isTrue();
            assertThat(XmlDiff.diff(xml, xml)).isEmpty();
        }

        @Test
        @DisplayName("identical XmlDocument objects should be equal")
        void identicalXmlDocumentObjectsShouldBeEqual() {
            String xml = "<root><item>value</item></root>";
            XmlDocument doc1 = XmlDocument.parse(xml);
            XmlDocument doc2 = XmlDocument.parse(xml);

            assertThat(XmlDiff.isEqual(doc1, doc2)).isTrue();
        }

        @Test
        @DisplayName("empty root elements should be equal")
        void emptyRootElementsShouldBeEqual() {
            String xml1 = "<root/>";
            String xml2 = "<root></root>";

            assertThat(XmlDiff.isEqual(xml1, xml2)).isTrue();
        }

        @Test
        @DisplayName("documents with same attributes should be equal")
        void documentsWithSameAttributesShouldBeEqual() {
            String xml1 = "<root attr=\"value\"><item>text</item></root>";
            String xml2 = "<root attr=\"value\"><item>text</item></root>";

            assertThat(XmlDiff.isEqual(xml1, xml2)).isTrue();
        }
    }

    @Nested
    @DisplayName("Element Difference Tests")
    class ElementDifferenceTests {

        @Test
        @DisplayName("added element should be detected")
        void addedElementShouldBeDetected() {
            String xml1 = "<root><a>1</a></root>";
            String xml2 = "<root><a>1</a><b>2</b></root>";

            List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);

            assertThat(diffs).anyMatch(d ->
                d.type() == DiffType.ADDED && d.path().contains("b"));
        }

        @Test
        @DisplayName("removed element should be detected")
        void removedElementShouldBeDetected() {
            String xml1 = "<root><a>1</a><b>2</b></root>";
            String xml2 = "<root><a>1</a></root>";

            List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);

            assertThat(diffs).anyMatch(d ->
                d.type() == DiffType.REMOVED && d.path().contains("b"));
        }

        @Test
        @DisplayName("different root elements should be detected")
        void differentRootElementsShouldBeDetected() {
            String xml1 = "<root1/>";
            String xml2 = "<root2/>";

            List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);

            assertThat(diffs).isNotEmpty();
            assertThat(diffs.getFirst().type()).isEqualTo(DiffType.MODIFIED);
        }

        @Test
        @DisplayName("multiple added elements should be detected")
        void multipleAddedElementsShouldBeDetected() {
            String xml1 = "<root/>";
            String xml2 = "<root><a>1</a><b>2</b></root>";

            List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);

            assertThat(diffs).hasSize(2);
            assertThat(diffs).allMatch(d -> d.type() == DiffType.ADDED);
        }
    }

    @Nested
    @DisplayName("Text Difference Tests")
    class TextDifferenceTests {

        @Test
        @DisplayName("modified text should be detected")
        void modifiedTextShouldBeDetected() {
            String xml1 = "<root><item>old</item></root>";
            String xml2 = "<root><item>new</item></root>";

            List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);

            assertThat(diffs).anyMatch(d ->
                d.type() == DiffType.TEXT_MODIFIED
                && "old".equals(d.oldValue())
                && "new".equals(d.newValue()));
        }

        @Test
        @DisplayName("added text should be detected")
        void addedTextShouldBeDetected() {
            String xml1 = "<root><item/></root>";
            String xml2 = "<root><item>text</item></root>";

            List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);

            assertThat(diffs).anyMatch(d ->
                d.type() == DiffType.TEXT_MODIFIED
                && d.oldValue() == null
                && "text".equals(d.newValue()));
        }

        @Test
        @DisplayName("removed text should be detected")
        void removedTextShouldBeDetected() {
            String xml1 = "<root><item>text</item></root>";
            String xml2 = "<root><item/></root>";

            List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);

            assertThat(diffs).anyMatch(d ->
                d.type() == DiffType.TEXT_MODIFIED
                && "text".equals(d.oldValue())
                && d.newValue() == null);
        }
    }

    @Nested
    @DisplayName("Attribute Difference Tests")
    class AttributeDifferenceTests {

        @Test
        @DisplayName("added attribute should be detected")
        void addedAttributeShouldBeDetected() {
            String xml1 = "<root><item/></root>";
            String xml2 = "<root><item id=\"1\"/></root>";

            List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);

            assertThat(diffs).anyMatch(d ->
                d.type() == DiffType.ATTRIBUTE_ADDED
                && d.path().contains("@id")
                && "1".equals(d.newValue()));
        }

        @Test
        @DisplayName("removed attribute should be detected")
        void removedAttributeShouldBeDetected() {
            String xml1 = "<root><item id=\"1\"/></root>";
            String xml2 = "<root><item/></root>";

            List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);

            assertThat(diffs).anyMatch(d ->
                d.type() == DiffType.ATTRIBUTE_REMOVED
                && d.path().contains("@id")
                && "1".equals(d.oldValue()));
        }

        @Test
        @DisplayName("modified attribute should be detected")
        void modifiedAttributeShouldBeDetected() {
            String xml1 = "<root><item id=\"1\"/></root>";
            String xml2 = "<root><item id=\"2\"/></root>";

            List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);

            assertThat(diffs).anyMatch(d ->
                d.type() == DiffType.ATTRIBUTE_MODIFIED
                && d.path().contains("@id")
                && "1".equals(d.oldValue())
                && "2".equals(d.newValue()));
        }

        @Test
        @DisplayName("root attribute difference should be detected")
        void rootAttributeDifferenceShouldBeDetected() {
            String xml1 = "<root version=\"1\"/>";
            String xml2 = "<root version=\"2\"/>";

            List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);

            assertThat(diffs).anyMatch(d ->
                d.type() == DiffType.ATTRIBUTE_MODIFIED
                && d.path().equals("/root/@version"));
        }
    }

    @Nested
    @DisplayName("Path Format Tests")
    class PathFormatTests {

        @Test
        @DisplayName("path should use indexed format")
        void pathShouldUseIndexedFormat() {
            String xml1 = "<root><item>a</item><item>b</item></root>";
            String xml2 = "<root><item>a</item><item>c</item></root>";

            List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);

            assertThat(diffs).anyMatch(d ->
                d.path().equals("/root/item[1]")
                && d.type() == DiffType.TEXT_MODIFIED);
        }

        @Test
        @DisplayName("nested path should be correct")
        void nestedPathShouldBeCorrect() {
            String xml1 = "<root><parent><child>old</child></parent></root>";
            String xml2 = "<root><parent><child>new</child></parent></root>";

            List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);

            assertThat(diffs).anyMatch(d ->
                d.path().equals("/root/parent[0]/child[0]")
                && d.type() == DiffType.TEXT_MODIFIED);
        }
    }

    @Nested
    @DisplayName("Complex Comparison Tests")
    class ComplexComparisonTests {

        @Test
        @DisplayName("deeply nested differences should be detected")
        void deeplyNestedDifferencesShouldBeDetected() {
            String xml1 = "<root><a><b><c>old</c></b></a></root>";
            String xml2 = "<root><a><b><c>new</c></b></a></root>";

            List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);

            assertThat(diffs).hasSize(1);
            assertThat(diffs.getFirst().type()).isEqualTo(DiffType.TEXT_MODIFIED);
            assertThat(diffs.getFirst().path()).contains("c[0]");
        }

        @Test
        @DisplayName("multiple differences should all be detected")
        void multipleDifferencesShouldAllBeDetected() {
            String xml1 = "<root><a>1</a><b>2</b></root>";
            String xml2 = "<root><a>x</a><b>y</b></root>";

            List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);

            assertThat(diffs).hasSize(2);
            assertThat(diffs).allMatch(d -> d.type() == DiffType.TEXT_MODIFIED);
        }

        @Test
        @DisplayName("mixed element and attribute changes should be detected")
        void mixedElementAndAttributeChangesShouldBeDetected() {
            String xml1 = "<root><item id=\"1\">old</item></root>";
            String xml2 = "<root><item id=\"2\">new</item></root>";

            List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);

            assertThat(diffs).anyMatch(d -> d.type() == DiffType.ATTRIBUTE_MODIFIED);
            assertThat(diffs).anyMatch(d -> d.type() == DiffType.TEXT_MODIFIED);
        }
    }

    @Nested
    @DisplayName("Null Handling Tests")
    class NullHandlingTests {

        @Test
        @DisplayName("null xml1 should throw NullPointerException")
        void nullXml1ShouldThrowNullPointerException() {
            assertThatThrownBy(() -> XmlDiff.diff((String) null, "<root/>"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null xml2 should throw NullPointerException")
        void nullXml2ShouldThrowNullPointerException() {
            assertThatThrownBy(() -> XmlDiff.diff("<root/>", (String) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null doc1 should throw NullPointerException")
        void nullDoc1ShouldThrowNullPointerException() {
            assertThatThrownBy(() -> XmlDiff.diff((XmlDocument) null, XmlDocument.parse("<root/>")))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null doc2 should throw NullPointerException")
        void nullDoc2ShouldThrowNullPointerException() {
            assertThatThrownBy(() -> XmlDiff.diff(XmlDocument.parse("<root/>"), (XmlDocument) null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("isEqual Tests")
    class IsEqualTests {

        @Test
        @DisplayName("isEqual with XmlDocument should work")
        void isEqualWithXmlDocumentShouldWork() {
            XmlDocument doc1 = XmlDocument.parse("<root><a>1</a></root>");
            XmlDocument doc2 = XmlDocument.parse("<root><a>1</a></root>");

            assertThat(XmlDiff.isEqual(doc1, doc2)).isTrue();
        }

        @Test
        @DisplayName("isEqual should return false for different documents")
        void isEqualShouldReturnFalseForDifferentDocuments() {
            assertThat(XmlDiff.isEqual("<root><a>1</a></root>", "<root><a>2</a></root>")).isFalse();
        }
    }
}
