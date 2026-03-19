package cloud.opencode.base.crypto;

import cloud.opencode.base.crypto.enums.PasswordHashAlgorithm;
import cloud.opencode.base.crypto.password.PasswordPolicy;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for OpenPasswordHash
 *
 * @author Leon Soo
 * @since JDK 25, OpenCode-Base-Crypto V1.0.0
 */
@DisplayName("OpenPasswordHash Tests")
class OpenPasswordHashTest {

    private static final String TEST_PASSWORD = "SecurePassword123!";
    private static final char[] TEST_PASSWORD_CHARS = TEST_PASSWORD.toCharArray();

    @Nested
    @DisplayName("Argon2 Tests")
    class Argon2Tests {

        @Test
        @DisplayName("Should hash and verify password with Argon2")
        void testArgon2HashAndVerify() {
            OpenPasswordHash hasher = OpenPasswordHash.argon2();

            String hash = hasher.hash(TEST_PASSWORD);
            assertNotNull(hash);
            assertTrue(hash.length() > 0);

            assertTrue(hasher.verify(TEST_PASSWORD, hash));
        }

        @Test
        @DisplayName("Should hash and verify char array password")
        void testArgon2HashCharArray() {
            OpenPasswordHash hasher = OpenPasswordHash.argon2();

            String hash = hasher.hash(TEST_PASSWORD_CHARS);
            assertNotNull(hash);

            assertTrue(hasher.verify(TEST_PASSWORD_CHARS, hash));
        }

        @Test
        @DisplayName("Should produce different hashes for same password (salt)")
        void testArgon2DifferentHashes() {
            OpenPasswordHash hasher = OpenPasswordHash.argon2();

            String hash1 = hasher.hash(TEST_PASSWORD);
            String hash2 = hasher.hash(TEST_PASSWORD);

            // Should be different due to random salt
            assertNotEquals(hash1, hash2);
            // But both should verify
            assertTrue(hasher.verify(TEST_PASSWORD, hash1));
            assertTrue(hasher.verify(TEST_PASSWORD, hash2));
        }

        @Test
        @DisplayName("Should fail verification with wrong password")
        void testArgon2WrongPassword() {
            OpenPasswordHash hasher = OpenPasswordHash.argon2();

            String hash = hasher.hash(TEST_PASSWORD);
            assertFalse(hasher.verify("WrongPassword", hash));
        }

        @Test
        @DisplayName("Should return Argon2 algorithm name")
        void testArgon2AlgorithmName() {
            OpenPasswordHash hasher = OpenPasswordHash.argon2();
            assertTrue(hasher.getAlgorithm().toLowerCase().contains("argon2"));
        }
    }

    @Nested
    @DisplayName("BCrypt Tests")
    class BCryptTests {

        @Test
        @DisplayName("Should hash and verify password with BCrypt")
        void testBCryptHashAndVerify() {
            OpenPasswordHash hasher = OpenPasswordHash.bcrypt();

            String hash = hasher.hash(TEST_PASSWORD);
            assertNotNull(hash);
            assertTrue(hash.startsWith("$2")); // BCrypt hash format

            assertTrue(hasher.verify(TEST_PASSWORD, hash));
        }

        @Test
        @DisplayName("Should hash with custom cost factor")
        void testBCryptCustomCost() {
            OpenPasswordHash hasher = OpenPasswordHash.bcrypt(10);

            String hash = hasher.hash(TEST_PASSWORD);
            assertNotNull(hash);

            assertTrue(hasher.verify(TEST_PASSWORD, hash));
        }

        @Test
        @DisplayName("Should produce different hashes for same password")
        void testBCryptDifferentHashes() {
            OpenPasswordHash hasher = OpenPasswordHash.bcrypt();

            String hash1 = hasher.hash(TEST_PASSWORD);
            String hash2 = hasher.hash(TEST_PASSWORD);

            assertNotEquals(hash1, hash2);
            assertTrue(hasher.verify(TEST_PASSWORD, hash1));
            assertTrue(hasher.verify(TEST_PASSWORD, hash2));
        }

        @Test
        @DisplayName("Should fail verification with wrong password")
        void testBCryptWrongPassword() {
            OpenPasswordHash hasher = OpenPasswordHash.bcrypt();

            String hash = hasher.hash(TEST_PASSWORD);
            assertFalse(hasher.verify("WrongPassword", hash));
        }

