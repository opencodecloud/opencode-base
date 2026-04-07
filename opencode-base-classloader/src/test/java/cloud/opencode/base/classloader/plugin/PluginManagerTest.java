package cloud.opencode.base.classloader.plugin;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;
import cloud.opencode.base.classloader.leak.LeakDetection;
import cloud.opencode.base.classloader.loader.IsoClassLoader;
import cloud.opencode.base.classloader.security.ClassLoadingPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for PluginManager
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
@DisplayName("PluginManager Tests")
class PluginManagerTest {

    @TempDir
    Path tempDir;

    /**
     * Create a plugin JAR containing a plugin.properties descriptor.
     * Note: The JAR will NOT contain a valid Plugin class, so load() will fail
     * with ClassNotFoundException. This is intentional for discovery-only tests.
     */
    private Path createPluginJar(String id, String name, String version, String mainClass)
            throws IOException {
        Path jarPath = tempDir.resolve(id + ".jar");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarPath.toFile()))) {
            // Write plugin.properties
            JarEntry entry = new JarEntry("META-INF/opencode/plugin.properties");
            jos.putNextEntry(entry);

            Properties props = new Properties();
            props.setProperty("plugin.id", id);
            props.setProperty("plugin.name", name);
            props.setProperty("plugin.version", version);
            props.setProperty("plugin.mainClass", mainClass);
            props.store(jos, null);
            jos.closeEntry();
        }
        return jarPath;
    }

    /**
     * Create a JAR without a plugin descriptor.
     */
    private Path createNonPluginJar() throws IOException {
        Path jarPath = tempDir.resolve("non-plugin.jar");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarPath.toFile()))) {
            JarEntry entry = new JarEntry("some-file.txt");
            jos.putNextEntry(entry);
            jos.write("hello".getBytes());
            jos.closeEntry();
        }
        return jarPath;
    }

    /**
     * Create a JAR with incomplete plugin.properties (missing required fields).
     */
    private Path createIncompletePluginJar() throws IOException {
        Path jarPath = tempDir.resolve("incomplete.jar");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarPath.toFile()))) {
            JarEntry entry = new JarEntry("META-INF/opencode/plugin.properties");
            jos.putNextEntry(entry);

            Properties props = new Properties();
            props.setProperty("plugin.id", "incomplete");
            // Missing name, version, mainClass
            props.store(jos, null);
            jos.closeEntry();
        }
        return jarPath;
    }

    private PluginManager createManager() {
        return PluginManager.builder()
                .pluginDir(tempDir)
                .build();
    }

    // ==================== Builder Tests ====================

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Build with valid pluginDir succeeds")
        void buildWithValidPluginDir() {
            PluginManager manager = PluginManager.builder()
                    .pluginDir(tempDir)
                    .build();

            assertThat(manager).isNotNull();
            manager.close();
        }

        @Test
        @DisplayName("Build without pluginDir throws exception")
        void buildWithoutPluginDirThrows() {
            assertThatThrownBy(() -> PluginManager.builder().build())
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("pluginDir");
        }

        @Test
        @DisplayName("Builder accepts all configuration options")
        void builderAcceptsAllOptions() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder().build();

            PluginManager manager = PluginManager.builder()
                    .pluginDir(tempDir)
                    .loadingStrategy(IsoClassLoader.LoadingStrategy.PARENT_FIRST)
                    .leakDetection(LeakDetection.SIMPLE)
                    .policy(policy)
                    .build();

            assertThat(manager).isNotNull();
            manager.close();
        }

        @Test
        @DisplayName("Builder rejects null pluginDir")
        void builderRejectsNullPluginDir() {
            assertThatNullPointerException()
                    .isThrownBy(() -> PluginManager.builder().pluginDir(null))
                    .withMessageContaining("pluginDir");
        }

        @Test
        @DisplayName("Builder rejects null loadingStrategy")
        void builderRejectsNullLoadingStrategy() {
            assertThatNullPointerException()
                    .isThrownBy(() -> PluginManager.builder().loadingStrategy(null))
                    .withMessageContaining("loadingStrategy");
        }

        @Test
        @DisplayName("Builder rejects null leakDetection")
        void builderRejectsNullLeakDetection() {
            assertThatNullPointerException()
                    .isThrownBy(() -> PluginManager.builder().leakDetection(null))
                    .withMessageContaining("leakDetection");
        }

        @Test
        @DisplayName("Builder rejects null policy")
        void builderRejectsNullPolicy() {
            assertThatNullPointerException()
                    .isThrownBy(() -> PluginManager.builder().policy(null))
                    .withMessageContaining("policy");
        }
    }

    // ==================== Discovery Tests ====================

    @Nested
    @DisplayName("Discovery Tests")
    class DiscoveryTests {

        @Test
        @DisplayName("Discover plugins from directory with valid JARs")
        void discoverValidPlugins() throws IOException {
            createPluginJar("auth", "Auth Plugin", "1.0.0", "com.example.AuthPlugin");
            createPluginJar("cache", "Cache Plugin", "2.0.0", "com.example.CachePlugin");

            try (PluginManager manager = createManager()) {
                List<PluginDescriptor> descriptors = manager.discoverPlugins();

                assertThat(descriptors).hasSize(2);
                assertThat(descriptors).extracting(PluginDescriptor::id)
                        .containsExactlyInAnyOrder("auth", "cache");
            }
        }

        @Test
        @DisplayName("Discover skips non-plugin JARs")
        void discoverSkipsNonPluginJars() throws IOException {
            createPluginJar("auth", "Auth Plugin", "1.0.0", "com.example.AuthPlugin");
            createNonPluginJar();

            try (PluginManager manager = createManager()) {
                List<PluginDescriptor> descriptors = manager.discoverPlugins();

                assertThat(descriptors).hasSize(1);
                assertThat(descriptors.getFirst().id()).isEqualTo("auth");
            }
        }

        @Test
        @DisplayName("Discover skips JARs with incomplete descriptors")
        void discoverSkipsIncompleteDescriptors() throws IOException {
            createPluginJar("auth", "Auth Plugin", "1.0.0", "com.example.AuthPlugin");
            createIncompletePluginJar();

            try (PluginManager manager = createManager()) {
                List<PluginDescriptor> descriptors = manager.discoverPlugins();

                assertThat(descriptors).hasSize(1);
            }
        }

        @Test
        @DisplayName("Discover with empty directory returns empty list")
        void discoverEmptyDirectory() {
            try (PluginManager manager = createManager()) {
                List<PluginDescriptor> descriptors = manager.discoverPlugins();

                assertThat(descriptors).isEmpty();
            }
        }

        @Test
        @DisplayName("Discover with non-existent directory throws exception")
        void discoverNonExistentDirectoryThrows() {
            PluginManager manager = PluginManager.builder()
                    .pluginDir(tempDir.resolve("nonexistent"))
                    .build();

            assertThatThrownBy(manager::discoverPlugins)
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("does not exist");
            manager.close();
        }

        @Test
        @DisplayName("Discover returns unmodifiable list")
        void discoverReturnsUnmodifiableList() throws IOException {
            createPluginJar("auth", "Auth Plugin", "1.0.0", "com.example.AuthPlugin");

            try (PluginManager manager = createManager()) {
                List<PluginDescriptor> descriptors = manager.discoverPlugins();

                assertThatThrownBy(() -> descriptors.add(null))
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        }

        @Test
        @DisplayName("Descriptor fields are correctly parsed")
        void descriptorFieldsParsed() throws IOException {
            Path jarPath = createPluginJar("my-plugin", "My Plugin", "3.2.1",
                    "com.example.MyPlugin");

            try (PluginManager manager = createManager()) {
                List<PluginDescriptor> descriptors = manager.discoverPlugins();

                assertThat(descriptors).hasSize(1);
                PluginDescriptor desc = descriptors.getFirst();
                assertThat(desc.id()).isEqualTo("my-plugin");
                assertThat(desc.name()).isEqualTo("My Plugin");
                assertThat(desc.version()).isEqualTo("3.2.1");
                assertThat(desc.mainClass()).isEqualTo("com.example.MyPlugin");
                assertThat(desc.jarPath()).isEqualTo(jarPath);
            }
        }
    }

    // ==================== Load Tests ====================

    @Nested
    @DisplayName("Load Tests")
    class LoadTests {

        @Test
        @DisplayName("Load undiscovered plugin throws exception")
        void loadUndiscoveredPluginThrows() {
            try (PluginManager manager = createManager()) {
                assertThatThrownBy(() -> manager.load("unknown"))
                        .isInstanceOf(OpenClassLoaderException.class)
                        .hasMessageContaining("not discovered");
            }
        }

        @Test
        @DisplayName("Load with null pluginId throws NullPointerException")
        void loadNullIdThrows() {
            try (PluginManager manager = createManager()) {
                assertThatNullPointerException()
                        .isThrownBy(() -> manager.load(null))
                        .withMessageContaining("pluginId");
            }
        }

        @Test
        @DisplayName("Load plugin with missing main class throws exception")
        void loadMissingMainClassThrows() throws IOException {
            createPluginJar("auth", "Auth Plugin", "1.0.0", "com.nonexistent.AuthPlugin");

            try (PluginManager manager = createManager()) {
                manager.discoverPlugins();

                assertThatThrownBy(() -> manager.load("auth"))
                        .isInstanceOf(OpenClassLoaderException.class)
                        .hasMessageContaining("Failed to load plugin");
            }
        }
    }

    // ==================== Start Tests ====================

    @Nested
    @DisplayName("Start Tests")
    class StartTests {

        @Test
        @DisplayName("Start unloaded plugin throws exception")
        void startUnloadedPluginThrows() {
            try (PluginManager manager = createManager()) {
                assertThatThrownBy(() -> manager.start("unknown"))
                        .isInstanceOf(OpenClassLoaderException.class)
                        .hasMessageContaining("not loaded");
            }
        }

        @Test
        @DisplayName("Start with null pluginId throws NullPointerException")
        void startNullIdThrows() {
            try (PluginManager manager = createManager()) {
                assertThatNullPointerException()
                        .isThrownBy(() -> manager.start(null))
                        .withMessageContaining("pluginId");
            }
        }
    }

    // ==================== Stop Tests ====================

    @Nested
    @DisplayName("Stop Tests")
    class StopTests {

        @Test
        @DisplayName("Stop unloaded plugin throws exception")
        void stopUnloadedPluginThrows() {
            try (PluginManager manager = createManager()) {
                assertThatThrownBy(() -> manager.stop("unknown"))
                        .isInstanceOf(OpenClassLoaderException.class)
                        .hasMessageContaining("not loaded");
            }
        }

        @Test
        @DisplayName("Stop with null pluginId throws NullPointerException")
        void stopNullIdThrows() {
            try (PluginManager manager = createManager()) {
                assertThatNullPointerException()
                        .isThrownBy(() -> manager.stop(null))
                        .withMessageContaining("pluginId");
            }
        }
    }

    // ==================== Unload Tests ====================

    @Nested
    @DisplayName("Unload Tests")
    class UnloadTests {

        @Test
        @DisplayName("Unload non-existent plugin throws exception")
        void unloadNonExistentThrows() {
            try (PluginManager manager = createManager()) {
                assertThatThrownBy(() -> manager.unload("unknown"))
                        .isInstanceOf(OpenClassLoaderException.class)
                        .hasMessageContaining("not loaded");
            }
        }

        @Test
        @DisplayName("Unload with null pluginId throws NullPointerException")
        void unloadNullIdThrows() {
            try (PluginManager manager = createManager()) {
                assertThatNullPointerException()
                        .isThrownBy(() -> manager.unload(null))
                        .withMessageContaining("pluginId");
            }
        }
    }

    // ==================== Reload Tests ====================

    @Nested
    @DisplayName("Reload Tests")
    class ReloadTests {

        @Test
        @DisplayName("Reload undiscovered plugin throws exception")
        void reloadUndiscoveredThrows() {
            try (PluginManager manager = createManager()) {
                assertThatThrownBy(() -> manager.reload("unknown"))
                        .isInstanceOf(OpenClassLoaderException.class)
                        .hasMessageContaining("not discovered");
            }
        }

        @Test
        @DisplayName("Reload with null pluginId throws NullPointerException")
        void reloadNullIdThrows() {
            try (PluginManager manager = createManager()) {
                assertThatNullPointerException()
                        .isThrownBy(() -> manager.reload(null))
                        .withMessageContaining("pluginId");
            }
        }
    }

    // ==================== Query Tests ====================

    @Nested
    @DisplayName("Query Tests")
    class QueryTests {

        @Test
        @DisplayName("getPlugin returns empty for unknown id")
        void getPluginReturnsEmpty() {
            try (PluginManager manager = createManager()) {
                Optional<PluginHandle> result = manager.getPlugin("unknown");

                assertThat(result).isEmpty();
            }
        }

        @Test
        @DisplayName("getPlugin with null id throws NullPointerException")
        void getPluginNullIdThrows() {
            try (PluginManager manager = createManager()) {
                assertThatNullPointerException()
                        .isThrownBy(() -> manager.getPlugin(null))
                        .withMessageContaining("pluginId");
            }
        }

        @Test
        @DisplayName("getPlugins returns empty map initially")
        void getPluginsReturnsEmptyMap() {
            try (PluginManager manager = createManager()) {
                Map<String, PluginHandle> plugins = manager.getPlugins();

                assertThat(plugins).isEmpty();
            }
        }

        @Test
        @DisplayName("getPlugins returns unmodifiable map")
        void getPluginsReturnsUnmodifiableMap() {
            try (PluginManager manager = createManager()) {
                Map<String, PluginHandle> plugins = manager.getPlugins();

                assertThatThrownBy(() -> plugins.put("x", null))
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        }
    }

    // ==================== Close Tests ====================

    @Nested
    @DisplayName("Close Tests")
    class CloseTests {

        @Test
        @DisplayName("Close is idempotent")
        void closeIsIdempotent() {
            PluginManager manager = createManager();
            manager.close();
            assertThatCode(manager::close).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Operations on closed manager throw exception")
        void operationsOnClosedManagerThrow() {
            PluginManager manager = createManager();
            manager.close();

            assertThatThrownBy(manager::discoverPlugins)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("closed");

            assertThatThrownBy(() -> manager.load("x"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("closed");

            assertThatThrownBy(() -> manager.start("x"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("closed");

            assertThatThrownBy(() -> manager.stop("x"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("closed");

            assertThatThrownBy(() -> manager.unload("x"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("closed");

            assertThatThrownBy(() -> manager.reload("x"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("closed");
        }
    }

    // ==================== Non-JAR file Tests ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Non-jar files in plugin directory are ignored")
        void nonJarFilesIgnored() throws IOException {
            Files.writeString(tempDir.resolve("readme.txt"), "hello");
            Files.writeString(tempDir.resolve("config.xml"), "<config/>");

            try (PluginManager manager = createManager()) {
                List<PluginDescriptor> descriptors = manager.discoverPlugins();
                assertThat(descriptors).isEmpty();
            }
        }

        @Test
        @DisplayName("Multiple discover calls update discovered map")
        void multipleDiscoverCallsWork() throws IOException {
            try (PluginManager manager = createManager()) {
                // First discover: empty
                List<PluginDescriptor> first = manager.discoverPlugins();
                assertThat(first).isEmpty();

                // Add a plugin JAR
                createPluginJar("auth", "Auth", "1.0", "com.Auth");

                // Second discover: finds the new plugin
                List<PluginDescriptor> second = manager.discoverPlugins();
                assertThat(second).hasSize(1);
            }
        }
    }
}
