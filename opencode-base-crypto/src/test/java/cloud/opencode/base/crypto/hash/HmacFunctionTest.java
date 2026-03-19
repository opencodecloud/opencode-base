package cloud.opencode.base.crypto.hash;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.*;

/**
 * HmacFunction 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("HmacFunction 测试")
class HmacFunctionTest {

    private static final byte[] TEST_KEY = "secret-key-for-testing-hmac-256".getBytes(StandardCharsets.UTF_8);
    private static final byte[] TEST_DATA = "Hello, World!".getBytes(StandardCharsets.UTF_8);
    private static final String TEST_STRING = "Hello, World!";

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("hmacSha256创建HMAC-SHA256")
        void testHmacSha256Factory() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            assertThat(hmac.getAlgorithm()).isEqualTo("HmacSHA256");
        }

        @Test
        @DisplayName("hmacSha384创建HMAC-SHA384")
        void testHmacSha384Factory() {
            HmacFunction hmac = HmacFunction.hmacSha384(TEST_KEY);

            assertThat(hmac.getAlgorithm()).isEqualTo("HmacSHA384");
        }

        @Test
        @DisplayName("hmacSha512创建HMAC-SHA512")
        void testHmacSha512Factory() {
            HmacFunction hmac = HmacFunction.hmacSha512(TEST_KEY);

            assertThat(hmac.getAlgorithm()).isEqualTo("HmacSHA512");
        }

        @Test
        @DisplayName("of创建自定义算法的HMAC")
        void testOfFactory() {
            HmacFunction hmac = HmacFunction.of("HmacSHA1", TEST_KEY);

            assertThat(hmac.getAlgorithm()).isEqualTo("HmacSHA1");
        }

        @Test
        @DisplayName("null密钥抛出NullPointerException")
        void testNullKeyThrowsException() {
            assertThatThrownBy(() -> HmacFunction.hmacSha256(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Key cannot be null");
        }

        @Test
        @DisplayName("空密钥抛出IllegalArgumentException")
        void testEmptyKeyThrowsException() {
            assertThatThrownBy(() -> HmacFunction.hmacSha256(new byte[0]))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Key cannot be empty");
        }

        @Test
        @DisplayName("null算法抛出NullPointerException")
        void testNullAlgorithmThrowsException() {
            assertThatThrownBy(() -> HmacFunction.of(null, TEST_KEY))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Algorithm cannot be null");
        }
    }

    @Nested
    @DisplayName("mac(byte[]) 测试")
    class MacByteArrayTests {

        @Test
        @DisplayName("mac返回正确长度的字节数组")
        void testMacReturnsCorrectLength() {
            assertThat(HmacFunction.hmacSha256(TEST_KEY).mac(TEST_DATA)).hasSize(32);
            assertThat(HmacFunction.hmacSha384(TEST_KEY).mac(TEST_DATA)).hasSize(48);
            assertThat(HmacFunction.hmacSha512(TEST_KEY).mac(TEST_DATA)).hasSize(64);
        }

        @Test
        @DisplayName("相同输入产生相同MAC")
        void testMacDeterministic() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            byte[] mac1 = hmac.mac(TEST_DATA);
            byte[] mac2 = hmac.mac(TEST_DATA);

            assertThat(mac1).isEqualTo(mac2);
        }

        @Test
        @DisplayName("不同数据产生不同MAC")
        void testDifferentDataDifferentMac() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            byte[] mac1 = hmac.mac("data1".getBytes(StandardCharsets.UTF_8));
            byte[] mac2 = hmac.mac("data2".getBytes(StandardCharsets.UTF_8));

            assertThat(mac1).isNotEqualTo(mac2);
        }

        @Test
        @DisplayName("不同密钥产生不同MAC")
        void testDifferentKeyDifferentMac() {
            HmacFunction hmac1 = HmacFunction.hmacSha256("key1".getBytes(StandardCharsets.UTF_8));
            HmacFunction hmac2 = HmacFunction.hmacSha256("key2".getBytes(StandardCharsets.UTF_8));

            byte[] mac1 = hmac1.mac(TEST_DATA);
            byte[] mac2 = hmac2.mac(TEST_DATA);

            assertThat(mac1).isNotEqualTo(mac2);
        }

        @Test
        @DisplayName("null数据抛出NullPointerException")
        void testMacNullDataThrowsException() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            assertThatThrownBy(() -> hmac.mac((byte[]) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Data cannot be null");
        }

        @Test
        @DisplayName("空数据有效")
        void testMacEmptyData() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            byte[] result = hmac.mac(new byte[0]);

            assertThat(result).hasSize(32);
        }
    }

    @Nested
    @DisplayName("mac(String) 测试")
    class MacStringTests {

        @Test
        @DisplayName("mac字符串与字节数组一致")
        void testMacStringEqualsBytes() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            byte[] macFromString = hmac.mac(TEST_STRING);
            byte[] macFromBytes = hmac.mac(TEST_DATA);

            assertThat(macFromString).isEqualTo(macFromBytes);
        }

        @Test
        @DisplayName("null字符串抛出NullPointerException")
        void testMacNullStringThrowsException() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            assertThatThrownBy(() -> hmac.mac((String) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Data cannot be null");
        }

        @Test
        @DisplayName("空字符串有效")
        void testMacEmptyString() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            byte[] result = hmac.mac("");

            assertThat(result).hasSize(32);
        }
    }

    @Nested
    @DisplayName("macHex 测试")
    class MacHexTests {

        @Test
        @DisplayName("macHex返回十六进制字符串")
        void testMacHex() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            String hex = hmac.macHex(TEST_DATA);

            assertThat(hex).hasSize(64); // 32字节 = 64个十六进制字符
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("macHex相同输入产生相同输出")
        void testMacHexDeterministic() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            String hex1 = hmac.macHex(TEST_DATA);
            String hex2 = hmac.macHex(TEST_DATA);

            assertThat(hex1).isEqualTo(hex2);
        }
    }

    @Nested
    @DisplayName("macBase64 测试")
    class MacBase64Tests {

        @Test
        @DisplayName("macBase64返回Base64字符串")
        void testMacBase64() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            String base64 = hmac.macBase64(TEST_DATA);

            assertThat(base64).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("macBase64相同输入产生相同输出")
        void testMacBase64Deterministic() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            String base641 = hmac.macBase64(TEST_DATA);
            String base642 = hmac.macBase64(TEST_DATA);

            assertThat(base641).isEqualTo(base642);
        }
    }

    @Nested
    @DisplayName("verify 测试")
    class VerifyTests {

        @Test
        @DisplayName("verify对有效MAC返回true")
        void testVerifyValidMac() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            byte[] mac = hmac.mac(TEST_DATA);
            boolean valid = hmac.verify(TEST_DATA, mac);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("verify对无效MAC返回false")
        void testVerifyInvalidMac() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            byte[] mac = hmac.mac(TEST_DATA);
            mac[0] ^= 0xFF; // 修改一个字节

            boolean valid = hmac.verify(TEST_DATA, mac);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("verify对篡改的数据返回false")
        void testVerifyTamperedData() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            byte[] mac = hmac.mac(TEST_DATA);
            byte[] tamperedData = "Tampered!".getBytes(StandardCharsets.UTF_8);

            boolean valid = hmac.verify(tamperedData, mac);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("verify对错误长度的MAC返回false")
        void testVerifyWrongLengthMac() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            byte[] wrongLengthMac = new byte[16]; // 应该是32字节

            boolean valid = hmac.verify(TEST_DATA, wrongLengthMac);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("verify使用恒定时间比较")
        void testVerifyConstantTimeComparison() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);
            byte[] validMac = hmac.mac(TEST_DATA);

            // 多次验证应该花费相似的时间（无法直接测试，但确保不抛出异常）
            for (int i = 0; i < 100; i++) {
                byte[] testMac = validMac.clone();
                testMac[i % 32] ^= 0xFF;
                assertThat(hmac.verify(TEST_DATA, testMac)).isFalse();
            }
        }

        @Test
        @DisplayName("verify null MAC抛出NullPointerException")
        void testVerifyNullMacThrowsException() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            assertThatThrownBy(() -> hmac.verify(TEST_DATA, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("MAC cannot be null");
        }

        @Test
        @DisplayName("verify null数据抛出NullPointerException")
        void testVerifyNullDataThrowsException() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);
            byte[] mac = hmac.mac(TEST_DATA);

            assertThatThrownBy(() -> hmac.verify(null, mac))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getAlgorithm 测试")
    class GetAlgorithmTests {

        @Test
        @DisplayName("getAlgorithm返回正确算法名")
        void testGetAlgorithm() {
            assertThat(HmacFunction.hmacSha256(TEST_KEY).getAlgorithm()).isEqualTo("HmacSHA256");
            assertThat(HmacFunction.hmacSha384(TEST_KEY).getAlgorithm()).isEqualTo("HmacSHA384");
            assertThat(HmacFunction.hmacSha512(TEST_KEY).getAlgorithm()).isEqualTo("HmacSHA512");
            assertThat(HmacFunction.of("HmacSHA1", TEST_KEY).getAlgorithm()).isEqualTo("HmacSHA1");
        }
    }

    @Nested
    @DisplayName("密钥防御性复制测试")
    class DefensiveCopyTests {

        @Test
        @DisplayName("密钥是防御性复制的")
        void testKeyDefensiveCopy() {
            byte[] key = "original-key-1234567890123456".getBytes(StandardCharsets.UTF_8);
            HmacFunction hmac = HmacFunction.hmacSha256(key);

            // 修改原始密钥
            byte[] originalMac = hmac.mac(TEST_DATA);
            key[0] = 'X';

            // HMAC应该仍然使用原始密钥
            byte[] newMac = hmac.mac(TEST_DATA);

            assertThat(newMac).isEqualTo(originalMac);
        }
    }

    @Nested
    @DisplayName("已知向量测试")
    class KnownVectorTests {

        @Test
        @DisplayName("HMAC-SHA256已知测试向量")
        void testHmacSha256KnownVector() {
            // RFC 4231 Test Case 1
            byte[] key = new byte[20];
            java.util.Arrays.fill(key, (byte) 0x0b);

            HmacFunction hmac = HmacFunction.hmacSha256(key);
            String result = hmac.macHex("Hi There".getBytes(StandardCharsets.UTF_8));

            assertThat(result).isEqualTo("b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7");
        }

        @Test
        @DisplayName("HMAC-SHA512已知测试向量")
        void testHmacSha512KnownVector() {
            // RFC 4231 Test Case 1
            byte[] key = new byte[20];
            java.util.Arrays.fill(key, (byte) 0x0b);

            HmacFunction hmac = HmacFunction.hmacSha512(key);
            String result = hmac.macHex("Hi There".getBytes(StandardCharsets.UTF_8));

            assertThat(result).isEqualTo("87aa7cdea5ef619d4ff0b4241a1d6cb02379f4e2ce4ec2787ad0b30545e17cdedaa833b7d6b8a702038b274eaea3f4e4be9d914eeb61f1702e696c203a126854");
        }
    }

    @Nested
    @DisplayName("无效算法测试")
    class InvalidAlgorithmTests {

        @Test
        @DisplayName("无效算法名抛出OpenCryptoException")
        void testInvalidAlgorithmThrowsException() {
            HmacFunction hmac = HmacFunction.of("InvalidAlgorithm", TEST_KEY);

            assertThatThrownBy(() -> hmac.mac(TEST_DATA))
                    .isInstanceOf(OpenCryptoException.class);
        }
    }

    @Nested
    @DisplayName("大数据测试")
    class LargeDataTests {

        @Test
        @DisplayName("处理大数据")
        void testLargeData() {
            HmacFunction hmac = HmacFunction.hmacSha256(TEST_KEY);

            byte[] largeData = new byte[1024 * 1024]; // 1MB
            new SecureRandom().nextBytes(largeData);

            byte[] mac = hmac.mac(largeData);

            assertThat(mac).hasSize(32);
            assertThat(hmac.verify(largeData, mac)).isTrue();
        }
    }
}