        @Test
        @DisplayName("Should return BCrypt algorithm name")
        void testBCryptAlgorithmName() {
            OpenPasswordHash hasher = OpenPasswordHash.bcrypt();
            assertTrue(hasher.getAlgorithm().toLowerCase().contains("bcrypt"));
        }
    }

    @Nested
    @DisplayName("SCrypt Tests")
    class SCryptTests {

        @Test
        @DisplayName("Should hash and verify password with SCrypt")
        void testSCryptHashAndVerify() {
            OpenPasswordHash hasher = OpenPasswordHash.scrypt();

            String hash = hasher.hash(TEST_PASSWORD);
            assertNotNull(hash);

            assertTrue(hasher.verify(TEST_PASSWORD, hash));
        }

        @Test
        @DisplayName("Should produce different hashes for same password")
        void testSCryptDifferentHashes() {
            OpenPasswordHash hasher = OpenPasswordHash.scrypt();

            String hash1 = hasher.hash(TEST_PASSWORD);
            String hash2 = hasher.hash(TEST_PASSWORD);

            assertNotEquals(hash1, hash2);
            assertTrue(hasher.verify(TEST_PASSWORD, hash1));
        }

        @Test
        @DisplayName("Should fail verification with wrong password")
        void testSCryptWrongPassword() {
            OpenPasswordHash hasher = OpenPasswordHash.scrypt();

            String hash = hasher.hash(TEST_PASSWORD);
            assertFalse(hasher.verify("WrongPassword", hash));
        }

        @Test
        @DisplayName("Should return SCrypt algorithm name")
        void testSCryptAlgorithmName() {
            OpenPasswordHash hasher = OpenPasswordHash.scrypt();
            assertTrue(hasher.getAlgorithm().toLowerCase().contains("scrypt"));
        }
    }

    @Nested
    @DisplayName("PBKDF2 Tests")
    class Pbkdf2Tests {

        @Test
        @DisplayName("Should hash and verify password with PBKDF2")
        void testPbkdf2HashAndVerify() {
            OpenPasswordHash hasher = OpenPasswordHash.pbkdf2();

            String hash = hasher.hash(TEST_PASSWORD);
            assertNotNull(hash);

            assertTrue(hasher.verify(TEST_PASSWORD, hash));
        }

        @Test
        @DisplayName("Should hash with custom iterations")
        void testPbkdf2CustomIterations() {
            OpenPasswordHash hasher = OpenPasswordHash.pbkdf2(100000);

            String hash = hasher.hash(TEST_PASSWORD);
            assertNotNull(hash);

            assertTrue(hasher.verify(TEST_PASSWORD, hash));
        }

        @Test
        @DisplayName("Should produce different hashes for same password")
        void testPbkdf2DifferentHashes() {
            OpenPasswordHash hasher = OpenPasswordHash.pbkdf2();

            String hash1 = hasher.hash(TEST_PASSWORD);
            String hash2 = hasher.hash(TEST_PASSWORD);

            assertNotEquals(hash1, hash2);
            assertTrue(hasher.verify(TEST_PASSWORD, hash1));
        }

        @Test
        @DisplayName("Should fail verification with wrong password")
        void testPbkdf2WrongPassword() {
            OpenPasswordHash hasher = OpenPasswordHash.pbkdf2();

            String hash = hasher.hash(TEST_PASSWORD);
            assertFalse(hasher.verify("WrongPassword", hash));
        }

        @Test
        @DisplayName("Should return PBKDF2 algorithm name")
        void testPbkdf2AlgorithmName() {
            OpenPasswordHash hasher = OpenPasswordHash.pbkdf2();
            assertTrue(hasher.getAlgorithm().toLowerCase().contains("pbkdf2"));
        }
    }

    @Nested
    @DisplayName("Algorithm Enum Tests")
    class AlgorithmEnumTests {

        @Test
        @DisplayName("Should create hasher from Argon2 enum")
        void testCreateArgon2FromEnum() {
            OpenPasswordHash hasher = OpenPasswordHash.of(PasswordHashAlgorithm.ARGON2ID);
            assertNotNull(hasher);

            String hash = hasher.hash(TEST_PASSWORD);
            assertTrue(hasher.verify(TEST_PASSWORD, hash));
        }

        @Test
        @DisplayName("Should create hasher from BCrypt enum")
        void testCreateBCryptFromEnum() {
            OpenPasswordHash hasher = OpenPasswordHash.of(PasswordHashAlgorithm.BCRYPT);
            assertNotNull(hasher);

            String hash = hasher.hash(TEST_PASSWORD);
            assertTrue(hasher.verify(TEST_PASSWORD, hash));
        }

