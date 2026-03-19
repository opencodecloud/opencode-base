package cloud.opencode.base.reflect.lambda;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SerializableFunctionTest Tests
 * SerializableFunctionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("SerializableFunction 测试")
class SerializableFunctionTest {

    @Nested
    @DisplayName("apply方法测试")
    class ApplyTests {

        @Test
        @DisplayName("应用函数")
        void testApply() {
            SerializableFunction<String, Integer> function = String::length;
            assertThat(function.apply("test")).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("of工厂方法测试")
    class OfTests {

        @Test
        @DisplayName("创建SerializableFunction")
        void testOf() {
            SerializableFunction<String, Integer> function = SerializableFunction.of(String::length);
            assertThat(function.apply("test")).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("identity工厂方法测试")
    class IdentityTests {

        @Test
        @DisplayName("创建恒等函数")
        void testIdentity() {
            SerializableFunction<String, String> identity = SerializableFunction.identity();
            assertThat(identity.apply("test")).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("compose方法测试")
    class ComposeTests {

        @Test
        @DisplayName("组合函数（前）")
        void testCompose() {
            SerializableFunction<String, Integer> f = String::length;
            SerializableFunction<Integer, String> before = Object::toString;
            SerializableFunction<Integer, Integer> composed = f.compose(before);
            assertThat(composed.apply(123)).isEqualTo(3); // "123".length()
        }
    }

    @Nested
    @DisplayName("andThen方法测试")
    class AndThenTests {

        @Test
        @DisplayName("组合函数（后）")
        void testAndThen() {
            SerializableFunction<String, Integer> f = String::length;
            SerializableFunction<Integer, String> after = Object::toString;
            SerializableFunction<String, String> composed = f.andThen(after);
            assertThat(composed.apply("test")).isEqualTo("4");
        }
    }

    @Nested
    @DisplayName("Serializable测试")
    class SerializableTests {

        @Test
        @DisplayName("函数是可序列化的")
        void testSerializable() {
            SerializableFunction<String, Integer> function = String::length;
            assertThat(function).isInstanceOf(java.io.Serializable.class);
        }
    }
}
