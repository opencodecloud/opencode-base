package cloud.opencode.base.reflect.scan;

import org.junit.jupiter.api.*;

import java.io.Serializable;
import java.lang.annotation.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * ClassScannerTest Tests
 * ClassScannerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("ClassScanner 测试")
class ClassScannerTest {

    @Nested
    @DisplayName("from静态方法测试")
    class FromTests {

        @Test
        @DisplayName("从ClassPath创建扫描器")
        void testFromClassPath() {
            ClassPath classPath = ClassPath.fromContextClassLoader();
            ClassScanner scanner = ClassScanner.from(classPath);
            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("从ClassLoader创建扫描器")
        void testFromClassLoader() {
            ClassScanner scanner = ClassScanner.from(ClassLoader.getSystemClassLoader());
            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("create静态方法测试")
    class CreateTests {

        @Test
        @DisplayName("使用上下文ClassLoader创建")
        void testCreate() {
            ClassScanner scanner = ClassScanner.create();
            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("inPackage方法测试")
    class InPackageTests {

        @Test
        @DisplayName("添加基础包")
        void testInPackage() {
            ClassScanner scanner = ClassScanner.create()
                    .inPackage("cloud.opencode.base.reflect.scan");
            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("inPackages方法测试")
    class InPackagesTests {

        @Test
        @DisplayName("添加多个基础包")
        void testInPackages() {
            ClassScanner scanner = ClassScanner.create()
                    .inPackages("java.lang", "java.util");
            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("recursive方法测试")
    class RecursiveTests {

        @Test
        @DisplayName("设置递归扫描")
        void testRecursive() {
            ClassScanner scanner = ClassScanner.create()
                    .inPackage("java.lang")
                    .recursive(true);
            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("设置非递归扫描")
        void testNonRecursive() {
            ClassScanner scanner = ClassScanner.create()
                    .inPackage("java.lang")
                    .recursive(false);
            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("includeInnerClasses方法测试")
    class IncludeInnerClassesTests {

        @Test
        @DisplayName("包含内部类")
        void testIncludeInnerClasses() {
            ClassScanner scanner = ClassScanner.create()
                    .includeInnerClasses(true);
            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("filterInfo方法测试")
    class FilterInfoTests {

        @Test
        @DisplayName("按ClassInfo过滤")
        void testFilterInfo() {
            ClassScanner scanner = ClassScanner.create()
                    .inPackage("cloud.opencode.base.reflect.scan")
                    .filterInfo(info -> info.getSimpleName().startsWith("Class"));
            Set<ClassInfo> result = scanner.scanInfo();
            for (ClassInfo info : result) {
                assertThat(info.getSimpleName()).startsWith("Class");
            }
        }
    }

    @Nested
    @DisplayName("filter方法测试")
    class FilterTests {

        @Test
        @DisplayName("按Class过滤")
        void testFilter() {
            ClassScanner scanner = ClassScanner.create()
                    .inPackage("cloud.opencode.base.reflect.scan")
                    .filter(c -> c.isInterface());
            Set<Class<?>> result = scanner.scan();
            for (Class<?> clazz : result) {
                assertThat(clazz.isInterface()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("withAnnotation方法测试")
    class WithAnnotationTests {

        @Test
        @DisplayName("过滤具有注解的类")
        void testWithAnnotation() {
            ClassScanner scanner = ClassScanner.create()
                    .inPackage("cloud.opencode.base.reflect.scan")
                    .withAnnotation(Deprecated.class);
            Set<Class<?>> result = scanner.scan();
            for (Class<?> clazz : result) {
                assertThat(clazz.isAnnotationPresent(Deprecated.class)).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("subtypeOf方法测试")
    class SubtypeOfTests {

        @Test
        @DisplayName("过滤子类型")
        void testSubtypeOf() {
            ClassScanner scanner = ClassScanner.create()
                    .inPackage("cloud.opencode.base.reflect.scan")
                    .subtypeOf(ResourceInfo.class);
            Set<Class<?>> result = scanner.scan();
            for (Class<?> clazz : result) {
                assertThat(ResourceInfo.class.isAssignableFrom(clazz)).isTrue();
                assertThat(clazz).isNotEqualTo(ResourceInfo.class);
            }
        }
    }

    @Nested
    @DisplayName("implementing方法测试")
    class ImplementingTests {

        @Test
        @DisplayName("过滤接口实现类")
        void testImplementing() {
            ClassScanner scanner = ClassScanner.create()
                    .inPackage("java.util")
                    .implementing(Serializable.class);
            Set<Class<?>> result = scanner.scan();
            for (Class<?> clazz : result) {
                assertThat(Serializable.class.isAssignableFrom(clazz)).isTrue();
                assertThat(clazz.isInterface()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("interfacesOnly方法测试")
    class InterfacesOnlyTests {

        @Test
        @DisplayName("仅过滤接口")
        void testInterfacesOnly() {
            ClassScanner scanner = ClassScanner.create()
                    .inPackage("cloud.opencode.base.reflect.scan")
                    .interfacesOnly();
            Set<Class<?>> result = scanner.scan();
            for (Class<?> clazz : result) {
                assertThat(clazz.isInterface()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("concreteOnly方法测试")
    class ConcreteOnlyTests {

        @Test
        @DisplayName("仅过滤具体类")
        void testConcreteOnly() {
            ClassScanner scanner = ClassScanner.create()
                    .inPackage("cloud.opencode.base.reflect.scan")
                    .concreteOnly();
            Set<Class<?>> result = scanner.scan();
            for (Class<?> clazz : result) {
                assertThat(clazz.isInterface()).isFalse();
                assertThat(java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("recordsOnly方法测试")
    class RecordsOnlyTests {

        @Test
        @DisplayName("仅过滤Record类")
        void testRecordsOnly() {
            ClassScanner scanner = ClassScanner.create()
                    .inPackage("cloud.opencode.base.reflect.scan")
                    .recordsOnly();
            Set<Class<?>> result = scanner.scan();
            for (Class<?> clazz : result) {
                assertThat(clazz.isRecord()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("enumsOnly方法测试")
    class EnumsOnlyTests {

        @Test
        @DisplayName("仅过滤枚举类")
        void testEnumsOnly() {
            ClassScanner scanner = ClassScanner.create()
                    .inPackage("java.time")
                    .enumsOnly();
            Set<Class<?>> result = scanner.scan();
            for (Class<?> clazz : result) {
                assertThat(clazz.isEnum()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("scanInfo方法测试")
    class ScanInfoTests {

        @Test
        @DisplayName("扫描ClassInfo")
        void testScanInfo() {
            ClassScanner scanner = ClassScanner.create()
                    .inPackage("cloud.opencode.base.reflect.scan");
            Set<ClassInfo> result = scanner.scanInfo();
            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("scan方法测试")
    class ScanTests {

        @Test
        @DisplayName("扫描类")
        void testScan() {
            ClassScanner scanner = ClassScanner.create()
                    .inPackage("cloud.opencode.base.reflect.scan");
            Set<Class<?>> result = scanner.scan();
            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("stream方法测试")
    class StreamTests {

        @Test
        @DisplayName("扫描并返回流")
        void testStream() {
            ClassScanner scanner = ClassScanner.create()
                    .inPackage("cloud.opencode.base.reflect.scan");
            Stream<Class<?>> stream = scanner.stream();
            assertThat(stream.count()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("toList方法测试")
    class ToListTests {

        @Test
        @DisplayName("扫描并返回列表")
        void testToList() {
            ClassScanner scanner = ClassScanner.create()
                    .inPackage("cloud.opencode.base.reflect.scan");
            List<Class<?>> list = scanner.toList();
            assertThat(list).isNotEmpty();
        }
    }
}
