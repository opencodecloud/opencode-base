/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.captcha;

import cloud.opencode.base.captcha.ValidationResult.ResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for ValidationResult record
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("ValidationResult Tests")
class ValidationResultTest {

    @Nested
    @DisplayName("ok() Factory Method Tests")
    class OkFactoryMethodTests {

        @Test
        @DisplayName("ok returns successful result")
        void okReturnsSuccessfulResult() {
            ValidationResult result = ValidationResult.ok();

            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("ok returns correct message")
        void okReturnsCorrectMessage() {
            ValidationResult result = ValidationResult.ok();

            assertThat(result.message()).isEqualTo("Validation successful");
        }

        @Test
        @DisplayName("ok returns SUCCESS result code")
        void okReturnsSuccessResultCode() {
            ValidationResult result = ValidationResult.ok();

            assertThat(result.code()).isEqualTo(ResultCode.SUCCESS);
        }

        @Test
        @DisplayName("ok result is not failed")
        void okResultIsNotFailed() {
            ValidationResult result = ValidationResult.ok();

            assertThat(result.isFailed()).isFalse();
        }
    }

    @Nested
    @DisplayName("notFound() Factory Method Tests")
    class NotFoundFactoryMethodTests {

        @Test
        @DisplayName("notFound returns failed result")
        void notFoundReturnsFailedResult() {
            ValidationResult result = ValidationResult.notFound();

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("notFound returns correct message")
        void notFoundReturnsCorrectMessage() {
            ValidationResult result = ValidationResult.notFound();

            assertThat(result.message()).isEqualTo("CAPTCHA not found");
        }

        @Test
        @DisplayName("notFound returns NOT_FOUND result code")
        void notFoundReturnsNotFoundResultCode() {
            ValidationResult result = ValidationResult.notFound();

            assertThat(result.code()).isEqualTo(ResultCode.NOT_FOUND);
        }

        @Test
        @DisplayName("notFound result is failed")
        void notFoundResultIsFailed() {
            ValidationResult result = ValidationResult.notFound();

            assertThat(result.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("expired() Factory Method Tests")
    class ExpiredFactoryMethodTests {

        @Test
        @DisplayName("expired returns failed result")
        void expiredReturnsFailedResult() {
            ValidationResult result = ValidationResult.expired();

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("expired returns correct message")
        void expiredReturnsCorrectMessage() {
            ValidationResult result = ValidationResult.expired();

            assertThat(result.message()).isEqualTo("CAPTCHA has expired");
        }

        @Test
        @DisplayName("expired returns EXPIRED result code")
        void expiredReturnsExpiredResultCode() {
            ValidationResult result = ValidationResult.expired();

            assertThat(result.code()).isEqualTo(ResultCode.EXPIRED);
        }

        @Test
        @DisplayName("expired result is failed")
        void expiredResultIsFailed() {
            ValidationResult result = ValidationResult.expired();

            assertThat(result.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("mismatch() Factory Method Tests")
    class MismatchFactoryMethodTests {

        @Test
        @DisplayName("mismatch returns failed result")
        void mismatchReturnsFailedResult() {
            ValidationResult result = ValidationResult.mismatch();

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("mismatch returns correct message")
        void mismatchReturnsCorrectMessage() {
            ValidationResult result = ValidationResult.mismatch();

            assertThat(result.message()).isEqualTo("Answer does not match");
        }

        @Test
        @DisplayName("mismatch returns MISMATCH result code")
        void mismatchReturnsMismatchResultCode() {
            ValidationResult result = ValidationResult.mismatch();

            assertThat(result.code()).isEqualTo(ResultCode.MISMATCH);
        }

        @Test
        @DisplayName("mismatch result is failed")
        void mismatchResultIsFailed() {
            ValidationResult result = ValidationResult.mismatch();

            assertThat(result.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("rateLimited() Factory Method Tests")
    class RateLimitedFactoryMethodTests {

        @Test
        @DisplayName("rateLimited returns failed result")
        void rateLimitedReturnsFailedResult() {
            ValidationResult result = ValidationResult.rateLimited();

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("rateLimited returns correct message")
        void rateLimitedReturnsCorrectMessage() {
            ValidationResult result = ValidationResult.rateLimited();

            assertThat(result.message()).isEqualTo("Too many attempts, please try again later");
        }

        @Test
        @DisplayName("rateLimited returns RATE_LIMITED result code")
        void rateLimitedReturnsRateLimitedResultCode() {
            ValidationResult result = ValidationResult.rateLimited();

            assertThat(result.code()).isEqualTo(ResultCode.RATE_LIMITED);
        }

        @Test
        @DisplayName("rateLimited result is failed")
        void rateLimitedResultIsFailed() {
            ValidationResult result = ValidationResult.rateLimited();

            assertThat(result.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("invalidInput() Factory Method Tests")
    class InvalidInputFactoryMethodTests {

        @Test
        @DisplayName("invalidInput returns failed result")
        void invalidInputReturnsFailedResult() {
            ValidationResult result = ValidationResult.invalidInput();

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("invalidInput returns correct message")
        void invalidInputReturnsCorrectMessage() {
            ValidationResult result = ValidationResult.invalidInput();

            assertThat(result.message()).isEqualTo("Invalid input provided");
        }

        @Test
        @DisplayName("invalidInput returns INVALID_INPUT result code")
        void invalidInputReturnsInvalidInputResultCode() {
            ValidationResult result = ValidationResult.invalidInput();

            assertThat(result.code()).isEqualTo(ResultCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("invalidInput result is failed")
        void invalidInputResultIsFailed() {
            ValidationResult result = ValidationResult.invalidInput();

            assertThat(result.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("suspiciousBehavior() Factory Method Tests")
    class SuspiciousBehaviorFactoryMethodTests {

        @Test
        @DisplayName("suspiciousBehavior returns failed result")
        void suspiciousBehaviorReturnsFailedResult() {
            ValidationResult result = ValidationResult.suspiciousBehavior();

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("suspiciousBehavior returns correct message")
        void suspiciousBehaviorReturnsCorrectMessage() {
            ValidationResult result = ValidationResult.suspiciousBehavior();

            assertThat(result.message()).isEqualTo("Suspicious behavior detected");
        }

        @Test
        @DisplayName("suspiciousBehavior returns SUSPICIOUS_BEHAVIOR result code")
        void suspiciousBehaviorReturnsSuspiciousBehaviorResultCode() {
            ValidationResult result = ValidationResult.suspiciousBehavior();

            assertThat(result.code()).isEqualTo(ResultCode.SUSPICIOUS_BEHAVIOR);
        }

        @Test
        @DisplayName("suspiciousBehavior result is failed")
        void suspiciousBehaviorResultIsFailed() {
            ValidationResult result = ValidationResult.suspiciousBehavior();

            assertThat(result.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("Record Accessor Tests")
    class RecordAccessorTests {

        @Test
        @DisplayName("success accessor returns correct value for successful result")
        void successAccessorReturnsTrueForSuccess() {
            ValidationResult result = new ValidationResult(true, "test", ResultCode.SUCCESS);

            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("success accessor returns correct value for failed result")
        void successAccessorReturnsFalseForFailure() {
            ValidationResult result = new ValidationResult(false, "test", ResultCode.MISMATCH);

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("message accessor returns correct value")
        void messageAccessorReturnsCorrectValue() {
            ValidationResult result = new ValidationResult(true, "Custom message", ResultCode.SUCCESS);

            assertThat(result.message()).isEqualTo("Custom message");
        }

        @Test
        @DisplayName("code accessor returns correct value")
        void codeAccessorReturnsCorrectValue() {
            ValidationResult result = new ValidationResult(false, "test", ResultCode.EXPIRED);

            assertThat(result.code()).isEqualTo(ResultCode.EXPIRED);
        }

        @Test
        @DisplayName("message can be null when constructed directly")
        void messageCanBeNull() {
            ValidationResult result = new ValidationResult(false, null, ResultCode.NOT_FOUND);

            assertThat(result.message()).isNull();
        }

        @Test
        @DisplayName("code can be null when constructed directly")
        void codeCanBeNull() {
            ValidationResult result = new ValidationResult(false, "test", null);

            assertThat(result.code()).isNull();
        }
    }

    @Nested
    @DisplayName("isFailed Tests")
    class IsFailedTests {

        @Test
        @DisplayName("isFailed returns false for ok result")
        void isFailedReturnsFalseForOk() {
            assertThat(ValidationResult.ok().isFailed()).isFalse();
        }

        @Test
        @DisplayName("isFailed returns true for notFound result")
        void isFailedReturnsTrueForNotFound() {
            assertThat(ValidationResult.notFound().isFailed()).isTrue();
        }

        @Test
        @DisplayName("isFailed returns true for expired result")
        void isFailedReturnsTrueForExpired() {
            assertThat(ValidationResult.expired().isFailed()).isTrue();
        }

        @Test
        @DisplayName("isFailed returns true for mismatch result")
        void isFailedReturnsTrueForMismatch() {
            assertThat(ValidationResult.mismatch().isFailed()).isTrue();
        }

        @Test
        @DisplayName("isFailed returns true for rateLimited result")
        void isFailedReturnsTrueForRateLimited() {
            assertThat(ValidationResult.rateLimited().isFailed()).isTrue();
        }

        @Test
        @DisplayName("isFailed returns true for invalidInput result")
        void isFailedReturnsTrueForInvalidInput() {
            assertThat(ValidationResult.invalidInput().isFailed()).isTrue();
        }

        @Test
        @DisplayName("isFailed returns true for suspiciousBehavior result")
        void isFailedReturnsTrueForSuspiciousBehavior() {
            assertThat(ValidationResult.suspiciousBehavior().isFailed()).isTrue();
        }

        @Test
        @DisplayName("isFailed is opposite of success")
        void isFailedIsOppositeOfSuccess() {
            ValidationResult success = ValidationResult.ok();
            ValidationResult failure = ValidationResult.mismatch();

            assertThat(success.isFailed()).isNotEqualTo(success.success());
            assertThat(failure.isFailed()).isNotEqualTo(failure.success());
        }

        @Test
        @DisplayName("isFailed returns false for custom successful result")
        void isFailedReturnsFalseForCustomSuccess() {
            ValidationResult result = new ValidationResult(true, "custom", ResultCode.SUCCESS);

            assertThat(result.isFailed()).isFalse();
        }

        @Test
        @DisplayName("isFailed returns true for custom failed result")
        void isFailedReturnsTrueForCustomFailure() {
            ValidationResult result = new ValidationResult(false, "custom", ResultCode.NOT_FOUND);

            assertThat(result.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("ResultCode Enum Tests")
    class ResultCodeEnumTests {

        @Test
        @DisplayName("ResultCode has exactly 7 values")
        void resultCodeHasSevenValues() {
            assertThat(ResultCode.values()).hasSize(7);
        }

        @Test
        @DisplayName("ResultCode contains SUCCESS")
        void resultCodeContainsSuccess() {
            assertThat(ResultCode.valueOf("SUCCESS")).isEqualTo(ResultCode.SUCCESS);
        }

        @Test
        @DisplayName("ResultCode contains NOT_FOUND")
        void resultCodeContainsNotFound() {
            assertThat(ResultCode.valueOf("NOT_FOUND")).isEqualTo(ResultCode.NOT_FOUND);
        }

        @Test
        @DisplayName("ResultCode contains EXPIRED")
        void resultCodeContainsExpired() {
            assertThat(ResultCode.valueOf("EXPIRED")).isEqualTo(ResultCode.EXPIRED);
        }

        @Test
        @DisplayName("ResultCode contains MISMATCH")
        void resultCodeContainsMismatch() {
            assertThat(ResultCode.valueOf("MISMATCH")).isEqualTo(ResultCode.MISMATCH);
        }

        @Test
        @DisplayName("ResultCode contains RATE_LIMITED")
        void resultCodeContainsRateLimited() {
            assertThat(ResultCode.valueOf("RATE_LIMITED")).isEqualTo(ResultCode.RATE_LIMITED);
        }

        @Test
        @DisplayName("ResultCode contains INVALID_INPUT")
        void resultCodeContainsInvalidInput() {
            assertThat(ResultCode.valueOf("INVALID_INPUT")).isEqualTo(ResultCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("ResultCode contains SUSPICIOUS_BEHAVIOR")
        void resultCodeContainsSuspiciousBehavior() {
            assertThat(ResultCode.valueOf("SUSPICIOUS_BEHAVIOR")).isEqualTo(ResultCode.SUSPICIOUS_BEHAVIOR);
        }

        @Test
        @DisplayName("ResultCode values are in expected order")
        void resultCodeValuesInExpectedOrder() {
            assertThat(ResultCode.values()).containsExactly(
                ResultCode.SUCCESS,
                ResultCode.NOT_FOUND,
                ResultCode.EXPIRED,
                ResultCode.MISMATCH,
                ResultCode.RATE_LIMITED,
                ResultCode.INVALID_INPUT,
                ResultCode.SUSPICIOUS_BEHAVIOR
            );
        }

        @Test
        @DisplayName("ResultCode valueOf throws for invalid name")
        void resultCodeValueOfThrowsForInvalidName() {
            assertThatThrownBy(() -> ResultCode.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Record Equality Tests")
    class RecordEqualityTests {

        @Test
        @DisplayName("equal results are equal")
        void equalResultsAreEqual() {
            ValidationResult result1 = new ValidationResult(true, "test", ResultCode.SUCCESS);
            ValidationResult result2 = new ValidationResult(true, "test", ResultCode.SUCCESS);

            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("results with different success are not equal")
        void resultsWithDifferentSuccessAreNotEqual() {
            ValidationResult result1 = new ValidationResult(true, "test", ResultCode.SUCCESS);
            ValidationResult result2 = new ValidationResult(false, "test", ResultCode.SUCCESS);

            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("results with different messages are not equal")
        void resultsWithDifferentMessagesAreNotEqual() {
            ValidationResult result1 = new ValidationResult(false, "msg1", ResultCode.MISMATCH);
            ValidationResult result2 = new ValidationResult(false, "msg2", ResultCode.MISMATCH);

            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("results with different codes are not equal")
        void resultsWithDifferentCodesAreNotEqual() {
            ValidationResult result1 = new ValidationResult(false, "test", ResultCode.EXPIRED);
            ValidationResult result2 = new ValidationResult(false, "test", ResultCode.NOT_FOUND);

            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("hashCode is consistent for equal results")
        void hashCodeIsConsistentForEqualResults() {
            ValidationResult result1 = new ValidationResult(true, "test", ResultCode.SUCCESS);
            ValidationResult result2 = new ValidationResult(true, "test", ResultCode.SUCCESS);

            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }

        @Test
        @DisplayName("toString returns non-null string")
        void toStringReturnsNonNullString() {
            ValidationResult result = ValidationResult.ok();

            assertThat(result.toString()).isNotNull();
            assertThat(result.toString()).isNotEmpty();
        }

        @Test
        @DisplayName("toString contains ValidationResult")
        void toStringContainsClassName() {
            ValidationResult result = ValidationResult.ok();

            assertThat(result.toString()).contains("ValidationResult");
        }
    }

    @Nested
    @DisplayName("Factory Method Consistency Tests")
    class FactoryMethodConsistencyTests {

        @Test
        @DisplayName("only ok returns successful result")
        void onlyOkReturnsSuccessfulResult() {
            assertThat(ValidationResult.ok().success()).isTrue();
            assertThat(ValidationResult.notFound().success()).isFalse();
            assertThat(ValidationResult.expired().success()).isFalse();
            assertThat(ValidationResult.mismatch().success()).isFalse();
            assertThat(ValidationResult.rateLimited().success()).isFalse();
            assertThat(ValidationResult.invalidInput().success()).isFalse();
            assertThat(ValidationResult.suspiciousBehavior().success()).isFalse();
        }

        @Test
        @DisplayName("all failure factory methods return non-empty messages")
        void allFailureFactoryMethodsReturnNonEmptyMessages() {
            assertThat(ValidationResult.notFound().message()).isNotEmpty();
            assertThat(ValidationResult.expired().message()).isNotEmpty();
            assertThat(ValidationResult.mismatch().message()).isNotEmpty();
            assertThat(ValidationResult.rateLimited().message()).isNotEmpty();
            assertThat(ValidationResult.invalidInput().message()).isNotEmpty();
            assertThat(ValidationResult.suspiciousBehavior().message()).isNotEmpty();
        }

        @Test
        @DisplayName("all factory methods return distinct result codes")
        void allFactoryMethodsReturnDistinctResultCodes() {
            assertThat(ValidationResult.ok().code()).isEqualTo(ResultCode.SUCCESS);
            assertThat(ValidationResult.notFound().code()).isEqualTo(ResultCode.NOT_FOUND);
            assertThat(ValidationResult.expired().code()).isEqualTo(ResultCode.EXPIRED);
            assertThat(ValidationResult.mismatch().code()).isEqualTo(ResultCode.MISMATCH);
            assertThat(ValidationResult.rateLimited().code()).isEqualTo(ResultCode.RATE_LIMITED);
            assertThat(ValidationResult.invalidInput().code()).isEqualTo(ResultCode.INVALID_INPUT);
            assertThat(ValidationResult.suspiciousBehavior().code()).isEqualTo(ResultCode.SUSPICIOUS_BEHAVIOR);
        }

        @Test
        @DisplayName("factory methods produce new instances each call")
        void factoryMethodsProduceNewInstances() {
            ValidationResult ok1 = ValidationResult.ok();
            ValidationResult ok2 = ValidationResult.ok();

            // Records with same values are equals, but not same reference
            assertThat(ok1).isEqualTo(ok2);
        }
    }
}
