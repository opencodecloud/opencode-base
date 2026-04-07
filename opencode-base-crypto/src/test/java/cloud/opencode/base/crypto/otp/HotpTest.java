package cloud.opencode.base.crypto.otp;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

class HotpTest {

    // RFC 4226 Appendix D test secret: "12345678901234567890" (ASCII)
    private static final byte[] RFC_SECRET = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);

    // RFC 4226 Appendix D expected HOTP values (SHA-1, 6 digits)
    private static final String[] RFC_EXPECTED = {
            "755224", // counter 0
            "287082", // counter 1
            "359152", // counter 2
            "969429", // counter 3
            "338314", // counter 4
            "254676", // counter 5
            "287922", // counter 6
            "162583", // counter 7
            "399871", // counter 8
            "520489"  // counter 9
    };

    @Nested
    class Rfc4226TestVectors {

        @Test
        void shouldMatchRfc4226AppendixDVectors() {
            Hotp hotp = Hotp.sha1();
            for (int i = 0; i < RFC_EXPECTED.length; i++) {
                assertThat(hotp.generate(RFC_SECRET, i))
                        .as("HOTP counter=%d", i)
                        .isEqualTo(RFC_EXPECTED[i]);
            }
        }

        @Test
        void shouldVerifyRfc4226Codes() {
            Hotp hotp = Hotp.sha1();
            for (int i = 0; i < RFC_EXPECTED.length; i++) {
                assertThat(hotp.verify(RFC_SECRET, i, RFC_EXPECTED[i]))
                        .as("verify counter=%d", i)
                        .isTrue();
            }
        }
    }

    @Nested
    class Generate {

        @Test
        void shouldGenerate6DigitsByDefault() {
            String code = Hotp.sha1().generate(RFC_SECRET, 0);
            assertThat(code).hasSize(6);
            assertThat(code).matches("\\d{6}");
        }

        @Test
        void shouldGenerate7Digits() {
            String code = Hotp.sha1().generate(RFC_SECRET, 0, 7);
            assertThat(code).hasSize(7);
            assertThat(code).matches("\\d{7}");
        }

        @Test
        void shouldGenerate8Digits() {
            String code = Hotp.sha1().generate(RFC_SECRET, 0, 8);
            assertThat(code).hasSize(8);
            assertThat(code).matches("\\d{8}");
        }

        @Test
        void shouldRejectDigitsBelow6() {
            assertThatThrownBy(() -> Hotp.sha1().generate(RFC_SECRET, 0, 5))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldRejectDigitsAbove8() {
            assertThatThrownBy(() -> Hotp.sha1().generate(RFC_SECRET, 0, 9))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldRejectNullSecret() {
            assertThatThrownBy(() -> Hotp.sha1().generate(null, 0))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class Verify {

        @Test
        void shouldVerifyExactCounter() {
            Hotp hotp = Hotp.sha1();
            String code = hotp.generate(RFC_SECRET, 5);
            assertThat(hotp.verify(RFC_SECRET, 5, code)).isTrue();
        }

        @Test
        void shouldRejectWrongCounter() {
            Hotp hotp = Hotp.sha1();
            String code = hotp.generate(RFC_SECRET, 5);
            assertThat(hotp.verify(RFC_SECRET, 6, code)).isFalse();
        }

        @Test
        void shouldVerifyWithLookAhead() {
            Hotp hotp = Hotp.sha1();
            String code = hotp.generate(RFC_SECRET, 7);
            // counter=5 with lookAhead=3 checks 5,6,7,8
            assertThat(hotp.verify(RFC_SECRET, 5, code, 3)).isTrue();
        }

        @Test
        void shouldRejectBeyondLookAhead() {
            Hotp hotp = Hotp.sha1();
            String code = hotp.generate(RFC_SECRET, 10);
            // counter=5 with lookAhead=3 checks 5,6,7,8
            assertThat(hotp.verify(RFC_SECRET, 5, code, 3)).isFalse();
        }

        @Test
        void shouldRejectNegativeLookAhead() {
            assertThatThrownBy(() -> Hotp.sha1().verify(RFC_SECRET, 0, "123456", -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldRejectExcessiveLookAhead() {
            assertThatThrownBy(() -> Hotp.sha1().verify(RFC_SECRET, 0, "123456", 101))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldRejectInvalidCodeLength() {
            assertThat(Hotp.sha1().verify(RFC_SECRET, 0, "12345")).isFalse();
            assertThat(Hotp.sha1().verify(RFC_SECRET, 0, "123456789")).isFalse();
        }
    }

    @Nested
    class Algorithms {

        @Test
        void shouldSupportSha256() {
            Hotp hotp = Hotp.sha256();
            String code = hotp.generate(RFC_SECRET, 0);
            assertThat(code).hasSize(6).matches("\\d{6}");
            assertThat(hotp.verify(RFC_SECRET, 0, code)).isTrue();
        }

        @Test
        void shouldSupportSha512() {
            Hotp hotp = Hotp.sha512();
            String code = hotp.generate(RFC_SECRET, 0);
            assertThat(code).hasSize(6).matches("\\d{6}");
            assertThat(hotp.verify(RFC_SECRET, 0, code)).isTrue();
        }

        @Test
        void shouldRejectInvalidAlgorithm() {
            assertThatThrownBy(() -> Hotp.of("HmacINVALID"))
                    .isInstanceOf(OpenCryptoException.class);
        }

        @Test
        void shouldReturnAlgorithmName() {
            assertThat(Hotp.sha1().algorithm()).isEqualTo("HmacSHA1");
            assertThat(Hotp.sha256().algorithm()).isEqualTo("HmacSHA256");
            assertThat(Hotp.sha512().algorithm()).isEqualTo("HmacSHA512");
        }
    }
}
