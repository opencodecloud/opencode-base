package cloud.opencode.base.classloader.plugin;

import cloud.opencode.base.classloader.loader.IsoClassLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.jar.JarOutputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for PluginHandle
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
@DisplayName("PluginHandle Tests")
class PluginHandleTest {

    @TempDir
    Path tempDir;

    private IsoClassLoader createTestClassLoader() throws IOException {
        Path jarPath = tempDir.resolve("test.jar");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarPath.toFile()))) {
            // Empty JAR is sufficient for handle tests
        }
        return IsoClassLoader.fromJar(jarPath).build();
    }

    private PluginDescriptor createDescriptor() {
        return new PluginDescriptor(
                "test-plugin", "Test Plugin", "1.0.0",
                "com.example.TestPlugin", Path.of("/test.jar"));
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Valid construction succeeds")
        void validConstruction() throws IOException {
            IsoClassLoader cl = createTestClassLoader();
            try {
                Plugin plugin = new Plugin() {};
                PluginDescriptor desc = createDescriptor();

                PluginHandle handle = new PluginHandle(
                        "test-plugin", desc, cl, plugin, PluginState.LOADED);

                assertThat(handle.getPluginId()).isEqualTo("test-plugin");
                assertThat(handle.getDescriptor()).isSameAs(desc);
                assertThat(handle.getClassLoader()).isSameAs(cl);
                assertThat(handle.getPlugin()).isSameAs(plugin);
                assertThat(handle.getState()).isEqualTo(PluginState.LOADED);
            } finally {
                cl.close();
            }
        }

        @Test
        @DisplayName("Null pluginId throws NullPointerException")
        void nullPluginIdThrows() throws IOException {
            IsoClassLoader cl = createTestClassLoader();
            try {
                assertThatNullPointerException()
                        .isThrownBy(() -> new PluginHandle(
                                null, createDescriptor(), cl, new Plugin() {}, PluginState.LOADED))
                        .withMessageContaining("pluginId");
            } finally {
                cl.close();
            }
        }

        @Test
        @DisplayName("Null descriptor throws NullPointerException")
        void nullDescriptorThrows() throws IOException {
            IsoClassLoader cl = createTestClassLoader();
            try {
                assertThatNullPointerException()
                        .isThrownBy(() -> new PluginHandle(
                                "id", null, cl, new Plugin() {}, PluginState.LOADED))
                        .withMessageContaining("descriptor");
            } finally {
                cl.close();
            }
        }

        @Test
        @DisplayName("Null classLoader throws NullPointerException")
        void nullClassLoaderThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new PluginHandle(
                            "id", createDescriptor(), null, new Plugin() {}, PluginState.LOADED))
                    .withMessageContaining("classLoader");
        }

        @Test
        @DisplayName("Null plugin throws NullPointerException")
        void nullPluginThrows() throws IOException {
            IsoClassLoader cl = createTestClassLoader();
            try {
                assertThatNullPointerException()
                        .isThrownBy(() -> new PluginHandle(
                                "id", createDescriptor(), cl, null, PluginState.LOADED))
                        .withMessageContaining("plugin");
            } finally {
                cl.close();
            }
        }

        @Test
        @DisplayName("Null state throws NullPointerException")
        void nullStateThrows() throws IOException {
            IsoClassLoader cl = createTestClassLoader();
            try {
                assertThatNullPointerException()
                        .isThrownBy(() -> new PluginHandle(
                                "id", createDescriptor(), cl, new Plugin() {}, null))
                        .withMessageContaining("state");
            } finally {
                cl.close();
            }
        }
    }

    @Nested
    @DisplayName("State Transition Tests")
    class StateTransitionTests {

        @Test
        @DisplayName("setState changes state")
        void setStateChangesState() throws IOException {
            IsoClassLoader cl = createTestClassLoader();
            try {
                PluginHandle handle = new PluginHandle(
                        "test", createDescriptor(), cl, new Plugin() {}, PluginState.LOADED);

                handle.setState(PluginState.STARTED);
                assertThat(handle.getState()).isEqualTo(PluginState.STARTED);

                handle.setState(PluginState.STOPPED);
                assertThat(handle.getState()).isEqualTo(PluginState.STOPPED);

                handle.setState(PluginState.UNLOADED);
                assertThat(handle.getState()).isEqualTo(PluginState.UNLOADED);
            } finally {
                cl.close();
            }
        }

        @Test
        @DisplayName("setState with null throws NullPointerException")
        void setStateNullThrows() throws IOException {
            IsoClassLoader cl = createTestClassLoader();
            try {
                PluginHandle handle = new PluginHandle(
                        "test", createDescriptor(), cl, new Plugin() {}, PluginState.LOADED);

                assertThatNullPointerException()
                        .isThrownBy(() -> handle.setState(null))
                        .withMessageContaining("state");
            } finally {
                cl.close();
            }
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString contains pluginId and state")
        void toStringContainsFields() throws IOException {
            IsoClassLoader cl = createTestClassLoader();
            try {
                PluginHandle handle = new PluginHandle(
                        "my-plugin", createDescriptor(), cl, new Plugin() {}, PluginState.STARTED);

                String str = handle.toString();
                assertThat(str).contains("my-plugin");
                assertThat(str).contains("STARTED");
            } finally {
                cl.close();
            }
        }
    }
}
