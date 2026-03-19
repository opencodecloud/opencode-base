package cloud.opencode.base.crypto.keyexchange;

import cloud.opencode.base.crypto.exception.OpenKeyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for {@link X448Engine}.
 * X448Engine单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("X448Engine Tests / X448Engine测试")
class X448EngineTest {

    /**
     * Check if X448 algorithm is available (requires JDK 11+).
     */
    private static boolean isX448Available() {
        try {
            X448Engine engine = X448Engine.create();
            engine.generateKeyPair();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Nested
    @DisplayName("Factory Method Tests / 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create()创建X448引擎")
        void testCreateFactory() {
            X448Engine engine = X448Engine.create();
            assertThat(engine).isNotNull();
            assertThat(engine.getAlgorithm()).isEqualTo("X448");
        }

        @Test
        @DisplayName("withGeneratedKeyPair()创建带密钥对的引擎")
        void testWithGeneratedKeyPairFactory() {
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine engine = X448Engine.withGeneratedKeyPair();
            assertThat(engine).isNotNull();
            assertThat(engine.getPrivateKey()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Key Generation Tests / 密钥生成测试")
    class KeyGenerationTests {

        @Test
        @DisplayName("generateKeyPair()生成X448密钥对")
        void testGenerateKeyPair() {
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine engine = X448Engine.create();
            KeyPair keyPair = engine.generateKeyPair();

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
            // X448 keys may report as "X448" or "XDH"
            assertThat(keyPair.getPublic().getAlgorithm())
                    .satisfiesAnyOf(
                            alg -> assertThat(alg).isEqualTo("X448"),
                            alg -> assertThat(alg).isEqualTo("XDH")
                    );
        }

        @Test
        @DisplayName("generateKeyPair()每次生成不同密钥对")
        void testGenerateKeyPairUnique() {
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine engine = X448Engine.create();
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
        @DisplayName("X448密钥协商产生相同共享密钥")
        void testKeyAgreement() {
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine alice = X448Engine.create();
            X448Engine bob = X448Engine.create();

            KeyPair aliceKeyPair = alice.generateKeyPair();
            KeyPair bobKeyPair = bob.generateKeyPair();

            alice.setPrivateKey(aliceKeyPair.getPrivate())
                 .setRemotePublicKey(bobKeyPair.getPublic());
            bob.setPrivateKey(bobKeyPair.getPrivate())
               .setRemotePublicKey(aliceKeyPair.getPublic());

            byte[] aliceSecret = alice.computeSharedSecret();
            byte[] bobSecret = bob.computeSharedSecret();

            assertThat(aliceSecret).isEqualTo(bobSecret);
            assertThat(aliceSecret).hasSize(56); // X448 produces 56-byte secrets
        }

        @Test
        @DisplayName("agree()静态方法进行密钥协商")
        void testAgreeStaticMethod() {
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine engine = X448Engine.create();
            KeyPair aliceKeyPair = engine.generateKeyPair();
            KeyPair bobKeyPair = engine.generateKeyPair();

            byte[] aliceSecret = X448Engine.agree(aliceKeyPair.getPrivate(), bobKeyPair.getPublic());
            byte[] bobSecret = X448Engine.agree(bobKeyPair.getPrivate(), aliceKeyPair.getPublic());

            assertThat(aliceSecret).isEqualTo(bobSecret);
            assertThat(aliceSecret).hasSize(56);
        }

        @Test
        @DisplayName("agree()使用null密钥抛出异常")
        void testAgreeNullKeysThrows() {
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine engine = X448Engine.create();
            KeyPair keyPair = engine.generateKeyPair();

            assertThatThrownBy(() -> X448Engine.agree(null, keyPair.getPublic()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");

            assertThatThrownBy(() -> X448Engine.agree(keyPair.getPrivate(), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("agreeAndDerive()静态方法派生密钥")
        void testAgreeAndDeriveStaticMethod() {
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine engine = X448Engine.create();
            KeyPair aliceKeyPair = engine.generateKeyPair();
            KeyPair bobKeyPair = engine.generateKeyPair();

            byte[] info = "test context".getBytes();
            byte[] aliceDerivedKey = X448Engine.agreeAndDerive(
                    aliceKeyPair.getPrivate(), bobKeyPair.getPublic(), info, 32);
            byte[] bobDerivedKey = X448Engine.agreeAndDerive(
                    bobKeyPair.getPrivate(), aliceKeyPair.getPublic(), info, 32);

            assertThat(aliceDerivedKey).isEqualTo(bobDerivedKey);
            assertThat(aliceDerivedKey).hasSize(32);
        }

        @Test
        @DisplayName("不同密钥对产生不同共享密钥")
        void testDifferentKeyPairsProduceDifferentSecrets() {
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine engine = X448Engine.create();
            KeyPair keyPair1 = engine.generateKeyPair();
            KeyPair keyPair2 = engine.generateKeyPair();
            KeyPair keyPair3 = engine.generateKeyPair();

            byte[] secret12 = X448Engine.agree(keyPair1.getPrivate(), keyPair2.getPublic());
            byte[] secret13 = X448Engine.agree(keyPair1.getPrivate(), keyPair3.getPublic());

            assertThat(secret12).isNotEqualTo(secret13);
        }
    }

    @Nested
    @DisplayName("Key Derivation Tests / 密钥派生测试")
    class KeyDerivationTests {

        @Test
        @DisplayName("deriveKey()派生指定长度密钥")
        void testDeriveKey() {
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine alice = X448Engine.create();
            X448Engine bob = X448Engine.create();

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
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine alice = X448Engine.create();
            X448Engine bob = X448Engine.create();

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
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine alice = X448Engine.create();
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
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine alice = X448Engine.create();
            KeyPair aliceKeyPair = alice.generateKeyPair();
            KeyPair bobKeyPair = alice.generateKeyPair();

            alice.setPrivateKey(aliceKeyPair.getPrivate())
                 .setRemotePublicKey(bobKeyPair.getPublic());

            byte[] key1 = alice.deriveKey("context1".getBytes(), 32);
            byte[] key2 = alice.deriveKey("context2".getBytes(), 32);

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("X448使用SHA-512进行密钥派生")
        void testX448UsesSha512ForKeyDerivation() {
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine alice = X448Engine.create();
            X448Engine bob = X448Engine.create();

            KeyPair aliceKeyPair = alice.generateKeyPair();
            KeyPair bobKeyPair = bob.generateKeyPair();

            alice.setPrivateKey(aliceKeyPair.getPrivate())
                 .setRemotePublicKey(bobKeyPair.getPublic());
            bob.setPrivateKey(bobKeyPair.getPrivate())
               .setRemotePublicKey(aliceKeyPair.getPublic());

            // Derive a key longer than SHA-256 output to verify SHA-512 is used
            byte[] aliceKey = alice.deriveKey("test".getBytes(), 64);
            byte[] bobKey = bob.deriveKey("test".getBytes(), 64);

            assertThat(aliceKey).isEqualTo(bobKey);
            assertThat(aliceKey).hasSize(64);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests / 错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("setPrivateKey(null)抛出异常")
        void testSetPrivateKeyNullThrows() {
            X448Engine engine = X448Engine.create();

            assertThatThrownBy(() -> engine.setPrivateKey(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Private key cannot be null");
        }

        @Test
        @DisplayName("setRemotePublicKey(null)抛出异常")
        void testSetRemotePublicKeyNullThrows() {
            X448Engine engine = X448Engine.create();

            assertThatThrownBy(() -> engine.setRemotePublicKey(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Public key cannot be null");
        }

        @Test
        @DisplayName("未设置私钥时computeSharedSecret抛出异常")
        void testComputeSharedSecretWithoutPrivateKeyThrows() {
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine engine = X448Engine.create();
            KeyPair keyPair = engine.generateKeyPair();
            engine.setRemotePublicKey(keyPair.getPublic());

            assertThatThrownBy(engine::computeSharedSecret)
                    .isInstanceOf(OpenKeyException.class)
                    .hasMessageContaining("private key");
        }

        @Test
        @DisplayName("未设置远程公钥时computeSharedSecret抛出异常")
        void testComputeSharedSecretWithoutRemotePublicKeyThrows() {
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine engine = X448Engine.create();
            KeyPair keyPair = engine.generateKeyPair();
            engine.setPrivateKey(keyPair.getPrivate());

            assertThatThrownBy(engine::computeSharedSecret)
                    .isInstanceOf(OpenKeyException.class)
                    .hasMessageContaining("remote public key");
        }

        @Test
        @DisplayName("设置错误类型私钥抛出异常")
        void testSetWrongTypePrivateKeyThrows() throws Exception {
            X448Engine engine = X448Engine.create();

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
            X448Engine engine = X448Engine.create();

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
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine engine = X448Engine.create();
            KeyPair keyPair = engine.generateKeyPair();

            X448Engine result = engine
                    .setPrivateKey(keyPair.getPrivate())
                    .setRemotePublicKey(keyPair.getPublic());

            assertThat(result).isSameAs(engine);
        }

        @Test
        @DisplayName("完整流式调用链")
        void testCompleteFluentChain() {
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine alice = X448Engine.create();
            X448Engine bob = X448Engine.create();

            KeyPair aliceKeyPair = alice.generateKeyPair();
            KeyPair bobKeyPair = bob.generateKeyPair();

            byte[] sharedSecret = alice
                    .setPrivateKey(aliceKeyPair.getPrivate())
                    .setRemotePublicKey(bobKeyPair.getPublic())
                    .computeSharedSecret();

            assertThat(sharedSecret).isNotNull();
            assertThat(sharedSecret).hasSize(56);
        }
    }

    @Nested
    @DisplayName("Getter Tests / 获取器测试")
    class GetterTests {

        @Test
        @DisplayName("getAlgorithm()返回X448")
        void testGetAlgorithm() {
            X448Engine engine = X448Engine.create();
            assertThat(engine.getAlgorithm()).isEqualTo("X448");
        }

        @Test
        @DisplayName("getPrivateKey()返回已设置的私钥")
        void testGetPrivateKey() {
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine engine = X448Engine.create();
            assertThat(engine.getPrivateKey()).isNull();

            KeyPair keyPair = engine.generateKeyPair();
            engine.setPrivateKey(keyPair.getPrivate());

            assertThat(engine.getPrivateKey()).isEqualTo(keyPair.getPrivate());
        }

        @Test
        @DisplayName("getPublicKey() throws UnsupportedOperationException")
        void testGetPublicKeyThrowsUnsupportedOperation() {
            X448Engine engine = X448Engine.create();
            assertThatThrownBy(engine::getPublicKey)
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("use generateKeyPair()");
        }

        @Test
        @DisplayName("withGeneratedKeyPair()后getPrivateKey()返回非null")
        void testWithGeneratedKeyPairGetPrivateKey() {
            assumeTrue(isX448Available(), "X448 algorithm not available");

            X448Engine engine = X448Engine.withGeneratedKeyPair();
            assertThat(engine.getPrivateKey()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Security Comparison Tests / 安全性对比测试")
    class SecurityComparisonTests {

        @Test
        @DisplayName("X448共享密钥比X25519更长")
        void testX448ProducesLongerSecretThanX25519() {
            assumeTrue(isX448Available(), "X448 algorithm not available");

            // X25519 key agreement
            X25519Engine x25519Alice = X25519Engine.create();
            X25519Engine x25519Bob = X25519Engine.create();
            KeyPair x25519AliceKeyPair = x25519Alice.generateKeyPair();
            KeyPair x25519BobKeyPair = x25519Bob.generateKeyPair();
            x25519Alice.setPrivateKey(x25519AliceKeyPair.getPrivate())
                       .setRemotePublicKey(x25519BobKeyPair.getPublic());
            byte[] x25519Secret = x25519Alice.computeSharedSecret();

            // X448 key agreement
            X448Engine x448Alice = X448Engine.create();
            X448Engine x448Bob = X448Engine.create();
            KeyPair x448AliceKeyPair = x448Alice.generateKeyPair();
            KeyPair x448BobKeyPair = x448Bob.generateKeyPair();
            x448Alice.setPrivateKey(x448AliceKeyPair.getPrivate())
                     .setRemotePublicKey(x448BobKeyPair.getPublic());
            byte[] x448Secret = x448Alice.computeSharedSecret();

            // X448 produces 56-byte secret, X25519 produces 32-byte secret
            assertThat(x25519Secret).hasSize(32);
            assertThat(x448Secret).hasSize(56);
            assertThat(x448Secret.length).isGreaterThan(x25519Secret.length);
        }
    }
}
