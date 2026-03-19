package cloud.opencode.base.reflect.lambda;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SerializableBiFunctionTest Tests
 * SerializableBiFunctionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("SerializableBiFunction 测试")
class SerializableBiFunctionTest {

    @Nested
    @DisplayName("apply方法测试")
    class ApplyTests {

        @Test
        @DisplayName("应用双参数函数")
        void testApply() {
            SerializableBiFunction<String, Integer, String> function = (s, n) -> s.repeat(n);
            assertThat(function.apply("ab", 3)).isEqualTo("ababab");
        }
    }

    @Nested
    @DisplayName("of工厂方法测试")
    class OfTests {

        @Test
        @DisplayName("创建SerializableBiFunction")
        void testOf() {
            SerializableBiFunction<String, String, String> function = SerializableBiFunction.of((a, b) -> a + b);
            assertThat(function.apply("hello", "world")).isEqualTo("helloworld");
        }
    }

    @Nested
    @DisplayName("constant工厂方法测试")
    class ConstantTests {

        @Test
        @DisplayName("创建常量函数")
        void testConstant() {
            SerializableBiFunction<String, Integer, String> function = SerializableBiFunction.constant("constant");
            assertThat(function.apply("any", 123)).isEqualTo("constant");
        }
    }

    @Nested
    @DisplayName("first工厂方法测试")
    class FirstTests {

        @Test
        @DisplayName("创建返回第一个参数的函数")
        void testFirst() {
            SerializableBiFunction<String, Integer, String> function = SerializableBiFunction.first();
            assertThat(function.apply("first", 123)).isEqualTo("first");
        }
    }

    @Nested
    @DisplayName("second工厂方法测试")
    class SecondTests {

        @Test
        @DisplayName("创建返回第二个参数的函数")
        void testSecond() {
            SerializableBiFunction<String, Integer, Integer> function = SerializableBiFunction.second();
            assertThat(function.apply("first", 123)).isEqualTo(123);
        }
    }

    @Nested
    @DisplayName("andThen方法测试")
    class AndThenTests {

        @Test
        @DisplayName("组合函数（后）")
        void testAndThen() {
            SerializableBiFunction<String, String, String> concat = (a, b) -> a + b;
            SerializableFunction<String, Integer> length = String::length;
            SerializableBiFunction<String, String, Integer> composed = concat.andThen(length);

            assertThat(composed.apply("hello", "world")).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Serializable测试")
    class SerializableTests {

        @Test
        @DisplayName("函数是可序列化的")
        void testSerializable() {
            SerializableBiFunction<String, String, String> function = (a, b) -> a + b;
            assertThat(function).isInstanceOf(java.io.Serializable.class);
        }
    }
}
