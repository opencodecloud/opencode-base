package cloud.opencode.base.string.desensitize.strategy;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * MaskPatternTest Tests
 * MaskPatternTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("MaskPattern Tests")
class MaskPatternTest {

    @Nested
    @DisplayName("Record Tests")
    class RecordTests {

        @Test
        @DisplayName("Should create custom pattern")
        void shouldCreateCustomPattern() {
            MaskPattern pattern = new MaskPattern(2, 3, '#');
            assertThat(pattern.startKeep()).isEqualTo(2);
            assertThat(pattern.endKeep()).isEqualTo(3);
            assertThat(pattern.maskChar()).isEqualTo('#');
        }
    }

    @Nested
    @DisplayName("Constant Tests")
    class ConstantTests {

        @Test
        @DisplayName("DEFAULT pattern should have correct values")
        void defaultPatternShouldHaveCorrectValues() {
            assertThat(MaskPattern.DEFAULT.startKeep()).isEqualTo(3);
            assertThat(MaskPattern.DEFAULT.endKeep()).isEqualTo(4);
            assertThat(MaskPattern.DEFAULT.maskChar()).isEqualTo('*');
        }

        @Test
        @DisplayName("MOBILE pattern should have correct values")
        void mobilePatternShouldHaveCorrectValues() {
            assertThat(MaskPattern.MOBILE.startKeep()).isEqualTo(3);
            assertThat(MaskPattern.MOBILE.endKeep()).isEqualTo(4);
            assertThat(MaskPattern.MOBILE.maskChar()).isEqualTo('*');
        }

        @Test
        @DisplayName("ID_CARD pattern should have correct values")
        void idCardPatternShouldHaveCorrectValues() {
            assertThat(MaskPattern.ID_CARD.startKeep()).isEqualTo(6);
            assertThat(MaskPattern.ID_CARD.endKeep()).isEqualTo(4);
            assertThat(MaskPattern.ID_CARD.maskChar()).isEqualTo('*');
        }

        @Test
        @DisplayName("EMAIL pattern should have correct values")
        void emailPatternShouldHaveCorrectValues() {
            assertThat(MaskPattern.EMAIL.startKeep()).isEqualTo(1);
            assertThat(MaskPattern.EMAIL.endKeep()).isEqualTo(0);
            assertThat(MaskPattern.EMAIL.maskChar()).isEqualTo('*');
        }

        @Test
        @DisplayName("BANK_CARD pattern should have correct values")
        void bankCardPatternShouldHaveCorrectValues() {
            assertThat(MaskPattern.BANK_CARD.startKeep()).isEqualTo(4);
            assertThat(MaskPattern.BANK_CARD.endKeep()).isEqualTo(4);
            assertThat(MaskPattern.BANK_CARD.maskChar()).isEqualTo('*');
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Same values should be equal")
        void sameValuesShouldBeEqual() {
            MaskPattern p1 = new MaskPattern(3, 4, '*');
            MaskPattern p2 = new MaskPattern(3, 4, '*');
            assertThat(p1).isEqualTo(p2);
        }

        @Test
        @DisplayName("Different values should not be equal")
        void differentValuesShouldNotBeEqual() {
            MaskPattern p1 = new MaskPattern(3, 4, '*');
            MaskPattern p2 = new MaskPattern(3, 4, '#');
            assertThat(p1).isNotEqualTo(p2);
        }
    }
}
