package cloud.opencode.base.crypto.mac;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * HmacSha512 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("HmacSha512 测试")
class HmacSha512Test {

    private static final byte[] TEST_KEY = "test-secret-key-for-hmac-512!!!".getBytes(StandardCharsets.UTF_8);
    private static final byte[] TEST_DATA = "Hello, World!".getBytes(StandardCharsets.UTF_8);

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("使用字节数组创建")
        void testOfByteArray() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);
            assertThat(mac).isNotNull();
            assertThat(mac.getAlgorithm()).isEqualTo("HmacSHA512");
        }

        @Test
        @DisplayName("使用SecretKey创建")
        void testOfSecretKey() {
            SecretKey key = new SecretKeySpec(TEST_KEY, "HmacSHA512");
            HmacSha512 mac = HmacSha512.of(key);
            assertThat(mac).isNotNull();
        }

        @Test
        @DisplayName("null字节数组抛出异常")
        void testOfNullByteArray() {
            assertThatThrownBy(() -> HmacSha512.of((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null SecretKey抛出异常")
        void testOfNullSecretKey() {
            assertThatThrownBy(() -> HmacSha512.of((SecretKey) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("compute测试")
    class ComputeTests {

        @Test
        @DisplayName("计算字节数组MAC")
        void testComputeBytes() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);
            byte[] result = mac.compute(TEST_DATA);

            assertThat(result).hasSize(64); // SHA-512 produces 64 bytes
        }

        @Test
        @DisplayName("计算字符串MAC")
        void testComputeString() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);
            byte[] result = mac.compute("Hello, World!");

            assertThat(result).hasSize(64);
        }

        @Test
        @DisplayName("相同输入相同输出")
        void testComputeDeterministic() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);

            byte[] result1 = mac.compute(TEST_DATA);
            byte[] result2 = mac.compute(TEST_DATA);

            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("不同输入不同输出")
        void testComputeDifferentInputs() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);

            byte[] result1 = mac.compute("Hello".getBytes(StandardCharsets.UTF_8));
            byte[] result2 = mac.compute("World".getBytes(StandardCharsets.UTF_8));

            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("compute null字节数组抛出异常")
        void testComputeNullBytes() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);

            assertThatThrownBy(() -> mac.compute((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("compute null字符串抛出异常")
        void testComputeNullString() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);

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
            HmacSha512 mac = HmacSha512.of(TEST_KEY);
            String result = mac.computeHex(TEST_DATA);

            assertThat(result).hasSize(128); // 64 bytes = 128 hex chars
            assertThat(result).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("computeBase64返回Base64")
        void testComputeBase64() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);
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
            HmacSha512 mac = HmacSha512.of(TEST_KEY);
            byte[] computedMac = mac.compute(TEST_DATA);

            assertThat(mac.verify(TEST_DATA, computedMac)).isTrue();
        }

        @Test
        @DisplayName("验证错误MAC")
        void testVerifyIncorrect() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);
            byte[] wrongMac = new byte[64];

            assertThat(mac.verify(TEST_DATA, wrongMac)).isFalse();
        }

        @Test
        @DisplayName("verify null数据抛出异常")
        void testVerifyNullData() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);

            assertThatThrownBy(() -> mac.verify(null, new byte[64]))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify null MAC抛出异常")
        void testVerifyNullMac() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);

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
            HmacSha512 mac = HmacSha512.of(TEST_KEY);
            String hexMac = mac.computeHex(TEST_DATA);

            assertThat(mac.verifyHex(TEST_DATA, hexMac)).isTrue();
        }

        @Test
        @DisplayName("verifyHex验证错误")
        void testVerifyHexIncorrect() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);
            String wrongHex = "00".repeat(64);

            assertThat(mac.verifyHex(TEST_DATA, wrongHex)).isFalse();
        }

        @Test
        @DisplayName("verifyHex无效格式返回false")
        void testVerifyHexInvalid() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);

            assertThat(mac.verifyHex(TEST_DATA, "invalid-hex")).isFalse();
        }

        @Test
        @DisplayName("verifyHex null抛出异常")
        void testVerifyHexNull() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);

            assertThatThrownBy(() -> mac.verifyHex(TEST_DATA, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyBase64验证正确")
        void testVerifyBase64Correct() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);
            String base64Mac = mac.computeBase64(TEST_DATA);

            assertThat(mac.verifyBase64(TEST_DATA, base64Mac)).isTrue();
        }

        @Test
        @DisplayName("verifyBase64无效格式返回false")
        void testVerifyBase64Invalid() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);

            assertThat(mac.verifyBase64(TEST_DATA, "!!!invalid!!!")).isFalse();
        }

        @Test
        @DisplayName("verifyBase64 null抛出异常")
        void testVerifyBase64Null() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);

            assertThatThrownBy(() -> mac.verifyBase64(TEST_DATA, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("增量计算测试")
    class IncrementalTests {

        @Test
        @DisplayName("update和doFinal")
        void testUpdateAndDoFinal() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);

            mac.update("Hello, ".getBytes(StandardCharsets.UTF_8));
            mac.update("World!");
            byte[] incremental = mac.doFinal();

            HmacSha512 mac2 = HmacSha512.of(TEST_KEY);
            byte[] direct = mac2.compute("Hello, World!");

            assertThat(incremental).isEqualTo(direct);
        }

        @Test
        @DisplayName("update null字节数组抛出异常")
        void testUpdateNullBytes() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);

            assertThatThrownBy(() -> mac.update((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("update null字符串抛出异常")
        void testUpdateNullString() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);

            assertThatThrownBy(() -> mac.update((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("doFinalHex")
        void testDoFinalHex() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);
            mac.update(TEST_DATA);
            String result = mac.doFinalHex();

            assertThat(result).hasSize(128);
            assertThat(result).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("doFinalBase64")
        void testDoFinalBase64() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);
            mac.update(TEST_DATA);
            String result = mac.doFinalBase64();

            assertThat(result).matches("[A-Za-z0-9+/=]+");
        }
    }

    @Nested
    @DisplayName("reset测试")
    class ResetTests {

        @Test
        @DisplayName("reset清除状态")
        void testReset() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);

            mac.update("partial");
            mac.reset();
            mac.update(TEST_DATA);
            byte[] result1 = mac.doFinal();

            HmacSha512 mac2 = HmacSha512.of(TEST_KEY);
            byte[] result2 = mac2.compute(TEST_DATA);

            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("reset返回自身")
        void testResetReturnsSelf() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);

            assertThat(mac.reset()).isSameAs(mac);
        }
    }

    @Nested
    @DisplayName("属性测试")
    class PropertyTests {

        @Test
        @DisplayName("getAlgorithm")
        void testGetAlgorithm() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);
            assertThat(mac.getAlgorithm()).isEqualTo("HmacSHA512");
        }

        @Test
        @DisplayName("getMacLength")
        void testGetMacLength() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);
            assertThat(mac.getMacLength()).isEqualTo(64);
        }

        @Test
        @DisplayName("实现Mac接口")
        void testImplementsMacInterface() {
            HmacSha512 mac = HmacSha512.of(TEST_KEY);
            assertThat(mac).isInstanceOf(Mac.class);
        }
    }

    @Nested
    @DisplayName("与HmacSha256对比测试")
    class ComparisonTests {

        @Test
        @DisplayName("HmacSha512输出比HmacSha256长")
        void testDifferentOutputLengths() {
            HmacSha256 mac256 = HmacSha256.of(TEST_KEY);
            HmacSha512 mac512 = HmacSha512.of(TEST_KEY);

            byte[] result256 = mac256.compute(TEST_DATA);
            byte[] result512 = mac512.compute(TEST_DATA);

            assertThat(result512.length).isGreaterThan(result256.length);
            assertThat(result256).hasSize(32);
            assertThat(result512).hasSize(64);
        }

        @Test
        @DisplayName("相同输入产生不同输出")
        void testDifferentAlgorithmsProduceDifferentOutput() {
            HmacSha256 mac256 = HmacSha256.of(TEST_KEY);
            HmacSha512 mac512 = HmacSha512.of(TEST_KEY);

            byte[] result256 = mac256.compute(TEST_DATA);
            byte[] result512 = mac512.compute(TEST_DATA);

            // First 32 bytes of SHA-512 should not equal SHA-256
            byte[] first32 = new byte[32];
            System.arraycopy(result512, 0, first32, 0, 32);

            assertThat(first32).isNotEqualTo(result256);
        }
    }
}
