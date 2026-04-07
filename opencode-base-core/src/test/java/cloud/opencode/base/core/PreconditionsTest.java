package cloud.opencode.base.core;

import cloud.opencode.base.core.exception.OpenIllegalArgumentException;
import cloud.opencode.base.core.exception.OpenIllegalStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Preconditions 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Preconditions 测试")
class PreconditionsTest {

    @Nested
    @DisplayName("checkNotNull 测试")
    class CheckNotNullTests {

        @Test
        @DisplayName("非 null 值通过")
        void testCheckNotNullPass() {
            String result = Preconditions.checkNotNull("hello");
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("null 值抛出 NullPointerException")
        void testCheckNotNullFail() {
            assertThatThrownBy(() -> Preconditions.checkNotNull(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("带消息的 checkNotNull")
        void testCheckNotNullWithMessage() {
            assertThatThrownBy(() -> Preconditions.checkNotNull(null, "value cannot be null"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("value cannot be null");
        }

        @Test
        @DisplayName("带格式化消息的 checkNotNull")
        void testCheckNotNullWithFormattedMessage() {
            assertThatThrownBy(() -> Preconditions.checkNotNull(null, "Parameter %s cannot be null", "name"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Parameter name cannot be null");
        }
    }

    @Nested
    @DisplayName("checkArgument 测试")
    class CheckArgumentTests {

        @Test
        @DisplayName("条件为 true 通过")
        void testCheckArgumentPass() {
            assertThatNoException().isThrownBy(() -> Preconditions.checkArgument(true));
        }

        @Test
        @DisplayName("条件为 false 抛出异常")
        void testCheckArgumentFail() {
            assertThatThrownBy(() -> Preconditions.checkArgument(false))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("带消息的 checkArgument")
        void testCheckArgumentWithMessage() {
            assertThatThrownBy(() -> Preconditions.checkArgument(false, "Age must be positive"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("Age must be positive");
        }

        @Test
        @DisplayName("带格式化消息的 checkArgument")
        void testCheckArgumentWithFormattedMessage() {
            int age = -5;
            assertThatThrownBy(() -> Preconditions.checkArgument(age > 0, "Age must be positive, got: %s", age))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("Age must be positive, got: -5");
        }
    }

    @Nested
    @DisplayName("checkState 测试")
    class CheckStateTests {

        @Test
        @DisplayName("状态为 true 通过")
        void testCheckStatePass() {
            assertThatNoException().isThrownBy(() -> Preconditions.checkState(true));
        }

        @Test
        @DisplayName("状态为 false 抛出异常")
        void testCheckStateFail() {
            assertThatThrownBy(() -> Preconditions.checkState(false))
                    .isInstanceOf(OpenIllegalStateException.class);
        }

        @Test
        @DisplayName("带消息的 checkState")
        void testCheckStateWithMessage() {
            assertThatThrownBy(() -> Preconditions.checkState(false, "Service not initialized"))
                    .isInstanceOf(OpenIllegalStateException.class)
                    .hasMessageContaining("Service not initialized");
        }

        @Test
        @DisplayName("带格式化消息的 checkState")
        void testCheckStateWithFormattedMessage() {
            String service = "UserService";
            assertThatThrownBy(() -> Preconditions.checkState(false, "%s not initialized", service))
                    .isInstanceOf(OpenIllegalStateException.class)
                    .hasMessageContaining("UserService not initialized");
        }
    }

    @Nested
    @DisplayName("checkElementIndex 测试")
    class CheckElementIndexTests {

        @Test
        @DisplayName("有效索引通过")
        void testCheckElementIndexPass() {
            int result = Preconditions.checkElementIndex(2, 5);
            assertThat(result).isEqualTo(2);
        }

        @Test
        @DisplayName("索引等于 size 失败")
        void testCheckElementIndexEqualSize() {
            assertThatThrownBy(() -> Preconditions.checkElementIndex(5, 5))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("负数索引失败")
        void testCheckElementIndexNegative() {
            assertThatThrownBy(() -> Preconditions.checkElementIndex(-1, 5))
                    .isInstanceOf(IndexOutOfBoundsException.class)
                    .hasMessageContaining("must not be negative");
        }

        @Test
        @DisplayName("带描述的 checkElementIndex")
        void testCheckElementIndexWithDesc() {
            assertThatThrownBy(() -> Preconditions.checkElementIndex(10, 5, "arrayIndex"))
                    .isInstanceOf(IndexOutOfBoundsException.class)
                    .hasMessageContaining("arrayIndex");
        }

        @Test
        @DisplayName("负数 size 失败")
        void testCheckElementIndexNegativeSize() {
            assertThatThrownBy(() -> Preconditions.checkElementIndex(0, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negative size");
        }
    }

    @Nested
    @DisplayName("checkPositionIndex 测试")
    class CheckPositionIndexTests {

        @Test
        @DisplayName("有效位置索引通过")
        void testCheckPositionIndexPass() {
            int result = Preconditions.checkPositionIndex(5, 5);
            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("索引大于 size 失败")
        void testCheckPositionIndexGreaterThanSize() {
            assertThatThrownBy(() -> Preconditions.checkPositionIndex(6, 5))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("负数索引失败")
        void testCheckPositionIndexNegative() {
            assertThatThrownBy(() -> Preconditions.checkPositionIndex(-1, 5))
                    .isInstanceOf(IndexOutOfBoundsException.class)
                    .hasMessageContaining("must not be negative");
        }

        @Test
        @DisplayName("带描述的 checkPositionIndex")
        void testCheckPositionIndexWithDesc() {
            assertThatThrownBy(() -> Preconditions.checkPositionIndex(10, 5, "insertIndex"))
                    .isInstanceOf(IndexOutOfBoundsException.class)
                    .hasMessageContaining("insertIndex");
        }
    }

    @Nested
    @DisplayName("checkPositionIndexes 测试")
    class CheckPositionIndexesTests {

        @Test
        @DisplayName("有效范围通过")
        void testCheckPositionIndexesPass() {
            assertThatNoException().isThrownBy(() -> Preconditions.checkPositionIndexes(0, 5, 10));
        }

        @Test
        @DisplayName("start 大于 end 失败")
        void testCheckPositionIndexesStartGreaterThanEnd() {
            assertThatThrownBy(() -> Preconditions.checkPositionIndexes(5, 3, 10))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("end 大于 size 失败")
        void testCheckPositionIndexesEndGreaterThanSize() {
            assertThatThrownBy(() -> Preconditions.checkPositionIndexes(0, 15, 10))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("负数 start 失败")
        void testCheckPositionIndexesNegativeStart() {
            assertThatThrownBy(() -> Preconditions.checkPositionIndexes(-1, 5, 10))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("checkPositive(int) 测试")
    class CheckPositiveIntTests {

        @Test
        @DisplayName("正数通过并返回值")
        void testPositiveValue() {
            assertThat(Preconditions.checkPositive(5, "count")).isEqualTo(5);
        }

        @Test
        @DisplayName("零值抛出异常")
        void testZeroValue() {
            assertThatThrownBy(() -> Preconditions.checkPositive(0, "count"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("count")
                    .hasMessageContaining("must be positive")
                    .hasMessageContaining("0");
        }

        @Test
        @DisplayName("负数抛出异常")
        void testNegativeValue() {
            assertThatThrownBy(() -> Preconditions.checkPositive(-3, "count"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must be positive")
                    .hasMessageContaining("-3");
        }
    }

    @Nested
    @DisplayName("checkPositive(long) 测试")
    class CheckPositiveLongTests {

        @Test
        @DisplayName("正数通过并返回值")
        void testPositiveValue() {
            assertThat(Preconditions.checkPositive(100L, "size")).isEqualTo(100L);
        }

        @Test
        @DisplayName("零值抛出异常")
        void testZeroValue() {
            assertThatThrownBy(() -> Preconditions.checkPositive(0L, "size"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must be positive");
        }

        @Test
        @DisplayName("负数抛出异常")
        void testNegativeValue() {
            assertThatThrownBy(() -> Preconditions.checkPositive(-1L, "size"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must be positive");
        }
    }

    @Nested
    @DisplayName("checkNonNegative(int) 测试")
    class CheckNonNegativeIntTests {

        @Test
        @DisplayName("正数通过")
        void testPositiveValue() {
            assertThat(Preconditions.checkNonNegative(10, "offset")).isEqualTo(10);
        }

        @Test
        @DisplayName("零通过")
        void testZeroValue() {
            assertThat(Preconditions.checkNonNegative(0, "offset")).isEqualTo(0);
        }

        @Test
        @DisplayName("负数抛出异常")
        void testNegativeValue() {
            assertThatThrownBy(() -> Preconditions.checkNonNegative(-1, "offset"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must be non-negative")
                    .hasMessageContaining("-1");
        }
    }

    @Nested
    @DisplayName("checkNonNegative(long) 测试")
    class CheckNonNegativeLongTests {

        @Test
        @DisplayName("正数通过")
        void testPositiveValue() {
            assertThat(Preconditions.checkNonNegative(10L, "offset")).isEqualTo(10L);
        }

        @Test
        @DisplayName("零通过")
        void testZeroValue() {
            assertThat(Preconditions.checkNonNegative(0L, "offset")).isEqualTo(0L);
        }

        @Test
        @DisplayName("负数抛出异常")
        void testNegativeValue() {
            assertThatThrownBy(() -> Preconditions.checkNonNegative(-5L, "offset"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must be non-negative");
        }
    }

    @Nested
    @DisplayName("checkBetween(int) 测试")
    class CheckBetweenIntTests {

        @Test
        @DisplayName("范围内值通过")
        void testInRange() {
            assertThat(Preconditions.checkBetween(5, 1, 10, "port")).isEqualTo(5);
        }

        @Test
        @DisplayName("最小边界通过")
        void testAtMin() {
            assertThat(Preconditions.checkBetween(1, 1, 10, "port")).isEqualTo(1);
        }

        @Test
        @DisplayName("最大边界通过")
        void testAtMax() {
            assertThat(Preconditions.checkBetween(10, 1, 10, "port")).isEqualTo(10);
        }

        @Test
        @DisplayName("低于最小值抛出异常")
        void testBelowMin() {
            assertThatThrownBy(() -> Preconditions.checkBetween(0, 1, 10, "port"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must be between 1 and 10")
                    .hasMessageContaining("0");
        }

        @Test
        @DisplayName("高于最大值抛出异常")
        void testAboveMax() {
            assertThatThrownBy(() -> Preconditions.checkBetween(11, 1, 10, "port"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must be between 1 and 10")
                    .hasMessageContaining("11");
        }
    }

    @Nested
    @DisplayName("checkBetween(long) 测试")
    class CheckBetweenLongTests {

        @Test
        @DisplayName("范围内值通过")
        void testInRange() {
            assertThat(Preconditions.checkBetween(50L, 0L, 100L, "percent")).isEqualTo(50L);
        }

        @Test
        @DisplayName("低于最小值抛出异常")
        void testBelowMin() {
            assertThatThrownBy(() -> Preconditions.checkBetween(-1L, 0L, 100L, "percent"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must be between");
        }

        @Test
        @DisplayName("高于最大值抛出异常")
        void testAboveMax() {
            assertThatThrownBy(() -> Preconditions.checkBetween(101L, 0L, 100L, "percent"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must be between");
        }
    }

    @Nested
    @DisplayName("checkNotBlank 测试")
    class CheckNotBlankTests {

        @Test
        @DisplayName("非空白字符串通过并返回")
        void testValidString() {
            assertThat(Preconditions.checkNotBlank("hello", "name")).isEqualTo("hello");
        }

        @Test
        @DisplayName("null 抛出异常")
        void testNullString() {
            assertThatThrownBy(() -> Preconditions.checkNotBlank(null, "name"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must not be blank");
        }

        @Test
        @DisplayName("空字符串抛出异常")
        void testEmptyString() {
            assertThatThrownBy(() -> Preconditions.checkNotBlank("", "name"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must not be blank");
        }

        @Test
        @DisplayName("纯空格字符串抛出异常")
        void testBlankString() {
            assertThatThrownBy(() -> Preconditions.checkNotBlank("   ", "name"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must not be blank");
        }
    }

    @Nested
    @DisplayName("checkNotEmpty(Collection) 测试")
    class CheckNotEmptyCollectionTests {

        @Test
        @DisplayName("非空集合通过并返回")
        void testValidCollection() {
            List<String> list = List.of("a", "b");
            assertThat(Preconditions.checkNotEmpty(list, "items")).isSameAs(list);
        }

        @Test
        @DisplayName("null 集合抛出异常")
        void testNullCollection() {
            assertThatThrownBy(() -> Preconditions.checkNotEmpty((List<?>) null, "items"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must not be empty");
        }

        @Test
        @DisplayName("空集合抛出异常")
        void testEmptyCollection() {
            assertThatThrownBy(() -> Preconditions.checkNotEmpty(Collections.emptyList(), "items"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must not be empty");
        }
    }

    @Nested
    @DisplayName("checkNotEmpty(Map) 测试")
    class CheckNotEmptyMapTests {

        @Test
        @DisplayName("非空 Map 通过并返回")
        void testValidMap() {
            Map<String, String> map = Map.of("k", "v");
            assertThat(Preconditions.checkNotEmpty(map, "config")).isSameAs(map);
        }

        @Test
        @DisplayName("null Map 抛出异常")
        void testNullMap() {
            assertThatThrownBy(() -> Preconditions.checkNotEmpty((Map<?, ?>) null, "config"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must not be empty");
        }

        @Test
        @DisplayName("空 Map 抛出异常")
        void testEmptyMap() {
            assertThatThrownBy(() -> Preconditions.checkNotEmpty(Collections.emptyMap(), "config"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must not be empty");
        }
    }
}
