package cloud.opencode.base.xml.splitter;

import cloud.opencode.base.xml.XmlDocument;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SplitResult Tests
 * SplitResult 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
@DisplayName("SplitResult Tests")
class SplitResultTest {

    @Test
    @DisplayName("should store index and fragment")
    void shouldStoreIndexAndFragment() {
        XmlDocument doc = XmlDocument.parse("<item>hello</item>");
        SplitResult result = new SplitResult(3, doc);

        assertThat(result.index()).isEqualTo(3);
        assertThat(result.fragment()).isSameAs(doc);
    }

    @Test
    @DisplayName("should support zero index")
    void shouldSupportZeroIndex() {
        XmlDocument doc = XmlDocument.parse("<item/>");
        SplitResult result = new SplitResult(0, doc);

        assertThat(result.index()).isZero();
        assertThat(result.fragment()).isNotNull();
    }

    @Test
    @DisplayName("should implement equals and hashCode")
    void shouldImplementEqualsAndHashCode() {
        XmlDocument doc = XmlDocument.parse("<item>value</item>");
        SplitResult r1 = new SplitResult(1, doc);
        SplitResult r2 = new SplitResult(1, doc);

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    @DisplayName("should not be equal with different index")
    void shouldNotBeEqualWithDifferentIndex() {
        XmlDocument doc = XmlDocument.parse("<item/>");
        SplitResult r1 = new SplitResult(0, doc);
        SplitResult r2 = new SplitResult(1, doc);

        assertThat(r1).isNotEqualTo(r2);
    }

    @Test
    @DisplayName("toString should contain index")
    void toStringShouldContainIndex() {
        XmlDocument doc = XmlDocument.parse("<item/>");
        SplitResult result = new SplitResult(5, doc);

        assertThat(result.toString()).contains("5");
    }
}
