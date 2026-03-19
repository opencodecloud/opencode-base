package cloud.opencode.base.email;

import jakarta.mail.Flags;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for EmailFlags
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailFlags")
class EmailFlagsTest {

    @Nested
    @DisplayName("Constants")
    class Constants {

        @Test
        @DisplayName("UNREAD should have correct flags")
        void unreadShouldHaveCorrectFlags() {
            assertThat(EmailFlags.UNREAD.seen()).isFalse();
            assertThat(EmailFlags.UNREAD.recent()).isTrue();
            assertThat(EmailFlags.UNREAD.isUnread()).isTrue();
        }

        @Test
        @DisplayName("READ should have correct flags")
        void readShouldHaveCorrectFlags() {
            assertThat(EmailFlags.READ.seen()).isTrue();
            assertThat(EmailFlags.READ.recent()).isFalse();
            assertThat(EmailFlags.READ.isUnread()).isFalse();
        }
    }

    @Nested
    @DisplayName("from()")
    class FromMethod {

        @Test
        @DisplayName("should create from Jakarta Mail Flags")
        void shouldCreateFromJakartaMailFlags() {
            Flags mailFlags = new Flags();
            mailFlags.add(Flags.Flag.SEEN);
            mailFlags.add(Flags.Flag.FLAGGED);
            mailFlags.add(Flags.Flag.ANSWERED);

            EmailFlags flags = EmailFlags.from(mailFlags);

            assertThat(flags.seen()).isTrue();
            assertThat(flags.flagged()).isTrue();
            assertThat(flags.answered()).isTrue();
            assertThat(flags.deleted()).isFalse();
            assertThat(flags.draft()).isFalse();
        }

        @Test
        @DisplayName("should return UNREAD for null flags")
        void shouldReturnUnreadForNullFlags() {
            EmailFlags flags = EmailFlags.from(null);

            assertThat(flags).isEqualTo(EmailFlags.UNREAD);
        }
    }

    @Nested
    @DisplayName("toMailFlags()")
    class ToMailFlagsMethod {

        @Test
        @DisplayName("should convert to Jakarta Mail Flags")
        void shouldConvertToJakartaMailFlags() {
            EmailFlags flags = new EmailFlags(true, true, true, false, false, false);

            Flags mailFlags = flags.toMailFlags();

            assertThat(mailFlags.contains(Flags.Flag.SEEN)).isTrue();
            assertThat(mailFlags.contains(Flags.Flag.ANSWERED)).isTrue();
            assertThat(mailFlags.contains(Flags.Flag.FLAGGED)).isTrue();
            assertThat(mailFlags.contains(Flags.Flag.DELETED)).isFalse();
        }
    }

    @Nested
    @DisplayName("with* methods")
    class WithMethods {

        @Test
        @DisplayName("should create copy with seen flag")
        void shouldCreateCopyWithSeenFlag() {
            EmailFlags original = EmailFlags.UNREAD;
            EmailFlags modified = original.withSeen(true);

            assertThat(original.seen()).isFalse();
            assertThat(modified.seen()).isTrue();
        }

        @Test
        @DisplayName("should create copy with flagged status")
        void shouldCreateCopyWithFlaggedStatus() {
            EmailFlags original = EmailFlags.UNREAD;
            EmailFlags modified = original.withFlagged(true);

            assertThat(original.flagged()).isFalse();
            assertThat(modified.flagged()).isTrue();
        }

        @Test
        @DisplayName("should create copy with deleted flag")
        void shouldCreateCopyWithDeletedFlag() {
            EmailFlags original = EmailFlags.READ;
            EmailFlags modified = original.withDeleted(true);

            assertThat(original.deleted()).isFalse();
            assertThat(modified.deleted()).isTrue();
        }

        @Test
        @DisplayName("should create copy with answered flag")
        void shouldCreateCopyWithAnsweredFlag() {
            EmailFlags original = EmailFlags.READ;
            EmailFlags modified = original.withAnswered(true);

            assertThat(original.answered()).isFalse();
            assertThat(modified.answered()).isTrue();
        }
    }
}
