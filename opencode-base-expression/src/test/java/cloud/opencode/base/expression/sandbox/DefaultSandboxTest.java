package cloud.opencode.base.expression.sandbox;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultSandbox Tests
 * DefaultSandbox 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("DefaultSandbox Tests | DefaultSandbox 测试")
class DefaultSandboxTest {

    @Nested
    @DisplayName("Factory Methods Tests | 工厂方法测试")
    class FactoryMethodsTests {

        @Test
        @DisplayName("permissive allows all | permissive 允许所有")
        void testPermissive() {
            DefaultSandbox sandbox = DefaultSandbox.permissive();
            assertThat(sandbox.isClassAllowed(String.class)).isTrue();
            assertThat(sandbox.isClassAllowed(Runtime.class)).isTrue();
        }

        @Test
        @DisplayName("restrictive denies dangerous classes | restrictive 拒绝危险类")
        void testRestrictive() {
            DefaultSandbox sandbox = DefaultSandbox.restrictive();
            assertThat(sandbox.isClassAllowed(String.class)).isTrue();
            assertThat(sandbox.isClassAllowed(List.class)).isTrue();
            assertThat(sandbox.isClassAllowed(Runtime.class)).isFalse();
            assertThat(sandbox.isClassAllowed(ProcessBuilder.class)).isFalse();
        }

        @Test
        @DisplayName("standard allows most but denies dangerous | standard 允许大多数但拒绝危险")
        void testStandard() {
            DefaultSandbox sandbox = DefaultSandbox.standard();
            assertThat(sandbox.isClassAllowed(String.class)).isTrue();
            assertThat(sandbox.isClassAllowed(Runtime.class)).isFalse();
            assertThat(sandbox.getMaxExpressionLength()).isEqualTo(10000);
            assertThat(sandbox.getMaxEvaluationDepth()).isEqualTo(100);
            assertThat(sandbox.getMaxEvaluationTime()).isEqualTo(5000);
        }
    }

    @Nested
    @DisplayName("IsClassAllowed Tests | isClassAllowed 测试")
    class IsClassAllowedTests {

        @Test
        @DisplayName("null class returns false | null 类返回 false")
        void testNullClass() {
            DefaultSandbox sandbox = DefaultSandbox.permissive();
            assertThat(sandbox.isClassAllowed(null)).isFalse();
        }

        @Test
        @DisplayName("denied class returns false | 拒绝的类返回 false")
        void testDeniedClass() {
            DefaultSandbox sandbox = DefaultSandbox.builder()
                    .allowAllByDefault(true)
                    .addDeniedClass(String.class)
                    .build();
            assertThat(sandbox.isClassAllowed(String.class)).isFalse();
        }

        @Test
        @DisplayName("denied package returns false | 拒绝的包返回 false")
        void testDeniedPackage() {
            DefaultSandbox sandbox = DefaultSandbox.builder()
                    .allowAllByDefault(true)
                    .addDeniedPackage("java.lang.reflect")
                    .build();
            assertThat(sandbox.isClassAllowed(java.lang.reflect.Method.class)).isFalse();
        }

        @Test
        @DisplayName("allowed class returns true | 允许的类返回 true")
        void testAllowedClass() {
            DefaultSandbox sandbox = DefaultSandbox.builder()
                    .allowAllByDefault(false)
                    .addAllowedClass(String.class)
                    .build();
            assertThat(sandbox.isClassAllowed(String.class)).isTrue();
            assertThat(sandbox.isClassAllowed(Integer.class)).isFalse();
        }

        @Test
        @DisplayName("allowed package returns true | 允许的包返回 true")
        void testAllowedPackage() {
            DefaultSandbox sandbox = DefaultSandbox.builder()
                    .allowAllByDefault(false)
                    .addAllowedPackage("java.util")
                    .build();
            assertThat(sandbox.isClassAllowed(List.class)).isTrue();
        }

