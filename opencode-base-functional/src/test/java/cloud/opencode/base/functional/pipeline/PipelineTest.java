package cloud.opencode.base.functional.pipeline;

import cloud.opencode.base.functional.monad.Try;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * Pipeline 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("Pipeline 测试")
class PipelineTest {

    @Nested
    @DisplayName("Pipeline 工厂方法测试")
    class PipelineFactoryTests {

        @Test
        @DisplayName("of() 创建 PipelineBuilder")
        void testOf() {
            Pipeline.PipelineBuilder<String> builder = Pipeline.of("hello");

            assertThat(builder.execute()).isEqualTo("hello");
        }

        @Test
        @DisplayName("identity() 创建恒等管道")
        void testIdentity() {
            Pipeline<String, String> pipeline = Pipeline.identity();

            assertThat(pipeline.apply("hello")).isEqualTo("hello");
        }

        @Test
        @DisplayName("from() 从函数创建管道")
        void testFrom() {
            Pipeline<String, Integer> pipeline = Pipeline.from(String::length);

            assertThat(pipeline.apply("hello")).isEqualTo(5);
        }

        @Test
        @DisplayName("ofCollection() 创建集合管道")
        void testOfCollection() {
            List<Integer> result = Pipeline.ofCollection(List.of(1, 2, 3))
                    .map(n -> n * 2)
                    .toList();

            assertThat(result).containsExactly(2, 4, 6);
        }
    }

    @Nested
    @DisplayName("Pipeline 操作测试")
    class PipelineOperationsTests {

        @Test
        @DisplayName("apply() 应用管道")
        void testApply() {
            Pipeline<String, Integer> pipeline = Pipeline.from(String::length);

            assertThat(pipeline.apply("hello")).isEqualTo(5);
        }

        @Test
        @DisplayName("applyTry() 安全应用管道")
        void testApplyTry() {
            Pipeline<String, Integer> pipeline = Pipeline.from(Integer::parseInt);

            Try<Integer> result = pipeline.applyTry("123");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(123);
        }

