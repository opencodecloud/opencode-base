package cloud.opencode.base.xml.sax;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * SaxHandlerTest Tests
 * SaxHandlerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("SaxHandler 接口测试")
class SaxHandlerTest {

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("startDocument默认不抛出异常")
        void testStartDocumentDefault() {
            SaxHandler handler = new SaxHandler() {};

            assertThatNoException().isThrownBy(handler::startDocument);
        }

        @Test
        @DisplayName("endDocument默认不抛出异常")
        void testEndDocumentDefault() {
            SaxHandler handler = new SaxHandler() {};

            assertThatNoException().isThrownBy(handler::endDocument);
        }

        @Test
        @DisplayName("startElement默认不抛出异常")
        void testStartElementDefault() {
            SaxHandler handler = new SaxHandler() {};

            assertThatNoException().isThrownBy(() ->
                    handler.startElement("", "element", "element", Map.of()));
        }

        @Test
        @DisplayName("endElement默认不抛出异常")
        void testEndElementDefault() {
            SaxHandler handler = new SaxHandler() {};

            assertThatNoException().isThrownBy(() ->
                    handler.endElement("", "element", "element"));
        }

        @Test
        @DisplayName("characters默认不抛出异常")
        void testCharactersDefault() {
            SaxHandler handler = new SaxHandler() {};

            assertThatNoException().isThrownBy(() -> handler.characters("text content"));
        }
    }

    @Nested
    @DisplayName("自定义实现测试")
    class CustomImplementationTests {

        @Test
        @DisplayName("自定义handler记录所有事件")
        void testCustomHandlerRecordsEvents() {
            List<String> events = new ArrayList<>();
            SaxHandler handler = new SaxHandler() {
                @Override
                public void startDocument() {
                    events.add("start-doc");
                }

                @Override
                public void startElement(String uri, String localName, String qName,
                                         Map<String, String> attributes) {
                    events.add("start:" + qName);
                }

                @Override
                public void characters(String content) {
                    events.add("chars:" + content);
                }

                @Override
                public void endElement(String uri, String localName, String qName) {
                    events.add("end:" + qName);
                }

                @Override
                public void endDocument() {
                    events.add("end-doc");
                }
            };

            handler.startDocument();
            handler.startElement("", "root", "root", Map.of());
            handler.characters("hello");
            handler.endElement("", "root", "root");
            handler.endDocument();

            assertThat(events).containsExactly(
                    "start-doc", "start:root", "chars:hello", "end:root", "end-doc");
        }
    }
}
