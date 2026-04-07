package cloud.opencode.base.crypto.otp;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TotpSecretTest {

    @Nested
    class Generate {

        @Test
        void shouldGenerateDefaultLength() {
            byte[] secret = TotpSecret.generate();
            assertThat(secret).hasSize(20);
        }

        @Test
        void shouldGenerateCustomLength() {
            byte[] secret = TotpSecret.generate(32);
            assertThat(secret).hasSize(32);
        }

        @Test
        void shouldGenerateDifferentSecrets() {
            byte[] s1 = TotpSecret.generate();
            byte[] s2 = TotpSecret.generate();
            assertThat(s1).isNotEqualTo(s2);
        }

        @Test
        void shouldRejectNonPositiveLength() {
            assertThatThrownBy(() -> TotpSecret.generate(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> TotpSecret.generate(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class Base32Encoding {

        @Test
        void shouldEncodeAndDecodeRoundTrip() {
            byte[] original = TotpSecret.generate();
            String encoded = TotpSecret.toBase32(original);
            byte[] decoded = TotpSecret.fromBase32(encoded);
            assertThat(decoded).isEqualTo(original);
        }

        @Test
        void shouldEncodeKnownValue() {
            // "Hello!" = JBSWY3DPEE (RFC 4648 test)
            // "f" = MY
            // "fo" = MZXQ
            // "foo" = MZXW6
            // "foob" = MZXW6YQ
            // "fooba" = MZXW6YTB
            // "foobar" = MZXW6YTBOI
            byte[] data = "foobar".getBytes();
            String encoded = TotpSecret.toBase32(data);
            assertThat(encoded).isEqualToIgnoringCase("MZXW6YTBOI");
        }

        @Test
        void shouldDecodeKnownValue() {
            byte[] decoded = TotpSecret.fromBase32("MZXW6YTBOI");
            assertThat(new String(decoded)).isEqualTo("foobar");
        }

        @Test
        void shouldBeInsensitiveToCase() {
            byte[] upper = TotpSecret.fromBase32("MZXW6YTBOI");
            byte[] lower = TotpSecret.fromBase32("mzxw6ytboi");
            assertThat(upper).isEqualTo(lower);
        }

        @Test
        void shouldIgnoreSpacesAndHyphens() {
            byte[] clean = TotpSecret.fromBase32("MZXW6YTBOI");
            byte[] withSpaces = TotpSecret.fromBase32("MZXW 6YTB OI");
            byte[] withHyphens = TotpSecret.fromBase32("MZXW-6YTB-OI");
            assertThat(withSpaces).isEqualTo(clean);
            assertThat(withHyphens).isEqualTo(clean);
        }

        @Test
        void shouldIgnorePadding() {
            byte[] withPad = TotpSecret.fromBase32("MZXW6YTBOI======");
            byte[] withoutPad = TotpSecret.fromBase32("MZXW6YTBOI");
            assertThat(withPad).isEqualTo(withoutPad);
        }

        @Test
        void shouldHandleEmptyInput() {
            assertThat(TotpSecret.toBase32(new byte[0])).isEmpty();
            assertThat(TotpSecret.fromBase32("")).isEmpty();
        }

        @Test
        void shouldRejectInvalidCharacters() {
            assertThatThrownBy(() -> TotpSecret.fromBase32("MZXW6!@#"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldRejectNullInput() {
            assertThatThrownBy(() -> TotpSecret.toBase32(null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> TotpSecret.fromBase32(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldRoundTripVariousLengths() {
            for (int len = 1; len <= 40; len++) {
                byte[] data = TotpSecret.generate(len);
                String encoded = TotpSecret.toBase32(data);
                byte[] decoded = TotpSecret.fromBase32(encoded);
                assertThat(decoded).as("round-trip length=%d", len).isEqualTo(data);
            }
        }
    }
}
