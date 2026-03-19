package cloud.opencode.base.email.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailFolder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailFolder 测试")
class EmailFolderTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValueTests {

        @Test
        @DisplayName("所有枚举值存在")
        void testAllEnumValues() {
            assertThat(EmailFolder.values()).contains(
                    EmailFolder.INBOX,
                    EmailFolder.SENT,
                    EmailFolder.DRAFTS,
                    EmailFolder.TRASH,
                    EmailFolder.SPAM,
                    EmailFolder.ARCHIVE,
                    EmailFolder.STARRED,
                    EmailFolder.IMPORTANT
            );
        }

        @Test
        @DisplayName("枚举值数量正确")
        void testEnumCount() {
            assertThat(EmailFolder.values()).hasSize(8);
        }
    }

    @Nested
    @DisplayName("getName() 测试")
    class GetNameTests {

        @Test
        @DisplayName("INBOX返回正确名称")
        void testInboxName() {
            assertThat(EmailFolder.INBOX.getName()).isEqualTo("INBOX");
        }

        @Test
        @DisplayName("SENT返回正确名称")
        void testSentName() {
            assertThat(EmailFolder.SENT.getName()).isEqualTo("Sent");
        }

        @Test
        @DisplayName("DRAFTS返回正确名称")
        void testDraftsName() {
            assertThat(EmailFolder.DRAFTS.getName()).isEqualTo("Drafts");
        }

        @Test
        @DisplayName("TRASH返回正确名称")
        void testTrashName() {
            assertThat(EmailFolder.TRASH.getName()).isEqualTo("Trash");
        }
    }

    @Nested
    @DisplayName("getAlternativeNames() 测试")
    class GetAlternativeNamesTests {

        @Test
        @DisplayName("INBOX有可选名称")
        void testInboxAlternatives() {
            String[] alternatives = EmailFolder.INBOX.getAlternativeNames();
            assertThat(alternatives).contains("Inbox");
        }

        @Test
        @DisplayName("SENT有多个可选名称")
        void testSentAlternatives() {
            String[] alternatives = EmailFolder.SENT.getAlternativeNames();
            assertThat(alternatives).contains("Sent Items", "Sent Messages", "[Gmail]/Sent Mail");
        }

        @Test
        @DisplayName("返回数组副本")
        void testReturnsCopy() {
            String[] alt1 = EmailFolder.INBOX.getAlternativeNames();
            String[] alt2 = EmailFolder.INBOX.getAlternativeNames();
            assertThat(alt1).isNotSameAs(alt2);
        }
    }

    @Nested
    @DisplayName("getAllNames() 测试")
    class GetAllNamesTests {

        @Test
        @DisplayName("包含主要名称和可选名称")
        void testAllNamesIncludesPrimaryAndAlternatives() {
            String[] all = EmailFolder.SENT.getAllNames();
            assertThat(all[0]).isEqualTo("Sent");
            assertThat(all).contains("Sent Items", "Sent Messages");
        }

        @Test
        @DisplayName("INBOX所有名称")
        void testInboxAllNames() {
            String[] all = EmailFolder.INBOX.getAllNames();
            assertThat(all).contains("INBOX", "Inbox");
        }
    }

    @Nested
    @DisplayName("fromName() 测试")
    class FromNameTests {

        @Test
        @DisplayName("主要名称匹配")
        void testPrimaryNameMatch() {
            assertThat(EmailFolder.fromName("INBOX")).isEqualTo(EmailFolder.INBOX);
            assertThat(EmailFolder.fromName("Sent")).isEqualTo(EmailFolder.SENT);
        }

        @Test
        @DisplayName("可选名称匹配")
        void testAlternativeNameMatch() {
            assertThat(EmailFolder.fromName("Sent Items")).isEqualTo(EmailFolder.SENT);
            assertThat(EmailFolder.fromName("[Gmail]/Sent Mail")).isEqualTo(EmailFolder.SENT);
        }

        @Test
        @DisplayName("不区分大小写")
        void testCaseInsensitive() {
            assertThat(EmailFolder.fromName("inbox")).isEqualTo(EmailFolder.INBOX);
            assertThat(EmailFolder.fromName("SENT ITEMS")).isEqualTo(EmailFolder.SENT);
        }

        @Test
        @DisplayName("null返回null")
        void testNullReturnsNull() {
            assertThat(EmailFolder.fromName(null)).isNull();
        }

        @Test
        @DisplayName("空白返回null")
        void testBlankReturnsNull() {
            assertThat(EmailFolder.fromName("")).isNull();
            assertThat(EmailFolder.fromName("   ")).isNull();
        }

        @Test
        @DisplayName("未知名称返回null")
        void testUnknownReturnsNull() {
            assertThat(EmailFolder.fromName("Unknown Folder")).isNull();
        }
    }

    @Nested
    @DisplayName("matches() 测试")
    class MatchesTests {

        @Test
        @DisplayName("匹配主要名称")
        void testMatchesPrimaryName() {
            assertThat(EmailFolder.INBOX.matches("INBOX")).isTrue();
            assertThat(EmailFolder.SENT.matches("Sent")).isTrue();
        }

        @Test
        @DisplayName("匹配可选名称")
        void testMatchesAlternativeName() {
            assertThat(EmailFolder.SENT.matches("Sent Items")).isTrue();
            assertThat(EmailFolder.TRASH.matches("Deleted Items")).isTrue();
        }

        @Test
        @DisplayName("不区分大小写")
        void testMatchesCaseInsensitive() {
            assertThat(EmailFolder.INBOX.matches("inbox")).isTrue();
            assertThat(EmailFolder.SENT.matches("SENT")).isTrue();
        }

        @Test
        @DisplayName("null不匹配")
        void testNullDoesNotMatch() {
            assertThat(EmailFolder.INBOX.matches(null)).isFalse();
        }

        @Test
        @DisplayName("不匹配的名称")
        void testDoesNotMatch() {
            assertThat(EmailFolder.INBOX.matches("Sent")).isFalse();
            assertThat(EmailFolder.SENT.matches("INBOX")).isFalse();
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("返回主要名称")
        void testReturnsPrimaryName() {
            assertThat(EmailFolder.INBOX.toString()).isEqualTo("INBOX");
            assertThat(EmailFolder.SENT.toString()).isEqualTo("Sent");
            assertThat(EmailFolder.DRAFTS.toString()).isEqualTo("Drafts");
        }
    }

    @Nested
    @DisplayName("Gmail文件夹名称测试")
    class GmailFolderTests {

        @Test
        @DisplayName("Gmail发件箱名称")
        void testGmailSentFolder() {
            assertThat(EmailFolder.fromName("[Gmail]/Sent Mail")).isEqualTo(EmailFolder.SENT);
        }

        @Test
        @DisplayName("Gmail垃圾箱名称")
        void testGmailTrashFolder() {
            assertThat(EmailFolder.fromName("[Gmail]/Trash")).isEqualTo(EmailFolder.TRASH);
        }

        @Test
        @DisplayName("Gmail垃圾邮件名称")
        void testGmailSpamFolder() {
            assertThat(EmailFolder.fromName("[Gmail]/Spam")).isEqualTo(EmailFolder.SPAM);
        }

        @Test
        @DisplayName("Gmail草稿箱名称")
        void testGmailDraftsFolder() {
            assertThat(EmailFolder.fromName("[Gmail]/Drafts")).isEqualTo(EmailFolder.DRAFTS);
        }
    }
}
