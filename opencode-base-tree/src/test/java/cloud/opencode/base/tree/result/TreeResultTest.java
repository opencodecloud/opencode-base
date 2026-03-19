package cloud.opencode.base.tree.result;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeResultTest Tests
 * TreeResultTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("TreeResult Tests")
class TreeResultTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("success should create Success result")
        void successShouldCreateSuccessResult() {
            TreeResult<String> result = TreeResult.success("data");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrNull()).isEqualTo("data");
        }

        @Test
        @DisplayName("failure should create Failure result")
        void failureShouldCreateFailureResult() {
            TreeResult<String> result = TreeResult.failure("error");

            assertThat(result.isFailed()).isTrue();
        }

        @Test
        @DisplayName("failure with cause should include cause")
        void failureWithCauseShouldIncludeCause() {
            RuntimeException cause = new RuntimeException("cause");
            TreeResult<String> result = TreeResult.failure("error", cause);

            assertThat(result.isFailed()).isTrue();
        }

        @Test
        @DisplayName("empty should create Empty result")
        void emptyShouldCreateEmptyResult() {
            TreeResult<String> result = TreeResult.empty();

            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("validation should create Validation result")
        void validationShouldCreateValidationResult() {
            TreeResult<String> result = TreeResult.validation(
                List.of(TreeResult.Violation.of("error")));

            assertThat(result.isValidation()).isTrue();
        }

        @Test
        @DisplayName("fromOptional should create result from Optional")
        void fromOptionalShouldCreateResultFromOptional() {
            assertThat(TreeResult.fromOptional(Optional.of("data")).isSuccess()).isTrue();
            assertThat(TreeResult.fromOptional(Optional.empty()).isEmpty()).isTrue();
        }

        @Test
        @DisplayName("fromNullable should create result from nullable")
        void fromNullableShouldCreateResultFromNullable() {
            assertThat(TreeResult.fromNullable("data").isSuccess()).isTrue();
            assertThat(TreeResult.fromNullable(null).isEmpty()).isTrue();
        }

        @Test
        @DisplayName("fromCallable should handle successful call")
        void fromCallableShouldHandleSuccessfulCall() {
            TreeResult<String> result = TreeResult.fromCallable(() -> "data");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrNull()).isEqualTo("data");
        }

        @Test
        @DisplayName("fromCallable should handle exception")
        void fromCallableShouldHandleException() {
            TreeResult<String> result = TreeResult.fromCallable(() -> {
                throw new RuntimeException("error");
            });

            assertThat(result.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("State Query Tests")
    class StateQueryTests {

        @Test
        @DisplayName("isSuccess should return correct state")
        void isSuccessShouldReturnCorrectState() {
            assertThat(TreeResult.success("data").isSuccess()).isTrue();
            assertThat(TreeResult.failure("error").isSuccess()).isFalse();
        }

        @Test
        @DisplayName("isFailed should return correct state")
        void isFailedShouldReturnCorrectState() {
            assertThat(TreeResult.failure("error").isFailed()).isTrue();
            assertThat(TreeResult.success("data").isFailed()).isFalse();
        }

        @Test
        @DisplayName("isEmpty should return correct state")
        void isEmptyShouldReturnCorrectState() {
            assertThat(TreeResult.empty().isEmpty()).isTrue();
            assertThat(TreeResult.success("data").isEmpty()).isFalse();
        }

        @Test
        @DisplayName("isValidation should return correct state")
        void isValidationShouldReturnCorrectState() {
            assertThat(TreeResult.validation(List.of()).isValidation()).isTrue();
            assertThat(TreeResult.success("data").isValidation()).isFalse();
        }
    }

    @Nested
    @DisplayName("Value Access Tests")
    class ValueAccessTests {

        @Test
        @DisplayName("getOrNull should return value for success")
        void getOrNullShouldReturnValueForSuccess() {
            assertThat(TreeResult.success("data").getOrNull()).isEqualTo("data");
        }

        @Test
        @DisplayName("getOrNull should return null for failure")
        void getOrNullShouldReturnNullForFailure() {
            assertThat(TreeResult.failure("error").getOrNull()).isNull();
        }

        @Test
        @DisplayName("getOrThrow should return value for success")
        void getOrThrowShouldReturnValueForSuccess() {
            assertThat(TreeResult.success("data").getOrThrow()).isEqualTo("data");
        }

        @Test
        @DisplayName("getOrThrow should throw for failure")
        void getOrThrowShouldThrowForFailure() {
            assertThatThrownBy(() -> TreeResult.failure("error").getOrThrow())
                .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("getOrThrow with supplier should throw custom exception")
        void getOrThrowWithSupplierShouldThrowCustomException() {
            assertThatThrownBy(() ->
                TreeResult.failure("error").getOrThrow(IllegalStateException::new))
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("getOrElse should return value for success")
        void getOrElseShouldReturnValueForSuccess() {
            assertThat(TreeResult.success("data").getOrElse("default")).isEqualTo("data");
        }

        @Test
        @DisplayName("getOrElse should return default for failure")
        void getOrElseShouldReturnDefaultForFailure() {
            assertThat(TreeResult.failure("error").getOrElse("default")).isEqualTo("default");
        }

        @Test
        @DisplayName("getOrElseGet should return value for success")
        void getOrElseGetShouldReturnValueForSuccess() {
            assertThat(TreeResult.success("data").getOrElseGet(() -> "default")).isEqualTo("data");
        }

        @Test
        @DisplayName("getOrElseGet should compute default for failure")
        void getOrElseGetShouldComputeDefaultForFailure() {
            assertThat(TreeResult.failure("error").getOrElseGet(() -> "default")).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("Transformation Tests")
    class TransformationTests {

        @Test
        @DisplayName("map should transform success value")
        void mapShouldTransformSuccessValue() {
            TreeResult<Integer> result = TreeResult.success("data").map(String::length);

            assertThat(result.getOrNull()).isEqualTo(4);
        }

        @Test
        @DisplayName("map should propagate failure")
        void mapShouldPropagateFailure() {
            TreeResult<Integer> result = TreeResult.<String>failure("error").map(String::length);

            assertThat(result.isFailed()).isTrue();
        }

        @Test
        @DisplayName("flatMap should transform with result function")
        void flatMapShouldTransformWithResultFunction() {
            TreeResult<Integer> result = TreeResult.success("data")
                .flatMap(s -> TreeResult.success(s.length()));

            assertThat(result.getOrNull()).isEqualTo(4);
        }

        @Test
        @DisplayName("recover should transform failure")
        void recoverShouldTransformFailure() {
            TreeResult<String> result = TreeResult.<String>failure("error")
                .recover(msg -> "recovered: " + msg);

            assertThat(result.getOrNull()).isEqualTo("recovered: error");
        }

        @Test
        @DisplayName("recoverWith should transform failure to result")
        void recoverWithShouldTransformFailureToResult() {
            TreeResult<String> result = TreeResult.<String>failure("error")
                .recoverWith(msg -> TreeResult.success("recovered"));

            assertThat(result.getOrNull()).isEqualTo("recovered");
        }
    }

    @Nested
    @DisplayName("Callback Tests")
    class CallbackTests {

        @Test
        @DisplayName("onSuccess should execute for success")
        void onSuccessShouldExecuteForSuccess() {
            AtomicBoolean called = new AtomicBoolean(false);

            TreeResult.success("data").onSuccess(d -> called.set(true));

            assertThat(called).isTrue();
        }

        @Test
        @DisplayName("onFailure should execute for failure")
        void onFailureShouldExecuteForFailure() {
            AtomicBoolean called = new AtomicBoolean(false);

            TreeResult.failure("error").onFailure(e -> called.set(true));

            assertThat(called).isTrue();
        }

        @Test
        @DisplayName("onEmpty should execute for empty")
        void onEmptyShouldExecuteForEmpty() {
            AtomicBoolean called = new AtomicBoolean(false);

            TreeResult.empty().onEmpty(() -> called.set(true));

            assertThat(called).isTrue();
        }

        @Test
        @DisplayName("onValidation should execute for validation")
        void onValidationShouldExecuteForValidation() {
            AtomicBoolean called = new AtomicBoolean(false);

            TreeResult.validation(List.of(TreeResult.Violation.of("error")))
                .onValidation(v -> called.set(true));

            assertThat(called).isTrue();
        }
    }

    @Nested
    @DisplayName("Conversion Tests")
    class ConversionTests {

        @Test
        @DisplayName("toOptional should convert to Optional")
        void toOptionalShouldConvertToOptional() {
            assertThat(TreeResult.success("data").toOptional()).contains("data");
            assertThat(TreeResult.failure("error").toOptional()).isEmpty();
        }

        @Test
        @DisplayName("fold should apply appropriate function")
        void foldShouldApplyAppropriateFunction() {
            String result = TreeResult.success("data").fold(
                d -> "success: " + d,
                e -> "failure: " + e,
                () -> "empty",
                v -> "validation: " + v.size()
            );

            assertThat(result).isEqualTo("success: data");
        }
    }

    @Nested
    @DisplayName("Violation Tests")
    class ViolationTests {

        @Test
        @DisplayName("Violation.of should create simple violation")
        void violationOfShouldCreateSimpleViolation() {
            TreeResult.Violation v = TreeResult.Violation.of("error");

            assertThat(v.message()).isEqualTo("error");
            assertThat(v.severity()).isEqualTo(TreeResult.Violation.Severity.ERROR);
        }

        @Test
        @DisplayName("Violation.error should create error violation")
        void violationErrorShouldCreateErrorViolation() {
            TreeResult.Violation v = TreeResult.Violation.error("field", "error");

            assertThat(v.field()).isEqualTo("field");
            assertThat(v.isError()).isTrue();
        }

        @Test
        @DisplayName("Violation.warning should create warning violation")
        void violationWarningShouldCreateWarningViolation() {
            TreeResult.Violation v = TreeResult.Violation.warning("field", "warning");

            assertThat(v.isWarning()).isTrue();
        }

        @Test
        @DisplayName("Violation.info should create info violation")
        void violationInfoShouldCreateInfoViolation() {
            TreeResult.Violation v = TreeResult.Violation.info("field", "info");

            assertThat(v.isInfo()).isTrue();
        }
    }

    @Nested
    @DisplayName("Validation Result Tests")
    class ValidationResultTests {

        @Test
        @DisplayName("Validation should support getting violations by severity")
        void validationShouldSupportGettingViolationsBySeverity() {
            TreeResult.Validation<String> result = new TreeResult.Validation<>(List.of(
                TreeResult.Violation.error("f1", "error1"),
                TreeResult.Violation.warning("f2", "warning1"),
                TreeResult.Violation.error("f3", "error2")
            ));

            assertThat(result.getErrors()).hasSize(2);
            assertThat(result.getWarnings()).hasSize(1);
        }

        @Test
        @DisplayName("Validation should return combined message")
        void validationShouldReturnCombinedMessage() {
            TreeResult.Validation<String> result = new TreeResult.Validation<>(List.of(
                TreeResult.Violation.of("error1"),
                TreeResult.Violation.of("error2")
            ));

            assertThat(result.getCombinedMessage()).isEqualTo("error1; error2");
        }
    }

    @Nested
    @DisplayName("Failure Tests")
    class FailureTests {

        @Test
        @DisplayName("Failure should track root cause")
        void failureShouldTrackRootCause() {
            RuntimeException root = new RuntimeException("root");
            RuntimeException wrapper = new RuntimeException("wrapper", root);
            TreeResult.Failure<String> failure = new TreeResult.Failure<>("error", wrapper);

            assertThat(failure.hasCause()).isTrue();
            assertThat(failure.getRootCause()).isEqualTo(root);
        }
    }
}
