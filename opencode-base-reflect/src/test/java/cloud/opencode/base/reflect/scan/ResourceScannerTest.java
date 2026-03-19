package cloud.opencode.base.reflect.scan;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * ResourceScannerTest Tests
 * ResourceScannerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("ResourceScanner 测试")
class ResourceScannerTest {

    @Nested
    @DisplayName("from静态方法测试")
    class FromTests {

        @Test
        @DisplayName("从ClassPath创建扫描器")
        void testFromClassPath() {
            ClassPath classPath = ClassPath.fromContextClassLoader();
            ResourceScanner scanner = ResourceScanner.from(classPath);
            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("从ClassLoader创建扫描器")
        void testFromClassLoader() {
            ResourceScanner scanner = ResourceScanner.from(ClassLoader.getSystemClassLoader());
            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("create静态方法测试")
    class CreateTests {

        @Test
        @DisplayName("使用上下文ClassLoader创建")
        void testCreate() {
            ResourceScanner scanner = ResourceScanner.create();
            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("excludeClasses方法测试")
    class ExcludeClassesTests {

        @Test
        @DisplayName("排除类文件")
        void testExcludeClasses() {
            ResourceScanner scanner = ResourceScanner.create()
                    .excludeClasses(true);
            Set<ResourceInfo> result = scanner.scan();
            for (ResourceInfo resource : result) {
                assertThat(resource.isClassFile()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("includeClasses方法测试")
    class IncludeClassesTests {

        @Test
        @DisplayName("包含类文件")
        void testIncludeClasses() {
            ResourceScanner scanner = ResourceScanner.create()
                    .includeClasses();
            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("withExtension方法测试")
    class WithExtensionTests {

        @Test
        @DisplayName("按扩展名过滤")
        void testWithExtension() {
            ResourceScanner scanner = ResourceScanner.create()
                    .withExtension("properties");
            Set<ResourceInfo> result = scanner.scan();
            for (ResourceInfo resource : result) {
                assertThat(resource.getExtension().toLowerCase()).isEqualTo("properties");
            }
        }
    }

    @Nested
    @DisplayName("withExtensions方法测试")
    class WithExtensionsTests {

        @Test
        @DisplayName("按多个扩展名过滤")
        void testWithExtensions() {
            ResourceScanner scanner = ResourceScanner.create()
                    .withExtensions("properties", "xml");
            Set<ResourceInfo> result = scanner.scan();
            for (ResourceInfo resource : result) {
                String ext = resource.getExtension().toLowerCase();
                assertThat(ext).isIn("properties", "xml");
            }
        }
    }

    @Nested
    @DisplayName("inPackage方法测试")
    class InPackageTests {

        @Test
        @DisplayName("按包过滤")
        void testInPackage() {
            ResourceScanner scanner = ResourceScanner.create()
                    .inPackage("META-INF");
            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("inPackages方法测试")
    class InPackagesTests {

        @Test
        @DisplayName("按多个包过滤")
        void testInPackages() {
            ResourceScanner scanner = ResourceScanner.create()
                    .inPackages("META-INF", "com");
            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("matching方法测试")
    class MatchingTests {

        @Test
        @DisplayName("按正则模式过滤")
        void testMatching() {
            ResourceScanner scanner = ResourceScanner.create()
                    .matching(".*\\.properties");
            Set<ResourceInfo> result = scanner.scan();
            for (ResourceInfo resource : result) {
                assertThat(resource.getResourceName()).endsWith(".properties");
            }
        }
    }

    @Nested
    @DisplayName("nameContains方法测试")
    class NameContainsTests {

        @Test
        @DisplayName("按名称包含过滤")
        void testNameContains() {
            ResourceScanner scanner = ResourceScanner.create()
                    .nameContains("META");
            Set<ResourceInfo> result = scanner.scan();
            for (ResourceInfo resource : result) {
                assertThat(resource.getResourceName()).contains("META");
            }
        }
    }

    @Nested
    @DisplayName("nameStartsWith方法测试")
    class NameStartsWithTests {

        @Test
        @DisplayName("按名称前缀过滤")
        void testNameStartsWith() {
            ResourceScanner scanner = ResourceScanner.create()
                    .nameStartsWith("MANIFEST");
            Set<ResourceInfo> result = scanner.scan();
            for (ResourceInfo resource : result) {
                assertThat(resource.getSimpleName()).startsWith("MANIFEST");
            }
        }
    }

    @Nested
    @DisplayName("nameEndsWith方法测试")
    class NameEndsWithTests {

        @Test
        @DisplayName("按名称后缀过滤")
        void testNameEndsWith() {
            ResourceScanner scanner = ResourceScanner.create()
                    .nameEndsWith(".MF");
            Set<ResourceInfo> result = scanner.scan();
            for (ResourceInfo resource : result) {
                assertThat(resource.getSimpleName()).endsWith(".MF");
            }
        }
    }

    @Nested
    @DisplayName("filter方法测试")
    class FilterTests {

        @Test
        @DisplayName("自定义过滤器")
        void testFilter() {
            ResourceScanner scanner = ResourceScanner.create()
                    .filter(r -> r.getSimpleName().length() > 5);
            Set<ResourceInfo> result = scanner.scan();
            for (ResourceInfo resource : result) {
                assertThat(resource.getSimpleName().length()).isGreaterThan(5);
            }
        }
    }

    @Nested
    @DisplayName("scan方法测试")
    class ScanTests {

        @Test
        @DisplayName("扫描资源")
        void testScan() {
            ResourceScanner scanner = ResourceScanner.create();
            Set<ResourceInfo> result = scanner.scan();
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("stream方法测试")
    class StreamTests {

        @Test
        @DisplayName("扫描并返回流")
        void testStream() {
            ResourceScanner scanner = ResourceScanner.create();
            Stream<ResourceInfo> stream = scanner.stream();
            assertThat(stream).isNotNull();
        }
    }

    @Nested
    @DisplayName("toList方法测试")
    class ToListTests {

        @Test
        @DisplayName("扫描并返回列表")
        void testToList() {
            ResourceScanner scanner = ResourceScanner.create();
            List<ResourceInfo> list = scanner.toList();
            assertThat(list).isNotNull();
        }
    }

    @Nested
    @DisplayName("findProperties方法测试")
    class FindPropertiesTests {

        @Test
        @DisplayName("查找属性文件")
        void testFindProperties() {
            ResourceScanner scanner = ResourceScanner.create();
            Set<ResourceInfo> result = scanner.findProperties();
            for (ResourceInfo resource : result) {
                assertThat(resource.isPropertiesFile()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("findXml方法测试")
    class FindXmlTests {

        @Test
        @DisplayName("查找XML文件")
        void testFindXml() {
            ResourceScanner scanner = ResourceScanner.create();
            Set<ResourceInfo> result = scanner.findXml();
            for (ResourceInfo resource : result) {
                assertThat(resource.getExtension()).isEqualTo("xml");
            }
        }
    }

    @Nested
    @DisplayName("findJson方法测试")
    class FindJsonTests {

        @Test
        @DisplayName("查找JSON文件")
        void testFindJson() {
            ResourceScanner scanner = ResourceScanner.create();
            Set<ResourceInfo> result = scanner.findJson();
            for (ResourceInfo resource : result) {
                assertThat(resource.getExtension()).isEqualTo("json");
            }
        }
    }

    @Nested
    @DisplayName("findYaml方法测试")
    class FindYamlTests {

        @Test
        @DisplayName("查找YAML文件")
        void testFindYaml() {
            ResourceScanner scanner = ResourceScanner.create();
            Set<ResourceInfo> result = scanner.findYaml();
            for (ResourceInfo resource : result) {
                assertThat(resource.getExtension()).isIn("yaml", "yml");
            }
        }
    }
}
