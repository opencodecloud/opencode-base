package cloud.opencode.base.crypto.keyexchange;

import cloud.opencode.base.crypto.exception.OpenKeyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link X25519Engine}.
 * X25519Engine单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("X25519Engine Tests / X25519Engine测试")
class X25519EngineTest {

    @Nested
    @DisplayName("Factory Method Tests / 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create()创建X25519引擎")
        void testCreateFactory() {
            X25519Engine engine = X25519Engine.create();
            assertThat(engine).isNotNull();
            assertThat(engine.getAlgorithm()).isEqualTo("X25519");
        }

        @Test
        @DisplayName("withGeneratedKeyPair()创建带密钥对的引擎")
        void testWithGeneratedKeyPairFactory() {
            X25519Engine engine = X25519Engine.withGeneratedKeyPair();
            assertThat(engine).isNotNull();
            assertThat(engine.getPrivateKey()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Key Generation Tests / 密钥生成测试")
    class KeyGenerationTests {

        @Test
        @DisplayName("generateKeyPair()生成X25519密钥对")
        void testGenerateKeyPair() {
            X25519Engine engine = X25519Engine.create();
            KeyPair keyPair = engine.generateKeyPair();

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
            // X25519 keys may report as "X25519" or "XDH"
            assertThat(keyPair.getPublic().getAlgorithm())
                    .satisfiesAnyOf(
                            alg -> assertThat(alg).isEqualTo("X25519"),
                            alg -> assertThat(alg).isEqualTo("XDH")
                    );
        }

        @Test
        @DisplayName("generateKeyPair()每次生成不同密钥对")
        void testGenerateKeyPairUnique() {
            X25519Engine engine = X25519Engine.create();
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
        @DisplayName("X25519密钥协商产生相同共享密钥")
        void testKeyAgreement() {
            X25519Engine alice = X25519Engine.create();
            X25519Engine bob = X25519Engine.create();

            KeyPair aliceKeyPair = alice.generateKeyPair();
            KeyPair bobKeyPair = bob.generateKeyPair();

            alice.setPrivateKey(aliceKeyPair.getPrivate())
                 .setRemotePublicKey(bobKeyPair.getPublic());
            bob.setPrivateKey(bobKeyPair.getPrivate())
               .setRemotePublicKey(aliceKeyPair.getPublic());

            byte[] aliceSecret = alice.computeSharedSecret();
            byte[] bobSecret = bob.computeSharedSecret();

            assertThat(aliceSecret).isEqualTo(bobSecret);
            assertThat(aliceSecret).hasSize(32); // X25519 produces 32-byte secrets
        }

        @Test
        @DisplayName("agree()静态方法进行密钥协商")
        void testAgreeStaticMethod() {
            X25519Engine engine = X25519Engine.create();
            KeyPair aliceKeyPair = engine.generateKeyPair();
            KeyPair bobKeyPair = engine.generateKeyPair();

            byte[] aliceSecret = X25519Engine.agree(aliceKeyPair.getPrivate(), bobKeyPair.getPublic());
            byte[] bobSecret = X25519Engine.agree(bobKeyPair.getPrivate(), aliceKeyPair.getPublic());

            assertThat(aliceSecret).isEqualTo(bobSecret);
            assertThat(aliceSecret).hasSize(32);
        }

        @Test
        @DisplayName("agree()使用null密钥抛出异常")
        void testAgreeNullKeysThrows() {
            X25519Engine engine = X25519Engine.create();
            KeyPair keyPair = engine.generateKeyPair();

            assertThatThrownBy(() -> X25519Engine.agree(null, keyPair.getPublic()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");

            assertThatThrownBy(() -> X25519Engine.agree(keyPair.getPrivate(), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("agreeAndDerive()静态方法派生密钥")
        void testAgreeAndDeriveStaticMethod() {
            X25519Engine engine = X25519Engine.create();
            KeyPair aliceKeyPair = engine.generateKeyPair();
            KeyPair bobKeyPair = engine.generateKeyPair();

            byte[] info = "test context".getBytes();
            byte[] aliceDerivedKey = X25519Engine.agreeAndDerive(
                    aliceKeyPair.getPrivate(), bobKeyPair.getPublic(), info, 32);
            byte[] bobDerivedKey = X25519Engine.agreeAndDerive(
                    bobKeyPair.getPrivate(), aliceKeyPair.getPublic(), info, 32);

            assertThat(aliceDerivedKey).isEqualTo(bobDerivedKey);
            assertThat(aliceDerivedKey).hasSize(32);
        }

        @Test
        @DisplayName("不同密钥对产生不同共享密钥")
        void testDifferentKeyPairsProduceDifferentSecrets() {
            X25519Engine engine = X25519Engine.create();
            KeyPair keyPair1 = engine.generateKeyPair();
            KeyPair keyPair2 = engine.generateKeyPair();
            KeyPair keyPair3 = engine.generateKeyPair();

            byte[] secret12 = X25519Engine.agree(keyPair1.getPrivate(), keyPair2.getPublic());
            byte[] secret13 = X25519Engine.agree(keyPair1.getPrivate(), keyPair3.getPublic());

            assertThat(secret12).isNotEqualTo(secret13);
        }
    }

    @Nested
    @DisplayName("Key Derivation Tests / 密钥派生测试")
    class KeyDerivationTests {

        @Test
        @DisplayName("deriveKey()派生指定长度密钥")
        void testDeriveKey() {
            X25519Engine alice = X25519Engine.create();
            X25519Engine bob = X25519Engine.create();

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
            X25519Engine alice = X25519Engine.create();
            X25519Engine bob = X25519Engine.create();

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
        @DisplayName("deriveKey()派生不同长度密钥")
        void testDeriveKeyDifferentLengths() {
            X25519Engine alice = X25519Engine.create();
            KeyPair aliceKeyPair = alice.generateKeyPair();
            KeyPair bobKeyPair = alice.generateKeyPair();

            alice.setPrivateKey(aliceKeyPair.getPrivate())
                 .setRemotePublicKey(bobKeyPair.getPublic());

            byte[] key16 = alice.deriveKey("test".getBytes(), 16);
            byte[] key32 = alice.deriveKey("test".getBytes(), 32);
            byte[] key64 = alice.deriveKey("test".getBytes(), 64);

            assertThat(key16).hasSize(16);
            assertThat(key32).hasSize(32);
            assertThat(key64).hasSize(64);
        }

        @Test
        @DisplayName("不同info派生不同密钥")
        void testDifferentInfoProducesDifferentKeys() {
            X25519Engine alice = X25519Engine.create();
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
            X25519Engine engine = X25519Engine.create();

            assertThatThrownBy(() -> engine.setPrivateKey(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Private key cannot be null");
        }

        @Test
        @DisplayName("setRemotePublicKey(null)抛出异常")
        void testSetRemotePublicKeyNullThrows() {
            X25519Engine engine = X25519Engine.create();

            assertThatThrownBy(() -> engine.setRemotePublicKey(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Public key cannot be null");
        }

        @Test
        @DisplayName("未设置私钥时computeSharedSecret抛出异常")
        void testComputeSharedSecretWithoutPrivateKeyThrows() {
            X25519Engine engine = X25519Engine.create();
            KeyPair keyPair = engine.generateKeyPair();
            engine.setRemotePublicKey(keyPair.getPublic());

            assertThatThrownBy(engine::computeSharedSecret)
                    .isInstanceOf(OpenKeyException.class)
                    .hasMessageContaining("private key");
        }

        @Test
        @DisplayName("未设置远程公钥时computeSharedSecret抛出异常")
        void testComputeSharedSecretWithoutRemotePublicKeyThrows() {
            X25519Engine engine = X25519Engine.create();
            KeyPair keyPair = engine.generateKeyPair();
            engine.setPrivateKey(keyPair.getPrivate());

            assertThatThrownBy(engine::computeSharedSecret)
                    .isInstanceOf(OpenKeyException.class)
                    .hasMessageContaining("remote public key");
        }

        @Test
        @DisplayName("设置错误类型私钥抛出异常")
        void testSetWrongTypePrivateKeyThrows() throws Exception {
            X25519Engine engine = X25519Engine.create();

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
            X25519Engine engine = X25519Engine.create();

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
            X25519Engine engine = X25519Engine.create();
            KeyPair keyPair = engine.generateKeyPair();

            X25519Engine result = engine
                    .setPrivateKey(keyPair.getPrivate())
                    .setRemotePublicKey(keyPair.getPublic());

            assertThat(result).isSameAs(engine);
        }

        @Test
        @DisplayName("完整流式调用链")
        void testCompleteFluentChain() {
            X25519Engine alice = X25519Engine.create();
            X25519Engine bob = X25519Engine.create();

            KeyPair aliceKeyPair = alice.generateKeyPair();
            KeyPair bobKeyPair = bob.generateKeyPair();

            byte[] sharedSecret = alice
                    .setPrivateKey(aliceKeyPair.getPrivate())
                    .setRemotePublicKey(bobKeyPair.getPublic())
                    .computeSharedSecret();

            assertThat(sharedSecret).isNotNull();
            assertThat(sharedSecret).hasSize(32);
        }
    }

    @Nested
    @DisplayName("Getter Tests / 获取器测试")
    class GetterTests {

        @Test
        @DisplayName("getAlgorithm()返回X25519")
        void testGetAlgorithm() {
            X25519Engine engine = X25519Engine.create();
            assertThat(engine.getAlgorithm()).isEqualTo("X25519");
        }

        @Test
        @DisplayName("getPrivateKey()返回已设置的私钥")
        void testGetPrivateKey() {
            X25519Engine engine = X25519Engine.create();
            assertThat(engine.getPrivateKey()).isNull();

            KeyPair keyPair = engine.generateKeyPair();
            engine.setPrivateKey(keyPair.getPrivate());

            assertThat(engine.getPrivateKey()).isEqualTo(keyPair.getPrivate());
        }

        @Test
        @DisplayName("getPublicKey() throws UnsupportedOperationException")
        void testGetPublicKeyThrowsUnsupportedOperation() {
            X25519Engine engine = X25519Engine.create();
            assertThatThrownBy(engine::getPublicKey)
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("use generateKeyPair()");

            // Even after setting private key, getPublicKey still throws
            // (X25519 does not support deriving public key from private key via JCA)
            KeyPair keyPair = engine.generateKeyPair();
            engine.setPrivateKey(keyPair.getPrivate());
            assertThatThrownBy(engine::getPublicKey)
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("withGeneratedKeyPair()后getPrivateKey()返回非null")
        void testWithGeneratedKeyPairGetPrivateKey() {
            X25519Engine engine = X25519Engine.withGeneratedKeyPair();
            assertThat(engine.getPrivateKey()).isNotNull();
        }
    }
}
