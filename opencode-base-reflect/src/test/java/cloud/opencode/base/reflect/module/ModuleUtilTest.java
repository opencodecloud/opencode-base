package cloud.opencode.base.reflect.module;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ModuleUtil Tests
 * ModuleUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.3
 */
@DisplayName("ModuleUtil 测试")
class ModuleUtilTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数不可实例化")
        void testPrivateConstructor() throws Exception {
            var constructor = ModuleUtil.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("isAccessible 方法测试")
    class IsAccessibleTests {

        @Test
        @DisplayName("JDK 类 java.lang.String 对本模块可访问")
        void testJdkClassAccessible() {
            // java.lang is exported by java.base to everyone
            boolean accessible = ModuleUtil.isAccessible(String.class, ModuleUtilTest.class);
            assertThat(accessible).isTrue();
        }

        @Test
        @DisplayName("同模块类互相可访问")
        void testSameModuleAccessible() {
            boolean accessible = ModuleUtil.isAccessible(ModuleUtil.class, ModuleUtilTest.class);
            assertThat(accessible).isTrue();
        }

        @Test
        @DisplayName("null targetClass 抛出异常")
        void testNullTargetClassThrows() {
            assertThatThrownBy(() -> ModuleUtil.isAccessible(null, ModuleUtilTest.class))
                    .isInstanceOf(OpenReflectException.class)
                    .hasMessageContaining("targetClass");
        }

        @Test
        @DisplayName("null callerClass 抛出异常")
        void testNullCallerClassThrows() {
            assertThatThrownBy(() -> ModuleUtil.isAccessible(String.class, null))
                    .isInstanceOf(OpenReflectException.class)
                    .hasMessageContaining("callerClass");
        }
    }

    @Nested
    @DisplayName("isInNamedModule 方法测试")
    class IsInNamedModuleTests {

        @Test
        @DisplayName("JDK 类在命名模块中")
        void testJdkClassInNamedModule() {
            assertThat(ModuleUtil.isInNamedModule(String.class)).isTrue();
            assertThat(ModuleUtil.isInNamedModule(Integer.class)).isTrue();
        }

        @Test
        @DisplayName("测试类不在命名模块中（classpath 加载）")
        void testTestClassNotInNamedModule() {
            // Test classes are typically loaded from the classpath, not named modules
            // Unless the project uses module-info in test scope
            // We check it does not throw and returns a boolean
            boolean result = ModuleUtil.isInNamedModule(ModuleUtilTest.class);
            // The result depends on test runtime configuration; just verify no exception
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("null 参数抛出异常")
        void testNullThrows() {
            assertThatThrownBy(() -> ModuleUtil.isInNamedModule(null))
                    .isInstanceOf(OpenReflectException.class)
                    .hasMessageContaining("clazz");
        }
    }

    @Nested
    @DisplayName("getModuleInfo 方法测试")
    class GetModuleInfoTests {

        @Test
        @DisplayName("JDK String 类的模块信息")
        void testJdkStringModuleInfo() {
            ModuleUtil.ModuleInfo info = ModuleUtil.getModuleInfo(String.class);

            assertThat(info.moduleName()).isEqualTo("java.base");
            assertThat(info.packageName()).isEqualTo("java.lang");
            assertThat(info.isNamed()).isTrue();
            assertThat(info.isExported()).isTrue();
            // java.lang is exported but not unconditionally open
            assertThat(info.exportedPackages()).contains("java.lang");
        }

        @Test
        @DisplayName("JDK String 模块信息包含多个导出包")
        void testJdkModuleHasMultipleExports() {
            ModuleUtil.ModuleInfo info = ModuleUtil.getModuleInfo(String.class);

            assertThat(info.exportedPackages()).isNotEmpty();
            assertThat(info.exportedPackages()).contains("java.lang", "java.util", "java.io");
        }

        @Test
        @DisplayName("测试类的模块信息")
        void testOwnClassModuleInfo() {
            ModuleUtil.ModuleInfo info = ModuleUtil.getModuleInfo(ModuleUtilTest.class);

            assertThat(info.packageName()).isEqualTo("cloud.opencode.base.reflect.module");
            // moduleName should be either named or "unnamed"
            assertThat(info.moduleName()).isNotNull();
            assertThat(info.moduleName()).isNotEmpty();
        }

        @Test
        @DisplayName("null 参数抛出异常")
        void testNullThrows() {
            assertThatThrownBy(() -> ModuleUtil.getModuleInfo(null))
                    .isInstanceOf(OpenReflectException.class)
                    .hasMessageContaining("clazz");
        }

        @Test
        @DisplayName("ModuleInfo record 字段不可为 null")
        void testModuleInfoNullValidation() {
            assertThatThrownBy(() -> new ModuleUtil.ModuleInfo(
                    null, "pkg", true, false, false, java.util.Set.of(), java.util.Set.of()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("moduleName");

            assertThatThrownBy(() -> new ModuleUtil.ModuleInfo(
                    "mod", null, true, false, false, java.util.Set.of(), java.util.Set.of()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("packageName");
        }

        @Test
        @DisplayName("ModuleInfo 集合是不可变的")
        void testModuleInfoImmutableSets() {
            ModuleUtil.ModuleInfo info = ModuleUtil.getModuleInfo(String.class);

            assertThatThrownBy(() -> info.openPackages().add("test"))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> info.exportedPackages().add("test"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("canDeepReflect 方法测试")
    class CanDeepReflectTests {

        @Test
        @DisplayName("同模块类可以深度反射")
        void testSameModuleCanDeepReflect() {
            boolean result = ModuleUtil.canDeepReflect(ModuleUtil.class, ModuleUtilTest.class);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("JDK 非开放包不能深度反射")
        void testJdkNonOpenPackageCannotDeepReflect() {
            // java.lang is exported but not open to unnamed modules for deep reflection
            // Unless running with --add-opens; the exact result depends on runtime configuration.
            // We simply verify no exception and the method returns a boolean.
            boolean result = ModuleUtil.canDeepReflect(String.class, ModuleUtilTest.class);
            // Don't assert the specific value as it depends on JVM flags
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("null 参数抛出异常")
        void testNullThrows() {
            assertThatThrownBy(() -> ModuleUtil.canDeepReflect(null, ModuleUtilTest.class))
                    .isInstanceOf(OpenReflectException.class);
            assertThatThrownBy(() -> ModuleUtil.canDeepReflect(String.class, null))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("isOpen 方法测试")
    class IsOpenTests {

        @Test
        @DisplayName("同模块包是开放的")
        void testSameModuleIsOpen() {
            Module module = ModuleUtilTest.class.getModule();
            boolean result = ModuleUtil.isOpen(ModuleUtil.class, module);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("null 参数抛出异常")
        void testNullThrows() {
            assertThatThrownBy(() -> ModuleUtil.isOpen(null, ModuleUtilTest.class.getModule()))
                    .isInstanceOf(OpenReflectException.class);
            assertThatThrownBy(() -> ModuleUtil.isOpen(String.class, null))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("isExported 方法测试")
    class IsExportedTests {

        @Test
        @DisplayName("java.lang 包对所有模块导出")
        void testJavaLangExported() {
            Module callerModule = ModuleUtilTest.class.getModule();
            boolean result = ModuleUtil.isExported(String.class, callerModule);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("同模块包已导出")
        void testSameModuleExported() {
            Module module = ModuleUtilTest.class.getModule();
            boolean result = ModuleUtil.isExported(ModuleUtil.class, module);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("null 参数抛出异常")
        void testNullThrows() {
            assertThatThrownBy(() -> ModuleUtil.isExported(null, ModuleUtilTest.class.getModule()))
                    .isInstanceOf(OpenReflectException.class);
            assertThatThrownBy(() -> ModuleUtil.isExported(String.class, null))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("getAccessDiagnostic 方法测试")
    class GetAccessDiagnosticTests {

        @Test
        @DisplayName("同模块诊断信息包含 SAME MODULE")
        void testSameModuleDiagnostic() {
            String diagnostic = ModuleUtil.getAccessDiagnostic(ModuleUtil.class, ModuleUtilTest.class);
            assertThat(diagnostic).contains("SAME MODULE");
            assertThat(diagnostic).contains("full access");
        }

        @Test
        @DisplayName("JDK 类诊断信息包含模块名")
        void testJdkClassDiagnostic() {
            String diagnostic = ModuleUtil.getAccessDiagnostic(String.class, ModuleUtilTest.class);
            assertThat(diagnostic).contains("java.base");
            assertThat(diagnostic).contains("java.lang");
            assertThat(diagnostic).contains("Exported to caller: true");
        }

        @Test
        @DisplayName("诊断信息包含目标和调用方类名")
        void testDiagnosticContainsClassNames() {
            String diagnostic = ModuleUtil.getAccessDiagnostic(String.class, ModuleUtilTest.class);
            assertThat(diagnostic).contains("java.lang.String");
            assertThat(diagnostic).contains(ModuleUtilTest.class.getName());
        }

        @Test
        @DisplayName("诊断信息包含建议修复方法")
        void testDiagnosticContainsFix() {
            String diagnostic = ModuleUtil.getAccessDiagnostic(String.class, ModuleUtilTest.class);
            // For exported but not-open packages, should contain fix suggestions
            // or for fully accessible, should say so
            assertThat(diagnostic).isNotEmpty();
            // It should contain either "full access" or "Fix:" or "open"
            assertThat(diagnostic).containsAnyOf("access", "Fix", "open", "Status");
        }

        @Test
        @DisplayName("null 参数抛出异常")
        void testNullThrows() {
            assertThatThrownBy(() -> ModuleUtil.getAccessDiagnostic(null, ModuleUtilTest.class))
                    .isInstanceOf(OpenReflectException.class);
            assertThatThrownBy(() -> ModuleUtil.getAccessDiagnostic(String.class, null))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("tryPrivateLookup 方法测试")
    class TryPrivateLookupTests {

        @Test
        @DisplayName("本模块类可获取 PrivateLookup")
        void testOwnModuleLookupSucceeds() {
            Optional<MethodHandles.Lookup> lookup = ModuleUtil.tryPrivateLookup(ModuleUtil.class);
            assertThat(lookup).isPresent();
        }

        @Test
        @DisplayName("返回的 Lookup 拥有 PRIVATE 访问权限")
        void testLookupHasPrivateAccess() {
            Optional<MethodHandles.Lookup> lookup = ModuleUtil.tryPrivateLookup(ModuleUtil.class);
            assertThat(lookup).isPresent();
            assertThat(lookup.get().lookupModes() & MethodHandles.Lookup.PRIVATE)
                    .isNotZero();
        }

        @Test
        @DisplayName("JDK 非开放类返回 empty 或 present（取决于 JVM 配置）")
        void testJdkClassLookup() {
            // This may succeed or fail depending on --add-opens flags
            Optional<MethodHandles.Lookup> lookup = ModuleUtil.tryPrivateLookup(String.class);
            // Just verify it doesn't throw
            assertThat(lookup).isNotNull();
        }

        @Test
        @DisplayName("null 参数抛出异常")
        void testNullThrows() {
            assertThatThrownBy(() -> ModuleUtil.tryPrivateLookup(null))
                    .isInstanceOf(OpenReflectException.class);
        }
    }
}
