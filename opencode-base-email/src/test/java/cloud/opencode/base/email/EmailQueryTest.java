package cloud.opencode.base.email;

import cloud.opencode.base.email.query.EmailFolder;
import cloud.opencode.base.email.query.EmailQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for EmailQuery
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailQuery")
class EmailQueryTest {

    @Nested
    @DisplayName("Builder")
    class Builder {

        @Test
        @DisplayName("should build query with defaults")
        void shouldBuildQueryWithDefaults() {
            EmailQuery query = EmailQuery.builder().build();

            assertThat(query.folder()).isEqualTo("INBOX");
            assertThat(query.limit()).isEqualTo(100);
            assertThat(query.offset()).isEqualTo(0);
            assertThat(query.sortOrder()).isEqualTo(EmailQuery.SortOrder.NEWEST_FIRST);
            assertThat(query.unreadOnly()).isFalse();
        }

        @Test
        @DisplayName("should build query with folder")
        void shouldBuildQueryWithFolder() {
            EmailQuery query = EmailQuery.builder()
                    .folder(EmailFolder.SENT)
                    .build();

            assertThat(query.folder()).isEqualTo("Sent");
        }

        @Test
        @DisplayName("should build query with date range")
        void shouldBuildQueryWithDateRange() {
            LocalDateTime from = LocalDateTime.now().minusDays(7);
            LocalDateTime to = LocalDateTime.now();

            EmailQuery query = EmailQuery.builder()
                    .dateRange(from, to)
                    .build();

            assertThat(query.fromDate()).isEqualTo(from);
            assertThat(query.toDate()).isEqualTo(to);
        }

        @Test
        @DisplayName("should build query with filters")
        void shouldBuildQueryWithFilters() {
            EmailQuery query = EmailQuery.builder()
                    .from("sender@example.com")
                    .to("recipient@example.com")
                    .subjectContains("Report")
                    .unreadOnly()
                    .flaggedOnly()
                    .hasAttachments()
                    .build();

            assertThat(query.from()).containsExactly("sender@example.com");
            assertThat(query.to()).containsExactly("recipient@example.com");
            assertThat(query.subjectContains()).isEqualTo("Report");
            assertThat(query.unreadOnly()).isTrue();
            assertThat(query.flaggedOnly()).isTrue();
            assertThat(query.hasAttachments()).isTrue();
        }

        @Test
        @DisplayName("should build query with pagination")
        void shouldBuildQueryWithPagination() {
            EmailQuery query = EmailQuery.builder()
                    .limit(50)
                    .offset(100)
                    .build();

            assertThat(query.limit()).isEqualTo(50);
            assertThat(query.offset()).isEqualTo(100);
        }

        @Test
        @DisplayName("should build query with sort order")
        void shouldBuildQueryWithSortOrder() {
            EmailQuery query = EmailQuery.builder()
                    .sortBy(EmailQuery.SortOrder.OLDEST_FIRST)
                    .build();

            assertThat(query.sortOrder()).isEqualTo(EmailQuery.SortOrder.OLDEST_FIRST);
        }
    }

    @Nested
    @DisplayName("Factory methods")
    class FactoryMethods {

        @Test
        @DisplayName("should create unread query")
        void shouldCreateUnreadQuery() {
            EmailQuery query = EmailQuery.unread();

            assertThat(query.unreadOnly()).isTrue();
        }

        @Test
        @DisplayName("should create folder query")
        void shouldCreateFolderQuery() {
            EmailQuery query = EmailQuery.forFolder(EmailFolder.DRAFTS);

            assertThat(query.folder()).isEqualTo("Drafts");
        }

        @Test
        @DisplayName("should create folder query by name")
        void shouldCreateFolderQueryByName() {
            EmailQuery query = EmailQuery.forFolder("Archive");

            assertThat(query.folder()).isEqualTo("Archive");
        }
    }

    @Nested
    @DisplayName("hasFilters")
    class HasFilters {

        @Test
        @DisplayName("should return false when no filters")
        void shouldReturnFalseWhenNoFilters() {
            EmailQuery query = EmailQuery.builder().build();

            assertThat(query.hasFilters()).isFalse();
        }

        @Test
        @DisplayName("should return true when has date filter")
        void shouldReturnTrueWhenHasDateFilter() {
            EmailQuery query = EmailQuery.builder()
                    .fromDate(LocalDateTime.now().minusDays(1))
                    .build();

            assertThat(query.hasFilters()).isTrue();
        }

        @Test
        @DisplayName("should return true when has unread filter")
        void shouldReturnTrueWhenHasUnreadFilter() {
            EmailQuery query = EmailQuery.builder()
                    .unreadOnly()
                    .build();

            assertThat(query.hasFilters()).isTrue();
        }
    }
}
