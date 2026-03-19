package cloud.opencode.base.functional.monad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * Validation 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("Validation 测试")
class ValidationTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("valid() 创建有效结果")
        void testValid() {
            Validation<String, Integer> validation = Validation.valid(42);

            assertThat(validation.isValid()).isTrue();
            assertThat(validation.isInvalid()).isFalse();
            assertThat(validation.getValue()).contains(42);
            assertThat(validation.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("valid(null) 允许 null")
        void testValidWithNull() {
            Validation<String, Integer> validation = Validation.valid(null);

            assertThat(validation.isValid()).isTrue();
            assertThat(validation.getValue()).isEmpty();
        }

        @Test
        @DisplayName("invalid(E) 创建带单个错误的无效结果")
        void testInvalidSingle() {
            Validation<String, Integer> validation = Validation.invalid("error");

            assertThat(validation.isInvalid()).isTrue();
            assertThat(validation.isValid()).isFalse();
            assertThat(validation.getErrors()).containsExactly("error");
            assertThat(validation.getValue()).isEmpty();
        }

        @Test
        @DisplayName("invalid(List) 创建带多个错误的无效结果")
        void testInvalidList() {
            Validation<String, Integer> validation = Validation.invalid(List.of("error1", "error2"));

            assertThat(validation.isInvalid()).isTrue();
            assertThat(validation.getErrors()).containsExactly("error1", "error2");
        }
    }

    @Nested
    @DisplayName("getValue/getErrors 测试")
    class GetValueErrorsTests {

        @Test
        @DisplayName("Valid.getValue() 返回值")
        void testValidGetValue() {
            Validation<String, Integer> validation = Validation.valid(42);

            assertThat(validation.getValue()).contains(42);
        }

        @Test
        @DisplayName("Valid.getErrors() 返回空列表")
        void testValidGetErrors() {
            Validation<String, Integer> validation = Validation.valid(42);

            assertThat(validation.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Invalid.getValue() 返回空 Optional")
        void testInvalidGetValue() {
            Validation<String, Integer> validation = Validation.invalid("error");

            assertThat(validation.getValue()).isEmpty();
        }

        @Test
        @DisplayName("Invalid.getErrors() 返回错误列表")
        void testInvalidGetErrors() {
            Validation<String, Integer> validation = Validation.invalid("error");

            assertThat(validation.getErrors()).containsExactly("error");
        }
    }

    @Nested
    @DisplayName("map() 测试")
    class MapTests {

        @Test
        @DisplayName("Valid.map() 转换值")
        void testValidMap() {
            Validation<String, Integer> validation = Validation.valid(5);

            Validation<String, Integer> mapped = validation.map(n -> n * 2);

            assertThat(mapped.isValid()).isTrue();
            assertThat(mapped.getValue()).contains(10);
        }

        @Test
        @DisplayName("Invalid.map() 保持 Invalid")
        void testInvalidMap() {
            Validation<String, Integer> validation = Validation.invalid("error");

            Validation<String, Integer> mapped = validation.map(n -> n * 2);

            assertThat(mapped.isInvalid()).isTrue();
            assertThat(mapped.getErrors()).containsExactly("error");
        }
    }

    @Nested
    @DisplayName("flatMap() 测试")
    class FlatMapTests {

        @Test
        @DisplayName("Valid.flatMap() 成功转换")
        void testValidFlatMap() {
            Validation<String, Integer> validation = Validation.valid(5);

            Validation<String, Integer> mapped = validation.flatMap(n -> Validation.valid(n * 2));

            assertThat(mapped.isValid()).isTrue();
            assertThat(mapped.getValue()).contains(10);
        }

        @Test
        @DisplayName("Valid.flatMap() 转换为 Invalid")
        void testValidFlatMapToInvalid() {
            Validation<String, Integer> validation = Validation.valid(5);

            Validation<String, Integer> mapped = validation.flatMap(n -> Validation.invalid("error"));

            assertThat(mapped.isInvalid()).isTrue();
            assertThat(mapped.getErrors()).containsExactly("error");
        }

        @Test
        @DisplayName("Invalid.flatMap() 保持 Invalid")
        void testInvalidFlatMap() {
            Validation<String, Integer> validation = Validation.invalid("error");

            Validation<String, Integer> mapped = validation.flatMap(n -> Validation.valid(n * 2));

            assertThat(mapped.isInvalid()).isTrue();
            assertThat(mapped.getErrors()).containsExactly("error");
        }
    }

    @Nested
    @DisplayName("ap() 测试")
    class ApTests {

        @Test
        @DisplayName("Valid.ap() 应用有效函数")
        void testValidApValidFunction() {
            Validation<String, Integer> validation = Validation.valid(5);
            Validation<String, Function<? super Integer, ? extends Integer>> vf = Validation.valid(n -> n * 2);

            Validation<String, Integer> result = validation.ap(vf);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getValue()).contains(10);
        }

        @Test
        @DisplayName("Valid.ap() 应用无效函数")
        void testValidApInvalidFunction() {
            Validation<String, Integer> validation = Validation.valid(5);
            Validation<String, Function<? super Integer, ? extends Integer>> vf = Validation.invalid("function error");

            Validation<String, Integer> result = validation.ap(vf);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).containsExactly("function error");
        }

        @Test
        @DisplayName("Invalid.ap() 累积错误")
        void testInvalidApAccumulatesErrors() {
            Validation<String, Integer> validation = Validation.invalid("value error");
            Validation<String, Function<? super Integer, ? extends Integer>> vf = Validation.invalid("function error");

            Validation<String, Integer> result = validation.ap(vf);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).containsExactly("value error", "function error");
        }
    }

    @Nested
    @DisplayName("fold() 测试")
    class FoldTests {

        @Test
        @DisplayName("Valid.fold() 应用 ifValid 函数")
        void testValidFold() {
            Validation<String, Integer> validation = Validation.valid(42);

            String result = validation.fold(
                    errors -> "Errors: " + errors.size(),
                    value -> "Value: " + value
            );

            assertThat(result).isEqualTo("Value: 42");
        }

        @Test
        @DisplayName("Invalid.fold() 应用 ifInvalid 函数")
        void testInvalidFold() {
            Validation<String, Integer> validation = Validation.invalid(List.of("e1", "e2"));

            String result = validation.fold(
                    errors -> "Errors: " + errors.size(),
                    value -> "Value: " + value
            );

            assertThat(result).isEqualTo("Errors: 2");
        }
    }

    @Nested
    @DisplayName("toEither() 测试")
    class ToEitherTests {

        @Test
        @DisplayName("Valid.toEither() 返回 Right")
        void testValidToEither() {
            Validation<String, Integer> validation = Validation.valid(42);

            Either<List<String>, Integer> either = validation.toEither();

            assertThat(either.isRight()).isTrue();
            assertThat(either.getRight()).contains(42);
        }

        @Test
        @DisplayName("Invalid.toEither() 返回 Left")
        void testInvalidToEither() {
            Validation<String, Integer> validation = Validation.invalid(List.of("e1", "e2"));

            Either<List<String>, Integer> either = validation.toEither();

            assertThat(either.isLeft()).isTrue();
            assertThat(either.getLeft().get()).containsExactly("e1", "e2");
        }
    }

    @Nested
    @DisplayName("combine() 两个验证测试")
    class CombineTwoTests {

        @Test
        @DisplayName("两个 Valid 组合成功")
        void testCombineTwoValidSuccess() {
            Validation<String, Integer> v1 = Validation.valid(1);
            Validation<String, Integer> v2 = Validation.valid(2);

            Validation<String, Integer> result = Validation.combine(v1, v2, Integer::sum);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getValue()).contains(3);
        }

        @Test
        @DisplayName("一个 Invalid 时失败")
        void testCombineTwoOneInvalid() {
            Validation<String, Integer> v1 = Validation.valid(1);
            Validation<String, Integer> v2 = Validation.invalid("error");

            Validation<String, Integer> result = Validation.combine(v1, v2, Integer::sum);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).containsExactly("error");
        }

        @Test
        @DisplayName("两个 Invalid 时累积错误")
        void testCombineTwoBothInvalid() {
            Validation<String, Integer> v1 = Validation.invalid("error1");
            Validation<String, Integer> v2 = Validation.invalid("error2");

            Validation<String, Integer> result = Validation.combine(v1, v2, Integer::sum);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).containsExactly("error1", "error2");
        }
    }

    @Nested
    @DisplayName("combine() 三个验证测试")
    class CombineThreeTests {

        @Test
        @DisplayName("三个 Valid 组合成功")
        void testCombineThreeValidSuccess() {
            Validation<String, Integer> v1 = Validation.valid(1);
            Validation<String, Integer> v2 = Validation.valid(2);
            Validation<String, Integer> v3 = Validation.valid(3);

            Validation<String, Integer> result = Validation.combine(v1, v2, v3, (a, b, c) -> a + b + c);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getValue()).contains(6);
        }

        @Test
        @DisplayName("部分 Invalid 时累积错误")
        void testCombineThreePartialInvalid() {
            Validation<String, Integer> v1 = Validation.invalid("error1");
            Validation<String, Integer> v2 = Validation.valid(2);
            Validation<String, Integer> v3 = Validation.invalid("error3");

            Validation<String, Integer> result = Validation.combine(v1, v2, v3, (a, b, c) -> a + b + c);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).containsExactly("error1", "error3");
        }

        @Test
        @DisplayName("全部 Invalid 时累积所有错误")
        void testCombineThreeAllInvalid() {
            Validation<String, Integer> v1 = Validation.invalid("error1");
            Validation<String, Integer> v2 = Validation.invalid("error2");
            Validation<String, Integer> v3 = Validation.invalid("error3");

            Validation<String, Integer> result = Validation.combine(v1, v2, v3, (a, b, c) -> a + b + c);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).containsExactly("error1", "error2", "error3");
        }
    }

    @Nested
    @DisplayName("sequence() 测试")
    class SequenceTests {

        @Test
        @DisplayName("全部 Valid 时返回 Valid")
        void testSequenceAllValid() {
            List<Validation<String, Integer>> validations = List.of(
                    Validation.valid(1),
                    Validation.valid(2),
                    Validation.valid(3)
            );

            Validation<String, List<Integer>> result = Validation.sequence(validations);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getValue().get()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("部分 Invalid 时累积错误")
        void testSequencePartialInvalid() {
            List<Validation<String, Integer>> validations = List.of(
                    Validation.valid(1),
                    Validation.invalid("error2"),
                    Validation.valid(3),
                    Validation.invalid("error4")
            );

            Validation<String, List<Integer>> result = Validation.sequence(validations);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).containsExactly("error2", "error4");
        }

        @Test
        @DisplayName("空列表返回空 Valid")
        void testSequenceEmptyList() {
            List<Validation<String, Integer>> validations = List.of();

            Validation<String, List<Integer>> result = Validation.sequence(validations);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getValue().get()).isEmpty();
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString 测试")
    class EqualsHashCodeToStringTests {

        @Test
        @DisplayName("Valid equals 测试")
        void testValidEquals() {
            Validation<String, Integer> v1 = Validation.valid(42);
            Validation<String, Integer> v2 = Validation.valid(42);
            Validation<String, Integer> v3 = Validation.valid(0);

            assertThat(v1).isEqualTo(v2);
            assertThat(v1).isNotEqualTo(v3);
        }

        @Test
        @DisplayName("Invalid equals 测试")
        void testInvalidEquals() {
            Validation<String, Integer> v1 = Validation.invalid(List.of("e1", "e2"));
            Validation<String, Integer> v2 = Validation.invalid(List.of("e1", "e2"));
            Validation<String, Integer> v3 = Validation.invalid("e1");

            assertThat(v1).isEqualTo(v2);
            assertThat(v1).isNotEqualTo(v3);
        }

        @Test
        @DisplayName("Valid 和 Invalid 不相等")
        void testValidNotEqualsInvalid() {
            Validation<Integer, Integer> valid = Validation.valid(42);
            Validation<Integer, Integer> invalid = Validation.invalid(42);

            assertThat(valid).isNotEqualTo(invalid);
        }

        @Test
        @DisplayName("Valid hashCode 测试")
        void testValidHashCode() {
            Validation<String, Integer> v1 = Validation.valid(42);
            Validation<String, Integer> v2 = Validation.valid(42);

            assertThat(v1.hashCode()).isEqualTo(v2.hashCode());
        }

        @Test
        @DisplayName("Invalid hashCode 测试")
        void testInvalidHashCode() {
            Validation<String, Integer> v1 = Validation.invalid(List.of("e1"));
            Validation<String, Integer> v2 = Validation.invalid(List.of("e1"));

            assertThat(v1.hashCode()).isEqualTo(v2.hashCode());
        }

        @Test
        @DisplayName("Valid toString 测试")
        void testValidToString() {
            Validation<String, Integer> validation = Validation.valid(42);

            assertThat(validation.toString()).isEqualTo("Valid[42]");
        }

        @Test
        @DisplayName("Invalid toString 测试")
        void testInvalidToString() {
            Validation<String, Integer> validation = Validation.invalid(List.of("e1", "e2"));

            assertThat(validation.toString()).isEqualTo("Invalid[[e1, e2]]");
        }
    }

    @Nested
    @DisplayName("Valid.value() 测试")
    class ValidValueTests {

        @Test
        @DisplayName("Valid.value() 返回值")
        void testValidValue() {
            Validation.Valid<String, Integer> valid = new Validation.Valid<>(42);

            assertThat(valid.value()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("Invalid.errors() 测试")
    class InvalidErrorsTests {

        @Test
        @DisplayName("Invalid.errors() 返回错误列表")
        void testInvalidErrors() {
            Validation.Invalid<String, Integer> invalid = new Validation.Invalid<>(List.of("e1", "e2"));

            assertThat(invalid.errors()).containsExactly("e1", "e2");
        }
    }

    @Nested
    @DisplayName("实际使用场景测试")
    class RealWorldUsageTests {

        record Person(String name, int age, String email) {}

        Validation<String, String> validateName(String name) {
            if (name == null || name.isBlank()) {
                return Validation.invalid("Name is required");
            }
            return Validation.valid(name);
        }

        Validation<String, Integer> validateAge(int age) {
            if (age < 0) {
                return Validation.invalid("Age cannot be negative");
            }
            if (age > 150) {
                return Validation.invalid("Age is unrealistic");
            }
            return Validation.valid(age);
        }

        Validation<String, String> validateEmail(String email) {
            if (email == null || !email.contains("@")) {
                return Validation.invalid("Invalid email format");
            }
            return Validation.valid(email);
        }

        @Test
        @DisplayName("验证通过时创建对象")
        void testValidationSuccess() {
            Validation<String, Person> result = Validation.combine(
                    validateName("John"),
                    validateAge(30),
                    validateEmail("john@example.com"),
                    Person::new
            );

            assertThat(result.isValid()).isTrue();
            Person person = result.getValue().get();
            assertThat(person.name()).isEqualTo("John");
            assertThat(person.age()).isEqualTo(30);
            assertThat(person.email()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("验证失败时累积所有错误")
        void testValidationFailureAccumulatesErrors() {
            Validation<String, Person> result = Validation.combine(
                    validateName(""),
                    validateAge(-5),
                    validateEmail("invalid"),
                    Person::new
            );

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).containsExactly(
                    "Name is required",
                    "Age cannot be negative",
                    "Invalid email format"
            );
        }
    }
}