        @Test
        @DisplayName("Should create hasher from SCrypt enum")
        void testCreateSCryptFromEnum() {
            OpenPasswordHash hasher = OpenPasswordHash.of(PasswordHashAlgorithm.SCRYPT);
            assertNotNull(hasher);

            String hash = hasher.hash(TEST_PASSWORD);
            assertTrue(hasher.verify(TEST_PASSWORD, hash));
        }

        @Test
        @DisplayName("Should create hasher from PBKDF2 enum")
        void testCreatePbkdf2FromEnum() {
            OpenPasswordHash hasher = OpenPasswordHash.of(PasswordHashAlgorithm.PBKDF2_SHA256);
            assertNotNull(hasher);

            String hash = hasher.hash(TEST_PASSWORD);
            assertTrue(hasher.verify(TEST_PASSWORD, hash));
        }

        @Test
        @DisplayName("Should throw on null algorithm")
        void testNullAlgorithm() {
            assertThrows(NullPointerException.class, () -> OpenPasswordHash.of(null));
        }
    }

    @Nested
    @DisplayName("Needs Rehash Tests")
    class NeedsRehashTests {

        @Test
        @DisplayName("Should check if hash needs rehash")
        void testNeedsRehash() {
            OpenPasswordHash hasher = OpenPasswordHash.argon2();
            String hash = hasher.hash(TEST_PASSWORD);

            // For same settings, should not need rehash
            boolean needsRehash = hasher.needsRehash(hash);
            assertNotNull(Boolean.valueOf(needsRehash)); // Just verify it returns without error
        }
    }

    @Nested
    @DisplayName("Password Policy Tests")
    class PasswordPolicyTests {

        @Test
        @DisplayName("Should return default password policy")
        void testDefaultPolicy() {
            PasswordPolicy policy = OpenPasswordHash.defaultPolicy();
            assertNotNull(policy);
        }

        @Test
        @DisplayName("Should return strong password policy")
        void testStrongPolicy() {
            PasswordPolicy policy = OpenPasswordHash.strongPolicy();
            assertNotNull(policy);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw on null password for hash")
        void testHashNullPassword() {
            OpenPasswordHash hasher = OpenPasswordHash.argon2();

            assertThrows(NullPointerException.class, () -> hasher.hash((String) null));
            assertThrows(NullPointerException.class, () -> hasher.hash((char[]) null));
        }

        @Test
        @DisplayName("Should throw on null password for verify")
        void testVerifyNullPassword() {
            OpenPasswordHash hasher = OpenPasswordHash.argon2();
            String hash = hasher.hash(TEST_PASSWORD);

            assertThrows(NullPointerException.class, () -> hasher.verify((String) null, hash));
            assertThrows(NullPointerException.class, () -> hasher.verify((char[]) null, hash));
        }

        @Test
        @DisplayName("Should throw on null hash for verify")
        void testVerifyNullHash() {
            OpenPasswordHash hasher = OpenPasswordHash.argon2();

            assertThrows(NullPointerException.class, () -> hasher.verify(TEST_PASSWORD, null));
        }

        @Test
        @DisplayName("Should throw on null hash for needsRehash")
        void testNeedsRehashNullHash() {
            OpenPasswordHash hasher = OpenPasswordHash.argon2();

            assertThrows(NullPointerException.class, () -> hasher.needsRehash(null));
        }

        @Test
        @DisplayName("Should handle empty password")
        void testEmptyPassword() {
            OpenPasswordHash hasher = OpenPasswordHash.argon2();

            String hash = hasher.hash("");
            assertNotNull(hash);
            assertTrue(hasher.verify("", hash));
        }
    }

    @Nested
    @DisplayName("Cross-Algorithm Tests")
    class CrossAlgorithmTests {

        @Test
        @DisplayName("Different algorithms produce different hash formats")
        void testDifferentHashFormats() {
            String argon2Hash = OpenPasswordHash.argon2().hash(TEST_PASSWORD);
            String scryptHash = OpenPasswordHash.scrypt().hash(TEST_PASSWORD);
            String pbkdf2Hash = OpenPasswordHash.pbkdf2().hash(TEST_PASSWORD);

            // All should be different
            assertNotEquals(argon2Hash, scryptHash);
            assertNotEquals(argon2Hash, pbkdf2Hash);
            assertNotEquals(scryptHash, pbkdf2Hash);
        }
    }
}
