package cloud.opencode.base.crypto.otp;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class TotpTest {

    // RFC 6238 test secrets (ASCII byte representations)
    private static final byte[] SECRET_SHA1 = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] SECRET_SHA256 = "12345678901234567890123456789012".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] SECRET_SHA512 = "1234567890123456789012345678901234567890123456789012345678901234".getBytes(StandardCharsets.US_ASCII);

    @Nested
    class Rfc6238TestVectors {

        @Test
        void shouldMatchSha1Vectors() {
            Totp totp = Totp.builder().algorithm("HmacSHA1").digits(8).period(30).build();

            assertThat(totp.generate(SECRET_SHA1, Instant.ofEpochSecond(59)))
                    .isEqualTo("94287082");
            assertThat(totp.generate(SECRET_SHA1, Instant.ofEpochSecond(1111111109)))
                    .isEqualTo("07081804");
            assertThat(totp.generate(SECRET_SHA1, Instant.ofEpochSecond(1111111111)))
                    .isEqualTo("14050471");
            assertThat(totp.generate(SECRET_SHA1, Instant.ofEpochSecond(1234567890)))
                    .isEqualTo("89005924");
            assertThat(totp.generate(SECRET_SHA1, Instant.ofEpochSecond(2000000000)))
                    .isEqualTo("69279037");
            assertThat(totp.generate(SECRET_SHA1, Instant.ofEpochSecond(20000000000L)))
                    .isEqualTo("65353130");
        }

        @Test
        void shouldMatchSha256Vectors() {
            Totp totp = Totp.builder().algorithm("HmacSHA256").digits(8).period(30).build();

            assertThat(totp.generate(SECRET_SHA256, Instant.ofEpochSecond(59)))
                    .isEqualTo("46119246");
            assertThat(totp.generate(SECRET_SHA256, Instant.ofEpochSecond(1111111109)))
                    .isEqualTo("68084774");
            assertThat(totp.generate(SECRET_SHA256, Instant.ofEpochSecond(1111111111)))
                    .isEqualTo("67062674");
            assertThat(totp.generate(SECRET_SHA256, Instant.ofEpochSecond(1234567890)))
                    .isEqualTo("91819424");
            assertThat(totp.generate(SECRET_SHA256, Instant.ofEpochSecond(2000000000)))
                    .isEqualTo("90698825");
            assertThat(totp.generate(SECRET_SHA256, Instant.ofEpochSecond(20000000000L)))
                    .isEqualTo("77737706");
        }

        @Test
        void shouldMatchSha512Vectors() {
            Totp totp = Totp.builder().algorithm("HmacSHA512").digits(8).period(30).build();

            assertThat(totp.generate(SECRET_SHA512, Instant.ofEpochSecond(59)))
                    .isEqualTo("90693936");
            assertThat(totp.generate(SECRET_SHA512, Instant.ofEpochSecond(1111111109)))
                    .isEqualTo("25091201");
            assertThat(totp.generate(SECRET_SHA512, Instant.ofEpochSecond(1111111111)))
                    .isEqualTo("99943326");
            assertThat(totp.generate(SECRET_SHA512, Instant.ofEpochSecond(1234567890)))
                    .isEqualTo("93441116");
            assertThat(totp.generate(SECRET_SHA512, Instant.ofEpochSecond(2000000000)))
                    .isEqualTo("38618901");
            assertThat(totp.generate(SECRET_SHA512, Instant.ofEpochSecond(20000000000L)))
                    .isEqualTo("47863826");
        }
    }

    @Nested
    class Generate {

        @Test
        void shouldGenerateDefault6Digits() {
            Totp totp = Totp.sha1();
            String code = totp.generate(SECRET_SHA1, Instant.ofEpochSecond(59));
            assertThat(code).hasSize(6);
        }

        @Test
        void shouldGenerateForCurrentTime() {
            Totp totp = Totp.sha1();
            String code = totp.generate(SECRET_SHA1);
            assertThat(code).hasSize(6).matches("\\d{6}");
        }

        @Test
        void shouldRejectPreEpochTime() {
            assertThatThrownBy(() -> Totp.sha1().generate(SECRET_SHA1, Instant.ofEpochSecond(-1)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class Verify {

        @Test
        void shouldVerifyExactTime() {
            Totp totp = Totp.builder().algorithm("HmacSHA1").digits(8).period(30).build();
            Instant time = Instant.ofEpochSecond(59);
            String code = totp.generate(SECRET_SHA1, time);
            assertThat(totp.verify(SECRET_SHA1, code, time, 0)).isTrue();
        }

        @Test
        void shouldVerifyWithWindow() {
            Totp totp = Totp.builder().algorithm("HmacSHA1").digits(8).period(30).build();
            Instant time = Instant.ofEpochSecond(59);
            String code = totp.generate(SECRET_SHA1, time);
            // 30 seconds later (next window), code should still verify with window=1
            Instant laterTime = Instant.ofEpochSecond(59 + 30);
            assertThat(totp.verify(SECRET_SHA1, code, laterTime, 1)).isTrue();
        }

        @Test
        void shouldRejectExpiredCode() {
            Totp totp = Totp.builder().algorithm("HmacSHA1").digits(8).period(30).build();
            Instant time = Instant.ofEpochSecond(59);
            String code = totp.generate(SECRET_SHA1, time);
            // 90 seconds later (3 windows), window=1 should not reach back
            Instant muchLater = Instant.ofEpochSecond(59 + 90);
            assertThat(totp.verify(SECRET_SHA1, code, muchLater, 1)).isFalse();
        }

        @Test
        void shouldRejectNegativeWindow() {
            assertThatThrownBy(() ->
                    Totp.sha1().verify(SECRET_SHA1, "123456", Instant.now(), -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldVerifyCurrentTimeDefaultWindow() {
            Totp totp = Totp.sha1();
            String code = totp.generate(SECRET_SHA1);
            assertThat(totp.verify(SECRET_SHA1, code)).isTrue();
        }

        @Test
        void shouldVerifyCurrentTimeExplicitWindow() {
            Totp totp = Totp.sha1();
            String code = totp.generate(SECRET_SHA1);
            assertThat(totp.verify(SECRET_SHA1, code, 1)).isTrue();
        }
    }

    @Nested
    class BuilderConfig {

        @Test
        void shouldConfigurePeriod() {
            Totp totp = Totp.builder().period(60).build();
            assertThat(totp.period()).isEqualTo(60);
        }

        @Test
        void shouldConfigureDigits() {
            Totp totp = Totp.builder().digits(8).build();
            assertThat(totp.digits()).isEqualTo(8);
        }

        @Test
        void shouldRejectInvalidPeriod() {
            assertThatThrownBy(() -> Totp.builder().period(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldRejectInvalidDigits() {
            assertThatThrownBy(() -> Totp.builder().digits(5))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldReturnAlgorithm() {
            assertThat(Totp.sha1().algorithm()).isEqualTo("HmacSHA1");
            assertThat(Totp.sha256().algorithm()).isEqualTo("HmacSHA256");
            assertThat(Totp.sha512().algorithm()).isEqualTo("HmacSHA512");
        }
    }

    @Nested
    class Uri {

        @Test
        void shouldGenerateDefaultUri() {
            byte[] secret = "HELLO".getBytes(StandardCharsets.US_ASCII);
            String uri = Totp.generateUri("MyApp", "user@example.com", secret);
            assertThat(uri).startsWith("otpauth://totp/");
            assertThat(uri).contains("secret=");
            assertThat(uri).contains("issuer=MyApp");
            assertThat(uri).contains("algorithm=SHA1");
            assertThat(uri).contains("digits=6");
            assertThat(uri).contains("period=30");
        }

        @Test
        void shouldGenerateCustomUri() {
            byte[] secret = "HELLO".getBytes(StandardCharsets.US_ASCII);
            String uri = Totp.generateUri("MyApp", "user@example.com", secret, "SHA256", 8, 60);
            assertThat(uri).contains("algorithm=SHA256");
            assertThat(uri).contains("digits=8");
            assertThat(uri).contains("period=60");
        }

        @Test
        void shouldRejectNullIssuer() {
            assertThatThrownBy(() -> Totp.generateUri(null, "user", new byte[20]))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
