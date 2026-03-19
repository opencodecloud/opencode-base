package cloud.opencode.base.reflect.scan;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.assertj.core.api.Assertions.*;

/**
 * ResourceInfoTest Tests
 * ResourceInfoTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("ResourceInfo 测试")
class ResourceInfoTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建ResourceInfo")
        void testCreate() {
            ResourceInfo resource = new ResourceInfo("META-INF/MANIFEST.MF", ClassLoader.getSystemClassLoader());
            assertThat(resource).isNotNull();
        }

        @Test
        @DisplayName("null资源名抛出异常")
        void testCreateNullResourceName() {
            assertThatThrownBy(() -> new ResourceInfo(null, ClassLoader.getSystemClassLoader()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null类加载器抛出异常")
        void testCreateNullClassLoader() {
            assertThatThrownBy(() -> new ResourceInfo("test.txt", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getResourceName方法测试")
    class GetResourceNameTests {

        @Test
        @DisplayName("获取资源名")
        void testGetResourceName() {
            ResourceInfo resource = new ResourceInfo("META-INF/MANIFEST.MF", ClassLoader.getSystemClassLoader());
            assertThat(resource.getResourceName()).isEqualTo("META-INF/MANIFEST.MF");
        }
    }

    @Nested
    @DisplayName("getSimpleName方法测试")
    class GetSimpleNameTests {

        @Test
        @DisplayName("获取简单名称")
        void testGetSimpleName() {
            ResourceInfo resource = new ResourceInfo("META-INF/MANIFEST.MF", ClassLoader.getSystemClassLoader());
            assertThat(resource.getSimpleName()).isEqualTo("MANIFEST.MF");
        }

        @Test
        @DisplayName("无路径的资源")
        void testGetSimpleNameNoPath() {
            ResourceInfo resource = new ResourceInfo("test.txt", ClassLoader.getSystemClassLoader());
            assertThat(resource.getSimpleName()).isEqualTo("test.txt");
        }
    }

    @Nested
    @DisplayName("getPackageName方法测试")
    class GetPackageNameTests {

        @Test
        @DisplayName("获取包名")
        void testGetPackageName() {
            ResourceInfo resource = new ResourceInfo("com/example/test.txt", ClassLoader.getSystemClassLoader());
            assertThat(resource.getPackageName()).isEqualTo("com.example");
        }

        @Test
        @DisplayName("无路径返回空")
        void testGetPackageNameEmpty() {
            ResourceInfo resource = new ResourceInfo("test.txt", ClassLoader.getSystemClassLoader());
            assertThat(resource.getPackageName()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getClassLoader方法测试")
    class GetClassLoaderTests {

        @Test
        @DisplayName("获取类加载器")
        void testGetClassLoader() {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            ResourceInfo resource = new ResourceInfo("test.txt", classLoader);
            assertThat(resource.getClassLoader()).isSameAs(classLoader);
        }
    }

    @Nested
    @DisplayName("getUrl方法测试")
    class GetUrlTests {

        @Test
        @DisplayName("获取存在资源的URL")
        void testGetUrl() {
            ResourceInfo resource = new ResourceInfo("java/lang/String.class", ClassLoader.getSystemClassLoader());
            URL url = resource.getUrl();
            assertThat(url).isNotNull();
        }

        @Test
        @DisplayName("不存在资源返回null")
        void testGetUrlNotFound() {
            ResourceInfo resource = new ResourceInfo("nonexistent/resource.txt", ClassLoader.getSystemClassLoader());
            assertThat(resource.getUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("openStream方法测试")
    class OpenStreamTests {

        @Test
        @DisplayName("打开存在资源的流")
        void testOpenStream() throws IOException {
            ResourceInfo resource = new ResourceInfo("java/lang/String.class", ClassLoader.getSystemClassLoader());
            try (InputStream is = resource.openStream()) {
                assertThat(is).isNotNull();
            }
        }

        @Test
        @DisplayName("打开不存在资源抛出异常")
        void testOpenStreamNotFound() {
            ResourceInfo resource = new ResourceInfo("nonexistent/resource.txt", ClassLoader.getSystemClassLoader());
            assertThatThrownBy(resource::openStream).isInstanceOf(IOException.class);
        }
    }

    @Nested
    @DisplayName("readBytes方法测试")
    class ReadBytesTests {

        @Test
        @DisplayName("读取资源为字节数组")
        void testReadBytes() throws IOException {
            ResourceInfo resource = new ResourceInfo("java/lang/String.class", ClassLoader.getSystemClassLoader());
            byte[] bytes = resource.readBytes();
            assertThat(bytes).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("isClassFile方法测试")
    class IsClassFileTests {

        @Test
        @DisplayName("类文件返回true")
        void testIsClassFileTrue() {
            ResourceInfo resource = new ResourceInfo("com/example/Test.class", ClassLoader.getSystemClassLoader());
            assertThat(resource.isClassFile()).isTrue();
        }

        @Test
        @DisplayName("非类文件返回false")
        void testIsClassFileFalse() {
            ResourceInfo resource = new ResourceInfo("test.txt", ClassLoader.getSystemClassLoader());
            assertThat(resource.isClassFile()).isFalse();
        }
    }

    @Nested
    @DisplayName("isPropertiesFile方法测试")
    class IsPropertiesFileTests {

        @Test
        @DisplayName("属性文件返回true")
        void testIsPropertiesFileTrue() {
            ResourceInfo resource = new ResourceInfo("config.properties", ClassLoader.getSystemClassLoader());
            assertThat(resource.isPropertiesFile()).isTrue();
        }

        @Test
        @DisplayName("非属性文件返回false")
        void testIsPropertiesFileFalse() {
            ResourceInfo resource = new ResourceInfo("test.txt", ClassLoader.getSystemClassLoader());
            assertThat(resource.isPropertiesFile()).isFalse();
        }
    }

    @Nested
    @DisplayName("getExtension方法测试")
    class GetExtensionTests {

        @Test
        @DisplayName("获取扩展名")
        void testGetExtension() {
            ResourceInfo resource = new ResourceInfo("test.txt", ClassLoader.getSystemClassLoader());
            assertThat(resource.getExtension()).isEqualTo("txt");
        }

        @Test
        @DisplayName("无扩展名返回空")
        void testGetExtensionEmpty() {
            ResourceInfo resource = new ResourceInfo("Makefile", ClassLoader.getSystemClassLoader());
            assertThat(resource.getExtension()).isEmpty();
        }

        @Test
        @DisplayName("获取类文件扩展名")
        void testGetExtensionClass() {
            ResourceInfo resource = new ResourceInfo("Test.class", ClassLoader.getSystemClassLoader());
            assertThat(resource.getExtension()).isEqualTo("class");
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同资源相等")
        void testEquals() {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            ResourceInfo r1 = new ResourceInfo("test.txt", cl);
            ResourceInfo r2 = new ResourceInfo("test.txt", cl);
            assertThat(r1).isEqualTo(r2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() {
            ResourceInfo resource = new ResourceInfo("test.txt", ClassLoader.getSystemClassLoader());
            assertThat(resource).isEqualTo(resource);
        }

        @Test
        @DisplayName("不同资源不相等")
        void testNotEquals() {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            ResourceInfo r1 = new ResourceInfo("test1.txt", cl);
            ResourceInfo r2 = new ResourceInfo("test2.txt", cl);
            assertThat(r1).isNotEqualTo(r2);
        }
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相同资源有相同hashCode")
        void testHashCode() {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            ResourceInfo r1 = new ResourceInfo("test.txt", cl);
            ResourceInfo r2 = new ResourceInfo("test.txt", cl);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含资源名")
        void testToString() {
            ResourceInfo resource = new ResourceInfo("test.txt", ClassLoader.getSystemClassLoader());
            assertThat(resource.toString()).contains("ResourceInfo");
            assertThat(resource.toString()).contains("test.txt");
        }
    }
}
