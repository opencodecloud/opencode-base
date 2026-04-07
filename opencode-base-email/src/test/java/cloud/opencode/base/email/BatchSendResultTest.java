package cloud.opencode.base.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BatchSendResult")
class BatchSendResultTest {

    private Email createEmail(String to) {
        return Email.builder().from("s@test.com").to(to).subject("Test").text("body").build();
    }

    @Nested
    @DisplayName("counts")
    class Counts {

        @Test
        @DisplayName("should count successes and failures")
        void countSuccessFailure() {
            Email e1 = createEmail("a@test.com");
            Email e2 = createEmail("b@test.com");
            Email e3 = createEmail("c@test.com");
            List<BatchSendResult.ItemResult> items = List.of(
                    BatchSendResult.ItemResult.success(e1, "id1"),
                    BatchSendResult.ItemResult.failure(e2, new RuntimeException("fail")),
                    BatchSendResult.ItemResult.success(e3, "id3")
            );
            BatchSendResult result = new BatchSendResult(items, Instant.now(), Duration.ofMillis(100));

            assertThat(result.totalCount()).isEqualTo(3);
            assertThat(result.successCount()).isEqualTo(2);
            assertThat(result.failureCount()).isEqualTo(1);
            assertThat(result.allSucceeded()).isFalse();
        }

        @Test
        @DisplayName("allSucceeded should be true when all pass")
        void allSucceeded() {
            Email e1 = createEmail("a@test.com");
            List<BatchSendResult.ItemResult> items = List.of(
                    BatchSendResult.ItemResult.success(e1, "id1")
            );
            BatchSendResult result = new BatchSendResult(items, Instant.now(), Duration.ofMillis(10));
            assertThat(result.allSucceeded()).isTrue();
        }
    }

    @Nested
    @DisplayName("filtering")
    class Filtering {

        @Test
        @DisplayName("failures should return only failed items")
        void failures() {
            Email e1 = createEmail("a@test.com");
            Email e2 = createEmail("b@test.com");
            List<BatchSendResult.ItemResult> items = List.of(
                    BatchSendResult.ItemResult.success(e1, "id1"),
                    BatchSendResult.ItemResult.failure(e2, new RuntimeException("err"))
            );
            BatchSendResult result = new BatchSendResult(items, Instant.now(), Duration.ofMillis(50));

            assertThat(result.failures()).hasSize(1);
            assertThat(result.failures().getFirst().email()).isEqualTo(e2);
            assertThat(result.successes()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("empty batch")
    class EmptyBatch {

        @Test
        @DisplayName("should handle empty results")
        void emptyResults() {
            BatchSendResult result = new BatchSendResult(List.of(), Instant.now(), Duration.ZERO);
            assertThat(result.totalCount()).isZero();
            assertThat(result.successCount()).isZero();
            assertThat(result.allSucceeded()).isTrue();
        }
    }

    @Nested
    @DisplayName("ItemResult")
    class ItemResultTest {

        @Test
        @DisplayName("success result has correct fields")
        void successFields() {
            Email email = createEmail("a@test.com");
            BatchSendResult.ItemResult item = BatchSendResult.ItemResult.success(email, "msg-id");
            assertThat(item.success()).isTrue();
            assertThat(item.messageId()).isEqualTo("msg-id");
            assertThat(item.error()).isNull();
            assertThat(item.cause()).isNull();
        }

        @Test
        @DisplayName("failure result has correct fields")
        void failureFields() {
            Email email = createEmail("a@test.com");
            RuntimeException cause = new RuntimeException("oops");
            BatchSendResult.ItemResult item = BatchSendResult.ItemResult.failure(email, cause);
            assertThat(item.success()).isFalse();
            assertThat(item.messageId()).isNull();
            assertThat(item.error()).isEqualTo("oops");
            assertThat(item.cause()).isEqualTo(cause);
        }
    }
}
