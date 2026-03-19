package cloud.opencode.base.crypto.keyexchange;

import cloud.opencode.base.crypto.enums.CurveType;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link EcdhEngine}.
 * EcdhEngine单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("EcdhEngine Tests / EcdhEngine测试")
class EcdhEngineTest {

    @Nested
    @DisplayName("Factory Method Tests / 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("p256()创建P-256曲线引擎")
        void testP256Factory() {
            EcdhEngine engine = EcdhEngine.p256();
            assertThat(engine).isNotNull();
            assertThat(engine.getCurve()).isEqualTo(CurveType.P_256);
            assertThat(engine.getAlgorithm()).contains("ECDH").contains("P_256");
        }

        @Test
        @DisplayName("p384()创建P-384曲线引擎")
        void testP384Factory() {
            EcdhEngine engine = EcdhEngine.p384();
            assertThat(engine).isNotNull();
            assertThat(engine.getCurve()).isEqualTo(CurveType.P_384);
            assertThat(engine.getAlgorithm()).contains("ECDH").contains("P_384");
        }

        @Test
        @DisplayName("p521()创建P-521曲线引擎")
        void testP521Factory() {
            EcdhEngine engine = EcdhEngine.p521();
            assertThat(engine).isNotNull();
            assertThat(engine.getCurve()).isEqualTo(CurveType.P_521);
            assertThat(engine.getAlgorithm()).contains("ECDH").contains("P_521");
        }

        @Test
        @DisplayName("withCurve()创建指定曲线引擎")
        void testWithCurveFactory() {
            EcdhEngine engine = EcdhEngine.withCurve(CurveType.SECP256K1);
            assertThat(engine).isNotNull();
            assertThat(engine.getCurve()).isEqualTo(CurveType.SECP256K1);
        }

        @Test
        @DisplayName("withCurve(null)抛出异常")
        void testWithCurveNullThrows() {
            assertThatThrownBy(() -> EcdhEngine.withCurve(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Curve cannot be null");
        }

        @Test
        @DisplayName("withCurve(CURVE25519)抛出异常")
        void testWithCurve25519Throws() {
            assertThatThrownBy(() -> EcdhEngine.withCurve(CurveType.CURVE25519))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("X25519Engine");
        }

        @Test
        @DisplayName("withCurve(CURVE448)抛出异常")
        void testWithCurve448Throws() {
            assertThatThrownBy(() -> EcdhEngine.withCurve(CurveType.CURVE448))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("X448Engine");
        }

        @Test
        @DisplayName("withGeneratedKeyPair()创建带密钥对的引擎")
        void testWithGeneratedKeyPairFactory() {
            EcdhEngine engine = EcdhEngine.withGeneratedKeyPair(CurveType.P_256);
            assertThat(engine).isNotNull();
            assertThat(engine.getCurve()).isEqualTo(CurveType.P_256);
        }

        @Test
        @DisplayName("withGeneratedKeyPair(null)抛出异常")
        void testWithGeneratedKeyPairNullThrows() {
            assertThatThrownBy(() -> EcdhEngine.withGeneratedKeyPair(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Curve cannot be null");
        }
    }

    @Nested
    @DisplayName("Key Generation Tests / 密钥生成测试")
    class KeyGenerationTests {

        @Test
        @DisplayName("generateKeyPair()生成P-256密钥对")
        void testGenerateKeyPairP256() {
            EcdhEngine engine = EcdhEngine.p256();
            KeyPair keyPair = engine.generateKeyPair();

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
            assertThat(keyPair.getPublic().getAlgorithm()).isEqualTo("EC");
            assertThat(keyPair.getPrivate().getAlgorithm()).isEqualTo("EC");
        }

        @Test
        @DisplayName("generateKeyPair()生成P-384密钥对")
        void testGenerateKeyPairP384() {
            EcdhEngine engine = EcdhEngine.p384();
            KeyPair keyPair = engine.generateKeyPair();

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
        }

        @Test
        @DisplayName("generateKeyPair()生成P-521密钥对")
        void testGenerateKeyPairP521() {
            EcdhEngine engine = EcdhEngine.p521();
            KeyPair keyPair = engine.generateKeyPair();

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
        }

        @Test
        @DisplayName("generateKeyPair()每次生成不同密钥对")
        void testGenerateKeyPairUnique() {
            EcdhEngine engine = EcdhEngine.p256();
            KeyPair keyPair1 = engine.generateKeyPair();
            KeyPair keyPair2 = engine.generateKeyPair();

            assertThat(keyPair1.getPublic().getEncoded())
                    .isNotEqualTo(keyPair2.getPublic().getEncoded());
            assertThat(keyPair1.getPrivate().getEncoded())
                    .isNotEqualTo(keyPair2.getPrivate().getEncoded());
        }
    }

    @Nested
    @DisplayName("Key Agreement Tests / 密钥协商测试")
    class KeyAgreementTests {

        @Test
        @DisplayName("P-256密钥协商产生相同共享密钥")
        void testKeyAgreementP256() {
            EcdhEngine alice = EcdhEngine.p256();
            EcdhEngine bob = EcdhEngine.p256();

            KeyPair aliceKeyPair = alice.generateKeyPair();
            KeyPair bobKeyPair = bob.generateKeyPair();

            alice.setPrivateKey(aliceKeyPair.getPrivate())
                 .setRemotePublicKey(bobKeyPair.getPublic());
            bob.setPrivateKey(bobKeyPair.getPrivate())
               .setRemotePublicKey(aliceKeyPair.getPublic());

            byte[] aliceSecret = alice.computeSharedSecret();
            byte[] bobSecret = bob.computeSharedSecret();

            assertThat(aliceSecret).isEqualTo(bobSecret);
            assertThat(aliceSecret.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("P-384密钥协商产生相同共享密钥")
        void testKeyAgreementP384() {
            EcdhEngine alice = EcdhEngine.p384();
            EcdhEngine bob = EcdhEngine.p384();

            KeyPair aliceKeyPair = alice.generateKeyPair();
            KeyPair bobKeyPair = bob.generateKeyPair();

            alice.setPrivateKey(aliceKeyPair.getPrivate())
                 .setRemotePublicKey(bobKeyPair.getPublic());
            bob.setPrivateKey(bobKeyPair.getPrivate())
               .setRemotePublicKey(aliceKeyPair.getPublic());

            byte[] aliceSecret = alice.computeSharedSecret();
            byte[] bobSecret = bob.computeSharedSecret();

            assertThat(aliceSecret).isEqualTo(bobSecret);
        }

        @Test
        @DisplayName("P-521密钥协商产生相同共享密钥")
        void testKeyAgreementP521() {
            EcdhEngine alice = EcdhEngine.p521();
            EcdhEngine bob = EcdhEngine.p521();

            KeyPair aliceKeyPair = alice.generateKeyPair();
            KeyPair bobKeyPair = bob.generateKeyPair();

            alice.setPrivateKey(aliceKeyPair.getPrivate())
                 .setRemotePublicKey(bobKeyPair.getPublic());
            bob.setPrivateKey(bobKeyPair.getPrivate())
               .setRemotePublicKey(aliceKeyPair.getPublic());

            byte[] aliceSecret = alice.computeSharedSecret();
            byte[] bobSecret = bob.computeSharedSecret();

            assertThat(aliceSecret).isEqualTo(bobSecret);
        }

        @Test
        @DisplayName("agree()静态方法进行密钥协商")
        void testAgreeStaticMethod() {
            EcdhEngine engine = EcdhEngine.p256();
            KeyPair aliceKeyPair = engine.generateKeyPair();
            KeyPair bobKeyPair = engine.generateKeyPair();

            byte[] aliceSecret = EcdhEngine.agree(aliceKeyPair.getPrivate(), bobKeyPair.getPublic());
            byte[] bobSecret = EcdhEngine.agree(bobKeyPair.getPrivate(), aliceKeyPair.getPublic());

            assertThat(aliceSecret).isEqualTo(bobSecret);
        }

        @Test
        @DisplayName("agree()使用null密钥抛出异常")
        void testAgreeNullKeysThrows() {
            EcdhEngine engine = EcdhEngine.p256();
            KeyPair keyPair = engine.generateKeyPair();

            assertThatThrownBy(() -> EcdhEngine.agree(null, keyPair.getPublic()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");

            assertThatThrownBy(() -> EcdhEngine.agree(keyPair.getPrivate(), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("agreeAndDerive()静态方法派生密钥")
        void testAgreeAndDeriveStaticMethod() {
            EcdhEngine engine = EcdhEngine.p256();
            KeyPair aliceKeyPair = engine.generateKeyPair();
            KeyPair bobKeyPair = engine.generateKeyPair();

            byte[] info = "test context".getBytes();
            byte[] aliceDerivedKey = EcdhEngine.agreeAndDerive(
                    aliceKeyPair.getPrivate(), bobKeyPair.getPublic(), info, 32);
            byte[] bobDerivedKey = EcdhEngine.agreeAndDerive(
                    bobKeyPair.getPrivate(), aliceKeyPair.getPublic(), info, 32);

            assertThat(aliceDerivedKey).isEqualTo(bobDerivedKey);
            assertThat(aliceDerivedKey).hasSize(32);
        }

        @Test
        @DisplayName("不同密钥对产生不同共享密钥")
        void testDifferentKeyPairsProduceDifferentSecrets() {
            EcdhEngine engine = EcdhEngine.p256();
            KeyPair keyPair1 = engine.generateKeyPair();
            KeyPair keyPair2 = engine.generateKeyPair();
            KeyPair keyPair3 = engine.generateKeyPair();

            byte[] secret12 = EcdhEngine.agree(keyPair1.getPrivate(), keyPair2.getPublic());
            byte[] secret13 = EcdhEngine.agree(keyPair1.getPrivate(), keyPair3.getPublic());

            assertThat(secret12).isNotEqualTo(secret13);
        }
    }

    @Nested
    @DisplayName("Key Derivation Tests / 密钥派生测试")
    class KeyDerivationTests {

        @Test
        @DisplayName("deriveKey()派生指定长度密钥")
        void testDeriveKey() {
            EcdhEngine alice = EcdhEngine.p256();
            EcdhEngine bob = EcdhEngine.p256();

            KeyPair aliceKeyPair = alice.generateKeyPair();
            KeyPair bobKeyPair = bob.generateKeyPair();

            alice.setPrivateKey(aliceKeyPair.getPrivate())
                 .setRemotePublicKey(bobKeyPair.getPublic());
            bob.setPrivateKey(bobKeyPair.getPrivate())
               .setRemotePublicKey(aliceKeyPair.getPublic());

            byte[] info = "encryption key".getBytes();
            byte[] aliceKey = alice.deriveKey(info, 32);
            byte[] bobKey = bob.deriveKey(info, 32);

            assertThat(aliceKey).isEqualTo(bobKey);
            assertThat(aliceKey).hasSize(32);
        }

        @Test
        @DisplayName("deriveKey()使用null info")
        void testDeriveKeyWithNullInfo() {
            EcdhEngine alice = EcdhEngine.p256();
            EcdhEngine bob = EcdhEngine.p256();

            KeyPair aliceKeyPair = alice.generateKeyPair();
            KeyPair bobKeyPair = bob.generateKeyPair();

            alice.setPrivateKey(aliceKeyPair.getPrivate())
                 .setRemotePublicKey(bobKeyPair.getPublic());
            bob.setPrivateKey(bobKeyPair.getPrivate())
               .setRemotePublicKey(aliceKeyPair.getPublic());

            byte[] aliceKey = alice.deriveKey(null, 32);
            byte[] bobKey = bob.deriveKey(null, 32);

            assertThat(aliceKey).isEqualTo(bobKey);
        }

        @Test
        @DisplayName("不同info派生不同密钥")
        void testDifferentInfoProducesDifferentKeys() {
            EcdhEngine alice = EcdhEngine.p256();
            KeyPair aliceKeyPair = alice.generateKeyPair();
            KeyPair bobKeyPair = alice.generateKeyPair();

            alice.setPrivateKey(aliceKeyPair.getPrivate())
                 .setRemotePublicKey(bobKeyPair.getPublic());

            byte[] key1 = alice.deriveKey("context1".getBytes(), 32);
            byte[] key2 = alice.deriveKey("context2".getBytes(), 32);

            assertThat(key1).isNotEqualTo(key2);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests / 错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("setPrivateKey(null)抛出异常")
        void testSetPrivateKeyNullThrows() {
            EcdhEngine engine = EcdhEngine.p256();

            assertThatThrownBy(() -> engine.setPrivateKey(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Private key cannot be null");
        }

        @Test
        @DisplayName("setRemotePublicKey(null)抛出异常")
        void testSetRemotePublicKeyNullThrows() {
            EcdhEngine engine = EcdhEngine.p256();

            assertThatThrownBy(() -> engine.setRemotePublicKey(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Public key cannot be null");
        }

        @Test
        @DisplayName("未设置私钥时computeSharedSecret抛出异常")
        void testComputeSharedSecretWithoutPrivateKeyThrows() {
            EcdhEngine engine = EcdhEngine.p256();
            KeyPair keyPair = engine.generateKeyPair();
            engine.setRemotePublicKey(keyPair.getPublic());

            assertThatThrownBy(engine::computeSharedSecret)
                    .isInstanceOf(OpenKeyException.class)
                    .hasMessageContaining("private key");
        }

        @Test
        @DisplayName("未设置远程公钥时computeSharedSecret抛出异常")
        void testComputeSharedSecretWithoutRemotePublicKeyThrows() {
            EcdhEngine engine = EcdhEngine.p256();
            KeyPair keyPair = engine.generateKeyPair();
            engine.setPrivateKey(keyPair.getPrivate());

            assertThatThrownBy(engine::computeSharedSecret)
                    .isInstanceOf(OpenKeyException.class)
                    .hasMessageContaining("remote public key");
        }

        @Test
        @DisplayName("设置错误类型私钥抛出异常")
        void testSetWrongTypePrivateKeyThrows() throws Exception {
            EcdhEngine engine = EcdhEngine.p256();

            // Generate RSA key pair (wrong type)
            KeyPairGenerator rsaGen = KeyPairGenerator.getInstance("RSA");
            rsaGen.initialize(2048);
            KeyPair rsaKeyPair = rsaGen.generateKeyPair();

            assertThatThrownBy(() -> engine.setPrivateKey(rsaKeyPair.getPrivate()))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("设置错误类型公钥抛出异常")
        void testSetWrongTypePublicKeyThrows() throws Exception {
            EcdhEngine engine = EcdhEngine.p256();

            // Generate RSA key pair (wrong type)
            KeyPairGenerator rsaGen = KeyPairGenerator.getInstance("RSA");
            rsaGen.initialize(2048);
            KeyPair rsaKeyPair = rsaGen.generateKeyPair();

            assertThatThrownBy(() -> engine.setRemotePublicKey(rsaKeyPair.getPublic()))
                    .isInstanceOf(OpenKeyException.class);
        }
    }

    @Nested
    @DisplayName("Fluent API Tests / 流式API测试")
    class FluentApiTests {

        @Test
        @DisplayName("链式调用返回同一实例")
        void testChainedSettersReturnSameInstance() {
            EcdhEngine engine = EcdhEngine.p256();
            KeyPair keyPair = engine.generateKeyPair();

            EcdhEngine result = engine
                    .setPrivateKey(keyPair.getPrivate())
                    .setRemotePublicKey(keyPair.getPublic());

            assertThat(result).isSameAs(engine);
        }
    }

    @Nested
    @DisplayName("Getter Tests / 获取器测试")
    class GetterTests {

        @Test
        @DisplayName("getCurve()返回正确曲线类型")
        void testGetCurve() {
            assertThat(EcdhEngine.p256().getCurve()).isEqualTo(CurveType.P_256);
            assertThat(EcdhEngine.p384().getCurve()).isEqualTo(CurveType.P_384);
            assertThat(EcdhEngine.p521().getCurve()).isEqualTo(CurveType.P_521);
            assertThat(EcdhEngine.withCurve(CurveType.SECP256K1).getCurve())
                    .isEqualTo(CurveType.SECP256K1);
        }

        @Test
        @DisplayName("getAlgorithm()返回包含曲线信息的算法名")
        void testGetAlgorithm() {
            EcdhEngine engine = EcdhEngine.p256();
            String algorithm = engine.getAlgorithm();

            assertThat(algorithm).contains("ECDH");
            assertThat(algorithm).contains("P_256");
        }
    }
}
