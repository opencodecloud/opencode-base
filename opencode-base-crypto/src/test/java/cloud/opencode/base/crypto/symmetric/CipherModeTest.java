package cloud.opencode.base.crypto.symmetric;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CipherMode 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("CipherMode 测试")
class CipherModeTest {

    @Test
    @DisplayName("枚举值存在")
    void testEnumValues() {
        assertThat(CipherMode.values()).containsExactly(
                CipherMode.ECB,
                CipherMode.CBC,
                CipherMode.CTR,
                CipherMode.GCM,
                CipherMode.CCM
        );
    }

    @Test
    @DisplayName("valueOf转换")
    void testValueOf() {
        assertThat(CipherMode.valueOf("ECB")).isEqualTo(CipherMode.ECB);
        assertThat(CipherMode.valueOf("CBC")).isEqualTo(CipherMode.CBC);
        assertThat(CipherMode.valueOf("CTR")).isEqualTo(CipherMode.CTR);
        assertThat(CipherMode.valueOf("GCM")).isEqualTo(CipherMode.GCM);
        assertThat(CipherMode.valueOf("CCM")).isEqualTo(CipherMode.CCM);
    }

    @Test
    @DisplayName("无效值抛出异常")
    void testInvalidValueOf() {
        assertThatThrownBy(() -> CipherMode.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("name方法返回正确名称")
    void testName() {
        assertThat(CipherMode.ECB.name()).isEqualTo("ECB");
        assertThat(CipherMode.CBC.name()).isEqualTo("CBC");
        assertThat(CipherMode.CTR.name()).isEqualTo("CTR");
        assertThat(CipherMode.GCM.name()).isEqualTo("GCM");
        assertThat(CipherMode.CCM.name()).isEqualTo("CCM");
    }

    @Test
    @DisplayName("ordinal方法返回正确序号")
    void testOrdinal() {
        assertThat(CipherMode.ECB.ordinal()).isEqualTo(0);
        assertThat(CipherMode.CBC.ordinal()).isEqualTo(1);
        assertThat(CipherMode.CTR.ordinal()).isEqualTo(2);
        assertThat(CipherMode.GCM.ordinal()).isEqualTo(3);
        assertThat(CipherMode.CCM.ordinal()).isEqualTo(4);
    }
}
