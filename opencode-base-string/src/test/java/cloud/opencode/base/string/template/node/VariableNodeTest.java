package cloud.opencode.base.string.template.node;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * VariableNodeTest Tests
 * VariableNodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("VariableNode Tests")
class VariableNodeTest {

    @Nested
    @DisplayName("render Tests")
    class RenderTests {

        @Test
        @DisplayName("Should render variable from context")
        void shouldRenderVariableFromContext() {
            VariableNode node = new VariableNode("name", null);
            assertThat(node.render(Map.of("name", "World"))).isEqualTo("World");
        }

        @Test
        @DisplayName("Should use default value when variable not in context")
        void shouldUseDefaultValueWhenVariableNotInContext() {
            VariableNode node = new VariableNode("name", "Guest");
            assertThat(node.render(Map.of())).isEqualTo("Guest");
        }

        @Test
        @DisplayName("Should return empty string when no default and not in context")
        void shouldReturnEmptyStringWhenNoDefaultAndNotInContext() {
            VariableNode node = new VariableNode("name", null);
            assertThat(node.render(Map.of())).isEmpty();
        }

        @Test
        @DisplayName("Should render non-string value as string")
        void shouldRenderNonStringValueAsString() {
            VariableNode node = new VariableNode("count", null);
            assertThat(node.render(Map.of("count", 42))).isEqualTo("42");
        }

        @Test
        @DisplayName("Should prefer context value over default")
        void shouldPreferContextValueOverDefault() {
            VariableNode node = new VariableNode("name", "Default");
            assertThat(node.render(Map.of("name", "Actual"))).isEqualTo("Actual");
        }
    }
}
