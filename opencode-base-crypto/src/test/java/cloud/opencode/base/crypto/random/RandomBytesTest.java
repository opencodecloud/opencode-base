package cloud.opencode.base.crypto.random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * RandomBytes 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("RandomBytes 测试")
class RandomBytesTest {

    @Nested
    @DisplayName("generate 测试")
    class GenerateTests {

        @Test
        @DisplayName("生成指定长度的随机字节")
        void testGenerate() {
            byte[] result = RandomBytes.generate(16);

            assertThat(result).hasSize(16);
        }

        @Test
        @DisplayName("生成的随机字节不全为零")
        void testGenerateNotAllZeros() {
            byte[] result = RandomBytes.generate(32);

            // 32字节全为零的概率是 2^-256，几乎不可能
            boolean hasNonZero = false;
            for (byte b : result) {
                if (b != 0) {
                    hasNonZero = true;
                    break;
                }
            }
            assertThat(hasNonZero).isTrue();
        }

        @Test
        @DisplayName("生成不同的随机字节")
        void testGenerateDifferent() {
            byte[] result1 = RandomBytes.generate(16);
            byte[] result2 = RandomBytes.generate(16);

            // 两次生成相同的概率是 2^-128，几乎不可能
            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("使用自定义SecureRandom")
        void testGenerateWithCustomRandom() {
            SecureRandom random = new SecureRandom();
            byte[] result = RandomBytes.generate(16, random);

            assertThat(result).hasSize(16);
        }

        @Test
        @DisplayName("长度为0或负数抛出异常")
        void testGenerateInvalidLength() {
            assertThatThrownBy(() -> RandomBytes.generate(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> RandomBytes.generate(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null SecureRandom抛出异常")
        void testGenerateNullRandom() {
            assertThatThrownBy(() -> RandomBytes.generate(16, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("generateHex 测试")
    class GenerateHexTests {

        @Test
        @DisplayName("生成十六进制字符串")
        void testGenerateHex() {
            String result = RandomBytes.generateHex(16);

            assertThat(result).hasSize(32); // 16字节 = 32个十六进制字符
            assertThat(result).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("使用自定义SecureRandom生成十六进制")
        void testGenerateHexWithCustomRandom() {
            SecureRandom random = new SecureRandom();
            String result = RandomBytes.generateHex(8, random);

            assertThat(result).hasSize(16);
        }

        @Test
        @DisplayName("生成不同的十六进制字符串")
        void testGenerateHexDifferent() {
            Set<String> results = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                results.add(RandomBytes.generateHex(8));
            }
            // 100次生成应该都不同
            assertThat(results).hasSize(100);
        }
    }

    @Nested
    @DisplayName("generateBase64 测试")
    class GenerateBase64Tests {

        @Test
        @DisplayName("生成Base64字符串")
        void testGenerateBase64() {
            String result = RandomBytes.generateBase64(12);

            // 12字节 = 16个Base64字符（带填充）
            assertThat(result).hasSize(16);
            assertThat(result).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("使用自定义SecureRandom生成Base64")
        void testGenerateBase64WithCustomRandom() {
            SecureRandom random = new SecureRandom();
            String result = RandomBytes.generateBase64(9, random);

            assertThat(result).hasSize(12); // 9字节 = 12个Base64字符
        }
    }

    @Nested
    @DisplayName("generateBase64Url 测试")
    class GenerateBase64UrlTests {

        @Test
        @DisplayName("生成URL安全的Base64字符串")
        void testGenerateBase64Url() {
            String result = RandomBytes.generateBase64Url(12);

            // URL安全的Base64不包含+和/
            assertThat(result).doesNotContain("+").doesNotContain("/");
            assertThat(result).matches("[A-Za-z0-9_=-]+");
        }

        @Test
        @DisplayName("使用自定义SecureRandom生成URL安全Base64")
        void testGenerateBase64UrlWithCustomRandom() {
            SecureRandom random = new SecureRandom();
            String result = RandomBytes.generateBase64Url(16, random);

            assertThat(result).doesNotContain("+").doesNotContain("/");
        }

        @Test
        @DisplayName("生成不同的URL安全Base64字符串")
        void testGenerateBase64UrlDifferent() {
            Set<String> results = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                results.add(RandomBytes.generateBase64Url(16));
            }
            assertThat(results).hasSize(100);
        }
    }

    @Nested
    @DisplayName("实例化测试")
    class InstantiationTests {

        @Test
        @DisplayName("无法实例化工具类")
        void testCannotInstantiate() {
            assertThatThrownBy(() -> {
                var constructor = RandomBytes.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }).hasCauseInstanceOf(AssertionError.class);
        }
    }
}
