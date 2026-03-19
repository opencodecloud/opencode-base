package cloud.opencode.base.functional.monad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * Either 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("Either 测试")
class EitherTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("left() 创建 Left")
        void testLeft() {
            Either<String, Integer> either = Either.left("error");

            assertThat(either.isLeft()).isTrue();
            assertThat(either.isRight()).isFalse();
            assertThat(either.getLeft()).contains("error");
        }

        @Test
        @DisplayName("right() 创建 Right")
        void testRight() {
            Either<String, Integer> either = Either.right(42);

            assertThat(either.isRight()).isTrue();
            assertThat(either.isLeft()).isFalse();
            assertThat(either.getRight()).contains(42);
        }

        @Test
        @DisplayName("left(null) 允许 null")
        void testLeftWithNull() {
            Either<String, Integer> either = Either.left(null);

            assertThat(either.isLeft()).isTrue();
            assertThat(either.getLeft()).isEmpty();
        }

        @Test
        @DisplayName("right(null) 允许 null")
        void testRightWithNull() {
            Either<String, Integer> either = Either.right(null);

            assertThat(either.isRight()).isTrue();
            assertThat(either.getRight()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getLeft/getRight 测试")
    class GetLeftRightTests {

        @Test
        @DisplayName("Left.getLeft() 返回值")
        void testLeftGetLeft() {
            Either<String, Integer> either = Either.left("error");

            assertThat(either.getLeft()).contains("error");
        }

        @Test
        @DisplayName("Left.getRight() 返回空 Optional")
        void testLeftGetRight() {
            Either<String, Integer> either = Either.left("error");

            assertThat(either.getRight()).isEmpty();
        }

        @Test
        @DisplayName("Right.getRight() 返回值")
        void testRightGetRight() {
            Either<String, Integer> either = Either.right(42);

            assertThat(either.getRight()).contains(42);
        }

        @Test
        @DisplayName("Right.getLeft() 返回空 Optional")
        void testRightGetLeft() {
            Either<String, Integer> either = Either.right(42);

            assertThat(either.getLeft()).isEmpty();
        }
    }

    @Nested
    @DisplayName("map() 测试")
    class MapTests {

        @Test
        @DisplayName("Right.map() 转换值")
        void testRightMap() {
            Either<String, Integer> either = Either.right(5);

            Either<String, Integer> mapped = either.map(n -> n * 2);

            assertThat(mapped.isRight()).isTrue();
            assertThat(mapped.getRight()).contains(10);
        }

        @Test
        @DisplayName("Left.map() 保持 Left")
        void testLeftMap() {
            Either<String, Integer> either = Either.left("error");

            Either<String, Integer> mapped = either.map(n -> n * 2);

            assertThat(mapped.isLeft()).isTrue();
            assertThat(mapped.getLeft()).contains("error");
        }
    }

    @Nested
    @DisplayName("flatMap() 测试")
    class FlatMapTests {

        @Test
        @DisplayName("Right.flatMap() 成功转换")
        void testRightFlatMap() {
            Either<String, Integer> either = Either.right(5);

            Either<String, Integer> mapped = either.flatMap(n -> Either.right(n * 2));

            assertThat(mapped.isRight()).isTrue();
            assertThat(mapped.getRight()).contains(10);
        }

        @Test
        @DisplayName("Right.flatMap() 转换为 Left")
        void testRightFlatMapToLeft() {
            Either<String, Integer> either = Either.right(5);

            Either<String, Integer> mapped = either.flatMap(n -> Either.left("error"));

            assertThat(mapped.isLeft()).isTrue();
            assertThat(mapped.getLeft()).contains("error");
        }

        @Test
        @DisplayName("Left.flatMap() 保持 Left")
        void testLeftFlatMap() {
            Either<String, Integer> either = Either.left("error");

            Either<String, Integer> mapped = either.flatMap(n -> Either.right(n * 2));

            assertThat(mapped.isLeft()).isTrue();
            assertThat(mapped.getLeft()).contains("error");
        }
    }

    @Nested
    @DisplayName("mapLeft() 测试")
    class MapLeftTests {

        @Test
        @DisplayName("Left.mapLeft() 转换值")
        void testLeftMapLeft() {
            Either<String, Integer> either = Either.left("error");

            Either<String, Integer> mapped = either.mapLeft(s -> s.toUpperCase());

            assertThat(mapped.isLeft()).isTrue();
            assertThat(mapped.getLeft()).contains("ERROR");
        }

        @Test
        @DisplayName("Right.mapLeft() 保持 Right")
        void testRightMapLeft() {
            Either<String, Integer> either = Either.right(42);

            Either<String, Integer> mapped = either.mapLeft(s -> s.toUpperCase());

            assertThat(mapped.isRight()).isTrue();
            assertThat(mapped.getRight()).contains(42);
        }
    }

    @Nested
    @DisplayName("bimap() 测试")
    class BimapTests {

        @Test
        @DisplayName("Left.bimap() 应用 leftMapper")
        void testLeftBimap() {
            Either<String, Integer> either = Either.left("error");

            Either<Integer, String> mapped = either.bimap(
                    String::length,
                    Object::toString
            );

            assertThat(mapped.isLeft()).isTrue();
            assertThat(mapped.getLeft()).contains(5);
        }

        @Test
        @DisplayName("Right.bimap() 应用 rightMapper")
        void testRightBimap() {
            Either<String, Integer> either = Either.right(42);

            Either<Integer, String> mapped = either.bimap(
                    String::length,
                    Object::toString
            );

            assertThat(mapped.isRight()).isTrue();
            assertThat(mapped.getRight()).contains("42");
        }
    }

    @Nested
    @DisplayName("getOrElse() 测试")
    class GetOrElseTests {

        @Test
        @DisplayName("Right.getOrElse() 返回值")
        void testRightGetOrElse() {
            Either<String, Integer> either = Either.right(42);

            assertThat(either.getOrElse(0)).isEqualTo(42);
        }

        @Test
        @DisplayName("Left.getOrElse() 返回默认值")
        void testLeftGetOrElse() {
            Either<String, Integer> either = Either.left("error");

            assertThat(either.getOrElse(0)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("orElse() 测试")
    class OrElseTests {

        @Test
        @DisplayName("Right.orElse() 返回自身")
        void testRightOrElse() {
            Either<String, Integer> either = Either.right(42);
            Either<String, Integer> other = Either.right(0);

            assertThat(either.orElse(other)).isSameAs(either);
        }

        @Test
        @DisplayName("Left.orElse() 返回备选 Either")
        void testLeftOrElse() {
            Either<String, Integer> either = Either.left("error");
            Either<String, Integer> other = Either.right(0);

            assertThat(either.orElse(other)).isEqualTo(other);
        }
    }

    @Nested
    @DisplayName("fold() 测试")
    class FoldTests {

        @Test
        @DisplayName("Left.fold() 应用 leftMapper")
        void testLeftFold() {
            Either<String, Integer> either = Either.left("error");

            String result = either.fold(
                    error -> "Error: " + error,
                    value -> "Value: " + value
            );

            assertThat(result).isEqualTo("Error: error");
        }

        @Test
        @DisplayName("Right.fold() 应用 rightMapper")
        void testRightFold() {
            Either<String, Integer> either = Either.right(42);

            String result = either.fold(
                    error -> "Error: " + error,
                    value -> "Value: " + value
            );

            assertThat(result).isEqualTo("Value: 42");
        }
    }

    @Nested
    @DisplayName("swap() 测试")
    class SwapTests {

        @Test
        @DisplayName("Left.swap() 变为 Right")
        void testLeftSwap() {
            Either<String, Integer> either = Either.left("error");

            Either<Integer, String> swapped = either.swap();

            assertThat(swapped.isRight()).isTrue();
            assertThat(swapped.getRight()).contains("error");
        }

        @Test
        @DisplayName("Right.swap() 变为 Left")
        void testRightSwap() {
            Either<String, Integer> either = Either.right(42);

            Either<Integer, String> swapped = either.swap();

            assertThat(swapped.isLeft()).isTrue();
            assertThat(swapped.getLeft()).contains(42);
        }
    }

    @Nested
    @DisplayName("peek() 测试")
    class PeekTests {

        @Test
        @DisplayName("Right.peek() 执行操作")
        void testRightPeek() {
            AtomicReference<Integer> captured = new AtomicReference<>();
            Either<String, Integer> either = Either.right(42);

            Either<String, Integer> result = either.peek(captured::set);

            assertThat(result).isSameAs(either);
            assertThat(captured.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("Left.peek() 不执行操作")
        void testLeftPeek() {
            AtomicReference<Integer> captured = new AtomicReference<>();
            Either<String, Integer> either = Either.left("error");

            Either<String, Integer> result = either.peek(captured::set);

            assertThat(result).isSameAs(either);
            assertThat(captured.get()).isNull();
        }
    }

    @Nested
    @DisplayName("peekLeft() 测试")
    class PeekLeftTests {

        @Test
        @DisplayName("Left.peekLeft() 执行操作")
        void testLeftPeekLeft() {
            AtomicReference<String> captured = new AtomicReference<>();
            Either<String, Integer> either = Either.left("error");

            Either<String, Integer> result = either.peekLeft(captured::set);

            assertThat(result).isSameAs(either);
            assertThat(captured.get()).isEqualTo("error");
        }

        @Test
        @DisplayName("Right.peekLeft() 不执行操作")
        void testRightPeekLeft() {
            AtomicReference<String> captured = new AtomicReference<>();
            Either<String, Integer> either = Either.right(42);

            Either<String, Integer> result = either.peekLeft(captured::set);

            assertThat(result).isSameAs(either);
            assertThat(captured.get()).isNull();
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString 测试")
    class EqualsHashCodeToStringTests {

        @Test
        @DisplayName("Left equals 测试")
        void testLeftEquals() {
            Either<String, Integer> left1 = Either.left("error");
            Either<String, Integer> left2 = Either.left("error");
            Either<String, Integer> left3 = Either.left("other");

            assertThat(left1).isEqualTo(left2);
            assertThat(left1).isNotEqualTo(left3);
        }

        @Test
        @DisplayName("Right equals 测试")
        void testRightEquals() {
            Either<String, Integer> right1 = Either.right(42);
            Either<String, Integer> right2 = Either.right(42);
            Either<String, Integer> right3 = Either.right(0);

            assertThat(right1).isEqualTo(right2);
            assertThat(right1).isNotEqualTo(right3);
        }

        @Test
        @DisplayName("Left 和 Right 不相等")
        void testLeftNotEqualsRight() {
            Either<Integer, Integer> left = Either.left(42);
            Either<Integer, Integer> right = Either.right(42);

            assertThat(left).isNotEqualTo(right);
        }

        @Test
        @DisplayName("Left hashCode 测试")
        void testLeftHashCode() {
            Either<String, Integer> left1 = Either.left("error");
            Either<String, Integer> left2 = Either.left("error");

            assertThat(left1.hashCode()).isEqualTo(left2.hashCode());
        }

        @Test
        @DisplayName("Right hashCode 测试")
        void testRightHashCode() {
            Either<String, Integer> right1 = Either.right(42);
            Either<String, Integer> right2 = Either.right(42);

            assertThat(right1.hashCode()).isEqualTo(right2.hashCode());
        }

        @Test
        @DisplayName("Left toString 测试")
        void testLeftToString() {
            Either<String, Integer> either = Either.left("error");

            assertThat(either.toString()).isEqualTo("Left[error]");
        }

        @Test
        @DisplayName("Right toString 测试")
        void testRightToString() {
            Either<String, Integer> either = Either.right(42);

            assertThat(either.toString()).isEqualTo("Right[42]");
        }
    }

    @Nested
    @DisplayName("Left.value() 测试")
    class LeftValueTests {

        @Test
        @DisplayName("Left.value() 返回左值")
        void testLeftValue() {
            Either.Left<String, Integer> left = new Either.Left<>("error");

            assertThat(left.value()).isEqualTo("error");
        }
    }

    @Nested
    @DisplayName("Right.value() 测试")
    class RightValueTests {

        @Test
        @DisplayName("Right.value() 返回右值")
        void testRightValue() {
            Either.Right<String, Integer> right = new Either.Right<>(42);

            assertThat(right.value()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("链式操作测试")
    class ChainedOperationsTests {

        @Test
        @DisplayName("链式 map 操作")
        void testChainedMap() {
            Either<String, Integer> result = Either.<String, Integer>right(5)
                    .map(n -> n * 2)
                    .map(n -> n + 1);

            assertThat(result.getRight()).contains(11);
        }

        @Test
        @DisplayName("链式 flatMap 操作")
        void testChainedFlatMap() {
            Either<String, Integer> result = Either.<String, Integer>right(5)
                    .flatMap(n -> Either.right(n * 2))
                    .flatMap(n -> Either.right(n + 1));

            assertThat(result.getRight()).contains(11);
        }

        @Test
        @DisplayName("链式操作中遇到 Left")
        void testChainedWithLeft() {
            Either<String, Integer> result = Either.<String, Integer>right(5)
                    .flatMap(n -> Either.<String, Integer>left("error"))
                    .map(n -> n * 2);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).contains("error");
        }
    }
}
