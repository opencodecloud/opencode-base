package cloud.opencode.base.email;

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
    @DisplayName("fromImapFlags()")
    class FromImapFlagsMethod {

        @Test
        @DisplayName("should create from IMAP flags string")
        void shouldCreateFromImapFlagsString() {
            EmailFlags flags = EmailFlags.fromImapFlags("(\\Seen \\Flagged \\Answered)");

            assertThat(flags.seen()).isTrue();
            assertThat(flags.flagged()).isTrue();
            assertThat(flags.answered()).isTrue();
            assertThat(flags.deleted()).isFalse();
            assertThat(flags.draft()).isFalse();
        }

        @Test
        @DisplayName("should return UNREAD for null flags")
        void shouldReturnUnreadForNullFlags() {
            EmailFlags flags = EmailFlags.fromImapFlags(null);

            assertThat(flags).isEqualTo(EmailFlags.UNREAD);
        }

        @Test
        @DisplayName("should return UNREAD for blank flags")
        void shouldReturnUnreadForBlankFlags() {
            EmailFlags flags = EmailFlags.fromImapFlags("");

            assertThat(flags).isEqualTo(EmailFlags.UNREAD);
        }

        @Test
        @DisplayName("should parse all flag types")
        void shouldParseAllFlagTypes() {
            EmailFlags flags = EmailFlags.fromImapFlags(
                    "(\\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent)");

            assertThat(flags.seen()).isTrue();
            assertThat(flags.answered()).isTrue();
            assertThat(flags.flagged()).isTrue();
            assertThat(flags.deleted()).isTrue();
            assertThat(flags.draft()).isTrue();
            assertThat(flags.recent()).isTrue();
        }
    }

    @Nested
    @DisplayName("toImapFlags()")
    class ToImapFlagsMethod {

        @Test
        @DisplayName("should convert to IMAP flags string")
        void shouldConvertToImapFlagsString() {
            EmailFlags flags = new EmailFlags(true, true, true, false, false, false);

            String imapFlags = flags.toImapFlags();

            assertThat(imapFlags).contains("\\Seen");
            assertThat(imapFlags).contains("\\Answered");
            assertThat(imapFlags).contains("\\Flagged");
            assertThat(imapFlags).doesNotContain("\\Deleted");
            assertThat(imapFlags).startsWith("(");
            assertThat(imapFlags).endsWith(")");
        }

        @Test
        @DisplayName("should round-trip through IMAP flags string")
        void shouldRoundTripThroughImapFlagsString() {
            EmailFlags original = new EmailFlags(true, false, true, false, true, false);

            String imapFlags = original.toImapFlags();
            EmailFlags restored = EmailFlags.fromImapFlags(imapFlags);

            assertThat(restored.seen()).isEqualTo(original.seen());
            assertThat(restored.answered()).isEqualTo(original.answered());
            assertThat(restored.flagged()).isEqualTo(original.flagged());
            assertThat(restored.deleted()).isEqualTo(original.deleted());
            assertThat(restored.draft()).isEqualTo(original.draft());
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
