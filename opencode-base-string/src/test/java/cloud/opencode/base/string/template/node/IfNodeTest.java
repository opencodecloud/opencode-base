package cloud.opencode.base.string.template.node;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * IfNodeTest Tests
 * IfNodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("IfNode Tests")
class IfNodeTest {

    @Nested
    @DisplayName("render Tests")
    class RenderTests {

        @Test
        @DisplayName("Should render then nodes when condition is true boolean")
        void shouldRenderThenNodesWhenConditionIsTrueBoolean() {
            List<TemplateNode> thenNodes = List.of(new TextNode("Yes"));
            List<TemplateNode> elseNodes = List.of(new TextNode("No"));
            IfNode node = new IfNode("show", thenNodes, elseNodes);

            assertThat(node.render(Map.of("show", true))).isEqualTo("Yes");
        }

        @Test
        @DisplayName("Should render else nodes when condition is false boolean")
        void shouldRenderElseNodesWhenConditionIsFalseBoolean() {
            List<TemplateNode> thenNodes = List.of(new TextNode("Yes"));
            List<TemplateNode> elseNodes = List.of(new TextNode("No"));
            IfNode node = new IfNode("show", thenNodes, elseNodes);

            assertThat(node.render(Map.of("show", false))).isEqualTo("No");
        }

        @Test
        @DisplayName("Should render then nodes when condition is non-null")
        void shouldRenderThenNodesWhenConditionIsNonNull() {
            List<TemplateNode> thenNodes = List.of(new TextNode("Yes"));
            List<TemplateNode> elseNodes = List.of(new TextNode("No"));
            IfNode node = new IfNode("show", thenNodes, elseNodes);

            assertThat(node.render(Map.of("show", "anything"))).isEqualTo("Yes");
        }

        @Test
        @DisplayName("Should render else nodes when condition is null")
        void shouldRenderElseNodesWhenConditionIsNull() {
            List<TemplateNode> thenNodes = List.of(new TextNode("Yes"));
            List<TemplateNode> elseNodes = List.of(new TextNode("No"));
            IfNode node = new IfNode("show", thenNodes, elseNodes);

            assertThat(node.render(Map.of())).isEqualTo("No");
        }

        @Test
        @DisplayName("Should render multiple then nodes")
        void shouldRenderMultipleThenNodes() {
            List<TemplateNode> thenNodes = List.of(new TextNode("A"), new TextNode("B"));
            List<TemplateNode> elseNodes = List.of();
            IfNode node = new IfNode("show", thenNodes, elseNodes);

            assertThat(node.render(Map.of("show", true))).isEqualTo("AB");
        }

        @Test
        @DisplayName("Should render empty when else nodes empty and condition false")
        void shouldRenderEmptyWhenElseNodesEmptyAndConditionFalse() {
            List<TemplateNode> thenNodes = List.of(new TextNode("Yes"));
            List<TemplateNode> elseNodes = List.of();
            IfNode node = new IfNode("show", thenNodes, elseNodes);

            assertThat(node.render(Map.of("show", false))).isEmpty();
        }
    }
}
