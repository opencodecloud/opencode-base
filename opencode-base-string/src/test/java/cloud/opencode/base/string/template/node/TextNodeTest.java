package cloud.opencode.base.string.template.node;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TextNodeTest Tests
 * TextNodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("TextNode Tests")
class TextNodeTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create text node")
        void shouldCreateTextNode() {
            TextNode node = new TextNode("Hello World");
            assertThat(node).isNotNull();
        }
    }

    @Nested
    @DisplayName("render Tests")
    class RenderTests {

        @Test
        @DisplayName("Should render text unchanged")
        void shouldRenderTextUnchanged() {
            TextNode node = new TextNode("Hello World");
            assertThat(node.render(Map.of())).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("Should render regardless of context")
        void shouldRenderRegardlessOfContext() {
            TextNode node = new TextNode("Static Text");
            assertThat(node.render(Map.of("name", "ignored"))).isEqualTo("Static Text");
        }

        @Test
        @DisplayName("Should render null text")
        void shouldRenderNullText() {
            TextNode node = new TextNode(null);
            assertThat(node.render(Map.of())).isNull();
        }

        @Test
        @DisplayName("Should render empty text")
        void shouldRenderEmptyText() {
            TextNode node = new TextNode("");
            assertThat(node.render(Map.of())).isEmpty();
        }
    }
}
