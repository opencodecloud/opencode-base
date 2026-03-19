package cloud.opencode.base.crypto.mac;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link Mac} interface.
 * Mac接口单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Mac Interface Tests / Mac接口测试")
class MacTest {

    private static final String TEST_DATA = "Hello, World!";
    private static final byte[] TEST_BYTES = "Hello, World!".getBytes();
    private static final byte[] TEST_KEY = "0123456789abcdef0123456789abcdef".getBytes();

    @Nested
    @DisplayName("Interface Contract Tests / 接口契约测试")
    class InterfaceContractTests {

        @Test
        @DisplayName("HmacSha256实现Mac接口")
        void testHmacSha256ImplementsInterface() {
            Mac mac = HmacSha256.of(TEST_KEY);
            assertThat(mac).isInstanceOf(Mac.class);
        }

        @Test
        @DisplayName("HmacSha512实现Mac接口")
        void testHmacSha512ImplementsInterface() {
            Mac mac = HmacSha512.of(TEST_KEY);
            assertThat(mac).isInstanceOf(Mac.class);
        }

        @Test
        @DisplayName("Poly1305实现Mac接口")
        void testPoly1305ImplementsInterface() {
            Mac mac = Poly1305.of(TEST_KEY);
            assertThat(mac).isInstanceOf(Mac.class);
        }

