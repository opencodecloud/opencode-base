package cloud.opencode.base.reflect.scan;

import org.junit.jupiter.api.*;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * ClassPathTest Tests
 * ClassPathTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("ClassPath 测试")
class ClassPathTest {

    private static ClassPath classPath;

    @BeforeAll
    static void setUp() {
        classPath = ClassPath.fromContextClassLoader();
    }

    @Nested
    @DisplayName("from静态方法测试")
    class FromTests {

        @Test
        @DisplayName("从ClassLoader创建ClassPath")
        void testFrom() {
            ClassPath cp = ClassPath.from(ClassLoader.getSystemClassLoader());
            assertThat(cp).isNotNull();
        }
    }

    @Nested
    @DisplayName("fromSystemClassLoader静态方法测试")
    class FromSystemClassLoaderTests {

        @Test
        @DisplayName("从系统ClassLoader创建")
        void testFromSystemClassLoader() {
            ClassPath cp = ClassPath.fromSystemClassLoader();
            assertThat(cp).isNotNull();
        }
    }

    @Nested
    @DisplayName("fromContextClassLoader静态方法测试")
    class FromContextClassLoaderTests {

        @Test
        @DisplayName("从上下文ClassLoader创建")
        void testFromContextClassLoader() {
            ClassPath cp = ClassPath.fromContextClassLoader();
            assertThat(cp).isNotNull();
        }
    }

    @Nested
    @DisplayName("getAllClasses方法测试")
    class GetAllClassesTests {

        @Test
        @DisplayName("获取所有类")
        void testGetAllClasses() {
            Set<ClassInfo> classes = classPath.getAllClasses();
            assertThat(classes).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getResources方法测试")
    class GetResourcesTests {

        @Test
        @DisplayName("获取所有资源")
        void testGetResources() {
            Set<ResourceInfo> resources = classPath.getResources();
            assertThat(resources).isNotNull();
        }
    }

    @Nested
    @DisplayName("getClassesInPackage方法测试")
    class GetClassesInPackageTests {

        @Test
        @DisplayName("获取指定包中的类")
        void testGetClassesInPackage() {
            Set<ClassInfo> classes = classPath.getClassesInPackage("java.lang");
            // java.lang should have classes
            assertThat(classes).isNotNull();
        }
    }

    @Nested
    @DisplayName("getTopLevelClassesInPackage方法测试")
    class GetTopLevelClassesInPackageTests {

        @Test
        @DisplayName("获取指定包中的顶层类")
        void testGetTopLevelClassesInPackage() {
            Set<ClassInfo> classes = classPath.getTopLevelClassesInPackage("java.lang");
            assertThat(classes).isNotNull();
            // All returned should be non-inner classes
            for (ClassInfo classInfo : classes) {
                assertThat(classInfo.isInnerClass()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("getClassesRecursively方法测试")
    class GetClassesRecursivelyTests {

        @Test
        @DisplayName("递归获取包中的类")
        void testGetClassesRecursively() {
            Set<ClassInfo> classes = classPath.getClassesRecursively("java.lang");
            assertThat(classes).isNotNull();
        }
    }

    @Nested
    @DisplayName("getTopLevelClassesRecursively方法测试")
    class GetTopLevelClassesRecursivelyTests {

        @Test
        @DisplayName("递归获取顶层类")
        void testGetTopLevelClassesRecursively() {
            Set<ClassInfo> classes = classPath.getTopLevelClassesRecursively("java.lang");
            assertThat(classes).isNotNull();
        }
    }

    @Nested
    @DisplayName("getResourcesInPackage方法测试")
    class GetResourcesInPackageTests {

        @Test
        @DisplayName("获取指定包中的资源")
        void testGetResourcesInPackage() {
            Set<ResourceInfo> resources = classPath.getResourcesInPackage("META-INF");
            assertThat(resources).isNotNull();
        }
    }

    @Nested
    @DisplayName("getResourcesWithExtension方法测试")
    class GetResourcesWithExtensionTests {

        @Test
        @DisplayName("获取指定扩展名的资源")
        void testGetResourcesWithExtension() {
            Set<ResourceInfo> resources = classPath.getResourcesWithExtension("properties");
            assertThat(resources).isNotNull();
        }
    }

    @Nested
    @DisplayName("getClassLoader方法测试")
    class GetClassLoaderTests {

        @Test
        @DisplayName("获取类加载器")
        void testGetClassLoader() {
            assertThat(classPath.getClassLoader()).isNotNull();
        }
    }

    @Nested
    @DisplayName("streamClasses方法测试")
    class StreamClassesTests {

        @Test
        @DisplayName("流式获取类")
        void testStreamClasses() {
            Stream<ClassInfo> stream = classPath.streamClasses();
            assertThat(stream).isNotNull();
        }
    }

    @Nested
    @DisplayName("streamResources方法测试")
    class StreamResourcesTests {

        @Test
        @DisplayName("流式获取资源")
        void testStreamResources() {
            Stream<ResourceInfo> stream = classPath.streamResources();
            assertThat(stream).isNotNull();
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含统计信息")
        void testToString() {
            String str = classPath.toString();
            assertThat(str).contains("ClassPath");
            assertThat(str).contains("classes=");
            assertThat(str).contains("resources=");
        }
    }
}
