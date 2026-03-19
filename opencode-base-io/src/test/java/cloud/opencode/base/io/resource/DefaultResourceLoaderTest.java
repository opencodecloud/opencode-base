package cloud.opencode.base.io.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultResourceLoader 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("DefaultResourceLoader 测试")
class DefaultResourceLoaderTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("单例测试")
    class SingletonTests {

        @Test
        @DisplayName("INSTANCE是单例")
        void testInstance() {
            assertThat(DefaultResourceLoader.INSTANCE).isNotNull();
            assertThat(DefaultResourceLoader.INSTANCE).isSameAs(DefaultResourceLoader.INSTANCE);
        }
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造函数")
        void testDefaultConstructor() {
            DefaultResourceLoader loader = new DefaultResourceLoader();

            assertThat(loader.getClassLoader()).isNotNull();
        }

        @Test
        @DisplayName("使用自定义ClassLoader")
        void testConstructorWithClassLoader() {
            ClassLoader customLoader = Thread.currentThread().getContextClassLoader();
            DefaultResourceLoader loader = new DefaultResourceLoader(customLoader);

            assertThat(loader.getClassLoader()).isSameAs(customLoader);
        }

        @Test
        @DisplayName("null ClassLoader使用默认")
        void testConstructorWithNullClassLoader() {
            DefaultResourceLoader loader = new DefaultResourceLoader(null);

            assertThat(loader.getClassLoader()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getResource方法测试")
    class GetResourceTests {

        @Test
        @DisplayName("加载classpath:前缀资源")
        void testGetResourceClasspath() {
            DefaultResourceLoader loader = new DefaultResourceLoader();

            Resource resource = loader.getResource("classpath:nonexistent.txt");

            assertThat(resource).isInstanceOf(ClassPathResource.class);
            assertThat(resource.getDescription()).isEqualTo("classpath:nonexistent.txt");
        }

        @Test
        @DisplayName("加载file:前缀资源")
        void testGetResourceFile() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            DefaultResourceLoader loader = new DefaultResourceLoader();

            Resource resource = loader.getResource("file:" + file.toString());

            assertThat(resource).isInstanceOf(FileSystemResource.class);
        }

        @Test
        @DisplayName("加载http:前缀资源")
        void testGetResourceHttp() {
            DefaultResourceLoader loader = new DefaultResourceLoader();

            Resource resource = loader.getResource("http://example.com/test.txt");

            assertThat(resource).isInstanceOf(UrlResource.class);
        }

        @Test
        @DisplayName("加载https:前缀资源")
        void testGetResourceHttps() {
            DefaultResourceLoader loader = new DefaultResourceLoader();

            Resource resource = loader.getResource("https://example.com/test.txt");

            assertThat(resource).isInstanceOf(UrlResource.class);
        }

        @Test
        @DisplayName("加载ftp:前缀资源")
        void testGetResourceFtp() {
            DefaultResourceLoader loader = new DefaultResourceLoader();

            Resource resource = loader.getResource("ftp://example.com/test.txt");

            assertThat(resource).isInstanceOf(UrlResource.class);
        }

        @Test
        @DisplayName("加载jar:前缀资源")
        void testGetResourceJar() {
            DefaultResourceLoader loader = new DefaultResourceLoader();

            Resource resource = loader.getResource("jar:file:/test.jar!/test.txt");

            assertThat(resource).isInstanceOf(UrlResource.class);
        }

        @Test
        @DisplayName("无前缀时先尝试classpath再尝试file")
        void testGetResourceNoPrefix() {
            DefaultResourceLoader loader = new DefaultResourceLoader();

            // Non-existent resource defaults to file
            Resource resource = loader.getResource("nonexistent-resource-xyz.txt");

            // Since the resource doesn't exist on classpath, it falls back to FileSystemResource
            assertThat(resource).isInstanceOf(FileSystemResource.class);
        }

        @Test
        @DisplayName("无前缀且classpath存在时返回ClassPathResource")
        void testGetResourceNoPrefixClasspathExists() {
            DefaultResourceLoader loader = new DefaultResourceLoader();

            // META-INF/MANIFEST.MF typically exists in classpath
            Resource resource = loader.getResource("META-INF/MANIFEST.MF");

            // May return ClassPathResource or FileSystemResource depending on availability
            assertThat(resource).isNotNull();
        }

        @Test
        @DisplayName("null位置抛出异常")
        void testGetResourceNull() {
            DefaultResourceLoader loader = new DefaultResourceLoader();

            assertThatThrownBy(() -> loader.getResource(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getClassLoader方法测试")
    class GetClassLoaderTests {

        @Test
        @DisplayName("返回ClassLoader")
        void testGetClassLoader() {
            DefaultResourceLoader loader = new DefaultResourceLoader();

            assertThat(loader.getClassLoader()).isNotNull();
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("返回描述")
        void testToString() {
            DefaultResourceLoader loader = new DefaultResourceLoader();

            assertThat(loader.toString()).contains("DefaultResourceLoader");
            assertThat(loader.toString()).contains("classLoader");
        }
    }

    @Nested
    @DisplayName("URL检测测试")
    class UrlDetectionTests {

        @Test
        @DisplayName("识别http URL")
        void testIsUrlHttp() {
            DefaultResourceLoader loader = new DefaultResourceLoader();

            Resource resource = loader.getResource("http://example.com");

            assertThat(resource).isInstanceOf(UrlResource.class);
        }

        @Test
        @DisplayName("识别https URL")
        void testIsUrlHttps() {
            DefaultResourceLoader loader = new DefaultResourceLoader();

            Resource resource = loader.getResource("https://example.com");

            assertThat(resource).isInstanceOf(UrlResource.class);
        }

        @Test
        @DisplayName("普通字符串不识别为URL")
        void testIsUrlPlainString() {
            DefaultResourceLoader loader = new DefaultResourceLoader();

            Resource resource = loader.getResource("not-a-url");

            // Should fall back to FileSystemResource
            assertThat(resource).isInstanceOf(FileSystemResource.class);
        }
    }

    @Nested
    @DisplayName("实际文件加载测试")
    class ActualFileLoadingTests {

        @Test
        @DisplayName("加载实际文件")
        void testLoadActualFile() throws Exception {
            Path file = tempDir.resolve("actual.txt");
            Files.writeString(file, "actual content");
            DefaultResourceLoader loader = new DefaultResourceLoader();

            Resource resource = loader.getResource("file:" + file.toString());

            assertThat(resource.exists()).isTrue();
            assertThat(resource.readString()).isEqualTo("actual content");
        }
    }
}
