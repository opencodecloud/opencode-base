package cloud.opencode.base.deepclone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ClonePolicy 枚举测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("ClonePolicy 测试")
class ClonePolicyTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("应包含3个枚举值")
        void shouldHaveThreeValues() {
            ClonePolicy[] values = ClonePolicy.values();

            assertThat(values).hasSize(3);
        }

        @Test
        @DisplayName("应包含STANDARD枚举值")
        void shouldContainStandard() {
            assertThat(ClonePolicy.STANDARD).isNotNull();
            assertThat(ClonePolicy.STANDARD.name()).isEqualTo("STANDARD");
        }

        @Test
        @DisplayName("应包含STRICT枚举值")
        void shouldContainStrict() {
            assertThat(ClonePolicy.STRICT).isNotNull();
            assertThat(ClonePolicy.STRICT.name()).isEqualTo("STRICT");
        }

        @Test
        @DisplayName("应包含LENIENT枚举值")
        void shouldContainLenient() {
            assertThat(ClonePolicy.LENIENT).isNotNull();
            assertThat(ClonePolicy.LENIENT.name()).isEqualTo("LENIENT");
        }

        @Test
        @DisplayName("values()应返回所有3个值")
        void valuesShouldReturnAllThree() {
            assertThat(ClonePolicy.values())
                    .containsExactly(ClonePolicy.STANDARD, ClonePolicy.STRICT, ClonePolicy.LENIENT);
        }
    }

    @Nested
    @DisplayName("valueOf() 测试")
    class ValueOfTests {

        @Test
        @DisplayName("valueOf应正确解析STANDARD")
        void valueOfStandard() {
            assertThat(ClonePolicy.valueOf("STANDARD")).isEqualTo(ClonePolicy.STANDARD);
        }

        @Test
        @DisplayName("valueOf应正确解析STRICT")
        void valueOfStrict() {
            assertThat(ClonePolicy.valueOf("STRICT")).isEqualTo(ClonePolicy.STRICT);
        }

        @Test
        @DisplayName("valueOf应正确解析LENIENT")
        void valueOfLenient() {
            assertThat(ClonePolicy.valueOf("LENIENT")).isEqualTo(ClonePolicy.LENIENT);
        }

        @Test
        @DisplayName("valueOf无效值应抛出IllegalArgumentException")
        void valueOfInvalidShouldThrow() {
            assertThatThrownBy(() -> ClonePolicy.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("ordinal() 测试")
    class OrdinalTests {

        @Test
        @DisplayName("枚举顺序应正确")
        void ordinalsShouldBeCorrect() {
            assertThat(ClonePolicy.STANDARD.ordinal()).isEqualTo(0);
            assertThat(ClonePolicy.STRICT.ordinal()).isEqualTo(1);
            assertThat(ClonePolicy.LENIENT.ordinal()).isEqualTo(2);
        }
    }
}
