package cloud.opencode.base.functional;

import cloud.opencode.base.functional.async.LazyAsync;
import cloud.opencode.base.functional.function.CheckedBiConsumer;
import cloud.opencode.base.functional.function.CheckedBiFunction;
import cloud.opencode.base.functional.monad.*;
import cloud.opencode.base.functional.optics.Lens;
import cloud.opencode.base.functional.optics.OptionalLens;
import cloud.opencode.base.functional.pattern.Case;
import cloud.opencode.base.functional.pattern.OpenMatch;
import cloud.opencode.base.functional.pattern.Pattern;
import cloud.opencode.base.functional.pipeline.Pipeline;
import cloud.opencode.base.functional.pipeline.PipeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenFunctional 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("OpenFunctional 测试")
class OpenFunctionalTest {

    // Test records
    record Person(String name, int age) {}

    @Nested
    @DisplayName("Try 方法测试")
    class TryMethodTests {

        @Test
        @DisplayName("tryOf() 创建 Try")
        void testTryOf() {
            Try<Integer> result = OpenFunctional.tryOf(() -> Integer.parseInt("42"));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("tryOf() 异常时返回 Failure")
        void testTryOfFailure() {
            Try<Integer> result = OpenFunctional.tryOf(() -> Integer.parseInt("not a number"));

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("success() 创建成功的 Try")
        void testSuccess() {
            Try<Integer> result = OpenFunctional.success(42);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("failure() 创建失败的 Try")
        void testFailure() {
            Try<Integer> result = OpenFunctional.failure(new RuntimeException("error"));

            assertThat(result.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("Either 方法测试")
    class EitherMethodTests {

        @Test
        @DisplayName("left() 创建 Left")
        void testLeft() {
            Either<String, Integer> result = OpenFunctional.left("error");

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).contains("error");
        }

        @Test
        @DisplayName("right() 创建 Right")
        void testRight() {
            Either<String, Integer> result = OpenFunctional.right(42);

            assertThat(result.isRight()).isTrue();
            assertThat(result.getRight()).contains(42);
        }
    }

    @Nested
    @DisplayName("Option 方法测试")
    class OptionMethodTests {

        @Test
        @DisplayName("some() 创建 Some")
        void testSome() {
            Option<String> result = OpenFunctional.some("value");

            assertThat(result.isSome()).isTrue();
            assertThat(result.get()).isEqualTo("value");
        }

        @Test
        @DisplayName("none() 创建 None")
        void testNone() {
            Option<String> result = OpenFunctional.none();

            assertThat(result.isNone()).isTrue();
        }

        @Test
        @DisplayName("option() 从可空值创建")
        void testOption() {
            Option<String> some = OpenFunctional.option("value");
            Option<String> none = OpenFunctional.option(null);

            assertThat(some.isSome()).isTrue();
            assertThat(none.isNone()).isTrue();
        }
    }

    @Nested
    @DisplayName("Validation 方法测试")
    class ValidationMethodTests {

        @Test
        @DisplayName("valid() 创建有效的 Validation")
        void testValid() {
            Validation<String, Integer> result = OpenFunctional.valid(42);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getValue()).contains(42);
        }

        @Test
        @DisplayName("invalid() 创建无效的 Validation")
        void testInvalid() {
            Validation<String, Integer> result = OpenFunctional.invalid("error");

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).containsExactly("error");
        }
    }

    @Nested
    @DisplayName("Lazy 方法测试")
    class LazyMethodTests {

        @Test
        @DisplayName("lazy() 创建惰性计算")
        void testLazy() {
            Lazy<Integer> result = OpenFunctional.lazy(() -> 42);

            assertThat(result.isEvaluated()).isFalse();
            assertThat(result.get()).isEqualTo(42);
            assertThat(result.isEvaluated()).isTrue();
        }
    }

    @Nested
    @DisplayName("Pattern Matching 方法测试")
    class PatternMatchingMethodTests {

        @Test
        @DisplayName("match() 开始模式匹配")
        void testMatch() {
            String result = OpenFunctional.match((Object) "hello")
                    .caseOf(String.class, s -> "String: " + s)
                    .orElse(o -> "Unknown");

            assertThat(result).isEqualTo("String: hello");
        }

        @Test
        @DisplayName("typePattern() 创建类型模式")
        void testTypePattern() {
            Pattern<Object, String> pattern = OpenFunctional.typePattern(String.class);

            assertThat(pattern.match("hello")).contains("hello");
            assertThat(pattern.match(123)).isEmpty();
        }

        @Test
        @DisplayName("caseOf() 创建类型分支")
        void testCaseOf() {
            Case<Object, String> stringCase = OpenFunctional.caseOf(String.class, s -> "String");

            assertThat(stringCase.apply("hello")).contains("String");
            assertThat(stringCase.apply(123)).isEmpty();
        }

        @Test
        @DisplayName("when() 创建谓词分支")
        void testWhen() {
            Case<Integer, String> positiveCase = OpenFunctional.when(n -> n > 0, n -> "Positive");

            assertThat(positiveCase.apply(5)).contains("Positive");
            assertThat(positiveCase.apply(-5)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Function Utilities 方法测试")
    class FunctionUtilitiesMethodTests {

        @Test
        @DisplayName("compose() 组合函数")
        void testCompose() {
            Function<String, Integer> parseAndDouble = OpenFunctional.compose(
                    Integer::parseInt,
                    n -> n * 2
            );

            assertThat(parseAndDouble.apply("21")).isEqualTo(42);
        }

        @Test
        @DisplayName("curry() 柯里化函数")
        void testCurry() {
            BiFunction<Integer, Integer, Integer> add = Integer::sum;
            Function<Integer, Function<Integer, Integer>> curried = OpenFunctional.curry(add);

            assertThat(curried.apply(1).apply(2)).isEqualTo(3);
        }

        @Test
        @DisplayName("memoize() 记忆化函数")
        void testMemoize() {
            int[] callCount = {0};
            Function<Integer, Integer> expensive = n -> {
                callCount[0]++;
                return n * 2;
            };

            Function<Integer, Integer> memoized = OpenFunctional.memoize(expensive);

            assertThat(memoized.apply(5)).isEqualTo(10);
            assertThat(memoized.apply(5)).isEqualTo(10);
            assertThat(callCount[0]).isEqualTo(1);
        }

        @Test
        @DisplayName("memoize() 带缓存大小")
        void testMemoizeWithSize() {
            Function<Integer, Integer> memoized = OpenFunctional.memoize(n -> n * 2, 100);

            assertThat(memoized.apply(5)).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Checked Functions 方法测试")
    class CheckedFunctionsMethodTests {

        @Test
        @DisplayName("checkedBiFunction() 创建可抛异常双参函数")
        void testCheckedBiFunction() {
            CheckedBiFunction<String, String, String> concat = (a, b) -> a + b;

            assertThat(OpenFunctional.checkedBiFunction(concat)).isNotNull();
        }

        @Test
        @DisplayName("checkedBiConsumer() 创建可抛异常双参消费者")
        void testCheckedBiConsumer() {
            CheckedBiConsumer<String, String> print = (a, b) -> System.out.println(a + b);

            assertThat(OpenFunctional.checkedBiConsumer(print)).isNotNull();
        }

        @Test
        @DisplayName("uncheckedBiFunction() 转换为标准 BiFunction")
        void testUncheckedBiFunction() {
            CheckedBiFunction<String, String, String> checked = (a, b) -> a + b;
            BiFunction<String, String, String> unchecked = OpenFunctional.uncheckedBiFunction(checked);

            assertThat(unchecked.apply("Hello, ", "World")).isEqualTo("Hello, World");
        }
    }

    @Nested
    @DisplayName("Lens 方法测试")
    class LensMethodTests {

        @Test
        @DisplayName("lens() 创建透镜")
        void testLens() {
            Lens<Person, String> nameLens = OpenFunctional.lens(
                    Person::name,
                    (p, n) -> new Person(n, p.age())
            );
            Person person = new Person("Alice", 30);

            assertThat(nameLens.get(person)).isEqualTo("Alice");
            assertThat(nameLens.set(person, "Bob").name()).isEqualTo("Bob");
        }

        @Test
        @DisplayName("optionalLens() 创建可选透镜")
        void testOptionalLens() {
            OptionalLens<Person, String> nameLens = OpenFunctional.optionalLens(
                    p -> Optional.of(p.name()),
                    (p, n) -> new Person(n, p.age())
            );
            Person person = new Person("Alice", 30);

            assertThat(nameLens.get(person)).contains("Alice");
        }
    }

    @Nested
    @DisplayName("Pipeline 方法测试")
    class PipelineMethodTests {

        @Test
        @DisplayName("pipeline() 创建管道")
        void testPipeline() {
            String result = OpenFunctional.pipeline("  hello  ")
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .execute();

            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("pipe() 创建管道链")
        void testPipe() {
            String result = OpenFunctional.pipe("  hello  ")
                    .then(String::trim)
                    .then(String::toUpperCase)
                    .get();

            assertThat(result).isEqualTo("HELLO");
        }
    }

    @Nested
    @DisplayName("Async 方法测试")
    class AsyncMethodTests {

        @Test
        @DisplayName("async() 异步执行")
        void testAsync() {
            CompletableFuture<Integer> future = OpenFunctional.async(() -> 42);

            assertThat(future.join()).isEqualTo(42);
        }

        @Test
        @DisplayName("asyncTimeout() 带超时异步执行")
        void testAsyncTimeout() {
            Try<Integer> result = OpenFunctional.asyncTimeout(() -> 42, Duration.ofSeconds(1));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("lazyAsync() 创建惰性异步")
        void testLazyAsync() {
            LazyAsync<Integer> lazy = OpenFunctional.lazyAsync(() -> 42);

            assertThat(lazy.isStarted()).isFalse();
            assertThat(lazy.force()).isEqualTo(42);
        }

        @Test
        @DisplayName("parallelMap() 并行映射")
        void testParallelMap() {
            List<Integer> items = List.of(1, 2, 3);

            List<Integer> result = OpenFunctional.parallelMap(items, n -> n * 2);

            assertThat(result).containsExactly(2, 4, 6);
        }
    }

    @Nested
    @DisplayName("Record 方法测试")
    class RecordMethodTests {

        // Note: Record methods are tested in RecordUtilTest with proper public records
        // Inner record classes are not accessible via reflection from RecordUtil

        @Test
        @DisplayName("recordLens() 方法存在")
        void testRecordLensExists() {
            // Just verify the method exists and can be called
            // Detailed tests are in RecordUtilTest
            assertThat(OpenFunctional.class.getDeclaredMethods())
                    .anyMatch(m -> m.getName().equals("recordLens"));
        }

        @Test
        @DisplayName("copyRecord() 方法存在")
        void testCopyRecordExists() {
            // Just verify the method exists
            assertThat(OpenFunctional.class.getDeclaredMethods())
                    .anyMatch(m -> m.getName().equals("copyRecord"));
        }

        @Test
        @DisplayName("recordToMap() 方法存在")
        void testRecordToMapExists() {
            // Just verify the method exists
            assertThat(OpenFunctional.class.getDeclaredMethods())
                    .anyMatch(m -> m.getName().equals("recordToMap"));
        }
    }

    @Nested
    @DisplayName("综合场景测试")
    class CompleteScenarioTests {

        @Test
        @DisplayName("使用 OpenFunctional 进行错误处理")
        void testErrorHandling() {
            String input = "  42  ";

            Integer result = OpenFunctional.pipeline(input)
                    .map(String::trim)
                    .mapTry(Integer::parseInt)
                    .map(n -> n * 2)
                    .recover(e -> 0)
                    .execute();

            assertThat(result).isEqualTo(84);
        }

        @Test
        @DisplayName("使用 OpenFunctional 进行模式匹配")
        void testPatternMatching() {
            Object value = 42;

            String result = OpenFunctional.match(value)
                    .caseOf(String.class, s -> "String: " + s)
                    .caseOf(Integer.class, n -> "Integer: " + n)
                    .orElse(o -> "Unknown");

            assertThat(result).isEqualTo("Integer: 42");
        }

        @Test
        @DisplayName("使用 OpenFunctional 进行函数组合")
        void testFunctionComposition() {
            Function<String, String> process = OpenFunctional.compose(
                    OpenFunctional.compose(String::trim, String::toLowerCase),
                    s -> s.replace(" ", "_")
            );

            assertThat(process.apply("  Hello World  ")).isEqualTo("hello_world");
        }
    }
}
