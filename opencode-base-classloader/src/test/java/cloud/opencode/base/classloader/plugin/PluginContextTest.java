package cloud.opencode.base.classloader.plugin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for PluginContext record
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
@DisplayName("PluginContext Tests")
class PluginContextTest {

    private static final PluginDescriptor DESCRIPTOR = new PluginDescriptor(
            "test-plugin", "Test Plugin", "1.0.0", "com.example.TestPlugin", Path.of("/plugins/test.jar"));

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Valid construction succeeds")
        void validConstruction() {
            PluginContext context = new PluginContext("test-plugin", DESCRIPTOR);

            assertThat(context.pluginId()).isEqualTo("test-plugin");
            assertThat(context.descriptor()).isSameAs(DESCRIPTOR);
        }

        @Test
        @DisplayName("Null pluginId throws NullPointerException")
        void nullPluginIdThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new PluginContext(null, DESCRIPTOR))
                    .withMessageContaining("pluginId");
        }

        @Test
        @DisplayName("Null descriptor throws NullPointerException")
        void nullDescriptorThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new PluginContext("test", null))
                    .withMessageContaining("descriptor");
        }
    }

    @Nested
    @DisplayName("Record Behavior Tests")
    class RecordBehaviorTests {

        @Test
        @DisplayName("equals and hashCode work correctly")
        void equalsAndHashCode() {
            PluginContext ctx1 = new PluginContext("test-plugin", DESCRIPTOR);
            PluginContext ctx2 = new PluginContext("test-plugin", DESCRIPTOR);

            assertThat(ctx1).isEqualTo(ctx2);
            assertThat(ctx1.hashCode()).isEqualTo(ctx2.hashCode());
        }

        @Test
        @DisplayName("Different pluginId produces inequality")
        void differentPluginIdNotEqual() {
            PluginContext ctx1 = new PluginContext("plugin-a", DESCRIPTOR);
            PluginContext ctx2 = new PluginContext("plugin-b", DESCRIPTOR);

            assertThat(ctx1).isNotEqualTo(ctx2);
        }

        @Test
        @DisplayName("toString contains fields")
        void toStringContainsFields() {
            PluginContext context = new PluginContext("test-plugin", DESCRIPTOR);
            String str = context.toString();

            assertThat(str).contains("test-plugin");
        }
    }
}
