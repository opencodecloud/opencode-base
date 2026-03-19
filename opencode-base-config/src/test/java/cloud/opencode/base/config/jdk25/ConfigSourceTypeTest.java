package cloud.opencode.base.config.jdk25;

import cloud.opencode.base.config.source.ConfigSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * ConfigSourceType 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ConfigSourceType 测试")
class ConfigSourceTypeTest {

    @Nested
    @DisplayName("File类型测试")
    class FileTypeTests {

        @Test
        @DisplayName("创建File类型 - 带监视")
        void testFileWithWatch() {
            Path path = Path.of("/tmp/config.properties");
            ConfigSourceType.File fileType = new ConfigSourceType.File(path, true);

            assertThat(fileType.path()).isEqualTo(path);
            assertThat(fileType.watchable()).isTrue();
        }

        @Test
        @DisplayName("创建File类型 - 默认监视")
        void testFileDefaultWatch() {
            Path path = Path.of("/tmp/config.properties");
            ConfigSourceType.File fileType = new ConfigSourceType.File(path);

            assertThat(fileType.watchable()).isTrue();
        }

        @Test
        @DisplayName("File转换为ConfigSource")
        void testFileToSource() {
            Path path = Path.of("/tmp/config.properties");
            ConfigSourceType fileType = new ConfigSourceType.File(path);

            ConfigSource source = fileType.toSource();

            assertThat(source).isNotNull();
        }
    }

    @Nested
    @DisplayName("Classpath类型测试")
    class ClasspathTypeTests {

        @Test
        @DisplayName("创建Classpath类型")
        void testClasspath() {
            ConfigSourceType.Classpath classpathType = new ConfigSourceType.Classpath("application.properties");

            assertThat(classpathType.resource()).isEqualTo("application.properties");
        }

        @Test
        @DisplayName("Classpath转换为ConfigSource")
        void testClasspathToSource() {
            ConfigSourceType classpathType = new ConfigSourceType.Classpath("test.properties");

            ConfigSource source = classpathType.toSource();

            assertThat(source).isNotNull();
        }
    }

    @Nested
    @DisplayName("Environment类型测试")
    class EnvironmentTypeTests {

        @Test
        @DisplayName("创建Environment类型 - 带前缀")
        void testEnvironmentWithPrefix() {
            ConfigSourceType.Environment envType = new ConfigSourceType.Environment("APP");

            assertThat(envType.prefix()).isEqualTo("APP");
        }

        @Test
        @DisplayName("创建Environment类型 - 无前缀")
        void testEnvironmentNoPrefix() {
            ConfigSourceType.Environment envType = new ConfigSourceType.Environment();

            assertThat(envType.prefix()).isNull();
        }

        @Test
        @DisplayName("Environment转换为ConfigSource")
        void testEnvironmentToSource() {
            ConfigSourceType envType = new ConfigSourceType.Environment("TEST");

            ConfigSource source = envType.toSource();

            assertThat(source).isNotNull();
        }
    }

    @Nested
    @DisplayName("System类型测试")
    class SystemTypeTests {

        @Test
        @DisplayName("创建System类型")
        void testSystem() {
            ConfigSourceType.System sysType = new ConfigSourceType.System();

            assertThat(sysType).isNotNull();
        }

        @Test
        @DisplayName("System转换为ConfigSource")
        void testSystemToSource() {
            ConfigSourceType sysType = new ConfigSourceType.System();

            ConfigSource source = sysType.toSource();

            assertThat(source).isNotNull();
            assertThat(source.getProperties()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("InMemory类型测试")
    class InMemoryTypeTests {

        @Test
        @DisplayName("创建InMemory类型")
        void testInMemory() {
            Map<String, String> props = Map.of("key", "value");
            ConfigSourceType.InMemory memType = new ConfigSourceType.InMemory(props);

            assertThat(memType.properties()).isEqualTo(props);
        }

        @Test
        @DisplayName("InMemory转换为ConfigSource")
        void testInMemoryToSource() {
            Map<String, String> props = Map.of("key", "value");
            ConfigSourceType memType = new ConfigSourceType.InMemory(props);

            ConfigSource source = memType.toSource();

            assertThat(source).isNotNull();
            assertThat(source.getProperty("key")).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("密封类型测试")
    class SealedTypeTests {

        @Test
        @DisplayName("接口是密封的")
        void testInterfaceIsSealed() {
            assertThat(ConfigSourceType.class.isSealed()).isTrue();
        }

        @Test
        @DisplayName("允许的子类型")
        void testPermittedSubclasses() {
            Class<?>[] permitted = ConfigSourceType.class.getPermittedSubclasses();

            assertThat(permitted).hasSize(5);
        }
    }
}
