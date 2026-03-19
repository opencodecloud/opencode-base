package cloud.opencode.base.string.template.node;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TemplateNodeTest Tests
 * TemplateNodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("TemplateNode Tests")
class TemplateNodeTest {

    @Nested
    @DisplayName("TextNode Tests")
    class TextNodeTests {

        @Test
        @DisplayName("Should render static text")
        void shouldRenderStaticText() {
            TextNode node = new TextNode("Hello World");
            assertThat(node.render(Map.of())).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("Should render empty text")
        void shouldRenderEmptyText() {
            TextNode node = new TextNode("");
            assertThat(node.render(Map.of())).isEmpty();
        }

        @Test
        @DisplayName("Should render null as null")
        void shouldRenderNullAsNull() {
            TextNode node = new TextNode(null);
            assertThat(node.render(Map.of())).isNull();
        }

        @Test
        @DisplayName("Should ignore context")
        void shouldIgnoreContext() {
            TextNode node = new TextNode("Static");
            assertThat(node.render(Map.of("name", "World"))).isEqualTo("Static");
        }
    }

    @Nested
    @DisplayName("VariableNode Tests")
    class VariableNodeTests {

        @Test
        @DisplayName("Should render variable from context")
        void shouldRenderVariableFromContext() {
            VariableNode node = new VariableNode("name", null);
            assertThat(node.render(Map.of("name", "World"))).isEqualTo("World");
        }

        @Test
        @DisplayName("Should use default value when variable missing")
        void shouldUseDefaultValueWhenVariableMissing() {
            VariableNode node = new VariableNode("name", "Guest");
            assertThat(node.render(Map.of())).isEqualTo("Guest");
        }

        @Test
        @DisplayName("Should return empty when no value and no default")
        void shouldReturnEmptyWhenNoValueAndNoDefault() {
            VariableNode node = new VariableNode("name", null);
            assertThat(node.render(Map.of())).isEmpty();
        }

        @Test
        @DisplayName("Should convert non-string values to string")
        void shouldConvertNonStringValuesToString() {
            VariableNode node = new VariableNode("count", null);
            assertThat(node.render(Map.of("count", 42))).isEqualTo("42");
        }
    }

    @Nested
    @DisplayName("IfNode Tests")
    class IfNodeTests {

        @Test
        @DisplayName("Should render then branch when condition is true")
        void shouldRenderThenBranchWhenConditionIsTrue() {
            IfNode node = new IfNode("show",
                List.of(new TextNode("visible")),
                List.of(new TextNode("hidden")));
            assertThat(node.render(Map.of("show", true))).isEqualTo("visible");
        }

        @Test
        @DisplayName("Should render else branch when condition is false")
        void shouldRenderElseBranchWhenConditionIsFalse() {
            IfNode node = new IfNode("show",
                List.of(new TextNode("visible")),
                List.of(new TextNode("hidden")));
            assertThat(node.render(Map.of("show", false))).isEqualTo("hidden");
        }

        @Test
        @DisplayName("Should treat non-null value as true")
        void shouldTreatNonNullValueAsTrue() {
            IfNode node = new IfNode("value",
                List.of(new TextNode("yes")),
                List.of(new TextNode("no")));
            assertThat(node.render(Map.of("value", "anything"))).isEqualTo("yes");
        }

        @Test
        @DisplayName("Should treat null value as false")
        void shouldTreatNullValueAsFalse() {
            IfNode node = new IfNode("value",
                List.of(new TextNode("yes")),
                List.of(new TextNode("no")));
            assertThat(node.render(Map.of())).isEqualTo("no");
        }

        @Test
        @DisplayName("Should render multiple nodes in branch")
        void shouldRenderMultipleNodesInBranch() {
            IfNode node = new IfNode("show",
                List.of(new TextNode("Hello "), new TextNode("World")),
                List.of());
            assertThat(node.render(Map.of("show", true))).isEqualTo("Hello World");
        }
    }

    @Nested
    @DisplayName("ForNode Tests")
    class ForNodeTests {

        @Test
        @DisplayName("Should iterate over list")
        void shouldIterateOverList() {
            ForNode node = new ForNode("item", "items",
                List.of(new VariableNode("item", null), new TextNode(" ")));
            Map<String, Object> context = Map.of("items", List.of("a", "b", "c"));
            assertThat(node.render(context)).isEqualTo("a b c ");
        }

        @Test
        @DisplayName("Should provide index variable")
        void shouldProvideIndexVariable() {
            ForNode node = new ForNode("item", "items",
                List.of(new VariableNode("item_index", null), new TextNode(":")));
            Map<String, Object> context = Map.of("items", List.of("a", "b"));
            assertThat(node.render(context)).isEqualTo("0:1:");
        }

        @Test
        @DisplayName("Should provide first flag")
        void shouldProvideFirstFlag() {
            ForNode node = new ForNode("item", "items",
                List.of(new VariableNode("item_first", null), new TextNode(" ")));
            Map<String, Object> context = Map.of("items", List.of("a", "b"));
            assertThat(node.render(context)).isEqualTo("true false ");
        }

        @Test
        @DisplayName("Should return empty for non-iterable")
        void shouldReturnEmptyForNonIterable() {
            ForNode node = new ForNode("item", "items", List.of(new TextNode("x")));
            assertThat(node.render(Map.of("items", "not-iterable"))).isEmpty();
        }

        @Test
        @DisplayName("Should return empty for missing collection")
        void shouldReturnEmptyForMissingCollection() {
            ForNode node = new ForNode("item", "items", List.of(new TextNode("x")));
            assertThat(node.render(Map.of())).isEmpty();
        }
    }

    @Nested
    @DisplayName("IncludeNode Tests")
    class IncludeNodeTests {

        @Test
        @DisplayName("Should render include placeholder")
        void shouldRenderIncludePlaceholder() {
            IncludeNode node = new IncludeNode("header.tpl");
            assertThat(node.render(Map.of())).contains("header.tpl");
        }
    }
}
