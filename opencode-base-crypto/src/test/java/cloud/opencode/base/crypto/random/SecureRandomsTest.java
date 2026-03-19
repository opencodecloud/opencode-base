package cloud.opencode.base.crypto.random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.security.DrbgParameters;

import static org.assertj.core.api.Assertions.*;

/**
 * SecureRandoms 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("SecureRandoms 测试")
class SecureRandomsTest {

    @Nested
    @DisplayName("getDefault 测试")
    class GetDefaultTests {

        @Test
        @DisplayName("获取默认SecureRandom")
        void testGetDefault() {
            SecureRandom random = SecureRandoms.getDefault();
            assertThat(random).isNotNull();
        }

        @Test
        @DisplayName("每次调用返回新实例")
        void testGetDefaultNewInstance() {
            SecureRandom random1 = SecureRandoms.getDefault();
            SecureRandom random2 = SecureRandoms.getDefault();

            assertThat(random1).isNotSameAs(random2);
        }
    }

    @Nested
    @DisplayName("getStrong 测试")
    class GetStrongTests {

        @Test
        @DisplayName("获取强SecureRandom")
        void testGetStrong() {
            SecureRandom random = SecureRandoms.getStrong();
            assertThat(random).isNotNull();
        }
    }

    @Nested
    @DisplayName("getDrbg 测试")
    class GetDrbgTests {

        @Test
        @DisplayName("获取默认DRBG")
        void testGetDrbg() {
            SecureRandom random = SecureRandoms.getDrbg();
            assertThat(random).isNotNull();
        }

        @Test
        @DisplayName("获取自定义配置DRBG")
        void testGetDrbgWithConfig() {
            SecureRandom random = SecureRandoms.getDrbg(
                    256,
                    DrbgParameters.Capability.PR_AND_RESEED,
                    null
            );
            assertThat(random).isNotNull();
        }

        @Test
        @DisplayName("使用个性化字符串配置DRBG")
        void testGetDrbgWithPersonalization() {
            byte[] personalization = "my-app-instance-1".getBytes();
            SecureRandom random = SecureRandoms.getDrbg(
                    256,
                    DrbgParameters.Capability.RESEED_ONLY,
                    personalization
            );
            assertThat(random).isNotNull();
        }
    }

    @Nested
    @DisplayName("getInstance 测试")
    class GetInstanceTests {

        @Test
        @DisplayName("获取SHA1PRNG实例")
        void testGetInstanceSha1Prng() {
            SecureRandom random = SecureRandoms.getInstance("SHA1PRNG");
            assertThat(random).isNotNull();
            assertThat(random.getAlgorithm()).isEqualTo("SHA1PRNG");
        }

        @Test
        @DisplayName("null算法抛出异常")
        void testGetInstanceNullAlgorithm() {
            assertThatThrownBy(() -> SecureRandoms.getInstance(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("空白算法抛出异常")
        void testGetInstanceBlankAlgorithm() {
            assertThatThrownBy(() -> SecureRandoms.getInstance("  "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("不存在的算法抛出异常")
        void testGetInstanceInvalidAlgorithm() {
            assertThatThrownBy(() -> SecureRandoms.getInstance("INVALID_ALGORITHM"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getInstance(algorithm, provider) 测试")
    class GetInstanceWithProviderTests {

        @Test
        @DisplayName("获取指定提供者的实例")
        void testGetInstanceWithProvider() {
            SecureRandom random = SecureRandoms.getInstance("SHA1PRNG", "SUN");
            assertThat(random).isNotNull();
        }

        @Test
        @DisplayName("null提供者抛出异常")
        void testGetInstanceNullProvider() {
            assertThatThrownBy(() -> SecureRandoms.getInstance("SHA1PRNG", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("空白提供者抛出异常")
        void testGetInstanceBlankProvider() {
            assertThatThrownBy(() -> SecureRandoms.getInstance("SHA1PRNG", "  "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("不存在的提供者抛出异常")
        void testGetInstanceInvalidProvider() {
            assertThatThrownBy(() -> SecureRandoms.getInstance("SHA1PRNG", "INVALID_PROVIDER"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("generateSeed 测试")
    class GenerateSeedTests {

        @Test
        @DisplayName("生成种子字节")
        void testGenerateSeed() {
            byte[] seed = SecureRandoms.generateSeed(16);
            assertThat(seed).hasSize(16);
        }

        @Test
        @DisplayName("生成不同的种子")
        void testGenerateSeedDifferent() {
            byte[] seed1 = SecureRandoms.generateSeed(16);
            byte[] seed2 = SecureRandoms.generateSeed(16);

            assertThat(seed1).isNotEqualTo(seed2);
        }

        @Test
        @DisplayName("非正数长度抛出异常")
        void testGenerateSeedInvalidLength() {
            assertThatThrownBy(() -> SecureRandoms.generateSeed(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> SecureRandoms.generateSeed(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("nextBytes 测试")
    class NextBytesTests {

        @Test
        @DisplayName("生成随机字节")
        void testNextBytes() {
            byte[] bytes = SecureRandoms.nextBytes(32);
            assertThat(bytes).hasSize(32);
        }

        @Test
        @DisplayName("生成不同的随机字节")
        void testNextBytesDifferent() {
            byte[] bytes1 = SecureRandoms.nextBytes(32);
            byte[] bytes2 = SecureRandoms.nextBytes(32);

            assertThat(bytes1).isNotEqualTo(bytes2);
        }

        @Test
        @DisplayName("非正数长度抛出异常")
        void testNextBytesInvalidLength() {
            assertThatThrownBy(() -> SecureRandoms.nextBytes(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> SecureRandoms.nextBytes(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("实例化测试")
    class InstantiationTests {

        @Test
        @DisplayName("无法实例化工具类")
        void testCannotInstantiate() {
            assertThatThrownBy(() -> {
                var constructor = SecureRandoms.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }).hasCauseInstanceOf(AssertionError.class);
        }
    }
}
