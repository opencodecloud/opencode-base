package cloud.opencode.base.core.func;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * CheckedFunction 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("CheckedFunction 测试")
class CheckedFunctionTest {

    @Nested
    @DisplayName("apply 测试")
    class ApplyTests {

        @Test
        @DisplayName("正常返回值")
        void testNormalReturn() throws Exception {
            CheckedFunction<String, Integer> func = String::length;
            assertThat(func.apply("hello")).isEqualTo(5);
        }

        @Test
        @DisplayName("返回 null")
        void testReturnNull() throws Exception {
            CheckedFunction<String, String> func = s -> null;
            assertThat(func.apply("test")).isNull();
        }

        @Test
        @DisplayName("抛出受检异常")
        void testThrowCheckedException() {
            CheckedFunction<String, Integer> func = s -> {
                throw new IOException("IO error");
            };

            assertThatThrownBy(() -> func.apply("test"))
                    .isInstanceOf(IOException.class)
                    .hasMessage("IO error");
        }
    }

    @Nested
    @DisplayName("unchecked 测试")
    class UncheckedTests {

        @Test
        @DisplayName("正常返回值")
        void testNormalReturn() {
            CheckedFunction<String, Integer> checked = String::length;
            Function<String, Integer> func = checked.unchecked();

            assertThat(func.apply("hello")).isEqualTo(5);
        }

        @Test
        @DisplayName("受检异常包装为 RuntimeException")
        void testCheckedExceptionWrapped() {
            CheckedFunction<String, Integer> checked = s -> {
                throw new IOException("IO error");
            };
            Function<String, Integer> func = checked.unchecked();

            assertThatThrownBy(() -> func.apply("test"))
                    .isInstanceOf(RuntimeException.class)
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("运行时异常直接抛出")
        void testRuntimeExceptionDirectly() {
            CheckedFunction<String, Integer> checked = s -> {
                throw new IllegalArgumentException("Invalid");
            };
            Function<String, Integer> func = checked.unchecked();

            assertThatThrownBy(() -> func.apply("test"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("applyQuietly 测试")
    class ApplyQuietlyTests {

        @Test
        @DisplayName("正常返回值")
        void testNormalReturn() {
            CheckedFunction<String, Integer> func = String::length;
            assertThat(func.applyQuietly("hello")).isEqualTo(5);
        }

        @Test
        @DisplayName("异常时返回 null")
        void testReturnNullOnException() {
            CheckedFunction<String, Integer> func = s -> {
                throw new IOException("IO error");
            };
            assertThat(func.applyQuietly("test")).isNull();
        }
    }

    @Nested
    @DisplayName("applyOrDefault 测试")
    class ApplyOrDefaultTests {

        @Test
        @DisplayName("正常返回值")
        void testNormalReturn() {
            CheckedFunction<String, Integer> func = String::length;
            assertThat(func.applyOrDefault("hello", -1)).isEqualTo(5);
        }

        @Test
        @DisplayName("异常时返回默认值")
        void testReturnDefaultOnException() {
            CheckedFunction<String, Integer> func = s -> {
                throw new IOException("IO error");
            };
            assertThat(func.applyOrDefault("test", -1)).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("andThen 测试")
    class AndThenTests {

        @Test
        @DisplayName("组合两个函数")
        void testComposition() throws Exception {
            CheckedFunction<String, Integer> first = String::length;
            CheckedFunction<Integer, String> second = i -> "Length: " + i;

            String result = first.andThen(second).apply("hello");
            assertThat(result).isEqualTo("Length: 5");
        }

        @Test
        @DisplayName("第一个函数抛异常")
        void testFirstThrows() {
            CheckedFunction<String, Integer> first = s -> {
                throw new IOException("First failed");
            };
            CheckedFunction<Integer, String> second = i -> "Result: " + i;

            assertThatThrownBy(() -> first.andThen(second).apply("test"))
                    .isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("第二个函数抛异常")
        void testSecondThrows() {
            CheckedFunction<String, Integer> first = String::length;
            CheckedFunction<Integer, String> second = i -> {
                throw new IOException("Second failed");
            };

            assertThatThrownBy(() -> first.andThen(second).apply("test"))
                    .isInstanceOf(IOException.class);
        }
    }

    @Nested
    @DisplayName("compose 测试")
    class ComposeTests {

        @Test
        @DisplayName("组合两个函数")
        void testComposition() throws Exception {
            CheckedFunction<Integer, String> first = i -> "Number: " + i;
            CheckedFunction<String, Integer> before = String::length;

            String result = first.compose(before).apply("hello");
            assertThat(result).isEqualTo("Number: 5");
        }
    }

    @Nested
    @DisplayName("of 测试")
    class OfTests {

        @Test
        @DisplayName("从 Function 创建 CheckedFunction")
        void testOfFunction() throws Exception {
            Function<String, Integer> func = String::length;
            CheckedFunction<String, Integer> checked = CheckedFunction.of(func);

            assertThat(checked.apply("hello")).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("identity 测试")
    class IdentityTests {

        @Test
        @DisplayName("返回输入值")
        void testIdentity() throws Exception {
            CheckedFunction<String, String> identity = CheckedFunction.identity();
            assertThat(identity.apply("hello")).isEqualTo("hello");
        }
    }
}
