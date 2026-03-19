package cloud.opencode.base.lock;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * LockType test - 锁类型枚举测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class LockTypeTest {

    @Nested
    @DisplayName("Enum Values | 枚举值")
    class EnumValuesTests {

        @Test
        @DisplayName("should have REENTRANT value")
        void shouldHaveReentrantValue() {
            assertThat(LockType.REENTRANT).isNotNull();
            assertThat(LockType.REENTRANT.name()).isEqualTo("REENTRANT");
        }

        @Test
        @DisplayName("should have READ_WRITE value")
        void shouldHaveReadWriteValue() {
            assertThat(LockType.READ_WRITE).isNotNull();
            assertThat(LockType.READ_WRITE.name()).isEqualTo("READ_WRITE");
        }

        @Test
        @DisplayName("should have STAMPED value")
        void shouldHaveStampedValue() {
            assertThat(LockType.STAMPED).isNotNull();
            assertThat(LockType.STAMPED.name()).isEqualTo("STAMPED");
        }

        @Test
        @DisplayName("should have SPIN value")
        void shouldHaveSpinValue() {
            assertThat(LockType.SPIN).isNotNull();
            assertThat(LockType.SPIN.name()).isEqualTo("SPIN");
        }

        @Test
        @DisplayName("should have SEGMENT value")
        void shouldHaveSegmentValue() {
            assertThat(LockType.SEGMENT).isNotNull();
            assertThat(LockType.SEGMENT.name()).isEqualTo("SEGMENT");
        }

        @Test
        @DisplayName("should have exactly 5 values")
        void shouldHaveExactlyFiveValues() {
            assertThat(LockType.values()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("Enum Methods | 枚举方法")
    class EnumMethodsTests {

        @Test
        @DisplayName("values() should return all values")
        void values_shouldReturnAllValues() {
            LockType[] values = LockType.values();

            assertThat(values).containsExactly(
                    LockType.REENTRANT,
                    LockType.READ_WRITE,
                    LockType.STAMPED,
                    LockType.SPIN,
                    LockType.SEGMENT
            );
        }

        @Test
        @DisplayName("valueOf() should return correct value")
        void valueOf_shouldReturnCorrectValue() {
            assertThat(LockType.valueOf("REENTRANT")).isEqualTo(LockType.REENTRANT);
            assertThat(LockType.valueOf("READ_WRITE")).isEqualTo(LockType.READ_WRITE);
            assertThat(LockType.valueOf("STAMPED")).isEqualTo(LockType.STAMPED);
            assertThat(LockType.valueOf("SPIN")).isEqualTo(LockType.SPIN);
            assertThat(LockType.valueOf("SEGMENT")).isEqualTo(LockType.SEGMENT);
        }

        @Test
        @DisplayName("valueOf() should throw for invalid value")
        void valueOf_shouldThrowForInvalidValue() {
            assertThatThrownBy(() -> LockType.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("ordinal() should return correct index")
        void ordinal_shouldReturnCorrectIndex() {
            assertThat(LockType.REENTRANT.ordinal()).isEqualTo(0);
            assertThat(LockType.READ_WRITE.ordinal()).isEqualTo(1);
            assertThat(LockType.STAMPED.ordinal()).isEqualTo(2);
            assertThat(LockType.SPIN.ordinal()).isEqualTo(3);
            assertThat(LockType.SEGMENT.ordinal()).isEqualTo(4);
        }

        @Test
        @DisplayName("name() should return name string")
        void name_shouldReturnNameString() {
            assertThat(LockType.REENTRANT.name()).isEqualTo("REENTRANT");
            assertThat(LockType.SPIN.name()).isEqualTo("SPIN");
        }

        @Test
        @DisplayName("toString() should return name")
        void toString_shouldReturnName() {
            assertThat(LockType.REENTRANT.toString()).isEqualTo("REENTRANT");
        }
    }

    @Nested
    @DisplayName("Comparison Tests | 比较测试")
    class ComparisonTests {

        @Test
        @DisplayName("compareTo() should work correctly")
        void compareTo_shouldWorkCorrectly() {
            assertThat(LockType.REENTRANT.compareTo(LockType.READ_WRITE)).isLessThan(0);
            assertThat(LockType.SPIN.compareTo(LockType.REENTRANT)).isGreaterThan(0);
            assertThat(LockType.STAMPED.compareTo(LockType.STAMPED)).isEqualTo(0);
        }

        @Test
        @DisplayName("equals() should work correctly")
        void equals_shouldWorkCorrectly() {
            assertThat(LockType.REENTRANT).isEqualTo(LockType.REENTRANT);
            assertThat(LockType.REENTRANT).isNotEqualTo(LockType.SPIN);
        }
    }
}
