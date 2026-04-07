package cloud.opencode.base.xml.canonical;

import cloud.opencode.base.xml.XmlDocument;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlCanonicalizer Tests
 * XmlCanonicalizer 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
@DisplayName("XmlCanonicalizer Tests")
class XmlCanonicalizerTest {

    @Nested
    @DisplayName("Attribute Reordering Tests")
    class AttributeReorderingTests {

        @Test
        @DisplayName("should sort attributes alphabetically")
        void shouldSortAttributesAlphabetically() {
            String xml = "<root z=\"3\" a=\"1\" m=\"2\"/>";

            String canonical = XmlCanonicalizer.canonicalize(xml);

            assertThat(canonical).contains("a=\"1\"");
            // Verify 'a' appears before 'm' and 'm' before 'z'
            int posA = canonical.indexOf("a=\"1\"");
            int posM = canonical.indexOf("m=\"2\"");
            int posZ = canonical.indexOf("z=\"3\"");
            assertThat(posA).isLessThan(posM);
            assertThat(posM).isLessThan(posZ);
        }

        @Test
        @DisplayName("should handle element with single attribute")
        void shouldHandleSingleAttribute() {
            String xml = "<root attr=\"value\"/>";

            String canonical = XmlCanonicalizer.canonicalize(xml);

            assertThat(canonical).contains("attr=\"value\"");
        }

        @Test
        @DisplayName("should handle nested elements with attributes")
        void shouldHandleNestedElementsWithAttributes() {
            String xml = "<root b=\"2\" a=\"1\"><child y=\"2\" x=\"1\"/></root>";

            String canonical = XmlCanonicalizer.canonicalize(xml);

            // Both root and child attributes should be sorted
            int rootA = canonical.indexOf("a=\"1\"");
            int rootB = canonical.indexOf("b=\"2\"");
            assertThat(rootA).isLessThan(rootB);

            int childX = canonical.indexOf("x=\"1\"");
            int childY = canonical.indexOf("y=\"2\"");
            assertThat(childX).isLessThan(childY);
        }
    }

    @Nested
    @DisplayName("Whitespace Normalization Tests")
    class WhitespaceNormalizationTests {

        @Test
        @DisplayName("should normalize whitespace between elements")
        void shouldNormalizeWhitespaceBetweenElements() {
            String xml = "<root>  <child>value</child>  </root>";

            String canonical = XmlCanonicalizer.canonicalize(xml);

            assertThat(canonical).isEqualTo("<root><child>value</child></root>");
        }

        @Test
        @DisplayName("should preserve text content whitespace")
        void shouldPreserveTextContentWhitespace() {
            String xml = "<root><text>hello world</text></root>";

            String canonical = XmlCanonicalizer.canonicalize(xml);

            assertThat(canonical).contains("hello world");
        }
    }

    @Nested
    @DisplayName("Comment Handling Tests")
    class CommentHandlingTests {

        @Test
        @DisplayName("should preserve comments by default")
        void shouldPreserveCommentsByDefault() {
            String xml = "<root><!-- comment --><child/></root>";

            String canonical = XmlCanonicalizer.canonicalize(xml);

            assertThat(canonical).contains("<!-- comment -->");
        }

        @Test
        @DisplayName("should remove comments when requested")
        void shouldRemoveCommentsWhenRequested() {
            String xml = "<root><!-- comment --><child/></root>";

            String canonical = XmlCanonicalizer.canonicalize(xml, true);

            assertThat(canonical).doesNotContain("comment");
        }

        @Test
        @DisplayName("should remove nested comments when requested")
        void shouldRemoveNestedComments() {
            String xml = "<root><!-- outer --><child><!-- inner --><sub/></child></root>";

            String canonical = XmlCanonicalizer.canonicalize(xml, true);

            assertThat(canonical).doesNotContain("outer");
            assertThat(canonical).doesNotContain("inner");
        }
    }

    @Nested
    @DisplayName("Consistency Tests")
    class ConsistencyTests {

        @Test
        @DisplayName("should produce consistent output across multiple calls")
        void shouldProduceConsistentOutput() {
            String xml = "<root c=\"3\" a=\"1\" b=\"2\"><child/></root>";

            String first = XmlCanonicalizer.canonicalize(xml);
            String second = XmlCanonicalizer.canonicalize(xml);
            String third = XmlCanonicalizer.canonicalize(xml);

            assertThat(first).isEqualTo(second);
            assertThat(second).isEqualTo(third);
        }

        @Test
        @DisplayName("should produce same output for equivalent XML with different formatting")
        void shouldProduceSameOutputForEquivalentXml() {
            String xml1 = "<root a=\"1\" b=\"2\"><child>text</child></root>";
            String xml2 = "<root   b=\"2\"   a=\"1\"  >\n  <child>text</child>\n</root>";

            String canonical1 = XmlCanonicalizer.canonicalize(xml1);
            String canonical2 = XmlCanonicalizer.canonicalize(xml2);

            assertThat(canonical1).isEqualTo(canonical2);
        }
    }

    @Nested
    @DisplayName("XML Declaration Tests")
    class XmlDeclarationTests {

        @Test
        @DisplayName("should omit XML declaration")
        void shouldOmitXmlDeclaration() {
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>";

            String canonical = XmlCanonicalizer.canonicalize(xml);

            assertThat(canonical).doesNotContain("<?xml");
        }
    }

    @Nested
    @DisplayName("Namespace Handling Tests")
    class NamespaceHandlingTests {

        @Test
        @DisplayName("should handle elements without namespace")
        void shouldHandleElementsWithoutNamespace() {
            String xml = "<root><child>text</child></root>";

            String canonical = XmlCanonicalizer.canonicalize(xml);

            assertThat(canonical).isEqualTo("<root><child>text</child></root>");
        }

        @Test
        @DisplayName("should place namespace declarations before regular attributes")
        void shouldPlaceNamespaceFirst() {
            String xml = "<root z=\"1\" xmlns:ns=\"http://example.com\"/>";

            String canonical = XmlCanonicalizer.canonicalize(xml);

            int nsPos = canonical.indexOf("xmlns:");
            int attrPos = canonical.indexOf("z=\"1\"");
            assertThat(nsPos).isLessThan(attrPos);
        }
    }

    @Nested
    @DisplayName("XmlDocument Input Tests")
    class XmlDocumentInputTests {

        @Test
        @DisplayName("should canonicalize XmlDocument")
        void shouldCanonicalizeXmlDocument() {
            XmlDocument doc = XmlDocument.parse("<root b=\"2\" a=\"1\"><child/></root>");

            String canonical = XmlCanonicalizer.canonicalize(doc);

            int posA = canonical.indexOf("a=\"1\"");
            int posB = canonical.indexOf("b=\"2\"");
            assertThat(posA).isLessThan(posB);
        }

        @Test
        @DisplayName("should not modify original document")
        void shouldNotModifyOriginalDocument() {
            XmlDocument doc = XmlDocument.parse("<root><!-- comment --><child/></root>");
            String original = doc.toXml();

            XmlCanonicalizer.canonicalize(doc, true);

            assertThat(doc.toXml()).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("Null Safety Tests")
    class NullSafetyTests {

        @Test
        @DisplayName("should throw on null XML string")
        void shouldThrowOnNullXmlString() {
            assertThatNullPointerException()
                .isThrownBy(() -> XmlCanonicalizer.canonicalize((String) null));
        }

        @Test
        @DisplayName("should throw on null document")
        void shouldThrowOnNullDocument() {
            assertThatNullPointerException()
                .isThrownBy(() -> XmlCanonicalizer.canonicalize((XmlDocument) null));
        }
    }
}
