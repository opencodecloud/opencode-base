package cloud.opencode.base.crypto.keyexchange;

import cloud.opencode.base.crypto.enums.CurveType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link KeyExchangeEngine} interface.
 * KeyExchangeEngine接口单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("KeyExchangeEngine Interface Tests / KeyExchangeEngine接口测试")
class KeyExchangeEngineTest {

    @Nested
    @DisplayName("Interface Contract Tests / 接口契约测试")
    class InterfaceContractTests {

        @Test
        @DisplayName("EcdhEngine实现KeyExchangeEngine接口")
        void testEcdhEngineImplementsInterface() {
            KeyExchangeEngine engine = EcdhEngine.p256();
            assertThat(engine).isInstanceOf(KeyExchangeEngine.class);
        }

        @Test
        @DisplayName("X25519Engine实现KeyExchangeEngine接口")
        void testX25519EngineImplementsInterface() {
            KeyExchangeEngine engine = X25519Engine.create();
            assertThat(engine).isInstanceOf(KeyExchangeEngine.class);
        }

        @Test
        @DisplayName("X448Engine实现KeyExchangeEngine接口")
        void testX448EngineImplementsInterface() {
            KeyExchangeEngine engine = X448Engine.create();
            assertThat(engine).isInstanceOf(KeyExchangeEngine.class);
        }

        @Test
        @DisplayName("通过接口调用generateKeyPair方法")
        void testGenerateKeyPairThroughInterface() {
            KeyExchangeEngine engine = EcdhEngine.p256();
            KeyPair keyPair = engine.generateKeyPair();
            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
        }

        @Test
        @DisplayName("通过接口调用setPrivateKey方法")
        void testSetPrivateKeyThroughInterface() {
            KeyExchangeEngine engine = EcdhEngine.p256();
            KeyPair keyPair = engine.generateKeyPair();
            KeyExchangeEngine result = engine.setPrivateKey(keyPair.getPrivate());
            assertThat(result).isSameAs(engine);
        }

        @Test
        @DisplayName("通过接口调用setRemotePublicKey方法")
        void testSetRemotePublicKeyThroughInterface() {
            KeyExchangeEngine engine = EcdhEngine.p256();
            KeyPair keyPair = engine.generateKeyPair();
            KeyExchangeEngine result = engine.setRemotePublicKey(keyPair.getPublic());
            assertThat(result).isSameAs(engine);
        }

        @Test
        @DisplayName("通过接口调用computeSharedSecret方法")
        void testComputeSharedSecretThroughInterface() {
            // Alice
            KeyExchangeEngine alice = EcdhEngine.p256();
            KeyPair aliceKeyPair = alice.generateKeyPair();

            // Bob
            KeyExchangeEngine bob = EcdhEngine.p256();
            KeyPair bobKeyPair = bob.generateKeyPair();

            // Alice computes shared secret
            alice.setPrivateKey(aliceKeyPair.getPrivate())
                 .setRemotePublicKey(bobKeyPair.getPublic());
            byte[] aliceSecret = alice.computeSharedSecret();

            // Bob computes shared secret
            bob.setPrivateKey(bobKeyPair.getPrivate())
               .setRemotePublicKey(aliceKeyPair.getPublic());
            byte[] bobSecret = bob.computeSharedSecret();

            assertThat(aliceSecret).isEqualTo(bobSecret);
        }

        @Test
        @DisplayName("通过接口调用deriveKey方法")
        void testDeriveKeyThroughInterface() {
            // Alice
            KeyExchangeEngine alice = EcdhEngine.p256();
            KeyPair aliceKeyPair = alice.generateKeyPair();

            // Bob
            KeyExchangeEngine bob = EcdhEngine.p256();
            KeyPair bobKeyPair = bob.generateKeyPair();

            // Setup keys
            alice.setPrivateKey(aliceKeyPair.getPrivate())
                 .setRemotePublicKey(bobKeyPair.getPublic());
            bob.setPrivateKey(bobKeyPair.getPrivate())
               .setRemotePublicKey(aliceKeyPair.getPublic());

            // Derive keys
            byte[] info = "test context".getBytes();
            byte[] aliceKey = alice.deriveKey(info, 32);
            byte[] bobKey = bob.deriveKey(info, 32);

            assertThat(aliceKey).isEqualTo(bobKey);
            assertThat(aliceKey).hasSize(32);
        }

        @Test
        @DisplayName("通过接口调用getAlgorithm方法")
        void testGetAlgorithmThroughInterface() {
            KeyExchangeEngine ecdhEngine = EcdhEngine.p256();
            KeyExchangeEngine x25519Engine = X25519Engine.create();
            KeyExchangeEngine x448Engine = X448Engine.create();

            assertThat(ecdhEngine.getAlgorithm()).contains("ECDH");
            assertThat(x25519Engine.getAlgorithm()).isEqualTo("X25519");
            assertThat(x448Engine.getAlgorithm()).isEqualTo("X448");
        }
    }

    @Nested
    @DisplayName("Polymorphism Tests / 多态测试")
    class PolymorphismTests {

        @Test
        @DisplayName("不同实现通过接口引用")
        void testDifferentImplementations() {
            KeyExchangeEngine[] engines = {
                    EcdhEngine.p256(),
                    EcdhEngine.p384(),
                    X25519Engine.create(),
                    X448Engine.create()
            };

            for (KeyExchangeEngine engine : engines) {
                KeyPair keyPair = engine.generateKeyPair();
                assertThat(keyPair).isNotNull();
                assertThat(engine.getAlgorithm()).isNotNull();
            }
        }

        @Test
        @DisplayName("通过接口数组批量密钥协商")
        void testBatchKeyExchange() {
            // Create different engine pairs
            KeyExchangeEngine[] aliceEngines = {
                    EcdhEngine.p256(),
                    X25519Engine.create()
            };

            KeyExchangeEngine[] bobEngines = {
                    EcdhEngine.p256(),
                    X25519Engine.create()
            };

            for (int i = 0; i < aliceEngines.length; i++) {
                KeyExchangeEngine alice = aliceEngines[i];
                KeyExchangeEngine bob = bobEngines[i];

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
        }
    }

    @Nested
    @DisplayName("Fluent API Tests / 流式API测试")
    class FluentApiTests {

        @Test
        @DisplayName("链式调用设置方法")
        void testChainedSetters() {
            KeyExchangeEngine alice = EcdhEngine.p256();
            KeyExchangeEngine bob = EcdhEngine.p256();

            KeyPair aliceKeyPair = alice.generateKeyPair();
            KeyPair bobKeyPair = bob.generateKeyPair();

            // Chained setters
            byte[] sharedSecret = alice
                    .setPrivateKey(aliceKeyPair.getPrivate())
                    .setRemotePublicKey(bobKeyPair.getPublic())
                    .computeSharedSecret();

            assertThat(sharedSecret).isNotNull();
            assertThat(sharedSecret.length).isGreaterThan(0);
        }
    }
}
