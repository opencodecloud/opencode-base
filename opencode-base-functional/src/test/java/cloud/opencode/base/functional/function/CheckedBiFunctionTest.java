package cloud.opencode.base.functional.function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.*;

/**
 * CheckedBiFunction 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("CheckedBiFunction 测试")
class CheckedBiFunctionTest {

    @Nested
    @DisplayName("apply() 测试")
    class ApplyTests {

        @Test
        @DisplayName("正常执行返回结果")
        void testApplyNormally() throws Exception {
            CheckedBiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;

            Integer result = add.apply(3, 5);

            assertThat(result).isEqualTo(8);
        }

        @Test
        @DisplayName("抛出受检异常")
        void testApplyThrowsCheckedException() {
            CheckedBiFunction<String, String, String> throwsIO = (a, b) -> {
                throw new IOException("IO error");
            };

            assertThatThrownBy(() -> throwsIO.apply("a", "b"))
                    .isInstanceOf(IOException.class)
                    .hasMessage("IO error");
        }

        @Test
        @DisplayName("可用作Lambda")
        void testAsLambda() throws Exception {
            CheckedBiFunction<String, String, String> concat = (a, b) -> a + b;

            assertThat(concat.apply("hello", "world")).isEqualTo("helloworld");
        }
    }

    @Nested
    @DisplayName("unchecked() 测试")
    class UncheckedTests {

        @Test
        @DisplayName("转换为标准BiFunction")
        void testUnchecked() {
            CheckedBiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;

            BiFunction<Integer, Integer, Integer> unchecked = add.unchecked();

            assertThat(unchecked.apply(3, 5)).isEqualTo(8);
        }

        @Test
        @DisplayName("受检异常包装为RuntimeException")
        void testUncheckedWrapsCheckedException() {
            CheckedBiFunction<String, String, String> throwsIO = (a, b) -> {
                throw new IOException("IO error");
            };

            BiFunction<String, String, String> unchecked = throwsIO.unchecked();

            assertThatThrownBy(() -> unchecked.apply("a", "b"))
                    .isInstanceOf(RuntimeException.class)
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("RuntimeException不被包装")
        void testUncheckedDoesNotWrapRuntimeException() {
            CheckedBiFunction<String, String, String> throwsRuntime = (a, b) -> {
                throw new IllegalArgumentException("bad arg");
            };

            BiFunction<String, String, String> unchecked = throwsRuntime.unchecked();

            assertThatThrownBy(() -> unchecked.apply("a", "b"))
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
            CheckedBiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;

            Integer result = add.applyQuietly(3, 5);

            assertThat(result).isEqualTo(8);
        }

        @Test
        @DisplayName("异常时返回null")
        void testApplyQuietlyReturnsNullOnException() {
            CheckedBiFunction<Integer, Integer, Integer> throwsError = (a, b) -> {
                throw new RuntimeException("error");
            };

            Integer result = throwsError.applyQuietly(3, 5);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("applyOrDefault() 测试")
    class ApplyOrDefaultTests {

        @Test
        @DisplayName("正常执行返回结果")
        void testApplyOrDefaultSuccess() {
            CheckedBiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;

            Integer result = add.applyOrDefault(3, 5, -1);

            assertThat(result).isEqualTo(8);
        }

        @Test
        @DisplayName("异常时返回默认值")
        void testApplyOrDefaultReturnsDefaultOnException() {
            CheckedBiFunction<Integer, Integer, Integer> throwsError = (a, b) -> {
                throw new RuntimeException("error");
            };

            Integer result = throwsError.applyOrDefault(3, 5, -1);

            assertThat(result).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("andThen() 测试")
    class AndThenTests {

        @Test
        @DisplayName("组合BiFunction和Function")
        void testAndThen() throws Exception {
            CheckedBiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
            CheckedFunction<Integer, Integer> doubleIt = n -> n * 2;

            CheckedBiFunction<Integer, Integer, Integer> combined = add.andThen(doubleIt);

            assertThat(combined.apply(3, 5)).isEqualTo(16); // (3+5)*2
        }

        @Test
        @DisplayName("第一个函数异常时传播")
        void testAndThenFirstFunctionException() {
            CheckedBiFunction<Integer, Integer, Integer> throwsError = (a, b) -> {
                throw new IOException("error");
            };
            CheckedFunction<Integer, Integer> doubleIt = n -> n * 2;

            CheckedBiFunction<Integer, Integer, Integer> combined = throwsError.andThen(doubleIt);

            assertThatThrownBy(() -> combined.apply(3, 5))
                    .isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("第二个函数异常时传播")
        void testAndThenSecondFunctionException() {
            CheckedBiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
            CheckedFunction<Integer, Integer> throwsError = n -> {
                throw new IOException("error");
            };

            CheckedBiFunction<Integer, Integer, Integer> combined = add.andThen(throwsError);

            assertThatThrownBy(() -> combined.apply(3, 5))
                    .isInstanceOf(IOException.class);
        }
    }

    @Nested
    @DisplayName("of() 测试")
    class OfTests {

        @Test
        @DisplayName("包装标准BiFunction")
        void testOf() throws Exception {
            BiFunction<String, String, String> stdFunction = (a, b) -> a + b;

            CheckedBiFunction<String, String, String> checked = CheckedBiFunction.of(stdFunction);

            assertThat(checked.apply("hello", "world")).isEqualTo("helloworld");
        }
    }
}
