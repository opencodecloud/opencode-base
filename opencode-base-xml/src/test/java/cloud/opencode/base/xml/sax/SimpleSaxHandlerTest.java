package cloud.opencode.base.xml.sax;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * SimpleSaxHandlerTest Tests
 * SimpleSaxHandlerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("SimpleSaxHandler Tests")
class SimpleSaxHandlerTest {

    private static final String TEST_XML = """
        <catalog>
            <book id="1">
                <title>Java Programming</title>
            </book>
            <book id="2">
                <title>XML Guide</title>
            </book>
        </catalog>
        """;

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("create should create new handler")
        void createShouldCreateNewHandler() {
            SimpleSaxHandler handler = SimpleSaxHandler.create();

            assertThat(handler).isNotNull();
        }
    }

    @Nested
    @DisplayName("OnStart Tests")
    class OnStartTests {

        @Test
        @DisplayName("onStart should handle specific element start")
        void onStartShouldHandleSpecificElementStart() {
            List<String> ids = new ArrayList<>();

            SimpleSaxHandler handler = SimpleSaxHandler.create()
                .onStart("book", (name, attrs) -> ids.add(attrs.get("id")));

            SaxParser.create().handler(handler).parse(TEST_XML);

            assertThat(ids).containsExactly("1", "2");
        }

        @Test
        @DisplayName("onAnyStart should handle all element starts")
        void onAnyStartShouldHandleAllElementStarts() {
            List<String> elements = new ArrayList<>();

            SimpleSaxHandler handler = SimpleSaxHandler.create()
                .onAnyStart((name, attrs) -> elements.add(name));

            SaxParser.create().handler(handler).parse(TEST_XML);

            assertThat(elements).contains("catalog", "book", "title");
        }
    }

    @Nested
    @DisplayName("OnEnd Tests")
    class OnEndTests {

        @Test
        @DisplayName("onEnd should handle specific element end")
        void onEndShouldHandleSpecificElementEnd() {
            List<String> bookEnds = new ArrayList<>();

            SimpleSaxHandler handler = SimpleSaxHandler.create()
                .onEnd("book", name -> bookEnds.add(name));

            SaxParser.create().handler(handler).parse(TEST_XML);

            assertThat(bookEnds).hasSize(2);
        }

        @Test
        @DisplayName("onAnyEnd should handle all element ends")
        void onAnyEndShouldHandleAllElementEnds() {
            List<String> elements = new ArrayList<>();

            SimpleSaxHandler handler = SimpleSaxHandler.create()
                .onAnyEnd(name -> elements.add(name));

            SaxParser.create().handler(handler).parse(TEST_XML);

            assertThat(elements).contains("catalog", "book", "title");
        }
    }

    @Nested
    @DisplayName("OnText Tests")
    class OnTextTests {

        @Test
        @DisplayName("onText should handle text content for specific element")
        void onTextShouldHandleTextContentForSpecificElement() {
            List<String> titles = new ArrayList<>();

            SimpleSaxHandler handler = SimpleSaxHandler.create()
                .onText("title", text -> titles.add(text));

            SaxParser.create().handler(handler).parse(TEST_XML);

            assertThat(titles).containsExactly("Java Programming", "XML Guide");
        }
    }

    @Nested
    @DisplayName("Chaining Tests")
    class ChainingTests {

        @Test
        @DisplayName("handlers should be chainable")
        void handlersShouldBeChainable() {
            List<String> starts = new ArrayList<>();
            List<String> ends = new ArrayList<>();
            List<String> texts = new ArrayList<>();

            SimpleSaxHandler handler = SimpleSaxHandler.create()
                .onStart("book", (name, attrs) -> starts.add(name))
                .onEnd("book", name -> ends.add(name))
                .onText("title", text -> texts.add(text));

            SaxParser.create().handler(handler).parse(TEST_XML);

            assertThat(starts).hasSize(2);
            assertThat(ends).hasSize(2);
            assertThat(texts).containsExactly("Java Programming", "XML Guide");
        }
    }

    @Nested
    @DisplayName("Multiple Element Handlers Tests")
    class MultipleElementHandlersTests {

        @Test
        @DisplayName("should support multiple start handlers")
        void shouldSupportMultipleStartHandlers() {
            List<String> bookStarts = new ArrayList<>();
            List<String> titleStarts = new ArrayList<>();

            SimpleSaxHandler handler = SimpleSaxHandler.create()
                .onStart("book", (name, attrs) -> bookStarts.add(name))
                .onStart("title", (name, attrs) -> titleStarts.add(name));

            SaxParser.create().handler(handler).parse(TEST_XML);

            assertThat(bookStarts).hasSize(2);
            assertThat(titleStarts).hasSize(2);
        }

        @Test
        @DisplayName("should support multiple end handlers")
        void shouldSupportMultipleEndHandlers() {
            List<String> bookEnds = new ArrayList<>();
            List<String> titleEnds = new ArrayList<>();

            SimpleSaxHandler handler = SimpleSaxHandler.create()
                .onEnd("book", name -> bookEnds.add(name))
                .onEnd("title", name -> titleEnds.add(name));

            SaxParser.create().handler(handler).parse(TEST_XML);

            assertThat(bookEnds).hasSize(2);
            assertThat(titleEnds).hasSize(2);
        }

        @Test
        @DisplayName("should support multiple text handlers")
        void shouldSupportMultipleTextHandlers() {
            List<String> titles = new ArrayList<>();

            SimpleSaxHandler handler = SimpleSaxHandler.create()
                .onText("title", text -> titles.add(text));

            SaxParser.create().handler(handler).parse(TEST_XML);

            assertThat(titles).containsExactly("Java Programming", "XML Guide");
        }
    }
}
