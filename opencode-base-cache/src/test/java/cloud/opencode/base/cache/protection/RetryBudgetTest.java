package cloud.opencode.base.cache.protection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RetryBudget")
class RetryBudgetTest {

    @Nested
    @DisplayName("ofRatio")
    class OfRatio {

        @Test
        @DisplayName("should create budget with valid ratio")
        void shouldCreateWithValidRatio() {
            RetryBudget budget = RetryBudget.ofRatio(0.20);
            assertThat(budget.getRetryRatio()).isEqualTo(0.20);
        }

        @Test
        @DisplayName("should throw for ratio <= 0")
        void shouldThrowForZeroRatio() {
            assertThatThrownBy(() -> RetryBudget.ofRatio(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw for ratio >= 1")
        void shouldThrowForOneRatio() {
            assertThatThrownBy(() -> RetryBudget.ofRatio(1.0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw for negative ratio")
        void shouldThrowForNegativeRatio() {
            assertThatThrownBy(() -> RetryBudget.ofRatio(-0.5))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("ofPercent")
    class OfPercent {

        @Test
        @DisplayName("should create budget with valid percent")
        void shouldCreateWithValidPercent() {
            RetryBudget budget = RetryBudget.ofPercent(20);
            assertThat(budget.getRetryRatio()).isEqualTo(0.20);
        }

        @Test
        @DisplayName("should throw for percent <= 0")
        void shouldThrowForZeroPercent() {
            assertThatThrownBy(() -> RetryBudget.ofPercent(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw for percent >= 100")
        void shouldThrowFor100Percent() {
            assertThatThrownBy(() -> RetryBudget.ofPercent(100))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("unlimited")
    class Unlimited {

        @Test
        @DisplayName("should create budget with high ratio")
        void shouldCreateUnlimited() {
            RetryBudget budget = RetryBudget.unlimited();
            assertThat(budget.getRetryRatio()).isEqualTo(0.9999);
        }
    }

    @Nested
    @DisplayName("recordRequest")
    class RecordRequest {

        @Test
        @DisplayName("should increment total requests")
        void shouldIncrementRequests() {
            RetryBudget budget = RetryBudget.ofRatio(0.20);
            budget.recordRequest();
            budget.recordRequest();
            assertThat(budget.getTotalRequests()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("canRetry")
    class CanRetry {

        @Test
        @DisplayName("should allow retry when no requests recorded")
        void shouldAllowRetryInitially() {
            RetryBudget budget = RetryBudget.ofRatio(0.20);
            assertThat(budget.canRetry()).isTrue();
        }

        @Test
        @DisplayName("should allow retry within budget")
        void shouldAllowRetryWithinBudget() {
            RetryBudget budget = RetryBudget.ofRatio(0.50);
            budget.recordRequest();
            budget.recordRequest();
            budget.recordRequest();
            budget.recordRequest();
            // 0 retries out of 4 requests, well within 50%
            assertThat(budget.canRetry()).isTrue();
        }

        @Test
        @DisplayName("should deny retry when budget exceeded")
        void shouldDenyRetryWhenExceeded() {
            RetryBudget budget = RetryBudget.ofRatio(0.20);
            budget.recordRequest();
            budget.recordRetry();
            // 1 retry out of 1 request = 100% > 20%
            assertThat(budget.canRetry()).isFalse();
        }
    }

    @Nested
    @DisplayName("recordRetry")
    class RecordRetry {

        @Test
        @DisplayName("should increment total retries")
        void shouldIncrementRetries() {
            RetryBudget budget = RetryBudget.ofRatio(0.50);
            budget.recordRetry();
            budget.recordRetry();
            assertThat(budget.getTotalRetries()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("currentRetryRatio")
    class CurrentRetryRatio {

        @Test
        @DisplayName("should return 0 when no requests")
        void shouldReturnZeroInitially() {
            RetryBudget budget = RetryBudget.ofRatio(0.20);
            assertThat(budget.currentRetryRatio()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should return correct ratio")
        void shouldReturnCorrectRatio() {
            RetryBudget budget = RetryBudget.ofRatio(0.50);
            budget.recordRequest();
            budget.recordRequest();
            budget.recordRequest();
            budget.recordRequest();
            budget.recordRetry();
            assertThat(budget.currentRetryRatio()).isEqualTo(0.25);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("should contain ratio and counts")
        void shouldContainInfo() {
            RetryBudget budget = RetryBudget.ofRatio(0.20);
            budget.recordRequest();
            String str = budget.toString();
            assertThat(str).contains("RetryBudget")
                    .contains("0.2")
                    .contains("requests=1");
        }
    }
}
