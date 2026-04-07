package cloud.opencode.base.web;

import cloud.opencode.base.web.exception.OpenBizException;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * ResultTest Tests
 * ResultTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("Result Tests")
class ResultTest {

    @Nested
    @DisplayName("Static Factory Methods Tests")
    class StaticFactoryMethodsTests {

        @Test
        @DisplayName("ok should create success result without data")
        void okShouldCreateSuccessResultWithoutData() {
            Result<String> result = Result.ok();

            assertThat(result.success()).isTrue();
            assertThat(result.code()).isEqualTo(CommonResultCode.SUCCESS.getCode());
            assertThat(result.data()).isNull();
            assertThat(result.timestamp()).isNotNull();
            assertThat(result.traceId()).isNotNull();
        }

        @Test
        @DisplayName("ok with data should create success result with data")
        void okWithDataShouldCreateSuccessResultWithData() {
            Result<String> result = Result.ok("test data");

            assertThat(result.success()).isTrue();
            assertThat(result.data()).isEqualTo("test data");
        }

        @Test
        @DisplayName("ok with message and data should create success result")
        void okWithMessageAndDataShouldCreateSuccessResult() {
            Result<Integer> result = Result.ok("Custom message", 42);

            assertThat(result.success()).isTrue();
            assertThat(result.message()).isEqualTo("Custom message");
            assertThat(result.data()).isEqualTo(42);
        }

        @Test
        @DisplayName("fail should create failure result with code and message")
        void failShouldCreateFailureResultWithCodeAndMessage() {
            Result<String> result = Result.fail("E001", "Error message");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo("E001");
            assertThat(result.message()).isEqualTo("Error message");
        }

        @Test
        @DisplayName("fail with result code should create failure result")
        void failWithResultCodeShouldCreateFailureResult() {
            Result<String> result = Result.fail(CommonResultCode.BAD_REQUEST);

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(CommonResultCode.BAD_REQUEST.getCode());
        }

        @Test
        @DisplayName("fail with result code and message should create failure result")
        void failWithResultCodeAndMessageShouldCreateFailureResult() {
            Result<String> result = Result.fail(CommonResultCode.VALIDATION_ERROR, "Custom validation error");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(CommonResultCode.VALIDATION_ERROR.getCode());
            assertThat(result.message()).isEqualTo("Custom validation error");
        }

        @Test
        @DisplayName("fail with exception should create failure result with generic message")
        void failWithExceptionShouldCreateFailureResult() {
            Result<String> result = Result.fail(new RuntimeException("Test error"));

            assertThat(result.success()).isFalse();
            // Generic message to prevent internal info leakage
            assertThat(result.message()).isEqualTo("Internal server error");
        }
    }

    @Nested
    @DisplayName("Instance Methods Tests")
    class InstanceMethodsTests {

        @Test
        @DisplayName("isFailed should return true for failure result")
        void isFailedShouldReturnTrueForFailureResult() {
            Result<String> result = Result.fail("E001", "Error");

            assertThat(result.isFailed()).isTrue();
        }

        @Test
        @DisplayName("isFailed should return false for success result")
        void isFailedShouldReturnFalseForSuccessResult() {
            Result<String> result = Result.ok("data");

            assertThat(result.isFailed()).isFalse();
        }

        @Test
        @DisplayName("getDataOrThrow should return data for success result")
        void getDataOrThrowShouldReturnDataForSuccessResult() {
            Result<String> result = Result.ok("data");

            assertThat(result.getDataOrThrow()).isEqualTo("data");
        }

        @Test
        @DisplayName("getDataOrThrow should throw exception for failure result")
        void getDataOrThrowShouldThrowExceptionForFailureResult() {
            Result<String> result = Result.fail("E001", "Error message");

            assertThatThrownBy(result::getDataOrThrow)
                .isInstanceOf(OpenBizException.class)
                .hasMessage("Error message");
        }

        @Test
        @DisplayName("getDataOrDefault should return data for success result")
        void getDataOrDefaultShouldReturnDataForSuccessResult() {
            Result<String> result = Result.ok("data");

            assertThat(result.getDataOrDefault("default")).isEqualTo("data");
        }

