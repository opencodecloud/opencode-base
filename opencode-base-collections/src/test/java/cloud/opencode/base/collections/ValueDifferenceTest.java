package cloud.opencode.base.collections;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ValueDifferenceTest Tests
 * ValueDifferenceTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ValueDifference 测试")
class ValueDifferenceTest {

    @Nested
    @DisplayName("create工厂方法测试")
    class CreateTests {

        @Test
        @DisplayName("创建包含左右值的ValueDifference")
        void testCreate() {
            ValueDifference<Integer> diff = ValueDifference.create(100, 200);

            assertThat(diff.leftValue()).isEqualTo(100);
            assertThat(diff.rightValue()).isEqualTo(200);
        }

        @Test
        @DisplayName("允许null值")
        void testCreateWithNulls() {
            ValueDifference<String> diff = ValueDifference.create(null, "right");

            assertThat(diff.leftValue()).isNull();
            assertThat(diff.rightValue()).isEqualTo("right");
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同值的ValueDifference相等")
        void testEquals() {
            ValueDifference<Integer> diff1 = ValueDifference.create(1, 2);
            ValueDifference<Integer> diff2 = ValueDifference.create(1, 2);

            assertThat(diff1).isEqualTo(diff2);
        }

        @Test
        @DisplayName("不同值的ValueDifference不相等")
        void testNotEquals() {
            ValueDifference<Integer> diff1 = ValueDifference.create(1, 2);
            ValueDifference<Integer> diff2 = ValueDifference.create(1, 3);

            assertThat(diff1).isNotEqualTo(diff2);
        }

        @Test
        @DisplayName("自反性")
        void testReflexive() {
            ValueDifference<Integer> diff = ValueDifference.create(1, 2);

            assertThat(diff).isEqualTo(diff);
        }
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相等的对象具有相同的hashCode")
        void testHashCodeConsistentWithEquals() {
            ValueDifference<Integer> diff1 = ValueDifference.create(1, 2);
            ValueDifference<Integer> diff2 = ValueDifference.create(1, 2);

            assertThat(diff1.hashCode()).isEqualTo(diff2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含左右值")
        void testToString() {
            ValueDifference<Integer> diff = ValueDifference.create(100, 200);

            String str = diff.toString();

            assertThat(str).contains("100");
            assertThat(str).contains("200");
        }
    }
}
