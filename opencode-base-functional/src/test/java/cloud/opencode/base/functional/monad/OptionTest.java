package cloud.opencode.base.functional.monad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * Option 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("Option 测试")
class OptionTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("some() 创建包含值的 Some")
        void testSome() {
            Option<String> option = Option.some("value");

            assertThat(option.isSome()).isTrue();
            assertThat(option.isNone()).isFalse();
            assertThat(option.get()).isEqualTo("value");
        }

        @Test
        @DisplayName("some(null) 抛出 NullPointerException")
        void testSomeWithNull() {
            assertThatThrownBy(() -> Option.some(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("none() 创建 None")
        void testNone() {
            Option<String> option = Option.none();

            assertThat(option.isNone()).isTrue();
            assertThat(option.isSome()).isFalse();
        }

        @Test
        @DisplayName("of() 从非空值创建 Some")
        void testOfNonNull() {
            Option<String> option = Option.of("value");

            assertThat(option.isSome()).isTrue();
            assertThat(option.get()).isEqualTo("value");
        }

        @Test
        @DisplayName("of(null) 创建 None")
        void testOfNull() {
            Option<String> option = Option.of(null);

            assertThat(option.isNone()).isTrue();
        }

        @Test
        @DisplayName("fromOptional() 从 Optional 创建 Option")
        void testFromOptional() {
            Option<String> some = Option.fromOptional(Optional.of("value"));
            Option<String> none = Option.fromOptional(Optional.empty());

            assertThat(some.isSome()).isTrue();
            assertThat(some.get()).isEqualTo("value");
            assertThat(none.isNone()).isTrue();
        }

        @Test
        @DisplayName("when() 条件为真时创建 Some")
        void testWhenTrue() {
            Option<String> option = Option.when(true, () -> "value");

            assertThat(option.isSome()).isTrue();
            assertThat(option.get()).isEqualTo("value");
        }

        @Test
        @DisplayName("when() 条件为假时创建 None")
        void testWhenFalse() {
            Option<String> option = Option.when(false, () -> "value");

            assertThat(option.isNone()).isTrue();
        }

        @Test
        @DisplayName("when() 条件为真但供应商返回 null 时创建 None")
        void testWhenTrueWithNullSupplier() {
            Option<String> option = Option.when(true, () -> null);

            assertThat(option.isNone()).isTrue();
        }
    }

    @Nested
    @DisplayName("get() 测试")
    class GetTests {

        @Test
        @DisplayName("Some.get() 返回值")
        void testSomeGet() {
            Option<String> option = Option.some("value");

            assertThat(option.get()).isEqualTo("value");
        }

        @Test
        @DisplayName("None.get() 抛出 NoSuchElementException")
        void testNoneGet() {
            Option<String> option = Option.none();

            assertThatThrownBy(option::get)
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("map() 测试")
    class MapTests {

        @Test
        @DisplayName("Some.map() 转换值")
        void testSomeMap() {
            Option<String> option = Option.some("hello");

            Option<Integer> mapped = option.map(String::length);

            assertThat(mapped.isSome()).isTrue();
            assertThat(mapped.get()).isEqualTo(5);
        }

        @Test
        @DisplayName("Some.map() 映射到 null 返回 None")
        void testSomeMapToNull() {
            Option<String> option = Option.some("hello");

            Option<String> mapped = option.map(s -> null);

            assertThat(mapped.isNone()).isTrue();
        }

        @Test
        @DisplayName("None.map() 返回 None")
        void testNoneMap() {
            Option<String> option = Option.none();

            Option<Integer> mapped = option.map(String::length);

            assertThat(mapped.isNone()).isTrue();
        }
    }

    @Nested
    @DisplayName("flatMap() 测试")
    class FlatMapTests {

        @Test
        @DisplayName("Some.flatMap() 成功转换")
        void testSomeFlatMap() {
            Option<String> option = Option.some("hello");

            Option<Integer> mapped = option.flatMap(s -> Option.some(s.length()));

            assertThat(mapped.isSome()).isTrue();
            assertThat(mapped.get()).isEqualTo(5);
        }

        @Test
        @DisplayName("Some.flatMap() 转换为 None")
        void testSomeFlatMapToNone() {
            Option<String> option = Option.some("hello");

            Option<Integer> mapped = option.flatMap(s -> Option.none());

            assertThat(mapped.isNone()).isTrue();
        }

        @Test
        @DisplayName("None.flatMap() 返回 None")
        void testNoneFlatMap() {
            Option<String> option = Option.none();

            Option<Integer> mapped = option.flatMap(s -> Option.some(s.length()));

            assertThat(mapped.isNone()).isTrue();
        }
    }

    @Nested
    @DisplayName("filter() 测试")
    class FilterTests {

        @Test
        @DisplayName("Some.filter() 条件满足时保留值")
        void testSomeFilterMatches() {
            Option<Integer> option = Option.some(10);

            Option<Integer> filtered = option.filter(n -> n > 5);

            assertThat(filtered.isSome()).isTrue();
            assertThat(filtered.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("Some.filter() 条件不满足时返回 None")
        void testSomeFilterNoMatch() {
            Option<Integer> option = Option.some(3);

            Option<Integer> filtered = option.filter(n -> n > 5);

            assertThat(filtered.isNone()).isTrue();
        }

        @Test
        @DisplayName("None.filter() 返回 None")
        void testNoneFilter() {
            Option<Integer> option = Option.none();

            Option<Integer> filtered = option.filter(n -> n > 5);

            assertThat(filtered.isNone()).isTrue();
        }
    }

    @Nested
    @DisplayName("fold() 测试")
    class FoldTests {

        @Test
        @DisplayName("Some.fold() 应用 ifSome 函数")
        void testSomeFold() {
            Option<String> option = Option.some("hello");

            String result = option.fold(
                    () -> "empty",
                    s -> "value: " + s
            );

            assertThat(result).isEqualTo("value: hello");
        }

        @Test
        @DisplayName("None.fold() 应用 ifNone 函数")
        void testNoneFold() {
            Option<String> option = Option.none();

            String result = option.fold(
                    () -> "empty",
                    s -> "value: " + s
            );

            assertThat(result).isEqualTo("empty");
        }
    }

    @Nested
    @DisplayName("getOrElse() 测试")
    class GetOrElseTests {

        @Test
        @DisplayName("Some.getOrElse() 返回值")
        void testSomeGetOrElse() {
            Option<String> option = Option.some("value");

            assertThat(option.getOrElse("default")).isEqualTo("value");
        }

        @Test
        @DisplayName("None.getOrElse() 返回默认值")
        void testNoneGetOrElse() {
            Option<String> option = Option.none();

            assertThat(option.getOrElse("default")).isEqualTo("default");
        }

        @Test
        @DisplayName("Some.getOrElse(Supplier) 返回值")
        void testSomeGetOrElseSupplier() {
            Option<String> option = Option.some("value");

            assertThat(option.getOrElse(() -> "default")).isEqualTo("value");
        }

        @Test
        @DisplayName("None.getOrElse(Supplier) 返回计算的默认值")
        void testNoneGetOrElseSupplier() {
            Option<String> option = Option.none();

            assertThat(option.getOrElse(() -> "default")).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("orElse() 测试")
    class OrElseTests {

        @Test
        @DisplayName("Some.orElse(Option) 返回自身")
        void testSomeOrElse() {
            Option<String> option = Option.some("value");
            Option<String> other = Option.some("other");

            assertThat(option.orElse(other)).isSameAs(option);
        }

        @Test
        @DisplayName("None.orElse(Option) 返回备选 Option")
        void testNoneOrElse() {
            Option<String> option = Option.none();
            Option<String> other = Option.some("other");

            assertThat(option.orElse(other)).isEqualTo(other);
        }

        @Test
        @DisplayName("Some.orElse(Supplier) 返回自身")
        void testSomeOrElseSupplier() {
            Option<String> option = Option.some("value");

            assertThat(option.orElse(() -> Option.some("other"))).isSameAs(option);
        }

        @Test
        @DisplayName("None.orElse(Supplier) 返回计算的备选 Option")
        void testNoneOrElseSupplier() {
            Option<String> option = Option.none();

            Option<String> result = option.orElse(() -> Option.some("other"));

            assertThat(result.get()).isEqualTo("other");
        }
    }

    @Nested
    @DisplayName("toOptional() 测试")
    class ToOptionalTests {

        @Test
        @DisplayName("Some.toOptional() 返回非空 Optional")
        void testSomeToOptional() {
            Option<String> option = Option.some("value");

            Optional<String> optional = option.toOptional();

            assertThat(optional).isPresent();
            assertThat(optional.get()).isEqualTo("value");
        }

        @Test
        @DisplayName("None.toOptional() 返回空 Optional")
        void testNoneToOptional() {
            Option<String> option = Option.none();

            Optional<String> optional = option.toOptional();

            assertThat(optional).isEmpty();
        }
    }

    @Nested
    @DisplayName("toEither() 测试")
    class ToEitherTests {

        @Test
        @DisplayName("Some.toEither() 返回 Right")
        void testSomeToEither() {
            Option<String> option = Option.some("value");

            Either<String, String> either = option.toEither("error");

            assertThat(either.isRight()).isTrue();
            assertThat(either.getRight()).contains("value");
        }

        @Test
        @DisplayName("None.toEither() 返回 Left")
        void testNoneToEither() {
            Option<String> option = Option.none();

            Either<String, String> either = option.toEither("error");

            assertThat(either.isLeft()).isTrue();
            assertThat(either.getLeft()).contains("error");
        }
    }

    @Nested
    @DisplayName("peek() 测试")
    class PeekTests {

        @Test
        @DisplayName("Some.peek() 执行操作并返回自身")
        void testSomePeek() {
            AtomicReference<String> captured = new AtomicReference<>();
            Option<String> option = Option.some("value");

            Option<String> result = option.peek(captured::set);

            assertThat(result).isSameAs(option);
            assertThat(captured.get()).isEqualTo("value");
        }

        @Test
        @DisplayName("None.peek() 不执行操作")
        void testNonePeek() {
            AtomicBoolean called = new AtomicBoolean(false);
            Option<String> option = Option.none();

            Option<String> result = option.peek(s -> called.set(true));

            assertThat(result).isSameAs(option);
            assertThat(called.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("onNone() 测试")
    class OnNoneTests {

        @Test
        @DisplayName("Some.onNone() 不执行操作")
        void testSomeOnNone() {
            AtomicBoolean called = new AtomicBoolean(false);
            Option<String> option = Option.some("value");

            Option<String> result = option.onNone(() -> called.set(true));

            assertThat(result).isSameAs(option);
            assertThat(called.get()).isFalse();
        }

        @Test
        @DisplayName("None.onNone() 执行操作")
        void testNoneOnNone() {
            AtomicBoolean called = new AtomicBoolean(false);
            Option<String> option = Option.none();

            Option<String> result = option.onNone(() -> called.set(true));

            assertThat(result).isSameAs(option);
            assertThat(called.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString 测试")
    class EqualsHashCodeToStringTests {

        @Test
        @DisplayName("Some equals 测试")
        void testSomeEquals() {
            Option<String> option1 = Option.some("value");
            Option<String> option2 = Option.some("value");
            Option<String> option3 = Option.some("other");

            assertThat(option1).isEqualTo(option2);
            assertThat(option1).isNotEqualTo(option3);
        }

        @Test
        @DisplayName("None equals 测试")
        void testNoneEquals() {
            Option<String> none1 = Option.none();
            Option<Integer> none2 = Option.none();

            assertThat(none1).isEqualTo(none2);
        }

        @Test
        @DisplayName("Some hashCode 测试")
        void testSomeHashCode() {
            Option<String> option1 = Option.some("value");
            Option<String> option2 = Option.some("value");

            assertThat(option1.hashCode()).isEqualTo(option2.hashCode());
        }

        @Test
        @DisplayName("None hashCode 测试")
        void testNoneHashCode() {
            Option<String> none = Option.none();

            assertThat(none.hashCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("Some toString 测试")
        void testSomeToString() {
            Option<String> option = Option.some("value");

            assertThat(option.toString()).isEqualTo("Some[value]");
        }

        @Test
        @DisplayName("None toString 测试")
        void testNoneToString() {
            Option<String> option = Option.none();

            assertThat(option.toString()).isEqualTo("None");
        }
    }

    @Nested
    @DisplayName("链式操作测试")
    class ChainedOperationsTests {

        @Test
        @DisplayName("链式 map 和 filter")
        void testChainedMapAndFilter() {
            Option<Integer> result = Option.of("hello")
                    .map(String::length)
                    .filter(len -> len > 3);

            assertThat(result.isSome()).isTrue();
            assertThat(result.get()).isEqualTo(5);
        }

        @Test
        @DisplayName("链式 flatMap")
        void testChainedFlatMap() {
            Option<String> result = Option.of("hello")
                    .flatMap(s -> Option.of(s.toUpperCase()))
                    .flatMap(s -> Option.of(s + "!"));

            assertThat(result.isSome()).isTrue();
            assertThat(result.get()).isEqualTo("HELLO!");
        }

        @Test
        @DisplayName("复杂链式操作")
        void testComplexChain() {
            String result = Option.of("hello")
                    .map(String::toUpperCase)
                    .filter(s -> s.length() > 3)
                    .flatMap(s -> Option.of(s + " WORLD"))
                    .peek(System.out::println)
                    .getOrElse("DEFAULT");

            assertThat(result).isEqualTo("HELLO WORLD");
        }
    }
}
