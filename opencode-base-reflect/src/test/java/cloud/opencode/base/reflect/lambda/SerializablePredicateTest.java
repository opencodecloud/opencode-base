package cloud.opencode.base.reflect.lambda;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SerializablePredicateTest Tests
 * SerializablePredicateTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("SerializablePredicate 测试")
class SerializablePredicateTest {

    @Nested
    @DisplayName("test方法测试")
    class TestMethodTests {

        @Test
        @DisplayName("测试谓词")
        void testTest() {
            SerializablePredicate<String> predicate = s -> s.length() > 3;
            assertThat(predicate.test("test")).isTrue();
            assertThat(predicate.test("ab")).isFalse();
        }
    }

    @Nested
    @DisplayName("of工厂方法测试")
    class OfTests {

        @Test
        @DisplayName("创建SerializablePredicate")
        void testOf() {
            SerializablePredicate<String> predicate = SerializablePredicate.of(s -> s.length() > 3);
            assertThat(predicate.test("test")).isTrue();
        }
    }

    @Nested
    @DisplayName("alwaysTrue工厂方法测试")
    class AlwaysTrueTests {

        @Test
        @DisplayName("创建总是true的谓词")
        void testAlwaysTrue() {
            SerializablePredicate<String> predicate = SerializablePredicate.alwaysTrue();
            assertThat(predicate.test("any")).isTrue();
            assertThat(predicate.test(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("alwaysFalse工厂方法测试")
    class AlwaysFalseTests {

        @Test
        @DisplayName("创建总是false的谓词")
        void testAlwaysFalse() {
            SerializablePredicate<String> predicate = SerializablePredicate.alwaysFalse();
            assertThat(predicate.test("any")).isFalse();
            assertThat(predicate.test(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isNull工厂方法测试")
    class IsNullTests {

        @Test
        @DisplayName("创建检查null的谓词")
        void testIsNull() {
            SerializablePredicate<String> predicate = SerializablePredicate.isNull();
            assertThat(predicate.test(null)).isTrue();
            assertThat(predicate.test("test")).isFalse();
        }
    }

    @Nested
    @DisplayName("nonNull工厂方法测试")
    class NonNullTests {

        @Test
        @DisplayName("创建检查非null的谓词")
        void testNonNull() {
            SerializablePredicate<String> predicate = SerializablePredicate.nonNull();
            assertThat(predicate.test("test")).isTrue();
            assertThat(predicate.test(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isEqual工厂方法测试")
    class IsEqualTests {

        @Test
        @DisplayName("创建相等检查谓词")
        void testIsEqual() {
            SerializablePredicate<String> predicate = SerializablePredicate.isEqual("test");
            assertThat(predicate.test("test")).isTrue();
            assertThat(predicate.test("other")).isFalse();
        }

        @Test
        @DisplayName("null目标值返回isNull谓词")
        void testIsEqualNull() {
            SerializablePredicate<String> predicate = SerializablePredicate.isEqual(null);
            assertThat(predicate.test(null)).isTrue();
            assertThat(predicate.test("test")).isFalse();
        }
    }

    @Nested
    @DisplayName("and方法测试")
    class AndTests {

        @Test
        @DisplayName("组合谓词（AND）")
        void testAnd() {
            SerializablePredicate<String> p1 = s -> s.length() > 2;
            SerializablePredicate<String> p2 = s -> s.startsWith("t");
            SerializablePredicate<String> combined = p1.and(p2);

            assertThat(combined.test("test")).isTrue();
            assertThat(combined.test("ab")).isFalse();
            assertThat(combined.test("abc")).isFalse();
        }
    }

    @Nested
    @DisplayName("negate方法测试")
    class NegateTests {

        @Test
        @DisplayName("否定谓词")
        void testNegate() {
            SerializablePredicate<String> predicate = SerializablePredicate.<String>isNull().negate();
            assertThat(predicate.test("test")).isTrue();
            assertThat(predicate.test(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("or方法测试")
    class OrTests {

        @Test
        @DisplayName("组合谓词（OR）")
        void testOr() {
            SerializablePredicate<String> p1 = s -> s.length() > 5;
            SerializablePredicate<String> p2 = s -> s.startsWith("t");
            SerializablePredicate<String> combined = p1.or(p2);

            assertThat(combined.test("test")).isTrue(); // startsWith "t"
            assertThat(combined.test("abcdef")).isTrue(); // length > 5
            assertThat(combined.test("abc")).isFalse(); // neither
        }
    }

    @Nested
    @DisplayName("Serializable测试")
    class SerializableTests {

        @Test
        @DisplayName("谓词是可序列化的")
        void testSerializable() {
            SerializablePredicate<String> predicate = s -> true;
            assertThat(predicate).isInstanceOf(java.io.Serializable.class);
        }
    }
}
