package cloud.opencode.base.crypto;

import cloud.opencode.base.crypto.enums.SignatureAlgorithm;
import cloud.opencode.base.crypto.exception.OpenSignatureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for OpenSign
 *
 * @author Leon Soo
 * @since JDK 25, OpenCode-Base-Crypto V1.0.0
 */
@DisplayName("OpenSign Tests")
class OpenSignTest {

    private static final String TEST_DATA = "Hello, OpenCode Crypto!";
    private static final byte[] TEST_DATA_BYTES = TEST_DATA.getBytes(StandardCharsets.UTF_8);

    @Nested
    @DisplayName("Ed25519 Tests")
    class Ed25519Tests {

        @Test
        @DisplayName("Should sign and verify with Ed25519")
        void testEd25519SignAndVerify() {
            OpenSign signer = OpenSign.ed25519().withGeneratedKeyPair();

            byte[] signature = signer.sign(TEST_DATA_BYTES);
            assertNotNull(signature);
            assertTrue(signature.length > 0);

            assertTrue(signer.verify(TEST_DATA_BYTES, signature));
        }

        @Test
        @DisplayName("Should sign and verify string with Ed25519")
        void testEd25519SignAndVerifyString() {
            OpenSign signer = OpenSign.ed25519().withGeneratedKeyPair();

            byte[] signature = signer.sign(TEST_DATA);
            assertTrue(signer.verify(TEST_DATA, signature));
        }

        @Test
        @DisplayName("Should sign to hex and verify from hex")
        void testEd25519HexEncoding() {
            OpenSign signer = OpenSign.ed25519().withGeneratedKeyPair();

            String hexSignature = signer.signHex(TEST_DATA);
            assertNotNull(hexSignature);
            assertTrue(hexSignature.matches("[0-9a-f]+"));

            assertTrue(signer.verifyHex(TEST_DATA, hexSignature));
        }

        @Test
        @DisplayName("Should sign to Base64 and verify from Base64")
        void testEd25519Base64Encoding() {
            OpenSign signer = OpenSign.ed25519().withGeneratedKeyPair();

            String base64Signature = signer.signBase64(TEST_DATA);
            assertNotNull(base64Signature);

            assertTrue(signer.verifyBase64(TEST_DATA, base64Signature));
        }

        @Test
        @DisplayName("Should fail verification with wrong data")
        void testEd25519VerificationFailsWithWrongData() {
            OpenSign signer = OpenSign.ed25519().withGeneratedKeyPair();

            byte[] signature = signer.sign(TEST_DATA);
            assertFalse(signer.verify("Wrong data", signature));
        }

        @Test
        @DisplayName("Should fail verification with wrong key")
        void testEd25519VerificationFailsWithWrongKey() {
            OpenSign signer1 = OpenSign.ed25519().withGeneratedKeyPair();
            OpenSign signer2 = OpenSign.ed25519().withGeneratedKeyPair();

            byte[] signature = signer1.sign(TEST_DATA);

            // Verify with different public key should fail
            assertFalse(signer2.verify(TEST_DATA, signature));
        }
    }

    @Nested
    @DisplayName("ECDSA Tests")
    class EcdsaTests {

        @Test
        @DisplayName("Should sign and verify with ECDSA P-256")
        void testEcdsaP256SignAndVerify() {
            OpenSign signer = OpenSign.ecdsaP256().withGeneratedKeyPair();

            byte[] signature = signer.sign(TEST_DATA_BYTES);
            assertNotNull(signature);

            assertTrue(signer.verify(TEST_DATA_BYTES, signature));
        }

        @Test
        @DisplayName("Should sign and verify with ECDSA P-384")
        void testEcdsaP384SignAndVerify() {
            OpenSign signer = OpenSign.ecdsaP384().withGeneratedKeyPair();

            byte[] signature = signer.sign(TEST_DATA);
            assertTrue(signer.verify(TEST_DATA, signature));
        }

        @Test
        @DisplayName("Should sign and verify with ECDSA P-521")
        void testEcdsaP521SignAndVerify() {
            OpenSign signer = OpenSign.ecdsaP521().withGeneratedKeyPair();

            String hexSignature = signer.signHex(TEST_DATA);
            assertTrue(signer.verifyHex(TEST_DATA, hexSignature));
        }

        @Test
        @DisplayName("ECDSA signature should be different each time (non-deterministic)")
        void testEcdsaNonDeterministic() {
            OpenSign signer = OpenSign.ecdsaP256().withGeneratedKeyPair();

            String sig1 = signer.signHex(TEST_DATA);
            String sig2 = signer.signHex(TEST_DATA);

            // ECDSA uses random k value, so signatures differ
            assertNotEquals(sig1, sig2);
            // But both should verify
            assertTrue(signer.verifyHex(TEST_DATA, sig1));
            assertTrue(signer.verifyHex(TEST_DATA, sig2));
        }
    }

