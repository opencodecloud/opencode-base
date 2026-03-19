package cloud.opencode.base.expression.sandbox;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * Sandbox Interface Tests
 * Sandbox 接口测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("Sandbox Interface Tests | Sandbox 接口测试")
class SandboxTest {

    @Nested
    @DisplayName("Default Methods Tests | 默认方法测试")
    class DefaultMethodsTests {

        @Test
        @DisplayName("isConstructionAllowed delegates to isClassAllowed | isConstructionAllowed 委托给 isClassAllowed")
        void testIsConstructionAllowedDefault() {
            Sandbox sandbox = new Sandbox() {
                @Override
                public boolean isClassAllowed(Class<?> clazz) {
                    return clazz == String.class;
                }

                @Override
                public boolean isMethodAllowed(Object target, Method method) {
                    return true;
                }

                @Override
                public boolean isPropertyAllowed(Object target, String property) {
                    return true;
                }
            };

            assertThat(sandbox.isConstructionAllowed(String.class)).isTrue();
            assertThat(sandbox.isConstructionAllowed(Integer.class)).isFalse();
        }

        @Test
        @DisplayName("getMaxExpressionLength returns -1 by default | getMaxExpressionLength 默认返回 -1")
        void testGetMaxExpressionLengthDefault() {
            Sandbox sandbox = createMinimalSandbox();
            assertThat(sandbox.getMaxExpressionLength()).isEqualTo(-1);
        }

        @Test
        @DisplayName("getMaxEvaluationDepth returns 100 by default | getMaxEvaluationDepth 默认返回 100")
        void testGetMaxEvaluationDepthDefault() {
            Sandbox sandbox = createMinimalSandbox();
            assertThat(sandbox.getMaxEvaluationDepth()).isEqualTo(100);
        }

