package cloud.opencode.base.parallel.pipeline;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TriFunctionTest Tests
 * TriFunctionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@DisplayName("TriFunction 测试")
class TriFunctionTest {

    @Nested
    @DisplayName("apply方法测试")
    class ApplyTests {

        @Test
        @DisplayName("应用三个参数")
        void testApply() {
            TriFunction<String, Integer, Boolean, String> fn =
                    (name, age, active) -> name + ":" + age + ":" + active;

            String result = fn.apply("John", 25, true);

            assertThat(result).isEqualTo("John:25:true");
        }

        @Test
        @DisplayName("不同类型的参数和返回值")
        void testDifferentTypes() {
            TriFunction<Integer, Integer, Integer, Integer> sum =
                    (a, b, c) -> a + b + c;

            assertThat(sum.apply(1, 2, 3)).isEqualTo(6);
        }

        @Test
        @DisplayName("处理null参数")
        void testNullArguments() {
            TriFunction<String, String, String, String> concat =
                    (a, b, c) -> (a == null ? "" : a) + (b == null ? "" : b) + (c == null ? "" : c);

            assertThat(concat.apply(null, "b", null)).isEqualTo("b");
        }
    }

    @Nested
    @DisplayName("andThen方法测试")
    class AndThenTests {

        @Test
        @DisplayName("链接另一个函数")
        void testAndThen() {
            TriFunction<Integer, Integer, Integer, Integer> sum =
                    (a, b, c) -> a + b + c;

            TriFunction<Integer, Integer, Integer, String> sumToString =
                    sum.andThen(Object::toString);

            assertThat(sumToString.apply(1, 2, 3)).isEqualTo("6");
        }

        @Test
        @DisplayName("链接多个函数")
        void testChainedAndThen() {
            TriFunction<String, String, String, String> concat =
                    (a, b, c) -> a + b + c;

            TriFunction<String, String, String, Integer> concatLength =
                    concat.andThen(String::length);

            assertThat(concatLength.apply("abc", "def", "ghi")).isEqualTo(9);
        }

        @Test
        @DisplayName("andThen参数为null时抛出异常")
        void testAndThenNull() {
            TriFunction<Integer, Integer, Integer, Integer> sum =
                    (a, b, c) -> a + b + c;

            assertThatThrownBy(() -> sum.andThen(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Lambda实现测试")
    class LambdaImplementationTests {

        @Test
        @DisplayName("使用Lambda实现")
        void testLambdaImplementation() {
            TriFunction<Double, Double, Double, Double> average =
                    (a, b, c) -> (a + b + c) / 3.0;

            assertThat(average.apply(1.0, 2.0, 3.0)).isEqualTo(2.0);
        }

        @Test
        @DisplayName("使用方法引用")
        void testMethodReference() {
            TriFunction<String, String, String, String> fn = TriFunctionTest::concatenate;

            assertThat(fn.apply("a", "b", "c")).isEqualTo("a-b-c");
        }
    }

    private static String concatenate(String a, String b, String c) {
        return a + "-" + b + "-" + c;
    }
}
