package cloud.opencode.base.functional.function;

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
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("CheckedFunction 测试")
class CheckedFunctionTest {

    @Nested
    @DisplayName("apply() 测试")
    class ApplyTests {

        @Test
        @DisplayName("正常执行返回结果")
        void testApplyNormally() throws Exception {
            CheckedFunction<String, Integer> parseInt = Integer::parseInt;

            Integer result = parseInt.apply("42");

            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("抛出受检异常")
        void testApplyThrowsCheckedException() {
            CheckedFunction<String, String> throwsIO = s -> {
                throw new IOException("IO error");
            };

            assertThatThrownBy(() -> throwsIO.apply("test"))
                    .isInstanceOf(IOException.class)
                    .hasMessage("IO error");
        }

        @Test
        @DisplayName("可用作Lambda")
        void testAsLambda() throws Exception {
            CheckedFunction<String, String> toUpper = String::toUpperCase;

            assertThat(toUpper.apply("hello")).isEqualTo("HELLO");
        }
    }

    @Nested
    @DisplayName("unchecked() 测试")
    class UncheckedTests {

        @Test
        @DisplayName("转换为标准Function")
        void testUnchecked() {
            CheckedFunction<String, Integer> parseInt = Integer::parseInt;

            Function<String, Integer> unchecked = parseInt.unchecked();

            assertThat(unchecked.apply("123")).isEqualTo(123);
        }

        @Test
        @DisplayName("受检异常包装为RuntimeException")
        void testUncheckedWrapsCheckedException() {
            CheckedFunction<String, String> throwsIO = s -> {
                throw new IOException("IO error");
            };

            Function<String, String> unchecked = throwsIO.unchecked();

            assertThatThrownBy(() -> unchecked.apply("test"))
                    .isInstanceOf(RuntimeException.class)
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("RuntimeException不被包装")
        void testUncheckedDoesNotWrapRuntimeException() {
            CheckedFunction<String, String> throwsRuntime = s -> {
                throw new IllegalArgumentException("bad arg");
            };

            Function<String, String> unchecked = throwsRuntime.unchecked();

            assertThatThrownBy(() -> unchecked.apply("test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("bad arg");
        }
    }

    @Nested
    @DisplayName("applyQuietly() 测试")
    class ApplyQuietlyTests {

        @Test
        @DisplayName("正常执行返回结果")
        void testApplyQuietlySuccess() {
            CheckedFunction<String, Integer> parseInt = Integer::parseInt;

            Integer result = parseInt.applyQuietly("42");

            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("异常时返回null")
        void testApplyQuietlyReturnsNullOnException() {
            CheckedFunction<String, Integer> parseInt = Integer::parseInt;

            Integer result = parseInt.applyQuietly("not a number");

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("applyOrDefault() 测试")
    class ApplyOrDefaultTests {

        @Test
        @DisplayName("正常执行返回结果")
        void testApplyOrDefaultSuccess() {
            CheckedFunction<String, Integer> parseInt = Integer::parseInt;

            Integer result = parseInt.applyOrDefault("42", -1);

            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("异常时返回默认值")
        void testApplyOrDefaultReturnsDefaultOnException() {
            CheckedFunction<String, Integer> parseInt = Integer::parseInt;

            Integer result = parseInt.applyOrDefault("not a number", -1);

            assertThat(result).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("andThen() 测试")
    class AndThenTests {

        @Test
        @DisplayName("组合两个函数")
        void testAndThen() throws Exception {
            CheckedFunction<String, Integer> parseInt = Integer::parseInt;
            CheckedFunction<Integer, Integer> doubleIt = n -> n * 2;

            CheckedFunction<String, Integer> combined = parseInt.andThen(doubleIt);

            assertThat(combined.apply("5")).isEqualTo(10);
        }

        @Test
        @DisplayName("第一个函数异常时传播")
        void testAndThenFirstFunctionException() {
            CheckedFunction<String, Integer> parseInt = Integer::parseInt;
            CheckedFunction<Integer, Integer> doubleIt = n -> n * 2;

            CheckedFunction<String, Integer> combined = parseInt.andThen(doubleIt);

            assertThatThrownBy(() -> combined.apply("not a number"))
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("第二个函数异常时传播")
        void testAndThenSecondFunctionException() {
            CheckedFunction<String, Integer> parseInt = Integer::parseInt;
            CheckedFunction<Integer, Integer> throwsError = n -> {
                throw new IOException("error");
            };

            CheckedFunction<String, Integer> combined = parseInt.andThen(throwsError);

            assertThatThrownBy(() -> combined.apply("5"))
                    .isInstanceOf(IOException.class);
        }
    }

    @Nested
    @DisplayName("compose() 测试")
    class ComposeTests {

        @Test
        @DisplayName("组合两个函数")
        void testCompose() throws Exception {
            CheckedFunction<Integer, Integer> doubleIt = n -> n * 2;
            CheckedFunction<String, Integer> parseInt = Integer::parseInt;

            CheckedFunction<String, Integer> combined = doubleIt.compose(parseInt);

            assertThat(combined.apply("5")).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("identity() 测试")
    class IdentityTests {

        @Test
        @DisplayName("返回输入值")
        void testIdentity() throws Exception {
            CheckedFunction<String, String> identity = CheckedFunction.identity();

            assertThat(identity.apply("test")).isEqualTo("test");
        }

        @Test
        @DisplayName("处理null")
        void testIdentityWithNull() throws Exception {
            CheckedFunction<String, String> identity = CheckedFunction.identity();

            assertThat(identity.apply(null)).isNull();
        }
    }

    @Nested
    @DisplayName("of() 测试")
    class OfTests {

        @Test
        @DisplayName("包装标准Function")
        void testOf() throws Exception {
            Function<String, Integer> stdFunction = String::length;

            CheckedFunction<String, Integer> checked = CheckedFunction.of(stdFunction);

            assertThat(checked.apply("hello")).isEqualTo(5);
        }
    }
}
