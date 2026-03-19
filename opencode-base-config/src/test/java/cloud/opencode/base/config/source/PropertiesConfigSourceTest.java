package cloud.opencode.base.config.source;

import cloud.opencode.base.config.OpenConfigException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * PropertiesConfigSource 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("PropertiesConfigSource 测试")
class PropertiesConfigSourceTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("类路径资源测试")
    class ClasspathResourceTests {

        @Test
        @DisplayName("加载不存在的类路径资源 - 空配置")
        void testLoadNonExistentClasspathResource() {
            PropertiesConfigSource source = new PropertiesConfigSource(
                    "nonexistent-" + System.currentTimeMillis() + ".properties", true);

            assertThat(source.getProperties()).isEmpty();
        }

        @Test
        @DisplayName("类路径资源不支持热重载")
        void testClasspathResourceNoReload() {
            PropertiesConfigSource source = new PropertiesConfigSource("test.properties", true);

            assertThat(source.supportsReload()).isFalse();
        }
    }

    @Nested
    @DisplayName("文件系统资源测试")
    class FileSystemResourceTests {

        @Test
        @DisplayName("从文件路径加载")
        void testLoadFromFilePath() throws IOException {
            Path propsFile = tempDir.resolve("test.properties");
            Files.writeString(propsFile, "key1=value1\nkey2=value2");

            PropertiesConfigSource source = new PropertiesConfigSource(propsFile);

            assertThat(source.getProperty("key1")).isEqualTo("value1");
            assertThat(source.getProperty("key2")).isEqualTo("value2");
        }

        @Test
        @DisplayName("从字符串路径加载")
        void testLoadFromStringPath() throws IOException {
            Path propsFile = tempDir.resolve("string-path.properties");
            Files.writeString(propsFile, "app.name=TestApp");

            PropertiesConfigSource source = new PropertiesConfigSource(propsFile.toString(), false);

            assertThat(source.getProperty("app.name")).isEqualTo("TestApp");
        }

        @Test
        @DisplayName("文件不存在 - 空配置")
        void testFileNotExists() {
            Path nonExistent = tempDir.resolve("nonexistent.properties");

            PropertiesConfigSource source = new PropertiesConfigSource(nonExistent);

            assertThat(source.getProperties()).isEmpty();
        }

        @Test
        @DisplayName("文件系统资源支持热重载")
        void testFileSystemSupportsReload() throws IOException {
            Path propsFile = tempDir.resolve("reload.properties");
            Files.writeString(propsFile, "key=value");

            PropertiesConfigSource source = new PropertiesConfigSource(propsFile);

            assertThat(source.supportsReload()).isTrue();
        }
    }

    @Nested
    @DisplayName("ConfigSource接口测试")
    class ConfigSourceInterfaceTests {

        @Test
        @DisplayName("getName - 返回资源名称")
        void testGetName() throws IOException {
            Path propsFile = tempDir.resolve("named.properties");
            Files.writeString(propsFile, "key=value");

            PropertiesConfigSource source = new PropertiesConfigSource(propsFile);

            assertThat(source.getName()).isEqualTo(propsFile.toString());
        }

        @Test
        @DisplayName("getPriority - 返回50")
        void testGetPriority() throws IOException {
            Path propsFile = tempDir.resolve("priority.properties");
            Files.writeString(propsFile, "key=value");

            PropertiesConfigSource source = new PropertiesConfigSource(propsFile);

            assertThat(source.getPriority()).isEqualTo(50);
        }

        @Test
        @DisplayName("getProperties - 返回不可变映射")
        void testGetPropertiesImmutable() throws IOException {
            Path propsFile = tempDir.resolve("immutable.properties");
            Files.writeString(propsFile, "key=value");

            PropertiesConfigSource source = new PropertiesConfigSource(propsFile);
            Map<String, String> props = source.getProperties();

            assertThatThrownBy(() -> props.put("new", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("热重载测试")
    class HotReloadTests {

        @Test
        @DisplayName("reload - 检测文件变更")
        void testReloadDetectsChanges() throws Exception {
            Path propsFile = tempDir.resolve("hot-reload.properties");
            Files.writeString(propsFile, "key=original");

            PropertiesConfigSource source = new PropertiesConfigSource(propsFile);
            assertThat(source.getProperty("key")).isEqualTo("original");

            // 修改文件
            Thread.sleep(100); // 确保文件修改时间不同
            Files.writeString(propsFile, "key=updated");

            // 重载
            source.reload();

            assertThat(source.getProperty("key")).isEqualTo("updated");
        }

        @Test
        @DisplayName("reload - 文件未变更不重载")
        void testReloadNoChangeSkipsReload() throws IOException {
            Path propsFile = tempDir.resolve("no-change.properties");
            Files.writeString(propsFile, "key=value");

            PropertiesConfigSource source = new PropertiesConfigSource(propsFile);
            Map<String, String> beforeReload = source.getProperties();

            source.reload();

            // 属性应该保持不变
            assertThat(source.getProperties()).isEqualTo(beforeReload);
        }

        @Test
        @DisplayName("reload - 类路径资源忽略重载")
        void testReloadClasspathResourceIgnored() {
            PropertiesConfigSource source = new PropertiesConfigSource("test.properties", true);

            // 不应该抛出异常
            source.reload();

            assertThat(source.supportsReload()).isFalse();
        }
    }

    @Nested
    @DisplayName("属性格式测试")
    class PropertyFormatTests {

        @Test
        @DisplayName("支持等号分隔符")
        void testEqualsDelimiter() throws IOException {
            Path propsFile = tempDir.resolve("equals.properties");
            Files.writeString(propsFile, "key=value");

            PropertiesConfigSource source = new PropertiesConfigSource(propsFile);

            assertThat(source.getProperty("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("支持多行值")
        void testMultipleProperties() throws IOException {
            Path propsFile = tempDir.resolve("multi.properties");
            Files.writeString(propsFile, """
                    key1=value1
                    key2=value2
                    key3=value3
                    """);

            PropertiesConfigSource source = new PropertiesConfigSource(propsFile);

            assertThat(source.getProperties()).hasSize(3);
        }

        @Test
        @DisplayName("支持空值")
        void testEmptyValue() throws IOException {
            Path propsFile = tempDir.resolve("empty.properties");
            Files.writeString(propsFile, "empty.key=");

            PropertiesConfigSource source = new PropertiesConfigSource(propsFile);

            assertThat(source.getProperty("empty.key")).isEmpty();
        }
    }
}
