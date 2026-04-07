package cloud.opencode.base.xml.benchmark;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.canonical.XmlCanonicalizer;
import cloud.opencode.base.xml.diff.XmlDiff;
import cloud.opencode.base.xml.merge.MergeStrategy;
import cloud.opencode.base.xml.merge.XmlMerge;
import cloud.opencode.base.xml.path.XmlPath;
import cloud.opencode.base.xml.splitter.XmlSplitter;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * XML Performance Regression Tests - validates key operations complete within bounds
 * XML 性能回归测试 - 验证关键操作在性能边界内完成
 *
 * <p>These tests ensure that optimized code paths do not regress.
 * Thresholds are generous to avoid flaky tests on CI.</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
@DisplayName("XML Performance Tests")
class XmlPerformanceTest {

    /**
     * Generates a wide XML with n direct children.
     */
    private static String generateWideXml(int childCount) {
        StringBuilder sb = new StringBuilder("<root>");
        for (int i = 0; i < childCount; i++) {
            sb.append("<item id=\"").append(i).append("\">value").append(i).append("</item>");
        }
        sb.append("</root>");
        return sb.toString();
    }

    /**
     * Generates a deep XML with n levels of nesting.
     */
    private static String generateDeepXml(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("<level").append(i).append(">");
        }
        sb.append("leaf");
        for (int i = depth - 1; i >= 0; i--) {
            sb.append("</level").append(i).append(">");
        }
        return sb.toString();
    }

    @Nested
    @DisplayName("XmlPath Performance")
    class XmlPathPerformance {

        @Test
        @DisplayName("1000 path lookups on wide XML should complete in < 2s")
        void pathLookupsShouldBeEfficient() {
            String xml = generateWideXml(1000);
            XmlDocument doc = XmlDocument.parse(xml);

            long start = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                String value = XmlPath.getString(doc, "root.item");
                assertThat(value).isNotNull();
            }
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            assertThat(elapsed).as("1000 path lookups should complete in < 2s").isLessThan(2000);
        }
    }

    @Nested
    @DisplayName("XmlDiff Performance")
    class XmlDiffPerformance {

        @Test
        @DisplayName("diff of two 500-element XMLs should complete in < 2s")
        void diffWideShouldBeEfficient() {
            String xml1 = generateWideXml(500);
            String xml2 = generateWideXml(500);

            long start = System.nanoTime();
            var diffs = XmlDiff.diff(xml1, xml2);
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            assertThat(diffs).isEmpty();
            assertThat(elapsed).as("diff of 500-element XMLs").isLessThan(2000);
        }
    }

    @Nested
    @DisplayName("XmlMerge Performance")
    class XmlMergePerformance {

        @Test
        @DisplayName("merge of two 500-element XMLs should complete in < 2s")
        void mergeWideShouldBeEfficient() {
            String baseXml = generateWideXml(500);
            String overlayXml = generateWideXml(500);
            XmlDocument base = XmlDocument.parse(baseXml);
            XmlDocument overlay = XmlDocument.parse(overlayXml);

            long start = System.nanoTime();
            XmlDocument merged = XmlMerge.merge(base, overlay, MergeStrategy.OVERRIDE);
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            assertThat(merged).isNotNull();
            assertThat(elapsed).as("merge of 500-element XMLs").isLessThan(2000);
        }
    }

    @Nested
    @DisplayName("XmlSplitter Performance")
    class XmlSplitterPerformance {

        @Test
        @DisplayName("split 1000 elements should complete in < 3s")
        void splitShouldBeEfficient() {
            String xml = generateWideXml(1000);

            long start = System.nanoTime();
            AtomicInteger count = new AtomicInteger(0);
            XmlSplitter.split(xml, "item", doc -> count.incrementAndGet());
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            assertThat(count.get()).isEqualTo(1000);
            assertThat(elapsed).as("split 1000 elements").isLessThan(3000);
        }
    }

    @Nested
    @DisplayName("XmlCanonicalizer Performance")
    class XmlCanonicalizerPerformance {

        @Test
        @DisplayName("canonicalize 500-element XML should complete in < 2s")
        void canonicalizeShouldBeEfficient() {
            String xml = generateWideXml(500);

            long start = System.nanoTime();
            String canonical = XmlCanonicalizer.canonicalize(xml);
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            assertThat(canonical).isNotNull();
            assertThat(elapsed).as("canonicalize 500 elements").isLessThan(2000);
        }
    }

    @Nested
    @DisplayName("XmlElement.getChild Performance")
    class GetChildPerformance {

        @Test
        @DisplayName("getChild on wide tree should not trigger subtree search")
        void getChildShouldBeLinearNotSubtreeSearch() {
            String xml = generateWideXml(5000);
            XmlDocument doc = XmlDocument.parse(xml);

            long start = System.nanoTime();
            // Access last child by iterating getChild — should be O(n) not O(n^2)
            for (int i = 0; i < 100; i++) {
                doc.getRoot().getChild("item");
            }
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            assertThat(elapsed).as("100 getChild calls on 5000-element tree").isLessThan(1000);
        }
    }
}