        @Test
        @DisplayName("通过接口调用compute(byte[])方法")
        void testComputeBytesThroughInterface() {
            Mac mac = HmacSha256.of(TEST_KEY);
            byte[] result = mac.compute(TEST_BYTES);
            assertThat(result).isNotNull();
            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("通过接口调用compute(String)方法")
        void testComputeStringThroughInterface() {
            Mac mac = HmacSha256.of(TEST_KEY);
            byte[] result = mac.compute(TEST_DATA);
            assertThat(result).isNotNull();
            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("通过接口调用computeHex方法")
        void testComputeHexThroughInterface() {
            Mac mac = HmacSha256.of(TEST_KEY);
            String hex = mac.computeHex(TEST_BYTES);
            assertThat(hex).isNotNull();
            assertThat(hex).hasSize(64);
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("通过接口调用computeBase64方法")
        void testComputeBase64ThroughInterface() {
            Mac mac = HmacSha256.of(TEST_KEY);
            String base64 = mac.computeBase64(TEST_BYTES);
            assertThat(base64).isNotNull();
            assertThat(base64).isNotEmpty();
        }

        @Test
        @DisplayName("通过接口调用verify方法")
        void testVerifyThroughInterface() {
            Mac mac = HmacSha256.of(TEST_KEY);
            byte[] computed = mac.compute(TEST_BYTES);
            boolean valid = mac.verify(TEST_BYTES, computed);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("通过接口调用verifyHex方法")
        void testVerifyHexThroughInterface() {
            Mac mac = HmacSha256.of(TEST_KEY);
            String hex = mac.computeHex(TEST_BYTES);
            boolean valid = mac.verifyHex(TEST_BYTES, hex);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("通过接口调用verifyBase64方法")
        void testVerifyBase64ThroughInterface() {
            Mac mac = HmacSha256.of(TEST_KEY);
            String base64 = mac.computeBase64(TEST_BYTES);
            boolean valid = mac.verifyBase64(TEST_BYTES, base64);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("通过接口调用getAlgorithm方法")
        void testGetAlgorithmThroughInterface() {
            Mac mac = HmacSha256.of(TEST_KEY);
            String algorithm = mac.getAlgorithm();
            assertThat(algorithm).isEqualTo("HmacSHA256");
        }

        @Test
        @DisplayName("通过接口调用getMacLength方法")
        void testGetMacLengthThroughInterface() {
            Mac mac = HmacSha256.of(TEST_KEY);
            int length = mac.getMacLength();
            assertThat(length).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("Incremental API Tests / 增量API测试")
    class IncrementalApiTests {

        @Test
        @DisplayName("update和doFinal增量计算")
        void testIncrementalComputation() {
            Mac mac = HmacSha256.of(TEST_KEY);

            // Incremental computation
            mac.update("Hello, ");
            mac.update("World!");
            byte[] incremental = mac.doFinal();

            // Direct computation
            mac.reset();
            byte[] direct = mac.compute(TEST_DATA);

            assertThat(incremental).isEqualTo(direct);
        }

        @Test
        @DisplayName("多次update后doFinal")
        void testMultipleUpdates() {
            Mac mac = HmacSha256.of(TEST_KEY);

            mac.update("Part1");
            mac.update("Part2");
            mac.update("Part3");
            byte[] result = mac.doFinal();

            assertThat(result).isNotNull();
            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("reset后可重新计算")
        void testResetAndRecompute() {
            Mac mac = HmacSha256.of(TEST_KEY);

            byte[] first = mac.compute(TEST_DATA);
            mac.reset();
            byte[] second = mac.compute(TEST_DATA);

            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("doFinalHex返回十六进制字符串")
        void testDoFinalHex() {
            Mac mac = HmacSha256.of(TEST_KEY);
            mac.update(TEST_DATA);
            String hex = mac.doFinalHex();

            assertThat(hex).isNotNull();
            assertThat(hex).hasSize(64);
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("doFinalBase64返回Base64字符串")
        void testDoFinalBase64() {
            Mac mac = HmacSha256.of(TEST_KEY);
            mac.update(TEST_DATA);
            String base64 = mac.doFinalBase64();

            assertThat(base64).isNotNull();
            assertThat(base64).isNotEmpty();
        }

        @Test
        @DisplayName("链式调用update")
        void testChainedUpdates() {
            Mac mac = HmacSha256.of(TEST_KEY);

            byte[] result = mac
                    .update("Part1")
                    .update("Part2")
                    .update("Part3")
                    .doFinal();

            assertThat(result).isNotNull();
            assertThat(result).hasSize(32);
        }
    }

    @Nested
    @DisplayName("Polymorphism Tests / 多态测试")
    class PolymorphismTests {

        @Test
        @DisplayName("不同实现通过接口引用")
        void testDifferentImplementations() {
            Mac hmac256 = HmacSha256.of(TEST_KEY);
            Mac hmac512 = HmacSha512.of(TEST_KEY);
            Mac poly1305 = Poly1305.of(TEST_KEY);

            assertThat(hmac256.getMacLength()).isEqualTo(32);
            assertThat(hmac512.getMacLength()).isEqualTo(64);
            assertThat(poly1305.getMacLength()).isEqualTo(16);
        }

        @Test
        @DisplayName("相同数据通过不同实现产生不同MAC")
        void testDifferentMacsFromDifferentAlgorithms() {
            Mac hmac256 = HmacSha256.of(TEST_KEY);
            Mac hmac512 = HmacSha512.of(TEST_KEY);

            String mac1 = hmac256.computeHex(TEST_BYTES);
            String mac2 = hmac512.computeHex(TEST_BYTES);

            assertThat(mac1).isNotEqualTo(mac2);
        }

        @Test
        @DisplayName("通过接口数组批量验证")
        void testBatchVerificationThroughInterface() {
            Mac[] macs = {
                    HmacSha256.of(TEST_KEY),
                    HmacSha512.of(TEST_KEY)
            };

            for (Mac mac : macs) {
                byte[] computed = mac.compute(TEST_DATA);
                boolean valid = mac.verify(TEST_DATA.getBytes(), computed);
                assertThat(valid).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Verification Failure Tests / 验证失败测试")
    class VerificationFailureTests {

        @Test
        @DisplayName("错误的MAC验证失败")
        void testInvalidMacVerificationFails() {
            Mac mac = HmacSha256.of(TEST_KEY);
            byte[] computed = mac.compute(TEST_DATA);

            // Modify one byte
            computed[0] = (byte) (computed[0] ^ 0xFF);

            boolean valid = mac.verify(TEST_BYTES, computed);
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("不同数据的MAC验证失败")
        void testDifferentDataVerificationFails() {
            Mac mac = HmacSha256.of(TEST_KEY);
            byte[] computed = mac.compute(TEST_DATA);

            boolean valid = mac.verify("Different data".getBytes(), computed);
            assertThat(valid).isFalse();
        }
    }
}
