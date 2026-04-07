package cloud.opencode.base.email;

import cloud.opencode.base.email.exception.EmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EmailAddress")
class EmailAddressTest {

    @Nested
    @DisplayName("of()")
    class Of {

        @Test
        @DisplayName("should create valid email address")
        void validEmail() {
            EmailAddress addr = EmailAddress.of("user@example.com");
            assertThat(addr.address()).isEqualTo("user@example.com");
            assertThat(addr.localPart()).isEqualTo("user");
            assertThat(addr.domain()).isEqualTo("example.com");
        }

        @Test
        @DisplayName("should normalize domain to lowercase")
        void normalizeDomain() {
            EmailAddress addr = EmailAddress.of("User@EXAMPLE.COM");
            assertThat(addr.address()).isEqualTo("User@example.com");
            assertThat(addr.localPart()).isEqualTo("User");
            assertThat(addr.domain()).isEqualTo("example.com");
        }

        @Test
        @DisplayName("should accept complex local parts")
        void complexLocalPart() {
            EmailAddress addr = EmailAddress.of("user.name+tag@example.co.uk");
            assertThat(addr.localPart()).isEqualTo("user.name+tag");
            assertThat(addr.domain()).isEqualTo("example.co.uk");
        }

        @Test
        @DisplayName("should reject null")
        void rejectNull() {
            assertThatThrownBy(() -> EmailAddress.of(null))
                    .isInstanceOf(EmailException.class);
        }

        @Test
        @DisplayName("should reject blank")
        void rejectBlank() {
            assertThatThrownBy(() -> EmailAddress.of("  "))
                    .isInstanceOf(EmailException.class);
        }

        @Test
        @DisplayName("should reject invalid format")
        void rejectInvalid() {
            assertThatThrownBy(() -> EmailAddress.of("invalid"))
                    .isInstanceOf(EmailException.class);
            assertThatThrownBy(() -> EmailAddress.of("user@"))
                    .isInstanceOf(EmailException.class);
            assertThatThrownBy(() -> EmailAddress.of("@domain.com"))
                    .isInstanceOf(EmailException.class);
        }

        @Test
        @DisplayName("should reject exceeding max length")
        void rejectTooLong() {
            String longLocal = "a".repeat(65) + "@example.com";
            assertThatThrownBy(() -> EmailAddress.of(longLocal))
                    .isInstanceOf(EmailException.class);
        }

        @Test
        @DisplayName("should strip whitespace")
        void stripWhitespace() {
            EmailAddress addr = EmailAddress.of("  user@example.com  ");
            assertThat(addr.address()).isEqualTo("user@example.com");
        }
    }

    @Nested
    @DisplayName("isValid()")
    class IsValid {

        @Test
        @DisplayName("should return true for valid addresses")
        void valid() {
            assertThat(EmailAddress.isValid("user@example.com")).isTrue();
            assertThat(EmailAddress.isValid("a.b+c@d.e.f")).isTrue();
        }

        @Test
        @DisplayName("should return false for invalid addresses")
        void invalid() {
            assertThat(EmailAddress.isValid(null)).isFalse();
            assertThat(EmailAddress.isValid("")).isFalse();
            assertThat(EmailAddress.isValid("invalid")).isFalse();
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("should be equal for same normalized address")
        void equalNormalized() {
            EmailAddress a = EmailAddress.of("user@EXAMPLE.COM");
            EmailAddress b = EmailAddress.of("user@example.com");
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different local parts")
        void notEqualDifferentLocal() {
            EmailAddress a = EmailAddress.of("user1@example.com");
            EmailAddress b = EmailAddress.of("user2@example.com");
            assertThat(a).isNotEqualTo(b);
        }
    }

    @Nested
    @DisplayName("compareTo")
    class CompareTo {

        @Test
        @DisplayName("should compare lexicographically")
        void compare() {
            EmailAddress a = EmailAddress.of("a@example.com");
            EmailAddress b = EmailAddress.of("b@example.com");
            assertThat(a.compareTo(b)).isLessThan(0);
            assertThat(b.compareTo(a)).isGreaterThan(0);
            assertThat(a.compareTo(EmailAddress.of("a@example.com"))).isZero();
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("should return address string")
        void toStringTest() {
            assertThat(EmailAddress.of("user@example.com").toString()).isEqualTo("user@example.com");
        }
    }
}
