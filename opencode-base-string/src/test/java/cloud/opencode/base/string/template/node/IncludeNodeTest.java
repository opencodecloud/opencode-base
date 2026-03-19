package cloud.opencode.base.string.template.node;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * IncludeNodeTest Tests
 * IncludeNodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("IncludeNode Tests")
class IncludeNodeTest {

    @Nested
    @DisplayName("render Tests")
    class RenderTests {

        @Test
        @DisplayName("Should render include placeholder")
        void shouldRenderIncludePlaceholder() {
            IncludeNode node = new IncludeNode("header.html");
            String result = node.render(Map.of());
            assertThat(result).contains("include").contains("header.html");
        }

        @Test
        @DisplayName("Should include template name in output")
        void shouldIncludeTemplateNameInOutput() {
            IncludeNode node = new IncludeNode("footer.html");
            String result = node.render(Map.of("any", "context"));
            assertThat(result).contains("footer.html");
        }
    }
}