        @Test
        @DisplayName("denied takes priority over allowed | 拒绝优先于允许")
        void testDeniedPriority() {
            DefaultSandbox sandbox = DefaultSandbox.builder()
                    .addAllowedPackage("java.lang")
                    .addDeniedClass("java.lang.Runtime")
                    .build();
            assertThat(sandbox.isClassAllowed(String.class)).isTrue();
            assertThat(sandbox.isClassAllowed(Runtime.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("IsMethodAllowed Tests | isMethodAllowed 测试")
    class IsMethodAllowedTests {

        @Test
        @DisplayName("null target returns false | null 目标返回 false")
        void testNullTarget() throws NoSuchMethodException {
            DefaultSandbox sandbox = DefaultSandbox.permissive();
            Method method = String.class.getMethod("length");
            assertThat(sandbox.isMethodAllowed(null, method)).isFalse();
        }

        @Test
        @DisplayName("null method returns false | null 方法返回 false")
        void testNullMethod() {
            DefaultSandbox sandbox = DefaultSandbox.permissive();
            assertThat(sandbox.isMethodAllowed("test", null)).isFalse();
        }

        @Test
        @DisplayName("allowed method returns true | 允许的方法返回 true")
        void testAllowedMethod() throws NoSuchMethodException {
            DefaultSandbox sandbox = DefaultSandbox.permissive();
            Method method = String.class.getMethod("length");
            assertThat(sandbox.isMethodAllowed("test", method)).isTrue();
        }

        @Test
        @DisplayName("denied method returns false | 拒绝的方法返回 false")
        void testDeniedMethod() throws NoSuchMethodException {
            DefaultSandbox sandbox = DefaultSandbox.builder()
                    .allowAllByDefault(true)
                    .addDeniedMethod("length")
                    .build();
            Method method = String.class.getMethod("length");
            assertThat(sandbox.isMethodAllowed("test", method)).isFalse();
        }

        @Test
        @DisplayName("denied class method returns false | 拒绝类的方法返回 false")
        void testDeniedClassMethod() throws NoSuchMethodException {
            DefaultSandbox sandbox = DefaultSandbox.builder()
                    .allowAllByDefault(true)
                    .addDeniedClass(String.class)
                    .build();
            Method method = String.class.getMethod("length");
            assertThat(sandbox.isMethodAllowed("test", method)).isFalse();
        }

        @Test
        @DisplayName("allowed method list check | 允许的方法列表检查")
        void testAllowedMethodList() throws NoSuchMethodException {
            DefaultSandbox sandbox = DefaultSandbox.builder()
                    .allowAllByDefault(true)
                    .addAllowedMethod("length")
                    .build();
            Method lengthMethod = String.class.getMethod("length");
            Method substringMethod = String.class.getMethod("substring", int.class);
            assertThat(sandbox.isMethodAllowed("test", lengthMethod)).isTrue();
            assertThat(sandbox.isMethodAllowed("test", substringMethod)).isFalse();
        }
    }

    @Nested
    @DisplayName("IsPropertyAllowed Tests | isPropertyAllowed 测试")
    class IsPropertyAllowedTests {

        @Test
        @DisplayName("null target returns false | null 目标返回 false")
        void testNullTarget() {
            DefaultSandbox sandbox = DefaultSandbox.permissive();
            assertThat(sandbox.isPropertyAllowed(null, "prop")).isFalse();
        }

        @Test
        @DisplayName("null property returns false | null 属性返回 false")
        void testNullProperty() {
            DefaultSandbox sandbox = DefaultSandbox.permissive();
            assertThat(sandbox.isPropertyAllowed("test", null)).isFalse();
        }

        @Test
        @DisplayName("allowed class property returns true | 允许类的属性返回 true")
        void testAllowedClassProperty() {
            DefaultSandbox sandbox = DefaultSandbox.permissive();
            assertThat(sandbox.isPropertyAllowed("test", "length")).isTrue();
        }

        @Test
        @DisplayName("denied class property returns false | 拒绝类的属性返回 false")
        void testDeniedClassProperty() {
            DefaultSandbox sandbox = DefaultSandbox.builder()
                    .allowAllByDefault(true)
                    .addDeniedClass(String.class)
                    .build();
            assertThat(sandbox.isPropertyAllowed("test", "length")).isFalse();
        }
    }

    @Nested
    @DisplayName("Limits Tests | 限制测试")
    class LimitsTests {

        @Test
        @DisplayName("default limits | 默认限制")
        void testDefaultLimits() {
            DefaultSandbox sandbox = DefaultSandbox.builder().build();
            assertThat(sandbox.getMaxExpressionLength()).isEqualTo(-1);
            assertThat(sandbox.getMaxEvaluationDepth()).isEqualTo(100);
            assertThat(sandbox.getMaxEvaluationTime()).isEqualTo(-1);
        }

        @Test
        @DisplayName("custom limits | 自定义限制")
        void testCustomLimits() {
            DefaultSandbox sandbox = DefaultSandbox.builder()
                    .maxExpressionLength(5000)
                    .maxEvaluationDepth(50)
                    .maxEvaluationTime(3000)
                    .build();
            assertThat(sandbox.getMaxExpressionLength()).isEqualTo(5000);
            assertThat(sandbox.getMaxEvaluationDepth()).isEqualTo(50);
            assertThat(sandbox.getMaxEvaluationTime()).isEqualTo(3000);
        }
    }

    @Nested
    @DisplayName("Builder Tests | 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("builder creates sandbox | builder 创建沙箱")
        void testBuilder() {
            DefaultSandbox sandbox = DefaultSandbox.builder()
                    .allowAllByDefault(true)
                    .addAllowedClass(String.class)
                    .addDeniedClass(Runtime.class)
                    .addAllowedPackage("java.util")
                    .addDeniedPackage("java.lang.reflect")
                    .addAllowedMethod("toString")
                    .addDeniedMethod("getClass")
                    .maxExpressionLength(1000)
                    .maxEvaluationDepth(10)
                    .maxEvaluationTime(100)
                    .build();

            assertThat(sandbox).isNotNull();
        }

        @Test
        @DisplayName("builder addAllowedClass by name | builder 按名称添加允许的类")
        void testBuilderAddAllowedClassByName() {
            DefaultSandbox sandbox = DefaultSandbox.builder()
                    .allowAllByDefault(false)
                    .addAllowedClass("java.lang.String")
                    .build();
            assertThat(sandbox.isClassAllowed(String.class)).isTrue();
        }

        @Test
        @DisplayName("builder addDeniedClass by Class | builder 按 Class 添加拒绝的类")
        void testBuilderAddDeniedClassByClass() {
            DefaultSandbox sandbox = DefaultSandbox.builder()
                    .allowAllByDefault(true)
                    .addDeniedClass(Runtime.class)
                    .build();
            assertThat(sandbox.isClassAllowed(Runtime.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("Sandbox Interface Tests | Sandbox 接口测试")
    class SandboxInterfaceTests {

        @Test
        @DisplayName("isConstructionAllowed delegates to isClassAllowed | isConstructionAllowed 委托给 isClassAllowed")
        void testIsConstructionAllowed() {
            DefaultSandbox sandbox = DefaultSandbox.builder()
                    .allowAllByDefault(true)
                    .addDeniedClass(Runtime.class)
                    .build();
            assertThat(sandbox.isConstructionAllowed(String.class)).isTrue();
            assertThat(sandbox.isConstructionAllowed(Runtime.class)).isFalse();
        }
    }
}