    @Nested
    @DisplayName("RSA Tests")
    class RsaTests {

        @Test
        @DisplayName("Should sign and verify with RSA-SHA256")
        void testRsaSha256SignAndVerify() {
            OpenSign signer = OpenSign.sha256WithRsa().withGeneratedKeyPair();

            byte[] signature = signer.sign(TEST_DATA_BYTES);
            assertNotNull(signature);

            assertTrue(signer.verify(TEST_DATA_BYTES, signature));
        }

        @Test
        @DisplayName("Should sign and verify with RSA-SHA384")
        void testRsaSha384SignAndVerify() {
            OpenSign signer = OpenSign.sha384WithRsa().withGeneratedKeyPair();

            byte[] signature = signer.sign(TEST_DATA);
            assertTrue(signer.verify(TEST_DATA, signature));
        }

        @Test
        @DisplayName("Should sign and verify with RSA-SHA512")
        void testRsaSha512SignAndVerify() {
            OpenSign signer = OpenSign.sha512WithRsa().withGeneratedKeyPair();

            String base64Signature = signer.signBase64(TEST_DATA);
            assertTrue(signer.verifyBase64(TEST_DATA, base64Signature));
        }

        @Test
        @DisplayName("RSA signature should be deterministic")
        void testRsaDeterministic() {
            OpenSign signer = OpenSign.sha256WithRsa().withGeneratedKeyPair();

            String sig1 = signer.signHex(TEST_DATA);
            String sig2 = signer.signHex(TEST_DATA);

            // RSA-PKCS#1 is deterministic
            assertEquals(sig1, sig2);
        }
    }

    @Nested
    @DisplayName("RSA-PSS Tests")
    class RsaPssTests {

        @Test
        @DisplayName("Should sign and verify with RSA-PSS")
        void testRsaPssSignAndVerify() {
            OpenSign signer = OpenSign.rsaPss().withGeneratedKeyPair();

            byte[] signature = signer.sign(TEST_DATA_BYTES);
            assertNotNull(signature);

            assertTrue(signer.verify(TEST_DATA_BYTES, signature));
        }

        @Test
        @DisplayName("Should sign and verify with RSA-PSS-SHA384")
        void testRsaPssSha384SignAndVerify() {
            OpenSign signer = OpenSign.rsaPssSha384().withGeneratedKeyPair();

            byte[] signature = signer.sign(TEST_DATA);
            assertTrue(signer.verify(TEST_DATA, signature));
        }

        @Test
        @DisplayName("Should sign and verify with RSA-PSS-SHA512")
        void testRsaPssSha512SignAndVerify() {
            OpenSign signer = OpenSign.rsaPssSha512().withGeneratedKeyPair();

            String hexSignature = signer.signHex(TEST_DATA);
            assertTrue(signer.verifyHex(TEST_DATA, hexSignature));
        }

        @Test
        @DisplayName("RSA-PSS signature should be non-deterministic")
        void testRsaPssNonDeterministic() {
            OpenSign signer = OpenSign.rsaPss().withGeneratedKeyPair();

            String sig1 = signer.signHex(TEST_DATA);
            String sig2 = signer.signHex(TEST_DATA);

            // RSA-PSS uses random salt, so signatures differ
            assertNotEquals(sig1, sig2);
            // But both should verify
            assertTrue(signer.verifyHex(TEST_DATA, sig1));
            assertTrue(signer.verifyHex(TEST_DATA, sig2));
        }
    }

    @Nested
    @DisplayName("Key Pair Tests")
    class KeyPairTests {

        @Test
        @DisplayName("Should generate key pair for Ed25519")
        void testGenerateEd25519KeyPair() {
            OpenSign signer = OpenSign.ed25519();
            KeyPair keyPair = signer.generateKeyPair();

            assertNotNull(keyPair);
            assertNotNull(keyPair.getPrivate());
            assertNotNull(keyPair.getPublic());
            // JDK 25 reports Ed25519 as "EdDSA"
            assertTrue(keyPair.getPrivate().getAlgorithm().equals("Ed25519") ||
                       keyPair.getPrivate().getAlgorithm().equals("EdDSA"));
        }

        @Test
        @DisplayName("Should generate key pair for ECDSA")
        void testGenerateEcdsaKeyPair() {
            OpenSign signer = OpenSign.ecdsaP256();
            KeyPair keyPair = signer.generateKeyPair();

            assertNotNull(keyPair);
            assertEquals("EC", keyPair.getPrivate().getAlgorithm());
        }

        @Test
        @DisplayName("Should generate key pair for RSA")
        void testGenerateRsaKeyPair() {
            OpenSign signer = OpenSign.sha256WithRsa();
            KeyPair keyPair = signer.generateKeyPair();

            assertNotNull(keyPair);
            assertEquals("RSA", keyPair.getPrivate().getAlgorithm());
        }

