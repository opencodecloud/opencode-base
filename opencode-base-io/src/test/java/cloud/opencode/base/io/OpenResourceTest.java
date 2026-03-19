package cloud.opencode.base.io;

import cloud.opencode.base.io.exception.OpenIOOperationException;
import cloud.opencode.base.io.resource.Resource;
import cloud.opencode.base.io.resource.ResourceLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenResource 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("OpenResource 测试")
class OpenResourceTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("getResource方法测试")
    class GetResourceTests {

        @Test
        @DisplayName("获取存在的资源URL")
        void testGetResourceExists() {
            // META-INF/MANIFEST.MF is a common classpath resource
            URL url = OpenResource.getResource("META-INF/MANIFEST.MF");

            // May or may not exist depending on classpath
            // Just test it doesn't throw
        }

        @Test
        @DisplayName("不存在的资源返回null")
        void testGetResourceNotExists() {
            URL url = OpenResource.getResource("nonexistent/resource/path123.txt");

            assertThat(url).isNull();
        }

        @Test
        @DisplayName("路径以/开头时移除斜杠")
        void testGetResourceLeadingSlash() {
            // Test that leading slash is handled
            URL url1 = OpenResource.getResource("/some/path");
            URL url2 = OpenResource.getResource("some/path");

            // Both should behave the same (both null for non-existent)
            assertThat(url1).isEqualTo(url2);
        }
    }

    @Nested
    @DisplayName("getResourceRequired方法测试")
    class GetResourceRequiredTests {

        @Test
        @DisplayName("不存在的资源抛出异常")
        void testGetResourceRequiredNotExists() {
            assertThatThrownBy(() -> OpenResource.getResourceRequired("nonexistent/path123.txt"))
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("getResources方法测试")
    class GetResourcesTests {

        @Test
        @DisplayName("返回资源URL列表")
        void testGetResources() {
            List<URL> urls = OpenResource.getResources("META-INF/MANIFEST.MF");

            // May be empty or have entries, just test it returns a list
            assertThat(urls).isNotNull();
        }

        @Test
        @DisplayName("不存在的资源返回空列表")
        void testGetResourcesNotExists() {
            List<URL> urls = OpenResource.getResources("nonexistent/path123.txt");

            assertThat(urls).isEmpty();
        }
    }

    @Nested
    @DisplayName("exists方法测试")
    class ExistsTests {

        @Test
        @DisplayName("不存在的资源返回false")
        void testExistsFalse() {
            boolean exists = OpenResource.exists("nonexistent/path123.txt");

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("getStream方法测试")
    class GetStreamTests {

        @Test
        @DisplayName("不存在的资源抛出异常")
        void testGetStreamNotExists() {
            assertThatThrownBy(() -> OpenResource.getStream("nonexistent/path123.txt"))
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("readBytes方法测试")
    class ReadBytesTests {

        @Test
        @DisplayName("不存在的资源抛出异常")
        void testReadBytesNotExists() {
            assertThatThrownBy(() -> OpenResource.readBytes("nonexistent/path123.txt"))
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("readString方法测试")
    class ReadStringTests {

        @Test
        @DisplayName("不存在的资源抛出异常")
        void testReadStringNotExists() {
            assertThatThrownBy(() -> OpenResource.readString("nonexistent/path123.txt"))
                .isInstanceOf(OpenIOOperationException.class);
        }

        @Test
        @DisplayName("使用指定字符集读取")
        void testReadStringWithCharset() {
            // This tests the method signature exists and works
            assertThatThrownBy(() -> OpenResource.readString("nonexistent.txt", StandardCharsets.UTF_8))
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("readLines方法测试")
    class ReadLinesTests {

        @Test
        @DisplayName("不存在的资源抛出异常")
        void testReadLinesNotExists() {
            assertThatThrownBy(() -> OpenResource.readLines("nonexistent/path123.txt"))
                .isInstanceOf(OpenIOOperationException.class);
        }

        @Test
        @DisplayName("使用指定字符集读取行")
        void testReadLinesWithCharset() {
            assertThatThrownBy(() -> OpenResource.readLines("nonexistent.txt", StandardCharsets.UTF_8))
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("readProperties方法测试")
    class ReadPropertiesTests {

        @Test
        @DisplayName("不存在的资源抛出异常")
        void testReadPropertiesNotExists() {
            assertThatThrownBy(() -> OpenResource.readProperties("nonexistent/path123.properties"))
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("classPathResource方法测试")
    class ClassPathResourceTests {

        @Test
        @DisplayName("创建类路径资源")
        void testClassPathResource() {
            Resource resource = OpenResource.classPathResource("test/path.txt");

            assertThat(resource).isNotNull();
            assertThat(resource.getDescription()).contains("test/path.txt");
        }
    }

    @Nested
    @DisplayName("fileResource方法测试")
    class FileResourceTests {

        @Test
        @DisplayName("从Path创建文件资源")
        void testFileResourcePath() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "content");

            Resource resource = OpenResource.fileResource(file);

            assertThat(resource).isNotNull();
            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("从字符串创建文件资源")
        void testFileResourceString() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "content");

            Resource resource = OpenResource.fileResource(file.toString());

            assertThat(resource).isNotNull();
            assertThat(resource.exists()).isTrue();
        }
    }

    @Nested
    @DisplayName("urlResource方法测试")
    class UrlResourceTests {

        @Test
        @DisplayName("从URL创建资源")
        void testUrlResourceUrl() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "content");
            URL url = file.toUri().toURL();

            Resource resource = OpenResource.urlResource(url);

            assertThat(resource).isNotNull();
            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("从字符串创建URL资源")
        void testUrlResourceString() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "content");

            Resource resource = OpenResource.urlResource(file.toUri().toString());

            assertThat(resource).isNotNull();
        }
    }

    @Nested
    @DisplayName("getResourceLoader方法测试")
    class GetResourceLoaderTests {

        @Test
        @DisplayName("返回资源加载器")
        void testGetResourceLoader() {
            ResourceLoader loader = OpenResource.getResourceLoader();

            assertThat(loader).isNotNull();
        }
    }

    @Nested
    @DisplayName("load方法测试")
    class LoadTests {

        @Test
        @DisplayName("加载类路径资源")
        void testLoadClasspath() {
            Resource resource = OpenResource.load("classpath:test/path.txt");

            assertThat(resource).isNotNull();
        }

        @Test
        @DisplayName("加载文件资源")
        void testLoadFile() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "content");

            Resource resource = OpenResource.load("file:" + file.toString());

            assertThat(resource).isNotNull();
            assertThat(resource.exists()).isTrue();
        }
    }
}
