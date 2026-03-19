package cloud.opencode.base.expression.sandbox;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * SecurityPolicy Tests
 * SecurityPolicy 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("SecurityPolicy Tests | SecurityPolicy 测试")
class SecurityPolicyTest {

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("Strict policy | 严格策略")
        void testStrictPolicy() {
            SecurityPolicy policy = SecurityPolicy.strict();

            assertThat(policy.timeoutMillis()).isEqualTo(5000);
            assertThat(policy.maxIterations()).isEqualTo(10000);
            assertThat(policy.maxExpressionLength()).isEqualTo(10000);
            assertThat(policy.allowedClasses()).isNotEmpty();
            assertThat(policy.deniedMethods()).contains("getClass", "wait", "notify");
        }

        @Test
        @DisplayName("Lenient policy | 宽松策略")
        void testLenientPolicy() {
            SecurityPolicy policy = SecurityPolicy.lenient();

            assertThat(policy.timeoutMillis()).isEqualTo(30000);
            assertThat(policy.maxIterations()).isEqualTo(100000);
            assertThat(policy.allowedClasses()).isEmpty();
            assertThat(policy.deniedClasses()).contains("java.lang.Runtime");
        }
    }

    @Nested
    @DisplayName("Class Check Tests | 类检查测试")
    class ClassCheckTests {

        @Test
        @DisplayName("Check allowed class | 检查允许的类")
        void testIsClassAllowed() {
            SecurityPolicy policy = SecurityPolicy.strict();

            assertThat(policy.isClassAllowed(String.class)).isTrue();
            assertThat(policy.isClassAllowed(Integer.class)).isTrue();
            assertThat(policy.isClassAllowed(List.class)).isTrue();
        }

        @Test
        @DisplayName("Check denied class | 检查拒绝的类")
        void testDeniedClass() {
            SecurityPolicy policy = SecurityPolicy.builder()
                    .denyClass("java.lang.Runtime")
                    .build();

            assertThat(policy.isClassAllowed(Runtime.class)).isFalse();
        }

        @Test
        @DisplayName("Null class check | null 类检查")
        void testNullClassCheck() {
            SecurityPolicy policy = SecurityPolicy.strict();
            assertThat(policy.isClassAllowed(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Method Check Tests | 方法检查测试")
    class MethodCheckTests {

        @Test
        @DisplayName("Check allowed method | 检查允许的方法")
        void testIsMethodAllowed() {
            SecurityPolicy policy = SecurityPolicy.builder()
                    .denyMethod("getClass")
                    .build();

            assertThat(policy.isMethodAllowed("toString")).isTrue();
            assertThat(policy.isMethodAllowed("getClass")).isFalse();
        }

        @Test
        @DisplayName("Empty allowed means all | 空允许列表表示全部")
        void testEmptyAllowedMeansAll() {
            SecurityPolicy policy = SecurityPolicy.builder().build();
            assertThat(policy.isMethodAllowed("anyMethod")).isTrue();
        }
    }

    @Nested
    @DisplayName("Function Check Tests | 函数检查测试")
    class FunctionCheckTests {

        @Test
        @DisplayName("Check allowed function | 检查允许的函数")
        void testIsFunctionAllowed() {
            SecurityPolicy policy = SecurityPolicy.builder()
                    .denyFunction("dangerous")
                    .build();

            assertThat(policy.isFunctionAllowed("upper")).isTrue();
            assertThat(policy.isFunctionAllowed("dangerous")).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder Tests | 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("Build with all options | 使用所有选项构建")
        void testBuilderAllOptions() {
            SecurityPolicy policy = SecurityPolicy.builder()
                    .allowClass(String.class, Integer.class)
                    .denyClass("java.lang.Runtime")
                    .allowMethod("get", "set")
                    .denyMethod("execute")
                    .allowFunction("upper", "lower")
                    .denyFunction("eval")
                    .timeout(10000)
                    .maxIterations(5000)
                    .maxExpressionLength(20000)
                    .build();

            assertThat(policy.allowedClasses()).hasSize(2);
            assertThat(policy.deniedClasses()).contains("java.lang.Runtime");
            assertThat(policy.allowedMethods()).contains("get", "set");
            assertThat(policy.deniedMethods()).contains("execute");
            assertThat(policy.allowedFunctions()).contains("upper", "lower");
            assertThat(policy.deniedFunctions()).contains("eval");
            assertThat(policy.timeoutMillis()).isEqualTo(10000);
            assertThat(policy.maxIterations()).isEqualTo(5000);
            assertThat(policy.maxExpressionLength()).isEqualTo(20000);
        }
    }

    @Nested
    @DisplayName("Record Tests | 记录测试")
    class RecordTests {

        @Test
        @DisplayName("Record equality | 记录相等性")
        void testRecordEquality() {
            SecurityPolicy policy1 = SecurityPolicy.strict();
            SecurityPolicy policy2 = SecurityPolicy.strict();

            assertThat(policy1).isEqualTo(policy2);
            assertThat(policy1.hashCode()).isEqualTo(policy2.hashCode());
        }
    }
}