        @Test
        @DisplayName("getDataOrDefault should return default for failure result")
        void getDataOrDefaultShouldReturnDefaultForFailureResult() {
            Result<String> result = Result.fail("E001", "Error");

            assertThat(result.getDataOrDefault("default")).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("Map Operations Tests")
    class MapOperationsTests {

        @Test
        @DisplayName("map should transform data for success result")
        void mapShouldTransformDataForSuccessResult() {
            Result<Integer> result = Result.ok(5);

            Result<String> mapped = result.map(i -> "Value: " + i);

            assertThat(mapped.success()).isTrue();
            assertThat(mapped.data()).isEqualTo("Value: 5");
        }

        @Test
        @DisplayName("map should preserve failure for failure result")
        void mapShouldPreserveFailureForFailureResult() {
            Result<Integer> result = Result.fail("E001", "Error");

            Result<String> mapped = result.map(i -> "Value: " + i);

            assertThat(mapped.success()).isFalse();
            assertThat(mapped.code()).isEqualTo("E001");
        }

        @Test
        @DisplayName("map should handle null data")
        void mapShouldHandleNullData() {
            Result<String> result = Result.ok();

            Result<Integer> mapped = result.map(s -> s != null ? s.length() : 0);

            assertThat(mapped.success()).isTrue();
            assertThat(mapped.data()).isNull();
        }

        @Test
        @DisplayName("flatMap should chain results for success")
        void flatMapShouldChainResultsForSuccess() {
            Result<Integer> result = Result.ok(5);

            Result<String> flatMapped = result.flatMap(i -> Result.ok("Value: " + i));

            assertThat(flatMapped.success()).isTrue();
            assertThat(flatMapped.data()).isEqualTo("Value: 5");
        }

        @Test
        @DisplayName("flatMap should preserve failure")
        void flatMapShouldPreserveFailure() {
            Result<Integer> result = Result.fail("E001", "Error");

            Result<String> flatMapped = result.flatMap(i -> Result.ok("Value: " + i));

            assertThat(flatMapped.success()).isFalse();
            assertThat(flatMapped.code()).isEqualTo("E001");
        }
    }

    @Nested
    @DisplayName("Callback Operations Tests")
    class CallbackOperationsTests {

        @Test
        @DisplayName("onSuccess should execute action for success result")
        void onSuccessShouldExecuteActionForSuccessResult() {
            AtomicReference<String> captured = new AtomicReference<>();
            Result<String> result = Result.ok("data");

            result.onSuccess(captured::set);

            assertThat(captured.get()).isEqualTo("data");
        }

        @Test
        @DisplayName("onSuccess should not execute action for failure result")
        void onSuccessShouldNotExecuteActionForFailureResult() {
            AtomicBoolean executed = new AtomicBoolean(false);
            Result<String> result = Result.fail("E001", "Error");

            result.onSuccess(d -> executed.set(true));

            assertThat(executed.get()).isFalse();
        }

        @Test
        @DisplayName("onFailure should execute action for failure result")
        void onFailureShouldExecuteActionForFailureResult() {
            AtomicReference<String> capturedCode = new AtomicReference<>();
            AtomicReference<String> capturedMessage = new AtomicReference<>();
            Result<String> result = Result.fail("E001", "Error message");

            result.onFailure((code, message) -> {
                capturedCode.set(code);
                capturedMessage.set(message);
            });

            assertThat(capturedCode.get()).isEqualTo("E001");
            assertThat(capturedMessage.get()).isEqualTo("Error message");
        }

        @Test
        @DisplayName("onFailure should not execute action for success result")
        void onFailureShouldNotExecuteActionForSuccessResult() {
            AtomicBoolean executed = new AtomicBoolean(false);
            Result<String> result = Result.ok("data");

            result.onFailure((code, message) -> executed.set(true));

            assertThat(executed.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("With Methods Tests")
    class WithMethodsTests {

        @Test
        @DisplayName("withTraceId should create new result with different trace ID")
        void withTraceIdShouldCreateNewResultWithDifferentTraceId() {
            Result<String> result = Result.ok("data");
            String newTraceId = "new-trace-id";

            Result<String> newResult = result.withTraceId(newTraceId);

            assertThat(newResult.traceId()).isEqualTo(newTraceId);
            assertThat(newResult.data()).isEqualTo("data");
            assertThat(newResult.success()).isTrue();
        }

        @Test
        @DisplayName("withMessage should create new result with different message")
        void withMessageShouldCreateNewResultWithDifferentMessage() {
            Result<String> result = Result.ok("data");
            String newMessage = "New message";

            Result<String> newResult = result.withMessage(newMessage);

            assertThat(newResult.message()).isEqualTo(newMessage);
            assertThat(newResult.data()).isEqualTo("data");
        }
    }

    @Nested
    @DisplayName("Record Methods Tests")
    class RecordMethodsTests {

        @Test
        @DisplayName("equals should compare results correctly")
        void equalsShouldCompareResultsCorrectly() {
            Result<String> result1 = new Result<>("00000", "Success", "data", true, null, "trace1");
            Result<String> result2 = new Result<>("00000", "Success", "data", true, null, "trace1");

            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("hashCode should be consistent")
        void hashCodeShouldBeConsistent() {
            Result<String> result1 = new Result<>("00000", "Success", "data", true, null, "trace1");
            Result<String> result2 = new Result<>("00000", "Success", "data", true, null, "trace1");

            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }

        @Test
        @DisplayName("toString should return string representation")
        void toStringShouldReturnStringRepresentation() {
            Result<String> result = new Result<>("00000", "Success", "data", true, null, "trace1");

            assertThat(result.toString()).contains("00000", "Success", "data");
        }
    }
}
