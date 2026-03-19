package cloud.opencode.base.crypto.symmetric;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Padding 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Padding 测试")
class PaddingTest {

    @Test
    @DisplayName("枚举值存在")
    void testEnumValues() {
        assertThat(Padding.values()).containsExactly(
                Padding.NO_PADDING,
                Padding.PKCS5,
                Padding.PKCS7,
                Padding.ISO10126
        );
    }

    @Test
    @DisplayName("getValue返回正确JCE名称")
    void testGetValue() {
        assertThat(Padding.NO_PADDING.getValue()).isEqualTo("NoPadding");
        assertThat(Padding.PKCS5.getValue()).isEqualTo("PKCS5Padding");
        assertThat(Padding.PKCS7.getValue()).isEqualTo("PKCS5Padding"); // JCE uses PKCS5Padding for PKCS7
        assertThat(Padding.ISO10126.getValue()).isEqualTo("ISO10126Padding");
    }

    @Test
    @DisplayName("valueOf转换")
    void testValueOf() {
        assertThat(Padding.valueOf("NO_PADDING")).isEqualTo(Padding.NO_PADDING);
        assertThat(Padding.valueOf("PKCS5")).isEqualTo(Padding.PKCS5);
        assertThat(Padding.valueOf("PKCS7")).isEqualTo(Padding.PKCS7);
        assertThat(Padding.valueOf("ISO10126")).isEqualTo(Padding.ISO10126);
    }

    @Test
    @DisplayName("无效值抛出异常")
    void testInvalidValueOf() {
        assertThatThrownBy(() -> Padding.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("PKCS5和PKCS7使用相同JCE值")
    void testPkcs5AndPkcs7SameValue() {
        // JCE treats PKCS5Padding and PKCS7 padding the same for AES
        assertThat(Padding.PKCS5.getValue()).isEqualTo(Padding.PKCS7.getValue());
    }
}
