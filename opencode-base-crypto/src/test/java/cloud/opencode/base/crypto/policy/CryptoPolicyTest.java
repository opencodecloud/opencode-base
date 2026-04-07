package cloud.opencode.base.crypto.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link CryptoPolicy} and {@link PolicyViolationException}.
 */
class CryptoPolicyTest {

    @Nested
    @DisplayName("Strict Policy")
    class StrictPolicyTests {

        private final CryptoPolicy policy = CryptoPolicy.strict();

        @Test
        @DisplayName("allows AES-256-GCM")
        void allowsAes256Gcm() {
            assertThat(policy.isAllowed("AES-256-GCM", 256)).isTrue();
            assertThatCode(() -> policy.check("AES-256-GCM", 256)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("allows ChaCha20-Poly1305")
        void allowsChaCha20Poly1305() {
            assertThat(policy.isAllowed("ChaCha20-Poly1305", 256)).isTrue();
        }

        @Test
        @DisplayName("allows Ed25519")
        void allowsEd25519() {
            assertThat(policy.isAllowed("Ed25519", 256)).isTrue();
        }

        @Test
        @DisplayName("allows SHA-512")
        void allowsSha512() {
            assertThat(policy.isAllowed("SHA-512", 0)).isTrue();
        }

        @Test
        @DisplayName("allows SHA3-256")
        void allowsSha3_256() {
            assertThat(policy.isAllowed("SHA3-256", 0)).isTrue();
        }

        @Test
        @DisplayName("allows Argon2id")
        void allowsArgon2id() {
            assertThat(policy.isAllowed("Argon2id", 0)).isTrue();
        }

        @Test
        @DisplayName("allows ECDSA-P256 and ECDSA-P384")
        void allowsEcdsaCurves() {
            assertThat(policy.isAllowed("ECDSA-P256", 256)).isTrue();
            assertThat(policy.isAllowed("ECDSA-P384", 384)).isTrue();
        }

        @Test
        @DisplayName("allows RSA-PSS with >= 4096 bits")
        void allowsRsaPssWithSufficientBits() {
            assertThat(policy.isAllowed("RSA-PSS", 4096)).isTrue();
            assertThat(policy.isAllowed("RSA-PSS", 8192)).isTrue();
        }

        @Test
        @DisplayName("denies RSA-PSS with < 4096 bits")
        void deniesRsaPssWithInsufficientBits() {
            assertThat(policy.isAllowed("RSA-PSS", 2048)).isFalse();
        }

        @Test
        @DisplayName("denies AES-128-GCM")
        void deniesAes128Gcm() {
            assertThat(policy.isAllowed("AES-128-GCM", 128)).isFalse();
        }

        @Test
        @DisplayName("denies MD5")
        void deniesMd5() {
            assertThat(policy.isAllowed("MD5", 0)).isFalse();
        }

        @Test
        @DisplayName("denies SHA-1")
        void deniesSha1() {
            assertThat(policy.isAllowed("SHA-1", 0)).isFalse();
        }

        @Test
        @DisplayName("denies 3DES")
        void denies3Des() {
            assertThat(policy.isAllowed("3DES", 168)).isFalse();
        }

        @Test
        @DisplayName("denies RSA (non-PSS)")
        void deniesPlainRsa() {
            assertThat(policy.isAllowed("RSA", 4096)).isFalse();
        }
    }

    @Nested
    @DisplayName("Standard Policy")
    class StandardPolicyTests {

        private final CryptoPolicy policy = CryptoPolicy.standard();

        @Test
        @DisplayName("includes all strict algorithms")
        void includesStrictAlgorithms() {
            assertThat(policy.isAllowed("AES-256-GCM", 256)).isTrue();
            assertThat(policy.isAllowed("ChaCha20-Poly1305", 256)).isTrue();
            assertThat(policy.isAllowed("Ed25519", 256)).isTrue();
            assertThat(policy.isAllowed("SHA-256", 0)).isTrue();
        }

        @Test
        @DisplayName("allows AES-128-GCM")
        void allowsAes128Gcm() {
            assertThat(policy.isAllowed("AES-128-GCM", 128)).isTrue();
        }

        @Test
        @DisplayName("allows AES-256-CBC")
        void allowsAes256Cbc() {
            assertThat(policy.isAllowed("AES-256-CBC", 256)).isTrue();
        }

        @Test
        @DisplayName("allows RSA-OAEP with >= 2048 bits")
        void allowsRsaOaep() {
            assertThat(policy.isAllowed("RSA-OAEP", 2048)).isTrue();
            assertThat(policy.isAllowed("RSA-OAEP", 4096)).isTrue();
        }

        @Test
        @DisplayName("denies RSA-OAEP with < 2048 bits")
        void deniesRsaOaepSmallKey() {
            assertThat(policy.isAllowed("RSA-OAEP", 1024)).isFalse();
        }

        @Test
        @DisplayName("allows RSA with >= 2048 bits")
        void allowsRsa() {
            assertThat(policy.isAllowed("RSA", 2048)).isTrue();
        }

        @Test
        @DisplayName("denies RSA with < 2048 bits")
        void deniesRsaSmallKey() {
            assertThat(policy.isAllowed("RSA", 1024)).isFalse();
        }

        @Test
        @DisplayName("allows ECDSA-P521, PBKDF2, BCrypt, SCrypt, Ed448, X448")
        void allowsAdditionalAlgorithms() {
            assertThat(policy.isAllowed("ECDSA-P521", 521)).isTrue();
            assertThat(policy.isAllowed("PBKDF2", 0)).isTrue();
            assertThat(policy.isAllowed("BCrypt", 0)).isTrue();
            assertThat(policy.isAllowed("SCrypt", 0)).isTrue();
            assertThat(policy.isAllowed("Ed448", 448)).isTrue();
            assertThat(policy.isAllowed("X448", 448)).isTrue();
        }

        @Test
        @DisplayName("denies MD5 and SHA-1")
        void deniesLegacy() {
            assertThat(policy.isAllowed("MD5", 0)).isFalse();
            assertThat(policy.isAllowed("SHA-1", 0)).isFalse();
        }
    }

    @Nested
    @DisplayName("Legacy Policy")
    class LegacyPolicyTests {

        private final CryptoPolicy policy = CryptoPolicy.legacy();

        @Test
        @DisplayName("includes all standard algorithms")
        void includesStandardAlgorithms() {
            assertThat(policy.isAllowed("AES-256-GCM", 256)).isTrue();
            assertThat(policy.isAllowed("AES-128-GCM", 128)).isTrue();
            assertThat(policy.isAllowed("RSA", 2048)).isTrue();
        }

        @Test
        @DisplayName("allows AES-128-CBC")
        void allowsAes128Cbc() {
            assertThat(policy.isAllowed("AES-128-CBC", 128)).isTrue();
        }

        @Test
        @DisplayName("allows RSA with >= 1024 bits")
        void allowsRsa1024() {
            assertThat(policy.isAllowed("RSA", 1024)).isTrue();
        }

        @Test
        @DisplayName("denies RSA with < 1024 bits")
        void deniesRsa512() {
            assertThat(policy.isAllowed("RSA", 512)).isFalse();
        }

        @Test
        @DisplayName("allows 3DES, SHA-1, MD5")
        void allowsLegacyAlgorithms() {
            assertThat(policy.isAllowed("3DES", 168)).isTrue();
            assertThat(policy.isAllowed("SHA-1", 0)).isTrue();
            assertThat(policy.isAllowed("MD5", 0)).isTrue();
        }
    }

    @Nested
    @DisplayName("Custom Builder")
    class CustomBuilderTests {

        @Test
        @DisplayName("builds custom policy with allow and deny")
        void customAllowAndDeny() {
            CryptoPolicy policy = CryptoPolicy.builder()
                    .allow("AES-256-GCM", "SHA-256")
                    .deny("MD5")
                    .build();

            assertThat(policy.isAllowed("AES-256-GCM", 256)).isTrue();
            assertThat(policy.isAllowed("SHA-256", 0)).isTrue();
            assertThat(policy.isAllowed("MD5", 0)).isFalse();
            assertThat(policy.isAllowed("RSA", 2048)).isFalse();
        }

        @Test
        @DisplayName("deny overrides allow")
        void denyOverridesAllow() {
            CryptoPolicy policy = CryptoPolicy.builder()
                    .allow("MD5")
                    .deny("MD5")
                    .build();

            assertThat(policy.isAllowed("MD5", 0)).isFalse();
        }

        @Test
        @DisplayName("minKeyBits enforcement")
        void minKeyBitsEnforcement() {
            CryptoPolicy policy = CryptoPolicy.builder()
                    .allow("RSA")
                    .minKeyBits("RSA", 2048)
                    .build();

            assertThat(policy.isAllowed("RSA", 2048)).isTrue();
            assertThat(policy.isAllowed("RSA", 4096)).isTrue();
            assertThat(policy.isAllowed("RSA", 1024)).isFalse();
        }

        @Test
        @DisplayName("getAllowedAlgorithms returns unmodifiable set")
        void getAllowedAlgorithmsUnmodifiable() {
            CryptoPolicy policy = CryptoPolicy.builder()
                    .allow("AES-256-GCM", "SHA-256")
                    .build();

            Set<String> allowed = policy.getAllowedAlgorithms();
            assertThat(allowed).containsExactlyInAnyOrder("AES-256-GCM", "SHA-256");
            assertThatThrownBy(() -> allowed.add("MD5"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Case Insensitivity")
    class CaseInsensitivityTests {

        @Test
        @DisplayName("algorithm matching is case-insensitive")
        void caseInsensitiveMatching() {
            CryptoPolicy policy = CryptoPolicy.builder()
                    .allow("AES-256-GCM")
                    .build();

            assertThat(policy.isAllowed("aes-256-gcm", 256)).isTrue();
            assertThat(policy.isAllowed("Aes-256-Gcm", 256)).isTrue();
            assertThat(policy.isAllowed("AES-256-GCM", 256)).isTrue();
        }

        @Test
        @DisplayName("deny matching is case-insensitive")
        void caseInsensitiveDeny() {
            CryptoPolicy policy = CryptoPolicy.builder()
                    .allow("AES-256-GCM")
                    .deny("md5")
                    .build();

            assertThat(policy.isAllowed("MD5", 0)).isFalse();
        }

        @Test
        @DisplayName("minKeyBits matching is case-insensitive")
        void caseInsensitiveMinKeyBits() {
            CryptoPolicy policy = CryptoPolicy.builder()
                    .allow("RSA")
                    .minKeyBits("rsa", 2048)
                    .build();

            assertThat(policy.isAllowed("RSA", 2048)).isTrue();
            assertThat(policy.isAllowed("RSA", 1024)).isFalse();
        }
    }

    @Nested
    @DisplayName("Check Throws Exception")
    class CheckThrowsTests {

        @Test
        @DisplayName("check throws PolicyViolationException for denied algorithm")
        void checkThrowsForDenied() {
            CryptoPolicy policy = CryptoPolicy.builder()
                    .allow("AES-256-GCM")
                    .deny("MD5")
                    .build();

            assertThatThrownBy(() -> policy.check("MD5", 0))
                    .isInstanceOf(PolicyViolationException.class)
                    .hasMessageContaining("MD5")
                    .hasMessageContaining("denied");
        }

        @Test
        @DisplayName("check throws PolicyViolationException for unknown algorithm")
        void checkThrowsForUnknown() {
            CryptoPolicy policy = CryptoPolicy.builder()
                    .allow("AES-256-GCM")
                    .build();

            assertThatThrownBy(() -> policy.check("BLOWFISH", 128))
                    .isInstanceOf(PolicyViolationException.class)
                    .hasMessageContaining("BLOWFISH")
                    .hasMessageContaining("not allowed");
        }

        @Test
        @DisplayName("check throws PolicyViolationException for insufficient key bits")
        void checkThrowsForInsufficientKeyBits() {
            CryptoPolicy policy = CryptoPolicy.builder()
                    .allow("RSA")
                    .minKeyBits("RSA", 2048)
                    .build();

            assertThatThrownBy(() -> policy.check("RSA", 1024))
                    .isInstanceOf(PolicyViolationException.class)
                    .hasMessageContaining("2048")
                    .hasMessageContaining("1024");
        }

        @Test
        @DisplayName("PolicyViolationException carries violated algorithm")
        void exceptionCarriesAlgorithm() {
            CryptoPolicy policy = CryptoPolicy.builder()
                    .allow("AES-256-GCM")
                    .build();

            assertThatThrownBy(() -> policy.check("DES", 56))
                    .isInstanceOf(PolicyViolationException.class)
                    .satisfies(ex -> assertThat(((PolicyViolationException) ex).getViolatedAlgorithm())
                            .isEqualTo("DES"));
        }

        @Test
        @DisplayName("check throws NullPointerException for null algorithm")
        void checkThrowsForNull() {
            CryptoPolicy policy = CryptoPolicy.strict();
            assertThatThrownBy(() -> policy.check(null, 0))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