        @Test
        @DisplayName("applyTry() 异常返回 Failure")
        void testApplyTryFailure() {
            Pipeline<String, Integer> pipeline = Pipeline.from(Integer::parseInt);

            Try<Integer> result = pipeline.applyTry("not a number");

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("andThen() 链接转换")
        void testAndThen() {
            Pipeline<String, Integer> pipeline = Pipeline.<String>identity()
                    .andThen(String::trim)
                    .andThen(String::length);

            assertThat(pipeline.apply("  hello  ")).isEqualTo(5);
        }

        @Test
        @DisplayName("compose() 组合管道")
        void testCompose() {
            Pipeline<String, Integer> lengthPipeline = Pipeline.from(String::length);
            Pipeline<Integer, String> toStringPipeline = Pipeline.from(Object::toString);

            Pipeline<String, String> composed = toStringPipeline.compose(lengthPipeline);

            assertThat(composed.apply("hello")).isEqualTo("5");
        }

        @Test
        @DisplayName("toFunction() 获取底层函数")
        void testToFunction() {
            Pipeline<String, Integer> pipeline = Pipeline.from(String::length);

            var function = pipeline.toFunction();

            assertThat(function.apply("hello")).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("PipelineBuilder 测试")
    class PipelineBuilderTests {

        @Test
        @DisplayName("map() 转换值")
        void testMap() {
            String result = Pipeline.of("  hello  ")
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .execute();

            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("map() 链式调用")
        void testMapChain() {
            Integer result = Pipeline.of("hello")
                    .map(String::length)
                    .map(n -> n * 2)
                    .execute();

            assertThat(result).isEqualTo(10);
        }

        @Test
        @DisplayName("map() null 值返回 null")
        void testMapNull() {
            String result = Pipeline.of((String) null)
                    .map(String::toUpperCase)
                    .execute();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("mapTry() 成功时转换")
        void testMapTrySuccess() {
            Try<Integer> result = Pipeline.of("123")
                    .mapTry(Integer::parseInt)
                    .executeTry();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(123);
        }

        @Test
        @DisplayName("mapTry() 失败时返回 Failure")
        void testMapTryFailure() {
            Try<Integer> result = Pipeline.of("not a number")
                    .mapTry(Integer::parseInt)
                    .executeTry();

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("flatMap() 扁平映射")
        void testFlatMap() {
            String result = Pipeline.of("hello")
                    .flatMap(s -> Pipeline.of(s.toUpperCase()))
                    .execute();

            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("filter() 条件满足时保持值")
        void testFilterPass() {
            String result = Pipeline.of("hello")
                    .filter(s -> s.length() > 3)
                    .execute();

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("filter() 条件不满足时返回 null")
        void testFilterFail() {
            String result = Pipeline.of("hi")
                    .filter(s -> s.length() > 3)
                    .execute();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("peek() 执行副作用")
        void testPeek() {
            AtomicReference<String> captured = new AtomicReference<>();
            String result = Pipeline.of("hello")
                    .peek(captured::set)
                    .execute();

            assertThat(result).isEqualTo("hello");
            assertThat(captured.get()).isEqualTo("hello");
        }

        @Test
        @DisplayName("execute() 获取结果")
        void testExecute() {
            String result = Pipeline.of("hello").execute();

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("executeOptional() 包装为 Optional")
        void testExecuteOptional() {
            Optional<String> result = Pipeline.of("hello").executeOptional();

            assertThat(result).contains("hello");
        }

        @Test
        @DisplayName("executeOptional() null 返回 empty")
        void testExecuteOptionalNull() {
            Optional<String> result = Pipeline.of((String) null).executeOptional();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("executeOrElse() 有值时返回值")
        void testExecuteOrElseWithValue() {
            String result = Pipeline.of("hello").executeOrElse("default");

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("executeOrElse() null 时返回默认值")
        void testExecuteOrElseNull() {
            String result = Pipeline.of((String) null).executeOrElse("default");

            assertThat(result).isEqualTo("default");
        }

        @Test
        @DisplayName("executeOrElseGet() 有值时返回值")
        void testExecuteOrElseGetWithValue() {
            String result = Pipeline.of("hello").executeOrElseGet(() -> "default");

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("executeOrElseGet() null 时计算默认值")
        void testExecuteOrElseGetNull() {
            String result = Pipeline.of((String) null).executeOrElseGet(() -> "default");

            assertThat(result).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("TryPipelineBuilder 测试")
    class TryPipelineBuilderTests {

        @Test
        @DisplayName("map() 成功时转换")
        void testTryBuilderMap() {
            Try<Integer> result = Pipeline.of("123")
                    .mapTry(Integer::parseInt)
                    .map(n -> n * 2)
                    .executeTry();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(246);
        }

        @Test
        @DisplayName("flatMap() 成功时扁平映射")
        void testTryBuilderFlatMap() {
            Try<Integer> result = Pipeline.of("123")
                    .mapTry(Integer::parseInt)
                    .flatMap(n -> Try.success(n * 2))
                    .executeTry();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(246);
        }

        @Test
        @DisplayName("filter() 过滤")
        void testTryBuilderFilter() {
            Try<Integer> result = Pipeline.of("123")
                    .mapTry(Integer::parseInt)
                    .filter(n -> n > 100)
                    .executeTry();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(123);
        }

        @Test
        @DisplayName("recover() 从失败恢复")
        void testTryBuilderRecover() {
            Try<Integer> result = Pipeline.of("not a number")
                    .mapTry(Integer::parseInt)
                    .recover(e -> 0)
                    .executeTry();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("execute() 成功时返回值")
        void testTryBuilderExecute() {
            Integer result = Pipeline.of("123")
                    .mapTry(Integer::parseInt)
                    .execute();

            assertThat(result).isEqualTo(123);
        }

        @Test
        @DisplayName("execute() 失败时抛出异常")
        void testTryBuilderExecuteFailure() {
            assertThatThrownBy(() ->
                    Pipeline.of("not a number")
                            .mapTry(Integer::parseInt)
                            .execute()
            ).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("executeOrElse() 失败时返回默认值")
        void testTryBuilderExecuteOrElse() {
            Integer result = Pipeline.of("not a number")
                    .mapTry(Integer::parseInt)
                    .executeOrElse(0);

            assertThat(result).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("CollectionPipeline 测试")
    class CollectionPipelineTests {

        record User(String name, boolean active) {}

        @Test
        @DisplayName("map() 转换每个元素")
        void testCollectionMap() {
            List<Integer> result = Pipeline.ofCollection(List.of(1, 2, 3))
                    .map(n -> n * 2)
                    .toList();

            assertThat(result).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("filter() 过滤元素")
        void testCollectionFilter() {
            List<Integer> result = Pipeline.ofCollection(List.of(1, 2, 3, 4, 5))
                    .filter(n -> n > 2)
                    .toList();

            assertThat(result).containsExactly(3, 4, 5);
        }

        @Test
        @DisplayName("filter() 和 map() 组合")
        void testCollectionFilterAndMap() {
            List<User> users = List.of(
                    new User("Alice", true),
                    new User("Bob", false),
                    new User("Charlie", true)
            );

            List<String> result = Pipeline.ofCollection(users)
                    .filter(User::active)
                    .map(User::name)
                    .toList();

            assertThat(result).containsExactly("Alice", "Charlie");
        }

        @Test
        @DisplayName("forEach() 对每个元素执行动作")
        void testCollectionForEach() {
            AtomicReference<Integer> sum = new AtomicReference<>(0);

            Pipeline.ofCollection(List.of(1, 2, 3))
                    .forEach(n -> sum.updateAndGet(s -> s + n));

            assertThat(sum.get()).isEqualTo(6);
        }

        @Test
        @DisplayName("count() 计数元素")
        void testCollectionCount() {
            long count = Pipeline.ofCollection(List.of(1, 2, 3, 4, 5))
                    .filter(n -> n > 2)
                    .count();

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("findFirst() 查找第一个元素")
        void testCollectionFindFirst() {
            Optional<Integer> result = Pipeline.ofCollection(List.of(1, 2, 3))
                    .map(n -> n * 2)
                    .findFirst();

            assertThat(result).contains(2);
        }

        @Test
        @DisplayName("anyMatch() 任意匹配")
        void testCollectionAnyMatch() {
            boolean result = Pipeline.ofCollection(List.of(1, 2, 3))
                    .anyMatch(n -> n > 2);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("allMatch() 全部匹配")
        void testCollectionAllMatch() {
            boolean result = Pipeline.ofCollection(List.of(2, 4, 6))
                    .allMatch(n -> n % 2 == 0);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("reduce() 规约")
        void testCollectionReduce() {
            Integer result = Pipeline.ofCollection(List.of(1, 2, 3))
                    .reduce(0, Integer::sum);

            assertThat(result).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("完整管道场景测试")
    class CompleteScenarioTests {

        @Test
        @DisplayName("字符串处理管道")
        void testStringProcessingPipeline() {
            String result = Pipeline.of("  Hello World  ")
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .map(s -> s.replace(" ", "_"))
                    .execute();

            assertThat(result).isEqualTo("hello_world");
        }

        @Test
        @DisplayName("数字解析管道")
        void testNumberParsingPipeline() {
            Try<Integer> result = Pipeline.of("  42  ")
                    .map(String::trim)
                    .mapTry(Integer::parseInt)
                    .map(n -> n * 2)
                    .executeTry();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(84);
        }
    }
}
