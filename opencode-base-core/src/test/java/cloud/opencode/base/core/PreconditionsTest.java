package cloud.opencode.base.core;

import cloud.opencode.base.core.exception.OpenIllegalArgumentException;
import cloud.opencode.base.core.exception.OpenIllegalStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("负数索引失败")
        void testCheckElementIndexNegative() {
            assertThatThrownBy(() -> Preconditions.checkElementIndex(-1, 5))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must not be negative");
        }

        @Test
        @DisplayName("带描述的 checkElementIndex")
        void testCheckElementIndexWithDesc() {
            assertThatThrownBy(() -> Preconditions.checkElementIndex(10, 5, "arrayIndex"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("arrayIndex");
        }

        @Test
        @DisplayName("负数 size 失败")
        void testCheckElementIndexNegativeSize() {
            assertThatThrownBy(() -> Preconditions.checkElementIndex(0, -1))
                    .isInstanceOf(OpenIllegalArgumentException.class)
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
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("负数索引失败")
        void testCheckPositionIndexNegative() {
            assertThatThrownBy(() -> Preconditions.checkPositionIndex(-1, 5))
                    .isInstanceOf(OpenIllegalArgumentException.class)
                    .hasMessageContaining("must not be negative");
        }

        @Test
        @DisplayName("带描述的 checkPositionIndex")
        void testCheckPositionIndexWithDesc() {
            assertThatThrownBy(() -> Preconditions.checkPositionIndex(10, 5, "insertIndex"))
                    .isInstanceOf(OpenIllegalArgumentException.class)
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
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("end 大于 size 失败")
        void testCheckPositionIndexesEndGreaterThanSize() {
            assertThatThrownBy(() -> Preconditions.checkPositionIndexes(0, 15, 10))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }

        @Test
        @DisplayName("负数 start 失败")
        void testCheckPositionIndexesNegativeStart() {
            assertThatThrownBy(() -> Preconditions.checkPositionIndexes(-1, 5, 10))
                    .isInstanceOf(OpenIllegalArgumentException.class);
        }
    }
}
