package cloud.opencode.base.test.data;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * RandomDataTest Tests
 * RandomDataTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("RandomData Tests")
class RandomDataTest {

    @Nested
    @DisplayName("UUID Generation Tests")
    class UuidGenerationTests {

        @Test
        @DisplayName("uuid() should return valid UUID format")
        void uuidShouldReturnValidUuidFormat() {
            String uuid = RandomData.uuid();
            assertThat(uuid).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("uuid() should return unique values")
        void uuidShouldReturnUniqueValues() {
            String uuid1 = RandomData.uuid();
            String uuid2 = RandomData.uuid();
            assertThat(uuid1).isNotEqualTo(uuid2);
        }

        @Test
        @DisplayName("shortUuid() should return 8 characters")
        void shortUuidShouldReturn8Characters() {
            String shortUuid = RandomData.shortUuid();
            assertThat(shortUuid).hasSize(8);
            assertThat(shortUuid).matches("[0-9a-f]{8}");
        }

        @Test
        @DisplayName("compactUuid() should return UUID without dashes")
        void compactUuidShouldReturnUuidWithoutDashes() {
            String compactUuid = RandomData.compactUuid();
            assertThat(compactUuid).hasSize(32);
            assertThat(compactUuid).doesNotContain("-");
            assertThat(compactUuid).matches("[0-9a-f]{32}");
        }
    }

    @Nested
    @DisplayName("Bytes Generation Tests")
    class BytesGenerationTests {

        @Test
        @DisplayName("bytes() should return correct length")
        void bytesShouldReturnCorrectLength() {
            byte[] bytes = RandomData.bytes(16);
            assertThat(bytes).hasSize(16);
        }

        @Test
        @DisplayName("hex() should return double length string")
        void hexShouldReturnDoubleLengthString() {
            String hex = RandomData.hex(16);
            assertThat(hex).hasSize(32);
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("base64() should return base64 encoded string")
        void base64ShouldReturnBase64EncodedString() {
            String base64 = RandomData.base64(24);
            assertThat(base64).isNotBlank();
            // Base64 encoding of 24 bytes = 32 characters
            assertThat(base64).hasSize(32);
        }

        @Test
        @DisplayName("base64Url() should return URL-safe base64")
        void base64UrlShouldReturnUrlSafeBase64() {
            String base64Url = RandomData.base64Url(24);
            assertThat(base64Url).isNotBlank();
            assertThat(base64Url).doesNotContain("+");
            assertThat(base64Url).doesNotContain("/");
            assertThat(base64Url).doesNotContain("=");
        }
    }

    @Nested
    @DisplayName("Hash Generation Tests")
    class HashGenerationTests {

        @Test
        @DisplayName("md5() should return 32 character hex string")
        void md5ShouldReturn32CharacterHexString() {
            String md5 = RandomData.md5();
            assertThat(md5).hasSize(32);
            assertThat(md5).matches("[0-9a-f]{32}");
        }

        @Test
        @DisplayName("sha256() should return 64 character hex string")
        void sha256ShouldReturn64CharacterHexString() {
            String sha256 = RandomData.sha256();
            assertThat(sha256).hasSize(64);
            assertThat(sha256).matches("[0-9a-f]{64}");
        }

        @Test
        @DisplayName("sha512() should return 128 character hex string")
        void sha512ShouldReturn128CharacterHexString() {
            String sha512 = RandomData.sha512();
            assertThat(sha512).hasSize(128);
            assertThat(sha512).matches("[0-9a-f]{128}");
        }
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("apiKey() should start with ak_ prefix")
        void apiKeyShouldStartWithAkPrefix() {
            String apiKey = RandomData.apiKey();
            assertThat(apiKey).startsWith("ak_");
        }

        @Test
        @DisplayName("secretKey() should start with sk_ prefix")
        void secretKeyShouldStartWithSkPrefix() {
            String secretKey = RandomData.secretKey();
            assertThat(secretKey).startsWith("sk_");
        }

        @Test
        @DisplayName("accessToken() should not be blank")
        void accessTokenShouldNotBeBlank() {
            String accessToken = RandomData.accessToken();
            assertThat(accessToken).isNotBlank();
        }

        @Test
        @DisplayName("refreshToken() should not be blank")
        void refreshTokenShouldNotBeBlank() {
            String refreshToken = RandomData.refreshToken();
            assertThat(refreshToken).isNotBlank();
        }
    }

    @Nested
    @DisplayName("Code Generation Tests")
    class CodeGenerationTests {

        @Test
        @DisplayName("numericCode() should return digits only")
        void numericCodeShouldReturnDigitsOnly() {
            String code = RandomData.numericCode(6);
            assertThat(code).hasSize(6);
            assertThat(code).matches("\\d{6}");
        }

        @Test
        @DisplayName("alphanumericCode() should return alphanumeric characters")
        void alphanumericCodeShouldReturnAlphanumericCharacters() {
            String code = RandomData.alphanumericCode(8);
            assertThat(code).hasSize(8);
            assertThat(code).matches("[A-Z0-9]+");
        }
    }

    @Nested
    @DisplayName("Sequence Generation Tests")
    class SequenceGenerationTests {

        @Test
        @DisplayName("sequenceId() should start with prefix")
        void sequenceIdShouldStartWithPrefix() {
            String seqId = RandomData.sequenceId("TEST");
            assertThat(seqId).startsWith("TEST");
        }

        @Test
        @DisplayName("orderNumber() should start with ORD")
        void orderNumberShouldStartWithOrd() {
            String orderNum = RandomData.orderNumber();
            assertThat(orderNum).startsWith("ORD");
        }

        @Test
        @DisplayName("transactionId() should start with TXN")
        void transactionIdShouldStartWithTxn() {
            String txnId = RandomData.transactionId();
            assertThat(txnId).startsWith("TXN");
        }
    }
}
