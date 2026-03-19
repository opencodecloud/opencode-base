package cloud.opencode.base.string.desensitize.strategy;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DesensitizeTypeTest Tests
 * DesensitizeTypeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("DesensitizeType Tests")
class DesensitizeTypeTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("Should have all expected values")
        void shouldHaveAllExpectedValues() {
            assertThat(DesensitizeType.values()).containsExactly(
                DesensitizeType.MOBILE_PHONE,
                DesensitizeType.ID_CARD,
                DesensitizeType.EMAIL,
                DesensitizeType.BANK_CARD,
                DesensitizeType.CHINESE_NAME,
                DesensitizeType.ADDRESS,
                DesensitizeType.PASSWORD,
                DesensitizeType.CUSTOM
            );
        }

        @Test
        @DisplayName("Should return value from name")
        void shouldReturnValueFromName() {
            assertThat(DesensitizeType.valueOf("MOBILE_PHONE")).isEqualTo(DesensitizeType.MOBILE_PHONE);
            assertThat(DesensitizeType.valueOf("ID_CARD")).isEqualTo(DesensitizeType.ID_CARD);
            assertThat(DesensitizeType.valueOf("EMAIL")).isEqualTo(DesensitizeType.EMAIL);
            assertThat(DesensitizeType.valueOf("BANK_CARD")).isEqualTo(DesensitizeType.BANK_CARD);
            assertThat(DesensitizeType.valueOf("CHINESE_NAME")).isEqualTo(DesensitizeType.CHINESE_NAME);
            assertThat(DesensitizeType.valueOf("ADDRESS")).isEqualTo(DesensitizeType.ADDRESS);
            assertThat(DesensitizeType.valueOf("PASSWORD")).isEqualTo(DesensitizeType.PASSWORD);
            assertThat(DesensitizeType.valueOf("CUSTOM")).isEqualTo(DesensitizeType.CUSTOM);
        }
    }
}
