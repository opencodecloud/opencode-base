package cloud.opencode.base.crypto.mac;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Poly1305 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Poly1305 测试")
@EnabledIf("isBouncyCastleAvailable")
class Poly1305Test {

    // Poly1305 requires a 32-byte key
    private static final byte[] TEST_KEY = new byte[32];
    private static final byte[] TEST_DATA = "Hello, World!".getBytes(StandardCharsets.UTF_8);

    static {
        // Fill test key with recognizable pattern
        for (int i = 0; i < 32; i++) {
            TEST_KEY[i] = (byte) (i + 1);
        }
    }

    /**
     * Check if Bouncy Castle is available
     */
    static boolean isBouncyCastleAvailable() {
        try {
            Class.forName("org.bouncycastle.crypto.macs.Poly1305");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("使用32字节密钥创建")
        void testOfValidKey() {
            Poly1305 mac = Poly1305.of(TEST_KEY);
            assertThat(mac).isNotNull();
            assertThat(mac.getAlgorithm()).isEqualTo("Poly1305");
        }

        @Test
        @DisplayName("null密钥抛出异常")
        void testOfNullKey() {
            assertThatThrownBy(() -> Poly1305.of(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("无效长度密钥抛出异常")
        void testOfInvalidKeyLength() {
            assertThatThrownBy(() -> Poly1305.of(new byte[16]))
                    .isInstanceOf(OpenCryptoException.class);
        }
    }

    @Nested
    @DisplayName("compute测试")
    class ComputeTests {

        @Test
        @DisplayName("计算字节数组MAC")
        void testComputeBytes() {
            Poly1305 mac = Poly1305.of(TEST_KEY);
            byte[] result = mac.compute(TEST_DATA);

            assertThat(result).hasSize(16); // Poly1305 produces 16 bytes
        }

        @Test
        @DisplayName("计算字符串MAC")
        void testComputeString() {
            Poly1305 mac = Poly1305.of(TEST_KEY);
            byte[] result = mac.compute("Hello, World!");

            assertThat(result).hasSize(16);
        }

        @Test
        @DisplayName("相同输入相同输出")
        void testComputeDeterministic() {
            Poly1305 mac = Poly1305.of(TEST_KEY);

            byte[] result1 = mac.compute(TEST_DATA);
            byte[] result2 = mac.compute(TEST_DATA);

            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("不同输入不同输出")
        void testComputeDifferentInputs() {
            Poly1305 mac = Poly1305.of(TEST_KEY);

            byte[] result1 = mac.compute("Hello".getBytes(StandardCharsets.UTF_8));
            byte[] result2 = mac.compute("World".getBytes(StandardCharsets.UTF_8));

            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("compute null字节数组抛出异常")
        void testComputeNullBytes() {
            Poly1305 mac = Poly1305.of(TEST_KEY);

            assertThatThrownBy(() -> mac.compute((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("compute null字符串抛出异常")
        void testComputeNullString() {
            Poly1305 mac = Poly1305.of(TEST_KEY);

            assertThatThrownBy(() -> mac.compute((String) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("computeHex和computeBase64测试")
    class ComputeEncodedTests {

        @Test
        @DisplayName("computeHex返回十六进制")
        void testComputeHex() {
            Poly1305 mac = Poly1305.of(TEST_KEY);
            String result = mac.computeHex(TEST_DATA);

            assertThat(result).hasSize(32); // 16 bytes = 32 hex chars
            assertThat(result).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("computeBase64返回Base64")
        void testComputeBase64() {
            Poly1305 mac = Poly1305.of(TEST_KEY);
            String result = mac.computeBase64(TEST_DATA);

            assertThat(result).matches("[A-Za-z0-9+/=]+");
        }
    }

    @Nested
    @DisplayName("verify测试")
    class VerifyTests {

        @Test
        @DisplayName("验证正确MAC")
        void testVerifyCorrect() {
            Poly1305 mac = Poly1305.of(TEST_KEY);
            byte[] computedMac = mac.compute(TEST_DATA);

            assertThat(mac.verify(TEST_DATA, computedMac)).isTrue();
        }

        @Test
        @DisplayName("验证错误MAC")
        void testVerifyIncorrect() {
            Poly1305 mac = Poly1305.of(TEST_KEY);
            byte[] wrongMac = new byte[16];

            assertThat(mac.verify(TEST_DATA, wrongMac)).isFalse();
        }

        @Test
        @DisplayName("verify null数据抛出异常")
        void testVerifyNullData() {
            Poly1305 mac = Poly1305.of(TEST_KEY);

            assertThatThrownBy(() -> mac.verify(null, new byte[16]))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify null MAC抛出异常")
        void testVerifyNullMac() {
            Poly1305 mac = Poly1305.of(TEST_KEY);

            assertThatThrownBy(() -> mac.verify(TEST_DATA, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("verifyHex和verifyBase64测试")
    class VerifyEncodedTests {

        @Test
        @DisplayName("verifyHex验证正确")
        void testVerifyHexCorrect() {
            Poly1305 mac = Poly1305.of(TEST_KEY);
            String hexMac = mac.computeHex(TEST_DATA);

            assertThat(mac.verifyHex(TEST_DATA, hexMac)).isTrue();
        }

        @Test
        @DisplayName("verifyHex验证错误")
        void testVerifyHexIncorrect() {
            Poly1305 mac = Poly1305.of(TEST_KEY);
            String wrongHex = "00".repeat(16);

            assertThat(mac.verifyHex(TEST_DATA, wrongHex)).isFalse();
        }

        @Test
        @DisplayName("verifyHex无效格式返回false")
        void testVerifyHexInvalid() {
            Poly1305 mac = Poly1305.of(TEST_KEY);

            assertThat(mac.verifyHex(TEST_DATA, "invalid-hex")).isFalse();
        }

        @Test
        @DisplayName("verifyHex null抛出异常")
        void testVerifyHexNull() {
            Poly1305 mac = Poly1305.of(TEST_KEY);

            assertThatThrownBy(() -> mac.verifyHex(TEST_DATA, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyBase64验证正确")
        void testVerifyBase64Correct() {
            Poly1305 mac = Poly1305.of(TEST_KEY);
            String base64Mac = mac.computeBase64(TEST_DATA);

            assertThat(mac.verifyBase64(TEST_DATA, base64Mac)).isTrue();
        }

        @Test
        @DisplayName("verifyBase64无效格式返回false")
        void testVerifyBase64Invalid() {
            Poly1305 mac = Poly1305.of(TEST_KEY);

            assertThat(mac.verifyBase64(TEST_DATA, "!!!invalid!!!")).isFalse();
        }

        @Test
        @DisplayName("verifyBase64 null抛出异常")
        void testVerifyBase64Null() {
            Poly1305 mac = Poly1305.of(TEST_KEY);

            assertThatThrownBy(() -> mac.verifyBase64(TEST_DATA, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("增量计算测试")
    class IncrementalTests {

        @Test
        @DisplayName("update null字节数组抛出异常")
        void testUpdateNullBytes() {
            Poly1305 mac = Poly1305.of(TEST_KEY);

            assertThatThrownBy(() -> mac.update((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("update null字符串抛出异常")
        void testUpdateNullString() {
            Poly1305 mac = Poly1305.of(TEST_KEY);

            assertThatThrownBy(() -> mac.update((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("doFinalHex")
        void testDoFinalHex() {
            Poly1305 mac = Poly1305.of(TEST_KEY);
            mac.update(TEST_DATA);
            String result = mac.doFinalHex();

            assertThat(result).hasSize(32);
            assertThat(result).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("doFinalBase64")
        void testDoFinalBase64() {
            Poly1305 mac = Poly1305.of(TEST_KEY);
            mac.update(TEST_DATA);
            String result = mac.doFinalBase64();

            assertThat(result).matches("[A-Za-z0-9+/=]+");
        }
    }

    @Nested
    @DisplayName("reset测试")
    class ResetTests {

        @Test
        @DisplayName("reset返回自身")
        void testResetReturnsSelf() {
            Poly1305 mac = Poly1305.of(TEST_KEY);

            assertThat(mac.reset()).isSameAs(mac);
        }
    }

    @Nested
    @DisplayName("属性测试")
    class PropertyTests {

        @Test
        @DisplayName("getAlgorithm")
        void testGetAlgorithm() {
            Poly1305 mac = Poly1305.of(TEST_KEY);
            assertThat(mac.getAlgorithm()).isEqualTo("Poly1305");
        }

        @Test
        @DisplayName("getMacLength")
        void testGetMacLength() {
            Poly1305 mac = Poly1305.of(TEST_KEY);
            assertThat(mac.getMacLength()).isEqualTo(16);
        }

        @Test
        @DisplayName("实现Mac接口")
        void testImplementsMacInterface() {
            Poly1305 mac = Poly1305.of(TEST_KEY);
            assertThat(mac).isInstanceOf(Mac.class);
        }
    }
}
