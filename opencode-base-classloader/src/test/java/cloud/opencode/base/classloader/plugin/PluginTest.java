package cloud.opencode.base.classloader.plugin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for Plugin interface
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
@DisplayName("Plugin Tests")
class PluginTest {

    @Nested
    @DisplayName("Default Methods Tests")
    class DefaultMethodsTests {

        @Test
        @DisplayName("onStart default implementation does nothing")
        void onStartDefaultDoesNothing() {
            Plugin plugin = new Plugin() {};
            PluginDescriptor descriptor = new PluginDescriptor(
                    "test", "Test", "1.0", "com.Test", Path.of("/test.jar"));
            PluginContext context = new PluginContext("test", descriptor);

            assertThatCode(() -> plugin.onStart(context))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("onStop default implementation does nothing")
        void onStopDefaultDoesNothing() {
            Plugin plugin = new Plugin() {};

            assertThatCode(plugin::onStop)
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Custom Implementation Tests")
    class CustomImplementationTests {

        @Test
        @DisplayName("Custom plugin receives context on start")
        void customPluginReceivesContext() {
            var holder = new Object() { String receivedId = null; };

            Plugin plugin = new Plugin() {
                @Override
                public void onStart(PluginContext context) {
                    holder.receivedId = context.pluginId();
                }
            };

            PluginDescriptor descriptor = new PluginDescriptor(
                    "my-plugin", "My Plugin", "2.0", "com.My", Path.of("/my.jar"));
            PluginContext context = new PluginContext("my-plugin", descriptor);
            plugin.onStart(context);

            assertThat(holder.receivedId).isEqualTo("my-plugin");
        }

        @Test
        @DisplayName("Custom plugin onStop is called")
        void customPluginOnStopCalled() {
            var holder = new Object() { boolean stopped = false; };

            Plugin plugin = new Plugin() {
                @Override
                public void onStop() {
                    holder.stopped = true;
                }
            };

            plugin.onStop();
            assertThat(holder.stopped).isTrue();
        }
    }
}