        @Test
        @DisplayName("getMaxEvaluationTime returns -1 by default | getMaxEvaluationTime 默认返回 -1")
        void testGetMaxEvaluationTimeDefault() {
            Sandbox sandbox = createMinimalSandbox();
            assertThat(sandbox.getMaxEvaluationTime()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("Class Allowed Tests | 类允许测试")
    class ClassAllowedTests {

        @Test
        @DisplayName("isClassAllowed returns true for allowed class | isClassAllowed 对允许的类返回 true")
        void testClassAllowed() {
            Sandbox sandbox = createPermissiveSandbox();
            assertThat(sandbox.isClassAllowed(String.class)).isTrue();
        }

        @Test
        @DisplayName("isClassAllowed returns false for denied class | isClassAllowed 对拒绝的类返回 false")
        void testClassDenied() {
            Sandbox sandbox = createRestrictiveSandbox();
            assertThat(sandbox.isClassAllowed(Runtime.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("Method Allowed Tests | 方法允许测试")
    class MethodAllowedTests {

        @Test
        @DisplayName("isMethodAllowed returns true for allowed method | isMethodAllowed 对允许的方法返回 true")
        void testMethodAllowed() throws NoSuchMethodException {
            Sandbox sandbox = createPermissiveSandbox();
            Method method = String.class.getMethod("length");

            assertThat(sandbox.isMethodAllowed("test", method)).isTrue();
        }

        @Test
        @DisplayName("isMethodAllowed returns false for denied method | isMethodAllowed 对拒绝的方法返回 false")
        void testMethodDenied() throws NoSuchMethodException {
            Sandbox sandbox = createMethodRestrictiveSandbox();
            Method method = Runtime.class.getMethod("exec", String.class);
            Runtime runtime = Runtime.getRuntime();

            assertThat(sandbox.isMethodAllowed(runtime, method)).isFalse();
        }
    }

    @Nested
    @DisplayName("Property Allowed Tests | 属性允许测试")
    class PropertyAllowedTests {

        @Test
        @DisplayName("isPropertyAllowed returns true for allowed property | isPropertyAllowed 对允许的属性返回 true")
        void testPropertyAllowed() {
            Sandbox sandbox = createPermissiveSandbox();
            assertThat(sandbox.isPropertyAllowed("object", "name")).isTrue();
        }

        @Test
        @DisplayName("isPropertyAllowed returns false for denied property | isPropertyAllowed 对拒绝的属性返回 false")
        void testPropertyDenied() {
            Sandbox sandbox = createPropertyRestrictiveSandbox();
            assertThat(sandbox.isPropertyAllowed("object", "class")).isFalse();
        }
    }

    @Nested
    @DisplayName("DefaultSandbox Integration Tests | DefaultSandbox 集成测试")
    class DefaultSandboxIntegrationTests {

        @Test
        @DisplayName("permissive sandbox allows all | permissive 沙箱允许所有")
        void testPermissiveSandbox() {
            Sandbox sandbox = DefaultSandbox.permissive();

            assertThat(sandbox.isClassAllowed(String.class)).isTrue();
            assertThat(sandbox.isClassAllowed(Runtime.class)).isTrue();
        }

        @Test
        @DisplayName("restrictive sandbox denies dangerous classes | restrictive 沙箱拒绝危险类")
        void testRestrictiveSandbox() {
            Sandbox sandbox = DefaultSandbox.restrictive();

            assertThat(sandbox.isClassAllowed(String.class)).isTrue();
            assertThat(sandbox.isClassAllowed(Runtime.class)).isFalse();
            assertThat(sandbox.isClassAllowed(ProcessBuilder.class)).isFalse();
        }

        @Test
        @DisplayName("standard sandbox has limits | standard 沙箱有限制")
        void testStandardSandbox() {
            Sandbox sandbox = DefaultSandbox.standard();

            assertThat(sandbox.getMaxExpressionLength()).isGreaterThan(0);
            assertThat(sandbox.getMaxEvaluationDepth()).isGreaterThan(0);
            assertThat(sandbox.getMaxEvaluationTime()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Custom Sandbox Tests | 自定义沙箱测试")
    class CustomSandboxTests {

        @Test
        @DisplayName("custom sandbox with overridden limits | 自定义沙箱带覆盖的限制")
        void testCustomSandboxWithLimits() {
            Sandbox sandbox = new Sandbox() {
                @Override
                public boolean isClassAllowed(Class<?> clazz) {
                    return true;
                }

                @Override
                public boolean isMethodAllowed(Object target, Method method) {
                    return true;
                }

                @Override
                public boolean isPropertyAllowed(Object target, String property) {
                    return true;
                }

                @Override
                public int getMaxExpressionLength() {
                    return 1000;
                }

                @Override
                public int getMaxEvaluationDepth() {
                    return 50;
                }

                @Override
                public long getMaxEvaluationTime() {
                    return 3000;
                }
            };

            assertThat(sandbox.getMaxExpressionLength()).isEqualTo(1000);
            assertThat(sandbox.getMaxEvaluationDepth()).isEqualTo(50);
            assertThat(sandbox.getMaxEvaluationTime()).isEqualTo(3000);
        }
    }

    // Helper methods to create test sandboxes

    private Sandbox createMinimalSandbox() {
        return new Sandbox() {
            @Override
            public boolean isClassAllowed(Class<?> clazz) {
                return true;
            }

            @Override
            public boolean isMethodAllowed(Object target, Method method) {
                return true;
            }

            @Override
            public boolean isPropertyAllowed(Object target, String property) {
                return true;
            }
        };
    }

    private Sandbox createPermissiveSandbox() {
        return createMinimalSandbox();
    }

    private Sandbox createRestrictiveSandbox() {
        return new Sandbox() {
            @Override
            public boolean isClassAllowed(Class<?> clazz) {
                // Deny dangerous classes
                return clazz != Runtime.class && clazz != ProcessBuilder.class;
            }

            @Override
            public boolean isMethodAllowed(Object target, Method method) {
                return true;
            }

            @Override
            public boolean isPropertyAllowed(Object target, String property) {
                return true;
            }
        };
    }

    private Sandbox createMethodRestrictiveSandbox() {
        return new Sandbox() {
            @Override
            public boolean isClassAllowed(Class<?> clazz) {
                return true;
            }

            @Override
            public boolean isMethodAllowed(Object target, Method method) {
                // Deny exec methods
                return !method.getName().equals("exec");
            }

            @Override
            public boolean isPropertyAllowed(Object target, String property) {
                return true;
            }
        };
    }

    private Sandbox createPropertyRestrictiveSandbox() {
        return new Sandbox() {
            @Override
            public boolean isClassAllowed(Class<?> clazz) {
                return true;
            }

            @Override
            public boolean isMethodAllowed(Object target, Method method) {
                return true;
            }

            @Override
            public boolean isPropertyAllowed(Object target, String property) {
                // Deny "class" property access
                return !"class".equals(property);
            }
        };
    }
}
