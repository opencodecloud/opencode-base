package cloud.opencode.base.io.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ResourceLoader 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("ResourceLoader 接口测试")
class ResourceLoaderTest {

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("CLASSPATH_PREFIX常量")
        void testClasspathPrefix() {
            assertThat(ResourceLoader.CLASSPATH_PREFIX).isEqualTo("classpath:");
        }

        @Test
        @DisplayName("FILE_PREFIX常量")
        void testFilePrefix() {
            assertThat(ResourceLoader.FILE_PREFIX).isEqualTo("file:");
        }
    }

    @Nested
    @DisplayName("getDefault方法测试")
    class GetDefaultTests {

        @Test
        @DisplayName("返回DefaultResourceLoader实例")
        void testGetDefault() {
            ResourceLoader loader = ResourceLoader.getDefault();

            assertThat(loader).isInstanceOf(DefaultResourceLoader.class);
        }

        @Test
        @DisplayName("返回单例实例")
        void testGetDefaultSingleton() {
            ResourceLoader loader1 = ResourceLoader.getDefault();
            ResourceLoader loader2 = ResourceLoader.getDefault();

            assertThat(loader1).isSameAs(loader2);
        }
    }

    @Nested
    @DisplayName("接口方法测试")
    class InterfaceMethodsTests {

        @Test
        @DisplayName("getResource方法存在")
        void testGetResourceMethod() {
            ResourceLoader loader = ResourceLoader.getDefault();

            Resource resource = loader.getResource("classpath:nonexistent.txt");

            assertThat(resource).isNotNull();
        }

        @Test
        @DisplayName("getClassLoader方法存在")
        void testGetClassLoaderMethod() {
            ResourceLoader loader = ResourceLoader.getDefault();

            ClassLoader classLoader = loader.getClassLoader();

            assertThat(classLoader).isNotNull();
        }
    }

    @Nested
    @DisplayName("自定义实现测试")
    class CustomImplementationTests {

        @Test
        @DisplayName("实现ResourceLoader接口")
        void testCustomImplementation() {
            ResourceLoader customLoader = new ResourceLoader() {
                @Override
                public Resource getResource(String location) {
                    return new ClassPathResource(location);
                }

                @Override
                public ClassLoader getClassLoader() {
                    return getClass().getClassLoader();
                }
            };

            assertThat(customLoader.getResource("test.txt")).isNotNull();
            assertThat(customLoader.getClassLoader()).isNotNull();
        }
    }
}