        @Test
        @DisplayName("Should set key pair correctly")
        void testSetKeyPair() {
            OpenSign signer = OpenSign.ed25519();
            KeyPair keyPair = signer.generateKeyPair();

            signer.setKeyPair(keyPair);

            byte[] signature = signer.sign(TEST_DATA);
            assertTrue(signer.verify(TEST_DATA, signature));
        }

        @Test
        @DisplayName("Should set private and public key separately")
        void testSetKeysSeparately() {
            OpenSign signer1 = OpenSign.ed25519();
            OpenSign signer2 = OpenSign.ed25519();
            KeyPair keyPair = signer1.generateKeyPair();

            // Set only private key for signing
            signer1.setPrivateKey(keyPair.getPrivate());
            signer1.setPublicKey(keyPair.getPublic());
            byte[] signature = signer1.sign(TEST_DATA);

            // Set only public key for verification
            signer2.setPublicKey(keyPair.getPublic());
            assertTrue(signer2.verify(TEST_DATA, signature));
        }
    }

    @Nested
    @DisplayName("Algorithm Enum Tests")
    class AlgorithmEnumTests {

        @Test
        @DisplayName("Should create signer from enum")
        void testCreateFromEnum() {
            OpenSign signer = OpenSign.of(SignatureAlgorithm.ED25519);
            assertNotNull(signer);
            assertEquals("Ed25519", signer.getAlgorithm());
        }

        @Test
        @DisplayName("Should create ECDSA from enum")
        void testCreateEcdsaFromEnum() {
            OpenSign signer = OpenSign.of(SignatureAlgorithm.ECDSA_P256_SHA256);
            assertNotNull(signer);
            assertTrue(signer.getAlgorithm().contains("ECDSA"));
        }

        @Test
        @DisplayName("Should create RSA from enum")
        void testCreateRsaFromEnum() {
            OpenSign signer = OpenSign.of(SignatureAlgorithm.RSA_SHA256);
            assertNotNull(signer);
            assertTrue(signer.getAlgorithm().contains("RSA"));
        }

        @Test
        @DisplayName("Should throw on null algorithm")
        void testNullAlgorithm() {
            assertThrows(NullPointerException.class, () -> OpenSign.of(null));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw when signing without private key")
        void testSignWithoutPrivateKey() {
            OpenSign signer = OpenSign.ed25519();
            KeyPair keyPair = signer.generateKeyPair();
            signer.setPublicKey(keyPair.getPublic());

            assertThrows(OpenSignatureException.class, () -> signer.sign(TEST_DATA));
        }

        @Test
        @DisplayName("Should throw when verifying without public key")
        void testVerifyWithoutPublicKey() {
            OpenSign signer = OpenSign.ed25519();
            KeyPair keyPair = signer.generateKeyPair();
            signer.setPrivateKey(keyPair.getPrivate());

            byte[] signature = signer.sign(TEST_DATA);

            OpenSign verifier = OpenSign.ed25519();
            assertThrows(OpenSignatureException.class, () -> verifier.verify(TEST_DATA, signature));
        }

        @Test
        @DisplayName("Should throw on null data for signing")
        void testSignNullData() {
            OpenSign signer = OpenSign.ed25519().withGeneratedKeyPair();

            assertThrows(NullPointerException.class, () -> signer.sign((byte[]) null));
            assertThrows(NullPointerException.class, () -> signer.sign((String) null));
        }

        @Test
        @DisplayName("Should throw on null data for verification")
        void testVerifyNullData() {
            OpenSign signer = OpenSign.ed25519().withGeneratedKeyPair();
            byte[] signature = signer.sign(TEST_DATA);

            assertThrows(NullPointerException.class, () -> signer.verify((byte[]) null, signature));
            assertThrows(NullPointerException.class, () -> signer.verify((String) null, signature));
        }

        @Test
        @DisplayName("Should throw on null signature for verification")
        void testVerifyNullSignature() {
            OpenSign signer = OpenSign.ed25519().withGeneratedKeyPair();

            assertThrows(NullPointerException.class, () -> signer.verify(TEST_DATA, (byte[]) null));
        }

        @Test
        @DisplayName("Should throw on null key pair")
        void testSetNullKeyPair() {
            OpenSign signer = OpenSign.ed25519();

            assertThrows(NullPointerException.class, () -> signer.setKeyPair(null));
            assertThrows(NullPointerException.class, () -> signer.setPrivateKey(null));
            assertThrows(NullPointerException.class, () -> signer.setPublicKey(null));
        }
    }

    @Nested
    @DisplayName("Info Methods Tests")
    class InfoMethodsTests {

        @Test
        @DisplayName("Should return correct algorithm name")
        void testGetAlgorithm() {
            assertEquals("Ed25519", OpenSign.ed25519().getAlgorithm());
            assertTrue(OpenSign.ecdsaP256().getAlgorithm().contains("ECDSA"));
            assertTrue(OpenSign.sha256WithRsa().getAlgorithm().contains("RSA"));
        }
    }
}
