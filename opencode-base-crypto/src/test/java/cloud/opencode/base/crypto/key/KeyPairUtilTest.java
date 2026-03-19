package cloud.opencode.base.crypto.key;

import cloud.opencode.base.crypto.enums.CurveType;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link KeyPairUtil}.
 * 密钥对工具类单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("KeyPairUtil Tests / 密钥对工具类测试")
class KeyPairUtilTest {

    @Nested
    @DisplayName("Instantiation Tests / 实例化测试")
    class InstantiationTests {

        @Test
        @DisplayName("不能实例化工具类")
        void testCannotInstantiate() throws Exception {
            var constructor = KeyPairUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatThrownBy(constructor::newInstance)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Generate Tests / 生成测试")
    class GenerateTests {

        @Test
        @DisplayName("generate生成RSA密钥对")
        void testGenerateRsa() {
            KeyPair keyPair = KeyPairUtil.generate("RSA", 2048);
            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
            assertThat(keyPair.getPublic().getAlgorithm()).isEqualTo("RSA");
        }

        @Test
        @DisplayName("generate(null algorithm)抛出异常")
        void testGenerateNullAlgorithm() {
            assertThatThrownBy(() -> KeyPairUtil.generate(null, 2048))
                .isInstanceOf(OpenKeyException.class)
                .hasMessageContaining("null or empty");
        }

        @Test
        @DisplayName("generate(empty algorithm)抛出异常")
        void testGenerateEmptyAlgorithm() {
            assertThatThrownBy(() -> KeyPairUtil.generate("", 2048))
                .isInstanceOf(OpenKeyException.class)
                .hasMessageContaining("null or empty");
        }

        @Test
        @DisplayName("generate(invalid keySize)抛出异常")
        void testGenerateInvalidKeySize() {
            assertThatThrownBy(() -> KeyPairUtil.generate("RSA", 0))
                .isInstanceOf(OpenKeyException.class)
                .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("generate(negative keySize)抛出异常")
        void testGenerateNegativeKeySize() {
            assertThatThrownBy(() -> KeyPairUtil.generate("RSA", -1))
                .isInstanceOf(OpenKeyException.class)
                .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("generate(invalid algorithm)抛出异常")
        void testGenerateInvalidAlgorithm() {
            assertThatThrownBy(() -> KeyPairUtil.generate("INVALID_ALGO", 2048))
                .isInstanceOf(OpenKeyException.class);
        }
    }

    @Nested
    @DisplayName("GenerateEc Tests / EC生成测试")
    class GenerateEcTests {

        @Test
        @DisplayName("generateEc生成P-256密钥对")
        void testGenerateEcP256() {
            KeyPair keyPair = KeyPairUtil.generateEc(CurveType.P_256);
            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
            assertThat(keyPair.getPublic().getAlgorithm()).isEqualTo("EC");
        }

        @Test
        @DisplayName("generateEc生成P-384密钥对")
        void testGenerateEcP384() {
            KeyPair keyPair = KeyPairUtil.generateEc(CurveType.P_384);
            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic().getAlgorithm()).isEqualTo("EC");
        }

        @Test
        @DisplayName("generateEc生成P-521密钥对")
        void testGenerateEcP521() {
            KeyPair keyPair = KeyPairUtil.generateEc(CurveType.P_521);
            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic().getAlgorithm()).isEqualTo("EC");
        }

        @Test
        @DisplayName("generateEc(null)抛出异常")
        void testGenerateEcNull() {
            assertThatThrownBy(() -> KeyPairUtil.generateEc(null))
                .isInstanceOf(OpenKeyException.class)
                .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("ExtractPublicKey Tests / 提取公钥测试")
    class ExtractPublicKeyTests {

        @Test
        @DisplayName("extractPublicKey从RSA私钥提取公钥")
        void testExtractPublicKeyFromRsa() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            // Only works with RSAPrivateCrtKey
            if (keyPair.getPrivate() instanceof RSAPrivateCrtKey) {
                var publicKey = KeyPairUtil.extractPublicKey(keyPair.getPrivate());
                assertThat(publicKey).isNotNull();
                assertThat(publicKey.getAlgorithm()).isEqualTo("RSA");
            }
        }

        @Test
        @DisplayName("extractPublicKey(null)抛出异常")
        void testExtractPublicKeyNull() {
            assertThatThrownBy(() -> KeyPairUtil.extractPublicKey(null))
                .isInstanceOf(OpenKeyException.class)
                .hasMessageContaining("null");
        }

        @Test
        @DisplayName("extractPublicKey从EC私钥不支持")
        void testExtractPublicKeyFromEc() {
            KeyPair keyPair = KeyPairUtil.generateEc(CurveType.P_256);
            assertThatThrownBy(() -> KeyPairUtil.extractPublicKey(keyPair.getPrivate()))
                .isInstanceOf(OpenKeyException.class)
                .hasMessageContaining("EC public key");
        }
    }

    @Nested
    @DisplayName("IsMatchingPair Tests / 密钥对匹配测试")
    class IsMatchingPairTests {

        @Test
        @DisplayName("isMatchingPair对于匹配的RSA密钥对返回true")
        void testIsMatchingPairRsa() {
            KeyPair keyPair = KeyPairUtil.generate("RSA", 2048);
            boolean matching = KeyPairUtil.isMatchingPair(keyPair.getPublic(), keyPair.getPrivate());
            assertThat(matching).isTrue();
        }

        @Test
        @DisplayName("isMatchingPair对于匹配的EC密钥对返回true")
        void testIsMatchingPairEc() {
            KeyPair keyPair = KeyPairUtil.generateEc(CurveType.P_256);
            boolean matching = KeyPairUtil.isMatchingPair(keyPair.getPublic(), keyPair.getPrivate());
            assertThat(matching).isTrue();
        }

        @Test
        @DisplayName("isMatchingPair对于匹配的Ed25519密钥对返回true")
        void testIsMatchingPairEd25519() {
            KeyPair keyPair = KeyGenerator.generateEd25519KeyPair();
            boolean matching = KeyPairUtil.isMatchingPair(keyPair.getPublic(), keyPair.getPrivate());
            assertThat(matching).isTrue();
        }

        @Test
        @DisplayName("isMatchingPair对于不匹配的密钥对返回false")
        void testIsMatchingPairNotMatching() {
            KeyPair keyPair1 = KeyPairUtil.generate("RSA", 2048);
            KeyPair keyPair2 = KeyPairUtil.generate("RSA", 2048);
            boolean matching = KeyPairUtil.isMatchingPair(keyPair1.getPublic(), keyPair2.getPrivate());
            assertThat(matching).isFalse();
        }

        @Test
        @DisplayName("isMatchingPair对于不同算法返回false")
        void testIsMatchingPairDifferentAlgorithms() {
            KeyPair rsaKeyPair = KeyPairUtil.generate("RSA", 2048);
            KeyPair ecKeyPair = KeyPairUtil.generateEc(CurveType.P_256);
            boolean matching = KeyPairUtil.isMatchingPair(rsaKeyPair.getPublic(), ecKeyPair.getPrivate());
            assertThat(matching).isFalse();
        }

        @Test
        @DisplayName("isMatchingPair(null publicKey)抛出异常")
        void testIsMatchingPairNullPublicKey() {
            KeyPair keyPair = KeyPairUtil.generate("RSA", 2048);
            assertThatThrownBy(() -> KeyPairUtil.isMatchingPair(null, keyPair.getPrivate()))
                .isInstanceOf(OpenKeyException.class)
                .hasMessageContaining("null");
        }

        @Test
        @DisplayName("isMatchingPair(null privateKey)抛出异常")
        void testIsMatchingPairNullPrivateKey() {
            KeyPair keyPair = KeyPairUtil.generate("RSA", 2048);
            assertThatThrownBy(() -> KeyPairUtil.isMatchingPair(keyPair.getPublic(), null))
                .isInstanceOf(OpenKeyException.class)
                .hasMessageContaining("null");
        }
    }
}
