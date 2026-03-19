package cloud.opencode.base.email.query;

import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailQueryTest Tests
 * EmailQueryTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailQuery Tests")
class EmailQueryTest {

    private LocalDateTime now;
    private LocalDateTime weekAgo;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        weekAgo = now.minusDays(7);
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("builder should create new builder")
        void builderShouldCreateNewBuilder() {
            EmailQuery.Builder builder = EmailQuery.builder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("unread should create query for unread emails")
        void unreadShouldCreateQueryForUnreadEmails() {
            EmailQuery query = EmailQuery.unread();

            assertThat(query.unreadOnly()).isTrue();
            assertThat(query.folder()).isEqualTo("INBOX");
        }

        @Test
        @DisplayName("forFolder with EmailFolder should create query")
        void forFolderWithEmailFolderShouldCreateQuery() {
            EmailQuery query = EmailQuery.forFolder(EmailFolder.SENT);

            assertThat(query.folder()).isEqualTo("Sent");
        }

        @Test
        @DisplayName("forFolder with String should create query")
        void forFolderWithStringShouldCreateQuery() {
            EmailQuery query = EmailQuery.forFolder("Custom/MyFolder");

            assertThat(query.folder()).isEqualTo("Custom/MyFolder");
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("build should create query with defaults")
        void buildShouldCreateQueryWithDefaults() {
            EmailQuery query = EmailQuery.builder().build();

            assertThat(query.folder()).isEqualTo("INBOX");
            assertThat(query.limit()).isEqualTo(100);
            assertThat(query.offset()).isEqualTo(0);
            assertThat(query.sortOrder()).isEqualTo(EmailQuery.SortOrder.NEWEST_FIRST);
            assertThat(query.unreadOnly()).isFalse();
            assertThat(query.flaggedOnly()).isFalse();
            assertThat(query.hasAttachments()).isFalse();
            assertThat(query.includeDeleted()).isFalse();
        }

        @Test
        @DisplayName("folder with EmailFolder should set folder")
        void folderWithEmailFolderShouldSetFolder() {
            EmailQuery query = EmailQuery.builder()
                .folder(EmailFolder.INBOX)
                .build();

            assertThat(query.folder()).isEqualTo("INBOX");
        }

        @Test
        @DisplayName("folder with String should set folder")
        void folderWithStringShouldSetFolder() {
            EmailQuery query = EmailQuery.builder()
                .folder("MyFolder")
                .build();

            assertThat(query.folder()).isEqualTo("MyFolder");
        }

        @Test
        @DisplayName("fromDate should set minimum date")
        void fromDateShouldSetMinimumDate() {
            EmailQuery query = EmailQuery.builder()
                .fromDate(weekAgo)
                .build();

            assertThat(query.fromDate()).isEqualTo(weekAgo);
        }

        @Test
        @DisplayName("toDate should set maximum date")
        void toDateShouldSetMaximumDate() {
            EmailQuery query = EmailQuery.builder()
                .toDate(now)
                .build();

            assertThat(query.toDate()).isEqualTo(now);
        }

        @Test
        @DisplayName("dateRange should set both dates")
        void dateRangeShouldSetBothDates() {
            EmailQuery query = EmailQuery.builder()
                .dateRange(weekAgo, now)
                .build();

            assertThat(query.fromDate()).isEqualTo(weekAgo);
            assertThat(query.toDate()).isEqualTo(now);
        }

        @Test
        @DisplayName("from with single address should set sender filter")
        void fromWithSingleAddressShouldSetSenderFilter() {
            EmailQuery query = EmailQuery.builder()
                .from("sender@example.com")
                .build();

            assertThat(query.from()).containsExactly("sender@example.com");
        }

        @Test
        @DisplayName("from with Set should set multiple senders")
        void fromWithSetShouldSetMultipleSenders() {
            Set<String> senders = Set.of("a@example.com", "b@example.com");
            EmailQuery query = EmailQuery.builder()
                .from(senders)
                .build();

            assertThat(query.from()).containsExactlyInAnyOrderElementsOf(senders);
        }

        @Test
        @DisplayName("from with null should clear filter")
        void fromWithNullShouldClearFilter() {
            EmailQuery query = EmailQuery.builder()
                .from((Set<String>) null)
                .build();

            assertThat(query.from()).isNull();
        }

        @Test
        @DisplayName("to with single address should set recipient filter")
        void toWithSingleAddressShouldSetRecipientFilter() {
            EmailQuery query = EmailQuery.builder()
                .to("recipient@example.com")
                .build();

            assertThat(query.to()).containsExactly("recipient@example.com");
        }

        @Test
        @DisplayName("to with Set should set multiple recipients")
        void toWithSetShouldSetMultipleRecipients() {
            Set<String> recipients = Set.of("a@example.com", "b@example.com");
            EmailQuery query = EmailQuery.builder()
                .to(recipients)
                .build();

            assertThat(query.to()).containsExactlyInAnyOrderElementsOf(recipients);
        }

        @Test
        @DisplayName("to with null should clear filter")
        void toWithNullShouldClearFilter() {
            EmailQuery query = EmailQuery.builder()
                .to((Set<String>) null)
                .build();

            assertThat(query.to()).isNull();
        }

        @Test
        @DisplayName("subjectContains should set subject filter")
        void subjectContainsShouldSetSubjectFilter() {
            EmailQuery query = EmailQuery.builder()
                .subjectContains("Report")
                .build();

            assertThat(query.subjectContains()).isEqualTo("Report");
        }

        @Test
        @DisplayName("bodyContains should set body filter")
        void bodyContainsShouldSetBodyFilter() {
            EmailQuery query = EmailQuery.builder()
                .bodyContains("important")
                .build();

            assertThat(query.bodyContains()).isEqualTo("important");
        }

        @Test
        @DisplayName("unreadOnly without arg should enable filter")
        void unreadOnlyWithoutArgShouldEnableFilter() {
            EmailQuery query = EmailQuery.builder()
                .unreadOnly()
                .build();

            assertThat(query.unreadOnly()).isTrue();
        }

        @Test
        @DisplayName("unreadOnly with boolean should set filter")
        void unreadOnlyWithBooleanShouldSetFilter() {
            EmailQuery query = EmailQuery.builder()
                .unreadOnly(true)
                .build();

            assertThat(query.unreadOnly()).isTrue();
        }

        @Test
        @DisplayName("flaggedOnly without arg should enable filter")
        void flaggedOnlyWithoutArgShouldEnableFilter() {
            EmailQuery query = EmailQuery.builder()
                .flaggedOnly()
                .build();

            assertThat(query.flaggedOnly()).isTrue();
        }

        @Test
        @DisplayName("flaggedOnly with boolean should set filter")
        void flaggedOnlyWithBooleanShouldSetFilter() {
            EmailQuery query = EmailQuery.builder()
                .flaggedOnly(false)
                .build();

            assertThat(query.flaggedOnly()).isFalse();
        }

        @Test
        @DisplayName("hasAttachments without arg should enable filter")
        void hasAttachmentsWithoutArgShouldEnableFilter() {
            EmailQuery query = EmailQuery.builder()
                .hasAttachments()
                .build();

            assertThat(query.hasAttachments()).isTrue();
        }

        @Test
        @DisplayName("hasAttachments with boolean should set filter")
        void hasAttachmentsWithBooleanShouldSetFilter() {
            EmailQuery query = EmailQuery.builder()
                .hasAttachments(true)
                .build();

            assertThat(query.hasAttachments()).isTrue();
        }

        @Test
        @DisplayName("includeDeleted without arg should enable filter")
        void includeDeletedWithoutArgShouldEnableFilter() {
            EmailQuery query = EmailQuery.builder()
                .includeDeleted()
                .build();

            assertThat(query.includeDeleted()).isTrue();
        }

        @Test
        @DisplayName("includeDeleted with boolean should set filter")
        void includeDeletedWithBooleanShouldSetFilter() {
            EmailQuery query = EmailQuery.builder()
                .includeDeleted(false)
                .build();

            assertThat(query.includeDeleted()).isFalse();
        }

        @Test
        @DisplayName("limit should set maximum count")
        void limitShouldSetMaximumCount() {
            EmailQuery query = EmailQuery.builder()
                .limit(50)
                .build();

            assertThat(query.limit()).isEqualTo(50);
        }

        @Test
        @DisplayName("offset should set pagination offset")
        void offsetShouldSetPaginationOffset() {
            EmailQuery query = EmailQuery.builder()
                .offset(20)
                .build();

            assertThat(query.offset()).isEqualTo(20);
        }

        @Test
        @DisplayName("page should set limit and offset")
        void pageShouldSetLimitAndOffset() {
            EmailQuery query = EmailQuery.builder()
                .page(25, 50)
                .build();

            assertThat(query.limit()).isEqualTo(25);
            assertThat(query.offset()).isEqualTo(50);
        }

        @Test
        @DisplayName("sortBy should set sort order")
        void sortByShouldSetSortOrder() {
            EmailQuery query = EmailQuery.builder()
                .sortBy(EmailQuery.SortOrder.SUBJECT_ASC)
                .build();

            assertThat(query.sortOrder()).isEqualTo(EmailQuery.SortOrder.SUBJECT_ASC);
        }

        @Test
        @DisplayName("newestFirst should set sort order")
        void newestFirstShouldSetSortOrder() {
            EmailQuery query = EmailQuery.builder()
                .newestFirst()
                .build();

            assertThat(query.sortOrder()).isEqualTo(EmailQuery.SortOrder.NEWEST_FIRST);
        }

        @Test
        @DisplayName("oldestFirst should set sort order")
        void oldestFirstShouldSetSortOrder() {
            EmailQuery query = EmailQuery.builder()
                .oldestFirst()
                .build();

            assertThat(query.sortOrder()).isEqualTo(EmailQuery.SortOrder.OLDEST_FIRST);
        }
    }

    @Nested
    @DisplayName("hasFilters Tests")
    class HasFiltersTests {

        @Test
        @DisplayName("hasFilters should return false for default query")
        void hasFiltersShouldReturnFalseForDefaultQuery() {
            EmailQuery query = EmailQuery.builder().build();

            assertThat(query.hasFilters()).isFalse();
        }

        @Test
        @DisplayName("hasFilters should return true for fromDate filter")
        void hasFiltersShouldReturnTrueForFromDateFilter() {
            EmailQuery query = EmailQuery.builder()
                .fromDate(weekAgo)
                .build();

            assertThat(query.hasFilters()).isTrue();
        }

        @Test
        @DisplayName("hasFilters should return true for toDate filter")
        void hasFiltersShouldReturnTrueForToDateFilter() {
            EmailQuery query = EmailQuery.builder()
                .toDate(now)
                .build();

            assertThat(query.hasFilters()).isTrue();
        }

        @Test
        @DisplayName("hasFilters should return true for from filter")
        void hasFiltersShouldReturnTrueForFromFilter() {
            EmailQuery query = EmailQuery.builder()
                .from("test@example.com")
                .build();

            assertThat(query.hasFilters()).isTrue();
        }

        @Test
        @DisplayName("hasFilters should return true for to filter")
        void hasFiltersShouldReturnTrueForToFilter() {
            EmailQuery query = EmailQuery.builder()
                .to("test@example.com")
                .build();

            assertThat(query.hasFilters()).isTrue();
        }

        @Test
        @DisplayName("hasFilters should return true for subject filter")
        void hasFiltersShouldReturnTrueForSubjectFilter() {
            EmailQuery query = EmailQuery.builder()
                .subjectContains("test")
                .build();

            assertThat(query.hasFilters()).isTrue();
        }

        @Test
        @DisplayName("hasFilters should return true for body filter")
        void hasFiltersShouldReturnTrueForBodyFilter() {
            EmailQuery query = EmailQuery.builder()
                .bodyContains("test")
                .build();

            assertThat(query.hasFilters()).isTrue();
        }

        @Test
        @DisplayName("hasFilters should return true for unreadOnly")
        void hasFiltersShouldReturnTrueForUnreadOnly() {
            EmailQuery query = EmailQuery.builder()
                .unreadOnly()
                .build();

            assertThat(query.hasFilters()).isTrue();
        }

        @Test
        @DisplayName("hasFilters should return true for flaggedOnly")
        void hasFiltersShouldReturnTrueForFlaggedOnly() {
            EmailQuery query = EmailQuery.builder()
                .flaggedOnly()
                .build();

            assertThat(query.hasFilters()).isTrue();
        }

        @Test
        @DisplayName("hasFilters should return true for hasAttachments")
        void hasFiltersShouldReturnTrueForHasAttachments() {
            EmailQuery query = EmailQuery.builder()
                .hasAttachments()
                .build();

            assertThat(query.hasFilters()).isTrue();
        }
    }

    @Nested
    @DisplayName("SortOrder Enum Tests")
    class SortOrderEnumTests {

        @Test
        @DisplayName("should have all expected values")
        void shouldHaveAllExpectedValues() {
            assertThat(EmailQuery.SortOrder.values()).containsExactly(
                EmailQuery.SortOrder.NEWEST_FIRST,
                EmailQuery.SortOrder.OLDEST_FIRST,
                EmailQuery.SortOrder.SUBJECT_ASC,
                EmailQuery.SortOrder.SUBJECT_DESC,
                EmailQuery.SortOrder.SENDER_ASC,
                EmailQuery.SortOrder.SENDER_DESC
            );
        }

        @Test
        @DisplayName("valueOf should return correct enum")
        void valueOfShouldReturnCorrectEnum() {
            assertThat(EmailQuery.SortOrder.valueOf("NEWEST_FIRST"))
                .isEqualTo(EmailQuery.SortOrder.NEWEST_FIRST);
        }
    }

    @Nested
    @DisplayName("Record Tests")
    class RecordTests {

        @Test
        @DisplayName("record components should be accessible")
        void recordComponentsShouldBeAccessible() {
            EmailQuery query = new EmailQuery(
                "INBOX",
                weekAgo,
                now,
                Set.of("from@test.com"),
                Set.of("to@test.com"),
                "subject",
                "body",
                true,
                true,
                true,
                false,
                50,
                10,
                EmailQuery.SortOrder.OLDEST_FIRST
            );

            assertThat(query.folder()).isEqualTo("INBOX");
            assertThat(query.fromDate()).isEqualTo(weekAgo);
            assertThat(query.toDate()).isEqualTo(now);
            assertThat(query.from()).containsExactly("from@test.com");
            assertThat(query.to()).containsExactly("to@test.com");
            assertThat(query.subjectContains()).isEqualTo("subject");
            assertThat(query.bodyContains()).isEqualTo("body");
            assertThat(query.unreadOnly()).isTrue();
            assertThat(query.flaggedOnly()).isTrue();
            assertThat(query.hasAttachments()).isTrue();
            assertThat(query.includeDeleted()).isFalse();
            assertThat(query.limit()).isEqualTo(50);
            assertThat(query.offset()).isEqualTo(10);
            assertThat(query.sortOrder()).isEqualTo(EmailQuery.SortOrder.OLDEST_FIRST);
        }
    }

    @Nested
    @DisplayName("Complex Query Tests")
    class ComplexQueryTests {

        @Test
        @DisplayName("should build complex query with multiple filters")
        void shouldBuildComplexQueryWithMultipleFilters() {
            EmailQuery query = EmailQuery.builder()
                .folder(EmailFolder.INBOX)
                .dateRange(weekAgo, now)
                .from("sender@example.com")
                .subjectContains("Report")
                .unreadOnly()
                .hasAttachments()
                .limit(50)
                .newestFirst()
                .build();

            assertThat(query.folder()).isEqualTo("INBOX");
            assertThat(query.fromDate()).isEqualTo(weekAgo);
            assertThat(query.toDate()).isEqualTo(now);
            assertThat(query.from()).containsExactly("sender@example.com");
            assertThat(query.subjectContains()).isEqualTo("Report");
            assertThat(query.unreadOnly()).isTrue();
            assertThat(query.hasAttachments()).isTrue();
            assertThat(query.limit()).isEqualTo(50);
            assertThat(query.sortOrder()).isEqualTo(EmailQuery.SortOrder.NEWEST_FIRST);
            assertThat(query.hasFilters()).isTrue();
        }
    }
}
