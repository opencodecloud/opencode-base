package cloud.opencode.base.core.assertion;

import cloud.opencode.base.core.exception.OpenIllegalArgumentException;
import cloud.opencode.base.core.exception.OpenIllegalStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenAssert 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenAssert 测试")
class OpenAssertTest {

    @Nested
    @DisplayName("notNull 测试")
    class NotNullTests {

        @Test
        @DisplayName("notNull 非 null 通过")
        void testNotNullPasses() {
            String result = OpenAssert.notNull("test", "Should not be null");
            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("notNull null 抛异常")
        void testNotNullThrows() {
            assertThatThrownBy(() -> OpenAssert.notNull(null, "Value is null"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("Value is null");
        }

        @Test
        @DisplayName("notNull 格式化消息")
        void testNotNullFormattedMessage() {
            assertThatThrownBy(() -> OpenAssert.notNull(null, "Value %s is %s", "x", "null"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("Value x is null");
        }
    }

    @Nested
    @DisplayName("isTrue 测试")
    class IsTrueTests {

        @Test
        @DisplayName("isTrue true 通过")
        void testIsTruePasses() {
            assertThatNoException().isThrownBy(() -> OpenAssert.isTrue(true, "Should be true"));
        }

        @Test
        @DisplayName("isTrue false 抛异常")
        void testIsTrueThrows() {
            assertThatThrownBy(() -> OpenAssert.isTrue(false, "Condition failed"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("Condition failed");
        }

        @Test
        @DisplayName("isTrue 格式化消息")
        void testIsTrueFormattedMessage() {
            assertThatThrownBy(() -> OpenAssert.isTrue(false, "Value %d is not positive", -1))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("Value -1 is not positive");
        }
    }

    @Nested
    @DisplayName("isFalse 测试")
    class IsFalseTests {

        @Test
        @DisplayName("isFalse false 通过")
        void testIsFalsePasses() {
            assertThatNoException().isThrownBy(() -> OpenAssert.isFalse(false, "Should be false"));
        }

        @Test
        @DisplayName("isFalse true 抛异常")
        void testIsFalseThrows() {
            assertThatThrownBy(() -> OpenAssert.isFalse(true, "Condition should be false"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("Condition should be false");
        }
    }

    @Nested
    @DisplayName("state 测试")
    class StateTests {

        @Test
        @DisplayName("state true 通过")
        void testStatePasses() {
            assertThatNoException().isThrownBy(() -> OpenAssert.state(true, "State is valid"));
        }

        @Test
        @DisplayName("state false 抛 OpenIllegalStateException")
        void testStateThrows() {
            assertThatThrownBy(() -> OpenAssert.state(false, "Invalid state"))
                    .isInstanceOf(OpenIllegalStateException.class)
                    .hasMessageContaining("Invalid state");
        }
    }

    @Nested
    @DisplayName("notEmpty CharSequence 测试")
    class NotEmptyCharSequenceTests {

        @Test
        @DisplayName("notEmpty 非空字符串通过")
        void testNotEmptyStringPasses() {
            String result = OpenAssert.notEmpty("test", "Should not be empty");
            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("notEmpty null 字符串抛异常")
        void testNotEmptyNullThrows() {
            assertThatThrownBy(() -> OpenAssert.notEmpty((String) null, "String is null"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("notEmpty 空字符串抛异常")
        void testNotEmptyEmptyThrows() {
            assertThatThrownBy(() -> OpenAssert.notEmpty("", "String is empty"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("notBlank 测试")
    class NotBlankTests {

        @Test
        @DisplayName("notBlank 非空白字符串通过")
        void testNotBlankPasses() {
            String result = OpenAssert.notBlank("test", "Should not be blank");
            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("notBlank null 抛异常")
        void testNotBlankNullThrows() {
            assertThatThrownBy(() -> OpenAssert.notBlank(null, "String is null"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("notBlank 空白字符串抛异常")
        void testNotBlankBlankThrows() {
            assertThatThrownBy(() -> OpenAssert.notBlank("   ", "String is blank"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("matchesPattern 测试")
    class MatchesPatternTests {

        @Test
        @DisplayName("matchesPattern 匹配通过")
        void testMatchesPatternPasses() {
            assertThatNoException().isThrownBy(() ->
                    OpenAssert.matchesPattern("abc123", "[a-z]+\\d+", "Pattern mismatch"));
        }

        @Test
        @DisplayName("matchesPattern 不匹配抛异常")
        void testMatchesPatternThrows() {
            assertThatThrownBy(() ->
                    OpenAssert.matchesPattern("abc", "\\d+", "Must be numeric"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("Must be numeric");
        }

        @Test
        @DisplayName("matchesPattern null 输入抛异常")
        void testMatchesPatternNullThrows() {
            assertThatThrownBy(() ->
                    OpenAssert.matchesPattern(null, ".*", "Input is null"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("notEmpty Collection 测试")
    class NotEmptyCollectionTests {

        @Test
        @DisplayName("notEmpty 非空集合通过")
        void testNotEmptyCollectionPasses() {
            List<String> list = List.of("a", "b");
            List<String> result = OpenAssert.notEmpty(list, "Should not be empty");
            assertThat(result).isSameAs(list);
        }

        @Test
        @DisplayName("notEmpty null 集合抛异常")
        void testNotEmptyNullCollectionThrows() {
            assertThatThrownBy(() -> OpenAssert.notEmpty((Collection<?>) null, "Collection is null"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("notEmpty 空集合抛异常")
        void testNotEmptyEmptyCollectionThrows() {
            assertThatThrownBy(() -> OpenAssert.notEmpty(List.of(), "Collection is empty"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("notEmpty Map 测试")
    class NotEmptyMapTests {

        @Test
        @DisplayName("notEmpty 非空 Map 通过")
        void testNotEmptyMapPasses() {
            Map<String, Object> map = Map.of("key", "value");
            Map<String, Object> result = OpenAssert.notEmpty(map, "Should not be empty");
            assertThat(result).isSameAs(map);
        }

        @Test
        @DisplayName("notEmpty null Map 抛异常")
        void testNotEmptyNullMapThrows() {
            assertThatThrownBy(() -> OpenAssert.notEmpty((Map<?, ?>) null, "Map is null"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("notEmpty 空 Map 抛异常")
        void testNotEmptyEmptyMapThrows() {
            assertThatThrownBy(() -> OpenAssert.notEmpty(Map.of(), "Map is empty"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("notEmpty Array 测试")
    class NotEmptyArrayTests {

        @Test
        @DisplayName("notEmpty 非空数组通过")
        void testNotEmptyArrayPasses() {
            String[] array = {"a", "b"};
            String[] result = OpenAssert.notEmpty(array, "Should not be empty");
            assertThat(result).isSameAs(array);
        }

        @Test
        @DisplayName("notEmpty null 数组抛异常")
        void testNotEmptyNullArrayThrows() {
            assertThatThrownBy(() -> OpenAssert.notEmpty((String[]) null, "Array is null"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("notEmpty 空数组抛异常")
        void testNotEmptyEmptyArrayThrows() {
            assertThatThrownBy(() -> OpenAssert.notEmpty(new String[0], "Array is empty"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("noNullElements Array 测试")
    class NoNullElementsArrayTests {

        @Test
        @DisplayName("noNullElements 无 null 通过")
        void testNoNullElementsArrayPasses() {
            String[] array = {"a", "b", "c"};
            String[] result = OpenAssert.noNullElements(array, "Has null elements");
            assertThat(result).isSameAs(array);
        }

        @Test
        @DisplayName("noNullElements 有 null 抛异常")
        void testNoNullElementsArrayThrows() {
            String[] array = {"a", null, "c"};
            assertThatThrownBy(() -> OpenAssert.noNullElements(array, "Has null elements"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("noNullElements null 数组返回 null")
        void testNoNullElementsNullArray() {
            String[] result = OpenAssert.noNullElements((String[]) null, "Has null elements");
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("noNullElements Iterable 测试")
    class NoNullElementsIterableTests {

        @Test
        @DisplayName("noNullElements 无 null 通过")
        void testNoNullElementsIterablePasses() {
            List<String> list = List.of("a", "b", "c");
            Iterable<String> result = OpenAssert.noNullElements(list, "Has null elements");
            assertThat(result).isSameAs(list);
        }

        @Test
        @DisplayName("noNullElements 有 null 抛异常")
        void testNoNullElementsIterableThrows() {
            List<String> list = new ArrayList<>();
            list.add("a");
            list.add(null);
            list.add("c");

            assertThatThrownBy(() -> OpenAssert.noNullElements(list, "Has null elements"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("inclusiveBetween 测试")
    class InclusiveBetweenTests {

        @Test
        @DisplayName("inclusiveBetween 范围内通过")
        void testInclusiveBetweenPasses() {
            Integer result = OpenAssert.inclusiveBetween(Integer.valueOf(1), Integer.valueOf(10), Integer.valueOf(5));
            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("inclusiveBetween 边界值通过")
        void testInclusiveBetweenBoundary() {
            assertThat(OpenAssert.inclusiveBetween(Integer.valueOf(1), Integer.valueOf(10), Integer.valueOf(1))).isEqualTo(1);
            assertThat(OpenAssert.inclusiveBetween(Integer.valueOf(1), Integer.valueOf(10), Integer.valueOf(10))).isEqualTo(10);
        }

        @Test
        @DisplayName("inclusiveBetween 超出范围抛异常")
        void testInclusiveBetweenThrows() {
            assertThatThrownBy(() -> OpenAssert.inclusiveBetween(1, 10, 11))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("inclusiveBetween long")
        void testInclusiveBetweenLong() {
            assertThatNoException().isThrownBy(() -> OpenAssert.inclusiveBetween(1L, 100L, 50L));
            assertThatThrownBy(() -> OpenAssert.inclusiveBetween(1L, 100L, 101L))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("inclusiveBetween double")
        void testInclusiveBetweenDouble() {
            assertThatNoException().isThrownBy(() -> OpenAssert.inclusiveBetween(1.0, 10.0, 5.5));
            assertThatThrownBy(() -> OpenAssert.inclusiveBetween(1.0, 10.0, 10.1))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("exclusiveBetween 测试")
    class ExclusiveBetweenTests {

        @Test
        @DisplayName("exclusiveBetween 范围内通过")
        void testExclusiveBetweenPasses() {
            Integer result = OpenAssert.exclusiveBetween(Integer.valueOf(1), Integer.valueOf(10), Integer.valueOf(5));
            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("exclusiveBetween 边界值抛异常")
        void testExclusiveBetweenBoundaryThrows() {
            assertThatThrownBy(() -> OpenAssert.exclusiveBetween(1, 10, 1))
                    .isInstanceOf(OpenIllegalArgumentException.class);
            assertThatThrownBy(() -> OpenAssert.exclusiveBetween(1, 10, 10))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("exclusiveBetween long")
        void testExclusiveBetweenLong() {
            assertThatNoException().isThrownBy(() -> OpenAssert.exclusiveBetween(1L, 100L, 50L));
            assertThatThrownBy(() -> OpenAssert.exclusiveBetween(1L, 100L, 1L))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("exclusiveBetween double")
        void testExclusiveBetweenDouble() {
            assertThatNoException().isThrownBy(() -> OpenAssert.exclusiveBetween(1.0, 10.0, 5.5));
            assertThatThrownBy(() -> OpenAssert.exclusiveBetween(1.0, 10.0, 1.0))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("validIndex 测试")
    class ValidIndexTests {

        @Test
        @DisplayName("validIndex 有效索引通过")
        void testValidIndexPasses() {
            assertThatNoException().isThrownBy(() -> OpenAssert.validIndex(0, 5));
            assertThatNoException().isThrownBy(() -> OpenAssert.validIndex(4, 5));
        }

        @Test
        @DisplayName("validIndex 负索引抛异常")
        void testValidIndexNegativeThrows() {
            assertThatThrownBy(() -> OpenAssert.validIndex(-1, 5))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("validIndex 超出范围抛异常")
        void testValidIndexOutOfBoundsThrows() {
            assertThatThrownBy(() -> OpenAssert.validIndex(5, 5))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("validIndex 数组")
        void testValidIndexArray() {
            String[] array = {"a", "b", "c"};
            String[] result = OpenAssert.validIndex(array, 1, "Invalid index");
            assertThat(result).isSameAs(array);

            assertThatThrownBy(() -> OpenAssert.validIndex(array, 5, "Invalid index"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("validIndex CharSequence")
        void testValidIndexCharSequence() {
            String str = "abc";
            CharSequence result = OpenAssert.validIndex(str, 1, "Invalid index");
            assertThat(result).isEqualTo(str);

            assertThatThrownBy(() -> OpenAssert.validIndex(str, 5, "Invalid index"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("validIndex Collection")
        void testValidIndexCollection() {
            List<String> list = List.of("a", "b", "c");
            Collection<String> result = OpenAssert.validIndex(list, 1, "Invalid index");
            assertThat(result).isSameAs(list);

            assertThatThrownBy(() -> OpenAssert.validIndex(list, 5, "Invalid index"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("isInstanceOf 测试")
    class IsInstanceOfTests {

        @Test
        @DisplayName("isInstanceOf 正确类型通过")
        void testIsInstanceOfPasses() {
            assertThatNoException().isThrownBy(() ->
                    OpenAssert.isInstanceOf(String.class, "test", "Type mismatch"));
        }

        @Test
        @DisplayName("isInstanceOf 错误类型抛异常")
        void testIsInstanceOfThrows() {
            assertThatThrownBy(() ->
                    OpenAssert.isInstanceOf(Integer.class, "test", "Expected Integer"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("Expected Integer");
        }

        @Test
        @DisplayName("isInstanceOf null 类型抛异常")
        void testIsInstanceOfNullTypeThrows() {
            assertThatThrownBy(() ->
                    OpenAssert.isInstanceOf(null, "test", "Type is null"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("isAssignableFrom 测试")
    class IsAssignableFromTests {

        @Test
        @DisplayName("isAssignableFrom 可赋值通过")
        void testIsAssignableFromPasses() {
            assertThatNoException().isThrownBy(() ->
                    OpenAssert.isAssignableFrom(Number.class, Integer.class, "Not assignable"));
        }

        @Test
        @DisplayName("isAssignableFrom 不可赋值抛异常")
        void testIsAssignableFromThrows() {
            assertThatThrownBy(() ->
                    OpenAssert.isAssignableFrom(Integer.class, String.class, "Not assignable"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("isAssignableFrom null 子类型抛异常")
        void testIsAssignableFromNullSubTypeThrows() {
            assertThatThrownBy(() ->
                    OpenAssert.isAssignableFrom(Number.class, null, "SubType is null"))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }
    }
}
