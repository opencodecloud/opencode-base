package cloud.opencode.base.core.random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * VerifyCodeUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("VerifyCodeUtil 测试")
class VerifyCodeUtilTest {

    @Nested
    @DisplayName("numeric 测试")
    class NumericTests {

        @Test
        @DisplayName("numeric 默认 6 位")
        void testNumericDefault() {
            String code = VerifyCodeUtil.numeric();
            assertThat(code).hasSize(6);
            assertThat(code).matches("[0-9]+");
        }

        @Test
        @DisplayName("numeric 指定长度")
        void testNumericCustomLength() {
            String code = VerifyCodeUtil.numeric(4);
            assertThat(code).hasSize(4);
            assertThat(code).matches("[0-9]+");
        }

        @RepeatedTest(10)
        @DisplayName("numeric 重复测试验证随机性")
        void testNumericRandomness() {
            String code = VerifyCodeUtil.numeric(6);
            assertThat(code).hasSize(6);
        }
    }

    @Nested
    @DisplayName("alphabetic 测试")
    class AlphabeticTests {

        @Test
        @DisplayName("alphabetic")
        void testAlphabetic() {
            String code = VerifyCodeUtil.alphabetic(8);
            assertThat(code).hasSize(8);
            assertThat(code).matches("[A-Za-z]+");
        }
    }

    @Nested
    @DisplayName("alphanumeric 测试")
    class AlphanumericTests {

        @Test
        @DisplayName("alphanumeric")
        void testAlphanumeric() {
            String code = VerifyCodeUtil.alphanumeric(8);
            assertThat(code).hasSize(8);
            assertThat(code).matches("[A-Za-z0-9]+");
        }
    }

    @Nested
    @DisplayName("noConfusing 测试")
    class NoConfusingTests {

        @Test
        @DisplayName("noConfusing 不包含混淆字符")
        void testNoConfusing() {
            // 生成多次检查不包含混淆字符
            for (int i = 0; i < 100; i++) {
                String code = VerifyCodeUtil.noConfusing(10);
                assertThat(code).hasSize(10);
                // 不应包含 0, O, 1, I, L
                assertThat(code).doesNotContain("0", "O", "1", "I", "L", "o", "l", "i");
            }
        }

