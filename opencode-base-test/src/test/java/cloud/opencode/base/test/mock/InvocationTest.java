package cloud.opencode.base.test.mock;

import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * InvocationTest Tests
 * InvocationTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("Invocation Tests")
class InvocationTest {

    interface TestService {
        String getName();
        void doSomething(String arg);
        int calculate(int a, int b);
    }

    @Nested
    @DisplayName("Record Tests")
    class RecordTests {

        @Test
        @DisplayName("Should create with method, args and timestamp")
        void shouldCreateWithMethodArgsAndTimestamp() throws Exception {
            Method method = TestService.class.getMethod("getName");
            Instant timestamp = Instant.now();
            Object[] args = new Object[0];

            Invocation invocation = new Invocation(method, args, timestamp);

            assertThat(invocation.method()).isEqualTo(method);
            assertThat(invocation.args()).isEqualTo(args);
            assertThat(invocation.timestamp()).isEqualTo(timestamp);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() should create with current timestamp")
        void ofShouldCreateWithCurrentTimestamp() throws Exception {
            Method method = TestService.class.getMethod("getName");
            Instant before = Instant.now();

            Invocation invocation = Invocation.of(method, null);

            assertThat(invocation.timestamp()).isAfterOrEqualTo(before);
        }
    }

    @Nested
    @DisplayName("Method Name Tests")
    class MethodNameTests {

        @Test
        @DisplayName("methodName should return method name")
        void methodNameShouldReturnMethodName() throws Exception {
            Method method = TestService.class.getMethod("getName");
            Invocation invocation = Invocation.of(method, null);

            assertThat(invocation.methodName()).isEqualTo("getName");
        }
    }

    @Nested
    @DisplayName("Return Type Tests")
    class ReturnTypeTests {

        @Test
        @DisplayName("returnType should return method return type")
        void returnTypeShouldReturnMethodReturnType() throws Exception {
            Method method = TestService.class.getMethod("getName");
            Invocation invocation = Invocation.of(method, null);

            assertThat(invocation.returnType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("returnType should return void for void method")
        void returnTypeShouldReturnVoidForVoidMethod() throws Exception {
            Method method = TestService.class.getMethod("doSomething", String.class);
            Invocation invocation = Invocation.of(method, new Object[]{"arg"});

            assertThat(invocation.returnType()).isEqualTo(void.class);
        }
    }

    @Nested
    @DisplayName("Parameter Types Tests")
    class ParameterTypesTests {

        @Test
        @DisplayName("parameterTypes should return method parameter types")
        void parameterTypesShouldReturnMethodParameterTypes() throws Exception {
            Method method = TestService.class.getMethod("calculate", int.class, int.class);
            Invocation invocation = Invocation.of(method, new Object[]{1, 2});

            assertThat(invocation.parameterTypes()).containsExactly(int.class, int.class);
        }

        @Test
        @DisplayName("parameterTypes should return empty for no-arg method")
        void parameterTypesShouldReturnEmptyForNoArgMethod() throws Exception {
            Method method = TestService.class.getMethod("getName");
            Invocation invocation = Invocation.of(method, null);

            assertThat(invocation.parameterTypes()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Args Match Tests")
    class ArgsMatchTests {

        @Test
        @DisplayName("argsMatch should return true when args match")
        void argsMatchShouldReturnTrueWhenArgsMatch() throws Exception {
            Method method = TestService.class.getMethod("calculate", int.class, int.class);
            Invocation invocation = Invocation.of(method, new Object[]{1, 2});

            assertThat(invocation.argsMatch(1, 2)).isTrue();
        }

        @Test
        @DisplayName("argsMatch should return false when args don't match")
        void argsMatchShouldReturnFalseWhenArgsDontMatch() throws Exception {
            Method method = TestService.class.getMethod("calculate", int.class, int.class);
            Invocation invocation = Invocation.of(method, new Object[]{1, 2});

            assertThat(invocation.argsMatch(2, 1)).isFalse();
        }

        @Test
        @DisplayName("argsMatch should handle null args")
        void argsMatchShouldHandleNullArgs() throws Exception {
            Method method = TestService.class.getMethod("getName");
            Invocation invocation = Invocation.of(method, null);

            assertThat(invocation.argsMatch((Object[]) null)).isTrue();
        }
    }

    @Nested
    @DisplayName("isMethod Tests")
    class IsMethodTests {

        @Test
        @DisplayName("isMethod should return true when method name matches")
        void isMethodShouldReturnTrueWhenMethodNameMatches() throws Exception {
            Method method = TestService.class.getMethod("getName");
            Invocation invocation = Invocation.of(method, null);

            assertThat(invocation.isMethod("getName")).isTrue();
        }

        @Test
        @DisplayName("isMethod should return false when method name doesn't match")
        void isMethodShouldReturnFalseWhenMethodNameDoesntMatch() throws Exception {
            Method method = TestService.class.getMethod("getName");
            Invocation invocation = Invocation.of(method, null);

            assertThat(invocation.isMethod("doSomething")).isFalse();
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should include method name and args")
        void toStringShouldIncludeMethodNameAndArgs() throws Exception {
            Method method = TestService.class.getMethod("calculate", int.class, int.class);
            Invocation invocation = Invocation.of(method, new Object[]{1, 2});

            String str = invocation.toString();
            assertThat(str).contains("calculate");
            assertThat(str).contains("[1, 2]");
            assertThat(str).contains("at");
        }

        @Test
        @DisplayName("toString should handle null args")
        void toStringShouldHandleNullArgs() throws Exception {
            Method method = TestService.class.getMethod("getName");
            Invocation invocation = Invocation.of(method, null);

            String str = invocation.toString();
            assertThat(str).contains("getName");
            assertThat(str).contains("[]");
        }
    }
}
