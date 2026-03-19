package cloud.opencode.base.string.template.node;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ForNodeTest Tests
 * ForNodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("ForNode Tests")
class ForNodeTest {

    @Nested
    @DisplayName("render Tests")
    class RenderTests {

        @Test
        @DisplayName("Should iterate over collection")
        void shouldIterateOverCollection() {
            List<TemplateNode> bodyNodes = List.of(new VariableNode("item", ""));
            ForNode node = new ForNode("item", "items", bodyNodes);

            String result = node.render(Map.of("items", List.of("A", "B", "C")));
            assertThat(result).isEqualTo("ABC");
        }

        @Test
        @DisplayName("Should provide index variable")
        void shouldProvideIndexVariable() {
            List<TemplateNode> bodyNodes = List.of(
                new VariableNode("item", ""),
                new TextNode(":"),
                new VariableNode("item_index", "")
            );
            ForNode node = new ForNode("item", "items", bodyNodes);

            String result = node.render(Map.of("items", List.of("A", "B")));
            assertThat(result).isEqualTo("A:0B:1");
        }

        @Test
        @DisplayName("Should provide first variable")
        void shouldProvideFirstVariable() {
            List<TemplateNode> bodyNodes = List.of(new VariableNode("item_first", ""));
            ForNode node = new ForNode("item", "items", bodyNodes);

            String result = node.render(Map.of("items", List.of("A", "B", "C")));
            assertThat(result).isEqualTo("truefalsefalse");
        }

        @Test
        @DisplayName("Should return empty when collection is not iterable")
        void shouldReturnEmptyWhenCollectionIsNotIterable() {
            List<TemplateNode> bodyNodes = List.of(new TextNode("item"));
            ForNode node = new ForNode("item", "items", bodyNodes);

            assertThat(node.render(Map.of("items", "not-iterable"))).isEmpty();
        }

        @Test
        @DisplayName("Should return empty when collection is missing")
        void shouldReturnEmptyWhenCollectionIsMissing() {
            List<TemplateNode> bodyNodes = List.of(new TextNode("item"));
            ForNode node = new ForNode("item", "items", bodyNodes);

            assertThat(node.render(Map.of())).isEmpty();
        }

        @Test
        @DisplayName("Should preserve outer context")
        void shouldPreserveOuterContext() {
            List<TemplateNode> bodyNodes = List.of(
                new VariableNode("outer", ""),
                new TextNode("-"),
                new VariableNode("item", "")
            );
            ForNode node = new ForNode("item", "items", bodyNodes);

            String result = node.render(Map.of("outer", "X", "items", List.of("A", "B")));
            assertThat(result).isEqualTo("X-AX-B");
        }
    }
}