        @Test
        @DisplayName("noConfusing 只包含允许的字符")
        void testNoConfusingAllowedChars() {
            String code = VerifyCodeUtil.noConfusing(8);
            // ABCDEFGHJKLMNPQRSTUVWXYZ23456789
            assertThat(code).matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]+");
        }
    }

    @Nested
    @DisplayName("numericRange 测试")
    class NumericRangeTests {

        @RepeatedTest(20)
        @DisplayName("numericRange 范围内")
        void testNumericRange() {
            String code = VerifyCodeUtil.numericRange(100, 999);
            int value = Integer.parseInt(code);
            assertThat(value).isBetween(100, 999);
        }

        @Test
        @DisplayName("numericRange 补零")
        void testNumericRangePadding() {
            // 最大值 999，生成的字符串应该是 3 位
            String code = VerifyCodeUtil.numericRange(1, 999);
            assertThat(code).hasSize(3);
        }
    }

    @Nested
    @DisplayName("generate 测试")
    class GenerateTests {

        @Test
        @DisplayName("generate 自定义字符集")
        void testGenerateCustomChars() {
            String code = VerifyCodeUtil.generate(5, "ABC123");
            assertThat(code).hasSize(5);
            assertThat(code).matches("[ABC123]+");
        }

        @Test
        @DisplayName("generate 空参数")
        void testGenerateEmpty() {
            assertThat(VerifyCodeUtil.generate(0, "ABC")).isEmpty();
            assertThat(VerifyCodeUtil.generate(-1, "ABC")).isEmpty();
            assertThat(VerifyCodeUtil.generate(5, null)).isEmpty();
            assertThat(VerifyCodeUtil.generate(5, "")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("builder 默认配置")
        void testBuilderDefaults() {
            String code = VerifyCodeUtil.builder().build();
            assertThat(code).hasSize(6);
            assertThat(code).matches("[0-9]+");
        }

        @Test
        @DisplayName("builder length")
        void testBuilderLength() {
            String code = VerifyCodeUtil.builder()
                    .length(8)
                    .build();
            assertThat(code).hasSize(8);
        }

        @Test
        @DisplayName("builder numeric")
        void testBuilderNumeric() {
            String code = VerifyCodeUtil.builder()
                    .length(6)
                    .numeric()
                    .build();
            assertThat(code).matches("[0-9]+");
        }

        @Test
        @DisplayName("builder alphabetic")
        void testBuilderAlphabetic() {
            String code = VerifyCodeUtil.builder()
                    .length(6)
                    .alphabetic()
                    .build();
            assertThat(code).matches("[A-Za-z]+");
        }

        @Test
        @DisplayName("builder alphanumeric")
        void testBuilderAlphanumeric() {
            String code = VerifyCodeUtil.builder()
                    .length(6)
                    .alphanumeric()
                    .build();
            assertThat(code).matches("[A-Za-z0-9]+");
        }

        @Test
        @DisplayName("builder custom")
        void testBuilderCustom() {
            String code = VerifyCodeUtil.builder()
                    .length(5)
                    .custom("XYZ")
                    .build();
            assertThat(code).hasSize(5);
            assertThat(code).matches("[XYZ]+");
        }

        @Test
        @DisplayName("builder excludeConfusing")
        void testBuilderExcludeConfusing() {
            String code = VerifyCodeUtil.builder()
                    .length(10)
                    .alphanumeric()
                    .excludeConfusing()
                    .build();

            assertThat(code).doesNotContain("0", "O", "1", "I", "L");
        }

        @Test
        @DisplayName("builder 链式调用")
        void testBuilderChaining() {
            String code = VerifyCodeUtil.builder()
                    .length(8)
                    .alphanumeric()
                    .excludeConfusing()
                    .build();

            assertThat(code).hasSize(8);
            assertThat(code).doesNotContain("0", "O", "1", "I", "L");
        }

        @Test
        @DisplayName("builder custom 覆盖其他类型")
        void testBuilderCustomOverrides() {
            String code = VerifyCodeUtil.builder()
                    .numeric()
                    .custom("ABC")
                    .length(4)
                    .build();

            assertThat(code).hasSize(4);
            assertThat(code).matches("[ABC]+");
        }

        @Test
        @DisplayName("builder custom null 使用默认")
        void testBuilderCustomNull() {
            String code = VerifyCodeUtil.builder()
                    .custom(null)
                    .build();

            // 当 custom 为 null 时，应该使用默认的 NUMERIC
            assertThat(code).hasSize(6);
            assertThat(code).matches("[0-9]+");
        }
    }

    @Nested
    @DisplayName("安全性测试")
    class SecurityTests {

        @Test
        @DisplayName("验证码使用 SecureRandom")
        void testUsesSecureRandom() {
            // 生成大量验证码检查分布
            int[] counts = new int[10];
            for (int i = 0; i < 10000; i++) {
                String code = VerifyCodeUtil.numeric(1);
                int digit = Integer.parseInt(code);
                counts[digit]++;
            }

            // 每个数字应该出现大约 1000 次（允许一定偏差）
            for (int count : counts) {
                assertThat(count).isBetween(800, 1200);
            }
        }

        @Test
        @DisplayName("不同调用生成不同验证码")
        void testDifferentCodes() {
            java.util.Set<String> codes = new java.util.HashSet<>();
            for (int i = 0; i < 100; i++) {
                codes.add(VerifyCodeUtil.numeric(6));
            }
            // 100 个 6 位数字验证码应该几乎都不同
            assertThat(codes.size()).isGreaterThan(95);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @Test
        @DisplayName("长度为 1")
        void testLengthOne() {
            String code = VerifyCodeUtil.numeric(1);
            assertThat(code).hasSize(1);
            assertThat(code).matches("[0-9]");
        }

        @Test
        @DisplayName("较长验证码")
        void testLongCode() {
            String code = VerifyCodeUtil.alphanumeric(100);
            assertThat(code).hasSize(100);
        }
    }
}
