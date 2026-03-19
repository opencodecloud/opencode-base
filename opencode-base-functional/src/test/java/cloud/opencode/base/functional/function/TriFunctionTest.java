package cloud.opencode.base.functional.function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * TriFunction 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("TriFunction 测试")
class TriFunctionTest {

    @Nested
    @DisplayName("apply() 测试")
    class ApplyTests {

        @Test
        @DisplayName("正常执行返回结果")
        void testApplyNormally() {
            TriFunction<Integer, Integer, Integer, Integer> add = (a, b, c) -> a + b + c;

            Integer result = add.apply(1, 2, 3);

            assertThat(result).isEqualTo(6);
        }

        @Test
        @DisplayName("可用作Lambda")
        void testAsLambda() {
            TriFunction<String, String, String, String> concat = (a, b, c) -> a + b + c;

            assertThat(concat.apply("a", "b", "c")).isEqualTo("abc");
        }

        @Test
        @DisplayName("可用于创建对象")
        void testCreateObject() {
            record Person(String name, int age, boolean active) {}

            TriFunction<String, Integer, Boolean, Person> createPerson = Person::new;

            Person person = createPerson.apply("Alice", 30, true);

            assertThat(person.name()).isEqualTo("Alice");
            assertThat(person.age()).isEqualTo(30);
            assertThat(person.active()).isTrue();
        }
    }

    @Nested
    @DisplayName("andThen() 测试")
    class AndThenTests {

        @Test
        @DisplayName("组合TriFunction和Function")
        void testAndThen() {
            TriFunction<Integer, Integer, Integer, Integer> add = (a, b, c) -> a + b + c;
            Function<Integer, Integer> doubleIt = n -> n * 2;

            TriFunction<Integer, Integer, Integer, Integer> combined = add.andThen(doubleIt);

            assertThat(combined.apply(1, 2, 3)).isEqualTo(12); // (1+2+3)*2
        }

        @Test
        @DisplayName("组合TriFunction和字符串转换")
        void testAndThenWithStringConversion() {
            TriFunction<Integer, Integer, Integer, Integer> add = (a, b, c) -> a + b + c;
            Function<Integer, String> toString = n -> "Result: " + n;

            TriFunction<Integer, Integer, Integer, String> combined = add.andThen(toString);

            assertThat(combined.apply(1, 2, 3)).isEqualTo("Result: 6");
        }

        @Test
        @DisplayName("链式组合多个Function")
        void testAndThenChain() {
            TriFunction<Integer, Integer, Integer, Integer> add = (a, b, c) -> a + b + c;

            TriFunction<Integer, Integer, Integer, String> combined = add
                    .andThen(n -> n * 2)
                    .andThen(n -> n + 1)
                    .andThen(Object::toString);

            assertThat(combined.apply(1, 2, 3)).isEqualTo("13"); // ((1+2+3)*2)+1 = 13
        }
    }

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("可作为方法引用")
        void testMethodReference() {
            record Point(int x, int y, int z) {}

            TriFunction<Integer, Integer, Integer, Point> createPoint = Point::new;

            Point point = createPoint.apply(1, 2, 3);

            assertThat(point.x()).isEqualTo(1);
            assertThat(point.y()).isEqualTo(2);
            assertThat(point.z()).isEqualTo(3);
        }

        @Test
        @DisplayName("可作为参数传递")
        void testAsParameter() {
            TriFunction<String, String, String, String> concat = (a, b, c) -> a + b + c;

            String result = applyTriFunction(concat, "Hello", " ", "World");

            assertThat(result).isEqualTo("Hello World");
        }

        private <T1, T2, T3, R> R applyTriFunction(TriFunction<T1, T2, T3, R> func, T1 t1, T2 t2, T3 t3) {
            return func.apply(t1, t2, t3);
        }
    }
}
