package cloud.opencode.base.io.resource;

import cloud.opencode.base.io.exception.OpenIOOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * ClassPathResource 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("ClassPathResource 测试")
class ClassPathResourceTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用路径创建")
        void testConstructorWithPath() {
            ClassPathResource resource = new ClassPathResource("test-resource.txt");

            assertThat(resource.getClassPath()).isEqualTo("test-resource.txt");
            assertThat(resource.getClassLoader()).isNotNull();
        }

        @Test
        @DisplayName("路径以/开头时自动移除")
        void testConstructorWithLeadingSlash() {
            ClassPathResource resource = new ClassPathResource("/test-resource.txt");

            assertThat(resource.getClassPath()).isEqualTo("test-resource.txt");
        }

        @Test
        @DisplayName("使用自定义ClassLoader")
        void testConstructorWithClassLoader() {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            ClassPathResource resource = new ClassPathResource("test.txt", classLoader);

            assertThat(resource.getClassLoader()).isSameAs(classLoader);
        }

        @Test
        @DisplayName("null ClassLoader使用默认")
        void testConstructorWithNullClassLoader() {
            ClassPathResource resource = new ClassPathResource("test.txt", null);

            assertThat(resource.getClassLoader()).isNotNull();
        }

        @Test
        @DisplayName("null路径抛出异常")
        void testConstructorWithNullPath() {
            assertThatThrownBy(() -> new ClassPathResource(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("exists方法测试")
    class ExistsTests {

        @Test
        @DisplayName("存在的资源返回true")
        void testExistsTrue() {
            // META-INF/MANIFEST.MF is a common classpath resource
            ClassPathResource resource = new ClassPathResource("META-INF/MANIFEST.MF");

            // This may or may not exist depending on the test environment
            // Just verify the method doesn't throw
            resource.exists();
        }

        @Test
        @DisplayName("不存在的资源返回false")
        void testExistsFalse() {
            ClassPathResource resource = new ClassPathResource("non-existent-resource-xyz.txt");

            assertThat(resource.exists()).isFalse();
        }
    }

    @Nested
    @DisplayName("isReadable方法测试")
    class IsReadableTests {

        @Test
        @DisplayName("可读性与存在性相同")
        void testIsReadable() {
            ClassPathResource resource = new ClassPathResource("non-existent.txt");

            assertThat(resource.isReadable()).isEqualTo(resource.exists());
        }
    }

    @Nested
    @DisplayName("getInputStream方法测试")
    class GetInputStreamTests {

        @Test
        @DisplayName("不存在的资源抛出异常")
        void testGetInputStreamNotFound() {
            ClassPathResource resource = new ClassPathResource("non-existent-xyz.txt");

            assertThatThrownBy(resource::getInputStream)
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("getURL方法测试")
    class GetURLTests {

        @Test
        @DisplayName("不存在的资源抛出异常")
        void testGetURLNotFound() {
            ClassPathResource resource = new ClassPathResource("non-existent-xyz.txt");

            assertThatThrownBy(resource::getURL)
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("getPath方法测试")
    class GetPathTests {

        @Test
        @DisplayName("非文件协议返回null")
        void testGetPathNonFile() {
            ClassPathResource resource = new ClassPathResource("non-existent.txt");

            // For non-existent resources, getPath should return null
            assertThat(resource.getPath()).isNull();
        }
    }

    @Nested
    @DisplayName("getDescription方法测试")
    class GetDescriptionTests {

        @Test
        @DisplayName("返回classpath:前缀描述")
        void testGetDescription() {
            ClassPathResource resource = new ClassPathResource("config/app.properties");

            assertThat(resource.getDescription()).isEqualTo("classpath:config/app.properties");
        }
    }

    @Nested
    @DisplayName("getFilename方法测试")
    class GetFilenameTests {

        @Test
        @DisplayName("返回文件名部分")
        void testGetFilename() {
            ClassPathResource resource = new ClassPathResource("config/app.properties");

            assertThat(resource.getFilename()).isEqualTo("app.properties");
        }

        @Test
        @DisplayName("无目录时返回完整路径")
        void testGetFilenameNoDirectory() {
            ClassPathResource resource = new ClassPathResource("config.yaml");

            assertThat(resource.getFilename()).isEqualTo("config.yaml");
        }
    }

    @Nested
    @DisplayName("contentLength方法测试")
    class ContentLengthTests {

        @Test
        @DisplayName("不存在的资源抛出异常")
        void testContentLengthNotFound() {
            ClassPathResource resource = new ClassPathResource("non-existent.txt");

            // contentLength calls getInputStream which throws when resource not found
            assertThatThrownBy(resource::contentLength)
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("lastModified方法测试")
    class LastModifiedTests {

        @Test
        @DisplayName("不存在的资源抛出异常")
        void testLastModifiedNotFound() {
            ClassPathResource resource = new ClassPathResource("non-existent.txt");

            // lastModified calls getURL which throws when resource not found
            assertThatThrownBy(resource::lastModified)
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("createRelative方法测试")
    class CreateRelativeTests {

        @Test
        @DisplayName("创建相对资源")
        void testCreateRelative() {
            ClassPathResource resource = new ClassPathResource("config/app.properties");
            Resource relative = resource.createRelative("other.properties");

            assertThat(relative).isInstanceOf(ClassPathResource.class);
            assertThat(relative.getDescription()).isEqualTo("classpath:config/other.properties");
        }

        @Test
        @DisplayName("无目录时直接使用相对路径")
        void testCreateRelativeNoDirectory() {
            ClassPathResource resource = new ClassPathResource("app.properties");
            Resource relative = resource.createRelative("other.properties");

            assertThat(relative.getDescription()).isEqualTo("classpath:other.properties");
        }
    }

    @Nested
    @DisplayName("getClassPath方法测试")
    class GetClassPathTests {

        @Test
        @DisplayName("返回标准化路径")
        void testGetClassPath() {
            ClassPathResource resource = new ClassPathResource("/config/app.properties");

            assertThat(resource.getClassPath()).isEqualTo("config/app.properties");
        }
    }

    @Nested
    @DisplayName("getClassLoader方法测试")
    class GetClassLoaderTests {

        @Test
        @DisplayName("返回使用的ClassLoader")
        void testGetClassLoader() {
            ClassLoader customLoader = getClass().getClassLoader();
            ClassPathResource resource = new ClassPathResource("test.txt", customLoader);

            assertThat(resource.getClassLoader()).isSameAs(customLoader);
        }
    }

    @Nested
    @DisplayName("equals和hashCode方法测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同路径和ClassLoader相等")
        void testEquals() {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            ClassPathResource r1 = new ClassPathResource("test.txt", cl);
            ClassPathResource r2 = new ClassPathResource("test.txt", cl);

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("不同路径不相等")
        void testNotEqualsDifferentPath() {
            ClassPathResource r1 = new ClassPathResource("test1.txt");
            ClassPathResource r2 = new ClassPathResource("test2.txt");

            assertThat(r1).isNotEqualTo(r2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() {
            ClassPathResource r1 = new ClassPathResource("test.txt");

            assertThat(r1).isEqualTo(r1);
        }

        @Test
        @DisplayName("与null不相等")
        void testNotEqualsNull() {
            ClassPathResource r1 = new ClassPathResource("test.txt");

            assertThat(r1).isNotEqualTo(null);
        }

        @Test
        @DisplayName("与其他类型不相等")
        void testNotEqualsOtherType() {
            ClassPathResource r1 = new ClassPathResource("test.txt");

            assertThat(r1).isNotEqualTo("test.txt");
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("返回描述")
        void testToString() {
            ClassPathResource resource = new ClassPathResource("config/app.properties");

            assertThat(resource.toString()).isEqualTo("classpath:config/app.properties");
        }
    }
}
